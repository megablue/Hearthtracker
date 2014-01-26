package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import my.hearthtracking.app.HearthScanner.SceneResult;
import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthScannerManager {
	public static final int UNKNOWNMODE = -1;
	public static final int MENUMODE = 0;
	public static final int ARENAMODE = 1;
	public static final int RANKEDMODE = 2;
	public static final int UNRANKEDMODE = 3;
	public static final int CHALLENGEMODE = 4;
	public static final int PRACTICEMODE = 5;
	
	public static final int ARENA_MAX_WINS = 12;
	public static final int ARENA_MAX_LOSSES = 3;
	
	public static final float BASE_RESOLUTION_HEIGHT = 1080f;
	private static final long FPS_LIMIT = 24;
	private static final float FPS_RESOLUTION = 60; //keep approximately 60 seconds of frames
	private static final long GAME_SCAN_INTERVAL = 10000;
	
	private long scannerStarted = System.currentTimeMillis();

	private boolean debugMode = false;
	private boolean autoDetectGameRes = true;
	private boolean inited = false;
	private boolean scannerSettingsInitialzed = false;
	private volatile boolean reInitScannerSettings = false;
	private volatile boolean alwaysScan = true;
	private volatile boolean notify = true;
	private volatile boolean isDirty = true;

	private HearthGameLangList gameLanguages;
	private String gameLang;

	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;
	private static HearthLanguageManager uiLang = HearthLanguageManager.getInstance();

	private int gameResX = 1920, gameResY = 1080;
	private int oldGameResX = -1, oldGameResY = -1;

	private HearthTracker tracker;
	private HearthScannerSettings scannerSettings = null;
	private int xOffetOverrideVal = 0;
	private int yOffsetOverrideVal = 0;

	private HearthScanner scanner = new HearthScanner();
	
	private int totalFramesCounter = 0;
	private long totalTimeSpentCapturing = 0;
	private List<HearthReaderNotification> notifications =  Collections.synchronizedList(new ArrayList<HearthReaderNotification>());
	
	private boolean exMinimized = false;
	
	//Game status related variables
	private long lastArenaScoreReport = System.currentTimeMillis() - 60000;
	private long lastArenaWin = System.currentTimeMillis();
	private int exArenaWins = -1;
	private int arenaWins = -1;
	
	private int exArenaLosses = -1;
	private int arenaLosses = -1;
	
	private int myHero = -1;
	private int oppHero = -1;
	
	private int selectedDeck = -1;
	private int exSelectedDeck = -1;
	
	private int exMyHero = -1;
	private int exOppHero = -1;
	
	private int victory = -1;
	private int exVictory = -1;
	private int goFirst = -1;
	private int exGoFirst = -1;

	private int gameMode = UNKNOWNMODE;
	private int exGameMode = UNKNOWNMODE;
	private int inGameMode = -1;
	
	private int timeslot = 250;
	private long gameStartedTime = System.currentTimeMillis();
	private long lastSaveAttempt = System.currentTimeMillis() - 60000;

	public HearthScannerManager (HearthTracker t, int tslot, String lang, int resX, int resY, boolean autoping, boolean alwaysScanFlag){
		debugMode = HearthHelper.isDevelopmentEnvironment();
		tracker = t;
		gameResX = resX;
		gameResY = resY;
		gameLang = lang.toLowerCase();
		timeslot = tslot;
		
		if(debugMode){
			timeslot = 100;
		}
		
		alwaysScan = alwaysScanFlag;
	}

	public void startup(){
		init();
		initScannerSettings();
	}

	private synchronized void init(){
		if(inited){
			return;
		}
		
		heroesList = (HearthHeroesList) config.load(HearthFilesNameManager.heroesFile);
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, HearthFilesNameManager.heroesFile);
		}
		
		gameLanguages = (HearthGameLangList) config.load(HearthFilesNameManager.gameLangsFile);
		
		if(gameLanguages == null){
			gameLanguages = new HearthGameLangList();
			config.save(gameLanguages, HearthFilesNameManager.gameLangsFile);
		}
		
		inited = true;
	}
	
	private int[] getGameResolution(){
		int[] resolution = {gameResX, gameResY};
		
		if(!autoDetectGameRes){
			return resolution;
		}
		
		int[] winPos = HearthHelper.getHearthstonePosition();

		resolution[0] = winPos[2] - winPos[0];
		resolution[1] = winPos[3] - winPos[1];
		
		//use default resolution if failed to detect
		if(resolution[0] == 0 || resolution[1] == 0){
			resolution[0] = gameResX;
			resolution[1] = gameResY;
		}

		//workaround
		if(resolution[0] < 1024 && resolution[1] < 768){
			System.out.println("Weird resolution change detected.");
			System.out.println("Old resolution: " + oldGameResX + "x" + oldGameResY);
			System.out.println("New resolution: " + resolution[0] + "x" + resolution[1]);

			resolution[0] = oldGameResX;
			resolution[1] = oldGameResY;

			return resolution;
		}
		
		//if resolution is different from previous scan
		if(resolution[0] != oldGameResX || resolution[1] != oldGameResY){
			if(scannerSettingsInitialzed){
				System.out.println("Resolution change detected.");
				System.out.println("Old resolution: " + oldGameResX + "x" + oldGameResY);
				System.out.println("New resolution: " + resolution[0] + "x" + resolution[1]);
			}
			
			oldGameResX = resolution[0];
			oldGameResY = resolution[1];
			
			if(scannerSettingsInitialzed){
				initScannerSettings();
			}
		}
		
		return resolution;
	}
	
	private int getBoardWidth(){
		int[] gameRes = getGameResolution();
		return (gameRes[1]/3) * 4;
	}
	
	private int getBoardHeight(){
		int[] gameRes = getGameResolution();
		return gameRes[1];
	}
		
	private int getAbsoluteBoardXoffset(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int xOffset = winPos[0];
		int ret = xOffset + getRelativeBoardXoffset();
		return ret;
	}
	
	private int getAbsoluteBoardYoffset(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int ret = winPos[1] + getRelativeBoardYoffset();		
		return ret;
	}
	
	private int getRelativeBoardXoffset(){
		int[] gameRes = getGameResolution();
		int ret = (gameRes[0] - getBoardWidth()) / 2;
		return ret;
	}
	
	private int getRelativeBoardYoffset(){
		int ret = 0;		
		return ret;
	}
	
	private float getScaleFactor(){
		int[] gameRes = this.getGameResolution();
		int gameScreenHeight = gameRes[1];
		
		//do not scale if the height is 0 or 1080
		//zero usually means the game is running full screen
		if(gameScreenHeight == 0 || 
		   gameScreenHeight == BASE_RESOLUTION_HEIGHT){
			return 1;
		}
		
		return (gameScreenHeight/BASE_RESOLUTION_HEIGHT);
	}
	
	public synchronized void setXOffetOverride(int val){
		xOffetOverrideVal = val;
	}
	
	public synchronized int getXOffsetOverride(){
		return xOffetOverrideVal;
	}
	
	public synchronized void setYOffetOverride(int val){
		yOffsetOverrideVal = val;
	}
	
	public synchronized int getYOffetOverride(){
		return yOffsetOverrideVal;
	}

	private synchronized void initScannerSettings(){
		scanner.pause();
		
		int[] gameRes = getGameResolution();
		int gameScreenWidth		= gameRes[0];
		int gameScreenHeight	= gameRes[1];
		
		gameLang = sanitizeGameLang(gameLang);
		
		String pathScannerSettingByRes 
		= String.format(
			HearthFilesNameManager.scannerSettingFileOverrideByRes, 
			gameLang, 
			gameScreenWidth, 
			gameScreenHeight
		);
		
		String pathScannerSetting 
		= String.format(
				HearthFilesNameManager.scannerSettingFileDefault, 
				gameLang
		);
		
		scannerSettings = (HearthScannerSettings) config.load(pathScannerSettingByRes);
		
		if(scannerSettings == null){
			scannerSettings = (HearthScannerSettings) config.load(pathScannerSetting);
		}
		
		if(scannerSettings == null){
			scannerSettings = new HearthScannerSettings();
			config.save(scannerSettings, pathScannerSetting);
		}
		
		synchronized(scannerSettings.list){
			scanner.clearScanboxes();
			
			for(Scanbox sb : scannerSettings.list){
				prepareScanbox(sb);
				scanner.addScanbox(sb);
			}
		}
		
		scanner.initScale(getScaleFactor());
		scanner.resume();
		scannerSettingsInitialzed = true;
		reInitScannerSettings = false;
		resetFPS();
	}
		
	private synchronized void prepareScanbox(HearthScannerSettings.Scanbox sb){
		File file = null;
		float scaling = sb.scale * getScaleFactor();
		
		String targetFileLevel1 = String.format(HearthFilesNameManager.scanTargetFileOverrideByLang, gameLang, sb.imgfile);
		String targetFileLevel2 = String.format(HearthFilesNameManager.scanTargetFileDefault, sb.imgfile);
		
		if( HearthHelper.fileExists(targetFileLevel1) ){
			file = new File(targetFileLevel1);
		} else {
			file = new File(targetFileLevel2);
		}
		
		BufferedImage preTarget;
		
		if(sb.unScaledTarget == null){
			preTarget = HearthHelper.loadImage(file);
			sb.unScaledTarget = preTarget;
		}else{
			preTarget = sb.unScaledTarget;
		}
		
		//if scaling required
		if(scaling != 1){
			preTarget = HearthHelper.resizeImage(preTarget, scaling);
		}
		
		if(sb.mask != null){
			HearthHelper.applyMaskImage(
				preTarget, 
				(int) Math.round(sb.mask.xOffset 	* scaling), 
				(int) Math.round(sb.mask.yOffset 	* scaling), 
				(int) Math.round(sb.mask.width 		* scaling), 
				(int) Math.round(sb.mask.height 	* scaling)
			);
		}
				
		sb.target = preTarget;
		
		if(sb.nestedSb != null){
			prepareScanbox(sb.nestedSb);
		}
	}
	
	//return run time in seconds
	@SuppressWarnings("unused")
	private int getRunTime(){
		return (int) Math.round( (System.currentTimeMillis() - scannerStarted / 1000) ); 
	}

	private void resetFPS(){
		totalTimeSpentCapturing = 0;
		totalFramesCounter = 0;
	}
	
	private float getAverageFPS(){
		float timeSpentSec = (totalTimeSpentCapturing/1000) > 1 ? (totalTimeSpentCapturing/1000) : 1;
		float average =  (float) totalFramesCounter/timeSpentSec;
		
		if(timeSpentSec > FPS_RESOLUTION){
			float diffTime = timeSpentSec - FPS_RESOLUTION;
			float diffFrames  = diffTime * average;
			
			totalTimeSpentCapturing -= diffTime * 1000;
			totalFramesCounter -= (int) diffFrames;
		}
		
		System.out.println("Average FPS: " + HearthHelper.formatNumber("0.00", average));
			
		return average;
	}
	
	private void updateFPSTime(long cycleStarted){
		totalTimeSpentCapturing += (System.currentTimeMillis() - cycleStarted);
	}
	
	private BufferedImage capture(){
		
		if(getAverageFPS() > FPS_LIMIT){
			return null;
		}
		
		BufferedImage snapshot = null;
		int gameScreenWidth		= getBoardWidth(),
			gameScreenHeight	= getBoardHeight(),
			
			//always use absolute screen offsets unless specified otherwise
			boardX 				= getAbsoluteBoardXoffset() + getXOffsetOverride(),
			boardY 				= getAbsoluteBoardYoffset() + getYOffetOverride(),
			
			//Heartstone handle
			handle = HearthHelper.getHearthstoneHandle();
			
				  //capture area
		Rectangle rec = null;

		//if Hearthstone handle is obtainable and we're on Windows
		if(handle!=0 && HearthHelper.getOSName().equals("win")){
			//since we're using a window handle, we do not need absolute screen offsets
			//getting relative offsets for client area
			boardX 				= getRelativeBoardXoffset();
			boardY 				= getRelativeBoardYoffset();
		}

		rec = new Rectangle(boardX, boardY, gameScreenWidth, gameScreenHeight);
		
		snapshot = HearthRobot.capture(handle, rec);

		//HearthHelper.bufferedImageToFile(snapshot, "./cache/capture-" + totalFramesCounter + ".png");

		totalFramesCounter++;
		
		return snapshot;
	}
		
	public void process(){
		boolean isMinimized = HearthHelper.isHearthstoneMinimized();
		boolean scanAllowed = false;
				
		if(alwaysScan){
			scanAllowed = true;
		} else if(HearthHelper.isHSDetected() && !isMinimized){
			//allow the scan to process if hearthstone is detected and not minimized
			scanAllowed = true;
		}

		if(isMinimized != exMinimized){
			exMinimized = isMinimized;
			
			if(isMinimized){
				addNotification(
					new HearthReaderNotification( uiLang.t("HS minimized"), uiLang.t("Stopped scanning!"))
				);
			} else {
				addNotification(
					new HearthReaderNotification( uiLang.t("HS restored"), uiLang.t("Scanning again!"))
				);
			}
		}
		
		if(!scanAllowed){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) { }
			return;
		}
		
		long started = System.currentTimeMillis();
		BufferedImage snap = capture();
		
		if(snap != null){
			scanner.insertFrame(snap);

			//we need to know the game mode all the time
			scanner.subscribe("gameMode");

			if(!isInGame() && isArenaMode()){
				scanner.subscribe("arenaHero");
				scanner.subscribe("arenaWins");
				scanner.subscribe("arenaLose");
			}

			if(!isInGame() && (isRankedMode() || isUnrankedMode() || isChallengeMode() || isPracticeMode())){
				scanner.subscribe("deckSelection");
			}

			//give the scanner 15 seconds 
			//delay before trying to scan for game again
			if(System.currentTimeMillis() - lastSaveAttempt > GAME_SCAN_INTERVAL){
				scanner.subscribe("coin");
				scanner.subscribe("myHero");
				scanner.subscribe("oppHero");
				scanner.subscribe("gameResult");
			}

			if(isInGame()){
				scanner.unsubscribe("arenaHero");
				scanner.unsubscribe("arenaWins");
				scanner.unsubscribe("arenaLose");
				scanner.unsubscribe("deckSelection");
			}
		}
		
		processResults();

		if(reInitScannerSettings){
			initScannerSettings();
		}
		
		long spent = System.currentTimeMillis() - started;
		
		if(spent < timeslot){
			try {
				Thread.sleep(timeslot - spent);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
		}
		
		updateFPSTime(started);
	}

	private void processResults(){
		List<SceneResult> results = scanner.getQueryResults();
		
		if(results ==  null){
			return;
		}
		
		for(SceneResult sr : results){
			System.out.println("SceneResult: " + sr.scene + ", result: " + sr.result);
			
			switch(sr.scene){
				case "gameMode":
					processGameMode(sr.result);
				break;
				
				case "deckSelection":
					processDeckSelection(sr.result);
				break;
				
				case "coin":
					processCoin(sr.result);
				break;

				case "gameResult":
					processGameResult(sr.result);
				break;
				
				case "arenaWins":
					processArenaWins(sr.result);
				break;
				
				case "arenaLose":
					processArenaLose(sr.result);
				break;

				case "arenaHero":
				case "myHero":
				case "oppHero":
					processHero(sr.scene, sr.result);
				break;
			}
		}
	}
	
	private void processDeckSelection(String result){
		System.out.println("processDeckSelection(), result: " + result);

		int selected = Integer.parseInt(result);
		
		if(selected != this.exSelectedDeck){
			isDirty = true;
			this.selectedDeck = selected;
			this.exSelectedDeck = selected;

			HearthDecks decks = HearthDecks.getInstance();
			String deckName = decks.list[selectedDeck];

			addNotification(
				new HearthReaderNotification( uiLang.t("Deck #%d", (selected+1)), deckName)
			);

			if(deckName.length() > 0){
				try {
					int deckWins = tracker.getWinsByDeck(gameMode, deckName);
					int deckLosses = tracker.getLossesByDeck(gameMode, deckName);
					float winRate = tracker.getWinRateByDeck(gameMode, deckName);
					String winRateS = winRate > -1 ? new DecimalFormat("0.00").format(winRate) + "%" : "N|A";
					
					if(winRate > -1 ){
						addNotification(new HearthReaderNotification(uiLang.t("Deck win rate"), 
								winRateS + " (" + deckWins + " - "  + deckLosses +  ")"
						));
					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void processArenaWins(String result){
		System.out.println("processArenaWins(), result: " + result);

		int wins = Integer.parseInt(result);
		boolean expired = false;
		inGameMode = 0;

		if(wins > ARENA_MAX_WINS){
			System.out.println("Something went wrong! Arena wins of " 
				+ wins + " detected. defined maximum is " + ARENA_MAX_WINS);
		}

		if(System.currentTimeMillis() - lastArenaWin > 500){
			lastArenaWin = System.currentTimeMillis();
			expired = true;
		}
		
		if( (expired && wins != exArenaWins) || (System.currentTimeMillis() - lastArenaScoreReport > 60000) ){
			isDirty = true;
			arenaWins = wins;
			exArenaWins = wins;
			String arenaHero = heroesList.getHeroLabel(myHero);

			if(arenaLosses > -1 && myHero > -1){
				addNotification(
					new HearthReaderNotification(
						uiLang.t("Arena score"), 
						uiLang.t("%d - %d as %s", arenaWins, arenaLosses, arenaHero)
					)
				);
				
				lastArenaScoreReport = System.currentTimeMillis();

				if(wins == ARENA_MAX_WINS || arenaLosses == ARENA_MAX_LOSSES){
					concludeArena();
				}
			}
		}
	}
	
	private void processArenaLose(String result){
		System.out.println("processArenaLose(), result: " + result);

		int losses = Integer.parseInt(result);
		inGameMode = 0;

		if(losses > ARENA_MAX_LOSSES){
			System.out.println("Something went wrong! Arena losses of " 
				+ losses + " detected. defined maximum is " + ARENA_MAX_LOSSES);
		}
		
		if(losses != exArenaLosses && losses > exArenaLosses){
			isDirty = true;
			arenaLosses = losses;
			exArenaLosses = losses;
			String arenaHero = heroesList.getHeroLabel(myHero);
			
			if( (arenaWins > -1 && myHero > -1) ){
				addNotification(
					new HearthReaderNotification(
						uiLang.t("Arena score"), 
						uiLang.t("%d - %d as %s", arenaWins, arenaLosses, arenaHero)
					)
				);
				
				lastArenaScoreReport = System.currentTimeMillis();

				if(arenaWins == ARENA_MAX_WINS || arenaLosses == ARENA_MAX_LOSSES){
					concludeArena();
				}
			}
		}
	}

	private void processGameResult(String result){
		boolean found = false;
		System.out.println("processGameResult(), result: " + result);

		switch(result){
			case "victory":
				victory = 1;
				found = true;
			break;

			case "defeat":
				victory = 0;
				found = true;
			break;
		}

		if(found && exVictory != victory){
			exVictory = victory;
			isDirty = true;

			if(!isGameTooShort()){
				concludeGame();
			}
			
			lastSaveAttempt = System.currentTimeMillis();
			scanner.unsubscribe("coin");
			scanner.unsubscribe("myHero");
			scanner.unsubscribe("oppHero");
			scanner.unsubscribe("gameResult");
		}
	}
	
	private void processCoin(String result){
		if(System.currentTimeMillis() - lastSaveAttempt < GAME_SCAN_INTERVAL ){
			return;
		}

		boolean found = false;
		System.out.println("processCoin(), result: " + result);
		inGameMode = 1;

		switch(result){
			case "first":
				goFirst = 1;
				found = true;
			break;
			
			case "second":
				goFirst = 0;
				found = true;
			break;
		}
		
		//check are we receive the "same" scene twice
		if(found && (exGoFirst != goFirst)){
			
			if(goFirst == 1){
				System.out.println("Found coin, go first");
				addNotification(
					new HearthReaderNotification( 
							uiLang.t("Coin detected"), 
							uiLang.t("You go first!")
					)
				);
			} else if( goFirst == 0){
				System.out.println("Found coin, go second");
				addNotification(
					new HearthReaderNotification( 
						uiLang.t("Coin detected"), 
						uiLang.t("You go second!")
					)
				);
			}
			
			isDirty = true;
			exGoFirst = goFirst;
			inGameMode = 1;
			gameStartedTime = System.currentTimeMillis();
		}
	}

	private void processHero(String scene, String result){
		//make sure we don't process at the wrong timing
		if(System.currentTimeMillis() - lastSaveAttempt < GAME_SCAN_INTERVAL && !scene.equals("arenaHero")){
			return;
		}

		boolean found = false;

		System.out.println("processHero(), scene: " + scene +", hero: " + result);

		int hero = heroesList.getHeroId(result);
		
		if(scene.equals("arenaHero") || scene.equals("myHero")){
			myHero = hero;
			
			if(myHero != exMyHero){
				exMyHero = myHero;
				found = true;
				System.out.println("Found my hero: " + result);
			}
		} else if(scene.equals("oppHero")){
			oppHero = hero;
			
			if(oppHero != exOppHero){
				exOppHero = oppHero;
				found = true;
				System.out.println("Found opponent hero: " + result);
			}
		}

		if(!scene.equals("arenaHero")){
			inGameMode = 1;
			gameStartedTime = System.currentTimeMillis();
		}

		if(found){
			isDirty = true;
			String title = "";
			
			switch(scene){
				case "arenaHero":
					title = "Arena Hero";
				break;
				
				case "myHero":
					title = "My Hero";
				break;
				
				case "oppHero":
					title = "Opponent Hero";
				break;
			}
			
			if(scene.equals("myHero")){
				addNotification(
					new HearthReaderNotification( 
						uiLang.t("Hero Detected"), 
						uiLang.t("Your hero is %s",  heroesList.getHeroLabel(hero))
					)
				);
			}

			if(scene.equals("oppHero")){
				addNotification(
					new HearthReaderNotification( 
						uiLang.t("Hero Detected"), 
						uiLang.t("Opponent hero is %s",  heroesList.getHeroLabel(hero))
					)
				);
			}
		}
	}

	private void processGameMode(String result){
		boolean found = false;

		System.out.println("processGameMode() result: " + result);
		
		switch(result.toLowerCase()){
			case "arena":
				if(isGameModeDiff(ARENAMODE)){
					gameMode = ARENAMODE;
					found = true;
				}
			break;

			case "ranked":
				if(isGameModeDiff(RANKEDMODE)){
					gameMode = RANKEDMODE;
					found = true;
				}
			break;

			case "unranked":
				if(isGameModeDiff(UNRANKEDMODE)){
					gameMode = UNRANKEDMODE;
					found = true;
				}
			break;

			case "practice":
				if(isGameModeDiff(PRACTICEMODE)){
					gameMode = PRACTICEMODE;
					found = true;
				}
			break;

			case "challenge":
				if(isGameModeDiff(CHALLENGEMODE)){
					gameMode = CHALLENGEMODE;
					found = true;
				}
			break;
		}

		if(found){
			String oldMode = HearthHelper.gameModeToString(exGameMode);
			String newMode = HearthHelper.gameModeToString(gameMode);

			System.out.println("Mode detected: " + newMode + ", previous mode: " + oldMode);
			
			addNotification(
				new HearthReaderNotification( 
					uiLang.t("Game Mode"),
					uiLang.t( newMode + " mode detected") 
				)
			);

			//we missed the victory/defeat screen
			if( isInGame() ){
				//try to save the game
				if(!isGameTooShort()){
					concludeGame();
				}
				
				lastSaveAttempt = System.currentTimeMillis();
				resetGameStatus();
			}

			isDirty = true;
			inGameMode = 0;
			exGameMode = gameMode;
		}
	}

	private boolean isGameModeDiff(int newValue){
		return (exGameMode != newValue);
	}
	
	private boolean isGameTooShort(){
		boolean isTooShort    = (System.currentTimeMillis() - gameStartedTime) < 60000;
		boolean isFalseRecord = (myHero == -1 || oppHero == -1);

		System.out.println("isTooShort: " + isTooShort);
		System.out.println("isFalseRecord: " + isFalseRecord);
		
		//take our best guess to rule out false record
		if( isTooShort && isFalseRecord){
			System.out.println("Possible false record. Reseting game status");
			resetGameStatus();
			return true;
		}
		
		return false;
	}
	
	private void concludeGame(){		
		int totalTime = (int) (System.currentTimeMillis() - gameStartedTime)/1000;
		HearthDecks decks = HearthDecks.getInstance();
		String deckName = "";
		
		if(decks == null){
			decks = new HearthDecks();
			//we don't need to save the decks.xml here because the main UI will handle it if it is missing.
		}
		
		if(victory == 1){
			System.out.println("Found Victory");
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Victory!", getMyHero(), getOppHero())
				)
			);
		} else if(victory == 0) {
			System.out.println("Found Defeat");
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Defeat!", getMyHero(), getOppHero())
				)
			);
		} else {
			System.out.println("Found Unknown game result");
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Unknown result!", getMyHero(), getOppHero())
				)
			);
		}
		
		System.out.println("Saving match result...");
		try {
			if(selectedDeck > -1 && selectedDeck < decks.list.length){
				deckName = decks.list[selectedDeck];
			}
			tracker.saveMatchResult(gameMode, myHero, oppHero, goFirst, victory, gameStartedTime, totalTime, false, deckName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done saving match result...");
		
		lastSaveAttempt = System.currentTimeMillis();
		resetGameStatus();
	}

	private void resetGameStatus(){
		inGameMode = 0;

		exVictory = -1;
		victory = -1;

		myHero = -1;

		oppHero = -1;
		exOppHero = -1;

		goFirst = -1;
		exGoFirst = -1;

		gameMode = -1;
		exGameMode = -1;
		
		gameStartedTime = System.currentTimeMillis();
	}

	private void concludeArena(){
		System.out.println("Saving arena result...");
		try {
			tracker.saveArenaResult(myHero, arenaWins, arenaLosses, System.currentTimeMillis(), false);
		} catch (SQLException e) { e.printStackTrace(); }
		System.out.println("Done saving arena result...");
		isDirty = true;
		this.resetArenaStatus();
	}

	private void resetArenaStatus(){
		this.arenaLosses = -1;
		this.myHero = -1;
		this.gameMode = UNKNOWNMODE;
	}
	
	private String sanitizeGameLang(String gLang){
		boolean foundLang = false;

		for(int i = 0; i < gameLanguages.langs.length; i++){
			if(gLang.toLowerCase().equals(gameLanguages.langs[i].code.toLowerCase())){
				foundLang = true;
				//make sure the cases are exactly the way it should (not an issue with filename case insensitive OS though).
				gLang = gameLanguages.langs[i].code;
				break;
			}
		}
		
		if(!foundLang){
			gLang = (gameLang == null || gLang.equals("")) ? gameLanguages.langs[0].code: gLang;
		}

		return gLang;
	}
	
	public void setGameLang(String lang){
		gameLang = lang;
		reInitScannerSettings = true;
	}
	
	public void setAutoGameRes(boolean flag){
		autoDetectGameRes = flag;
		reInitScannerSettings = true;
	}
	
	public void setGameRes(int w, int h){
		gameResX = w;
		gameResY = h;
		reInitScannerSettings = true;
	}

	public synchronized void addNotification(HearthReaderNotification note){
		if(notify == true){
			notifications.add(note);
		}
	}
	
	public void setInterval(int interval){
		timeslot = interval;
	}
	
	public void setAlwaysScan(boolean flag){
		alwaysScan = flag;
	}
	
	public void setAutoPing(boolean flag){
		
	}

	public void setNotification(boolean flag){
		notify = flag;
	}
	
	public void resume(){
		
	}
	
	public void pause(){
		totalTimeSpentCapturing = 0;
		totalFramesCounter = 0;
	}
	
	public void forcePing(){
		
	}
	
	public void dispose(){
		String path = String.format(HearthFilesNameManager.scannerSettingFileDefault, gameLang);
		config.save(scannerSettings, path);
		System.out.println("Shutting down the scanner!");
		scanner.dispose();
	}
	
	public int[] getLastScanArea(){
		return new int[4];
	}
	
	public int[] getLastScanSubArea(){
		return new int[4];
	}
	
	public long getLastseen(){
		return 0;
	}
	
	public boolean isDirty(){
		if(isDirty){
			isDirty = false;
			
			return true;
		}
		
		return false;
	}
	
public String getOverview(){
		String goes = uiLang.t("Unknown");
		int arenaWins = -1;
		int arenaLosses = -1;
		float arenaWinrate = -1;
		
		int rankedWins = -1;
		int rankedLosses = -1;
		float rankedWinrate = -1;
		
		int unrankedWins = -1;
		int unrankedLosses = -1;
		float unrankedWinrate = -1;
		String output = "";

		try {
			arenaWins = 	tracker.getTotalWins(HearthScannerManager.ARENAMODE);
			arenaLosses = 	tracker.getTotalLosses(HearthScannerManager.ARENAMODE);
			arenaWinrate =  (arenaWins + arenaLosses) > 0 ? (float) arenaWins /  (arenaWins + arenaLosses) * 100: -1;
			rankedWins = 	tracker.getTotalWins(HearthScannerManager.RANKEDMODE);
			rankedLosses = 	tracker.getTotalLosses(HearthScannerManager.RANKEDMODE);
			rankedWinrate =  (rankedWins + rankedLosses) > 0 ? (float) rankedWins / (rankedWins + rankedLosses) * 100 : -1;
			unrankedWins = 	tracker.getTotalWins(HearthScannerManager.UNRANKEDMODE);
			unrankedLosses = 	tracker.getTotalLosses(HearthScannerManager.UNRANKEDMODE);
			unrankedWinrate =  (unrankedWins + unrankedLosses) > 0 ? (float) unrankedWins / (unrankedWins + unrankedLosses) * 100 : -1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String strArena = arenaWinrate > -1 ?  arenaWins + " - " + arenaLosses + " (" + new DecimalFormat("0.00").format(arenaWinrate) + "%) " : "N|A";
		String strRanked = rankedWinrate > -1 ?  rankedWins + " - " + rankedLosses + " (" + new DecimalFormat("0.00").format(rankedWinrate) + "%) " : "N|A";
		String strUnranked = unrankedWinrate > -1 ? unrankedWins + " - " + unrankedLosses + " (" + new DecimalFormat("0.00").format(unrankedWinrate) + "%) " : "N|A";

		if(this.isGoFirst()){
			goes = uiLang.t("go first");
		}
		
		if(this.isGoSecond()){
			goes = uiLang.t("go second");
		}
		
		//output += "Last seen: " + seen + "\r\n";
		
		if(arenaWinrate > -1){
			output += uiLang.t("Arena: %s", strArena)  +"\r\n";
		}
		
		if(rankedWinrate > -1){
			output += uiLang.t("Ranked: %s", strRanked) +"\r\n";
		}
		
		if(unrankedWinrate > -1){
			output += uiLang.t("Unranked: %s", strUnranked) +"\r\n";
		}

		output += uiLang.t("Game mode: %s", this.getGameMode()) +"\r\n";
		
		if(this.isArenaMode()){
			String score = this.getArenaWins() > -1 && this.getArenaLosses() > -1 ? this.getArenaWins() + " - " + this.getArenaLosses() : "Unknown";
			
			output +="\r\n";
			output += uiLang.t("Live Arena status") + "\r\n";
			output += uiLang.t("Score: %s", score) + "\r\n";
			output += uiLang.t("Playing as %s", this.getMyHero()) + "\r\n";
		}
				
		if(this.isInGame()){
			output +="\r\n";
			output += uiLang.t("Live match status") + "\r\n";
			output += uiLang.t("%s vs %s, %s", this.getMyHero(), this.getOppHero(), goes)+ "\r\n";
		}
		
		try {
			ResultSet rs = tracker.getLastMatches(5);
			output += "\r\n" + uiLang.t("Latest match(es): ") + "\r\n";
			while(rs.next()){
				
				String as 	= heroesList.getHeroLabel(rs.getInt("MYHEROID"));
				String vs 	= heroesList.getHeroLabel(rs.getInt("OPPHEROID"));
				String first = rs.getInt("GOESFIRST") == 1 ? uiLang.t("go first") : uiLang.t("go second");
				String result = rs.getInt("WIN") == 1 ? uiLang.t("Win") : uiLang.t("Lose");
				
				if(rs.getInt("GOESFIRST") == -1){
					first =  "";
				}
				
				if(rs.getInt("WIN") == -1){
					result =  "";
				}
				
				output += uiLang.t("%s vs %s, %s, %s", as, vs, first, result)+ "\r\n";
			}
			
			rs = tracker.getLastArenaResults(5);

			output +="\r\n" + uiLang.t("Latest Arena: ") + "\r\n";
			
			while(rs.next()){
				String as 	= heroesList.getHeroLabel(rs.getInt("HEROID"));
				String result = rs.getInt("WINS") + "-" + rs.getInt("LOSSES");
				output +=as + " " + result + "\r\n";
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return output;
	}

	public boolean isArenaMode(){
		return gameMode == ARENAMODE ? true : false;
	}
	
	public boolean isRankedMode(){
		return gameMode == RANKEDMODE ? true : false;
	}
	
	public boolean isUnrankedMode(){
		return gameMode == UNRANKEDMODE ? true : false;
	}
	
	public boolean isChallengeMode(){
		return gameMode == CHALLENGEMODE ? true : false;
	}
	
	public boolean isPracticeMode(){
		return gameMode == PRACTICEMODE ? true : false;
	}

	public boolean isGoFirst(){
		return goFirst == 1 ? true : false;
	}
	
	public boolean isGoSecond(){
		return goFirst == 0 ? true : false;
	}

	public boolean isInGame() {
		return inGameMode == 1 ? true : false;
	}

	public int getArenaWins(){
		return exArenaWins;
	}
	
	public int getArenaLosses(){
		return exArenaLosses;
	}

	public String getGameMode(){
		if(this.isArenaMode()){
			return uiLang.t("Arena");
		}
		
		if(this.isRankedMode()){
			return uiLang.t("Ranked");
		}
		
		if(this.isUnrankedMode()){
			return uiLang.t("Unranked");
		}
		
		if(this.isPracticeMode()){
			return uiLang.t("Practice");
		}
		
		if(this.isChallengeMode()){
			return uiLang.t("Challenge");
		}
		
		return uiLang.t("Unknown");
	}

	public String getMyHero(){
		if(myHero >= 0 ){
			return heroesList.getHeroLabel(myHero);
		}
		
		return "Unknown";
	}

	public String getOppHero(){
		if(oppHero >= 0 ){
			return heroesList.getHeroLabel(oppHero);
		}
		
		return "Unknown";
	}
	
	public HearthReaderNotification getNotification(){
		if(notifications.size() == 0){
			return null;
		}
		
		synchronized(notifications){
			HearthReaderNotification first = notifications.get(0);
			notifications.remove(0);
			
			return first;
		}
	}
}

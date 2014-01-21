package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sikuli.api.ImageTarget;

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
	private static final long FPS_LIMIT = 15;
	private static final float FPS_RESOLUTION = 60; //keep approaximate 60 seconds of frames
	
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

	//private Robot robot = null;

	private HearthScanner scanner = new HearthScanner();
	
	private int totalFramesCounter = 0;
	private long totalTimeSpentCapturing = 0;
	private List<HearthReaderNotification> notifications =  Collections.synchronizedList(new ArrayList<HearthReaderNotification>());
	
	//Game status related variables
	private long lastArenaWinsReported = 0;
	private int exArenaWins = -1;
	private int arenaWins = -1;
	
	private int previousArenaLosses = -1;
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
	
	private int timeslot = 100;
	private long gameStartedTime = System.currentTimeMillis();

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
		
	private int getBoardX(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int xOffset = winPos[0];
		int[] gameRes = getGameResolution();
		int absoluteX = xOffset + (gameRes[0] - getBoardWidth()) / 2;
		return absoluteX;
	}
	
	private int getBoardY(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int yOffset = winPos[1];		
		return yOffset;
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
			
			if(HearthHelper.isDevelopmentEnvironment()){
				String original 
				= String.format(
						HearthFilesNameManager.scannerSettingFileDefault, 
						"original"
				);
				
				//save an extra copy of the original values
				//so that we can compare it with the "self-corrected" version later on
				config.save(scannerSettings, original);
			}
		}
		
		synchronized(scannerSettings.list){
			for(Scanbox sb : scannerSettings.list){
				prepareScanbox(sb);
				scanner.addScanbox(sb);
			}
		}
		
		scanner.initScale(getScaleFactor());
		scanner.resume();
		scannerSettingsInitialzed = true;
		reInitScannerSettings = false;
	}
		
	private synchronized void prepareScanbox(HearthScannerSettings.Scanbox sb){
		ImageTarget it = null;
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
		
		if(scaling != 1){
			preTarget = HearthHelper.resizeImage(file, scaling);	
		} else {
			preTarget = HearthHelper.loadImage(file);
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
		
		it = new ImageTarget(preTarget);
		
		if(sb.matchQuality >= 0 && it != null){
			it.setMinScore(sb.matchQuality);
			//it.similar(sb.matchQuality);
		}
		
		sb.target = it;
		
		if(sb.nestedSb != null){
			prepareScanbox(sb.nestedSb);
		}
	}
	
	//return run time in seconds
	@SuppressWarnings("unused")
	private int getRunTime(){
		return (int) Math.round( (System.currentTimeMillis() - scannerStarted / 1000) ); 
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
			boardX 				= getBoardX() + getXOffsetOverride(),
			boardY 				= getBoardY() + getYOffetOverride();
		
		Rectangle rec = new Rectangle(boardX, boardY, gameScreenWidth, gameScreenHeight);
				
		snapshot = HearthRobot.capture(rec);
		totalFramesCounter++;
		
		return snapshot;
	}
		
	public void process(){
		boolean scanAllowed = alwaysScan || (!alwaysScan && HearthHelper.isHSDetected());
		
		if(!scanAllowed){
			return;
		}
		
		long started = System.currentTimeMillis();
		BufferedImage snap = capture();
		
		if(snap != null){
			scanner.insertFrame(snap);
			scanner.subscribe("gameMode");
			scanner.subscribe("gameResult");
			scanner.subscribe("arenaHero");
			scanner.subscribe("arenaWins");
			scanner.subscribe("coin");
			scanner.subscribe("myHero");
			scanner.subscribe("oppHero");
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
				
				case "coin":
					processCoin(sr.result);
				break;

				case "gameResult":
					processGameResult(sr.result);
				break;
				
				case "arenaWins":
					processArenaWins(sr.result);
				break;

				case "arenaHero":
				case "myHero":
				case "oppHero":
					processHero(sr.scene, sr.result);
				break;
			}
		}
	}

	private void processArenaWins(String result){
		System.out.println("processArenaWins(), result: " + result);

		int wins = Integer.parseInt(result);

		if(wins > ARENA_MAX_WINS){
			System.out.println("Something went wrong! Arena wins of " 
				+ wins + " detected. defined maximum is " + ARENA_MAX_WINS);
		}
		
		if(wins != exArenaWins){
			arenaWins = wins;
			exArenaWins = wins;
			String arenaHero = heroesList.getHeroLabel(myHero);
			
			addNotification(
				new HearthReaderNotification(
					uiLang.t("Arena score"), 
					uiLang.t("%d - %d as %s", arenaWins, arenaLosses, arenaHero)
				)
			);
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

			if(victory == 1){
				System.out.println("Found Victory");
				addNotification(
					new HearthReaderNotification(
							uiLang.t("Game Result"), 
							"Victory!"
					)
				);
			} else if(victory == 0) {
				System.out.println("Found Defeat");
				addNotification(
					new HearthReaderNotification(
							uiLang.t("Game Result"), 
							"Defeat!"
					)
				);
			}
		}
	}
	
	private void processCoin(String result){
		boolean found = false;
		System.out.println("processCoin(), result: " + result);
		
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
			
			addNotification(
				new HearthReaderNotification(title, result)
			);
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

			exGameMode = gameMode; 
			isDirty = true;
		}
	}

	private boolean isGameModeDiff(int newValue){
		return (exGameMode != newValue);
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
		return "";
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

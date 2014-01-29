package my.hearthtracking.app;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthScannerManager {
	private HearthLogger logger = HearthLogger.getInstance();
	
	public static final int ARENA_MAX_WINS = 12;
	public static final int ARENA_MAX_LOSSES = 3;
	
	public static final float BASE_RESOLUTION_HEIGHT = 1080f;
	private static final long FPS_LIMIT = 24;
	private static final float FPS_RESOLUTION = 60; //keep approximately 60 seconds of frames

	private static final long DELAY_ARENA_SCORE = 3000;
	private static final long DELAY_GAME_RESULT = 1000;
	private static final long DELAY_GAME_HEROES = 1000;

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

	private HearthDB db;
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
	private int arenaWins = -1;
	private int arenaLosses = -1;
	
	private int arenaHero	= -1;
	private int myHero 		= -1;
	private int oppHero		= -1;
	
	private int selectedDeck = -1;

	private int gameResult = -1;
	private int goFirst = -1;

	private int gameMode = HearthGameMode.UNKNOWNMODE;

	private boolean gameJustEnded = false;
	private int inGameMode = -1;
	
	private int timeslot = 250;
	private long gameStartedTime = System.currentTimeMillis();
	
	private List<HearthScanResult> scanResults = Collections.synchronizedList(new ArrayList<HearthScanResult>());
	
	public HearthScannerManager (HearthDB t, int tslot, String lang, int resX, int resY, boolean autoping, boolean alwaysScanFlag){
		debugMode = HearthHelper.isDevelopmentEnvironment();
		db  = t;
		gameResX = resX;
		gameResY = resY;
		gameLang = lang.toLowerCase();
		timeslot = tslot;
		
		if(debugMode){
			timeslot = 100;
		}
		
		alwaysScan = alwaysScanFlag;

		logger.info("HearthScannerManager() started");
		logger.finer(
			"debugMode: " + debugMode + ", " + 
			"gameResX: " + gameResX + ", " + 
			"gameResY: " + gameResY + ", " + 
			"gameLang: " + lang.toLowerCase() + ", " + 
			"timeslot: " + tslot  + ", " + 
			"alwaysScan" + alwaysScan
		);
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
			logger.finest("Failed to detect HS resolution: " + resolution[0] + ", " + resolution[1]);
			resolution[0] = gameResX;
			resolution[1] = gameResY;
			logger.finest("Resort to " + resolution[0] + ", " + resolution[1]);
		}

		//workaround
		if(resolution[0] < 1024 && resolution[1] < 768){
			logger.finest("Weird resolution change detected.");
			logger.finest("Old resolution: " + oldGameResX + "x" + oldGameResY);
			logger.finest("New resolution: " + resolution[0] + "x" + resolution[1]);

			resolution[0] = oldGameResX;
			resolution[1] = oldGameResY;

			logger.finest("Resort to " + resolution[0] + ", " + resolution[1]);

			return resolution;
		}
		
		//if resolution is different from previous scan
		if(resolution[0] != oldGameResX || resolution[1] != oldGameResY){
			if(scannerSettingsInitialzed){
				logger.info("Resolution change detected.");
				logger.info("Old resolution: " + oldGameResX + "x" + oldGameResY);
				logger.info("New resolution: " + resolution[0] + "x" + resolution[1]);
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
		if(gameScreenHeight == 0 || gameScreenHeight == BASE_RESOLUTION_HEIGHT){
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
			logger.finest("Loading scanbox image file: " + file);
			preTarget = HearthHelper.loadImage(file);
			sb.unScaledTarget = preTarget;
		} else {
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
		return (int) Math.round( (System.currentTimeMillis() - scannerStarted / 1000f) ); 
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
		
		logger.finest("Average FPS: " + HearthHelper.formatNumber("0.00", average));
			
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
				scanner.unsubscribe("deckSelection");
			}

			if(!isInGame() && (isRankedMode() || isUnrankedMode() || isChallengeMode() || isPracticeMode())){
				scanner.subscribe("deckSelection");
			}

			if(!gameJustEnded){
				scanner.subscribe("coin");
				scanner.subscribe("bottomHero");
				scanner.subscribe("topHero");
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
		List<HearthScanResult> results = scanner.getQueryResults();
		
		if(results ==  null){
			return;
		}
		
		for(HearthScanResult sr : results){
			logger.finest("SceneResult: " + sr.scene + ", result: " + sr.result);

			switch(sr.scene){
				case "gameMode":
					processGameMode(sr);
				break;
				
				case "deckSelection":
					processDeckSelection(sr);
				break;
				
				case "coin":
					processCoin(sr);
				break;

				case "gameResult":
					processGameResult(sr);
				break;
				
				case "arenaWins":
					processArenaWins(sr);
				break;
				
				case "arenaLose":
					processArenaLose(sr);
				break;

				case "arenaHero":
				case "bottomHero":
				case "topHero":
					processHero(sr);
				break;
			}
		}
		
		processArenaScore();
		processHeroBoxes();
	}
	
	private void processDeckSelection(HearthScanResult sr){
		logger.finest("processDeckSelection(), result: " + sr.result);

		int selected = Integer.parseInt(sr.result);
		
		if(selected != this.selectedDeck){
			isDirty = true;
			this.selectedDeck = selected;

			HearthDecks decks = HearthDecks.getInstance();
			String deckName = decks.list[selectedDeck];

			addNotification(
				new HearthReaderNotification( uiLang.t("Deck #%d", (selected+1)), deckName)
			);

			if(deckName.length() > 0){
				try {
					int deckWins = db.getWinsByDeck(gameMode, deckName);
					int deckLosses = db.getLossesByDeck(gameMode, deckName);
					float winRate = db.getWinRateByDeck(gameMode, deckName);
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
	
	private void processArenaWins(HearthScanResult sr){
		logger.finest("processArenaWins(), result: " + sr.result);

		int wins = Integer.parseInt(sr.result);
	
		if(wins > ARENA_MAX_WINS){
			logger.severe("Something went wrong! Arena wins of " 
				+ wins + " detected. defined maximum is " + ARENA_MAX_WINS);
		}
		
		sr.setExpiry(DELAY_ARENA_SCORE);
		
		synchronized(scanResults){
			scanResults.add(sr);
		}
	}
	
	private void processArenaLose(HearthScanResult sr){
		logger.finest("processArenaLose(), result: " + sr.result);

		int losses = Integer.parseInt(sr.result);
		inGameMode = 0;

		if(losses > ARENA_MAX_LOSSES){
			logger.severe("Something went wrong! Arena losses of " 
				+ losses + " detected. defined maximum is " + ARENA_MAX_LOSSES);
		}
		
		sr.setExpiry(DELAY_ARENA_SCORE);
		
		synchronized(scanResults){
			scanResults.add(sr);
		}
	}
	
	private void processGameResult(HearthScanResult sr){
		logger.finest("processGameResult(), result: " + sr.result);

		synchronized(scanResults){
			sr.setExpiry(DELAY_GAME_RESULT);
			scanResults.add(sr);
		}
	}
	
	private void processHeroBoxes(){
		long currentTime 		= System.currentTimeMillis();
		
		boolean topHeroDetected = false;
		boolean bottomHeroDetected = false;
		
		int topHero = -1;
		int bottomHero = -1;
		
		boolean gameResultDetected = false;
		boolean topDefeated 	= false;
		boolean bottomDefeated 	= false;
		
		int detectedGameResult = HearthMatch.GAME_RESULT_UNKNOWN;
		
		synchronized(scanResults){
			Iterator <HearthScanResult>it = scanResults.iterator();
			
			while(it.hasNext()){
				HearthScanResult sr = it.next();
				
				if(sr.scene.equals("gameResult") && sr.isExpired(currentTime)){
					if(sr.result.equals("topDefeated")){
						topDefeated = true;
						gameResultDetected = true;
					} else if(sr.result.equals("bottomDefeated")){
						bottomDefeated = true;
						gameResultDetected = true;
					}
					
					//remove the expired
					it.remove();
				}
				else
				if(sr.scene.equals("bottomHero") && sr.isExpired(currentTime)){
					bottomHero = heroesList.getHeroId(sr.result);
					bottomHeroDetected = true;
					
					//remove the expired
					it.remove();
				}
				else
				if(sr.scene.equals("topHero") && sr.isExpired(currentTime)){
					topHero = heroesList.getHeroId(sr.result);
					topHeroDetected = true;
					
					//remove the expired
					it.remove();
				}
			}
		}
	
		//if match heroes are detected
		if(!gameJustEnded && !gameResultDetected && (topHeroDetected || bottomHeroDetected) ){
			
			if( (bottomHero!= -1 && bottomHero != myHero) && (topHero!= -1 && topHero != oppHero) ){
				addNotification(
					new HearthReaderNotification( 
						uiLang.t("Match detected"), 
						uiLang.t("%s vs %s",  heroesList.getHeroLabel(topHero), heroesList.getHeroLabel(bottomHero) )
					)
				);
			}

			if(bottomHeroDetected && myHero!=bottomHero){
				isDirty = true;			
				myHero = bottomHero;
				//update the game started time
				gameStartedTime = System.currentTimeMillis();
				
				logger.info("Hero (bottom) detected: " + heroesList.getHeroName(bottomHero));
			}
			
			if(topHeroDetected && oppHero != topHero){
				isDirty = true;
				oppHero = topHero;
				//update the game started time
				gameStartedTime = System.currentTimeMillis();
				
				logger.info("Hero (top) detected: " + heroesList.getHeroName(topHero));
			}
			
			inGameMode = 1;
		}
		
		//if game result is detected
		else if(!gameJustEnded && gameResultDetected){
			
			if(topDefeated && bottomDefeated){
				detectedGameResult = HearthMatch.GAME_RESULT_DRAW;
			} else if(topDefeated){
				detectedGameResult = HearthMatch.GAME_RESULT_VICTORY;
			} else if(bottomDefeated){
				detectedGameResult = HearthMatch.GAME_RESULT_DEFEAT;
			}

			if(detectedGameResult != gameResult){
				logger.info("Game result: " + HearthMatch.gameResultToString(detectedGameResult));
				
				gameResult = detectedGameResult;
				isDirty = true;
				concludeGame();
				scanner.unsubscribe("coin");
				scanner.unsubscribe("bottomHero");
				scanner.unsubscribe("topHero");
				scanner.unsubscribe("gameResult");
			}
		}
	}
	
	private void processArenaScore(){
		long currentTime = System.currentTimeMillis();

		//try to make sure both results are confirmed before we continues
		int confirmedWins = processDelayedArenaScore("arenaWins", currentTime);
		int confirmedLosses = processDelayedArenaScore("arenaLose", currentTime);
		
		boolean winsUpdated = (confirmedWins > -1 && confirmedWins != arenaWins);
		boolean lossesUpdated = (confirmedLosses > -1 && confirmedLosses != arenaLosses);
		boolean annoucementExpired = System.currentTimeMillis() - lastArenaScoreReport > 60000;

		//if the latest wins is different or last announcement time expired
		if( winsUpdated || lossesUpdated || (annoucementExpired && arenaWins != -1 && arenaLosses != -1)  ){
						
			if(confirmedWins > -1 && confirmedLosses > -1){
				String hero = heroesList.getHeroLabel(arenaHero);
				
				addNotification(
					new HearthReaderNotification(
						uiLang.t("Arena score"), 
						uiLang.t("%d - %d as %s", confirmedWins, confirmedLosses, hero)
					)
				);
			}

			lastArenaScoreReport = System.currentTimeMillis();
			
			if(winsUpdated || lossesUpdated){
				isDirty = true;
				
				arenaWins = confirmedWins;
				arenaLosses = confirmedLosses;
				
				//if we reached the end of an arena session
				if(arenaWins == ARENA_MAX_WINS || arenaLosses == ARENA_MAX_LOSSES){
					concludeArena();
				}
			}
		}
	}
	
	//the idea is to look into N seconds ago
	//and compare it with latest result
	//so that we can get a more accurate picture of
	//what is going on
	private int processDelayedArenaScore(String currentScene, long currentTime){
		int latestResult = -1;
		int lastestExpiredResult = -1;
		int confirmedResult = -1;
		
		synchronized(scanResults){
			Iterator <HearthScanResult>it = scanResults.iterator();
			
			while(it.hasNext()){
				HearthScanResult sr = it.next();
				
				if(sr.scene.equals(currentScene) && sr.isExpired(currentTime)){
					
					//make sure we get the largest value within the time frame
					if(Integer.parseInt(sr.result) >= lastestExpiredResult){
						//make sure this is the most recent expired result
						lastestExpiredResult = Integer.parseInt(sr.result);
					}
					
					//remove the expired
					it.remove();
				}
			}
			
			if(lastestExpiredResult > -1){
				logger.finest( "[" + currentScene + "]" + "Expired result found: " + lastestExpiredResult);
				
				//get a new iterator;
				it = scanResults.iterator();
				
				while(it.hasNext()){
					HearthScanResult sr = it.next();	
					
					if(sr.scene.equals(currentScene) && !sr.isExpired(currentTime)){

						//make sure we get the largest value from within the time frame
						if(Integer.parseInt(sr.result) >= latestResult){
							latestResult = Integer.parseInt(sr.result);
						}
					}
				}
				
				if(latestResult > -1){
					logger.finest("[" + currentScene + "]" + "Latest result found: " + latestResult);
				}
			}
		}
		
		//the most recent expired result take precedent 
		//if we cant find any non-expiry result
		if(lastestExpiredResult > -1 && latestResult == -1){
			confirmedResult = lastestExpiredResult;
			logger.finest("[" + currentScene + "]" +"(lastestExpiredResult > -1 && latestResult == -1)");
		}
		
		//if both latest and expired result are found
		if(latestResult > -1 && lastestExpiredResult > -1){
			logger.finest("[" + currentScene + "]" + "(latestResult > -1 && lastestExpiredResult > -1)");
			
			//if latest is greater than the expired
			//since we're comparing them in relatively short time frame
			//we can safely assume it is the same session
			if(latestResult >= lastestExpiredResult){
				
				//we will use the latest result
				confirmedResult = latestResult;
				
				logger.finest("[" + currentScene + "]" + "(latestResult >= lastestExpiredResult)");
			}
		}
		
		if(confirmedResult > - 1){
			logger.finest("[" + currentScene + "]" + "confirmedResult: " + confirmedResult);
		}

		
		return confirmedResult;
	}
	
	private void processCoin(HearthScanResult sr){
		boolean found = false;
		logger.finest("processCoin(), result: " + sr.result);
		inGameMode = 1;
		int coin = -1;

		switch(sr.result){
			case "first":
				coin = 1;
				found = true;
			break;
			
			case "second":
				coin = 0;
				found = true;
			break;
		}
		
		//check are we receive the "same" scene twice
		if(found && (coin != goFirst)){
			
			if(coin == 1){
				logger.info("Found coin, go first");
				addNotification(
					new HearthReaderNotification( 
							uiLang.t("Coin detected"), 
							uiLang.t("You go first!")
					)
				);
			} else if( coin == 0){
				logger.info("Found coin, go second");
				addNotification(
					new HearthReaderNotification( 
						uiLang.t("Coin detected"), 
						uiLang.t("You go second!")
					)
				);
			}
			
			isDirty = true;
			goFirst = coin;
			inGameMode = 1;
			gameStartedTime = System.currentTimeMillis();
		}
	}

	private void processHero(HearthScanResult sr){
		logger.finest("processHero(), scene: " + sr.scene +", hero: " + sr.result);

		int detectedHero = heroesList.getHeroId(sr.result);
		
		if(sr.scene.equals("arenaHero") && myHero != detectedHero){
			myHero = detectedHero;
			arenaHero = detectedHero;
			isDirty = true;
			logger.info("Found arena hero: " + sr.result);
		} else if(sr.scene.equals("bottomHero") || sr.scene.equals("topHero")){
			synchronized(scanResults){
				sr.setExpiry(DELAY_GAME_HEROES);
				scanResults.add(sr);
			}
		}
	}

	private void processGameMode(HearthScanResult sr){
		boolean found = false;
		int detectedMode = -1;

		logger.finest("processGameMode() result: " + sr.result);
		
		switch(sr.result.toLowerCase()){
			case "arena":
				if(isGameModeDiff(HearthGameMode.ARENAMODE)){
					detectedMode = HearthGameMode.ARENAMODE;
					found = true;
				}
			break;

			case "ranked":
				if(isGameModeDiff(HearthGameMode.RANKEDMODE)){
					detectedMode = HearthGameMode.RANKEDMODE;
					found = true;
				}
			break;

			case "unranked":
				if(isGameModeDiff(HearthGameMode.UNRANKEDMODE)){
					detectedMode = HearthGameMode.UNRANKEDMODE;
					found = true;
				}
			break;

			case "practice":
				if(isGameModeDiff(HearthGameMode.PRACTICEMODE)){
					detectedMode = HearthGameMode.PRACTICEMODE;
					found = true;
				}
			break;

			case "challenge":
				if(isGameModeDiff(HearthGameMode.CHALLENGEMODE)){
					detectedMode = HearthGameMode.CHALLENGEMODE;
					found = true;
				}
			break;
		}

		if(found){
			String oldMode = HearthGameMode.gameModeToString(gameMode);
			String newMode = HearthGameMode.gameModeToString(detectedMode);
			
			if(gameMode == HearthGameMode.ARENAMODE && detectedMode != HearthGameMode.ARENAMODE){
				flushScanResults("arenaWins");
				flushScanResults("arenaLose");
			}
			
			isDirty = true;
			inGameMode = 0;
			gameMode = detectedMode;
			gameJustEnded = false;

			logger.info("Mode detected: " + newMode + ", previous mode: " + oldMode);
			
			addNotification(
				new HearthReaderNotification( 
					uiLang.t("Game Mode"),
					uiLang.t( newMode + " mode detected") 
				)
			);

			//we missed the victory/defeat screen
			if( isInGame() && !gameJustEnded ){
				concludeGame();
			}
			
			scanner.subscribe("coin");
			scanner.subscribe("bottomHero");
			scanner.subscribe("topHero");
			scanner.subscribe("gameResult");
		}
	}

	private boolean isGameModeDiff(int newValue){
		return (gameMode != newValue);
	}
	
	private void concludeGame(){
		
		// System.out.println("System.currentTimeMillis() - gameStartedTime = " + (System.currentTimeMillis() - gameStartedTime) );
		// System.out.println("...." + ((int) Math.round((System.currentTimeMillis() - gameStartedTime)/1000f)));
		
		int totalTime = (int) Math.round((System.currentTimeMillis() - gameStartedTime)/1000f);
		HearthDecks decks = HearthDecks.getInstance();
		String deckName = "";
		
		if(decks == null){
			decks = new HearthDecks();
			//we don't need to save the decks.xml here because the main UI will handle it if it is missing.
		}
		
		if(gameResult == 1){
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Victory!", getMyHero(), getOppHero())
				)
			);
		} else if(gameResult == 0) {
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Defeat!", getMyHero(), getOppHero())
				)
			);
		} else {
			addNotification(
				new HearthReaderNotification(
						uiLang.t("Game Result"), 
						uiLang.t( "%s vs %s, Unknown result!", getMyHero(), getOppHero())
				)
			);
		}
		
		logger.info("Saving match result...");
		try {
			if(selectedDeck > -1 && selectedDeck < decks.list.length){
				deckName = decks.list[selectedDeck];
			}
			db.saveMatchResult(gameMode, myHero, oppHero, goFirst, gameResult, gameStartedTime, totalTime, false, deckName);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		logger.info("Done saving match result...");
		
		gameJustEnded = true;
		resetGameStatus();
	}

	private void resetGameStatus(){
		myHero 			= -1;
		oppHero 		= -1;
		selectedDeck 	= -1;
		gameResult 		= -1;
		goFirst 		= -1;
		inGameMode 		= -1;
		gameMode		= HearthGameMode.UNKNOWNMODE;
		gameStartedTime = System.currentTimeMillis();
		
		flushScanResults("topHero");
		flushScanResults("bottomHero");
		flushScanResults("gameResult");
		scanner.resetFrames();
	}

	private void concludeArena(){
		logger.info("Saving arena result...");
		try {
			db.saveArenaResult(arenaHero, arenaWins, arenaLosses, System.currentTimeMillis(), false);
		} catch (SQLException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		logger.info("Done saving arena result...");
		isDirty = true;
		this.resetArenaStatus();
	}

	private void resetArenaStatus(){
		this.arenaWins		= -1;
		this.arenaLosses 	= -1;
		this.arenaHero 		= -1;
		this.gameMode 		= HearthGameMode.UNKNOWNMODE;
	}
	
	private void flushScanResults(String scene){
		Iterator<HearthScanResult> it = scanResults.iterator();
		
		logger.finest("Start flushScanResults(): " + scene);
		
		while(it.hasNext()){
			HearthScanResult sr = it.next();
			
			if(sr.scene.equals(scene)){
				it.remove();
			}
		}
		
		logger.finest("Done flushScanResults(): " + scene);
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
		
		logger.info("Setting gameLang to " + lang);
	}
	
	public void setAutoGameRes(boolean flag){
		autoDetectGameRes = flag;
		reInitScannerSettings = true;
		
		logger.info("Setting autoDetectGameRes to " + autoDetectGameRes);
	}
	
	public void setGameRes(int w, int h){
		gameResX = w;
		gameResY = h;
		reInitScannerSettings = true;
		
		logger.info("Setting game resolution to " + w + ", " + h);
	}

	public synchronized void addNotification(HearthReaderNotification note){
		if(notify == true){
			notifications.add(note);
		}
	}
	
	public void setInterval(int interval){
		timeslot = interval;
		logger.info("Setting timeslot to " + timeslot);
	}
	
	public void setAlwaysScan(boolean flag){
		alwaysScan = flag;
		logger.info("Setting alwaysScan to " + flag);
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
		logger.info("pause: HearthScannerManager()" );
	}
	
	public void forcePing(){
		
	}
	
	public void dispose(){
		String path = String.format(HearthFilesNameManager.scannerSettingFileDefault, gameLang);
		config.save(scannerSettings, path);
		logger.info("Shutting down the scanner!");
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
			arenaWins = 	db.getTotalWins(HearthGameMode.ARENAMODE);
			arenaLosses = 	db.getTotalLosses(HearthGameMode.ARENAMODE);
			arenaWinrate =  (arenaWins + arenaLosses) > 0 ? (float) arenaWins /  (arenaWins + arenaLosses) * 100: -1;
			rankedWins = 	db.getTotalWins(HearthGameMode.RANKEDMODE);
			rankedLosses = 	db.getTotalLosses(HearthGameMode.RANKEDMODE);
			rankedWinrate =  (rankedWins + rankedLosses) > 0 ? (float) rankedWins / (rankedWins + rankedLosses) * 100 : -1;
			unrankedWins = 	db.getTotalWins(HearthGameMode.UNRANKEDMODE);
			unrankedLosses = 	db.getTotalLosses(HearthGameMode.UNRANKEDMODE);
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
			ResultSet rs = db.getLastMatches(5);
			output += "\r\n" + uiLang.t("Latest match(es): ") + "\r\n";
			while(rs.next()){
				
				String as 	= heroesList.getHeroLabel(rs.getInt("myHeroID"));
				String vs 	= heroesList.getHeroLabel(rs.getInt("oppHeroID"));
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
			
			rs = db.getLastArenaResults(5);

			output +="\r\n" + uiLang.t("Latest Arena: ") + "\r\n";
			
			while(rs.next()){
				String as 	= heroesList.getHeroLabel(rs.getInt("HEROID"));
				String result = rs.getInt("WINS") + "-" + rs.getInt("LOSSES");
				output +=as + " " + result + "\r\n";
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
			logger.severe(e.getMessage());
		}
		
		return output;
	}

	public boolean isArenaMode(){
		return gameMode == HearthGameMode.ARENAMODE ? true : false;
	}
	
	public boolean isRankedMode(){
		return gameMode == HearthGameMode.RANKEDMODE ? true : false;
	}
	
	public boolean isUnrankedMode(){
		return gameMode == HearthGameMode.UNRANKEDMODE ? true : false;
	}
	
	public boolean isChallengeMode(){
		return gameMode == HearthGameMode.CHALLENGEMODE ? true : false;
	}
	
	public boolean isPracticeMode(){
		return gameMode == HearthGameMode.PRACTICEMODE ? true : false;
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
		return arenaWins;
	}
	
	public int getArenaLosses(){
		return arenaLosses;
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

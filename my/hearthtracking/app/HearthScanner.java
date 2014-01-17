package my.hearthtracking.app;

import java.awt.Color;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthScanner {
	public static final int UNKNOWNMODE = -1;
	public static final int MENUMODE = 0;
	public static final int ARENAMODE = 1;
	public static final int RANKEDMODE = 2;
	public static final int UNRANKEDMODE = 3;
	public static final int CHALLENGEMODE = 4;
	public static final int PRACTICEMODE = 5;
	
	private boolean debugMode = false;
	private boolean inited = false;
	private int xOffetOverrideVal = 0;
	private int yOffsetOverrideVal = 0;
	
	private int previousWins = -1;
	private int wins = -1;
	
	private int previousLosses = -1;
	private int losses = -1;
	
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
	private long startTime = System.currentTimeMillis();
	private long lastSave  = System.currentTimeMillis() - 30000;

	private int gameMode = UNKNOWNMODE;
	private int exGameMode = UNKNOWNMODE;
	private int inGameMode = -1;
	
	private boolean paused = false;

	private HearthTracker tracker = null;

	private HearthGameLangList gameLanguages;
	private String gameLang;
		
	private HearthScannerSettings scannerSettings = null;
	
	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;
	
	private int gameResX = 1920, gameResY = 1080;
	private int oldGameResX = -1, oldGameResY = -1;
	private boolean gameScannerInitialized = false;
	
	private boolean forcePing = false;
	private boolean pingHearthstone = true;
	private boolean seenHearthstone = false;
	private boolean autoDetectGameRes = true;
	
	private long pingInterval = 60 * 1000;
	private long lastSeen =  0;
	private boolean isDirty = true;
	private int[] lastScanArea = {0,0,0,0};
	private int[] lastScanSubArea = {0,0,0,0};
	private long lastPing = System.currentTimeMillis() - pingInterval;
	
	private boolean reinitScanner = false;
	
	private boolean alwaysScan = false;
	
	private boolean notify = true;
	
	private List<HearthReaderNotification> notifications = new ArrayList<HearthReaderNotification>();
	
	private static HearthLanguageManager uiLang = HearthLanguageManager.getInstance();
	
	Hashtable<String, ScreenRegion> regionsMap = new Hashtable<String, ScreenRegion>();
	
	Hashtable<String, String> imageCacheMap = new Hashtable<String, String>();
	
	private boolean generateBeterOffsets = true; 
	
	private boolean enableMagicHash = false;
	
	private ImagePHash pHash = new ImagePHash();
	
	public HearthScanner (HearthTracker t, String lang, int resX, int resY, boolean autoping, boolean alwaysScanFlag){
		//debugMode = HearthHelper.isDevelopmentEnvironment();
		tracker = t;
		gameResX = resX;
		gameResY = resY;
		gameLang = lang.toLowerCase();
		setAlwaysScan(alwaysScanFlag);
		setAutoPing(autoping);
		init();
		initGameScanner();
	}
	
	public static String gameModeToStringLabel(int mode){
		switch(mode){
			case HearthScanner.ARENAMODE:
				return uiLang.t("Arena");
	
			case HearthScanner.RANKEDMODE:
				return uiLang.t("Ranked");
		
			case HearthScanner.UNRANKEDMODE:
				return uiLang.t("Unranked");
			
			case HearthScanner.PRACTICEMODE:
				return uiLang.t("Practice");
				
			case HearthScanner.CHALLENGEMODE:
				return uiLang.t("Challenge");
		}
		
		return uiLang.t("Unknown mode");
	}
	
	public static String gameModeToString(int mode){
		switch(mode){
			case HearthScanner.ARENAMODE:
				return "Arena";
	
			case HearthScanner.RANKEDMODE:
				return "Ranked";
		
			case HearthScanner.UNRANKEDMODE:
				return "Unranked";
			
			case HearthScanner.PRACTICEMODE:
				return"Practice";
				
			case HearthScanner.CHALLENGEMODE:
				return "Challenge";
		}
		
		return "Unknown mode";
	}
	
	public static String goesFirstToString(int goesFirst){
		switch(goesFirst){
			case 1:
				return "First";
	
			case 0:
				return "Second";
		}
		
		return "Unknown";
	}
	
	public static String goesFirstToStringLabel(int goesFirst){
		switch(goesFirst){
			case 1:
				return uiLang.t("First");
	
			case 0:
				return uiLang.t("Second");
		}
		
		return uiLang.t("Unknown");
	}
	
	public void pause(){
		paused = true;
		System.out.println("paused");
	}
	
	public void resume(){
		paused = false;
		System.out.println("resumed");
	}
	
	public synchronized void setGameLang(String lang){
		gameLang = lang;
		reinitScanner = true;
	}
	
	public synchronized void setGameRes(int w, int h){
		gameResX = w;
		gameResY = h;
		reinitScanner = true;
	}
	
	public synchronized void setAutoGameRes(boolean flag){
		autoDetectGameRes = flag;
		reinitScanner = true;
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
	
	public synchronized void setNotification(boolean enable){
		notify = enable;
	}
	
	public synchronized HearthReaderNotification getNotification(){
		
		if(notifications.size() == 0){
			return null;
		}
		
		HearthReaderNotification first = notifications.get(0);
		notifications.remove(0);
		
		return first;
	}
	
	public synchronized void addNotification(HearthReaderNotification note){
		if(notify == true){
			notifications.add(note);
		}
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
	
	private synchronized void initGameScanner(){
		int[] gameRes = this.getGameResolution();
		
		gameLang = sanitizeGameLang(gameLang);
		
		String pathScannerSettingByRes = String.format(HearthFilesNameManager.scannerSettingFileOverrideByRes, gameLang, gameRes[0], gameRes[1]);
		
		String pathScannerSetting = String.format(HearthFilesNameManager.scannerSettingFileDefault, gameLang);
		
		scannerSettings = (HearthScannerSettings) config.load(pathScannerSettingByRes);
		
		if(scannerSettings == null){
			scannerSettings = (HearthScannerSettings) config.load(pathScannerSetting);
		}
		
		if(scannerSettings == null){
			scannerSettings = new HearthScannerSettings();
			config.save(scannerSettings, pathScannerSetting);
		}
		
		for(int i = 0; i < heroesList.getTotal(); i++){
			this.prepareImageTarget(scannerSettings.arenaHeroScanboxes[i]);
			this.prepareImageTarget(scannerSettings.myHeroScanboxes[i]);
			this.prepareImageTarget(scannerSettings.opponentHeroScanboxes[i]);
		}
		
		for(int i = 0; i < scannerSettings.lossesScanboxes.length; i++){
			this.prepareImageTarget(scannerSettings.lossesScanboxes[i]);
		}
		
		for(int i = 0; i < scannerSettings.lossesUncheckedScanboxes.length; i++){
			this.prepareImageTarget(scannerSettings.lossesUncheckedScanboxes[i]);
		}
		
		for(int i = 0; i < scannerSettings.winsScanboxes.length; i++){
			this.prepareImageTarget(scannerSettings.winsScanboxes[i]);
		}
		
		for(int i = 0; i < scannerSettings.deckScanboxses.length; i++){
			this.prepareImageTarget(scannerSettings.deckScanboxses[i]);
		}
		
		this.prepareImageTarget(scannerSettings.rankedScanbox);
		this.prepareImageTarget(scannerSettings.unrankedScanbox);
		this.prepareImageTarget(scannerSettings.challengeScanbox);
		this.prepareImageTarget(scannerSettings.practiceScanbox);
		this.prepareImageTarget(scannerSettings.arenaLeafScanbox);
		this.prepareImageTarget(scannerSettings.victoryScanbox);
		this.prepareImageTarget(scannerSettings.defeatScanbox);

		this.initGameLang();
		gameScannerInitialized = true;
	}
		
	private void initGameLang(){		
		//language dependent
		this.prepareImageTarget(scannerSettings.goFirstScanbox);
		this.prepareImageTarget(scannerSettings.goSecondScanbox);
		inited = true;
	}
	
	private String sanitizeGameLang(String gLang){
		boolean foundLang = false;

		for(int i = 0; i < gameLanguages.langs.length; i++){
			if(gLang.toLowerCase().equals(gameLanguages.langs[i].code.toLowerCase())){
				foundLang = true;
				gLang = gameLanguages.langs[i].code; //make sure the cases are exactly the way it should (not an issue with filename case insensitive OS though).
				break;
			}
		}
		
		if(!foundLang){
			gLang = (gameLang == null || gLang.equals("")) ? gameLanguages.langs[0].code: gLang;
		}

		return gLang;
	}
	
	private float getScaleFactor(){
		int[] gameRes = this.getGameResolution();
		
		//do not scale if the height is 0 or 1080
		//zero usually means the game is running full screen
		if(gameRes[1] == 0 || gameRes[1] == 1080){
			return 1;
		}
		
		return (gameRes[1]/1080f);
	}
	
	private void prepareImageTarget(HearthScannerSettings.Scanbox sb){
		HearthImageTarget it = null;
		File file = null;
		float scaling = sb.scale * this.getScaleFactor();
		
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
				(int) (sb.mask.xOffset 	* scaling), 
				(int) (sb.mask.yOffset 	* scaling), 
				(int) (sb.mask.width 	* scaling), 
				(int) (sb.mask.height 	* scaling)
			);
		}
		
		it = new HearthImageTarget(preTarget);
		
		if(sb.matchQuality >= 0 && it != null){
			it.setMinScore(sb.matchQuality);
		}

		sb.target = it;
		
		if(sb.nestedSb != null){
			prepareImageTarget(sb.nestedSb);
		}
	}
			
	private boolean findImage(HearthScannerSettings.Scanbox sb, String label){
		return _findImage(sb, label);
	}
	
	private boolean _findImage(HearthScannerSettings.Scanbox sb, String label){
		boolean found = false;
		HearthImageTarget target = sb.target;
		Canvas canvas = new DesktopCanvas();
		float scaleFactor = this.getScaleFactor();
		int[] winRect = HearthHelper.getHearthstonePosition();
		int[] gameRes = this.getGameResolution();
				
		int boardX = this.getBoardX() + this.getXOffsetOverride();
		int boardY = this.getBoardY() + this.getYOffetOverride();

		int roiX = (int) (sb.xOffset * scaleFactor);
		int roiY = (int) (sb.yOffset * scaleFactor);
		int roiW = (int) (sb.width * scaleFactor);
		int roiH = (int) (sb.height * scaleFactor);
		
		lastScanArea[0]	= winRect[0] + this.getXOffsetOverride();
		lastScanArea[1]	= winRect[1] + this.getYOffetOverride();
		lastScanArea[2]	= gameRes[0];
		lastScanArea[3]	= gameRes[1];
		lastScanSubArea[0] = roiX + boardX;
		lastScanSubArea[1] = roiY + boardY;
		lastScanSubArea[2] = roiW;
		lastScanSubArea[3] = roiH;

		String hashKey = getBoardWidth() + "-" + getBoardHeight() + "-" + roiX + "-" + roiY + "-" + roiW + "-" + roiH + "-" + sb.imgfile; 
		ScreenRegion cachedRegion = regionsMap.get(hashKey);
		String magicHash = imageCacheMap.get(hashKey);

		//if the desktop region is not cached
		if(cachedRegion == null){
			//create a new screen region
			cachedRegion = new DesktopScreenRegion(boardX + roiX, boardY + roiY, roiW, roiH);
			//capture the screen
			cachedRegion.capture();
			regionsMap.put(hashKey, cachedRegion);
		}
		
		if(debugMode)
		{
			canvas.addBox(cachedRegion);
			canvas.addLabel(cachedRegion, "Region " + label).display(1);
		}
		
		if(target == null){
			System.out.println("Image file \"" + sb.imgfile + "\" didn't get loaded properly! ");
		}
		
		//masked image requires very specific offsets
		//make sure you the offsets are accurate enough
		if(sb.mask != null){
			BufferedImage regionImage = cachedRegion.getLastCapturedImage();
			
			HearthHelper.applyMaskImage(
				regionImage, 
				(int) (sb.mask.xOffset 	* scaleFactor), 
				(int) (sb.mask.yOffset 	* scaleFactor), 
				(int) (sb.mask.width 	* scaleFactor), 
				(int) (sb.mask.height 	* scaleFactor)
			);
		}
		
		ScreenRegion foundRegion = null;
		
		if(!enableMagicHash || magicHash == null){
			foundRegion = cachedRegion.find(target);
		} else {
			String[] parts = magicHash.split("-");
						
			if(parts.length == 5){
				int x = Integer.parseInt(parts[0]);
				int y = Integer.parseInt(parts[1]);
				int w = Integer.parseInt(parts[2]);
				int h = Integer.parseInt(parts[3]);
				String phashVal = parts[4];	
				
				BufferedImage targetImage = cachedRegion.getLastCapturedImage();
				targetImage = HearthHelper.cropImage(targetImage, x, y, w, h);
				String targetHash = null;

				try {
					targetHash = pHash.getHash(targetImage);
					int distance = pHash.distance(phashVal, targetHash);
					
					System.out.println("Magic Hash: " + magicHash);
					System.out.println("Distance: " + distance);
					
					if(distance < 20){
						foundRegion = cachedRegion.find(target);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}	
			}
		}

		if((foundRegion != null) && debugMode)
		{
			canvas.addBox(foundRegion);
			canvas.addLabel(foundRegion, "Found region " + label).display(1);
		}
		
		if(foundRegion != null){
			
			if(enableMagicHash && magicHash == null){
				Rectangle rec = foundRegion.getBounds();
				int targetRelativeX = rec.x - (roiX + boardX);
				int targetRelativeY = rec.y - (roiY + boardY);
				int targetWidth = rec.width;
				int targetHeight = rec.height;
								
				BufferedImage targetImage = cachedRegion.getLastCapturedImage();
				targetImage = HearthHelper.cropImage(targetImage, targetRelativeX, targetRelativeY, targetWidth, targetHeight);
				
				try {
					String phashVal = pHash.getHash(targetImage);
					magicHash = targetRelativeX 
								+ "-" + targetRelativeY 
								+ "-" + targetWidth 
								+ "-" + targetHeight
								+ "-" + phashVal;
					
					imageCacheMap.put(hashKey, magicHash);
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//String output = String.format(HearthFilesNameManager.scannerImageCacheFile, "out-" + hashKey);
				//HearthHelper.bufferedImageToFile(targetImage, output);
			}
			
			
			if(generateBeterOffsets){
				Rectangle rec = foundRegion.getBounds();
				int targetRelativeX = rec.x - boardX;
				int targetRelativeY = rec.y - boardY;
				
				sb.xOffset = ((int) (targetRelativeX/scaleFactor)) -4;
				sb.yOffset = ((int) (targetRelativeY/scaleFactor)) -4;
				sb.width   = ((int) (rec.width/scaleFactor)) + 8;
				sb.height  = ((int) (rec.height/scaleFactor)) + 8;
			}

			updateLastSeen();
			
			if(sb.nestedSb != null){
				HearthScannerSettings.Scanbox nestedSb = sb.nestedSb.makeCopy();
				
				//convert from absolute to relative offsets from captured region
				Rectangle rec = foundRegion.getBounds();
				int targetRelativeX = rec.x - boardX;
				int targetRelativeY = rec.y - boardY;
				
				//offsets relative to parent region
				nestedSb.xOffset = ((int) (targetRelativeX/scaleFactor)) + nestedSb.xOffset;
				nestedSb.yOffset = ((int) (targetRelativeY/scaleFactor)) + nestedSb.yOffset;				
				
				found = _findImage(nestedSb, label);
			} else {
				found = true;
			}
		}
		
		return found;
	}
	
	private synchronized void clearRegionsCache(){
		regionsMap.clear();
	}

	private synchronized void scanMode() {
		boolean found = false;
		
		if(!found && this.findImage(scannerSettings.arenaLeafScanbox, "Arena Leaf") ){
			gameMode = ARENAMODE;
			found =  true;
		}
		
		if(!found && this.findImage(scannerSettings.rankedScanbox, "Ranked mode Label")){
			gameMode = RANKEDMODE;
			found =  true;
		}
		
		if(!found && this.findImage(scannerSettings.unrankedScanbox, "Unranked mode Label")){
			gameMode = UNRANKEDMODE;
			found =  true;
		}
		
		if(!found && this.findImage(scannerSettings.challengeScanbox, "Challenge mode Label")){
			gameMode = CHALLENGEMODE;
			found =  true;
		}
		
		if(!found && this.findImage(scannerSettings.practiceScanbox, "Practice mode Label")){
			gameMode = PRACTICEMODE;
			found =  true;
		}
		
		if(found){
			//if we failed to detect the last game result scene
			if(isInGame()){
				//save the game result of last game regardless of the game result
				saveGameResult();
			}
			
			exVictory = -1;
			myHero = -1;
			oppHero = -1;
			inGameMode = 0;
			
			if(isArenaMode()){
				//reset selected deck for Arena until I find a proper solution to it
				selectedDeck = -1;
			}
	
			if(exGameMode != gameMode){
				exGameMode = gameMode; 
				isDirty = true;
				
				switch(gameMode){
					case MENUMODE:
						System.out.println("Mode: Main Menu");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Main menu detected") )
						);
					break;
					
					case RANKEDMODE:
						System.out.println("Mode: Ranked Mode");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Ranked mode detected") )
						);
					break;
					
					case UNRANKEDMODE:
						System.out.println("Mode: Unranked Mode");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Unranked mode detected") )
						);
					break;
					
					case ARENAMODE:
						System.out.println("Mode: Arena Mode");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Arena mode detected") )
						);
					break;
					
					case PRACTICEMODE:
						System.out.println("Mode: Practice Mode");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Practice mode detected") )
						);
					break;
					
					case CHALLENGEMODE:
						System.out.println("Mode: Challenge Mode");
						addNotification(
							new HearthReaderNotification( uiLang.t("Game Mode"), uiLang.t("Challenge mode detected") )
						);
					break;
					
					default:
						System.out.println("Mode: Unknown");
				}
			}
		}
				
		return;
	}
	
	public int getArenaWins(){
		return previousWins;
	}
	
	public int getArenaLosses(){
		return previousLosses;
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
	
	private synchronized void scanArenaScore() {
		boolean foundWins = false;
		boolean foundLosses = false;
		boolean saved = false;

		for(int i = (scannerSettings.winsScanboxes.length - 1); i >= 0; i--){
			foundWins = this.findImage(scannerSettings.winsScanboxes[i], "Wins (" + i + ")");
			
			if(foundWins){
				System.out.println("Found " + i + " wins" );
				wins = i;
				break;
			}
		}
		
		for(int i = scannerSettings.lossesScanboxes.length - 1; i >= 0; i--){
			if(this.findImage(scannerSettings.lossesScanboxes[i], "Losses " + (i+1))){
				System.out.println("Found " + (i+1) + " losses");
				foundLosses = true;
				losses = i+1;
				break;
			}
		}
		
		if(!foundLosses){
			if(this.findImage(scannerSettings.lossesUncheckedScanboxes[0], "First Unchecked Losses")){
				System.out.println("Found first unchecked losses");
				losses = 0;
			}
		}
				
		if(foundWins && (wins == (scannerSettings.winsScanboxes.length - 1) || losses == scannerSettings.lossesScanboxes.length) 
			&& (previousWins != wins || previousLosses != losses) ){

			try {
				int hero = myHero != -1 ? myHero : exMyHero;

				System.out.println("Saving arena result...");
				tracker.saveArenaResult(hero, wins, losses, new Date().getTime(), false);
				System.out.println("Done saving arena result...");
				isDirty = true;
				this.resetFlags();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(foundWins && (previousWins != wins || previousLosses != losses)){
			previousWins = wins;
			previousLosses = losses;
			String hero = myHero != -1 ? heroesList.getHeroLabel(myHero) : heroesList.getHeroLabel(exMyHero);
			
			if(!saved){
				addNotification(
					new HearthReaderNotification(
						uiLang.t("Arena score"), 
						uiLang.t("%d - %d as %s", wins, losses, hero)
					)
				);
			} else {
				addNotification(new HearthReaderNotification(
					uiLang.t("Arena result"), 
					uiLang.t("%d - %d as %s", wins, losses, hero)
				));
			}
			
			saved = false;
			isDirty = true;
		}
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
	
	private synchronized void resetFlags(){
		gameMode = -1;
		inGameMode = -1;
		victory = -1;
		myHero = -1;
		oppHero = -1;
		goFirst = -1;
	}
	
	private synchronized void scanSeletedDeck() {
		for(int i = 0; i < scannerSettings.deckScanboxses.length; i++){
			if(this.findImage(scannerSettings.deckScanboxses[i], "Deck (" + i + ") ")){
				
				HearthDecks decks = (HearthDecks) config.load(HearthFilesNameManager.decksFile);
				String deckName = "";
				
				if(decks == null){
					decks = new HearthDecks();
					//we don't need to save the decks.xml here because the main UI will handle it if it is missing.
				}

				selectedDeck = i;
				
				if(selectedDeck != exSelectedDeck){
					if(selectedDeck > -1 && selectedDeck < decks.list.length){
						deckName = decks.list[selectedDeck];
					}
					
					System.out.println("Found selected deck #: " + i);
					
					addNotification(
						new HearthReaderNotification( uiLang.t("Deck #%d", i+1), deckName)
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
					
					exSelectedDeck = selectedDeck;
					isDirty = true;
				}
				
				break;
			}
		}
	}
	
	private synchronized void scanArenaHero() {
		if(this.isInGame() || !this.isArenaMode()){
			return;
		}
		
		for(int i = 0; i < scannerSettings.arenaHeroScanboxes.length; i++){
			if(this.findImage(scannerSettings.arenaHeroScanboxes[i], "Arena Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found arena hero: (" + i + ") " + heroesList.getHeroLabel(i));
				myHero = i;
				
				if(myHero != exMyHero){
					exMyHero = myHero;
					isDirty = true;
				}
				
				break;
			}
		}
		
		return;
	}
	
	private synchronized void scanGameHeroes() {
		if(this.foundGameHero() || System.currentTimeMillis() - lastSave < 30000){
			return;
		}
				
		for(int i = 0; i < scannerSettings.myHeroScanboxes.length; i++){
			if(myHero == -1 && this.findImage(scannerSettings.myHeroScanboxes[i], "My Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found my hero: (" + i + ") " + heroesList.getHeroLabel(i));
				myHero = i;
				inGameMode = 1;
				if(myHero != exMyHero){
					exMyHero = myHero;
					isDirty = true;
					addNotification(
						new HearthReaderNotification( uiLang.t("Hero Detected"), uiLang.t("Your hero is %s",  heroesList.getHeroLabel(i)))
					);
				}
				break;
			}
		}
		
		for(int i = 0; i < scannerSettings.opponentHeroScanboxes.length; i++){
			if(this.findImage(scannerSettings.opponentHeroScanboxes[i], "Opp Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found opp hero: (" + i + ") " + heroesList.getHeroLabel(i));
				oppHero = i;
				inGameMode = 1;
				if(oppHero != exOppHero){
					exOppHero = oppHero;
					isDirty = true;
					addNotification(
						new HearthReaderNotification( uiLang.t("Hero Detected"), uiLang.t("Opponent hero is %s",  heroesList.getHeroLabel(i)))
					);
				}
				break;
			}
		}

		return;
	}
	
	private synchronized void scanVictory(){
		boolean found = false;
		
		if(this.findImage(scannerSettings.victoryScanbox, "Victory")){
			victory = 1;
			found = true;
		}
		
		if(!found && this.findImage(scannerSettings.defeatScanbox, "Defeat")){
			victory = 0;
			found = true;
		}
		
		if(found){
			assert goFirst == 1 || goFirst == 0;
			assert victory == 1 || victory == 0;
				
			isDirty = true;
			
			if(exVictory != victory){
				saveGameResult();
			}
			
		}
		
		return;
	}
	
	private void saveGameResult(){
		if(System.currentTimeMillis() - lastSave < 30000){
			return;
		}
		
		int totalTime = (int) (System.currentTimeMillis() - startTime)/1000;
		HearthDecks decks = (HearthDecks) config.load(HearthFilesNameManager.decksFile);
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
			tracker.saveMatchResult(gameMode, myHero, oppHero, goFirst, victory, startTime, totalTime, false, deckName);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done saving match result...");
		
		lastSave = System.currentTimeMillis();		
		exVictory = victory;
		inGameMode = 0;
		victory = -1;
		myHero = -1;
		oppHero = -1;
		exOppHero = -1;
		goFirst = -1;
		exGoFirst = -1;
	}
	
	public boolean isDirty(){

		if(isDirty){
			isDirty = false;
			return true;
		}
		
		return false;
	}
	
	public boolean isVictory(){
		return victory == 1 ? true : false;
	}
	
	private synchronized void scanCoinScreen() {
		if(goFirst != -1){
			return;
		}
		
		boolean found = false;
		
		if(this.findImage(scannerSettings.goFirstScanbox, "Go First")){
			goFirst = 1;
			found = true;
		}
		
		if(!found && this.findImage(scannerSettings.goSecondScanbox, "Go Second")){
			goFirst = 0;
			found = true;
		}
		
		//check are we scanning the "same" scene twice
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
						
			exGoFirst = goFirst;
			inGameMode = 1;
			startTime = System.currentTimeMillis();
			isDirty = true;
		}
		
		return;
	}
	
	public int getBoardWidth(){
		int[] gameRes = this.getGameResolution();
		return (gameRes[1]/3) * 4;
	}
	
	public int getBoardHeight(){
		int[] gameRes = this.getGameResolution();
		return gameRes[1];
	}
		
	public int getBoardX(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int xOffset = winPos[0];
		int[] gameRes = this.getGameResolution();
		int absoluteX = xOffset + (gameRes[0] - this.getBoardWidth()) / 2;		
		return absoluteX;
	}
	
	public int getBoardY(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int yOffset = winPos[1];		
		return yOffset;
	}
	
	public long getLastseen(){		
		return lastSeen;
	}
	
	public int[] getLastScanSubArea(){
		return lastScanSubArea;
	}
	
	public int[] getLastScanArea(){
		return lastScanArea;
	}
	
	public synchronized void setAlwaysScan(boolean enabled){
		alwaysScan = enabled;
	}
	
	public synchronized void setAutoPing(boolean enabled){
		pingHearthstone = enabled;
		seenHearthstone = false;
		lastPing = System.currentTimeMillis() - pingInterval;
	}
	
	private void autoPing(){
		if(pingHearthstone && seenHearthstone ){
			if( lastPing + pingInterval < System.currentTimeMillis() ){
				pingHearthstone();
				lastPing = System.currentTimeMillis();
				seenHearthstone = false;
			}
		}
	}
	
	public void forcePing(){
		forcePing = true;
	}
	
	public void pingHearthstone(){
		Canvas canvas = new DesktopCanvas();
		int lineWidth = 10;
		ScreenRegion region = new DesktopScreenRegion(
				lastScanArea[0],
				lastScanArea[1], 
				lastScanArea[2] - lineWidth / 2, 
				lastScanArea[3] - lineWidth / 2
		);
		
		ScreenRegion subregion = new DesktopScreenRegion(
				lastScanSubArea[0],
				lastScanSubArea[1], 
				lastScanSubArea[2] - lineWidth / 2, 
				lastScanSubArea[3] - lineWidth / 2
		);
		
		canvas.addBox(region).withLineColor(Color.blue).withLineWidth(10);
		canvas.addBox(subregion).withLineColor(Color.yellow).withLineWidth(10);
		canvas.display(2);
	}
	
	public boolean foundGameHero(){
		return myHero > - 1 && oppHero > - 1  ? true : false;
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
	
	public int[] getGameResolution(){
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
			oldGameResX = resolution[0];
			oldGameResY = resolution[1];
			
			if(gameScannerInitialized){
				this.initGameScanner();
			}			
		}
		
		return resolution;
	}
	
	private void updateLastSeen(){
		//if more than 1 minutes, force ping
		if(lastSeen + pingInterval < System.currentTimeMillis()){
			seenHearthstone = true;
		}
		
		lastSeen = System.currentTimeMillis();		
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
			arenaWins = 	tracker.getTotalWins(HearthScanner.ARENAMODE);
			arenaLosses = 	tracker.getTotalLosses(HearthScanner.ARENAMODE);
			arenaWinrate =  (arenaWins + arenaLosses) > 0 ? (float) arenaWins /  (arenaWins + arenaLosses) * 100: -1;
			rankedWins = 	tracker.getTotalWins(HearthScanner.RANKEDMODE);
			rankedLosses = 	tracker.getTotalLosses(HearthScanner.RANKEDMODE);
			rankedWinrate =  (rankedWins + rankedLosses) > 0 ? (float) rankedWins / (rankedWins + rankedLosses) * 100 : -1;
			unrankedWins = 	tracker.getTotalWins(HearthScanner.UNRANKEDMODE);
			unrankedLosses = 	tracker.getTotalLosses(HearthScanner.UNRANKEDMODE);
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
	
	public void process(){
		boolean hsDetected = alwaysScan || (!alwaysScan && HearthHelper.isHSDetected());

		if(paused || !hsDetected){
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//
			}
			return;
		}
		
		long startBench = System.currentTimeMillis();
		
		scanMode();
		
		if(isArenaMode() && !isInGame()){
			scanArenaHero();
			scanArenaScore();
		}
		
		if((isRankedMode() || isUnrankedMode() || isChallengeMode() || isPracticeMode()) && !isInGame()){
			scanSeletedDeck();
		}
		
		scanCoinScreen();
		scanGameHeroes();
		scanVictory();	
		autoPing();
		
		//clear the cached regions
		clearRegionsCache();
		
		System.out.println("Process() time spent: " + (System.currentTimeMillis() - startBench) + " ms");
		
		if(reinitScanner){
			initGameScanner();
			reinitScanner = false;
		}
		
		if(forcePing){
			pingHearthstone();
			forcePing = false;
		}
	}
	
	public void dispose(){
		
		if(generateBeterOffsets){
			System.out.println("saving 'better' offsets");
			String path = String.format(HearthFilesNameManager.scannerSettingFileDefault, "better");
			config.save(scannerSettings, path);
		}
	}
}
	
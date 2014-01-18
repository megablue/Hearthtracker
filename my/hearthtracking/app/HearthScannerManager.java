package my.hearthtracking.app;

import java.awt.AWTException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;

import my.hearthtracking.app.HearthScannerSettings.Scanbox;

public class HearthScannerManager {
	public static final int UNKNOWNMODE = -1;
	public static final int MENUMODE = 0;
	public static final int ARENAMODE = 1;
	public static final int RANKEDMODE = 2;
	public static final int UNRANKEDMODE = 3;
	public static final int CHALLENGEMODE = 4;
	public static final int PRACTICEMODE = 5;
	
	public static final float BASE_RESOLUTION_HEIGHT = 1080f;

	private boolean debugMode = false;
	private boolean autoDetectGameRes = true;
	private boolean inited = false;
	private boolean scannerSettingsInitialzed = false;
	private volatile boolean reInitScannerSettings = false;
	private volatile boolean alwaysScan = true;

	private HearthGameLangList gameLanguages;
	private String gameLang;

	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;

	private int gameResX = 1920, gameResY = 1080;
	private int oldGameResX = -1, oldGameResY = -1;

	private HearthTracker tracker;
	private HearthScannerSettings scannerSettings = null;
	private int xOffetOverrideVal = 0;
	private int yOffsetOverrideVal = 0;

	private Robot robot = null;

	private HearthScanner scanner = new HearthScanner();

	public HearthScannerManager (HearthTracker t, String lang, int resX, int resY, boolean autoping, boolean alwaysScanFlag){
		debugMode = HearthHelper.isDevelopmentEnvironment();
		tracker = t;
		gameResX = resX;
		gameResY = resY;
		gameLang = lang.toLowerCase();
		alwaysScan = alwaysScanFlag; 
		init();
		initScannerSettings();
	}
	
	public class MyThread implements Runnable {

		public MyThread(Object parameter) {
			// store parameter for later user
		}

		public void run() {
			
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
		
		try {
			robot = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
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
		
		for(Scanbox sb : scannerSettings.list){
			prepareScanbox(sb);
			scanner.addScanbox(sb);
		}
				
		scanner.setScale(getScaleFactor());
		scanner.init();
		
		scannerSettingsInitialzed = true;
		reInitScannerSettings = false;
	}
		
	private synchronized void prepareScanbox(HearthScannerSettings.Scanbox sb){
		HearthImageTarget it = null;
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
		
		it = new HearthImageTarget(preTarget);
		
		if(sb.matchQuality >= 0 && it != null){
			it.setMinScore(sb.matchQuality);
		}
		
		sb.target = it;
		
		if(sb.nestedSb != null){
			prepareScanbox(sb.nestedSb);
		}
	}
	
	private synchronized BufferedImage capture(){
		int gameScreenWidth		= getBoardWidth(),
			gameScreenHeight	= getBoardHeight(),
			boardX 				= getBoardX() + getXOffsetOverride(),
			boardY 				= getBoardY() + getYOffetOverride();
		
		Rectangle rec = new Rectangle(boardX, boardY, gameScreenWidth, gameScreenHeight);
		return robot.createScreenCapture(rec);		
	}
	
	public void process(){
		boolean scanAllowed = alwaysScan || (!alwaysScan && HearthHelper.isHSDetected());
		
		if(!scanAllowed){
			return;
		}
		
		long startBench = System.currentTimeMillis();
		long benchDiff = 0;
		
		BufferedImage frame = capture();
		benchDiff = (System.currentTimeMillis() - startBench);
		System.out.println("Capture() time spent: " + benchDiff + " ms");
		
		scanner.insertFrame(frame);
		
		startBench = System.currentTimeMillis();
		scanner.scan();
		benchDiff  = (System.currentTimeMillis() - startBench);
		
		System.out.println("Scan() time spent: " + benchDiff + " ms");
		
		if(reInitScannerSettings){
			initScannerSettings();
		}
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
	
	public void setAlwaysScan(boolean flag){
		alwaysScan = flag;
	}
	
	public void setAutoPing(boolean flag){
		
	}

	public void setNotification(boolean flag){
		
	}
	
	public void resume(){
		
	}
	
	public void pause(){
		
	}
	
	public void forcePing(){
		
	}
	
	public void dispose(){
		String path = String.format(HearthFilesNameManager.scannerSettingFileDefault, gameLang);
		config.save(scannerSettings, path);
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
		return false;
	}
	
	public String getOverview(){
		return "";
	}
	
	public HearthReaderNotification getNotification(){
		return null;
	}
}

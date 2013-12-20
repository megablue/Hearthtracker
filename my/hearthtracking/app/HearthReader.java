package my.hearthtracking.app;

import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthReader {
	public static final int UNKNOWNMODE = -1;
	public static final int MENUMODE = 0;
	public static final int ARENAMODE = 1;
	public static final int RANKEDMODE = 2;
	public static final int UNRANKEDMODE = 3;
	public static final int CHALLENGEMODE = 4;
	public static final int PRACTICEMODE = 5;
	boolean debugMode = false;
	boolean inited = false;
	boolean gameLangInited = false;
	int xOffetOverrideVal = 0;
	int yOffsetOverrideVal = 0;
	
	int previousWins = -1;
	int wins = -1;
	
	int previousLosses = -1;
	int losses = -1;
	
	int myHero = -1;
	int oppHero = -1;
	
	int exMyHero = -1;
	int exOppHero = -1;
	
	int victory = -1;
	int exVictory = -1;
	int goFirst = -1;
	int exGoFirst = -1;
	Date startTime = new Date();
	Date lastUpdate = new Date();

	int gameMode = UNKNOWNMODE;
	int exGameMode = UNKNOWNMODE;
	int inGameMode = -1;
	
	boolean paused = false;

	HearthTracker tracker = null;

	Target rankedImageTarget;
	Target unrankedImageTarget;
	Target practiceImageTarget;
	Target challengeImageTarget;
	Target questImageTarget;
	Target checkedImageTarget;
	Target uncheckedImageTarget;
	Target arenaLeafImageTarget;
	Target goFirstImageTarget;
	Target goSecondImageTarget;
	Target victoryImageTarget;
	Target defeatImageTarget;

	Target[] winsImageTarget;
	
	HearthGameLangList gameLanguages;
	String gameLang;
		
	Target[] heroesIT;
	Target[] heroesThumbIT;
	
	HearthReaderSetting readerSettings = null;
	
	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;
	
	int gameResX = 1920, gameResY = 1080;
	int oldGameResX = 1920, oldGameResY = 1080;
	
	boolean pingHearthstone = true;
	boolean seenHearthstone = false;
	boolean autoDetectGameRes = true;
	
	long pingInterval = 60 * 1000;
	Date lastSeen =  new Date(0);
	boolean isDirty = true;
	int[] lastScanArea = {0,0,0,0};
	int[] lastScanSubArea = {0,0,0,0};
	Date lastPing = new Date(new Date().getTime() - pingInterval);
	
	private boolean alwaysScan = false;
	
	private List<HearthReaderNotification> notifications = new ArrayList<HearthReaderNotification>();
	
	public HearthReader(HearthTracker t){
		//debugMode = HearthHelper.isDevelopmentEnvironment();
		tracker = t;
		init();
	}
	
	public HearthReader (HearthTracker t, String lang, int resX, int resY, boolean autoping, boolean alwaysScanFlag){
		//debugMode = HearthHelper.isDevelopmentEnvironment();
		tracker = t;
		oldGameResX = gameResX = resX;
		oldGameResY = gameResY = resY;
		gameLang = lang.toLowerCase();
		setAlwaysScan(alwaysScanFlag);
		setAutoPing(autoping);
		init();
	}
	
	public static String gameModeToString(int mode){
		switch(mode){
			case HearthReader.ARENAMODE:
				return "Arena";
	
			case HearthReader.RANKEDMODE:
				return "Ranked";
		
			case HearthReader.UNRANKEDMODE:
				return "Unranked";
			
			case HearthReader.PRACTICEMODE:
				return "Practice";
				
			case HearthReader.CHALLENGEMODE:
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
		
		return "unknown";
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
		this.initGameScanner();
	}
	
	public synchronized void setGameRes(int w, int h){
		gameResX = w;
		gameResY = h;
		this.initGameScanner();
	}
	
	public synchronized void setAutoGameRes(boolean flag){
		autoDetectGameRes = flag;
		this.initGameScanner();
	}
	
	public synchronized void setXOffetOverride(int val){
		xOffetOverrideVal = val;
	}
	
	public int getXOffsetOverride(){
		return xOffetOverrideVal;
	}
	
	public synchronized void setYOffetOverride(int val){
		yOffsetOverrideVal = val;
	}
	
	public int getYOffetOverride(){
		return yOffsetOverrideVal;
	}
	
	public synchronized HearthReaderNotification getNotification(){
		
		if(notifications.size() == 0){
			return null;
		}
		
		HearthReaderNotification first = notifications.get(0);
		notifications.remove(0);
		
		return first;
	}
	
	private synchronized void init(){
		if(inited){
			return;
		}
		
		heroesList = (HearthHeroesList) config.load("." + File.separator + "configs" + File.separator + "heroes.xml");
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, "." + File.separator + "configs" + File.separator + "heroes.xml");
		}
		
		gameLanguages = (HearthGameLangList) config.load("." + File.separator + "configs" + File.separator + "gameLangs.xml");
		
		if(gameLanguages == null){
			gameLanguages = new HearthGameLangList();
			config.save(gameLanguages, "." + File.separator + "configs" + File.separator + "gameLangs.xml");
		}
		
		inited = true;
		
		this.initGameScanner();
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
		
		//do not scale if the height is 1080
		if(gameRes[1] == 1080){
			return 1;
		}
		
		return (gameRes[1]/1080f);
	}
	
	private Target prepareImageTarget(HearthReaderSetting.Scanbox sb){
		Target it = null;
		File file = null;
		float scaling = sb.scale * this.getScaleFactor();
		
		if(HearthHelper.fileExists("." + File.separator + "images" + File.separator + gameLang + File.separator + sb.imgfile)){
			file = new File("." + File.separator + "images" + File.separator + gameLang + File.separator + sb.imgfile);
		} else {
			file = new File("." + File.separator + "images" + File.separator + sb.imgfile);
		}
		
		if(scaling != 1){
			it = new ImageTarget(HearthHelper.resizeImage(file, scaling));
		} else {
			it = new ImageTarget(file);
		}
		
		if(sb.matchQuality >= 0 && it != null){
			it.setMinScore(sb.matchQuality);
		}
		
		return it;
	}
	
	private synchronized void initGameScanner(){
		int[] gameRes = this.getGameResolution();
		
		gameLang = sanitizeGameLang(gameLang);
		
		String pahtGameSettingByResolution = "." + File.separator + "configs" 
											+ File.separator + "gameLangs" 
											+ File.separator + gameLang 
											+ File.separator + gameRes[0] + "x" + gameRes[1]
											+ ".xml";
		
		String pathGameSetting = "." + File.separator + "configs" + File.separator + "gameLangs" + File.separator + gameLang + ".xml";
		
		readerSettings = (HearthReaderSetting) config.load(pahtGameSettingByResolution);
		
		if(readerSettings == null){
			readerSettings = (HearthReaderSetting) config.load(pathGameSetting);
		}
		
		if(readerSettings == null){
			readerSettings = new HearthReaderSetting();
			config.save(readerSettings, pathGameSetting);
		}
		
		heroesIT = new ImageTarget[heroesList.getTotal()];
		heroesThumbIT = new ImageTarget[heroesList.getTotal()];
		
		for(int i = 0; i < heroesList.getTotal(); i++){
			heroesIT[i] = this.prepareImageTarget(readerSettings.arenaHeroScanboxes[i]);
			heroesThumbIT[i] = this.prepareImageTarget(readerSettings.opponentHeroScanboxes[i]);
		}
		
		questImageTarget 	= this.prepareImageTarget(readerSettings.menuScanbox);
		checkedImageTarget 	= this.prepareImageTarget(readerSettings.lossesScanboxes[0]);
		uncheckedImageTarget 	= this.prepareImageTarget(readerSettings.lossesUncheckedScanboxes[0]);
		
		rankedImageTarget = this.prepareImageTarget(readerSettings.rankedScanbox);
		unrankedImageTarget = this.prepareImageTarget(readerSettings.unrankedScanbox);
		challengeImageTarget = this.prepareImageTarget(readerSettings.challengeScanbox);
		practiceImageTarget = this.prepareImageTarget(readerSettings.practiceScanbox);
		arenaLeafImageTarget 	= this.prepareImageTarget(readerSettings.arenaLeafScanbox);
		
		this.initGameLang();
	}
		
	private void initGameLang(){		
		//language dependent
		winsImageTarget = new ImageTarget[readerSettings.winsScanboxes.length];
		
		for(int i = 0; i < readerSettings.winsScanboxes.length; i++){
			winsImageTarget[i] =  this.prepareImageTarget(readerSettings.winsScanboxes[i]);
		}
		
		goFirstImageTarget 		= this.prepareImageTarget(readerSettings.goFirstScanbox);
		goSecondImageTarget 	= this.prepareImageTarget(readerSettings.goSecondScanbox);
		victoryImageTarget 		= this.prepareImageTarget(readerSettings.victoryScanbox);
		defeatImageTarget 		= this.prepareImageTarget(readerSettings.defeatScanbox);
		
		gameLangInited = true;
	}
	
	@SuppressWarnings("unused")
	private boolean findText(ScreenRegion region, TextTarget target, String label){
		Canvas canvas = new DesktopCanvas();
		ScreenRegion foundRegion;

		if(debugMode)
		{
			canvas.addBox(region);
			canvas.addLabel(region, "Region " + label).display(1);
		}
		
		foundRegion = region.find(target);
		
		if((foundRegion != null) && debugMode)
		{
			canvas.addBox(region);
			canvas.addLabel(region, "Found region " + label).display(1);
		}
		
		return (foundRegion != null);
	}
		
	private boolean findImage(HearthReaderSetting.Scanbox sb, Target target, String label){
		Canvas canvas = new DesktopCanvas();
		int x = 0, y = 0, w = 0, h = 0;
		float scaling = this.getScaleFactor();
		int[] winRect = HearthHelper.getHearthstonePosition();
		int[] gameRes = this.getGameResolution();
		
		lastScanArea[0]	= winRect[0] + this.getXOffsetOverride();
		lastScanArea[1]	= winRect[1] + this.getYOffetOverride();
		lastScanArea[2]	= gameRes[0];
		lastScanArea[3]	= gameRes[1];
		
		lastScanSubArea[0] = x = ((int) (scaling * sb.xOffset) + this.getBoardX()) + this.getXOffsetOverride();
		lastScanSubArea[1] = y = ((int) (scaling * sb.yOffset) + this.getBoardY()) + this.getYOffetOverride();
		lastScanSubArea[2] = w = (int) (sb.width * scaling);
		lastScanSubArea[3] = h = (int) (sb.height * scaling);
					
		ScreenRegion region = new DesktopScreenRegion(x, y, w, h);
		ScreenRegion foundRegion;

		if(debugMode)
		{
			canvas.addBox(region);
			canvas.addLabel(region, "Region " + label).display(1);
		}
		
		foundRegion = region.find(target);
		
		if((foundRegion != null) && debugMode)
		{
			canvas.addBox(region);
			canvas.addLabel(region, "Found region " + label).display(1);
		}
		
		if(foundRegion != null){
			updateLastSeen();
		}
		
		return (foundRegion != null);
	}

	private synchronized void scanMode() {
		if(this.findImage(readerSettings.menuScanbox, questImageTarget, "Quest icon")){
			gameMode = MENUMODE;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode;
				myHero = -1;
				isDirty = true;
				System.out.println("Mode: Menu Mode");
				notifications.add(new HearthReaderNotification("Mode", "Menu detected"));
			}

			return;
		}
		
		if(this.findImage(readerSettings.arenaLeafScanbox, arenaLeafImageTarget, "Arena Leaf") ){
			gameMode = ARENAMODE;
			oppHero = -1;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode;
				isDirty = true;
				System.out.println("Mode: Arena Mode");
				notifications.add(new HearthReaderNotification("Mode", "Arena detected"));
			}
			
			return;
		}
		
		if(this.findImage(readerSettings.rankedScanbox, rankedImageTarget, "Ranked mode Label")){
			gameMode = RANKEDMODE;
			oppHero = -1;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode;
				isDirty = true;
				System.out.println("Mode: Ranked Mode");
				notifications.add(new HearthReaderNotification("Mode", "Ranked mode detected"));
			}
			
			return;
		}
		
		if(this.findImage(readerSettings.unrankedScanbox, unrankedImageTarget, "Unranked mode Label")){
			gameMode = UNRANKEDMODE;
			oppHero = -1;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode; 
				isDirty = true;
				System.out.println("Mode: Unranked Mode");
				notifications.add(new HearthReaderNotification("Mode", "Unranked mode detected"));
			}
			
			return;
		}
		
		if(this.findImage(readerSettings.challengeScanbox, challengeImageTarget, "Challenge mode Label")){
			gameMode = CHALLENGEMODE;
			oppHero = -1;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode; 
				isDirty = true;
				System.out.println("Mode: Challenge Mode");
				notifications.add(new HearthReaderNotification("Mode", "Challenge mode detected"));
			}

			return;
		}
		
		if(this.findImage(readerSettings.practiceScanbox, practiceImageTarget, "Practice mode Label")){
			gameMode = PRACTICEMODE;
			oppHero = -1;
			inGameMode = 0;
			
			if(exGameMode != gameMode){
				exGameMode = gameMode; 
				isDirty = true;
				System.out.println("Mode: Practice Mode");
				notifications.add(new HearthReaderNotification("Mode", "Practice mode detected"));
			}
			
			return;
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

		for(int i = (winsImageTarget.length - 1); i >= 0; i--){
			foundWins = this.findImage(readerSettings.winsScanboxes[i], winsImageTarget[i], "Wins (" + i + ")");
			
			if(foundWins){
				System.out.println("Found " + i + " wins" );
				wins = i;
				break;
			}
		}
		
		for(int i = 2; i >= 0; i--){
			if(this.findImage(readerSettings.lossesScanboxes[i], checkedImageTarget, "Losses " + (i+1))){
				System.out.println("Found " + (i+1) + " losses");
				foundLosses = true;
				losses = i+1;
				break;
			}
		}
		
		if(!foundLosses){
			for(int i = 0; i < 3; i++){
				if(this.findImage(readerSettings.lossesUncheckedScanboxes[i], uncheckedImageTarget, "Unchecked Losses " + (i+1))){
					System.out.println("Found " + (i+1) + " unchecked losses");
					losses = 3 - (i + 1);
				}
			}
		}
				
		if(foundWins && (wins == (winsImageTarget.length - 1) || losses == 3) && (previousWins != wins || previousLosses != losses) ){

			try {
				System.out.println("Saving arena result...");
				tracker.saveArenaResult(myHero, wins, losses, new Date().getTime(), false);
				System.out.println("Done saving arena result...");
				this.formatArenaStatus();
				isDirty = true;
				this.resetFlags();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
		if(foundWins && (previousWins != wins || previousLosses != losses)){
			previousWins = wins;
			previousLosses = losses;
			notifications.add(new HearthReaderNotification("Arena score", "Wins " + wins + ", losses " + losses));
			isDirty = true;
		}
	}
	
	public String getGameMode(){
		if(this.isArenaMode()){
			return "Arena";
		}
		
		if(this.isRankedMode()){
			return "Ranked";
		}
		
		if(this.isUnrankedMode()){
			return "Unranked";
		}
		
		if(this.isPracticeMode()){
			return "Practice";
		}
		
		if(this.isChallengeMode()){
			return "Challenge";
		}
		
		return "Unknown";
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
	
	private synchronized void scanArenaHero() {
		if(this.isInGame() || !this.isArenaMode()){
			return;
		}
		
		for(int i = 0; i < heroesIT.length; i++){
			if(this.findImage(readerSettings.arenaHeroScanboxes[i], heroesIT[i], "Arena Hero (" + heroesList.getHeroLabel(i) + ") ")){
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
		if(this.foundGameHero()){
			return;
		}
		
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(myHero == -1 && this.findImage(readerSettings.myHeroScanboxes[i], heroesThumbIT[i], "My Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found my hero: (" + i + ") " + heroesList.getHeroLabel(i));
				myHero = i;
				if(myHero != exMyHero){
					exMyHero = myHero;
					isDirty = true;
					notifications.add(new HearthReaderNotification("Hero Detected", "Your hero is " + heroesList.getHeroLabel(i)));
				}
				
				this.formatMatchStatus();
				break;
			}
		}
		
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(this.findImage(readerSettings.opponentHeroScanboxes[i], heroesThumbIT[i], "Opp Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found opp hero: (" + i + ") " + heroesList.getHeroLabel(i));
				oppHero = i;
				if(oppHero != exOppHero){
					exOppHero = oppHero;
					exVictory = -1;
					isDirty = true;
					
					notifications.add(new HearthReaderNotification("Hero Detected", "Opponent hero is " + heroesList.getHeroLabel(i)));
				}
				this.formatMatchStatus();
				break;
			}
		}

		return;
	}
	
	private synchronized void scanVictory(){
		boolean found = false;
		
		if(this.findImage(readerSettings.victoryScanbox, victoryImageTarget, "Victory")){
			victory = 1;
			found = true;
		}
		
		if(!found && this.findImage(readerSettings.defeatScanbox, defeatImageTarget, "Defeat")){
			victory = 0;
			found = true;
		}
		
		if(found){
			int totalTime = (int) (new Date().getTime() - startTime.getTime())/1000;
			
			assert goFirst == 1 || goFirst == 0;
			assert victory == 1 || victory == 0;
			
			this.formatMatchStatus();
			
			try {
				isDirty = true;
				
				if(exVictory != victory){
					
					if(victory == 1){
						System.out.println("Found Victory");
						notifications.add(new HearthReaderNotification("Game Result", "Victory"));
					} else if(victory == 1) {
						System.out.println("Found Defeat");
						notifications.add(new HearthReaderNotification("Game Result", "Defeat"));
					}
					
					System.out.println("Saving match result...");
					tracker.saveMatchResult(gameMode, myHero, oppHero, goFirst, victory, startTime.getTime(), totalTime, false);
					System.out.println("Done saving match result...");
					
					exVictory = victory;
					inGameMode = 0;
					victory = -1;
					myHero = -1;
					oppHero = -1;
					goFirst = -1;
				}
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		
		return;
	}
	
	public boolean isDirty(){

		if(isDirty){
			isDirty = false;
			return true;
		}
		
		return false;
	}
	
	private synchronized void formatMatchStatus(){
		String whosFirst = goFirst == 1 ? "Goes first" : "Goes second";
		String result = victory == 1 ? ", 1 - 0 " : ", 0 - 1 ";
		String output = "Unknown";
		
		if(myHero == -1){
			return;
		}
		
		if(goFirst != -1){
			output = whosFirst;
		}
		
		if(goFirst != -1 && oppHero != -1){
			output = whosFirst + ", vs " + heroesList.getHeroLabel(oppHero);
		}
		
		if(goFirst != -1 && oppHero != -1 && victory != -1){
			output = whosFirst + ", vs " + heroesList.getHeroLabel(oppHero) + result;
		}
		
		tracker.ouputMatchStatus(HearthReader.ARENAMODE, output);
	}
	
	private synchronized void formatArenaStatus(){
		String score = "Unknown";
		String hero = "Unknown";

		if(wins != -1){
			if(losses == -1){
				losses = 0;
			}
			
			score = wins + " - " + losses;
		}
		
		if(myHero != -1){
			hero = heroesList.getHeroLabel(myHero);
		}
		
		tracker.outputArenaStatus(ARENAMODE, score, hero);
	}
	
	public boolean isVictory(){
		return victory == 1 ? true : false;
	}
	
	private synchronized void scanCoinScreen() {
		if(goFirst != -1){
			return;
		}
		
		if(this.findImage(readerSettings.goFirstScanbox, goFirstImageTarget, "Go First")){
			
			if(exGoFirst != goFirst){
				System.out.println("Found go first");
				goFirst = 1;
				exGoFirst = goFirst;
				inGameMode = 1;
				exVictory = -1;
				startTime = new Date();
				this.formatMatchStatus();
				isDirty = true;
				notifications.add(new HearthReaderNotification("Game start detected", "You go first!"));
			}

			return;
		}
		
		if(this.findImage(readerSettings.goSecondScanbox, goSecondImageTarget, "Go Second")){
			
			if(exGoFirst != goFirst){
				System.out.println("Found go second");
				goFirst = 0;
				exGoFirst = goFirst;
				inGameMode = 1;
				exVictory = -1;
				startTime = new Date();
				this.formatMatchStatus();
				isDirty = true;
				notifications.add(new HearthReaderNotification("Game start detected", "You go second!"));
			}
			
			return;
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
	
	public Date getLastseen(){		
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
		lastPing = new Date(new Date().getTime() - pingInterval);
	}
	
	private void autoPing(){
		if(pingHearthstone && seenHearthstone ){
			if( lastPing.getTime() + pingInterval < new Date().getTime() ){
				pingHearthstone();
				lastPing = new Date();
				seenHearthstone = false;
			}
		}
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
		resolution[1] =  winPos[3] - winPos[1];
		
		//use default resolution if failed to detect
		if(resolution[0] == 0 || resolution[1] == 0){
			resolution[0] = gameResX;
			resolution[1] = gameResY;
		}
		
		if(resolution[0] != oldGameResX || resolution[1] != oldGameResY){
			oldGameResX = resolution[0];
			oldGameResY = resolution[1];
			this.initGameScanner();
		}
		
		return resolution;
	}
	
	private void updateLastSeen(){
		//if more than 1 minutes, force ping
		if(lastSeen.getTime() + pingInterval < new Date().getTime()){
			seenHearthstone = true;
		}
		
		lastSeen = new Date();		
	}
	
	public void process(){
		boolean hsDetected = alwaysScan || (!alwaysScan && HearthHelper.isHSDetected());
		
		if(paused || !hsDetected){
			return;
		}

		this.scanMode();
		
		if(this.isArenaMode()){
			this.scanArenaScore();
			this.scanArenaHero();
		}
		
		this.scanCoinScreen();
		this.scanGameHeroes();
		this.scanVictory();
		
//		if(this.isArenaMode() || this.isRankedMode() || this.isUnrankedMode() || this.isChallengeMode() || this.isPracticeMode()){
//			if(!this.isInGame()){
//				
//				if(this.isArenaMode()){
//					this.scanArenaScore();
//					this.scanArenaHero();
//				}
//
//				this.scanCoinScreen();
//			} else {
//				this.scanGameHeroes();
//				this.scanVictory();
//			}
//		}
		
		this.autoPing();
	}
}
	
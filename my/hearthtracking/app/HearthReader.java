package my.hearthtracking.app;

import java.awt.Color;
import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
	
	private int exMyHero = -1;
	private int exOppHero = -1;
	
	private int victory = -1;
	private int exVictory = -1;
	private int goFirst = -1;
	private int exGoFirst = -1;
	private Date startTime = new Date();
	private Date lastSave = new Date();

	private int gameMode = UNKNOWNMODE;
	private int exGameMode = UNKNOWNMODE;
	private int inGameMode = -1;
	
	private boolean paused = false;

	private HearthTracker tracker = null;

	private Target rankedImageTarget;
	private Target unrankedImageTarget;
	private Target practiceImageTarget;
	private Target challengeImageTarget;
//	private Target questImageTarget;
	private Target checkedImageTarget;
	private Target uncheckedImageTarget;
	private Target arenaLeafImageTarget;
	private Target goFirstImageTarget;
	private Target goSecondImageTarget;
	private Target victoryImageTarget;
	private Target defeatImageTarget;

	private Target[] winsImageTarget;
	
	private HearthGameLangList gameLanguages;
	private String gameLang;
		
	private Target[] heroesIT;
	private Target[] heroesThumbIT;
	
	private HearthReaderSetting readerSettings = null;
	
	
	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;
	
	private int gameResX = 1920, gameResY = 1080;
	private int oldGameResX = 1920, oldGameResY = 1080;
	
	private boolean pingHearthstone = true;
	private boolean seenHearthstone = false;
	private boolean autoDetectGameRes = true;
	
	private long pingInterval = 60 * 1000;
	private Date lastSeen =  new Date(0);
	private boolean isDirty = true;
	private int[] lastScanArea = {0,0,0,0};
	private int[] lastScanSubArea = {0,0,0,0};
	private Date lastPing = new Date(new Date().getTime() - pingInterval);
	
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
		
//		questImageTarget 	= this.prepareImageTarget(readerSettings.menuScanbox);
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
		boolean found = false;
		
//		if(this.findImage(readerSettings.menuScanbox, questImageTarget, "Quest icon")){
//			gameMode = MENUMODE;
//			found =  true;
//		}
		
		if(!found && this.findImage(readerSettings.arenaLeafScanbox, arenaLeafImageTarget, "Arena Leaf") ){
			gameMode = ARENAMODE;
			found =  true;
		}
		
		if(!found && this.findImage(readerSettings.rankedScanbox, rankedImageTarget, "Ranked mode Label")){
			gameMode = RANKEDMODE;
			found =  true;
		}
		
		if(!found && this.findImage(readerSettings.unrankedScanbox, unrankedImageTarget, "Unranked mode Label")){
			gameMode = UNRANKEDMODE;
			found =  true;
		}
		
		if(!found && this.findImage(readerSettings.challengeScanbox, challengeImageTarget, "Challenge mode Label")){
			gameMode = CHALLENGEMODE;
			found =  true;
		}
		
		if(!found && this.findImage(readerSettings.practiceScanbox, practiceImageTarget, "Practice mode Label")){
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
	
			if(exGameMode != gameMode){
				exGameMode = gameMode; 
				isDirty = true;
				
				switch(gameMode){
					case MENUMODE:
						System.out.println("Mode: Main Menu");
						notifications.add(new HearthReaderNotification("Game Mode", "Main menu detected"));
					break;
					
					case RANKEDMODE:
						System.out.println("Mode: Ranked Mode");
						notifications.add(new HearthReaderNotification("Game Mode", "Ranked mode detected"));
					break;
					
					case UNRANKEDMODE:
						System.out.println("Mode: Unranked Mode");
						notifications.add(new HearthReaderNotification("Game Mode", "Unranked mode detected"));
					break;
					
					case ARENAMODE:
						System.out.println("Mode: Arena Mode");
						notifications.add(new HearthReaderNotification("Game Mode", "Arena mode detected"));
					break;
					
					case PRACTICEMODE:
						System.out.println("Mode: Practice Mode");
						notifications.add(new HearthReaderNotification("Game Mode", "Practice mode detected"));
					break;
					
					case CHALLENGEMODE:
						System.out.println("Mode: Challenge Mode");
						notifications.add(new HearthReaderNotification("Game Mode", "Challenge mode detected"));
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
			if(this.findImage(readerSettings.lossesUncheckedScanboxes[0], uncheckedImageTarget, "First Unchecked Losses")){
				System.out.println("Found first unchecked losses");
				losses = 0;
			}
		}
				
		if(foundWins && (wins == (winsImageTarget.length - 1) || losses == 3) && (previousWins != wins || previousLosses != losses) ){

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
				notifications.add(new HearthReaderNotification("Arena score", "Wins " + wins + ", losses " + losses + " as " + hero));
			} else {
				notifications.add(new HearthReaderNotification("Arena concluded", "Wins " + wins + ", losses " + losses + " as " + hero));
			}
			
			saved = false;
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
		if(this.foundGameHero() || new Date().getTime() - lastSave.getTime() < 10000){
			return;
		}
				
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(myHero == -1 && this.findImage(readerSettings.myHeroScanboxes[i], heroesThumbIT[i], "My Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found my hero: (" + i + ") " + heroesList.getHeroLabel(i));
				myHero = i;
				inGameMode = 1;
				if(myHero != exMyHero){
					exMyHero = myHero;
					isDirty = true;
					notifications.add(new HearthReaderNotification("Hero Detected", "Your hero is " + heroesList.getHeroLabel(i)));
				}
				break;
			}
		}
		
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(this.findImage(readerSettings.opponentHeroScanboxes[i], heroesThumbIT[i], "Opp Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found opp hero: (" + i + ") " + heroesList.getHeroLabel(i));
				oppHero = i;
				inGameMode = 1;
				if(oppHero != exOppHero){
					exOppHero = oppHero;
					isDirty = true;
					
					notifications.add(new HearthReaderNotification("Hero Detected", "Opponent hero is " + heroesList.getHeroLabel(i)));
				}
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
		int totalTime = (int) (new Date().getTime() - startTime.getTime())/1000;
		
		if(victory == 1){
			System.out.println("Found Victory");
			notifications.add(new HearthReaderNotification("Game Result", getMyHero() + " vs " + getOppHero() + ", Victory!"));
		} else if(victory == 0) {
			System.out.println("Found Defeat");
			notifications.add(new HearthReaderNotification("Game Result", getMyHero() + " vs " + getOppHero() + ", Defeat!"));
		} else {
			System.out.println("Found Unknown game result");
			notifications.add(new HearthReaderNotification("Game Result", getMyHero() + " vs " + getOppHero() + ", Result unknown!"));
		}
		
		System.out.println("Saving match result...");
		try {
			tracker.saveMatchResult(gameMode, myHero, oppHero, goFirst, victory, startTime.getTime(), totalTime, false);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		System.out.println("Done saving match result...");
		
		lastSave = new Date();		
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
		
		if(this.findImage(readerSettings.goFirstScanbox, goFirstImageTarget, "Go First")){
			goFirst = 1;
			found = true;
		}
		
		if(!found && this.findImage(readerSettings.goSecondScanbox, goSecondImageTarget, "Go Second")){
			goFirst = 0;
			found = true;
		}
		
		//check are we scanning the "same" scene twice
		if(found && (exGoFirst != goFirst)){
			
			if(goFirst == 1){
				System.out.println("Found coin, go first");
				notifications.add(new HearthReaderNotification("Coin detected", "You go first!"));
			} else if( goFirst == 0){
				System.out.println("Found coin, go second");
				notifications.add(new HearthReaderNotification("Coin detected", "You go second!"));
			}
						
			exGoFirst = goFirst;
			inGameMode = 1;
			startTime = new Date();
			isDirty = true;
		}
		
		return;
	}
	
	public String getOverview(){
		String goes = "Unknown";
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
			arenaWins = 	tracker.getTotalWins(HearthReader.ARENAMODE);
			arenaLosses = 	tracker.getTotalLosses(HearthReader.ARENAMODE);
			arenaWinrate =  (arenaWins + arenaLosses) > 0 ? (float) arenaWins /  (arenaWins + arenaLosses) * 100: -1;
			rankedWins = 	tracker.getTotalWins(HearthReader.RANKEDMODE);
			rankedLosses = 	tracker.getTotalLosses(HearthReader.RANKEDMODE);
			rankedWinrate =  (rankedWins + rankedLosses) > 0 ? (float) rankedWins / (rankedWins + rankedLosses) * 100 : -1;
			unrankedWins = 	tracker.getTotalWins(HearthReader.UNRANKEDMODE);
			unrankedLosses = 	tracker.getTotalLosses(HearthReader.UNRANKEDMODE);
			unrankedWinrate =  (unrankedWins + unrankedLosses) > 0 ? (float) unrankedWins / (unrankedWins + unrankedLosses) * 100 : -1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		String strArena = arenaWinrate > -1 ?  arenaWins + " - " + arenaLosses + " (" + new DecimalFormat("0.00").format(arenaWinrate) + "%) " : "N|A";
		String strRanked = rankedWinrate > -1 ?  rankedWins + " - " + rankedLosses + " (" + new DecimalFormat("0.00").format(rankedWinrate) + "%) " : "N|A";
		String strUnranked = unrankedWinrate > -1 ? unrankedWins + " - " + unrankedLosses + " (" + new DecimalFormat("0.00").format(unrankedWinrate) + "%) " : "N|A";

		if(this.isGoFirst()){
			goes = "first";
		}
		
		if(this.isGoSecond()){
			goes = "second";
		}
		
		//output += "Last seen: " + seen + "\r\n";
		
		if(arenaWinrate > -1){
			output +="Arena: " + strArena  +"\r\n";
		}
		
		if(rankedWinrate > -1){
			output +="Ranked: " + strRanked +"\r\n";
		}
		
		if(unrankedWinrate > -1){
			output +="Unranked: " + strUnranked +"\r\n";
		}

		output +="Game mode: " + this.getGameMode() +"\r\n";
		
		if(this.isArenaMode()){
			String score = this.getArenaWins() > -1 && this.getArenaLosses() > -1 ? this.getArenaWins() + " - " + this.getArenaLosses() : "Unknown";
			
			output +="\r\n";
			output +="Live Arena status" + "\r\n";
			output +="Score: " + score + "\r\n";
			output +="Playing as " + this.getMyHero() + "\r\n";
		}
				
		if(this.isInGame()){
			output +="\r\n";
			output +="Live match status" + "\r\n";
			output += this.getMyHero() + " vs " + this.getOppHero() + ", " + goes + "\r\n";
		}
		
		try {
			ResultSet rs = tracker.getLastMatches(5);
			output += "\r\nLatest match(es): \r\n";
			while(rs.next()){
				
				String as 	= heroesList.getHeroLabel(rs.getInt("MYHEROID"));
				String vs 	= heroesList.getHeroLabel(rs.getInt("OPPHEROID"));
				String first = rs.getInt("GOESFIRST") == 1 ? "(1st) " : "(2nd) ";
				String result = rs.getInt("WIN") == 1 ? "(W) " : "(L) ";
				
				if(rs.getInt("GOESFIRST") == -1){
					first =  "";
				}
				
				if(rs.getInt("WIN") == -1){
					result =  "";
				}
				
				output += as + " vs " + vs + " " + first + result + "\r\n";
			}
			
			rs = tracker.getLastArenaResults(5);

			output +="\r\nLatest Arena: \r\n";
			
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
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				//
			}
			return;
		}
		
		this.scanMode();
		
		if(this.isArenaMode() && !this.isInGame()){
			this.scanArenaHero();
			this.scanArenaScore();
		}
		
		this.scanCoinScreen();
		this.scanGameHeroes();
		this.scanVictory();	
		this.autoPing();
	}
}
	
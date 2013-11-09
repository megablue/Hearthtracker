import java.awt.Color;
import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthReader {
	private static final int UNKNOWNMODE = -1;
	private static final int MENUMODE = 0;
	private static final int ARENAMODE = 1;
	private static final int PLAYMODE = 2;
	private static final int CHALLENGEMODE = 3;
	private static final int PRACTICEMODE = 4;
	boolean debugMode = true;
	boolean inited = false;
	boolean gameLangInited = false;

	int previousWins = -1;
	int wins = -1;
	
	int previousLosses = -1;
	int losses = -1;
	
	int myHero = -1;
	int oppHero = -1;
	
	int victory = -1;
	int goFirst = -1;
	Date startTime = new Date();
	Date lastUpdate = new Date();
	
	static String lastArenaResult = "Unknown";
	static String lastMatchResult = "Unknown";
	
	int gameMode = UNKNOWNMODE;
	int inGameMode = -1;
	
	boolean paused = false;

	Tracker tracker = null;

	Target playImageTarget;
	Target practiceImageTarget;
	Target challengeImageTarget;
	Target questImageTarget;
	Target checkedImageTarget;
	Target lossesLabelImageTarget;
	Target winsLabelImageTarget;
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
	Date lastSeen =  new Date(new Date().getTime() - pingInterval);
	int[] lastScanArea = {0,0,0,0};
	int[] lastScanSubArea = {0,0,0,0};
	Date lastPing = new Date(new Date().getTime() - pingInterval);
	
	public HearthReader(Tracker t){
		debugMode = false;
		tracker = t;
		init();
	}
	
	public HearthReader (Tracker t, String lang, int resX, int resY, boolean autoping, boolean mode){
		debugMode = mode;
		tracker = t;
		oldGameResX = gameResX = resX;
		oldGameResY = gameResY = resY;
		gameLang = lang.toLowerCase();
		setAutoPing(autoping);
		init();
	}
	
	public void pause(){
		paused = true;
		System.out.println("paused");
	}
	
	public void resume(){
		paused = false;
		System.out.println("resumed");
	}
	
	public void setGameLang(String lang){
		gameLang = lang;
		this.initGameScanner();
	}
	
	public void setGameRes(int w, int h){
		gameResX = w;
		gameResY = h;
		this.initGameScanner();
	}
	
	public void setAutoGameRes(boolean flag){
		autoDetectGameRes = flag;
		this.initGameScanner();
	}
	
	private void init(){
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
	
	private void initGameScanner(){
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
		
		for(int i = 0; i < heroesList.getTotal(); i++)
		{
			heroesIT[i] = this.prepareImageTarget(readerSettings.arenaHeroScanboxes[i]);
			heroesThumbIT[i] = this.prepareImageTarget(readerSettings.opponentHeroScanboxes[i]);
		}
		
		questImageTarget 	= this.prepareImageTarget(readerSettings.menuScanbox);
		checkedImageTarget 	= this.prepareImageTarget(readerSettings.lossesScanboxes[0]);
		
		playImageTarget = this.prepareImageTarget(readerSettings.playScanbox);
		challengeImageTarget = this.prepareImageTarget(readerSettings.challengeScanbox);
		practiceImageTarget = this.prepareImageTarget(readerSettings.practiceScanbox);
		
		this.initGameLang();
	}
		
	private void initGameLang(){		
		//language dependent
		winsImageTarget = new ImageTarget[10];
		
		for(int i = 0; i < 10; i++){
			winsImageTarget[i] =  this.prepareImageTarget(readerSettings.winsScanboxes[i]);
		}
		
		lossesLabelImageTarget	= this.prepareImageTarget(readerSettings.lossesLabelScanbox);
		winsLabelImageTarget 	= this.prepareImageTarget(readerSettings.winsLabelScanbox);
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
		
		lastScanArea[0]	= winRect[0];
		lastScanArea[1]	= winRect[1];
		lastScanArea[2]	= gameRes[0];
		lastScanArea[3]	= gameRes[1];
		
		lastScanSubArea[0] = x = (int) (scaling * sb.xOffset) + this.getBoardX();
		lastScanSubArea[1] = y = (int) (scaling * sb.yOffset) + this.getBoardY();
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
			System.out.println("Found quest icon");
			gameMode = MENUMODE;
			inGameMode = 0;
			return;
		}
		
		if(this.findImage(readerSettings.winsLabelScanbox, winsLabelImageTarget, "Wins Label")){
			gameMode = ARENAMODE;
			oppHero = -1;
			inGameMode = 0;
			return;
		}
		
		if(this.findImage(readerSettings.playScanbox, playImageTarget, "Play mode Label")){
			gameMode = PLAYMODE;
			oppHero = -1;
			inGameMode = 0;
			return;
		}
		
		if(this.findImage(readerSettings.playScanbox, challengeImageTarget, "Challenge mode Label")){
			gameMode = CHALLENGEMODE;
			oppHero = -1;
			inGameMode = 0;
			return;
		}
		
		if(this.findImage(readerSettings.playScanbox, practiceImageTarget, "Practice mode Label")){
			gameMode = PRACTICEMODE;
			oppHero = -1;
			inGameMode = 0;
			return;
		}
				
		return;
	}
	
	public int getArenaWins(){
		return wins;
	}
	
	public int getArenaLosses(){
		return losses;
	}
	
	public boolean isArenaMode(){
		return gameMode == 1 ? true : false;
	}
	
	public boolean isPlayMode(){
		return gameMode == 2 ? true : false;
	}
	
	public boolean isChallengeMode(){
		return gameMode == 3 ? true : false;
	}
	
	public boolean isPraticeMode(){
		return gameMode == 4 ? true : false;
	}
	
	private synchronized void scanArenaScore() {
		boolean foundWins = false;
		boolean foundLosses = false;
		
		for(int i = (winsImageTarget.length - 1); i >= 0; i--)
		{
			foundWins = this.findImage(readerSettings.winsScanboxes[i], winsImageTarget[i], "Wins (" + i + ")");
			
			if(foundWins)
			{
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
		
		if(foundWins){
			if(!foundLosses){
				losses = 0;
			}
			this.formatArenaStatus();
			System.out.println("lastArenaResult: " + lastArenaResult);
		}
		
		if(foundWins && (wins == 9 || losses == 3) && (previousWins != wins || previousLosses != losses) ){
			
			if(!foundLosses){
				losses = 0;
			}

			try {
				System.out.println("Saving arena result...");
				tracker.saveArenaResult(myHero, wins, losses);
				System.out.println("Done saving arena result...");
				
				this.formatArenaStatus();
				previousWins = wins;
				previousLosses = losses;
				this.resetFlags();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			previousWins = wins;
			previousLosses = losses;
		}
	}
	
	
	public String getLastArenaResult(){
		return lastArenaResult;
	}
	
	public String getMatchStatus(){
		return lastMatchResult;
	}
	
	public String getMyArenaHero(){
		if(myHero >= 0 ){
			return heroesList.getHeroLabel(myHero);
		}
		
		return "Unknown";
	}
	
	private synchronized void resetFlags(){
		gameMode = -1;
		inGameMode = -1;
		victory = -1;
		myHero = -1;
		wins = -1;
		losses = -1;
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
				break;
			}
		}
		
		return;
	}
	
	private synchronized void scanGameHeroes() {
		if(!this.isInGame() || this.foundGameHero()){
			return;
		}
		
		if(!this.isArenaMode()){
			for(int i = 0; i < heroesThumbIT.length; i++){
				if(this.findImage(readerSettings.myHeroScanboxes[i], heroesThumbIT[i], "My Hero (" + heroesList.getHeroLabel(i) + ") ")){
					System.out.println("Found my hero: (" + i + ") " + heroesList.getHeroLabel(i));
					myHero = i;
					this.formatMatchStatus();
					break;
				}
			}
		}
		
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(this.findImage(readerSettings.opponentHeroScanboxes[i], heroesThumbIT[i], "Opp Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found opp hero: (" + i + ") " + heroesList.getHeroLabel(i));
				oppHero = i;
				this.formatMatchStatus();
				break;
			}
		}

		return;
	}
	
	private synchronized void scanVictory(){
		boolean found = false;
		
		if(!this.isInGame()){
			return;
		}
		
		if(this.findImage(readerSettings.victoryScanbox, victoryImageTarget, "Victory")){
			System.out.println("Found Victory");
			victory = 1;
			found = true;
		}
		
		if(!found && this.findImage(readerSettings.defeatScanbox, defeatImageTarget, "Defeat")){
			System.out.println("Found Defeat");
			victory = 0;
			found = true;
		}
		
		if(found){
			int totalTime = (int) (new Date().getTime() - startTime.getTime())/1000;

			assert goFirst == 1 || goFirst == 0;
			assert victory == 1 || victory == 0;
			
			this.formatMatchStatus();
			
			try {
				System.out.println("Saving match result...");
				tracker.saveMatchResult(myHero, oppHero, goFirst, victory, startTime, totalTime);
				System.out.println("Done saving match result...");
				inGameMode = 0;
				victory = -1;
				oppHero = -1;
				goFirst = -1;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return;
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
		
		lastMatchResult = output;
		tracker.saveLiveMatch(output);
	}
	
	private synchronized void formatArenaStatus(){
		String score = "Unknown";
		String hero = "Unknown";
		
		if(wins != -1){
			if(losses == -1){
				losses = 0;
			}
			
			score = lastArenaResult = wins + " - " + losses;
		}
		
		if(myHero != -1){
			hero = heroesList.getHeroLabel(myHero);
		}
		
		tracker.saveLiveArenaScore(score, hero);
	}
	
	public boolean isVictory(){
		return victory == 1 ? true : false;
	}
	
	private synchronized void scanCoinScreen() {
		if(this.isInGame()){
			return;
		}
		
		if(this.findImage(readerSettings.goFirstScanbox, goFirstImageTarget, "Go First")){
			System.out.println("Found go first");
			goFirst = 1;
			inGameMode = 1;
			startTime = new Date();
			this.formatMatchStatus();
			return;
		}
		
		if(this.findImage(readerSettings.goSecondScanbox, goSecondImageTarget, "Go Second")){
			System.out.println("Found go second");
			goFirst = 0;
			inGameMode = 1;
			startTime = new Date();
			this.formatMatchStatus();
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
		int relativeX = xOffset + (gameRes[0] - this.getBoardWidth()) / 2;
		return relativeX;
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
	
	public void setAutoPing(boolean enabled){
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
		ScreenRegion region = new DesktopScreenRegion(lastScanArea[0],
				lastScanArea[1], 
				lastScanArea[2] - lineWidth / 2, 
				lastScanArea[3] - lineWidth / 2
		);
		
		ScreenRegion subregion = new DesktopScreenRegion(lastScanSubArea[0],
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
		if(paused){
			return;
		}

		if(!this.isInGame()){
			this.scanMode();
		}
		
		if(this.isArenaMode() || this.isPlayMode() || this.isChallengeMode() || this.isPraticeMode()){
			if(!this.isInGame()){
				
				if(this.isArenaMode()){
					this.scanArenaScore();
					this.scanArenaHero();
				}

				this.scanCoinScreen();
			} else {
				this.scanGameHeroes();
				this.scanVictory();
			}
		}
		
		this.autoPing();
	}
}
	
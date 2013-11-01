import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthReader {
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
	
	int arenaMode = -1;
	int inGameMode = -1;
	
	boolean paused = false;

	Tracker tracker = null;

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
	
	public HearthReader(Tracker t){
		debugMode = false;
		tracker = t;
		init();
	}
	
	public HearthReader(Tracker t, String lang, int resX, int resY, boolean mode){
		debugMode = mode;
		tracker = t;
		gameResX = resX;
		gameResY = resY;
		gameLang = lang.toLowerCase();
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
		//do not scale if the height is 1080
		if(gameResY == 1080){
			return 1;
		}
		
		return (gameResY/1080f);
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
		gameLang = sanitizeGameLang(gameLang);
		String pahtGameSettingByResolution = "." + File.separator + "configs" 
											+ File.separator + "gameLangs" 
											+ File.separator + gameLang 
											+ File.separator + gameResX + "x" + gameResY
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

		x = (int) (scaling * sb.xOffset) + this.getBoardX();
		y = (int) (scaling * sb.yOffset) + this.getBoardY();
		w = (int) (sb.width * scaling);
		h = (int) (sb.height * scaling);
		
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
		
		return (foundRegion != null);
	}

	private synchronized void scanArenaScoreScreen() {	
		if(this.findImage(readerSettings.winsLabelScanbox, winsLabelImageTarget, "Wins Label")){
			arenaMode = 1;
			oppHero = -1;
			return;
		}
		
		if(this.findImage(readerSettings.lossesLabelScanbox, lossesLabelImageTarget, "Losses Label")){
			arenaMode = 1;
			oppHero = -1;
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
		return arenaMode == 1 ? true : false;
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
		
		for(int i = 2; i <= 0; i ++){
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
		arenaMode = -1;
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
	
	private synchronized void scanOppHero() {
		if(!this.isInGame() || this.foundOppHero()){
			return;
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
	
	private synchronized void scanMenuScreen(){
		if(this.isInGame()){
			return;
		}
		
		if(this.findImage(readerSettings.menuScanbox, questImageTarget, "Quest icon")){
			System.out.println("Found quest icon");
			arenaMode = 0;
			inGameMode = 0;
			return;
		}
	}
	
	public int getBoardWidth(){
		return (gameResY/3) * 4;
	}
	
	public int getBoardHeight(){
		return gameResY;
	}
		
	public int getBoardX(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int xOffset = winPos[0];
		int relativeX = xOffset + (gameResX - this.getBoardWidth()) / 2;
		return relativeX;
	}
	
	public int getBoardY(){
		int[] winPos = HearthHelper.getHearthstonePosition();
		int yOffset = winPos[1];
		return yOffset;
	}
	
	public void pingBox(int x, int y, int w, int h){
		Canvas canvas = new DesktopCanvas();
		ScreenRegion region = new DesktopScreenRegion(x, y, w, h);
		canvas.addBox(region);
		canvas.addLabel(region, x + ", " + y + ", w: " + w + ", h: " + h).display(5);
		System.out.println(x + ", " + y + ", w: " + w + ", h: " + h);
	}
	
	public boolean foundOppHero(){
		return oppHero > - 1  ? true : false;
	}
	
	public boolean isGoFirst(){
		return goFirst == 1 ? true : false;
	}
	
	public boolean isInGame() {
		return inGameMode == 1 ? true : false;
	}
	
	public void process(){
		if(paused){
			return;
		}

		if(!this.isInGame()){
			this.scanMenuScreen();
			this.scanArenaScoreScreen();
		}
		
		if(this.isArenaMode() && !this.isInGame()){
			this.scanArenaScore();
			this.scanArenaHero();
		}
		
		if(this.isArenaMode() && !this.isInGame()){
			this.scanCoinScreen();
		}
		
		if(this.isArenaMode() && this.isInGame()){
			this.scanOppHero();
			this.scanVictory();
		}
	}
}
	
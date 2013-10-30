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

	ImageTarget questImageTarget;
	ImageTarget checkedImageTarget;
	ImageTarget lossesLabelImageTarget;
	ImageTarget winsLabelImageTarget;
	ImageTarget goFirstImageTarget;
	ImageTarget goSecondImageTarget;
	ImageTarget victoryImageTarget;
	ImageTarget defeatImageTarget;

	ImageTarget[] winsImageTarget;
	
	HearthGameLangList gameLanguages;
	String gameLang;
		
	ImageTarget[] heroesIT;
	ImageTarget[] heroesThumbIT;
	
	HearthReaderSetting readerSettings = null;
	
	private HearthConfigurator config = new HearthConfigurator();
	private HearthHeroesList heroesList;
	
	public HearthReader(Tracker t){
		debugMode = false;
		tracker = t;
		init();
	}
	
	public HearthReader(Tracker t, String lang, boolean mode){
		debugMode = mode;
		tracker = t;
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
		this.initGameLang();
	}
	
	private void init(){
		if(inited){
			return;
		}
		
		heroesList = (HearthHeroesList) config.load("./configs/heroes.xml");
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, "./configs/heroes.xml");
		}
		
		gameLanguages = (HearthGameLangList) config.load("./configs/gameLangs.xml");
		
		if(gameLanguages == null){
			gameLanguages = new HearthGameLangList();
			config.save(gameLanguages, "./configs/gameLangs.xml");
		}
		
		heroesIT = new ImageTarget[heroesList.getTotal()];
		heroesThumbIT = new ImageTarget[heroesList.getTotal()];
		
		for(int i = 0; i < heroesList.getTotal(); i++)
		{
			heroesIT[i] = new ImageTarget(new File("./images/" + heroesList.getHeroName(i) + ".png"));
			heroesThumbIT[i] = new ImageTarget(new File("./images/" + heroesList.getHeroName(i) + "-s.png"));
		}
		
		questImageTarget = new ImageTarget(new File("./images/quest.png"));
		checkedImageTarget = new ImageTarget(new File("./images/lose-checkbox-checked.png"));
		
		inited = true;
		
		this.initGameLang();
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
	
	private void initGameLang(){
		gameLang = sanitizeGameLang(gameLang);
		
		readerSettings = (HearthReaderSetting) config.load("./configs/gameLangs/" + gameLang + ".xml");
		
		if(readerSettings == null){
			readerSettings = new HearthReaderSetting();
			config.save(readerSettings, "./configs/gameLangs/" + gameLang + ".xml");
		}
		
		//language dependent
		winsImageTarget = new ImageTarget[10];
		
		for(int i = 0; i < 10; i++){
			ImageTarget itarget; 
			
			if(readerSettings.winsNumberScaleFactor == 1){
				itarget = new ImageTarget(new File("./images/" + i + ".png"));
			}else{
				itarget = new ImageTarget(HearthHelper.resizeImage(new File("./images/" + i + ".png"), readerSettings.winsNumberScaleFactor));
			}
			
			winsImageTarget[i] = itarget;
			winsImageTarget[i].setMinScore(readerSettings.winsNumberMatchQuality);
		}
		
		lossesLabelImageTarget = new ImageTarget(new File("./images/" + gameLang + "/losses-label.png"));
		winsLabelImageTarget = new ImageTarget(new File("./images/" + gameLang + "/wins-label.png"));
		goFirstImageTarget = new ImageTarget(new File("./images/" + gameLang + "/go-first.png"));
		goSecondImageTarget = new ImageTarget(new File("./images/" + gameLang + "/go-second.png"));
		victoryImageTarget = new ImageTarget(new File("./images/" + gameLang + "/victory.png"));
		defeatImageTarget = new ImageTarget(new File("./images/" + gameLang + "/defeat.png"));
		
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
	
	@SuppressWarnings("unused")
	private boolean findImage(ScreenRegion region, ImageTarget target){
		return this.findImage(region, target, "");
	}
	
	private boolean findImage(ScreenRegion region, ImageTarget target, String label){
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

	private synchronized void scanArenaScoreScreen() {
		ScreenRegion winsLabelRegion = new DesktopScreenRegion(720,460,140,80);
		ScreenRegion lossesLabelRegion = new DesktopScreenRegion(520,540,140,80);
		
		if(this.findImage(winsLabelRegion, winsLabelImageTarget, "Wins Label")){
			arenaMode = 1;
			oppHero = -1;
			return;
		}
		
		if(this.findImage(lossesLabelRegion, lossesLabelImageTarget, "Losses Label")){
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
		ScreenRegion winsSRegion = new DesktopScreenRegion(740,360,110,100);
		ScreenRegion lossesSRegion3 = new DesktopScreenRegion(840,530,80,80);
		ScreenRegion lossesSRegion2 = new DesktopScreenRegion(750,530,80,80);
		ScreenRegion lossesSRegion1 = new DesktopScreenRegion(660,530,80,80);
		boolean foundWins = false;
		boolean foundLosses = false;
		
		for(int i = (winsImageTarget.length - 1); i >= 0; i--)
		{
			foundWins = this.findImage(winsSRegion, winsImageTarget[i], "Wins (" + i + ")");
			
			if(foundWins)
			{
				System.out.println("Found " + i + " wins" );
				wins = i;
				break;
			}
		}
		
		if(this.findImage(lossesSRegion3, checkedImageTarget, "Losses (3)")){
			System.out.println("Found 3 losses");
			foundLosses = true;
			losses = 3;
		}
		
		if(!foundLosses && this.findImage(lossesSRegion2, checkedImageTarget, "Losses (2)")){
			System.out.println("Found 2 losses");
			foundLosses = true;
			losses = 2;
		}
		
		if(!foundLosses && this.findImage(lossesSRegion1, checkedImageTarget, "Losses (1)")){
			System.out.println("Found 1 losses");
			foundLosses = true;
			losses = 1;
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
	
	private synchronized void scanMyHero() {
		ScreenRegion heroSRegion = new DesktopScreenRegion(340,730,220,120);
		
		if(this.isInGame() || !this.isArenaMode()){
			return;
		}
		
		for(int i = 0; i < heroesIT.length; i++){
			if(this.findImage(heroSRegion, heroesIT[i], "My Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found hero: " + heroesList.getHeroLabel(i));
				myHero = i;
				break;
			}
		}
		
		return;
	}
	
	private synchronized void scanOppHero() {
		ScreenRegion heroSRegion = new DesktopScreenRegion(850,70,220,200);
		
		if(!this.isInGame() || this.foundOppHero()){
			return;
		}
		
		for(int i = 0; i < heroesThumbIT.length; i++){
			if(this.findImage(heroSRegion, heroesThumbIT[i], "Opp Hero (" + heroesList.getHeroLabel(i) + ") ")){
				System.out.println("Found Opp hero: " + heroesList.getHeroLabel(i));
				oppHero = i;
				this.formatMatchStatus();
				break;
			}
		}
		
		return;
	}
	
	private synchronized void scanVictory(){
		ScreenRegion victoryRegion = new DesktopScreenRegion(750,550,400,150);
		boolean found = false;
		
		if(!this.isInGame()){
			return;
		}
		
		if(this.findImage(victoryRegion, victoryImageTarget, "Victory")){
			System.out.println("Found Victory");
			victory = 1;
			found = true;
		}
		
		if(!found && this.findImage(victoryRegion, defeatImageTarget, "Defeat")){
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
		ScreenRegion coinRegion = new DesktopScreenRegion(1150,550,400,150);
		
		if(this.isInGame()){
			return;
		}
		
		if(this.findImage(coinRegion, goFirstImageTarget, "Go First")){
			System.out.println("Found go first");
			goFirst = 1;
			inGameMode = 1;
			startTime = new Date();
			this.formatMatchStatus();
			return;
		}
		
		if(this.findImage(coinRegion, goSecondImageTarget, "Go Second")){
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
		ScreenRegion coinRegion = new DesktopScreenRegion(430, 850, 200, 200);
		
		if(this.isInGame()){
			return;
		}
		
		if(this.findImage(coinRegion, questImageTarget, "Quest icon")){
			System.out.println("Found quest icon");
			arenaMode = 0;
			inGameMode = 0;
			return;
		}
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
			this.scanArenaScoreScreen();
			this.scanMenuScreen();
		}
		
		if(this.isArenaMode() && !this.isInGame()){
			this.scanArenaScore();
			this.scanMyHero();
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
	
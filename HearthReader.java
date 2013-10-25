import java.io.File;
import java.sql.SQLException;
import java.util.Date;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthReader {
	boolean debugMode = true;

	int wins = -1;
	int losses = -1;
	
	int myHero = -1;
	int oppHero = -1;
	
	int victory = -1;
	int goFirst = -1;
	Date startTime = new Date();
	Date lastUpdate = new Date();
	
	static String lastArenaResult = "";
	static String lastMatchResult = "";
	
	int arenaMode = -1;
	int inGameMode = -1;
	
	boolean paused = false;

	Tracker tracker = null;
	
	static ImageTarget checkedImageTarget = new ImageTarget(new File(".\\images\\lose-checkbox-checked.png"));
	static ImageTarget lossesLabelImageTarget = new ImageTarget(new File(".\\images\\losses-label.png"));
	static ImageTarget winsLabelImageTarget = new ImageTarget(new File(".\\images\\wins-label.png"));
	static ImageTarget goFirstImageTarget = new ImageTarget(new File(".\\images\\go-first.png"));
	static ImageTarget goSecondImageTarget = new ImageTarget(new File(".\\images\\go-second.png"));
	static ImageTarget victoryImageTarget = new ImageTarget(new File(".\\images\\victory.png"));
	static ImageTarget defeatImageTarget = new ImageTarget(new File(".\\images\\defeat.png"));

	static ImageTarget[] winsIT = {	
		new ImageTarget(new File(".\\images\\0.png")), 
		new ImageTarget(new File(".\\images\\1.png")),
		new ImageTarget(new File(".\\images\\2.png")),
		new ImageTarget(new File(".\\images\\3.png")),
		new ImageTarget(new File(".\\images\\4.png")),
		new ImageTarget(new File(".\\images\\5.png")),
		new ImageTarget(new File(".\\images\\6.png")),
		new ImageTarget(new File(".\\images\\7.png")),
		new ImageTarget(new File(".\\images\\8.png")),
		new ImageTarget(new File(".\\images\\9.png"))
	};
	
	String[] heroesLabel = {
		"mage",
		"hunter",
		"warrior",
		"shaman",
		"druid",
		"priest",
		"rogue",
		"paladin",
		"warlock"
	};
		
	ImageTarget[] heroesIT;
	ImageTarget[] heroesThumbIT;
	
	public HearthReader(Tracker t){
		debugMode = false;
		tracker = t;
		init();
	}
	
	public HearthReader(Tracker t, boolean mode){
		debugMode = mode;
		tracker = t;
		init();
	}
	
	public void pause(){
		paused = true;
		//System.out.println("paused");
	}
	
	public void resume(){
		paused = false;
		//System.out.println("resumed");
	}
	
	private void init(){
		heroesIT = new ImageTarget[heroesLabel.length];
		heroesThumbIT = new ImageTarget[heroesLabel.length];
		
		for(int i = 0; i < heroesLabel.length; i++)
		{
			heroesIT[i] = new ImageTarget(new File(".\\images\\" + heroesLabel[i] + ".png"));
			heroesThumbIT[i] = new ImageTarget(new File(".\\images\\" + heroesLabel[i] + "-s.png"));
		}
	
		for(int i = (winsIT.length - 1); i >= 0; i--)
		{
			winsIT[i].setMinScore(0.9);
		}
	}
	
	public String getHeroLabel(int heroID){
		
		if(heroID >= 0 && heroID < heroesLabel.length){
			return heroesLabel[heroID];
		}
		
		return "?????";
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
		
		for(int i = (winsIT.length - 1); i >= 0; i--)
		{
			foundWins = this.findImage(winsSRegion, winsIT[i], "Wins (" + i + ")");
			
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
			
			lastArenaResult = wins + " - " + losses;
			System.out.println("lastArenaResult: " + lastArenaResult);
		}
		
		if(foundWins && (wins == 9 || losses == 3)){
			
			if(!foundLosses){
				losses = 0;
			}
			
			try {
				System.out.println("Saving arena result...");
				tracker.saveArenaResult(myHero, wins, losses);
				System.out.println("Done saving arena result...");
				lastArenaResult = wins + " - " + losses;
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
			return this.getHeroLabel(myHero);
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
			if(this.findImage(heroSRegion, heroesIT[i], "My Hero (" + heroesLabel[i] + ") ")){
				System.out.println("Found hero: " + heroesLabel[i]);
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
			if(this.findImage(heroSRegion, heroesThumbIT[i], "Opp Hero (" + heroesLabel[i] + ") ")){
				System.out.println("Found Opp hero: " + heroesLabel[i]);
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

			this.formatMatchStatus();
			
			assert goFirst == 1 || goFirst == 0;
			assert victory == 1 || victory == 0;
			
			try {
				System.out.println("Saving match result...");
				tracker.saveMatchResult(myHero, oppHero, goFirst, victory, startTime, totalTime);
				System.out.println("Done saving match result...");
				inGameMode = 0;
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return;
	}
	
	private synchronized void formatMatchStatus(){
		String tmp = goFirst == 1 ? "goes first" : "goes second";
		String tmp2 = victory == 1 ? ", 1 - 0 " : ", 0 - 1 ";
		String output = "Unknown";
		
		if(myHero == -1){
			return;
		}
		
		if(goFirst != -1){
			output = heroesLabel[myHero] + " (goes first)";
		}
		
		if(goFirst != -1 && oppHero != -1){
			output = heroesLabel[myHero] + " (" + tmp + ")" + " vs " + heroesLabel[oppHero];
		}
		
		if(goFirst != -1 && oppHero != -1 && victory != -1){
			output = heroesLabel[myHero] + " (" + tmp + ")" + " vs " + heroesLabel[oppHero] + tmp2;
		}
		
		lastMatchResult = output;
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
	
	public boolean foundOppHero(){
		return oppHero > - 1  ? true : false;
	}
	
	public boolean isGoFirst(){
		return goFirst == 1 ? true : false;
	}
	
	public boolean isInGame() {
		return inGameMode == 1 ? true : false;
	}
	
	public String[] getHeroes(){
		return heroesLabel;
	}
	
	public void process(){	
		if(paused){
			return;
		}
		
		System.out.println("running...");

		if(!this.isInGame()){
			this.scanArenaScoreScreen();
		}
		
		if(this.isArenaMode() && !this.isInGame()){
			this.scanArenaScore();
			this.scanMyHero();
		}
		
		if(!this.isInGame()){
			this.scanCoinScreen();
		}
		
		if(this.isArenaMode() && this.isInGame()){
			this.scanOppHero();
			this.scanVictory();
		}
	}
}
	
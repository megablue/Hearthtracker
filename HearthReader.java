import java.io.File;

import org.sikuli.api.*;
import org.sikuli.api.visual.Canvas;
import org.sikuli.api.visual.DesktopCanvas;

public class HearthReader {
	boolean debugMode = true;
	int changed_since = 0;
	String lastRunSocre = "";
	
	static ImageTarget checkedImageTarget = new ImageTarget(new File(".\\images\\lose-checkbox-checked.png"));
	static ImageTarget lossesLabelImageTarget = new ImageTarget(new File(".\\images\\losses-label.png"));
	static ImageTarget winsLabelImageTarget = new ImageTarget(new File(".\\images\\wins-label.png"));

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
	
	static String[] heroesLabel = {
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
		
	static ImageTarget[] heroesIT = {	
		new ImageTarget(new File(".\\images\\" + heroesLabel[0] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[1] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[2] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[3] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[4] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[5] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[6] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[7] + ".png")), 
		new ImageTarget(new File(".\\images\\" + heroesLabel[8] + ".png"))
	};
	
	public HearthReader(){
		debugMode = false;
		init();
	}
	
	public HearthReader(boolean mode){
		debugMode = mode;
		init();
	}
	
	private void init(){
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

	public boolean isScoreScreen() {
		ScreenRegion winsLabelRegion = new DesktopScreenRegion(720,460,140,80);
		ScreenRegion lossesLabelRegion = new DesktopScreenRegion(520,540,140,80);
		
		if(this.findImage(winsLabelRegion, winsLabelImageTarget, "Wins Label")){
			return true;
		}
		
		if(this.findImage(lossesLabelRegion, lossesLabelImageTarget, "Losses Label")){
			return true;
		}
				
		return false;
	}
	
	public int getWins() {
		ScreenRegion winsSRegion = new DesktopScreenRegion(740,360,110,100);
		boolean foundWins = false;
		
		for(int i = (winsIT.length - 1); i >= 0; i--)
		{
			foundWins = this.findImage(winsSRegion, winsIT[i], "Wins (" + i + ")");
			
			if(foundWins)
			{
				System.out.println("Found " + i + " wins" );
				return i;
			}
		}
		
		return -1;
	}
	
	public int getLosses() {
		ScreenRegion lossesSRegion3 = new DesktopScreenRegion(840,530,80,80);
		ScreenRegion lossesSRegion2 = new DesktopScreenRegion(750,530,80,80);
		ScreenRegion lossesSRegion1 = new DesktopScreenRegion(660,530,80,80);

		if(this.findImage(lossesSRegion3, checkedImageTarget, "Losses (3)")){
			System.out.println("Found 3 losses");
			return 3;
		}
		
		if(this.findImage(lossesSRegion2, checkedImageTarget, "Losses (2)")){
			System.out.println("Found 2 losses");
			return 2;
		}
		
		if(this.findImage(lossesSRegion1, checkedImageTarget, "Losses (1)")){
			System.out.println("Found 1 losses");
			return 1;
		}
		
		return 0;
	}
	
	public int getHero() {
		ScreenRegion heroSRegion = new DesktopScreenRegion(340,730,220,120);
		
		for(int i = 0; i < heroesIT.length; i++)
		{
			if(this.findImage(heroSRegion, heroesIT[i], "Hero (" + heroesLabel[i] + ") "))
			{
				System.out.println("Found hero: " + heroesLabel[i]);
				return i;
			}
		}
		
		return -1;
	}
}
	
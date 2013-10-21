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
	}
	
	public HearthReader(boolean mode){
		debugMode = mode;
	}
	
	public String getHeroLabel(int heroID){
		
		if(heroID >= 0 && heroID < heroesLabel.length){
			return heroesLabel[heroID];
		}
		
		return "?????";
	}

	public boolean isScoreScreen() {
		ScreenRegion winsLabelRegion = new DesktopScreenRegion(720,460,140,80);
		ScreenRegion lossesLabelRegion = new DesktopScreenRegion(520,540,140,80);
		Canvas canvas = new DesktopCanvas();
		ScreenRegion found;
		
		if(debugMode)
		{
			canvas.addBox(winsLabelRegion);
			canvas.addLabel(winsLabelRegion, "Wins Label").display(1);
		}
		
		found = winsLabelRegion.find(winsLabelImageTarget);
		
		if(found != null){
			if(debugMode){
				canvas.addBox(winsLabelRegion);
				canvas.addLabel(winsLabelRegion, "Found Wins label").display(1);
			}
			
			return true;
		}
		
		if(debugMode)
		{
			canvas.addBox(lossesLabelRegion);
			canvas.addLabel(lossesLabelRegion, "Losses Label").display(1);
		}	
		
		found = lossesLabelRegion.find(lossesLabelImageTarget);
		
		if(found != null){
			if(debugMode){
				canvas.addBox(lossesLabelRegion);
				canvas.addLabel(lossesLabelRegion, "Found Losses label").display(1);
			}
			
			return true;
		}
		
		return false;
	}
	
	public int getWins() {
		ScreenRegion winsSRegion = new DesktopScreenRegion(740,360,110,100);
		Canvas canvas = new DesktopCanvas();
		ScreenRegion foundWins;
		int i = 0;
		
		if(debugMode)
		{
			canvas.addBox(winsSRegion);
			canvas.addLabel(winsSRegion, "Wins counter").display(1);
		}
		
		for(i = 9; i >= 0; i--)
		{
			winsIT[i].setMinScore(0.9);
			foundWins = winsSRegion.find(winsIT[i]);
			
			if(foundWins != null)
			{
				if(debugMode){
					canvas.addBox(winsSRegion);
					canvas.addLabel(winsSRegion, "Found " + i + " wins").display(1);
				}

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
		Canvas canvas = new DesktopCanvas();
		ScreenRegion foundChecked;
		
		if(debugMode)
		{
			canvas.addBox(lossesSRegion3);
			canvas.addLabel(lossesSRegion3, "Region3").display(1);
			
			canvas.addBox(lossesSRegion2);
			canvas.addLabel(lossesSRegion2, "Region2").display(1);

			canvas.addBox(lossesSRegion1);
			canvas.addLabel(lossesSRegion1, "Region1").display(1);
		}
		
		foundChecked = lossesSRegion3.find(checkedImageTarget);

		if(foundChecked != null){
			if(debugMode)
			{
				canvas.addBox(lossesSRegion3);
				canvas.addLabel(lossesSRegion3, "Found 3").display(3);
			}
			System.out.println("Found 3 losses");
			return 3;
		}
		
		foundChecked = lossesSRegion2.find(checkedImageTarget);
		
		if(foundChecked != null){
			if(debugMode)
			{
				canvas.addBox(lossesSRegion2);
				canvas.addLabel(lossesSRegion2, "Found 2").display(3);
			}
			System.out.println("Found 2 losses");
			return 2;
		}
		
		foundChecked = lossesSRegion1.find(checkedImageTarget);
		
		if(foundChecked != null){
			if(debugMode)
			{
				canvas.addBox(lossesSRegion1);
				canvas.addLabel(lossesSRegion1, "Found 1").display(3);
			}
			System.out.println("Found 1 losses");
			return 1;
		}
		
		return 0;
	}
	
	public int getHero() {
		ScreenRegion heroSRegion = new DesktopScreenRegion(340,730,220,120);
		Canvas canvas = new DesktopCanvas();
		ScreenRegion foundHero;
		int i = 0;
		
		if(debugMode)
		{
			canvas.addBox(heroSRegion);
			canvas.addLabel(heroSRegion, "Hero region").display(1);
		}
		
		for(i = 0; i < heroesIT.length; i++)
		{
			foundHero = heroSRegion.find(heroesIT[i]);
			
			if(foundHero != null)
			{
				System.out.println("Found hero: " + heroesLabel[i]);
				
				if(debugMode)
				{
					canvas.addBox(heroSRegion);
					canvas.addLabel(heroSRegion, "Found " + heroesLabel[i]).display(1);
				}
				
				return i;
			}
		}
		
		return -1;
	}
}
	
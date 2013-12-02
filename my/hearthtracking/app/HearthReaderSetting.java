package my.hearthtracking.app;

public class HearthReaderSetting {
	public String lang = "enUS";
	public int baseResolutionHeight		= 1080;
	public boolean overwriteAutoScale	= false;
	public float autoScaleFactor		= 1f;
	
	public Scanbox menuScanbox 			= new Scanbox("quest.png", 			190, 850, 200, 200);
	
	//Arena related scan boxes
	public Scanbox winsLabelScanbox		= new Scanbox("wins-label.png", 	480, 440, 140, 140);
	public Scanbox lossesLabelScanbox	= new Scanbox("losses-label.png", 	250, 510, 160, 150);
	
	//Ranked mode scan boxes
	public Scanbox rankedScanbox		= new Scanbox("ranked-mode.png", 	1200, 100, 120, 120);

	//Play mode scan boxes
	public Scanbox unrankedScanbox		= new Scanbox("unranked-mode.png", 	1200, 100, 120, 120);

	//practice mode scan boxes
	public Scanbox practiceScanbox		= new Scanbox("practice-mode.png", 	1200, 100, 120, 120);
	
	//challenge mode scan boxes
	public Scanbox challengeScanbox		= new Scanbox("challenge-mode.png", 1200, 100, 120, 120);
	
	public Scanbox goFirstScanbox 		= new Scanbox("go-first.png", 		910, 550, 400, 150);
	public Scanbox goSecondScanbox 		= new Scanbox("go-second.png", 		910, 550, 400, 150);
	public Scanbox victoryScanbox 		= new Scanbox("victory.png", 		510, 550, 400, 150);
	public Scanbox defeatScanbox 		= new Scanbox("defeat.png", 		510, 550, 400, 150);
	
	public Scanbox[] winsScanboxes	= {
			new Scanbox("0.png",		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("1.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("2.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("3.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("4.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("5.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("6.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("7.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("8.png", 		300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("9.png", 		300, 190, 200, 200, 1f, 0.9f),
	};
	
	public Scanbox[] lossesScanboxes  = { 
			new Scanbox("lose-checkbox-checked.png", 	400, 507, 110, 110),
			new Scanbox("lose-checkbox-checked.png", 	492, 507, 110, 110),
			new Scanbox("lose-checkbox-checked.png", 	584, 507, 110, 110),
	};
	
	public Scanbox[] lossesUncheckedScanboxes  = { 
			new Scanbox("lose-checkbox-unchecked.png", 	400, 507, 110, 110),
			new Scanbox("lose-checkbox-unchecked.png", 	492, 507, 110, 110),
			new Scanbox("lose-checkbox-unchecked.png", 	584, 507, 110, 110),
	};
	
	public Scanbox[] arenaHeroScanboxes	= {
			new Scanbox("mage.png", 	80, 720, 240, 160),
			new Scanbox("hunter.png", 	80, 720, 240, 160),
			new Scanbox("warrior.png", 	80, 720, 240, 160),
			new Scanbox("shaman.png", 	80, 720, 240, 160),
			new Scanbox("druid.png", 	80, 720, 240, 160),
			new Scanbox("priest.png", 	80, 720, 240, 160),
			new Scanbox("rogue.png", 	80, 720, 240, 160),
			new Scanbox("paladin.png", 	80, 720, 240, 160),
			new Scanbox("warlock.png", 	80, 720, 240, 160),
	};
	
	public Scanbox[] opponentHeroScanboxes	= {
			new Scanbox("mage-s.png",		610, 70, 220, 200),
			new Scanbox("hunter-s.png", 	610, 70, 220, 200),
			new Scanbox("warrior-s.png", 	610, 70, 220, 200),
			new Scanbox("shaman-s.png", 	610, 70, 220, 200),
			new Scanbox("druid-s.png", 		610, 70, 220, 200),
			new Scanbox("priest-s.png", 	610, 70, 220, 200),
			new Scanbox("rogue-s.png", 		610, 70, 220, 200),
			new Scanbox("paladin-s.png", 	610, 70, 220, 200),
			new Scanbox("warlock-s.png", 	610, 70, 220, 200),
	};
	
	public Scanbox[] myHeroScanboxes	= {
			new Scanbox("mage-s.png",		610, 710, 220, 200),
			new Scanbox("hunter-s.png", 	610, 710, 220, 200),
			new Scanbox("warrior-s.png", 	610, 710, 220, 200),
			new Scanbox("shaman-s.png", 	610, 710, 220, 200),
			new Scanbox("druid-s.png", 		610, 710, 220, 200),
			new Scanbox("priest-s.png", 	610, 710, 220, 200),
			new Scanbox("rogue-s.png", 		610, 710, 220, 200),
			new Scanbox("paladin-s.png", 	610, 710, 220, 200),
			new Scanbox("warlock-s.png", 	610, 710, 220, 200),
	};
	
	public class Scanbox{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		String imgfile = null;
		float scale = 1f;
		float matchQuality = -1;

		public Scanbox(String filename, int x, int y, int w, int h){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
		}
		
		public Scanbox(String filename, int x, int y, int w, int h, float scaling, float quality){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scale = scaling;
			matchQuality = quality;
		}
	}
}

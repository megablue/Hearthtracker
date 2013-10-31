
public class HearthReaderSetting {
	public String lang = "enUS";
	public Scanbox menuScanbox 			= new Scanbox("quest.png", 			190, 850, 200, 200);
	
	//Arena related scan boxes
	public Scanbox winsLabelScanbox		= new Scanbox("wins-label.png", 	480, 460, 140, 80);
	public Scanbox lossesLabelScanbox	= new Scanbox("losses-label.png", 	480, 460, 140, 80);
	public Scanbox goFirstScanbox 		= new Scanbox("go-first.png", 		910, 550, 400, 150);
	public Scanbox goSecondScanbox 		= new Scanbox("go-second.png", 		910, 550, 400, 150);
	public Scanbox victoryScanbox 		= new Scanbox("victory.png", 		510, 550, 400, 150);
	public Scanbox defeatScanbox 		= new Scanbox("defeat.png", 		510, 550, 400, 150);
	
	public Scanbox[] winsScanboxes	= {
			new Scanbox("0.png",		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("1.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("2.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("3.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("4.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("5.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("6.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("7.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("8.png", 		320, 210, 150, 150, 1f, 0.9f),
			new Scanbox("9.png", 		320, 210, 150, 150, 1f, 0.9f),
	};
	
	public Scanbox[] lossesScanboxes  = { 
			new Scanbox("lose-checkbox-checked.png", 	420, 530, 80, 80),
			new Scanbox("lose-checkbox-checked.png", 	510, 530, 80, 80),
			new Scanbox("lose-checkbox-checked.png", 	600, 530, 80, 80),
	};
	
	public Scanbox[] arenaHeroScanboxes	= {
			new Scanbox("mage.png", 	100, 730, 220, 120),
			new Scanbox("hunter.png", 	100, 730, 220, 120),
			new Scanbox("warrior.png", 	100, 730, 220, 120),
			new Scanbox("shaman.png", 	100, 730, 220, 120),
			new Scanbox("druid.png", 	100, 730, 220, 120),
			new Scanbox("priest.png", 	100, 730, 220, 120),
			new Scanbox("rogue.png", 	100, 730, 220, 120),
			new Scanbox("paladin.png", 	100, 730, 220, 120),
			new Scanbox("warlock.png", 	100, 730, 220, 120),
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

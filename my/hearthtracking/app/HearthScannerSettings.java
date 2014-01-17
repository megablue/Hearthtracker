package my.hearthtracking.app;

public class HearthScannerSettings {
	public String lang = "enUS";
	public int baseResolutionHeight		= 1080;
	public boolean overwriteAutoScale	= false;
	public float autoScaleFactor		= 1f;
	
	//Arena related scan boxes
	public Scanbox arenaLeafScanbox		= new Scanbox("arena-leaf.png", 	"gameMode", 435, 340, 100, 100);
	
	//Ranked mode scan boxes
	public Scanbox rankedScanbox		= new Scanbox("ranked-mode.png",	"gameMode", 1080, 134, 200, 200);

	//Play mode scan boxes
	public Scanbox unrankedScanbox		= new Scanbox("unranked-mode.png",	"gameMode", 1070, 138, 100, 100);

	//practice mode scan boxes
	public Scanbox practiceScanbox		= new Scanbox("practice-mode.png",	"gameMode", 985, 50, 80, 80);
	
	//challenge mode scan boxes
	public Scanbox challengeScanbox		= new Scanbox("challenge-mode.png",	"gameMode", 1200, 100, 120, 120);
	
	public Scanbox[] deckScanboxses = {
			new Scanbox("deck-selected.png", "deckSelection", 130, 220, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 370, 220, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 610, 220, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 130, 445, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 370, 445, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 610, 445, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 130, 667, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 370, 667, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
			new Scanbox("deck-selected.png", "deckSelection", 610, 667, 55, 55, 1f, 0.7f)
				.addNested(
					new Scanbox("deck-selected-verify.png", "deckSelection", -22, 23, 245, 108, 1f, 0.6f)
					.addMask(new Mask(22, 20, 204, 64))
				),
	};

	public Scanbox goFirstScanbox 		= new Scanbox("go-first.png",	"mulligan", 910, 550, 400, 150);
	public Scanbox goSecondScanbox 		= new Scanbox("go-second.png",	"mulligan", 910, 550, 400, 150);
	public Scanbox victoryScanbox 		= new Scanbox("victory.png",	"mulligan",	440, 500, 100, 100);
	public Scanbox defeatScanbox 		= new Scanbox("defeat.png",		"mulligan",	452, 547, 100, 100);
	
	public Scanbox[] winsScanboxes	= {
			new Scanbox("0.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("1.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("2.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("3.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("4.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("5.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("6.png",	"arenaScore", 	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("7.png",	"arenaScore", 	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("8.png",	"arenaScore",  	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("9.png",	"arenaScore",	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("10.png",	"arenaScore",  	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("11.png",	"arenaScore",  	300, 190, 200, 200, 1f, 0.9f),
			new Scanbox("12.png",	"arenaScore",  	300, 190, 200, 200, 1f, 0.9f),
	};
	
	public Scanbox[] lossesScanboxes  = { 
			new Scanbox("lose-checkbox-checked.png", 	"arenaScore", 400, 507, 110, 110),
			new Scanbox("lose-checkbox-checked.png", 	"arenaScore", 492, 507, 110, 110),
			new Scanbox("lose-checkbox-checked.png", 	"arenaScore", 584, 507, 110, 110),
	};
	
	public Scanbox[] lossesUncheckedScanboxes  = { 
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaScore", 400, 507, 110, 110),
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaScore", 492, 507, 110, 110),
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaScore", 584, 507, 110, 110),
	};
	
	public Scanbox[] arenaHeroScanboxes	= {
			new Scanbox("mage.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("hunter.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("warrior.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("shaman.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("druid.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("priest.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("rogue.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("paladin.png", 	"arenaScore",	80, 720, 240, 160),
			new Scanbox("warlock.png", 	"arenaScore",	80, 720, 240, 160),
	};
	
	public Scanbox[] opponentHeroScanboxes	= {
			new Scanbox("mage-s.png",		"gameStarted", 610, 70, 220, 200),
			new Scanbox("hunter-s.png", 	"gameStarted", 610, 70, 220, 200),
			new Scanbox("warrior-s.png", 	"gameStarted", 610, 70, 220, 200),
			new Scanbox("shaman-s.png", 	"gameStarted", 610, 70, 220, 200),
			new Scanbox("druid-s.png", 		"gameStarted", 610, 70, 220, 200),
			new Scanbox("priest-s.png", 	"gameStarted", 610, 70, 220, 200),
			new Scanbox("rogue-s.png", 		"gameStarted", 610, 70, 220, 200),
			new Scanbox("paladin-s.png", 	"gameStarted", 610, 70, 220, 200),
			new Scanbox("warlock-s.png", 	"gameStarted", 610, 70, 220, 200),
	};
	
	public Scanbox[] myHeroScanboxes	= {
			new Scanbox("mage-s.png",		"gameStarted", 610, 710, 220, 200),
			new Scanbox("hunter-s.png", 	"gameStarted", 610, 710, 220, 200),
			new Scanbox("warrior-s.png", 	"gameStarted", 610, 710, 220, 200),
			new Scanbox("shaman-s.png", 	"gameStarted", 610, 710, 220, 200),
			new Scanbox("druid-s.png", 		"gameStarted", 610, 710, 220, 200),
			new Scanbox("priest-s.png", 	"gameStarted", 610, 710, 220, 200),
			new Scanbox("rogue-s.png", 		"gameStarted", 610, 710, 220, 200),
			new Scanbox("paladin-s.png", 	"gameStarted", 610, 710, 220, 200),
			new Scanbox("warlock-s.png", 	"gameStarted", 610, 710, 220, 200),
	};
	
	public class Scanbox{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		String imgfile = null;
		float scale = 1f;
		float matchQuality = -1;
		String scene = "";
		Mask mask;
		Scanbox nestedSb = null;
		HearthImageTarget target = null;

		public Scanbox(String filename, String sce, int x, int y, int w, int h){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scene = sce;
		}
		
		public Scanbox(String filename, String sce, int x, int y, int w, int h, float scaling, float quality){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scale = scaling;
			matchQuality = quality;
			scene = sce;
		}
		
		public Scanbox makeCopy(){
			Scanbox cloned = new Scanbox(imgfile, scene, xOffset, yOffset, width, height, scale, matchQuality);
			cloned.addNested(this.nestedSb);
			cloned.addMask(mask);
			cloned.target = target;
			return cloned;
		}
		
		public Scanbox addNested(Scanbox nesting){
			nestedSb = nesting;
			return this;
		}
		
		public Scanbox addMask(Mask mask){
			this.mask = mask;
			return this;
		}
	}
	
	public class Mask{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		
		public Mask(int x, int y, int w, int h){
			xOffset=x;
			yOffset=y;
			width = w;
			height = h;
		}
	}
}

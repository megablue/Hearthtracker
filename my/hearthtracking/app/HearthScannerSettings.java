package my.hearthtracking.app;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class HearthScannerSettings {
	ArrayList <Scanbox>list 	= null;

	public HearthScannerSettings (){
		if(list != null){
			return;
		}

		list	= new ArrayList<Scanbox>();
		
		list.add(
			new Scanbox("arena-mode.png", 		"gameMode",		"arena",		460, 366, 50, 50)
		);

		list.add(
			new Scanbox("ranked-mode.png",		"gameMode",		"ranked",		1283, 219, 64, 64).matchColor(123,132,131)
		);

		list.add(
			new Scanbox("unranked-mode.png",	"gameMode",		"unranked",		974, 222, 64, 64).matchColor(154,156,149)
		);

		list.add(
			new Scanbox("practice-mode.png",	"gameMode",		"practice",		1180, 213, 120, 120).matchColor(93,88,58)
		);

		list.add(
			new Scanbox("challenge-mode.png",	"gameMode",		"challenge",	1180, 213, 120, 120).matchColor(79,79,72)
		);

		list.add(
			new Scanbox("coin-0.png",			"coin", 		"first",		1060, 485, 90, 90)
		);

		list.add(
			new Scanbox("coin-1.png",			"coin", 		"second",		1060, 485, 90, 90)
		);

		list.add(
			new Scanbox("coin-2.png",			"coin", 		"second",		880, 1000, 50, 50)
		);

		list.add(
			new Scanbox("coin-3.png",			"coin", 		"second",		794, 967, 50, 50)
		);
		
		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLose", 	"0",	424, 534, 64, 59).matchColor(193,162,108)
		);
		
		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"1",	424, 534, 64, 56)
		);
		
		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"2",	514, 534, 64, 56)
		);
		
		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"3",	608, 534, 64, 56)
		);
	
		list.add(
			new Scanbox("arena-key-0.png",	"arenaWins",	"0", 347, 236, 100, 100).setScore(0.15f).matchColor(143, 96, 67).resolveConflict()
		);
		
		list.add(
			new Scanbox("arena-key-1.png",	"arenaWins",	"1", 347, 236, 100, 100).setScore(0.1f).matchColor(123, 88, 69).setColorScore(0.92f).resolveConflict()
		);
		
		list.add(
			new Scanbox("arena-key-2.png",	"arenaWins",	"2", 347, 236, 100, 100).setScore(0.1f).matchColor(117, 84, 48).setColorScore(0.91f).resolveConflict()
		);
		
		list.add(
			new Scanbox("arena-key-3.png",	"arenaWins",	"3", 347, 236, 100, 100).setScore(0.1f).matchColor(99, 72, 58)
		);

		list.add(
			new Scanbox("arena-key-4.png",	"arenaWins",	"4", 347, 236, 100, 100).setScore(0.1f).matchColor(127, 136, 141)
		);
		
		list.add(
			new Scanbox("arena-key-5.png",	"arenaWins",	"5", 347, 236, 100, 100).setScore(0.1f).matchColor(161, 137, 67)
		);
		
		list.add(
			new Scanbox("arena-key-6.png",	"arenaWins",	"6", 347, 236, 100, 100).setScore(0.1f).matchColor(119, 119, 126)
		);
		
		list.add(
			new Scanbox("arena-key-7.png",	"arenaWins",	"7", 347, 236, 100, 100).setScore(0.1f).matchColor(95, 109, 115)
		);
		
		list.add(
			new Scanbox("arena-key-8.png",	"arenaWins",	"8", 347, 236, 100, 100).setScore(0.1f).matchColor(149, 124, 75)
		);
		
		list.add(
			new Scanbox("arena-key-9.png",	"arenaWins",	"9", 347, 236, 100, 100).setScore(0.1f).matchColor(155, 127, 80)
		);

		list.add(
			new Scanbox("arena-key-10.png",	"arenaWins",	"10", 347, 236, 100, 100).setScore(0.1f).matchColor(61, 84, 92)
		);
		
		list.add(
			new Scanbox("arena-key-11.png",	"arenaWins",	"11", 347, 236, 100, 100).setScore(0.1f).matchColor(163, 109, 66)
		);
		
		list.add(
			new Scanbox("arena-key-12.png",	"arenaWins",	"12", 347, 236, 100, 100).setScore(0.1f).matchColor(177, 169, 147)
		);
		
		list.add(
			new Scanbox("mage.png", 	"arenaHero",	"mage",	144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("hunter.png", 	"arenaHero",	"hunter", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("warrior.png", 	"arenaHero",	"warrior", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("shaman.png", 	"arenaHero",	"shaman", 144, 752, 120, 120)
		);

		list.add(
			new Scanbox("druid.png", 	"arenaHero",	"druid", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("priest.png", 	"arenaHero",	"priest", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("rogue.png", 	"arenaHero",	"rogue", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("paladin.png", 	"arenaHero",	"paladin", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("warlock.png", 	"arenaHero",	"warlock", 144, 752, 120, 120)
		);
		
		list.add(
			new Scanbox("mage-ingame-bottom.png", 	"topHero",	"mage", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("hunter-ingame-bottom.png", 	"topHero",	"hunter", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("warrior-ingame-bottom.png", 	"topHero",	"warrior", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("shaman-ingame-bottom.png", 	"topHero",	"shaman", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("druid-ingame-bottom.png", 	"topHero",	"druid", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("priest-ingame-bottom.png", 	"topHero",	"priest", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("rogue-ingame-bottom.png", 	"topHero",	"rogue", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("paladin-ingame-bottom.png", 	"topHero",	"paladin", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("warlock-ingame-bottom.png", 	"topHero",	"warlock", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("mage-ingame-bottom.png", 		"bottomHero",	"mage",		693, 757, 60, 60)
		);

		list.add(
			new Scanbox("hunter-ingame-bottom.png", 	"bottomHero",	"hunter",	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("warrior-ingame-bottom.png", 	"bottomHero",	"warrior",	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("shaman-ingame-bottom.png", 	"bottomHero",	"shaman", 	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("druid-ingame-bottom.png", 		"bottomHero",	"druid", 	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("priest-ingame-bottom.png", 	"bottomHero",	"priest", 	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("rogue-ingame-bottom.png", 		"bottomHero",	"rogue", 	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("paladin-ingame-bottom.png", 	"bottomHero",	"paladin", 	693, 757, 60, 60)
		);

		list.add(
			new Scanbox("warlock-ingame-bottom.png", 	"bottomHero",	"warlock", 	693, 757, 60, 60)
		);
		
		list.add(
			new Scanbox("deck-selection-0.png", 	"deckSelection",	"0", 	127, 332, 80, 23).matchColor(83,104,118)
		);
		
		list.add(
			new Scanbox("deck-selection-1.png", 	"deckSelection",	"1", 	368, 332, 80, 23).matchColor(83,104,118)
		);
		
		list.add(
			new Scanbox("deck-selection-2.png", 	"deckSelection",	"2", 	608, 332, 80, 23).matchColor(83,104,118)
		);
		
		list.add(
			new Scanbox("deck-selection-3.png", 	"deckSelection",	"3", 	127, 555, 80, 23).matchColor(83,104,118)
		);
			
		list.add(
			new Scanbox("deck-selection-4.png", 	"deckSelection",	"4", 	368, 555, 80, 23).matchColor(83,104,118)
		);
			
		list.add(
			new Scanbox("deck-selection-5.png", 	"deckSelection",	"5", 	608, 555, 80, 23).matchColor(83,104,118)
		);
		
		list.add(
			new Scanbox("deck-selection-6.png", 	"deckSelection",	"6", 	127, 779, 80, 23).matchColor(83,104,118)
		);
			
		list.add(
			new Scanbox("deck-selection-7.png", 	"deckSelection",	"7", 	368, 779, 80, 23).matchColor(83,104,118)
		);
			
		list.add(
			new Scanbox("deck-selection-8.png", 	"deckSelection",	"8", 	608, 779, 80, 23).matchColor(83,104,118)
		);

		list.add(
			new Scanbox("defeat-bottom.png",		"gameResult",	"bottomDefeated",	549, 782, 120, 120)
		);

		list.add(
			new Scanbox("defeat-top.png",			"gameResult",	"topDefeated",		558, 155, 120, 120)
		);
	}

	public class Scanbox{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		String imgfile = null;
		float scale = 1f;
		float matchQuality = 0.1f;

		String scene = "";
		String identifier	=	"";

		Mask mask;
		Scanbox nestedSb = null;
		
		BufferedImage target = null;
		BufferedImage unScaledTarget = null;
		
		boolean resolveConflict = false;
		
		boolean matchColor = false;

		float colorScore = 0.9f;
		
		int red = 0;
		int green = 0;
		int blue = 0;
		
		public Scanbox(String filename, String sce, String id, int x, int y, int w, int h){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scene = sce;
			identifier = id;
		}

		public Scanbox makeCopy(){
			Scanbox cloned = new Scanbox(imgfile, scene, identifier, xOffset, yOffset, width, height);
			cloned.setScore(this.matchQuality);
			cloned.scale = this.scale;
			cloned.addNested(this.nestedSb);
			cloned.addMask(mask);
			cloned.identifier = identifier;
			cloned.target = target;
			return cloned;
		}
		
		public Scanbox resolveConflict(){
			resolveConflict = true;
			return this;
		}
		
		public Scanbox matchColor(int r, int g, int b){
			this.matchColor = true;
			this.red = r;
			this.green = g;
			this.blue = b;
			return this;
		}

		public Scanbox setColorScore(float s){
			this.colorScore = s;
			return this;
		}

		public Scanbox setScore(float s){
			this.matchQuality = s;
			return this;
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

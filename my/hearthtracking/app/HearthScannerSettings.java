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
			new Scanbox("go-first.png",			"coin", 		"first",		1060, 485, 90, 90)
		);

		list.add(
			new Scanbox("go-second.png",		"coin", 		"second",		1060, 485, 90, 90)
		);

		list.add(
			new Scanbox("victory.png",			"gameResult",		"victory",		491, 524, 60, 60)
		);

				list.add(
					new Scanbox("victory.png",			"gameResult",		"victory",		496, 519, 60, 60)
				);

				list.add(
					new Scanbox("victory.png",			"gameResult",		"victory",		501, 514, 60, 60)
				);

				list.add(
					new Scanbox("victory.png",			"gameResult",		"victory",		506, 509, 60, 60)
				);

				list.add(
					new Scanbox("victory.png",			"gameResult",		"victory",		511, 504, 60, 60)
				);
				
		list.add(
			new Scanbox("victory.png",			"gameResult",		"victory",		519, 488, 60, 60)
		);

		list.add(
			new Scanbox("defeat.png",			"gameResult",		"defeat",		468, 568, 60, 60)
		);
		
				list.add(
					new Scanbox("defeat.png",			"gameResult",		"defeat",		471, 565, 60, 60)
				);
				
				list.add(
					new Scanbox("defeat.png",			"gameResult",		"defeat",		474, 562, 60, 60)
				);
					
				list.add(
					new Scanbox("defeat.png",			"gameResult",		"defeat",		477, 559, 60, 60)
				);
				
				list.add(
					new Scanbox("defeat.png",			"gameResult",		"defeat",		480, 556, 60, 60)
				);
				
				list.add(
					new Scanbox("defeat.png",			"gameResult",		"defeat",		483, 553, 60, 60)
				);
		
		list.add(
			new Scanbox("defeat.png",			"gameResult",		"defeat",		486, 550, 60, 60)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"3",	608, 534, 64, 56)
		);
		
		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"2",	514, 534, 64, 56)
		);
		
		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"1",	424, 534, 64, 56)
		);
		
		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"3",	608, 534, 64, 59)
		);
		
		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"2",	514, 534, 64, 59)
		);
		
		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"1",	424, 534, 64, 59)
		);
	
		list.add(
			new Scanbox("0.png",	"arenaWins",	"0", 347, 236, 100, 100).setScore(0.1f).resolveConflict()
		);
		
		list.add(
			new Scanbox("1.png",	"arenaWins",	"1", 347, 236, 100, 100).setScore(0.1f).resolveConflict()
		);
		
		list.add(
			new Scanbox("2.png",	"arenaWins",	"2", 347, 236, 100, 100).setScore(0.1f).resolveConflict()
		);
		
		list.add(
			new Scanbox("3.png",	"arenaWins",	"3", 347, 236, 100, 100).setScore(0.1f)
		);

		list.add(
			new Scanbox("4.png",	"arenaWins",	"4", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("5.png",	"arenaWins",	"5", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("6.png",	"arenaWins",	"6", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("7.png",	"arenaWins",	"7", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("8.png",	"arenaWins",	"8", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("9.png",	"arenaWins",	"9", 347, 236, 100, 100).setScore(0.1f)
		);

		list.add(
			new Scanbox("10.png",	"arenaWins",	"10", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("11.png",	"arenaWins",	"11", 347, 236, 100, 100).setScore(0.1f)
		);
		
		list.add(
			new Scanbox("12.png",	"arenaWins",	"12", 347, 236, 100, 100).setScore(0.1f)
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
			new Scanbox("mage-opphero.png", 	"oppHero",	"mage", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("hunter-opphero.png", 	"oppHero",	"hunter", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("warrior-opphero.png", 	"oppHero",	"warrior", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("shaman-opphero.png", 	"oppHero",	"shaman", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("druid-opphero.png", 	"oppHero",	"druid", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("priest-opphero.png", 	"oppHero",	"priest", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("rogue-opphero.png", 	"oppHero",	"rogue", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("paladin-opphero.png", 	"oppHero",	"paladin", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("warlock-opphero.png", 	"oppHero",	"warlock", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("mage-s.png", 		"myHero",	"mage",		690, 757, 60, 60)
		);

		list.add(
			new Scanbox("hunter-s.png", 	"myHero",	"hunter",	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("warrior-s.png", 	"myHero",	"warrior",	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("shaman-s.png", 	"myHero",	"shaman", 	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("druid-s.png", 		"myHero",	"druid", 	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("priest-s.png", 	"myHero",	"priest", 	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("rogue-s.png", 		"myHero",	"rogue", 	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("paladin-s.png", 	"myHero",	"paladin", 	690, 757, 60, 60)
		);

		list.add(
			new Scanbox("warlock-s.png", 	"myHero",	"warlock", 	690, 757, 60, 60)
		);
		
		list.add(
			new Scanbox("deck-selection-1.png", 	"deckSelection",	"1", 	127, 332, 80, 100)
		);
		
		list.add(
			new Scanbox("deck-selection-2.png", 	"deckSelection",	"2", 	368, 332, 80, 100)
		);
		
		list.add(
			new Scanbox("deck-selection-3.png", 	"deckSelection",	"3", 	608, 332, 80, 100)
		);
		
		list.add(
			new Scanbox("deck-selection-4.png", 	"deckSelection",	"4", 	127, 555, 80, 100)
		);
			
		list.add(
			new Scanbox("deck-selection-5.png", 	"deckSelection",	"5", 	368, 555, 80, 100)
		);
			
		list.add(
			new Scanbox("deck-selection-6.png", 	"deckSelection",	"6", 	608, 555, 80, 100)
		);
		
		list.add(
			new Scanbox("deck-selection-7.png", 	"deckSelection",	"7", 	127, 779, 80, 100)
		);
			
		list.add(
			new Scanbox("deck-selection-8.png", 	"deckSelection",	"8", 	368, 779, 80, 100)
		);
			
		list.add(
			new Scanbox("deck-selection-9.png", 	"deckSelection",	"9", 	608, 779, 80, 100)
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

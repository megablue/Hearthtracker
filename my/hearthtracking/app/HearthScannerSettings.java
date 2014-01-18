package my.hearthtracking.app;

import java.util.ArrayList;

public class HearthScannerSettings {
	ArrayList <Scanbox>list 	= null;

	public HearthScannerSettings (){
		if(list != null){
			return;
		}

		list	= new ArrayList<Scanbox>();
		
		list.add(
			new Scanbox("arena-leaf.png", 		"gameMode",		"arena",		435, 340, 100, 100)
		);

		list.add(
			new Scanbox("ranked-mode.png",		"gameMode",		"ranked",		1080, 134, 200, 200)
		);

		list.add(
			new Scanbox("unranked-mode.png",	"gameMode",		"unranked",		1070, 138, 100, 100)
		);

		list.add(
			new Scanbox("practice-mode.png",	"gameMode",		"practice",		985, 50, 80, 80)
		);

		list.add(
			new Scanbox("challenge-mode.png",	"gameMode",		"challenge",	1200, 100, 120, 120)
		);

		list.add(
			new Scanbox("go-first.png",			"coin", 		"first",		910, 550, 400, 150)
		);

		list.add(
			new Scanbox("go-second.png",		"coin", 		"second",		910, 550, 400, 150)
		);

		list.add(
			new Scanbox("victory.png",			"gameResult",		"victory",		440, 500, 100, 100)
		);

		list.add(
			new Scanbox("defeat.png",			"gameResult",		"defeat",		452, 547, 100, 100)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"1",	400, 507, 110, 110)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"2",	492, 507, 110, 110)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"3",	584, 507, 110, 110)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"1",	400, 507, 110, 110)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"2",	492, 507, 110, 110)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"3",	584, 507, 110, 110)
		);
		
		list.add(
			new Scanbox("0.png",	"arenaWins",	"0", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("1.png",	"arenaWins",	"1", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("2.png",	"arenaWins",	"2", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("3.png",	"arenaWins",	"3", 300, 190, 200, 200, 1f, 0.9f)
		);

		list.add(
			new Scanbox("4.png",	"arenaWins",	"4", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("5.png",	"arenaWins",	"5", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("6.png",	"arenaWins",	"6", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("7.png",	"arenaWins",	"7", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("8.png",	"arenaWins",	"8", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("9.png",	"arenaWins",	"9", 300, 190, 200, 200, 1f, 0.9f)
		);

		list.add(
			new Scanbox("10.png",	"arenaWins",	"10", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("11.png",	"arenaWins",	"11", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("12.png",	"arenaWins",	"12", 300, 190, 200, 200, 1f, 0.9f)
		);
		
		list.add(
			new Scanbox("mage.png", 	"arenaHero",	"mage",	80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("hunter.png", 	"arenaHero",	"hunter", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("warrior.png", 	"arenaHero",	"warrior", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("shaman.png", 	"arenaHero",	"shaman", 80, 720, 240, 160)
		);

		list.add(
			new Scanbox("druid.png", 	"arenaHero",	"druid", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("priest.png", 	"arenaHero",	"priest", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("rogue.png", 	"arenaHero",	"rogue", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("paladin.png", 	"arenaHero",	"paladin", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("warlock.png", 	"arenaHero",	"warlock", 80, 720, 240, 160)
		);
		
		list.add(
			new Scanbox("mage-s.png", 		"oppHero",	"mage", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("hunter-s.png", 	"oppHero",	"hunter", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("warrior-s.png", 	"oppHero",	"warrior", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("shaman-s.png", 	"oppHero",	"shaman", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("druid-s.png", 		"oppHero",	"druid", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("priest-s.png", 	"oppHero",	"priest", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("rogue-s.png", 		"oppHero",	"rogue", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("paladin-s.png", 	"oppHero",	"paladin", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("warlock-s.png", 	"oppHero",	"warlock", 610, 70, 220, 200)
		);

		list.add(
			new Scanbox("mage-s.png", 		"myHero",	"mage",		610, 710, 220, 200)
		);

		list.add(
			new Scanbox("hunter-s.png", 	"myHero",	"hunter",	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("warrior-s.png", 	"myHero",	"warrior",	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("shaman-s.png", 	"myHero",	"shaman", 	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("druid-s.png", 		"myHero",	"druid", 	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("priest-s.png", 	"myHero",	"priest", 	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("rogue-s.png", 		"myHero",	"rogue", 	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("paladin-s.png", 	"myHero",	"paladin", 	610, 710, 220, 200)
		);

		list.add(
			new Scanbox("warlock-s.png", 	"myHero",	"warlock", 	610, 710, 220, 200)
		);
	}

	public class Scanbox{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		String imgfile = null;
		float scale = 1f;
		float matchQuality = -1;

		String scene = "";
		String identifier	=	"";

		Mask mask;
		Scanbox nestedSb = null;
		HearthImageTarget target = null;

		public Scanbox(String filename, String sce, String id){
			imgfile = filename;
			scene = sce;
			identifier = id;
		}

		public Scanbox(String filename, String sce, String id, int x, int y, int w, int h){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scene = sce;
			identifier = id;
		}

		public Scanbox(String filename, String sce, String id, int x, int y, int w, int h, float scaling, float quality){
			imgfile = filename;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scale = scaling;
			matchQuality = quality;
			scene = sce;
			identifier = id;
		}
		
		public Scanbox makeCopy(){
			Scanbox cloned = new Scanbox(imgfile, scene, identifier, xOffset, yOffset, width, height, scale, matchQuality);
			cloned.addNested(this.nestedSb);
			cloned.addMask(mask);
			cloned.identifier = identifier;
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

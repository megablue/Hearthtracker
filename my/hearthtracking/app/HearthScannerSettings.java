package my.hearthtracking.app;

import java.util.ArrayList;

import org.sikuli.api.ImageTarget;

public class HearthScannerSettings {
	ArrayList <Scanbox>list 	= null;

	public HearthScannerSettings (){
		if(list != null){
			return;
		}

		list	= new ArrayList<Scanbox>();
		
		list.add(
			new Scanbox("arena-leaf.png", 		"gameMode",		"arena",		460, 366, 50, 50)
		);

		list.add(
			new Scanbox("ranked-mode.png",		"gameMode",		"ranked",		1093, 145, 100, 100)
		);

		list.add(
			new Scanbox("unranked-mode.png",	"gameMode",		"unranked",		1094, 163, 50, 50)
		);

		list.add(
			new Scanbox("practice-mode.png",	"gameMode",		"practice",		1000, 64, 50, 50)
		);

		list.add(
			new Scanbox("challenge-mode.png",	"gameMode",		"challenge",	1255, 120, 50, 50)
		);

		list.add(
			new Scanbox("go-first.png",			"coin", 		"first",		1030, 629, 155, 30)
		);

		list.add(
			new Scanbox("go-second.png",		"coin", 		"second",		966, 629, 270, 30)
		);

		list.add(
			new Scanbox("victory.png",			"gameResult",		"victory",		462, 528, 50, 50)
		);

		list.add(
			new Scanbox("defeat.png",			"gameResult",		"defeat",		481, 574, 50, 50)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"1",	425, 532, 66, 59)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"2",	515, 532, 66, 59)
		);

		list.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"3",	609, 532, 66, 59)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"1",	423, 533, 68, 59)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"2",	514, 533, 68, 59)
		);

		list.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"3",	608, 533, 68, 59)
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
			new Scanbox("mage-opphero.png", 		"oppHero",	"mage", 690, 130, 60, 60)
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
			new Scanbox("druid-opphero.png", 		"oppHero",	"druid", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("priest-opphero.png", 	"oppHero",	"priest", 690, 130, 60, 60)
		);

		list.add(
			new Scanbox("rogue-opphero.png", 		"oppHero",	"rogue", 690, 130, 60, 60)
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
		ImageTarget target = null;
		
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

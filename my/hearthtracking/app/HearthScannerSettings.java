package my.hearthtracking.app;

import java.util.ArrayList;

public class HearthScannerSettings {
	ArrayList <Scanbox>singleList 	= null;
	ArrayList <ScanGroup>groupList 	= null;

	public HearthScannerSettings (){
		if(singleList != null || groupList != null){
			return;
		}

		singleList	= new ArrayList<Scanbox>();
		groupList	= new ArrayList<ScanGroup>();

		singleList.add(
			new Scanbox("arena-leaf.png", 		"gameMode",		"arena",		435, 340, 100, 100)
		);

		singleList.add(
			new Scanbox("ranked-mode.png",		"gameMode",		"ranked",		1080, 134, 200, 200)
		);

		singleList.add(
			new Scanbox("unranked-mode.png",	"gameMode",		"unranked",		1070, 138, 100, 100)
		);

		singleList.add(
			new Scanbox("practice-mode.png",	"gameMode",		"practice",		985, 50, 80, 80)
		);

		singleList.add(
			new Scanbox("challenge-mode.png",	"gameMode",		"challenge",	1200, 100, 120, 120)
		);

		singleList.add(
			new Scanbox("go-first.png",			"coin", 		"first",		910, 550, 400, 150)
		);

		singleList.add(
			new Scanbox("go-second.png",		"coin", 		"second",		910, 550, 400, 150)
		);

		singleList.add(
			new Scanbox("victory.png",			"gameResult",		"victory",		440, 500, 100, 100)
		);

		singleList.add(
			new Scanbox("defeat.png",			"gameResult",		"defeat",		452, 547, 100, 100)
		);

		singleList.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"1",	400, 507, 110, 110)
		);

		singleList.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"2",	492, 507, 110, 110)
		);

		singleList.add(
			new Scanbox("lose-checkbox-checked.png", 	"arenaLose", 	"3",	584, 507, 110, 110)
		);

		singleList.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"1",	400, 507, 110, 110)
		);

		singleList.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"2",	492, 507, 110, 110)
		);

		singleList.add(
			new Scanbox("lose-checkbox-unchecked.png", 	"arenaLive", 	"3",	584, 507, 110, 110)
		);

		Scanbox[] arenaWins = {
			new Scanbox("0.png",	"arenaWins",	"0"),	
			new Scanbox("1.png",	"arenaWins",	"1"),
			new Scanbox("2.png",	"arenaWins",	"2"),
			new Scanbox("3.png",	"arenaWins",	"3"),
			new Scanbox("4.png",	"arenaWins",	"4"),
			new Scanbox("5.png",	"arenaWins",	"5"),
			new Scanbox("6.png",	"arenaWins",	"6"),
			new Scanbox("7.png",	"arenaWins",	"7"),
			new Scanbox("8.png",	"arenaWins",	"8"),
			new Scanbox("9.png",	"arenaWins",	"9"),
			new Scanbox("10.png",	"arenaWins",	"10"),
			new Scanbox("11.png",	"arenaWins",	"11"),
			new Scanbox("12.png",	"arenaWins",	"12")
		};
		
		groupList.add(new ScanGroup(arenaWins, "arenaWins", 300, 190, 200, 200, 1f, 0.9f));

		Scanbox[] arenaHero = {
			new Scanbox("mage.png", 	"arenaHero",	"mage"),
			new Scanbox("hunter.png", 	"arenaHero",	"hunter"),
			new Scanbox("warrior.png", 	"arenaHero",	"warrior"),
			new Scanbox("shaman.png", 	"arenaHero",	"shaman"),
			new Scanbox("druid.png", 	"arenaHero",	"druid"),
			new Scanbox("priest.png", 	"arenaHero",	"priest"),
			new Scanbox("rogue.png", 	"arenaHero",	"rogue"),
			new Scanbox("paladin.png", 	"arenaHero",	"paladin"),
			new Scanbox("warlock.png", 	"arenaHero",	"warlock"),
		};
		
		groupList.add(new ScanGroup(arenaHero, "arenaWins", 80, 720, 240, 160));

		Scanbox[] oppHero = {
			new Scanbox("mage-s.png", 		"oppHero",	"mage"),
			new Scanbox("hunter-s.png", 	"oppHero",	"hunter"),
			new Scanbox("warrior-s.png", 	"oppHero",	"warrior"),
			new Scanbox("shaman-s.png", 	"oppHero",	"shaman"),
			new Scanbox("druid-s.png", 		"oppHero",	"druid"),
			new Scanbox("priest-s.png", 	"oppHero",	"priest"),
			new Scanbox("rogue-s.png", 		"oppHero",	"rogue"),
			new Scanbox("paladin-s.png", 	"oppHero",	"paladin"),
			new Scanbox("warlock-s.png", 	"oppHero",	"warlock"),
		};
		
		groupList.add(new ScanGroup( oppHero, "oppHero", 610, 70, 220, 200));

		Scanbox[] myHero = {
			new Scanbox("mage-s.png", 		"myHero",	"mage"),
			new Scanbox("hunter-s.png", 	"myHero",	"hunter"),
			new Scanbox("warrior-s.png", 	"myHero",	"warrior"),
			new Scanbox("shaman-s.png", 	"myHero",	"shaman"),
			new Scanbox("druid-s.png", 		"myHero",	"druid"),
			new Scanbox("priest-s.png", 	"myHero",	"priest"),
			new Scanbox("rogue-s.png", 		"myHero",	"rogue"),
			new Scanbox("paladin-s.png", 	"myHero",	"paladin"),
			new Scanbox("warlock-s.png", 	"myHero",	"warlock"),
		};
		
		groupList.add( new ScanGroup(myHero, "myHero", 610, 710, 220, 200));
	}

	//same offsets different images
	public class ScanGroup{
		int xOffset = 0;
		int yOffset = 0;
		int width = 0;
		int height = 0;
		float scale = 1f;
		float matchQuality = -1;
		String scene = "";
		Scanbox[] scanBoxes = null;

		public ScanGroup(Scanbox[] boxes, String sce, int x, int y, int w, int h){
			scene = sce;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scanBoxes = boxes;
		}

		public ScanGroup(Scanbox[] boxes, String sce, int x, int y, int w, int h, float scaling, float quality){
			scene = sce;
			xOffset = x;
			yOffset = y;
			width = w;
			height = h;
			scanBoxes = boxes;
			scale = scaling;
			matchQuality = quality;
		}
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

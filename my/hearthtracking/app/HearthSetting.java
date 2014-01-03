package my.hearthtracking.app;

public class HearthSetting {
	int scanInterval = 500;
	int gameWidth = 1920;
	int gameHeight = 1080;
	String gameLang = "enUS";
	String gameServer = "";
	boolean scannerEnabled = true;
	boolean autoPing = true;
	boolean autoRes = true;
	int settingVer = 0;
	int xOffset = 0;
	int yOffset = 0;
	boolean alwaysScan = false;
	int tracker = 0;
	boolean popup = true;
	
	public boolean upgrade(){
		return upgrade1() || upgrade2() || upgrade3();
	}
	
	private boolean upgrade1(){
		boolean upgraded = false;
		
		if(settingVer == 0){
			autoRes = true;
			settingVer = 1;
			upgraded = true;
		}
	
		return upgraded;
	}
	
	private boolean upgrade2(){
		boolean upgraded = false;
		
		if(settingVer == 1){
			alwaysScan = false;
			settingVer = 2;
			upgraded = true;
		}
	
		return upgraded;
	}
	
	private boolean upgrade3(){
		boolean upgraded = false;
		
		if(settingVer == 2){
			popup = true;
			settingVer = 3;
			upgraded = true;
		}
	
		return upgraded;
	}
}

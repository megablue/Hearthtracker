public class HearthSetting {
	int scanInterval = 500;
	int gameWidth = 1920;
	int gameHeight = 1080;
	String gameLang = "enUS";
	boolean scannerEnabled = true;
	boolean autoPing = true;
	boolean autoRes = true;
	int settingVer = 0;
	
	public boolean upgrade(){
		boolean upgraded = false;
		
		upgraded = upgrade1();

		return upgraded;
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
}

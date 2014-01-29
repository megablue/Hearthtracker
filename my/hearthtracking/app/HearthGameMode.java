package my.hearthtracking.app;

public class HearthGameMode {
	public static final int UNKNOWNMODE = -1;
	public static final int MENUMODE = 0;
	public static final int ARENAMODE = 1;
	public static final int RANKEDMODE = 2;
	public static final int UNRANKEDMODE = 3;
	public static final int CHALLENGEMODE = 4;
	public static final int PRACTICEMODE = 5;
	
	public HearthGameMode() {
		
	}
	
	public static String gameModeToString(int mode){
		switch(mode){
			case HearthGameMode.ARENAMODE:
				return "Arena";
	
			case HearthGameMode.RANKEDMODE:
				return "Ranked";
		
			case HearthGameMode.UNRANKEDMODE:
				return "Unranked";
			
			case HearthGameMode.PRACTICEMODE:
				return"Practice";
				
			case HearthGameMode.CHALLENGEMODE:
				return "Challenge";
		}
		
		return "Unknown mode";
	}
	
	public static String gameModeToStringLabel(int mode){
		HearthLanguageManager uiLang = HearthLanguageManager.getInstance();
		
		switch(mode){
			case HearthGameMode.ARENAMODE:
				return uiLang.t("Arena");
	
			case HearthGameMode.RANKEDMODE:
				return uiLang.t("Ranked");
		
			case HearthGameMode.UNRANKEDMODE:
				return uiLang.t("Unranked");
			
			case HearthGameMode.PRACTICEMODE:
				return uiLang.t("Practice");
				
			case HearthGameMode.CHALLENGEMODE:
				return uiLang.t("Challenge");
		}
		
		return uiLang.t("Unknown mode");
	}

}

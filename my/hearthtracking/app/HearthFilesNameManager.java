package my.hearthtracking.app;

import java.io.File;

public class HearthFilesNameManager {
	
	//log/settings
	static String updaterLog 		= "." + File.separator + "configs" + File.separator + "update.xml";
	static String settingFile 		= "." + File.separator + "configs" + File.separator + "settings.xml";
	static String decksFile 		= "." + File.separator + "configs" + File.separator + "decks.xml";
	static String heroesFile 		= "." + File.separator + "configs" + File.separator + "heroes.xml";
	static String gameLangsFile 	= "." + File.separator + "configs" + File.separator + "gameLangs.xml";
	static String gameResFile 		= "." + File.separator + "configs" + File.separator + "gameResolutions.xml";
	static String uiLangFile 		= "." + File.separator + "configs"  + File.separator + "uiLangs" + File.separator + "%s.xml";
	static String logFile 			= "." + File.separator + "logs" + File.separator + "%s.log";
	static String syncFile 			= "." + File.separator + "configs" + File.separator + "sync.xml";
	static String dbFile 			= "." + File.separator + "data" + File.separator + "database.xml";
	static String updateLog 		= "." + File.separator + "configs" + File.separator + "update.xml";
	
	//UI related files
	static String miniHeroImage		= "." + File.separator + "images" + File.separator + "%s-s.png";
	static String logo				= "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo.png";
	static String logo32			= "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-32.png";
	static String logo128			= "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-128.png";
	static String payapl			= "." + File.separator + "images" + File.separator + "etc" + File.separator + "paypal.png";
	static String facebook			= "." + File.separator + "images" + File.separator + "etc" + File.separator + "facebook.png";
	static String teamliquid		= "." + File.separator + "images" + File.separator + "etc" + File.separator + "teamliquid.png";
	static String twitter			= "." + File.separator + "images" + File.separator + "etc" + File.separator + "twitter.png";
	
	//scanner related files
	static String scanTargetFileOverrideByLang		= "." + File.separator + "images" + File.separator + "%s" + File.separator + "%s";
	static String scanTargetFileDefault				= "." + File.separator + "images" + File.separator + "%s";
	static String scannerSettingFileDefault			= "." + File.separator + "configs" + File.separator + "gameLangs" + File.separator + "%s.xml"; 
	static String scannerSettingFileOverrideByRes	= "." + File.separator + "configs" 
													+ File.separator + "gameLangs" 
													+ File.separator + "%s" 
													+ File.separator + "%d" + "x" + "%d"
													+ ".xml"; 
}

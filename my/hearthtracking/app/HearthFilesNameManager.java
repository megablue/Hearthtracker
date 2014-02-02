package my.hearthtracking.app;

import java.io.File;

public class HearthFilesNameManager {
	static String workingDir	= "."		+ File.separator;
	static String configsPath 	= "configs" + File.separator;
	static String imagesPath 	= "images"	+ File.separator;
	static String logsPath 		= "logs"	+ File.separator;
	static String dataPath 		= "data"	+ File.separator;
	static String cachePath 	= "cache"	+ File.separator;
	
	//configs
	static String updaterLog 		= workingDir 	+ configsPath	+ "update.xml";
	static String uiLangsListFile 	= workingDir 	+ configsPath	+ "uiLangs.xml";
	static String settingFile 		= workingDir 	+ configsPath	+ "settings.xml";
	static String decksFile 		= workingDir 	+ configsPath	+ "decks.xml";
	static String heroesFile 		= workingDir 	+ configsPath	+ "heroes.xml";
	static String gameLangsFile 	= workingDir 	+ configsPath	+ "gameLangs.xml";
	static String gameResFile 		= workingDir 	+ configsPath 	+ "gameResolutions.xml";
	static String uiLangFile 		= workingDir 	+ configsPath	+ "uiLangs" + File.separator + "%s.xml";
	static String syncFile 			= workingDir 	+ configsPath 	+ "sync.xml";
	static String updateLog 		= workingDir 	+ configsPath 	+ "update.xml";
	
	//log
	static String logFile 			= workingDir 	+ logsPath 		+ "%s.html";

	//db
	static String dbFile 			= workingDir	+ dataPath		+ "database.xml";

	
	//UI related files
	static String miniHeroImage		= workingDir 	+ imagesPath 	+ "%s-ingame-bottom.png";
	static String logo				= workingDir 	+ imagesPath 	+ "etc" + File.separator + "logo.png";
	static String logo32			= workingDir 	+ imagesPath 	+ "etc" + File.separator + "logo-32.png";
	static String logo128			= workingDir 	+ imagesPath 	+ "etc" + File.separator + "logo-128.png";
	static String payapl			= workingDir 	+ imagesPath 	+ "etc" + File.separator + "paypal.png";
	static String facebook			= workingDir 	+ imagesPath 	+ "etc" + File.separator + "facebook.png";
	static String teamliquid		= workingDir 	+ imagesPath 	+ "etc" + File.separator + "teamliquid.png";
	static String twitter			= workingDir 	+ imagesPath 	+ "etc" + File.separator + "twitter.png";
	
	//scanner related files
	static String scanTargetFileOverrideByLang		= workingDir 	+ imagesPath 	+ "%s" + File.separator + "%s";
	static String scanTargetFileDefault				= workingDir 	+ imagesPath 	+ "%s";
	static String scannerSettingFileDefault			= workingDir + configsPath + "gameLangs" + File.separator + "default.xml"; 

	static String scannerSettingFileOverrideByRes	= workingDir + configsPath 
													+ "gameLangs" 
													+ File.separator + "%s" 
													+ File.separator + "%d" + "x" + "%d"
													+ ".xml"; 

	//cache												
	static String scannerImageCacheFile	= workingDir	+ cachePath		+ "%s"; 
}

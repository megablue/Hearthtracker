package my.hearthtracking.app;

import java.io.File;

public class MainLoader {
	//static Logger logger = HearthHelper.getLogger(Level.ALL);
	
	public static void main(String[] args) {
		File swtJar = new File(HearthHelper.getArchFilename("lib/swt"));
		HearthHelper.addJarToClasspath(swtJar);
		
		HearthConfigurator config = new HearthConfigurator();
		HearthSetting setting =	(HearthSetting) config.load(HearthFilesNameManager.settingFile);
		
		if(setting == null){
			setting = new HearthSetting();
			config.save(setting, HearthFilesNameManager.settingFile);
		}
		
		HearthLanguageManager.getInstance().loadLang(setting.uiLang);
		
		HearthUpdater updater = new HearthUpdater();

		if(updater.lastCheckExpired()){
			updater.check();
						
			if(updater.hasUpdate()){
				new HearthUpdateUI().open();
			}		
		}
	
		
		HearthDatabase dbSetting = (HearthDatabase) config.load(HearthFilesNameManager.dbFile);
		
		if(dbSetting == null){
			dbSetting = new HearthDatabase();
			config.save(dbSetting, HearthFilesNameManager.dbFile);
		}
		
		if(dbSetting.serverSelected == 0){
			new HearthTrackerUpgradeUI().open();
		}
				
//		HearthSync sync = new HearthSync();
//		sync.checkAccessKey();
//		sync.syncArenaBatch();
//		sync.syncMatchBatch();
				
		new HearthUI().open();
	}

}

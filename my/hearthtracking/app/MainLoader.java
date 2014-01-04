package my.hearthtracking.app;

import java.io.File;

public class MainLoader {
	//static Logger logger = HearthHelper.getLogger(Level.ALL);
	
	public static void main(String[] args) {
		File swtJar = new File(HearthHelper.getArchFilename("lib/swt"));
		HearthHelper.addJarToClasspath(swtJar);

		HearthUpdater updater = new HearthUpdater();

		if(updater.lastCheckExpired()){
			updater.check();
						
			if(updater.hasUpdate()){
				new HearthUpdateUI().open();
			}		
		}
		
		HearthConfigurator config = new HearthConfigurator();
		HearthDatabase dbSetting = (HearthDatabase) config.load("." + File.separator + "data" + File.separator + "database.xml");
		
		if(dbSetting == null){
			dbSetting = new HearthDatabase();
			config.save(dbSetting, "." + File.separator + "data" + File.separator + "database.xml");
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

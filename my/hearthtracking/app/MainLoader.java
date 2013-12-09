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
		
		HearthSync sync = new HearthSync();
		
		if(sync.checkAccessKey()){
			sync.syncAll();
		}
				
		HearthUI.main(args);
	}

}

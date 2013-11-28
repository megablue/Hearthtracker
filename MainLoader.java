import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

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
		
		HearthUI.main(args);
	}

}

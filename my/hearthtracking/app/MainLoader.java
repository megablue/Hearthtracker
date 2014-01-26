package my.hearthtracking.app;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import notifier.NotifierDialog;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainLoader {
	public static int[] version = {1, 1, 8};
	public static int experimental = 0;
	private final static int syncInterval =  1 * 60 * 1000;
	
	//static Logger logger = HearthHelper.getLogger(Level.ALL);
	public static HearthConfigurator config = new HearthConfigurator();
	public static HearthSetting setting;
	public static HearthDecks decks;
	public static HearthHeroesList heroesList;
	public static HearthGameLangList gameLanguages;
	public static HearthResolutionsList gameResolutions;
	public static HearthULangsList uiLangsList;
	
	public static HearthTracker tracker;
	public static HearthScannerManager scannerManager;
	private static Thread scannerManagerThread;
	
	volatile static boolean shutdown = false;
	volatile static boolean threadRunning = true;
	
	public static List<HearthReaderNotification> notifications = new ArrayList<HearthReaderNotification>();
	private static HearthLanguageManager lang;
	
	private static HearthUI theUI = null;

	public static void main(String[] args) {
		startup();
	}
	
	@SuppressWarnings("unused")
	private static void debug(){
		int timeLimit = 20;
		long start = System.currentTimeMillis();

		init();
		
		tracker = new HearthTracker();
		
		scannerManager = new HearthScannerManager(
			tracker, 
			setting.scanInterval,
			setting.gameLang, 
			setting.gameWidth, 
			setting.gameHeight, 
			setting.autoPing, 
			true
		);
		
		start = System.currentTimeMillis();

		while( (int) Math.round( (System.currentTimeMillis() - start) / 1000f) < timeLimit ){
			scannerManager.process();
		}
		
		scannerManager.dispose();
	}
	
	private static void startup(){
		File swtJar = new File(HearthHelper.getArchFilename("lib/swt"));
		HearthHelper.addJarToClasspath(swtJar);
		
		init();
		
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
		
		processNotification();
		startSync();
		startScannerManager();
		
		do{
			theUI = new HearthUI(scannerManager, tracker);
			theUI.open();
		}while(theUI.isRestart());
		
		exit();
	}
	
	private static void exit(){
		HearthSync sync = new HearthSync();
		
		if(sync.isValidKeyFormat() && sync.checkAccessKey()){
			sync.syncArenaBatch();
			sync.syncMatchBatch();
		}
		
		shutdown = true;
		
		while(threadRunning){
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		scannerManager.dispose();
		lang.dispose();
		tracker.closeDB();
		
		System.exit(0);
	}
	
	private synchronized static void init(){
		HearthUpdaterLog updateLog = (HearthUpdaterLog) config.load(HearthFilesNameManager.updaterLog);
		
		if(updateLog == null){
			updateLog = new HearthUpdaterLog();
			config.save(updateLog, HearthFilesNameManager.updaterLog);
		}
		
		setting =			(HearthSetting) 		config.load(HearthFilesNameManager.settingFile);
		decks =				(HearthDecks)			config.load(HearthFilesNameManager.decksFile);
		heroesList =		(HearthHeroesList) 		config.load(HearthFilesNameManager.heroesFile);
		gameLanguages = 	(HearthGameLangList) 	config.load(HearthFilesNameManager.gameLangsFile);
		gameResolutions = 	(HearthResolutionsList) config.load(HearthFilesNameManager.gameResFile);
		uiLangsList = 		(HearthULangsList) 		config.load(HearthFilesNameManager.uiLangsListFile);
		
		if(setting == null){
			setting = new HearthSetting();
			config.save(setting, HearthFilesNameManager.settingFile);
		}
		
		if(decks == null){
			decks = new HearthDecks();
			config.save(decks, HearthFilesNameManager.decksFile);
		} else {
			//register the unserialized instance
			HearthDecks.setInstance(decks);
		}
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, HearthFilesNameManager.heroesFile);
		}
		
		if(gameLanguages == null){
			gameLanguages = new HearthGameLangList();
			config.save(gameLanguages, HearthFilesNameManager.gameLangsFile);
		}
		
		if(gameResolutions == null){
			gameResolutions = new HearthResolutionsList();
			config.save(gameResolutions, HearthFilesNameManager.gameResFile);
		}
		
		if(uiLangsList == null){
			uiLangsList = new HearthULangsList();
			config.save(uiLangsList, HearthFilesNameManager.uiLangsListFile);
		}

		if(setting.upgrade()){
			config.save(setting, HearthFilesNameManager.settingFile);
		}
		
		lang = HearthLanguageManager.getInstance();
		
		lang.loadLang(setting.uiLang);
	}
	
	private static void startScannerManager(){
		tracker = new HearthTracker();
		scannerManager = new HearthScannerManager(tracker, setting.scanInterval, setting.gameLang, setting.gameWidth, setting.gameHeight, setting.autoPing, setting.alwaysScan);

		Runnable runnable = new Runnable() {
 			public void run() {
				HearthReaderNotification note = null;

		    	scannerManager.startup();

		    	if(!setting.scannerEnabled){
					scannerManager.pause();
				}

		    	while(!shutdown){
	        		scannerManager.process();
        			
        			note = scannerManager.getNotification();
        			
        			if(note != null){
        				notifications.add(note);
        			}
		    	}
		    	
		    	threadRunning = false;
		    }
		};

		scannerManagerThread = new Thread(runnable);
		scannerManagerThread.start();
	}

    private static void processNotification(){
    	Runnable runnable = new Runnable() {
		    public void run() {
		    	while(notifications.size() > 0){
			    	HearthReaderNotification note = notifications.get(0);
			    	notifications.remove(note);
			    	
			    	if(note != null && theUI != null){
		 				NotifierDialog.notify(
        						note.title, 
        						note.message, 
        						new Image( Display.getDefault(), HearthFilesNameManager.logo32),
        						theUI.getShell().getMonitor()
        				);
			    	}
		    	}
		    	
		    	Display.getDefault().timerExec(50, this);
		    }
		};
    	
		Display.getDefault().timerExec(50, runnable);
    }
 
    private static void startSync(){
    	Runnable runnable = new Runnable() {
		    public void run() {
		    	while(!shutdown){
		    		HearthSync sync = new HearthSync();
			    	boolean success = true;
			    	boolean hasEffected = false;
			    	
			    	long lastSync = sync.getLastSync();
			    	long timeDiff = new Date().getTime() - lastSync;
			    	int nextSync = (int) (timeDiff > syncInterval ? syncInterval : timeDiff - syncInterval);
			    	
			    	if(timeDiff >= syncInterval && sync.isValidKeyFormat()){	
			    		
			    		if(sync.checkAccessKey()){
			    			int arenaRecordsCount = sync.getUnsyncArenaCount();
			    			int matchRecordsCount = sync.getUnsyncMatchCount();

			    			if(arenaRecordsCount > 0){
			    				success = sync.syncArenaBatch();
			    				hasEffected = true;
			    				lastSync = new Date().getTime();
			    			}
			    			
			    			if(matchRecordsCount > 0){
			    				success = sync.syncMatchBatch() && success;
			    				hasEffected = true;
			    				lastSync = new Date().getTime();
			    			}
				    		
				    		if(hasEffected && success){
				    			HearthReaderNotification note = new HearthReaderNotification(
				    					lang.t("Web Sync"), 
				    					lang.t("Synced %d records", matchRecordsCount + arenaRecordsCount)
				    			);
				    			
				    			MainLoader.notifications.add(note);
				    		} else if(hasEffected) {
				    			HearthReaderNotification note = new HearthReaderNotification(
				    					"Web Sync", 
				    					lang.t("Error with %d records", matchRecordsCount + arenaRecordsCount)
				    			);
				    			MainLoader.notifications.add(note);
				    		}
			    		}
			    		
			    		if(sync.isTimeout()){
			    			HearthReaderNotification note = new HearthReaderNotification(
			    					lang.t("Web Sync"), 
			    					lang.t("Sync timeout. Will retry later!")
			    			);
			    			MainLoader.notifications.add(note);	 				
			 				nextSync = (int) (sync.getTimeout() - new Date().getTime());
			    		}
			    	}
			    	
			    	try {
						Thread.sleep(nextSync);
					} catch (InterruptedException e) {
						
					}
		    	}
		    }
		};
		
		Thread myThread = new Thread(runnable);
		myThread.start();
    }

}

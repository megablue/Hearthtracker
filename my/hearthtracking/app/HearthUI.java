package my.hearthtracking.app;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import notifier.NotifierDialog;
import notifier.NotificationType;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.custom.TableTree;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Table;

import swing2swt.layout.BoxLayout;
import swing2swt.layout.BorderLayout;

import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

import com.googlecode.javacv.CameraDevice.Settings;

import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuDetectEvent;

import java.text.DecimalFormat;

@SuppressWarnings({ "unused", "deprecation" })
public class HearthUI {
	protected Shell shlHearthtracker;
	private CCombo cmbGameLang;
	private Button btnEnableScanner;
	private Button[] btnScanSpeed = new Button[4];
	private CCombo cmbGameRes;
	private Button btnAutoPing;
	private Button btnVisualizeNow;
	private Label lblLastSeen;
	private Label lblLastscreencoordinate;
	private Label lblLastScanSubArea;
	private Button btnAutoDetectGameRes;
	private Group grpCurrentStats;
	private StyledText styledTextStatus; 
	private Combo cmbStatsMode;
	
	private Group grpStats;
	
	private static Display display;
	private static HearthUI window;
	static boolean debugMode = HearthHelper.isDevelopmentEnvironment();
	
	private static HearthReader hearth;
	private static HearthTracker tracker;
	private static HearthConfigurator config = new HearthConfigurator();
	private static HearthGameLangList gameLanguages;
	private static HearthResolutionsList gameResolutions;
	private static HearthSetting setting;
	private static HearthDecks decks;
	private static HearthHeroesList heroesList;
	private static Logger logger;
	
	Thread hearththread;
	private Table table;
	private Table matchesTable;
	private Table arenaTable;
	private TabFolder tabFolder_1;
	private TabFolder tabFolder_2; 
	private TabItem tbtmMatchesEdit;
	private TabItem tbtmMatchesNew;
	private TabItem tbtmArenaEdit;
	private TabItem tbtmArenaNew;
	private static Image[] heroImgs;
	private Composite composite_9;

	public static int[] version = {1, 1, 3};
	public static int experimental = 0;
	private Text txtWebSyncKey;
	private Composite composite_11;
	private Spinner spXOffset;
	private Spinner spYOffset;
	
	public static int syncInterval =  1 * 60 * 1000;
	
	volatile static boolean shutdown = false;
	volatile static boolean threadRunning = true;
	private Button btnAlwaysScan;
	
	private static List<HearthReaderNotification> notifications = new ArrayList<HearthReaderNotification>();
	private CCombo cbServer;
	private Text text_1;
	private Text text_4;
	private Text text_2;
	private Text text_3;
	private Text text_5;
	private Text text_6;
	private Text text_7;
	private Text text_8;
	private Text text_9;
	
	private Text[] txtDecks = new Text[9];
	private Label[] lblDecks = new Label[9];

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			init();
			window.open();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void init(){
		HearthUpdaterLog updateLog = (HearthUpdaterLog) config.load("." + File.separator + "configs" + File.separator + "update.xml");
		
		if(updateLog == null){
			updateLog = new HearthUpdaterLog();
			config.save(updateLog, "." + File.separator + "configs" + File.separator + "update.xml");
		}
		
		decks = (HearthDecks) config.load("." + File.separator + "configs" + File.separator + "decks.xml");
		heroesList = (HearthHeroesList) config.load("." + File.separator + "configs" + File.separator + "heroes.xml");
		gameLanguages = (HearthGameLangList) config.load("." + File.separator + "configs" + File.separator + "gameLangs.xml");
		gameResolutions = (HearthResolutionsList) config.load("." + File.separator + "configs" + File.separator + "gameResolutions.xml");
		setting = (HearthSetting) config.load("." + File.separator + "configs" + File.separator + "settings.xml");
		
		if(decks == null){
			decks = new HearthDecks();
			config.save(decks, "." + File.separator + "configs" + File.separator + "decks.xml");
		}
		
		if(heroesList == null){
			heroesList = new HearthHeroesList();
			config.save(heroesList, "." + File.separator + "configs" + File.separator + "heroes.xml");
		}
		
		if(gameLanguages == null){
			gameLanguages = new HearthGameLangList();
			config.save(gameLanguages, "." + File.separator + "configs" + File.separator + "gameLangs.xml");
		}
		
		if(gameResolutions == null){
			gameResolutions = new HearthResolutionsList();
			config.save(gameResolutions, "." + File.separator + "configs" + File.separator + "gameResolutions.xml");
		}
		
		if(setting == null){
			setting = new HearthSetting();
			config.save(setting, "." + File.separator + "configs" + File.separator + "settings.xml");
		}
		
		if(setting.upgrade()){
			config.save(setting, "." + File.separator + "configs" + File.separator + "settings.xml");
		}
		
		window = new HearthUI();
		tracker = new HearthTracker();
		hearth = new HearthReader(tracker, setting.gameLang, setting.gameWidth, setting.gameHeight, setting.autoPing, setting.alwaysScan);
		
		if(!setting.scannerEnabled){
			hearth.pause();
		}
		
		heroImgs = new Image[heroesList.getTotal()+1];
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			heroImgs[i+1] = new Image(display, "." + File.separator + "images" + File.separator + heroesList.getHeroName(i) + "-s.png");
			heroImgs[i+1] = resize(heroImgs[i+1], 24, 24);
		}

	}
	
    private static class ReaderThread
    implements Runnable {
	    public void run() {
	    	HearthReaderNotification note = null;
	    	
	    	while(!shutdown){
	        	try {
	        		long sleepTime;

	        		hearth.process();
	        		
        			Thread.sleep(setting.scanInterval);
        			
        			note = hearth.getNotification();
        			
        			if(note != null){
        				notifications.add(note);
        			}
	    		} catch (InterruptedException e) {
	    			break;
	    		}
	    	}
	    	
	    	threadRunning = false;

	    }
    }
    
    private static void exit(){
    	while(threadRunning){
    		try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				
			}
    	}
    	
		tracker.closeDB();
		System.exit(0);
    }
    
    private void recordSyncTimer(){
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
				    					"Web Sync", 
				    					"Without errors. " + (matchRecordsCount + arenaRecordsCount) + " records. "
				    			);
				    			notifications.add(note);
				    		} else if(hasEffected) {
				    			HearthReaderNotification note = new HearthReaderNotification(
				    					"Web Sync", 
				    					"With errors. " + (matchRecordsCount + arenaRecordsCount) + " records. "
				    			);
				    			notifications.add(note);
				    		}
			    		}
			    		
			    		if(sync.isTimeout()){
			    			HearthReaderNotification note = new HearthReaderNotification(
			    					"Web Sync", 
			    					"Sync timeout. Will retry later."
			    			);
			    			notifications.add(note);	 				
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
    
    private void processNotification(){
    	Runnable runnable = new Runnable() {
		    public void run() {
		    	if(notifications.size() > 0){
			    	HearthReaderNotification note = notifications.get(0);
			    	notifications.remove(note);
			    	
			    	if(note != null){
		 				NotifierDialog.notify(
        						note.title, 
        						note.message, 
        						new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-32.png" ),
        						shlHearthtracker.getMonitor()
        				);
			    	}
		    	}
		    	
		    	Display.getDefault().timerExec(100, this);
		    }
		};
    	
		Display.getDefault().timerExec(100, runnable);
    }
    
	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlHearthtracker.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-128.png" ));

		Menu menu = new Menu(shlHearthtracker, SWT.BAR);
		shlHearthtracker.setMenuBar(menu);
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText("&File");
		
		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);
		
		MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shutdown();
			}
		});
		mntmNewItem.setText("E&xit");
		
		MenuItem mntmNewSubmenu_1 = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu_1.setText("&Edit");
		
		Menu menu_2 = new Menu(mntmNewSubmenu_1);
		mntmNewSubmenu_1.setMenu(menu_2);
		
		MenuItem mntmNewItem_1 = new MenuItem(menu_2, SWT.NONE);
		mntmNewItem_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					tracker.setLastMatchWon();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		mntmNewItem_1.setText("Last Match -> Won");
		
		MenuItem mntmLastMatch = new MenuItem(menu_2, SWT.NONE);
		mntmLastMatch.setText("Last Match -> Lost");
		
		mntmLastMatch.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					tracker.setLastMatchLost();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		
		MenuItem mntmLastMatch_1 = new MenuItem(menu_2, SWT.NONE);
		mntmLastMatch_1.setText("Last Match -> went first");
		
		mntmLastMatch_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					tracker.setLastMatchWentFirst();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		
		MenuItem mntmNewItem_2 = new MenuItem(menu_2, SWT.NONE);
		mntmNewItem_2.setText("Last Match -> went second");
		
		mntmNewItem_2.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					tracker.setLastMatchWentSecond();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
		shlHearthtracker.open();
		shlHearthtracker.layout();
		Date lastUpdate = new Date(); 
		
		hearththread = new Thread(new ReaderThread());
		hearththread.start();
		processNotification();
		recordSyncTimer();

		while (!shlHearthtracker.isDisposed()) {
			if(hearththread.isAlive()){
				if(tracker.isDirty()){
					window.fillOverviewTable();
					window.fillArenaTable();
					window.fillMatchesTable();
					window.updateStatus();
					window.fillDeckWinRate();
					tracker.clearDirty();
				}
				
				if(hearth.isDirty()){
					window.updateStatus();
				}
				
				if(new Date().getTime() - lastUpdate.getTime() > 2000){	
					window.poppulateDiagnoticsStatus();
					lastUpdate = new Date();
				}
			}
						
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void shutdown(){
		hearththread.interrupt();
		shutdown = true;
		HearthSync sync = new HearthSync();
		
		if(sync.isValidKeyFormat() && sync.checkAccessKey()){
			sync.syncArenaBatch();
			sync.syncMatchBatch();
		}
		
		exit();
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlHearthtracker = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX));
		shlHearthtracker.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				shlHearthtracker.setMinimized(true);
				arg0.doit = false;
			}
		});
		shlHearthtracker.setSize(620, 456);
		shlHearthtracker.setText("HearthTracker - Automated Stats Tracking for Hearthstone enthusiasts!");
		
		if(experimental > 0){
			shlHearthtracker.setText("HearthTracker (Experimental build " 
									+ experimental 
									+ ") - Automated Stats Tracking for Hearthstone enthusiasts!");
		}
		
		shlHearthtracker.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		TabFolder tabFolder = new TabFolder(shlHearthtracker, SWT.NONE);
		
		TabItem tbtmDashboard = new TabItem(tabFolder, SWT.NONE);
		tbtmDashboard.setText("&Overview");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmDashboard.setControl(composite);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite, SWT.NONE);
		sashForm.setLayoutData(new RowData(598, 373));
		
		grpStats = new Group(sashForm, SWT.NONE);
		grpStats.setText("Stats");
		grpStats.setLayout(new FormLayout());
		
		table = new Table(grpStats, SWT.FULL_SELECTION);
		table.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		table.setLinesVisible(true);
		table.setHeaderVisible(true);
		FormData fd_table = new FormData();
		fd_table.bottom = new FormAttachment(100, -10);
		fd_table.left = new FormAttachment(0, 13);
		fd_table.right = new FormAttachment(100, -13);
		fd_table.top = new FormAttachment(0, 48);
		table.setLayoutData(fd_table);
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.CENTER);
		tblclmnNewColumn.setWidth(29);
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_1.setWidth(55);
		tblclmnNewColumn_1.setText("Wins");
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_2.setWidth(55);
		tblclmnNewColumn_2.setText("Losses");
		
		TableColumn tblclmnWin = new TableColumn(table, SWT.RIGHT);
		tblclmnWin.setWidth(55);
		tblclmnWin.setText("Win %");
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_3.setWidth(55);
		tblclmnNewColumn_3.setText("7+ %");
		
		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_4.setWidth(72);
		tblclmnNewColumn_4.setText("Total Runs");
		
		cmbStatsMode = new Combo(grpStats, SWT.READ_ONLY);
		cmbStatsMode.setItems(new String[] {"Arena mode (played as)", "Ranked mode (played as)", "Unranked mode (played as)", "Challenge mode (played as)", "Practice mode (played as)"});
		FormData fd_cmbStatsMode = new FormData();
		fd_cmbStatsMode.top = new FormAttachment(0, 10);
		fd_cmbStatsMode.left = new FormAttachment(0, 10);
		cmbStatsMode.setLayoutData(fd_cmbStatsMode);
		cmbStatsMode.select(0);
		
		grpCurrentStats = new Group(sashForm, SWT.NONE);
		grpCurrentStats.setText("Sideboard");
		grpCurrentStats.setLayout(null);
		
		styledTextStatus = new StyledText(grpCurrentStats, SWT.READ_ONLY);
		styledTextStatus.setBounds(10, 23, 211, 340);
		styledTextStatus.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		sashForm.setWeights(new int[] {365, 230});
		GridData gd_lblNewLabel_15 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_15.widthHint = 60;
		
		TabItem tbtmRecords = new TabItem(tabFolder, SWT.NONE);
		tbtmRecords.setText("&Matches");
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmRecords.setControl(composite_2);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_1 = new SashForm(composite_2, SWT.NONE);
		sashForm_1.setLayoutData(new RowData(598, 373));
		
		matchesTable = new Table(sashForm_1, SWT.FULL_SELECTION);
		matchesTable.setLinesVisible(true);
		matchesTable.setHeaderVisible(true);
		matchesTable.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tblclmnAs = new TableColumn(matchesTable, SWT.NONE);
		tblclmnAs.setText("As");
		tblclmnAs.setWidth(29);
		
		TableColumn tblclmnVs = new TableColumn(matchesTable, SWT.NONE);
		tblclmnVs.setWidth(29);
		tblclmnVs.setText("Vs");
		
		TableColumn tblclmnMode = new TableColumn(matchesTable, SWT.NONE);
		tblclmnMode.setWidth(68);
		tblclmnMode.setText("Mode");
		
		TableColumn tblclmnResult = new TableColumn(matchesTable, SWT.NONE);
		tblclmnResult.setWidth(45);
		tblclmnResult.setText("Result");
		
		TableColumn tblclmnOn = new TableColumn(matchesTable, SWT.NONE);
		tblclmnOn.setWidth(88);
		tblclmnOn.setText("On");
		
		tabFolder_1 = new TabFolder(sashForm_1, SWT.NONE);
		
		tbtmMatchesEdit = new TabItem(tabFolder_1, SWT.NONE);
		tbtmMatchesEdit.setText("&Edit");
		
		tbtmMatchesNew = new TabItem(tabFolder_1, SWT.NONE);
		tbtmMatchesNew.setText("&New");
		
		Composite composite_5  = new Composite(tabFolder_1, SWT.NONE);
		tbtmMatchesNew.setControl(composite_5);
		composite_5.setLayout(new FormLayout());
		
		TabItem tbtmArena = new TabItem(tabFolder, 0);
		tbtmArena.setText("&Arena");
		
		Composite composite_8 = new Composite(tabFolder, SWT.NONE);
		tbtmArena.setControl(composite_8);
		composite_8.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_2 = new SashForm(composite_8, SWT.NONE);
		sashForm_2.setLayoutData(new RowData(598, 373));
		
		arenaTable = new Table(sashForm_2, SWT.FULL_SELECTION);
		arenaTable.setLinesVisible(true);
		arenaTable.setHeaderVisible(true);
		arenaTable.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tblclmnAs_1 = new TableColumn(arenaTable, SWT.NONE);
		tblclmnAs_1.setText("As");
		tblclmnAs_1.setWidth(29);
		
		TableColumn tableColumn_3 = new TableColumn(arenaTable, SWT.NONE);
		tableColumn_3.setWidth(45);
		tableColumn_3.setText("Result");
		
		TableColumn tableColumn_4 = new TableColumn(arenaTable, SWT.NONE);
		tableColumn_4.setWidth(88);
		tableColumn_4.setText("On");
		
		tabFolder_2 = new TabFolder(sashForm_2, SWT.NONE);
		
		tbtmArenaEdit = new TabItem(tabFolder_2, 0);
		tbtmArenaEdit.setText("&Edit");
		
		TabItem tbtmArenaNew = new TabItem(tabFolder_2, 0);
		tbtmArenaNew.setText("&New");
		
		TabItem tbtmDeck = new TabItem(tabFolder, SWT.NONE);
		tbtmDeck.setText("De&cks");
		
		Composite composite_6 = new Composite(tabFolder, SWT.NONE);
		tbtmDeck.setControl(composite_6);
		composite_6.setLayout(new GridLayout(7, false));
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck = new Label(composite_6, SWT.NONE);
		lblDeck.setText("Slot #1");
		
		Label lblNewLabel_17 = new Label(composite_6, SWT.NONE);
		
		Label lblDeck_1 = new Label(composite_6, SWT.NONE);
		lblDeck_1.setText("Slot #2");
		
		Label lblNewLabel_20 = new Label(composite_6, SWT.NONE);
		
		Label lblDeck_2 = new Label(composite_6, SWT.NONE);
		lblDeck_2.setText("Slot #3");
		
		Label label_3 = new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[0] = text_1 = new Text(composite_6, SWT.BORDER);
		text_1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[1] = text_2 = new Text(composite_6, SWT.BORDER);
		text_2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[2] = text_3 = new Text(composite_6, SWT.BORDER);
		text_3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck1 = new Label(composite_6, SWT.NONE);
		lblDeck1.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[0] = lblDeck1;
		lblDeck1.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck2 = new Label(composite_6, SWT.NONE);
		lblDeck2.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[1] = lblDeck2;
		lblDeck2.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck3 = new Label(composite_6, SWT.NONE);
		lblDeck3.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[2] = lblDeck3;
		lblDeck3.setText("......");
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_18 = new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblDeckdsds = new Label(composite_6, SWT.NONE);
		lblDeckdsds.setText("Slot #4");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeckxx= new Label(composite_6, SWT.NONE);
		lblDeckxx.setText("Slot #5");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeckss = new Label(composite_6, SWT.NONE);
		lblDeckss.setText("Slot #6");
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[3] = text_4 = new Text(composite_6, SWT.BORDER);
		text_4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[4] = text_5 = new Text(composite_6, SWT.BORDER);
		text_5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[5] = text_6 = new Text(composite_6, SWT.BORDER);
		text_6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck4 = new Label(composite_6, SWT.NONE);
		lblDeck4.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[3] = lblDeck4;
		lblDeck4.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck5 = new Label(composite_6, SWT.NONE);
		lblDeck5.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[4] = lblDeck5;
		lblDeck5.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck6 = new Label(composite_6, SWT.NONE);
		lblDeck6.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[5] = lblDeck6;
		lblDeck6.setText("......");
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_19 = new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck_6 = new Label(composite_6, SWT.NONE);
		lblDeck_6.setText("Slot #7");
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_15 = new Label(composite_6, SWT.NONE);
		lblNewLabel_15.setText("Slot #8");
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_16 = new Label(composite_6, SWT.NONE);
		lblNewLabel_16.setText("Slot #9");
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[6] = text_7 = new Text(composite_6, SWT.BORDER);
		text_7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[7] = text_8 = new Text(composite_6, SWT.BORDER);
		text_8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		
		txtDecks[8] = text_9 = new Text(composite_6, SWT.BORDER);
		text_9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck7 = new Label(composite_6, SWT.NONE);
		lblDeck7.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[6] = lblDeck7;
		lblDeck7.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck8 = new Label(composite_6, SWT.NONE);
		lblDeck8.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[7] = lblDeck8;
		lblDeck8.setText("......");
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck9 = new Label(composite_6, SWT.NONE);
		lblDeck9.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		lblDecks[8] = lblDeck9;
		lblDeck9.setText("......");
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		Link link_2 = new Link(composite_6, SWT.NONE);
		link_2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://blog.hearthtracking.com/2014/01/how-to-use-decks-builder.html").toURI());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		link_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 5, 1));
		link_2.setText("<a>How to use the Decks Manager</a>");
		new Label(composite_6, SWT.NONE);
			
		TabItem tbtmPerferences = new TabItem(tabFolder, SWT.NONE);
		tbtmPerferences.setText("&Preferences");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmPerferences.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		Group grpGeneral = new Group(composite_1, SWT.NONE);
		grpGeneral.setText("General");
		grpGeneral.setLayout(null);
		GridData gd_grpGeneral = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_grpGeneral.heightHint = 134;
		gd_grpGeneral.widthHint = 585;
		grpGeneral.setLayoutData(gd_grpGeneral);
		
		Label lblNewLabel_1 = new Label(grpGeneral, SWT.NONE);
		lblNewLabel_1.setBounds(134, 25, 92, 15);
		lblNewLabel_1.setText("Game Language");
		
		cmbGameLang = new CCombo(grpGeneral, SWT.BORDER | SWT.READ_ONLY);
		cmbGameLang.setBounds(232, 22, 150, 21);
		cmbGameLang.setEditable(false);
		cmbGameLang.setItems(new String[] {});
		cmbGameLang.setVisibleItemCount(13);
		
		Link link = new Link(grpGeneral, SWT.NONE);
		link.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://blog.hearthtracking.com/2014/01/how-to-use-web-sync-function.html").toURI());
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		link.setBounds(63, 80, 163, 15);
		link.setText("<a>HearthTracker Web Sync Key</a>");
		
		txtWebSyncKey = new Text(grpGeneral, SWT.BORDER);
		txtWebSyncKey.setBounds(232, 78, 324, 21);
		
		Label lblNewLabel_14 = new Label(grpGeneral, SWT.NONE);
		lblNewLabel_14.setBounds(144, 53, 81, 15);
		lblNewLabel_14.setText("Batte.net server");
		
		cbServer = new CCombo(grpGeneral, SWT.BORDER | SWT.READ_ONLY);
		cbServer.setVisibleItemCount(4);
		cbServer.setItems(new String[] {"NA", "EU", "Asia", "China"});
		cbServer.setEditable(false);
		cbServer.setBounds(231, 49, 150, 21);
		
		Group grpAdvanced = new Group(composite_1, SWT.NONE);
		grpAdvanced.setText("Advanced");
		grpAdvanced.setLayout(null);
		GridData gd_grpAdvanced = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_grpAdvanced.heightHint = 194;
		gd_grpAdvanced.widthHint = 585;
		grpAdvanced.setLayoutData(gd_grpAdvanced);
		
		Label lblDetect = new Label(grpAdvanced, SWT.NONE);
		lblDetect.setBounds(71, 21, 156, 15);
		lblDetect.setText("Auto Detect Game Resolution");
		
		btnAutoDetectGameRes = new Button(grpAdvanced, SWT.CHECK);
		btnAutoDetectGameRes.setBounds(239, 20, 183, 16);
		btnAutoDetectGameRes.setToolTipText("It is recommended to enable it specially your desktop is running at same resolution with your Hearthstone resolution.");
		btnAutoDetectGameRes.setText("Enable");
		
		Label lblGameResolution = new Label(grpAdvanced, SWT.NONE);
		lblGameResolution.setBounds(24, 48, 211, 15);
		lblGameResolution.setText("Game Resolution (if auto detect failed)");
		
		cmbGameRes = new CCombo(grpAdvanced, SWT.BORDER | SWT.READ_ONLY);
		cmbGameRes.setBounds(239, 42, 150, 21);
		
		Label lblNewLabel = new Label(grpAdvanced, SWT.NONE);
		lblNewLabel.setBounds(167, 156, 60, 15);
		lblNewLabel.setText("Scan Speed");
		
		Label lblNewLabel_2 = new Label(grpAdvanced, SWT.NONE);
		lblNewLabel_2.setBounds(185, 129, 42, 15);
		lblNewLabel_2.setText("Scanner");
		
		btnEnableScanner = new Button(grpAdvanced, SWT.CHECK);
		btnEnableScanner.setBounds(241, 128, 56, 16);
		btnEnableScanner.setSelection(true);
		btnEnableScanner.setText("Enable");
		
		Button button_0 = new Button(grpAdvanced, SWT.RADIO);
		button_0.setBounds(239, 155, 75, 16);
		btnScanSpeed[3] = button_0;
		button_0.setText("Really Fast");
		
		Button button = new Button(grpAdvanced, SWT.RADIO);
		button.setBounds(321, 156, 42, 16);
		btnScanSpeed[0] = button;
		button.setText("Fast");
		
		Button button_1 = new Button(grpAdvanced, SWT.RADIO);
		button_1.setBounds(368, 156, 85, 16);
		btnScanSpeed[1] = button_1;
		button_1.setText("Intermediate");
		
		Button button_2 = new Button(grpAdvanced, SWT.RADIO);
		button_2.setBounds(459, 156, 46, 16);
		btnScanSpeed[2] = button_2;
		button_2.setText("Slow");
		
//		Button button_3 = new Button(grpAdvanced, SWT.RADIO);
//		button_3.setBounds(379, 155, 46, 16);
//		btnScanSpeed[3] = button_3;
//		button_3.setText("Slowest");
		
		Label lblNewLabel_11 = new Label(grpAdvanced, SWT.NONE);
		lblNewLabel_11.setBounds(10, 78, 225, 15);
		lblNewLabel_11.setText("Override Offsets (Multi monitors dirty fix)");
		
		spXOffset = new Spinner(grpAdvanced, SWT.BORDER);
		spXOffset.setMaximum(10240);
		spXOffset.setMinimum(-10240);
		spXOffset.setBounds(288, 75, 75, 22);
		
		spYOffset = new Spinner(grpAdvanced, SWT.BORDER);
		spYOffset.setMaximum(10240);
		spYOffset.setMinimum(-10240);
		spYOffset.setBounds(442, 75, 75, 22);
		
		Label lblNewLabel_12 = new Label(grpAdvanced, SWT.NONE);
		lblNewLabel_12.setBounds(241, 78, 42, 15);
		lblNewLabel_12.setText("X offset");
		
		Label lblYOffset = new Label(grpAdvanced, SWT.NONE);
		lblYOffset.setText("Y offset");
		lblYOffset.setBounds(398, 78, 42, 15);
		
		Label lblNewLabel_13 = new Label(grpAdvanced, SWT.NONE);
		lblNewLabel_13.setBounds(23, 187, 204, 15);
		lblNewLabel_13.setText("Force Scanning even if HS is not found");
		
		btnAlwaysScan = new Button(grpAdvanced, SWT.CHECK);
		btnAlwaysScan.setBounds(239, 186, 93, 16);
		btnAlwaysScan.setText("Enable");
		
		TabItem tbtmDiagnostics = new TabItem(tabFolder, SWT.NONE);
		tbtmDiagnostics.setText("&Tools");
		
		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmDiagnostics.setControl(composite_4);
		composite_4.setLayout(new GridLayout(1, false));
		
		Group grpDiagnostics = new Group(composite_4, SWT.NONE);
		GridData gd_grpDiagnostics = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpDiagnostics.heightHint = 353;
		gd_grpDiagnostics.widthHint = 589;
		grpDiagnostics.setLayoutData(gd_grpDiagnostics);
		grpDiagnostics.setText("Diagnostics");
		grpDiagnostics.setLayout(null);
		
		Label lblLastSeenLabel = new Label(grpDiagnostics, SWT.NONE);
		lblLastSeenLabel.setBounds(206, 21, 48, 15);
		lblLastSeenLabel.setText("Last seen");
		
		lblLastSeen = new Label(grpDiagnostics, SWT.NONE);
		lblLastSeen.setBounds(260, 21, 300, 15);
		lblLastSeen.setText("..........");
		
		Label lblLastScanCoordinate = new Label(grpDiagnostics, SWT.NONE);
		lblLastScanCoordinate.setBounds(181, 41, 73, 15);
		lblLastScanCoordinate.setText("Last scan area");
		
		lblLastscreencoordinate = new Label(grpDiagnostics, SWT.NONE);
		lblLastscreencoordinate.setBounds(260, 41, 300, 15);
		lblLastscreencoordinate.setText("..........");
		
		Label lblNewLabel_5 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_5.setBounds(157, 61, 97, 15);
		lblNewLabel_5.setText("Last scan sub-area");
		
		lblLastScanSubArea = new Label(grpDiagnostics, SWT.NONE);
		lblLastScanSubArea.setBounds(260, 61, 300, 15);
		lblLastScanSubArea.setText("..........");
		
		Label lblAutoPingLabel = new Label(grpDiagnostics, SWT.NONE);
		lblAutoPingLabel.setBounds(132, 113, 117, 15);
		lblAutoPingLabel.setText("Visualize scanned area");
		
		btnAutoPing = new Button(grpDiagnostics, SWT.CHECK);
		btnAutoPing.setBounds(255, 112, 56, 16);
		btnAutoPing.setToolTipText("Visualize scanned areas after Hearthstone being out of sight for more than 1 min.");
		btnAutoPing.setText("Enable");
		
		Label lblNewLabel_6 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_6.setBounds(171, 138, 78, 15);
		lblNewLabel_6.setText("Diagnotic Tool");
		
		btnVisualizeNow = new Button(grpDiagnostics, SWT.NONE);
		btnVisualizeNow.setBounds(255, 133, 83, 25);
		btnVisualizeNow.setText("Visualize now");
		
		Label label_2 = new Label(grpDiagnostics, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_2.setBounds(10, 93, 575, 2);
		
		TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
		tbtmAbout.setText("A&bout");
		
		Group grpAbout = new Group(tabFolder, SWT.NONE);
		tbtmAbout.setControl(grpAbout);
		grpAbout.setText("About");
		grpAbout.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite composite_3 = new Composite(grpAbout, SWT.NONE);
		composite_3.setLayout(null);
		
		Label lblVersion = new Label(composite_3, SWT.NONE);
		lblVersion.setBounds(42, 132, 108, 15);
		lblVersion.setText("HearthTracker v" + version[0] + "." + version[1] + "." + version[2]);
		
		Label lblCopyrightc = new Label(composite_3, SWT.NONE);
		lblCopyrightc.setBounds(24, 153, 150, 15);
		lblCopyrightc.setText("Copyright \u00A9 2013 megablue");
		
		Label lblNewLabel_7 = new Label(composite_3, SWT.NONE);
		lblNewLabel_7.setBounds(5, 65, 0, 15);
		lblNewLabel_7.setText("");
		
		Label lblNewLabel_8 = new Label(composite_3, SWT.NONE);
		lblNewLabel_8.setBounds(5, 209, 0, 15);
		lblNewLabel_8.setText("");
		
		Label lblPaypal = new Label(composite_3, SWT.NONE);
		lblPaypal.setBounds(236, 258, 153, 96);
		lblPaypal.setText("");
		lblPaypal.setToolTipText("Your support means a lot to me. Thank you for even hovering over the donate button!");
		lblPaypal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=YNFGYE9V386UQ").toURI());
				}catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		});
		lblPaypal.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "paypal.png" ));
		
		StyledText styledText = new StyledText(composite_3, SWT.READ_ONLY | SWT.WRAP);
		styledText.setBounds(208, 31, 382, 183);
		styledText.setText("HearthTracker is designed specifically to automate and ease score tracking for Hearthstone enthusiasts. It is coded by megablue. He first created the prototype to display arena score on his stream. Later, realizing it might help a lot of players and streamers, he continued to add new features and refine the code. He still has a lot of interesting ideas that are yet to be implemented. A lot of time and efforts need to be invested into it in order to implement all the exciting features. He hopes that you can show your support by donating. Your support will be greatly appreciated and keep the project alive!");
		
		Link link_1 = new Link(composite_3, SWT.NONE);
		link_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://bit.ly/HearthTracking").toURI());
				} catch (Throwable e) {
					//.printStackTrace();
				}
			}
		});
		link_1.setBounds(32, 176, 150, 15);
		link_1.setText("<a>www.HearthTracking.com</a>");
		
		Label lblFacebook = new Label(composite_3, SWT.NONE);
		lblFacebook.setBounds(10, 258, 96, 96);
		lblFacebook.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "facebook.png" ));
		
		lblFacebook.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://bit.ly/hearthtrackingfacebook").toURI());
				} catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		});
		
		Label lblTLQ = new Label(composite_3, SWT.NONE);
		lblTLQ.setBounds(124, 258, 96, 96);
		lblTLQ.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "teamliquid.png" ));
		
		lblTLQ.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://bit.ly/teamliquid").toURI());
				} catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		});
		
		Label lblTwitter = new Label(composite_3, SWT.NONE);
		lblTwitter.setBounds(407, 258, 96, 96);
		lblTwitter.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "twitter.png" ));
		
		lblTwitter.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://bit.ly/hearthtrackingtwitter").toURI());
				} catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		});
		
		Label lblFindUsOn = new Label(composite_3, SWT.NONE);
		lblFindUsOn.setBounds(10, 237, 96, 15);
		lblFindUsOn.setText("Show us the love!");
		
		Label lblNewLabel_10 = new Label(composite_3, SWT.NONE);
		lblNewLabel_10.setBounds(208, 10, 96, 15);
		lblNewLabel_10.setText("How it all began...");
		
		Label label_1 = new Label(composite_3, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_1.setBounds(10, 222, 580, 2);
		
		Label label = new Label(composite_3, SWT.NONE);
		label.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo.png" ));
		label.setBounds(46, 31, 96, 96);

		shlHearthtracker.setTabList(new Control[]{tabFolder});

		sashForm_1.setWeights(new int[] {277, 318});
		sashForm_2.setWeights(new int[] {277, 318});
		
		createMatchesEditForm(new Composite(tabFolder_1, SWT.NONE), tbtmMatchesEdit);
		createMatchesNewForm(new Composite(tabFolder_1, SWT.NONE), 	tbtmMatchesNew);
		createArenaEditForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaEdit);
		createArenaNewForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaNew);
		
		poppulatesOffsetOptions();
		poppulateScannerOptions();
		poppulateGeneralPerferences();
		poppulateGameServer();
		poppulateResolutions();
		poppulateDiagnoticsControls();
		poppulateDiagnoticsStatus();
		updateStatus();
		fillOverviewTable();
		fillMatchesTable();
		fillArenaTable();
		setupModeSelection();
		decksManager();
	}
	
	private void decksManager(){
		FocusAdapter deckFocus = new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				Text deck = (Text) arg0.getSource();
				int index = (int) deck.getData("index");
				String input = deck.getText().trim();
				System.out.println("Slot #" + index + " lost focus");

				if(!input.toLowerCase().equals(decks.list[index].toLowerCase())){
					decks.list[index] = input;
					saveDecks();
					fillDeckWinRate();
				}
			}
		};
		
		for(int i = 0; i < txtDecks.length; i++){
			txtDecks[i].setText(decks.list[i]);
			txtDecks[i].setData("index", i);
			txtDecks[i].addFocusListener(deckFocus);
			lblDecks[i].setText("");
		}
		
		fillDeckWinRate();
	}
	
	private void fillDeckWinRate(){
		for(int i = 0; i < txtDecks.length; i++){
			String deckName = decks.list[i];
			try {
				float ranked = tracker.getWinRateByDeck(HearthReader.RANKEDMODE, deckName);
				float unranked = tracker.getWinRateByDeck(HearthReader.UNRANKEDMODE, deckName);
				float challenge = tracker.getWinRateByDeck(HearthReader.CHALLENGEMODE, deckName);
				float practice = tracker.getWinRateByDeck(HearthReader.PRACTICEMODE, deckName);
				
				String rankedS = ranked > -1 ? new DecimalFormat("0.00").format(ranked) + "%" : "-\t";
				String unrankedS = unranked > -1 ? new DecimalFormat("0.00").format(unranked) + "%" : "-";
				String challengeS = challenge > -1 ? new DecimalFormat("0.00").format(challenge) + "%" : "-\t";
				String practiceS = practice > -1 ? new DecimalFormat("0.00").format(practice) + "%" : "-";

				lblDecks[i].setText(
						"R: " + rankedS 
					   +"\tU: " + unrankedS + "\r\n"
					   +"C: " + challengeS
					   +"\tP: " + practiceS + "\r\n" 
				);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void fillMatchesTable(){
		try {
			TableItem[] tis = matchesTable.getSelection();
			int selectedID = -1;
			
			if(tis.length > 0){
				selectedID = (int) tis[0].getData("id");
			}
			
			matchesTable.removeAll();
			
			ResultSet rs = tracker.getMatches();
			Calendar cal = Calendar.getInstance();

			while(rs.next()){
				TableItem tableItem = new TableItem(matchesTable, SWT.NONE);
				String result = "Unknown";
				cal.setTime(new Date(rs.getLong("STARTTIME")));
				
				if(rs.getInt("WIN") == 1){
					result = "Win";
				} else if(rs.getInt("WIN") == 0){
					result = "Lose";
				}
				
				tableItem.setData("id", rs.getInt("ID"));
				tableItem.setImage(0, heroImgs[rs.getInt("MYHEROID")+1]);
				tableItem.setImage(1, heroImgs[rs.getInt("OPPHEROID")+1]);
				tableItem.setText(2, HearthReader.gameModeToString(rs.getInt("MODE")));
				tableItem.setText(3, result);
				tableItem.setText(4, (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR));
				
				if(selectedID == rs.getInt("ID")){
					matchesTable.setSelection(tableItem);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void fillArenaTable(){
		try {
			TableItem[] tis = arenaTable.getSelection();
			int selectedID = -1;
			
			if(tis.length > 0){
				selectedID = (int) tis[0].getData("id");
			}
			
			arenaTable.removeAll();
			
			ResultSet rs = tracker.getArenaResults();
			Calendar cal = Calendar.getInstance();

			while(rs.next()){
				TableItem tableItem = new TableItem(arenaTable, SWT.NONE);
				String result = rs.getInt("WINS") + "-" + rs.getInt("LOSSES");
				cal.setTime(new Date(rs.getLong("TIMECAPTURED")));
				
				tableItem.setData("id", rs.getInt("ID"));
				tableItem.setImage(0, heroImgs[rs.getInt("HEROID")+1]);
				tableItem.setText(1, result);
				tableItem.setText(2, (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR));
				
				if(selectedID == rs.getInt("ID")){
					arenaTable.setSelection(tableItem);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createMatchesEditForm(Composite composite_4, TabItem tabitem){
		tabitem.setControl(composite_4);
		composite_4.setLayout(null);
		final Combo cbMatchesEditAs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditAs.setBounds(48, 66, 90, 23);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		lblNewLabel_3.setBounds(154, 69, 11, 15);
		lblNewLabel_3.setText("vs");
		
		final Combo cbMatchesEditVs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditVs.setBounds(184, 66, 90, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbMatchesEditAs.add(heroesList.getHeroLabel(i));
			cbMatchesEditVs.add(heroesList.getHeroLabel(i));
		}
		
		Label lblNewLabel_4 = new Label(composite_4, SWT.NONE);
		lblNewLabel_4.setBounds(148, 101, 26, 15);
		lblNewLabel_4.setText("Goes");
		
		final Combo cbMatchesEditGoes = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGoes.setBounds(125, 123, 74, 23);
		cbMatchesEditGoes.setItems(new String[] {"First", "Second", "Unknown"});
		
		final Combo cbMatchesEditGameMode = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGameMode.setBounds(109, 25, 99, 23);
		cbMatchesEditGameMode.setItems(new String[] {"Unknown mode", "Arena", "Ranked", "Unranked", "Challenge", "Practice"});
		
		final Combo cbMatchesEditResult = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditResult.setBounds(136, 165, 49, 23);
		cbMatchesEditResult.setItems(new String[] {"Win", "Loss", "Unknown"});
		
		final DateTime dtMatchesEditDate = new DateTime(composite_4, SWT.NONE);
		dtMatchesEditDate.setEnabled(false);
		dtMatchesEditDate.setBounds(69, 211, 80, 24);
		
		final DateTime dtMatchesEditTime = new DateTime(composite_4, SWT.TIME);
		dtMatchesEditTime.setEnabled(false);
		dtMatchesEditTime.setBounds(169, 211, 86, 24);
		
		final Spinner spMatchesEditMinute = new Spinner(composite_4, SWT.BORDER);
		spMatchesEditMinute.setBounds(148, 254, 44, 22);
		
		Label lblNewLabel_9 = new Label(composite_4, SWT.NONE);
		lblNewLabel_9.setBounds(88, 257, 35, 15);
		lblNewLabel_9.setText("Played");
		
		Label lblMinutes = new Label(composite_4, SWT.NONE);
		lblMinutes.setBounds(212, 257, 43, 15);
		lblMinutes.setText("minutes");
		
		final Button btnMatchesEditSave = new Button(composite_4, SWT.NONE);
		btnMatchesEditSave.setEnabled(false);
		btnMatchesEditSave.setBounds(88, 310, 36, 25);
		btnMatchesEditSave.setText("&Save");

		final Button btnMatchesEditDelete = new Button(composite_4, SWT.NONE);
		btnMatchesEditDelete.setEnabled(false);
		btnMatchesEditDelete.setBounds(212, 310, 45, 25);
		btnMatchesEditDelete.setText("&Delete");
		
		final Label lblID = new Label(composite_4, SWT.NONE);
		lblID.setBounds(10, 10, 303, 15);
		lblID.setText("ID #00000");
		lblID.setData("id", 0);
		
		final Button btnConfirmChanges = new Button(composite_4, SWT.CHECK);
		btnConfirmChanges.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				boolean confirmed = btnConfirmChanges.getSelection();
				
				if(confirmed){
					btnMatchesEditDelete.setEnabled(true);
					btnMatchesEditSave.setEnabled(true);
				} else {
					btnMatchesEditDelete.setEnabled(false);
					btnMatchesEditSave.setEnabled(false);
				}
			}
		});
		btnConfirmChanges.setBounds(134, 293, 65, 16);
		btnConfirmChanges.setText("&Confirm");
		
		matchesTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] titems = matchesTable.getSelection();
				
				if(titems.length > 0){
					int id = (int) titems[0].getData("id");
					lblID.setText("ID #" + id);
					lblID.setData("id", id);
					tabFolder_1.setSelection(0);
					
					ResultSet rs;
					try {
						rs = tracker.getMatch(id);
						
						if(rs.next()){
							int myheroid = rs.getInt("MYHEROID");
							int oppheroid = rs.getInt("OPPHEROID");
							int goes = rs.getInt("GOESFIRST");
							int win	= rs.getInt("WIN");				
							int totaltime = rs.getInt("TOTALTIME") / 60;
							int gameMode = rs.getInt("MODE");
							
							Date startdate = new Date(rs.getLong("STARTTIME"));
							Calendar calDate = Calendar.getInstance();
							calDate.setTime(startdate);
							
							if(myheroid == -1){
								cbMatchesEditAs.select(0);
							}else{
								cbMatchesEditAs.select(myheroid + 1);
							}
							
							if(oppheroid == -1){
								cbMatchesEditVs.select(0);
							} else {
								cbMatchesEditVs.select(oppheroid + 1);
							}
							
							if(goes == 1){
								cbMatchesEditGoes.select(0);
							} else if(goes == 0){
								cbMatchesEditGoes.select(1);
							} else {
								cbMatchesEditGoes.select(2);
							}
							
							if(gameMode > 0){
								cbMatchesEditGameMode.select(gameMode);
							} else {
								cbMatchesEditGameMode.select(0);
							}
							
							if(win == 1){
								cbMatchesEditResult.select(0);
							}else if (win == 0){
								cbMatchesEditResult.select(1);
							}else {
								cbMatchesEditResult.select(2);
							}
							
							spMatchesEditMinute.setSelection(totaltime);
						
							dtMatchesEditDate.setYear(calDate.get(Calendar.YEAR));
							dtMatchesEditDate.setMonth(calDate.get(Calendar.MONTH));
							dtMatchesEditDate.setDay(calDate.get(Calendar.DAY_OF_MONTH));
	
							dtMatchesEditTime.setHours(calDate.get(Calendar.HOUR_OF_DAY));
							dtMatchesEditTime.setMinutes(calDate.get(Calendar.MINUTE));
							dtMatchesEditTime.setSeconds(calDate.get(Calendar.SECOND));
							
							btnConfirmChanges.setSelection(false);
							btnMatchesEditDelete.setEnabled(false);
							btnMatchesEditSave.setEnabled(false);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}

				}
			}
		});
		
		
		btnMatchesEditSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				int gMode = cbMatchesEditGameMode.getSelectionIndex();
				int myheroid = cbMatchesEditAs.getSelectionIndex() - 1;
				int oppheroid = cbMatchesEditVs.getSelectionIndex() - 1;
				int goes = -1;
				int result = -1;
				int totaltime = spMatchesEditMinute.getSelection() * 60;
				
				if(id == 0){
					return;
				}
				
				if(cbMatchesEditResult.getSelectionIndex() == 0){
					result = 1;
				} if(cbMatchesEditResult.getSelectionIndex() == 1){
					result = 0;
				} else {
					result = -1;
				}
				
				if(cbMatchesEditGoes.getSelectionIndex() == 0){
					goes = 1;
				} else if (cbMatchesEditGoes.getSelectionIndex() == 1){
					goes = 0;
				}
				
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 	dtMatchesEditDate.getDay());
				cal.set(Calendar.MONTH, 		dtMatchesEditDate.getMonth());
				cal.set(Calendar.YEAR, 			dtMatchesEditDate.getYear());
				cal.set(Calendar.HOUR_OF_DAY, 	dtMatchesEditTime.getHours());
				cal.set(Calendar.MINUTE, 		dtMatchesEditTime.getMinutes());
				cal.set(Calendar.SECOND, 		dtMatchesEditTime.getSeconds());
				
				Long starttime = cal.getTime().getTime(); 
				
				try {
					btnMatchesEditSave.setEnabled(false);
					btnMatchesEditSave.setText("Saving...");
					tracker.saveModifiedMatchResult(id, gMode, myheroid, oppheroid, goes, result, totaltime);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditSave.setEnabled(true);
				btnMatchesEditSave.setText("&Save");
			}
		});
		
		btnMatchesEditDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				try {
					btnMatchesEditDelete.setEnabled(false);
					btnMatchesEditDelete.setText("Deleting...");
					tracker.deleteMatchResult(id);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditDelete.setEnabled(true);
				btnMatchesEditDelete.setText("&Delete");
			}
		});
	}
	
	private void createMatchesNewForm(Composite composite_4, TabItem tabitem){
		tabitem.setControl(composite_4);
		composite_4.setLayout(null);
		final Combo cbMatchesEditAs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditAs.setBounds(48, 66, 90, 23);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		lblNewLabel_3.setBounds(154, 69, 11, 15);
		lblNewLabel_3.setText("vs");
		
		final Combo cbMatchesEditVs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditVs.setBounds(184, 66, 90, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbMatchesEditAs.add(heroesList.getHeroLabel(i));
			cbMatchesEditVs.add(heroesList.getHeroLabel(i));
		}
		
		Label lblNewLabel_4 = new Label(composite_4, SWT.NONE);
		lblNewLabel_4.setBounds(148, 101, 26, 15);
		lblNewLabel_4.setText("Goes");
		
		final Combo cbMatchesEditGoes = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGoes.setBounds(125, 123, 74, 23);
		cbMatchesEditGoes.setItems(new String[] {"First", "Second", "Unknown"});
		
		final Combo cbMatchesEditGameMode = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGameMode.setBounds(109, 25, 99, 23);
		cbMatchesEditGameMode.setItems(new String[] {"Unknown mode", "Arena", "Ranked", "Unranked", "Challenge", "Practice"});
		
		final Combo cbMatchesEditResult = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditResult.setBounds(136, 165, 49, 23);
		cbMatchesEditResult.setItems(new String[] {"Win", "Loss", "Unknown"});
		
		final DateTime dtMatchesEditDate = new DateTime(composite_4, SWT.NONE);
		dtMatchesEditDate.setBounds(69, 211, 80, 24);
		
		final DateTime dtMatchesEditTime = new DateTime(composite_4, SWT.TIME);
		dtMatchesEditTime.setBounds(169, 211, 86, 24);
		
		final Spinner spMatchesEditMinute = new Spinner(composite_4, SWT.BORDER);
		spMatchesEditMinute.setBounds(148, 254, 44, 22);
		
		Label lblNewLabel_9 = new Label(composite_4, SWT.NONE);
		lblNewLabel_9.setBounds(88, 257, 35, 15);
		lblNewLabel_9.setText("Played");
		
		Label lblMinutes = new Label(composite_4, SWT.NONE);
		lblMinutes.setBounds(212, 257, 43, 15);
		lblMinutes.setText("minutes");
		
		final Button btnMatchesEditSave = new Button(composite_4, SWT.NONE);
		btnMatchesEditSave.setBounds(150, 310, 36, 25);
		btnMatchesEditSave.setText("&Save");
				
		btnMatchesEditSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int gMode = cbMatchesEditGameMode.getSelectionIndex();
				int myheroid = cbMatchesEditAs.getSelectionIndex() - 1;
				int oppheroid = cbMatchesEditVs.getSelectionIndex() - 1;
				int goes = cbMatchesEditGoes.getSelectionIndex() == 0 ? 1 : 0;
				int result = -1;
				int totaltime = spMatchesEditMinute.getSelection() * 60;
				
				if(cbMatchesEditResult.getSelectionIndex() == 0){
					result = 1;
				} else if(cbMatchesEditResult.getSelectionIndex() == 1){
					result = 0;
				}

				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 	dtMatchesEditDate.getDay());
				cal.set(Calendar.MONTH, 		dtMatchesEditDate.getMonth());
				cal.set(Calendar.YEAR, 			dtMatchesEditDate.getYear());
				cal.set(Calendar.HOUR_OF_DAY, 	dtMatchesEditTime.getHours());
				cal.set(Calendar.MINUTE, 		dtMatchesEditTime.getMinutes());
				
				long starttime = cal.getTime().getTime();
				
				try {
					btnMatchesEditSave.setEnabled(false);
					btnMatchesEditSave.setText("Saving...");
					tracker.saveMatchResult(gMode, myheroid, oppheroid, goes, result, starttime, totaltime, true, "");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditSave.setEnabled(true);
				btnMatchesEditSave.setText("&Save");
			}
		});
	}

	private void createArenaEditForm(Composite composite_4, TabItem tabitem){
		tabitem.setControl(composite_4);
		composite_4.setLayout(null);
		final Label lblID = new Label(composite_4, SWT.NONE);
		lblID.setBounds(10, 10, 348, 15);
		lblID.setText("ID #00000");
		lblID.setData("id", 0);
		
		final Combo cbArenaEditHero = new Combo(composite_4, SWT.READ_ONLY);
		cbArenaEditHero.setBounds(102, 31, 89, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbArenaEditHero.add(heroesList.getHeroLabel(i));
		}
		
		final DateTime dtArenaDate = new DateTime(composite_4, SWT.NONE);
		dtArenaDate.setEnabled(false);
		dtArenaDate.setBounds(59, 125, 80, 24);
		
		final DateTime dtArenaTime = new DateTime(composite_4, SWT.TIME);
		dtArenaTime.setEnabled(false);
		dtArenaTime.setBounds(156, 125, 86, 24);
		
		final Button btnArenaSave = new Button(composite_4, SWT.NONE);
		btnArenaSave.setEnabled(false);
		btnArenaSave.setBounds(59, 303, 44, 25);
		btnArenaSave.setText("&Save");
		
		final Button btnArenaDelete = new Button(composite_4, SWT.NONE);
		btnArenaDelete.setEnabled(false);
		btnArenaDelete.setBounds(197, 303, 45, 25);
		btnArenaDelete.setText("&Delete");
		
		final Spinner spEditArenaWins = new Spinner(composite_4, SWT.BORDER);
		spEditArenaWins.setMaximum(12);
		spEditArenaWins.setBounds(81, 78, 44, 22);
		
		final Spinner spEditArenaLosses = new Spinner(composite_4, SWT.BORDER);
		spEditArenaLosses.setMaximum(3);
		spEditArenaLosses.setBounds(171, 78, 44, 22);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		lblNewLabel_3.setBounds(145, 81, 20, 15);
		lblNewLabel_3.setText("-");
		
		
		final Button btnArenaConfirm = new Button(composite_4, SWT.CHECK);
		btnArenaConfirm.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
				if(btnArenaConfirm.getSelection()){
					btnArenaSave.setEnabled(true);
					btnArenaDelete.setEnabled(true);
				} else {
					btnArenaSave.setEnabled(false);
					btnArenaDelete.setEnabled(false);
				}
			}
		});
		btnArenaConfirm.setBounds(119, 169, 65, 16);
		btnArenaConfirm.setText("&Confirm");
		
		
		arenaTable.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] titems = arenaTable.getSelection();
				
				if(titems.length > 0){
					int id = (int) titems[0].getData("id");
					lblID.setText("ID #" + id);
					lblID.setData("id", id);
					tabFolder_2.setSelection(0);
					btnArenaConfirm.setSelection(false);
					btnArenaSave.setEnabled(false);
					btnArenaDelete.setEnabled(false);
					ResultSet rs;
					
					try {
						rs = tracker.getArenaResult(id);
						
						if(rs.next()){
							
							int heroid = rs.getInt("HEROID");
							int wins = rs.getInt("WINS");
							int losses = rs.getInt("LOSSES");
							
							Date startdate = new Date(rs.getLong("TIMECAPTURED"));
							Calendar calDate = Calendar.getInstance();
							calDate.setTime(startdate);

							if(heroid == -1){
								cbArenaEditHero.select(0);
							}else{
								cbArenaEditHero.select(heroid + 1);
							}
							
							spEditArenaWins.setSelection(wins);
							spEditArenaLosses.setSelection(losses);
							
							dtArenaDate.setYear(calDate.get(Calendar.YEAR));
							dtArenaDate.setMonth(calDate.get(Calendar.MONDAY));
							dtArenaDate.setDay(calDate.get(Calendar.DAY_OF_MONTH));
							
							dtArenaTime.setHours(calDate.get(Calendar.HOUR_OF_DAY));
							dtArenaTime.setMinutes(calDate.get(Calendar.MINUTE));
							dtArenaTime.setSeconds(calDate.get(Calendar.SECOND));
						}
						
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
			}
		});
		
		
		btnArenaSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				int heroid = cbArenaEditHero.getSelectionIndex() - 1;
				int wins = spEditArenaWins.getSelection();
				int losses = spEditArenaLosses.getSelection();
				
				if(id == 0){
					return;
				}
				
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 	dtArenaDate.getDay());
				cal.set(Calendar.MONTH, 		dtArenaDate.getMonth());
				cal.set(Calendar.YEAR, 			dtArenaDate.getYear());
				cal.set(Calendar.HOUR_OF_DAY, 	dtArenaTime.getHours());
				cal.set(Calendar.MINUTE, 		dtArenaTime.getMinutes());
				cal.set(Calendar.SECOND, 		dtArenaTime.getSeconds());
				
				Long time = cal.getTime().getTime(); 
				
				try {
					btnArenaSave.setEnabled(false);
					btnArenaSave.setText("Saving...");
					tracker.saveModifiedArenaResult(id, heroid, wins, losses);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
				btnArenaSave.setEnabled(true);
				btnArenaSave.setText("&Save");
			}
		});
		
		btnArenaDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				try {
					btnArenaDelete.setEnabled(false);
					btnArenaDelete.setText("Deleting...");
					tracker.deleteModifiedArenaResult(id);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
				btnArenaDelete.setText("&Delete");
				btnArenaDelete.setEnabled(true);
			}
		});
	}
	
	private void createArenaNewForm(Composite composite_4, TabItem tabitem){
		tabitem.setControl(composite_4);
		composite_4.setLayout(null);
		final Combo cbArenaEditHero = new Combo(composite_4, SWT.READ_ONLY);
		cbArenaEditHero.setBounds(113, 43, 89, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbArenaEditHero.add(heroesList.getHeroLabel(i));
		}
		
		final DateTime dtArenaDate = new DateTime(composite_4, SWT.NONE);
		dtArenaDate.setBounds(70, 137, 80, 24);
		
		final DateTime dtArenaTime = new DateTime(composite_4, SWT.TIME);
		dtArenaTime.setBounds(167, 137, 86, 24);
		
		final Button btnArenaSave = new Button(composite_4, SWT.NONE);
		btnArenaSave.setBounds(139, 310, 44, 25);
		btnArenaSave.setText("&Save");

		final Spinner spEditArenaWins = new Spinner(composite_4, SWT.BORDER);
		spEditArenaWins.setMaximum(12);
		spEditArenaWins.setBounds(92, 90, 44, 22);
		
		final Spinner spEditArenaLosses = new Spinner(composite_4, SWT.BORDER);
		spEditArenaLosses.setMaximum(3);
		spEditArenaLosses.setBounds(182, 90, 44, 22);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		lblNewLabel_3.setBounds(156, 93, 20, 15);
		lblNewLabel_3.setText("-");
	
		btnArenaSave.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int heroid = cbArenaEditHero.getSelectionIndex() - 1;
				int wins = spEditArenaWins.getSelection();
				int losses = spEditArenaLosses.getSelection();
				
				Calendar cal = Calendar.getInstance();
				cal.set(Calendar.DAY_OF_MONTH, 	dtArenaDate.getDay());
				cal.set(Calendar.MONTH, 		dtArenaDate.getMonth());
				cal.set(Calendar.YEAR, 			dtArenaDate.getYear());
				cal.set(Calendar.HOUR_OF_DAY, 	dtArenaTime.getHours());
				cal.set(Calendar.MINUTE, 		dtArenaTime.getMinutes());
				cal.set(Calendar.SECOND, 		dtArenaTime.getSeconds());
				
				Long time = cal.getTime().getTime(); 
				
				try {
					btnArenaSave.setEnabled(false);
					btnArenaSave.setText("Saving...");
					tracker.saveArenaResult(heroid, wins, losses, time, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
				btnArenaSave.setEnabled(true);
				btnArenaSave.setText("&Save");
			}
		});
	}
	
	private void savePreferences(){
		config.save(setting, "." + File.separator + "configs" + File.separator + "settings.xml");
	}
	
	private void saveDecks(){
		config.save(decks, "." + File.separator + "configs" + File.separator + "decks.xml");
	}
	
	private void poppulateDiagnoticsStatus(){
		Date lastSeen = hearth.getLastseen();
		int[] area = hearth.getLastScanArea();
		int[] subArea = hearth.getLastScanSubArea();
		String last = lastSeen.getTime() == 0 ? "Never" : HearthHelper.getPrettyText(lastSeen); 

		if(lastSeen == null){
			lblLastSeen.setText("N|A");
		} else {
			lblLastSeen.setText(last);
		}
		
		lblLastscreencoordinate.setText(area[0] + ", " + area[1] + ", w: " + area[2] + ", h: " + area[3]);
		lblLastScanSubArea.setText(subArea[0] + ", " + subArea[1] + ", w: " + subArea[2] + ", h: " + subArea[3]);
	}
	
	private void poppulateDiagnoticsControls(){
		btnAutoPing.setSelection(setting.autoPing);
		
		btnAutoPing.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.autoPing = btnAutoPing.getSelection();
				hearth.setAutoPing(setting.autoPing);
				savePreferences();
			}
		});
		
		btnVisualizeNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				hearth.pingHearthstone();
			}
		});
	}
	
	private void poppulateResolutions(){
		cmbGameRes.setVisibleItemCount(gameResolutions.resolutions.length);
		int selected = -1;
		
		for(int i = 0; i < gameResolutions.resolutions.length; i++)
		{
			String res = gameResolutions.resolutions[i].width + "x" + gameResolutions.resolutions[i].height;
			cmbGameRes.add(res);
			cmbGameRes.setData(res, gameResolutions.resolutions[i]);
			
			if(setting.gameWidth == gameResolutions.resolutions[i].width 
					&& setting.gameHeight == gameResolutions.resolutions[i].height)
			{
				selected = i;
			}
		}
		
		if(selected > -1)
		{
			cmbGameRes.select(selected);
		}
		
		cmbGameRes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int selected = cmbGameRes.getSelectionIndex();
				HearthResolutionsList.Resolution res = (HearthResolutionsList.Resolution) cmbGameRes.getData(cmbGameRes.getItem(selected));
				System.out.println("preferences resolution selected: " + res.width + "x" + res.height);
				
				setting.gameWidth = res.width;
				setting.gameHeight = res.height;
				
				savePreferences();
				
				hearth.setGameRes(res.width, res.height);
			}
		});
		
		btnAutoDetectGameRes.setSelection(setting.autoRes);
		
		btnAutoDetectGameRes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.autoRes = btnAutoDetectGameRes.getSelection();
				savePreferences();
				hearth.setAutoGameRes(setting.autoRes);
			}
		});
		
	}
	
	private void poppulatesOffsetOptions(){
		hearth.setXOffetOverride(setting.xOffset);
		hearth.setYOffetOverride(setting.yOffset);
		
		spXOffset.setSelection(setting.xOffset);
		spYOffset.setSelection(setting.yOffset);
		
		spXOffset.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setting.xOffset = Integer.parseInt(spXOffset.getText());
				hearth.setXOffetOverride(setting.xOffset);
				savePreferences();
			}
		});
		
		spYOffset.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setting.yOffset = Integer.parseInt(spYOffset.getText());
				hearth.setYOffetOverride(setting.yOffset);
				savePreferences();
			}
		});
	}
	
	private void poppulateScannerOptions(){
		btnAlwaysScan.setSelection(setting.alwaysScan);
		btnAlwaysScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.alwaysScan = btnAlwaysScan.getSelection();
				savePreferences();
				hearth.setAlwaysScan(setting.alwaysScan);
			}
		});
		
		
		btnEnableScanner.setSelection(setting.scannerEnabled);
		btnEnableScanner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.scannerEnabled = btnEnableScanner.getSelection();
				savePreferences();
				
				if(setting.scannerEnabled){
					hearth.resume();
				} else {
					hearth.pause();
				}
			}
		});
		
		switch(setting.scanInterval){
			case 0:
			btnScanSpeed[3].setSelection(true);
			break;
		
			case 250:
				btnScanSpeed[0].setSelection(true);
				break;
			case 750:
				btnScanSpeed[2].setSelection(true);
				break;
			case 500:
			default:
				btnScanSpeed[1].setSelection(true);
				break;
		}
		
		
		for(int i = 0; i < 3; i++){
			btnScanSpeed[i].addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent arg0) {
					
					if(btnScanSpeed[0].getSelection()){
						setting.scanInterval = 250;
					}else if(btnScanSpeed[1].getSelection()){
						setting.scanInterval = 500;
					}else if(btnScanSpeed[2].getSelection()){
						setting.scanInterval = 750;
					}else if(btnScanSpeed[3].getSelection()){
						setting.scanInterval = 0;
					}
					
					savePreferences();
				}
			});
		}
		
	}
	
	private void poppulateGeneralPerferences(){
		cmbGameLang.removeAll();
		int selected = 0;
		
		for(int i = 0; i < gameLanguages.langs.length; i++){
			cmbGameLang.add(gameLanguages.langs[i].label);
			cmbGameLang.setData(gameLanguages.langs[i].label, gameLanguages.langs[i].code);
			
			if(setting.gameLang.toLowerCase().equals(gameLanguages.langs[i].code.toLowerCase())){
				selected = i;
			}
		}
		
		cmbGameLang.select(selected);
		
		cmbGameLang.addSelectionListener(new SelectionAdapter() {
			
			int previousSelection = -1;
			
			private void selected(SelectionEvent e){
				int i = cmbGameLang.getSelectionIndex();
				
				if(i != -1){
					String langCode = (String) cmbGameLang.getData(cmbGameLang.getItem(i));
					System.out.println("preferences game lang selected: " + langCode);
					setting.gameLang = langCode;
					
					if(previousSelection != i){
						hearth.setGameLang(langCode);
						previousSelection = i; 
						savePreferences();
					}
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				this.selected(e);
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				this.selected(e);
			}
		});
		
		txtWebSyncKey.setText(new HearthSync().getKey());
		
		txtWebSyncKey.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				String key = txtWebSyncKey.getText().trim();
				boolean keyValid = false;
				boolean sameKey = false;
				HearthSync hsync = new HearthSync();
					
				if(hsync.getKey().equals(key)){
					sameKey = true;
				}
				
				if(key.length() == 48){
					hsync.setKey(key);
					
					if(hsync.checkAccessKey()){
						keyValid = true;
					}
				}
				
				if(!sameKey && keyValid){
	 				NotifierDialog.notify(
    						"Web Access Key", 
    						"Validated. Key saved. Sync started.", 
    						new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-32.png" ),
    						shlHearthtracker.getMonitor()
    				);
	 				
	 				hsync.setKey(key);
	 				hsync.saveKey();

				}else if(!sameKey && !keyValid){
					NotifierDialog.notify(
    						"Web Access Key", 
    						"Invalid. Key is not saved!", 
    						new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-32.png" ),
    						shlHearthtracker.getMonitor()
    				);
				}
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				txtWebSyncKey.selectAll();
			}
		});
	}
	
	private void poppulateGameServer(){
		HearthBnetServersList servers = new HearthBnetServersList();
		int selected = -1;
		cbServer.removeAll();
		
		for(int i = 0; i < servers.getTotal(); i++){
			cbServer.add(servers.getServerLabel(i));
			cbServer.setData(servers.getServerLabel(i), servers.getServerName(i));
			
			if(servers.getServerName(i).equals(setting.gameServer)){
				selected = i;
			}
		}
		
		cbServer.select(selected);
			
		cbServer.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int i = cbServer.getSelectionIndex();
				
				if( i >= 0 ){					
					String server = (String) cbServer.getData(cbServer.getItem(i));

					if(setting.gameServer == null || !setting.gameServer.equals(server)){
						setting.gameServer = server;
						tracker.setServer(server);
						config.save(setting, "." + File.separator + "configs" + File.separator + "settings.xml");
					}
				}
			}
		});
	}
	
	private void updateStatus(){
		String overview = hearth.getOverview();
		styledTextStatus.setText(overview);
		tracker.writeLines(overview);
	}
	
	private void setupModeSelection(){
		cmbStatsMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				fillOverviewTable();
			}
		});
	}
	
	private static Image resize(Image image, int width, int height) {
		Image scaled = new Image(Display.getDefault(), width, height);
		GC gc = new GC(scaled);
		gc.setAntialias(SWT.ON);
		gc.setInterpolation(SWT.HIGH);
		gc.drawImage(image, 0, 0, 
		image.getBounds().width, image.getBounds().height, 
		0, 0, width, height);
		gc.dispose();
		image.dispose(); // don't forget about me!
		return scaled;
	}
	
	private int getMode(){
		int mode = cmbStatsMode.getSelectionIndex();
		
		switch(mode){
			case 0:
				return HearthReader.ARENAMODE;
			case 1:
				return HearthReader.RANKEDMODE;
			case 2:
				return HearthReader.UNRANKEDMODE;
			case 3:
				return HearthReader.CHALLENGEMODE;
			case 4:
				return HearthReader.PRACTICEMODE;
		}
		
		return HearthReader.UNKNOWNMODE;
	}
	
	private void fillOverviewTable(){
		int selected = table.getSelectionIndex();
		table.removeAll();
		int mode = this.getMode();
		
		for(int i = 0; i < heroesList.getTotal() + 1; i++){
			float sevenplus = 0, overall = 0;
			int wins = 0;
			int losses = 0;
			int totalrun = 0;
			Image heroImg;
			int heroId = i < heroesList.getTotal() ? i : -1;
			
			try {
				wins = tracker.getTotalWinsByHero(mode, heroId);
				losses = tracker.getTotalLossesByHero(mode, heroId);
				sevenplus = tracker.getWinRateByHeroSpecial(mode, heroId);
				overall = tracker.getWinRateByHero(mode, heroId);
				totalrun = tracker.getTotalRunsByHero(mode, heroId);
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			if(heroId == -1 && !(overall > -1)){
				continue;
			}
			
			TableItem tableItem_1 = new TableItem(table, SWT.NONE);
			
			tableItem_1.setImage(0, heroImgs[heroId+1]);
			
			if( !(overall > -1) ){
				continue;
			}
			
			tableItem_1.setText(1,   wins + "");
			tableItem_1.setText(2,   losses + "");
			
			if(overall > -1){
				tableItem_1.setText(3,  new DecimalFormat("0.00").format(overall*100));
			}
			
			if(sevenplus > -1){
				tableItem_1.setText(4,  new DecimalFormat("0.00").format(sevenplus*100));
			}
			
			tableItem_1.setText(5,  totalrun + "");
			
		}
		
		if(selected > -1){
			table.select(selected);
		}
	}
}

package my.hearthtracking.app;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Logger;

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

@SuppressWarnings({ "unused", "deprecation" })
public class HearthUI {
	protected Shell shlHearthtracker;
	private CCombo cmbGameLang;
	private Button btnEnableScanner;
	private Button[] btnScanSpeed = new Button[3];
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
	private Text text;
	private Composite composite_11;
	private Spinner spXOffset;
	private Spinner spYOffset;
	
	volatile static boolean shutdown = false;
	private Button btnAlwaysScan;
	
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
		
		heroesList = (HearthHeroesList) config.load("." + File.separator + "configs" + File.separator + "heroes.xml");
		gameLanguages = (HearthGameLangList) config.load("." + File.separator + "configs" + File.separator + "gameLangs.xml");
		gameResolutions = (HearthResolutionsList) config.load("." + File.separator + "configs" + File.separator + "gameResolutions.xml");
		setting = (HearthSetting) config.load("." + File.separator + "configs" + File.separator + "settings.xml");
		
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
	    	while(!shutdown){
	        	try {
	        		long sleepTime;
//	        		Date lastScan = new Date();
	        		hearth.process();
//	        		long timeDiff = new Date().getTime() - lastScan.getTime();
//	        		
//	        		sleepTime =  setting.scanInterval - timeDiff;
//	        		
//	        		System.out.println("Diff: " + timeDiff);
//	        		System.out.println("Sleep: " + sleepTime);
//
//        			if(sleepTime < 0){
//        				sleepTime = 100;
//        			}

        			Thread.sleep(setting.scanInterval);
        			
        			if(Thread.currentThread().isInterrupted()){
            			tracker.closeDB();
    	    			System.exit(0);
        			}
        			
	    		} catch (InterruptedException e) {
	    			tracker.closeDB();
	    			System.exit(0);
	    			break;
	    		}
	    	}
	    }
    }
    
    private static class SyncThread
    implements Runnable {
	    public void run() {
	    	while(true){
//	        	try {
//	        		
//	    		} catch (InterruptedException e) {
//	    			break;
//	    		}
	    	}
	    }
    }


	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlHearthtracker.setImage(new Image( display, "." + File.separator + "images" + File.separator + "etc" + File.separator + "logo-128.png" ));
		shlHearthtracker.open();
		shlHearthtracker.layout();
		Date lastUpdate = new Date(); 
		
		hearththread = new Thread(new ReaderThread());
		hearththread.start();
		
		while (!shlHearthtracker.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			
			if(hearththread.isAlive()){
				if(tracker.isDirty()){
					window.fillOverviewTable();
					window.fillArenaTable();
					window.fillMatchesTable();
					window.updateStatus();
				}
				
				if(hearth.isDirty()){
					window.updateStatus();
				}
				
				if(new Date().getTime() - lastUpdate.getTime() > 2000){	
					window.poppulateDiagnoticsStatus();
					lastUpdate = new Date();
				}
			}
		}
	}

	/**
	 * Create contents of the window.
	 */
	protected void createContents() {
		shlHearthtracker = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX));
		shlHearthtracker.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				hearththread.interrupt();
				shutdown = true;
			}
		});
		shlHearthtracker.setSize(620, 438);
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
		cmbStatsMode.setItems(new String[] {"Arena mode (played as)", "Ranked mode (played as)", "Unranked mode (played as)", "Challenge/Pratice mode (played as)"});
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
		link.setBounds(149, 53, 71, 15);
		link.setText("<a>Web App Key</a>");
		
		text = new Text(grpGeneral, SWT.BORDER | SWT.PASSWORD);
		text.setBounds(232, 49, 241, 21);
		text.setEnabled(false);
		
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
		cmbGameRes.setEditable(false);
		
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
		
		Button button = new Button(grpAdvanced, SWT.RADIO);
		button.setBounds(241, 155, 42, 16);
		btnScanSpeed[0] = button;
		button.setText("Fast");
		
		Button button_1 = new Button(grpAdvanced, SWT.RADIO);
		button_1.setBounds(288, 155, 85, 16);
		btnScanSpeed[1] = button_1;
		button_1.setText("Intermediate");
		
		Button button_2 = new Button(grpAdvanced, SWT.RADIO);
		button_2.setBounds(379, 155, 46, 16);
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
		tbtmDiagnostics.setText("D&iagnostics");
		
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
		lblVersion.setBounds(46, 132, 113, 15);
		lblVersion.setText("HearthTracker v" + version[0] + "." + version[1] + "." + version[2]);
		
		Label lblCopyrightc = new Label(composite_3, SWT.NONE);
		lblCopyrightc.setBounds(36, 153, 150, 15);
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
					java.awt.Desktop.getDesktop().browse(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2NK7Y4PU86UK2").toURI());
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
		link_1.setBounds(36, 176, 150, 15);
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
		poppulateGameLangs();
		poppulateResolutions();
		poppulateDiagnoticsControls();
		poppulateDiagnoticsStatus();
		updateStatus();
		fillOverviewTable();
		fillMatchesTable();
		fillArenaTable();
		setupModeSelection();
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
				String result = rs.getInt("WIN") == 1 ? "Win" : "Loss";
				cal.setTime(new Date(rs.getLong("STARTTIME")));
				
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
		cbMatchesEditResult.setItems(new String[] {"Win", "Loss"});
		
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
							} else {
								cbMatchesEditGoes.select(1);
							}
							
							if(gameMode > 0){
								cbMatchesEditGameMode.select(gameMode);
							} else {
								cbMatchesEditGameMode.select(0);
							}
							
							if(win == 1){
								cbMatchesEditResult.select(0);
							}else{
								cbMatchesEditResult.select(1);
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
				int goes = cbMatchesEditGoes.getSelectionIndex() == 0 ? 1 : 0;
				int result = cbMatchesEditResult.getSelectionIndex() == 0 ? 1 : 0;
				int totaltime = spMatchesEditMinute.getSelection() * 60;
				
				if(id == 0){
					return;
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
		cbMatchesEditResult.setItems(new String[] {"Win", "Loss"});
		
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
				int result = cbMatchesEditResult.getSelectionIndex() == 0 ? 1 : 0;
				int totaltime = spMatchesEditMinute.getSelection() * 60;

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
					tracker.saveMatchResult(gMode, myheroid, oppheroid, goes, result, starttime, totaltime, true);
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
		
		spXOffset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.xOffset = spXOffset.getSelection();
				hearth.setXOffetOverride(setting.xOffset);
				savePreferences();
			}
		});
		
		spYOffset.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.yOffset = spYOffset.getSelection();
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
					}
					
					savePreferences();
				}
			});
		}
		
	}
	
	private void poppulateGameLangs(){
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
	}
	
	private void updateStatus(){
		Date lastSeen = hearth.getLastseen();
		String seen = lastSeen.getTime() == 0 ? "Nope" : HearthHelper.getPrettyText(lastSeen);
		String goes = "Unknown";
		int arenaWins = -1;
		int arenaLosses = -1;
		float arenaWinrate = -1;
		
		int rankedWins = -1;
		int rankedLosses = -1;
		float rankedWinrate = -1;
		
		int unrankedWins = -1;
		int unrankedLosses = -1;
		float unrankedWinrate = -1;

		try {
			arenaWins = 	tracker.getTotalWins(HearthReader.ARENAMODE);
			arenaLosses = 	tracker.getTotalLosses(HearthReader.ARENAMODE);
			arenaWinrate =  (arenaWins + arenaLosses) > 0 ? (float) arenaWins /  (arenaWins + arenaLosses) * 100: -1;
			rankedWins = 	tracker.getTotalWins(HearthReader.RANKEDMODE);
			rankedLosses = 	tracker.getTotalLosses(HearthReader.RANKEDMODE);
			rankedWinrate =  (rankedWins + rankedLosses) > 0 ? (float) rankedWins / (rankedWins + rankedLosses) * 100 : -1;
			unrankedWins = 	tracker.getTotalWins(HearthReader.UNRANKEDMODE);
			unrankedLosses = 	tracker.getTotalLosses(HearthReader.UNRANKEDMODE);
			unrankedWinrate =  (unrankedWins + unrankedLosses) > 0 ? (float) unrankedWins / (unrankedWins + unrankedLosses) * 100 : -1;
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		String strArena = arenaWinrate > -1 ?  arenaWins + "-" + arenaLosses + " (" + new DecimalFormat("0.00").format(arenaWinrate) + "%) " : "N|A";
		String strRanked = rankedWinrate > -1 ?  rankedWins + "-" + rankedLosses + " (" + new DecimalFormat("0.00").format(rankedWinrate) + "%) " : "N|A";
		String strUnranked = unrankedWinrate > -1 ? unrankedWins + "-" + unrankedLosses + " (" + new DecimalFormat("0.00").format(unrankedWinrate) + "%) " : "N|A";

		if(hearth.isGoFirst()){
			goes = "first";
		}
		
		if(hearth.isGoSecond()){
			goes = "second";
		}
		
		styledTextStatus.setText("");
		
		styledTextStatus.append("Last seen: " + seen + "\r\n");
		
		if(arenaWinrate > -1){
			styledTextStatus.append("Arena: " + strArena  +"\r\n");
		}
		
		if(rankedWinrate > -1){
			styledTextStatus.append("Ranked: " + strRanked +"\r\n");
		}
		
		if(unrankedWinrate > -1){
			styledTextStatus.append("Unranked: " + strUnranked +"\r\n");
		}

		styledTextStatus.append("Current Game mode: " + hearth.getGameMode() +"\r\n");
		
		if(hearth.isArenaMode()){
			String score = hearth.getArenaWins() > -1 && hearth.getArenaLosses() > -1 ? hearth.getArenaWins() + "-" + hearth.getArenaLosses() : "Unknown";
			
			styledTextStatus.append("\r\n");
			styledTextStatus.append("Live Arena status" + "\r\n");
			styledTextStatus.append("Score: " + score + "\r\n");
			styledTextStatus.append("Playing as " + hearth.getMyHero() + "\r\n");
		}
				
		if( !hearth.getMyHero().toLowerCase().equals("unknown") || hearth.isGoFirst() || hearth.isGoSecond() ){
			styledTextStatus.append("\r\n");
			styledTextStatus.append("Live match status" + "\r\n");
			styledTextStatus.append(hearth.getMyHero() + " vs " + hearth.getOppHero() + ", " + goes + "\r\n");
		}
		
		try {
			ResultSet rs = tracker.getLastMatches(5);

			styledTextStatus.append("\r\nLatest match(es): \r\n");
			
			while(rs.next()){
				
				String as 	= heroesList.getHeroLabel(rs.getInt("MYHEROID"));
				String vs 	= heroesList.getHeroLabel(rs.getInt("OPPHEROID"));
				String first = rs.getInt("GOESFIRST") == 1 ? "(1st) " : "(2nd) ";
				String result = rs.getInt("WIN") == 1 ? "(W) " : "(L) ";
				
				if(rs.getInt("GOESFIRST") == -1){
					first =  "";
				}
				
				if(rs.getInt("WIN") == -1){
					result =  "";
				}
				
				styledTextStatus.append(as + " vs " + vs + " " + first + result + "\r\n");
			}
			
			rs = tracker.getLastArenaResults(5);

			styledTextStatus.append("\r\nLatest Arena: \r\n");
			
			while(rs.next()){
				
				String as 	= heroesList.getHeroLabel(rs.getInt("HEROID"));
				String result = rs.getInt("WINS") + "-" + rs.getInt("LOSSES");
	
				styledTextStatus.append(as + " " + result + "\r\n");
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
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
		}
		
		return HearthReader.UNKNOWNMODE;
	}
	
	private void fillOverviewTable(){
		int selected = table.getSelectionIndex();
		table.removeAll();
		
		for(int i = 0; i < heroesList.getTotal() + 1; i++){
			float sevenplus = 0, overall = 0;
			int wins = 0;
			int losses = 0;
			int totalrun = 0;
			Image heroImg;
			int mode = this.getMode();
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

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


@SuppressWarnings({ "unused", "deprecation" })
public class HearthUI {

	private static final int SAVEMODE = 0;
	private static final int NEWMODE = 1;
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
	private Label[] lblStatus = new Label[17]; 
	private Combo cmbStatsMode;
	
	private Group grpStats;
	
	private static Display display;
	private static HearthUI window;
	static boolean debugMode = HearthHelper.isDevelopmentEnvironment();
	
	private static HearthReader hearth;
	private static Tracker tracker;
	private static HearthConfigurator config = new HearthConfigurator();
	private static HearthGameLangList gameLanguages;
	private static HearthResolutionsList gameResolutions;
	private static HearthSetting setting;
	private static HearthHeroesList heroesList;
	private static Logger logger;
	
	Thread hearththread;
	private Table table;
	private Table table_1;
	private Table table_2;
	private TabFolder tabFolder_1;
	private TabItem tbtmMatchesEdit;
	private TabItem tbtmMatchesNew;
	private TabItem tbtmArenaEdit;
	private TabItem tbtmArenaNew;
	private static Image[] heroImgs;
	private Composite composite_9;
	
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
		tracker = new Tracker();
		hearth = new HearthReader(tracker, setting.gameLang, setting.gameWidth, setting.gameHeight, setting.autoPing, debugMode);
		
		if(!setting.scannerEnabled){
			hearth.pause();
		}
		
		heroImgs = new Image[heroesList.getTotal()+1];
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			heroImgs[i+1] = new Image(display, "." + File.separator + "images" + File.separator + heroesList.getHeroName(i) + "-s.png");
			heroImgs[i+1] = resize(heroImgs[i+1], 24, 24);
		}

	}
	
    private static class MessageLoop
    implements Runnable {
	    public void run() {
	    	while(true){
	        	try {
	        		long sleepTime;
	        		Date lastScan = new Date();
	        		hearth.process();
	        		
	        		sleepTime = setting.scanInterval - (new Date().getTime() - lastScan.getTime());
        			
        			if(sleepTime > 0){
        				Thread.sleep(sleepTime);
        			}
	    		} catch (InterruptedException e) {
	    			tracker.closeDB();
	    			break;
	    		}
	    	}
	    }
    }


	/**
	 * Open the window.
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlHearthtracker.open();
		shlHearthtracker.layout();
		Date lastUpdate = new Date(); 
		
		hearththread = new Thread(new MessageLoop());
		hearththread.start();
		
		while (!shlHearthtracker.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
			
			if(hearththread.isAlive()){
				if(new Date().getTime() - lastUpdate.getTime() > 2000){
					window.fillOverviewTable();
					window.poppulateCurrentStats();
					window.updateStatus();
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
			}
		});
		shlHearthtracker.setSize(620, 438);
		shlHearthtracker.setText("HearthTracker - Automated Stats Tracking for Hearthstone enthusiasts!");
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
		tblclmnNewColumn_3.setText("7+");
		
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
		grpCurrentStats.setText("Current Status");
		grpCurrentStats.setLayout(new FillLayout(SWT.VERTICAL));
		
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
		
		table_1 = new Table(sashForm_1, SWT.FULL_SELECTION);
		table_1.setLinesVisible(true);
		table_1.setHeaderVisible(true);
		table_1.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tblclmnAs = new TableColumn(table_1, SWT.NONE);
		tblclmnAs.setText("As");
		tblclmnAs.setWidth(29);
		
		TableColumn tblclmnVs = new TableColumn(table_1, SWT.NONE);
		tblclmnVs.setWidth(29);
		tblclmnVs.setText("Vs");
		
		TableColumn tblclmnMode = new TableColumn(table_1, SWT.NONE);
		tblclmnMode.setWidth(55);
		tblclmnMode.setText("Mode");
		
		TableColumn tblclmnResult = new TableColumn(table_1, SWT.NONE);
		tblclmnResult.setWidth(45);
		tblclmnResult.setText("Result");
		
		TableColumn tblclmnOn = new TableColumn(table_1, SWT.NONE);
		tblclmnOn.setWidth(88);
		tblclmnOn.setText("On");
		
		tabFolder_1 = new TabFolder(sashForm_1, SWT.NONE);
		sashForm_1.setWeights(new int[] {264, 331});
		
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
		
		table_2 = new Table(sashForm_2, SWT.FULL_SELECTION);
		table_2.setLinesVisible(true);
		table_2.setHeaderVisible(true);
		table_2.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tblclmnAs_1 = new TableColumn(table_2, SWT.NONE);
		tblclmnAs_1.setText("As");
		tblclmnAs_1.setWidth(25);
		
		TableColumn tableColumn_3 = new TableColumn(table_2, SWT.NONE);
		tableColumn_3.setWidth(60);
		tableColumn_3.setText("Result");
		
		TableColumn tableColumn_4 = new TableColumn(table_2, SWT.NONE);
		tableColumn_4.setWidth(60);
		tableColumn_4.setText("On");
		
		TabFolder tabFolder_2 = new TabFolder(sashForm_2, SWT.NONE);
		
		tbtmArenaEdit = new TabItem(tabFolder_2, 0);
		tbtmArenaEdit.setText("&Edit");
		
		TabItem tbtmArenaNew = new TabItem(tabFolder_2, 0);
		tbtmArenaNew.setText("&New");
		
		Composite composite_10 = new Composite(tabFolder_2, SWT.NONE);
		tbtmArenaNew.setControl(composite_10);
		composite_10.setLayout(new FormLayout());
		sashForm_2.setWeights(new int[] {220, 375});
			
		TabItem tbtmPerferences = new TabItem(tabFolder, SWT.NONE);
		tbtmPerferences.setText("&Preferences");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmPerferences.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		Group grpGeneral = new Group(composite_1, SWT.NONE);
		grpGeneral.setText("General");
		grpGeneral.setLayout(new GridLayout(4, false));
		GridData gd_grpGeneral = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_grpGeneral.heightHint = 160;
		gd_grpGeneral.widthHint = 585;
		grpGeneral.setLayoutData(gd_grpGeneral);
		
		Label lblNewLabel_2 = new Label(grpGeneral, SWT.NONE);
		GridData gd_lblNewLabel_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_2.widthHint = 100;
		lblNewLabel_2.setLayoutData(gd_lblNewLabel_2);
		lblNewLabel_2.setText("Scanner");
		new Label(grpGeneral, SWT.NONE);
		new Label(grpGeneral, SWT.NONE);
		
		btnEnableScanner = new Button(grpGeneral, SWT.CHECK);
		btnEnableScanner.setSelection(true);
		btnEnableScanner.setText("Enable");
		
		Label lblNewLabel = new Label(grpGeneral, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.widthHint = 100;
		lblNewLabel.setLayoutData(gd_lblNewLabel);
		lblNewLabel.setText("Scan Speed");
		new Label(grpGeneral, SWT.NONE);
		new Label(grpGeneral, SWT.NONE);
		
		Composite composite_6 = new Composite(grpGeneral, SWT.NONE);
		composite_6.setToolTipText("Increase the scanner frequency in order to catch stats better. Lower the speed if you experience bad performance.");
		composite_6.setLayout(new GridLayout(5, false));
		GridData gd_composite_6 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_composite_6.heightHint = 27;
		gd_composite_6.widthHint = 256;
		composite_6.setLayoutData(gd_composite_6);
		
		btnScanSpeed[0] = new Button(composite_6, SWT.RADIO);
		btnScanSpeed[0].setText("Fast");
		new Label(composite_6, SWT.NONE);
		
		btnScanSpeed[1] = new Button(composite_6, SWT.RADIO);
		btnScanSpeed[1].setText("Intermediate");
		new Label(composite_6, SWT.NONE);
		
		btnScanSpeed[2] = new Button(composite_6, SWT.RADIO);
		btnScanSpeed[2].setText("Slow");
		
		Label lblNewLabel_1 = new Label(grpGeneral, SWT.NONE);
		GridData gd_lblNewLabel_1 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_1.widthHint = 100;
		lblNewLabel_1.setLayoutData(gd_lblNewLabel_1);
		lblNewLabel_1.setText("Game Language");
		new Label(grpGeneral, SWT.NONE);
		new Label(grpGeneral, SWT.NONE);
		
		cmbGameLang = new CCombo(grpGeneral, SWT.BORDER | SWT.READ_ONLY);
		cmbGameLang.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmbGameLang.setEditable(false);
		cmbGameLang.setItems(new String[] {});
		cmbGameLang.setVisibleItemCount(1);
		
		Label lblDetect = new Label(grpGeneral, SWT.NONE);
		lblDetect.setText("Auto Detect Game Resolution");
		new Label(grpGeneral, SWT.NONE);
		new Label(grpGeneral, SWT.NONE);
		
		btnAutoDetectGameRes = new Button(grpGeneral, SWT.CHECK);
		btnAutoDetectGameRes.setToolTipText("It is recommended to enable it specially your desktop is running at same resolution with your Hearthstone resolution.");
		btnAutoDetectGameRes.setText("Enable");
		
		Label lblGameResolution = new Label(grpGeneral, SWT.NONE);
		GridData gd_lblGameResolution = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblGameResolution.widthHint = 100;
		lblGameResolution.setLayoutData(gd_lblGameResolution);
		lblGameResolution.setText("Game Resolution");
		new Label(grpGeneral, SWT.NONE);
		new Label(grpGeneral, SWT.NONE);
		
		cmbGameRes = new CCombo(grpGeneral, SWT.BORDER | SWT.READ_ONLY);
		cmbGameRes.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cmbGameRes.setEditable(false);
		
		Group grpDiagnostics = new Group(composite_1, SWT.NONE);
		grpDiagnostics.setText("Diagnostics");
		grpDiagnostics.setLayout(new GridLayout(4, false));
		GridData gd_grpDiagnostics = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_grpDiagnostics.heightHint = 168;
		gd_grpDiagnostics.widthHint = 585;
		grpDiagnostics.setLayoutData(gd_grpDiagnostics);
		
		Label lblLastSeenLabel = new Label(grpDiagnostics, SWT.NONE);
		GridData gd_lblLastSeenLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblLastSeenLabel.widthHint = 100;
		lblLastSeenLabel.setLayoutData(gd_lblLastSeenLabel);
		lblLastSeenLabel.setText("Last seen");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		lblLastSeen = new Label(grpDiagnostics, SWT.NONE);
		GridData gd_lblLastSeen = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblLastSeen.widthHint = 300;
		lblLastSeen.setLayoutData(gd_lblLastSeen);
		lblLastSeen.setText("..........");
		
		Label lblLastScanCoordinate = new Label(grpDiagnostics, SWT.NONE);
		lblLastScanCoordinate.setText("Last scan area");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		lblLastscreencoordinate = new Label(grpDiagnostics, SWT.NONE);
		GridData gd_lblLastscreencoordinate = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblLastscreencoordinate.widthHint = 300;
		lblLastscreencoordinate.setLayoutData(gd_lblLastscreencoordinate);
		lblLastscreencoordinate.setText("..........");
		
		Label lblNewLabel_5 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_5.setText("Last scan sub-area");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		lblLastScanSubArea = new Label(grpDiagnostics, SWT.NONE);
		GridData gd_lblLastScanSubArea = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblLastScanSubArea.widthHint = 300;
		lblLastScanSubArea.setLayoutData(gd_lblLastScanSubArea);
		lblLastScanSubArea.setText("..........");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		Label lblAutoPingLabel = new Label(grpDiagnostics, SWT.NONE);
		lblAutoPingLabel.setText("Visualize scanned area");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		btnAutoPing = new Button(grpDiagnostics, SWT.CHECK);
		btnAutoPing.setToolTipText("Visualize scanned areas after Hearthstone being out of sight for more than 1 min.");
		btnAutoPing.setText("Enable");
		
		Label lblNewLabel_6 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_6.setText("Diagnotic Tool");
		new Label(grpDiagnostics, SWT.NONE);
		new Label(grpDiagnostics, SWT.NONE);
		
		btnVisualizeNow = new Button(grpDiagnostics, SWT.NONE);
		btnVisualizeNow.setText("Visualize now");
		
		TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
		tbtmAbout.setText("A&bout");
		
		Group grpAbout = new Group(tabFolder, SWT.NONE);
		tbtmAbout.setControl(grpAbout);
		grpAbout.setText("About");
		grpAbout.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite composite_3 = new Composite(grpAbout, SWT.NONE);
		composite_3.setLayout(new GridLayout(1, false));
		
		Label lblVersion = new Label(composite_3, SWT.NONE);
		lblVersion.setText("HearthTracker v1.0.7 Beta");
		
		Label lblCopyrightc = new Label(composite_3, SWT.NONE);
		lblCopyrightc.setText("Copyright \u00A9 2013 megablue");
		
		Label lblWebsite = new Label(composite_3, SWT.NONE);
		lblWebsite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://hearthtracking.com").toURI());
				} catch (IOException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
			}
		});
		lblWebsite.setText("Website:  HearthTracking.com");
		
		Label lblNewLabel_7 = new Label(composite_3, SWT.NONE);
		lblNewLabel_7.setText("");
		
		Composite composite_7 = new Composite(composite_3, SWT.NONE);
		GridData gd_composite_7 = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_composite_7.widthHint = 583;
		composite_7.setLayoutData(gd_composite_7);
		
		StyledText styledText = new StyledText(composite_7, SWT.READ_ONLY | SWT.WRAP);
		styledText.setText("HearthTracker is designed specifically to automate and ease score tracking for Hearthstone enthusiasts. It is coded by megablue. He first created the prototype to display arena score on his stream. Later, realizing it might help a lot of players and streamers, he continued to add new features and refine the code. He still has a lot of interesting ideas that are yet to be implemented. A lot of time and efforts need to be invested into it in order to implement all the exciting features. He hopes that you can show your support by donating. Your support will be greatly appreciated and keep the project alive!");
		styledText.setBounds(0, 0, 583, 119);
		
		Label lblNewLabel_8 = new Label(composite_3, SWT.NONE);
		lblNewLabel_8.setText("");
		
		Label lblSupportTheProject = new Label(composite_3, SWT.NONE);
		lblSupportTheProject.setText("Support the project!");
		
		Label lblPaypal = new Label(composite_3, SWT.NONE);
		lblPaypal.setSize(146, 58);
		lblPaypal.setText("");
		lblPaypal.setToolTipText("Your support means a lot to me. Thank you for even hovering over the donate button!");
		lblPaypal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=2NK7Y4PU86UK2").toURI());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		lblPaypal.setImage(new Image( display, "." + File.separator + "images" + File.separator + "btn_donate_150wx70h.gif" ));

		shlHearthtracker.setTabList(new Control[]{tabFolder});

		createLabels();
		createMatchesEditForm(new Composite(tabFolder_1, SWT.NONE), tbtmMatchesEdit);
		createMatchesNewForm(new Composite(tabFolder_1, SWT.NONE), tbtmMatchesNew);
		createArenaForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaEdit,		SAVEMODE);
		//createArenaForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaNew,			NEWMODE);
		
		poppulateScannerOptions();
		poppulateGameLangs();
		poppulateResolutions();
		poppulateDiagnoticsControls();
		poppulateDiagnoticsStatus();
		poppulateCurrentStats();
		updateStatus();
		poppulateDiagnoticsStatus();
		
		fillOverviewTable();
		fillMatchesTable();
	}
	
	private void fillMatchesTable(){
		try {
			TableItem[] tis = table_1.getSelection();
			int selectedID = -1;
			
			if(tis.length > 0){
				selectedID = (int) tis[0].getData("id");
			}
			
			table_1.removeAll();
			
			ResultSet rs = tracker.getMatches();
			Calendar cal = Calendar.getInstance();

			while(rs.next()){
				TableItem tableItem = new TableItem(table_1, SWT.NONE);
				String result = rs.getInt("WIN") == 1 ? "Win" : "Loss";
				cal.setTime(new Date(rs.getDate("STARTTIME").getTime() + rs.getTime("STARTTIME").getTime()));
				
				tableItem.setData("id", rs.getInt("ID"));
				tableItem.setImage(0, heroImgs[rs.getInt("MYHEROID")+1]);
				tableItem.setImage(1, heroImgs[rs.getInt("OPPHEROID")+1]);
				tableItem.setText(2, HearthReader.gameModeToString(rs.getInt("MODE")));
				tableItem.setText(3, result);
				tableItem.setText(4, (cal.get(Calendar.MONTH) + 1) + "/" + cal.get(Calendar.DAY_OF_MONTH) + "/" + cal.get(Calendar.YEAR));
				
				if(selectedID == rs.getInt("ID")){
					table_1.setSelection(tableItem);
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void createMatchesEditForm(Composite composite_4, TabItem tabitem){
		tabitem.setControl(composite_4);
		
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
		btnMatchesEditSave.setBounds(88, 310, 36, 25);
		btnMatchesEditSave.setText("&Save");

		final Button btnMatchesEditDelete = new Button(composite_4, SWT.NONE);
		btnMatchesEditDelete.setBounds(212, 310, 45, 25);
		btnMatchesEditDelete.setText("&Delete");
		
		final Label lblID = new Label(composite_4, SWT.NONE);
		lblID.setBounds(10, 10, 55, 15);
		lblID.setText("ID #00000");
		lblID.setData("id", 0);
		
		table_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TableItem[] titems = table_1.getSelection();
				
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
							Date startdate = rs.getDate("STARTTIME");
							Date starttime = rs.getTime("STARTTIME");
							Calendar calDate = Calendar.getInstance();
							Calendar calTime = Calendar.getInstance();
							int totaltime = rs.getInt("TOTALTIME") / 60;
							int gameMode = rs.getInt("MODE");

							calDate.setTime(startdate);
							calTime.setTime(starttime);
							
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
	
							dtMatchesEditTime.setHours(calTime.get(Calendar.HOUR_OF_DAY));
							dtMatchesEditTime.setMinutes(calTime.get(Calendar.MINUTE));
							dtMatchesEditTime.setSeconds(calTime.get(Calendar.SECOND));

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
				
				Date starttime = cal.getTime(); 
				
				try {
					btnMatchesEditSave.setEnabled(false);
					btnMatchesEditSave.setText("Saving...");
					tracker.saveModifiedMatchResult(id, gMode, myheroid, oppheroid, goes, result, starttime, totaltime);
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
				
				Date starttime = cal.getTime(); 
				
				System.out.println("mode: " + gMode);
				
				try {
					btnMatchesEditSave.setEnabled(false);
					btnMatchesEditSave.setText("Saving...");
					tracker.saveMatchResult(gMode, myheroid, oppheroid, goes, result, starttime, totaltime);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditSave.setEnabled(true);
				btnMatchesEditSave.setText("&Save");
			}
		});
	}

	private void createArenaForm(Composite composite_4, TabItem tabitem, int mode){
		tbtmArenaEdit.setControl(composite_4);
		composite_4.setLayout(new FormLayout());
		
		Combo combo = new Combo(composite_4, SWT.NONE);
		FormData fd_combo = new FormData();
		fd_combo.top = new FormAttachment(0, 24);
		fd_combo.left = new FormAttachment(0, 134);
		fd_combo.right = new FormAttachment(100, -145);
		combo.setLayoutData(fd_combo);
		
		DateTime dateTime = new DateTime(composite_4, SWT.NONE);
		FormData fd_dateTime = new FormData();
		dateTime.setLayoutData(fd_dateTime);
		
		DateTime dateTime_1 = new DateTime(composite_4, SWT.TIME);
		fd_dateTime.right = new FormAttachment(100, -197);
		FormData fd_dateTime_1 = new FormData();
		fd_dateTime_1.top = new FormAttachment(dateTime, 0, SWT.TOP);
		fd_dateTime_1.left = new FormAttachment(dateTime, 17);
		dateTime_1.setLayoutData(fd_dateTime_1);
		
		Button btnNewButton = new Button(composite_4, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				
			}
		});
		FormData fd_btnNewButton = new FormData();
		fd_btnNewButton.bottom = new FormAttachment(100, -10);
		btnNewButton.setLayoutData(fd_btnNewButton);
		btnNewButton.setText("&Save");
		btnNewButton.setData("mode", mode);
		fd_btnNewButton.right = new FormAttachment(100, -253);
		
		Button btnNewButton_5 = new Button(composite_4, SWT.NONE);
		FormData fd_btnNewButton_5 = new FormData();
		fd_btnNewButton_5.top = new FormAttachment(btnNewButton, 0, SWT.TOP);
		fd_btnNewButton_5.right = new FormAttachment(100, -52);
		btnNewButton_5.setLayoutData(fd_btnNewButton_5);
		btnNewButton_5.setText("&Delete");
		
		Spinner spinner = new Spinner(composite_4, SWT.BORDER);
		fd_dateTime.top = new FormAttachment(spinner, 25);
		FormData fd_spinner = new FormData();
		fd_spinner.top = new FormAttachment(combo, 24);
		fd_spinner.left = new FormAttachment(0, 113);
		spinner.setLayoutData(fd_spinner);
		
		Spinner spinner_1 = new Spinner(composite_4, SWT.BORDER);
		FormData fd_spinner_1 = new FormData();
		fd_spinner_1.bottom = new FormAttachment(spinner, 0, SWT.BOTTOM);
		fd_spinner_1.left = new FormAttachment(spinner, 46);
		spinner_1.setLayoutData(fd_spinner_1);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.top = new FormAttachment(spinner, 3, SWT.TOP);
		fd_lblNewLabel_3.left = new FormAttachment(spinner, 20);
		fd_lblNewLabel_3.right = new FormAttachment(spinner_1, -6);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("-");
		
	}
	
	private void savePreferences(){
		config.save(setting, "." + File.separator + "configs" + File.separator + "settings.xml");
	}
	
	private void poppulateDiagnoticsStatus(){
		Date lastSeen = hearth.getLastseen();
		int[] area = hearth.getLastScanArea();
		int[] subArea = hearth.getLastScanSubArea();

		if(lastSeen == null){
			lblLastSeen.setText("N|A");
		} else {
			lblLastSeen.setText( HearthHelper.getPrettyText(lastSeen) );
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
	
	private void poppulateScannerOptions(){
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
	
	private void poppulateCurrentStats(){
		String winrateStr = "";
		float winrate = 0;
		int mode = this.getMode();

		try {
			winrate = tracker.getOverallWinRate(mode);
			
			if(winrate >= 0){
				winrateStr += " (" + new DecimalFormat("#.##").format(winrate) + "% )";
			}
			
			winrateStr = tracker.getTotalWins(mode) + "-" + tracker.getTotalLosses(mode) + winrateStr;
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
	private void updateStatus(){
		Date lastSeen = hearth.getLastseen();
		String seen = lastSeen.getTime() == 0 ? "Not yet" : HearthHelper.getPrettyText(lastSeen);
		String goes = "Unknown";
		int line = 0;
		
		if(hearth.isGoFirst()){
			goes = "first";
		}
		
		if(hearth.isGoSecond()){
			goes = "second";
		}
		
		lblStatus[line++].setText("Detected: " + seen);
		lblStatus[line++].setText("Game mode: " + hearth.getGameMode());
		lblStatus[line++].setText("Arena Win%: ");
		lblStatus[line++].setText("Ranked Win%: ");
		lblStatus[line++].setText("Unranked Win%: ");
		
		if(hearth.isArenaMode()){
			lblStatus[line++].setText("");
			lblStatus[line++].setText("Live Arena status");
			lblStatus[line++].setText("Score: " + hearth.getArenaWins() + "-" + hearth.getArenaLosses());
			lblStatus[line++].setText("Playing as " + hearth.getMyHero());
		}
				
		if(hearth.isInGame()){
			lblStatus[line++].setText("");
			lblStatus[line++].setText("Live match status");
			lblStatus[line++].setText(hearth.getMyHero() + " vs " + hearth.getOppHero());
			lblStatus[line++].setText("Go " + goes);
		}
		
		lblStatus[line++].setText("");
		lblStatus[line++].setText("Previous match status");
		lblStatus[line++].setText(hearth.getMyHero() + " vs " + hearth.getOppHero());
		lblStatus[line++].setText("Go " + goes);
		lblStatus[line++].setText("Victory/Defeat");
				
		for(int i = line; i < lblStatus.length; i++){
			lblStatus[i].setText("");
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
	
	private void createLabels(){	
		for(int i = 0; i < lblStatus.length; i++){
			lblStatus[i] = new Label(grpCurrentStats, SWT.NONE);
			lblStatus[i].setText("...................................................................");
			lblStatus[i].setSize(300, 20);
		}
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
		
		if(selected > 0){
			table.select(selected);
		}
	}
}

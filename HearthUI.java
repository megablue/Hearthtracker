import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
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
	
	private Display display;
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
	}
	
    private static class MessageLoop
    implements Runnable {
    public void run() {
    	while(true){
        	try {
        		hearth.process();
    			Thread.sleep(setting.scanInterval);
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
				if(hearththread.isAlive()){
					if(new Date().getTime() - lastUpdate.getTime() > 2000){
						window.poppulateOverviewTable();
						window.poppulateCurrentStats();
						window.updateStatus();
						window.poppulateDiagnoticsStatus();
						lastUpdate = new Date();
					}
				}
				display.sleep();
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
		tbtmDashboard.setText("Overview");
		
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
		
		TableColumn tblclmnNewColumn = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn.setWidth(40);
		
		TableColumn tblclmnNewColumn_1 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_1.setWidth(55);
		tblclmnNewColumn_1.setText("Wins");
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_2.setWidth(55);
		tblclmnNewColumn_2.setText("Losses");
		
		TableColumn tblclmnWin = new TableColumn(table, SWT.NONE);
		tblclmnWin.setWidth(55);
		tblclmnWin.setText("Win %");
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_3.setWidth(55);
		tblclmnNewColumn_3.setText("6+");
		
		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.NONE);
		tblclmnNewColumn_4.setWidth(72);
		tblclmnNewColumn_4.setText("Total Runs");
		
		cmbStatsMode = new Combo(grpStats, SWT.READ_ONLY);
		cmbStatsMode.setItems(new String[] {"Arena mode (played as)", "Ranked mode (played as)", "Unranked mode (played as)"});
		FormData fd_cmbStatsMode = new FormData();
		fd_cmbStatsMode.top = new FormAttachment(0, 10);
		fd_cmbStatsMode.left = new FormAttachment(0, 10);
		cmbStatsMode.setLayoutData(fd_cmbStatsMode);
		cmbStatsMode.select(0);
		
		grpCurrentStats = new Group(sashForm, SWT.NONE);
		grpCurrentStats.setText("Current Status");
		grpCurrentStats.setLayout(new GridLayout(1, false));
		sashForm.setWeights(new int[] {365, 230});
		GridData gd_lblNewLabel_15 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_15.widthHint = 60;
		
		TabItem tbtmRecords = new TabItem(tabFolder, SWT.NONE);
		tbtmRecords.setText("Matches");
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmRecords.setControl(composite_2);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_1 = new SashForm(composite_2, SWT.NONE);
		sashForm_1.setLayoutData(new RowData(598, 373));
		
		table_1 = new Table(sashForm_1, SWT.FULL_SELECTION);
		table_1.setLinesVisible(true);
		table_1.setHeaderVisible(true);
		table_1.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tableColumn = new TableColumn(table_1, SWT.NONE);
		tableColumn.setWidth(40);
		
		TableColumn tblclmnVs = new TableColumn(table_1, SWT.NONE);
		tblclmnVs.setWidth(40);
		tblclmnVs.setText("vs");
		
		TableColumn tblclmnResult = new TableColumn(table_1, SWT.NONE);
		tblclmnResult.setWidth(60);
		tblclmnResult.setText("Result");
		
		TableColumn tblclmnOn = new TableColumn(table_1, SWT.NONE);
		tblclmnOn.setWidth(60);
		tblclmnOn.setText("On");
		
		TabFolder tabFolder_1 = new TabFolder(sashForm_1, SWT.NONE);
		
		TabItem tbtmEdi = new TabItem(tabFolder_1, SWT.NONE);
		tbtmEdi.setText("Modify");
		
		Composite composite_4 = new Composite(tabFolder_1, SWT.NONE);
		tbtmEdi.setControl(composite_4);
		composite_4.setLayout(new FormLayout());
		
		Combo combo = new Combo(composite_4, SWT.NONE);
		FormData fd_combo = new FormData();
		fd_combo.left = new FormAttachment(0, 72);
		combo.setLayoutData(fd_combo);
		
		Label lblNewLabel_3 = new Label(composite_4, SWT.NONE);
		fd_combo.right = new FormAttachment(lblNewLabel_3, -12);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.left = new FormAttachment(0, 172);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("vs");
		
		Combo combo_1 = new Combo(composite_4, SWT.NONE);
		FormData fd_combo_1 = new FormData();
		fd_combo_1.left = new FormAttachment(lblNewLabel_3, 15);
		combo_1.setLayoutData(fd_combo_1);
		
		Label lblNewLabel_4 = new Label(composite_4, SWT.NONE);
		fd_combo.bottom = new FormAttachment(100, -290);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.top = new FormAttachment(combo, 30);
		fd_lblNewLabel_4.left = new FormAttachment(0, 116);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Goes");
		
		Combo combo_2 = new Combo(composite_4, SWT.NONE);
		fd_combo_1.bottom = new FormAttachment(combo_2, -27);
		fd_lblNewLabel_3.bottom = new FormAttachment(combo_2, -27);
		fd_lblNewLabel_4.right = new FormAttachment(combo_2, -20);
		combo_2.setItems(new String[] {"First", "Second", "Unknown"});
		FormData fd_combo_2 = new FormData();
		fd_combo_2.top = new FormAttachment(combo, 27);
		fd_combo_2.left = new FormAttachment(0, 162);
		combo_2.setLayoutData(fd_combo_2);
		
		TabItem tbtmNew = new TabItem(tabFolder_1, SWT.NONE);
		tbtmNew.setText("New");
		
		Composite composite_5 = new Composite(tabFolder_1, SWT.NONE);
		tbtmNew.setControl(composite_5);
		composite_5.setLayout(new FormLayout());
		sashForm_1.setWeights(new int[] {220, 375});
		
		TabItem tbtmArena = new TabItem(tabFolder, 0);
		tbtmArena.setText("Arena");
		
		Composite composite_8 = new Composite(tabFolder, SWT.NONE);
		tbtmArena.setControl(composite_8);
		composite_8.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_2 = new SashForm(composite_8, SWT.NONE);
		sashForm_2.setLayoutData(new RowData(598, 373));
		
		table_2 = new Table(sashForm_2, SWT.FULL_SELECTION);
		table_2.setLinesVisible(true);
		table_2.setHeaderVisible(true);
		table_2.setFont(SWTResourceManager.getFont("Segoe UI", 8, SWT.NORMAL));
		
		TableColumn tableColumn_1 = new TableColumn(table_2, SWT.NONE);
		tableColumn_1.setWidth(40);
		
		TableColumn tableColumn_3 = new TableColumn(table_2, SWT.NONE);
		tableColumn_3.setWidth(60);
		tableColumn_3.setText("Result");
		
		TableColumn tableColumn_4 = new TableColumn(table_2, SWT.NONE);
		tableColumn_4.setWidth(60);
		tableColumn_4.setText("On");
		
		TabFolder tabFolder_2 = new TabFolder(sashForm_2, SWT.NONE);
		
		TabItem tabItem = new TabItem(tabFolder_2, 0);
		tabItem.setText("Edit");
		
		Composite composite_9 = new Composite(tabFolder_2, SWT.NONE);
		tabItem.setControl(composite_9);
		composite_9.setLayout(new FormLayout());
		
		TabItem tabItem_1 = new TabItem(tabFolder_2, 0);
		tabItem_1.setText("New");
		
		Composite composite_10 = new Composite(tabFolder_2, SWT.NONE);
		tabItem_1.setControl(composite_10);
		composite_10.setLayout(new FormLayout());
		sashForm_2.setWeights(new int[] {220, 375});
			
		TabItem tbtmPerferences = new TabItem(tabFolder, SWT.NONE);
		tbtmPerferences.setText("Preferences");
		
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
		tbtmAbout.setText("About");
		
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
		poppulateScannerOptions();
		poppulateGameLangs();
		poppulateResolutions();
		poppulateDiagnoticsControls();
		poppulateDiagnoticsStatus();
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
			
			winrateStr = tracker.getTotalWins() + "-" + tracker.getTotalLosses(mode) + winrateStr;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
//		lblWinrate.setText(winrateStr);
//		lblArenaScoreStatus.setText(score);
//		lblMyClassStatus.setText(hero);
//		lblLatestGameStatus.setText(latest + "");
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
		
	private void poppulateOverviewTable(){
		int selected = table.getSelectionIndex();
		table.removeAll();
		
		for(int heroId = 0; heroId < heroesList.getTotal(); heroId++){
			fillTable(heroId);
		}
		
		//fill the unknown heroes as well
		fillTable(-1);
		
		table.select(selected);
	}
	
	private void createLabels(){	
		for(int i = 0; i < lblStatus.length; i++){
			lblStatus[i] = new Label(grpCurrentStats, SWT.NONE);
			lblStatus[i].setText("...................................................................");
		}
	}
	
	private Image resize(Image image, int width, int height) {
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
		}
		
		return HearthReader.UNKNOWNMODE;
	}
	
	private void fillTable(int heroId){
		TableItem tableItem_1 = new TableItem(table, SWT.NONE);
		float sixplus = 0, overall = 0;
		int wins = 0;
		int losses = 0;
		Image heroImg;
		int mode = this.getMode();
		
		try {
			wins = tracker.getTotalWinsByHero(mode, heroId);
			losses = tracker.getTotalLossesByHero(mode, heroId);
			sixplus = tracker.getWinRateByHeroSpecial(mode, heroId);
			overall = tracker.getWinRateByHero(mode, heroId);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		heroImg = new Image(display, "." + File.separator + "images" + File.separator + heroesList.getHeroName(heroId) + "-s.png");
		heroImg = resize(heroImg, 24, 24);
		tableItem_1.setImage(0, heroImg);
		
		if( !(overall > -1 && sixplus > -1) ){
			return;
		}
		
		tableItem_1.setText(1,   wins + "");
		tableItem_1.setText(2,   losses + "");
		
		if(overall > -1){
			tableItem_1.setText(3,  new DecimalFormat("0.00").format(overall*100));
		}
		
		if(sixplus > -1){
			tableItem_1.setText(4,  new DecimalFormat("0.00").format(sixplus*100));
		}
		
	}
}

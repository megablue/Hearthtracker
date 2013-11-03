import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Date;

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


public class HearthUI {

	protected Shell shlHearthtracker;
	private Composite composite_2;
	private Table tableOverview;
	private Label lblLatestGameStatus;
	private Label lblWinrate;
	private Label lblMyClassStatus;
	private Label lblArenaScoreStatus;
	private CCombo cmbGameLang;
	private Button btnEnableScanner;
	private Button[] btnScanSpeed = new Button[3];
	private CCombo cmbGameRes;
	private Button btnAutoPing;
	private Button btnVisualizeNow;
	private Label lblLastSeen;
	private Label lblLastscreencoordinate;
	private Label lblLastScanSubArea;
	
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
	
	Thread hearththread;
	
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
		
		window = new HearthUI();
		tracker = new Tracker();
		hearth = new HearthReader(tracker, setting.gameLang, setting.gameWidth, setting.gameHeight, debugMode);
		
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
    			// TODO Auto-generated catch block
    			//e.printStackTrace();
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
		shlHearthtracker.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent arg0) {
				window.poppulateOverviewTable();
				window.poppulateCurrentStats();
			}
		});
		shlHearthtracker.setSize(615, 530);
		shlHearthtracker.setText("HearthTracker - Automated Stats Tracking for Hearthstone players!");
		shlHearthtracker.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		TabFolder tabFolder = new TabFolder(shlHearthtracker, SWT.NONE);
		
		TabItem tbtmDashboard = new TabItem(tabFolder, SWT.NONE);
		tbtmDashboard.setText("Overview");
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmDashboard.setControl(composite);
		composite.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite, SWT.NONE);
		
		composite_2 = new Composite(sashForm, SWT.NONE);
		composite_2.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_1 = new SashForm(composite_2, SWT.VERTICAL);
		
		Group grpCurrentStats = new Group(sashForm_1, SWT.NONE);
		grpCurrentStats.setText("Current Stats");
		grpCurrentStats.setLayout(new FormLayout());
		
		Label lblArenaScore = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblArenaScore = new FormData();
		fd_lblArenaScore.top = new FormAttachment(0, 26);
		fd_lblArenaScore.left = new FormAttachment(0, 10);
		lblArenaScore.setLayoutData(fd_lblArenaScore);
		lblArenaScore.setText("Arena Score:");
		
		Label lblNewLabel_4 = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblNewLabel_4 = new FormData();
		fd_lblNewLabel_4.left = new FormAttachment(0, 10);
		fd_lblNewLabel_4.top = new FormAttachment(lblArenaScore, 6);
		lblNewLabel_4.setLayoutData(fd_lblNewLabel_4);
		lblNewLabel_4.setText("Arena Class:");
		
		Label lblLatestGame = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblLatestGame = new FormData();
		fd_lblLatestGame.top = new FormAttachment(lblNewLabel_4, 6);
		fd_lblLatestGame.left = new FormAttachment(lblArenaScore, 0, SWT.LEFT);
		lblLatestGame.setLayoutData(fd_lblLatestGame);
		lblLatestGame.setText("Latest Game:");
		
		lblLatestGameStatus = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblLatestGameStatus = new FormData();
		fd_lblLatestGameStatus.right = new FormAttachment(100, -7);
		fd_lblLatestGameStatus.left = new FormAttachment(lblLatestGame, 19);
		fd_lblLatestGameStatus.top = new FormAttachment(lblLatestGame, 0, SWT.TOP);
		lblLatestGameStatus.setLayoutData(fd_lblLatestGameStatus);
		lblLatestGameStatus.setText("...........................................");
		
		Label lblNewLabel_3 = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblNewLabel_3 = new FormData();
		fd_lblNewLabel_3.bottom = new FormAttachment(lblArenaScore, -6);
		fd_lblNewLabel_3.left = new FormAttachment(lblArenaScore, 0, SWT.LEFT);
		lblNewLabel_3.setLayoutData(fd_lblNewLabel_3);
		lblNewLabel_3.setText("Games Played:");
		
		lblWinrate = new Label(grpCurrentStats, SWT.NONE);
		FormData fd_lblWinrate = new FormData();
		fd_lblWinrate.top = new FormAttachment(0, 5);
		fd_lblWinrate.left = new FormAttachment(0, 97);
		lblWinrate.setLayoutData(fd_lblWinrate);
		lblWinrate.setText(".....................");
		
		lblMyClassStatus = new Label(grpCurrentStats, SWT.NONE);
		lblMyClassStatus.setText("...........................................");
		FormData fd_lblMyClassStatus = new FormData();
		fd_lblMyClassStatus.bottom = new FormAttachment(lblLatestGameStatus, -6);
		fd_lblMyClassStatus.top = new FormAttachment(lblWinrate, 27);
		fd_lblMyClassStatus.right = new FormAttachment(100, -79);
		fd_lblMyClassStatus.left = new FormAttachment(lblNewLabel_4, 23);
		lblMyClassStatus.setLayoutData(fd_lblMyClassStatus);
		
		lblArenaScoreStatus = new Label(grpCurrentStats, SWT.NONE);
		lblArenaScoreStatus.setLayoutData(new FormData());
		lblArenaScoreStatus.setText("...........................................");
		FormData fd_label = new FormData();
		fd_label.bottom = new FormAttachment(lblMyClassStatus, -6);
		fd_label.right = new FormAttachment(100, -73);
		fd_label.top = new FormAttachment(lblArenaScore, 0, SWT.TOP);
		fd_label.left = new FormAttachment(lblLatestGameStatus, 0, SWT.LEFT);
		lblArenaScoreStatus.setLayoutData(fd_label);
		
		Composite composite_5 = new Composite(sashForm_1, SWT.NONE);
		composite_5.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group grpAbout = new Group(composite_5, SWT.NONE);
		grpAbout.setText("About");
		grpAbout.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite composite_3 = new Composite(grpAbout, SWT.NONE);
		composite_3.setLayout(new GridLayout(1, false));
		
		Label lblVersion = new Label(composite_3, SWT.NONE);
		lblVersion.setText("HearthTracker v1.0.6 Beta");
		
		Label lblCopyrightc = new Label(composite_3, SWT.NONE);
		lblCopyrightc.setText("Copyright \u00A9 2013 megablue");
		
		Label lblWebsite = new Label(composite_3, SWT.NONE);
		lblWebsite.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://hearthtracker.blogspot.com").toURI());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		lblWebsite.setText("Website:  hearthtracker.blogspot.com");
		
		StyledText styledText = new StyledText(composite_3, SWT.READ_ONLY | SWT.WRAP);
		styledText.setText("HearthTracker is designed specifically to automate and ease score tracking for Hearthstone enthusiasts. It is coded by megablue. He first created the prototype to display arena score on his stream. Later, realizing it might help a lot of players and streamers, he continued to add new features and refine the code. He still has a lot of interesting ideas that are yet to be implemented. A lot of time and efforts need to be invested into it in order to implement all the exciting features. He hopes that you can show your support by donating. Your support will be greatly appreciated and keep the project alive!");
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		Composite composite_4 = new Composite(sashForm_1, SWT.NONE);
		composite_4.setLayout(new FillLayout(SWT.HORIZONTAL));
		
		Group grpSupportTheProject = new Group(composite_4, SWT.NONE);
		grpSupportTheProject.setText("Support the project!");
		grpSupportTheProject.setLayout(new FormLayout());
		
		Label lblPaypal = new Label(grpSupportTheProject, SWT.NONE);
		lblPaypal.setText("");
		FormData fd_lblPaypal = new FormData();
		fd_lblPaypal.top = new FormAttachment(0);
		fd_lblPaypal.left = new FormAttachment(0, 71);
		fd_lblPaypal.bottom = new FormAttachment(0, 58);
		fd_lblPaypal.right = new FormAttachment(0, 217);
		lblPaypal.setLayoutData(fd_lblPaypal);
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
		sashForm_1.setWeights(new int[] {110, 278, 80});
		
		tableOverview = new Table(sashForm, SWT.BORDER | SWT.FULL_SELECTION);
		tableOverview.setLinesVisible(true);
		tableOverview.setHeaderVisible(true);
		
		TableColumn tblclmnClass = new TableColumn(tableOverview, SWT.CENTER);
		tblclmnClass.setText("H");
		tblclmnClass.setToolTipText("Your class");
		tblclmnClass.setWidth(34);
		
		TableColumn tblclmnWins = new TableColumn(tableOverview, SWT.RIGHT);
		tblclmnWins.setToolTipText("% for 6 wins or more per arena session");
		tblclmnWins.setWidth(50);
		tblclmnWins.setText("6+");
		
		TableColumn tblclmnWin = new TableColumn(tableOverview, SWT.RIGHT);
		tblclmnWin.setToolTipText("Total wins/losses in %");
		tblclmnWin.setWidth(48);
		tblclmnWin.setText("Win %");
		
		TableColumn tblclmnTotal = new TableColumn(tableOverview, SWT.RIGHT);
		tblclmnTotal.setToolTipText("Total wins");
		tblclmnTotal.setWidth(50);
		tblclmnTotal.setText("Wins");
		
		TableColumn tblclmnNewColumn = new TableColumn(tableOverview, SWT.RIGHT);
		tblclmnNewColumn.setToolTipText("Total losses");
		tblclmnNewColumn.setWidth(50);
		tblclmnNewColumn.setText("Losses");
		
		//tableItem_1.setT
		//sashForm.setWeights(new int[] {263, 312});
		
		TabItem tbtmPerferences = new TabItem(tabFolder, SWT.NONE);
		tbtmPerferences.setText("Preferences");
		
		Composite composite_1 = new Composite(tabFolder, SWT.NONE);
		tbtmPerferences.setControl(composite_1);
		composite_1.setLayout(new GridLayout(1, false));
		
		Group grpGeneral = new Group(composite_1, SWT.NONE);
		grpGeneral.setText("General");
		grpGeneral.setLayout(new GridLayout(4, false));
		GridData gd_grpGeneral = new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1);
		gd_grpGeneral.heightHint = 114;
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
		cmbGameLang.setItems(new String[] {"enUS"});
		cmbGameLang.setVisibleItemCount(1);
		cmbGameLang.setText("enUS");
		
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
		gd_grpDiagnostics.heightHint = 310;
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
		shlHearthtracker.setTabList(new Control[]{tabFolder});

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

		try {
			winrate = tracker.getOverallWinRate();
			
			if(winrate >= 0){
				winrateStr += " (" + new DecimalFormat("#.##").format(winrate) + "% )";
			}
			
			winrateStr = tracker.getTotalWins() + "-" + tracker.getTotalLosses() + winrateStr;
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String score = hearth.getLastArenaResult();
		String hero = hearth.getMyArenaHero();
		String latest = new String(hearth.getMatchStatus());
		
		lblWinrate.setText(winrateStr);
		lblArenaScoreStatus.setText(score);
		lblMyClassStatus.setText(hero);
		lblLatestGameStatus.setText(latest + "");	
		composite_2.layout();
	}
	
	private void poppulateOverviewTable(){	
		tableOverview.removeAll();
		
		for(int heroId = 0; heroId < heroesList.getTotal(); heroId++){
			fillWinrate(heroId);
		}
		
		//fill the unknown heroes as well
		fillWinrate(-1);
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
	
	private void fillWinrate(int heroId){
		TableItem tableItem_1 = new TableItem(tableOverview, SWT.NONE);
		float sixplus = 0, overall = 0;
		Image heroImg;
		
		try {
			sixplus = tracker.getWinRateByHeroSpecial(heroId);
			overall = tracker.getWinRateByHero(heroId);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		heroImg = new Image(display, "." + File.separator + "images" + File.separator + heroesList.getHeroName(heroId) + "-s.png");
		heroImg = resize(heroImg, 28, 28);
		tableItem_1.setImage(0, heroImg);

		//tableItem_1.setText(1, heroesList.getHeroLabel(heroId));
		
		if(sixplus > -1){
			tableItem_1.setText(1,  new DecimalFormat("0.00").format(sixplus*100));
		}
		
		if(overall > -1){
			tableItem_1.setText(2,  new DecimalFormat("0.00").format(overall*100));
		}
		
		try {
			tableItem_1.setText(3,  tracker.getTotalWinsByHero(heroId) + "");
			tableItem_1.setText(4,  tracker.getTotalLossesByHero(heroId) + "");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

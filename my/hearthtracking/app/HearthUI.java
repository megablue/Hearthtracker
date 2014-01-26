package my.hearthtracking.app;

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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Map.Entry;
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
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
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

import org.eclipse.swt.widgets.DateTime;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.TrayItem;
import org.eclipse.swt.events.MenuDetectListener;
import org.eclipse.swt.events.MenuDetectEvent;

import java.text.DecimalFormat;

import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;

@SuppressWarnings({ "unused", "deprecation" })
public class HearthUI {	
	protected Shell shlHearthtracker;
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
	static boolean debugMode = HearthHelper.isDevelopmentEnvironment();
	
	private HearthScannerManager hearthScanner;
	private HearthTracker tracker;
	private HearthConfigurator config = new HearthConfigurator();
	
	private HearthSetting setting = MainLoader.setting;
	private HearthGameLangList gameLanguages = MainLoader.gameLanguages;
	private HearthResolutionsList gameResolutions = MainLoader.gameResolutions;
	private HearthDecks decks = HearthDecks.getInstance();
	private HearthHeroesList heroesList = MainLoader.heroesList;
	private HearthULangsList uiLangsList = MainLoader.uiLangsList;
	
	private Table table;
	private Table matchesTable;
	private Table arenaTable;
	private TabFolder tabFolder_1;
	private TabFolder tabFolder_2; 
	private TabItem tbtmMatchesEdit;
	private TabItem tbtmMatchesNew;
	private TabItem tbtmArenaEdit;
	private TabItem tbtmArenaNew;
	private Image[] heroImgs;
	private Composite composite_9;
	
	private Text txtWebSyncKey;
	private Composite composite_11;
	private Spinner spXOffset;
	private Spinner spYOffset;

	private Button btnAlwaysScan;

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
	private Button btnPopup;
	private Button btnRearrangeDeck;

	public static HearthLanguageManager lang = HearthLanguageManager.getInstance();
	private CCombo cbUILangs;
	
	private boolean restart = false;

	public HearthUI(HearthScannerManager s, HearthTracker t){
		hearthScanner = s;
		tracker = t;
		init();
	}
	
	public boolean isRestart(){
		return restart;
	}
	
	public void setRetart(){
		restart = true;
	}
	
	public void init(){
		heroImgs = new Image[heroesList.getTotal()+1];
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			heroImgs[i+1] = new Image(display, String.format(HearthFilesNameManager.miniHeroImage, heroesList.getHeroName(i)));
			heroImgs[i+1] = resize(heroImgs[i+1], 24, 24);
		}
	}
	
	public Shell getShell(){
		return shlHearthtracker;
	}
	
    private void openDonationDialog(){
		try {
			int totalMatches = tracker.getTotalMatches();
			
			//beg for donation every 1000 games tracked
			if(totalMatches/1000 > setting.tracker){
				new HearthDonate(shlHearthtracker, totalMatches).open();
				setting.tracker = totalMatches/1000;
				savePreferences();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}
    }
    
	/**
	 * Open the this.
	 */
	/**
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		display = Display.getDefault();
		createContents();
		shlHearthtracker.setImage(new Image( display, HearthFilesNameManager.logo128));
		shlHearthtracker.open();
		shlHearthtracker.layout();
		Date lastUpdate = new Date(); 
		openDonationDialog();

		while (!shlHearthtracker.isDisposed()) {
			if(tracker.isDirty()){
				this.fillOverviewTable();
				this.fillArenaTable();
				this.fillMatchesTable();
				this.updateStatus();
				this.fillDeckWinRate();
				tracker.clearDirty();
			}
			
			if(hearthScanner.isDirty()){
				this.updateStatus();
			}
			
			if(new Date().getTime() - lastUpdate.getTime() > 2000){	
				this.poppulateDiagnoticsStatus();
				lastUpdate = new Date();
			}
						
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	private void shutdown(){
		shlHearthtracker.getShell().dispose();
	}

	/**
	 * Create contents of the window
	 * @wbp.parser.entryPoint
	 */
	protected void createContents() {
		FontData[] fontData = display.getSystemFont().getFontData();
		fontData[0].setHeight(8);

		shlHearthtracker = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX));
		shlHearthtracker.addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent arg0) {
				shlHearthtracker.setMinimized(true);
				arg0.doit = false;
			}
		});
		shlHearthtracker.setSize(620, 456);
		shlHearthtracker.setText(
				"HearthTracker - " + lang.t("Automated Stats Tracking for Hearthstone enthusiasts!")
		);
		
		if(MainLoader.experimental > 0){
			shlHearthtracker.setText(
					"HearthTracker - " + lang.t("Experimental build %s", MainLoader.experimental)
			);
		}
		
		shlHearthtracker.setLayout(new FillLayout(SWT.HORIZONTAL));

		Menu menu = new Menu(shlHearthtracker, SWT.BAR);
		shlHearthtracker.setMenuBar(menu);
		
		MenuItem mntmNewSubmenu = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu.setText(lang.t("&File"));
		
		Menu menu_1 = new Menu(mntmNewSubmenu);
		mntmNewSubmenu.setMenu(menu_1);
		
		MenuItem mntmNewItem = new MenuItem(menu_1, SWT.NONE);
		mntmNewItem.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				shutdown();
			}
		});
		mntmNewItem.setText(lang.t("E&xit"));
		
		MenuItem mntmNewSubmenu_1 = new MenuItem(menu, SWT.CASCADE);
		mntmNewSubmenu_1.setText(lang.t("&Edit"));
		
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
		mntmNewItem_1.setText(lang.t("Last Match -> Won"));
		
		MenuItem mntmLastMatch = new MenuItem(menu_2, SWT.NONE);
		mntmLastMatch.setText(lang.t("Last Match -> Lost"));
		
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
		mntmLastMatch_1.setText(lang.t("Last Match -> went first"));
		
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
		mntmNewItem_2.setText(lang.t("Last Match -> went second"));
		
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
		
		TabFolder tabFolder = new TabFolder(shlHearthtracker, SWT.NONE);
		
		TabItem tbtmDashboard = new TabItem(tabFolder, SWT.NONE);
		tbtmDashboard.setText(lang.t("&Overview"));
		
		Composite composite = new Composite(tabFolder, SWT.NONE);
		tbtmDashboard.setControl(composite);
		composite.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm = new SashForm(composite, SWT.NONE);
		sashForm.setLayoutData(new RowData(598, 373));
		
		grpStats = new Group(sashForm, SWT.NONE);
		grpStats.setText(lang.t("Stats"));
		grpStats.setLayout(new FormLayout());

		table = new Table(grpStats, SWT.FULL_SELECTION);
		table.setFont(new Font(display,fontData[0]));
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
		tblclmnNewColumn_1.setText(lang.t("Wins"));
		
		TableColumn tblclmnNewColumn_2 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_2.setWidth(55);
		tblclmnNewColumn_2.setText(lang.t("Losses"));
		
		TableColumn tblclmnWin = new TableColumn(table, SWT.RIGHT);
		tblclmnWin.setWidth(55);
		tblclmnWin.setText(lang.t("Win %%"));
		
		TableColumn tblclmnNewColumn_3 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_3.setWidth(55);
		tblclmnNewColumn_3.setText(lang.t("7+ %%"));
		
		TableColumn tblclmnNewColumn_4 = new TableColumn(table, SWT.RIGHT);
		tblclmnNewColumn_4.setWidth(72);
		tblclmnNewColumn_4.setText(lang.t("Total Runs"));
		
		cmbStatsMode = new Combo(grpStats, SWT.READ_ONLY);
		cmbStatsMode.setItems(
				new String[] {
						lang.t("Arena mode (played as)"), 
						lang.t("Ranked mode (played as)"), 
						lang.t("Unranked mode (played as)"), 
						lang.t("Challenge mode (played as)"), 
						lang.t("Practice mode (played as)")
					}
		);
		FormData fd_cmbStatsMode = new FormData();
		fd_cmbStatsMode.top = new FormAttachment(0, 10);
		fd_cmbStatsMode.left = new FormAttachment(0, 10);
		cmbStatsMode.setLayoutData(fd_cmbStatsMode);
		cmbStatsMode.select(0);
		
		grpCurrentStats = new Group(sashForm, SWT.NONE);
		grpCurrentStats.setText(
				lang.t("Sideboard")
		);
		grpCurrentStats.setLayout(null);
		
		styledTextStatus = new StyledText(grpCurrentStats, SWT.READ_ONLY);
		styledTextStatus.setBounds(10, 23, 211, 340);
		styledTextStatus.setFont(new Font(display,fontData[0]));
		
		sashForm.setWeights(new int[] {365, 230});
		GridData gd_lblNewLabel_15 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel_15.widthHint = 60;
		
		TabItem tbtmRecords = new TabItem(tabFolder, SWT.NONE);
		tbtmRecords.setText(
			lang.t("&Matches")
		);
		
		Composite composite_2 = new Composite(tabFolder, SWT.NONE);
		tbtmRecords.setControl(composite_2);
		composite_2.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_1 = new SashForm(composite_2, SWT.NONE);
		sashForm_1.setLayoutData(new RowData(598, 373));
		
		matchesTable = new Table(sashForm_1, SWT.FULL_SELECTION);
		matchesTable.setLinesVisible(true);
		matchesTable.setHeaderVisible(true);
		matchesTable.setFont(new Font(display,fontData[0]));
		
		TableColumn tblclmnAs = new TableColumn(matchesTable, SWT.NONE);
		tblclmnAs.setText(lang.t("As"));
		tblclmnAs.setWidth(29);
		
		TableColumn tblclmnVs = new TableColumn(matchesTable, SWT.NONE);
		tblclmnVs.setWidth(29);
		tblclmnVs.setText(lang.t("Vs"));
		
		TableColumn tblclmnMode = new TableColumn(matchesTable, SWT.NONE);
		tblclmnMode.setWidth(68);
		tblclmnMode.setText(lang.t("Mode"));
		
		TableColumn tblclmnResult = new TableColumn(matchesTable, SWT.NONE);
		tblclmnResult.setWidth(45);
		tblclmnResult.setText(lang.t("Result"));
		
		TableColumn tblclmnOn = new TableColumn(matchesTable, SWT.NONE);
		tblclmnOn.setWidth(88);
		tblclmnOn.setText(lang.t("On"));
		
		tabFolder_1 = new TabFolder(sashForm_1, SWT.NONE);
		
		tbtmMatchesEdit = new TabItem(tabFolder_1, SWT.NONE);
		tbtmMatchesEdit.setText(lang.t("&Edit"));
		
		tbtmMatchesNew = new TabItem(tabFolder_1, SWT.NONE);
		tbtmMatchesNew.setText(lang.t("&New"));
		
		Composite composite_5  = new Composite(tabFolder_1, SWT.NONE);
		tbtmMatchesNew.setControl(composite_5);
		composite_5.setLayout(new FormLayout());
		
		TabItem tbtmArena = new TabItem(tabFolder, 0);
		tbtmArena.setText(lang.t("&Arena"));
		
		Composite composite_8 = new Composite(tabFolder, SWT.NONE);
		tbtmArena.setControl(composite_8);
		composite_8.setLayout(new RowLayout(SWT.HORIZONTAL));
		
		SashForm sashForm_2 = new SashForm(composite_8, SWT.NONE);
		sashForm_2.setLayoutData(new RowData(598, 373));
		
		arenaTable = new Table(sashForm_2, SWT.FULL_SELECTION);
		arenaTable.setLinesVisible(true);
		arenaTable.setHeaderVisible(true);
		arenaTable.setFont(new Font(display,fontData[0]));
		
		TableColumn tblclmnAs_1 = new TableColumn(arenaTable, SWT.NONE);
		tblclmnAs_1.setText(lang.t("As"));
		tblclmnAs_1.setWidth(29);
		
		TableColumn tableColumn_3 = new TableColumn(arenaTable, SWT.NONE);
		tableColumn_3.setWidth(45);
		tableColumn_3.setText(lang.t("Result"));
		
		TableColumn tableColumn_4 = new TableColumn(arenaTable, SWT.NONE);
		tableColumn_4.setWidth(88);
		tableColumn_4.setText(lang.t("On"));
		
		tabFolder_2 = new TabFolder(sashForm_2, SWT.NONE);
		
		tbtmArenaEdit = new TabItem(tabFolder_2, 0);
		tbtmArenaEdit.setText(lang.t("&Edit"));
		
		TabItem tbtmArenaNew = new TabItem(tabFolder_2, 0);
		tbtmArenaNew.setText(lang.t("&New"));
		
		TabItem tbtmDeck = new TabItem(tabFolder, SWT.NONE);
		tbtmDeck.setText(lang.t("De&cks"));
		
		Composite composite_6 = new Composite(tabFolder, SWT.NONE);
		tbtmDeck.setControl(composite_6);
		composite_6.setLayout(new GridLayout(7, false));
		new Label(composite_6, SWT.NONE);
		
		Label lblDeck = new Label(composite_6, SWT.NONE);
		lblDeck.setText(lang.t("Slot #1"));
		
		Label lblNewLabel_17 = new Label(composite_6, SWT.NONE);
		
		Label lblDeck_1 = new Label(composite_6, SWT.NONE);
		lblDeck_1.setText(lang.t("Slot #2"));
		
		Label lblNewLabel_20 = new Label(composite_6, SWT.NONE);
		
		Label lblDeck_2 = new Label(composite_6, SWT.NONE);
		lblDeck_2.setText(lang.t("Slot #3"));
		
		Label label_3 = new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[0] = text_1 = new Text(composite_6, SWT.BORDER);
		GridData gd_text_1 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_1.widthHint = 85;
		text_1.setLayoutData(gd_text_1);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[1] = text_2 = new Text(composite_6, SWT.BORDER);
		GridData gd_text_2 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_2.widthHint = 85;
		text_2.setLayoutData(gd_text_2);
		new Label(composite_6, SWT.NONE);
		
		txtDecks[2] = text_3 = new Text(composite_6, SWT.BORDER);
		GridData gd_text_3 = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_text_3.widthHint = 85;
		text_3.setLayoutData(gd_text_3);
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
		lblDeckdsds.setText(lang.t("Slot #4"));
		new Label(composite_6, SWT.NONE);
		
		Label lblDeckxx= new Label(composite_6, SWT.NONE);
		lblDeckxx.setText(lang.t("Slot #5"));
		new Label(composite_6, SWT.NONE);
		
		Label lblDeckss = new Label(composite_6, SWT.NONE);
		lblDeckss.setText(lang.t("Slot #6"));
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
		lblDeck_6.setText(lang.t("Slot #7"));
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_15 = new Label(composite_6, SWT.NONE);
		lblNewLabel_15.setText(lang.t("Slot #8"));
		new Label(composite_6, SWT.NONE);
		
		Label lblNewLabel_16 = new Label(composite_6, SWT.NONE);
		lblNewLabel_16.setText(lang.t("Slot #9"));
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
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		new Label(composite_6, SWT.NONE);
		
		btnRearrangeDeck = new Button(composite_6, SWT.NONE);
		GridData gd_btnR = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_btnR.widthHint = 85;
		btnRearrangeDeck.setLayoutData(gd_btnR);
		btnRearrangeDeck.setText(lang.t("Trim Empty &Slots "));
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
		link_2.setText(lang.t("%sHow to use the Decks Manager%s", "<a>", "</a>"));
		new Label(composite_6, SWT.NONE);
		
		TabItem tbtmpreferences = new TabItem(tabFolder, SWT.NONE);
		tbtmpreferences.setText(lang.t("&Preferences"));
		
		ScrolledComposite scrolledComposite = new ScrolledComposite(tabFolder, SWT.BORDER | SWT.V_SCROLL);
		scrolledComposite.setBackground(SWTResourceManager.getColor(SWT.COLOR_WIDGET_HIGHLIGHT_SHADOW));
		scrolledComposite.setExpandVertical(true);
		scrolledComposite.setExpandHorizontal(true);
		tbtmpreferences.setControl(scrolledComposite);
		
		ExpandBar expandBar = new ExpandBar(scrolledComposite, SWT.NONE);
		
		ExpandItem xpndtmGeneral = new ExpandItem(expandBar, SWT.NONE);
		xpndtmGeneral.setExpanded(true);
		xpndtmGeneral.setText(lang.t("General"));
		
		Composite composite_7 = new Composite(expandBar, SWT.NONE);
		xpndtmGeneral.setControl(composite_7);
		xpndtmGeneral.setHeight(150);
		composite_7.setLayout(new GridLayout(3, false));
		
		Label lblNewLabel_14 = new Label(composite_7, SWT.NONE);
		lblNewLabel_14.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_14.setBounds(144, 53, 81, 15);
		lblNewLabel_14.setText(lang.t("Batte.net server"));
		new Label(composite_7, SWT.NONE);
		
		cbServer = new CCombo(composite_7, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_cbServer = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		gd_cbServer.widthHint = 340;
		cbServer.setLayoutData(gd_cbServer);
		cbServer.setVisibleItemCount(4);
		cbServer.setEditable(false);
		cbServer.setBounds(231, 49, 150, 21);
		
		Link link = new Link(composite_7, SWT.NONE);
		link.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
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
		link.setBounds(95, 80, 136, 15);
		link.setText(lang.t("%sHearthTracker Web Sync Key%s", "<a>", "</a>"));
		new Label(composite_7, SWT.NONE);
		
		txtWebSyncKey = new Text(composite_7, SWT.BORDER);
		txtWebSyncKey.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		txtWebSyncKey.setBounds(232, 78, 324, 21);
		
		Label lblNewLabel_4 = new Label(composite_7, SWT.NONE);
		lblNewLabel_4.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_4.setText(lang.t("User Interface Language"));
		new Label(composite_7, SWT.NONE);
		scrolledComposite.setContent(expandBar);
		scrolledComposite.setMinSize(expandBar.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		cbUILangs = new CCombo(composite_7, SWT.BORDER | SWT.READ_ONLY);
		cbUILangs.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		cbUILangs.setVisibleItemCount(13);
		cbUILangs.setItems(new String[] {});
		cbUILangs.setEditable(false);
		
		Label lblNewLabel_21 = new Label(composite_7, SWT.NONE);
		lblNewLabel_21.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_21.setBounds(125, 112, 101, 15);
		lblNewLabel_21.setText(
			lang.t("Notification Popup")
		);
		new Label(composite_7, SWT.NONE);
		
		btnPopup = new Button(composite_7, SWT.CHECK);
		btnPopup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
		btnPopup.setBounds(232, 111, 93, 16);
		btnPopup.setText(lang.t("Enable"));
		
		ExpandItem xpndtmAdvanced = new ExpandItem(expandBar, SWT.NONE);
		xpndtmAdvanced.setExpanded(true);
		xpndtmAdvanced.setText(lang.t("Advanced"));
		
		Composite composite_10 = new Composite(expandBar, SWT.NONE);
		xpndtmAdvanced.setControl(composite_10);
		xpndtmAdvanced.setHeight(160);
		composite_10.setLayout(new GridLayout(9, false));
		
		Label lblDetect = new Label(composite_10, SWT.NONE);
		lblDetect.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblDetect.setText(lang.t("Auto Detect Game Resolution"));
		
		btnAutoDetectGameRes = new Button(composite_10, SWT.CHECK);
		btnAutoDetectGameRes.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 8, 1));
		btnAutoDetectGameRes.setToolTipText(
			lang.t("It is recommended to enable it specially your desktop is running at same resolution with your Hearthstone resolution.")
		);
		btnAutoDetectGameRes.setText(lang.t("Enable"));
		
		Label lblGameResolution = new Label(composite_10, SWT.NONE);
		lblGameResolution.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblGameResolution.setText(lang.t("Game Resolution (if auto detect failed)"));
		
		cmbGameRes = new CCombo(composite_10, SWT.BORDER | SWT.READ_ONLY);
		GridData gd_cmbGameRes = new GridData(SWT.LEFT, SWT.CENTER, false, false, 5, 1);
		gd_cmbGameRes.widthHint = 356;
		cmbGameRes.setLayoutData(gd_cmbGameRes);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		
//		Button button_3 = new Button(composite_10, SWT.RADIO);
//		button_3.setBounds(379, 155, 46, 16);
//		btnScanSpeed[3] = button_3;
//		button_3.setText("Slowest");
		
		Label lblNewLabel_11 = new Label(composite_10, SWT.NONE);
		lblNewLabel_11.setText(lang.t("Override Offsets (Multi monitors dirty fix)"));
		
		Label lblNewLabel_12 = new Label(composite_10, SWT.NONE);
		lblNewLabel_12.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_12.setText(lang.t("X offset"));
		
		spXOffset = new Spinner(composite_10, SWT.BORDER);
		spXOffset.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		spXOffset.setMaximum(10240);
		spXOffset.setMinimum(-10240);
		
		Label lblYOffset = new Label(composite_10, SWT.NONE);
		lblYOffset.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblYOffset.setText(lang.t("Y offset"));
		
		spYOffset = new Spinner(composite_10, SWT.BORDER);
		spYOffset.setMaximum(10240);
		spYOffset.setMinimum(-10240);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		
		Label lblNewLabel_2 = new Label(composite_10, SWT.NONE);
		lblNewLabel_2.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_2.setText(lang.t("Scanner"));
		
		btnEnableScanner = new Button(composite_10, SWT.CHECK);
		btnEnableScanner.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
		btnEnableScanner.setSelection(true);
		btnEnableScanner.setText(lang.t("Enable"));
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		
		Label lblNewLabel = new Label(composite_10, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel.setText(lang.t("Scan Speed"));
		
		Button button_0 = new Button(composite_10, SWT.RADIO);
		GridData gd_button_0 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_button_0.widthHint = 75;
		button_0.setLayoutData(gd_button_0);
		btnScanSpeed[3] = button_0;
		button_0.setText(lang.t("Really Fast"));
		
		Button button = new Button(composite_10, SWT.RADIO);
		btnScanSpeed[0] = button;
		button.setText(lang.t("Fast"));
		new Label(composite_10, SWT.NONE);
		
		Button button_1 = new Button(composite_10, SWT.RADIO);
		btnScanSpeed[1] = button_1;
		button_1.setText(lang.t("Intermediate"));
		
		Button button_2 = new Button(composite_10, SWT.RADIO);
		btnScanSpeed[2] = button_2;
		button_2.setText(lang.t("Slow"));
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		
		Label lblNewLabel_13 = new Label(composite_10, SWT.NONE);
		lblNewLabel_13.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNewLabel_13.setText(lang.t("Forced Scanning"));
		
		btnAlwaysScan = new Button(composite_10, SWT.CHECK);
		btnAlwaysScan.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		btnAlwaysScan.setText(lang.t("Enable"));
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);
		new Label(composite_10, SWT.NONE);

		TabItem tbtmDiagnostics = new TabItem(tabFolder, SWT.NONE);
		tbtmDiagnostics.setText(lang.t("&Tools"));
		
		Composite composite_4 = new Composite(tabFolder, SWT.NONE);
		tbtmDiagnostics.setControl(composite_4);
		composite_4.setLayout(new GridLayout(1, false));
		
		Group grpDiagnostics = new Group(composite_4, SWT.NONE);
		GridData gd_grpDiagnostics = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_grpDiagnostics.heightHint = 353;
		gd_grpDiagnostics.widthHint = 589;
		grpDiagnostics.setLayoutData(gd_grpDiagnostics);
		grpDiagnostics.setText(lang.t("Diagnostics"));
		grpDiagnostics.setLayout(null);
		
		Label lblLastSeenLabel = new Label(grpDiagnostics, SWT.NONE);
		lblLastSeenLabel.setBounds(206, 21, 48, 15);
		lblLastSeenLabel.setText(lang.t("Last seen"));
		
		lblLastSeen = new Label(grpDiagnostics, SWT.NONE);
		lblLastSeen.setBounds(260, 21, 300, 15);
		lblLastSeen.setText("..........");
		
		Label lblLastScanCoordinate = new Label(grpDiagnostics, SWT.NONE);
		lblLastScanCoordinate.setBounds(181, 41, 73, 15);
		lblLastScanCoordinate.setText(lang.t("Last scanned area"));
		
		lblLastscreencoordinate = new Label(grpDiagnostics, SWT.NONE);
		lblLastscreencoordinate.setBounds(260, 41, 300, 15);
		lblLastscreencoordinate.setText("..........");
		
		Label lblNewLabel_5 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_5.setBounds(157, 61, 97, 15);
		lblNewLabel_5.setText(lang.t("Last scanned sub-area"));
		
		lblLastScanSubArea = new Label(grpDiagnostics, SWT.NONE);
		lblLastScanSubArea.setBounds(260, 61, 300, 15);
		lblLastScanSubArea.setText("..........");
		
		Label lblAutoPingLabel = new Label(grpDiagnostics, SWT.NONE);
		lblAutoPingLabel.setBounds(132, 113, 117, 15);
		lblAutoPingLabel.setText(lang.t("Visualize scanned area"));
		
		btnAutoPing = new Button(grpDiagnostics, SWT.CHECK);
		btnAutoPing.setBounds(255, 112, 56, 16);
		btnAutoPing.setToolTipText(lang.t("Visualize scanned areas after Hearthstone being out of sight for more than 1 min."));
		btnAutoPing.setText(lang.t("Enable"));
		
		Label lblNewLabel_6 = new Label(grpDiagnostics, SWT.NONE);
		lblNewLabel_6.setBounds(171, 138, 78, 15);
		lblNewLabel_6.setText(lang.t("Diagnotic Tool"));
		
		btnVisualizeNow = new Button(grpDiagnostics, SWT.NONE);
		btnVisualizeNow.setBounds(255, 133, 83, 25);
		btnVisualizeNow.setText(lang.t("Visualize now"));
		
		Label label_2 = new Label(grpDiagnostics, SWT.SEPARATOR | SWT.HORIZONTAL);
		label_2.setBounds(10, 93, 575, 2);
		
		TabItem tbtmAbout = new TabItem(tabFolder, SWT.NONE);
		tbtmAbout.setText(lang.t("A&bout"));
		
		Group grpAbout = new Group(tabFolder, SWT.NONE);
		tbtmAbout.setControl(grpAbout);
		grpAbout.setText(lang.t("About"));
		grpAbout.setLayout(new FillLayout(SWT.VERTICAL));
		
		Composite composite_3 = new Composite(grpAbout, SWT.NONE);
		composite_3.setLayout(new FormLayout());
		
		Label lblNewLabel_7 = new Label(composite_3, SWT.NONE);
		FormData fd_lblNewLabel_7 = new FormData();
		fd_lblNewLabel_7.top = new FormAttachment(0, 65);
		fd_lblNewLabel_7.left = new FormAttachment(0, 5);
		lblNewLabel_7.setLayoutData(fd_lblNewLabel_7);
		lblNewLabel_7.setText("");
		
		Label lblNewLabel_8 = new Label(composite_3, SWT.NONE);
		FormData fd_lblNewLabel_8 = new FormData();
		fd_lblNewLabel_8.top = new FormAttachment(0, 209);
		fd_lblNewLabel_8.left = new FormAttachment(0, 5);
		lblNewLabel_8.setLayoutData(fd_lblNewLabel_8);
		lblNewLabel_8.setText("");
		
		Label lblPaypal = new Label(composite_3, SWT.NONE);
		FormData fd_lblPaypal = new FormData();
		fd_lblPaypal.bottom = new FormAttachment(0, 354);
		fd_lblPaypal.right = new FormAttachment(0, 389);
		fd_lblPaypal.top = new FormAttachment(0, 258);
		fd_lblPaypal.left = new FormAttachment(0, 236);
		lblPaypal.setLayoutData(fd_lblPaypal);
		lblPaypal.setText("");
		lblPaypal.setToolTipText(lang.t("Your support means a lot to me. Thank you for even hovering over the donate button!"));
		lblPaypal.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent arg0) {
				HearthHelper.openDonateLink();
			}
		});
		lblPaypal.setImage(new Image( display, HearthFilesNameManager.payapl));
		
		Label lblFacebook = new Label(composite_3, SWT.NONE);
		FormData fd_lblFacebook = new FormData();
		fd_lblFacebook.bottom = new FormAttachment(0, 354);
		fd_lblFacebook.right = new FormAttachment(0, 106);
		fd_lblFacebook.top = new FormAttachment(0, 258);
		fd_lblFacebook.left = new FormAttachment(0, 10);
		lblFacebook.setLayoutData(fd_lblFacebook);
		lblFacebook.setImage(new Image( display, HearthFilesNameManager.facebook));
		
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
		FormData fd_lblTLQ = new FormData();
		fd_lblTLQ.bottom = new FormAttachment(0, 354);
		fd_lblTLQ.right = new FormAttachment(0, 220);
		fd_lblTLQ.top = new FormAttachment(0, 258);
		fd_lblTLQ.left = new FormAttachment(0, 124);
		lblTLQ.setLayoutData(fd_lblTLQ);
		lblTLQ.setImage(new Image( display, HearthFilesNameManager.teamliquid));
		
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
		FormData fd_lblTwitter = new FormData();
		fd_lblTwitter.bottom = new FormAttachment(0, 354);
		fd_lblTwitter.right = new FormAttachment(0, 503);
		fd_lblTwitter.top = new FormAttachment(0, 258);
		fd_lblTwitter.left = new FormAttachment(0, 407);
		lblTwitter.setLayoutData(fd_lblTwitter);
		lblTwitter.setImage(new Image( display,  HearthFilesNameManager.twitter));
		
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
		FormData fd_lblFindUsOn = new FormData();
		fd_lblFindUsOn.right = new FormAttachment(0, 590);
		fd_lblFindUsOn.top = new FormAttachment(0, 237);
		fd_lblFindUsOn.left = new FormAttachment(0, 10);
		lblFindUsOn.setLayoutData(fd_lblFindUsOn);
		lblFindUsOn.setText(lang.t("Show us the love!"));
		
		Label label_1 = new Label(composite_3, SWT.SEPARATOR | SWT.HORIZONTAL);
		FormData fd_label_1 = new FormData();
		fd_label_1.right = new FormAttachment(0, 590);
		fd_label_1.top = new FormAttachment(0, 222);
		fd_label_1.left = new FormAttachment(0, 10);
		label_1.setLayoutData(fd_label_1);
		
		Label label = new Label(composite_3, SWT.NONE);
		FormData fd_label = new FormData();
		fd_label.top = new FormAttachment(0, 47);
		fd_label.height = 96;
		fd_label.width = 96;
		fd_label.left = new FormAttachment(0, 157);
		label.setLayoutData(fd_label);
		label.setImage(new Image( display, HearthFilesNameManager.logo));
		
		Composite composite_12 = new Composite(composite_3, SWT.NONE);
		fd_label.right = new FormAttachment(composite_12, -23);
		FormData fd_composite_12 = new FormData();
		fd_composite_12.bottom = new FormAttachment(label_1, -92);
		fd_composite_12.top = new FormAttachment(lblNewLabel_7, 0, SWT.TOP);
		fd_composite_12.right = new FormAttachment(100, -65);
		fd_composite_12.left = new FormAttachment(0, 276);
		composite_12.setLayoutData(fd_composite_12);
		composite_12.setLayout(new GridLayout(1, false));
		
		Label lblVersion = new Label(composite_12, SWT.NONE);
		lblVersion.setText(String.format("HearthTracker v%d.%d.%d",  MainLoader.version[0], MainLoader.version[1], MainLoader.version[2]));
		
		Label lblCopyrightc = new Label(composite_12, SWT.NONE);
		lblCopyrightc.setText("Copyright \u00A9 2014 megablue");
		
		Link link_1 = new Link(composite_12, SWT.NONE);
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
		link_1.setText("<a>www.HearthTracking.com</a>");

		shlHearthtracker.setTabList(new Control[]{tabFolder});

		sashForm_1.setWeights(new int[] {277, 318});
		sashForm_2.setWeights(new int[] {277, 318});
		
		Composite composite_1 = new Composite(tabFolder_1, SWT.NONE);
		createMatchesEditForm(composite_1, tbtmMatchesEdit);
		createMatchesNewForm(new Composite(tabFolder_1, SWT.NONE), 	tbtmMatchesNew);
		createArenaEditForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaEdit);
		createArenaNewForm(new Composite(tabFolder_2, SWT.NONE), 	tbtmArenaNew);
		
		populatesOffsetOptions();
		populatescannerOptions();
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
		initDecksManager();
	}
	
	private void initDecksManager(){
		btnRearrangeDeck.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				decks.rearrange();
				saveDecks();
				fillDeckSlots();
				fillDeckWinRate();
			}
		});

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
			txtDecks[i].addFocusListener(deckFocus);
		}
		
		fillDeckSlots();
		fillDeckWinRate();
	}
	
	private void fillDeckSlots(){

		for(int i = 0; i < txtDecks.length; i++){
			if(decks.list[i] != null){
				txtDecks[i].setText(decks.list[i]);
			}
			
			txtDecks[i].setData("index", i);
			lblDecks[i].setText("");
		}
	}
	
	private void fillDeckWinRate(){
		for(int i = 0; i < txtDecks.length; i++){
			String deckName = decks.list[i];
			try {
				float ranked = tracker.getWinRateByDeck(HearthScannerManager.RANKEDMODE, deckName);
				float unranked = tracker.getWinRateByDeck(HearthScannerManager.UNRANKEDMODE, deckName);
				float challenge = tracker.getWinRateByDeck(HearthScannerManager.CHALLENGEMODE, deckName);
				float practice = tracker.getWinRateByDeck(HearthScannerManager.PRACTICEMODE, deckName);
				
				String rankedS = ranked > -1 ? new DecimalFormat("0.00").format(ranked) + "%" : "-\t";
				String unrankedS = unranked > -1 ? new DecimalFormat("0.00").format(unranked) + "%" : "-";
				String challengeS = challenge > -1 ? new DecimalFormat("0.00").format(challenge) + "%" : "-\t";
				String practiceS = practice > -1 ? new DecimalFormat("0.00").format(practice) + "%" : "-";

				lblDecks[i].setText(
						lang.t("R: ") + rankedS 
					   +"\t" + lang.t("U: ") + unrankedS + "\r\n"
					   + lang.t("C: ") + challengeS
					   +"\t" + lang.t("P: ") + practiceS + "\r\n" 
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
				String result = lang.t("Unknown");
				cal.setTime(new Date(rs.getLong("STARTTIME")));
				
				if(rs.getInt("WIN") == 1){
					result = lang.t("Win");
				} else if(rs.getInt("WIN") == 0){
					result = lang.t("Lose");
				}
				
				tableItem.setData("id", rs.getInt("ID"));
				tableItem.setImage(0, heroImgs[rs.getInt("MYHEROID")+1]);
				tableItem.setImage(1, heroImgs[rs.getInt("OPPHEROID")+1]);
				tableItem.setText(2, HearthHelper.gameModeToStringLabel(rs.getInt("MODE")));
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
		lblNewLabel_3.setText(lang.t("vs"));
		
		final Combo cbMatchesEditVs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditVs.setBounds(184, 66, 90, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbMatchesEditAs.add(heroesList.getHeroLabel(i));
			cbMatchesEditVs.add(heroesList.getHeroLabel(i));
		}
		
		final Combo cbMatchesEditGoes = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGoes.setBounds(123, 101, 74, 23);
		cbMatchesEditGoes.setItems(new String[] {
			lang.t("Go First"), 
			lang.t("Go Second"), 
			lang.t("Unknown")
		});
		
		final Combo cbMatchesEditGameMode = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGameMode.setBounds(109, 25, 99, 23);
		cbMatchesEditGameMode.setItems(new String[] {
			lang.t("Unknown mode"), 
			lang.t("Arena"), 
			lang.t("Ranked"), 
			lang.t("Unranked"), 
			lang.t("Challenge"), 
			lang.t("Practice")
		});
		
		final Combo cbMatchesEditResult = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditResult.setBounds(134, 136, 49, 23);
		cbMatchesEditResult.setItems(new String[] {
				lang.t("Win"), 
				lang.t("Lose"), 
				lang.t("Unknown")
		});
		
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
		lblNewLabel_9.setText(lang.t("Played"));
		
		Label lblMinutes = new Label(composite_4, SWT.NONE);
		lblMinutes.setBounds(212, 257, 43, 15);
		lblMinutes.setText(lang.t("minutes"));
		
		final Button btnMatchesEditSave = new Button(composite_4, SWT.NONE);
		btnMatchesEditSave.setEnabled(false);
		btnMatchesEditSave.setBounds(88, 310, 36, 25);
		btnMatchesEditSave.setText(lang.t("&Save"));

		final Button btnMatchesEditDelete = new Button(composite_4, SWT.NONE);
		btnMatchesEditDelete.setEnabled(false);
		btnMatchesEditDelete.setBounds(212, 310, 45, 25);
		btnMatchesEditDelete.setText(lang.t("&Delete"));
		
		final Label lblID = new Label(composite_4, SWT.NONE);
		lblID.setBounds(10, 10, 303, 15);
		lblID.setText("ID #00000");
		lblID.setData("id", 0);
		
		final Text cbMatchesEditDeck = new Text(composite_4, SWT.BORDER);
		cbMatchesEditDeck.setBounds(88, 172, 139, 21);
		
		
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
		btnConfirmChanges.setText(lang.t("&Confirm"));
		
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
							String deck = rs.getString("deck");
							
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
							
							cbMatchesEditDeck.setText(deck);
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
				String deckName = cbMatchesEditDeck.getText();
				
				if(id == 0){
					return;
				}
				
				if(cbMatchesEditResult.getSelectionIndex() == 0){
					result = 1;
				} else if(cbMatchesEditResult.getSelectionIndex() == 1){
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
					tracker.saveModifiedMatchResult(id, gMode, myheroid, oppheroid, goes, result, totaltime, deckName);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditSave.setEnabled(true);
			}
		});
		
		btnMatchesEditDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				try {
					btnMatchesEditDelete.setEnabled(false);
					tracker.deleteMatchResult(id);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditDelete.setEnabled(true);
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
		lblNewLabel_3.setText(lang.t("vs"));
		
		final Combo cbMatchesEditVs = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditVs.setBounds(184, 66, 90, 23);
		
		for(int i = -1; i < heroesList.getTotal(); i++){
			cbMatchesEditAs.add(heroesList.getHeroLabel(i));
			cbMatchesEditVs.add(heroesList.getHeroLabel(i));
		}
		
		final Combo cbMatchesEditGoes = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGoes.setBounds(125, 123, 74, 23);
		cbMatchesEditGoes.setItems(
			new String[] {
				lang.t("Go First"), 
				lang.t("Go Second"), 
				lang.t("Unknown")
			}
		);
		
		final Combo cbMatchesEditGameMode = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditGameMode.setBounds(109, 25, 99, 23);
		cbMatchesEditGameMode.setItems(new String[] {
				lang.t("Unknown mode"), 
				lang.t("Arena"), 
				lang.t("Ranked"), 
				lang.t("Unranked"), 
				lang.t("Challenge"), 
				lang.t("Practice")
			}
		);
		
		final Combo cbMatchesEditResult = new Combo(composite_4, SWT.READ_ONLY);
		cbMatchesEditResult.setBounds(136, 165, 49, 23);
		cbMatchesEditResult.setItems(new String[] {
				lang.t("Win"), 
				lang.t("Lose"), 
				lang.t("Unknown")
			});
		
		final DateTime dtMatchesEditDate = new DateTime(composite_4, SWT.NONE);
		dtMatchesEditDate.setBounds(69, 211, 80, 24);
		
		final DateTime dtMatchesEditTime = new DateTime(composite_4, SWT.TIME);
		dtMatchesEditTime.setBounds(169, 211, 86, 24);
		
		final Spinner spMatchesEditMinute = new Spinner(composite_4, SWT.BORDER);
		spMatchesEditMinute.setBounds(148, 254, 44, 22);
		
		Label lblNewLabel_9 = new Label(composite_4, SWT.NONE);
		lblNewLabel_9.setBounds(88, 257, 35, 15);
		lblNewLabel_9.setText(lang.t("Played"));
		
		Label lblMinutes = new Label(composite_4, SWT.NONE);
		lblMinutes.setBounds(212, 257, 43, 15);
		lblMinutes.setText(lang.t("minutes"));
		
		final Button btnMatchesEditSave = new Button(composite_4, SWT.NONE);
		btnMatchesEditSave.setBounds(150, 310, 36, 25);
		btnMatchesEditSave.setText(lang.t("&Save"));
				
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
					tracker.saveMatchResult(gMode, myheroid, oppheroid, goes, result, starttime, totaltime, true, "");
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillMatchesTable();
				btnMatchesEditSave.setEnabled(true);
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
		btnArenaSave.setText(lang.t("&Save"));
		
		final Button btnArenaDelete = new Button(composite_4, SWT.NONE);
		btnArenaDelete.setEnabled(false);
		btnArenaDelete.setBounds(197, 303, 45, 25);
		btnArenaDelete.setText(lang.t("&Delete"));
		
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
		btnArenaConfirm.setText(lang.t("&Confirm"));
		
		
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
					tracker.saveModifiedArenaResult(id, heroid, wins, losses);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
				btnArenaSave.setEnabled(true);
			}
		});
		
		btnArenaDelete.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int id = (int) lblID.getData("id");
				try {
					btnArenaDelete.setEnabled(false);
					tracker.deleteModifiedArenaResult(id);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
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
		btnArenaSave.setText(lang.t("&Save"));

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
					tracker.saveArenaResult(heroid, wins, losses, time, true);
				} catch (SQLException e) {
					e.printStackTrace();
				}
				
				fillArenaTable();
				btnArenaSave.setEnabled(true);
			}
		});
	}
	
	private void savePreferences(){
		config.save(setting, HearthFilesNameManager.settingFile);
	}
	
	private void saveDecks(){
		config.save(decks, HearthFilesNameManager.decksFile);
	}
	
	private void poppulateDiagnoticsStatus(){
		Date lastSeen = new Date(hearthScanner.getLastseen());
		int[] area = hearthScanner.getLastScanArea();
		int[] subArea = hearthScanner.getLastScanSubArea();
		String last = lastSeen == null || lastSeen.getTime() == 0 ? lang.t("Never") : HearthHelper.getPrettyText(lastSeen); 

		if(lastSeen == null){
			lblLastSeen.setText(lang.t("N|A"));
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
				hearthScanner.setAutoPing(setting.autoPing);
				savePreferences();
			}
		});
		
		btnVisualizeNow.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				hearthScanner.forcePing();
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
				
				hearthScanner.setGameRes(res.width, res.height);
			}
		});
		
		btnAutoDetectGameRes.setSelection(setting.autoRes);
		
		btnAutoDetectGameRes.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.autoRes = btnAutoDetectGameRes.getSelection();
				savePreferences();
				hearthScanner.setAutoGameRes(setting.autoRes);
			}
		});
		
	}
	
	private void populatesOffsetOptions(){
		hearthScanner.setXOffetOverride(setting.xOffset);
		hearthScanner.setYOffetOverride(setting.yOffset);
		
		spXOffset.setSelection(setting.xOffset);
		spYOffset.setSelection(setting.yOffset);
		
		spXOffset.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setting.xOffset = Integer.parseInt(spXOffset.getText());
				hearthScanner.setXOffetOverride(setting.xOffset);
				savePreferences();
			}
		});
		
		spYOffset.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent arg0) {
				setting.yOffset = Integer.parseInt(spYOffset.getText());
				hearthScanner.setYOffetOverride(setting.yOffset);
				savePreferences();
			}
		});
	}
	
	private void populatescannerOptions(){
		btnAlwaysScan.setSelection(setting.alwaysScan);
		btnAlwaysScan.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.alwaysScan = btnAlwaysScan.getSelection();
				savePreferences();
				hearthScanner.setAlwaysScan(setting.alwaysScan);
			}
		});
		
		
		btnEnableScanner.setSelection(setting.scannerEnabled);
		btnEnableScanner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				setting.scannerEnabled = btnEnableScanner.getSelection();
				savePreferences();
				
				if(setting.scannerEnabled){
					hearthScanner.resume();
				} else {
					hearthScanner.pause();
				}
			}
		});
		
		switch(setting.scanInterval){
			case 100:
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
				btnScanSpeed[0].setSelection(true);
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
						setting.scanInterval = 100;
					}
					
					hearthScanner.setInterval(setting.scanInterval);
					savePreferences();
				}
			});
		}
		
	}
	
	private void poppulateGeneralPerferences(){
		cbUILangs.removeAll();
		
		int index = 0;
		Iterator<Entry<String, String>> it = uiLangsList.map.entrySet().iterator();

	    while (it.hasNext()) {
	        Map.Entry <String, String> pairs = it.next();
	        
	        cbUILangs.add(pairs.getKey());
	        cbUILangs.setData(pairs.getKey(), pairs.getValue());
			
			if(pairs.getValue().equals(setting.uiLang)){
				cbUILangs.select(index);
			}
			
	        ++index;
	    }
	    
	    cbUILangs.addSelectionListener(new SelectionAdapter() {
			
			int previousSelection = -1;
			
			private void selected(SelectionEvent e){
				int i = cbUILangs.getSelectionIndex();
				
				if(i != -1){
					String uiLangFile = (String) cbUILangs.getData(cbUILangs.getItem(i));
					System.out.println("preferences ui lang selected: " + uiLangFile);
					setting.uiLang = uiLangFile;
					
					if(previousSelection != i){
						lang.loadLang(setting.uiLang);
						//restart = true;
						previousSelection = i; 
						savePreferences();
						setRetart();
						shutdown();
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
    						new Image( display, HearthFilesNameManager.logo32),
    						shlHearthtracker.getMonitor()
    				);
	 				
	 				hsync.setKey(key);
	 				hsync.saveKey();

				}else if(!sameKey && !keyValid){
					NotifierDialog.notify(
    						"Web Access Key", 
    						"Invalid. Key is not saved!", 
    						new Image( display, HearthFilesNameManager.logo32),
    						shlHearthtracker.getMonitor()
    				);
				}
			}
			@Override
			public void focusGained(FocusEvent arg0) {
				txtWebSyncKey.selectAll();
			}
		});
		
		btnPopup.setSelection(setting.popup);
		hearthScanner.setNotification(setting.popup);
		
		btnPopup.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(btnPopup.getSelection() != setting.popup){
					setting.popup = btnPopup.getSelection();
					hearthScanner.setNotification(setting.popup);
					savePreferences();
				}
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
				tracker.setServer(setting.gameServer);
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
						config.save(setting,HearthFilesNameManager.settingFile);
					}
				}
			}
		});
	}
	
	private void updateStatus(){
		String overview = hearthScanner.getOverview();
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
				return HearthScannerManager.ARENAMODE;
			case 1:
				return HearthScannerManager.RANKEDMODE;
			case 2:
				return HearthScannerManager.UNRANKEDMODE;
			case 3:
				return HearthScannerManager.CHALLENGEMODE;
			case 4:
				return HearthScannerManager.PRACTICEMODE;
		}
		
		return HearthScannerManager.UNKNOWNMODE;
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

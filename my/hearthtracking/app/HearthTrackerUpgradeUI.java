package my.hearthtracking.app;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;

public class HearthTrackerUpgradeUI {
	HearthTracker tracker = new HearthTracker();
	HearthLanguageManager lang = HearthLanguageManager.getInstance();
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		Display display = Display.getDefault();
		final Shell ui = new Shell(display, SWT.SHELL_TRIM & (~SWT.RESIZE) & (~SWT.MAX) & (~SWT.CLOSE));
		ui.setText(
			lang.t("HearthTracker Upgrade Confirmation")
		);
		ui.setSize(388, 200);
		ui.setImage(new Image( display, HearthFilesNameManager.logo128));
		
		Label lblPleaseSelectA = new Label(ui, SWT.WRAP);
		lblPleaseSelectA.setBounds(10, 10, 362, 50);
		lblPleaseSelectA.setText(
			lang.t("Please select the server you played on for your existing arena and matches records. The server field for existing records can not be altered after this.")
		);
		
		final Combo cbServer = new Combo(ui, SWT.READ_ONLY);
		cbServer.setBounds(113, 78, 141, 23);
		
		HearthBnetServersList servers = new HearthBnetServersList();
		
		for(int i = 0; i < servers.getTotal(); i++){
			cbServer.add(servers.getServerLabel(i));
			cbServer.setData(servers.getServerLabel(i), servers.getServerName(i));
		}
		
		if(servers.getTotal() > 0){
			cbServer.select(0);
		}
		
		Button btnNewButton = new Button(ui, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				int i = cbServer.getSelectionIndex();
				
				if( i >= 0 ){
					String server = (String) cbServer.getData(cbServer.getItem(i));
					tracker.setServerForOldRecords(server);
					HearthConfigurator config = new HearthConfigurator();
					HearthSetting setting = (HearthSetting) config.load(HearthFilesNameManager.settingFile);
					
					if(setting == null){
						setting = new HearthSetting();
						config.save(setting, HearthFilesNameManager.settingFile);
					}
					
					setting.gameServer = server;
					
					config.save(setting, HearthFilesNameManager.settingFile);
					ui.close();
				}
			}
		});
		btnNewButton.setBounds(147, 137, 75, 25);
		btnNewButton.setText(lang.t("C&onfirm"));
		
		if(!tracker.isServerSelected()){
			ui.open();
			ui.layout();
			while (!ui.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		}		
	}
}

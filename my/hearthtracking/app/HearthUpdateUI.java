package my.hearthtracking.app;

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;


public class HearthUpdateUI {
	private static HearthLanguageManager uiLang = HearthLanguageManager.getInstance();
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		HearthUpdater updater = new HearthUpdater();
		Display display = Display.getDefault();
		Shell shlHearthtrackerUpdateNotification = new Shell(display, SWT.TITLE | SWT.CLOSE | SWT.BORDER);
		shlHearthtrackerUpdateNotification.setText(
				uiLang.t("HearthTracker Update Notification!")
		);
		shlHearthtrackerUpdateNotification.setSize(388, 228);
		shlHearthtrackerUpdateNotification.setImage(new Image( display, HearthFilesNameManager.logo128 ));
		
		setCenter(display, shlHearthtrackerUpdateNotification);
		
		Link link = new Link(shlHearthtrackerUpdateNotification, SWT.NONE);
		link.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					java.awt.Desktop.getDesktop().browse(new URL("http://hearthtracking.com").toURI());
				} catch (Throwable e) {
					e.printStackTrace();
				} 
			}
		});
		link.setBounds(166, 180, 59, 15);
		link.setText(uiLang.t("%sGet update%s", "<a>", "</a>"));
		
		StyledText styledText = new StyledText(shlHearthtrackerUpdateNotification, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP);
		styledText.setBounds(10, 77, 362, 97);
		
		styledText.setText(updater.getUpdateMessage());
		
		Label lblNewLabel = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 69, 15);
		lblNewLabel.setText(uiLang.t("Your version:"));
		
		Label lblNewLabel_1 = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel_1.setBounds(10, 31, 76, 15);
		lblNewLabel_1.setText(uiLang.t("Latest version:"));
		
		Label lblNewLabel_2 = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel_2.setBounds(10, 52, 69, 15);
		lblNewLabel_2.setText(uiLang.t("Released on:"));
		
		Label lblYourVersion = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblYourVersion.setBounds(95, 10, 277, 15);
		lblYourVersion.setText(MainLoader.version[0] + "." + MainLoader.version[1] + "." + MainLoader.version[2]);
		
		Label lblLatestVersion = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblLatestVersion.setBounds(95, 31, 277, 15);
		lblLatestVersion.setText(updater.getUpdateVersionString());
		
		Label lblReleased = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblReleased.setBounds(95, 52, 277, 15);
		Date released = new Date(updater.getLastUpdated());
		
		Calendar cal = Calendar.getInstance();
		
		cal.setTime(released);
		
		lblReleased.setText(new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(cal.getTime()) + " (" + HearthHelper.getPrettyText(released) + ")" );

		shlHearthtrackerUpdateNotification.open();
		shlHearthtrackerUpdateNotification.layout();
		while (!shlHearthtrackerUpdateNotification.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
	
	public boolean setCenter(Display display, Shell shell){
		Rectangle shellBounds = shell.getBounds();	
		java.awt.Point centerPoint = HearthHelper.getCenter(display, shellBounds);
		
		if(centerPoint!=null){
			shell.setLocation (centerPoint.x, centerPoint.y);
			return true;
		}
		
		return false;
	}
}

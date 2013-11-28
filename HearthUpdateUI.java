import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
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
	
	/**
	 * Open the window.
	 * @wbp.parser.entryPoint
	 */
	public void open() {
		HearthUpdater updater = new HearthUpdater();
		Display display = Display.getDefault();
		Shell shlHearthtrackerUpdateNotification = new Shell(display, SWT.TITLE | SWT.CLOSE | SWT.BORDER);
		shlHearthtrackerUpdateNotification.setText("HearthTracker Update Notification!");
		shlHearthtrackerUpdateNotification.setSize(388, 228);
		
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
		link.setText("<a>Get update</a>");
		
		StyledText styledText = new StyledText(shlHearthtrackerUpdateNotification, SWT.BORDER);
		styledText.setBounds(10, 77, 362, 97);
		
		styledText.setText(updater.getUpdateMessage());
		
		Label lblNewLabel = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel.setBounds(10, 10, 69, 15);
		lblNewLabel.setText("Your version:");
		
		Label lblNewLabel_1 = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel_1.setBounds(10, 31, 76, 15);
		lblNewLabel_1.setText("Latest version:");
		
		Label lblNewLabel_2 = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblNewLabel_2.setBounds(10, 52, 69, 15);
		lblNewLabel_2.setText("Released on:");
		
		Label lblYourVersion = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblYourVersion.setBounds(95, 10, 277, 15);
		lblYourVersion.setText(HearthUI.version[0] + "." + HearthUI.version[1] + "." + HearthUI.version[2]);
		
		Label lblLatestVersion = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblLatestVersion.setBounds(95, 31, 277, 15);
		lblLatestVersion.setText(updater.getUpdateVersionString());
		
		Label lblReleased = new Label(shlHearthtrackerUpdateNotification, SWT.NONE);
		lblReleased.setBounds(95, 52, 277, 15);
		Date released = new Date(updater.getLastUpdated());
		lblReleased.setText( released.toLocaleString() + " (" + HearthHelper.getPrettyText(released) + ")" );

		shlHearthtrackerUpdateNotification.open();
		shlHearthtrackerUpdateNotification.layout();
		while (!shlHearthtrackerUpdateNotification.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
	}
}

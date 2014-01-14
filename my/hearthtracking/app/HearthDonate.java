package my.hearthtracking.app;

import java.text.DecimalFormat;

import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Label;
import org.eclipse.wb.swt.SWTResourceManager;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;

public class HearthDonate extends Dialog {

	protected Object result;
	protected Shell shell;
	private int gamesTracked = 1000;
	private HearthLanguageManager lang = HearthLanguageManager.getInstance();

	/**
	 * Create the dialog.
	 * @param parent
	 * @param style
	 */
	public HearthDonate(Shell parent, int games) {
		super(parent, SWT.NONE);
		setText("SWT Dialog");
		gamesTracked = games;
	}

	/**
	 * Open the dialog.
	 * @return the result
	 */
	public Object open() {
		createContents();
		shell.open();
		shell.layout();
		Display display = getParent().getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		return result;
	}

	/**
	 * Create contents of the dialog.
	 */
	private void createContents() {
		shell = new Shell(getParent(), SWT.PRIMARY_MODAL);
		shell.setSize(450, 327);
		shell.setText(getText());
		String formattedNumber = new DecimalFormat("#,###").format(gamesTracked);
		
		Label lblHearthtrackerHadTracked = new Label(shell, SWT.NONE);
		lblHearthtrackerHadTracked.setBounds(20, 54, 428, 15);
		lblHearthtrackerHadTracked.setText(		
				lang.t("Congrats! HearthTracker had tracked %s games for you.", formattedNumber)
		);
		
		Label lblHearthtrackerHadTracked_1 = new Label(shell, SWT.NONE);
		lblHearthtrackerHadTracked_1.setFont(SWTResourceManager.getFont("Segoe UI", 18, SWT.BOLD));
		lblHearthtrackerHadTracked_1.setBounds(10, 10, 399, 38);
		lblHearthtrackerHadTracked_1.setText( 
				lang.t("%s+ Games Tracked!", formattedNumber)
		);
		
		Label lblNewLabel = new Label(shell, SWT.NONE);
		lblNewLabel.setBounds(20, 75, 418, 15);
		lblNewLabel.setText(
				lang.t("I am sincerely hope that you like it! I've spent countless hours to develope it. ")
		);
		
		Label lblIfYouThink = new Label(shell, SWT.WRAP);
		lblIfYouThink.setBounds(20, 110, 418, 46);
		lblIfYouThink.setText(
				lang.t("If you think HearthTracker helped you. Please consider donating a few bucks so that I can get better web servers for the upcoming Web Sync feature and keep the developing on it!")
		);
		
		Label lblNewLabel_1 = new Label(shell, SWT.NONE);
		lblNewLabel_1.setBounds(21, 172, 406, 15);
		lblNewLabel_1.setText(
				lang.t("It really means a lot to me. Your support is greatly appreciated!")
		);
		
		Button btnNewButton = new Button(shell, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				HearthHelper.openDonateLink();
				shell.close();
			}
		});
		btnNewButton.setBounds(131, 212, 163, 45);
		btnNewButton.setText(
				lang.t("Yes, I want to donate!")
		);
		
		Button btnNewButton_1 = new Button(shell, SWT.NONE);
		btnNewButton_1.setBounds(145, 272, 132, 25);
		btnNewButton_1.setText(
				lang.t("No, I refuse to help!")
		);
		
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				try {
					shell.close();
				}catch (Throwable e) {
					//e.printStackTrace();
				}
			}
		});

	}
}

package grant;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.swt.events.*;
import org.eclipse.swt.graphics.*;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.custom.*;
public class FormatterProblem {
	public static void main(String args[]) {
		new FormatterProblem().buildGUI();
	}
	private void buildGUI() {
		Shell shell = new Shell();
		final Display d = shell.getDisplay();
		shell.setBounds(100, 100, 300, 100);
		shell.setLayout(new GridLayout());
		Composite buttonGroup = new Composite(shell, SWT.BORDER);
		buttonGroup.setLayout(new GridLayout());
		buttonGroup.setLayoutData(new GridData());
		final Button addButton = new Button(buttonGroup, SWT.PUSH);
		addButton.setLayoutData(new GridData());
		SelectionAdapter buttonSelection = new SelectionAdapter() {
			//....
		};
		addButton.addSelectionListener(buttonSelection);
		shell.open();
		while (!shell.isDisposed()) {
			if (!d.readAndDispatch()) d.sleep();
		}
	}
}
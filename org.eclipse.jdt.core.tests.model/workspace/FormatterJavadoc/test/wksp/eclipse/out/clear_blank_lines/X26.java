package test.wksp.eclipse;

/**
 * A ControlEditor is a manager for a Control that appears above a composite and
 * tracks with the moving and resizing of that composite. It can be used to
 * display one control above another control. This could be used when editing a
 * control that does not have editing capabilities by using a text editor or for
 * launching a dialog by placing a button above a control.
 * <p>
 * Here is an example of using a ControlEditor: <code><pre>
* Canvas canvas = new Canvas(shell, SWT.BORDER);
* canvas.setBounds(10, 10, 300, 300);	
* Color color = new Color(null, 255, 0, 0);
* canvas.setBackground(color);
* ControlEditor editor = new ControlEditor (canvas);
* // The editor will be a button in the bottom right corner of the canvas.
* // When selected, it will launch a Color dialog that will change the background 
* // of the canvas.
* Button button = new Button(canvas, SWT.PUSH);
* button.setText("Select Color...");
* button.addSelectionListener (new SelectionAdapter() {
* 	public void widgetSelected(SelectionEvent e) {
* 		ColorDialog dialog = new ColorDialog(shell);
* 		dialog.open();
* 		RGB rgb = dialog.getRGB();
* 		if (rgb != null) {
* 			if (color != null) color.dispose();
* 			color = new Color(null, rgb);
* 			canvas.setBackground(color);
* 		}
* 		
* 	}
* });
*
* editor.horizontalAlignment = SWT.RIGHT;
* editor.verticalAlignment = SWT.BOTTOM;
* editor.grabHorizontal = false;
* editor.grabVertical = false;
* Point size = button.computeSize(SWT.DEFAULT, SWT.DEFAULT);
* editor.minimumWidth = size.x;
* editor.minimumHeight = size.y;
* editor.setEditor (button);
* </pre></code>
 */
public class X26 {

}

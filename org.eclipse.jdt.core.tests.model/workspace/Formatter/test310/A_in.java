public class A {

	public void actionPerformed(ActionEvent e) {
		this.getParentTiledContainer()
				.dockPaneAt(Integer.parseInt(e.getActionCommand()), pane,
RTabbedDocumentPane.this,
						0.5)		;
	}
}
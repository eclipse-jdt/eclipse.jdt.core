public class A {
	public void setBorderType(JComponent c, int borderType) {
		myOtherData.setBorderType(borderType);
		if (c != null) {
			switch (borderType)
			{
				case 0 : // none
					c.setBorder(null);
					break;
				case 1 : // line
					c.setBorder(BorderFactory.createLineBorder(Color.black));
					break;
				case 3 : // bevel lowered
					c.setBorder(BorderFactory.createLoweredBevelBorder());
					break;
				case 4 : // bevel raised
					c.setBorder(BorderFactory.createRaisedBevelBorder());
					break;
				case 5 : // etched
					c.setBorder(BorderFactory.createEtchedBorder());
					break;
				default :
			} // end switch
		}
	}
}
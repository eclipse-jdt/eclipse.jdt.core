public class A {
	public void launch() {
		switch (key) {
			case VALUE0 :
				doCase0();
				break;
			case VALUE1 :
				{
					doCase1();
					break;
				}
			case VALUE2 :
				doCase2();
				break;
			default :
				{
					doDefault();
				}
		}
	}
}
package javadoc.testBug54776;
public class Test {
	
	private int field= /*]*/foo()/*[*/;	
	
	public int foo() {
		return 1;
	}
}
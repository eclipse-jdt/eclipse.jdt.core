package test0343;

public class Test {

	public volatile boolean flag;

	public void foo() {
		int i= 5;
		/*]*/if (flag)
			i= 10;/*[*/
		i--;
	}
}
package test0293;

public class Test {
	public void foo() {
		Runnable run= new Runnable() {
			public void run() {
				/*]*/foo();/*[*/
			}
		};
	}
}
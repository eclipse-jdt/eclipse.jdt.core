/* Test case for PR 1GGNOTF: ITPJCORE:WINNT - Search doesn't find method referenced in anonymous inner class */
public class PR_1GGNOTF {
	void method() {
	}	
	void method2() {
		Runnable r = new Runnable() {
			public void run() {
				method();
			}
		};
	}
}
package e9;
/* Test case for bug 49120 search doesn't find references to anonymous inner methods*/
public class A {
	public void bar() {
	}
	public void foo() {
		A a = new A() {
			public void bar() {
			}
		};
		a.bar();
	}
}

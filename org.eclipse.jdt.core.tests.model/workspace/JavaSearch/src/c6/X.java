package c6;
/* Test case for bug 24346 Method declaration not found in field initializer*/
public class X {
	public void bar() {
	}
	public Object x = new Object() {
		public void foo24346() {
		}
	};
}

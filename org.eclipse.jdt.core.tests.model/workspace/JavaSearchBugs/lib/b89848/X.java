package b89848;
public class X {
	String bar() { return ""; }
	public void foo() {
		new Object() {
			public String toString() {
				return bar();
			}
		};
	}
}

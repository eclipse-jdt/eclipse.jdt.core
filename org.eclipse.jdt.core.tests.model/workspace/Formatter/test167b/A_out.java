public class X {
	X(String s) {
	}
	protected void foo() {
		X a = new X(new StringBuffer("this").append("is").append("a").append(
				"long").append("argument").toString()) {
			public void run() {
			}
		};
	}
}
public class ResolveMethod {
	public void foo(int i) {
	}
	public void foo(String s) {
	}
	public void bar() {
		new ResolveMethod().foo("");
	}
}
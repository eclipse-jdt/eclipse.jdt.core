public class ResolveMethodWithIncorrectParameter {
	public void foo(int x) {
	}
	public void bar() {
		foo("String");
	}
}
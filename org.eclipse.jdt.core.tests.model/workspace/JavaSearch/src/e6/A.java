package e6;
/* Test case for bug 36479 Rename operation during refactoring fails */
public class A {
	Object foo() {
		return (B36479.C)this;
	}
}

package e3;
/* Test case for bug 31985 NPE searching non-qualified and case insensitive type ref */
public class Y31985 {
	Object foo() {
		return X31985.CONSTANT;
	}
}

public class X {
	int foo() {
		return (1);
	}
	Object foo2() {
		return (Object) bar();
	}
	StringBuffer bar() {
		return null;
	}
	String foo3() {
		return "";
	}
	int foo4(int i) {
		return ++i;
	}
}

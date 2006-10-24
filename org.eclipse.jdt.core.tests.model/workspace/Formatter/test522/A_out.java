/**
 * New Lines
 */
public class Empty {
}

class Example {
	static int[] fArray = { 1, 2, 3, 4, 5 };
	Listener fListener = new Listener() {
	};

	// the following line contains line breaks
	// which can be preserved:
	void bar() {
	}

	void foo() {
		;
		;
		do {
		} while (false);
		for (;;) {
		}
	}
}

enum MyEnum {
	UNDEFINED(0) {
	}
}

enum EmptyEnum {
}

public class C {

	public static class Nested {
		class Nested2 {
			Object anonymous = new Object() {
				@Override
				public String toString() {
					return super.toString();
				}

			};

		}

	}

	class Empty {
	}

	class Empty2 {
		// with comment
	}

}

public class C2 {
	int a; // comment
	// comment

}

public class C3 {
	int a;

	// comment
	// comment
	// comment
}

public class C3b {
	int a;
	// comment
	// comment
	// comment

}

abstract class C4 {
	int a; /* comment */
	/* comment */

}

class C5 {
	int a;

	/* comment */
	/* comment */
	/* comment */
}

class C5b {
	int a;
	/* comment */
	/* comment */
	/* comment */

}

enum Enum {
	A, B;



}

enum Enum2 {
	ONE,

	TWO;

	private int value;

	@Override
	public String toString() {
		return super.toString();
	}

}

interface Interface {
	void doSomething();

}

@interface Annotation {
	String value;

}

package test0069;

class Outer<A> {
	class Inner<B> {
	}
}

public class X {
	void foo() {
		test0069.Outer<String>.Inner<Integer> in = new Outer<String>().new Inner<Integer>();
	}
}
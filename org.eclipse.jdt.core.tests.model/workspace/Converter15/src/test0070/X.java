package test0070;

class Outer<A> {
		class Inner<C> {
		}
}

public class X {
	void foo() {
		Outer<String>.Inner<Number> in = new Outer<String>().new Inner<Number>();
	}
}
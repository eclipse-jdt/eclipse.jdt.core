package test0070;

class Outer<A> {
	class Inner<B> {
		class InnerInner<C> {
		}
	}
}

public class X {
	void foo() {
		Outer<String>.Inner<Integer>.InnerInner<Number> in = new Outer<String>().new Inner<Integer>().new InnerInner<Number>();
	}
}
package test0045;

class B {
	<Z> void foo(Z z) {
		System.out.println(z);
	}
}

public class X {
	public static void main(String[] args) {
		B b = new B();
		b.<String>foo("SUCCESS");
	}
}
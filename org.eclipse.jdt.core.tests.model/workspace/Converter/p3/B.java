package p3;

import p1.*;

public class B {

	public static void bar() {
		p2.A.foo();
	}
	public static void main(String[] args) {
		A.foo();
	}
}

package c10;
/* Test case for bug 43276 How to search for implicit constructors */
class X {
	class Inner {
	}
}
class B extends X.Inner {
	B() {
		new X().super();
	}
}

package t;

import a.A;
import a.B;

public class T {
	A a = new A();
	void test() {
		B b = new B();
		b.foo(); // available only in 25
	}
}

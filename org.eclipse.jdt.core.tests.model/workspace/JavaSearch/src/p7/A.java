package p7;
/* Test case for 1G52F7P: ITPJCORE:WINNT - Search - finds bogus references to class */
public class A {
}

class C {
	void m() {
		class A {
		}
		new A();
	}
}
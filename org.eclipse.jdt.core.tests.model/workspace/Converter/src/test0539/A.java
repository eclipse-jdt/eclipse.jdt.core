public class A {
	class Inner {
	}
	void make() {
		new A(){}.new Inner(){/*x*/};
	}
}
package b70827;
class A {
	private void privateMethod() {
	}
}

class Second extends A {
	void call() {
		int i= privateMethod();
	}
	int privateMethod() {
		return 1;
	}
}

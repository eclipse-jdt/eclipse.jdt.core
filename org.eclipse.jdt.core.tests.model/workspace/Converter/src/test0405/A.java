package test0405;

class A {
	public void foo() {
	}
}

class Test {
	void test() throws CloneNotSupportedException {
		A a = new A();
		a.clone();
	}
}
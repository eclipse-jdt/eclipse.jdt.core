package test0403;

class Test {
	public void foo(){};
}

public class A {
	void test1() throws CloneNotSupportedException {
		Test test = new Test();
		test.clone();
	}
}
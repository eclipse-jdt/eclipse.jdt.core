package test0430;

public class A {
	private A() {
		this(coo2());
	}
	
	private A(int i) {
	}

	private int coo2() {
		return 7;
	}
}
package test0468;

public class A {

	int i;
	
	public int foo() {
		return this.i;
	}

	public void bar() {
		foo();
	}
}
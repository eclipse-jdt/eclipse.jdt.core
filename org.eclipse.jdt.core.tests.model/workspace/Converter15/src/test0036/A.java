package test0030;

public class A<X> {

	public A() {
		this.<X>foo();
	}
	
	public <X> void foo() {
	}
}
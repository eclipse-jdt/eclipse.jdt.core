package test0036;

public class A<X> {

	public A() {
		this.<X>foo();
	}
	
	public <X> void foo() {
	}
}
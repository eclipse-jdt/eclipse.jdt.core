package test0036;

public class X<T> {

	public X() {
		this.<T>foo();
	}
	
	public <T> void foo() {
	}
}
package test0030;

public class X<T> {

	public X(int i) {
		<T>this();
	}
	
	public <T> X() {
	}
}
package test0088;

public class X {
	public <E> void foo(E param) {
		E local= param;
		foo(local);
	}
}
package p2;
public class Y {
	void bar() {
		X<Object> x = new X<Object>();
		x.foo(this);
	}
	Object foo() {
		return new X<Object>(this);
	}
}
package p2;
public class Y {
	void bar() {
		X<Object> x = new X<Object>();
		x.foo(this);
	}
}
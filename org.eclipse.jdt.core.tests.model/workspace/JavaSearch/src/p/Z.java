package p;
public class Z extends Y {
	public Z() {
	}
	public Z(int i) {
		super(i);
	}
	public void foo(int i, String s, X x) {
		super.foo(i, s, new Y(true));
	}

}

package bug132665;
public class Bug132665<T> {
	public void foo1() {}
	public Bug132665<? extends java.lang.Object> foo2() {
		return null;
	}
	public void foo3() {}
}
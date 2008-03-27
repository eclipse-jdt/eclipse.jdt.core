package bug223878;
public class Bug223878<T> {
	public void foo1() {}
	public void foo2(Bug223878<? extends java.lang.Object> arg) {
		
	}
	public void foo3() {}
}
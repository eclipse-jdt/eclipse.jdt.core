package test0275;
public class Test {
	public void foo(int j) {
		int i = 0;
		while (i < 10) foo(i++);
	}
}
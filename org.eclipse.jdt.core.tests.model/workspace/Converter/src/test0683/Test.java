package test0683;
public class Test {
	public void bar(String[][][] a) {}
	public void foo(int[] b) {
		bar(new String[0][b[10]][]);
	}
}
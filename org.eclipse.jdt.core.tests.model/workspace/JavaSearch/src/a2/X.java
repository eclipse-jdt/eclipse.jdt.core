package a2;
/* Test case for bug 5923 Search for "length" field refs finds [].length  */
public class X {
	int length = 1;
	public void foo() {
		int[] array = new int[length];
		for (int i = 0, length = array.length; i < length; i++) {
		}
	}
}


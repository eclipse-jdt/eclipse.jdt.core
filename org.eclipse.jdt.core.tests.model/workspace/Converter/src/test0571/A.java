package test0571;

public class A {
	java.util.zip.ZipFile[] zipFile[];
	
	{
		zipFile = null;
	}
	
	int i;

	A(final int i) {
		this.i = i;
	}
	
	public static int[] foo(String s, final int i)[] {
		bar(String.class);
	}
	
	Object bar(Class c) {
		return new Object() {
			public String toString() {
				return A.super.toString() + "null";
			}
		};
	}
}
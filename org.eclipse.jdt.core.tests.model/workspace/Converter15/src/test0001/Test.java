package test0001;

public class Test {
	
	public static final String HELLO_WORLD = "Hello" + " world";

	private static String bar(final String s) {
		return s;
	}
	public static void main(String[] args) {
		System.out.println(bar(HELLO_WORLD));
	}
}
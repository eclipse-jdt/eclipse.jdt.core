public class AmbiguousSyntaxDemo {
	public static void main(String[] args) {
		int i = 2;
		int a = 3;
		int b = a + -(-i);
		int c = a + - -i;
		int d = a + --i;
		System.out.printf("a=%d b=%d c=%d d=%d i=%d%n", a, b, c, d, i);
	}
}

package test0545;

public class Second {
	private static class Test {
		Test(int i) {
		}
	}
	Second() {
		final Test t = new Test(0);
	}
}
package test0545;

public class First {
	private static class Test {
		// default constructor
		Test(float f) {
		}
	}
	First() {
		final Test t = new Test(0.0f);
	}
}
package test0308;

import java.net.MalformedURLException;

public class Test {
	static class A {
		public A(int i) throws MalformedURLException {
		}
	}

	static class B extends A {
		public B() {
			super(10);
		}
	}
}
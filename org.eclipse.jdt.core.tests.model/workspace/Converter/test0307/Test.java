package test0307;

import java.net.MalformedURLException;

public class Test {
	static class A {
		public A(int i) throws MalformedURLException {
		}
	}

	static class B extends A {
		public B()  throws MalformedURLException {
			super(10);
		}
	}
}
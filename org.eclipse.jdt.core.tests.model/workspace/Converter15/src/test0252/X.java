package test0252;

import java.util.Map;

class A {
	public <K,V> Map<K,V> foo() {
		return null;
	}
}
public class X extends A {
	void caller() {
		Map<String, String> explicitEmptyMap = super.<String, String> foo();
		method(explicitEmptyMap);
		Map<String, String> emptyMap = super.foo();
		method(emptyMap);
	}

	void method(Map<String, String> map) {
	}
}

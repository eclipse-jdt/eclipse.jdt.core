package xyz;

public class MultiNestedType {
	class InnerClass1 {
		class InnerClass2 {
			class InnerClass3 {
				class InnerClass4 {}
			}
		}
	}
	interface InnerInterface {
		interface InnerInterface1 {}
	}
}
public class A {
	protected void anotherMethod() {
		methodA(null);
	}
	private Object methodA(ClassB.InnerInterface arg3) {
		return null;
	}
}
class ClassB implements InterfaceB {
}
interface InterfaceB {
	interface InnerInterface {
	}
}

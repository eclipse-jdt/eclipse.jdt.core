interface A {
	int hashCode();
}
interface B extends A {
}
class C implements B {
	public int hashCode() {
		return super.hashCode();
	}
}
public class CompletionObjectsMethodWithInterfaceReceiver {
	public static void main(String[] arguments) {
		C c = new C();
		System.out.println("((A)c).hashCode() : " + ((A) c).hash
	}
}
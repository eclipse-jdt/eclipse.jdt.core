interface A1 {
	class Inner1 {
	}
}
interface B1 extends A1 {
	class Inner1 {
	}
}
public class CompletionFindMemberType1 {
	public void foo() {
		B1.Inner
	}
}
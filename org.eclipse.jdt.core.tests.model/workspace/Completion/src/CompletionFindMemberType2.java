interface A2 {
	class Inner2{
	}
}
interface B2 extends A2 {
	class Inner2 {
	}
}
public class CompletionFindMemberType2 implements B2{
	public void foo() {
		Inner
	}
}
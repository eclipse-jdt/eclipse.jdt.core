interface A3 {
	public static final String bar = "a";
}
interface B3 extends A3 {
	public static final String bar = "b";
}
public class CompletionFindField3 {
	public void foo() {
		B3 b = null;
		System.out.println(b.ba);
	}
}


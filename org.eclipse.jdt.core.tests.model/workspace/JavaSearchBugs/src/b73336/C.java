package b73336;
public class C implements I<A> {
	public void foo() {
		B b = new B();
		b.<A>foo(new A());
	}
}

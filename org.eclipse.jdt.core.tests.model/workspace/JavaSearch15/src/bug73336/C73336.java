package bug73336;
public class C73336 implements I73336<A73336> {
	public void foo() {
		B73336 b = new B73336();
		b.<A73336>foo(new A73336());
	}
}

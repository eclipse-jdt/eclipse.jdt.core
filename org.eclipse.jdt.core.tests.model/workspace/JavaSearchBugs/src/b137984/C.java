package b137984;
public class C {
	C2 c2;
	C2.C3 c3;
	C(int c) {
		c2 = new C2(c);
		c3 = c2.new C3(c);
	}
	class C2 {
		C2(int x) {}
		class C3 {
			C3(int x) {}
		}
	}
}

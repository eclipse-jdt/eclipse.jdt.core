package f2;
public class X {
	Object foo1() {
		class Y {
		}
		return new Y();
	}
	Object foo2() {
		return new X() {
		};
	}
}
package p9;
public class X {
	void foo() {
		new X() {
		};
		class Y extends X {
		}
	}
}
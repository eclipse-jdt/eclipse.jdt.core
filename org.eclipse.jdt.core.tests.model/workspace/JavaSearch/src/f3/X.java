package f3;
public class X {
	void foo() {
		new X() {
		};
		new X() {
			void foobar() {
				bar();
			}
		};
		if (true) {
			class Y {
			}
		} else {
			class Y {
				void foobar() {
					bar();
				}
			}
		}
	}
	void bar() {
	}
}
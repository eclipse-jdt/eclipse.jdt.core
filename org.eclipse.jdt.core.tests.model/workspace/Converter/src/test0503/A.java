package test0503;

public class A {
	public class B {
		void bar() {
			class C {
			}
			new Object() {
				class D {
				}
			};
		}
	}
	
	void foo() {
		class E {
		}
		new Object() {
			class F {
			}
		};
		if (false) {
			class G {
			}
		}
	}
}

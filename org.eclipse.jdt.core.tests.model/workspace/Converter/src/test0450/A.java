package test0450;

public final class A {
	
	void outerMethod() {
		
		new Object() {
			
			class Subroutine {
				class B {
				}
			}
			
			private void innerMethod() {
				class B {
				}
				Subroutine sub = null;
			}
			
		};
	}
}

public class ResolveLocalVariable {
	void foo1() {
		
	}
	void foo2() {
		new Object() {
			
		};
		new Object() {
			void bar() {
			}
			void toto() {
				Object var;
				var = null;
			}
		};
	}
}
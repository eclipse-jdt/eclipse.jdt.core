public class ResolveLocalMethodDeclaration2 {
	ResolveLocalMethodDeclaration2(Object arg) {
	}
	void foo() {
		new ResolveLocalMethodDeclaration2("") {
			void bar() {
				new Object() {
					void selectMe() {
					}
				};
			}
		};
	}
}

public class ResolveMethodDeclarationInAnonymous3 {
	ResolveMethodDeclarationInAnonymous3(Object arg) {
	}
	void foo() {
		new ResolveMethodDeclarationInAnonymous3("") {
			void bar() {
				new Object() {
					void selectMe() {
					}
				};
			}
		};
	}
}

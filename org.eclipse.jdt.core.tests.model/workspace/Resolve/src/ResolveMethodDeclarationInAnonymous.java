public class ResolveMethodDeclarationInAnonymous {
	void foo() {
		Object o = new Object() {
			void bar() {
			}
		};
	}
}
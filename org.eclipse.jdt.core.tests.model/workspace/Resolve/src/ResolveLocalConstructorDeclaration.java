public class ResolveLocalConstructorDeclaration {
	void foo() {
		class Y {
			public Y(int i) {
			}
			public Y(String s) {
			}
		}
	}
}
package test0684;

class X {
	Object m() {
		return new Cloneable() {
			class MemberOfLocal {
			}
		};
	}
}
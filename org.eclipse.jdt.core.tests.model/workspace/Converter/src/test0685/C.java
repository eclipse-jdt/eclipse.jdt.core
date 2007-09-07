package test0685;

class C {
	void m() {
		new Cloneable() {
			class MemberOfLocal {
			}
		};
	}
}
package MemberTypeReference;
public class B {
	protected class BMember{}
	
	void foo() {
		Azz.fHello= 1;
		Object someVar = null;
		Object o = (Azz.AzzMember.BMember) someVar;
	}
	void poo() {}
}

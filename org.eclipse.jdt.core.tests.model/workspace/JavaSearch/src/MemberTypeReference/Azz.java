package MemberTypeReference;
public class Azz extends B {
	class AzzMember extends BMember {
		class BMember{
		}
	}
	static int fHello;
	void poo() {
		B.BMember someVar;
		super.poo();
		fHello= 9;
	}
}  
	
class BMember extends B {}
	
class X {
	Azz.AzzMember.BMember val;
}
package p4;
/* Regression test for 1GLBP65: ITPJCORE:WIN2000 - search: type refs - incorrect match */
public class A {
	static A A;
}
class X extends p4.A{
	void x(){
		p4.A.A= A.A;  /*1*/
	}
}
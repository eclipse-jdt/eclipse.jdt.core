package p6;
/* Test case for PR 1GKB9YH: ITPJCORE:WIN2000 - search for field refs - incorrect results */
public class A {
	protected int f;
	void m() {
		f++;
	}
}
class AA extends A {
	protected int f;
}
class B {
	A a;
	AA b;
	A ab = new AA();
	void m() {
		a.f = 0; /*1*/
		b.f = 0; /*2*/
		ab.f = 0; /*3*/
	}
}
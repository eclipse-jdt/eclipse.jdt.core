package q7;
/* Test case for bug 5070 search: missing interface method reference */
interface I {
void k();
}
class C1 implements I{
	public void k(){};
}
class D{
	void h(){
		I a= new C1();
		a.k();
	}
}

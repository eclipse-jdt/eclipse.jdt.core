package q5;
/* Test case for bug 5068 search: missing method reference */
interface I{
void k();
}
class T{
	void m(){
		class X implements I{
			public void k(){}
		};
		X x= new X();
		x.k(); /**/
	}
}

package p8;
/* Test case for bug #3433 search: missing field occurrecnces (1GKZ8J6)  */
public class A{
	protected int g;
	void m(){
		g++;
	}
}
class B extends A{
	void m(){
		g= 0;
	}
}

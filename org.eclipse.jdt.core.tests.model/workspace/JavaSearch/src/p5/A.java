package p5;
/* Regression test for 1GD79XM: ITPJCORE:WINNT - Search - search for field references - not all found */
public class A{
	int f;
	A x;
	void k(){
		x.x.x.f= 0;
	}
}
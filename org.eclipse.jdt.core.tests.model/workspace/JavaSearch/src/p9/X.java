package p9;
/* Test case for PR 1GK8TXE: ITPJCORE:WIN2000 - search: missing field reference */
public class X {
	public X f;
	public int k;
	void m() {
		for (int g = 0; g < 10; g++) {
		}
		f.k = 0; //<<
	}
}
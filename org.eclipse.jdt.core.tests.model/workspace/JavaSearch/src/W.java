/* Regression test for 1GL9UMH: ITPJCORE:WIN2000 - search: missing type occurrences */
public class W {
	static int length = 17;
	int m() {
		int[] R = new int[1];
		return W.length; /*1*/
	}
}
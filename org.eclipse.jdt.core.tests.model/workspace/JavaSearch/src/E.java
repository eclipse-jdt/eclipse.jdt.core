/* Test case for 1GHDA2V: ITPJCORE:WINNT - ClassCastException when doing a search */
public class E {
	public Object foo() {
		int[] result = new int[0];
		return result.clone();
	}
}
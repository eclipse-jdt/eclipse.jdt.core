/* Test case for 1GKZ8VZ: ITPJCORE:WINNT - Search - did not find references to member constructor */
public class A {
	public class Inner {
		public Inner(int i) {
		}
	}
	Inner[] field = new Inner[] {new Inner(1), new Inner(2)};
}

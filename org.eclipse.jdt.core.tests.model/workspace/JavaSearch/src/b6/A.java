package b6;
/* Test case for bug 10386 NPE in MatchLocator.lookupType */
public class A {
	int[] field;
	int foo() {
		return this.field.length;
	}
}
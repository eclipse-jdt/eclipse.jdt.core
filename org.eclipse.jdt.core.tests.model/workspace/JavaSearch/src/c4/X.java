package c4;
/* Test case for bug 20693 Finding references to variables does not find all occurances */
public class X {
	int x;
	int foo() {
		return (this.x);
	}
}
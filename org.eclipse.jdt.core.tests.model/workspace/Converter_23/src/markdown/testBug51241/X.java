package javadoc.testBug51241;
public class X {
	// First class line comment
	int i;
	// C1
	
	void foo() {
		int x;
		// C2
		int y;
		// First method line comment
	}
	// Syntax error here!
	int z
	
	// Second class line comment
	void bar() {
		// Second method line comment
	}
}

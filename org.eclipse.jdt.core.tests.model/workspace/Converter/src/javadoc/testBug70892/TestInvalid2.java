package javadoc.testBug70892;
/**
 * {@value "invalid}
 * {@value <a href}
 * {@value <a href="invalid">invalid</a} invalid
 * {@value #xxx Unknown}
 * {@value #xxx() Unknown}
 */
public class TestInvalid2 {
	int field;
	void foo() {}
}

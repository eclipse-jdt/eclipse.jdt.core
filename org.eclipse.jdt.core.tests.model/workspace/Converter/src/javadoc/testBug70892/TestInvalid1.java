package javadoc.testBug70892;
/**
 * {@value "invalid"}
 * {@value <a href="invalid">invalid</a>} invalid
 * {@value #field}
 * {@value #foo}
 * {@value #foo()}
 */
public class TestInvalid1 {
	int field;
	void foo() {}
}

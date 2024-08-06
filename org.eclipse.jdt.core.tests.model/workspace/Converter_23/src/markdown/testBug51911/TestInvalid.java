package javadoc.testBug51911;
/**
 * @see #foo
 */
public class TestInvalid {
	public void foo(int i, float f) {}
	public void foo(String str) {}
}

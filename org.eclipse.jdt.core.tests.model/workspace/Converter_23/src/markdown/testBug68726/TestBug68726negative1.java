package javadoc.testBug68726;
public class TestBug68726negative1 {
	/**
	 * Invalid URL link references
	 *
	 * @see <a href="invalid" target
	 * @see <a href="invalid" target=
	 * @see <a href="invalid" target="
	 * @see <a href="invalid" target="_top
	 * @see <a href="invalid" target="_top"
	 * @see <a href="invalid" target="_top">
	 * @see <a href="invalid" target="_top">
	 * @see <a href="invalid" target="_top">invalid
	 * @see <a href="invalid" target="_top">invalid<
	 * @see <a href="invalid" target="_top">invalid</
	 * @see <a href="invalid" target="_top">invalid</a
	 * @see <a href="invalid" target="_top">invalid</a> no text allowed after the href
	 */
	public void s_foo() {
	}
}

/**
 * Need this header comment to have inexact matches occur.
 */
package j5;
public class Bug49994 {
	int field;
	public Bug49994(String str) {}
	void bar() {}
	/**
	 * @see #field
	 * @see #bar()
	 * @see #Bug49994(String)
	 */
	void foo() {}
}

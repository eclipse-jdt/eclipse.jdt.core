package javadoc.testBug69272;
public class TestMethInvalid {
	/**@see Object#wait()* */
	public void foo1() {}
	/**@see Object#wait()*** ***/
	public void foo2() {}
	/**@see Object#wait()***
	 */
	public void foo3() {}
}

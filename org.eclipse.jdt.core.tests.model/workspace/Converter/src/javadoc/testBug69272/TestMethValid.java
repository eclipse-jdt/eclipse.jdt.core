package javadoc.testBug69272;
public class TestMethValid {
	/**@see Object#wait()*/
	public void foo1() {}
	/**@see Object#wait()
	*/
	public void foo2() {}
	/**@see Object#wait()    */
	public void foo3() {}
	/**@see Object#wait()****/
	public void foo4() {}
	/**@see Object#wait()		****/
	public void foo5() {}
}

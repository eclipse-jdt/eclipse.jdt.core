package javadoc.testBug68726;
public class TestBug68726conform1 {
	/**
	 *	@see Object <a href="http://www.eclipse.org" target="_top">Eclipse</a>
	 */
	void foo1() {}
	/**@see Object <a href="http://www.eclipse.org" target="_top" target1="_top1" target2="_top2">Eclipse</a>*/
	void foo2() {}
}

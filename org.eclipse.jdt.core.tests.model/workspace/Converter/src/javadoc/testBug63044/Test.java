package javadoc.testBug63044;
/**
 * @see #Test()
 * @see Test#Test()
 * @see javadoc.testBug63044.Test#Test()
 */
public class Test{ 
	Test( ){
		new Test();
	};
}

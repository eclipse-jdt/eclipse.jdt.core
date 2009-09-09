package p288698;

/**
 * create type hierarchy for class @see AbstractBugTest
 * You will get @see java.lang.reflect.InvocationTargetException where Root exception is:
 * 
 * @see java.lang.StringIndexOutOfBoundsException: String index out of range: -27; method substring (), called by @see
 *      org.eclipse.jdt.internal.core.hierarchy.IndexBasedHierarchyBuilder.java : 475
 * 
 * @author Ivan
 */
public class BugTest2Buggy {
	public void testIt () {
		new AbstractBugTest() {};
	}
}


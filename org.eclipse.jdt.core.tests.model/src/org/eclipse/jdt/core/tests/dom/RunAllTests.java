package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunAllTests extends TestCase {
public RunAllTests(String name) {
	super(name);
}
public static Class[] getAllTestClasses() {
	return new Class[] {
		org.eclipse.jdt.core.tests.dom.ASTConverterTest.class,
		org.eclipse.jdt.core.tests.dom.ASTTest.class,
		org.eclipse.jdt.core.tests.dom.ASTVisitorTest.class,
		org.eclipse.jdt.core.tests.dom.ASTMatcherTest.class
	};
}
public static Test suite() {
	TestSuite ts = new TestSuite(RunAllTests.class.getName());

	Class[] testClasses = getAllTestClasses();
	for (int i = 0; i < testClasses.length; i++) {
		Class testClass = testClasses[i];

		// call the suite() method and add the resulting suite to the suite
		try {
			Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]);
			Test suite = (Test)suiteMethod.invoke(null, new Object[0]);
			ts.addTest(suite);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.getTargetException().printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		}
	}
	return ts;
}
}

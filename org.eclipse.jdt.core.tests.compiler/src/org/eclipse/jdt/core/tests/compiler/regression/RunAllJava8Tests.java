package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.compiler.parser.ComplianceDiagnoseTest;
import org.eclipse.jdt.core.tests.compiler.parser.LambdaExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.ReferenceExpressionSyntaxTest;
import org.eclipse.jdt.core.tests.compiler.parser.TypeAnnotationSyntaxTest;

public class RunAllJava8Tests extends TestCase {
	
	public RunAllJava8Tests(String name) {
		super(name);
	}
	public static Class[] getAllTestClasses() {
		return new Class[] {
			LambdaExpressionSyntaxTest.class,
			NegativeLambdaExpressionsTest.class,
			NegativeTypeAnnotationTest.class,
			TypeAnnotationSyntaxTest.class,
			ReferenceExpressionSyntaxTest.class,
			DefaultMethodsTest.class,
			ComplianceDiagnoseTest.class,
		};
	}
	public static Test suite() {
		TestSuite ts = new TestSuite(RunAllJava8Tests.class.getName());

		Class[] testClasses = getAllTestClasses();
		for (int i = 0; i < testClasses.length; i++) {
			Class testClass = testClasses[i];
			// call the suite() method and add the resulting suite to the suite
			try {
				Method suiteMethod = testClass.getDeclaredMethod("suite", new Class[0]); //$NON-NLS-1$
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

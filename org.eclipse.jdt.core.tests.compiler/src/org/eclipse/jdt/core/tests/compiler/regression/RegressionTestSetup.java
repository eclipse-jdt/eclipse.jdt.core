package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.core.tests.compiler.regression.*;
import org.eclipse.jdt.core.tests.util.*;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import java.util.Enumeration;

import junit.extensions.*;
import junit.framework.*;

public class RegressionTestSetup extends TestDecorator {
	TestVerifier verifier = new TestVerifier(true);
public RegressionTestSetup(Test test) {
	super(test);
}
private void initTest(Object test, TestVerifier verifier, INameEnvironment javaClassLib) {
	if (test instanceof AbstractRegressionTest) {
		AbstractRegressionTest regressionTest = (AbstractRegressionTest)test;
		regressionTest.verifier = verifier;
		regressionTest.javaClassLib = javaClassLib;
		return;
	}
	if (test instanceof TestSuite) {
		TestSuite regressionTestClassSuite = (TestSuite) test;
		Enumeration regressionTestClassTests = regressionTestClassSuite.tests();
		while (regressionTestClassTests.hasMoreElements()) {
			initTest(regressionTestClassTests.nextElement(), verifier, javaClassLib);
		}
		return;
	}
		
}
public void run(TestResult result) {
	try {
		setUp();
		super.run(result);
	} finally {
		tearDown();
	}
}
protected void setUp() {
	// Create name environment
	INameEnvironment javaClassLib = new FileSystem(new String[] {Util.getJavaClassLib()}, new String[0], null);

	// Init wrapped suite
	initTest(fTest, this.verifier, javaClassLib);
}
protected void tearDown() {
	this.verifier.shutDown();
}
}

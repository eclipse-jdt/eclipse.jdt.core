package org.eclipse.jdt.compiler.tool.tests;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(CompilerToolTests.class)
public class AllTests extends TestCase {
	// run all tests
	public static Test suite() {
		return new JUnit4TestAdapter(AllTests.class);
	}
}

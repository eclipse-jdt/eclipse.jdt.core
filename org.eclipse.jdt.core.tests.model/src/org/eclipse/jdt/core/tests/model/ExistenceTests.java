package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.tests.model.*;

public class ExistenceTests extends ModifyingResourceTests {
public ExistenceTests(String name) {
	super(name);
}



public static Test suite() {
	TestSuite suite = new Suite(ExistenceTests.class.getName());
	
	suite.addTest(new ExistenceTests("testClassFileInSource"));
	suite.addTest(new ExistenceTests("testClassFileInLibrary"));
	suite.addTest(new ExistenceTests("testClassFileInBinary"));
	
	return suite;
}

public void testClassFileInBinary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/bin/X.class", "");
		IClassFile classFile = this.getClassFile("P/bin/X.class");
		assertTrue(!classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
public void testClassFileInLibrary() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {}, new String[] {"lib"}, "bin");
		this.createFile("P/lib/X.class", "");
		IClassFile classFile = this.getClassFile("P/lib/X.class");
		assertTrue(classFile.exists());
	} finally {
		this.deleteProject("P");
	}
}
public void testClassFileInSource() throws CoreException {
	try {
		this.createJavaProject("P", new String[] {"src"}, "bin");
		this.createFile("P/src/X.class", "");
		IClassFile classFile = this.getClassFile("P/src/X.class");
		// for now, we don't check the kind (source or library), 
		// so class file can exist in source folder
		assertTrue(classFile.exists()); 
	} finally {
		this.deleteProject("P");
	}
}
}
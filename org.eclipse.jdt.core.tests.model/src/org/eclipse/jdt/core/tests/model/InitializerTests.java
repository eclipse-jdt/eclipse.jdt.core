/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

import junit.framework.Test;

/*
 * Test variable initializers and container initializers.
 */
public class InitializerTests extends ModifyingResourceTests {
	
public class DefaultVariableInitializer implements VariablesInitializer.ITestInitializer {
	Map variableValues;
	
	/*
	 * values is [<var name>, <var value>]*
	 */
	public DefaultVariableInitializer(String[] values) throws JavaModelException {
		variableValues = new HashMap();
		for (int i = 0; i < values.length; i+=2) {
			variableValues.put(values[i], new Path(values[i+1]));
		}
	}
	
	public void initialize(String variable) throws JavaModelException {
		if (variableValues == null) return;
		JavaCore.setClasspathVariable(
			variable, 
			(IPath)variableValues.get(variable), 
			null);
	}

}
	
public InitializerTests(String name) {
	super(name);
}
public static Test suite() {
	return new Suite(InitializerTests.class);
}
public void testVariableInitializer1() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {"TEST_LIB", "/P1/lib.jar"}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(this.getFile("/P1/lib.jar"));
		assertTrue("/P1/lib.jar should exist", root.exists());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer2() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		this.createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		IPackageFragmentRoot root = p2.getPackageFragmentRoot(this.getFile("/P1/lib.jar"));
		assertEquals("Unexpected source attachment path", "/P1/src.zip", root.getSourceAttachmentPath().toString());
		assertEquals("Unexpected source attachment root path", "src", root.getSourceAttachmentRootPath().toString());
	} finally {
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer3() throws CoreException {
	try {
		this.createProject("P1");
		this.createFile("/P1/lib.jar", "");
		this.createFile("/P1/src.zip", "");
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		IJavaProject p2 = this.createJavaProject("P2", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");

		// simulate state on startup
		VariablesInitializer.reset();
		JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject)p2);
		p2.close();
		
		startDeltas();
		VariablesInitializer.setInitializer(new DefaultVariableInitializer(new String[] {
			"TEST_LIB", "/P1/lib.jar",
			"TEST_SRC", "/P1/src.zip",
			"TEST_ROOT", "src",
		}));
		p2.getResolvedClasspath(true);
		
		assertDeltas(
			"Unexpected delta on startup", 
			// TODO: Change to "" when startup behavior is fixed
			"P2[*]: {CHILDREN}\n" + 
			"	lib.jar[*]: {ADDED TO CLASSPATH}"
		);
	} finally {
		this.startDeltas();
		this.deleteProject("P1");
		this.deleteProject("P2");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer4() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		this.createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		this.deleteProject("P");
		VariablesInitializer.reset();
	}
}
public void testVariableInitializer5() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				buffer.append("Initializing " + variable + "\n");
				IPath path = new Path(variable.toLowerCase());
				JavaCore.getClasspathVariable("TEST_SRC");
				buffer.append("Setting variable " + variable + " to " + path + "\n");
				JavaCore.setClasspathVariable(variable, path, null);
			}
		});
		this.createJavaProject("P", new String[] {}, new String[] {"TEST_LIB,TEST_SRC,TEST_ROOT"}, "");
		assertEquals(
			"Initializing TEST_LIB\n" +
			"Initializing TEST_SRC\n" +
			"Setting variable TEST_SRC to test_src\n" +
			"Setting variable TEST_LIB to test_lib\n",
			buffer.toString());
	} finally {
		this.deleteProject("P");
		VariablesInitializer.reset();
	}
}
/*
 * Ensures that if the initializer doesn't initialize a variable, it can be
 * initialized later on.
 */
public void testVariableInitializer6() throws CoreException {
	try {
		final StringBuffer buffer = new StringBuffer();
		VariablesInitializer.setInitializer(new VariablesInitializer.ITestInitializer() {
			public void initialize(String variable) throws JavaModelException {
				// do nothing
				buffer.append("Ignoring request to initialize");
			}
		});
		
		// cause variable initializer to be called
		IPath path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after initializer was called",
			null,
			path);
		assertEquals(
			"Unexpected trace of initializer 1",
			"Ignoring request to initialize",
			buffer.toString());
			
		// explicitely set classpath var
		buffer.delete(0, buffer.length());
		IPath varValue = new Path("src.zip");
		JavaCore.setClasspathVariable("TEST_SRC", varValue, null);
		path = JavaCore.getClasspathVariable("TEST_SRC");
		assertEquals(
			"Unexpected value of TEST_SRC after setting it",
			varValue,
			path);
		assertEquals(
			"Unexpected trace of initializer 2",
			"",
			buffer.toString());
	} finally {
		VariablesInitializer.reset();
	}
}
}

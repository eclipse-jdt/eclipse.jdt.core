/*******************************************************************************
 * Copyright (c) 2008 Walter Harley and others
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.InheritedAnnoProc;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Tests covering the IDE's ability to process the correct set of files.
 */
public class BuilderTests extends TestBase
{

	public BuilderTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(BuilderTests.class);
	}
	
	// Need this to avoid JUnit complaining that there are no tests in this suite
	public void testDummy() {
		assertTrue(true);
	}
	
	/**
	 * Verify that a class whose superclass is annotated with an inherited annotation
	 * gets treated the same as if the annotation were present on the class itself.
	 * See <a href="http://bugs.eclipse.org/270754">Bug 270754</a>.
	 */
	public void disabled_testInheritedAnnotation() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		
		env.addClass(root, "", "Base",
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.InheritedTrigger;\n" +
				"@InheritedTrigger(0)\n" +
				"public class Base {}"
		);

		// Because Sub extends Base, it should be treated as if it were annotated with InheritedTrigger
		env.addClass(root, "", "Sub",
				"public class Sub extends Base {\n" +
				"}"
		);
			
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		List<String> elements = InheritedAnnoProc.getProcessedElements();
		assertTrue(elements.contains("Base"));
		assertTrue(elements.contains("Sub"));
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());

		// Modify base class and verify that both base and subclass get processed
		InheritedAnnoProc.clearProcessedElements();
		env.addClass(root, "", "Base",
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.InheritedTrigger;\n" +
				"@InheritedTrigger(1)\n" +
				"public class Base {}"
		);
		incrementalBuild();
		expectingNoProblems();
		elements = InheritedAnnoProc.getProcessedElements();
		assertTrue(elements.contains("Base"));
		assertTrue(elements.contains("Sub"));
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
		
		// Modify subclass and verify that it gets processed
		InheritedAnnoProc.clearProcessedElements();
		env.addClass(root, "", "Sub",
				"public class Sub extends Base {\n" +
				" // this line is new\n" +
				"}"
		);
		incrementalBuild();
		expectingNoProblems();
		elements = InheritedAnnoProc.getProcessedElements();
		assertTrue(elements.contains("Sub"));
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}	
}

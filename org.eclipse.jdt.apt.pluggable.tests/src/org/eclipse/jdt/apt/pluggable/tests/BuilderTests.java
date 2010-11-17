/*******************************************************************************
 * Copyright (c) 2008 - 2010 Walter Harley and others
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
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.TestFinalRoundProc;
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
	
	/**
	 * Verify that a new type generated in the final round does not get
	 * annotations processed, but does get compiled. The JSR269 spec is somewhat
	 * vague about whether it should be possible to generate a new type during
	 * the final round (since the final round does not happen until after a
	 * round in which no new types are generated); but apparently javac behaves
	 * this way.
	 * <p>
	 * See <a href="http://bugs.eclipse.org/329156">Bug 329156</a> and <a
	 * href="http://bugs.sun.com/view_bug.do?bug_id=6634138">the corresponding
	 * bug in javac</a>, which Sun fixed.
	 */
	public void testFinalRound() throws Throwable {
		ProcessorTestStatus.reset();
		TestFinalRoundProc.resetNumRounds();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		
		// The @FinalRoundTestTrigger processor does not generate any files when it
		// first runs; but on its final round it then generates a new Java type
		// that is annotated with @FinalRoundTestTrigger.
		env.addClass(root, "t", "Foo",
				"package t;\n" +
				"import org.eclipse.jdt.apt.pluggable.tests.annotations.FinalRoundTestTrigger;\n" +
				"@FinalRoundTestTrigger\n" +
				"public class Foo {}"
		);
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		
		// Processor should have run total of two rounds; compiled classes
		// should include Foo and FinalRoundGen.
		assertEquals(2, TestFinalRoundProc.getNumRounds());
		expectingUniqueCompiledClasses(new String[] {"t.Foo", "g.FinalRoundGen"});
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
	
	/**
	 * Deleting FooEvent caused the reference to FooEvent in 
	 * FooImpl to appear as a package in one case and as a type in another,
	 * in turn causing a null binding to be passed to APT.
	 *
	 * See <a href="http://bugs.eclipse.org/295948">Bug 295948</a>.
	 */
	public void testBug295948() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		
		env.addClass(root, "test295948", "FooEvent",
				"package test295948;\n" +
				"public class FooEvent {\n" + 
				"    public interface Handler {\n" + 
				"        void handle(FooEvent event);\n" + 
				"    }\n" + 
				"}\n" + 
				"\n"
		);
		
		IPath fooImplClass = env.addClass(root, "test295948", "FooImpl",
				"package test295948;\n" +
				"public class FooImpl implements FooEvent.Handler {\n" + 
				"    @Override\n" + 
				"    public void handle(FooEvent event) {\n" + 
				"    }\n" + 
				"}\n"
		);
		
		AptConfig.setEnabled(jproj, true);
		
		fullBuild();
		expectingNoProblems();
		
		// Delete FooEvent and recompile
		proj.findMember("src/test295948/FooEvent.java").delete(false, null);
		incrementalBuild();
		expectingProblemsFor(fooImplClass,
				"Problem : FooEvent cannot be resolved to a type [ resource : </" + _projectName + "/src/test295948/FooImpl.java> range : <108,116> category : <40> severity : <2>]\n" + 
				"Problem : FooEvent cannot be resolved to a type [ resource : </" + _projectName + "/src/test295948/FooImpl.java> range : <52,60> category : <40> severity : <2>]");
	}	
}

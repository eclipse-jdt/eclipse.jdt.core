/*******************************************************************************
 * Copyright (c) 2008 - 2017 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com 			- initial API and implementation
 *    Harry Terkelsen <het@google.com> 	- Contribution for Bug 437414 - Annotation processing is broken when build is batched
 *    Fabian Steeg <steeg@hbz-nrw.de> - Pass automatically provided options to Java 6 processors - https://bugs.eclipse.org/341298
 *    Pierre-Yves B. <pyvesdev@gmail.com> - Contribution for bug 559618 - No compiler warning for import from same package
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.Bug341298Processor;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.Bug468893Processor;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.Bug510118Processor;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.BugsProc;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.InheritedAnnoProc;
import org.eclipse.jdt.apt.pluggable.tests.processors.buildertester.TestFinalRoundProc;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.tests.builder.Problem;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

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
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=407841
	public void testBug407841() throws Throwable {
		int old = org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE;
		try {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE =1;
			ProcessorTestStatus.reset();
			IJavaProject jproj = createJavaProject(_projectName);
			disableJava5Factories(jproj);
			IProject proj = jproj.getProject();
			IdeTestUtils.copyResources(proj, "targets/bug407841", "src/targets/bug407841");

			AptConfig.setEnabled(jproj, true);
			fullBuild();
			expectingNoProblems();
			assertEquals("Elements should have been processed", 0, BugsProc.getUnprocessedElements());
			assertEquals("Elements should have been processed", 4, BugsProc.getNumRounds());
		} finally {
			org.eclipse.jdt.internal.core.builder.AbstractImageBuilder.MAX_AT_ONCE = old;
		}
	}
	public void testBug468893() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/bug468893", "src/targets/bug468893");

		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		assertEquals("Should have been processed over just once", 1, Bug468893Processor.count());
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=419769
	public void testBug419769() throws Throwable {
		try {
			ProcessorTestStatus.reset();
			IJavaProject jproj = createJavaProject(_projectName);
			disableJava5Factories(jproj);
			IProject proj = jproj.getProject();
			IdeTestUtils.copyResources(proj, "targets/bug419769", "src/targets/bug419769");
			AptConfig.setEnabled(jproj, true);
			fullBuild();
			expectingNoProblems(); // There should be no compiler errors, i.e. except the APT injected ones.
			Problem[] problems = env.getProblemsFor(jproj.getProject().getFullPath(), "org.eclipse.jdt.apt.pluggable.core.compileProblem");
			StringBuilder buf = new StringBuilder();
			for (int i = 0, length = problems.length; i < length; i++) {
				Problem problem = problems[i];
				buf.append(problem.getMessage());
				if (i < length - 1) buf.append('\n');
			}
			assertEquals("There should be two reported problems",
						"Some Error Message.\nYet another Error Message.",
						buf.toString());
		} finally {
		}
	}

	public void testBug387956() throws Exception {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/bug387956", "src/targets/bug387956");

		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
	}
	public void testBatchedBuild() throws Throwable {
		int old = AbstractImageBuilder.MAX_AT_ONCE;
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		IPath packagePath = root.append("test");
		try {
			// Force the build to be batched
			AbstractImageBuilder.MAX_AT_ONCE = 1;
			ProcessorTestStatus.reset();

			env.addClass(root, "test", "Foo",
					"package test;\n" +
					"import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;\n" +
			        "@GenClass6(name = \"FooGen\", pkg = \"test\")\n" +
				    "public class Foo {\n" +
			        "    public Bar bar;\n" +
				    "}");
			env.addClass(root, "test", "Bar",
					"package test;\n" +
					"import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;\n" +
			        "@GenClass6(name = \"BarGen\", pkg = \"test\")\n" +
				    "public class Bar {\n" +
			        "    public Foo foo;\n" +
				    "}");
			AptConfig.setEnabled(jproj, true);

			fullBuild();
			expectingNoProblems();
			expectingUniqueCompiledClasses(
					new String[] {"test.Foo", "test.Bar", "test.FooGen", "test.BarGen"});

		} finally {
			AbstractImageBuilder.MAX_AT_ONCE = old;
			env.removeClass(packagePath, "Foo");
			env.removeClass(packagePath, "Bar");
			env.removeClass(packagePath, "FooGen");
			env.removeClass(packagePath, "BarGen");

		}
	}

	public void testBug468853() throws Throwable {
		int old = AbstractImageBuilder.MAX_AT_ONCE;
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		IPath packagePath = root.append("test");
		try {
			// Force the build to be batched
			AbstractImageBuilder.MAX_AT_ONCE = 2;
			ProcessorTestStatus.reset();

			env.addClass(root, "test", "Foo",
					"package test;\n" +
							"import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;\n" +
							"import org.eclipse.jdt.apt.pluggable.tests.annotations.Message6;\n" +
							"import javax.tools.Diagnostic.Kind;\n" +
							"@GenClass6(name = \"FooGen\", pkg = \"test\", rounds = 2)\n" +
							"@Message6(text = \"APT message\", value = Kind.ERROR)\n" +
							"public class Foo extends FooGen {\n" +
							"    public Bar bar;\n" +
					"}");
			env.addClass(root, "test", "Bar",
					"package test;\n" +
							"public class Bar {\n" +
							"    public Foo foo;\n" +
					"}");
			AptConfig.setEnabled(jproj, true);

			fullBuild();
			expectingNoProblems();
			expectingUniqueCompiledClasses(
					new String[] {"test.Foo", "test.Bar", "test.FooGen", "test.FooGenGen"});

		} finally {
			AbstractImageBuilder.MAX_AT_ONCE = old;
			env.removeClass(packagePath, "Foo");
			env.removeClass(packagePath, "Bar");
			env.removeClass(packagePath, "FooGen");

		}
	}

	public void testBug510118() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IdeTestUtils.copyResources(proj, "targets/bug510118", "src/targets/bug510118");

		AptConfig.setEnabled(jproj, true);
		fullBuild();
		expectingNoProblems();
		assertTrue("Incorrect status received from annotation processor", Bug510118Processor.status());
	}

	public void testBug341298() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject project = createJavaProject(_projectName);
		IPath root = project.getProject().getFullPath().append("src");
		env.addClass(root, "test341298", "Annotated",
				"package test341298;\n" +
				"@Annotation public class Annotated {}"
		);
		env.addClass(root, "test341298", "Annotation",
				"package test341298;\n" +
				"public @interface Annotation {}"
		);
		AptConfig.addProcessorOption(project, "classpath", "%classpath%");
		AptConfig.addProcessorOption(project, "sourcepath", "%sourcepath%");
		AptConfig.addProcessorOption(project, "phase", "%test341298%");
		AptConfig.setEnabled(project, true);
		fullBuild();
		assertTrue("Processor should be able to compile with passed options", Bug341298Processor.success());
	}

	public void testBug539663() throws Throwable {
		if (!canRunJava9()) {
			return;
		}
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJava9Project(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();
		IPath root = projPath.append("src");
		IPath packagePath = root.append("test");
		try {
			env.addClass(root, null, "module-info", "module example {requires annotations;}");

			env.addClass(root, "test", "Foo",
					"package test;\n" +
					"import org.eclipse.jdt.apt.pluggable.tests.annotations.GenClass6;\n" +
			        "@GenClass6(name = \"ImmutableFoo\", pkg = \"test\")\n" +
				    "public class Foo {\n" +
			        "    public void f(ImmutableFoo o) { }\n" +
				    "}");
			AptConfig.setEnabled(jproj, true);

			fullBuild();
			expectingNoProblems();
		} finally {
			env.removeClass(packagePath, "module-info");
			env.removeClass(packagePath, "Foo");
			env.removeClass(packagePath, "ImmutableFoo");
		}
	}
}

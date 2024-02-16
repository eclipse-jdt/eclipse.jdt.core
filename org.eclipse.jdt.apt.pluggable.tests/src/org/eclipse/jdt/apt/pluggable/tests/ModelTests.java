/*******************************************************************************
 * Copyright (c) 2008, 2015 Walter Harley and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    eclipse@cafewalter.com - initial API and implementation
 *    IBM Corporation - 	Added new test testInfoProblems()
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import javax.lang.model.SourceVersion;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.pluggable.tests.processors.modeltester.ModelTester8Proc;
import org.eclipse.jdt.apt.pluggable.tests.processors.modeltester.ModelTesterProc;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * Basic tests for the typesystem model interfaces in the IDE.
 * Note that most coverage of these interfaces is provided by
 * org.eclipse.jdt.compiler.apt.tests.
 */
public class ModelTests extends TestBase
{

	public ModelTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(ModelTests.class);
	}

	/**
	 * Call ModelTesterProc.testFieldType(), which checks the type of a field
	 */
	public void testFieldType() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();

		env.addClass(projPath.append("src"),
				ModelTesterProc.TEST_FIELD_TYPE_PKG,
				ModelTesterProc.TEST_FIELD_TYPE_CLASS,
				ModelTesterProc.TEST_FIELD_TYPE_SOURCE);

		AptConfig.setEnabled(jproj, true);

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	/**
	 * Call ModelTesterProc.testMethodType(), which checks the type of a method
	 */
	public void testMethodType() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();

		env.addClass(projPath.append("src"),
				ModelTesterProc.TEST_METHOD_TYPE_PKG,
				ModelTesterProc.TEST_METHOD_TYPE_CLASS,
				ModelTesterProc.TEST_METHOD_TYPE_SOURCE);

		AptConfig.setEnabled(jproj, true);

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	/**
	 * Test whether problems with severity Info are flagged accordingly.
	 *
	 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=83548
	 */
	public void testInfoProblems() throws Throwable {
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJavaProject(_projectName);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();

		jproj.setOption(CompilerOptions.OPTION_ReportUnusedParameter, CompilerOptions.WARNING);
		jproj.setOption(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, CompilerOptions.INFO);
		jproj.setOption(CompilerOptions.OPTION_ReportUnusedLocal, CompilerOptions.INFO);

		IPath className = env.addClass(projPath.append("src"),
				ModelTesterProc.TEST_METHOD_TYPE_PKG,
				"X",
				"package p;\n" +
				"public class X { \n" +
				"	int i;\n" +
				"	public int foo(int p) {\n" +
				"		int k = 0;\n" +
				"		return i;\n" +
				"	}\n" +
				"}");

		fullBuild();
		expectingProblemsFor(className,
				"Problem : The value of the local variable k is not used [ resource : </"+ _projectName + "/src/p/X.java> range : <68,69> category : <120> severity : <0>]\n" +
				"Problem : The value of the parameter p is not used [ resource : </"+ _projectName + "/src/p/X.java> range : <57,58> category : <120> severity : <1>]\n" +
				"Problem : Unqualified access to the field X.i  [ resource : </"+ _projectName + "/src/p/X.java> range : <84,85> category : <80> severity : <0>]");
		env.removeClass(projPath.append("src").append(ModelTesterProc.TEST_METHOD_TYPE_PKG), ModelTesterProc.TEST_METHOD_TYPE_CLASS);
	}

	/**
	 * Call ModelTester8Proc.testMethodParameters(), which checks the type of a method
	 */
	public void testMethodParameters() throws Throwable {
		if (!canRunJava8()) {
			return;
		}
		ProcessorTestStatus.reset();
		IJavaProject jproj = createJava8Project(_projectName);
		jproj.setOption(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
		jproj.setOption(CompilerOptions.OPTION_MethodParametersAttribute, CompilerOptions.GENERATE);
		disableJava5Factories(jproj);
		IProject proj = jproj.getProject();
		IPath projPath = proj.getFullPath();

		env.addClass(projPath.append("src"),
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE1_PKG,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE1_CLASS,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE1_SOURCE);
		env.addClass(projPath.append("src"),
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE2_PKG,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE2_CLASS,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE2_SOURCE);
		fullBuild();
		expectingNoProblems();
		assertFalse("Processor ran too early", ProcessorTestStatus.processorRan());

		keepBinaryOnly(jproj,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE1_PKG,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE1_CLASS);

		keepBinaryOnly(jproj,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE2_PKG,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE2_CLASS);

		fullBuild();

		env.addClass(projPath.append("src"),
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE3_PKG,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE3_CLASS,
				ModelTester8Proc.TEST_METHOD_PARAMETERS_TYPE3_SOURCE);

		AptConfig.setEnabled(jproj, true);

		fullBuild();
		expectingNoProblems();
		assertTrue("Processor did not run", ProcessorTestStatus.processorRan());
		assertEquals("Processor reported errors", ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	private void keepBinaryOnly(IJavaProject jproj, String packageName, String className) throws CoreException {
		IFile realSourceFile = jproj.getProject().getFolder("src").getFolder(packageName).getFile(className + ".java");
		IFile compiledClassFile = jproj.getProject().getFolder("bin").getFolder(packageName).getFile(className + ".class");
		assertTrue("No compiled class for " + packageName + "." + className + ": ",compiledClassFile.exists());
		IFile prebuiltClassFile = jproj.getProject().getFolder("prebuilt").getFolder(packageName).getFile(className + ".class");
		assertFalse("Compiled class already in src: ",prebuiltClassFile.exists());
		compiledClassFile.copy(prebuiltClassFile.getFullPath(), true, new NullProgressMonitor());
		assertTrue("Compiled class not copied to src",prebuiltClassFile.exists());
		realSourceFile.delete(true, new NullProgressMonitor());
		assertFalse("Still source?: ", realSourceFile.exists());
	}
	public boolean canRunJava8() {
		try {
			SourceVersion.valueOf("RELEASE_8");
		} catch(IllegalArgumentException iae) {
			return false;
		}
		return true;
	}
}

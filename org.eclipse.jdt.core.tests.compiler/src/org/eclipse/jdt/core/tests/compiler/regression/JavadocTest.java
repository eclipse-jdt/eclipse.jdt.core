/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.util.ArrayList;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class JavadocTest extends AbstractRegressionTest {
		
	boolean useLibrary = false;
	static String zipFile = "/TestJavadocVisibility.zip";
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static ArrayList allTestClasses = null;
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");
	static boolean debug = false;

	static {
		allTestClasses = new ArrayList();
		allTestClasses.add(JavadocTestForMethod.class);
		allTestClasses.add(JavadocTestMixed.class);
		allTestClasses.add(JavadocTestForClass.class);
		allTestClasses.add(JavadocTestForConstructor.class);
		allTestClasses.add(JavadocTestForField.class);
		allTestClasses.add(JavadocTestForInterface.class);
		allTestClasses.add(JavadocTestOptions.class);
		// Reset forgotten subsets tests
		TESTS_PREFIX = null;
		TESTS_NAMES = null;
		TESTS_NUMBERS= null;
		TESTS_RANGE = null;
	}
	
	
	public static void addTest(TestSuite suite, Class testClass) {
		TestSuite innerSuite = new TestSuite(testClass);
		suite.addTest(innerSuite);
	}

	public static Test suite() {
		TestSuite ts = new TestSuite(JavadocTest.class.getName());
	
		// Reset forgotten subsets of tests
		TestCase.TESTS_PREFIX = null;
		TestCase.TESTS_NAMES = null;
		TestCase.TESTS_NUMBERS = null;
		TestCase.TESTS_RANGE = null;
	
		for (int i = 0, l=allTestClasses.size(); i < l; i++) {
			Class testClass = (Class) allTestClasses.get(i);
			Test suite = buildTestSuite(testClass);
			ts.addTest(suite);
		}
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			Test suite = buildTestSuite(JavadocTest_1_3.class);
			ts.addTest(new RegressionTestSetup(suite, COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			Test suite = buildTestSuite(JavadocTest_1_4.class);
			ts.addTest(new RegressionTestSetup(suite, COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			Test suite = buildTestSuite(JavadocTest_1_5.class);
			ts.addTest(new RegressionTestSetup(suite, COMPLIANCE_1_5));
		}
		return ts;
	}
	
	public static Test suiteForComplianceLevel(String level, Class testClass) {
		Test suite = buildTestSuite(testClass);
		return new RegressionTestSetup(suite, level);
	}
	
	public JavadocTest(String name) {
		super(name);
	}
	protected Map getCompilerOptions() {
		Map options = super.getCompilerOptions();
		options.put(CompilerOptions.OPTION_ReportFieldHiding, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, CompilerOptions.IGNORE);
		options.put(CompilerOptions.OPTION_DocCommentSupport, CompilerOptions.ENABLED);
		return options;
	}
	
	protected String[] getDefaultClassPaths() {
		if (useLibrary) {
			String[] classLibs = super.getDefaultClassPaths();
			final int length = classLibs.length;
			String[] newClassPaths = new String[length + 1];
			System.arraycopy(classLibs, 0, newClassPaths, 0, length);
			newClassPaths[length] = getClass().getResource(zipFile).getPath();
			return newClassPaths;
		}
		return super.getDefaultClassPaths();
	}
	
	static String[] referencedClasses = null;
	static {
		referencedClasses =
			new String[] {
				"test/AbstractVisibility.java",
				"package test;\n"
					+ "public abstract class AbstractVisibility {\n"
					+ "	private class AvcPrivate {\n"
					+ "		private int avf_private = 10;\n"
					+ "		public int avf_public = avf_private;\n"
					+ "		private int avm_private() {\n"
					+ "			avf_private = (new AvcPrivate()).avf_private;\n"
					+ "			return avf_private;\n"
					+ "		}\n"
					+ "		public int avm_public() {\n"
					+ "			return avm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class AvcPublic {\n"
					+ "		private int avf_private = 10;\n"
					+ "		public int avf_public = avf_private;\n"
					+ "		private int avm_private() {\n"
					+ "			avf_private = (new AvcPrivate()).avf_private;\n"
					+ "			return avf_private;\n"
					+ "		}\n"
					+ "		public int avm_public() {\n"
					+ "			return avm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int avf_private = 100;\n"
					+ "	public int avf_public = avf_private;\n"
					+ "	\n"
					+ "	private int avm_private() {\n"
					+ "		avf_private = (new AvcPrivate()).avf_private;\n"
					+ "		return avf_private;\n"
					+ "	}\n"
					+ "	public int avm_public() {\n"
					+ "		return avm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/Visibility.java",
				"package test;\n"
					+ "public class Visibility extends AbstractVisibility {\n"
					+ "	private class VcPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VcPrivate()).vf_private;\n"
					+ "			avf_private = vf_private;\n"
					+ "			return vf_private+avf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	};\n"
					+ "	public class VcPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VcPrivate()).vf_private;\n"
					+ "			avf_private = vf_private;\n"
					+ "			return vf_private+avf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	};\n"
					+ "	private int vf_private = 100;\n"
					+ "	private int avf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	public int avf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VcPrivate()).vf_private;\n"
					+ "		avf_private = vf_private;\n"
					+ "		return vf_private+avf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/copy/VisibilityPackage.java",
				"package test.copy;\n"
					+ "class VisibilityPackage {\n"
					+ "	private class VpPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class VpPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int vf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VpPrivate()).vf_private;\n"
					+ "		return vf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n",
				"test/copy/VisibilityPublic.java",
				"package test.copy;\n"
					+ "public class VisibilityPublic {\n"
					+ "	private class VpPrivate {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	public class VpPublic {\n"
					+ "		private int vf_private = 10;\n"
					+ "		public int vf_public = vf_private;\n"
					+ "		private int vm_private() {\n"
					+ "			vf_private = (new VpPrivate()).vf_private;\n"
					+ "			return vf_private;\n"
					+ "		}\n"
					+ "		public int vm_public() {\n"
					+ "			return vm_private();\n"
					+ "		}\n"
					+ "	}\n"
					+ "	private int vf_private = 100;\n"
					+ "	public int vf_public = vf_private;\n"
					+ "	\n"
					+ "	private int vm_private() {\n"
					+ "		vf_private = (new VpPrivate()).vf_private;\n"
					+ "		return vf_private;\n"
					+ "	}\n"
					+ "	public int vm_public() {\n"
					+ "		return vm_private();\n"
					+ "	}\n"
					+ "}\n" };
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
//		SHIFT = true;
//		INDENT = 3;
	}

	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
//		SHIFT = false;
//		INDENT = 2;
		super.tearDown();
	}

	protected void runConformReferenceTest(String[] testFiles) {
		String[] completedFiles = testFiles;
		if (!useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
		}
		runConformTest(completedFiles);
	}
	protected void runNegativeReferenceTest(String[] testFiles, String expected) {
		String[] completedFiles = testFiles;
		if (!useLibrary) {
			completedFiles = new String[testFiles.length + referencedClasses.length];
			System.arraycopy(referencedClasses, 0, completedFiles, 0, referencedClasses.length);
			System.arraycopy(testFiles, 0, completedFiles, referencedClasses.length, testFiles.length);
		}
		runNegativeTest(completedFiles, expected);
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;
import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public abstract class JavadocTest extends AbstractRegressionTest {
		
	boolean useLibrary = false;
	static String ZIP_FILE = "/TestJavadocVisibility.zip";
	static final String LINE_SEPARATOR = System.getProperty("line.separator");
	public static ArrayList ALL_CLASSES = null;
	static final String DOC_COMMENT_SUPPORT = System.getProperty("doc.support");
	static boolean debug = false;

	static {
		ALL_CLASSES = new ArrayList();
		ALL_CLASSES.add(JavadocBugsTest.class);
		ALL_CLASSES.add(JavadocTestForMethod.class);
		ALL_CLASSES.add(JavadocTestMixed.class);
		ALL_CLASSES.add(JavadocTestForClass.class);
		ALL_CLASSES.add(JavadocTestForConstructor.class);
		ALL_CLASSES.add(JavadocTestForField.class);
		ALL_CLASSES.add(JavadocTestForInterface.class);
		ALL_CLASSES.add(JavadocTestOptions.class);
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
	
		for (int i = 0, size=ALL_CLASSES.size(); i < size; i++) {
			Class testClass = (Class) ALL_CLASSES.get(i);
			Test suite = buildTestSuite(testClass);
			ts.addTest(suite);
		}
		int complianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
		if ((complianceLevels & AbstractCompilerTest.F_1_3) != 0) {
			ts.addTest(buildTestSuite(JavadocTest_1_3.class, COMPLIANCE_1_3));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_4) != 0) {
			ts.addTest(buildTestSuite(JavadocTest_1_4.class, COMPLIANCE_1_4));
		}
		if ((complianceLevels & AbstractCompilerTest.F_1_5) != 0) {
			ts.addTest(buildTestSuite(JavadocTest_1_5.class, COMPLIANCE_1_5));
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
		// Set default before bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=110964 changes
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility, CompilerOptions.PRIVATE);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef, CompilerOptions.ENABLED);
		options.put(CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility, CompilerOptions.PRIVATE);
		return options;
	}
	
	protected String[] getDefaultClassPaths() {
		if (useLibrary) {
			String[] classLibs = super.getDefaultClassPaths();
			final int length = classLibs.length;
			String[] newClassPaths = new String[length + 1];
			System.arraycopy(classLibs, 0, newClassPaths, 0, length);
			newClassPaths[length] = getClass().getResource(ZIP_FILE).getPath();
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
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runConformTest(java.lang.String[], java.lang.String, java.lang.String[], boolean, java.lang.String[], java.util.Map, org.eclipse.jdt.internal.compiler.ICompilerRequestor)
	 *
	protected void runConformTest(String[] testFiles,
			String expectedSuccessOutputString,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			String[] vmArguments,
			Map customOptions,
			ICompilerRequestor clientRequestor) {
		if (TESTS_NAMES != null || TESTS_PREFIX != null || TESTS_NUMBERS != null || TESTS_RANGE != null) {
			writeFiles(testFiles);
		}
		super.runConformTest(testFiles,
			expectedSuccessOutputString,
			classLib,
			shouldFlushOutputDirectory,
			vmArguments,
			customOptions,
			clientRequestor);
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.tests.compiler.regression.AbstractRegressionTest#runNegativeTest(java.lang.String[], java.lang.String, java.lang.String[], boolean, java.util.Map, boolean)
	 *
	protected void runNegativeTest(String[] testFiles,
			String expectedProblemLog,
			String[] classLib,
			boolean shouldFlushOutputDirectory,
			Map customOptions,
			boolean generateOutput) {
		if (TESTS_NAMES != null || TESTS_PREFIX != null || TESTS_NUMBERS != null || TESTS_RANGE != null) {
			writeFiles(testFiles);
		}
		super.runNegativeTest(testFiles,
			expectedProblemLog,
			classLib,
			shouldFlushOutputDirectory,
			customOptions,
			generateOutput);
	}
	*/
	void writeFiles(String[] testFiles) {
		String classDirName = getClass().getName().substring(getClass().getName().lastIndexOf('.')+1); //.substring(11);
		String testName = getName();
		int idx = testName.indexOf(" - ");
		if (idx > 0) {
			testName = testName.substring(idx+3);
		}

		File dir = new File("d:/usr/OTI/tests/javadoc/");
		if (!dir.exists()) return;
		dir = new File(dir, classDirName);
		if (!dir.exists()) dir.mkdirs();
		dir = new File(dir, Character.toUpperCase(testName.charAt(0))+testName.substring(1));
		if (!dir.exists()) dir.mkdirs();
		System.out.println("Write test file to "+dir+"...");
		for (int i=0, length=testFiles.length; i<length; i++) {
			String contents = testFiles[i+1];
			String fileName = testFiles[i++];
			String dirFileName = dir.getPath();
			if (fileName.indexOf("Visibility")>0) {
				continue;
			} else {
				int index = fileName.lastIndexOf('/');
				if (index > 0) {
					String subdirs = fileName.substring(0, index);
					String packName = subdirs.replace('/', '.');
					contents = "package "+packName+";"+contents.substring(contents.indexOf(';')+1);
					dir = new File(dirFileName, subdirs);
					if (!dir.exists()) dir.mkdirs();
					fileName = fileName.substring(index+1);
				}
			}
			Util.writeToFile(contents, dirFileName+"/"+fileName);
		}
	}
}

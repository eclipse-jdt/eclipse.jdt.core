/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.*;
import junit.framework.Test;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class JavaConventionTests extends AbstractJavaModelTests {
	private final static String sourceLevel = CompilerOptions.VERSION_1_3; 
	private final static String complianceLevel = CompilerOptions.VERSION_1_3;
	public JavaConventionTests(String name) {
		super(name);
	}

	static {
//		TESTS_PREFIX = "testInvalidCompilerOptions";
//		TESTS_NAMES = new String[] { "testValidCompilerOptions", "testInvalidCompilerOptions" };
	}
	public static Test suite() {
		return buildModelTestSuite(JavaConventionTests.class);
	}

	/*
	 * Verify status type and messages.
	 */
	protected void verifyStatus(IStatus status, String[] expectedMessages) {
		int length = expectedMessages.length;
		IStatus[] allStatus = status.getChildren();
		switch (length) {
			case 0:
				assertTrue("Status should be OK!", status.isOK());
				return;
			case 1:
				assertFalse("Unexpected type of status!", status.isMultiStatus());
				assertEquals("Unexpected number of errors!", length, 1);
				allStatus = new IStatus[] { status };
				break;
			default:
				assertTrue("Unexpected type of status!", status.isMultiStatus());
				assertEquals("Unexpected number of errors!", length, allStatus.length);
				break;
		}
		List failuresMessages = new ArrayList();
		for (int i=0; i<length; i++) {
			assertFalse("Status should be KO!", allStatus[i].isOK());
			if (!allStatus[i].getMessage().equals(expectedMessages[i])) {
				failuresMessages.add(allStatus[i].getMessage());
			}
		}
		int count = failuresMessages.size();
		if (count > 0) {
			System.out.println("Test "+getName()+" fails. Add this declaration to fix it:");
			System.out.println("		String[] expectedMessages = {");
			for (int i=0; i<count; i++) {
				System.out.print("			\""+failuresMessages.get(i)+"\"");
				if (i==(count-1))
					System.out.println("");
				else
					System.out.println(",");
			}
			System.out.println("		};");
		}
		assertTrue("There "+(count>1?"are ":"is ")+count+" unexpected status!", count==0);
	}

	/**
	 * @see JavaConventions
	 */
	public void testInvalidIdentifier() {
		String[] invalidIds = new String[] {" s\\u0069ze", " s\\u0069ze ", "", "1java", "Foo Bar", "#@$!", "Foo-Bar", "if", "InvalidEscapeSequence\\g", "true", "false", "null", null, " untrimmmed "};
		for (int i = 0; i < invalidIds.length; i++) {
			assertTrue("identifier not recognized as invalid: " + invalidIds[i], !JavaConventions.validateIdentifier(invalidIds[i], sourceLevel, complianceLevel).isOK());
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration1() {
		assertTrue("import not reconized as invalid; java.math.", !JavaConventions.validateImportDeclaration("java.math.", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration2() {
		assertTrue("import not reconized as invalid; java.math*", !JavaConventions.validateImportDeclaration("java.math*", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration3() {
		assertTrue("import not reconized as invalid; empty string", !JavaConventions.validateImportDeclaration("", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * Test for package fragment root overlap
	 * @deprecated isOverlappingRoots is deprecated
	 */
	public void testPackageFragmentRootOverlap() throws Exception {
		try {
			IJavaProject project = this.createJavaProject("P1", new String[] {"src"}, new String[] {"/P1/jclMin.jar"}, "bin");
			
			// ensure the external JCL is copied
			setupExternalJCL("jclMin");
			
			this.copy(new java.io.File(getExternalJCLPathString()), new java.io.File(getWorkspaceRoot().getLocation().toOSString() + java.io.File.separator + "P1" + java.io.File.separator + "jclMin.jar"));
			project.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
			
			IPackageFragmentRoot p1Zip= getPackageFragmentRoot("P1", "jclMin.jar");
			IPackageFragmentRoot p1Src= getPackageFragmentRoot("P1", "src");
		
			assertTrue("zip should not overlap source root",
					!JavaConventions.isOverlappingRoots(p1Zip.getUnderlyingResource().getFullPath(), p1Src.getUnderlyingResource().getFullPath()));
		
			this.createJavaProject("P2", new String[] {"src"}, "bin");
		
			IPackageFragmentRoot p2Src= getPackageFragmentRoot("P2", "src");
			assertTrue("source roots in different projects should not overlap ",
					!JavaConventions.isOverlappingRoots(p1Src.getUnderlyingResource().getFullPath(), p2Src.getUnderlyingResource().getFullPath()));
		
			assertTrue("The same root should overlap", JavaConventions.isOverlappingRoots(p2Src.getUnderlyingResource().getFullPath(), p2Src.getUnderlyingResource().getFullPath()));
		
			assertTrue("isOverLappingRoot does not handle null arguments", !JavaConventions.isOverlappingRoots(p2Src.getUnderlyingResource().getFullPath(), null));
			assertTrue("isOverLappingRoot does not handle null arguments", !JavaConventions.isOverlappingRoots(null, null));
		} finally {
			this.deleteProject("P1");
			this.deleteProject("P2");
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidCompilationUnitName() {
		String[] invalidNames = new String[] {"java/lang/Object.java", "Object.class", ".java", "Object.javaaa", "A.B.java"};
		for (int i = 0; i < invalidNames.length; i++) {
			assertTrue("compilation unit name not recognized as invalid: " + invalidNames[i], !JavaConventions.validateCompilationUnitName(invalidNames[i], sourceLevel, complianceLevel).isOK());
		}
		String[] validNames = new String[] {"Object.java", "OBJECT.java", "object.java", "package-info.java"};
		for (int i = 0; i < validNames.length; i++) {
			assertTrue("compilation unit name not recognized as valid: " + validNames[i], JavaConventions.validateCompilationUnitName(validNames[i], sourceLevel, complianceLevel).isOK());
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidFieldName() {
		assertTrue("unicode field name not handled", JavaConventions.validateFieldName("s\\u0069ze", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidIdentifier() {
		String[] validIds = new String[] {"s\\u0069ze", "Object", "An_Extremly_Long_Class_Name_With_Words_Separated_By_Undescores"};
		for (int i = 0; i < validIds.length; i++) {
			assertTrue("identifier not recognized as valid: " + validIds[i], JavaConventions.validateIdentifier(validIds[i], sourceLevel, complianceLevel).isOK());
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidImportDeclaration() {
		assertTrue("import not reconized as valid", JavaConventions.validateImportDeclaration("java.math.*", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidMethodName() {
		assertTrue("unicode method name not handled", JavaConventions.validateMethodName("getSiz\\u0065", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidPackageName() {
		
		String pkgName= "org.eclipse.jdt.core.t\\u0065sts.MyPackage";
		assertTrue("unicode package name not handled", JavaConventions.validatePackageName(pkgName, sourceLevel, complianceLevel).isOK());
	
		assertTrue("package name not recognized as invalid1", !JavaConventions.validatePackageName("", sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as valid1", JavaConventions.validatePackageName("java . lang", sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as invalid2", !JavaConventions.validatePackageName("   java . lang", sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as invalid3", !JavaConventions.validatePackageName("java . lang  ", sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as invalid4", !JavaConventions.validatePackageName(null, sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as unconventional1", JavaConventions.validatePackageName("Java.lang", sourceLevel, complianceLevel).getSeverity() == IStatus.WARNING);
		assertTrue("package name not recognized as valid2", JavaConventions.validatePackageName("java.Lang", sourceLevel, complianceLevel).isOK());
		assertTrue("package name not recognized as invalid5", JavaConventions.validatePackageName("Test.sample&plugin", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
		assertTrue("package name not recognized as unconventional2", JavaConventions.validatePackageName("Test.sample", sourceLevel, complianceLevel).getSeverity() == IStatus.WARNING);
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidTypeName() {
		// regression tests for 1G5HVPB: ITPJCORE:WINNT - validateJavaTypeName accepts type names ending with \
		assertTrue("type name should not contain slashes (1)", JavaConventions.validateJavaTypeName("Object\\", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
		assertTrue("type name should not contain slashes (2)", JavaConventions.validateJavaTypeName("Object/", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
		assertTrue("type name should not contain slashes (3)", JavaConventions.validateJavaTypeName("\\Object", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
		assertTrue("type name should not contain slashes (4)", JavaConventions.validateJavaTypeName("java\\lang\\Object", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
	
		// regression test for 1G52ZIF: ITPJUI:WINNT - Wizards should strongly discourage the use of non-standard names
		assertTrue("discouraged type names not handled", JavaConventions.validateJavaTypeName("alowercasetypename", sourceLevel, complianceLevel).getSeverity() == IStatus.WARNING);
	
		// other tests
		assertTrue("unicode type name not handled", JavaConventions.validateJavaTypeName("P\\u0065a", sourceLevel, complianceLevel).getSeverity() == IStatus.OK);
		assertTrue("qualified type names not handled", JavaConventions.validateJavaTypeName("java  .  lang\t.Object", sourceLevel, complianceLevel).getSeverity() == IStatus.OK);
		assertTrue("simple qualified type names not handled", JavaConventions.validateJavaTypeName("java.lang.Object", sourceLevel, complianceLevel).getSeverity() == IStatus.OK);
		assertTrue("simple type names not handled", JavaConventions.validateJavaTypeName("Object", sourceLevel, complianceLevel).getSeverity() == IStatus.OK);
		assertTrue("discouraged type names not handled", JavaConventions.validateJavaTypeName("Object$SubType", sourceLevel, complianceLevel).getSeverity() == IStatus.WARNING);
		assertTrue("invalid type name not recognized", JavaConventions.validateJavaTypeName("==?==", sourceLevel, complianceLevel).getSeverity() == IStatus.ERROR);
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidTypeVariableName() {
		assertTrue("E sould be a valid variable name", JavaConventions.validateTypeVariableName("E", sourceLevel, complianceLevel).isOK());
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidUnicodeImportDeclaration() {
		
		String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
		assertTrue("import not reconized as valid", JavaConventions.validateImportDeclaration(pkgName, sourceLevel, complianceLevel).isOK());
	
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidUnicodePackageName() {
		
		String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
		assertTrue("unicode package name not handled", JavaConventions.validatePackageName(pkgName, sourceLevel, complianceLevel).isOK());
		assertTrue("Parameter modified", pkgName.equals("com.\\u0069bm.jdt.core.tests.MyPackag\\u0065"));
	
	}

	/**
	 * Test fix for bug 79392: [prefs] JavaConventions should offer compiler options validation API
	 * @see <a href="http://bugs.eclipse.org/bugs/show_bug.cgi?id=79392">79392</a>
	 * TODO (frederic) activate all following tests when bug 79392 will be finalized
	 */
	/*
	public void testInvalidCompilerOptions01() throws CoreException, BackingStoreException {
		// Set options
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		// Validate options
		String[] expectedMessages = {
			"Target level '1.4' is incompatible with source level '1.5'. A target level '1.5' or better is required"
		};
		verifyStatus(JavaConventions.validateCompilerOptions(options), expectedMessages);
	}
	public void testInvalidCompilerOptions02() throws CoreException, BackingStoreException {
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);

		// Validate options
		String[] expectedMessages = {
			"Target level '1.2' is incompatible with source level '1.5'. A target level '1.5' or better is required",
			"Compliance level '1.4' is incompatible with source level '1.5'. A compliance level '1.5' or better is required"
		};
		verifyStatus(JavaConventions.validateCompilerOptions(options), expectedMessages);
	}
	public void testInvalidCompilerOptions04() throws CoreException, BackingStoreException {
		// Set options
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_3);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_5);

		// Validate options
		String[] expectedMessages = {
			"Compliance level '1.3' is incompatible with target level '1.4'. A compliance level '1.4' or better is required",
			"Compliance level '1.3' is incompatible with source level '1.4'. A compliance level '1.4' or better is required"
		};
		verifyStatus(JavaConventions.validateCompilerOptions(options), expectedMessages);
	}
	public void testValidCompilerOptions01() throws CoreException, BackingStoreException {
		// Set options
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		// Validate options
		String[] expectedMessages = {};
		verifyStatus(JavaConventions.validateCompilerOptions(options), expectedMessages);
	}
	public void testValidCompilerOptions02() throws CoreException, BackingStoreException {
		// Set options
		Map options = JavaCore.getOptions();
		options.put(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_4);
		options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_3);
		options.put(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, JavaCore.VERSION_1_4);

		// Validate options
		String[] expectedMessages = {};
		verifyStatus(JavaConventions.validateCompilerOptions(options), expectedMessages);
	}
	*/
}

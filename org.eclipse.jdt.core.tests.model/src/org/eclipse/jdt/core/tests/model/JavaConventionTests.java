/*******************************************************************************
 * Copyright (c) 2000, 2017 IBM Corporation and others.
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

@SuppressWarnings({"rawtypes", "unchecked"})
public class JavaConventionTests extends AbstractJavaModelTests {
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

	// Kind of validations
	static final int COMPILATION_UNIT_NAME = 1;
	static final int CLASS_FILE_NAME = 2;
	static final int FIELD_NAME = 3;
	static final int IDENTIFIER = 4;
	static final int IMPORT_DECLARATION = 5;
	static final int JAVA_TYPE_NAME = 6;
	static final int METHOD_NAME = 7;
	static final int PACKAGE_NAME = 8;
	static final int TYPE_VARIABLE_NAME = 9;

	// All possible compiler versions
	static final String[] VERSIONS = new String[] {
		CompilerOptions.VERSION_1_1,
		CompilerOptions.VERSION_1_2,
		CompilerOptions.VERSION_1_3,
		CompilerOptions.VERSION_1_4,
		CompilerOptions.VERSION_1_5,
		CompilerOptions.VERSION_1_6,
		CompilerOptions.VERSION_1_7,
		CompilerOptions.VERSION_1_8,
		CompilerOptions.VERSION_9,
		CompilerOptions.VERSION_10,
	};

	/*
	 * Return the status for a string regarding a given kind of validation.
	 * Use JavaConventions default source and compliance levels.
	 */
	int validate(String string, int kind) {
		return validate(string, kind, CompilerOptions.VERSION_1_3, CompilerOptions.VERSION_1_3);
	}

	/*
	 * Return the status for a string regarding a given kind of validation.
	 */
	int validate(String string, int kind, String sourceLevel, String complianceLevel) {
		switch (kind) {
			case COMPILATION_UNIT_NAME:
				return JavaConventions.validateCompilationUnitName(string, sourceLevel, complianceLevel).getSeverity();
			case CLASS_FILE_NAME:
				return JavaConventions.validateClassFileName(string, sourceLevel, complianceLevel).getSeverity();
			case FIELD_NAME:
				return JavaConventions.validateFieldName(string, sourceLevel, complianceLevel).getSeverity();
			case IDENTIFIER:
				return JavaConventions.validateIdentifier(string, sourceLevel, complianceLevel).getSeverity();
			case IMPORT_DECLARATION:
				return JavaConventions.validateImportDeclaration(string, sourceLevel, complianceLevel).getSeverity();
			case JAVA_TYPE_NAME:
				return JavaConventions.validateJavaTypeName(string, sourceLevel, complianceLevel).getSeverity();
			case METHOD_NAME:
				return JavaConventions.validateMethodName(string, sourceLevel, complianceLevel).getSeverity();
			case PACKAGE_NAME:
				return JavaConventions.validatePackageName(string, sourceLevel, complianceLevel).getSeverity();
			case TYPE_VARIABLE_NAME:
				return JavaConventions.validateTypeVariableName(string, sourceLevel, complianceLevel).getSeverity();
		}
		return -1;
	}
	void validateModuleName(String name, String sourceLevel, String complianceLevel, int sev, String message) {
		IStatus s = JavaConventions.validateModuleName(name, sourceLevel, complianceLevel);
		assertEquals("incorrect status", sev, s.getSeverity());
		assertEquals("incorrect status message", message, s.getMessage());
	}

	/**
	 * @see JavaConventions
	 */
	public void testInvalidIdentifier() {
		String[] invalidIds = new String[] {" s\\u0069ze", " s\\u0069ze ", "", "1java", "Foo Bar", "#@$!", "Foo-Bar", "if", "InvalidEscapeSequence\\g", "true", "false", "null", null, " untrimmmed "};
		for (int i = 0; i < invalidIds.length; i++) {
			assertEquals("identifier not recognized as invalid: " + invalidIds[i], IStatus.ERROR, validate(invalidIds[i], IDENTIFIER));
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration1() {
		assertEquals("import not reconized as invalid; java.math.", IStatus.ERROR, validate("java.math.", IMPORT_DECLARATION));
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration2() {
		assertEquals("import not reconized as invalid; java.math*", IStatus.ERROR, validate("java.math*", IMPORT_DECLARATION));
	}
	/**
	 * @see JavaConventions
	 */
	public void testInvalidImportDeclaration3() {
		assertEquals("import not reconized as invalid; empty string", IStatus.ERROR, validate("", IMPORT_DECLARATION));
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

			copy(new java.io.File(getExternalJCLPathString()), new java.io.File(getWorkspaceRoot().getLocation().toOSString() + java.io.File.separator + "P1" + java.io.File.separator + "jclMin.jar"));
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
			assertEquals("compilation unit name not recognized as invalid: " + invalidNames[i], IStatus.ERROR, validate(invalidNames[i], COMPILATION_UNIT_NAME));
		}
		invalidNames = new String[] {"module-package.class"};
		for (int i = 0; i < invalidNames.length; i++) {
			assertEquals("compilation unit name not recognized as valid: " + invalidNames[i], IStatus.ERROR, validate(invalidNames[i], CLASS_FILE_NAME));
		}
		String[] validNames = new String[] {"Object.java", "OBJECT.java", "object.java", "package-info.java", "module-info.java"};
		for (int i = 0; i < validNames.length; i++) {
			assertEquals("compilation unit name not recognized as valid: " + validNames[i], IStatus.OK, validate(validNames[i], COMPILATION_UNIT_NAME));
		}
		validNames = new String[] {"module-info.class"};
		for (int i = 0; i < validNames.length; i++) {
			assertEquals("compilation unit name not recognized as valid: " + validNames[i], IStatus.OK, validate(validNames[i], CLASS_FILE_NAME));
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidFieldName() {
		assertEquals("unicode field name not handled", IStatus.OK, validate("s\\u0069ze", FIELD_NAME));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidIdentifier() {
		String[] validIds = new String[] {"s\\u0069ze", "Object", "An_Extremly_Long_Class_Name_With_Words_Separated_By_Undescores"};
		for (int i = 0; i < validIds.length; i++) {
			assertEquals("identifier not recognized as valid: " + validIds[i], IStatus.OK, validate(validIds[i], IDENTIFIER));
		}
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidImportDeclaration() {
		assertEquals("import not reconized as valid", IStatus.OK, validate("java.math.*", IMPORT_DECLARATION));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidMethodName() {
		assertEquals("unicode method name not handled", IStatus.OK, validate("getSiz\\u0065", METHOD_NAME));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidPackageName() {

		String pkgName= "org.eclipse.jdt.core.t\\u0065sts.MyPackage";
		assertEquals("unicode package name not handled", IStatus.OK, validate(pkgName, PACKAGE_NAME));

		assertEquals("package name not recognized as invalid1", IStatus.ERROR, validate("", PACKAGE_NAME));
		assertEquals("package name not recognized as valid1", IStatus.OK, validate("java . lang", PACKAGE_NAME));
		assertEquals("package name not recognized as invalid2", IStatus.ERROR, validate("   java . lang", PACKAGE_NAME));
		assertEquals("package name not recognized as invalid3", IStatus.ERROR, validate("java . lang  ", PACKAGE_NAME));
		assertEquals("package name not recognized as invalid4", IStatus.ERROR, validate(null, PACKAGE_NAME));
		assertEquals("package name not recognized as unconventional1", IStatus.WARNING, validate("Java.lang", PACKAGE_NAME));
		assertEquals("package name not recognized as valid2", IStatus.OK, validate("java.Lang", PACKAGE_NAME));
		assertEquals("package name not recognized as invalid5", IStatus.ERROR, validate("Test.sample&plugin", PACKAGE_NAME));
		assertEquals("package name not recognized as unconventional2", IStatus.WARNING, validate("Test.sample", PACKAGE_NAME));
		assertEquals("package name not recognized as valid2", IStatus.OK, validate("com.  sap", PACKAGE_NAME));
		assertEquals("package name not recognized as invalid6", IStatus.ERROR, validate("co m.sap", PACKAGE_NAME));
		assertEquals("package name not recognized as valid2", IStatus.OK, validate("elnu", PACKAGE_NAME));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidTypeName() {
		// regression tests for 1G5HVPB: ITPJCORE:WINNT - validateJavaTypeName accepts type names ending with \
		assertEquals("type name should not contain slashes (1)", IStatus.ERROR, validate("Object\\", JAVA_TYPE_NAME));
		assertEquals("type name should not contain slashes (2)", IStatus.ERROR, validate("Object/", JAVA_TYPE_NAME));
		assertEquals("type name should not contain slashes (3)", IStatus.ERROR, validate("\\Object", JAVA_TYPE_NAME));
		assertEquals("type name should not contain slashes (4)", IStatus.ERROR, validate("java\\lang\\Object", JAVA_TYPE_NAME));

		// regression test for 1G52ZIF: ITPJUI:WINNT - Wizards should strongly discourage the use of non-standard names
		assertEquals("discouraged type names not handled", IStatus.WARNING, validate("alowercasetypename", JAVA_TYPE_NAME));

		// other tests
		assertEquals("unicode type name not handled", IStatus.OK, validate("P\\u0065a", JAVA_TYPE_NAME));
		assertEquals("qualified type names not handled", IStatus.OK, validate("java  .  lang\t.Object", JAVA_TYPE_NAME));
		assertEquals("simple qualified type names not handled", IStatus.OK, validate("java.lang.Object", JAVA_TYPE_NAME));
		assertEquals("simple type names not handled", IStatus.OK, validate("Object", JAVA_TYPE_NAME));
		assertEquals("discouraged type names not handled", IStatus.WARNING, validate("Object$SubType", JAVA_TYPE_NAME));
		assertEquals("invalid type name not recognized", IStatus.ERROR, validate("==?==", JAVA_TYPE_NAME));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidTypeVariableName() {
		assertEquals("E sould be a valid variable name", IStatus.OK, validate("E", TYPE_VARIABLE_NAME));
	}
	/**
	 * @see JavaConventions
	 */
	public void testValidUnicodeImportDeclaration() {

		String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
		assertEquals("import not reconized as valid", IStatus.OK, validate(pkgName, IMPORT_DECLARATION));

	}
	/**
	 * @see JavaConventions
	 */
	public void testValidUnicodePackageName() {

		String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
		assertEquals("unicode package name not handled", IStatus.OK, validate(pkgName, PACKAGE_NAME));
		assertEquals("Parameter modified", "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065", pkgName);

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

	/**
	 * @bug 161621: enum is a Keyword for Java5 and cannot be used as a Enum name
	 * @test Ensure that 'assert' identifier is rejected when source level greater than 1.3
	 * 	and that 'enum' identifier is rejected when source level greater than 1.4
	 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=161621"
	 */
	public void testAssertIdentifier() {
		int length = VERSIONS.length;
		for (int i=0; i<length; i++) { // source level
			for (int j=0; j<length; j++) { // compliance level
				if (i < 3) { // source level < VERSION_1_4
					assertEquals("'assert' should be accepted with source level "+VERSIONS[i], IStatus.OK, validate("assert", IDENTIFIER,VERSIONS[i], VERSIONS[j]));
					assertEquals("By convention, Java type names usually start with an uppercase letter", IStatus.WARNING, validate("assert", JAVA_TYPE_NAME,VERSIONS[i], VERSIONS[j]));
				} else {
					assertEquals("'assert' should be rejected with source level "+VERSIONS[i], IStatus.ERROR, validate("assert", IDENTIFIER,VERSIONS[i], VERSIONS[j]));
				}
			}
		}
	}
	public void testEnumIdentifier() {
		int length = VERSIONS.length;
		for (int i=0; i<length; i++) { // source level
			for (int j=0; j<length; j++) { // compliance level
				if (i < 4) { // source level < VERSION_1_5
					assertEquals("'enum' should be accepted with source level "+VERSIONS[i], IStatus.OK, validate("enum", IDENTIFIER,VERSIONS[i], VERSIONS[j]));
					assertEquals("By convention, Java type names usually start with an uppercase letter", IStatus.WARNING, validate("enum", JAVA_TYPE_NAME,VERSIONS[i], VERSIONS[j]));
				} else {
					assertEquals("'enum' should be rejected with source level "+VERSIONS[i], IStatus.ERROR, validate("enum", IDENTIFIER,VERSIONS[i], VERSIONS[j]));
				}
			}
		}
	}

	public void testModuleName() {
		int length = VERSIONS.length;
		for (int i = 0; i < length; i++) { // source level
			for (int j = 0; j < length; j++) { // compliance level
				if (i >= 8 && j >= 8) { // source and compliance level > VERSION_9
					validateModuleName(null, VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name must not be null");
					validateModuleName("mod.one", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("mod_one", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("one.java", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("m0d.one", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("1mod1. one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'1mod1' is not a valid Java identifier");
					validateModuleName("mod1.2one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'2one' is not a valid Java identifier");
					validateModuleName("mod..one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name must not contain consecutive dots");
					validateModuleName(".mod.one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name cannot start or end with a dot");
					validateModuleName("mod.one.", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name cannot start or end with a dot");
					validateModuleName(".mod.one.", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name cannot start or end with a dot");
					validateModuleName("mod.one ", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name must not start or end with a blank");
					validateModuleName(" mod.one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "A module name must not start or end with a blank");
					validateModuleName("java one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'java one' is not a valid Java identifier");
					validateModuleName("mod,one", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'mod,one' is not a valid Java identifier");
					validateModuleName("mod1.one", VERSIONS[i], VERSIONS[j], IStatus.WARNING, "A module name should avoid terminal digits");
					validateModuleName("mod.one1.two", VERSIONS[i], VERSIONS[j], IStatus.WARNING, "A module name should avoid terminal digits");
					validateModuleName("java.one", VERSIONS[i], VERSIONS[j], IStatus.WARNING, "java is reserved for system modules");
					validateModuleName("mod. one", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");

					// Now try using keywords
					validateModuleName("module.requires", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("exports.to", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("provides.with", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("opens.uses", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("transitive.open", VERSIONS[i], VERSIONS[j], IStatus.OK, "OK");
					validateModuleName("static.requires", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'static' is not a valid Java identifier");
					validateModuleName("class.interface.method", VERSIONS[i], VERSIONS[j], IStatus.ERROR, "'class' is not a valid Java identifier");
				}
			}
		}
	}
}

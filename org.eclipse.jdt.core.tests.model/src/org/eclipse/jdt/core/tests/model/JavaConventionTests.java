package org.eclipse.jdt.core.tests.model;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.JavaElement;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JavaConventionTests extends AbstractJavaModelTests {
public JavaConventionTests(String name) {
	super(name);
}

public static Test suite() {
	TestSuite suite = new Suite(JavaConventionTests.class.getName());

	suite.addTest(new JavaConventionTests("testInvalidImportDeclaration1"));
	suite.addTest(new JavaConventionTests("testInvalidImportDeclaration2"));
	suite.addTest(new JavaConventionTests("testInvalidImportDeclaration3"));
	suite.addTest(new JavaConventionTests("testPackageFragmentRootOverlap"));
	suite.addTest(new JavaConventionTests("testValidCompilationUnitName"));
	suite.addTest(new JavaConventionTests("testValidIdentifier"));
	suite.addTest(new JavaConventionTests("testInvalidIdentifier"));
	suite.addTest(new JavaConventionTests("testValidFieldName"));
	suite.addTest(new JavaConventionTests("testValidImportDeclaration"));
	suite.addTest(new JavaConventionTests("testValidMethodName"));
	suite.addTest(new JavaConventionTests("testValidPackageName"));
	suite.addTest(new JavaConventionTests("testValidTypeName"));
	suite.addTest(new JavaConventionTests("testValidUnicodeImportDeclaration"));
	suite.addTest(new JavaConventionTests("testValidUnicodePackageName"));
	
	return suite;
}
/**
 * @see JavaConventions
 */
public void testInvalidIdentifier() {
	String[] invalidIds = new String[] {"", "1java", "Foo Bar", "#@$!", "Foo-Bar", "if", "InvalidEscapeSequence\\g", "true", "false", "null", null, " untrimmmed "};
	for (int i = 0; i < invalidIds.length; i++) {
		assertTrue("identifier not recognized as invalid: " + invalidIds[i], !JavaConventions.validateIdentifier(invalidIds[i]).isOK());
	}
}
/**
 * @see JavaNamingConventions
 */
public void testInvalidImportDeclaration1() {
	assertTrue("import not reconized as invalid; java.math.", !JavaConventions.validateImportDeclaration("java.math.").isOK());
}
/**
 * @see JavaNamingConventions
 */
public void testInvalidImportDeclaration2() {
	assertTrue("import not reconized as invalid; java.math*", !JavaConventions.validateImportDeclaration("java.math*").isOK());
}
/**
 * @see JavaNamingConventions
 */
public void testInvalidImportDeclaration3() {
	assertTrue("import not reconized as invalid; empty string", !JavaConventions.validateImportDeclaration("").isOK());
}
/**
 * Test for package fragment root overlap
 */
public void testPackageFragmentRootOverlap() throws Exception {
	try {
		IJavaProject project = this.createJavaProject("P1", new String[] {"src"}, new String[] {"/P1/jclMin.jar"}, "bin");
		this.copy(new java.io.File(getExternalJCLPath()), new java.io.File(getWorkspaceRoot().getLocation().toOSString() + java.io.File.separator + "P1" + java.io.File.separator + "jclMin.jar"));
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
		assertTrue("compilation unit name not recognized as invalid: " + invalidNames[i], !JavaConventions.validateCompilationUnitName(invalidNames[i]).isOK());
	}
	String[] validNames = new String[] {"Object.JaVa"};
	for (int i = 0; i < validNames.length; i++) {
		assertTrue("compilation unit name not recognized as valid: " + validNames[i], JavaConventions.validateCompilationUnitName(validNames[i]).isOK());
	}
}
/**
 * @see JavaNamingConventions
 */
public void testValidFieldName() {
	assertTrue("unicode field name not handled", JavaConventions.validateFieldName("s\\u0069ze").isOK());
}
/**
 * @see JavaConventions
 */
public void testValidIdentifier() {
	String[] validIds = new String[] {"s\\u0069ze", "Object", "An_Extremly_Long_Class_Name_With_Words_Separated_By_Undescores"};
	for (int i = 0; i < validIds.length; i++) {
		assertTrue("identifier not recognized as valid: " + validIds[i], JavaConventions.validateIdentifier(validIds[i]).isOK());
	}
}
/**
 * @see JavaConventions
 */
public void testValidImportDeclaration() {
	assertTrue("import not reconized as valid", JavaConventions.validateImportDeclaration("java.math.*").isOK());
}
/**
 * @see JavaNamingConventions
 */
public void testValidMethodName() {
	assertTrue("unicode method name not handled", JavaConventions.validateMethodName("getSiz\\u0065").isOK());
}
/**
 * @see JavaNamingConventions
 */
public void testValidPackageName() {
	
	String pkgName= "org.eclipse.jdt.core.t\\u0065sts.MyPackage";
	assertTrue("unicode package name not handled", JavaConventions.validatePackageName(pkgName).isOK());

	assertTrue("package name not recognized as invalid1", !JavaConventions.validatePackageName("").isOK());
	assertTrue("package name not recognized as valid1", JavaConventions.validatePackageName("java . lang").isOK());
	assertTrue("package name not recognized as invalid2", !JavaConventions.validatePackageName("   java . lang").isOK());
	assertTrue("package name not recognized as invalid3", !JavaConventions.validatePackageName("java . lang  ").isOK());
	assertTrue("package name not recognized as invalid4", !JavaConventions.validatePackageName(null).isOK());
	assertTrue("package name not recognized as unconventional1", JavaConventions.validatePackageName("Java.lang").getSeverity() == IStatus.WARNING);
	assertTrue("package name not recognized as valid2", JavaConventions.validatePackageName("java.Lang").isOK());
}
/**
 * @see JavaNamingConventions
 */
public void testValidTypeName() {
	// regression tests for 1G5HVPB: ITPJCORE:WINNT - validateJavaTypeName accepts type names ending with \
	assertTrue("type name should not contain slashes (1)", JavaConventions.validateJavaTypeName("Object\\").getSeverity() == IStatus.ERROR);
	assertTrue("type name should not contain slashes (2)", JavaConventions.validateJavaTypeName("Object/").getSeverity() == IStatus.ERROR);
	assertTrue("type name should not contain slashes (3)", JavaConventions.validateJavaTypeName("\\Object").getSeverity() == IStatus.ERROR);
	assertTrue("type name should not contain slashes (4)", JavaConventions.validateJavaTypeName("java\\lang\\Object").getSeverity() == IStatus.ERROR);

	// regression test for 1G52ZIF: ITPJUI:WINNT - Wizards should strongly discourage the use of non-standard names
	assertTrue("discouraged type names not handled", JavaConventions.validateJavaTypeName("alowercasetypename").getSeverity() == IStatus.WARNING);

	// other tests
	assertTrue("unicode type name not handled", JavaConventions.validateJavaTypeName("P\\u0065a").getSeverity() == IStatus.OK);
	assertTrue("qualified type names not handled", JavaConventions.validateJavaTypeName("java  .  lang\t.Object").getSeverity() == IStatus.OK);
	assertTrue("simple qualified type names not handled", JavaConventions.validateJavaTypeName("java.lang.Object").getSeverity() == IStatus.OK);
	assertTrue("simple type names not handled", JavaConventions.validateJavaTypeName("Object").getSeverity() == IStatus.OK);
	assertTrue("discouraged type names not handled", JavaConventions.validateJavaTypeName("Object$SubType").getSeverity() == IStatus.WARNING);
	assertTrue("invalid type name not recognized", JavaConventions.validateJavaTypeName("==?==").getSeverity() == IStatus.ERROR);
}
/**
 * @see JavaNamingConventions
 */
public void testValidUnicodeImportDeclaration() {
	
	String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
	assertTrue("import not reconized as valid", JavaConventions.validateImportDeclaration(pkgName).isOK());

}
/**
 * @see JavaNamingConventions
 */
public void testValidUnicodePackageName() {
	
	String pkgName= "com.\\u0069bm.jdt.core.tests.MyPackag\\u0065";
	assertTrue("unicode package name not handled", JavaConventions.validatePackageName(pkgName).isOK());
	assertTrue("Parameter modified", pkgName.equals("com.\\u0069bm.jdt.core.tests.MyPackag\\u0065"));

}
}

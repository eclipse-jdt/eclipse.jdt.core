/*******************************************************************************
 * Copyright (c) 2005, 2012 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    IBM Corporation - Added test for https://bugs.eclipse.org/bugs/show_bug.cgi?id=352949
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.env.EnvironmentFactory;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.generic.AbstractGenericProcessor;
import org.eclipse.jdt.apt.tests.annotations.generic.GenericFactory;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorDeclarationCodeExample;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.SourceMirrorCodeExample;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationMirror;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.AnnotationValue;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.SourcePosition;

import junit.framework.Test;
import junit.framework.TestSuite;

public class MirrorDeclarationTests extends APTTestBase {

	public MirrorDeclarationTests(final String name)
	{
		super(name);
	}

	public static Test suite()
	{
		return new TestSuite(MirrorDeclarationTests.class);
	}

	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String code = MirrorDeclarationCodeExample.CODE;
		env.addClass(srcRoot, MirrorDeclarationCodeExample.CODE_PACKAGE, MirrorDeclarationCodeExample.CODE_CLASS_NAME, code);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}

	public void testMirrorDeclaration() throws Exception
	{
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}

	public void testFieldConstant() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String codeTrigger =
			"package test;\n" +
			"public @interface Trigger{}";

		env.addClass(srcRoot, "test", "Trigger", codeTrigger);

		String codeEntryPoint = "package test;\n" +
								"@Trigger\n" +
								"public class EntryPoint {\n" +
								"    ClassWithNestedAnnotation nestedAnno;\n}";

		env.addClass(srcRoot, "test", "EntryPoint", codeEntryPoint);

		String codeClassWithNestedAnnotation =
			"package test; \n" +
			"public class ClassWithNestedAnnotation {\n" +
			"	public final int FOUR = 4; \n " +
			"}";

		env.addClass(srcRoot, "test", "ClassWithNestedAnnotation", codeClassWithNestedAnnotation);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}

	// TODO: Disabled due to Bugzilla 124388 -theodora
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=124388
	public void DISABLED_testDefault() throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String codeTrigger =
			"package test;\n" +
			"public @interface Trigger{}";

		env.addClass(srcRoot, "test", "Trigger", codeTrigger);

		String codeEntryPoint = "package test;\n" +
								"@Trigger\n" +
								"public class EntryPoint {\n" +
								"    ClassWithNestedAnnotation nestedAnno;\n}";

		env.addClass(srcRoot, "test", "EntryPoint", codeEntryPoint);

		String codeClassWithNestedAnnotation =
			"package test; \n" +
			"public class ClassWithNestedAnnotation {\n" +
			"	public @interface NestedAnnotation{\n" +
			"		public enum Character{ \n" +
			"			Winnie, Tiger, Piglet, Eore; \n" +
			"		}\n"+
			"		Character value() default Character.Eore; \n" +
			"	}\n" +
			"}";

		env.addClass(srcRoot, "test", "ClassWithNestedAnnotation", codeClassWithNestedAnnotation);
		fullBuild( project.getFullPath() );
		expectingNoProblems();
	}

	/**
	 * Test AST based mirror implementation and binding based mirror implementation.
	 * Specifically,
	 *   (i) method declaration with unresolvable return type.
	 *  (ii) constructor declaration with unresolvable parameter
	 * (iii) field declaration with unresolvable type.
	 *
	 * This test focus on declarations from file in context.
	 *
	 * @throws Exception
	 */
	public void testUnresolvableDeclarations0()
		throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String declAnno =
			"package test;\n" +
			"public @interface DeclarationAnno{}";

		env.addClass(srcRoot, "test", "DeclarationAnno", declAnno);

		String codeFoo =
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Foo {\n" +
			"    int field0;\n " +
			"    UnknownType field1;\n " +
			"    public Foo(UnknownType type){} \n" +
			"    public void voidMethod(){} \n " +
			"    public UnknownType getType(){}\n " +
			"    public class Inner{} \n" +
			"}";

		final IPath fooPath = env.addClass(srcRoot, "test", "Foo", codeFoo);
		fullBuild( project.getFullPath() );

		expectingOnlySpecificProblemsFor(fooPath, new ExpectedProblem[]{
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath)}
		);
	}

	/**
	 * Test AST based mirror implementation and binding based mirror implementation.
	 * Specifically,
	 *   (i) method declaration with unresolvable return type.
	 *  (ii) constructor declaration with unresolvable parameter
	 * (iii) field declaration with unresolvable type.
	 *
	 * This test focus on declarations from file outside of processor
	 * environment context.
	 *
	 * @throws Exception
	 */
	public void testUnresolvableDeclarations1()
		throws Exception
	{
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String declAnno =
			"package test;\n" +
			"public @interface DeclarationAnno{}";

		env.addClass(srcRoot, "test", "DeclarationAnno", declAnno);

		String codeBar =
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Bar {}";
		env.addClass(srcRoot, "test", "Bar", codeBar);

		String codeFoo =
			"package test;\n" +
			"@DeclarationAnno\n" +
			"public class Foo {\n" +
			"    int field0;\n " +
			"    UnknownType field1;\n " +
			"    public Foo(UnknownType type){} \n" +
			"    public void voidMethod(){} \n " +
			"    public UnknownType getType(){}\n " +
			"    public class Inner{} \n" +
			"}";

		final IPath fooPath = env.addClass(srcRoot, "test", "Foo", codeFoo);

		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemsFor(fooPath, new ExpectedProblem[]{
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath),
				new ExpectedProblem("", "UnknownType cannot be resolved to a type", fooPath)}
		);
	}

	public void testLocation() {

		TestLocationProc p = new TestLocationProc();
		GenericFactory.setProcessor(p);

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		String x =
			"package test;\n" +
			"import org.eclipse.jdt.apt.tests.annotations.generic.*;\n" +
			"@GenericAnnotation public class X {}";

		env.addClass(srcRoot, "test", "X", x);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue("Processor not invoked", p.called);
	}

	public void testSourceMirror() {

		TestSourceMirrorProc p = new TestSourceMirrorProc();
		GenericFactory.setProcessor(p);

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		env.addClass(srcRoot, SourceMirrorCodeExample.CODE_PACKAGE, SourceMirrorCodeExample.ANNO_CODE_CLASS_NAME, SourceMirrorCodeExample.ANNO_CODE);
		env.addClass(srcRoot, SourceMirrorCodeExample.CODE_PACKAGE, SourceMirrorCodeExample.CODE_CLASS_NAME, SourceMirrorCodeExample.CODE);

		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue("Processor not invoked", p.called);
	}

	public void testPackageInfo() {
		PackageInfoProc p = new PackageInfoProc();
		GenericFactory.setProcessor(p);

		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		IPath pkg = env.addPackage(srcRoot, "pkg");
		String contents = "@PkgAnnotation\n" +
						  "@GenericAnnotation\n" +
						  "package pkg;\n" +
						  "import org.eclipse.jdt.apt.tests.annotations.generic.*;";
		env.addFile(pkg, "package-info.java", contents);
		String annContents =
				"package pkg;\n" +
				"\n" +
				"@interface PkgAnnotation {\n" +
				"    String value() default \"def\";\n" +
				"}\n";
		env.addClass(srcRoot, "pkg", "pkgAnnotation", annContents);
		fullBuild( project.getFullPath() );
		expectingNoProblems();

		assertTrue("Processor not invoked", p.called);
	}

	static class TestLocationProc extends AbstractGenericProcessor {

		boolean called;

		@Override
		public void _process() {
			called = true;
			assertEquals(1, decls.size());

			Declaration d = decls.iterator().next();
			SourcePosition p = d.getPosition();

			assertEquals(32, p.column());
			assertEquals(3, p.line());
		}
	}

	static class TestSourceMirrorProc extends AbstractGenericProcessor {

		boolean called;

		@Override
		public void _process() {
			called = true;
			TypeDeclaration tdCode = env.getTypeDeclaration(SourceMirrorCodeExample.CODE_PACKAGE + "." + SourceMirrorCodeExample.CODE_CLASS_NAME);
			for (AnnotationMirror am : tdCode.getAnnotationMirrors()) {
				if ("GenericAnnotation".equals(am.getAnnotationType().getDeclaration().getSimpleName())) {
					continue;
				}
				assertNotNull(am.getPosition());
				AnnotationTypeDeclaration atd = am.getAnnotationType().getDeclaration();
				assertNotNull(atd.getPosition());
				for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : am.getElementValues().entrySet()) {
					assertNotNull(entry.getKey().getPosition());
					assertNotNull(entry.getKey().getDefaultValue().getPosition());
				}
			}
		}
	}

	static class PackageInfoProc extends AbstractGenericProcessor {

		boolean called;

		@Override
		public void _process() {
			called = true;
			AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)env.getTypeDeclaration("pkg.PkgAnnotation");
			assertNotNull("Type declaration 'pkg.PkgAnnotation' not found!", annoDecl);
			// get the annotated declarations
			Collection<Declaration> annotatedDecls = env.getDeclarationsAnnotatedWith(annoDecl);
			// don't return the package declaration - well, apt is doing that..
			assertTrue(annotatedDecls == null  || annotatedDecls.size() == 0);

			//TODO: test to support package annotation
			// PackageDeclaration pdCode = env.getPackage("pkg");
			// for (AnnotationMirror am : pdCode.getAnnotationMirrors()) {
			//	if ("GenericAnnotation".equals(am.getAnnotationType().getDeclaration().getSimpleName())) {
			//		continue;
			//	}
			//	assertTrue(null != am.getPosition());
			//	AnnotationTypeDeclaration atd = am.getAnnotationType().getDeclaration();
			//	assertTrue(null != atd.getPosition());
			//	for (Map.Entry<AnnotationTypeElementDeclaration, AnnotationValue> entry : am.getElementValues().entrySet()) {
			//		assertNotNull(entry.getKey().getPosition());
			//		assertNotNull(entry.getKey().getDefaultValue().getPosition());
			//	}
			// }
		}
	}

	public void testEnvFactory() throws JavaModelException {

		IProject project = env.getProject(getProjectName());
		IPath srcRoot = getSourcePath();
		String x = "package test;\n" + "import org.eclipse.jdt.apt.tests.annotations.generic.*;\n"
				+ "@GenericAnnotation public class X {}";

		IPath path = env.addClass(srcRoot, "test", "X", x);
		IPath tail = path.removeFirstSegments(2);
		IJavaProject p = JavaCore.create(project);
		ICompilationUnit cu = (ICompilationUnit) p.findElement(tail);
		assertNotNull("Could not find cu", cu);

		AnnotationProcessorEnvironment env = EnvironmentFactory.getEnvironment(cu, p);
		TypeDeclaration t = env.getTypeDeclaration("test.X");

		SourcePosition pos = t.getPosition();

		assertEquals(32, pos.column());
		assertEquals(3, pos.line());

		// Bad type should gracefully return null
		TypeDeclaration tBad = env.getTypeDeclaration("test.XYZ");
		assertNull(tBad);
	}

}

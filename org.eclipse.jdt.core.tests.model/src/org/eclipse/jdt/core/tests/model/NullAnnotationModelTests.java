/*******************************************************************************
 * Copyright (c) 2011, 2020 GK Software AG and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMemberValuePair;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.tests.util.Util;
import org.osgi.framework.Bundle;

@SuppressWarnings("rawtypes")
public class NullAnnotationModelTests extends ReconcilerTests {

	String ANNOTATION_LIB;
	String ANNOTATION_LIB_V1;

	public static Test suite() {
		return buildModelTestSuite(NullAnnotationModelTests.class);
	}

	public NullAnnotationModelTests(String name) {
		super(name);
	}

	static {
//		TESTS_NAMES = new String[] { "testBug565246" };
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	@Override
	public void setUp() throws Exception {
		super.setUp();
		Bundle[] bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();

		bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[1.1.0,2.0.0)");
		bundleFile = FileLocator.getBundleFileLocation(bundles[0]).get();
		this.ANNOTATION_LIB_V1 = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();
	}

	protected String testJarPath(String jarName) throws IOException {
		URL libEntry = Platform.getBundle("org.eclipse.jdt.core.tests.model").getEntry("/workspace/NullAnnotations/lib/"+jarName);
		return FileLocator.toFileURL(libEntry).getPath();
	}

	public void testConvertedSourceType1() throws CoreException, InterruptedException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			this.createFolder("/P/p1");
			String c1SourceString =
				"package p1;\n" +
				"import org.eclipse.jdt.annotation.*;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C1 {\n" +
				"	 public String foo(@Nullable Object arg) {\n" + // this is consumed via SourceTypeConverter
				"		return arg == null ? \"\" : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p1/C1.java",
	    			c1SourceString);

			this.createFolder("/P/p2");
			String c2SourceString =
				"package p2;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C2 {\n" +
				"	 String bar(p1.C1 c, C2 c2) {;\n" +
				"        return c.foo(null);\n" + // don't complain despite default nonnull, foo has explicit @Nullable
				"    }\n" +
				"	 String foo(Object arg) {\n" +
				"		return arg == null ? null : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p2/C2.java",
	    			c2SourceString);

			char[] c2SourceChars = c2SourceString.toCharArray();
			this.problemRequestor.initialize(c2SourceChars);

			getCompilationUnit("/P/p2/C2.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems", "----------\n" +
					"1. WARNING in /P/p2/C2.java (at line 8)\n" +
					"	return arg == null ? null : arg.toString();\n" +
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Null type safety: The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
					"----------\n");
    	} finally {
    		deleteProject("P");
    	}
    }

	public void testBinaryType1() throws CoreException, InterruptedException, IOException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""},
											   new String[] {"JCL15_LIB", this.ANNOTATION_LIB, testJarPath("example.jar")},
											   "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			// example.jar contains p1/C1.java just like testConvertedSourceType1()

			this.createFolder("/P/p2");
			String c2SourceString =
				"package p2;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C2 {\n" +
				"	 String bar(p1.C1 c) {;\n" +
				"        return c.foo(null);\n" + // don't complain despite default nonnull, foo has explicit @Nullable
				"    }\n" +
				"	 String foo(Object arg) {\n" +
				"		return arg == null ? null : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p2/C2.java",
	    			c2SourceString);

			char[] c2SourceChars = c2SourceString.toCharArray();
			this.problemRequestor.initialize(c2SourceChars);

			getCompilationUnit("/P/p2/C2.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems", "----------\n" +
					"1. WARNING in /P/p2/C2.java (at line 8)\n" +
					"	return arg == null ? null : arg.toString();\n" +
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Null type safety: The expression of type 'String' needs unchecked conversion to conform to \'@NonNull String\'\n" +
					"----------\n");
    	} finally {
    		deleteProject("P");
    	}
    }

	// DISABLED: no longer a problem since bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
	public void _testMissingAnnotation1() throws CoreException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			p.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "in.valid");

			this.createFolder("/P/p1");
			String c1SourceString =
				"package p1;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C1 {\n" +
				"	 public String foo(Object arg) {\n" +
				"		return arg == null ? \"\" : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p1/C1.java",
	    			c1SourceString);

			this.problemRequestor.initialize(c1SourceString.toCharArray());

			getCompilationUnit("/P/p1/C1.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems",
					"----------\n" +
					"1. ERROR in /P/p1/C1.java (at line 1)\n" +
					"	package p1;\n" +
					"	^\n" +
					"Buildpath problem: the type in.valid, which is configured as a null annotation type, cannot be resolved\n" +
					"----------\n");
    	} finally {
    		deleteProject("P");
    	}
	}

	// DISABLED: no longer a problem since bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
	public void _testMissingAnnotation2() throws CoreException {
		Hashtable<String, String> javaOptions = JavaCore.getOptions();
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			IFile settings = (IFile) p.getProject().findMember(".settings/org.eclipse.jdt.core.prefs");
			settings.appendContents(new ByteArrayInputStream("\norg.eclipse.jdt.core.compiler.annotation.nonnull=not.valid\n".getBytes()), 0, null);
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			this.createFolder("/P/p1");
			String c1SourceString =
				"package p1;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C1 {\n" +
				"	 public String foo(Object arg) {\n" +
				"		return arg == null ? \"\" : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p1/C1.java",
	    			c1SourceString);

			this.problemRequestor.initialize(c1SourceString.toCharArray());

			getCompilationUnit("/P/p1/C1.java").getWorkingCopy(this.wcOwner, null);

			assertProblems("Unexpected problems",
					"----------\n" +
					"1. ERROR in /P/p1/C1.java (at line 1)\n" +
					"	package p1;\n" +
					"	^\n" +
					"Buildpath problem: the type not.valid, which is configured as a null annotation type, cannot be resolved\n" +
					"----------\n");
    	} finally {
    		deleteProject("P");
    		JavaCore.setOptions(javaOptions);
    		// work against side-effect of JavaRuntime listening to change of prefs-file.
    		// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=302850#c25
    	}
	}

	// Bug 363858 - [dom] early throwing of AbortCompilation causes NPE in CompilationUnitResolver
	// currently not actually challenging the NPE, because we no longer report
	// "Cannot use the unqualified name \'invalid\' as an annotation name for null specification"
	// DISABLED: no longer a problem since bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
	public void _testMissingAnnotation3() throws CoreException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			p.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "invalid");

			this.createFolder("/P/p1");
			String c1SourceString =
				"package p1;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C1 {\n" +
				"	 public String foo(Object arg) {\n" +
				"		return arg == null ? \"\" : arg.toString();\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p1/C1.java",
	    			c1SourceString);

			this.problemRequestor.initialize(c1SourceString.toCharArray());

			final ICompilationUnit unit = getCompilationUnit("/P/p1/C1.java").getWorkingCopy(this.wcOwner, null);
			assertProblems("Unexpected problems",
					"----------\n" +
					"1. ERROR in /P/p1/C1.java (at line 1)\n" +
					"	package p1;\n" +
					"	^\n" +
					"Buildpath problem: the type invalid, which is configured as a null annotation type, cannot be resolved\n" +
					"----------\n");

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
			parser.setProject(p);
			parser.setResolveBindings(true);
			parser.setSource(unit);
			CompilationUnit ast = (CompilationUnit) parser.createAST(null);
			assertNotNull("ast should not be null", ast);
			this.problemRequestor.reset();
			this.problemRequestor.beginReporting();
			IProblem[] problems = ast.getProblems();
			for (int i=0; i<problems.length; i++)
				this.problemRequestor.acceptProblem(problems[i]);
			assertProblems("Unexpected problems (2)",
					"----------\n" +
					"1. ERROR in /P/p1/C1.java (at line 1)\n" +
					"	package p1;\n" +
					"	^\n" +
					"Buildpath problem: the type invalid, which is configured as a null annotation type, cannot be resolved\n" +
					"----------\n");
    	} finally {
    		deleteProject("P");
    	}
	}

	// initialization of null annotations is triggered from package-info.java: illegal simple name
	public void testMissingAnnotation4() throws CoreException, InterruptedException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			p.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "invalid");

			this.createFolder("/P/p1");
			String piSourceString =
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"package p1;\n";
			this.createFile(
				"/P/p1/package-info.java",
	    			piSourceString);

			this.problemRequestor.initialize(piSourceString.toCharArray());

			// Challenge CompilationUnitProblemFinder:
			final ICompilationUnit unit = getCompilationUnit("/P/p1/package-info.java").getWorkingCopy(this.wcOwner, null);
// This error is not raised currently:
//			String expectedError = "----------\n" +
//								   "1. ERROR in /P/p1/package-info.java (at line 0)\n" +
//								   "	@org.eclipse.jdt.annotation.NonNullByDefault\n" +
//								   "	^\n" +
//								   "Cannot use the unqualified name \'invalid\' as an annotation name for null specification\n" +
//								   "----------\n";
//			assertProblems("Unexpected problems from CompilationUnitProblemFinder", expectedError);
			assertNoProblem(unit.getBuffer().getCharacters(), unit);

			// Challenge JavaBuilder:
			p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
//			assertMarkers("Unexpected markers",
//					"Cannot use the unqualified name 'invalid' as an annotation name for null specification",
//					markers);
//			assertEquals("Unexpected marker path", "/P", markers[0].getResource().getFullPath().toString());
			assertEquals("Should have no markers", 0, markers.length);

			// Challenge CompilationUnitResolver:
			ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
			parser.setProject(p);
			parser.setResolveBindings(true);
			parser.setSource(unit);
			CompilationUnit ast = (CompilationUnit) parser.createAST(null);
			assertNotNull("ast should not be null", ast);
//			this.problemRequestor.reset();
//			this.problemRequestor.beginReporting();
//			IProblem[] problems = ast.getProblems();
//			for (int i=0; i<problems.length; i++)
//				this.problemRequestor.acceptProblem(problems[i]);
//			assertProblems("Unexpected problems from CompilationUnitResolver", expectedError);
			assertEquals("Should have no problems", 0, ast.getProblems().length);
    	} finally {
    		deleteProject("P");
    	}
	}

	// initialization of null annotations is
	// - triggered from resolveTypesFor(MethodBinding)
	// - default is defined in package-info.java:
	// must detect missing non-null annotation and report against the project
	// DISABLED: no longer a problem since bug 365531 - [compiler][null] investigate alternative strategy for internally encoding nullness defaults
	public void _testMissingAnnotation5() throws CoreException, InterruptedException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			p.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "pack.Missing");

			this.createFolder("/P/p1");
			String piSourceString =
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"package p1;\n";
			this.createFile("/P/p1/package-info.java", piSourceString);

			String c1SourceString =
				"package p1;\n" +
				"public class C1 {\n" +
				"    String foo(String arg) { return arg; }\n" +
				"}\n";
			this.createFile("/P/p1/C1.java", c1SourceString);

			this.problemRequestor.initialize(piSourceString.toCharArray());

			// Challenge CompilationUnitProblemFinder:
			assertNoProblem(piSourceString.toCharArray(), getCompilationUnit("/P/p1/package-info.java"));

			this.problemRequestor.initialize(c1SourceString.toCharArray());

			// Challenge CompilationUnitProblemFinder:
			ICompilationUnit unit = getCompilationUnit("/P/p1/C1.java").getWorkingCopy(this.wcOwner, null);
			String expectedError = "----------\n" +
								   "1. ERROR in /P/p1/C1.java (at line 1)\n" +
								   "	package p1;\n" +
								   "	^\n" +
								   "Buildpath problem: the type pack.Missing, which is configured as a null annotation type, cannot be resolved\n" +
								   "----------\n";
			assertProblems("Unexpected problems", expectedError);

			// Challenge JavaBuilder:
			p.getProject().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = p.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers",
					"Buildpath problem: the type pack.Missing, which is configured as a null annotation type, cannot be resolved",
					markers);
// toggle expectation depending on how CAT_BUILDPATH problems are reported (see AbstractImageBuilder.storeProblemsFor(..))
//			assertEquals("Unexpected marker path", "/P", markers[0].getResource().getFullPath().toString());
			assertEquals("Unexpected marker path", "/P/p1/C1.java", markers[0].getResource().getFullPath().toString());

			// Challenge CompilationUnitResolver:
			ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
			parser.setProject(p);
			parser.setResolveBindings(true);
			parser.setSource(unit);
			CompilationUnit ast = (CompilationUnit) parser.createAST(null);
			assertNotNull("ast should not be null", ast);
			this.problemRequestor.reset();
			this.problemRequestor.beginReporting();
			IProblem[] problems = ast.getProblems();
			for (int i=0; i<problems.length; i++)
				this.problemRequestor.acceptProblem(problems[i]);
			assertProblems("Unexpected problems (2)", expectedError);
    	} finally {
    		deleteProject("P");
    	}
	}

	// A synthetic annotation from a default should not be converted to DOM AST
	public void testAnnotationAST1() throws CoreException, InterruptedException {
    	try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			this.createFolder("/P/p1");
			// bug could only be triggered if ASTConvert actually finds a '@'
			// so in addition to the synthetic annotation there must also be a real one:
			String annotSourceString =
				"package p1;\n" +
				"import java.lang.annotation.ElementType;\n" +
				"import java.lang.annotation.Target;\n" +
				"@Target({ElementType.PARAMETER,ElementType.METHOD})\n" +
				"public @interface Annot {}\n";
			this.createFile(
				"/P/p1/Annot.java",
	    			annotSourceString);
			String c1SourceString =
				"package p1;\n" +
				"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
				"public class C1 {\n" +
				"	 public @Annot Object foo(@Annot Object arg) {\n" +
				"         return this;\n" +
				"	 }\n" +
				"}\n";
			this.createFile(
				"/P/p1/C1.java",
	    			c1SourceString);

			this.problemRequestor.initialize(c1SourceString.toCharArray());

			final ICompilationUnit unit = getCompilationUnit("/P/p1/C1.java").getWorkingCopy(this.wcOwner, null);
			assertNoProblem(c1SourceString.toCharArray(), unit);

			ASTParser parser = ASTParser.newParser(AST_INTERNAL_LATEST);
			parser.setProject(p);
			parser.setResolveBindings(true);
			parser.setSource(unit);
			CompilationUnit ast = (CompilationUnit) parser.createAST(null);
			assertNotNull("ast should not be null", ast);
			TypeDeclaration type = (TypeDeclaration) ast.types().get(0);
			assertNotNull("type should not be null", type);
			MethodDeclaration method = (MethodDeclaration) type.bodyDeclarations().get(0);
			assertNotNull("method should not be null", method);
			SingleVariableDeclaration arg = (SingleVariableDeclaration) method.parameters().get(0);
			assertNotNull("argument should not be null", arg);
			List modifiers = arg.modifiers();
			assertEquals("Should have exactly one modifier", 1, modifiers.size());
			assertEquals("Unexpected modifier", "@Annot", ((MarkerAnnotation)modifiers.get(0)).toString());
			modifiers = method.modifiers();
			assertEquals("Method should have exactly two modifiers", 2, modifiers.size());
			assertEquals("Unexpected modifier #1 for method", "public", ((Modifier)modifiers.get(0)).toString());
			assertEquals("Unexpected modifier #2 for method", "@Annot", ((MarkerAnnotation)modifiers.get(1)).toString());
    	} finally {
    		deleteProject("P");
    	}
	}
	// https://bugs.eclipse.org/bugs/show_bug.cgi?id=372012
	// no problem should be created for a compilation unit in a package missing package-info when the warning is enabled
	public void testBug372012() throws JavaModelException, IOException, CoreException, InterruptedException {
		try {
			// Resources creation
			IJavaProject p = createJavaProject("P", new String[] {""}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			p.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			p.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "in.valid");
			p.setOption(JavaCore.COMPILER_PB_MISSING_NONNULL_BY_DEFAULT_ANNOTATION, JavaCore.ERROR);

			this.createFolder("/P/p1");
			String c1SourceString =
				"package p1;\n" +
				"public class C1 {\n" +
				"	 public String foo(Object arg) {\n" +
				"		return arg == null ? \"\" : arg.toString();\n" +
				"	 }\n" +
				"}\n";

			assertNoProblem(c1SourceString.toCharArray(), getCompilationUnit("/P/p1/C1.java"));
    	} finally {
    		deleteProject("P");
    	}
	}

	// see https://bugs.eclipse.org/418233
	public void testNonNullDefaultInInner()  throws CoreException, IOException, InterruptedException  {
		IJavaProject project15 = null;
		try {
			project15 = createJavaProject("TestAnnot", new String[] {"src"}, new String[] {"JCL15_LIB", this.ANNOTATION_LIB}, "bin", "1.5");
			createFolder("/TestAnnot/src/p1");
			createFile(
					"/TestAnnot/src/p1/Interfaces.java",
					"package p1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"@NonNullByDefault\n" +
					"public interface Interfaces {\n" +
					"  public interface InnerInterface {\n" +
					"    Object doSomethingElse(Object o);\n" +
					"  }\n" +
					"}"
				);
			String source =
					"package p1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"\n" +
					"@NonNullByDefault\n" +
					"public class Implementations implements Interfaces.InnerInterface {\n" +
					"	public Object doSomethingElse(Object o) {\n" +
					"		return o; \n" +
					"	}\n" +
					"}";
			createFile(
					"/TestAnnot/src/p1/Implementations.java",
					source
				);
			project15.setOption(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_5);
			project15.setOption(JavaCore.COMPILER_COMPLIANCE, JavaCore.VERSION_1_5);
			project15.setOption(JavaCore.COMPILER_PB_NULL_REFERENCE, JavaCore.ERROR);
			project15.setOption(JavaCore.COMPILER_PB_POTENTIAL_NULL_REFERENCE, JavaCore.ERROR);
			project15.setOption(JavaCore.COMPILER_PB_REDUNDANT_NULL_CHECK, JavaCore.ERROR);
			project15.setOption(JavaCore.COMPILER_PB_INCLUDE_ASSERTS_IN_NULL_ANALYSIS, JavaCore.ENABLED);
			project15.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			this.workingCopies = new ICompilationUnit[1];
			char[] sourceChars = source.toCharArray();
			this.problemRequestor.initialize(sourceChars);
			this.workingCopies[0] = getCompilationUnit("/TestAnnot/src/p1/Implementations.java").getWorkingCopy(this.wcOwner, null);
			this.workingCopies[0].makeConsistent(null);
			this.workingCopies[0].reconcile(ICompilationUnit.NO_AST, false, null, null);

			assertNoProblem(sourceChars, this.workingCopies[0]);
		} finally {
			if (project15 != null)
				deleteProject(project15);
		}
	}
	/*
	 * Bug 405843 - [1.8] Support type annotations in Java Model(https://bugs.eclipse.org/bugs/show_bug.cgi?id=405843)
	 */
	public void testBug405843() throws CoreException, IOException, InterruptedException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug405843", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			createFolder("/Bug405843/src/p1");
			createFile("/Bug405843/src/p1/Function.java",
					"package p1;\n" +
					"public interface Function <I, O> {\n" +
					"}\n;");

			createFile("/Bug405843/src/p1/FunctionImpl.java",
					"package p1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class FunctionImpl implements Function<@NonNull String, @Nullable Object> {\n" +
					"}\n");

			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			setUpWorkingCopy("/Bug405843/src/p1/X.java",
					"package p1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class X {\n" +
					"	public Object foo() {\n" +
					"		Function<@NonNull String, @Nullable Object> impl = new FunctionImpl();\n" +
					"		return impl;\n" +
					"	}\n" +
					"}\n");
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n"
					);

		} finally {
			if (project != null)
				deleteProject(project);
		}
	}
	public void testBug405843a() throws CoreException, IOException, InterruptedException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug405843", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			createFolder("/Bug405843/src/p1");
			createFile("/Bug405843/src/p1/Y.java",
					"package p1;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public class Y {\n" +
					"    void foo(@NonNull String @NonNull [] array) {}\n" +
					"}\n;");

			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			setUpWorkingCopy("/Bug405843/src/p1/X.java",
					"package p1;\n" +
					"public class X {\n" +
					"	public void foo(Y y) {\n" +
					"		y.foo(null);\n" +
					"	}\n" +
					"}\n");
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /Bug405843/src/p1/X.java (at line 4)\n" +
					"	y.foo(null);\n" +
					"	      ^^^^\n" +
					"Null type mismatch: required \'@NonNull String @NonNull[]\' but the provided value is null\n" +
					"----------\n"
					);

		} finally {
			if (project != null)
				deleteProject(project);
		}
	}

	// was: NPE in ProblemReporter.illegalReturnRedefinition() from ImplicitNullAnnotationVerifier.checkNullSpecInheritance()
	public void testBug458361a() throws CoreException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug458361", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");

			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			project.setOption(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);

			setUpWorkingCopy("/Bug458361/src/MyCollection.java",
					"import java.util.Collection;\n" +
					"import org.eclipse.jdt.annotation.*;\n" +
					"public interface MyCollection<T> extends Collection<T> {\n" +
					"    public @Nullable T get(int i);\n" +
					"}\n");
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /Bug458361/src/MyCollection.java (at line 4)\n" +
					"	public @Nullable T get(int i);\n" +
					"	       ^^^^^^^^^^^\n" +
					"The return type is incompatible with the free type variable \'T\' returned from Collection<T>.get(int) (mismatching null constraints)\n" +
					"----------\n");
		} finally {
			if (project != null)
				deleteProject(project);
		}
	}
	public void testBug458361b() throws CoreException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug458361", new String[] {"src"}, new String[] {"JCL17_LIB", this.ANNOTATION_LIB_V1}, "bin", "1.7");

			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			project.setOption(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);

			createFile("/Bug458361/src/Super.java",
					"import org.eclipse.jdt.annotation.*;\n" +
					"public interface Super {\n" +
					"	@NonNull String getName();\n" +
					"}\n");

			setUpWorkingCopy("/Bug458361/src/Sub.java",
					"import org.eclipse.jdt.annotation.*;\n" +
					"public interface Sub extends Super {\n" +
					"    @Nullable String getName();\n" +
					"}\n");
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /Bug458361/src/Sub.java (at line 3)\n" +
					"	@Nullable String getName();\n" +
					"	^^^^^^^^^^^^^^^^\n" +
					"The return type is incompatible with \'@NonNull String\' returned from Super.getName() (mismatching null constraints)\n" +
					"----------\n");
		} finally {
			if (project != null)
				deleteProject(project);
		}
	}

	public void testBug487781() throws CoreException, IOException, InterruptedException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug487781", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			project.setOption(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);
			createFolder("/Bug487781/src/test");

			createFile("/Bug487781/src/test/Util.java",
					"package test;\n" +
					"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
					"import org.eclipse.jdt.annotation.Nullable;\n" +
					"class A<T> {}\n" +
					"interface I {}\n" +
					"@NonNullByDefault\n" +
					"class Util2<T extends @Nullable I> {}\n" +
					"@NonNullByDefault\n" +
					"public class Util {\n" +
					"	public static <T extends @Nullable I> void uniqueMapOfUniqueable(A<T> set) {\n" +
					"	}\n" +
					"}\n");


			setUpWorkingCopy("/Bug487781/src/test/Usage.java",
					"package test;\n" +
					"import org.eclipse.jdt.annotation.Nullable;\n" +
					"public class Usage {\n" +
					"	public void f() {\n" +
					"		Util.uniqueMapOfUniqueable(new A<@Nullable I>());\n" +
					"		new Util2<@Nullable I>();\n" +
					"	}\n" +
					"}\n;");
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n"
					);

		} finally {
			if (project != null)
				deleteProject(project);
		}
	}

	public void testBug495635() throws CoreException, IOException, InterruptedException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug495635", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			project.setOption(JavaCore.COMPILER_PB_NULL_SPECIFICATION_VIOLATION, JavaCore.ERROR);

			createFile("/Bug495635/src/AURegObject.java",
					"public interface AURegObject {}\n"
					);
			createFile("/Bug495635/src/AURegKey.java",
					"public interface AURegKey<O extends AURegObject> {}\n"
					);
			createFile("/Bug495635/src/Person.java",
					"public interface Person<O extends Person<O>> extends AURegObject, PersonKey<O> {}\n"
				);
			createFile("/Bug495635/src/PersonKey.java",
					"public interface PersonKey<O extends Person<?>> extends AURegKey<O> {}\n"
					);

			setUpWorkingCopy("/Bug495635/src/Person.java",
					"public interface Person<O extends Person<O>> extends AURegObject, PersonKey<O> {}\n"
				);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n"
					);

			String str = this.workingCopy.getSource();

			int start = str.indexOf("PersonKey");
			int length = "PersonKey".length();

			IJavaElement[] elements = this.workingCopy.codeSelect(start, length);
			assertElementsEqual(
				"Unexpected elements",
				"PersonKey [in PersonKey.java [in <default> [in src [in Bug495635]]]]",
				elements
			);

		} finally {
			if (project != null)
				deleteProject(project);
		}
	}

	public void testBug460491WithOldBinary() throws CoreException, InterruptedException, IOException {
		IJavaProject project = null;
    	try {
			project = createJavaProject("Bug460491", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB, testJarPath("bug460491-compiled-with-4.6.jar")}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			// bug460491-compiled-with-4.6.jar contains classes compiled with eclipse 4.6:
			/*-
				package test1;

				import org.eclipse.jdt.annotation.DefaultLocation;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;

				public abstract class Base<B> {
				   static public class Static {
				    public class Middle1 {
				     public class Middle2<M> {
				       public class Middle3 {
				        public class GenericInner<T> {
				        }
				       }
				     }
				   }
				  }

				  @NonNullByDefault(DefaultLocation.PARAMETER)
				  public Object method( Static.Middle1.Middle2<Object>.Middle3.@Nullable GenericInner<String> nullable) {
				    return new Object();
				  }
				}
			 */

			this.createFolder("/Bug460491/src/test2");
			String c2SourceString =
					"package test2;\n" +
					"\n" +
					"import test1.Base;\n" +
					"\n" +
					"class Derived extends Base<Object> {\n" +
					"  void test() {\n" +
					"    method(null);\n" +
					"  }\n" +
					"}\n";
			this.createFile(
				"/Bug460491/src/test2/Derived.java",
	    			c2SourceString);

			char[] c2SourceChars = c2SourceString.toCharArray();
			this.problemRequestor.initialize(c2SourceChars);

			getCompilationUnit("/Bug460491/src/test2/Derived.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"----------\n"
					);
		} finally {
			if (project != null)
				deleteProject(project);
		}
}

	public void testBug460491WithOldBinary_b() throws CoreException, InterruptedException, IOException {
		IJavaProject project = null;
    	try {
			project = createJavaProject("Bug460491", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB, testJarPath("bug460491b-compiled-with-4.6.jar")}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			// bug460491b-compiled-with-4.6.jar contains classes compiled with eclipse 4.6:
			/*-
				package test1;

				import org.eclipse.jdt.annotation.DefaultLocation;
				import org.eclipse.jdt.annotation.NonNullByDefault;
				import org.eclipse.jdt.annotation.Nullable;

				public abstract class Base<B> {
				    static public class Static1 {
				        public static class Static2<X> {
				            public class Middle1<M> {
				                public class Middle2 {
				                    public class GenericInner<T> {
				                    }
				                }
				            }
				        }
				    }

				    @NonNullByDefault(DefaultLocation.PARAMETER)
				    public Object method( Static1.Static2<Exception>.Middle1<Object>.Middle2.@Nullable GenericInner<String> nullable) {
				        return new Object();
				    }
				}
			 */

			this.createFolder("/Bug460491/src/test2");
			String c2SourceString =
					"package test2;\n" +
					"\n" +
					"import test1.Base;\n" +
					"\n" +
					"class Derived extends Base<Object> {\n" +
					"  void testOK(Static1.Static2<Exception>.Middle1<Object>.Middle2.GenericInner<String> gi) {\n" +
					"    method(gi);\n" +
					"  }\n" +
					"  void testNOK(Static1.Static2<String>.Middle1<Object>.Middle2.GenericInner<String> gi) {\n" +
					"    method(gi);\n" +
					"  }\n" +
					"}\n";
			this.createFile(
				"/Bug460491/src/test2/Derived.java",
	    			c2SourceString);

			char[] c2SourceChars = c2SourceString.toCharArray();
			this.problemRequestor.initialize(c2SourceChars);

			getCompilationUnit("/Bug460491/src/test2/Derived.java").getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /Bug460491/src/test2/Derived.java (at line 10)\n" +
					"	method(gi);\n" +
					"	^^^^^^\n" +
					"The method method(Base.Static1.Static2<Exception>.Middle1<Object>.Middle2.GenericInner<String>) in the type Base<Object> is not applicable for the arguments (Base.Static1.Static2<String>.Middle1<Object>.Middle2.GenericInner<String>)\n" +
					"----------\n"
					);
		} finally {
			if (project != null)
				deleteProject(project);
		}
	}

	public void testTargetTypeUse() throws CoreException {
		IJavaProject project = null;
    	try {
			project = createJavaProject("TargetTypeUse", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			IType nonNull = project.findType(NonNull.class.getName());
			IAnnotation annot = nonNull.getAnnotation(Target.class.getName());
			for (IMemberValuePair memberValuePair : annot.getMemberValuePairs()) {
				Object value = memberValuePair.getValue();
				if(value instanceof Object[]) {
					Object[] arr = (Object[]) value;
					for (Object object : arr) {
						if (object.equals(ElementType.class.getName()+'.'+ElementType.TYPE_USE)) {
							return;
						}
					}
				} else {
					if (value.equals(ElementType.class.getName()+'.'+ElementType.TYPE_USE))
						return;
				}
			}
			fail("TYPE_USE target not found");
		} finally {
			if (project != null)
				deleteProject(project);
		}
	}
	public void testBug543304() throws Exception {
		IJavaProject annotations = createJavaProject("Annotations", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
		IJavaProject client = createJavaProject("Client", new String[] {"src"}, new String[] {"JCL17_LIB"}, "bin", "1.7");
		try {
			createFolder("Annotations/src/p");
			createFile("Annotations/src/p/NonNull.java",
					"package p;\n" +
					"import java.lang.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target({METHOD, PARAMETER, FIELD, TYPE_USE})\n" +
					"public @interface NonNull {}\n");
			createFile("Annotations/src/p/Nullable.java",
					"package p;\n" +
					"import java.lang.annotation.*;\n" +
					"import static java.lang.annotation.ElementType.*;\n" +
					"@Target({METHOD, PARAMETER, FIELD, TYPE_USE})\n" +
					"public @interface Nullable {}\n");

			addClasspathEntry(client, JavaCore.newProjectEntry(annotations.getPath()));
			client.setOption(JavaCore.COMPILER_NONNULL_ANNOTATION_NAME, "p.NonNull");
			client.setOption(JavaCore.COMPILER_NULLABLE_ANNOTATION_NAME, "p.Nullable");
			client.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			createFile("Client/src/Test.java",
					"import p.*;\n" +
					"public class Test {\n" +
					"  @Nullable int[] ints = null;\n" +
					"  public @NonNull Object foo(@NonNull byte[] data) {\n" +
					"    return data;\n" +
					"  }\n" +
					"}\n");

			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);
			IMarker[] markers = client.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(annotations);
			deleteProject(client);
		}
	}
	public void testBug549764() throws CoreException, IOException {
		IJavaProject project = null;
    	try {
			project = createJavaProject("Bug549764", new String[] {"src"}, new String[] {"JCL18_LIB", this.ANNOTATION_LIB}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			IPath libpath = project.getProject().getLocation().append("libann.jar");
			Util.createJar(new String[] {
					"lib/MyGenerated.java",
					"package lib;\n" +
					"public @interface MyGenerated {\n" +
					"	String value();\n" +
					"}"},
					libpath.toOSString(),
					"1.8");
			addLibraryEntry(project, libpath, false);

			createFolder("Bug549764/src/nullAnalysis");
			createFile("Bug549764/src/nullAnalysis/package-info.java",
					"@org.eclipse.jdt.annotation.NonNullByDefault\n" +
					"package nullAnalysis;");
			String testSource =
					"package nullAnalysis;\n" +
					"\n" +
					"import org.eclipse.jdt.annotation.NonNull;\n" +
					"import lib.MyGenerated;\n" +
					"\n" +
					"@MyGenerated(Endpoint.COMMENT)\n" +
					"public class Endpoint {\n" +
					"\n" +
					"	public static final String COMMENT = \" comment\";\n" +
					"\n" +
					"	public void method() {\n" +
					"		format(COMMENT, \"\");\n" +
					"	}\n" +
					"	native void format(@NonNull String comment, String arg);\n" +
					"}\n";
			String testSourcePath = "Bug549764/src/nullAnalysis/Endpoint.java";
			createFile(testSourcePath, testSource);
			char[] testSourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(testSourceChars);

			getCompilationUnit(testSourcePath).getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. WARNING in /Bug549764/src/nullAnalysis/Endpoint.java (at line 14)\n" +
					"	native void format(@NonNull String comment, String arg);\n" +
					"	                   ^^^^^^^^^^^^^^^\n" +
					"The nullness annotation is redundant with a default that applies to this location\n" +
					"----------\n"
					);
    	} finally {
    		deleteProject(project);
    	}
	}

	// was: NPE in SourceTypeBinding.getAnnotationTagBits
	@SuppressWarnings("deprecation")
	public void testBug551426() throws CoreException, Exception {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<String, String> options = new HashMap<>();
		astParser.setResolveBindings(true);
		astParser.setEnvironment(new String[] {}, new String[] {}, new String[] {}, true);
		options.put(JavaCore.COMPILER_SOURCE, "1.8");
		options.put(JavaCore.COMPILER_COMPLIANCE, "1.8");
		astParser.setCompilerOptions(options);
		astParser.setUnitName("C.java");
		String source =
				"class C {\n" +
				"  public static final Object f = new Object() {};\n" +
				"}\n";
		astParser.setSource(source.toCharArray());
		CompilationUnit astNode = (CompilationUnit) astParser.createAST(null);
		AbstractTypeDeclaration typeDeclaration = (AbstractTypeDeclaration) astNode.types().get(0);
		FieldDeclaration fieldDeclaration = (FieldDeclaration) typeDeclaration.bodyDeclarations().get(0);
		VariableDeclarationFragment fragment = (VariableDeclarationFragment) fieldDeclaration.fragments().get(0);
		ITypeBinding typeBinding = fragment.getInitializer().resolveTypeBinding();
		IAnnotationBinding[] annotations = typeBinding.getAnnotations();
		assertEquals(0, annotations.length);
	}
	public void testBug479389() throws CoreException, IOException {
		IJavaProject project = null;
		try {
			project = createJavaProject("Bug479389", new String[] {"src"}, new String[] {"JCL18_LIB"}, "bin", "1.8");
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);

			createFolder("Bug479389/src/nullAnalysis");
			String testSource =
					"package nullAnalysis;\n" +
					"interface MyList<T> {\n" +
					"	public Stream<T> stream();\n" +
					"}\n" +
					"interface Stream<T> {\n" +
					"	T[] toArray(IntFunction<T[]> supplier);" +
					"}\n" +
					"interface IntFunction<T> {\n" +
					"	T apply(int i);\n" +
					"}\n" +
					"public class X {\n" +
					"\n" +
					"	public String[] method(MyList<String> in) {\n" +
					"		return in.stream().toArray(String[]::new);\n" +
					"	}\n" +
					"}\n";
			String testSourcePath = "Bug479389/src/nullAnalysis/X.java";
			createFile(testSourcePath, testSource);
			char[] testSourceChars = testSource.toCharArray();
			this.problemRequestor.initialize(testSourceChars);

			getCompilationUnit(testSourcePath).getWorkingCopy(this.wcOwner, null);
			assertProblems(
					"Unexpected problems",
					"----------\n" +
					"1. ERROR in /Bug479389/src/nullAnalysis/X.java (at line 13)\n" +
					"	return in.stream().toArray(String[]::new);\n" +
					"	       ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^\n" +
					"Annotation type \'org.eclipse.jdt.annotation.NonNull\' cannot be found on the build path, which is implicitly needed for null analysis\n" +
					"----------\n"					);
		} finally {
			deleteProject(project);
		}
	}
	public void testBug565246() throws CoreException {
		IJavaProject project = createJavaProject("Bug565246", new String[] {"src"}, new String[] {"JCL17_LIB", this.ANNOTATION_LIB_V1}, "bin", "1.7");
		try {
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			createFolder("Bug565246/src/java/util");
			createFile("Bug565246/src/java/util/Iterator.java",
					"package java.util;\n" +
					"import org.eclipse.jdt.annotation.NonNullByDefault;\n" +
					"\n" +
					"@NonNullByDefault\n" +
					"public interface Iterator<E> {\n" +
					"	boolean hasNext();\n" +
					"\n" +
					"	E next();\n" +
					"}\n");
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			String testSourcePath = "Bug565246/src/Test.java";
			createFile(testSourcePath,
					"import org.eclipse.jdt.annotation.NonNull;\n" +
					"import java.util.Collection;\n" +
					"public class Test {\n" +
					"	public void foo(Collection<String> list) {\n" +
					"		for (String s : list)\n" +
					"			bar(s);\n" +
					"	}\n" +
					"	void bar(@NonNull String s) {}\n" +
					"}\n");

			getCompilationUnit(testSourcePath).getWorkingCopy(this.wcOwner, null);
			assertProblems("", "----------\n----------\n");

			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(project);
		}
	}
	public void testGH875() throws CoreException, InterruptedException {
		// first fixed by commit da0a6d8d5088b92dbc3602cdccff8d667b6d5e8b
		IJavaProject project = createJavaProject("GH875", new String[] {"src"}, new String[] {"JCL17_LIB", this.ANNOTATION_LIB_V1}, "bin", "1.7");
		try {
			project.setOption(JavaCore.COMPILER_ANNOTATION_NULL_ANALYSIS, JavaCore.ENABLED);
			createFile("GH875/src/ICheckValidatorImpl.java",
					"public interface ICheckValidatorImpl { }\n");
			createFile("GH875/src/DefaultCheckImpl.java",
					"public class DefaultCheckImpl implements ICheckValidatorImpl { }\n");
			createFile("GH875/src/XCheck.java",
					"public class XCheck extends DefaultCheckImpl { }\n");
			createFile("GH875/src/DefaultCheckValidator.java",
					"public class DefaultCheckValidator { }\n");
			createFile("GH875/src/AcfCheckValidator.java",
					"public class AcfCheckValidator extends DefaultCheckValidator { }\n");
			createFile("GH875/src/CompilerCatalog.java",
					"""
					import java.lang.annotation.ElementType;
					import java.lang.annotation.Retention;
					import java.lang.annotation.RetentionPolicy;
					import java.lang.annotation.Target;

					@Retention(RetentionPolicy.RUNTIME)
					@Target({ElementType.TYPE})
					public @interface CompilerCatalog {
					  Class<? extends ICheckValidatorImpl>[] compilers();
					}
					""");
			String testSourcePath = "GH875/src/AvqBaseCheckValidator.java";
			String testContent =
					"""
					@CompilerCatalog(compilers = {XCheck.class})
					public class AvqBaseCheckValidator extends AcfCheckValidator { }
					""";
			createFile(testSourcePath, testContent);
			getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, null);

			ICompilationUnit unit = getCompilationUnit(testSourcePath).getWorkingCopy(this.wcOwner, null);
			assertNoProblem(testContent.toCharArray(), unit);

			getWorkspace().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, null);
			IMarker[] markers = project.getProject().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
			assertMarkers("Unexpected markers", "", markers);
		} finally {
			deleteProject(project);
		}
	}
}

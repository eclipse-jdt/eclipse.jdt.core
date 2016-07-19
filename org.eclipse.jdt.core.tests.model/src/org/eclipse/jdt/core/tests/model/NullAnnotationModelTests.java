/*******************************************************************************
 * Copyright (c) 2011, 2016 GK Software AG and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.net.URL;
import java.util.Hashtable;
import java.util.List;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
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
//		TESTS_NAMES = new String[] { "testConvertedSourceType1" };
	}

	/**
	 * @deprecated indirectly uses deprecated class PackageAdmin
	 */
	public void setUp() throws Exception {
		super.setUp();
		Bundle[] bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[2.0.0,3.0.0)");
		File bundleFile = FileLocator.getBundleFile(bundles[0]);
		this.ANNOTATION_LIB = bundleFile.isDirectory() ? bundleFile.getPath()+"/bin" : bundleFile.getPath();

		bundles = org.eclipse.jdt.core.tests.Activator.getPackageAdmin().getBundles("org.eclipse.jdt.annotation", "[1.1.0,2.0.0)");
		bundleFile = FileLocator.getBundleFile(bundles[0]);
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

			ASTParser parser = ASTParser.newParser(AST.JLS8);
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
			ASTParser parser = ASTParser.newParser(AST.JLS8);
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
			ASTParser parser = ASTParser.newParser(AST.JLS8);
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

			ASTParser parser = ASTParser.newParser(AST.JLS8);
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
}

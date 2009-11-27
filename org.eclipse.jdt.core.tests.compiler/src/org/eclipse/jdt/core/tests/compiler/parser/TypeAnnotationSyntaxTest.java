/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Locale;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.CompilerTestSetup;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.select.SelectionParser;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.DocumentElementParser;
import org.eclipse.jdt.internal.compiler.IDocumentElementRequestor;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

public class TypeAnnotationSyntaxTest extends AbstractCompilerTest implements IDocumentElementRequestor, ISourceElementRequestor {
	
	static final class LocationPrinterVisitor extends ASTVisitor {
		Annotation[] primaryAnnotations;
		TypeReference enclosingReference;

		public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
			Annotation[] annotations = fieldDeclaration.annotations;
			this.enclosingReference = fieldDeclaration.type;
			this.primaryAnnotations = annotations;
			return true;
		}

		public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				printLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation));
			}
			return false;
		}
		public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				printLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation));
			}
			return false;
		}
		public boolean visit(NormalAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				printLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation));
			}
			return false;
		}
		public void printLocations(Annotation annotation, int[] tab) {
			if (tab == null) {
				System.out.println(String.valueOf(annotation) + " no_locations");
				return;
			}
			StringBuffer buffer = new StringBuffer(String.valueOf(annotation));
			buffer.append(" {");
			for (int i = 0, max = tab.length; i < max; i++) {
				if (i > 0) {
					buffer.append(',');
				}
				buffer.append(tab[i]);
			}
			buffer.append('}');
			System.out.println(String.valueOf(buffer));
		}

		public boolean visit(ArrayTypeReference arrayReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(ParameterizedSingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
		public boolean visit(SingleTypeReference typeReference, BlockScope scope) {
			if (this.enclosingReference == null) return false;
			return true;
		}
	}
	private static final int CHECK_PARSER = 0x1;
	private static final int CHECK_COMPLETION_PARSER = 0x2;
	private static final int CHECK_SELECTION_PARSER = 0x4;
	private static final int CHECK_DOCUMENT_ELEMENT_PARSER = 0x8;
	private static final int CHECK_COMMENT_RECORDER_PARSER = 0x10;
	private static final int CHECK_SOURCE_ELEMENT_PARSER = 0x20;
	private static final int CHECK_INDEXING_PARSER = 0x40;
	private static final int CHECK_ALL = CHECK_PARSER
										| CHECK_COMPLETION_PARSER
										| CHECK_SELECTION_PARSER
										| CHECK_DOCUMENT_ELEMENT_PARSER
										| CHECK_COMMENT_RECORDER_PARSER
										| CHECK_SOURCE_ELEMENT_PARSER
										| CHECK_INDEXING_PARSER;

	public static boolean optimizeStringLiterals = false;
	public static long sourceLevel = ClassFileConstants.JDK1_3; //$NON-NLS-1$

	static {
//		TESTS_NAMES = new String[] {"test0020"};
//		TESTS_NUMBERS = new int[] { 29 };
	}
	public static Class testClass() {
		return TypeAnnotationSyntaxTest.class;
	}
	public void initialize(CompilerTestSetup setUp) {
		super.initialize(setUp);
	}
	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_7);
	}
public TypeAnnotationSyntaxTest(String testName){
	super(testName);
}
public void checkParse(
		int parserToCheck,	
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString,
		ASTVisitor visitor) {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	options.complianceLevel = ClassFileConstants.JDK1_7;
	options.sourceLevel = ClassFileConstants.JDK1_7;
	options.targetJDK = ClassFileConstants.JDK1_7;

	ICompilationUnit sourceUnit = null;
	CompilationResult compilationResult = null;
	CompilationUnitDeclaration unit = null;
	
	if ((parserToCheck & CHECK_PARSER) != 0) {
		Parser parser1 =
			new Parser(
					new ProblemReporter(
							DefaultErrorHandlingPolicies.proceedWithAllProblems(),
							options,
							new DefaultProblemFactory(Locale.getDefault())),
							optimizeStringLiterals);
		sourceUnit = new CompilationUnit(source, testName, null);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser1.parse(sourceUnit, compilationResult);
		parser1.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		if (visitor != null) {
			unit.traverse(visitor, (CompilationUnitScope) null);
		}
		parser1 = null;
	}
	
	if ((parserToCheck & CHECK_COMPLETION_PARSER) != 0) {
		CompletionParser parser2 = new CompletionParser(
				new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						options,
						new DefaultProblemFactory(Locale.getDefault())),
						false);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser2.parse(sourceUnit, compilationResult, Integer.MAX_VALUE);
		parser2.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser2 = null;
	}
	if ((parserToCheck & CHECK_SELECTION_PARSER) != 0) {
		SelectionParser parser3 = new SelectionParser(
				new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						options,
						new DefaultProblemFactory(Locale.getDefault())));
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser3.parse(sourceUnit, compilationResult, Integer.MAX_VALUE, Integer.MAX_VALUE);
		parser3.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser3 = null;
	}
	if ((parserToCheck & CHECK_DOCUMENT_ELEMENT_PARSER) != 0) {
		DocumentElementParser parser4 = new DocumentElementParser(
				this, 
				new DefaultProblemFactory(Locale.getDefault()),
				options);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser4.parse(sourceUnit, compilationResult);
		parser4.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser4 = null;
	}
	if ((parserToCheck & CHECK_COMMENT_RECORDER_PARSER) != 0) {
		CommentRecorderParser parser5 = new CommentRecorderParser(
				new ProblemReporter(
						DefaultErrorHandlingPolicies.proceedWithAllProblems(),
						options,
						new DefaultProblemFactory(Locale.getDefault())),
						optimizeStringLiterals);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser5.parse(sourceUnit, compilationResult);
		parser5.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser5 = null;
	}
	if ((parserToCheck & CHECK_SOURCE_ELEMENT_PARSER) != 0) {
		SourceElementParser parser6 = new SourceElementParser(this, 
				new DefaultProblemFactory(Locale.getDefault()),
				options,
				true,
				optimizeStringLiterals);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser6.parse(sourceUnit, compilationResult);
		parser6.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser6 = null;
	}
	if ((parserToCheck & CHECK_INDEXING_PARSER) != 0) {
		IndexingParser parser7 = new IndexingParser(this, 
				new DefaultProblemFactory(Locale.getDefault()),
				options,
				true,
				optimizeStringLiterals, false);
		compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);
		unit = parser7.parse(sourceUnit, compilationResult);
		parser7.getMethodBodies(unit);
		assertDianosticEquals(expectedSyntaxErrorDiagnosis, testName, compilationResult);
		assertParseTreeEquals(expectedUnitToString, unit.toString());
		parser7 = null;
	}
}
public void checkParse(
		int parserToCheck,	
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString) {
	checkParse(parserToCheck, source, expectedSyntaxErrorDiagnosis, testName, expectedUnitToString, null);
}
public void checkParse(
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString) {
	checkParse(CHECK_ALL, source, expectedSyntaxErrorDiagnosis, testName, expectedUnitToString);
}
public void checkParse(
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString,
		ASTVisitor visitor) {
	checkParse(CHECK_ALL, source, expectedSyntaxErrorDiagnosis, testName, expectedUnitToString, visitor);
}
private void assertParseTreeEquals(String expectedUnitToString, String computedUnitToString) {
		if (expectedUnitToString == null) {  // just checking that we are able to digest.
			return;
		}
		if (!expectedUnitToString.equals(computedUnitToString)) {
			System.out.println(Util.displayString(computedUnitToString));
		}
		assertEquals("Parse Tree is wrong",
				Util.convertToIndependantLineDelimiter(expectedUnitToString),
				Util.convertToIndependantLineDelimiter(computedUnitToString));
}
private void assertDianosticEquals(String expectedSyntaxErrorDiagnosis,
		String testName, CompilationResult compilationResult) {
	String computedSyntaxErrorDiagnosis = getCompilerMessages(compilationResult);
	assertEquals(
		"Invalid syntax error diagnosis" + testName,
		Util.convertToIndependantLineDelimiter(expectedSyntaxErrorDiagnosis),
		Util.convertToIndependantLineDelimiter(computedSyntaxErrorDiagnosis));
}
private String getCompilerMessages(CompilationResult compilationResult) {
	StringBuffer buffer = new StringBuffer(100);
	if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
		CategorizedProblem[] problems = compilationResult.getAllProblems();
		int count = problems.length;
		int problemCount = 0;
		char[] unitSource = compilationResult.compilationUnit.getContents();
		for (int i = 0; i < count; i++) {
			if (problems[i] != null) {
				if (problemCount == 0)
					buffer.append("----------\n");
				problemCount++;
				buffer.append(problemCount + (problems[i].isError() ? ". ERROR" : ". WARNING"));
				buffer.append(" in " + new String(problems[i].getOriginatingFileName()).replace('/', '\\'));
				try {
					buffer.append(((DefaultProblem)problems[i]).errorReportSource(unitSource));
					buffer.append("\n");
					buffer.append(problems[i].getMessage());
					buffer.append("\n");
				} catch (Exception e) {
				}
				buffer.append("----------\n");
			}
		}
	}
	String computedSyntaxErrorDiagnosis = buffer.toString();
	return computedSyntaxErrorDiagnosis;
}

void traverse (File f) throws IOException {
if (f.isDirectory()) {
	File[] files = f.listFiles();
	for (int i = 0; i < files.length; i++) {
		traverse(files[i]);
	}
} else {
	if (f.getName().endsWith(".java")) {
		System.out.println(f.getCanonicalPath());
		char[] contents = new char[(int) f.length()];
		FileInputStream fs = new FileInputStream(f);
		InputStreamReader isr = new InputStreamReader(fs);
		isr.read(contents);
		checkParse(contents, null, f.getCanonicalPath(), null);
	}
}
}
public void _test000() throws IOException {
traverse(new File("C:\\jsr308tests"));
}
public void test0001() {
	String source = "@Marker class A extends String {}\n;" +
					"@Marker class B extends @Marker String {}\n" +
					"@Marker class C extends @Marker @SingleMember(0) String {}\n" +
					"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"@Marker class E extends String {}\n;";

	String expectedUnitToString = 
		"@Marker class A extends String {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class B extends @Marker String {\n" + 
		"  B() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class C extends @Marker @SingleMember(0) String {\n" + 
		"  C() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" + 
		"  D() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"@Marker class E extends String {\n" + 
		"  E() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0001a() {
	String source = "class A extends String {}\n;" +
					"class B extends @Marker String {}\n" +
					"class C extends @Marker @SingleMember(0) String {}\n" +
					"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {}\n" +
					"class E extends String {}\n;";
    
	String expectedUnitToString = 
		"class A extends String {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class B extends @Marker String {\n" + 
		"  B() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class C extends @Marker @SingleMember(0) String {\n" + 
		"  C() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class D extends @Marker @SingleMember(0) @Normal(Value = 0) String {\n" + 
		"  D() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n" + 
		"class E extends String {\n" + 
		"  E() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0002() {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker Serializable, Cloneable {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0002", expectedUnitToString);
}
public void test0002a() {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker @SingleMember(0) Serializable, Cloneable {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0002a", expectedUnitToString);
}
public void test0002b() {
	String source = "@Marker class A implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0002b", expectedUnitToString);
}
public void test0002c() {
	String source = "@Marker class A implements @Marker Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   @Marker Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A implements @Marker Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, @Marker Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0002c", expectedUnitToString);
}
public void test0003() {
	String source = "@Marker class A extends Object implements Comparable, " +
					"                   @Marker @SingleMember(10) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A extends Object implements Comparable, @Marker @SingleMember(10) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0003", expectedUnitToString);
}
public void test0003a() {
	String source = "@Marker class A extends @Marker Object implements Comparable, " +
					"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
					"                   Cloneable {\n" +
					"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0003a", expectedUnitToString);
}
public void test0003b() {
	String source = "@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker @SingleMember(0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0003b", expectedUnitToString);
}
public void test0003c() {
	String source = "@Marker class A extends @Marker @SingleMember(0) @Normal(Value=0) Object implements Comparable, " +
	"                   @Marker @SingleMember(0) @Normal(Value=0) Serializable," +
	"                   Cloneable {\n" +
	"}\n";
	String expectedUnitToString = 
		"@Marker class A extends @Marker @SingleMember(0) @Normal(Value = 0) Object implements Comparable, @Marker @SingleMember(0) @Normal(Value = 0) Serializable, Cloneable {\n" +
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0003c", expectedUnitToString);
}
public void test0004() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @Marker[][] s[] @SingleMember(0)[][] @Normal(Value = 0)[][];\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String[] @Marker [][][] @SingleMember(0) [][] @Normal(Value = 0) [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0004", expectedUnitToString);
}
public void test0005() {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0005", expectedUnitToString);
}
public void test0005a() {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static void main(String args[]) {\n" +
					"    @Readonly String s;\n" +
					"	 s = new @Readonly String @NonNull[] @Nullable[] { {\"Hello\"}, {\"World\"}} [0][0];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    @Readonly String s;\n" + 
		"    s = new @Readonly String @NonNull [] @Nullable []{{\"Hello\"}, {\"World\"}}[0][0];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0005a", expectedUnitToString);
}
public void test0006() {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] @Marker {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) @Marker {\n" +
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0006", expectedUnitToString);

}
public void test0007() {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"public static int main(String args[])[] @Marker[][] @Marker @SingleMember(0) @Normal(Value=0)[][] @Marker {\n" +
					"    @Readonly String @Nullable[] @NonNull[] s;\n" +
					"    s = new @Readonly String @NonNull[5] @Nullable[];\n" +
					"}\n" +
					"@Marker public A () @Marker @SingleMember(0) @Normal(Value=10) {}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  int[][] f;\n" + 
		"  @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"  float[][] p;\n" + 
		"  public static int[] @Marker [][] @Marker @SingleMember(0) @Normal(Value = 0) [][] main(String[] args) @Marker {\n" + 
		"    @Readonly String @Nullable [] @NonNull [] s;\n" + 
		"    s = new @Readonly String @NonNull [5] @Nullable [];\n" + 
		"  }\n" + 
		"  public @Marker A() @Marker @SingleMember(0) @Normal(Value = 10) {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// parameters
public void test0008() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008a() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008b() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<String, Object>[] @SingleMember(10)[][] args[] @Normal(Value = 10)[][])[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008c() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][] args[] @Normal(Value = 10) [][])[] @Marker [][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<String, Object>.Iterator[] @SingleMember(10) [][][] @Normal(Value = 10) [][] args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// varargs annotation
public void test0008d() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(int[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(int[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008e() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(String[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(String[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008f() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
public void test0008g() {
	String source = "public class A {\n" +
					"@Marker public int[] @Marker[][] main(HashMap<Integer,String>.Iterator[] @SingleMember(10)[][] @Marker ... args )[] @Marker[][] @Marker {\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @Marker [][][] @Marker [][] main(HashMap<Integer, String>.Iterator[] @SingleMember(10) [][] @Marker ... args) @Marker {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// local variables
public void test0009() {
	String source = "public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" +
					"public static void main(String args[]) {\n" +
					"    int[] f[];\n" +
					"    @English String[] @NonNull[] s[] @Nullable[][];\n" +
					"    float[] p[];\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A implements @Readonly Comparable, @NonNull Serializable, Cloneable {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int[][] f;\n" + 
		"    @English String[] @NonNull [][] @Nullable [][] s;\n" + 
		"    float[][] p;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// type parameter
public void test0010() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> void foo() {\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" +
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>void foo() {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// Type
public void test0011() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int foo() @Marker {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int bar() @Marker{\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker int foo() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>int bar() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// Type
public void test0011a() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker String foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>String bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type
public void test0011b() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object> foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object> bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker HashMap<@Readonly String, Object> foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object> bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// Type
public void test0011c() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>.Iterator bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type
public void test0011d() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>.Iterator[] @NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>.Iterator[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>.Iterator[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type
public void test0011e() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker int[] @NonEmpty[][] foo() @Marker {\n" +
					"    return 0;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> int[] @NonEmpty[][] bar() @Marker{\n" +
					"    return 0;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker int[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>int[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return 0;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// Type
public void test0011f() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker String[]@NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> String[]@NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker String[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>String[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type
public void test0011g() {
	String source = "class A {\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> @Marker HashMap<@Readonly String, Object>[] @NonEmpty[][] foo() @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"public <Integer, @Positive Integer, @Negative Integer, Integer> HashMap<String, @NonNull Object>[]@NonEmpty[][] bar () @Marker {\n" +
					"    return null;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"class A {\n" + 
		"  A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>@Marker HashMap<@Readonly String, Object>[] @NonEmpty [][] foo() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"  public <Integer, @Positive Integer, @Negative Integer, Integer>HashMap<String, @NonNull Object>[] @NonEmpty [][] bar() @Marker {\n" + 
		"    return null;\n" +
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// Type0 field declaration.
public void test0012() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker int k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012a() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012b() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer> k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012c() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012d() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker int[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker int[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012e() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker String[] @NonEmpty[][]k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker String[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012f() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 field declaration.
public void test0012g() {
	String source = "public class A {\n" +
					"    int[] f[];\n" +
					"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] k;\n" +
					"    float[] p[];\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  int[][] f;\n" + 
		"  @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] k;\n" + 
		"  float[][] p;\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013() {
	String source = "public class A {\n" +
					"    public @Marker int foo() { return 0; }\n" +
					"    public int bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int foo() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"  public int bar() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013a() {
	String source = "public class A {\n" +
					"    public @Marker String foo() { return null; }\n" +
					"    public String bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker String foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public String bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013b() {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer> foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer> foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer> bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013c() {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator  bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013d() {
	String source = "public class A {\n" +
					"    public @Marker int[] foo() @NonEmpty[][] { return 0; }\n" +
					"    public int[] @NonEmpty[][] bar() { return 0; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker int[] @NonEmpty [][] foo() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"  public int[] @NonEmpty [][] bar() {\n" + 
		"    return 0;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013e() {
	String source = "public class A {\n" +
					"    public @Marker String[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public String[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker String[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public String[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013f() {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>[] foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer> [] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 MethodHeaderName.
public void test0013g() {
	String source = "public class A {\n" +
					"    public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[]  foo() @NonEmpty[][] { return null; }\n" +
					"    public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] bar() { return null; }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] foo() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"  public HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][] bar() {\n" + 
		"    return null;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int p;\n" +
					"        int q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker int p;\n" + 
		"    int q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014a() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String p;\n" +
					"        String q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker String p;\n" + 
		"    String q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014b() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer> q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer> p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer> q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014c() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014d() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker int[] @NonNull[] p @NonEmpty[][];\n" +
					"        int[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker int[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    int[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014e() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker String[] @NonNull[] p @NonEmpty[][];\n" +
					"        String[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker String[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    String[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014f() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>[] @NonNull[] q @NonEmpty[][];\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 local variable declaration
public void test0014g() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] p @NonEmpty[][];\n" +
					"        HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull[] @NonEmpty[][] q;\n" + 
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    @Marker HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] p;\n" + 
		"    HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonNull [] @NonEmpty [][] q;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 foreach
public void test0015() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        String @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly String @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (String @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    String @NonNull [] @Marker [] @Readonly [] s;\n" + 
		"    for (@Readonly String @NonNull [] @Marker [] si : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"    for (String @NonNull [] @Marker [] sii : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//Type0 foreach
public void test0015a() {
	String source = "public class A {\n" +
					"    public void foo() {\n" +
					"        int @NonNull[] @Marker[] s @Readonly[];\n" +
					"    	 for (@Readonly int @NonNull[] si @Marker[] : s) {}\n" +
					"    	 for (int @NonNull[] sii @Marker[] : s) {}\n" +
					"    }\n" +
					"}\n";
	String expectedUnitToString = 
		"public class A {\n" + 
		"  public A() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public void foo() {\n" + 
		"    int @NonNull [] @Marker [] @Readonly [] s;\n" + 
		"    for (@Readonly int @NonNull [] @Marker [] si : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"    for (int @NonNull [] @Marker [] sii : s) \n" + 
		"      {\n" + 
		"      }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// cast expression
public void test0016() {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value=0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(@Readonly String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(@Readonly int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly HashMap<@Positive Integer, @Negative Integer>)\n" +
					"(@Readonly Object)\n" +
					"(@ReadOnly String)\n" +
					"(@Readonly Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int x;\n" + 
		"    x = (Integer) (@Readonly Object) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @SingleMember(0) [][]) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @Marker [][]) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Normal(Value = 0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, @Negative Integer>[] @Marker [][]) (@Readonly Object) (@Readonly String[] @Normal(Value = 0) [][]) (@Readonly String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (@Readonly Object) (@Readonly int[] @Normal(Value = 0) [][]) (@Readonly int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (@Readonly Object) ( @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) (@Readonly Object) (@Readonly HashMap<@Positive Integer, @Negative Integer>) (@Readonly Object) (@ReadOnly String) (@Readonly Object) (@Readonly int) 10;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
//cast expression
public void test0016a() {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"int x;\n" +
					"x = (Integer)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value=0)[][] )\n" +
					"(HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value=0)[][] )\n" +
					"(HashMap<Integer, @Negative Integer>[] @SingleMember(0)[][] )\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly String[] @Normal(Value=0)[][] )\n" +
					"(String[] @SingleMember(0)[][] )\n" +
					"(@Readonly String[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly int[] @Normal(Value=0)[][] )\n" +
					"(int[] @SingleMember(0)[][] )\n" +
					"(@Readonly int[] @Marker[][] )\n" +
					"(Object)\n" +
					"(@Readonly HashMap<Integer, @Negative Integer>.Iterator)\n" +
					"(Object)\n" +
					"(@Readonly HashMap<@Positive Integer, Integer>)\n" +
					"(Object)\n" +
					"(@ReadOnly String)\n" +
					"(Object)\n" +
					"(@Readonly int) 10;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    int x;\n" + 
		"    x = (Integer) (Object) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Normal(Value = 0) [][]) (HashMap<@Positive Integer, Integer>.Iterator[] @SingleMember(0) [][]) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator[] @Marker [][]) (Object) (@Readonly HashMap<@Positive Integer, Integer>[] @Normal(Value = 0) [][]) (HashMap<Integer, @Negative Integer>[] @SingleMember(0) [][]) (@Readonly HashMap<@Positive Integer, Integer>[] @Marker [][]) (Object) (@Readonly String[] @Normal(Value = 0) [][]) (String[] @SingleMember(0) [][]) (@Readonly String[] @Marker [][]) (Object) (@Readonly int[] @Normal(Value = 0) [][]) (int[] @SingleMember(0) [][]) (@Readonly int[] @Marker [][]) (Object) ( @Readonly HashMap<Integer, @Negative Integer>.Iterator) (Object) (@Readonly HashMap<@Positive Integer, Integer>) (Object) (@ReadOnly String) (Object) (@Readonly int) 10;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test001", expectedUnitToString);
}
// instanceof checks 
public void test0017() {
	String source = "public class Clazz {\n" +
					"public static void main(Object o) {\n" +
					"if (o instanceof @Readonly String) {\n" +
					"} else if (o instanceof @Readonly int[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly String[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<?,?>[] @NonEmpty[][] ) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty[][] ) {\n" +	
					"} else if (o instanceof @Readonly HashMap<?,?>) {\n" +
					"} else if (o instanceof @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator) {\n" +
					"}\n" +
					"}\n" +
					"}";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(Object o) {\n" + 
		"    if ((o instanceof @Readonly String))\n" + 
		"        {\n" + 
		"        }\n" + 
		"    else\n" + 
		"        if ((o instanceof @Readonly int[] @NonEmpty [][]))\n" + 
		"            {\n" + 
		"            }\n" + 
		"        else\n" + 
		"            if ((o instanceof @Readonly String[] @NonEmpty [][]))\n" + 
		"                {\n" + 
		"                }\n" + 
		"            else\n" + 
		"                if ((o instanceof @Readonly HashMap<?, ?>[] @NonEmpty [][]))\n" + 
		"                    {\n" + 
		"                    }\n" + 
		"                else\n" + 
		"                    if ((o instanceof  @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator[] @NonEmpty [][]))\n" + 
		"                        {\n" + 
		"                        }\n" + 
		"                    else\n" + 
		"                        if ((o instanceof @Readonly HashMap<?, ?>))\n" + 
		"                            {\n" + 
		"                            }\n" + 
		"                        else\n" + 
		"                            if ((o instanceof  @Readonly HashMap<@Positive Integer, @Negative Integer>.Iterator))\n" + 
		"                                {\n" + 
		"                                }\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0017", expectedUnitToString);
}
// assorted unclassified 
public void test0018() {
	String source = "import java.util.HashMap;\n" +
					"import java.util.Map; \n" +  
					"\n" +
					"public class Clazz <@A M extends @B String, @C N extends @D Comparable> extends\n" +
					"								@E Object implements @F Comparable <@G Object> {\n" +
					"	\n" +
					"  Clazz(char[] ...args) @H { \n" +   
					"   }\n" +
					"   \n" +
					"  int @I[] f @J[], g, h[], i@K[];\n" +
					"  int @L[][]@M[] f2; \n" +
					"   \n" +
					"  Clazz (int @N[] @O... a) @Q {}\n" +
					" int @R[]@S[] aa() {}\n" +
					" \n" +
					" int @T[]@U[]@V[] a () @W[]@X[]@Y[] @Z { return null; }\n" +
					"   \n" +
					"  public void main(String @A[] @B ... args) @C throws @D Exception {\n" +
					"  	\n" +
					"       HashMap<@E String, @F String> b1;\n" +
					"      \n" +
					"     int b; b = (@G int) 10;\n" +
					"      \n" +
					"     char @H[]@I[] ch; ch = (@K char @L[]@M[])(@N char @O[]@P[]) null;\n" +
					"      \n" +
					"      int[] i; i = new @Q int @R[10];\n" +
					"       \n" +
					"      \n" +
					"   Integer w; w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" +
					"    throw new @V Exception(\"test\");\n" +
					"    boolean c; c  = null instanceof @W String;\n" +
					"	} \n" +
					" public <@X X, @Y Y> void foo(X x, Y @Z... y) {  \n" +
					"	\n" +
					"}\n" +
					" \n" +
					" void foo(Map<? super @A Object, ? extends @B String> m){}\n" +
					" public int compareTo(Object arg0) {\n" +
					"     return 0;\n" +
					" }\n" +
					"\n" +
					"}\n" +
					"class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" +
					"	\n" +
					"  public Integer get(Integer integer) {\n" +
					"       return null;\n" +
					"   }\n" +
					"}\n";
					
					
	String expectedUnitToString = "import java.util.HashMap;\n" + 
								  "import java.util.Map;\n" + 
								  "public class Clazz<@A M extends @B String, @C N extends @D Comparable> extends @E Object implements @F Comparable<@G Object> {\n" + 
								  "  int @I [] @J [] f;\n" + 
								  "  int @I [] g;\n" + 
								  "  int @I [][] h;\n" + 
								  "  int @I [] @K [] i;\n" + 
								  "  int @L [][] @M [] f2;\n" + 
								  "  Clazz(char[]... args) @H {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  Clazz(int @N [] @O ... a) @Q {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  int @R [] @S [] aa() {\n" + 
								  "  }\n" + 
								  "  int @T [] @U [] @V [] @W [] @X [] @Y [] a() @Z {\n" + 
								  "    return null;\n" + 
								  "  }\n" + 
								  "  public void main(String @A [] @B ... args) @C throws @D Exception {\n" + 
								  "    HashMap<@E String, @F String> b1;\n" + 
								  "    int b;\n" +
								  "    b = (@G int) 10;\n" + 
								  "    char @H [] @I [] ch;\n" +
								  "    ch = (@K char @L [] @M []) (@N char @O [] @P []) null;\n" + 
								  "    int[] i;\n" +
								  "    i = new @Q int @R [10];\n" + 
								  "    Integer w;\n" +
								  "    w = new X<@S String, @T Integer>().get(new @U Integer(12));\n" + 
								  "    throw new @V Exception(\"test\");\n" + 
								  "    boolean c;\n" +
								  "    c = (null instanceof @W String);\n" + 
								  "  }\n" + 
								  "  public <@X X, @Y Y>void foo(X x, Y @Z ... y) {\n" + 
								  "  }\n" + 
								  "  void foo(Map<? super @A Object, ? extends @B String> m) {\n" + 
								  "  }\n" + 
								  "  public int compareTo(Object arg0) {\n" + 
								  "    return 0;\n" + 
								  "  }\n" + 
								  "}\n" + 
								  "class X<@C K, @D T extends @E Object & @F Comparable<? super @G T>> {\n" + 
								  "  X() {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "  public Integer get(Integer integer) {\n" + 
								  "    return null;\n" + 
								  "  }\n" + 
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test0018", expectedUnitToString);
}
//assorted unclassified 
public void test0019() {
	String source = "class X<T extends @E Object & @F Comparable<? super T>> {}\n";
	String expectedUnitToString = "class X<T extends @E Object & @F Comparable<? super T>> {\n" + 
								  "  X() {\n" + 
								  "    super();\n" + 
								  "  }\n" + 
								  "}\n";
	// indexing parser avoids creating lots of nodes, so parse tree comes out incorrectly.
	// this is not bug, but intended behavior - see IndexingParser.newSingleNameReference(char[], long)
	checkParse(CHECK_ALL & ~CHECK_INDEXING_PARSER, source.toCharArray(), null, "test019", expectedUnitToString);
}
//type class literal expression
public void test0020() {
	String source = "public class Clazz {\n" +
					"public static void main(String[] args) {\n" +
					"Class x;\n" +
					"x = Integer.class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = HashMap.Iterator[] @Normal(Value=0)[][].class;\n" +
					"x = @Readonly HashMap.Iterator[] @SingleMember(0)[][].class;\n" +
					"x = @Readonly HashMap.Iterator @Normal(Value=1)[] @Marker[] @Normal(Value=2)[].class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = @Readonly String[] @Normal(Value=0)[][].class;\n" +
					"x = @Readonly String[] @SingleMember(0)[][].class;\n" +
					"x = @Readonly String[] @Marker[][].class;\n" +
					"x = @Readonly Object.class;\n" +
					"x = @Readonly int[][] @Normal(Value=0)[].class;\n" +
					"x = @Readonly int @SingleMember(0)[][][].class;\n" +
					"x = @Readonly int[] @Marker[][].class;\n" +
					"x = @Readonly int.class;\n" +
					"}\n" +
					"}\n";
	String expectedUnitToString = 
		"public class Clazz {\n" + 
		"  public Clazz() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public static void main(String[] args) {\n" + 
		"    Class x;\n" + 
		"    x = Integer.class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = HashMap.Iterator[] @Normal(Value = 0) [][].class;\n" + 
		"    x = @Readonly HashMap.Iterator[] @SingleMember(0) [][].class;\n" + 
		"    x = @Readonly HashMap.Iterator @Normal(Value = 1) [] @Marker [] @Normal(Value = 2) [].class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = @Readonly String[] @Normal(Value = 0) [][].class;\n" + 
		"    x = @Readonly String[] @SingleMember(0) [][].class;\n" + 
		"    x = @Readonly String[] @Marker [][].class;\n" + 
		"    x = @Readonly Object.class;\n" + 
		"    x = @Readonly int[][] @Normal(Value = 0) [].class;\n" + 
		"    x = @Readonly int @SingleMember(0) [][][].class;\n" + 
		"    x = @Readonly int[] @Marker [][].class;\n" + 
		"    x = @Readonly int.class;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0020", expectedUnitToString);
}
//type class literal expression
public void test0021() {
	String source = "public class X {\n" + 
			"	<T extends Y<@A String @C[][]@B[]> & Cloneable> void foo(T t) {}\n" + 
			"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  <T extends Y<@A String @C [][] @B []> & Cloneable>void foo(T t) {\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0021", expectedUnitToString);
}
//type class literal expression
public void test0022() {
	String source = 
	"public class X {\n" + 
	"	public boolean foo(String s) {\n" + 
	"		return (s instanceof @C('_') Object[]);\n" + 
	"	}\n" + 
	"	public Object foo1(String s) {\n" + 
	"		return new @B(3) @A(\"new Object\") Object[] {};\n" + 
	"	}\n" + 
	"	public Class foo2(String s) {\n" + 
	"		return @B(4) Object[].class;\n" + 
	"	}\n" + 
	"	public Class foo3(String s) {\n" + 
	"		return @A(\"int class literal\")  @B(5) int[].class;\n" + 
	"	}\n" + 
	"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"  public boolean foo(String s) {\n" + 
		"    return (s instanceof @C(\'_\') Object[]);\n" + 
		"  }\n" + 
		"  public Object foo1(String s) {\n" + 
		"    return new @B(3) @A(\"new Object\") Object[]{};\n" + 
		"  }\n" + 
		"  public Class foo2(String s) {\n" + 
		"    return @B(4) Object[].class;\n" + 
		"  }\n" + 
		"  public Class foo3(String s) {\n" + 
		"    return @A(\"int class literal\") @B(5) int[].class;\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0022", expectedUnitToString);
}
//check locations
public void test0023() {
	String source = 
		"public class X {\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"	@A Map<@B String, @C List<@D Object>> field2;\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H String @E [] @F [] @G [] field;\n" + 
		"  @A Map<@B String, @C List<@D Object>> field2;\n" + 
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0023", expectedUnitToString);
}
//check locations
public void test0024() {
	String source = 
		"public class X {\n" + 
		"	@H String @E[] @F[] @G[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @H String @E [] @F [] @G [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0024", expectedUnitToString, new LocationPrinterVisitor());
}
//check locations
public void test0025() {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @H String> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @H String> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0025", expectedUnitToString, new LocationPrinterVisitor());
}
//check locations
public void test0026() {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @H String @E[] @F[] @G[]> field3;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @H String @E [] @F [] @G []> field3;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0026", expectedUnitToString, new LocationPrinterVisitor());
}
//check locations
public void test0027() {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0027", expectedUnitToString, new LocationPrinterVisitor());
}
//check locations
public void test0028() {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>>[] @I[] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>>[] @I [] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0028", expectedUnitToString, new LocationPrinterVisitor());
}
//check locations
public void test0029() {
	String source = 
		"public class X {\n" + 
		"	@A Map<@B String, @C List<@H String @E[][] @G[]>> @I[][] @J[] field;\n" + 
		"}";
	String expectedUnitToString = 
		"public class X {\n" + 
		"  @A Map<@B String, @C List<@H String @E [][] @G []>> @I [][] @J [] field;\n" + 
		"  public X() {\n" + 
		"    super();\n" + 
		"  }\n" + 
		"}\n";
	checkParse(source.toCharArray(), null, "test0028", expectedUnitToString, new LocationPrinterVisitor());
}

public void acceptImport(int declarationStart, int declarationEnd,
		int[] javaDocPositions, char[] name, int nameStartPosition,
		boolean onDemand, int modifiers) {
}
public void acceptInitializer(int declarationStart, int declarationEnd,
		int[] javaDocPositions, int modifiers, int modifiersStart,
		int bodyStart, int bodyEnd) {
}
public void acceptLineSeparatorPositions(int[] positions) {
}
public void acceptPackage(int declarationStart, int declarationEnd,
		int[] javaDocPositions, char[] name, int nameStartPosition) {
}
public void acceptProblem(CategorizedProblem problem) {
}
public void enterClass(int declarationStart, int[] javaDocPositions,
		int modifiers, int modifiersStart, int classStart, char[] name,
		int nameStart, int nameEnd, char[] superclass, int superclassStart,
		int superclassEnd, char[][] superinterfaces,
		int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
}
public void enterCompilationUnit() {
}
public void enterConstructor(int declarationStart, int[] javaDocPositions,
		int modifiers, int modifiersStart, char[] name, int nameStart,
		int nameEnd, char[][] parameterTypes, int[] parameterTypeStarts,
		int[] parameterTypeEnds, char[][] parameterNames,
		int[] parameterNameStarts, int[] parameterNameEnds, int parametersEnd,
		char[][] exceptionTypes, int[] exceptionTypeStarts,
		int[] exceptionTypeEnds, int bodyStart) {
}
public void enterField(int declarationStart, int[] javaDocPositions,
		int modifiers, int modifiersStart, char[] type, int typeStart,
		int typeEnd, int typeDimensionCount, char[] name, int nameStart,
		int nameEnd, int extendedTypeDimensionCount,
		int extendedTypeDimensionEnd) {
}
public void enterInterface(int declarationStart, int[] javaDocPositions,
		int modifiers, int modifiersStart, int interfaceStart, char[] name,
		int nameStart, int nameEnd, char[][] superinterfaces,
		int[] superinterfaceStarts, int[] superinterfaceEnds, int bodyStart) {
}
public void enterMethod(int declarationStart, int[] javaDocPositions,
		int modifiers, int modifiersStart, char[] returnType,
		int returnTypeStart, int returnTypeEnd, int returnTypeDimensionCount,
		char[] name, int nameStart, int nameEnd, char[][] parameterTypes,
		int[] parameterTypeStarts, int[] parameterTypeEnds,
		char[][] parameterNames, int[] parameterNameStarts,
		int[] parameterNameEnds, int parametersEnd,
		int extendedReturnTypeDimensionCount,
		int extendedReturnTypeDimensionEnd, char[][] exceptionTypes,
		int[] exceptionTypeStarts, int[] exceptionTypeEnds, int bodyStart) {
}
public void exitClass(int bodyEnd, int declarationEnd) {
}
public void exitCompilationUnit(int declarationEnd) {
}
public void exitConstructor(int bodyEnd, int declarationEnd) {
}
public void exitField(int bodyEnd, int declarationEnd) {
}
public void exitInterface(int bodyEnd, int declarationEnd) {
}
public void exitMethod(int bodyEnd, int declarationEnd) {
}
public void acceptAnnotationTypeReference(char[][] annotation, int sourceStart,
		int sourceEnd) {
}
public void acceptAnnotationTypeReference(char[] annotation, int sourcePosition) {
}
public void acceptConstructorReference(char[] typeName, int argCount,
		int sourcePosition) {
}
public void acceptFieldReference(char[] fieldName, int sourcePosition) {
}
public void acceptImport(int declarationStart, int declarationEnd,
		char[][] tokens, boolean onDemand, int modifiers) {
}
public void acceptMethodReference(char[] methodName, int argCount,
		int sourcePosition) {
}
public void acceptPackage(ImportReference importReference) {
}
public void acceptTypeReference(char[][] typeName, int sourceStart,
		int sourceEnd) {
}
public void acceptTypeReference(char[] typeName, int sourcePosition) {
}
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {
}
public void acceptUnknownReference(char[] name, int sourcePosition) {
}
public void enterConstructor(MethodInfo methodInfo) {
}
public void enterField(FieldInfo fieldInfo) {
}
public void enterInitializer(int declarationStart, int modifiers) {
}
public void enterMethod(MethodInfo methodInfo) {
}
public void enterType(TypeInfo typeInfo) {
}
public void exitConstructor(int declarationEnd) {
}
public void exitField(int initializationStart, int declarationEnd,
		int declarationSourceEnd) {
}
public void exitInitializer(int declarationEnd) {
}
public void exitMethod(int declarationEnd, Expression defaultValue) {
}
public void exitType(int declarationEnd) {
}
}

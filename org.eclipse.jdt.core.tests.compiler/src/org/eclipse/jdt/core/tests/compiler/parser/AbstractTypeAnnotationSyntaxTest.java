/*******************************************************************************
 * Copyright (c) 2011, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
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
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.search.indexing.IndexingParser;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

public abstract class AbstractTypeAnnotationSyntaxTest extends AbstractCompilerTest implements IDocumentElementRequestor, ISourceElementRequestor {
	static final class LocationPrinterVisitor extends ASTVisitor {
		Annotation[] primaryAnnotations;
		TypeReference enclosingReference;
		Map locations;

		public LocationPrinterVisitor() {
			this.locations = new HashMap();
		}

		public Map getLocations() {
			return this.locations;
		}
		public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
			Annotation[] annotations = fieldDeclaration.annotations;
			this.enclosingReference = fieldDeclaration.type;
			this.primaryAnnotations = annotations;
			return true;
		}
		public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
			this.primaryAnnotations = methodDeclaration.annotations;
			TypeReference returnType = methodDeclaration.returnType;
			if (returnType != null) {
				this.enclosingReference = returnType;
				returnType.traverse(this, scope);
			}
			if (methodDeclaration.thrownExceptions != null) {
				int thrownExceptionsLength = methodDeclaration.thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++) {
					TypeReference typeReference = methodDeclaration.thrownExceptions[i];
					this.enclosingReference = typeReference;
					this.primaryAnnotations = null;
					typeReference.traverse(this, scope);
				}
			}
			return false;
		}
		public boolean visit(Argument argument, ClassScope scope) {
			Annotation[] annotations = argument.annotations;
			this.enclosingReference = argument.type;
			this.primaryAnnotations = annotations;
			return true;
		}
		public boolean visit(Argument argument, BlockScope scope) {
			Annotation[] annotations = argument.annotations;
			this.enclosingReference = argument.type;
			this.primaryAnnotations = annotations;
			return true;
		}
		public boolean visit(MarkerAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation, null));
			}
			return false;
		}
		public boolean visit(SingleMemberAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation, null));
			}
			return false;
		}
		public boolean visit(NormalAnnotation annotation, BlockScope scope) {
			if (this.enclosingReference != null) {
				storeLocations(annotation, Annotation.getLocations(this.enclosingReference, this.primaryAnnotations, annotation, null));
			}
			return false;
		}
		public void storeLocations(Annotation annotation, int[] tab) {
			String key = String.valueOf(annotation);
			if (this.locations.get(key) != null) {
				return;
			}
			if (tab == null) {
				this.locations.put(key, null);
				return;
			}
			StringBuffer buffer = new StringBuffer("{");
			for (int i = 0, max = tab.length; i < max; i++) {
				if (i > 0) {
					buffer.append(',');
				}
				buffer.append(tab[i]);
			}
			buffer.append('}');
			this.locations.put(key, String.valueOf(buffer));
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
	protected static String  jsr308TestScratchArea = "c:\\Jsr308TestScratchArea";
	protected static final int CHECK_PARSER = 0x1;
	protected static final int CHECK_COMPLETION_PARSER = 0x2;
	protected static final int CHECK_SELECTION_PARSER = 0x4;
	protected static final int CHECK_DOCUMENT_ELEMENT_PARSER = 0x8;
	protected static final int CHECK_COMMENT_RECORDER_PARSER = 0x10;
	protected static final int CHECK_SOURCE_ELEMENT_PARSER = 0x20;
	protected static final int CHECK_INDEXING_PARSER = 0x40;
	protected static final int CHECK_JAVAC_PARSER = 0x80;
	protected static int CHECK_ALL = (CHECK_PARSER | CHECK_COMPLETION_PARSER | CHECK_SELECTION_PARSER |
											CHECK_DOCUMENT_ELEMENT_PARSER | CHECK_COMMENT_RECORDER_PARSER |
											CHECK_SOURCE_ELEMENT_PARSER | CHECK_INDEXING_PARSER);
	public static final String compiler = "C:\\jdk-7-ea-bin-b75-windows-i586-30_oct_2009\\jdk7\\bin\\javac.exe";
	public static boolean optimizeStringLiterals = false;



public void initialize(CompilerTestSetup setUp) {
	super.initialize(setUp);
}
public AbstractTypeAnnotationSyntaxTest(String testName){
	super(testName);
	if ((new File(compiler).exists())) {
		File f = new File(jsr308TestScratchArea);
		if (!f.exists()) {
			f.mkdir();
		}
		if (f.exists()) {
			try {
				OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Marker.java")));
				w.write("@interface Marker {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Normal.java")));
				w.write("@interface Normal {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "SingleMember.java")));
				w.write("@interface SingleMember {\n\tint value() default 10;\n}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Positive.java")));
				w.write("@interface Positive {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Negative.java")));
				w.write("@interface Negative{}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "Readonly.java")));
				w.write("@interface Readonly {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "NonNull.java")));
				w.write("@interface NonNull {}\n".toCharArray());
				w.close();
				w = new OutputStreamWriter(new FileOutputStream(new File(jsr308TestScratchArea + File.separator + "HashMap.java")));
				w.write("class HashMap<X,Y> {\n class Iterator {}; \n}\n".toCharArray());
				w.close();
				CHECK_ALL |= CHECK_JAVAC_PARSER;
			} catch (IOException e) {
				// ignore
			}
		}
	}
}
public void checkParse(
		int parserToCheck,
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString,
		ASTVisitor visitor) throws IOException {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	options.complianceLevel = ClassFileConstants.JDK1_8;
	options.sourceLevel = ClassFileConstants.JDK1_8;
	options.targetJDK = ClassFileConstants.JDK1_8;

	ICompilationUnit sourceUnit = null;
	CompilationResult compilationResult = null;
	CompilationUnitDeclaration unit = null;

	if ((parserToCheck & CHECK_JAVAC_PARSER) != 0) {
		String javaFilePath = jsr308TestScratchArea + "\\Xyz.java";
		File f = new File(javaFilePath);
		FileOutputStream o = new FileOutputStream(f);
		OutputStreamWriter w = new OutputStreamWriter(o);
		w.write(source);
		w.close();
		Process p = Runtime.getRuntime().exec (new String[] { compiler, "-sourcepath", jsr308TestScratchArea, javaFilePath }, null, new File(jsr308TestScratchArea));
		try {
			BufferedReader stdout  = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader stderr  = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line;
			while ((line = stderr.readLine())!= null)
				System.out.println(line);
			while ((line = stdout.readLine())!= null)
				System.out.println(line);
			assertTrue("javac unhappy", p.waitFor() == 0);
		} catch (InterruptedException e) {
			System.err.println("Skipped javac behavior check due to interrupt...");
		}
	}
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
		String testName,
		String expectedUnitToString) throws IOException {
	checkParse(parserToCheck, source, expectedSyntaxErrorDiagnosis, testName, expectedUnitToString, null);
}
public void checkParse(
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString) throws IOException {
	checkParse(CHECK_ALL, source, expectedSyntaxErrorDiagnosis, testName, expectedUnitToString);
}
public void checkParse(
		char[] source,
		String expectedSyntaxErrorDiagnosis,
		String testName, String expectedUnitToString,
		ASTVisitor visitor) throws IOException {
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
	File [] files = f.listFiles();
	for (int i = 0; i < files.length; i++) {
		traverse(files[i]);
	}
} else {
	if (f.getName().endsWith(".java")) {
		System.out.println(f.getCanonicalPath());
		char [] contents = new char[(int) f.length()];
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
		int nameStart, int nameEnd,
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

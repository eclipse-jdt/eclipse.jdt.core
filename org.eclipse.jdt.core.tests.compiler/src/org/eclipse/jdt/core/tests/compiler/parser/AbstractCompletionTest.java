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
package org.eclipse.jdt.core.tests.compiler.parser;

import java.util.Locale;

import org.eclipse.jdt.internal.codeassist.complete.CompletionParser;
import org.eclipse.jdt.internal.codeassist.complete.CompletionScanner;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.Util;

public abstract class AbstractCompletionTest extends AbstractCompilerTest {

	public final static String NONE = "<NONE>";
public AbstractCompletionTest(String testName){
	super(testName);
}
/*
 * DietParse with completionNode check
 */
public void checkDietParse(
	char[] source, 
	int cursorLocation, 
	String expectedCompletion, 
	String expectedUnitToString, 
	String expectedCompletionIdentifier,
	String expectedReplacedSource,
	String testName) {
	this.checkDietParse(
		source, 
		cursorLocation, 
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		testName);
}
/*
 * DietParse with completionNode check
 */
public void checkDietParse(
	char[] source, 
	int cursorLocation, 
	String expectedCompletion, 
	String expectedParentCompletion,
	String expectedUnitToString, 
	String expectedCompletionIdentifier,
	String expectedReplacedSource,
	String testName) {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	CompletionParser parser = 
		new CompletionParser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
				options, 
				new DefaultProblemFactory(Locale.getDefault())));

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);	

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, cursorLocation);

	String computedCompletion = parser.assistNode == null 
									? NONE
									: parser.assistNode.toString();
	String computedParentCompletion = parser.assistNodeParent == null 
								? NONE
								: parser.assistNodeParent.toString();
	String computedUnitToString = unit.toString();
	//System.out.println(computedUnitToString);
	//System.out.println(Util.displayString(computedUnitToString));
	//System.out.println(expectedUnitToString);
	
	if (!expectedCompletion.equals(computedCompletion)) {
		System.out.println(Util.displayString(computedCompletion));
	}
	assertEquals(
		"invalid completion node-" + testName,
		expectedCompletion,
		computedCompletion);
	
	if(expectedParentCompletion != null) {
		if (!expectedParentCompletion.equals(computedParentCompletion)) {
			System.out.println(Util.displayString(computedParentCompletion));
		}
		assertEquals(
		"invalid completion parent node-" + testName,
		expectedParentCompletion,
		computedParentCompletion);
	}

	if (!expectedUnitToString.equals(computedUnitToString)) {
		System.out.println(Util.displayString(computedUnitToString));
	}
	assertEquals(
		"invalid completion tree-" + testName,
		expectedUnitToString,
		computedUnitToString);
	
	if (expectedCompletionIdentifier != null){
		char[] chars = ((CompletionScanner)parser.scanner).completionIdentifier;
		String computedCompletionIdentifier = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid completion identifier-" + testName,
			expectedCompletionIdentifier,
			computedCompletionIdentifier);
	}
	
	if (expectedReplacedSource != null){
		char[] chars = null;
		if (parser.assistNode != null){
			chars = CharOperation.subarray(
				parser.scanner.source, 
				parser.assistNode.sourceStart, 
				parser.assistNode.sourceEnd + 1);
		} else {
			if (parser.assistIdentifier() != null){
				if (((CompletionScanner)parser.scanner).completedIdentifierEnd 
					>= ((CompletionScanner)parser.scanner).completedIdentifierStart){
					chars = CharOperation.subarray(
						parser.scanner.source, 
						((CompletionScanner)parser.scanner).completedIdentifierStart, 
						((CompletionScanner)parser.scanner).completedIdentifierEnd + 1);
				}
			}
		}
		String computedReplacedSource  = chars == null ? NONE : new String(chars);
		assertEquals(
			"invalid replaced source-" + testName,
			expectedReplacedSource,
			computedReplacedSource);
	}
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source, 
		int cursorLocation, 
		String expectedCompletion, 
		String expectedParentCompletion,
		String expectedUnitToString, 
		String expectedCompletionIdentifier, 
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {

	CompilerOptions options = new CompilerOptions(getCompilerOptions());
	CompletionParser parser = 
		new CompletionParser(
			new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(), 
				options, 
				new DefaultProblemFactory(Locale.getDefault())));

	ICompilationUnit sourceUnit = new CompilationUnit(source, testName, null);
	CompilationResult compilationResult = new CompilationResult(sourceUnit, 0, 0, 0);	

	CompilationUnitDeclaration unit = parser.dietParse(sourceUnit, compilationResult, cursorLocation);

	ASTNode foundMethod = null;
	if (unit.types != null) {
		for (int i = 0; i < unit.types.length; i++) {
			TypeDeclaration type = unit.types[i];
			ASTNode method = findMethod(type, cursorLocation);
			if (method != null) {
				foundMethod = method;
				break;
			}
		}
	}
	assertTrue("no method found at cursor location", foundMethod != null);
	if (foundMethod instanceof AbstractMethodDeclaration) {
		parser.parseBlockStatements((AbstractMethodDeclaration)foundMethod, unit);
	} else {
		TypeDeclaration type = (TypeDeclaration)foundMethod;
		if (type.fields != null) {
			for (int i = 0; i < type.fields.length; i++) {
				FieldDeclaration field = type.fields[i];
				if (field.declarationSourceStart <= cursorLocation && (cursorLocation <= field.declarationSourceEnd || field.declarationSourceEnd == 0)) {
					if (field instanceof Initializer) {
						parser.parseBlockStatements((Initializer)field, type, unit);
						break;
					}
					assertTrue("TBD", false); // field initializer
				}
			}
		}
	}
	
	String computedCompletion = parser.assistNode == null 
								? NONE
								: parser.assistNode.toString();
	String computedParentCompletion = parser.assistNodeParent == null 
								? NONE
								: parser.assistNodeParent.toString();
	String computedUnitToString = unit.toString();

	if (!expectedCompletion.equals(computedCompletion)) {
		System.out.println(Util.displayString(computedCompletion));
	}
	assertEquals(
		"invalid completion node-" + testName,
		expectedCompletion,
		computedCompletion);
		
	if(expectedParentCompletion != null) {
		if (!expectedParentCompletion.equals(computedParentCompletion)) {
			System.out.println(Util.displayString(computedParentCompletion));
		}
		assertEquals(
		"invalid completion parent node-" + testName,
		expectedParentCompletion,
		computedParentCompletion);
	}

	if (!expectedUnitToString.equals(computedUnitToString)) {
		System.out.println(Util.displayString(computedUnitToString));
	}
	assertEquals(
		"invalid completion location-"+testName,
		expectedUnitToString,
		computedUnitToString);

	if (expectedCompletionIdentifier != null){
		char[] chars = ((CompletionScanner)parser.scanner).completionIdentifier;
		String computedCompletionIdentifier = chars == null ? NONE : new String(chars);
		if (!expectedCompletionIdentifier.equals(computedCompletionIdentifier)) {
			System.out.println(Util.displayString(computedCompletionIdentifier));
		}
		assertEquals(
			"invalid completion identifier-" + testName,
			expectedCompletionIdentifier,
			computedCompletionIdentifier);
	}
	if (expectedReplacedSource != null){
		char[] chars = null;
		if (parser.assistNode != null){
			chars = CharOperation.subarray(
				parser.scanner.source, 
				parser.assistNode.sourceStart, 
				parser.assistNode.sourceEnd + 1);
		} else {
			if (parser.assistIdentifier() != null){
				if (((CompletionScanner)parser.scanner).completedIdentifierEnd 
					>= ((CompletionScanner)parser.scanner).completedIdentifierStart){
					chars = CharOperation.subarray(
						parser.scanner.source, 
						((CompletionScanner)parser.scanner).completedIdentifierStart, 
						((CompletionScanner)parser.scanner).completedIdentifierEnd + 1);
				}
			}
		}
		String computedReplacedSource  = chars == null ? NONE : new String(chars);
		if (!expectedReplacedSource.equals(computedReplacedSource)) {
			System.out.println(Util.displayString(computedReplacedSource));
		}
		assertEquals(
			"invalid replaced source-" + testName,
			expectedReplacedSource,
			computedReplacedSource);
		if (expectedReplacedSource.length() == 0) {
			assertEquals(
				"invalid insertion point-" + testName,
				cursorLocation + 1, 
				parser.assistNode.sourceStart);
		}
	}
	if (expectedLabels != null) {
//		int length = (parser.labels == null) ? 0 : parser.labels.length;
//		assertEquals("invalid number of labels-" + testName, expectedLabels.length, length);
//		for (int i = 0; i < length; i++) {
//			String label = new String(parser.labels[i]);
//			assertEquals("invalid label-" + testName, expectedLabels[i], label);
//		}
	}
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source, 
		int cursorLocation, 
		String expectedCompletion, 
		String expectedUnitToString, 
		String expectedCompletionIdentifier, 
		String expectedReplacedSource, 
		String testName) {

	this.checkMethodParse(
		source, 
		cursorLocation, 
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source, 
		int cursorLocation, 
		String expectedCompletion,
		String expectedParentCompletion, 
		String expectedUnitToString, 
		String expectedCompletionIdentifier, 
		String expectedReplacedSource, 
		String testName) {

	this.checkMethodParse(
		source, 
		cursorLocation, 
		expectedCompletion,
		expectedParentCompletion,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName);
}
/*
 * Parse a method with completionNode check
 */
public void checkMethodParse(
		char[] source, 
		int cursorLocation, 
		String expectedCompletion, 
		String expectedUnitToString, 
		String expectedCompletionIdentifier, 
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {
		
	this.checkMethodParse(
		source, 
		cursorLocation, 
		expectedCompletion,
		null,
		expectedUnitToString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		expectedLabels,
		testName);
}
/*
 * Returns the method, the constructor or the type declaring the initializer
 * at the cursor location in the given type.
 * Returns null if not found.
 */
private ASTNode findMethod(TypeDeclaration type, int cursorLocation) {
	if (type.methods != null) {
		for (int i = 0; i < type.methods.length; i++) {
			AbstractMethodDeclaration method = type.methods[i];
			if (method.declarationSourceStart <= cursorLocation && (cursorLocation <= method.declarationSourceEnd || method.declarationSourceEnd == 0)) {
				return method;
			}
		}
	}
	if (type.memberTypes != null) {
		for (int i = 0; i < type.memberTypes.length; i++) {
			TypeDeclaration memberType = type.memberTypes[i];
			ASTNode method = findMethod(memberType, cursorLocation);
			if (method != null) {
				return method;
			}
		}
	}
	if (type.fields != null) {
		for (int i = 0; i < type.fields.length; i++) {
			FieldDeclaration field = type.fields[i];
			if (field instanceof Initializer && field.declarationSourceStart <= cursorLocation && (cursorLocation <= field.declarationSourceEnd || field.declarationSourceEnd == 0)) {
				return type;
			}
		}
	}
	return null;
}
/**
 * Runs the given test that checks that diet completion parsing returns the given completion.
 */
protected void runTestCheckDietParse(
		String compilationUnit, 
		String completeBehind, 
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {
			
	int cursorLocation = compilationUnit.indexOf(completeBehind) + completeBehind.length() - 1;
	this.checkDietParse(
		compilationUnit.toCharArray(), 
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		testName); 
}
/**
 * Runs the given test that checks that method completion parsing returns the given completion.
 */
protected void runTestCheckMethodParse(
		String compilationUnit, 
		String completeBehind, 
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String[] expectedLabels,
		String testName) {
			
	int completeBehindStart = compilationUnit.indexOf(completeBehind);
	assertTrue("completeBehind string not found", completeBehindStart >= 0);
	int cursorLocation = completeBehindStart + completeBehind.length() - 1;
	this.checkMethodParse(
		compilationUnit.toCharArray(), 
		cursorLocation,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		expectedLabels,
		testName); 
}
/**
 * Runs the given test that checks that method completion parsing returns the given completion.
 */
protected void runTestCheckMethodParse(
		String compilationUnit, 
		String completeBehind, 
		String expectedCompletionNodeToString,
		String expectedUnitDisplayString,
		String expectedCompletionIdentifier,
		String expectedReplacedSource,
		String testName) {
			
	this.runTestCheckMethodParse(
		compilationUnit,
		completeBehind,
		expectedCompletionNodeToString,
		expectedUnitDisplayString,
		expectedCompletionIdentifier,
		expectedReplacedSource,
		null,
		testName); 
}
}

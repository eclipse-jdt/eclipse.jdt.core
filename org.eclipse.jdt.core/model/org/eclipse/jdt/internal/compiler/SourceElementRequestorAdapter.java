/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.core.compiler.IProblem;

public class SourceElementRequestorAdapter implements ISourceElementRequestor {

	/**
	 * @see ISourceElementRequestor#acceptConstructorReference(char[], int, int)
	 */
	public void acceptConstructorReference(
		char[] typeName,
		int argCount,
		int sourcePosition) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptFieldReference(char[], int)
	 */
	public void acceptFieldReference(char[] fieldName, int sourcePosition) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptImport(int, int, char[], boolean, int)
	 */
	public void acceptImport(
		int declarationStart,
		int declarationEnd,
		char[] name,
		boolean onDemand,
		int modifiers) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptLineSeparatorPositions(int[])
	 */
	public void acceptLineSeparatorPositions(int[] positions) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptMethodReference(char[], int, int)
	 */
	public void acceptMethodReference(
		char[] methodName,
		int argCount,
		int sourcePosition) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptPackage(int, int, char[])
	 */
	public void acceptPackage(
		int declarationStart,
		int declarationEnd,
		char[] name) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptProblem(IProblem)
	 */
	public void acceptProblem(IProblem problem) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptTypeReference(char[][], int, int)
	 */
	public void acceptTypeReference(
		char[][] typeName,
		int sourceStart,
		int sourceEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptTypeReference(char[], int)
	 */
	public void acceptTypeReference(char[] typeName, int sourcePosition) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptUnknownReference(char[][], int, int)
	 */
	public void acceptUnknownReference(
		char[][] name,
		int sourceStart,
		int sourceEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#acceptUnknownReference(char[], int)
	 */
	public void acceptUnknownReference(char[] name, int sourcePosition) {
		// default implementation: do nothing
	}

	public void enterClass(int declarationStart, int modifiers, char[] name,
			int nameSourceStart, int nameSourceEnd, char[] superclass,
			char[][] superinterfaces) {
		enterClass(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, superclass, superinterfaces, null, null);
	}
	
	// TODO remove once JavaParseTreeBuilder subclasses are removed
	public void enterClass(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[] superclass,
		char[][] superinterfaces,
		char[][] typeParameterNames,
		char[][][] typeParameterBounds) {
		
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#enterCompilationUnit()
	 */
	public void enterCompilationUnit() {
		// default implementation: do nothing
	}

	public void enterConstructor(int declarationStart, int modifiers,
			char[] name, int nameSourceStart, int nameSourceEnd,
			char[][] parameterTypes, char[][] parameterNames,
			char[][] exceptionTypes) {
		enterConstructor(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, parameterNames, parameterNames, exceptionTypes, null, null);

	}
	
	// TODO remove once JavaParseTreeBuilder subclasses are removed
	public void enterConstructor(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes,
		char[][] typeParameterNames, 
		char[][][] typeParameterBounds) {
		
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#enterField(int, int, char[], char[], int, int)
	 */
	public void enterField(
		int declarationStart,
		int modifiers,
		char[] type,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#enterInitializer(int, int)
	 */
	public void enterInitializer(int declarationStart, int modifiers) {
		// default implementation: do nothing
	}

	public void enterInterface(int declarationStart, int modifiers,
			char[] name, int nameSourceStart, int nameSourceEnd,
			char[][] superinterfaces) {
		enterInterface(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, superinterfaces, null, null);
	}
	
	// TODO remove once JavaParseTreeBuilder subclasses are removed
	public void enterInterface(
		int declarationStart,
		int modifiers,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] superinterfaces,
		char[][] typeParameterNames,
		char[][][] typeParameterBounds) {
		
		// default implementation: do nothing
	}

	public void enterMethod(int declarationStart, int modifiers,
			char[] returnType, char[] name, int nameSourceStart,
			int nameSourceEnd, char[][] parameterTypes,
			char[][] parameterNames, char[][] exceptionTypes) {
		enterMethod(declarationStart, modifiers, returnType, name, nameSourceStart, nameSourceEnd, parameterTypes, parameterNames, exceptionTypes, null, null);
		
	}
	
	// TODO remove once JavaParseTreeBuilder subclasses are removed
	public void enterMethod(
		int declarationStart,
		int modifiers,
		char[] returnType,
		char[] name,
		int nameSourceStart,
		int nameSourceEnd,
		char[][] parameterTypes,
		char[][] parameterNames,
		char[][] exceptionTypes,
		char[][] typeParameterNames, 
		char[][][] typeParameterBounds) {
		
		// default implementation: do nothing
	}

	public void enterTypeParameter(int declarationStart, char[] name,
			int nameSourceStart, int nameSourceEnd, char[][] typeParameterBounds) {
		// default implementation: do nothing
	}
	
	/**
	 * @see ISourceElementRequestor#exitClass(int)
	 */
	public void exitClass(int declarationEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitCompilationUnit(int)
	 */
	public void exitCompilationUnit(int declarationEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitConstructor(int)
	 */
	public void exitConstructor(int declarationEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitField(int, int, int)
	 */
	public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitInitializer(int)
	 */
	public void exitInitializer(int declarationEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitInterface(int)
	 */
	public void exitInterface(int declarationEnd) {
		// default implementation: do nothing
	}

	/**
	 * @see ISourceElementRequestor#exitMethod(int)
	 */
	public void exitMethod(int declarationEnd) {
		// default implementation: do nothing
	}
	
	/**
	 * @see ISourceElementRequestor#exitTypeParameter(int)
	 */
	public void exitTypeParameter(int declarationEnd) {
		// default implementation: do nothing
	}
}


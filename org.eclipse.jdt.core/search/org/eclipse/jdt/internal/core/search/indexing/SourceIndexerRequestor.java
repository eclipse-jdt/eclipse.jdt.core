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
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

/**
 * This class is used by the JavaParserIndexer. When parsing the java file, the requestor
 * recognises the java elements (methods, fields, ...) and add them to an index.
 */
public class SourceIndexerRequestor implements ISourceElementRequestor, IIndexConstants {
	SourceIndexer indexer;

	char[] packageName = CharOperation.NO_CHAR;
	char[][] enclosingTypeNames = new char[5][];
	int depth = 0;
	int methodDepth = 0;
	
public SourceIndexerRequestor(SourceIndexer indexer) {
	this.indexer = indexer;
}
/**
 * @see ISourceElementRequestor#acceptConstructorReference(char[], int, int)
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {
	this.indexer.addConstructorReference(typeName, argCount);
	int lastDot = CharOperation.lastIndexOf('.', typeName);
	if (lastDot != -1) {
		char[][] qualification = CharOperation.splitOn('.', CharOperation.subarray(typeName, 0, lastDot));
		for (int i = 0, length = qualification.length; i < length; i++) {
			this.indexer.addNameReference(qualification[i]);
		}
	}
}
/**
 * @see ISourceElementRequestor#acceptFieldReference(char[], int)
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {
	this.indexer.addFieldReference(fieldName);
}
/**
 * @see ISourceElementRequestor#acceptImport(int, int, char[], boolean, int)
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand, int modifiers) {
	char[][] qualification = CharOperation.splitOn('.', CharOperation.subarray(name, 0, CharOperation.lastIndexOf('.', name)));
	for (int i = 0, length = qualification.length; i < length; i++) {
		this.indexer.addNameReference(qualification[i]);
	}
}
/**
 * @see ISourceElementRequestor#acceptLineSeparatorPositions(int[])
 */
public void acceptLineSeparatorPositions(int[] positions) {
	// implements interface method
}
/**
 * @see ISourceElementRequestor#acceptMethodReference(char[], int, int)
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {
	this.indexer.addMethodReference(methodName, argCount);
}
/**
 * @see ISourceElementRequestor#acceptPackage(int, int, char[])
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {
	this.packageName = name;
}
/**
 * @see ISourceElementRequestor#acceptProblem(IProblem)
 */
public void acceptProblem(IProblem problem) {
	// implements interface method
}
/**
 * @see ISourceElementRequestor#acceptTypeReference(char[][], int, int)
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {
	int length = typeName.length;
	for (int i = 0; i < length - 1; i++)
		acceptUnknownReference(typeName[i], 0); // ?
	acceptTypeReference(typeName[length - 1], 0);
}
/**
 * @see ISourceElementRequestor#acceptTypeReference(char[], int)
 */
public void acceptTypeReference(char[] simpleTypeName, int sourcePosition) {
	this.indexer.addTypeReference(simpleTypeName);
}
/**
 * @see ISourceElementRequestor#acceptUnknownReference(char[][], int, int)
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {
	for (int i = 0; i < name.length; i++) {
		acceptUnknownReference(name[i], 0);
	}
}
/**
 * @see ISourceElementRequestor#acceptUnknownReference(char[], int)
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {
	this.indexer.addNameReference(name);
}
/*
 * Rebuild the proper qualification for the current source type:
 *
 * java.lang.Object ---> null
 * java.util.Hashtable$Entry --> [Hashtable]
 * x.y.A$B$C --> [A, B]
 */
public char[][] enclosingTypeNames(){

	if (depth == 0) return null;

	char[][] qualification = new char[this.depth][];
	System.arraycopy(this.enclosingTypeNames, 0, qualification, 0, this.depth);
	return qualification;
}
/**
 * @see ISourceElementRequestor#enterClass(int, int, char[], int, int, char[], char[][])
 */
public void enterClass(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[] superclass, char[][] superinterfaces) {

	// eliminate possible qualifications, given they need to be fully resolved again
	if (superclass != null){
		superclass = CharOperation.lastSegment(superclass, '.');
		
		// add implicit constructor reference to default constructor
		this.indexer.addConstructorReference(superclass, 0);
	}
	if (superinterfaces != null){
		for (int i = 0, length = superinterfaces.length; i < length; i++){
			superinterfaces[i] = CharOperation.lastSegment(superinterfaces[i], '.');
		}
	}
	char[][] typeNames;
	if (this.methodDepth > 0) {
		typeNames = ONE_ZERO_CHAR;
	} else {
		typeNames = this.enclosingTypeNames();
	}
	this.indexer.addClassDeclaration(modifiers, this.packageName, name, typeNames, superclass, superinterfaces);
	this.pushTypeName(name);
}
/**
 * @see ISourceElementRequestor#enterCompilationUnit()
 */
public void enterCompilationUnit() {
	// implements interface method
}
/**
 * @see ISourceElementRequestor#enterConstructor(int, int, char[], int, int, char[][], char[][], char[][])
 */
public void enterConstructor(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	this.indexer.addConstructorDeclaration(name, parameterTypes, exceptionTypes);
	this.methodDepth++;
}
/**
 * @see ISourceElementRequestor#enterField(int, int, char[], char[], int, int)
 */
public void enterField(int declarationStart, int modifiers, char[] type, char[] name, int nameSourceStart, int nameSourceEnd) {
	this.indexer.addFieldDeclaration(type, name);
	this.methodDepth++;
}
/**
 * @see ISourceElementRequestor#enterInitializer(int, int)
 */
public void enterInitializer(int declarationSourceStart, int modifiers) {
	this.methodDepth++;
}
/**
 * @see ISourceElementRequestor#enterInterface(int, int, char[], int, int, char[][])
 */
public void enterInterface(int declarationStart, int modifiers, char[] name, int nameSourceStart, int nameSourceEnd, char[][] superinterfaces) {
	// eliminate possible qualifications, given they need to be fully resolved again
	if (superinterfaces != null){
		for (int i = 0, length = superinterfaces.length; i < length; i++){
			superinterfaces[i] = CharOperation.lastSegment(superinterfaces[i], '.');
		}
	}	
	char[][] typeNames;
	if (this.methodDepth > 0) {
		typeNames = ONE_ZERO_CHAR;
	} else {
		typeNames = this.enclosingTypeNames();
	}
	this.indexer.addInterfaceDeclaration(modifiers, packageName, name, typeNames, superinterfaces);
	this.pushTypeName(name);	
}
/**
 * @see ISourceElementRequestor#enterMethod(int, int, char[], char[], int, int, char[][], char[][], char[][])
 */
public void enterMethod(int declarationStart, int modifiers, char[] returnType, char[] name, int nameSourceStart, int nameSourceEnd, char[][] parameterTypes, char[][] parameterNames, char[][] exceptionTypes) {
	this.indexer.addMethodDeclaration(name, parameterTypes, returnType, exceptionTypes);
	this.methodDepth++;
}
public void enterTypeParameter(int declarationStart, char[] name,
		int nameSourceStart, int nameSourceEnd, char[][] typeParameterBounds) {
	// TODO (jerome) should type parameters be indexed ?
}
/**
 * @see ISourceElementRequestor#exitClass(int)
 */
public void exitClass(int declarationEnd) {
	popTypeName();
}
/**
 * @see ISourceElementRequestor#exitCompilationUnit(int)
 */
public void exitCompilationUnit(int declarationEnd) {
	// implements interface method
}
/**
 * @see ISourceElementRequestor#exitConstructor(int)
 */
public void exitConstructor(int declarationEnd) {
	this.methodDepth--;
}
/**
 * @see ISourceElementRequestor#exitField(int, int, int)
 */
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
	this.methodDepth--;
}
/**
 * @see ISourceElementRequestor#exitInitializer(int)
 */
public void exitInitializer(int declarationEnd) {
	this.methodDepth--;
}
/**
 * @see ISourceElementRequestor#exitInterface(int)
 */
public void exitInterface(int declarationEnd) {
	popTypeName();	
}
/**
 * @see ISourceElementRequestor#exitMethod(int)
 */
public void exitMethod(int declarationEnd) {
	this.methodDepth--;
}
public void exitTypeParameter(int declarationEnd) {
	// TODO (jerome) should type parameters be indexed ?
}
public void popTypeName() {
	if (depth > 0) {
		enclosingTypeNames[--depth] = null;
	} else if (JobManager.VERBOSE) {
		// dump a trace so it can be tracked down
		try {
			enclosingTypeNames[-1] = null;
		} catch (ArrayIndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
}
public void pushTypeName(char[] typeName) {
	if (depth == enclosingTypeNames.length)
		System.arraycopy(enclosingTypeNames, 0, enclosingTypeNames = new char[depth*2][], 0, depth);
	enclosingTypeNames[depth++] = typeName;
}
}

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
package org.eclipse.jdt.internal.core;

import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ISourceElementRequestor;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.core.util.ReferenceInfoAdapter;

/**
 * A requestor for the fuzzy parser, used to compute the children of an ICompilationUnit.
 */
public class CompilationUnitStructureRequestor extends ReferenceInfoAdapter implements ISourceElementRequestor {

	/**
	 * The handle to the compilation unit being parsed
	 */
	protected ICompilationUnit unit;

	/**
	 * The info object for the compilation unit being parsed
	 */
	protected CompilationUnitElementInfo unitInfo;

	/**
	 * The import container info - null until created
	 */
	protected JavaElementInfo importContainerInfo = null;

	/**
	 * Hashtable of children elements of the compilation unit.
	 * Children are added to the table as they are found by
	 * the parser. Keys are handles, values are corresponding
	 * info objects.
	 */
	protected Map newElements;

	/**
	 * Stack of parent scope info objects. The info on the
	 * top of the stack is the parent of the next element found.
	 * For example, when we locate a method, the parent info object
	 * will be the type the method is contained in.
	 */
	protected Stack infoStack;

	/**
	 * Stack of parent handles, corresponding to the info stack. We
	 * keep both, since info objects do not have back pointers to
	 * handles.
	 */
	protected Stack handleStack;

	/**
	 * The name of the source file being parsed.
	 */
	protected char[] sourceFileName= null;

	/**
	 * The dot-separated name of the package the compilation unit
	 * is contained in - based on the package statement in the
	 * compilation unit, and initialized by #acceptPackage.
	 * Initialized to <code>null</code> for the default package.
	 */
	protected char[] packageName= null;

	/**
	 * The number of references reported thus far. Used to
	 * expand the arrays of reference kinds and names.
	 */
	protected int referenceCount= 0;

	/**
	 * Problem requestor which will get notified of discovered problems
	 */
	protected boolean hasSyntaxErrors = false;
	
	/*
	 * The parser this requestor is using.
	 */
	protected Parser parser;
	
	/**
	 * Empty collections used for efficient initialization
	 */
	protected static String[] NO_STRINGS = new String[0];
	protected static byte[] NO_BYTES= new byte[]{};

	protected HashtableOfObject fieldRefCache;
	protected HashtableOfObject messageRefCache;
	protected HashtableOfObject typeRefCache;
	protected HashtableOfObject unknownRefCache;

protected CompilationUnitStructureRequestor(ICompilationUnit unit, CompilationUnitElementInfo unitInfo, Map newElements) {
	this.unit = unit;
	this.unitInfo = unitInfo;
	this.newElements = newElements;
	this.sourceFileName= unit.getElementName().toCharArray();
} 
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[] name, boolean onDemand, int modifiers) {
	JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
	JavaElement parentHandle= (JavaElement) this.handleStack.peek();
	if (!(parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT)) {
		Assert.isTrue(false); // Should not happen
	}

	ICompilationUnit parentCU= (ICompilationUnit)parentHandle;
	//create the import container and its info
	ImportContainer importContainer= (ImportContainer)parentCU.getImportContainer();
	if (this.importContainerInfo == null) {
		this.importContainerInfo= new JavaElementInfo();
		this.importContainerInfo.setIsStructureKnown(true);
		parentInfo.addChild(importContainer);
		this.newElements.put(importContainer, this.importContainerInfo);
	}
	
	// tack on the '.*' if it is onDemand
	String importName;
	if (onDemand) {
		importName= new String(name) + ".*"; //$NON-NLS-1$
	} else {
		importName= new String(name);
	}
	
	ImportDeclaration handle = new ImportDeclaration(importContainer, importName);
	resolveDuplicates(handle);
	
	ImportDeclarationElementInfo info = new ImportDeclarationElementInfo();
	info.setSourceRangeStart(declarationStart);
	info.setSourceRangeEnd(declarationEnd);
	info.setFlags(modifiers);
	info.setName(name); // no trailing * if onDemand
	info.setOnDemand(onDemand);

	this.importContainerInfo.addChild(handle);
	this.newElements.put(handle, info);
}
/*
 * Table of line separator position. This table is passed once at the end
 * of the parse action, so as to allow computation of normalized ranges.
 *
 * A line separator might corresponds to several characters in the source,
 * 
 */
public void acceptLineSeparatorPositions(int[] positions) {
	// ignore line separator positions
}
/**
 * @see ISourceElementRequestor
 */
public void acceptPackage(int declarationStart, int declarationEnd, char[] name) {

		JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
		JavaElement parentHandle= (JavaElement) this.handleStack.peek();
		PackageDeclaration handle = null;
		this.packageName= name;
		
		if (parentHandle.getElementType() == IJavaElement.COMPILATION_UNIT) {
			handle = new PackageDeclaration((CompilationUnit) parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceRefElementInfo info = new SourceRefElementInfo();
		info.setSourceRangeStart(declarationStart);
		info.setSourceRangeEnd(declarationEnd);

		parentInfo.addChild(handle);
		this.newElements.put(handle, info);

}
public void acceptProblem(IProblem problem) {
	if ((problem.getID() & IProblem.Syntax) != 0){
		this.hasSyntaxErrors = true;
	}
}
/**
 * Convert these type names to signatures.
 * @see Signature
 */
/* default */ static String[] convertTypeNamesToSigs(char[][] typeNames) {
	if (typeNames == null)
		return NO_STRINGS;
	int n = typeNames.length;
	if (n == 0)
		return NO_STRINGS;
	String[] typeSigs = new String[n];
	for (int i = 0; i < n; ++i) {
		typeSigs[i] = Signature.createTypeSignature(typeNames[i], false);
	}
	return typeSigs;
}
/**
 * @see ISourceElementRequestor
 */
public void enterClass(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, superclass, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterCompilationUnit() {
	this.infoStack = new Stack();
	this.handleStack= new Stack();
	this.infoStack.push(this.unitInfo);
	this.handleStack.push(this.unit);
}
/**
 * @see ISourceElementRequestor
 */
public void enterConstructor(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, null, name, nameSourceStart,
			nameSourceEnd,	parameterTypes, parameterNames, exceptionTypes, true);
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(
	int declarationStart,
	int modifiers,
	char[] type,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) this.infoStack.peek();
		JavaElement parentHandle= (JavaElement) this.handleStack.peek();
		SourceField handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceField(parentHandle, new String(name));
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceFieldElementInfo info = new SourceFieldElementInfo();
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setSourceRangeStart(declarationStart);
		info.setFlags(modifiers);
		info.setTypeName(type);

		parentInfo.addChild(handle);
		this.newElements.put(handle, info);

		this.infoStack.push(info);
		this.handleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void enterInitializer(
	int declarationSourceStart,
	int modifiers) {
		JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
		JavaElement parentHandle= (JavaElement) this.handleStack.peek();
		Initializer handle = null;
		
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new Initializer(parentHandle, 1);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		InitializerElementInfo info = new InitializerElementInfo();
		info.setSourceRangeStart(declarationSourceStart);
		info.setFlags(modifiers);

		parentInfo.addChild(handle);
		this.newElements.put(handle, info);

		this.infoStack.push(info);
		this.handleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void enterInterface(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] superinterfaces) {

	enterType(declarationStart, modifiers, name, nameSourceStart, nameSourceEnd, null, superinterfaces);

}
/**
 * @see ISourceElementRequestor
 */
public void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes) {

		enterMethod(declarationStart, modifiers, returnType, name, nameSourceStart,
			nameSourceEnd, parameterTypes, parameterNames, exceptionTypes, false);
}
/**
 * @see ISourceElementRequestor
 */
protected void enterMethod(
	int declarationStart,
	int modifiers,
	char[] returnType,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[][] parameterTypes,
	char[][] parameterNames,
	char[][] exceptionTypes,
	boolean isConstructor) {

		SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) this.infoStack.peek();
		JavaElement parentHandle= (JavaElement) this.handleStack.peek();
		SourceMethod handle = null;

		// translate nulls to empty arrays
		if (parameterTypes == null) {
			parameterTypes= CharOperation.NO_CHAR_CHAR;
		}
		if (parameterNames == null) {
			parameterNames= CharOperation.NO_CHAR_CHAR;
		}
		if (exceptionTypes == null) {
			exceptionTypes= CharOperation.NO_CHAR_CHAR;
		}
		
		String[] parameterTypeSigs = convertTypeNamesToSigs(parameterTypes);
		if (parentHandle.getElementType() == IJavaElement.TYPE) {
			handle = new SourceMethod(parentHandle, new String(name), parameterTypeSigs);
		}
		else {
			Assert.isTrue(false); // Should not happen
		}
		resolveDuplicates(handle);
		
		SourceMethodElementInfo info = new SourceMethodElementInfo();
		info.setSourceRangeStart(declarationStart);
		int flags = modifiers;
		info.setName(name);
		info.setNameSourceStart(nameSourceStart);
		info.setNameSourceEnd(nameSourceEnd);
		info.setConstructor(isConstructor);
		info.setFlags(flags);
		info.setArgumentNames(parameterNames);
		info.setArgumentTypeNames(parameterTypes);
		info.setReturnType(returnType == null ? new char[]{'v', 'o','i', 'd'} : returnType);
		info.setExceptionTypeNames(exceptionTypes);
		parentInfo.addChild(handle);
		this.newElements.put(handle, info);
		this.infoStack.push(info);
		this.handleStack.push(handle);
}
/**
 * Common processing for classes and interfaces.
 */
protected void enterType(
	int declarationStart,
	int modifiers,
	char[] name,
	int nameSourceStart,
	int nameSourceEnd,
	char[] superclass,
	char[][] superinterfaces) {

	JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
	JavaElement parentHandle= (JavaElement) this.handleStack.peek();
	String nameString= new String(name);
	SourceType handle = handle = new SourceType(parentHandle, nameString); //NB: occurenceCount is computed in resolveDuplicates
	resolveDuplicates(handle);
	
	SourceTypeElementInfo info = new SourceTypeElementInfo();
	info.setHandle(handle);
	info.setSourceRangeStart(declarationStart);
	info.setFlags(modifiers);
	info.setName(name);
	info.setNameSourceStart(nameSourceStart);
	info.setNameSourceEnd(nameSourceEnd);
	info.setSuperclassName(superclass);
	info.setSuperInterfaceNames(superinterfaces);
	info.setSourceFileName(this.sourceFileName);
	info.setPackageName(this.packageName);
	parentInfo.addChild(handle);
	this.newElements.put(handle, info);
	this.infoStack.push(info);
	this.handleStack.push(handle);
}
public void enterTypeParameter(int declarationStart, char[] name, int nameSourceStart, int nameSourceEnd, char[][] typeParameterBounds) {
	JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
	JavaElement parentHandle = (JavaElement) this.handleStack.peek();
	String nameString = new String(name);
	TypeParameter handle = handle = new TypeParameter(parentHandle, nameString); //NB: occurenceCount is computed in resolveDuplicates
	resolveDuplicates(handle);
	
	TypeParameterElementInfo info = new TypeParameterElementInfo();
	info.setSourceRangeStart(declarationStart);
	info.nameStart = nameSourceStart;
	info.nameEnd = nameSourceEnd;
	info.bounds = typeParameterBounds;
	parentInfo.addChild(handle);
	this.newElements.put(handle, info);
	this.infoStack.push(info);
	this.handleStack.push(handle);
}
/**
 * @see ISourceElementRequestor
 */
public void exitClass(int declarationEnd) {

	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitCompilationUnit(int declarationEnd) {
	this.unitInfo.setSourceLength(declarationEnd + 1);

	// determine if there were any parsing errors
	this.unitInfo.setIsStructureKnown(!this.hasSyntaxErrors);
}
/**
 * @see ISourceElementRequestor
 */
public void exitConstructor(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitField(int initializationStart, int declarationEnd, int declarationSourceEnd) {
	SourceFieldElementInfo info = (SourceFieldElementInfo) this.infoStack.pop();
	info.setSourceRangeEnd(declarationSourceEnd);
	
	// remember initializer source if field is a constant
	if (initializationStart != -1) {
		int flags = info.flags;
		Object typeInfo;
		if (Flags.isStatic(flags) && Flags.isFinal(flags)
				|| ((typeInfo = this.infoStack.peek()) instanceof SourceTypeElementInfo
					 && (Flags.isInterface(((SourceTypeElementInfo)typeInfo).flags)))) {
			int length = declarationEnd - initializationStart;
			if (length > 0) {
				char[] initializer = new char[length];
				System.arraycopy(this.parser.scanner.source, initializationStart, initializer, 0, length);
				info.initializationSource = initializer;
			}
		}
	}
	this.handleStack.pop();
}
/**
 * @see ISourceElementRequestor
 */
public void exitInitializer(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitInterface(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * common processing for classes and interfaces
 */
protected void exitMember(int declarationEnd) {
	SourceRefElementInfo info = (SourceRefElementInfo) this.infoStack.pop();
	info.setSourceRangeEnd(declarationEnd);
	this.handleStack.pop();
}
/**
 * @see ISourceElementRequestor
 */
public void exitMethod(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * @see ISourceElementRequestor
 */
public void exitTypeParameter(int declarationEnd) {
	exitMember(declarationEnd);
}
/**
 * Resolves duplicate handles by incrementing the occurrence count
 * of the handle being created until there is no conflict.
 */
protected void resolveDuplicates(SourceRefElement handle) {
	while (this.newElements.containsKey(handle)) {
		handle.occurrenceCount++;
	}
}
}

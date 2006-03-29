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
package org.eclipse.jdt.internal.core;

import java.util.Map;
import java.util.Stack;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
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
} 
/**
 * @see ISourceElementRequestor
 */
public void acceptImport(int declarationStart, int declarationEnd, char[][] tokens, boolean onDemand, int modifiers) {
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
		parentInfo.addChild(importContainer);
		this.newElements.put(importContainer, this.importContainerInfo);
	}
	
	String elementName = JavaModelManager.getJavaModelManager().intern(new String(CharOperation.concatWith(tokens, '.')));
	ImportDeclaration handle = new ImportDeclaration(importContainer, elementName, onDemand);
	resolveDuplicates(handle);
	
	ImportDeclarationElementInfo info = new ImportDeclarationElementInfo();
	info.setSourceRangeStart(declarationStart);
	info.setSourceRangeEnd(declarationEnd);
	info.setFlags(modifiers);

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
public void acceptProblem(CategorizedProblem problem) {
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
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	String[] typeSigs = new String[n];
	for (int i = 0; i < n; ++i) {
		typeSigs[i] = manager.intern(Signature.createTypeSignature(typeNames[i], false));
	}
	return typeSigs;
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
public void enterConstructor(MethodInfo methodInfo) {
	enterMethod(methodInfo);
}
/**
 * @see ISourceElementRequestor
 */
public void enterField(FieldInfo fieldInfo) {

	SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) this.infoStack.peek();
	JavaElement parentHandle= (JavaElement) this.handleStack.peek();
	SourceField handle = null;
	if (parentHandle.getElementType() == IJavaElement.TYPE) {
		String fieldName = JavaModelManager.getJavaModelManager().intern(new String(fieldInfo.name));
		handle = new SourceField(parentHandle, fieldName);
	}
	else {
		Assert.isTrue(false); // Should not happen
	}
	resolveDuplicates(handle);
	
	SourceFieldElementInfo info = new SourceFieldElementInfo();
	info.setNameSourceStart(fieldInfo.nameSourceStart);
	info.setNameSourceEnd(fieldInfo.nameSourceEnd);
	info.setSourceRangeStart(fieldInfo.declarationStart);
	info.setFlags(fieldInfo.modifiers);
	char[] typeName = JavaModelManager.getJavaModelManager().intern(fieldInfo.type);
	info.setTypeName(typeName);
	
	this.unitInfo.addAnnotationPositions(handle, fieldInfo.annotationPositions);

	parentInfo.addChild(handle);
	parentInfo.addCategories(handle, fieldInfo.categories);
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
public void enterMethod(MethodInfo methodInfo) {

	SourceTypeElementInfo parentInfo = (SourceTypeElementInfo) this.infoStack.peek();
	JavaElement parentHandle= (JavaElement) this.handleStack.peek();
	SourceMethod handle = null;

	// translate nulls to empty arrays
	if (methodInfo.parameterTypes == null) {
		methodInfo.parameterTypes= CharOperation.NO_CHAR_CHAR;
	}
	if (methodInfo.parameterNames == null) {
		methodInfo.parameterNames= CharOperation.NO_CHAR_CHAR;
	}
	if (methodInfo.exceptionTypes == null) {
		methodInfo.exceptionTypes= CharOperation.NO_CHAR_CHAR;
	}
	
	String[] parameterTypeSigs = convertTypeNamesToSigs(methodInfo.parameterTypes);
	if (parentHandle.getElementType() == IJavaElement.TYPE) {
		String selector = JavaModelManager.getJavaModelManager().intern(new String(methodInfo.name));
		handle = new SourceMethod(parentHandle, selector, parameterTypeSigs);
	}
	else {
		Assert.isTrue(false); // Should not happen
	}
	resolveDuplicates(handle);
	
	SourceMethodElementInfo info;
	if (methodInfo.isConstructor)
		info = new SourceConstructorInfo();
	else if (methodInfo.isAnnotation)
		info = new SourceAnnotationMethodInfo();
	else
		info = new SourceMethodInfo();
	info.setSourceRangeStart(methodInfo.declarationStart);
	int flags = methodInfo.modifiers;
	info.setNameSourceStart(methodInfo.nameSourceStart);
	info.setNameSourceEnd(methodInfo.nameSourceEnd);
	info.setFlags(flags);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	char[][] parameterNames = methodInfo.parameterNames;
	for (int i = 0, length = parameterNames.length; i < length; i++)
		parameterNames[i] = manager.intern(parameterNames[i]);
	info.setArgumentNames(parameterNames);
	char[] returnType = methodInfo.returnType == null ? new char[]{'v', 'o','i', 'd'} : methodInfo.returnType;
	info.setReturnType(manager.intern(returnType));
	char[][] exceptionTypes = methodInfo.exceptionTypes;
	info.setExceptionTypeNames(exceptionTypes);
	for (int i = 0, length = exceptionTypes.length; i < length; i++)
		exceptionTypes[i] = manager.intern(exceptionTypes[i]);
	this.unitInfo.addAnnotationPositions(handle, methodInfo.annotationPositions);
	parentInfo.addChild(handle);
	parentInfo.addCategories(handle, methodInfo.categories);
	this.newElements.put(handle, info);
	this.infoStack.push(info);
	this.handleStack.push(handle);

	if (methodInfo.typeParameters != null) {
		for (int i = 0, length = methodInfo.typeParameters.length; i < length; i++) {
			TypeParameterInfo typeParameterInfo = methodInfo.typeParameters[i];
			enterTypeParameter(typeParameterInfo);
			exitMember(typeParameterInfo.declarationEnd);
		}
	}
}
/**
 * @see ISourceElementRequestor
 */
public void enterType(TypeInfo typeInfo) {

	JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
	JavaElement parentHandle= (JavaElement) this.handleStack.peek();
	String nameString= new String(typeInfo.name);
	SourceType handle = new SourceType(parentHandle, nameString); //NB: occurenceCount is computed in resolveDuplicates
	resolveDuplicates(handle);
	
	SourceTypeElementInfo info = new SourceTypeElementInfo();
	info.setHandle(handle);
	info.setSourceRangeStart(typeInfo.declarationStart);
	info.setFlags(typeInfo.modifiers);
	info.setNameSourceStart(typeInfo.nameSourceStart);
	info.setNameSourceEnd(typeInfo.nameSourceEnd);
	JavaModelManager manager = JavaModelManager.getJavaModelManager();
	char[] superclass = typeInfo.superclass;
	info.setSuperclassName(superclass == null ? null : manager.intern(superclass));
	char[][] superinterfaces = typeInfo.superinterfaces;
	for (int i = 0, length = superinterfaces == null ? 0 : superinterfaces.length; i < length; i++)
		superinterfaces[i] = manager.intern(superinterfaces[i]);
	info.setSuperInterfaceNames(superinterfaces);
	info.addCategories(handle, typeInfo.categories);
	if (parentHandle.getElementType() == IJavaElement.TYPE)
		((SourceTypeElementInfo) parentInfo).addCategories(handle, typeInfo.categories);
	parentInfo.addChild(handle);
	this.unitInfo.addAnnotationPositions(handle, typeInfo.annotationPositions);
	this.newElements.put(handle, info);
	this.infoStack.push(info);
	this.handleStack.push(handle);
	
	if (typeInfo.typeParameters != null) {
		for (int i = 0, length = typeInfo.typeParameters.length; i < length; i++) {
			TypeParameterInfo typeParameterInfo = typeInfo.typeParameters[i];
			enterTypeParameter(typeParameterInfo);
			exitMember(typeParameterInfo.declarationEnd);
		}
	}
}
protected void enterTypeParameter(TypeParameterInfo typeParameterInfo) {
	JavaElementInfo parentInfo = (JavaElementInfo) this.infoStack.peek();
	JavaElement parentHandle = (JavaElement) this.handleStack.peek();
	String nameString = new String(typeParameterInfo.name);
	TypeParameter handle = new TypeParameter(parentHandle, nameString); //NB: occurenceCount is computed in resolveDuplicates
	resolveDuplicates(handle);
	
	TypeParameterElementInfo info = new TypeParameterElementInfo();
	info.setSourceRangeStart(typeParameterInfo.declarationStart);
	info.nameStart = typeParameterInfo.nameSourceStart;
	info.nameEnd = typeParameterInfo.nameSourceEnd;
	info.bounds = typeParameterInfo.bounds;
	if (parentInfo instanceof SourceTypeElementInfo) {
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) parentInfo;
		ITypeParameter[] typeParameters = elementInfo.typeParameters;
		int length = typeParameters.length;
		System.arraycopy(typeParameters, 0, typeParameters = new ITypeParameter[length+1], 0, length);
		typeParameters[length] = handle;
		elementInfo.typeParameters = typeParameters;
	} else {
		SourceMethodElementInfo elementInfo = (SourceMethodElementInfo) parentInfo;
		ITypeParameter[] typeParameters = elementInfo.typeParameters;
		int length = typeParameters.length;
		System.arraycopy(typeParameters, 0, typeParameters = new ITypeParameter[length+1], 0, length);
		typeParameters[length] = handle;
		elementInfo.typeParameters = typeParameters;
	}
	this.unitInfo.addAnnotationPositions(handle, typeParameterInfo.annotationPositions);
	this.newElements.put(handle, info);
	this.infoStack.push(info);
	this.handleStack.push(handle);
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
public void exitMethod(int declarationEnd, int defaultValueStart, int defaultValueEnd) {
	SourceMethodElementInfo info = (SourceMethodElementInfo) this.infoStack.pop();
	info.setSourceRangeEnd(declarationEnd);
	
	// remember default value of annotation method
	if (info.isAnnotationMethod()) {
		SourceAnnotationMethodInfo annotationMethodInfo = (SourceAnnotationMethodInfo) info;
		annotationMethodInfo.defaultValueStart = defaultValueStart;
		annotationMethodInfo.defaultValueEnd = defaultValueEnd;
	}
	this.handleStack.pop();
}
/**
 * @see ISourceElementRequestor
 */
public void exitType(int declarationEnd) {

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

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

import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

/** 
 * Element info for an IType element that originated from source. 
 */
public class SourceTypeElementInfo extends MemberElementInfo implements ISourceType {

	protected static final ISourceImport[] NO_IMPORTS = new ISourceImport[0];
	protected static final InitializerElementInfo[] NO_INITIALIZERS = new InitializerElementInfo[0];
	protected static final ISourceField[] NO_FIELDS = new ISourceField[0];
	protected static final ISourceMethod[] NO_METHODS = new ISourceMethod[0];
	protected static final ISourceType[] NO_TYPES = new ISourceType[0];
	/**
	 * The name of the superclass for this type. This name
	 * is fully qualified for binary types and is NOT
	 * fully qualified for source types.
	 */
	protected char[] superclassName;
	
	/**
	 * The names of the interfaces this type implements or
	 * extends. These names are fully qualified in the case
	 * of a binary type, and are NOT fully qualified in the
	 * case of a source type
	 */
	protected char[][] superInterfaceNames;
	
	/**
	 * The name of the source file this type is declared in.
	 */
	protected char[] sourceFileName;

	/**
	 * The name of the package this type is contained in.
	 */
	protected char[] packageName;

	/**
	 * The infos of the imports in this type's compilation unit
	 */
	private ISourceImport[] imports;
	
	/**
	 * Backpointer to my type handle - useful for translation
	 * from info to handle.
	 */
	protected IType handle = null;

	/**
	 * Signatures of type parameters (for generic types)
	 * 
	 */
	protected char[][] typeParameterNames;
	protected char[][][] typeParameterBounds;
	protected char[][] typeParameterSignatures;
	
/**
 * Returns the ISourceType that is the enclosing type for this
 * type, or <code>null</code> if this type is a top level type.
 */
public ISourceType getEnclosingType() {
	IJavaElement parent= this.handle.getParent();
	if (parent != null && parent.getElementType() == IJavaElement.TYPE) {
		try {
			return (ISourceType)((JavaElement)parent).getElementInfo();
		} catch (JavaModelException e) {
			return null;
		}
	} else {
		return null;
	}
}
/**
 * @see ISourceType
 */
public ISourceField[] getFields() {
	int length = this.children.length;
	if (length == 0) return NO_FIELDS;
	ISourceField[] fields = new ISourceField[length];
	int fieldIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceField) {
			try {
				ISourceField field = (ISourceField)((SourceField)child).getElementInfo();
				fields[fieldIndex++] = field;
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	if (fieldIndex == 0) return NO_FIELDS;
	System.arraycopy(fields, 0, fields = new ISourceField[fieldIndex], 0, fieldIndex);
	return fields;
}
/**
 * @see ISourceType
 */
public char[] getFileName() {
	return this.sourceFileName;
}
/**
 * Returns the handle for this type info
 */
public IType getHandle() {
	return this.handle;
}
/**
 * @see ISourceType
 */
public ISourceImport[] getImports() {
	if (this.imports == null) {
		try {
			IImportDeclaration[] importDeclarations = this.handle.getCompilationUnit().getImports();
			int length = importDeclarations.length;
			if (length == 0) {
				this.imports = NO_IMPORTS;
			} else {
				ISourceImport[] sourceImports = new ISourceImport[length];
				for (int i = 0; i < length; i++) {
					sourceImports[i] = (ImportDeclarationElementInfo)((ImportDeclaration)importDeclarations[i]).getElementInfo();
				}
				this.imports = sourceImports; // only commit at the end, once completed (bug 36854)
			}
		} catch (JavaModelException e) {
			this.imports = NO_IMPORTS;
		}
	}
	return this.imports;
}
/*
 * Returns the InitializerElementInfos for this type.
 * Returns an empty array if none.
 */
public InitializerElementInfo[] getInitializers() {
	int length = this.children.length;
	if (length == 0) return NO_INITIALIZERS;
	InitializerElementInfo[] initializers = new InitializerElementInfo[length];
	int initializerIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof Initializer) {
			try {
				InitializerElementInfo initializer = (InitializerElementInfo)((Initializer)child).getElementInfo();
				initializers[initializerIndex++] = initializer;
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	if (initializerIndex == 0) return NO_INITIALIZERS;
	System.arraycopy(initializers, 0, initializers = new InitializerElementInfo[initializerIndex], 0, initializerIndex);
	return initializers;
}
/**
 * @see ISourceType
 */
public char[][] getInterfaceNames() {
	if (this.handle.getElementName().length() == 0) { // if anonymous type
		return null;
	}
	return this.superInterfaceNames;
}
/**
 * @see ISourceType
 */
public ISourceType[] getMemberTypes() {
	int length = this.children.length;
	if (length == 0) return NO_TYPES;
	ISourceType[] memberTypes = new ISourceType[length];
	int typeIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceType) {
			try {
				ISourceType type = (ISourceType)((SourceType)child).getElementInfo();
				memberTypes[typeIndex++] = type;
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	if (typeIndex == 0) return NO_TYPES;
	System.arraycopy(memberTypes, 0, memberTypes = new ISourceType[typeIndex], 0, typeIndex);
	return memberTypes;
}
/**
 * @see ISourceType
 */
public ISourceMethod[] getMethods() {
	int length = this.children.length;
	if (length == 0) return NO_METHODS;
	ISourceMethod[] methods = new ISourceMethod[length];
	int methodIndex = 0;
	for (int i = 0; i < length; i++) {
		IJavaElement child = this.children[i];
		if (child instanceof SourceMethod) {
			try {
				ISourceMethod method = (ISourceMethod)((SourceMethod)child).getElementInfo();
				methods[methodIndex++] = method;
			} catch (JavaModelException e) {
				// ignore
			}
		}
	}
	if (methodIndex == 0) return NO_METHODS;
	System.arraycopy(methods, 0, methods = new ISourceMethod[methodIndex], 0, methodIndex);
	return methods;
}
/**
 * @see ISourceType
 */
public char[] getPackageName() {
	return this.packageName;
}
/**
 * @see ISourceType
 */
public char[] getSuperclassName() {
	if (this.handle.getElementName().length() == 0) { // if anonymous type
		char[][] interfaceNames = this.superInterfaceNames;	
		if (interfaceNames != null && interfaceNames.length > 0) {
			return interfaceNames[0];
		}
	} 
	return this.superclassName;
}
public char[][] getTypeParameterNames() {
	return this.typeParameterNames;
}
public char[][][] getTypeParameterBounds() {
	return this.typeParameterBounds;
}
public char[][] getTypeParameterSignatures() {
	if (this.typeParameterSignatures == null) {
		if (this.typeParameterNames != null) {
			int length = this.typeParameterNames.length;
			this.typeParameterSignatures = new char[length][];
			for (int i = 0; i < length; i++) {
				char[][] bounds = this.typeParameterBounds[i];
				if (bounds == null) {
					this.typeParameterSignatures[i] = Signature.createTypeParameterSignature(this.typeParameterNames[i], CharOperation.NO_CHAR_CHAR);
				} else {
					int boundsLength = bounds.length;
					char[][] boundSignatures = new char[boundsLength][];
					for (int j = 0; j < boundsLength; j++) {
						boundSignatures[i] = Signature.createCharArrayTypeSignature(bounds[j], false);
					}
					this.typeParameterSignatures[i] = Signature.createTypeParameterSignature(this.typeParameterNames[i], boundSignatures);
				}
			}
		}
	}
	return this.typeParameterSignatures;
}

/**
 * @see ISourceType
 */
public boolean isBinaryType() {
	return false;
}
/**
 * @see ISourceType
 */
public boolean isClass() {
	return (this.flags & IConstants.AccInterface) == 0;
}
/**
 * @see ISourceType
 */
public boolean isInterface() {
	return (this.flags & IConstants.AccInterface) != 0;
}
/**
 * Sets the handle for this type info
 */
protected void setHandle(IType handle) {
	this.handle = handle;
}
/**
 * Sets the name of the package this type is declared in.
 */
protected void setPackageName(char[] name) {
	this.packageName= name;
}
/**
 * Sets the name of the source file this type is declared in.
 */
protected void setSourceFileName(char[] name) {
	this.sourceFileName= name;
}
/**
 * Sets the (unqualified) name of this type's superclass
 */
protected void setSuperclassName(char[] superclassName) {
	this.superclassName = superclassName;
}
/**
 * Sets the (unqualified) names of the interfaces this type implements or extends
 */
protected void setSuperInterfaceNames(char[][] superInterfaceNames) {
	this.superInterfaceNames = superInterfaceNames;
}
/**
 * Sets the names of the type parameters this type declares
 */
protected void setTypeParameterNames(char[][] typeParameterNames) {
	this.typeParameterNames = typeParameterNames;
}
/**
 * Sets the names of the type parameter bounds this type declares
 */
protected void setTypeParameterBounds(char[][][] typeParameterBounds) {
	this.typeParameterBounds = typeParameterBounds;
}
public String toString() {
	return "Info for " + this.handle.toString(); //$NON-NLS-1$
}
}

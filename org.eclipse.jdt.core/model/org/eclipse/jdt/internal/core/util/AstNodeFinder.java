/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AnonymousLocalTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * Finds an AstNode given an IJavaElement in a CompilationUnitDeclaration
 */
public class AstNodeFinder {
	private CompilationUnitDeclaration unit;

	public AstNodeFinder(CompilationUnitDeclaration unit) {
		this.unit = unit;
	}

	/*
	 * Finds the FieldDeclaration in the given ast corresponding to the given field handle.
	 * Returns null if not found.
	 */
	public FieldDeclaration findField(IField fieldHandle) {
		TypeDeclaration typeDecl = findType((IType)fieldHandle.getParent());
		FieldDeclaration[] fields = typeDecl.fields;
		if (fields != null) {
			char[] fieldName = fieldHandle.getElementName().toCharArray();
			for (int i = 0, length = fields.length; i < length; i++) {
				FieldDeclaration field = fields[i];
				if (CharOperation.equals(fieldName, field.name)) {
					return field;
				}
			}
		}
		return null;
	}

	/*
	 * Finds the Initializer in the given ast corresponding to the given initializer handle.
	 * Returns null if not found.
	 */
	public Initializer findInitializer(IInitializer initializerHandle) {
		TypeDeclaration typeDecl = findType((IType)initializerHandle.getParent());
		FieldDeclaration[] fields = typeDecl.fields;
		if (fields != null) {
			int occurenceCount = ((JavaElement)initializerHandle).occurrenceCount;
			for (int i = 0, length = fields.length; i < length; i++) {
				FieldDeclaration field = fields[i];
				if (field instanceof Initializer && --occurenceCount == 0) {
					return (Initializer)field;
				}
			}
		}
		return null;
	}

	/*
	 * Finds the AbstractMethodDeclaration in the given ast corresponding to the given method handle.
	 * Returns null if not found.
	 */
	public AbstractMethodDeclaration findMethod(IMethod methodHandle) {
		TypeDeclaration typeDecl = findType((IType)methodHandle.getParent());
		AbstractMethodDeclaration[] methods = typeDecl.methods;
		if (methods != null) {
			char[] selector = methodHandle.getElementName().toCharArray();
			String[] parameterTypeNames = methodHandle.getParameterTypes();
			int parameterCount = parameterTypeNames.length;
			nextMethod: for (int i = 0, length = methods.length; i < length; i++) {
				AbstractMethodDeclaration method = methods[i];
				if (CharOperation.equals(selector, method.selector)) {
					Argument[] args = method.arguments;
					if (args != null && args.length == parameterCount) {
						for (int j = 0; j < parameterCount; j++) {
							if (!CharOperation.equals(args[j].name, parameterTypeNames[j].toCharArray())) {
								continue nextMethod;
							}
						}
						return method;
					}
				}
					
			}
		}
		return null;
	}

	/*
	 * Finds the TypeDeclaration in the given ast corresponding to the given type handle.
	 * Returns null if not found.
	 */
	public TypeDeclaration findType(IType typeHandle) {
		IJavaElement parent = typeHandle.getParent();
		final char[] typeName = typeHandle.getElementName().toCharArray();
		final int occurenceCount = ((SourceType)typeHandle).occurrenceCount;
		final boolean isAnonymous = typeName.length == 0;
		switch (parent.getElementType()) {
			case IJavaElement.COMPILATION_UNIT:
				TypeDeclaration[] types = this.unit.types;
				if (types != null) {
					for (int i = 0, length = types.length; i < length; i++) {
						TypeDeclaration type = types[i];
						if (CharOperation.equals(typeName, type.name)) {
							return type;
						}
					}
				}
				break;
			case IJavaElement.TYPE:
				TypeDeclaration parentDecl = findType((IType)parent);
				types = parentDecl.memberTypes;
				if (types != null) {
					for (int i = 0, length = types.length; i < length; i++) {
						TypeDeclaration type = types[i];
						if (CharOperation.equals(typeName, type.name)) {
							return type;
						}
					}
				}
				break;
			case IJavaElement.FIELD:
				class Visitor extends AbstractSyntaxTreeVisitorAdapter {
					TypeDeclaration result;
					int count = 0;
					public boolean visit(AnonymousLocalTypeDeclaration anonymousTypeDeclaration, BlockScope scope) {
						if (result != null) return false;
						if (isAnonymous && ++count == occurenceCount) {
							result = anonymousTypeDeclaration;
						}
						return false; // visit only one level
					}
					public boolean visit(LocalTypeDeclaration typeDeclaration, BlockScope scope) {
						if (result != null) return false;
						if (!isAnonymous && CharOperation.equals(typeName, typeDeclaration.name)) {
							result = typeDeclaration;
						}
						return false; // visit only one level
					}
				}
				FieldDeclaration fieldDecl = findField((IField)parent);
				Visitor visitor = new Visitor();
				fieldDecl.traverse(visitor, null);
				return visitor.result;
			case IJavaElement.INITIALIZER:
				Initializer initializer = findInitializer((IInitializer)parent);
				visitor = new Visitor();
				initializer.traverse(visitor, null);
				return visitor.result;
			case IJavaElement.METHOD:
				AbstractMethodDeclaration methodDecl = findMethod((IMethod)parent);
				visitor = new Visitor();
				methodDecl.traverse(visitor, methodDecl.scope);
				return visitor.result;
		}
		return null;
	}
}

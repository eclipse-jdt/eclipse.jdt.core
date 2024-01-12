/*******************************************************************************
 * Copyright (c) 2023 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.RecordDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

class DOMToIndexVisitor extends ASTVisitor {

	private SourceIndexer sourceIndexer;

	public DOMToIndexVisitor(SourceIndexer sourceIndexer) {
		this.sourceIndexer = sourceIndexer;
	}

	@Override
	public boolean visit(TypeDeclaration type) {
		if (type.isInterface()) {
			this.sourceIndexer.addInterfaceDeclaration(type.getModifiers(), getPackage(type), type.getName().toString().toCharArray(), null, ((List<Type>)type.superInterfaceTypes()).stream().map(superInterface -> superInterface.toString().toCharArray()).toArray(char[][]::new), null, false);
		} else {
			this.sourceIndexer.addClassDeclaration(type.getModifiers(), getPackage(type), type.getName().toString().toCharArray(), null, type.getSuperclassType() == null ? null : type.getSuperclassType().toString().toCharArray(),
				((List<Type>)type.superInterfaceTypes()).stream().map(superInterface -> superInterface.toString().toCharArray()).toArray(char[][]::new), null, isSecondary(type));
		}
		// TODO other types
		return true;
	}

	private boolean isSecondary(TypeDeclaration type) {
		return type.getParent() instanceof CompilationUnit unit &&
			unit.types().size() > 1 &&
			unit.types().indexOf(type) > 0;
			// TODO: check name?
	}

	private char[] getPackage(ASTNode node) {
		while (node != null && !(node instanceof CompilationUnit)) {
			node = node.getParent();
		}
		return node == null ? null :
			node instanceof CompilationUnit unit && unit.getPackage() != null ? unit.getPackage().getName().toString().toCharArray() :
			null; 
	}

	@Override
	public boolean visit(RecordDeclaration recordDecl) {
		// copied processing of TypeDeclaration
		this.sourceIndexer.addClassDeclaration(recordDecl.getModifiers(), getPackage(recordDecl), recordDecl.getName().toString().toCharArray(), null, null,
				((List<Type>)recordDecl.superInterfaceTypes()).stream().map(type -> type.toString().toCharArray()).toArray(char[][]::new), null, false);
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration method) {
		char[] methodName = method.getName().toString().toCharArray();
		char[][] parameterTypes = ((List<VariableDeclaration>)method.parameters()).stream()
			.filter(SingleVariableDeclaration.class::isInstance)
			.map(SingleVariableDeclaration.class::cast)
			.map(SingleVariableDeclaration::getType)
			.map(Type::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		char[] returnType = null;
		if (method.getReturnType2() instanceof SimpleType simple) {
			returnType = simple.getName().toString().toCharArray();
		} else if (method.getReturnType2() instanceof PrimitiveType primitive) {
			returnType = primitive.getPrimitiveTypeCode().toString().toCharArray();
		} else if (method.getReturnType2() == null) {
			// do nothing
		} else {
			returnType = method.getReturnType2().toString().toCharArray();
		}
		char[][] exceptionTypes = ((List<Type>)method.thrownExceptionTypes()).stream()
			.map(Type::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		this.sourceIndexer.addMethodDeclaration(methodName, parameterTypes, returnType, exceptionTypes);
		char[][] parameterNames = ((List<VariableDeclaration>)method.parameters()).stream()
			.map(VariableDeclaration::getName)
			.map(SimpleName::toString)
			.map(String::toCharArray)
			.toArray(char[][]::new);
		this.sourceIndexer.addMethodDeclaration(null,
			null /* TODO: fully qualified name of enclosing type? */,
			methodName,
			parameterTypes.length,
			null,
			parameterTypes,
			parameterNames,
			returnType,
			method.getModifiers(),
			getPackage(method),
			0 /* TODO What to put here? */,
			exceptionTypes,
			0 /* TODO ExtraFlags.IsLocalType ? */);
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration field) {
		char[] typeName = field.getType().toString().toCharArray();
		for (VariableDeclarationFragment fragment: (List<VariableDeclarationFragment>)field.fragments()) {
			this.sourceIndexer.addFieldDeclaration(typeName, fragment.getName().toString().toCharArray());
		}
		return true;
	}
	
	// TODO (cf SourceIndexer and SourceIndexerRequestor)
	// * Module: addModuleDeclaration/addModuleReference/addModuleExportedPackages
	// * Lambda: addIndexEntry/addClassDeclaration
	// * addMethodReference
	// * addConstructorReference
}

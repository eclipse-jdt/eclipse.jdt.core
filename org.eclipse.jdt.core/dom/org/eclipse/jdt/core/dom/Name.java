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

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

/**
 * Abstract base class for all AST nodes that represent names.
 * There are exactly two kinds of name: simple ones 
 * (<code>SimpleName</code>) and qualified ones (<code>QualifiedName</code>).
 * <p>
 * <pre>
 * Name:
 *     SimpleName
 *     QualifiedName
 * </pre>
 * </p>
 * 
 * @since 2.0
 */
public abstract class Name extends Expression {
	
	/**
	 * This index reprensents the position inside a qualified name.
	 */
	int index;
	
	/**
	 * Creates a new AST node for a name owned by the given AST.
	 * <p>
	 * N.B. This constructor is package-private.
	 * </p>
	 * 
	 * @param ast the AST that is to own this node
	 */
	Name(AST ast) {
		super(ast);
	}
	
	/**
	 * Returns whether this name is a simple name
	 * (<code>SimpleName</code>).
	 * 
	 * @return <code>true</code> if this is a simple name, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isSimpleName() {
		return (this instanceof SimpleName);
	}
		
	/**
	 * Returns whether this name is a qualified name
	 * (<code>QualifiedName</code>).
	 * 
	 * @return <code>true</code> if this is a qualified name, and 
	 *    <code>false</code> otherwise
	 */
	public final boolean isQualifiedName() {
		return (this instanceof QualifiedName);
	}

	/**
	 * Resolves and returns the binding for the entity referred to by this name.
	 * <p>
	 * Note that bindings are generally unavailable unless requested when the
	 * AST is being built.
	 * </p>
	 * 
	 * @return the binding, or <code>null</code> if the binding cannot be 
	 *    resolved
	 */	
	public final IBinding resolveBinding() {
		return getAST().getBindingResolver().resolveName(this);
	}

	BlockScope lookupScope() {
		ASTNode currentNode = this;
		while(currentNode != null
			&&!(currentNode instanceof MethodDeclaration)
			&& !(currentNode instanceof Initializer)
			&& !(currentNode instanceof FieldDeclaration)) {
			currentNode = currentNode.getParent();
		}
		if (currentNode == null) {
			return null;
		}
		if (currentNode instanceof Initializer) {
			Initializer initializer = (Initializer) currentNode;
			while(!(currentNode instanceof TypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.getAST().getBindingResolver().getCorrespondingNode(currentNode);
			if ((initializer.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		} else if (currentNode instanceof FieldDeclaration) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) currentNode;
			while(!(currentNode instanceof TypeDeclaration)) {
				currentNode = currentNode.getParent();
			}
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDecl = (org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) this.getAST().getBindingResolver().getCorrespondingNode(currentNode);
			if ((fieldDeclaration.getModifiers() & Modifier.STATIC) != 0) {
				return typeDecl.staticInitializerScope;
			} else {
				return typeDecl.initializerScope;
			}
		}
		AbstractMethodDeclaration abstractMethodDeclaration = (AbstractMethodDeclaration) this.getAST().getBindingResolver().getCorrespondingNode(currentNode);
		return abstractMethodDeclaration.scope;
	}	
	
}

/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class DisjonctiveTypeReference extends TypeReference {
	public TypeReference[] typeReferences;

	public DisjonctiveTypeReference(TypeReference[] typeReferences) {
		this.typeReferences = typeReferences; 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#copyDims(int)
	 */
	public TypeReference copyDims(int dim) {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getLastToken()
	 */
	public char[] getLastToken() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeBinding(org.eclipse.jdt.internal.compiler.lookup.Scope)
	 */
	protected TypeBinding getTypeBinding(Scope scope) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#getTypeName()
	 */
	public char[][] getTypeName() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		int length = this.typeReferences == null ? 0 : this.typeReferences.length;
		for (int i = 0; i < length; i++) {
			this.typeReferences[i].traverse(visitor, scope);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.TypeReference#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public void traverse(ASTVisitor visitor, ClassScope scope) {
		int length = this.typeReferences == null ? 0 : this.typeReferences.length;
		for (int i = 0; i < length; i++) {
			this.typeReferences[i].traverse(visitor, scope);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Expression#printExpression(int, java.lang.StringBuffer)
	 */
	public StringBuffer printExpression(int indent, StringBuffer output) {
		int length = this.typeReferences == null ? 0 : this.typeReferences.length;
		printIndent(indent, output);
		for (int i = 0; i < length; i++) {
			this.typeReferences[i].printExpression(0, output);
			if (i != length - 1) {
				output.append(" | "); //$NON-NLS-1$
			}
		}
		return output;
	}

}

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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeParameter extends AbstractVariableDeclaration {

    public TypeVariableBinding binding;
	public TypeReference[] bounds;

	public void resolve(ClassScope scope) {
	    // TODO (philippe) add warning for detecting variable name collisions
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer printStatement(int indent, StringBuffer output) {
		output.append(this.name);
		if (this.type != null) {
			output.append(" extends "); //$NON-NLS-1$
			this.type.print(0, output);
		}
		if (this.bounds != null){
			for (int i = 0; i < this.bounds.length; i++) {
				output.append(" & "); //$NON-NLS-1$
				this.bounds[i].print(0, output);
			}
		}
		return output;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	    // nothing to do
	}
	
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (type != null) {
				type.traverse(visitor, scope);
			}
			if (bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (type != null) {
				type.traverse(visitor, scope);
			}
			if (bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}	
}
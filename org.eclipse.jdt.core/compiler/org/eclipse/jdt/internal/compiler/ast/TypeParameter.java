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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class TypeParameter extends AbstractVariableDeclaration {

	public TypeReference[] bounds;

	public void resolve(ClassScope scope) {
		
		if (bounds != null){
			for (int i = 0, max = bounds.length; i < max; i++){
				TypeBinding boundType = bounds[i].getTypeBinding(scope);
				if (isTypeUseDeprecated(boundType, scope)){
					scope.problemReporter().deprecatedType(boundType, bounds[i]);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer print(int indent, StringBuffer output) {
		output.append(this.name);
		if (this.bounds != null){
			output.append(" extends "); //$NON-NLS-1$
			this.type.print(0, output);
			for (int i = 0; i < this.bounds.length; i++) {
				if (i > 0) {
					output.append("& "); //$NON-NLS-1$
				}
				output.append(this.bounds[i].print(0, output));
			}
		}
		return output;
	}
	
	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	}
	
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
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

	public void traverse(IAbstractSyntaxTreeVisitor visitor, ClassScope scope) {
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
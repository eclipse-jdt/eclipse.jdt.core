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
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Node to represent Wildcard
 */
public class Wildcard extends SingleTypeReference {

	public TypeReference bound;
	boolean isSuper;
	/**
	 * @param source
	 * @param pos
	 */
	public Wildcard(boolean isSuper) {
		super(null, 0);
		this.isSuper = isSuper;
	}
	
	public StringBuffer printExpression(int indent, StringBuffer output){
		output.append('?');
		if (this.bound != null) {
			if (this.isSuper) {
				output.append(" super "); //$NON-NLS-1$
			} else {
				output.append(" extends "); //$NON-NLS-1$
			}
			this.bound.printExpression(0, output);
		}
		return output;
	}	
	public TypeBinding resolveType(ClassScope classScope) {
		return null;
	}
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		if (this.bound != null) {
			this.bound.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		visitor.visit(this, scope);
		if (this.bound != null) {
			this.bound.traverse(visitor, scope);
		}
		visitor.endVisit(this, scope);
	}
}

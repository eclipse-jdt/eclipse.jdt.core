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
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.lookup.*;


public class AnnotationArgument extends Argument {

	public AnnotationArgument(char[] name, int startPosition, int endPosition) {
		super(name, (((long) startPosition) << 32) + endPosition, null, IConstants.AccDefault);
		this.bits |= InsideAnnotation;
	}

	public AnnotationArgument(char[] name, int startPosition, int endPosition, TypeReference typeRef) {
		this(name, startPosition, endPosition);
		this.type = typeRef;
		this.bits |= InsideAnnotation;
	}

	public StringBuffer print(int indent, StringBuffer output) {

		if (type != null) {
			type.print(0, output).append(' '); 
		}
		return output.append(this.name);
	}

	public void resolve(BlockScope scope) {
		
		Binding existingVariable = scope.getBinding(name, BindingIds.VARIABLE, this);
		if (existingVariable != null && existingVariable.isValidBinding()){
			if (existingVariable instanceof LocalVariableBinding && this.hiddenVariableDepth == 0) {
				LocalVariableBinding local = (LocalVariableBinding) existingVariable;
				if (local.isArgument) {
					this.binding = local;
					return;
				}
			} 
		}
		scope.problemReporter().annotationInvalidParamName(this, false);
	}

	/* (non-Javadoc)
	 * Redefine to capture annotation specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (type != null) {
				type.traverse(visitor, scope);
			}
			if (initialization != null) {
				initialization.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}

}

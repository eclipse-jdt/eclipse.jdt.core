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

public class JavadocSingleNameReference extends SingleNameReference {

	public int tagSourceStart, tagSourceEnd;

	public JavadocSingleNameReference(char[] name, int startPosition, int endPosition) {
		super(name, (((long) startPosition) << 32) + endPosition);
		this.bits |= InsideJavadoc;
	}

	public void resolve(BlockScope scope) {
		resolve(scope, true);
	}

	/**
	 * Resolve without warnings
	 */
	public void resolve(BlockScope scope, boolean warn) {
		
		LocalVariableBinding variableBinding = scope.findVariable(this.token);
		if (variableBinding != null && variableBinding.isValidBinding() && variableBinding.isArgument) {
			this.binding = variableBinding;
			return;
		}
		if (warn) {
			try {
				MethodScope methScope = (MethodScope) scope;
				scope.problemReporter().javadocInvalidParamName(this, methScope.referenceMethod().modifiers);
			}
			catch (Exception e) {
				scope.problemReporter().javadocInvalidParamName(this, -1);
			}
		}
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.ASTNode#traverse(org.eclipse.jdt.internal.compiler.ASTVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(ASTVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}

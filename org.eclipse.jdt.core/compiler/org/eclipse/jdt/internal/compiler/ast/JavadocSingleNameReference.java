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
import org.eclipse.jdt.internal.compiler.lookup.*;

public class JavadocSingleNameReference extends SingleNameReference {

	public int tagSourceStart, tagSourceEnd;

	public JavadocSingleNameReference(char[] name, int startPosition, int endPosition) {
		super(name, (((long) startPosition) << 32) + endPosition);
		this.bits |= InsideJavadoc;
	}

	public void resolve(BlockScope scope) {
		
		LocalVariableBinding variableBinding = scope.findVariable(token);
		if (variableBinding != null && variableBinding.isValidBinding() && variableBinding.isArgument) {
			this.binding = variableBinding;
			return;
		}
		scope.problemReporter().javadocInvalidParamName(this, false);
	}

	/* (non-Javadoc)
	 * Redefine to capture javadoc specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		visitor.visit(this, scope);
		visitor.endVisit(this, scope);
	}
}

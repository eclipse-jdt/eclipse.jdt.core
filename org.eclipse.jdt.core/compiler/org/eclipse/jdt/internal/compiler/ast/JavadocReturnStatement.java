/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.*;


public class JavadocReturnStatement extends ReturnStatement {
	public boolean empty = true;

	public JavadocReturnStatement(int s, int e) {
		super(null, s, e);
		this.bits |= InsideJavadoc;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Statement#resolve(org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void resolve(BlockScope scope) {
		MethodScope methodScope = scope.methodScope();
		MethodBinding methodBinding = null;
		TypeBinding methodType =
			(methodScope.referenceContext instanceof AbstractMethodDeclaration)
				? ((methodBinding = ((AbstractMethodDeclaration) methodScope.referenceContext).binding) == null 
					? null 
					: methodBinding.returnType)
				: VoidBinding;
		if (methodType == null || methodType == VoidBinding) {
			scope.problemReporter().javadocUnexpectedTag(this.sourceStart, this.sourceEnd);
		} else if (this.empty) {
			scope.problemReporter().javadocEmptyReturnTag(this.sourceStart, this.sourceEnd);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.Statement#printStatement(int, java.lang.StringBuffer)
	 */
	public StringBuffer printStatement(int tab, StringBuffer output) {
		printIndent(tab, output).append("return"); //$NON-NLS-1$
		if (!this.empty)
			output.append(' ').append(" <not empty>"); //$NON-NLS-1$
		return output;
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

/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;

class MethodInvocationFragmentBuilder
	extends ASTVisitor {
		
	ArrayList fragmentsList;

	MethodInvocationFragmentBuilder() {
		this.fragmentsList = new ArrayList();
	}

	public List fragments() {
		return this.fragmentsList;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.MessageSend, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(MethodInvocation methodInvocation) {
		final Expression expression = methodInvocation.getExpression();
		if (expression != null) {
			switch(expression.getNodeType()) {
				case ASTNode.METHOD_INVOCATION :
				case ASTNode.SUPER_METHOD_INVOCATION :
					expression.accept(this);
					break;
				default:
					this.fragmentsList.add(expression);
			}
		}
		this.fragmentsList.add(methodInvocation.getName());
		return false;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.MessageSend, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public boolean visit(SuperMethodInvocation methodInvocation) {
		final Name qualifier = methodInvocation.getQualifier();
		if (qualifier != null) {
			this.fragmentsList.add(qualifier);
		}
		this.fragmentsList.add(methodInvocation.getName());
		return false;
	}
}

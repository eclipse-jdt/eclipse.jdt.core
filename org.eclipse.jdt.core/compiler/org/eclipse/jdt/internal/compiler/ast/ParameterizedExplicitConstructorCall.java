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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;

public class ParameterizedExplicitConstructorCall extends ExplicitConstructorCall {

	public TypeReference[] typeArguments;
	
	/**
	 * @param accessMode
	 */
	public ParameterizedExplicitConstructorCall(int accessMode) {
		super(accessMode);
	}

	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.qualification != null) {
				this.qualification.traverse(visitor, scope);
			}
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
			if (this.arguments != null) {
				for (int i = 0, argumentLength = this.arguments.length; i < argumentLength; i++) {
					this.arguments[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}
}

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

public class ParameterizedQualifiedAllocationExpression extends QualifiedAllocationExpression {
	public TypeReference[] typeArguments;

	public static ParameterizedQualifiedAllocationExpression copyInto(QualifiedAllocationExpression allocationExpression) {
		ParameterizedQualifiedAllocationExpression parameterizedQualifiedAllocationExpression = new ParameterizedQualifiedAllocationExpression();
		parameterizedQualifiedAllocationExpression.anonymousType = allocationExpression.anonymousType;
		parameterizedQualifiedAllocationExpression.arguments = allocationExpression.arguments;
		parameterizedQualifiedAllocationExpression.bits = allocationExpression.bits;
		parameterizedQualifiedAllocationExpression.constant = allocationExpression.constant;
		parameterizedQualifiedAllocationExpression.enclosingInstance = allocationExpression.enclosingInstance;
		parameterizedQualifiedAllocationExpression.implicitConversion = allocationExpression.implicitConversion;
		parameterizedQualifiedAllocationExpression.sourceEnd = allocationExpression.sourceEnd;
		parameterizedQualifiedAllocationExpression.sourceStart = allocationExpression.sourceStart;
		parameterizedQualifiedAllocationExpression.type = allocationExpression.type;
		return parameterizedQualifiedAllocationExpression;
	}
	
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope scope) {

		if (visitor.visit(this, scope)) {
			if (enclosingInstance != null) {
				enclosingInstance.traverse(visitor, scope);
			}
			for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
				this.typeArguments[i].traverse(visitor, scope);
			}
			type.traverse(visitor, scope);
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++) {
					arguments[i].traverse(visitor, scope);
				}
			}
			if (anonymousType != null) {
				anonymousType.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, scope);
	}
}

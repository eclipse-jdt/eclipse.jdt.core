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
	
	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (enclosingInstance != null)
			enclosingInstance.printExpression(0, output).append('.'); 
		if (typeArguments != null) {
			output.append('<');//$NON-NLS-1$
			int max = typeArguments.length - 1;
			for (int j = 0; j < max; j++) {
				typeArguments[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeArguments[max].print(0, output);
			output.append('>');
		}
		output.append("new "); //$NON-NLS-1$
		type.printExpression(0, output); 
		output.append('(');
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].printExpression(0, output);
			}
		}
		output.append(')');
		if (anonymousType != null) {
			anonymousType.print(indent, output);
		}
		return output;
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

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

public class ParameterizedMessageSend extends MessageSend {
	public TypeReference[] typeArguments;

public StringBuffer printExpression(int indent, StringBuffer output){
	
	if (!receiver.isImplicitThis()) receiver.printExpression(0, output).append('.');
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
	
	output.append(selector).append('(') ; //$NON-NLS-1$
	if (arguments != null) {
		for (int i = 0; i < arguments.length ; i ++) {	
			if (i > 0) output.append(", "); //$NON-NLS-1$
			arguments[i].printExpression(0, output);
		}
	}
	return output.append(')');
}
	
public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
	if (visitor.visit(this, blockScope)) {
		receiver.traverse(visitor, blockScope);
		for (int i = 0, typeArgumentsLength = this.typeArguments.length; i < typeArgumentsLength; i++) {
			this.typeArguments[i].traverse(visitor, blockScope);
		}
		if (arguments != null) {
			int argumentsLength = arguments.length;
			for (int i = 0; i < argumentsLength; i++)
				arguments[i].traverse(visitor, blockScope);
		}
	}
	visitor.endVisit(this, blockScope);
}	
}

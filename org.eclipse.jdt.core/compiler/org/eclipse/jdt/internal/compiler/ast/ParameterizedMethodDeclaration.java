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

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;

public class ParameterizedMethodDeclaration extends MethodDeclaration {

	public TypeParameter[] typeParameters;
	
	/**
	 * @param compilationResult
	 */
	public ParameterizedMethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public StringBuffer print(int tab, StringBuffer output) {
		printIndent(tab, output);
		printModifiers(modifiers, output);
		if (typeParameters != null) {
			output.append('<');//$NON-NLS-1$
			int max = typeParameters.length - 1;
			for (int j = 0; j < max; j++) {
				typeParameters[j].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			typeParameters[max].print(0, output);
			output.append('>');
		}
	
		printReturnType(0, output).append(selector).append('(');
		if (arguments != null) {
			for (int i = 0; i < arguments.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				arguments[i].print(0, output);
			}
		}
		output.append(')');
		if (thrownExceptions != null) {
			output.append(" throws "); //$NON-NLS-1$
			for (int i = 0; i < thrownExceptions.length; i++) {
				if (i > 0) output.append(", "); //$NON-NLS-1$
				thrownExceptions[i].print(0, output);
			}
		}
		printBody(tab + 1, output);
		return output;
	}
	
	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.typeParameters != null) {
				int typeParametersLength = this.typeParameters.length;
				for (int i = 0; i < typeParametersLength; i++) {
					this.typeParameters[i].traverse(visitor, scope);
				}
			}
			if (returnType != null)
				returnType.traverse(visitor, scope);
			if (arguments != null) {
				int argumentLength = arguments.length;
				for (int i = 0; i < argumentLength; i++)
					arguments[i].traverse(visitor, scope);
			}
			if (thrownExceptions != null) {
				int thrownExceptionsLength = thrownExceptions.length;
				for (int i = 0; i < thrownExceptionsLength; i++)
					thrownExceptions[i].traverse(visitor, scope);
			}
			if (statements != null) {
				int statementsLength = statements.length;
				for (int i = 0; i < statementsLength; i++)
					statements[i].traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
}

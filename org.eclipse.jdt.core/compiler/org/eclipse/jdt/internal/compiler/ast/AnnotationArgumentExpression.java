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
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;


public class AnnotationArgumentExpression extends Expression {
	public char[] token;
	public AnnotationArgument argument;

	public AnnotationArgumentExpression(char[] name, int startPos, int endPos, TypeReference typeRef) {
		this.token = name;
		this.sourceStart = startPos;
		this.sourceEnd = endPos;
		this.argument = new AnnotationArgument(name, startPos, endPos, typeRef);
		this.bits |= InsideAnnotation;
	}

	private TypeBinding commonResolveType(Scope scope) {
		constant = NotAConstant;
		if (this.resolvedType != null) { // is a shared type reference which was already resolved
			if (!this.resolvedType.isValidBinding()) {
				return null; // already reported error
			}
		}
		else {
			if (this.argument != null) {
				TypeReference typeRef = this.argument.type;
				if (typeRef != null) {
					this.resolvedType = typeRef.getTypeBinding(scope);
					if (!this.resolvedType.isValidBinding()) {
						scope.problemReporter().invalidType(this, this.resolvedType);
						return null;
					}
					if (isTypeUseDeprecated(this.resolvedType, scope)) {
						scope.problemReporter().deprecatedType(this.resolvedType, this);
						return null;
					}
					return this.resolvedType;
				}
			}
		}
		return null;
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		if (argument == null) {
			if (token != null) {
				output.append(token);
			}
		}
		else {
			argument.print(indent, output);
		}
		return output;
	}

	public void resolve(BlockScope scope) {
		if (argument != null) {
			argument.resolve(scope);
		}
	}

	public TypeBinding resolveType(BlockScope scope) {
		return commonResolveType(scope);
	}

	public TypeBinding resolveType(ClassScope scope) {
		return commonResolveType(scope);
	}

	/* (non-Javadoc)
	 * Redefine to capture annotation specific signatures
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#traverse(org.eclipse.jdt.internal.compiler.IAbstractSyntaxTreeVisitor, org.eclipse.jdt.internal.compiler.lookup.BlockScope)
	 */
	public void traverse(IAbstractSyntaxTreeVisitor visitor, BlockScope blockScope) {
		if (visitor.visit(this, blockScope)) {
			if (this.argument != null) {
				argument.traverse(visitor, blockScope);
			}
		}
		visitor.endVisit(this, blockScope);
	}
}

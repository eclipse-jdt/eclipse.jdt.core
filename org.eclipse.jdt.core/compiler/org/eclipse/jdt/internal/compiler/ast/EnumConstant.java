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
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

/**
 * Enum constant node
 */
public class EnumConstant extends TypeDeclaration {

	public Expression[] arguments;

	public EnumConstant(CompilationResult compilationResult) {
		super(compilationResult);
		this.compilationResult = compilationResult;
	}

	public StringBuffer print(int indent, StringBuffer output) {
		output.append(name);
		if (arguments != null) {
			output.append('(');
			int length = arguments.length;
			for (int i = 0; i < length - 1; i++) {
				arguments[i].print(0, output);
				output.append(", ");//$NON-NLS-1$
			}
			arguments[length - 1].print(0, output);
			output.append(')');
		}
		printBody(indent, output);
		return output;
	}

	/**
	 *	Iteration for a package member type
	 *
	 */
	public void traverse(
		ASTVisitor visitor,
		CompilationUnitScope unitScope) {
	
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, unitScope)) {
				if (this.annotations != null) {
					int annotationsLength = this.annotations.length;
					for (int i = 0; i < annotationsLength; i++)
						this.annotations[i].traverse(visitor, scope);
				}
				if (this.arguments != null) {
					int length = this.arguments.length;
					for (int i = 0; i < length; i++)
						this.arguments[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++)
						this.enums[i].traverse(visitor, scope);
				}
				if (this.fields != null) {
					int length = this.fields.length;
					for (int i = 0; i < length; i++) {
						FieldDeclaration field;
						if ((field = this.fields[i]).isStatic()) {
							field.traverse(visitor, staticInitializerScope);
						} else {
							field.traverse(visitor, initializerScope);
						}
					}
				}
				if (this.methods != null) {
					int length = this.methods.length;
					for (int i = 0; i < length; i++)
						this.methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, unitScope);
		} catch (AbortType e) {
			// silent abort
		}
	}

	/**
	 *	Iteration for a local innertype
	 *
	 */
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {
		
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, blockScope)) {
				if (this.annotations != null) {
					int annotationsLength = this.annotations.length;
					for (int i = 0; i < annotationsLength; i++)
						this.annotations[i].traverse(visitor, scope);
				}
				if (this.arguments != null) {
					int length = this.arguments.length;
					for (int i = 0; i < length; i++)
						this.arguments[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++)
						this.enums[i].traverse(visitor, scope);
				}
				if (this.fields != null) {
					int length = this.fields.length;
					for (int i = 0; i < length; i++) {
						FieldDeclaration field;
						if ((field = this.fields[i]).isStatic()) {
							field.traverse(visitor, staticInitializerScope);
						} else {
							field.traverse(visitor, initializerScope);
						}
					}
				}
				if (this.methods != null) {
					int length = this.methods.length;
					for (int i = 0; i < length; i++)
						this.methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, blockScope);
		} catch (AbortType e) {
			// silent abort
		}
	}

	/**
	 *	Iteration for a member innertype
	 *
	 */
	public void traverse(ASTVisitor visitor, ClassScope classScope) {
		
		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, classScope)) {
				if (this.annotations != null) {
					int annotationsLength = this.annotations.length;
					for (int i = 0; i < annotationsLength; i++)
						this.annotations[i].traverse(visitor, scope);
				}
				if (this.arguments != null) {
					int length = this.arguments.length;
					for (int i = 0; i < length; i++)
						this.arguments[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++)
						this.enums[i].traverse(visitor, scope);
				}
				if (this.fields != null) {
					int length = this.fields.length;
					for (int i = 0; i < length; i++) {
						FieldDeclaration field;
						if ((field = this.fields[i]).isStatic()) {
							field.traverse(visitor, staticInitializerScope);
						} else {
							field.traverse(visitor, initializerScope);
						}
					}
				}
				if (this.methods != null) {
					int length = this.methods.length;
					for (int i = 0; i < length; i++)
						this.methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, classScope);
		} catch (AbortType e) {
			// silent abort
		}
	}
}
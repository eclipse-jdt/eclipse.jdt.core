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

import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.problem.AbortType;

/**
 * Enum declaration
 */
public class EnumDeclaration extends TypeDeclaration {

	public EnumConstant[] enumConstants;
	
	/**
	 * @param compilationResult
	 */
	public EnumDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		output.append(" {"); //$NON-NLS-1$
		if (enumConstants != null) {
			int length = enumConstants.length;
			output.append('\n');
			for (int i = 0; i < length - 1; i++) {
				if (enumConstants[i] != null) {
					enumConstants[i].print(indent + 1, output);
					output.append(",\n");//$NON-NLS-1$
				}
			}
			enumConstants[length - 1].print(indent + 1, output);
			output.append("\n;\n");//$NON-NLS-1$
		}
		if (this.enums != null) {
			for (int i = 0; i < this.enums.length; i++) {
				if (this.enums[i] != null) {
					output.append('\n');
					this.enums[i].print(indent + 1, output);
				}
			}
		}
		if (memberTypes != null) {
			for (int i = 0; i < memberTypes.length; i++) {
				if (memberTypes[i] != null) {
					output.append('\n');
					memberTypes[i].print(indent + 1, output);
				}
			}
		}
		if (fields != null) {
			for (int fieldI = 0; fieldI < fields.length; fieldI++) {
				if (fields[fieldI] != null) {
					output.append('\n');
					fields[fieldI].print(indent + 1, output);
				}
			}
		}
		if (methods != null) {
			for (int i = 0; i < methods.length; i++) {
				if (methods[i] != null) {
					output.append('\n');
					methods[i].print(indent + 1, output); 
				}
			}
		}
		output.append('\n');
		return printIndent(indent, output).append('}');
	}	

	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, blockScope)) {
				if (this.typeParameters != null) {
					int length = this.typeParameters.length;
					for (int i = 0; i < length; i++) {
						this.typeParameters[i].traverse(visitor, scope);
					}
				}
				if (this.superclass != null)
					this.superclass.traverse(visitor, scope);
				if (this.superInterfaces != null) {
					int length = this.superInterfaces.length;
					for (int i = 0; i < length; i++)
						this.superInterfaces[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++) {
						this.enums[i].traverse(visitor, scope);
					}
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
					int length = methods.length;
					for (int i = 0; i < length; i++)
						this.methods[i].traverse(visitor, scope);
				}
			}
			visitor.endVisit(this, blockScope);
		} catch (AbortType e) {
			// silent abort
		}
	}
	public void traverse(ASTVisitor visitor, ClassScope classScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, classScope)) {
				if (this.typeParameters != null) {
					int typeParametersLength = this.typeParameters.length;
					for (int i = 0; i < typeParametersLength; i++) {
						this.typeParameters[i].traverse(visitor, scope);
					}
				}
				if (this.superclass != null)
					this.superclass.traverse(visitor, scope);
				if (this.superInterfaces != null) {
					int length = this.superInterfaces.length;
					for (int i = 0; i < length; i++)
						this.superInterfaces[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++) {
						this.enums[i].traverse(visitor, scope);
					}
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

	public void traverse(ASTVisitor visitor, CompilationUnitScope unitScope) {

		if (ignoreFurtherInvestigation)
			return;
		try {
			if (visitor.visit(this, unitScope)) {
				if (this.typeParameters != null) {
					int length = this.typeParameters.length;
					for (int i = 0; i < length; i++) {
						this.typeParameters[i].traverse(visitor, scope);
					}
				}
				if (this.superclass != null)
					this.superclass.traverse(visitor, scope);
				if (this.superInterfaces != null) {
					int length = this.superInterfaces.length;
					for (int i = 0; i < length; i++)
						this.superInterfaces[i].traverse(visitor, scope);
				}
				if (this.memberTypes != null) {
					int length = this.memberTypes.length;
					for (int i = 0; i < length; i++)
						this.memberTypes[i].traverse(visitor, scope);
				}
				if (this.enums != null) {
					int length = this.enums.length;
					for (int i = 0; i < length; i++) {
						this.enums[i].traverse(visitor, scope);
					}
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
}

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
 * Annotation type declaration
 */
public class AnnotationTypeDeclaration extends TypeDeclaration {

	public AnnotationTypeMemberDeclaration[] annotationTypeMemberDeclarations;

	/**
	 * @param compilationResult
	 */
	public AnnotationTypeDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public StringBuffer printHeader(int indent, StringBuffer output) {

		printModifiers(this.modifiers, output);
		output.append("@interface "); //$NON-NLS-1$ //$NON-NLS-2$
		output.append(name);
		if (typeParameters != null) {
			output.append("<");//$NON-NLS-1$
			for (int i = 0; i < typeParameters.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				typeParameters[i].print(0, output);
			}
			output.append(">");//$NON-NLS-1$
		}
		if (superclass != null) {
			output.append(" extends ");  //$NON-NLS-1$
			superclass.print(0, output);
		}
		if (superInterfaces != null && superInterfaces.length > 0) {
			output.append(isInterface() ? " extends " : " implements ");//$NON-NLS-2$ //$NON-NLS-1$
			for (int i = 0; i < superInterfaces.length; i++) {
				if (i > 0) output.append( ", "); //$NON-NLS-1$
				superInterfaces[i].print(0, output);
			}
		}
		return output;
	}

	public StringBuffer printBody(int indent, StringBuffer output) {

		output.append(" {"); //$NON-NLS-1$
		if (this.enums != null) {
			for (int i = 0; i < this.enums.length; i++) {
				if (this.enums[i] != null) {
					output.append('\n');
					this.enums[i].print(indent + 1, output);
				}
			}
		}
		if (this.annotationTypeMemberDeclarations != null) {
			for (int i = 0; i < this.annotationTypeMemberDeclarations.length; i++) {
				if (this.annotationTypeMemberDeclarations[i] != null) {
					output.append('\n');
					this.annotationTypeMemberDeclarations[i].print(indent + 1, output);
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

	public void traverse(ASTVisitor visitor, BlockScope unitScope) {

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
				if (this.annotationTypeMemberDeclarations != null) {
					int length = this.annotationTypeMemberDeclarations.length;
					for (int i = 0; i < length; i++) {
						this.annotationTypeMemberDeclarations[i].traverse(visitor, scope);
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
			visitor.endVisit(this, unitScope);
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
				if (this.annotationTypeMemberDeclarations != null) {
					int length = this.annotationTypeMemberDeclarations.length;
					for (int i = 0; i < length; i++) {
						this.annotationTypeMemberDeclarations[i].traverse(visitor, scope);
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
				if (this.annotationTypeMemberDeclarations != null) {
					int length = this.annotationTypeMemberDeclarations.length;
					for (int i = 0; i < length; i++) {
						this.annotationTypeMemberDeclarations[i].traverse(visitor, scope);
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

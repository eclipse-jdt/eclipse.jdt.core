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
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;

public class AnnotationMethodDeclaration extends MethodDeclaration {
	
	public Expression defaultValue;
	public int extendedDimensions;

	/**
	 * MethodDeclaration constructor comment.
	 */
	public AnnotationMethodDeclaration(CompilationResult compilationResult) {
		super(compilationResult);
	}

	public void generateCode(ClassFile classFile) {
		classFile.generateMethodInfoHeader(this.binding);
		int methodAttributeOffset = classFile.contentsOffset;
		int attributeNumber = classFile.generateMethodInfoAttribute(this.binding, this);
		classFile.completeMethodInfo(methodAttributeOffset, attributeNumber);
	}
	
	public void parseStatements(Parser parser, CompilationUnitDeclaration unit) {
		// nothing to do
		// annotation type member declaration don't have any body
	}

	public void resolveStatements() {

		super.resolveStatements();
		if (this.arguments != null) {
			scope.problemReporter().annotationMembersCannotHaveParameters(this);
		}
		if (this.typeParameters != null) {
			scope.problemReporter().annotationMembersCannotHaveTypeParameters(this);
		}
		if (this.extendedDimensions != 0) {
			scope.problemReporter().illegalExtendedDimensions(this);		
		}		
		if (this.binding == null) return;
		TypeBinding returnTypeBinding = this.binding.returnType;
		if (returnTypeBinding != null) {
				
			// annotation methods can only return base types, String, Class, enum type, annotation types and arrays of these
			checkAnnotationMethodType: {
				TypeBinding leafReturnType = returnTypeBinding.leafComponentType();
					
				switch (leafReturnType.erasure().id) {
					case T_byte :
					case T_short :
					case T_char :
					case T_int :
					case T_long :
					case T_float :
					case T_double :
					case T_boolean :
					case T_JavaLangString :
					case T_JavaLangClass :
						if (returnTypeBinding.dimensions() <= 1) // only 1-dimensional array permitted
							break checkAnnotationMethodType;
				}
				if (leafReturnType.isEnum()) {
					if (returnTypeBinding.dimensions() <= 1) // only 1-dimensional array permitted
						break checkAnnotationMethodType;
				}
				if (leafReturnType.isAnnotationType()) {
					scope.classScope().detectAnnotationCycle(scope.enclosingSourceType(), leafReturnType, this.returnType);
					if (returnTypeBinding.dimensions() <= 1) // only 1-dimensional array permitted
						break checkAnnotationMethodType;
				}
				scope.problemReporter().invalidAnnotationMemberType(this);
			}
			if (this.defaultValue != null) {
				MemberValuePair pair = new MemberValuePair(this.selector, this.sourceStart, this.sourceEnd, this.defaultValue);
				pair.binding = this.binding;
				pair.resolveTypeExpecting(scope, returnTypeBinding);
			}
		}
	}

	public void traverse(
		ASTVisitor visitor,
		ClassScope classScope) {

		if (visitor.visit(this, classScope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.returnType != null) {
				this.returnType.traverse(visitor, scope);
			}
			if (this.defaultValue != null) {
				this.defaultValue.traverse(visitor, scope);
			}
		}
		visitor.endVisit(this, classScope);
	}
}

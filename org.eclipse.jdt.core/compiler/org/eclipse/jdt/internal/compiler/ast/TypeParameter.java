/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Stephan Herrmann - Contribution for
 *								bug 392384 - [1.8][compiler][null] Restore nullness info from type annotations in class files
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import java.util.List;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.TypeReference.AnnotationCollector;
import org.eclipse.jdt.internal.compiler.codegen.AnnotationTargetTypeConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeParameter extends AbstractVariableDeclaration {

    public TypeVariableBinding binding;
	public TypeReference[] bounds;

	/**
	 * @see org.eclipse.jdt.internal.compiler.ast.AbstractVariableDeclaration#getKind()
	 */
	public int getKind() {
		return TYPE_PARAMETER;
	}

	public void checkBounds(Scope scope) {

		if (this.type != null) {
			this.type.checkBounds(scope);
		}
		if (this.bounds != null) {
			for (int i = 0, length = this.bounds.length; i < length; i++) {
				this.bounds[i].checkBounds(scope);
			}
		}
	}

	public void getAllAnnotationContexts(int targetType, int typeParameterIndex, List allAnnotationContexts) {
		AnnotationCollector collector = new AnnotationCollector(this, targetType, typeParameterIndex, allAnnotationContexts);
		if (this.annotations != null) {
			int annotationsLength = this.annotations.length;
			for (int i = 0; i < annotationsLength; i++)
				this.annotations[i].traverse(collector, (BlockScope) null);
		}
		switch(collector.targetType) {
			case AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER :
				collector.targetType = AnnotationTargetTypeConstants.CLASS_TYPE_PARAMETER_BOUND;
				break;
			case AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER :
				collector.targetType = AnnotationTargetTypeConstants.METHOD_TYPE_PARAMETER_BOUND;
		}
		if (this.type != null && ((this.type.bits & ASTNode.HasTypeAnnotations) != 0)) {
			collector.info2 = 0;
			this.type.traverse(collector, (BlockScope) null);
		}
		if (this.bounds != null) {
			int boundsLength = this.bounds.length;
			for (int i = 0; i < boundsLength; i++) {
				TypeReference bound = this.bounds[i];
				if ((bound.bits & ASTNode.HasTypeAnnotations) == 0) {
					continue;
				}
				collector.info2 = i + 1;
				bound.traverse(collector, (BlockScope) null);
			}
		}
	}
	private void internalResolve(Scope scope, boolean staticContext) {
	    // detect variable/type name collisions
		if (this.binding != null) {
			Binding existingType = scope.parent.getBinding(this.name, Binding.TYPE, this, false/*do not resolve hidden field*/);
			if (existingType != null
					&& this.binding != existingType
					&& existingType.isValidBinding()
					&& (existingType.kind() != Binding.TYPE_PARAMETER || !staticContext)) {
				scope.problemReporter().typeHiding(this, existingType);
			}
		}
		if (this.annotations != null) {
			resolveAnnotations(scope);
		}
	}

	public void resolve(BlockScope scope) {
		internalResolve(scope, scope.methodScope().isStatic);
	}

	public void resolve(ClassScope scope) {
		internalResolve(scope, scope.enclosingSourceType().isStatic());
	}

	public void resolveAnnotations(Scope scope) {
		BlockScope resolutionScope = Scope.typeAnnotationsResolutionScope(scope);
		if (resolutionScope != null) {
			resolveAnnotations(resolutionScope, this.annotations, new Annotation.TypeUseBinding(Binding.TYPE_PARAMETER));
			if (this.binding != null && this.binding.isValidBinding())
				this.binding.evaluateNullAnnotations(this.annotations);
		}	
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.compiler.ast.AstNode#print(int, java.lang.StringBuffer)
	 */
	public StringBuffer printStatement(int indent, StringBuffer output) {
		if (this.annotations != null) {
			printAnnotations(this.annotations, output);
			output.append(' ');
		}
		output.append(this.name);
		if (this.type != null) {
			output.append(" extends "); //$NON-NLS-1$
			this.type.print(0, output);
		}
		if (this.bounds != null){
			for (int i = 0; i < this.bounds.length; i++) {
				output.append(" & "); //$NON-NLS-1$
				this.bounds[i].print(0, output);
			}
		}
		return output;
	}

	public void generateCode(BlockScope currentScope, CodeStream codeStream) {
	    // nothing to do
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			if (this.bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}

	public void traverse(ASTVisitor visitor, ClassScope scope) {
		if (visitor.visit(this, scope)) {
			if (this.annotations != null) {
				int annotationsLength = this.annotations.length;
				for (int i = 0; i < annotationsLength; i++)
					this.annotations[i].traverse(visitor, scope);
			}
			if (this.type != null) {
				this.type.traverse(visitor, scope);
			}
			if (this.bounds != null) {
				int boundsLength = this.bounds.length;
				for (int i = 0; i < boundsLength; i++) {
					this.bounds[i].traverse(visitor, scope);
				}
			}
		}
		visitor.endVisit(this, scope);
	}
}

/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.apt.model.ElementFactory;
import org.eclipse.jdt.internal.compiler.apt.util.ManyToMany;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;

/**
 * This class is used to visit the JDT compiler internal AST to discover annotations, 
 * in the course of dispatching to annotation processors.
 */
public class AnnotationDiscoveryVisitor extends ASTVisitor {

	/**
	 * Collects a many-to-many map of annotation types to
	 * the elements they appear on.
	 */
	ManyToMany<TypeElement, Element> _annoToElement;

	boolean storeAnnotations;

	public AnnotationDiscoveryVisitor() {
		this._annoToElement = new ManyToMany<TypeElement, Element>();
	}

	@Override
	public void endVisit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
		scope.compilerOptions().storeAnnotations = this.storeAnnotations;
	}

	@Override
	public boolean visit(Argument argument, BlockScope scope) {
		Annotation[] annotations = argument.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					scope,
					annotations,
					argument.binding);
		}
		return false;
	}

	@Override
	public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
		this.storeAnnotations = scope.compilerOptions().storeAnnotations;
		scope.compilerOptions().storeAnnotations = true;
		return true;
	}

	@Override
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		Annotation[] annotations = constructorDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					constructorDeclaration.scope,
					annotations,
					constructorDeclaration.binding);
		}
		Argument[] arguments = constructorDeclaration.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			for (int i = 0; i < argumentLength; i++) {
				arguments[i].traverse(this, constructorDeclaration.scope);
			}
		}
		return false;
	}

	@Override
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		Annotation[] annotations = fieldDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(scope, annotations, fieldDeclaration.binding);
		}
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		Annotation[] annotations = methodDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					methodDeclaration.scope,
					annotations,
					methodDeclaration.binding);
		}

		Argument[] arguments = methodDeclaration.arguments;
		if (arguments != null) {
			int argumentLength = arguments.length;
			for (int i = 0; i < argumentLength; i++) {
				arguments[i].traverse(this, methodDeclaration.scope);
			}
		}
		return false;
	}

	@Override
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		Annotation[] annotations = memberTypeDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					memberTypeDeclaration.staticInitializerScope,
					annotations,
					memberTypeDeclaration.binding);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		Annotation[] annotations = typeDeclaration.annotations;
		if (annotations != null) {
			this.resolveAnnotations(
					typeDeclaration.staticInitializerScope,
					annotations,
					typeDeclaration.binding);
		}
		return true;
	}

	private void resolveAnnotations(
			BlockScope scope,
			Annotation[] annotations,
			Binding currentBinding) {
		ASTNode.resolveAnnotations(scope, annotations, currentBinding);
		
		for (Annotation annotation : annotations) {
			AnnotationBinding binding = annotation.getCompilerAnnotation();
			TypeElement anno = (TypeElement)ElementFactory.newElement(binding.getAnnotationType()); 
			Element element = ElementFactory.newElement(currentBinding);
			_annoToElement.put(anno, element);
		}
	}
}

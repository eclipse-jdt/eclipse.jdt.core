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
import org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.NormalAnnotation;
import org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation;
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

	Binding currentBinding;
	boolean storeAnnotations;

	/**
	 * Collects a many-to-many map of annotation types to
	 * the elements they appear on.
	 */
	ManyToMany<TypeElement, Element> _annoToElement;

	public AnnotationDiscoveryVisitor() {
		this._annoToElement = new ManyToMany<TypeElement, Element>();
	}

	public boolean visit(Argument argument, BlockScope scope) {
		this.currentBinding = argument.binding;
		Annotation[] annotations = argument.annotations;
		if (annotations != null) {
			int annotationsLength = annotations.length;
			for (int i = 0; i < annotationsLength; i++) {
				annotations[i].traverse(this, scope);
			}
		}
		return false;
	}

	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		this.currentBinding = constructorDeclaration.binding;
		Annotation[] annotations = constructorDeclaration.annotations;
		if (annotations != null) {
			int annotationsLength = annotations.length;
			for (int i = 0; i < annotationsLength; i++) {
				annotations[i].traverse(this, constructorDeclaration.scope);
			}
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

	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		this.currentBinding = fieldDeclaration.binding;
		Annotation[] annotations = fieldDeclaration.annotations;
		if (annotations != null) {
			int annotationsLength = annotations.length;
			for (int i = 0; i < annotationsLength; i++) {
				annotations[i].traverse(this, scope);
			}
		}
		return false;
	}

	public void endVisit(MarkerAnnotation annotation, BlockScope scope) {
		ASTNode.resolveAnnotations(scope, new Annotation[] { annotation }, this.currentBinding);
		AnnotationBinding binding = annotation.getCompilerAnnotation();
		TypeElement anno = (TypeElement)ElementFactory.newElement(binding.getAnnotationType()); 
		Element element = ElementFactory.newElement(this.currentBinding);
		_annoToElement.put(anno, element);
	}

	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		this.currentBinding = methodDeclaration.binding;
		Annotation[] annotations = methodDeclaration.annotations;
		if (annotations != null) {
			int annotationsLength = annotations.length;
			for (int i = 0; i < annotationsLength; i++) {
				annotations[i].traverse(this, methodDeclaration.scope);
			}
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

	public void endVisit(NormalAnnotation annotation, BlockScope scope) {
		ASTNode.resolveAnnotations(scope, new Annotation[] { annotation }, this.currentBinding);
		AnnotationBinding binding = annotation.getCompilerAnnotation();
		TypeElement anno = (TypeElement)ElementFactory.newElement(binding.getAnnotationType()); 
		Element element = ElementFactory.newElement(this.currentBinding);
		_annoToElement.put(anno, element);
	}

	public void endVisit(SingleMemberAnnotation annotation, BlockScope scope) {
		ASTNode.resolveAnnotations(scope, new Annotation[] { annotation }, this.currentBinding);
		AnnotationBinding binding = annotation.getCompilerAnnotation();
		TypeElement anno = (TypeElement)ElementFactory.newElement(binding.getAnnotationType()); 
		Element element = ElementFactory.newElement(this.currentBinding);
		_annoToElement.put(anno, element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration,
	 *      org.eclipse.jdt.internal.compiler.lookup.ClassScope)
	 */
	public boolean visit(TypeDeclaration memberTypeDeclaration, ClassScope scope) {
		this.currentBinding = memberTypeDeclaration.binding;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jdt.internal.compiler.ASTVisitor#visit(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration,
	 *      org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope)
	 */
	public boolean visit(TypeDeclaration typeDeclaration, CompilationUnitScope scope) {
		this.currentBinding = typeDeclaration.binding;
		return true;
	}

	@Override
	public void endVisit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
		scope.compilerOptions().storeAnnotations = this.storeAnnotations;
	}

	@Override
	public boolean visit(CompilationUnitDeclaration compilationUnitDeclaration, CompilationUnitScope scope) {
		this.storeAnnotations = scope.compilerOptions().storeAnnotations;
		scope.compilerOptions().storeAnnotations = true;
		return true;
	}

}

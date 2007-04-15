/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.internal.compiler.apt.model.Factory;
import org.eclipse.jdt.internal.compiler.apt.util.ManyToMany;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class RoundEnvImpl implements RoundEnvironment
{
	private final BaseProcessingEnvImpl _processingEnv;
	private final boolean _isLastRound;
	private final CompilationUnitDeclaration[] _units;
	private final ManyToMany<TypeElement, Element> _annoToUnit;
	private final BinaryTypeBinding[] _binaryTypes;

	public RoundEnvImpl(CompilationUnitDeclaration[] units, BinaryTypeBinding[] binaryTypeBindings, boolean isLastRound, BaseProcessingEnvImpl env) {
		_processingEnv = env;
		_isLastRound = isLastRound;
		_units = units;
		
		// TODO: deal with inherited annotations (esp. annotations inherited from binary supertypes)
		
		// Discover the annotations that will be passed to Processor.process()
		AnnotationDiscoveryVisitor visitor = new AnnotationDiscoveryVisitor();
		for (CompilationUnitDeclaration unit : _units) {
			unit.traverse(visitor, unit.scope);
		}
		_annoToUnit = visitor._annoToElement;
		if (binaryTypeBindings != null) collectAnnotations(binaryTypeBindings);
		_binaryTypes = binaryTypeBindings;
	}

	private void collectAnnotations(ReferenceBinding[] referenceBindings) {
		for (ReferenceBinding referenceBinding : referenceBindings) {
			// collect all annotations from the binary types
			AnnotationBinding[] annotationBindings = referenceBinding.getAnnotations();
			for (AnnotationBinding annotationBinding : annotationBindings) {
				TypeElement anno = (TypeElement)Factory.newElement(annotationBinding.getAnnotationType()); 
				Element element = Factory.newElement(referenceBinding);
				_annoToUnit.put(anno, element);
			}
			FieldBinding[] fieldBindings = referenceBinding.fields();
			for (FieldBinding fieldBinding : fieldBindings) {
				annotationBindings = fieldBinding.getAnnotations();
				for (AnnotationBinding annotationBinding : annotationBindings) {
					TypeElement anno = (TypeElement)Factory.newElement(annotationBinding.getAnnotationType()); 
					Element element = Factory.newElement(fieldBinding);
					_annoToUnit.put(anno, element);
				}
			}
			MethodBinding[] methodBindings = referenceBinding.methods();
			for (MethodBinding methodBinding : methodBindings) {
				annotationBindings = methodBinding.getAnnotations();
				for (AnnotationBinding annotationBinding : annotationBindings) {
					TypeElement anno = (TypeElement)Factory.newElement(annotationBinding.getAnnotationType()); 
					Element element = Factory.newElement(methodBinding);
					_annoToUnit.put(anno, element);
				}
			}
			ReferenceBinding[] memberTypes = referenceBinding.memberTypes();
			collectAnnotations(memberTypes);
		}
	}

	/**
	 * @return the set of annotation types that were discovered on the root elements.
	 */
	public Set<TypeElement> getRootAnnotations()
	{
		return Collections.unmodifiableSet(_annoToUnit.getKeySet());
	}

	@Override
	public boolean errorRaised()
	{
		return _processingEnv.errorRaised();
	}

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(TypeElement a)
	{
		return _annoToUnit.getValues(a);
	}

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a)
	{
		TypeElement annoType = _processingEnv.getElementUtils().getTypeElement(a.getCanonicalName());
		return _annoToUnit.getValues(annoType);
	}

	@Override
	public Set<? extends Element> getRootElements()
	{
		Set<TypeElement> elements = new HashSet<TypeElement>(_units.length);
		for (CompilationUnitDeclaration unit : _units) {
			if (null == unit.scope || null == unit.scope.topLevelTypes)
				continue;
			for (SourceTypeBinding binding : unit.scope.topLevelTypes) {
				TypeElement element = (TypeElement)Factory.newElement(binding);
				if (null == element) {
					throw new IllegalArgumentException("Top-level type binding could not be converted to element: " + binding); //$NON-NLS-1$
				}
				elements.add(element);
			}
		}
		if (this._binaryTypes != null) {
			for (BinaryTypeBinding typeBinding : _binaryTypes) {
				TypeElement element = (TypeElement)Factory.newElement(typeBinding);
				if (null == element) {
					throw new IllegalArgumentException("Top-level type binding could not be converted to element: " + typeBinding); //$NON-NLS-1$
				}
				elements.add(element);
			}
		}
		return elements;
	}

	@Override
	public boolean processingOver()
	{
		return _isLastRound;
	}

}

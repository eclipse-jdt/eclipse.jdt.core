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
import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class RoundEnvImpl implements RoundEnvironment
{
	//private final BaseProcessingEnvImpl _processingEnv;
	private boolean _errorRaised = false;
	private final boolean _isLastRound;
	private final CompilationUnitDeclaration[] _units;
	private final ManyToMany<TypeElement, Element> _annoToUnit;

	public RoundEnvImpl(CompilationUnitDeclaration[] units, boolean isLastRound, BaseProcessingEnvImpl env) {
		//_processingEnv = env;
		_isLastRound = isLastRound;
		_units = units;
		
		// TODO: deal with inherited annotations (esp. annotations inherited from binary supertypes)
		
		// Discover the annotations that will be passed to Processor.process()
		AnnotationDiscoveryVisitor visitor = new AnnotationDiscoveryVisitor();
		for (CompilationUnitDeclaration unit : _units) {
			unit.traverse(visitor, unit.scope);
		}
		_annoToUnit = visitor._annoToElement;
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
		return _errorRaised;
	}

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(TypeElement a)
	{
		return _annoToUnit.getValues(a);
	}

	@Override
	public Set<? extends Element> getElementsAnnotatedWith(Class<? extends Annotation> a)
	{
		// TODO get the annotation type corresponding to "a".
		return null;
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
		return elements;
	}

	@Override
	public boolean processingOver()
	{
		return _isLastRound;
	}

}

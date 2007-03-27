/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * 
 */
public class TypeParameterElementImpl extends ElementImpl implements TypeParameterElement
{
	private final Element _declaringElement;
	
	// Cache the bounds, because they're expensive to compute
	private List<? extends TypeMirror> _bounds = null;
	
	/* package */ TypeParameterElementImpl(TypeVariableBinding binding, Element declaringElement) {
		super(binding);
		_declaringElement = declaringElement;
	}

	@Override
	public List<? extends TypeMirror> getBounds()
	{
		if (null == _bounds) {
			_bounds = calculateBounds();
		}
		return _bounds;
	}
	
	// This code is drawn from org.eclipse.jdt.core.dom.TypeBinding.getTypeBounds()
	private List<? extends TypeMirror> calculateBounds() {
		TypeVariableBinding typeVariableBinding = (TypeVariableBinding)_binding;
		ReferenceBinding varSuperclass = typeVariableBinding.superclass();
		TypeBinding firstClassOrArrayBound = typeVariableBinding.firstBound;
		int boundsLength = 0;
		if (firstClassOrArrayBound != null) {
			if (firstClassOrArrayBound == varSuperclass) {
				boundsLength++;
			} else if (firstClassOrArrayBound.isArrayType()) { // capture of ? extends/super arrayType
				boundsLength++;
			} else {
				firstClassOrArrayBound = null;
			}
		}
		ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
		int superinterfacesLength = 0;
		if (superinterfaces != null) {
			superinterfacesLength = superinterfaces.length;
			boundsLength += superinterfacesLength;
		}
		if (boundsLength != 0) {
			List<TypeMirror> typeBounds = new ArrayList<TypeMirror>(boundsLength);
			if (firstClassOrArrayBound != null) {
				TypeMirror typeBinding = Factory.newTypeMirror(firstClassOrArrayBound);
				if (typeBinding == null) {
					return Collections.emptyList();
				}
				typeBounds.add(typeBinding);
			}
			if (superinterfaces != null) {
				for (int i = 0; i < superinterfacesLength; i++) {
					TypeMirror typeBinding = Factory.newTypeMirror(superinterfaces[i]);
					if (typeBinding == null) {
						return Collections.emptyList();
					}
					typeBounds.add(typeBinding);
				}
			}
			return Collections.unmodifiableList(typeBounds);
		}
		return Collections.emptyList();
	}

	@Override
	public Element getGenericElement()
	{
		return _declaringElement;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitTypeParameter(this, p);
	}

	/*
	 * (non-Javadoc)
	 * Java does not currently support annotations on type parameters.
	 * @see javax.lang.model.element.Element#getAnnotationMirrors()
	 */
	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors()
	{
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * Always return an empty list; type parameters do not enclose other elements.
	 * @see javax.lang.model.element.Element#getEnclosedElements()
	 */
	@Override
	public List<? extends Element> getEnclosedElements()
	{
		return Collections.emptyList();
	}

	/*
	 * (non-Javadoc)
	 * Always return null.
	 * @see javax.lang.model.element.Element#getEnclosingElement()
	 */
	@Override
	public Element getEnclosingElement()
	{
		return null;
	}

	@Override
	public ElementKind getKind()
	{
		return ElementKind.TYPE_PARAMETER;
	}

}

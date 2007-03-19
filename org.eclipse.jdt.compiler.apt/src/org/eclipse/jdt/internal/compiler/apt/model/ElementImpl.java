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
package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.type.TypeMirror;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Element represents any defined Java language element - a package, 
 * a method, a class or interface.  Contrast with DeclaredType.
 */
public abstract class ElementImpl 
	implements javax.lang.model.element.Element, IElementInfo
{
	
	protected final Binding _binding;
	
	protected ElementImpl(Binding binding) {
		_binding = binding;
	}

	public <R, P> R accept(ElementVisitor<R, P> v, P p) {
		// TODO Auto-generated method stub
		return null;
	}

	public TypeMirror asType() {
		return Factory.newTypeMirror(_binding);
	}

	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		// TODO Auto-generated method stub
		return null;
	}

	protected List<? extends AnnotationMirror> getAnnotationMirrors(AnnotationBinding[] annotations) {
		if (null == annotations || 0 == annotations.length) {
			return Collections.emptyList();
		}
		List<AnnotationMirror> list = new ArrayList<AnnotationMirror>(annotations.length);
		for (AnnotationBinding annotation : annotations) {
			list.add(AnnotationMirrorImpl.getAnnotationMirror(annotation));
		}
		return Collections.unmodifiableList(list);
	}

	public Set<Modifier> getModifiers() {
		// TODO Auto-generated method stub
		return null;
	}

	public Name getSimpleName() {
		return new NameImpl(_binding.shortReadableName());
	}

	@Override
	public int hashCode() {
		return _binding.hashCode();
	}

	// TODO: equals() implemented as == of JDT bindings.  Valid within
	// a single Compiler instance; breaks in IDE if processors cache values. 
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final ElementImpl other = (ElementImpl) obj;
		if (_binding == null) {
			if (other._binding != null)
				return false;
		} else if (_binding != other._binding)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return _binding.toString();
	}

	@Override
	public String getFileName() {
		// Subclasses should override and return something of value
		return null;
	}
}

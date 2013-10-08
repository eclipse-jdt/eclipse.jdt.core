/*******************************************************************************
 * Copyright (c) 2005, 2013 IBM Corporation and others.
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
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;

/**
 * Element represents any defined Java language element - a package, 
 * a method, a class or interface.  Contrast with DeclaredType.
 */
public abstract class ElementImpl 
	implements javax.lang.model.element.Element, IElementInfo
{
	public final BaseProcessingEnvImpl _env;
	public final Binding _binding;
	
	protected ElementImpl(BaseProcessingEnvImpl env, Binding binding) {
		_env = env;
		_binding = binding;
	}

	@Override
	public TypeMirror asType() {
		return _env.getFactory().newTypeMirror(_binding);
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
		return _env.getFactory().getAnnotation(getAnnotationBindings(), annotationClass);
	}
	
	/**
	 * @return the set of compiler annotation bindings on this element
	 */
	protected abstract AnnotationBinding[] getAnnotationBindings();

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		return _env.getFactory().getAnnotationMirrors(getAnnotationBindings());
	}

	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		return _env.getFactory().getAnnotationsByType(getAnnotationBindings(), annotationType);	
	}

	@Override
	public Set<Modifier> getModifiers() {
		// Most subclasses implement this; this default is appropriate for 
		// PackageElement and TypeParameterElement.
		return Collections.emptySet();
	}

	@Override
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
		StringBuffer buf = new StringBuffer();
		List<? extends AnnotationMirror> annots = getAnnotationMirrors();
		for (AnnotationMirror annotationMirror : annots) {
			buf.append(annotationMirror.toString());
			buf.append(' ');
		}
		buf.append(_binding.toString());
		return buf.toString();
	}

	@Override
	public String getFileName() {
		// Subclasses should override and return something of value
		return null;
	}

	/**
	 * @return the package containing this element.  The package of a PackageElement is itself.
	 * @see javax.lang.model.util.Elements#getPackageOf(javax.lang.model.element.Element)
	 */
	abstract /* package */ PackageElement getPackage();

	/**
	 * Subclassed by VariableElementImpl, TypeElementImpl, and ExecutableElementImpl.
	 * This base implementation suffices for other types.
	 * @see Elements#hides
	 * @return true if this element hides {@code hidden}
	 */
	public boolean hides(Element hidden)
	{
		return false;
	}
}

/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.VariableElement;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.VariableBinding;

/**
 * Implementation of VariableElement, which represents a a field, enum constant, 
 * method or constructor parameter, local variable, or exception parameter.
 * In the JDT internal typesystem, this does not correspond to a unitary type:
 * fields are FieldBindings, but parameters are TypeBindings.
 */
public class VariableElementImpl extends ElementImpl implements VariableElement {

	/**
	 * @param binding might be a VariableBinding (for a field) or a TypeBinding (for a method param)
	 */
	VariableElementImpl(Binding binding) {
		super(binding);
	}
	
	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitVariable(this, p);
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		if (_binding instanceof VariableBinding) {
			AnnotationBinding[] annotations = ((VariableBinding)_binding).getAnnotations();
			return Factory.getAnnotationMirrors(annotations);
		}
		else {
			// TODO: how to get annotations from parameters?
			return Collections.emptyList();
		}
	}

	@Override
	public Object getConstantValue() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<? extends Element> getEnclosedElements() {
		return Collections.emptyList();
	}

	@Override
	public Element getEnclosingElement() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ElementKind getKind() {
		if (_binding instanceof FieldBinding) {
			return ElementKind.FIELD;
		}
		else {
			return ElementKind.PARAMETER;
		}
	}

	@Override
	public Set<Modifier> getModifiers()
	{
		if (_binding instanceof VariableBinding) {
			return Factory.getModifiers(((VariableBinding)_binding).modifiers);
		}
		// TODO: for method params, we might want to know about "final".
		return Collections.emptySet();
	}

	@Override
	PackageElement getPackage()
	{
		if (_binding instanceof FieldBinding) {
			PackageBinding pkgBinding = ((FieldBinding)_binding).declaringClass.fPackage;
			return Factory.newPackageElement(pkgBinding);
		}
		else {
			// TODO: what is the package of a method parameter?
			return null;
		}
	}
	
	@Override
	public Name getSimpleName() {
		if (_binding instanceof VariableBinding) {
			return new NameImpl(((VariableBinding)_binding).name);
		}
		// TODO: how can we get the name of a parameter?
		throw new UnsupportedOperationException("NYI"); //$NON-NLS-1$
	}

}

/*******************************************************************************
 * Copyright (c) 2007, 2023 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    IBM Corporation - fix for 342470
 *    IBM Corporation - fix for 342598
 *    IBM Corporation - Java 8 support
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.type.TypeMirror;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

public class TypeParameterElementImpl extends ElementImpl implements TypeParameterElement
{
	private final Element _declaringElement;

	// Cache the bounds, because they're expensive to compute
	private List<? extends TypeMirror> _bounds = null;

	/* package */ TypeParameterElementImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding, Element declaringElement) {
		super(env, binding);
		this._declaringElement = declaringElement;
	}

	/* package */ TypeParameterElementImpl(BaseProcessingEnvImpl env, TypeVariableBinding binding) {
		super(env, binding);
		this._declaringElement = this._env.getFactory().newElement(binding.declaringElement);
	}

	@Override
	public List<? extends TypeMirror> getBounds()
	{
		if (null == this._bounds) {
			this._bounds = calculateBounds();
		}
		return this._bounds;
	}

	private List<? extends TypeMirror> calculateBounds() {
		TypeBinding[] boundBindings = getBoundBindings((TypeVariableBinding)this._binding);
		if (boundBindings.length == 0) {
			// at least we must add java.lang.Object
			return Collections.singletonList(this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().getType(TypeConstants.JAVA_LANG_OBJECT)));
		}
		List<TypeMirror> typeBounds = new ArrayList<>(boundBindings.length);
		for (TypeBinding boundBinding : boundBindings) {
			TypeMirror typeBinding = this._env.getFactory().newTypeMirror(boundBinding);
			if (typeBinding == null) {
				return Collections.emptyList();
			}
			typeBounds.add(typeBinding);
		}
		return Collections.unmodifiableList(typeBounds);
	}

	/**
	 * Answer the bounds of the given type variable in declaration order, or an empty array
	 * if it has no explicit bound (its only bound is then java.lang.Object).
	 * This code is drawn from org.eclipse.jdt.core.dom.TypeBinding.getTypeBounds()
	 */
	static TypeBinding[] getBoundBindings(TypeVariableBinding typeVariableBinding) {
		ReferenceBinding varSuperclass = typeVariableBinding.superclass();
		TypeBinding firstClassOrArrayBound = typeVariableBinding.firstBound;
		boolean isFirstBoundATypeVariable = false;
		if (firstClassOrArrayBound != null) {
			if (firstClassOrArrayBound.isTypeVariable()) {
				isFirstBoundATypeVariable = true;
			}
			if (!TypeBinding.equalsEquals(firstClassOrArrayBound, varSuperclass)
					&& !firstClassOrArrayBound.isArrayType()) { // array: capture of ? extends/super arrayType
				firstClassOrArrayBound = null;
			}
		}
		List<TypeBinding> bounds = new ArrayList<>();
		if (firstClassOrArrayBound != null) {
			bounds.add(firstClassOrArrayBound);
		}
		ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
		// we need to filter out remaining bounds if the first bound is a type variable
		if (superinterfaces != null && !isFirstBoundATypeVariable) {
			Collections.addAll(bounds, superinterfaces);
		}
		return bounds.toArray(new TypeBinding[bounds.size()]);
	}

	@Override
	public Element getGenericElement()
	{
		return this._declaringElement;
	}

	@Override
	public <R, P> R accept(ElementVisitor<R, P> v, P p)
	{
		return v.visitTypeParameter(this, p);
	}

	/*
	 * (non-Javadoc)
	 * Java supports annotations on type parameters from JLS8
	 * @see javax.lang.model.element.Element#getAnnotationMirrors()
	 */
	@Override
	protected AnnotationBinding[] getAnnotationBindings()
	{
		return ((TypeVariableBinding)this._binding).getTypeAnnotations();
	}

	private boolean shouldEmulateJavacBug() {
		if (this._env.getLookupEnvironment().globalOptions.emulateJavacBug8031744) {
			AnnotationBinding [] annotations = getAnnotationBindings();
			for (int i = 0, length = annotations.length; i < length; i++) {
				ReferenceBinding firstAnnotationType = annotations[i].getAnnotationType();
				for (int j = i+1; j < length; j++) {
					ReferenceBinding secondAnnotationType = annotations[j].getAnnotationType();
					if (firstAnnotationType == secondAnnotationType) //$IDENTITY-COMPARISON$
						return true;
				}
			}
		}
		return false;
	}

	@Override
	public List<? extends AnnotationMirror> getAnnotationMirrors() {
		if (shouldEmulateJavacBug())
			return Collections.emptyList();
		return super.getAnnotationMirrors();
	}

	@Override
	@SuppressWarnings("unchecked") // for the cast to A
	public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
		if (shouldEmulateJavacBug())
			return (A[]) Array.newInstance(annotationType, 0);
		return super.getAnnotationsByType(annotationType);
	}

	@Override
	public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
		if (shouldEmulateJavacBug())
			return null;
		return super.getAnnotation(annotationType);
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
		return getGenericElement();
	}

	@Override
	public ElementKind getKind()
	{
		return ElementKind.TYPE_PARAMETER;
	}

	@Override
	PackageElement getPackage()
	{
		// TODO what is the package of a type parameter?
		return null;
	}

	@Override
	public String toString() {
		return new String(this._binding.readableName());
	}
}

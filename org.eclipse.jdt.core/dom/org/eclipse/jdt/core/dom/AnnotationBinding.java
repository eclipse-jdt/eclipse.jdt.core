/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *    IBM Corporation - implemented methods from IBinding
 *    IBM Corporation - renamed from ResolvedAnnotation to AnnotationBinding
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * Internal class
 */
class AnnotationBinding implements IAnnotationBinding {
	static final AnnotationBinding[] NoAnnotations = new AnnotationBinding[0];
	private org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding internalAnnotation;
	private BindingResolver bindingResolver;

	AnnotationBinding(org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding annotation, BindingResolver resolver) {
		if (annotation == null)
			throw new IllegalStateException();
		internalAnnotation = annotation;
		bindingResolver = resolver;
	}
	
	public IAnnotationBinding[] getAnnotations() {
		return NoAnnotations;
	}

	public ITypeBinding getAnnotationType() {
		ITypeBinding binding = this.bindingResolver.getTypeBinding(this.internalAnnotation.getAnnotationType());
		if (binding == null || !binding.isAnnotation())
			return null;
		return binding;
	}
	
	public IMemberValuePairBinding[] getDeclaredMemberValuePairs() {
		ElementValuePair[] internalPairs = this.internalAnnotation.getElementValuePairs();
		int length = internalPairs.length;
		IMemberValuePairBinding[] pairs = length == 0 ? MemberValuePairBinding.NoPair : new MemberValuePairBinding[length];
		for (int i = 0; i < length; i++)
			pairs[i] = this.bindingResolver.getMemberValuePairBinding(internalPairs[i]);
		return pairs;
	}

	public IMemberValuePairBinding[] getAllMemberValuePairs() {
		IMemberValuePairBinding[] pairs = getDeclaredMemberValuePairs();
		ReferenceBinding typeBinding = this.internalAnnotation.getAnnotationType();
		if (typeBinding == null) return pairs;
		MethodBinding[] methods = typeBinding.methods();
		int methodLength = methods == null ? 0 : methods.length;
		if (methodLength == 0) return pairs;

		int declaredLength = pairs.length;
		if (declaredLength == methodLength)
			return pairs;

		HashtableOfObject table = new HashtableOfObject(declaredLength);
		for (int i = 0; i < declaredLength; i++)
			table.put(((MemberValuePairBinding) pairs[i]).internalName(), pairs[i]);

		// handle case of more methods than declared members
		IMemberValuePairBinding[] allPairs = new  IMemberValuePairBinding[methodLength];
		for (int i = 0; i < methodLength; i++) {
			Object pair = table.get(methods[i].selector);
			allPairs[i] = pair == null ? new DefaultValuePairBinding(methods[i], this.bindingResolver) : (IMemberValuePairBinding) pair;
		}
		return allPairs;
	}
	
	public IJavaElement getJavaElement() {
		ITypeBinding annotationType = getAnnotationType();
		if (annotationType == null)
			return null;
		return annotationType.getJavaElement();
	}

	public String getKey() {
		// TODO when implementing, update spec in IBinding
		return null;
	}

	public int getKind() {
		return IBinding.ANNOTATION;
	}

	public int getModifiers() {
		return -1;
	}

	public String getName() {
		ITypeBinding annotationType = getAnnotationType();
		if (annotationType == null)
			return new String(this.internalAnnotation.getAnnotationType().sourceName());
		else
			return annotationType.getName();
	}
	
	public boolean isDeprecated() {
		return false;
	}
	
	public boolean isEqualTo(IBinding binding) {
		if (this == binding)
			return true;
		if (binding.getKind() != IBinding.ANNOTATION)
			return false;
		IAnnotationBinding other = (IAnnotationBinding) binding;
		if (!getAnnotationType().isEqualTo(other.getAnnotationType()))
			return false;
		IMemberValuePairBinding[] memberValuePairs = getDeclaredMemberValuePairs();
		IMemberValuePairBinding[] otherMemberValuePairs = other.getDeclaredMemberValuePairs();
		if (memberValuePairs.length != otherMemberValuePairs.length)
			return false;
		for (int i = 0, length = memberValuePairs.length; i < length; i++) {
			if (!memberValuePairs[i].isEqualTo(otherMemberValuePairs[i]))
				return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IBinding#isRecovered()
	 */
	public boolean isRecovered() {
		return false;
	}

	public boolean isSynthetic() {
		return false;
	}

	public String toString() {
		ITypeBinding type = getAnnotationType();
		final StringBuffer buffer = new StringBuffer();
		buffer.append('@');
		if (type != null)
			buffer.append(type.getName());
		buffer.append('(');
		IMemberValuePairBinding[] pairs = getDeclaredMemberValuePairs();
		for (int i = 0, len = pairs.length; i < len; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(pairs[i].toString());
		}
		buffer.append(')');
		return buffer.toString();
	}
	
}

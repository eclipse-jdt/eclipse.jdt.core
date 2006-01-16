/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.lookup.AnnotationBinding;
import org.eclipse.jdt.internal.compiler.lookup.ElementValuePair;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.util.*;

/**
 * Internal class
 */
class ResolvedAnnotation implements IResolvedAnnotation {
	static final ResolvedAnnotation[] NoAnnotations = new ResolvedAnnotation[0];
	private AnnotationBinding internalAnnotation;
	private BindingResolver bindingResolver;

	ResolvedAnnotation(AnnotationBinding annotation, BindingResolver resolver) {
		if (annotation == null)
			throw new IllegalStateException();
		internalAnnotation = annotation;
		bindingResolver = resolver;
	}

	public ITypeBinding getAnnotationType() {
		ITypeBinding binding = this.bindingResolver.getTypeBinding(this.internalAnnotation.getAnnotationType());
		if (binding == null || !binding.isAnnotation())
			return null;
		return binding;
	}

	public IResolvedMemberValuePair[] getDeclaredMemberValuePairs() {
		ElementValuePair[] internalPairs = this.internalAnnotation.getElementValuePairs();
		int length = internalPairs.length;
		IResolvedMemberValuePair[] pairs = length == 0 ? ResolvedMemberValuePair.NoPair : new ResolvedMemberValuePair[length];
		for (int i = 0; i < length; i++)
			pairs[i] = new ResolvedMemberValuePair(internalPairs[i], this.bindingResolver);
		return pairs;
	}

	public IResolvedMemberValuePair[] getAllMemberValuePairs() {
		IResolvedMemberValuePair[] pairs = getDeclaredMemberValuePairs();
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
			table.put(((ResolvedMemberValuePair) pairs[i]).internalName(), pairs[i]);

		// handle case of more methods than declared members
		IResolvedMemberValuePair[] allPairs = new  IResolvedMemberValuePair[methodLength];
		for (int i = 0; i < methodLength; i++) {
			Object pair = table.get(methods[i].selector);
			allPairs[i] = pair == null ? new ResolvedDefaultValuePair(methods[i], this.bindingResolver) : (IResolvedMemberValuePair) pair;
		}
		return allPairs;
	}

	public String toString() {
		ITypeBinding type = getAnnotationType();
		final StringBuffer buffer = new StringBuffer();
		buffer.append('@');
		if (type != null)
			buffer.append(type.getName());
		buffer.append('(');
		IResolvedMemberValuePair[] pairs = getDeclaredMemberValuePairs();
		for (int i = 0, len = pairs.length; i < len; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(pairs[i].toString());
		}
		buffer.append(')');
		return buffer.toString();
	}
}

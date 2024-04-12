/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac.dom;

import java.util.Objects;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;

import com.sun.tools.javac.code.Attribute.Compound;

public class JavacAnnotationBinding implements IAnnotationBinding {

	private final JavacBindingResolver resolver;
	private final Compound annotation;

	private transient String key;
	private final IBinding recipient;

	public JavacAnnotationBinding(Compound ann, JavacBindingResolver resolver, IBinding recipient) {
		this.resolver = resolver;
		this.annotation = ann;
		this.recipient = recipient;
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return new IAnnotationBinding[0];
	}

	@Override
	public int getKind() {
		return ANNOTATION;
	}

	@Override
	public int getModifiers() {
		return getAnnotationType().getModifiers();
	}

	@Override
	public boolean isDeprecated() {
		return getAnnotationType().isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		throw new UnsupportedOperationException("Unimplemented method 'isRecovered'");
	}

	@Override
	public boolean isSynthetic() {
		return getAnnotationType().isSynthetic();
	}

	@Override
	public IJavaElement getJavaElement() {
		return getAnnotationType().getJavaElement();
	}

	@Override
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.recipient.getKey());
		builder.append('@');
		builder.append(this.getAnnotationType().getKey());
		return builder.toString();
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacAnnotationBinding other && Objects.equals(this.annotation, other.annotation);
	}

	@Override
	public IMemberValuePairBinding[] getAllMemberValuePairs() {
		return this.annotation.getElementValues().entrySet().stream()
			.map(entry -> new JavacMemberValuePairBinding(entry.getKey(), entry.getValue(), this.resolver))
			.toArray(IMemberValuePairBinding[]::new);
	}

	@Override
	public ITypeBinding getAnnotationType() {
		return new JavacTypeBinding(this.annotation.type, this.resolver, null);
	}

	@Override
	public IMemberValuePairBinding[] getDeclaredMemberValuePairs() {
		return getAllMemberValuePairs();
	}

	@Override
	public String getName() {
		return getAnnotationType().getName();
	}

}

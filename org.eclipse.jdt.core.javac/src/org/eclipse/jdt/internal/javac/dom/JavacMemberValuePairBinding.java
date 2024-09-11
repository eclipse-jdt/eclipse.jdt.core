/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
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
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol.MethodSymbol;

public abstract class JavacMemberValuePairBinding implements IMemberValuePairBinding {

	public final JavacMethodBinding method;
	public final Attribute value;
	private final JavacBindingResolver resolver;
	
	public JavacMemberValuePairBinding(MethodSymbol key, Attribute value, JavacBindingResolver resolver) {
		this.method = resolver.bindings.getMethodBinding(key.type.asMethodType(), key, null, true);
		this.value = value;
		this.resolver = resolver;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacMemberValuePairBinding other
				&& Objects.equals(this.resolver, other.resolver)
				&& Objects.equals(this.method, other.method)
				&& Objects.equals(this.value, other.value);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.resolver, this.method, this.value);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return new IAnnotationBinding[0];
	}

	@Override
	public int getKind() {
		return MEMBER_VALUE_PAIR;
	}

	@Override
	public int getModifiers() {
		return method.getModifiers();
	}

	@Override
	public boolean isDeprecated() {
		return method.isDeprecated();
	}

	@Override
	public boolean isRecovered() {
		return this.value instanceof Attribute.Error;
	}

	@Override
	public boolean isSynthetic() {
		return method.isSynthetic();
	}

	@Override
	public IJavaElement getJavaElement() {
		return method.getJavaElement();
	}

	@Override
	public String getKey() {
		// as of writing, not yet implemented for ECJ
		// @see org.eclipse.jdt.core.dom.MemberValuePairBinding.getKey
		return null;
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof IMemberValuePairBinding other && Objects.equals(this.getKey(), other.getKey());
	}

	@Override
	public String getName() {
		return this.method.getName();
	}

	@Override
	public IMethodBinding getMethodBinding() {
		return this.method;
	}

	@Override
	public Object getValue() {
		return this.resolver.getValueFromAttribute(this.value);
	}

	@Override
	public boolean isDefault() {
		return this.value == this.method.methodSymbol.defaultValue;
	}

	@Override
	public String toString() {
		return getName() + " = " + getValue().toString(); //$NON-NLS-1$
	}
}

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

public class JavacMemberValuePairBinding implements IMemberValuePairBinding {

	public final JavacMethodBinding method;
	public final Attribute value;

	public JavacMemberValuePairBinding(MethodSymbol key, Attribute value, JavacBindingResolver resolver) {
		this.method = new JavacMethodBinding(key, resolver);
		this.value = value;
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isRecovered'");
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
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'getKey'");
	}

	@Override
	public boolean isEqualTo(IBinding binding) {
		return binding instanceof JavacMemberValuePairBinding other && this.method.isEqualTo(other.method)
			&& Objects.equals(this.value, other.value);
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
		throw new UnsupportedOperationException("Unimplemented method 'getValue'");
	}

	@Override
	public boolean isDefault() {
		// TODO Auto-generated method stub
		throw new UnsupportedOperationException("Unimplemented method 'isDefault'");
	}

}

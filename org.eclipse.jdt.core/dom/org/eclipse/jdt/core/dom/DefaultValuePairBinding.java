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
 *    IBM Corporation - renamed from ResolvedDefaultValuePair to DefaultValuePairBinding
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.dom.BindingResolver;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;

/**
 * Member value pair which compose of default values.
 */
class DefaultValuePairBinding implements IMemberValuePairBinding {

	private org.eclipse.jdt.internal.compiler.lookup.MethodBinding method;
	private Object domValue;
	private BindingResolver bindingResolver;

	static void appendValue(Object value, StringBuffer buffer) {
		if (value instanceof Object[]) {
			Object[] values = (Object[]) value;
			buffer.append('{');
			for (int i = 0, l = values.length; i < l; i++) {
				if (i != 0)
					buffer.append(", "); //$NON-NLS-1$
				appendValue(values[i], buffer);
			}
			buffer.append('}');
		} else if (value instanceof ITypeBinding) {
			buffer.append(((ITypeBinding) value).getName());
			buffer.append(".class"); //$NON-NLS-1$
		} else {
			buffer.append(value);
		}
	}

	DefaultValuePairBinding(org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding, BindingResolver resolver) {
		this.method = binding;
		this.domValue = MemberValuePairBinding.buildDOMValue(binding.getDefaultValue(), resolver);
		this.bindingResolver = resolver;
	}

	public IAnnotationBinding[] getAnnotations() {
		return AnnotationBinding.NoAnnotations;
	}

	public IJavaElement getJavaElement() {
		return null;
	}

	public String getKey() {
		// TODO when implementing, update spec in IBinding
		return null;
	}

	public int getKind() {
		return IBinding.MEMBER_VALUE_PAIR;
	}

	public IMethodBinding getMethodBinding() {
		return this.bindingResolver.getMethodBinding(this.method);
	}

	public int getModifiers() {
		return -1;
	}

	public String getName() {
		return new String(this.method.selector);
	}

	public Object getValue() {
		return this.domValue;
	}

	public boolean isDefault() {
		return true;
	}

	public boolean isDeprecated() {
		return false;
	}

	public boolean isEqualTo(IBinding binding) {
		if (this == binding)
			return true;
		if (binding.getKind() != IBinding.MEMBER_VALUE_PAIR)
			return false;
		IMemberValuePairBinding other = (IMemberValuePairBinding) binding;
		if (!getMethodBinding().isEqualTo(other.getMethodBinding()))
			return false;
		Object value = getValue();
		if (value == null)
			return other.getValue() == null;
		return value.equals(other.getValue());
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
		StringBuffer buffer = new StringBuffer();
		toString(buffer);
		return buffer.toString();
	}

	public void toString(StringBuffer buffer) {
		buffer.append(getName());
		buffer.append(" = "); //$NON-NLS-1$
		appendValue(getValue(), buffer);
	}
}

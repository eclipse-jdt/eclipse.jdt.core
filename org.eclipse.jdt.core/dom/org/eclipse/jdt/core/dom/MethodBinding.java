/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;

/**
 * Internal implementation of method bindings.
 */
class MethodBinding implements IMethodBinding {

	private static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding;
	private BindingResolver resolver;
	private ITypeBinding[] parameterTypes;
	private ITypeBinding[] exceptionTypes;
	private String name;
	private ITypeBinding declaringClass;
	private ITypeBinding returnType;
	private String key;
	
	MethodBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.MethodBinding binding) {
		this.resolver = resolver;
		this.binding = binding;
	}
	
	/*
	 * @see IMethodBinding#isConstructor()
	 */
	public boolean isConstructor() {
		return this.binding.isConstructor();
	}
	
	/*
	 * @see IMethodBinding#isDefaultConstructor()
	 * @since 3.0
	 */
	public boolean isDefaultConstructor() {
		if (this.binding.declaringClass.isBinaryBinding()) {
			return false;
		}
		return (this.binding.modifiers & CompilerModifiers.AccIsDefaultConstructor) != 0;
	}	

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (name == null) {
			if (this.binding.isConstructor()) {
				name = this.getDeclaringClass().getName();
			} else {
				name = new String(this.binding.selector);
			}
		}
		return name;
	}

	/*
	 * @see IMethodBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (this.declaringClass == null) {
			this.declaringClass = this.resolver.getTypeBinding(this.binding.declaringClass);
		}
		return declaringClass;
	}

	/*
	 * @see IMethodBinding#getParameterTypes()
	 */
	public ITypeBinding[] getParameterTypes() {
		if (this.parameterTypes != null) {
			return parameterTypes;
		}
		org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] parameters = this.binding.parameters;
		int length = parameters.length;
		if (length == 0) {
			return NO_TYPE_BINDINGS;
		}
		this.parameterTypes = new ITypeBinding[length];
		for (int i = 0; i < length; i++) {
			this.parameterTypes[i] = this.resolver.getTypeBinding(parameters[i]);
		}
		return this.parameterTypes;
	}

	/*
	 * @see IMethodBinding#getReturnType()
	 */
	public ITypeBinding getReturnType() {
		if (this.returnType == null) {
			this.returnType = this.resolver.getTypeBinding(this.binding.returnType);
		}
		return this.returnType;
	}

	/*
	 * @see IMethodBinding#getExceptionTypes()
	 */
	public ITypeBinding[] getExceptionTypes() {
		if (this.exceptionTypes != null) {
			return exceptionTypes;
		}
		org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] exceptions = this.binding.thrownExceptions;
		int length = exceptions.length;
		if (length == 0) {
			return NO_TYPE_BINDINGS;
		}
		this.exceptionTypes = new ITypeBinding[length];
		for (int i = 0; i < length; i++) {
			this.exceptionTypes[i] = this.resolver.getTypeBinding(exceptions[i]);
		}
		return this.exceptionTypes;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.METHOD;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		return this.binding.getAccessFlags();
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		return this.binding.isDeprecated();
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return this.binding.isSynthetic();
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			StringBuffer buffer = new StringBuffer();
			buffer.append(this.getDeclaringClass().getKey());
			buffer.append('/');
			ITypeBinding _returnType = getReturnType();
			if (_returnType != null) {
				if (_returnType.isTypeVariable()) {
					buffer.append(_returnType.getQualifiedName());
				} else {
					buffer.append(_returnType.getKey());
				}
			}
			if (!isConstructor()) {
				buffer.append(this.getName());
			}
			ITypeBinding[] parameters = getParameterTypes();
			buffer.append('(');
			for (int i = 0, max = parameters.length; i < max; i++) {
				final ITypeBinding parameter = parameters[i];
				if (parameter != null) {
					if (parameter.isTypeVariable()) {
						buffer.append(parameter.getQualifiedName());
					} else {
						buffer.append(parameter.getKey());
					}
				}
			}
			buffer.append(')');
			ITypeBinding[] thrownExceptions = getExceptionTypes();
			for (int i = 0, max = thrownExceptions.length; i < max; i++) {
				final ITypeBinding thrownException = thrownExceptions[i];
				if (thrownException != null) {
					if (thrownException.isTypeVariable()) {
						buffer.append(thrownException.getQualifiedName());					
					} else {
						buffer.append(thrownException.getKey());
					}
				}
			}
			this.key = String.valueOf(buffer);
		}
		return this.key;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#getTypeParameters()
	 */
	public ITypeBinding[] getTypeParameters() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#getTypeArguments()
	 */
	public ITypeBinding[] getTypeArguments() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#isParameterizedMethod()
	 */
	public boolean isParameterizedMethod() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#isRawMethod()
	 */
	public boolean isRawMethod() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#getErasure()
	 */
	public IMethodBinding getErasure() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return this;
	}

	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}

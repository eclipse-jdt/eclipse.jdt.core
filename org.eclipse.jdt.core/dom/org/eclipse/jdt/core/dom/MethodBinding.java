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
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

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
	private ITypeBinding[] typeParameters;
	private ITypeBinding[] typeArguments;
	
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
			this.parameterTypes = NO_TYPE_BINDINGS;
		} else {
			this.parameterTypes = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				this.parameterTypes[i] = this.resolver.getTypeBinding(parameters[i]);
			}
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
			this.exceptionTypes = NO_TYPE_BINDINGS;
		} else {
			this.exceptionTypes = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				this.exceptionTypes[i] = this.resolver.getTypeBinding(exceptions[i]);
			}
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
				} else if (_returnType.isArray() && _returnType.getElementType().isTypeVariable()) {
					int dimensions = _returnType.getDimensions();
					buffer.append(_returnType.getElementType().getQualifiedName());
					for (int i = 0; i < dimensions; i++) {
						buffer.append('[').append(']');
					}
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
					} else if (parameter.isArray() && parameter.getElementType().isTypeVariable()) {
						int dimensions = parameter.getDimensions();
						buffer.append(parameter.getElementType().getQualifiedName());
						for (int j = 0; j < dimensions; j++) {
							buffer.append('[').append(']');
						}
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
					} else if (thrownException.isArray() && thrownException.getElementType().isTypeVariable()) {
						int dimensions = thrownException.getDimensions();
						buffer.append(thrownException.getElementType().getQualifiedName());
						for (int j = 0; j < dimensions; j++) {
							buffer.append('[').append(']');
						}
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
		if (this.typeParameters != null) {
			return this.typeParameters;
		}
		TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
		if (typeVariableBindings != null) {
			int typeVariableBindingsLength = typeVariableBindings.length;
			if (typeVariableBindingsLength != 0) {
				this.typeParameters = new ITypeBinding[typeVariableBindingsLength];
				for (int i = 0; i < typeVariableBindingsLength; i++) {
					typeParameters[i] = this.resolver.getTypeBinding(typeVariableBindings[i]);
				}
			} else {
				this.typeParameters = NO_TYPE_BINDINGS;
			}
		} else {
			this.typeParameters = NO_TYPE_BINDINGS;
		}
		return this.typeParameters;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#getTypeArguments()
	 */
	public ITypeBinding[] getTypeArguments() {
		if (this.typeArguments != null) {
			return this.typeArguments;
		}

		if (this.binding instanceof ParameterizedGenericMethodBinding) {
			ParameterizedGenericMethodBinding genericMethodBinding = (ParameterizedGenericMethodBinding) this.binding;
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] typeArgumentsBindings = genericMethodBinding.typeArguments;
			if (typeArgumentsBindings != null) {
				int typeArgumentsLength = typeArgumentsBindings.length;
				if (typeArgumentsLength != 0) {
					this.typeArguments = new ITypeBinding[typeArgumentsLength];
					for (int i = 0; i < typeArgumentsLength; i++) {
						this.typeArguments[i] = this.resolver.getTypeBinding(typeArgumentsBindings[i]);
					}
				} else {
					this.typeArguments = NO_TYPE_BINDINGS;
				}
			} else {
				this.typeArguments = NO_TYPE_BINDINGS;
			}
		} else {
			this.typeArguments = NO_TYPE_BINDINGS;
		}
		return this.typeArguments;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#isParameterizedMethod()
	 */
	public boolean isParameterizedMethod() {
		return this.binding instanceof ParameterizedGenericMethodBinding;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#isRawMethod()
	 */
	public boolean isRawMethod() {
		return !(this.binding instanceof ParameterizedGenericMethodBinding);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#getErasure()
	 */
	public IMethodBinding getErasure() {
		return this.resolver.getMethodBinding(this.binding.original());
	}

	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}

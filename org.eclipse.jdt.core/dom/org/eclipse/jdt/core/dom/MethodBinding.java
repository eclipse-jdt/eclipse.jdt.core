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

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.ParameterizedGenericMethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeVariableBinding;

/**
 * Internal implementation of method bindings.
 */
class MethodBinding implements IMethodBinding {

	private static final int VALID_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
		Modifier.ABSTRACT | Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED | Modifier.NATIVE |
		Modifier.STRICTFP;
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
	 * Returns the signature of the given type.
	 */
	private String getSignature(Type type) {
		StringBuffer buffer = new StringBuffer();
		getFullyQualifiedName(type, buffer);
		return Signature.createTypeSignature(buffer.toString(), false/*not resolved in source*/);
	}
	
	/*
	 * Appends to the given buffer the fully qualified name (as it appears in the source) of the given type
	 */
	private void getFullyQualifiedName(Type type, StringBuffer buffer) {
		if (type.isArrayType()) {
			ArrayType arrayType = (ArrayType) type;
			getFullyQualifiedName(arrayType.getElementType(), buffer);
			for (int i = 0, length = arrayType.getDimensions(); i < length; i++) {
				buffer.append('[');
				buffer.append(']');
			}
		} else if (type.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			getFullyQualifiedName(parameterizedType.getType(), buffer);
			buffer.append('<');
			Iterator iterator = parameterizedType.typeArguments().iterator();
			boolean isFirst = true;
			while (iterator.hasNext()) {
				if (!isFirst)
					buffer.append(',');
				else
					isFirst = false;
				Type typeArgument = (Type) iterator.next();
				getFullyQualifiedName(typeArgument, buffer);
			}
			buffer.append('>');
		} else if (type.isPrimitiveType()) {
			buffer.append(((PrimitiveType) type).getPrimitiveTypeCode().toString());
		} else if (type.isQualifiedType()) {
			buffer.append(((QualifiedType) type).getName().getFullyQualifiedName());
		} else if (type.isSimpleType()) {
			buffer.append(((SimpleType) type).getName().getFullyQualifiedName());
		} else if (type.isWildcardType()) {
			buffer.append('?');
			WildcardType wildcardType = (WildcardType) type;
			Type bound = wildcardType.getBound();
			if (bound == null) return;
			if (wildcardType.isUpperBound()) {
				buffer.append(" extends "); //$NON-NLS-1$
			} else {
				buffer.append(" super "); //$NON-NLS-1$
			}
			getFullyQualifiedName(bound, buffer);
		}
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
	 * @see IBinding#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		IType declaringType = (IType) getDeclaringClass().getJavaElement();
		if (declaringType == null) return null;
		if (!(this.resolver instanceof DefaultBindingResolver)) return null;
		MethodDeclaration method = (MethodDeclaration) ((DefaultBindingResolver) this.resolver).bindingsToAstNodes.get(this);
		if (method == null) return null;
		ArrayList parameterSignatures = new ArrayList();
		Iterator iterator = method.parameters().iterator();
		while (iterator.hasNext()) {
			SingleVariableDeclaration parameter = (SingleVariableDeclaration) iterator.next();
			Type type = parameter.getType();
			parameterSignatures.add(getSignature(type));
		}
		int parameterCount = parameterSignatures.size();
		String[] parameters = new String[parameterCount];
		parameterSignatures.toArray(parameters);
		return declaringType.getMethod(getName(), parameters);
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
		return this.binding.getAccessFlags() & VALID_MODIFIERS;
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

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.IMethodBinding#isVarargs()
	 * @since 3.1
	 */
	public boolean isVarargs() {
		return this.binding.isVarargs();
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			StringBuffer buffer = new StringBuffer();
			TypeBinding typeBinding = (TypeBinding) getDeclaringClass();
			typeBinding.appendKey(buffer);
			buffer.append('/');
			if (!isConstructor()) {
				buffer.append(getName());
			}
			ITypeBinding[] parameters = getParameterTypes();
			buffer.append('(');
			for (int i = 0, max = parameters.length; i < max; i++) {
				TypeBinding parameter = (TypeBinding) parameters[i];
				if (parameter != null) {
					parameter.appendParameterKey(buffer);
					buffer.append(',');
				}
			}
			buffer.append(')');
			
			// only one of the type parameters or type arguments is non-empty at the same time
			typeBinding.appendTypeParameters(buffer, getTypeParameters());
			typeBinding.appendTypeArguments(buffer, getTypeArguments());
			
			this.key = buffer.toString();
		}
		return this.key;
	}
	
	/*
	 * @see IBinding#isEqualTo(Binding)
	 * @since 3.1
	 */
	public boolean isEqualTo(IBinding other) {
		if (other == this) {
			// identical binding - equal (key or no key)
			return true;
		}
		if (other == null) {
			// other binding missing
			return false;
		}
		if (!(other instanceof MethodBinding)) {
			return false;
		}
		org.eclipse.jdt.internal.compiler.lookup.MethodBinding otherBinding = ((MethodBinding) other).binding;
		return BindingComparator.isEqual(this.binding, otherBinding);
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

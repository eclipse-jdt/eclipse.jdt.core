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

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.ArrayBinding;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Internal implementation of type bindings.
 */
class TypeBinding implements ITypeBinding {

	private static final String NO_NAME = ""; //$NON-NLS-1$	
	private static final ITypeBinding[] NO_TYPE_BINDINGS = new ITypeBinding[0];
	private static final IVariableBinding[] NO_VARIABLE_BINDINGS = new IVariableBinding[0];
	private static final IMethodBinding[] NO_METHOD_BINDINGS = new IMethodBinding[0];
	
	private org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding;
	private BindingResolver resolver;
	private String key;
	
	public TypeBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.TypeBinding binding) {
		this.binding = binding;
		this.resolver = resolver;
	}
	
	/*
	 * @see ITypeBinding#isPrimitive()
	 */
	public boolean isPrimitive() {
		return !isNullType() && binding.isBaseType();
	}

	/*
	 * @see ITypeBinding#isArray()
	 */
	public boolean isArray() {
		return binding.isArrayType();
	}

	/*
	 * @see ITypeBinding#getElementType()
	 */
	public ITypeBinding getElementType() {
		if (!this.isArray()) {
			return null;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return resolver.getTypeBinding(arrayBinding.leafComponentType);
	}

	/*
	 * @see ITypeBinding#getDimensions()
	 */
	public int getDimensions() {
		if (!this.isArray()) {
			return 0;
		}
		ArrayBinding arrayBinding = (ArrayBinding) binding;
		return arrayBinding.dimensions;
	}

	/*
	 * @see ITypeBinding#isClass()
	 */
	public boolean isClass() {
		return this.binding.isClass();
	}

	/*
	 * @see ITypeBinding#isInterface()
	 */
	public boolean isInterface() {
		return this.binding.isInterface();
	}

	/*
	 * @see ITypeBinding#isTypeVariable()
	 */
	public boolean isTypeVariable() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isAnonymousType()) {
				return NO_NAME;
			} else {
				char[] shortName = referenceBinding.shortReadableName();
				if (referenceBinding.isMemberType() || referenceBinding.isLocalType()) {
					return new String(CharOperation.subarray(shortName, CharOperation.lastIndexOf('.', shortName) + 1, shortName.length));
				} else {
					return new String(shortName);
				}
			}
		} else if (this.binding.isArrayType()) {
			ArrayBinding arrayBinding = (ArrayBinding) this.binding;
			int dimensions = arrayBinding.dimensions;
			char[] brackets = new char[dimensions * 2];
			for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
				brackets[i] = ']';
				brackets[i - 1] = '[';
			}
			StringBuffer buffer = new StringBuffer();
			org.eclipse.jdt.internal.compiler.lookup.TypeBinding leafComponentTypeBinding = arrayBinding.leafComponentType;
			if (leafComponentTypeBinding.isClass() || leafComponentTypeBinding.isInterface()) {
				ReferenceBinding referenceBinding2 = (ReferenceBinding) leafComponentTypeBinding;
				char[] shortName = referenceBinding2.shortReadableName();
				if (referenceBinding2.isMemberType() || referenceBinding2.isLocalType()) {
					buffer.append(CharOperation.subarray(shortName, CharOperation.lastIndexOf('.', shortName) + 1, shortName.length));
				} else {
					buffer.append(shortName);
				}
			} else {
				buffer.append(leafComponentTypeBinding.readableName());
			}
			buffer.append(brackets);
			return buffer.toString();
		} else {
			return new String(this.binding.readableName());
		}
	}

	/*
	 * @see ITypeBinding#getPackage()
	 */
	public IPackageBinding getPackage() {
		if (this.binding.isBaseType() || this.binding.isArrayType()) {
			return null;
		} else {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return this.resolver.getPackageBinding(referenceBinding.getPackage());
		}
	}

	/*
	 * @see ITypeBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (this.binding.isArrayType() || this.binding.isBaseType()) {
			return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		if (referenceBinding.isNestedType()) {
			try {
				return this.resolver.getTypeBinding(referenceBinding.enclosingType());
			} catch (RuntimeException e) {
				/* in case a method cannot be resolvable due to missing jars on the classpath
				 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
				 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
				 */
			}
		}
		return null;
	}

	/*
	 * @see ITypeBinding#getSuperclass()
	 */
	public ITypeBinding getSuperclass() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType() || this.binding.isInterface()) {
			return null;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding superclass = null;
		try {
			superclass = referenceBinding.superclass();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
			return this.resolver.resolveWellKnownType("java.lang.Object"); //$NON-NLS-1$
		}
		if (superclass == null) {
			return null;
		}
		return this.resolver.getTypeBinding(superclass);		
	}

	/*
	 * @see ITypeBinding#getInterfaces()
	 */
	public ITypeBinding[] getInterfaces() {
		if (this.binding == null || this.binding.isArrayType() || this.binding.isBaseType()) {
			return NO_TYPE_BINDINGS;
		}
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		ReferenceBinding[] interfaces = null;
		try {
			interfaces = referenceBinding.superInterfaces();
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		if (interfaces == null) {
			return NO_TYPE_BINDINGS;
		}
		int length = interfaces.length;
		if (length == 0) {
			return NO_TYPE_BINDINGS;
		} else {
			ITypeBinding[] newInterfaces = new ITypeBinding[length];
			for (int i = 0; i < length; i++) {
				newInterfaces[i] = this.resolver.getTypeBinding(interfaces[i]);
			}
			return newInterfaces;
		}
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (this.binding.isClass()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			if (referenceBinding.isAnonymousType()) {
				return referenceBinding.getAccessFlags() & ~Modifier.FINAL;
			}
			return referenceBinding.getAccessFlags();
		} else if (this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			// clear the AccAbstract and the AccInterface bits
			return referenceBinding.getAccessFlags() & ~(Modifier.ABSTRACT | 0x200);
		} else {
			return 0;
		}
	}

	/*
	 * @see ITypeBinding#getDeclaredModifiers()
	 */
	public int getDeclaredModifiers() {
		return getModifiers();
	}

	/*
	 * @see ITypeBinding#isTopLevel()
	 */
	public boolean isTopLevel() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isNested()
	 */
	public boolean isNested() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isNestedType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isMember()
	 */
	public boolean isMember() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isLocal()
	 */
	public boolean isLocal() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isLocalType() && !referenceBinding.isMemberType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#isAnonymous()
	 */
	public boolean isAnonymous() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isAnonymousType();
		}
		return false;
	}

	/*
	 * @see ITypeBinding#getDeclaredTypes()
	 */
	public ITypeBinding[] getDeclaredTypes() {
		try {
			if (this.binding.isClass() || this.binding.isInterface()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				ReferenceBinding[] members = referenceBinding.memberTypes();
				int length = members.length;
				ITypeBinding[] newMembers = new ITypeBinding[length];
				for (int i = 0; i < length; i++) {
					newMembers[i] = this.resolver.getTypeBinding(members[i]);
				}
				return newMembers;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_TYPE_BINDINGS;
	}
	
	/*
	 * @see ITypeBinding#getBinaryName()
	 * @since 3.0
	 */
	public String getBinaryName() {
		char[] constantPoolName = this.binding.constantPoolName();
		if (constantPoolName == null) return null;
		char[] dotSeparated = CharOperation.replaceOnCopy(constantPoolName, '/', '.');
		return new String(dotSeparated);
	}

	/*
	 * @see ITypeBinding#getDeclaredFields()
	 */
	public IVariableBinding[] getDeclaredFields() {
		try {
			if (this.binding.isClass() || this.binding.isInterface()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				FieldBinding[] fields = referenceBinding.fields();
				int length = fields.length;
				IVariableBinding[] newFields = new IVariableBinding[length];
				for (int i = 0; i < length; i++) {
					newFields[i] = this.resolver.getVariableBinding(fields[i]);
				}
				return newFields;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_VARIABLE_BINDINGS;
	}

	/*
	 * @see ITypeBinding#getDeclaredMethods()
	 */
	public IMethodBinding[] getDeclaredMethods() {
		try {
			if (this.binding.isClass() || this.binding.isInterface()) {
				ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
				org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] methods = referenceBinding.methods();
				int length = methods.length;
				int removeSyntheticsCounter = 0;
				IMethodBinding[] newMethods = new IMethodBinding[length];
				for (int i = 0; i < length; i++) {
					org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding = methods[i];
					if (!shouldBeRemoved(methodBinding)) { 
						newMethods[removeSyntheticsCounter++] = this.resolver.getMethodBinding(methodBinding);
					}
				}
				if (removeSyntheticsCounter != length) {
					System.arraycopy(newMethods, 0, (newMethods = new IMethodBinding[removeSyntheticsCounter]), 0, removeSyntheticsCounter);
				}
				return newMethods;
			}
		} catch (RuntimeException e) {
			/* in case a method cannot be resolvable due to missing jars on the classpath
			 * see https://bugs.eclipse.org/bugs/show_bug.cgi?id=57871
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=63550
			 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=64299
			 */
		}
		return NO_METHOD_BINDINGS;
	}

	private boolean shouldBeRemoved(org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding) {
		return methodBinding.isDefaultAbstract() || methodBinding.isSynthetic() || (methodBinding.isConstructor() && isInterface());
	}
	
	/*
	 * @see ITypeBinding#isFromSource()
	 */
	public boolean isFromSource() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return !referenceBinding.isBinaryBinding();
		}
		return false;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.TYPE;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (this.binding.isClass() || this.binding.isInterface()) {
			ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
			return referenceBinding.isDeprecated();
		}
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		return false;
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			if (isLocal()) {
				StringBuffer buffer = new StringBuffer();
				
				// declaring method or type
				SourceTypeBinding sourceBinding = (SourceTypeBinding) this.binding; // per construction, a local type can only be defined in source
				ClassScope scope = sourceBinding.scope;
				ReferenceContext referenceContext;
				if (isAnonymous()) {
					ClassScope classScope = scope.enclosingClassScope();
					referenceContext = classScope.referenceContext;
				} else {
					MethodScope methodScope = scope.enclosingMethodScope();
					referenceContext = methodScope.referenceContext;
				}
				if (referenceContext instanceof AbstractMethodDeclaration) {
					org.eclipse.jdt.internal.compiler.lookup.MethodBinding internalBinding = ((AbstractMethodDeclaration) referenceContext).binding;
					IMethodBinding methodBinding = this.resolver.getMethodBinding(internalBinding);
					if (methodBinding != null) {
						buffer.append(methodBinding.getKey());
					}
				} else if (referenceContext instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
					org.eclipse.jdt.internal.compiler.lookup.TypeBinding internalBinding = ((org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) referenceContext).binding;
					ITypeBinding typeBinding = this.resolver.getTypeBinding(internalBinding);
					if (typeBinding != null) {
						buffer.append(typeBinding.getKey());
					}
				}
	
				if (isAnonymous()) {
					buffer.append('$');
					CompilationUnitScope compilationUnitScope = scope.compilationUnitScope();
					CompilationUnitDeclaration compilationUnitDeclaration = compilationUnitScope.referenceContext;
					LocalTypeBinding[] localTypeBindings = compilationUnitDeclaration.localTypes;
					for (int i = 0, max = compilationUnitDeclaration.localTypeCount; i < max; i++) {
						if (localTypeBindings[i] == sourceBinding) {
							buffer.append(i+1);
							break;
						}
					}
				} else {
					// type name
					buffer.append('/');
					buffer.append(getName());
				}
				
				this.key = buffer.toString();
			} else {
				if (this.binding.isClass() || this.binding.isInterface()) {
					StringBuffer buffer = new StringBuffer();
					char[] constantPoolName = this.binding.constantPoolName();
					if (constantPoolName != null) {
						buffer.append(constantPoolName);
					} else {
						char[] qualifiedSourceName = this.binding.qualifiedSourceName();
						if (qualifiedSourceName != null) {
							CharOperation.replace(qualifiedSourceName, '.', '$');
							buffer
								.append(getPackage().getName())
								.append('/')
								.append(qualifiedSourceName);
						} else {
							buffer
								.append(getPackage().getName())
								.append('/')
								.append(getName());
						}
					}
					this.key = buffer.toString();
				} else if (this.binding.isArrayType()) {
					if (this.getElementType() != null) {
						this.key = this.getElementType().getKey() + this.getDimensions();
					} else {
						this.key = Integer.toString(this.getDimensions());
					}
				} else {
					// this is a primitive type
					this.key = this.getName();
				}
			}
		}
		return this.key;
	}

	/**
	 * @see ITypeBinding#isNullType()
	 */
	public boolean isNullType() {
		return this.binding == BaseTypes.NullBinding;
	}

	/**
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getQualifiedName()
	 */
	public String getQualifiedName() {
		if (isAnonymous() || isLocal()) {
			return NO_NAME;
		}
		
		if (isPrimitive() || isNullType()) {
			return getName();
		}
		
		if (isArray()) {
			ITypeBinding elementType = getElementType();
			String elementTypeQualifiedName = elementType.getQualifiedName();
			if (elementTypeQualifiedName.length() != 0) {
				int dimensions = getDimensions();
				char[] brackets = new char[dimensions * 2];
				for (int i = dimensions * 2 - 1; i >= 0; i -= 2) {
					brackets[i] = ']';
					brackets[i - 1] = '[';
				}
				StringBuffer stringBuffer = new StringBuffer(elementTypeQualifiedName);
				stringBuffer.append(brackets);
				return stringBuffer.toString();
			} else {
				return NO_NAME;
			}
		}
		
		if (isTopLevel() || isMember()) {
			PackageBinding packageBinding = this.binding.getPackage();
			
			if (packageBinding == null || packageBinding.compoundName == CharOperation.NO_CHAR_CHAR) {
				return new String(this.binding.qualifiedSourceName());
			} else {
				StringBuffer stringBuffer = new StringBuffer();
				stringBuffer
					.append(this.binding.qualifiedPackageName())
					.append('.')
					.append(this.binding.qualifiedSourceName());
				return stringBuffer.toString();
			}
		}
		return NO_NAME;
	}
	
	/* (non-Javadoc)
	 * @see ITypeBinding#isEnum()
	 */
	public boolean isEnum() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isAnnotation()
	 */
	public boolean isAnnotation() {
		// TODO (olivier) missing implementation of J2SE 1.5 language feature
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeParameters()
	 */
	public ITypeBinding[] getTypeParameters() {
		TypeVariableBinding[] typeVariableBindings = this.binding.typeVariables();
		if (typeVariableBindings != null) {
			int typeVariableBindingsLength = typeVariableBindings.length;
			if (typeVariableBindingsLength != 0) {
				ITypeBinding[] typeParameters = new ITypeBinding[typeVariableBindingsLength];
				for (int i = 0; i < typeVariableBindingsLength; i++) {
					typeParameters[i] = this.resolver.getTypeBinding(typeVariableBindings[i]);
				}
				return typeParameters;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeBounds()
	 */
	public ITypeBinding[] getTypeBounds() {
		if (this.binding instanceof TypeVariableBinding) {
			TypeVariableBinding typeVariableBinding = (TypeVariableBinding) this.binding;
			int boundsNumber = 0;
			ReferenceBinding superclass = typeVariableBinding.superclass();
			if (superclass != null) {
				boundsNumber++;
			}
			ReferenceBinding[] superinterfaces = typeVariableBinding.superInterfaces();
			int superinterfacesLength = 0;
			if (superinterfaces != null) {
				superinterfacesLength = superinterfaces.length;
				boundsNumber += superinterfacesLength;
			}
			if (boundsNumber != 0) {
				ITypeBinding[] typeBounds = new ITypeBinding[boundsNumber];
				int boundsIndex = 0;
				if (superclass != null) {
					typeBounds[boundsIndex++] = this.resolver.getTypeBinding(superclass);
				}
				if (superinterfaces != null) {
					for (int i = 0; i < superinterfacesLength; i++, boundsIndex++) {
						typeBounds[boundsIndex] = this.resolver.getTypeBinding(superinterfaces[i]);
					}
				}
				return typeBounds;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isParameterizedType()
	 */
	public boolean isParameterizedType() {
		return this.binding.isParameterizedType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getTypeArguments()
	 */
	public ITypeBinding[] getTypeArguments() {
		if (this.binding.isParameterizedType()) {
			ParameterizedTypeBinding parameterizedTypeBinding = (ParameterizedTypeBinding) this.binding;
			final org.eclipse.jdt.internal.compiler.lookup.TypeBinding[] arguments = parameterizedTypeBinding.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				ITypeBinding[] typeArguments = new ITypeBinding[argumentsLength];
				for (int i = 0; i < argumentsLength; i++) {
					typeArguments[i] = this.resolver.getTypeBinding(arguments[i]);
				}
				return typeArguments;
			}
		}
		return NO_TYPE_BINDINGS;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getErasure()
	 */
	public ITypeBinding getErasure() {
		return this.resolver.getTypeBinding(this.binding.erasure());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isRawType()
	 */
	public boolean isRawType() {
		return this.binding.isRawType();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isWildcardType()
	 */
	public boolean isWildcardType() {
		return this.binding.isWildcard();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#getBound()
	 */
	public ITypeBinding getBound() {
		if (this.binding.isWildcard()) {
			WildcardBinding wildcardBinding = (WildcardBinding) this.binding;
			if (wildcardBinding.bound != null) {
				return this.resolver.getTypeBinding(wildcardBinding.bound);
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.core.dom.ITypeBinding#isUpperbound()
	 */
	public boolean isUpperbound() {
		return this.binding.isWildcard() && ((WildcardBinding) this.binding).kind == Wildcard.SUPER;
	}
	
	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}

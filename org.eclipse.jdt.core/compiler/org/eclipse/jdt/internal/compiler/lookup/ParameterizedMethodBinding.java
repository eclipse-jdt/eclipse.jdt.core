/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.ast.Wildcard;

/**
 * Binding denoting a method after type parameter substitutions got performed.
 * On parameterized type bindings, all methods got substituted, regardless whether
 * their signature did involve generics or not, so as to get the proper declaringClass for
 * these methods.
 */
public class ParameterizedMethodBinding extends MethodBinding {

	protected MethodBinding originalMethod;

	/**
	 * Create method of parameterized type, substituting original parameters/exception/return type with type arguments.
	 */
	public ParameterizedMethodBinding(final ParameterizedTypeBinding parameterizedDeclaringClass, MethodBinding originalMethod) {

		super(
				originalMethod.modifiers,
				originalMethod.selector,
				 originalMethod.returnType,
				originalMethod.parameters,
				originalMethod.thrownExceptions,
				parameterizedDeclaringClass);
		this.originalMethod = originalMethod;
		this.tagBits = originalMethod.tagBits;
		
		final TypeVariableBinding[] originalVariables = originalMethod.typeVariables;
		Substitution substitution = null;
		final int length = originalVariables.length;
		final boolean isStatic = originalMethod.isStatic();
		if (length == 0) {
			this.typeVariables = NoTypeVariables;
			if (!isStatic) substitution = parameterizedDeclaringClass;
		} else {
			// at least fix up the declaringElement binding + bound substitution if non static
			final TypeVariableBinding[] substitutedVariables = new TypeVariableBinding[length];
			for (int i = 0; i < length; i++) { // copy original type variable to relocate
				TypeVariableBinding originalVariable = originalVariables[i];
				substitutedVariables[i] = new TypeVariableBinding(originalVariable.sourceName, this, originalVariable.rank);
			}
			this.typeVariables = substitutedVariables;
			
			// need to substitute old var refs with new ones (double substitution: declaringClass + new type variables)
			substitution = new Substitution() {
				public LookupEnvironment environment() { 
					return parameterizedDeclaringClass.environment; 
				}
				public boolean isRawSubstitution() {
					return !isStatic && parameterizedDeclaringClass.isRawSubstitution();
				}
				public TypeBinding substitute(TypeVariableBinding typeVariable) {
			        // check this variable can be substituted given copied variables
			        if (typeVariable.rank < length && originalVariables[typeVariable.rank] == typeVariable) {
						return substitutedVariables[typeVariable.rank];
			        }
			        if (!isStatic)
						return parameterizedDeclaringClass.substitute(typeVariable);
			        return typeVariable;
				}
			};
		
			// initialize new variable bounds
			for (int i = 0; i < length; i++) {
				TypeVariableBinding originalVariable = originalVariables[i];
				TypeVariableBinding substitutedVariable = substitutedVariables[i];
				TypeBinding substitutedSuperclass = Scope.substitute(substitution, originalVariable.superclass);
				ReferenceBinding[] substitutedInterfaces = Scope.substitute(substitution, originalVariable.superInterfaces);
				if (originalVariable.firstBound != null) {
					substitutedVariable.firstBound = originalVariable.firstBound == originalVariable.superclass
						? substitutedSuperclass // could be array type or interface
						: substitutedInterfaces[0];
				}				
				switch (substitutedSuperclass.kind()) {
					case Binding.ARRAY_TYPE :
						substitutedVariable.superclass = parameterizedDeclaringClass.environment.getType(JAVA_LANG_OBJECT);
						substitutedVariable.superInterfaces = substitutedInterfaces;
						break;
					default:
						if (substitutedSuperclass.isInterface()) {
							substitutedVariable.superclass = parameterizedDeclaringClass.environment.getType(JAVA_LANG_OBJECT);
							int interfaceCount = substitutedInterfaces.length;
							System.arraycopy(substitutedInterfaces, 0, substitutedInterfaces = new ReferenceBinding[interfaceCount+1], 1, interfaceCount);
							substitutedInterfaces[0] = (ReferenceBinding) substitutedSuperclass;
							substitutedVariable.superInterfaces = substitutedInterfaces;
						} else {
							substitutedVariable.superclass = (ReferenceBinding) substitutedSuperclass; // typeVar was extending other typeVar which got substituted with interface
							substitutedVariable.superInterfaces = substitutedInterfaces;
						}
				}
			}
		}
		if (substitution != null) {
			this.returnType = Scope.substitute(substitution, this.returnType);
			this.parameters = Scope.substitute(substitution, this.parameters);
			this.thrownExceptions = Scope.substitute(substitution, this.thrownExceptions);
		}
	}

	public ParameterizedMethodBinding() {
		// no init
	}

	/**
	 * The type of x.getClass() is substituted from 'Class<? extends Object>' into: 'Class<? extends raw(X)>
	 */
	public static ParameterizedMethodBinding instantiateGetClass(TypeBinding receiverType, MethodBinding originalMethod, Scope scope) {
		ParameterizedMethodBinding method = new ParameterizedMethodBinding();
		method.modifiers = originalMethod.modifiers;
		method.selector = originalMethod.selector;
		method.declaringClass = originalMethod.declaringClass;
		method.typeVariables = NoTypeVariables;
		method.originalMethod = originalMethod;
		method.parameters = originalMethod.parameters;
		method.thrownExceptions = originalMethod.thrownExceptions;
		ReferenceBinding genericClassType = scope.getJavaLangClass();
		LookupEnvironment environment = scope.environment();
		TypeBinding rawType = environment.convertToRawType(receiverType);
		method.returnType = environment.createParameterizedType(
			genericClassType,
			new TypeBinding[] {  environment.createWildcard(genericClassType, 0, rawType, null /*no extra bound*/, Wildcard.EXTENDS) },
			null);
		return method;
	}

	/**
	 * Returns true if some parameters got substituted.
	 */
	public boolean hasSubstitutedParameters() {
		return this.parameters != originalMethod.parameters;
	}

	/**
	 * Returns true if the return type got substituted.
	 */
	public boolean hasSubstitutedReturnType() {
		return this.returnType != originalMethod.returnType;
	}

	/**
	 * Returns the original method (as opposed to parameterized instances)
	 */
	public MethodBinding original() {
		return this.originalMethod.original();
	}
}

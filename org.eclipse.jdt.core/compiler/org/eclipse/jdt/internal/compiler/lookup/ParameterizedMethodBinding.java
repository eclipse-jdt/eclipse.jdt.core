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
	public ParameterizedMethodBinding(ParameterizedTypeBinding parameterizedDeclaringClass, MethodBinding originalMethod, boolean isStatic) {

		super(
				originalMethod.modifiers,
				originalMethod.selector,
				isStatic // no substitution if original was static
						? originalMethod.returnType
						: parameterizedDeclaringClass.substitute(originalMethod.returnType),
				isStatic // no substitution if original was static
					? originalMethod.parameters
					: Scope.substitute(parameterizedDeclaringClass, originalMethod.parameters),
				isStatic // no substitution if original was static
					? originalMethod.thrownExceptions
					: Scope.substitute(parameterizedDeclaringClass, originalMethod.thrownExceptions),
				parameterizedDeclaringClass);
		this.originalMethod = originalMethod;
		this.typeVariables = originalMethod.typeVariables;
	}

	public ParameterizedMethodBinding() {
		// no init
	}

	/**
	 * The type of x.getClass() is substituted from 'Class<? extends Object>' into: 'Class<? extends |X|> where |X| is X's erasure.
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
		method.returnType = scope.createParameterizedType(
			genericClassType,
			new TypeBinding[] {  scope.environment().createWildcard(genericClassType, 0, receiverType.erasure(), Wildcard.EXTENDS) },
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
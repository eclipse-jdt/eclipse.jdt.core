/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * Builders adapted from org.eclipse.jdt.internal.codeassist.CompletionEngine in order to work with IBindings
 */
class DOMCompletionEngineBuilder {

	private static final String EXTENDS = "extends"; //$NON-NLS-1$
	private static final String THROWS = "throws"; //$NON-NLS-1$
	private static final String SUPER = "super"; //$NON-NLS-1$

	static void createMethod(IMethodBinding methodBinding, StringBuilder completion) {

		// Modifiers
		// flush uninteresting modifiers
		int insertedModifiers = methodBinding.getModifiers()
				& ~(ClassFileConstants.AccNative | ClassFileConstants.AccAbstract);
		if (insertedModifiers != ClassFileConstants.AccDefault) {
			ASTNode.printModifiers(insertedModifiers, completion);
		}

		// Type parameters

		ITypeBinding[] typeVariableBindings = methodBinding.getTypeParameters();
		if (typeVariableBindings != null && typeVariableBindings.length != 0) {
			completion.append('<');
			for (int i = 0; i < typeVariableBindings.length; i++) {
				if (i != 0) {
					completion.append(',');
					completion.append(' ');
				}
				createTypeVariable(typeVariableBindings[i], completion);
			}
			completion.append('>');
			completion.append(' ');
		}

		// Return type
		createType(methodBinding.getReturnType(), completion);
		completion.append(' ');

		// Selector (name)
		completion.append(methodBinding.getName());

		completion.append('(');

		// Parameters
		ITypeBinding[] parameterTypes = methodBinding.getParameterTypes();
		String[] parameterNames = methodBinding.getParameterNames();
		int length = parameterTypes.length;
		for (int i = 0; i < length; i++) {
			if (i != 0) {
				completion.append(',');
				completion.append(' ');
			}
			createType(parameterTypes[i], completion);
			completion.append(' ');
			if (parameterNames != null) {
				completion.append(parameterNames[i]);
			} else {
				completion.append('%');
			}
		}

		completion.append(')');

		// Exceptions
		ITypeBinding[] exceptions = methodBinding.getExceptionTypes();

		if (exceptions != null && exceptions.length > 0) {
			completion.append(' ');
			completion.append(THROWS);
			completion.append(' ');
			for (int i = 0; i < exceptions.length; i++) {
				if (i != 0) {
					completion.append(' ');
					completion.append(',');
				}
				createType(exceptions[i], completion);
			}
		}
	}

	static void createType(ITypeBinding type, StringBuilder completion) {
		if (type.isWildcardType() || type.isIntersectionType()) {
			completion.append('?');
			if (type.isUpperbound()) {
				completion.append(' ');
				completion.append(EXTENDS);
				completion.append(' ');
				createType(type.getBound(), completion);
				if (type.getTypeBounds() != null) {
					for (ITypeBinding bound : type.getTypeBounds()) {
						completion.append(' ');
						completion.append('&');
						completion.append(' ');
						createType(bound, completion);
					}
				}
			} else {
				completion.append(' ');
				completion.append(SUPER);
				completion.append(' ');
				createType(type.getBound(), completion);
			}
		} else if (type.isArray()) {
			createType(type.getElementType(), completion);
			int dim = type.getDimensions();
			for (int i = 0; i < dim; i++) {
				completion.append("[]"); //$NON-NLS-1$
			}
		} else if (type.isParameterizedType()) {
			if (type.isMember()) {
				createType(type.getDeclaringClass(), completion);
				completion.append('.');
				completion.append(type.getName());
			} else {
				completion.append(type.getQualifiedName());
			}
			ITypeBinding[] typeArguments = type.getTypeArguments();
			if (typeArguments != null) {
				completion.append('<');
				for (int i = 0, length = typeArguments.length; i < length; i++) {
					if (i != 0)
						completion.append(',');
					createType(typeArguments[i], completion);
				}
				completion.append('>');
			}
		} else {
			completion.append(type.getQualifiedName());
		}
	}

	static void createTypeVariable(ITypeBinding typeVariable, StringBuilder completion) {
		completion.append(typeVariable.getName());

		if (typeVariable.getSuperclass() != null
				&& typeVariable.getTypeBounds()[0].getKey().equals(typeVariable.getSuperclass().getKey())) {
			completion.append(' ');
			completion.append(EXTENDS);
			completion.append(' ');
			createType(typeVariable.getSuperclass(), completion);
		}
		if (typeVariable.getInterfaces() != null) {
			if (!typeVariable.getTypeBounds()[0].getKey().equals(typeVariable.getSuperclass().getKey())) {
				completion.append(' ');
				completion.append(EXTENDS);
				completion.append(' ');
			}
			for (int i = 0, length = typeVariable.getInterfaces().length; i < length; i++) {
				if (i > 0 || typeVariable.getTypeBounds()[0].getKey().equals(typeVariable.getSuperclass().getKey())) {
					completion.append(' ');
					completion.append(EXTENDS);
					completion.append(' ');
				}
				createType(typeVariable.getInterfaces()[i], completion);
			}
		}
	}

	static char[] getSignature(IMethodBinding methodBinding) {
		String fullKey = methodBinding.getKey().replace('/', '.');
		String justReturn = fullKey.substring(fullKey.indexOf('('));
		return justReturn.toCharArray();
	}

	static char[] getSignature(ITypeBinding typeBinding) {
		return typeBinding.getKey().replace('/', '.').toCharArray();
	}

}

/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;

public class SignatureUtils {

	public static String createSignature(Type type, SearchEngine searchEngine, IJavaSearchScope scope, IProgressMonitor monitor) {
		ITypeBinding binding = type.resolveBinding();
		if (binding != null && !binding.isRecovered()) {
			return getSignature(binding);
		}
		String simpleName = simpleName(type);
		IType resolvedType = binding.getJavaElement() instanceof IType element ? element : null;
		if (resolvedType == null || resolvedType.exists()) {
			List<IType> types = new ArrayList<>();
			try {
				searchEngine.searchAllTypeNames(null, SearchPattern.R_PREFIX_MATCH, simpleName.toCharArray(), SearchPattern.R_EXACT_MATCH, IJavaSearchConstants.TYPE, scope, new TypeNameMatchRequestor() {
					@Override
					public void acceptTypeNameMatch(TypeNameMatch match) {
						types.add(match.getType());
					}
				}, IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, monitor);
			} catch (JavaModelException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			if (types.size() == 1) {
				resolvedType = types.get(0);
			}
		}
		StringBuilder res = new StringBuilder();
		if (resolvedType != null && resolvedType.exists()) {
			res.append(Signature.C_RESOLVED);
			res.append(resolvedType.getFullyQualifiedName());
		} else {
			res.append(Signature.C_UNRESOLVED);
			res.append(simpleName);
		}
		if (type instanceof ParameterizedType parameterized) {
			res.append(Signature.C_GENERIC_START);
			((List<Type>)parameterized.typeArguments()).stream()
				.map(param -> createSignature(param, searchEngine, scope, monitor))
				.forEach(res::append);
			res.append(Signature.C_GENERIC_END);
		}
		res.append(';');
		return res.toString();
	}

	private static String simpleName(Type type) {
		if (type instanceof SimpleType simple) {
			return simple.getName().toString();
		}
		if (type instanceof ParameterizedType parameterized) {
			return simpleName(parameterized.getType());
		}
		return type.toString();
	}

	/**
	 * Returns the signature of the given model type.
	 *
	 * @param type the model type to get the signature of
	 * @return the signature of the given model type
	 */
	public static String createSignature(IType type) {
		return getSignatureForTypeKey(type.getKey());
	}

	/**
	 * Returns the signature of the given type binding as a character array.
	 *
	 * @param typeBinding the type binding to get the signature of
	 * @return the signature of the given type binding as a character array
	 */
	public static char[] getSignatureChar(ITypeBinding typeBinding) {
		return SignatureUtils.getSignature(typeBinding).toCharArray();
	}

	/**
	 * Returns the signature of the given type binding.
	 *
	 * @param typeBinding the type binding to get the signature of
	 * @return the signature of the given type binding
	 */
	public static String getSignature(ITypeBinding typeBinding) {
		if (typeBinding.isArray()) {
			return Signature.createArraySignature(getSignature(typeBinding.getComponentType()), 1);
		}
		if (typeBinding.isWildcardType()) {
			// TODO if typeBinding.getBounds(): C_EXTENDS, C_SUPER
			return Character.toString(Signature.C_STAR);
		}
		ITypeBinding[] typeBounds = typeBinding.getTypeBounds();
		if (typeBinding.isTypeVariable() || typeBinding.isWildcardType()) {
			return Signature.C_TYPE_VARIABLE + typeBinding.getName() + Signature.C_NAME_END;
		}
		if (typeBinding.isIntersectionType()) {
			return Signature.createIntersectionTypeSignature(Stream.of(typeBounds).map(SignatureUtils::getSignature).toArray(String[]::new));
		}
		if (typeBinding.isParameterizedType()) {
			StringBuilder res = new StringBuilder(Signature.createTypeSignature(typeBinding.getErasure().getQualifiedName(), true));
			res.deleteCharAt(res.length() - 1);
			return res.toString()
				+ Signature.C_GENERIC_START
				+ Stream.of(typeBinding.getTypeArguments()).map(SignatureUtils::getSignature).collect(Collectors.joining())
				+ Signature.C_GENERIC_END
				+ Signature.C_NAME_END;
		}
		if (typeBinding.isGenericType()) {
			StringBuilder res = new StringBuilder(Signature.createTypeSignature(typeBinding.getErasure().getQualifiedName(), true));
			res.deleteCharAt(res.length() - 1);
			return res.toString()
				+ Signature.C_GENERIC_START
				+ Stream.of(typeBinding.getTypeParameters()).map(SignatureUtils::getSignature).collect(Collectors.joining())
				+ Signature.C_GENERIC_END
				+ Signature.C_NAME_END;
		}
		return SignatureUtils.getSignatureForTypeKey(typeBinding.getKey());
	}

	/**
	 * Returns the signature of the given type key.
	 *
	 * @param key the type key to get the signature of
	 * @return the signature of the given type key
	 */
	public static String getSignatureForTypeKey(String key) {
		return key.replace('/', '.').replaceFirst("(?<=\\.|L)[_$A-Za-z][_$A-Za-z0-9]*~", "");
	}

	public static String getSignature(IMethodBinding methodBinding) {
		return getSignatureForMethodKey(methodBinding.getKey());
	}

	/**
	 * Returns the signature of the given method binding as a character array.
	 *
	 * @param methodBinding the method binding to get the signature of
	 * @return the signature of the given method binding as a character array
	 */
	public static char[] getSignatureChar(IMethodBinding methodBinding) {
		return getSignatureForMethodKey(methodBinding.getKey()).toCharArray();
	}

	public static char[] getSignatureChar(IMethod method) {
		return getSignatureForMethodKey(method.getKey()).toCharArray();
	}

	/**
	 * Returns the signature for the given method key.
	 *
	 * @param key the method key to get the signature of
	 * @return the signature for the given method key
	 */
	public static String getSignatureForMethodKey(String key) {
		String fullKey = key
				.replace('/', '.')
				.replace("<+Ljava.lang.Object;>", "<*>");
		String removeName = fullKey.substring(fullKey.indexOf('('));
		int firstException = removeName.indexOf('|');
		String exceptionRemoved;
		if (firstException > 0) {
			exceptionRemoved =  removeName.substring(0, firstException);
		} else {
			exceptionRemoved = removeName;
		}
		// strip out the weird information the keys need to differentiate themselves
		return exceptionRemoved.replaceAll("!L[^;]+;\\{[0-9]+\\}(\\+L[^;]+;)[0-9]+;", "$1");
	}

	public static String stripTypeArgumentsFromKey(String key) {
		if (key.indexOf(">;") < 0) {
			return key;
		}
		return key.substring(0, key.lastIndexOf("<")) + "<>;";
	}

	/**
	 * Returns true if the given signature is a primitive number, and false
	 * otherwise.
	 *
	 * @param sig the signature to check
	 * @return true if the given signature is a primitive number, and false
	 *         otherwise
	 */
	public static boolean isNumeric(String sig) {
		return sig.equals(Signature.SIG_BYTE) || sig.equals(Signature.SIG_CHAR) || sig.equals(Signature.SIG_DOUBLE)
				|| sig.equals(Signature.SIG_FLOAT) || sig.equals(Signature.SIG_INT) || sig.equals(Signature.SIG_LONG)
				|| sig.equals(Signature.SIG_SHORT);
	}

}

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
package org.eclipse.jdt.internal.core.search.matching;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.core.util.KeyToSignature;
import org.eclipse.jdt.internal.javac.dom.JavacTypeBinding;

public class TypeArgumentMatchingUtility {

	public static boolean validateSingleTypeArgMatches(boolean requiresExactMatch, String patternSig, IBinding patternBinding, IBinding domBinding) {
		ITypeBinding domTypeBinding = domBinding instanceof ITypeBinding ? (ITypeBinding)domBinding : null;
		String domKey1 = domBinding == null ? null : domBinding.getKey();
		String domSig = null;
		if( domKey1 != null ) {
			try {
				KeyToSignature ks = new KeyToSignature(domKey1, KeyToSignature.SIGNATURE);
				ks.parse();
				domSig = ks.toString();
			} catch(RuntimeException re ) {
				// TODO ignore
			}
		}
		//String domSig = domKey1 == null ? null : domBinding instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : domBinding.getKey();
		if( requiresExactMatch ) {

			if( Objects.equals(patternSig, domSig)) {
				return true;
			}
			if( patternSig.equals("*") && isQuestionMark(domSig)) {
				return true;
			}
			if( patternSig.startsWith("Q")) {
				String patternSimpleName = null;
				try {
					patternSimpleName = Signature.getSignatureSimpleName(patternSig);
				} catch(IllegalArgumentException iae) {
					// Ignore
				}
				String bindingSimpleName = null;
				try {
					bindingSimpleName = Signature.getSignatureSimpleName(domSig);
				} catch(IllegalArgumentException iae) {
					// Ignore
				}
				if( Objects.equals(patternSimpleName, bindingSimpleName)) {
					return true;
				}
				if( patternSimpleName != null && patternSimpleName.startsWith(bindingSimpleName + "<")) {
					return true;
				}
			}
			return false;
		}

		if( patternSig.equals(("*")))
			return true;
		if( patternSig.equals(domSig)) {
			return true;
		}

		String patternKeyFromBinding = patternBinding == null ? null : patternBinding instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : patternBinding.getKey();
		List<IBinding> patternAncestors = new ArrayList<>();
		if( patternBinding instanceof ITypeBinding patternTypeBinding) {
			if( patternSig.startsWith("+") || patternSig.startsWith("-")) {
				patternAncestors = findAllSuperclassAndInterfaceBindingsForWildcard(patternTypeBinding);
			} else {
				patternAncestors = findAllSuperclassAndInterfaceBindings(patternTypeBinding);
			}
		}

		List<String> patternAncestorKeys = patternAncestors.stream().map(x -> x instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : x.getKey()).collect(Collectors.toList());
		if( patternSig.startsWith("-")) {
			if( domSig.startsWith("-") || !domSig.startsWith("+")) {
				String domKey = domBinding instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : domBinding.getKey();
				if( !patternAncestorKeys.contains(domKey)) {
					return false;
				}
			} else if( domSig.startsWith("+") && !isQuestionMark(domSig)) {
				return false;
			}
		} else if( patternSig.startsWith("+")) {
			if( domSig.startsWith("-")) {
				// There's no way ALL ancestors of dom can be a subclass of pattern unless pattern is java.lang.Object
				return false;
			} else {
				List<IBinding> domHeirarchy = findAllSuperclassAndInterfaceBindingsForWildcard(domTypeBinding);
				List<String> domHeirarchyStrings = domHeirarchy.stream().map(x -> x instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : x.getKey()).collect(Collectors.toList());
				if( patternKeyFromBinding != null ) {
					if( !resolvedPatternMatchesDom(patternSig.substring(1), patternKeyFromBinding, domTypeBinding, domHeirarchyStrings)) {
						return false;
					}
				} else {
					if( !unresolvedPatternMatchesDom(patternSig, domSig, domTypeBinding, domHeirarchyStrings)) {
						return false;
					}
				}
			}
		} else {
			// pattern is a normal defined type, ex:  Exception
			if( domSig.startsWith("-")) {
				List<IBinding> domHeirarchy = findAllSuperclassAndInterfaceBindingsForWildcard(domTypeBinding);
				List<String> domHeirarchyStrings = domHeirarchy.stream().map(x -> x instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : x.getKey()).collect(Collectors.toList());
				if( patternKeyFromBinding == null && !domHeirarchyStrings.contains(patternKeyFromBinding))
					return false;
			} else if( domSig.startsWith("+")) {
				if( domTypeBinding != null ) {
					ITypeBinding bound = domTypeBinding.getBound();
					String boundKey = bound == null ? null : bound instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : bound.getKey();
					if( !isQuestionMark(domSig) && (boundKey == null || !patternAncestorKeys.contains(boundKey))) {
						return false;
					}
				}
			} else {
				// Just two normal param types, see if they match
				if( !patternSig.equals(domSig)) {
					if( !unresolvedPatternMatchesDom(patternSig, domSig, domTypeBinding, new ArrayList<>())) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private static boolean resolvedPatternMatchesDom(String patternSig, String patternKeyFromBinding, ITypeBinding domTypeBinding,
			List<String> domHeirarchyStrings) {
		String k = domTypeBinding instanceof JavacTypeBinding jctb ? jctb.getGenericTypeSignature(false) : domTypeBinding.getKey();
		if( isQuestionMark(k))
			return true;
		if( domHeirarchyStrings.contains(patternSig))
			return true;
		if( patternKeyFromBinding != null) {
			if( domHeirarchyStrings.contains(patternKeyFromBinding))
				return true;
			if( patternKeyFromBinding.startsWith("+") && domHeirarchyStrings.contains(patternKeyFromBinding.substring(1))) {
				return true;
			}
		}
		return false;
	}

	private static boolean unresolvedPatternMatchesDom(String patternSig, String domSig, ITypeBinding domTypeBinding,
			List<String> domHeirarchyStrings) {
		boolean patternSigIsUnresolved = false;
		String patternSigTrimmed = null;
		if( patternSig.startsWith("Q")) {
			patternSigTrimmed = patternSig.substring(1);
			patternSigIsUnresolved = true;
		} else if( patternSig.startsWith("+Q")) {
			patternSigTrimmed = patternSig.substring(2);
			patternSigIsUnresolved = true;
		}
		if( patternSigIsUnresolved) {
			// TODO this is insufficient
			if( domSig.endsWith("." + patternSigTrimmed))
				return true;
		}

		String patternSigWithoutPrefix = patternSig.startsWith("+") || patternSig.startsWith("-") ? patternSig.substring(1) : patternSig;
		String domSigWithoutPrefix = domSig.startsWith("+") || domSig.startsWith("-") ? domSig.substring(1) : domSig;
		String patternSig2 = patternSigWithoutPrefix.substring(1);
		if( patternSig2.equals(domSigWithoutPrefix.substring(1)))
			return true;
		if( domSig.endsWith("." + patternSig2) )
			return true;

		return false;
	}
	private static boolean isQuestionMark(String k) {
		// wut ?
		boolean isQuestionMark = "+Ljava/lang/Object;".equals(k) || "+Qjava.lang.Object;".equals(k);
		return isQuestionMark;
	}
	private static List<IBinding> findAllSuperclassAndInterfaceBindingsForWildcard(ITypeBinding binding) {
		List<IBinding> ret = new ArrayList<>();
		if( binding == null )
			return ret;
		// Sometimes we have a discovered binding which is already the bound...
		ITypeBinding param = binding.isWildcardType() ? binding.getBound() : binding;
		if( param != null ) {
			ret.add(param);
			fillAllSuperclassAndInterfaceBindings(param, ret);
		} else {
			// for pure `?`
			ret.add(binding);
		}
		return ret;
	}
	private static List<IBinding> findAllSuperclassAndInterfaceBindings(ITypeBinding binding) {
		List<IBinding> ret = new ArrayList<>();
		if( binding == null )
			return ret;
		fillAllSuperclassAndInterfaceBindings(binding, ret);
		return ret;
	}
	private static void fillAllSuperclassAndInterfaceBindings(ITypeBinding binding, List<IBinding> list) {
		if( binding == null )
			return;
		ITypeBinding[] ifaces = binding.getInterfaces();
		for( int q = 0; q < ifaces.length; q++ ) {
			ITypeBinding oneInterface = ifaces[q];
			if( oneInterface != null ) {
				list.add(oneInterface);
				fillAllSuperclassAndInterfaceBindings(oneInterface, list);
			}
		}
		ITypeBinding superClaz = binding.getSuperclass();
		if( superClaz != null ) {
			list.add(superClaz);
			fillAllSuperclassAndInterfaceBindings(superClaz, list);
		}
	}
}

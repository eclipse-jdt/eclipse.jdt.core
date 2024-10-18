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
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import java.util.function.Supplier;
import java.util.stream.Stream;
import org.eclipse.jdt.core.CompletionContext;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;

class DOMCompletionContext extends CompletionContext {
	private final int offset;
	private final char[] token;
	private final IJavaElement enclosingElement;
	private final Supplier<Stream<IBinding>> bindingsAcquirer;

	DOMCompletionContext(int offset, char[] token, IJavaElement enclosingElement,
			Supplier<Stream<IBinding>> bindingHaver) {
		this.offset = offset;
		this.enclosingElement = enclosingElement;
		this.token = token;
		this.bindingsAcquirer = bindingHaver;
	}

	@Override
	public int getOffset() {
		return this.offset;
	}

	@Override
	public char[] getToken() {
		return this.token;
	}

	@Override
	public IJavaElement getEnclosingElement() {
		return this.enclosingElement;
	}

	@Override
	public IJavaElement[] getVisibleElements(String typeSignature) {
		return this.bindingsAcquirer.get() //
			.filter(binding -> {
				if (typeSignature == null) {
					return binding instanceof IVariableBinding || binding instanceof IMethodBinding;
				}
				if (binding instanceof IVariableBinding variableBinding) {
					return castCompatable(variableBinding.getType(),
							typeSignature);
				} else if (binding instanceof IMethodBinding methodBinding) {
					return castCompatable(methodBinding.getReturnType(),
							typeSignature);
				}
				// notably, ITypeBinding is not used to complete values,
				// even, for instance, in the case that a `java.lang.Class<?>` is desired.
				return false;
			}) //
			.map(binding -> binding.getJavaElement()) //
			.filter(obj -> obj != null) // eg. ArrayList.getFirst() when working with a Java 8 project
			.toArray(IJavaElement[]::new);
	}

	@Override
	public boolean isExtended() {
		return true;
	}

	private static boolean castCompatable(ITypeBinding typeBinding, String sig2) {
		String sig1 = typeBinding.getKey().replace('/', '.');
		// NOTE: this is actually the "raw" version (no type arguments, no type params)
		String sig1Raw = new String(Signature.getTypeErasure(sig1.toCharArray()));
		// TODO: consider autoboxing numbers; upstream JDT doesn't handle this yet but it would be nice
		switch (sig1) {
			case Signature.SIG_LONG:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_INT:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_INT)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_BYTE:
				return sig2.equals(Signature.SIG_LONG)
						|| sig2.equals(Signature.SIG_INT)
						|| sig2.equals(Signature.SIG_BYTE)
						|| sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
			case Signature.SIG_DOUBLE:
			case Signature.SIG_FLOAT:
				return sig2.equals(Signature.SIG_DOUBLE)
						|| sig2.equals(Signature.SIG_FLOAT);
		}
		if (sig1.equals(sig2) || sig1Raw.equals(sig2)) {
			return true;
		}
		if (typeBinding.getSuperclass() != null && castCompatable(typeBinding.getSuperclass(), sig2)) {
			return true;
		}
		for (ITypeBinding superInterface : typeBinding.getInterfaces()) {
			if (castCompatable(superInterface, sig2)) {
				return true;
			}
		}
		return false;
	}
}
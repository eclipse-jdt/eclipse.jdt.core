/*******************************************************************************
 * Copyright (c) 2024 Red Hat Inc., and others.
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
package org.eclipse.jdt.internal.javac.dom;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.JavacBindingResolver.BindingKeyException;

import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Type.TypeVar;

/**
 * Note that this isn't API and isn't part of the IBinding tree type.
 * The sole purpose of this class is to help calculate getKey.
 */
public abstract class JavacTypeVariableBinding extends JavacTypeBinding {
	private final TypeVariableSymbol sym;
	private final JavacBindingResolver bindingResolver;

	public JavacTypeVariableBinding(TypeVar type, TypeVariableSymbol sym, JavacBindingResolver bindingResolver) {
		super(type, sym, false, bindingResolver);
		this.sym = sym;
		this.bindingResolver = bindingResolver;
	}

	@Override
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		if (this.sym.owner != null) {
			IBinding ownerBinding = this.bindingResolver.bindings.getBinding(this.sym.owner, null);
			if (ownerBinding != null) {
				builder.append(ownerBinding.getKey());
			}
		}
		builder.append(":T");
		builder.append(sym.getSimpleName());
		builder.append(";");
		return builder.toString();
	}

	@Override
	public String getQualifiedName() {
		return sym.getSimpleName().toString();
	}

	/**
	 * this is the one that's used in method params and such, not the one that's actually used as it's final resting place (RIP)
	 * @param sym
	 * @return
	 * @throws BindingKeyException 
	 */
	static String getTypeVariableKey(TypeVariableSymbol sym) throws BindingKeyException {
		StringBuilder builder = new StringBuilder();
		builder.append(sym.getSimpleName());
		builder.append(':');
		boolean prependColon = sym.getBounds().size() > 1
				|| (sym.getBounds().size() > 0 && sym.getBounds().get(0).isInterface());
		for (var bound : sym.getBounds()) {
			if (prependColon) {
				builder.append(":");
			}
			JavacTypeBinding.getKey(builder, bound, false);
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return getKey();
	}
}

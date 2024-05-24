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

import java.util.Objects;

import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;

/**
 * Note that this isn't API and isn't part of the IBinding tree type.
 * The sole purpose of this class is to help calculate getKey.
 */
public abstract class JavacTypeVariableBinding {
	private TypeVariableSymbol typeVar;

	public JavacTypeVariableBinding(TypeVariableSymbol typeVar) {
		this.typeVar = typeVar;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacTypeVariableBinding other
				&& Objects.equals(this.typeVar, other.typeVar);
	}
	@Override
	public int hashCode() {
		return Objects.hash(this.typeVar);
	}
	
	public String getKey() {
		StringBuilder builder = new StringBuilder();
		builder.append(typeVar.getSimpleName());
		builder.append(':');
		boolean prependColon = typeVar.getBounds().size() > 1
				|| (typeVar.getBounds().size() > 0 && typeVar.getBounds().get(0).isInterface());
		for (var bound : typeVar.getBounds()) {
			if (prependColon) {
				builder.append(":");
			}
			JavacTypeBinding.getKey(builder, bound, false);
		}
		return builder.toString();
	}
}

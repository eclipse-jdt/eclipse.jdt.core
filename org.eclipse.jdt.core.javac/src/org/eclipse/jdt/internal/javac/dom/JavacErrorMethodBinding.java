/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac.dom;

import java.util.Objects;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.JavacBindingResolver;
import org.eclipse.jdt.core.dom.JavacBindingResolver.BindingKeyException;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.JCNoType;
import com.sun.tools.javac.code.Type.MethodType;

public abstract class JavacErrorMethodBinding extends JavacMethodBinding {

	private Symbol originatingSymbol;

	public JavacErrorMethodBinding(Symbol originatingSymbol, MethodType methodType, JavacBindingResolver resolver) {
		super(methodType, null, resolver);
		this.originatingSymbol = originatingSymbol;
	}

	@Override
	public String getKey() {
		try {
			return getKeyImpl();
		} catch(BindingKeyException bke) {
			return null;
		}
	}
	private String getKeyImpl() throws BindingKeyException {
		StringBuilder builder = new StringBuilder();
		if (this.originatingSymbol instanceof TypeSymbol typeSymbol) {
			JavacTypeBinding.getKey(builder, resolver.getTypes().erasure(typeSymbol.type), false);
		}
		builder.append('(');
		for (Type param : this.methodType.getParameterTypes()) {
			JavacTypeBinding.getKey(builder, param, false);
		}
		builder.append(')');
		Type returnType = this.methodType.getReturnType();
		if (returnType != null && !(returnType instanceof JCNoType)) {
			JavacTypeBinding.getKey(builder, returnType, false);
		}
		return builder.toString();
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof JavacErrorMethodBinding other &&
			Objects.equals(this.methodSymbol, other.methodSymbol) &&
			Objects.equals(this.methodType, other.methodType) &&
			Objects.equals(this.originatingSymbol, other.originatingSymbol) &&
			Objects.equals(this.resolver, other.resolver);
	}

	@Override
	public boolean isRecovered() {
		return true;
	}

	@Override
	public String getName() {
		return this.originatingSymbol.getSimpleName().toString();
	}

	@Override
	public ITypeBinding getDeclaringClass() {
		if (this.originatingSymbol instanceof ClassSymbol clazz && clazz.owner instanceof ClassSymbol actualOwner) {
			this.resolver.bindings.getTypeBinding(actualOwner.type);
		}
		return null;
	}

	@Override
	public boolean isDeprecated() {
		return this.originatingSymbol.isDeprecated();
	}

	@Override
	public IMethodBinding getMethodDeclaration() {
		return this.resolver.bindings.getErrorMethodBinding(this.resolver.getTypes().erasure(methodType).asMethodType(), originatingSymbol.type.tsym);
	}

	@Override
	public IAnnotationBinding[] getAnnotations() {
		return this.originatingSymbol.getAnnotationMirrors().stream().map(ann -> this.resolver.bindings.getAnnotationBinding(ann, this)).toArray(IAnnotationBinding[]::new);
	}

}

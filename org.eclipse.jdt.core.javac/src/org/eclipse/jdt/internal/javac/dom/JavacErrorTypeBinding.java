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
package org.eclipse.jdt.internal.javac.dom;

import org.eclipse.jdt.core.dom.JavacBindingResolver;

import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;

/**
 * Represents a binding for a type that javac struggles to recover,
 * for example a class with an empty `implements` or `extends`.
 */
public class JavacErrorTypeBinding extends JavacTypeBinding {

	private TypeSymbol originatingSymbol;

	public JavacErrorTypeBinding(Type type, final TypeSymbol typeSymbol, Type[] alternatives, boolean isDeclaration,
			JavacBindingResolver resolver, TypeSymbol originatingSymbol) {
		super(type, typeSymbol, alternatives, isDeclaration, resolver);
		this.originatingSymbol = originatingSymbol;
	}

	@Override
	public String getKey() {
		return getKeyImpl();
	}

	private String getKeyImpl() {
		StringBuilder builder = new StringBuilder();
		builder.append("L");
		String packageName = "";
		if (originatingSymbol.packge() != null) {
			packageName = originatingSymbol.packge().getQualifiedName().toString();
		}
		if (!packageName.isEmpty()) {
			builder.append(packageName.replace(".", "/"));
			builder.append("/");
		}
		String typeName = originatingSymbol.getQualifiedName().toString();
		if (!packageName.isEmpty()) {
			typeName = typeName.substring(packageName.length() + 1);
		}
		if (typeName.indexOf(".") < 0
				&& !((ClassSymbol) originatingSymbol).sourcefile.getName().endsWith(typeName + ".java")) {
			String fileName = ((ClassSymbol) originatingSymbol).sourcefile.toUri().getPath();
			int lastSlash = fileName.lastIndexOf('/');
			if (lastSlash >= 0) {
				fileName = fileName.substring(lastSlash + 1);
			}
			int lastDot = fileName.lastIndexOf('.');
			fileName = fileName.substring(0, lastDot);
			// file~ prefix is used only for top-level and top-level not matching file name
			if (!fileName.equals(typeName)) {
				builder.append(fileName);
				builder.append('~');
			}
		}
		builder.append(typeName.replace(".", "$"));
		builder.append(";");
		return builder.toString();
	}

}

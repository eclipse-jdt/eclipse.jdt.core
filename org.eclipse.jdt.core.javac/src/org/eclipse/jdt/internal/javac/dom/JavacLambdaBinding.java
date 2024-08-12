/*******************************************************************************
 * Copyright (c) 2024, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac.dom;

import org.eclipse.jdt.core.dom.Modifier;

public class JavacLambdaBinding extends JavacMethodBinding {

	public JavacLambdaBinding(JavacMethodBinding methodBinding) {
		super(methodBinding.methodType, methodBinding.methodSymbol, methodBinding.parentType, methodBinding.resolver);
	}

	@Override
	public int getModifiers() {
		return super.getModifiers() & ~Modifier.ABSTRACT;
	}

}

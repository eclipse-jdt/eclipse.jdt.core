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
package org.eclipse.jdt.core.dom;

public class GenericRecoveredTypeBinding extends RecoveredTypeBinding {

	private ITypeBinding from;

	public GenericRecoveredTypeBinding(BindingResolver resolver, Type type, ITypeBinding from) {
		super(resolver, type);
		this.from = from;
	}

	@Override
	public boolean isParameterizedType() {
		return false;
	}

	@Override
	public boolean isGenericType() {
		return super.isParameterizedType();
	}

	@Override
	public ITypeBinding[] getTypeParameters() {
		return TypeBinding.NO_TYPE_BINDINGS;
	}

	@Override
	public ITypeBinding[] getTypeArguments() {
		return super.getTypeParameters();
	}

	@Override
	public IPackageBinding getPackage() {
		return this.from.getPackage();
	}

}

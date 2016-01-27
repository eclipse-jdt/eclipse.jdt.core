/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd;

import org.eclipse.jdt.internal.core.nd.field.StructDef.DeletionSemantics;

/**
 * @since 3.12
 */
public abstract class AbstractTypeFactory<T> implements ITypeFactory<T> {
	@Override
	public void destructFields(Nd dom, long address) {}

	@Override
	public void destruct(Nd dom, long address) {}

	@Override
	public boolean hasDestructor() {
		return false;
	}

	@Override
	public boolean isReadyForDeletion(Nd dom, long address) {
		return false;
	}

	@Override
	public DeletionSemantics getDeletionSemantics() {
		return DeletionSemantics.EXPLICIT;
	}
}

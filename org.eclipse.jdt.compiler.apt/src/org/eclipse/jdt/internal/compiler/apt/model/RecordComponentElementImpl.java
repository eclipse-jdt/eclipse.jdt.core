/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.RecordComponentElement;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;

public class RecordComponentElementImpl extends VariableElementImpl implements RecordComponentElement {

	protected RecordComponentElementImpl(BaseProcessingEnvImpl env, FieldBinding binding) {
		super(env, binding);
	}

	@Override
	public ElementKind getKind() {
		return ElementKind.RECORD_COMPONENT;
	}

	@Override
	public ExecutableElement getAccessor() {
		// TODO: Looks like no direct way of accessing the synthetic directly from the field binding.
		return null;
	}
}

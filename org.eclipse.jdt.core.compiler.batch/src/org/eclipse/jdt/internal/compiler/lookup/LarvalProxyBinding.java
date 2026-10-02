/*******************************************************************************
* Copyright (c) 2026 Advantest Europe GmbH and others.
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
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;

/**
 * An abstraction that serves as a proxy for fields being read in early construction context.
 * As `this` is still uninitialized, the VM would reject getfield attempts. So we put up a local
 * to serve as the `proxy` for the field and once the containing object evolves out of it larval
 * stage, carry over the value to the field.
 *
 * A field that is ONLY written to in a prologue would not need a proxy in that constructor.
 */
public class LarvalProxyBinding extends LocalVariableBinding {

	private FieldBinding larvalField;

	public LarvalProxyBinding(FieldBinding field) {
		super(CharOperation.concat(field.name, "'".toCharArray()),  //$NON-NLS-1$
				field.type,
				field.modifiers & (ClassFileConstants.AccFinal | ExtraCompilerModifiers.AccBlankFinal),
				false);
		this.setConstant(Constant.NotAConstant);    // for now, field is not resolved yet, we will update status upon look up.
        this.useFlag = LocalVariableBinding.UNUSED; // see VCAOT.testPrematureProxy
		this.larvalField = field;
		this.id = -1; // analysis id will be assigned at prologue analysis time.
	}

	public FieldBinding getLarvalField() {
		return this.larvalField;
	}

	@Override
	public boolean isFieldProxy() {
		return true;
	}
}

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
* Contributors:
*     Srikanth Sankaran - initial implementation
*******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class LarvalProxyBinding extends LocalVariableBinding {
	private FieldBinding field;
	public LarvalProxyBinding(FieldBinding field) {
		super(CharOperation.concat(field.name, "'".toCharArray()),  //$NON-NLS-1$
				field.type,
				field.modifiers & (ClassFileConstants.AccFinal | ExtraCompilerModifiers.AccBlankFinal),
				false);
		this.field = field;
	}
	public FieldBinding getShadowedBinding() {
		return this.field;
	}
}

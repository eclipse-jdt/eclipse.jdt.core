/*******************************************************************************
 * Copyright (c) 2021 IBM Corporation and others.
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
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Sole purpose of {@link FakeDefaultLiteral} is to appear
 * in case 'default' of switch patterns (JEP 406 at the time
 * of writing this comment)
 *
 */
public class FakeDefaultLiteral extends MagicLiteral {

	static final char[] source = {'d' , 'e' , 'f' , 'a', 'u', 'l','t'};

	public FakeDefaultLiteral(int s , int e) {

		super(s,e);
	}

	@Override
	public void computeConstant() {

		this.constant = Constant.NotAConstant;
	}

	@Override
	public TypeBinding literalType(BlockScope scope) {
		// TODO Change this while implementing flow analysis
		return TypeBinding.VOID;
	}

	@Override
	public char[] source() {
		return source;
	}
}

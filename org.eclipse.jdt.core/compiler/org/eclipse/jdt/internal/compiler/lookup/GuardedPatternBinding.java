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
package org.eclipse.jdt.internal.compiler.lookup;

public class GuardedPatternBinding  extends PatternBinding {

	public PatternBinding pattern;

	public GuardedPatternBinding(PatternBinding pattern) {
		this.pattern = pattern;
	}

	@Override
	public TypeBinding getType() {
		return this.pattern.getType();
	}

	@Override
	public char[] readableName() {
		return this.pattern.readableName();
	}
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("<GuardedPattern>"); //$NON-NLS-1$
		buffer.append(this.readableName());
		buffer.append("&& condition"); //$NON-NLS-1$ //TODO: Add more meaningful info
		return buffer.toString();
	}

}
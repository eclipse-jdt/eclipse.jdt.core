/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.processors.notaprocessor;

/**
 * A class that intentionally does NOT implement
 * {@link javax.annotation.processing.Processor}. Its constructor records itself
 * via a {@link System} property so that tests can detect whether it was
 * instantiated.
 *
 * <p>Used by security regression tests that verify arbitrary classes named on
 * the {@code -processor} command-line option are rejected <em>before</em> their
 * constructors are called.
 */
public class NotAProcessor {

	/** System-property key set by the constructor. Tests assert this is absent. */
	public static final String CONSTRUCTOR_CALLED_PROPERTY =
			"org.eclipse.jdt.compiler.apt.tests.NotAProcessor.constructorCalled"; //$NON-NLS-1$

	public NotAProcessor() {
		// Side-effect: mark that this constructor was invoked.
		// The security fix must prevent this line from ever being reached.
		System.setProperty(CONSTRUCTOR_CALLED_PROPERTY, "true"); //$NON-NLS-1$
	}
}

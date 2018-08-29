/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.dom;

public interface DefaultMarkedNodeLabelProviderOptions {
	/**
	 * No extra information about marked nodes will be display.
	 */
	public static final int NO_OPTIONS = 0;

	/**
	 * All extra information about marked nodes will be display.
	 */
	public static final int ALL_OPTIONS = ~NO_OPTIONS;

	/**
	 * Marked nodes type will be display.
	 */
	public static final int NODE_TYPE = 1;

	/**
	 * Marked nodes position will be display.
	 */
	public static final int NODE_POSITION = 2;

	/**
	 * Marked nodes extended position will be display.
	 */
	public static final int NODE_EXTENDED_POSITION = 4;

	/**
	 * Marked nodes flags will be display.
	 */
	public static final int NODE_FLAGS = 8;

	/**
	 * Marked nodes binding kind will be display.
	 */
	public static final int BINDING_KIND = 16;

	/**
	 * Marked nodes binding key will be display.
	 */
	public static final int BINDING_KEY = 32;

	/**
	 * Marked nodes binding flags will be display.
	 */
	public static final int BINDING_FLAGS = 64;

	/**
	 * All extra nodes information about marked nodes will be display.
	 */
	public static final int NODE_OPTIONS = NODE_TYPE | NODE_POSITION | NODE_EXTENDED_POSITION | NODE_FLAGS;

	/**
	 * All extra bindings information about marked nodes will be display.
	 */
	public static final int BINDING_OPTIONS = BINDING_KIND | BINDING_KEY | BINDING_FLAGS;
}

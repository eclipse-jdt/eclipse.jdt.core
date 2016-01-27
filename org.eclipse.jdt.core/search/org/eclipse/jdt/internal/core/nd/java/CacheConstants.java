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
package org.eclipse.jdt.internal.core.nd.java;

/**
 * Holds constants for accessing the PDOM cache
 * @since 3.12
 */
public class CacheConstants {
	public final static int CACHE_MEMBERS= 0;
	public final static int CACHE_BASES= 1;
	public final static int CACHE_INSTANCES= 2;
	public final static int CACHE_INSTANCE_SCOPE= 3;

	private CacheConstants() {};
}

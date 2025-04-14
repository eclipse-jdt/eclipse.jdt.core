/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Red Hat Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

/**
 * Utility methods for manipulating binding keys.
 */
public class KeyUtils {

	public static final String OBJECT_KEY = "Ljava/lang/Object;";

	/**
	 * Returns the given method key with the owner type and return type removed.
	 *
	 * @param methodKey the method key to remove the owner type and return type from
	 * @return the given method key with the owner type and return type removed
	 */
	public static final String getMethodKeyWithOwnerTypeAndReturnTypeRemoved(String methodKey) {
		return methodKey.substring(methodKey.indexOf('.'), methodKey.lastIndexOf(')') + 1);
	}

}

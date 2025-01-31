/*******************************************************************************
 * Copyright (c) 2021 Gayan Perera and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gayan Perera - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.index;

/**
 * The index qualifier which is used for represent MetaIndex qualifications which contains a category and a search key.
 */
public record IndexQualifier(String category,String key) {
	public static IndexQualifier qualifier(String category, String key) {
		return new IndexQualifier(category, key);
	}
}
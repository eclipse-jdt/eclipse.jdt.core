/*******************************************************************************
 * Copyright (c) 2021 jkubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Gunnar Wagenknecht, jkubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import java.util.Arrays;

/**
 * Wrapper around char arrays that can be used as a key in a Map or Set.
 * <p>
 * The {@link #hashCode()} and {@link #equals(Object)} method will work with the underlying array using
 * <code>Arrays.hashCode</code> and <code>Arrays.equals</code>.
 * </p>
 */
public final record CharArray(char[] key) implements Comparable<CharArray> {

	public CharArray(char[] key) {
		this.key = key;
	}

	@Override
	public int compareTo(CharArray o) {
		return Arrays.compare(this.key, o.key);
	}

	public char[] getKey() {
		return this.key;
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof CharArray)) {
			return false;
		}
		return Arrays.equals(this.key, ((CharArray) obj).key);
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(this.key);
	}

	/**
	 * @return <code>Arrays.toString</code> of the underlying array
	 */
	@Override
	public String toString() {
		return Arrays.toString(this.key);
	}
}
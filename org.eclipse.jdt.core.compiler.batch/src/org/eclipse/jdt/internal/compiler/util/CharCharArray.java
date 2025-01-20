/*******************************************************************************
 * Copyright (c) 2025 jkubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     jkubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import java.util.Arrays;

/**
 * Wrapper around char[][] that can be used as a key in a Map or Set.
 */
public final record CharCharArray(char[][] key) implements Comparable<CharCharArray> {

	public CharCharArray(char[][] key) {
		this.key = key;
	}

	@Override
	public int compareTo(CharCharArray other) {
		int length = Math.min(this.key.length, other.key.length);
		for (int i = 0; i < length; i++) {
			int c = Arrays.compare(this.key[i], other.key[i]);
			if (c != 0) {
				return c;
			}
		}
		return this.key.length - other.key.length;
	}

	public char[][] getKey() {
		return this.key;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharCharArray other) {
			return Arrays.deepEquals(this.key, other.key);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return Arrays.deepHashCode(this.key);
	}

	/**
	 * @return <code>Arrays.toString</code> of the underlying array
	 */
	@Override
	public String toString() {
		return Arrays.deepToString(this.key);
	}
}
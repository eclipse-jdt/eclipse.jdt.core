package org.eclipse.jdt.internal.compiler.util;

import java.util.Arrays;

public final record CharCharArray(char[][] key) {

	public CharCharArray(char[][] key) {
		this.key = key;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof CharCharArray other) {
			return Arrays.deepEquals(this.key, other.key);
		}
		return false;
	}

}
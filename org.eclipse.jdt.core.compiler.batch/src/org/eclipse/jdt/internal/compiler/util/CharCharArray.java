package org.eclipse.jdt.internal.compiler.util;

import java.util.Arrays;

public final record CharCharArray(char[][] key) implements Comparable<CharCharArray> {

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

	@Override
	public int compareTo(CharCharArray o) {
		return 0;
	}

}
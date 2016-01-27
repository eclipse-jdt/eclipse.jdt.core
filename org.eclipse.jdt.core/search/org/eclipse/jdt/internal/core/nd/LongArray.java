package org.eclipse.jdt.internal.core.nd;

/**
 * Represents an array of long
 * @since 3.12
 */
public class LongArray {
	private static final int MIN_CAPACITY = 8;
	private long[] contents;
	private int size;

	long get(int index) {
		if (index >= this.size) {
			throw new ArrayIndexOutOfBoundsException(index);
		}

		return this.contents[index];
	}

	long removeLast() {
		return this.contents[--this.size];
	}

	void addLast(long toAdd) {
		ensureCapacity(this.size + 1);
		this.contents[this.size++] = toAdd;
	}

	private void ensureCapacity(int capacity) {
		if (this.contents == null) {
			this.contents = new long[Math.max(MIN_CAPACITY, capacity)];
		}

		if (this.contents.length >= capacity) {
			return;
		}

		int newSize = capacity * 2;
		long[] newContents = new long[newSize];

		System.arraycopy(this.contents, 0, newContents, 0, this.contents.length);
		this.contents = newContents;
	}

	int size() {
		return this.size;
	}

	public boolean isEmpty() {
		return this.size == 0;
	}
}

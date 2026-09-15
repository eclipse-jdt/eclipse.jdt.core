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
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Unit tests for {@link CharArrayBuffer}, focusing on the bounds checking of
 * {@link CharArrayBuffer#append(char[], int, int)} including the integer
 * overflow edge cases of the {@code start + length} range check.
 */
public class CharArrayBufferTest extends TestCase {

	public CharArrayBufferTest(String name) {
		super(name);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(CharArrayBufferTest.class.getPackageName());
		suite.addTest(new TestSuite(CharArrayBufferTest.class));
		return suite;
	}

	private static void assertAIOOBE(char[] src, int start, int length) {
		try {
			new CharArrayBuffer().append(src, start, length);
			fail("Expected ArrayIndexOutOfBoundsException for start=" + start + ", length=" + length);
		} catch (ArrayIndexOutOfBoundsException expected) {
			// expected
		}
	}

	// --- valid cases that must succeed ---

	public void testAppendFullArray() {
		char[] src = "hello".toCharArray();
		CharArrayBuffer buffer = new CharArrayBuffer();
		buffer.append(src, 0, src.length);
		assertEquals("hello", buffer.toString());
	}

	public void testAppendSubArray() {
		char[] src = "hello".toCharArray();
		CharArrayBuffer buffer = new CharArrayBuffer();
		buffer.append(src, 1, 3);
		assertEquals("ell", buffer.toString());
	}

	public void testAppendZeroLengthIsNoOp() {
		char[] src = "hello".toCharArray();
		CharArrayBuffer buffer = new CharArrayBuffer();
		buffer.append(src, 5, 0); // start == srcLength, length == 0 -> allowed no-op
		buffer.append(src, 0, 0);
		assertNull("nothing appended -> null contents", buffer.getContents());
	}

	public void testAppendNullSrcIsNoOp() {
		CharArrayBuffer buffer = new CharArrayBuffer();
		buffer.append((char[]) null, 0, 0);
		buffer.append((char[]) null, 10, 20); // null short-circuits before bounds copy
		assertNull(buffer.getContents());
	}

	public void testAppendTailBoundary() {
		char[] src = "hello".toCharArray();
		CharArrayBuffer buffer = new CharArrayBuffer();
		buffer.append(src, 4, 1); // last char
		assertEquals("o", buffer.toString());
	}

	// --- invalid cases that must throw ---

	public void testNegativeStart() {
		assertAIOOBE("hello".toCharArray(), -1, 1);
	}

	public void testNegativeLength() {
		assertAIOOBE("hello".toCharArray(), 0, -1);
	}

	public void testStartBeyondLength() {
		assertAIOOBE("hello".toCharArray(), 6, 0);
	}

	public void testRangeExceedsLength() {
		assertAIOOBE("hello".toCharArray(), 3, 5); // 3 + 5 = 8 > 5
	}

	// The core of this test: start + length overflows int and wraps negative.
	// The old check "length + start > srcLength" would pass (negative < srcLength)
	// and let System.arraycopy read past the end. The fixed check must throw.
	public void testAdditionOverflowIsRejected() {
		char[] src = "hello".toCharArray();
		assertAIOOBE(src, 1, Integer.MAX_VALUE);
		assertAIOOBE(src, Integer.MAX_VALUE, 1);
		assertAIOOBE(src, Integer.MAX_VALUE, Integer.MAX_VALUE);
		assertAIOOBE(src, 2, Integer.MAX_VALUE - 1); // 2 + (MAX-1) = MAX+1 -> overflow
	}

	// Ensure no huge allocation happens: guard must trigger before any copy even
	// with a tiny source array and enormous indices.
	public void testOverflowDoesNotAllocate() {
		char[] src = new char[1];
		assertAIOOBE(src, 1, Integer.MAX_VALUE);
	}
}

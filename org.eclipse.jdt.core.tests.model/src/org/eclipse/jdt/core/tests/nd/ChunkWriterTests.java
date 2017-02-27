/*******************************************************************************
 * Copyright (c) 2005, 2016 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.db.ChunkWriter;

import junit.framework.AssertionFailedError;
import junit.framework.Test;

/**
 * Tests for the {@link ChunkWriter} class.
 */
public class ChunkWriterTests extends BaseTestCase {
	private ChunkWriter writer;
	private long sleepTime;
	private long[] desiredPosition;
	private int[] desiredLength;
	private boolean wasInterrupted;
	private int writeCount;

	@FunctionalInterface
	private interface IoExceptionRunnable {
		public void run() throws IOException;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.writer = new ChunkWriter(0x1000, 1, this::write);
		this.writer.setSleepFunction(this::sleepFunction);
		this.sleepTime = 0;
	}

	public static Test suite() {
		return BaseTestCase.suite(ChunkWriterTests.class);
	}

	@Override
	protected void tearDown() throws Exception {
		this.writer = null;
	}

	private void sleepFunction(long milliseconds) {
		this.sleepTime += milliseconds;
	}

	private boolean write(ByteBuffer data, long position) throws IOException {
		if (this.writeCount > this.desiredLength.length) {
			throw new AssertionFailedError("Too many calls to write");
		}
		assertEquals(this.desiredPosition[this.writeCount], position);
		assertEquals(this.desiredLength[this.writeCount], data.limit());

		this.writeCount++;
		return this.wasInterrupted;
	}

	private void runTest(IoExceptionRunnable runnable, long[] positions, int[] lengths, int expectedElapsed,
			boolean expectedReturnValue) throws IOException {
		this.desiredPosition = positions;
		this.desiredLength = lengths;
		this.wasInterrupted = expectedReturnValue;
		long startTime = System.currentTimeMillis();
		runnable.run();
		long elapsed = System.currentTimeMillis() - startTime;
		assertTrue("Insufficient sleep time", elapsed + this.sleepTime >= expectedElapsed);
		assertEquals("Incorrect number of writes", positions.length, this.writeCount);
	}

	public void testNoWritesIfNoFlush() throws Exception {
		runTest(() -> {
			this.writer.write(100, new byte[200]);
		}, new long[0], new int[0], 0, false);
	}

	public void testNoWritesIfFlushWithNoData() throws Exception {
		runTest(() -> {
			this.writer.flush();
		}, new long[0], new int[0], 0, false);
	}

	public void testWriteAfterFlush() throws Exception {
		runTest(() -> {
			this.writer.write(100, new byte[200]);
			this.writer.flush();
		}, new long[] {100}, new int[] {200}, 200, false);
	}

	public void testWriteAfterBufferFull() throws Exception {
		runTest(() -> {
			this.writer.write(0x100, new byte[0x200]);
			this.writer.write(0x300, new byte[0x1000]);
			this.writer.flush();
		}, new long[] {0x100, 0x300}, new int[] {0x200, 0x1000}, 0x1200, false);
	}

	public void testSequentialWritesCombined() throws Exception {
		runTest(() -> {
			this.writer.write(100, new byte[200]);
			this.writer.write(300, new byte[100]);
			this.writer.flush();
		}, new long[] {100}, new int[] {300}, 300, false);
	}

	public void testNonSequentialWrites() throws Exception {
		runTest(() -> {
			this.writer.write(100, new byte[200]);
			this.writer.write(400, new byte[100]);
			this.writer.flush();
		}, new long[] {100, 400}, new int[] {200, 100}, 300, true);
	}
}

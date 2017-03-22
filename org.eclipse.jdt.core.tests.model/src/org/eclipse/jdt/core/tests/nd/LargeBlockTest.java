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

import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.Database;

import junit.framework.Test;

/**
 * Tests for the {@link Database} class.
 */
public class LargeBlockTest extends BaseTestCase {
	private Nd nd;
	protected Database db;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		String testName = getName();
		this.nd = DatabaseTestUtil.createWithoutNodeRegistry(testName);
		this.db = this.nd.getDB();
		this.db.setExclusiveLock();
		this.db.flush();
	}

	public static Test suite() {
		return BaseTestCase.suite(LargeBlockTest.class);
	}

	@Override
	protected void tearDown() throws Exception {
		DatabaseTestUtil.deleteDatabase(this.db);
		this.db = null;
	}

	private long mallocChunks(int chunks) {
		return malloc(Database.getBytesThatFitInChunks(chunks));
	}

	private long malloc(long bytes) {
		return this.db.malloc(bytes, Database.POOL_MISC);
	}

	private void free(long address) {
		this.db.free(address, Database.POOL_MISC);
	}

	/**
	 * Allocate the maximum number of bytes that can fit in 3 chunks and verify
	 * that it doesn't overflow.
	 */
	public void testAllocationThatFillsMultipleChunksDoesntOverflow() throws Exception {
		int chunkCount = this.db.getChunkCount();

		int numChunks = 5;
		mallocChunks(numChunks);

		assertEquals("The database should not allocate more (or less) memory than is needed", numChunks + chunkCount,
				this.db.getChunkCount());
	}

	/**
	 * Allocates a few blocks, frees them, then allocates more blocks. Verifies
	 * that the database reuses the chunks from the first allocation when it
	 * tries to allocate the larger block later.
	 */
	public void testLastChunkIsReused() throws Exception {
		int chunkCount = this.db.getChunkCount();

		int numChunks = 10;
		long temporaryBlockAddress = mallocChunks(3);
		free(temporaryBlockAddress);
		mallocChunks(numChunks);

		assertEquals("If the last chunk is free, it should be resized if necessary when a new chunk is requested",
				numChunks + chunkCount, this.db.getChunkCount());
	}

	/**
	 * Tests that if there is a single large free block available, that block
	 * will be split and reused if necessary to satisfy a number of smaller
	 * requests.
	 * 
	 * @throws Exception
	 */
	public void testLargeAllocationIsSplitAndReused() throws Exception {
		long tempAddress = malloc(Database.getBytesThatFitInChunks(10));
		// Use some space at the end of the database to prevent the allocator
		// from using the end of the database, where stuff can be easily resized
		mallocChunks(1);
		free(tempAddress);

		// Keep track of how much memory we are currently using, so we can
		// ensure that any further allocations come from the freed block rather
		// than the end of the database.
		int chunkCount = this.db.getChunkCount();

		long firstAllocation = mallocChunks(7);

		assertEquals("The freed chunk should be reused (there should be 10 chunks available)", chunkCount,
				this.db.getChunkCount());

		long secondAllocation = mallocChunks(1);

		assertEquals("The freed chunk should be reused (there should be 3 chunks available)", chunkCount,
				this.db.getChunkCount());

		long thirdAllocation = mallocChunks(2);

		assertEquals("The freed chunk should be reused (there should be exactly 2 chunks available)", chunkCount,
				this.db.getChunkCount());
		assertTrue(
				"Allocations should happen from the start of the database if it makes no difference to fragmentation",
				secondAllocation > firstAllocation);
		assertTrue("Free space should have been kept next to the largest block for as long as possible",
				secondAllocation > thirdAllocation);

		// Do another allocation when there are no free chunks
		mallocChunks(1);

		assertEquals("New chunks should be allocated when the database is out of free blocks", chunkCount + 1,
				this.db.getChunkCount());
	}

	/**
	 * Verifies that if a block is freed and the previous block is also free,
	 * the two free blocks will be combined into a single larger block.
	 */
	public void testFreeBlockMergesWithPrevious() throws Exception {
		long firstBlock = mallocChunks(1);
		long secondBlock = mallocChunks(1);
		mallocChunks(1);

		free(firstBlock);
		free(secondBlock);

		int chunkCount = this.db.getChunkCount();

		mallocChunks(2);
		assertEquals("The merged block should have been used", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Verifies that if a block is freed and the next block is also free, the
	 * two free blocks will be combined into a single larger block.
	 */
	public void testFreeBlockMergesWithNext() throws Exception {
		long firstBlock = mallocChunks(1);
		long secondBlock = mallocChunks(1);
		mallocChunks(1);

		free(secondBlock);
		free(firstBlock);

		int chunkCount = this.db.getChunkCount();

		mallocChunks(2);
		assertEquals("The merged block should have been used", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Verifies that if a block is freed and the blocks on both sides are also
	 * free, the three free blocks will be combined into a single larger block.
	 */
	public void testFreeBlockMergesWithBothNextAndPrevious() throws Exception {
		long firstBlock = mallocChunks(1);
		long secondBlock = mallocChunks(1);
		long thirdBlock = mallocChunks(1);
		mallocChunks(1);

		free(firstBlock);
		free(thirdBlock);
		free(secondBlock);

		int chunkCount = this.db.getChunkCount();

		mallocChunks(3);
		assertEquals("The merged block should have been used", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Tests removal of a chunk from the free space trie when there are
	 * duplicate free space nodes with the same size and the node being removed
	 * isn't the one with the embedded trie node.
	 */
	public void testRemoveFreeSpaceNodeFromDuplicateList() throws Exception {
		long chunk1 = mallocChunks(1);
		mallocChunks(1);
		long chunk3 = mallocChunks(1);
		long chunk4 = mallocChunks(1);
		mallocChunks(1);

		int chunkCount = this.db.getChunkCount();

		free(chunk1);
		free(chunk3);
		// At this point chunks 1 and 3 should be in the same linked list. Chunk
		// 1 contains the embedded trie.

		free(chunk4);
		// Should merge with chunk3, causing it to be removed from the list

		// Verify that we can allocate the merged chunk 3+4
		mallocChunks(2);

		assertEquals("Chunks 3 and 4 should have been merged", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Tests removal of a chunk from the free space trie when the node being
	 * removed was part of the embedded trie and it has a non-empty list of
	 * other nodes of the same size.
	 */
	public void testRemoveFreeSpaceNodeFromTrieWithDuplicates() throws Exception {
		long chunk1 = mallocChunks(1);
		mallocChunks(1);
		long chunk3 = mallocChunks(1);
		long chunk4 = mallocChunks(1);
		mallocChunks(1);

		int chunkCount = this.db.getChunkCount();

		free(chunk3);
		free(chunk1);
		// At this point chunks 1 and 3 should be in the same linked list. Chunk
		// 3 contains the embedded trie.

		free(chunk4);
		// Should merge with chunk3, causing it to be removed from the list

		// Verify that we can allocate the merged chunk 3+4
		mallocChunks(2);

		assertEquals("Chunks 3 and 4 should have been merged", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Tests reusing a chunk from the free space trie when it contains
	 * duplicates.
	 */
	public void testReuseDeallocatedChunksWithMultipleFreeSpaceNodesOfTheSameSize() throws Exception {
		long chunk1 = mallocChunks(2);
		mallocChunks(1);
		long chunk3 = mallocChunks(2);
		mallocChunks(1);
		long chunk5 = mallocChunks(2);
		mallocChunks(1);

		int chunkCount = this.db.getChunkCount();

		free(chunk1);
		free(chunk3);
		free(chunk5);

		mallocChunks(2);

		assertEquals("A chunk should have been reused", chunkCount, this.db.getChunkCount());
	}

	public void testEndOfFreeBlockIsUsedIfThePreviousBlockIsLargerThanTheNextBlock() throws Exception {
		long prevChunk = mallocChunks(4);
		long middleChunk = mallocChunks(4);
		long nextChunk = mallocChunks(2);

		free(middleChunk);
		// This should be taken from the end of "middleChunk", since that's closer to the smaller neighbor
		long smallChunk1 = mallocChunks(1);
		// This should also be taken from the end of the remaining portion of "middleChunk"
		long smallChunk2 = mallocChunks(1);

		assertTrue("The small chunks should have been allocated from space after 'prevChunk'",
				prevChunk < smallChunk2);
		assertTrue("The small chunks should have been allocated from the end of the free block",
				smallChunk2 < smallChunk1);
		assertTrue("The small chunks should have been allocated from space before 'nextChunk'",
				smallChunk1 < nextChunk);
	}

	/**
	 * Tests various corner cases in the trie map.
	 */
	public void testTriesOfVariousSize() throws Exception {
		long chunk1 = mallocChunks(1);
		mallocChunks(1);
		long chunk2 = mallocChunks(2);
		mallocChunks(1);
		long chunk3 = mallocChunks(3);
		mallocChunks(1);
		long chunk4 = mallocChunks(5);
		mallocChunks(1);
		long chunk5 = mallocChunks(6);
		mallocChunks(1);
		long chunk6 = mallocChunks(6);
		mallocChunks(1);
		long chunk7 = mallocChunks(10);
		mallocChunks(1);
		long chunk8 = mallocChunks(20);
		mallocChunks(1);

		int chunkCount = this.db.getChunkCount();

		free(chunk7);
		free(chunk4);
		free(chunk1);
		free(chunk3);
		free(chunk8);
		free(chunk5);
		free(chunk2);
		free(chunk6);

		mallocChunks(4);
		mallocChunks(10);

		assertEquals("A chunk should have been reused", chunkCount, this.db.getChunkCount());
	}

	/**
	 * Tests that if there are multiple free blocks of different sizes and of
	 * exactly one of the requested size, that one is always selected.
	 */
	public void testBestBlockIsAlwaysSelected() throws Exception {
		int[] sizes = { 11, 2, 6, 1, 9, 10, 7, 8, 12, 20, 15, 3 };
		long[] pointers = new long[sizes.length];

		for (int idx = 0; idx < sizes.length; idx++) {
			pointers[idx] = mallocChunks(sizes[idx]);
			mallocChunks(1);
		}

		int chunkCount = this.db.getChunkCount();

		for (int idx = 0; idx < pointers.length; idx++) {
			free(pointers[idx]);
		}

		for (int idx = 0; idx < sizes.length; idx++) {
			long nextPointer = mallocChunks(sizes[idx]);
			assertEquals("Returned wrong pointer for malloc of " + sizes[idx] + " chunks", pointers[idx], nextPointer);
			assertEquals("A chunk should have been reused", chunkCount, this.db.getChunkCount());
		}
	}
}

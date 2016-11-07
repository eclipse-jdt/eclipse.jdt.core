/*******************************************************************************
 * Copyright (c) 2006, 2016 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Symbian - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import junit.framework.Test;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.db.BTree;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IBTreeComparator;
import org.eclipse.jdt.internal.core.nd.db.IBTreeVisitor;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Test insertion/deletion of records of a mock record type in a B-tree.
 *
 * @author aferguso
 */
public class BTreeTests extends BaseTestCase {
	private static int DEBUG= 0;
	protected File dbFile;
	protected Nd nd;
	protected Database db;
	protected BTree btree;
	protected int rootRecord;
	protected IBTreeComparator comparator;

	public static Test suite() {
		return suite(BTreeTests.class);
	}

	// setUp is not used since we need to parameterize this method,
	// and invoke it multiple times per Junit test
	protected void init(int degree) throws Exception {
		this.dbFile = File.createTempFile("ndtest", "db");
		this.nd = DatabaseTestUtil.createEmptyNd(getName());
		this.db = this.nd.getDB();
		this.db.setExclusiveLock();
		this.rootRecord = Database.DATA_AREA_OFFSET;
		this.comparator = new BTMockRecordComparator();
		this.btree = new BTree(this.nd, this.rootRecord, degree, this.comparator);
	}

	// tearDown is not used for the same reason as above
	protected void finish() throws Exception {
		this.db.close();
		this.dbFile.deleteOnExit();
	}


	public void testBySortedSetMirrorLite() throws Exception {
		sortedMirrorTest(8);
	}

	/**
	 * Test random (but reproducible via known seed) sequences of insertions/deletions
	 * and use TreeSet as a reference implementation to check behaviour against.
	 * @throws Exception
	 */
	protected void sortedMirrorTest(int noTrials) throws Exception {
		Random seeder = new Random(90210);

		for (int i = 0; i < noTrials; i++) {
			int seed = seeder.nextInt();
			if (DEBUG > 0)
				System.out.println("Iteration #" + i);
			trial(seed, false);
		}
	}

	/**
	 * Test random (but reproducible via known seed) sequence of insertions
	 * and use TreeSet as a reference implementation to check behaviour against.
	 * @throws Exception
	 */
	public void testInsertion() throws Exception {
		Random seeder = new Random();

		for (int i = 0; i < 6; i++) {
			int seed = seeder.nextInt();
			if (DEBUG > 0)
				System.out.println("Iteration #" + i);
			trialImp(seed, false, new Random(seed * 2), 1);
		}
	}

	/**
	 * Bug 402177: BTree.insert should return the matching record if the new record was not inserted.
	 */
	public void testEquivalentRecordInsert_Bug402177() throws Exception {
		init(8);
		try {
			BTMockRecord value1 = new BTMockRecord(this.db, 42);
			BTMockRecord value2 = new BTMockRecord(this.db, 42);

			long insert1 = this.btree.insert(value1.getRecord());
			long insert2 = this.btree.insert(value2.getRecord());
			assertEquals(insert1, insert2);
		} finally {
			finish();
		}
	}

	/**
	 * Insert/Delete a random number of records into/from the B-tree
	 * @param seed the seed for obtaining the deterministic random testing
	 * @param checkCorrectnessEachIteration if true, then on every single insertion/deletion check that the B-tree invariants
	 * still hold
	 * @throws Exception
	 */
	protected void trial(int seed, final boolean checkCorrectnessEachIteration) throws Exception {
		Random random = new Random(seed);

		// the probabilty that a particular iterations action will be an insertion
		double pInsert = Math.min(0.5 + random.nextDouble(), 1);

		trialImp(seed, checkCorrectnessEachIteration, random, pInsert);
	}

	private void trialImp(int seed, final boolean checkCorrectnessEachIteration, Random random,
			double pInsert) throws Exception {
		final int degree = 2 + random.nextInt(11);
		final int nIterations = random.nextInt(100000);
		final SortedSet<Integer> expected = new TreeSet<>();
		final List<BTMockRecord> history = new ArrayList<>();

		init(degree);

		if (DEBUG > 0)
			System.out.print("\t " + seed + " " + (nIterations/1000) + "K: ");
		for (int i = 0; i < nIterations; i++) {
			if (random.nextDouble() < pInsert) {
				Integer value = new Integer(random.nextInt(Integer.MAX_VALUE));
				boolean newEntry = expected.add(value);
				if (newEntry) {
					BTMockRecord btValue = new BTMockRecord(this.db, value.intValue());
					history.add(btValue);
					if (DEBUG > 1)
						System.out.println("Add: " + value + " @ " + btValue.record);
					this.btree.insert(btValue.getRecord());
				}
			} else {
				if (!history.isEmpty()) {
					int index = random.nextInt(history.size());
					BTMockRecord btValue = history.get(index);
					history.remove(index);
					expected.remove(new Integer(btValue.intValue()));
					if (DEBUG > 1)
						System.out.println("Remove: " + btValue.intValue() + " @ " + btValue.record);
					this.btree.delete(btValue.getRecord());
				}
			}
			if (i % 1000 == 0 && DEBUG > 0) {
				System.out.print(".");
			}
			if (checkCorrectnessEachIteration) {
				assertBTreeMatchesSortedSet("[iteration " + i + "] ", this.btree, expected);
				assertBTreeInvariantsHold("[iteration " + i + "] ");
			}
		}
		if (DEBUG > 0)
			System.out.println();

		assertBTreeMatchesSortedSet("[Trial end] ", this.btree, expected);
		assertBTreeInvariantsHold("[Trial end]");

		finish();
	}

	public void assertBTreeInvariantsHold(String msg) throws CoreException {
		String errorReport = this.btree.getInvariantsErrorReport();
		if (!errorReport.equals("")) {
			fail("Invariants do not hold: " + errorReport);
		}
	}

	public void assertBTreeMatchesSortedSet(final String msg, BTree actual, SortedSet<Integer> expected) throws CoreException {
		final Iterator<Integer> i = expected.iterator();
		this.btree.accept(new IBTreeVisitor() {
			int k;
			@Override
			public int compare(long record) {
				return 0;
			}

			@Override
			public boolean visit(long record) {
				if (record != 0) {
					BTMockRecord btValue = new BTMockRecord(record, BTreeTests.this.db);
					if (i.hasNext()) {
						Integer exp = i.next();
						assertEquals(msg + " Differ at index: " + this.k, btValue.intValue(), exp.intValue());
						this.k++;
					} else {
						fail("Sizes different");
						return false;
					}
				}
				return true;
			}
		});
	}

	private static class BTMockRecord {
		public static final int VALUE_PTR = 0;
		public static final int RECORD_SIZE = Database.INT_SIZE;
		long record;
		Database db;

		/**
		 * Make a new record
		 */
		public BTMockRecord(Database db, int value) throws CoreException {
			this.db = db;
			this.record = db.malloc(BTMockRecord.RECORD_SIZE, Database.POOL_MISC);
			db.putInt(this.record + VALUE_PTR, value);
		}

		/**
		 * Get an existing record
		 */
		public BTMockRecord(long record, Database db) {
			this.db = db;
			this.record = record;
		}

		public int intValue() {
			return this.db.getInt(this.record);
		}

		public long getRecord() {
			return this.record;
		}
	}

	private class BTMockRecordComparator implements IBTreeComparator {
		public BTMockRecordComparator() {
		}

		@Override
		public int compare(Nd ndToCompare, long record1, long record2) {
			Database dbToCompare = ndToCompare.getDB();
			return dbToCompare.getInt(record1) - dbToCompare.getInt(record2);
		}
	}
}

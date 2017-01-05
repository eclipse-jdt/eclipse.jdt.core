/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToOne;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

import junit.framework.Test;

public class FieldOneToOneTest extends BaseTestCase {
	static int struct2Deletions;
	static int structDeletions;

	public static class TestStruct extends NdNode {
		public static final FieldOneToOne<TestStruct2> ANOTHER_STRUCT;
		public static final FieldOneToOne<TestStruct2> OWNED;

		@SuppressWarnings("hiding")
		public static final StructDef<TestStruct> type;

		static {
			type = StructDef.create(TestStruct.class, NdNode.type);

			ANOTHER_STRUCT = FieldOneToOne.create(type, TestStruct2.class, TestStruct2.ANOTHER_STRUCT);
			OWNED = FieldOneToOne.create(type, TestStruct2.class, TestStruct2.OWNER);
			type.done();
		}

		public TestStruct(Nd nd) {
			super(nd);
		}

		public TestStruct(Nd nd, long record) {
			super(nd, record);
		}

		public void setAnother(TestStruct2 toSet) {
			ANOTHER_STRUCT.put(getNd(), this.address, toSet);
		}

		public TestStruct2 getAnother() {
			return ANOTHER_STRUCT.get(getNd(), this.address);
		}

		public void setOwned(TestStruct2 owned) {
			OWNED.put(getNd(), this.address, owned);
		}

		public TestStruct2 getOwned() {
			return OWNED.get(getNd(), this.address);
		}

		@Override
		public void destruct() {
			super.destruct();
			FieldOneToOneTest.structDeletions++;
		}
	}

	public static class TestStruct2 extends NdNode {
		public static final FieldOneToOne<TestStruct> OWNER;
		public static final FieldOneToOne<TestStruct> ANOTHER_STRUCT;

		@SuppressWarnings("hiding")
		public static final StructDef<TestStruct2> type;

		static {
			type = StructDef.create(TestStruct2.class, NdNode.type);

			OWNER = FieldOneToOne.createOwner(type, TestStruct.class, TestStruct.OWNED);
			ANOTHER_STRUCT = FieldOneToOne.create(type, TestStruct.class, TestStruct.ANOTHER_STRUCT);

			type.done();
		}

		public TestStruct2(Nd nd) {
			super(nd);
		}

		public TestStruct2(Nd nd, long record) {
			super(nd, record);
		}

		public void setAnother(TestStruct toSet) {
			ANOTHER_STRUCT.put(getNd(), this.address, toSet);
		}

		public TestStruct getAnother() {
			return ANOTHER_STRUCT.get(getNd(), this.address);
		}

		public void setOwner(TestStruct owner) {
			OWNER.put(getNd(), this.address, owner);
		}

		public TestStruct getOwner() {
			return OWNER.get(getNd(), this.address);
		}

		@Override
		public void destruct() {
			super.destruct();
			FieldOneToOneTest.struct2Deletions++;
		}
	}

	TestStruct referencer;
	TestStruct owner;
	TestStruct2 referenced;
	TestStruct2 owned;
	private Nd nd;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		structDeletions = 0;
		struct2Deletions = 0;
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0, TestStruct2.type.getFactory());
		registry.register(1, TestStruct.type.getFactory());
		this.nd = DatabaseTestUtil.createEmptyNd(getName(), registry);
		this.nd.getDB().setExclusiveLock();
		this.referencer = new TestStruct(this.nd);
		this.owner = new TestStruct(this.nd);
		this.referenced = new TestStruct2(this.nd);
		this.owned = new TestStruct2(this.nd);

		this.owner.setOwned(this.owned);
		this.referencer.setAnother(this.referenced);
	}

	public static Test suite() {
		return BaseTestCase.suite(FieldOneToOneTest.class);
	}

	public void testPointersSetCorrectly() {
		assertEquals(this.owner.address, this.owned.getOwner().address);
		assertEquals(this.owned.address, this.owner.getOwned().address);
		assertEquals(this.referencer.address, this.referenced.getAnother().address);
		assertEquals(this.referenced.address, this.referencer.getAnother().address);
	}

	public void testClearingOwnerDeletesOwned() {
		long freed = this.nd.getDB().getBytesFreed();

		this.owner.setOwned(null);

		this.nd.processDeletions();

		assertEquals(null, this.owner.getOwned());
		assertTrue(this.nd.getDB().getBytesFreed() - freed >= TestStruct2.type.size());
		assertEquals(1, struct2Deletions);
	}

	public void testDeletingOwnerDeletesOwned() {
		long freed = this.nd.getDB().getBytesFreed();

		this.owner.delete();

		this.nd.processDeletions();

		assertTrue(this.nd.getDB().getBytesFreed() - freed >= TestStruct2.type.size() + TestStruct.type.size());
		assertEquals(1, struct2Deletions);
		assertEquals(1, structDeletions);
	}

	public void testReassignedOwnerDeletesOwned() {
		long freed = this.nd.getDB().getBytesFreed();
		TestStruct2 newOwned = new TestStruct2(this.nd);

		this.owner.setOwned(newOwned);

		this.nd.processDeletions();

		long freed2 = this.nd.getDB().getBytesFreed();

		assertEquals(newOwned, this.owner.getOwned());
		assertTrue(freed2 - freed >= TestStruct2.type.size());
		assertEquals(1, struct2Deletions);
	}

	public void testMovingToNewOwnerDoesntDeleteOwned() {
		long freed = this.nd.getDB().getBytesFreed();
		TestStruct newOwner = new TestStruct(this.nd);

		this.owned.setOwner(newOwner);

		this.nd.processDeletions();

		long freed2 = this.nd.getDB().getBytesFreed();

		assertEquals(freed, freed2);
		assertEquals(null, this.owner.getOwned());
		assertEquals(this.owned, newOwner.getOwned());
		assertEquals(0, struct2Deletions);
		assertEquals(0, structDeletions);
	}

	public void testMovingToNewOwnerAfterClearingOwnerPointerDoesntDeleteOwned() {
		long freed = this.nd.getDB().getBytesFreed();
		TestStruct newOwner = new TestStruct(this.nd);

		this.owner.setOwned(null);
		this.owned.setOwner(newOwner);

		this.nd.processDeletions();

		long freed2 = this.nd.getDB().getBytesFreed();

		assertEquals(freed, freed2);
		assertEquals(null, this.owner.getOwned());
		assertEquals(this.owned, newOwner.getOwned());
		assertEquals(0, struct2Deletions);
		assertEquals(0, structDeletions);
	}

	public void testClearingOwnerPointerDeletesOwned() {
		long freed = this.nd.getDB().getBytesFreed();

		this.owned.setOwner(null);

		this.nd.processDeletions();

		long freed2 = this.nd.getDB().getBytesFreed();

		assertTrue(freed2 - freed >= TestStruct2.type.size());
		assertEquals(null, this.owner.getOwned());
		assertEquals(1, struct2Deletions);
		assertEquals(0, structDeletions);
	}

	public void testMovingReferenceClearsOldBackPointer() {
		TestStruct2 newTarget = new TestStruct2(this.nd);

		long freed = this.nd.getDB().getBytesFreed();
		this.referencer.setAnother(newTarget);
		this.nd.processDeletions();

		long freed2 = this.nd.getDB().getBytesFreed();
		assertEquals(newTarget, this.referencer.getAnother());
		assertEquals(null, this.referenced.getAnother());
		assertEquals(freed, freed2);
	}
}

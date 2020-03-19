/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

import junit.framework.Test;

public class InheritenceTests extends BaseTestCase {
	/**
	 * Every other object in this test has a pointer to the deletion detector, so we can detect
	 * which objects have been deleted by looking for the object in the backpointer list.
	 */
	public static class AllObjects extends NdNode {
		public static final FieldOneToMany<BaseClass> BASE_CLASS_INSTANCES;
		public static final FieldOneToMany<Reference> REFERENCE_INSTANCES;

		@SuppressWarnings("hiding")
		public static final StructDef<AllObjects> type;

		static {
			type = StructDef.create(AllObjects.class, NdNode.type);

			BASE_CLASS_INSTANCES = FieldOneToMany.create(type, BaseClass.DELETION_DETECTOR, 0);
			REFERENCE_INSTANCES = FieldOneToMany.create(type, Reference.DELETION_DETECTOR, 0);
			type.done();
		}

		public AllObjects(Nd nd, long record) {
			super(nd, record);
		}

		public AllObjects(Nd nd) {
			super(nd);
		}

		boolean contains(BaseClass toTest) {
			return BASE_CLASS_INSTANCES.asList(getNd(), this.address).contains(toTest);
		}

		boolean contains(Reference toTest) {
			return REFERENCE_INSTANCES.asList(getNd(), this.address).contains(toTest);
		}
	}

	public static class BaseClass extends NdNode {
		public static final FieldOneToMany<Reference> INCOMING_REFERENCES;
		public static final FieldOneToMany<Reference> OWNED_REFERENCES;
		public static final FieldManyToOne<AllObjects> DELETION_DETECTOR;

		@SuppressWarnings("hiding")
		public static final StructDef<BaseClass> type;

		static {
			type = StructDef.create(BaseClass.class, NdNode.type);

			INCOMING_REFERENCES = FieldOneToMany.create(type, Reference.BASE_CLASS_REFERENCE, 0);
			OWNED_REFERENCES = FieldOneToMany.create(type, Reference.OWNER, 0);
			DELETION_DETECTOR = FieldManyToOne.create(type, AllObjects.BASE_CLASS_INSTANCES);
			type.useStandardRefCounting().done();
		}

		public BaseClass(Nd nd, AllObjects deletionDetector) {
			super(nd);

			DELETION_DETECTOR.put(nd, this.address, deletionDetector);
		}

		public BaseClass(Nd nd, long record) {
			super(nd, record);
		}
	}

	public static class SubClass extends BaseClass {
		public static final FieldOneToMany<Reference> MORE_REFERENCES;

		@SuppressWarnings("hiding")
		public static final StructDef<SubClass> type;

		static {
			type = StructDef.create(SubClass.class, BaseClass.type);

			MORE_REFERENCES = FieldOneToMany.create(type, Reference.SUB_CLASS_REFERENCE, 0);
			type.useStandardRefCounting().done();
		}

		public SubClass(Nd nd, long record) {
			super(nd, record);
		}

		public SubClass(Nd nd, AllObjects deletionDetector) {
			super(nd, deletionDetector);
		}
	}

	public static class Reference extends NdNode {
		public static final FieldManyToOne<BaseClass> BASE_CLASS_REFERENCE;
		public static final FieldManyToOne<BaseClass> OWNER;
		public static final FieldManyToOne<SubClass> SUB_CLASS_REFERENCE;
		public static final FieldManyToOne<AllObjects> DELETION_DETECTOR;

		@SuppressWarnings("hiding")
		public static StructDef<Reference> type;

		static {
			type = StructDef.create(Reference.class, NdNode.type);

			BASE_CLASS_REFERENCE = FieldManyToOne.create(type, BaseClass.INCOMING_REFERENCES);
			OWNER = FieldManyToOne.createOwner(type, BaseClass.OWNED_REFERENCES);
			SUB_CLASS_REFERENCE = FieldManyToOne.create(type, SubClass.MORE_REFERENCES);
			DELETION_DETECTOR = FieldManyToOne.create(type, AllObjects.REFERENCE_INSTANCES);
			type.done();
		}

		public Reference(Nd nd, long record) {
			super(nd, record);
		}

		public Reference(Nd nd, AllObjects deletionDetector) {
			super(nd);

			DELETION_DETECTOR.put(nd, this.address, deletionDetector);
		}

		public void setBaseClassReference(BaseClass target) {
			BASE_CLASS_REFERENCE.put(getNd(), this.address, target);
		}

		public void setOwner(BaseClass target) {
			OWNER.put(getNd(), this.address, target);
		}

		public void setSubClassReference(SubClass target) {
			SUB_CLASS_REFERENCE.put(getNd(), this.address, target);
		}
	}

	AllObjects allObjects;
	BaseClass baseClass;
	SubClass subClass;
	Reference refA;
	Reference refB;
	Reference refC;
	private Nd nd;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0, BaseClass.type.getFactory());
		registry.register(1, SubClass.type.getFactory());
		registry.register(2, Reference.type.getFactory());
		registry.register(3, AllObjects.type.getFactory());
		this.nd = DatabaseTestUtil.createEmptyNd(getName(), registry);
		this.nd.getDB().setExclusiveLock();

		this.allObjects = new AllObjects(this.nd);
		this.baseClass = new BaseClass(this.nd, this.allObjects);
		this.subClass = new SubClass(this.nd, this.allObjects);

		this.refA = new Reference(this.nd, this.allObjects);
		this.refB = new Reference(this.nd, this.allObjects);
		this.refC = new Reference(this.nd, this.allObjects);
	}

	public static Test suite() {
		return BaseTestCase.suite(InheritenceTests.class);
	}

	public void testRemovingOnlyRefcountDeletesObject() {
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(this.subClass);
		this.refA.setSubClassReference(null);
		this.nd.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testReferencesToBaseClassIncludedInRefCountA() {
		// Test what happens when the subclass reference is removed first.
		this.refA.setSubClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(null);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.nd.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testReferencesToBaseClassIncludedInRefCountB() {
		// Test what happens when the base class reference is removed first.
		this.refA.setSubClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(null);
		this.nd.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testOwnedPointersDontCountTowardsRefCount() {
		this.refA.setOwner(this.subClass);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(this.subClass);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		assertTrue(this.allObjects.contains(this.refA));
		this.refB.setBaseClassReference(null);
		this.nd.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
		assertFalse(this.allObjects.contains(this.refA));
	}

	public void testMultipleReferences() {
		this.refA.setBaseClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		this.refA.setBaseClassReference(null);
		this.nd.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.nd.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}
}

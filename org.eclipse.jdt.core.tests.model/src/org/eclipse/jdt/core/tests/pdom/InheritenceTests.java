package org.eclipse.jdt.core.tests.pdom;

import org.eclipse.jdt.core.tests.pdom.util.BaseTestCase;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.PDOMNodeTypeRegistry;
import org.eclipse.jdt.internal.core.pdom.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.pdom.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

import junit.framework.Test;

public class InheritenceTests extends BaseTestCase {
	/**
	 * Every other object in this test has a pointer to the deletion detector, so we can detect
	 * which objects have been deleted by looking for the object in the backpointer list.
	 */
	public static class AllObjects extends PDOMNode {
		public static final FieldOneToMany<BaseClass> BASE_CLASS_INSTANCES;
		public static final FieldOneToMany<Reference> REFERENCE_INSTANCES;

		@SuppressWarnings("hiding")
		public static final StructDef<AllObjects> type;

		static {
			type = StructDef.create(AllObjects.class, PDOMNode.type);

			BASE_CLASS_INSTANCES = FieldOneToMany.create(type, BaseClass.DELETION_DETECTOR, 0);
			REFERENCE_INSTANCES = FieldOneToMany.create(type, Reference.DELETION_DETECTOR, 0);
			type.done();
		}

		public AllObjects(PDOM pdom, long record) {
			super(pdom, record);
		}

		public AllObjects(PDOM pdom) {
			super(pdom);
		}

		boolean contains(BaseClass toTest) {
			return BASE_CLASS_INSTANCES.asList(getPDOM(), this.address).contains(toTest);
		}

		boolean contains(Reference toTest) {
			return REFERENCE_INSTANCES.asList(getPDOM(), this.address).contains(toTest);
		}
	}

	public static class BaseClass extends PDOMNode {
		public static final FieldOneToMany<Reference> INCOMING_REFERENCES;
		public static final FieldOneToMany<Reference> OWNED_REFERENCES;
		public static final FieldManyToOne<AllObjects> DELETION_DETECTOR;

		@SuppressWarnings("hiding")
		public static final StructDef<BaseClass> type;

		static {
			type = StructDef.create(BaseClass.class, PDOMNode.type);

			INCOMING_REFERENCES = FieldOneToMany.create(type, Reference.BASE_CLASS_REFERENCE, 0);
			OWNED_REFERENCES = FieldOneToMany.create(type, Reference.OWNER, 0);
			DELETION_DETECTOR = FieldManyToOne.create(type, AllObjects.BASE_CLASS_INSTANCES);
			type.done();
		}

		public BaseClass(PDOM pdom, AllObjects deletionDetector) {
			super(pdom);

			DELETION_DETECTOR.put(pdom, this.address, deletionDetector);
		}

		public BaseClass(PDOM pdom, long record) {
			super(pdom, record);
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

		public SubClass(PDOM pdom, long record) {
			super(pdom, record);
		}

		public SubClass(PDOM pdom, AllObjects deletionDetector) {
			super(pdom, deletionDetector);
		}
	}
	
	public static class Reference extends PDOMNode {
		public static final FieldManyToOne<BaseClass> BASE_CLASS_REFERENCE;
		public static final FieldManyToOne<BaseClass> OWNER;
		public static final FieldManyToOne<SubClass> SUB_CLASS_REFERENCE;
		public static final FieldManyToOne<AllObjects> DELETION_DETECTOR;

		@SuppressWarnings("hiding")
		public static StructDef<Reference> type;

		static {
			type = StructDef.create(Reference.class, PDOMNode.type);

			BASE_CLASS_REFERENCE = FieldManyToOne.create(type, BaseClass.INCOMING_REFERENCES);
			OWNER = FieldManyToOne.createOwner(type, BaseClass.OWNED_REFERENCES);
			SUB_CLASS_REFERENCE = FieldManyToOne.create(type, SubClass.MORE_REFERENCES);
			DELETION_DETECTOR = FieldManyToOne.create(type, AllObjects.REFERENCE_INSTANCES);
			type.done();
		}

		public Reference(PDOM pdom, long record) {
			super(pdom, record);
		}

		public Reference(PDOM pdom, AllObjects deletionDetector) {
			super(pdom);

			DELETION_DETECTOR.put(pdom, this.address, deletionDetector);
		}

		public void setBaseClassReference(BaseClass target) {
			BASE_CLASS_REFERENCE.put(getPDOM(), this.address, target);
		}

		public void setOwner(BaseClass target) {
			OWNER.put(getPDOM(), this.address, target);
		}

		public void setSubClassReference(SubClass target) {
			SUB_CLASS_REFERENCE.put(getPDOM(), this.address, target);
		}
	}

	AllObjects allObjects;
	BaseClass baseClass;
	SubClass subClass;
	Reference refA;
	Reference refB;
	Reference refC;
	private PDOM pdom;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		PDOMNodeTypeRegistry<PDOMNode> registry = new PDOMNodeTypeRegistry<>();
		registry.register(0, BaseClass.type.getFactory());
		registry.register(1, SubClass.type.getFactory());
		registry.register(2, Reference.type.getFactory());
		registry.register(3, AllObjects.type.getFactory());
		this.pdom = DatabaseTestUtil.createEmptyPdom(getName(), registry);
		this.pdom.getDB().setExclusiveLock();

		this.allObjects = new AllObjects(this.pdom);
		this.baseClass = new BaseClass(this.pdom, this.allObjects);
		this.subClass = new SubClass(this.pdom, this.allObjects);

		this.refA = new Reference(this.pdom, this.allObjects);
		this.refB = new Reference(this.pdom, this.allObjects);
		this.refC = new Reference(this.pdom, this.allObjects);
	}

	public static Test suite() {
		return BaseTestCase.suite(InheritenceTests.class);
	}

	public void testRemovingOnlyRefcountDeletesObject() {
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(this.subClass);
		this.refA.setSubClassReference(null);
		this.pdom.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testReferencesToBaseClassIncludedInRefCountA() {
		// Test what happens when the subclass reference is removed first.
		this.refA.setSubClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(null);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.pdom.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testReferencesToBaseClassIncludedInRefCountB() {
		// Test what happens when the base class reference is removed first.
		this.refA.setSubClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refA.setSubClassReference(null);
		this.pdom.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}

	public void testOwnedPointersDontCountTowardsRefCount() {
		this.refA.setOwner(this.subClass);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(this.subClass);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		assertTrue(this.allObjects.contains(this.refA));
		this.refB.setBaseClassReference(null);
		this.pdom.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
		assertFalse(this.allObjects.contains(this.refA));
	}

	public void testMultipleReferences() {
		this.refA.setBaseClassReference(this.subClass);
		this.refB.setBaseClassReference(this.subClass);
		this.refA.setBaseClassReference(null);
		this.pdom.processDeletions();
		assertTrue(this.allObjects.contains(this.subClass));
		this.refB.setBaseClassReference(null);
		this.pdom.processDeletions();
		assertFalse(this.allObjects.contains(this.subClass));
	}
}

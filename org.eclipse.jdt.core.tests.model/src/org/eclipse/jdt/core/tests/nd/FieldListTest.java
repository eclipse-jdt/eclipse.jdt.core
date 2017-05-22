/*******************************************************************************
 * Copyright (c) 2017 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import java.util.List;

import org.eclipse.jdt.core.tests.nd.util.BaseTestCase;
import org.eclipse.jdt.internal.core.nd.IDestructable;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.NdStruct;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldList;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

import junit.framework.Test;

public class FieldListTest extends BaseTestCase {
	static int nodeDeletions;
	static int structDeletions;

	public static class ElementNode extends NdNode {
		public static final FieldString NAME;
		public static final FieldOneToMany<TestStruct> RELATED_STRUCTS;
		public static final FieldManyToOne<TestStruct> PARENT_NODE;
		public static final FieldList<TestStruct> LIST_CONTENTS;

		@SuppressWarnings("hiding")
		public static final StructDef<ElementNode> type;

		static {
			type = StructDef.create(ElementNode.class, NdNode.type);

			NAME = type.addString();
			RELATED_STRUCTS = FieldOneToMany.create(type, TestStruct.RELATED_NODE);
			PARENT_NODE = FieldManyToOne.createOwner(type, TestStruct.CHILD_NODES);
			LIST_CONTENTS = FieldList.create(type, TestStruct.type, 3);
			type.done();
		}

		public ElementNode(Nd nd, long record) {
			super(nd, record);
		}

		public ElementNode(Nd nd, String name, TestStruct parent) {
			super(nd);

			NAME.put(nd, this.address, name);
			PARENT_NODE.put(nd, this.address, parent);
		}

		public TestStruct createChild(String name) {
			TestStruct result = LIST_CONTENTS.append(this.nd, this.address);
			result.setName(name);
			return result;
		}

		public List<TestStruct> getChildren() {
			return LIST_CONTENTS.asList(getNd(), getAddress());
		}

		@Override
		public void destruct() {
			super.destruct();

			FieldListTest.nodeDeletions++;
		}

		public IString getName() {
			return NAME.get(getNd(), getAddress());
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			printStringTo(builder);
			return builder.toString();
		}

		public void printStringTo(StringBuilder builder) {
			builder.append(getName().getString());
			if (!RELATED_STRUCTS.isEmpty(getNd(), getAddress())) {
				builder.append("->[");
				boolean isFirst = true;
				for (TestStruct struct : getRelatedStructs()) {
					if (!isFirst) {
						builder.append(", ");
					}
					isFirst = false;
					builder.append(struct.getName());
				}
				builder.append("]");
			}

			List<TestStruct> children = getChildren();
			if (!children.isEmpty()) {
				builder.append("(");
				boolean isFirst = true;
				for (TestStruct struct : children) {
					if (!isFirst) {
						builder.append(", ");
					}
					isFirst = false;
					struct.printStringTo(builder);
				}
				builder.append(")");
			}
		}

		public List<TestStruct> getRelatedStructs() {
			return RELATED_STRUCTS.asList(getNd(), getAddress());
		}
	}

	public static class TestStruct extends NdStruct implements IDestructable {
		public static final FieldString NAME;
		public static final FieldOneToMany<ElementNode> CHILD_NODES;
		public static final FieldManyToOne<ElementNode> RELATED_NODE;
		public static final FieldByte EXTRA_BYTE;

		@SuppressWarnings("hiding")
		public static final StructDef<TestStruct> type;

		static {
			type = StructDef.create(TestStruct.class, NdStruct.type);

			NAME = type.addString();
			CHILD_NODES = FieldOneToMany.create(type, ElementNode.PARENT_NODE);
			RELATED_NODE = FieldManyToOne.create(type, ElementNode.RELATED_STRUCTS);
			EXTRA_BYTE = type.addByte();
			type.done();
		}

		public TestStruct(Nd nd, long record) {
			super(nd, record);
		}

		public void printStringTo(StringBuilder builder) {
			builder.append(getName().getString());
			ElementNode related = RELATED_NODE.get(getNd(), getAddress());
			if (related != null) {
				builder.append("->[");
				builder.append(related.getName().getString());
				builder.append("]");
			}

			List<ElementNode> children = getChildren();
			if (!children.isEmpty()) {
				builder.append("(");
				boolean isFirst = true;
				for (ElementNode struct : children) {
					if (!isFirst) {
						builder.append(", ");
					}
					isFirst = false;
					struct.printStringTo(builder);
				}
				builder.append(")");
			}
		}

		public String toString() {
			StringBuilder builder = new StringBuilder();
			printStringTo(builder);
			return builder.toString();
		}

		public IString getName() {
			return NAME.get(getNd(), getAddress());
		}

		public void setRelatedNode(ElementNode elementNode) {
			RELATED_NODE.put(getNd(), getAddress(), elementNode);
		}

		public ElementNode getRelatedNode() {
			return RELATED_NODE.get(getNd(), getAddress());
		}

		public void setName(String name) {
			NAME.put(getNd(), getAddress(), name);
		}

		public List<ElementNode> getChildren() {
			return CHILD_NODES.asList(getNd(), getAddress());
		}

		public ElementNode createChild(String name) {
			return new ElementNode(getNd(), name, this);
		}

		@Override
		public void destruct() {
			FieldListTest.structDeletions++;
		}
	}

	TestStruct referencer;
	TestStruct owner;
	private Nd nd;
	private ElementNode root;
	private boolean nodeDeleted;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		structDeletions = 0;
		nodeDeletions = 0;
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0, ElementNode.type.getFactory());
		this.nd = DatabaseTestUtil.createEmptyNd(getName(), registry);
		this.nd.getDB().setExclusiveLock();
		this.root = new ElementNode(this.nd, "root", null);
	}

	protected void freeAllMemory() throws Exception {
		if (this.nodeDeleted) {
			return;
		}
		this.nodeDeleted = true;
		this.root.delete();
		this.nd.processDeletions();
		long freed = this.nd.getDB().getBytesFreed();
		long allocated = this.nd.getDB().getBytesAllocated();
		assertEquals("We should have freed all the bytes we allocated and no more", allocated, freed);
	}

	@Override
	protected void tearDown() throws Exception {
		freeAllMemory();
		super.tearDown();
	}

	public void testEmptyList() throws Exception {
		assertTrue("isEmpty() should return true if no children inserted",
				this.root.getChildren().isEmpty());
		freeAllMemory();
		assertEquals("No structs should have been disposed during this test", 0, structDeletions);
		assertEquals("One node should have been disposed during this test", 1, nodeDeletions);
	}

	public void testOneChild() throws Exception {
		TestStruct testStruct = this.root.createChild("child");
		assertEquals("root should be initialized properly", "child", testStruct.getName().getString());
		assertEquals("root should have correct contents", "root(child)", this.root.toString());
		freeAllMemory();
		assertEquals("No structs should have been disposed during this test", 1, structDeletions);
		assertEquals("One node should have been disposed during this test", 1, nodeDeletions);
	}

	public void testElementsInBlock() throws Exception {
		this.root.createChild("child1");
		this.root.createChild("child2");
		assertEquals("root should have correct contents", "root(child1, child2)", this.root.toString());
		this.root.createChild("child3");
		assertEquals("root should have correct contents", "root(child1, child2, child3)",
				this.root.toString());
		this.root.createChild("child4");
		assertEquals("root should have correct contents", "root(child1, child2, child3, child4)",
				this.root.toString());
		this.root.createChild("child5");
		assertEquals("root should have correct contents", "root(child1, child2, child3, child4, child5)",
				this.root.toString());
		freeAllMemory();
		assertEquals("No structs should have been disposed during this test", 5, structDeletions);
		assertEquals("One node should have been disposed during this test", 1, nodeDeletions);
	}

	public void testDestructorInPartiallyFilledBlock() throws Exception {
		this.root.createChild("child1");
		this.root.createChild("child2");
		this.root.createChild("child3");
		this.root.createChild("child4");
		freeAllMemory();
		assertEquals("No structs should have been disposed during this test", 4, structDeletions);
		assertEquals("One node should have been disposed during this test", 1, nodeDeletions);
	}

	public void testListOwningNode() throws Exception {
		TestStruct child1 = this.root.createChild("child1");
		child1.createChild("grandchild1");

		assertEquals("root should have correct contents", "root(child1(grandchild1))", this.root.toString());

		freeAllMemory();
		assertEquals("No structs should have been disposed during this test", 1, structDeletions);
		assertEquals("One node should have been disposed during this test", 2, nodeDeletions);
	}

	public void testListWithManyToOneNode() throws Exception {
		TestStruct child1 = this.root.createChild("child1");
		ElementNode relatedNode = new ElementNode(this.nd, "relatedNode", null);
		child1.setRelatedNode(relatedNode);

		assertEquals("Related node should have been set", relatedNode, child1.getRelatedNode());
		assertEquals("root should have correct contents", "root(child1->[relatedNode])", this.root.toString());

		this.root.delete();
		this.nodeDeleted = true;

		assertEquals("Related node should be cleared", null, child1.getRelatedNode());
	}

	public static Test suite() {
		return BaseTestCase.suite(FieldListTest.class);
	}
}

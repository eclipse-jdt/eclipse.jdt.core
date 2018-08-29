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
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.nd.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

import junit.framework.Test;

public class SearchKeyTests extends BaseTestCase {
	private static final String SEARCH_STRING_B = "Yo";
	private static final String SEARCH_STRING_A = "Heyguyswhatshappening";
	private static final String SEARCH_STRING_C = "Shnoogins";

	public static class TestSearchIndex {
		public static final FieldSearchIndex<Element> NICKNAME_INDEX;
		public static final FieldSearchIndex<Element> NAME_INDEX;

		public static final StructDef<TestSearchIndex> type;

		static {
			type = StructDef.create(TestSearchIndex.class);
			NICKNAME_INDEX = FieldSearchIndex.create(type, Element.NICKNAME);
			NAME_INDEX = FieldSearchIndex.create(type, Element.NAME);
			type.done();
		}

		private final long address;
		private Nd nd;
		
		public TestSearchIndex(Nd dom, long address) {
			this.address = address;
			this.nd = dom;
		}

		public static TestSearchIndex getIndex(Nd nd) {
			return new TestSearchIndex(nd, Database.DATA_AREA_OFFSET);
		}

		public Element findName(String searchString) {
			return NAME_INDEX.findFirst(this.nd, this.address,
					FieldSearchIndex.SearchCriteria.create(searchString.toCharArray()));
		}

		public Element findNickName(String searchString) {
			return NICKNAME_INDEX.findFirst(this.nd, this.address,
					FieldSearchIndex.SearchCriteria.create(searchString.toCharArray()));
		}
	}
	
	public static class Element extends NdNode {
		public static final FieldSearchKey<TestSearchIndex> NAME;
		public static final FieldSearchKey<TestSearchIndex> NICKNAME;

		@SuppressWarnings("hiding")
		public static StructDef<Element> type;

		static {
			type = StructDef.create(Element.class, NdNode.type);

			NAME = FieldSearchKey.create(type, TestSearchIndex.NAME_INDEX);
			NICKNAME = FieldSearchKey.create(type, TestSearchIndex.NICKNAME_INDEX);
			type.done();
		}

		public Element(Nd nd, long record) {
			super(nd, record);
		}

		public Element(Nd nd) {
			super(nd);
		}

		public void setName(String searchStringA) {
			NAME.put(getNd(), this.address, searchStringA);
		}

		public void setNickName(String searchStringA) {
			NICKNAME.put(getNd(), this.address, searchStringA);
		}
	}

	private Nd nd;
	private Element elementA;
	private Element elementB;
	private TestSearchIndex index;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		registry.register(0, Element.type.getFactory());
		this.nd = DatabaseTestUtil.createEmptyNd(getName(), registry);
		this.nd.getDB().setExclusiveLock();

		this.elementA = new Element(this.nd);
		this.elementB = new Element(this.nd);
		
		this.index = TestSearchIndex.getIndex(this.nd);
	}

	public static Test suite() {
		return BaseTestCase.suite(SearchKeyTests.class);
	}

	public void testSettingKeyCausesInsertionInSearchIndex() {
		this.elementA.setName(SEARCH_STRING_A);
		this.elementB.setName(SEARCH_STRING_B);

		Element foundElementA = this.index.findName(SEARCH_STRING_A);
		Element foundElementB = this.index.findName(SEARCH_STRING_B);
		Element foundElementC = this.index.findName(SEARCH_STRING_C);

		assertEquals(this.elementA, foundElementA);
		assertEquals(this.elementB, foundElementB);
		assertEquals(null, foundElementC);
	}

	public void testChangingSearchKeyAffectsIndex() {
		this.elementA.setName(SEARCH_STRING_A);

		Element foundElementA = this.index.findName(SEARCH_STRING_A);
		Element foundElementB = this.index.findName(SEARCH_STRING_B);

		assertEquals(null, foundElementB);
		assertEquals(this.elementA, foundElementA);

		this.elementA.setName(SEARCH_STRING_B);

		foundElementA = this.index.findName(SEARCH_STRING_A);
		foundElementB = this.index.findName(SEARCH_STRING_B);

		assertEquals(this.elementA, foundElementB);
		assertEquals(null, foundElementA);
	}

	public void testDeletingElementRemovesFromIndex() {
		this.elementA.setName(SEARCH_STRING_A);
		this.elementA.setNickName(SEARCH_STRING_B);

		assertEquals(this.elementA, this.index.findName(SEARCH_STRING_A));
		assertEquals(this.elementA, this.index.findNickName(SEARCH_STRING_B));

		this.elementA.delete();
		this.nd.processDeletions();
		assertEquals(null, this.index.findName(SEARCH_STRING_A));
		assertEquals(null, this.index.findNickName(SEARCH_STRING_B));
	}
}

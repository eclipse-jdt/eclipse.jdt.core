package org.eclipse.jdt.core.tests.pdom;

import org.eclipse.jdt.core.tests.pdom.util.BaseTestCase;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.PDOMNode;
import org.eclipse.jdt.internal.core.pdom.PDOMNodeTypeRegistry;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchIndex;
import org.eclipse.jdt.internal.core.pdom.field.FieldSearchKey;
import org.eclipse.jdt.internal.core.pdom.field.StructDef;

import junit.framework.Test;

public class SearchKeyTests extends BaseTestCase {
	private static final String SEARCH_STRING_B = "Yo";
	private static final String SEARCH_STRING_A = "Heyguyswhatshappening";
	private static final String SEARCH_STRING_C = "Shnoogins";

	public static class TestSearchIndex {
		public static final FieldSearchIndex<Element> NICKNAME_INDEX;
		public static final FieldSearchIndex<Element> NAME_INDEX;

		@SuppressWarnings("hiding")
		public static final StructDef<TestSearchIndex> type;

		static {
			type = StructDef.create(TestSearchIndex.class);
			NICKNAME_INDEX = FieldSearchIndex.create(type, Element.NICKNAME);
			NAME_INDEX = FieldSearchIndex.create(type, Element.NAME);
			type.done();
		}

		private final long address;
		private PDOM pdom;
		
		public TestSearchIndex(PDOM dom, long address) {
			this.address = address;
			this.pdom = dom;
		}

		public static TestSearchIndex getIndex(PDOM pdom) {
			return new TestSearchIndex(pdom, Database.DATA_AREA);
		}

		public Element findName(String searchString) {
			return NAME_INDEX.findFirst(this.pdom, this.address,
					FieldSearchIndex.SearchCriteria.create(searchString.toCharArray()));
		}

		public Element findNickName(String searchString) {
			return NICKNAME_INDEX.findFirst(this.pdom, this.address,
					FieldSearchIndex.SearchCriteria.create(searchString.toCharArray()));
		}
	}
	
	public static class Element extends PDOMNode {
		public static final FieldSearchKey<TestSearchIndex> NAME;
		public static final FieldSearchKey<TestSearchIndex> NICKNAME;

		@SuppressWarnings("hiding")
		public static StructDef<Element> type;

		static {
			type = StructDef.create(Element.class, PDOMNode.type);

			NAME = FieldSearchKey.create(type, TestSearchIndex.NAME_INDEX);
			NICKNAME = FieldSearchKey.create(type, TestSearchIndex.NICKNAME_INDEX);
			type.done();
		}

		public Element(PDOM pdom, long record) {
			super(pdom, record);
		}

		public Element(PDOM pdom) {
			super(pdom);
		}

		public void setName(String searchStringA) {
			NAME.put(getPDOM(), this.address, searchStringA);
		}

		public void setNickName(String searchStringA) {
			NICKNAME.put(getPDOM(), this.address, searchStringA);
		}
	}

	private PDOM pdom;
	private Element elementA;
	private Element elementB;
	private TestSearchIndex index;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		PDOMNodeTypeRegistry<PDOMNode> registry = new PDOMNodeTypeRegistry<>();
		registry.register(0, Element.type.getFactory());
		this.pdom = DatabaseTestUtil.createEmptyPdom(getName(), registry);
		this.pdom.getDB().setExclusiveLock();

		this.elementA = new Element(this.pdom);
		this.elementB = new Element(this.pdom);
		
		this.index = TestSearchIndex.getIndex(this.pdom);
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

		assertEquals(null, this.index.findName(SEARCH_STRING_A));
		assertEquals(null, this.index.findNickName(SEARCH_STRING_B));
	}
}

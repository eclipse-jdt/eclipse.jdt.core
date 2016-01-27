/*******************************************************************************
 * Copyright (c) 2015 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.field;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.pdom.ITypeFactory;
import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.db.BTree;
import org.eclipse.jdt.internal.core.pdom.db.IBTreeComparator;
import org.eclipse.jdt.internal.core.pdom.db.IBTreeVisitor;
import org.eclipse.jdt.internal.core.pdom.db.IString;
import org.eclipse.jdt.internal.core.pdom.db.IndexException;

/**
 * Declares a field representing a case-insensitive search tree over elements which are a subtype of PDOMNode.
 * This field may only ever  
 * @since 3.12
 */
public class FieldSearchIndex<T extends NdNode> implements IField, IDestructableField {
	private int offset;
	private final ITypeFactory<BTree> btreeFactory;
	FieldSearchKey<?> searchKey;
	private static IResultRank anything = new IResultRank() {
		@Override
		public long getRank(Nd pdom, long address) {
			return 1;
		}
	};
 
	public static final class SearchCriteria {
		private boolean matchCase = true;
		private boolean isPrefix = false;
		private char[] searchString;
		private short requiredNodeType = -1;
		private long requiredParentNodeAddress;
		private boolean matchingParentNodeAddress = false;

		private SearchCriteria(char[] searchString) {
			this.searchString = searchString;
		}

		public static SearchCriteria create(String searchString) {
			return create(searchString.toCharArray());
		}

		public static SearchCriteria create(char[] searchString) {
			return new SearchCriteria(searchString);
		}

		public SearchCriteria requireNodeType(short type) {
			this.requiredNodeType = type;
			return this;
		}

		public SearchCriteria allowAnyNodeType() {
			this.requiredNodeType = -1;
			return this;
		}

		public SearchCriteria matchCase(boolean match) {
			this.matchCase = match;
			return this;
		}

		public SearchCriteria prefix(boolean isPrefixSearch) {
			this.isPrefix = isPrefixSearch;
			return this;
		}
//
//		public SearchCriteria requireParentNode(long parentNameAddress) {
//			this.requiredParentNodeAddress = parentNameAddress;
//			return this;
//		}

		public boolean isMatchingParentNodeAddress() {
			return this.matchingParentNodeAddress;
		}

		public boolean isMatchingCase() {
			return this.matchCase;
		}

		public boolean isPrefixSearch() {
			return this.isPrefix;
		}

		public char[] getSearchString() {
			return this.searchString;
		}
//
//		public long getRequiredParentAddress() {
//			return this.requiredParentNodeAddress;
//		}

		public boolean acceptsNodeType(short nodeType) {
			return this.requiredNodeType == -1 || this.requiredNodeType == nodeType;
		}

		public boolean requiresSpecificNodeType() {
			return this.requiredNodeType != -1;
		}

//		public SearchCriteria requireParentNode(PDOMNamedNode parentName) {
//			return requireParentNode(PDOMNode.addressOf(parentName));
//		}
	}

	public static interface IResultRank {
		public long getRank(Nd pdom, long address);
	}

	private abstract class SearchCriteriaToBtreeVisitorAdapter implements IBTreeVisitor {
		private final SearchCriteria searchCriteria;
		private final Nd pdom;

		private SearchCriteriaToBtreeVisitorAdapter(SearchCriteria searchCriteria, Nd pdom) {
			this.searchCriteria = searchCriteria;
			this.pdom = pdom;
		}

		@Override
		public int compare(long record) throws IndexException {
			IString key = FieldSearchIndex.this.searchKey.get(this.pdom, record);

			if (this.searchCriteria.isPrefixSearch()) {
				return key.comparePrefix(this.searchCriteria.getSearchString(), false);
			} else {
				return key.compareCompatibleWithIgnoreCase(this.searchCriteria.getSearchString());
			}
		}

		@Override
		public boolean visit(long record) throws IndexException {
			if (this.searchCriteria.requiresSpecificNodeType()) {
				short nodeType = NdNode.NODE_TYPE.get(this.pdom, record);

				if (!this.searchCriteria.acceptsNodeType(nodeType)) {
					return true;
				}
			}

//			long parent = PDOMNamedNode.PARENT.getAddress(this.pdom, record);
//			if (parent != this.searchCriteria.getRequiredParentAddress()) {
//				return true;
//			}

			IString key = FieldSearchIndex.this.searchKey.get(this.pdom, record);

			if (this.searchCriteria.isMatchingCase()) {
				if (this.searchCriteria.isPrefixSearch()) {
					if (key.comparePrefix(this.searchCriteria.getSearchString(), true) != 0) {
						return true;
					}
				} else {
					if (key.compare(this.searchCriteria.getSearchString(), true) != 0) {
						return true;
					}
				}
			}

			acceptResult(record);
			return true;
		}

		protected abstract void acceptResult(long record);
	}

	private FieldSearchIndex(FieldSearchKey<?> searchKey) {
		this.btreeFactory = BTree.getFactory(new IBTreeComparator() {
			@Override
			public int compare(Nd pdom, long record1, long record2) {
				IString key1 = FieldSearchIndex.this.searchKey.get(pdom, record1);
				IString key2 = FieldSearchIndex.this.searchKey.get(pdom, record2);

				int cmp = key1.compareCompatibleWithIgnoreCase(key2);
				if (cmp == 0) {
					cmp = Long.signum(record1 - record2);
				}

				return cmp;
			}
		});

		if (searchKey != null) {
			if (searchKey.searchIndex != null && searchKey.searchIndex != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldSearchIndex referring to a search key that " //$NON-NLS-1$
					+ "is already in use by a different index"); //$NON-NLS-1$
			}
			searchKey.searchIndex = this;
		}
		this.searchKey = searchKey;
	}

	public static <T extends NdNode, B> FieldSearchIndex<T> create(StructDef<B> builder,
			final FieldSearchKey<B> searchKey) {

		FieldSearchIndex<T> result = new FieldSearchIndex<T>(searchKey);

		builder.add(result);
		builder.addDestructableField(result);

		return result;
	}

	public BTree get(Nd pdom, long record) {
		return this.btreeFactory.create(pdom, record + this.offset);
	}

	@Override
	public void destruct(Nd pdom, long record) {
		this.btreeFactory.destruct(pdom, record);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return this.btreeFactory.getRecordSize();
	}

	public T findFirst(final Nd pdom, long address, final SearchCriteria searchCriteria) {
		return findBest(pdom, address, searchCriteria, anything);
	}

	public T findBest(final Nd pdom, long address, final SearchCriteria searchCriteria, final IResultRank rankFunction) {
		final long[] resultRank = new long[1];
		final long[] result = new long[1];
		get(pdom, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, pdom) {
			@Override
			protected void acceptResult(long record) {
				long rank = rankFunction.getRank(pdom, record);
				if (rank >= resultRank[0]) {
					resultRank[0] = rank;
					result[0] = record;
				}
			}
		});

		if (result[0] == 0) {
			return null;
		}
		return (T)NdNode.load(pdom, result[0]);
	}

	public List<T> findAll(final Nd pdom, long address, final SearchCriteria searchCriteria) {
		final List<T> result = new ArrayList<T>();
		get(pdom, address).accept(new SearchCriteriaToBtreeVisitorAdapter(searchCriteria, pdom) {
			@Override
			protected void acceptResult(long record) {
				result.add((T)NdNode.load(pdom, record));
			}
		});

		return result;
	}
}

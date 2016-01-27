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
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.BTree;
import org.eclipse.jdt.internal.core.nd.db.Database;
import org.eclipse.jdt.internal.core.nd.db.IString;

/**
 * Represents a search key into a global search index.
 * @since 3.12
 */
public class FieldSearchKey<T> implements IField, IDestructableField {
	private final FieldString key;
	FieldSearchIndex<?> searchIndex;

	private FieldSearchKey(FieldSearchIndex<?> searchIndex) {
		this.key = new FieldString();

		if (searchIndex != null) {
			if (searchIndex.searchKey != null && searchIndex.searchKey != this) {
				throw new IllegalArgumentException(
					"Attempted to construct a FieldSearchKey referring to a search index that is " //$NON-NLS-1$
					+ "already in use by a different key"); //$NON-NLS-1$
			}
			searchIndex.searchKey = this;
		}
		this.searchIndex = searchIndex;
	}

	/**
	 * Creates a search key attribute in the given struct which stores an entry in the given global search index
	 *
	 * @param builder
	 * @param searchIndex
	 * @return
	 */
	public static <T, B extends NdNode> FieldSearchKey<T> create(StructDef<B> builder,
			FieldSearchIndex<B> searchIndex) {
		FieldSearchKey<T> result = new FieldSearchKey<T>(searchIndex);

		builder.add(result);
		builder.addDestructableField(result);

		return result;
	}

	public void put(Nd pdom, long address, String newString) {
		put(pdom, address, newString.toCharArray());
	}

	public void put(Nd pdom, long address, char[] newString) {
		BTree btree = this.searchIndex.get(pdom, Database.DATA_AREA);
		btree.delete(address);

		this.key.put(pdom, address, newString);

		btree.insert(address);
	}

	public IString get(Nd pdom, long address) {
		return this.key.get(pdom, address);
	}

	@Override
	public void destruct(Nd pdom, long address) {
		// Remove this entry from the search index
		this.searchIndex.get(pdom, Database.DATA_AREA).delete(address);

		this.key.destruct(pdom, address);
	}

	@Override
	public void setOffset(int offset) {
		this.key.setOffset(offset);
	}

	@Override
	public int getRecordSize() {
		return FieldString.RECORD_SIZE;
	}
}

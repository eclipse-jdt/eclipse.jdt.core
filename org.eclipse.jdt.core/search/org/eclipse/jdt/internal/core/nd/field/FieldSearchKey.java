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
import org.eclipse.jdt.internal.core.nd.db.EmptyString;
import org.eclipse.jdt.internal.core.nd.db.IString;

/**
 * Represents a search key into a global search index.
 * @since 3.12
 */
public class FieldSearchKey<T> implements IField, IDestructableField {
	private int offset;
	FieldSearchIndex<?> searchIndex;

	private FieldSearchKey(FieldSearchIndex<?> searchIndex) {
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

	/**
	 * Sets the value of the key and inserts it into the index if it is not already present
	 */
	public void put(Nd pdom, long address, char[] newString) {
		cleanup(pdom, address);

		Database db = pdom.getDB();
		BTree btree = this.searchIndex.get(pdom, Database.DATA_AREA);
		db.putRecPtr(address + this.offset, db.newString(newString).getRecord());
		btree.insert(address);
	}

	public IString get(Nd pdom, long address) {
		Database db = pdom.getDB();
		long namerec = db.getRecPtr(address + this.offset);

		if (namerec == 0) {
			return EmptyString.create();
		}
		return db.getString(namerec);
	}

	@Override
	public void destruct(Nd pdom, long address) {
		cleanup(pdom, address);
	}

	private void cleanup(Nd pdom, long address) {
		boolean isInIndex = isInIndex(pdom, address);

		if (isInIndex) {
			// Remove this entry from the search index
			this.searchIndex.get(pdom, Database.DATA_AREA).delete(address);

			get(pdom, address).delete();
			pdom.getDB().putRecPtr(address + this.offset, 0);
		}
	}

	/**
	 * Clears this key and removes it from the search index
	 */
	public void removeFromIndex(Nd nd, long address) {
		cleanup(nd, address);
	}

	/**
	 * Returns true iff this key is currently in the index
	 */
	public boolean isInIndex(Nd pdom, long address) {
		long fieldAddress = address + this.offset;
		Database db = pdom.getDB();
		long namerec = db.getRecPtr(fieldAddress);

		boolean isInIndex = namerec != 0;
		return isInIndex;
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return FieldString.RECORD_SIZE;
	}
}

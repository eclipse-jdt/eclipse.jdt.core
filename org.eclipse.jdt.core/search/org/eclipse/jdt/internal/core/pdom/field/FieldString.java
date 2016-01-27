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

import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.db.Database;
import org.eclipse.jdt.internal.core.pdom.db.EmptyString;
import org.eclipse.jdt.internal.core.pdom.db.IString;

/**
 * @since 3.12
 */
public class FieldString implements IDestructableField, IField {
	public static final int RECORD_SIZE = Database.STRING_SIZE;
	private int offset;

	public FieldString() {
	}

	public IString get(Nd pdom, long address) {
		Database db = pdom.getDB();
		long namerec = db.getRecPtr(address + this.offset);

		if (namerec == 0) {
			return EmptyString.create();
		}
		return db.getString(namerec);
	}

	public void put(Nd pdom, long address, char[] newString) {
		final Database db= pdom.getDB();
		IString name= get(pdom, address);
		if (name.compare(newString, true) != 0) {
			name.delete();
			if (newString != null && newString.length > 0) {
				db.putRecPtr(address + this.offset, db.newString(newString).getRecord());
			} else {
				db.putRecPtr(address + this.offset, 0);
			}
		}
	}

	public void put(Nd pdom, long address, String newString) {
		put(pdom, address, newString.toCharArray());
	}

	public void destruct(Nd pdom, long address) {
		get(pdom, address).delete();
		pdom.getDB().putRecPtr(address + this.offset, 0);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return RECORD_SIZE;
	}
}

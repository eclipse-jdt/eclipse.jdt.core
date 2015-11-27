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

import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.db.Database;

/**
 * Declares a PDOM field of type double.
 * 
 * @since 3.12
 */
public class FieldDouble implements IField {
	private int offset;

	public FieldDouble() {
	}

	public double get(PDOM pdom, long record) {
		Database db = pdom.getDB();
		return db.getDouble(record + this.offset);
	}

	public void put(PDOM pdom, long record, double newValue) {
		pdom.getDB().putDouble(record + this.offset, newValue);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.DOUBLE_SIZE;
	}
}

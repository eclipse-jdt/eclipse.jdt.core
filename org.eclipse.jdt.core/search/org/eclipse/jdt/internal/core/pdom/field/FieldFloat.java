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

/**
 * Declares a PDOM field of type float.
 * 
 * @since 3.12
 */
public class FieldFloat implements IField {
	private int offset;

	public FieldFloat() {
	}

	public float get(Nd pdom, long address) {
		Database db = pdom.getDB();
		return db.getFloat(address + this.offset);
	}

	public void put(Nd pdom, long address, float newValue) {
		pdom.getDB().putFloat(address + this.offset, newValue);
	}

	@Override
	public void setOffset(int offset) {
		this.offset = offset;
	}

	@Override
	public int getRecordSize() {
		return Database.FLOAT_SIZE;
	}
}

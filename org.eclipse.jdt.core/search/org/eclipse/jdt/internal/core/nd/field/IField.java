/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.nd.field;

import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * Represents a single field of a struct in the {@link Database}. Holds metadata for that field
 * and permits laziy initialization of the field offset.
 */
public interface IField {
	/**
	 * Sets the field offset (bytes from the start of the struct). This is invoked some time after field construction,
	 * after the sizes of all preceeding fields are known.
	 */
	void setOffset(int offset);
	/**
	 * Returns the size of the field, in bytes.
	 */
	int getRecordSize();

	/**
	 * Returns the name of the field. This is mainly used for error messages, debug output, and diagnostic tools.
	 * Meant to be programmer-readable but not user-readable.
	 */
	String getFieldName();

	/**
	 * Returns the field offset, in bytes from the start of the struct.
	 */
	int getOffset();
}

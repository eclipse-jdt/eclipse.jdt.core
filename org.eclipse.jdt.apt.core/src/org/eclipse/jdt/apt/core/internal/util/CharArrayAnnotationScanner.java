/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.IOException;


public class CharArrayAnnotationScanner extends AnnotationScanner {

	private final char[] _data;
	private int index = 0;

	public CharArrayAnnotationScanner(final char[] data) {
		_data = data;
	}

	@Override
	protected int getNext() throws IOException {
		if (index == _data.length - 1)
			return -1;
		return _data[index++];
	}

}

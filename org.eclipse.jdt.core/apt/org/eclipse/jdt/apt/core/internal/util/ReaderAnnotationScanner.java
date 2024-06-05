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
import java.io.InputStreamReader;


public class ReaderAnnotationScanner extends AnnotationScanner {

	private final InputStreamReader _reader;

	public ReaderAnnotationScanner(final InputStreamReader reader) {
		_reader = reader;
	}

	@Override
	protected int getNext() throws IOException {
		return _reader.read();
	}
}

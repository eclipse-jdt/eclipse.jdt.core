/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.index;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.internal.core.index.impl.Index;

public class IndexFactory {

	public static IIndex newIndex(File indexDirectory) throws IOException {
		return new Index(indexDirectory);
	}
	public static IIndex newIndex(File indexDirectory, String indexName) throws IOException {
		return new Index(indexDirectory, indexName);
	}
	public static IIndex newIndex(String indexName) throws IOException {
		return new Index(indexName);
	}
	public static IIndex newIndex(String indexName, String toString) throws IOException {
		return new Index(indexName, toString);
	}
}

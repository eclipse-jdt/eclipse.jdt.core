/*******************************************************************************
 * Copyright (c) 2015, 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd.indexer;

import java.io.File;

import org.eclipse.jdt.core.tests.nd.DatabaseTestUtil;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;

public class JavaIndexTestUtil {
	public static JavaIndex createTempIndex(String id) {
		File dbName = DatabaseTestUtil.getTempDbName(id);
		return JavaIndex.getIndex(JavaIndex.createNd(dbName, new ChunkCache()));
	}
}

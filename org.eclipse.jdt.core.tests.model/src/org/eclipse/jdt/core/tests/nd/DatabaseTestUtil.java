/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.tests.Activator;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.nd.db.ChunkCache;
import org.eclipse.jdt.internal.core.nd.db.Database;

/**
 * 
 */
public class DatabaseTestUtil {

	public static IPath getTestDir() {
		Plugin plugin = Activator.getInstance();
		
		IPath path = plugin.getStateLocation().append("tests/");
		File file = path.toFile();
		if (!file.exists())
			file.mkdir();
		return path;
	}

	public static File getTempDbName(String testName) {
		return DatabaseTestUtil.getTestDir().append(testName + System.currentTimeMillis() + ".dat").toFile();
	}

	/**
	 * Creates an empty {@link Nd} with an empty type registry and randomly-named
	 * database for the given test name
	 * 
	 * @param testName
	 * @return the new {@link Nd}
	 */
	public static Nd createEmptyNd(String testName) {
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		return new Nd(DatabaseTestUtil.getTempDbName(testName), new ChunkCache(), registry, 0, 0, 0);
	}

	public static Nd createEmptyNd(String testName, NdNodeTypeRegistry<NdNode> registry) {
		return new Nd(DatabaseTestUtil.getTempDbName(testName), new ChunkCache(), registry, 0, 0, 0);
	}

	static Nd createWithoutNodeRegistry(String testName) {
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		Nd tempNd = new Nd(getTempDbName(testName), new ChunkCache(), registry, 0, 100,
				DatabaseTestUtil.CURRENT_VERSION);
		return tempNd;
	}

	static final int CURRENT_VERSION = 10;

	static void deleteDatabase(Database db) {
		db.close();
		if (!db.getLocation().delete()) {
			db.getLocation().deleteOnExit();
		}
	}
}

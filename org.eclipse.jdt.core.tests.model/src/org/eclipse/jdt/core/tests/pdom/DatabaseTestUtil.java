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
package org.eclipse.jdt.core.tests.pdom;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.tests.Activator;
import org.eclipse.jdt.internal.core.pdom.Nd;
import org.eclipse.jdt.internal.core.pdom.NdNode;
import org.eclipse.jdt.internal.core.pdom.NdNodeTypeRegistry;
import org.eclipse.jdt.internal.core.pdom.db.ChunkCache;

import java.io.File;

/**
 * 
 */
public class DatabaseTestUtil {

	/**
	 * @param plugin
	 * @return
	 */
	public static IPath getTestDir() {
		Plugin plugin = Activator.getInstance();
		
		IPath path = plugin.getStateLocation().append("tests/");
		File file = path.toFile();
		if (!file.exists())
			file.mkdir();
		return path;
	}

	/**
	 * @param testName
	 * @return
	 */
	public static File getTempDbName(String testName) {
		return DatabaseTestUtil.getTestDir().append(testName + System.currentTimeMillis() + ".dat").toFile();
	}

	/**
	 * Creates an empty PDOM with an empty type registry and randomly-named
	 * database for the given test name
	 * 
	 * @param testName
	 * @return the new PDOM
	 */
	public static Nd createEmptyPdom(String testName) {
		NdNodeTypeRegistry<NdNode> registry = new NdNodeTypeRegistry<>();
		return new Nd(DatabaseTestUtil.getTempDbName(testName), new ChunkCache(), registry, 0, 0, 0);
	}

	public static Nd createEmptyPdom(String testName, NdNodeTypeRegistry<NdNode> registry) {
		return new Nd(DatabaseTestUtil.getTempDbName(testName), new ChunkCache(), registry, 0, 0, 0);
	}
}

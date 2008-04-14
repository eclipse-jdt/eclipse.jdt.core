/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.File;
import java.io.IOException;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;

class ForwardingStandardJavaFileManager<T extends StandardJavaFileManager> extends ForwardingJavaFileManager<T> implements StandardJavaFileManager {
	public ForwardingStandardJavaFileManager(T javaFileManager) {
		super(javaFileManager);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjects(File... files) {
		return this.fileManager.getJavaFileObjects(files);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjects(
			String... names) {
		return this.fileManager.getJavaFileObjects(names);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromFiles(
			Iterable<? extends File> files) {
		return this.fileManager.getJavaFileObjectsFromFiles(files);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromStrings(
			Iterable<String> names) {
		return this.fileManager.getJavaFileObjectsFromStrings(names);
	}

	@Override
	public Iterable<? extends File> getLocation(Location location) {
		return this.fileManager.getLocation(location);
	}

	@Override
	public void setLocation(Location location, Iterable<? extends File> path)
			throws IOException {
		this.fileManager.setLocation(location, path);		
	}
}

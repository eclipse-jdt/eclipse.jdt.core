/*******************************************************************************
 * Copyright (c) 2008, 2017 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import javax.tools.FileObject;
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

	@Override
	public Path asPath(FileObject arg0) {
		return this.fileManager.asPath(arg0);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjects(Path... arg0) {
		return this.fileManager.getJavaFileObjects(arg0);
	}

	@Override
	public Iterable<? extends JavaFileObject> getJavaFileObjectsFromPaths(Iterable<? extends Path> arg0) {
		return this.fileManager.getJavaFileObjectsFromPaths(arg0);
	}

	@Override
	public Iterable<? extends Path> getLocationAsPaths(Location arg0) {
		return this.fileManager.getLocationAsPaths(arg0);
	}

	@Override
	public void setLocationForModule(Location arg0, String arg1, Collection<? extends Path> arg2) throws IOException {
		this.fileManager.setLocationForModule(arg0, arg1, arg2);
	}

	@Override
	public void setLocationFromPaths(Location arg0, Collection<? extends Path> arg1) throws IOException {
		this.fileManager.setLocationFromPaths(arg0, arg1);
	}
}

/*******************************************************************************
* Copyright (c) 2024 Red Hat, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.FileSystem;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.tools.JavaFileManager;

import org.eclipse.core.runtime.ILog;

import com.sun.tools.javac.api.ClientCodeWrapper;
import com.sun.tools.javac.file.FSInfo;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Context.Factory;

/// An implementation of [JavacFileManager] suitable for local parsing/resolution.
/// It allows to reuse the filesystems for the referenced jars so they're not duplicated,
/// 
/// Note that the main goal is to override the [#close()] method so it does _not_ close
/// the underlying ZipFileSystem which might still be in use. 
public class CachingJarsJavaFileManager extends JavacFileManager {

	private static final ZipFileSystemProviderWithCache zipCache = new ZipFileSystemProviderWithCache();

	/**
	 * Register a Context.Factory to create a JavacFileManager.
	 */
	public static void preRegister(Context context) {
		context.put(FSInfo.class, new FSInfo() {
			@Override
			public synchronized java.nio.file.spi.FileSystemProvider getJarFSProvider() {
				return CachingJarsJavaFileManager.zipCache;
			}
		});
		context.put(ClientCodeWrapper.class, new ClientCodeWrapper(context) {
				@Override
				protected boolean isTrusted(Object o) {
					return super.isTrusted(o) || o.getClass().getClassLoader() == CachingJarsJavaFileManager.class.getClassLoader();
				}
			});
		context.put(JavaFileManager.class, (Factory<JavaFileManager>)c -> new CachingJarsJavaFileManager(c));
	}

	/**
	 * Create a JavacFileManager using a given context, optionally registering
	 * it as the JavaFileManager for that context.
	 */
	public CachingJarsJavaFileManager(Context context) {
		super(context, true, null);
		zipCache.register(this);
	}

	@Override
	public void close() throws IOException {
		// closes as much as possible, except the containers as they have a
		// ref to the shared ZipFileSystem that we don't want to close
		// To improve: close non-ArchiveContainer containers
		flush();
		locations.close();
		resetOutputFilesWritten();
		zipCache.closeFileManager(System.identityHashCode(this));
	}

	Collection<FileSystem> listFilesystemsInArchiveContainers() {
		Set<FileSystem> res = new HashSet<>();
		try {
			Field containerField = JavacFileManager.class.getDeclaredField("containers");
			containerField.setAccessible(true);
			if (containerField.get(this) instanceof Map containersMap) {
				for (Object o : containersMap.values()) {
					if (o.getClass().getName().equals(JavacFileManager.class.getName() + "$ArchiveContainer")) {
						Field filesystemField = o.getClass().getDeclaredField("fileSystem");
						filesystemField.setAccessible(true);
						if (filesystemField.get(o) instanceof FileSystem fs) {
							res.add(fs);
						}
					}
				}
			}
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return res;
	}
}

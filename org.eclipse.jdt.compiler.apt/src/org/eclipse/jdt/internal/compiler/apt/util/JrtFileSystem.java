/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipException;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.env.IModule;

public class JrtFileSystem extends Archive {

	private static URI JRT_URI = URI.create("jrt:/"); //$NON-NLS-1$
	
	private static final String BOOT_MODULE = "jrt-fs.jar"; //$NON-NLS-1$
	
	private static final String MODULES_SUBDIR = "/modules"; //$NON-NLS-1$
	
	private static final String DEFAULT_PACKAGE = ""; //$NON-NLS-1$

	private Set<String> typesCache = null;
	
	public JrtFileSystem(File file) throws ZipException, IOException {
		this.file = file;
		initialize();
	}
	
	private void initialize() throws IOException {
		// initialize packages
		this.packagesCache = new Hashtable<>();
		this.typesCache = new HashSet<>();
		if (!this.file.getName().equals(BOOT_MODULE)) return;
		java.nio.file.FileSystem fs = FileSystems.getFileSystem(JRT_URI);
		Iterable<java.nio.file.Path> roots = fs.getRootDirectories();
		for (java.nio.file.Path path : roots) {
			try (DirectoryStream<java.nio.file.Path> stream = Files.newDirectoryStream(path)) {
				for (final java.nio.file.Path subdir: stream) {
					if (subdir.toString().equals(MODULES_SUBDIR)) {
						Files.walkFileTree(subdir, new FileVisitor<java.nio.file.Path>() {

							@Override
							public FileVisitResult preVisitDirectory(java.nio.file.Path entry, BasicFileAttributes attrs)
									throws IOException {
								int count = entry.getNameCount();
								if (count < 2) return FileVisitResult.CONTINUE;
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFile(java.nio.file.Path entry, BasicFileAttributes attrs) throws IOException {
								int count = entry.getNameCount();
								if (entry == subdir || count < 3) return FileVisitResult.CONTINUE;
								if (count == 3) {
									cacheTypes(DEFAULT_PACKAGE, entry.getName(2).toString(), entry.getName(1).toString());
								} else {
									cacheTypes(entry.subpath(2, count - 1).toString(), 
										entry.getName(count - 1).toString(), entry.getName(1).toString());
								}
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult visitFileFailed(java.nio.file.Path entry, IOException exc) throws IOException {
								return FileVisitResult.CONTINUE;
							}

							@Override
							public FileVisitResult postVisitDirectory(java.nio.file.Path entry, IOException exc) throws IOException {
								return FileVisitResult.CONTINUE;
							}
						});
					}
				}
			} catch (Exception e) {
				throw new IOException(e.getMessage());
			}
		}
	}
	
	public ArchiveFileObject getArchiveFileObject(String fileName, Charset charset) {
		return new JrtFileObject(this.file, fileName, null, charset);
	}
	
	@Override
	public boolean contains(String entryName) {
		return this.typesCache.contains(entryName);
	}

	protected void cacheTypes(String packageName, String typeName, String module) {
		int length = packageName.length();
		if (length > 0 && packageName.charAt(packageName.length() - 1) != '/') {
			packageName = packageName + '/'; 
		}
		ArrayList<String[]> types = this.packagesCache.get(packageName);
		if (typeName == null) return;
		if (types == null) {
			types = new ArrayList<>();
			types.add(new String[]{typeName, module});
			this.packagesCache.put(packageName, types);
		} else {
			types.add(new String[]{typeName, module});
		}
		this.typesCache.add(packageName + typeName);
	}
	@Override
	public List<String[]> getTypes(String packageName) {
		// package name is expected to ends with '/'
		if (this.packagesCache == null) {
			try {
				this.initialize();
			} catch(IOException e) {
				return Collections.<String[]>emptyList();
			}
		}
		return this.packagesCache.get(packageName);
	}
	
	@Override
	public String toString() {
		return "JRT: " + (this.file == null ? "UNKNOWN_ARCHIVE" : this.file.getAbsolutePath()); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	class JrtFileObject extends ArchiveFileObject {
		IModule module = null;
		private JrtFileObject(File file, String fileName, IModule module, Charset charset) {
			super(file, fileName, charset);
			this.module = module;
		}

		@Override
		protected void finalize() throws Throwable {
			// Nothing to do here
		}

		@Override
		protected ClassFileReader getClassReader() {
			ClassFileReader reader = null;
			try {
				reader = ClassFileReader.readFromJrt(this.file, this.entryName, this.module);
			} catch (ClassFormatException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return reader;
		}
		

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#getCharContent(boolean)
		 */
		@Override
		public CharSequence getCharContent(boolean ignoreEncodingErrors) throws IOException {
			try {
				return Util.getCharContents(this, ignoreEncodingErrors,
						org.eclipse.jdt.internal.compiler.util.JRTUtil.getClassfileContent(this.file, this.entryName, new String(this.module.name())),
						this.charset.name());
			} catch (ClassFormatException e) {
				e.printStackTrace();
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#getLastModified()
		 */
		@Override
		public long getLastModified() {
			return 0;
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#getName()
		 */
		@Override
		public String getName() {
			return this.entryName;
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#openInputStream()
		 */
		@Override
		public InputStream openInputStream() throws IOException {
			return org.eclipse.jdt.internal.compiler.util.JRTUtil.getContentFromJrt(this.file, this.entryName, null);
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#openOutputStream()
		 */
		@Override
		public OutputStream openOutputStream() throws IOException {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#openReader(boolean)
		 */
		@Override
		public Reader openReader(boolean ignoreEncodingErrors) throws IOException {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#openWriter()
		 */
		@Override
		public Writer openWriter() throws IOException {
			throw new UnsupportedOperationException();
		}

		/* (non-Javadoc)
		 * @see javax.tools.FileObject#toUri()
		 */
		@Override
		public URI toUri() {
			try {
				return new URI("JRT:" + this.file.toURI().getPath() + "!" + this.entryName); //$NON-NLS-1$//$NON-NLS-2$
			} catch (URISyntaxException e) {
				return null;
			}
		}


		@Override
		public String toString() {
			return this.file.getAbsolutePath() + "[" + this.entryName + "]";//$NON-NLS-1$//$NON-NLS-2$
		}	
	}
}

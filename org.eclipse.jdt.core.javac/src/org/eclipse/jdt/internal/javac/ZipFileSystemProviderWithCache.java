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
import java.lang.ref.Cleaner;
import java.net.URI;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.AccessMode;
import java.nio.file.CopyOption;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileStore;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.FileAttributeView;
import java.nio.file.attribute.FileTime;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import org.eclipse.core.runtime.ILog;

import com.sun.tools.javac.file.CacheFSInfo;
import com.sun.tools.javac.file.JavacFileManager;

import sun.nio.ch.FileChannelImpl;

/// A filesystem provider for Zip/Jar files that is capable of caching content so it
/// can be reused by multiple contexts (as long as the cached objects don't get close
/// while still in use).
///
/// Caveats:
/// - cached objects are currenlty never released (some commented code attempts it, but
///   it requires the consumers to declare they consume a particular path/filesystem).
/// - This is currently only hooked for the main JavacFileManager. Some other filemanagers
///   are used (eg `JDKPlatformProvider.getFileManager()`) which don't take advantage of caching
public class ZipFileSystemProviderWithCache extends FileSystemProvider {

	private final Cleaner cleaner = Cleaner.create();
	private final Map<Path, FileSystem> cachedFilesystems = new HashMap<>();
	private final Map<FileSystem, FileTime> lastModificationOfCache = new HashMap<>();
	private final FileSystemProvider delegate = new CacheFSInfo().getJarFSProvider();
	/// a Set of int for output of `System.identityHashCode()` for given
	/// active {@link JavacFileManager}s.
	/// We actually store `int` instead of an actual reference to allow the underlying
	/// file managers to be garbage collected (and closed).
	private final Set<Integer> fileManagersIdentitieis = new HashSet<>();
	
	public void closeFileManager(int cachingJarsJavaFileManagerIdentityHashCode) {
		// We cannot keep a reference to JavaFileManager easily or it create leak in the context
		// we instead keep refs to ids
		
		// One important limitation is that we can only clear the cache when we know that
		// no relevant filesystem is still in use (called `.close()` or got GCed).
		// Ideally, we would have finer grain cache that would clear the unused filesystems
		// according to the filemanagers still in use. But as we can't keep ref on FileManagers
		// to not block GC, that seems impossible.
		Set<FileSystem> toClear = new HashSet<>();
		synchronized (this) {
			this.fileManagersIdentitieis.remove(cachingJarsJavaFileManagerIdentityHashCode);
			if (this.fileManagersIdentitieis.isEmpty()) {
				toClear.addAll(lastModificationOfCache.keySet());
				toClear.addAll(cachedFilesystems.values());
				lastModificationOfCache.clear();
				cachedFilesystems.clear();
				// GC can then happen
			}
		}
		CompletableFuture.runAsync(() -> toClear.forEach(fs -> {
			try {
				fs.close();
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}));
	}

	@Override
	public String getScheme() {
		return delegate.getScheme();
	}

	@Override
	public FileSystem newFileSystem(URI uri, Map<String, ?> env) throws IOException {
		return newFileSystem(getPath(uri), env);
	}
	@Override
	public FileSystem newFileSystem(Path path, Map<String,?> env) throws IOException {
		synchronized (this) {
			var cached = getCachedFileSystem(path);
			if (cached != null) {
				return cached;
			}
			var lastMod = Files.getLastModifiedTime(path);
			var res = delegate.newFileSystem(path, env);
			this.cachedFilesystems.put(path, res);
			this.lastModificationOfCache.put(res, lastMod);
			makeFileSystemUninterruptible(res);
			return res;
		}
	}

	static void makeFileSystemUninterruptible(FileSystem res) {
		try {
			// workaround to make the underlying work of the ZipFileSystem
			// resistant to thread abortion. Without it, the zips become
			// useless when a consumer thread aborts
			var zipFileSystemClass = res.getClass();
			var chField = zipFileSystemClass.getDeclaredField("ch");
			chField.setAccessible(true);
			if (chField.get(res) instanceof FileChannelImpl fileChannel) {
				fileChannel.setUninterruptible();
			}
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}
	
	@Override
	public FileSystem getFileSystem(URI uri) {
		var res = getCachedFileSystem(getPath(uri));
		if (res == null) {
			res = delegate.getFileSystem(uri);
		}
		return res;
	}
	/// Get the cached FileSystem for given path
	/// @param file the path of the archive
	/// @return the cache filesystem, or `null` is filesystem
	///         was not requested yet, or if the cached filesystem
	///         is outdated and not suitable for usage any more.
	private FileSystem getCachedFileSystem(Path file) { 
		var cached = this.cachedFilesystems.get(file);
		if (cached == null) {
			return null;
		}
		var cacheLastMod = this.lastModificationOfCache.get(cached);
		FileTime lastMod;
		try {
			lastMod = Files.getLastModifiedTime(file);
		} catch (IOException e) {
			return null;
		}
		if (lastMod.compareTo(cacheLastMod) > 0) { // file changed, cache is not valid
			// create and use a new container/filesystem
			return null;
		}
		return cached;
	}

	@Override
	public Path getPath(URI uri) {
		return delegate.getPath(uri);
	}

	@Override
	public SeekableByteChannel newByteChannel(Path path, Set<? extends OpenOption> options, FileAttribute<?>... attrs)
			throws IOException {
		return delegate.newByteChannel(path, options, attrs);
	}

	@Override
	public DirectoryStream<Path> newDirectoryStream(Path dir, Filter<? super Path> filter) throws IOException {
		return delegate.newDirectoryStream(dir, filter);
	}

	@Override
	public void createDirectory(Path dir, FileAttribute<?>... attrs) throws IOException {
		delegate.createDirectory(dir, attrs);
	}

	@Override
	public void delete(Path path) throws IOException {
		delegate.delete(path);
	}

	@Override
	public void copy(Path source, Path target, CopyOption... options) throws IOException {
		delegate.copy(source, target, options);
	}

	@Override
	public void move(Path source, Path target, CopyOption... options) throws IOException {
		delegate.move(source, target, options);
	}

	@Override
	public boolean isSameFile(Path path, Path path2) throws IOException {
		return delegate.isSameFile(path, path2);
	}

	@Override
	public boolean isHidden(Path path) throws IOException {
		return delegate.isHidden(path);
	}

	@Override
	public FileStore getFileStore(Path path) throws IOException {
		return delegate.getFileStore(path);
	}

	@Override
	public void checkAccess(Path path, AccessMode... modes) throws IOException {
		delegate.checkAccess(path, modes);
	}

	@Override
	public <V extends FileAttributeView> V getFileAttributeView(Path path, Class<V> type, LinkOption... options) {
		return delegate.getFileAttributeView(path, type, options);
	}

	@Override
	public <A extends BasicFileAttributes> A readAttributes(Path path, Class<A> type, LinkOption... options)
			throws IOException {
		return delegate.readAttributes(path, type, options);
	}

	@Override
	public Map<String, Object> readAttributes(Path path, String attributes, LinkOption... options) throws IOException {
		return delegate.readAttributes(path, attributes, options);
	}

	@Override
	public void setAttribute(Path path, String attribute, Object value, LinkOption... options) throws IOException {
		delegate.setAttribute(path, attribute, value, options);
	}

	public void register(CachingJarsJavaFileManager cachingJarsJavaFileManager) {
		int id = System.identityHashCode(cachingJarsJavaFileManager);
		cleaner.register(cachingJarsJavaFileManager, () -> closeFileManager(id));
		synchronized (this) {
			this.fileManagersIdentitieis.add(id);
		}
	}

}

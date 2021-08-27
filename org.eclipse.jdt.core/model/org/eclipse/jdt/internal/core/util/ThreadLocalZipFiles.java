/*******************************************************************************
 * Copyright (c) 2021 IBM Joerg Kubitz and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Joerg Kubitz - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.internal.core.JavaModelManager;

/**
 * * A factory for ThreadLocalZipFile. Cache zip files for performance (see
 * https://bugs.eclipse.org/bugs/show_bug.cgi?id=134172) It keeps a thread-local cache of AutoCloseable Wrappers around
 * ZipFile until all Wrapper and all AutoCloseable Holder closed.
 *
 * @see java.util.zip.ZipFile
 * @see ThreadLocalZipFile
 **/
public final class ThreadLocalZipFiles {
	public static boolean ZIP_ACCESS_VERBOSE = false;

	/**
	 * A cache of opened zip files per thread. (for a given thread, the object value is a HashMap from IPath to
	 * java.io.ZipFile)
	 */
	private static ThreadLocal<Map<IPath, ThreadLocalZipFile>> threadlocalZipMap = ThreadLocal
			.withInitial(() -> new HashMap<>());
	private static ThreadLocal<Integer> holderReferenceCount = ThreadLocal.withInitial(() -> 0);

	/**
	 * A java.lang.AutoCloseable wrapper to leave all ZipFiles open while the wrapper is open
	 *
	 * @see java.util.zip.ZipFile
	 **/
	public static final class ThreadLocalZipFileHolder implements AutoCloseable {
		ThreadLocalZipFileHolder() {
			int r = holderReferenceCount.get();
			if (verboseLogging()) {
				System.out.println(this + " locked " + r); //$NON-NLS-1$
			}
			r++;
			holderReferenceCount.set(r);
		}

		@Override
		public void close() {
			int r = holderReferenceCount.get();
			if (verboseLogging()) {
				System.out.println(this + " unlocked " + r); //$NON-NLS-1$
			}
			r--;
			holderReferenceCount.set(r);
			if (r == 0) {
				flushThreadLocalZipFiles();
				holderReferenceCount.remove();
			}
		}

		@Override
		public String toString() {
			return "(" + Thread.currentThread().getName() + ") [" + this.getClass().getSimpleName() + "@" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ System.identityHashCode(this) + "]"; //$NON-NLS-1$
		}
	}

	private static void flushThreadLocalZipFiles() {
		for (ThreadLocalZipFile zip : new ArrayList<>(threadlocalZipMap.get().values())) {
			zip.close();
		}
		threadlocalZipMap.remove();
	}

	/** returns a lock Resource that the receiver must close after use.
	 * While the lock is open createZipFile() will create only a single ZipFile.
	 * Successive calls to createZipFile() will return the same instance.
	 * The ZipFile will be not be closed until the lock is closed.
	 * If multiple locking resources are nested only the closing of the outermost lock
	 * will close the ZipFiles. Use this to cache ZipFiles within a block. **/
	public static ThreadLocalZipFileHolder createZipHolder(Object holder) {
		// ignore the holder
		return new ThreadLocalZipFileHolder();
	}

	/**
	 * A java.lang.AutoCloseable wrapper around java.util.zip.ZipFile which holds the Zipfile open within this thread
	 *
	 * @see java.util.zip.ZipFile
	 **/
	public static final class ThreadLocalZipFile implements AutoCloseable {

		private java.util.zip.ZipFile zipFile;
		private int referenceCount;
		private IPath path;

		public ThreadLocalZipFile(IPath path) throws IOException, CoreException {
			this.path = path;
			if (verboseLogging()) {
				System.out.println(this + " Opened"); //$NON-NLS-1$
			}
			try {
				File localFile=JavaModelManager.getLocalFile(path);
				java.util.zip.ZipFile zip = new java.util.zip.ZipFile(localFile);
				this.zipFile = zip;
			} catch (IOException e) {
				if (verboseLogging()) {
					System.out.println(this + " Error Opening"); //$NON-NLS-1$
				}
				throw e;
			}
		}

		@Override
		public void close() {
			if (verboseLogging()) {
				System.out.println(this + " Dereferenced " + this.referenceCount); //$NON-NLS-1$
			}
			if (this.path == null) {
				return; // already closed
			}
			this.referenceCount--;
			if (this.referenceCount == 0 && ((int) holderReferenceCount.get()) == 0) {
				realyClose();
				this.path = null;
			}
		}

		@SuppressWarnings("resource") // remove returns this
		private void realyClose() {
			try {
				threadlocalZipMap.get().remove(this.path);
				if (verboseLogging()) {
					System.out.println(this + " Closed"); //$NON-NLS-1$
				}
				this.zipFile.close();
			} catch (IOException e) {
				// problem occured closing zip file: cannot do much more
//				JavaCore.getPlugin().getLog().log(
//						new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, "Error closing " + this.zipFile.getName(), e)); //$NON-NLS-1$
			}
		}

		public Enumeration<? extends ZipEntry> entries() {
			return this.zipFile.entries();
		}

		public ZipEntry getEntry(String name) {
			return this.zipFile.getEntry(name);
		}

		public int size() {
			return this.zipFile.size();
		}

		public String getComment() {
			return this.zipFile.getComment();
		}

		public InputStream getInputStream(ZipEntry entry) throws IOException {
			return this.zipFile.getInputStream(entry);
		}

		public String getName() {
			return this.zipFile.getName();
		}

		@Override
		public String toString() {
			return "(" + Thread.currentThread().getName() + ") [" + this.getClass().getSimpleName() + "@" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ System.identityHashCode(this) + "] " + this.path; //$NON-NLS-1$
		}
	}

	/** returns either a new Wrapper around ZipFile or a previously cached instance.
	 * The receiver must close the returned resource after use.
	 * @throws CoreException **/
	@SuppressWarnings("resource") // create method forwards ownership to caller
	public static ThreadLocalZipFile createZipFile(IPath path) throws IOException, CoreException {
		{
			ThreadLocalZipFile existing = threadlocalZipMap.get().get(path);
			if (existing != null) {
				existing.referenceCount++;
				if (verboseLogging()) {
					System.out.println(existing + " Referenced " + existing.referenceCount); //$NON-NLS-1$
				}
				return existing;
			}
		}
		{
			ThreadLocalZipFile newZipFile = new ThreadLocalZipFile(path);
			threadlocalZipMap.get().put(path, newZipFile);
			newZipFile.referenceCount += 1 + (holderReferenceCount.get() > 0 ? 1 : 0);
			return newZipFile;
		}
	}

	public static boolean verboseLogging() {
		if (java.util.Arrays.stream(Thread.currentThread().getStackTrace()).anyMatch(s->(""+s).contains("JavaSearchBugsTests$"))) {  //$NON-NLS-1$//$NON-NLS-2$
			(new RuntimeException()).printStackTrace(System.out);
		}
		return true;
//		return ZIP_ACCESS_VERBOSE;
	}

	public static boolean isPresent(IPath path) {
		return threadlocalZipMap.get().get(path) != null;
	}
}

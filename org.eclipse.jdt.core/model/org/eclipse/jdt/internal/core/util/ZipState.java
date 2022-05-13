/*******************************************************************************
 * Copyright (c) 2000, 2021 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Joerg Kubitz - initial API
 *******************************************************************************/
package org.eclipse.jdt.internal.core.util;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.ThreadLocalZipFiles.ZipFileResource;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;

/**
 * A factory for ThreadLocalZipFile
 *
 * @see java.util.zip.ZipFile
 **/
public final class ZipState {
	public static boolean DEBUG_INVALID_ARCHIVES = false;

	/*
	 * A map of IPaths for jars with known validity (such as being in a valid/known format or not), to an eviction
	 * timestamp. Synchronize on validityMap before accessing.
	 */
	private static final ConcurrentHashMap<IPath, ArchiveValidityInfo> validityMap = new ConcurrentHashMap<>();

	// The amount of time from when an invalid archive is first sensed until that state is considered stale.
	private static long INVALID_ARCHIVE_TTL_MILLISECONDS = 2 * 60 * 1000;

	private static class ArchiveValidityInfo {
		/**
		 * Time at which this entry will be removed from the validityMap.
		 */
		final long evictionTimestamp;

		/**
		 * Reason the entry was added to the invalid validityMap.
		 */
		final ArchiveValidity reason;

		ArchiveValidityInfo(long evictionTimestamp, ArchiveValidity reason) {
			this.evictionTimestamp = evictionTimestamp;
			this.reason = reason;
		}
	}

	public enum ArchiveValidity {
		INVALID, VALID;

		public boolean isValid() {
			return this == VALID;
		}
	}

	/** returns either a new Wrapper around ZipFile or a previously cached instance.
	 * The receiver must close the returned resource after use. **/
	public static ZipFileResource createZipFile(IPath path) throws CoreException {
		getArchiveValidity(path); // evicts validity after some time
		return createZipFileWithoutEvict(path);
	}

	private static ZipFileResource createZipFileWithoutEvict(IPath path) throws CoreException {
		try {
			ZipFileResource zipFile = ThreadLocalZipFiles.createZipFile(path);
			setArchiveValidity(path, ArchiveValidity.VALID); // remember its valid & update TTL
			return zipFile;
		} catch (IOException e) {
			// file may exist but for some reason is inaccessible
			setArchiveValidity(path, ArchiveValidity.INVALID); // update TTL
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Messages.status_IOException, e));
		}
	}

	public static void setArchiveValidity(IPath path, ArchiveValidity reason) {
		if (DEBUG_INVALID_ARCHIVES) {
			System.out.println("JAR cache: adding " + reason + " " + path); //$NON-NLS-1$//$NON-NLS-2$
		}
		synchronized (validityMap) {
			validityMap.put(path,
					new ArchiveValidityInfo(System.currentTimeMillis() + INVALID_ARCHIVE_TTL_MILLISECONDS, reason));
		}
	}

	public static void removeArchiveValidity(IPath path) {
		synchronized (validityMap) {
			ArchiveValidityInfo entry = validityMap.get(path);
			if (entry != null && entry.reason == ArchiveValidity.VALID) {
				if (DEBUG_INVALID_ARCHIVES) {
					System.out.println("JAR cache: keep VALID " + path); //$NON-NLS-1$
				}
				return; // do not remove the VALID information
			}
			if (validityMap.remove(path) != null) {
				if (DEBUG_INVALID_ARCHIVES) {
					System.out.println("JAR cache: removed INVALID " + path); //$NON-NLS-1$
				}
				try {
					// Bug 455042: Force an update of the JavaProjectElementInfo project caches.
					JavaModelManager javaModelManager = JavaModelManager.getJavaModelManager();
					for (IJavaProject project : javaModelManager.getJavaModel().getJavaProjects()) {
						if (project.findPackageFragmentRoot(path) != null) {
							((JavaProject) project).resetCaches();
						}
					}
				} catch (JavaModelException e) {
					Util.log(e, "Unable to retrieve the Java model."); //$NON-NLS-1$
				}
			}
		}
	}

	private static boolean isArchiveStateKnownToBeValid(IPath path) throws CoreException {
		ArchiveValidity validity = getArchiveValidity(path);
		if (validity == null || validity == ArchiveValidity.INVALID) {
			return false; // chance the file has become accessible/readable now.
		}
		return true;
	}

	public static ArchiveValidity getArchiveValidity(IPath path) {
		ArchiveValidityInfo invalidArchiveInfo;
		synchronized (validityMap) {
			invalidArchiveInfo = validityMap.get(path);
		}
		if (invalidArchiveInfo == null) {
			if (DEBUG_INVALID_ARCHIVES) {
				System.out.println("JAR cache: UNKNOWN validity for " + path); //$NON-NLS-1$
			}
			return null;
		}
		long now = System.currentTimeMillis();

		// If the TTL for this cache entry has expired, directly check whether the archive is still invalid.
		// If it transitioned to being valid, remove it from the cache and force an update to project caches.
		if (now > invalidArchiveInfo.evictionTimestamp) {
			try (ZipFileResource zipFile = createZipFileWithoutEvict(path)) {
				removeArchiveValidity(path);
			} catch (CoreException e) {
				// Archive is still invalid, fall through to reporting it is invalid.
			}
			// Retry the test from the start, now that we have an up-to-date result
			return getArchiveValidity(path);
		}
		if (DEBUG_INVALID_ARCHIVES) {
			System.out.println("JAR cache: " + invalidArchiveInfo.reason + " " + path); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return invalidArchiveInfo.reason;
	}

	public static void verifyArchiveContent(IPath path) throws CoreException {
		// TODO: we haven't finalized what path the JRT is represented by. Don't attempt to validate it.
		if (JavaModelManager.isJrt(path)) {
			return;
		}
		if (isArchiveStateKnownToBeValid(path)) {
			return; // known to be valid
		}
		try (ZipFileResource file = createZipFile(path)) {
			// just check
		}
	}

}

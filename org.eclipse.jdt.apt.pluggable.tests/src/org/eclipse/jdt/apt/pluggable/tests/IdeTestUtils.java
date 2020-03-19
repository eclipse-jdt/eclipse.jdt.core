/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.pluggable.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;

/**
 * Helper class to support compilation and results checking for tests running in batch mode.
 * @since 3.3.1
 */
public class IdeTestUtils {
	/**
	 * Name of the subdirectory within the test bundle where target resources are stored.
	 */
	public static final String RESOURCES_DIR = "resources";

	/**
	 * Copy files from a bundle into a project in the target workspace. Newlines will be
	 * converted according to {@link #shouldConvertToIndependentLineDelimiter(File)}.
	 * Directories named "CVS" will be ignored.
	 *
	 * @param proj
	 *            the project within which the files will be created.
	 * @param resourceFolderName
	 *            the name of the folder within the plug-in that the files will be copied
	 *            from, relative to <code>[plugin-root]/resources</code>
	 * @param destFolderName
	 *            the name of the folder within the target workspace that the files will
	 *            be copied to, relative to the project
	 * @throws Exception
	 *            might be an IOException or a CoreException
	 */
	public static void copyResources(IProject proj, String resourceFolderName, String destFolderName) throws Exception {
		String destFolderOSName = proj.getFolder( destFolderName ).getLocation().toOSString(); //$NON-NLS-1$
		File destFolder = new File(destFolderOSName);
		File resourceFolder = TestUtils.concatPath(getPluginDirectoryPath(), RESOURCES_DIR, resourceFolderName);
		copyResources(resourceFolder, destFolder);
		proj.refreshLocal(IResource.DEPTH_INFINITE, null);
	}

	/**
	 * @return the absolute filesystem-based path of the root of the bundle filesystem.
	 * This will cause the bundle to be extracted to a temporary directory on the filesystem
	 * if necessary; see {@link FileLocator#toFileURL(URL)}.
	 */
	public static String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.apt.pluggable.tests").getEntry("/");
			return new File(FileLocator.toFileURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Copy a file from one location to another, unless the destination file already exists and has
	 * the same timestamp and file size. Create the destination location if necessary. Convert line
	 * delimiters according to {@link #shouldConvertToIndependentLineDelimiter(File)}.
	 *
	 * @param src
	 *            the full path to the resource location.
	 * @param destFolder
	 *            the full path to the destination location.
	 * @throws IOException
	 */
	private static void copyResource(File src, File dest) throws IOException {
		if (dest.exists() &&
				src.lastModified() < dest.lastModified() &&
				src.length() == dest.length())
		{
			return;
		}

		// read source bytes
		byte[] srcBytes = null;
		srcBytes = read(src);

		if (shouldConvertToIndependentLineDelimiter(src)) {
			String contents = new String(srcBytes);
			contents = TestUtils.convertToIndependentLineDelimiter(contents);
			srcBytes = contents.getBytes();
		}

		File destFolder = dest.getParentFile();
		if (!destFolder.exists()) {
			if (!destFolder.mkdirs()) {
				throw new IOException("Unable to create directory " + destFolder);
			}
		}
		// write bytes to dest
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(dest);
			out.write(srcBytes);
			out.flush();
		} finally {
			if (out != null) {
				out.close();
			}
		}
	}

	private static void copyResources(File resourceFolder, File destFolder) throws IOException {
		if (resourceFolder == null) {
			return;
		}
		// Copy all resources in this folder
		String[] children = resourceFolder.list();
		if (null == children) {
			return;
		}
		// if there are any children, (recursively) copy them
		for (String child : children) {
			if ("CVS".equals(child)) {
				continue;
			}
			File childRes = new File(resourceFolder, child);
			File childDest = new File(destFolder, child);
			if (childRes.isDirectory()) {
				copyResources(childRes, childDest);
			}
			else {
				copyResource(childRes, childDest);
			}
		}
	}

	private static byte[] read(java.io.File file) throws java.io.IOException {
		int fileLength;
		byte[] fileBytes = new byte[fileLength = (int) file.length()];
		java.io.FileInputStream stream = null;
		try {
			stream = new java.io.FileInputStream(file);
			int bytesRead = 0;
			int lastReadSize = 0;
			while ((lastReadSize != -1) && (bytesRead != fileLength)) {
				lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
				bytesRead += lastReadSize;
			}
		} finally {
			if (stream != null) {
				stream.close();
			}
		}
		return fileBytes;
	}

	/**
	 * @return true if this file's end-of-line delimiters should be replaced with
	 * a platform-independent value, e.g. for compilation.
	 */
	private static boolean shouldConvertToIndependentLineDelimiter(File file) {
		return file.getName().endsWith(".java");
	}

}

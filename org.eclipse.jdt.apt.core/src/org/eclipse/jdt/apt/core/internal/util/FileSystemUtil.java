/*******************************************************************************
 * Copyright (c) 2005, 2009 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.AptPlugin;

/**
 *  Simple utility class to encapsulate an mkdirs() that avoids a timing issue
 *  in the jdk.
 */
public final class FileSystemUtil
{
	private FileSystemUtil() {}

	/**
	 * If the given resource is a folder, then recursively deleted all derived
	 * files and folders contained within it. Delete the folder if it becomes empty
	 * and if itself is also a derived resource.
	 * If the given resource is a file, delete it iff it is a derived resource.
	 * The resource is left untouched if it is no a folder or a file.
	 * @return <code>true</code> iff the resource has been deleted.
	 */
	public static boolean deleteDerivedResources(final IResource resource)
		throws CoreException
	{
		if (null == resource) {
			return false;
		}
		if( resource.getType() == IResource.FOLDER ){
			boolean deleteFolder = resource.isDerived();
			IResource[] members = ((IFolder)resource).members();
			for( int i=0, len=members.length; i<len; i++ ){
				deleteFolder &= deleteDerivedResources(members[i]);
			}
			if( deleteFolder ){
				deleteResource(resource);
				return true;
			}
			return false;
		}
		else if( resource.getType() == IResource.FILE ){
			if( resource.isDerived() ){
				deleteResource(resource);
				return true;
			}
			return false;
		}
		// will skip pass everything else.
		else
			return false;
	}

	/**
	 * Delete a resource without throwing an exception.
	 */
	private static void deleteResource(IResource resource) {
		try {
			resource.delete(true, null);
		} catch (CoreException e) {
			// might have been concurrently deleted
			if (resource.exists()) {
				AptPlugin.log(e, "Unable to delete derived resource " + resource); //$NON-NLS-1$
			}
		}
	}

    public static void mkdirs( File parent )
    {
        if ( parent == null )
            return;

        // It is necessary to synchronize to prevent timing issues while creating the parent directories
        // We can be codegening multiple files that go into the same directory at the same time.
        synchronized (FileSystemUtil.class) {
            if (!parent.exists()) {
                boolean succeed = false;
                for (int i = 0 ; !succeed && i < 5 ; i++)
                    succeed = parent.mkdirs();
            }
        }
    }

    public static void makeDerivedParentFolders (IContainer container) throws CoreException {
    	// synchronize the "does it exist - if not, create it" sequence.
		if ((container instanceof IFolder) && !container.exists()) {
			makeDerivedParentFolders(container.getParent());
	    	try {
	    		((IFolder)container).create(true, true, null);
	    	}
	    	catch (CoreException e) {
	    		// Ignore race condition where another thread created the folder at the
	    		// same time, causing checkDoesNotExist() to throw within create().
	    		if (!container.exists()) {
	    			throw e;
	    		}
	    	}
			container.setDerived(true, null);
		}
    }
    public static void saveToDisk(IFile file, byte[] toSave) throws IOException{
		try {
			FileSystemUtil.makeDerivedParentFolders(file.getParent());
			file.write(toSave, true, true, false, null);
		} catch (CoreException ce) {
			if (file.exists()) {
				// Do nothing. This is a case-insensitive file system mismatch,
				// and the underlying platform has saved the contents already.
			}
			else {
				AptPlugin.log(ce, "Could not create generated file"); //$NON-NLS-1$
				throw new IOException(ce.getMessage(), ce);
			}
		}
	}

    /**
     * Returns the contents of a IFile as a string in UTF8 format
     */
	public static String getContentsOfIFile(IFile file) throws IOException, CoreException {
		return new String(file.readAllBytes(), StandardCharsets.UTF_8);
	}

    /**
     * Returns the contents of a file as a string in UTF8 format
     */
	public static String getContentsOfFile(File file) throws IOException {
		return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
	}

    /**
     * Stores a string into an Eclipse file in UTF8 format.  The file
     * will be created if it does not already exist.
     * @throws IOException
     * @throws CoreException
     */
	public static void writeStringToIFile(IFile file, String contents) throws IOException, CoreException {
		byte[] data = contents.getBytes(StandardCharsets.UTF_8);
		try {
			file.write(data, true, false, false, null);
		} catch (CoreException e) {
			if (file.exists() && file.isReadOnly()) {
				// provide opportunity to checkout read-only .factorypath file
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[] { file }, null);
				file.write(data, true, false, false, null);
			} else {
				throw e;
			}
		}
	}

    /**
     * Stores a string into an ordinary workspace file in UTF8 format.
     * The file will be created if it does not already exist.
     */
    public static void writeStringToFile(File file, String contents) throws IOException {
    	byte[] data = contents.getBytes(StandardCharsets.UTF_8);
    	OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
    	try {
    		for (byte b : data) {
    			out.write(b);
    		}
    	}
    	finally {
    		try {out.close();} catch (IOException ioe) {}
    	}
    }

    /**
	 * Return true if the content of the streams is identical,
	 * false if not.
	 */
	public static boolean compareStreams(InputStream is1, InputStream is2) {
		try {
			int b1 = is1.read();
	        while(b1 != -1) {
	            int b2 = is2.read();
	            if(b1 != b2) {
	                return false;
	            }
	            b1 = is1.read();
	        }

	        int b2 = is2.read();
	        if(-1 != b2) {
	            return false;
	        }
	        return true;
		}
		catch (IOException ioe) {
			return false;
		}
	}
}

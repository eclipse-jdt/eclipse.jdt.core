/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 *  Simple utility class to encapsulate an mkdirs() that avoids a timing issue
 *  in the jdk.  
 */
public final class FileSystemUtil
{
	private FileSystemUtil() {}
	
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
    
    public static String getContentsOfFile(IFile file) throws IOException, CoreException {
    	InputStream in = file.getContents(true);
    	try {
    		ByteArrayOutputStream out = new ByteArrayOutputStream();
    		byte[] buffer = new byte[512];
    		int len;
    		while ((len = in.read(buffer)) > 0) {
    			out.write(buffer, 0, len);
    		}
    		out.close();
    		String s = new String(out.toByteArray(), "UTF8");
    		return s;
    	}
    	finally {
    		try {in.close();} catch (IOException ioe) {}
    	}
    }
    
    public static void writeStringToFile(IFile file, String contents) throws IOException, CoreException {
    	byte[] data = contents.getBytes("UTF8");
    	ByteArrayInputStream input = new ByteArrayInputStream(data);
    	if (file.exists()) {
    		file.setContents(input, IResource.FORCE, null);
    	}
    	file.create(input, IResource.FORCE, null);
    }
}

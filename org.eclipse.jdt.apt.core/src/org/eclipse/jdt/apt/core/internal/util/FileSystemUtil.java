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

import java.io.File;

/**
 *  Simple utility class to encapsulate an mkdirs() that avoids a timing issue
 *  in the jdk.  
 */
public class FileSystemUtil
{
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
}

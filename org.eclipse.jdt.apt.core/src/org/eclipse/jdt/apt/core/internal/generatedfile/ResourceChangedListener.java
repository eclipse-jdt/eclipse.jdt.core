/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;

public class ResourceChangedListener implements IResourceChangeListener 
{
	public void resourceChanged(IResourceChangeEvent event) 
	{
		if ( event.getType() == IResourceChangeEvent.POST_CHANGE )
		{
			try
			{
				event.getDelta().accept( new Visitor() );
			}
			catch ( CoreException ce )
			{
				// TODO:  handle exception here.
				ce.printStackTrace();
			}
		}
	}

	public static class Visitor implements IResourceDeltaVisitor
	{

		public boolean visit(IResourceDelta delta) throws CoreException 
		{
			IResource r = delta.getResource();
			//printDeltaInfo( delta );
			
			if ( delta.getKind() == IResourceDelta.REMOVED && r instanceof IFile)
			{
			
				GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager();
				IFile f = (IFile)r;
				if ( gfm.isParentFile( f ) )
				{
					gfm.parentFileDeleted( (IFile) r, null /* progress monitor */ );
				}
				else if ( gfm.isGeneratedFile( f ) )
				{
					gfm.generatedFileDeleted( f, null /*progress monitor */ );
				}
			}
				
			if ( delta.getKind() == IResourceDelta.REMOVED && r instanceof IFolder )
			{
				// handle delete of generated source folder
			}

			return true;
		}		
	}
	
	private static void printDeltaInfo( IResourceDelta delta )
	{
		System.out.println("\n\n-------------------------");
		switch( delta.getKind() )
		{	             
	    case IResourceDelta.ADDED:
	        System.out.println("delta.getKind() is IResourceDelta.ADDED" );
	        break;
	    case IResourceDelta.REMOVED:
	        System.out.println("delta.getKind() is IResourceDelta.REMOVED" );
	        break;
	    case IResourceDelta.CHANGED:
	        System.out.println("delta.getKind() is IResourceDelta.CHANGED" );
	        break;
	    case IResourceDelta.ADDED_PHANTOM:
	        System.out.println("delta.getKind() is IResourceDelta.ADDED_PHANTOM" );
	        break;
	    case IResourceDelta.REMOVED_PHANTOM:
	        System.out.println("delta.getKind() is IResourceDelta.REMOVED_PHANTOM" );
	        break;

		}

		System.out.println("event has flags...");
		int eventFlags = delta.getFlags();
        if ( (eventFlags & IResourceDelta.CONTENT )!= 0 )
            System.out.println("\teventFlags has IResourceDelta.CONTENT" );
        
        if ( ( eventFlags & IResourceDelta.DESCRIPTION)!= 0 )
            System.out.println("\teventFlags has IResourceDelta.DESCRIPTION" );

        if ( (eventFlags & IResourceDelta.ENCODING )!= 0)
            System.out.println("\teventFlags has IResourceDelta.ENCODING" );

        if ( (eventFlags & IResourceDelta.OPEN )!= 0)
            System.out.println("\teventFlags has IResourceDelta.OPEN" );

        if ( (eventFlags & IResourceDelta.MOVED_TO )!= 0)
            System.out.println("\teventFlags has IResourceDelta.MOVED_TO" );

        if ( (eventFlags & IResourceDelta.MOVED_FROM )!= 0)
            System.out.println("\teventFlags has IResourceDelta.MOVED_FROM" );

        if ( (eventFlags & IResourceDelta.TYPE )!= 0)
            System.out.println("\teventFlags has IResourceDelta.TYPE" );

        if ( (eventFlags & IResourceDelta.SYNC )!= 0)
            System.out.println("\teventFlags has IResourceDelta.SYNC" );

        if ( (eventFlags & IResourceDelta.MARKERS )!= 0)
            System.out.println("\teventFlags has IResourceDelta.MARKERS" );

        if ( (eventFlags & IResourceDelta.REPLACED )!= 0)
            System.out.println("\teventFlags has IResourceDelta.REPLACED" );
		
		System.out.println("\n---------------------------------------\n\n\n");
	}
	
}

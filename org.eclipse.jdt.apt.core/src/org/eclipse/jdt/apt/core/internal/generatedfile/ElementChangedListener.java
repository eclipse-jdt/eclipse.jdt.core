/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

public class ElementChangedListener implements IElementChangedListener 
{
	public void elementChanged(ElementChangedEvent event) 
	{
		Object o = event.getSource();
		if ( o instanceof IJavaElementDelta )
		{			
			IJavaElementDelta delta = (IJavaElementDelta) o;
			processElementDelta( delta );

		}
	}
	
	private void processElementDelta( IJavaElementDelta delta )
	{
		IJavaElementDelta[] deltas = delta.getAffectedChildren();
		if ( deltas != null && deltas.length > 0 )
		{
			for( int i = 0; i<deltas.length; i++ )
				processElementDelta( deltas[i] );
		}
		if ( delta.getElement() instanceof ICompilationUnit )
		{
			ICompilationUnit cu = (ICompilationUnit) delta.getElement();	
				
			// handle case where a parent editor is closed.  If an editor is open, then 
			// the compilation unit's isWorkingCopy() will return false.
			if ( ! cu.isWorkingCopy() )
			{
				IJavaProject jp = cu.getJavaProject();
				IProject p = jp.getProject();
				GeneratedFileManager gfm = GeneratedFileManager.getGeneratedFileManager(p);
				IFile f = (IFile)cu.getResource();
				if ( gfm.isParentFile( f ) )
				{
					try 
					{
						gfm.parentWorkingCopyDiscarded( f );
					} 
					catch (JavaModelException e) 
					{
						// TODO handle this exception
						e.printStackTrace();
					}		
				}
			}
		}
	}
}

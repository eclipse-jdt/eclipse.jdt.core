/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal.generatedfile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Used by the GeneratedFileManager in order to clean up working copies after a build
 */
public class WorkingCopyCleanupListener implements IElementChangedListener 
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
			//
			// handle case where a working copy is discarded (e.g., an editor is closed).  If an editor 
			// is not open, then the compilation unit's isWorkingCopy() will return false.
			//
			
			ICompilationUnit cu = (ICompilationUnit) delta.getElement();	

			boolean workingCopyDiscarded = 
				cu.getOwner() == null ? !cu.isWorkingCopy() : !cu.exists();
			
			if ( workingCopyDiscarded )
			{
				IJavaProject jp = cu.getJavaProject();
				GeneratedFileManager gfm = AptPlugin.getAptProject(jp).getGeneratedFileManager();
				try {
					gfm.workingCopyDiscarded( cu );
				} catch (CoreException e) {
					AptPlugin.log(e, "Failure processing delta: " + delta); //$NON-NLS-1$
				}
			}
		}
	}
}

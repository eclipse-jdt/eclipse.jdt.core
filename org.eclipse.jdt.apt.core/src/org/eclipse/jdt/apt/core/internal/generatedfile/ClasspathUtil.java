/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.generatedfile;

import java.util.ArrayList;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utilities to ensure the generated source folder is (or is not) on the
 * Java build path as appropriate.
 */
public class ClasspathUtil {

	/**
	 * Given a java project, this function will determine if the specified
	 * folder is a source folder of the java project.
	 *
	 * @param jp - the java project
	 * @param folder - the folder that you want to see if it is a classpath entry for the java project
	 * @return the IClasspathEntry corresponding to folder, or null if none was found.
	 * @throws JavaModelException
	 */
	public static IClasspathEntry findProjectSourcePath( IJavaProject jp, IFolder folder )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IClasspathEntry searchingFor =
			JavaCore.newSourceEntry(folder.getFullPath());
		IPath searchingForPath = searchingFor.getPath();
		for (int i = 0; i < cp.length; i++)
		{
			if (cp[i].getPath().equals( searchingForPath ))
				return cp[i];
		}
		return null;
	}

	/**
	 * Does the classpath contain the specified path?
	 * @param jp if non-null, get this project's classpath and ignore cp
	 * @param cp if non-null, use this classpath and ignore jp
	 * @param path the entry to look for on the classpath
	 * @param progressMonitor
	 * @return true if classpath contains the path specified.
	 * @throws JavaModelException
	 */
	public static boolean doesClasspathContainEntry(
			IJavaProject jp,
			IClasspathEntry[] cp,
			IPath path,
			IProgressMonitor progressMonitor)
		throws JavaModelException
	{
		if( cp == null )
			cp = jp.getRawClasspath();
		for (int i = 0; i < cp.length; i++)
		{
			if (cp[i].getPath().equals( path ))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * removes a classpath entry from the project
	 */
	public static void removeFromProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IPath workspaceRelativePath = folder.getFullPath();
		boolean found = doesClasspathContainEntry(jp, cp, workspaceRelativePath, progressMonitor);

		if( found ){
			IPath projectRelativePath = folder.getProjectRelativePath().addTrailingSeparator();

			// remove entries that are for the specified folder, account for
			// multiple entries, and clean up any exclusion entries to the
			// folder being removed.
			int j = 0;
			for ( int i=0; i<cp.length; i++ )
			{
				if (! cp[i].getPath().equals( workspaceRelativePath ) )
				{

					// see if we added the generated source dir as an exclusion pattern to some other entry
					IPath[] oldExclusions = cp[i].getExclusionPatterns();
					int m = 0;
					for ( int k = 0; k < oldExclusions.length; k++ )
					{
						if ( !oldExclusions[k].equals( projectRelativePath ) )
						{
							oldExclusions[m] = oldExclusions[k];
							m++;
						}
					}

					if ( oldExclusions.length == m )
					{
						// no exclusions changed, so we do't need to create a new entry
						cp[j] = cp[i];
					}
					else
					{
						// we've removed some exclusion, so create a new entry
						IPath[] newExclusions = new IPath[ m ];
						System.arraycopy( oldExclusions, 0, newExclusions, 0, m );
						cp[j] = JavaCore.newSourceEntry( cp[i].getPath(), cp[i].getInclusionPatterns(), newExclusions, cp[i].getOutputLocation(), cp[i].getExtraAttributes() );
					}

					j++;
				}
			}

			// now copy updated classpath entries into new array
			IClasspathEntry[] newCp = new IClasspathEntry[ j ];
			System.arraycopy( cp, 0, newCp, 0, j);
			jp.setRawClasspath( newCp, progressMonitor );

			if( AptPlugin.DEBUG ){
				AptPlugin.trace("removed " + workspaceRelativePath + " from classpath"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/**
	 * returns true if we updated the classpath, false otherwise
	 * @param specificOutputLocation
	 */
	public static boolean updateProjectClasspath( IJavaProject jp, IFolder folder, IProgressMonitor progressMonitor, boolean isTestCode, IPath specificOutputLocation )
		throws JavaModelException
	{
		IClasspathEntry[] cp = jp.getRawClasspath();
		IPath path = folder.getFullPath();
		boolean found = ClasspathUtil.doesClasspathContainEntry(jp, cp, path, progressMonitor);

		if (!found)
		{
			// update exclusion patterns
			ArrayList<IPath> exclusions = new ArrayList<>();
			for ( int i = 0; i< cp.length; i++ )
			{
				if ( cp[i].getPath().isPrefixOf( path ) )
				{
					// exclusion patterns must be project-relative paths, and must end with a "/"
					IPath projectRelativePath = folder.getProjectRelativePath().addTrailingSeparator();

					// path is contained in an existing source path, so update existing paths's exclusion patterns
					IPath[] oldExclusions = cp[i].getExclusionPatterns();

					// don't add if exclusion pattern already contains src dir
					boolean add = true;
					for ( int j = 0; j < oldExclusions.length; j++ )
						if ( oldExclusions[j].equals( projectRelativePath ) )
							add = false;

					if ( add )
					{
						IPath[] newExclusions;
						if ( cp[i].getExclusionPatterns() == null )
							newExclusions = new IPath[1];
						else
						{
							newExclusions = new IPath[ oldExclusions.length + 1 ];
							System.arraycopy( oldExclusions, 0, newExclusions, 0, oldExclusions.length );
						}
						newExclusions[ newExclusions.length - 1 ] = projectRelativePath;
						cp[i] = JavaCore.newSourceEntry(cp[i].getPath(), cp[i].getInclusionPatterns(), newExclusions, cp[i].getOutputLocation(), cp[i].getExtraAttributes());
					}

				}
				else if ( path.isPrefixOf( cp[i].getPath() ))
				{
					// new source path contains an existing source path, so add an exclusion pattern for it
					exclusions.add( cp[i].getPath().addTrailingSeparator() );
				}
			}

			IPath[] exclusionPatterns = exclusions.toArray( new IPath[exclusions.size()] );
			final IClasspathAttribute[] attrs = new IClasspathAttribute[isTestCode ? 2 : 1];
			attrs[0] = JavaCore.newClasspathAttribute(IClasspathAttribute.OPTIONAL, Boolean.toString(true));
			if(isTestCode) {
				attrs[1] = JavaCore.newClasspathAttribute(IClasspathAttribute.TEST, Boolean.toString(true));
			}
			IClasspathEntry generatedSourceClasspathEntry =
				JavaCore.newSourceEntry(folder.getFullPath(), new IPath[] {}, exclusionPatterns, specificOutputLocation, attrs );

			IClasspathEntry[] newCp = new IClasspathEntry[cp.length + 1];
			System.arraycopy(cp, 0, newCp, 0, cp.length);
			newCp[newCp.length - 1] = generatedSourceClasspathEntry;

			jp.setRawClasspath(newCp, progressMonitor );
		}

		// return true if we updated the project's classpath entries
		return !found;
	}

	public static IPath findTestOutputLocation(IClasspathEntry[] cp) {
		for (IClasspathEntry entry : cp) {
			if(entry.getEntryKind()==IClasspathEntry.CPE_SOURCE && entry.isTest()) {
				IPath outputLocation = entry.getOutputLocation();
				if(outputLocation != null) {
					return outputLocation;
				}
			}
		}
		return null;
	}

	/**
	 * All methods static.  Clients should not instantiate this class.
	 */
	private ClasspathUtil() {
	}


}

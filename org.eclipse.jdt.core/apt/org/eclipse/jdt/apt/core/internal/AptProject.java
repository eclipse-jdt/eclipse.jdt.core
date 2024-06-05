/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     jgarms@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedFileManager;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.apt.core.internal.util.FileSystemUtil;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Stores project-specific data for APT. Analagous to JavaProject
 * @author jgarms
 */
public class AptProject {

	private final IJavaProject _javaProject;

	private final GeneratedFileManager _main_gfm;

	private final GeneratedSourceFolderManager _main_gsfm;

	private final GeneratedFileManager _test_gfm;

	private final GeneratedSourceFolderManager _test_gsfm;

	public AptProject(final IJavaProject javaProject) {
		_javaProject = javaProject;
		_main_gsfm = new GeneratedSourceFolderManager(this, false);
		_main_gfm = new GeneratedFileManager(this, _main_gsfm);
		_test_gsfm = new GeneratedSourceFolderManager(this, true);
		_test_gfm = new GeneratedFileManager(this, _test_gsfm);
	}

	public IJavaProject getJavaProject() {
		return _javaProject;
	}

	public GeneratedFileManager getGeneratedFileManager(boolean isTestCode) {
		return isTestCode ? _test_gfm : _main_gfm;
	}

	public GeneratedSourceFolderManager getGeneratedSourceFolderManager(boolean isTestCode) {
		return isTestCode ? _test_gsfm : _main_gsfm;
	}

	/**
	 * This method should be called whenever compilation begins, to perform
	 * initialization and verify configuration.
	 */
	public void compilationStarted() {
		getGeneratedFileManager(false).compilationStarted();
		getGeneratedFileManager(true).compilationStarted();
	}

	/**
	 * This method should be called whenever project preferences are
	 * changed by the user.  This may cause the classpath and generated
	 * source folder to change, so this should <em>not</em> be called
	 * from a context where resources may be locked, e.g., within
	 * certain resource change listeners.
	 * @param key a preference key such as @see AptPreferenceConstants#APT_ENABLED
	 */
	public void preferenceChanged(String key) {
		if (AptPreferenceConstants.APT_GENSRCDIR.equals(key)) {
			_main_gsfm.folderNamePreferenceChanged();
		} else if (AptPreferenceConstants.APT_GENTESTSRCDIR.equals(key)) {
			_test_gsfm.folderNamePreferenceChanged();
		} else if(AptPreferenceConstants.APT_ENABLED.equals(key) ){
			_main_gsfm.enabledPreferenceChanged();
			_test_gsfm.enabledPreferenceChanged();
		}
	}

	/**
	 * Invoked whenever a project is cleaned.  This will remove any state kept about
	 * generated files for the given project.  If the deleteFiles flag is specified,
	 * then the contents of the generated source folder will be deleted.
	 *
	 * @param deleteFiles true if the contents of the generated source folder are to be
	 * deleted, false otherwise.
	 */

	public void projectClean( boolean deleteFiles, boolean cleanMain, boolean cleanTest )
	{
		if(cleanMain)
			_main_gfm.projectCleaned();
		if(cleanTest)
			_test_gfm.projectCleaned();

		// delete the contents of the generated source folder, but don't delete
		// the generated source folder because that will cause a classpath change,
		// which will force the next build to be a full build.
		if ( deleteFiles )
		{
			if (cleanMain) {
				IFolder f = _main_gsfm.getFolder();
				if (f != null && f.exists()) {
					try {
						IResource[] members = f.members();
						for (int i = 0; i < members.length; i++) {
							FileSystemUtil.deleteDerivedResources(members[i]);
						}
					} catch (CoreException ce) {
						AptPlugin.log(ce, "Could not delete generated files"); //$NON-NLS-1$
					}
				}
			}
			if (cleanMain) {
				IFolder f = _test_gsfm.getFolder();
				if (f != null && f.exists()) {
					try {
						IResource[] members = f.members();
						for (int i = 0; i < members.length; i++) {
							FileSystemUtil.deleteDerivedResources(members[i]);
						}
					} catch (CoreException ce) {
						AptPlugin.log(ce, "Could not delete generated files"); //$NON-NLS-1$
					}
				}
			}
		}
	}

	/**
	 * Invoked when a project is closed.
	 */
	public void projectClosed()
	{
		_main_gfm.projectClosed();
		_test_gfm.projectClosed();
	}

	/**
	 * Invoked when a project has been deleted, to clean up
	 * state associated with the project.
	 * This will not delete any of the project's generated files
	 * from disk, nor will it delete this object (which in turn
	 * owns the GeneratedFileManager for this project).
	 */
	public void projectDeleted()
	{
		if (AptPlugin.DEBUG)
			AptPlugin.trace("AptProject.projectDeleted cleaning state for project " + _javaProject.getElementName()); //$NON-NLS-1$
		_main_gfm.projectDeleted();
		_test_gfm.projectDeleted();
	}

}

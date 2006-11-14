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
 *
 */
public class AptProject {
	
	private final IJavaProject _javaProject;
	
	private final GeneratedFileManager _gfm;
	
	private final GeneratedSourceFolderManager _gsfm;
	
	public AptProject(final IJavaProject javaProject) {
		_javaProject = javaProject;
		_gsfm = new GeneratedSourceFolderManager(this);
		_gfm = new GeneratedFileManager(this, _gsfm);
	}
	
	public IJavaProject getJavaProject() {
		return _javaProject;
	}
	
	public GeneratedFileManager getGeneratedFileManager() {
		return _gfm;
	}
	
	public GeneratedSourceFolderManager getGeneratedSourceFolderManager() {
		return _gsfm;
	}
	
	/**
	 * This method should be called whenever compilation begins, to perform
	 * initialization and verify configuration.
	 */
	public void compilationStarted() {
		_gfm.compilationStarted();
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
			_gsfm.folderNamePreferenceChanged();
		}
		else if(AptPreferenceConstants.APT_ENABLED.equals(key) ){
			_gsfm.enabledPreferenceChanged();
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
	
	public void projectClean( boolean deleteFiles )
	{
		_gfm.discardAllState();
		
		// delete the contents of the generated source folder, but don't delete
		// the generated source folder because that will cause a classpath change,
		// which will force the next build to be a full build.
		if ( deleteFiles )
		{
			IFolder f = _gsfm.getFolder();
			if ( f != null && f.exists() )
			{
				try
				{	
					IResource[] members = f.members();
					for ( int i = 0; i<members.length; i++ ){
						FileSystemUtil.deleteDerivedResources(members[i]);
					}
				}
				catch ( CoreException ce )
				{
					AptPlugin.log(ce, "Could not delete generated files"); //$NON-NLS-1$
				}
			}
		}
	}
	
	/**
	 * invoked when a project is closed.  This will discard any open working-copies
	 * of generated files.
	 */
	public void projectClosed()
	{
		_gfm.discardWorkingCopyState();
	}
	
	/**
	 * Invoked when a project has been deleted.  This will remove this generated file manager
	 * from the static map of projects->generated file managers, and this will flush any known
	 * in-memory state tracking generated files.  This will not delete any of the project's generated files
	 * from disk.  
	 */
	public void projectDeleted()
	{
		_gfm.discardAllState();
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - specified that a source archive or a source folder can be attached to a binary
 *                               package fragment root.
 *     IBM Corporation - added root manipulation APIs: copy, delete, move
 ******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * A package fragment root contains a set of package fragments.
 * It corresponds to an underlying resource which is either a folder,
 * JAR, or zip.  In the case of a folder, all descendant folders represent
 * package fragments.  For a given child folder representing a package fragment, 
 * the corresponding package name is composed of the folder names between the folder 
 * for this root and the child folder representing the package, separated by '.'.
 * In the case of a JAR or zip, the contents of the archive dictates 
 * the set of package fragments in an analogous manner.
 * Package fragment roots need to be opened before they can be navigated or manipulated.
 * The children are of type <code>IPackageFragment</code>, and are in no particular order.
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 */
public interface IPackageFragmentRoot
	extends IParent, IJavaElement, IOpenable {
	/**
	 * Kind constant for a source path root. Indicates this root
	 * only contains source files.
	 */
	int K_SOURCE = 1;
	/**
	 * Kind constant for a binary path root. Indicates this
	 * root only contains binary files.
	 */
	int K_BINARY = 2;
	/**
	 * Empty root path
	 */
	String DEFAULT_PACKAGEROOT_PATH = ""; //$NON-NLS-1$
	/**
	 * Attaches the source archive identified by the given absolute path to this
	 * binary package fragment root. <code>rootPath</code> specifies the location 
	 * of the root within the archive or folder (empty specifies the default root 
	 * and <code>null</code> specifies the root path should be detected).
	 * Once a source archive or folder is attached to the package fragment root,
	 * the <code>getSource</code> and <code>getSourceRange</code>
	 * methods become operational for binary types/members.
	 * To detach a source archive or folder from a package fragment root, specify 
	 * <code>null</code> as the source path.
	 *
	 * @param sourcePath the given absolute path to the source archive or folder
	 * @param rootPath specifies the location of the root within the archive 
	 *              (empty specifies the default root and <code>null</code> specifies 
	 *               automatic detection of the root path)
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if this operation fails. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while updating a server property
	 * <li> This package fragment root is not of kind binary (INVALID_ELEMENT_TYPES)
	 * <li> The path provided is not absolute (RELATIVE_PATH)
	 * </ul>
	 */
	void attachSource(IPath sourcePath, IPath rootPath, IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Computes and returns the source attachment root path for the given source attachment path.
	 * Returns <code>null</code> if none could be found.
	 * 
	 * @param sourceAttachmentPath the given absolute path to the source archive or folder
	 * @return the computed source attachment root path or <code>null</cde> if none could be found
	 * @throws JavaModelException
	 * @since 2.1
	 */
	IPath computeSourceAttachmentRootPath(IPath sourceAttachmentPath) 
		throws JavaModelException;
	/**
	 * Copies the resource of this package fragment root to the destination path as specified by
	 * <code>IResource.copy(IPath, int, IProgressMonitor)</code> but excluding nested source folders.
	 * <p>
	 * If specified, update the classpath of the destination's project. If a non <code>null</code> 
	 * sibling is specified, a copy of this root's classpath entry is inserted before the sibling on the 
	 * destination project's raw classpath. If <code>null></code> is specified, the classpath entry 
	 * is added at the end of the raw classpath.
	 * </p>
	 * 
	 * @param destination the destination path
	 * @param updateFlags bit-wise or of update flag constants
	 *   (<code>FORCE</code> and <code>SHALLOW</code>)
	 * @param updateClasspath whether the classpath of the destination project should be updated
	 * @param sibling the classpath entry before which a copy of the classpath entry should be inserted
	 * @param monitor a progress monitor
	 * 
	 * @exception JavaModelException if this root could not be copied. Reasons include:
	 * <ul>
	 * <li> This root does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while copying the resource
	 * <li> This root is external (INVALID_RESOURCE_TYPE)
	 * <li> The destination is not in a Java project and <code>updateClasspath</code> was specified
	 *        (INVALID_PROJECT)
	 * <li> The sibling is not a classpath entry on the destination project's raw classpath (INVALID_SIBLING)
	 * <li> The same classpath entry already exists on the destination project's classpath 
	 *        (NAME_COLLISION) and <code>updateFlags</code> has not been specified 
	 * 	   as <code>FORCE</code>
	 * </ul>
	 * @see IResource#copy
	 * @since 2.1
	 */
	public void copy(IPath destination, int updateFlags, boolean updateClasspath, IClasspathEntry sibling, IProgressMonitor monitor) throws JavaModelException;
		
	/**
	 * Creates and returns a package fragment in this root with the 
	 * given dot-separated package name.  An empty string specifies the default package. 
	 * This has the side effect of creating all package
	 * fragments that are a prefix of the new package fragment which
	 * do not exist yet. If the package fragment already exists, this
	 * has no effect.
	 *
	 * For a description of the <code>force</code> flag, see <code>IFolder.create</code>.
	 *
	 * @param name the given dot-separated package name
	 * @param force a flag controlling how to deal with resources that
	 *    are not in sync with the local file system
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the element could not be created. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while creating an underlying resource
	 * <li> This package fragment root is read only (READ_ONLY)
	 * <li> The name is not a valid package name (INVALID_NAME)
	 * </ul>
	 * @return a package fragment in this root with the given dot-separated package name
	 * @see org.eclipse.core.resources.IFolder#create
	 */
	IPackageFragment createPackageFragment(
		String name,
		boolean force,
		IProgressMonitor monitor)
		throws JavaModelException;
	/**
	 * Deletes the resource of this package fragment root as specified by
	 * <code>IResource.delete(int, IProgressMonitor)</code> but excluding nested source folders.
	 * <p>
	 * If specified, update the raw classpaths of all Java projects refering to this root's resource 
	 * by removing the corresponding classpath entries on the raw classpaths.
	 * </p>
	 * 
	 * @param updateFlags bit-wise or of update flag constants
	 *   (<code>FORCE</code> and <code>KEEP_HISTORY</code>)
	 * @param updateClasspath whether the classpaths of the refering projects should be updated
	 * @param monitor a progress monitor
	 * 
	 * @exception JavaModelException if this root could not be deleted. Reasons include:
	 * <ul>
	 * <li> This root does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while deleting the resource
	 * <li> This root is external (INVALID_RESOURCE_TYPE)
	 * </ul>
	 * @see IResource#delete
	 * @since 2.1
	 */
	void delete(int updateFlags, boolean updateClasspath, IProgressMonitor monitor) throws JavaModelException;
		
	/**
	 * Returns this package fragment root's kind encoded as an integer.
	 * A package fragment root can contain <code>.java</code> source files,
	 * or <code>.class</code> files, but not both.
	 * If the underlying folder or archive contains other kinds of files, they are ignored.
	 * In particular, <code>.class</code> files are ignored under a source package fragment root,
	 * and <code>.java</code> files are ignored under a binary package fragment root.
	 *
	 * @see IPackageFragmentRoot#K_SOURCE
	 * @see IPackageFragmentRoot#K_BINARY
	 *
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return this package fragment root's kind encoded as an integer
	 */
	int getKind() throws JavaModelException;
	
	/**
	 * Returns an array of non-Java resources contained in this package fragment root.
	 * <p>
	 * Non-Java resources includes other files and folders located in the same
	 * directories as the compilation units or class files under this package
	 * fragment root. Resources excluded from this package fragment root
	 * by one or more exclusion patterns on the corresponding source classpath
	 * entry are considered non-Java resources and will appear in the result
	 * (possibly in a folder). Thus when a nested source folder is excluded, it will appear
	 * in the non-Java resources of the outer folder.
	 * </p>
	 * @return an array of non-Java resources contained in this package fragment root
	 * @see IClasspathEntry#getExclusionPatterns
	 */
	Object[] getNonJavaResources() throws JavaModelException;
	
	/**
	 * Returns the package fragment with the given package name.
	 * An empty string indicates the default package.
	 * This is a handle-only operation.  The package fragment
	 * may or may not exist.
	 * 
	 * @param packageName the given package name
	 * @return the package fragment with the given package name
	 */
	IPackageFragment getPackageFragment(String packageName);
	

	/**
	 * Returns the first raw classpath entry that corresponds to this package
	 * fragment root.
	 * A raw classpath entry corresponds to a package fragment root if once resolved
	 * this entry's path is equal to the root's path. 
	 * 
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource.
	 * @return the first raw classpath entry that corresponds to this package fragment root
	 * @since 2.0
	 */
	IClasspathEntry getRawClasspathEntry() throws JavaModelException;
	
	/**
	 * Returns the absolute path to the source archive attached to
	 * this package fragment root's binary archive.
	 *
	 * @return the absolute path to the corresponding source archive, 
	 *   or <code>null</code> if this package fragment root's binary archive
	 *   has no corresponding source archive, or if this package fragment root
	 *   is not a binary archive
	 * @exception JavaModelException if this operation fails
	 */
	IPath getSourceAttachmentPath() throws JavaModelException;
	
	/**
	 * Returns the path within this package fragment root's source archive. 
	 * An empty path indicates that packages are located at the root of the
	 * source archive.
	 *
	 * @return the path within the corresponding source archive, 
	 *   or <code>null</code> if this package fragment root's binary archive
	 *   has no corresponding source archive, or if this package fragment root
	 *   is not a binary archive
	 * @exception JavaModelException if this operation fails
	 */
	IPath getSourceAttachmentRootPath() throws JavaModelException;
	
	/**
	 * Returns whether this package fragment root's underlying
	 * resource is a binary archive (a JAR or zip file).
	 * 
	 * @return true if this package ragment root's underlying resource is a binary archive, false otherwise
	 */
	public boolean isArchive();
	
	/**
	 * Returns whether this package fragment root is external
	 * to the workbench (that is, a local file), and has no
	 * underlying resource.
	 * 
	 * @return true if this package fragment root is external
	 * to the workbench (that is, a local file), and has no
	 * underlying resource, false otherwise
	 */
	boolean isExternal();
	/**
	 * Moves the resource of this package fragment root to the destination path as specified by
	 * <code>IResource.move(IPath, int, IProgressMonitor)</code> but excluding nested source folders.
	 * <p>
	 * If specified, update the raw classpaths of all Java projects refering to this root's resource 
	 * by removing the corresponding classpath entries on the raw classpaths and update the 
	 * classpath of the destination's project. If a non <code>null</code> sibling is specified, 
	 * a copy of this root's classpath entry is inserted before the sibling on the destination project's 
	 * raw classpath. If <code>null></code> is specified, the classpath entry is added at the end of
	 * the destination project's raw classpath.
	 * </p>
	 * 
	 * @param destination the destination path
	 * @param updateFlags bit-wise or of update flag constants
	 *   (<code>FORCE</code>, <code>KEEP_HISTORY</code> and <code>SHALLOW</code>)
	 * @param updateClasspath whether the classpaths of the refering projects and destination project 
	 * 			should be updated
	 * @param sibling the classpath entry before which a copy of the classpath entry should be inserted
	 * @param monitor a progress monitor
	 * 
	 * @exception JavaModelException if this root could not be copied. Reasons include:
	 * <ul>
	 * <li> This root does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> A <code>CoreException</code> occurred while copying the resource
	 * <li> This root is external (INVALID_RESOURCE_TYPE)
	 * <li> The destination is not in a Java project and <code>updateClasspath</code> was specified 
	 *       (INVALID_DESTINATION)
	 * <li> The sibling is not a classpath entry on the destination project's raw classpath (INVALID_SIBLING)
	 * <li> The same classpath entry already exists on the destination project's classpath 
	 *        (NAME_COLLISION) and <code>updateFlags</code> has not been specified 
	 * 	  as <code>FORCE</code>
	 * </ul>
	 * @see IResource#move
	 * @since 2.1
	 */
	void move(IPath destination, int updateFlags, boolean updateClasspath, IClasspathEntry sibling, IProgressMonitor monitor) throws JavaModelException;
}
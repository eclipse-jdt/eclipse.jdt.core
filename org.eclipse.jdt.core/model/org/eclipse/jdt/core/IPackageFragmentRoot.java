package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.core.resources.IResource;
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
public interface IPackageFragmentRoot extends IParent, IJavaElement, IOpenable {
	
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
	String DEFAULT_PACKAGEROOT_PATH = ""/*nonNLS*/;

/**
 * Attaches the source archive identified by the given absolute path to this
 * JAR package fragment root. <code>rootPath</code> specifies the location
 * of the root within the archive (<code>null</code> or empty specifies the default root).
 * Once a source archive is attached to the JAR,
 * the <code>getSource</code> and <code>getSourceRange</code>
 * methods become operational for binary types/members.
 * To detach a source archive from a JAR, specify <code>null</code> as the
 * archivePath.
 *
 * @exception JavaModelException if this operation fails. Reasons include:
 * <ul>
 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while updating a server property
 * <li> This package fragment root is not a JAR (INVALID_ELEMENT_TYPES)
 * <li> The path provided is not absolute (RELATIVE_PATH)
 * </ul>
 * @deprecated - source attachment is to be specified using classpath entries.
 */
void attachSource(IPath archivePath, IPath rootPath, IProgressMonitor monitor) throws JavaModelException;
/**
 * Creates and returns a package fragment in this root with the 
 * given dot-separated package name.  An empty string specifies the default package. 
 * This has the side effect of creating all package
 * fragments that are a prefix of the new package fragment which
 * do not exist yet. If the package fragment already exists, this
 * has no effect.
 *
 * For a descrption of the <code>force</code> flag, see <code>IFolder.create</code>.
 *
 * @exception JavaModelException if the element could not be created. Reasons include:
 * <ul>
 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
 * <li> A <code>CoreException</code> occurred while creating an underlying resource
 * <li> This package fragment root is read only (READ_ONLY)
 * <li> The name is not a valid package name (INVALID_NAME)
 * </ul>
 * @see org.eclipse.core.resources.IFolder#create
 */
IPackageFragment createPackageFragment(String name, boolean force, IProgressMonitor monitor) throws JavaModelException;
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
 */
int getKind() throws JavaModelException;
/**
 * Returns an array of non-Java resources contained in this package fragment root.
 */
Object[] getNonJavaResources() throws JavaModelException;
/**
 * Returns the package fragment with the given package name.
 * An empty string indicates the default package.
 * This is a handle-only operation.  The package fragment
 * may or may not exist.
 */
IPackageFragment getPackageFragment(String packageName);
/**
 * Returns the path of this package fragment root. If this
 * package fragment root is not external, the path returned is the
 * full, absolute path to this package fragment root, relative to the
 * workbench. If this package fragment root is external, the path
 * returned is the absolute path to this package fragment root's
 * archive in the file system.
 */
IPath getPath();
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
 */
public boolean isArchive();
/**
 * Returns whether this package fragment root is external
 * to the workbench (that is, a local file), and has no
 * underlying resource.
 */
boolean isExternal();
}

package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;

/**
 * An entry on a Java project classpath identifying one or more package fragment
 * roots. A classpath entry has a content kind (either source, 
 * <code>K_SOURCE</code>, or binary, <code>K_BINARY</code>) which is inherited
 * by each package fragment root and package fragment associated with the entry.
 * <p>
 * A classpath entry can refer to any of the following:<ul>
 *	<li>Source code in the current project. In this case, the entry identifies a
 *		root folder in the current project containing package fragments and
 *		<code>.java</code> source files. The root folder itself represents a default
 *		package, subfolders represent package fragments, and <code>.java</code> files
 *		represent compilation units. All compilation units will be compiled when
 * 		the project is built. The classpath entry must specify the
 *		absolute path to the root folder. Entries of this kind are 
 *		associated with the <code>CPE_SOURCE</code> constant.</li>
 *	<li>A binary library in the current project, in another project, or in the external
 *		file system. In this case the entry identifies a JAR (or root folder) containing
 *		package fragments and <code>.class</code> files.  The classpath entry
 *		must specify the absolute path to the JAR (or root folder), and in case it refers
 *		to an external JAR, then there is no associated resource in the workbench. Entries 
 *		of this kind are associated with the <code>CPE_LIBRARY</code> constant.</li>
 *	<li>A required project. In this case the entry identifies another project in
 *		the workspace. The required project is used as a binary library when compiling
 *		(that is, the builder looks in the output location of the required project
 *		for required <code>.class</code> files when building). When performing other
 *		"development" operations - such as code assist, code resolve, type hierarchy
 *		creation, etc. - the source code of the project is referred to. Thus, development
 *		is performed against a required project's source code, and compilation is 
 *		performed against a required project's last built state.  The
 *		classpath entry must specify the absolute path to the
 *		project. Entries of this kind are  associated with the <code>CPE_PROJECT</code>
 *		constant. Note: referencing a required project with a classpath entry 
 *		only refers to the source code and associated <code>.class</code>
 *		files. It does not include any other libraries or projects that the required
 *		project's classpath refers to. Classpaths are not chained - each project must
 *		specify its own classpath in its entirety.</li>
 *  <li> A path beginning in a classpath variable defined globally to the workspace.
 *		Entries of this kind are  associated with the <code>CPE_VARIABLE</code> constant.  
 *      These entries are created using <code>JavaCore.setClasspathVariable</code>,
 * 		and gets resolved, to either a project or library entry, using
 *      <code>JavaCore.getResolvedClasspathVariable</code>.</li>
 * </ul>
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * Classpath entries can be created via methods on <code>JavaCore</code>.
 * </p>
 *
 * @see IJavaProject#newLibraryEntry
 * @see IJavaProject#newProjectEntry
 * @see IJavaProject#newSourceEntry
 */
public interface IClasspathEntry {

	/**
	 * Entry kind constant describing a classpath entry identifying a
	 * library. A library is a folder or JAR containing package
	 * fragments consisting of pre-compiled binaries.
	 */
	public static final int CPE_LIBRARY = 1;

	/**
	 * Entry kind constant describing a classpath entry identifying a
	 * required project.
	 */
	public static final int CPE_PROJECT = 2;

	/**
	 * Entry kind constant describing a classpath entry identifying a
	 * folder containing package fragments with source code
	 * to be compiled.
	 */
	public static final int CPE_SOURCE = 3;

	/**
	 * Entry kind constant describing a classpath entry defined using
	 * a path that begins with a classpath variable reference.
	 */
	public static final int CPE_VARIABLE = 4;
	
/**
 * Returns the kind of files found in the package fragments identified by this
 * classpath entry.
 *
 * @return <code>IPackageFragmentRoot.K_SOURCE</code> for files containing
 *   source code, and <code>IPackageFragmentRoot.K_BINARY</code> for binary
 *   class files.
 *   There is no specified value for an entry denoting a variable (<code>CPE_VARIABLE</code>)
 */
int getContentKind();
/**
 * Returns the kind of this classpath entry.
 *
 * @return one of:
 * <ul>
 * <li><code>CPE_SOURCE</code> - this entry describes a source root in
 		its project
 * <li><code>CPE_LIBRARY</code> - this entry describes a folder or JAR
 		containing binaries
 * <li><code>CPE_PROJECT</code> - this entry describes another project
 *
 * <li><code>CPE_VARIABLE</code> - this entry describes a project or library
 *  	indirectly via a classpath variable in the first segment of the path
 * </ul>
 */
int getEntryKind();
/**
 * Returns the path of this classpath entry.
 *
 * The meaning of the path of a classpath entry depends on its entry kind:<ul>
 *	<li>Source code in the current project (<code>CPE_SOURCE</code>) -  
 *      The path associated with this entry is the absolute path to the root folder. </li>
 *	<li>A binary library in the current project (<code>CPE_LIBRARY</code>) - the path
 *		associated with this entry is the absolute path to the JAR (or root folder), and 
 *		in case it refers to an external JAR, then there is no associated resource in 
 *		the workbench.
 *	<li>A required project (<code>CPE_PROJECT</code>) - the path of the entry denotes the
 *		path to the corresponding project resource.</li>
 *  <li>A variable entry (<code>CPE_VARIABLE</code>) - the first segment of the path 
 *      is the name of a classpath variable. If this classpath variable
 *		is bound to the path <it>P</it>, the path of the corresponding classpath entry
 *		is computed by appending to <it>P</it> the segments of the returned
 *		path without the variable.</li>
 * </ul>
 *
 * @return the path of this classpath entry
 */
IPath getPath();
/**
 * This is a helper method which returns the resolved classpath entry denoted 
 * by an entry (if it is a variable entry). It is obtained by resolving the variable 
 * reference in the first segment. Returns <node>null</code> if unable to resolve using 
 * the following algorithm:
 * <ul>
 * <li> if variable segment cannot be resolved, returns <code>null</code></li>
 * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
 * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
 * <li> if none returns <code>null</code></li>
 * </ul>
 * <p>
 * Variable source attachment is also resolved and recorded in the resulting classpath entry.
 * <p>
 * @return the resolved library or project classpath entry, or <code>null</code>
 *   if the given path could not be resolved to a classpath entry
 *
 * @deprecated - use JavaCore.getResolvedClasspathEntry(...)
 */
IClasspathEntry getResolvedEntry();
/**
 * Returns the path to the source archive associated with this
 * classpath entry, or <code>null</code> if this classpath entry has no
 * source attachment.
 * <p>
 * Only library and variable classpath entries may have source attachments.
 * For library classpath entries, the result path (if present) locates a source
 * archive. For variable classpath entries, the result path (if present) has
 * an analogous form and meaning as the variable path, namely the first segment 
 * is the name of a classpath variable.
 * </p>
 *
 * @return the path to the source archive, or <code>null</code> if none
 */
IPath getSourceAttachmentPath();
/**
 * Returns the path within the source archive where package fragments
 * are located. An empty path indicates that packages are located at
 * the root of the source archive. Returns a non-<code>null</code> value
 * if and only if <code>getSourceAttachmentPath</code> returns 
 * a non-<code>null</code> value.
 *
 * @return the path within the source archive, or <code>null</code> if
 *    not applicable
 */
IPath getSourceAttachmentRootPath();
}

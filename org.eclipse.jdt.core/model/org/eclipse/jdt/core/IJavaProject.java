/*******************************************************************************
 * Copyright (c) 2000, 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added getOption(String, boolean), getOptions(boolean) and setOptions(Map)
 *     IBM Corporation - deprecated getPackageFragmentRoots(IClasspathEntry) and 
 *                               added findPackageFragmentRoots(IClasspathEntry)
 *     IBM Corporation - added isOnClasspath(IResource)
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.eval.IEvaluationContext;

/**
 * A Java project represents a view of a project resource in terms of Java 
 * elements such as package fragments, types, methods and fields.
 * A project may contain several package roots, which contain package fragments. 
 * A package root corresponds to an underlying folder or JAR.
 * <p>
 * Each Java project has a classpath, defining which folders contain source code and
 * where required libraries are located. Each Java project also has an output location,
 * defining where the builder writes <code>.class</code> files. A project that
 * references packages in another project can access the packages by including
 * the required project in a classpath entry. The Java model will present the
 * source elements in the required project; when building, the compiler will use
 * the corresponding generated class files from the required project's output
 * location(s)). The classpath format is a sequence of classpath entries
 * describing the location and contents of package fragment roots.
 * </p>
 * Java project elements need to be opened before they can be navigated or manipulated.
 * The children of a Java project are the package fragment roots that are 
 * defined by the classpath and contained in this project (in other words, it
 * does not include package fragment roots for other projects).
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients. An instance
 * of one of these handles can be created via 
 * <code>JavaCore.create(project)</code>.
 * </p>
 *
 * @see JavaCore#create(org.eclipse.core.resources.IProject)
 * @see IClasspathEntry
 */
public interface IJavaProject extends IParent, IJavaElement, IOpenable {

	/**
	 * Returns the <code>IJavaElement</code> corresponding to the given
	 * classpath-relative path, or <code>null</code> if no such 
	 * <code>IJavaElement</code> is found. The result is one of an
	 * <code>ICompilationUnit</code>, <code>IClassFile</code>, or
	 * <code>IPackageFragment</code>. 
	 * <p>
	 * When looking for a package fragment, there might be several potential
	 * matches; only one of them is returned.
	 *
	 * <p>For example, the path "java/lang/Object.java", would result in the
	 * <code>ICompilationUnit</code> or <code>IClassFile</code> corresponding to
	 * "java.lang.Object". The path "java/lang" would result in the
	 * <code>IPackageFragment</code> for "java.lang".
	 * @param path the given classpath-relative path
	 * @exception JavaModelException if the given path is <code>null</code>
	 *  or absolute
	 * @return the <code>IJavaElement</code> corresponding to the given
	 * classpath-relative path, or <code>null</code> if no such 
	 * <code>IJavaElement</code> is found
	 */
	IJavaElement findElement(IPath path) throws JavaModelException;

	/**
	 * Returns the first existing package fragment on this project's classpath
	 * whose path matches the given (absolute) path, or <code>null</code> if none
	 * exist.
	 * The path can be:
	 * 	- internal to the workbench: "/Project/src"
	 *  - external to the workbench: "c:/jdk/classes.zip/java/lang"
	 * @param path the given absolute path
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return the first existing package fragment on this project's classpath
	 * whose path matches the given (absolute) path, or <code>null</code> if none
	 * exist
	 */
	IPackageFragment findPackageFragment(IPath path) throws JavaModelException;

	/**
	 * Returns the existing package fragment root on this project's classpath
	 * whose path matches the given (absolute) path, or <code>null</code> if
	 * one does not exist.
	 * The path can be:
	 *	- internal to the workbench: "/Compiler/src"
	 *	- external to the workbench: "c:/jdk/classes.zip"
	 * @param path the given absolute path
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return the existing package fragment root on this project's classpath
	 * whose path matches the given (absolute) path, or <code>null</code> if
	 * one does not exist
	 */
	IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException;
	/**
	 * Returns the existing package fragment roots identified by the given entry.
	 * Note that a classpath entry that refers to another project may
	 * have more than one root (if that project has more than on root
	 * containing source), and classpath entries within the current
	 * project identify a single root.
	 * <p>
	 * If the classpath entry denotes a variable, it will be resolved and return
	 * the roots of the target entry (empty if not resolvable).
	 * <p>
	 * If the classpath entry denotes a container, it will be resolved and return
	 * the roots corresponding to the set of container entries (empty if not resolvable).
	 * 
	 * @param entry the given entry
	 * @return the existing package fragment roots identified by the given entry
	 * @see IClasspathContainer
	 * @since 2.1
	 */
	IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry);
	/**
	 * Returns the first type found following this project's classpath 
	 * with the given fully qualified name or <code>null</code> if none is found.
	 * The fully qualified name is a dot-separated name. For example,
	 * a class B defined as a member type of a class A in package x.y should have a 
	 * the fully qualified name "x.y.A.B".
	 * 
	 * Note that in order to be found, a type name (or its toplevel enclosing
	 * type name) must match its corresponding compilation unit name. As a 
	 * consequence, secondary types cannot be found using this functionality.
	 * Secondary types can however be explicitely accessed through their enclosing
	 * unit or found by the <code>SearchEngine</code>.
	 * 
	 * @param fullyQualifiedName the given fully qualified name
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return the first type found following this project's classpath 
	 * with the given fully qualified name or <code>null</code> if none is found
	 * @see IType#getFullyQualifiedName(char)
	 * @since 2.0
	 */
	IType findType(String fullyQualifiedName) throws JavaModelException;
	/**
	 * Returns the first type found following this project's classpath 
	 * with the given package name and type qualified name
	 * or <code>null</code> if none is found.
	 * The package name is a dot-separated name.
	 * The type qualified name is also a dot-separated name. For example,
	 * a class B defined as a member type of a class A should have the 
	 * type qualified name "A.B".
	 * 
	 * Note that in order to be found, a type name (or its toplevel enclosing
	 * type name) must match its corresponding compilation unit name. As a 
	 * consequence, secondary types cannot be found using this functionality.
	 * Secondary types can however be explicitely accessed through their enclosing
	 * unit or found by the <code>SearchEngine</code>.
	 * 
	 * @param packageName the given package name
	 * @param typeQualifiedName the given type qualified name
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return the first type found following this project's classpath 
	 * with the given package name and type qualified name
	 * or <code>null</code> if none is found
	 * @see IType#getTypeQualifiedName(char)

	 * @since 2.0
	 */
	IType findType(String packageName, String typeQualifiedName) throws JavaModelException;

	/**
	 * Returns all of the existing package fragment roots that exist
	 * on the classpath, in the order they are defined by the classpath.
	 *
	 * @return all of the existing package fragment roots that exist
	 * on the classpath
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException;

	/**
	 * Returns an array of non-Java resources directly contained in this project.
	 * It does not transitively answer non-Java resources contained in folders;
	 * these would have to be explicitly iterated over.
	 * <p>
	 * Non-Java resources includes other files and folders located in the
	 * project not accounted for by any of it source or binary package fragment
	 * roots. If the project is a source folder itself, resources excluded from the
	 * corresponding source classpath entry by one or more exclusion patterns
	 * are considered non-Java resources and will appear in the result
	 * (possibly in a folder)
	 * </p>
	 * 
	 * @return an array of non-Java resources directly contained in this project
	 */
	Object[] getNonJavaResources() throws JavaModelException;

	/**
	 * Helper method for returning one option value only. Equivalent to <code>(String)this.getOptions(inheritJavaCoreOptions).get(optionName)</code>
	 * Note that it may answer <code>null</code> if this option does not exist, or if there is no custom value for it.
	 * <p>
	 * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @param inheritJavaCoreOptions - boolean indicating whether JavaCore options should be inherited as well
	 * @return the String value of a given option
	 * @see JavaCore#getDefaultOptions
	 * @since 2.1
	 */
	String getOption(String optionName, boolean inheritJavaCoreOptions);
	
	/**
	 * Returns the table of the current custom options for this project. Projects remember their custom options,
	 * i.e. only the options different from the the JavaCore global options for the workspace.
	 * A boolean argument allows to directly merge the project options with global ones from <code>JavaCore</code>.
	 * <p>
	 * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param inheritJavaCoreOptions - boolean indicating whether JavaCore options should be inherited as well
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions
	 * @since 2.1
	 */
	Map getOptions(boolean inheritJavaCoreOptions);

	/**
	 * Returns the default output location for this project as a workspace-
	 * relative absolute path.
	 * <p>
	 * The default output location is where class files are ordinarily generated
	 * (and resource files, copied). Each source classpath entry can also
	 * specify an output location for the generated class files (and copied
	 * resource files) corresponding to compilation units under that source
	 * folder. This makes it possible to arrange generated class files for
	 * different source folders in different output folders, and not
	 * necessarily the default output folder. This means that the generated
	 * class files for the project may end up scattered across several folders,
	 * rather than all in the default output folder (which is more standard).
	 * </p>
	 * 
	 * @return the workspace-relative absolute path of the default output folder
	 * @exception JavaModelException if this element does not exist
	 * @see #setOutputLocation
	 * @see IClasspathEntry#getOutputLocation
	 */
	IPath getOutputLocation() throws JavaModelException;

	/**
	 * Returns a package fragment root for the JAR at the specified file system path.
	 * This is a handle-only method.  The underlying <code>java.io.File</code>
	 * may or may not exist. No resource is associated with this local JAR
	 * package fragment root.
	 * 
	 * @param jarPath the jars's file system path
	 * @return a package fragment root for the JAR at the specified file system path
	 */
	IPackageFragmentRoot getPackageFragmentRoot(String jarPath);

	/**
	 * Returns a package fragment root for the given resource, which
	 * must either be a folder representing the top of a package hierarchy,
	 * or a <code>.jar</code> or <code>.zip</code> file.
	 * This is a handle-only method.  The underlying resource may or may not exist. 
	 * 
	 * @param resource the given resource
	 * @return a package fragment root for the given resource, which
	 * must either be a folder representing the top of a package hierarchy,
	 * or a <code>.jar</code> or <code>.zip</code> file
	 */
	IPackageFragmentRoot getPackageFragmentRoot(IResource resource);

	/**
	 * Returns all of the  package fragment roots contained in this
	 * project, identified on this project's resolved classpath. The result
	 * does not include package fragment roots in other projects referenced
	 * on this project's classpath.
	 *
	 * <p>NOTE: This is equivalent to <code>getChildren()</code>.
	 *
	 * @return all of the  package fragment roots contained in this
	 * project, identified on this project's resolved classpath
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IPackageFragmentRoot[] getPackageFragmentRoots() throws JavaModelException;

	/**
	 * Returns the existing package fragment roots identified by the given entry.
	 * Note that a classpath entry that refers to another project may
	 * have more than one root (if that project has more than on root
	 * containing source), and classpath entries within the current
	 * project identify a single root.
	 * <p>
	 * If the classpath entry denotes a variable, it will be resolved and return
	 * the roots of the target entry (empty if not resolvable).
	 * <p>
	 * If the classpath entry denotes a container, it will be resolved and return
	 * the roots corresponding to the set of container entries (empty if not resolvable).
	 * 
	 * @param entry the given entry
	 * @return the existing package fragment roots identified by the given entry
	 * @see IClasspathContainer
	 * @deprecated Use IJavaProject#findPackageFragmentRoots instead
	 */
	IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry);

	/**
	 * Returns all package fragments in all package fragment roots contained
	 * in this project. This is a convenience method.
	 *
	 * Note that the package fragment roots corresponds to the resolved
	 * classpath of the project.
	 *
	 * @return all package fragments in all package fragment roots contained
	 * in this project
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	IPackageFragment[] getPackageFragments() throws JavaModelException;

	/**
	 * Returns the <code>IProject</code> on which this <code>IJavaProject</code>
	 * was created. This is handle-only method.
	 * 
	 * @return the <code>IProject</code> on which this <code>IJavaProject</code>
	 * was created
	 */
	IProject getProject();

	/**
	 * This is a helper method returning the resolved classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * <p>
	 * A resolved classpath corresponds to a particular instance of the raw classpath bound in the context of 
	 * the current values of the referred variables, and thus should not be persisted.
	 * <p>
	 * A classpath variable provides an indirection level for better sharing a classpath. As an example, it allows
	 * a classpath to no longer refer directly to external JARs located in some user specific location. The classpath
	 * can simply refer to some variables defining the proper locations of these external JARs.
	 * <p>
	 * The boolean argument <code>ignoreUnresolvedVariable</code> allows to specify how to handle unresolvable variables,
	 * when set to <code>true</code>, missing variables are simply ignored, the resulting path is then only formed of the
	 * resolvable entries, without any indication about which variable(s) was ignored. When set to <code>false</code>, a
	 * JavaModelException will be thrown for the first unresolved variable (from left to right).
	 * 
	 * @exception JavaModelException in one of the corresponding situation:
	 * <ul>
	 *    <li> this element does not exist </li>
	 *    <li> an exception occurs while accessing its corresponding resource </li>
	 *    <li> a classpath variable was not resolvable and <code>ignoreUnresolvedVariable</code> was set to <code>false</code>. </li>
	 * </ul>
	 * @return 
	 * @see IClasspathEntry 
	 */
//	IClasspathEntry[] getExpandedClasspath(boolean ignoreUnresolvedVariable)
//		throws JavaModelException;

	/**
	 * Returns the raw classpath for the project, as a list of classpath entries. This corresponds to the exact set
	 * of entries which were assigned using <code>setRawClasspath</code>, in particular such a classpath may contain
	 * classpath variable entries. Classpath variable entries can be resolved individually (see <code>JavaCore#getClasspathVariable</code>),
	 * or the full classpath can be resolved at once using the helper method <code>getResolvedClasspath</code>.
	 * <p>
	 * A classpath variable provides an indirection level for better sharing a classpath. As an example, it allows
	 * a classpath to no longer refer directly to external JARs located in some user specific location. The classpath
	 * can simply refer to some variables defining the proper locations of these external JARs.
	 *  <p>
	 * Note that in case the project isn't yet opened, the classpath will directly be read from the associated <tt>.classpath</tt> file.
	 * <p>
	 * 
	 * @return the raw classpath for the project, as a list of classpath entries
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @see IClasspathEntry
	 */
	IClasspathEntry[] getRawClasspath() throws JavaModelException;

	/**
	 * Returns the names of the projects that are directly required by this
	 * project. A project is required if it is in its classpath.
	 * <p>
	 * The project names are returned in the order they appear on the classpath.
	 *
	 * @return the names of the projects that are directly required by this
	 * project in classpath order
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 */
	String[] getRequiredProjectNames() throws JavaModelException;

	/** TODO: (jim) improve doc to mention classpath containers
	 * This is a helper method returning the resolved classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * <p>
	 * A resolved classpath corresponds to a particular instance of the raw classpath bound in the context of 
	 * the current values of the referred variables, and thus should not be persisted.
	 * <p>
	 * A classpath variable provides an indirection level for better sharing a classpath. As an example, it allows
	 * a classpath to no longer refer directly to external JARs located in some user specific location. The classpath
	 * can simply refer to some variables defining the proper locations of these external JARs.
	 * <p>
	 * The boolean argument <code>ignoreUnresolvedVariable</code> allows to specify how to handle unresolvable variables,
	 * when set to <code>true</code>, missing variables are simply ignored, the resulting path is then only formed of the
	 * resolvable entries, without any indication about which variable(s) was ignored. When set to <code>false</code>, a
	 * JavaModelException will be thrown for the first unresolved variable (from left to right).
	 * 
	 * @param ignoreUnresolvedVariable specify how to handle unresolvable variables
	 * @return the resolved classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries
	 * @exception JavaModelException in one of the corresponding situation:
	 * <ul>
	 *    <li> this element does not exist </li>
	 *    <li> an exception occurs while accessing its corresponding resource </li>
	 *    <li> a classpath variable was not resolvable and <code>ignoreUnresolvedVariable</code> was set to <code>false</code>. </li>
	 * </ul>
	 * @see IClasspathEntry 
	 */
	IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedVariable) throws JavaModelException;

	/**
	 * Returns whether this project has been built at least once and thus whether it has a build state.
	 * @return true if this project has been built at least once, false otherwise
	 */
	boolean hasBuildState();

	/**
	 * Returns whether setting this project's classpath to the given classpath entries
	 * would result in a cycle.
	 *
	 * If the set of entries contains some variables, those are resolved in order to determine
	 * cycles.
	 * 
	 * @param entries the given classpath entries
	 * @return true if the given classpath entries would result in a cycle, false otherwise
	 */
	boolean hasClasspathCycle(IClasspathEntry[] entries);
	/**
	 * Returns whether the given element is on the classpath of this project, 
	 * that is, referenced from a classpath entry and not explicitly excluded
	 * using an exclusion pattern. 
	 * 
	 * @param element the given element
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return <code>true</code> if the given element is on the classpath of
	 * this project, <code>false</code> otherwise
	 * @see IClasspathEntry#getExclusionPatterns
	 * @since 2.0
	 */
	boolean isOnClasspath(IJavaElement element) throws JavaModelException;
	/**
	 * Returns whether the given resource is on the classpath of this project,
	 * that is, referenced from a classpath entry and not explicitly excluded
	 * using an exclusion pattern.
	 * 
	 * @param element the given element
	 * @exception JavaModelException if this project does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @return <code>true</code> if the given resource is on the classpath of
	 * this project, <code>false</code> otherwise
	 * @see IClasspathEntry#getExclusionPatterns
	 * @since 2.1
	 */
	boolean isOnClasspath(IResource resource) throws JavaModelException;

	/**
	 * Creates a new evaluation context.
	 * @return a new evaluation context.
	 */
	IEvaluationContext newEvaluationContext();

	/**
	 * Creates and returns a type hierarchy for all types in the given
	 * region, considering subtypes within that region.
	 *
	 * @param monitor the given progress monitor
	 * @param region the given region
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 * @exception IllegalArgumentException if region is <code>null</code>
	 * @return a type hierarchy for all types in the given
	 * region, considering subtypes within that region
	 */
	ITypeHierarchy newTypeHierarchy(IRegion region, IProgressMonitor monitor)
		throws JavaModelException;

	/**
	 * Creates and returns a type hierarchy for the given type considering
	 * subtypes in the specified region.
	 * 
	 * @param monitor the given monitor
	 * @param region the given region
	 * @param type the given type
	 * 
	 * @exception JavaModelException if this element does not exist or if an
	 *		exception occurs while accessing its corresponding resource
	 *
	 * @exception IllegalArgumentException if type or region is <code>null</code>
	 * @return a type hierarchy for the given type considering
	 * subtypes in the specified region
	 */
	ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException;

	/**
	 * Sets the project custom options. All and only the options explicitly included in the given table 
	 * are remembered; all previous option settings are forgotten, including ones not explicitly
	 * mentioned.
	 * <p>
	 * For a complete description of the configurable options, see <code>JavaCore#getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
	 *   or <code>null</code> to flush all custom options (clients will automatically get the global JavaCore options).
	 * @see JavaCore#getDefaultOptions
	 * @since 2.1
	 */
	void setOptions(Map newOptions);

	/**
	 * Sets the default output location of this project to the location
	 * described by the given workspace-relative absolute path.
	 * <p>
	 * The default output location is where class files are ordinarily generated
	 * (and resource files, copied). Each source classpath entries can also
	 * specify an output location for the generated class files (and copied
	 * resource files) corresponding to compilation units under that source
	 * folder. This makes it possible to arrange that generated class files for
	 * different source folders to end up in different output folders, and not
	 * necessarily the default output folder. This means that the generated
	 * class files for the project may end up scattered across several folders,
	 * rather than all in the default output folder (which is more standard).
	 * </p>
	 * 
	 * @param path the workspace-relative absolute path of the default output
	 * folder
	 * @param monitor the progress monitor
	 * 
	 * @exception JavaModelException if the classpath could not be set. Reasons include:
	 * <ul>
	 *  <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 *  <li> The path refers to a location not contained in this project (<code>PATH_OUTSIDE_PROJECT</code>)
	 *  <li> The path is not an absolute path (<code>RELATIVE_PATH</code>)
	 *  <li> The path is nested inside a package fragment root of this project (<code>INVALID_PATH</code>)
	 *  <li> The output location is being modified during resource change event notification (CORE_EXCEPTION)	 
	 * </ul>
	 * @see #getOutputLocation
     * @see IClasspathEntry#getOutputLocation
	 */
	void setOutputLocation(IPath path, IProgressMonitor monitor)
		throws JavaModelException;

	/**
	 * Sets the classpath of this project using a list of classpath entries. In particular such a classpath may contain
	 * classpath variable entries. Classpath variable entries can be resolved individually (see <code>JavaCore#getClasspathVariable</code>),
	 * or the full classpath can be resolved at once using the helper method <code>getResolvedClasspath</code>.
	 * <p>
	 * A classpath variable provides an indirection level for better sharing a classpath. As an example, it allows
	 * a classpath to no longer refer directly to external JARs located in some user specific location. The classpath
	 * can simply refer to some variables defining the proper locations of these external JARs.
	 * <p>
	 * Setting the classpath to <code>null</code> specifies a default classpath
	 * (the project root). Setting the classpath to an empty array specifies an
	 * empty classpath.
	 * <p>
	 * If a cycle is detected while setting this classpath, an error marker will be added
	 * to the project closing the cycle.
	 * To avoid this problem, use <code>hasClasspathCycle(IClasspathEntry[] entries)</code>
	 * before setting the classpath.
	 *
	 * @param entries a list of classpath entries
	 * @param monitor the given progress monitor
	 * @exception JavaModelException if the classpath could not be set. Reasons include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> The classpath is being modified during resource change event notification (CORE_EXCEPTION)
	 * <li> The classpath failed the validation check as defined by <code>JavaConventions#validateClasspath</code>
	 * </ul>
	 * @see IClasspathEntry
	 */
	void setRawClasspath(IClasspathEntry[] entries, IProgressMonitor monitor)
		throws JavaModelException;
		
	/**
	 * Sets the both the classpath of this project and its default output
	 * location at once. The classpath is defined using a list of classpath
	 * entries. In particular, such a classpath may contain classpath variable
	 * entries. Classpath variable entries can be resolved individually (see
	 * <code>JavaCore#getClasspathVariable</code>), or the full classpath can be
	 * resolved at once using the helper method
	 * <code>getResolvedClasspath</code>.
	 * <p>
	 * A classpath variable provides an indirection level for better sharing a
	 * classpath. As an example, it allows a classpath to no longer refer
	 * directly to external JARs located in some user specific location. The
	 * classpath can simply refer to some variables defining the proper
	 * locations of these external JARs.
	 * </p>
	 * <p>
	 * Setting the classpath to <code>null</code> specifies a default classpath
	 * (the project root). Setting the classpath to an empty array specifies an
	 * empty classpath.
	 * </p>
	 * <p>
	 * If a cycle is detected while setting this classpath, an error marker will
	 * be added to the project closing the cycle. To avoid this problem, use
	 * <code>hasClasspathCycle(IClasspathEntry[] entries)</code> before setting
	 * the classpath.
	 * </p>
	 * 
	 * @param entries a list of classpath entries
	 * @param monitor the progress monitor
	 * @param outputLocation the default output location
	 * @exception JavaModelException if the classpath could not be set. Reasons
	 * include:
	 * <ul>
	 * <li> This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 * <li> Two or more entries specify source roots with the same or overlapping paths (NAME_COLLISION)
	 * <li> A entry of kind <code>CPE_PROJECT</code> refers to this project (INVALID_PATH)
	 *  <li>This Java element does not exist (ELEMENT_DOES_NOT_EXIST)</li>
	 *	<li>The output location path refers to a location not contained in this project (<code>PATH_OUTSIDE_PROJECT</code>)
	 *	<li>The output location path is not an absolute path (<code>RELATIVE_PATH</code>)
	 *  <li>The output location path is nested inside a package fragment root of this project (<code>INVALID_PATH</code>)
	 * <li> The classpath is being modified during resource change event notification (CORE_EXCEPTION)
	 * </ul>
	 * @see IClasspathEntry
	 * @since 2.0
	 */
	void setRawClasspath(IClasspathEntry[] entries, IPath outputLocation, IProgressMonitor monitor)
		throws JavaModelException;
}
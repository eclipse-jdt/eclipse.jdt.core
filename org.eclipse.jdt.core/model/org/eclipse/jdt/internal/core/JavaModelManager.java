/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.codeassist.SelectionEngine;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.hierarchy.TypeHierarchy;
import org.eclipse.jdt.internal.core.search.AbstractSearchScope;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.Util;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>JavaModelManager</code> manages instances of <code>IJavaModel</code>.
 * <code>IElementChangedListener</code>s register with the <code>JavaModelManager</code>,
 * and receive <code>ElementChangedEvent</code>s for all <code>IJavaModel</code>s.
 * <p>
 * The single instance of <code>JavaModelManager</code> is available from
 * the static method <code>JavaModelManager.getJavaModelManager()</code>.
 */
public class JavaModelManager implements ISaveParticipant { 	
 
	/**
	 * Unique handle onto the JavaModel
	 */
	final JavaModel javaModel = new JavaModel();
	
	/**
	 * Classpath variables pool
	 */
	public HashMap variables = new HashMap(5);
	public HashMap previousSessionVariables = new HashMap(5);
	private ThreadLocal variableInitializationInProgress = new ThreadLocal();
		
	/**
	 * Classpath containers pool
	 */
	public HashMap containers = new HashMap(5);
	public HashMap previousSessionContainers = new HashMap(5);
	private ThreadLocal containerInitializationInProgress = new ThreadLocal();

	public final static String CP_VARIABLE_PREFERENCES_PREFIX = JavaCore.PLUGIN_ID+".classpathVariable."; //$NON-NLS-1$
	public final static String CP_CONTAINER_PREFERENCES_PREFIX = JavaCore.PLUGIN_ID+".classpathContainer."; //$NON-NLS-1$
	public final static String CP_ENTRY_IGNORE = "##<cp entry ignore>##"; //$NON-NLS-1$

	/**
	 * Name of the extension point for contributing classpath variable initializers
	 */
	public static final String CPVARIABLE_INITIALIZER_EXTPOINT_ID = "classpathVariableInitializer" ; //$NON-NLS-1$

	/**
	 * Name of the extension point for contributing classpath container initializers
	 */
	public static final String CPCONTAINER_INITIALIZER_EXTPOINT_ID = "classpathContainerInitializer" ; //$NON-NLS-1$

	/**
	 * Name of the extension point for contributing a source code formatter
	 */
	public static final String FORMATTER_EXTPOINT_ID = "codeFormatter" ; //$NON-NLS-1$
	
	/**
	 * Special value used for recognizing ongoing initialization and breaking initialization cycles
	 */
	public final static IPath VARIABLE_INITIALIZATION_IN_PROGRESS = new Path("Variable Initialization In Progress"); //$NON-NLS-1$
	public final static IClasspathContainer CONTAINER_INITIALIZATION_IN_PROGRESS = new IClasspathContainer() {
		public IClasspathEntry[] getClasspathEntries() { return null; }
		public String getDescription() { return "Container Initialization In Progress"; } //$NON-NLS-1$
		public int getKind() { return 0; }
		public IPath getPath() { return null; }
		public String toString() { return getDescription(); }
	};
	
	private static final String BUFFER_MANAGER_DEBUG = JavaCore.PLUGIN_ID + "/debug/buffermanager" ; //$NON-NLS-1$
	private static final String INDEX_MANAGER_DEBUG = JavaCore.PLUGIN_ID + "/debug/indexmanager" ; //$NON-NLS-1$
	private static final String COMPILER_DEBUG = JavaCore.PLUGIN_ID + "/debug/compiler" ; //$NON-NLS-1$
	private static final String JAVAMODEL_DEBUG = JavaCore.PLUGIN_ID + "/debug/javamodel" ; //$NON-NLS-1$
	private static final String CP_RESOLVE_DEBUG = JavaCore.PLUGIN_ID + "/debug/cpresolution" ; //$NON-NLS-1$
	private static final String ZIP_ACCESS_DEBUG = JavaCore.PLUGIN_ID + "/debug/zipaccess" ; //$NON-NLS-1$
	private static final String DELTA_DEBUG =JavaCore.PLUGIN_ID + "/debug/javadelta" ; //$NON-NLS-1$
	private static final String DELTA_DEBUG_VERBOSE =JavaCore.PLUGIN_ID + "/debug/javadelta/verbose" ; //$NON-NLS-1$
	private static final String HIERARCHY_DEBUG = JavaCore.PLUGIN_ID + "/debug/hierarchy" ; //$NON-NLS-1$
	private static final String POST_ACTION_DEBUG = JavaCore.PLUGIN_ID + "/debug/postaction" ; //$NON-NLS-1$
	private static final String BUILDER_DEBUG = JavaCore.PLUGIN_ID + "/debug/builder" ; //$NON-NLS-1$
	private static final String COMPLETION_DEBUG = JavaCore.PLUGIN_ID + "/debug/completion" ; //$NON-NLS-1$
	private static final String SELECTION_DEBUG = JavaCore.PLUGIN_ID + "/debug/selection" ; //$NON-NLS-1$
	private static final String SEARCH_DEBUG = JavaCore.PLUGIN_ID + "/debug/search" ; //$NON-NLS-1$

	public final static ICompilationUnit[] NO_WORKING_COPY = new ICompilationUnit[0];
	
	public HashSet optionNames = new HashSet(20);

	/**
	 * Returns whether the given full path (for a package) conflicts with the output location
	 * of the given project.
	 */
	public static boolean conflictsWithOutputLocation(IPath folderPath, JavaProject project) {
		try {
			IPath outputLocation = project.getOutputLocation();
			if (outputLocation == null) {
				// in doubt, there is a conflict
				return true;
			}
			if (outputLocation.isPrefixOf(folderPath)) {
				// only allow nesting in project's output if there is a corresponding source folder
				// or if the project's output is not used (in other words, if all source folders have their custom output)
				IClasspathEntry[] classpath = project.getResolvedClasspath(true);
				boolean isOutputUsed = false;
				for (int i = 0, length = classpath.length; i < length; i++) {
					IClasspathEntry entry = classpath[i];
					if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
						if (entry.getPath().equals(outputLocation)) {
							return false;
						}
						if (entry.getOutputLocation() == null) {
							isOutputUsed = true;
						}
					}
				}
				return isOutputUsed;
			}
			return false;
		} catch (JavaModelException e) {
			// in doubt, there is a conflict
			return true;
		}
	}

	public synchronized IClasspathContainer containerGet(IJavaProject project, IPath containerPath) {	
		// check initialization in progress first
		HashSet projectInitializations = containerInitializationInProgress(project);
		if (projectInitializations.contains(containerPath)) {
			return CONTAINER_INITIALIZATION_IN_PROGRESS;
		}
		
		Map projectContainers = (Map)this.containers.get(project);
		if (projectContainers == null){
			return null;
		}
		IClasspathContainer container = (IClasspathContainer)projectContainers.get(containerPath);
		return container;
	}
	
	/*
	 * Returns the set of container paths for the given project that are being initialized in the current thread.
	 */
	private HashSet containerInitializationInProgress(IJavaProject project) {
		Map initializations = (Map)this.containerInitializationInProgress.get();
		if (initializations == null) {
			initializations = new HashMap();
			this.containerInitializationInProgress.set(initializations);
		}
		HashSet projectInitializations = (HashSet)initializations.get(project);
		if (projectInitializations == null) {
			projectInitializations = new HashSet();
			initializations.put(project, projectInitializations);
		}
		return projectInitializations;
	}

	public synchronized void containerPut(IJavaProject project, IPath containerPath, IClasspathContainer container){

		// set/unset the initialization in progress
		HashSet projectInitializations = containerInitializationInProgress(project);
		if (container == CONTAINER_INITIALIZATION_IN_PROGRESS) {
			projectInitializations.add(containerPath);
			
			// do not write out intermediate initialization value
			return;
		} else {
			projectInitializations.remove(containerPath);

			Map projectContainers = (Map)this.containers.get(project);
			if (projectContainers == null){
				projectContainers = new HashMap(1);
				this.containers.put(project, projectContainers);
			}
	
			if (container == null) {
				projectContainers.remove(containerPath);
				Map previousContainers = (Map)this.previousSessionContainers.get(project);
				if (previousContainers != null){
					previousContainers.remove(containerPath);
				}
			} else {
				projectContainers.put(containerPath, container);
			}
		}
		
		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
		String containerKey = CP_CONTAINER_PREFERENCES_PREFIX+project.getElementName() +"|"+containerPath;//$NON-NLS-1$
		String containerString = CP_ENTRY_IGNORE;
		try {
			if (container != null) {
				containerString = ((JavaProject)project).encodeClasspath(container.getClasspathEntries(), null, false);
			}
		} catch(JavaModelException e){
			// could not encode entry: leave it as CP_ENTRY_IGNORE
		}
		preferences.setDefault(containerKey, CP_ENTRY_IGNORE); // use this default to get rid of removed ones
		preferences.setValue(containerKey, containerString);
		JavaCore.getPlugin().savePluginPreferences();
	}

	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *  <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
	 *			or <code>IPackageFragment</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IResource resource, IJavaProject project) {
		if (resource == null) {
			return null;
		}
		int type = resource.getType();
		switch (type) {
			case IResource.PROJECT :
				return JavaCore.create((IProject) resource);
			case IResource.FILE :
				return create((IFile) resource, project);
			case IResource.FOLDER :
				return create((IFolder) resource, project);
			case IResource.ROOT :
				return JavaCore.create((IWorkspaceRoot) resource);
			default :
				return null;
		}
	}

	/**
	 * Returns the Java element corresponding to the given file, its project being the given
	 * project.
	 * Returns <code>null</code> if unable to associate the given file
	 * with a Java element.
	 *
	 * <p>The file must be one of:<ul>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFile file, IJavaProject project) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
	
		if (file.getFileExtension() != null) {
			String name = file.getName();
			if (org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(name))
				return createCompilationUnitFrom(file, project);
			if (org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(name))
				return createClassFileFrom(file, project);
			if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(name))
				return createJarPackageFragmentRootFrom(file, project);
		}
		return null;
	}

	/**
	 * Returns the package fragment or package fragment root corresponding to the given folder,
	 * its parent or great parent being the given project. 
	 * or <code>null</code> if unable to associate the given folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 */
	public static IJavaElement create(IFolder folder, IJavaProject project) {
		if (folder == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(folder.getProject());
		}
		IJavaElement element = determineIfOnClasspath(folder, project);
		if (conflictsWithOutputLocation(folder.getFullPath(), (JavaProject)project)
		 	|| (folder.getName().indexOf('.') >= 0 
		 		&& !(element instanceof IPackageFragmentRoot))) {
			return null; // only package fragment roots are allowed with dot names
		} else {
			return element;
		}
	}

	/**
	 * Creates and returns a class file element for the given <code>.class</code> file,
	 * its project being the given project. Returns <code>null</code> if unable
	 * to recognize the class file.
	 */
	public static IClassFile createClassFileFrom(IFile file, IJavaProject project ) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
		IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
		if (pkg == null) {
			// fix for 1FVS7WE
			// not on classpath - make the root its folder, and a default package
			IPackageFragmentRoot root = project.getPackageFragmentRoot(file.getParent());
			pkg = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
		}
		return pkg.getClassFile(file.getName());
	}
	
	/**
	 * Creates and returns a compilation unit element for the given <code>.java</code> 
	 * file, its project being the given project. Returns <code>null</code> if unable
	 * to recognize the compilation unit.
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file, IJavaProject project) {

		if (file == null) return null;

		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
		IPackageFragment pkg = (IPackageFragment) determineIfOnClasspath(file, project);
		if (pkg == null) {
			// not on classpath - make the root its folder, and a default package
			IPackageFragmentRoot root = project.getPackageFragmentRoot(file.getParent());
			pkg = root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
			
			if (VERBOSE){
				System.out.println("WARNING : creating unit element outside classpath ("+ Thread.currentThread()+"): " + file.getFullPath()); //$NON-NLS-1$//$NON-NLS-2$
			}
		}
		return pkg.getCompilationUnit(file.getName());
	}
	
	/**
	 * Creates and returns a handle for the given JAR file, its project being the given project.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * Returns <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file, IJavaProject project) {
		if (file == null) {
			return null;
		}
		if (project == null) {
			project = JavaCore.create(file.getProject());
		}
	
		// Create a jar package fragment root only if on the classpath
		IPath resourcePath = file.getFullPath();
		try {
			IClasspathEntry[] entries = ((JavaProject)project).getResolvedClasspath(true);
			for (int i = 0, length = entries.length; i < length; i++) {
				IClasspathEntry entry = entries[i];
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(file);
				}
			}
		} catch (JavaModelException e) {
			// project doesn't exist: return null
		}
		return null;
	}
	
	/**
	 * Returns the package fragment root represented by the resource, or
	 * the package fragment the given resource is located in, or <code>null</code>
	 * if the given resource is not on the classpath of the given project.
	 */
	public static IJavaElement determineIfOnClasspath(
		IResource resource,
		IJavaProject project) {
			
		IPath resourcePath = resource.getFullPath();
		try {
			IClasspathEntry[] entries = 
				org.eclipse.jdt.internal.compiler.util.Util.isJavaFileName(resourcePath.lastSegment())
					? project.getRawClasspath() // JAVA file can only live inside SRC folder (on the raw path)
					: ((JavaProject)project).getResolvedClasspath(true);
				
			for (int i = 0; i < entries.length; i++) {
				IClasspathEntry entry = entries[i];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
				IPath rootPath = entry.getPath();
				if (rootPath.equals(resourcePath)) {
					return project.getPackageFragmentRoot(resource);
				} else if (rootPath.isPrefixOf(resourcePath)) {
					// allow creation of package fragment if it contains a .java file that is included
					if (!Util.isExcluded(resource, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars())) {
						// given we have a resource child of the root, it cannot be a JAR pkg root
						IPackageFragmentRoot root = ((JavaProject) project).getFolderPackageFragmentRoot(rootPath);
						if (root == null) return null;
						IPath pkgPath = resourcePath.removeFirstSegments(rootPath.segmentCount());
	
						if (resource.getType() == IResource.FILE) {
							// if the resource is a file, then remove the last segment which
							// is the file name in the package
							pkgPath = pkgPath.removeLastSegments(1);
						}
						String pkgName = Util.packageName(pkgPath);
						if (pkgName == null || JavaConventions.validatePackageName(pkgName).getSeverity() == IStatus.ERROR) {
							return null;
						}
						return root.getPackageFragment(pkgName);
					}
				}
			}
		} catch (JavaModelException npe) {
			return null;
		}
		return null;
	}
	
	/**
	 * The singleton manager
	 */
	private final static JavaModelManager MANAGER= new JavaModelManager();

	/**
	 * Infos cache.
	 */
	protected JavaModelCache cache = new JavaModelCache();
	
	/*
	 * Temporary cache of newly opened elements
	 */
	private ThreadLocal temporaryCache = new ThreadLocal();

	/**
	 * Set of elements which are out of sync with their buffers.
	 */
	protected Map elementsOutOfSynchWithBuffers = new HashMap(11);
	
	/**
	 * Holds the state used for delta processing.
	 */
	public DeltaProcessingState deltaState = new DeltaProcessingState();

	public IndexManager indexManager = new IndexManager();
	
	/**
	 * Table from IProject to PerProjectInfo.
	 * NOTE: this object itself is used as a lock to synchronize creation/removal of per project infos
	 */
	protected Map perProjectInfos = new HashMap(5);
	
	/**
	 * Table from WorkingCopyOwner to a table of ICompilationUnit (working copy handle) to PerWorkingCopyInfo.
	 * NOTE: this object itself is used as a lock to synchronize creation/removal of per working copy infos
	 */
	protected Map perWorkingCopyInfos = new HashMap(5);
	
	/**
	 * A weak set of the known search scopes.
	 */
	protected WeakHashMap searchScopes = new WeakHashMap();

	public static class PerProjectInfo {
		
		public IProject project;
		public Object savedState;
		public boolean triedRead;
		public IClasspathEntry[] rawClasspath;
		public IClasspathEntry[] resolvedClasspath;
		public Map resolvedPathToRawEntries; // reverse map from resolved path to raw entries
		public IPath outputLocation;
		public Preferences preferences;
		
		public PerProjectInfo(IProject project) {

			this.triedRead = false;
			this.savedState = null;
			this.project = project;
		}
		
		// updating raw classpath need to flush obsoleted cached information about resolved entries
		public synchronized void updateClasspathInformation(IClasspathEntry[] newRawClasspath) {

			this.rawClasspath = newRawClasspath;
			this.resolvedClasspath = null;
			this.resolvedPathToRawEntries = null;
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(this.project.getFullPath());
			buffer.append("\nRaw classpath:\n"); //$NON-NLS-1$
			if (this.rawClasspath == null) {
				buffer.append("  <null>\n"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.rawClasspath.length; i < length; i++) {
					buffer.append("  "); //$NON-NLS-1$
					buffer.append(this.rawClasspath[i]);
					buffer.append('\n');
				}
			}
			buffer.append("Resolved classpath:\n"); //$NON-NLS-1$
			if (this.resolvedClasspath == null) {
				buffer.append("  <null>\n"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.resolvedClasspath.length; i < length; i++) {
					buffer.append("  "); //$NON-NLS-1$
					buffer.append(this.resolvedClasspath[i]);
					buffer.append('\n');
				}
			}
			buffer.append("Output location:\n  "); //$NON-NLS-1$
			if (this.outputLocation == null) {
				buffer.append("<null>"); //$NON-NLS-1$
			} else {
				buffer.append(this.outputLocation);
			}
			return buffer.toString();
		}
	}
	
	public static class PerWorkingCopyInfo implements IProblemRequestor {
		int useCount = 0;
		IProblemRequestor problemRequestor;
		ICompilationUnit workingCopy;
		public PerWorkingCopyInfo(ICompilationUnit workingCopy, IProblemRequestor problemRequestor) {
			this.workingCopy = workingCopy;
			this.problemRequestor = problemRequestor;
		}
		public void acceptProblem(IProblem problem) {
			if (this.problemRequestor == null) return;
			this.problemRequestor.acceptProblem(problem);
		}
		public void beginReporting() {
			if (this.problemRequestor == null) return;
			this.problemRequestor.beginReporting();
		}
		public void endReporting() {
			if (this.problemRequestor == null) return;
			this.problemRequestor.endReporting();
		}
		public ICompilationUnit getWorkingCopy() {
			return this.workingCopy;
		}
		public boolean isActive() {
			return this.problemRequestor != null && this.problemRequestor.isActive();
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			buffer.append("Info for "); //$NON-NLS-1$
			buffer.append(((JavaElement)workingCopy).toStringWithAncestors());
			buffer.append("\nUse count = "); //$NON-NLS-1$
			buffer.append(this.useCount);
			buffer.append("\nProblem requestor:\n  "); //$NON-NLS-1$
			buffer.append(this.problemRequestor);
			return buffer.toString();
		}
	}
	
	public static boolean VERBOSE = false;
	public static boolean CP_RESOLVE_VERBOSE = false;
	public static boolean ZIP_ACCESS_VERBOSE = false;
	
	/**
	 * A cache of opened zip files per thread.
	 * (for a given thread, the object value is a HashMap from IPath to java.io.ZipFile)
	 */
	private ThreadLocal zipFiles = new ThreadLocal();
	
	
	/**
	 * Update the classpath variable cache
	 */
	public static class PluginPreferencesListener implements Preferences.IPropertyChangeListener {
		/**
		 * @see org.eclipse.core.runtime.Preferences.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
		 */
		public void propertyChange(Preferences.PropertyChangeEvent event) {

			String propertyName = event.getProperty();
			if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)) {
				String varName = propertyName.substring(CP_VARIABLE_PREFERENCES_PREFIX.length());
				String newValue = (String)event.getNewValue();
				if (newValue != null && !(newValue = newValue.trim()).equals(CP_ENTRY_IGNORE)) {
					getJavaModelManager().variables.put(varName, new Path(newValue));
				} else {
					getJavaModelManager().variables.remove(varName);
				}
			}
			if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)) {
				recreatePersistedContainer(propertyName, (String)event.getNewValue(), false);
			}
		}
	}

	/**
	 * Constructs a new JavaModelManager
	 */
	private JavaModelManager() {
		// singleton: prevent others from creating a new instance
	}

	/**
	 * Starts caching ZipFiles.
	 * Ignores if there are already clients.
	 */
	public void cacheZipFiles() {
		if (this.zipFiles.get() != null) return;
		this.zipFiles.set(new HashMap());
	}
	public void closeZipFile(ZipFile zipFile) {
		if (zipFile == null) return;
		if (this.zipFiles.get() != null) {
			return; // zip file will be closed by call to flushZipFiles
		}
		try {
			if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [JavaModelManager.closeZipFile(ZipFile)] Closing ZipFile on " +zipFile.getName()); //$NON-NLS-1$	//$NON-NLS-2$
			}
			zipFile.close();
		} catch (IOException e) {
			// problem occured closing zip file: cannot do much more
		}
	}

	/**
	 * Configure the plugin with respect to option settings defined in ".options" file
	 */
	public void configurePluginDebugOptions(){
		if(JavaCore.getPlugin().isDebugging()){
			String option = Platform.getDebugOption(BUFFER_MANAGER_DEBUG);
			if(option != null) BufferManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(BUILDER_DEBUG);
			if(option != null) JavaBuilder.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(COMPILER_DEBUG);
			if(option != null) Compiler.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(COMPLETION_DEBUG);
			if(option != null) CompletionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(CP_RESOLVE_DEBUG);
			if(option != null) JavaModelManager.CP_RESOLVE_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(DELTA_DEBUG);
			if(option != null) DeltaProcessor.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(DELTA_DEBUG_VERBOSE);
			if(option != null) DeltaProcessor.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(HIERARCHY_DEBUG);
			if(option != null) TypeHierarchy.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(INDEX_MANAGER_DEBUG);
			if(option != null) JobManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			
			option = Platform.getDebugOption(JAVAMODEL_DEBUG);
			if(option != null) JavaModelManager.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(POST_ACTION_DEBUG);
			if(option != null) JavaModelOperation.POST_ACTION_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(SEARCH_DEBUG);
			if(option != null) SearchEngine.VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(SELECTION_DEBUG);
			if(option != null) SelectionEngine.DEBUG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$

			option = Platform.getDebugOption(ZIP_ACCESS_DEBUG);
			if(option != null) JavaModelManager.ZIP_ACCESS_VERBOSE = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
		}
	}
	
	/*
	 * Discards the per working copy info for the given working copy (making it a compilation unit)
	 * if its use count was 1. Otherwise, just decrement the use count.
	 * If the working copy is primary, computes the delta between its state and the original compilation unit
	 * and register it.
	 * Close the working copy, its buffer and remove it from the shared working copy table.
	 * Ignore if no per-working copy info existed.
	 * NOTE: it must be synchronized as it may interact with the element info cache (if useCount is decremented to 0), see bug 50667.
	 * Returns the new use count (or -1 if it didn't exist).
	 */
	public synchronized int discardPerWorkingCopyInfo(CompilationUnit workingCopy) throws JavaModelException {
		synchronized(perWorkingCopyInfos) {
			WorkingCopyOwner owner = workingCopy.owner;
			Map workingCopyToInfos = (Map)this.perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null) return -1;
			
			PerWorkingCopyInfo info = (PerWorkingCopyInfo)workingCopyToInfos.get(workingCopy);
			if (info == null) return -1;
			
			if (--info.useCount == 0) {
				// create the delta builder (this remembers the current content of the working copy)
				JavaElementDeltaBuilder deltaBuilder = null;
				if (workingCopy.isPrimary()) {
					deltaBuilder = new JavaElementDeltaBuilder(workingCopy);
				}

				// remove per working copy info
				workingCopyToInfos.remove(workingCopy);
				if (workingCopyToInfos.isEmpty()) {
					this.perWorkingCopyInfos.remove(owner);
				}

				// remove infos + close buffer (since no longer working copy)
				removeInfoAndChildren(workingCopy);
				workingCopy.closeBuffer();

				// compute the delta if needed and register it if there are changes
				if (deltaBuilder != null) {
					deltaBuilder.buildDeltas();
					if ((deltaBuilder.delta != null) && (deltaBuilder.delta.getAffectedChildren().length > 0)) {
						getDeltaProcessor().registerJavaModelDelta(deltaBuilder.delta);
					}
				}
				
			}
			return info.useCount;
		}
	}
	
	/**
	 * @see ISaveParticipant
	 */
	public void doneSaving(ISaveContext context){
		// nothing to do for jdt.core
	}

	/**
	 * Flushes ZipFiles cache if there are no more clients.
	 */
	public void flushZipFiles() {
		Thread currentThread = Thread.currentThread();
		HashMap map = (HashMap)this.zipFiles.get();
		if (map == null) return;
		this.zipFiles.set(null);
		Iterator iterator = map.values().iterator();
		while (iterator.hasNext()) {
			try {
				ZipFile zipFile = (ZipFile)iterator.next();
				if (JavaModelManager.ZIP_ACCESS_VERBOSE) {
					System.out.println("(" + currentThread + ") [JavaModelManager.flushZipFiles()] Closing ZipFile on " +zipFile.getName()); //$NON-NLS-1$//$NON-NLS-2$
				}
				zipFile.close();
			} catch (IOException e) {
				// problem occured closing zip file: cannot do much more
			}
		}
	}
	
	public DeltaProcessor getDeltaProcessor() {
		return this.deltaState.getDeltaProcessor();
	}
	
	/** 
	 * Returns the set of elements which are out of synch with their buffers.
	 */
	protected Map getElementsOutOfSynchWithBuffers() {
		return this.elementsOutOfSynchWithBuffers;
	}

	public IndexManager getIndexManager() {
		return this.indexManager;
	}

	/**
	 *  Returns the info for the element.
	 */
	public synchronized Object getInfo(IJavaElement element) {
		HashMap tempCache = (HashMap)this.temporaryCache.get();
		if (tempCache != null) {
			Object result = tempCache.get(element);
			if (result != null) {
				return result;
			}
		}
		return this.cache.getInfo(element);
	}

	/**
	 * Returns the handle to the active Java Model.
	 */
	public final JavaModel getJavaModel() {
		return javaModel;
	}

	/**
	 * Returns the singleton JavaModelManager
	 */
	public final static JavaModelManager getJavaModelManager() {
		return MANAGER;
	}

	/**
	 * Returns the last built state for the given project, or null if there is none.
	 * Deserializes the state if necessary.
	 *
	 * For use by image builder and evaluation support only
	 */
	public Object getLastBuiltState(IProject project, IProgressMonitor monitor) {
		if (!JavaProject.hasJavaNature(project)) return null; // should never be requested on non-Java projects
		PerProjectInfo info = getPerProjectInfo(project, true/*create if missing*/);
		if (!info.triedRead) {
			info.triedRead = true;
			try {
				if (monitor != null)
					monitor.subTask(Util.bind("build.readStateProgress", project.getName())); //$NON-NLS-1$
				info.savedState = readState(project);
			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
		return info.savedState;
	}

	/*
	 * Returns the per-project info for the given project. If specified, create the info if the info doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfo(IProject project, boolean create) {
		synchronized(perProjectInfos) { // use the perProjectInfo collection as its own lock
			PerProjectInfo info= (PerProjectInfo) perProjectInfos.get(project);
			if (info == null && create) {
				info= new PerProjectInfo(project);
				perProjectInfos.put(project, info);
			}
			return info;
		}
	}	
	
	/*
	 * Returns  the per-project info for the given project.
	 * If the info doesn't exist, check for the project existence and create the info.
	 * @throws JavaModelException if the project doesn't exist.
	 */
	public PerProjectInfo getPerProjectInfoCheckExistence(IProject project) throws JavaModelException {
		JavaModelManager.PerProjectInfo info = getPerProjectInfo(project, false /* don't create info */);
		if (info == null) {
			if (!JavaProject.hasJavaNature(project)) {
				throw ((JavaProject)JavaCore.create(project)).newNotPresentException();
			}
			info = getPerProjectInfo(project, true /* create info */);
		}
		return info;
	}
	
	/*
	 * Returns the per-working copy info for the given working copy at the given path.
	 * If it doesn't exist and if create, add a new per-working copy info with the given problem requestor.
	 * If recordUsage, increment the per-working copy info's use count.
	 * Returns null if it doesn't exist and not create.
	 */
	public PerWorkingCopyInfo getPerWorkingCopyInfo(CompilationUnit workingCopy,boolean create, boolean recordUsage, IProblemRequestor problemRequestor) {
		synchronized(perWorkingCopyInfos) { // use the perWorkingCopyInfo collection as its own lock
			WorkingCopyOwner owner = workingCopy.owner;
			Map workingCopyToInfos = (Map)this.perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null && create) {
				workingCopyToInfos = new HashMap();
				this.perWorkingCopyInfos.put(owner, workingCopyToInfos);
			}

			PerWorkingCopyInfo info = workingCopyToInfos == null ? null : (PerWorkingCopyInfo) workingCopyToInfos.get(workingCopy);
			if (info == null && create) {
				info= new PerWorkingCopyInfo(workingCopy, problemRequestor);
				workingCopyToInfos.put(workingCopy, info);
			}
			if (info != null && recordUsage) info.useCount++;
			return info;
		}
	}	
	
	/*
	 * Returns the temporary cache for newly opened elements for the current thread.
	 * Creates it if not already created.
	 */
	public HashMap getTemporaryCache() {
		HashMap result = (HashMap)this.temporaryCache.get();
		if (result == null) {
			result = new HashMap();
			this.temporaryCache.set(result);
		}
		return result;
	}

	/**
 	 * Returns the name of the variables for which an CP variable initializer is registered through an extension point
 	 */
	public static String[] getRegisteredVariableNames(){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		ArrayList variableList = new ArrayList(5);
		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for(int j = 0; j < configElements.length; j++){
					String varAttribute = configElements[j].getAttribute("variable"); //$NON-NLS-1$
					if (varAttribute != null) variableList.add(varAttribute);
				}
			}	
		}
		String[] variableNames = new String[variableList.size()];
		variableList.toArray(variableNames);
		return variableNames;
	}	

	/**
 	 * Returns the name of the container IDs for which an CP container initializer is registered through an extension point
 	 */
	public static String[] getRegisteredContainerIDs(){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		ArrayList containerIDList = new ArrayList(5);
		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for(int j = 0; j < configElements.length; j++){
					String idAttribute = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (idAttribute != null) containerIDList.add(idAttribute);
				}
			}	
		}
		String[] containerIDs = new String[containerIDList.size()];
		containerIDList.toArray(containerIDs);
		return containerIDs;
	}	

	/**
	 * Returns the File to use for saving and restoring the last built state for the given project.
	 */
	private File getSerializationFile(IProject project) {
		if (!project.exists()) return null;
		IPluginDescriptor descr= JavaCore.getJavaCore().getDescriptor();
		IPath workingLocation= project.getPluginWorkingLocation(descr);
		return workingLocation.append("state.dat").toFile(); //$NON-NLS-1$
	}
	
	/*
	 * Returns all the working copies which have the given owner.
	 * Adds the working copies of the primary owner if specified.
	 * Returns null if it has none.
	 */
	public ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner, boolean addPrimary) {
		synchronized(perWorkingCopyInfos) {
			ICompilationUnit[] primaryWCs = addPrimary && owner != DefaultWorkingCopyOwner.PRIMARY 
				? getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, false) 
				: null;
			Map workingCopyToInfos = (Map)perWorkingCopyInfos.get(owner);
			if (workingCopyToInfos == null) return primaryWCs;
			int primaryLength = primaryWCs == null ? 0 : primaryWCs.length;
			int size = workingCopyToInfos.size(); // note size is > 0 otherwise pathToPerWorkingCopyInfos would be null
			ICompilationUnit[] result = new ICompilationUnit[primaryLength + size];
			if (primaryWCs != null) {
				System.arraycopy(primaryWCs, 0, result, 0, primaryLength);
			}
			Iterator iterator = workingCopyToInfos.values().iterator();
			int index = primaryLength;
			while(iterator.hasNext()) {
				result[index++] = ((JavaModelManager.PerWorkingCopyInfo)iterator.next()).getWorkingCopy();
			}
			return result;
		}		
	}
	
	/**
	 * Returns the open ZipFile at the given location. If the ZipFile
	 * does not yet exist, it is created, opened, and added to the cache
	 * of open ZipFiles. The path must be absolute.
	 *
	 * @exception CoreException If unable to create/open the ZipFile
	 */
	public ZipFile getZipFile(IPath path) throws CoreException {
			
		HashMap map;
		ZipFile zipFile;
		if ((map = (HashMap)this.zipFiles.get()) != null 
				&& (zipFile = (ZipFile)map.get(path)) != null) {
				
			return zipFile;
		}
		String fileSystemPath= null;
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource file = root.findMember(path);
		if (file != null) {
			// internal resource
			IPath location;
			if (file.getType() != IResource.FILE || (location = file.getLocation()) == null) {
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("file.notFound", path.toString()), null)); //$NON-NLS-1$
			}
			fileSystemPath= location.toOSString();
		} else {
			// external resource
			fileSystemPath= path.toOSString();
		}

		try {
			if (ZIP_ACCESS_VERBOSE) {
				System.out.println("(" + Thread.currentThread() + ") [JavaModelManager.getZipFile(IPath)] Creating ZipFile on " + fileSystemPath ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			zipFile = new ZipFile(fileSystemPath);
			if (map != null) {
				map.put(path, zipFile);
			}
			return zipFile;
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, Util.bind("status.IOException"), e)); //$NON-NLS-1$
		}
	}
	
	/*
	 * Returns whether there is a temporary cache for the current thread.
	 */
	public boolean hasTemporaryCache() {
		return this.temporaryCache.get() != null;
	}

	public void loadVariablesAndContainers() throws CoreException {

		// backward compatibility, consider persistent property	
		QualifiedName qName = new QualifiedName(JavaCore.PLUGIN_ID, "variables"); //$NON-NLS-1$
		String xmlString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
		
		try {
			if (xmlString != null){
				StringReader reader = new StringReader(xmlString);
				Element cpElement;
				try {
					DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
					cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
				} catch(SAXException e) {
					return;
				} catch(ParserConfigurationException e){
					return;
				} finally {
					reader.close();
				}
				if (cpElement == null) return;
				if (!cpElement.getNodeName().equalsIgnoreCase("variables")) { //$NON-NLS-1$
					return;
				}
				
				NodeList list= cpElement.getChildNodes();
				int length= list.getLength();
				for (int i= 0; i < length; ++i) {
					Node node= list.item(i);
					short type= node.getNodeType();
					if (type == Node.ELEMENT_NODE) {
						Element element= (Element) node;
						if (element.getNodeName().equalsIgnoreCase("variable")) { //$NON-NLS-1$
							variablePut( 
								element.getAttribute("name"), //$NON-NLS-1$
								new Path(element.getAttribute("path"))); //$NON-NLS-1$
						}
					}
				}
			}
		} catch(IOException e){
			// problem loading xml file: nothing we can do
		} finally {
			if (xmlString != null){
				ResourcesPlugin.getWorkspace().getRoot().setPersistentProperty(qName, null); // flush old one
			}
			
		}
		
		// load variables and containers from preferences into cache
		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();

		// only get variable from preferences not set to their default
		String[] propertyNames = preferences.propertyNames();
		int variablePrefixLength = CP_VARIABLE_PREFERENCES_PREFIX.length();
		for (int i = 0; i < propertyNames.length; i++){
			String propertyName = propertyNames[i];
			if (propertyName.startsWith(CP_VARIABLE_PREFERENCES_PREFIX)){
				String varName = propertyName.substring(variablePrefixLength);
				IPath varPath = new Path(preferences.getString(propertyName).trim());
				
				this.variables.put(varName, varPath); 
				this.previousSessionVariables.put(varName, varPath);
			}
			if (propertyName.startsWith(CP_CONTAINER_PREFERENCES_PREFIX)){
				recreatePersistedContainer(propertyName, preferences.getString(propertyName), true/*add to container values*/);
			}
		}
		// override persisted values for variables which have a registered initializer
		String[] registeredVariables = getRegisteredVariableNames();
		for (int i = 0; i < registeredVariables.length; i++) {
			String varName = registeredVariables[i];
			this.variables.put(varName, null); // reset variable, but leave its entry in the Map, so it will be part of variable names.
		}
		// override persisted values for containers which have a registered initializer
		String[] registeredContainerIDs = getRegisteredContainerIDs();
		for (int i = 0; i < registeredContainerIDs.length; i++) {
			String containerID = registeredContainerIDs[i];
			Iterator projectIterator = this.containers.keySet().iterator();
			while (projectIterator.hasNext()){
				IJavaProject project = (IJavaProject)projectIterator.next();
				Map projectContainers = (Map)this.containers.get(project);
				if (projectContainers != null){
					Iterator containerIterator = projectContainers.keySet().iterator();
					while (containerIterator.hasNext()){
						IPath containerPath = (IPath)containerIterator.next();
						if (containerPath.segment(0).equals(containerID)) { // registered container
							projectContainers.put(containerPath, null); // reset container value, but leave entry in Map
						}
					}
				}
			}
		}
	}

	/**
	 *  Returns the info for this element without
	 *  disturbing the cache ordering.
	 */
	protected synchronized Object peekAtInfo(IJavaElement element) {
		HashMap tempCache = (HashMap)this.temporaryCache.get();
		if (tempCache != null) {
			Object result = tempCache.get(element);
			if (result != null) {
				return result;
			}
		}
		return this.cache.peekAtInfo(element);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void prepareToSave(ISaveContext context) /*throws CoreException*/ {
		// nothing to do
	}
	/*
	 * Puts the infos in the given map (keys are IJavaElements and values are JavaElementInfos)
	 * in the Java model cache in an atomic way.
	 * First checks that the info for the opened element (or one of its ancestors) has not been 
	 * added to the cache. If it is the case, another thread has opened the element (or one of
	 * its ancestors). So returns without updating the cache.
	 */
	protected synchronized void putInfos(IJavaElement openedElement, Map newElements) {
		while (openedElement != null) {
			if (!newElements.containsKey(openedElement)) {
				break;
			}
			if (this.cache.peekAtInfo(openedElement) != null) {
				return;
			}
			openedElement = openedElement.getParent();
		}
		
		Iterator iterator = newElements.keySet().iterator();
		while (iterator.hasNext()) {
			IJavaElement element = (IJavaElement)iterator.next();
			Object info = newElements.get(element);
			this.cache.putInfo(element, info);
		}
	}

	/**
	 * Reads the build state for the relevant project.
	 */
	protected Object readState(IProject project) throws CoreException {
		File file = getSerializationFile(project);
		if (file != null && file.exists()) {
			try {
				DataInputStream in= new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
				try {
					String pluginID= in.readUTF();
					if (!pluginID.equals(JavaCore.PLUGIN_ID))
						throw new IOException(Util.bind("build.wrongFileFormat")); //$NON-NLS-1$
					String kind= in.readUTF();
					if (!kind.equals("STATE")) //$NON-NLS-1$
						throw new IOException(Util.bind("build.wrongFileFormat")); //$NON-NLS-1$
					if (in.readBoolean())
						return JavaBuilder.readState(project, in);
					if (JavaBuilder.DEBUG)
						System.out.println("Saved state thinks last build failed for " + project.getName()); //$NON-NLS-1$
				} finally {
					in.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR, "Error reading last build state for project "+ project.getName(), e)); //$NON-NLS-1$
			}
		}
		return null;
	}

	public static void recreatePersistedContainer(String propertyName, String containerString, boolean addToContainerValues) {
		int containerPrefixLength = CP_CONTAINER_PREFERENCES_PREFIX.length();
		int index = propertyName.indexOf('|', containerPrefixLength);
		if (containerString != null) containerString = containerString.trim();
		if (index > 0) {
			final String projectName = propertyName.substring(containerPrefixLength, index).trim();
			JavaProject project = (JavaProject)getJavaModelManager().getJavaModel().getJavaProject(projectName);
			final IPath containerPath = new Path(propertyName.substring(index+1).trim());
			
			if (containerString == null || containerString.equals(CP_ENTRY_IGNORE)) {
				getJavaModelManager().containerPut(project, containerPath, null);
			} else {
				final IClasspathEntry[] containerEntries = project.decodeClasspath(containerString, false, false);
				if (containerEntries != null && containerEntries != JavaProject.INVALID_CLASSPATH) {
					IClasspathContainer container = new IClasspathContainer() {
						public IClasspathEntry[] getClasspathEntries() {
							return containerEntries;
						}
						public String getDescription() {
							return "Persisted container ["+containerPath+" for project ["+ projectName+"]"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						}
						public int getKind() {
							return 0; 
						}
						public IPath getPath() {
							return containerPath;
						}
						public String toString() {
							return getDescription();
						}

					};
					if (addToContainerValues) {
						getJavaModelManager().containerPut(project, containerPath, container);
					}
					Map projectContainers = (Map)getJavaModelManager().previousSessionContainers.get(project);
					if (projectContainers == null){
						projectContainers = new HashMap(1);
						getJavaModelManager().previousSessionContainers.put(project, projectContainers);
					}
					projectContainers.put(containerPath, container);
				}
			}
		}
	}
	
	/**
	 * Remembers the given scope in a weak set
	 * (so no need to remove it: it will be removed by the garbage collector)
	 */
	public void rememberScope(AbstractSearchScope scope) {
		// NB: The value has to be null so as to not create a strong reference on the scope
		this.searchScopes.put(scope, null); 
	}
	
	/*
	 * Removes all cached info for the given element (including all children)
	 * from the cache.
	 * Returns the info for the given element, or null if it was closed.
	 */
	public synchronized Object removeInfoAndChildren(JavaElement element) throws JavaModelException {
		Object info = peekAtInfo(element);
		if (info != null) {
			boolean wasVerbose = false;
			try {
				if (VERBOSE) {
					System.out.println("CLOSING Element ("+ Thread.currentThread()+"): " + element.toStringWithAncestors());  //$NON-NLS-1$//$NON-NLS-2$
					wasVerbose = true;
					VERBOSE = false;
				}
				element.closing(info);
				if (element instanceof IParent && info instanceof JavaElementInfo) {
					IJavaElement[] children = ((JavaElementInfo)info).getChildren();
					for (int i = 0, size = children.length; i < size; ++i) {
						JavaElement child = (JavaElement) children[i];
						child.close();
					}
				}
				this.cache.removeInfo(element);
				if (wasVerbose) {
					System.out.println("-> Package cache size = " + this.cache.pkgSize()); //$NON-NLS-1$
					System.out.println("-> Openable cache filling ratio = " + NumberFormat.getInstance().format(this.cache.openableFillingRatio()) + "%"); //$NON-NLS-1$//$NON-NLS-2$
				}
			} finally {
				JavaModelManager.VERBOSE = wasVerbose;
			}
			return info;
		}
		return null;
	}	

	public void removePerProjectInfo(JavaProject javaProject) {
		synchronized(perProjectInfos) { // use the perProjectInfo collection as its own lock
			IProject project = javaProject.getProject();
			PerProjectInfo info= (PerProjectInfo) perProjectInfos.get(project);
			if (info != null) {
				perProjectInfos.remove(project);
			}
		}
	}
	
	/*
	 * Resets the temporary cache for newly created elements to null.
	 */
	public void resetTemporaryCache() {
		this.temporaryCache.set(null);
	}

	/**
	 * @see ISaveParticipant
	 */
	public void rollback(ISaveContext context){
		// nothing to do
	}

	private void saveState(PerProjectInfo info, ISaveContext context) throws CoreException {

		// passed this point, save actions are non trivial
		if (context.getKind() == ISaveContext.SNAPSHOT) return;
		
		// save built state
		if (info.triedRead) saveBuiltState(info);
	}
	
	/**
	 * Saves the built state for the project.
	 */
	private void saveBuiltState(PerProjectInfo info) throws CoreException {
		if (JavaBuilder.DEBUG)
			System.out.println(Util.bind("build.saveStateProgress", info.project.getName())); //$NON-NLS-1$
		File file = getSerializationFile(info.project);
		if (file == null) return;
		long t = System.currentTimeMillis();
		try {
			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(file)));
			try {
				out.writeUTF(JavaCore.PLUGIN_ID);
				out.writeUTF("STATE"); //$NON-NLS-1$
				if (info.savedState == null) {
					out.writeBoolean(false);
				} else {
					out.writeBoolean(true);
					JavaBuilder.writeState(info.savedState, out);
				}
			} finally {
				out.close();
			}
		} catch (RuntimeException e) {
			try {
				file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
			throw new CoreException(
				new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Util.bind("build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		} catch (IOException e) {
			try {
				file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
			throw new CoreException(
				new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, Platform.PLUGIN_ERROR,
					Util.bind("build.cannotSaveState", info.project.getName()), e)); //$NON-NLS-1$
		}
		if (JavaBuilder.DEBUG) {
			t = System.currentTimeMillis() - t;
			System.out.println(Util.bind("build.saveStateComplete", String.valueOf(t))); //$NON-NLS-1$
		}
	}

	/**
	 * @see ISaveParticipant
	 */
	public void saving(ISaveContext context) throws CoreException {
		
		// clean up indexes on workspace full save
		// (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=52347)
		if (context.getKind() == ISaveContext.FULL_SAVE) {
			IndexManager manager = this.indexManager;
			if (manager != null) {
				manager.cleanUpIndexes();
			}
		}
	
		IProject savedProject = context.getProject();
		if (savedProject != null) {
			if (!JavaProject.hasJavaNature(savedProject)) return; // ignore
			PerProjectInfo info = getPerProjectInfo(savedProject, true /* create info */);
			saveState(info, context);
			return;
		}

		ArrayList vStats= null; // lazy initialized
		for (Iterator iter =  perProjectInfos.values().iterator(); iter.hasNext();) {
			try {
				PerProjectInfo info = (PerProjectInfo) iter.next();
				saveState(info, context);
			} catch (CoreException e) {
				if (vStats == null)
					vStats= new ArrayList();
				vStats.add(e.getStatus());
			}
		}
		if (vStats != null) {
			IStatus[] stats= new IStatus[vStats.size()];
			vStats.toArray(stats);
			throw new CoreException(new MultiStatus(JavaCore.PLUGIN_ID, IStatus.ERROR, stats, Util.bind("build.cannotSaveStates"), null)); //$NON-NLS-1$
		}
	}

	/**
	 * Record the order in which to build the java projects (batch build). This order is based
	 * on the projects classpath settings.
	 */
	protected void setBuildOrder(String[] javaBuildOrder) throws JavaModelException {

		// optional behaviour
		// possible value of index 0 is Compute
		if (!JavaCore.COMPUTE.equals(JavaCore.getOption(JavaCore.CORE_JAVA_BUILD_ORDER))) return; // cannot be customized at project level
		
		if (javaBuildOrder == null || javaBuildOrder.length <= 1) return;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceDescription description = workspace.getDescription();
		String[] wksBuildOrder = description.getBuildOrder();

		String[] newOrder;
		if (wksBuildOrder == null){
			newOrder = javaBuildOrder;
		} else {
			// remove projects which are already mentionned in java builder order
			int javaCount = javaBuildOrder.length;
			HashMap newSet = new HashMap(javaCount); // create a set for fast check
			for (int i = 0; i < javaCount; i++){
				newSet.put(javaBuildOrder[i], javaBuildOrder[i]);
			}
			int removed = 0;
			int oldCount = wksBuildOrder.length;
			for (int i = 0; i < oldCount; i++){
				if (newSet.containsKey(wksBuildOrder[i])){
					wksBuildOrder[i] = null;
					removed++;
				}
			}
			// add Java ones first
			newOrder = new String[oldCount - removed + javaCount];
			System.arraycopy(javaBuildOrder, 0, newOrder, 0, javaCount); // java projects are built first

			// copy previous items in their respective order
			int index = javaCount;
			for (int i = 0; i < oldCount; i++){
				if (wksBuildOrder[i] != null){
					newOrder[index++] = wksBuildOrder[i];
				}
			}
		}
		// commit the new build order out
		description.setBuildOrder(newOrder);
		try {
			workspace.setDescription(description);
		} catch(CoreException e){
			throw new JavaModelException(e);
		}
	}

	/**
	 * Sets the last built state for the given project, or null to reset it.
	 */
	public void setLastBuiltState(IProject project, Object state) {
		if (JavaProject.hasJavaNature(project)) {
			// should never be requested on non-Java projects
			PerProjectInfo info = getPerProjectInfo(project, true /*create if missing*/);
			info.triedRead = true; // no point trying to re-read once using setter
			info.savedState = state;
		}
		if (state == null) { // delete state file to ensure a full build happens if the workspace crashes
			try {
				File file = getSerializationFile(project);
				if (file != null && file.exists())
					file.delete();
			} catch(SecurityException se) {
				// could not delete file: cannot do much more
			}
		}
	}

	public void shutdown () {
		if (this.indexManager != null){ // no more indexing
			this.indexManager.shutdown();
		}
		// Note: no need to close the Java model as this just removes Java element infos from the Java model cache
	}
		
	public synchronized IPath variableGet(String variableName){
		// check initialization in progress first
		HashSet initializations = variableInitializationInProgress();
		if (initializations.contains(variableName)) {
			return VARIABLE_INITIALIZATION_IN_PROGRESS;
		}
		return (IPath)this.variables.get(variableName);
	}

	/*
	 * Returns the set of variable names that are being initialized in the current thread.
	 */
	private HashSet variableInitializationInProgress() {
		HashSet initializations = (HashSet)this.variableInitializationInProgress.get();
		if (initializations == null) {
			initializations = new HashSet();
			this.variableInitializationInProgress.set(initializations);
		}
		return initializations;
	}

	public synchronized String[] variableNames(){
		int length = this.variables.size();
		String[] result = new String[length];
		Iterator vars = this.variables.keySet().iterator();
		int index = 0;
		while (vars.hasNext()) {
			result[index++] = (String) vars.next();
		}
		return result;
	}
	
	public synchronized void variablePut(String variableName, IPath variablePath){		

		// set/unset the initialization in progress
		HashSet initializations = variableInitializationInProgress();
		if (variablePath == VARIABLE_INITIALIZATION_IN_PROGRESS) {
			initializations.add(variableName);
			
			// do not write out intermediate initialization value
			return;
		} else {
			initializations.remove(variableName);

			// update cache - do not only rely on listener refresh		
			if (variablePath == null) {
				this.variables.remove(variableName);
				this.previousSessionVariables.remove(variableName);
			} else {
				this.variables.put(variableName, variablePath);
			}
		}

		Preferences preferences = JavaCore.getPlugin().getPluginPreferences();
		String variableKey = CP_VARIABLE_PREFERENCES_PREFIX+variableName;
		String variableString = variablePath == null ? CP_ENTRY_IGNORE : variablePath.toString();
		preferences.setDefault(variableKey, CP_ENTRY_IGNORE); // use this default to get rid of removed ones
		preferences.setValue(variableKey, variableString);
		JavaCore.getPlugin().savePluginPreferences();
	}
}

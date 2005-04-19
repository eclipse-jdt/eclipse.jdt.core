/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.*;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IRegion;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.eval.IEvaluationContext;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.eval.EvaluationContextWrapper;
import org.eclipse.jdt.internal.core.util.MementoTokenizer;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;
import org.eclipse.jdt.internal.eval.EvaluationContext;
import org.osgi.service.prefs.BackingStoreException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Handle for a Java Project.
 *
 * <p>A Java Project internally maintains a devpath that corresponds
 * to the project's classpath. The classpath may include source folders
 * from the current project; jars in the current project, other projects,
 * and the local file system; and binary folders (output location) of other
 * projects. The Java Model presents source elements corresponding to output
 * .class files in other projects, and thus uses the devpath rather than
 * the classpath (which is really a compilation path). The devpath mimics
 * the classpath, except has source folder entries in place of output
 * locations in external projects.
 *
 * <p>Each JavaProject has a NameLookup facility that locates elements
 * on by name, based on the devpath.
 *
 * @see IJavaProject
 */
public class JavaProject
	extends Openable
	implements IJavaProject, IProjectNature, SuffixConstants {
	
	/**
	 * Name of file containing project classpath
	 */
	public static final String CLASSPATH_FILENAME = ".classpath";  //$NON-NLS-1$
	
	/**
	 * Value of the project's raw classpath if the .classpath file contains invalid entries.
	 */
	public static final IClasspathEntry[] INVALID_CLASSPATH = new IClasspathEntry[0];

	/**
	 * Whether the underlying file system is case sensitive.
	 */
	protected static final boolean IS_CASE_SENSITIVE = !new File("Temp").equals(new File("temp")); //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An empty array of strings indicating that a project doesn't have any prerequesite projects.
	 */
	protected static final String[] NO_PREREQUISITES = new String[0];

	/**
	 * Name of file containing custom project preferences
	 * @deprecated WARNING Visibility will be reduce to private before M9
	 * 	If you use this variable, change your implementation to avoid future compilation error...
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 * TODO (frederic) set visibility from public to private
	 */
	public static final String PREF_FILENAME = ".jprefs";  //$NON-NLS-1$
	
	/*
	 * Value of project's resolved classpath while it is being resolved
	 */
	private static final IClasspathEntry[] RESOLUTION_IN_PROGRESS = new IClasspathEntry[0];

	/**
	 * The platform project this <code>IJavaProject</code> is based on
	 */
	protected IProject project;
	
	/**
	 * Returns a canonicalized path from the given external path.
	 * Note that the return path contains the same number of segments
	 * and it contains a device only if the given path contained one.
	 * @param externalPath IPath
	 * @see java.io.File for the definition of a canonicalized path
	 * @return IPath
	 */
	public static IPath canonicalizedPath(IPath externalPath) {
		
		if (externalPath == null)
			return null;

//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonicalizing " + externalPath.toString()); //$NON-NLS-1$
//		}

		if (IS_CASE_SENSITIVE) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (file system is case sensitive)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		// if not external path, return original path
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace == null) return externalPath; // protection during shutdown (30487)
		if (workspace.getRoot().findMember(externalPath) != null) {
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (member of workspace)"); //$NON-NLS-1$
//			}
			return externalPath;
		}

		IPath canonicalPath = null;
		try {
			canonicalPath =
				new Path(new File(externalPath.toOSString()).getCanonicalPath());
		} catch (IOException e) {
			// default to original path
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (IOException)"); //$NON-NLS-1$
//			}
			return externalPath;
		}
		
		IPath result;
		int canonicalLength = canonicalPath.segmentCount();
		if (canonicalLength == 0) {
			// the java.io.File canonicalization failed
//			if (JavaModelManager.VERBOSE) {
//				System.out.println("JAVA MODEL - Canonical path is original path (canonical path is empty)"); //$NON-NLS-1$
//			}
			return externalPath;
		} else if (externalPath.isAbsolute()) {
			result = canonicalPath;
		} else {
			// if path is relative, remove the first segments that were added by the java.io.File canonicalization
			// e.g. 'lib/classes.zip' was converted to 'd:/myfolder/lib/classes.zip'
			int externalLength = externalPath.segmentCount();
			if (canonicalLength >= externalLength) {
				result = canonicalPath.removeFirstSegments(canonicalLength - externalLength);
			} else {
//				if (JavaModelManager.VERBOSE) {
//					System.out.println("JAVA MODEL - Canonical path is original path (canonical path is " + canonicalPath.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
//				}
				return externalPath;
			}
		}
		
		// keep device only if it was specified (this is because File.getCanonicalPath() converts '/lib/classed.zip' to 'd:/lib/classes/zip')
		if (externalPath.getDevice() == null) {
			result = result.setDevice(null);
		} 
//		if (JavaModelManager.VERBOSE) {
//			System.out.println("JAVA MODEL - Canonical path is " + result.toString()); //$NON-NLS-1$
//		}
		return result;
	}
	
	/**
	 * Returns true if the given project is accessible and it has
	 * a java nature, otherwise false.
	 * @param project IProject
	 * @return boolean
	 */
	public static boolean hasJavaNature(IProject project) { 
		try {
			return project.hasNature(JavaCore.NATURE_ID);
		} catch (CoreException e) {
			// project does not exist or is not open
		}
		return false;
	}

	/**
	 * Update cycle markers for all java projects
	 * @param preferredClasspaths Map
	 * @throws JavaModelException
	 */
	public static void updateAllCycleMarkers(Map preferredClasspaths) throws JavaModelException {

		//long start = System.currentTimeMillis();

		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] rscProjects = workspaceRoot.getProjects();
		int length = rscProjects.length;
		JavaProject[] projects = new JavaProject[length];
				
		HashSet cycleParticipants = new HashSet();
		HashSet traversed = new HashSet();
		
		// compute cycle participants
		ArrayList prereqChain = new ArrayList();
		for (int i = 0; i < length; i++){
			if (hasJavaNature(rscProjects[i])) {
				JavaProject project = (projects[i] = (JavaProject)JavaCore.create(rscProjects[i]));
				if (!traversed.contains(project.getPath())){
					prereqChain.clear();
					project.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
				}
			}
		}
		//System.out.println("updateAllCycleMarkers: " + (System.currentTimeMillis() - start) + " ms");

		for (int i = 0; i < length; i++){
			JavaProject project = projects[i];
			if (project != null) {
				if (cycleParticipants.contains(project.getPath())){
					IMarker cycleMarker = project.getCycleMarker();
					String circularCPOption = project.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true);
					int circularCPSeverity = JavaCore.ERROR.equals(circularCPOption) ? IMarker.SEVERITY_ERROR : IMarker.SEVERITY_WARNING;
					if (cycleMarker != null) {
						// update existing cycle marker if needed
						try {
							int existingSeverity = ((Integer)cycleMarker.getAttribute(IMarker.SEVERITY)).intValue();
							if (existingSeverity != circularCPSeverity) {
								cycleMarker.setAttribute(IMarker.SEVERITY, circularCPSeverity);
							}
						} catch (CoreException e) {
							throw new JavaModelException(e);
						}
					} else {
						// create new marker
						project.createClasspathProblemMarker(
							new JavaModelStatus(IJavaModelStatusConstants.CLASSPATH_CYCLE, project)); 
					}
				} else {
					project.flushClasspathProblemMarkers(true, false);
				}			
			}
		}
	}

	/**
	 * Constructor needed for <code>IProject.getNature()</code> and <code>IProject.addNature()</code>.
	 *
	 * @see #setProject(IProject)
	 */
	public JavaProject() {
		super(null);
	}

	public JavaProject(IProject project, JavaElement parent) {
		super(parent);
		this.project = project;
	}

	/**
	 * Adds a builder to the build spec for the given project.
	 */
	protected void addToBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		int javaCommandIndex = getJavaCommandIndex(description.getBuildSpec());

		if (javaCommandIndex == -1) {

			// Add a Java command to the build spec
			ICommand command = description.newCommand();
			command.setBuilderName(builderID);
			setJavaCommand(description, command);
		}
	}

	/**
	 * @see Openable
	 */
	protected boolean buildStructure(OpenableElementInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource) throws JavaModelException {
	
		// check whether the java project can be opened
		if (!underlyingResource.isAccessible()) {
			throw newNotPresentException();
		}
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot wRoot = workspace.getRoot();
		// cannot refresh cp markers on opening (emulate cp check on startup) since can create deadlocks (see bug 37274)
		IClasspathEntry[] resolvedClasspath = getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);

		// compute the pkg fragment roots
		info.setChildren(computePackageFragmentRoots(resolvedClasspath, false, null /*no reverse map*/));	
		
		// remember the timestamps of external libraries the first time they are looked up
		for (int i = 0, length = resolvedClasspath.length; i < length; i++) {
			IClasspathEntry entry = resolvedClasspath[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
				IPath path = entry.getPath();
				Object target = JavaModel.getTarget(wRoot, path, true);
				if (target instanceof java.io.File) {
					Map externalTimeStamps = JavaModelManager.getJavaModelManager().deltaState.externalTimeStamps;
					if (externalTimeStamps.get(path) == null) {
						long timestamp = DeltaProcessor.getTimeStamp((java.io.File)target);
						externalTimeStamps.put(path, new Long(timestamp));							
					}
				}
			}
		}			

		return true;
	}
	protected void closing(Object info) {
		
		// forget source attachment recommendations
		Object[] children = ((JavaElementInfo)info).children;
		for (int i = 0, length = children.length; i < length; i++) {
			Object child = children[i];
			if (child instanceof JarPackageFragmentRoot){
				((JarPackageFragmentRoot)child).setSourceAttachmentProperty(null); 
			}
		}
		
		super.closing(info);
	}
	/**
	 * Computes the collection of package fragment roots (local ones) and set it on the given info.
	 * Need to check *all* package fragment roots in order to reset NameLookup
	 * @param info JavaProjectElementInfo
	 * @throws JavaModelException
	 */
	public void computeChildren(JavaProjectElementInfo info) throws JavaModelException {
		IClasspathEntry[] classpath = getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
		JavaProjectElementInfo.ProjectCache projectCache = info.projectCache;
		if (projectCache != null) {
			IPackageFragmentRoot[] newRoots = computePackageFragmentRoots(classpath, true, null /*no reverse map*/);
			checkIdentical: { // compare all pkg fragment root lists
				IPackageFragmentRoot[] oldRoots = projectCache.allPkgFragmentRootsCache;
				if (oldRoots.length == newRoots.length){
					for (int i = 0, length = oldRoots.length; i < length; i++){
						if (!oldRoots[i].equals(newRoots[i])){
							break checkIdentical;
						}
					}
					return; // no need to update
				}	
			}
		}
		info.setNonJavaResources(null);
		info.setChildren(
			computePackageFragmentRoots(classpath, false, null /*no reverse map*/));		
	}
	


	/**
	 * Internal computation of an expanded classpath. It will eliminate duplicates, and produce copies
	 * of exported or restricted classpath entries to avoid possible side-effects ever after.
	 */			
	private void computeExpandedClasspath(
		ClasspathEntry referringEntry,
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError,
		HashSet rootIDs,
		ObjectVector accumulatedEntries,
		Map preferredClasspaths,
		Map preferredOutputs) throws JavaModelException {
		
		String projectRootId = this.rootID();
		if (rootIDs.contains(projectRootId)){
			return; // break cycles if any
		}
		rootIDs.add(projectRootId);

		IClasspathEntry[] preferredClasspath = preferredClasspaths != null ? (IClasspathEntry[])preferredClasspaths.get(this) : null;
		IPath preferredOutput = preferredOutputs != null ? (IPath)preferredOutputs.get(this) : null;
		IClasspathEntry[] immediateClasspath = 
			preferredClasspath != null 
				? getResolvedClasspath(preferredClasspath, preferredOutput, ignoreUnresolvedVariable, generateMarkerOnError, null /*no reverse map*/)
				: getResolvedClasspath(ignoreUnresolvedVariable, generateMarkerOnError, false/*don't returnResolutionInProgress*/);
			
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		boolean isInitialProject = referringEntry == null;
		for (int i = 0, length = immediateClasspath.length; i < length; i++){
			ClasspathEntry entry = (ClasspathEntry) immediateClasspath[i];
			if (isInitialProject || entry.isExported()){
				String rootID = entry.rootID();
				if (rootIDs.contains(rootID)) {
					continue;
				}
				// combine restrictions along the project chain
				ClasspathEntry combinedEntry = entry.combineWith(referringEntry);
				accumulatedEntries.add(combinedEntry);
				
				// recurse in project to get all its indirect exports (only consider exported entries from there on)				
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
					IResource member = workspaceRoot.findMember(entry.getPath()); 
					if (member != null && member.getType() == IResource.PROJECT){ // double check if bound to project (23977)
						IProject projRsc = (IProject) member;
						if (JavaProject.hasJavaNature(projRsc)) {
							JavaProject javaProject = (JavaProject) JavaCore.create(projRsc);
							javaProject.computeExpandedClasspath(
								combinedEntry, 
								ignoreUnresolvedVariable, 
								false /* no marker when recursing in prereq*/,
								rootIDs,
								accumulatedEntries,
								preferredClasspaths,
								preferredOutputs);
						}
					}
				} else {
					rootIDs.add(rootID);
				}
			}			
		}
	}

	/**
	 * Computes the package fragment roots identified by the given entry.
	 * Only works with resolved entry
	 * @param resolvedEntry IClasspathEntry
	 * @return IPackageFragmentRoot[]
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(IClasspathEntry resolvedEntry) {
		try {
			return 
				computePackageFragmentRoots(
					new IClasspathEntry[]{ resolvedEntry }, 
					false, // don't retrieve exported roots
					null /* no reverse map */
				);
		} catch (JavaModelException e) {
			return new IPackageFragmentRoot[] {};
		}
	}
	
	/**
	 * Returns the package fragment roots identified by the given entry. In case it refers to
	 * a project, it will follow its classpath so as to find exported roots as well.
	 * Only works with resolved entry
	 * @param resolvedEntry IClasspathEntry
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry the CP entry (project) referring to this entry, or null if initial project
	 * @param checkExistency boolean
	 * @param retrieveExportedRoots boolean
	 * @throws JavaModelException
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry resolvedEntry,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		IClasspathEntry referringEntry,
		boolean checkExistency,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaModelException {
			
		String rootID = ((ClasspathEntry)resolvedEntry).rootID();
		if (rootIDs.contains(rootID)) return;

		IPath projectPath = this.project.getFullPath();
		IPath entryPath = resolvedEntry.getPath();
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPackageFragmentRoot root = null;
		
		switch(resolvedEntry.getEntryKind()){
			
			// source folder
			case IClasspathEntry.CPE_SOURCE :

				if (projectPath.isPrefixOf(entryPath)){
					if (checkExistency) {
						Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
						if (target == null) return;
	
						if (target instanceof IFolder || target instanceof IProject){
							root = getPackageFragmentRoot((IResource)target);
						}
					} else {
						root = getFolderPackageFragmentRoot(entryPath);
					}
				}
				break;

			// internal/external JAR or folder
			case IClasspathEntry.CPE_LIBRARY :
			
				if (referringEntry != null  && !resolvedEntry.isExported()) return;
				
				if (checkExistency) {
					Object target = JavaModel.getTarget(workspaceRoot, entryPath, checkExistency);
					if (target == null) return;
	
					if (target instanceof IResource){
						// internal target
						root = getPackageFragmentRoot((IResource) target);
					} else {
						// external target - only JARs allowed
						if (((java.io.File)target).isFile() && (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(entryPath.lastSegment()))) {
							root = new JarPackageFragmentRoot(entryPath, this);
						}
					}
				} else {
					root = getPackageFragmentRoot(entryPath);
				}
				break;

			// recurse into required project
			case IClasspathEntry.CPE_PROJECT :

				if (!retrieveExportedRoots) return;
				if (referringEntry != null && !resolvedEntry.isExported()) return;

				IResource member = workspaceRoot.findMember(entryPath);
				if (member != null && member.getType() == IResource.PROJECT){// double check if bound to project (23977)
					IProject requiredProjectRsc = (IProject) member;
					if (JavaProject.hasJavaNature(requiredProjectRsc)){ // special builder binary output
						rootIDs.add(rootID);
						JavaProject requiredProject = (JavaProject)JavaCore.create(requiredProjectRsc);
						requiredProject.computePackageFragmentRoots(
							requiredProject.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/), 
							accumulatedRoots, 
							rootIDs, 
							rootToResolvedEntries == null ? resolvedEntry : ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry), // only combine if need to build the reverse map 
							checkExistency, 
							retrieveExportedRoots,
							rootToResolvedEntries);
					}
				break;
			}
		}
		if (root != null) {
			accumulatedRoots.add(root);
			rootIDs.add(rootID);
			if (rootToResolvedEntries != null) rootToResolvedEntries.put(root, ((ClasspathEntry)resolvedEntry).combineWith((ClasspathEntry) referringEntry));
		}
	}
	
	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IClasspathEntry[]
	 * @param retrieveExportedRoots boolean
	 * @return IPackageFragmentRoot[]
	 * @throws JavaModelException
	 */
	public IPackageFragmentRoot[] computePackageFragmentRoots(
					IClasspathEntry[] resolvedClasspath, 
					boolean retrieveExportedRoots,
					Map rootToResolvedEntries) throws JavaModelException {

		ObjectVector accumulatedRoots = new ObjectVector();
		computePackageFragmentRoots(
			resolvedClasspath, 
			accumulatedRoots, 
			new HashSet(5), // rootIDs
			null, // inside original project
			true, // check existency
			retrieveExportedRoots,
			rootToResolvedEntries);
		IPackageFragmentRoot[] rootArray = new IPackageFragmentRoot[accumulatedRoots.size()];
		accumulatedRoots.copyInto(rootArray);
		return rootArray;
	}

	/**
	 * Returns (local/all) the package fragment roots identified by the given project's classpath.
	 * Note: this follows project classpath references to find required project contributions,
	 * eliminating duplicates silently.
	 * Only works with resolved entries
	 * @param resolvedClasspath IClasspathEntry[]
	 * @param accumulatedRoots ObjectVector
	 * @param rootIDs HashSet
	 * @param referringEntry project entry referring to this CP or null if initial project
	 * @param checkExistency boolean
	 * @param retrieveExportedRoots boolean
	 * @throws JavaModelException
	 */
	public void computePackageFragmentRoots(
		IClasspathEntry[] resolvedClasspath,
		ObjectVector accumulatedRoots, 
		HashSet rootIDs, 
		IClasspathEntry referringEntry,
		boolean checkExistency,
		boolean retrieveExportedRoots,
		Map rootToResolvedEntries) throws JavaModelException {

		if (referringEntry == null){
			rootIDs.add(rootID());
		}	
		for (int i = 0, length = resolvedClasspath.length; i < length; i++){
			computePackageFragmentRoots(
				resolvedClasspath[i],
				accumulatedRoots,
				rootIDs,
				referringEntry,
				checkExistency,
				retrieveExportedRoots,
				rootToResolvedEntries);
		}
	}

	/**
	 * Compute the file name to use for a given shared property
	 * @param qName QualifiedName
	 * @return String
	 */
	public String computeSharedPropertyFileName(QualifiedName qName) {

		return '.' + qName.getLocalName();
	}
	
	/**
	 * Configure the project with Java nature.
	 */
	public void configure() throws CoreException {

		// register Java builder
		addToBuildSpec(JavaCore.BUILDER_ID);
	}
	/*
	 * Returns whether the given resource is accessible through the children or the non-Java resources of this project.
	 * Returns true if the resource is not in the project.
	 * Assumes that the resource is a folder or a file.
	 */
	public boolean contains(IResource resource) {
			
		IClasspathEntry[] classpath;
		IPath output;
		try {
			classpath = getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
			output = getOutputLocation();
		} catch (JavaModelException e) {
			return false;
		}
		
		IPath fullPath = resource.getFullPath();
		IPath innerMostOutput = output.isPrefixOf(fullPath) ? output : null;
		IClasspathEntry innerMostEntry = null;
		for (int j = 0, cpLength = classpath.length; j < cpLength; j++) {
			IClasspathEntry entry = classpath[j];
		
			IPath entryPath = entry.getPath();
			if ((innerMostEntry == null || innerMostEntry.getPath().isPrefixOf(entryPath))
					&& entryPath.isPrefixOf(fullPath)) {
				innerMostEntry = entry;
			}
			IPath entryOutput = classpath[j].getOutputLocation();
			if (entryOutput != null && entryOutput.isPrefixOf(fullPath)) {
				innerMostOutput = entryOutput;
			}
		}
		if (innerMostEntry != null) {
			// special case prj==src and nested output location
			if (innerMostOutput != null && innerMostOutput.segmentCount() > 1 // output isn't project
					&& innerMostEntry.getPath().segmentCount() == 1) { // 1 segment must be project name
				return false;
			}
			if  (resource instanceof IFolder) {
				 // folders are always included in src/lib entries
				 return true;
			}
			switch (innerMostEntry.getEntryKind()) {
				case IClasspathEntry.CPE_SOURCE:
					// .class files are not visible in source folders 
					return !org.eclipse.jdt.internal.compiler.util.Util.isClassFileName(fullPath.lastSegment());
				case IClasspathEntry.CPE_LIBRARY:
					// .java files are not visible in library folders
					return !org.eclipse.jdt.internal.core.util.Util.isJavaLikeFileName(fullPath.lastSegment());
			}
		}
		if (innerMostOutput != null) {
			return false;
		}
		return true;
	}

	/**
	 * Record a new marker denoting a classpath problem
	 */
	void createClasspathProblemMarker(IJavaModelStatus status) {
			
		IMarker marker = null;
		int severity;
		String[] arguments = new String[0];
		boolean isCycleProblem = false, isClasspathFileFormatProblem = false;
		switch (status.getCode()) {
	
			case  IJavaModelStatusConstants.CLASSPATH_CYCLE :
				isCycleProblem = true;
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;
	
			case  IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT :
				isClasspathFileFormatProblem = true;
				severity = IMarker.SEVERITY_ERROR;
				break;
	
			case  IJavaModelStatusConstants.INCOMPATIBLE_JDK_LEVEL :
				String setting = getOption(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, true);
				if (JavaCore.ERROR.equals(setting)) {
					severity = IMarker.SEVERITY_ERROR;
				} else if (JavaCore.WARNING.equals(setting)) {
					severity = IMarker.SEVERITY_WARNING;
				} else {
					return; // setting == IGNORE
				}
				break;
				
			default:
				IPath path = status.getPath();
				if (path != null) arguments = new String[] { path.toString() };
				if (JavaCore.ERROR.equals(getOption(JavaCore.CORE_INCOMPLETE_CLASSPATH, true))) {
					severity = IMarker.SEVERITY_ERROR;
				} else {
					severity = IMarker.SEVERITY_WARNING;
				}
				break;
		}
		
		try {
			marker = this.project.createMarker(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER);
			marker.setAttributes(
				new String[] { 
					IMarker.MESSAGE, 
					IMarker.SEVERITY, 
					IMarker.LOCATION, 
					IJavaModelMarker.CYCLE_DETECTED,
					IJavaModelMarker.CLASSPATH_FILE_FORMAT,
					IJavaModelMarker.ID,
					IJavaModelMarker.ARGUMENTS ,
				},
				new Object[] {
					status.getMessage(),
					new Integer(severity), 
					Messages.classpath_buildPath,
					isCycleProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					isClasspathFileFormatProblem ? "true" : "false",//$NON-NLS-1$ //$NON-NLS-2$
					new Integer(status.getCode()),
					Util.getProblemArgumentsForMarker(arguments) ,
				}
			);
		} catch (CoreException e) {
			// could not create marker: cannot do much
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Returns a new element info for this element.
	 */
	protected Object createElementInfo() {
		return new JavaProjectElementInfo();
	}

	/**
	 * Reads and decode an XML classpath string
	 */
	protected IClasspathEntry[] decodeClasspath(String xmlClasspath, boolean createMarker, boolean logProblems) {

		ArrayList paths = new ArrayList();
		IClasspathEntry defaultOutput = null;
		try {
			if (xmlClasspath == null) return null;
			StringReader reader = new StringReader(xmlClasspath);
			Element cpElement;
	
			try {
				DocumentBuilder parser =
					DocumentBuilderFactory.newInstance().newDocumentBuilder();
				cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
			} catch (SAXException e) {
				throw new IOException(Messages.file_badFormat); 
			} catch (ParserConfigurationException e) {
				throw new IOException(Messages.file_badFormat); 
			} finally {
				reader.close();
			}
	
			if (!cpElement.getNodeName().equalsIgnoreCase("classpath")) { //$NON-NLS-1$
				throw new IOException(Messages.file_badFormat); 
			}
			NodeList list = cpElement.getElementsByTagName("classpathentry"); //$NON-NLS-1$
			int length = list.getLength();
	
			for (int i = 0; i < length; ++i) {
				Node node = list.item(i);
				if (node.getNodeType() == Node.ELEMENT_NODE) {
					IClasspathEntry entry = ClasspathEntry.elementDecode((Element)node, this);
					if (entry != null){
						if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) { 
							defaultOutput = entry; // separate output
						} else {
							paths.add(entry);
				}
			}
				}
			}
		} catch (IOException e) {
			// bad format
			if (createMarker && this.project.isAccessible()) {
					this.createClasspathProblemMarker(new JavaModelStatus(
							IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
							Messages.bind(Messages.classpath_xmlFormatError, (new String[] {this.getElementName(), e.getMessage()})))); 
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will mark classpath as invalid"); //$NON-NLS-1$
			}
			return INVALID_CLASSPATH;
		} catch (Assert.AssertionFailedException e) { 
			// failed creating CP entries from file
			if (createMarker && this.project.isAccessible()) {
				this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Messages.bind(Messages.classpath_illegalEntryInClasspathFile, (new String[] {this.getElementName(), e.getMessage()})))); 
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will mark classpath as invalid"); //$NON-NLS-1$
			}
			return INVALID_CLASSPATH;
		}
		int pathSize = paths.size();
		if (pathSize > 0 || defaultOutput != null) {
			IClasspathEntry[] entries = new IClasspathEntry[pathSize + (defaultOutput == null ? 0 : 1)];
			paths.toArray(entries);
			if (defaultOutput != null) entries[pathSize] = defaultOutput; // ensure output is last item
			return entries;
		} else {
			return null;
		}
	}

	/**
	/**
	 * Removes the Java nature from the project.
	 */
	public void deconfigure() throws CoreException {

		// deregister Java builder
		removeFromBuildSpec(JavaCore.BUILDER_ID);
	}

	/**
	 * Returns a default class path.
	 * This is the root of the project
	 */
	protected IClasspathEntry[] defaultClasspath() {

		return new IClasspathEntry[] {
			 JavaCore.newSourceEntry(this.project.getFullPath())};
	}

	/**
	 * Returns a default output location.
	 * This is the project bin folder
	 */
	protected IPath defaultOutputLocation() {
		return this.project.getFullPath().append("bin"); //$NON-NLS-1$
	}

	/**
	 * Returns the XML String encoding of the class path.
	 */
	protected String encodeClasspath(IClasspathEntry[] classpath, IPath outputLocation, boolean indent) throws JavaModelException {
		try {
			ByteArrayOutputStream s = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
			XMLWriter xmlWriter = new XMLWriter(writer);
			
			xmlWriter.startTag(ClasspathEntry.TAG_CLASSPATH, indent);
			for (int i = 0; i < classpath.length; ++i) {
				((ClasspathEntry)classpath[i]).elementEncode(xmlWriter, this.project.getFullPath(), indent, true);
			}
	
			if (outputLocation != null) {
				outputLocation = outputLocation.removeFirstSegments(1);
				outputLocation = outputLocation.makeRelative();
				HashMap parameters = new HashMap();
				parameters.put(ClasspathEntry.TAG_KIND, ClasspathEntry.kindToString(ClasspathEntry.K_OUTPUT));
				parameters.put(ClasspathEntry.TAG_PATH, String.valueOf(outputLocation));
				xmlWriter.printTag(ClasspathEntry.TAG_CLASSPATHENTRY, parameters, indent, true, true);
			}
	
			xmlWriter.endTag(ClasspathEntry.TAG_CLASSPATH, indent);
			writer.flush();
			writer.close();
			return s.toString("UTF8");//$NON-NLS-1$
		} catch (IOException e) {
			throw new JavaModelException(e, IJavaModelStatusConstants.IO_EXCEPTION);
		}
	}
	
	/**
	 * Returns true if this handle represents the same Java project
	 * as the given handle. Two handles represent the same
	 * project if they are identical or if they represent a project with 
	 * the same underlying resource and occurrence counts.
	 *
	 * @see JavaElement#equals(Object)
	 */
	public boolean equals(Object o) {
	
		if (this == o)
			return true;
	
		if (!(o instanceof JavaProject))
			return false;
	
		JavaProject other = (JavaProject) o;
		return this.project.equals(other.getProject());
	}

	public boolean exists() {
		return hasJavaNature(this.project);
	}	

	/**
	 * @see IJavaProject
	 */
	public IJavaElement findElement(IPath path) throws JavaModelException {
		return findElement(path, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see IJavaProject
	 */
	public IJavaElement findElement(IPath path, WorkingCopyOwner owner) throws JavaModelException {
		
		if (path == null || path.isAbsolute()) {
			throw new JavaModelException(
				new JavaModelStatus(IJavaModelStatusConstants.INVALID_PATH, path));
		}
		try {

			String extension = path.getFileExtension();
			if (extension == null) {
				String packageName = path.toString().replace(IPath.SEPARATOR, '.');

				NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
				IPackageFragment[] pkgFragments = lookup.findPackageFragments(packageName, false);
				if (pkgFragments == null) {
					return null;

				} else {
					// try to return one that is a child of this project
					for (int i = 0, length = pkgFragments.length; i < length; i++) {

						IPackageFragment pkgFragment = pkgFragments[i];
						if (this.equals(pkgFragment.getParent().getParent())) {
							return pkgFragment;
						}
					}
					// default to the first one
					return pkgFragments[0];
				}
			} else if (Util.isJavaLikeFileName(path.lastSegment())
					|| extension.equalsIgnoreCase(EXTENSION_class)) {
				IPath packagePath = path.removeLastSegments(1);
				String packageName = packagePath.toString().replace(IPath.SEPARATOR, '.');
				String typeName = path.lastSegment();
				typeName = typeName.substring(0, typeName.length() - extension.length() - 1);
				String qualifiedName = null;
				if (packageName.length() > 0) {
					qualifiedName = packageName + "." + typeName; //$NON-NLS-1$
				} else {
					qualifiedName = typeName;
				}

				// lookup type
				NameLookup lookup = newNameLookup(owner);
				IType type = lookup.findType(
					qualifiedName,
					false,
					NameLookup.ACCEPT_ALL);

				if (type != null) {
					return type.getParent();
				} else {
					return null;
				}
			} else {
				// unsupported extension
				return null;
			}
		} catch (JavaModelException e) {
			if (e.getStatus().getCode()
				== IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST) {
				return null;
			} else {
				throw e;
			}
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment findPackageFragment(IPath path)
		throws JavaModelException {

		return findPackageFragment0(JavaProject.canonicalizedPath(path));
	}

	/*
	 * non path canonicalizing version
	 */
	private IPackageFragment findPackageFragment0(IPath path) 
		throws JavaModelException {

		NameLookup lookup = newNameLookup((WorkingCopyOwner)null/*no need to look at working copies for pkgs*/);
		return lookup.findPackageFragment(path);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot findPackageFragmentRoot(IPath path)
		throws JavaModelException {

		return findPackageFragmentRoot0(JavaProject.canonicalizedPath(path));
	}

	/*
	 * no path canonicalization 
	 */
	public IPackageFragmentRoot findPackageFragmentRoot0(IPath path)
		throws JavaModelException {

		IPackageFragmentRoot[] allRoots = this.getAllPackageFragmentRoots();
		if (!path.isAbsolute()) {
			throw new IllegalArgumentException(Messages.path_mustBeAbsolute); 
		}
		for (int i= 0; i < allRoots.length; i++) {
			IPackageFragmentRoot classpathRoot= allRoots[i];
			if (classpathRoot.getPath().equals(path)) {
				return classpathRoot;
			}
		}
		return null;
	}
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] findPackageFragmentRoots(IClasspathEntry entry) {
		try {
			IClasspathEntry[] classpath = this.getRawClasspath();
			for (int i = 0, length = classpath.length; i < length; i++) {
				if (classpath[i].equals(entry)) { // entry may need to be resolved
					return 
						computePackageFragmentRoots(
							getResolvedClasspath(new IClasspathEntry[] {entry}, null, true, false, null/*no reverse map*/), 
							false, // don't retrieve exported roots
							null); /*no reverse map*/
				}
			}
		} catch (JavaModelException e) {
			// project doesn't exist: return an empty array
		}
		return new IPackageFragmentRoot[] {};
	}
	
	/**
	 * @see IJavaProject#findType(String)
	 */
	public IType findType(String fullyQualifiedName) throws JavaModelException {
		return findType(fullyQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}
	
	/**
	 * @see IJavaProject#findType(String, String)
	 */
	public IType findType(String packageName, String typeQualifiedName) throws JavaModelException {
		return findType(packageName, typeQualifiedName, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * @see IJavaProject#findType(String, String, WorkingCopyOwner)
	 */
	public IType findType(String packageName, String typeQualifiedName, WorkingCopyOwner owner) throws JavaModelException {
		NameLookup lookup = newNameLookup(owner);
		return lookup.findType(
			typeQualifiedName, 
			packageName,
			false,
			NameLookup.ACCEPT_ALL);
	}	

	/**
	 * @see IJavaProject#findType(String, WorkingCopyOwner)
	 */
	public IType findType(String fullyQualifiedName, WorkingCopyOwner owner) throws JavaModelException {
		
		NameLookup lookup = newNameLookup(owner);
		IType type = lookup.findType(
			fullyQualifiedName,
			false,
			NameLookup.ACCEPT_ALL);
		if (type == null) {
			// try to find enclosing type
			int lastDot = fullyQualifiedName.lastIndexOf('.');
			if (lastDot == -1) return null;
			type = this.findType(fullyQualifiedName.substring(0, lastDot));
			if (type != null) {
				type = type.getType(fullyQualifiedName.substring(lastDot+1));
				if (!type.exists()) {
					return null;
				}
			}
		}
		return type;
	}
	
	/**
	 * Remove all markers denoting classpath problems
	 */ //TODO (philippe) should improve to use a bitmask instead of booleans (CYCLE, FORMAT, VALID)
	protected void flushClasspathProblemMarkers(boolean flushCycleMarkers, boolean flushClasspathFormatMarkers) {
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					if (flushCycleMarkers && flushClasspathFormatMarkers) {
						marker.delete();
					} else {
						String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
						String classpathFileFormatAttr =  (String)marker.getAttribute(IJavaModelMarker.CLASSPATH_FILE_FORMAT);
						if ((flushCycleMarkers == (cycleAttr != null && cycleAttr.equals("true"))) //$NON-NLS-1$
							&& (flushClasspathFormatMarkers == (classpathFileFormatAttr != null && classpathFileFormatAttr.equals("true")))){ //$NON-NLS-1$
							marker.delete();
						}
					}
				}
			}
		} catch (CoreException e) {
			// could not flush markers: not much we can do
			if (JavaModelManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}

	/*
	 * Force the project to reload its <code>.classpath</code> file from disk and update the classpath accordingly.
	 * Usually, a change to the <code>.classpath</code> file is automatically noticed and reconciled at the next 
	 * resource change notification event. If required to consider such a change prior to the next automatic
	 * refresh, then this functionnality should be used to trigger a refresh. In particular, if a change to the file is performed,
	 * during an operation where this change needs to be reflected before the operation ends, then an explicit refresh is
	 * necessary.
	 * Note that classpath markers are NOT created.
	 * 
	 * @param monitor a progress monitor for reporting operation progress
	 * @exception JavaModelException if the classpath could not be updated. Reasons
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
	 */
	protected void forceClasspathReload(IProgressMonitor monitor) throws JavaModelException {

		if (monitor != null && monitor.isCanceled()) return;
		
		// check if any actual difference
		boolean wasSuccessful = false; // flag recording if .classpath file change got reflected
		try {
			// force to (re)read the property file
			IClasspathEntry[] fileEntries = readClasspathFile(false/*don't create markers*/, false/*don't log problems*/);
			if (fileEntries == null) {
				return; // could not read, ignore 
			}
			JavaModelManager.PerProjectInfo info = getPerProjectInfo();
			if (info.rawClasspath != null) { // if there is an in-memory classpath
				if (isClasspathEqualsTo(info.rawClasspath, info.outputLocation, fileEntries)) {
					wasSuccessful = true;
					return;
				}
			}

			// will force an update of the classpath/output location based on the file information
			// extract out the output location
			IPath outputLocation = SetClasspathOperation.REUSE_PATH; 
			if (fileEntries != null && fileEntries.length > 0) {
				IClasspathEntry entry = fileEntries[fileEntries.length - 1];
				if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
					outputLocation = entry.getPath();
					IClasspathEntry[] copy = new IClasspathEntry[fileEntries.length - 1];
					System.arraycopy(fileEntries, 0, copy, 0, copy.length);
					fileEntries = copy;
				}
			}
			IClasspathEntry[] oldResolvedClasspath = info.resolvedClasspath;
			setRawClasspath(
				fileEntries, 
				outputLocation,
				monitor, 
				!ResourcesPlugin.getWorkspace().isTreeLocked(), // canChangeResource
				oldResolvedClasspath != null ? oldResolvedClasspath : getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/),
				true, // needValidation
				false); // no need to save
			
			// if reach that far, the classpath file change got absorbed
			wasSuccessful = true;
		} catch (RuntimeException e) {
			// setRawClasspath might fire a delta, and a listener may throw an exception
			if (this.project.isAccessible()) {
				Util.log(e, "Could not set classpath for "+ getPath()); //$NON-NLS-1$
			}
			throw e; // rethrow 
		} catch (JavaModelException e) { // CP failed validation
			if (!ResourcesPlugin.getWorkspace().isTreeLocked()) {
				if (this.project.isAccessible()) {
					if (e.getJavaModelStatus().getException() instanceof CoreException) {
						// happens if the .classpath could not be written to disk
						createClasspathProblemMarker(new JavaModelStatus(
								IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
								Messages.bind(Messages.classpath_couldNotWriteClasspathFile, new String[] {getElementName(), e.getMessage()}))); 
					} else {
						createClasspathProblemMarker(new JavaModelStatus(
								IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
								Messages.bind(Messages.classpath_invalidClasspathInClasspathFile, new String[] {getElementName(), e.getMessage()}))); 
					}			
				}
			}
			throw e; // rethrow
		} finally {
			if (!wasSuccessful) { 
				try {
					this.getPerProjectInfo().updateClasspathInformation(JavaProject.INVALID_CLASSPATH);
					updatePackageFragmentRoots();
				} catch (JavaModelException e) {
					// ignore
				}
			}
		}
	}	

	/** 
	 * Returns the set of patterns corresponding to this project visibility given rules
	 * @return an array of IPath or null if none
	 */
	public IPath[] getAccessRestrictions(String optionName) {
		String sequence = getOption(optionName, true); // inherit from workspace
		if (sequence == null || sequence.length() == 0) return null;
		IPath[] rules = null;
		char[][] patterns = CharOperation.splitOn('|', sequence.toCharArray());
		int patternCount;
		if ((patternCount  = patterns.length) > 0) {
			rules = new IPath[patternCount];
			for (int j = 0; j < patterns.length; j++){
				rules[j] = new Path(new String(patterns[j]));
			}
		}
		return rules;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots()
		throws JavaModelException {

		return getAllPackageFragmentRoots(null /*no reverse map*/);
	}
	
	public IPackageFragmentRoot[] getAllPackageFragmentRoots(Map rootToResolvedEntries) throws JavaModelException {

		return computePackageFragmentRoots(getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/), true/*retrieveExportedRoots*/, rootToResolvedEntries);
	}

	/**
	 * Returns the classpath entry that refers to the given path
	 * or <code>null</code> if there is no reference to the path.
	 * @param path IPath
	 * @return IClasspathEntry
	 * @throws JavaModelException
	 */
	public IClasspathEntry getClasspathEntryFor(IPath path)
		throws JavaModelException {

		IClasspathEntry[] entries = getExpandedClasspath(true);
		for (int i = 0; i < entries.length; i++) {
			if (entries[i].getPath().equals(path)) {
				return entries[i];
			}
		}
		return null;
	}

	/*
	 * Returns the cycle marker associated with this project or null if none.
	 */
	public IMarker getCycleMarker(){
		try {
			if (this.project.isAccessible()) {
				IMarker[] markers = this.project.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
				for (int i = 0, length = markers.length; i < length; i++) {
					IMarker marker = markers[i];
					String cycleAttr = (String)marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED);
					if (cycleAttr != null && cycleAttr.equals("true")){ //$NON-NLS-1$
						return marker;
					}
				}
			}
		} catch (CoreException e) {
			// could not get markers: return null
		}
		return null;
	}

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * @return IEclipsePreferences
	 */
	public IEclipsePreferences getEclipsePreferences(){
		if (!JavaProject.hasJavaNature(this.project)) return null;
		// Get cached preferences if exist
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, true);
		if (perProjectInfo.preferences != null) return perProjectInfo.preferences;
		// Init project preferences
		IScopeContext context = new ProjectScope(getProject());
		final IEclipsePreferences eclipsePreferences = context.getNode(JavaCore.PLUGIN_ID);
		updatePreferences(eclipsePreferences);
		perProjectInfo.preferences = eclipsePreferences;

		// Listen to node removal from parent in order to reset cache (see bug 68993)
		IEclipsePreferences.INodeChangeListener nodeListener = new IEclipsePreferences.INodeChangeListener() {
			public void added(IEclipsePreferences.NodeChangeEvent event) {
				// do nothing
			}
			public void removed(IEclipsePreferences.NodeChangeEvent event) {
				if (event.getChild() == eclipsePreferences) {
					JavaModelManager.getJavaModelManager().resetProjectPreferences(JavaProject.this);
				}
			}
		};
		((IEclipsePreferences) eclipsePreferences.parent()).addNodeChangeListener(nodeListener);

		// Listen to preference changes
		IEclipsePreferences.IPreferenceChangeListener preferenceListener = new IEclipsePreferences.IPreferenceChangeListener() {
			public void preferenceChange(IEclipsePreferences.PreferenceChangeEvent event) {
				JavaModelManager.getJavaModelManager().resetProjectOptions(JavaProject.this);
			}
		};
		eclipsePreferences.addPreferenceChangeListener(preferenceListener);
		return eclipsePreferences;
	}

	public String getElementName() {
		return this.project.getName();
	}
	
	/**
	 * @see IJavaElement
	 */
	public int getElementType() {
		return JAVA_PROJECT;
	}

	/**
	 * This is a helper method returning the expanded classpath for the project, as a list of classpath entries, 
	 * where all classpath variable entries have been resolved and substituted with their final target entries.
	 * All project exports have been appended to project entries.
	 * @param ignoreUnresolvedVariable boolean
	 * @return IClasspathEntry[]
	 * @throws JavaModelException
	 */
	public IClasspathEntry[] getExpandedClasspath(boolean ignoreUnresolvedVariable)	throws JavaModelException {
			
			return getExpandedClasspath(ignoreUnresolvedVariable, false/*don't create markers*/, null, null);
	}
		
	/**
	 * Internal variant which can create marker on project for invalid entries,
	 * it will also perform classpath expansion in presence of project prerequisites
	 * exporting their entries.
	 * @param ignoreUnresolvedVariable boolean
	 * @param generateMarkerOnError boolean
	 * @param preferredClasspaths Map
	 * @param preferredOutputs Map
	 * @return IClasspathEntry[]
	 * @throws JavaModelException
	 */
	public IClasspathEntry[] getExpandedClasspath(
		boolean ignoreUnresolvedVariable,
		boolean generateMarkerOnError,
		Map preferredClasspaths,
		Map preferredOutputs) throws JavaModelException {
	
		ObjectVector accumulatedEntries = new ObjectVector();		
		computeExpandedClasspath(null, ignoreUnresolvedVariable, generateMarkerOnError, new HashSet(5), accumulatedEntries, preferredClasspaths, preferredOutputs);
		
		IClasspathEntry[] expandedPath = new IClasspathEntry[accumulatedEntries.size()];
		accumulatedEntries.copyInto(expandedPath);

		return expandedPath;
	}

	/**
	 * The path is known to match a source/library folder entry.
	 * @param path IPath
	 * @return IPackageFragmentRoot
	 */
	public IPackageFragmentRoot getFolderPackageFragmentRoot(IPath path) {
		if (path.segmentCount() == 1) { // default project root
			return getPackageFragmentRoot(this.project);
		}
		return getPackageFragmentRoot(this.project.getWorkspace().getRoot().getFolder(path));
	}

	/*
	 * @see JavaElement
	 */
	public IJavaElement getHandleFromMemento(String token, MementoTokenizer memento, WorkingCopyOwner owner) {
		switch (token.charAt(0)) {
			case JEM_PACKAGEFRAGMENTROOT:
				String rootPath = IPackageFragmentRoot.DEFAULT_PACKAGEROOT_PATH;
				token = null;
				while (memento.hasMoreTokens()) {
					token = memento.nextToken();
					char firstChar = token.charAt(0);
					if (firstChar != JEM_PACKAGEFRAGMENT && firstChar != JEM_COUNT) {
						rootPath += token;
					} else {
						break;
					}
				}
				JavaElement root = (JavaElement)getPackageFragmentRoot(new Path(rootPath));
				if (token != null && token.charAt(0) == JEM_PACKAGEFRAGMENT) {
					return root.getHandleFromMemento(token, memento, owner);
				} else {
					return root.getHandleFromMemento(memento, owner);
				}
		}
		return null;
	}

	/**
	 * Returns the <code>char</code> that marks the start of this handles
	 * contribution to a memento.
	 */
	protected char getHandleMementoDelimiter() {

		return JEM_JAVAPROJECT;
	}

	/**
	 * Find the specific Java command amongst the given build spec
	 * and return its index or -1 if not found.
	 */
	private int getJavaCommandIndex(ICommand[] buildSpec) {

		for (int i = 0; i < buildSpec.length; ++i) {
			if (buildSpec[i].getBuilderName().equals(JavaCore.BUILDER_ID)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Convenience method that returns the specific type of info for a Java project.
	 */
	protected JavaProjectElementInfo getJavaProjectElementInfo()
		throws JavaModelException {

		return (JavaProjectElementInfo) getElementInfo();
	}

	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() throws JavaModelException {

		return ((JavaProjectElementInfo) getElementInfo()).getNonJavaResources(this);
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOption(String, boolean)
	 */	
	public String getOption(String optionName, boolean inheritJavaCoreOptions) {
		
		String propertyName = optionName;
		if (JavaModelManager.getJavaModelManager().optionNames.contains(propertyName)){
			IEclipsePreferences projectPreferences = getEclipsePreferences();
			String javaCoreDefault = inheritJavaCoreOptions ? JavaCore.getOption(propertyName) : null;
			if (projectPreferences == null) return javaCoreDefault;
			String value = projectPreferences.get(propertyName, javaCoreDefault);
			return value == null ? null : value.trim();
		}
		return null;
	}
	
	/**
	 * @see org.eclipse.jdt.core.IJavaProject#getOptions(boolean)
	 */
	public Map getOptions(boolean inheritJavaCoreOptions) {

		// initialize to the defaults from JavaCore options pool
		Map options = inheritJavaCoreOptions ? JavaCore.getOptions() : new Hashtable(5);

		// Get project specific options
		JavaModelManager.PerProjectInfo perProjectInfo = null;
		Hashtable projectOptions = null;
		HashSet optionNames = JavaModelManager.getJavaModelManager().optionNames;
		try {
			perProjectInfo = getPerProjectInfo();
			projectOptions = perProjectInfo.options;
			if (projectOptions == null) {
				// get eclipse preferences
				IEclipsePreferences projectPreferences= getEclipsePreferences();
				if (projectPreferences == null) return options; // cannot do better (non-Java project)
				// create project options
				String[] propertyNames = projectPreferences.keys();
				projectOptions = new Hashtable(propertyNames.length);
				for (int i = 0; i < propertyNames.length; i++){
					String propertyName = propertyNames[i];
					String value = projectPreferences.get(propertyName, null);
					if (value != null && optionNames.contains(propertyName)){
						projectOptions.put(propertyName, value.trim());
					}
				}		
				// cache project options
				perProjectInfo.options = projectOptions;
			}
		} catch (JavaModelException jme) {
			projectOptions = new Hashtable();
		} catch (BackingStoreException e) {
			projectOptions = new Hashtable();
		}

		// Inherit from JavaCore options if specified
		if (inheritJavaCoreOptions) {
			Iterator propertyNames = projectOptions.keySet().iterator();
			while (propertyNames.hasNext()) {
				String propertyName = (String) propertyNames.next();
				String propertyValue = (String) perProjectInfo.options.get(propertyName);
				if (propertyValue != null && optionNames.contains(propertyName)){
					options.put(propertyName, propertyValue.trim());
				}
			}
			return options;
		}
		return projectOptions;
	}

	/**
	 * @see IJavaProject
	 */
	public IPath getOutputLocation() throws JavaModelException {
		// Do not create marker but log problems while getting output location
		return this.getOutputLocation(false, true);
	}
	
	/**
	 * @param createMarkers boolean
	 * @param logProblems boolean
	 * @return IPath
	 * @throws JavaModelException
	 */
	public IPath getOutputLocation(boolean createMarkers, boolean logProblems) throws JavaModelException {

		JavaModelManager.PerProjectInfo perProjectInfo = getPerProjectInfo();
		IPath outputLocation = perProjectInfo.outputLocation;
		if (outputLocation != null) return outputLocation;

		// force to read classpath - will position output location as well
		this.getRawClasspath(createMarkers, logProblems);
		outputLocation = perProjectInfo.outputLocation;
		if (outputLocation == null) {
			return defaultOutputLocation();
		}
		return outputLocation;
	}

	/**
	 * @param path IPath
	 * @return A handle to the package fragment root identified by the given path.
	 * This method is handle-only and the element may or may not exist. Returns
	 * <code>null</code> if unable to generate a handle from the path (for example,
	 * an absolute path that has less than 1 segment. The path may be relative or
	 * absolute.
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IPath path) {
		if (!path.isAbsolute()) {
			path = getPath().append(path);
		}
		int segmentCount = path.segmentCount();
		switch (segmentCount) {
			case 0:
				return null;
			case 1:
				if (path.equals(getPath())) { // see https://bugs.eclipse.org/bugs/show_bug.cgi?id=75814
					// default root
					return getPackageFragmentRoot(this.project);
				}
			default:
				// a path ending with .jar/.zip is still ambiguous and could still resolve to a source/lib folder 
				// thus will try to guess based on existing resource
				if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(path.lastSegment())) {
					IResource resource = this.project.getWorkspace().getRoot().findMember(path); 
					if (resource != null && resource.getType() == IResource.FOLDER){
						return getPackageFragmentRoot(resource);
					}
					return getPackageFragmentRoot0(path);
				} else {
					return getPackageFragmentRoot(this.project.getWorkspace().getRoot().getFolder(path));
				}
		}
	}
	
	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(IResource resource) {

		switch (resource.getType()) {
			case IResource.FILE:
				if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(resource.getName())) {
					return new JarPackageFragmentRoot(resource, this);
				} else {
					return null;
				}
			case IResource.FOLDER:
				return new PackageFragmentRoot(resource, this);
			case IResource.PROJECT:
				return new PackageFragmentRoot(resource, this); //$NON-NLS-1$
			default:
				return null;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot getPackageFragmentRoot(String jarPath) {

		return getPackageFragmentRoot0(JavaProject.canonicalizedPath(new Path(jarPath)));
	}
	
	/*
	 * no path canonicalization
	 */
	public IPackageFragmentRoot getPackageFragmentRoot0(IPath jarPath) {

		return new JarPackageFragmentRoot(jarPath, this);
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots()
		throws JavaModelException {

		Object[] children;
		int length;
		IPackageFragmentRoot[] roots;

		System.arraycopy(
			children = getChildren(), 
			0, 
			roots = new IPackageFragmentRoot[length = children.length], 
			0, 
			length);
			
		return roots;
	}

	/**
	 * @see IJavaProject
	 * @deprecated
	 */
	public IPackageFragmentRoot[] getPackageFragmentRoots(IClasspathEntry entry) {
		return findPackageFragmentRoots(entry);
	}

	/**
	 * Returns the package fragment root prefixed by the given path, or
	 * an empty collection if there are no such elements in the model.
	 */
	protected IPackageFragmentRoot[] getPackageFragmentRoots(IPath path)

		throws JavaModelException {
		IPackageFragmentRoot[] roots = getAllPackageFragmentRoots();
		ArrayList matches = new ArrayList();

		for (int i = 0; i < roots.length; ++i) {
			if (path.isPrefixOf(roots[i].getPath())) {
				matches.add(roots[i]);
			}
		}
		IPackageFragmentRoot[] copy = new IPackageFragmentRoot[matches.size()];
		matches.toArray(copy);
		return copy;
	}

	/**
	 * @see IJavaProject
	 */
	public IPackageFragment[] getPackageFragments() throws JavaModelException {

		IPackageFragmentRoot[] roots = getPackageFragmentRoots();
		return getPackageFragmentsInRoots(roots);
	}

	/**
	 * Returns all the package fragments found in the specified
	 * package fragment roots.
	 * @param roots IPackageFragmentRoot[]
	 * @return IPackageFragment[]
	 */
	public IPackageFragment[] getPackageFragmentsInRoots(IPackageFragmentRoot[] roots) {

		ArrayList frags = new ArrayList();
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			try {
				IJavaElement[] rootFragments = root.getChildren();
				for (int j = 0; j < rootFragments.length; j++) {
					frags.add(rootFragments[j]);
				}
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		IPackageFragment[] fragments = new IPackageFragment[frags.size()];
		frags.toArray(fragments);
		return fragments;
	}
	
	/**
	 * @see IJavaElement
	 */
	public IPath getPath() {
		return this.project.getFullPath();
	}
	
	public JavaModelManager.PerProjectInfo getPerProjectInfo() throws JavaModelException {
		return JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(this.project);
	}

	private IPath getPluginWorkingLocation() {
		return this.project.getWorkingLocation(JavaCore.PLUGIN_ID);
	}	

	/**
	 * Returns the project custom preference pool.
	 * Project preferences may include custom encoding.
	 * @return Preferences
	 * @deprecated WARNING:  this method do nothing from now and will be removed soon!
	 * 	If you use it, switch as soon as possible to new preferences API by using 
	 * 	{@link #getEclipsePreferences()} to avoid future compilation error...
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 * TODO (frederic) remove for 3.1...
	 */
	public Preferences getPreferences(){
		/*
		if (!JavaProject.hasJavaNature(this.project)) return null;
		JavaModelManager.PerProjectInfo perProjectInfo = JavaModelManager.getJavaModelManager().getPerProjectInfo(this.project, true);
		Preferences preferences =  perProjectInfo.preferences;
		if (preferences != null) return preferences;
		preferences = loadPreferences();
		if (preferences == null) preferences = new Preferences();
		perProjectInfo.preferences = preferences;
		return preferences;
		*/
		return new Preferences();
	}

	/**
	 * @see IJavaProject#getProject()
	 */
	public IProject getProject() {
		return this.project;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getRawClasspath() throws JavaModelException {
		// Do not create marker but log problems while getting raw classpath
		return getRawClasspath(false, true);
	}

	/*
	 * Internal variant allowing to parameterize problem creation/logging
	 */
	public IClasspathEntry[] getRawClasspath(boolean createMarkers, boolean logProblems) throws JavaModelException {

		JavaModelManager.PerProjectInfo perProjectInfo = null;
		IClasspathEntry[] classpath;
		if (createMarkers) {
			this.flushClasspathProblemMarkers(false/*cycle*/, true/*format*/);
			classpath = this.readClasspathFile(createMarkers, logProblems);
		} else {
			perProjectInfo = getPerProjectInfo();
			classpath = perProjectInfo.rawClasspath;
			if (classpath != null) return classpath;
			classpath = this.readClasspathFile(createMarkers, logProblems);
		}
		// extract out the output location
		IPath outputLocation = null;
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
				IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
			}
		}
		if (classpath == null) {
			return defaultClasspath();
		}
		/* Disable validate: classpath can contain CP variables and container that need to be resolved 
		if (classpath != INVALID_CLASSPATH
				&& !JavaConventions.validateClasspath(this, classpath, outputLocation).isOK()) {
			classpath = INVALID_CLASSPATH;
		}
		*/
		if (!createMarkers) {
			perProjectInfo.rawClasspath = classpath;
			perProjectInfo.outputLocation = outputLocation;
		}
		return classpath;
	}
	/**
	 * @see IJavaProject#getRequiredProjectNames()
	 */
	public String[] getRequiredProjectNames() throws JavaModelException {

		return this.projectPrerequisites(getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/));
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry)
		throws JavaModelException {

		return 
			getResolvedClasspath(
				ignoreUnresolvedEntry, 
				false, // don't generateMarkerOnError
				true // returnResolutionInProgress
			);
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] getResolvedClasspath(boolean ignoreUnresolvedEntry, boolean generateMarkerOnError)
		throws JavaModelException {

		return 
			getResolvedClasspath(
				ignoreUnresolvedEntry, 
				generateMarkerOnError,
				true // returnResolutionInProgress
			);
	}

	/*
	 * Internal variant which can create marker on project for invalid entries
	 * and caches the resolved classpath on perProjectInfo.
	 * If requested, return a special classpath (RESOLUTION_IN_PROGRESS) if the classpath is being resolved.
	 */
	public IClasspathEntry[] getResolvedClasspath(
		boolean ignoreUnresolvedEntry,
		boolean generateMarkerOnError,
		boolean returnResolutionInProgress)
		throws JavaModelException {

	    JavaModelManager manager = JavaModelManager.getJavaModelManager();
		JavaModelManager.PerProjectInfo perProjectInfo = null;
		if (ignoreUnresolvedEntry && !generateMarkerOnError) {
			perProjectInfo = getPerProjectInfo();
			if (perProjectInfo != null) {
				// resolved path is cached on its info
				IClasspathEntry[] infoPath = perProjectInfo.resolvedClasspath;
				if (infoPath != null) {
					return infoPath;
				} else if  (returnResolutionInProgress && manager.isClasspathBeingResolved(this)) {
					if (JavaModelManager.CP_RESOLVE_VERBOSE) {
						Util.verbose(
							"CPResolution: reentering raw classpath resolution, will use empty classpath instead" + //$NON-NLS-1$
							"	project: " + getElementName() + '\n' + //$NON-NLS-1$
							"	invocation stack trace:"); //$NON-NLS-1$
						new Exception("<Fake exception>").printStackTrace(System.out); //$NON-NLS-1$
					}						
				    return RESOLUTION_IN_PROGRESS;
				}
			}
		}
		Map rawReverseMap = perProjectInfo == null ? null : new HashMap(5);
		IClasspathEntry[] resolvedPath = null;
		boolean nullOldResolvedCP = perProjectInfo != null && perProjectInfo.resolvedClasspath == null;
		try {
			// protect against misbehaving clients (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=61040)
			if (nullOldResolvedCP) manager.setClasspathBeingResolved(this, true);
			resolvedPath = getResolvedClasspath(
				getRawClasspath(generateMarkerOnError, !generateMarkerOnError), 
				generateMarkerOnError ? getOutputLocation() : null, 
				ignoreUnresolvedEntry, 
				generateMarkerOnError,
				rawReverseMap);
		} finally {
			if (nullOldResolvedCP) perProjectInfo.resolvedClasspath = null;
		}

		if (perProjectInfo != null){
			if (perProjectInfo.rawClasspath == null // .classpath file could not be read
				&& generateMarkerOnError 
				&& JavaProject.hasJavaNature(this.project)) {
					// flush .classpath format markers (bug 39877), but only when file cannot be read (bug 42366)
					this.flushClasspathProblemMarkers(false, true);
					this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Messages.bind(Messages.classpath_cannotReadClasspathFile, this.getElementName()))); 
			}

			perProjectInfo.resolvedClasspath = resolvedPath;
			perProjectInfo.resolvedPathToRawEntries = rawReverseMap;
			manager.setClasspathBeingResolved(this, false);
		}
		return resolvedPath;
	}
	
	/**
	 * Internal variant which can process any arbitrary classpath
	 * @param classpathEntries IClasspathEntry[] 
	 * @param projectOutputLocation IPath
	 * @param ignoreUnresolvedEntry boolean
	 * @param generateMarkerOnError boolean
	 * @param rawReverseMap Map
	 * @return IClasspathEntry[] 
	 * @throws JavaModelException
	 */
	public IClasspathEntry[] getResolvedClasspath(
		IClasspathEntry[] classpathEntries,
		IPath projectOutputLocation, // only set if needing full classpath validation (and markers)
		boolean ignoreUnresolvedEntry, // if unresolved entries are met, should it trigger initializations
		boolean generateMarkerOnError,
		Map rawReverseMap) // can be null if not interested in reverse mapping
		throws JavaModelException {

		IJavaModelStatus status;
		if (generateMarkerOnError){
			flushClasspathProblemMarkers(false, false);
		}

		int length = classpathEntries.length;
		ArrayList resolvedEntries = new ArrayList();
		
		for (int i = 0; i < length; i++) {

			IClasspathEntry rawEntry = classpathEntries[i];
			IPath resolvedPath;
			status = null;
			
			/* validation if needed */
			if (generateMarkerOnError || !ignoreUnresolvedEntry) {
				status = ClasspathEntry.validateClasspathEntry(this, rawEntry, false /*ignore src attach*/, false /*do not recurse in containers, done later to accumulate*/);
				if (generateMarkerOnError && !status.isOK()) createClasspathProblemMarker(status);
			}

			switch (rawEntry.getEntryKind()){
				
				case IClasspathEntry.CPE_VARIABLE :
				
					IClasspathEntry resolvedEntry = null;
					try {
						resolvedEntry = JavaCore.getResolvedClasspathEntry(rawEntry);
					} catch (Assert.AssertionFailedException e) {
						// Catch the assertion failure and throw java model exception instead
						// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=55992
						// if ignoredUnresolvedEntry is false, status is set by by ClasspathEntry.validateClasspathEntry
						// called above as validation was needed
						if (!ignoreUnresolvedEntry) throw new JavaModelException(status);
					}
					if (resolvedEntry == null) {
						if (!ignoreUnresolvedEntry) throw new JavaModelException(status);
					} else {
						if (rawReverseMap != null) {
							if (rawReverseMap.get(resolvedPath = resolvedEntry.getPath()) == null) rawReverseMap.put(resolvedPath , rawEntry);
						}
						resolvedEntries.add(resolvedEntry);
					}
					break; 

				case IClasspathEntry.CPE_CONTAINER :
				
					IClasspathContainer container = JavaCore.getClasspathContainer(rawEntry.getPath(), this);
					if (container == null){
						if (!ignoreUnresolvedEntry) throw new JavaModelException(status);
						break;
					}

					IClasspathEntry[] containerEntries = container.getClasspathEntries();
					if (containerEntries == null) break;

					// container was bound
					for (int j = 0, containerLength = containerEntries.length; j < containerLength; j++){
						ClasspathEntry cEntry = (ClasspathEntry) containerEntries[j];
						if (generateMarkerOnError) {
							IJavaModelStatus containerStatus = ClasspathEntry.validateClasspathEntry(this, cEntry, false, true /*recurse*/);
							if (!containerStatus.isOK()) createClasspathProblemMarker(containerStatus);
						}
						// if container is exported or restricted, then its nested entries must in turn be exported  (21749) and/or propagate restrictions
						cEntry = cEntry.combineWith((ClasspathEntry) rawEntry);
						if (rawReverseMap != null) {
							if (rawReverseMap.get(resolvedPath = cEntry.getPath()) == null) rawReverseMap.put(resolvedPath , rawEntry);
						}
						resolvedEntries.add(cEntry);
					}
					break;
										
				default :

					if (rawReverseMap != null) {
						if (rawReverseMap.get(resolvedPath = rawEntry.getPath()) == null) rawReverseMap.put(resolvedPath , rawEntry);
					}
					resolvedEntries.add(rawEntry);
				
			}					
		}

		IClasspathEntry[] resolvedPath = new IClasspathEntry[resolvedEntries.size()];
		resolvedEntries.toArray(resolvedPath);

		if (generateMarkerOnError && projectOutputLocation != null) {
			status = ClasspathEntry.validateClasspath(this, resolvedPath, projectOutputLocation);
			if (!status.isOK()) createClasspathProblemMarker(status);
		}
		return resolvedPath;
	}

	/**
	 * @see IJavaElement
	 */
	public IResource getResource() {
		return this.project;
	}

	/**
	 * Retrieve a shared property on a project. If the property is not defined, answers null.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 *
	 * @param key String
	 * @see JavaProject#setSharedProperty(String, String)
	 * @return String
	 * @throws CoreException
	 */
	public String getSharedProperty(String key) throws CoreException {

		String property = null;
		IFile rscFile = this.project.getFile(key);
		if (rscFile.exists()) {
			byte[] bytes = Util.getResourceContentsAsByteArray(rscFile);
			try {
				property = new String(bytes, "UTF-8"); //$NON-NLS-1$ // .classpath always encoded with UTF-8
			} catch (UnsupportedEncodingException e) {
				Util.log(e, "Could not read .classpath with UTF-8 encoding"); //$NON-NLS-1$
				// fallback to default
				property = new String(bytes);
			}
		}
		return property;
	}

	/**
	 * @see JavaElement
	 */
	public SourceMapper getSourceMapper() {

		return null;
	}
	
	/**
	 * @see IJavaElement
	 */
	public IResource getUnderlyingResource() throws JavaModelException {
		if (!exists()) throw newNotPresentException();
		return this.project;
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasBuildState() {

		return JavaModelManager.getJavaModelManager().getLastBuiltState(this.project, null) != null;
	}

	/**
	 * @see IJavaProject
	 */
	public boolean hasClasspathCycle(IClasspathEntry[] preferredClasspath) {
		HashSet cycleParticipants = new HashSet();
		HashMap preferredClasspaths = new HashMap(1);
		preferredClasspaths.put(this, preferredClasspath);
		updateCycleParticipants(new ArrayList(2), cycleParticipants, ResourcesPlugin.getWorkspace().getRoot(), new HashSet(2), preferredClasspaths);
		return !cycleParticipants.isEmpty();
	}
	
	public boolean hasCycleMarker(){
		return this.getCycleMarker() != null;
	}

	public int hashCode() {
		return this.project.hashCode();
	}
	
	/**
	 * Answers true if the project potentially contains any source. A project which has no source is immutable.
	 * @return boolean
	 */
	public boolean hasSource() {

		// look if any source folder on the classpath
		// no need for resolved path given source folder cannot be abstracted
		IClasspathEntry[] entries;
		try {
			entries = this.getRawClasspath();
		} catch (JavaModelException e) {
			return true; // unsure
		}
		for (int i = 0, max = entries.length; i < max; i++) {
			if (entries[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Compare current classpath with given one to see if any different.
	 * Note that the argument classpath contains its binary output.
	 * @param newClasspath IClasspathEntry[]
	 * @param newOutputLocation IPath
	 * @param otherClasspathWithOutput IClasspathEntry[]
	 * @return boolean
	 */
	public boolean isClasspathEqualsTo(IClasspathEntry[] newClasspath, IPath newOutputLocation, IClasspathEntry[] otherClasspathWithOutput) {

		if (otherClasspathWithOutput == null || otherClasspathWithOutput.length == 0)
			return false;

		int length = otherClasspathWithOutput.length;
		if (length != newClasspath.length + 1) 
				// output is amongst file entries (last one)
				return false;
		
		
		// compare classpath entries
		for (int i = 0; i < length - 1; i++) {
			if (!otherClasspathWithOutput[i].equals(newClasspath[i]))
				return false;
		}
		// compare binary outputs
		IClasspathEntry output = otherClasspathWithOutput[length - 1];
		if (output.getContentKind() != ClasspathEntry.K_OUTPUT
				|| !output.getPath().equals(newOutputLocation))
			return false;
		return true;
	}
	

	
	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IJavaElement element) {
		IPath path = element.getPath();
		IClasspathEntry[] classpath;
		try {
			classpath = getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
		} catch(JavaModelException e){
			return false; // not a Java project
		}
		boolean isFolderPath = false;
		switch (element.getElementType()) {
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				// package fragment roots must match exactly entry pathes (no exclusion there)
				for (int i = 0; i < classpath.length; i++) {
					IClasspathEntry entry = classpath[i];
					IPath entryPath = entry.getPath();
					if (entryPath.equals(path)) {
						return true;
					}
				}
				return false;
				
			case IJavaElement.PACKAGE_FRAGMENT:
				if (!((IPackageFragmentRoot)element.getParent()).isArchive()) {
					// ensure that folders are only excluded if all of their children are excluded
					isFolderPath = true;
				}
				break;
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.isPrefixOf(path) 
					&& !Util.isExcluded(path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath)) {
				return true;
			}
		}
		return false;
	}

	/*
	 * @see IJavaProject
	 */
	public boolean isOnClasspath(IResource resource) {
		IPath exactPath = resource.getFullPath();
		IPath path = exactPath;
		
		// ensure that folders are only excluded if all of their children are excluded
		boolean isFolderPath = resource.getType() == IResource.FOLDER;
		
		IClasspathEntry[] classpath;
		try {
			classpath = this.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
		} catch(JavaModelException e){
			return false; // not a Java project
		}
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath entryPath = entry.getPath();
			if (entryPath.equals(exactPath)) { // package fragment roots must match exactly entry pathes (no exclusion there)
				return true;
			}
			if (entryPath.isPrefixOf(path) 
					&& !Util.isExcluded(path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), isFolderPath)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * load preferences from a shareable format (VCM-wise)
	 * @deprecated WARNING, visibility of this method will be decreased soon
	 * 	to private and won't be usable in the future.
	 * @see <a href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=59258">bug 59258</a>
	 * TODO (frederic) set visibility from public to private
	 */
	 public Preferences loadPreferences() {
	 	
	 	Preferences preferences = new Preferences();
	 	IPath projectMetaLocation = getPluginWorkingLocation();
		if (projectMetaLocation != null) {
			File prefFile = projectMetaLocation.append(PREF_FILENAME).toFile();
			if (prefFile.exists()) { // load preferences from file
				InputStream in = null;
				try {
					in = new BufferedInputStream(new FileInputStream(prefFile));
					preferences.load(in);
				} catch (IOException e) { // problems loading preference store - quietly ignore
				} finally {
					if (in != null) {
						try {
							in.close();
						} catch (IOException e) { // ignore problems with close
						}
					}
				}
				// one shot read, delete old preferences
				prefFile.delete();
				return preferences;
			}
		}
		return null;
	 }

	/**
	 * @see IJavaProject#newEvaluationContext()
	 */
	public IEvaluationContext newEvaluationContext() {

		return new EvaluationContextWrapper(new EvaluationContext(), this);
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the given working copies.
	 */
	public NameLookup newNameLookup(ICompilationUnit[] workingCopies) throws JavaModelException {
		return getJavaProjectElementInfo().newNameLookup(this, workingCopies);
	}

	/*
	 * Returns a new name lookup. This name lookup first looks in the working copies of the given owner.
	 */
	public NameLookup newNameLookup(WorkingCopyOwner owner) throws JavaModelException {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		ICompilationUnit[] workingCopies = owner == null ? null : manager.getWorkingCopies(owner, true/*add primary WCs*/);
		return newNameLookup(workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the given working copies.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(ICompilationUnit[] workingCopies) throws JavaModelException {
		return new SearchableEnvironment(this, workingCopies);
	}

	/*
	 * Returns a new search name environment for this project. This name environment first looks in the working copies
	 * of the given owner.
	 */
	public SearchableEnvironment newSearchableNameEnvironment(WorkingCopyOwner owner) throws JavaModelException {
		return new SearchableEnvironment(this, owner);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {
			
		return newTypeHierarchy(region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, null, true);
		op.runOperation(monitor);
		return op.getResult();
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		IProgressMonitor monitor)
		throws JavaModelException {
			
		return newTypeHierarchy(type, region, DefaultWorkingCopyOwner.PRIMARY, monitor);
	}

	/**
	 * @see IJavaProject
	 */
	public ITypeHierarchy newTypeHierarchy(
		IType type,
		IRegion region,
		WorkingCopyOwner owner,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (type == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullFocusType);
		}
		if (region == null) {
			throw new IllegalArgumentException(Messages.hierarchy_nullRegion);
		}
		ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(owner, true/*add primary working copies*/);
		CreateTypeHierarchyOperation op =
			new CreateTypeHierarchyOperation(region, workingCopies, type, true/*compute subtypes*/);
		op.runOperation(monitor);
		return op.getResult();
	}
	public String[] projectPrerequisites(IClasspathEntry[] entries)
		throws JavaModelException {
			
		ArrayList prerequisites = new ArrayList();
		// need resolution
		entries = getResolvedClasspath(entries, null, true, false, null/*no reverse map*/);
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = entries[i];
			if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				prerequisites.add(entry.getPath().lastSegment());
			}
		}
		int size = prerequisites.size();
		if (size == 0) {
			return NO_PREREQUISITES;
		} else {
			String[] result = new String[size];
			prerequisites.toArray(result);
			return result;
		}
	}


	/**
	 * Reads the .classpath file from disk and returns the list of entries it contains (including output location entry)
	 * Returns null if .classfile is not present.
	 * Returns INVALID_CLASSPATH if it has a format problem.
	 */
	protected IClasspathEntry[] readClasspathFile(boolean createMarker, boolean logProblems) {

		try {
			String xmlClasspath = getSharedProperty(CLASSPATH_FILENAME);
			if (xmlClasspath == null) {
				if (createMarker && this.project.isAccessible()) {
						this.createClasspathProblemMarker(new JavaModelStatus(
							IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
							Messages.bind(Messages.classpath_cannotReadClasspathFile, this.getElementName()))); 
				}
				return null;
			}
			return decodeClasspath(xmlClasspath, createMarker, logProblems);
		} catch(CoreException e) {
			// file does not exist (or not accessible)
			if (createMarker && this.project.isAccessible()) {
					this.createClasspathProblemMarker(new JavaModelStatus(
						IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
						Messages.bind(Messages.classpath_cannotReadClasspathFile, this.getElementName()))); 
			}
			if (logProblems) {
				Util.log(e, 
					"Exception while retrieving "+ this.getPath() //$NON-NLS-1$
					+"/.classpath, will revert to default classpath"); //$NON-NLS-1$
			}
		}
		return null;
	}

	/**
	 * @see IJavaProject
	 */
	public IPath readOutputLocation() {

		// Read classpath file without creating markers nor logging problems
		IClasspathEntry[] classpath = this.readClasspathFile(false, false);
		// extract the output location
		IPath outputLocation = null;
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				outputLocation = entry.getPath();
			}
		}
		return outputLocation;
	}

	/**
	 * @see IJavaProject
	 */
	public IClasspathEntry[] readRawClasspath() {

		// Read classpath file without creating markers nor logging problems
		IClasspathEntry[] classpath = this.readClasspathFile(false, false);
		// discard the output location
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
			}
		}
		return classpath;
	}

	/**
	 * Removes the given builder from the build spec for the given project.
	 */
	protected void removeFromBuildSpec(String builderID) throws CoreException {

		IProjectDescription description = this.project.getDescription();
		ICommand[] commands = description.getBuildSpec();
		for (int i = 0; i < commands.length; ++i) {
			if (commands[i].getBuilderName().equals(builderID)) {
				ICommand[] newCommands = new ICommand[commands.length - 1];
				System.arraycopy(commands, 0, newCommands, 0, i);
				System.arraycopy(commands, i + 1, newCommands, i, commands.length - i - 1);
				description.setBuildSpec(newCommands);
				this.project.setDescription(description, null);
				return;
			}
		}
	}
	
	/*
	 * Resets this project's caches
	 */
	public void resetCaches() {
		JavaProjectElementInfo info = (JavaProjectElementInfo) JavaModelManager.getJavaModelManager().peekAtInfo(this);
		if (info != null){
			info.resetCaches();
		}
	}

	/**
	 * Answers an ID which is used to distinguish project/entries during package
	 * fragment root computations
	 * @return String
	 */
	public String rootID(){
		return "[PRJ]"+this.project.getFullPath(); //$NON-NLS-1$
	}
	
	/**
	 * Saves the classpath in a shareable format (VCM-wise) only when necessary, that is, if  it is semantically different
	 * from the existing one in file. Will never write an identical one.
	 * 
	 * @param newClasspath IClasspathEntry[]
	 * @param newOutputLocation IPath
	 * @return boolean Return whether the .classpath file was modified.
	 * @throws JavaModelException
	 */
	public boolean saveClasspath(IClasspathEntry[] newClasspath, IPath newOutputLocation) throws JavaModelException {

		if (!this.project.isAccessible()) return false;

		IClasspathEntry[] fileEntries = readClasspathFile(false /*don't create markers*/, false/*don't log problems*/);
		if (fileEntries != null && isClasspathEqualsTo(newClasspath, newOutputLocation, fileEntries)) {
			// no need to save it, it is the same
			return false;
		}

		// actual file saving
		try {
			setSharedProperty(CLASSPATH_FILENAME, encodeClasspath(newClasspath, newOutputLocation, true));
			return true;
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Update the Java command in the build spec (replace existing one if present,
	 * add one first if none).
	 */
	private void setJavaCommand(
		IProjectDescription description,
		ICommand newCommand)
		throws CoreException {

		ICommand[] oldBuildSpec = description.getBuildSpec();
		int oldJavaCommandIndex = getJavaCommandIndex(oldBuildSpec);
		ICommand[] newCommands;

		if (oldJavaCommandIndex == -1) {
			// Add a Java build spec before other builders (1FWJK7I)
			newCommands = new ICommand[oldBuildSpec.length + 1];
			System.arraycopy(oldBuildSpec, 0, newCommands, 1, oldBuildSpec.length);
			newCommands[0] = newCommand;
		} else {
		    oldBuildSpec[oldJavaCommandIndex] = newCommand;
			newCommands = oldBuildSpec;
		}

		// Commit the spec change into the project
		description.setBuildSpec(newCommands);
		this.project.setDescription(description, null);
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#setOption(java.lang.String, java.lang.String)
	 */
	public void setOption(String optionName, String optionValue) {
		if (!JavaModelManager.getJavaModelManager().optionNames.contains(optionName)) return; // unrecognized option
		IEclipsePreferences projectPreferences = getEclipsePreferences();
		String defaultValue = JavaCore.getOption(optionName);
		if (defaultValue == null || !defaultValue.equals(optionValue)) {
			projectPreferences.put(optionName, optionValue);
			try {
				projectPreferences.flush();
			} catch (BackingStoreException e) {
				// problem with pref store - quietly ignore
			}
		}
	}

	/**
	 * @see org.eclipse.jdt.core.IJavaProject#setOptions(Map)
	 */
	public void setOptions(Map newOptions) {

		IEclipsePreferences projectPreferences = getEclipsePreferences();
		try {
			if (newOptions == null){
				projectPreferences.clear();
			} else {
				Iterator keys = newOptions.keySet().iterator();
				while (keys.hasNext()){
					String key = (String)keys.next();
					if (!JavaModelManager.getJavaModelManager().optionNames.contains(key)) continue; // unrecognized option
					// no filtering for encoding (custom encoding for project is allowed)
					String value = (String)newOptions.get(key);
					projectPreferences.put(key, value);
				}
				
				// reset to default all options not in new map
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=26255
				// @see https://bugs.eclipse.org/bugs/show_bug.cgi?id=49691
				String[] pNames = projectPreferences.keys();
				int ln = pNames.length;
				for (int i=0; i<ln; i++) {
					String key = pNames[i];
					if (!newOptions.containsKey(key)) {
						projectPreferences.remove(key); // old preferences => remove from preferences table
					}
				}
			}

			// persist options
			projectPreferences.flush();
			
			// flush cache immediately
			try {
				getPerProjectInfo().options = null;
			} catch (JavaModelException e) {
				// do nothing
			}
		} catch (BackingStoreException e) {
			// problem with pref store - quietly ignore
		}
	}

	/**
	 * @see IJavaProject
	 */
	public void setOutputLocation(IPath path, IProgressMonitor monitor)
		throws JavaModelException {

		if (path == null) {
			throw new IllegalArgumentException(Messages.path_nullPath); 
		}
		if (path.equals(getOutputLocation())) {
			return;
		}
		this.setRawClasspath(SetClasspathOperation.REUSE_ENTRIES, path, monitor);
	}

	/**
	 * Sets the underlying kernel project of this Java project,
	 * and fills in its parent and name.
	 * Called by IProject.getNature().
	 *
	 * @see IProjectNature#setProject(IProject)
	 */
	public void setProject(IProject project) {

		this.project = project;
		this.parent = JavaModelManager.getJavaModelManager().getJavaModel();
	}

	/**
	 * @see IJavaProject#setRawClasspath(IClasspathEntry[],IPath,IProgressMonitor)
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IPath outputLocation,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			outputLocation,
			monitor, 
			true, // canChangeResource (as per API contract)
			getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/),
			true, // needValidation
			true); // need to save
	}

	public void setRawClasspath(
		IClasspathEntry[] newEntries,
		IPath newOutputLocation,
		IProgressMonitor monitor,
		boolean canChangeResource,
		IClasspathEntry[] oldResolvedPath,
		boolean needValidation,
		boolean needSave)
		throws JavaModelException {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			IClasspathEntry[] newRawPath = newEntries;
			if (newRawPath == null) { //are we already with the default classpath
				newRawPath = defaultClasspath();
			}
			SetClasspathOperation op =
				new SetClasspathOperation(
					this, 
					oldResolvedPath, 
					newRawPath, 
					newOutputLocation,
					canChangeResource, 
					needValidation,
					needSave);
			op.runOperation(monitor);
			
		} catch (JavaModelException e) {
			manager.getDeltaProcessor().flush();
			throw e;
		}
	}

	/**
	 * @see IJavaProject
	 */
	public void setRawClasspath(
		IClasspathEntry[] entries,
		IProgressMonitor monitor)
		throws JavaModelException {

		setRawClasspath(
			entries, 
			SetClasspathOperation.REUSE_PATH, 
			monitor, 
			true, // canChangeResource (as per API contract)
			getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/),
			true, // needValidation
			true); // need to save
	}

	/**
	 * Record a shared persistent property onto a project.
	 * Note that it is orthogonal to IResource persistent properties, and client code has to decide
	 * which form of storage to use appropriately. Shared properties produce real resource files which
	 * can be shared through a VCM onto a server. Persistent properties are not shareable.
	 * 
	 * shared properties end up in resource files, and thus cannot be modified during
	 * delta notifications (a CoreException would then be thrown).
	 * 
	 * @param key String
	 * @param value String
	 * @see JavaProject#getSharedProperty(String key)
	 * @throws CoreException
	 */
	public void setSharedProperty(String key, String value) throws CoreException {

		IFile rscFile = this.project.getFile(key);
		byte[] bytes = null;
		try {
			bytes = value.getBytes("UTF-8"); //$NON-NLS-1$ // .classpath always encoded with UTF-8
		} catch (UnsupportedEncodingException e) {
			Util.log(e, "Could not write .classpath with UTF-8 encoding "); //$NON-NLS-1$
			// fallback to default
			bytes = value.getBytes();
		}
		InputStream inputStream = new ByteArrayInputStream(bytes);
		// update the resource content
		if (rscFile.exists()) {
			if (rscFile.isReadOnly()) {
				// provide opportunity to checkout read-only .classpath file (23984)
				ResourcesPlugin.getWorkspace().validateEdit(new IFile[]{rscFile}, null);
			}
			rscFile.setContents(inputStream, IResource.FORCE, null);
		} else {
			rscFile.create(inputStream, IResource.FORCE, null);
		}
	}
	
	/*
	 * Update .classpath format markers.
	 */
	public void updateClasspathMarkers(Map preferredClasspaths, Map preferredOutputs) {
		
		this.flushClasspathProblemMarkers(false/*cycle*/, true/*format*/);
		this.flushClasspathProblemMarkers(false/*cycle*/, false/*format*/);

		IClasspathEntry[] classpath = this.readClasspathFile(true/*marker*/, false/*log*/);
		IPath output = null;
		// discard the output location
		if (classpath != null && classpath.length > 0) {
			IClasspathEntry entry = classpath[classpath.length - 1];
			if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
				IClasspathEntry[] copy = new IClasspathEntry[classpath.length - 1];
				System.arraycopy(classpath, 0, copy, 0, copy.length);
				classpath = copy;
				output = entry.getPath();
			}
		}					
		// remember invalid path so as to avoid reupdating it again later on
		if (preferredClasspaths != null) {
			preferredClasspaths.put(this, classpath == null ? INVALID_CLASSPATH : classpath);
		}
		if (preferredOutputs != null) {
			preferredOutputs.put(this, output == null ? defaultOutputLocation() : output);
		}
		
		 // force classpath marker refresh
		 if (classpath != null && output != null) {
		 	for (int i = 0; i < classpath.length; i++) {
				IJavaModelStatus status = ClasspathEntry.validateClasspathEntry(this, classpath[i], false/*src attach*/, true /*recurse in container*/);
				if (!status.isOK()) this.createClasspathProblemMarker(status);					 
			 }
			IJavaModelStatus status = ClasspathEntry.validateClasspath(this, classpath, output);
			if (!status.isOK()) this.createClasspathProblemMarker(status);
		 }
	}

	/**
	 * If a cycle is detected, then cycleParticipants contains all the paths of projects involved in this cycle (directly and indirectly),
	 * no cycle if the set is empty (and started empty)
	 * @param prereqChain ArrayList
	 * @param cycleParticipants HashSet
	 * @param workspaceRoot IWorkspaceRoot
	 * @param traversed HashSet
	 * @param preferredClasspaths Map
	 */
	public void updateCycleParticipants(
			ArrayList prereqChain, 
			HashSet cycleParticipants, 
			IWorkspaceRoot workspaceRoot,
			HashSet traversed,
			Map preferredClasspaths){

		IPath path = this.getPath();
		prereqChain.add(path);
		traversed.add(path);
		try {
			IClasspathEntry[] classpath = null;
			if (preferredClasspaths != null) classpath = (IClasspathEntry[])preferredClasspaths.get(this);
			if (classpath == null) classpath = getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/);
			for (int i = 0, length = classpath.length; i < length; i++) {
				IClasspathEntry entry = classpath[i];
				
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT){
					IPath prereqProjectPath = entry.getPath();
					int index = cycleParticipants.contains(prereqProjectPath) ? 0 : prereqChain.indexOf(prereqProjectPath);
					if (index >= 0) { // refer to cycle, or in cycle itself
						for (int size = prereqChain.size(); index < size; index++) {
							cycleParticipants.add(prereqChain.get(index)); 
						}
					} else {
						if (!traversed.contains(prereqProjectPath)) {
							IResource member = workspaceRoot.findMember(prereqProjectPath);
							if (member != null && member.getType() == IResource.PROJECT){
								JavaProject javaProject = (JavaProject)JavaCore.create((IProject)member);
								javaProject.updateCycleParticipants(prereqChain, cycleParticipants, workspaceRoot, traversed, preferredClasspaths);
							}
						}
					}
				}
			}
		} catch(JavaModelException e){
			// project doesn't exist: ignore
		}
		prereqChain.remove(path);
	}
	
	/**
	 * Reset the collection of package fragment roots (local ones) - only if opened.
	 */
	public void updatePackageFragmentRoots(){
		
			if (this.isOpen()) {
				try {
					JavaProjectElementInfo info = getJavaProjectElementInfo();
					computeChildren(info);
					info.resetCaches(); // discard caches (hold onto roots and pkg fragments)
				} catch(JavaModelException e){
					try {
						close(); // could not do better
					} catch(JavaModelException ex){
						// ignore
					}
				}
			}
	}

	/*
	 * Update eclipse preferences from old preferences.
	 */
	 private void updatePreferences(IEclipsePreferences preferences) {
	 	
	 	Preferences oldPreferences = loadPreferences();
	 	if (oldPreferences != null) {
	 		String[] propertyNames = oldPreferences.propertyNames();
			for (int i = 0; i < propertyNames.length; i++){
				String propertyName = propertyNames[i];
			    String propertyValue = oldPreferences.getString(propertyName);
			    if (!"".equals(propertyValue)) { //$NON-NLS-1$
				    preferences.put(propertyName, propertyValue);
			    }
			}
			try {
				// save immediately old preferences
				preferences.flush();
			} catch (BackingStoreException e) {
				// fails silently
			}
		}
	 }
}

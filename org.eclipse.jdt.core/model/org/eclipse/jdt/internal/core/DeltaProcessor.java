/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.File;
import java.util.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.builder.JavaBuilder;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * This class is used by <code>JavaModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
 * It also does some processing on the <code>JavaElement</code>s involved
 * (e.g. closing them or updating classpaths).
 */
public class DeltaProcessor implements IResourceChangeListener {
	
	final static int IGNORE = 0;
	final static int SOURCE = 1;
	final static int BINARY = 2;
	
	final static String EXTERNAL_JAR_ADDED = "external jar added"; //$NON-NLS-1$
	final static String EXTERNAL_JAR_REMOVED = "external jar removed"; //$NON-NLS-1$
	final static String EXTERNAL_JAR_CHANGED = "external jar changed"; //$NON-NLS-1$
	final static String EXTERNAL_JAR_UNCHANGED = "external jar unchanged"; //$NON-NLS-1$
	final static String INTERNAL_JAR_IGNORE = "internal jar ignore"; //$NON-NLS-1$
	
	final static int NON_JAVA_RESOURCE = -1;
	
	/**
	 * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	protected JavaElementDelta currentDelta;
	
	protected IndexManager indexManager = new IndexManager();
		
	/* A table from IPath (from a classpath entry) to RootInfo */
	public Map roots;
	
	/* A table from IPath (from a classpath entry) to ArrayList of RootInfo
	 * Used when an IPath corresponds to more than one root */
	Map otherRoots;
	
	/* Whether the roots tables should be recomputed */
	public boolean rootsAreStale = true;
	
	/* A table from IPath (from a classpath entry) to RootInfo
	 * from the last time the delta processor was invoked. */
	public Map oldRoots;

	/* A table from IPath (from a classpath entry) to ArrayList of RootInfo
	 * from the last time the delta processor was invoked.
	 * Used when an IPath corresponds to more than one root */
	Map oldOtherRoots;
	
	/* A table from IPath (a source attachment path from a classpath entry) to IPath (a root path) */
	Map sourceAttachments;

	/* The java element that was last created (see createElement(IResource)). 
	 * This is used as a stack of java elements (using getParent() to pop it, and 
	 * using the various get*(...) to push it. */
	Openable currentElement;
		
	public HashMap externalTimeStamps = new HashMap();
	public HashSet projectsToUpdate = new HashSet();
	// list of root projects which namelookup caches need to be updated for dependents
	// TODO: (jerome) is it needed? projectsToUpdate might be sufficient
	public HashSet projectsForDependentNamelookupRefresh = new HashSet();  
	
	JavaModelManager manager;
	
	/* A table from IJavaProject to an array of IPackageFragmentRoot.
	 * This table contains the pkg fragment roots of the project that are being deleted.
	 */
	Map removedRoots;
	
	/*
	 * A list of IJavaElement used as a scope for external archives refresh during POST_CHANGE.
	 * This is null if no refresh is needed.
	 */
	HashSet refreshedElements;
	public static boolean VERBOSE = false;
	
	class OutputsInfo {
		IPath[] paths;
		int[] traverseModes;
		int outputCount;
		OutputsInfo(IPath[] paths, int[] traverseModes, int outputCount) {
			this.paths = paths;
			this.traverseModes = traverseModes;
			this.outputCount = outputCount;
		}
		public String toString() {
			if (this.paths == null) return "<none>"; //$NON-NLS-1$
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < this.outputCount; i++) {
				buffer.append("path="); //$NON-NLS-1$
				buffer.append(this.paths[i].toString());
				buffer.append("\n->traverse="); //$NON-NLS-1$
				switch (this.traverseModes[i]) {
					case BINARY:
						buffer.append("BINARY"); //$NON-NLS-1$
						break;
					case IGNORE:
						buffer.append("IGNORE"); //$NON-NLS-1$
						break;
					case SOURCE:
						buffer.append("SOURCE"); //$NON-NLS-1$
						break;
					default:
						buffer.append("<unknown>"); //$NON-NLS-1$
				}
				if (i+1 < this.outputCount) {
					buffer.append('\n');
				}
			}
			return buffer.toString();
		}
	}
	class RootInfo {
		IJavaProject project;
		IPath rootPath;
		char[][] exclusionPatterns;
		RootInfo(IJavaProject project, IPath rootPath, char[][] exclusionPatterns) {
			this.project = project;
			this.rootPath = rootPath;
			this.exclusionPatterns = exclusionPatterns;
		}
		boolean isRootOfProject(IPath path) {
			return this.rootPath.equals(path) && this.project.getProject().getFullPath().isPrefixOf(path);
		}
		public String toString() {
			StringBuffer buffer = new StringBuffer("project="); //$NON-NLS-1$
			if (this.project == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				buffer.append(this.project.getElementName());
			}
			buffer.append("\npath="); //$NON-NLS-1$
			if (this.rootPath == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				buffer.append(this.rootPath.toString());
			}
			buffer.append("\nexcluding="); //$NON-NLS-1$
			if (this.exclusionPatterns == null) {
				buffer.append("null"); //$NON-NLS-1$
			} else {
				for (int i = 0, length = this.exclusionPatterns.length; i < length; i++) {
					buffer.append(new String(this.exclusionPatterns[i]));
					if (i < length-1) {
						buffer.append("|"); //$NON-NLS-1$
					}
				}
			}
			return buffer.toString();
		}
	}

	DeltaProcessor(JavaModelManager manager) {
		this.manager = manager;
	}

	/*
	 * Adds the dependents of the given project to the list of the projects
	 * to update.
	 */
	void addDependentProjects(IPath projectPath, HashSet result) {
		try {
			IJavaProject[] projects = this.manager.getJavaModel().getJavaProjects();
			for (int i = 0, length = projects.length; i < length; i++) {
				IJavaProject project = projects[i];
				IClasspathEntry[] classpath = ((JavaProject)project).getExpandedClasspath(true);
				for (int j = 0, length2 = classpath.length; j < length2; j++) {
					IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
								&& entry.getPath().equals(projectPath)) {
							result.add(project);
						}
					}
				}
		} catch (JavaModelException e) {
		}
	}
	/*
	 * Adds the given element to the list of elements used as a scope for external jars refresh.
	 */
	public void addForRefresh(IJavaElement element) {
		if (this.refreshedElements == null) {
			this.refreshedElements = new HashSet();
		}
		this.refreshedElements.add(element);
	}
	/*
	 * Adds the given project and its dependents to the list of the projects
	 * to update.
	 */
	void addToProjectsToUpdateWithDependents(IProject project) {
		this.projectsToUpdate.add(JavaCore.create(project));
		this.addDependentProjects(project.getFullPath(), this.projectsToUpdate);
	}
	
	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	protected void addToParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			try {
				JavaElementInfo info = (JavaElementInfo)parent.getElementInfo();
				info.addChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}

	/**
	 * Check all external archive (referenced by given roots, projects or model) status and issue a corresponding root delta.
	 * Also triggers index updates
	 */
	public void checkExternalArchiveChanges(IJavaElement[] refreshedElements, IProgressMonitor monitor) throws JavaModelException {
		try {
			for (int i = 0, length = refreshedElements.length; i < length; i++) {
				this.addForRefresh(refreshedElements[i]);
			}
			boolean hasDelta = this.createExternalArchiveDelta(monitor);
			if (monitor != null && monitor.isCanceled()) return; 
			if (hasDelta){
				// force classpath marker refresh of affected projects
				JavaModel.flushExternalFileCache();
				IJavaElementDelta[] projectDeltas = this.currentDelta.getAffectedChildren();
				for (int i = 0, length = projectDeltas.length; i < length; i++) {
					IJavaElementDelta delta = projectDeltas[i];
					((JavaProject)delta.getElement()).getResolvedClasspath(
						true, // ignoreUnresolvedEntry
						true); // generateMarkerOnError
				}		
				if (this.currentDelta != null) { // if delta has not been fired while creating markers
					this.manager.fire(this.currentDelta, JavaModelManager.DEFAULT_CHANGE_EVENT);
				}
			}
		} finally {
			this.currentDelta = null;
			if (monitor != null) monitor.done();
		}
	}
	/*
	 * Check if external archives have changed and create the corresponding deltas.
	 * Returns whether at least on delta was created.
	 */
	public boolean createExternalArchiveDelta(IProgressMonitor monitor) throws JavaModelException {
		
		if (this.refreshedElements == null) return false;
			
		HashMap externalArchivesStatus = new HashMap();
		boolean hasDelta = false;
		
		// find JARs to refresh
		HashSet archivePathsToRefresh = new HashSet();
		try {
			Iterator iterator = this.refreshedElements.iterator();
			while (iterator.hasNext()) {
				IJavaElement element = (IJavaElement)iterator.next();
				switch(element.getElementType()){
					case IJavaElement.PACKAGE_FRAGMENT_ROOT :
						archivePathsToRefresh.add(element.getPath());
						break;
					case IJavaElement.JAVA_PROJECT :
						IJavaProject project = (IJavaProject) element;
						if (!JavaProject.hasJavaNature(project.getProject())) {
							// project is not accessible or has lost its Java nature
							break;
						}
						IClasspathEntry[] classpath = project.getResolvedClasspath(true);
						for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
							if (classpath[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
								archivePathsToRefresh.add(classpath[j].getPath());
							}
						}
						break;
					case IJavaElement.JAVA_MODEL :
						IJavaProject[] projects = manager.getJavaModel().getOldJavaProjectsList();
						for (int j = 0, projectsLength = projects.length; j < projectsLength; j++){
							project = projects[j];
							if (!JavaProject.hasJavaNature(project.getProject())) {
								// project is not accessible or has lost its Java nature
								continue;
							}
							classpath = project.getResolvedClasspath(true);
							for (int k = 0, cpLength = classpath.length; k < cpLength; k++){
								if (classpath[k].getEntryKind() == IClasspathEntry.CPE_LIBRARY){
									archivePathsToRefresh.add(classpath[k].getPath());
								}
							}
						}
						break;
				}
			}
		} finally {
			this.refreshedElements = null;
		}
		
		// perform refresh
		IJavaProject[] projects = manager.getJavaModel().getOldJavaProjectsList();
		IWorkspaceRoot wksRoot = ResourcesPlugin.getWorkspace().getRoot();
		for (int i = 0, length = projects.length; i < length; i++) {
			
			if (monitor != null && monitor.isCanceled()) break; 
			
			IJavaProject project = projects[i];
			if (!JavaProject.hasJavaNature(project.getProject())) {
				// project is not accessible or has lost its Java nature
				continue;
			}
			IClasspathEntry[] entries = project.getResolvedClasspath(true);
			for (int j = 0; j < entries.length; j++){
				if (entries[j].getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
					
					IPath entryPath = entries[j].getPath();
					
					if (!archivePathsToRefresh.contains(entryPath)) continue; // not supposed to be refreshed
					
					String status = (String)externalArchivesStatus.get(entryPath); 
					if (status == null){
						
						// compute shared status
						Object targetLibrary = JavaModel.getTarget(wksRoot, entryPath, true);
		
						if (targetLibrary == null){ // missing JAR
							if (this.externalTimeStamps.containsKey(entryPath)){
								this.externalTimeStamps.remove(entryPath);
								externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
								// the jar was physically removed: remove the index
								indexManager.removeIndex(entryPath);
							}
		
						} else if (targetLibrary instanceof File){ // external JAR
		
							File externalFile = (File)targetLibrary;
							
							// check timestamp to figure if JAR has changed in some way
							Long oldTimestamp =(Long) this.externalTimeStamps.get(entryPath);
							long newTimeStamp = getTimeStamp(externalFile);
							if (oldTimestamp != null){
		
								if (newTimeStamp == 0){ // file doesn't exist
									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_REMOVED);
									this.externalTimeStamps.remove(entryPath);
									// remove the index
									indexManager.removeIndex(entryPath);
		
								} else if (oldTimestamp.longValue() != newTimeStamp){
									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_CHANGED);
									this.externalTimeStamps.put(entryPath, new Long(newTimeStamp));
									// first remove the index so that it is forced to be re-indexed
									indexManager.removeIndex(entryPath);
									// then index the jar
									indexManager.indexLibrary(entryPath, project.getProject());
								} else {
									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
								}
							} else {
								if (newTimeStamp == 0){ // jar still doesn't exist
									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_UNCHANGED);
								} else {
									externalArchivesStatus.put(entryPath, EXTERNAL_JAR_ADDED);
									this.externalTimeStamps.put(entryPath, new Long(newTimeStamp));
									// index the new jar
									indexManager.indexLibrary(entryPath, project.getProject());
								}
							}
						} else { // internal JAR
							externalArchivesStatus.put(entryPath, INTERNAL_JAR_IGNORE);
						}
					}
					// according to computed status, generate a delta
					status = (String)externalArchivesStatus.get(entryPath); 
					if (status != null){
						if (status == EXTERNAL_JAR_ADDED){
							PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
							if (VERBOSE){
								System.out.println("- External JAR ADDED, affecting root: "+root.getElementName()); //$NON-NLS-1$
							} 
							elementAdded(root, null, null);
							hasDelta = true;
						} else if (status == EXTERNAL_JAR_CHANGED) {
							PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
							if (VERBOSE){
								System.out.println("- External JAR CHANGED, affecting root: "+root.getElementName()); //$NON-NLS-1$
							}
							// reset the corresponding project built state, since the builder would miss this change
							this.manager.setLastBuiltState(project.getProject(), null /*no state*/);
							contentChanged(root, null);
							hasDelta = true;
						} else if (status == EXTERNAL_JAR_REMOVED) {
							PackageFragmentRoot root = (PackageFragmentRoot)project.getPackageFragmentRoot(entryPath.toString());
							if (VERBOSE){
								System.out.println("- External JAR REMOVED, affecting root: "+root.getElementName()); //$NON-NLS-1$
							}
							elementRemoved(root, null, null);
							hasDelta = true;
						}
					}
				}
			}
		}
		return hasDelta;
	}
	JavaElementDelta currentDelta() {
		if (this.currentDelta == null) {
			this.currentDelta = new JavaElementDelta(this.manager.getJavaModel());
		}
		return this.currentDelta;
	}
	
	/*
	 * Process the given delta and look for projects being added, opened, closed or
	 * with a java nature being added or removed.
	 * Note that projects being deleted are checked in deleting(IProject).
	 * In all cases, add the project's dependents to the list of projects to update
	 * so that the classpath related markers can be updated.
	 */
	public void checkProjectsBeingAddedOrRemoved(IResourceDelta delta) {
		IResource resource = delta.getResource();
		switch (resource.getType()) {
			case IResource.ROOT :
				// workaround for bug 15168 circular errors not reported 
				if (this.manager.javaProjectsCache == null) {
					try {
						this.manager.javaProjectsCache = this.manager.getJavaModel().getJavaProjects();
					} catch (JavaModelException e) {
					}
				}
				
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++) {
					this.checkProjectsBeingAddedOrRemoved(children[i]);
				}
				break;
			case IResource.PROJECT :
				// NB: No need to check project's nature as if the project is not a java project:
				//     - if the project is added or changed this is a noop for projectsBeingDeleted
				//     - if the project is closed, it has already lost its java nature
				int deltaKind = delta.getKind();
				if (deltaKind == IResourceDelta.ADDED) {
					// remember project and its dependents
					IProject project = (IProject)resource;
					this.addToProjectsToUpdateWithDependents(project);
					
					// workaround for bug 15168 circular errors not reported 
					if (JavaProject.hasJavaNature(project)) {
						this.addToParentInfo((JavaProject)JavaCore.create(project));
					}

				} else if (deltaKind == IResourceDelta.CHANGED) {
					IProject project = (IProject)resource;
					if ((delta.getFlags() & IResourceDelta.OPEN) != 0) {
						// project opened or closed: remember  project and its dependents
						this.addToProjectsToUpdateWithDependents(project);
						
						// workaround for bug 15168 circular errors not reported 
						if (project.isOpen()) {
							if (JavaProject.hasJavaNature(project)) {
								this.addToParentInfo((JavaProject)JavaCore.create(project));
							}
						} else {
							JavaProject javaProject = (JavaProject)this.manager.getJavaModel().findJavaProject(project);
							if (javaProject != null) {
								try {
									javaProject.close();
								} catch (JavaModelException e) {
								}
								this.removeFromParentInfo(javaProject);
							}
						}
					} else if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
						boolean wasJavaProject = this.manager.getJavaModel().findJavaProject(project) != null;
						boolean isJavaProject = JavaProject.hasJavaNature(project);
						if (wasJavaProject != isJavaProject) {
							// java nature added or removed: remember  project and its dependents
							this.addToProjectsToUpdateWithDependents(project);

							// workaround for bug 15168 circular errors not reported 
							if (isJavaProject) {
								this.addToParentInfo((JavaProject)JavaCore.create(project));
							} else {
								JavaProject javaProject = (JavaProject)JavaCore.create(project);
								
								// flush classpath markers
								javaProject.
									flushClasspathProblemMarkers(
										true, // flush cycle markers
										true  //flush classpath format markers
									);
									
								// remove problems and tasks created  by the builder
								JavaBuilder.removeProblemsAndTasksFor(project);

								// close project
								try {
									javaProject.close();
								} catch (JavaModelException e) {
								}
								this.removeFromParentInfo(javaProject);
							}
						} else {
							// in case the project was removed then added then changed (see bug 19799)
							if (JavaProject.hasJavaNature(project)) { // need nature check - 18698
								this.addToParentInfo((JavaProject)JavaCore.create(project));
							}
						}
					} else {
						// workaround for bug 15168 circular errors not reported 
						// in case the project was removed then added then changed
						if (JavaProject.hasJavaNature(project)) { // need nature check - 18698
							this.addToParentInfo((JavaProject)JavaCore.create(project));
						}						
					}					
				}
				break;
		}
	}

	private void checkSourceAttachmentChange(IResourceDelta delta, IResource res) {
		IPath rootPath = (IPath)this.sourceAttachments.get(res.getFullPath());
		if (rootPath != null) {
			RootInfo rootInfo = this.rootInfo(rootPath, delta.getKind());
			if (rootInfo != null) {
				IJavaProject projectOfRoot = rootInfo.project;
				IPackageFragmentRoot root = null;
				try {
					// close the root so that source attachement cache is flushed
					root = projectOfRoot.findPackageFragmentRoot(rootPath);
					if (root != null) {
						root.close();
					}
				} catch (JavaModelException e) {
				}
				if (root == null) return;
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						currentDelta().sourceAttached(root);
						break;
					case IResourceDelta.CHANGED:
						currentDelta().sourceDetached(root);
						currentDelta().sourceAttached(root);
						break;
					case IResourceDelta.REMOVED:
						currentDelta().sourceDetached(root);
						break;
				}
			} 
		}
	}

	/**
	 * Closes the given element, which removes it from the cache of open elements.
	 */
	protected static void close(Openable element) {

		try {
			element.close();
		} catch (JavaModelException e) {
			// do nothing
		}
	}
	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 * Delta argument could be null if processing an external JAR change
	 */
	protected void contentChanged(Openable element, IResourceDelta delta) {

		close(element);
		int flags = IJavaElementDelta.F_CONTENT;
		if (element instanceof JarPackageFragmentRoot){
			flags |= IJavaElementDelta.F_ARCHIVE_CONTENT_CHANGED;
		}
		currentDelta().changed(element, flags);
	}
	
	/**
	 * Creates the openables corresponding to this resource.
	 * Returns null if none was found.
	 */
	protected Openable createElement(IResource resource, int elementType, RootInfo rootInfo) {
		if (resource == null) return null;
		
		IPath path = resource.getFullPath();
		IJavaElement element = null;
		switch (elementType) {
			
			case IJavaElement.JAVA_PROJECT:
			
				// note that non-java resources rooted at the project level will also enter this code with
				// an elementType JAVA_PROJECT (see #elementType(...)).
				if (resource instanceof IProject){

					this.popUntilPrefixOf(path);
					
					if (this.currentElement != null 
						&& this.currentElement.getElementType() == IJavaElement.JAVA_PROJECT
						&& ((IJavaProject)this.currentElement).getProject().equals(resource)) {
						return this.currentElement;
					}
					if  (rootInfo != null && rootInfo.project.getProject().equals(resource)){
						element = (Openable)rootInfo.project;
						break;
					}
					IProject proj = (IProject)resource;
					if (JavaProject.hasJavaNature(proj)) {
						element = JavaCore.create(proj);
					} else {
						// java project may have been been closed or removed (look for
						// element amongst old java project s list).
						element =  (Openable) manager.getJavaModel().findJavaProject(proj);
					}
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
				element = rootInfo == null ? JavaCore.create(resource) : rootInfo.project.getPackageFragmentRoot(resource);
				break;
			case IJavaElement.PACKAGE_FRAGMENT:
				// find the element that encloses the resource
				this.popUntilPrefixOf(path);
				
				if (this.currentElement == null) {
					element = rootInfo == null ? JavaCore.create(resource) : JavaModelManager.create(resource, rootInfo.project);
				} else {
					// find the root
					IPackageFragmentRoot root = this.currentElement.getPackageFragmentRoot();
					if (root == null) {
						element =  rootInfo == null ? JavaCore.create(resource) : JavaModelManager.create(resource, rootInfo.project);
					} else if (((JavaProject)root.getJavaProject()).contains(resource)) {
						// create package handle
						IPath pkgPath = path.removeFirstSegments(root.getPath().segmentCount());
						String pkg = Util.packageName(pkgPath);
						if (pkg == null) return null;
						element = root.getPackageFragment(pkg);
					}
				}
				break;
			case IJavaElement.COMPILATION_UNIT:
			case IJavaElement.CLASS_FILE:
				// find the element that encloses the resource
				this.popUntilPrefixOf(path);
				
				if (this.currentElement == null) {
					element =  rootInfo == null ? JavaCore.create(resource) : JavaModelManager.create(resource, rootInfo.project);
				} else {
					// find the package
					IPackageFragment pkgFragment = null;
					switch (this.currentElement.getElementType()) {
						case IJavaElement.PACKAGE_FRAGMENT_ROOT:
							IPackageFragmentRoot root = (IPackageFragmentRoot)this.currentElement;
							IPath rootPath = root.getPath();
							IPath pkgPath = path.removeLastSegments(1);
							String pkgName = Util.packageName(pkgPath.removeFirstSegments(rootPath.segmentCount()));
							if (pkgName != null) {
								pkgFragment = root.getPackageFragment(pkgName);
							}
							break;
						case IJavaElement.PACKAGE_FRAGMENT:
							Openable pkg = (Openable)this.currentElement;
							if (pkg.getPath().equals(path.removeLastSegments(1))) {
								pkgFragment = (IPackageFragment)pkg;
							} // else case of package x which is a prefix of x.y
							break;
						case IJavaElement.COMPILATION_UNIT:
						case IJavaElement.CLASS_FILE:
							pkgFragment = (IPackageFragment)this.currentElement.getParent();
							break;
					}
					if (pkgFragment == null) {
						element =  rootInfo == null ? JavaCore.create(resource) : JavaModelManager.create(resource, rootInfo.project);
					} else {
						if (elementType == IJavaElement.COMPILATION_UNIT) {
							// create compilation unit handle 
							// fileName validation has been done in elementType(IResourceDelta, int, boolean)
							String fileName = path.lastSegment();
							element = pkgFragment.getCompilationUnit(fileName);
						} else {
							// create class file handle
							// fileName validation has been done in elementType(IResourceDelta, int, boolean)
							String fileName = path.lastSegment();
							element = pkgFragment.getClassFile(fileName);
						}
					}
				}
				break;
		}
		if (element == null) {
			return null;
		} else {
			this.currentElement = (Openable)element;
			return this.currentElement;
		}
	}
	/**
	 * Note that the project is about to be deleted.
	 */
	public void deleting(IProject project) {
		
		try {
			// discard indexing jobs that belong to this project so that the project can be 
			// deleted without interferences from the index manager
			this.indexManager.discardJobs(project.getName());

			JavaProject javaProject = (JavaProject)JavaCore.create(project);
			
			// remember roots of this project
			if (this.removedRoots == null) {
				this.removedRoots = new HashMap();
			}
			if (javaProject.isOpen()) {
				this.removedRoots.put(javaProject, javaProject.getPackageFragmentRoots());
			} else {
				// compute roots without opening project
				this.removedRoots.put(
					javaProject, 
					javaProject.computePackageFragmentRoots(
						javaProject.getResolvedClasspath(true), 
						false));
			}
			
			javaProject.close();

			// workaround for bug 15168 circular errors not reported  
			if (this.manager.javaProjectsCache == null) {
				this.manager.javaProjectsCache = this.manager.getJavaModel().getJavaProjects();
			}
			this.removeFromParentInfo(javaProject);

		} catch (JavaModelException e) {
		}
		
		this.addDependentProjects(project.getFullPath(), this.projectsToUpdate);
	}


	/**
	 * Processing for an element that has been added:<ul>
	 * <li>If the element is a project, do nothing, and do not process
	 * children, as when a project is created it does not yet have any
	 * natures - specifically a java nature.
	 * <li>If the elemet is not a project, process it as added (see
	 * <code>basicElementAdded</code>.
	 * </ul>
	 * Delta argument could be null if processing an external JAR change
	 */
	protected void elementAdded(Openable element, IResourceDelta delta, RootInfo rootInfo) {
		int elementType = element.getElementType();
		
		if (elementType == IJavaElement.JAVA_PROJECT) {
			// project add is handled by JavaProject.configure() because
			// when a project is created, it does not yet have a java nature
			if (delta != null && JavaProject.hasJavaNature((IProject)delta.getResource())) {
				addToParentInfo(element);
				if ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
					Openable movedFromElement = (Openable)element.getJavaModel().getJavaProject(delta.getMovedFromPath().lastSegment());
					currentDelta().movedTo(element, movedFromElement);
				} else {
					currentDelta().added(element);
				}
				this.projectsToUpdate.add(element);
				this.updateRoots(element.getPath(), delta);
				this.projectsForDependentNamelookupRefresh.add((JavaProject) element);
			}
		} else {			
			addToParentInfo(element);
			
			// Force the element to be closed as it might have been opened 
			// before the resource modification came in and it might have a new child
			// For example, in an IWorkspaceRunnable:
			// 1. create a package fragment p using a java model operation
			// 2. open package p
			// 3. add file X.java in folder p
			// When the resource delta comes in, only the addition of p is notified, 
			// but the package p is already opened, thus its children are not recomputed
			// and it appears empty.
			close(element);
			
			if (delta != null && (delta.getFlags() & IResourceDelta.MOVED_FROM) != 0) {
				IPath movedFromPath = delta.getMovedFromPath();
				IResource res = delta.getResource();
				IResource movedFromRes;
				if (res instanceof IFile) {
					movedFromRes = res.getWorkspace().getRoot().getFile(movedFromPath);
				} else {
					movedFromRes = res.getWorkspace().getRoot().getFolder(movedFromPath);
				}
				
				// find the element type of the moved from element
				RootInfo movedFromInfo = this.enclosingRootInfo(movedFromPath, IResourceDelta.REMOVED);
				int movedFromType = 
					this.elementType(
						movedFromRes, 
						IResourceDelta.REMOVED,
						element.getParent().getElementType(), 
						movedFromInfo);
				
				// reset current element as it might be inside a nested root (popUntilPrefixOf() may use the outer root)
				this.currentElement = null;
			
				// create the moved from element
				Openable movedFromElement = 
					elementType != IJavaElement.JAVA_PROJECT && movedFromType == IJavaElement.JAVA_PROJECT ? 
						null : // outside classpath
						this.createElement(movedFromRes, movedFromType, movedFromInfo);
				if (movedFromElement == null) {
					// moved from outside classpath
					currentDelta().added(element);
				} else {
					currentDelta().movedTo(element, movedFromElement);
				}
			} else {
				currentDelta().added(element);
			}
			
			switch (elementType) {
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					// when a root is added, and is on the classpath, the project must be updated
					JavaProject project = (JavaProject) element.getJavaProject();
					this.projectsToUpdate.add(project);
					this.projectsForDependentNamelookupRefresh.add(project);
					
					break;
				case IJavaElement.PACKAGE_FRAGMENT :
					// get rid of namelookup since it holds onto obsolete cached info 
					project = (JavaProject) element.getJavaProject();
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
						this.projectsForDependentNamelookupRefresh.add(project);						
					} catch (JavaModelException e) {
					}
					// add subpackages
					if (delta != null){
						PackageFragmentRoot root = element.getPackageFragmentRoot();
						String name = element.getElementName();
						IResourceDelta[] children = delta.getAffectedChildren();
						for (int i = 0, length = children.length; i < length; i++) {
							IResourceDelta child = children[i];
							IResource resource = child.getResource();
							if (resource instanceof IFolder) {
								String folderName = resource.getName();
								if (Util.isValidFolderNameForPackage(folderName)) {
									String subpkgName = 
										name.length() == 0 ? 
											folderName : 
											name + "." + folderName; //$NON-NLS-1$
									Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
									this.updateIndex(subpkg, child);
									this.elementAdded(subpkg, child, rootInfo);
								}
							}
						}
					}
					break;
			}
		}
	}

	/**
	 * Generic processing for a removed element:<ul>
	 * <li>Close the element, removing its structure from the cache
	 * <li>Remove the element from its parent's cache of children
	 * <li>Add a REMOVED entry in the delta
	 * </ul>
	 * Delta argument could be null if processing an external JAR change
	 */
	protected void elementRemoved(Openable element, IResourceDelta delta, RootInfo rootInfo) {
		
		if (element.isOpen()) {
			close(element);
		}
		removeFromParentInfo(element);
		int elementType = element.getElementType();
		if (delta != null && (delta.getFlags() & IResourceDelta.MOVED_TO) != 0) {
			IPath movedToPath = delta.getMovedToPath();
			IResource res = delta.getResource();
			IResource movedToRes;
			switch (res.getType()) {
				case IResource.PROJECT:
					movedToRes = res.getWorkspace().getRoot().getProject(movedToPath.lastSegment());
					break;
				case IResource.FOLDER:
					movedToRes = res.getWorkspace().getRoot().getFolder(movedToPath);
					break;
				case IResource.FILE:
					movedToRes = res.getWorkspace().getRoot().getFile(movedToPath);
					break;
				default:
					return;
			}

			// find the element type of the moved from element
			RootInfo movedToInfo = this.enclosingRootInfo(movedToPath, IResourceDelta.ADDED);
			int movedToType = 
				this.elementType(
					movedToRes, 
					IResourceDelta.ADDED,
					element.getParent().getElementType(), 
					movedToInfo);

			// reset current element as it might be inside a nested root (popUntilPrefixOf() may use the outer root)
			this.currentElement = null;
			
			// create the moved To element
			Openable movedToElement = 
				elementType != IJavaElement.JAVA_PROJECT && movedToType == IJavaElement.JAVA_PROJECT ? 
					null : // outside classpath
					this.createElement(movedToRes, movedToType, movedToInfo);
			if (movedToElement == null) {
				// moved outside classpath
				currentDelta().removed(element);
			} else {
				currentDelta().movedFrom(element, movedToElement);
			}
		} else {
			currentDelta().removed(element);
		}

		switch (elementType) {
			case IJavaElement.JAVA_MODEL :
				this.indexManager.reset();
				break;
			case IJavaElement.JAVA_PROJECT :
				this.manager.removePerProjectInfo(
					(JavaProject) element);
				this.updateRoots(element.getPath(), delta);
				this.projectsForDependentNamelookupRefresh.add((JavaProject) element);
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				JavaProject project = (JavaProject) element.getJavaProject();
				this.projectsToUpdate.add(project);
				this.projectsForDependentNamelookupRefresh.add(project);				
				break;
			case IJavaElement.PACKAGE_FRAGMENT :
				//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
				project = (JavaProject) element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null); 
					this.projectsForDependentNamelookupRefresh.add(project);
				} catch (JavaModelException e) { 
				}
				// remove subpackages
				if (delta != null){
					PackageFragmentRoot root = element.getPackageFragmentRoot();
					String name = element.getElementName();
					IResourceDelta[] children = delta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						IResourceDelta child = children[i];
						IResource resource = child.getResource();
						if (resource instanceof IFolder) {
							String folderName = resource.getName();
							if (Util.isValidFolderNameForPackage(folderName)) {
								String subpkgName = 
									name.length() == 0 ? 
										folderName : 
										name + "." + folderName; //$NON-NLS-1$
								Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
								this.updateIndex(subpkg, child);
								this.elementRemoved(subpkg, child, rootInfo);
							}
						}
					}
				}
				break;
		}
	}

	/*
	 * Returns the type of the java element the given delta matches to.
	 * Returns NON_JAVA_RESOURCE if unknown (e.g. a non-java resource or excluded .java file)
	 */
	private int elementType(IResource res, int kind, int parentType, RootInfo rootInfo) {
		switch (parentType) {
			case IJavaElement.JAVA_MODEL:
				// case of a movedTo or movedFrom project (other cases are handled in processResourceDelta(...)
				return IJavaElement.JAVA_PROJECT;
			case NON_JAVA_RESOURCE:
			case IJavaElement.JAVA_PROJECT:
				if (rootInfo == null) {
					rootInfo = this.enclosingRootInfo(res.getFullPath(), kind);
				}
				if (rootInfo != null && rootInfo.isRootOfProject(res.getFullPath())) {
					return IJavaElement.PACKAGE_FRAGMENT_ROOT;
				} else {
					return NON_JAVA_RESOURCE; // not yet in a package fragment root or root of another project
				}
			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
			case IJavaElement.PACKAGE_FRAGMENT:
				if (rootInfo == null) {
					rootInfo = this.enclosingRootInfo(res.getFullPath(), kind);
				}
				if (rootInfo == null || Util.isExcluded(res, rootInfo.exclusionPatterns)) {
					return NON_JAVA_RESOURCE;
				}
				if (res instanceof IFolder) {
					if (Util.isValidFolderNameForPackage(res.getName())) {
						return IJavaElement.PACKAGE_FRAGMENT;
					} else {
						return NON_JAVA_RESOURCE;
					}
				} else {
					String fileName = res.getName();
					if (Util.isValidCompilationUnitName(fileName)) {
						return IJavaElement.COMPILATION_UNIT;
					} else if (Util.isValidClassFileName(fileName)) {
						return IJavaElement.CLASS_FILE;
					} else if (this.rootInfo(res.getFullPath(), kind) != null) {
						// case of proj=src=bin and resource is a jar file on the classpath
						return IJavaElement.PACKAGE_FRAGMENT_ROOT;
					} else {
						return NON_JAVA_RESOURCE;
					}
				}
			default:
				return NON_JAVA_RESOURCE;
		}
	}

	/**
	 * Answer a combination of the lastModified stamp and the size.
	 * Used for detecting external JAR changes
	 */
	public static long getTimeStamp(File file) {
		return file.lastModified() + file.length();
	}

	public void initializeRoots() {
		// remember roots infos as old roots infos
		this.oldRoots = this.roots == null ? new HashMap() : this.roots;
		this.oldOtherRoots = this.otherRoots == null ? new HashMap() : this.otherRoots;
		
		// recompute root infos only if necessary
		if (!rootsAreStale) return;

		this.roots = new HashMap();
		this.otherRoots = new HashMap();
		this.sourceAttachments = new HashMap();
		
		IJavaModel model = this.manager.getJavaModel();
		IJavaProject[] projects;
		try {
			projects = model.getJavaProjects();
		} catch (JavaModelException e) {
			// nothing can be done
			return;
		}
		for (int i = 0, length = projects.length; i < length; i++) {
			IJavaProject project = projects[i];
			IClasspathEntry[] classpath;
			try {
				classpath = project.getResolvedClasspath(true);
			} catch (JavaModelException e) {
				// continue with next project
				continue;
			}
			for (int j= 0, classpathLength = classpath.length; j < classpathLength; j++) {
				IClasspathEntry entry = classpath[j];
				if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
				
				// root path
				IPath path = entry.getPath();
				if (this.roots.get(path) == null) {
					this.roots.put(path, new RootInfo(project, path, ((ClasspathEntry)entry).fullExclusionPatternChars()));
				} else {
					ArrayList rootList = (ArrayList)this.otherRoots.get(path);
					if (rootList == null) {
						rootList = new ArrayList();
						this.otherRoots.put(path, rootList);
					}
					rootList.add(new RootInfo(project, path, ((ClasspathEntry)entry).fullExclusionPatternChars()));
				}
				
				// source attachment path
				if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) continue;
				QualifiedName qName = new QualifiedName(JavaCore.PLUGIN_ID, "sourceattachment: " + path.toOSString()); //$NON-NLS-1$;
				String propertyString = null;
				try {
					propertyString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
				} catch (CoreException e) {
					continue;
				}
				IPath sourceAttachmentPath;
				if (propertyString != null) {
					int index= propertyString.lastIndexOf(JarPackageFragmentRoot.ATTACHMENT_PROPERTY_DELIMITER);
					sourceAttachmentPath = (index < 0) ?  new Path(propertyString) : new Path(propertyString.substring(0, index));
				} else {
					sourceAttachmentPath = entry.getSourceAttachmentPath();
				}
				if (sourceAttachmentPath != null) {
					this.sourceAttachments.put(sourceAttachmentPath, path);
				}
			}
		}
		this.rootsAreStale = false;
	}

	/*
	 * Returns whether a given delta contains some information relevant to the JavaModel,
	 * in particular it will not consider SYNC or MARKER only deltas.
	 */
	public boolean isAffectedBy(IResourceDelta rootDelta){
		//if (rootDelta == null) System.out.println("NULL DELTA");
		//long start = System.currentTimeMillis();
		if (rootDelta != null) {
			// use local exception to quickly escape from delta traversal
			class FoundRelevantDeltaException extends RuntimeException {}
			try {
				rootDelta.accept(new IResourceDeltaVisitor() {
					public boolean visit(IResourceDelta delta) throws CoreException {
						switch (delta.getKind()){
							case IResourceDelta.ADDED :
							case IResourceDelta.REMOVED :
								throw new FoundRelevantDeltaException();
							case IResourceDelta.CHANGED :
								// if any flag is set but SYNC or MARKER, this delta should be considered
								if (delta.getAffectedChildren().length == 0 // only check leaf delta nodes
										&& (delta.getFlags() & ~(IResourceDelta.SYNC | IResourceDelta.MARKERS)) != 0) {
									throw new FoundRelevantDeltaException();
								}
						}
						return true;
					}
				});
			} catch(FoundRelevantDeltaException e) {
				//System.out.println("RELEVANT DELTA detected in: "+ (System.currentTimeMillis() - start));
				return true;
			} catch(CoreException e) { // ignore delta if not able to traverse
			}
		}
		//System.out.println("IGNORE SYNC DELTA took: "+ (System.currentTimeMillis() - start));
		return false;
	}
	
	/*
	 * Returns whether the given resource is in one of the given output folders and if
	 * it is filtered out from this output folder.
	 */
	private boolean isResFilteredFromOutput(OutputsInfo info, IResource res, int elementType) {
		if (info != null) {
			IPath resPath = res.getFullPath();
			for (int i = 0;  i < info.outputCount; i++) {
				if (info.paths[i].isPrefixOf(resPath)) {
					if (info.traverseModes[i] != IGNORE) {
						// case of bin=src
						if (info.traverseModes[i] == SOURCE && elementType == IJavaElement.CLASS_FILE) {
							return true;
						} else {
							// case of .class file under project and no source folder
							// proj=bin
							if (elementType == IJavaElement.JAVA_PROJECT 
									&& res instanceof IFile 
									&& Util.isValidClassFileName(res.getName())) {
								return true;
							}
						}
					} else {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void nonJavaResourcesChanged(Openable element, IResourceDelta delta)
		throws JavaModelException {

		// reset non-java resources if element was open
		if (element.isOpen()) {
			JavaElementInfo info = (JavaElementInfo)element.getElementInfo();
			switch (element.getElementType()) {
				case IJavaElement.JAVA_MODEL :
					((JavaModelInfo) info).nonJavaResources = null;
					currentDelta().addResourceDelta(delta);
					return;
				case IJavaElement.JAVA_PROJECT :
					((JavaProjectElementInfo) info).setNonJavaResources(null);
	
					// if a package fragment root is the project, clear it too
					JavaProject project = (JavaProject) element;
					PackageFragmentRoot projectRoot =
						(PackageFragmentRoot) project.getPackageFragmentRoot(project.getProject());
					if (projectRoot.isOpen()) {
						((PackageFragmentRootInfo) projectRoot.getElementInfo()).setNonJavaResources(
							null);
					}
					break;
				case IJavaElement.PACKAGE_FRAGMENT :
					 ((PackageFragmentInfo) info).setNonJavaResources(null);
					break;
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					 ((PackageFragmentRootInfo) info).setNonJavaResources(null);
			}
		}

		JavaElementDelta elementDelta = currentDelta().find(element);
		if (elementDelta == null) {
			currentDelta().changed(element, IJavaElementDelta.F_CONTENT);
			elementDelta = currentDelta().find(element);
		}
		elementDelta.addResourceDelta(delta);
	}
	private OutputsInfo outputsInfo(RootInfo rootInfo, IResource res) {
		try {
			IJavaProject proj =
				rootInfo == null ?
					(IJavaProject)this.createElement(res.getProject(), IJavaElement.JAVA_PROJECT, null) :
					rootInfo.project;
			if (proj != null) {
				IPath projectOutput = proj.getOutputLocation();
				int traverseMode = IGNORE;
				if (proj.getProject().getFullPath().equals(projectOutput)){ // case of proj==bin==src
					return new OutputsInfo(new IPath[] {projectOutput}, new int[] {SOURCE}, 1);
				} else {
					IClasspathEntry[] classpath = proj.getResolvedClasspath(true);
					IPath[] outputs = new IPath[classpath.length+1];
					int[] traverseModes = new int[classpath.length+1];
					int outputCount = 1;
					outputs[0] = projectOutput;
					traverseModes[0] = traverseMode;
					for (int i = 0, length = classpath.length; i < length; i++) {
						IClasspathEntry entry = classpath[i];
						IPath entryPath = entry.getPath();
						IPath output = entry.getOutputLocation();
						if (output != null) {
							outputs[outputCount] = output;
							// check case of src==bin
							if (entryPath.equals(output)) {
								traverseModes[outputCount++] = (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) ? SOURCE : BINARY;
							} else {
								traverseModes[outputCount++] = IGNORE;
							}
						}
						
						// check case of src==bin
						if (entryPath.equals(projectOutput)) {
							traverseModes[0] = (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) ? SOURCE : BINARY;
						}
					}
					return new OutputsInfo(outputs, traverseModes, outputCount);
				}
			}
		} catch (JavaModelException e) {
		}
		return null;
	}
	
	/**
	 * Check whether the updated file is affecting some of the properties of a given project (like
	 * its classpath persisted as a file).
	 * Also force classpath problems to be refresh if not running in autobuild mode.
	 * NOTE: It can induce resource changes, and cannot be called during POST_CHANGE notification.
	 *
	 */
	public void performPreBuildCheck(
		IResourceDelta delta,
		IJavaElement parent) {
	
		IResource resource = delta.getResource();
		IJavaElement element = null;
		boolean processChildren = false;
	
		switch (resource.getType()) {
	
			case IResource.ROOT :
				if (delta.getKind() == IResourceDelta.CHANGED) {
					element = JavaCore.create(resource);
					processChildren = true;
				}
				break;
			case IResource.PROJECT :
				int kind = delta.getKind();
				switch (kind) {
					case IResourceDelta.CHANGED:
						// do not visit non-java projects (see bug 16140 Non-java project gets .classpath)
						IProject project = (IProject)resource;
						if (JavaProject.hasJavaNature(project)) {
							element = JavaCore.create(resource);
							processChildren = true;
						} else if (JavaModelManager.getJavaModelManager().getJavaModel().findJavaProject(project) != null) {
							// project had the java nature
							this.rootsAreStale = true;

							// remove classpath cache so that initializeRoots() will not consider the project has a classpath
							this.manager.removePerProjectInfo((JavaProject)JavaCore.create(project));
						}
						break;
					case IResourceDelta.ADDED:
						this.rootsAreStale = true;
						break;
					case IResourceDelta.REMOVED:
						// remove classpath cache so that initializeRoots() will not consider the project has a classpath
						this.manager.removePerProjectInfo((JavaProject)JavaCore.create(resource));
						
						this.rootsAreStale = true;
						break;
				}
				break;
			case IResource.FILE :
				if (parent.getElementType() == IJavaElement.JAVA_PROJECT) {
					IFile file = (IFile) resource;
					JavaProject project = (JavaProject) parent;
	
					/* check classpath file change */
					if (file.getName().equals(JavaProject.CLASSPATH_FILENAME)) {
						reconcileClasspathFileUpdate(delta, file, project);
						this.rootsAreStale = true;
						break;
					}
//					/* check custom preference file change */
//					if (file.getName().equals(JavaProject.PREF_FILENAME)) {
//						reconcilePreferenceFileUpdate(delta, file, project);
//						break;
//					}
				}
				break;
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				performPreBuildCheck(children[i], element);
			}
		}
	}
	
	private void popUntilPrefixOf(IPath path) {
		while (this.currentElement != null) {
			IPath currentElementPath = null;
			if (this.currentElement instanceof IPackageFragmentRoot) {
				currentElementPath = ((IPackageFragmentRoot)this.currentElement).getPath();
			} else {
				IResource currentElementResource = this.currentElement.getResource();
				if (currentElementResource != null) {
					currentElementPath = currentElementResource.getFullPath();
				}
			}
			if (currentElementPath != null) {
				if (this.currentElement instanceof IPackageFragment 
					&& this.currentElement.getElementName().length() == 0
					&& currentElementPath.segmentCount() != path.segmentCount()-1) {
						// default package and path is not a direct child
						this.currentElement = (Openable)this.currentElement.getParent();
				}
				if (currentElementPath.isPrefixOf(path)) {
					return;
				}
			}
			this.currentElement = (Openable)this.currentElement.getParent();
		}
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>IJavaElementDelta</code>, rooted in the
	 * relevant <code>JavaModel</code>s.
	 */
	public IJavaElementDelta processResourceDelta(IResourceDelta changes) {

		try {
			IJavaModel model = this.manager.getJavaModel();
			if (!model.isOpen()) {
				// force opening of java model so that java element delta are reported
				try {
					model.open(null);
				} catch (JavaModelException e) {
					if (VERBOSE) {
						e.printStackTrace();
					}
					return null;
				}
			}
			this.initializeRoots();
			this.currentElement = null;
			
			// get the workspace delta, and start processing there.
			IResourceDelta[] deltas = changes.getAffectedChildren();
			for (int i = 0; i < deltas.length; i++) {
				IResourceDelta delta = deltas[i];
				IResource res = delta.getResource();
				
				// find out the element type
				RootInfo rootInfo = null;
				int elementType;
				IProject proj = (IProject)res;
				boolean wasJavaProject = this.manager.getJavaModel().findJavaProject(proj) != null;
				boolean isJavaProject = JavaProject.hasJavaNature(proj);
				if (!wasJavaProject && !isJavaProject) {
					elementType = NON_JAVA_RESOURCE;
				} else {
					rootInfo = this.enclosingRootInfo(res.getFullPath(), delta.getKind());
					if (rootInfo != null && rootInfo.isRootOfProject(res.getFullPath())) {
						elementType = IJavaElement.PACKAGE_FRAGMENT_ROOT;
					} else {
						elementType = IJavaElement.JAVA_PROJECT; 
					}
				}
				
				// traverse delta
				if (!this.traverseDelta(delta, elementType, rootInfo, null) 
						|| (wasJavaProject != isJavaProject && (delta.getKind()) == IResourceDelta.CHANGED)) { // project has changed nature (description or open/closed)
					try {
						// add child as non java resource
						nonJavaResourcesChanged((JavaModel)model, delta);
					} catch (JavaModelException e) {
					}
				}

			}
			
			// update package fragment roots of projects that were affected
			Iterator iterator = this.projectsToUpdate.iterator();
			while (iterator.hasNext()) {
				JavaProject project = (JavaProject)iterator.next();
				project.updatePackageFragmentRoots();
			}
	
			updateDependentNamelookups();

			return this.currentDelta;
		} finally {
			this.currentDelta = null;
			this.projectsToUpdate.clear();
			this.projectsForDependentNamelookupRefresh.clear();
		}
	}

	/**
	 * Update the JavaModel according to a .classpath file change. The file can have changed as a result of a previous
	 * call to JavaProject#setRawClasspath or as a result of some user update (through repository)
	 */
	void reconcileClasspathFileUpdate(IResourceDelta delta, IFile file, JavaProject project) {
			
		switch (delta.getKind()) {
			case IResourceDelta.REMOVED : // recreate one based on in-memory classpath
				try {
					JavaModelManager.PerProjectInfo info = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(project.getProject());
					if (info.classpath != null) { // if there is an in-memory classpath
						project.saveClasspath(info.classpath, info.outputLocation);
					}
				} catch (JavaModelException e) {
					if (project.getProject().isAccessible()) {
						Util.log(e, "Could not save classpath for "+ project.getPath()); //$NON-NLS-1$
					}
				}
				break;
			case IResourceDelta.CHANGED :
				if ((delta.getFlags() & IResourceDelta.CONTENT) == 0  // only consider content change
						&& (delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) // and also move and overide scenario (see http://dev.eclipse.org/bugs/show_bug.cgi?id=21420)
					break;
			case IResourceDelta.ADDED :
				// check if any actual difference
				project.flushClasspathProblemMarkers(false, true);
				boolean wasSuccessful = false; // flag recording if .classpath file change got reflected
				try {
					// force to (re)read the property file
					IClasspathEntry[] fileEntries = project.readClasspathFile(true/*create markers*/, false/*don't log problems*/);
					if (fileEntries == null)
						break; // could not read, ignore 
					JavaModelManager.PerProjectInfo info = JavaModelManager.getJavaModelManager().getPerProjectInfoCheckExistence(project.getProject());
					if (info.classpath != null) { // if there is an in-memory classpath
						if (project.isClasspathEqualsTo(info.classpath, info.outputLocation, fileEntries)) {
							wasSuccessful = true;
							break;
						}
					}
		
					// will force an update of the classpath/output location based on the file information
					// extract out the output location
					IPath outputLocation = null;
					if (fileEntries != null && fileEntries.length > 0) {
						IClasspathEntry entry = fileEntries[fileEntries.length - 1];
						if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
							outputLocation = entry.getPath();
							IClasspathEntry[] copy = new IClasspathEntry[fileEntries.length - 1];
							System.arraycopy(fileEntries, 0, copy, 0, copy.length);
							fileEntries = copy;
						}
					}
					// restore output location				
					if (outputLocation == null) {
						outputLocation = SetClasspathOperation.ReuseOutputLocation;
						// clean mode will also default to reusing current one
					}
					project.setRawClasspath(
						fileEntries, 
						outputLocation, 
						null, // monitor
						true, // canChangeResource
						project.getResolvedClasspath(true), // ignoreUnresolvedVariable
						true, // needValidation
						false); // no need to save
					
					// if reach that far, the classpath file change got absorbed
					wasSuccessful = true;
				} catch (RuntimeException e) {
					// setRawClasspath might fire a delta, and a listener may throw an exception
					if (project.getProject().isAccessible()) {
						Util.log(e, "Could not set classpath for "+ project.getPath()); //$NON-NLS-1$
					}
					break;
				} catch (JavaModelException e) { // CP failed validation
					if (project.getProject().isAccessible()) {
						if (e.getJavaModelStatus().getException() instanceof CoreException) {
							// happens if the .classpath could not be written to disk
							project.createClasspathProblemMarker(new JavaModelStatus(
									IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
									Util.bind("classpath.couldNotWriteClasspathFile", project.getElementName(), e.getMessage()))); //$NON-NLS-1$
						} else {
							project.createClasspathProblemMarker(new JavaModelStatus(
									IJavaModelStatusConstants.INVALID_CLASSPATH_FILE_FORMAT,
									Util.bind("classpath.invalidClasspathInClasspathFile", project.getElementName(), e.getMessage()))); //$NON-NLS-1$
						}			
					}
					break;
				} finally {
					if (!wasSuccessful) { 
						try {
							project.setRawClasspath0(JavaProject.INVALID_CLASSPATH);
							project.updatePackageFragmentRoots();
						} catch (JavaModelException e) {
						}
					}
				}
		}
	}

	/**
	 * Update the JavaModel according to a .jprefs file change. The file can have changed as a result of a previous
	 * call to JavaProject#setOptions or as a result of some user update (through repository)
	 * Unused until preference file get shared (.jpref)
	 */
	void reconcilePreferenceFileUpdate(IResourceDelta delta, IFile file, JavaProject project) {
			
		switch (delta.getKind()) {
			case IResourceDelta.REMOVED : // flush project custom settings
				project.setOptions(null);
				return;
			case IResourceDelta.CHANGED :
				if ((delta.getFlags() & IResourceDelta.CONTENT) == 0  // only consider content change
						&& (delta.getFlags() & IResourceDelta.MOVED_FROM) == 0) // and also move and overide scenario
					break;
				identityCheck : { // check if any actual difference
					// force to (re)read the property file
					Preferences filePreferences = project.loadPreferences();
					if (filePreferences == null){ 
						project.setOptions(null); // should have got removed delta.
						return;
					}
					Preferences projectPreferences = project.getPreferences();
					if (projectPreferences == null) return; // not a Java project
						
					// compare preferences set to their default
					String[] defaultProjectPropertyNames = projectPreferences.defaultPropertyNames();
					String[] defaultFilePropertyNames = filePreferences.defaultPropertyNames();
					if (defaultProjectPropertyNames.length == defaultFilePropertyNames.length) {
						for (int i = 0; i < defaultProjectPropertyNames.length; i++){
							String propertyName = defaultProjectPropertyNames[i];
							if (!projectPreferences.getString(propertyName).trim().equals(filePreferences.getString(propertyName).trim())){
								break identityCheck;
							}
						}		
					} else break identityCheck;

					// compare custom preferences not set to their default
					String[] projectPropertyNames = projectPreferences.propertyNames();
					String[] filePropertyNames = filePreferences.propertyNames();
					if (projectPropertyNames.length == filePropertyNames.length) {
						for (int i = 0; i < projectPropertyNames.length; i++){
						String propertyName = projectPropertyNames[i];
							if (!projectPreferences.getString(propertyName).trim().equals(filePreferences.getString(propertyName).trim())){
								break identityCheck;
							}
						}		
					} else break identityCheck;
					
					// identical - do nothing
					return;
				}
			case IResourceDelta.ADDED :
				// not identical, create delta and reset cached preferences
				project.setPreferences(null);
				// create delta
				//fCurrentDelta.changed(project, IJavaElementDelta.F_OPTIONS_CHANGED);				
		}
	}

	/**
	 * Removes the given element from its parents cache of children. If the
	 * element does not have a parent, or the parent is not currently open,
	 * this has no effect. 
	 */
	protected void removeFromParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			try {
				JavaElementInfo info = (JavaElementInfo)parent.getElementInfo();
				info.removeChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}
	/**
	 * Notification that some resource changes have happened
	 * on the platform, and that the Java Model should update any required
	 * internal structures such that its elements remain consistent.
	 * Translates <code>IResourceDeltas</code> into <code>IJavaElementDeltas</code>.
	 *
	 * @see IResourceDelta
	 * @see IResource 
	 */
	public void resourceChanged(IResourceChangeEvent event) {
	
		if (event.getSource() instanceof IWorkspace) {
			IResource resource = event.getResource();
			IResourceDelta delta = event.getDelta();
			
			switch(event.getType()){
				case IResourceChangeEvent.PRE_DELETE :
					try {
						if(resource.getType() == IResource.PROJECT 
							&& ((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
								
							this.deleting((IProject)resource);
						}
					} catch(CoreException e){
					}
					return;
					
				case IResourceChangeEvent.PRE_AUTO_BUILD :
					if(isAffectedBy(delta)) { // avoid populating for SYNC or MARKER deltas
						this.checkProjectsBeingAddedOrRemoved(delta);
						
						// update the classpath related markers
						this.updateClasspathMarkers();
	
						// the following will close project if affected by the property file change
						try {
							// don't fire classpath change deltas right away, but batch them
							this.manager.stopDeltas();
							this.performPreBuildCheck(delta, null); 
						} finally {
							this.manager.startDeltas();
						}
					}
					// only fire already computed deltas (resource ones will be processed in post change only)
					this.manager.fire(null, ElementChangedEvent.PRE_AUTO_BUILD);
					break;

				case IResourceChangeEvent.POST_AUTO_BUILD :
					JavaBuilder.finishedBuilding(event);
					break;
					
				case IResourceChangeEvent.POST_CHANGE :
					if (isAffectedBy(delta)) {
						try {
							if (this.refreshedElements != null) {
								try {
									createExternalArchiveDelta(null);
								} catch (JavaModelException e) {
									e.printStackTrace();
								}
							}
							IJavaElementDelta translatedDelta = this.processResourceDelta(delta);
							if (translatedDelta != null) { 
								this.manager.registerJavaModelDelta(translatedDelta);
							}
							this.manager.fire(null, ElementChangedEvent.POST_CHANGE);
						} finally {
							// workaround for bug 15168 circular errors not reported 
							this.manager.javaProjectsCache = null;
							this.removedRoots = null;
						}
					}
			}
		}
	}
	/*
	 * Finds the root info this path is included in.
	 * Returns null if not found.
	 */
	RootInfo enclosingRootInfo(IPath path, int kind) {
		while (path != null && path.segmentCount() > 0) {
			RootInfo rootInfo =  this.rootInfo(path, kind);
			if (rootInfo != null) return rootInfo;
			path = path.removeLastSegments(1);
		}
		return null;
	}
	/*
	 * Returns the root info for the given path. Look in the old roots table if kind is REMOVED.
	 */
	RootInfo rootInfo(IPath path, int kind) {
		if (kind == IResourceDelta.REMOVED) {
			return (RootInfo)this.oldRoots.get(path);
		} else {
			return (RootInfo)this.roots.get(path);
		}
	}
	/*
	 * Returns the other root infos for the given path. Look in the old other roots table if kind is REMOVED.
	 */
	ArrayList otherRootsInfo(IPath path, int kind) {
		if (kind == IResourceDelta.REMOVED) {
			return (ArrayList)this.oldOtherRoots.get(path);
		} else {
			return (ArrayList)this.otherRoots.get(path);
		}
	}	

	/**
	 * Converts an <code>IResourceDelta</code> and its children into
	 * the corresponding <code>IJavaElementDelta</code>s.
	 * Return whether the delta corresponds to a java element.
	 * If it is not a java element, it will be added as a non-java
	 * resource by the sender of this method.
	 */
	protected boolean traverseDelta(
		IResourceDelta delta, 
		int elementType, 
		RootInfo rootInfo,
		OutputsInfo outputsInfo) {
			
		IResource res = delta.getResource();
	
		// set stack of elements
		if (this.currentElement == null && rootInfo != null) {
			this.currentElement = (Openable)rootInfo.project;
		}
		
		// process current delta
		boolean processChildren = true;
		if (res instanceof IProject) {
			processChildren = 
				this.updateCurrentDeltaAndIndex(
					delta, 
					elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT ? 
						IJavaElement.JAVA_PROJECT : // case of prj=src
						elementType, 
					rootInfo);
		} else if (rootInfo != null) {
			processChildren = this.updateCurrentDeltaAndIndex(delta, elementType, rootInfo);
		} else {
			// not yet inside a package fragment root
			processChildren = true;
		}
		
		// get the project's output locations and traverse mode
		if (outputsInfo == null) outputsInfo = this.outputsInfo(rootInfo, res);
	
		// process children if needed
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			boolean oneChildOnClasspath = false;
			int length = children.length;
			IResourceDelta[] orphanChildren = null;
			Openable parent = null;
			boolean isValidParent = true;
			for (int i = 0; i < length; i++) {
				IResourceDelta child = children[i];
				IResource childRes = child.getResource();
	
				// check source attachment change
				this.checkSourceAttachmentChange(child, childRes);
				
				// find out whether the child is a package fragment root of the current project
				IPath childPath = childRes.getFullPath();
				int childKind = child.getKind();
				RootInfo childRootInfo = this.rootInfo(childPath, childKind);
				if (childRootInfo != null && !childRootInfo.isRootOfProject(childPath)) {
					// package fragment root of another project (dealt with later)
					childRootInfo = null;
				}
				
				// compute child type
				int childType = 
					this.elementType(
						childRes, 
						childKind,
						elementType, 
						rootInfo == null ? childRootInfo : rootInfo
					);
						
				// is childRes in the output folder and is it filtered out ?
				boolean isResFilteredFromOutput = this.isResFilteredFromOutput(outputsInfo, childRes, childType);

				boolean isNestedRoot = rootInfo != null && childRootInfo != null;
				if (!isResFilteredFromOutput 
						&& !isNestedRoot) { // do not treat as non-java rsc if nested root
					if (!this.traverseDelta(child, childType, rootInfo == null ? childRootInfo : rootInfo, outputsInfo)) { // traverse delta for child in the same project
						// it is a non-java resource
						try {
							if (rootInfo != null) { // if inside a package fragment root
								if (!isValidParent) continue; 
								if (parent == null) {
									// find the parent of the non-java resource to attach to
									if (this.currentElement == null 
											|| !this.currentElement.getJavaProject().equals(rootInfo.project)) {
										// force the currentProject to be used
										this.currentElement = (Openable)rootInfo.project;
									}
									if (elementType == IJavaElement.JAVA_PROJECT
										|| (elementType == IJavaElement.PACKAGE_FRAGMENT_ROOT 
											&& res instanceof IProject)) { 
										// NB: attach non-java resource to project (not to its package fragment root)
										parent = (Openable)rootInfo.project;
									} else {
										parent = this.createElement(res, elementType, rootInfo);
									}
									if (parent == null) {
										isValidParent = false;
										continue;
									}
								}
								// add child as non java resource
								nonJavaResourcesChanged(parent, child);
							} else {
								// the non-java resource (or its parent folder) will be attached to the java project
								if (orphanChildren == null) orphanChildren = new IResourceDelta[length];
								orphanChildren[i] = child;
							}
						} catch (JavaModelException e) {
						}
					} else {
						oneChildOnClasspath = true;
					}
				} else {
					oneChildOnClasspath = true; // to avoid reporting child delta as non-java resource delta
				}
								
				// if child is a nested root 
				// or if it is not a package fragment root of the current project
				// but it is a package fragment root of another project, traverse delta too
				if (isNestedRoot 
						|| (childRootInfo == null && (childRootInfo = this.rootInfo(childPath, childKind)) != null)) {
					this.traverseDelta(child, IJavaElement.PACKAGE_FRAGMENT_ROOT, childRootInfo, null); // binary output of childRootInfo.project cannot be this root
					// NB: No need to check the return value as the child can only be on the classpath
				}
	
				// if the child is a package fragment root of one or several other projects
				ArrayList rootList;
				if ((rootList = this.otherRootsInfo(childPath, childKind)) != null) {
					Iterator iterator = rootList.iterator();
					while (iterator.hasNext()) {
						childRootInfo = (RootInfo) iterator.next();
						this.traverseDelta(child, IJavaElement.PACKAGE_FRAGMENT_ROOT, childRootInfo, null); // binary output of childRootInfo.project cannot be this root
					}
				}
			}
			if (orphanChildren != null
					&& (oneChildOnClasspath // orphan children are siblings of a package fragment root
						|| res instanceof IProject)) { // non-java resource directly under a project
						
				// attach orphan children
				IProject rscProject = res.getProject();
				JavaProject adoptiveProject = (JavaProject)JavaCore.create(rscProject);
				if (adoptiveProject != null 
						&& JavaProject.hasJavaNature(rscProject)) { // delta iff Java project (18698)
					for (int i = 0; i < length; i++) {
						if (orphanChildren[i] != null) {
							try {
								nonJavaResourcesChanged(adoptiveProject, orphanChildren[i]);
							} catch (JavaModelException e) {
							}
						}
					}
				}
			} // else resource delta will be added by parent
			return elementType != NON_JAVA_RESOURCE; // TODO: (jerome) do we still need to return? (check could be done by caller)
		} else {
			return elementType != NON_JAVA_RESOURCE;
		}
	}

	/**
	 * Update the classpath markers and cycle markers for the projects to update.
	 */
	void updateClasspathMarkers() {
		try {
			if (!ResourcesPlugin.getWorkspace().isAutoBuilding()) {
				Iterator iterator = this.projectsToUpdate.iterator();
				while (iterator.hasNext()) {
					try {
						JavaProject project = (JavaProject)iterator.next();
						
						 // force classpath marker refresh
						project.getResolvedClasspath(
							true, // ignoreUnresolvedEntry
							true); // generateMarkerOnError
						
					} catch (JavaModelException e) {
					}
				}
			}
			if (!this.projectsToUpdate.isEmpty()){
				try {
					// update all cycle markers
					JavaProject.updateAllCycleMarkers();
				} catch (JavaModelException e) {
				}
			}				
		} finally {
			this.projectsToUpdate = new HashSet();
		}
	}

	/*
	 * Update the current delta (ie. add/remove/change the given element) and update the correponding index.
	 * Returns whether the children of the given delta must be processed.
	 * @throws a JavaModelException if the delta doesn't correspond to a java element of the given type.
	 */
	private boolean updateCurrentDeltaAndIndex(IResourceDelta delta, int elementType, RootInfo rootInfo) {
		Openable element;
		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
				IResource deltaRes = delta.getResource();
				element = this.createElement(deltaRes, elementType, rootInfo);
				if (element == null) {
					// resource might be containing shared roots (see bug 19058)
					this.updateRoots(deltaRes.getFullPath(), delta);
					return false;
				}
				this.updateIndex(element, delta);
				this.elementAdded(element, delta, rootInfo);
				return false;
			case IResourceDelta.REMOVED :
				deltaRes = delta.getResource();
				element = this.createElement(deltaRes, elementType, rootInfo);
				if (element == null) {
					// resource might be containing shared roots (see bug 19058)
					this.updateRoots(deltaRes.getFullPath(), delta);
					return false;
				}
				this.updateIndex(element, delta);
				this.elementRemoved(element, delta, rootInfo);
	
				if (deltaRes.getType() == IResource.PROJECT){			
					// reset the corresponding project built state, since cannot reuse if added back
					this.manager.setLastBuiltState((IProject)deltaRes, null /*no state*/);
				}
				return false;
			case IResourceDelta.CHANGED :
				int flags = delta.getFlags();
				if ((flags & IResourceDelta.CONTENT) != 0) {
					// content has changed
					element = this.createElement(delta.getResource(), elementType, rootInfo);
					if (element == null) return false;
					this.updateIndex(element, delta);
					this.contentChanged(element, delta);
				} else if (elementType == IJavaElement.JAVA_PROJECT) {
					if ((flags & IResourceDelta.OPEN) != 0) {
						// project has been opened or closed
						IProject res = (IProject)delta.getResource();
						element = this.createElement(res, elementType, rootInfo);
						if (element == null) {
							// resource might be containing shared roots (see bug 19058)
							this.updateRoots(res.getFullPath(), delta);
							return false;
						}
						if (res.isOpen()) {
							if (JavaProject.hasJavaNature(res)) {
								this.elementAdded(element, delta, rootInfo);
								this.indexManager.indexAll(res);
							}
						} else {
							JavaModel javaModel = this.manager.getJavaModel();
							boolean wasJavaProject = javaModel.findJavaProject(res) != null;
							if (wasJavaProject) {
								this.elementRemoved(element, delta, rootInfo);
								this.indexManager.discardJobs(element.getElementName());
								this.indexManager.removeIndexFamily(res.getFullPath());
								
							}
						}
						return false; // when a project is open/closed don't process children
					}
					if ((flags & IResourceDelta.DESCRIPTION) != 0) {
						IProject res = (IProject)delta.getResource();
						JavaModel javaModel = this.manager.getJavaModel();
						boolean wasJavaProject = javaModel.findJavaProject(res) != null;
						boolean isJavaProject = JavaProject.hasJavaNature(res);
						if (wasJavaProject != isJavaProject) {
							// project's nature has been added or removed
							element = this.createElement(res, elementType, rootInfo);
							if (element == null) return false; // note its resources are still visible as roots to other projects
							if (isJavaProject) {
								this.elementAdded(element, delta, rootInfo);
								this.indexManager.indexAll(res);
							} else {
								this.elementRemoved(element, delta, rootInfo);
								this.indexManager.discardJobs(element.getElementName());
								this.indexManager.removeIndexFamily(res.getFullPath());
								// reset the corresponding project built state, since cannot reuse if added back
								this.manager.setLastBuiltState(res, null /*no state*/);
							}
							return false; // when a project's nature is added/removed don't process children
						}
					}
				}
				return true;
		}
		return true;
	}

	/**
	 * Traverse the set of projects which have changed namespace, and refresh their dependents
	 */
	public void updateDependentNamelookups() {
		Iterator iterator;
		// update namelookup of dependent projects
		iterator = this.projectsForDependentNamelookupRefresh.iterator();
		HashSet affectedDependents = new HashSet();
		while (iterator.hasNext()) {
			JavaProject project = (JavaProject)iterator.next();
			addDependentProjects(project.getPath(), affectedDependents);
		}
		iterator = affectedDependents.iterator();
		while (iterator.hasNext()) {
			JavaProject project = (JavaProject) iterator.next();
			if (project.isOpen()){
				try {
					((JavaProjectElementInfo)project.getElementInfo()).setNameLookup(null);
				} catch (JavaModelException e) {
				}
			}
		}
	}

protected void updateIndex(Openable element, IResourceDelta delta) {

	if (indexManager == null)
		return;

	switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT :
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
					this.indexManager.indexAll(element.getJavaProject().getProject());
					break;
				case IResourceDelta.REMOVED :
					this.indexManager.removeIndexFamily(element.getJavaProject().getProject().getFullPath());
					// NB: Discarding index jobs belonging to this project was done during PRE_DELETE
					break;
				// NB: Update of index if project is opened, closed, or its java nature is added or removed
				//     is done in updateCurrentDeltaAndIndex
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT :
			if (element instanceof JarPackageFragmentRoot) {
				JarPackageFragmentRoot root = (JarPackageFragmentRoot)element;
				// index jar file only once (if the root is in its declaring project)
				IPath jarPath = root.getPath();
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						// index the new jar
						indexManager.indexLibrary(jarPath, root.getJavaProject().getProject());
						break;
					case IResourceDelta.CHANGED:
						// first remove the index so that it is forced to be re-indexed
						indexManager.removeIndex(jarPath);
						// then index the jar
						indexManager.indexLibrary(jarPath, root.getJavaProject().getProject());
						break;
					case IResourceDelta.REMOVED:
						// the jar was physically removed: remove the index
						this.indexManager.discardJobs(jarPath.toString());
						this.indexManager.removeIndex(jarPath);
						break;
				}
				break;
			} else {
				int kind = delta.getKind();
				if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED) {
					IPackageFragmentRoot root = (IPackageFragmentRoot)element;
					this.updateRootIndex(root, root.getPackageFragment(""), delta); //$NON-NLS-1$
					break;
				}
			}
			// don't break as packages of the package fragment root can be indexed below
		case IJavaElement.PACKAGE_FRAGMENT :
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.REMOVED:
					IPackageFragment pkg = null;
					if (element instanceof IPackageFragmentRoot) {
						IPackageFragmentRoot root = (IPackageFragmentRoot)element;
						pkg = root.getPackageFragment(""); //$NON-NLS-1$
					} else {
						pkg = (IPackageFragment)element;
					}
					IResourceDelta[] children = delta.getAffectedChildren();
					for (int i = 0, length = children.length; i < length; i++) {
						IResourceDelta child = children[i];
						IResource resource = child.getResource();
						if (resource instanceof IFile) {
							String name = resource.getName();
							if (Util.isJavaFileName(name)) {
								Openable cu = (Openable)pkg.getCompilationUnit(name);
								this.updateIndex(cu, child);
							} else if (Util.isClassFileName(name)) {
								Openable classFile = (Openable)pkg.getClassFile(name);
								this.updateIndex(classFile, child);
							}
						}
					}
					break;
			}
			break;
		case IJavaElement.CLASS_FILE :
			IFile file = (IFile) delta.getResource();
			IJavaProject project = element.getJavaProject();
			IPath binaryFolderPath = element.getPackageFragmentRoot().getPath();
			// if the class file is part of the binary output, it has been created by
			// the java builder -> ignore
			try {
				if (binaryFolderPath.equals(project.getOutputLocation())) {
					break;
				}
			} catch (JavaModelException e) {
			}
			switch (delta.getKind()) {
				case IResourceDelta.CHANGED :
					// no need to index if the content has not changed
					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
						break;
				case IResourceDelta.ADDED :
					indexManager.addBinary(file, binaryFolderPath);
					break;
				case IResourceDelta.REMOVED :
					indexManager.remove(file.getFullPath().toString(), binaryFolderPath);
					break;
			}
			break;
		case IJavaElement.COMPILATION_UNIT :
			file = (IFile) delta.getResource();
			switch (delta.getKind()) {
				case IResourceDelta.CHANGED :
					// no need to index if the content has not changed
					if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
						break;
				case IResourceDelta.ADDED :
					indexManager.addSource(file, file.getProject().getProject().getFullPath());
					break;
				case IResourceDelta.REMOVED :
					indexManager.remove(file.getFullPath().toString(), file.getProject().getProject().getFullPath());
					break;
			}
	}
}
/**
 * Upadtes the index of the given root (assuming it's an addition or a removal).
 * This is done recusively, pkg being the current package.
 */
private void updateRootIndex(IPackageFragmentRoot root, IPackageFragment pkg, IResourceDelta delta) {
	this.updateIndex((Openable)pkg, delta);
	IResourceDelta[] children = delta.getAffectedChildren();
	String name = pkg.getElementName();
	for (int i = 0, length = children.length; i < length; i++) {
		IResourceDelta child = children[i];
		IResource resource = child.getResource();
		if (resource instanceof IFolder) {
			String subpkgName = 
				name.length() == 0 ? 
					resource.getName() : 
					name + "." + resource.getName(); //$NON-NLS-1$
			IPackageFragment subpkg = root.getPackageFragment(subpkgName);
			this.updateRootIndex(root, subpkg, child);
		}
	}
}
/*
 * Update the roots that are affected by the addition or the removal of the given container resource.
 */
private void updateRoots(IPath containerPath, IResourceDelta containerDelta) {
	Map roots;
	Map otherRoots;
	if (containerDelta.getKind() == IResourceDelta.REMOVED) {
		roots = this.oldRoots;
		otherRoots = this.oldOtherRoots;
	} else {
		roots = this.roots;
		otherRoots = this.otherRoots;
	}
	Iterator iterator = roots.keySet().iterator();
	while (iterator.hasNext()) {
		IPath path = (IPath)iterator.next();
		if (containerPath.isPrefixOf(path) && !containerPath.equals(path)) {
			IResourceDelta rootDelta = containerDelta.findMember(path.removeFirstSegments(1));
			if (rootDelta == null) continue;
			RootInfo rootInfo = (RootInfo)roots.get(path);

			if (!rootInfo.project.getPath().isPrefixOf(path)) { // only consider roots that are not included in the container
				this.updateCurrentDeltaAndIndex(rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
			}
			
			ArrayList rootList = (ArrayList)otherRoots.get(path);
			if (rootList != null) {
				Iterator otherProjects = rootList.iterator();
				while (otherProjects.hasNext()) {
					rootInfo = (RootInfo)otherProjects.next();
					if (!rootInfo.project.getPath().isPrefixOf(path)) { // only consider roots that are not included in the container
						this.updateCurrentDeltaAndIndex(rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
					}
				}
			}
		}
	}
}

}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.compiler.env.AccessRuleSet;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.Messages;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * This operation sets an <code>IJavaProject</code>'s classpath.
 *
 * @see IJavaProject
 */
public class SetClasspathOperation extends JavaModelOperation {

	IClasspathEntry[] oldResolvedPath, newResolvedPath;
	IClasspathEntry[] newRawPath;
	boolean canChangeResources;
	boolean classpathWasSaved;
	boolean needCycleCheck;
	boolean needValidation;
	boolean needSave;
	IPath newOutputLocation;
	JavaProject project;
	boolean identicalRoots;
	
	public static final IClasspathEntry[] REUSE_ENTRIES = new IClasspathEntry[0];
	public static final IClasspathEntry[] UPDATE_ENTRIES = new IClasspathEntry[0];
	// if reusing output location, then also reuse clean flag
	public static final IPath REUSE_PATH = new Path("Reuse Existing Output Location");  //$NON-NLS-1$
	public static final IPath[] REUSE_PATHS = new IPath[0];
	
	/**
	 * When executed, this operation sets the classpath of the given project.
	 */
	public SetClasspathOperation(
		JavaProject project,
		IClasspathEntry[] oldResolvedPath,
		IClasspathEntry[] newRawPath,
		IPath newOutputLocation,
		boolean canChangeResource,
		boolean needValidation,
		boolean needSave) {

		super(new IJavaElement[] { project });
		this.oldResolvedPath = oldResolvedPath;
		this.newRawPath = newRawPath;
		this.newOutputLocation = newOutputLocation;
		this.canChangeResources = canChangeResource;
		this.needValidation = needValidation;
		this.needSave = needSave;
		this.project = project;
	}

	/**
	 * Adds deltas for the given roots, with the specified change flag,
	 * and closes the root. Helper method for #setClasspath
	 */
	protected void addClasspathDeltas(
		IPackageFragmentRoot[] roots,
		int flag,
		JavaElementDelta delta) {

		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			delta.changed(root, flag);
			if ((flag & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0 
					|| (flag & IJavaElementDelta.F_SOURCEATTACHED) != 0
					|| (flag & IJavaElementDelta.F_SOURCEDETACHED) != 0){
				try {
					root.close();
				} catch (JavaModelException e) {
					// ignore
				}
				// force detach source on jar package fragment roots (source will be lazily computed when needed)
				((PackageFragmentRoot) root).setSourceAttachmentProperty(null);// loose info - will be recomputed
			}
		}
	}

	protected boolean canModifyRoots() {
		// setting classpath can modify roots
		return true;
	}

	/**
	 * Returns the index of the item in the list if the given list contains the specified entry. If the list does
	 * not contain the entry, -1 is returned.
	 * A helper method for #setClasspath
	 */
	protected int classpathContains(
		IClasspathEntry[] list,
		IClasspathEntry entry) {

		IPath[] exclusionPatterns = entry.getExclusionPatterns();
		IPath[] inclusionPatterns = entry.getInclusionPatterns();
		nextEntry: for (int i = 0; i < list.length; i++) {
			IClasspathEntry other = list[i];
			if (other.getContentKind() == entry.getContentKind()
				&& other.getEntryKind() == entry.getEntryKind()
				&& other.isExported() == entry.isExported()
				&& other.getPath().equals(entry.getPath())) {
					// check custom outputs
					IPath entryOutput = entry.getOutputLocation();
					IPath otherOutput = other.getOutputLocation();
					if (entryOutput == null) {
						if (otherOutput != null)
							continue;
					} else {
						if (!entryOutput.equals(otherOutput))
							continue;
					}
					
					// check inclusion patterns
					IPath[] otherIncludes = other.getInclusionPatterns();
					if (inclusionPatterns != otherIncludes) {
					    if (inclusionPatterns == null) continue;
						int includeLength = inclusionPatterns.length;
						if (otherIncludes == null || otherIncludes.length != includeLength)
							continue;
						for (int j = 0; j < includeLength; j++) {
							// compare toStrings instead of IPaths 
							// since IPath.equals is specified to ignore trailing separators
							if (!inclusionPatterns[j].toString().equals(otherIncludes[j].toString()))
								continue nextEntry;
						}
					}
					// check exclusion patterns
					IPath[] otherExcludes = other.getExclusionPatterns();
					if (exclusionPatterns != otherExcludes) {
					    if (exclusionPatterns == null) continue;
						int excludeLength = exclusionPatterns.length;
						if (otherExcludes == null || otherExcludes.length != excludeLength)
							continue;
						for (int j = 0; j < excludeLength; j++) {
							// compare toStrings instead of IPaths 
							// since IPath.equals is specified to ignore trailing separators
							if (!exclusionPatterns[j].toString().equals(otherExcludes[j].toString()))
								continue nextEntry;
						}
					}
					return i;
			}
		}
		return -1;
	}

	/**
	 * Recursively adds all subfolders of <code>folder</code> to the given collection.
	 */
	protected void collectAllSubfolders(IFolder folder, ArrayList collection) throws JavaModelException {
		try {
			IResource[] members= folder.members();
			for (int i = 0, max = members.length; i < max; i++) {
				IResource r= members[i];
				if (r.getType() == IResource.FOLDER) {
					collection.add(r);
					collectAllSubfolders((IFolder)r, collection);
				}
			}	
		} catch (CoreException e) {
			throw new JavaModelException(e);
		}
	}

	/**
	 * Returns a collection of package fragments that have been added/removed
	 * as the result of changing the output location to/from the given
	 * location. The collection is empty if no package fragments are
	 * affected.
	 */
	protected ArrayList determineAffectedPackageFragments(IPath location) throws JavaModelException {
		ArrayList fragments = new ArrayList();
	
		// see if this will cause any package fragments to be affected
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IResource resource = null;
		if (location != null) {
			resource = workspace.getRoot().findMember(location);
		}
		if (resource != null && resource.getType() == IResource.FOLDER) {
			IFolder folder = (IFolder) resource;
			// only changes if it actually existed
			IClasspathEntry[] classpath = project.getExpandedClasspath(true);
			for (int i = 0; i < classpath.length; i++) {
				IClasspathEntry entry = classpath[i];
				IPath path = classpath[i].getPath();
				if (entry.getEntryKind() != IClasspathEntry.CPE_PROJECT && path.isPrefixOf(location) && !path.equals(location)) {
					IPackageFragmentRoot[] roots = project.computePackageFragmentRoots(classpath[i]);
					PackageFragmentRoot root = (PackageFragmentRoot) roots[0];
					// now the output location becomes a package fragment - along with any subfolders
					ArrayList folders = new ArrayList();
					folders.add(folder);
					collectAllSubfolders(folder, folders);
					Iterator elements = folders.iterator();
					int segments = path.segmentCount();
					while (elements.hasNext()) {
						IFolder f = (IFolder) elements.next();
						IPath relativePath = f.getFullPath().removeFirstSegments(segments);
						String[] pkgName = relativePath.segments();
						IPackageFragment pkg = root.getPackageFragment(pkgName);
						fragments.add(pkg);
					}
				}
			}
		}
		return fragments;
	}

	/**
	 * Sets the classpath of the pre-specified project.
	 */
	protected void executeOperation() throws JavaModelException {
		// project reference updated - may throw an exception if unable to write .project file
		updateProjectReferencesIfNecessary();

		// classpath file updated - may throw an exception if unable to write .classpath file
		saveClasspathIfNecessary();
		
		// perform classpath and output location updates, if exception occurs in classpath update,
		// make sure the output location is updated before surfacing the exception (in case the output
		// location update also throws an exception, give priority to the classpath update one).
		JavaModelException originalException = null;

		try {
			if (this.newRawPath == UPDATE_ENTRIES) this.newRawPath = project.getRawClasspath();
			if (this.newRawPath != REUSE_ENTRIES){
				updateClasspath();
				project.updatePackageFragmentRoots();
				JavaModelManager.getJavaModelManager().getDeltaProcessor().addForRefresh(project);
			}

		} catch(JavaModelException e){
			originalException = e;
			throw e;

		} finally { // if traversed by an exception we still need to update the output location when necessary

			try {
				if (this.newOutputLocation != REUSE_PATH) updateOutputLocation();

			} catch(JavaModelException e){
				if (originalException != null) throw originalException; 
				throw e;
			} finally {
				// ensures the project is getting rebuilt if only variable is modified
				if (!this.identicalRoots && this.canChangeResources) {
					try {
						this.project.getProject().touch(this.progressMonitor);
					} catch (CoreException e) {
						if (JavaModelManager.CP_RESOLVE_VERBOSE){
							Util.verbose("CPContainer INIT - FAILED to touch project: "+ this.project.getElementName(), System.err); //$NON-NLS-1$
							e.printStackTrace();
						}
					}
				}				
			}
		}
		done();
	}

	/**
	 * Generates the delta of removed/added/reordered roots.
	 * Use three deltas in case the same root is removed/added/reordered (for
	 * instance, if it is changed from K_SOURCE to K_BINARY or vice versa)
	 */
	protected void generateClasspathChangeDeltas() {

		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		boolean needToUpdateDependents = false;
		JavaElementDelta delta = new JavaElementDelta(getJavaModel());
		boolean hasDelta = false;
		if (this.classpathWasSaved) {
			delta.changed(this.project, IJavaElementDelta.F_CLASSPATH_CHANGED);
			hasDelta = true;
		}
		int oldLength = oldResolvedPath.length;
		int newLength = newResolvedPath.length;
			
		final IndexManager indexManager = manager.getIndexManager();
		Map oldRoots = null;
		IPackageFragmentRoot[] roots = null;
		if (project.isOpen()) {
			try {
				roots = project.getPackageFragmentRoots();
			} catch (JavaModelException e) {
				// ignore
			}
		} else {
			Map allRemovedRoots ;
			if ((allRemovedRoots = manager.getDeltaProcessor().removedRoots) != null) {
		 		roots = (IPackageFragmentRoot[]) allRemovedRoots.get(project);
			}
		}
		if (roots != null) {
			oldRoots = new HashMap();
			for (int i = 0; i < roots.length; i++) {
				IPackageFragmentRoot root = roots[i];
				oldRoots.put(root.getPath(), root);
			}
		}
		for (int i = 0; i < oldLength; i++) {
			
			int index = classpathContains(newResolvedPath, oldResolvedPath[i]);
			if (index == -1) {
				// do not notify remote project changes
				if (oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					needToUpdateDependents = true;
					this.needCycleCheck = true;
					continue; 
				}

				IPackageFragmentRoot[] pkgFragmentRoots = null;
				if (oldRoots != null) {
					IPackageFragmentRoot oldRoot = (IPackageFragmentRoot)  oldRoots.get(oldResolvedPath[i].getPath());
					if (oldRoot != null) { // use old root if any (could be none if entry wasn't bound)
						pkgFragmentRoots = new IPackageFragmentRoot[] { oldRoot };
					}
				}
				if (pkgFragmentRoots == null) {
					try {
						ObjectVector accumulatedRoots = new ObjectVector();
						HashSet rootIDs = new HashSet(5);
						rootIDs.add(project.rootID());
						project.computePackageFragmentRoots(
							oldResolvedPath[i], 
							accumulatedRoots, 
							rootIDs,
							null, // inside original project
							false, // don't check existency
							false, // don't retrieve exported roots
							null); /*no reverse map*/
						pkgFragmentRoots = new IPackageFragmentRoot[accumulatedRoots.size()];
						accumulatedRoots.copyInto(pkgFragmentRoots);
					} catch (JavaModelException e) {
						pkgFragmentRoots =  new IPackageFragmentRoot[] {};
					}
				}
				addClasspathDeltas(pkgFragmentRoots, IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, delta);
				
				int changeKind = oldResolvedPath[i].getEntryKind();
				needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE) || oldResolvedPath[i].isExported();

				// Remove the .java files from the index for a source folder
				// For a lib folder or a .jar file, remove the corresponding index if not shared.
				if (indexManager != null) {
					IClasspathEntry oldEntry = oldResolvedPath[i];
					final IPath path = oldEntry.getPath();
					switch (changeKind) {
						case IClasspathEntry.CPE_SOURCE:
							final char[][] inclusionPatterns = ((ClasspathEntry)oldEntry).fullInclusionPatternChars();
							final char[][] exclusionPatterns = ((ClasspathEntry)oldEntry).fullExclusionPatternChars();
							postAction(new IPostAction() {
								public String getID() {
									return path.toString();
								}
								public void run() /* throws JavaModelException */ {
									indexManager.removeSourceFolderFromIndex(project, path, inclusionPatterns, exclusionPatterns);
								}
							}, 
							REMOVEALL_APPEND);
							break;
						case IClasspathEntry.CPE_LIBRARY:
							final DeltaProcessingState deltaState = manager.deltaState;
							postAction(new IPostAction() {
								public String getID() {
									return path.toString();
								}
								public void run() /* throws JavaModelException */ {
									if (deltaState.otherRoots.get(path) == null) { // if root was not shared
										indexManager.discardJobs(path.toString());
										indexManager.removeIndex(path);
										// TODO (kent) we could just remove the in-memory index and have the indexing check for timestamps
									}
								}
							}, 
							REMOVEALL_APPEND);
							break;
					}		
				}
				hasDelta = true;

			} else {
				// do not notify remote project changes
				if (oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					// Need to updated dependents in case old and/or new entries are exported and have an access restriction
					ClasspathEntry oldEntry = (ClasspathEntry) oldResolvedPath[i];
					ClasspathEntry newEntry = (ClasspathEntry) newResolvedPath[index];
					if (oldEntry.isExported || newEntry.isExported) { // then we need to verify if there's access restriction
						AccessRuleSet oldRuleSet = oldEntry.getAccessRuleSet();
						AccessRuleSet newRuleSet = newEntry.getAccessRuleSet();
						if (index != i) { // entry has been moved
							needToUpdateDependents |= (oldRuleSet != null || newRuleSet != null); // there's an access restriction, this may change combination
						} else if (oldRuleSet == null) {
							needToUpdateDependents |= newRuleSet != null; // access restriction was added
						} else {
							needToUpdateDependents |= !oldRuleSet.equals(newRuleSet); // access restriction has changed or has been removed
						}
					}
					this.needCycleCheck |= (oldEntry.isExported() != newEntry.isExported());
					continue; 
				}				
				needToUpdateDependents |= (oldResolvedPath[i].isExported() != newResolvedPath[index].isExported());
				if (index != i) { //reordering of the classpath
						addClasspathDeltas(
							project.computePackageFragmentRoots(oldResolvedPath[i]),
							IJavaElementDelta.F_REORDER,
							delta);
						int changeKind = oldResolvedPath[i].getEntryKind();
						needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE);
		
						hasDelta = true;
				}
				
				// check source attachment
				IPath newSourcePath = newResolvedPath[index].getSourceAttachmentPath();
				int sourceAttachmentFlags = 
					this.getSourceAttachmentDeltaFlag(
						oldResolvedPath[i].getSourceAttachmentPath(),
						newSourcePath);
				IPath oldRootPath = oldResolvedPath[i].getSourceAttachmentRootPath();
				IPath newRootPath = newResolvedPath[index].getSourceAttachmentRootPath();
				int sourceAttachmentRootFlags = getSourceAttachmentDeltaFlag(oldRootPath, newRootPath);
				int flags = sourceAttachmentFlags | sourceAttachmentRootFlags;
				if (flags != 0) {
					addClasspathDeltas(project.computePackageFragmentRoots(oldResolvedPath[i]), flags, delta);
					hasDelta = true;
				} else {
					if (oldRootPath == null && newRootPath == null) {
						// if source path is specified and no root path, it needs to be recomputed dynamically
						// force detach source on jar package fragment roots (source will be lazily computed when needed)
						IPackageFragmentRoot[] computedRoots = project.computePackageFragmentRoots(oldResolvedPath[i]);
						for (int j = 0; j < computedRoots.length; j++) {
							IPackageFragmentRoot root = computedRoots[j];
							// force detach source on jar package fragment roots (source will be lazily computed when needed)
							try {
								root.close();
							} catch (JavaModelException e) {
								// ignore
							}
							((PackageFragmentRoot) root).setSourceAttachmentProperty(null);// loose info - will be recomputed
						}
					}
				}
			}
		}

		for (int i = 0; i < newLength; i++) {

			int index = classpathContains(oldResolvedPath, newResolvedPath[i]);
			if (index == -1) {
				// do not notify remote project changes
				if (newResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT){
					needToUpdateDependents = true;
					this.needCycleCheck = true;
					continue; 
				}
				addClasspathDeltas(
					project.computePackageFragmentRoots(newResolvedPath[i]),
					IJavaElementDelta.F_ADDED_TO_CLASSPATH,
					delta);
				int changeKind = newResolvedPath[i].getEntryKind();
				
				// Request indexing
				if (indexManager != null) {
					switch (changeKind) {
						case IClasspathEntry.CPE_LIBRARY:
							boolean pathHasChanged = true;
							final IPath newPath = newResolvedPath[i].getPath();
							for (int j = 0; j < oldLength; j++) {
								IClasspathEntry oldEntry = oldResolvedPath[j];
								if (oldEntry.getPath().equals(newPath)) {
									pathHasChanged = false;
									break;
								}
							}
							if (pathHasChanged) {
								postAction(new IPostAction() {
									public String getID() {
										return newPath.toString();
									}
									public void run() /* throws JavaModelException */ {
										indexManager.indexLibrary(newPath, project.getProject());
									}
								}, 
								REMOVEALL_APPEND);
							}
							break;
						case IClasspathEntry.CPE_SOURCE:
							IClasspathEntry entry = newResolvedPath[i];
							final IPath path = entry.getPath();
							final char[][] inclusionPatterns = ((ClasspathEntry)entry).fullInclusionPatternChars();
							final char[][] exclusionPatterns = ((ClasspathEntry)entry).fullExclusionPatternChars();
							postAction(new IPostAction() {
								public String getID() {
									return path.toString();
								}
								public void run() /* throws JavaModelException */ {
									indexManager.indexSourceFolder(project, path, inclusionPatterns, exclusionPatterns);
								}
							}, 
							APPEND); // append so that a removeSourceFolder action is not removed
							break;
					}
				}
				
				needToUpdateDependents |= (changeKind == IClasspathEntry.CPE_SOURCE) || newResolvedPath[i].isExported();
				hasDelta = true;

			} // classpath reordering has already been generated in previous loop
		}

		if (hasDelta) {
			this.addDelta(delta);
		} else {
			this.identicalRoots = true;
		}
		if (needToUpdateDependents){
			updateAffectedProjects(project.getProject().getFullPath());
		}
	}
	/*
	 * Returns the source attachment flag for the delta between the 2 give source paths.
	 * Returns either F_SOURCEATTACHED, F_SOURCEDETACHED, F_SOURCEATTACHED | F_SOURCEDETACHED
	 * or 0 if there is no difference.
	 */
	private int getSourceAttachmentDeltaFlag(IPath oldPath, IPath newPath) {
		if (oldPath == null) {
			if (newPath != null) {
				return IJavaElementDelta.F_SOURCEATTACHED;
			} else {
				return 0;
			}
		} else if (newPath == null) {
			return IJavaElementDelta.F_SOURCEDETACHED;
		} else if (!oldPath.equals(newPath)) {
			return IJavaElementDelta.F_SOURCEATTACHED | IJavaElementDelta.F_SOURCEDETACHED;
		} else {
			return 0;
		}
	}

	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return !this.canChangeResources;
	}

	protected void saveClasspathIfNecessary() throws JavaModelException {
		
		if (!this.canChangeResources || !this.needSave) return;
				
		IClasspathEntry[] classpathForSave;
		if (this.newRawPath == REUSE_ENTRIES || this.newRawPath == UPDATE_ENTRIES){
			classpathForSave = project.getRawClasspath();
		} else {
			classpathForSave = this.newRawPath;
		}
		IPath outputLocationForSave;
		if (this.newOutputLocation == REUSE_PATH){
			outputLocationForSave = project.getOutputLocation();
		} else {
			outputLocationForSave = this.newOutputLocation;
		}
		// if read-only .classpath, then the classpath setting will never been performed completely
		if (project.saveClasspath(classpathForSave, outputLocationForSave)) {
			this.classpathWasSaved = true;
			this.setAttribute(HAS_MODIFIED_RESOURCE_ATTR, TRUE); 
		}
	}
	
	public String toString(){
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("SetClasspathOperation\n"); //$NON-NLS-1$
		buffer.append(" - classpath : "); //$NON-NLS-1$
		if (this.newRawPath == REUSE_ENTRIES){
			buffer.append("<Reuse Existing Classpath>"); //$NON-NLS-1$
		} else {
			buffer.append("{"); //$NON-NLS-1$
			for (int i = 0; i < this.newRawPath.length; i++) {
				if (i > 0) buffer.append(","); //$NON-NLS-1$
				IClasspathEntry element = this.newRawPath[i];
				buffer.append(" ").append(element.toString()); //$NON-NLS-1$
			}
		}
		buffer.append("\n - output location : ");  //$NON-NLS-1$
		if (this.newOutputLocation == REUSE_PATH){
			buffer.append("<Reuse Existing Output Location>"); //$NON-NLS-1$
		} else {
			buffer.append(this.newOutputLocation.toString()); //$NON-NLS-1$
		}
		return buffer.toString();
	}

	private void updateClasspath() throws JavaModelException {

		beginTask(Messages.bind(Messages.classpath_settingProgress, project.getElementName()), 2); 

		// SIDE-EFFECT: from thereon, the classpath got modified
		project.getPerProjectInfo().updateClasspathInformation(this.newRawPath);

		// resolve new path (asking for marker creation if problems)
		if (this.newResolvedPath == null) {
			this.newResolvedPath = project.getResolvedClasspath(true, this.canChangeResources, false/*don't returnResolutionInProgress*/);
		}
		
		if (this.oldResolvedPath != null) {
			generateClasspathChangeDeltas();
		} else {
			this.needCycleCheck = true;
			updateAffectedProjects(project.getProject().getFullPath());
		}
		
		updateCycleMarkersIfNecessary();
	}

	/**
	 * Update projects which are affected by this classpath change:
	 * those which refers to the current project as source (indirectly)
	 */
	protected void updateAffectedProjects(IPath prerequisiteProjectPath) {

		// remove all update classpath post actions for this project
		final String updateClasspath = "UpdateClassPath:"; //$NON-NLS-1$
		removeAllPostAction(updateClasspath + prerequisiteProjectPath.toString());
		
		try {
			IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject initialProject = this.project;
			IJavaProject[] projects = model.getJavaProjects();
			for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
				try {
					final JavaProject affectedProject = (JavaProject) projects[i];
					if (affectedProject.equals(initialProject)) continue; // skip itself
					
					// consider ALL dependents (even indirect ones), since they may need to
					// flush their respective namelookup caches (all pkg fragment roots).

					IClasspathEntry[] classpath = affectedProject.getExpandedClasspath(true);
					for (int j = 0, entryCount = classpath.length; j < entryCount; j++) {
						IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
							&& entry.getPath().equals(prerequisiteProjectPath)) {
								
							postAction(new IPostAction() {
									public String getID() {
										return updateClasspath + affectedProject.getPath().toString();
									}
									public void run() throws JavaModelException {
										affectedProject.setRawClasspath(
											UPDATE_ENTRIES, 
											SetClasspathOperation.REUSE_PATH, 
											SetClasspathOperation.this.progressMonitor, 
											SetClasspathOperation.this.canChangeResources,  
											affectedProject.getResolvedClasspath(true/*ignoreUnresolvedEntry*/, false/*don't generateMarkerOnError*/, false/*don't returnResolutionInProgress*/), 
											false, // updating only - no validation
											false); // updating only - no need to save
									}
								},
								REMOVEALL_APPEND);
							break;
						}
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
		} catch (JavaModelException e) {
			// ignore
		}
		
	}

	/**
	 * Update cycle markers
	 */
	protected void updateCycleMarkersIfNecessary() {

		if (!this.needCycleCheck) return;
		if (!this.canChangeResources) return;
		 
		if (!project.hasCycleMarker() && !project.hasClasspathCycle(newResolvedPath)){
			return;
		}
	
		postAction(
			new IPostAction() {
				public String getID() {
					return "updateCycleMarkers";  //$NON-NLS-1$
				}
				public void run() throws JavaModelException {
					JavaProject.updateAllCycleMarkers(null);
				}
			},
			REMOVEALL_APPEND);
	}

	/**
	 * Sets the output location of the pre-specified project.
	 *
	 * <p>This can cause changes in package fragments, in case either  the
	 * old or new output location folder are considered as a package fragment.
	 */
	protected void updateOutputLocation() throws JavaModelException {
		
		beginTask(Messages.bind(Messages.classpath_settingOutputLocationProgress, project.getElementName()), 2); 
		
		IPath oldLocation= project.getOutputLocation();
	
		// see if this will cause any package fragments to be added
		boolean deltaToFire= false;
		JavaElementDelta delta = newJavaElementDelta();
		ArrayList added= determineAffectedPackageFragments(oldLocation);
		Iterator iter = added.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close();
			if (!Util.isExcluded(frag)) {
				delta.added(frag);
				deltaToFire = true;
			}
		}
	
		// see if this will cause any package fragments to be removed
		ArrayList removed= determineAffectedPackageFragments(this.newOutputLocation);
		iter = removed.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close(); 
			if (!Util.isExcluded(frag)) {
				delta.removed(frag);
				deltaToFire = true;
			}
		}

		JavaModelManager.PerProjectInfo perProjectInfo = project.getPerProjectInfo();
		synchronized (perProjectInfo) {
			perProjectInfo.outputLocation = this.newOutputLocation;
		}
				
		if (deltaToFire) {
			addDelta(delta);	
		}
		worked(1);
	}
	
	/**
	 * Update projects references so that the build order is consistent with the classpath
	 */
	protected void updateProjectReferencesIfNecessary() throws JavaModelException {
		
		if (this.newRawPath == REUSE_ENTRIES || this.newRawPath == UPDATE_ENTRIES) return;
		// will run now, or be deferred until next pre-auto-build notification if resource tree is locked
		JavaModelManager.getJavaModelManager().deltaState.performClasspathResourceChange(
		        project, 
		        oldResolvedPath, 
		        newResolvedPath, 
		        newRawPath, 
		        canChangeResources);
	}

	public IJavaModelStatus verify() {

		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}

		if (needValidation) {
			// retrieve classpath 
			IClasspathEntry[] entries = this.newRawPath;
			if (entries == REUSE_ENTRIES){
				try {
					entries = project.getRawClasspath();			
				} catch (JavaModelException e) {
					return e.getJavaModelStatus();
				}
			}		
			// retrieve output location
			IPath outputLocation = this.newOutputLocation;
			if (outputLocation == REUSE_PATH){
				try {
					outputLocation = project.getOutputLocation();
				} catch (JavaModelException e) {
					return e.getJavaModelStatus();
				}
			}
					
			// perform validation
			return ClasspathEntry.validateClasspath(
				project,
				entries,
				outputLocation);
		}
		
		return JavaModelStatus.VERIFIED_OK;
	}
}

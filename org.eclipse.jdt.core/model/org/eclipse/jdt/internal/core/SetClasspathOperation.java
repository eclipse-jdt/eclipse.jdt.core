package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;

/**
 * This operation sets an <code>IJavaProject</code>'s classpath.
 *
 * @see IJavaProject
 */
public class SetClasspathOperation extends JavaModelOperation {

	IClasspathEntry[] oldResolvedPath;
	IClasspathEntry[] newRawPath;
	boolean canChangeResource;
	boolean forceSave;
	boolean mayChangeProjectDependencies;
	
	IPath newOutputLocation;
	public static final IClasspathEntry[] ReuseClasspath = new IClasspathEntry[0];
	public static final IPath ReuseOutputLocation = new Path("Reuse Existing Output Location");  //$NON-NLS-1$
	
	/**
	 * When executed, this operation sets the classpath of the given project.
	 */
	public SetClasspathOperation(
		IJavaProject project,
		IClasspathEntry[] oldResolvedPath,
		IClasspathEntry[] newRawPath,
		IPath newOutputLocation,
		boolean canChangeResource,
		boolean forceSave,
		boolean mayChangeProjectDependencies) {

		super(new IJavaElement[] { project });
		this.oldResolvedPath = oldResolvedPath;
		this.newRawPath = newRawPath;
		this.newOutputLocation = newOutputLocation;
		this.canChangeResource = canChangeResource;
		this.forceSave = forceSave;
		this.mayChangeProjectDependencies = mayChangeProjectDependencies;
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
					|| (flag & IJavaElementDelta.F_SOURCEDETACHED) != 0){
				try {
					root.close();
				} catch (JavaModelException e) {
				}
				// force detach source on jar package fragment roots (source will be lazily computed when needed)
				if (root instanceof JarPackageFragmentRoot) {
					((JarPackageFragmentRoot) root).setSourceAttachmentProperty(null);// loose info - will be recomputed
				}
			}
		}
	}



	/**
	 * Returns the index of the item in the list if the given list contains the specified entry. If the list does
	 * not contain the entry, -1 is returned.
	 * A helper method for #setClasspath
	 */
	protected int classpathContains(
		IClasspathEntry[] list,
		IClasspathEntry entry) {

		for (int i = 0; i < list.length; i++) {
			if (list[i].getPath().equals(entry.getPath())) {
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
	 * Sets the classpath of the pre-specified project.
	 */
	protected void executeOperation() throws JavaModelException {

		if (this.newRawPath != ReuseClasspath){
			updateClasspath();
		}
		if (this.newOutputLocation != ReuseOutputLocation){
			updateOutputLocation();
		}
		done();
	}
	
	/**
	 * Returns <code>true</code> if this operation performs no resource modifications,
	 * otherwise <code>false</code>. Subclasses must override.
	 */
	public boolean isReadOnly() {
		return !this.canChangeResource;
	}
	private void updateClasspath() throws JavaModelException {

		JavaProject project = ((JavaProject) getElementsToProcess()[0]);

		beginTask(Util.bind("classpath.settingProgress", project.getElementName()), 2); //$NON-NLS-1$

		String[] oldRequired = project.getRequiredProjectNames();
		
		project.setRawClasspath0(this.newRawPath);

		// resolve new path (asking for marker creation if problems)
		IClasspathEntry[] newResolvedPath = 
			project.getResolvedClasspath(true,  this.canChangeResource);// also update cp markers

		if (this.oldResolvedPath != null) {
			generateClasspathChangeDeltas(
				this.oldResolvedPath,
				newResolvedPath,
				project.getJavaModelManager(),
				project);
		} else {
			this.hasModifiedResource = project.saveClasspath(this.forceSave);
			updateAffectedProjects(project.getProject().getFullPath());
		}
		
		if (this.mayChangeProjectDependencies){
			updateProjectReferences(oldRequired, project.getRequiredProjectNames());
			updateCycleMarkers();
		}
	}
	/**
	 * Sets the output location of the pre-specified project.
	 *
	 * <p>This can cause changes in package fragments - i.e. if the
	 * old and new output location folder could be considered as
	 * a package fragment.
	 */
	protected void updateOutputLocation() throws JavaModelException {
		
		JavaProject project= ((JavaProject) getElementsToProcess()[0]);

		beginTask(Util.bind("classpath.settingOutputLocationProgress", project.getElementName()), 2); //$NON-NLS-1$
		
		IPath oldLocation= project.getOutputLocation();
	
		// see if this will cause any package fragments to be added
		boolean deltaToFire= false;
		JavaElementDelta delta = newJavaElementDelta();
		ArrayList added= determineAffectedPackageFragments(oldLocation);
		Iterator iter = added.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close();
			delta.added(frag);
			deltaToFire = true;
		}
	
		// see if this will cause any package fragments to be removed
		ArrayList removed= determineAffectedPackageFragments(this.newOutputLocation);
		iter = removed.iterator();
		while (iter.hasNext()){
			IPackageFragment frag= (IPackageFragment)iter.next();
			((IPackageFragmentRoot)frag.getParent()).close();
			delta.removed(frag);
			deltaToFire = true;
		}
		
		project.getJavaProjectElementInfo().setOutputLocation(this.newOutputLocation);
		if (deltaToFire) {
			addDelta(delta);	
		}
		worked(1);
		// .classpath file may already have been updated in #updateClasspath
		this.hasModifiedResource |= project.saveClasspath(false);
		worked(1);
	}
	
	/**
	 * Returns a collection of package fragments that have been added/removed
	 * as the result of changing the output location to/from the given
	 * location. The collection is empty if no package fragments are
	 * affected.
	 */
	protected ArrayList determineAffectedPackageFragments(IPath location) throws JavaModelException {
		ArrayList fragments = new ArrayList();
		JavaProject project = ((JavaProject) getElementsToProcess()[0]);
	
		// see if this will cause any package fragments to be affected
		IWorkspace workspace = getWorkspace();
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
					IPackageFragmentRoot[] roots = project.getPackageFragmentRoots(classpath[i]);
					IPackageFragmentRoot root = roots[0];
					// now the output location becomes a package fragment - along with any subfolders
					ArrayList folders = new ArrayList();
					folders.add(folder);
					collectAllSubfolders(folder, folders);
					Iterator elements = folders.iterator();
					int segments = path.segmentCount();
					while (elements.hasNext()) {
						IFolder f = (IFolder) elements.next();
						IPath relativePath = f.getFullPath().removeFirstSegments(segments);
						String name = relativePath.toOSString();
						name = name.replace(File.pathSeparatorChar, '.');
						if (name.endsWith(".")) { //$NON-NLS-1$
							name = name.substring(0, name.length() - 1);
						}
						IPackageFragment pkg = root.getPackageFragment(name);
						fragments.add(pkg);
					}
				}
			}
		}
		return fragments;
	}

	/**
	 * Generates the delta of removed/added/reordered roots.
	 * Use three deltas in case the same root is removed/added/reordered (i.e. changed from
	 * K_SOURCE to K_BINARY or visa versa)
	 */
	protected void generateClasspathChangeDeltas(
		IClasspathEntry[] oldResolvedPath,
		IClasspathEntry[] newResolvedPath,
		JavaModelManager manager,
		JavaProject project) {

		boolean hasChangedContentForDependents = false;

		JavaElementDelta delta = new JavaElementDelta(getJavaModel());
		boolean hasDelta = false;
		boolean oldResolvedPathLongest =
			oldResolvedPath.length >= newResolvedPath.length;
			
		IndexManager indexManager = manager.getIndexManager();
		for (int i = 0; i < oldResolvedPath.length; i++) {
			
			// do not notify remote project changes
			if (oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) continue; 
			
			int index = classpathContains(newResolvedPath, oldResolvedPath[i]);
			if (index == -1) {
				IPackageFragmentRoot[] pkgFragmentRoots =
					project.getPackageFragmentRoots(oldResolvedPath[i]);
				addClasspathDeltas(pkgFragmentRoots, IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, delta);

				int changeKind = oldResolvedPath[i].getEntryKind();
				hasChangedContentForDependents |= 
					(changeKind == IClasspathEntry.CPE_SOURCE) || oldResolvedPath[i].isExported();

				// Remove the .java files from the index.
				// Note that .class files belong to binary folders which can be shared, 
				// so leave the index for .class files.
				if (indexManager != null && changeKind == IClasspathEntry.CPE_SOURCE) {
					indexManager.removeSourceFolderFromIndex(project, oldResolvedPath[i].getPath());
				}
				hasDelta = true;

			} else {
				hasChangedContentForDependents |= (oldResolvedPath[i].isExported() != newResolvedPath[index].isExported());
				if (oldResolvedPathLongest && index != i) { //reordering of the classpath
						addClasspathDeltas(
							project.getPackageFragmentRoots(oldResolvedPath[i]),
							IJavaElementDelta.F_CLASSPATH_REORDER,
							delta);
						int changeKind = oldResolvedPath[i].getEntryKind();
						hasChangedContentForDependents |= (changeKind == IClasspathEntry.CPE_SOURCE);
		
						hasDelta = true;
				}
				
				// check source attachment
				int sourceAttachmentFlags = 
					this.getSourceAttachmentDeltaFlag(
						oldResolvedPath[i].getSourceAttachmentPath(),
						newResolvedPath[index].getSourceAttachmentPath());
				int sourceAttachmentRootFlags = 
					this.getSourceAttachmentDeltaFlag(
						oldResolvedPath[i].getSourceAttachmentRootPath(),
						newResolvedPath[index].getSourceAttachmentRootPath());
				int flags = sourceAttachmentFlags | sourceAttachmentRootFlags;
				if (flags != 0) {
					addClasspathDeltas(
						project.getPackageFragmentRoots(oldResolvedPath[i]),
						flags | IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, // TEMPORARY: Need to signal a F_REMOVED_FROM_CLASSPATH so that UI updates correctly
						delta);
					hasDelta = true;
				}
			}
		}

		for (int i = 0; i < newResolvedPath.length; i++) {

			// do not notify remote project changes
			if (newResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_PROJECT) continue; 

			int index = classpathContains(oldResolvedPath, newResolvedPath[i]);
			if (index == -1) {
				addClasspathDeltas(
					project.getPackageFragmentRoots(newResolvedPath[i]),
					IJavaElementDelta.F_ADDED_TO_CLASSPATH,
					delta);
				int changeKind = newResolvedPath[i].getEntryKind();
				
				// Request indexing
				if (indexManager != null) {
					switch (changeKind) {
						case IClasspathEntry.CPE_LIBRARY:
							boolean pathHasChanged = true;
							IPath newPath = newResolvedPath[i].getPath();
							for (int j = 0; j < oldResolvedPath.length; j++) {
								IClasspathEntry oldEntry = oldResolvedPath[j];
								if (oldEntry.getPath().equals(newPath)) {
									pathHasChanged = false;
									break;
								}
							}
							if (pathHasChanged) {
								indexManager.indexLibrary(newPath, project.getProject());
							}
							break;
						case IClasspathEntry.CPE_SOURCE:
							indexManager.indexSourceFolder(project, newResolvedPath[i].getPath());
							break;
					}
				}
				
				hasChangedContentForDependents |= 
					(changeKind == IClasspathEntry.CPE_SOURCE) || newResolvedPath[i].isExported();
				hasDelta = true;

			} // classpath reordering has already been generated in previous loop
		}
		if (hasDelta) {
			try {
				this.hasModifiedResource = project.saveClasspath(this.forceSave);
			} catch (JavaModelException e) {
			}
			this.addDelta(delta);

			if (hasChangedContentForDependents){
				updateAffectedProjects(project.getProject().getFullPath());
			}
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

	public IJavaModelStatus verify() {

		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IJavaProject project = (IJavaProject) getElementToProcess();

		// retrieve classpath 
		IClasspathEntry[] entries = this.newRawPath;
		if (entries == ReuseClasspath){
			try {
				entries = project.getRawClasspath();			
			} catch (JavaModelException e) {
				return e.getJavaModelStatus();
			}
		}		
		// retrieve output location
		IPath outputLocation = this.newOutputLocation;
		if (outputLocation == ReuseOutputLocation){
			try {
				outputLocation = project.getOutputLocation();
			} catch (JavaModelException e) {
				return e.getJavaModelStatus();
			}
		}
		
		// perform validation
		return JavaConventions.validateClasspath(
			project,
			entries,
			outputLocation);
	}

	/**
	 * Update cycle markers
	 */
	protected void updateCycleMarkers() {
		if (!this.canChangeResource) return;

		try {
			IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject[] projects = model.getJavaProjects();
			for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
				try {
					JavaProject project = (JavaProject)projects[i];
					project.flushClasspathProblemMarkers(true);
					if (project.hasClasspathCycle(project.getResolvedClasspath(true))){
						project.createClasspathProblemMarker(
							Util.bind("classpath.cycle"), //$NON-NLS-1$
							IMarker.SEVERITY_ERROR,
							true); 
					}
				} catch (JavaModelException e) {
				}
			}
		} catch (JavaModelException e) {
		}
	}

	/**
	 * Update projects which are affected by this classpath change:
	 * those which refers to the current project as source
	 */
	protected void updateAffectedProjects(IPath prerequisiteProjectPath) {

		try {
			IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
			IJavaProject[] projects = model.getJavaProjects();
			for (int i = 0, projectCount = projects.length; i < projectCount; i++) {
				try {
					JavaProject project = (JavaProject) projects[i];
					// consider ALL dependents (even indirect ones), since they may need to
					// flush their respective namelookup caches (all pkg fragment roots).
					IClasspathEntry[] classpath = project.getExpandedClasspath(true);
					for (int j = 0, entryCount = classpath.length; j < entryCount; j++) {
						IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
							&& entry.getPath().equals(prerequisiteProjectPath)) {
							project.updateClassPath(this.fMonitor, this.canChangeResource, this.mayChangeProjectDependencies);
							break;
						}
					}
				} catch (JavaModelException e) {
				}
			}
		} catch (JavaModelException e) {
		}
		
	}

	/**
	 * Update projects references so that the build order is consistent with the classpath
	 */
	protected void updateProjectReferences(String[] oldRequired, String[] newRequired) {

		try {		
			if (!this.canChangeResource) return;

			JavaProject jproject = ((JavaProject) getElementsToProcess()[0]);
			IProject project = jproject.getProject();
			IProjectDescription description = project.getDescription();
			 
			IProject[] projectReferences = description.getReferencedProjects();
			
			HashSet oldReferences = new HashSet(projectReferences.length);
			for (int i = 0; i < projectReferences.length; i++){
				String projectName = projectReferences[i].getName();
				oldReferences.add(projectName);
			}
			HashSet newReferences = (HashSet)oldReferences.clone();

			for (int i = 0; i < oldRequired.length; i++){
				String projectName = oldRequired[i];
				newReferences.remove(projectName);
			}
			for (int i = 0; i < newRequired.length; i++){
				String projectName = newRequired[i];
				newReferences.add(projectName);
			}

			Iterator iter;
			int newSize = newReferences.size();
			
			checkIdentity: {
				if (oldReferences.size() == newSize){
					iter = newReferences.iterator();
					while (iter.hasNext()){
						if (!oldReferences.contains(iter.next())){
							break checkIdentity;
						}
					}
					return;
				}
			}
			String[] requiredProjectNames = new String[newSize];
			int index = 0;
			iter = newReferences.iterator();
			while (iter.hasNext()){
				requiredProjectNames[index++] = (String)iter.next();
			}
			Util.sort(requiredProjectNames); // ensure that if changed, the order is consistent
			
			IProject[] requiredProjectArray = new IProject[newSize];
			IWorkspaceRoot wksRoot = project.getWorkspace().getRoot();
			for (int i = 0; i < newSize; i++){
				requiredProjectArray[i] = wksRoot.getProject(requiredProjectNames[i]);
			}

			description.setReferencedProjects(requiredProjectArray);
			project.setDescription(description, this.fMonitor);

		} catch(CoreException e){
		}
	}
	public String toString(){
		StringBuffer buffer = new StringBuffer(20);
		buffer.append("SetClasspathOperation\n"); //$NON-NLS-1$
		buffer.append(" - classpath : "); //$NON-NLS-1$
		if (this.newRawPath == ReuseClasspath){
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
		if (this.newOutputLocation == ReuseOutputLocation){
			buffer.append("<Reuse Existing Output Location>"); //$NON-NLS-1$
		} else {
			buffer.append(this.newOutputLocation.toString()); //$NON-NLS-1$
		}
		return buffer.toString();
	}
}
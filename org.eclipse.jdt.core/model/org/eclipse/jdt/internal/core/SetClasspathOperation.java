package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Enumeration;
import java.util.Hashtable;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.compiler.util.ObjectSet;
import org.eclipse.jdt.internal.compiler.util.ObjectVector;

/**
 * This operation sets an <code>IJavaProject</code>'s classpath.
 *
 * @see IJavaProject
 */
public class SetClasspathOperation extends JavaModelOperation {

	IClasspathEntry[] oldExpandedPath;
	IClasspathEntry[] newRawPath;
	boolean canChangeResource;
	boolean forceSave;

	/**
	 * When executed, this operation sets the classpath of the given project.
	 */
	public SetClasspathOperation(
		IJavaProject project,
		IClasspathEntry[] oldExpandedPath,
		IClasspathEntry[] newRawPath,
		boolean canChangeResource,
		boolean forceSave) {

		super(new IJavaElement[] { project });
		this.oldExpandedPath = oldExpandedPath;
		this.newRawPath = newRawPath;
		this.canChangeResource = canChangeResource;
		this.forceSave = forceSave;
	}

	/**
	 * Adds deltas for the given roots, with the specified change flag,
	 * and closes the root. Helper method for #setClasspath
	 */
	protected void addDeltas(
		IPackageFragmentRoot[] roots,
		int flag,
		JavaElementDelta delta) {

		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			delta.changed(root, flag);
			if (flag == IJavaElementDelta.F_REMOVED_FROM_CLASSPATH){
				try {
					root.close();
				} catch (JavaModelException e) {
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
			if (list[i].equals(entry)) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * Sets the classpath of the pre-specified project.
	 */
	protected void executeOperation() throws JavaModelException {

		beginTask(Util.bind("classpath.settingProgress"), 2); //$NON-NLS-1$
		JavaProject project = ((JavaProject) getElementsToProcess()[0]);

		String[] oldRequired = project.getRequiredProjectNames();
		
		project.setRawClasspath0(this.newRawPath);

		// change builder specs to build in the order given by the new classpath
		JavaModelManager manager = project.getJavaModelManager();
		manager.setBuildOrder(
			((JavaModel) project.getJavaModel()).computeBuildOrder(true));

		// flush markers
		project.flushClasspathProblemMarkers();

		// resolve new path (asking for marker creation if problems)
		IClasspathEntry[] newExpandedPath = project.getExpandedClasspath(true, true);

		if (this.oldExpandedPath != null) {
			generateClasspathChangeDeltas(
				this.oldExpandedPath,
				newExpandedPath,
				manager,
				project);
		} else {
			project.saveClasspath(this.forceSave);
			updateAffectedProjects(project.getProject().getFullPath());
		}
		updateProjectReferences(oldRequired, project.getRequiredProjectNames());

		
		done();
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

		boolean hasChangedSourceEntries = false;

		JavaElementDelta delta = new JavaElementDelta(getJavaModel());
		boolean hasDelta = false;
		boolean oldResolvedPathLongest =
			oldResolvedPath.length >= newResolvedPath.length;

		for (int i = 0; i < oldResolvedPath.length; i++) {

			int index = classpathContains(newResolvedPath, oldResolvedPath[i]);
			if (index == -1) {
				IPackageFragmentRoot[] pkgFragmentRoots =
					project.getPackageFragmentRoots(oldResolvedPath[i]);
				addDeltas(pkgFragmentRoots, IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, delta);

				int changeKind = oldResolvedPath[i].getEntryKind();
				hasChangedSourceEntries |= changeKind == IClasspathEntry.CPE_SOURCE;

				// force detach source on jar package fragment roots (source will be lazily computed when needed)
				for (int j = 0, length = pkgFragmentRoots.length; j < length; j++) {
					IPackageFragmentRoot root = pkgFragmentRoots[j];
					if (root instanceof JarPackageFragmentRoot) {
						((JarPackageFragmentRoot) root).setSourceAttachmentProperty(null);// loose info - will be recomputed
					}
				}
				hasDelta = true;

			} else if (
				oldResolvedPathLongest && index != i) { //reordering of the classpath
				addDeltas(
					project.getPackageFragmentRoots(oldResolvedPath[i]),
					IJavaElementDelta.F_CLASSPATH_REORDER,
					delta);
				int changeKind = oldResolvedPath[i].getEntryKind();
				hasChangedSourceEntries |= changeKind == IClasspathEntry.CPE_SOURCE;

				hasDelta = true;
			}
		}

		for (int i = 0; i < newResolvedPath.length; i++) {

			int index = classpathContains(oldResolvedPath, newResolvedPath[i]);
			if (index == -1) {
				addDeltas(
					project.getPackageFragmentRoots(newResolvedPath[i]),
					IJavaElementDelta.F_ADDED_TO_CLASSPATH,
					delta);
				int changeKind = newResolvedPath[i].getEntryKind();
				hasChangedSourceEntries |= changeKind == IClasspathEntry.CPE_SOURCE;
				hasDelta = true;

			} else if (
				!oldResolvedPathLongest && index != i) { //reordering of the classpath
				addDeltas(
					project.getPackageFragmentRoots(newResolvedPath[i]),
					IJavaElementDelta.F_CLASSPATH_REORDER,
					delta);
				int changeKind = newResolvedPath[i].getEntryKind();
				hasChangedSourceEntries |= changeKind == IClasspathEntry.CPE_SOURCE;
				hasDelta = true;
			}
		}
		if (hasDelta) {
			try {
				project.saveClasspath(this.forceSave);
			} catch (JavaModelException e) {
			}
			this.addDelta(delta);
			// loose all built state - next build will be a full one
			manager.setLastBuiltState(project.getProject(), null);

			if (hasChangedSourceEntries)
				updateAffectedProjects(project.getProject().getFullPath());
		}
	}

	/**
	 * Possible failures: <ul>
	 *  <li>NO_ELEMENTS_TO_PROCESS - the project supplied to the operation is
	 * 		<code>null</code>.
	 *	<li>NULL_PATH - the output location path supplied to the operation
	 * 		is <code>null</code>.
	 *	<li>PATH_OUTSIDE_PROJECT - the output location path supplied to the operation
	 * 		is outside of the project supplied to this operation.
	 *	<li>DEVICE_PATH - the path supplied to this operation must not specify a 
	 * 		device
	 *	<li>RELATIVE_PATH - the path supplied to this operation must be
	 *		an absolute path
	 *	<li>INVALID_PATH - the output location cannot overlap any package fragment
	 *		root, except the project folder.
	 *  <li>ELEMENT_DOES_NOT_EXIST - the Java project does not exist
	 * </ul>
	 */
	public IJavaModelStatus verify() {

		IJavaModelStatus status = super.verify();
		if (!status.isOK()) {
			return status;
		}
		IJavaProject javaProject = (IJavaProject) getElementToProcess();

		// retrieve output location
		IPath outputLocation;
		try {
			outputLocation = javaProject.getOutputLocation();
		} catch (JavaModelException e) {
			return e.getJavaModelStatus();
		}

		return JavaConventions.validateClasspath(
			javaProject,
			this.newRawPath,
			outputLocation);
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
					IClasspathEntry[] classpath = project.getResolvedClasspath(true);
					for (int j = 0, entryCount = classpath.length; j < entryCount; j++) {
						IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT
							&& entry.getPath().equals(prerequisiteProjectPath)) {
							project.updateClassPath(this.fMonitor, this.canChangeResource);
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
			
			ObjectSet oldReferences = new ObjectSet(projectReferences.length);
			for (int i = 0; i < projectReferences.length; i++){
				String projectName = projectReferences[i].getName();
				oldReferences.add(projectName);
			}
			ObjectSet newReferences = (ObjectSet)oldReferences.clone();

			for (int i = 0; i < oldRequired.length; i++){
				String projectName = oldRequired[i];
				newReferences.remove(projectName);
			}
			for (int i = 0; i < newRequired.length; i++){
				String projectName = newRequired[i];
				newReferences.add(projectName);
			}
			boolean hasDifferences;
			if (newReferences.size() == oldReferences.size()){
				hasDifferences = false;
				Enumeration enum = oldReferences.elements();
				while (enum.hasMoreElements()){
					String oldName = (String)enum.nextElement();
					if (!newReferences.contains(oldName)){
						hasDifferences = true;
						break;
					}
				}
			} else {
				hasDifferences = true;
			}
			
			if (hasDifferences){
				IProject[] requiredProjectArray = new IProject[newReferences.size()];
				IWorkspaceRoot wksRoot = project.getWorkspace().getRoot();
				Enumeration enum = newReferences.elements();
				int index = 0;
				while (enum.hasMoreElements()){
					String newName = (String)enum.nextElement();
					requiredProjectArray[index++] = wksRoot.getProject(newName);
				}
				description.setReferencedProjects(requiredProjectArray);
				project.setDescription(description, this.fMonitor);
			}
		} catch(CloneNotSupportedException e) {
		} catch(CoreException e){
		}
	}

}
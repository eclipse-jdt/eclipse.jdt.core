package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.*;

/**
 * This operation sets an <code>IJavaProject</code>'s classpath.
 *
 * @see IJavaProject
 */
public class SetClasspathOperation extends JavaModelOperation {
	IClasspathEntry[] oldResolvedPath;	
	IClasspathEntry[] newRawPath;
	boolean saveClasspath;
/**
 * When executed, this operation sets the classpath of the given project.
 */
public SetClasspathOperation(IJavaProject project, IClasspathEntry[] oldResolvedPath, IClasspathEntry[] newRawPath, boolean saveClasspath) {
	super(new IJavaElement[] {project});
	this.oldResolvedPath = oldResolvedPath;
	this.newRawPath = newRawPath;
	this.saveClasspath = saveClasspath;
}
	/**
	 * Adds deltas for the given roots, with the specified change flag,
	 * and closes the root. Helper method for #setClasspath
	 */
	protected void addDeltas(IPackageFragmentRoot[] roots, int flag, JavaElementDelta delta) {
		for (int i= 0; i < roots.length; i++) {
			IPackageFragmentRoot root= roots[i];
			delta.changed(root, flag);
			try {
				root.close();
			} catch (JavaModelException e) {
			}
		}
	}
	/**
	 * Returns the index of the item in the list if the given list contains the specified entry. If the list does
	 * not contain the entry, -1 is returned.
	 * A helper method for #setClasspath
	 */
	protected int classpathContains(IClasspathEntry[] list, IClasspathEntry entry) {
		for (int i= 0; i < list.length; i++) {
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
	JavaProject project= ((JavaProject) getElementsToProcess()[0]);
	
	project.setRawClasspath0(this.newRawPath);

	// change builder specs to build in the order given by the new classpath
	JavaModelManager manager = project.getJavaModelManager();
	manager.setBuildOrder(((JavaModel) project.getJavaModel()).computeBuildOrder(true));

	// flush markers
	project.flushClasspathProblemMarkers();

	// resolve new path (asking for marker creation if problems)
	IClasspathEntry[] newResolvedPath = project.getResolvedClasspath(true, true);
	
	if (this.oldResolvedPath != null) {
		generateClasspathChangeDeltas(this.oldResolvedPath, newResolvedPath, manager, project);
	} else {
		project.saveClasspath(this.saveClasspath);		
	}

	done();
}
	/**
	 * Generates the delta of removed/added/reordered roots.
	 * Use three deltas in case the same root is removed/added/reordered (i.e. changed from
	 * K_SOURCE to K_BINARY or visa versa)
	 */
	protected void generateClasspathChangeDeltas(IClasspathEntry[] oldResolvedPath, IClasspathEntry[] newResolvedPath, JavaModelManager manager, JavaProject project) {

		boolean hasChangedSourceEntries = false;
		
		JavaElementDelta delta= new JavaElementDelta(getJavaModel());
		boolean hasDelta = false;
		boolean oldResolvedPathLongest= oldResolvedPath.length >= newResolvedPath.length;
		for (int i= 0; i < oldResolvedPath.length; i++) {
			int index= classpathContains(newResolvedPath, oldResolvedPath[i]);
			if (index == -1) {
				IPackageFragmentRoot[] pkgFragmentRoots = project.getPackageFragmentRoots(oldResolvedPath[i]);
				addDeltas(pkgFragmentRoots, IJavaElementDelta.F_REMOVED_FROM_CLASSPATH, delta);
				hasChangedSourceEntries |= oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE;
				// force detach source on jar package fragment roots (source will be lazily computed when needed)
				for (int j = 0, length = pkgFragmentRoots.length; j < length; j++) {
					IPackageFragmentRoot root = pkgFragmentRoots[j];
					if (root instanceof JarPackageFragmentRoot) {
						JarPackageFragmentRoot jarRoot = (JarPackageFragmentRoot)root;
						try {
							jarRoot.getWorkspace().getRoot().setPersistentProperty(jarRoot.getSourceAttachmentPropertyName(), null); // loose info - will be recomputed
						} catch(CoreException ce){
						}
					}
				}
				
				hasDelta = true;
			} else if (oldResolvedPathLongest && index != i) { //reordering of the classpath
				addDeltas(project.getPackageFragmentRoots(oldResolvedPath[i]), IJavaElementDelta.F_CLASSPATH_REORDER, delta);
				hasChangedSourceEntries |= oldResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE;
				hasDelta = true;
			}
		}

		for (int i= 0; i < newResolvedPath.length; i++) {
			int index= classpathContains(oldResolvedPath, newResolvedPath[i]);
			if (index == -1) {
				addDeltas(project.getPackageFragmentRoots(newResolvedPath[i]), IJavaElementDelta.F_ADDED_TO_CLASSPATH, delta);
				hasChangedSourceEntries |= newResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE;
				hasDelta = true;
			} else if (!oldResolvedPathLongest && index != i) { //reordering of the classpath
				addDeltas(project.getPackageFragmentRoots(newResolvedPath[i]), IJavaElementDelta.F_CLASSPATH_REORDER, delta);
				hasChangedSourceEntries |= newResolvedPath[i].getEntryKind() == IClasspathEntry.CPE_SOURCE;				
				hasDelta = true;
			}
		}
		if (hasDelta) {
			try {
				project.saveClasspath(this.saveClasspath);
			} catch(JavaModelException e){
			}
			this.addDelta(delta);
			// loose all built state - next build will be a full one
			manager.setLastBuiltState(project.getProject(), null);

			if (hasChangedSourceEntries) updateAffectedProjects(project.getProject().getFullPath());
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
	IJavaProject javaProject = (IJavaProject)getElementToProcess();

	// retrieve output location
	IPath outputLocation;
	try {
		outputLocation = javaProject.getOutputLocation();
	} catch(JavaModelException e){
		return e.getJavaModelStatus();
	}

	return JavaConventions.validateClasspath(javaProject, this.newRawPath, outputLocation);
}

/**
 * Update projects which are affected by this classpath change:
 * those which refers to the current project as source
 */
protected void updateAffectedProjects(IPath prerequisiteProjectPath){

	try {
		IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		IJavaProject[] projects = model.getJavaProjects();
		for (int i = 0, projectCount = projects.length; i < projectCount; i++){
			try {
				JavaProject project = (JavaProject)projects[i];
				IClasspathEntry[] classpath = project.getRawClasspath();
				for (int j =0, entryCount = classpath.length; j < entryCount; j++){
					IClasspathEntry entry = classpath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT 
						&& entry.getPath().equals(prerequisiteProjectPath)){
							project.updateClassPath();
							break;
						}
				}
			} catch(JavaModelException e){
			}
		}
	} catch(JavaModelException e){
	}
}
}

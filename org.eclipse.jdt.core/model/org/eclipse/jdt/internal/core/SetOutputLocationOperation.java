package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import org.eclipse.jdt.core.*;

import java.io.File;
import java.util.*;

/**
 * This operation sets an <code>IJavaProject</code>'s output location.
 *
 * @see IJavaProject
 */
public class SetOutputLocationOperation extends JavaModelOperation {
	/**
	 * The new output location for the Java project
	 */
	protected IPath fOutputLocation;
/**
 * When executed, this operation sets the output location of the given project.
 * The output location is where the builder writes <code>.class</code> files.
 */
public SetOutputLocationOperation(IJavaProject project, IPath outputLocation) {
	super(new IJavaElement[] {project});
	fOutputLocation = outputLocation;
}
/**
 * Recursively adds all subfolders of <code>folder</code> to the given collection.
 */
protected void collectAllSubfolders(IFolder folder, Vector collection) throws JavaModelException {
	try {
		IResource[] members= folder.members();
		for (int i = 0, max = members.length; i < max; i++) {
			IResource r= members[i];
			if (r.getType() == IResource.FOLDER) {
				collection.addElement(r);
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
protected Vector determineAffectedPackageFragments(IPath location) throws JavaModelException {
	Vector fragments = new Vector();
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
		IClasspathEntry[] classpath = project.getResolvedClasspath(true);
		for (int i = 0; i < classpath.length; i++) {
			IClasspathEntry entry = classpath[i];
			IPath path = classpath[i].getPath();
			if (entry.getEntryKind() != IClasspathEntry.CPE_PROJECT && path.isPrefixOf(location) && !path.equals(location)) {
				IPackageFragmentRoot[] roots = project.getPackageFragmentRoots(classpath[i]);
				IPackageFragmentRoot root = roots[0];
				// now the output location becomes a package fragment - along with any subfolders
				Vector folders = new Vector();
				folders.addElement(folder);
				collectAllSubfolders(folder, folders);
				Enumeration elements = folders.elements();
				int segments = path.segmentCount();
				while (elements.hasMoreElements()) {
					IFolder f = (IFolder) elements.nextElement();
					IPath relativePath = f.getFullPath().removeFirstSegments(segments);
					String name = relativePath.toOSString();
					name = name.replace(File.pathSeparatorChar, '.');
					if (name.endsWith("."/*nonNLS*/)) {
						name = name.substring(0, name.length() - 1);
					}
					IPackageFragment pkg = root.getPackageFragment(name);
					fragments.addElement(pkg);
				}
			}
		}
	}
	return fragments;
}
/**
 * Sets the output location of the pre-specified project.
 *
 * <p>This can cause changes in package fragments - i.e. if the
 * old and new output location folder could be considered as
 * a package fragment.
 */
protected void executeOperation() throws JavaModelException {
	beginTask(Util.bind("classpath.settingOutputLocationProgress"/*nonNLS*/), 2);
	JavaProject project= ((JavaProject) getElementsToProcess()[0]);
	
	IPath oldLocation= project.getOutputLocation();
	IPath newLocation= fOutputLocation;

	// see if this will cause any package fragments to be added
	boolean deltaToFire= false;
	JavaElementDelta delta = newJavaElementDelta();
	Vector added= determineAffectedPackageFragments(oldLocation);
	Enumeration pkgs= added.elements();
	while (pkgs.hasMoreElements()) {
		IPackageFragment frag= (IPackageFragment)pkgs.nextElement();
		((IPackageFragmentRoot)frag.getParent()).close();
		delta.added(frag);
		deltaToFire = true;
	}

	// see if this will cause any package fragments to be removed
	Vector removed= determineAffectedPackageFragments(newLocation);
	pkgs= removed.elements();
	while (pkgs.hasMoreElements()) {
		IPackageFragment frag= (IPackageFragment)pkgs.nextElement();
		((IPackageFragmentRoot)frag.getParent()).close();
		delta.removed(frag);
		deltaToFire = true;
	}
	
	project.setOutputLocation0(fOutputLocation);
	if (deltaToFire) {
		addDelta(delta);	
	}
	worked(1);
	project.saveClasspath();
	worked(1);

	// loose all built state - next build will be a full one
	JavaModelManager.getJavaModelManager().setLastBuiltState(project.getProject(), null);
	done();
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
	// retrieve classpath
	IClasspathEntry[] classpath = null;
	IJavaProject javaProject= (IJavaProject)getElementToProcess();
	IPath projectPath= javaProject.getProject().getFullPath();	
	try {
		classpath = javaProject.getResolvedClasspath(true);
	} catch (JavaModelException e) {
		return e.getJavaModelStatus();
	}
	return JavaConventions.validateClasspath((IJavaProject) fElementsToProcess[0], classpath, fOutputLocation);
}
}

package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.eclipse.jdt.internal.core.search.indexing.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class is used by <code>JavaModelManager</code> to convert
 * <code>IResourceDelta</code>s into <code>IJavaElementDelta</code>s.
 * It also does some processing on the <code>JavaElement</code>s involved
 * (e.g. closing them or updating classpaths).
 */
public class DeltaProcessor {
	
	static final IClasspathEntry[] UNKNOWN_CLASSPATH = new IClasspathEntry[] {};
	
	/**
	 * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	protected JavaElementDelta fCurrentDelta;

	/**
	 * JavaProjects that need classpaths updated when resource delta
	 * translation is complete.
	 */
	protected Hashtable fJavaProjectsToUpdate = null;

	protected IndexManager indexManager =
		JavaModelManager.ENABLE_INDEXING ? new IndexManager() : null;

	public static boolean VERBOSE = false;

	/**
	 * Adds the given child handle to its parent's cache of children. 
	 */
	protected void addToParentInfo(Openable child) {

		Openable parent = (Openable) child.getParent();
		if (parent != null && parent.isOpen()) {
			try {
				JavaElementInfo info = parent.getElementInfo();
				info.addChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}

	/**
	 * Generic processing for an element that has been added:<ul>
	 * <li>The element is added to its parent's cache of children
	 * <li>The element is closed (to ensure consistency)
	 * <li>An entry is made in the delta reporting it as added (ADDED).
	 * </ul>
	 * <p>
	 * If the element is an archive, and it has just been added, it may not be specified
	 * on the project's classpath. In this case, the new element is not added as a child
	 * of its parent (since only package fragment roots on the classpath are considered to
	 * be children of a project).
	 */
	protected void basicElementAdded(Openable element, IResourceDelta delta) {

		if (isOpen(delta.getResource())) {

			addToParentInfo(element);
			switch (element.getElementType()) {
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					// when a root is added, and is on the classpath, the project must be updated
					JavaProject project = (JavaProject) element.getJavaProject();
					updateProject(project);
					//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
					} catch (JavaModelException e) {
					}
					break;
				case IJavaElement.PACKAGE_FRAGMENT :
					//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
					project = (JavaProject) element.getJavaProject();
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
					} catch (JavaModelException e) {
					}
					break;
			}

			close(element);
			fCurrentDelta.added(element);
		}
	}

	/**
	 * Check whether the updated file is affecting some of the properties of a given project (like
	 * its classpath persisted as a file).
	 * 
	 * NOTE: It can induce resource changes, and cannot be called during POST_CHANGE notification.
	 *
	 */
	public static void checkProjectPropertyFileUpdate(
		IResourceDelta delta,
		IJavaElement parent) {

		IResource resource = delta.getResource();
		IJavaElement element = JavaCore.create(resource);

		boolean processChildren = false;

		switch (resource.getType()) {

			case IResource.ROOT :
				processChildren = true;
				break;
			case IResource.PROJECT :
				try {
					if (((IProject) resource).hasNature(JavaCore.NATURE_ID)) {
						processChildren = true;
					}
				} catch (CoreException e) {
				}
				break;
			case IResource.FILE :
				if (parent.getElementType() == IJavaElement.JAVA_PROJECT) {
					IFile file = (IFile) resource;
					JavaProject project = (JavaProject) parent;

					/* check classpath property file change */
					QualifiedName classpathProp;
					if (file.getName().equals(
							project.computeSharedPropertyFileName(
								classpathProp = project.getClasspathPropertyName()))) {

						switch (delta.getKind()) {
							case IResourceDelta.REMOVED : // recreate one based on in-memory path
								try {
									project.saveClasspath(false);
								} catch (JavaModelException e) {
								}
								break;
							case IResourceDelta.CHANGED :
								if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
									break; // only consider content change
							case IResourceDelta.ADDED :
								// check if any actual difference
								IPath oldOutputLocation = null;
								try {
									oldOutputLocation = project.getOutputLocation();
									// force to (re)read the property file
									String fileClasspathString = project.getSharedProperty(classpathProp);
									if (fileClasspathString == null)
										break; // did not find the file
									IClasspathEntry[] fileEntries = project.readPaths(fileClasspathString);
									if (fileEntries == null)
										break; // could not read, ignore 
									if (project.isClasspathEqualsTo(fileEntries))
										break;

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
									if (outputLocation != null) {
										project.setOutputLocation0(outputLocation);
									}
									try {
										project.setRawClasspath(fileEntries, null, false);
									} catch (JavaModelException e) { // undo output location change
										project.setOutputLocation0(oldOutputLocation);
									}
								} catch (IOException e) {
									break;
								} catch (RuntimeException e) {
									break;
								} catch (CoreException e) {
									break;
								}

						}
					}
				}
				break;
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				checkProjectPropertyFileUpdate(children[i], element);
			}
		}
	}

	/**
	 * Clears the caches related to classpath updates.
	 */
	protected void clearState() {

		fJavaProjectsToUpdate = null;
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
	 * Traverse an existing delta and close the affected compilation units.
	 */
	protected void closeAffectedElements(IResourceDelta delta) {

		Openable element = (Openable) JavaCore.create(delta.getResource());
		boolean processChildren = true;
		if (element != null) {
			int flags = delta.getFlags();
			switch (element.getElementType()) {
				case IJavaElement.CLASS_FILE :
				case IJavaElement.COMPILATION_UNIT :
					processChildren = false;
					switch (delta.getKind()) {
						case IResourceDelta.ADDED :
							break;
						case IResourceDelta.CHANGED :
							if ((flags & IResourceDelta.CONTENT) != 0) {
								try {
									element.close();
								} catch (JavaModelException e) {
								}
							}
							break;
						case IResourceDelta.REMOVED :
							try {
								element.close();
							} catch (JavaModelException e) {
							}
					}
			}
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			for (int i = 0; i < children.length; i++) {
				closeAffectedElements(children[i]);
			}
		}
	}

	/**
	 * Generic processing for elements with changed contents:<ul>
	 * <li>The element is closed such that any subsequent accesses will re-open
	 * the element reflecting its new structure.
	 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
	 * </ul>
	 */
	protected void contentChanged(Openable element, IResourceDelta delta) {

		close(element);
		fCurrentDelta.changed(element, IJavaElementDelta.F_CONTENT);
	}

	/**
	 * Creates the openables corresponding to this resource.
	 * Returns null if none was found.
	 * In general, there is only one openable corresponding to a resource,
	 * except for jar and zip files that can correspond to one or more
	 * JarPackageFragmentRoots.
	 */
	protected Openable[] createElements(IResource resource) {

		if (resource == null)
			return null;
		String extension = resource.getFileExtension();
		extension = extension == null ? null : extension.toLowerCase();
		if ("jar".equals(extension) //$NON-NLS-1$
			|| "zip".equals(extension)) { //$NON-NLS-1$ 
			IJavaProject[] projects = null;
			try {
				projects =
					JavaModelManager.getJavaModel(resource.getWorkspace()).getJavaProjects();
			} catch (JavaModelException e) {
				return null;
			}
			Vector jars = new Vector();
			for (int i = 0, length = projects.length; i < length; i++) {
				IJavaProject project = projects[i];
				// Create a jar package fragment root only if on the classpath
				IPath resourcePath = resource.getFullPath();
				try {
					IClasspathEntry[] entries = project.getResolvedClasspath(true);
					for (int j = 0, length2 = entries.length; j < length2; j++) {
						IClasspathEntry entry = entries[j];
						IPath rootPath = entry.getPath();
						if (rootPath.equals(resourcePath)) {
							jars.add(project.getPackageFragmentRoot((IFile) resource));
						}
					}
				} catch (JavaModelException e) {
				}
			}
			int size = jars.size();
			if (size == 0)
				return null;
			Openable[] result = new Openable[size];
			jars.copyInto(result);
			return result;
		} else {
			Openable element = (Openable) JavaCore.create(resource);
			if (element == null) {
				return null;
			} else {
				return new Openable[] { element };
			}
		}
	}

	/**
	 * Processing for an element that has been added:<ul>
	 * <li>If the element is a project, do nothing, and do not process
	 * children, as when a project is created it does not yet have any
	 * natures - specifically a java nature.
	 * <li>If the elemet is not a project, process it as added (see
	 * <code>basicElementAdded</code>.
	 * </ul>
	 */
	protected void elementAdded(Openable element, IResourceDelta delta) {

		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			// project add is handled by JavaProject.configure() because
			// when a project is created, it does not yet have a java nature
			if (hasJavaNature(delta.getResource())) {
				basicElementAdded(element, delta);
			}
		} else {
			basicElementAdded(element, delta);
		}
	}

	/**
	 * Processing for the closing of an element - there are two cases:<ul>
	 * <li>when a project is closed (in the platform sense), the
	 * 		JavaModel reports this as if the JavaProject has been removed.
	 * <li>otherwise, the JavaModel reports this
	 *		as a the element being closed (CHANGED + F_CLOSED).
	 * </ul>
	 * <p>In both cases, the children of the element are not processed. When
	 * a resource is closed, the platform reports all children as removed. This
	 * would effectively delete the classpath if we processed children.
	 */
	protected void elementClosed(Openable element, IResourceDelta delta) {

		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			// treat project closing as removal
			elementRemoved(element, delta);
		} else {
			removeFromParentInfo(element);
			close(element);
			fCurrentDelta.closed(element);
		}
	}

	/**
	 * Processing for the opening of an element - there are two cases:<ul>
	 * <li>when a project is opened (in the platform sense), the
	 * 		JavaModel reports this as if the JavaProject has been added.
	 * <li>otherwise, the JavaModel reports this
	 *		as a the element being opened (CHANGED + F_CLOSED).
	 * </ul>
	 */
	protected void elementOpened(Openable element, IResourceDelta delta) {

		if (element.getElementType() == IJavaElement.JAVA_PROJECT) {
			// treat project opening as addition
			if (hasJavaNature(delta.getResource())) {
				basicElementAdded(element, delta);
			}
		} else {
			addToParentInfo(element);
			fCurrentDelta.opened(element);
		}
	}

	/**
	 * Generic processing for a removed element:<ul>
	 * <li>Close the element, removing its structure from the cache
	 * <li>Remove the element from its parent's cache of children
	 * <li>Add a REMOVED entry in the delta
	 * </ul>
	 */
	protected void elementRemoved(Openable element, IResourceDelta delta) {
		
		close(element);
		removeFromParentInfo(element);
		fCurrentDelta.removed(element);

		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT :
				JavaModelManager.getJavaModelManager().removePerProjectInfo(
					(JavaProject) element);
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				updateProject(element.getJavaProject()); // to trigger deltas
				JavaProject project = (JavaProject) element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null);
				} catch (JavaModelException e) {
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT :
				//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
				project = (JavaProject) element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null);
				} catch (JavaModelException e) {
				}
				break;
			case IJavaElement.JAVA_MODEL :
				element.getJavaModelManager().getIndexManager().reset();
				element.getJavaModelManager().fModelInfo = null;
				break;
		}
	}

	/**
	 * Filters the generated <code>JavaElementDelta</code>s to remove those
	 * which should not be fired (because they don't represent a real change
	 * in the Java Model).
	 */
	protected IJavaElementDelta[] filterRealDeltas(IJavaElementDelta[] deltas) {

		IJavaElementDelta[] realDeltas = new IJavaElementDelta[deltas.length];
		int index = 0;
		for (int i = 0; i < deltas.length; i++) {
			IJavaElementDelta delta = deltas[i];
			if (delta == null) {
				continue;
			}
			if (delta.getAffectedChildren().length > 0
				|| delta.getKind() != IJavaElementDelta.CHANGED
				|| (delta.getFlags() & IJavaElementDelta.F_CLOSED) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_OPENED) != 0) {

				realDeltas[index++] = delta;
			}
		}
		IJavaElementDelta[] result = new IJavaElementDelta[index];
		if (result.length > 0) {
			System.arraycopy(realDeltas, 0, result, 0, result.length);
		}
		return result;
	}

	/**
	 * Returns true if the given resource is contained in an open project
	 * with a java nature, otherwise false.
	 */
	protected boolean hasJavaNature(IResource resource) {

		// ensure the project has a java nature (if open)
		IProject project = resource.getProject();
		if (project.isOpen()) {
			try {
				return project.hasNature(JavaCore.NATURE_ID);
			} catch (CoreException e) {
				// do nothing
			}
		}
		return false;
	}

	/**
	 * Returns true if the given resource is considered open (in the
	 * platform sense), otherwise false.
	 */
	protected boolean isOpen(IResource resource) {

		IProject project = resource.getProject();
		if (project == null) {
			return true; // workspace is always open
		} else {
			return project.isOpen();
		}
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

		JavaElementInfo info = element.getElementInfo();
		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT :
				IResource resource = delta.getResource();
				((JavaProjectElementInfo) info).setNonJavaResources(null);

				// if a package fragment root is the project, clear it too
				PackageFragmentRoot projectRoot =
					(PackageFragmentRoot) ((JavaProject) element).getPackageFragmentRoot(
						new Path(IPackageFragment.DEFAULT_PACKAGE_NAME));
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
		};

		JavaElementDelta elementDelta = fCurrentDelta.find(element);
		if (elementDelta == null) {
			fCurrentDelta.changed(element, IJavaElementDelta.F_CONTENT);
			elementDelta = fCurrentDelta.find(element);
		}
		elementDelta.addResourceDelta(delta);
	}

	/**
	 * Converts a <code>IResourceDelta</code> rooted in a <code>Workspace</code> into
	 * the corresponding set of <code>IJavaElementDelta</code>, rooted in the
	 * relevant <code>JavaModel</code>s.
	 */
	public IJavaElementDelta[] processResourceDelta(IResourceDelta changes) {

		// clear state
		clearState();

		// get the workspace delta, and start processing there.
		IResourceDelta[] deltas = changes.getAffectedChildren();
		IJavaElementDelta[] translatedDeltas = new JavaElementDelta[deltas.length];
		for (int i = 0; i < deltas.length; i++) {
			IResourceDelta delta = deltas[i];
			JavaModel model =
				JavaModelManager.getJavaModel(delta.getResource().getWorkspace());
			if (model != null) {
				fCurrentDelta = new JavaElementDelta(model);
				traverseDelta(delta, UNKNOWN_CLASSPATH); // traverse delta
				translatedDeltas[i] = fCurrentDelta;
			}
		}
		// update classpaths
		updateClasspaths(false);

		// clear state
		clearState();

		return filterRealDeltas(translatedDeltas);
	}
	
/*
 * Update the current delta (ie. add/remove/change the given element) and update the correponding index.
 * Returns whether the children of the given delta must be processed.
 */
private boolean updateCurrentDeltaAndIndex(Openable element, IResourceDelta delta) {
	switch (delta.getKind()) {
		case IResourceDelta.ADDED :
			updateIndex(element, delta);
			elementAdded(element, delta);
			if (element instanceof IPackageFragmentRoot) {
				element = (Openable)((IPackageFragmentRoot)element).getPackageFragment("");
			}
			if (element instanceof IPackageFragment) {
				// add subpackages
				PackageFragmentRoot root = element.getPackageFragmentRoot();
				String name = element.getElementName();
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++) {
					IResourceDelta child = children[i];
					IResource resource = child.getResource();
					if (resource instanceof IFolder) {
						String subpkgName = 
							name.length() == 0 ? 
								resource.getName() : 
								name + "." + resource.getName(); //$NON-NLS-1$
						Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
						this.updateCurrentDeltaAndIndex(subpkg, child);
					}
				}
			}
			return false;
		case IResourceDelta.REMOVED :
			updateIndex(element, delta);
			elementRemoved(element, delta);
			if (element instanceof IPackageFragmentRoot) {
				element = (Openable)((IPackageFragmentRoot)element).getPackageFragment("");
			}
			if (element instanceof IPackageFragment) {
				// remove subpackages
				PackageFragmentRoot root = element.getPackageFragmentRoot();
				String name = element.getElementName();
				IResourceDelta[] children = delta.getAffectedChildren();
				for (int i = 0, length = children.length; i < length; i++) {
					IResourceDelta child = children[i];
					IResource resource = child.getResource();
					if (resource instanceof IFolder) {
						String subpkgName = 
							name.length() == 0 ? 
								resource.getName() : 
								name + "." + resource.getName(); //$NON-NLS-1$
						Openable subpkg = (Openable)root.getPackageFragment(subpkgName);
						this.updateCurrentDeltaAndIndex(subpkg, child);
					}
				}
			}
			return false;
		case IResourceDelta.CHANGED :
			int flags = delta.getFlags();
			if ((flags & IResourceDelta.CONTENT) != 0) {
				updateIndex(element, delta);
				contentChanged(element, delta);
			} else if ((flags & IResourceDelta.OPEN) != 0) {
				updateIndex(element, delta);
				if (isOpen(delta.getResource())) {
					elementOpened(element, delta);
				} else {
					elementClosed(element, delta);
				}
				return false; // when a project is open/closed don't process children
			}
			return true;
	}
	return true;
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
				JavaElementInfo info = parent.getElementInfo();
				info.removeChild(child);
			} catch (JavaModelException e) {
				// do nothing - we already checked if open
			}
		}
	}

	/**
	 * Reset the non-java resources collection for package fragment roots of the
	 * corresponding project.
	 */
	protected void resetNonJavaResourcesForPackageFragmentRoots(JavaProject project)
		throws JavaModelException {

		IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
		if (roots == null)
			return;
		for (int i = 0, max = roots.length; i < max; i++) {
			IPackageFragmentRoot root = roots[i];
			IResource res = root.getUnderlyingResource();
			if (res != null) {
				((PackageFragmentRoot) root).resetNonJavaResources();
			}
		}
	}

	/**
	 * Converts an <code>IResourceDelta</code> and its children into
	 * the corresponding <code>IJavaElementDelta</code>s.
	 * The classpath is passed along. If it is null, the resource
	 * is already known to be on the classpath, if it is UNKNOWN_CLASSPATH, then 
	 * it will be computed if the resource corresponds to a Java project.
	 * Return whether the delta corresponds to a resource on the classpath.
	 */
	protected boolean traverseDelta(IResourceDelta delta, IClasspathEntry[] classpath) {

		IResource res = delta.getResource();
		boolean isOnClasspath = this.isOnClasspath(classpath, res);
		
		Openable element = null;
		boolean processChildren = true;
		JavaProject project = null;
		if (isOnClasspath) {
			Openable[] elements = this.createElements(res);
			if (elements != null) {
				for (int i = 0, length = elements.length; i < length; i++) {
					element = elements[i];
					processChildren = this.updateCurrentDeltaAndIndex(element, delta);
				}
			} else {
				return false;
			}
		} else {
			if (res instanceof IProject) {
				project = (JavaProject)JavaCore.getJavaCore().create((IProject)res);
				if (project == null) return false; // not a Java project
				processChildren = this.updateCurrentDeltaAndIndex(project, delta);
				if (delta.getKind() != IResourceDelta.CHANGED 
						|| (delta.getFlags() & IResourceDelta.OPEN) != 0) {
					return false; // don't go deeper for added, removed, opened or closed projects
				}
				try {
					classpath = project.getExpandedClasspath(true);
				} catch (JavaModelException e) {
				}
			} else {
				// if classpath is known, we are for sure out of classpath: stop processing children
				processChildren = classpath != null;
			}
		}
		if (processChildren) {
			IResourceDelta[] children = delta.getAffectedChildren();
			boolean oneChildOnClasspath = false;
			int length = children.length;
			IResourceDelta[] orphanChildren = new IResourceDelta[length];
			for (int i = 0; i < length; i++) {
				IResourceDelta child = children[i];
				if (!traverseDelta(child, isOnClasspath ? null : classpath)) {
					try {
						if (isOnClasspath) { 
							// add child as non java resource if current element on classpath
							nonJavaResourcesChanged(element, child);
						} else {
							orphanChildren[i] = child;
						}
					} catch (JavaModelException e) {
					}
				} else {
					oneChildOnClasspath = true;
				}
			}
			if (oneChildOnClasspath || project != null) {
				// add orphan children (case of non java resources under project)
				if (project == null) {
					project = (JavaProject)JavaCore.getJavaCore().create(res.getProject());
				}
				for (int i = 0; i < length; i++) {
					if (orphanChildren[i] != null) {
						try {
							nonJavaResourcesChanged(project, orphanChildren[i]);
						} catch (JavaModelException e) {
						}
					}
				}
			} // else resource delta will be added by parent
			return isOnClasspath || oneChildOnClasspath;
		} else {
			return isOnClasspath && element != null; // element is null if non-java resource in package
		}
	}
	
private boolean isOnClasspath(IClasspathEntry[] classpath, IResource res) {
	IPath path = res.getFullPath();
	if (classpath == null) {
		return true;
	} else if (classpath == UNKNOWN_CLASSPATH) {
		return false;
	} else {
		for (int i = 0, length = classpath.length; i < length; i++) {
			if (classpath[i].getPath().isPrefixOf(path)) {
				return true;
			}
		}
		return false;
	}
}

	/**
	 * Updates the classpath of each project requiring update. This refreshes
	 * the cached info in each project's namelookup facility, and persists
	 * classpaths.
	 */
	protected void updateClasspaths(boolean canChangeResource) {

		if (fJavaProjectsToUpdate != null) {
			Enumeration projects = fJavaProjectsToUpdate.elements();
			while (projects.hasMoreElements()) {
				JavaProject project = (JavaProject) projects.nextElement();
				try {
					project.updateClassPath(null, canChangeResource);
					if (canChangeResource)
						project.saveClasspath(false);
				} catch (JavaModelException e) {
				}
			}
		}

	}

protected void updateIndex(Openable element, IResourceDelta delta) {

	try {		
		if (indexManager == null)
			return;

		switch (element.getElementType()) {
			case IJavaElement.JAVA_PROJECT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED :
					case IResourceDelta.OPEN :
						indexManager.indexAll(element.getJavaProject().getProject());
						break;
				}
				break;
			case IJavaElement.PACKAGE_FRAGMENT_ROOT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.CHANGED:
						if (element instanceof JarPackageFragmentRoot) {
							JarPackageFragmentRoot root = (JarPackageFragmentRoot)element;
							// index jar file only once (if the root is in its declaring project)
							if (root.getJavaProject().getProject().getFullPath().isPrefixOf(root.getPath())) {
								indexManager.indexJarFile(root.getPath(), root.getJavaProject().getElementName());
							}
						}
						break;
					case IResourceDelta.REMOVED:
						// keep index in case it is added back later in this session
						break;
				}
				// don't break as packages of the package fragment root can be indexed below
			case IJavaElement.PACKAGE_FRAGMENT :
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
					case IResourceDelta.REMOVED:
						IPackageFragment pkg = null;
						if (element instanceof IPackageFragmentRoot) {
							IPackageFragmentRoot root = (IPackageFragmentRoot)element;
							pkg = root.getPackageFragment("");
						} else {
							pkg = (IPackageFragment)element;
						}
						String name = pkg.getElementName();
						IResourceDelta[] children = delta.getAffectedChildren();
						for (int i = 0, length = children.length; i < length; i++) {
							IResourceDelta child = children[i];
							IResource resource = child.getResource();
							if (resource instanceof IFile) {
								String extension = resource.getFileExtension();
								if ("java".equalsIgnoreCase(extension)) { //$NON-NLS-1$
									Openable cu = (Openable)pkg.getCompilationUnit(resource.getName());
									this.updateIndex(cu, child);
								} else if ("class".equalsIgnoreCase(extension)) { //$NON-NLS-1$
									Openable classFile = (Openable)pkg.getClassFile(resource.getName());
									this.updateIndex(classFile, child);
								}
							}
						}
						break;
				}
				break;
			case IJavaElement.CLASS_FILE :
				IFile file = (IFile) element.getUnderlyingResource();
				IJavaProject project = element.getJavaProject();
				IResource binaryFolder;
				try {
					binaryFolder = element.getPackageFragmentRoot().getUnderlyingResource();
					// if the class file is part of the binary output, it has been created by
					// the java builder -> ignore
					if (binaryFolder.getFullPath().equals(project.getOutputLocation())) {
						break;
					}
				} catch (JavaModelException e) {
					break;
				}
				switch (delta.getKind()) {
					case IResourceDelta.CHANGED :
						// no need to index if the content has not changed
						if ((delta.getFlags() & IResourceDelta.CONTENT) == 0)
							break;
					case IResourceDelta.ADDED :
						if (file.isLocal(IResource.DEPTH_ZERO))
							indexManager.add(file, binaryFolder);
						break;
					case IResourceDelta.REMOVED :
						indexManager.remove(file.getFullPath().toString(), binaryFolder);
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
						if (file.isLocal(IResource.DEPTH_ZERO))
							indexManager.add(file, file.getProject());
						break;
					case IResourceDelta.REMOVED :
						indexManager.remove(file.getFullPath().toString(), file.getProject());
						break;
				}
		}
	} catch (CoreException e) {
		// ignore: index won't be updated
	}
}

	/**
	 * Adds the given project to the cache of projects requiring classpath
	 * updates when delta translation is complete.
	 */
	protected void updateProject(IJavaProject project) {

		if (fJavaProjectsToUpdate == null) {
			fJavaProjectsToUpdate = new Hashtable(2);
		}
		fJavaProjectsToUpdate.put(project, project);
	}
}
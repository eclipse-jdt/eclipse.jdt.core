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

import org.eclipse.jdt.core.IClasspathEntry;
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
	/**
	 * The <code>JavaElementDelta</code> corresponding to the <code>IResourceDelta</code> being translated.
	 */
	protected JavaElementDelta fCurrentDelta;

	/**
	 * JavaProjects that need classpaths updated when resource delta
	 * translation is complete.
	 */
	protected Hashtable fJavaProjectsToUpdate = null;

	/**
	 * PackageFragmentRoots that have been removed. When delta translation
	 * is compelte, these roots are removed from classpaths.
	 */
	protected Vector fRootsRemoved = null;

	/**
	 * Flag can be set to avoid processing children of current element
	 */
	protected boolean fProcessChildren = true;
	 
	protected IndexManager indexManager = JavaModelManager.ENABLE_INDEXING ? new IndexManager() : null;
/**
 * Inserts the new classpath entry after the specified entry, in the give
 * projects info. This is done such that resources in this root can be
 * translated into JavaElement by the factory. The classpath is not persisted
 * until delta translation is complete.
 */
protected void addClasspathEntry(JavaProject project, IClasspathEntry after, IClasspathEntry newEntry) {
	try {
		IClasspathEntry[] cp = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[cp.length + 1];
		int j = 0;
		for (int i = 0; i < cp.length; i++) {
			newPath[j] = cp[i];
			if (cp[i].equals(after)) {
				j++;
				newPath[j] = newEntry;
			}
			j++;
		}
		((JavaProjectElementInfo)project.getElementInfo()).setRawClasspath(newPath);
		resetNonJavaResourcesForPackageFragmentRoots(project);
	} catch (JavaModelException e) {
		// nothing
	}

}
/**
 * Adds the given child handle to its parent's cache of children. 
 */
protected void addToParentInfo(Openable child) {
	Openable parent = (Openable)child.getParent();
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
		boolean onClasspath = isOnClasspath(element);

		// only add as a child if it is on the classpath
		if (onClasspath) {
			addToParentInfo(element);
			switch (element.getElementType()) {
				case IJavaElement.PACKAGE_FRAGMENT_ROOT :
					// when a root is added, and is on the classpath, the project must be updated
					JavaProject project = (JavaProject)element.getJavaProject();
					updateProject(project);
					//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
					} catch(JavaModelException e){
					}
					break;
				case IJavaElement.PACKAGE_FRAGMENT :
					//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
					project = (JavaProject)element.getJavaProject();
					try {
						project.getJavaProjectElementInfo().setNameLookup(null);
					} catch(JavaModelException e){
					}
					break;
			}
		}
				
		close(element);
		fCurrentDelta.added(element);
	}
}
/**
 * Check whether the updated file is affecting some of the properties of a given project (like
 * its classpath persisted as a file).
 *
 */
public static void checkProjectPropertyFileUpdate(IResourceDelta delta, IJavaElement parent){
	
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
			} catch(CoreException e) {
			}
			break;
		case IResource.FILE :
			if (parent.getElementType() == IJavaElement.JAVA_PROJECT) {
				IFile file = (IFile)resource;
				JavaProject project = (JavaProject)parent;
				
				/* check classpath property file change */
				QualifiedName classpathProp;
				if (file.getName().equals(project.computeSharedPropertyFileName(classpathProp = project.getClasspathPropertyName()))){
					switch(delta.getKind()){
						case IResourceDelta.REMOVED : 	// recreate one based on in-memory path
							try {
								project.saveClasspath();
							} catch(JavaModelException e){
							}
							break;						// might consider to regenerate the file (accidental deletion?)
						case IResourceDelta.ADDED :	
						case IResourceDelta.CHANGED :	// check if any actual difference
							IPath oldOutputLocation = null;
							try {
								oldOutputLocation = project.getOutputLocation();
								// force to (re)read the property file
								String fileClasspathString = project.getSharedProperty(classpathProp);
								if (fileClasspathString == null) break; // did not find the file
								IClasspathEntry[] fileEntries = project.readPaths(fileClasspathString);
								if (fileEntries == null) break; // could not read, ignore 
								if (project.isClasspathEqualsTo(fileEntries)) break;

								// will force an update of the classpath/output location based on the file information
								// extract out the output location
								IPath outputLocation= null;								
								if (fileEntries != null && fileEntries.length > 0) {
									IClasspathEntry entry = fileEntries[fileEntries.length - 1];
									if (entry.getContentKind() == ClasspathEntry.K_OUTPUT) {
										outputLocation = entry.getPath();
										IClasspathEntry[] copy= new IClasspathEntry[fileEntries.length - 1];
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
								} catch(JavaModelException e){ // undo output location change
									project.setOutputLocation0(oldOutputLocation);
								}	
							} catch(IOException e){
								break;
							} catch(RuntimeException e){
								break;
							} catch(CoreException e){
								break;
							}
						
					}
				}
			}
			break;
	}
	if (processChildren){
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
	fRootsRemoved = null;
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
					
	Openable element = (Openable)JavaCore.create(delta.getResource());
	boolean processChildren = true;
	if (element != null){
		int flags = delta.getFlags();
		switch(element.getElementType()){
			case IJavaElement.CLASS_FILE :
			case IJavaElement.COMPILATION_UNIT :
				processChildren = false;
				switch (delta.getKind()) {
					case IResourceDelta.ADDED:
						break;
					case IResourceDelta.CHANGED:
						if ((flags & IResourceDelta.CONTENT) > 1) {
							try {
								element.close();
							} catch(JavaModelException e){
							}
						}
						break;
					case IResourceDelta.REMOVED:
						try {
							element.close();
						} catch(JavaModelException e){
						}
				}
		}
	}
	if (processChildren){
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
	if (resource == null) return null;
	String extension = resource.getFileExtension();
	extension = extension == null ? null : extension.toLowerCase();
	if ("jar"/*nonNLS*/.equals(extension) || "zip"/*nonNLS*/.equals(extension)) {
		IJavaProject[] projects = null;
		try {
			projects = JavaModelManager.getJavaModel(resource.getWorkspace()).getJavaProjects();
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
						jars.add(project.getPackageFragmentRoot((IFile)resource));
					}
				}
			} catch (JavaModelException e) {
			}
		}
		int size = jars.size();
		if (size == 0) return null;
		Openable[] result = new Openable[size];
		jars.copyInto(result);
		return result;
	} else {
		Openable element = (Openable)JavaCore.create(resource);
		if (element == null) {
			return null;
		} else {
			return new Openable[] {element};
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
		fProcessChildren = false;
		return;
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
	// do not process any children
	fProcessChildren = false;
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
 * When a package fragment root is removed, and it is on its project's classpath,
 * the classpath entry is marked to be removed when delta translation it done.
 * We postpone removing the classpath entry until delta translation is complete
 * so the factory can still translate resources into JavaElements for resources
 * which are children of the root. The delta for the removed mark is also annotated
 * with the F_REMOVED_FROM_CLASSPATH change flag.
 */
protected void elementRemoved(Openable element, IResourceDelta delta) {
	close(element);
	removeFromParentInfo(element);
	fCurrentDelta.removed(element);

	switch(element.getElementType()){
		case IJavaElement.JAVA_PROJECT :
			JavaModelManager.getJavaModelManager().removePerProjectInfo((JavaProject) element);
			break;
		case IJavaElement.PACKAGE_FRAGMENT_ROOT :
			// when a root on the classpath is removed, the classpath must be updated
			if (isOnClasspath(element)) {
				rootRemoved((IPackageFragmentRoot)element);
				JavaElementDelta rootDelta = fCurrentDelta.find(element);
				rootDelta.setFlags(rootDelta.getFlags() | IJavaElementDelta.F_REMOVED_FROM_CLASSPATH);
				//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
				JavaProject project = (JavaProject)element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null);
				} catch(JavaModelException e){
				}				
			}
			break;
		case IJavaElement.PACKAGE_FRAGMENT :
			//1G1TW2T - get rid of namelookup since it holds onto obsolete cached info 
			if (isOnClasspath(element)) {
				JavaProject project = (JavaProject)element.getJavaProject();
				try {
					project.getJavaProjectElementInfo().setNameLookup(null);
				} catch(JavaModelException e){
				}
			}
			break;
		case IJavaElement.JAVA_MODEL:
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
		if (delta.getAffectedChildren().length > 0 ||
			delta.getKind() != IJavaElementDelta.CHANGED ||
			delta.getFlags() == IJavaElementDelta.F_CLOSED ||
			delta.getFlags() == IJavaElementDelta.F_OPENED) {

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
 * Returns true if on of the following holds, otherwise false:<ul>
 * <li>the given element is a package fragment root and is specified
 * 		on its project's classpath
 * <li>the given element is not a package fragment root
 * </ul>
 */
protected boolean isOnClasspath(IJavaElement element) {

	if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
		IPackageFragmentRoot root = (IPackageFragmentRoot)element;
		JavaProject jp= (JavaProject)element.getJavaProject();
		try {
			return jp.getClasspathEntryFor(root.getPath()) != null;
		} catch (JavaModelException e) {
			return false;
		}
	} else {
		return true;
	}

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
 * Creates and returns a new classpath entry of the same kind as the
 * old entry, but with the new specified path.
 */
protected IClasspathEntry newClasspathEntry(IJavaProject project, IClasspathEntry oldEntry, IPath to) {
	IClasspathEntry newEntry = null;
	switch (oldEntry.getEntryKind()) {
		case IClasspathEntry.CPE_LIBRARY :
			newEntry = JavaCore.newLibraryEntry(to, oldEntry.getSourceAttachmentPath(), oldEntry.getSourceAttachmentRootPath());
			break;
		case IClasspathEntry.CPE_PROJECT :
			newEntry = JavaCore.newProjectEntry(to);
			break;
		case IClasspathEntry.CPE_SOURCE :
			newEntry = JavaCore.newSourceEntry(to);
			break;
	}
	return newEntry;
}
/**
 * Generic processing for elements with changed contents:<ul>
 * <li>The element is closed such that any subsequent accesses will re-open
 * the element reflecting its new structure.
 * <li>An entry is made in the delta reporting a content change (K_CHANGE with F_CONTENT flag set).
 * </ul>
 */
protected void nonJavaResourcesChanged(Openable element, IResourceDelta delta) throws JavaModelException {
	JavaElementInfo info = element.getElementInfo();
	switch (element.getElementType()) {
		case IJavaElement.JAVA_PROJECT :
			IResource resource = delta.getResource();
		 	((JavaProjectElementInfo) info).setNonJavaResources(null);

		 	// if a package fragment root is the project, clear it too
		 	PackageFragmentRoot projectRoot = (PackageFragmentRoot)((JavaProject)element).getPackageFragmentRoot(new Path(IPackageFragment.DEFAULT_PACKAGE_NAME));
		 	if (projectRoot.isOpen()) {
			 	((PackageFragmentRootInfo)projectRoot.getElementInfo()).setNonJavaResources(null);
		 	}
		 	
		 	if (delta.getResource().getFullPath().equals(((JavaProject)element).getOutputLocation())) {
				fProcessChildren = false;
				return;
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
 * Removes classpath entries associated with removed roots, from the
 * corresponding project's.
 */
protected void processRemovedRoots() {
	if (fRootsRemoved != null) {
		Enumeration roots = fRootsRemoved.elements();
		while (roots.hasMoreElements()) {
			PackageFragmentRoot root = (PackageFragmentRoot)roots.nextElement();
			JavaProject jp = (JavaProject)root.getJavaProject();
			try {
				IClasspathEntry entry = jp.getClasspathEntryFor(root.getPath());
				if (entry != null) {
					removeClasspathEntry(jp, entry);
				} else {
					// The root was not referenced, do nothing.
				}
			} catch (JavaModelException e) {
				// do nothing
			}
			
		}
	}
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
		JavaModel model = JavaModelManager.getJavaModel(delta.getResource().getWorkspace());
		if (model != null) {
			fCurrentDelta = new JavaElementDelta(model);
			traverseDelta(delta, model); // traverse delta
			translatedDeltas[i] = fCurrentDelta;
		}
	}
	// process removed roots
	processRemovedRoots();
	// update classpaths
	updateClasspaths(false); 
	
	// clear state
	clearState();
	
	return filterRealDeltas(translatedDeltas);
}
/**
 * Removes the specified classpath entry from the given project's info.
 */
protected void removeClasspathEntry(JavaProject project, IClasspathEntry entry) {
	try {
		IClasspathEntry[] cp = project.getRawClasspath();
		IClasspathEntry[] newPath = new IClasspathEntry[cp.length - 1];
		int pos = 0;
		for (int i = 0; i < cp.length; i++) {
			if (!cp[i].equals(entry)) {
				newPath[pos] = cp[i];
				pos++;
			}
		}

		((JavaProjectElementInfo)project.getElementInfo()).setRawClasspath(newPath);
		resetNonJavaResourcesForPackageFragmentRoots(project);
	} catch (JavaModelException e) {
		// failed to update classpath
	}
}
/**
 * Removes the given element from its parents cache of children. If the
 * element does not have a parent, or the parent is not currently open,
 * this has no effect. 
 */
protected void removeFromParentInfo(Openable child) {
	Openable parent = (Openable)child.getParent();
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
protected void resetNonJavaResourcesForPackageFragmentRoots(JavaProject project) throws JavaModelException {
	IPackageFragmentRoot[] roots = project.getAllPackageFragmentRoots();
	if (roots == null) return;
	for (int i = 0, max = roots.length; i < max; i++) {
		IPackageFragmentRoot root = roots[i];
		IResource res = root.getUnderlyingResource();
		if (res != null) {
			((PackageFragmentRoot)root).resetNonJavaResources();
		}
	}
}
/**
 * Add the given root to the list of roots that have been removed, and mark
 * its project as requiring an update. When delta translation is complete,
 * projects will have their classpaths updated.
 */
protected void rootRemoved(IPackageFragmentRoot root) {
	if (fRootsRemoved == null) {
		fRootsRemoved = new Vector(2);
	}
	fRootsRemoved.addElement(root);
	updateProject(root.getJavaProject());
}
/**
 * Converts an <code>IResourceDelta</code> and its children into
 * the corresponding <code>IJavaElementDelta</code>s.
 */
protected void traverseDelta(IResourceDelta delta, Openable parentElement) {
	Openable[] elements = this.createElements(delta.getResource());
	Openable element = null;
	int flags = delta.getFlags();
	fProcessChildren = true;
	if (elements != null) {
		for (int i = 0, length = elements.length; i < length; i++) {
			element = elements[i];
			IResource res = delta.getResource();

			updateIndex(element, delta);
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
					PackageFragmentRoot pkgRoot;
					if (res.getType() == IResource.FILE 
						&& parentElement != null 
						&& !parentElement.equals(element.getParent())
						&& ((pkgRoot = element.getPackageFragmentRoot()) == null || !isOnClasspath(pkgRoot))){
						try { // fake compilation/class file scenario (see JavaCore.createCompilationUnitFrom & createClassFileFrom
							nonJavaResourcesChanged(parentElement, delta);
							break;
						} catch(JavaModelException e) {
						}
					}
					elementAdded(element, delta);
					break;
				case IResourceDelta.REMOVED:
					if (res.getType() == IResource.FILE 
						&& parentElement != null 
						&& !parentElement.equals(element.getParent())
						&& ((pkgRoot = element.getPackageFragmentRoot()) == null || !isOnClasspath(pkgRoot))){
						try { // fake compilation/class file scenario (see JavaCore.createCompilationUnitFrom & createClassFileFrom
							nonJavaResourcesChanged(parentElement, delta);
							break;
						} catch(JavaModelException e) {
						}
					}
					elementRemoved(element, delta);
					break;
				case IResourceDelta.CHANGED:
					if ((flags & IResourceDelta.CONTENT) > 1) {
						contentChanged(element, delta);
						break;
					}
					if ((flags & IResourceDelta.OPEN) > 1) {
						res = delta.getResource();
						if (isOpen(res)) {
							elementOpened(element, delta);
						} else {
							elementClosed(element, delta);
						}
						break;
					}
					break;
			}
		}
	} else {
		try {
			if (parentElement != null && delta.getResource() != null) {
				switch (delta.getResource().getType()) {
					case IResource.FILE:
					case IResource.FOLDER:
						nonJavaResourcesChanged(parentElement, delta);
				}
			}
		} catch (JavaModelException e) {
			// do nothing
		}
			
		// checked for a moved root - the factory cannot create elements for roots not specified
		// on the classpath.
		if (delta.getKind() == IResourceDelta.ADDED &&
			((flags & IResourceDelta.MOVED_FROM) > 1)) {
				IProject project = delta.getResource().getProject();
				if (project != null) {
					JavaProject jp = (JavaProject)JavaCore.create(project);
					if (jp != null) {
						try {
							IClasspathEntry oldEntry = jp.getClasspathEntryFor(delta.getMovedFromPath());
							if (oldEntry != null) {
								IClasspathEntry newEntry = newClasspathEntry(jp, oldEntry, delta.getResource().getFullPath());
								addClasspathEntry(jp, oldEntry, newEntry);
								// now the factory can create the root.
								element = (Openable)JavaCore.create(delta.getResource());
								elementAdded(element, delta);
								JavaElementDelta rootDelta = fCurrentDelta.find(element);
								rootDelta.setFlags(rootDelta.getFlags() | IJavaElementDelta.F_ADDED_TO_CLASSPATH);
							}
						} catch (JavaModelException e) {
							// nothing
						}
					}
				}
			}
	}
	if (fProcessChildren) {
		IResourceDelta[] children = delta.getAffectedChildren();
		for (int i = 0; i < children.length; i++) {
			traverseDelta(children[i], element);
		}
	}
}
/**
 * Updates the classpath of each project requiring update. This refreshes
 * the cached info in each project's namelookup facility, and persists
 * classpaths.
 */
protected void updateClasspaths(boolean saveClasspath) {
	if (fJavaProjectsToUpdate != null) {
		Enumeration projects = fJavaProjectsToUpdate.elements();
		while (projects.hasMoreElements()) {
			JavaProject project = (JavaProject)projects.nextElement();
			try {
				project.updateClassPath();
				if (saveClasspath) project.saveClasspath();
			} catch (JavaModelException e) {
			}
		}
	}

}
protected void updateIndex(Openable element, IResourceDelta delta){

	if (indexManager == null) return;
	
	switch(element.getElementType()){
		case IJavaElement.JAVA_PROJECT : 
			switch (delta.getKind()) {
				case IResourceDelta.ADDED :
				case IResourceDelta.OPEN:
					indexManager.indexAll((IProject)delta.getResource());
					break;
			}
			break;
		case IJavaElement.CLASS_FILE :
			break;
		case IJavaElement.COMPILATION_UNIT :
			IFile file = (IFile)delta.getResource();
			String extension;
			switch (delta.getKind()) {
				case IResourceDelta.ADDED:
				case IResourceDelta.CHANGED:
					if (file.isLocal(IResource.DEPTH_ZERO)) indexManager.add(file);
					break;
				case IResourceDelta.REMOVED:
					extension = file.getFileExtension();
					indexManager.remove(file.getFullPath().toString(), file.getProject());
					break;
			}
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

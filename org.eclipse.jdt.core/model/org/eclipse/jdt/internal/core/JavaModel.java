package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.ConfigurableOption;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.util.*;

import java.io.File;
import java.util.*;

/**
 * Implementation of <code>IJavaModel<code>. The Java Model maintains a cache of
 * active <code>IJavaProject</code>s in a workspace. A Java Model is specific to a
 * workspace. To retrieve a workspace's model, use the
 * <code>#getJavaModel(IWorkspace)</code> method.
 *
 * @see IJavaModel
 */
public class JavaModel extends Openable implements IJavaModel {
	/**
	 * The workspace this Java Model represents
	 */
	protected IWorkspace workspace = null;

/**
 * Constructs a new Java Model on the given workspace.
 *
 * @exception Error if called more than once
 */
protected JavaModel(IWorkspace workspace) throws Error {
	super(JAVA_MODEL, null, ""/*nonNLS*/ /*workspace has empty name*/);
	this.workspace = workspace;
}
private void cleanupCycleMarkers() {
	try {
		IMarker[] markers = workspace.getRoot().findMarkers(IJavaModelMarker.TRANSIENT_PROBLEM, true,  IResource.DEPTH_ONE);
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			if (marker.getAttribute(IJavaModelMarker.CYCLE_DETECTED) != null) {
				workspace.deleteMarkers(new IMarker[] {marker});
			}
		}
	} catch (CoreException e) {
		e.printStackTrace();
	}
}
/**
 * Remove the Java Model from the cache
 */
protected void closing(Object info) throws JavaModelException {
	JavaModelManager.fgManager.fModelInfo.close();
	JavaModelManager.fgManager.fModelInfo= null;
}
/**
 * Computes the build order of the Java Projects in this Java Model.
 * The order is computed by following the class path of the projects.
 * Prerequesite projects appear first in the list, dependent projects
 * appear last.
 * If specified, reports an error against a project using a marker if a cycle
 * is detected. Otherwise throws a JavaModelException.
 */
public String[] computeBuildOrder(boolean generateMarkerOnError) throws JavaModelException {
	// Remove markers indicating cycle
	if (generateMarkerOnError) {
		this.cleanupCycleMarkers();
	}

	// Compute depth of each project and detect cycle
	StringHashtableOfInt depthTable = new StringHashtableOfInt();
	IJavaProject[] projects = getJavaProjects();
	int length = projects.length;
	int maxDepth = -1;
	for (int i = 0; i < length; i++) {
		String projectName = projects[i].getElementName();
		maxDepth = 
			Math.max(
				maxDepth, 
				this.computeDepth(projectName, depthTable, projectName, generateMarkerOnError));
	}

	// Sort projects by depth
	return depthTable.sortedKeys(maxDepth);	
}
/**
 * Computes the depth of the given java project following its classpath.
 * Only projects are taken into consideration. Store the depth in the given table.
 * Returns the depth.
 * Note that a project with no prerequisites has a depth of 0.
 * Returns -1 if a cycle is detected
 */
protected int computeDepth(String projectName, StringHashtableOfInt depthTable, String dependentProjectName, boolean generateMarkerOnError) throws JavaModelException {
	int depth = depthTable.get(projectName);
	switch (depth) {
		case -2: // project already visited -> it's a cycle
			if (generateMarkerOnError) {
				try {
					IMarker marker = this.workspace.getRoot().getProject(dependentProjectName).createMarker(IJavaModelMarker.TRANSIENT_PROBLEM);
					marker.setAttributes(
						new String[]{ IMarker.MESSAGE, IMarker.PRIORITY, IMarker.LOCATION, IJavaModelMarker.CYCLE_DETECTED},
						new Object[]{ Util.bind("classpath.cycle"/*nonNLS*/), new Integer(IMarker.PRIORITY_HIGH), dependentProjectName, dependentProjectName});
				} catch (CoreException e) {
					e.printStackTrace();
				}
			} else {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.NAME_COLLISION));
			}
			return -1;
		case -1:
			depthTable.put(projectName, -2); // mark we're visiting the project
			int prereqDepth = -1;
			JavaProject project = (JavaProject)this.getJavaProject(projectName);
			String[] prerequisites = null;
			try {
				prerequisites = project.getRequiredProjectNames();
			} catch (JavaModelException e) {
				prerequisites = JavaProject.NO_PREREQUISITES;
			}
			for (int i = 0, length = prerequisites.length; i < length; i++) {
				String prerequisite = prerequisites[i];
				prereqDepth = 
					Math.max(
						prereqDepth, 
						this.computeDepth(prerequisite, depthTable, projectName, generateMarkerOnError)
					);
			}
			depth = 1 + prereqDepth;
			depthTable.put(projectName, depth);
			return depth;
		default:
			return depth;
	}
}
/**
 * @see IJavaModel
 */
public void copy(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new CopyResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new CopyElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}
/**
 * Returns a new element info for this element.
 */
protected OpenableElementInfo createElementInfo() {
	return new JavaModelInfo(this, this.workspace);
}
/**
 * @see IJavaModel
 */
public void delete(IJavaElement[] elements, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new DeleteResourceElementsOperation(elements, force), monitor);
	} else {
		runOperation(new DeleteElementsOperation(elements, force), monitor);
	}
}
/**
 * Java Models are equal if their workspaces are equal
 *
 * @see Object#equals
 */
public boolean equals(Object o) {
	if (this == o)
		return true;
	if (o instanceof JavaModel) {
		JavaModel other = (JavaModel) o;
		return this.workspace.equals(other.workspace);
	}
	return false;
}
/**
 */
protected boolean generateInfos(
	OpenableElementInfo info,
	IProgressMonitor pm,
	Hashtable newElements,
	IResource underlyingResource)
	throws JavaModelException {

	fgJavaModelManager.fModelInfo = (JavaModelInfo) info;
	// determine my children
	try {
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0, max = projects.length; i < max; i++) {
			IProject project = projects[i];
			if (project.isOpen() && project.hasNature(JavaCore.NATURE_ID)) {
				info.addChild(getJavaProject(project));
			}
		}
	} catch (CoreException e) {
		throw new JavaModelException(e);
	}
	return true;
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IJavaElement getHandleFromMementoForBinaryMembers(String memento, IPackageFragmentRoot root, int rootEnd, int end) throws JavaModelException {

	//deal with class file and binary members
	IPackageFragment frag = null;
	if (rootEnd == end - 1) {
		//default package
		frag= root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	} else {
		frag= root.getPackageFragment(memento.substring(rootEnd + 1, end));
	}
	int oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	if (end == -1) {
		//we ended with a class file 
		return frag.getClassFile(memento.substring(oldEnd + 1));
	}
	IClassFile cf = frag.getClassFile(memento.substring(oldEnd + 1, end));
	oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_FIELD, end);
	if (end != -1) {
		//binary field
		IType type = cf.getType();
		return type.getField(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_METHOD, oldEnd);
	if (end != -1) {
		//binary method
		oldEnd = end;
		IType type = cf.getType();
		String methodName;
		end = memento.lastIndexOf(JavaElement.JEM_METHOD);
		String[] parameterTypes = null;
		if (end == oldEnd) {
			methodName = memento.substring(end + 1);
			//no parameter types
			parameterTypes = new String[] {};
		} else {
			String parameters = memento.substring(oldEnd + 1);
			StringTokenizer tokenizer = new StringTokenizer(parameters, new String(new char[] {JavaElement.JEM_METHOD}));
			parameterTypes = new String[tokenizer.countTokens() - 1];
			methodName= tokenizer.nextToken();
			int i = 0;
			while (tokenizer.hasMoreTokens()) {
				parameterTypes[i] = tokenizer.nextToken();
				i++;
			}
		}
		return type.getMethod(methodName, parameterTypes);
	}

	//binary type
	return cf.getType();
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IJavaElement getHandleFromMementoForSourceMembers(String memento, IPackageFragmentRoot root, int rootEnd, int end) throws JavaModelException {

	//deal with compilation units and source members
	IPackageFragment frag = null;
	if (rootEnd == end - 1) {
		//default package
		frag= root.getPackageFragment(IPackageFragment.DEFAULT_PACKAGE_NAME);
	} else {
		frag= root.getPackageFragment(memento.substring(rootEnd + 1, end));
	}
	int oldEnd = end;
	end = memento.indexOf(JavaElement.JEM_PACKAGEDECLARATION, end);
	if (end != -1) {
		//package declaration
		ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, end));
		return cu.getPackageDeclaration(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_IMPORTDECLARATION, oldEnd);
	if (end != -1) {
		//import declaration
		ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, end));
		return cu.getImport(memento.substring(end + 1));
	}
	int typeStart = memento.indexOf(JavaElement.JEM_TYPE, oldEnd);
	if (typeStart == -1) {
		//we ended with a compilation unit
		return frag.getCompilationUnit(memento.substring(oldEnd + 1));
	}

	//source members
	ICompilationUnit cu = frag.getCompilationUnit(memento.substring(oldEnd + 1, typeStart));
	end = memento.indexOf(JavaElement.JEM_FIELD, oldEnd);
	if (end != -1) {
		//source field
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		return type.getField(memento.substring(end + 1));
	}
	end = memento.indexOf(JavaElement.JEM_METHOD, oldEnd);
	if (end != -1) {
		//source method
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		oldEnd = end;
		String methodName;
		end = memento.lastIndexOf(JavaElement.JEM_METHOD);
		String[] parameterTypes = null;
		if (end == oldEnd) {
			methodName = memento.substring(end + 1);
			//no parameter types
			parameterTypes = new String[] {};
		} else {
			String parameters = memento.substring(oldEnd + 1);
			StringTokenizer mTokenizer = new StringTokenizer(parameters, new String(new char[] {JavaElement.JEM_METHOD}));
			parameterTypes = new String[mTokenizer.countTokens() - 1];
			methodName = mTokenizer.nextToken();
			int i = 0;
			while (mTokenizer.hasMoreTokens()) {
				parameterTypes[i] = mTokenizer.nextToken();
				i++;
			}
		}
		return type.getMethod(methodName, parameterTypes);
	}
	
	end = memento.indexOf(JavaElement.JEM_INITIALIZER, oldEnd);
	if (end != -1 ) {
		//initializer
		IType type = getHandleFromMementoForSourceType(memento, cu, typeStart, end);
		return type.getInitializer(Integer.parseInt(memento.substring(end + 1)));
	}
	//source type
	return getHandleFromMementoForSourceType(memento, cu, typeStart, memento.length());
	;
}
/**
 * Returns the <code>IJavaElement</code> represented by the <code>String</code>
 * memento.
 * @see getHandleMemento()
 */
protected IType getHandleFromMementoForSourceType(String memento, ICompilationUnit cu, int typeStart, int typeEnd) throws JavaModelException {
	int end = memento.lastIndexOf(JavaElement.JEM_TYPE);
	IType type = null;
	if (end == typeStart) {
		String typeName = memento.substring(typeStart + 1, typeEnd);
		type = cu.getType(typeName);
		
	} else {
		String typeNames = memento.substring(typeStart + 1, typeEnd);
		StringTokenizer tokenizer = new StringTokenizer(typeNames, new String(new char[] {JavaElement.JEM_TYPE}));
		type = cu.getType(tokenizer.nextToken());
		while (tokenizer.hasMoreTokens()) {
			//deal with inner types
			type= type.getType(tokenizer.nextToken());
		}
	}
	return type;
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	return getElementName();
}
/**
 * Returns the <code>char</code> that marks the start of this handles
 * contribution to a memento.
 */
protected char getHandleMementoDelimiter(){
	Assert.isTrue(false, Util.bind("assert.shouldNotImplement"/*nonNLS*/));
	return 0;
}
/**
 * @see IJavaElement
 */
public IJavaModel getJavaModel() {
	return this;
}
/**
 * @see IJavaElement
 */
public IJavaProject getJavaProject() {
	return null;
}
/**
 * @see IJavaModel
 */
public IJavaProject getJavaProject(String name) {
	return new JavaProject(this.workspace.getRoot().getProject(name), this);
}
/**
 * Returns the active Java project associated with the specified
 * resource, or <code>null</code> if no Java project yet exists
 * for the resource.
 *
 * @exception IllegalArgumentException if the given resource
 * is not one of an IProject, IFolder, or IFile.
 */
public IJavaProject getJavaProject(IResource resource) {
	if (resource.getType() == IResource.FOLDER) {
		return new JavaProject(((IFolder)resource).getProject(), this);
	} else if (resource.getType() == IResource.FILE) {
		return new JavaProject(((IFile)resource).getProject(), this);
	} else if (resource.getType() == IResource.PROJECT) {
		return new JavaProject((IProject)resource, this);
	} else {
		throw new IllegalArgumentException(Util.bind("element.invalidResourceForProject"/*nonNLS*/));
	}
}
/**
 * @see IJavaModel
 */
public IJavaProject[] getJavaProjects() throws JavaModelException {
	Vector v= getChildrenOfType(JAVA_PROJECT);
	IJavaProject[] array= new IJavaProject[v.size()];
	v.copyInto(array);
	return array;

}
/**
 * @see IOpenable
 */
public IResource getUnderlyingResource() throws JavaModelException {
	return null;
}
/**
 * Returns the workbench associated with this object.
 */
public IWorkspace getWorkspace() {
	return this.workspace;
}
/**
 * The hashcode of a Java Model is that of its workspace.
 */
public int hashCode() {
	return this.workspace.hashCode();
}
/**
 * @see IJavaModel
 */
public void move(IJavaElement[] elements, IJavaElement[] containers, IJavaElement[] siblings, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		runOperation(new MoveResourceElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	} else {
		runOperation(new MoveElementsOperation(elements, containers, force), elements, siblings, renamings, monitor);
	}
}
/**
 * @see IJavaModel
 */
public void rename(IJavaElement[] elements, IJavaElement[] destinations, String[] renamings, boolean force, IProgressMonitor monitor) throws JavaModelException {
	MultiOperation op;
	if (elements != null && elements[0] != null && elements[0].getElementType() < IJavaElement.TYPE) {
		op = new RenameResourceElementsOperation(elements, destinations, renamings, force);
	} else {
		op = new RenameElementsOperation(elements, destinations, renamings, force);
	}
	
	runOperation(op, monitor);
}
/**
 * Configures and runs the <code>MultiOperation</code>.
 */
protected void runOperation(MultiOperation op, IJavaElement[] elements, IJavaElement[] siblings, String[] renamings, IProgressMonitor monitor) throws JavaModelException {
	op.setRenamings(renamings);
	if (siblings != null) {
		for (int i = 0; i < elements.length; i++) {
			op.setInsertBefore(elements[i], siblings[i]);
		}
	}
	runOperation(op, monitor);
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append("Java Model"/*nonNLS*/);
	if (info == null) {
		buffer.append(" (not open)"/*nonNLS*/);
	}
}

/**
 * Helper method - returns the targeted item (IResource if internal or java.io.File if external), 
 * or null if unbound
 * Internal items must be referred to using container relative paths.
 */
public static Object getTarget(IContainer container, IPath path, boolean checkResourceExistence) {

	if (path == null) return null;
	
	// lookup - inside the container
	IResource resource = container.findMember(path);
	if (resource != null){
		if (!checkResourceExistence ||resource.exists()) return resource;
		return null;
	}

	// lookup - outside the container
	File externalFile = new File(path.toOSString());
	if (externalFile.exists()) return externalFile;
	return null;	
}
}

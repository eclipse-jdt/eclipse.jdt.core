/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IImportDeclaration;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageDeclaration;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;


public abstract class AbstractJavaModelTests extends SuiteOfTestCases {
	
	/**
	 * The java.io.File path to the directory that contains the external jars.
	 */
	protected static String EXTERNAL_JAR_DIR_PATH;
	
	/**
	 * Delta listener
	 */
	protected class DeltaListener implements IElementChangedListener {
		/**
		 * Deltas received from the java model. See 
		 * <code>#startDeltas</code> and
		 * <code>#stopDeltas</code>.
		 */
		public IJavaElementDelta[] deltas;
	
		public void elementChanged(ElementChangedEvent ev) {
			IJavaElementDelta[] copy= new IJavaElementDelta[deltas.length + 1];
			System.arraycopy(deltas, 0, copy, 0, deltas.length);
			copy[deltas.length]= ev.getDelta();
			deltas= copy;
		}
	}
	protected DeltaListener deltaListener = new DeltaListener();
	 
	
public AbstractJavaModelTests(String name) {
	super(name);
}
protected void addJavaNature(String projectName) throws CoreException {
	IProject project = getWorkspaceRoot().getProject(projectName);
	IProjectDescription description = project.getDescription();
	description.setNatureIds(new String[] {JavaCore.NATURE_ID});
	project.setDescription(description, null);
}
/**
 * Ensures the elements are present after creation.
 */
public void assertCreation(IJavaElement[] newElements) {
	for (int i = 0; i < newElements.length; i++) {
		IJavaElement newElement = newElements[i];
		assertTrue("Element should be present after creation", newElement.exists());
	}
}
/**
 * Ensures the element is present after creation.
 */
public void assertCreation(IJavaElement newElement) {
	assertCreation(new IJavaElement[] {newElement});
}
/**
 * Creates an operation to delete the given elements, asserts
 * the operation is successful, and ensures the elements are no
 * longer present in the model.
 */
public void assertDeletion(IJavaElement[] elementsToDelete) throws JavaModelException {
	IJavaElement elementToDelete = null;
	for (int i = 0; i < elementsToDelete.length; i++) {
		elementToDelete = elementsToDelete[i];
		assertTrue("Element must be present to be deleted", elementToDelete.exists());
	}

	getJavaModel().delete(elementsToDelete, false, null);
	
	for (int i = 0; i < elementsToDelete.length; i++) {
		elementToDelete = elementsToDelete[i];
		assertTrue("Element should not be present after deletion: " + elementToDelete, !elementToDelete.exists());
	}
}
/**
 * Attaches a source zip to the given jar package fragment root.
 */
protected void attachSource(IPackageFragmentRoot root, String sourcePath, String sourceRoot) throws JavaModelException {
	IJavaProject javaProject = root.getJavaProject();
	IClasspathEntry[] entries = (IClasspathEntry[])javaProject.getRawClasspath().clone();
	for (int i = 0; i < entries.length; i++){
		IClasspathEntry entry = entries[i];
		if (entry.getPath().toOSString().toLowerCase().equals(root.getPath().toOSString().toLowerCase())) {
			entries[i] = JavaCore.newLibraryEntry(
				root.getPath(),
				sourcePath == null ? null : new Path(sourcePath),
				sourceRoot == null ? null : new Path(sourceRoot),
				false);
			break;
		}
	}
	javaProject.setRawClasspath(entries, null);
}
/**
 * Creates an operation to delete the given element, asserts
 * the operation is successfull, and ensures the element is no
 * longer present in the model.
 */
public void assertDeletion(IJavaElement elementToDelete) throws JavaModelException {
	assertDeletion(new IJavaElement[] {elementToDelete});
}
/**
 * Empties the current deltas.
 */
public void clearDeltas() {
	this.deltaListener.deltas = new IJavaElementDelta[0];
}
/**
 * Copy file from src (path to the original file) to dest (path to the destination file).
 */
public void copy(File src, File dest) throws IOException {
	// read source bytes
	byte[] srcBytes = this.read(src);

	// write bytes to dest
	FileOutputStream out = new FileOutputStream(dest);
	out.write(srcBytes);
	out.close();
}
/**
 * Copy the given source directory (and all its contents) to the given target directory.
 */
protected void copyDirectory(File source, File target) throws IOException {
	if (!target.exists()) {
		target.mkdirs();
	}
	File[] files = source.listFiles();
	if (files == null) return;
	for (int i = 0; i < files.length; i++) {
		File sourceChild = files[i];
		String name =  sourceChild.getName();
		if (name.equals("CVS")) continue;
		File targetChild = new File(target, name);
		if (sourceChild.isDirectory()) {
			copyDirectory(sourceChild, targetChild);
		} else {
			copy(sourceChild, targetChild);
		}
	}
}
/*
 * Creates a Java project with the given source folders an output location. 
 * Add those on the project's classpath.
 */
protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String output) throws CoreException {
	return this.createJavaProject(projectName, sourceFolders, new String[] {}, output);
}
protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String[] libraries, final String output) throws CoreException {
	return this.createJavaProject(projectName, sourceFolders, libraries, new String[] {}, output);
}
protected IJavaProject createJavaProject(final String projectName, final String[] sourceFolders, final String[] libraries, final String[] projects, final String output) throws CoreException {
	final IJavaProject[] result = new IJavaProject[1];
	IWorkspaceRunnable create = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			// create project
			createProject(projectName);
			
			// set java nature
			addJavaNature(projectName);
			
			// create classpath entries 
			IProject project = getWorkspaceRoot().getProject(projectName);
			IPath projectPath = project.getFullPath();
			int sourceLength = sourceFolders.length;
			int libLength = libraries.length;
			int projectLength = projects.length;
			IClasspathEntry[] entries = new IClasspathEntry[sourceLength+libLength+projectLength];
			for (int i= 0; i < sourceLength; i++) {
				IPath sourcePath = new Path(sourceFolders[i]);
				int segmentCount = sourcePath.segmentCount();
				if (segmentCount > 0) {
					// create folder and its parents
					IContainer container = project;
					for (int j = 0; j < segmentCount; j++) {
						IFolder folder = container.getFolder(new Path(sourcePath.segment(j)));
						folder.create(true, true, null);
						container = folder;
					}
				}
				entries[i] = JavaCore.newSourceEntry(projectPath.append(sourcePath));
			}
			for (int i= 0; i < libLength; i++) {
				String lib = libraries[i];
				if (JavaCore.getClasspathVariable(lib) != null) {
					entries[sourceLength+i] = JavaCore.newVariableEntry(new Path(lib), null, null);
				} else {
					IPath libPath = new Path(lib);
					if (!libPath.isAbsolute() && libPath.segmentCount() > 0 && libPath.getFileExtension() == null) {
						project.getFolder(libPath).create(true, true, null);
						libPath = projectPath.append(libPath);
					}
					entries[sourceLength+i] = JavaCore.newLibraryEntry(libPath, null, null);
				}
			}
			for  (int i= 0; i < projectLength; i++) {
				entries[sourceLength+libLength+i] = JavaCore.newProjectEntry(new Path(projects[i]));
			}
			
			// create output folder
			IPath outputPath = new Path(output);
			if (outputPath.segmentCount() > 0) {
				IFolder output = project.getFolder(outputPath);
				if (!output.exists()) {
					output.create(true, true, null);
				}
			}
			
			// set classpath and output location
			IJavaProject javaProject = JavaCore.create(project);
			javaProject.setRawClasspath(entries, projectPath.append(outputPath), null);
			result[0] = javaProject;
		}
	};
	getWorkspace().run(create, null);	
	return result[0];
}
/*
 * Create simple project.
 */
protected void createProject(final String projectName) throws CoreException {
	IWorkspaceRunnable create = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			IProject project = getProject(projectName);
			project.create(null);
			project.open(null);
		}
	};
	getWorkspace().run(create, null);	
}
public void deleteFile(File file) {
	file = file.getAbsoluteFile();
	if (!file.exists())
		return;
	if (file.isDirectory()) {
		String[] files = file.list();
		//file.list() can return null
		if (files != null) {
			for (int i = 0; i < files.length; ++i) {
				deleteFile(new File(file, files[i]));
			}
		}
	}
	if (!file.delete()) {
		System.out.println("WARNING: deleteFile(File) could not delete: " + file.getPath());
	}
}
protected void deleteProject(String projectName) throws CoreException {
	IProject project = this.getProject(projectName);
	CoreException lastException = null;
	try {
		if (!project.isOpen()) { // force opening so that project can be deleted without logging (see bug 23629)
			project.open(null);
		}
		project.delete(true, null);
	} catch (CoreException e) {
		lastException = e;
	}
	int retryCount = 60; // wait 1 minute at most
	while (project.isAccessible() && --retryCount >= 0) {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		try {
			project.delete(true, null);
		} catch (CoreException e) {
			lastException = e;
		}
	}
	if (!project.isAccessible()) return;
	System.err.println("Failed to delete project " + projectName);
	if (lastException != null) {
		throw lastException;
	}
}
/**
 * Delete this resource.
 */
public void deleteResource(IResource resource) throws CoreException {
	resource.delete(false, null);
}
/**
 * Returns true if this delta is flagged as having changed children.
 */
protected boolean deltaChildrenChanged(IJavaElementDelta delta) {
	return delta.getKind() == IJavaElementDelta.CHANGED &&
		(delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0;
}
/**
 * Returns true if this delta is flagged as having had a content change
 */
protected boolean deltaContentChanged(IJavaElementDelta delta) {
	return delta.getKind() == IJavaElementDelta.CHANGED &&
		(delta.getFlags() & IJavaElementDelta.F_CONTENT) != 0;
}
/**
 * Returns true if this delta is flagged as having moved from a location
 */
protected boolean deltaMovedFrom(IJavaElementDelta delta) {
	return delta.getKind() == IJavaElementDelta.ADDED &&
		(delta.getFlags() & IJavaElementDelta.F_MOVED_FROM) != 0;
}
/**
 * Returns true if this delta is flagged as having moved to a location
 */
protected boolean deltaMovedTo(IJavaElementDelta delta) {
	return delta.getKind() == IJavaElementDelta.REMOVED &&
		(delta.getFlags() & IJavaElementDelta.F_MOVED_TO) != 0;
}
/**
 * Returns the specified compilation unit in the given project, root, and
 * package fragment or <code>null</code> if it does not exist.
 */
public IClassFile getClassFile(String projectName, String rootPath, String packageName, String className) throws JavaModelException {
	IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
	if (pkg == null) {
		return null;
	} else {
		return pkg.getClassFile(className);
	}
}
/**
 * Returns the specified compilation unit in the given project, root, and
 * package fragment or <code>null</code> if it does not exist.
 */
public ICompilationUnit getCompilationUnit(String projectName, String rootPath, String packageName, String cuName) throws JavaModelException {
	IPackageFragment pkg= getPackageFragment(projectName, rootPath, packageName);
	if (pkg == null) {
		return null;
	} else {
		return pkg.getCompilationUnit(cuName);
	}
}
protected ICompilationUnit getCompilationUnitFor(IJavaElement element) {

	if (element instanceof ICompilationUnit) {
		return (ICompilationUnit)element;
	}

	if (element instanceof IMember) {
		return ((IMember)element).getCompilationUnit();
	}

	if (element instanceof IPackageDeclaration ||
		element instanceof IImportDeclaration) {
			return (ICompilationUnit)element.getParent();
		}

	return null;

}
/**
 * Returns the last delta for the given element from the cached delta.
 */
protected IJavaElementDelta getDeltaFor(IJavaElement element) {
	return getDeltaFor(element, false);
}
/**
 * Returns the delta for the given element from the cached delta.
 * If the boolean is true returns the first delta found.
 */
protected IJavaElementDelta getDeltaFor(IJavaElement element, boolean returnFirst) {
	IJavaElementDelta result = null;
	for (int i = 0; i < this.deltaListener.deltas.length; i++) {
		IJavaElementDelta delta = searchForDelta(element, (IJavaElementDelta) this.deltaListener.deltas[i]);
		if (delta != null) {
			if (returnFirst) {
				return delta;
			} else {
				result = delta;
			}
		}
	}
	return result;
}
/**
 * Returns the java.io path to the external java class library (e.g. jclMin.jar)
 */
protected String getExternalJCLPath() {
	return EXTERNAL_JAR_DIR_PATH + File.separator + "jclMin.jar";
}
/**
 * Returns the java.io path to the root source of the external java class library (e.g. "src")
 */
protected String getExternalJCLRootSourcePath() {
	return "";
}
/**
 * Returns the java.io path to the source of the external java class library (e.g. jclMinsrc.zip)
 */
protected String getExternalJCLSourcePath() {
	return EXTERNAL_JAR_DIR_PATH + File.separator + "jclMinsrc.zip";
}
/**
 * Returns the Java Model this test suite is running on.
 */
public IJavaModel getJavaModel() {
	return JavaCore.create(getWorkspaceRoot());
}
/**
 * Returns the Java Project with the given name in this test
 * suite's model. This is a convenience method.
 */
public IJavaProject getJavaProject(String name) {
	IProject project = getProject(name);
	return JavaCore.create(project);
}
/**
 * Returns the specified package fragment in the given project and root, or
 * <code>null</code> if it does not exist.
 * The rootPath must be specified as a project relative path. The empty
 * path refers to the default package fragment.
 */
public IPackageFragment getPackageFragment(String projectName, String rootPath, String packageName) throws JavaModelException {
	IPackageFragmentRoot root= getPackageFragmentRoot(projectName, rootPath);
	if (root == null) {
		return null;
	} else {
		return root.getPackageFragment(packageName);
	}
}
/**
 * Returns the specified package fragment root in the given project, or
 * <code>null</code> if it does not exist.
 * The rootPath must be specified as a project relative path. The empty
 * path refers to the package fragment root that is the project
 * folder iteslf.
 */
public IPackageFragmentRoot getPackageFragmentRoot(
	String projectName, 
	String rootPath)
	throws JavaModelException {
		
	IJavaProject project = getJavaProject(projectName);
	if (project == null) {
		return null;
	}
	IPath path = new Path(rootPath);
	if (path.isAbsolute()) {
		IPackageFragmentRoot root = project.getPackageFragmentRoot(rootPath);
		if (root.exists()) {
			return root;
		}
	} else {
		IPackageFragmentRoot[] roots = project.getPackageFragmentRoots();
		if (roots == null || roots.length == 0) {
			return null;
		}
		for (int i = 0; i < roots.length; i++) {
			IPackageFragmentRoot root = roots[i];
			if (!root.isExternal()
				&& root.getUnderlyingResource().getProjectRelativePath().equals(path)) {
				return root;
			}
		}
	}
	return null;
}
protected IProject getProject(String project) {
	return getWorkspaceRoot().getProject(project);
}
/**
 * Returns the OS path to the directory that contains this plugin.
 */
protected String getPluginDirectoryPath() {
	CodeSource javaCoreCodeSource = JavaCore.class.getProtectionDomain().getCodeSource();
	if (javaCoreCodeSource != null) {
		URL javaCoreUrl = javaCoreCodeSource.getLocation();
		String javaCorePath = javaCoreUrl.getFile();
		int index = javaCorePath.indexOf(JavaCore.PLUGIN_ID);
		if (index != -1) {
			String pluginsPath = javaCorePath.substring(0, index);
			File pluginsFile = new File(pluginsPath);
			String[] list = pluginsFile.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					return name.startsWith( "org.eclipse.jdt.core.tests.model");
				}
			});
			if (list != null && list.length > 0) {
				return pluginsPath + list[0];
			}
		}
	}
	return null;
}
public String getSourceWorkspacePath() {
	return getPluginDirectoryPath() +  java.io.File.separator + "workspace";
}
/**
 * Returns the IWorkspace this test suite is running on.
 */
public IWorkspace getWorkspace() {
	return ResourcesPlugin.getWorkspace();
}
protected IWorkspaceRoot getWorkspaceRoot() {
	return getWorkspace().getRoot();
}
public byte[] read(java.io.File file) throws java.io.IOException {
	int fileLength;
	byte[] fileBytes = new byte[fileLength = (int) file.length()];
	java.io.FileInputStream stream = new java.io.FileInputStream(file);
	int bytesRead = 0;
	int lastReadSize = 0;
	while ((lastReadSize != -1) && (bytesRead != fileLength)) {
		lastReadSize = stream.read(fileBytes, bytesRead, fileLength - bytesRead);
		bytesRead += lastReadSize;
	}
	stream.close();
	return fileBytes;
}
protected void removeJavaNature(String projectName) throws CoreException {
	IProject project = this.getProject(projectName);
	IProjectDescription description = project.getDescription();
	description.setNatureIds(new String[] {});
	project.setDescription(description, null);
}
/**
 * Returns a delta for the given element in the delta tree
 */
protected IJavaElementDelta searchForDelta(IJavaElement element, IJavaElementDelta delta) {

	if (delta == null) {
		return null;
	} else {
		if (delta.getElement().equals(element)) {
			return delta;
		} else {
			for (int i= 0; i < delta.getAffectedChildren().length; i++) {
				IJavaElementDelta child= searchForDelta(element, delta.getAffectedChildren()[i]);
				if (child != null) {
					return child;
				}
			}
			return null;
		}
	}
}
/**
 * Sets the class path of the Java project.
 */
public void setClasspath(IJavaProject javaProject, IClasspathEntry[] classpath) {
	try {
		javaProject.setRawClasspath(classpath, null);
	} catch (JavaModelException e) {
		assertTrue("failed to set classpath", false);
	}
}
/**
 * Check locally for the required JCL files, jclMin.jar and jclMinsrc.zip.
 * If not available, copy from the project resources.
 */
public void setupExternalJCL() throws IOException {
	if (EXTERNAL_JAR_DIR_PATH != null) return;
	
	String separator = java.io.File.separator;
	String resourceJCLDir = getPluginDirectoryPath() + separator + "JCL";
	String localJCLPath = getWorkspaceRoot().getLocation().toFile().getParentFile().getCanonicalPath();
	EXTERNAL_JAR_DIR_PATH = localJCLPath;
	java.io.File jclDir = new java.io.File(localJCLPath);
	java.io.File jclMin =
		new java.io.File(localJCLPath + separator + "jclMin.jar");
	java.io.File jclMinsrc = new java.io.File(localJCLPath + separator + "jclMinsrc.zip");
	if (!jclDir.exists()) {
		if (!jclDir.mkdir()) {
			//mkdir failed
			throw new IOException("Could not create the directory " + jclDir);
		} else {
			//copy the two files to the JCL directory
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "jclMin.jar");
			copy(resourceJCLMin, jclMin);
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "jclMinsrc.zip");
			copy(resourceJCLMinsrc, jclMinsrc);
		}
	} else {
		//check that the two files, jclMin.jar and jclMinsrc.zip are present
		//copy either file that is missing
		if (!jclMin.exists()) {
			java.io.File resourceJCLMin =
				new java.io.File(resourceJCLDir + separator + "jclMin.jar");
			copy(resourceJCLMin, jclMin);
		}
		if (!jclMinsrc.exists()) {
			java.io.File resourceJCLMinsrc =
				new java.io.File(resourceJCLDir + separator + "jclMinsrc.zip");
			copy(resourceJCLMinsrc, jclMinsrc);
		}
	}
}
protected IJavaProject setUpJavaProject(final String projectName) throws CoreException, IOException {
	// copy files in project from source workspace to target workspace
	String sourceWorkspacePath = getSourceWorkspacePath();
	String targetWorkspacePath = getWorkspaceRoot().getLocation().toFile().getCanonicalPath();
	copyDirectory(new File(sourceWorkspacePath, projectName), new File(targetWorkspacePath, projectName));
	
	// ensure variables are set
	if (JavaCore.getClasspathVariable("JCL_LIB") == null) {
		JavaCore.setClasspathVariables(
			new String[] {"JCL_LIB", "JCL_SRC", "JCL_SRCROOT"},
			new Path[] {new Path(getExternalJCLPath()), new Path(getExternalJCLSourcePath()), new Path(getExternalJCLRootSourcePath())},
			null);
	}
	
	// ensure autobuilding is turned off
	IWorkspaceDescription description = getWorkspace().getDescription();
	if (description.isAutoBuilding()) {
		description.setAutoBuilding(false);
		 getWorkspace().setDescription(description);
	}

	// create project
	final IProject project = getWorkspaceRoot().getProject(projectName);
	IWorkspaceRunnable populate = new IWorkspaceRunnable() {
		public void run(IProgressMonitor monitor) throws CoreException {
			project.create(null);
			project.open(null);
		}
	};
	getWorkspace().run(populate, null);
	return JavaCore.create(project);
}
public void setUpSuite() throws Exception {
	super.setUpSuite();
	setupExternalJCL();
}
/**
 * Starts listening to element deltas, and queues them in fgDeltas.
 */
public void startDeltas() {
	clearDeltas();
	JavaCore.addElementChangedListener(this.deltaListener);
}
/**
 * Stops listening to element deltas, and clears the current deltas.
 */
public void stopDeltas() {
	JavaCore.removeElementChangedListener(this.deltaListener);
	clearDeltas();
}

}

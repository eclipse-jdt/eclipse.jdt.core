package org.eclipse.jdt.internal.core.newbuilder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;

import java.io.File;
import java.util.*;

public class JavaBuilder extends IncrementalProjectBuilder {

IProject currentProject;
IJavaProject javaProject;
IWorkspaceRoot workspaceRoot;
ClasspathLocation[] classpath;
IContainer outputFolder;
IContainer[] sourceFolders;
LookupTable prereqOutputFolders; // maps a prereq project to its output folder
State lastState;
BuildNotifier notifier;

public static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
public static final String CLASS_EXTENSION = "class"; //$NON-NLS-1$
public static final String JAR_EXTENSION = "jar"; //$NON-NLS-1$
public static final String ZIP_EXTENSION = "zip"; //$NON-NLS-1$

public static boolean DEBUG = false;

public JavaBuilder() {
}

protected IProject[] build(int kind, Map ignored, IProgressMonitor monitor) throws CoreException {
	this.currentProject = getProject();
	if (currentProject == null || !currentProject.exists()) return new IProject[0];

	this.notifier = new BuildNotifier(monitor, currentProject);
	notifier.begin();
	boolean ok = false;
	try {
		initializeBuilder();

		if (kind == FULL_BUILD) {
			buildAll();
		} else {
			if (lastState == null || hasClasspathChanged() || hasOutputLocationChanged()) {
				// if the output location changes, do not delete the binary files from old location
				// the user may be trying something
				buildAll();
			} else if (sourceFolders.length > 0) {
				// if there is no source to compile & no classpath changes then we are done
				LookupTable deltas = findDeltas();
				if (deltas == null)
					buildAll();
				else
					buildDeltas(deltas);
			}
		}
		ok = true;
	} catch (ImageBuilderInternalException e) {
		// Fix for 1FW2XY6: ITPJCORE:ALL - Image builder wrappers CoreException
		// WHY not just let the CoreException thru instead of wrappering it?
		if (e.getThrowable() instanceof CoreException)
			throw (CoreException) e.getThrowable();
		throw new CoreException(
			new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1,
				Util.bind("build.builderName"), e)); //$NON-NLS-1$
	} finally {
		if (!ok)
			// If the build failed, clear the previously built state, forcing a full build next time.
			setLastState(null);
		notifier.done();
		cleanup();
	}
	return getRequiredProjects();
}

private void buildAll() throws CoreException {
	if (DEBUG)
		System.out.println("\nFULL build of: " + currentProject.getName()); //$NON-NLS-1$

	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	setLastState(null); // free last state since we do not need it
	BatchImageBuilder imageBuilder = new BatchImageBuilder(this);
	imageBuilder.build();
	setLastState(imageBuilder.newState);
}

private void buildDeltas(LookupTable deltas) throws CoreException {
	if (DEBUG)
		System.out.println("\nINCREMENTAL build of: " + currentProject.getName()); //$NON-NLS-1$

	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	IncrementalImageBuilder imageBuilder = new IncrementalImageBuilder(this);
	if (imageBuilder.build(deltas))
		setLastState(imageBuilder.newState);
	else
		buildAll();
}

private void cleanup() {
	this.classpath = null;
	this.outputFolder = null;
	this.sourceFolders = null;
	this.prereqOutputFolders = null;
	this.lastState = null;
	this.notifier = null;
}

private LookupTable findDeltas() {
	notifier.subTask(Util.bind("build.readingDelta", currentProject.getName())); //$NON-NLS-1$
	IResourceDelta delta = getDelta(currentProject);
	LookupTable deltas = new LookupTable();
	if (delta != null) {
		deltas.put(currentProject, delta);
	} else {
		if (DEBUG)
			System.out.println("Missing delta for: " + currentProject.getName()); //$NON-NLS-1$
		notifier.subTask(""); //$NON-NLS-1$
		return null;
	}

	Enumeration enum = prereqOutputFolders.keys();
	while (enum.hasMoreElements()) {
		IProject prereqProject = (IProject) enum.nextElement();
		notifier.subTask(Util.bind("build.readingDelta", prereqProject.getName())); //$NON-NLS-1$
		delta = getDelta(prereqProject);
		if (delta != null) {
			deltas.put(prereqProject, delta);
		} else {
			if (DEBUG)
				System.out.println("Missing delta for: " + prereqProject.getName());	 //$NON-NLS-1$
			notifier.subTask(""); //$NON-NLS-1$
			return null;
		}
	}
	notifier.subTask(""); //$NON-NLS-1$
	return deltas;
}

private State getLastState() {
	return (State) JavaModelManager.getJavaModelManager().getLastBuiltState2(currentProject, notifier.monitor);
}

private void setLastState(State state) {
//	if (state != null) state.dump();
	JavaModelManager.getJavaModelManager().setLastBuiltState2(currentProject, state);
}

/* Return the list of projects for which it requires a resource delta. This builder's project
* is implicitly included and need not be specified. Builders must re-specify the list 
* of interesting projects every time they are run as this is not carried forward
* beyond the next build. Missing projects should be specified but will be ignored until
* they are added to the workspace.
*/
private IProject[] getRequiredProjects() {
	if (javaProject == null || workspaceRoot == null) return new IProject[0];

	String[] projectNames;
	try {
		projectNames = javaProject.getRequiredProjectNames();
	} catch(JavaModelException e) {
		return new IProject[0];
	}

	ArrayList projects = new ArrayList();
	for (int i = 0; i < projectNames.length; ++i) {
		IProject p = workspaceRoot.getProject(projectNames[i]);
		if (p != null && !projects.contains(p))
			projects.add(p);
	}
	IProject[] result = new IProject[projects.size()];
	projects.toArray(result);
	return result;
}

private boolean hasClasspathChanged() {
	ClasspathLocation[] oldClasspathLocations = lastState.classpathLocations;
	if (classpath.length != oldClasspathLocations.length) return true;

	for (int i = 0, length = classpath.length; i < length; i++)
		if (!classpath[i].equals(oldClasspathLocations[i])) return true;
	return false;
}

private boolean hasOutputLocationChanged() {
	return !outputFolder.getLocation().toString().equals(lastState.outputLocationString);
}

private void createFolder(IContainer folder) throws CoreException {
	if (!folder.exists()) {
		IContainer parent = folder.getParent();
		if (currentProject.getFullPath() != parent.getFullPath())
			createFolder(parent);
		((IFolder) folder).create(true, true, null);
	}
}

private void initializeBuilder() throws CoreException {
	this.javaProject = JavaCore.create(currentProject);
	this.workspaceRoot = currentProject.getWorkspace().getRoot();
	this.outputFolder = (IContainer) workspaceRoot.findMember(javaProject.getOutputLocation());
	if (this.outputFolder == null) {
		this.outputFolder = workspaceRoot.getFolder(javaProject.getOutputLocation());
		createFolder(this.outputFolder);
	}
	this.lastState = getLastState();

	ArrayList sourceList = new ArrayList();
	this.prereqOutputFolders = new LookupTable();
	this.classpath = NameEnvironment.computeLocations(
		workspaceRoot,
		javaProject,
		outputFolder.getLocation().toString(),
		sourceList,
		prereqOutputFolders);
	this.sourceFolders = new IContainer[sourceList.size()];
	sourceList.toArray(this.sourceFolders);
}

/**
 * String representation for debugging purposes
 */
public String toString() {
	State state = getLastState();
	if (state != null)
		return "JavaBuilder(" //$NON-NLS-1$
			+ state + ")"; //$NON-NLS-1$
	return "JavaBuilder(no built state)"; //$NON-NLS-1$
}
}
/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.SimpleLookupTable;

import java.io.*;
import java.util.*;

public class JavaBuilder extends IncrementalProjectBuilder {

IProject currentProject;
JavaProject javaProject;
IWorkspaceRoot workspaceRoot;
NameEnvironment nameEnvironment;
SimpleLookupTable binaryLocationsPerProject; // maps a project to its binary resources (output folders, class folders, zip/jar files)
State lastState;
BuildNotifier notifier;
char[][] extraResourceFileFilters;
String[] extraResourceFolderFilters;

public static final String CLASS_EXTENSION = "class"; //$NON-NLS-1$

public static boolean DEBUG = false;

/**
 * A list of project names that have been built.
 * This list is used to reset the JavaModel.existingExternalFiles cache when a build cycle begins
 * so that deleted external jars are discovered.
 */
static ArrayList builtProjects = null;

public static IMarker[] getProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			return resource.findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there are no problems
	return new IMarker[0];
}

public static IMarker[] getTasksFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			return resource.findMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there are no tasks
	return new IMarker[0];
}

public static void removeProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there were no problems
}

public static void removeTasksFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			resource.deleteMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there were no problems
}

public static void removeProblemsAndTasksFor(IResource resource) {
	try {
		if (resource != null && resource.exists()) {
			resource.deleteMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
			resource.deleteMarkers(IJavaModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
		}
	} catch (CoreException e) {} // assume there were no problems
}

public static State readState(IProject project, DataInputStream in) throws IOException {
	return State.read(project, in);
}

public static void writeState(Object state, DataOutputStream out) throws IOException {
	((State) state).write(out);
}

public JavaBuilder() {
}

protected IProject[] build(int kind, Map ignored, IProgressMonitor monitor) throws CoreException {
	this.currentProject = getProject();
	if (currentProject == null || !currentProject.isAccessible()) return new IProject[0];

	if (DEBUG)
		System.out.println("\nStarting build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	this.notifier = new BuildNotifier(monitor, currentProject);
	notifier.begin();
	boolean ok = false;
	try {
		notifier.checkCancel();
		initializeBuilder();

		if (isWorthBuilding()) {
			if (kind == FULL_BUILD) {
				buildAll();
			} else {
				if ((this.lastState = getLastState(currentProject)) == null) {
					if (DEBUG)
						System.out.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
					buildAll();
				} else if (hasClasspathChanged()) {
					// if the output location changes, do not delete the binary files from old location
					// the user may be trying something
					buildAll();
				} else if (nameEnvironment.sourceLocations.length > 0) {
					// if there is no source to compile & no classpath changes then we are done
					SimpleLookupTable deltas = findDeltas();
					if (deltas == null)
						buildAll();
					else if (deltas.elementSize > 0)
						buildDeltas(deltas);
					else if (DEBUG)
						System.out.println("Nothing to build since deltas were empty"); //$NON-NLS-1$
				} else {
					if (hasStructuralDelta()) { // double check that a jar file didn't get replaced in a binary project
						buildAll();
					} else {
						if (DEBUG)
							System.out.println("Nothing to build since there are no source folders and no deltas"); //$NON-NLS-1$
						lastState.tagAsNoopBuild();
					}
				}
			}
			ok = true;
		}
	} catch (CoreException e) {
		Util.log(e, "JavaBuilder handling CoreException"); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject", e.getLocalizedMessage())); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (ImageBuilderInternalException e) {
		Util.log(e.getThrowable(), "JavaBuilder handling ImageBuilderInternalException"); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject", e.coreException.getLocalizedMessage())); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (MissingClassFileException e) {
		// do not log this exception since its thrown to handle aborted compiles because of missing class files
		if (DEBUG)
			System.out.println(Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
		IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (MissingSourceFileException e) {
		// do not log this exception since its thrown to handle aborted compiles because of missing source files
		if (DEBUG)
			System.out.println(Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
		removeProblemsAndTasksFor(currentProject); // make this the only problem for this project
		IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} finally {
		if (!ok)
			// If the build failed, clear the previously built state, forcing a full build next time.
			clearLastState();
		notifier.done();
		cleanup();
	}
	IProject[] requiredProjects = getRequiredProjects(true);
	if (DEBUG)
		System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	return requiredProjects;
}

private void buildAll() {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	if (DEBUG && lastState != null)
		System.out.println("Clearing last state : " + lastState); //$NON-NLS-1$
	clearLastState();
	BatchImageBuilder imageBuilder = new BatchImageBuilder(this);
	imageBuilder.build();
	recordNewState(imageBuilder.newState);
}

private void buildDeltas(SimpleLookupTable deltas) {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	if (DEBUG && lastState != null)
		System.out.println("Clearing last state : " + lastState); //$NON-NLS-1$
	clearLastState(); // clear the previously built state so if the build fails, a full build will occur next time
	IncrementalImageBuilder imageBuilder = new IncrementalImageBuilder(this);
	if (imageBuilder.build(deltas))
		recordNewState(imageBuilder.newState);
	else
		buildAll();
}

private void cleanup() {
	this.nameEnvironment = null;
	this.binaryLocationsPerProject = null;
	this.lastState = null;
	this.notifier = null;
	this.extraResourceFileFilters = null;
	this.extraResourceFolderFilters = null;
}

private void clearLastState() {
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, null);
}

boolean filterExtraResource(IResource resource) {
	if (extraResourceFileFilters != null) {
		char[] name = resource.getName().toCharArray();
		for (int i = 0, l = extraResourceFileFilters.length; i < l; i++)
			if (CharOperation.match(extraResourceFileFilters[i], name, true))
				return true;
	}
	if (extraResourceFolderFilters != null) {
		IPath path = resource.getProjectRelativePath();
		String pathName = path.toString();
		int count = path.segmentCount();
		if (resource.getType() == IResource.FILE) count--;
		for (int i = 0, l = extraResourceFolderFilters.length; i < l; i++)
			if (pathName.indexOf(extraResourceFolderFilters[i]) != -1)
				for (int j = 0; j < count; j++)
					if (extraResourceFolderFilters[i].equals(path.segment(j)))
						return true;
	}
	return false;
}

private SimpleLookupTable findDeltas() {
	notifier.subTask(Util.bind("build.readingDelta", currentProject.getName())); //$NON-NLS-1$
	IResourceDelta delta = getDelta(currentProject);
	SimpleLookupTable deltas = new SimpleLookupTable(3);
	if (delta != null) {
		if (delta.getKind() != IResourceDelta.NO_CHANGE) {
			if (DEBUG)
				System.out.println("Found source delta for: " + currentProject.getName()); //$NON-NLS-1$
			deltas.put(currentProject, delta);
		}
	} else {
		if (DEBUG)
			System.out.println("Missing delta for: " + currentProject.getName()); //$NON-NLS-1$
		notifier.subTask(""); //$NON-NLS-1$
		return null;
	}

	Object[] keyTable = binaryLocationsPerProject.keyTable;
	Object[] valueTable = binaryLocationsPerProject.valueTable;
	nextProject : for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject p = (IProject) keyTable[i];
		if (p != null && p != currentProject) {
			State s = getLastState(p);
			if (!lastState.wasStructurallyChanged(p, s)) { // see if we can skip its delta
				if (s.wasNoopBuild())
					continue nextProject; // project has no source folders and can be skipped
				ClasspathLocation[] classFoldersAndJars = (ClasspathLocation[]) valueTable[i];
				boolean canSkip = true;
				for (int j = 0, m = classFoldersAndJars.length; j < m; j++) {
					if (classFoldersAndJars[j].isOutputFolder())
						classFoldersAndJars[j] = null; // can ignore output folder since project was not structurally changed
					else
						canSkip = false;
				}
				if (canSkip) continue nextProject; // project has no structural changes in its output folders
			}

			notifier.subTask(Util.bind("build.readingDelta", p.getName())); //$NON-NLS-1$
			delta = getDelta(p);
			if (delta != null) {
				if (delta.getKind() != IResourceDelta.NO_CHANGE) {
					if (DEBUG)
						System.out.println("Found binary delta for: " + p.getName()); //$NON-NLS-1$
					deltas.put(p, delta);
				}
			} else {
				if (DEBUG)
					System.out.println("Missing delta for: " + p.getName());	 //$NON-NLS-1$
				notifier.subTask(""); //$NON-NLS-1$
				return null;
			}
		}
	}
	notifier.subTask(""); //$NON-NLS-1$
	return deltas;
}

private State getLastState(IProject project) {
	return (State) JavaModelManager.getJavaModelManager().getLastBuiltState(project, notifier.monitor);
}

/* Return the list of projects for which it requires a resource delta. This builder's project
* is implicitly included and need not be specified. Builders must re-specify the list 
* of interesting projects every time they are run as this is not carried forward
* beyond the next build. Missing projects should be specified but will be ignored until
* they are added to the workspace.
*/
private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
	if (javaProject == null || workspaceRoot == null) return new IProject[0];

	ArrayList projects = new ArrayList();
	try {
		IClasspathEntry[] entries = javaProject.getExpandedClasspath(true);
		for (int i = 0, l = entries.length; i < l; i++) {
			IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
			if (entry != null) {
				IPath path = entry.getPath();
				IProject p = null;
				switch (entry.getEntryKind()) {
					case IClasspathEntry.CPE_PROJECT :
						p = workspaceRoot.getProject(path.lastSegment()); // missing projects are considered too
						break;
					case IClasspathEntry.CPE_LIBRARY :
						if (includeBinaryPrerequisites && path.segmentCount() > 1) {
							// some binary resources on the class path can come from projects that are not included in the project references
							IResource resource = workspaceRoot.findMember(path.segment(0));
							if (resource instanceof IProject)
								p = (IProject) resource;
						}
				}
				if (p != null && !projects.contains(p))
					projects.add(p);
			}
		}
	} catch(JavaModelException e) {
		return new IProject[0];
	}
	IProject[] result = new IProject[projects.size()];
	projects.toArray(result);
	return result;
}

private boolean hasClasspathChanged() {
	ClasspathMultiDirectory[] newSourceLocations = nameEnvironment.sourceLocations;
	ClasspathMultiDirectory[] oldSourceLocations = lastState.sourceLocations;
	int newLength = newSourceLocations.length;
	int oldLength = oldSourceLocations.length;
	int n, o;
	for (n = o = 0; n < newLength && o < oldLength; n++, o++) {
		if (newSourceLocations[n].equals(oldSourceLocations[o])) continue; // checks source & output folders
		try {
			if (newSourceLocations[n].sourceFolder.members().length == 0) { // added new empty source folder
				o--;
				continue;
			}
		} catch (CoreException ignore) {}
		if (DEBUG)
			System.out.println(newSourceLocations[n] + " != " + oldSourceLocations[o]); //$NON-NLS-1$
		return true;
	}
	while (n < newLength) {
		try {
			if (newSourceLocations[n].sourceFolder.members().length == 0) { // added new empty source folder
				n++;
				continue;
			}
		} catch (CoreException ignore) {}
		if (DEBUG)
			System.out.println("Added non-empty source folder"); //$NON-NLS-1$
		return true;
	}
	if (o < oldLength) {
		if (DEBUG)
			System.out.println("Removed source folder"); //$NON-NLS-1$
		return true;
	}

	ClasspathLocation[] newBinaryLocations = nameEnvironment.binaryLocations;
	ClasspathLocation[] oldBinaryLocations = lastState.binaryLocations;
	newLength = newBinaryLocations.length;
	oldLength = oldBinaryLocations.length;
	for (n = o = 0; n < newLength && o < oldLength; n++, o++) {
		if (newBinaryLocations[n].equals(oldBinaryLocations[o])) continue;
		if (DEBUG)
			System.out.println(newBinaryLocations[n] + " != " + oldBinaryLocations[o]); //$NON-NLS-1$
		return true;
	}
	if (n < newLength || o < oldLength) {
		if (DEBUG)
			System.out.println("Number of binary folders/jar files has changed"); //$NON-NLS-1$
		return true;
	}
	return false;
}

private boolean hasStructuralDelta() {
	// handle case when currentProject has only .class file folders and/or jar files... no source/output folders
	IResourceDelta delta = getDelta(currentProject);
	if (delta != null && delta.getKind() != IResourceDelta.NO_CHANGE) {
		ClasspathLocation[] classFoldersAndJars = (ClasspathLocation[]) binaryLocationsPerProject.get(currentProject);
		if (classFoldersAndJars != null) {
			for (int i = 0, l = classFoldersAndJars.length; i < l; i++) {
				ClasspathLocation classFolderOrJar = classFoldersAndJars[i]; // either a .class file folder or a zip/jar file
				if (classFolderOrJar != null) {
					IPath p = classFolderOrJar.getProjectRelativePath();
					if (p != null) {
						IResourceDelta binaryDelta = delta.findMember(p);
						if (binaryDelta != null && binaryDelta.getKind() != IResourceDelta.NO_CHANGE)
							return true;
					}
				}
			}
		}
	}
	return false;
}

private void initializeBuilder() throws CoreException {
	this.javaProject = (JavaProject) JavaCore.create(currentProject);
	this.workspaceRoot = currentProject.getWorkspace().getRoot();

	// Flush the existing external files cache if this is the beginning of a build cycle
	String projectName = currentProject.getName();
	if (builtProjects == null || builtProjects.contains(projectName)) {
		JavaModel.flushExternalFileCache();
		builtProjects = new ArrayList();
	}
	builtProjects.add(projectName);

	this.binaryLocationsPerProject = new SimpleLookupTable(3);
	this.nameEnvironment = new NameEnvironment(workspaceRoot, javaProject, binaryLocationsPerProject);

	String filterSequence = javaProject.getOption(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, true);
	char[][] filters = filterSequence != null && filterSequence.length() > 0
		? CharOperation.splitAndTrimOn(',', filterSequence.toCharArray())
		: null;
	if (filters == null) {
		this.extraResourceFileFilters = null;
		this.extraResourceFolderFilters = null;
	} else {
		int fileCount = 0, folderCount = 0;
		for (int i = 0, l = filters.length; i < l; i++) {
			char[] f = filters[i];
			if (f.length == 0) continue;
			if (f[f.length - 1] == '/') folderCount++; else fileCount++;
		}
		this.extraResourceFileFilters = new char[fileCount][];
		this.extraResourceFolderFilters = new String[folderCount];
		for (int i = 0, l = filters.length; i < l; i++) {
			char[] f = filters[i];
			if (f.length == 0) continue;
			if (f[f.length - 1] == '/')
				extraResourceFolderFilters[--folderCount] = new String(CharOperation.subarray(f, 0, f.length - 1));
			else
				extraResourceFileFilters[--fileCount] = f;
		}
	}
}

private boolean isClasspathBroken(IClasspathEntry[] classpath, IProject p) throws CoreException {
	if (classpath == JavaProject.INVALID_CLASSPATH) // the .classpath file could not be read
		return true;

	IMarker[] markers = p.findMarkers(IJavaModelMarker.BUILDPATH_PROBLEM_MARKER, false, IResource.DEPTH_ZERO);
	for (int i = 0, l = markers.length; i < l; i++)
		if (((Integer) markers[i].getAttribute(IMarker.SEVERITY)).intValue() == IMarker.SEVERITY_ERROR)
			return true;
	return false;
}

private boolean isWorthBuilding() throws CoreException {
	boolean abortBuilds =
		JavaCore.ABORT.equals(javaProject.getOption(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, true));
	if (!abortBuilds) return true;

	// Abort build only if there are classpath errors
	IClasspathEntry[] classpath = javaProject.getRawClasspath();
	if (isClasspathBroken(classpath, currentProject)) {
		if (DEBUG)
			System.out.println("Aborted build because project has classpath errors (incomplete or involved in cycle)"); //$NON-NLS-1$

		// remove all existing class files... causes all dependent projects to do the same
		// only if the .classpath file could be read
		if (classpath != JavaProject.INVALID_CLASSPATH)
			new BatchImageBuilder(this).cleanOutputFolders();
			
		JavaModelManager.getJavaModelManager().deltaProcessor.addForRefresh(javaProject);
		
		removeProblemsAndTasksFor(currentProject); // remove all compilation problems

		IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.abortDueToClasspathProblems")); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		return false;
	}

	// make sure all prereq projects have valid build states... only when aborting builds since projects in cycles do not have build states
	// except for projects involved in a 'warning' cycle (see below)
	IProject[] requiredProjects = getRequiredProjects(false);
	next : for (int i = 0, l = requiredProjects.length; i < l; i++) {
		IProject p = requiredProjects[i];
		if (getLastState(p) == null)  {
			// The prereq project has no build state: if this prereq project has a 'warning' cycle marker then allow build (see bug id 23357)
			JavaProject prereq = (JavaProject) JavaCore.create(p);
			if (prereq.hasCycleMarker() && JavaCore.WARNING.equals(javaProject.getOption(JavaCore.CORE_CIRCULAR_CLASSPATH, true)))
				continue;
			if (DEBUG)
				System.out.println("Aborted build because prereq project " + p.getName() //$NON-NLS-1$
					+ " was not built"); //$NON-NLS-1$

			// remove all existing class files... causes all dependent projects to do the same only if the .classpath file could be read
			IClasspathEntry[] prereqClasspath = prereq.getRawClasspath();
			if (prereqClasspath != JavaProject.INVALID_CLASSPATH)
				new BatchImageBuilder(this).cleanOutputFolders();

			removeProblemsAndTasksFor(currentProject); // make this the only problem for this project
			IMarker marker = currentProject.createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
			marker.setAttribute(IMarker.MESSAGE,
				isClasspathBroken(prereqClasspath, p)
					? Util.bind("build.prereqProjectHasClasspathProblems", p.getName()) //$NON-NLS-1$
					: Util.bind("build.prereqProjectMustBeRebuilt", p.getName())); //$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			return false;
		}
	}
	return true;
}

/*
 * Instruct the build manager that this project is involved in a cycle and
 * needs to propagate structural changes to the other projects in the cycle.
 */
void mustPropagateStructuralChanges() {
	HashSet cycleParticipants = new HashSet(3);
	javaProject.updateCycleParticipants(null, new ArrayList(), cycleParticipants, workspaceRoot);

	Iterator i= cycleParticipants.iterator();
	while (i.hasNext()) {
		IJavaProject p = (IJavaProject) i.next();
		if (p != javaProject && hasBeenBuilt(p.getProject())) {
			if (DEBUG) 
				System.out.println("Requesting another build iteration since cycle participant " + p.getProject().getName() //$NON-NLS-1$
					+ " has not yet seen some structural changes"); //$NON-NLS-1$
			needRebuild();
			return;
		}
	}
}

private void recordNewState(State state) {
	Object[] keyTable = binaryLocationsPerProject.keyTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject prereqProject = (IProject) keyTable[i];
		if (prereqProject != null && prereqProject != currentProject)
			state.recordStructuralDependency(prereqProject, getLastState(prereqProject));
	}

	if (DEBUG)
		System.out.println("Recording new state : " + state); //$NON-NLS-1$
	// state.dump();
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, state);
}

/**
 * String representation for debugging purposes
 */
public String toString() {
	return currentProject == null
		? "JavaBuilder for unknown project" //$NON-NLS-1$
		: "JavaBuilder for " + currentProject.getName(); //$NON-NLS-1$
}
}
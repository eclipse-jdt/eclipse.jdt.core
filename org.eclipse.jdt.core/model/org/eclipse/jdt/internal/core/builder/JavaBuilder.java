package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2002.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.*;

import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.Util;

import java.io.*;
import java.util.*;

public class JavaBuilder extends IncrementalProjectBuilder {

IProject currentProject;
IJavaProject javaProject;
IWorkspaceRoot workspaceRoot;
ClasspathLocation[] classpath;
IContainer outputFolder;
IContainer[] sourceFolders;
SimpleLookupTable binaryResources; // maps a project to its binary resources (output folder, class folders, zip/jar files)
State lastState;
BuildNotifier notifier;
char[][] resourceFilters;

public static final String JAVA_EXTENSION = "java"; //$NON-NLS-1$
public static final String CLASS_EXTENSION = "class"; //$NON-NLS-1$
public static final String JAR_EXTENSION = "jar"; //$NON-NLS-1$
public static final String ZIP_EXTENSION = "zip"; //$NON-NLS-1$
public static final String OPTION_ResourceCopyFilter = "org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"; //$NON-NLS-1$

public static boolean DEBUG = false;

static final String ProblemMarkerTag = IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER;
/**
 * A list of project names that have been built.
 * This list is used to reset the JavaModel.existingExternalFiles cache when a build cycle begins
 * so that deleted external jars are discovered.
 */
static ArrayList builtProjects = null;

public static IMarker[] getProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			return resource.findMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there are no problems
	return new IMarker[0];
}

public static void removeProblemsFor(IResource resource) {
	try {
		if (resource != null && resource.exists())
			resource.deleteMarkers(ProblemMarkerTag, false, IResource.DEPTH_INFINITE);
	} catch (CoreException e) {} // assume there were no problems
}

public static State readState(DataInputStream in) throws IOException {
	return State.read(in);
}

public static void writeState(Object state, DataOutputStream out) throws IOException {
	((State) state).write(out);
}

public JavaBuilder() {
}

protected IProject[] build(int kind, Map ignored, IProgressMonitor monitor) throws CoreException {
	this.currentProject = getProject();
	if (currentProject == null || !currentProject.exists()) return new IProject[0];

	if (DEBUG)
		System.out.println("\nStarting build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	this.notifier = new BuildNotifier(monitor, currentProject);
	notifier.begin();
	boolean ok = false;
	try {
		notifier.checkCancel();
		initializeBuilder();

		if (kind == FULL_BUILD) {
			buildAll();
		} else {
			if ((this.lastState = getLastState(currentProject)) == null) {
				if (DEBUG)
					System.out.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
				buildAll();
			} else if (hasClasspathChanged() || hasOutputLocationChanged()) {
				// if the output location changes, do not delete the binary files from old location
				// the user may be trying something
				buildAll();
			} else if (sourceFolders.length > 0) { // if there is no source to compile & no classpath changes then we are done
				clearLastState(); // clear the previously built state so if the build fails, a full build will occur next time
				SimpleLookupTable deltas = findDeltas();
				if (deltas == null)
					buildAll();
				else
					buildDeltas(deltas);
			}
		}
		ok = true;
	} catch (CoreException e) {
		if (DEBUG){
			System.out.println("Builder handling CoreException"); //$NON-NLS-1$
			 e.printStackTrace();
		}
		try {
			IMarker marker = currentProject.createMarker(ProblemMarkerTag);
			marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException ignore) {
			throw e;
		}
	} catch (ImageBuilderInternalException e) {
		if (DEBUG){
			System.out.println("Builder handling ImageBuilderInternalException"); //$NON-NLS-1$
			 e.printStackTrace();
		}
		try {
			IMarker marker = currentProject.createMarker(ProblemMarkerTag);
			marker.setAttribute(IMarker.MESSAGE, Util.bind("build.inconsistentProject")); //$NON-NLS-1$
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		} catch (CoreException ignore) {
			throw e.getThrowable();
		}
	} catch (IncompleteClassPathException e) {
		if (DEBUG){
			System.out.println("Builder handling IncompleteClassPathException"); //$NON-NLS-1$
			 e.printStackTrace();
		}
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.incompleteClassPath", e.missingClassFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} catch (MissingSourceFileException e) {
		if (DEBUG){
			System.out.println("Builder handling MissingSourceFileException"); //$NON-NLS-1$
			 e.printStackTrace();
		}
		removeProblemsFor(currentProject); // make this the only problem for this project
		IMarker marker = currentProject.createMarker(ProblemMarkerTag);
		marker.setAttribute(IMarker.MESSAGE, Util.bind("build.missingSourceFile", e.missingSourceFile)); //$NON-NLS-1$
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
	} finally {
		if (!ok)
			// If the build failed, clear the previously built state, forcing a full build next time.
			clearLastState();
		notifier.done();
		cleanup();
	}
	IProject[] requiredProjects = getRequiredProjects();
	if (DEBUG)
		System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
			+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
	return requiredProjects;
}

private void buildAll() {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	clearLastState();
	BatchImageBuilder imageBuilder = new BatchImageBuilder(this);
	imageBuilder.build();
	recordNewState(imageBuilder.newState);
}

private void buildDeltas(SimpleLookupTable deltas) {
	notifier.checkCancel();
	notifier.subTask(Util.bind("build.preparingBuild")); //$NON-NLS-1$
	IncrementalImageBuilder imageBuilder = new IncrementalImageBuilder(this);
	if (imageBuilder.build(deltas))
		recordNewState(imageBuilder.newState);
	else
		buildAll();
}

private void cleanup() {
	this.classpath = null;
	this.outputFolder = null;
	this.sourceFolders = null;
	this.lastState = null;
	this.notifier = null;
}

private void clearLastState() {
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, null);
}

private void createFolder(IContainer folder) throws CoreException {
	if (!folder.exists()) {
		IContainer parent = folder.getParent();
		if (currentProject.getFullPath() != parent.getFullPath())
			createFolder(parent);
		((IFolder) folder).create(true, true, null);
	}
}

boolean filterResource(IResource resource) {
	if (resourceFilters != null) {
		char[] name = resource.getName().toCharArray();
		for (int i = 0, length = resourceFilters.length; i < length; i++)
			if (CharOperation.match(resourceFilters[i], name, true))
				return true;
	}
	return false;
}

private SimpleLookupTable findDeltas() {
	notifier.subTask(Util.bind("build.readingDelta", currentProject.getName())); //$NON-NLS-1$
	IResourceDelta delta = getDelta(currentProject);
	SimpleLookupTable deltas = new SimpleLookupTable(binaryResources.elementSize + 1);
	if (delta != null) {
		deltas.put(currentProject, delta);
	} else {
		if (DEBUG)
			System.out.println("Missing delta for: " + currentProject.getName()); //$NON-NLS-1$
		notifier.subTask(""); //$NON-NLS-1$
		return null;
	}

	Object[] keyTable = binaryResources.keyTable;
	Object[] valueTable = binaryResources.valueTable;
	nextProject : for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject p = (IProject) keyTable[i];
		if (p != null && p != currentProject) {
			if (!lastState.isStructurallyChanged(p, getLastState(p))) { // see if we can skip its delta
				IResource[] binaryResources = (IResource[]) valueTable[i];
				if (binaryResources.length <= 1)
					continue nextProject; // project has no structural changes in its output folder
				binaryResources[0] = null; // skip the output folder
			}

			notifier.subTask(Util.bind("build.readingDelta", p.getName())); //$NON-NLS-1$
			delta = getDelta(p);
			if (delta != null) {
				deltas.put(p, delta);
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
private IProject[] getRequiredProjects() {
	if (javaProject == null || workspaceRoot == null) return new IProject[0];

	ArrayList projects = new ArrayList();
	try {
		IClasspathEntry[] entries = ((JavaProject) javaProject).getExpandedClasspath(true);
		for (int i = 0, length = entries.length; i < length; i++) {
			IClasspathEntry entry = JavaCore.getResolvedClasspathEntry(entries[i]);
			if (entry != null && entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) {
				IProject p = workspaceRoot.getProject(entry.getPath().lastSegment());
				if (p != null && !projects.contains(p))
					projects.add(p);
			}
		}
		// some binary resources on the class path can come from projects that are not included in the project references
		if (binaryResources != null) {
			Object[] keyTable = binaryResources.keyTable;
			for (int i = 0, l = keyTable.length; i < l; i++) {
				IProject p = (IProject) keyTable[i];
				if (p != null && !projects.contains(p))
					projects.add(p);
			}
			this.binaryResources = null;
		}
	} catch(JavaModelException e) {
		return new IProject[0];
	}
	IProject[] result = new IProject[projects.size()];
	projects.toArray(result);
	return result;
}

private boolean hasClasspathChanged() {
	ClasspathLocation[] oldClasspathLocations = lastState.classpathLocations;
	int newLength = classpath.length;
	int oldLength = oldClasspathLocations.length;
	int diff = newLength - oldLength;
	if (diff == 0) {
		for (int i = 0; i < newLength; i++) {
			if (classpath[i].equals(oldClasspathLocations[i])) continue;
			if (DEBUG)
				System.out.println(classpath[i] + " != " + oldClasspathLocations[i]); //$NON-NLS-1$
			return true;
		}
		return false;
	} else if (diff == 1) {
		ClasspathMultiDirectory newSourceDirectory = null;
		int n = 0, o = 0;
		for (; n < newLength && o < oldLength; n++, o++) {
			if (classpath[n].equals(oldClasspathLocations[o])) continue;
			if (diff == 1 && classpath[n] instanceof ClasspathMultiDirectory) { // added a new source folder
				newSourceDirectory = (ClasspathMultiDirectory) classpath[n];
				o--;
				diff = 0; // found new element
				continue;
			}
			if (DEBUG)
				System.out.println(classpath[n] + " != " + oldClasspathLocations[o]); //$NON-NLS-1$
			return true;
		}

		if (diff == 1 && classpath[n] instanceof ClasspathMultiDirectory) // added a new source folder at the end
			newSourceDirectory = (ClasspathMultiDirectory) classpath[n];
		if (newSourceDirectory != null) {
			IContainer sourceFolder = workspaceRoot.getContainerForLocation(new Path(newSourceDirectory.sourcePath));
			if (sourceFolder != null && sourceFolder.exists()) {
				try {
					if (sourceFolder.members().length == 0) return false; // added a new empty source folder
				} catch (CoreException ignore) {}
			}
		}
	}

	if (DEBUG)
		System.out.println("Class path size changed"); //$NON-NLS-1$
	return true;
}

private boolean hasOutputLocationChanged() {
	if (outputFolder.getLocation().toString().equals(lastState.outputLocationString))
		return false;

	if (DEBUG)
		System.out.println(outputFolder.getLocation().toString() + " != " + lastState.outputLocationString); //$NON-NLS-1$
	return true;
} 

private void initializeBuilder() throws CoreException {
	this.javaProject = JavaCore.create(currentProject);
	this.workspaceRoot = currentProject.getWorkspace().getRoot();
	this.outputFolder = (IContainer) workspaceRoot.findMember(javaProject.getOutputLocation());
	if (this.outputFolder == null) {
		this.outputFolder = workspaceRoot.getFolder(javaProject.getOutputLocation());
		createFolder(this.outputFolder);
	}

	ArrayList sourceList = new ArrayList();
	this.binaryResources = new SimpleLookupTable(3);
	this.classpath = NameEnvironment.computeLocations(
		workspaceRoot,
		javaProject,
		outputFolder.getLocation().toString(),
		sourceList,
		binaryResources);
	this.sourceFolders = new IContainer[sourceList.size()];
	sourceList.toArray(this.sourceFolders);
	
	String filterSequence = (String) JavaCore.getOptions().get(OPTION_ResourceCopyFilter);
	this.resourceFilters = filterSequence != null && filterSequence.length() > 0
		? CharOperation.splitOn(',', filterSequence.toCharArray())
		: null;
		
	// Flush the existing external files cache if this is the beginning of a build cycle
	String projectName = this.currentProject.getName();
	if (builtProjects == null || builtProjects.contains(projectName)) {
		JavaModel.flushExternalFileCache();
		builtProjects = new ArrayList();
	}
	builtProjects.add(projectName);
}

private void recordNewState(State state) {
	Object[] keyTable = binaryResources.keyTable;
	for (int i = 0, l = keyTable.length; i < l; i++) {
		IProject prereqProject = (IProject) keyTable[i];
		if (prereqProject != null && prereqProject != currentProject) {
			State prereqState = getLastState(prereqProject);
			if (prereqState != null)
				state.recordLastStructuralChanges(prereqProject, prereqState.lastStructuralBuildNumber);
		}
	}

	// state.dump();
	JavaModelManager.getJavaModelManager().setLastBuiltState(currentProject, state);
}

/**
 * String representation for debugging purposes
 */
public String toString() {
	return "JavaBuilder for " + currentProject.getName(); //$NON-NLS-1$
}
}
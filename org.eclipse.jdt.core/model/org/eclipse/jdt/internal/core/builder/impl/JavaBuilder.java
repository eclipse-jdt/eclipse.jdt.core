package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.*;

import java.io.*;
import java.util.*;

/**
 * The Java Image Builder, which is a VA/Base builder.
 */
public class JavaBuilder extends IncrementalProjectBuilder {

	/**
	 * Flag indicating whether to persist built states between sessions.
	 */
	public static final boolean SAVE_ENABLED = true;
	/**
	 * Constructs a new Java Builder.
	 */
	public JavaBuilder() {
	}

	/**
	 * Run the Java Image Builder.
	 */
	protected IProject[] build(int kind, Map map, IProgressMonitor monitor)
		throws CoreException {

		if (!this.getProject().exists())
			return new IProject[0];
		//if (!((JavaProject)getJavaProject()).hasSource()) return new IProject[0];

		JavaDevelopmentContextImpl dc = getDevelopmentContext();
		dc.setProgressMonitor(monitor);
		boolean ok = false;
		try {
			if (kind == FULL_BUILD) {
				fullBuild(dc, monitor);
			} else {
				Hashtable deltas = checkIncrementalBuild(monitor);
				if (deltas == null) {
					fullBuild(dc, monitor);
				} else {
					incrementalBuild(dc, deltas, monitor);
				}
			}
			ok = true;
		} catch (ImageBuilderInternalException e) {
			// Fix for 1FW2XY6: ITPJCORE:ALL - Image builder wrappers CoreException
			if (e.getThrowable() instanceof CoreException) {
				throw (CoreException) e.getThrowable();
			} else {
				throw new CoreException(
					new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, -1, "Java Builder", e));
			}
		} catch (OperationCanceledException e) {
			// Do nothing for now, and avoid propagating the exception.  
			// The finally block ensures we will do a full build next time.
			// See 1FVJ5Z8: ITPCORE:ALL - How should builders handle cancel?
		} finally {
			// Don't let DC hang onto progress monitor.
			dc.setProgressMonitor(null);
			if (!ok) {
				// If the build failed, clear out the previously built state,
				// forcing a full build next time.
				setLastBuiltState(null);
			}
			if (monitor != null)
				monitor.subTask("Java build completed");
		}
		return getRequiredProjects(getLastBuiltState(monitor));
	}

	/**
	 * Checks whether we can do an incremental build.
	 * Returns a hashtable containing all the required deltas if yes, null if no.
	 *
	 * @return hashtable mapping from IProject to IResourceDelta for all required project deltas, or null
	 */
	protected Hashtable checkIncrementalBuild(IProgressMonitor monitor)
		throws CoreException {
		IState oldState = getLastBuiltState(monitor);
		if (oldState == null) {
			//System.out.println("No previous built state for: "+getProject().getName());			
			return null;
		}
		Hashtable deltas = new Hashtable(11);
		IProject project = getProject();

		if (monitor != null)
			monitor.subTask(
				"Reading resource change information for :" + project.getName());
		IResourceDelta delta = getDelta(project);
		if (delta == null) {
			//System.out.println("Missing delta for: "+ project.getName());			
			if (monitor != null)
				monitor.subTask("");
			return null;
		} else {
			deltas.put(project, delta);
		}
		IProject[] prereqs = getRequiredProjects(oldState);
		for (int i = 0; i < prereqs.length; ++i) {
			if (monitor != null)
				monitor.subTask(
					"Reading resource change information for :" + prereqs[i].getName());
			delta = getDelta(prereqs[i]);
			if (delta == null) {
				//System.out.println("Missing delta for: "+ prereqs[i].getName());			
				if (monitor != null)
					monitor.subTask("");
				return null;
			} else {
				deltas.put(prereqs[i], delta);
			}
		}
		if (monitor != null)
			monitor.subTask("");
		return deltas;
	}

	/**
	 * Returns true if the class path has changed since the last built state.
	 */
	protected boolean classpathChanged(IState lastBuiltState)
		throws CoreException {
		try {
			IPackageFragmentRoot[] oldRoots =
				((StateImpl) lastBuiltState).getPackageFragmentRootsInClassPath();
			IPackageFragmentRoot[] newRoots =
				((JavaProject) getJavaProject()).getBuilderRoots(null);
			return !Util.equalArraysOrNull(oldRoots, newRoots);
		} catch (JavaModelException e) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					JavaCore.PLUGIN_ID,
					Platform.PLUGIN_ERROR,
					"Project " + getProject().getFullPath() + " not present",
					e));
		}
	}

	/**
	 * Runs a full build.
	 */
	protected void fullBuild(
		JavaDevelopmentContextImpl dc,
		IProgressMonitor monitor)
		throws CoreException {
		IProject project = getProject();
		//System.out.println("FULL build of: "+project.getName());			

		/* create problem reporter and clear all problems */
		IProblemReporter problemReporter = new MarkerProblemReporter(project, dc);
		problemReporter.removeProblems(project);

		/* create and invoke the batch builder */
		// Pass the compiler options, needed for 1FVXS80: ITPJCORE:ALL - .class files are missing their LocalVariableTable
		ConfigurableOption[] options =
			JavaModelManager.convertConfigurableOptions(JavaCore.getOptions());
		setLastBuiltState(null); // free possible existing state
		IImageBuilder builder = dc.createState(project, null, problemReporter, options);
		setLastBuiltState(builder.getNewState());
	}

	/**
	 * Returns the development context to use for this builder.
	 */
	protected JavaDevelopmentContextImpl getDevelopmentContext() {
		return (JavaDevelopmentContextImpl) JavaModelManager
			.getJavaModelManager()
			.getDevelopmentContext(getProject());
	}

	/**
	 * Returns the Java view of the project.
	 */
	protected IJavaProject getJavaProject() {
		return JavaCore.create(getProject());
	}

	/**
	 * Returns the last built state for this builder.
	 */
	protected IState getLastBuiltState(IProgressMonitor monitor) {
		return JavaModelManager.getJavaModelManager().getLastBuiltState(
			getProject(),
			monitor);
	}

	/**
	 * Returns a problem factory for the given locale.
	 */
	public IProblemFactory getProblemFactory(Locale locale) {
		return ProblemFactory.getProblemFactory(locale);
	}

	/**
	 * Returns the prerequisite projects for the given built state.
	 * Returns an empty array if state is null.
	 *
	 * @param the state or null
	 */
	protected IProject[] getRequiredProjects(IState state) {
		if (state == null) {
			return new IProject[0];
		}
		// This must not assume the given state is the current one.
		// It may be the old state.
		IProject project = getProject();
		IProject[] all = ((StateImpl) state).getClassPathProjects();
		Vector v = new Vector();
		for (int i = 0; i < all.length; ++i) {
			if (!all[i].equals(project)) {
				v.addElement(all[i]);
			}
		}
		IProject[] result = new IProject[v.size()];
		v.copyInto(result);
		return result;
	}

	/**
	 * Runs an incremental build.
	 *
	 * @param deltas maps from IProject to IResourceDelta for this builder's project and all prerequisite projects
	 */
	protected void incrementalBuild(
		JavaDevelopmentContextImpl dc,
		Hashtable deltas,
		IProgressMonitor monitor)
		throws CoreException {
		IProject project = getProject();
		//System.out.println("INCREMENTAL build of: "+project.getName());		
		StateImpl oldState = (StateImpl) getLastBuiltState(monitor);
		if (needIncrementalBuild(oldState, deltas)) {
			IncrementalImageBuilder builder =
				new IncrementalImageBuilder(oldState, project, null);
			builder.applySourceDelta(deltas);
			setLastBuiltState(builder.getNewState());
		} else {
			/* Still update resources in binary output */
			BuildNotifier notifier =
				new BuildNotifier((JavaDevelopmentContextImpl) dc, false);
			notifier.begin();
			try {
				ProjectResourceCopier copier =
					new ProjectResourceCopier(getJavaProject(), dc, notifier, 1.0f);
				IResourceDelta change = (IResourceDelta) deltas.get(project);
				copier.updateAffectedResources(change);
			} finally {
				notifier.done();
			}
		}
	}

	/**
	 * Checks whether this is an empty delta as far as the builder is concerned.
	 */
	protected boolean isEmpty(IResourceDelta change) {
		// Made checks more selective for 1FW1S0Y: ITPJCORE:ALL - Java builder builds when non-Java files affected
		if (change == null)
			return true;
		int kind = change.getKind();
		boolean isAdded = kind == IResourceDelta.ADDED;
		boolean isRemoved = kind == IResourceDelta.REMOVED;
		boolean isChanged = kind == IResourceDelta.CHANGED;
		int flags = change.getFlags();
		boolean contentChanged = isChanged && (flags & IResourceDelta.CONTENT) != 0;
		String extension = change.getFullPath().getFileExtension();
		boolean isJavaOrClassFile =
			extension != null
				&& (extension.equalsIgnoreCase("java") || extension.equalsIgnoreCase("class"));
		boolean isArchive =
			extension != null
				&& (extension.equalsIgnoreCase("zip") || extension.equalsIgnoreCase("jar"));

		// care about added, removed and modified (content actually modified) .java and .class files
		if (isJavaOrClassFile && (isAdded || isRemoved || contentChanged))
			return false;
		// care about added, removed and modified (content actually modified) .jar and .zip files
		if (isArchive && (isAdded || isRemoved || contentChanged))
			return false;

		// care about all folder additions and removals since they may represent package fragments
		IResource resource = change.getResource();
		if (resource != null) {
			int type = resource.getType();
			// may have been a container previously if its type is changing
			boolean isFolder =
				(flags & IResourceDelta.TYPE) != 0 || type == IResource.FOLDER;
			if (isFolder && (isAdded || isRemoved))
				return false;
		}
		// recurse on children
		IResourceDelta[] children = change.getAffectedChildren();
		for (int i = 0; i < children.length; ++i) {
			if (!isEmpty(children[i]))
				return false;
		}
		return true;
	}

	/**
	 * Checks whether an incremental build is really necessary.
	 */
	protected boolean needIncrementalBuild(StateImpl oldState, Hashtable deltas)
		throws CoreException {
		if (classpathChanged(oldState)) {
			return true;
		}
		for (Enumeration e = deltas.elements(); e.hasMoreElements();) {
			IResourceDelta delta = (IResourceDelta) e.nextElement();
			if (!isEmpty(delta)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if the output location has changed since the last built state.
	 */
	protected boolean outputLocationChanged(IState lastBuiltState)
		throws CoreException {
		try {
			IPath oldOutputLocation = ((StateImpl) lastBuiltState).getOutputLocation();
			IPath newOutputLocation = getJavaProject().getOutputLocation();
			return !oldOutputLocation.equals(newOutputLocation);
		} catch (JavaModelException e) {
			throw new CoreException(
				new Status(
					IStatus.ERROR,
					JavaCore.PLUGIN_ID,
					Platform.PLUGIN_ERROR,
					"Project " + getProject().getFullPath() + " not present",
					e));
		}
	}

	protected void setLastBuiltState(IState state) {
		JavaModelManager.getJavaModelManager().setLastBuiltState(getProject(), state);
	}

	/**
	 * String representation for debugging purposes
	 */
	public String toString() {
		IState lastBuiltState = getLastBuiltState(null);
		if (lastBuiltState == null) {
			return "JavaBuilder(no built state)";
		} else {
			return "JavaBuilder(" + lastBuiltState + ")";
		}
	}

}

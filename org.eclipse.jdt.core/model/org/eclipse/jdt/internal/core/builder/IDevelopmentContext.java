package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This maintains a description of an image.  It has the following responsibilities:
 * <dl>
 *	 <dd> - given the source for a system, build a representation of the image which it describes
 *	 <dd> - provide navigational access over the image
 *	 <dd> - check the consistency of the system and report any problems in the form of a 'report card'
 *	 <dd> - given a source delta describing changes to the source, incrementally build 
 *		 an updated representation of the image and report card
 *	 <dd> - when a delta is applied, also output an image delta describing changes to the image
 * </dl>
 *
 */
public interface IDevelopmentContext {

	/**
	 * Adds an IBuildListener which is notified of builder-specific
	 * information during a batch or incremental build.
	 */
	public void addBuildListener(IBuildListener buildListener);
	/**
	 * Builds a new state representing the image described by a project.
	 * Returns an <code>ImageBuilder</code> that will build a representation
	 * of the image and answer a new state.
	 * The current state is not affected.
	 * A build context is given, representing the subset of the
	 * image which is important to have built as early as possible.
	 *
	 * <p> N.B. The image builder may read from the project at any time,
	 * not just when a build is initiated.  Any exceptions encountered during
	 * reading are propagated to the client by the image builder.
	 * The image builder guarantees that its internal structures
	 * are not corrupted by such occurrences.
	 *
	 * <p> There are a few operations in the image builder API that are 
	 * independent of the workspace:
	 * <dl>
	 * <dd> handle-only methods on <code>IHandle</code> and subclasses
	 * <dd> <code>equal()</code>, 
	 *		<code>toString()</code>
	 *		and <code>hashCode()</code> methods
	 * <dd> <code>DeltaKey</code> operations
	 * <dd> <code>IImageContext</code> operations
	 * <dd> <code>ISourceFragment</code> operations
	 * <dd> <code>IState.getCurrentState()</code>,
	 *		<code>setCurrentState()</code> 
	 *		and <code>getImage()</code> 
	 * </dl>
	 * Clients must assume that this exception could be thrown at any time by 
	 * any other operation defined in the image builder API.
	 *
	 * @param project the project which provides source for the state
	 * @see IState#applySourceDelta
	 * @see IState#getBuildContext
	 */
	IImageBuilder createState(IProject project, IImageContext buildContext);
	/**
	 * Builds a new state representing the image described by a project.
	 * Returns an <code>ImageBuilder</code> that will build a representation
	 * of the image and answer a new state.
	 * The current state is not affected.
	 * A build context is given, representing the subset of the
	 * image which is important to have built as early as possible.
	 *
	 * <p> N.B. The image builder may read from the project at any time,
	 * not just when a build is initiated.  Any exceptions encountered during
	 * reading are propagated to the client by the image builder.
	 * The image builder guarantees that its internal structures
	 * are not corrupted by such occurrences.
	 *
	 * <p> Compilation and build problems encountered during the image building
	 * are reported using the given <code>IProblemReporter</code>. The report
	 * card will use this problem reporter. 
	 *
	 * <p> There are a few operations in the image builder API that are 
	 * independent of the workspace:
	 * <dl>
	 * <dd> handle-only methods on <code>IHandle</code> and subclasses
	 * <dd> <code>equal()</code>, 
	 *		<code>toString()</code>
	 *		and <code>hashCode()</code> methods
	 * <dd> <code>DeltaKey</code> operations
	 * <dd> <code>IImageContext</code> operations
	 * <dd> <code>ISourceFragment</code> operations
	 * <dd> <code>IState.getCurrentState()</code>,
	 *		<code>setCurrentState()</code> 
	 *		and <code>getImage()</code> 
	 * </dl>
	 * Clients must assume that this exception could be thrown at any time by 
	 * any other operation defined in the image builder API.
	 *
	 * @param project the project which provides source for the state
	 * @see IState#applySourceDelta
	 * @see IState#getBuildContext
	 */
	IImageBuilder createState(
		IProject project,
		IImageContext buildContext,
		IProblemReporter problemReporter);
	/**
	 * Compare two Objects for equality.  Returns true iff they represent the
	 * same development context.
	 */
	boolean equals(Object obj);
	/**
	 * Garbage collect any resources maintained by the development
	 * context which are no longer needed, given the states which
	 * are still in use.
	 *
	 * This releases any binaries in the binary broker
	 * which aren't needed for the given states.
	 */
	void garbageCollect(IState[] statesInUse);
	/**
	 * Returns the binary broker which maintains the binaries for types which are built
	 * by this development context.
	 */
	IBinaryBroker getBinaryBroker();
	/**
	 * Returns the build monitor.
	 */
	IBuildMonitor getBuildMonitor();
	/**
	 * Returns the current state of the development context.
	 *
	 * @exception NotPresentException when no current state has been set. */
	IState getCurrentState() throws NotPresentException;
	/**
	 * Returns the image described by the current state.
	 * The result is non-state-specific.
	 *
	 * @see IState
	 */
	IImage getImage();
	/**
	 * Returns the progress monitor set by setProgressMonitor(...) or null
	 * if none has been set.
	 */
	public IProgressMonitor getProgressMonitor();
	/**
	 * Removes a IBuildListener which is notified of builder-specific
	 * information during a batch or incremental build.
	 */
	public void removeBuildListener(IBuildListener buildListener);
	/**
	 * Returns a new state of this development context by restoring a 
	 * previously-saved one from the given input. Since
	 * development context states are saved without their project, 
	 * the correct project also must be supplied. 
	 * The results are undefined if this project does not have
	 * the same content as the one from which the state was built.
	 * Note: the restored state is -not- made to be the current state.
	 * The current state of the development context is unaffected.
	 *
	 * @exception IOException If there are problems reading or 
	 * 				restoring the object
	 * @see #saveState
	 */
	IState restoreState(IProject project, java.io.DataInputStream in)
		throws java.io.IOException;
	/**
	 * Save the contents of the given state of this development context 
	 * to the given output. The state's workspace must be saved
	 * separately, and reassociated with the state when it is restored. 
	 * Because of this quirk, this class does not implement 
	 * <code>java.io.Externalizable</code>.
		 * @exception NotPresentException when no current state has been set.
		 * @exception java.io.IOException Includes any I/O exceptions that may occur
	 * @see #restoreState
	 */
	void saveState(IState state, java.io.DataOutputStream out)
		throws java.io.IOException;
	/**
	 * Sets the binary broker which maintains the binaries for types which are built
	 * by this development context.
	 */
	void setBinaryBroker(IBinaryBroker broker);
	/**
	 * Sets the build monitor.
	 */
	void setBuildMonitor(IBuildMonitor monitor);
	/**
	 * Sets the current state of the development context.
	 *
	 * @param state the state to set as the current state
	 */
	void setCurrentState(IState state);
	/**
	 * Sets the build progress monitor for this development context.
	 * If monitor is <code>null</code>, deregister any existing one.
	 * The build progress monitor is notified of the image building
	 * activities done on any state of this development context.
	 *
	 * For batch builds and incremental builds, the builder periodically
	 * checks the <code>isCanceled()</code> status on the progress monitor.
	 * If set, it stops the build and throws <code>BuildCanceledException</code>.
	 * It does not check the <code>isCanceled()</code> status for background or
	 * lazy builds.
	 */
	public void setProgressMonitor(IProgressMonitor monitor);
	/**
	 * Return a string of the form:
	 * 		JDC#?
	 * where ? is the instance number of the DevelopmentContext.
	 * The string returned is only for debugging purposes,
	 * and the contents of the string may change in the future.
	 * @return java.lang.String
	 */
	public String toString();
}

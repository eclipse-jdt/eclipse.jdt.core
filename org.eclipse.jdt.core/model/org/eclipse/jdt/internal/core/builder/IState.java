package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceDelta;

/**
 * This represents a specific, immutable state of the image described by the Development Context.
 */
public interface IState {
	/**
	 * Performs an incremental build, given a new project
	 * and a delta describing the differences between
	 * the project for this state and the new project.
	 * Returns an ImageBuilder representing the process of building
	 * the new state.  The result object can be queried for the
	 * new state and for the image delta between this state and
	 * the new state.
	 * 
	 * <p> The given delta must represent the differences
	 * between the project for this state and the new project.
	 *
	 * <p> Optionally, the build context for the new state can be
	 * specified.  If null is specified, this state's build context 
	 * is given to the new state.
	 *
	 * @see #getBuildContext
	 */
	IImageBuilder applySourceDelta(
		IProject newProject,
		IResourceDelta delta,
		IImageContext buildContext);
	/**
	 * Compare two Objects for equality.  Returns true iff they represent the same state
	 * in the same development context. 
	 *
	 * @see IDevelopmentContext
	 */
	boolean equals(Object obj);
	/**
	 * Returns the ImageContext representing the subset of the
	 * image which is important to have built as early
	 * as possible.  Although all parts of the image can be navigated
	 * to and queried, possibly using other ImageContexts, the builder
	 * gives higher priority to maintaining the build context subset
	 * than to other parts of the image.
	 * 
	 * @see #applySourceDelta
	 */
	IImageContext getBuildContext();
	/**
	 * Returns the dependency graph for this state, if supported,
	 * or null if the dependency graph is unknown.
	 */
	IDependencyGraph getDependencyGraph();
	/**
	 * Returns this state's development context.
	 */
	IDevelopmentContext getDevelopmentContext();
	/**
	 * Answer a unique fingerprint for this state.
	 * It is guaranteed that no other state can have the same 
	 * fingerprint.
	 * The result should not be modified.
	 */
	byte[] getFingerprint();
	/**
	 * Returns the image described by this state.  The result is state-specific. 
	 *
	 * @see IDevelopmentContext
	 * @see IDevelopmentContext#getImage
	 */
	IImage getImage();
	/**
	 * Returns the project which provides the source for this state.
	 */
	IProject getProject();
	/**
	 * Returns a report card for this state., restricted to
	 * the given image context.
	 * Problems are organized by workspace element identifiers.
	 * This method is on <code>IState</code> rather than 
	 * <code>IImage</code> to make it clear that
	 * the result is inherently state-specific.
	 * @param imageContext the image context in which to 
	 *    restrict the report card.
	 */
	IReportCard getReportCard(IImageContext imageContext);
	/**
	 * Returns a new image delta representing the differences between the this
	 * state and another one.  The delta is naive in that no delta information is initially provided.
	 * Only the portion of the states within the given image context are examined.
	 */
	IDelta newNaiveDeltaWith(IState otherState, IImageContext imgCtx);
}

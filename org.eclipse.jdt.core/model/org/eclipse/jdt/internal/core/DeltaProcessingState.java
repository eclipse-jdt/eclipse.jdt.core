/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.resources.*;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Keep the global states used during Java element delta processing.
 */
public class DeltaProcessingState implements IResourceChangeListener {
	
	/*
	 * Collection of listeners for Java element deltas
	 */
	public IElementChangedListener[] elementChangedListeners = new IElementChangedListener[5];
	public int[] elementChangedListenerMasks = new int[5];
	public int elementChangedListenerCount = 0;

	/*
	 * The delta processor for the current thread.
	 */
	private ThreadLocal deltaProcessors = new ThreadLocal();
	
	/* A table from IPath (from a classpath entry) to RootInfo */
	public HashMap roots = new HashMap();
	
	/* A table from IPath (from a classpath entry) to ArrayList of RootInfo
	 * Used when an IPath corresponds to more than one root */
	public HashMap otherRoots = new HashMap();
	
	/* A table from IPath (from a classpath entry) to RootInfo
	 * from the last time the delta processor was invoked. */
	public HashMap oldRoots = new HashMap();
	
	/* A table from IPath (from a classpath entry) to ArrayList of RootInfo
	 * from the last time the delta processor was invoked.
	 * Used when an IPath corresponds to more than one root */
	public HashMap oldOtherRoots = new HashMap();
	
	/* A table from IPath (a source attachment path from a classpath entry) to IPath (a root path) */
	public HashMap sourceAttachments = new HashMap();

	/* Whether the roots tables should be recomputed */
	public boolean rootsAreStale = true;
	
	/* Threads that are currently running initializeRoots() */
	private Set initializingThreads = Collections.synchronizedSet(new HashSet());	
	
	public Hashtable externalTimeStamps = new Hashtable();

	/**
	 * Workaround for bug 15168 circular errors not reported  
	 * This is a cache of the projects before any project addition/deletion has started.
	 */
	public IJavaProject[] modelProjectsCache;
		
	/*
	 * Need to clone defensively the listener information, in case some listener is reacting to some notification iteration by adding/changing/removing
	 * any of the other (for example, if it deregisters itself).
	 */
	public void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		for (int i = 0; i < this.elementChangedListenerCount; i++){
			if (this.elementChangedListeners[i].equals(listener)){
				
				// only clone the masks, since we could be in the middle of notifications and one listener decide to change
				// any event mask of another listeners (yet not notified).
				int cloneLength = this.elementChangedListenerMasks.length;
				System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[cloneLength], 0, cloneLength);
				this.elementChangedListenerMasks[i] = eventMask; // could be different
				return;
			}
		}
		// may need to grow, no need to clone, since iterators will have cached original arrays and max boundary and we only add to the end.
		int length;
		if ((length = this.elementChangedListeners.length) == this.elementChangedListenerCount){
			System.arraycopy(this.elementChangedListeners, 0, this.elementChangedListeners = new IElementChangedListener[length*2], 0, length);
			System.arraycopy(this.elementChangedListenerMasks, 0, this.elementChangedListenerMasks = new int[length*2], 0, length);
		}
		this.elementChangedListeners[this.elementChangedListenerCount] = listener;
		this.elementChangedListenerMasks[this.elementChangedListenerCount] = eventMask;
		this.elementChangedListenerCount++;
	}

	public DeltaProcessor getDeltaProcessor() {
		DeltaProcessor deltaProcessor = (DeltaProcessor)this.deltaProcessors.get();
		if (deltaProcessor != null) return deltaProcessor;
		deltaProcessor = new DeltaProcessor(this, JavaModelManager.getJavaModelManager());
		this.deltaProcessors.set(deltaProcessor);
		return deltaProcessor;
	}

	public void initializeRoots() {
		
		// recompute root infos only if necessary
		HashMap newRoots = null;
		HashMap newOtherRoots = null;
		HashMap newSourceAttachments = null;
		if (this.rootsAreStale) {
			Thread currentThread = Thread.currentThread();
			boolean addedCurrentThread = false;			
			try {
				// if reentering initialization (through a container initializer for example) no need to compute roots again
				// see https://bugs.eclipse.org/bugs/show_bug.cgi?id=47213
				if (!this.initializingThreads.add(currentThread)) return;
				addedCurrentThread = true;

				newRoots = new HashMap();
				newOtherRoots = new HashMap();
				newSourceAttachments = new HashMap();
		
				IJavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
				IJavaProject[] projects;
				try {
					projects = model.getJavaProjects();
				} catch (JavaModelException e) {
					// nothing can be done
					return;
				}
				for (int i = 0, length = projects.length; i < length; i++) {
					JavaProject project = (JavaProject) projects[i];
					IClasspathEntry[] classpath;
					try {
						classpath = project.getResolvedClasspath(true);
					} catch (JavaModelException e) {
						// continue with next project
						continue;
					}
					for (int j= 0, classpathLength = classpath.length; j < classpathLength; j++) {
						IClasspathEntry entry = classpath[j];
						if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT) continue;
						
						// root path
						IPath path = entry.getPath();
						if (newRoots.get(path) == null) {
							newRoots.put(path, new DeltaProcessor.RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry.getEntryKind()));
						} else {
							ArrayList rootList = (ArrayList)newOtherRoots.get(path);
							if (rootList == null) {
								rootList = new ArrayList();
								newOtherRoots.put(path, rootList);
							}
							rootList.add(new DeltaProcessor.RootInfo(project, path, ((ClasspathEntry)entry).fullInclusionPatternChars(), ((ClasspathEntry)entry).fullExclusionPatternChars(), entry.getEntryKind()));
						}
						
						// source attachment path
						if (entry.getEntryKind() != IClasspathEntry.CPE_LIBRARY) continue;
						QualifiedName qName = new QualifiedName(JavaCore.PLUGIN_ID, "sourceattachment: " + path.toOSString()); //$NON-NLS-1$;
						String propertyString = null;
						try {
							propertyString = ResourcesPlugin.getWorkspace().getRoot().getPersistentProperty(qName);
						} catch (CoreException e) {
							continue;
						}
						IPath sourceAttachmentPath;
						if (propertyString != null) {
							int index= propertyString.lastIndexOf(PackageFragmentRoot.ATTACHMENT_PROPERTY_DELIMITER);
							sourceAttachmentPath = (index < 0) ?  new Path(propertyString) : new Path(propertyString.substring(0, index));
						} else {
							sourceAttachmentPath = entry.getSourceAttachmentPath();
						}
						if (sourceAttachmentPath != null) {
							newSourceAttachments.put(sourceAttachmentPath, path);
						}
					}
				}
			} finally {
				if (addedCurrentThread) {
					this.initializingThreads.remove(currentThread);
				}
			}
		}
		synchronized(this) {
			this.oldRoots = this.roots;
			this.oldOtherRoots = this.otherRoots;			
			if (this.rootsAreStale && newRoots != null) { // double check again
				this.roots = newRoots;
				this.otherRoots = newOtherRoots;
				this.sourceAttachments = newSourceAttachments;
				this.rootsAreStale = false;
			}
		}
	}

	public void removeElementChangedListener(IElementChangedListener listener) {
		
		for (int i = 0; i < this.elementChangedListenerCount; i++){
			
			if (this.elementChangedListeners[i].equals(listener)){
				
				// need to clone defensively since we might be in the middle of listener notifications (#fire)
				int length = this.elementChangedListeners.length;
				IElementChangedListener[] newListeners = new IElementChangedListener[length];
				System.arraycopy(this.elementChangedListeners, 0, newListeners, 0, i);
				int[] newMasks = new int[length];
				System.arraycopy(this.elementChangedListenerMasks, 0, newMasks, 0, i);
				
				// copy trailing listeners
				int trailingLength = this.elementChangedListenerCount - i - 1;
				if (trailingLength > 0){
					System.arraycopy(this.elementChangedListeners, i+1, newListeners, i, trailingLength);
					System.arraycopy(this.elementChangedListenerMasks, i+1, newMasks, i, trailingLength);
				}
				
				// update manager listener state (#fire need to iterate over original listeners through a local variable to hold onto
				// the original ones)
				this.elementChangedListeners = newListeners;
				this.elementChangedListenerMasks = newMasks;
				this.elementChangedListenerCount--;
				return;
			}
		}
	}

	public void resourceChanged(IResourceChangeEvent event) {
		try {
			getDeltaProcessor().resourceChanged(event);
		} finally {
			// TODO (jerome) see 47631, may want to get rid of following so as to reuse delta processor ? 
			if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
				this.deltaProcessors.set(null);
			}
		}

	}

	/*
	 * Update the roots that are affected by the addition or the removal of the given container resource.
	 */
	public synchronized void updateRoots(IPath containerPath, IResourceDelta containerDelta, DeltaProcessor deltaProcessor) {
		Map updatedRoots;
		Map otherUpdatedRoots;
		if (containerDelta.getKind() == IResourceDelta.REMOVED) {
			updatedRoots = this.oldRoots;
			otherUpdatedRoots = this.oldOtherRoots;
		} else {
			updatedRoots = this.roots;
			otherUpdatedRoots = this.otherRoots;
		}
		Iterator iterator = updatedRoots.keySet().iterator();
		while (iterator.hasNext()) {
			IPath path = (IPath)iterator.next();
			if (containerPath.isPrefixOf(path) && !containerPath.equals(path)) {
				IResourceDelta rootDelta = containerDelta.findMember(path.removeFirstSegments(1));
				if (rootDelta == null) continue;
				DeltaProcessor.RootInfo rootInfo = (DeltaProcessor.RootInfo)updatedRoots.get(path);
	
				if (!rootInfo.project.getPath().isPrefixOf(path)) { // only consider roots that are not included in the container
					deltaProcessor.updateCurrentDeltaAndIndex(rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
				}
				
				ArrayList rootList = (ArrayList)otherUpdatedRoots.get(path);
				if (rootList != null) {
					Iterator otherProjects = rootList.iterator();
					while (otherProjects.hasNext()) {
						rootInfo = (DeltaProcessor.RootInfo)otherProjects.next();
						if (!rootInfo.project.getPath().isPrefixOf(path)) { // only consider roots that are not included in the container
							deltaProcessor.updateCurrentDeltaAndIndex(rootDelta, IJavaElement.PACKAGE_FRAGMENT_ROOT, rootInfo);
						}
					}
				}
			}
		}
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;

/*
 * Abstract class for operations that change the classpath
 */
public abstract class ChangeClasspathOperation extends JavaModelOperation {

	protected boolean canChangeResources;

	public ChangeClasspathOperation(IJavaElement[] elements, boolean canChangeResources) {
		super(elements);
		this.canChangeResources = canChangeResources;
	}

	protected boolean canModifyRoots() {
		// changing the classpath can modify roots
		return true;
	}
	
	/*
	 * The resolved classpath of the given project may have changed:
	 * - generate a delta
	 * - trigger indexing
	 * - update project references
	 * - create resolved classpath markers
	 */
	protected void classpathChanged(JavaProject project) throws JavaModelException {
		DeltaProcessingState state = JavaModelManager.getJavaModelManager().deltaState;
		DeltaProcessor deltaProcessor = state.getDeltaProcessor();
		ClasspathChange change = (ClasspathChange) deltaProcessor.classpathChanges.get(project.getProject());
		if (this.canChangeResources) {
			// delta, indexing and classpath markers are going to be created by the delta processor 
			// while handling the .classpath file change

			// however ensure project references are updated
			// since some clients rely on the project references when run inside an IWorkspaceRunnable
			new ProjectReferenceChange(project, change.oldResolvedClasspath).updateProjectReferencesIfNecessary();
		} else {
			JavaElementDelta delta = new JavaElementDelta(getJavaModel());
			if (change.generateDelta(delta)) {
				// create delta
				addDelta(delta);
				
				// ensure indexes are updated
				change.requestIndexing();
				
				// ensure classpath is validated on next build
				state.addClasspathValidation(project);
		
				// ensure project references are updated on next build
				state.addProjectReferenceChange(project, change.oldResolvedClasspath);
			}
		}
	}

	protected ISchedulingRule getSchedulingRule() {
		return null; // no lock taken while changing classpath
	}
	
	public boolean isReadOnly() {
		return !this.canChangeResources;
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.processing;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IJob {

	/* Waiting policies */
	int ForceImmediate = 1;
	int CancelIfNotReady = 2;
	int WaitUntilReady = 3;

	/* Job's result */
	boolean FAILED = false;
	boolean COMPLETE = true;

	/**
	 * Answer true if the job belongs to a given family (tag)
	 */
	public boolean belongsTo(String jobFamily);
	/**
	 * Asks this job to cancel its execution. The cancellation
	 * can take an undertermined amount of time.
	 */
	public void cancel();
	/**
	 * Execute the current job, answer whether it was successful.
	 */
	public boolean execute(IProgressMonitor progress);
	/**
	 * Answer whether the job is ready to run.
	 */
	public boolean isReadyToRun();
}

package org.eclipse.jdt.internal.core.search.processing;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.*;

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
	 * Execute the current job, answering:
	 *      RESCHEDULE if the job should be rescheduled later on
	 *      COMPLETE if the job is over
	 */
	public boolean execute(IProgressMonitor progress);
}
package org.eclipse.jdt.internal.core.search.processing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import java.io.*;
import java.util.*;

public abstract class JobManager implements Runnable, IJobConstants {

	/* queue of jobs to execute */
	protected IJob[] awaitingJobs = new IJob[10];
	protected int jobStart = 0;
	protected int jobEnd = -1;
	protected boolean executing = false;

	/* background processing */
	protected Thread thread;

	/* flag indicating whether job execution is enabled or not */
	private boolean enabled = true;

	public static boolean VERBOSE = false;
	/**
	 * Invoked exactly once, in background, before starting processing any job
	 */
	public abstract void activateProcessing();
	/**
	 * Answer the amount of awaiting jobs.
	 */
	public synchronized int awaitingJobsCount() {

		return jobEnd - jobStart + 1;
	}

	/**
	 * Answers the first job in the queue, or null if there is no job available
	 * Until the job has completed, the job manager will keep answering the same job.
	 */
	public synchronized IJob currentJob() {

		if (!enabled)
			return null;

		if (jobStart <= jobEnd) {
			return awaitingJobs[jobStart];
		}
		return null;
	}

	public synchronized void disable() {
		enabled = false;
	}

	/**
	 * Remove the index from cache for a given project.
	 * Passing null as a job family discards them all.
	 */
	public void discardJobs(String jobFamily) {
		boolean wasEnabled = isEnabled();
		try {
			disable();

			// wait until current job has completed
			while (thread != null && executing) {
				try {
					Thread.currentThread().sleep(50);
				} catch (InterruptedException e) {
				}
			}

			// flush and compact awaiting jobs
			int loc = -1;
			for (int i = jobStart; i <= jobEnd; i++) {
				IJob currentJob = awaitingJobs[i];
				awaitingJobs[i] = null;
				if (!(jobFamily == null
					|| currentJob.belongsTo(jobFamily))) { // copy down, compacting
					awaitingJobs[++loc] = currentJob;
				}
			}
			jobStart = 0;
			jobEnd = loc;
		} finally {
			if (wasEnabled)
				enable();
		}
	}

	public synchronized void enable() {
		enabled = true;
	}

	public synchronized boolean isEnabled() {
		return enabled;
	}

	/**
	 * Advance to the next available job, once the current one has been completed.
	 * Note: clients awaiting until the job count is zero are still waiting at this point.
	 */
	protected synchronized void moveToNextJob() {

		//if (!enabled) return;

		if (jobStart <= jobEnd) {
			awaitingJobs[jobStart++] = null;
			if (jobStart > jobEnd) {
				jobStart = 0;
				jobEnd = -1;
			}
		}
	}

	/**
	 * When idle, give chance to do something
	 */
	protected void notifyIdle(long idlingTime) {
	}

	/**
	 * This API is allowing to run one job in concurrence with background processing.
	 * Indeed since other jobs are performed in background, resource sharing might be 
	 * an issue.Therefore, this functionality allows a given job to be run without
	 * colliding with background ones.
	 * Note: multiple thread might attempt to perform concurrent jobs at the same time,
	 * 			and shoud synchronize (it is deliberately left to clients to decide whether
	 *			concurrent jobs might interfere or not, i.e. multiple read jobs are ok).
	 *
	 * Waiting policy can be:
	 * 		IJobConstants.ForceImmediateSearch
	 * 		IJobConstants.CancelIfNotReadyToSearch
	 * 		IJobConstants.WaitUntilReadyToSearch
	 *
	 */
	public boolean performConcurrentJob(
		IJob searchJob,
		int waitingPolicy,
		IProgressMonitor progress) {

		if (VERBOSE)
			System.out.println("-> performing concurrent job : START - " + searchJob);
		boolean status = FAILED;
		if (awaitingJobsCount() > 0) {
			switch (waitingPolicy) {

				case ForceImmediate :
					if (VERBOSE)
						System.out.println(
							"-> performing concurrent job : NOT READY - ForceImmediate - " + searchJob);
					boolean wasEnabled = isEnabled();
					try {
						disable(); // pause indexing
						status = searchJob.execute();
						if (VERBOSE)
							System.out.println("-> performing concurrent job : END - " + searchJob);
					} finally {
						if (wasEnabled)
							enable();
					}
					return status;
				case CancelIfNotReady :
					if (VERBOSE)
						System.out.println(
							"-> performing concurrent job : NOT READY - CancelIfNotReady - " + searchJob);
					progress.setCanceled(true);
					break;

				case WaitUntilReady :
					int awaitingWork;
					IJob previousJob = null;
					IJob currentJob;
					while ((awaitingWork = awaitingJobsCount()) > 0) {
						if (progress != null && progress.isCanceled())
							throw new OperationCanceledException();
						currentJob = currentJob();
						// currentJob can be null when jobs have been added to the queue but job manager is not enabled
						if (currentJob != null && currentJob != previousJob) {
							if (VERBOSE)
								System.out.println(
									"-> performing concurrent job : NOT READY - WaitUntilReady - " + searchJob);
							if (progress != null) {
								progress.subTask(awaitingWork + " files to index");
							}
							previousJob = currentJob;
						}
						try {
							Thread.currentThread().sleep(50);
						} catch (InterruptedException e) {
						}
					}
			}
		}
		status = searchJob.execute();
		if (VERBOSE)
			System.out.println("-> performing concurrent job : END - " + searchJob);
		return status;
	}

	public abstract String processName();
	public synchronized void request(IJob job) {

		// append the job to the list of ones to process later on
		int size = awaitingJobs.length;
		if (++jobEnd == size) { // when growing, relocate jobs starting at position 0
			jobEnd -= jobStart;
			System.arraycopy(
				awaitingJobs,
				jobStart,
				(awaitingJobs = new IJob[size * 2]),
				0,
				jobEnd);
			jobStart = 0;
		}
		awaitingJobs[jobEnd] = job;
		if (VERBOSE)
			System.out.println("-> requesting job: " + job);

	}

	/**
	 * Flush current state
	 */
	public void reset() {

		if (thread != null) {
			discardJobs(null); // discard all jobs
		} else {
			/* initiate background processing */
			thread = new Thread(this, this.processName());
			thread.setDaemon(true);
			thread.start();
		}
	}

	/**
	 * Infinite loop performing resource indexing
	 */
	public void run() {

		long idlingStart = -1;
		activateProcessing();
		while (true) {
			try {
				IJob job;
				if ((job = currentJob()) == null) {
					if (idlingStart < 0)
						idlingStart = System.currentTimeMillis();
					notifyIdle(System.currentTimeMillis() - idlingStart);
					Thread.currentThread().sleep(500);
					continue;
				} else {
					idlingStart = -1;
				}
				if (VERBOSE) {
					System.out.println("-> executing: " + job);
					System.out.println("\t" + awaitingJobsCount() + " awaiting jobs.");
				}
				try {
					executing = true;
					boolean status = job.execute();
					//if (status == FAILED) request(job);
					moveToNextJob();
				} finally {
					executing = false;
					Thread.currentThread().sleep(50);
				}
			} catch (InterruptedException e) { // background indexing was interrupted
			}
		}
	}

	/**
	 * Stop background processing, and wait until the current job is completed before returning
	 */
	public void shutdown() {

		disable();
		discardJobs(null); // will wait until current executing job has completed
	}

}

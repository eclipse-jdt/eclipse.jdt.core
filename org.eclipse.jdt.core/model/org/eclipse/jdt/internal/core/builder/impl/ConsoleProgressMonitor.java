package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * This progress monitor is used as the default progress monitor in cases
 * where none has been set.  It is used mainly for test cases.  The monitor
 * simply echoes updates to the console (System.out).
 */
class ConsoleProgressMonitor implements IProgressMonitor {
	protected int fTotalWork;
	protected int fWorkDone;
	/**
	 * beginTask method comment.
	 */
	public void beginTask(String name, int totalWork) {
		System.out.println(name + ", total work: " + totalWork);
		fTotalWork = totalWork;
		fWorkDone = 0;
	}

	/**
	 * done method comment.
	 */
	public void done() {
		System.out.println("Task Done");
		fTotalWork = 0;
		fWorkDone = 0;
	}

	public void internalWorked(double work) {
		fWorkDone += work;
		double percent = (double) fWorkDone / (double) fTotalWork;
		percent *= 100;
		System.out.println("\t" + percent + "% work done");
	}

	/**
	 * isCanceled method comment.
	 */
	public boolean isCanceled() {
		return false;
	}

	/**
	 * setCanceled method comment.
	 */
	public void setCanceled(boolean b) {
	}

	/**
	 * setTaskName method comment.
	 */
	public void setTaskName(String name) {
		System.out.println("\t" + name);
	}

	/**
	 * subTask method comment.
	 */
	public void subTask(String name) {
		System.out.println("\t" + name);
	}

	/**
	 * Returns a String that represents the value of this object.
	 * @return a string representation of the receiver
	 */
	public String toString() {
		return "Default ImageBuilder Progress Monitor";
	}

	/**
	 * worked method comment.
	 */
	public void worked(int work) {
		fWorkDone += work;
		double percent = (double) fWorkDone / (double) fTotalWork;
		percent *= 100;
		System.out.println("\t" + percent + "% work done");
	}

}

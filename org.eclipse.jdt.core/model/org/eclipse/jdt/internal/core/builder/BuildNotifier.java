/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core.builder;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.core.Util;

public class BuildNotifier {

protected IProgressMonitor monitor;
protected boolean cancelling;
protected float percentComplete;
protected float progressPerCompilationUnit;
protected int newErrorCount;
protected int fixedErrorCount;
protected int newWarningCount;
protected int fixedWarningCount;
protected int workDone;
protected int totalWork;
protected String previousSubtask;

public static int NewErrorCount = 0;
public static int FixedErrorCount = 0;
public static int NewWarningCount = 0;
public static int FixedWarningCount = 0;

public static void resetProblemCounters() {
	NewErrorCount = 0;
	FixedErrorCount = 0;
	NewWarningCount = 0;
	FixedWarningCount = 0;
}

public BuildNotifier(IProgressMonitor monitor, IProject project) {
	this.monitor = monitor;
	this.cancelling = false;
	this.newErrorCount = NewErrorCount;
	this.fixedErrorCount = FixedErrorCount;
	this.newWarningCount = NewWarningCount;
	this.fixedWarningCount = FixedWarningCount;
	this.workDone = 0;
	this.totalWork = 1000000;
}

/**
 * Notification before a compile that a unit is about to be compiled.
 */
public void aboutToCompile(SourceFile unit) {
	String message = Util.bind("build.compiling", unit.resource.getFullPath().removeLastSegments(1).makeRelative().toString()); //$NON-NLS-1$
	subTask(message);
}

public void begin() {
	if (monitor != null)
		monitor.beginTask("", totalWork); //$NON-NLS-1$
	this.previousSubtask = null;
}

/**
 * Check whether the build has been canceled.
 */
public void checkCancel() {
	if (monitor != null && monitor.isCanceled())
		throw new OperationCanceledException();
}

/**
 * Check whether the build has been canceled.
 * Must use this call instead of checkCancel() when within the compiler.
 */
public void checkCancelWithinCompiler() {
	if (monitor != null && monitor.isCanceled() && !cancelling) {
		// Once the compiler has been canceled, don't check again.
		setCancelling(true);
		// Only AbortCompilation can stop the compiler cleanly.
		// We check cancelation again following the call to compile.
		throw new AbortCompilation(true, null); 
	}
}

/**
 * Notification while within a compile that a unit has finished being compiled.
 */
public void compiled(SourceFile unit) {
	String message = Util.bind("build.compiling", unit.resource.getFullPath().removeLastSegments(1).makeRelative().toString()); //$NON-NLS-1$
	subTask(message);
	updateProgressDelta(progressPerCompilationUnit);
	checkCancelWithinCompiler();
}

public void done() {
	NewErrorCount = this.newErrorCount;
	FixedErrorCount = this.fixedErrorCount;
	NewWarningCount = this.newWarningCount;
	FixedWarningCount = this.fixedWarningCount;

	updateProgress(1.0f);
	subTask(Util.bind("build.done")); //$NON-NLS-1$
	if (monitor != null)
		monitor.done();
	this.previousSubtask = null;
}

/**
 * Returns a string describing the problems.
 */
protected String problemsMessage() {
	int numNew = newErrorCount + newWarningCount;
	int numFixed = fixedErrorCount + fixedWarningCount;
	if (numNew == 0 && numFixed == 0) return ""; //$NON-NLS-1$

	StringBuffer buffer = new StringBuffer();
	buffer.append('(');
	if (numNew == 0) {
		// (Fixed: x errors, y warnings)
		buffer.append(Util.bind("build.fixedHeader")); //$NON-NLS-1$
		buffer.append(' ');
		if (fixedErrorCount > 0) {
			if (fixedErrorCount == 1)
				buffer.append(Util.bind("build.oneError")); //$NON-NLS-1$
			else
				buffer.append(Util.bind("build.multipleErrors", String.valueOf(fixedErrorCount))); //$NON-NLS-1$
			if (fixedWarningCount > 0)
				buffer.append(',').append(' ');
		}
		if (fixedWarningCount > 0) {
			if (fixedWarningCount == 1)
				buffer.append(Util.bind("build.oneWarning")); //$NON-NLS-1$
			else
				buffer.append(Util.bind("build.multipleWarnings", String.valueOf(fixedWarningCount))); //$NON-NLS-1$
		}
	} else if (numFixed == 0) {
		// (Found: x errors, y warnings)
		buffer.append(Util.bind("build.foundHeader")); //$NON-NLS-1$
		buffer.append(' ');
		if (newErrorCount > 0) {
			if (newErrorCount == 1)
				buffer.append(Util.bind("build.oneError")); //$NON-NLS-1$
			else
				buffer.append(Util.bind("build.multipleErrors", String.valueOf(newErrorCount))); //$NON-NLS-1$
			if (newWarningCount > 0)
				buffer.append(',').append(' ');
		}
		if (newWarningCount > 0) {
			if (newWarningCount == 1)
				buffer.append(Util.bind("build.oneWarning")); //$NON-NLS-1$
			else
				buffer.append(Util.bind("build.multipleWarnings", String.valueOf(newWarningCount))); //$NON-NLS-1$
		}
	} else {
		// (Found/fixed: x/y errors, x/y warnings)
		buffer.append(Util.bind("build.foundFixedHeader")); //$NON-NLS-1$
		buffer.append(' ');

		if (newErrorCount > 0 || fixedErrorCount > 0) {
			String plusMinus = String.valueOf(newErrorCount) + "/" + String.valueOf(fixedErrorCount); //$NON-NLS-1$
			buffer.append(Util.bind("build.multipleErrors", plusMinus)); //$NON-NLS-1$
			if (fixedWarningCount > 0 || newWarningCount > 0)
				buffer.append(',').append(' ');
		}
		if (newWarningCount > 0 || fixedWarningCount > 0) {
			String plusMinus = String.valueOf(newWarningCount) + "/" + String.valueOf(fixedWarningCount); //$NON-NLS-1$
			buffer.append(Util.bind("build.multipleWarnings", plusMinus)); //$NON-NLS-1$
		}
	}
	buffer.append(')');
	return buffer.toString();
}

/**
 * Sets the cancelling flag, which indicates we are in the middle
 * of being cancelled.  Certain places (those callable indirectly from the compiler)
 * should not check cancel again while this is true, to avoid OperationCanceledException
 * being thrown at an inopportune time.
 */
public void setCancelling(boolean cancelling) {
	this.cancelling = cancelling;
}

/**
 * Sets the amount of progress to report for compiling each compilation unit.
 */
public void setProgressPerCompilationUnit(float progress) {
	this.progressPerCompilationUnit = progress;
}

public void subTask(String message) {
	String pm = problemsMessage();
	String msg = pm.length() == 0 ? message : pm + " " + message; //$NON-NLS-1$

	if (msg.equals(this.previousSubtask)) return; // avoid refreshing with same one
	//if (JavaBuilder.DEBUG) System.out.println(msg);
	if (monitor != null)
		monitor.subTask(msg);

	this.previousSubtask = msg;
}

protected void updateProblemCounts(IProblem[] newProblems) {
	for (int i = 0, l = newProblems.length; i < l; i++)
		if (newProblems[i].isError()) newErrorCount++; else newWarningCount++;
}

/**
 * Update the problem counts from one compilation result given the old and new problems,
 * either of which may be null.
 */
protected void updateProblemCounts(IMarker[] oldProblems, IProblem[] newProblems) {
	if (newProblems != null) {
		next : for (int i = 0, l = newProblems.length; i < l; i++) {
			IProblem newProblem = newProblems[i];
			if (newProblem.getID() == IProblem.Task) continue; // skip task
			boolean isError = newProblem.isError();
			String message = newProblem.getMessage();

			if (oldProblems != null) {
				for (int j = 0, m = oldProblems.length; j < m; j++) {
					IMarker pb = oldProblems[j];
					if (pb == null) continue; // already matched up with a new problem
					boolean wasError = IMarker.SEVERITY_ERROR
						== pb.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
					if (isError == wasError && message.equals(pb.getAttribute(IMarker.MESSAGE, ""))) { //$NON-NLS-1$
						oldProblems[j] = null;
						continue next;
					}
				}
			}
			if (isError) newErrorCount++; else newWarningCount++;
		}
	}
	if (oldProblems != null) {
		next : for (int i = 0, l = oldProblems.length; i < l; i++) {
			IMarker oldProblem = oldProblems[i];
			if (oldProblem == null) continue next; // already matched up with a new problem
			boolean wasError = IMarker.SEVERITY_ERROR
				== oldProblem.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
			String message = oldProblem.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$

			if (newProblems != null) {
				for (int j = 0, m = newProblems.length; j < m; j++) {
					IProblem pb = newProblems[j];
					if (pb.getID() == IProblem.Task) continue; // skip task
					if (wasError == pb.isError() && message.equals(pb.getMessage()))
						continue next;
				}
			}
			if (wasError) fixedErrorCount++; else fixedWarningCount++;
		}
	}
}

public void updateProgress(float percentComplete) {
	if (percentComplete > this.percentComplete) {
		this.percentComplete = Math.min(percentComplete, 1.0f);
		int work = Math.round(this.percentComplete * this.totalWork);
		if (work > this.workDone) {
			if (monitor != null)
				monitor.worked(work - this.workDone);
			//if (JavaBuilder.DEBUG)
				//System.out.println(java.text.NumberFormat.getPercentInstance().format(this.percentComplete));
			this.workDone = work;
		}
	}
}

public void updateProgressDelta(float percentWorked) {
	updateProgress(percentComplete + percentWorked);
}
}
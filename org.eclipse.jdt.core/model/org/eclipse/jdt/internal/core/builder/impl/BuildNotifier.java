package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;

import java.text.NumberFormat;
import java.util.*;

/**
 * A utility for notifying clients of changes during a build.
 * This includes, progress, build events, etc.
 */
public class BuildNotifier {
	protected JavaDevelopmentContextImpl fDC;
	protected boolean fIsBatch;
	protected IBuildMonitor fBuildMonitor;
	protected IProgressMonitor fProgress;
	protected boolean fCancelling = false;
	protected float fPercentComplete;
	protected float fProgressPerCompilationUnit;
	protected Vector fBuildListeners;
	protected int fNewErrorCount = 0;
	protected int fFixedErrorCount = 0;
	protected int fNewWarningCount = 0;
	protected int fFixedWarningCount = 0;
	protected int fWorkDone = 0;
	protected int fTotalWork;

	public static boolean DEBUG = false;

	protected String previousSubtask;
public BuildNotifier(JavaDevelopmentContextImpl dc, boolean isBatch) {
	fDC = dc;
	fIsBatch = isBatch;
	fProgress = dc.getProgressMonitor();
	fBuildMonitor = dc.getBuildMonitor();
	fBuildListeners = dc.getBuildListeners();
	fTotalWork = 1000000;
}
public void begin() {
	if (fBuildMonitor != null) {
		fBuildMonitor.beginBuild(fIsBatch ? Util.bind("build.beginBatch"/*nonNLS*/) : Util.bind("build.beginIncremental"/*nonNLS*/));
	}
	if (fProgress != null) {
		fProgress.beginTask(""/*nonNLS*/, fTotalWork);
	}
	this.previousSubtask = null;
}
	/**
	 * Check whether the build has been canceled.
	 */
	public void checkCancel() {
		if (fProgress != null && fProgress.isCanceled()) {
			throw new OperationCanceledException();
		}
	}
/**
 * Check whether the build has been canceled.
 * Must use this call instead of checkCancel() when within the compiler.
 */
public void checkCancelWithinCompiler() {
	if (fProgress != null && fProgress.isCanceled() && !fCancelling) {
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
public void compiled(CompilerCompilationUnit unit) {
	compiling(unit);
}
/**
 * Notification while within a compile that a unit is starting to be compiled.
 */
public void compiling(CompilerCompilationUnit unit) {
	String message = new String(unit.getFileName());
	message = message.replace('\\', '/');
	// Trim off project names
	int start = message.indexOf('/', 1);
	int end = message.lastIndexOf('/');
	if (end <= start){
		message = Util.bind("build.compiling"/*nonNLS*/, message.substring(start+1));
	} else {
		message = Util.bind("build.compilingContent"/*nonNLS*/, message.substring(start+1, end));
	}
	subTask(message);
	updateProgressDelta(fProgressPerCompilationUnit/2);
	checkCancelWithinCompiler();
}
public void done() {
	updateProgress(1.0f);
	subTask(Util.bind("build.done"/*nonNLS*/));
	if (fProgress != null) {
		fProgress.done();
	}
	if (fBuildMonitor != null) {
		fBuildMonitor.endBuild(fIsBatch ? Util.bind("build.endBatch"/*nonNLS*/) : Util.bind("build.endIncremental"/*nonNLS*/));
	}
	this.previousSubtask = null;
}
/**
 * Notify listeners that elements have changed.
 */
public void notifyElementsChanged(ConvertedCompilationResult[] results, StateImpl oldState, StateImpl newState) {
	//	if (fBuildListeners.size() == 0) {
	//		return;
	//	}
	for (int i = 0; i < results.length; ++i) {
		ConvertedCompilationResult result = results[i];
		PackageElement element = result.getPackageElement();

		/* notify the build monitor */
		if (fBuildMonitor != null) {
			String typeName = element.getFileName();
			int lastDot = typeName.lastIndexOf('.');
			typeName = typeName.substring(0, lastDot);
			fBuildMonitor.compiled(element.getPackage().getName() + '.' + typeName);
		}
		Vector oldProblems = null;
		Vector newProblems = null;
		if (oldState != null) {
			SourceEntry oldEntry = oldState.getSourceEntry(element);
			if (oldEntry != null) {
				oldProblems = oldState.getProblemReporter().getProblemVector(oldEntry);
			}
		}
		SourceEntry newEntry = newState.getSourceEntry(element);
		if (newEntry != null) {
			newProblems = newState.getProblemReporter().getProblemVector(newEntry);
		}
		updateProblemCounts(oldProblems, newProblems);
		ISourceFragment fragment = (newEntry == null ? null : new SourceFragmentImpl(newEntry));
		BuildEvent event = new BuildEvent(fragment, fNewErrorCount, fFixedErrorCount, fNewWarningCount, fFixedWarningCount);
		for (int j = 0, size = fBuildListeners.size(); j < size; ++j) {
			((IBuildListener) fBuildListeners.elementAt(j)).buildUpdate(event);
		}
	}
}
/**
 * Returns a string describing the problems.
 */
protected String problemsMessage() {
	int numNew = fNewErrorCount + fNewWarningCount;
	int numFixed = fFixedErrorCount + fFixedWarningCount;
	if (numNew == 0 && numFixed == 0) {
		return ""/*nonNLS*/;
	}
	if (numFixed == 0) {
		return '(' + numNew == 1 ? Util.bind("build.oneProblemFound"/*nonNLS*/, String.valueOf(numNew)) : Util.bind("build.problemsFound"/*nonNLS*/, String.valueOf(numNew)) +')';
	} else
		if (numNew == 0) {
			return '(' + numFixed == 1 ? Util.bind("build.oneProblemFixed"/*nonNLS*/, String.valueOf(numFixed)) : Util.bind("build.problemsFixed"/*nonNLS*/, String.valueOf(numFixed)) + ')';
		} else {
			return '(' + (numFixed == 1 ? Util.bind("build.oneProblemFixed"/*nonNLS*/, String.valueOf(numFixed)) : Util.bind("build.problemsFixed"/*nonNLS*/, String.valueOf(numFixed)))
					+ ", "/*nonNLS*/
					+ (numNew == 1 ? Util.bind("build.oneProblemFound"/*nonNLS*/, String.valueOf(numNew)) : Util.bind("build.problemsFound"/*nonNLS*/, String.valueOf(numNew))) + ')';
		}
}
/**
 * Sets the cancelling flag, which indicates we are in the middle
 * of being cancelled.  Certain places (those callable indirectly from the compiler)
 * should not check cancel again while this is true, to avoid OperationCanceledException
 * being thrown at an inopportune time.
 */
public void setCancelling(boolean cancelling) {
	fCancelling = cancelling;
}
/**
 * Sets the amount of progress to report for compiling each compilation unit.
 */
public void setProgressPerCompilationUnit(float progress) {
	fProgressPerCompilationUnit = progress;
}
public void subTask(String message) {
	String pm = problemsMessage();
	String msg = pm.length() == 0 ? message : pm + " "/*nonNLS*/ + message;

	if (msg.equals(this.previousSubtask)) return; // avoid refreshing with same one
	if (DEBUG) System.out.println(msg);
	if (fProgress != null) fProgress.subTask(msg);

	this.previousSubtask = msg;
}
/**
 * Update the problem counts given the old and new problems,
 * either of which may be null.
 */
protected void updateProblemCounts(Vector oldProblems, Vector newProblems) {
	if (oldProblems != null) {
		for (int i = 0, oldSize = oldProblems.size(); i < oldSize; ++i) {
			ProblemDetailImpl oldProblem = (ProblemDetailImpl) oldProblems.elementAt(i);
			ProblemDetailImpl newProblem = null;
			if (newProblems != null) {
				for (int j = 0, newSize = newProblems.size(); j < newSize; ++j) {
					ProblemDetailImpl pb = (ProblemDetailImpl) newProblems.elementAt(j);
					if (oldProblem.equals(pb, true)) {
						newProblem = pb;
						break;
					}
				}
			}
			if (newProblem == null) {
				if ((oldProblem.getSeverity() & IProblemDetail.S_ERROR) != 0) {
					fFixedErrorCount++;
				} else {
					fFixedWarningCount++;
				}
			}
		}
	}
	if (newProblems != null) {
		for (int i = 0, newSize = newProblems.size(); i < newSize; ++i) {
			ProblemDetailImpl newProblem = (ProblemDetailImpl) newProblems.elementAt(i);
			ProblemDetailImpl oldProblem = null;
			if (oldProblems != null) {
				for (int j = 0, oldSize = oldProblems.size(); j < oldSize; ++j) {
					ProblemDetailImpl pb = (ProblemDetailImpl) oldProblems.elementAt(j);
					if (newProblem.equals(pb, true)) {
						oldProblem = pb;
						break;
					}
				}
			}
			if (oldProblem == null) {
				if ((newProblem.getSeverity() & IProblemDetail.S_ERROR) != 0) {
					fNewErrorCount++;
				} else {
					fNewWarningCount++;
				}
			}
		}
	}
}
public void updateProgress(float percentComplete) {
	if (percentComplete > fPercentComplete) {
		fPercentComplete = Math.min(percentComplete, 1.0f);
		int work = Math.round(fPercentComplete * fTotalWork);
		if (work > fWorkDone) {
			if (fProgress != null) {
				fProgress.worked(work - fWorkDone);
			}
			if (DEBUG) {
				System.out.println(NumberFormat.getPercentInstance().format(fPercentComplete));
			}
			fWorkDone = work;
		}
	}
}
public void updateProgressDelta(float percentWorked) {
	updateProgress(fPercentComplete + percentWorked);
}
}

package org.eclipse.jdt.internal.core.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
The <code>IProgressListener</code> interface is implemented
by objects that monitor the progress of an activity.

<p> All activity is broken down into a linear sequence of tasks against
which progress is reported. When a task begins, a <code>begin</code> 
notification is reported, followed by any number and mixture of progress
reports (<code>worked()</code>) and subtask 
notifications (<code>nowDoing()</code>).  When the task
is eventually completed, a <code>done()</code> notification is reported.

<p> Since notification is synchronous with the activity itself,
the listener should provide a fast and robust implementation.
If the handling of notifications would involve blocking operations, 
or operations which might throw uncaught exceptions, the notifications
should be queued, and the actual processing delegated to a separate thread.
 */
public interface IProgressListener {

/** Notifies that the main task is beginning.
@param name the name (or description) of the main task
@param totalWork the total number of work units into which
the main task is been subdivided
 */
public void begin(String name, int totalWork);
/** Notifies that the work is done, the main task is completed.
 */
public void done();
/** Notifies that a subtask of the main task is beginning.
Subtasks are optional; the main task might not have subtasks.
@param name the name (or description) of the subtask
 */
public void nowDoing(String name);
/** Notifies that a given number of work unit of the main task
has been completed.  Note that this amount represents an
installment, as opposed to a cumulative amount of work done
to date.
@param work the work units just completed
 */
public void worked(int work);
} // IProgressListener

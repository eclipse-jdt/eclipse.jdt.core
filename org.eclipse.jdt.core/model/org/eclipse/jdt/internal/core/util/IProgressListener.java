package org.eclipse.jdt.internal.core.util;

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

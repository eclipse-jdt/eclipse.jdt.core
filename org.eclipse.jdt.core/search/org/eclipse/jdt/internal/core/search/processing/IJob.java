package org.eclipse.jdt.internal.core.search.processing;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public interface IJob {
/**
 * Answer true if the job belongs to a given family (tag)
 */
public boolean belongsTo(String jobFamily);
/**
 * Execute the current job, answering:
 *      RESCHEDULE if the job should be rescheduled later on
 *      COMPLETE if the job is over
 */
public boolean execute();
}

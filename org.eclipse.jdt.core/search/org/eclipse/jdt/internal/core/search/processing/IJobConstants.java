package org.eclipse.jdt.internal.core.search.processing;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

public interface IJobConstants {

	int ForceImmediate = 1;
	int CancelIfNotReady = 2;
	int WaitUntilReady = 3;

	boolean FAILED = false;
	boolean COMPLETE = true;    
}

/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
    Philippe Mulet - Initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.IProblem;

/**
 * A callback interface for receiving java problem as they are discovered
 * by some Java operation.
 * 
 * @see IProblem
 * @since 2.0
 */
public interface IProblemRequestor {
	
	/**
	 * Notification of a Java problem.
	 * 
	 * @return void - Nothing is answered back to the operation which discovered the problem.
	 * @param problem IProblem - The discovered Java problem.
	 */	
	void acceptProblem(IProblem problem);

	/**
	 * Notification sent before starting a Java operation which can cause problems to be reported.
	 * Typically, this would tell the problem collector to clear previously recorded problems.
	 */
	//void clear();

	/**
	 * Notification sent after having completed the Java operation which caused problems to be reported.
	 * Typically, this would tell the problem collector that no more problems should be expected in this
	 * iteration.
	 */
	//void done();


}

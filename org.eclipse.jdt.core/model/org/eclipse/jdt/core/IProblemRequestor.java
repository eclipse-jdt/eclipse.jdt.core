package org.eclipse.jdt.core;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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

}

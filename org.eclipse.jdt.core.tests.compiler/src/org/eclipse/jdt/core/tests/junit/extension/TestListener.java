package org.eclipse.jdt.core.tests.junit.extension;

import junit.framework.Test;
import junit.framework.TestFailure;
/**
 * A Listener for test progress
 */
public interface TestListener extends junit.framework.TestListener {
   /**
 	* An error occurred.
 	*/
	public void addError(Test test, TestFailure testFailure);
   /**
 	* A failure occurred.
 	*/
 	public void addFailure(Test test, TestFailure testFailure); 
}

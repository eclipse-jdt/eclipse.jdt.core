package org.eclipse.jdt.core.tests.junit.extension;

/**
 * A test case that is being sent stop() when the user presses 'Stop' or 'Exit'.
 */
public class StopableTestCase extends junit.framework.TestCase {
public StopableTestCase(String name) {
	super(name);
}
/**
 * Default is to do nothing.
 */
public void stop() {
}
}

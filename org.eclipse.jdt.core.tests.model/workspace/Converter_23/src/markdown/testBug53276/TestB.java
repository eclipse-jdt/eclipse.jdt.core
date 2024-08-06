package javadoc.testBug53276;

/**
 * Handles a "Removed" response from the CVS server.
 * <p>
 * Suppose as a result of performing a command the CVS server responds
 * as follows:<br>
 * <pre>
 *   [...]
 *   Removed ??? \n
 *   [...]
 * </pre>
 * Then 
 * </p>
 */

/**
 * It removes the file from both the entries of the parent-folder
 * and from the local filesystem.
 */
public class TestB {
}


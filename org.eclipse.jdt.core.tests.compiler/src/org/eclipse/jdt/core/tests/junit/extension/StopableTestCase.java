/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
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

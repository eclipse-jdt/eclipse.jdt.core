/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance;

import java.io.IOException;
import junit.framework.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.test.performance.Dimension;


/**
 */
public class FullSourceWorkspaceBuildTests extends FullSourceWorkspaceTests {

	/**
	 * @param name
	 */
	public FullSourceWorkspaceBuildTests(String name) {
		super(name);
	}

	public static Test suite() {
		return buildSuite(FullSourceWorkspaceBuildTests.class);
	}

	// Set doc comment support
	public void testPerfFullBuild() throws CoreException, IOException {
		tagAsGlobalSummary("Full source workspace build", Dimension.CPU_TIME);
		startBuild(JavaCore.getDefaultOptions());
	}
}

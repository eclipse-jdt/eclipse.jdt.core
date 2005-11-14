/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.CodeExample;
import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorTestAnnotationProcessor;

public class MirrorTests extends APTTestBase {
	
	public MirrorTests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( MirrorTests.class );
	}
	
	/**
	 * Runs the MirrorTestAnnotationProcessor, which contains
	 * the actual tests
	 */
	public void testMirror() throws Exception {
		MirrorTestAnnotationProcessor._processRun = false;
		
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		
		String code = CodeExample.CODE;

		env.addClass( 
				srcRoot, 
				CodeExample.CODE_PACKAGE, 
				CodeExample.CODE_CLASS_NAME,
				code );

		fullBuild( project.getFullPath() );

		expectingNoProblems();
		
		assertTrue("Processor was not run", MirrorTestAnnotationProcessor._processRun); //$NON-NLS-1$
		
		assertEquals(ProcessorTestStatus.NO_ERRORS, ProcessorTestStatus.getErrors());
	}
	
}

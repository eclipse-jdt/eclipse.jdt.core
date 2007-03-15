/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.tools.JavaCompiler;

import junit.framework.TestCase;

/**
 * 
 * @since 3.3
 */
public class ModelTests extends TestCase {
	
	JavaCompiler _compiler = null;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		BatchTestUtils.init();
		_compiler = BatchTestUtils.getEclipseCompiler();
		// For sanity checking, replace above line with this:
		// _compiler = ToolProvider.getSystemJavaCompiler();
	}

	/**
	 * Attempt to read various elements of the Element hierarchy.
	 * @throws IOException 
	 */
	public void testElement() throws IOException {
		File targetFolder = TestUtils.concatPath(BatchTestUtils.getSrcFolderName(), "targets", "model");
		BatchTestUtils.copyResources("targets/model", targetFolder);

		List<String> options = new ArrayList<String>();
		options.add("-Aorg.eclipse.jdt.compiler.apt.tests.processors.elements.ElementProc");
		BatchTestUtils.compileTree(_compiler, options, targetFolder);

		// check that everything was processed
	}


}

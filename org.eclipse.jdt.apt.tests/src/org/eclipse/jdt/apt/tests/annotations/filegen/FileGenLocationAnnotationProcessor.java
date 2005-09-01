/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.filegen;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

public class FileGenLocationAnnotationProcessor extends BaseProcessor {

	public FileGenLocationAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process()
	{
		try
		{
			Filer f = _env.getFiler();

			PrintWriter pwa = f.createSourceFile("test.A"); //$NON-NLS-1$
			pwa.print(CODE_GEN_IN_PKG);
			pwa.close();

			PrintWriter pwb = f.createSourceFile("B"); //$NON-NLS-1$
			pwb.print(CODE_GEN_AT_PROJ_ROOT);
			pwb.close();
			
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	protected String CODE_GEN_IN_PKG = 
		"package test;" + "\n" +
		"public class A" + "\n" +
		"{" + "\n" +
		"}";

	@SuppressWarnings("nls")
	protected String CODE_GEN_AT_PROJ_ROOT = 
		"public class B" + "\n" +
		"{" + "\n" +
		"    test.A a;" + "\n" +
		"}";
}

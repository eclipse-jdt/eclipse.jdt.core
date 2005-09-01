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

public class FirstGenAnnotationProcessor extends BaseProcessor {

	public FirstGenAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process()
	{
		try
		{
			Filer f = _env.getFiler();
			PrintWriter pw = f.createSourceFile("duptest.DupFile"); //$NON-NLS-1$
			pw.print(CODE);
			pw.close();
		}
		catch( IOException e )
		{
			e.printStackTrace();
		}
	}

	@SuppressWarnings("nls")
	protected String CODE =
		"package duptest;" + "\n" +
		"import org.eclipse.jdt.apt.tests.annotations.filegen.SecondGenAnnotation;" + "\n" +
		"@SecondGenAnnotation" + "\n" +
		"public class DupFile" + "\n" +
		"{" + "\n" +
		"}";
}

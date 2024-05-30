/*******************************************************************************
 * Copyright (c) 2005, 2008 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.filegen;

import java.io.IOException;
import java.io.PrintWriter;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

public class FirstGenAnnotationProcessor extends BaseProcessor {

	public FirstGenAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process()
	{
		ProcessorTestStatus.setProcessorRan();
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

	protected String CODE =
		"""
		package duptest;\
		
		import org.eclipse.jdt.apt.tests.annotations.filegen.SecondGenAnnotation;\
		
		@SecondGenAnnotation\
		
		public class DupFile\
		
		{\
		
		}""";
}

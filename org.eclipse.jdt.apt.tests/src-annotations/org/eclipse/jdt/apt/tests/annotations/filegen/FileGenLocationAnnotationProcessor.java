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

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import java.io.IOException;
import java.io.PrintWriter;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

public class FileGenLocationAnnotationProcessor extends BaseProcessor {

	public FileGenLocationAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process()
	{
		ProcessorTestStatus.setProcessorRan();
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

	protected String CODE_GEN_IN_PKG =
		"package test;" + "\n" +
		"public class A" + "\n" +
		"{" + "\n" +
		"}";

	protected String CODE_GEN_AT_PROJ_ROOT =
		"public class B" + "\n" +
		"{" + "\n" +
		"    test.A a;" + "\n" +
		"}";
}

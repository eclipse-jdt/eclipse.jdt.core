/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.helloworld;

import java.io.IOException;
import java.io.PrintWriter;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

public class HelloWorldAnnotationProcessor implements AnnotationProcessor
{

	public HelloWorldAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}

	public void process()
	{
		try
		{
			Filer f = getEnvironment().getFiler();
			PrintWriter pw = f
				.createSourceFile( PACKAGE_NAME + "." + TYPE_NAME );
			pw.print( CODE );
			pw.close();
		}
		catch( IOException ioe )
		{
			ioe.printStackTrace();
		}
	}

	public AnnotationProcessorEnvironment getEnvironment()
	{
		return _env;
	}

	AnnotationProcessorEnvironment	_env;

	protected final static String	PACKAGE_NAME	= "generatedfilepackage";

	protected final static String	TYPE_NAME		= "GeneratedFileTest";

	protected final static String	CODE			= "package "
														+ PACKAGE_NAME
														+ ";"
														+ "\n"
														+ "public class "
														+ TYPE_NAME
														+ "\n"
														+ "{"
														+ "\n"
														+ "    public static void helloWorld()"
														+ "\n"
														+ "    {"
														+ "\n"
														+ "        System.out.println( \"Hello, world!  I am a generated file!\" ); "
														+ "\n" + "    }" + "\n"
														+ "}";
}

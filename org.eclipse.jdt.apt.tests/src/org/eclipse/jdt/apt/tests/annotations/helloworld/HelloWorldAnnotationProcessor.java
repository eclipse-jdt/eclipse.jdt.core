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
				.createSourceFile( getPackageName() + "." + getTypeName() );
			pw.print( getCode() );
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

	public String getCode() { return CODE; }
	public String getPackageName() { return "generatedfilepackage"; }
	public String getTypeName() { return "GeneratedFileTest"; }

	AnnotationProcessorEnvironment	_env;

	protected final static String	PACKAGE_NAME	= "";

	protected final static String	TYPE_NAME		= "";

	protected String	CODE			= "package "
														+ getPackageName()
														+ ";"
														+ "\n"
														+ "public class "
														+ getTypeName()
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

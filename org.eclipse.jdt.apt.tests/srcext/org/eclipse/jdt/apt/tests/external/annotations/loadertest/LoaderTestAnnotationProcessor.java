/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.external.annotations.loadertest;

import java.io.IOException;
import java.io.PrintWriter;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

/**
 * Used to test loading an annotation processor from a jar file.
 */
public class LoaderTestAnnotationProcessor implements AnnotationProcessor {
	
	private static String getClassName() {
		return getPackageName() + "." + getTypeName();
	}
	
	private final AnnotationProcessorEnvironment _env;

	/**
	 * Has an instance of this class been constructed in this VM?
	 * This is implemented in such a way that the answer does not
	 * depend on sharing a classloader.
	 */
	public static boolean isLoaded() {
		return "loaded".equals(System.getProperty(getClassName()));
	}

	/**
	 * Clear the "isLoaded" setting.  After this, isLoaded() will
	 * return false until the next time an instance is constructed.
	 */
	public static void clearLoaded() {
		System.clearProperty(getClassName());
	}

	public LoaderTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		System.setProperty(getClassName(), "loaded");
		_env = env;
	}

	public void process() {
		try
		{
			Filer f = getEnvironment().getFiler();
			PrintWriter pw = f
				.createSourceFile( getClassName() );
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
	
	protected static String getCode() {
		return CODE; 
	}
	
	protected static String getPackageName() {
		return "generatedfilepackage"; 
	}
	
	protected static String getTypeName() { 
		return "LoadFactoryFromJarTest"; 
	}

	protected final static String CODE			= 
		"package " + getPackageName() + ";\n" +
		"public class "	+ getTypeName()	+ "\n"	+ 
		"{\n" +
		"    public static void helloWorld() {\n" +
		"        System.out.println( \"Hello, world!  I am a generated file!\" ); \n" +
		"    }\n" +
		"}\n";
}

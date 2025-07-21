/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.external.annotations.loadertest;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Used to test loading an annotation processor from a jar file.
 */
public class LoaderTestAnnotationProcessor implements AnnotationProcessor {

	private static String getClassName() {
		return getPackageName() + "." + getTypeName(); //$NON-NLS-1$
	}

	private final AnnotationProcessorEnvironment _env;

	/**
	 * Has an instance of this class been constructed in this VM?
	 * This is implemented in such a way that the answer does not
	 * depend on sharing a classloader.
	 */
	public static boolean isLoaded() {
		return "loaded".equals(System.getProperty(getClassName())); //$NON-NLS-1$
	}

	/**
	 * Clear the "isLoaded" setting.  After this, isLoaded() will
	 * return false until the next time an instance is constructed.
	 */
	public static void clearLoaded() {
		System.clearProperty(getClassName());
	}

	public LoaderTestAnnotationProcessor(AnnotationProcessorEnvironment env) {
		System.setProperty(getClassName(), "loaded"); //$NON-NLS-1$
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
		return "generatedfilepackage";  //$NON-NLS-1$
	}

	protected static String getTypeName() {
		return "LoadFactoryFromJarTest";  //$NON-NLS-1$
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

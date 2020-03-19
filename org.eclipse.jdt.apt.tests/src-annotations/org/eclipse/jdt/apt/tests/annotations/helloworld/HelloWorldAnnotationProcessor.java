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
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.helloworld;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.ProcessorTestStatus;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;

/**
 * A processor that looks for HelloWorldAnnotation, and in response
 * generates a type named in the value of the annotation, containing code
 * specified in getCode().
 */
public class HelloWorldAnnotationProcessor extends BaseProcessor {

	private final static String PACKAGENAME = "generatedfilepackage"; //$NON-NLS-1$

	public HelloWorldAnnotationProcessor(AnnotationProcessorEnvironment env) {
		super(env);
	}

	public void process()
	{
		ProcessorTestStatus.setProcessorRan();
		Filer f = _env.getFiler();
		AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration) _env.getTypeDeclaration(HelloWorldAnnotation.class.getName());
		Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(annoDecl);
		try {
			for (Declaration annotatedDecl : annotatedDecls) {
				String typeName = getTypeName(annotatedDecl);
				PrintWriter writer = f.createSourceFile(
						PACKAGENAME + "." + typeName);
				writer.print(getCode(typeName));
				writer.close();
			}
			reportSuccess(this.getClass());
		}
		catch (NullPointerException npe) {
			reportError(this.getClass(), "Could not read annotation in order to generate text file");
		}
		catch (IOException ioe) {
			reportError(this.getClass(), "Could not generate text file due to IOException");
		}
	}

	private String getTypeName(Declaration annotatedDecl) {
		HelloWorldAnnotation tganno = annotatedDecl.getAnnotation(HelloWorldAnnotation.class);
		return tganno.value();
	}

	private String getCode(String typeName) {
		return "package " + PACKAGENAME + ";" + "\n"
		+ "public class "+ typeName	+ "\n"
		+ "{" + "\n"
		+ "    public static void helloWorld()"	+ "\n"
		+ "    {" + "\n"
		+ "        System.out.println( \"Hello, world!  I am a generated file!\" ); " + "\n"
		+ "    }" + "\n"
		+ "}";
	}

}

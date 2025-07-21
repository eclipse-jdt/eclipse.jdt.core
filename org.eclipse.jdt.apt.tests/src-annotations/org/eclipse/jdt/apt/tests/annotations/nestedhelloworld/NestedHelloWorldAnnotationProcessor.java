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
 *    mkaufman@bea.com - initial API and implementation
 *
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.nestedhelloworld;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;


public class NestedHelloWorldAnnotationProcessor extends
		BaseProcessor {

	public NestedHelloWorldAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		super( env );
	}

	// Code is annotated with HelloWorldAnnotation, so it will cause another round of processing
	public String getCode() {
		return "package " + PACKAGENAME + ";" + "\n" +
		"@" + HelloWorldAnnotation.class.getName() + "\n" +
		"public class " + TYPENAME + "\n" +
		"{  }";
	}

	private final static String PACKAGENAME = "nested.hello.world.generatedclass.pkg"; //$NON-NLS-1$
	private final static String TYPENAME = "NestedHelloWorldAnnotationGeneratedClass"; //$NON-NLS-1$

	@SuppressWarnings("unused")
	public void process()
	{
		Filer f = _env.getFiler();
		AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration) _env.getTypeDeclaration(NestedHelloWorldAnnotation.class.getName());
		Collection<Declaration> annotatedDecls = _env.getDeclarationsAnnotatedWith(annoDecl);
		try {
			for (Declaration annotatedDecl : annotatedDecls) {
				String typeName = TYPENAME;
				PrintWriter writer = f.createSourceFile(
						PACKAGENAME + "." + typeName);
				writer.print(getCode());
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
}

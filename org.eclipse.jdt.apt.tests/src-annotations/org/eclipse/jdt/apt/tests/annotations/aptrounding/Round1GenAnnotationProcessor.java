/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.aptrounding;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.TypeDeclaration;

public class Round1GenAnnotationProcessor implements AnnotationProcessor{

	private final AnnotationProcessorEnvironment _env;
	Round1GenAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}

	public void process() {
		final TypeDeclaration beanType = _env.getTypeDeclaration("test.Bean");
		final Filer filer = _env.getFiler();
		if( beanType == null ){
			try{
				PrintWriter writer = filer.createSourceFile("test.Bean");
				writer.print("package test;\n");
				writer.print("public class Bean{}\n");
				writer.close();
			}
			catch(IOException io){}
		}

		final Collection<TypeDeclaration> typeDecls = _env.getTypeDeclarations();
		final Messager msger = _env.getMessager();
		if( typeDecls.size() == 1 ){
			final TypeDeclaration type = typeDecls.iterator().next();
			if( !type.getQualifiedName().equals( "p1.X") )
				msger.printError("Expected to find p1.X but got " + type.getQualifiedName() );
		}
		else
			msger.printError("expected one type declaration but got " + typeDecls );
	}
}

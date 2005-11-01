/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.aptrounding;

import java.io.IOException;
import java.io.PrintWriter;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.TypeDeclaration;

public class Round2GenAnnotationProcessor implements AnnotationProcessor {
	private final AnnotationProcessorEnvironment _env;
	Round2GenAnnotationProcessor(AnnotationProcessorEnvironment env)
	{
		_env = env;
	}
	
	public void process() {
		final TypeDeclaration beanType = _env.getTypeDeclaration("test.Bean");
		final Filer filer = _env.getFiler();
		if( beanType != null ){
			try{
				PrintWriter writer = filer.createSourceFile("test.BeanBean");
				writer.print("package test;\n");
				writer.print("public class BeanBean{ public Bean bean = null; }\n");
				writer.close();
			}
			catch(IOException io){}
		}
	}
}

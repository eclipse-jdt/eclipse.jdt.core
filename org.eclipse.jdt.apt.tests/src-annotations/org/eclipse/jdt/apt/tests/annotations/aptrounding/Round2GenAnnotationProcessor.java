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

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.TypeDeclaration;
import java.io.IOException;
import java.io.PrintWriter;

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

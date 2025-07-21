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
package org.eclipse.jdt.apt.tests.external.annotations.batch;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class BatchGenAnnotationFactory implements AnnotationProcessorFactory {

	private static int ROUND = 0;
	private static final List<String> SUPPORTED_TYPES =
		Collections.singletonList(BatchGen.class.getName());

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> decls,
			AnnotationProcessorEnvironment env) {
		if( ROUND == 0 ){
			ROUND ++;
			return new BatchGen0AnnotationProcessor(env);
		}
		else if( ROUND == 1){
			ROUND ++;
			if( !decls.isEmpty() )
				env.getMessager().printError("Expecting empty set but got " + decls );

			return new BatchGen1AnnotationProcessor(env);
		}
		else if( ROUND == 2 ){ // NO-OP
			env.getMessager().printError("Called the third time.");
			return null;
		}
		// This is to make sure we aren't bouncing the class loader without a full build.
		else
			env.getMessager().printError("Calling BatchGenAnnotionFactory too many times. Round=" + ROUND );
		return null;
	}

	public Collection<String> supportedAnnotationTypes() {
		return SUPPORTED_TYPES;
	}
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	static class BatchGen0AnnotationProcessor implements AnnotationProcessor {

		final AnnotationProcessorEnvironment _env;
		BatchGen0AnnotationProcessor(AnnotationProcessorEnvironment env){
			_env = env;
		}
		public void process() {
			// a generated file will cause BatchGenAnnotationFactory to be
			// called again.
			try{
				final PrintWriter writer = _env.getFiler().createSourceFile("gen.Class0");
				writer.print("package gen;\n");
				writer.print("public class Class0{}");
				writer.close();
			}
			catch(IOException e){
				_env.getMessager().printError(e.getMessage());
			}
		}
	}

	static class BatchGen1AnnotationProcessor implements AnnotationProcessor {
		final AnnotationProcessorEnvironment _env;
		BatchGen1AnnotationProcessor(AnnotationProcessorEnvironment env){
			_env = env;
		}
		public void process(){
			try{
				final PrintWriter writer = _env.getFiler().createSourceFile("gen.Class1");
				writer.print("package gen;\n");
				writer.print("public class Class1{}");
				writer.close();
			}
			catch(IOException e){
				_env.getMessager().printError(e.getMessage());
			}
		}
	}
}

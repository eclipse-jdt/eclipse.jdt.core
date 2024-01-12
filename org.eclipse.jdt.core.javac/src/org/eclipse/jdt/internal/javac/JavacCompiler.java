/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.nio.charset.Charset;
import java.util.Objects;
import java.util.stream.Stream;

import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;

import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.tool.EclipseFileObject;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.builder.SourceFile;

import com.sun.tools.javac.main.JavaCompiler;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

public class JavacCompiler extends Compiler {

	public JavacCompiler(INameEnvironment environment, IErrorHandlingPolicy policy, CompilerOptions options,
			ICompilerRequestor requestor, IProblemFactory problemFactory) {
		super(environment, policy, options, requestor, problemFactory);
	}

	@Override
	public void compile(ICompilationUnit[] sourceUnits) {
		Context javacContext = new Context();
//		javacContext.put(DiagnosticListener.class, diagnostic -> {
//			this.problemReporter.
//			if (Objects.equals(diagnostic.getSource(), fileObject) ||
//				diagnostic.getSource() instanceof DiagnosticSource source && Objects.equals(source.getFile(), fileObject)) {
//				IProblem[] previous = res.getProblems();
//				IProblem[] newProblems = Arrays.copyOf(previous, previous.length + 1);
//				newProblems[newProblems.length - 1] = JavacConverter.convertDiagnostic(diagnostic);
//				res.setProblems(newProblems);
//			}
//		});
		JavacUtils.configureJavacContext(javacContext, this.options.getMap(), Stream.of(sourceUnits)
			.filter(SourceFile.class::isInstance)
			.map(SourceFile.class::cast)
			.map(source -> source.resource)
			.map(IResource::getProject)
			.filter(JavaProject::hasJavaNature)
			.map(JavaCore::create)
			.findFirst()
			.orElse(null));
		// TODO map context DiagnosticHandler to IProblemFactory to create markers
		JavaCompiler javac = JavaCompiler.instance(javacContext);
		try {
			javac.compile(List.from(Stream.of(sourceUnits)
				.filter(SourceFile.class::isInstance)
				.map(SourceFile.class::cast)
				.map(source -> source.resource.getLocationURI())
				.map(uri -> new EclipseFileObject(null, uri, Kind.SOURCE, Charset.defaultCharset()))
				.map(JavaFileObject.class::cast)
				.toList()));
		} catch (Throwable e) {
			// TODO fail
		}
		for (int i = 0; i < sourceUnits.length; i++) {
			ICompilationUnit in = sourceUnits[i];
			CompilationResult result = new CompilationResult(in, i, sourceUnits.length, Integer.MAX_VALUE);
			this.requestor.acceptResult(result);
		}
		
	}


}

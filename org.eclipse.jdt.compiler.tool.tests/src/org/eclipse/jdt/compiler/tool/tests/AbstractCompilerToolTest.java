/*******************************************************************************
 * Copyright (c) 2008, 2012 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *    IBM Corporation - fix for 342936
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool.tests;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.eclipse.jdt.core.tests.compiler.regression.BatchCompilerTest;
import org.eclipse.jdt.internal.compiler.tool.EclipseCompiler;

public class AbstractCompilerToolTest extends BatchCompilerTest {
	public AbstractCompilerToolTest(String name) {
		super(name);
	}
	static class CompilerInvocationTestsArguments {
		StandardJavaFileManager standardJavaFileManager;
		List<String> options;
		String[] fileNames;
		CompilerInvocationTestsArguments(
				StandardJavaFileManager standardJavaFileManager,
				List<String> options,
				String[] fileNames) {
			this.standardJavaFileManager = standardJavaFileManager;
			this.options = options;
			this.fileNames = fileNames;
		}
		@Override
		public String toString() {
			StringBuilder result = new StringBuilder();
			for (String option: this.options) {
				result.append(option);
				result.append(' ');
			}
			return result.toString();
		}
	}
	static class CompilerInvocationDiagnosticListener implements DiagnosticListener<JavaFileObject> {
		public static final int NONE = 0;
		public static final int ERROR = 1;
		public static final int INFO = 2;
		public static final int WARNING = 4;

		private final PrintWriter err;
		public int kind;

		public CompilerInvocationDiagnosticListener(PrintWriter err) {
			this.err = err;
			this.kind = NONE;
		}
		@Override
		public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
			err.println(diagnostic);
			if (this.kind == NONE) {
				switch(diagnostic.getKind()) {
					case ERROR :
						this.kind = ERROR;
						break;
					case WARNING :
					case MANDATORY_WARNING :
						this.kind = WARNING;
						break;
					case NOTE :
					case OTHER :
						this.kind = INFO;
				}
			}
		}
	}
	static EclipseCompiler COMPILER = new EclipseCompiler();
	static JavaCompiler JAVAC_COMPILER = ToolProvider.getSystemJavaCompiler();
	@Override
	protected boolean invokeCompiler(
			PrintWriter out,
			PrintWriter err,
			Object extraArguments,
			TestCompilationProgress compilationProgress) {
		CompilerInvocationTestsArguments arguments = (CompilerInvocationTestsArguments) extraArguments;
		StandardJavaFileManager manager = arguments.standardJavaFileManager;
		boolean ownsManager = false;
		if (manager == null) {
			manager = COMPILER.getStandardFileManager(null, null, null); // will pick defaults up
			ownsManager = true;
		}
		try {
			List<File> files = new ArrayList<>();
			String[] fileNames = arguments.fileNames;
			for (int i = 0, l = fileNames.length; i < l; i++) {
				if (fileNames[i].startsWith(OUTPUT_DIR)) {
					files.add(new File(fileNames[i]));
				} else {
					files.add(new File(OUTPUT_DIR + File.separator + fileNames[i]));
				}
			}
			CompilationTask task = COMPILER.getTask(out, arguments.standardJavaFileManager /* carry the null over */, new CompilerInvocationDiagnosticListener(err), arguments.options, null, manager.getJavaFileObjectsFromFiles(files));
			return task.call();
		} finally {
			try {
				if (ownsManager)
					manager.close();
			} catch (IOException e) {
				// nop
			}
		}
	}
}

/*******************************************************************************
* Copyright (c) 2024 Microsoft Corporation and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*
* Contributors:
*     Microsoft Corporation - initial API and implementation
*******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.JCDiagnostic;

public class JavacProblemConverter {
	public static JavacProblem createJavacProblem(Diagnostic<? extends JavaFileObject> diagnostic) {
		return new JavacProblem(
			diagnostic.getSource().getName().toCharArray(),
			diagnostic.getMessage(Locale.getDefault()),
			diagnostic.getCode(),
			toProblemId(diagnostic),
			new String[0],
			switch (diagnostic.getKind()) {
				case ERROR -> ProblemSeverities.Error;
				case WARNING, MANDATORY_WARNING -> ProblemSeverities.Warning;
				case NOTE -> ProblemSeverities.Info;
				default -> ProblemSeverities.Error;
			},
			(int) Math.min(diagnostic.getPosition(), diagnostic.getStartPosition()),
			(int) (diagnostic.getEndPosition() - 1),
			(int) diagnostic.getLineNumber(),
			(int) diagnostic.getColumnNumber());
	}

	/**
	 * See the link below for Javac problem list:
	 * https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/resources/compiler.properties
	 * 
	 * And the examples to reproduce the Javac problems:
	 * https://github.com/openjdk/jdk/tree/master/test/langtools/tools/javac/diags/examples
	 */
	public static int toProblemId(Diagnostic<? extends JavaFileObject> javacDiagnostic) {
		String javacDiagnosticCode = javacDiagnostic.getCode();
		// better use a Map<String, IProblem> if there is a 1->0..1 mapping
		return switch (javacDiagnosticCode) {
			case "compiler.err.expected" -> IProblem.ParsingErrorInsertTokenAfter;
			case "compiler.warn.raw.class.use" -> IProblem.RawTypeReference;
			case "compiler.err.cant.resolve.location" -> convertUnresolvedSymbol(javacDiagnostic);
			case "compiler.err.cant.resolve.location.args" -> IProblem.UndefinedMethod;
			case "compiler.err.cant.resolve" -> IProblem.UndefinedField;
			case "compiler.err.cant.apply.symbols" -> IProblem.UndefinedConstructor;
			case "compiler.err.premature.eof" -> IProblem.ParsingErrorUnexpectedEOF; // syntax error
			case "compiler.err.report.access" -> convertNotVisibleAccess(javacDiagnostic);
			// TODO complete mapping list; dig in https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/resources/compiler.properties
			// for an exhaustive (but polluted) list, unless a better source can be found (spec?)
			default -> 0;
		};
	}

	private static int convertUnresolvedSymbol(Diagnostic<? extends JavaFileObject> javacDiagnostic) {
		if (javacDiagnostic instanceof JCDiagnostic jcDiagnostic) {
			Object[] args = jcDiagnostic.getArgs();
			if (args != null) {
				for (Object arg : args) {
					if (arg instanceof Kinds.KindName kindName) {
						return switch (kindName) {
							case CLASS -> IProblem.UndefinedType;
							case METHOD -> IProblem.UndefinedMethod;
							case VAR -> IProblem.UnresolvedVariable;
							default -> IProblem.UndefinedName;
						};
					}
				}
			}
		}

		return IProblem.UndefinedName;
	}

	private static int convertNotVisibleAccess(Diagnostic<? extends JavaFileObject> javacDiagnostic) {
		if (javacDiagnostic instanceof JCDiagnostic jcDiagnostic) {
			Object[] args = jcDiagnostic.getArgs();
			if (args != null && args.length > 0) {
				if (args[0] instanceof Symbol.MethodSymbol methodSymbol) {
					if (methodSymbol.isConstructor()) {
						return IProblem.NotVisibleConstructor;
					}

					return IProblem.NotVisibleMethod;
				} else if (args[0] instanceof Symbol.ClassSymbol) {
					return IProblem.NotVisibleType;
				} else if (args[0] instanceof Symbol.VarSymbol) {
					return IProblem.NotVisibleField;
				}
			}
		}

		return 0;
	}
}

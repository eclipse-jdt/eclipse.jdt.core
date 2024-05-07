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

package org.eclipse.jdt.internal.javac;

import java.io.IOException;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Position;

public class JavacProblemConverter {
	private static final String COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD = "compiler.warn.non.serializable.instance.field";
	private static final String COMPILER_WARN_MISSING_SVUID = "compiler.warn.missing.SVUID";

	public static JavacProblem createJavacProblem(Diagnostic<? extends JavaFileObject> diagnostic) {
		int problemId = toProblemId(diagnostic);
		org.eclipse.jface.text.Position diagnosticPosition = getDiagnosticPosition(diagnostic);
		return new JavacProblem(
				diagnostic.getSource().getName().toCharArray(),
				diagnostic.getMessage(Locale.getDefault()),
				diagnostic.getCode(),
				problemId,
				new String[0],
				toSeverity(diagnostic),
				diagnosticPosition.getOffset(),
				diagnosticPosition.getOffset() + diagnosticPosition.getLength(),
				(int) diagnostic.getLineNumber(),
				(int) diagnostic.getColumnNumber());
	}

	// result[0] - startPosition
	// result[1] - endPosition
	private static org.eclipse.jface.text.Position getDiagnosticPosition(Diagnostic<? extends JavaFileObject> diagnostic) {
		switch (diagnostic) {
		case JCDiagnostic jcDiagnostic -> {
			switch (jcDiagnostic.getDiagnosticPosition()) {
			case JCClassDecl jcClassDecl -> {
				return getDiagnosticPosition(jcDiagnostic, jcClassDecl);
			}
			case JCVariableDecl JCVariableDecl -> {
				return getDiagnosticPosition(jcDiagnostic, JCVariableDecl);
			}
			default -> {
				return getPositionUsingScanner(jcDiagnostic);
			}
			}
		}
		default -> {}
		}
		return getDefaultPosition(diagnostic);
	}

	private static org.eclipse.jface.text.Position getDefaultPosition(Diagnostic<? extends JavaFileObject> diagnostic) {
		int start = (int) Math.min(diagnostic.getPosition(), diagnostic.getStartPosition());
		int end = (int) Math.max(diagnostic.getEndPosition() - 1, start);
		return new org.eclipse.jface.text.Position( start, end - start);
	}

	private static org.eclipse.jface.text.Position getPositionUsingScanner(JCDiagnostic jcDiagnostic) {
		try {
			int preferedOffset = jcDiagnostic.getDiagnosticPosition().getPreferredPosition();
			DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
			JavaFileObject fileObject = source.getFile();
			CharSequence charContent = fileObject.getCharContent(true);
			ScannerFactory scannerFactory = ScannerFactory.instance(new Context());
			Scanner javacScanner = scannerFactory.newScanner(charContent, true);
			while (javacScanner.token().endPos <= preferedOffset) {
				javacScanner.nextToken();
			}
			Token toHighlight = javacScanner.prevToken();
			return new org.eclipse.jface.text.Position(toHighlight.pos, toHighlight.endPos - toHighlight.pos - 1);
		} catch (IOException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static org.eclipse.jface.text.Position getDiagnosticPosition(JCDiagnostic jcDiagnostic, JCVariableDecl jcVariableDecl) {
		int startPosition = (int) jcDiagnostic.getPosition();
		if (startPosition != Position.NOPOS) {
			try {
				String name = jcVariableDecl.getName().toString();
				return getDiagnosticPosition(name, startPosition, jcDiagnostic);
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static org.eclipse.jface.text.Position getDiagnosticPosition(JCDiagnostic jcDiagnostic, JCClassDecl jcClassDecl) {
		int startPosition = (int) jcDiagnostic.getPosition();
		if (startPosition != Position.NOPOS) {
			try {
				String name = jcClassDecl.getSimpleName().toString();
				return getDiagnosticPosition(name, startPosition, jcDiagnostic);
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static org.eclipse.jface.text.Position getDiagnosticPosition(String name, int startPosition, JCDiagnostic jcDiagnostic)
			throws IOException {
		if (name != null) {
			DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
			JavaFileObject fileObject = source.getFile();
			CharSequence charContent = fileObject.getCharContent(true);
			String content = charContent.toString();
			if (content != null && content.length() > startPosition) {
				String temp = content.substring(startPosition);
				int ind = temp.indexOf(name);
				int offset = startPosition + ind;
				int length = name.length() - 1;
				return new org.eclipse.jface.text.Position(offset, length);
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static org.eclipse.jface.text.Position getDiagnosticPosition(int preferedOffset, JCDiagnostic jcDiagnostic) throws IOException {
		DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
		JavaFileObject fileObject = source.getFile();
		CharSequence charContent = fileObject.getCharContent(true);
		String content = charContent.toString();
		if (content != null && content.length() > preferedOffset) {
			int scanOffset = preferedOffset - 1;
			while (scanOffset > 0 && Character.isAlphabetic(content.charAt(scanOffset))) {
				scanOffset--;
			}
			return new org.eclipse.jface.text.Position(scanOffset, preferedOffset - scanOffset - 1);
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static int toSeverity(Diagnostic<? extends JavaFileObject> diagnostic) {
		return switch (diagnostic.getKind()) {
			case ERROR -> ProblemSeverities.Error;
			case WARNING, MANDATORY_WARNING -> ProblemSeverities.Warning;
			case NOTE -> ProblemSeverities.Info;
			default -> ProblemSeverities.Error;
		};
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
			case COMPILER_WARN_MISSING_SVUID -> IProblem.MissingSerialVersion;
			case COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD -> 99999999; // JDT doesn't have this diagnostic
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

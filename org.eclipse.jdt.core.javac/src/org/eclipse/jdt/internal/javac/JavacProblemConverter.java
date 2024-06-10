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
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Position;

public class JavacProblemConverter {
	private static final String COMPILER_ERR_MISSING_RET_STMT = "compiler.err.missing.ret.stmt";
	private static final String COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD = "compiler.warn.non.serializable.instance.field";
	private static final String COMPILER_WARN_MISSING_SVUID = "compiler.warn.missing.SVUID";
	private final CompilerOptions compilerOptions;
	private final Context context;

	public JavacProblemConverter(Map<String, String> options, Context context) {
		this(new CompilerOptions(options), context);
	}
	public JavacProblemConverter(CompilerOptions options, Context context) {
		this.compilerOptions = options;
		this.context = context;
	}

	/**
	 *
	 * @param diagnostic
	 * @param context
	 * @return a JavacProblem matching the given diagnostic, or <code>null</code> if problem is ignored
	 */
	public JavacProblem createJavacProblem(Diagnostic<? extends JavaFileObject> diagnostic) {
		// Ignore "documentation comment is not attached to any declaration" warnings
		if (diagnostic.getCode().equals("compiler.warn.dangling.doc.comment")) {
			return null;
		}
		int problemId = toProblemId(diagnostic);
		int severity = toSeverity(problemId, diagnostic);
		if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
			return null;
		}
		org.eclipse.jface.text.Position diagnosticPosition = getDiagnosticPosition(diagnostic, context);
		return new JavacProblem(
				diagnostic.getSource().getName().toCharArray(),
				diagnostic.getMessage(Locale.getDefault()),
				diagnostic.getCode(),
				problemId,
				new String[0],
				severity,
				diagnosticPosition.getOffset(),
				diagnosticPosition.getOffset() + diagnosticPosition.getLength(),
				(int) diagnostic.getLineNumber(),
				(int) diagnostic.getColumnNumber());
	}

	private static org.eclipse.jface.text.Position getDiagnosticPosition(Diagnostic<? extends JavaFileObject> diagnostic, Context context) {
		if (diagnostic.getCode().contains(".dc")) { //javadoc
			return getDefaultPosition(diagnostic);
		}
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
				org.eclipse.jface.text.Position result = getMissingReturnMethodDiagnostic(jcDiagnostic, context);
				if (result != null) {
					return result;
				}
				if (jcDiagnostic.getStartPosition() == jcDiagnostic.getEndPosition()) {
					return getPositionUsingScanner(jcDiagnostic, context);
				}
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

	private static org.eclipse.jface.text.Position getPositionUsingScanner(JCDiagnostic jcDiagnostic, Context context) {
		try {
			int preferedOffset = jcDiagnostic.getDiagnosticPosition().getPreferredPosition();
			DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
			JavaFileObject fileObject = source.getFile();
			CharSequence charContent = fileObject.getCharContent(true);
			Context scanContext = new Context();
			ScannerFactory scannerFactory = ScannerFactory.instance(scanContext);
			Log log = Log.instance(scanContext);
			log.useSource(fileObject);
			Scanner javacScanner = scannerFactory.newScanner(charContent, true);
			Token t = javacScanner.token();
			while (t != null && t.kind != TokenKind.EOF && t.endPos <= preferedOffset) {
				javacScanner.nextToken();
				t = javacScanner.token();
				Token prev = javacScanner.prevToken();
				if( prev != null ) {
					if( t.endPos == prev.endPos && t.pos == prev.pos && t.kind.equals(prev.kind)) {
						t = null; // We're stuck in a loop. Give up.
					}
				}
			}
			Token toHighlight = javacScanner.token();
			if (isTokenBadChoiceForHighlight(t) && !isTokenBadChoiceForHighlight(javacScanner.prevToken())) {
				toHighlight = javacScanner.prevToken();
			}
			return new org.eclipse.jface.text.Position(Math.min(charContent.length() - 1, toHighlight.pos), Math.max(0, toHighlight.endPos - toHighlight.pos - 1));
		} catch (IOException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private static org.eclipse.jface.text.Position getMissingReturnMethodDiagnostic(JCDiagnostic jcDiagnostic, Context context) {
		// https://github.com/eclipse-jdtls/eclipse-jdt-core-incubator/issues/313
		if (COMPILER_ERR_MISSING_RET_STMT.equals(jcDiagnostic.getCode())) {
			JCTree tree = jcDiagnostic.getDiagnosticPosition().getTree();
			if (tree instanceof JCBlock) {
				try {
					int startOffset = tree.getStartPosition();
					DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
					JavaFileObject fileObject = source.getFile();
					CharSequence charContent = fileObject.getCharContent(true);
					ScannerFactory scannerFactory = ScannerFactory.instance(context);
					Scanner javacScanner = scannerFactory.newScanner(charContent, true);
					Token t = javacScanner.token();
					Token lparen = null;
					Token rparen = null;
					Token name = null;
					while (t.kind != TokenKind.EOF && t.endPos <= startOffset) {
						javacScanner.nextToken();
						t = javacScanner.token();
						switch (t.kind) {
						case TokenKind.IDENTIFIER: {
							if (lparen == null) {
								name = t;
							}
							break;
						}
						case TokenKind.LPAREN: {
							lparen = t;
							break;
						}
						case TokenKind.RPAREN: {
							if (name != null) {
								rparen = t;
							}
							break;
						}
						case TokenKind.RBRACE:
						case TokenKind.SEMI: {
							name = null;
							lparen = null;
							rparen = null;
							break;
						}
						default:
							break;
						}
					}
					if (lparen != null && name != null && rparen != null) {
						return new org.eclipse.jface.text.Position(Math.min(charContent.length() - 1, name.pos), Math.max(0, rparen.endPos - name.pos - 1));
					}
				} catch (IOException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			return getDefaultPosition(jcDiagnostic);
		}
		return null;
	}

	/**
	 * Returns true if, based off a heuristic, the token is not a good choice for highlighting.
	 *
	 * eg. a closing bracket is bad, because the problem in the code is likely before the bracket,
	 *     and the bracket is narrow and hard to see
	 * eg. an identifier is good, because it's very likely the problem, and it's probably wide
	 *
	 * @param t the token to check
	 * @return true if, based off a heuristic, the token is not a good choice for highlighting, and false otherwise
	 */
	private static boolean isTokenBadChoiceForHighlight(Token t) {
		return t.kind == TokenKind.LPAREN
				|| t.kind == TokenKind.RPAREN
				|| t.kind == TokenKind.LBRACKET
				|| t.kind == TokenKind.RBRACKET
				|| t.kind == TokenKind.LBRACE
				|| t.kind == TokenKind.RBRACE;
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
		if (startPosition != Position.NOPOS &&
			!(jcClassDecl.getMembers().isEmpty() && jcClassDecl.getStartPosition() == jcClassDecl.getMembers().get(0).getStartPosition())) {
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
		if (name != null && !name.isEmpty()) {
			DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
			JavaFileObject fileObject = source.getFile();
			CharSequence charContent = fileObject.getCharContent(true);
			String content = charContent.toString();
			if (content != null && content.length() > startPosition) {
				String temp = content.substring(startPosition);
				int ind = temp.indexOf(name);
				if (ind >= 0) {
					int offset = startPosition + ind;
					int length = name.length() - 1;
					return new org.eclipse.jface.text.Position(offset, length);
				}
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private int toSeverity(int jdtProblemId, Diagnostic<? extends JavaFileObject> diagnostic) {
		if (jdtProblemId != 0) {
			int irritant = ProblemReporter.getIrritant(jdtProblemId);
			if (irritant != 0) {
				int res = this.compilerOptions.getSeverity(irritant);
				res &= ~ProblemSeverities.Optional; // reject optional flag at this stage
				return res;
			}
		}
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
	public static int toProblemId(Diagnostic<? extends JavaFileObject> diagnostic) {
		String javacDiagnosticCode = diagnostic.getCode();
		return switch (javacDiagnosticCode) {
			case "compiler.err.expected" -> IProblem.ParsingErrorInsertTokenAfter;
			case "compiler.warn.raw.class.use" -> IProblem.RawTypeReference;
			case "compiler.err.cant.resolve.location" -> convertUnresolvedSymbol(diagnostic);
			case "compiler.err.cant.resolve.location.args" -> convertUndefinedMethod(diagnostic);
			case "compiler.err.cant.resolve.location.args.params" -> IProblem.UndefinedMethod;
			case "compiler.err.cant.resolve" -> convertUnresolvedVariable(diagnostic);
			case "compiler.err.cant.resolve.args" -> convertUndefinedMethod(diagnostic);
			case "compiler.err.cant.resolve.args.params" -> IProblem.UndefinedMethod;
			case "compiler.err.cant.apply.symbols" -> convertInApplicableSymbols(diagnostic);
			case "compiler.err.cant.apply.symbol" -> convertInApplicableSymbols(diagnostic);
			case "compiler.err.premature.eof" -> IProblem.ParsingErrorUnexpectedEOF; // syntax error
			case "compiler.err.report.access" -> convertNotVisibleAccess(diagnostic);
			case "compiler.err.does.not.override.abstract" -> IProblem.AbstractMethodMustBeImplemented;
			case COMPILER_WARN_MISSING_SVUID -> IProblem.MissingSerialVersion;
			case COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD -> 99999999; // JDT doesn't have this diagnostic
			case "compiler.err.ref.ambiguous" -> convertAmbiguous(diagnostic);
			case "compiler.err.illegal.initializer.for.type" -> IProblem.TypeMismatch;
			case "compiler.err.prob.found.req" -> convertTypeMismatch(diagnostic);
			case "compiler.err.invalid.meth.decl.ret.type.req" -> IProblem.MissingReturnType;
			case "compiler.err.abstract.meth.cant.have.body" -> IProblem.BodyForAbstractMethod;
			case "compiler.err.unreported.exception.default.constructor" -> IProblem.UnhandledExceptionInDefaultConstructor;
			case "compiler.err.unreachable.stmt" -> IProblem.CodeCannotBeReached;
			case "compiler.err.except.never.thrown.in.try" -> IProblem.UnreachableCatch;
			case "compiler.err.except.already.caught" -> IProblem.InvalidCatchBlockSequence;
			case "compiler.err.unclosed.str.lit" -> IProblem.UnterminatedString;
			case "compiler.err.class.public.should.be.in.file" -> IProblem.PublicClassMustMatchFileName;
			case "compiler.err.already.defined.this.unit" -> IProblem.ConflictingImport;
			case "compiler.err.override.meth.doesnt.throw" -> IProblem.IncompatibleExceptionInThrowsClause;
			case "compiler.err.override.incompatible.ret" -> IProblem.IncompatibleReturnType;
			case "compiler.err.annotation.missing.default.value" -> IProblem.MissingValueForAnnotationMember;
			case "compiler.err.annotation.value.must.be.name.value" -> IProblem.UndefinedAnnotationMember;
			case "compiler.err.multicatch.types.must.be.disjoint" -> IProblem.InvalidUnionTypeReferenceSequence;
			case "compiler.err.unreported.exception.implicit.close" -> IProblem.UnhandledExceptionOnAutoClose;
			// next are javadoc; defaulting to JavadocUnexpectedText when no better problem could be found
			case "compiler.err.dc.bad.entity" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.bad.inline.tag" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.identifier.expected" -> IProblem.JavadocMissingIdentifier;
			case "compiler.err.dc.invalid.html" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.malformed.html" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.missing.semicolon" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.no.content" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.no.tag.name" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.no.url" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.no.title" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.gt.expected" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.ref.bad.parens" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.ref.syntax.error" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.ref.unexpected.input" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.unexpected.content" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.unterminated.inline.tag" -> IProblem.JavadocUnterminatedInlineTag;
			case "compiler.err.dc.unterminated.signature" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.unterminated.string" -> IProblem.JavadocUnexpectedText;
			case "compiler.err.dc.ref.annotations.not.allowed" -> IProblem.JavadocUnexpectedText;
			case COMPILER_ERR_MISSING_RET_STMT -> IProblem.ShouldReturnValue;
			default -> 0;
		};
	}

	// compiler.err.cant.resolve
	private static int convertUnresolvedVariable(Diagnostic<?> diagnostic) {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			if (jcDiagnostic.getDiagnosticPosition() instanceof JCTree.JCFieldAccess) {
				return IProblem.UndefinedField;
			}
		}

		return IProblem.UnresolvedVariable;
	}

	private static int convertUndefinedMethod(Diagnostic<?> diagnostic) {
		Diagnostic<?> diagnosticArg = getDiagnosticArgumentByType(diagnostic, Diagnostic.class);
		if (diagnosticArg != null && "compiler.misc.location.1".equals(diagnosticArg.getCode())) {
			return IProblem.NoMessageSendOnArrayType;
		}

		if ("compiler.err.cant.resolve.args".equals(diagnostic.getCode())) {
			Kinds.KindName kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
			if (kind == Kinds.KindName.CONSTRUCTOR) {
				return IProblem.UndefinedConstructor;
			}
		}

		return IProblem.UndefinedMethod;
	}

	private static <T> T getDiagnosticArgumentByType(Diagnostic<?> diagnostic, Class<T> type) {
		if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
			return null;
		}

		Object[] args = jcDiagnostic.getArgs();
		if (args != null) {
			for (Object arg : args) {
				if (type.isInstance(arg)) {
					return type.cast(arg);
				}
			}
		}

		return null;
	}

	private static Object[] getDiagnosticArguments(Diagnostic<?> diagnostic) {
		if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
			return new Object[0];
		}

		return jcDiagnostic.getArgs();
	}

	private static int convertInApplicableSymbols(Diagnostic<? extends JavaFileObject> diagnostic) {
		Kinds.KindName kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
		if ("compiler.err.cant.apply.symbols".equals(diagnostic.getCode())) {
			return switch (kind) {
				case CONSTRUCTOR -> IProblem.UndefinedConstructor;
				case METHOD -> IProblem.ParameterMismatch;
				default -> 0;
			};
		} else if ("compiler.err.cant.apply.symbol".equals(diagnostic.getCode())) {
			return switch (kind) {
				case CONSTRUCTOR -> IProblem.UndefinedConstructorInDefaultConstructor;
				case METHOD -> IProblem.ParameterMismatch;
				default -> 0;
			};
		}

		return 0;
	}

	// compiler.err.prob.found.req -> TypeMismatch, ReturnTypeMismatch, IllegalCast, VoidMethodReturnsValue...
	private static int convertTypeMismatch(Diagnostic<?> diagnostic) {
		Diagnostic<?> diagnosticArg = getDiagnosticArgumentByType(diagnostic, Diagnostic.class);
		if (diagnosticArg != null) {
			if ("compiler.misc.inconvertible.types".equals(diagnosticArg.getCode())) {
				Object[] args = getDiagnosticArguments(diagnosticArg);
				if (args != null && args.length > 0
					&& args[0] instanceof Type.JCVoidType) {
					return IProblem.MethodReturnsVoid;
				}

				return IProblem.TypeMismatch;
			} else if ("compiler.misc.unexpected.ret.val".equals(diagnosticArg.getCode())) {
				return IProblem.VoidMethodReturnsValue;
			} else if ("compiler.misc.missing.ret.val".equals(diagnosticArg.getCode())) {
				return IProblem.ShouldReturnValue;
			}
		}

		return 0;
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

	private static int convertNotVisibleAccess(Diagnostic<?> diagnostic) {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			Object[] args = jcDiagnostic.getArgs();
			if (args != null && args.length > 0) {
				if (args[0] instanceof Symbol.MethodSymbol methodSymbol) {
					if (methodSymbol.isConstructor()) {
						if (jcDiagnostic.getDiagnosticPosition() instanceof JCTree.JCIdent id
							&& id.getName() != null && id.getName().toString().equals("super")) {
							return IProblem.NotVisibleConstructorInDefaultConstructor;
						}
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

	private static int convertAmbiguous(Diagnostic<?> diagnostic) {
		Kinds.KindName kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
		return switch (kind) {
			case CLASS -> IProblem.AmbiguousType;
			case METHOD -> IProblem.AmbiguousMethod;
			default -> 0;
		};
	}
}

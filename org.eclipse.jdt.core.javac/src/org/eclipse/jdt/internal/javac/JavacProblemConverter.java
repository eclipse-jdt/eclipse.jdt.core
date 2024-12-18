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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.source.tree.Tree;
import com.sun.source.tree.Tree.Kind;
import com.sun.source.util.TreePath;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Kinds;
import com.sun.tools.javac.code.Kinds.KindName;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Token;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.EndPosTable;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.TreeInfo;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.JCDiagnostic.DiagnosticPosition;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Position;

public class JavacProblemConverter {
	private static final String COMPILER_ERR_MISSING_RET_STMT = "compiler.err.missing.ret.stmt";
	private static final String COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD = "compiler.warn.non.serializable.instance.field";
	private static final String COMPILER_WARN_MISSING_SVUID = "compiler.warn.missing.SVUID";
	private final CompilerOptions compilerOptions;
	private final Context context;
	private final Map<JavaFileObject, JCCompilationUnit> units = new HashMap<>();

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
		var nestedDiagnostic = getDiagnosticArgumentByType(diagnostic, JCDiagnostic.class);
		boolean useNestedDiagnostic = nestedDiagnostic != null
			&& diagnostic.getCode().equals("compiler.err.invalid.permits.clause")
			&& (nestedDiagnostic.getSource() == diagnostic.getSource()
				|| (nestedDiagnostic.getSource() == null && findSymbol(nestedDiagnostic) instanceof ClassSymbol classSymbol
					&& classSymbol.sourcefile == diagnostic.getSource()));
		int problemId = toProblemId(useNestedDiagnostic ? nestedDiagnostic : diagnostic);
		if (problemId == -1) { // cannot use < 0 as IProblem.Javadoc < 0
			return null;
		}
		int severity = toSeverity(problemId, diagnostic);
		if (severity == ProblemSeverities.Ignore || severity == ProblemSeverities.Optional) {
			return null;
		}
		org.eclipse.jface.text.Position diagnosticPosition = getDiagnosticPosition(diagnostic, context, problemId);
		if (diagnosticPosition == null) {
			return null;
		}
		if (diagnosticPosition.length == 0) {
			// workaround Eclipse Platform unable to render diagnostics with length=0 or at end of line
			// https://github.com/eclipse-platform/eclipse.platform.ui/issues/2321
			diagnosticPosition.length++;
			try {
				String documentText = loadDocumentText(diagnostic);
				if (diagnosticPosition.getOffset() >= documentText.length() || documentText.charAt(diagnosticPosition.getOffset()) == '\n') {
					diagnosticPosition.offset--;
				}
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		String[] arguments = getDiagnosticStringArguments(diagnostic);
		return new JavacProblem(
				diagnostic.getSource().getName().toCharArray(),
				diagnostic.getMessage(Locale.getDefault()),
				diagnostic.getCode(),
				problemId,
				arguments,
				severity,
				diagnosticPosition.getOffset(),
				diagnosticPosition.getOffset() + Math.max(diagnosticPosition.getLength() - 1, 0),
				(int) diagnostic.getLineNumber(),
				(int) diagnostic.getColumnNumber());
	}

	private static ClassSymbol findSymbol(Diagnostic<?> diagnostic) {
		var res = getDiagnosticArgumentByType(diagnostic, ClassSymbol.class);
		if (res != null) {
			return res;
		}
		var type = getDiagnosticArgumentByType(diagnostic, ClassType.class);
		if (type != null && type.tsym instanceof ClassSymbol classSym) {
			return classSym;
		}
		return null;
	}

	private org.eclipse.jface.text.Position getDiagnosticPosition(Diagnostic<? extends JavaFileObject> diagnostic, Context context, int problemId) {
		if (diagnostic.getCode().contains(".dc") || "compiler.warn.proc.messager".equals(diagnostic.getCode())) { //javadoc
			if (problemId == IProblem.JavadocMissingParamTag) {
				String message = diagnostic.getMessage(Locale.ENGLISH);
				TreePath path = getTreePath(diagnostic);
				if (message.startsWith("no @param for <") && path.getLeaf() instanceof JCMethodDecl method) {
					String typeParam = message.substring("no @param for <".length(), message.length() - 1);
					var position = method.getTypeParameters().stream()
						.filter(paramDecl -> typeParam.equals(paramDecl.getName().toString()))
						.map(paramDecl -> new org.eclipse.jface.text.Position(paramDecl.getPreferredPosition(), paramDecl.getName().toString().length()))
						.findFirst()
						.orElse(null);
					if (position != null) {
						return position;
					}
				}
				if (message.startsWith("no @param for ") && path.getLeaf() instanceof JCMethodDecl method) {
					String param = message.substring("no @param for ".length());
					var position = method.getParameters().stream()
						.filter(paramDecl -> param.equals(paramDecl.getName().toString()))
						.map(paramDecl -> new org.eclipse.jface.text.Position(paramDecl.getPreferredPosition(), paramDecl.getName().toString().length()))
						.findFirst()
						.orElse(null);
					if (position != null) {
						return position;
					}
				}
			}
			if (problemId == IProblem.JavadocMissingReturnTag
				&& diagnostic instanceof JCDiagnostic jcDiagnostic
				&& jcDiagnostic.getDiagnosticPosition() instanceof JCMethodDecl methodDecl
				&& methodDecl.getReturnType() != null) {
				JCTree returnType = methodDecl.getReturnType();
				JCCompilationUnit unit = units.get(jcDiagnostic.getSource());
				if (unit != null) {
					int end = unit.endPositions.getEndPos(returnType);
					int start = returnType.getStartPosition();
					return new org.eclipse.jface.text.Position(start, end - start);
				}
			}
			if (problemId == IProblem.JavadocMissingThrowsTag
					&& diagnostic instanceof JCDiagnostic jcDiagnostic
					&& jcDiagnostic.getDiagnosticPosition() instanceof JCMethodDecl methodDecl
					&& methodDecl.getThrows() != null && !methodDecl.getThrows().isEmpty()) {
				JCTree ex = methodDecl.getThrows().head;
				JCCompilationUnit unit = units.get(jcDiagnostic.getSource());
				if (unit != null) {
					int end = unit.endPositions.getEndPos(ex);
					int start = ex.getStartPosition();
					return new org.eclipse.jface.text.Position(start, end - start);
				}
			}
			return getDefaultPosition(diagnostic);
		}
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			if (problemId == IProblem.IncompatibleExceptionInThrowsClause && jcDiagnostic.getDiagnosticPosition() instanceof JCMethodDecl method) {
				int start = method.getPreferredPosition();
				var unit = this.units.get(jcDiagnostic.getSource());
				if (unit != null) {
					int end = method.thrown.stream().mapToInt(unit.endPositions::getEndPos).max().orElse(-1);
					if (end >= 0) {
						return new org.eclipse.jface.text.Position(start, end - start);
					}
				}
			}
			if (problemId == IProblem.UninitializedBlankFinalField ||
				problemId == IProblem.UninitializedLocalVariable) {
				var varSymbol = getDiagnosticArgumentByType(diagnostic, VarSymbol.class);
				if (varSymbol != null) {
					return new org.eclipse.jface.text.Position(varSymbol.pos, varSymbol.getSimpleName().length());
				}
			}
			TreePath diagnosticPath = getTreePath(jcDiagnostic);
			JCCompilationUnit unit = units.get(diagnostic.getSource());
			EndPosTable endPos = unit != null ? unit.endPositions : null;
			if (diagnosticPath != null) {
				if (problemId == IProblem.ParameterMismatch) {
					// Javac points to the arg, which JDT expects the method name
					diagnosticPath = diagnosticPath.getParentPath();
					while (diagnosticPath != null
						&& diagnosticPath.getLeaf() instanceof JCExpression
						&& !(diagnosticPath.getLeaf() instanceof JCMethodInvocation)) {
						diagnosticPath = diagnosticPath.getParentPath();
					}
					if (diagnosticPath.getLeaf() instanceof JCMethodInvocation method) {
						var selectExpr = method.getMethodSelect();
						if (selectExpr instanceof JCIdent methodNameIdent) {
							int start = methodNameIdent.getStartPosition();
							int end = methodNameIdent.getEndPosition(endPos);
							return new org.eclipse.jface.text.Position(start, end - start);
						}
						if (selectExpr instanceof JCFieldAccess methodFieldAccess) {
							int start = methodFieldAccess.getPreferredPosition() + 1; // after dot
							int end = methodFieldAccess.getEndPosition(endPos);
							return new org.eclipse.jface.text.Position(start, end - start);
						}
					}
				} else if (problemId == IProblem.NotVisibleConstructorInDefaultConstructor || problemId == IProblem.UndefinedConstructorInDefaultConstructor) {
					while (diagnosticPath != null && !(diagnosticPath.getLeaf() instanceof JCClassDecl)) {
						diagnosticPath = diagnosticPath.getParentPath();
					}
				} else if (problemId == IProblem.SealedSuperClassDoesNotPermit) {
					// jdt expects the node in the extends clause with the name of the sealed class
					if (diagnosticPath.getLeaf() instanceof JCTree.JCClassDecl classDecl) {
						diagnosticPath = JavacTrees.instance(context).getPath(units.get(jcDiagnostic.getSource()), classDecl.getExtendsClause());
					}
				} else if (problemId == IProblem.SealedSuperInterfaceDoesNotPermit) {
					// jdt expects the node in the implements clause with the name of the sealed class
					if (diagnosticPath.getLeaf() instanceof JCTree.JCClassDecl classDecl) {
						Symbol.ClassSymbol sym = getDiagnosticArgumentByType(jcDiagnostic, Symbol.ClassSymbol.class);
						Optional<JCExpression> jcExpr = classDecl.getImplementsClause().stream() //
								.filter(expression -> {
									return expression instanceof JCIdent jcIdent && jcIdent.sym.equals(sym);
								}) //
								.findFirst();
						if (jcExpr.isPresent()) {
							diagnosticPath = JavacTrees.instance(context).getPath(units.get(jcDiagnostic.getSource()), jcExpr.get());
						}
					}
				} else if (problemId == IProblem.TypeMismatch && diagnosticPath.getLeaf() instanceof JCFieldAccess fieldAccess) {
					int start = fieldAccess.getStartPosition();
					int end = fieldAccess.getEndPosition(endPos);
					return new org.eclipse.jface.text.Position(start, end - start);
				} else if (problemId == IProblem.MethodMustOverrideOrImplement) {
					Tree tree = diagnosticPath.getParentPath() == null ? null
							: diagnosticPath.getParentPath().getParentPath() == null ? null
									: diagnosticPath.getParentPath().getParentPath().getLeaf();
					if (tree != null) {
						if (unit != null && tree instanceof JCMethodDecl methodDecl) {
							try {
								int startPosition = methodDecl.pos;
								var lastParenthesisIndex = unit.getSourceFile()
										.getCharContent(false).toString()
										.indexOf(')', startPosition);
								return new org.eclipse.jface.text.Position(startPosition, lastParenthesisIndex - startPosition + 1);
							} catch (IOException e) {
								// fall through to default behaviour
							}
						}
					}
				} else if (problemId == IProblem.VoidMethodReturnsValue
						&& diagnosticPath.getParentPath() != null
						&& diagnosticPath.getParentPath().getLeaf() instanceof JCReturn returnStmt) {
					return getPositionByNodeRangeOnly(jcDiagnostic, returnStmt);
				} else if (problemId == IProblem.IncompatibleReturnType
						&& diagnosticPath.getParentPath() != null
						&& diagnosticPath.getParentPath().getLeaf() instanceof JCMethodDecl methodDecl) {
					return getPositionByNodeRangeOnly(jcDiagnostic, methodDecl.getReturnType());
				} else if (problemId == IProblem.ProviderMethodOrConstructorRequiredForServiceImpl || problemId == IProblem.AbstractServiceImplementation) {
					return getPositionByNodeRangeOnly(jcDiagnostic, (JCTree)diagnosticPath.getLeaf());
				} else if (problemId == IProblem.SwitchExpressionsYieldMissingDefaultCase
						&& diagnosticPath.getLeaf() instanceof JCTree.JCSwitchExpression switchExpr) {
					return getPositionByNodeRangeOnly(jcDiagnostic, switchExpr.selector instanceof JCTree.JCParens parens? parens.expr : switchExpr.selector);
				} else if (problemId == IProblem.UndefinedConstructor
						&& diagnosticPath.getParentPath() != null
						&& (diagnosticPath.getParentPath().getLeaf() instanceof JCNewClass
							|| (!(diagnosticPath.getLeaf() instanceof JCNewClass) && diagnosticPath.getParentPath().getLeaf() instanceof JCVariableDecl /* case of enum components */))) {
					return getPositionByNodeRangeOnly(jcDiagnostic, (JCTree)diagnosticPath.getParentPath().getLeaf());
				}
			}

			TreePath current = diagnosticPath;
			while (current != null && current.getLeaf() instanceof JCTree tree &&
					endPos != null && TreeInfo.getEndPos(tree, endPos) == Position.NOPOS) {
				current = current.getParentPath();
			}
 			Tree element = current != null ? current.getLeaf() :
				jcDiagnostic.getDiagnosticPosition() instanceof Tree tree ? tree :
				null;
			if (problemId == IProblem.NoMessageSendOnArrayType
				&& element instanceof JCFieldAccess
				&& diagnosticPath != null
				&& diagnosticPath.getParentPath().getLeaf() instanceof JCMethodInvocation methodInvocation
				&& methodInvocation.getMethodSelect() == element) {
				element = methodInvocation;
			}
			if (problemId == IProblem.UndefinedType
				&& element instanceof JCFieldAccess fieldAccess
				&& jcDiagnostic.getArgs().length > 0
				&& jcDiagnostic.getArgs()[0] instanceof PackageElement) {
				element = fieldAccess.getExpression();
			}
			if (problemId == IProblem.EnumAbstractMethodMustBeImplemented
				&& element instanceof JCClassDecl classDecl
				&& jcDiagnostic.getArgs().length >= 2
				&& jcDiagnostic.getArgs()[1] instanceof MethodSymbol method) {
				element = classDecl.getMembers().stream()
					.filter(JCMethodDecl.class::isInstance)
					.map(JCMethodDecl.class::cast)
					.filter(m -> m.getModifiers().getFlags().contains(Modifier.ABSTRACT))
					.filter(m -> method.name.equals(m.getName()))
					.findFirst()
					.map(Tree.class::cast)
					.orElse(element);
			}
			if (problemId == IProblem.UndefinedMethod && element instanceof JCMethodInvocation method
				&& method.getMethodSelect() instanceof JCIdent name) {
				element = name;
			}
			if (element != null) {
				switch (element) {
					case JCTree.JCTypeApply jcTypeApply: return getPositionByNodeRangeOnly(jcDiagnostic, jcTypeApply.clazz);
					case JCClassDecl jcClassDecl: return getDiagnosticPosition(jcDiagnostic, jcClassDecl);
					case JCVariableDecl jcVariableDecl: return getDiagnosticPosition(jcDiagnostic, jcVariableDecl);
					case JCMethodDecl jcMethodDecl: return getDiagnosticPosition(jcDiagnostic, jcMethodDecl, problemId);
					case JCIdent jcIdent: return getPositionByNodeRangeOnly(jcDiagnostic, jcIdent);
					case JCMethodInvocation methodInvocation: return getPositionByNodeRangeOnly(jcDiagnostic, methodInvocation);
					case JCFieldAccess jcFieldAccess:
						if (getDiagnosticArgumentByType(jcDiagnostic, KindName.class) != KindName.PACKAGE && getDiagnosticArgumentByType(jcDiagnostic, Symbol.PackageSymbol.class) == null) {
							// TODO here, instead of recomputing a position, get the JDT DOM node and call the Name (which has a position)
							return new org.eclipse.jface.text.Position(jcFieldAccess.getPreferredPosition() + 1, jcFieldAccess.getIdentifier().length());
						}
					// else: fail-through
					default:
						org.eclipse.jface.text.Position result = getMissingReturnMethodDiagnostic(jcDiagnostic, context);
						if (result != null) {
							return result;
						}
						if (jcDiagnostic.getStartPosition() == jcDiagnostic.getEndPosition()) {
							return getPositionUsingScanner(jcDiagnostic);
						}
				}
			}
		}
		return getDefaultPosition(diagnostic);
	}

	private org.eclipse.jface.text.Position getPositionByNodeRangeOnly(Diagnostic<?> jcDiagnostic, JCTree jcTree) {
		int startPosition = jcTree.getStartPosition();
		if (startPosition != Position.NOPOS) {
			JCCompilationUnit trackedUnit = this.units.get(jcDiagnostic.getSource());
			if (trackedUnit != null && trackedUnit.endPositions != null) {
				int endPosition = jcTree.getEndPosition(trackedUnit.endPositions);
				return new org.eclipse.jface.text.Position(startPosition, endPosition - startPosition);
			} else if (jcTree instanceof JCIdent ident) {
				return new org.eclipse.jface.text.Position(startPosition, ident.getName().length());
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}
	private org.eclipse.jface.text.Position getDiagnosticPosition(JCDiagnostic jcDiagnostic,
			JCMethodDecl jcMethodDecl, int problemId) {
		int startPosition = (int) jcDiagnostic.getPosition();
		boolean includeLastParenthesis =
				problemId == IProblem.FinalMethodCannotBeOverridden
				|| problemId == IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod
				|| problemId == IProblem.InheritedMethodReducesVisibility
				|| problemId == IProblem.MethodReducesVisibility
				|| problemId == IProblem.OverridingNonVisibleMethod;
		if (startPosition != Position.NOPOS) {
			try {
				String name = jcMethodDecl.getName().toString();
				if (includeLastParenthesis) {
					var unit = this.units.get(jcDiagnostic.getSource());
					if (unit != null) {
						var lastParenthesisIndex = unit.getSourceFile()
								.getCharContent(false).toString()
								.indexOf(')', startPosition);
						return new org.eclipse.jface.text.Position(startPosition, lastParenthesisIndex - startPosition + 1);
					}
				}
				return getDiagnosticPosition(name, startPosition, jcDiagnostic);
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}
	private org.eclipse.jface.text.Position getDefaultPosition(Diagnostic<?> diagnostic) {
		if (diagnostic.getPosition() >= 0) {
			int start = (int) Math.min(diagnostic.getPosition(), diagnostic.getStartPosition());
			int end = (int) Math.max(diagnostic.getEndPosition(), start);
			return new org.eclipse.jface.text.Position(start, end - start);
		}
		if (findSymbol(diagnostic) instanceof ClassSymbol classSymbol) {
			JCCompilationUnit unit = this.units.get(classSymbol.sourcefile);
			if (unit != null) {
				var declaration = TreeInfo.declarationFor(classSymbol, unit);
				if (declaration instanceof JCClassDecl classDeclaration) {
					// next should use the name position
					int startPosition = classDeclaration.getPreferredPosition();
					if (startPosition != Position.NOPOS) {
						return new org.eclipse.jface.text.Position(startPosition, classDeclaration.getSimpleName().length());
					}
				}
			}
		}
		return null;
	}

	private org.eclipse.jface.text.Position getPositionUsingScanner(JCDiagnostic jcDiagnostic) {
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
			return new org.eclipse.jface.text.Position(Math.min(charContent.length() - 1, toHighlight.pos), Math.max(1, toHighlight.endPos - toHighlight.pos - 1));
		} catch (IOException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private org.eclipse.jface.text.Position getMissingReturnMethodDiagnostic(JCDiagnostic jcDiagnostic, Context context) {
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

	private org.eclipse.jface.text.Position getDiagnosticPosition(JCDiagnostic jcDiagnostic, JCVariableDecl jcVariableDecl) {
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

	private org.eclipse.jface.text.Position getDiagnosticPosition(JCDiagnostic jcDiagnostic, JCClassDecl jcClassDecl) {
		int startPosition = (int) jcDiagnostic.getPosition();
		List<JCTree> realMembers = jcClassDecl.getMembers().stream() //
			.filter(member -> !(member instanceof JCMethodDecl methodDecl && methodDecl.sym != null && (methodDecl.sym.flags() & Flags.GENERATEDCONSTR) != 0))
			.collect(Collectors.toList());
		if (startPosition != Position.NOPOS &&
			(realMembers.isEmpty() || jcClassDecl.getStartPosition() != jcClassDecl.getMembers().get(0).getStartPosition())) {
			try {
				String name = jcClassDecl.getSimpleName().toString();
				return getDiagnosticPosition(name, startPosition, jcDiagnostic);
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}

	private org.eclipse.jface.text.Position getDiagnosticPosition(String name, int startPosition, JCDiagnostic jcDiagnostic)
			throws IOException {
		if (name != null && !name.isEmpty()) {
			String content = loadDocumentText(jcDiagnostic);
			if (content != null && content.length() > startPosition) {
				String temp = content.substring(startPosition);
				int ind = temp.indexOf(name);
				if (ind >= 0) {
					int offset = startPosition + ind;
					int length = name.length();
					return new org.eclipse.jface.text.Position(offset, length);
				}
			}
		}
		return getDefaultPosition(jcDiagnostic);
	}
	private static String loadDocumentText(Diagnostic<?> diagnostic) throws IOException {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			DiagnosticSource source = jcDiagnostic.getDiagnosticSource();
			JavaFileObject fileObject = source.getFile();
			CharSequence charContent = fileObject.getCharContent(true);
			String content = charContent.toString();
			return content;
		}
		return null;
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
	public int toProblemId(Diagnostic<? extends JavaFileObject> diagnostic) {
		String javacDiagnosticCode = diagnostic.getCode();
		return switch (javacDiagnosticCode) {
			case "compiler.warn.dangling.doc.comment" -> -1; // ignore
			case "compiler.note.removal.filename", "compiler.note.deprecated.plural.additional" -> -1; //ignore due to lack of position
			case "compiler.err.expected" -> IProblem.ParsingErrorInsertTokenAfter;
			case "compiler.err.expected2" -> IProblem.ParsingErrorInsertTokenBefore;
			case "compiler.err.expected3" -> IProblem.ParsingErrorInsertToComplete;
			case "compiler.err.unclosed.comment" -> IProblem.UnterminatedComment;
			case "compiler.err.illegal.start.of.type" -> IProblem.Syntax;
			case "compiler.err.illegal.start.of.expr" -> {
				try {
					String token = readIdentifier(loadDocumentText(diagnostic), diagnostic.getPosition());
					if (ModifierKeyword.toKeyword(token) != null) {
						yield IProblem.IllegalModifiers;
					}
				} catch (Exception ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
				yield IProblem.Syntax;
			}
			case "compiler.err.illegal.start.of.stmt" -> IProblem.Syntax;
			case "compiler.err.variable.not.allowed" -> IProblem.Syntax;
			case "compiler.err.illegal.dot" -> IProblem.Syntax;
			case "compiler.warn.raw.class.use" -> IProblem.RawTypeReference;
			case "compiler.err.cant.resolve.location" -> switch (getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class)) {
					case CLASS -> IProblem.UndefinedType;
					case METHOD -> IProblem.UndefinedMethod;
					case VAR -> { /* is a default choice for Javac while ECJ default choice is Undefined. Verify... */
						TreePath path = getTreePath(diagnostic);
						yield (path != null && path.getParentPath() != null
							&& path.getLeaf() instanceof JCIdent ident
							&& path.getParentPath().getLeaf() instanceof JCFieldAccess fieldAccess
							&& fieldAccess.getExpression() == ident) ?
							// is left part of fieldAccess, can be either a type or a var
							IProblem.UndefinedName :
							// otherwise it's in middle or at the end of an expression, or standalone,
							// so most likely a variable
							IProblem.UnresolvedVariable;
					}
					default -> IProblem.UndefinedName;
				};
			case "compiler.err.cant.resolve.location.args" -> convertUndefinedMethod(diagnostic);
			case "compiler.err.cant.resolve.location.args.params" -> IProblem.UndefinedMethod;
			case "compiler.err.cant.resolve", "compiler.err.invalid.mref" -> convertUnresolved(diagnostic);
			case "compiler.err.cant.resolve.args" -> convertUndefinedMethod(diagnostic);
			case "compiler.err.cant.resolve.args.params" -> IProblem.UndefinedMethod;
			case "compiler.err.cant.apply.symbols", "compiler.err.cant.apply.symbol" ->
				switch (getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class)) {
					case CONSTRUCTOR -> {
						TreePath treePath = getTreePath(diagnostic);
						while (treePath != null && !(treePath.getLeaf() instanceof JCMethodDecl) && treePath != null) {
							treePath = treePath.getParentPath();
						}
						if (treePath == null || !(treePath.getLeaf() instanceof JCMethodDecl methodDecl)) {
							// potential case of enum values without explicit call to constructor
							yield IProblem.UndefinedConstructor;
						}
						boolean isDefault = (methodDecl.sym.flags() & Flags.GENERATEDCONSTR) != 0;
						if (diagnostic instanceof JCDiagnostic.MultilineDiagnostic && isDefault) {
							yield IProblem.UndefinedConstructorInDefaultConstructor;
						}
						JCDiagnostic rootCause = getDiagnosticArgumentByType(diagnostic, JCDiagnostic.class);
						if (rootCause == null) {
							yield IProblem.UndefinedConstructor;
						}
						String rootCauseCode = rootCause.getCode();
						yield switch (rootCauseCode) {
						case "compiler.misc.report.access" -> isDefault ? IProblem.NotVisibleConstructorInDefaultConstructor : IProblem.NotVisibleConstructor;
						case "compiler.misc.arg.length.mismatch" -> isDefault ? IProblem.UndefinedConstructorInDefaultConstructor : IProblem.UndefinedConstructor;
						default -> IProblem.UndefinedConstructor;
						};
					}
					case METHOD -> IProblem.ParameterMismatch;
					default -> IProblem.ParameterMismatch;
				};
			case "compiler.err.premature.eof" -> IProblem.ParsingErrorUnexpectedEOF; // syntax error
			case "compiler.err.report.access" -> convertNotVisibleAccess(diagnostic);
			case "compiler.err.does.not.override.abstract" -> {
				Object[] args = getDiagnosticArguments(diagnostic);
				if (args.length > 2
					&& args[0] instanceof ClassSymbol classSymbol
					&& args[0] == args[2]) { // means abstract method defined in Concrete class
					if (classSymbol.isEnum()) {
						yield IProblem.EnumAbstractMethodMustBeImplemented;
					}
					if (!classSymbol.isInterface() && !classSymbol.isAbstract()) {
						yield IProblem.AbstractMethodsInConcreteClass;
					}
				}
				yield IProblem.AbstractMethodMustBeImplemented;
			}
			case COMPILER_WARN_MISSING_SVUID -> IProblem.MissingSerialVersion;
			case COMPILER_WARN_NON_SERIALIZABLE_INSTANCE_FIELD -> 99999999; // JDT doesn't have this diagnostic
			case "compiler.err.ref.ambiguous" -> convertAmbiguous(diagnostic);
			case "compiler.err.illegal.initializer.for.type" -> IProblem.TypeMismatch;
			case "compiler.err.prob.found.req" -> convertTypeMismatch(diagnostic);
			case "compiler.err.invalid.meth.decl.ret.type.req" -> IProblem.MissingReturnType;
			case "compiler.err.abstract.meth.cant.have.body" -> IProblem.BodyForAbstractMethod;
			case "compiler.err.unreported.exception.need.to.catch.or.throw" -> IProblem.UnhandledException;
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
			case "compiler.err.repeated.modifier" -> IProblem.DuplicateModifierForArgument; // TODO different according to target node
			case "compiler.err.not.stmt" -> IProblem.InvalidExpressionAsStatement;
			case "compiler.err.varargs.and.old.array.syntax" -> IProblem.VarargsConflict;
			case "compiler.err.non-static.cant.be.ref" -> switch (getDiagnosticArgumentByType(diagnostic, KindName.class)) {
				case METHOD -> IProblem.StaticMethodRequested;
				case VAR -> IProblem.NonStaticFieldFromStaticInvocation;
				default -> IProblem.NonStaticFieldFromStaticInvocation;
				// note IProblem.NonStaticAccessToStaticMethod is for the warning `objectInstance.staticMethod()`
			};
			case COMPILER_ERR_MISSING_RET_STMT -> IProblem.ShouldReturnValue;
			case "compiler.err.cant.ref.before.ctor.called" -> IProblem.InstanceFieldDuringConstructorInvocation; // TODO different according to target node
			case "compiler.err.not.def.public.cant.access" -> IProblem.NotVisibleType; // TODO different according to target node
			case "compiler.err.already.defined" -> IProblem.DuplicateMethod; // TODO different according to target node
			case "compiler.warn.underscore.as.identifier" -> IProblem.IllegalUseOfUnderscoreAsAnIdentifier;
			case "compiler.err.var.might.not.have.been.initialized" -> {
				VarSymbol symbol = getDiagnosticArgumentByType(diagnostic, VarSymbol.class);
				yield symbol.owner instanceof ClassSymbol ?
						IProblem.UninitializedBlankFinalField :
						IProblem.UninitializedLocalVariable;
			}
			case "compiler.err.missing.meth.body.or.decl.abstract" -> {
				if (diagnostic instanceof JCDiagnostic jcDiagnostic
						&& jcDiagnostic.getDiagnosticPosition() instanceof JCMethodDecl jcMethodDecl
						&& jcMethodDecl.sym != null
						&& jcMethodDecl.sym.enclClass() != null
						&& jcMethodDecl.sym.enclClass().type != null
						&& jcMethodDecl.sym.enclClass().type.isInterface()) {
					// javac states that the method must have a body or be abstract;
					// in the case of an interface where neither are required,
					// this likely means the method has a private modifier.
					if (compilerOptions.complianceLevel < ClassFileConstants.JDK1_8) {
						yield IProblem.IllegalModifierForInterfaceMethod;
					} else if (compilerOptions.complianceLevel < ClassFileConstants.JDK9) {
						yield IProblem.IllegalModifierForInterfaceMethod18;
					} else {
						yield IProblem.IllegalModifierForInterfaceMethod9;
					}
				}
				yield IProblem.MethodRequiresBody;
			}
			case "compiler.err.intf.meth.cant.have.body" -> IProblem.BodyForAbstractMethod;
			case "compiler.warn.empty.if" -> IProblem.EmptyControlFlowStatement;
			case "compiler.warn.redundant.cast" -> IProblem.UnnecessaryCast;
			case "compiler.err.illegal.char" -> IProblem.InvalidCharacterConstant;
			case "compiler.err.enum.label.must.be.unqualified.enum" -> IProblem.UndefinedField;
			case "compiler.err.bad.initializer" -> IProblem.ParsingErrorInsertToComplete;
			case "compiler.err.cant.assign.val.to.var" -> IProblem.FinalFieldAssignment;
			case "compiler.err.cant.inherit.from.final" -> isInAnonymousClass(diagnostic) ? IProblem.AnonymousClassCannotExtendFinalClass : IProblem.ClassExtendFinalClass;
			case "compiler.err.qualified.new.of.static.class" -> IProblem.InvalidClassInstantiation;
			case "compiler.err.abstract.cant.be.instantiated" -> IProblem.InvalidClassInstantiation;
			case "compiler.err.mod.not.allowed.here" -> illegalModifier(diagnostic);
			case "compiler.warn.strictfp" -> uselessStrictfp(diagnostic);
			case "compiler.err.invalid.permits.clause" -> illegalModifier(diagnostic);
			case "compiler.err.sealed.class.must.have.subclasses" -> IProblem.SealedSealedTypeMissingPermits;
			case "compiler.misc.doesnt.extend.sealed" -> getDiagnosticArgumentByType(diagnostic, ClassType.class).isInterface() ? IProblem.SealedNotDirectSuperInterface : IProblem.SealedNotDirectSuperClass;
			case "compiler.err.feature.not.supported.in.source.plural" -> {
				if (compilerOptions.complianceLevel < ClassFileConstants.JDK1_8) {
					yield IProblem.IllegalModifierForInterfaceMethod;
				} else if (compilerOptions.complianceLevel < ClassFileConstants.JDK9) {
					yield IProblem.IllegalModifierForInterfaceMethod18;
				} else {
					yield IProblem.IllegalModifierForInterfaceMethod9;
				}
			}
			case "compiler.err.expression.not.allowable.as.annotation.value" -> IProblem.AnnotationValueMustBeConstant;
			case "compiler.err.illegal.combination.of.modifiers" -> illegalCombinationOfModifiers(diagnostic);
			case "compiler.err.duplicate.class" -> IProblem.DuplicateTypes;
			case "compiler.err.module.not.found", "compiler.warn.module.not.found" -> IProblem.UndefinedModule;
			case "compiler.err.package.empty.or.not.found" -> IProblem.PackageDoesNotExistOrIsEmpty;
			case "compiler.warn.service.provided.but.not.exported.or.used" -> -1; // ECJ doesn't have this diagnostic TODO: file upstream
			case "compiler.warn.missing-explicit-ctor" -> IProblem.ConstructorRelated;
			case "compiler.warn.has.been.deprecated", "compiler.warn.has.been.deprecated.for.removal" -> {
				var kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
				yield kind == null ? IProblem.UsingDeprecatedField :
					switch (kind) {
						case CONSTRUCTOR -> IProblem.UsingDeprecatedConstructor;
						case METHOD -> IProblem.UsingDeprecatedMethod;
						case VAR, RECORD_COMPONENT -> IProblem.UsingDeprecatedField;
						case ANNOTATION -> IProblem.UsingDeprecatedType;
						case PACKAGE -> IProblem.UsingDeprecatedPackage;
						case MODULE -> IProblem.UsingDeprecatedModule;
						case CLASS, RECORD, INTERFACE, ENUM -> IProblem.UsingDeprecatedType;
						default -> IProblem.UsingDeprecatedField;
					};
				}
			case "compiler.warn.inconsistent.white.space.indentation" -> -1;
			case "compiler.warn.trailing.white.space.will.be.removed" -> -1;
			case "compiler.warn.possible.fall-through.into.case" -> IProblem.FallthroughCase;
			case "compiler.warn.restricted.type.not.allowed.preview" -> IProblem.RestrictedTypeName;
			case "compiler.err.illegal.esc.char" -> IProblem.InvalidEscape;
			case "compiler.err.preview.feature.disabled", "compiler.err.preview.feature.disabled.plural" -> IProblem.PreviewFeatureDisabled;
			case "compiler.err.is.preview" -> IProblem.PreviewAPIUsed;
			case "compiler.err.cant.access" -> IProblem.NotAccessibleType;
			case "compiler.err.var.not.initialized.in.default.constructor" -> IProblem.UninitializedBlankFinalField;
			case "compiler.err.assert.as.identifier" -> IProblem.UseAssertAsAnIdentifier;
			case "compiler.warn.unchecked.varargs.non.reifiable.type" -> IProblem.PotentialHeapPollutionFromVararg;
			case "compiler.err.var.might.already.be.assigned" -> IProblem.FinalFieldAssignment;
			case "compiler.err.annotation.missing.default.value.1" -> IProblem.MissingValueForAnnotationMember;
			case "compiler.warn.static.not.qualified.by.type" -> {
				var kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
				yield kind == null ? IProblem.NonStaticAccessToStaticField :
					switch (kind) {
						case METHOD -> IProblem.NonStaticAccessToStaticMethod;
						case VAR, RECORD_COMPONENT -> IProblem.NonStaticAccessToStaticField;
						default -> IProblem.NonStaticAccessToStaticField;
				};
			}
			case "compiler.err.illegal.static.intf.meth.call" -> IProblem.InterfaceStaticMethodInvocationNotBelow18;
			case "compiler.err.recursive.ctor.invocation" -> IProblem.RecursiveConstructorInvocation;
			case "compiler.err.illegal.text.block.open" -> IProblem.Syntax;
			case "compiler.warn.prob.found.req" -> IProblem.UncheckedAccessOfValueOfFreeTypeVariable;
			case "compiler.warn.restricted.type.not.allowed" -> IProblem.RestrictedTypeName;
			case "compiler.err.override.weaker.access" -> IProblem.MethodReducesVisibility;
			case "compiler.err.enum.constant.expected" -> IProblem.Syntax;
			case "compiler.err.limit.string" -> IProblem.TooManyBytesForStringConstant;
			case "compiler.err.limit.string.overflow" -> IProblem.StringConstantIsExceedingUtf8Limit;
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
			case "compiler.warn.proc.messager", "compiler.err.proc.messager" -> {
				// probably some javadoc comment, we didn't find a good way to get javadoc
				// code/ids: there are lost in the diagnostic when going through
				// jdk.javadoc.internal.doclint.Messages.report(...) and we cannot override
				// Messages class to plug some specific strategy.
				// So we fail back to (weak) message check.
				String message = diagnostic.getMessage(Locale.ENGLISH).toLowerCase();
				if (message.contains("no @param for")) {
					yield IProblem.JavadocMissingParamTag;
				}
				if (message.contains("no @return")) {
					yield IProblem.JavadocMissingReturnTag;
				}
				if (message.contains("@param name not found")) {
					yield IProblem.JavadocInvalidParamName;
				}
				if (message.contains("no @throws for ")) {
					yield IProblem.JavadocMissingThrowsTag;
				}
				if (message.contains("invalid use of @return")) {
					yield IProblem.JavadocUnexpectedTag;
				}
				if (message.startsWith("exception not thrown: ")) {
					yield IProblem.JavadocInvalidThrowsClassName;
				}
				if (message.startsWith("@param ") && message.endsWith(" has already been specified")) {
					yield IProblem.JavadocDuplicateParamName;
				}
				if (message.contains("no comment")) {
					yield IProblem.JavadocMissing;
				}
				if (message.contains("empty comment") && diagnostic instanceof JCDiagnostic jcDiag && jcDiag.getDiagnosticPosition() instanceof JCMethodDecl method) {
					if (method.getReturnType() instanceof JCPrimitiveTypeTree primitiveType
						&& primitiveType.getPrimitiveTypeKind() != TypeKind.VOID) {
						yield IProblem.JavadocMissingReturnTag;
					}
					// TODO also return a IProblem.JavadocMissingParamTag for each arg
				}
				// most others are ignored
				yield -1;
			}
			case "compiler.err.doesnt.exist" -> {
				JCCompilationUnit unit = units.get(diagnostic.getSource());
				if (unit != null) {
					long diagPos = diagnostic.getPosition();
					boolean isImport = unit.getImports().stream().anyMatch(jcImport -> diagPos >= jcImport.getStartPosition() && diagPos <= jcImport.getEndPosition(unit.endPositions));
					if (isImport) {
						yield IProblem.ImportNotFound;
					}
					if (unit.getModule() != null) {
						yield IProblem.PackageDoesNotExistOrIsEmpty;
					}
				}
				yield IProblem.UndefinedType;
			}
			case "compiler.err.override.meth" -> diagnostic.getMessage(Locale.ENGLISH).contains("static") ?
					IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod :
					IProblem.FinalMethodCannotBeOverridden;
			case "compiler.err.unclosed.char.lit", "compiler.err.empty.char.lit" -> IProblem.InvalidCharacterConstant;
			case "compiler.err.malformed.fp.lit" -> IProblem.InvalidFloat;
			case "compiler.warn.missing.deprecated.annotation" -> {
				if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
					yield -1;
				}
				DiagnosticPosition pos = jcDiagnostic.getDiagnosticPosition();
				if (pos instanceof JCTree.JCVariableDecl) {
					yield IProblem.FieldMissingDeprecatedAnnotation;
				} else if (pos instanceof JCTree.JCMethodDecl) {
					yield IProblem.MethodMissingDeprecatedAnnotation;
				} else if (pos instanceof JCTree.JCClassDecl) {
					yield IProblem.TypeMissingDeprecatedAnnotation;
				}
				ILog.get().error("Could not convert diagnostic " + diagnostic);
				yield -1;
			}
			case "compiler.warn.override.equals.but.not.hashcode" -> IProblem.ShouldImplementHashcode;
			case "compiler.warn.unchecked.call.mbr.of.raw.type" -> IProblem.UnsafeRawMethodInvocation;
			case "compiler.err.cant.inherit.from.sealed" -> {
				Symbol.ClassSymbol sym = getDiagnosticArgumentByType(diagnostic, Symbol.ClassSymbol.class);
				if (sym == null) {
					yield -1;
				}
				if (sym.isInterface()) {
					yield IProblem.SealedSuperInterfaceDoesNotPermit;
				} else {
					yield IProblem.SealedSuperClassDoesNotPermit;
				}
			}
			case "compiler.err.non.sealed.sealed.or.final.expected" -> IProblem.SealedMissingClassModifier;
			case "compiler.err.enum.annotation.must.be.enum.constant" -> IProblem.AnnotationValueMustBeAnEnumConstant;
			case "compiler.err.package.in.other.module" -> IProblem.ConflictingPackageFromOtherModules;
			case "compiler.err.module.decl.sb.in.module-info.java" -> {
				if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
					yield -1;
				}
				DiagnosticPosition pos = jcDiagnostic.getDiagnosticPosition();
				if (pos instanceof JCTree.JCModuleDecl) {
					yield IProblem.ParsingErrorOnKeywordNoSuggestion;
				} else if (pos instanceof JCTree.JCModuleImport) {
				}
				ILog.get().error("Could not convert diagnostic " + diagnostic);
				yield -1;
			}
			case "compiler.err.file.sb.on.source.or.patch.path.for.module" -> IProblem.ParsingErrorOnKeywordNoSuggestion;
			case "compiler.err.package.not.visible" -> IProblem.NotVisibleType;
			case "compiler.err.expected4" -> IProblem.Syntax;
			case "compiler.err.no.intf.expected.here" -> IProblem.SuperclassMustBeAClass;
			case "compiler.err.intf.expected.here" -> IProblem.SuperInterfaceMustBeAnInterface;
			case "compiler.err.method.does.not.override.superclass" -> IProblem.MethodMustOverrideOrImplement;
			case "compiler.err.name.clash.same.erasure.no.override" -> IProblem.DuplicateMethodErasure;
			case "compiler.err.cant.deref" -> IProblem.NoMessageSendOnBaseType;
			case "compiler.err.cant.infer.local.var.type" -> IProblem.VarLocalWithoutInitizalier;
			case "compiler.err.array.and.varargs" -> IProblem.RedefinedArgument;
			case "compiler.err.type.doesnt.take.params" -> IProblem.NonGenericType;
			case "compiler.err.static.imp.only.classes.and.interfaces" -> IProblem.InvalidTypeForStaticImport;
			case "compiler.err.service.implementation.is.abstract" -> IProblem.AbstractServiceImplementation;
			case "compiler.err.service.implementation.no.args.constructor.not.public" -> IProblem.ServiceImplDefaultConstructorNotPublic;
			case "compiler.err.service.implementation.doesnt.have.a.no.args.constructor" -> IProblem.ProviderMethodOrConstructorRequiredForServiceImpl;
			case "compiler.err.not.exhaustive" -> IProblem.SwitchExpressionsYieldMissingDefaultCase;
			case "compiler.err.switch.expression.empty" -> IProblem.SwitchExpressionsYieldMissingDefaultCase;
			case "compiler.err.switch.mixing.case.types" -> IProblem.SwitchPreviewMixedCase;
			case "compiler.err.return.outside.switch.expression" -> IProblem.SwitchExpressionsReturnWithinSwitchExpression;
			case "compiler.err.cant.apply.diamond.1" -> IProblem.NonGenericType;
			case "compiler.err.class.in.unnamed.module.cant.extend.sealed.in.diff.package" -> IProblem.SealedPermittedTypeOutsideOfPackage;
			case "compiler.err.non.sealed.or.sealed.expected" -> IProblem.SealedMissingInterfaceModifier;
			case "compiler.err.array.dimension.missing" -> IProblem.MustDefineEitherDimensionExpressionsOrInitializer;
			case "compiler.warn.deprecated.annotation.has.no.effect" -> IProblem.TypeRelated; // not in ECJ
			case "compiler.err.enum.constant.not.expected" -> IProblem.UndefinedMethod;
			case "compiler.warn.poor.choice.for.module.name" -> IProblem.ModuleRelated;
			case "compiler.err.try.without.catch.finally.or.resource.decls" -> IProblem.Syntax;
			case "compiler.warn.unchecked.meth.invocation.applied" -> IProblem.UnsafeTypeConversion;
			case "compiler.warn.override.unchecked.ret" -> IProblem.UnsafeReturnTypeOverride;
			case "compiler.err.encl.class.required" -> IProblem.MissingEnclosingInstanceForConstructorCall;
			case "compiler.err.operator.cant.be.applied", "compiler.err.operator.cant.be.applied.1" -> IProblem.InvalidOperator;
			case "compiler.warn.try.resource.not.referenced" -> IProblem.LocalVariableIsNeverUsed; // not in ECJ
			case "compiler.warn.try.explicit.close.call" -> IProblem.ExplicitlyClosedAutoCloseable;
			case "compiler.err.types.incompatible" -> IProblem.DuplicateInheritedDefaultMethods;
			case "compiler.err.incompatible.thrown.types.in.mref" -> IProblem.UnhandledException;
			case "compiler.err.already.defined.single.import" -> IProblem.ConflictingImport;
			case "compiler.err.icls.cant.have.static.decl" -> IProblem.UnexpectedStaticModifierForMethod;
			case "compiler.err.override.static" -> IProblem.CannotHideAnInstanceMethodWithAStaticMethod;
			case "compiler.err.native.meth.cant.have.body" -> IProblem.BodyForNativeMethod;
			case "compiler.err.varargs.invalid.trustme.anno" -> IProblem.SafeVarargsOnFixedArityMethod;
			case "compiler.warn.unchecked.generic.array.creation" -> IProblem.UnsafeGenericArrayForVarargs;
			case "compiler.warn.varargs.redundant.trustme.anno" -> IProblem.TypeRelated; // not in ECJ
			case "compiler.warn.finally.cannot.complete" -> IProblem.FinallyMustCompleteNormally;
			case "compiler.err.generic.throwable" -> IProblem.GenericTypeCannotExtendThrowable;
			case "compiler.warn.potentially.ambiguous.overload" -> IProblem.TypeRelated; // not in ECJ
			case "compiler.warn.inexact.non-varargs.call" -> IProblem.MethodVarargsArgumentNeedCast;
			case "compiler.note.deprecated.filename" -> IProblem.OverridingDeprecatedMethod;
			case "compiler.note.unchecked.plural.additional" -> IProblem.TypeRelated; // not in ECJ; this is a project-wide warning
			case "compiler.err.error.reading.file" -> IProblem.CannotReadSource;
			case "compiler.err.dot.class.expected" -> IProblem.TypeRelated; //not in ECJ
			case "compiler.err.feature.not.supported.in.source" -> IProblem.FeatureNotSupported;
			case "compiler.err.annotation.type.not.applicable.to.type", "compiler.err.annotation.type.not.applicable" -> {
				if (diagnostic instanceof JCDiagnostic jcDiagnostic && jcDiagnostic.getDiagnosticPosition() instanceof JCAnnotation jcAnnotation
						&& jcAnnotation.type.tsym.getAnnotationTypeMetadata().getTarget() == null) {
					yield IProblem.ExplicitAnnotationTargetRequired;
				}
				yield IProblem.DisallowedTargetForAnnotation;
			}
			case "compiler.err.pkg.annotations.sb.in.package-info.java" -> IProblem.InvalidFileNameForPackageAnnotations;
			case "compiler.err.unexpected.type" -> IProblem.TypeMismatch;
			case "compiler.err.intf.annotation.members.cant.have.params" -> IProblem.AnnotationMembersCannotHaveParameters;
			case "compiler.err.static.declaration.not.allowed.in.inner.classes" -> {
				if (diagnostic instanceof JCDiagnostic jcDiagnostic && jcDiagnostic.getDiagnosticPosition() instanceof JCClassDecl classDecl && classDecl.sym.isEnum()) {
					yield IProblem.NonStaticContextForEnumMemberType;
				}
				yield IProblem.IllegalStaticModifierForMemberType;
			}
			case "compiler.err.new.not.allowed.in.annotation" -> IProblem.AnnotationValueMustBeConstant;
			case "compiler.err.foreach.not.applicable.to.type" -> IProblem.InvalidTypeForCollection;
			case "compiler.err.this.as.identifier" -> IProblem.Syntax;
			case "compiler.err.int.number.too.large" -> IProblem.NumericValueOutOfRange;
			case "compiler.err.type.var.cant.be.deref" -> IProblem.IllegalAccessFromTypeVariable;
			case "compiler.err.try.with.resources.expr.needs.var" -> IProblem.Syntax;
			case "compiler.err.catch.without.try" -> IProblem.Syntax;
			case "compiler.err.not.encl.class" -> IProblem.IllegalEnclosingInstanceSpecification;
			case "compiler.err.type.found.req" -> IProblem.DisallowedTargetForAnnotation;
			case "compiler.warn.try.resource.throws.interrupted.exc" -> IProblem.UnhandledExceptionOnAutoClose;
			case "compiler.err.cyclic.inheritance" -> IProblem.HierarchyCircularity;
			case "compiler.err.incorrect.receiver.type" -> IProblem.IllegalTypeForExplicitThis;
			case "compiler.err.incorrect.constructor.receiver.type" -> IProblem.IllegalTypeForExplicitThis;
			case "compiler.err.incorrect.constructor.receiver.name" -> IProblem.IllegalQualifierForExplicitThis;
			case "compiler.err.too.many.modules" -> IProblem.ModuleRelated;
			case "compiler.err.call.must.only.appear.in.ctor" -> IProblem.InvalidExplicitConstructorCall;
			case "compiler.err.void.not.allowed.here" -> IProblem.ParameterMismatch;
			case "compiler.err.abstract.cant.be.accessed.directly" -> IProblem.DirectInvocationOfAbstractMethod;
			case "compiler.warn.annotation.method.not.found" -> IProblem.UndefinedAnnotationMember;
			case "compiler.err.import.module.not.found" -> IProblem.UndefinedModule;
			default -> {
				ILog.get().error("Could not accurately convert diagnostic (" + diagnostic.getCode() + ")\n" + diagnostic);
				if (diagnostic.getKind() == javax.tools.Diagnostic.Kind.ERROR && diagnostic.getCode().startsWith("compiler.err")) {
					yield IProblem.Unclassified;
				}
				yield -1;
			}
		};
	}

	private String readIdentifier(String documentText, long position) {
		int endIndex = (int)position;
		if (!Character.isJavaIdentifierStart(documentText.charAt(endIndex))) {
			return null;
		}
		endIndex++;
		do {
			endIndex++;
		} while (endIndex < documentText.length() && Character.isJavaIdentifierPart(documentText.charAt(endIndex)));
		return documentText.substring((int)position, endIndex);
	}

	private int uselessStrictfp(Diagnostic<? extends JavaFileObject> diagnostic) {
		TreePath path = getTreePath(diagnostic);
		if (path != null && path.getLeaf() instanceof JCMethodDecl && path.getParentPath() != null && path.getParentPath().getLeaf() instanceof JCClassDecl) {
			return IProblem.IllegalStrictfpForAbstractInterfaceMethod;
		}
		return IProblem.StrictfpNotRequired;
	}

	private int illegalCombinationOfModifiers(Diagnostic<? extends JavaFileObject> diagnostic) {
		String message = diagnostic.getMessage(Locale.ENGLISH);
		TreePath path = getTreePath(diagnostic);
		if (path != null) {
			var leaf = path.getLeaf();
			var parentPath = path.getParentPath();
			var parentNode = parentPath != null ? parentPath.getLeaf() : null;
			if (message.contains("public") || message.contains("protected") || message.contains("private")) {
				if (leaf instanceof JCMethodDecl) {
					return IProblem.IllegalVisibilityModifierCombinationForMethod;
				} else if (leaf instanceof JCClassDecl && parentNode instanceof JCClassDecl parentDecl) {
					return switch (parentDecl.getKind()) {
						case INTERFACE -> IProblem.IllegalVisibilityModifierForInterfaceMemberType;
						default -> IProblem.IllegalVisibilityModifierCombinationForMemberType;
					};
				} else if (leaf instanceof JCVariableDecl && parentNode instanceof JCClassDecl) {
					return IProblem.IllegalVisibilityModifierCombinationForField;
				}
			} else if (leaf instanceof JCMethodDecl) {
				if (parentNode instanceof JCClassDecl declaringClass) {
					if (declaringClass.getKind() == Kind.INTERFACE) {
						return IProblem.IllegalModifierCombinationForInterfaceMethod;
					}
					if (message.contains("abstract") && message.contains("final")) {
						return IProblem.IllegalModifierCombinationFinalAbstractForClass;
					}
				}
			} else if (leaf instanceof JCVariableDecl && parentNode instanceof JCClassDecl) {
				if (message.contains("volatile") && message.contains("final")) {
					return IProblem.IllegalModifierCombinationFinalVolatileForField;
				}
			}
		}
		return IProblem.IllegalModifiers;
	}

	private int illegalModifier(Diagnostic<? extends JavaFileObject> diagnostic) {
		TreePath path = getTreePath(diagnostic);
		while (path != null) {
			var leaf = path.getLeaf();
			var parentPath = path.getParentPath();
			var parentNode = parentPath != null ? parentPath.getLeaf() : null;
			if (leaf instanceof JCMethodDecl methodDecl) {
				if (parentNode instanceof JCClassDecl classDecl) {
					return methodDecl.getReturnType() == null
						? switch (classDecl.getKind()) {
							case ENUM -> IProblem.IllegalModifierForEnumConstructor;
							default -> IProblem.IllegalModifierForConstructor;
						} : switch (classDecl.getKind()) {
							case INTERFACE -> {
								if (compilerOptions.complianceLevel < ClassFileConstants.JDK1_8) {
									yield IProblem.IllegalModifierForInterfaceMethod;
								} else if (compilerOptions.complianceLevel < ClassFileConstants.JDK9) {
									yield IProblem.IllegalModifierForInterfaceMethod18;
								} else {
									yield IProblem.IllegalModifierForInterfaceMethod9;
								}
							}
							case ANNOTATION_TYPE -> IProblem.IllegalModifierForAnnotationMethod;
							default -> IProblem.IllegalModifierForMethod;
						};
				}
				return IProblem.IllegalModifierForMethod;
			} else if (leaf instanceof JCClassDecl classDecl) {
				return parentNode instanceof JCClassDecl ? switch (classDecl.getKind()) {
					case RECORD -> IProblem.RecordIllegalModifierForInnerRecord;
					case ENUM -> IProblem.IllegalModifierForMemberEnum;
					case INTERFACE -> IProblem.IllegalModifierForMemberInterface;
					default -> IProblem.IllegalModifierForMemberClass;
				} : parentNode instanceof JCCompilationUnit ? switch (classDecl.getKind()) {
					case RECORD -> IProblem.RecordIllegalModifierForRecord;
					case ENUM -> IProblem.IllegalModifierForEnum;
					case INTERFACE -> IProblem.IllegalModifierForInterface;
					default -> IProblem.IllegalModifierForClass;
				} : switch (classDecl.getKind()) {
					case RECORD -> IProblem.RecordIllegalModifierForLocalRecord;
					case ENUM -> IProblem.IllegalModifierForLocalEnumDeclaration;
					default -> IProblem.IllegalModifierForLocalClass;
				};
			} else if (leaf instanceof JCVariableDecl) {
					if (parentNode instanceof JCMethodDecl) {
						return IProblem.IllegalModifierForArgument;
					} else if (parentNode instanceof JCClassDecl classDecl) {
						return switch (classDecl.getKind()) {
							case INTERFACE -> IProblem.IllegalModifierForInterfaceField;
							default-> IProblem.IllegalModifierForField;
						};
					}
			}
			path = parentPath;
		}
		return IProblem.IllegalModifiers;
	}

	private boolean isInAnonymousClass(Diagnostic<? extends JavaFileObject> diagnostic) {
		TreePath path = getTreePath(diagnostic);
		while (path != null) {
			if (path.getLeaf() instanceof JCNewClass newClass) {
				return newClass.getClassBody() != null;
			}
			if (path.getLeaf() instanceof JCClassDecl) {
				return false;
			}
			path = path.getParentPath();
		}
		return false;
	}
	// compiler.err.cant.resolve
	private int convertUnresolved(Diagnostic<?> diagnostic) {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			if (jcDiagnostic.getDiagnosticPosition() instanceof JCTree.JCFieldAccess) {
				return IProblem.UndefinedField;
			}
		}
		return switch (getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class)) {
			case CLASS, INTERFACE, RECORD, ENUM -> IProblem.UndefinedType;
			case METHOD -> IProblem.UndefinedMethod;
			case MODULE -> IProblem.UndefinedModule;
			case VAR -> IProblem.UnresolvedVariable;
			default -> IProblem.UnresolvedVariable;
		};
	}

	private int convertUndefinedMethod(Diagnostic<?> diagnostic) {
		JCDiagnostic diagnosticArg = getDiagnosticArgumentByType(diagnostic, JCDiagnostic.class);
		if (diagnosticArg != null) {
			Type receiverArg = getDiagnosticArgumentByType(diagnosticArg, Type.class);
			if (receiverArg.hasTag(TypeTag.ARRAY)) {
				return IProblem.NoMessageSendOnArrayType;
			}
		}

		if ("compiler.err.cant.resolve.args".equals(diagnostic.getCode())) {
			Kinds.KindName kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
			if (kind == Kinds.KindName.CONSTRUCTOR) {
				return IProblem.UndefinedConstructor;
			}
		}

		TreePath treePath = getTreePath(diagnostic);
		if (treePath != null) {
			// @Annot(unknownArg = 1)
			if (treePath.getParentPath() != null && treePath.getParentPath().getLeaf() instanceof JCAssign
				&& treePath.getParentPath().getParentPath() != null && treePath.getParentPath().getParentPath().getLeaf() instanceof JCAnnotation) {
				return IProblem.UndefinedAnnotationMember;
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

	private Object[] getDiagnosticArguments(Diagnostic<?> diagnostic) {
		if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
			return new Object[0];
		}

		return jcDiagnostic.getArgs();
	}

	private String[] getDiagnosticStringArguments(Diagnostic<?> diagnostic) {
		if (!(diagnostic instanceof JCDiagnostic jcDiagnostic)) {
			return new String[0];
		}

		if (!jcDiagnostic.getSubdiagnostics().isEmpty()) {
			jcDiagnostic = jcDiagnostic.getSubdiagnostics().get(0);
		}

		if (jcDiagnostic.getArgs().length != 0
				&& jcDiagnostic.getArgs()[0] instanceof JCDiagnostic argDiagnostic) {
			return Stream.of(argDiagnostic.getArgs()) //
					.filter(Predicate.not(KindName.class::isInstance)) // can confuse JDT-LS
					.map(Object::toString) //
					.toArray(String[]::new);
		}

		return Stream.of(jcDiagnostic.getArgs()) //
				.filter(Predicate.not(KindName.class::isInstance)) // can confuse JDT-LS
				.map(Object::toString) //
				.toArray(String[]::new);
	}

	// compiler.err.prob.found.req -> TypeMismatch, ReturnTypeMismatch, IllegalCast, VoidMethodReturnsValue...
	private int convertTypeMismatch(Diagnostic<?> diagnostic) {
		Diagnostic<?> diagnosticArg = getDiagnosticArgumentByType(diagnostic, Diagnostic.class);
		if (diagnosticArg != null) {
			if ("compiler.misc.inconvertible.types".equals(diagnosticArg.getCode())) {
				Object[] args = getDiagnosticArguments(diagnosticArg);
				if (args != null && args.length > 1
					&& args[1] instanceof Type.JCVoidType) {
					return IProblem.MethodReturnsVoid;
				}
				TreePath path = getTreePath(diagnostic);
				if (path != null && path.getParentPath() != null
					&& path.getParentPath().getLeaf() instanceof JCNewClass) {
					return IProblem.UndefinedConstructor;
				}
			} else if ("compiler.misc.unexpected.ret.val".equals(diagnosticArg.getCode())) {
				return IProblem.VoidMethodReturnsValue;
			} else if ("compiler.misc.missing.ret.val".equals(diagnosticArg.getCode())) {
				return IProblem.ShouldReturnValue;
			} else if ("compiler.misc.incompatible.ret.type.in.lambda".equals(diagnosticArg.getCode())) {
				return IProblem.ShouldReturnValue;
			}
		}
		if (diagnostic instanceof JCDiagnostic jcDiagnostic && jcDiagnostic.getDiagnosticPosition() instanceof JCTree tree) {
			JCCompilationUnit unit = units.get(jcDiagnostic.getSource());
			if (unit != null) {
				// is the error in a method argument?
				TreePath path = JavacTrees.instance(context).getPath(unit, tree);
				if (path != null) {
					path = path.getParentPath();
				}
				if (path != null && path.getLeaf() instanceof JCEnhancedForLoop) {
					return IProblem.IncompatibleTypesInForeach;
				}
				while (path != null && path.getLeaf() instanceof JCExpression) {
					if (path.getLeaf() instanceof JCMethodInvocation) {
						return IProblem.ParameterMismatch;
					}
					path = path.getParentPath();
				}
			}
		}
		return IProblem.TypeMismatch;
	}

	private TreePath getTreePath(Diagnostic<?> diagnostic) {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic && jcDiagnostic.getDiagnosticPosition() instanceof JCTree tree) {
			JCCompilationUnit unit = units.get(jcDiagnostic.getSource());
			if (unit != null) {
				return JavacTrees.instance(context).getPath(unit, tree);
			}
		}
		return null;
	}

	private int convertNotVisibleAccess(Diagnostic<?> diagnostic) {
		if (diagnostic instanceof JCDiagnostic jcDiagnostic) {
			Object[] args = jcDiagnostic.getArgs();
			if (args != null && args.length > 0) {
				if (args[0] == Kinds.KindName.CONSTRUCTOR) {
					Object lastArg = args[args.length - 1];
					if (lastArg instanceof JCDiagnostic subDiagnostic) {
						args = subDiagnostic.getArgs();
					} else {
						return IProblem.NotVisibleConstructor;
					}
				}
				if (args[0] instanceof Symbol.MethodSymbol methodSymbol) {
					if (methodSymbol.isConstructor()) {
						TreePath treePath = getTreePath(jcDiagnostic);
						while (treePath != null && !(treePath.getLeaf() instanceof JCMethodDecl)) {
							treePath = treePath.getParentPath();
						}
						return treePath != null && treePath.getLeaf() instanceof JCMethodDecl methodDecl && (methodDecl.sym.flags() & Flags.GENERATEDCONSTR) != 0 ?
							IProblem.NotVisibleConstructorInDefaultConstructor : IProblem.NotVisibleConstructor;
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

	private int convertAmbiguous(Diagnostic<?> diagnostic) {
		Kinds.KindName kind = getDiagnosticArgumentByType(diagnostic, Kinds.KindName.class);
		return switch (kind) {
			case CLASS, INTERFACE -> IProblem.AmbiguousType;
			case METHOD -> IProblem.AmbiguousMethod;
			default -> 0;
		};
	}

	public void registerUnit(JavaFileObject javaFileObject, JCCompilationUnit unit) {
		this.units.put(javaFileObject, unit);
	}

}

/*******************************************************************************
 * Copyright (c) 2023, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;
import java.util.function.Predicate;

import javax.lang.model.type.TypeKind;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.core.dom.PrimitiveType.Code;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

import com.sun.source.tree.CaseTree.CaseKind;
import com.sun.tools.javac.parser.Tokens.Comment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCAnyPattern;
import com.sun.tools.javac.tree.JCTree.JCArrayAccess;
import com.sun.tools.javac.tree.JCTree.JCArrayTypeTree;
import com.sun.tools.javac.tree.JCTree.JCAssert;
import com.sun.tools.javac.tree.JCTree.JCAssign;
import com.sun.tools.javac.tree.JCTree.JCBinary;
import com.sun.tools.javac.tree.JCTree.JCBindingPattern;
import com.sun.tools.javac.tree.JCTree.JCBlock;
import com.sun.tools.javac.tree.JCTree.JCBreak;
import com.sun.tools.javac.tree.JCTree.JCCase;
import com.sun.tools.javac.tree.JCTree.JCCatch;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCConditional;
import com.sun.tools.javac.tree.JCTree.JCContinue;
import com.sun.tools.javac.tree.JCTree.JCDoWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCEnhancedForLoop;
import com.sun.tools.javac.tree.JCTree.JCErroneous;
import com.sun.tools.javac.tree.JCTree.JCExpression;
import com.sun.tools.javac.tree.JCTree.JCExpressionStatement;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.tree.JCTree.JCForLoop;
import com.sun.tools.javac.tree.JCTree.JCIdent;
import com.sun.tools.javac.tree.JCTree.JCIf;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.tree.JCTree.JCInstanceOf;
import com.sun.tools.javac.tree.JCTree.JCLabeledStatement;
import com.sun.tools.javac.tree.JCTree.JCLambda;
import com.sun.tools.javac.tree.JCTree.JCLiteral;
import com.sun.tools.javac.tree.JCTree.JCMemberReference;
import com.sun.tools.javac.tree.JCTree.JCMethodDecl;
import com.sun.tools.javac.tree.JCTree.JCMethodInvocation;
import com.sun.tools.javac.tree.JCTree.JCModifiers;
import com.sun.tools.javac.tree.JCTree.JCNewArray;
import com.sun.tools.javac.tree.JCTree.JCNewClass;
import com.sun.tools.javac.tree.JCTree.JCPackageDecl;
import com.sun.tools.javac.tree.JCTree.JCParens;
import com.sun.tools.javac.tree.JCTree.JCPattern;
import com.sun.tools.javac.tree.JCTree.JCPrimitiveTypeTree;
import com.sun.tools.javac.tree.JCTree.JCReturn;
import com.sun.tools.javac.tree.JCTree.JCStatement;
import com.sun.tools.javac.tree.JCTree.JCSwitch;
import com.sun.tools.javac.tree.JCTree.JCSynchronized;
import com.sun.tools.javac.tree.JCTree.JCThrow;
import com.sun.tools.javac.tree.JCTree.JCTry;
import com.sun.tools.javac.tree.JCTree.JCTypeApply;
import com.sun.tools.javac.tree.JCTree.JCTypeCast;
import com.sun.tools.javac.tree.JCTree.JCTypeIntersection;
import com.sun.tools.javac.tree.JCTree.JCTypeUnion;
import com.sun.tools.javac.tree.JCTree.JCUnary;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.tree.JCTree.JCWhileLoop;
import com.sun.tools.javac.tree.JCTree.JCWildcard;
import com.sun.tools.javac.tree.JCTree.JCYield;
import com.sun.tools.javac.tree.JCTree.Tag;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Position.LineMap;

/**
 * Deals with conversion of Javac domain into JDT DOM domain
 * @implNote Cannot move to another package as it uses some package protected methods
 */
@SuppressWarnings("unchecked")
class JavacConverter {

	public final AST ast;
	private final JCCompilationUnit javacCompilationUnit;
	private final Context context;
	final Map<ASTNode, JCTree> domToJavac = new HashMap<>();
	//private final Map<JCTree, ASTNode> javacToDom = new HashMap<>();

	public JavacConverter(AST ast, JCCompilationUnit javacCompilationUnit, Context context) {
		this.ast = ast;
		this.javacCompilationUnit = javacCompilationUnit;
		this.context = context;
	}

	CompilationUnit convertCompilationUnit(JCCompilationUnit javacCompilationUnit) {
		CompilationUnit res = this.ast.newCompilationUnit();
		populateCompilationUnit(res, javacCompilationUnit);
		return res;
	}

	void populateCompilationUnit(CompilationUnit res, JCCompilationUnit javacCompilationUnit) {
		commonSettings(res, javacCompilationUnit);
		res.setLineEndTable(toLineEndPosTable(javacCompilationUnit.getLineMap(), res.getLength()));
		if (javacCompilationUnit.getPackage() != null) {
			res.setPackage(convert(javacCompilationUnit.getPackage()));
		}
		javacCompilationUnit.getImports().stream().map(jc -> convert(jc)).forEach(res.imports()::add);
		javacCompilationUnit.getTypeDecls().stream()
			.map(this::convertBodyDeclaration)
			.forEach(res.types()::add);
		res.accept(new FixPositions());
	}

	private int[] toLineEndPosTable(LineMap lineMap, int fileLength) {
		List<Integer> lineEnds = new ArrayList<>();
		int line = 1;
		try {
			do {
				lineEnds.add(lineMap.getStartPosition(line + 1) - 1);
				line++;
			} while (true);
		} catch (ArrayIndexOutOfBoundsException ex) {
			// expected
		}
		lineEnds.add(fileLength - 1);
		return lineEnds.stream().mapToInt(Integer::intValue).toArray();
	}

	private PackageDeclaration convert(JCPackageDecl javac) {
		PackageDeclaration res = this.ast.newPackageDeclaration();
		res.setName(toName(javac.getPackageName()));
		commonSettings(res, javac);
		return res;
	}

	private ImportDeclaration convert(JCImport javac) {
		ImportDeclaration res = this.ast.newImportDeclaration();
		commonSettings(res, javac);
		if (javac.isStatic()) {
			res.setStatic(true);
		}
		var select = javac.getQualifiedIdentifier();
		if (select.getIdentifier().contentEquals("*")) {
			res.setOnDemand(true);
			res.setName(toName(select.getExpression()));
		} else {
			res.setName(toName(select));
		}
		return res;
	}

	private void commonSettings(ASTNode res, JCTree javac) {
		if (javac.getStartPosition() >= 0) {
			int length = javac.getEndPosition(this.javacCompilationUnit.endPositions) - javac.getStartPosition();
			res.setSourceRange(javac.getStartPosition(), Math.max(0, length));
		}
		this.domToJavac.put(res, javac);
	}

	private Name toName(JCTree expression) {
		if (expression instanceof JCIdent ident) {
			Name res = convert(ident.getName());
			commonSettings(res, ident);
			return res;
		}
		if (expression instanceof JCFieldAccess fieldAccess) {
			QualifiedName res = this.ast.newQualifiedName(toName(fieldAccess.getExpression()), (SimpleName)convert(fieldAccess.getIdentifier()));
			commonSettings(res, fieldAccess);
			return res;
		}
		throw new UnsupportedOperationException("toName for " + expression + " (" + expression.getClass().getName() + ")");
	}

	private AbstractTypeDeclaration convertClassDecl(JCClassDecl javacClassDecl) {
		AbstractTypeDeclaration	res = switch (javacClassDecl.getKind()) {
			case ANNOTATED_TYPE -> this.ast.newAnnotationTypeDeclaration();
			case ENUM -> this.ast.newEnumDeclaration();
			case RECORD -> this.ast.newRecordDeclaration();
			case INTERFACE -> {
				TypeDeclaration decl = this.ast.newTypeDeclaration();
				decl.setInterface(true);
				yield decl;
			}
			case CLASS -> this.ast.newTypeDeclaration();
			default -> throw new IllegalStateException();
		};
		commonSettings(res, javacClassDecl);
		res.setName((SimpleName)convert(javacClassDecl.getSimpleName()));
		res.modifiers().addAll(convert(javacClassDecl.mods));
		if (res instanceof TypeDeclaration typeDeclaration) {
			if (javacClassDecl.getExtendsClause() != null) {
				typeDeclaration.setSuperclassType(convertToType(javacClassDecl.getExtendsClause()));
			}
			if (javacClassDecl.getImplementsClause() != null) {
				javacClassDecl.getImplementsClause().stream()
					.map(this::convertToType)
					.forEach(typeDeclaration.superInterfaceTypes()::add);
			}
			if (javacClassDecl.getPermitsClause() != null) {
				javacClassDecl.getPermitsClause().stream()
					.map(this::convertToType)
					.forEach(typeDeclaration.permittedTypes()::add);
			}
			if (javacClassDecl.getMembers() != null) {
				javacClassDecl.getMembers().stream()
					.map(this::convertBodyDeclaration)
					.forEach(typeDeclaration.bodyDeclarations()::add);
			}
//
//			Javadoc doc = this.ast.newJavadoc();
//			TagElement tagElement = this.ast.newTagElement();
//			TextElement textElement = this.ast.newTextElement();
//			textElement.setText("Hello");
//			tagElement.fragments().add(textElement);
//			doc.tags().add(tagElement);
//			res.setJavadoc(doc);
		}
		// TODO Javadoc
		return res;
	}

	private ASTNode convertBodyDeclaration(JCTree tree) {
		if (tree instanceof JCMethodDecl methodDecl) {
			return convertMethodDecl(methodDecl);
		}
		if (tree instanceof JCClassDecl jcClassDecl) {
			return convertClassDecl(jcClassDecl);
		}
		if (tree instanceof JCVariableDecl jcVariableDecl) {
			return convertFieldDeclaration(jcVariableDecl);
		}
		if (tree instanceof JCBlock block) {
			Initializer res = this.ast.newInitializer();
			commonSettings(res, tree);
			res.setBody(convertBlock(block));
			return res;
		}
		throw new UnsupportedOperationException("Unsupported " + tree + " of type" + tree.getClass());
	}

	private MethodDeclaration convertMethodDecl(JCMethodDecl javac) {
		MethodDeclaration res = this.ast.newMethodDeclaration();
		commonSettings(res, javac);
		res.modifiers().addAll(convert(javac.getModifiers()));
		res.setConstructor(Objects.equals(javac.getName(), Names.instance(this.context).init));
		if (!res.isConstructor()) {
			res.setName((SimpleName)convert(javac.getName()));
		}
		if (javac.getReturnType() != null) {
		//res.setConstructor(javac.);
			res.setReturnType2(convertToType(javac.getReturnType()));
		}
		javac.getParameters().stream().map(this::convertVariableDeclaration).forEach(res.parameters()::add);
		if (javac.getBody() != null) {
			res.setBody(convertBlock(javac.getBody()));
		}
		return res;
	}

	private VariableDeclaration convertVariableDeclaration(JCVariableDecl javac) {
		// if (singleDecl) {
		SingleVariableDeclaration res = this.ast.newSingleVariableDeclaration();
		commonSettings(res, javac);
		if (convert(javac.getName()) instanceof SimpleName simpleName) {
			res.setName(simpleName);
		}
		res.modifiers().addAll(convert(javac.getModifiers()));
		if (javac.getType() != null) {
			res.setType(convertToType(javac.getType()));
		}
		if (javac.getInitializer() != null) {
			res.setInitializer(convertExpression(javac.getInitializer()));
		}
		return res;
	}

	private FieldDeclaration convertFieldDeclaration(JCVariableDecl javac) {
		// if (singleDecl) {
		VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
		commonSettings(fragment, javac);
		if (convert(javac.getName()) instanceof SimpleName simpleName) {
			fragment.setName(simpleName);
		}
		if (javac.getInitializer() != null) {
			fragment.setInitializer(convertExpression(javac.getInitializer()));
		}
		FieldDeclaration res = this.ast.newFieldDeclaration(fragment);
		commonSettings(res, javac);
		res.modifiers().addAll(convert(javac.getModifiers()));
		res.setType(convertToType(javac.getType()));
		return res;
	}

	private Expression convertExpression(JCExpression javac) {
		if (javac instanceof JCIdent ident) {
			if (Objects.equals(ident.name, Names.instance(this.context)._this)) {
				ThisExpression res = this.ast.newThisExpression();
				commonSettings(res, javac);
				return res;
			}
			return toName(ident);
		}
		if (javac instanceof JCLiteral literal) {
			return convertLiteral(literal);
		}
		if (javac instanceof JCFieldAccess fieldAccess) {
			if (Objects.equals(Names.instance(this.context)._class, fieldAccess.getIdentifier())) {
				TypeLiteral res = this.ast.newTypeLiteral();
				commonSettings(res, javac);
				res.setType(convertToType(fieldAccess.getExpression()));
				return res;
			}
			if (fieldAccess.getExpression() instanceof JCFieldAccess parentFieldAccess && Objects.equals(Names.instance(this.context)._super, parentFieldAccess.getIdentifier())) {
				SuperFieldAccess res = this.ast.newSuperFieldAccess();
				commonSettings(res, javac);
				res.setQualifier(toName(parentFieldAccess.getExpression()));
				res.setName((SimpleName)convert(fieldAccess.getIdentifier()));
				return res;
			}
			FieldAccess res = this.ast.newFieldAccess();
			commonSettings(res, javac);
			res.setExpression(convertExpression(fieldAccess.getExpression()));
			if (convert(fieldAccess.getIdentifier()) instanceof SimpleName name) {
				res.setName(name);
			}
			return res;
		}
		if (javac instanceof JCMethodInvocation methodInvocation) {
			MethodInvocation res = this.ast.newMethodInvocation();
			commonSettings(res, methodInvocation);
			JCExpression nameExpr = methodInvocation.getMethodSelect();
			if (nameExpr instanceof JCIdent ident) {
				if (Objects.equals(ident.getName(), Names.instance(this.context)._super)) {
					return convertSuperMethodInvocation(methodInvocation);
				}
				SimpleName name = (SimpleName)convert(ident.getName());
				commonSettings(name, ident);
				res.setName(name);
			} else if (nameExpr instanceof JCFieldAccess access) {
				if (access.getExpression() instanceof JCFieldAccess parentAccess && Objects.equals(Names.instance(this.context)._super, parentAccess.getIdentifier())) {
					SuperMethodInvocation res2 = this.ast.newSuperMethodInvocation();
					commonSettings(res2, javac);
					methodInvocation.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
					methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
					res2.setQualifier(toName(parentAccess.getExpression()));
					res2.setName((SimpleName)convert(access.getIdentifier()));
					return res2;
				}
				res.setName((SimpleName)convert(access.getIdentifier()));
				res.setExpression(convertExpression(access.getExpression()));
			}
			if (methodInvocation.getArguments() != null) {
				methodInvocation.getArguments().stream()
					.map(this::convertExpression)
					.forEach(res.arguments()::add);
			}
			if (methodInvocation.getTypeArguments() != null) {
				methodInvocation.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
			}
			return res;
		}
		if (javac instanceof JCNewClass newClass) {
			ClassInstanceCreation res = this.ast.newClassInstanceCreation();
			commonSettings(res, javac);
			res.setType(convertToType(newClass.getIdentifier()));
			if (newClass.getClassBody() != null) {
				res.setAnonymousClassDeclaration(null); // TODO
			}
			if (newClass.getArguments() != null) {
				newClass.getArguments().stream()
					.map(this::convertExpression)
					.forEach(res.arguments()::add);
			}
			return res;
		}
		if (javac instanceof JCErroneous error) {
			if (error.getErrorTrees().size() == 1) {
				JCTree tree = error.getErrorTrees().get(0);
				if (tree instanceof JCExpression nestedExpr) {
					return convertExpression(nestedExpr);
				}
			} else {
				ParenthesizedExpression substitute = this.ast.newParenthesizedExpression();
				commonSettings(substitute, error);
				return substitute;
			}
			return null;
		}
		if (javac instanceof JCBinary binary) {
			InfixExpression res = this.ast.newInfixExpression();
			commonSettings(res, javac);
			Expression left = convertExpression(binary.getLeftOperand());
			if (left != null) {
				res.setLeftOperand(left);
			}
			Expression right = convertExpression(binary.getRightOperand());
			if (right != null) {
				res.setRightOperand(right);
			}
			res.setOperator(switch (binary.getTag()) {
				case OR -> InfixExpression.Operator.CONDITIONAL_OR;
				case AND -> InfixExpression.Operator.CONDITIONAL_AND;
				case BITOR -> InfixExpression.Operator.OR;
				case BITXOR -> InfixExpression.Operator.XOR;
				case BITAND -> InfixExpression.Operator.AND;
				case EQ -> InfixExpression.Operator.EQUALS;
				case NE -> InfixExpression.Operator.NOT_EQUALS;
				case LT -> InfixExpression.Operator.LESS;
				case GT -> InfixExpression.Operator.GREATER;
				case LE -> InfixExpression.Operator.LESS_EQUALS;
				case GE -> InfixExpression.Operator.GREATER_EQUALS;
				case SL -> InfixExpression.Operator.LEFT_SHIFT;
				case SR -> InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
				case USR -> InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
				case PLUS -> InfixExpression.Operator.PLUS;
				case MINUS -> InfixExpression.Operator.MINUS;
				case MUL -> InfixExpression.Operator.TIMES;
				case DIV -> InfixExpression.Operator.DIVIDE;
				case MOD -> InfixExpression.Operator.REMAINDER;
				default -> null;
			});
			return res;
		}
		if (javac instanceof JCUnary unary) {
			if (unary.getTag() != Tag.POSTINC && unary.getTag() != Tag.POSTDEC) {
				PrefixExpression res = this.ast.newPrefixExpression();
				commonSettings(res, javac);
				res.setOperand(convertExpression(unary.getExpression()));
				res.setOperator(switch (unary.getTag()) {
					case POS -> PrefixExpression.Operator.PLUS;
					case NEG -> PrefixExpression.Operator.MINUS;
					case NOT -> PrefixExpression.Operator.NOT;
					case COMPL -> PrefixExpression.Operator.COMPLEMENT;
					case PREINC -> PrefixExpression.Operator.INCREMENT;
					case PREDEC -> PrefixExpression.Operator.DECREMENT;
					default -> null;
				});
				return res;
			} else {
				PostfixExpression res = this.ast.newPostfixExpression();
				commonSettings(res, javac);
				res.setOperand(convertExpression(unary.getExpression()));
				res.setOperator(switch (unary.getTag()) {
					case POSTINC -> PostfixExpression.Operator.INCREMENT;
					case POSTDEC -> PostfixExpression.Operator.DECREMENT;
					default -> null;
				});
				return res;
			}
		}
		if (javac instanceof JCParens parens) {
			ParenthesizedExpression res = this.ast.newParenthesizedExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(parens.getExpression()));
			return res;
		}
		if (javac instanceof JCAssign assign) {
			Assignment res = this.ast.newAssignment();
			commonSettings(res, javac);
			res.setLeftHandSide(convertExpression(assign.getVariable()));
			res.setRightHandSide(convertExpression(assign.getExpression()));
			return res;
		}
		if (javac instanceof JCInstanceOf jcInstanceOf) {
			if (jcInstanceOf.getType() != null) {
				InstanceofExpression res = this.ast.newInstanceofExpression();
				commonSettings(res, javac);
				res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
				res.setRightOperand(convertToType(jcInstanceOf.getType()));
				return res;
			}
			JCPattern jcPattern = jcInstanceOf.getPattern();
			if (jcPattern instanceof JCAnyPattern) {
				InstanceofExpression res = this.ast.newInstanceofExpression();
				commonSettings(res, javac);
				res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
				throw new UnsupportedOperationException("Right operand not supported yet");
//				return res;
			}
			PatternInstanceofExpression res = this.ast.newPatternInstanceofExpression();
			commonSettings(res, javac);
			res.setLeftOperand(convertExpression(jcInstanceOf.getExpression()));
			if (jcPattern instanceof JCBindingPattern jcBindingPattern) {
				TypePattern jdtPattern = this.ast.newTypePattern();
				commonSettings(jdtPattern, jcBindingPattern);
				jdtPattern.setPatternVariable((SingleVariableDeclaration)convertVariableDeclaration(jcBindingPattern.var));
				res.setPattern(jdtPattern);
			} else {
				throw new UnsupportedOperationException("Missing support to convert '" + jcPattern + "' of type " + javac.getClass().getSimpleName());
			}
			return res;
		}
		if (javac instanceof JCArrayAccess jcArrayAccess) {
			ArrayAccess res = this.ast.newArrayAccess();
			commonSettings(res, javac);
			res.setArray(convertExpression(jcArrayAccess.getExpression()));
			res.setIndex(convertExpression(jcArrayAccess.getIndex()));
			return res;
		}
		if (javac instanceof JCTypeCast jcCast) {
			CastExpression res = this.ast.newCastExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcCast.getExpression()));
			res.setType(convertToType(jcCast.getType()));
			return res;
		}
		if (javac instanceof JCMemberReference jcMemberReference) {
			if (Objects.equals(Names.instance(this.context).init, jcMemberReference.getName())) {
				CreationReference res = this.ast.newCreationReference();
				commonSettings(res, javac);
				res.setType(convertToType(jcMemberReference.getQualifierExpression()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().map(this::convertToType).forEach(res.typeArguments()::add);
				}
				return res;
			} else {
				ExpressionMethodReference res = this.ast.newExpressionMethodReference();
				commonSettings(res, javac);
				res.setExpression(convertExpression(jcMemberReference.getQualifierExpression()));
				res.setName((SimpleName)convert(jcMemberReference.getName()));
				if (jcMemberReference.getTypeArguments() != null) {
					jcMemberReference.getTypeArguments().map(this::convertToType).forEach(res.typeArguments()::add);
				}
				return res;
			}
		}
		if (javac instanceof JCConditional jcCondition) {
			ConditionalExpression res = this.ast.newConditionalExpression();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcCondition.getCondition()));
			res.setThenExpression(convertExpression(jcCondition.getTrueExpression()));
			res.setElseExpression(convertExpression(jcCondition.getFalseExpression()));
			return res;
		}
		if (javac instanceof JCLambda jcLambda) {
			LambdaExpression res = this.ast.newLambdaExpression();
			jcLambda.getParameters().stream()
				.filter(JCVariableDecl.class::isInstance)
				.map(JCVariableDecl.class::cast)
				.map(this::convertVariableDeclaration)
				.forEach(res.parameters()::add);
			res.setBody(
					jcLambda.getBody() instanceof JCExpression expr ? convertExpression(expr) :
					jcLambda.getBody() instanceof JCStatement stmt ? convertStatement(stmt) :
					null);
			// TODO set parenthesis looking at the next non-whitespace char after the last parameter
			return res;
		}
		if (javac instanceof JCNewArray jcNewArray) {
			ArrayCreation res = this.ast.newArrayCreation();
			commonSettings(res, javac);
			if (jcNewArray.getType() != null) {
				Type type = convertToType(jcNewArray.getType());
				ArrayType arrayType = this.ast.newArrayType(type);
				commonSettings(arrayType, jcNewArray.getType());
				res.setType(arrayType);
			}
			jcNewArray.getDimensions().map(this::convertExpression).forEach(res.dimensions()::add);
			if (jcNewArray.getInitializers() != null) {
				ArrayInitializer initializer = this.ast.newArrayInitializer();
				commonSettings(initializer, javac);
				jcNewArray.getInitializers().stream().map(this::convertExpression).forEach(initializer.expressions()::add);
			}
			return res;
		}
		// TODO instanceof, lambdas
		throw new UnsupportedOperationException("Missing support to convert '" + javac + "' of type " + javac.getClass().getSimpleName());
	}

	private SuperMethodInvocation convertSuperMethodInvocation(JCMethodInvocation javac) {
		SuperMethodInvocation res = this.ast.newSuperMethodInvocation();
		commonSettings(res, javac);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
		javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		return res;
	}

	private ConstructorInvocation convertThisConstructorInvocation(JCMethodInvocation javac) {
		ConstructorInvocation res = this.ast.newConstructorInvocation();
		commonSettings(res, javac);
		javac.getArguments().stream().map(this::convertExpression).forEach(res.arguments()::add);
		javac.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
		return res;
	}

	private Expression convertLiteral(JCLiteral literal) {
		Object value = literal.getValue();
		if (value instanceof Number number) {
			NumberLiteral res = this.ast.newNumberLiteral();
			commonSettings(res, literal);
			res.setToken(literal.value.toString()); // TODO: we want the token here
			return res;
		}
		if (value instanceof String string) {
			StringLiteral res = this.ast.newStringLiteral();
			commonSettings(res, literal);
			res.setLiteralValue(string);  // TODO: we want the token here
			return res;
		}
		if (value instanceof Boolean string) {
			BooleanLiteral res = this.ast.newBooleanLiteral(string.booleanValue());
			commonSettings(res, literal);
			return res;
		}
		if (value == null) {
			NullLiteral res = this.ast.newNullLiteral();
			commonSettings(res, literal);
			return res;
		}
		if (value instanceof Character) {
			CharacterLiteral res = this.ast.newCharacterLiteral();
			commonSettings(res, literal);
			res.setCharValue(res.charValue());
			return res;
		}
		throw new UnsupportedOperationException("Not supported yet " + literal + "\n of type" + literal.getClass().getName());
	}

	private Statement convertStatement(JCStatement javac) {
		if (javac instanceof JCReturn returnStatement) {
			ReturnStatement res = this.ast.newReturnStatement();
			commonSettings(res, javac);
			if (returnStatement.getExpression() != null) {
				res.setExpression(convertExpression(returnStatement.getExpression()));
			}
			return res;
		}
		if (javac instanceof JCBlock block) {
			return convertBlock(block);
		}
		if (javac instanceof JCExpressionStatement jcExpressionStatement) {
			JCExpression jcExpression = jcExpressionStatement.getExpression();
			if (jcExpression instanceof JCMethodInvocation jcMethodInvocation
				&& jcMethodInvocation.getMethodSelect() instanceof JCIdent methodName
				&& Objects.equals(methodName.getName(), Names.instance(this.context)._this)) {
				return convertThisConstructorInvocation(jcMethodInvocation);
			}
			if (jcExpressionStatement.getExpression() == null) {
				return null;
			}
			if (jcExpressionStatement.getExpression() instanceof JCErroneous jcError) {
				if (jcError.getErrorTrees().size() == 1) {
					JCTree tree = jcError.getErrorTrees().get(0);
					if (tree instanceof JCStatement nestedStmt) {
						return convertStatement(nestedStmt);
					}
				} else {
					Block substitute = this.ast.newBlock();
					commonSettings(substitute, jcError);
					return substitute;
				}
			}
			ExpressionStatement res = this.ast.newExpressionStatement(convertExpression(jcExpressionStatement.getExpression()));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCVariableDecl jcVariableDecl) {
			VariableDeclarationFragment fragment = this.ast.newVariableDeclarationFragment();
			commonSettings(fragment, javac);
			fragment.setName((SimpleName)convert(jcVariableDecl.getName()));
			if (jcVariableDecl.getInitializer() != null) {
				fragment.setInitializer(convertExpression(jcVariableDecl.getInitializer()));
			}
			VariableDeclarationStatement res = this.ast.newVariableDeclarationStatement(fragment);
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCIf ifStatement) {
			return convertIfStatement(ifStatement);
		}
		if (javac instanceof JCThrow throwStatement) {
			ThrowStatement res = this.ast.newThrowStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(throwStatement.getExpression()));
			return res;
		}
		if (javac instanceof JCTry tryStatement) {
			return convertTryStatement(tryStatement);
		}
		if (javac instanceof JCSynchronized jcSynchronized) {
			SynchronizedStatement res = this.ast.newSynchronizedStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcSynchronized.getExpression()));
			res.setBody(convertBlock(jcSynchronized.getBlock()));
			return res;
		}
		if (javac instanceof JCForLoop jcForLoop) {
			ForStatement res = this.ast.newForStatement();
			commonSettings(res, javac);
			res.setBody(convertStatement(jcForLoop.getStatement()));
			jcForLoop.getInitializer().stream().map(this::convertStatementToExpression).forEach(res.initializers()::add);
			res.setExpression(convertExpression(jcForLoop.getCondition()));
			jcForLoop.getUpdate().stream().map(this::convertStatementToExpression).forEach(res.updaters()::add);
			return res;
		}
		if (javac instanceof JCEnhancedForLoop jcEnhancedForLoop) {
			EnhancedForStatement res = this.ast.newEnhancedForStatement();
			commonSettings(res, javac);
			res.setParameter((SingleVariableDeclaration)convertVariableDeclaration(jcEnhancedForLoop.getVariable()));
			res.setExpression(convertExpression(jcEnhancedForLoop.getExpression()));
			res.setBody(convertStatement(jcEnhancedForLoop.getStatement()));
			return res;
		}
		if (javac instanceof JCBreak jcBreak) {
			BreakStatement res = this.ast.newBreakStatement();
			commonSettings(res, javac);
			if (jcBreak.getLabel() != null) {
				res.setLabel((SimpleName)convert(jcBreak.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCSwitch jcSwitch) {
			SwitchStatement res = this.ast.newSwitchStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcSwitch.getExpression()));
			jcSwitch.getCases().stream()
				.flatMap(switchCase -> {
					List<JCStatement> stmts = new ArrayList<>(switchCase.getStatements().size() + 1);
					stmts.add(switchCase);
					stmts.addAll(switchCase.getStatements());
					return stmts.stream();
				}).map(this::convertStatement)
				.forEach(res.statements()::add);
			return res;
		}
		if (javac instanceof JCCase jcCase) {
			SwitchCase res = this.ast.newSwitchCase();
			commonSettings(res, javac);
			res.setSwitchLabeledRule(jcCase.getCaseKind() == CaseKind.RULE);
			jcCase.getExpressions().stream().map(this::convertExpression).forEach(res.expressions()::add);
			// jcCase.getStatements is processed as part of JCSwitch conversion
			return res;
		}
		if (javac instanceof JCWhileLoop jcWhile) {
			WhileStatement res = this.ast.newWhileStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcWhile.getCondition()));
			res.setBody(convertStatement(jcWhile.getStatement()));
			return res;
		}
		if (javac instanceof JCDoWhileLoop jcDoWhile) {
			DoStatement res = this.ast.newDoStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcDoWhile.getCondition()));
			res.setBody(convertStatement(jcDoWhile.getStatement()));
			return res;
		}
		if (javac instanceof JCYield jcYield) {
			YieldStatement res = this.ast.newYieldStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcYield.getValue()));
			return res;
		}
		if (javac instanceof JCContinue jcContinue) {
			ContinueStatement res = this.ast.newContinueStatement();
			commonSettings(res, javac);
			if (jcContinue.getLabel() != null) {
				res.setLabel((SimpleName)convert(jcContinue.getLabel()));
			}
			return res;
		}
		if (javac instanceof JCLabeledStatement jcLabel) {
			LabeledStatement res = this.ast.newLabeledStatement();
			commonSettings(res, javac);
			res.setLabel((SimpleName)convert(jcLabel.getLabel()));
			return res;
		}
		if (javac instanceof JCAssert jcAssert) {
			AssertStatement res =this.ast.newAssertStatement();
			commonSettings(res, javac);
			res.setExpression(convertExpression(jcAssert.getCondition()));
			return res;
		}
		throw new UnsupportedOperationException("Missing support to convert " + javac + "of type " + javac.getClass().getName());
	}

	private Expression convertStatementToExpression(JCStatement javac) {
		if (javac instanceof JCExpressionStatement jcExpressionStatement) {
			return convertExpression(jcExpressionStatement.getExpression());
		}
		Statement javacStatement = convertStatement(javac);
		if (javacStatement instanceof VariableDeclarationStatement decl && decl.fragments().size() == 1) {
			javacStatement.delete();
			VariableDeclarationFragment fragment = (VariableDeclarationFragment)decl.fragments().get(0);
			fragment.delete();
			VariableDeclarationExpression jdtVariableDeclarationExpression = this.ast.newVariableDeclarationExpression(fragment);
			commonSettings(jdtVariableDeclarationExpression, javac);
			return jdtVariableDeclarationExpression;
		}
		throw new UnsupportedOperationException(javac + " of type" + javac.getClass());
	} 

	private Block convertBlock(JCBlock javac) {
		Block res = this.ast.newBlock();
		commonSettings(res, javac);
		if (javac.getStatements() != null) {
			javac.getStatements().stream()
				.map(this::convertStatement)
				.forEach(res.statements()::add);
		}
		return res;
	}

	private TryStatement convertTryStatement(JCTry javac) {
		TryStatement res = this.ast.newTryStatement();
		commonSettings(res, javac);
		res.setBody(convertBlock(javac.getBlock()));
		if (javac.finalizer != null) {
			res.setFinally(convertBlock(javac.getFinallyBlock()));
		}
		javac.getResources().stream().map(this::convertTryResource).forEach(res.resources()::add);
		javac.getCatches().stream().map(this::convertCatcher).forEach(res.catchClauses()::add);
		return res;
	}

	private ASTNode /*VariableDeclarationExpression or Name*/ convertTryResource(JCTree javac) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	private CatchClause convertCatcher(JCCatch javac) {
		CatchClause res = this.ast.newCatchClause();
		commonSettings(res, javac);
		res.setBody(convertBlock(javac.getBlock()));
		res.setException((SingleVariableDeclaration)convertVariableDeclaration(javac.getParameter()));
		return res;
	}

	private IfStatement convertIfStatement(JCIf javac) {
		IfStatement res = this.ast.newIfStatement();
		commonSettings(res, javac);
		if (javac.getCondition() != null) {
			res.setExpression(convertExpression(javac.getCondition()));
		}
		if (javac.getThenStatement() != null) {
			res.setThenStatement(convertStatement(javac.getThenStatement()));
		}
		if (javac.getElseStatement() != null) {
			res.setElseStatement(convertStatement(javac.getElseStatement()));
		}
		return res;
	}

	private Type convertToType(JCTree javac) {
		if (javac instanceof JCIdent ident) {
			SimpleType res = this.ast.newSimpleType(convert(ident.name));
			commonSettings(res, ident);
			return res;
		}
		if (javac instanceof JCFieldAccess qualified) {
			QualifiedType res = this.ast.newQualifiedType(convertToType(qualified.getExpression()), (SimpleName)convert(qualified.name));
			commonSettings(res, qualified);
			return res;
		}
		if (javac instanceof JCPrimitiveTypeTree primitiveTypeTree) {
			PrimitiveType res = this.ast.newPrimitiveType(convert(primitiveTypeTree.getPrimitiveTypeKind()));
			commonSettings(res, primitiveTypeTree);
			return res;
		}
		if (javac instanceof JCTypeUnion union) {
			UnionType res = this.ast.newUnionType();
			commonSettings(res, javac);
			union.getTypeAlternatives().stream().map(this::convertToType).forEach(res.types()::add);
			return res;
		}
		if (javac instanceof JCArrayTypeTree jcArrayType) {
			ArrayType res = this.ast.newArrayType(convertToType(jcArrayType.getType()));
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCTypeApply jcTypeApply) {
			ParameterizedType res = this.ast.newParameterizedType(convertToType(jcTypeApply.getType()));
			commonSettings(res, javac);
			jcTypeApply.getTypeArguments().stream().map(this::convertToType).forEach(res.typeArguments()::add);
			return res;
		}
		if (javac instanceof JCWildcard) {
			WildcardType res = this.ast.newWildcardType();
			commonSettings(res, javac);
			return res;
		}
		if (javac instanceof JCTypeIntersection jcTypeIntersection) {
			IntersectionType res = this.ast.newIntersectionType();
			commonSettings(res, javac);
			jcTypeIntersection.getBounds().stream().map(this::convertToType).forEach(res.types()::add);
			return res;
		}
		throw new UnsupportedOperationException("Not supported yet, type " + javac + " of class" + javac.getClass());
	}

	private List<IExtendedModifier> convert(JCModifiers modifiers) {
		List<IExtendedModifier> res = new ArrayList<>();
		modifiers.getFlags().stream().map(this::convert).forEach(res::add);
		modifiers.getAnnotations().stream().map(this::convert).forEach(res::add);
		return res;
	}

	private Code convert(TypeKind javac) {
		return switch(javac) {
			case BOOLEAN -> PrimitiveType.INT;
			case BYTE -> PrimitiveType.BYTE;
			case SHORT -> PrimitiveType.SHORT;
			case INT -> PrimitiveType.INT;
			case LONG -> PrimitiveType.LONG;
			case CHAR -> PrimitiveType.CHAR;
			case FLOAT -> PrimitiveType.FLOAT;
			case DOUBLE -> PrimitiveType.DOUBLE;
			case VOID -> PrimitiveType.VOID;
			default -> throw new IllegalArgumentException(javac.toString());
		};
	}

	private Annotation convert(JCAnnotation javac) {
		Annotation res = this.ast.newNormalAnnotation();
		commonSettings(res, javac);
		res.setTypeName(toName(javac.getAnnotationType()));
		// TODO member values/arguments
		return res;
	}

	private Modifier convert(javax.lang.model.element.Modifier javac) {
		Modifier res = this.ast.newModifier(switch (javac) {
			case PUBLIC -> ModifierKeyword.PUBLIC_KEYWORD;
			case PROTECTED -> ModifierKeyword.PROTECTED_KEYWORD;
			case PRIVATE -> ModifierKeyword.PRIVATE_KEYWORD;
			case ABSTRACT -> ModifierKeyword.ABSTRACT_KEYWORD;
			case DEFAULT -> ModifierKeyword.DEFAULT_KEYWORD;
			case STATIC -> ModifierKeyword.STATIC_KEYWORD;
			case SEALED -> ModifierKeyword.SEALED_KEYWORD;
			case NON_SEALED -> ModifierKeyword.NON_SEALED_KEYWORD;
			case FINAL -> ModifierKeyword.FINAL_KEYWORD;
			case TRANSIENT -> ModifierKeyword.TRANSIENT_KEYWORD;
			case VOLATILE -> ModifierKeyword.VOLATILE_KEYWORD;
			case SYNCHRONIZED -> ModifierKeyword.SYNCHRONIZED_KEYWORD;
			case NATIVE -> ModifierKeyword.NATIVE_KEYWORD;
			case STRICTFP -> ModifierKeyword.STRICTFP_KEYWORD;
		});
		// TODO set positions
		return res;
	}

	private Name convert(com.sun.tools.javac.util.Name javac) {
		if (javac == null || Objects.equals(javac, Names.instance(this.context).error) || Objects.equals(javac, Names.instance(this.context).empty)) {
			return null;
		}
		int lastDot = javac.lastIndexOf((byte)'.');
		if (lastDot < 0) {
			return this.ast.newSimpleName(javac.toString());
		} else {
			return this.ast.newQualifiedName(convert(javac.subName(0, lastDot)), (SimpleName)convert(javac.subName(lastDot + 1, javac.length() - 1)));
		}
		// position is set later, in FixPositions, as computing them depends on the sibling
	}

	public org.eclipse.jdt.core.dom.Comment convert(Comment javac, int pos, int endPos) {
		org.eclipse.jdt.core.dom.Comment jdt = switch (javac.getStyle()) {
			case LINE -> this.ast.newLineComment();
			case BLOCK -> this.ast.newBlockComment();
			case JAVADOC -> this.ast.newJavadoc();
		};
		jdt.setSourceRange(pos, endPos - pos);
		return jdt;
	}

	static IProblem convertDiagnostic(Diagnostic<? extends JavaFileObject> javacDiagnostic) {
		// TODO use a problem factory? Map code to category...?
		return new DefaultProblem(
			javacDiagnostic.getSource().getName().toCharArray(),
			javacDiagnostic.getMessage(Locale.getDefault()),
			toProblemId(javacDiagnostic.getCode()), // TODO probably use `getCode()` here
			null,
			switch (javacDiagnostic.getKind()) {
				case ERROR -> ProblemSeverities.Error;
				case WARNING, MANDATORY_WARNING -> ProblemSeverities.Warning;
				case NOTE -> ProblemSeverities.Info;
				default -> ProblemSeverities.Error;
			},
			(int)Math.min(javacDiagnostic.getPosition(), javacDiagnostic.getStartPosition()),
			(int)javacDiagnostic.getEndPosition(),
			(int)javacDiagnostic.getLineNumber(),
			(int)javacDiagnostic.getColumnNumber());
	}

	private static int toProblemId(String javacDiagnosticCode) {
		// better use a Map<String, IProblem> if there is a 1->0..1 mapping
		return switch (javacDiagnosticCode) {
			case "compiler.warn.raw.class.use" -> IProblem.RawTypeReference;
			// TODO complete mapping list; dig in https://github.com/openjdk/jdk/blob/master/src/jdk.compiler/share/classes/com/sun/tools/javac/resources/compiler.properties
			// for an exhaustive (but polluted) list, unless a better source can be found (spec?)
			default -> 0;
		};
	}

	class FixPositions extends ASTVisitor {
		private final String contents;

		FixPositions() {
			String s = null;
			try {
				s = JavacConverter.this.javacCompilationUnit.getSourceFile().getCharContent(true).toString();
			} catch (IOException ex) {
				ILog.get().error(ex.getMessage(), ex);
			}
			this.contents = s;
		}

		@Override
		public boolean visit(QualifiedName node) {
			if (node.getStartPosition() < 0) {
				int foundOffset = findPositionOfText(node.getFullyQualifiedName(), node.getParent(), siblings(node));
				if (foundOffset >= 0) {
					node.setSourceRange(foundOffset, node.getFullyQualifiedName().length());
				}
			}
			return true;
		}

		@Override
		public void endVisit(QualifiedName node) {
			if (node.getName().getStartPosition() >= 0) {
				node.setSourceRange(node.getQualifier().getStartPosition(), node.getName().getStartPosition() + node.getName().getLength() - node.getQualifier().getStartPosition());
			}
		}

		@Override
		public boolean visit(SimpleName name) {
			if (name.getStartPosition() < 0) {
				int foundOffset = findPositionOfText(name.getIdentifier(), name.getParent(), siblings(name));
				if (foundOffset >= 0) {
					name.setSourceRange(foundOffset, name.getIdentifier().length());
				}
			}
			return false;
		}

		@Override
		public boolean visit(Modifier modifier) {
			int parentStart = modifier.getParent().getStartPosition();
			int relativeStart = this.contents.substring(parentStart, parentStart + modifier.getParent().getLength()).indexOf(modifier.getKeyword().toString());
			if (relativeStart >= 0) {
				modifier.setSourceRange(parentStart + relativeStart, modifier.getKeyword().toString().length());
			} else {
				ILog.get().warn("Couldn't compute position of " + modifier);
			}
			return true;
		}

		private static List<ASTNode> siblings(ASTNode node) {
			return ((Collection<Object>)node.getParent().properties().values()).stream()
				.filter(ASTNode.class::isInstance)
				.map(ASTNode.class::cast)
				.filter(Predicate.not(node::equals))
				.toList();
		}

		private int findPositionOfText(String text, ASTNode in, List<ASTNode> excluding) {
			int current = in.getStartPosition();
			PriorityQueue<ASTNode> excluded = new PriorityQueue<>(Comparator.comparing(ASTNode::getStartPosition));
			if (excluded.isEmpty()) {
				String subText = this.contents.substring(current, current + in.getLength());
				int foundInSubText = subText.indexOf(text);
				if (foundInSubText >= 0) {
					return current + foundInSubText;
				}
			} else {
				ASTNode currentExclusion = null;
				while ((currentExclusion = excluded.poll()) != null) {
					if (currentExclusion.getStartPosition() >= current) {
						int rangeEnd = currentExclusion.getStartPosition();
						String subText = this.contents.substring(current, rangeEnd);
						int foundInSubText = subText.indexOf(text);
						if (foundInSubText >= 0) {
							return current + foundInSubText;
						}
						current = rangeEnd + currentExclusion.getLength();
					}
				}
			}
			return -1;
		}
	}

}

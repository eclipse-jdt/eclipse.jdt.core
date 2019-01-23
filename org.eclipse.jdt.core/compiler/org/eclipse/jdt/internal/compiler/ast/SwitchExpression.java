/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.VANILLA_CONTEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public class SwitchExpression extends SwitchStatement implements IPolyExpression {

	/* package */ TypeBinding expectedType;
	private ExpressionContext expressionContext = VANILLA_CONTEXT;
	private boolean isPolyExpression = false;
	private TypeBinding[] originalValueResultExpressionTypes;
	private TypeBinding[] finalValueResultExpressionTypes;

	public List<Expression> resultExpressions;
	private static Map<TypeBinding, TypeBinding[]> type_map;

	static {
		type_map = new HashMap<TypeBinding, TypeBinding[]>();
		type_map.put(TypeBinding.CHAR, new TypeBinding[] {TypeBinding.CHAR, TypeBinding.BYTE, TypeBinding.INT});
		type_map.put(TypeBinding.SHORT, new TypeBinding[] {TypeBinding.SHORT, TypeBinding.BYTE, TypeBinding.INT});
		type_map.put(TypeBinding.BYTE, new TypeBinding[] {TypeBinding.BYTE, TypeBinding.INT});
	}

	@Override
	public void setExpressionContext(ExpressionContext context) {
		this.expressionContext = context;
	}

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	@Override
	public ExpressionContext getExpressionContext() {
		return this.expressionContext;
	}
	@Override
	protected boolean ignoreMissingDefaultCase(CompilerOptions compilerOptions, boolean isEnumSwitch) {
		return isEnumSwitch; // mandatory error if not enum in switch expressions
	}
	@Override
	protected int getFallThroughState(Statement stmt, BlockScope blockScope) {
		if (stmt instanceof Expression || stmt instanceof ThrowStatement)
			return BREAKING;
		if (this.switchLabeledRules // do this check for every block if '->' (Switch Labeled Rules) 
				&& stmt instanceof Block) {
			Block block = (Block) stmt;
			if (block.doesNotCompleteNormally()) {
				return BREAKING;
			}
			//JLS 12 15.29.1 Given a switch expression, if the switch block consists of switch labeled rules,
			//then it is a compile-time error if any switch labeled block can complete normally.
			blockScope.problemReporter().switchExpressionSwitchLabeledBlockCompletesNormally(block);
		}
		return FALLTHROUGH;
	}
	@Override
	protected void completeNormallyCheck(BlockScope blockScope) {
		if (this.switchLabeledRules) return; // already taken care in getFallThroughState()
		int sz = this.statements != null ? this.statements.length : 0;
		if (sz == 0) return;
		/* JLS 12 15.29.1
		 * If, on the other hand, the switch block consists of switch labeled statement groups, then it is a
		 * compile-time error if either the last statement in the switch block can complete normally, or the
		 * switch block includes one or more switch labels at the end.
		 */
		Statement lastNonCaseStmt = null;
		Statement firstTrailingCaseStmt = null;
		for (int i = sz - 1; i >= 0; i--) {
			Statement stmt = this.statements[sz - 1];
			if (stmt instanceof CaseStatement)
				firstTrailingCaseStmt = stmt;
			else {
				lastNonCaseStmt = stmt;
				break;
			}
		}
		if (lastNonCaseStmt != null && !lastNonCaseStmt.doesNotCompleteNormally()) {
			blockScope.problemReporter().switchExpressionLastStatementCompletesNormally(lastNonCaseStmt);				
		}
		if (firstTrailingCaseStmt != null) {
			blockScope.problemReporter().switchExpressionTrailingSwitchLabels(firstTrailingCaseStmt);				
		}
	}
	@Override
	public Expression[] getPolyExpressions() {
		List<Expression> polys = new ArrayList<>();
		for (Expression e : this.resultExpressions) {
			Expression[] ea = e.getPolyExpressions();
			if (ea == null || ea.length ==0) continue;
			polys.addAll(Arrays.asList(ea));
		}
		return polys.toArray(new Expression[0]);
	}
	@Override
	public boolean isPertinentToApplicability(TypeBinding targetType, MethodBinding method) {
		for (Expression e : this.resultExpressions) {
			if (!e.isPertinentToApplicability(targetType, method))
				return false;
		}
		return true;
	}
	@Override
	public boolean isPotentiallyCompatibleWith(TypeBinding targetType, Scope scope1) {
		for (Expression e : this.resultExpressions) {
			if (!e.isPotentiallyCompatibleWith(targetType, scope1))
				return false;
		}
		return true;
	}
	@Override
	public boolean isFunctionalType() {
		for (Expression e : this.resultExpressions) {
			if (e.isFunctionalType()) // return true even for one functional type
				return true;
		}
		return false;
	}
	@Override
	protected void statementGenerateCode(BlockScope currentScope, CodeStream codeStream, Statement statement) {
		if (!(statement instanceof Expression)) {
			super.statementGenerateCode(currentScope, codeStream, statement);
			return;
		}
		Expression expression1 = (Expression) statement;
		expression1.generateCode(currentScope, codeStream, true /* valueRequired */);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
		super.generateCode(currentScope, codeStream);
		// TODO
	}
	protected boolean computeConversions(BlockScope blockScope, TypeBinding targetType) {
		boolean ok = true;
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			ok &= computeConversionsResultExpressions(blockScope, targetType, ok, this.originalValueResultExpressionTypes[i], 
					this.resultExpressions.get(i));
		}
		return ok;
	}
	private boolean computeConversionsResultExpressions(BlockScope blockScope, TypeBinding targetType, boolean ok,
			TypeBinding resultExpressionType, Expression resultExpression) {
		if (resultExpressionType != null && resultExpressionType.isValidBinding()) {
			if (resultExpression.isConstantValueOfTypeAssignableToType(resultExpressionType, targetType)
					|| resultExpressionType.isCompatibleWith(targetType)) {

				resultExpression.computeConversion(blockScope, targetType, resultExpressionType);
				if (resultExpressionType.needsUncheckedConversion(targetType)) {
					blockScope.problemReporter().unsafeTypeConversion(resultExpression, resultExpressionType, targetType);
				}
				if (resultExpression instanceof CastExpression
						&& (resultExpression.bits & (ASTNode.UnnecessaryCast|ASTNode.DisableUnnecessaryCastCheck)) == 0) {
					CastExpression.checkNeedForAssignedCast(blockScope, targetType, (CastExpression) resultExpression);
				}
			} else if (isBoxingCompatible(resultExpressionType, targetType, resultExpression, blockScope)) {
				resultExpression.computeConversion(blockScope, targetType, resultExpressionType);
				if (resultExpression instanceof CastExpression
						&& (resultExpression.bits & (ASTNode.UnnecessaryCast|ASTNode.DisableUnnecessaryCastCheck)) == 0) {
					CastExpression.checkNeedForAssignedCast(blockScope, targetType, (CastExpression) resultExpression);
				}
			} else {
				blockScope.problemReporter().typeMismatchError(resultExpressionType, targetType, resultExpression, null);
				ok = false;
			}
		}
		return ok;
	}
	/* package */ void collectResultExpressions() {
		if (this.resultExpressions != null)
			return; // already calculated.

		class ResultExpressionsCollector extends ASTVisitor {
			SwitchExpression targetSwitchExpression;
			public ResultExpressionsCollector(SwitchExpression se) {
				this.targetSwitchExpression = se;
			}
			@Override
			public boolean visit(SwitchExpression switchExpression, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(BreakStatement breakStatement, BlockScope blockScope) {
				if (breakStatement.expression != null) {
					this.targetSwitchExpression.resultExpressions.add(breakStatement.expression);
					breakStatement.switchExpression = this.targetSwitchExpression;
					breakStatement.label = null; // not a label, but an expression
				}
				return false;
			}
			@Override
			public boolean visit(DoStatement stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(ForStatement stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(ForeachStatement stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(SwitchStatement stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(TypeDeclaration stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(WhileStatement stmt, BlockScope blockScope) {
				return false;
			}
			@Override
			public boolean visit(CaseStatement caseStatement, BlockScope blockScope) {
				return true; // do nothing by default, keep traversing
			}
		}
		this.resultExpressions = new ArrayList<>(0); // indicates processed
		int l = this.statements == null ? 0 : this.statements.length;
		for (int i = 0; i < l; ++i) {
			Statement stmt = this.statements[i];
			if (stmt instanceof CaseStatement) {
				CaseStatement caseStatement = (CaseStatement) stmt;
				if (!caseStatement.isExpr) continue;
				stmt = this.statements[++i];
				if (stmt instanceof Expression) {
					this.resultExpressions.add((Expression) stmt);
					continue;
				} else if (stmt instanceof ThrowStatement) {
					// TODO: Throw Expression Processing. Anything to be done here for resolve?
					continue;
				}
			}
			// break statement and block statement of SwitchLabelRule or block statement of ':'
			ResultExpressionsCollector reCollector = new ResultExpressionsCollector(this);
			stmt.traverse(reCollector, this.scope);

			/*TODO: Do the following error check in analyseCode() rather than here - JLS 12 15.29
			 * Given a switch expression, if the switch block consists of switch labeled rules, then
			 * it is a compile-time error if any switch labeled block can complete normally. If, on  the
			 * other hand, the switch block consists of switch labeled statement groups, then it is a
			 * compile-time error if either the last statement in the switch block can complete
			 * normally, or the switch block includes one or more switch labels at the end.
			 */
		}
	}
	@Override
	public TypeBinding resolveType(BlockScope upperScope) {
		try {
			this.constant = Constant.NotAConstant;

			// tag break statements and (alongwih in the same pass) collect the result expressions
			collectResultExpressions();

			resolve(upperScope);

			if (this.statements == null || this.statements.length == 0) {
				//	Report Error JLS 12 15.29.1  The switch block must not be empty.
				upperScope.problemReporter().switchExpressionEmptySwitchBlock(this);
				return null;
			}
			
			int resultExpressionsCount = this.resultExpressions != null ? this.resultExpressions.size() : 0;
			if (resultExpressionsCount == 0) {
				//  Report Error JLS 12 15.29.1 
				// It is a compile-time error if a switch expression has no result expressions.
				upperScope.problemReporter().switchExpressionNoResultExpressions(this);
				return null;
			}
			//A switch expression is a poly expression if it appears in an assignment context or an invocation context (5.2, 5.3). 
			//Otherwise, it is a standalone expression.
			if (this.expressionContext == ASSIGNMENT_CONTEXT || this.expressionContext == INVOCATION_CONTEXT) {
				for (Expression e : this.resultExpressions) {
					//Where a poly switch expression appears in a context of a particular kind with target type T,
					//its result expressions similarly appear in a context of the same kind with target type T.
					e.setExpressionContext(this.expressionContext);
					e.setExpectedType(this.expectedType);
				}
			}

			if (this.originalValueResultExpressionTypes == null) {
				this.originalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
				this.finalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
				for (int i = 0; i < resultExpressionsCount; ++i) {
					this.finalValueResultExpressionTypes[i] = this.originalValueResultExpressionTypes[i] =
							this.resultExpressions.get(i).resolvedType;
				}
			}
			if (isPolyExpression()) { //The type of a poly switch expression is the same as its target type.
				if (this.expectedType == null || !this.expectedType.isProperType(true)) {
					return new PolyTypeBinding(this);
				}
				return this.resolvedType = computeConversions(this.scope, this.expectedType) ? this.expectedType : null;
			}

			if (resultExpressionsCount == 1)
				return this.originalValueResultExpressionTypes[0];

			boolean typeUniformAcrossAllArms = true;
			TypeBinding tmp = this.originalValueResultExpressionTypes[0];
			for (int i = 1, l = this.originalValueResultExpressionTypes.length; i < l; ++i) {
				if (TypeBinding.notEquals(tmp, this.originalValueResultExpressionTypes[i])) {
					typeUniformAcrossAllArms = false;
					break;
				}
			}
			// If the result expressions all have the same type (which may be the null type), 
			// then that is the type of the switch expression.
			if (typeUniformAcrossAllArms) {
				tmp = this.originalValueResultExpressionTypes[0];
				for (int i = 0; i < resultExpressionsCount; ++i) {
					tmp = NullAnnotationMatching.moreDangerousType(tmp, this.originalValueResultExpressionTypes[i]);
				}
				return this.resolvedType = tmp;
			}
			
			boolean typeBbolean = true;
			for (TypeBinding t : this.originalValueResultExpressionTypes) {
				typeBbolean &= t.id == T_boolean || t.id == T_JavaLangBoolean;
			}
			LookupEnvironment env = this.scope.environment();
			/*
			 * Otherwise, if the type of each result expression is boolean or Boolean,
			 * an unboxing conversion (5.1.8) is applied to each result expression of type Boolean,
			 * and the switch expression has type boolean.
			 */
			if (typeBbolean) {
				for (int i = 0; i < resultExpressionsCount; ++i) {
					if (this.originalValueResultExpressionTypes[i].id == T_boolean) continue;
					this.finalValueResultExpressionTypes[i] = env.computeBoxingType(this.originalValueResultExpressionTypes[i]);
					this.resultExpressions.get(i).computeConversion(this.scope, this.finalValueResultExpressionTypes[i], this.originalValueResultExpressionTypes[i]);
				}
				return this.resolvedType = TypeBinding.BOOLEAN;
			}

			/*
			 * Otherwise, if the type of each result expression is convertible to a numeric type (5.1.8), the type
			 * of the switch expression is given by numeric promotion (5.6.3) applied to the result expressions.
			 */
			boolean typeNumeric = true;
			TypeBinding resultNumeric = null;
			HashSet<TypeBinding> typeSet = new HashSet<>();
			/*  JLS 12 5.6.3 Switch Numeric Promotion
			 * When a switch expression applies numeric promotion to a set of result expressions, each of which
			 * must denote a value that is convertible to a numeric type, the following rules apply, in order:
			 *  If any result expression is of a reference type, it is subjected to unboxing conversion (5.1.8).
			 */
			for (int i = 0; i < resultExpressionsCount; ++i) {
				tmp = this.originalValueResultExpressionTypes[i].isNumericType() ?
						this.originalValueResultExpressionTypes[i] :
							env.computeBoxingType(this.originalValueResultExpressionTypes[i]);
				if (!tmp.isNumericType()) {
					typeNumeric = false;
					break;
				}
				typeSet.add(TypeBinding.wellKnownType(this.scope, tmp.id));
			}
			if (typeNumeric) {
				 /* If any result expression is of type double, then other result expressions that are not of type double
				 *  are widened to double.
				 *  Otherwise, if any result expression is of type float, then other result expressions that are not of
				 *  type float are widened to float.
				 *  Otherwise, if any result expression is of type long, then other result expressions that are not of 
				 *  type long are widened to long.
				 */
				TypeBinding[] dfl = new TypeBinding[]{// do not change the order JLS 12 5.6.3
						TypeBinding.DOUBLE,
						TypeBinding.FLOAT,
						TypeBinding.LONG};
				for (TypeBinding binding : dfl) {
					if (typeSet.contains(binding)) {
						resultNumeric = binding;
						break;
					}
				}

				 /*  Otherwise, if any result expression is of type int and is not a constant expression, the other 
				 *  result expressions that are not of type int are widened to int.
				 */
				resultNumeric = resultNumeric != null ? resultNumeric : check_nonconstant_int();

				resultNumeric = resultNumeric != null ? resultNumeric : // one among the first few rules applied.
					getResultNumeric(typeSet, this.originalValueResultExpressionTypes); // check the rest
				typeSet = null; // hey gc!
				for (int i = 0; i < resultExpressionsCount; ++i) {
					// auto-unboxing and/or widening/narrrowing JLS 12 5.6.3
					this.resultExpressions.get(i).computeConversion(this.scope,
							resultNumeric, this.originalValueResultExpressionTypes[i]);
					this.finalValueResultExpressionTypes[i] = resultNumeric;
				}
				// After the conversion(s), if any, value set conversion (5.1.13) is then applied to each result expression.
				return this.resolvedType = resultNumeric;
			}

			/* Otherwise, boxing conversion (5.1.7) is applied to each result expression that has a primitive type,
			 * after which the type of the switch expression is the result of applying capture conversion (5.1.10)
			 * to the least upper bound (4.10.4) of the types of the result expressions.
			 */
			for (int i = 0; i < resultExpressionsCount; ++i) {
				if (this.finalValueResultExpressionTypes[i].isBaseType())
					this.finalValueResultExpressionTypes[i] = env.computeBoxingType(this.finalValueResultExpressionTypes[i]);
			}
			TypeBinding commonType = this.scope.lowerUpperBound(this.finalValueResultExpressionTypes);
			if (commonType != null) {
				for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
					this.resultExpressions.get(i).computeConversion(this.scope, commonType, this.originalValueResultExpressionTypes[i]);
					this.finalValueResultExpressionTypes[i] = commonType;
				}
				return this.resolvedType = commonType.capture(this.scope, this.sourceStart, this.sourceEnd);
			}
			this.scope.problemReporter().switchExpressionIncompatibleResultExpressions(this);
			return null;
		} finally {
			if (this.scope != null) this.scope.enclosingCase = null; // no longer inside switch case block
		}
	}
	private TypeBinding check_nonconstant_int() {
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			Expression e = this.resultExpressions.get(i);
			TypeBinding type = this.originalValueResultExpressionTypes[i];
			if (type.id == T_int && e.constant == Constant.NotAConstant)
				return TypeBinding.INT;
		}
		return null;
	}
	private boolean areAllIntegerResultExpressionsConvertibleToTargetType(TypeBinding targetType) {
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			Expression e = this.resultExpressions.get(i);
			TypeBinding t = this.originalValueResultExpressionTypes[i];
			if (!TypeBinding.equalsEquals(t, TypeBinding.INT)) continue;
			if (!e.isConstantValueOfTypeAssignableToType(t, targetType))
				return false;
		}
		return true;
	}
	private TypeBinding check_csb(Set<TypeBinding> typeSet, TypeBinding candidate) {
		if (!typeSet.contains(candidate))
			return null;

		TypeBinding[] allowedTypes = SwitchExpression.type_map.get(candidate);
		Set<TypeBinding> allowedSet = Arrays.stream(allowedTypes).collect(Collectors.toSet());

		if (!allowedSet.containsAll(typeSet))
			return null;

		return areAllIntegerResultExpressionsConvertibleToTargetType(candidate) ?
				candidate : null;
	}
	private TypeBinding getResultNumeric(Set<TypeBinding> typeSet, TypeBinding[] armTypes) {
		// note: if an expression has a type integer, then it will be a constant 
		// since non-constant integers are already processed before reaching here.

		/*
		 * Otherwise, if any result expression is of type char, and every other result expression is either of
		 * type char, or of type byte, or a constant expression of type int with a value that is representable
		 * in the type char, then the byte results are widened to char and the int results are narrowed to char.
		 */

		 /*  Otherwise, if any result expression is of type short, and every other result expression is either of
		 *  type short, or of type byte, or a constant expression of type int with a value that is representable
		 *  in the type short, then the byte results are widened to short and the int results are narrowed to
		 *  short.
		 */
		 /*  Otherwise, if any result expression is of type byte, and every other result expression is either of
		 *  type byte or a constant expression of type int with a value that is representable in the type byte,
		 *  then the int results are narrowed to byte.
		 */

		// DO NOT Change the order below [as per JLS 12 5.6.3 item 2, sub-items 5,6 and 7].
		TypeBinding[] csb = new TypeBinding[] {TypeBinding.CHAR, TypeBinding.SHORT, TypeBinding.BYTE};
		for (TypeBinding c : csb) {
			TypeBinding result = check_csb(typeSet, c);
			if (result != null)
				return result;
		}
		 /*  Otherwise, all the result expressions that are not of type int are widened to int. */
		return TypeBinding.INT;
	}
	@Override
	public boolean isPolyExpression() {
		if (this.isPolyExpression)
			return true;
		// JLS 12 15.29.1 A switch expression is a poly expression if it appears in an assignment context or
		// an invocation context (5.2, 5.3). Otherwise, it is a standalone expression.
		return this.isPolyExpression = this.expressionContext == ASSIGNMENT_CONTEXT || 
				this.expressionContext == INVOCATION_CONTEXT;
	}
	@Override
	public boolean isCompatibleWith(TypeBinding left, Scope skope) {
		if (!isPolyExpression())
			return super.isCompatibleWith(left, skope);

		for (Expression e : this.resultExpressions) {
			if (!e.isCompatibleWith(left, skope))
				return false;
		}
		return true;
	}
	@Override
	public boolean isBoxingCompatibleWith(TypeBinding targetType, Scope skope) {
		if (!isPolyExpression())
			return super.isBoxingCompatibleWith(targetType, skope);

		for (Expression e : this.resultExpressions) {
			if (!(e.isCompatibleWith(targetType, skope) || e.isBoxingCompatibleWith(targetType, skope)))
				return false;
		}
		return true;
	}
	@Override
	public boolean sIsMoreSpecific(TypeBinding s, TypeBinding t, Scope skope) {
		if (super.sIsMoreSpecific(s, t, skope))
			return true;
		if (!isPolyExpression())
			return false;
		for (Expression e : this.resultExpressions) {
			if (!e.sIsMoreSpecific(s, t, skope))
				return false;
		}
		return true;
	}
}
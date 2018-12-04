/*******************************************************************************
 * Copyright (c) 2018 GK Software SE and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.ASSIGNMENT_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.INVOCATION_CONTEXT;
import static org.eclipse.jdt.internal.compiler.ast.ExpressionContext.VANILLA_CONTEXT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
			if (e.isFunctionalType())
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
			ok = computeConversionsResultExpressions(blockScope, targetType, ok, this.originalValueResultExpressionTypes[i], 
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
	private void collectResultExpressions() {
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
		for (int i = 0, l = this.statements.length; i < l; ++i) {
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
			// JLS12 15.29.1
			CompilerOptions compilerOptions = upperScope.compilerOptions();
			this.scope = new BlockScope(upperScope);
			LookupEnvironment env = this.scope.environment();

			resolve(upperScope);

			if (this.statements == null || this.statements.length == 0) {
				//	Report Error JLS 12 15.29.1  The switch block must not be empty.
				upperScope.problemReporter().switchExpressionEmptySwitchBlock(this);
				return null;
			}
			// now we are done with case constants, let us collect the result expressions
			collectResultExpressions();
			
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
			 * Otherwise, if the type of each result expression is convertible to a numeric type (5.1.8),
			 * the type of the switch expression is given by numeric promotion (5.6.3) applied to the result expressions.
			 */
			boolean typeNumeric = true;
			TypeBinding resultNumeric = this.originalValueResultExpressionTypes[0];
			HashSet<TypeBinding> typeSet = new HashSet<>(); // for inconclusive in first attempt
			for (int i = 0; i < resultExpressionsCount; ++i) {
				tmp = this.originalValueResultExpressionTypes[i].isNumericType() ?
						this.originalValueResultExpressionTypes[i] :
							env.computeBoxingType(this.originalValueResultExpressionTypes[i]);
				if (!tmp.isNumericType()) {
					typeNumeric = false;
					break;
				}
				resultNumeric = getFirstResults(tmp, this.resultExpressions.get(i), resultNumeric);
				typeSet.add(TypeBinding.wellKnownType(this.scope, tmp.id));
			}
			if (typeNumeric) {
				resultNumeric = resultNumeric != null ? resultNumeric :
					getResultNumeric(typeSet, this.originalValueResultExpressionTypes);
				typeSet = null; // hey gc!
				for (int i = 0; i < resultExpressionsCount; ++i) {
					// auto-unboxing and/or widening/narrrowing JLS 12 5.6.3
					this.resultExpressions.get(i).computeConversion(this.scope,
							resultNumeric, this.originalValueResultExpressionTypes[i]);
					this.finalValueResultExpressionTypes[i] = resultNumeric;
				}
				// After the conversion(s), if any, value set conversion (5.1.13) is then applied to each result expression.
				// TODO: check whether 5.6.3 automatically done - check CE.resolveType() as well.
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
	/*
	 * pre-req: type.isNumeric() true
	 */
	private TypeBinding getFirstResults(TypeBinding type, Expression expr, TypeBinding resultNumeric) {
		if (resultNumeric == null || TypeBinding.equalsEquals(type, resultNumeric))
			return resultNumeric;
		int[] typeId = new int[]{T_double, T_float, T_long};
		for (int id : typeId) {
			if (resultNumeric.id == id || type.id == id)
				return TypeBinding.wellKnownType(this.scope, id);
		}
		if (type.id == T_int && expr.constant != Constant.NotAConstant)
			return TypeBinding.INT;
		return null;
	}
	private boolean isIntegerConvertible(TypeBinding targetType) {
		if (TypeBinding.equalsEquals(targetType, TypeBinding.INT))
			return true;
		for (int i = 0, l = this.resultExpressions.size(); i < l; ++i) {
			Expression e = this.resultExpressions.get(i);
			TypeBinding t = this.originalValueResultExpressionTypes[i];
			if (!TypeBinding.equalsEquals(t, TypeBinding.INT)) continue;
			if (!e.isConstantValueOfTypeAssignableToType(t, targetType))
				return false;
		}
		return true;
	}
	private TypeBinding getResultNumeric(HashSet<TypeBinding> typeSet, TypeBinding[] armTypes) {
		if (typeSet.contains(TypeBinding.CHAR) && typeSet.contains(TypeBinding.SHORT)) {
			return TypeBinding.INT;
		}
		TypeBinding[] types = new TypeBinding[] { TypeBinding.CHAR, TypeBinding.SHORT, TypeBinding.BYTE };
		for (TypeBinding t : types) {
			if (typeSet.contains(t) && isIntegerConvertible(t))
				return t;
		}
		return TypeBinding.INT;
	}
	@Override
	public boolean isPolyExpression() throws UnsupportedOperationException {

		if (this.isPolyExpression)
			return true;

		return this.isPolyExpression = this.expressionContext == ASSIGNMENT_CONTEXT || 
				this.expressionContext == INVOCATION_CONTEXT;
	}
}
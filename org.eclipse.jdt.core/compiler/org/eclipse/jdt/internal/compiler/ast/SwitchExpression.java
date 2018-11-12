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
import java.util.Stack;

import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.Binding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.PolyTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class SwitchExpression extends Expression implements IPolyExpression {

	public Expression expression;
	public SwitchExprArm[] exprArms;
	public BlockScope scope;

	public CaseStatement[] cases;
	public CaseStatement defaultCase;
	public int caseCount;
	int[] constants;
	String[] stringConstants;

	/* package */ TypeBinding expectedType;
	private ExpressionContext expressionContext = VANILLA_CONTEXT;
	private boolean isPolyExpression = false;
	private TypeBinding[] originalValueResultExpressionTypes;
	private TypeBinding[] finalValueResultExpressionTypes;

	private List<Expression> resultExpressions;

	CaseStatement[] duplicateCaseStatements = null;
	int duplicateCaseStatementsCounter = 0;
	private LocalVariableBinding dispatchStringCopy = null;
	// for switch on strings
	private static final char[] SecretStringVariableName = " switchDispatchString".toCharArray(); //$NON-NLS-1$

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
	public void traverse(ASTVisitor visitor, BlockScope blockScope) {

			if (visitor.visit(this, blockScope)) {
				this.expression.traverse(visitor, blockScope);
				if (this.exprArms != null && this.exprArms.length > 0) {
					SwitchExprArm arms[] = this.exprArms; 
					for (SwitchExprArm arm : arms)
						arm.traverse(visitor, blockScope);
				}
			}
			visitor.endVisit(this, blockScope);
	}
	@Override
	public void generateCode(BlockScope currentScope, CodeStream codeStream, boolean valueRequired) {
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
	private void reportDuplicateCase(final CaseStatement duplicate, final CaseStatement original, int length) {
		if (this.duplicateCaseStatements == null) {
			this.scope.problemReporter().duplicateCase(original);
			this.scope.problemReporter().duplicateCase(duplicate);
			this.duplicateCaseStatements = new CaseStatement[length];
			this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = original;
			this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
		} else {
			boolean found = false;
			searchReportedDuplicate: for (int k = 2; k < this.duplicateCaseStatementsCounter; k++) {
				if (this.duplicateCaseStatements[k] == duplicate) {
					found = true;
					break searchReportedDuplicate;
				}
			}
			if (!found) {
				this.scope.problemReporter().duplicateCase(duplicate);
				this.duplicateCaseStatements[this.duplicateCaseStatementsCounter++] = duplicate;
			}
		}
	}

	private void collectResultExpressions() {
		if (this.resultExpressions != null)
			return; // already calculated.

		class ResultExpressionsCollector extends ASTVisitor {
			Stack<SwitchExpression> seStack = new Stack<>();
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean visit(SwitchExpression switchExpression, BlockScope blockScope) {
				switchExpression.resultExpressions = new ArrayList<>(0); // by default
				this.seStack.add(switchExpression);
				return true; // do nothing by default, keep traversing
			}
			@Override
			public void endVisit(SwitchExpression switchExpression, BlockScope blockScope) {
				this.seStack.pop();
			}
			@SuppressWarnings("synthetic-access")
			@Override
			public boolean visit(BreakStatement breakStatement, BlockScope blockScope) {
				if (breakStatement.expression != null && !this.seStack.isEmpty()) {
					this.seStack.peek().resultExpressions.add(breakStatement.expression);
				}
				return true; // do nothing by default, keep traversing
			}
		}
		ResultExpressionsCollector reCollector = new ResultExpressionsCollector();
		this.traverse(reCollector, this.scope);
	}
	@Override
	public TypeBinding resolveType(BlockScope upperScope) {
		try {
			// JLS12 15.29.1
			boolean isEnumSwitch = false;
			boolean isStringSwitch = false;
			TypeBinding expressionType = this.expression.resolveType(upperScope);
			CompilerOptions compilerOptions = upperScope.compilerOptions();
			
			/* The type of the selector expression must be char, byte, short, int, Character, Byte, Short, Integer, String,
			 * or an enum type (8.9), or a compile-time error occurs.
			 */
			if (expressionType != null) {
				this.expression.computeConversion(upperScope, expressionType, expressionType);
				checkType: {
					if (!expressionType.isValidBinding()) {
						expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
						break checkType;
					} else if (expressionType.isBaseType()) {
						if (this.expression.isConstantValueOfTypeAssignableToType(expressionType, TypeBinding.INT))
							break checkType;
						if (expressionType.isCompatibleWith(TypeBinding.INT))
							break checkType;
					} else if (expressionType.isEnum()) {
						isEnumSwitch = true;
						break checkType;
					} else if (upperScope.isBoxingCompatibleWith(expressionType, TypeBinding.INT)) {
						this.expression.computeConversion(upperScope, TypeBinding.INT, expressionType);
						break checkType;
					} else if (expressionType.id == TypeIds.T_JavaLangString) {
						isStringSwitch = true;
						break checkType;
					}
					upperScope.problemReporter().incorrectSwitchType(this.expression, expressionType);
					expressionType = null; // fault-tolerance: ignore type mismatch from constants from hereon
				}
			}
			if (isStringSwitch) {
				// the secret variable should be created before iterating over the switch's statements that could
				// create more locals. This must be done to prevent overlapping of locals
				// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=356002 [ref SwitchStatement]
				this.dispatchStringCopy  = new LocalVariableBinding(SecretStringVariableName, upperScope.getJavaLangString(), ClassFileConstants.AccDefault, false);
				upperScope.addLocalVariable(this.dispatchStringCopy);
				this.dispatchStringCopy.setConstant(Constant.NotAConstant);
				this.dispatchStringCopy.useFlag = LocalVariableBinding.USED;
			}
			
			SwitchExprArm arms[] = this.exprArms;
			if (arms == null || arms.length == 0) {
				//	Report Error JLS 12 15.29.1  The switch block must not be empty.
				upperScope.problemReporter().switchExpressionEmptySwitchBlock(this);
				return null;
			}

			// At this point, this.exprArms.length > 0
			this.scope = new BlockScope(upperScope);
			LookupEnvironment env = this.scope.environment();

			int length;
			// collection of cases is too big but we will only iterate until caseCount
			this.cases = new CaseStatement[length = this.exprArms.length];
			if (!isStringSwitch) {
				this.constants = new int[length];
			} else {
				this.stringConstants = new String[length];
			}
			int counter = 0;
			for (int i = 0; i < length; i++) {
				Constant constant1;
				final SwitchExprArm arm = this.exprArms[i];
				if ((constant1 = arm.resolveCase(this.scope, expressionType, this)) != Constant.NotAConstant) {
					if (!isStringSwitch) {
						int key = constant1.intValue();
						//----check for duplicate case statement------------
						for (int j = 0; j < counter; j++) {
							if (this.constants[j] == key) {
								reportDuplicateCase(arm.getLhs(), this.cases[j], length);
							}
						}
						this.constants[counter++] = key;
					} else {
						String key = constant1.stringValue();
						//----check for duplicate case statement------------
						for (int j = 0; j < counter; j++) {
							if (this.stringConstants[j].equals(key)) {
								reportDuplicateCase(arm.getLhs(), this.cases[j], length);
							}
						}
						this.stringConstants[counter++] = key;			
					}
				}
			}
			if (length != counter) { // resize constants array
				if (!isStringSwitch) {
					System.arraycopy(this.constants, 0, this.constants = new int[counter], 0, counter);
				} else {
					System.arraycopy(this.stringConstants, 0, this.stringConstants = new String[counter], 0, counter);
				}
			}

			// check default case for all kinds of switch:
			if (this.defaultCase == null) {
				if (compilerOptions.getSeverity(CompilerOptions.MissingDefaultCase) == ProblemSeverities.Ignore) {
					if (isEnumSwitch) {
						upperScope.methodScope().hasMissingSwitchDefault = true;
					}
				}
				// cannot ignore in SwitchExpressions - this is a compile-time error
				if (!isEnumSwitch) // check for all cases of enum below and report if there an entry missing
					upperScope.problemReporter().missingDefaultCase(this, false /* isEnumSwitch */, expressionType);
			}
			// for enum switch, check if all constants are accounted for (perhaps depending on existence of a default case)
			if (isEnumSwitch) {
				if (this.defaultCase == null || compilerOptions.reportMissingEnumCaseDespiteDefault) {
					int constantCount = this.constants == null ? 0 : this.constants.length; // could be null if no case statement
					if (constantCount == this.caseCount
							&& this.caseCount != ((ReferenceBinding)expressionType).enumConstantCount()) {
						FieldBinding[] enumFields = ((ReferenceBinding)expressionType.erasure()).fields();
						for (int i = 0, max = enumFields.length; i <max; i++) {
							FieldBinding enumConstant = enumFields[i];
							if ((enumConstant.modifiers & ClassFileConstants.AccEnum) == 0) continue;
							findConstant : {
								for (int j = 0; j < this.caseCount; j++) {
									if ((enumConstant.id + 1) == this.constants[j]) // zero should not be returned see bug 141810
										break findConstant;
								}
								// enum constant did not get referenced from switch
								boolean suppress = (this.defaultCase != null && (this.defaultCase.bits & DocumentedCasesOmitted) != 0);
								if (!suppress) {
									upperScope.problemReporter().missingEnumConstantCase(this, enumConstant);
								}
							}
						}
					}
				}
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
			
			if (this.constant != Constant.NotAConstant) {
				this.constant = Constant.NotAConstant;
				this.originalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
				for (int i = 0; i < resultExpressionsCount; ++i) {
					this.originalValueResultExpressionTypes[i] = this.resultExpressions.get(i).resolveType(this.scope);
				}
				// TODO: should we return if one of the types is null? to check
			} else {
				for (int i = 0; i < resultExpressionsCount; ++i) {
					if (this.originalValueResultExpressionTypes[i].kind() == Binding.POLY_TYPE)
						this.originalValueResultExpressionTypes[i] = this.resultExpressions.get(i).resolveType(this.scope);
				}
				// TODO: should we return if one of the types is null? to check
			}
			this.finalValueResultExpressionTypes = new TypeBinding[resultExpressionsCount];
			for (int i = 0; i < resultExpressionsCount; ++i)
				this.finalValueResultExpressionTypes[i] = this.originalValueResultExpressionTypes[i];
			
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
				tmp = this.originalValueResultExpressionTypes[i].isNumericType() ? this.originalValueResultExpressionTypes[i] : env.computeBoxingType(this.originalValueResultExpressionTypes[i]);
				if (!tmp.isNumericType()) {
					typeNumeric = false;
					break;
				}
				resultNumeric = getFirstResults(tmp, this.resultExpressions.get(i), resultNumeric);
				typeSet.add(TypeBinding.wellKnownType(this.scope, tmp.id));
			}
			if (typeNumeric) {
				resultNumeric = resultNumeric != null ? resultNumeric : getResultNumeric(typeSet, this.originalValueResultExpressionTypes);
				typeSet = null; // hey gc!
				for (int i = 0; i < resultExpressionsCount; ++i) {
					// auto-unboxing and/or widening/narrrowing JLS 12 5.6.3
					this.resultExpressions.get(i).computeConversion(this.scope, resultNumeric, this.originalValueResultExpressionTypes[i]);
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
				for (int i = 0, l = arms.length; i < l; ++i) {
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
		for (int i = 0, l = this.exprArms.length; i < l; ++i) {
			Expression e = this.exprArms[i];
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

		if (this.expressionContext != ASSIGNMENT_CONTEXT && this.expressionContext != INVOCATION_CONTEXT)
			return false;
		
		for (TypeBinding tmp : this.originalValueResultExpressionTypes)
			if (tmp == null) // resolution error.
				return false;
		
		for (SwitchExprArm arm : this.exprArms)
			if (arm.isPolyExpression())
				return true;
		
		// "... unless all arms produce primitives (or boxed primitives)":	
		boolean allPrimitives = true;
		for (TypeBinding tmp : this.originalValueResultExpressionTypes) {
			allPrimitives &= tmp.isBaseType() || (tmp.id >= TypeIds.T_JavaLangByte && tmp.id <= TypeIds.T_JavaLangBoolean);
		}
		if (!allPrimitives)
			return false;

		return this.isPolyExpression = true;
	}
	
	@Override
	public StringBuffer printExpression(int indent, StringBuffer output) {
		output.append("switch ("); //$NON-NLS-1$
		this.expression.printExpression(0, output).append(") {"); //$NON-NLS-1$
		for (SwitchExprArm arm : this.exprArms)
			arm.print(0, output);
		output.append('\n');
		return printIndent(indent, output).append('}');
	}
}
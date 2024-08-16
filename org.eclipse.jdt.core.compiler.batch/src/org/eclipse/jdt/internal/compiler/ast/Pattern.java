/*******************************************************************************
 * Copyright (c) 2021, 2024 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
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

import org.eclipse.jdt.internal.compiler.codegen.BranchLabel;
import org.eclipse.jdt.internal.compiler.codegen.CodeStream;
import org.eclipse.jdt.internal.compiler.impl.JavaFeature;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodBinding;
import org.eclipse.jdt.internal.compiler.lookup.Scope;
import org.eclipse.jdt.internal.compiler.lookup.TypeBinding;

public abstract class Pattern extends Expression {

	boolean isTotalTypeNode = false;

	private Pattern enclosingPattern;

	protected MethodBinding accessorMethod;

	public int index = -1; // index of this in enclosing record pattern, or -1 for top level patterns

	public boolean isUnguarded = true; // no guard or guard is compile time constant true.

	public enum PrimitiveConversionRoute {
		IDENTITY_CONVERSION,
		WIDENING_PRIMITIVE_CONVERSION,
		NARROWING_PRIMITVE_CONVERSION,
		WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION,
		BOXING_CONVERSION,
		BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION,
		// following for reference
		WIDENING_REFERENCE_AND_UNBOXING_COVERSION,
		WIDENING_REFERENCE_AND_UNBOXING_COVERSION_AND_WIDENING_PRIMITIVE_CONVERSION,
		NARROWING_AND_UNBOXING_CONVERSION,
		UNBOXING_CONVERSION,
		UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION,
		NO_CONVERSION_ROUTE
	}

	protected TypeBinding expectedType;
	record TestContextRecord(TypeBinding left, TypeBinding right, PrimitiveConversionRoute route) {}

	public Pattern getEnclosingPattern() {
		return this.enclosingPattern;
	}

	public void setEnclosingPattern(RecordPattern enclosingPattern) {
		this.enclosingPattern = enclosingPattern;
	}

	public boolean isUnnamed() {
		return false;
	}

	/**
	 * Implement the rules in the spec under 14.11.1.1 Exhaustive Switch Blocks
	 *
	 * @return whether pattern covers the given type or not
	 */
	public boolean coversType(TypeBinding type) {
		if (!isUnguarded())
			return false;
		if (type == null || this.resolvedType == null)
			return false;
		return (type.isSubtypeOf(this.resolvedType, false));
	}

	// Given a non-null instance of same type, would the pattern always match ?
	public boolean matchFailurePossible() {
		return false;
	}

	public boolean isUnconditional(TypeBinding t) {
		return isUnguarded() && coversType(t);
	}

	public abstract void generateCode(BlockScope currentScope, CodeStream codeStream, BranchLabel patternMatchLabel, BranchLabel matchFailLabel);

	public void generateTestingConversion(BlockScope scope, CodeStream codeStream) {
		// TODO: MAKE THIS abstract
	}


	@Override
	public boolean checkUnsafeCast(Scope scope, TypeBinding castType, TypeBinding expressionType, TypeBinding match, boolean isNarrowing) {
		if (!castType.isReifiable())
			return CastExpression.checkUnsafeCast(this, scope, castType, expressionType, match, isNarrowing);
		else
			return super.checkUnsafeCast(scope, castType, expressionType, match, isNarrowing);
	}

	public TypeReference getType() {
		return null;
	}

	// 14.30.3 Properties of Patterns: A pattern p is said to be applicable at a type T if ...
	protected boolean isApplicable(TypeBinding other, BlockScope scope) {
		TypeBinding patternType = this.resolvedType;
		if (patternType == null) // ill resolved pattern
			return false;
		// 14.30.3 Properties of Patterns doesn't allow boxing nor unboxing, primitive widening/narrowing.
		if (patternType.isBaseType() != other.isBaseType()) {
			scope.problemReporter().incompatiblePatternType(this, other, patternType);
			return false;
		}
		if (patternType.isBaseType()) {
			PrimitiveConversionRoute route = Pattern.findPrimitiveConversionRoute(this.resolvedType, this.expectedType, scope);
			if (!TypeBinding.equalsEquals(other, patternType)
					&& route == PrimitiveConversionRoute.NO_CONVERSION_ROUTE) {
				scope.problemReporter().incompatiblePatternType(this, other, patternType);
				return false;
			}
		} else if (!checkCastTypesCompatibility(scope, other, patternType, null, true)) {
			scope.problemReporter().incompatiblePatternType(this, other, patternType);
			return false;
		}
		return true;
	}

	public abstract boolean dominates(Pattern p);

	@Override
	public StringBuilder print(int indent, StringBuilder output) {
		return this.printExpression(indent, output);
	}

	public Pattern[] getAlternatives() {
		return new Pattern [] { this };
	}

	public abstract void setIsEitherOrPattern(); // if set, is one of multiple (case label) patterns and so pattern variables can't be named.

	@Override
	public void setExpectedType(TypeBinding expectedType) {
		this.expectedType = expectedType;
	}

	public boolean isUnguarded() {
		return this.isUnguarded;
	}

	public void setIsGuarded() {
		this.isUnguarded = false;
	}
	public static boolean isBoxing(TypeBinding left, TypeBinding right) {

		if (right.isBaseType() && !left.isBaseType()) {
			int expected = switch(right.id) {
				case T_char     -> T_JavaLangCharacter;
				case T_byte     -> T_JavaLangByte;
				case T_short    -> T_JavaLangShort;
				case T_boolean  -> T_JavaLangBoolean;
				case T_long     -> T_JavaLangLong;
				case T_double   -> T_JavaLangDouble;
				case T_float    -> T_JavaLangFloat;
				case T_int      -> T_JavaLangInteger;
				default -> -1;
			};
			return left.id == expected;
		}
		return false;
	}
	public static PrimitiveConversionRoute findPrimitiveConversionRoute(TypeBinding destinationType, TypeBinding expressionType, BlockScope scope) {
		if (!(JavaFeature.PRIMITIVES_IN_PATTERNS.isSupported(
				scope.compilerOptions().sourceLevel,
				scope.compilerOptions().enablePreviewFeatures))) {
			return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
		}
		boolean destinationIsBaseType = destinationType.isBaseType();
		boolean expressionIsBaseType = expressionType.isBaseType();
		if (destinationIsBaseType && expressionIsBaseType) {
			if (TypeBinding.equalsEquals(destinationType, expressionType)) {
				return PrimitiveConversionRoute.IDENTITY_CONVERSION;
			}
			if (BaseTypeBinding.isWidening(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.WIDENING_PRIMITIVE_CONVERSION;
			if (BaseTypeBinding.isNarrowing(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.NARROWING_PRIMITVE_CONVERSION;
			if (BaseTypeBinding.isWideningAndNarrowing(destinationType.id, expressionType.id))
				return PrimitiveConversionRoute.WIDENING_AND_NARROWING_PRIMITIVE_CONVERSION;
		} else {
			if (expressionIsBaseType) {
				if (isBoxing(destinationType, expressionType))
					return PrimitiveConversionRoute.BOXING_CONVERSION;
				if (scope.environment().computeBoxingType(expressionType).isCompatibleWith(destinationType))
					return PrimitiveConversionRoute.BOXING_CONVERSION_AND_WIDENING_REFERENCE_CONVERSION;

			} else if (expressionType.isBoxedPrimitiveType() && destinationIsBaseType) {
				TypeBinding unboxedExpressionType = scope.environment().computeBoxingType(expressionType);
				 //TODO: a widening reference conversion followed by an unboxing conversion
				 //TODO: a widening reference conversion followed by an unboxing conversion, then followed by a widening primitive conversion
				 //TODO: a narrowing reference conversion that is checked followed by an unboxing conversion
				 //an unboxing conversion (5.1.8)
				if (TypeBinding.equalsEquals(destinationType, unboxedExpressionType))
					return PrimitiveConversionRoute.UNBOXING_CONVERSION;
				 //an unboxing conversion followed by a widening primitive conversion
				if (BaseTypeBinding.isWidening(destinationType.id, unboxedExpressionType.id))
					return PrimitiveConversionRoute.UNBOXING_AND_WIDENING_PRIMITIVE_CONVERSION;
			}
		}
		return PrimitiveConversionRoute.NO_CONVERSION_ROUTE;
	}
}
/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.ASTVisitor;

public abstract class ASTNode implements BaseTypes, CompilerModifiers, TypeConstants, TypeIds {
	
	public int sourceStart, sourceEnd;

	//some global provision for the hierarchy
	public final static Constant NotAConstant = Constant.NotAConstant;

	// storage for internal flags (32 bits)						BIT USAGE
	public final static int Bit1 = 0x1; 							// return type (operator) | name reference kind (name ref) | add assertion (type decl) | useful empty statement (empty statement)
	public final static int Bit2 = 0x2; 							// return type (operator) | name reference kind (name ref) | has local type (type, method, field decl)
	public final static int Bit3 = 0x4; 							// return type (operator) | name reference kind (name ref) | implicit this (this ref)
	public final static int Bit4 = 0x8; 							// return type (operator) | first assignment to local (local decl) | undocumented empty block (block, type and method decl)
	public final static int Bit5 = 0x10; 						// value for return (expression) | has all method bodies (unit) | supertype ref (type ref)
	public final static int Bit6 = 0x20; 						// depth (name ref, msg) | only value required (binary expression) | ignore need cast check (cast expression)
	public final static int Bit7 = 0x40; 						// depth (name ref, msg) | operator (operator) | need runtime checkcast (cast expression)
	public final static int Bit8 = 0x80; 						// depth (name ref, msg) | operator (operator) 
	public final static int Bit9 = 0x100; 						// depth (name ref, msg) | operator (operator) | is local type (type decl)
	public final static int Bit10= 0x200; 					// depth (name ref, msg) | operator (operator) | is anonymous type (type decl)
	public final static int Bit11 = 0x400; 					// depth (name ref, msg) | operator (operator) | is member type (type decl)
	public final static int Bit12 = 0x800; 					// depth (name ref, msg) | operator (operator)
	public final static int Bit13 = 0x1000; 				// depth (name ref, msg) 
	public final static int Bit14 = 0x2000; 				// strictly assigned (reference lhs)
	public final static int Bit15 = 0x4000; 				// is unnecessary cast (expression)
	public final static int Bit16 = 0x8000; 				// in javadoc comment (name ref, type ref, msg)
	public final static int Bit17 = 0x10000; 				// compound assigned (reference lhs)
	public final static int Bit18 = 0x20000; 
	public final static int Bit19 = 0x40000; 
	public final static int Bit20 = 0x80000; 
	public final static int Bit21 = 0x100000; 		
	public final static int Bit22 = 0x200000; 			// parenthesis count (expression)
	public final static int Bit23 = 0x400000; 			// parenthesis count (expression)
	public final static int Bit24 = 0x800000; 			// parenthesis count (expression)
	public final static int Bit25 = 0x1000000; 			// parenthesis count (expression)
	public final static int Bit26 = 0x2000000; 			// parenthesis count (expression)
	public final static int Bit27 = 0x4000000; 			// parenthesis count (expression)
	public final static int Bit28 = 0x8000000; 			// parenthesis count (expression)
	public final static int Bit29 = 0x10000000; 		// parenthesis count (expression)
	public final static int Bit30 = 0x20000000; 		// assignment with no effect (assignment) | elseif (if statement)
	public final static int Bit31 = 0x40000000; 		// local declaration reachable (local decl)
	public final static int Bit32 = 0x80000000; 		// reachable (statement)

	public final static long Bit32L = 0x80000000L; 		
	public final static long Bit33L = 0x100000000L;
	public final static long Bit34L = 0x200000000L;
	public final static long Bit35L = 0x400000000L;
	public final static long Bit36L = 0x800000000L;
	public final static long Bit37L = 0x1000000000L;
	public final static long Bit38L = 0x2000000000L;
	public final static long Bit39L = 0x4000000000L;
	public final static long Bit40L = 0x8000000000L;

	public int bits = IsReachableMASK; 				// reachable by default

	// for operators 
	public static final int ReturnTypeIDMASK = Bit1|Bit2|Bit3|Bit4; 
	public static final int OperatorSHIFT = 6;	// Bit7 -> Bit12
	public static final int OperatorMASK = Bit7|Bit8|Bit9|Bit10|Bit11|Bit12; // 6 bits for operator ID

	// for binary expressions
	public static final int ValueForReturnMASK = Bit5; 
	public static final int OnlyValueRequiredMASK = Bit6; 

	// for cast expressions
	public static final int UnnecessaryCastMask = Bit15;
	public static final int NeedRuntimeCheckCastMASK = Bit7;
	public static final int IgnoreNeedForCastCheckMASK = Bit6;
	
	// for name references 
	public static final int RestrictiveFlagMASK = Bit1|Bit2|Bit3;	
	public static final int FirstAssignmentToLocalMASK = Bit4;
	
	// for this reference
	public static final int IsImplicitThisMask = Bit3; 

	// for single name references
	public static final int DepthSHIFT = 5;	// Bit6 -> Bit13
	public static final int DepthMASK = Bit6|Bit7|Bit8|Bit9|Bit10|Bit11|Bit12|Bit13; // 8 bits for actual depth value (max. 255)

	// for statements 
	public static final int IsReachableMASK = Bit32; 
	public static final int IsLocalDeclarationReachableMASK = Bit31; 

	// for type declaration
	public static final int AddAssertionMASK = Bit1;
	public static final int IsLocalTypeMASK = Bit9;
	public static final int IsAnonymousTypeMASK = Bit10; // used to test for anonymous 
	public static final int AnonymousAndLocalMask = IsAnonymousTypeMASK | IsLocalTypeMASK; // used to set anonymous marker
	public static final int IsMemberTypeMASK = Bit11; // local member do not know it is local at parse time (need to look at binding)
	
	// for type, method and field declarations 
	public static final int HasLocalTypeMASK = Bit2; // cannot conflict with AddAssertionMASK

	// for expression 
	public static final int ParenthesizedSHIFT = 21; // Bit22 -> Bit29
	public static final int ParenthesizedMASK = Bit22|Bit23|Bit24|Bit25|Bit26|Bit27|Bit28|Bit29; // 8 bits for parenthesis count value (max. 255)

	// for assignment
	public static final int IsAssignmentWithNoEffectMASK = Bit30;	
	
	// for references on lhs of assignment
	public static final int IsStrictlyAssignedMASK = Bit14; // set only for true assignments, as opposed to compound ones
	public static final int IsCompoundAssignedMASK = Bit17; // set only for compound assignments, as opposed to other ones

	// for empty statement
	public static final int IsUsefulEmptyStatementMASK = Bit1;

	// for block and method declaration
	public static final int UndocumentedEmptyBlockMASK = Bit4;

	// for compilation unit
	public static final int HasAllMethodBodies = Bit5;
	
	// for references in Javadoc comments
	public static final int InsideJavadoc = Bit16;
	
	// for if statement
	public static final int IsElseIfStatement = Bit30;
	
	// for type reference
	public static final int IsSuperType = Bit5;
	
	
	public ASTNode() {

		super();
	}
	public static void checkInvocationArguments(BlockScope scope, Expression receiver, TypeBinding receiverType, MethodBinding method, Expression[] arguments, TypeBinding[] argumentTypes, boolean argsContainCast, InvocationSite invocationSite) {
		boolean unsafeWildcardInvocation = false;
		for (int i = 0; i < arguments.length; i++) {
		    TypeBinding parameterType = method.parameters[i];
		    TypeBinding argumentType = argumentTypes[i];
			arguments[i].computeConversion(scope, parameterType, argumentType);
			if (argumentType != NullBinding && parameterType.isWildcard() && ((WildcardBinding)parameterType).kind != Wildcard.SUPER) {
			    unsafeWildcardInvocation = true;
			} else if (argumentType != parameterType 
			        && argumentType.isRawType() 
			        && (parameterType.isBoundParameterizedType() || parameterType.isGenericType())) {
			    scope.problemReporter().unsafeRawConversion(arguments[i], argumentType, parameterType);
			}
		}
		if (argsContainCast) {
			CastExpression.checkNeedForArgumentCasts(scope, receiver, receiverType, method, arguments, argumentTypes, invocationSite);
		}
		if (unsafeWildcardInvocation) {
		    scope.problemReporter().wildcardInvocation((ASTNode)invocationSite, receiverType, method, argumentTypes);
		} else if (receiverType.isRawType() && method.hasSubstitutedParameters()) {
		    scope.problemReporter().unsafeRawInvocation((ASTNode)invocationSite, receiverType, method);
		}
	}
	public ASTNode concreteStatement() {
		return this;
	}

	/* Answer true if the field use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isFieldUseDeprecated(FieldBinding field, Scope scope, boolean isStrictlyAssigned) {

		if (!isStrictlyAssigned && field.isPrivate() && !scope.isDefinedInField(field)) {
			// ignore cases where field is used from within inside itself 
			field.modifiers |= AccPrivateUsed;
		}

		if (!field.isViewedAsDeprecated()) return false;

		// inside same unit - no report
		if (scope.isDefinedInSameUnit(field.declaringClass)) return false;
		
		// if context is deprecated, may avoid reporting
		if (!scope.environment().options.reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode()) return false;
		return true;
	}

	public boolean isImplicitThis() {
		
		return false;
	}
	
	/* Answer true if the method use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isMethodUseDeprecated(MethodBinding method, Scope scope) {

		if (method.isPrivate() && !scope.isDefinedInMethod(method)) {
			// ignore cases where method is used from within inside itself (e.g. direct recursions)
			method.original().modifiers |= AccPrivateUsed;
		}
		
		if (!method.isViewedAsDeprecated()) return false;

		// inside same unit - no report
		if (scope.isDefinedInSameUnit(method.declaringClass)) return false;
		
		// if context is deprecated, may avoid reporting
		if (!scope.environment().options.reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode()) return false;
		return true;
	}

	public boolean isSuper() {

		return false;
	}

	public boolean isThis() {

		return false;
	}

	/* Answer true if the type use is considered deprecated.
	* An access in the same compilation unit is allowed.
	*/
	public final boolean isTypeUseDeprecated(TypeBinding type, Scope scope) {

		if (type.isArrayType())
			type = ((ArrayBinding) type).leafComponentType;
		if (type.isBaseType())
			return false;

		ReferenceBinding refType = (ReferenceBinding) type;

		if (refType.isPrivate() && !scope.isDefinedInType(refType)) {
			// ignore cases where type is used from within inside itself 
			((ReferenceBinding)refType.erasure()).modifiers |= AccPrivateUsed;
		}

		if (!refType.isViewedAsDeprecated()) return false;
		
		// inside same unit - no report
		if (scope.isDefinedInSameUnit(refType)) return false;
		
		// if context is deprecated, may avoid reporting
		if (!scope.environment().options.reportDeprecationInsideDeprecatedCode && scope.isInsideDeprecatedCode()) return false;
		return true;
	}

	public abstract StringBuffer print(int indent, StringBuffer output);

	public static StringBuffer printIndent(int indent, StringBuffer output) {

		for (int i = indent; i > 0; i--) output.append("  "); //$NON-NLS-1$
		return output;
	}

	public static StringBuffer printModifiers(int modifiers, StringBuffer output) {

		if ((modifiers & AccPublic) != 0)
			output.append("public "); //$NON-NLS-1$
		if ((modifiers & AccPrivate) != 0)
			output.append("private "); //$NON-NLS-1$
		if ((modifiers & AccProtected) != 0)
			output.append("protected "); //$NON-NLS-1$
		if ((modifiers & AccStatic) != 0)
			output.append("static "); //$NON-NLS-1$
		if ((modifiers & AccFinal) != 0)
			output.append("final "); //$NON-NLS-1$
		if ((modifiers & AccSynchronized) != 0)
			output.append("synchronized "); //$NON-NLS-1$
		if ((modifiers & AccVolatile) != 0)
			output.append("volatile "); //$NON-NLS-1$
		if ((modifiers & AccTransient) != 0)
			output.append("transient "); //$NON-NLS-1$
		if ((modifiers & AccNative) != 0)
			output.append("native "); //$NON-NLS-1$
		if ((modifiers & AccAbstract) != 0)
			output.append("abstract "); //$NON-NLS-1$
		return output;
	}
	public int sourceStart() {
		return this.sourceStart;
	}
	public int sourceEnd() {
		return this.sourceEnd;
	}
	public String toString() {

		return print(0, new StringBuffer(30)).toString();
	}

	public void traverse(ASTVisitor visitor, BlockScope scope) {
		// do nothing by default
	}
}

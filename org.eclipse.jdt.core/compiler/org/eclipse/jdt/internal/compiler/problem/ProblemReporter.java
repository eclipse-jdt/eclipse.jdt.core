/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.util.Util;

public class ProblemReporter extends ProblemHandler implements ProblemReasons {
	
	public ReferenceContext referenceContext;
public ProblemReporter(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	super(policy, options, problemFactory);
}
public void abortDueToInternalError(String errorMessage) {
	String[] arguments = new String[] {errorMessage};
	this.handle(
		IProblem.Unclassified,
		arguments,
		arguments,
		Error | Abort,
		0,
		0);
}
public void abortDueToInternalError(String errorMessage, AstNode location) {
	String[] arguments = new String[] {errorMessage};
	this.handle(
		IProblem.Unclassified,
		arguments,
		arguments,
		Error | Abort,
		location.sourceStart,
		location.sourceEnd);
}
public void abstractMethodCannotBeOverridden(SourceTypeBinding type, MethodBinding concreteMethod) {

	this.handle(
		// %1 must be abstract since it cannot override the inherited package-private abstract method %2
		IProblem.AbstractMethodCannotBeOverridden,
		new String[] {
			new String(type.sourceName()), 
			new String(
					CharOperation.concat(
						concreteMethod.declaringClass.readableName(),
						concreteMethod.readableName(),
						'.'))},
		new String[] {
			new String(type.sourceName()), 
			new String(
					CharOperation.concat(
						concreteMethod.declaringClass.shortReadableName(),
						concreteMethod.shortReadableName(),
						'.'))},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {

	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.AbstractMethodInAbstractClass,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod) {
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		IProblem.AbstractMethodMustBeImplemented,
		new String[] {
			new String(
				CharOperation.concat(
					abstractMethod.declaringClass.readableName(),
					abstractMethod.readableName(),
					'.'))},
		new String[] {
			new String(
				CharOperation.concat(
					abstractMethod.declaringClass.shortReadableName(),
					abstractMethod.shortReadableName(),
					'.'))},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodNeedingNoBody(AbstractMethodDeclaration method) {
	this.handle(
		IProblem.BodyForAbstractMethod,
		NoArgument,
		NoArgument,
		method.sourceStart,
		method.sourceEnd,
		method,
		method.compilationResult());
}
public void alreadyDefinedLabel(char[] labelName, AstNode location) {
	String[] arguments = new String[] {new String(labelName)};
	this.handle(
		IProblem.DuplicateLabel,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void anonymousClassCannotExtendFinalClass(Expression expression, TypeBinding type) {
	this.handle(
		IProblem.AnonymousClassCannotExtendFinalClass,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void argumentTypeCannotBeVoid(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	String[] arguments = new String[] {new String(methodDecl.selector), new String(arg.name)};
	this.handle(
		IProblem.ArgumentTypeCannotBeVoid,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void argumentTypeCannotBeVoidArray(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	String[] arguments = new String[] {new String(methodDecl.selector), new String(arg.name)};
	this.handle(
		IProblem.ArgumentTypeCannotBeVoidArray,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void argumentTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ArgumentTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ArgumentTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ArgumentTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ArgumentTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ArgumentTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), arg.name(), new String(expectedType.readableName())},
		new String[] {new String(methodDecl.selector), arg.name(), new String(expectedType.shortReadableName())},
		arg.type.sourceStart,
		arg.type.sourceEnd);
}
public void arrayConstantsOnlyInArrayInitializers(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.ArrayConstantsOnlyInArrayInitializers,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void assignmentHasNoEffect(Assignment assignment, char[] name){
	String[] arguments = new String[] { new String(name) };	
	this.handle(
			IProblem.AssignmentHasNoEffect,
			arguments,
			arguments,
			assignment.sourceStart,
			assignment.sourceEnd);
}
public void attemptToReturnNonVoidExpression(ReturnStatement returnStatement, TypeBinding expectedType) {
	this.handle(
		IProblem.VoidMethodReturnsValue,
		new String[] {new String(expectedType.readableName())},
		new String[] {new String(expectedType.shortReadableName())},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void attemptToReturnVoidValue(ReturnStatement returnStatement) {
	this.handle(
		IProblem.MethodReturnsVoid,
		NoArgument,
		NoArgument,
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	String[] arguments = new String[] {new String(location.selector), parametersAsString(location.binding)};
	if (location.isConstructor()) {
		this.handle(
			IProblem.BytecodeExceeds64KLimitForConstructor,
			arguments,
			arguments,
			Error | Abort,
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.BytecodeExceeds64KLimit,
			arguments,
			arguments,
			Error | Abort,
			location.sourceStart,
			location.sourceEnd);
	}
}
public void bytecodeExceeds64KLimit(TypeDeclaration location) {
	this.handle(
		IProblem.BytecodeExceeds64KLimitForClinit,
		NoArgument,
		NoArgument,
		Error | Abort,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAllocateVoidArray(Expression expression) {
	this.handle(
		IProblem.CannotAllocateVoidArray,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void cannotAssignToFinalField(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.FinalFieldAssignment,
		new String[] {
			(field.declaringClass == null ? "array" : new String(field.declaringClass.readableName())), //$NON-NLS-1$
			new String(field.readableName())},
		new String[] {
			(field.declaringClass == null ? "array" : new String(field.declaringClass.shortReadableName())), //$NON-NLS-1$
			new String(field.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAssignToFinalLocal(LocalVariableBinding local, AstNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.NonBlankFinalLocalAssignment,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAssignToFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	String[] arguments = new String[] {new String(local.readableName())};
	this.handle(
		IProblem.FinalOuterLocalAssignment,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotDeclareLocalInterface(char[] interfaceName, int sourceStart, int sourceEnd) {
	String[] arguments = new String[] {new String(interfaceName)};
	this.handle(
		IProblem.CannotDefineInterfaceInLocalType,
		arguments,
		arguments,
		sourceStart,
		sourceEnd);
}
public void cannotDefineDimensionsAndInitializer(ArrayAllocationExpression expresssion) {
	this.handle(
		IProblem.CannotDefineDimensionExpressionsWithInit,
		NoArgument,
		NoArgument,
		expresssion.sourceStart,
		expresssion.sourceEnd);
}
public void cannotDireclyInvokeAbstractMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.DirectInvocationOfAbstractMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsShortString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void cannotImportPackage(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.CannotImportPackage,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void cannotInstantiate(TypeReference typeRef, TypeBinding type) {
	this.handle(
		IProblem.InvalidClassInstantiation,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
public void cannotReferToNonFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	String[] arguments =new String[]{ new String(local.readableName())};
	this.handle(
		IProblem.OuterLocalMustBeFinal,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotReturnInInitializer(AstNode location) {
	this.handle(
		IProblem.CannotReturnInInitializer,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotThrowNull(ThrowStatement statement) {
	this.handle(
		IProblem.CannotThrowNull,
		NoArgument,
		NoArgument,
		statement.sourceStart,
		statement.sourceEnd);
}
public void cannotThrowType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	this.handle(
		IProblem.CannotThrowType,
		new String[] {new String(expectedType.readableName())},
		new String[] {new String(expectedType.shortReadableName())},
		exceptionType.sourceStart,
		exceptionType.sourceEnd);
}
public void cannotUseSuperInJavaLangObject(AstNode reference) {
	this.handle(
		IProblem.ObjectHasNoSuperclass,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void cannotUseSuperInCodeSnippet(int start, int end) {
	this.handle(
		IProblem.CannotUseSuperInCodeSnippet,
		NoArgument,
		NoArgument,
		Error | Abort,
		start,
		end);
}
public void caseExpressionMustBeConstant(Expression expression) {
	this.handle(
		IProblem.NonConstantExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void classExtendFinalClass(SourceTypeBinding type, TypeReference superclass, TypeBinding expectedType) {
	String name = new String(type.sourceName());
	String expectedFullName = new String(expectedType.readableName());
	String expectedShortName = new String(expectedType.shortReadableName());
	if (expectedShortName.equals(name)) expectedShortName = expectedFullName;
	this.handle(
		IProblem.ClassExtendFinalClass,
		new String[] {expectedFullName, name},
		new String[] {expectedShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
}
public void codeSnippetMissingClass(String missing, int start, int end) {
	String[] arguments = new String[]{missing};
	this.handle(
		IProblem.CodeSnippetMissingClass,
		arguments,
		arguments,
		Error | Abort,
		start,
		end);
}
public void codeSnippetMissingMethod(String className, String missingMethod, String argumentTypes, int start, int end) {
	String[] arguments = new String[]{ className, missingMethod, argumentTypes };
	this.handle(
		IProblem.CodeSnippetMissingMethod,
		arguments,
		arguments,
		Error | Abort,
		start,
		end);
}
/*
 * Given the current configuration, answers which category the problem
 * falls into:
 *		Error | Warning | Ignore
 */
public int computeSeverity(int problemId){

	// severity can have been preset on the problem
//	if ((problem.severity & Fatal) != 0){
//		return Error;
//	}

	// if not then check whether it is a configurable problem
	int errorThreshold = this.options.errorThreshold;
	int warningThreshold = this.options.warningThreshold;
	
	switch(problemId){

		case IProblem.UnreachableCatch :
		case IProblem.CodeCannotBeReached :
			if ((errorThreshold & CompilerOptions.UnreachableCode) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnreachableCode) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.MaskedCatch : 
			if ((errorThreshold & CompilerOptions.MaskedCatchBlock) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.MaskedCatchBlock) != 0){
				return Warning;
			}
			return Ignore;
			
/*
		case Never Used  :
			if ((errorThreshold & ParsingOptionalError) != 0){
				return Error;
			}
			if ((warningThreshold & ParsingOptionalError) != 0){
				return Warning;
			}
			return Ignore;
*/
		case IProblem.ImportNotFound :
		case IProblem.ImportNotVisible :
		case IProblem.ImportAmbiguous :
		case IProblem.ImportInternalNameProvided :
		case IProblem.ImportInheritedNameHidesEnclosingName :
		case IProblem.DuplicateImport :
		case IProblem.ConflictingImport :
		case IProblem.CannotImportPackage :
			if ((errorThreshold & CompilerOptions.ImportProblem) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.ImportProblem) != 0){
				return Warning;
			}
			return Ignore;
			
		case IProblem.UnusedImport :
			// if import problem are disabled, then ignore
			if ((errorThreshold & CompilerOptions.ImportProblem) == 0 
				&& (warningThreshold & CompilerOptions.ImportProblem) == 0){
				return Ignore;
			}
			if ((errorThreshold & CompilerOptions.UnusedImport) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedImport) != 0){
				return Warning;
			}
			return Ignore;
			
		case IProblem.MethodButWithConstructorName :
			if ((errorThreshold & CompilerOptions.MethodWithConstructorName) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.MethodWithConstructorName) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.OverridingNonVisibleMethod :
			if ((errorThreshold & CompilerOptions.OverriddenPackageDefaultMethod) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.OverriddenPackageDefaultMethod) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod :
		case IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod :
			if ((errorThreshold & CompilerOptions.IncompatibleNonInheritedInterfaceMethod) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.IncompatibleNonInheritedInterfaceMethod) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.OverridingDeprecatedMethod :				
		case IProblem.UsingDeprecatedType :				
		case IProblem.UsingDeprecatedMethod :
		case IProblem.UsingDeprecatedConstructor :
		case IProblem.UsingDeprecatedField :
			if ((errorThreshold & CompilerOptions.UsingDeprecatedAPI) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UsingDeprecatedAPI) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.LocalVariableIsNeverUsed :
			if ((errorThreshold & CompilerOptions.UnusedLocalVariable) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedLocalVariable) != 0){
				return Warning;
			}
			return Ignore;
		
		case IProblem.ArgumentIsNeverUsed :
			if ((errorThreshold & CompilerOptions.UnusedArgument) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedArgument) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.NoImplicitStringConversionForCharArrayExpression :
			if ((errorThreshold & CompilerOptions.NoImplicitStringConversion) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.NoImplicitStringConversion) != 0){
				return Warning;
			}
			return Ignore;

		case IProblem.NeedToEmulateFieldReadAccess :
		case IProblem.NeedToEmulateFieldWriteAccess :
		case IProblem.NeedToEmulateMethodAccess :
		case IProblem.NeedToEmulateConstructorAccess :			
			if ((errorThreshold & CompilerOptions.AccessEmulation) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.AccessEmulation) != 0){
				return Warning;
			}
			return Ignore;
		case IProblem.NonExternalizedStringLiteral :
			if ((errorThreshold & CompilerOptions.NonExternalizedString) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.NonExternalizedString) != 0){
				return Warning;
			}
			return Ignore;
		case IProblem.UseAssertAsAnIdentifier :
			if ((errorThreshold & CompilerOptions.AssertUsedAsAnIdentifier) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.AssertUsedAsAnIdentifier) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.NonStaticAccessToStaticMethod :
		case IProblem.NonStaticAccessToStaticField :
			if ((errorThreshold & CompilerOptions.StaticAccessReceiver) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.StaticAccessReceiver) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.AssignmentHasNoEffect:
			if ((errorThreshold & CompilerOptions.NoEffectAssignment) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.NoEffectAssignment) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.UnusedPrivateConstructor:
		case IProblem.UnusedPrivateMethod:
		case IProblem.UnusedPrivateField:
		case IProblem.UnusedPrivateType:
			if ((errorThreshold & CompilerOptions.UnusedPrivateMember) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.UnusedPrivateMember) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.Task :
			return Warning;			
		case IProblem.LocalVariableHidingLocalVariable:
		case IProblem.LocalVariableHidingField:
		case IProblem.ArgumentHidingLocalVariable:
		case IProblem.ArgumentHidingField:
			if ((errorThreshold & CompilerOptions.LocalVariableHiding) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.LocalVariableHiding) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.FieldHidingLocalVariable:
		case IProblem.FieldHidingField:
			if ((errorThreshold & CompilerOptions.FieldHiding) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.FieldHiding) != 0){
				return Warning;
			}
			return Ignore;		
		case IProblem.PossibleAccidentalBooleanAssignment:
			if ((errorThreshold & CompilerOptions.AccidentalBooleanAssign) != 0){
				return Error;
			}
			if ((warningThreshold & CompilerOptions.AccidentalBooleanAssign) != 0){
				return Warning;
			}
			return Ignore;		
		default:
			return Error;
	}
}
public void conditionalArgumentsIncompatibleTypes(ConditionalExpression expression, TypeBinding trueType, TypeBinding falseType) {
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {new String(trueType.readableName()), new String(falseType.readableName())},
		new String[] {new String(trueType.sourceName()), new String(falseType.sourceName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void conflictingImport(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.ConflictingImport,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void constantOutOfFormat(NumberLiteral lit) {
	// the literal is not in a correct format
	// this code is called on IntLiteral and LongLiteral 
	// example 000811 ...the 8 is uncorrect.

	if ((lit instanceof LongLiteral) || (lit instanceof IntLiteral)) {
		char[] source = lit.source();
		try {
			final String Radix;
			final int radix;
			if ((source[1] == 'x') || (source[1] == 'X')) {
				radix = 16;
				Radix = "Hexa"; //$NON-NLS-1$
			} else {
				radix = 8;
				Radix = "Octal"; //$NON-NLS-1$
			}
			//look for the first digit that is incorrect
			int place = -1;
			label : for (int i = radix == 8 ? 1 : 2; i < source.length; i++) {
				if (Character.digit(source[i], radix) == -1) {
					place = i;
					break label;
				}
			}
			String[] arguments = new String[] {Radix + " " + new String(source) + " (digit " + new String(new char[] {source[place]}) + ")"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			this.handle(
				IProblem.NumericValueOutOfRange,
				arguments,
				arguments,
				lit.sourceStart,
				lit.sourceEnd);
			return;
		} catch (IndexOutOfBoundsException ex) {}
	
		// just in case .... use a predefined error..
		// we should never come here...(except if the code changes !)
		this.constantOutOfRange(lit);
	}
}
public void constantOutOfRange(Literal lit) {
	// lit is some how out of range of it declared type
	// example 9999999999999999999999999999999999999999999999999999999999999999999
	String[] arguments = new String[] {new String(lit.source())};
	this.handle(
		IProblem.NumericValueOutOfRange,
		arguments,
		arguments,
		lit.sourceStart,
		lit.sourceEnd);
}
public void deprecatedField(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.UsingDeprecatedField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void deprecatedMethod(MethodBinding method, AstNode location) {
	if (method.isConstructor())
		this.handle(
			IProblem.UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), parametersAsString(method)},
			new String[] {new String(method.declaringClass.shortReadableName()), parametersAsShortString(method)},
			location.sourceStart,
			location.sourceEnd);
	else
		this.handle(
			IProblem.UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
			new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsShortString(method)},
			location.sourceStart,
			location.sourceEnd);
}
public void deprecatedType(TypeBinding type, AstNode location) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	this.handle(
		IProblem.UsingDeprecatedType,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateCase(AstNode statement, Constant constant) {
	String[] arguments = new String[] {String.valueOf(constant.intValue())};
	this.handle(
		IProblem.DuplicateCase,
		arguments,
		arguments,
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateDefaultCase(AstNode statement) {
	this.handle(
		IProblem.DuplicateDefaultCase,
		NoArgument,
		NoArgument,
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateFieldInType(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.DuplicateField,
		new String[] {new String(type.sourceName()), fieldDecl.name()},
		new String[] {new String(type.shortReadableName()), fieldDecl.name()},
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateImport(ImportReference importRef) {
	String[] arguments = new String[] {CharOperation.toString(importRef.tokens)};
	this.handle(
		IProblem.DuplicateImport,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd);
}
public void duplicateInitializationOfBlankFinalField(FieldBinding field, Reference reference) {
	String[] arguments = new String[]{ new String(field.readableName())};
	this.handle(
		IProblem.DuplicateBlankFinalFieldInitialization,
		arguments,
		arguments,
		reference.sourceStart,
		reference.sourceEnd);
}
public void duplicateInitializationOfFinalLocal(LocalVariableBinding local, AstNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.DuplicateFinalLocalInitialization,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateMethodInType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(methodDecl.selector), new String(type.sourceName())};
	this.handle(
		IProblem.DuplicateMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void duplicateModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
/* to highlight modifiers use:
	this.handle(
		new Problem(
			DuplicateModifierForField,
			new String[] {fieldDecl.name()},
			fieldDecl.modifiers.sourceStart,
			fieldDecl.modifiers.sourceEnd));
*/
	String[] arguments = new String[] {fieldDecl.name()};
	this.handle(
		IProblem.DuplicateModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void duplicateModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.DuplicateModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		new String[] {new String(type.shortReadableName()), new String(methodDecl.selector)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void duplicateModifierForType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.DuplicateModifierForType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateModifierForVariable(LocalDeclaration localDecl, boolean complainForArgument) {
	String[] arguments = new String[] {localDecl.name()};
	this.handle(
		complainForArgument
			? IProblem.DuplicateModifierForArgument 
			: IProblem.DuplicateModifierForVariable,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void duplicateNestedType(TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(typeDecl.name)};
	this.handle(
		IProblem.DuplicateNestedType,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void duplicateSuperinterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		IProblem.DuplicateSuperInterface,
		new String[] {
			new String(superType.readableName()),
			new String(type.sourceName())},
		new String[] {
			new String(superType.shortReadableName()),
			new String(type.sourceName())},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void duplicateTypes(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		IProblem.DuplicateTypes,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void errorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
	StringBuffer buffer = new StringBuffer();
	StringBuffer shortBuffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0){
			buffer.append(", "); //$NON-NLS-1$
			shortBuffer.append(", "); //$NON-NLS-1$
		}
		buffer.append(new String(params[i].readableName()));
		shortBuffer.append(new String(params[i].shortReadableName()));
	}

	this.handle(
		recType.isArrayType() ? IProblem.NoMessageSendOnArrayType : IProblem.NoMessageSendOnBaseType,
		new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
		new String[] {new String(recType.shortReadableName()), new String(messageSend.selector), shortBuffer.toString()},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void errorThisSuperInStatic(AstNode reference) {
	String[] arguments = new String[] {reference.isSuper() ? "super" : "this"}; //$NON-NLS-2$ //$NON-NLS-1$
	this.handle(
		IProblem.ThisInStaticContext,
		arguments,
		arguments,
		reference.sourceStart,
		reference.sourceEnd);
}
public void exceptionTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ExceptionTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ExceptionTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ExceptionTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ExceptionTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ExceptionTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
		new String[] {new String(methodDecl.selector), new String(expectedType.shortReadableName())},
		exceptionType.sourceStart,
		exceptionType.sourceEnd);
}
public void expressionShouldBeAVariable(Expression expression) {
	this.handle(
		IProblem.ExpressionShouldBeAVariable,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void fieldHiding(FieldDeclaration fieldDecl, Binding otherVariable) {
	FieldBinding field = fieldDecl.binding;
	if (otherVariable instanceof LocalVariableBinding) {
		this.handle(
			IProblem.FieldHidingLocalVariable,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) },
			fieldDecl.sourceStart,
			fieldDecl.sourceEnd);
	} else if (otherVariable instanceof FieldBinding) {
		FieldBinding otherField = (FieldBinding) otherVariable;
		this.handle(
			IProblem.FieldHidingField,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) , new String(otherField.declaringClass.readableName())  },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) , new String(otherField.declaringClass.shortReadableName()) },
			fieldDecl.sourceStart,
			fieldDecl.sourceEnd);
	}
}

public void fieldsOrThisBeforeConstructorInvocation(ThisReference reference) {
	this.handle(
		IProblem.ThisSuperDuringConstructorInvocation,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void fieldTypeProblem(SourceTypeBinding type, FieldDeclaration fieldDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.FieldTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.FieldTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.FieldTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.FieldTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.FieldTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {fieldDecl.name(), new String(type.sourceName()), new String(expectedType.readableName())},
		new String[] {fieldDecl.name(), new String(type.sourceName()), new String(expectedType.shortReadableName())},
		fieldDecl.type.sourceStart,
		fieldDecl.type.sourceEnd);
}
public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		// Cannot override the final method from %1
		// 8.4.3.3 - Final methods cannot be overridden or hidden.
		IProblem.FinalMethodCannotBeOverridden,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void forwardReference(Reference reference, int indexInQualification, TypeBinding type) {
	this.handle(
		IProblem.ReferenceToForwardField,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments

private void handle(
	int problemId, 
	String[] problemArguments,
	String[] messageArguments,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			messageArguments,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			referenceContext == null ? null : referenceContext.compilationResult()); 
	referenceContext = null;
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments

private void handle(
	int problemId, 
	String[] problemArguments,
	String[] messageArguments,
	int severity,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
			messageArguments,
			severity,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			referenceContext == null ? null : referenceContext.compilationResult()); 
	referenceContext = null;
}
// use this private API when the compilation unit result cannot be found through the
// reference context. 

private void handle(
	int problemId, 
	String[] problemArguments,
	String[] messageArguments,
	int problemStartPosition, 
	int problemEndPosition,
	CompilationResult unitResult){

	this.handle(
			problemId,
			problemArguments,
			messageArguments,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			unitResult); 
	referenceContext = null;
}
public void hidingEnclosingType(TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(typeDecl.name)};
	this.handle(
		IProblem.HidingEnclosingType,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void hierarchyCircularity(SourceTypeBinding sourceType, ReferenceBinding superType, TypeReference reference) {
	int start = 0;
	int end = 0;
	String typeName = ""; //$NON-NLS-1$
	String shortTypeName = ""; //$NON-NLS-1$

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
		typeName = new String(superType.readableName());
		shortTypeName = new String(superType.sourceName());
	} else {
		start = reference.sourceStart;
		end = reference.sourceEnd;
		char[][] qName = reference.getTypeName();
		typeName = CharOperation.toString(qName);
		shortTypeName = new String(qName[qName.length-1]);
	}

	if (sourceType == superType)
		this.handle(
			IProblem.HierarchyCircularitySelfReference,
			new String[] {new String(sourceType.sourceName()), typeName},
			new String[] {new String(sourceType.sourceName()), shortTypeName},
			start,
			end);
	else
		this.handle(
			IProblem.HierarchyCircularity,
			new String[] {new String(sourceType.sourceName()), typeName},
			new String[] {new String(sourceType.sourceName()), shortTypeName},
			start,
			end);
}
public void hierarchyHasProblems(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.HierarchyHasProblems,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalAbstractModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalAbstractModifierCombinationForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierCombinationFinalAbstractForClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierCombinationFinalAbstractForClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierCombinationFinalVolatileForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {fieldDecl.name()};

	this.handle(
		IProblem.IllegalModifierCombinationFinalVolatileForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}

public void illegalModifierForClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {fieldDecl.name()};
	this.handle(
		IProblem.IllegalModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterface(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForInterface,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForInterfaceField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {fieldDecl.name()};
	this.handle(
		IProblem.IllegalModifierForInterfaceField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterfaceMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalModifierForInterfaceMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForLocalClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForLocalClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberClass(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberClass,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberInterface(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberInterface,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalModifierForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForVariable(LocalDeclaration localDecl, boolean complainAsArgument) {
	String[] arguments = new String[] {localDecl.name()};
	this.handle(
		complainAsArgument
			? IProblem.IllegalModifierForArgument
			: IProblem.IllegalModifierForVariable,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void illegalPrimitiveOrArrayTypeForEnclosingInstance(TypeBinding enclosingType, AstNode location) {
	this.handle(
		IProblem.IllegalPrimitiveOrArrayTypeForEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		new String[] {new String(enclosingType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalStaticModifierForMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalStaticModifierForMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name())};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalVisibilityModifierCombinationForMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalVisibilityModifierCombinationForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalVisibilityModifierForInterfaceMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalVisibilityModifierForInterfaceMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVoidExpression(AstNode location) {
	this.handle(
		IProblem.InvalidVoidExpression,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void importProblem(ImportReference importRef, Binding expectedImport) {
	int problemId = expectedImport.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ImportNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ImportNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ImportAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ImportInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ImportInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	String argument;
	if(expectedImport instanceof ProblemReferenceBinding) {
		argument = CharOperation.toString(((ProblemReferenceBinding)expectedImport).compoundName);
	} else {
		argument = CharOperation.toString(importRef.tokens);
	}
	String[] arguments = new String[]{argument};
	this.handle(id, arguments, arguments, importRef.sourceStart, importRef.sourceEnd);
}
public void incompatibleExceptionInThrowsClause(SourceTypeBinding type, MethodBinding currentMethod, MethodBinding inheritedMethod, ReferenceBinding exceptionType) {
	if (type == currentMethod.declaringClass) {
		int id;
		if (currentMethod.declaringClass.isInterface() 
				&& !inheritedMethod.isPublic()){ // interface inheriting Object protected method
			id = IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod;
		} else {
			id = IProblem.IncompatibleExceptionInThrowsClause;
		}
		this.handle(
			// Exception %1 is not compatible with throws clause in %2
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			id,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.shortReadableName(),
						inheritedMethod.shortReadableName(),
						'.'))},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	} else	
		this.handle(
			// Exception %1 in throws clause of %2 is not compatible with %3
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IProblem.IncompatibleExceptionInInheritedMethodThrowsClause,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						currentMethod.declaringClass.sourceName(),
						currentMethod.readableName(),
						'.')),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						currentMethod.declaringClass.sourceName(),
						currentMethod.shortReadableName(),
						'.')),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.shortReadableName(),
						inheritedMethod.shortReadableName(),
						'.'))},
			type.sourceStart(),
			type.sourceEnd());
}
public void incompatibleReturnType(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	StringBuffer methodSignature = new StringBuffer();
	methodSignature
		.append(inheritedMethod.declaringClass.readableName())
		.append('.')
		.append(inheritedMethod.readableName());

	StringBuffer shortSignature = new StringBuffer();
	shortSignature
		.append(inheritedMethod.declaringClass.shortReadableName())
		.append('.')
		.append(inheritedMethod.shortReadableName());

	int id;
	if (currentMethod.declaringClass.isInterface() 
			&& !inheritedMethod.isPublic()){ // interface inheriting Object protected method
		id = IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod;
	} else {
		id = IProblem.IncompatibleReturnType;
	}
	this.handle(
		id,
		new String[] {methodSignature.toString()},
		new String[] {shortSignature.toString()},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void incorrectLocationForEmptyDimension(ArrayAllocationExpression expression, int index) {
	this.handle(
		IProblem.IllegalDimension,
		NoArgument,
		NoArgument,
		expression.dimensions[index + 1].sourceStart,
		expression.dimensions[index + 1].sourceEnd);
}
public void incorrectSwitchType(Expression expression, TypeBinding testType) {
	this.handle(
		IProblem.IncorrectSwitchType,
		new String[] {new String(testType.readableName())},
		new String[] {new String(testType.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	StringBuffer concreteSignature = new StringBuffer();
	concreteSignature
		.append(concreteMethod.declaringClass.readableName())
		.append('.')
		.append(concreteMethod.readableName());
	StringBuffer shortSignature = new StringBuffer();
	shortSignature
		.append(concreteMethod.declaringClass.shortReadableName())
		.append('.')
		.append(concreteMethod.shortReadableName());
	this.handle(
		// The inherited method %1 cannot hide the public abstract method in %2
		IProblem.InheritedMethodReducesVisibility,
		new String[] {
			new String(concreteSignature.toString()),
			new String(abstractMethods[0].declaringClass.readableName())},
		new String[] {
			new String(shortSignature.toString()),
			new String(abstractMethods[0].declaringClass.shortReadableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void inheritedMethodsHaveIncompatibleReturnTypes(SourceTypeBinding type, MethodBinding[] inheritedMethods, int length) {
	StringBuffer methodSignatures = new StringBuffer();
	StringBuffer shortSignatures = new StringBuffer();
	for (int i = length; --i >= 0;) {
		methodSignatures
			.append(inheritedMethods[i].declaringClass.readableName())
			.append('.')
			.append(inheritedMethods[i].readableName());
		shortSignatures
			.append(inheritedMethods[i].declaringClass.shortReadableName())
			.append('.')
			.append(inheritedMethods[i].shortReadableName());
		if (i != 0){
			methodSignatures.append(", "); //$NON-NLS-1$
			shortSignatures.append(", "); //$NON-NLS-1$
		}
	}

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IProblem.IncompatibleReturnType,
		new String[] {methodSignatures.toString()},
		new String[] {shortSignatures.toString()},
		type.sourceStart(),
		type.sourceEnd());
}
public void initializerMustCompleteNormally(FieldDeclaration fieldDecl) {
	this.handle(
		IProblem.InitializerMustCompleteNormally,
		NoArgument,
		NoArgument,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, AstNode location) {
	this.handle(
		IProblem.CannotDefineStaticInitializerInLocalType,
		new String[] {new String(innerType.readableName())},
		new String[] {new String(innerType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void interfaceCannotHaveConstructors(ConstructorDeclaration constructor) {
	this.handle(
		IProblem.InterfaceCannotHaveConstructors,
		NoArgument,
		NoArgument,
		constructor.sourceStart,
		constructor.sourceEnd,
		constructor,
		constructor.compilationResult());
}
public void interfaceCannotHaveInitializers(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(type.sourceName())};

	this.handle(
		IProblem.InterfaceCannotHaveInitializers,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void invalidBreak(AstNode location) {
	this.handle(
		IProblem.InvalidBreak,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void invalidConstructor(Statement statement, MethodBinding targetConstructor) {

	boolean insideDefaultConstructor = 
		(referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(statement instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) statement).accessMode == ExplicitConstructorCall.ImplicitSuper);

	int flag = IProblem.UndefinedConstructor; //default...
	switch (targetConstructor.problemId()) {
		case NotFound :
			if (insideDefaultConstructor){
				flag = IProblem.UndefinedConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.UndefinedConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.UndefinedConstructor;
			}
			break;
		case NotVisible :
			if (insideDefaultConstructor){
				flag = IProblem.NotVisibleConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.NotVisibleConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.NotVisibleConstructor;
			}
			break;
		case Ambiguous :
			if (insideDefaultConstructor){
				flag = IProblem.AmbiguousConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				flag = IProblem.AmbiguousConstructorInImplicitConstructorCall;
			} else {
				flag = IProblem.AmbiguousConstructor;
			}
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	
	this.handle(
		flag,
		new String[] {new String(targetConstructor.declaringClass.readableName()), parametersAsString(targetConstructor)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), parametersAsShortString(targetConstructor)},
		statement.sourceStart,
		statement.sourceEnd);
}
public void invalidContinue(AstNode location) {
	this.handle(
		IProblem.InvalidContinue,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void invalidEnclosingType(Expression expression, TypeBinding type, ReferenceBinding enclosingType) {

	if (enclosingType.isAnonymousType()) enclosingType = enclosingType.superclass();
	int flag = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case NotFound : // 1
			flag = IProblem.UndefinedType;
			break;
		case NotVisible : // 2
			flag = IProblem.NotVisibleType;
			break;
		case Ambiguous : // 3
			flag = IProblem.AmbiguousType;
			break;
		case InternalNameProvided :
			flag = IProblem.InternalTypeNameProvided;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(enclosingType.readableName()) + "." + new String(type.readableName())}, //$NON-NLS-1$
		new String[] {new String(enclosingType.shortReadableName()) + "." + new String(type.shortReadableName())}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidExpressionAsStatement(Expression expression){
	this.handle(
		IProblem.InvalidExpressionAsStatement,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidField(FieldReference fieldRef, TypeBinding searchedType) {
	int severity = Error;
	int flag = IProblem.UndefinedField;
	FieldBinding field = fieldRef.binding;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(searchedType.leafComponentType().readableName())},
				new String[] {new String(searchedType.leafComponentType().shortReadableName())},
				fieldRef.receiver.sourceStart,
				fieldRef.receiver.sourceEnd);
			return;
			
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		flag,
		arguments,
		arguments,
		severity,
		fieldRef.sourceStart,
		fieldRef.sourceEnd);
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	int flag = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(field.declaringClass.leafComponentType().readableName())},
				new String[] {new String(field.declaringClass.leafComponentType().shortReadableName())},
				nameRef.sourceStart,
				nameRef.sourceEnd);
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		flag,
		arguments,
		arguments,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void invalidField(QualifiedNameReference nameRef, FieldBinding field, int index, TypeBinding searchedType) {
	//the resolution of the index-th field of qname failed
	//qname.otherBindings[index] is the binding that has produced the error

	//The different targetted errors should be :
	//UndefinedField
	//NotVisibleField
	//AmbiguousField

	if (searchedType.isBaseType()) {
		this.handle(
			IProblem.NoFieldOnBaseType,
			new String[] {
				new String(searchedType.readableName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			new String[] {
				new String(searchedType.sourceName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			nameRef.sourceStart,
			nameRef.sourceEnd);
		return;
	}

	int flag = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(searchedType.leafComponentType().readableName())},
				new String[] {new String(searchedType.leafComponentType().shortReadableName())},
				nameRef.sourceStart,
				nameRef.sourceEnd);
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	String[] arguments = new String[] {CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index + 1))};
	this.handle(
		flag, 
		arguments,
		arguments,
		nameRef.sourceStart, 
		nameRef.sourceEnd); 
}
public void invalidMethod(MessageSend messageSend, MethodBinding method) {
	// CODE should be UPDATED according to error coding in the different method binding errors
	// The different targetted errors should be :
	// 	UndefinedMethod
	//	NotVisibleMethod
	//	AmbiguousMethod
	//  InheritedNameHidesEnclosingName
	//	InstanceMethodDuringConstructorInvocation
	// StaticMethodRequested

	int flag = IProblem.UndefinedMethod; //default...
	switch (method.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedMethod;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleMethod;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousMethod;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedMethodHidesEnclosingName;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = IProblem.InstanceMethodDuringConstructorInvocation;
			break;
		case NonStaticReferenceInStaticContext :
			flag = IProblem.StaticMethodRequested;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,
				new String[] {new String(method.declaringClass.leafComponentType().readableName())},
				new String[] {new String(method.declaringClass.leafComponentType().shortReadableName())},
				messageSend.receiver.sourceStart,
				messageSend.receiver.sourceEnd);
			return;
		
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	if (flag == IProblem.UndefinedMethod) {
		ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
		if (problemMethod.closestMatch != null) {
				String closestParameterTypeNames = parametersAsString(problemMethod.closestMatch);
				String parameterTypeNames = parametersAsString(method);
				String closestParameterTypeShortNames = parametersAsShortString(problemMethod.closestMatch);
				String parameterTypeShortNames = parametersAsShortString(method);
				if (closestParameterTypeShortNames.equals(parameterTypeShortNames)){
					closestParameterTypeShortNames = closestParameterTypeNames;
					parameterTypeShortNames = parameterTypeNames;
				}
				this.handle(
					IProblem.ParameterMismatch,
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.readableName()),
						new String(problemMethod.closestMatch.selector),
						closestParameterTypeNames,
						parameterTypeNames 
					},
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.shortReadableName()),
						new String(problemMethod.closestMatch.selector),
						closestParameterTypeShortNames,
						parameterTypeShortNames
					},
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);
				return;
		}
	}

	this.handle(
		flag,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector), parametersAsString(method)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector), parametersAsShortString(method)},
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void invalidNullToSynchronize(Expression expression) {
	this.handle(
		IProblem.InvalidNullToSynchronized,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(BinaryExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			expression.operatorToString(),
			leftName + ", " + rightName}, //$NON-NLS-1$
		new String[] {
			expression.operatorToString(),
			leftShortName + ", " + rightShortName}, //$NON-NLS-1$
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(CompoundAssignment assign, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.InvalidOperator,
		new String[] {
			assign.operatorToString(),
			leftName + ", " + rightName}, //$NON-NLS-1$
		new String[] {
			assign.operatorToString(),
			leftShortName + ", " + rightShortName}, //$NON-NLS-1$
		assign.sourceStart,
		assign.sourceEnd);
}
public void invalidOperator(UnaryExpression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidOperator,
		new String[] {expression.operatorToString(), new String(type.readableName())},
		new String[] {expression.operatorToString(), new String(type.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidParenthesizedExpression(AstNode reference) {
	this.handle(
		IProblem.InvalidParenthesizedExpression,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void invalidSuperclass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.SuperclassNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.SuperclassNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.SuperclassAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.SuperclassInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.SuperclassInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
		new String[] {new String(expectedType.shortReadableName()), new String(type.sourceName())},
		superclassRef.sourceStart,
		superclassRef.sourceEnd);
}
public void invalidSuperinterface(SourceTypeBinding type, TypeReference superinterfaceRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.InterfaceNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.InterfaceNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.InterfaceAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.InterfaceInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.InterfaceInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
		this.handle(
			id,
			new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
			new String[] {new String(expectedType.shortReadableName()), new String(type.sourceName())},
			superinterfaceRef.sourceStart,
			superinterfaceRef.sourceEnd);
}
public void invalidType(AstNode location, TypeBinding type) {
	int flag = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case NotFound :
			flag = IProblem.UndefinedType;
			break;
		case NotVisible :
			flag = IProblem.NotVisibleType;
			break;
		case Ambiguous :
			flag = IProblem.AmbiguousType;
			break;
		case InternalNameProvided :
			flag = IProblem.InternalTypeNameProvided;
			break;
		case InheritedNameHidesEnclosingName :
			flag = IProblem.InheritedTypeHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidTypeReference(Expression expression) {
	this.handle(
		IProblem.InvalidTypeExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeToSynchronize(Expression expression, TypeBinding type) {
	this.handle(
		IProblem.InvalidTypeToSynchronized,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidUnaryExpression(Expression expression) {
	this.handle(
		IProblem.InvalidUnaryExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void isClassPathCorrect(char[][] wellKnownTypeName, CompilationUnitDeclaration compUnitDecl) {
	referenceContext = compUnitDecl;
	String[] arguments = new String[] {CharOperation.toString(wellKnownTypeName)};
	this.handle(
		IProblem.IsClassPathCorrect,
		arguments, 
		arguments,
		AbortCompilation | Error,
		compUnitDecl == null ? 0 : compUnitDecl.sourceStart,
		compUnitDecl == null ? 1 : compUnitDecl.sourceEnd);
}
public void localVariableHiding(LocalDeclaration local, Binding otherVariable, boolean  isSpecialArgHidingField) {
	if (otherVariable instanceof LocalVariableBinding) {
		String[] arguments = new String[] {new String(local.name)  };
		this.handle(
			(local instanceof Argument) 
				? IProblem.ArgumentHidingLocalVariable 
				: IProblem.LocalVariableHidingLocalVariable,
			arguments,
			arguments,
			local.sourceStart,
			local.sourceEnd);
	} else if (otherVariable instanceof FieldBinding) {
		if (isSpecialArgHidingField && !this.options.reportSpecialParameterHidingField){
			return;
		}
		FieldBinding field = (FieldBinding) otherVariable;
		this.handle(
			(local instanceof Argument)
				? IProblem.ArgumentHidingField
				: IProblem.LocalVariableHidingField,
			new String[] {new String(local.name) , new String(field.declaringClass.readableName()) },
			new String[] {new String(local.name), new String(field.declaringClass.shortReadableName()) },
			local.sourceStart,
			local.sourceEnd);
	}
}
public void maskedExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		IProblem.MaskedCatch,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void methodNeedingAbstractModifier(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodRequiresBody,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodNeedingNoBody(MethodDeclaration methodDecl) {
	this.handle(
		((methodDecl.modifiers & CompilerModifiers.AccNative) != 0) ? IProblem.BodyForNativeMethod : IProblem.BodyForAbstractMethod,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodWithConstructorName(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodButWithConstructorName,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
//public void missingEnclosingInstanceSpecification(ReferenceBinding enclosingType, AstNode location) {
//	boolean insideConstructorCall =
//		(location instanceof ExplicitConstructorCall)
//			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);
//
//	this.handle(
//		insideConstructorCall
//			? IProblem.MissingEnclosingInstanceForConstructorCall
//			: IProblem.MissingEnclosingInstance,
//		new String[] {new String(enclosingType.readableName())},
//		new String[] {new String(enclosingType.shortReadableName())},
//		location.sourceStart,
//		location.sourceEnd);
//}
public void missingReturnType(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.MissingReturnType,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void missingSemiColon(Expression expression){
	this.handle(
		IProblem.MissingSemiColon,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void mustDefineDimensionsOrInitializer(ArrayAllocationExpression expression) {
	this.handle(
		IProblem.MustDefineEitherDimensionExpressionsOrInitializer,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void mustSpecifyPackage(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {new String(compUnitDecl.getFileName())};
	this.handle(
		IProblem.MustSpecifyPackage,
		arguments,
		arguments,
		compUnitDecl.sourceStart,
		compUnitDecl.sourceStart + 1);	
}
public void mustUseAStaticMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		IProblem.StaticMethodRequested,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsShortString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}

public void nativeMethodsCannotBeStrictfp(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.NativeMethodsCannotBeStrictfp,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void needImplementation() {
	this.abortDueToInternalError(Util.bind("abort.missingCode")); //$NON-NLS-1$
}
public void needToEmulateFieldReadAccess(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.NeedToEmulateFieldReadAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateFieldWriteAccess(FieldBinding field, AstNode location) {
	this.handle(
		IProblem.NeedToEmulateFieldWriteAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateMethodAccess(
	MethodBinding method, 
	AstNode location) {

	if (method.isConstructor())
		this.handle(
			IProblem.NeedToEmulateConstructorAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				parametersAsString(method)
			 }, 
			new String[] {
				new String(method.declaringClass.shortReadableName()), 
				parametersAsShortString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
	else
		this.handle(
			IProblem.NeedToEmulateMethodAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				new String(method.selector), 
				parametersAsString(method)
			 }, 
			new String[] {
				new String(method.declaringClass.shortReadableName()), 
				new String(method.selector), 
				parametersAsShortString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
}
public void nestedClassCannotDeclareInterface(TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(typeDecl.name)};
	this.handle(
		IProblem.CannotDefineInterfaceInLocalType,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void noMoreAvailableSpaceForArgument(LocalVariableBinding local, AstNode location) {
	String[] arguments = new String[]{ new String(local.name) };
	this.handle(
		local instanceof SyntheticArgumentBinding
			? IProblem.TooManySyntheticArgumentSlots
			: IProblem.TooManyArgumentSlots,
		arguments,
		arguments,
		Abort | Error,
		location.sourceStart,
		location.sourceEnd);
}
public void noMoreAvailableSpaceForLocal(LocalVariableBinding local, AstNode location) {
	String[] arguments = new String[]{ new String(local.name) };
	this.handle(
		IProblem.TooManyLocalVariableSlots,
		arguments,
		arguments,
		Abort | Error,
		location.sourceStart,
		location.sourceEnd);
}
public void noSuchEnclosingInstance(TypeBinding targetType, AstNode location, boolean isConstructorCall) {

	int id;

	if (isConstructorCall) {
		//28 = No enclosing instance of type {0} is available due to some intermediate constructor invocation
		id = IProblem.EnclosingInstanceInConstructorCall;
	} else if ((location instanceof ExplicitConstructorCall)
				&& ((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper) {
		//20 = No enclosing instance of type {0} is accessible to invoke the super constructor. Must define a constructor and explicitly qualify its super constructor invocation with an instance of {0} (e.g. x.super() where x is an instance of {0}).
		id = IProblem.MissingEnclosingInstanceForConstructorCall;
	} else if (location instanceof AllocationExpression 
				&& (((AllocationExpression) location).binding.declaringClass.isMemberType()
					|| (((AllocationExpression) location).binding.declaringClass.isAnonymousType() 
						&& ((AllocationExpression) location).binding.declaringClass.superclass().isMemberType()))) {
		//21 = No enclosing instance of type {0} is accessible. Must qualify the allocation with an enclosing instance of type {0} (e.g. x.new A() where x is an instance of {0}).
		id = IProblem.MissingEnclosingInstance;
	} else { // default
		//22 = No enclosing instance of the type {0} is accessible in scope
		id = IProblem.IncorrectEnclosingInstanceReference;
	}

	this.handle(
		id,
		new String[] { new String(targetType.readableName())}, 
		new String[] { new String(targetType.shortReadableName())}, 
		location.sourceStart, 
		location.sourceEnd); 
}
public void notCompatibleTypesError(EqualExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInEqualityOperator,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void notCompatibleTypesError(InstanceOfExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInConditionalOperator,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void objectCannotHaveSuperTypes(SourceTypeBinding type) {
	this.handle(
		IProblem.ObjectCannotHaveSuperTypes,
		NoArgument,
		NoArgument,
		type.sourceStart(),
		type.sourceEnd());
}
public void operatorOnlyValidOnNumericType(CompoundAssignment  assignment, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.TypeMismatch,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void overridesDeprecatedMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingDeprecatedMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.shortReadableName(),
						localMethod.shortReadableName(),
						'.')),
			new String(inheritedMethod.declaringClass.shortReadableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.OverridingNonVisibleMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.shortReadableName(),
						localMethod.shortReadableName(),
						'.')),
			new String(inheritedMethod.declaringClass.shortReadableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void packageCollidesWithType(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {CharOperation.toString(compUnitDecl.currentPackage.tokens)};
	this.handle(
		IProblem.PackageCollidesWithType,
		arguments,
		arguments,
		compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage.sourceEnd);
}
public void packageIsNotExpectedPackage(CompilationUnitDeclaration compUnitDecl) {
	String[] arguments = new String[] {CharOperation.toString(compUnitDecl.compilationResult.compilationUnit.getPackageName())};
	this.handle(
		IProblem.PackageIsNotExpectedPackage,
		arguments,
		arguments,
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage == null ? 0 : compUnitDecl.currentPackage.sourceEnd);
}
private String parametersAsString(MethodBinding method) {
	TypeBinding[] params = method.parameters;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		buffer.append(new String(params[i].readableName()));
	}
	return buffer.toString();
}
private String parametersAsShortString(MethodBinding method) {
	TypeBinding[] params = method.parameters;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		buffer.append(new String(params[i].shortReadableName()));
	}
	return buffer.toString();
}
public void parseError(
	int startPosition, 
	int endPosition, 
	char[] currentTokenSource, 
	String errorTokenName, 
	String[] possibleTokens) {
		
	if (possibleTokens.length == 0) { //no suggestion available
		if (isKeyword(currentTokenSource)) {
			String[] arguments = new String[] {new String(currentTokenSource)};
			this.handle(
				IProblem.ParsingErrorOnKeywordNoSuggestion,
				arguments,
				arguments,
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		} else {
			String[] arguments = new String[] {errorTokenName};
			this.handle(
				IProblem.ParsingErrorNoSuggestion,
				arguments,
				arguments,
				// this is the current -invalid- token position
				startPosition,
				endPosition);
			return;
		}
	}

	//build a list of probable right tokens
	StringBuffer list = new StringBuffer(20);
	for (int i = 0, max = possibleTokens.length; i < max; i++) {
		if (i > 0)
			list.append(", "); //$NON-NLS-1$
		list.append('"');
		list.append(possibleTokens[i]);
		list.append('"');
	}

	if (isKeyword(currentTokenSource)) {
		String[] arguments = new String[] {new String(currentTokenSource), list.toString()};
		this.handle(
			IProblem.ParsingErrorOnKeyword,
			arguments,
			arguments,
			// this is the current -invalid- token position
			startPosition,
			endPosition);
		return;
	}
	//extract the literal when it's a literal  
	if ((errorTokenName.equals("IntegerLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("LongLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("FloatingPointLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("DoubleLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("StringLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("CharacterLiteral")) || //$NON-NLS-1$
		(errorTokenName.equals("Identifier"))) { //$NON-NLS-1$
			errorTokenName = new String(currentTokenSource);
	}

	String[] arguments = new String[] {errorTokenName, list.toString()};
	this.handle(
		IProblem.ParsingError,
		arguments,
		arguments,
		// this is the current -invalid- token position
		startPosition,
		endPosition);
}
public void possibleAccidentalBooleanAssignment(Assignment assignment) {
	String[] arguments = new String[] {};
	this.handle(
		IProblem.PossibleAccidentalBooleanAssignment,
		arguments,
		arguments,
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void publicClassMustMatchFileName(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.handle(
		IProblem.PublicClassMustMatchFileName,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void recursiveConstructorInvocation(ExplicitConstructorCall constructorCall) {

	this.handle(
		IProblem.RecursiveConstructorInvocation,
		new String[] {
			new String(constructorCall.binding.declaringClass.readableName()), 
			parametersAsString(constructorCall.binding)
		},
		new String[] {
			new String(constructorCall.binding.declaringClass.shortReadableName()), 
			parametersAsShortString(constructorCall.binding)
		},
		constructorCall.sourceStart,
		constructorCall.sourceEnd);
}

public void redefineArgument(Argument arg) {
	String[] arguments = new String[] {new String(arg.name)};
	this.handle(
		IProblem.RedefinedArgument,
		arguments,
		arguments,
		arg.sourceStart,
		arg.sourceEnd);
}
public void redefineLocal(LocalDeclaration localDecl) {
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		IProblem.RedefinedLocal,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void referenceMustBeArrayTypeAt(TypeBinding arrayType, ArrayReference arrayRef) {
	this.handle(
		IProblem.ArrayReferenceRequired,
		new String[] {new String(arrayType.readableName())},
		new String[] {new String(arrayType.shortReadableName())},
		arrayRef.sourceStart,
		arrayRef.sourceEnd);
}
public void returnTypeCannotBeVoidArray(SourceTypeBinding type, MethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(methodDecl.selector)};
	this.handle(
		IProblem.ReturnTypeCannotBeVoidArray,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void returnTypeProblem(SourceTypeBinding type, MethodDeclaration methodDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	int id;
	switch (problemId) {
		case NotFound : // 1
			id = IProblem.ReturnTypeNotFound;
			break;
		case NotVisible : // 2
			id = IProblem.ReturnTypeNotVisible;
			break;
		case Ambiguous : // 3
			id = IProblem.ReturnTypeAmbiguous;
			break;
		case InternalNameProvided : // 4
			id = IProblem.ReturnTypeInternalNameProvided;
			break;
		case InheritedNameHidesEnclosingName : // 5
			id = IProblem.ReturnTypeInheritedNameHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			return;
	}
	this.handle(
		id,
		new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
		new String[] {new String(methodDecl.selector), new String(expectedType.shortReadableName())},
		methodDecl.returnType.sourceStart,
		methodDecl.returnType.sourceEnd);
}
public void scannerError(Parser parser, String errorTokenName) {
	Scanner scanner = parser.scanner;

	int flag = IProblem.ParsingErrorNoSuggestion;
	int startPos = scanner.startPosition;

	//special treatment for recognized errors....
	if (errorTokenName.equals(Scanner.END_OF_SOURCE))
		flag = IProblem.EndOfSource;
	else
		if (errorTokenName.equals(Scanner.INVALID_HEXA))
			flag = IProblem.InvalidHexa;
		else
			if (errorTokenName.equals(Scanner.INVALID_OCTAL))
				flag = IProblem.InvalidOctal;
			else
				if (errorTokenName.equals(Scanner.INVALID_CHARACTER_CONSTANT))
					flag = IProblem.InvalidCharacterConstant;
				else
					if (errorTokenName.equals(Scanner.INVALID_ESCAPE))
						flag = IProblem.InvalidEscape;
					else
						if (errorTokenName.equals(Scanner.INVALID_UNICODE_ESCAPE)){
							flag = IProblem.InvalidUnicodeEscape;
							// better locate the error message
							char[] source = scanner.source;
							int checkPos = scanner.currentPosition - 1;
							if (checkPos >= source.length) checkPos = source.length - 1;
							while (checkPos >= startPos){
								if (source[checkPos] == '\\') break;
								checkPos --;
							}
							startPos = checkPos;
						} else
							if (errorTokenName.equals(Scanner.INVALID_FLOAT))
								flag = IProblem.InvalidFloat;
							else
								if (errorTokenName.equals(Scanner.UNTERMINATED_STRING))
									flag = IProblem.UnterminatedString;
								else
									if (errorTokenName.equals(Scanner.UNTERMINATED_COMMENT))
										flag = IProblem.UnterminatedComment;
									else
										if (errorTokenName.equals(Scanner.INVALID_CHAR_IN_STRING))
											flag = IProblem.UnterminatedString;

	String[] arguments = flag == IProblem.ParsingErrorNoSuggestion 
			? new String[] {errorTokenName}
			: NoArgument;
	this.handle(
		flag, 
		arguments,
		arguments,
		// this is the current -invalid- token position
		startPos, 
		scanner.currentPosition - 1,
		parser.compilationUnit.compilationResult);
}
public void shouldReturn(TypeBinding returnType, AstNode location) {
	this.handle(
		IProblem.ShouldReturnValue,
		new String[] { new String (returnType.readableName())},
		new String[] { new String (returnType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void signalNoImplicitStringConversionForCharArrayExpression(Expression expression) {
	this.handle(
		IProblem.NoImplicitStringConversionForCharArrayExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (currentMethod.isStatic())
		this.handle(
			// This static method cannot hide the instance method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature a static (non-abstract) method cannot hide an instance method.
			IProblem.CannotHideAnInstanceMethodWithAStaticMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else
		this.handle(
			// This instance method cannot override the static method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature an instance (non-abstract) method cannot override a static method.
			IProblem.CannotOverrideAStaticMethodWithAnInstanceMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}
public void staticFieldAccessToNonStaticVariable(FieldReference fieldRef, FieldBinding field) {
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		arguments,
		arguments,
		fieldRef.sourceStart,
		fieldRef.sourceEnd); 
}
public void staticFieldAccessToNonStaticVariable(QualifiedNameReference nameRef, FieldBinding field){
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		arguments,
		arguments,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticFieldAccessToNonStaticVariable(SingleNameReference nameRef, FieldBinding field) {
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		arguments,
		arguments,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticInheritedMethodConflicts(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	this.handle(
		// The static method %1 conflicts with the abstract method in %2
		// 8.4.6.4 - If a class inherits more than one method with the same signature it is an error for one to be static (non-abstract) and the other abstract.
		IProblem.StaticInheritedMethodConflicts,
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.readableName())},
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.shortReadableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void stringConstantIsExceedingUtf8Limit(AstNode location) {
	this.handle(
		IProblem.StringConstantIsExceedingUtf8Limit,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void superclassMustBeAClass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperclassMustBeAClass,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superclassRef.sourceStart,
		superclassRef.sourceEnd);
}
public void superinterfaceMustBeAnInterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperInterfaceMustBeAnInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void task(String tag, String message, String priority, int start, int end){
	this.handle(
		IProblem.Task,
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/},
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/}, 
		start,
		end);
}
public void tooManyDimensions(AstNode expression) {
	this.handle(
		IProblem.TooManyArrayDimensions,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void tooManyFields(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyFields,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void tooManyMethods(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyMethods,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void typeCastError(CastExpression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IllegalCast,
		new String[] { rightName, leftName },
		new String[] { rightShortName, leftShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void typeCollidesWithPackage(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	String[] arguments = new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)};
	this.handle(
		IProblem.TypeCollidesWithPackage,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd,
		compUnitDecl.compilationResult);
}
public void typeMismatchError(TypeBinding resultType, TypeBinding expectedType, AstNode location) {
	String resultTypeName = new String(resultType.readableName());
	String expectedTypeName = new String(expectedType.readableName());
	String resultTypeShortName = new String(resultType.shortReadableName());
	String expectedTypeShortName = new String(expectedType.shortReadableName());
	if (resultTypeShortName.equals(expectedTypeShortName)){
		resultTypeShortName = resultTypeName;
		expectedTypeShortName = expectedTypeName;
	}
	this.handle(
		IProblem.TypeMismatch,
		new String[] {resultTypeName, expectedTypeName},
		new String[] {resultTypeShortName, expectedTypeShortName},
		location.sourceStart,
		location.sourceEnd);
}
public void typeMismatchErrorActualTypeExpectedType(Expression expression, TypeBinding constantType, TypeBinding expectedType) {
	String constantTypeName = new String(constantType.readableName());
	String expectedTypeName = new String(expectedType.readableName());
	String constantTypeShortName = new String(constantType.shortReadableName());
	String expectedTypeShortName = new String(expectedType.shortReadableName());
	if (constantTypeShortName.equals(expectedTypeShortName)){
		constantTypeShortName = constantTypeName;
		expectedTypeShortName = expectedTypeName;
	}
	this.handle(
		IProblem.TypeMismatch,
		new String[] {constantTypeName, expectedTypeName},
		new String[] {constantTypeShortName, expectedTypeShortName},
		expression.sourceStart,
		expression.sourceEnd);
}
public void undefinedLabel(BranchStatement statement) {
	String[] arguments = new String[] {new String(statement.label)};
	this.handle(
		IProblem.UndefinedLabel,
		arguments,
		arguments,
		statement.sourceStart,
		statement.sourceEnd);
}
public void unexpectedStaticModifierForField(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {fieldDecl.name()};
	this.handle(
		IProblem.UnexpectedStaticModifierForField,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void unexpectedStaticModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {new String(type.sourceName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.UnexpectedStaticModifierForMethod,
		arguments,
		arguments,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void unhandledException(TypeBinding exceptionType, AstNode location) {

	boolean insideDefaultConstructor = 
		(referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(location instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);

	this.handle(
		insideDefaultConstructor
			? IProblem.UnhandledExceptionInDefaultConstructor
			: (insideImplicitConstructorCall 
					? IProblem.UndefinedConstructorInImplicitConstructorCall
					: IProblem.UnhandledException),
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void uninitializedBlankFinalField(FieldBinding binding, AstNode location) {
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
		IProblem.UninitializedBlankFinalField,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void uninitializedLocalVariable(LocalVariableBinding binding, AstNode location) {
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
		IProblem.UninitializedLocalVariable,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void unmatchedBracket(int position, ReferenceContext context, CompilationResult compilationResult) {

	this.handle(
		IProblem.UnmatchedBracket, 
		NoArgument,
		NoArgument,
		position, 
		position,
		context,
		compilationResult);
}
public void unnecessaryEnclosingInstanceSpecification(Expression expression, ReferenceBinding targetType) {
	this.handle(
		IProblem.IllegalEnclosingInstanceSpecification,
		new String[]{ new String(targetType.readableName())},
		new String[]{ new String(targetType.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void unnecessaryReceiverForStaticMethod(AstNode location, MethodBinding method) {
	this.handle(
		IProblem.NonStaticAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsShortString(method)},
		location.sourceStart,
		location.sourceEnd);
}
public void unnecessaryReceiverForStaticField(AstNode location, FieldBinding field) {
	this.handle(
		IProblem.NonStaticAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void unreachableCode(Statement statement) {
	this.handle(
		IProblem.CodeCannotBeReached,
		NoArgument,
		NoArgument,
		statement.sourceStart,
		statement.sourceEnd);
}
public void unreachableExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		IProblem.UnreachableCatch,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void unresolvableReference(NameReference nameRef, Binding binding) {
	int severity = Error;
/* also need to check that the searchedType is the receiver type
	if (binding instanceof ProblemBinding) {
		ProblemBinding problem = (ProblemBinding) binding;
		if (problem.searchType != null && problem.searchType.isHierarchyInconsistent())
			severity = SecondaryError;
	}
*/
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
		IProblem.UndefinedName,
		arguments,
		arguments,
		severity,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void unusedArgument(LocalDeclaration localDecl) {

	String[] arguments = new String[] {localDecl.name()};
	this.handle(
		IProblem.ArgumentIsNeverUsed,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedImport(ImportReference importRef) {
	String[] arguments = new String[] { CharOperation.toString(importRef.tokens) };
	this.handle(
		IProblem.UnusedImport,
		arguments,
		arguments,
		importRef.sourceStart,
		importRef.sourceEnd); 
}
public void unusedLocalVariable(LocalDeclaration localDecl) {
	String[] arguments = new String[] {localDecl.name()};
	this.handle(
		IProblem.LocalVariableIsNeverUsed,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedPrivateConstructor(ConstructorDeclaration constructorDecl) {
	
	if (computeSeverity(IProblem.UnusedPrivateConstructor) == Ignore) return;

	// no complaint for no-arg constructors (or default ones) - known pattern to block instantiation
	if (constructorDecl.arguments == null || constructorDecl.arguments.length == 0) return;
					
	MethodBinding constructor = constructorDecl.binding;
	this.handle(
			IProblem.UnusedPrivateConstructor,
		new String[] {
			new String(constructor.declaringClass.readableName()),
			parametersAsString(constructor)
		 }, 
		new String[] {
			new String(constructor.declaringClass.shortReadableName()),
			parametersAsShortString(constructor)
		 }, 
		constructorDecl.sourceStart,
		constructorDecl.sourceEnd);
}
public void unusedPrivateField(FieldDeclaration fieldDecl) {
	
	if (computeSeverity(IProblem.UnusedPrivateField) == Ignore) return;

	FieldBinding field = fieldDecl.binding;
	
	if (CharOperation.equals(TypeConstants.SERIALVERSIONUID, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& TypeBinding.LongBinding == field.type) {
				return; // do not report unused serialVersionUID field
		}
	this.handle(
			IProblem.UnusedPrivateField,
		new String[] {
			new String(field.declaringClass.readableName()),
			new String(field.name),
		 }, 
		new String[] {
			new String(field.declaringClass.shortReadableName()),
			new String(field.name),
		 }, 
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void unusedPrivateMethod(AbstractMethodDeclaration methodDecl) {

	if (computeSeverity(IProblem.UnusedPrivateMethod) == Ignore) return;
	
	MethodBinding method = methodDecl.binding;
	
	// no report for serialization support 'void readObject(ObjectInputStream)'
	if (!method.isStatic()
			&& TypeBinding.VoidBinding == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.READOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTINPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'void writeObject(ObjectOutputStream)'
	if (!method.isStatic()
			&& TypeBinding.VoidBinding == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTOUTPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'Object readResolve()'
	if (!method.isStatic()
			&& TypeBinding.T_Object == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.READRESOLVE)) {
		return;
	}
	// no report for serialization support 'Object writeReplace()'
	if (!method.isStatic()
			&& TypeBinding.T_Object == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEREPLACE)) {
		return;
	}
	this.handle(
			IProblem.UnusedPrivateMethod,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector),
			parametersAsString(method)
		 }, 
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector),
			parametersAsShortString(method)
		 }, 
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void unusedPrivateType(TypeDeclaration typeDecl) {
	
	if (computeSeverity(IProblem.UnusedPrivateType) == Ignore) return;

	ReferenceBinding type = typeDecl.binding;
	this.handle(
			IProblem.UnusedPrivateType,
		new String[] {
			new String(type.readableName()),
		 }, 
		new String[] {
			new String(type.shortReadableName()),
		 }, 
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void useAssertAsAnIdentifier(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UseAssertAsAnIdentifier,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);	
}

public void variableTypeCannotBeVoid(AbstractVariableDeclaration varDecl) {
	String[] arguments = new String[] {new String(varDecl.name)};
	this.handle(
		IProblem.VariableTypeCannotBeVoid,
		arguments,
		arguments,
		varDecl.sourceStart,
		varDecl.sourceEnd);
}
public void variableTypeCannotBeVoidArray(AbstractVariableDeclaration varDecl) {
	String[] arguments = new String[] {new String(varDecl.name)};
	this.handle(
		IProblem.VariableTypeCannotBeVoidArray,
		arguments,
		arguments,
		varDecl.sourceStart,
		varDecl.sourceEnd);
}
public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		//	Cannot reduce the visibility of the inherited method from %1
		// 8.4.6.3 - The access modifier of an hiding method must provide at least as much access as the hidden method.
		// 8.4.6.3 - The access modifier of an overiding method must provide at least as much access as the overriden method.
		IProblem.MethodReducesVisibility,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		new String[] {new String(inheritedMethod.declaringClass.shortReadableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void wrongSequenceOfExceptionTypesError(TryStatement statement, int under, int upper) {
	//the two catch block under and upper are in an incorrect order.
	//under should be define BEFORE upper in the source

	TypeReference typeRef = statement.catchArguments[under].type;
	this.handle(
		IProblem.UnreachableCatch,
		NoArgument,
		NoArgument,
		typeRef.sourceStart,
		typeRef.sourceEnd);
}

public void nonExternalizedStringLiteral(AstNode location) {
	this.handle(
		IProblem.NonExternalizedStringLiteral,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}

public void noMoreAvailableSpaceForConstant(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyBytesForStringConstant,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}

public void noMoreAvailableSpaceInConstantPool(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyConstantsInConstantPool,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}

private boolean isKeyword(char[] tokenSource) {
	/*
	 * This code is heavily grammar dependant
	 */

	if (tokenSource == null) {
		return false;
	}
	try {
		Scanner scanner = new Scanner();
		scanner.setSource(tokenSource);
		int token = scanner.getNextToken();
		char[] currentKeyword;
		try {
			currentKeyword = scanner.getCurrentIdentifierSource();
		} catch (ArrayIndexOutOfBoundsException e) {
			return false;
		}
		int nextToken= scanner.getNextToken();
		if (nextToken == TerminalTokens.TokenNameEOF
			&& scanner.startPosition == scanner.source.length) { // to handle case where we had an ArrayIndexOutOfBoundsException 
															     // while reading the last token
			switch(token) {
				case Scanner.TokenNameERROR:
					if (CharOperation.equals("goto".toCharArray(), currentKeyword) ||CharOperation.equals("const".toCharArray(), currentKeyword)) { //$NON-NLS-1$ //$NON-NLS-2$
						return true;
					} else {
						return false;
					}
				case Scanner.TokenNameabstract:
				case Scanner.TokenNameassert:
				case Scanner.TokenNamebyte:
				case Scanner.TokenNamebreak:
				case Scanner.TokenNameboolean:
				case Scanner.TokenNamecase:
				case Scanner.TokenNamechar:
				case Scanner.TokenNamecatch:
				case Scanner.TokenNameclass:
				case Scanner.TokenNamecontinue:
				case Scanner.TokenNamedo:
				case Scanner.TokenNamedouble:
				case Scanner.TokenNamedefault:
				case Scanner.TokenNameelse:
				case Scanner.TokenNameextends:
				case Scanner.TokenNamefor:
				case Scanner.TokenNamefinal:
				case Scanner.TokenNamefloat:
				case Scanner.TokenNamefalse:
				case Scanner.TokenNamefinally:
				case Scanner.TokenNameif:
				case Scanner.TokenNameint:
				case Scanner.TokenNameimport:
				case Scanner.TokenNameinterface:
				case Scanner.TokenNameimplements:
				case Scanner.TokenNameinstanceof:
				case Scanner.TokenNamelong:
				case Scanner.TokenNamenew:
				case Scanner.TokenNamenull:
				case Scanner.TokenNamenative:
				case Scanner.TokenNamepublic:
				case Scanner.TokenNamepackage:
				case Scanner.TokenNameprivate:
				case Scanner.TokenNameprotected:
				case Scanner.TokenNamereturn:
				case Scanner.TokenNameshort:
				case Scanner.TokenNamesuper:
				case Scanner.TokenNamestatic:
				case Scanner.TokenNameswitch:
				case Scanner.TokenNamestrictfp:
				case Scanner.TokenNamesynchronized:
				case Scanner.TokenNametry:
				case Scanner.TokenNamethis:
				case Scanner.TokenNametrue:
				case Scanner.TokenNamethrow:
				case Scanner.TokenNamethrows:
				case Scanner.TokenNametransient:
				case Scanner.TokenNamevoid:
				case Scanner.TokenNamevolatile:
				case Scanner.TokenNamewhile:
					return true;
				default: 
					return false;
			}
		} else {
			return false;
		}
	}
	catch (InvalidInputException e) {
		return false;
	}
	
}
}

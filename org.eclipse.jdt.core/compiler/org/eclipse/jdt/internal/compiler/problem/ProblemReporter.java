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
package org.eclipse.jdt.internal.compiler.problem;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
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
public void abortDueToInternalError(String errorMessage, ASTNode location) {
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
public void alreadyDefinedLabel(char[] labelName, ASTNode location) {
	String[] arguments = new String[] {new String(labelName)};
	this.handle(
		IProblem.DuplicateLabel,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void annotationTypeMemberDeclarationWithConstructorName(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
	this.handle(
		IProblem.AnnotationButConstructorName,
		NoArgument,
		NoArgument,
		annotationTypeMemberDeclaration.sourceStart,
		annotationTypeMemberDeclaration.sourceEnd);
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
public void boundsMustBeAnInterface(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.BoundsMustBeAnInterface,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	if (location.isConstructor()) {
		this.handle(
			IProblem.BytecodeExceeds64KLimitForConstructor,
			new String[] {new String(location.selector), parametersAsString(location.binding.parameters, false)},
			new String[] {new String(location.selector), parametersAsString(location.binding.parameters, true)},
			Error | Abort,
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.BytecodeExceeds64KLimit,
			new String[] {new String(location.selector), parametersAsString(location.binding.parameters, false)},
			new String[] {new String(location.selector), parametersAsString(location.binding.parameters, true)},
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
public void cannotAssignToFinalField(FieldBinding field, ASTNode location) {
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
public void cannotAssignToFinalLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.NonBlankFinalLocalAssignment,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotAssignToFinalOuterLocal(LocalVariableBinding local, ASTNode location) {
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
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
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
public void cannotReferToNonFinalOuterLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments =new String[]{ new String(local.readableName())};
	this.handle(
		IProblem.OuterLocalMustBeFinal,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void cannotReturnInInitializer(ASTNode location) {
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
public void cannotUseSuperInCodeSnippet(int start, int end) {
	this.handle(
		IProblem.CannotUseSuperInCodeSnippet,
		NoArgument,
		NoArgument,
		Error | Abort,
		start,
		end);
}
public void cannotUseSuperInJavaLangObject(ASTNode reference) {
	this.handle(
		IProblem.ObjectHasNoSuperclass,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void caseExpressionMustBeConstant(Expression expression) {
	this.handle(
		IProblem.NonConstantExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void classExtendFinalClass(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.ClassExtendFinalClass,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
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
	switch(problemId){

		case IProblem.MaskedCatch : 
			return this.options.getSeverity(CompilerOptions.MaskedCatchBlock);

		case IProblem.UnusedImport :
			return this.options.getSeverity(CompilerOptions.UnusedImport);
			
		case IProblem.MethodButWithConstructorName :
			return this.options.getSeverity(CompilerOptions.MethodWithConstructorName);
		
		case IProblem.OverridingNonVisibleMethod :
			return this.options.getSeverity(CompilerOptions.OverriddenPackageDefaultMethod);

		case IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod :
		case IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod :
			return this.options.getSeverity(CompilerOptions.IncompatibleNonInheritedInterfaceMethod);

		case IProblem.OverridingDeprecatedMethod :				
		case IProblem.UsingDeprecatedType :				
		case IProblem.UsingDeprecatedMethod :
		case IProblem.UsingDeprecatedConstructor :
		case IProblem.UsingDeprecatedField :
			return this.options.getSeverity(CompilerOptions.UsingDeprecatedAPI);
		
		case IProblem.LocalVariableIsNeverUsed :
			return this.options.getSeverity(CompilerOptions.UnusedLocalVariable);
		
		case IProblem.ArgumentIsNeverUsed :
			return this.options.getSeverity(CompilerOptions.UnusedArgument);

		case IProblem.NoImplicitStringConversionForCharArrayExpression :
			return this.options.getSeverity(CompilerOptions.NoImplicitStringConversion);

		case IProblem.NeedToEmulateFieldReadAccess :
		case IProblem.NeedToEmulateFieldWriteAccess :
		case IProblem.NeedToEmulateMethodAccess :
		case IProblem.NeedToEmulateConstructorAccess :			
			return this.options.getSeverity(CompilerOptions.AccessEmulation);

		case IProblem.NonExternalizedStringLiteral :
			return this.options.getSeverity(CompilerOptions.NonExternalizedString);

		case IProblem.UseAssertAsAnIdentifier :
			return this.options.getSeverity(CompilerOptions.AssertUsedAsAnIdentifier);
		case IProblem.UseEnumAsAnIdentifier :
			return this.options.getSeverity(CompilerOptions.EnumUsedAsAnIdentifier);

		case IProblem.NonStaticAccessToStaticMethod :
		case IProblem.NonStaticAccessToStaticField :
			return this.options.getSeverity(CompilerOptions.NonStaticAccessToStatic);

		case IProblem.IndirectAccessToStaticMethod :
		case IProblem.IndirectAccessToStaticField :
		case IProblem.IndirectAccessToStaticType :
			return this.options.getSeverity(CompilerOptions.IndirectStaticAccess);

		case IProblem.AssignmentHasNoEffect:
			return this.options.getSeverity(CompilerOptions.NoEffectAssignment);

		case IProblem.UnusedPrivateConstructor:
		case IProblem.UnusedPrivateMethod:
		case IProblem.UnusedPrivateField:
		case IProblem.UnusedPrivateType:
			return this.options.getSeverity(CompilerOptions.UnusedPrivateMember);

		case IProblem.Task :
			return Warning;			

		case IProblem.LocalVariableHidingLocalVariable:
		case IProblem.LocalVariableHidingField:
		case IProblem.ArgumentHidingLocalVariable:
		case IProblem.ArgumentHidingField:
			return this.options.getSeverity(CompilerOptions.LocalVariableHiding);

		case IProblem.FieldHidingLocalVariable:
		case IProblem.FieldHidingField:
			return this.options.getSeverity(CompilerOptions.FieldHiding);

		case IProblem.PossibleAccidentalBooleanAssignment:
			return this.options.getSeverity(CompilerOptions.AccidentalBooleanAssign);

		case IProblem.SuperfluousSemicolon:
		case IProblem.EmptyControlFlowStatement:
			return this.options.getSeverity(CompilerOptions.EmptyStatement);

		case IProblem.UndocumentedEmptyBlock:
			return this.options.getSeverity(CompilerOptions.UndocumentedEmptyBlock);
			
		case IProblem.UnnecessaryCast:
		case IProblem.UnnecessaryArgumentCast:
		case IProblem.UnnecessaryInstanceof:
			return this.options.getSeverity(CompilerOptions.UnnecessaryTypeCheck);
			
		case IProblem.FinallyMustCompleteNormally:
			return this.options.getSeverity(CompilerOptions.FinallyBlockNotCompleting);
			
		case IProblem.UnusedMethodDeclaredThrownException:
		case IProblem.UnusedConstructorDeclaredThrownException:
			return this.options.getSeverity(CompilerOptions.UnusedDeclaredThrownException);

		case IProblem.UnqualifiedFieldAccess:
			return this.options.getSeverity(CompilerOptions.UnqualifiedFieldAccess);
		
		case IProblem.UnnecessaryElse:
			return this.options.getSeverity(CompilerOptions.UnnecessaryElse);

		case IProblem.UnsafeRawConstructorInvocation:
		case IProblem.UnsafeRawMethodInvocation:
		case IProblem.UnsafeRawConversion:
		case IProblem.UnsafeRawFieldAssignment:
		case IProblem.UnsafeGenericCast:
		case IProblem.UnsafeReturnTypeOverride:
			return this.options.getSeverity(CompilerOptions.UnsafeTypeOperation);

		case IProblem.FinalBoundForTypeVariable:
		    return this.options.getSeverity(CompilerOptions.FinalParameterBound);

		case IProblem.MissingSerialVersion:
			return this.options.getSeverity(CompilerOptions.MissingSerialVersion);
		
		case IProblem.ForbiddenReference:
			return this.options.getSeverity(CompilerOptions.ForbiddenReference);
		
		/*
		 * Javadoc syntax errors
		 */
		case IProblem.JavadocUnexpectedTag:
		case IProblem.JavadocDuplicateReturnTag:
		case IProblem.JavadocInvalidThrowsClass:
		case IProblem.JavadocInvalidReference:
		case IProblem.JavadocMalformedSeeReference:
		case IProblem.JavadocInvalidSeeHref:
		case IProblem.JavadocInvalidSeeArgs:
		case IProblem.JavadocInvalidTag:
		case IProblem.JavadocUnterminatedInlineTag:
		case IProblem.JavadocMissingHashCharacter:
		case IProblem.JavadocEmptyReturnTag:
		case IProblem.JavadocUnexpectedText:
			if (this.options.docCommentSupport) {
				return this.options.getSeverity(CompilerOptions.InvalidJavadoc);
			} else {
				return ProblemSeverities.Ignore;
			}

		/*
		 * Javadoc tags resolved references errors
		 */
		case IProblem.JavadocInvalidParamName:
		case IProblem.JavadocDuplicateParamName:
		case IProblem.JavadocMissingParamName:
		case IProblem.JavadocInvalidThrowsClassName:
		case IProblem.JavadocDuplicateThrowsClassName:
		case IProblem.JavadocMissingThrowsClassName:
		case IProblem.JavadocMissingReference:
		case IProblem.JavadocInvalidValueReference:
		case IProblem.JavadocUsingDeprecatedField:
		case IProblem.JavadocUsingDeprecatedConstructor:
		case IProblem.JavadocUsingDeprecatedMethod:
		case IProblem.JavadocUsingDeprecatedType:
		case IProblem.JavadocUndefinedField:
		case IProblem.JavadocNotVisibleField:
		case IProblem.JavadocAmbiguousField:
		case IProblem.JavadocUndefinedConstructor:
		case IProblem.JavadocNotVisibleConstructor:
		case IProblem.JavadocAmbiguousConstructor:
		case IProblem.JavadocUndefinedMethod:
		case IProblem.JavadocNotVisibleMethod:
		case IProblem.JavadocAmbiguousMethod:
		case IProblem.JavadocAmbiguousMethodReference:
		case IProblem.JavadocParameterMismatch:
		case IProblem.JavadocUndefinedType:
		case IProblem.JavadocNotVisibleType:
		case IProblem.JavadocAmbiguousType:
		case IProblem.JavadocInternalTypeNameProvided:
		case IProblem.JavadocNoMessageSendOnArrayType:
		case IProblem.JavadocNoMessageSendOnBaseType:
		case IProblem.JavadocInheritedMethodHidesEnclosingName:
		case IProblem.JavadocInheritedFieldHidesEnclosingName:
		case IProblem.JavadocInheritedNameHidesEnclosingTypeName:
			if (this.options.docCommentSupport && this.options.reportInvalidJavadocTags) {
				return this.options.getSeverity(CompilerOptions.InvalidJavadoc);
			} else {
				return ProblemSeverities.Ignore;
			}

		/*
		 * Javadoc missing tags errors
		 */
		case IProblem.JavadocMissingParamTag:
		case IProblem.JavadocMissingReturnTag:
		case IProblem.JavadocMissingThrowsTag:
			if (this.options.docCommentSupport) {
				return this.options.getSeverity(CompilerOptions.MissingJavadocTags);
			} else {
				return ProblemSeverities.Ignore;
			}

		/*
		 * Missing Javadoc errors
		 */
		case IProblem.JavadocMissing:
			if (this.options.docCommentSupport) {
				return this.options.getSeverity(CompilerOptions.MissingJavadocComments);
			} else {
				return ProblemSeverities.Ignore;
			}

		// by default problems are errors.
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
public void constantOutOfFormat(NumberLiteral literal) {
	// the literal is not in a correct format
	// this code is called on IntLiteral and LongLiteral 
	// example 000811 ...the 8 is uncorrect.

	if ((literal instanceof LongLiteral) || (literal instanceof IntLiteral)) {
		char[] source = literal.source();
		try {
			final String Radix;
			final int radix;
			if ((source[1] == 'x') || (source[1] == 'X')) {
				radix = 16;
				Radix = "Hex"; //$NON-NLS-1$
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
			String[] arguments = new String[] {
				new String(literal.literalType(null).readableName()), // numeric literals do not need scope to reach type
				Radix + " " + new String(source) + " (digit " + new String(new char[] {source[place]}) + ")"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			this.handle(
				IProblem.NumericValueOutOfRange,
				arguments,
				arguments,
				literal.sourceStart,
				literal.sourceEnd);
			return;
		} catch (IndexOutOfBoundsException ex) {
			// should never happen
		}
	
		// just in case .... use a predefined error..
		// we should never come here...(except if the code changes !)
		this.constantOutOfRange(literal, literal.literalType(null)); // numeric literals do not need scope to reach type
	}
}
public void constantOutOfRange(Literal literal, TypeBinding literalType) {
	String[] arguments = new String[] {new String(literalType.readableName()), new String(literal.source())};
	this.handle(
		IProblem.NumericValueOutOfRange,
		arguments,
		arguments,
		literal.sourceStart,
		literal.sourceEnd);
}
public void corruptedSignature(TypeBinding enclosingType, char[] signature, int position) {
	this.handle(
		IProblem.CorruptedSignature,
		new String[] { new String(enclosingType.readableName()), new String(signature), String.valueOf(position) },
		new String[] { new String(enclosingType.shortReadableName()), new String(signature), String.valueOf(position) },
		Error | Abort,
		0,
		0);
}
public void deprecatedField(FieldBinding field, ASTNode location) {
	this.handle(
		IProblem.UsingDeprecatedField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void deprecatedMethod(MethodBinding method, ASTNode location) {
	if (method.isConstructor()) {
		this.handle(
			IProblem.UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), parametersAsString(method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), parametersAsString(method.parameters, true)},
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
			location.sourceStart,
			location.sourceEnd);
	}
}
public void deprecatedType(TypeBinding type, ASTNode location) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	this.handle(
		IProblem.UsingDeprecatedType,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateCase(CaseStatement caseStatement) {
	this.handle(
		IProblem.DuplicateCase,
		NoArgument,
		NoArgument,
		caseStatement.sourceStart,
		caseStatement.sourceEnd);
}
public void duplicateDefaultCase(ASTNode statement) {
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
		new String[] {new String(type.sourceName()), new String(fieldDecl.name)},
		new String[] {new String(type.shortReadableName()), new String(fieldDecl.name)},
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
public void duplicateInitializationOfFinalLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] { new String(local.readableName())};
	this.handle(
		IProblem.DuplicateFinalLocalInitialization,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateMethodInType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
    MethodBinding method = methodDecl.binding;
    boolean duplicateErasure = false;
    if ((method.modifiers & CompilerModifiers.AccGenericSignature) != 0) {
        // chech it occurs in parameters (the bit is set for return type | params | thrown exceptions
        for (int i = 0, length = method.parameters.length; i < length; i++) {
            if ((method.parameters[i].tagBits & TagBits.HasTypeVariable) != 0) {
                duplicateErasure = true;
                break;
            }
        }
    }
    if (duplicateErasure) {
        int length = method.parameters.length;
        TypeBinding[] erasures = new TypeBinding[length];
        for (int i = 0; i < length; i++)  {
            erasures[i] = method.parameters[i].erasure();
        }
		this.handle(
			IProblem.DuplicateMethodErasure,
			new String[] {
		        new String(methodDecl.selector),
				new String(method.declaringClass.readableName()),
				parametersAsString(method.parameters, false),
				parametersAsString(erasures, false) } ,
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				parametersAsString(method.parameters, true),
				parametersAsString(erasures, true) },
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
    } else {
		this.handle(
			IProblem.DuplicateMethod,
			new String[] {
		        new String(methodDecl.selector),
				new String(method.declaringClass.readableName()),
				parametersAsString(method.parameters, false)},
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				parametersAsString(method.parameters, true)},
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
    }
}
public void duplicateModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
/* to highlight modifiers use:
	this.handle(
		new Problem(
			DuplicateModifierForField,
			new String[] {new String(fieldDecl.name)},
			fieldDecl.modifiers.sourceStart,
			fieldDecl.modifiers.sourceEnd));
*/
	String[] arguments = new String[] {new String(fieldDecl.name)};
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
	String[] arguments = new String[] {new String(localDecl.name)};
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
public void duplicateTypeParameterInType(TypeParameter typeParameter) {
	this.handle(
		IProblem.DuplicateTypeVariable,
		new String[] { new String(typeParameter.name)},
		new String[] { new String(typeParameter.name)},
		typeParameter.sourceStart,
		typeParameter.sourceEnd);
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
public void emptyControlFlowStatement(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.EmptyControlFlowStatement,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);	
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

	int id = recType.isArrayType() ? IProblem.NoMessageSendOnArrayType : IProblem.NoMessageSendOnBaseType;
	/*
	if ((messageSend.bits & ASTNode.InsideJavadoc) != 0) {
		id |= IProblem.Javadoc;
		if (!reportInvalidJavadocTagsVisibility()) return;
	}
	*/
	this.handle(
		id,
		new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
		new String[] {new String(recType.shortReadableName()), new String(messageSend.selector), shortBuffer.toString()},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void errorThisSuperInStatic(ASTNode reference) {
	String[] arguments = new String[] {reference.isSuper() ? "super" : "this"}; //$NON-NLS-2$ //$NON-NLS-1$
	this.handle(
		IProblem.ThisInStaticContext,
		arguments,
		arguments,
		reference.sourceStart,
		reference.sourceEnd);
}
public void expressionShouldBeAVariable(Expression expression) {
	this.handle(
		IProblem.ExpressionShouldBeAVariable,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void fieldHiding(FieldDeclaration fieldDecl, Binding hiddenVariable) {
	FieldBinding field = fieldDecl.binding;
	if (CharOperation.equals(TypeConstants.SERIALVERSIONUID, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& BaseTypes.LongBinding == field.type) {
				return; // do not report unused serialVersionUID field
	}
	if (CharOperation.equals(TypeConstants.SERIALPERSISTENTFIELDS, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& field.type.dimensions() == 1
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTSTREAMFIELD, field.type.leafComponentType().readableName())) {
				return; // do not report unused serialPersistentFields field
	}
	
	if (hiddenVariable instanceof LocalVariableBinding) {
		this.handle(
			IProblem.FieldHidingLocalVariable,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) },
			fieldDecl.sourceStart,
			fieldDecl.sourceEnd);
	} else if (hiddenVariable instanceof FieldBinding) {
		FieldBinding hiddenField = (FieldBinding) hiddenVariable;
		this.handle(
			IProblem.FieldHidingField,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name) , new String(hiddenField.declaringClass.readableName())  },
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name) , new String(hiddenField.declaringClass.shortReadableName()) },
			fieldDecl.sourceStart,
			fieldDecl.sourceEnd);
	}
}
private int fieldLocation(FieldBinding field, ASTNode node) {
	if (node instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) node;
		FieldBinding[] bindings = ref.otherBindings;
		if (bindings != null)
			for (int i = bindings.length; --i >= 0;)
				if (bindings[i] == field)
					return (int) ref.sourcePositions[i + 1]; // first position is for the primary field
	}
	return node.sourceEnd;
}
public void fieldsOrThisBeforeConstructorInvocation(ThisReference reference) {
	this.handle(
		IProblem.ThisSuperDuringConstructorInvocation,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void finallyMustCompleteNormally(Block finallyBlock) {
	this.handle(
		IProblem.FinallyMustCompleteNormally,
		NoArgument,
		NoArgument,
		finallyBlock.sourceStart,
		finallyBlock.sourceEnd);
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
public void finalVariableBound(TypeVariableBinding typeVariable, TypeReference typeRef) {
	this.handle(
		IProblem.FinalBoundForTypeVariable,
		new String[] { new String(typeVariable.sourceName), new String(typeRef.resolvedType.readableName())},
		new String[] { new String(typeVariable.sourceName), new String(typeRef.resolvedType.shortReadableName())},
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
public void forbiddenReference(TypeBinding type, ASTNode location, String visibilityRuleOwner) {
	if (location == null) return; 
	this.handle(
		IProblem.ForbiddenReference,
		new String[] {new String(type.readableName()), visibilityRuleOwner},
		new String[] {new String(type.shortReadableName()), visibilityRuleOwner},
		location.sourceStart,
		location.sourceEnd);
}
public void forwardReference(Reference reference, int indexInQualification, TypeBinding type) {
	this.handle(
		IProblem.ReferenceToForwardField,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void forwardTypeVariableReference(ASTNode location, TypeVariableBinding type) {
	this.handle(
		IProblem.ReferenceToForwardTypeVariable,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void genericTypeCannotExtendThrowable(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.GenericTypeCannotExtendThrowable,
		new String[]{ new String(typeDecl.binding.readableName()) },
		new String[]{ new String(typeDecl.binding.shortReadableName()) },
		typeDecl.superclass.sourceStart,
		typeDecl.superclass.sourceEnd);
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
			this.referenceContext, 
			this.referenceContext == null ? null : this.referenceContext.compilationResult()); 
	this.referenceContext = null;
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
			this.referenceContext, 
			unitResult); 
	this.referenceContext = null;
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
			this.referenceContext, 
			this.referenceContext == null ? null : this.referenceContext.compilationResult()); 
	this.referenceContext = null;
}
public void hiddenCatchBlock(ReferenceBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.MaskedCatch,
		new String[] {
			new String(exceptionType.readableName()),
		 }, 
		new String[] {
			new String(exceptionType.shortReadableName()),
		 }, 
		location.sourceStart,
		location.sourceEnd);
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
	String typeName = new String(superType.readableName());
	String shortTypeName = new String(superType.sourceName());

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
	} else {
		start = reference.sourceStart;
		end = reference.sourceEnd;
		if (sourceType != superType) {
			char[][] qName = reference.getTypeName();
			typeName = CharOperation.toString(qName);
			shortTypeName = new String(qName[qName.length-1]);
		}
	}

	if (sourceType == superType)
		this.handle(
			IProblem.HierarchyCircularitySelfReference,
			new String[] {typeName},
			new String[] {shortTypeName},
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
public void illegalClassLiteralForTypeVariable(TypeVariableBinding variable, ASTNode location) {
	String[] arguments = new String[] { new String(variable.sourceName) };
	this.handle(
		IProblem.IllegalClassLiteralForTypeVariable,
		arguments, 
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void illegalExtendedDimensions(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
	this.handle(
		IProblem.IllegalExtendedDimensions,
		NoArgument, 
		NoArgument, 
		annotationTypeMemberDeclaration.sourceStart,
		annotationTypeMemberDeclaration.sourceEnd);
}
public void illegalGenericArray(TypeBinding leadtComponentType, ASTNode location) {
	this.handle(
		IProblem.IllegalGenericArray,
		new String[]{ new String(leadtComponentType.readableName())},
		new String[]{ new String(leadtComponentType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalInstanceOfGenericType(TypeBinding checkedType, ASTNode location) {
	if (checkedType.isTypeVariable()) {
		this.handle(
		IProblem.IllegalInstanceofTypeParameter,
			new String[] { new String(checkedType.readableName()), new String(checkedType.erasure().readableName())},
			new String[] { new String(checkedType.shortReadableName()), new String(checkedType.erasure().shortReadableName())},
			location.sourceStart,
			location.sourceEnd);
		return;
	}
	this.handle(
		IProblem.IllegalInstanceofParameterizedType,
		new String[] { new String(checkedType.readableName()), new String(checkedType.erasure().sourceName())},
		new String[] { new String(checkedType.shortReadableName()), new String(checkedType.erasure().sourceName())},
		location.sourceStart,
		location.sourceEnd);
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
	String[] arguments = new String[] {new String(fieldDecl.name)};

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
	String[] arguments = new String[] {new String(fieldDecl.name)};
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
	String[] arguments = new String[] {new String(fieldDecl.name)};
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
	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		complainAsArgument
			? IProblem.IllegalModifierForArgument
			: IProblem.IllegalModifierForVariable,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void illegalPrimitiveOrArrayTypeForEnclosingInstance(TypeBinding enclosingType, ASTNode location) {
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
public void illegalUsageOfQualifiedTypeReference(QualifiedTypeReference qualifiedTypeReference) {
	StringBuffer buffer = new StringBuffer();
	char[][] tokens = qualifiedTypeReference.tokens;
	for (int i = 0; i < tokens.length; i++) {
		if (i > 0) buffer.append('.');
		buffer.append(tokens[i]);
	}
	String[] arguments = new String[] { String.valueOf(buffer)};
	this.handle(
		IProblem.IllegalUsageOfQualifiedTypeReference,
		arguments,
		arguments,
		qualifiedTypeReference.sourceStart,
		qualifiedTypeReference.sourceEnd);	
}
public void illegalVisibilityModifierCombinationForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
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
public void illegalVoidExpression(ASTNode location) {
	this.handle(
		IProblem.InvalidVoidExpression,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void importProblem(ImportReference importRef, Binding expectedImport) {
	if (expectedImport.problemId() == NotFound) {
		char[][] tokens = expectedImport instanceof ProblemReferenceBinding
			? ((ProblemReferenceBinding) expectedImport).compoundName
			: importRef.tokens;
		String[] arguments = new String[]{CharOperation.toString(tokens)};
		this.handle(
		        IProblem.ImportNotFound, 
		        arguments, 
		        arguments, 
		        importRef.sourceStart, 
		        (int) importRef.sourcePositions[tokens.length - 1]);
		return;
	}
	invalidType(importRef, (TypeBinding)expectedImport);
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
public void incorrectArityForParameterizedType(ASTNode location, TypeBinding type, TypeBinding[] argumentTypes) {
    if (location == null) {
		this.handle(
			IProblem.IncorrectArityForParameterizedType,
			new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true)},
			AbortCompilation | Error,
			0,
			1);        
    }
	this.handle(
		IProblem.IncorrectArityForParameterizedType,
		new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true)},
		location.sourceStart,
		location.sourceEnd);
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
public void indirectAccessToStaticField(ASTNode location, FieldBinding field){
	this.handle(
		IProblem.IndirectAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		fieldLocation(field, location));
}
public void indirectAccessToStaticMethod(ASTNode location, MethodBinding method) {
	this.handle(
		IProblem.IndirectAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void indirectAccessToStaticType(ASTNode location, ReferenceBinding type) {
	this.handle(
		IProblem.IndirectAccessToStaticMethod,
		new String[] {new String(type.enclosingType().readableName()), new String(type.sourceName) },
		new String[] {new String(type.enclosingType().shortReadableName()), new String(type.sourceName) },
		location.sourceStart,
		location.sourceEnd);
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
			concreteSignature.toString(),
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
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, ASTNode location) {
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
public void invalidBreak(ASTNode location) {
	this.handle(
		IProblem.InvalidBreak,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void invalidConstructor(Statement statement, MethodBinding targetConstructor) {

	boolean insideDefaultConstructor = 
		(this.referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)this.referenceContext).isDefaultConstructor();
	boolean insideImplicitConstructorCall =
		(statement instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) statement).accessMode == ExplicitConstructorCall.ImplicitSuper);

	int id = IProblem.UndefinedConstructor; //default...
    MethodBinding shownConstructor = targetConstructor;
	switch (targetConstructor.problemId()) {
		case NotFound :
			if (insideDefaultConstructor){
				id = IProblem.UndefinedConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.UndefinedConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.UndefinedConstructor;
			}
			break;
		case NotVisible :
			if (insideDefaultConstructor){
				id = IProblem.NotVisibleConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.NotVisibleConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.NotVisibleConstructor;
			}
			ProblemMethodBinding problemConstructor = (ProblemMethodBinding) targetConstructor;
			if (problemConstructor.closestMatch != null) {
			    shownConstructor = problemConstructor.closestMatch.original();
		    }					
			break;
		case Ambiguous :
			if (insideDefaultConstructor){
				id = IProblem.AmbiguousConstructorInDefaultConstructor;
			} else if (insideImplicitConstructorCall){
				id = IProblem.AmbiguousConstructorInImplicitConstructorCall;
			} else {
				id = IProblem.AmbiguousConstructor;
			}
			break;
		case ParameterBoundMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			ParameterizedGenericMethodBinding substitutedConstructor = (ParameterizedGenericMethodBinding) problemConstructor.closestMatch;
			shownConstructor = substitutedConstructor.original();
			TypeBinding typeArgument = targetConstructor.parameters[0];
			TypeVariableBinding typeParameter = (TypeVariableBinding) targetConstructor.parameters[1];
			this.handle(
				IProblem.GenericConstructorTypeArgumentMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        parametersAsString(substitutedConstructor.parameters, false), 
				        new String(typeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        parametersAsString(substitutedConstructor.parameters, true), 
				        new String(typeArgument.shortReadableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, true) },
				statement.sourceStart,
				statement.sourceEnd);		    
			return;		    
			
		case TypeParameterArityMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			if (shownConstructor.typeVariables == TypeConstants.NoTypeVariables) {
				this.handle(
					IProblem.NonGenericConstructor,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        parametersAsString(shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
					        parametersAsString(targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        parametersAsString(shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
					        parametersAsString(targetConstructor.parameters, true) },
					statement.sourceStart,
					statement.sourceEnd);		    
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedConstructor  ,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        parametersAsString(shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
							parametersAsString(shownConstructor.typeVariables, false),
					        parametersAsString(targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        parametersAsString(shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
							parametersAsString(shownConstructor.typeVariables, true),
					        parametersAsString(targetConstructor.parameters, true) },
					statement.sourceStart,
					statement.sourceEnd);		    
			}
			return;
		case ParameterizedMethodTypeMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.ParameterizedConstructorArgumentTypeMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
						parametersAsString(((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, false),
				        parametersAsString(targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
						parametersAsString(((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, true),
				        parametersAsString(targetConstructor.parameters, true) },
				statement.sourceStart,
				statement.sourceEnd);		    
			return;
		case TypeArgumentsForRawGenericMethod :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.TypeArgumentsForRawGenericConstructor,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        parametersAsString(targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        parametersAsString(shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        parametersAsString(targetConstructor.parameters, true) },
				statement.sourceStart,
				statement.sourceEnd);	
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), parametersAsString(shownConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), parametersAsString(shownConstructor.parameters, true)},
		statement.sourceStart,
		statement.sourceEnd);
}
public void invalidContinue(ASTNode location) {
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

public void invalidExplicitConstructorCall(ASTNode location) {
	
	this.handle(
		IProblem.InvalidExplicitConstructorCall,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
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
	int id = IProblem.UndefinedField;
	FieldBinding field = fieldRef.binding;
	switch (field.problemId()) {
		case NotFound :
			id = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			id = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType, // cannot occur in javadoc comments
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
		id,
		arguments,
		arguments,
		fieldRef.sourceStart,
		fieldRef.sourceEnd);
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	int id = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			id = IProblem.UndefinedField;
			break;
		case NotVisible :
			id = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
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
		id,
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
			(int) nameRef.sourcePositions[index]);
		return;
	}

	int id = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			id = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			id = IProblem.NotVisibleField;
			break;
		case Ambiguous :
			id = IProblem.AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			id = IProblem.NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.InheritedFieldHidesEnclosingName;
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
		id, 
		arguments,
		arguments,
		nameRef.sourceStart, 
		(int) nameRef.sourcePositions[index]);
}
public void invalidFileNameForPackageAnnotations(Annotation annotation) {
	this.handle(
			IProblem.InvalidFileNameForPackageAnnotations,
			NoArgument,
			NoArgument,
			annotation.sourceStart,
			annotation.sourceEnd);	
}
public void invalidMethod(MessageSend messageSend, MethodBinding method) {
	int id = IProblem.UndefinedMethod; //default...
    MethodBinding shownMethod = method;
	switch (method.problemId()) {
		case NotFound :
			id = IProblem.UndefinedMethod;
			ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
			    	shownMethod = problemMethod.closestMatch;
					String closestParameterTypeNames = parametersAsString(shownMethod.parameters, false);
					String parameterTypeNames = parametersAsString(method.parameters, false);
					String closestParameterTypeShortNames = parametersAsString(shownMethod.parameters, true);
					String parameterTypeShortNames = parametersAsString(method.parameters, true);
					if (closestParameterTypeShortNames.equals(parameterTypeShortNames)){
						closestParameterTypeShortNames = closestParameterTypeNames;
						parameterTypeShortNames = parameterTypeNames;
					}
					this.handle(
						IProblem.ParameterMismatch,
						new String[] {
							new String(shownMethod.declaringClass.readableName()),
							new String(shownMethod.selector),
							closestParameterTypeNames,
							parameterTypeNames 
						},
						new String[] {
							new String(shownMethod.declaringClass.shortReadableName()),
							new String(shownMethod.selector),
							closestParameterTypeShortNames,
							parameterTypeShortNames
						},
						(int) (messageSend.nameSourcePosition >>> 32),
						(int) messageSend.nameSourcePosition);
					return;
			}			
			break;
		case NotVisible :
			id = IProblem.NotVisibleMethod;
			problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
			    shownMethod = problemMethod.closestMatch.original();
		    }			
			break;
		case Ambiguous :
			id = IProblem.AmbiguousMethod;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.InheritedMethodHidesEnclosingName;
			break;
		case NonStaticReferenceInConstructorInvocation :
			id = IProblem.InstanceMethodDuringConstructorInvocation;
			break;
		case NonStaticReferenceInStaticContext :
			id = IProblem.StaticMethodRequested;
			break;
		case ReceiverTypeNotVisible :
			this.handle(
				IProblem.NotVisibleType,	// cannot occur in javadoc comments
				new String[] {new String(method.declaringClass.leafComponentType().readableName())},
				new String[] {new String(method.declaringClass.leafComponentType().shortReadableName())},
				messageSend.receiver.sourceStart,
				messageSend.receiver.sourceEnd);
			return;
		case ParameterBoundMismatch :
			problemMethod = (ProblemMethodBinding) method;
			ParameterizedGenericMethodBinding substitutedMethod = (ParameterizedGenericMethodBinding) problemMethod.closestMatch;
			shownMethod = substitutedMethod.original();
			TypeBinding typeArgument = method.parameters[0];
			TypeVariableBinding typeParameter = (TypeVariableBinding) method.parameters[1];
			this.handle(
				IProblem.GenericMethodTypeArgumentMismatch,
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        parametersAsString(substitutedMethod.parameters, false), 
				        new String(typeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        parametersAsString(substitutedMethod.parameters, true), 
				        new String(typeArgument.shortReadableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);		    
			return;
		case TypeParameterArityMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			if (shownMethod.typeVariables == TypeConstants.NoTypeVariables) {
				this.handle(
					IProblem.NonGenericMethod ,
					new String[] { 
					        new String(shownMethod.selector),
					        parametersAsString(shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
					        parametersAsString(method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        parametersAsString(shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
					        parametersAsString(method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);		    
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedMethod  ,
					new String[] { 
					        new String(shownMethod.selector),
					        parametersAsString(shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
							parametersAsString(shownMethod.typeVariables, false),
					        parametersAsString(method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        parametersAsString(shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
							parametersAsString(shownMethod.typeVariables, true),
					        parametersAsString(method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);		    
			}
			return;
		case ParameterizedMethodTypeMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.ParameterizedMethodArgumentTypeMismatch,
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
						parametersAsString(((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
				        parametersAsString(method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
						parametersAsString(((ParameterizedGenericMethodBinding)shownMethod).typeArguments, true),
				        parametersAsString(method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);		    
			return;
		case TypeArgumentsForRawGenericMethod :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.TypeArgumentsForRawGenericMethod ,
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        parametersAsString(method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        parametersAsString(shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        parametersAsString(method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);		       
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		id,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(shownMethod.selector), parametersAsString(shownMethod.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(shownMethod.selector), parametersAsString(shownMethod.parameters, true)},
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
public void invalidParameterizedExceptionType(TypeBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.InvalidParameterizedExceptionType,
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidParenthesizedExpression(ASTNode reference) {
	this.handle(
		IProblem.InvalidParenthesizedExpression,
		NoArgument,
		NoArgument,
		reference.sourceStart,
		reference.sourceEnd);
}
public void invalidType(ASTNode location, TypeBinding type) {
	int id = IProblem.UndefinedType; // default
	switch (type.problemId()) {
		case NotFound :
			id = IProblem.UndefinedType;
			break;
		case NotVisible :
			id = IProblem.NotVisibleType;
			break;
		case Ambiguous :
			id = IProblem.AmbiguousType;
			break;
		case InternalNameProvided :
			id = IProblem.InternalTypeNameProvided;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.InheritedTypeHidesEnclosingName;
			break;
		case NonStaticReferenceInStaticContext :
			id = IProblem.TypeVariableReferenceFromStaticContext;
		    break;
		case IllegalSuperTypeVariable : 
		    id = IProblem.IllegalTypeVariableSuperReference;
		    break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	
	int end = location.sourceEnd;
	if (location instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) location;
		if (ref.indexOfFirstFieldBinding >= 1)
			end = (int) ref.sourcePositions[ref.indexOfFirstFieldBinding - 1];
	} else if (location instanceof QualifiedTypeReference) {
		QualifiedTypeReference ref = (QualifiedTypeReference) location;
		if (type instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) type).compoundName;
			end = (int) ref.sourcePositions[name.length - 1];
		}
	} else if (location instanceof ImportReference) {
		ImportReference ref = (ImportReference) location;
		if (type instanceof ReferenceBinding) {
			char[][] name = ((ReferenceBinding) type).compoundName;
			end = (int) ref.sourcePositions[name.length - 1];
		}
	}
	this.handle(
		id,
		new String[] {new String(type.readableName()) },	
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		end);
}
public void invalidTypeForCollection(Expression expression) {
	this.handle(
			IProblem.InvalidTypeForCollection,
			NoArgument,
			NoArgument,
			expression.sourceStart,
			expression.sourceEnd);
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
public void invalidTypeVariableAsException(TypeBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.InvalidTypeVariableExceptionType,
		new String[] {new String(exceptionType.readableName())},
		new String[] {new String(exceptionType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void invalidUnaryExpression(Expression expression) {
	this.handle(
		IProblem.InvalidUnaryExpression,
		NoArgument,
		NoArgument,
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidUsageOfAnnotation(Annotation annotation) {
	this.handle(
		IProblem.InvalidUsageOfAnnotations,
		NoArgument, 
		NoArgument,
		annotation.sourceStart,
		annotation.sourceEnd);	
}
public void invalidUsageOfAnnotationDeclarations(AnnotationTypeDeclaration annotationTypeDeclaration) {
	this.handle(
		IProblem.InvalidUsageOfAnnotationDeclarations,
		NoArgument, 
		NoArgument, 
		annotationTypeDeclaration.sourceStart,
		annotationTypeDeclaration.sourceEnd);
}
public void invalidUsageOfEnumDeclarations(EnumDeclaration enumDeclaration) {
	this.handle(
		IProblem.InvalidUsageOfEnumDeclarations,
		NoArgument, 
		NoArgument, 
		enumDeclaration.sourceStart,
		enumDeclaration.sourceEnd);
}
public void invalidUsageOfForeachStatements(LocalDeclaration elementVariable, Expression collection) {
	this.handle(
		IProblem.InvalidUsageOfForeachStatements,
		NoArgument, 
		NoArgument, 
		elementVariable.declarationSourceStart,
		collection.sourceEnd);
}
public void invalidUsageOfStaticImports(ImportReference staticImport) {
	this.handle(
		IProblem.InvalidUsageOfStaticImports,
		NoArgument, 
		NoArgument, 
		staticImport.declarationSourceStart,
		staticImport.declarationSourceEnd);
}
public void invalidUsageOfTypeArguments(TypeReference firstTypeReference, TypeReference lastTypeReference) {
	this.handle(
		IProblem.InvalidUsageOfTypeArguments,
		NoArgument, 
		NoArgument, 
		firstTypeReference.sourceStart,
		lastTypeReference.sourceEnd);
}
public void invalidUsageOfTypeParameters(TypeParameter firstTypeParameter, TypeParameter lastTypeParameter) {
	this.handle(
		IProblem.InvalidUsageOfTypeParameters,
		NoArgument, 
		NoArgument, 
		firstTypeParameter.declarationSourceStart,
		lastTypeParameter.declarationSourceEnd);
}
public void invalidUsageOfVarargs(Argument argument) {
	this.handle(
		IProblem.InvalidUsageOfVarargs,
		NoArgument, 
		NoArgument, 
		argument.type.sourceStart,
		argument.sourceEnd);
}
public void isClassPathCorrect(char[][] wellKnownTypeName, CompilationUnitDeclaration compUnitDecl) {
	this.referenceContext = compUnitDecl;
	String[] arguments = new String[] {CharOperation.toString(wellKnownTypeName)};
	this.handle(
		IProblem.IsClassPathCorrect,
		arguments, 
		arguments,
		AbortCompilation | Error,
		0,
		0);
}

private boolean isIdentifier(int token) {
	return token == TerminalTokens.TokenNameIdentifier;
}

private boolean isKeyword(int token) {
	switch(token) {
		case TerminalTokens.TokenNameabstract:
		case TerminalTokens.TokenNameassert:
		case TerminalTokens.TokenNamebyte:
		case TerminalTokens.TokenNamebreak:
		case TerminalTokens.TokenNameboolean:
		case TerminalTokens.TokenNamecase:
		case TerminalTokens.TokenNamechar:
		case TerminalTokens.TokenNamecatch:
		case TerminalTokens.TokenNameclass:
		case TerminalTokens.TokenNamecontinue:
		case TerminalTokens.TokenNamedo:
		case TerminalTokens.TokenNamedouble:
		case TerminalTokens.TokenNamedefault:
		case TerminalTokens.TokenNameelse:
		case TerminalTokens.TokenNameextends:
		case TerminalTokens.TokenNamefor:
		case TerminalTokens.TokenNamefinal:
		case TerminalTokens.TokenNamefloat:
		case TerminalTokens.TokenNamefalse:
		case TerminalTokens.TokenNamefinally:
		case TerminalTokens.TokenNameif:
		case TerminalTokens.TokenNameint:
		case TerminalTokens.TokenNameimport:
		case TerminalTokens.TokenNameinterface:
		case TerminalTokens.TokenNameimplements:
		case TerminalTokens.TokenNameinstanceof:
		case TerminalTokens.TokenNamelong:
		case TerminalTokens.TokenNamenew:
		case TerminalTokens.TokenNamenull:
		case TerminalTokens.TokenNamenative:
		case TerminalTokens.TokenNamepublic:
		case TerminalTokens.TokenNamepackage:
		case TerminalTokens.TokenNameprivate:
		case TerminalTokens.TokenNameprotected:
		case TerminalTokens.TokenNamereturn:
		case TerminalTokens.TokenNameshort:
		case TerminalTokens.TokenNamesuper:
		case TerminalTokens.TokenNamestatic:
		case TerminalTokens.TokenNameswitch:
		case TerminalTokens.TokenNamestrictfp:
		case TerminalTokens.TokenNamesynchronized:
		case TerminalTokens.TokenNametry:
		case TerminalTokens.TokenNamethis:
		case TerminalTokens.TokenNametrue:
		case TerminalTokens.TokenNamethrow:
		case TerminalTokens.TokenNamethrows:
		case TerminalTokens.TokenNametransient:
		case TerminalTokens.TokenNamevoid:
		case TerminalTokens.TokenNamevolatile:
		case TerminalTokens.TokenNamewhile:
			return true;
		default: 
			return false;
	}
}

private boolean isLiteral(int token) {
	switch(token) {
		case TerminalTokens.TokenNameIntegerLiteral:
		case TerminalTokens.TokenNameLongLiteral:
		case TerminalTokens.TokenNameFloatingPointLiteral:
		case TerminalTokens.TokenNameDoubleLiteral:
		case TerminalTokens.TokenNameStringLiteral:
		case TerminalTokens.TokenNameCharacterLiteral:
			return true;
		default: 
			return false;
	}
}
public void javadocAmbiguousMethodReference(int sourceStart, int sourceEnd, Binding fieldBinding, int modifiers) {
	int id = IProblem.JavadocAmbiguousMethodReference;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {new String(fieldBinding.readableName())};
		handle(id, arguments, arguments, sourceStart, sourceEnd);
	}
}
public void javadocDeprecatedField(FieldBinding field, ASTNode location, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(
			IProblem.JavadocUsingDeprecatedField,
			new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
			new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
			location.sourceStart,
			location.sourceEnd);
	}
}
public void javadocDeprecatedMethod(MethodBinding method, ASTNode location, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		if (method.isConstructor()) {
			this.handle(
				IProblem.JavadocUsingDeprecatedConstructor,
				new String[] {new String(method.declaringClass.readableName()), parametersAsString(method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), parametersAsString(method.parameters, true)},
				location.sourceStart,
				location.sourceEnd);
		} else {
			this.handle(
				IProblem.JavadocUsingDeprecatedMethod,
				new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
				location.sourceStart,
				location.sourceEnd);
		}
	}
}
public void javadocDeprecatedType(TypeBinding type, ASTNode location, int modifiers) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(
			IProblem.JavadocUsingDeprecatedType,
			new String[] {new String(type.readableName())},
			new String[] {new String(type.shortReadableName())},
			location.sourceStart,
			location.sourceEnd);
	}
}
public void javadocDuplicatedParamTag(JavadocSingleNameReference param, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(param.token)};
		this.handle(IProblem.JavadocDuplicateParamName, arguments, arguments, param.sourceStart, param.sourceEnd);
	}
}
public void javadocDuplicatedReturnTag(int sourceStart, int sourceEnd){
	this.handle(IProblem.JavadocDuplicateReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocDuplicatedThrowsClassName(TypeReference typeReference, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(typeReference.resolvedType.sourceName())};
		this.handle(IProblem.JavadocDuplicateThrowsClassName, arguments, arguments, typeReference.sourceStart, typeReference.sourceEnd);
	}
}
public void javadocEmptyReturnTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocEmptyReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocErrorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params, int modifiers) {
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

	int id = recType.isArrayType() ? IProblem.JavadocNoMessageSendOnArrayType : IProblem.JavadocNoMessageSendOnBaseType;
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		this.handle(
			id,
			new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
			new String[] {new String(recType.shortReadableName()), new String(messageSend.selector), shortBuffer.toString()},
			messageSend.sourceStart,
			messageSend.sourceEnd);
	}
}
public void javadocInvalidConstructor(Statement statement, MethodBinding targetConstructor, int modifiers) {

	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		return;
	}
//	boolean insideDefaultConstructor = 
//		(this.referenceContext instanceof ConstructorDeclaration)
//			&& ((ConstructorDeclaration)this.referenceContext).isDefaultConstructor();
//	boolean insideImplicitConstructorCall =
//		(statement instanceof ExplicitConstructorCall)
//			&& (((ExplicitConstructorCall) statement).accessMode == ExplicitConstructorCall.ImplicitSuper);

	int id = IProblem.JavadocUndefinedConstructor; //default...
	switch (targetConstructor.problemId()) {
		case NotFound :
//			if (insideDefaultConstructor){
//				id = IProblem.JavadocUndefinedConstructorInDefaultConstructor;
//			} else if (insideImplicitConstructorCall){
//				id = IProblem.JavadocUndefinedConstructorInImplicitConstructorCall;
//			} else {
				id = IProblem.JavadocUndefinedConstructor;
//			}
			break;
		case NotVisible :
//			if (insideDefaultConstructor){
//				id = IProblem.JavadocNotVisibleConstructorInDefaultConstructor;
//			} else if (insideImplicitConstructorCall){
//				id = IProblem.JavadocNotVisibleConstructorInImplicitConstructorCall;
//			} else {
				id = IProblem.JavadocNotVisibleConstructor;
//			}
			break;
		case Ambiguous :
//			if (insideDefaultConstructor){
//				id = IProblem.AmbiguousConstructorInDefaultConstructor;
//			} else if (insideImplicitConstructorCall){
//				id = IProblem.AmbiguousConstructorInImplicitConstructorCall;
//			} else {
				id = IProblem.JavadocAmbiguousConstructor;
//			}
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), parametersAsString(targetConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), parametersAsString(targetConstructor.parameters, true)},
		statement.sourceStart,
		statement.sourceEnd);
}
/*
 * Similar implementation than invalidField(FieldReference...)
 * Note that following problem id cannot occur for Javadoc:
 * 	- NonStaticReferenceInStaticContext :
 * 	- NonStaticReferenceInConstructorInvocation :
 * 	- ReceiverTypeNotVisible :
 */
public void javadocInvalidField(int sourceStart, int sourceEnd, Binding fieldBinding, TypeBinding searchedType, int modifiers) {
	int id = IProblem.JavadocUndefinedField;
	switch (fieldBinding.problemId()) {
		case NotFound :
			id = IProblem.JavadocUndefinedField;
			break;
		case NotVisible :
			id = IProblem.JavadocNotVisibleField;
			break;
		case Ambiguous :
			id = IProblem.JavadocAmbiguousField;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.JavadocInheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {new String(fieldBinding.readableName())};
		handle(id, arguments, arguments, sourceStart, sourceEnd);
	}
}
/*
 * Similar implementation than invalidMethod(MessageSend...)
 * Note that following problem id cannot occur for Javadoc:
 * 	- NonStaticReferenceInStaticContext :
 * 	- NonStaticReferenceInConstructorInvocation :
 * 	- ReceiverTypeNotVisible :
 */
public void javadocInvalidMethod(MessageSend messageSend, MethodBinding method, int modifiers) {
	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		return;
	}
	int id = IProblem.JavadocUndefinedMethod; //default...
	switch (method.problemId()) {
		case NotFound :
			id = IProblem.JavadocUndefinedMethod;
			break;
		case NotVisible :
			id = IProblem.JavadocNotVisibleMethod;
			break;
		case Ambiguous :
			id = IProblem.JavadocAmbiguousMethod;
			break;
		case InheritedNameHidesEnclosingName :
			id = IProblem.JavadocInheritedMethodHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	if (id == IProblem.JavadocUndefinedMethod) {
		ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
		if (problemMethod.closestMatch != null) {
				String closestParameterTypeNames = parametersAsString(problemMethod.closestMatch.parameters, false);
				String parameterTypeNames = parametersAsString(method.parameters, false);
				String closestParameterTypeShortNames = parametersAsString(problemMethod.closestMatch.parameters, true);
				String parameterTypeShortNames = parametersAsString(method.parameters, true);
				if (closestParameterTypeShortNames.equals(parameterTypeShortNames)){
					closestParameterTypeShortNames = closestParameterTypeNames;
					parameterTypeShortNames = parameterTypeNames;
				}
				this.handle(
					IProblem.JavadocParameterMismatch,
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
		id,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector), parametersAsString(method.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector), parametersAsString(method.parameters, true)},
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void javadocInvalidParamName(JavadocSingleNameReference param, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(param.token)};
		this.handle(IProblem.JavadocInvalidParamName, arguments, arguments, param.sourceStart, param.sourceEnd);
	}
}
public void javadocInvalidReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidSeeReferenceArgs(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeArgs, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidSeeUrlReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeHref, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidThrowsClass(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidThrowsClass, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidThrowsClassName(TypeReference typeReference, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(typeReference.resolvedType.sourceName())};
		this.handle(IProblem.JavadocInvalidThrowsClassName, arguments, arguments, typeReference.sourceStart, typeReference.sourceEnd);
	}
}
public void javadocInvalidType(ASTNode location, TypeBinding type, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		int id = IProblem.JavadocUndefinedType; // default
		switch (type.problemId()) {
			case NotFound :
				id = IProblem.JavadocUndefinedType;
				break;
			case NotVisible :
				id = IProblem.JavadocNotVisibleType;
				break;
			case Ambiguous :
				id = IProblem.JavadocAmbiguousType;
				break;
			case InternalNameProvided :
				id = IProblem.JavadocInternalTypeNameProvided;
				break;
			case InheritedNameHidesEnclosingName :
				id = IProblem.JavadocInheritedNameHidesEnclosingTypeName;
				break;
			case NoError : // 0
			default :
				needImplementation(); // want to fail to see why we were here...
				break;
		}
		this.handle(
			id,
			new String[] {new String(type.readableName())},
			new String[] {new String(type.shortReadableName())},
			location.sourceStart,
			location.sourceEnd);
	}
}
public void javadocInvalidValueReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidValueReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMalformedSeeReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocMalformedSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissing(int sourceStart, int sourceEnd, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing+CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocComments) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocCommentsOverriding);
	if (report) {
		String arg = javadocVisibilityArgument(this.options.reportMissingJavadocCommentsVisibility, modifiers);
		if (arg != null) {
			String[] arguments = new String[] { arg };
			this.handle(IProblem.JavadocMissing, arguments, arguments, sourceStart, sourceEnd);
		}
	}
}
public void javadocMissingHashCharacter(int sourceStart, int sourceEnd, String ref){
	String[] arguments = new String[] { ref };
	this.handle(IProblem.JavadocMissingHashCharacter, arguments, arguments, sourceStart, sourceEnd);
}
public void javadocMissingParamName(int sourceStart, int sourceEnd){
	this.handle(IProblem.JavadocMissingParamName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingParamTag(Argument param, int modifiers) {
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing+CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(param.name) };
		this.handle(IProblem.JavadocMissingParamTag, arguments, arguments, param.sourceStart, param.sourceEnd);
	}
}
public void javadocMissingReference(int sourceStart, int sourceEnd){
	this.handle(IProblem.JavadocMissingReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingReturnTag(int sourceStart, int sourceEnd, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing+CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocMissingReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
	}
}
public void javadocMissingThrowsClassName(int sourceStart, int sourceEnd){
	this.handle(IProblem.JavadocMissingThrowsClassName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingThrowsTag(TypeReference typeRef, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing+CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(typeRef.resolvedType.sourceName()) };
		this.handle(IProblem.JavadocMissingThrowsTag, arguments, arguments, typeRef.sourceStart, typeRef.sourceEnd);
	}
}
public void javadocUnexpectedTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnexpectedTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocUnexpectedText(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnexpectedText, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocUnterminatedInlineTag(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocUnterminatedInlineTag, NoArgument, NoArgument, sourceStart, sourceEnd);
}
private boolean javadocVisibility(int visibility, int modifiers) {
	switch (modifiers & CompilerModifiers.AccVisibilityMASK) {
		case IConstants.AccPublic :
			return true;
		case IConstants.AccProtected:
			return (visibility != IConstants.AccPublic);
		case IConstants.AccDefault:
			return (visibility == IConstants.AccDefault || visibility == IConstants.AccPrivate);
		case IConstants.AccPrivate:
			return (visibility == IConstants.AccPrivate);
	}
	return true;
}
private String javadocVisibilityArgument(int visibility, int modifiers) {
	String argument = null;
	switch (modifiers & CompilerModifiers.AccVisibilityMASK) {
		case IConstants.AccPublic :
			argument = CompilerOptions.PUBLIC;
			break;
		case IConstants.AccProtected:
			if (visibility != IConstants.AccPublic) {
				argument = CompilerOptions.PROTECTED;
			}
			break;
		case IConstants.AccDefault:
			if (visibility == IConstants.AccDefault || visibility == IConstants.AccPrivate) {
				argument = CompilerOptions.DEFAULT;
			}
			break;
		case IConstants.AccPrivate:
			if (visibility == IConstants.AccPrivate) {
				argument = CompilerOptions.PRIVATE;
			}
			break;
	}
	return argument;
}
public void localVariableHiding(LocalDeclaration local, Binding hiddenVariable, boolean  isSpecialArgHidingField) {
	if (hiddenVariable instanceof LocalVariableBinding) {
		String[] arguments = new String[] {new String(local.name)  };
		this.handle(
			(local instanceof Argument) 
				? IProblem.ArgumentHidingLocalVariable 
				: IProblem.LocalVariableHidingLocalVariable,
			arguments,
			arguments,
			local.sourceStart,
			local.sourceEnd);
	} else if (hiddenVariable instanceof FieldBinding) {
		if (isSpecialArgHidingField && !this.options.reportSpecialParameterHidingField){
			return;
		}
		FieldBinding field = (FieldBinding) hiddenVariable;
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
public void methodNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {

	this.handle(
			IProblem.MethodNameClash,
			new String[] {
				new String(currentMethod.selector),
				parametersAsString(currentMethod.original().parameters, false),
				new String(currentMethod.declaringClass.readableName()),
				parametersAsString(inheritedMethod.original().parameters, false),
				new String(inheritedMethod.declaringClass.readableName()),
			 }, 
			new String[] {
				new String(currentMethod.selector),
				parametersAsString(currentMethod.original().parameters, true),
				new String(currentMethod.declaringClass.shortReadableName()),
				parametersAsString(inheritedMethod.original().parameters, true),
				new String(inheritedMethod.declaringClass.shortReadableName()),
			 }, 
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}	

public void methodNeedBody(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.MethodRequiresBody,
		NoArgument,
		NoArgument,
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodNeedingNoBody(MethodDeclaration methodDecl) {
	this.handle(
		((methodDecl.modifiers & IConstants.AccNative) != 0) ? IProblem.BodyForNativeMethod : IProblem.BodyForAbstractMethod,
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
public void parameterizedMemberTypeMissingArguments(ASTNode location, TypeBinding type) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.MissingArgumentsForParameterizedMemberType,
			new String[] {new String(type.readableName())},
			new String[] {new String(type.shortReadableName())},
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.MissingArgumentsForParameterizedMemberType,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
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
public void missingSerialVersion(TypeDeclaration typeDecl) {
	String[] arguments = new String[] {new String(typeDecl.name)};
	this.handle(
		IProblem.MissingSerialVersion,
		arguments,
		arguments,
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
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
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
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
public void needToEmulateFieldAccess(FieldBinding field, ASTNode location, boolean isReadAccess) {
	this.handle(
		isReadAccess 
			? IProblem.NeedToEmulateFieldReadAccess
			: IProblem.NeedToEmulateFieldWriteAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateMethodAccess(
	MethodBinding method, 
	ASTNode location) {

	if (method.isConstructor())
		this.handle(
			IProblem.NeedToEmulateConstructorAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				parametersAsString(method.parameters, false)
			 }, 
			new String[] {
				new String(method.declaringClass.shortReadableName()), 
				parametersAsString(method.parameters, true)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
	else
		this.handle(
			IProblem.NeedToEmulateMethodAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				new String(method.selector), 
				parametersAsString(method.parameters, false)
			 }, 
			new String[] {
				new String(method.declaringClass.shortReadableName()), 
				new String(method.selector), 
				parametersAsString(method.parameters, true)
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
public void noMoreAvailableSpaceForArgument(LocalVariableBinding local, ASTNode location) {
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

public void noMoreAvailableSpaceForConstant(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.TooManyBytesForStringConstant,
		new String[]{ new String(typeDeclaration.binding.readableName())},
		new String[]{ new String(typeDeclaration.binding.shortReadableName())},
		Abort | Error,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void noMoreAvailableSpaceForLocal(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[]{ new String(local.name) };
	this.handle(
		IProblem.TooManyLocalVariableSlots,
		arguments,
		arguments,
		Abort | Error,
		location.sourceStart,
		location.sourceEnd);
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
public void nonExternalizedStringLiteral(ASTNode location) {
	this.handle(
		IProblem.NonExternalizedStringLiteral,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void nonGenericTypeCannotBeParameterized(ASTNode location, TypeBinding type, TypeBinding[] argumentTypes) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.NonGenericType,
			new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true)},
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.NonGenericType,
		new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void nonStaticAccessToStaticField(ASTNode location, FieldBinding field) {
	this.handle(
		IProblem.NonStaticAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		location.sourceStart,
		fieldLocation(field, location));
}
public void nonStaticAccessToStaticMethod(ASTNode location, MethodBinding method) {
	this.handle(
		IProblem.NonStaticAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), parametersAsString(method.parameters, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void noSuchEnclosingInstance(TypeBinding targetType, ASTNode location, boolean isConstructorCall) {

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
public void notCompatibleTypesErrorInForeach(Expression expression, TypeBinding leftType, TypeBinding rightType) {
	String leftName = new String(leftType.readableName());
	String rightName = new String(rightType.readableName());
	String leftShortName = new String(leftType.shortReadableName());
	String rightShortName = new String(rightType.shortReadableName());
	if (leftShortName.equals(rightShortName)){
		leftShortName = leftName;
		rightShortName = rightName;
	}
	this.handle(
		IProblem.IncompatibleTypesInForeach,
		new String[] {leftName, rightName },
		new String[] {leftShortName, rightShortName },
		expression.sourceStart,
		expression.sourceEnd);
}
public void objectCannotBeGeneric(TypeDeclaration typeDecl) {
	this.handle(
		IProblem.ObjectCannotBeGeneric,
		NoArgument,
		NoArgument,
		typeDecl.typeParameters[0].sourceStart,
		typeDecl.typeParameters[typeDecl.typeParameters.length-1].sourceEnd);
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
private String parameterBoundAsString(TypeVariableBinding typeVariable, boolean makeShort) {
    StringBuffer nameBuffer = new StringBuffer(10);
    if (typeVariable.firstBound == typeVariable.superclass) {
        nameBuffer.append(makeShort ? typeVariable.superclass.shortReadableName() : typeVariable.superclass.readableName());
    }
    int length;
    if ((length = typeVariable.superInterfaces.length) > 0) {
	    for (int i = 0; i < length; i++) {
	        if (i > 0 || typeVariable.firstBound == typeVariable.superclass) nameBuffer.append(" & "); //$NON-NLS-1$
	        nameBuffer.append(makeShort ? typeVariable.superInterfaces[i].shortReadableName() : typeVariable.superInterfaces[i].readableName());
	    }
	}
	return nameBuffer.toString();
}
private String parametersAsString(TypeBinding[] params, boolean makeShort) {
	StringBuffer buffer = new StringBuffer(10);
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		buffer.append(new String(makeShort ? params[i].shortReadableName() : params[i].readableName()));
	}
	return buffer.toString();
}
public void parseError(
	int startPosition, 
	int endPosition, 
	int currentToken,
	char[] currentTokenSource, 
	String errorTokenName, 
	String[] possibleTokens) {
		
	if (possibleTokens.length == 0) { //no suggestion available
		if (isKeyword(currentToken)) {
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

	if (isKeyword(currentToken)) {
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
	if (isLiteral(currentToken) ||
		isIdentifier(currentToken)) { //$NON-NLS-1$
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
public void parseErrorDeleteToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName){
	this.syntaxError(
		IProblem.ParsingErrorDeleteToken,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName,
		null); 
}
public void parseErrorDeleteTokens(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorDeleteTokens,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorInsertAfterToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	this.syntaxError(
		IProblem.ParsingErrorInsertTokenAfter,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName, 
		expectedToken); 
}

public void parseErrorInsertBeforeToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	this.syntaxError(
		IProblem.ParsingErrorInsertTokenBefore,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName, 
		expectedToken); 
}
public void parseErrorInsertToComplete(
	int start,
	int end,
	String inserted,
	String completed){
	String[] arguments = new String[] {inserted, completed};
	this.handle(
		IProblem.ParsingErrorInsertToComplete,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorInsertToCompletePhrase(
	int start,
	int end,
	String inserted){
	String[] arguments = new String[] {inserted};
	this.handle(
		IProblem.ParsingErrorInsertToCompletePhrase,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorInsertToCompleteScope(
	int start,
	int end,
	String inserted){
	String[] arguments = new String[] {inserted};
	this.handle(
		IProblem.ParsingErrorInsertToCompleteScope,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorInvalidToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	this.syntaxError(
		IProblem.ParsingErrorInvalidToken,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName, 
		expectedToken); 
}
public void parseErrorMergeTokens(
	int start,
	int end,
	String expectedToken){
	String[] arguments = new String[] {expectedToken};
	this.handle(
		IProblem.ParsingErrorMergeTokens,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorMisplacedConstruct(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorMisplacedConstruct,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorNoSuggestion(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName){
	this.syntaxError(
		IProblem.ParsingErrorNoSuggestion,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName,
		null); 
}
public void parseErrorNoSuggestionForTokens(
	int start,
	int end){
	this.handle(
		IProblem.ParsingErrorNoSuggestionForTokens,
		NoArgument,
		NoArgument,
		start,
		end);
}
public void parseErrorReplaceToken(
	int start,
	int end,
	int currentKind,
	char[] errorTokenSource,
	String errorTokenName,
	String expectedToken){
	this.syntaxError(
		IProblem.ParsingError,
		start, 
		end, 
		currentKind,
		errorTokenSource, 
		errorTokenName, 
		expectedToken); 
}
public void parseErrorReplaceTokens(
	int start,
	int end,
	String expectedToken){
	String[] arguments = new String[] {expectedToken};
	this.handle(
		IProblem.ParsingErrorReplaceTokens,
		arguments,
		arguments,
		start,
		end);
}
public void parseErrorUnexpectedEnd(
	int start,
	int end){
		
	String[] arguments;
	if(this.referenceContext instanceof ConstructorDeclaration) {
		arguments = new String[] {Util.bind("parser.endOfConstructor")}; //$NON-NLS-1$
	} else if(this.referenceContext instanceof MethodDeclaration) {
		arguments = new String[] {Util.bind("parser.endOfMethod")}; //$NON-NLS-1$
	} else if(this.referenceContext instanceof TypeDeclaration) {
		arguments = new String[] {Util.bind("parser.endOfInitializer")}; //$NON-NLS-1$
	} else {
		arguments = new String[] {Util.bind("parser.endOfFile")}; //$NON-NLS-1$
	}
	this.handle(
		IProblem.ParsingErrorUnexpectedEOF,
		arguments,
		arguments,
		start,
		end);
}
public void possibleAccidentalBooleanAssignment(Assignment assignment) {
	this.handle(
		IProblem.PossibleAccidentalBooleanAssignment,
		NoArgument,
		NoArgument,
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
public void rawMemberTypeCannotBeParameterized(ASTNode location, ReferenceBinding type, TypeBinding[] argumentTypes) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.RawMemberTypeCannotBeParameterized,
			new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false), new String(type.enclosingType().readableName())},
			new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true), new String(type.enclosingType().shortReadableName())},
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.RawMemberTypeCannotBeParameterized,
		new String[] {new String(type.readableName()), parametersAsString(argumentTypes, false), new String(type.enclosingType().readableName())},
		new String[] {new String(type.shortReadableName()), parametersAsString(argumentTypes, true), new String(type.enclosingType().shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void recursiveConstructorInvocation(ExplicitConstructorCall constructorCall) {

	this.handle(
		IProblem.RecursiveConstructorInvocation,
		new String[] {
			new String(constructorCall.binding.declaringClass.readableName()), 
			parametersAsString(constructorCall.binding.parameters, false)
		},
		new String[] {
			new String(constructorCall.binding.declaringClass.shortReadableName()), 
			parametersAsString(constructorCall.binding.parameters, true)
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

public void scannerError(Parser parser, String errorTokenName) {
	Scanner scanner = parser.scanner;

	int flag = IProblem.ParsingErrorNoSuggestion;
	int startPos = scanner.startPosition;

	//special treatment for recognized errors....
	if (errorTokenName.equals(Scanner.END_OF_SOURCE))
		flag = IProblem.EndOfSource;
	else if (errorTokenName.equals(Scanner.INVALID_HEXA))
		flag = IProblem.InvalidHexa;
	else if (errorTokenName.equals(Scanner.INVALID_OCTAL))
		flag = IProblem.InvalidOctal;
	else if (errorTokenName.equals(Scanner.INVALID_CHARACTER_CONSTANT))
		flag = IProblem.InvalidCharacterConstant;
	else if (errorTokenName.equals(Scanner.INVALID_ESCAPE))
		flag = IProblem.InvalidEscape;
	else if (errorTokenName.equals(Scanner.INVALID_UNICODE_ESCAPE)){
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
	} else if (errorTokenName.equals(Scanner.INVALID_FLOAT))
		flag = IProblem.InvalidFloat;
	else if (errorTokenName.equals(Scanner.UNTERMINATED_STRING))
		flag = IProblem.UnterminatedString;
	else if (errorTokenName.equals(Scanner.UNTERMINATED_COMMENT))
		flag = IProblem.UnterminatedComment;
	else if (errorTokenName.equals(Scanner.INVALID_CHAR_IN_STRING))
		flag = IProblem.UnterminatedString;
	else if (errorTokenName.equals(Scanner.INVALID_DIGIT))
		flag = IProblem.InvalidDigit;

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
public void shouldReturn(TypeBinding returnType, ASTNode location) {
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
public void staticFieldAccessToNonStaticVariable(ASTNode location, FieldBinding field) {
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.NonStaticFieldFromStaticInvocation,
		arguments,
		arguments,
		location.sourceStart,
		fieldLocation(field, location)); 
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
public void staticMemberOfParameterizedType(ASTNode location, ReferenceBinding type) {
	if (location == null) { // binary case
	    this.handle(
			IProblem.StaticMemberOfParameterizedType,
			new String[] {new String(type.readableName()), new String(type.enclosingType().readableName()), },
			new String[] {new String(type.shortReadableName()), new String(type.enclosingType().shortReadableName()), },
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.StaticMemberOfParameterizedType,
		new String[] {new String(type.readableName()), new String(type.enclosingType().readableName()), },
		new String[] {new String(type.shortReadableName()), new String(type.enclosingType().shortReadableName()), },
		location.sourceStart,
		location.sourceEnd);
}
public void stringConstantIsExceedingUtf8Limit(ASTNode location) {
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
public void superfluousSemicolon(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.SuperfluousSemicolon,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);	
}
public void superinterfaceMustBeAnInterface(SourceTypeBinding type, TypeReference superInterfaceRef, ReferenceBinding superType) {
	this.handle(
		IProblem.SuperInterfaceMustBeAnInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superInterfaceRef.sourceStart,
		superInterfaceRef.sourceEnd);
}
public void superinterfacesCollide(ReferenceBinding type, TypeDeclaration typeDecl, ReferenceBinding superType, ReferenceBinding inheritedSuperType) {
	this.handle(
		IProblem.SuperInterfacesCollide,
		new String[] {new String(superType.readableName()), new String(inheritedSuperType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(inheritedSuperType.shortReadableName()), new String(type.sourceName())},
		typeDecl.sourceStart,
		typeDecl.sourceEnd);
}
public void superTypeCannotUseWildcard(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.SuperTypeUsingWildcard,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
}

private void syntaxError(
	int id,
	int startPosition, 
	int endPosition, 
	int currentKind,
	char[] currentTokenSource, 
	String errorTokenName, 
	String expectedToken) {

	String eTokenName;
	if (isKeyword(currentKind) ||
		isLiteral(currentKind) ||
		isIdentifier(currentKind)) { //$NON-NLS-1$
			eTokenName = new String(currentTokenSource);
	} else {
		eTokenName = errorTokenName;
	}

	String[] arguments;
	if(expectedToken != null) {
		arguments = new String[] {eTokenName, expectedToken};
	} else {
		arguments = new String[] {eTokenName};
	}
	this.handle(
		id,
		arguments,
		arguments,
		startPosition,
		endPosition);
}

public void task(String tag, String message, String priority, int start, int end){
	this.handle(
		IProblem.Task,
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/},
		new String[] { tag, message, priority/*secret argument that is not surfaced in getMessage()*/}, 
		start,
		end);
}
public void tooManyDimensions(ASTNode expression) {
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
public void typeMismatchError(TypeBinding actualType, TypeBinding expectedType, ASTNode location) {
	this.handle(
		IProblem.TypeMismatch,
		new String[] {new String(actualType.readableName()), new String(expectedType.readableName())},
		new String[] {new String(actualType.shortReadableName()), new String(expectedType.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void typeMismatchError(TypeBinding typeArgument, TypeVariableBinding typeParameter, ReferenceBinding genericType, ASTNode location) {
    if (location == null) { // binary case
		this.handle(
			IProblem.TypeArgumentMismatch,
			new String[] { new String(typeArgument.readableName()), new String(genericType.readableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, false) },
			new String[] { new String(typeArgument.shortReadableName()), new String(genericType.shortReadableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, true) },
			AbortCompilation | Error,
			0,
			1);
        return;
    }
	this.handle(
		IProblem.TypeArgumentMismatch,
		new String[] { new String(typeArgument.readableName()), new String(genericType.readableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, false) },
		new String[] { new String(typeArgument.shortReadableName()), new String(genericType.shortReadableName()), new String(typeParameter.sourceName), parameterBoundAsString(typeParameter, true) },
		location.sourceStart,
		location.sourceEnd);
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
// can only occur inside binaries
public void undefinedTypeVariableSignature(char[] variableName, ReferenceBinding binaryType) {
	this.handle(
		IProblem.UndefinedTypeVariable,
		new String[] {new String(variableName), new String(binaryType.readableName()) },	
		new String[] {new String(variableName), new String(binaryType.shortReadableName())},
		AbortCompilation | Error,
		0,
		1);
}
public void undocumentedEmptyBlock(int blockStart, int blockEnd) {
	this.handle(
		IProblem.UndocumentedEmptyBlock,
		NoArgument,
		NoArgument,
		blockStart,
		blockEnd);
}
public void unexpectedStaticModifierForField(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
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
public void unhandledException(TypeBinding exceptionType, ASTNode location) {

	boolean insideDefaultConstructor = 
		(this.referenceContext instanceof ConstructorDeclaration)
			&& ((ConstructorDeclaration)this.referenceContext).isDefaultConstructor();
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
public void uninitializedBlankFinalField(FieldBinding binding, ASTNode location) {
	String[] arguments = new String[] {new String(binding.readableName())};
	this.handle(
		IProblem.UninitializedBlankFinalField,
		arguments,
		arguments,
		location.sourceStart,
		fieldLocation(binding, location));
}
public void uninitializedLocalVariable(LocalVariableBinding binding, ASTNode location) {
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
public void unnecessaryCast(CastExpression castExpression) {
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	this.handle(
		IProblem.UnnecessaryCast,
		new String[]{ new String(castedExpressionType.readableName()), new String(castExpression.resolvedType.readableName())},
		new String[]{ new String(castedExpressionType.shortReadableName()), new String(castExpression.resolvedType.shortReadableName())},
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unnecessaryCastForArgument(CastExpression castExpression, TypeBinding parameterType) {
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	this.handle(
		IProblem.UnnecessaryArgumentCast,
		new String[]{ new String(castedExpressionType.readableName()), new String(castExpression.resolvedType.readableName()), new String(parameterType.readableName())},
		new String[]{ new String(castedExpressionType.shortReadableName()), new String(castExpression.resolvedType.shortReadableName()), new String(parameterType.shortReadableName())},
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unnecessaryElse(ASTNode location) {
	this.handle(
		IProblem.UnnecessaryElse,
		NoArgument,
		NoArgument,
		location.sourceStart,
		location.sourceEnd);
}
public void unnecessaryEnclosingInstanceSpecification(Expression expression, ReferenceBinding targetType) {
	this.handle(
		IProblem.IllegalEnclosingInstanceSpecification,
		new String[]{ new String(targetType.readableName())},
		new String[]{ new String(targetType.shortReadableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void unnecessaryInstanceof(InstanceOfExpression instanceofExpression, TypeBinding checkType) {
	TypeBinding expressionType = instanceofExpression.expression.resolvedType;
	this.handle(
		IProblem.UnnecessaryInstanceof,
		new String[]{ new String(expressionType.readableName()), new String(checkType.readableName())},
		new String[]{ new String(expressionType.shortReadableName()), new String(checkType.shortReadableName())},
		instanceofExpression.sourceStart,
		instanceofExpression.sourceEnd);
}
public void unqualifiedFieldAccess(NameReference reference, FieldBinding field) {
	int end = reference.sourceEnd;
	if (reference instanceof QualifiedNameReference) {
		QualifiedNameReference qref = (QualifiedNameReference) reference;
		end = (int) qref.sourcePositions[0];
	}
	this.handle(
		IProblem.UnqualifiedFieldAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		reference.sourceStart,
		end);
}
public void unreachableCatchBlock(ReferenceBinding exceptionType, ASTNode location) {
	this.handle(
		IProblem.UnreachableCatch,
		new String[] {
			new String(exceptionType.readableName()),
		 }, 
		new String[] {
			new String(exceptionType.shortReadableName()),
		 }, 
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
	int end = nameRef.sourceEnd;
	if (nameRef instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) nameRef;
		if (ref.indexOfFirstFieldBinding >= 1)
			end = (int) ref.sourcePositions[ref.indexOfFirstFieldBinding - 1];
	}
	this.handle(
		IProblem.UndefinedName,
		arguments,
		arguments,
		severity,
		nameRef.sourceStart,
		end);
}
public void unsafeCast(CastExpression castExpression) {
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	this.handle(
		IProblem.UnsafeGenericCast,
		new String[]{ new String(castedExpressionType.readableName()), new String(castExpression.resolvedType.readableName())},
		new String[]{ new String(castedExpressionType.shortReadableName()), new String(castExpression.resolvedType.shortReadableName())},
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unsafeRawConversion(Expression expression, TypeBinding expressionType, TypeBinding expectedType) {
	this.handle(
		IProblem.UnsafeRawConversion,
		new String[] { new String(expressionType.readableName()), new String(expectedType.readableName()), new String(expectedType.erasure().readableName()) },
		new String[] { new String(expressionType.shortReadableName()), new String(expectedType.shortReadableName()), new String(expectedType.erasure().shortReadableName()) },
		expression.sourceStart,
		expression.sourceEnd);    
}
public void unsafeRawFieldAssignment(FieldBinding rawField, TypeBinding expressionType, ASTNode location) {
	this.handle(
		IProblem.UnsafeRawFieldAssignment,
		new String[] { 
		        new String(expressionType.readableName()), new String(rawField.name), new String(rawField.declaringClass.readableName()), new String(rawField.declaringClass.erasure().readableName()) },
		new String[] { 
		        new String(expressionType.shortReadableName()), new String(rawField.name), new String(rawField.declaringClass.shortReadableName()), new String(rawField.declaringClass.erasure().shortReadableName()) },
		location.sourceStart,
		location.sourceEnd);    
}
public void unsafeRawInvocation(ASTNode location, TypeBinding receiverType, MethodBinding method) {
    if (method.isConstructor()) {
		this.handle(
			IProblem.UnsafeRawConstructorInvocation,
			new String[] {
				new String(receiverType.readableName()),
				parametersAsString(method.original().parameters, false),
				new String(receiverType.erasure().readableName()),
			 }, 
			new String[] {
				new String(receiverType.shortReadableName()),
				parametersAsString(method.original().parameters, true),
				new String(receiverType.erasure().shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    } else {
		this.handle(
			IProblem.UnsafeRawMethodInvocation,
			new String[] {
				new String(method.selector),
				parametersAsString(method.original().parameters, false),
				new String(receiverType.readableName()),
				new String(receiverType.erasure().readableName()),
			 }, 
			new String[] {
				new String(method.selector),
				parametersAsString(method.original().parameters, true),
				new String(receiverType.shortReadableName()),
				new String(receiverType.erasure().shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    }
}
public void unsafeReturnTypeOverride(MethodBinding currentMethod, MethodBinding inheritedMethod, ASTNode location) {
	
	this.handle(
			IProblem.UnsafeReturnTypeOverride,
			new String[] {
				new String(currentMethod.returnType.readableName()),
				new String(currentMethod.selector),
				parametersAsString(currentMethod.original().parameters, false),
				new String(currentMethod.declaringClass.readableName()),
				new String(inheritedMethod.returnType.readableName()),
				//new String(inheritedMethod.returnType.erasure().readableName()),
			 }, 
			new String[] {
				new String(currentMethod.returnType.shortReadableName()),
				new String(currentMethod.selector),
				parametersAsString(currentMethod.original().parameters, true),
				new String(currentMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.returnType.shortReadableName()),
				//new String(inheritedMethod.returnType.erasure().shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);
}
public void unusedArgument(LocalDeclaration localDecl) {

	String[] arguments = new String[] {new String(localDecl.name)};
	this.handle(
		IProblem.ArgumentIsNeverUsed,
		arguments,
		arguments,
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedDeclaredThrownException(ReferenceBinding exceptionType, AbstractMethodDeclaration method, ASTNode location) {
	if (method.isConstructor()) {
		this.handle(
			IProblem.UnusedConstructorDeclaredThrownException,
			new String[] {
				new String(method.binding.declaringClass.readableName()),
				parametersAsString(method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 }, 
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				parametersAsString(method.binding.parameters, true),
				new String(exceptionType.shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.UnusedMethodDeclaredThrownException,
			new String[] {
				new String(method.binding.declaringClass.readableName()),
				new String(method.selector),
				parametersAsString(method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 }, 
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				new String(method.selector),
				parametersAsString(method.binding.parameters, true),
				new String(exceptionType.shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);
	}
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
	String[] arguments = new String[] {new String(localDecl.name)};
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
			parametersAsString(constructor.parameters, false)
		 }, 
		new String[] {
			new String(constructor.declaringClass.shortReadableName()),
			parametersAsString(constructor.parameters, true)
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
			&& BaseTypes.LongBinding == field.type) {
				return; // do not report unused serialVersionUID field
	}
	if (CharOperation.equals(TypeConstants.SERIALPERSISTENTFIELDS, field.name)
			&& field.isStatic()
			&& field.isFinal()
			&& field.type.dimensions() == 1
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTSTREAMFIELD, field.type.leafComponentType().readableName())) {
				return; // do not report unused serialPersistentFields field
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
			&& BaseTypes.VoidBinding == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.READOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTINPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'void writeObject(ObjectOutputStream)'
	if (!method.isStatic()
			&& BaseTypes.VoidBinding == method.returnType
			&& method.parameters.length == 1
			&& method.parameters[0].dimensions() == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEOBJECT)
			&& CharOperation.equals(TypeConstants.CharArray_JAVA_IO_OBJECTOUTPUTSTREAM, method.parameters[0].readableName())) {
		return;
	}
	// no report for serialization support 'Object readResolve()'
	if (!method.isStatic()
			&& TypeIds.T_Object == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.READRESOLVE)) {
		return;
	}
	// no report for serialization support 'Object writeReplace()'
	if (!method.isStatic()
			&& TypeIds.T_Object == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEREPLACE)) {
		return;
	}
	this.handle(
			IProblem.UnusedPrivateMethod,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector),
			parametersAsString(method.parameters, false)
		 }, 
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector),
			parametersAsString(method.parameters, true)
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
public void useEnumAsAnIdentifier(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UseEnumAsAnIdentifier,
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
public void wildcardAssignment(TypeBinding variableType, TypeBinding expressionType, ASTNode location) {
	this.handle(
		IProblem.WildcardFieldAssignment,
		new String[] { 
		        new String(expressionType.readableName()), new String(variableType.readableName()) },
		new String[] { 
		        new String(expressionType.shortReadableName()), new String(variableType.shortReadableName()) },
		location.sourceStart,
		location.sourceEnd);    
}
public void wildcardInvocation(ASTNode location, TypeBinding receiverType, MethodBinding method, TypeBinding[] arguments) {
	TypeBinding offendingArgument = null;
	TypeBinding offendingParameter = null;
	for (int i = 0, length = method.parameters.length; i < length; i++) {
		TypeBinding parameter = method.parameters[i];
		if (parameter.isWildcard() && (((WildcardBinding) parameter).kind != Wildcard.SUPER)) {
			offendingParameter = parameter;
			offendingArgument = arguments[i];
			break;
		}
	}
	
    if (method.isConstructor()) {
		this.handle(
			IProblem.WildcardConstructorInvocation,
			new String[] {
				new String(receiverType.sourceName()),
				parametersAsString(method.parameters, false),
				new String(receiverType.readableName()),
				parametersAsString(arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 }, 
			new String[] {
				new String(receiverType.sourceName()),
				parametersAsString(method.parameters, true),
				new String(receiverType.shortReadableName()),
				parametersAsString(arguments, true),
				new String(offendingArgument.shortReadableName()),
				new String(offendingParameter.shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    } else {
		this.handle(
			IProblem.WildcardMethodInvocation,
			new String[] {
				new String(method.selector),
				parametersAsString(method.parameters, false),
				new String(receiverType.readableName()),
				parametersAsString(arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 }, 
			new String[] {
				new String(method.selector),
				parametersAsString(method.parameters, true),
				new String(receiverType.shortReadableName()),
				parametersAsString(arguments, true),
				new String(offendingArgument.shortReadableName()),
				new String(offendingParameter.shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    }
}
public void wrongSequenceOfExceptionTypesError(TryStatement statement, TypeBinding exceptionType, int under, TypeBinding hidingExceptionType) {
	//the two catch block under and upper are in an incorrect order.
	//under should be define BEFORE upper in the source

	TypeReference typeRef = statement.catchArguments[under].type;
	this.handle(
		IProblem.InvalidCatchBlockSequence,
		new String[] {
			new String(exceptionType.readableName()),
			new String(hidingExceptionType.readableName()),
		 }, 
		new String[] {
			new String(exceptionType.shortReadableName()),
			new String(hidingExceptionType.shortReadableName()),
		 }, 
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
}

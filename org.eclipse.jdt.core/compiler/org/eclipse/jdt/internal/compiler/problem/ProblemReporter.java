/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import java.text.MessageFormat;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.Messages;

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
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false), 
		        new String(abstractMethod.declaringClass.readableName()), 
		        new String(type.readableName()), 
		},
		new String[] { 
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true), 
		        new String(abstractMethod.declaringClass.shortReadableName()), 
		        new String(type.shortReadableName()), 
		},
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
public void annotationCannotOverrideMethod(MethodBinding overrideMethod, MethodBinding inheritedMethod) {
	ASTNode location = overrideMethod.sourceMethod();
	this.handle(
		IProblem.AnnotationCannotOverrideMethod,
		new String[] {
				new String(overrideMethod.declaringClass.readableName()),
				new String(inheritedMethod.declaringClass.readableName()),
				new String(inheritedMethod.selector), 
				typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, false)},
		new String[] {
				new String(overrideMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.selector), 
				typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, true)},
		location.sourceStart,
		location.sourceEnd);	
}
public void annotationCircularity(TypeBinding sourceType, TypeBinding otherType, TypeReference reference) {
	if (sourceType == otherType)
		this.handle(
			IProblem.AnnotationCircularitySelfReference,
			new String[] {new String(sourceType.readableName())},
			new String[] {new String(sourceType.shortReadableName())},
			reference.sourceStart,
			reference.sourceEnd);
	else
		this.handle(
			IProblem.AnnotationCircularity,
			new String[] {new String(sourceType.readableName()), new String(otherType.readableName())},
			new String[] {new String(sourceType.shortReadableName()), new String(otherType.shortReadableName())},
			reference.sourceStart,
			reference.sourceEnd);
}
public void annotationMembersCannotHaveParameters(AnnotationMethodDeclaration annotationMethodDeclaration) {
	this.handle(
		IProblem.AnnotationMembersCannotHaveParameters,
		NoArgument,
		NoArgument,
		annotationMethodDeclaration.sourceStart,
		annotationMethodDeclaration.sourceEnd);
}
public void annotationMembersCannotHaveTypeParameters(AnnotationMethodDeclaration annotationMethodDeclaration) {
	this.handle(
		IProblem.AnnotationMembersCannotHaveTypeParameters,
		NoArgument,
		NoArgument,
		annotationMethodDeclaration.sourceStart,
		annotationMethodDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveConstructor(ConstructorDeclaration constructorDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveConstructor,
		NoArgument,
		NoArgument,
		constructorDeclaration.sourceStart,
		constructorDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveSuperclass(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveSuperclass,
		NoArgument,
		NoArgument,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void annotationTypeDeclarationCannotHaveSuperinterfaces(TypeDeclaration typeDeclaration) {
	this.handle(
		IProblem.AnnotationTypeDeclarationCannotHaveSuperinterfaces,
		NoArgument,
		NoArgument,
		typeDeclaration.sourceStart,
		typeDeclaration.sourceEnd);
}
public void annotationTypeUsedAsSuperinterface(SourceTypeBinding type, TypeReference superInterfaceRef, ReferenceBinding superType) {
	this.handle(
		IProblem.AnnotationTypeUsedAsSuperInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(type.sourceName())},
		superInterfaceRef.sourceStart,
		superInterfaceRef.sourceEnd);
}

public void annotationValueMustBeAnnotation(TypeBinding annotationType, char[] name, Expression value, TypeBinding expectedType) {
	String str = new String(name);
	this.handle(
		IProblem.AnnotationValueMustBeAnnotation,
		new String[] { new String(annotationType.readableName()), str, new String(expectedType.readableName()),  },
		new String[] { new String(annotationType.shortReadableName()), str, new String(expectedType.readableName()), },
		value.sourceStart,
		value.sourceEnd);
}
public void annotationValueMustBeClassLiteral(TypeBinding annotationType, char[] name, Expression value) {
	String str = new String(name);
	this.handle(
		IProblem.AnnotationValueMustBeClassLiteral,
		new String[] { new String(annotationType.readableName()), str },
		new String[] { new String(annotationType.shortReadableName()), str},
		value.sourceStart,
		value.sourceEnd);
}
public void annotationValueMustBeConstant(TypeBinding annotationType, char[] name, Expression value) {
	String str = 	new String(name);
	this.handle(
		IProblem.AnnotationValueMustBeConstant,
		new String[] { new String(annotationType.readableName()), str },
		new String[] { new String(annotationType.shortReadableName()), str},
		value.sourceStart,
		value.sourceEnd);
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
public void autoboxing(Expression expression, TypeBinding originalType, TypeBinding convertedType) {
	this.handle(
		originalType.isBaseType() ? IProblem.BoxingConversion : IProblem.UnboxingConversion,
		new String[] { new String(originalType.readableName()), new String(convertedType.readableName()), },
		new String[] { new String(originalType.shortReadableName()), new String(convertedType.shortReadableName()), },
		expression.sourceStart,
		expression.sourceEnd);
}
public void boundCannotBeArray(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.BoundCannotBeArray,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void boundMustBeAnInterface(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.BoundMustBeAnInterface,
		new String[] {new String(type.readableName())},
		new String[] {new String(type.shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	MethodBinding method = location.binding;
	if (location.isConstructor()) {
		this.handle(
			IProblem.BytecodeExceeds64KLimitForConstructor,
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, true)},
			Error | Abort,
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.BytecodeExceeds64KLimit,
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(location.selector), typesAsString(method.isVarargs(), method.parameters, true)},
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
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
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
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void cannotExtendEnum(SourceTypeBinding type, TypeReference superclass, TypeBinding superTypeBinding) {
	String name = new String(type.sourceName());
	String superTypeFullName = new String(superTypeBinding.readableName());
	String superTypeShortName = new String(superTypeBinding.shortReadableName());
	if (superTypeShortName.equals(name)) superTypeShortName = superTypeFullName;
	this.handle(
		IProblem.CannotExtendEnum,
		new String[] {superTypeFullName, name},
		new String[] {superTypeShortName, name},
		superclass.sourceStart,
		superclass.sourceEnd);
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
public void cannotInvokeSuperConstructorInEnum(ExplicitConstructorCall constructorCall, MethodBinding enumConstructor) {
	this.handle(
		IProblem.CannotInvokeSuperConstructorInEnum,
		new String[] { 
		        new String(enumConstructor.declaringClass.sourceName()),
		        typesAsString(enumConstructor.isVarargs(), enumConstructor.parameters, false), 
		 },
		new String[] { 
		        new String(enumConstructor.declaringClass.sourceName()),
		        typesAsString(enumConstructor.isVarargs(), enumConstructor.parameters, true), 
		 },
		constructorCall.sourceStart,
		constructorCall.sourceEnd);
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
public void cannotUseQualifiedEnumConstantInCaseLabel(Reference location, FieldBinding field) {
	this.handle(
			IProblem.IllegalQualifiedEnumConstantLabel,
			new String[]{ String.valueOf(field.declaringClass.readableName()), String.valueOf(field.name) },
			new String[]{ String.valueOf(field.declaringClass.shortReadableName()), String.valueOf(field.name) },
			fieldSourceStart(field, location),
			fieldSourceEnd(field, location)); 
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
public int computeSeverity(int problemID){

	switch (problemID) {
		case IProblem.Task :
 		case IProblem.VarargsConflict :
			return ProblemSeverities.Warning;
			
		/*
		 * Javadoc tags resolved references errors
		 */
		case IProblem.JavadocInvalidParamName:
		case IProblem.JavadocDuplicateParamName:
		case IProblem.JavadocMissingParamName:
		case IProblem.JavadocMissingIdentifier:
		case IProblem.JavadocInvalidThrowsClassName:
		case IProblem.JavadocDuplicateThrowsClassName:
		case IProblem.JavadocMissingThrowsClassName:
		case IProblem.JavadocMissingSeeReference:
		case IProblem.JavadocInvalidValueReference:
		case IProblem.JavadocUndefinedField:
		case IProblem.JavadocAmbiguousField:
		case IProblem.JavadocUndefinedConstructor:
		case IProblem.JavadocAmbiguousConstructor:
		case IProblem.JavadocUndefinedMethod:
		case IProblem.JavadocAmbiguousMethod:
		case IProblem.JavadocAmbiguousMethodReference:
		case IProblem.JavadocParameterMismatch:
		case IProblem.JavadocUndefinedType:
		case IProblem.JavadocAmbiguousType:
		case IProblem.JavadocInternalTypeNameProvided:
		case IProblem.JavadocNoMessageSendOnArrayType:
		case IProblem.JavadocNoMessageSendOnBaseType:
		case IProblem.JavadocInheritedMethodHidesEnclosingName:
		case IProblem.JavadocInheritedFieldHidesEnclosingName:
		case IProblem.JavadocInheritedNameHidesEnclosingTypeName:
		case IProblem.JavadocNonStaticTypeFromStaticInvocation:
		case IProblem.JavadocGenericMethodTypeArgumentMismatch:
		case IProblem.JavadocNonGenericMethod:
		case IProblem.JavadocIncorrectArityForParameterizedMethod:
		case IProblem.JavadocParameterizedMethodArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericMethod:
		case IProblem.JavadocGenericConstructorTypeArgumentMismatch:
		case IProblem.JavadocNonGenericConstructor:
		case IProblem.JavadocIncorrectArityForParameterizedConstructor:
		case IProblem.JavadocParameterizedConstructorArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericConstructor:
			if (!this.options.reportInvalidJavadocTags) {
				return ProblemSeverities.Ignore;		
			}
			break;
		/*
		 * Javadoc invalid tags due to deprecated references
		 */
		case IProblem.JavadocUsingDeprecatedField:
		case IProblem.JavadocUsingDeprecatedConstructor:
		case IProblem.JavadocUsingDeprecatedMethod:
		case IProblem.JavadocUsingDeprecatedType:
			if (!(this.options.reportInvalidJavadocTags && this.options.reportInvalidJavadocTagsDeprecatedRef)) {
				return ProblemSeverities.Ignore;
			}
			break;
		/*
		 * Javadoc invalid tags due to non-visible references
		 */
		case IProblem.JavadocNotVisibleField:
		case IProblem.JavadocNotVisibleConstructor:
		case IProblem.JavadocNotVisibleMethod:
		case IProblem.JavadocNotVisibleType:
			if (!(this.options.reportInvalidJavadocTags && this.options.reportInvalidJavadocTagsNotVisibleRef)) {
				return ProblemSeverities.Ignore;			
			}
			break;
	}
	long irritant = getIrritant(problemID);
	if (irritant != 0) {
		if ((problemID & IProblem.Javadoc) != 0 && !this.options.docCommentSupport) 
			return ProblemSeverities.Ignore;
		return this.options.getSeverity(irritant);
	}
	return Error;
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
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
}
public void deprecatedMethod(MethodBinding method, ASTNode location) {
	if (method.isConstructor()) {
		this.handle(
			IProblem.UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), typesAsString(method.isVarargs(), method.parameters, true)},
			location.sourceStart,
			location.sourceEnd);
	} else {
		this.handle(
			IProblem.UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
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
public void disallowedTargetForAnnotation(Annotation annotation) {
	this.handle(
		IProblem.DisallowedTargetForAnnotation,
		new String[] {new String(annotation.resolvedType.readableName())},
		new String[] {new String(annotation.resolvedType.shortReadableName())},
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void duplicateAnnotation(Annotation annotation) {
	this.handle(
		IProblem.DuplicateAnnotation,
		new String[] {new String(annotation.resolvedType.readableName())},
		new String[] {new String(annotation.resolvedType.shortReadableName())},
		annotation.sourceStart,
		annotation.sourceEnd);
}
public void duplicateAnnotationValue(TypeBinding annotationType, MemberValuePair memberValuePair) {
	String name = 	new String(memberValuePair.name);
	this.handle(
		IProblem.DuplicateAnnotationMember,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		memberValuePair.sourceStart,
		memberValuePair.sourceEnd);
}
public void duplicateBounds(ASTNode location, TypeBinding type) {
	this.handle(
		IProblem.DuplicateBounds,
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

public void duplicateEnumSpecialMethod(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
    MethodBinding method = methodDecl.binding;
	this.handle(
		IProblem.CannotDeclareEnumSpecialMethod,
		new String[] {
	        new String(methodDecl.selector),
			new String(method.declaringClass.readableName()),
			typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {
			new String(methodDecl.selector),
			new String(method.declaringClass.shortReadableName()),
			typesAsString(method.isVarargs(), method.parameters, true)},
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
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
public void duplicateInheritedMethods(SourceTypeBinding type, MethodBinding inheritedMethod1, MethodBinding inheritedMethod2) {
	this.handle(
		IProblem.DuplicateParameterizedMethods,
		new String[] {
	        new String(inheritedMethod1.selector),
			new String(inheritedMethod1.declaringClass.readableName()),
			typesAsString(inheritedMethod1.isVarargs(), inheritedMethod1.original().parameters, false),	
			typesAsString(inheritedMethod2.isVarargs(), inheritedMethod2.original().parameters, false)},
		new String[] {
			new String(inheritedMethod1.selector),
			new String(inheritedMethod1.declaringClass.shortReadableName()),
			typesAsString(inheritedMethod1.isVarargs(), inheritedMethod1.original().parameters, true),	
			typesAsString(inheritedMethod2.isVarargs(), inheritedMethod2.original().parameters, true)},
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateInitializationOfBlankFinalField(FieldBinding field, Reference reference) {
	String[] arguments = new String[]{ new String(field.readableName())};
	this.handle(
		IProblem.DuplicateBlankFinalFieldInitialization,
		arguments,
		arguments,
		fieldSourceStart(field, reference),
		fieldSourceEnd(field, reference));
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
				typesAsString(method.isVarargs(), method.parameters, false),
				typesAsString(method.isVarargs(), erasures, false) } ,
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				typesAsString(method.isVarargs(), method.parameters, true),
				typesAsString(method.isVarargs(), erasures, true) },
			methodDecl.sourceStart,
			methodDecl.sourceEnd);
    } else {
		this.handle(
			IProblem.DuplicateMethod,
			new String[] {
		        new String(methodDecl.selector),
				new String(method.declaringClass.readableName()),
				typesAsString(method.isVarargs(), method.parameters, false)},
			new String[] {
				new String(methodDecl.selector),
				new String(method.declaringClass.shortReadableName()),
				typesAsString(method.isVarargs(), method.parameters, true)},
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
public void duplicateSuperinterface(SourceTypeBinding type, TypeReference reference, ReferenceBinding superType) {
	this.handle(
		IProblem.DuplicateSuperInterface,
		new String[] {
			new String(superType.readableName()),
			new String(type.sourceName())},
		new String[] {
			new String(superType.shortReadableName()),
			new String(type.sourceName())},
		reference.sourceStart,
		reference.sourceEnd);
}
public void duplicateTargetInTargetAnnotation(TypeBinding annotationType, NameReference reference) {
	FieldBinding field = reference.fieldBinding();
	String name = 	new String(field.name);
	this.handle(
		IProblem.DuplicateTargetInTargetAnnotation,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		fieldSourceStart(field, reference),
		fieldSourceEnd(field, reference)); 
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
public void enumAbstractMethodMustBeImplemented(AbstractMethodDeclaration method) {
	MethodBinding abstractMethod = method.binding;
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		IProblem.EnumAbstractMethodMustBeImplemented,
		new String[] { 
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, false), 
		        new String(abstractMethod.declaringClass.readableName()), 
		},
		new String[] { 
		        new String(abstractMethod.selector),
		        typesAsString(abstractMethod.isVarargs(), abstractMethod.parameters, true), 
		        new String(abstractMethod.declaringClass.shortReadableName()), 
		},
		method.sourceStart(),
		method.sourceEnd());
}
public void enumStaticFieldUsedDuringInitialization(FieldBinding field, ASTNode location) {
	this.handle(
		IProblem.EnumStaticFieldInInInitializerContext,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
}
public void enumSwitchCannotTargetField(Reference reference, FieldBinding field) {
	this.handle(
			IProblem.EnumSwitchCannotTargetField,
			new String[]{ String.valueOf(field.declaringClass.readableName()), String.valueOf(field.name) },
			new String[]{ String.valueOf(field.declaringClass.shortReadableName()), String.valueOf(field.name) },
			fieldSourceStart(field, reference),
			fieldSourceEnd(field, reference)); 
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
private int fieldSourceEnd(FieldBinding field, ASTNode node) {
	if (node instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) node;
		if (ref.binding == field) {
			return (int) (ref.sourcePositions[ref.indexOfFirstFieldBinding-1]);
		}
		FieldBinding[] otherFields = ref.otherBindings;
		if (otherFields != null) {
			int offset = ref.indexOfFirstFieldBinding == 1 ? 1 : ref.indexOfFirstFieldBinding - 1;
			for (int i = 0, length = otherFields.length; i < length; i++) {
				if (otherFields[i] == field)
					return (int) (ref.sourcePositions[i + offset]);
			}
		}
	}	
	return node.sourceEnd;
}
private int fieldSourceStart(FieldBinding field, ASTNode node) {
	if (node instanceof FieldReference) {
		FieldReference fieldReference = (FieldReference) node;
		return (int) (fieldReference.nameSourcePosition >> 32);
	} else 	if (node instanceof QualifiedNameReference) {
		QualifiedNameReference ref = (QualifiedNameReference) node;
		if (ref.binding == field) {
			return (int) (ref.sourcePositions[ref.indexOfFirstFieldBinding-1] >> 32);
		}
		FieldBinding[] otherFields = ref.otherBindings;
		if (otherFields != null) {
			int offset = ref.indexOfFirstFieldBinding == 1 ? 1 : ref.indexOfFirstFieldBinding - 1;
			for (int i = 0, length = otherFields.length; i < length; i++) {
				if (otherFields[i] == field)
					return (int) (ref.sourcePositions[i + offset] >> 32);
			}
		}
	}

	return node.sourceStart;
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
public void forbiddenReference(TypeBinding type, ASTNode location, String messageTemplate, int problemId) {
	if (location == null) return;
	// this problem has a message template extracted from the access restriction rule
	this.handle(
		problemId,
		new String[] { new String(type.readableName()) }, // distinct from msg arg for quickfix purpose
		new String[] { MessageFormat.format(messageTemplate, new String[]{ new String(type.shortReadableName())})},
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
public static long getIrritant(int problemID) {
	switch(problemID){

		case IProblem.MaskedCatch : 
			return CompilerOptions.MaskedCatchBlock;

		case IProblem.UnusedImport :
			return CompilerOptions.UnusedImport;
			
		case IProblem.MethodButWithConstructorName :
			return CompilerOptions.MethodWithConstructorName;
		
		case IProblem.OverridingNonVisibleMethod :
			return CompilerOptions.OverriddenPackageDefaultMethod;

		case IProblem.IncompatibleReturnTypeForNonInheritedInterfaceMethod :
		case IProblem.IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod :
			return CompilerOptions.IncompatibleNonInheritedInterfaceMethod;

		case IProblem.OverridingDeprecatedMethod :				
		case IProblem.UsingDeprecatedType :				
		case IProblem.UsingDeprecatedMethod :
		case IProblem.UsingDeprecatedConstructor :
		case IProblem.UsingDeprecatedField :
			return CompilerOptions.UsingDeprecatedAPI;
		
		case IProblem.LocalVariableIsNeverUsed :
			return CompilerOptions.UnusedLocalVariable;
		
		case IProblem.ArgumentIsNeverUsed :
			return CompilerOptions.UnusedArgument;

		case IProblem.NoImplicitStringConversionForCharArrayExpression :
			return CompilerOptions.NoImplicitStringConversion;

		case IProblem.NeedToEmulateFieldReadAccess :
		case IProblem.NeedToEmulateFieldWriteAccess :
		case IProblem.NeedToEmulateMethodAccess :
		case IProblem.NeedToEmulateConstructorAccess :			
			return CompilerOptions.AccessEmulation;

		case IProblem.NonExternalizedStringLiteral :
		case IProblem.UnnecessaryNLSTag :
			return CompilerOptions.NonExternalizedString;

		case IProblem.UseAssertAsAnIdentifier :
			return CompilerOptions.AssertUsedAsAnIdentifier;
			
		case IProblem.UseEnumAsAnIdentifier :
			return CompilerOptions.EnumUsedAsAnIdentifier;

		case IProblem.NonStaticAccessToStaticMethod :
		case IProblem.NonStaticAccessToStaticField :
			return CompilerOptions.NonStaticAccessToStatic;

		case IProblem.IndirectAccessToStaticMethod :
		case IProblem.IndirectAccessToStaticField :
		case IProblem.IndirectAccessToStaticType :
			return CompilerOptions.IndirectStaticAccess;

		case IProblem.AssignmentHasNoEffect:
			return CompilerOptions.NoEffectAssignment;

		case IProblem.UnusedPrivateConstructor:
		case IProblem.UnusedPrivateMethod:
		case IProblem.UnusedPrivateField:
		case IProblem.UnusedPrivateType:
			return CompilerOptions.UnusedPrivateMember;

		case IProblem.LocalVariableHidingLocalVariable:
		case IProblem.LocalVariableHidingField:
		case IProblem.ArgumentHidingLocalVariable:
		case IProblem.ArgumentHidingField:
			return CompilerOptions.LocalVariableHiding;

		case IProblem.FieldHidingLocalVariable:
		case IProblem.FieldHidingField:
			return CompilerOptions.FieldHiding;

		case IProblem.TypeParameterHidingType:
			return CompilerOptions.TypeParameterHiding;
			
		case IProblem.PossibleAccidentalBooleanAssignment:
			return CompilerOptions.AccidentalBooleanAssign;

		case IProblem.SuperfluousSemicolon:
		case IProblem.EmptyControlFlowStatement:
			return CompilerOptions.EmptyStatement;

		case IProblem.UndocumentedEmptyBlock:
			return CompilerOptions.UndocumentedEmptyBlock;
			
		case IProblem.UnnecessaryCast:
		case IProblem.UnnecessaryInstanceof:
			return CompilerOptions.UnnecessaryTypeCheck;
			
		case IProblem.FinallyMustCompleteNormally:
			return CompilerOptions.FinallyBlockNotCompleting;
			
		case IProblem.UnusedMethodDeclaredThrownException:
		case IProblem.UnusedConstructorDeclaredThrownException:
			return CompilerOptions.UnusedDeclaredThrownException;

		case IProblem.UnqualifiedFieldAccess:
			return CompilerOptions.UnqualifiedFieldAccess;
		
		case IProblem.UnnecessaryElse:
			return CompilerOptions.UnnecessaryElse;

		case IProblem.UnsafeRawConstructorInvocation:
		case IProblem.UnsafeRawMethodInvocation:
		case IProblem.UnsafeTypeConversion:
		case IProblem.UnsafeRawFieldAssignment:
		case IProblem.UnsafeGenericCast:
		case IProblem.UnsafeReturnTypeOverride:
		case IProblem.UnsafeRawGenericMethodInvocation:
		case IProblem.UnsafeRawGenericConstructorInvocation:
			return CompilerOptions.UncheckedTypeOperation;

		case IProblem.RawTypeReference:
			return CompilerOptions.RawTypeReference;

		case IProblem.MissingOverrideAnnotation:
			return CompilerOptions.MissingOverrideAnnotation;
			
		case IProblem.FieldMissingDeprecatedAnnotation:
		case IProblem.MethodMissingDeprecatedAnnotation:
		case IProblem.TypeMissingDeprecatedAnnotation:
			return CompilerOptions.MissingDeprecatedAnnotation;
			
		case IProblem.FinalBoundForTypeVariable:
		    return CompilerOptions.FinalParameterBound;

		case IProblem.MissingSerialVersion:
			return CompilerOptions.MissingSerialVersion;
		
		case IProblem.ForbiddenReference:
			return CompilerOptions.ForbiddenReference;

		case IProblem.DiscouragedReference:
			return CompilerOptions.DiscouragedReference;

		case IProblem.MethodVarargsArgumentNeedCast :
		case IProblem.ConstructorVarargsArgumentNeedCast :
			return CompilerOptions.VarargsArgumentNeedCast;

		case IProblem.LocalVariableCannotBeNull :
		case IProblem.LocalVariableCanOnlyBeNull :
			return CompilerOptions.NullReference;
			
		case IProblem.BoxingConversion :
		case IProblem.UnboxingConversion :
			return CompilerOptions.AutoBoxing;

		case IProblem.MissingEnumConstantCase :
			return CompilerOptions.IncompleteEnumSwitch;
			
		case IProblem.AnnotationTypeUsedAsSuperInterface :
			return CompilerOptions.AnnotationSuperInterface;
			
		case IProblem.UnhandledWarningToken :
			return CompilerOptions.UnhandledWarningToken;
			
		case IProblem.JavadocUnexpectedTag:
		case IProblem.JavadocDuplicateReturnTag:
		case IProblem.JavadocInvalidThrowsClass:
		case IProblem.JavadocInvalidSeeReference:
		case IProblem.JavadocInvalidParamTagName:
		case IProblem.JavadocInvalidParamTagTypeParameter:
		case IProblem.JavadocMalformedSeeReference:
		case IProblem.JavadocInvalidSeeHref:
		case IProblem.JavadocInvalidSeeArgs:
		case IProblem.JavadocInvalidTag:
		case IProblem.JavadocUnterminatedInlineTag:
		case IProblem.JavadocMissingHashCharacter:
		case IProblem.JavadocEmptyReturnTag:
		case IProblem.JavadocUnexpectedText:
		case IProblem.JavadocInvalidParamName:
		case IProblem.JavadocDuplicateParamName:
		case IProblem.JavadocMissingParamName:
		case IProblem.JavadocMissingIdentifier:
		case IProblem.JavadocInvalidThrowsClassName:
		case IProblem.JavadocDuplicateThrowsClassName:
		case IProblem.JavadocMissingThrowsClassName:
		case IProblem.JavadocMissingSeeReference:
		case IProblem.JavadocInvalidValueReference:
		case IProblem.JavadocUndefinedField:
		case IProblem.JavadocAmbiguousField:
		case IProblem.JavadocUndefinedConstructor:
		case IProblem.JavadocAmbiguousConstructor:
		case IProblem.JavadocUndefinedMethod:
		case IProblem.JavadocAmbiguousMethod:
		case IProblem.JavadocAmbiguousMethodReference:
		case IProblem.JavadocParameterMismatch:
		case IProblem.JavadocUndefinedType:
		case IProblem.JavadocAmbiguousType:
		case IProblem.JavadocInternalTypeNameProvided:
		case IProblem.JavadocNoMessageSendOnArrayType:
		case IProblem.JavadocNoMessageSendOnBaseType:
		case IProblem.JavadocInheritedMethodHidesEnclosingName:
		case IProblem.JavadocInheritedFieldHidesEnclosingName:
		case IProblem.JavadocInheritedNameHidesEnclosingTypeName:
		case IProblem.JavadocNonStaticTypeFromStaticInvocation:
		case IProblem.JavadocGenericMethodTypeArgumentMismatch:
		case IProblem.JavadocNonGenericMethod:
		case IProblem.JavadocIncorrectArityForParameterizedMethod:
		case IProblem.JavadocParameterizedMethodArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericMethod:
		case IProblem.JavadocGenericConstructorTypeArgumentMismatch:
		case IProblem.JavadocNonGenericConstructor:
		case IProblem.JavadocIncorrectArityForParameterizedConstructor:
		case IProblem.JavadocParameterizedConstructorArgumentTypeMismatch:
		case IProblem.JavadocTypeArgumentsForRawGenericConstructor:
		case IProblem.JavadocNotVisibleField:
		case IProblem.JavadocNotVisibleConstructor:
		case IProblem.JavadocNotVisibleMethod:
		case IProblem.JavadocNotVisibleType:
			return CompilerOptions.InvalidJavadoc;
			
		case IProblem.JavadocUsingDeprecatedField:
		case IProblem.JavadocUsingDeprecatedConstructor:
		case IProblem.JavadocUsingDeprecatedMethod:
		case IProblem.JavadocUsingDeprecatedType:
			return CompilerOptions.InvalidJavadoc | CompilerOptions.UsingDeprecatedAPI;

		case IProblem.JavadocMissingParamTag:
		case IProblem.JavadocMissingReturnTag:
		case IProblem.JavadocMissingThrowsTag:
			return CompilerOptions.MissingJavadocTags;

		case IProblem.JavadocMissing:
			return CompilerOptions.MissingJavadocComments;
	}
	return 0;
}

/**
 * Compute problem category ID based on problem ID
 * @param problemID
 * @return a category ID
 * @see CategorizedProblem
 */
public static int getProblemCategory(int problemID) {
	long irritant = getIrritant(problemID);
	int irritantInt = (int) irritant;
	categorizeOnIrritant: {
		if (irritantInt == irritant) {
			switch (irritantInt) {
				case (int)CompilerOptions.MethodWithConstructorName:
				case (int)CompilerOptions.AccessEmulation:
				case (int)CompilerOptions.AssertUsedAsAnIdentifier:
				case (int)CompilerOptions.NonStaticAccessToStatic:
				case (int)CompilerOptions.UnqualifiedFieldAccess:
				case (int)CompilerOptions.UndocumentedEmptyBlock:
				case (int)CompilerOptions.IndirectStaticAccess:
				case (int)CompilerOptions.FinalParameterBound:
					return CategorizedProblem.CAT_CODE_STYLE;
					
				case (int)CompilerOptions.MaskedCatchBlock:
				case (int)CompilerOptions.NoImplicitStringConversion:
				case (int)CompilerOptions.NoEffectAssignment:
				case (int)CompilerOptions.AccidentalBooleanAssign:
				case (int)CompilerOptions.EmptyStatement:
				case (int)CompilerOptions.FinallyBlockNotCompleting:
					return CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM;
		
				case (int)CompilerOptions.OverriddenPackageDefaultMethod:
				case (int)CompilerOptions.IncompatibleNonInheritedInterfaceMethod:
				case (int)CompilerOptions.LocalVariableHiding:
				case (int)CompilerOptions.FieldHiding:
					return CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT;
					
				case (int)CompilerOptions.UnusedLocalVariable:
				case (int)CompilerOptions.UnusedArgument:
				case (int)CompilerOptions.UnusedImport:
				case (int)CompilerOptions.UnusedPrivateMember:
				case (int)CompilerOptions.UnusedDeclaredThrownException:
				case (int)CompilerOptions.UnnecessaryTypeCheck:
				case (int)CompilerOptions.UnnecessaryElse:
				case (int)CompilerOptions.UnhandledWarningToken:
					return CategorizedProblem.CAT_UNNECESSARY_CODE;
		
				case (int)CompilerOptions.UsingDeprecatedAPI:
					return CategorizedProblem.CAT_DEPRECATION;
					
				case (int)CompilerOptions.NonExternalizedString:
					return CategorizedProblem.CAT_NLS;
					
				case (int)CompilerOptions.Task:
					return CategorizedProblem.CAT_UNSPECIFIED; // TODO may want to improve
					
				case (int)CompilerOptions.MissingJavadocComments:
				case (int)CompilerOptions.MissingJavadocTags:
				case (int)CompilerOptions.InvalidJavadoc:
					return CategorizedProblem.CAT_JAVADOC;
					
				case (int)CompilerOptions.UncheckedTypeOperation:
					return CategorizedProblem.CAT_UNCHECKED_RAW;
					
				default:
					break categorizeOnIrritant;
			}
		} else {
			irritantInt = (int)(irritant >>> 32);
			switch (irritantInt) {
				case (int)(CompilerOptions.EnumUsedAsAnIdentifier >>> 32):
				case (int)(CompilerOptions.AnnotationSuperInterface >>> 32):
				case (int)(CompilerOptions.AutoBoxing >>> 32):
				case (int)(CompilerOptions.MissingOverrideAnnotation >>> 32):
				case (int)(CompilerOptions.MissingDeprecatedAnnotation >>> 32):
					return CategorizedProblem.CAT_CODE_STYLE;
				
				case (int)(CompilerOptions.MissingSerialVersion >>> 32):
				case (int)(CompilerOptions.VarargsArgumentNeedCast >>> 32):
				case (int)(CompilerOptions.NullReference >>> 32):
				case (int)(CompilerOptions.IncompleteEnumSwitch >>> 32):
					return CategorizedProblem.CAT_POTENTIAL_PROGRAMMING_PROBLEM;
	
				case (int)(CompilerOptions.TypeParameterHiding >>> 32):
					return CategorizedProblem.CAT_NAME_SHADOWING_CONFLICT;
					
				case (int)(CompilerOptions.ForbiddenReference >>> 32):
				case (int)(CompilerOptions.DiscouragedReference >>> 32):
					return CategorizedProblem.CAT_BUILDPATH;
	
				case (int)(CompilerOptions.RawTypeReference >>> 32):
					return CategorizedProblem.CAT_UNCHECKED_RAW;

				default:
					break categorizeOnIrritant;
			}
		}	
	}
	// categorize non optional problems per ID
	switch (problemID) {
		case IProblem.IsClassPathCorrect :
		case IProblem.CorruptedSignature :
			return CategorizedProblem.CAT_BUILDPATH;
			
		default :
			if ((problemID & IProblem.Syntax) != 0)
				return CategorizedProblem.CAT_SYNTAX;
			if ((problemID & IProblem.ImportRelated) != 0)
				return CategorizedProblem.CAT_IMPORT;
			if ((problemID & IProblem.TypeRelated) != 0)
				return CategorizedProblem.CAT_TYPE;
			if ((problemID & (IProblem.FieldRelated|IProblem.MethodRelated|IProblem.ConstructorRelated)) != 0)
				return CategorizedProblem.CAT_MEMBER;
	}
	return CategorizedProblem.CAT_UNSPECIFIED;
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

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
	} else {
		start = reference.sourceStart;
		end = reference.sourceEnd;
	}

	if (sourceType == superType)
		this.handle(
			IProblem.HierarchyCircularitySelfReference,
			new String[] {new String(sourceType.readableName()) },
			new String[] {new String(sourceType.shortReadableName()) },
			start,
			end);
	else
		this.handle(
			IProblem.HierarchyCircularity,
			new String[] {new String(sourceType.readableName()), new String(superType.readableName())},
			new String[] {new String(sourceType.shortReadableName()), new String(superType.shortReadableName())},
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
public void illegalExtendedDimensions(AnnotationMethodDeclaration annotationTypeMemberDeclaration) {
	this.handle(
		IProblem.IllegalExtendedDimensions,
		NoArgument, 
		NoArgument, 
		annotationTypeMemberDeclaration.sourceStart,
		annotationTypeMemberDeclaration.sourceEnd);
}
public void illegalExtendedDimensions(Argument argument) {
	this.handle(
		IProblem.IllegalExtendedDimensionsForVarArgs,
		NoArgument, 
		NoArgument, 
		argument.sourceStart,
		argument.sourceEnd);
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
public void illegalLocalTypeDeclaration(TypeDeclaration typeDeclaration) {
	int problemID = 0;
	if ((typeDeclaration.modifiers & IConstants.AccEnum) != 0) {
		problemID = IProblem.CannotDefineEnumInLocalType;
	} else if ((typeDeclaration.modifiers & IConstants.AccAnnotation) != 0) {
		problemID = IProblem.CannotDefineAnnotationInLocalType;		
	} else if ((typeDeclaration.modifiers & IConstants.AccInterface) != 0) {
		problemID = IProblem.CannotDefineInterfaceInLocalType;		
	}
	if (problemID != 0) {
		String[] arguments = new String[] {new String(typeDeclaration.name)};
		this.handle(
			problemID,
			arguments,
			arguments,
			typeDeclaration.sourceStart,
			typeDeclaration.sourceEnd);
	}
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
public void illegalModifierForAnnotationField(FieldDeclaration fieldDecl) {
	String name = new String(fieldDecl.name);
	this.handle(
		IProblem.IllegalModifierForAnnotationField,
		new String[] {
			new String(fieldDecl.binding.declaringClass.readableName()),
			name,
		},		
		new String[] {
			new String(fieldDecl.binding.declaringClass.shortReadableName()),
			name,
		},		
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}

public void illegalModifierForAnnotationMember(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForAnnotationMethod,
		new String[] {
			new String(methodDecl.binding.declaringClass.readableName()),
			new String(methodDecl.selector),
		},		
		new String[] {
			new String(methodDecl.binding.declaringClass.shortReadableName()),
			new String(methodDecl.selector),
		},		
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void illegalModifierForAnnotationMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForAnnotationMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForAnnotationType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForAnnotationType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
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
public void illegalModifierForEnum(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForEnum,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForEnumConstant(ReferenceBinding type, FieldDeclaration fieldDecl) {
	String[] arguments = new String[] {new String(fieldDecl.name)};
	this.handle(
		IProblem.IllegalModifierForEnumConstant,
		arguments,
		arguments,
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForEnumConstructor(AbstractMethodDeclaration constructor) {
	this.handle(
		IProblem.IllegalModifierForEnumConstructor,
		NoArgument,		
		NoArgument,	
		constructor.sourceStart,
		constructor.sourceEnd);
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
public void illegalModifierForInterfaceField(FieldDeclaration fieldDecl) {
	String name = new String(fieldDecl.name);
	this.handle(
		IProblem.IllegalModifierForInterfaceField,
		new String[] {
			new String(fieldDecl.binding.declaringClass.readableName()),
			name,
		},		
		new String[] {
			new String(fieldDecl.binding.declaringClass.shortReadableName()),
			name,
		},		
		fieldDecl.sourceStart,
		fieldDecl.sourceEnd);
}
public void illegalModifierForInterfaceMethod(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForInterfaceMethod,
		new String[] {
			new String(methodDecl.binding.declaringClass.readableName()),
			new String(methodDecl.selector),
			typesAsString(methodDecl.binding.isVarargs(), methodDecl.binding.parameters, false),
		},		
		new String[] {
			new String(methodDecl.binding.declaringClass.shortReadableName()),
			new String(methodDecl.selector),
			typesAsString(methodDecl.binding.isVarargs(), methodDecl.binding.parameters, true),
		},	
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
public void illegalModifierForLocalEnum(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForLocalEnum,
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
public void illegalModifierForMemberEnum(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.IllegalModifierForMemberEnum,
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
public void illegalModifierForMethod(AbstractMethodDeclaration methodDecl) {
	this.handle(
		IProblem.IllegalModifierForMethod,
		new String[] {
			new String(methodDecl.selector),
			typesAsString(methodDecl.binding.isVarargs(), methodDecl.binding.parameters, false),
			new String(methodDecl.binding.declaringClass.readableName()),
		},		
		new String[] {
			new String(methodDecl.selector),
			typesAsString(methodDecl.binding.isVarargs(), methodDecl.binding.parameters, true),
			new String(methodDecl.binding.declaringClass.shortReadableName()),
		},	
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
public void illegalQualifiedParameterizedTypeAllocation(TypeReference qualifiedTypeReference, TypeBinding allocatedType) {
	this.handle(
		IProblem.IllegalQualifiedParameterizedTypeAllocation,
		new String[] { new String(allocatedType.readableName()), new String(allocatedType.enclosingType().readableName()), },
		new String[] { new String(allocatedType.shortReadableName()), new String(allocatedType.enclosingType().shortReadableName()), },
		qualifiedTypeReference.sourceStart,
		qualifiedTypeReference.sourceEnd);	
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
public void illegalVararg(Argument argType, AbstractMethodDeclaration methodDecl) {
	String[] arguments = new String[] {CharOperation.toString(argType.type.getTypeName()), new String(methodDecl.selector)};
	this.handle(
		IProblem.IllegalVararg,
		arguments,
		arguments,
		argType.sourceStart,
		argType.sourceEnd);
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
	if (expectedImport.problemId() == InvalidTypeForStaticImport) {
		char[][] tokens = importRef.tokens;
		String[] arguments = new String[]{CharOperation.toString(tokens)};
		this.handle(
		        IProblem.InvalidTypeForStaticImport, 
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
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
			AbortCompilation | Error,
			0,
			1);        
    }
	this.handle(
		IProblem.IncorrectArityForParameterizedType,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void incorrectLocationForNonEmptyDimension(ArrayAllocationExpression expression, int index) {
	this.handle(
		IProblem.IllegalDimension,
		NoArgument,
		NoArgument,
		expression.dimensions[index].sourceStart,
		expression.dimensions[index].sourceEnd);
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
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
}
public void indirectAccessToStaticMethod(ASTNode location, MethodBinding method) {
	this.handle(
		IProblem.IndirectAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
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
public void inheritedMethodsHaveNameClash(SourceTypeBinding type, MethodBinding oneMethod, MethodBinding twoMethod) {
	this.handle(
		IProblem.MethodNameClash,
		new String[] {
			new String(oneMethod.selector),
			typesAsString(oneMethod.original().isVarargs(), oneMethod.original().parameters, false),
			new String(oneMethod.declaringClass.readableName()),
			typesAsString(twoMethod.original().isVarargs(), twoMethod.original().parameters, false),
			new String(twoMethod.declaringClass.readableName()),
		 }, 
		new String[] {
			new String(oneMethod.selector),
			typesAsString(oneMethod.original().isVarargs(), oneMethod.original().parameters, true),
			new String(oneMethod.declaringClass.shortReadableName()),
			typesAsString(twoMethod.original().isVarargs(), twoMethod.original().parameters, true),
			new String(twoMethod.declaringClass.shortReadableName()),
		 }, 
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
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, Initializer initializer) {
	this.handle(
		IProblem.CannotDefineStaticInitializerInLocalType,
		new String[] {new String(innerType.readableName())},
		new String[] {new String(innerType.shortReadableName())},
		initializer.sourceStart,
		initializer.sourceStart);
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
public void invalidAnnotationMemberType(MethodDeclaration methodDecl) {
	this.handle(
		IProblem.InvalidAnnotationMemberType,
		new String[] {
			new String(methodDecl.binding.returnType.readableName()),
			new String(methodDecl.selector),
			new String(methodDecl.binding.declaringClass.readableName()),
		},
		new String[] {
			new String(methodDecl.binding.returnType.shortReadableName()),
			new String(methodDecl.selector),
			new String(methodDecl.binding.declaringClass.shortReadableName()),
		},
		methodDecl.returnType.sourceStart,
		methodDecl.returnType.sourceEnd);
	
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

	int sourceStart = statement.sourceStart;
	int sourceEnd = statement.sourceEnd;
	if (statement instanceof AllocationExpression) {
		AllocationExpression allocation = (AllocationExpression)statement;
		if (allocation.enumConstant != null) {
			sourceStart = allocation.enumConstant.sourceStart;
			sourceEnd = allocation.enumConstant.sourceEnd;
		}
	}
	
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
			int augmentedLength = problemConstructor.parameters.length;
			TypeBinding inferredTypeArgument = problemConstructor.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemConstructor.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemConstructor.parameters, 0, invocationArguments, 0, augmentedLength-2);
			this.handle(
				IProblem.GenericConstructorTypeArgumentMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        typesAsString(false, invocationArguments, false), 
				        new String(inferredTypeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        typesAsString(false, invocationArguments, true), 
				        new String(inferredTypeArgument.shortReadableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, true) },
				sourceStart,
				sourceEnd);		    
			return;		    
			
		case TypeParameterArityMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			if (shownConstructor.typeVariables == TypeConstants.NoTypeVariables) {
				this.handle(
					IProblem.NonGenericConstructor,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);		    
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedConstructor  ,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
							typesAsString(false, shownConstructor.typeVariables, false),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
							typesAsString(false, shownConstructor.typeVariables, true),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);		    
			}
			return;
		case ParameterizedMethodTypeMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.ParameterizedConstructorArgumentTypeMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, false),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, true),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);		    
			return;
		case TypeArgumentsForRawGenericMethod :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.TypeArgumentsForRawGenericConstructor,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);	
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true)},
		sourceStart,
		sourceEnd);
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
	final int sourceStart= (int) (fieldRef.nameSourcePosition >> 32);
	switch (field.problemId()) {
		case NotFound :
			id = IProblem.UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			this.handle(
				IProblem.NotVisibleField,
				new String[] {new String(fieldRef.token), new String(field.declaringClass.readableName())},
				new String[] {new String(fieldRef.token), new String(field.declaringClass.shortReadableName())},
				sourceStart,
				fieldRef.sourceEnd);			
			return;
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
		sourceStart,
		fieldRef.sourceEnd);
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	int id = IProblem.UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			id = IProblem.UndefinedField;
			break;
		case NotVisible :
			char[] name = field.readableName();
			name = CharOperation.lastSegment(name, '.');
			this.handle(
				IProblem.NotVisibleField,
				new String[] {new String(name), new String(field.declaringClass.readableName())},
				new String[] {new String(name), new String(field.declaringClass.shortReadableName())},
				nameRef.sourceStart,
				nameRef.sourceEnd);				
			return;
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
			String fieldName = new String(nameRef.tokens[index]);
			this.handle(
				IProblem.NotVisibleField,
				new String[] {fieldName, new String(field.declaringClass.readableName())},
				new String[] {fieldName, new String(field.declaringClass.shortReadableName())},
				nameRef.sourceStart, 
				(int) nameRef.sourcePositions[index]);				
			return;
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
					String closestParameterTypeNames = typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false);
					String parameterTypeNames = typesAsString(false, problemMethod.parameters, false);
					String closestParameterTypeShortNames = typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true);
					String parameterTypeShortNames = typesAsString(false, problemMethod.parameters, true);
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
			int augmentedLength = problemMethod.parameters.length;
			TypeBinding inferredTypeArgument = problemMethod.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemMethod.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemMethod.parameters, 0, invocationArguments, 0, augmentedLength-2);
			this.handle(
				IProblem.GenericMethodTypeArgumentMismatch,
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        typesAsString(false, invocationArguments, false), 
				        new String(inferredTypeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        typesAsString(false, invocationArguments, true), 
				        new String(inferredTypeArgument.shortReadableName()), 
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
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
					        typesAsString(method.isVarargs(), method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);		    
			} else {
				this.handle(
					IProblem.IncorrectArityForParameterizedMethod  ,
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
							typesAsString(false, shownMethod.typeVariables, false),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
							typesAsString(false, shownMethod.typeVariables, true),
					        typesAsString(method.isVarargs(), method.parameters, true) },
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
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, true),
				        typesAsString(method.isVarargs(), method.parameters, true) },
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
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        typesAsString(method.isVarargs(), method.parameters, true) },
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
			new String(shownMethod.selector), typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(shownMethod.selector), typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true)},
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
			id = IProblem.NonStaticTypeFromStaticInvocation;
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
	} else if (location instanceof ArrayQualifiedTypeReference) {
		ArrayQualifiedTypeReference arrayQualifiedTypeReference = (ArrayQualifiedTypeReference) location;
		long[] positions = arrayQualifiedTypeReference.sourcePositions;
		end = (int) positions[positions.length - 1];
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
	} else if (location instanceof ArrayTypeReference) {
		ArrayTypeReference arrayTypeReference = (ArrayTypeReference) location;
		end = arrayTypeReference.originalSourceEnd;
	}
	this.handle(
		id,
		new String[] {new String(type.leafComponentType().readableName()) },	
		new String[] {new String(type.leafComponentType().shortReadableName())},
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
public void invalidUsageOfAnnotationDeclarations(TypeDeclaration annotationTypeDeclaration) {
	this.handle(
		IProblem.InvalidUsageOfAnnotationDeclarations,
		NoArgument, 
		NoArgument, 
		annotationTypeDeclaration.sourceStart,
		annotationTypeDeclaration.sourceEnd);
}
public void invalidUsageOfEnumDeclarations(TypeDeclaration enumDeclaration) {
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
				new String[] {new String(method.declaringClass.readableName()), typesAsString(method.isVarargs(), method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), typesAsString(method.isVarargs(), method.parameters, true)},
				location.sourceStart,
				location.sourceEnd);
		} else {
			this.handle(
				IProblem.JavadocUsingDeprecatedMethod,
				new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
				new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
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
public void javadocDuplicatedParamTag(char[] token, int sourceStart, int sourceEnd, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(token)};
		this.handle(IProblem.JavadocDuplicateParamName, arguments, arguments, sourceStart, sourceEnd);
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

	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) return;
	int sourceStart = statement.sourceStart;
	int sourceEnd = statement.sourceEnd;
	if (statement instanceof AllocationExpression) {
		AllocationExpression allocation = (AllocationExpression)statement;
		if (allocation.enumConstant != null) {
			sourceStart = allocation.enumConstant.sourceStart;
			sourceEnd = allocation.enumConstant.sourceEnd;
		}
	}
	int id = IProblem.JavadocUndefinedConstructor; //default...
	ProblemMethodBinding problemConstructor = null;
	MethodBinding shownConstructor = null;
	switch (targetConstructor.problemId()) {
		case NotFound :
			id = IProblem.JavadocUndefinedConstructor;
			break;
		case NotVisible :
			id = IProblem.JavadocNotVisibleConstructor;
			break;
		case Ambiguous :
			id = IProblem.JavadocAmbiguousConstructor;
			break;
		case ParameterBoundMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			ParameterizedGenericMethodBinding substitutedConstructor = (ParameterizedGenericMethodBinding) problemConstructor.closestMatch;
			shownConstructor = substitutedConstructor.original();
			
			int augmentedLength = problemConstructor.parameters.length;
			TypeBinding inferredTypeArgument = problemConstructor.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemConstructor.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemConstructor.parameters, 0, invocationArguments, 0, augmentedLength-2);
			
			this.handle(
				IProblem.JavadocGenericConstructorTypeArgumentMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        typesAsString(false, invocationArguments, false), 
				        new String(inferredTypeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        typesAsString(false, invocationArguments, true), 
				        new String(inferredTypeArgument.shortReadableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, true) },
				sourceStart,
				sourceEnd);		    
			return;		    
			
		case TypeParameterArityMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			if (shownConstructor.typeVariables == TypeConstants.NoTypeVariables) {
				this.handle(
					IProblem.JavadocNonGenericConstructor,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);		    
			} else {
				this.handle(
					IProblem.JavadocIncorrectArityForParameterizedConstructor  ,
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
					        new String(shownConstructor.declaringClass.readableName()), 
							typesAsString(false, shownConstructor.typeVariables, false),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
					new String[] { 
					        new String(shownConstructor.declaringClass.sourceName()),
					        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
					        new String(shownConstructor.declaringClass.shortReadableName()), 
							typesAsString(false, shownConstructor.typeVariables, true),
					        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
					sourceStart,
					sourceEnd);		    
			}
			return;
		case ParameterizedMethodTypeMismatch :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.JavadocParameterizedConstructorArgumentTypeMismatch,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, false),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownConstructor).typeArguments, true),
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);		    
			return;
		case TypeArgumentsForRawGenericMethod :
			problemConstructor = (ProblemMethodBinding) targetConstructor;
			shownConstructor = problemConstructor.closestMatch;
			this.handle(
				IProblem.JavadocTypeArgumentsForRawGenericConstructor,
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, false), 
				        new String(shownConstructor.declaringClass.readableName()), 
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false) },
				new String[] { 
				        new String(shownConstructor.declaringClass.sourceName()),
				        typesAsString(shownConstructor.isVarargs(), shownConstructor.parameters, true), 
				        new String(shownConstructor.declaringClass.shortReadableName()), 
				        typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true) },
				sourceStart,
				sourceEnd);	
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	this.handle(
		id,
		new String[] {new String(targetConstructor.declaringClass.readableName()), typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, false)},
		new String[] {new String(targetConstructor.declaringClass.shortReadableName()), typesAsString(targetConstructor.isVarargs(), targetConstructor.parameters, true)},
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
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	// report issue
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
	if (!javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) return;
	// set problem id
	ProblemMethodBinding problemMethod = null;
	MethodBinding shownMethod = null;
	int id = IProblem.JavadocUndefinedMethod; //default...
	switch (method.problemId()) {
		case NotFound :
			id = IProblem.JavadocUndefinedMethod;
			problemMethod = (ProblemMethodBinding) method;
			if (problemMethod.closestMatch != null) {
					String closestParameterTypeNames = typesAsString(problemMethod.closestMatch.isVarargs(), problemMethod.closestMatch.parameters, false);
					String parameterTypeNames = typesAsString(method.isVarargs(), method.parameters, false);
					String closestParameterTypeShortNames = typesAsString(problemMethod.closestMatch.isVarargs(), problemMethod.closestMatch.parameters, true);
					String parameterTypeShortNames = typesAsString(method.isVarargs(), method.parameters, true);
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
			break;
		case NotVisible :
			id = IProblem.JavadocNotVisibleMethod;
			break;
		case Ambiguous :
			id = IProblem.JavadocAmbiguousMethod;
			break;
		case ParameterBoundMismatch :
			problemMethod = (ProblemMethodBinding) method;
			ParameterizedGenericMethodBinding substitutedMethod = (ParameterizedGenericMethodBinding) problemMethod.closestMatch;
			shownMethod = substitutedMethod.original();
			int augmentedLength = problemMethod.parameters.length;
			TypeBinding inferredTypeArgument = problemMethod.parameters[augmentedLength-2];
			TypeVariableBinding typeParameter = (TypeVariableBinding) problemMethod.parameters[augmentedLength-1];
			TypeBinding[] invocationArguments = new TypeBinding[augmentedLength-2]; // remove extra info from the end
			System.arraycopy(problemMethod.parameters, 0, invocationArguments, 0, augmentedLength-2);
			this.handle(
				IProblem.JavadocGenericMethodTypeArgumentMismatch,
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        typesAsString(false, invocationArguments, false), 
				        new String(inferredTypeArgument.readableName()), 
				        new String(typeParameter.sourceName), 
				        parameterBoundAsString(typeParameter, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        typesAsString(false, invocationArguments, true), 
				        new String(inferredTypeArgument.shortReadableName()), 
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
					IProblem.JavadocNonGenericMethod ,
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
					        typesAsString(method.isVarargs(), method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);		    
			} else {
				this.handle(
					IProblem.JavadocIncorrectArityForParameterizedMethod  ,
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
					        new String(shownMethod.declaringClass.readableName()), 
							typesAsString(false, shownMethod.typeVariables, false),
					        typesAsString(method.isVarargs(), method.parameters, false) },
					new String[] { 
					        new String(shownMethod.selector),
					        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
					        new String(shownMethod.declaringClass.shortReadableName()), 
							typesAsString(false, shownMethod.typeVariables, true),
					        typesAsString(method.isVarargs(), method.parameters, true) },
					(int) (messageSend.nameSourcePosition >>> 32),
					(int) messageSend.nameSourcePosition);		    
			}
			return;
		case ParameterizedMethodTypeMismatch :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.JavadocParameterizedMethodArgumentTypeMismatch,
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, false),
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
						typesAsString(false, ((ParameterizedGenericMethodBinding)shownMethod).typeArguments, true),
				        typesAsString(method.isVarargs(), method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);		    
			return;
		case TypeArgumentsForRawGenericMethod :
			problemMethod = (ProblemMethodBinding) method;
			shownMethod = problemMethod.closestMatch;
			this.handle(
				IProblem.JavadocTypeArgumentsForRawGenericMethod ,
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, false), 
				        new String(shownMethod.declaringClass.readableName()), 
				        typesAsString(method.isVarargs(), method.parameters, false) },
				new String[] { 
				        new String(shownMethod.selector),
				        typesAsString(shownMethod.isVarargs(), shownMethod.parameters, true), 
				        new String(shownMethod.declaringClass.shortReadableName()), 
				        typesAsString(method.isVarargs(), method.parameters, true) },
				(int) (messageSend.nameSourcePosition >>> 32),
				(int) messageSend.nameSourcePosition);		       
			return;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	// report issue
	this.handle(
		id,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void javadocInvalidParamTagName(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidParamTagName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidParamTypeParameter(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidParamTagTypeParameter, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocInvalidReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocInvalidSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
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
			case NonStaticReferenceInStaticContext :
				id = IProblem.JavadocNonStaticTypeFromStaticInvocation;
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
public void javadocInvalidValueReference(int sourceStart, int sourceEnd, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocInvalidValueReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMalformedSeeReference(int sourceStart, int sourceEnd) {
	this.handle(IProblem.JavadocMalformedSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissing(int sourceStart, int sourceEnd, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing|CompilerModifiers.AccOverriding)) != 0;
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
public void javadocMissingIdentifier(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingIdentifier, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingParamName(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingParamName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingParamTag(char[] name, int sourceStart, int sourceEnd, int modifiers) {
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing|CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(name) };
		this.handle(IProblem.JavadocMissingParamTag, arguments, arguments, sourceStart, sourceEnd);
	}
}
public void javadocMissingReference(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingSeeReference, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingReturnTag(int sourceStart, int sourceEnd, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing|CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		this.handle(IProblem.JavadocMissingReturnTag, NoArgument, NoArgument, sourceStart, sourceEnd);
	}
}
public void javadocMissingThrowsClassName(int sourceStart, int sourceEnd, int modifiers){
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers))
		this.handle(IProblem.JavadocMissingThrowsClassName, NoArgument, NoArgument, sourceStart, sourceEnd);
}
public void javadocMissingThrowsTag(TypeReference typeRef, int modifiers){
	boolean overriding = (modifiers & (CompilerModifiers.AccImplementing|CompilerModifiers.AccOverriding)) != 0;
	boolean report = (this.options.getSeverity(CompilerOptions.MissingJavadocTags) != ProblemSeverities.Ignore)
					&& (!overriding || this.options.reportMissingJavadocTagsOverriding);
	if (report && javadocVisibility(this.options.reportMissingJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] { String.valueOf(typeRef.resolvedType.sourceName()) };
		this.handle(IProblem.JavadocMissingThrowsTag, arguments, arguments, typeRef.sourceStart, typeRef.sourceEnd);
	}
}
public void javadocUndeclaredParamTagName(char[] token, int sourceStart, int sourceEnd, int modifiers) {
	if (javadocVisibility(this.options.reportInvalidJavadocTagsVisibility, modifiers)) {
		String[] arguments = new String[] {String.valueOf(token)};
		this.handle(IProblem.JavadocInvalidParamName, arguments, arguments, sourceStart, sourceEnd);
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
	if (modifiers < 0) return true;
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
public void localVariableCannotBeNull(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.LocalVariableCannotBeNull,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
}
public void localVariableCanOnlyBeNull(LocalVariableBinding local, ASTNode location) {
	String[] arguments = new String[] {new String(local.name)  };
	this.handle(
		IProblem.LocalVariableCanOnlyBeNull,
		arguments,
		arguments,
		location.sourceStart,
		location.sourceEnd);
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
public void methodMustOverride(AbstractMethodDeclaration method) {
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MethodMustOverride,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		method.sourceStart,
		method.sourceEnd);
}
public void methodNameClash(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		IProblem.MethodNameClash,
		new String[] {
			new String(currentMethod.selector),
			typesAsString(currentMethod.isVarargs(), currentMethod.parameters, false),
			new String(currentMethod.declaringClass.readableName()),
			typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, false),
			new String(inheritedMethod.declaringClass.readableName()),
		 }, 
		new String[] {
			new String(currentMethod.selector),
			typesAsString(currentMethod.isVarargs(), currentMethod.parameters, true),
			new String(currentMethod.declaringClass.shortReadableName()),
			typesAsString(inheritedMethod.isVarargs(), inheritedMethod.parameters, true),
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
public void missingEnumConstantCase(SwitchStatement switchStatement, FieldBinding enumConstant) {
	this.handle(
		IProblem.MissingEnumConstantCase,
		new String[] {new String(enumConstant.declaringClass.readableName()), new String(enumConstant.name) },
		new String[] {new String(enumConstant.declaringClass.shortReadableName()), new String(enumConstant.name) },
		switchStatement.expression.sourceStart,
		switchStatement.expression.sourceEnd);
}
public void missingOverrideAnnotation(AbstractMethodDeclaration method) {
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MissingOverrideAnnotation,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		method.sourceStart,
		method.sourceEnd);
}
public void missingDeprecatedAnnotationForField(FieldDeclaration field) {
	FieldBinding binding = field.binding;
	this.handle(
		IProblem.FieldMissingDeprecatedAnnotation,
		new String[] {new String(binding.declaringClass.readableName()), new String(binding.name), },
		new String[] {new String(binding.declaringClass.shortReadableName()), new String(binding.name), },
		field.sourceStart,
		field.sourceEnd);
}
public void missingDeprecatedAnnotationForMethod(AbstractMethodDeclaration method) {
	MethodBinding binding = method.binding;
	this.handle(
		IProblem.MethodMissingDeprecatedAnnotation,
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, false), new String(binding.declaringClass.readableName()), },
		new String[] {new String(binding.selector), typesAsString(binding.isVarargs(), binding.parameters, true), new String(binding.declaringClass.shortReadableName()),},
		method.sourceStart,
		method.sourceEnd);
}
public void missingDeprecatedAnnotationForType(TypeDeclaration type) {
	TypeBinding binding = type.binding;
	this.handle(
		IProblem.TypeMissingDeprecatedAnnotation,
		new String[] {new String(binding.readableName()), },
		new String[] {new String(binding.shortReadableName()),},
		type.sourceStart,
		type.sourceEnd);
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
public void missingValueForAnnotationMember(Annotation annotation, char[] memberName) {
	String memberString = new String(memberName);
	this.handle(
		IProblem.MissingValueForAnnotationMember,
		new String[] {new String(annotation.resolvedType.readableName()), memberString },
		new String[] {new String(annotation.resolvedType.shortReadableName()), memberString},
		annotation.sourceStart,
		annotation.sourceEnd);
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
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
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
	this.abortDueToInternalError(Messages.abort_missingCode); 
}
public void needToEmulateFieldAccess(FieldBinding field, ASTNode location, boolean isReadAccess) {
	this.handle(
		isReadAccess 
			? IProblem.NeedToEmulateFieldReadAccess
			: IProblem.NeedToEmulateFieldWriteAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
}
public void needToEmulateMethodAccess(
	MethodBinding method, 
	ASTNode location) {

	if (method.isConstructor()) {
		if (method.declaringClass.isEnum())
			return; // tolerate emulation for enum constructors, which can only be made private
		this.handle(
			IProblem.NeedToEmulateConstructorAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				typesAsString(method.isVarargs(), method.parameters, false)
			 }, 
			new String[] {
				new String(method.declaringClass.shortReadableName()), 
				typesAsString(method.isVarargs(), method.parameters, true)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
		return;
	}
	this.handle(
		IProblem.NeedToEmulateMethodAccess, 
		new String[] {
			new String(method.declaringClass.readableName()), 
			new String(method.selector), 
			typesAsString(method.isVarargs(), method.parameters, false)
		 }, 
		new String[] {
			new String(method.declaringClass.shortReadableName()), 
			new String(method.selector), 
			typesAsString(method.isVarargs(), method.parameters, true)
		 }, 
		location.sourceStart, 
		location.sourceEnd); 
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
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.NonGenericType,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false)},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void nonStaticAccessToStaticField(ASTNode location, FieldBinding field) {
	this.handle(
		IProblem.NonStaticAccessToStaticField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
}
public void nonStaticAccessToStaticMethod(ASTNode location, MethodBinding method) {
	this.handle(
		IProblem.NonStaticAccessToStaticMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false)},
		new String[] {new String(method.declaringClass.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true)},
		location.sourceStart,
		location.sourceEnd);
}
public void nonStaticContextForEnumMemberType(SourceTypeBinding type) {
	String[] arguments = new String[] {new String(type.sourceName())};
	this.handle(
		IProblem.NonStaticContextForEnumMemberType,
		arguments,
		arguments,
		type.sourceStart(),
		type.sourceEnd());
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
public void objectMustBeClass(SourceTypeBinding type) {
	this.handle(
		IProblem.ObjectMustBeClass,
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
	String[] arguments = new String[] {
		CharOperation.toString(compUnitDecl.compilationResult.compilationUnit.getPackageName()),
		compUnitDecl.currentPackage == null ? "" : CharOperation.toString(compUnitDecl.currentPackage.tokens), //$NON-NLS-1$
	};
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
		isIdentifier(currentToken)) {
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
		arguments = new String[] {Messages.parser_endOfConstructor}; 
	} else if(this.referenceContext instanceof MethodDeclaration) {
		arguments = new String[] {Messages.parser_endOfMethod}; 
	} else if(this.referenceContext instanceof TypeDeclaration) {
		arguments = new String[] {Messages.parser_endOfInitializer}; 
	} else {
		arguments = new String[] {Messages.parser_endOfFile}; 
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
			new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false), new String(type.enclosingType().readableName())},
			new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true), new String(type.enclosingType().shortReadableName())},
			AbortCompilation | Error,
			0,
			1);
	    return;
	}
    this.handle(
		IProblem.RawMemberTypeCannotBeParameterized,
		new String[] {new String(type.readableName()), typesAsString(false, argumentTypes, false), new String(type.enclosingType().readableName())},
		new String[] {new String(type.shortReadableName()), typesAsString(false, argumentTypes, true), new String(type.enclosingType().shortReadableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void rawTypeReference(ASTNode location, TypeBinding type) {
    this.handle(
		IProblem.RawTypeReference,
		new String[] {new String(type.readableName()), new String(type.erasure().readableName()), },
		new String[] {new String(type.shortReadableName()),new String(type.erasure().shortReadableName()),},
		location.sourceStart,
		location.sourceEnd);
}
public void recursiveConstructorInvocation(ExplicitConstructorCall constructorCall) {

	this.handle(
		IProblem.RecursiveConstructorInvocation,
		new String[] {
			new String(constructorCall.binding.declaringClass.readableName()), 
			typesAsString(constructorCall.binding.isVarargs(), constructorCall.binding.parameters, false)
		},
		new String[] {
			new String(constructorCall.binding.declaringClass.shortReadableName()), 
			typesAsString(constructorCall.binding.isVarargs(), constructorCall.binding.parameters, true)
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
	int endPos = scanner.currentPosition - 1;

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
	} else if (errorTokenName.equals(Scanner.INVALID_LOW_SURROGATE)) {
		flag = IProblem.InvalidLowSurrogate;
	} else if (errorTokenName.equals(Scanner.INVALID_HIGH_SURROGATE)) {
		flag = IProblem.InvalidHighSurrogate;
		// better locate the error message
		char[] source = scanner.source;
		int checkPos = scanner.startPosition + 1;
		while (checkPos <= endPos){
			if (source[checkPos] == '\\') break;
			checkPos ++;
		}
		endPos = checkPos - 1;
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
		endPos,
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
		fieldSourceStart(field,location),
		fieldSourceEnd(field, location)); 
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
public void superinterfacesCollide(TypeBinding type, ASTNode decl, TypeBinding superType, TypeBinding inheritedSuperType) {
	this.handle(
		IProblem.SuperInterfacesCollide,
		new String[] {new String(superType.readableName()), new String(inheritedSuperType.readableName()), new String(type.sourceName())},
		new String[] {new String(superType.shortReadableName()), new String(inheritedSuperType.shortReadableName()), new String(type.sourceName())},
		decl.sourceStart,
		decl.sourceEnd);
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
		isIdentifier(currentKind)) {
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
public void typeHiding(TypeParameter typeParam, Binding hidden) {
	TypeBinding hiddenType = (TypeBinding) hidden;
	this.handle(
		IProblem.TypeParameterHidingType,
		new String[] { new String(typeParam.name) , new String(hiddenType.readableName())  },
		new String[] { new String(typeParam.name) , new String(hiddenType.shortReadableName()) },
		typeParam.sourceStart,
		typeParam.sourceEnd);
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
private String typesAsString(boolean isVarargs, TypeBinding[] types, boolean makeShort) {
	StringBuffer buffer = new StringBuffer(10);
	for (int i = 0, length = types.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "); //$NON-NLS-1$
		TypeBinding type = types[i];
		boolean isVarargType = isVarargs && i == length-1;
		if (isVarargType) type = ((ArrayBinding)type).elementsType();
		buffer.append(new String(makeShort ? type.shortReadableName() : type.readableName()));
		if (isVarargType) buffer.append("..."); //$NON-NLS-1$
	}
	return buffer.toString();
}
public void undefinedAnnotationValue(TypeBinding annotationType, MemberValuePair memberValuePair) {
	String name = 	new String(memberValuePair.name);
	this.handle(
		IProblem.UndefinedAnnotationMember,
		new String[] { name, new String(annotationType.readableName())},
		new String[] {	name, new String(annotationType.shortReadableName())},
		memberValuePair.sourceStart,
		memberValuePair.sourceEnd);
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
public void uninitializedBlankFinalField(FieldBinding field, ASTNode location) {
	String[] arguments = new String[] {new String(field.readableName())};
	this.handle(
		IProblem.UninitializedBlankFinalField,
		arguments,
		arguments,
		fieldSourceStart(field, location),
		fieldSourceEnd(field, location));
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
		new String[]{ new String(castedExpressionType.readableName()), new String(castExpression.type.resolvedType.readableName())},
		new String[]{ new String(castedExpressionType.shortReadableName()), new String(castExpression.type.resolvedType.shortReadableName())},
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
public void unnecessaryNLSTags(int sourceStart, int sourceEnd) {
	this.handle(
		IProblem.UnnecessaryNLSTag,
		NoArgument,
		NoArgument,
		sourceStart,
		sourceEnd);
}
public void unqualifiedFieldAccess(NameReference reference, FieldBinding field) {
	this.handle(
		IProblem.UnqualifiedFieldAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		new String[] {new String(field.declaringClass.shortReadableName()), new String(field.name)},
		fieldSourceStart(field, reference),
		fieldSourceEnd(field, reference)); 

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
public void unhandledWarningToken(Expression token) {
	String[] arguments = new String[] { token.constant.stringValue() };
	this.handle(
		IProblem.UnhandledWarningToken,
		arguments,
		arguments,
		token.sourceStart,
		token.sourceEnd);
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
public void unsafeCast(CastExpression castExpression, Scope scope) {
	TypeBinding castedExpressionType = castExpression.expression.resolvedType;
	TypeBinding erasedCastType = castExpression.resolvedType.erasure();
	TypeBinding erasedLeaf = erasedCastType.leafComponentType();
	int dim = erasedCastType.dimensions();
	if (erasedLeaf.isGenericType()) {
		erasedCastType = scope.environment().createRawType((ReferenceBinding)erasedLeaf, erasedLeaf.enclosingType());
		if (dim > 0) erasedCastType = scope.environment().createArrayType(erasedCastType, dim);
	}	
	this.handle(
		IProblem.UnsafeGenericCast,
		new String[]{ 
			new String(castedExpressionType.readableName()), 
			new String(castExpression.resolvedType.readableName()),
			new String(erasedCastType.readableName()),
		},
		new String[]{ 
			new String(castedExpressionType.shortReadableName()), 
			new String(castExpression.resolvedType.shortReadableName()),
			new String(erasedCastType.shortReadableName()),
		},
		castExpression.sourceStart,
		castExpression.sourceEnd);
}
public void unsafeRawFieldAssignment(FieldBinding field, TypeBinding expressionType, ASTNode location) {
	this.handle(
		IProblem.UnsafeRawFieldAssignment,
		new String[] { 
		        new String(expressionType.readableName()), new String(field.name), new String(field.declaringClass.readableName()), new String(field.declaringClass.erasure().readableName()) },
		new String[] { 
		        new String(expressionType.shortReadableName()), new String(field.name), new String(field.declaringClass.shortReadableName()), new String(field.declaringClass.erasure().shortReadableName()) },
		fieldSourceStart(field,location),
		fieldSourceEnd(field, location)); 
}
public void unsafeRawGenericMethodInvocation(ASTNode location, MethodBinding rawMethod) {
    if (rawMethod.isConstructor()) {
		this.handle(
			IProblem.UnsafeRawGenericConstructorInvocation, // The generic constructor {0}({1}) of type {2} is applied to non-parameterized type arguments ({3})
			new String[] {
				new String(rawMethod.declaringClass.sourceName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
			 }, 
			new String[] {
				new String(rawMethod.declaringClass.sourceName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    } else {
		this.handle(
			IProblem.UnsafeRawGenericMethodInvocation,
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
			 }, 
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.original().parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    }
}
public void unsafeRawInvocation(ASTNode location, MethodBinding rawMethod) {
    if (rawMethod.isConstructor()) {
		this.handle(
			IProblem.UnsafeRawConstructorInvocation,
			new String[] {
				new String(rawMethod.declaringClass.readableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
				new String(rawMethod.declaringClass.erasure().readableName()),
			 }, 
			new String[] {
				new String(rawMethod.declaringClass.shortReadableName()),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
				new String(rawMethod.declaringClass.erasure().shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    } else {
		this.handle(
			IProblem.UnsafeRawMethodInvocation,
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, false),
				new String(rawMethod.declaringClass.readableName()),
				new String(rawMethod.declaringClass.erasure().readableName()),
			 }, 
			new String[] {
				new String(rawMethod.selector),
				typesAsString(rawMethod.original().isVarargs(), rawMethod.parameters, true),
				new String(rawMethod.declaringClass.shortReadableName()),
				new String(rawMethod.declaringClass.erasure().shortReadableName()),
			 }, 
			location.sourceStart,
			location.sourceEnd);    
    }
}
public void unsafeReturnTypeOverride(MethodBinding currentMethod, MethodBinding inheritedMethod, SourceTypeBinding type) {
	int start = type.sourceStart();
	int end = type.sourceEnd();
	if (currentMethod.declaringClass == type) {
		ASTNode location = ((MethodDeclaration) currentMethod.sourceMethod()).returnType;
		start = location.sourceStart();
		end = location.sourceEnd();
	}
	this.handle(
			IProblem.UnsafeReturnTypeOverride,
			new String[] {
				new String(currentMethod.returnType.readableName()),
				new String(currentMethod.selector),
				typesAsString(currentMethod.original().isVarargs(), currentMethod.original().parameters, false),
				new String(currentMethod.declaringClass.readableName()),
				new String(inheritedMethod.returnType.readableName()),
				new String(inheritedMethod.declaringClass.readableName()),
				//new String(inheritedMethod.returnType.erasure().readableName()),
			 }, 
			new String[] {
				new String(currentMethod.returnType.shortReadableName()),
				new String(currentMethod.selector),
				typesAsString(currentMethod.original().isVarargs(), currentMethod.original().parameters, true),
				new String(currentMethod.declaringClass.shortReadableName()),
				new String(inheritedMethod.returnType.shortReadableName()),
				new String(inheritedMethod.declaringClass.shortReadableName()),
				//new String(inheritedMethod.returnType.erasure().shortReadableName()),
			 }, 
			start,
			end);
}
public void unsafeTypeConversion(Expression expression, TypeBinding expressionType, TypeBinding expectedType) {
	this.handle(
		IProblem.UnsafeTypeConversion,
		new String[] { new String(expressionType.readableName()), new String(expectedType.readableName()), new String(expectedType.erasure().readableName()) },
		new String[] { new String(expressionType.shortReadableName()), new String(expectedType.shortReadableName()), new String(expectedType.erasure().shortReadableName()) },
		expression.sourceStart,
		expression.sourceEnd);    
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
				typesAsString(method.binding.isVarargs(), method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 }, 
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, true),
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
				typesAsString(method.binding.isVarargs(), method.binding.parameters, false),
				new String(exceptionType.readableName()),
			 }, 
			new String[] {
				new String(method.binding.declaringClass.shortReadableName()),
				new String(method.selector),
				typesAsString(method.binding.isVarargs(), method.binding.parameters, true),
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
			typesAsString(constructor.isVarargs(), constructor.parameters, false)
		 }, 
		new String[] {
			new String(constructor.declaringClass.shortReadableName()),
			typesAsString(constructor.isVarargs(), constructor.parameters, true)
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
			&& TypeIds.T_JavaLangObject == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.READRESOLVE)) {
		return;
	}
	// no report for serialization support 'Object writeReplace()'
	if (!method.isStatic()
			&& TypeIds.T_JavaLangObject == method.returnType.id
			&& method.parameters.length == 0
			&& CharOperation.equals(method.selector, TypeConstants.WRITEREPLACE)) {
		return;
	}
	this.handle(
			IProblem.UnusedPrivateMethod,
		new String[] {
			new String(method.declaringClass.readableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, false)
		 }, 
		new String[] {
			new String(method.declaringClass.shortReadableName()),
			new String(method.selector),
			typesAsString(method.isVarargs(), method.parameters, true)
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
public void varargsArgumentNeedCast(MethodBinding method, TypeBinding argumentType, InvocationSite location) {
	TypeBinding lastParam = method.parameters[method.parameters.length-1];
	if (method.isConstructor()) {
		this.handle(
			IProblem.ConstructorVarargsArgumentNeedCast,
			new String[] {new String(argumentType.readableName()), new String(lastParam.readableName()), new String(method.declaringClass.readableName()), typesAsString(method.isVarargs(), method.parameters, false), },
			new String[] {new String(argumentType.shortReadableName()), new String(lastParam.shortReadableName()), new String(method.declaringClass.shortReadableName()), typesAsString(method.isVarargs(), method.parameters, true), },
			location.sourceStart(),
			location.sourceEnd());
	} else {
		this.handle(
			IProblem.MethodVarargsArgumentNeedCast,
			new String[] { new String(argumentType.readableName()), new String(lastParam.readableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, false), new String(method.declaringClass.readableName()), },
			new String[] { new String(argumentType.shortReadableName()), new String(lastParam.shortReadableName()), new String(method.selector), typesAsString(method.isVarargs(), method.parameters, true), new String(method.declaringClass.shortReadableName()), },
			location.sourceStart(),
			location.sourceEnd());
	}
}
public void varargsConflict(MethodBinding method1, MethodBinding method2, SourceTypeBinding type) {
	this.handle(
		IProblem.VarargsConflict,
		new String[] { 
		        new String(method1.selector),
		        typesAsString(method1.isVarargs(), method1.parameters, false),
		        new String(method1.declaringClass.readableName()),
		        typesAsString(method2.isVarargs(), method2.parameters, false),
		        new String(method2.declaringClass.readableName())
		},
		new String[] { 
		        new String(method1.selector),
		        typesAsString(method1.isVarargs(), method1.parameters, true),
		        new String(method1.declaringClass.shortReadableName()),
		        typesAsString(method2.isVarargs(), method2.parameters, true),
		        new String(method2.declaringClass.shortReadableName())
		},
		method1.declaringClass == type ? method1.sourceStart() : type.sourceStart(),
		method1.declaringClass == type ? method1.sourceEnd() : type.sourceEnd());
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
		if (parameter.isWildcard() && (((WildcardBinding) parameter).boundKind != Wildcard.SUPER)) {
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
				typesAsString(method.isVarargs(), method.parameters, false),
				new String(receiverType.readableName()),
				typesAsString(false, arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 }, 
			new String[] {
				new String(receiverType.sourceName()),
				typesAsString(method.isVarargs(), method.parameters, true),
				new String(receiverType.shortReadableName()),
				typesAsString(false, arguments, true),
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
				typesAsString(method.isVarargs(), method.parameters, false),
				new String(receiverType.readableName()),
				typesAsString(false, arguments, false),
				new String(offendingArgument.readableName()),
				new String(offendingParameter.readableName()),
			 }, 
			new String[] {
				new String(method.selector),
				typesAsString(method.isVarargs(), method.parameters, true),
				new String(receiverType.shortReadableName()),
				typesAsString(false, arguments, true),
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

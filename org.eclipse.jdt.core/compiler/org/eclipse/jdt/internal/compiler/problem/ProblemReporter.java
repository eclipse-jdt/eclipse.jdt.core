package org.eclipse.jdt.internal.compiler.problem;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.util.*;
import org.eclipse.jdt.internal.compiler.impl.*;

public class ProblemReporter extends ProblemHandler implements ConfigurableProblems, ProblemIrritants, ProblemReasons{
	public ReferenceContext referenceContext;
public ProblemReporter(IErrorHandlingPolicy policy, CompilerOptions options, IProblemFactory problemFactory) {
	super(policy, options, problemFactory);
}
public void abortDueToInternalError(String errorMessage) {
	this.handle(
		UnclassifiedProblem,
		new String[] {errorMessage},
		Error | Abort,
		0,
		0);
}
public void abortDueToInternalError(String errorMessage, AstNode location) {
	this.handle(
		UnclassifiedProblem,
		new String[] {errorMessage},
		Error | Abort,
		location.sourceStart(),
		location.sourceEnd());
}
public void abstractMethodCannotBeOverridden(SourceTypeBinding type, MethodBinding concreteMethod) {

	this.handle(
		// %1 must be abstract since it cannot override the inherited package-private abstract method %2
		AbstractMethodCannotBeOverridden,
		new String[] {new String(type.sourceName()), new String(concreteMethod.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodInAbstractClass(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		AbstractMethodInAbstractClass,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void abstractMethodMustBeImplemented(SourceTypeBinding type, MethodBinding abstractMethod) {
	this.handle(
		// Must implement the inherited abstract method %1
		// 8.4.3 - Every non-abstract subclass of an abstract type, A, must provide a concrete implementation of all of A's methods.
		AbstractMethodMustBeImplemented,
		new String[] {
			new String(
				CharOperation.concat(
					abstractMethod.declaringClass.readableName(),
					abstractMethod.readableName(),
					'.'))},
		type.sourceStart(),
		type.sourceEnd());
}
public void abstractMethodNeedingNoBody(AbstractMethodDeclaration method, CompilationResult result) {
	this.handle(
		BodyForAbstractMethod,
		new String[0],
		method.sourceStart,
		method.sourceEnd,
		method,
		result);
}
public void alreadyDefinedLabel(char[] labelName, AstNode location) {
	this.handle(
		DuplicateLabel,
		new String[] {new String(labelName)},
		location.sourceStart(),
		location.sourceEnd());
}
public void anonymousClassCannotExtendFinalClass(Expression expression, TypeBinding type) {
	this.handle(
		AnonymousClassCannotExtendFinalClass,
		new String[] {new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void argumentTypeCannotBeVoid(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	this.handle(
		ArgumentTypeCannotBeVoid,
		new String[] {new String(methodDecl.selector), new String(arg.name)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void argumentTypeCannotBeVoidArray(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	this.handle(
		ArgumentTypeCannotBeVoidArray,
		new String[] {new String(methodDecl.selector), new String(arg.name)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void argumentTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				ArgumentProblemBase + problemId,
				new String[] {new String(methodDecl.selector), arg.name(), new String(expectedType.readableName())},
				arg.type.sourceStart(),
				arg.type.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void arrayConstantsOnlyInArrayInitializers(int sourceStart, int sourceEnd) {
	this.handle(
		ArrayConstantsOnlyInArrayInitializers,
		new String[0],
		sourceStart,
		sourceEnd);
}
public void attemptToReturnNonVoidExpression(ReturnStatement returnStatement, TypeBinding expectedType) {
	this.handle(
		VoidMethodReturnsValue,
		new String[] {new String(expectedType.readableName())},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void attemptToReturnVoidValue(ReturnStatement returnStatement) {
	this.handle(
		MethodReturnsVoid,
		new String[] {},
		returnStatement.sourceStart,
		returnStatement.sourceEnd);
}
public void bytecodeExceeds64KLimit(AbstractMethodDeclaration location) {
	this.handle(
		BytecodeExceeds64KLimit,
		new String[] {new String(location.selector)},
		Error | Abort,
		location.sourceStart(),
		location.sourceEnd());
}
public void bytecodeExceeds64KLimit(TypeDeclaration location) {
	this.handle(
		BytecodeExceeds64KLimitForClinit,
		new String[0],
		Error | Abort,
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotAllocateVoidArray(Expression expression) {
	this.handle(
		CannotAllocateVoidArray,
		new String[] {},
		expression.sourceStart,
		expression.sourceEnd);
}
public void cannotAssignToFinalField(FieldBinding field, AstNode location) {
	this.handle(
		FinalFieldAssignment,
		new String[] {
			(field.declaringClass == null ? "array"/*nonNLS*/ : new String(field.declaringClass.readableName())),
			new String(field.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotAssignToFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	this.handle(
		FinalOuterLocalAssignment,
		new String[] {new String(local.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotDeclareLocalInterface(char[] interfaceName, int sourceStart, int sourceEnd) {
	this.handle(
		CannotDefineInterfaceInLocalType,
		new String[] {new String(interfaceName)},
		sourceStart,
		sourceEnd);
}
public void cannotDefineDimensionsAndInitializer(ArrayAllocationExpression expresssion) {
	this.handle(
		CannotDefineDimensionExpressionsWithInit,
		new String[0],
		expresssion.sourceStart,
		expresssion.sourceEnd);
}
public void cannotDireclyInvokeAbstractMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		DirectInvocationOfAbstractMethod,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void cannotImportPackage(ImportReference importRef) {
	this.handle(
		CannotImportPackage,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart(),
		importRef.sourceEnd());
}
public void cannotInstantiate(TypeReference typeRef, TypeBinding type) {
	this.handle(
		InvalidClassInstantiation,
		new String[] {new String(type.readableName())},
		typeRef.sourceStart,
		typeRef.sourceEnd);
}
public void cannotReferToNonFinalOuterLocal(LocalVariableBinding local, AstNode location) {
	this.handle(
		OuterLocalMustBeFinal,
		new String[] {new String(local.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotReturnInInitializer(AstNode location) {
	this.handle(
		CannotReturnInInitializer,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
}
public void cannotThrowNull(ThrowStatement statement) {
	this.handle(
		CannotThrowNull,
		new String[0],
		statement.sourceStart,
		statement.sourceEnd);
}
public void cannotThrowType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	this.handle(
		CannotThrowType,
		new String[] {new String(expectedType.readableName())},
		exceptionType.sourceStart(),
		exceptionType.sourceEnd());
}
public void cannotUseSuperInJavaLangObject(AstNode reference) {
	this.handle(
		ObjectHasNoSuperclass,
		new String[0],
		reference.sourceStart,
		reference.sourceEnd);
}
public void caseExpressionMustBeConstant(Expression expression) {
	this.handle(
		NonConstantExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void classExtendFinalClass(SourceTypeBinding type, TypeReference superclass, TypeBinding expectedType) {
	this.handle(
		ClassExtendFinalClass,
		new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
		superclass.sourceStart(),
		superclass.sourceEnd());
}
public void codeSnippetMissingClass(String missing, int start, int end) {
	this.handle(
		CodeSnippetMissingClass,
		new String[]{ missing },
		Error | Abort,
		start,
		end);
}
public void codeSnippetMissingMethod(String className, String missingMethod, String argumentTypes, int start, int end) {
	this.handle(
		CodeSnippetMissingMethod,
		new String[]{ className, missingMethod, argumentTypes },
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
	int errorThreshold = options.errorThreshold;
	int warningThreshold = options.warningThreshold;
	
	switch(problemId){

		case UnreachableCatch :
		case CodeCannotBeReached :
			if ((errorThreshold & UnreachableCode) != 0){
				return Error;
			}
			if ((warningThreshold & UnreachableCode) != 0){
				return Warning;
			}
			return Ignore;

		case MaskedCatch : 
			if ((errorThreshold & MaskedCatchBlock) != 0){
				return Error;
			}
			if ((warningThreshold & MaskedCatchBlock) != 0){
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
		case ImportProblemBase + NotFound :
		case ImportProblemBase + NotVisible :
		case ImportProblemBase + Ambiguous :
		case ImportProblemBase + InternalNameProvided :
		case ImportProblemBase + InheritedNameHidesEnclosingName :
		case DuplicateImport :
		case ConflictingImport :
		case CannotImportPackage :
			if ((errorThreshold & ImportProblem) != 0){
				return Error;
			}
			if ((warningThreshold & ImportProblem) != 0){
				return Warning;
			}
			return Ignore;
/*		
		case UnnecessaryEnclosingInstanceSpecification :
			if ((errorThreshold & UnnecessaryEnclosingInstance) != 0){
				return Error;
			}
			if ((warningThreshold & UnnecessaryEnclosingInstance) != 0){
				return Warning;
			}
			return Ignore;
*/		
		case MethodButWithConstructorName :
			if ((errorThreshold & MethodWithConstructorName) != 0){
				return Error;
			}
			if ((warningThreshold & MethodWithConstructorName) != 0){
				return Warning;
			}
			return Ignore;
		
		case OverridingNonVisibleMethod :
			if ((errorThreshold & OverriddenPackageDefaultMethod) != 0){
				return Error;
			}
			if ((warningThreshold & OverriddenPackageDefaultMethod) != 0){
				return Warning;
			}
			return Ignore;

		case OverridingDeprecatedMethod :				
		case UsingDeprecatedType :				
		case UsingDeprecatedMethod :
		case UsingDeprecatedConstructor :
		case UsingDeprecatedField :
			if ((errorThreshold & UsingDeprecatedAPI) != 0){
				return Error;
			}
			if ((warningThreshold & UsingDeprecatedAPI) != 0){
				return Warning;
			}
			return Ignore;
		
		case LocalVariableIsNeverUsed :
			if ((errorThreshold & UnusedLocalVariable) != 0){
				return Error;
			}
			if ((warningThreshold & UnusedLocalVariable) != 0){
				return Warning;
			}
			return Ignore;
		
		case ArgumentIsNeverUsed :
			if ((errorThreshold & UnusedArgument) != 0){
				return Error;
			}
			if ((warningThreshold & UnusedArgument) != 0){
				return Warning;
			}
			return Ignore;

		case NoImplicitStringConversionForCharArrayExpression :
			if ((errorThreshold & TemporaryWarning) != 0){
				return Error;
			}
			if ((warningThreshold & TemporaryWarning) != 0){
				return Warning;
			}
			return Ignore;

		case NeedToEmulateFieldReadAccess :
		case NeedToEmulateFieldWriteAccess :
		case NeedToEmulateMethodAccess :
		case NeedToEmulateConstructorAccess :			
			if ((errorThreshold & AccessEmulation) != 0){
				return Error;
			}
			if ((warningThreshold & AccessEmulation) != 0){
				return Warning;
			}
			return Ignore;
		case NonExternalizedStringLiteral :
			if ((errorThreshold & NonExternalizedString) != 0){
				return Error;
			}
			if ((warningThreshold & NonExternalizedString) != 0){
				return Warning;
			}
			return Ignore;
		
		default:
			return Error;
	}
}
public void conditionalArgumentsIncompatibleTypes(ConditionalExpression expression, TypeBinding trueType, TypeBinding falseType) {
	this.handle(
		IncompatibleTypesInConditionalOperator,
		new String[] {new String(trueType.readableName()), new String(falseType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void conflictingImport(ImportReference importRef) {
	this.handle(
		ConflictingImport,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart(),
		importRef.sourceEnd());
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
				Radix = "Hexa";
			} else {
				radix = 8;
				Radix = "Octal";
			}
			//look for the first digit that is incorrect
			int place = -1;
			label : for (int i = radix == 8 ? 1 : 2; i < source.length; i++) {
				if (Character.digit(source[i], radix) == -1) {
					place = i;
					break label;
				}
			}

			this.handle(
				NumericValueOutOfRange,
				new String[] {Radix + " " + new String(source) + " (digit " + new String(new char[] {source[place]}) + ")"},
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

	this.handle(
		NumericValueOutOfRange,
		new String[] {new String(lit.source())},
		lit.sourceStart,
		lit.sourceEnd);
}
public void deprecatedField(FieldBinding field, AstNode location) {
	this.handle(
		UsingDeprecatedField,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void deprecatedMethod(MethodBinding method, AstNode location) {
	if (method.isConstructor())
		this.handle(
			UsingDeprecatedConstructor,
			new String[] {new String(method.declaringClass.readableName()), parametersAsString(method)},
			location.sourceStart,
			location.sourceEnd);
	else
		this.handle(
			UsingDeprecatedMethod,
			new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
			location.sourceStart,
			location.sourceEnd);
}
public void deprecatedType(TypeBinding type, AstNode location) {
	if (location == null) return; // 1G828DN - no type ref for synthetic arguments
	this.handle(
		UsingDeprecatedType,
		new String[] {new String(type.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void duplicateCase(Case statement, Constant constant) {
	this.handle(
		DuplicateCase,
		new String[] {String.valueOf(constant.intValue())},
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateDefaultCase(DefaultCase statement) {
	this.handle(
		DuplicateDefaultCase,
		new String[0],
		statement.sourceStart,
		statement.sourceEnd);
}
public void duplicateFieldInType(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		DuplicateField,
		new String[] {new String(type.sourceName()), fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void duplicateImport(ImportReference importRef) {
	this.handle(
		DuplicateImport,
		new String[] {CharOperation.toString(importRef.tokens)},
		importRef.sourceStart(),
		importRef.sourceEnd());
}
public void duplicateInitializationOfBlankFinalField(FieldBinding field, Reference reference) {
	this.handle(
		DuplicateBlankFinalFieldInitialization,
		new String[] {new String(field.readableName())},
		reference.sourceStart(),
		reference.sourceEnd());
}
public void duplicateInitializationOfFinalLocal(LocalVariableBinding local, NameReference reference) {
	this.handle(
		DuplicateFinalLocalInitialization,
		new String[] {new String(local.readableName())},
		reference.sourceStart(),
		reference.sourceEnd());
}
public void duplicateMethodInType(SourceTypeBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		DuplicateMethod,
		new String[] {new String(methodDecl.selector), new String(type.sourceName())},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void duplicateModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
/* to highlight modifiers use:
	this.handle(
		new Problem(
			DuplicateModifierForField,
			new String[] {fieldDecl.name()},
			fieldDecl.modifiers.sourceStart(),
			fieldDecl.modifiers.sourceEnd()));
*/

	this.handle(
		DuplicateModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void duplicateModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		DuplicateModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void duplicateModifierForType(SourceTypeBinding type) {
	this.handle(
		DuplicateModifierForType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void duplicateModifierForVariable(LocalDeclaration localDecl) {
	this.handle(
		DuplicateModifierForVariable,
		new String[] {localDecl.name()},
		localDecl.sourceStart(),
		localDecl.sourceEnd());
}
public void duplicateNestedType(TypeDeclaration typeDecl) {
	this.handle(
		DuplicateNestedType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd());
}
public void duplicateSuperinterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		DuplicateSuperInterface,
		new String[] {
			new String(superType.readableName()),
			new String(type.sourceName())},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd());
}
public void duplicateTypes(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		DuplicateTypes,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd(),
		compUnitDecl.compilationResult);
}
public void errorNoMethodFor(MessageSend messageSend, TypeBinding recType, TypeBinding[] params) {
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "/*nonNLS*/);
		buffer.append(new String(params[i].readableName()));
	}

	this.handle(
		recType.isArrayType() ? NoMessageSendOnArrayType : NoMessageSendOnBaseType,
		new String[] {new String(recType.readableName()), new String(messageSend.selector), buffer.toString()},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void errorThisSuperInStatic(AstNode reference) {
	this.handle(
		ThisInStaticContext,
		new String[] {reference.isSuper() ? "super"/*nonNLS*/ : "this"/*nonNLS*/},
		reference.sourceStart,
		reference.sourceEnd);
}
public void exceptionTypeProblem(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, TypeReference exceptionType, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				ExceptionTypeProblemBase + problemId,
				new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
				exceptionType.sourceStart(),
				exceptionType.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void fieldsOrThisBeforeConstructorInvocation(ThisReference reference) {
	this.handle(
		ThisSuperDuringConstructorInvocation,
		new String[0],
		reference.sourceStart,
		reference.sourceEnd);
}
public void fieldTypeProblem(SourceTypeBinding type, FieldDeclaration fieldDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				FieldTypeProblemBase + problemId,
				new String[] {fieldDecl.name(), new String(type.sourceName()), new String(expectedType.readableName())},
				fieldDecl.type.sourceStart(),
				fieldDecl.type.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void finalMethodCannotBeOverridden(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		// Cannot override the final method from %1
		// 8.4.3.3 - Final methods cannot be overridden or hidden.
		FinalMethodCannotBeOverridden,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void forwardReference(Reference reference, int indexInQualification, TypeBinding type) {
	this.handle(
		ReferenceToForwardField,
		new String[] {},
		reference.sourceStart,
		reference.sourceEnd);
}
// use this private API when the compilation unit result can be found through the
// reference context. Otherwise, use the other API taking a problem and a compilation result
// as arguments

private void handle(
	int problemId, 
	String[] problemArguments,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
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
	int severity,
	int problemStartPosition, 
	int problemEndPosition){

	this.handle(
			problemId,
			problemArguments,
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
	int problemStartPosition, 
	int problemEndPosition,
	CompilationResult unitResult){

	this.handle(
			problemId,
			problemArguments,
			problemStartPosition,
			problemEndPosition,
			referenceContext, 
			unitResult); 
	referenceContext = null;
}
public void hidingEnclosingType(TypeDeclaration typeDecl) {
	this.handle(
		HidingEnclosingType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd());
}
public void hierarchyCircularity(SourceTypeBinding sourceType, ReferenceBinding superType, TypeReference reference) {
	int start = 0;
	int end = 0;
	String typeName = ""/*nonNLS*/;

	if (reference == null) {	// can only happen when java.lang.Object is busted
		start = sourceType.sourceStart();
		end = sourceType.sourceEnd();
		typeName = new String(superType.readableName());
	} else {
		start = reference.sourceStart();
		end = reference.sourceEnd();
		typeName = CharOperation.toString(reference.getTypeName());
	}

	if (sourceType == superType)
		this.handle(
			HierarchyCircularitySelfReference,
			new String[] {new String(sourceType.sourceName()), typeName},
			start,
			end);
	else
		this.handle(
			HierarchyCircularity,
			new String[] {new String(sourceType.sourceName()), typeName},
			start,
			end);
}
public void hierarchyHasProblems(SourceTypeBinding type) {
	this.handle(
		HierarchyHasProblems,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalAbstractModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IllegalAbstractModifierCombinationForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void illegalModifierCombinationFinalAbstractForClass(SourceTypeBinding type) {
	this.handle(
		IllegalModifierCombinationFinalAbstractForClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierCombinationFinalVolatileForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IllegalModifierCombinationFinalVolatileForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void illegalModifierForArgument(SourceTypeBinding type, AbstractMethodDeclaration methodDecl, Argument arg) {
	this.handle(
		IllegalModifierForArgument,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector), arg.name()},
		arg.sourceStart(),
		arg.sourceEnd());
}
public void illegalModifierForClass(SourceTypeBinding type) {
	this.handle(
		IllegalModifierForClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IllegalModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void illegalModifierForInterface(SourceTypeBinding type) {
	this.handle(
		IllegalModifierForInterface,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForInterfaceField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IllegalModifierForInterfaceField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void illegalModifierForInterfaceMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IllegalModifierForInterfaceMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void illegalModifierForLocalClass(SourceTypeBinding type) {
	this.handle(
		IllegalModifierForLocalClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberClass(SourceTypeBinding type) {
	this.handle(
		IllegalModifierForMemberClass,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMemberInterface(SourceTypeBinding type) {
	this.handle(
		IllegalModifierForMemberInterface,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IllegalModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void illegalModifierForVariable(LocalDeclaration localDecl) {
	this.handle(
		IllegalModifierForVariable,
		new String[] {localDecl.name()},
		localDecl.sourceStart(),
		localDecl.sourceEnd());
}
public void illegalPrimitiveOrArrayTypeForEnclosingInstance(TypeBinding enclosingType, AstNode location) {
	this.handle(
		IllegalPrimitiveOrArrayTypeForEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void illegalStaticModifierForMemberType(SourceTypeBinding type) {
	this.handle(
		IllegalStaticModifierForMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForField(ReferenceBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		IllegalVisibilityModifierCombinationForField,
		new String[] {new String(fieldDecl.name())},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void illegalVisibilityModifierCombinationForMemberType(SourceTypeBinding type) {
	this.handle(
		IllegalVisibilityModifierCombinationForMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void illegalVisibilityModifierCombinationForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		IllegalVisibilityModifierCombinationForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void illegalVisibilityModifierForInterfaceMemberType(SourceTypeBinding type) {
	this.handle(
		IllegalVisibilityModifierForInterfaceMemberType,
		new String[] {new String(type.sourceName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void importProblem(ImportReference importRef, Binding expectedImport) {
	int problemId = expectedImport.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				ImportProblemBase + problemId,
				new String[] {CharOperation.toString(importRef.tokens)},
				importRef.sourceStart(),
				importRef.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void incompatibleExceptionInThrowsClause(SourceTypeBinding type, MethodBinding currentMethod, MethodBinding inheritedMethod, ReferenceBinding exceptionType) {
	if (type == currentMethod.declaringClass)
		this.handle(
			// Exception %1 is not compatible with throws clause in %2
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IncompatibleExceptionInThrowsClause,
			new String[] {
				new String(exceptionType.sourceName()),
				new String(
					CharOperation.concat(
						inheritedMethod.declaringClass.readableName(),
						inheritedMethod.readableName(),
						'.'))},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else	
		this.handle(
			// Exception %1 in throws clause of %2 is not compatible with %3
			// 9.4.4 - The type of exception in the throws clause is incompatible.
			IncompatibleExceptionInInheritedMethodThrowsClause,
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
			type.sourceStart(),
			type.sourceEnd());
}
public void incompatibleReturnType(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	StringBuffer methodSignature = new StringBuffer();
	methodSignature
		.append(inheritedMethod.declaringClass.readableName())
		.append('.')
		.append(inheritedMethod.readableName());

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IncompatibleReturnType,
		new String[] {methodSignature.toString()},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void incorrectEnclosingInstanceReference(
	QualifiedThisReference reference, 
	TypeBinding qualificationType) {
		
	this.handle(
		IncorrectEnclosingInstanceReference, 
		new String[] { new String(qualificationType.readableName())}, 
		reference.sourceStart, 
		reference.sourceEnd); 
}
public void incorrectLocationForEmptyDimension(ArrayAllocationExpression expression, int index) {
	this.handle(
		IllegalDimension,
		new String[0],
		expression.dimensions[index + 1].sourceStart,
		expression.dimensions[index + 1].sourceEnd);
}
public void incorrectSwitchType(Expression expression, TypeBinding testType) {
	this.handle(
		IncorrectSwitchType,
		new String[] {new String(testType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void inheritedMethodReducesVisibility(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	this.handle(
		// The method %1 cannot hide the public abstract method in %2
		InheritedMethodReducesVisibility,
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void inheritedMethodsHaveIncompatibleReturnTypes(SourceTypeBinding type, MethodBinding[] inheritedMethods, int length) {
	StringBuffer methodSignatures = new StringBuffer();
	for (int i = length; --i >= 0;) {
		methodSignatures
			.append(inheritedMethods[i].declaringClass.readableName())
			.append('.')
			.append(inheritedMethods[i].readableName());
		if (i != 0)
			methodSignatures.append(", "/*nonNLS*/);
	}

	this.handle(
		// Return type is incompatible with %1
		// 9.4.2 - The return type from the method is incompatible with the declaration.
		IncompatibleReturnType,
		new String[] {methodSignatures.toString()},
		type.sourceStart(),
		type.sourceEnd());
}
public void initializerMustCompleteNormally(FieldDeclaration fieldDecl) {
	this.handle(
		InitializerMustCompleteNormally,
		new String[0],
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void innerTypesCannotDeclareStaticInitializers(ReferenceBinding innerType, AstNode location) {
	this.handle(
		CannotDefineStaticInitializerInLocalType,
		new String[] {new String(innerType.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void interfaceCannotHaveConstructors(ConstructorDeclaration constructor, CompilationResult result) {
	this.handle(
		InterfaceCannotHaveConstructors,
		new String[0],
		constructor.sourceStart,
		constructor.sourceEnd,
		constructor,
		result);
}
public void interfaceCannotHaveInitializers(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		InterfaceCannotHaveInitializers,
		new String[] {new String(type.sourceName())},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void invalidBreak(AstNode location) {
	this.handle(
		InvalidBreak,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
}
public void invalidConstructor(Statement statement, MethodBinding method) {
	// CODE should be UPDATED according to error coding in the different method binding errors
	// The different targetted errors should be :
	// UndefinedConstructor
	//	NotVisibleConstructor
	//	AmbiguousConstructor

	int flag = UndefinedConstructor; //default...
	switch (method.problemId()) {
		case NotFound :
			flag = UndefinedConstructor;
			break;
		case NotVisible :
			flag = NotVisibleConstructor;
			break;
		case Ambiguous :
			flag = AmbiguousConstructor;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(method.declaringClass.readableName()), parametersAsString(method)},
		statement.sourceStart,
		statement.sourceEnd);
}
public void invalidContinue(AstNode location) {
	this.handle(
		InvalidContinue,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
}
public void invalidEnclosingType(Expression expression, TypeBinding type, TypeBinding enclosingType) {
	//CODE should be UPDATED according to error coding in the different type binding errors
	//The different targetted errors should be :
		//UndefinedType
		//NotVisibleType
		//AmbiguousType
		//InternalNameProvided

	int flag = UndefinedType; // default
	switch (type.problemId()) {
		case NotFound : // 1
			flag = UndefinedType;
			break;
		case NotVisible : // 2
			flag = NotVisibleType;
			break;
		case Ambiguous : // 3
			flag = AmbiguousType;
			break;
		case InternalNameProvided :
			flag = InternalTypeNameProvided;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(enclosingType.readableName()) + "." + new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidExpressionAsStatement(Expression expression){
	this.handle(
		InvalidExpressionAsStatement,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidField(FieldReference fieldRef, TypeBinding searchedType) {
	int severity = Error;
	int flag = UndefinedField;
	FieldBinding field = fieldRef.binding;
	switch (field.problemId()) {
		case NotFound :
			flag = UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = NotVisibleField;
			break;
		case Ambiguous :
			flag = AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(field.readableName())},
		severity,
		fieldRef.sourceStart,
		fieldRef.sourceEnd);
}
public void invalidField(NameReference nameRef, FieldBinding field) {
	int flag = UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = UndefinedField;
			break;
		case NotVisible :
			flag = NotVisibleField;
			break;
		case Ambiguous :
			flag = AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	this.handle(
		flag,
		new String[] {new String(field.readableName())},
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
			NoFieldOnBaseType,
			new String[] {
				new String(searchedType.readableName()),
				CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index)),
				new String(nameRef.tokens[index])},
			nameRef.sourceStart(),
			nameRef.sourceEnd());
		return;
	}

	int flag = UndefinedField;
	switch (field.problemId()) {
		case NotFound :
			flag = UndefinedField;
/* also need to check that the searchedType is the receiver type
			if (searchedType.isHierarchyInconsistent())
				severity = SecondaryError;
*/
			break;
		case NotVisible :
			flag = NotVisibleField;
			break;
		case Ambiguous :
			flag = AmbiguousField;
			break;
		case NonStaticReferenceInStaticContext :
			flag = NonStaticFieldFromStaticInvocation;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = InstanceFieldDuringConstructorInvocation;
			break;
		case InheritedNameHidesEnclosingName :
			flag = InheritedFieldHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
	this.handle(
		flag, 
		new String[] {CharOperation.toString(CharOperation.subarray(nameRef.tokens, 0, index + 1))},
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

	int flag = UndefinedMethod; //default...
	switch (method.problemId()) {
		case NotFound :
			flag = UndefinedMethod;
			break;
		case NotVisible :
			flag = NotVisibleMethod;
			break;
		case Ambiguous :
			flag = AmbiguousMethod;
			break;
		case InheritedNameHidesEnclosingName :
			flag = InheritedMethodHidesEnclosingName;
			break;
		case NonStaticReferenceInConstructorInvocation :
			flag = InstanceMethodDuringConstructorInvocation;
			break;
		case NonStaticReferenceInStaticContext :
			flag = StaticMethodRequested;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	if (flag == UndefinedMethod) {
		ProblemMethodBinding problemMethod = (ProblemMethodBinding) method;
		if (problemMethod.closestMatch != null) {
				this.handle(
					ParameterMismatch,
					new String[] {
						new String(problemMethod.closestMatch.declaringClass.readableName()),
						new String(problemMethod.closestMatch.selector),
						parametersAsString(problemMethod.closestMatch),
						parametersAsString(method)},
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
		(int) (messageSend.nameSourcePosition >>> 32),
		(int) messageSend.nameSourcePosition);
}
public void invalidNullToSynchronize(Expression expression) {
	this.handle(
		InvalidNullToSynchronized,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(BinaryExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		InvalidOperator,
		new String[] {
			expression.operatorToString(),
			new String(leftType.readableName()) + ", "/*nonNLS*/ + new String(rightType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidOperator(CompoundAssignment assign, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		InvalidOperator,
		new String[] {
			assign.operatorToString(),
			new String(leftType.readableName()) + ", "/*nonNLS*/ + new String(rightType.readableName())},
		assign.sourceStart,
		assign.sourceEnd);
}
public void invalidOperator(UnaryExpression expression, TypeBinding type) {
	this.handle(
		InvalidOperator,
		new String[] {expression.operatorToString(), new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidSuperclass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				InvalidSuperclassBase + problemId,
				new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
				superclassRef.sourceStart(),
				superclassRef.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void invalidSuperinterface(SourceTypeBinding type, TypeReference superinterfaceRef, ReferenceBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				InvalidInterfaceBase + problemId,
				new String[] {new String(expectedType.readableName()), new String(type.sourceName())},
				superinterfaceRef.sourceStart(),
				superinterfaceRef.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void invalidType(Expression expression, TypeBinding type) {
	// CODE should be UPDATED according to error coding in the different type binding errors
	//The different targetted errors should be :
		//UndefinedType
		//NotVisibleType
		//AmbiguousType
		//InternalTypeNameProvided
		//InheritedTypeHidesEnclosingName

	int flag = UndefinedType; // default
	switch (type.problemId()) {
		case NotFound :
			flag = UndefinedType;
			break;
		case NotVisible :
			flag = NotVisibleType;
			break;
		case Ambiguous :
			flag = AmbiguousType;
			break;
		case InternalNameProvided :
			flag = InternalTypeNameProvided;
			break;
		case InheritedNameHidesEnclosingName :
			flag = InheritedTypeHidesEnclosingName;
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}

	this.handle(
		flag,
		new String[] {new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeReference(Expression expression) {
	this.handle(
		InvalidTypeExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidTypeToSynchronize(Expression expression, TypeBinding type) {
	this.handle(
		InvalidTypeToSynchronized,
		new String[] {new String(type.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void invalidUnaryExpression(Expression expression) {
	this.handle(
		InvalidUnaryExpression,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void isClassPathCorrect(char[][] wellKnownTypeName, CompilationUnitDeclaration compUnitDecl) {
	referenceContext = compUnitDecl;
	this.handle(
		IsClassPathCorrect,
		new String[] {CharOperation.toString(wellKnownTypeName)}, 
		AbortCompilation | Error,
		compUnitDecl == null ? 0 : compUnitDecl.sourceStart,
		compUnitDecl == null ? 1 : compUnitDecl.sourceEnd);
}
public void maskedExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		MaskedCatch,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
}
public void methodNeedingAbstractModifier(MethodDeclaration methodDecl) {
	this.handle(
		MethodRequiresBody,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodNeedingNoBody(MethodDeclaration methodDecl) {
	this.handle(
		((methodDecl.modifiers & CompilerModifiers.AccNative) != 0) ? BodyForNativeMethod : BodyForAbstractMethod,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void methodWithConstructorName(MethodDeclaration methodDecl) {
	this.handle(
		MethodButWithConstructorName,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void missingEnclosingInstanceSpecification(ReferenceBinding enclosingType, AstNode location) {
	boolean insideConstructorCall =
		(location instanceof ExplicitConstructorCall)
			&& (((ExplicitConstructorCall) location).accessMode == ExplicitConstructorCall.ImplicitSuper);

	this.handle(
		insideConstructorCall
			? MissingEnclosingInstanceForConstructorCall
			: MissingEnclosingInstance,
		new String[] {new String(enclosingType.readableName())},
		location.sourceStart,
		location.sourceEnd);
}
public void missingReturnType(AbstractMethodDeclaration methodDecl) {
	this.handle(
		MissingReturnType,
		new String[0],
		methodDecl.sourceStart,
		methodDecl.sourceEnd);
}
public void mustDefineDimensionsOrInitializer(ArrayAllocationExpression expression) {
	this.handle(
		MustDefineEitherDimensionExpressionsOrInitializer,
		new String[0],
		expression.sourceStart,
		expression.sourceEnd);
}
public void mustSpecifyPackage(CompilationUnitDeclaration compUnitDecl) {
	this.handle(
		MustSpecifyPackage,
		new String[] {new String(compUnitDecl.getFileName())},
		compUnitDecl.sourceStart,
		compUnitDecl.sourceStart + 1);	
}
public void mustUseAStaticMethod(MessageSend messageSend, MethodBinding method) {
	this.handle(
		StaticMethodRequested,
		new String[] {new String(method.declaringClass.readableName()), new String(method.selector), parametersAsString(method)},
		messageSend.sourceStart,
		messageSend.sourceEnd);
}
public void nativeMethodsCannotBeStrictfp(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		NativeMethodsCannotBeStrictfp,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void needImplementation() {
	this.abortDueToInternalError("Missing code implementation in the compiler");
}
public void needToEmulateFieldReadAccess(FieldBinding field, AstNode location) {
	this.handle(
		NeedToEmulateFieldReadAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateFieldWriteAccess(FieldBinding field, AstNode location) {
	this.handle(
		NeedToEmulateFieldWriteAccess,
		new String[] {new String(field.declaringClass.readableName()), new String(field.name)},
		location.sourceStart,
		location.sourceEnd);
}
public void needToEmulateMethodAccess(
	MethodBinding method, 
	AstNode location) {

	if (method.isConstructor())
		this.handle(
			NeedToEmulateConstructorAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				parametersAsString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
	else
		this.handle(
			NeedToEmulateMethodAccess, 
			new String[] {
				new String(method.declaringClass.readableName()), 
				new String(method.selector), 
				parametersAsString(method)
			 }, 
			location.sourceStart, 
			location.sourceEnd); 
}
public void nestedClassCannotDeclareInterface(TypeDeclaration typeDecl) {
	this.handle(
		CannotDefineInterfaceInLocalType,
		new String[] {new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd());
}
public void noMoreAvailableSpaceForArgument(LocalDeclaration localDeclaration) {
	this.handle(
		TooManyArgumentSlots,
		new String[]{localDeclaration.name()},
		Abort | Error,
		localDeclaration.sourceStart(),
		localDeclaration.sourceEnd());
}
public void noMoreAvailableSpaceForLocal(LocalDeclaration localDeclaration) {
	this.handle(
		TooManyLocalVariableSlots,
		new String[]{localDeclaration.name()},
		Abort | Error,
		localDeclaration.sourceStart(),
		localDeclaration.sourceEnd());
}
public void notCompatibleTypesError(EqualExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IncompatibleTypesInEqualityOperator,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void notCompatibleTypesError(InstanceOfExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IncompatibleTypesInConditionalOperator,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void operatorOnlyValidOnNumericType(CompoundAssignment  assignment, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		TypeMismatch,
		new String[] {new String(leftType.readableName()), new String(rightType.readableName())},
		assignment.sourceStart,
		assignment.sourceEnd);
}
public void overridesDeprecatedMethod(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		OverridingDeprecatedMethod,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void overridesPackageDefaultMethod(MethodBinding localMethod, MethodBinding inheritedMethod) {
	this.handle(
		OverridingNonVisibleMethod,
		new String[] {
			new String(
					CharOperation.concat(
						localMethod.declaringClass.readableName(),
						localMethod.readableName(),
						'.')),
			new String(inheritedMethod.declaringClass.readableName())},
		localMethod.sourceStart(),
		localMethod.sourceEnd());
}
public void packageCollidesWithType(CompilationUnitDeclaration compUnitDecl) {
	this.handle(
		PackageCollidesWithType,
		new String[] {CharOperation.toString(compUnitDecl.currentPackage.tokens)},
		compUnitDecl.currentPackage.sourceStart,
		compUnitDecl.currentPackage.sourceEnd);
}
private String parametersAsString(MethodBinding method) {
	TypeBinding[] params = method.parameters;
	StringBuffer buffer = new StringBuffer();
	for (int i = 0, length = params.length; i < length; i++) {
		if (i != 0)
			buffer.append(", "/*nonNLS*/);
		buffer.append(new String(params[i].readableName()));
	}
	return buffer.toString();
}
public void parseError(
	int startPosition, 
	int endPosition, 
	char[] currentTokenSource, 
	String errorTokenName, 
	String[] possibleTokens,
	ReferenceContext context,
	CompilationResult compilationResult) {
		
	if (possibleTokens.length == 0) { //no suggestion available
		this.handle(
			ParsingErrorNoSuggestion,
			new String[] {errorTokenName},
			// this is the current -invalid- token position
			startPosition,
			endPosition,
			context, 
			compilationResult);
		return;
	}

	//build a list of probable right tokens
	StringBuffer list = new StringBuffer(20);
	for (int i = 0, max = possibleTokens.length; i < max; i++) {
		if (i > 0)
			list.append(", "/*nonNLS*/);
		list.append('"');
		list.append(possibleTokens[i]);
		list.append('"');
	}

	//extract the literal when it's a literal  
	if ((errorTokenName.equals("IntegerLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("LongLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("FloatingPointLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("DoubleLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("StringLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("CharacterLiteral"/*nonNLS*/)) ||
		(errorTokenName.equals("Identifier"/*nonNLS*/))) {
			errorTokenName = new String(currentTokenSource);
	}

	this.handle(
		ParsingError,
		new String[] {errorTokenName, list.toString()},
		// this is the current -invalid- token position
		startPosition,
		endPosition,
		context,
		compilationResult);
}
public void publicClassMustMatchFileName(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		PublicClassMustMatchFileName,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd(),
		compUnitDecl.compilationResult);
}
/*
 * Flag all constructors involved in a cycle, we know we have a cycle.
 */
public void recursiveConstructorInvocation(TypeDeclaration typeDeclaration) {

	// propagate the reference count, negative counts means leading to a super constructor invocation (directly or indirectly)
	boolean hasChanged;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	int max = methods.length;
	do {
		hasChanged = false;
		for(int i = 0; i < max; i++){
			if (methods[i].isConstructor()){
				ConstructorDeclaration constructor = (ConstructorDeclaration) methods[i];
				if (constructor.referenceCount > 0){
					ConstructorDeclaration targetConstructor = (ConstructorDeclaration)(typeDeclaration.declarationOf(constructor.constructorCall.binding));
					if ((targetConstructor == null) || (targetConstructor.referenceCount < 0)){
						hasChanged = true;
						constructor.referenceCount = -1;
					}	
				}
			}
		}
	} while (hasChanged);

	// all remaining constructors with a positive count are still involved in a cycle
	for(int i = 0; i < max; i++){
		if (methods[i].isConstructor()){
			ConstructorDeclaration constructor = (ConstructorDeclaration) methods[i];
			if (constructor.referenceCount > 0){
				this.referenceContext = constructor;
				this.handle(
					RecursiveConstructorInvocation,
					new String[] {
						new String(constructor.constructorCall.binding.declaringClass.readableName()), 
						parametersAsString(constructor.constructorCall.binding)
					},
					constructor.constructorCall.sourceStart,
					constructor.constructorCall.sourceEnd);
			}
		}
	}
}
public void redefineArgument(Argument arg) {
	this.handle(
		RedefinedArgument,
		new String[] {new String(arg.name)},
		arg.sourceStart,
		arg.sourceEnd);
}
public void redefineLocal(LocalDeclaration localDecl) {
	this.handle(
		RedefinedLocal,
		new String[] {new String(localDecl.name)},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void referenceMustBeArrayTypeAt(TypeBinding arrayType, ArrayReference arrayRef) {
	this.handle(
		ArrayReferenceRequired,
		new String[] {new String(arrayType.readableName())},
		arrayRef.sourceStart,
		arrayRef.sourceEnd);
}
public void returnTypeCannotBeVoidArray(SourceTypeBinding type, MethodDeclaration methodDecl) {
	this.handle(
		ReturnTypeCannotBeVoidArray,
		new String[] {new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void returnTypeProblem(SourceTypeBinding type, MethodDeclaration methodDecl, TypeBinding expectedType) {
	int problemId = expectedType.problemId();
	switch (problemId) {
		case NotFound : // 1
		case NotVisible : // 2
		case Ambiguous : // 3
		case InternalNameProvided : // 4
		case InheritedNameHidesEnclosingName : // 5
			this.handle(
				ReturnTypeProblemBase + problemId,
				new String[] {new String(methodDecl.selector), new String(expectedType.readableName())},
				methodDecl.returnType.sourceStart(),
				methodDecl.returnType.sourceEnd());
			break;
		case NoError : // 0
		default :
			needImplementation(); // want to fail to see why we were here...
			break;
	}
}
public void scannerError(Parser parser, String errorTokenName) {
	Scanner scanner = parser.scanner;

	int flag = ParsingErrorNoSuggestion;
	int startPos = scanner.startPosition;

	//special treatment for recognized errors....
	if (errorTokenName.equals(Scanner.END_OF_SOURCE))
		flag = END_OF_SOURCE;
	else
		if (errorTokenName.equals(Scanner.INVALID_HEXA))
			flag = INVALID_HEXA;
		else
			if (errorTokenName.equals(Scanner.INVALID_OCTAL))
				flag = INVALID_OCTAL;
			else
				if (errorTokenName.equals(Scanner.INVALID_CHARACTER_CONSTANT))
					flag = INVALID_CHARACTER_CONSTANT;
				else
					if (errorTokenName.equals(Scanner.INVALID_ESCAPE))
						flag = INVALID_ESCAPE;
					else
						if (errorTokenName.equals(Scanner.INVALID_UNICODE_ESCAPE)){
							flag = INVALID_UNICODE_ESCAPE;
							// better locate the error message
							int checkPos = scanner.currentPosition - 1;
							char[] source = scanner.source;
							while (checkPos >= startPos){
								if (source[checkPos] == '\\') break;
								checkPos --;
							}
							startPos = checkPos;
						} else
							if (errorTokenName.equals(Scanner.INVALID_FLOAT))
								flag = INVALID_FLOAT;
							else
								if (errorTokenName.equals(Scanner.UNTERMINATED_STRING))
									flag = UNTERMINATED_STRING;
								else
									if (errorTokenName.equals(Scanner.UNTERMINATED_COMMENT))
										flag = UNTERMINATED_COMMENT;
									else
										if (errorTokenName.equals(Scanner.INVALID_CHAR_IN_STRING))
											flag = UNTERMINATED_STRING;

	this.handle(
		flag, 
		flag == ParsingErrorNoSuggestion 
			? new String[] {errorTokenName}
			: new String[0],
		// this is the current -invalid- token position
		startPos, 
		scanner.currentPosition - 1,
		parser.compilationUnit.compilationResult);
}
public void shouldReturn(TypeBinding returnType, AstNode location) {
	this.handle(
		ShouldReturnValue,
		new String[] { new String (returnType.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void signalNoImplicitStringConversionForCharArrayExpression(Expression expression) {
	this.handle(
		NoImplicitStringConversionForCharArrayExpression,
		new String[] {},
		expression.sourceStart,
		expression.sourceEnd);
}
public void staticAndInstanceConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	if (currentMethod.isStatic())
		this.handle(
			// This static method cannot hide the instance method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature a static (non-abstract) method cannot hide an instance method.
			CannotHideAnInstanceMethodWithAStaticMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
	else
		this.handle(
			// This instance method cannot override the static method from %1
			// 8.4.6.4 - If a class inherits more than one method with the same signature an instance (non-abstract) method cannot override a static method.
			CannotOverrideAStaticMethodWithAnInstanceMethod,
			new String[] {new String(inheritedMethod.declaringClass.readableName())},
			currentMethod.sourceStart(),
			currentMethod.sourceEnd());
}
public void staticFieldAccessToNonStaticVariable(FieldReference fieldRef, FieldBinding field) {
	this.handle(
		NonStaticFieldFromStaticInvocation,
		new String[] {new String(field.readableName())},
		fieldRef.sourceStart,
		fieldRef.sourceEnd); 
}
public void staticFieldAccessToNonStaticVariable(QualifiedNameReference nameRef, FieldBinding field){
	int fieldIndex = nameRef.indexOfFirstFieldBinding - 1;
	this.handle(
		NonStaticFieldFromStaticInvocation,
		new String[] { new String(field.readableName())},
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticFieldAccessToNonStaticVariable(SingleNameReference nameRef, FieldBinding field) {
	this.handle(
		NonStaticFieldFromStaticInvocation,
		new String[] {new String(field.readableName())},
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void staticInheritedMethodConflicts(SourceTypeBinding type, MethodBinding concreteMethod, MethodBinding[] abstractMethods) {
	this.handle(
		// The static method %1 conflicts with the abstract method in %2
		// 8.4.6.4 - If a class inherits more than one method with the same signature it is an error for one to be static (non-abstract) and the other abstract.
		StaticInheritedMethodConflicts,
		new String[] {
			new String(concreteMethod.readableName()),
			new String(abstractMethods[0].declaringClass.readableName())},
		type.sourceStart(),
		type.sourceEnd());
}
public void stringConstantIsExceedingUtf8Limit(AstNode location) {
	this.handle(
		StringConstantIsExceedingUtf8Limit,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
}
public void superclassMustBeAClass(SourceTypeBinding type, TypeReference superclassRef, ReferenceBinding superType) {
	this.handle(
		SuperclassMustBeAClass,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		superclassRef.sourceStart(),
		superclassRef.sourceEnd());
}
public void superinterfaceMustBeAnInterface(SourceTypeBinding type, TypeDeclaration typeDecl, ReferenceBinding superType) {
	this.handle(
		SuperInterfaceMustBeAnInterface,
		new String[] {new String(superType.readableName()), new String(type.sourceName())},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd());
}
public void typeCastError(CastExpression expression, TypeBinding leftType, TypeBinding rightType) {
	this.handle(
		IllegalCast,
		new String[] {new String(rightType.readableName()), new String(leftType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void typeCollidesWithPackage(CompilationUnitDeclaration compUnitDecl, TypeDeclaration typeDecl) {
	this.referenceContext = typeDecl; // report the problem against the type not the entire compilation unit
	this.handle(
		TypeCollidesWithPackage,
		new String[] {new String(compUnitDecl.getFileName()), new String(typeDecl.name)},
		typeDecl.sourceStart(),
		typeDecl.sourceEnd(),
		compUnitDecl.compilationResult);
}
public void typeMismatchError(TypeBinding resultType, TypeBinding expectedType, AstNode location) {
	this.handle(
		TypeMismatch,
		new String[] {new String(resultType.readableName()), new String(expectedType.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void typeMismatchErrorActualTypeExpectedType(Expression expression, TypeBinding constantType, TypeBinding expectedType) {
	this.handle(
		TypeMismatch,
		new String[] {new String(constantType.readableName()), new String(expectedType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void undefinedLabel(BranchStatement statement) {
	this.handle(
		UndefinedLabel,
		new String[] {new String(statement.label)},
		statement.sourceStart(),
		statement.sourceEnd());
}
public void unexpectedStaticModifierForField(SourceTypeBinding type, FieldDeclaration fieldDecl) {
	this.handle(
		UnexpectedStaticModifierForField,
		new String[] {fieldDecl.name()},
		fieldDecl.sourceStart(),
		fieldDecl.sourceEnd());
}
public void unexpectedStaticModifierForMethod(ReferenceBinding type, AbstractMethodDeclaration methodDecl) {
	this.handle(
		UnexpectedStaticModifierForMethod,
		new String[] {new String(type.sourceName()), new String(methodDecl.selector)},
		methodDecl.sourceStart(),
		methodDecl.sourceEnd());
}
public void unhandledException(TypeBinding exceptionType, AstNode location, Scope scope) {
	this.handle(
		scope.methodScope().isInsideInitializer()
			? CannotThrowCheckedExceptionInInitializer
			: (location instanceof AnonymousLocalTypeDeclaration
				? NoExceptionInAnonymousTypeConstructor
				: UnhandledException),
		new String[] {new String(exceptionType.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void uninitializedBlankFinalField(FieldBinding binding, AstNode location) {
	this.handle(
		UninitializedBlankFinalField,
		new String[] {new String(binding.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void uninitializedLocalVariable(LocalVariableBinding binding, AstNode location) {
	this.handle(
		UninitializedLocalVariable,
		new String[] {new String(binding.readableName())},
		location.sourceStart(),
		location.sourceEnd());
}
public void unmatchedBracket(int position, ReferenceContext context, CompilationResult compilationResult) {

	this.handle(
		UnmatchedBracket, 
		new String[] {},
		position, 
		position,
		context,
		compilationResult);
}
public void unnecessaryEnclosingInstanceSpecification(Expression expression, ReferenceBinding targetType) {
	this.handle(
		IllegalEnclosingInstanceSpecification,
		new String[]{ new String(targetType.readableName())},
		expression.sourceStart,
		expression.sourceEnd);
}
public void unreachableCode(Statement statement) {
	this.handle(
		CodeCannotBeReached,
		new String[0],
		statement.sourceStart(),
		statement.sourceEnd());
}
public void unreachableExceptionHandler(ReferenceBinding exceptionType, AstNode location) {
	this.handle(
		UnreachableCatch,
		new String[0],
		location.sourceStart(),
		location.sourceEnd());
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
	this.handle(
		UndefinedName,
		new String[] {new String(binding.readableName())},
		severity,
		nameRef.sourceStart,
		nameRef.sourceEnd);
}
public void unusedArgument(LocalDeclaration localDecl) {
	this.handle(
		ArgumentIsNeverUsed,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void unusedLocalVariable(LocalDeclaration localDecl) {
	this.handle(
		LocalVariableIsNeverUsed,
		new String[] {localDecl.name()},
		localDecl.sourceStart,
		localDecl.sourceEnd);
}
public void variableTypeCannotBeVoid(AbstractVariableDeclaration varDecl) {
	this.handle(
		VariableTypeCannotBeVoid,
		new String[] {new String(varDecl.name)},
		varDecl.sourceStart(),
		varDecl.sourceEnd());
}
public void variableTypeCannotBeVoidArray(AbstractVariableDeclaration varDecl) {
	this.handle(
		VariableTypeCannotBeVoidArray,
		new String[] {new String(varDecl.name)},
		varDecl.sourceStart(),
		varDecl.sourceEnd());
}
public void visibilityConflict(MethodBinding currentMethod, MethodBinding inheritedMethod) {
	this.handle(
		//	Cannot reduce the visibility of the inherited method from %1
		// 8.4.6.3 - The access modifier of an hiding method must provide at least as much access as the hidden method.
		// 8.4.6.3 - The access modifier of an overiding method must provide at least as much access as the overriden method.
		MethodReducesVisibility,
		new String[] {new String(inheritedMethod.declaringClass.readableName())},
		currentMethod.sourceStart(),
		currentMethod.sourceEnd());
}
public void wrongSequenceOfExceptionTypesError(TryStatement statement, int under, int upper) {
	//the two catch block under and upper are in an incorrect order.
	//under should be define BEFORE upper in the source

	//notice that the compiler could arrange automatically the
	//correct order - and the only error would be on cycle ....
	//on this one again , java is compiler-driven instead of being
	//user-driven .....

	TypeReference typeRef = statement.catchArguments[under].type;
	this.handle(
		UnreachableCatch,
		new String[0],
		typeRef.sourceStart,
		typeRef.sourceEnd);
}

public void nonExternalizedStringLiteral(AstNode location) {
	this.handle(
		NonExternalizedStringLiteral,
		new String[] {},
		location.sourceStart,
		location.sourceEnd);
}
}

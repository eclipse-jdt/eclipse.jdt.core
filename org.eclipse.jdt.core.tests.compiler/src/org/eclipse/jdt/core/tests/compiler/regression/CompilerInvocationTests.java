/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;

import junit.framework.Test;

/**
 * This class is meant to gather test cases related to the invocation of the
 * compiler, be it at an API or non API level.
 */
public class CompilerInvocationTests extends AbstractRegressionTest {

public CompilerInvocationTests(String name) {
    super(name);
}

	// Static initializer to specify tests subset using TESTS_* static variables
  	// All specified tests which does not belong to the class are skipped...
  	// Only the highest compliance level is run; add the VM argument
  	// -Dcompliance=1.4 (for example) to lower it if needed
  	static {
//    	TESTS_NAMES = new String[] { "test001" };
//    	TESTS_NUMBERS = new int[] { 1 };   
//    	TESTS_RANGE = new int[] { 1, -1 }; 
//  	TESTS_RANGE = new int[] { 1, 2049 }; 
//  	TESTS_RANGE = new int[] { 449, 451 }; 
//    	TESTS_RANGE = new int[] { 900, 999 }; 
  	}

public static Test suite() {
    return buildTestSuite(testClass());
}
  
public static Class testClass() {
    return CompilerInvocationTests.class;
}

// irritant vs warning token
public void test001_irritant_warning_token() {
	String [] tokens = new String[64];
	Map matcher = new HashMap();
	long irritant;
	String token;
	for (int i = 0; i < 64; i++) {
		if ((token = tokens[i] = CompilerOptions.warningTokenFromIrritant(irritant = 1L << i)) != null) {
			matcher.put(token, token);
			assertTrue((irritant & CompilerOptions.warningTokenToIrritant(token)) != 0);
		}
	}
	String [] allTokens = CompilerOptions.warningTokens;
	int length = allTokens.length;
	matcher.put("all", "all"); // all gets undetected in the From/To loop
	assertEquals(allTokens.length, matcher.size());
	for (int i = 0; i < length; i++) {
		assertNotNull(matcher.get(allTokens[i]));
	}
}

// problem categories - check that none is left unspecified
public void test002_problem_categories() {
	try {
		Class iProblemClass;
		Map excludedProblems = new HashMap();
		// categories
		excludedProblems.put("ObjectHasNoSuperclass", null);
		excludedProblems.put("UndefinedType", null);
		excludedProblems.put("NotVisibleType", null);
		excludedProblems.put("AmbiguousType", null);
		excludedProblems.put("UsingDeprecatedType", null);
		excludedProblems.put("InternalTypeNameProvided", null);
		excludedProblems.put("UnusedPrivateType", null);
		excludedProblems.put("IncompatibleTypesInEqualityOperator", null);
		excludedProblems.put("IncompatibleTypesInConditionalOperator", null);
		excludedProblems.put("TypeMismatch", null);
		excludedProblems.put("IndirectAccessToStaticType", null);
		excludedProblems.put("MissingEnclosingInstanceForConstructorCall", null);
		excludedProblems.put("MissingEnclosingInstance", null);
		excludedProblems.put("IncorrectEnclosingInstanceReference", null);
		excludedProblems.put("IllegalEnclosingInstanceSpecification", null);
		excludedProblems.put("CannotDefineStaticInitializerInLocalType", null);
		excludedProblems.put("OuterLocalMustBeFinal", null);
		excludedProblems.put("CannotDefineInterfaceInLocalType", null);
		excludedProblems.put("IllegalPrimitiveOrArrayTypeForEnclosingInstance", null);
		excludedProblems.put("EnclosingInstanceInConstructorCall", null);
		excludedProblems.put("AnonymousClassCannotExtendFinalClass", null);
		excludedProblems.put("CannotDefineAnnotationInLocalType", null);
		excludedProblems.put("CannotDefineEnumInLocalType", null);
		excludedProblems.put("NonStaticContextForEnumMemberType", null);
		excludedProblems.put("UndefinedName", null);
		excludedProblems.put("UninitializedLocalVariable", null);
		excludedProblems.put("VariableTypeCannotBeVoid", null);
		excludedProblems.put("VariableTypeCannotBeVoidArray", null);
		excludedProblems.put("CannotAllocateVoidArray", null);
		excludedProblems.put("RedefinedLocal", null);
		excludedProblems.put("RedefinedArgument", null);
		excludedProblems.put("DuplicateFinalLocalInitialization", null);
		excludedProblems.put("NonBlankFinalLocalAssignment", null);
		excludedProblems.put("ParameterAssignment", null);
		excludedProblems.put("FinalOuterLocalAssignment", null);
		excludedProblems.put("LocalVariableIsNeverUsed", null);
		excludedProblems.put("ArgumentIsNeverUsed", null);
		excludedProblems.put("BytecodeExceeds64KLimit", null);
		excludedProblems.put("BytecodeExceeds64KLimitForClinit", null);
		excludedProblems.put("TooManyArgumentSlots", null);
		excludedProblems.put("TooManyLocalVariableSlots", null);
		excludedProblems.put("TooManySyntheticArgumentSlots", null);
		excludedProblems.put("TooManyArrayDimensions", null);
		excludedProblems.put("BytecodeExceeds64KLimitForConstructor", null);
		excludedProblems.put("UndefinedField", null);
		excludedProblems.put("NotVisibleField", null);
		excludedProblems.put("AmbiguousField", null);
		excludedProblems.put("UsingDeprecatedField", null);
		excludedProblems.put("NonStaticFieldFromStaticInvocation", null);
		excludedProblems.put("ReferenceToForwardField", null);
		excludedProblems.put("NonStaticAccessToStaticField", null);
		excludedProblems.put("UnusedPrivateField", null);
		excludedProblems.put("IndirectAccessToStaticField", null);
		excludedProblems.put("UnqualifiedFieldAccess", null);
		excludedProblems.put("FinalFieldAssignment", null);
		excludedProblems.put("UninitializedBlankFinalField", null);
		excludedProblems.put("DuplicateBlankFinalFieldInitialization", null);
		excludedProblems.put("LocalVariableHidingLocalVariable", null);
		excludedProblems.put("LocalVariableHidingField", null);
		excludedProblems.put("FieldHidingLocalVariable", null);
		excludedProblems.put("FieldHidingField", null);
		excludedProblems.put("ArgumentHidingLocalVariable", null);
		excludedProblems.put("ArgumentHidingField", null);
		excludedProblems.put("MissingSerialVersion", null);
		excludedProblems.put("UndefinedMethod", null);
		excludedProblems.put("NotVisibleMethod", null);
		excludedProblems.put("AmbiguousMethod", null);
		excludedProblems.put("UsingDeprecatedMethod", null);
		excludedProblems.put("DirectInvocationOfAbstractMethod", null);
		excludedProblems.put("VoidMethodReturnsValue", null);
		excludedProblems.put("MethodReturnsVoid", null);
		excludedProblems.put("MethodRequiresBody", null);
		excludedProblems.put("ShouldReturnValue", null);
		excludedProblems.put("MethodButWithConstructorName", null);
		excludedProblems.put("MissingReturnType", null);
		excludedProblems.put("BodyForNativeMethod", null);
		excludedProblems.put("BodyForAbstractMethod", null);
		excludedProblems.put("NoMessageSendOnBaseType", null);
		excludedProblems.put("ParameterMismatch", null);
		excludedProblems.put("NoMessageSendOnArrayType", null);
		excludedProblems.put("NonStaticAccessToStaticMethod", null);
		excludedProblems.put("UnusedPrivateMethod", null);
		excludedProblems.put("IndirectAccessToStaticMethod", null);
		excludedProblems.put("UndefinedConstructor", null);
		excludedProblems.put("NotVisibleConstructor", null);
		excludedProblems.put("AmbiguousConstructor", null);
		excludedProblems.put("UsingDeprecatedConstructor", null);
		excludedProblems.put("UnusedPrivateConstructor", null);
		excludedProblems.put("InstanceFieldDuringConstructorInvocation", null);
		excludedProblems.put("InstanceMethodDuringConstructorInvocation", null);
		excludedProblems.put("RecursiveConstructorInvocation", null);
		excludedProblems.put("ThisSuperDuringConstructorInvocation", null);
		excludedProblems.put("InvalidExplicitConstructorCall", null);
		excludedProblems.put("UndefinedConstructorInDefaultConstructor", null);
		excludedProblems.put("NotVisibleConstructorInDefaultConstructor", null);
		excludedProblems.put("AmbiguousConstructorInDefaultConstructor", null);
		excludedProblems.put("UndefinedConstructorInImplicitConstructorCall", null);
		excludedProblems.put("NotVisibleConstructorInImplicitConstructorCall", null);
		excludedProblems.put("AmbiguousConstructorInImplicitConstructorCall", null);
		excludedProblems.put("UnhandledExceptionInDefaultConstructor", null);
		excludedProblems.put("UnhandledExceptionInImplicitConstructorCall", null);
		excludedProblems.put("ArrayReferenceRequired", null);
		excludedProblems.put("NoImplicitStringConversionForCharArrayExpression", null);
		excludedProblems.put("StringConstantIsExceedingUtf8Limit", null);
		excludedProblems.put("NonConstantExpression", null);
		excludedProblems.put("NumericValueOutOfRange", null);
		excludedProblems.put("IllegalCast", null);
		excludedProblems.put("InvalidClassInstantiation", null);
		excludedProblems.put("CannotDefineDimensionExpressionsWithInit", null);
		excludedProblems.put("MustDefineEitherDimensionExpressionsOrInitializer", null);
		excludedProblems.put("InvalidOperator", null);
		excludedProblems.put("CodeCannotBeReached", null);
		excludedProblems.put("CannotReturnInInitializer", null);
		excludedProblems.put("InitializerMustCompleteNormally", null);
		excludedProblems.put("InvalidVoidExpression", null);
		excludedProblems.put("MaskedCatch", null);
		excludedProblems.put("DuplicateDefaultCase", null);
		excludedProblems.put("UnreachableCatch", null);
		excludedProblems.put("UnhandledException", null);
		excludedProblems.put("IncorrectSwitchType", null);
		excludedProblems.put("DuplicateCase", null);
		excludedProblems.put("DuplicateLabel", null);
		excludedProblems.put("InvalidBreak", null);
		excludedProblems.put("InvalidContinue", null);
		excludedProblems.put("UndefinedLabel", null);
		excludedProblems.put("InvalidTypeToSynchronized", null);
		excludedProblems.put("InvalidNullToSynchronized", null);
		excludedProblems.put("CannotThrowNull", null);
		excludedProblems.put("AssignmentHasNoEffect", null);
		excludedProblems.put("PossibleAccidentalBooleanAssignment", null);
		excludedProblems.put("SuperfluousSemicolon", null);
		excludedProblems.put("UnnecessaryCast", null);
		excludedProblems.put("UnnecessaryArgumentCast", null);
		excludedProblems.put("UnnecessaryInstanceof", null);
		excludedProblems.put("FinallyMustCompleteNormally", null);
		excludedProblems.put("UnusedMethodDeclaredThrownException", null);
		excludedProblems.put("UnusedConstructorDeclaredThrownException", null);
		excludedProblems.put("InvalidCatchBlockSequence", null);
		excludedProblems.put("EmptyControlFlowStatement", null);
		excludedProblems.put("UnnecessaryElse", null);
		excludedProblems.put("NeedToEmulateFieldReadAccess", null);
		excludedProblems.put("NeedToEmulateFieldWriteAccess", null);
		excludedProblems.put("NeedToEmulateMethodAccess", null);
		excludedProblems.put("NeedToEmulateConstructorAccess", null);
		excludedProblems.put("FallthroughCase", null);
		excludedProblems.put("InheritedMethodHidesEnclosingName", null);
		excludedProblems.put("InheritedFieldHidesEnclosingName", null);
		excludedProblems.put("InheritedTypeHidesEnclosingName", null);
		excludedProblems.put("IllegalUsageOfQualifiedTypeReference", null);
		excludedProblems.put("UnusedLabel", null);
		excludedProblems.put("ThisInStaticContext", null);
		excludedProblems.put("StaticMethodRequested", null);
		excludedProblems.put("IllegalDimension", null);
		excludedProblems.put("InvalidTypeExpression", null);
		excludedProblems.put("ParsingError", null);
		excludedProblems.put("ParsingErrorNoSuggestion", null);
		excludedProblems.put("InvalidUnaryExpression", null);
		excludedProblems.put("InterfaceCannotHaveConstructors", null);
		excludedProblems.put("ArrayConstantsOnlyInArrayInitializers", null);
		excludedProblems.put("ParsingErrorOnKeyword", null);
		excludedProblems.put("ParsingErrorOnKeywordNoSuggestion", null);
		excludedProblems.put("UnmatchedBracket", null);
		excludedProblems.put("NoFieldOnBaseType", null);
		excludedProblems.put("InvalidExpressionAsStatement", null);
		excludedProblems.put("ExpressionShouldBeAVariable", null);
		excludedProblems.put("MissingSemiColon", null);
		excludedProblems.put("InvalidParenthesizedExpression", null);
		excludedProblems.put("ParsingErrorInsertTokenBefore", null);
		excludedProblems.put("ParsingErrorInsertTokenAfter", null);
		excludedProblems.put("ParsingErrorDeleteToken", null);
		excludedProblems.put("ParsingErrorDeleteTokens", null);
		excludedProblems.put("ParsingErrorMergeTokens", null);
		excludedProblems.put("ParsingErrorInvalidToken", null);
		excludedProblems.put("ParsingErrorMisplacedConstruct", null);
		excludedProblems.put("ParsingErrorReplaceTokens", null);
		excludedProblems.put("ParsingErrorNoSuggestionForTokens", null);
		excludedProblems.put("ParsingErrorUnexpectedEOF", null);
		excludedProblems.put("ParsingErrorInsertToComplete", null);
		excludedProblems.put("ParsingErrorInsertToCompleteScope", null);
		excludedProblems.put("ParsingErrorInsertToCompletePhrase", null);
		excludedProblems.put("EndOfSource", null);
		excludedProblems.put("InvalidHexa", null);
		excludedProblems.put("InvalidOctal", null);
		excludedProblems.put("InvalidCharacterConstant", null);
		excludedProblems.put("InvalidEscape", null);
		excludedProblems.put("InvalidInput", null);
		excludedProblems.put("InvalidUnicodeEscape", null);
		excludedProblems.put("InvalidFloat", null);
		excludedProblems.put("NullSourceString", null);
		excludedProblems.put("UnterminatedString", null);
		excludedProblems.put("UnterminatedComment", null);
		excludedProblems.put("NonExternalizedStringLiteral", null);
		excludedProblems.put("InvalidDigit", null);
		excludedProblems.put("InvalidLowSurrogate", null);
		excludedProblems.put("InvalidHighSurrogate", null);
		excludedProblems.put("UnnecessaryNLSTag", null);
		excludedProblems.put("DiscouragedReference", null);
		excludedProblems.put("InterfaceCannotHaveInitializers", null);
		excludedProblems.put("DuplicateModifierForType", null);
		excludedProblems.put("IllegalModifierForClass", null);
		excludedProblems.put("IllegalModifierForInterface", null);
		excludedProblems.put("IllegalModifierForMemberClass", null);
		excludedProblems.put("IllegalModifierForMemberInterface", null);
		excludedProblems.put("IllegalModifierForLocalClass", null);
		excludedProblems.put("ForbiddenReference", null);
		excludedProblems.put("IllegalModifierCombinationFinalAbstractForClass", null);
		excludedProblems.put("IllegalVisibilityModifierForInterfaceMemberType", null);
		excludedProblems.put("IllegalVisibilityModifierCombinationForMemberType", null);
		excludedProblems.put("IllegalStaticModifierForMemberType", null);
		excludedProblems.put("SuperclassMustBeAClass", null);
		excludedProblems.put("ClassExtendFinalClass", null);
		excludedProblems.put("DuplicateSuperInterface", null);
		excludedProblems.put("SuperInterfaceMustBeAnInterface", null);
		excludedProblems.put("HierarchyCircularitySelfReference", null);
		excludedProblems.put("HierarchyCircularity", null);
		excludedProblems.put("HidingEnclosingType", null);
		excludedProblems.put("DuplicateNestedType", null);
		excludedProblems.put("CannotThrowType", null);
		excludedProblems.put("PackageCollidesWithType", null);
		excludedProblems.put("TypeCollidesWithPackage", null);
		excludedProblems.put("DuplicateTypes", null);
		excludedProblems.put("IsClassPathCorrect", null);
		excludedProblems.put("PublicClassMustMatchFileName", null);
		excludedProblems.put("MustSpecifyPackage", null);
		excludedProblems.put("HierarchyHasProblems", null);
		excludedProblems.put("PackageIsNotExpectedPackage", null);
		excludedProblems.put("ObjectCannotHaveSuperTypes", null);
		excludedProblems.put("ObjectMustBeClass", null);
		excludedProblems.put("SuperclassNotFound", null);
		excludedProblems.put("SuperclassNotVisible", null);
		excludedProblems.put("SuperclassAmbiguous", null);
		excludedProblems.put("SuperclassInternalNameProvided", null);
		excludedProblems.put("SuperclassInheritedNameHidesEnclosingName", null);
		excludedProblems.put("InterfaceNotFound", null);
		excludedProblems.put("InterfaceNotVisible", null);
		excludedProblems.put("InterfaceAmbiguous", null);
		excludedProblems.put("InterfaceInternalNameProvided", null);
		excludedProblems.put("InterfaceInheritedNameHidesEnclosingName", null);
		excludedProblems.put("DuplicateField", null);
		excludedProblems.put("DuplicateModifierForField", null);
		excludedProblems.put("IllegalModifierForField", null);
		excludedProblems.put("IllegalModifierForInterfaceField", null);
		excludedProblems.put("IllegalVisibilityModifierCombinationForField", null);
		excludedProblems.put("IllegalModifierCombinationFinalVolatileForField", null);
		excludedProblems.put("UnexpectedStaticModifierForField", null);
		excludedProblems.put("FieldTypeNotFound", null);
		excludedProblems.put("FieldTypeNotVisible", null);
		excludedProblems.put("FieldTypeAmbiguous", null);
		excludedProblems.put("FieldTypeInternalNameProvided", null);
		excludedProblems.put("FieldTypeInheritedNameHidesEnclosingName", null);
		excludedProblems.put("DuplicateMethod", null);
		excludedProblems.put("IllegalModifierForArgument", null);
		excludedProblems.put("DuplicateModifierForMethod", null);
		excludedProblems.put("IllegalModifierForMethod", null);
		excludedProblems.put("IllegalModifierForInterfaceMethod", null);
		excludedProblems.put("IllegalVisibilityModifierCombinationForMethod", null);
		excludedProblems.put("UnexpectedStaticModifierForMethod", null);
		excludedProblems.put("IllegalAbstractModifierCombinationForMethod", null);
		excludedProblems.put("AbstractMethodInAbstractClass", null);
		excludedProblems.put("ArgumentTypeCannotBeVoid", null);
		excludedProblems.put("ArgumentTypeCannotBeVoidArray", null);
		excludedProblems.put("ReturnTypeCannotBeVoidArray", null);
		excludedProblems.put("NativeMethodsCannotBeStrictfp", null);
		excludedProblems.put("DuplicateModifierForArgument", null);
		excludedProblems.put("ArgumentTypeNotFound", null);
		excludedProblems.put("ArgumentTypeNotVisible", null);
		excludedProblems.put("ArgumentTypeAmbiguous", null);
		excludedProblems.put("ArgumentTypeInternalNameProvided", null);
		excludedProblems.put("ArgumentTypeInheritedNameHidesEnclosingName", null);
		excludedProblems.put("ExceptionTypeNotFound", null);
		excludedProblems.put("ExceptionTypeNotVisible", null);
		excludedProblems.put("ExceptionTypeAmbiguous", null);
		excludedProblems.put("ExceptionTypeInternalNameProvided", null);
		excludedProblems.put("ExceptionTypeInheritedNameHidesEnclosingName", null);
		excludedProblems.put("ReturnTypeNotFound", null);
		excludedProblems.put("ReturnTypeNotVisible", null);
		excludedProblems.put("ReturnTypeAmbiguous", null);
		excludedProblems.put("ReturnTypeInternalNameProvided", null);
		excludedProblems.put("ReturnTypeInheritedNameHidesEnclosingName", null);
		excludedProblems.put("ConflictingImport", null);
		excludedProblems.put("DuplicateImport", null);
		excludedProblems.put("CannotImportPackage", null);
		excludedProblems.put("UnusedImport", null);
		excludedProblems.put("ImportNotFound", null);
		excludedProblems.put("ImportNotVisible", null);
		excludedProblems.put("ImportAmbiguous", null);
		excludedProblems.put("ImportInternalNameProvided", null);
		excludedProblems.put("ImportInheritedNameHidesEnclosingName", null);
		excludedProblems.put("InvalidTypeForStaticImport", null);
		excludedProblems.put("DuplicateModifierForVariable", null);
		excludedProblems.put("IllegalModifierForVariable", null);
		excludedProblems.put("LocalVariableCannotBeNull", null);
		excludedProblems.put("LocalVariableCanOnlyBeNull", null);
		excludedProblems.put("LocalVariableMayBeNull", null);
		excludedProblems.put("AbstractMethodMustBeImplemented", null);
		excludedProblems.put("FinalMethodCannotBeOverridden", null);
		excludedProblems.put("IncompatibleExceptionInThrowsClause", null);
		excludedProblems.put("IncompatibleExceptionInInheritedMethodThrowsClause", null);
		excludedProblems.put("IncompatibleReturnType", null);
		excludedProblems.put("InheritedMethodReducesVisibility", null);
		excludedProblems.put("CannotOverrideAStaticMethodWithAnInstanceMethod", null);
		excludedProblems.put("CannotHideAnInstanceMethodWithAStaticMethod", null);
		excludedProblems.put("StaticInheritedMethodConflicts", null);
		excludedProblems.put("MethodReducesVisibility", null);
		excludedProblems.put("OverridingNonVisibleMethod", null);
		excludedProblems.put("AbstractMethodCannotBeOverridden", null);
		excludedProblems.put("OverridingDeprecatedMethod", null);
		excludedProblems.put("IncompatibleReturnTypeForNonInheritedInterfaceMethod", null);
		excludedProblems.put("IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod", null);
		excludedProblems.put("IllegalVararg", null);
		excludedProblems.put("CodeSnippetMissingClass", null);
		excludedProblems.put("CodeSnippetMissingMethod", null);
		excludedProblems.put("CannotUseSuperInCodeSnippet", null);
		excludedProblems.put("TooManyConstantsInConstantPool", null);
		excludedProblems.put("TooManyBytesForStringConstant", null);
		excludedProblems.put("TooManyFields", null);
		excludedProblems.put("TooManyMethods", null);
		excludedProblems.put("UseAssertAsAnIdentifier", null);
		excludedProblems.put("UseEnumAsAnIdentifier", null);
		excludedProblems.put("EnumConstantsCannotBeSurroundedByParenthesis", null);
		excludedProblems.put("Task", null);
		excludedProblems.put("UndocumentedEmptyBlock", null);
		excludedProblems.put("JavadocMissingIdentifier", null);
		excludedProblems.put("JavadocNonStaticTypeFromStaticInvocation", null);
		excludedProblems.put("JavadocInvalidParamTagTypeParameter", null);
		excludedProblems.put("JavadocUnexpectedTag", null);
		excludedProblems.put("JavadocMissingParamTag", null);
		excludedProblems.put("JavadocMissingParamName", null);
		excludedProblems.put("JavadocDuplicateParamName", null);
		excludedProblems.put("JavadocInvalidParamName", null);
		excludedProblems.put("JavadocMissingReturnTag", null);
		excludedProblems.put("JavadocDuplicateReturnTag", null);
		excludedProblems.put("JavadocMissingThrowsTag", null);
		excludedProblems.put("JavadocMissingThrowsClassName", null);
		excludedProblems.put("JavadocInvalidThrowsClass", null);
		excludedProblems.put("JavadocDuplicateThrowsClassName", null);
		excludedProblems.put("JavadocInvalidThrowsClassName", null);
		excludedProblems.put("JavadocMissingSeeReference", null);
		excludedProblems.put("JavadocInvalidSeeReference", null);
		excludedProblems.put("JavadocInvalidSeeHref", null);
		excludedProblems.put("JavadocInvalidSeeArgs", null);
		excludedProblems.put("JavadocMissing", null);
		excludedProblems.put("JavadocInvalidTag", null);
		excludedProblems.put("JavadocUndefinedField", null);
		excludedProblems.put("JavadocNotVisibleField", null);
		excludedProblems.put("JavadocAmbiguousField", null);
		excludedProblems.put("JavadocUsingDeprecatedField", null);
		excludedProblems.put("JavadocUndefinedConstructor", null);
		excludedProblems.put("JavadocNotVisibleConstructor", null);
		excludedProblems.put("JavadocAmbiguousConstructor", null);
		excludedProblems.put("JavadocUsingDeprecatedConstructor", null);
		excludedProblems.put("JavadocUndefinedMethod", null);
		excludedProblems.put("JavadocNotVisibleMethod", null);
		excludedProblems.put("JavadocAmbiguousMethod", null);
		excludedProblems.put("JavadocUsingDeprecatedMethod", null);
		excludedProblems.put("JavadocNoMessageSendOnBaseType", null);
		excludedProblems.put("JavadocParameterMismatch", null);
		excludedProblems.put("JavadocNoMessageSendOnArrayType", null);
		excludedProblems.put("JavadocUndefinedType", null);
		excludedProblems.put("JavadocNotVisibleType", null);
		excludedProblems.put("JavadocAmbiguousType", null);
		excludedProblems.put("JavadocUsingDeprecatedType", null);
		excludedProblems.put("JavadocInternalTypeNameProvided", null);
		excludedProblems.put("JavadocInheritedMethodHidesEnclosingName", null);
		excludedProblems.put("JavadocInheritedFieldHidesEnclosingName", null);
		excludedProblems.put("JavadocInheritedNameHidesEnclosingTypeName", null);
		excludedProblems.put("JavadocAmbiguousMethodReference", null);
		excludedProblems.put("JavadocUnterminatedInlineTag", null);
		excludedProblems.put("JavadocMalformedSeeReference", null);
		excludedProblems.put("JavadocMessagePrefix", null);
		excludedProblems.put("JavadocMissingHashCharacter", null);
		excludedProblems.put("JavadocEmptyReturnTag", null);
		excludedProblems.put("JavadocInvalidValueReference", null);
		excludedProblems.put("JavadocUnexpectedText", null);
		excludedProblems.put("JavadocInvalidParamTagName", null);
		excludedProblems.put("DuplicateTypeVariable", null);
		excludedProblems.put("IllegalTypeVariableSuperReference", null);
		excludedProblems.put("NonStaticTypeFromStaticInvocation", null);
		excludedProblems.put("ObjectCannotBeGeneric", null);
		excludedProblems.put("NonGenericType", null);
		excludedProblems.put("IncorrectArityForParameterizedType", null);
		excludedProblems.put("TypeArgumentMismatch", null);
		excludedProblems.put("DuplicateMethodErasure", null);
		excludedProblems.put("ReferenceToForwardTypeVariable", null);
		excludedProblems.put("BoundMustBeAnInterface", null);
		excludedProblems.put("UnsafeRawConstructorInvocation", null);
		excludedProblems.put("UnsafeRawMethodInvocation", null);
		excludedProblems.put("UnsafeTypeConversion", null);
		excludedProblems.put("InvalidTypeVariableExceptionType", null);
		excludedProblems.put("InvalidParameterizedExceptionType", null);
		excludedProblems.put("IllegalGenericArray", null);
		excludedProblems.put("UnsafeRawFieldAssignment", null);
		excludedProblems.put("FinalBoundForTypeVariable", null);
		excludedProblems.put("UndefinedTypeVariable", null);
		excludedProblems.put("SuperInterfacesCollide", null);
		excludedProblems.put("WildcardConstructorInvocation", null);
		excludedProblems.put("WildcardMethodInvocation", null);
		excludedProblems.put("WildcardFieldAssignment", null);
		excludedProblems.put("GenericMethodTypeArgumentMismatch", null);
		excludedProblems.put("GenericConstructorTypeArgumentMismatch", null);
		excludedProblems.put("UnsafeGenericCast", null);
		excludedProblems.put("IllegalInstanceofParameterizedType", null);
		excludedProblems.put("IllegalInstanceofTypeParameter", null);
		excludedProblems.put("NonGenericMethod", null);
		excludedProblems.put("IncorrectArityForParameterizedMethod", null);
		excludedProblems.put("ParameterizedMethodArgumentTypeMismatch", null);
		excludedProblems.put("NonGenericConstructor", null);
		excludedProblems.put("IncorrectArityForParameterizedConstructor", null);
		excludedProblems.put("ParameterizedConstructorArgumentTypeMismatch", null);
		excludedProblems.put("TypeArgumentsForRawGenericMethod", null);
		excludedProblems.put("TypeArgumentsForRawGenericConstructor", null);
		excludedProblems.put("SuperTypeUsingWildcard", null);
		excludedProblems.put("GenericTypeCannotExtendThrowable", null);
		excludedProblems.put("IllegalClassLiteralForTypeVariable", null);
		excludedProblems.put("UnsafeReturnTypeOverride", null);
		excludedProblems.put("MethodNameClash", null);
		excludedProblems.put("RawMemberTypeCannotBeParameterized", null);
		excludedProblems.put("MissingArgumentsForParameterizedMemberType", null);
		excludedProblems.put("StaticMemberOfParameterizedType", null);
		excludedProblems.put("BoundHasConflictingArguments", null);
		excludedProblems.put("DuplicateParameterizedMethods", null);
		excludedProblems.put("IllegalQualifiedParameterizedTypeAllocation", null);
		excludedProblems.put("DuplicateBounds", null);
		excludedProblems.put("BoundCannotBeArray", null);
		excludedProblems.put("UnsafeRawGenericConstructorInvocation", null);
		excludedProblems.put("UnsafeRawGenericMethodInvocation", null);
		excludedProblems.put("TypeParameterHidingType", null);
		excludedProblems.put("RawTypeReference", null);
		excludedProblems.put("NoAdditionalBoundAfterTypeVariable", null);
		excludedProblems.put("IncompatibleTypesInForeach", null);
		excludedProblems.put("InvalidTypeForCollection", null);
		excludedProblems.put("InvalidUsageOfTypeParameters", null);
		excludedProblems.put("InvalidUsageOfStaticImports", null);
		excludedProblems.put("InvalidUsageOfForeachStatements", null);
		excludedProblems.put("InvalidUsageOfTypeArguments", null);
		excludedProblems.put("InvalidUsageOfEnumDeclarations", null);
		excludedProblems.put("InvalidUsageOfVarargs", null);
		excludedProblems.put("InvalidUsageOfAnnotations", null);
		excludedProblems.put("InvalidUsageOfAnnotationDeclarations", null);
		excludedProblems.put("IllegalModifierForAnnotationMethod", null);
		excludedProblems.put("IllegalExtendedDimensions", null);
		excludedProblems.put("InvalidFileNameForPackageAnnotations", null);
		excludedProblems.put("IllegalModifierForAnnotationType", null);
		excludedProblems.put("IllegalModifierForAnnotationMemberType", null);
		excludedProblems.put("InvalidAnnotationMemberType", null);
		excludedProblems.put("AnnotationCircularitySelfReference", null);
		excludedProblems.put("AnnotationCircularity", null);
		excludedProblems.put("DuplicateAnnotation", null);
		excludedProblems.put("MissingValueForAnnotationMember", null);
		excludedProblems.put("DuplicateAnnotationMember", null);
		excludedProblems.put("UndefinedAnnotationMember", null);
		excludedProblems.put("AnnotationValueMustBeClassLiteral", null);
		excludedProblems.put("AnnotationValueMustBeConstant", null);
		excludedProblems.put("AnnotationFieldNeedConstantInitialization", null);
		excludedProblems.put("IllegalModifierForAnnotationField", null);
		excludedProblems.put("AnnotationCannotOverrideMethod", null);
		excludedProblems.put("AnnotationMembersCannotHaveParameters", null);
		excludedProblems.put("AnnotationMembersCannotHaveTypeParameters", null);
		excludedProblems.put("AnnotationTypeDeclarationCannotHaveSuperclass", null);
		excludedProblems.put("AnnotationTypeDeclarationCannotHaveSuperinterfaces", null);
		excludedProblems.put("DuplicateTargetInTargetAnnotation", null);
		excludedProblems.put("DisallowedTargetForAnnotation", null);
		excludedProblems.put("MethodMustOverride", null);
		excludedProblems.put("AnnotationTypeDeclarationCannotHaveConstructor", null);
		excludedProblems.put("AnnotationValueMustBeAnnotation", null);
		excludedProblems.put("AnnotationTypeUsedAsSuperInterface", null);
		excludedProblems.put("MissingOverrideAnnotation", null);
		excludedProblems.put("FieldMissingDeprecatedAnnotation", null);
		excludedProblems.put("MethodMissingDeprecatedAnnotation", null);
		excludedProblems.put("TypeMissingDeprecatedAnnotation", null);
		excludedProblems.put("UnhandledWarningToken", null);
		excludedProblems.put("AnnotationValueMustBeArrayInitializer", null);
		excludedProblems.put("CorruptedSignature", null);
		excludedProblems.put("BoxingConversion", null);
		excludedProblems.put("UnboxingConversion", null);
		excludedProblems.put("IllegalModifierForEnum", null);
		excludedProblems.put("IllegalModifierForEnumConstant", null);
		excludedProblems.put("IllegalModifierForLocalEnum", null);
		excludedProblems.put("IllegalModifierForMemberEnum", null);
		excludedProblems.put("CannotDeclareEnumSpecialMethod", null);
		excludedProblems.put("IllegalQualifiedEnumConstantLabel", null);
		excludedProblems.put("CannotExtendEnum", null);
		excludedProblems.put("CannotInvokeSuperConstructorInEnum", null);
		excludedProblems.put("EnumAbstractMethodMustBeImplemented", null);
		excludedProblems.put("EnumSwitchCannotTargetField", null);
		excludedProblems.put("IllegalModifierForEnumConstructor", null);
		excludedProblems.put("MissingEnumConstantCase", null);
		excludedProblems.put("EnumStaticFieldInInInitializerContext", null);
		excludedProblems.put("IllegalExtendedDimensionsForVarArgs", null);
		excludedProblems.put("MethodVarargsArgumentNeedCast", null);
		excludedProblems.put("ConstructorVarargsArgumentNeedCast", null);
		excludedProblems.put("VarargsConflict", null);
		excludedProblems.put("JavadocGenericMethodTypeArgumentMismatch", null);
		excludedProblems.put("JavadocNonGenericMethod", null);
		excludedProblems.put("JavadocIncorrectArityForParameterizedMethod", null);
		excludedProblems.put("JavadocParameterizedMethodArgumentTypeMismatch", null);
		excludedProblems.put("JavadocTypeArgumentsForRawGenericMethod", null);
		excludedProblems.put("JavadocGenericConstructorTypeArgumentMismatch", null);
		excludedProblems.put("JavadocNonGenericConstructor", null);
		excludedProblems.put("JavadocIncorrectArityForParameterizedConstructor", null);
		excludedProblems.put("JavadocParameterizedConstructorArgumentTypeMismatch", null);
		excludedProblems.put("JavadocTypeArgumentsForRawGenericConstructor", null);
		excludedProblems.put("ExternalProblemNotFixable", null);
		excludedProblems.put("ExternalProblemFixable", null);
		// TODO (maxime) there are obviously too many exclusions here... 
		
		Field[] fields = (iProblemClass = IProblem.class).getFields();
		for (int i = 0, length = fields.length; i < length; i++) {
			Field field = fields[i];
			int pureProblemId;
			if (field.getType() == Integer.TYPE) {
				if ((pureProblemId = field.getInt(iProblemClass) & IProblem.IgnoreCategoriesMask) != 0
						&& pureProblemId != IProblem.IgnoreCategoriesMask
						&& ProblemReporter.getProblemCategory(pureProblemId)
							== CategorizedProblem.CAT_UNSPECIFIED
						&& !excludedProblems.containsKey(field.getName())) {
					 fail("unspecified category for problem " + field.getName());
//					System.out.println("excludedProblems.put(\"" + field.getName() + "\", null);");
				}
			}
		}
	}
	catch (IllegalAccessException e) {
		fail("could not access members");
	}
}
  
}

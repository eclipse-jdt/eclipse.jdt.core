/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added the following constants
 *								   NonStaticAccessToStaticField
 *								   NonStaticAccessToStaticMethod
 *								   Task
 *								   ExpressionShouldBeAVariable
 *								   AssignmentHasNoEffect
 *     IBM Corporation - added the following constants
 *								   TooManySyntheticArgumentSlots
 *								   TooManyArrayDimensions
 *								   TooManyBytesForStringConstant
 *								   TooManyMethods
 *								   TooManyFields
 *								   NonBlankFinalLocalAssignment
 *								   ObjectCannotHaveSuperTypes
 *								   MissingSemiColon
 *								   InvalidParenthesizedExpression
 *								   EnclosingInstanceInConstructorCall
 *								   BytecodeExceeds64KLimitForConstructor
 *								   IncompatibleReturnTypeForNonInheritedInterfaceMethod
 *								   UnusedPrivateMethod
 *								   UnusedPrivateConstructor
 *								   UnusedPrivateType
 *								   UnusedPrivateField
 *								   IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod
 *								   InvalidExplicitConstructorCall
 *     IBM Corporation - added the following constants
 *								   PossibleAccidentalBooleanAssignment
 *								   SuperfluousSemicolon
 *								   IndirectAccessToStaticField
 *								   IndirectAccessToStaticMethod
 *								   IndirectAccessToStaticType
 *								   BooleanMethodThrowingException
 *								   UnnecessaryCast
 *								   UnnecessaryArgumentCast
 *								   UnnecessaryInstanceof
 *								   FinallyMustCompleteNormally
 *								   UnusedMethodDeclaredThrownException
 *								   UnusedConstructorDeclaredThrownException
 *								   InvalidCatchBlockSequence
 *								   UnqualifiedFieldAccess
 *     IBM Corporation - added the following constants
 *								   Javadoc
 *								   JavadocUnexpectedTag
 *								   JavadocMissingParamTag
 *								   JavadocMissingParamName
 *								   JavadocDuplicateParamName
 *								   JavadocInvalidParamName
 *								   JavadocMissingReturnTag
 *								   JavadocDuplicateReturnTag
 *								   JavadocMissingThrowsTag
 *								   JavadocMissingThrowsClassName
 *								   JavadocInvalidThrowsClass
 *								   JavadocDuplicateThrowsClassName
 *								   JavadocInvalidThrowsClassName
 *								   JavadocMissingSeeReference
 *								   JavadocInvalidSeeReference
 *								   JavadocInvalidSeeHref
 *								   JavadocInvalidSeeArgs
 *								   JavadocMissing
 *								   JavadocInvalidTag
 *								   JavadocMessagePrefix
 *								   EmptyControlFlowStatement
 *     IBM Corporation - added the following constants
 *								   IllegalUsageOfQualifiedTypeReference
 *								   InvalidDigit
 ****************************************************************************/
package org.eclipse.jdt.core.compiler;
 
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;

/**
 * Description of a Java problem, as detected by the compiler or some of the underlying
 * technology reusing the compiler. 
 * A problem provides access to:
 * <ul>
 * <li> its location (originating source file name, source position, line number), </li>
 * <li> its message description and a predicate to check its severity (warning or error). </li>
 * <li> its ID : an number identifying the very nature of this problem. All possible IDs are listed
 * as constants on this interface. </li>
 * </ul>
 * 
 * Note: the compiler produces IProblems internally, which are turned into markers by the JavaBuilder
 * so as to persist problem descriptions. This explains why there is no API allowing to reach IProblem detected
 * when compiling. However, the Java problem markers carry equivalent information to IProblem, in particular
 * their ID (attribute "id") is set to one of the IDs defined on this interface.
 * 
 * @since 2.0
 */
public interface IProblem { 
	
	/**
	 * Answer back the original arguments recorded into the problem.
	 * @return the original arguments recorded into the problem
	 */
	String[] getArguments();

	/**
	 * Returns the problem id
	 * 
	 * @return the problem id
	 */
	int getID();

	/**
	 * Answer a localized, human-readable message string which describes the problem.
	 * 
	 * @return a localized, human-readable message string which describes the problem
	 */
	String getMessage();

	/**
	 * Answer the file name in which the problem was found.
	 * 
	 * @return the file name in which the problem was found
	 */
	char[] getOriginatingFileName();
	
	/**
	 * Answer the end position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return the end position of the problem (inclusive), or -1 if unknown
	 */
	int getSourceEnd();

	/**
	 * Answer the line number in source where the problem begins.
	 * 
	 * @return the line number in source where the problem begins
	 */
	int getSourceLineNumber();

	/**
	 * Answer the start position of the problem (inclusive), or -1 if unknown.
	 * 
	 * @return the start position of the problem (inclusive), or -1 if unknown
	 */
	int getSourceStart();

	/**
	 * Checks the severity to see if the Error bit is set.
	 * 
	 * @return true if the Error bit is set for the severity, false otherwise
	 */
	boolean isError();

	/**
	 * Checks the severity to see if the Error bit is not set.
	 * 
	 * @return true if the Error bit is not set for the severity, false otherwise
	 */
	boolean isWarning();

	/**
	 * Set the end position of the problem (inclusive), or -1 if unknown.
	 * Used for shifting problem positions.
	 * 
	 * @param sourceEnd the given end position
	 */
	void setSourceEnd(int sourceEnd);

	/**
	 * Set the line number in source where the problem begins.
	 * 
	 * @param lineNumber the given line number
	 */
	void setSourceLineNumber(int lineNumber);

	/**
	 * Set the start position of the problem (inclusive), or -1 if unknown.
	 * Used for shifting problem positions.
	 * 
	 * @param sourceStart the given start position
	 */
	void setSourceStart(int sourceStart);
	
	/**
	 * Problem Categories
	 * The high bits of a problem ID contains information about the category of a problem. 
	 * For example, (problemID & TypeRelated) != 0, indicates that this problem is type related.
	 * 
	 * A problem category can help to implement custom problem filters. Indeed, when numerous problems
	 * are listed, focusing on import related problems first might be relevant.
	 * 
	 * When a problem is tagged as Internal, it means that no change other than a local source code change
	 * can  fix the corresponding problem.
	 */
	int TypeRelated = 0x01000000;
	int FieldRelated = 0x02000000;
	int MethodRelated = 0x04000000;
	int ConstructorRelated = 0x08000000;
	int ImportRelated = 0x10000000;
	int Internal = 0x20000000;
	int Syntax = 0x40000000;
	/**
	 * @since 3.0
	 */
	int Javadoc = 0x80000000;
	
	/**
	 * Mask to use in order to filter out the category portion of the problem ID.
	 */
	int IgnoreCategoriesMask = 0xFFFFFF;

	/**
	 * Below are listed all available problem IDs. Note that this list could be augmented in the future, 
	 * as new features are added to the Java core implementation.
	 */

	/**
	 * ID reserved for referencing an internal error inside the JavaCore implementation which
	 * may be surfaced as a problem associated with the compilation unit which caused it to occur.
	 */
	int Unclassified = 0;

	/**
	 * General type related problems
	 */
	int ObjectHasNoSuperclass = TypeRelated + 1;
	int UndefinedType = TypeRelated + 2;
	int NotVisibleType = TypeRelated + 3;
	int AmbiguousType = TypeRelated + 4;
	int UsingDeprecatedType = TypeRelated + 5;
	int InternalTypeNameProvided = TypeRelated + 6;
	/** @since 2.1 */
	int UnusedPrivateType = Internal + TypeRelated + 7;

	int IncompatibleTypesInEqualityOperator = TypeRelated + 15;
	int IncompatibleTypesInConditionalOperator = TypeRelated + 16;
	int TypeMismatch = TypeRelated + 17;
	/** @since 3.0 */
	int IndirectAccessToStaticType = Internal + TypeRelated + 18;
	
	/**
	 * Inner types related problems
	 */
	int MissingEnclosingInstanceForConstructorCall = TypeRelated + 20;
	int MissingEnclosingInstance = TypeRelated + 21;
	int IncorrectEnclosingInstanceReference = TypeRelated + 22;
	int IllegalEnclosingInstanceSpecification = TypeRelated + 23; 
	int CannotDefineStaticInitializerInLocalType = Internal + 24;
	int OuterLocalMustBeFinal = Internal + 25;
	int CannotDefineInterfaceInLocalType = Internal + 26;
	int IllegalPrimitiveOrArrayTypeForEnclosingInstance = TypeRelated + 27;
	/** @since 2.1 */
	int EnclosingInstanceInConstructorCall = Internal + 28;
	int AnonymousClassCannotExtendFinalClass = TypeRelated + 29;

	// variables
	int UndefinedName = 50;
	int UninitializedLocalVariable = Internal + 51;
	int VariableTypeCannotBeVoid = Internal + 52;
	int VariableTypeCannotBeVoidArray = Internal + 53;
	int CannotAllocateVoidArray = Internal + 54;
	// local variables
	int RedefinedLocal = Internal + 55;
	int RedefinedArgument = Internal + 56;
	// final local variables
	int DuplicateFinalLocalInitialization = Internal + 57;
	/** @since 2.1 */
	int NonBlankFinalLocalAssignment = Internal + 58;
	
	int FinalOuterLocalAssignment = Internal + 60;
	int LocalVariableIsNeverUsed = Internal + 61;
	int ArgumentIsNeverUsed = Internal + 62;
	int BytecodeExceeds64KLimit = Internal + 63;
	int BytecodeExceeds64KLimitForClinit = Internal + 64;
	int TooManyArgumentSlots = Internal + 65;
	int TooManyLocalVariableSlots = Internal + 66;
	/** @since 2.1 */
	int TooManySyntheticArgumentSlots = Internal + 67;
	/** @since 2.1 */
	int TooManyArrayDimensions = Internal + 68;
	/** @since 2.1 */
	int BytecodeExceeds64KLimitForConstructor = Internal + 69;

	// fields
	int UndefinedField = FieldRelated + 70;
	int NotVisibleField = FieldRelated + 71;
	int AmbiguousField = FieldRelated + 72;
	int UsingDeprecatedField = FieldRelated + 73;
	int NonStaticFieldFromStaticInvocation = FieldRelated + 74;
	int ReferenceToForwardField = FieldRelated + Internal + 75;
	/** @since 2.1 */
	int NonStaticAccessToStaticField = Internal + FieldRelated + 76;
	/** @since 2.1 */
	int UnusedPrivateField = Internal + FieldRelated + 77;
	/** @since 3.0 */
	int IndirectAccessToStaticField = Internal + FieldRelated + 78;
	/** @since 3.0 */
	int UnqualifiedFieldAccess = Internal + FieldRelated + 79;
	
	// blank final fields
	int FinalFieldAssignment = FieldRelated + 80;
	int UninitializedBlankFinalField = FieldRelated + 81;
	int DuplicateBlankFinalFieldInitialization = FieldRelated + 82;

	// variable hiding
	/**
	 * The local variable {0} is hiding another local variable defined in an enclosing type scope 
	 * @since 3.0
	 */
	int LocalVariableHidingLocalVariable = Internal + 90;		

	/**
	 * The local variable {0} is hiding the field {1}.{2} 
	 * @since 3.0
	 */
	int LocalVariableHidingField = Internal + FieldRelated + 91;		
	 
	/**
	 * The field {0}.{1} is hiding another local variable defined in an enclosing type scope
	 * @since 3.0 
	 */
	int FieldHidingLocalVariable = Internal + FieldRelated + 92;		

	/**
	 * The field {0}.{1} is hiding the field {2}.{3}
	 * @since 3.0 
	 */
	int FieldHidingField = Internal + FieldRelated + 93;		

	/**
	 * The argument {0} is hiding another local variable defined in an enclosing type scope
	 * @since 3.0 
	 */
	int ArgumentHidingLocalVariable = Internal + 94;		

	/**
	 * The argument {0} is hiding the field {2}.{3}
	 * @since 3.0 
	 */
	int ArgumentHidingField = Internal + 95;		
	/** @since 3.1 */
	int MissingSerialVersion = Internal + 96;
	
	// methods
	int UndefinedMethod = MethodRelated + 100;
	int NotVisibleMethod = MethodRelated + 101;
	int AmbiguousMethod = MethodRelated + 102;
	int UsingDeprecatedMethod = MethodRelated + 103;
	int DirectInvocationOfAbstractMethod = MethodRelated + 104;
	int VoidMethodReturnsValue = MethodRelated + 105;
	int MethodReturnsVoid = MethodRelated + 106;
	int MethodRequiresBody = Internal + MethodRelated + 107;
	int ShouldReturnValue = Internal + MethodRelated + 108;
	int MethodButWithConstructorName = MethodRelated + 110;
	int MissingReturnType = TypeRelated + 111;
	int BodyForNativeMethod = Internal + MethodRelated + 112;
	int BodyForAbstractMethod = Internal + MethodRelated + 113;
	int NoMessageSendOnBaseType = MethodRelated + 114;
	int ParameterMismatch = MethodRelated + 115;
	int NoMessageSendOnArrayType = MethodRelated + 116;
	/** @since 2.1 */
    int NonStaticAccessToStaticMethod = Internal + MethodRelated + 117;
	/** @since 2.1 */
	int UnusedPrivateMethod = Internal + MethodRelated + 118;
	/** @since 3.0 */
	int IndirectAccessToStaticMethod = Internal + MethodRelated + 119;

	    
	// constructors
	int UndefinedConstructor = ConstructorRelated + 130;
	int NotVisibleConstructor = ConstructorRelated + 131;
	int AmbiguousConstructor = ConstructorRelated + 132;
	int UsingDeprecatedConstructor = ConstructorRelated + 133;
	/** @since 2.1 */
	int UnusedPrivateConstructor = Internal + MethodRelated + 134;
	// explicit constructor calls
	int InstanceFieldDuringConstructorInvocation = ConstructorRelated + 135;
	int InstanceMethodDuringConstructorInvocation = ConstructorRelated + 136;
	int RecursiveConstructorInvocation = ConstructorRelated + 137;
	int ThisSuperDuringConstructorInvocation = ConstructorRelated + 138;
	/** @since 3.0 */
	int InvalidExplicitConstructorCall = ConstructorRelated + Syntax + 139;
	// implicit constructor calls
	int UndefinedConstructorInDefaultConstructor = ConstructorRelated + 140;
	int NotVisibleConstructorInDefaultConstructor = ConstructorRelated + 141;
	int AmbiguousConstructorInDefaultConstructor = ConstructorRelated + 142;
	int UndefinedConstructorInImplicitConstructorCall = ConstructorRelated + 143;
	int NotVisibleConstructorInImplicitConstructorCall = ConstructorRelated + 144;
	int AmbiguousConstructorInImplicitConstructorCall = ConstructorRelated + 145;
	int UnhandledExceptionInDefaultConstructor = TypeRelated + 146;
	int UnhandledExceptionInImplicitConstructorCall = TypeRelated + 147;
				
	// expressions
	int ArrayReferenceRequired = Internal + 150;
	int NoImplicitStringConversionForCharArrayExpression = Internal + 151;
	// constant expressions
	int StringConstantIsExceedingUtf8Limit = Internal + 152;
	int NonConstantExpression = 153;
	int NumericValueOutOfRange = Internal + 154;
	// cast expressions
	int IllegalCast = TypeRelated + 156;
	// allocations
	int InvalidClassInstantiation = TypeRelated + 157;
	int CannotDefineDimensionExpressionsWithInit = Internal + 158;
	int MustDefineEitherDimensionExpressionsOrInitializer = Internal + 159;
	// operators
	int InvalidOperator = Internal + 160;
	// statements
	int CodeCannotBeReached = Internal + 161;
	int CannotReturnInInitializer = Internal + 162;
	int InitializerMustCompleteNormally = Internal + 163;
	// assert
	int InvalidVoidExpression = Internal + 164;
	// try
	int MaskedCatch = TypeRelated + 165;
	int DuplicateDefaultCase = 166;
	int UnreachableCatch = TypeRelated + MethodRelated + 167;
	int UnhandledException = TypeRelated + 168;
	// switch       
	int IncorrectSwitchType = TypeRelated + 169;
	int DuplicateCase = FieldRelated + 170;
	// labelled
	int DuplicateLabel = Internal + 171;
	int InvalidBreak = Internal + 172;
	int InvalidContinue = Internal + 173;
	int UndefinedLabel = Internal + 174;
	//synchronized
	int InvalidTypeToSynchronized = Internal + 175;
	int InvalidNullToSynchronized = Internal + 176;
	// throw
	int CannotThrowNull = Internal + 177;
	// assignment
	/** @since 2.1 */
	int AssignmentHasNoEffect = Internal + 178;
	/** @since 3.0 */
	int PossibleAccidentalBooleanAssignment = Internal + 179;
	/** @since 3.0 */
	int SuperfluousSemicolon = Internal + 180;
	/** @since 3.0 */
	int UnnecessaryCast = Internal + TypeRelated + 181;
	/** @since 3.0 */
	int UnnecessaryArgumentCast = Internal + TypeRelated + 182;
	/** @since 3.0 */
	int UnnecessaryInstanceof = Internal + TypeRelated + 183;	
	/** @since 3.0 */
	int FinallyMustCompleteNormally = Internal + 184;	
	/** @since 3.0 */
	int UnusedMethodDeclaredThrownException = Internal + 185;	
	/** @since 3.0 */
	int UnusedConstructorDeclaredThrownException = Internal + 186;	
	/** @since 3.0 */
	int InvalidCatchBlockSequence = Internal + TypeRelated + 187;
	/** @since 3.0 */
	int EmptyControlFlowStatement = Internal + TypeRelated + 188;	
	/** @since 3.0 */
	int UnnecessaryElse = Internal + 189;	

	// inner emulation
	int NeedToEmulateFieldReadAccess = FieldRelated + 190;
	int NeedToEmulateFieldWriteAccess = FieldRelated + 191;
	int NeedToEmulateMethodAccess = MethodRelated + 192;
	int NeedToEmulateConstructorAccess = MethodRelated + 193;

	//inherited name hides enclosing name (sort of ambiguous)
	int InheritedMethodHidesEnclosingName = MethodRelated + 195;
	int InheritedFieldHidesEnclosingName = FieldRelated + 196;
	int InheritedTypeHidesEnclosingName = TypeRelated + 197;

	/** @since 3.1 */
	int IllegalUsageOfQualifiedTypeReference = Internal + Syntax + 198;

	// miscellaneous
	int ThisInStaticContext = Internal + 200;
	int StaticMethodRequested = Internal + MethodRelated + 201;
	int IllegalDimension = Internal + 202;
	int InvalidTypeExpression = Internal + 203;
	int ParsingError = Syntax + Internal + 204;
	int ParsingErrorNoSuggestion = Syntax + Internal + 205;
	int InvalidUnaryExpression = Syntax + Internal + 206;

	// syntax errors
	int InterfaceCannotHaveConstructors = Syntax + Internal + 207;
	int ArrayConstantsOnlyInArrayInitializers = Syntax + Internal + 208;
	int ParsingErrorOnKeyword = Syntax + Internal + 209;	
	int ParsingErrorOnKeywordNoSuggestion = Syntax + Internal + 210;

	int UnmatchedBracket = Syntax + Internal + 220;
	int NoFieldOnBaseType = FieldRelated + 221;
	int InvalidExpressionAsStatement = Syntax + Internal + 222;
	/** @since 2.1 */
	int ExpressionShouldBeAVariable = Syntax + Internal + 223;
	/** @since 2.1 */
	int MissingSemiColon = Syntax + Internal + 224;
	/** @since 2.1 */
	int InvalidParenthesizedExpression = Syntax + Internal + 225;
	
	/** @since 3.0 */
	int ParsingErrorInsertTokenBefore = Syntax + Internal + 230;
	/** @since 3.0 */
	int ParsingErrorInsertTokenAfter = Syntax + Internal + 231;
	/** @since 3.0 */
    int ParsingErrorDeleteToken = Syntax + Internal + 232;
    /** @since 3.0 */
    int ParsingErrorDeleteTokens = Syntax + Internal + 233;
    /** @since 3.0 */
    int ParsingErrorMergeTokens = Syntax + Internal + 234;
    /** @since 3.0 */
    int ParsingErrorInvalidToken = Syntax + Internal + 235;
    /** @since 3.0 */
    int ParsingErrorMisplacedConstruct = Syntax + Internal + 236;
    /** @since 3.0 */
    int ParsingErrorReplaceTokens = Syntax + Internal + 237;
    /** @since 3.0 */
    int ParsingErrorNoSuggestionForTokens = Syntax + Internal + 238;
    /** @since 3.0 */
    int ParsingErrorUnexpectedEOF = Syntax + Internal + 239;
    /** @since 3.0 */
    int ParsingErrorInsertToComplete = Syntax + Internal + 240;
    /** @since 3.0 */
    int ParsingErrorInsertToCompleteScope = Syntax + Internal + 241;
    /** @since 3.0 */
    int ParsingErrorInsertToCompletePhrase = Syntax + Internal + 242;
    
	// scanner errors
	int EndOfSource = Syntax + Internal + 250;
	int InvalidHexa = Syntax + Internal + 251;
	int InvalidOctal = Syntax + Internal + 252;
	int InvalidCharacterConstant = Syntax + Internal + 253;
	int InvalidEscape = Syntax + Internal + 254;
	int InvalidInput = Syntax + Internal + 255;
	int InvalidUnicodeEscape = Syntax + Internal + 256;
	int InvalidFloat = Syntax + Internal + 257;
	int NullSourceString = Syntax + Internal + 258;
	int UnterminatedString = Syntax + Internal + 259;
	int UnterminatedComment = Syntax + Internal + 260;
	/** @since 3.1 */
	int InvalidDigit = Syntax + Internal + 262;	

	// type related problems
	int InterfaceCannotHaveInitializers = TypeRelated + 300;
	int DuplicateModifierForType = TypeRelated + 301;
	int IllegalModifierForClass = TypeRelated + 302;
	int IllegalModifierForInterface = TypeRelated + 303;
	int IllegalModifierForMemberClass = TypeRelated + 304;
	int IllegalModifierForMemberInterface = TypeRelated + 305;
	int IllegalModifierForLocalClass = TypeRelated + 306;
	int ForbiddenReference = TypeRelated + 307;
	int IllegalModifierCombinationFinalAbstractForClass = TypeRelated + 308;
	int IllegalVisibilityModifierForInterfaceMemberType = TypeRelated + 309;
	int IllegalVisibilityModifierCombinationForMemberType = TypeRelated + 310;
	int IllegalStaticModifierForMemberType = TypeRelated + 311;
	int SuperclassMustBeAClass = TypeRelated + 312;
	int ClassExtendFinalClass = TypeRelated + 313;
	int DuplicateSuperInterface = TypeRelated + 314;
	int SuperInterfaceMustBeAnInterface = TypeRelated + 315;
	int HierarchyCircularitySelfReference = TypeRelated + 316;
	int HierarchyCircularity = TypeRelated + 317;
	int HidingEnclosingType = TypeRelated + 318;
	int DuplicateNestedType = TypeRelated + 319;
	int CannotThrowType = TypeRelated + 320;
	int PackageCollidesWithType = TypeRelated + 321;
	int TypeCollidesWithPackage = TypeRelated + 322;
	int DuplicateTypes = TypeRelated + 323;
	int IsClassPathCorrect = TypeRelated + 324;
	int PublicClassMustMatchFileName = TypeRelated + 325;
	int MustSpecifyPackage = 326;
	int HierarchyHasProblems = TypeRelated + 327;
	int PackageIsNotExpectedPackage = 328;
	/** @since 2.1 */
	int ObjectCannotHaveSuperTypes = 329;

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int SuperclassNotFound =  TypeRelated + 329 + ProblemReasons.NotFound; // TypeRelated + 330
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int SuperclassNotVisible =  TypeRelated + 329 + ProblemReasons.NotVisible; // TypeRelated + 331
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int SuperclassAmbiguous =  TypeRelated + 329 + ProblemReasons.Ambiguous; // TypeRelated + 332
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int SuperclassInternalNameProvided =  TypeRelated + 329 + ProblemReasons.InternalNameProvided; // TypeRelated + 333
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int SuperclassInheritedNameHidesEnclosingName =  TypeRelated + 329 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 334

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int InterfaceNotFound =  TypeRelated + 334 + ProblemReasons.NotFound; // TypeRelated + 335
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int InterfaceNotVisible =  TypeRelated + 334 + ProblemReasons.NotVisible; // TypeRelated + 336
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int InterfaceAmbiguous =  TypeRelated + 334 + ProblemReasons.Ambiguous; // TypeRelated + 337
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int InterfaceInternalNameProvided =  TypeRelated + 334 + ProblemReasons.InternalNameProvided; // TypeRelated + 338
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int InterfaceInheritedNameHidesEnclosingName =  TypeRelated + 334 + ProblemReasons.InheritedNameHidesEnclosingName; // TypeRelated + 339

	// field related problems
	int DuplicateField = FieldRelated + 340;
	int DuplicateModifierForField = FieldRelated + 341;
	int IllegalModifierForField = FieldRelated + 342;
	int IllegalModifierForInterfaceField = FieldRelated + 343;
	int IllegalVisibilityModifierCombinationForField = FieldRelated + 344;
	int IllegalModifierCombinationFinalVolatileForField = FieldRelated + 345;
	int UnexpectedStaticModifierForField = FieldRelated + 346;

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int FieldTypeNotFound =  FieldRelated + 349 + ProblemReasons.NotFound; // FieldRelated + 350
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int FieldTypeNotVisible =  FieldRelated + 349 + ProblemReasons.NotVisible; // FieldRelated + 351
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int FieldTypeAmbiguous =  FieldRelated + 349 + ProblemReasons.Ambiguous; // FieldRelated + 352
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int FieldTypeInternalNameProvided =  FieldRelated + 349 + ProblemReasons.InternalNameProvided; // FieldRelated + 353
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int FieldTypeInheritedNameHidesEnclosingName =  FieldRelated + 349 + ProblemReasons.InheritedNameHidesEnclosingName; // FieldRelated + 354
	
	// method related problems
	int DuplicateMethod = MethodRelated + 355;
	int IllegalModifierForArgument = MethodRelated + 356;
	int DuplicateModifierForMethod = MethodRelated + 357;
	int IllegalModifierForMethod = MethodRelated + 358;
	int IllegalModifierForInterfaceMethod = MethodRelated + 359;
	int IllegalVisibilityModifierCombinationForMethod = MethodRelated + 360;
	int UnexpectedStaticModifierForMethod = MethodRelated + 361;
	int IllegalAbstractModifierCombinationForMethod = MethodRelated + 362;
	int AbstractMethodInAbstractClass = MethodRelated + 363;
	int ArgumentTypeCannotBeVoid = MethodRelated + 364;
	int ArgumentTypeCannotBeVoidArray = MethodRelated + 365;
	int ReturnTypeCannotBeVoidArray = MethodRelated + 366;
	int NativeMethodsCannotBeStrictfp = MethodRelated + 367;
	int DuplicateModifierForArgument = MethodRelated + 368;

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int ArgumentTypeNotFound =  MethodRelated + 369 + ProblemReasons.NotFound; // MethodRelated + 370
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int ArgumentTypeNotVisible =  MethodRelated + 369 + ProblemReasons.NotVisible; // MethodRelated + 371
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int ArgumentTypeAmbiguous =  MethodRelated + 369 + ProblemReasons.Ambiguous; // MethodRelated + 372
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int ArgumentTypeInternalNameProvided =  MethodRelated + 369 + ProblemReasons.InternalNameProvided; // MethodRelated + 373
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int ArgumentTypeInheritedNameHidesEnclosingName =  MethodRelated + 369 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 374

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int ExceptionTypeNotFound =  MethodRelated + 374 + ProblemReasons.NotFound; // MethodRelated + 375
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int ExceptionTypeNotVisible =  MethodRelated + 374 + ProblemReasons.NotVisible; // MethodRelated + 376
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int ExceptionTypeAmbiguous =  MethodRelated + 374 + ProblemReasons.Ambiguous; // MethodRelated + 377
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int ExceptionTypeInternalNameProvided =  MethodRelated + 374 + ProblemReasons.InternalNameProvided; // MethodRelated + 378
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int ExceptionTypeInheritedNameHidesEnclosingName =  MethodRelated + 374 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 379

	/** @deprecated - problem is no longer generated, UndefinedType is used instead */
	int ReturnTypeNotFound =  MethodRelated + 379 + ProblemReasons.NotFound; // MethodRelated + 380
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int ReturnTypeNotVisible =  MethodRelated + 379 + ProblemReasons.NotVisible; // MethodRelated + 381
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int ReturnTypeAmbiguous =  MethodRelated + 379 + ProblemReasons.Ambiguous; // MethodRelated + 382
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int ReturnTypeInternalNameProvided =  MethodRelated + 379 + ProblemReasons.InternalNameProvided; // MethodRelated + 383
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int ReturnTypeInheritedNameHidesEnclosingName =  MethodRelated + 379 + ProblemReasons.InheritedNameHidesEnclosingName; // MethodRelated + 384

	// import related problems
	int ConflictingImport = ImportRelated + 385;
	int DuplicateImport = ImportRelated + 386;
	int CannotImportPackage = ImportRelated + 387;
	int UnusedImport = ImportRelated + 388;

	int ImportNotFound =  ImportRelated + 389 + ProblemReasons.NotFound; // ImportRelated + 390
	/** @deprecated - problem is no longer generated, NotVisibleType is used instead */
	int ImportNotVisible =  ImportRelated + 389 + ProblemReasons.NotVisible; // ImportRelated + 391
	/** @deprecated - problem is no longer generated, use AmbiguousType is used instead */
	int ImportAmbiguous =  ImportRelated + 389 + ProblemReasons.Ambiguous; // ImportRelated + 392
	/** @deprecated - problem is no longer generated, use InternalTypeNameProvided is used instead */
	int ImportInternalNameProvided =  ImportRelated + 389 + ProblemReasons.InternalNameProvided; // ImportRelated + 393
	/** @deprecated - problem is no longer generated, use InheritedTypeHidesEnclosingName is used instead */
	int ImportInheritedNameHidesEnclosingName =  ImportRelated + 389 + ProblemReasons.InheritedNameHidesEnclosingName; // ImportRelated + 394

	// local variable related problems
	int DuplicateModifierForVariable = MethodRelated + 395;
	int IllegalModifierForVariable = MethodRelated + 396;

	// method verifier problems
	int AbstractMethodMustBeImplemented = MethodRelated + 400;
	int FinalMethodCannotBeOverridden = MethodRelated + 401;
	int IncompatibleExceptionInThrowsClause = MethodRelated + 402;
	int IncompatibleExceptionInInheritedMethodThrowsClause = MethodRelated + 403;
	int IncompatibleReturnType = MethodRelated + 404;
	int InheritedMethodReducesVisibility = MethodRelated + 405;
	int CannotOverrideAStaticMethodWithAnInstanceMethod = MethodRelated + 406;
	int CannotHideAnInstanceMethodWithAStaticMethod = MethodRelated + 407;
	int StaticInheritedMethodConflicts = MethodRelated + 408;
	int MethodReducesVisibility = MethodRelated + 409;
	int OverridingNonVisibleMethod = MethodRelated + 410;
	int AbstractMethodCannotBeOverridden = MethodRelated + 411;
	int OverridingDeprecatedMethod = MethodRelated + 412;
	/** @since 2.1 */
	int IncompatibleReturnTypeForNonInheritedInterfaceMethod = MethodRelated + 413;
	/** @since 2.1 */
	int IncompatibleExceptionInThrowsClauseForNonInheritedInterfaceMethod = MethodRelated + 414;
	
	// code snippet support
	int CodeSnippetMissingClass = Internal + 420;
	int CodeSnippetMissingMethod = Internal + 421;
	int NonExternalizedStringLiteral = Internal + 261;
	int CannotUseSuperInCodeSnippet = Internal + 422;
	
	//constant pool
	int TooManyConstantsInConstantPool = Internal + 430;
	/** @since 2.1 */
	int TooManyBytesForStringConstant = Internal + 431;

	// static constraints
	/** @since 2.1 */
	int TooManyFields = Internal + 432;
	/** @since 2.1 */
	int TooManyMethods = Internal + 433; 
		
	// 1.4 features
	// assertion warning
	int UseAssertAsAnIdentifier = Internal + 440;
	
	// 1.5 features
	int UseEnumAsAnIdentifier = Internal + 441;
	
	// detected task
	/** @since 2.1 */
	int Task = Internal + 450;
	
	// block
	/** @since 3.0 */
	int UndocumentedEmptyBlock = Internal + 460;
		
	/*
	 * Javadoc comments
	 */
	/** @since 3.0 */
	int JavadocUnexpectedTag = Javadoc + Internal + 470;
	/** @since 3.0 */
	int JavadocMissingParamTag = Javadoc + Internal + 471;
	/** @since 3.0 */
	int JavadocMissingParamName = Javadoc + Internal + 472;
	/** @since 3.0 */
	int JavadocDuplicateParamName = Javadoc + Internal + 473;
	/** @since 3.0 */
	int JavadocInvalidParamName = Javadoc + Internal + 474;
	/** @since 3.0 */
	int JavadocMissingReturnTag = Javadoc + Internal + 475;
	/** @since 3.0 */
	int JavadocDuplicateReturnTag = Javadoc + Internal + 476;
	/** @since 3.0 */
	int JavadocMissingThrowsTag = Javadoc + Internal + 477;
	/** @since 3.0 */
	int JavadocMissingThrowsClassName = Javadoc + Internal + 478;
	/** @since 3.0 */
	int JavadocInvalidThrowsClass = Javadoc + Internal + 479;
	/** @since 3.0 */
	int JavadocDuplicateThrowsClassName = Javadoc + Internal + 480;
	/** @since 3.0 */
	int JavadocInvalidThrowsClassName = Javadoc + Internal + 481;
	/** @since 3.0 */
	int JavadocMissingReference = Javadoc + Internal + 482;
	/** @since 3.0 */
	int JavadocInvalidReference = Javadoc + Internal + 483;
	/** @since 3.0 */
	int JavadocInvalidSeeHref = Javadoc + Internal + 484;
	/** @since 3.0 */
	int JavadocInvalidSeeArgs = Javadoc + Internal + 485;
	/** @since 3.0 */
	int JavadocMissing = Javadoc + Internal + 486;
	/** @since 3.0 */
	int JavadocInvalidTag = Javadoc + Internal + 487;
	/*
	 * ID for field errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedField = Javadoc + Internal + 488;
	/** @since 3.0 */
	int JavadocNotVisibleField = Javadoc + Internal + 489;
	/** @since 3.0 */
	int JavadocAmbiguousField = Javadoc + Internal + 490;
	/** @since 3.0 */
	int JavadocUsingDeprecatedField = Javadoc + Internal + 491;
	/*
	 * IDs for constructor errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedConstructor = Javadoc + Internal + 492;
	/** @since 3.0 */
	int JavadocNotVisibleConstructor = Javadoc + Internal + 493;
	/** @since 3.0 */
	int JavadocAmbiguousConstructor = Javadoc + Internal + 494;
	/** @since 3.0 */
	int JavadocUsingDeprecatedConstructor = Javadoc + Internal + 495;
	/*
	 * IDs for method errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedMethod = Javadoc + Internal + 496;
	/** @since 3.0 */
	int JavadocNotVisibleMethod = Javadoc + Internal + 497;
	/** @since 3.0 */
	int JavadocAmbiguousMethod = Javadoc + Internal + 498;
	/** @since 3.0 */
	int JavadocUsingDeprecatedMethod = Javadoc + Internal + 499;
	/** @since 3.0 */
	int JavadocNoMessageSendOnBaseType = Javadoc + Internal + 500;
	/** @since 3.0 */
	int JavadocParameterMismatch = Javadoc + Internal + 501;
	/** @since 3.0 */
	int JavadocNoMessageSendOnArrayType = Javadoc + Internal + 502;
	/*
	 * IDs for type errors in Javadoc
	 */
	/** @since 3.0 */
	int JavadocUndefinedType = Javadoc + Internal + 503;
	/** @since 3.0 */
	int JavadocNotVisibleType = Javadoc + Internal + 504;
	/** @since 3.0 */
	int JavadocAmbiguousType = Javadoc + Internal + 505;
	/** @since 3.0 */
	int JavadocUsingDeprecatedType = Javadoc + Internal + 506;
	/** @since 3.0 */
	int JavadocInternalTypeNameProvided = Javadoc + Internal + 507;
	/** @since 3.0 */
	int JavadocInheritedMethodHidesEnclosingName = Javadoc + Internal + 508;
	/** @since 3.0 */
	int JavadocInheritedFieldHidesEnclosingName = Javadoc + Internal + 509;
	/** @since 3.0 */
	int JavadocInheritedNameHidesEnclosingTypeName = Javadoc + Internal + 510;
	/** @since 3.0 */
	int JavadocAmbiguousMethodReference = Javadoc + Internal + 511;
	/** @since 3.0 */
	int JavadocUnterminatedInlineTag = Javadoc + Internal + 512;
	/** @since 3.0 */
	int JavadocMissingHashCharacter = Javadoc + Internal + 513;
	/** @since 3.0 */
	int JavadocMalformedSeeReference = Javadoc + Internal + 514;
	/** @since 3.0 */
	int JavadocEmptyReturnTag = Javadoc + Internal + 515;
	/** @since 3.1 */
	int JavadocInvalidValueReference = Javadoc + Internal + 516;
	/** @since 3.1 */
	int JavadocUnexpectedText = Javadoc + Internal + 517;
	/** @since 3.0 */
	int JavadocMessagePrefix = Internal + 519;

	/**
	 * Generics
	 */
	/** @since 3.1 */
	int DuplicateTypeVariable = Internal + 520;
	/** @since 3.1 */
	int IllegalTypeVariableSuperReference = Internal + 521;
	/** @since 3.1 */
	int TypeVariableReferenceFromStaticContext = Internal + 522;
	/** @since 3.1 */
	int ObjectCannotBeGeneric = Internal + 523;
	/** @since 3.1 */
	int NonGenericType = TypeRelated + 524;
	/** @since 3.1 */
	int IncorrectArityForParameterizedType = TypeRelated + 525;
	/** @since 3.1 */
	int TypeArgumentMismatch = TypeRelated + 526;
	/** @since 3.1 */
	int DuplicateMethodErasure = TypeRelated + 527;
	/** @since 3.1 */
	int ReferenceToForwardTypeVariable = TypeRelated + 528;
    /** @since 3.1 */
	int BoundsMustBeAnInterface = TypeRelated + 529;	
    /** @since 3.1 */
	int UnsafeRawConstructorInvocation = TypeRelated + 530;
    /** @since 3.1 */
	int UnsafeRawMethodInvocation = TypeRelated + 531;
    /** @since 3.1 */
	int UnsafeRawConversion = TypeRelated + 532;
    /** @since 3.1 */
	int InvalidTypeVariableExceptionType = TypeRelated + 533;
	/** @since 3.1 */
	int InvalidParameterizedExceptionType = TypeRelated + 534;
	/** @since 3.1 */
	int IllegalGenericArray = TypeRelated + 535;
	/** @since 3.1 */
	int UnsafeRawFieldAssignment = TypeRelated + 536;
	/** @since 3.1 */
	int FinalBoundForTypeVariable = TypeRelated + 537;
	/** @since 3.1 */
	int UndefinedTypeVariable = Internal + 538;
	/** @since 3.1 */
	int SuperInterfacesCollide = TypeRelated + 539;
	/** @since 3.1 */
	int WildcardConstructorInvocation = TypeRelated + 540;
	/** @since 3.1 */
	int WildcardMethodInvocation = TypeRelated + 541;
	/** @since 3.1 */
	int WildcardFieldAssignment = TypeRelated + 542;
	/** @since 3.1 */
	int GenericMethodTypeArgumentMismatch = TypeRelated + 543;
	/** @since 3.1 */
	int GenericConstructorTypeArgumentMismatch = TypeRelated + 544;
	/** @since 3.1 */
	int UnsafeGenericCast = TypeRelated + 545;
	/** @since 3.1 */
	int IllegalInstanceofParameterizedType = Internal + 546;
	/** @since 3.1 */
	int IllegalInstanceofTypeParameter = Internal + 547;
	/** @since 3.1 */
	int NonGenericMethod = TypeRelated + 548;
	/** @since 3.1 */
	int IncorrectArityForParameterizedMethod = TypeRelated + 549;
	/** @since 3.1 */
	int ParameterizedMethodArgumentTypeMismatch = TypeRelated + 550;
	/** @since 3.1 */
	int NonGenericConstructor = TypeRelated + 551;
	/** @since 3.1 */
	int IncorrectArityForParameterizedConstructor = TypeRelated + 552;
	/** @since 3.1 */
	int ParameterizedConstructorArgumentTypeMismatch = TypeRelated + 553;
	/** @since 3.1 */
	int TypeArgumentsForRawGenericMethod = TypeRelated + 554;
	/** @since 3.1 */
	int TypeArgumentsForRawGenericConstructor = TypeRelated + 555;
	/** @since 3.1 */
	int SuperTypeUsingWildcard = TypeRelated + 556;
	/** @since 3.1 */
	int GenericTypeCannotExtendThrowable = TypeRelated + 557;
	/** @since 3.1 */
	int IllegalClassLiteralForTypeVariable = TypeRelated + 558;
	/** @since 3.1 */
	int UnsafeReturnTypeOverride = MethodRelated + 559;
	/** @since 3.1 */
	int MethodNameClash = MethodRelated + 560;
	/** @since 3.1 */
	int RawMemberTypeCannotBeParameterized = TypeRelated + 561;
	/** @since 3.1 */
	int MissingArgumentsForParameterizedMemberType = TypeRelated + 562;	
	/** @since 3.1 */
	int StaticMemberOfParameterizedType = TypeRelated + 563;	
	
	/**
	 * Foreach
	 */
	/** @since 3.1 */	
	int IncompatibleTypesInForeach = TypeRelated + 580;	
	/** @since 3.1 */
	int InvalidTypeForCollection = Internal + 581;
	
	/**
	 * 1.5 Syntax errors (when source level < 1.5)
	 */
	/** @since 3.1 */
    int InvalidUsageOfTypeParameters = Syntax + Internal + 590;
    /** @since 3.1 */
    int InvalidUsageOfStaticImports = Syntax + Internal + 591;
    /** @since 3.1 */
    int InvalidUsageOfForeachStatements = Syntax + Internal + 592;
    /** @since 3.1 */
    int InvalidUsageOfTypeArguments = Syntax + Internal + 593;
    /** @since 3.1 */
    int InvalidUsageOfEnumDeclarations = Syntax + Internal + 594;
    /** @since 3.1 */
    int InvalidUsageOfVarargs = Syntax + Internal + 595;
    /** @since 3.1 */
    int InvalidUsageOfAnnotations = Syntax + Internal + 596;
    /** @since 3.1 */
    int InvalidUsageOfAnnotationDeclarations = Syntax + Internal + 597;
    
    /**
     * Annotation
     */
    /** @since 3.0 */
    int AnnotationButConstructorName = MethodRelated + 600;
    /** @since 3.0 */
    int IllegalExtendedDimensions = MethodRelated + 601;
    /** @since 3.0 */
	int InvalidFileNameForPackageAnnotations = Syntax + Internal + 602;
	
	/**
	 * Corrupted binaries
	 */
	int CorruptedSignature = Internal + 700;
}

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
package org.eclipse.jdt.internal.compiler.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class CompilerOptions {
	
	/**
	 * Option IDs
	 */
	public static final String OPTION_LocalVariableAttribute = "org.eclipse.jdt.core.compiler.debug.localVariable"; //$NON-NLS-1$
	public static final String OPTION_LineNumberAttribute = "org.eclipse.jdt.core.compiler.debug.lineNumber"; //$NON-NLS-1$
	public static final String OPTION_SourceFileAttribute = "org.eclipse.jdt.core.compiler.debug.sourceFile"; //$NON-NLS-1$
	public static final String OPTION_PreserveUnusedLocal = "org.eclipse.jdt.core.compiler.codegen.unusedLocal"; //$NON-NLS-1$
	public static final String OPTION_DocCommentSupport= "org.eclipse.jdt.core.compiler.doc.comment.support"; //$NON-NLS-1$
	public static final String OPTION_ReportMethodWithConstructorName = "org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
	public static final String OPTION_ReportOverridingPackageDefaultMethod = "org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecation = "org.eclipse.jdt.core.compiler.problem.deprecation"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecationInDeprecatedCode = "org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecationWhenOverridingDeprecatedMethod = "org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportHiddenCatchBlock = "org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedLocal = "org.eclipse.jdt.core.compiler.problem.unusedLocal"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedParameter = "org.eclipse.jdt.core.compiler.problem.unusedParameter"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedParameterWhenImplementingAbstract = "org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedParameterWhenOverridingConcrete = "org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedImport = "org.eclipse.jdt.core.compiler.problem.unusedImport"; //$NON-NLS-1$
	public static final String OPTION_ReportSyntheticAccessEmulation = "org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
	public static final String OPTION_ReportNoEffectAssignment = "org.eclipse.jdt.core.compiler.problem.noEffectAssignment"; //$NON-NLS-1$
	public static final String OPTION_ReportLocalVariableHiding = "org.eclipse.jdt.core.compiler.problem.localVariableHiding"; //$NON-NLS-1$
	public static final String OPTION_ReportSpecialParameterHidingField = "org.eclipse.jdt.core.compiler.problem.specialParameterHidingField"; //$NON-NLS-1$
	public static final String OPTION_ReportFieldHiding = "org.eclipse.jdt.core.compiler.problem.fieldHiding"; //$NON-NLS-1$
	public static final String OPTION_ReportTypeParameterHiding = "org.eclipse.jdt.core.compiler.problem.typeParameterHiding"; //$NON-NLS-1$
	public static final String OPTION_ReportPossibleAccidentalBooleanAssignment = "org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment"; //$NON-NLS-1$
	public static final String OPTION_ReportNonExternalizedStringLiteral = "org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$
	public static final String OPTION_ReportIncompatibleNonInheritedInterfaceMethod = "org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedPrivateMember = "org.eclipse.jdt.core.compiler.problem.unusedPrivateMember"; //$NON-NLS-1$
	public static final String OPTION_ReportNoImplicitStringConversion = "org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion"; //$NON-NLS-1$
	public static final String OPTION_ReportAssertIdentifier = "org.eclipse.jdt.core.compiler.problem.assertIdentifier"; //$NON-NLS-1$
	public static final String OPTION_ReportEnumIdentifier = "org.eclipse.jdt.core.compiler.problem.enumIdentifier"; //$NON-NLS-1$
	public static final String OPTION_ReportNonStaticAccessToStatic = "org.eclipse.jdt.core.compiler.problem.staticAccessReceiver"; //$NON-NLS-1$
	public static final String OPTION_ReportIndirectStaticAccess = "org.eclipse.jdt.core.compiler.problem.indirectStaticAccess"; //$NON-NLS-1$
	public static final String OPTION_ReportEmptyStatement = "org.eclipse.jdt.core.compiler.problem.emptyStatement"; //$NON-NLS-1$
	public static final String OPTION_ReportUnnecessaryTypeCheck = "org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck"; //$NON-NLS-1$
	public static final String OPTION_ReportUnnecessaryElse = "org.eclipse.jdt.core.compiler.problem.unnecessaryElse"; //$NON-NLS-1$
	public static final String OPTION_ReportUndocumentedEmptyBlock = "org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidJavadoc = "org.eclipse.jdt.core.compiler.problem.invalidJavadoc"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidJavadocTags = "org.eclipse.jdt.core.compiler.problem.invalidJavadocTags"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidJavadocTagsDeprecatedRef = "org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsDeprecatedRef"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidJavadocTagsNotVisibleRef = "org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsNotVisibleRef"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidJavadocTagsVisibility = "org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocTags = "org.eclipse.jdt.core.compiler.problem.missingJavadocTags"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocTagsVisibility = "org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocTagsOverriding = "org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocComments = "org.eclipse.jdt.core.compiler.problem.missingJavadocComments"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocCommentsVisibility = "org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadocCommentsOverriding = "org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding"; //$NON-NLS-1$
	public static final String OPTION_ReportFinallyBlockNotCompletingNormally = "org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedDeclaredThrownException = "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding = "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding"; //$NON-NLS-1$
	public static final String OPTION_ReportUnqualifiedFieldAccess = "org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess"; //$NON-NLS-1$
	public static final String OPTION_ReportUncheckedTypeOperation = "org.eclipse.jdt.core.compiler.problem.uncheckedTypeOperation"; //$NON-NLS-1$
	public static final String OPTION_ReportRawTypeReference =  "org.eclipse.jdt.core.compiler.problem.rawTypeReference"; //$NON-NLS-1$
	public static final String OPTION_ReportFinalParameterBound = "org.eclipse.jdt.core.compiler.problem.finalParameterBound"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingSerialVersion = "org.eclipse.jdt.core.compiler.problem.missingSerialVersion"; //$NON-NLS-1$
	public static final String OPTION_ReportVarargsArgumentNeedCast = "org.eclipse.jdt.core.compiler.problem.varargsArgumentNeedCast"; //$NON-NLS-1$
	public static final String OPTION_Source = "org.eclipse.jdt.core.compiler.source"; //$NON-NLS-1$
	public static final String OPTION_TargetPlatform = "org.eclipse.jdt.core.compiler.codegen.targetPlatform"; //$NON-NLS-1$
	public static final String OPTION_Compliance = "org.eclipse.jdt.core.compiler.compliance"; //$NON-NLS-1$
	public static final String OPTION_Encoding = "org.eclipse.jdt.core.encoding"; //$NON-NLS-1$
	public static final String OPTION_MaxProblemPerUnit = "org.eclipse.jdt.core.compiler.maxProblemPerUnit"; //$NON-NLS-1$
	public static final String OPTION_TaskTags = "org.eclipse.jdt.core.compiler.taskTags"; //$NON-NLS-1$
	public static final String OPTION_TaskPriorities = "org.eclipse.jdt.core.compiler.taskPriorities"; //$NON-NLS-1$
	public static final String OPTION_TaskCaseSensitive = "org.eclipse.jdt.core.compiler.taskCaseSensitive"; //$NON-NLS-1$
	public static final String OPTION_InlineJsr = "org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode"; //$NON-NLS-1$
	public static final String OPTION_ReportNullReference = "org.eclipse.jdt.core.compiler.problem.nullReference"; //$NON-NLS-1$
	public static final String OPTION_ReportAutoboxing = "org.eclipse.jdt.core.compiler.problem.autoboxing"; //$NON-NLS-1$
	public static final String OPTION_ReportAnnotationSuperInterface = "org.eclipse.jdt.core.compiler.problem.annotationSuperInterface"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingOverrideAnnotation = "org.eclipse.jdt.core.compiler.problem.missingOverrideAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingDeprecatedAnnotation = "org.eclipse.jdt.core.compiler.problem.missingDeprecatedAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportIncompleteEnumSwitch = "org.eclipse.jdt.core.compiler.problem.incompleteEnumSwitch"; //$NON-NLS-1$
	public static final String OPTION_ReportForbiddenReference =  "org.eclipse.jdt.core.compiler.problem.forbiddenReference"; //$NON-NLS-1$
	public static final String OPTION_ReportDiscouragedReference =  "org.eclipse.jdt.core.compiler.problem.discouragedReference"; //$NON-NLS-1$
	public static final String OPTION_SuppressWarnings =  "org.eclipse.jdt.core.compiler.problem.suppressWarnings"; //$NON-NLS-1$
	public static final String OPTION_ReportUnhandledWarningToken =  "org.eclipse.jdt.core.compiler.problem.unhandledWarningToken"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedLabel =  "org.eclipse.jdt.core.compiler.problem.unusedLabel"; //$NON-NLS-1$
	public static final String OPTION_FatalOptionalError =  "org.eclipse.jdt.core.compiler.problem.fatalOptionalError"; //$NON-NLS-1$
	public static final String OPTION_ReportParameterAssignment =  "org.eclipse.jdt.core.compiler.problem.parameterAssignment"; //$NON-NLS-1$
	
	// Backward compatibility
	public static final String OPTION_ReportInvalidAnnotation = "org.eclipse.jdt.core.compiler.problem.invalidAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingAnnotation = "org.eclipse.jdt.core.compiler.problem.missingAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadoc = "org.eclipse.jdt.core.compiler.problem.missingJavadoc"; //$NON-NLS-1$

	/* should surface ??? */
	public static final String OPTION_PrivateConstructorAccess = "org.eclipse.jdt.core.compiler.codegen.constructorAccessEmulation"; //$NON-NLS-1$

	//TODO temporary option
	public static final String OPTION_StatementsRecovery = "org.eclipse.jdt.core.compiler.statementsRecovery"; //$NON-NLS-1$

	/**
	 * Possible values for configurable options
	 */
	public static final String GENERATE = "generate";//$NON-NLS-1$
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
	public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
	public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
	public static final String VERSION_1_5 = "1.5"; //$NON-NLS-1$
	public static final String VERSION_1_6 = "1.6"; //$NON-NLS-1$	
	public static final String ERROR = "error"; //$NON-NLS-1$
	public static final String WARNING = "warning"; //$NON-NLS-1$
	public static final String IGNORE = "ignore"; //$NON-NLS-1$
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$
	public static final String PUBLIC = "public";	//$NON-NLS-1$
	public static final String PROTECTED = "protected";	//$NON-NLS-1$
	public static final String DEFAULT = "default";	//$NON-NLS-1$
	public static final String PRIVATE = "private";	//$NON-NLS-1$
	
	/**
	 * Bit mask for configurable problems (error/warning threshold)
	 */
	public static final long MethodWithConstructorName = ASTNode.Bit1;
	public static final long OverriddenPackageDefaultMethod = ASTNode.Bit2;
	public static final long UsingDeprecatedAPI = ASTNode.Bit3;
	public static final long MaskedCatchBlock = ASTNode.Bit4;
	public static final long UnusedLocalVariable = ASTNode.Bit5;
	public static final long UnusedArgument = ASTNode.Bit6;
	public static final long NoImplicitStringConversion = ASTNode.Bit7;
	public static final long AccessEmulation = ASTNode.Bit8;
	public static final long NonExternalizedString = ASTNode.Bit9;
	public static final long AssertUsedAsAnIdentifier = ASTNode.Bit10;
	public static final long UnusedImport = ASTNode.Bit11;
	public static final long NonStaticAccessToStatic = ASTNode.Bit12;
	public static final long Task = ASTNode.Bit13;
	public static final long NoEffectAssignment = ASTNode.Bit14;
	public static final long IncompatibleNonInheritedInterfaceMethod = ASTNode.Bit15;
	public static final long UnusedPrivateMember = ASTNode.Bit16;
	public static final long LocalVariableHiding = ASTNode.Bit17;
	public static final long FieldHiding = ASTNode.Bit18;
	public static final long AccidentalBooleanAssign = ASTNode.Bit19;
	public static final long EmptyStatement = ASTNode.Bit20;
	public static final long MissingJavadocComments  = ASTNode.Bit21;
	public static final long MissingJavadocTags = ASTNode.Bit22;
	public static final long UnqualifiedFieldAccess = ASTNode.Bit23;
	public static final long UnusedDeclaredThrownException = ASTNode.Bit24;
	public static final long FinallyBlockNotCompleting = ASTNode.Bit25;
	public static final long InvalidJavadoc = ASTNode.Bit26;
	public static final long UnnecessaryTypeCheck = ASTNode.Bit27;
	public static final long UndocumentedEmptyBlock = ASTNode.Bit28;
	public static final long IndirectStaticAccess = ASTNode.Bit29;
	public static final long UnnecessaryElse  = ASTNode.Bit30;
	public static final long UncheckedTypeOperation = ASTNode.Bit31;
	public static final long FinalParameterBound = ASTNode.Bit32L;
	public static final long MissingSerialVersion = ASTNode.Bit33L;
	public static final long EnumUsedAsAnIdentifier = ASTNode.Bit34L;	
	public static final long ForbiddenReference = ASTNode.Bit35L;
	public static final long VarargsArgumentNeedCast = ASTNode.Bit36L;
	public static final long NullReference = ASTNode.Bit37L;
	public static final long AutoBoxing = ASTNode.Bit38L;
	public static final long AnnotationSuperInterface = ASTNode.Bit39L;
	public static final long TypeParameterHiding = ASTNode.Bit40L;
	public static final long MissingOverrideAnnotation = ASTNode.Bit41L;
	public static final long IncompleteEnumSwitch = ASTNode.Bit42L;
	public static final long MissingDeprecatedAnnotation = ASTNode.Bit43L;
	public static final long DiscouragedReference = ASTNode.Bit44L;
	public static final long UnhandledWarningToken = ASTNode.Bit45L;
	public static final long RawTypeReference = ASTNode.Bit46L;
	public static final long UnusedLabel = ASTNode.Bit47L;
	public static final long ParameterAssignment = ASTNode.Bit48L;
	
	// Default severity level for handlers
	public long errorThreshold = 0;
		
	public long warningThreshold = 
		MethodWithConstructorName 
		| UsingDeprecatedAPI 
		| MaskedCatchBlock 
		| OverriddenPackageDefaultMethod
		| UnusedImport
		| NonStaticAccessToStatic
		| NoEffectAssignment
		| IncompatibleNonInheritedInterfaceMethod
		| NoImplicitStringConversion
		| FinallyBlockNotCompleting
		| AssertUsedAsAnIdentifier
		| EnumUsedAsAnIdentifier
		| UncheckedTypeOperation
		| MissingSerialVersion
		| VarargsArgumentNeedCast
		| ForbiddenReference
		| DiscouragedReference
		| AnnotationSuperInterface
		| TypeParameterHiding
		| FinalParameterBound
		| UnhandledWarningToken
		| UnusedLocalVariable
		| UnusedPrivateMember
		| UnusedLabel
		/*| NullReference*/;

	// By default only lines and source attributes are generated.
	public int produceDebugAttributes = ClassFileConstants.ATTR_SOURCE | ClassFileConstants.ATTR_LINES;

	public long complianceLevel = ClassFileConstants.JDK1_4; // by default be compliant with 1.4
	public long sourceLevel = ClassFileConstants.JDK1_3; //1.3 source behavior by default
	public long targetJDK = ClassFileConstants.JDK1_2; // default generates for JVM1.2

	// toggle private access emulation for 1.2 (constr. accessor has extra arg on constructor) or 1.3 (make private constructor default access when access needed)
	public boolean isPrivateConstructorAccessChangingVisibility = false; // by default, follows 1.2
	
	// source encoding format
	public String defaultEncoding = null; // will use the platform default encoding
	
	// print what unit is being processed
	public boolean verbose = Compiler.DEBUG;

	// indicates if reference info is desired
	public boolean produceReferenceInfo = false;

	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public boolean preserveAllLocalVariables = false;

	// indicates whether literal expressions are inlined at parse-time or not
	public boolean parseLiteralExpressionsAsConstants = true;

	// max problems per compilation unit
	public int maxProblemsPerUnit = 100; // no more than 100 problems per default
	
	// tags used to recognize tasks in comments
	public char[][] taskTags = null;
	public char[][] taskPriorites = null;
	public boolean isTaskCaseSensitive = true;

	// deprecation report
	public boolean reportDeprecationInsideDeprecatedCode = false;
	public boolean reportDeprecationWhenOverridingDeprecatedMethod = false;
	
	// unused parameters report
	public boolean reportUnusedParameterWhenImplementingAbstract = false;
	public boolean reportUnusedParameterWhenOverridingConcrete = false;

	// unused declaration of thrown exception
	public boolean reportUnusedDeclaredThrownExceptionWhenOverriding = false;
	
	// constructor/setter parameter hiding
	public boolean reportSpecialParameterHidingField = false;

	// check javadoc comments tags
	public int reportInvalidJavadocTagsVisibility = ClassFileConstants.AccPublic; 
	public boolean reportInvalidJavadocTags = false;
	public boolean reportInvalidJavadocTagsDeprecatedRef = false;
	public boolean reportInvalidJavadocTagsNotVisibleRef = false;

	// check missing javadoc tags
	public int reportMissingJavadocTagsVisibility = ClassFileConstants.AccPublic; 
	public boolean reportMissingJavadocTagsOverriding = false;

	// check missing javadoc comments
	public int reportMissingJavadocCommentsVisibility = ClassFileConstants.AccPublic; 
	public boolean reportMissingJavadocCommentsOverriding = false; 

	// JSR bytecode inlining
	public boolean inlineJsrBytecode = false;
	
	// javadoc comment support
	public boolean docCommentSupport = false;
	
	// suppress warning annotation
	public boolean suppressWarnings = true;
	
	// treat optional error as fatal or just like warning?
	public boolean treatOptionalErrorAsFatal = true;
	
	// parser perform statements recovery 
	public boolean performStatementsRecovery = true;
	
	// store annotations
	public boolean storeAnnotations = false;
	
	/** 
	 * Initializing the compiler options with defaults
	 */
	public CompilerOptions(){
		// use default options
	}

	/** 
	 * Initializing the compiler options with external settings
	 * @param settings
	 */
	public CompilerOptions(Map settings){

		if (settings == null) return;
		set(settings);		
	}

	public Map getMap() {
		Map optionsMap = new HashMap(30);
		optionsMap.put(OPTION_LocalVariableAttribute, (this.produceDebugAttributes & ClassFileConstants.ATTR_VARS) != 0 ? GENERATE : DO_NOT_GENERATE); 
		optionsMap.put(OPTION_LineNumberAttribute, (this.produceDebugAttributes & ClassFileConstants.ATTR_LINES) != 0 ? GENERATE : DO_NOT_GENERATE);
		optionsMap.put(OPTION_SourceFileAttribute, (this.produceDebugAttributes & ClassFileConstants.ATTR_SOURCE) != 0 ? GENERATE : DO_NOT_GENERATE);
		optionsMap.put(OPTION_PreserveUnusedLocal, this.preserveAllLocalVariables ? PRESERVE : OPTIMIZE_OUT);
		optionsMap.put(OPTION_DocCommentSupport, this.docCommentSupport ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportMethodWithConstructorName, getSeverityString(MethodWithConstructorName)); 
		optionsMap.put(OPTION_ReportOverridingPackageDefaultMethod, getSeverityString(OverriddenPackageDefaultMethod)); 
		optionsMap.put(OPTION_ReportDeprecation, getSeverityString(UsingDeprecatedAPI)); 
		optionsMap.put(OPTION_ReportDeprecationInDeprecatedCode, this.reportDeprecationInsideDeprecatedCode ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, this.reportDeprecationWhenOverridingDeprecatedMethod ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportHiddenCatchBlock, getSeverityString(MaskedCatchBlock)); 
		optionsMap.put(OPTION_ReportUnusedLocal, getSeverityString(UnusedLocalVariable)); 
		optionsMap.put(OPTION_ReportUnusedParameter, getSeverityString(UnusedArgument)); 
		optionsMap.put(OPTION_ReportUnusedImport, getSeverityString(UnusedImport)); 
		optionsMap.put(OPTION_ReportSyntheticAccessEmulation, getSeverityString(AccessEmulation)); 
		optionsMap.put(OPTION_ReportNoEffectAssignment, getSeverityString(NoEffectAssignment)); 
		optionsMap.put(OPTION_ReportNonExternalizedStringLiteral, getSeverityString(NonExternalizedString)); 
		optionsMap.put(OPTION_ReportNoImplicitStringConversion, getSeverityString(NoImplicitStringConversion)); 
		optionsMap.put(OPTION_ReportNonStaticAccessToStatic, getSeverityString(NonStaticAccessToStatic)); 
		optionsMap.put(OPTION_ReportIndirectStaticAccess, getSeverityString(IndirectStaticAccess)); 
		optionsMap.put(OPTION_ReportIncompatibleNonInheritedInterfaceMethod, getSeverityString(IncompatibleNonInheritedInterfaceMethod)); 
		optionsMap.put(OPTION_ReportUnusedPrivateMember, getSeverityString(UnusedPrivateMember)); 
		optionsMap.put(OPTION_ReportLocalVariableHiding, getSeverityString(LocalVariableHiding)); 
		optionsMap.put(OPTION_ReportFieldHiding, getSeverityString(FieldHiding)); 
		optionsMap.put(OPTION_ReportTypeParameterHiding, getSeverityString(TypeParameterHiding)); 
		optionsMap.put(OPTION_ReportPossibleAccidentalBooleanAssignment, getSeverityString(AccidentalBooleanAssign)); 
		optionsMap.put(OPTION_ReportEmptyStatement, getSeverityString(EmptyStatement)); 
		optionsMap.put(OPTION_ReportAssertIdentifier, getSeverityString(AssertUsedAsAnIdentifier)); 
		optionsMap.put(OPTION_ReportEnumIdentifier, getSeverityString(EnumUsedAsAnIdentifier)); 
		optionsMap.put(OPTION_ReportUndocumentedEmptyBlock, getSeverityString(UndocumentedEmptyBlock)); 
		optionsMap.put(OPTION_ReportUnnecessaryTypeCheck, getSeverityString(UnnecessaryTypeCheck)); 
		optionsMap.put(OPTION_ReportUnnecessaryElse, getSeverityString(UnnecessaryElse)); 
		optionsMap.put(OPTION_ReportAutoboxing, getSeverityString(AutoBoxing)); 
		optionsMap.put(OPTION_ReportAnnotationSuperInterface, getSeverityString(AnnotationSuperInterface)); 
		optionsMap.put(OPTION_ReportIncompleteEnumSwitch, getSeverityString(IncompleteEnumSwitch)); 
		optionsMap.put(OPTION_ReportInvalidJavadoc, getSeverityString(InvalidJavadoc));
		optionsMap.put(OPTION_ReportInvalidJavadocTagsVisibility, getVisibilityString(this.reportInvalidJavadocTagsVisibility));
		optionsMap.put(OPTION_ReportInvalidJavadocTags, this.reportInvalidJavadocTags ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportInvalidJavadocTagsDeprecatedRef, this.reportInvalidJavadocTagsDeprecatedRef ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportInvalidJavadocTagsNotVisibleRef, this.reportInvalidJavadocTagsNotVisibleRef ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportMissingJavadocTags, getSeverityString(MissingJavadocTags));
		optionsMap.put(OPTION_ReportMissingJavadocTagsVisibility, getVisibilityString(this.reportMissingJavadocTagsVisibility));
		optionsMap.put(OPTION_ReportMissingJavadocTagsOverriding, this.reportMissingJavadocTagsOverriding ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportMissingJavadocComments, getSeverityString(MissingJavadocComments));
		optionsMap.put(OPTION_ReportMissingJavadocCommentsVisibility, getVisibilityString(this.reportMissingJavadocCommentsVisibility));
		optionsMap.put(OPTION_ReportMissingJavadocCommentsOverriding, this.reportMissingJavadocCommentsOverriding ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportFinallyBlockNotCompletingNormally, getSeverityString(FinallyBlockNotCompleting));
		optionsMap.put(OPTION_ReportUnusedDeclaredThrownException, getSeverityString(UnusedDeclaredThrownException));
		optionsMap.put(OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding, this.reportUnusedDeclaredThrownExceptionWhenOverriding ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportUnqualifiedFieldAccess, getSeverityString(UnqualifiedFieldAccess));
		optionsMap.put(OPTION_ReportUncheckedTypeOperation, getSeverityString(UncheckedTypeOperation));
		optionsMap.put(OPTION_ReportRawTypeReference, getSeverityString(RawTypeReference));
		optionsMap.put(OPTION_ReportFinalParameterBound, getSeverityString(FinalParameterBound));
		optionsMap.put(OPTION_ReportMissingSerialVersion, getSeverityString(MissingSerialVersion));
		optionsMap.put(OPTION_ReportForbiddenReference, getSeverityString(ForbiddenReference));
		optionsMap.put(OPTION_ReportDiscouragedReference, getSeverityString(DiscouragedReference));
		optionsMap.put(OPTION_ReportVarargsArgumentNeedCast, getSeverityString(VarargsArgumentNeedCast)); 
		optionsMap.put(OPTION_ReportMissingOverrideAnnotation, getSeverityString(MissingOverrideAnnotation));
		optionsMap.put(OPTION_ReportMissingDeprecatedAnnotation, getSeverityString(MissingDeprecatedAnnotation));
		optionsMap.put(OPTION_ReportIncompleteEnumSwitch, getSeverityString(IncompleteEnumSwitch));
		optionsMap.put(OPTION_ReportUnusedLabel, getSeverityString(UnusedLabel));
		optionsMap.put(OPTION_Compliance, versionFromJdkLevel(this.complianceLevel)); 
		optionsMap.put(OPTION_Source, versionFromJdkLevel(this.sourceLevel)); 
		optionsMap.put(OPTION_TargetPlatform, versionFromJdkLevel(this.targetJDK)); 
		optionsMap.put(OPTION_FatalOptionalError, this.treatOptionalErrorAsFatal ? ENABLED : DISABLED); 
		
		if (this.defaultEncoding != null) {
			optionsMap.put(OPTION_Encoding, this.defaultEncoding); 
		}
		optionsMap.put(OPTION_TaskTags, this.taskTags == null ? "" : new String(CharOperation.concatWith(this.taskTags,','))); //$NON-NLS-1$
		optionsMap.put(OPTION_TaskPriorities, this.taskPriorites == null ? "" : new String(CharOperation.concatWith(this.taskPriorites,','))); //$NON-NLS-1$
		optionsMap.put(OPTION_TaskCaseSensitive, this.isTaskCaseSensitive ? ENABLED : DISABLED);
		optionsMap.put(OPTION_ReportUnusedParameterWhenImplementingAbstract, this.reportUnusedParameterWhenImplementingAbstract ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportUnusedParameterWhenOverridingConcrete, this.reportUnusedParameterWhenOverridingConcrete ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportSpecialParameterHidingField, this.reportSpecialParameterHidingField ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_MaxProblemPerUnit, String.valueOf(this.maxProblemsPerUnit));
		optionsMap.put(OPTION_InlineJsr, this.inlineJsrBytecode ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportNullReference, getSeverityString(NullReference));
		optionsMap.put(OPTION_SuppressWarnings, this.suppressWarnings ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportUnhandledWarningToken, getSeverityString(UnhandledWarningToken));
		optionsMap.put(OPTION_ReportParameterAssignment, getSeverityString(ParameterAssignment));
		return optionsMap;		
	}
	
	public int getSeverity(long irritant) {
		if((this.errorThreshold & irritant) != 0)
			return ProblemSeverities.Error | ProblemSeverities.Optional;
		if((this.warningThreshold & irritant) != 0)
			return ProblemSeverities.Warning | ProblemSeverities.Optional;
		return ProblemSeverities.Ignore;
	}

	public String getSeverityString(long irritant) {
		if((this.warningThreshold & irritant) != 0)
			return WARNING;
		if((this.errorThreshold & irritant) != 0)
			return ERROR;
		return IGNORE;
	}
	
	public String getVisibilityString(int level) {
		switch (level) {
			case ClassFileConstants.AccPublic:
				return PUBLIC;
			case ClassFileConstants.AccProtected:
				return PROTECTED;
			case ClassFileConstants.AccPrivate:
				return PRIVATE;
			default:
				return DEFAULT;
		}
	}
	
	public void set(Map optionsMap) {

		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_LocalVariableAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= ClassFileConstants.ATTR_VARS;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~ClassFileConstants.ATTR_VARS;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LineNumberAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= ClassFileConstants.ATTR_LINES;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~ClassFileConstants.ATTR_LINES;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_SourceFileAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= ClassFileConstants.ATTR_SOURCE;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~ClassFileConstants.ATTR_SOURCE;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_PreserveUnusedLocal)) != null) {
			if (PRESERVE.equals(optionValue)) {
				this.preserveAllLocalVariables = true;
			} else if (OPTIMIZE_OUT.equals(optionValue)) {
				this.preserveAllLocalVariables = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportDeprecationInDeprecatedCode)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportDeprecationInsideDeprecatedCode = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportDeprecationInsideDeprecatedCode = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportDeprecationWhenOverridingDeprecatedMethod)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportDeprecationWhenOverridingDeprecatedMethod = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportDeprecationWhenOverridingDeprecatedMethod = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedDeclaredThrownExceptionWhenOverriding)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportUnusedDeclaredThrownExceptionWhenOverriding = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportUnusedDeclaredThrownExceptionWhenOverriding = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_Compliance)) != null) {
			long level = versionToJdkLevel(optionValue);
			if (level != 0) this.complianceLevel = level;
		}
		if ((optionValue = optionsMap.get(OPTION_Source)) != null) {
			long level = versionToJdkLevel(optionValue);
			if (level != 0) this.sourceLevel = level;
		}
		if ((optionValue = optionsMap.get(OPTION_TargetPlatform)) != null) {
			long level = versionToJdkLevel(optionValue);
			if (level != 0) this.targetJDK = level;
			if (this.targetJDK >= ClassFileConstants.JDK1_5) this.inlineJsrBytecode = true; // forced in 1.5 mode
		}
		if ((optionValue = optionsMap.get(OPTION_Encoding)) != null) {
			if (optionValue instanceof String) {
				this.defaultEncoding = null;
				String stringValue = (String) optionValue;
				if (stringValue.length() > 0){
					try { 
						new InputStreamReader(new ByteArrayInputStream(new byte[0]), stringValue);
						this.defaultEncoding = stringValue;
					} catch(UnsupportedEncodingException e){
						// ignore unsupported encoding
					}
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_PrivateConstructorAccess)) != null) {
			long level = versionToJdkLevel(optionValue);
			if (level >= ClassFileConstants.JDK1_3) this.isPrivateConstructorAccessChangingVisibility = true;
		}
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedParameterWhenImplementingAbstract)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportUnusedParameterWhenImplementingAbstract = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportUnusedParameterWhenImplementingAbstract = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedParameterWhenOverridingConcrete)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportUnusedParameterWhenOverridingConcrete = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportUnusedParameterWhenOverridingConcrete = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportSpecialParameterHidingField)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportSpecialParameterHidingField = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportSpecialParameterHidingField = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_MaxProblemPerUnit)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				try {
					int val = Integer.parseInt(stringValue);
					if (val >= 0) this.maxProblemsPerUnit = val;
				} catch(NumberFormatException e){
					// ignore ill-formatted limit
				}				
			}
		}
		if ((optionValue = optionsMap.get(OPTION_TaskTags)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() == 0) {
					this.taskTags = null;
				} else {
					this.taskTags = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_TaskPriorities)) != null) {
			if (optionValue instanceof String) {
				String stringValue = (String) optionValue;
				if (stringValue.length() == 0) {
					this.taskPriorites = null;
				} else {
					this.taskPriorites = CharOperation.splitAndTrimOn(',', stringValue.toCharArray());
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_TaskCaseSensitive)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.isTaskCaseSensitive = true;
			} else if (DISABLED.equals(optionValue)) {
				this.isTaskCaseSensitive = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_InlineJsr)) != null) {
			if (this.targetJDK < ClassFileConstants.JDK1_5) { // only optional if target < 1.5 (inlining on from 1.5 on)
				if (ENABLED.equals(optionValue)) {
					this.inlineJsrBytecode = true;
				} else if (DISABLED.equals(optionValue)) {
					this.inlineJsrBytecode = false;
				}
			}
		}
		if ((optionValue = optionsMap.get(OPTION_SuppressWarnings)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.suppressWarnings = true;
			} else if (DISABLED.equals(optionValue)) {
				this.suppressWarnings = false;
			}
		}		
		if ((optionValue = optionsMap.get(OPTION_FatalOptionalError)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.treatOptionalErrorAsFatal = true;
			} else if (DISABLED.equals(optionValue)) {
				this.treatOptionalErrorAsFatal = false;
			}
		}		
		if ((optionValue = optionsMap.get(OPTION_ReportMethodWithConstructorName)) != null) updateSeverity(MethodWithConstructorName, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportOverridingPackageDefaultMethod)) != null) updateSeverity(OverriddenPackageDefaultMethod, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportDeprecation)) != null) updateSeverity(UsingDeprecatedAPI, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportHiddenCatchBlock)) != null) updateSeverity(MaskedCatchBlock, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedLocal)) != null) updateSeverity(UnusedLocalVariable, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedParameter)) != null) updateSeverity(UnusedArgument, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedImport)) != null) updateSeverity(UnusedImport, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedPrivateMember)) != null) updateSeverity(UnusedPrivateMember, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedDeclaredThrownException)) != null) updateSeverity(UnusedDeclaredThrownException, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNoImplicitStringConversion)) != null) updateSeverity(NoImplicitStringConversion, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportSyntheticAccessEmulation)) != null) updateSeverity(AccessEmulation, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportLocalVariableHiding)) != null) updateSeverity(LocalVariableHiding, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportFieldHiding)) != null) updateSeverity(FieldHiding, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportTypeParameterHiding)) != null) updateSeverity(TypeParameterHiding, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportPossibleAccidentalBooleanAssignment)) != null) updateSeverity(AccidentalBooleanAssign, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportEmptyStatement)) != null) updateSeverity(EmptyStatement, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNonExternalizedStringLiteral)) != null) updateSeverity(NonExternalizedString, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportAssertIdentifier)) != null) updateSeverity(AssertUsedAsAnIdentifier, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportEnumIdentifier)) != null) updateSeverity(EnumUsedAsAnIdentifier, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNonStaticAccessToStatic)) != null) updateSeverity(NonStaticAccessToStatic, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportIndirectStaticAccess)) != null) updateSeverity(IndirectStaticAccess, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportIncompatibleNonInheritedInterfaceMethod)) != null) updateSeverity(IncompatibleNonInheritedInterfaceMethod, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUndocumentedEmptyBlock)) != null) updateSeverity(UndocumentedEmptyBlock, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnnecessaryTypeCheck)) != null) updateSeverity(UnnecessaryTypeCheck, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnnecessaryElse)) != null) updateSeverity(UnnecessaryElse, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportFinallyBlockNotCompletingNormally)) != null) updateSeverity(FinallyBlockNotCompleting, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnqualifiedFieldAccess)) != null) updateSeverity(UnqualifiedFieldAccess, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNoEffectAssignment)) != null) updateSeverity(NoEffectAssignment, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUncheckedTypeOperation)) != null) updateSeverity(UncheckedTypeOperation, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportRawTypeReference)) != null) updateSeverity(RawTypeReference, optionValue);		
		if ((optionValue = optionsMap.get(OPTION_ReportFinalParameterBound)) != null) updateSeverity(FinalParameterBound, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportMissingSerialVersion)) != null) updateSeverity(MissingSerialVersion, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportForbiddenReference)) != null) updateSeverity(ForbiddenReference, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportDiscouragedReference)) != null) updateSeverity(DiscouragedReference, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportVarargsArgumentNeedCast)) != null) updateSeverity(VarargsArgumentNeedCast, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportNullReference)) != null) updateSeverity(NullReference, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportAutoboxing)) != null) updateSeverity(AutoBoxing, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportAnnotationSuperInterface)) != null) updateSeverity(AnnotationSuperInterface, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportMissingOverrideAnnotation)) != null) updateSeverity(MissingOverrideAnnotation, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportMissingDeprecatedAnnotation)) != null) updateSeverity(MissingDeprecatedAnnotation, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportIncompleteEnumSwitch)) != null) updateSeverity(IncompleteEnumSwitch, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnhandledWarningToken)) != null) updateSeverity(UnhandledWarningToken, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportUnusedLabel)) != null) updateSeverity(UnusedLabel, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportParameterAssignment)) != null) updateSeverity(ParameterAssignment, optionValue);

		// Javadoc options
		if ((optionValue = optionsMap.get(OPTION_DocCommentSupport)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.docCommentSupport = true;
			} else if (DISABLED.equals(optionValue)) {
				this.docCommentSupport = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadoc)) != null) {
			updateSeverity(InvalidJavadoc, optionValue);
		}
		if ( (optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTagsVisibility)) != null) {
			if (PUBLIC.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = ClassFileConstants.AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = ClassFileConstants.AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = ClassFileConstants.AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = ClassFileConstants.AccPrivate;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTags)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportInvalidJavadocTags = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportInvalidJavadocTags = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTagsDeprecatedRef)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportInvalidJavadocTagsDeprecatedRef = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportInvalidJavadocTagsDeprecatedRef = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTagsNotVisibleRef)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportInvalidJavadocTagsNotVisibleRef = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportInvalidJavadocTagsNotVisibleRef = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocTags)) != null) {
			updateSeverity(MissingJavadocTags, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocTagsVisibility)) != null) {
			if (PUBLIC.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = ClassFileConstants.AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = ClassFileConstants.AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = ClassFileConstants.AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = ClassFileConstants.AccPrivate;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocTagsOverriding)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportMissingJavadocTagsOverriding = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportMissingJavadocTagsOverriding = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocComments)) != null) {
			updateSeverity(MissingJavadocComments, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocCommentsVisibility)) != null) {
			if (PUBLIC.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = ClassFileConstants.AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = ClassFileConstants.AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = ClassFileConstants.AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = ClassFileConstants.AccPrivate;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocCommentsOverriding)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportMissingJavadocCommentsOverriding = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportMissingJavadocCommentsOverriding = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_StatementsRecovery)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.performStatementsRecovery = true;
			} else if (DISABLED.equals(optionValue)) {
				this.performStatementsRecovery = false;
			}
		}
	}

	public String toString() {
	
		StringBuffer buf = new StringBuffer("CompilerOptions:"); //$NON-NLS-1$
		buf.append("\n\t- local variables debug attributes: ").append((this.produceDebugAttributes & ClassFileConstants.ATTR_VARS) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- line number debug attributes: ").append((this.produceDebugAttributes & ClassFileConstants.ATTR_LINES) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- source debug attributes: ").append((this.produceDebugAttributes & ClassFileConstants.ATTR_SOURCE) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- preserve all local variables: ").append(this.preserveAllLocalVariables ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- method with constructor name: ").append(getSeverityString(MethodWithConstructorName)); //$NON-NLS-1$
		buf.append("\n\t- overridden package default method: ").append(getSeverityString(OverriddenPackageDefaultMethod)); //$NON-NLS-1$
		buf.append("\n\t- deprecation: ").append(getSeverityString(UsingDeprecatedAPI)); //$NON-NLS-1$
		buf.append("\n\t- masked catch block: ").append(getSeverityString(MaskedCatchBlock)); //$NON-NLS-1$
		buf.append("\n\t- unused local variable: ").append(getSeverityString(UnusedLocalVariable)); //$NON-NLS-1$
		buf.append("\n\t- unused parameter: ").append(getSeverityString(UnusedArgument)); //$NON-NLS-1$
		buf.append("\n\t- unused import: ").append(getSeverityString(UnusedImport)); //$NON-NLS-1$
		buf.append("\n\t- synthetic access emulation: ").append(getSeverityString(AccessEmulation)); //$NON-NLS-1$
		buf.append("\n\t- assignment with no effect: ").append(getSeverityString(NoEffectAssignment)); //$NON-NLS-1$
		buf.append("\n\t- non externalized string: ").append(getSeverityString(NonExternalizedString)); //$NON-NLS-1$
		buf.append("\n\t- static access receiver: ").append(getSeverityString(NonStaticAccessToStatic)); //$NON-NLS-1$
		buf.append("\n\t- indirect static access: ").append(getSeverityString(IndirectStaticAccess)); //$NON-NLS-1$
		buf.append("\n\t- incompatible non inherited interface method: ").append(getSeverityString(IncompatibleNonInheritedInterfaceMethod)); //$NON-NLS-1$
		buf.append("\n\t- unused private member: ").append(getSeverityString(UnusedPrivateMember)); //$NON-NLS-1$
		buf.append("\n\t- local variable hiding another variable: ").append(getSeverityString(LocalVariableHiding)); //$NON-NLS-1$
		buf.append("\n\t- field hiding another variable: ").append(getSeverityString(FieldHiding)); //$NON-NLS-1$
		buf.append("\n\t- type parameter hiding another type: ").append(getSeverityString(TypeParameterHiding)); //$NON-NLS-1$
		buf.append("\n\t- possible accidental boolean assignment: ").append(getSeverityString(AccidentalBooleanAssign)); //$NON-NLS-1$
		buf.append("\n\t- superfluous semicolon: ").append(getSeverityString(EmptyStatement)); //$NON-NLS-1$
		buf.append("\n\t- uncommented empty block: ").append(getSeverityString(UndocumentedEmptyBlock)); //$NON-NLS-1$
		buf.append("\n\t- unnecessary type check: ").append(getSeverityString(UnnecessaryTypeCheck)); //$NON-NLS-1$
		buf.append("\n\t- javadoc comment support: ").append(this.docCommentSupport ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t\t+ invalid javadoc: ").append(getSeverityString(InvalidJavadoc)); //$NON-NLS-1$
		buf.append("\n\t\t+ report invalid javadoc tags: ").append(this.reportInvalidJavadocTags ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t\t\t* deprecated references: ").append(this.reportInvalidJavadocTagsDeprecatedRef ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t\t\t* not visible references: ").append(this.reportInvalidJavadocTagsNotVisibleRef ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t\t+ visibility level to report invalid javadoc tags: ").append(getVisibilityString(this.reportInvalidJavadocTagsVisibility)); //$NON-NLS-1$
		buf.append("\n\t\t+ missing javadoc tags: ").append(getSeverityString(MissingJavadocTags)); //$NON-NLS-1$
		buf.append("\n\t\t+ visibility level to report missing javadoc tags: ").append(getVisibilityString(this.reportMissingJavadocTagsVisibility)); //$NON-NLS-1$
		buf.append("\n\t\t+ report missing javadoc tags in overriding methods: ").append(this.reportMissingJavadocTagsOverriding ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t\t+ missing javadoc comments: ").append(getSeverityString(MissingJavadocComments)); //$NON-NLS-1$
		buf.append("\n\t\t+ visibility level to report missing javadoc comments: ").append(getVisibilityString(this.reportMissingJavadocCommentsVisibility)); //$NON-NLS-1$
		buf.append("\n\t\t+ report missing javadoc comments in overriding methods: ").append(this.reportMissingJavadocCommentsOverriding ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- finally block not completing normally: ").append(getSeverityString(FinallyBlockNotCompleting)); //$NON-NLS-1$
		buf.append("\n\t- unused declared thrown exception: ").append(getSeverityString(UnusedDeclaredThrownException)); //$NON-NLS-1$
		buf.append("\n\t- unused declared thrown exception when overriding: ").append(this.reportUnusedDeclaredThrownExceptionWhenOverriding ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- unnecessary else: ").append(getSeverityString(UnnecessaryElse)); //$NON-NLS-1$
		buf.append("\n\t- JDK compliance level: "+ versionFromJdkLevel(this.complianceLevel)); //$NON-NLS-1$
		buf.append("\n\t- JDK source level: "+ versionFromJdkLevel(this.sourceLevel)); //$NON-NLS-1$
		buf.append("\n\t- JDK target level: "+ versionFromJdkLevel(this.targetJDK)); //$NON-NLS-1$
		buf.append("\n\t- private constructor access: ").append(this.isPrivateConstructorAccessChangingVisibility ? "extra argument" : "make default access"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- verbose : ").append(this.verbose ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- produce reference info : ").append(this.produceReferenceInfo ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- parse literal expressions as constants : ").append(this.parseLiteralExpressionsAsConstants ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- encoding : ").append(this.defaultEncoding == null ? "<default>" : this.defaultEncoding); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n\t- task tags: ").append(this.taskTags == null ? "" : new String(CharOperation.concatWith(this.taskTags,',')));  //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n\t- task priorities : ").append(this.taskPriorites == null ? "" : new String(CharOperation.concatWith(this.taskPriorites,','))); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n\t- report deprecation inside deprecated code : ").append(this.reportDeprecationInsideDeprecatedCode ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- report deprecation when overriding deprecated method : ").append(this.reportDeprecationWhenOverridingDeprecatedMethod ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- report unused parameter when implementing abstract method : ").append(this.reportUnusedParameterWhenImplementingAbstract ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- report unused parameter when overriding concrete method : ").append(this.reportUnusedParameterWhenOverridingConcrete ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- report constructor/setter parameter hiding existing field : ").append(this.reportSpecialParameterHidingField ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- inline JSR bytecode : ").append(this.inlineJsrBytecode ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- unsafe type operation: ").append(getSeverityString(UncheckedTypeOperation)); //$NON-NLS-1$
		buf.append("\n\t- unsafe raw type: ").append(getSeverityString(RawTypeReference)); //$NON-NLS-1$
		buf.append("\n\t- final bound for type parameter: ").append(getSeverityString(FinalParameterBound)); //$NON-NLS-1$
		buf.append("\n\t- missing serialVersionUID: ").append(getSeverityString(MissingSerialVersion)); //$NON-NLS-1$
		buf.append("\n\t- varargs argument need cast: ").append(getSeverityString(VarargsArgumentNeedCast)); //$NON-NLS-1$
		buf.append("\n\t- forbidden reference to type with access restriction: ").append(getSeverityString(ForbiddenReference)); //$NON-NLS-1$
		buf.append("\n\t- discouraged reference to type with access restriction: ").append(getSeverityString(DiscouragedReference)); //$NON-NLS-1$
		buf.append("\n\t- null reference: ").append(getSeverityString(NullReference)); //$NON-NLS-1$
		buf.append("\n\t- autoboxing: ").append(getSeverityString(AutoBoxing)); //$NON-NLS-1$
		buf.append("\n\t- annotation super interface: ").append(getSeverityString(AnnotationSuperInterface)); //$NON-NLS-1$
		buf.append("\n\t- missing @Override annotation: ").append(getSeverityString(MissingOverrideAnnotation)); //$NON-NLS-1$		
		buf.append("\n\t- missing @Deprecated annotation: ").append(getSeverityString(MissingDeprecatedAnnotation)); //$NON-NLS-1$		
		buf.append("\n\t- incomplete enum switch: ").append(getSeverityString(IncompleteEnumSwitch)); //$NON-NLS-1$
		buf.append("\n\t- suppress warnings: ").append(this.suppressWarnings ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- unhandled warning token: ").append(getSeverityString(UnhandledWarningToken)); //$NON-NLS-1$
		buf.append("\n\t- unused label: ").append(getSeverityString(UnusedLabel)); //$NON-NLS-1$
		buf.append("\n\t- treat optional error as fatal: ").append(this.treatOptionalErrorAsFatal ? ENABLED : DISABLED); //$NON-NLS-1$
		buf.append("\n\t- parameter assignment: ").append(getSeverityString(ParameterAssignment)); //$NON-NLS-1$
		return buf.toString();
	}

	void updateSeverity(long irritant, Object severityString) {
		if (ERROR.equals(severityString)) {
			this.errorThreshold |= irritant;
			this.warningThreshold &= ~irritant;
		} else if (WARNING.equals(severityString)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold |= irritant;
		} else if (IGNORE.equals(severityString)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold &= ~irritant;
		}
	}				
	public static long versionToJdkLevel(Object versionID) {
		if (VERSION_1_1.equals(versionID)) {
			return ClassFileConstants.JDK1_1;
		} else if (VERSION_1_2.equals(versionID)) {
			return ClassFileConstants.JDK1_2;
		} else if (VERSION_1_3.equals(versionID)) {
			return ClassFileConstants.JDK1_3;
		} else if (VERSION_1_4.equals(versionID)) {
			return ClassFileConstants.JDK1_4;
		} else if (VERSION_1_5.equals(versionID)) {
			return ClassFileConstants.JDK1_5;
		} else if (VERSION_1_6.equals(versionID)) {
			return ClassFileConstants.JDK1_6;
		}
		return 0; // unknown
	}

	public static String versionFromJdkLevel(long jdkLevel) {
		if (jdkLevel == ClassFileConstants.JDK1_1) {
			return VERSION_1_1;
		} else if (jdkLevel == ClassFileConstants.JDK1_2) {
			return VERSION_1_2;
		} else if (jdkLevel == ClassFileConstants.JDK1_3) {
			return VERSION_1_3;
		} else if (jdkLevel == ClassFileConstants.JDK1_4) {
			return VERSION_1_4;
		} else if (jdkLevel == ClassFileConstants.JDK1_5) {
			return VERSION_1_5;
		} else if (jdkLevel == ClassFileConstants.JDK1_6) {
			return VERSION_1_6;
		}
		return ""; // unknown version //$NON-NLS-1$
	}
	
	/**
	 * Return all warning option names for use as keys in compiler options maps.
	 * @return all warning option names
	 * TODO (maxime) revise for ensuring completeness
	 */
	public static String[] warningOptionNames() {
		String[] result = {
			OPTION_ReportAnnotationSuperInterface,
			OPTION_ReportAssertIdentifier,
			OPTION_ReportAutoboxing,
			OPTION_ReportDeprecation,
			OPTION_ReportDiscouragedReference,
			OPTION_ReportEmptyStatement,
			OPTION_ReportEnumIdentifier,
			OPTION_ReportFieldHiding,
			OPTION_ReportFinalParameterBound,
			OPTION_ReportFinallyBlockNotCompletingNormally,
			OPTION_ReportForbiddenReference,
			OPTION_ReportHiddenCatchBlock,
			OPTION_ReportIncompatibleNonInheritedInterfaceMethod,
			OPTION_ReportIncompleteEnumSwitch,
			OPTION_ReportIndirectStaticAccess,
			OPTION_ReportInvalidJavadoc,
			OPTION_ReportLocalVariableHiding,
			OPTION_ReportMethodWithConstructorName,
			OPTION_ReportMissingDeprecatedAnnotation,
			OPTION_ReportMissingJavadocComments,
			OPTION_ReportMissingJavadocTags,
			OPTION_ReportMissingOverrideAnnotation,
			OPTION_ReportMissingSerialVersion,
			OPTION_ReportNoEffectAssignment,
			OPTION_ReportNoImplicitStringConversion,
			OPTION_ReportNonExternalizedStringLiteral,
			OPTION_ReportNonStaticAccessToStatic,
			OPTION_ReportNullReference,
			OPTION_ReportOverridingPackageDefaultMethod,
			OPTION_ReportParameterAssignment,
			OPTION_ReportPossibleAccidentalBooleanAssignment,
			OPTION_ReportSyntheticAccessEmulation,
			OPTION_ReportTypeParameterHiding,
			OPTION_ReportUncheckedTypeOperation,
			OPTION_ReportUndocumentedEmptyBlock,
			OPTION_ReportUnnecessaryElse,
			OPTION_ReportUnnecessaryTypeCheck,
			OPTION_ReportUnqualifiedFieldAccess,
			OPTION_ReportUnusedDeclaredThrownException,
			OPTION_ReportUnusedImport,
			OPTION_ReportUnusedLocal,
			OPTION_ReportUnusedParameter,
			OPTION_ReportUnusedPrivateMember,
			OPTION_ReportVarargsArgumentNeedCast,
			OPTION_ReportUnhandledWarningToken,
		};
		return result;
	}
	
	public static String warningTokenFromIrritant(long irritant) {
		int irritantInt = (int) irritant;
		if (irritantInt == irritant) {
			switch (irritantInt) {
				case (int) (InvalidJavadoc | UsingDeprecatedAPI) :
				case (int) UsingDeprecatedAPI :
					return "deprecation"; //$NON-NLS-1$
				case (int) FinallyBlockNotCompleting :
					return "finally"; //$NON-NLS-1$
				case (int) FieldHiding :
				case (int) LocalVariableHiding :
				case (int) MaskedCatchBlock :
					return "hiding"; //$NON-NLS-1$
				case (int) NonExternalizedString :
					return "nls"; //$NON-NLS-1$
				case (int) UnusedLocalVariable :
				case (int) UnusedArgument :
				case (int) UnusedPrivateMember:
				case (int) UnusedDeclaredThrownException:
					return "unused"; //$NON-NLS-1$
				case (int) IndirectStaticAccess :
				case (int) NonStaticAccessToStatic :
					return "static-access"; //$NON-NLS-1$
				case (int) AccessEmulation :
					return "synthetic-access"; //$NON-NLS-1$
				case (int) UnqualifiedFieldAccess :
					return "unqualified-field-access"; //$NON-NLS-1$
				case (int) UncheckedTypeOperation :
					return "unchecked"; //$NON-NLS-1$
			}
		} else {
			irritantInt = (int)(irritant >>> 32);
			switch (irritantInt) {
				case (int)(MissingSerialVersion >>> 32) :
					return "serial"; //$NON-NLS-1$
				case (int)(AutoBoxing >>> 32) :
					return "boxing"; //$NON-NLS-1$
				case (int)(TypeParameterHiding >>> 32) :
					return "hiding"; //$NON-NLS-1$
				case (int)(IncompleteEnumSwitch >>> 32) :
					return "incomplete-switch"; //$NON-NLS-1$
				case (int)(MissingDeprecatedAnnotation >>> 32) :
					return "dep-ann"; //$NON-NLS-1$
				case (int)(RawTypeReference >>> 32):
					return "unchecked"; //$NON-NLS-1$
				case (int) UnusedLabel:
					return "unused"; //$NON-NLS-1$
			}
		}
		return null;
	}
	public static long warningTokenToIrritant(String warningToken) {
		if (warningToken == null || warningToken.length() == 0) return 0;
		switch (warningToken.charAt(0)) {
			case 'a' :
				if ("all".equals(warningToken)) //$NON-NLS-1$
					return 0xFFFFFFFFFFFFFFFFl; // suppress all warnings
				break;
			case 'b' :
				if ("boxing".equals(warningToken)) //$NON-NLS-1$
					return AutoBoxing;
				break;
			case 'd' :
				if ("deprecation".equals(warningToken)) //$NON-NLS-1$
					return UsingDeprecatedAPI;
				if ("dep-ann".equals(warningToken)) //$NON-NLS-1$
					return MissingDeprecatedAnnotation;
				break;
			case 'f' :
				if ("finally".equals(warningToken)) //$NON-NLS-1$
					return FinallyBlockNotCompleting;
				break;
			case 'h' :
				if ("hiding".equals(warningToken)) //$NON-NLS-1$
					return FieldHiding | LocalVariableHiding | MaskedCatchBlock | TypeParameterHiding;
			case 'i' :
				if ("incomplete-switch".equals(warningToken)) //$NON-NLS-1$
					return IncompleteEnumSwitch;
				break;
			case 'n' :
				if ("nls".equals(warningToken)) //$NON-NLS-1$
					return NonExternalizedString;
				if ("null".equals(warningToken)) //$NON-NLS-1$
					return NullReference;
				break;
			case 's' :
				if ("serial".equals(warningToken)) //$NON-NLS-1$
					return MissingSerialVersion;
				if ("static-access".equals(warningToken)) //$NON-NLS-1$
					return IndirectStaticAccess | NonStaticAccessToStatic;
				if ("synthetic-access".equals(warningToken)) //$NON-NLS-1$
					return AccessEmulation;
				break;
			case 'u' :
				if ("unused".equals(warningToken)) //$NON-NLS-1$
					return UnusedLocalVariable | UnusedArgument | UnusedPrivateMember | UnusedDeclaredThrownException | UnusedLabel;
				if ("unchecked".equals(warningToken)) //$NON-NLS-1$
					return UncheckedTypeOperation | RawTypeReference;
				if ("unqualified-field-access".equals(warningToken)) //$NON-NLS-1$
					return UnqualifiedFieldAccess;
				break;
		}
		return 0;
	}
}

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
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

public class CompilerOptions implements ProblemReasons, ProblemSeverities, ClassFileConstants {
	
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
	public static final String OPTION_ReportUnsafeTypeOperation = "org.eclipse.jdt.core.compiler.problem.unsafeTypeOperation"; //$NON-NLS-1$
	public static final String OPTION_ReportFinalParameterBound = "org.eclipse.jdt.core.compiler.problem.finalParameterBound"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingSerialVersion = "org.eclipse.jdt.core.compiler.problem.missingSerialVersion"; //$NON-NLS-1$
	public static final String OPTION_ReportForbiddenReference =  "org.eclipse.jdt.core.compiler.problem.forbiddenReference"; //$NON-NLS-1$
	public static final String OPTION_Source = "org.eclipse.jdt.core.compiler.source"; //$NON-NLS-1$
	public static final String OPTION_TargetPlatform = "org.eclipse.jdt.core.compiler.codegen.targetPlatform"; //$NON-NLS-1$
	public static final String OPTION_Compliance = "org.eclipse.jdt.core.compiler.compliance"; //$NON-NLS-1$
	public static final String OPTION_Encoding = "org.eclipse.jdt.core.encoding"; //$NON-NLS-1$
	public static final String OPTION_MaxProblemPerUnit = "org.eclipse.jdt.core.compiler.maxProblemPerUnit"; //$NON-NLS-1$
	public static final String OPTION_TaskTags = "org.eclipse.jdt.core.compiler.taskTags"; //$NON-NLS-1$
	public static final String OPTION_TaskPriorities = "org.eclipse.jdt.core.compiler.taskPriorities"; //$NON-NLS-1$
	public static final String OPTION_TaskCaseSensitive = "org.eclipse.jdt.core.compiler.taskCaseSensitive"; //$NON-NLS-1$
	public static final String OPTION_InlineJsr = "org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode"; //$NON-NLS-1$
	
	// Backward compatibility
	public static final String OPTION_ReportInvalidAnnotation = "org.eclipse.jdt.core.compiler.problem.invalidAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingAnnotation = "org.eclipse.jdt.core.compiler.problem.missingAnnotation"; //$NON-NLS-1$
	public static final String OPTION_ReportMissingJavadoc = "org.eclipse.jdt.core.compiler.problem.missingJavadoc"; //$NON-NLS-1$

	/* should surface ??? */
	public static final String OPTION_PrivateConstructorAccess = "org.eclipse.jdt.core.compiler.codegen.constructorAccessEmulation"; //$NON-NLS-1$

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
	public static final long UnsafeTypeOperation = ASTNode.Bit31;
	public static final long FinalParameterBound = ASTNode.Bit32L;
	public static final long MissingSerialVersion = ASTNode.Bit33L;
	public static final long EnumUsedAsAnIdentifier = ASTNode.Bit34L;	
	public static final long ForbiddenReference = ASTNode.Bit35L;

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
		| UnsafeTypeOperation
		| MissingSerialVersion;

	// Debug attributes
	public static final int Source = 1; // SourceFileAttribute
	public static final int Lines = 2; // LineNumberAttribute
	public static final int Vars = 4; // LocalVariableTableAttribute

	// By default only lines and source attributes are generated.
	public int produceDebugAttributes = Lines | Source;

	public long complianceLevel = JDK1_4; // by default be compliant with 1.4
	public long sourceLevel = JDK1_3; //1.3 source behavior by default
	public long targetJDK = JDK1_2; // default generates for JVM1.2

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

	// check javadoc comments
	public int reportInvalidJavadocTagsVisibility = AccPrivate; 
	public boolean reportInvalidJavadocTags = true; 

	// check missing javadoc tags
	public int reportMissingJavadocTagsVisibility = AccPrivate; 
	public boolean reportMissingJavadocTagsOverriding = true;

	// check missing javadoc comments
	public int reportMissingJavadocCommentsVisibility = AccPublic; 
	public boolean reportMissingJavadocCommentsOverriding = true; 
	
	// JSR bytecode inlining
	public boolean inlineJsrBytecode = false;
	
	// javadoc comment support
	public boolean docCommentSupport = false;
	
	
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
		optionsMap.put(OPTION_LocalVariableAttribute, (this.produceDebugAttributes & Vars) != 0 ? GENERATE : DO_NOT_GENERATE); 
		optionsMap.put(OPTION_LineNumberAttribute, (this.produceDebugAttributes & Lines) != 0 ? GENERATE : DO_NOT_GENERATE);
		optionsMap.put(OPTION_SourceFileAttribute, (this.produceDebugAttributes & Source) != 0 ? GENERATE : DO_NOT_GENERATE);
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
		optionsMap.put(OPTION_ReportPossibleAccidentalBooleanAssignment, getSeverityString(AccidentalBooleanAssign)); 
		optionsMap.put(OPTION_ReportEmptyStatement, getSeverityString(EmptyStatement)); 
		optionsMap.put(OPTION_ReportAssertIdentifier, getSeverityString(AssertUsedAsAnIdentifier)); 
		optionsMap.put(OPTION_ReportEnumIdentifier, getSeverityString(EnumUsedAsAnIdentifier)); 
		optionsMap.put(OPTION_ReportUndocumentedEmptyBlock, getSeverityString(UndocumentedEmptyBlock)); 
		optionsMap.put(OPTION_ReportUnnecessaryTypeCheck, getSeverityString(UnnecessaryTypeCheck)); 
		optionsMap.put(OPTION_ReportUnnecessaryElse, getSeverityString(UnnecessaryElse)); 
		optionsMap.put(OPTION_ReportInvalidJavadoc, getSeverityString(InvalidJavadoc));
		optionsMap.put(OPTION_ReportInvalidJavadocTagsVisibility, getVisibilityString(this.reportInvalidJavadocTagsVisibility));
		optionsMap.put(OPTION_ReportInvalidJavadocTags, this.reportInvalidJavadocTags? ENABLED : DISABLED);
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
		optionsMap.put(OPTION_ReportUnsafeTypeOperation, getSeverityString(UnsafeTypeOperation));
		optionsMap.put(OPTION_ReportFinalParameterBound, getSeverityString(FinalParameterBound));
		optionsMap.put(OPTION_ReportMissingSerialVersion, getSeverityString(MissingSerialVersion));
		optionsMap.put(OPTION_ReportForbiddenReference, getSeverityString(ForbiddenReference));
		optionsMap.put(OPTION_Compliance, versionFromJdkLevel(this.complianceLevel)); 
		optionsMap.put(OPTION_Source, versionFromJdkLevel(this.sourceLevel)); 
		optionsMap.put(OPTION_TargetPlatform, versionFromJdkLevel(this.targetJDK)); 
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
		return optionsMap;		
	}
	
	public int getSeverity(long irritant) {
		if((this.warningThreshold & irritant) != 0)
			return Warning;
		if((this.errorThreshold & irritant) != 0)
			return Error;
		return Ignore;
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
			case AccPublic:
				return PUBLIC;
			case AccProtected:
				return PROTECTED;
			case AccPrivate:
				return PRIVATE;
			default:
				return DEFAULT;
		}
	}
	
	public void set(Map optionsMap) {

		Object optionValue;
		if ((optionValue = optionsMap.get(OPTION_LocalVariableAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= Vars;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~Vars;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_LineNumberAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= Lines;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~Lines;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_SourceFileAttribute)) != null) {
			if (GENERATE.equals(optionValue)) {
				this.produceDebugAttributes |= Source;
			} else if (DO_NOT_GENERATE.equals(optionValue)) {
				this.produceDebugAttributes &= ~Source;
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
			if (this.targetJDK >= JDK1_5) this.inlineJsrBytecode = true; // forced in 1.5 mode
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
			if (level >= JDK1_3) this.isPrivateConstructorAccessChangingVisibility = true;
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
			if (this.targetJDK < JDK1_5) { // only optional if target < 1.5 (inlining on from 1.5 on)
				if (ENABLED.equals(optionValue)) {
					this.inlineJsrBytecode = true;
				} else if (DISABLED.equals(optionValue)) {
					this.inlineJsrBytecode = false;
				}
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
		if ((optionValue = optionsMap.get(OPTION_ReportUnsafeTypeOperation)) != null) updateSeverity(UnsafeTypeOperation, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportFinalParameterBound)) != null) updateSeverity(FinalParameterBound, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportMissingSerialVersion)) != null) updateSeverity(MissingSerialVersion, optionValue);
		if ((optionValue = optionsMap.get(OPTION_ReportForbiddenReference)) != null) updateSeverity(ForbiddenReference, optionValue);

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
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTagsVisibility)) != null) {
			if (PUBLIC.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportInvalidJavadocTagsVisibility = AccPrivate;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportInvalidJavadocTags)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportInvalidJavadocTags= true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportInvalidJavadocTags = false;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocTags)) != null) {
			updateSeverity(MissingJavadocTags, optionValue);
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocTagsVisibility)) != null) {
			if (PUBLIC.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportMissingJavadocTagsVisibility = AccPrivate;
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
				this.reportMissingJavadocCommentsVisibility = AccPublic;
			} else if (PROTECTED.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = AccProtected;
			} else if (DEFAULT.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = AccDefault;
			} else if (PRIVATE.equals(optionValue)) {
				this.reportMissingJavadocCommentsVisibility = AccPrivate;
			}
		}
		if ((optionValue = optionsMap.get(OPTION_ReportMissingJavadocCommentsOverriding)) != null) {
			if (ENABLED.equals(optionValue)) {
				this.reportMissingJavadocCommentsOverriding = true;
			} else if (DISABLED.equals(optionValue)) {
				this.reportMissingJavadocCommentsOverriding = false;
			}
		}
	}

	public String toString() {
	
		StringBuffer buf = new StringBuffer("CompilerOptions:"); //$NON-NLS-1$
		buf.append("\n\t- local variables debug attributes: ").append((this.produceDebugAttributes & Vars) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- line number debug attributes: ").append((this.produceDebugAttributes & Lines) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t- source debug attributes: ").append((this.produceDebugAttributes & Source) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
		buf.append("\n\t- possible accidental boolean assignment: ").append(getSeverityString(AccidentalBooleanAssign)); //$NON-NLS-1$
		buf.append("\n\t- superfluous semicolon: ").append(getSeverityString(EmptyStatement)); //$NON-NLS-1$
		buf.append("\n\t- uncommented empty block: ").append(getSeverityString(UndocumentedEmptyBlock)); //$NON-NLS-1$
		buf.append("\n\t- unnecessary type check: ").append(getSeverityString(UnnecessaryTypeCheck)); //$NON-NLS-1$
		buf.append("\n\t- javadoc comment support: ").append(this.docCommentSupport ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n\t\t+ invalid javadoc: ").append(getSeverityString(InvalidJavadoc)); //$NON-NLS-1$
		buf.append("\n\t\t+ report invalid javadoc tags: ").append(this.reportInvalidJavadocTags ? ENABLED : DISABLED); //$NON-NLS-1$
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
		buf.append("\n\t- unsafe type operation: ").append(getSeverityString(UnsafeTypeOperation)); //$NON-NLS-1$
		buf.append("\n\t- final bound for type parameter: ").append(getSeverityString(FinalParameterBound)); //$NON-NLS-1$
		buf.append("\n\t- missing serialVersionUID: ").append(getSeverityString(MissingSerialVersion)); //$NON-NLS-1$
		buf.append("\n\t- forbidden reference to non-API type: ").append(getSeverityString(ForbiddenReference)); //$NON-NLS-1$
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
			return JDK1_1;
		} else if (VERSION_1_2.equals(versionID)) {
			return JDK1_2;
		} else if (VERSION_1_3.equals(versionID)) {
			return JDK1_3;
		} else if (VERSION_1_4.equals(versionID)) {
			return JDK1_4;
		} else if (VERSION_1_5.equals(versionID)) {
			return JDK1_5;
		}
		return 0; // unknown
	}

	public static String versionFromJdkLevel(long jdkLevel) {
		if (jdkLevel == JDK1_1) {
			return VERSION_1_1;
		} else if (jdkLevel == JDK1_2) {
			return VERSION_1_2;
		} else if (jdkLevel == JDK1_3) {
			return VERSION_1_3;
		} else if (jdkLevel == JDK1_4) {
			return VERSION_1_4;
		} else if (jdkLevel == JDK1_5) {
			return VERSION_1_5;
		}
		return ""; // unknown version //$NON-NLS-1$
	}
}

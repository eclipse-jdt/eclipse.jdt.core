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
package org.eclipse.jdt.internal.compiler.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.Compiler;
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
	public static final String OPTION_ReportUnreachableCode = "org.eclipse.jdt.core.compiler.problem.unreachableCode"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidImport = "org.eclipse.jdt.core.compiler.problem.invalidImport"; //$NON-NLS-1$
	public static final String OPTION_ReportMethodWithConstructorName = "org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
	public static final String OPTION_ReportOverridingPackageDefaultMethod = "org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecation = "org.eclipse.jdt.core.compiler.problem.deprecation"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecationInDeprecatedCode = "org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode"; //$NON-NLS-1$
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
	public static final String OPTION_ReportNonStaticAccessToStatic = "org.eclipse.jdt.core.compiler.problem.staticAccessReceiver"; //$NON-NLS-1$
	public static final String OPTION_ReportIndirectStaticAccess = "org.eclipse.jdt.core.compiler.problem.indirectStaticAccess"; //$NON-NLS-1$
	public static final String OPTION_ReportSuperfluousSemicolon = "org.eclipse.jdt.core.compiler.problem.superfluousSemicolon"; //$NON-NLS-1$
	public static final String OPTION_ReportUndocumentedEmptyBlock = "org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock"; //$NON-NLS-1$
	public static final String OPTION_ReportUnnecessaryTypeCheck = "org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck"; //$NON-NLS-1$
	public static final String OPTION_Source = "org.eclipse.jdt.core.compiler.source"; //$NON-NLS-1$
	public static final String OPTION_TargetPlatform = "org.eclipse.jdt.core.compiler.codegen.targetPlatform"; //$NON-NLS-1$
	public static final String OPTION_Compliance = "org.eclipse.jdt.core.compiler.compliance"; //$NON-NLS-1$
	public static final String OPTION_Encoding = "org.eclipse.jdt.core.encoding"; //$NON-NLS-1$
	public static final String OPTION_MaxProblemPerUnit = "org.eclipse.jdt.core.compiler.maxProblemPerUnit"; //$NON-NLS-1$
	public static final String OPTION_TaskTags = "org.eclipse.jdt.core.compiler.taskTags"; //$NON-NLS-1$
	public static final String OPTION_TaskPriorities = "org.eclipse.jdt.core.compiler.taskPriorities"; //$NON-NLS-1$

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
	
	/**
	 * Bit mask for configurable problems (error/warning threshold)
	 */
	public static final long UnreachableCode = 0x100L;
	public static final long ImportProblem = 0x400L;
	public static final long MethodWithConstructorName = 0x1000L;
	public static final long OverriddenPackageDefaultMethod = 0x2000L;
	public static final long UsingDeprecatedAPI = 0x4000L;
	public static final long MaskedCatchBlock = 0x8000L;
	public static final long UnusedLocalVariable = 0x10000L;
	public static final long UnusedArgument = 0x20000L;
	public static final long NoImplicitStringConversion = 0x40000L;
	public static final long AccessEmulation = 0x80000L;
	public static final long NonExternalizedString = 0x100000L;
	public static final long AssertUsedAsAnIdentifier = 0x200000L;
	public static final long UnusedImport = 0x400000L;
	public static final long NonStaticAccessToStatic = 0x800000L;
	public static final long Task = 0x1000000L;
	public static final long NoEffectAssignment = 0x2000000L;
	public static final long IncompatibleNonInheritedInterfaceMethod = 0x4000000L;
	public static final long UnusedPrivateMember = 0x8000000L;
	public static final long LocalVariableHiding = 0x10000000L;
	public static final long FieldHiding = 0x20000000L;
	public static final long AccidentalBooleanAssign = 0x40000000L;
	public static final long SuperfluousSemicolon = 0x80000000L;
	public static final long IndirectStaticAccess = 0x100000000L;
	public static final long UndocumentedEmptyBlock = 0x200000000L;
	public static final long UnnecessaryTypeCheck = 0x400000000L;
	
	// Default severity level for handlers
	public long errorThreshold = 
		UnreachableCode 
		| ImportProblem;
		
	public long warningThreshold = 
		MethodWithConstructorName 
		| UsingDeprecatedAPI 
		| MaskedCatchBlock 
		| OverriddenPackageDefaultMethod
		| UnusedImport
		| NonStaticAccessToStatic
		| NoEffectAssignment
		| IncompatibleNonInheritedInterfaceMethod
		| NoImplicitStringConversion;

	// Debug attributes
	public static final int Source = 1; // SourceFileAttribute
	public static final int Lines = 2; // LineNumberAttribute
	public static final int Vars = 4; // LocalVariableTableAttribute

	// By default only lines and source attributes are generated.
	public int produceDebugAttributes = Lines | Source;

	public long targetJDK = JDK1_1; // default generates for JVM1.1
	public long complianceLevel = JDK1_3; // by default be compliant with 1.3

	// toggle private access emulation for 1.2 (constr. accessor has extra arg on constructor) or 1.3 (make private constructor default access when access needed)
	public boolean isPrivateConstructorAccessChangingVisibility = false; // by default, follows 1.2

	// 1.4 feature (assertions are available in source 1.4 mode only)
	public long sourceLevel = JDK1_3; //1.3 behavior by default
	
	// source encoding format
	public String defaultEncoding = null; // will use the platform default encoding
	
	// print what unit is being processed
	public boolean verbose = Compiler.DEBUG;

	// indicates if reference info is desired
	public boolean produceReferenceInfo = true;

	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public boolean preserveAllLocalVariables = false;

	// indicates whether literal expressions are inlined at parse-time or not
	public boolean parseLiteralExpressionsAsConstants = true;

	// max problems per compilation unit
	public int maxProblemsPerUnit = 100; // no more than 100 problems per default
	
	// tags used to recognize tasks in comments
	public char[][] taskTags = null;

	// priorities of tasks in comments
	public char[][] taskPriorites = null;

	// deprecation report
	public boolean reportDeprecationInsideDeprecatedCode = false;
	
	// unused parameters report
	public boolean reportUnusedParameterWhenImplementingAbstract = false;
	public boolean reportUnusedParameterWhenOverridingConcrete = false;

	// constructor/setter parameter hiding
	public boolean reportSpecialParameterHidingField = false;
	
	/** 
	 * Initializing the compiler options with defaults
	 */
	public CompilerOptions(){
	}

	/** 
	 * Initializing the compiler options with external settings
	 */
	public CompilerOptions(Map settings){

		if (settings == null) return;
		
		// filter options which are related to the compiler component
		Iterator entries = settings.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry entry = (Map.Entry)entries.next();
			if (!(entry.getKey() instanceof String)) continue;
			if (!(entry.getValue() instanceof String)) continue;
			String optionID = (String) entry.getKey();
			String optionValue = (String) entry.getValue();
			
			// Local variable attribute
			if(optionID.equals(OPTION_LocalVariableAttribute)){
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Vars;
				} else if (optionValue.equals(DO_NOT_GENERATE)){
					this.produceDebugAttributes &= ~Vars;
				}
				continue;
			}  
			// Line number attribute	
			if(optionID.equals(OPTION_LineNumberAttribute)) {
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Lines;
				} else if (optionValue.equals(DO_NOT_GENERATE)) {
					this.produceDebugAttributes &= ~Lines;
				}
				continue;
			} 
			// Source file attribute	
			if(optionID.equals(OPTION_SourceFileAttribute)) {
				if (optionValue.equals(GENERATE)) {
					this.produceDebugAttributes |= Source;
				} else if (optionValue.equals(DO_NOT_GENERATE)) {
					this.produceDebugAttributes &= ~Source;
				}
				continue;
			} 
			// Preserve unused local	
			if(optionID.equals(OPTION_PreserveUnusedLocal)){
				if (optionValue.equals(PRESERVE)) {
					this.preserveAllLocalVariables = true;
				} else if (optionValue.equals(OPTIMIZE_OUT)) {
					this.preserveAllLocalVariables = false;
				}
				continue;
			} 
			// Report unreachable code				
			if(optionID.equals(OPTION_ReportUnreachableCode)){
				updateSeverity(UnreachableCode, optionValue);
				continue;
			} 
			// Report invalid import	
			if(optionID.equals(OPTION_ReportInvalidImport)){
				updateSeverity(ImportProblem, optionValue);
				continue;
			} 
			// Define the target JDK tag for .classfiles
			if(optionID.equals(OPTION_TargetPlatform)){
				long level = versionToJdkLevel(optionValue);
				if (level != 0) this.targetJDK = level;
				continue;
			} 
			// Define the JDK compliance level
			if(optionID.equals(OPTION_Compliance)){
				long level = versionToJdkLevel(optionValue);
				if (level != 0) this.complianceLevel = level;
				continue;
			} 
			// Private constructor access emulation (extra arg vs. visibility change)
			if(optionID.equals(OPTION_PrivateConstructorAccess)){
				long level = versionToJdkLevel(optionValue);
				if (level >= JDK1_3) this.isPrivateConstructorAccessChangingVisibility = true;
				continue;
			} 
			// Report method with constructor name
			if(optionID.equals(OPTION_ReportMethodWithConstructorName)){
				updateSeverity(MethodWithConstructorName, optionValue);
				continue;
			} 
			// Report overriding package default method
			if(optionID.equals(OPTION_ReportOverridingPackageDefaultMethod)){
				updateSeverity(OverriddenPackageDefaultMethod, optionValue);
				continue;
			} 
			// Report deprecation
			if(optionID.equals(OPTION_ReportDeprecation)){
				updateSeverity(UsingDeprecatedAPI, optionValue);
				continue;
			} 
			// Report deprecation inside deprecated code 
			if(optionID.equals(OPTION_ReportDeprecationInDeprecatedCode)){
				if (optionValue.equals(ENABLED)) {
					this.reportDeprecationInsideDeprecatedCode = true;
				} else if (optionValue.equals(DISABLED)) {
					this.reportDeprecationInsideDeprecatedCode = false;
				}
				continue;
			} 
			// Report hidden catch block
			if(optionID.equals(OPTION_ReportHiddenCatchBlock)){
				updateSeverity(MaskedCatchBlock, optionValue);
				continue;
			} 
			// Report unused local variable
			if(optionID.equals(OPTION_ReportUnusedLocal)){
				updateSeverity(UnusedLocalVariable, optionValue);
				continue;
			}
			// Report no implicit String conversion
			if (optionID.equals(OPTION_ReportNoImplicitStringConversion)) {
				updateSeverity(NoImplicitStringConversion, optionValue);
				continue;
			}
			// Report unused parameter
			if(optionID.equals(OPTION_ReportUnusedParameter)){
				updateSeverity(UnusedArgument, optionValue);
				continue;
			} 
			// Report unused parameter when implementing abstract method 
			if(optionID.equals(OPTION_ReportUnusedParameterWhenImplementingAbstract)){
				if (optionValue.equals(ENABLED)) {
					this.reportUnusedParameterWhenImplementingAbstract = true;
				} else if (optionValue.equals(DISABLED)) {
					this.reportUnusedParameterWhenImplementingAbstract = false;
				}
				continue;
			} 
			// Report unused parameter when implementing abstract method 
			if(optionID.equals(OPTION_ReportUnusedParameterWhenOverridingConcrete)){
				if (optionValue.equals(ENABLED)) {
					this.reportUnusedParameterWhenOverridingConcrete = true;
				} else if (optionValue.equals(DISABLED)) {
					this.reportUnusedParameterWhenOverridingConcrete = false;
				}
				continue;
			} 
			// Report unused import
			if(optionID.equals(OPTION_ReportUnusedImport)){
				updateSeverity(UnusedImport, optionValue);
				continue;
			} 
			// Report synthetic access emulation
			if(optionID.equals(OPTION_ReportSyntheticAccessEmulation)){
				updateSeverity(AccessEmulation, optionValue);
				continue;
			}
			// Report local var hiding another variable
			if(optionID.equals(OPTION_ReportLocalVariableHiding)){
				updateSeverity(LocalVariableHiding, optionValue);
				continue;
			}
			// Report field hiding another variable
			if(optionID.equals(OPTION_ReportFieldHiding)){
				updateSeverity(FieldHiding, optionValue);
				continue;
			}
			// Report constructor/setter parameter hiding another field
			if(optionID.equals(OPTION_ReportSpecialParameterHidingField)){
				if (optionValue.equals(ENABLED)) {
					this.reportSpecialParameterHidingField = true;
				} else if (optionValue.equals(DISABLED)) {
					this.reportSpecialParameterHidingField = false;
				}
				continue;
			}			
			// Report possible accidental boolean assignment
			if(optionID.equals(OPTION_ReportPossibleAccidentalBooleanAssignment)){
				updateSeverity(AccidentalBooleanAssign, optionValue);
				continue;
			}
			// Report possible accidental boolean assignment
			if(optionID.equals(OPTION_ReportSuperfluousSemicolon)){
				updateSeverity(SuperfluousSemicolon, optionValue);
				continue;
			}
			// Report non-externalized string literals
			if(optionID.equals(OPTION_ReportNonExternalizedStringLiteral)){
				updateSeverity(NonExternalizedString, optionValue);
				continue;
			}
			// Report usage of 'assert' as an identifier
			if(optionID.equals(OPTION_ReportAssertIdentifier)){
				updateSeverity(AssertUsedAsAnIdentifier, optionValue);
				continue;
			}
			// Set the source compatibility mode (assertions)
			if(optionID.equals(OPTION_Source)){
				long level = versionToJdkLevel(optionValue);
				if (level != 0) this.sourceLevel = level;
				continue;
			}
			// Set the default encoding format
			if(optionID.equals(OPTION_Encoding)){
				if (optionValue.length() == 0){
					this.defaultEncoding = null;
				} else {
					try { // ignore unsupported encoding
						new InputStreamReader(new ByteArrayInputStream(new byte[0]), optionValue);
						this.defaultEncoding = optionValue;
					} catch(UnsupportedEncodingException e){
					}
				}
				continue;
			}
			// Set the threshold for problems per unit
			if(optionID.equals(OPTION_MaxProblemPerUnit)){
				try {
					int val = Integer.parseInt(optionValue);
					if (val >= 0) this.maxProblemsPerUnit = val;
				} catch(NumberFormatException e){
				}				
				continue;
			}
			// Report unnecessary receiver for static access
			if(optionID.equals(OPTION_ReportNonStaticAccessToStatic)){
				updateSeverity(NonStaticAccessToStatic, optionValue);
				continue;
			} 
			// Report indirect static access
			if(optionID.equals(OPTION_ReportIndirectStaticAccess)){
				updateSeverity(IndirectStaticAccess, optionValue);
				continue;
			} 
			// Report interface method incompatible with non-inherited Object method
			if(optionID.equals(OPTION_ReportIncompatibleNonInheritedInterfaceMethod)){
				updateSeverity(IncompatibleNonInheritedInterfaceMethod, optionValue);
				continue;
			} 
			// Report unused private members
			if(optionID.equals(OPTION_ReportUnusedPrivateMember)){
				updateSeverity(UnusedPrivateMember, optionValue);
				continue;
			} 
			// Report boolean method throwing exception
			if(optionID.equals(OPTION_ReportUndocumentedEmptyBlock)){
				updateSeverity(UndocumentedEmptyBlock, optionValue);
				continue;
			} 
			// Report unnecessary cast
			if(optionID.equals(OPTION_ReportUnnecessaryTypeCheck)){
				updateSeverity(UnnecessaryTypeCheck, optionValue);
				continue;
			} 
			// Report task
			if(optionID.equals(OPTION_TaskTags)){
				if (optionValue.length() == 0) {
					this.taskTags = null;
				} else {
					this.taskTags = CharOperation.splitAndTrimOn(',', optionValue.toCharArray());
				}
				continue;
			} 
			// Report no-op assignments
			if(optionID.equals(OPTION_ReportNoEffectAssignment)){
				updateSeverity(NoEffectAssignment, optionValue);
				continue;
			}
			if(optionID.equals(OPTION_TaskPriorities)){
				if (optionValue.length() == 0) {
					this.taskPriorites = null;
				} else {
					this.taskPriorites = CharOperation.splitAndTrimOn(',', optionValue.toCharArray());
				}
				continue;
			} 
		}
	}

	public Map getMap() {
		Map optionsMap = new HashMap(30);
		optionsMap.put(OPTION_LocalVariableAttribute, (produceDebugAttributes & Vars) != 0 ? GENERATE : DO_NOT_GENERATE); 
		optionsMap.put(OPTION_LineNumberAttribute, (produceDebugAttributes & Lines) != 0 ? GENERATE : DO_NOT_GENERATE);
		optionsMap.put(OPTION_SourceFileAttribute, (produceDebugAttributes & Source) != 0 ? GENERATE : DO_NOT_GENERATE);
		optionsMap.put(OPTION_PreserveUnusedLocal, preserveAllLocalVariables ? PRESERVE : OPTIMIZE_OUT);
		optionsMap.put(OPTION_ReportUnreachableCode, getSeverityString(UnreachableCode)); 
		optionsMap.put(OPTION_ReportInvalidImport, getSeverityString(ImportProblem)); 
		optionsMap.put(OPTION_ReportMethodWithConstructorName, getSeverityString(MethodWithConstructorName)); 
		optionsMap.put(OPTION_ReportOverridingPackageDefaultMethod, getSeverityString(OverriddenPackageDefaultMethod)); 
		optionsMap.put(OPTION_ReportDeprecation, getSeverityString(UsingDeprecatedAPI)); 
		optionsMap.put(OPTION_ReportDeprecationInDeprecatedCode, reportDeprecationInsideDeprecatedCode ? ENABLED : DISABLED); 
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
		optionsMap.put(OPTION_ReportSuperfluousSemicolon, getSeverityString(SuperfluousSemicolon)); 
		optionsMap.put(OPTION_ReportAssertIdentifier, getSeverityString(AssertUsedAsAnIdentifier)); 
		optionsMap.put(OPTION_ReportUndocumentedEmptyBlock, getSeverityString(UndocumentedEmptyBlock)); 
		optionsMap.put(OPTION_ReportUnnecessaryTypeCheck, getSeverityString(UnnecessaryTypeCheck)); 
		optionsMap.put(OPTION_Compliance, versionFromJdkLevel(complianceLevel)); 
		optionsMap.put(OPTION_Source, versionFromJdkLevel(sourceLevel)); 
		optionsMap.put(OPTION_TargetPlatform, versionFromJdkLevel(targetJDK)); 
		if (defaultEncoding != null) {
			optionsMap.put(OPTION_Encoding, defaultEncoding); 
		}
		optionsMap.put(OPTION_TaskTags, this.taskTags == null ? "" : new String(CharOperation.concatWith(this.taskTags,','))); //$NON-NLS-1$
		optionsMap.put(OPTION_TaskPriorities, this.taskPriorites == null ? "" : new String(CharOperation.concatWith(this.taskPriorites,','))); //$NON-NLS-1$
		optionsMap.put(OPTION_ReportUnusedParameterWhenImplementingAbstract, reportUnusedParameterWhenImplementingAbstract ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportUnusedParameterWhenOverridingConcrete, reportUnusedParameterWhenOverridingConcrete ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_ReportSpecialParameterHidingField, reportSpecialParameterHidingField ? ENABLED : DISABLED); 
		optionsMap.put(OPTION_MaxProblemPerUnit, String.valueOf(maxProblemsPerUnit));
		return optionsMap;		
	}
	
	public int getSeverity(long irritant) {
		if((warningThreshold & irritant) != 0)
			return Warning;
		if((errorThreshold & irritant) != 0)
			return Error;
		return Ignore;
	}

	public String getSeverityString(long irritant) {
		if((warningThreshold & irritant) != 0)
			return WARNING;
		if((errorThreshold & irritant) != 0)
			return ERROR;
		return IGNORE;
	}
	
	public void produceReferenceInfo(boolean flag) {
		this.produceReferenceInfo = flag;
	}

	public void setVerboseMode(boolean flag) {
		this.verbose = flag;
	}

	public String toString() {
	
		StringBuffer buf = new StringBuffer("CompilerOptions:"); //$NON-NLS-1$
		buf.append("\n-local variables debug attributes: ").append((produceDebugAttributes & Vars) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-line number debug attributes: ").append((produceDebugAttributes & Lines) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-source debug attributes: ").append((produceDebugAttributes & Source) != 0 ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-preserve all local variables: ").append(preserveAllLocalVariables ? "ON" : " OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-unreachable code: ").append(getSeverityString(UnreachableCode)); //$NON-NLS-1$
		buf.append("\n-import problem: ").append(getSeverityString(ImportProblem)); //$NON-NLS-1$
		buf.append("\n--method with constructor name: ").append(getSeverityString(MethodWithConstructorName)); //$NON-NLS-1$
		buf.append("\n-overridden package default method: ").append(getSeverityString(OverriddenPackageDefaultMethod)); //$NON-NLS-1$
		buf.append("\n-deprecation: ").append(getSeverityString(UsingDeprecatedAPI)); //$NON-NLS-1$
		buf.append("\n-masked catch block: ").append(getSeverityString(MaskedCatchBlock)); //$NON-NLS-1$
		buf.append("\n-unused local variable: ").append(getSeverityString(UnusedLocalVariable)); //$NON-NLS-1$
		buf.append("\n-unused parameter: ").append(getSeverityString(UnusedArgument)); //$NON-NLS-1$
		buf.append("\n-unused import: ").append(getSeverityString(UnusedImport)); //$NON-NLS-1$
		buf.append("\n-synthetic access emulation: ").append(getSeverityString(AccessEmulation)); //$NON-NLS-1$
		buf.append("\n-assignment with no effect: ").append(getSeverityString(NoEffectAssignment)); //$NON-NLS-1$
		buf.append("\n-non externalized string: ").append(getSeverityString(NonExternalizedString)); //$NON-NLS-1$
		buf.append("\n-static access receiver: ").append(getSeverityString(NonStaticAccessToStatic)); //$NON-NLS-1$
		buf.append("\n-indirect static access: ").append(getSeverityString(IndirectStaticAccess)); //$NON-NLS-1$
		buf.append("\n-incompatible non inherited interface method: ").append(getSeverityString(IncompatibleNonInheritedInterfaceMethod)); //$NON-NLS-1$
		buf.append("\n-unused private member: ").append(getSeverityString(UnusedPrivateMember)); //$NON-NLS-1$
		buf.append("\n-local variable hiding another variable: ").append(getSeverityString(LocalVariableHiding)); //$NON-NLS-1$
		buf.append("\n-field hiding another variable: ").append(getSeverityString(FieldHiding)); //$NON-NLS-1$
		buf.append("\n-possible accidental boolean assignment: ").append(getSeverityString(AccidentalBooleanAssign)); //$NON-NLS-1$
		buf.append("\n-superfluous semicolon: ").append(getSeverityString(SuperfluousSemicolon)); //$NON-NLS-1$
		buf.append("\n-uncommented empty block: ").append(getSeverityString(UndocumentedEmptyBlock)); //$NON-NLS-1$
		buf.append("\n-JDK compliance level: "+ versionFromJdkLevel(complianceLevel)); //$NON-NLS-1$
		buf.append("\n-JDK source level: "+ versionFromJdkLevel(sourceLevel)); //$NON-NLS-1$
		buf.append("\n-JDK target level: "+ versionFromJdkLevel(targetJDK)); //$NON-NLS-1$
		buf.append("\n-private constructor access: ").append(isPrivateConstructorAccessChangingVisibility ? "extra argument" : "make default access"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-verbose : ").append(verbose ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-produce reference info : ").append(produceReferenceInfo ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-parse literal expressions as constants : ").append(parseLiteralExpressionsAsConstants ? "ON" : "OFF"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-encoding : ").append(defaultEncoding == null ? "<default>" : defaultEncoding); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n-task tags: ").append(this.taskTags == null ? "" : new String(CharOperation.concatWith(this.taskTags,',')));  //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n-task priorities : ").append(this.taskPriorites == null ? "" : new String(CharOperation.concatWith(this.taskPriorites,','))); //$NON-NLS-1$ //$NON-NLS-2$
		buf.append("\n-report deprecation inside deprecated code : ").append(reportDeprecationInsideDeprecatedCode ? "ENABLED" : "DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-report unused parameter when implementing abstract method : ").append(reportUnusedParameterWhenImplementingAbstract ? "ENABLED" : "DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-report unused parameter when overriding concrete method : ").append(reportUnusedParameterWhenOverridingConcrete ? "ENABLED" : "DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		buf.append("\n-report constructor/setter parameter hiding existing field : ").append(reportSpecialParameterHidingField ? "ENABLED" : "DISABLED"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return buf.toString();
	}

	void updateSeverity(long irritant, String severityString) {
		if (severityString.equals(ERROR)) {
			this.errorThreshold |= irritant;
			this.warningThreshold &= ~irritant;
		} else if (severityString.equals(WARNING)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold |= irritant;
		} else if (severityString.equals(IGNORE)) {
			this.errorThreshold &= ~irritant;
			this.warningThreshold &= ~irritant;
		}
	}				
	public static long versionToJdkLevel(String versionID) {
		if (versionID.equals(VERSION_1_1)) {
			return JDK1_1;
		} else if (versionID.equals(VERSION_1_2)) {
			return JDK1_2;
		} else if (versionID.equals(VERSION_1_3)) {
			return JDK1_3;
		} else if (versionID.equals(VERSION_1_4)) {
			return JDK1_4;
		} else if (versionID.equals(VERSION_1_5)) {
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

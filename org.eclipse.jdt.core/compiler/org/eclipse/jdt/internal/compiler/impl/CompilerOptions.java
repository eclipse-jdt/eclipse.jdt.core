package org.eclipse.jdt.internal.compiler.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.Locale;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.lookup.*;

public class CompilerOptions implements ConfigurableProblems, ProblemIrritants, ProblemReasons, ProblemSeverities {
	
	/**
	 * Option IDs
	 */
	public static final String OPTION_LocalVariableAttribute = Compiler.class.getName() + ".localVariableAttribute"; //$NON-NLS-1$
	public static final String OPTION_LineNumberAttribute = Compiler.class.getName() + ".lineNumberAttribute"; //$NON-NLS-1$
	public static final String OPTION_SourceFileAttribute = Compiler.class.getName() + ".sourceFileAttribute"; //$NON-NLS-1$
	public static final String OPTION_PreserveUnusedLocal = Compiler.class.getName() + ".preserveUnusedLocal"; //$NON-NLS-1$
	public static final String OPTION_ReportUnreachableCode = Compiler.class.getName() + ".reportUnreachableCode"; //$NON-NLS-1$
	public static final String OPTION_ReportInvalidImport = Compiler.class.getName() + ".reportInvalidImport"; //$NON-NLS-1$
	public static final String OPTION_ReportMethodWithConstructorName = Compiler.class.getName() + ".reportMethodWithConstructorName"; //$NON-NLS-1$
	public static final String OPTION_ReportOverridingPackageDefaultMethod = Compiler.class.getName() + ".reportOverridingPackageDefaultMethod"; //$NON-NLS-1$
	public static final String OPTION_ReportDeprecation = Compiler.class.getName() + ".reportDeprecation"; //$NON-NLS-1$
	public static final String OPTION_ReportHiddenCatchBlock = Compiler.class.getName() + ".reportHiddenCatchBlock"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedLocal = Compiler.class.getName() + ".reportUnusedLocal"; //$NON-NLS-1$
	public static final String OPTION_ReportUnusedParameter = Compiler.class.getName() + ".reportUnusedParameter"; //$NON-NLS-1$
	public static final String OPTION_ReportSyntheticAccessEmulation = Compiler.class.getName() + ".reportSyntheticAccessEmulation"; //$NON-NLS-1$
	public static final String OPTION_ReportNonExternalizedStringLiteral = Compiler.class.getName() + ".reportNonExternalizedStringLiteral"; //$NON-NLS-1$
	public static final String OPTION_Source = Compiler.class.getName() + ".source"; //$NON-NLS-1$
	public static final String OPTION_TargetPlatform = Compiler.class.getName() + ".targetPlatform"; //$NON-NLS-1$
	public static final String OPTION_ReportAssertIdentifier = Compiler.class.getName() + ".reportAssertIdentifier"; //$NON-NLS-1$

	// class file output
	// these are the bits used to buld a mask to know which debug 
	// attributes should be included in the .class file
	// By default only lines and source attributes are generated.
	public static final int Source = 1; // SourceFileAttribute
	public static final int Lines = 2; // LineNumberAttribute
	public static final int Vars = 4; // LocalVariableTableAttribute

	public int produceDebugAttributes = Lines | Source;

	// default severity level for handlers
	public int errorThreshold = UnreachableCode | ImportProblem;
	public int warningThreshold = 
		MethodWithConstructorName | OverriddenPackageDefaultMethod |
		UsingDeprecatedAPI | MaskedCatchBlock |
		UnusedLocalVariable | AssertUsedAsAnIdentifier |
		TemporaryWarning;

	// target JDK 1.1 or 1.2
	public static final int JDK1_1 = 0;
	public static final int JDK1_2 = 1;
	public int targetJDK = JDK1_1; // default generates for JVM1.1

	// 1.4 feature
	public boolean assertMode = false; //1.3 behavior by default
	
	// print what unit is being processed
	public boolean verbose = false;
	// indicates if reference info is desired
	public boolean produceReferenceInfo = true;
	// indicates if unused/optimizable local variables need to be preserved (debugging purpose)
	public boolean preserveAllLocalVariables = false;
	// indicates whether literal expressions are inlined at parse-time or not
	public boolean parseLiteralExpressionsAsConstants = true;

	// exception raised for unresolved compile errors
	public String runtimeExceptionNameForCompileError = "java.lang.Error"; //$NON-NLS-1$

	// toggle private access emulation for 1.2 (constr. accessor has extra arg on constructor) or 1.3 (make private constructor default access when access needed)
	public boolean isPrivateConstructorAccessChangingVisibility = false; // by default, follows 1.2
/** 
 * Initializing the compiler options with defaults
 */
public CompilerOptions(){
}
/** 
 * Initializing the compiler options with external settings
 */
public CompilerOptions(ConfigurableOption[] settings){
	if (settings == null) return;
	
	// filter options which are related to the compiler component
	String componentName = Compiler.class.getName();
	for (int i = 0, max = settings.length; i < max; i++){
		if (settings[i].getComponentName().equals(componentName)){
			this.setOption(settings[i]);
		}
	}
}

public int getDebugAttributesMask() {
	return this.produceDebugAttributes;
}
public int getTargetJDK() {
	return this.targetJDK;
}
public boolean getAssertMode() {
	return this.assertMode;
}
public void setAccessEmulationSeverity(int flag) {
	errorThreshold &= ~AccessEmulation;
	warningThreshold &= ~AccessEmulation;
	switch(flag){
		case Error : 
			errorThreshold |= AccessEmulation;
			break;
		case Warning : 
			warningThreshold |= AccessEmulation;
			break;
	}
}
public void setDeprecationUseSeverity(int flag) {
	errorThreshold &= ~UsingDeprecatedAPI;
	warningThreshold &= ~UsingDeprecatedAPI;
	switch(flag){
		case Error : 
			errorThreshold |= UsingDeprecatedAPI;
			break;
		case Warning : 
			warningThreshold |= UsingDeprecatedAPI;
			break;
	}
}
public void setImportProblemSeverity(int flag) {
	errorThreshold &= ~ImportProblem;
	warningThreshold &= ~ImportProblem;
	switch(flag){
		case Error : 
			errorThreshold |= ImportProblem;
			break;
		case Warning : 
			warningThreshold |= ImportProblem;
			break;
	}
}
public void setMaskedCatchBlockSeverity(int flag) {
	errorThreshold &= ~MaskedCatchBlock;
	warningThreshold &= ~MaskedCatchBlock;
	switch(flag){
		case Error : 
			errorThreshold |= MaskedCatchBlock;
			break;
		case Warning : 
			warningThreshold |= MaskedCatchBlock;
			break;
	}
}
public void setMethodWithConstructorNameSeverity(int flag) {
	errorThreshold &= ~MethodWithConstructorName;
	warningThreshold &= ~MethodWithConstructorName;
	switch(flag){
		case Error : 
			errorThreshold |= MethodWithConstructorName;
			break;
		case Warning : 
			warningThreshold |= MethodWithConstructorName;
			break;
	}
}

public void setOverriddenPackageDefaultMethodSeverity(int flag) {
	errorThreshold &= ~OverriddenPackageDefaultMethod;
	warningThreshold &= ~OverriddenPackageDefaultMethod;
	switch(flag){
		case Error : 
			errorThreshold |= OverriddenPackageDefaultMethod;
			break;
		case Warning : 
			warningThreshold |= OverriddenPackageDefaultMethod;
			break;
	}
}
public void setUnreachableCodeSeverity(int flag) {
	errorThreshold &= ~UnreachableCode;
	warningThreshold &= ~UnreachableCode;
	switch(flag){
		case Error : 
			errorThreshold |= UnreachableCode;
			break;
		case Warning : 
			warningThreshold |= UnreachableCode;
			break;
	}	
}
public void setUnusedArgumentSeverity(int flag) {
	errorThreshold &= ~UnusedArgument;
	warningThreshold &= ~UnusedArgument;
	switch(flag){
		case Error : 
			errorThreshold |= UnusedArgument;
			break;
		case Warning : 
			warningThreshold |= UnusedArgument;
			break;
	}
}
public void setUnusedLocalVariableSeverity(int flag) {
	errorThreshold &= ~UnusedLocalVariable;
	warningThreshold &= ~UnusedLocalVariable;
	switch(flag){
		case Error : 
			errorThreshold |= UnusedLocalVariable;
			break;
		case Warning : 
			warningThreshold |= UnusedLocalVariable;
			break;
	}
}
public void setNonExternalizedStringLiteralSeverity(int flag) {
	errorThreshold &= ~NonExternalizedString;
	warningThreshold &= ~NonExternalizedString;
	switch(flag){
		case Error : 
			errorThreshold |= NonExternalizedString;
			break;
		case Warning : 
			warningThreshold |= NonExternalizedString;
			break;
	}	
}
public void setAssertIdentifierSeverity(int flag) {
	errorThreshold &= ~AssertUsedAsAnIdentifier;
	warningThreshold &= ~AssertUsedAsAnIdentifier;
	switch(flag){
		case Error : 
			errorThreshold |= AssertUsedAsAnIdentifier;
			break;
		case Warning : 
			warningThreshold |= AssertUsedAsAnIdentifier;
			break;
	}	
}
public int getAccessEmulationSeverity() {
	if((warningThreshold & AccessEmulation) != 0)
		return Warning;
	if((errorThreshold & AccessEmulation) != 0)
		return Error;
	return Ignore;
}
public int getDeprecationUseSeverity() {
	if((warningThreshold & UsingDeprecatedAPI) != 0)
		return Warning;
	if((errorThreshold & UsingDeprecatedAPI) != 0)
		return Error;
	return Ignore;
}
public int getImportProblemSeverity() {
	if((warningThreshold & ImportProblem) != 0)
		return Warning;
	if((errorThreshold & ImportProblem) != 0)
		return Error;
	return Ignore;
}
public int getMaskedCatchBlockSeverity() {
	if((warningThreshold & MaskedCatchBlock) != 0)
		return Warning;
	if((errorThreshold & MaskedCatchBlock) != 0)
		return Error;
	return Ignore;
}
public int getMethodWithConstructorNameSeverity() {
	if((warningThreshold & MethodWithConstructorName) != 0)
		return Warning;
	if((errorThreshold & MethodWithConstructorName) != 0)
		return Error;
	return Ignore;
}

public int getOverriddenPackageDefaultMethodSeverity() {
	if((warningThreshold & OverriddenPackageDefaultMethod) != 0)
		return Warning;
	if((errorThreshold & OverriddenPackageDefaultMethod) != 0)
		return Error;
	return Ignore;
}
public boolean isPreservingAllLocalVariables() {
	return this.preserveAllLocalVariables ;
}
public boolean isPrivateConstructorAccessChangingVisibility() {
	return isPrivateConstructorAccessChangingVisibility;
}
public int getUnreachableCodeHandledAsError() {
	if((warningThreshold & UnreachableCode) != 0)
		return Warning;
	if((errorThreshold & UnreachableCode) != 0)
		return Error;
	return Ignore;
}
public int getUnusedArgumentSeverity() {
	if((warningThreshold & UnusedArgument) != 0)
		return Warning;
	if((errorThreshold & UnusedArgument) != 0)
		return Error;
	return Ignore;
}
public int getUnusedLocalVariableSeverity() {
	if((warningThreshold & UnusedLocalVariable) != 0)
		return Warning;
	if((errorThreshold & UnusedLocalVariable) != 0)
		return Error;
	return Ignore;
}
public int getNonExternalizedStringLiteralSeverity() {
	if((warningThreshold & NonExternalizedString) != 0)
		return Warning;
	if((errorThreshold & NonExternalizedString) != 0)
		return Error;
	return Ignore;
}
public int getAssertIdentifierSeverity() {
	if((warningThreshold & NonExternalizedString) != 0)
		return Warning;
	if((errorThreshold & NonExternalizedString) != 0)
		return Error;
	return Ignore;
}
public void preserveAllLocalVariables(boolean flag) {
	this.preserveAllLocalVariables = flag;
}
public void privateConstructorAccessChangesVisibility(boolean flag) {
	isPrivateConstructorAccessChangingVisibility = flag;
}
public void produceDebugAttributes(int mask) {
	this.produceDebugAttributes = mask;
}
public void produceReferenceInfo(boolean flag) {
	this.produceReferenceInfo = flag;
}
public void setErrorThreshold(int errorMask) {
	this.errorThreshold = errorMask;
}
/**
 * Change the value of the option corresponding to the option ID
 */
void setOption(ConfigurableOption setting) {
	String componentName = Compiler.class.getName();
	
	String optionID = setting.getID();
	
	if(optionID.equals(OPTION_LocalVariableAttribute)){
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Vars.
			produceDebugAttributes |= Vars;
		} else {
			produceDebugAttributes &= ~Vars;
		}
	} else if(optionID.equals(OPTION_LineNumberAttribute)) {
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Lines
			produceDebugAttributes |= Lines;
		} else {
			produceDebugAttributes &= ~Lines;
		}
	}else if(optionID.equals(OPTION_SourceFileAttribute)) {
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Source.
			produceDebugAttributes |= Source;
		} else {
			produceDebugAttributes &= ~Source;
		}
	}else if(optionID.equals(OPTION_PreserveUnusedLocal)){
		preserveAllLocalVariables(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_ReportUnreachableCode)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnreachableCodeSeverity(Error);
				break;
			case 1 :
				setUnreachableCodeSeverity(Warning);
				break;
			case 2 : 
				setUnreachableCodeSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportInvalidImport)){
		switch(setting.getValueIndex()){
			case 0 : 
				setImportProblemSeverity(Error);
				break;
			case 1 :
				setImportProblemSeverity(Warning);
				break;
			case 2 :
				setImportProblemSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_TargetPlatform)){
		setTargetJDK(setting.getValueIndex() == 0 ? JDK1_1 : JDK1_2);
	}else if(optionID.equals(OPTION_ReportMethodWithConstructorName)){
		switch(setting.getValueIndex()){
			case 0 : 
				setMethodWithConstructorNameSeverity(Error);
				break;
			case 1 :
				setMethodWithConstructorNameSeverity(Warning);
				break;
			case 2 :
				setMethodWithConstructorNameSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportOverridingPackageDefaultMethod)){
		switch(setting.getValueIndex()){
			case 0 : 
				setOverriddenPackageDefaultMethodSeverity(Error);
				break;
			case 1 :
				setOverriddenPackageDefaultMethodSeverity(Warning);
				break;
			case 2 :
				setOverriddenPackageDefaultMethodSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportDeprecation)){
		switch(setting.getValueIndex()){
			case 0 : 
				setDeprecationUseSeverity(Error);
				break;
			case 1 :
				setDeprecationUseSeverity(Warning);
				break;
			case 2 :
				setDeprecationUseSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportHiddenCatchBlock)){
		switch(setting.getValueIndex()){
			case 0 : 
				setMaskedCatchBlockSeverity(Error);
				break;
			case 1 :
				setMaskedCatchBlockSeverity(Warning);
				break;
			case 2 :
				setMaskedCatchBlockSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportUnusedLocal)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnusedLocalVariableSeverity(Error);
				break;
			case 1 :
				setUnusedLocalVariableSeverity(Warning);
				break;
			case 2 :
				setUnusedLocalVariableSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportUnusedParameter)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnusedArgumentSeverity(Error);
				break;
			case 1 :
				setUnusedArgumentSeverity(Warning);
				break;
			case 2 :
				setUnusedArgumentSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportSyntheticAccessEmulation)){
		switch(setting.getValueIndex()){
			case 0 : 
				setAccessEmulationSeverity(Error);
				break;
			case 1 :
				setAccessEmulationSeverity(Warning);
				break;
			case 2 :
				setAccessEmulationSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportNonExternalizedStringLiteral)){
		switch(setting.getValueIndex()){
			case 0 : 
				setNonExternalizedStringLiteralSeverity(Error);
				break;
			case 1 :
				setNonExternalizedStringLiteralSeverity(Warning);
				break;
			case 2 :
				setNonExternalizedStringLiteralSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_ReportAssertIdentifier)){
		switch(setting.getValueIndex()){
			case 0 : 
				setAssertIdentifierSeverity(Error);
				break;
			case 1 :
				setAssertIdentifierSeverity(Warning);
				break;
			case 2 :
				setAssertIdentifierSeverity(Ignore);
				break;
		}
	}else if(optionID.equals(OPTION_Source)){
		setAssertMode(setting.getValueIndex() == 1);
	}
}

public void setTargetJDK(int vmID) {
	this.targetJDK = vmID;
}
public void setVerboseMode(boolean flag) {
	this.verbose = flag;
}
public void setAssertMode(boolean assertMode) {
	this.assertMode = assertMode;
}
public void setWarningThreshold(int warningMask) {
	this.warningThreshold = warningMask;
}
public String toString() {

	StringBuffer buf = new StringBuffer("CompilerOptions:"); //$NON-NLS-1$
	if ((produceDebugAttributes & Vars) != 0){
		buf.append("\n-local variables debug attributes: ON"); //$NON-NLS-1$
	} else {
		buf.append("\n-local variables debug attributes: OFF"); //$NON-NLS-1$
	}
	if ((produceDebugAttributes & Lines) != 0){
		buf.append("\n-line number debug attributes: ON"); //$NON-NLS-1$
	} else {
		buf.append("\n-line number debug attributes: OFF"); //$NON-NLS-1$
	}
	if ((produceDebugAttributes & Source) != 0){
		buf.append("\n-source debug attributes: ON"); //$NON-NLS-1$
	} else {
		buf.append("\n-source debug attributes: OFF"); //$NON-NLS-1$
	}
	if (preserveAllLocalVariables){
		buf.append("\n-preserve all local variables: ON"); //$NON-NLS-1$
	} else {
		buf.append("\n-preserve all local variables: OFF"); //$NON-NLS-1$
	}
	if ((errorThreshold & UnreachableCode) != 0){
		buf.append("\n-unreachable code: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & UnreachableCode) != 0){
			buf.append("\n-unreachable code: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-unreachable code: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & ImportProblem) != 0){
		buf.append("\n-import problem: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & ImportProblem) != 0){
			buf.append("\n-import problem: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-import problem: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & MethodWithConstructorName) != 0){
		buf.append("\n-method with constructor name: ERROR");		 //$NON-NLS-1$
	} else {
		if ((warningThreshold & MethodWithConstructorName) != 0){
			buf.append("\n-method with constructor name: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-method with constructor name: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & OverriddenPackageDefaultMethod) != 0){
		buf.append("\n-overridden package default method: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & OverriddenPackageDefaultMethod) != 0){
			buf.append("\n-overridden package default method: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-overridden package default method: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & UsingDeprecatedAPI) != 0){
		buf.append("\n-deprecation: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & UsingDeprecatedAPI) != 0){
			buf.append("\n-deprecation: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-deprecation: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & MaskedCatchBlock) != 0){
		buf.append("\n-masked catch block: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & MaskedCatchBlock) != 0){
			buf.append("\n-masked catch block: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-masked catch block: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & UnusedLocalVariable) != 0){
		buf.append("\n-unused local variable: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & UnusedLocalVariable) != 0){
			buf.append("\n-unused local variable: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-unused local variable: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & UnusedArgument) != 0){
		buf.append("\n-unused parameter: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & UnusedArgument) != 0){
			buf.append("\n-unused parameter: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-unused parameter: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & AccessEmulation) != 0){
		buf.append("\n-synthetic access emulation: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & AccessEmulation) != 0){
			buf.append("\n-synthetic access emulation: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-synthetic access emulation: IGNORE"); //$NON-NLS-1$
		}
	}
	if ((errorThreshold & NonExternalizedString) != 0){
		buf.append("\n-non externalized string: ERROR"); //$NON-NLS-1$
	} else {
		if ((warningThreshold & NonExternalizedString) != 0){
			buf.append("\n-non externalized string: WARNING"); //$NON-NLS-1$
		} else {
			buf.append("\n-non externalized string: IGNORE"); //$NON-NLS-1$
		}
	}
	switch(targetJDK){
		case JDK1_1 :
			buf.append("\n-target JDK: 1.1"); //$NON-NLS-1$
			break;
		case JDK1_2 :
			buf.append("\n-target JDK: 1.2"); //$NON-NLS-1$
	}
	buf.append("\n-verbose : " + (verbose ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	buf.append("\n-produce reference info : " + (produceReferenceInfo ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	buf.append("\n-parse literal expressions as constants : " + (parseLiteralExpressionsAsConstants ? "ON" : "OFF")); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	buf.append("\n-runtime exception name for compile error : " + runtimeExceptionNameForCompileError); //$NON-NLS-1$
	return buf.toString();
}
}

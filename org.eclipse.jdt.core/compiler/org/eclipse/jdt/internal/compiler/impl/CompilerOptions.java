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
	private static final int ERROR = 0;
	private static final int WARNING = 1;
	private static final int IGNORE = 2;
	
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
	public String runtimeExceptionNameForCompileError = "java.lang.Error"/*nonNLS*/;

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
		case ERROR : 
			errorThreshold |= AccessEmulation;
			break;
		case WARNING : 
			warningThreshold |= AccessEmulation;
			break;
	}
}
public void setDeprecationUseSeverity(int flag) {
	errorThreshold &= ~UsingDeprecatedAPI;
	warningThreshold &= ~UsingDeprecatedAPI;
	switch(flag){
		case ERROR : 
			errorThreshold |= UsingDeprecatedAPI;
			break;
		case WARNING : 
			warningThreshold |= UsingDeprecatedAPI;
			break;
	}
}
public void setImportProblemSeverity(int flag) {
	errorThreshold &= ~ImportProblem;
	warningThreshold &= ~ImportProblem;
	switch(flag){
		case ERROR : 
			errorThreshold |= ImportProblem;
			break;
		case WARNING : 
			warningThreshold |= ImportProblem;
			break;
	}
}
public void setMaskedCatchBlockSeverity(int flag) {
	errorThreshold &= ~MaskedCatchBlock;
	warningThreshold &= ~MaskedCatchBlock;
	switch(flag){
		case ERROR : 
			errorThreshold |= MaskedCatchBlock;
			break;
		case WARNING : 
			warningThreshold |= MaskedCatchBlock;
			break;
	}
}
public void setMethodWithConstructorNameSeverity(int flag) {
	errorThreshold &= ~MethodWithConstructorName;
	warningThreshold &= ~MethodWithConstructorName;
	switch(flag){
		case ERROR : 
			errorThreshold |= MethodWithConstructorName;
			break;
		case WARNING : 
			warningThreshold |= MethodWithConstructorName;
			break;
	}
}

public void setOverriddenPackageDefaultMethodSeverity(int flag) {
	errorThreshold &= ~OverriddenPackageDefaultMethod;
	warningThreshold &= ~OverriddenPackageDefaultMethod;
	switch(flag){
		case ERROR : 
			errorThreshold |= OverriddenPackageDefaultMethod;
			break;
		case WARNING : 
			warningThreshold |= OverriddenPackageDefaultMethod;
			break;
	}
}
public void setUnreachableCodeSeverity(int flag) {
	errorThreshold &= ~UnreachableCode;
	warningThreshold &= ~UnreachableCode;
	switch(flag){
		case ERROR : 
			errorThreshold |= UnreachableCode;
			break;
		case WARNING : 
			warningThreshold |= UnreachableCode;
			break;
	}	
}
public void setUnusedArgumentSeverity(int flag) {
	errorThreshold &= ~UnusedArgument;
	warningThreshold &= ~UnusedArgument;
	switch(flag){
		case ERROR : 
			errorThreshold |= UnusedArgument;
			break;
		case WARNING : 
			warningThreshold |= UnusedArgument;
			break;
	}
}
public void setUnusedLocalVariableSeverity(int flag) {
	errorThreshold &= ~UnusedLocalVariable;
	warningThreshold &= ~UnusedLocalVariable;
	switch(flag){
		case ERROR : 
			errorThreshold |= UnusedLocalVariable;
			break;
		case WARNING : 
			warningThreshold |= UnusedLocalVariable;
			break;
	}
}
public void setNonExternalizedStringLiteralSeverity(int flag) {
	errorThreshold &= ~NonExternalizedString;
	warningThreshold &= ~NonExternalizedString;
	switch(flag){
		case ERROR : 
			errorThreshold |= NonExternalizedString;
			break;
		case WARNING : 
			warningThreshold |= NonExternalizedString;
			break;
	}	
}
public void setAssertIdentifierSeverity(int flag) {
	errorThreshold &= ~AssertUsedAsAnIdentifier;
	warningThreshold &= ~AssertUsedAsAnIdentifier;
	switch(flag){
		case ERROR : 
			errorThreshold |= AssertUsedAsAnIdentifier;
			break;
		case WARNING : 
			warningThreshold |= AssertUsedAsAnIdentifier;
			break;
	}	
}
public int getAccessEmulationSeverity() {
	if((warningThreshold & AccessEmulation) != 0)
		return WARNING;
	if((errorThreshold & AccessEmulation) != 0)
		return ERROR;
	return IGNORE;
}
public int getDeprecationUseSeverity() {
	if((warningThreshold & UsingDeprecatedAPI) != 0)
		return WARNING;
	if((errorThreshold & UsingDeprecatedAPI) != 0)
		return ERROR;
	return IGNORE;
}
public int getImportProblemSeverity() {
	if((warningThreshold & ImportProblem) != 0)
		return WARNING;
	if((errorThreshold & ImportProblem) != 0)
		return ERROR;
	return IGNORE;
}
public int getMaskedCatchBlockSeverity() {
	if((warningThreshold & MaskedCatchBlock) != 0)
		return WARNING;
	if((errorThreshold & MaskedCatchBlock) != 0)
		return ERROR;
	return IGNORE;
}
public int getMethodWithConstructorNameSeverity() {
	if((warningThreshold & MethodWithConstructorName) != 0)
		return WARNING;
	if((errorThreshold & MethodWithConstructorName) != 0)
		return ERROR;
	return IGNORE;
}

public int getOverriddenPackageDefaultMethodSeverity() {
	if((warningThreshold & OverriddenPackageDefaultMethod) != 0)
		return WARNING;
	if((errorThreshold & OverriddenPackageDefaultMethod) != 0)
		return ERROR;
	return IGNORE;
}
public boolean isPreservingAllLocalVariables() {
	return this.preserveAllLocalVariables ;
}
public boolean isPrivateConstructorAccessChangingVisibility() {
	return isPrivateConstructorAccessChangingVisibility;
}
public int getUnreachableCodeHandledAsError() {
	if((warningThreshold & UnreachableCode) != 0)
		return WARNING;
	if((errorThreshold & UnreachableCode) != 0)
		return ERROR;
	return IGNORE;
}
public int getUnusedArgumentSeverity() {
	if((warningThreshold & UnusedArgument) != 0)
		return WARNING;
	if((errorThreshold & UnusedArgument) != 0)
		return ERROR;
	return IGNORE;
}
public int getUnusedLocalVariableSeverity() {
	if((warningThreshold & UnusedLocalVariable) != 0)
		return WARNING;
	if((errorThreshold & UnusedLocalVariable) != 0)
		return ERROR;
	return IGNORE;
}
public int getNonExternalizedStringLiteralSeverity() {
	if((warningThreshold & NonExternalizedString) != 0)
		return WARNING;
	if((errorThreshold & NonExternalizedString) != 0)
		return ERROR;
	return IGNORE;
}
public int getAssertIdentifierSeverity() {
	if((warningThreshold & NonExternalizedString) != 0)
		return WARNING;
	if((errorThreshold & NonExternalizedString) != 0)
		return ERROR;
	return IGNORE;
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
	
	if(optionID.equals(componentName + ".debugLocalVariable"/*nonNLS*/)){
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Vars.
			produceDebugAttributes |= Vars;
		} else {
			produceDebugAttributes &= ~Vars;
		}
	} else if(optionID.equals(componentName + ".debugLineNumber"/*nonNLS*/)) {
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Lines
			produceDebugAttributes |= Lines;
		} else {
			produceDebugAttributes &= ~Lines;
		}
	}else if(optionID.equals(componentName + ".debugSourceFile"/*nonNLS*/)) {
		if (setting.getValueIndex() == 0) {
			// set the debug flag with Source.
			produceDebugAttributes |= Source;
		} else {
			produceDebugAttributes &= ~Source;
		}
	}else if(optionID.equals(componentName + ".codegenUnusedLocal"/*nonNLS*/)){
		preserveAllLocalVariables(setting.getValueIndex() == 0);
	}else if(optionID.equals(componentName + ".problemUnreachableCode"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnreachableCodeSeverity(ERROR);
				break;
			case 1 :
				setUnreachableCodeSeverity(WARNING);
				break;
			case 2 :
				setUnreachableCodeSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemInvalidImport"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setImportProblemSeverity(ERROR);
				break;
			case 1 :
				setImportProblemSeverity(WARNING);
				break;
			case 2 :
				setImportProblemSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".codegenTargetPlatform"/*nonNLS*/)){
		setTargetJDK(setting.getValueIndex() == 0 ? JDK1_1 : JDK1_2);
	}else if(optionID.equals(componentName + ".problemMethodWithConstructorName"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setMethodWithConstructorNameSeverity(ERROR);
				break;
			case 1 :
				setMethodWithConstructorNameSeverity(WARNING);
				break;
			case 2 :
				setMethodWithConstructorNameSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemOverridingPackageDefaultMethod"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setOverriddenPackageDefaultMethodSeverity(ERROR);
				break;
			case 1 :
				setOverriddenPackageDefaultMethodSeverity(WARNING);
				break;
			case 2 :
				setOverriddenPackageDefaultMethodSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemDeprecation"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setDeprecationUseSeverity(ERROR);
				break;
			case 1 :
				setDeprecationUseSeverity(WARNING);
				break;
			case 2 :
				setDeprecationUseSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemHiddenCatchBlock"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setMaskedCatchBlockSeverity(ERROR);
				break;
			case 1 :
				setMaskedCatchBlockSeverity(WARNING);
				break;
			case 2 :
				setMaskedCatchBlockSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemUnusedLocal"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnusedLocalVariableSeverity(ERROR);
				break;
			case 1 :
				setUnusedLocalVariableSeverity(WARNING);
				break;
			case 2 :
				setUnusedLocalVariableSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemUnusedParameter"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setUnusedArgumentSeverity(ERROR);
				break;
			case 1 :
				setUnusedArgumentSeverity(WARNING);
				break;
			case 2 :
				setUnusedArgumentSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemSyntheticAccessEmulation"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setAccessEmulationSeverity(ERROR);
				break;
			case 1 :
				setAccessEmulationSeverity(WARNING);
				break;
			case 2 :
				setAccessEmulationSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemNonExternalizedStringLiteral"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setNonExternalizedStringLiteralSeverity(ERROR);
				break;
			case 1 :
				setNonExternalizedStringLiteralSeverity(WARNING);
				break;
			case 2 :
				setNonExternalizedStringLiteralSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".problemAssertIdentifier"/*nonNLS*/)){
		switch(setting.getValueIndex()){
			case 0 : 
				setAssertIdentifierSeverity(ERROR);
				break;
			case 1 :
				setAssertIdentifierSeverity(WARNING);
				break;
			case 2 :
				setAssertIdentifierSeverity(IGNORE);
				break;
		}
	}else if(optionID.equals(componentName + ".source"/*nonNLS*/)){
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

	StringBuffer buf = new StringBuffer("CompilerOptions:"/*nonNLS*/);
	if ((produceDebugAttributes & Vars) != 0){
		buf.append("\n-local variables debug attributes: ON"/*nonNLS*/);
	} else {
		buf.append("\n-local variables debug attributes: OFF"/*nonNLS*/);
	}
	if ((produceDebugAttributes & Lines) != 0){
		buf.append("\n-line number debug attributes: ON"/*nonNLS*/);
	} else {
		buf.append("\n-line number debug attributes: OFF"/*nonNLS*/);
	}
	if ((produceDebugAttributes & Source) != 0){
		buf.append("\n-source debug attributes: ON"/*nonNLS*/);
	} else {
		buf.append("\n-source debug attributes: OFF"/*nonNLS*/);
	}
	if (preserveAllLocalVariables){
		buf.append("\n-preserve all local variables: ON"/*nonNLS*/);
	} else {
		buf.append("\n-preserve all local variables: OFF"/*nonNLS*/);
	}
	if ((errorThreshold & UnreachableCode) != 0){
		buf.append("\n-unreachable code: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & UnreachableCode) != 0){
			buf.append("\n-unreachable code: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-unreachable code: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & ImportProblem) != 0){
		buf.append("\n-import problem: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & ImportProblem) != 0){
			buf.append("\n-import problem: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-import problem: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & MethodWithConstructorName) != 0){
		buf.append("\n-method with constructor name: ERROR"/*nonNLS*/);		
	} else {
		if ((warningThreshold & MethodWithConstructorName) != 0){
			buf.append("\n-method with constructor name: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-method with constructor name: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & OverriddenPackageDefaultMethod) != 0){
		buf.append("\n-overridden package default method: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & OverriddenPackageDefaultMethod) != 0){
			buf.append("\n-overridden package default method: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-overridden package default method: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & UsingDeprecatedAPI) != 0){
		buf.append("\n-deprecation: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & UsingDeprecatedAPI) != 0){
			buf.append("\n-deprecation: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-deprecation: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & MaskedCatchBlock) != 0){
		buf.append("\n-masked catch block: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & MaskedCatchBlock) != 0){
			buf.append("\n-masked catch block: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-masked catch block: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & UnusedLocalVariable) != 0){
		buf.append("\n-unused local variable: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & UnusedLocalVariable) != 0){
			buf.append("\n-unused local variable: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-unused local variable: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & UnusedArgument) != 0){
		buf.append("\n-unused parameter: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & UnusedArgument) != 0){
			buf.append("\n-unused parameter: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-unused parameter: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & AccessEmulation) != 0){
		buf.append("\n-synthetic access emulation: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & AccessEmulation) != 0){
			buf.append("\n-synthetic access emulation: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-synthetic access emulation: IGNORE"/*nonNLS*/);
		}
	}
	if ((errorThreshold & NonExternalizedString) != 0){
		buf.append("\n-non externalized string: ERROR"/*nonNLS*/);
	} else {
		if ((warningThreshold & NonExternalizedString) != 0){
			buf.append("\n-non externalized string: WARNING"/*nonNLS*/);
		} else {
			buf.append("\n-non externalized string: IGNORE"/*nonNLS*/);
		}
	}
	switch(targetJDK){
		case JDK1_1 :
			buf.append("\n-target JDK: 1.1"/*nonNLS*/);
			break;
		case JDK1_2 :
			buf.append("\n-target JDK: 1.2"/*nonNLS*/);
	}
	buf.append("\n-verbose : "/*nonNLS*/ + (verbose ? "ON"/*nonNLS*/ : "OFF"/*nonNLS*/));
	buf.append("\n-produce reference info : "/*nonNLS*/ + (produceReferenceInfo ? "ON"/*nonNLS*/ : "OFF"/*nonNLS*/));
	buf.append("\n-parse literal expressions as constants : "/*nonNLS*/ + (parseLiteralExpressionsAsConstants ? "ON"/*nonNLS*/ : "OFF"/*nonNLS*/));
	buf.append("\n-runtime exception name for compile error : "/*nonNLS*/ + runtimeExceptionNameForCompileError);
	return buf.toString();
}
}

package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.internal.codeassist.CompletionEngine;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.core.*;

public class CompletionRequestorWrapper implements ICompletionRequestor {
	static final char[] ARG = "arg".toCharArray();  //$NON-NLS-1$
	
	ICompletionRequestor clientRequestor;
	NameLookup nameLookup;
	
public CompletionRequestorWrapper(ICompletionRequestor clientRequestor, NameLookup nameLookup){
	this.clientRequestor = clientRequestor;
	this.nameLookup = nameLookup;
}
public void acceptAnonymousType(char[] superTypePackageName,char[] superTypeName,char[][] parameterPackageNames,char[][] parameterTypeNames,char[][] parameterNames,char[] completionName,int modifiers,int completionStart,int completionEnd){
	if(parameterNames == null)
		parameterNames = findMethodParameterNames(superTypePackageName, superTypeName, superTypeName, parameterPackageNames, parameterTypeNames);

	if(CompletionEngine.DEBUG) {
		printDebug("acceptAnonymousType",  new String[]{
			String.valueOf(superTypePackageName),
			String.valueOf(superTypeName),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptAnonymousType(superTypePackageName, superTypeName, parameterPackageNames, parameterTypeNames, parameterNames, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptClass(char[] packageName, char[] className, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptClass",  new String[]{
			String.valueOf(packageName),
			String.valueOf(className),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptClass(packageName, className, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptError(IMarker problemMarker) {
	
	if(CompletionEngine.DEBUG) {
		System.out.print("COMPLETION - acceptError(");
		System.out.print(problemMarker);
		System.out.println(")");
	}
	this.clientRequestor.acceptError(problemMarker);
}
/**
 * See ICompletionRequestor
 */
public void acceptField(char[] declaringTypePackageName, char[] declaringTypeName, char[] name, char[] typePackageName, char[] typeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptField",  new String[]{
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(name),
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptField(declaringTypePackageName, declaringTypeName, name, typePackageName, typeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptInterface(char[] packageName, char[] interfaceName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptInterface",  new String[]{
			String.valueOf(packageName),
			String.valueOf(interfaceName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptInterface(packageName, interfaceName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptKeyword(char[] keywordName, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptKeyword",  new String[]{
			String.valueOf(keywordName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptKeyword(keywordName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptLabel(char[] labelName, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptLabel",  new String[]{
			String.valueOf(labelName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptLabel(labelName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptLocalVariable(char[] name, char[] typePackageName, char[] typeName, int modifiers, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptLocalVariable",  new String[]{
			String.valueOf(name),
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptLocalVariable(name, typePackageName, typeName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethod(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	if(parameterNames == null)
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);

	if(CompletionEngine.DEBUG) {
		printDebug("acceptMethod",  new String[]{
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(selector),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(returnTypePackageName),
			String.valueOf(returnTypeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptMethod(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptMethodDeclaration(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames, char[][] parameterNames, char[] returnTypePackageName, char[] returnTypeName, char[] completionName, int modifiers, int completionStart, int completionEnd) {
	if(parameterNames == null) {
		int length = parameterTypeNames.length;
		
		parameterNames = findMethodParameterNames(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames);
		
		StringBuffer completion = new StringBuffer(completionName.length);
			
		int start = 0;
		int end = CharOperation.indexOf('%', completionName);

		completion.append(CharOperation.subarray(completionName, start, end));
		
		for(int i = 0 ; i < length ; i++){
			completion.append(parameterNames[i]);
			start = end + 1;
			end = CharOperation.indexOf('%', completionName, start);
			if(end > -1){
				completion.append(CharOperation.subarray(completionName, start, end));
			} else {
				completion.append(CharOperation.subarray(completionName, start, completionName.length));
			}
		}
		
		completionName = completion.toString().toCharArray();
	}	
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptMethodDeclaration",  new String[]{
			String.valueOf(declaringTypePackageName),
			String.valueOf(declaringTypeName),
			String.valueOf(selector),
			String.valueOf(parameterPackageNames),
			String.valueOf(parameterTypeNames),
			String.valueOf(parameterNames),
			String.valueOf(returnTypePackageName),
			String.valueOf(returnTypeName),
			String.valueOf(completionName),
			String.valueOf(modifiers),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptMethodDeclaration(declaringTypePackageName, declaringTypeName, selector, parameterPackageNames, parameterTypeNames, parameterNames, returnTypePackageName, returnTypeName, completionName, modifiers, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptModifier(char[] modifierName, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptModifier",  new String[]{
			String.valueOf(modifierName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptModifier(modifierName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptPackage(char[] packageName, char[] completionName, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptPackage",  new String[]{
			String.valueOf(packageName),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptPackage(packageName, completionName, completionStart, completionEnd);
}
/**
 * See ICompletionRequestor
 */
public void acceptType(char[] packageName, char[] typeName, char[] completionName, int completionStart, int completionEnd) {
	
	if(CompletionEngine.DEBUG) {
		printDebug("acceptType",  new String[]{
			String.valueOf(packageName),
			String.valueOf(typeName),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptType(packageName, typeName, completionName, completionStart, completionEnd);
}
public void acceptVariableName(char[] typePackageName, char[] typeName, char[] name, char[] completionName, int completionStart, int completionEnd){
	
	if(CompletionEngine.DEBUG) {
		System.out.println("COMPLETION - acceptVariableName");
		printDebug("acceptVariableName",  new String[]{
			String.valueOf(typePackageName),
			String.valueOf(typeName),
			String.valueOf(name),
			String.valueOf(completionName),
			String.valueOf(completionStart),
			String.valueOf(completionEnd)
		});
	}
	this.clientRequestor.acceptVariableName(typePackageName, typeName, name, completionName, completionStart, completionEnd);
}
private char[][] findMethodParameterNames(char[] declaringTypePackageName, char[] declaringTypeName, char[] selector, char[][] parameterPackageNames, char[][] parameterTypeNames){
	char[][] parameterNames = null;
	int length = parameterTypeNames.length;
	
	char[] typeName = CharOperation.concat(declaringTypePackageName,declaringTypeName,'.');
	IType type = nameLookup.findType(new String(typeName), false, NameLookup.ACCEPT_CLASSES & NameLookup.ACCEPT_INTERFACES);
	if(type instanceof BinaryType){
		String[] args = new String[length];
		for(int i = 0;	i< length ; i++){
			char[] parameterType = CharOperation.concat(parameterPackageNames[i],parameterTypeNames[i],'.');
			args[i] = Signature.createTypeSignature(parameterType,true);
		}
		IMethod method = type.getMethod(new String(selector),args);
		try{
			parameterNames = new char[length][];
			String[] params = method.getParameterNames();
			for(int i = 0;	i< length ; i++){
				parameterNames[i] = params[i].toCharArray();
			}
		} catch(JavaModelException e){
			parameterNames = null;
		}
			
	}
	// default parameters name
	if(parameterNames == null) {
		parameterNames = new char[length][];
		for (int i = 0; i < length; i++) {
			parameterNames[i] = CharOperation.concat(ARG, String.valueOf(i).toCharArray());
		}
	}
	return parameterNames;
}

private void printDebug(String header, String[] param){
	StringBuffer buffer = new StringBuffer();
	buffer.append("COMPLETION - ");
	buffer.append(header);
	buffer.append("(");
	
	for (int i = 0; i < param.length; i++) {
		if(i != 0)
			buffer.append(", ");
		buffer.append(param[i]);
	}

	buffer.append(")");
	System.out.println(buffer.toString());
}
}

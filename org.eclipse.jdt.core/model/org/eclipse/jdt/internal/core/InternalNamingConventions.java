/*******************************************************************************
 * Copyright (c) 2003 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.Map;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaConventions;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

public class InternalNamingConventions {
	private static final char[] DEFAULT_NAME = "name".toCharArray(); //$NON-NLS-1$
	
	private static Scanner getNameScanner(CompilerOptions compilerOptions) {
		return
			new Scanner(
				false /*comment*/, 
				false /*whitespace*/, 
				false /*nls*/, 
				compilerOptions.sourceLevel >= CompilerOptions.JDK1_4 /*assert*/, 
				compilerOptions.complianceLevel >= CompilerOptions.JDK1_4 /*strict comment*/,
				null /*taskTags*/, 
				null/*taskPriorities*/);
	}
	public static void suggestArgumentNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames, INamingRequestor requestor) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	public static void suggestFieldNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, int modifiers, char[][] excludedNames, INamingRequestor requestor) {
		boolean isStatic = Flags.isStatic(modifiers);
		
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
			isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	public static void suggestLocalVariableNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames, INamingRequestor requestor) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		suggestNames(
			packageName,
			qualifiedTypeName,
			dim,
			assistOptions.localPrefixes,
			assistOptions.localSuffixes,
			excludedNames,
			getNameScanner(compilerOptions),
			requestor);
	}
	
	private static void suggestNames(
		char[] packageName,
		char[] qualifiedTypeName,
		int dim,
		char[][] prefixes,
		char[][] suffixes,
		char[][] excludedNames,
		Scanner nameScanner,
		INamingRequestor requestor){
		
		if(qualifiedTypeName == null || qualifiedTypeName.length == 0)
			return;
		
		char[] typeName = CharOperation.lastSegment(qualifiedTypeName, '.');
	
		if(prefixes == null || prefixes.length == 0) {
			prefixes = new char[1][0];
		} else {
			int length = prefixes.length;
			System.arraycopy(prefixes, 0, prefixes = new char[length+1][], 0, length);
			prefixes[length] = CharOperation.NO_CHAR;
		}
	
		if(suffixes == null || suffixes.length == 0) {
			suffixes = new char[1][0];
		} else {
			int length = suffixes.length;
			System.arraycopy(suffixes, 0, suffixes = new char[length+1][], 0, length);
			suffixes[length] = CharOperation.NO_CHAR;
		}
	
		char[][] tempNames = null;
	
		// compute variable name for base type
		try{
			nameScanner.setSource(typeName);
			switch (nameScanner.getNextToken()) {
				case TerminalTokens.TokenNameint :
				case TerminalTokens.TokenNamebyte :
				case TerminalTokens.TokenNameshort :
				case TerminalTokens.TokenNamechar :
				case TerminalTokens.TokenNamelong :
				case TerminalTokens.TokenNamefloat :
				case TerminalTokens.TokenNamedouble :
				case TerminalTokens.TokenNameboolean :	
					char[] name = computeBaseNames(typeName[0], prefixes, excludedNames);
					if(name != null) {
						tempNames =  new char[][]{name};
					}
					break;
			}	
		} catch(InvalidInputException e){
		}

		// compute variable name for non base type
		if(tempNames == null) {
			tempNames = computeNames(typeName);
		}
	
		boolean acceptDefaultName = true;
		
		for (int i = 0; i < tempNames.length; i++) {
			char[] tempName = tempNames[i];
			if(dim > 0) {
				int length = tempName.length;
				if (tempName[length-1] == 's'){
					System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
					tempName[length] = 'e';
					tempName[length+1] = 's';
				} else if(tempName[length-1] == 'y') {
					System.arraycopy(tempName, 0, tempName = new char[length + 2], 0, length);
					tempName[length-1] = 'i';
					tempName[length] = 'e';
					tempName[length+1] = 's';
				} else {
					System.arraycopy(tempName, 0, tempName = new char[length + 1], 0, length);
					tempName[length] = 's';
				}
			}
		
			for (int j = 0; j < prefixes.length; j++) {
				if(prefixes[j].length > 0
					&& Character.isLetterOrDigit(prefixes[j][prefixes[j].length - 1])) {
					tempName[0] = Character.toUpperCase(tempName[0]);
				} else {
					tempName[0] = Character.toLowerCase(tempName[0]);
				}
				char[] prefixName = CharOperation.concat(prefixes[j], tempName);
				for (int k = 0; k < suffixes.length; k++) {
					char[] suffixName = CharOperation.concat(prefixName, suffixes[k]);
					suffixName =
						excludeNames(
							suffixName,
							prefixName,
							suffixes[k],
							excludedNames);
					if(JavaConventions.validateFieldName(new String(suffixName)).isOK()) {
						acceptName(suffixName, prefixes[j], suffixes[k], requestor);
						acceptDefaultName = false;
					} else {
						suffixName = CharOperation.concat(
							prefixName,
							String.valueOf(1).toCharArray(),
							suffixes[k]
						);
						suffixName =
							excludeNames(
								suffixName,
								prefixName,
								suffixes[k],
								excludedNames);
						if(JavaConventions.validateFieldName(new String(suffixName)).isOK()) {
							acceptName(suffixName, prefixes[j], suffixes[k], requestor);
							acceptDefaultName = false;
						}
					}
				}
			
			}
		}
		// if no names were found
		if(acceptDefaultName) {
			char[] name = excludeNames(DEFAULT_NAME, DEFAULT_NAME, CharOperation.NO_CHAR, excludedNames);
			requestor.acceptNameWithoutPrefixAndSuffix(name);
		}
	}
	
	private static void acceptName(char[] name, char[] prefix, char[] suffix, INamingRequestor requestor) {
		if(prefix.length > 0 && suffix.length > 0) {
			requestor.acceptNameWithPrefixAndSuffix(name);
		} else if(prefix.length > 0){
			requestor.acceptNameWithPrefix(name);
		} else if(suffix.length > 0){
			requestor.acceptNameWithSuffix(name);
		} else {
			requestor.acceptNameWithoutPrefixAndSuffix(name);
		}
	}
	

	private static char[] computeBaseNames(char firstName, char[][] prefixes, char[][] excludedNames){
		char[] name = new char[]{firstName};
		
		for(int i = 0 ; i < excludedNames.length ; i++){
			if(CharOperation.equals(name, excludedNames[i], false)) {
				name[0]++;
				if(name[0] > 'z')
					name[0] = 'a';
				if(name[0] == firstName)
					return null;
				i = 0;
			}	
		}
		
		return name;
	}
	
	private static char[][] computeNames(char[] sourceName){
		char[][] names = new char[5][];
		int nameCount = 0;
		boolean previousIsUpperCase = false;
		boolean previousIsLetter = true;
		for(int i = sourceName.length - 1 ; i >= 0 ; i--){
			boolean isUpperCase = Character.isUpperCase(sourceName[i]);
			boolean isLetter = Character.isLetter(sourceName[i]);
			if(isUpperCase && !previousIsUpperCase && previousIsLetter){
				char[] name = CharOperation.subarray(sourceName,i,sourceName.length);
				if(name.length > 1){
					if(nameCount == names.length) {
						System.arraycopy(names, 0, names = new char[nameCount * 2][], 0, nameCount);
					}
					name[0] = Character.toLowerCase(name[0]);
					names[nameCount++] = name;
				}
			}
			previousIsUpperCase = isUpperCase;
			previousIsLetter = isLetter;
		}
		if(nameCount == 0){
			names[nameCount++] = CharOperation.toLowerCase(sourceName);				
		}
		System.arraycopy(names, 0, names = new char[nameCount][], 0, nameCount);
		return names;
	}

	private static char[] excludeNames(
		char[] suffixName,
		char[] prefixName,
		char[] suffix,
		char[][] excludedNames) {
		int count = 2;
		int m = 0;
		while (m < excludedNames.length) {
			if(CharOperation.equals(suffixName, excludedNames[m], false)) {
				suffixName = CharOperation.concat(
					prefixName,
					String.valueOf(count++).toCharArray(),
					suffix
				);
				m = 0;
			} else {
				m++;
			}
		}
		return suffixName;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core;

import java.util.Map;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.codeassist.impl.AssistOptions;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Provides methods for computing Java-specific names.
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 * 
 * @since 2.1
 */
public final class NamingConventions {
	private static final char[] DEFAULT_NAME = "name".toCharArray(); //$NON-NLS-1$
	
	private static final char[] GETTER_BOOL_NAME = "is".toCharArray(); //$NON-NLS-1$
	private static final char[] GETTER_NAME = "get".toCharArray(); //$NON-NLS-1$
	private static final char[] SETTER_NAME = "set".toCharArray(); //$NON-NLS-1$
	
	/**
	 * Not instantiable.
	 */
	private NamingConventions() {}
	
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
	
	private static char[] removePrefixAndSuffix(char[] name, char[][] prefixes, char[][] suffixes) {
		// remove longer prefix
		char[] withoutPrefixName = name;
		if (prefixes != null) {
			int bestLength = 0;
			for (int i= 0; i < prefixes.length; i++) {
				char[] prefix = prefixes[i];
				if (CharOperation.startsWith(name, prefix)) {
					int currLen = prefix.length;
					if (bestLength < currLen && name.length != currLen) {
						withoutPrefixName = CharOperation.subarray(name, currLen, name.length);
						bestLength = currLen;
					}
				}
			}
		}
		
		// remove longer suffix
		char[] withoutSuffixName = withoutPrefixName;
		if(suffixes != null) {
			int bestLength = 0;
			for (int i = 0; i < suffixes.length; i++) {
				char[] suffix = suffixes[i];
				if(CharOperation.endsWith(withoutPrefixName, suffix)) {
					int currLen = suffix.length;
					if(bestLength < currLen && withoutPrefixName.length != currLen) {
						withoutSuffixName = CharOperation.subarray(withoutPrefixName, 0, withoutPrefixName.length - currLen);
						bestLength = currLen;
					}
				}
			}
		}
		
		withoutSuffixName[0] = Character.toLowerCase(withoutSuffixName[0]);
		return withoutSuffixName;
	}

	/**
	 * Remove prefix and suffix from an argument name.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return char[] the name without prefix and suffix.
	 */
	public static char[] removePrefixAndSuffixForArgumentName(IJavaProject javaProject, char[] argumentName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			argumentName,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from an argument name.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param argumentName argument's name.
	 * @return String the name without prefix and suffix.
	 */
	public static String removePrefixAndSuffixForArgumentName(IJavaProject javaProject, String argumentName) {
		return String.valueOf(removePrefixAndSuffixForArgumentName(javaProject, argumentName.toCharArray()));
	}

	/**
	 * Remove prefix and suffix from a field name.
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers.
	 * @return char[] the name without prefix and suffix.
	 */
	public static char[] removePrefixAndSuffixForFieldName(IJavaProject javaProject, char[] fieldName, int modifiers) {
		boolean isStatic = Flags.isStatic(modifiers);
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			fieldName,
			isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
			isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes);
	}

	/**
	 * Remove prefix and suffix from a field name.
	 * 
	 * @param javaProject project which contains the field.
	 * @param fieldName field's name.
	 * @param modifiers field's modifiers.
	 * @return String the name without prefix and suffix.
	 */
	public static String removePrefixAndSuffixForFieldName(IJavaProject javaProject, String fieldName, int modifiers) {
		return String.valueOf(removePrefixAndSuffixForFieldName(javaProject, fieldName.toCharArray(), modifiers));
	}
	/**
	 * Remove prefix and suffix from a local variable name.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return char[] the name without prefix and suffix.
	 */
	public static char[] removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, char[] localName) {
		AssistOptions assistOptions = new AssistOptions(javaProject.getOptions(true));
		return	removePrefixAndSuffix(
			localName,
			assistOptions.argumentPrefixes,
			assistOptions.argumentSuffixes);
	}
	
	/**
	 * Remove prefix and suffix from a local variable name.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param localName variable's name.
	 * @return String the name without prefix and suffix.
	 */
	public static String removePrefixAndSuffixForLocalVariableName(IJavaProject javaProject, String localName) {
		return String.valueOf(removePrefixAndSuffixForLocalVariableName(javaProject, localName.toCharArray()));
	}
	/**
	 * Suggest names for an argument. The name is computed from argument's type.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 */
	public static char[][] suggestArgumentNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				assistOptions.argumentPrefixes,
				assistOptions.argumentSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}
	
	/**
	 * Suggest names for an argument. The name is computed from argument's type.
	 * 
	 * @param javaProject project which contains the argument.
	 * @param packageName package of the argument's type.
	 * @param qualifiedTypeName argument's type.
	 * @param dim argument's dimension (0 if the argument is not an array).
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return String[] an array of names.
	 */
	public static String[] suggestArgumentNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestArgumentNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	/**
	 * Suggest names for a field. The name is computed from field's type.
	 * 
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers.
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 */
	public static char[][] suggestFieldNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, int modifiers, char[][] excludedNames) {
		boolean isStatic = Flags.isStatic(modifiers);
		
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				isStatic ? assistOptions.staticFieldPrefixes : assistOptions.fieldPrefixes,
				isStatic ? assistOptions.staticFieldSuffixes : assistOptions.fieldSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}

	/**
	 * Suggest names for a field. The name is computed from field's type.
	 * 
	 * @param javaProject project which contains the field.
	 * @param packageName package of the field's type.
	 * @param qualifiedTypeName field's type.
	 * @param dim field's dimension (0 if the field is not an array).
	 * @param modifiers field's modifiers.
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return String[] an array of names.
	 */
	public static String[] suggestFieldNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, int modifiers, String[] excludedNames) {
		return convertCharsToString(
			suggestFieldNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				modifiers,
				convertStringToChars(excludedNames)));
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[][] an array of names.
	 */
	public static char[][] suggestLocalVariableNames(IJavaProject javaProject, char[] packageName, char[] qualifiedTypeName, int dim, char[][] excludedNames) {
		Map options = javaProject.getOptions(true);
		CompilerOptions compilerOptions = new CompilerOptions(options);
		AssistOptions assistOptions = new AssistOptions(options);

		return
			suggestNames(
				packageName,
				qualifiedTypeName,
				dim,
				assistOptions.localPrefixes,
				assistOptions.localSuffixes,
				excludedNames,
				getNameScanner(compilerOptions));
	}
	
	/**
	 * Suggest names for a local variable. The name is computed from variable's type.
	 * 
	 * @param javaProject project which contains the variable.
	 * @param packageName package of the variable's type.
	 * @param qualifiedTypeName variable's type.
	 * @param dim variable's dimension (0 if the variable is not an array).
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return String[] an array of names.
	 */
	public static String[] suggestLocalVariableNames(IJavaProject javaProject, String packageName, String qualifiedTypeName, int dim, String[] excludedNames) {
		return convertCharsToString(
			suggestLocalVariableNames(
				javaProject,
				packageName.toCharArray(),
				qualifiedTypeName.toCharArray(),
				dim,
				convertStringToChars(excludedNames)));
	}
	
	private static char[][] suggestNames(
		char[] packageName,
		char[] qualifiedTypeName,
		int dim,
		char[][] prefixes,
		char[][] suffixes,
		char[][] excludedNames,
		Scanner nameScanner){
			
		if(qualifiedTypeName == null || qualifiedTypeName.length == 0)
			return CharOperation.NO_CHAR_CHAR;
			
		char[] typeName = CharOperation.lastSegment(qualifiedTypeName, '.');
		
		if(prefixes == null || prefixes.length == 0) {
			prefixes = new char[1][0];
		}
		if(suffixes == null || suffixes.length == 0) {
			suffixes = new char[1][0];
		}
		
		char[][] names = new char[5][];
		int namesCount = 0;
		
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
					int count;
					int m;
					suffixName =
						excludeNames(
							suffixName,
							prefixName,
							suffixes[k],
							excludedNames);
					if(JavaConventions.validateFieldName(new String(suffixName)).isOK()) {
						names[namesCount++] = suffixName;
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
							names[namesCount++] = suffixName;
						}
					}
					if(namesCount == names.length) {
						System.arraycopy(names, 0, names = new char[namesCount * 2][], 0, namesCount);
					}
				}
				
			}
		}
		System.arraycopy(names, 0, names = new char[namesCount][], 0, namesCount);
		
		// if no names were found
		if(names.length == 0) {
			names = new char[][]{excludeNames(DEFAULT_NAME, DEFAULT_NAME, CharOperation.NO_CHAR, excludedNames)};
		}
		return names;
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name.
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 */
	public static char[] suggestGetterName(IJavaProject project, char[] fieldName, int modifiers, boolean isBoolean, char[][] excludedNames) {
		if (isBoolean) {
			char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
			int prefixLen =  GETTER_BOOL_NAME.length;
			if (CharOperation.startsWith(name, GETTER_BOOL_NAME) 
				&& name.length > prefixLen && Character.isUpperCase(name[prefixLen])) {
				return suggestNewName(name, excludedNames);
			} else {
				return suggestNewName(
					CharOperation.concat(GETTER_BOOL_NAME, suggestAccessorName(project, fieldName, modifiers)),
					excludedNames
				);
			}
		} else {
			return suggestNewName(
				CharOperation.concat(GETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
				excludedNames
			);
		}
	}
	
	/**
	 * Suggest name for a getter method. The name is computed from field's name.
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers.
	 * @param isBoolean <code>true</code> if the field's type is boolean
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return String a name.
	 */
	public static String suggestGetterName(IJavaProject project, String fieldName, int modifiers, boolean isBoolean, String[] excludedNames) {
		return String.valueOf(
			suggestGetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				isBoolean,
				convertStringToChars(excludedNames)));
	}
	/**
	 * Suggest name for a setter method. The name is computed from field's name.
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers.
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return char[] a name.
	 */
	public static char[] suggestSetterName(IJavaProject project, char[] fieldName, int modifiers, char[][] excludedNames) {
		return suggestNewName(
			CharOperation.concat(SETTER_NAME, suggestAccessorName(project, fieldName, modifiers)),
			excludedNames
		);
	}
	
	/**
	 * Suggest name for a setter method. The name is computed from field's name.
	 * 
	 * @param project project which contains the field.
	 * @param fieldName field's name's.
	 * @param modifiers field's modifiers.
	 * @param excludedNames a list of names which can not be suggest (already use names).
	 *         Can be <code>null</code> if there is no excluded names.
	 * @return String a name.
	 */
	public static String suggestSetterName(IJavaProject project, String fieldName, int modifiers, String[] excludedNames) {
		return String.valueOf(
			suggestSetterName(
				project,
				fieldName.toCharArray(),
				modifiers,
				convertStringToChars(excludedNames)));
	}
	
	private static char[] suggestAccessorName(IJavaProject project, char[] fieldName, int modifiers) {
		char[] name = removePrefixAndSuffixForFieldName(project, fieldName, modifiers);
		if (name.length > 0 && Character.isLowerCase(name[0])) {
			name[0] = Character.toUpperCase(name[0]);
		}
		return name;
	}
	
	private static char[] suggestNewName(char[] name, char[][] excludedNames){
		if(excludedNames == null) {
			return name;
		}
		
		char[] newName = name;
		int count = 2;
		int i = 0;
		while (i < excludedNames.length) {
			if(CharOperation.equals(newName, excludedNames[i], false)) {
				newName = CharOperation.concat(name, String.valueOf(count++).toCharArray());
				i = 0;
			} else {
				i++;
			}
		}
		return newName;
	}
	
	private static String[] convertCharsToString(char[][] c) {
		int length = c == null ? 0 : c.length;
		String[] s = new String[length];
		for (int i = 0; i < length; i++) {
			s[i] = String.valueOf(c[i]);
		}
		return s;
	}
	
	private static char[][] convertStringToChars(String[] s) {
		int length = s == null ? 0 : s.length;
		char[][] c = new char[length][];
		for (int i = 0; i < length; i++) {
			if(s[i] == null) {
				c[i] = CharOperation.NO_CHAR;
			} else {
				c[i] = s[i].toCharArray();
			}
		}
		return c;
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

/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.compiler.ExtraFlags;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class MethodDeclarationPattern extends MethodPattern {

	public int extraFlags;
	public int declaringTypeModifiers;
	
	public int modifiers;
	public char[] signature;
	public char[][] parameterTypes;
	public char[][] parameterNames;
	/**
	 * Method Declaration entries are encoded as described
	 * 
	 * Binary Method Declaration for class
	 * MethodName '/' Arity '/' DeclaringQualifier '/' TypeName '/' TypeModifers '/' PackageName '/' Signature '/' ParameterNamesopt '/' Modifiers '/' returnType
	 * Source method for class
	 * MethodName '/' Arity '/' DeclaringQualifier '/' TypeName '/' TypeModifers '/' PackageName '/' ParameterTypes '/' ParameterNamesopt '/' Modifiers '/' returnType
	 * TypeModifiers contains some encoded extra information
	 * 		{@link ExtraFlags#IsMemberType}
	 * 		{@link ExtraFlags#HasNonPrivateStaticMemberTypes}
	 * 		{@link ExtraFlags#ParameterTypesStoredAsSignature}
	 */
	public static char[] createDeclarationIndexKey(
			char[] typeName,
			char[] declaringQualification,
			char[] methodName,
			int argCount,
			char[] signature,
			char[][] parameterTypes,
			char[][] parameterNames,
			char[] returnType,
			int modifiers,
			char[] packageName,
			int typeModifiers,
			int extraFlags) {
		
		char[] countChars;
		char[] parameterTypesChars = null;
		char[] parameterNamesChars = null;
		
		
		countChars = argCount < 10 ? COUNTS[argCount]: ("/" + String.valueOf(argCount)).toCharArray(); //$NON-NLS-1$
		if (argCount > 0) {
			if (signature == null) {
				if (parameterTypes != null && parameterTypes.length == argCount) {
					char[][] parameterTypeErasures = new char[argCount][];
					for (int i = 0; i < parameterTypes.length; i++) {
						parameterTypeErasures[i] = getTypeErasure(parameterTypes[i]);
					}
					parameterTypesChars = CharOperation.concatWith(parameterTypeErasures, PARAMETER_SEPARATOR);
				}
			} else {
				extraFlags |= ExtraFlags.ParameterTypesStoredAsSignature;
			}
			
			if (parameterNames != null && parameterNames.length == argCount) {
				parameterNamesChars = CharOperation.concatWith(parameterNames, PARAMETER_SEPARATOR);
			}
		}
				
		char[] returnTypeChars = returnType == null ? CharOperation.NO_CHAR : getTypeErasure(returnType);
		
		
		int typeNameLength = typeName == null ? 0 : typeName.length;
		int qualifierLength = declaringQualification == null ? 0 : declaringQualification.length;
		int methodNameLength = methodName == null ? 0 : methodName.length;
		int packageNameLength = packageName == null ? 0 : packageName.length;
		int countCharsLength = countChars.length;
		int parameterTypesLength = signature == null ? (parameterTypesChars == null ? 0 : parameterTypesChars.length): signature.length;
		int parameterNamesLength = parameterNamesChars == null ? 0 : parameterNamesChars.length;
		int returnTypeLength = returnTypeChars.length;
		
		int resultLength = methodNameLength + countCharsLength + qualifierLength + typeNameLength + 2 /* type modifiers */
				+ packageNameLength + parameterTypesLength + parameterNamesLength + returnTypeLength + 2 /* modifiers*/ + 9; // SEPARATOR = 9
		char[] result = new char[resultLength];
		
		int pos = 0;
		if (methodNameLength > 0) {
			System.arraycopy(methodName, 0, result, pos, methodNameLength);
			pos += methodNameLength;
		}
		if (countCharsLength > 0) {
			System.arraycopy(countChars, 0, result, pos, countCharsLength);
			pos += countCharsLength;
		}
		result[pos++] = SEPARATOR;
		if (qualifierLength > 0) {
			System.arraycopy(declaringQualification, 0, result, pos, qualifierLength);
			pos += qualifierLength;
		}
		result[pos++] = SEPARATOR;

		if (typeNameLength > 0) {
			System.arraycopy(typeName, 0, result, pos, typeNameLength);
			pos += typeNameLength;
		}

		
		int typeModifiersWithExtraFlags = typeModifiers | encodeExtraFlags(extraFlags);
		result[pos++] = SEPARATOR;
		result[pos++] = (char) typeModifiersWithExtraFlags;
		result[pos++] = (char) (typeModifiersWithExtraFlags>>16);
		
		result[pos++] = SEPARATOR;
		if (packageNameLength > 0) {
			System.arraycopy(packageName, 0, result, pos, packageNameLength);
			pos += packageNameLength;
		}
		
		if (argCount == 0) {
			result[pos++] = SEPARATOR;
			result[pos++] = SEPARATOR;
			result[pos++] = SEPARATOR;
		} else if (argCount > 0) {
			result[pos++] = SEPARATOR;
			if (parameterTypesLength > 0) {
				if (signature == null) {
					System.arraycopy(parameterTypesChars, 0, result, pos, parameterTypesLength);
				} else {
					System.arraycopy(CharOperation.replaceOnCopy(signature, SEPARATOR, '\\'), 0, result, pos, parameterTypesLength);
				}
				pos += parameterTypesLength;
			}
			
			result[pos++] = SEPARATOR;
			if (parameterNamesLength > 0) {
				System.arraycopy(parameterNamesChars, 0, result, pos, parameterNamesLength);
				pos += parameterNamesLength;
			}
			
			result[pos++] = SEPARATOR;
		}
		result[pos++] = (char) modifiers;
		result[pos++] = (char) (modifiers>>16);
		result[pos++] = SEPARATOR;

		if (returnTypeLength > 0) {
			System.arraycopy(returnTypeChars, 0, result, pos, returnTypeLength);
			pos += returnTypeLength;
		}
		result[pos++] = SEPARATOR;
		return result;
	}
	
	private static int encodeExtraFlags(int extraFlags) {
		int encodedExtraFlags = 0;
		
		if ((extraFlags & ExtraFlags.ParameterTypesStoredAsSignature) != 0) {
			encodedExtraFlags |= ASTNode.Bit28;
		}
		
		if ((extraFlags & ExtraFlags.IsLocalType) != 0) {
			encodedExtraFlags |= ASTNode.Bit29;
		}
		
		if ((extraFlags & ExtraFlags.IsMemberType) != 0) {
			encodedExtraFlags |= ASTNode.Bit30;
		}
		if ((extraFlags & ExtraFlags.HasNonPrivateStaticMemberTypes) != 0) {
			encodedExtraFlags |= ASTNode.Bit31;
		}
		
		return encodedExtraFlags;
	}
	private static char[] getTypeErasure(char[] typeName) {
		int index;
		if ((index = CharOperation.indexOf('<', typeName)) == -1) return typeName;
		
		int length = typeName.length;
		char[] typeErasurename = new char[length - 2];
		
		System.arraycopy(typeName, 0, typeErasurename, 0, index);
		
		int depth = 1;
		for (int i = index + 1; i < length; i++) {
			switch (typeName[i]) {
				case '<':
					depth++;
					break;
				case '>':
					depth--;
					break;
				default:
					if (depth == 0) {
						typeErasurename[index++] = typeName[i];
					}
					break;
			}
		}
		
		System.arraycopy(typeErasurename, 0, typeErasurename = new char[index], 0, index);
		return typeErasurename;
	}

public MethodDeclarationPattern(
		char[] declaringPackageName, 
		char[] declaringQualification, 
		char[] declaringSimpleName,
		char[] methodName,
		int matchRule) {
	super(methodName, declaringQualification, declaringSimpleName, 
			null, null, null, null, null, 
			IJavaSearchConstants.DECLARATIONS, matchRule);
	this.declaringPackageName = declaringPackageName;
}

public MethodDeclarationPattern(int matchRule) {
	super(matchRule);
}

public void decodeIndexKey(char[] key) {
	
	int start = 0;
	int slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.selector = CharOperation.subarray(key, start, slash);

	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	int last = slash - 1;
	
	this.parameterCount = 0;
	int power = 1;
	for (int i = last; i >= start; i--) {
		if (i == last) {
			this.parameterCount = key[i] - '0';
		} else {
			power *= 10;
			this.parameterCount += power * (key[i] - '0');
		}
	}

	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.declaringQualification = CharOperation.subarray(key, start, slash);
	
	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.declaringSimpleName = CharOperation.subarray(key, start, slash);
	
	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	last = slash - 1;
	int typeModifiersWithExtraFlags = key[last-1] + (key[last]<<16);
	this.declaringTypeModifiers = ConstructorPattern.decodeModifers(typeModifiersWithExtraFlags);
	this.extraFlags = ConstructorPattern.decodeExtraFlags(typeModifiersWithExtraFlags);
	
	// initialize optional fields
	this.declaringPackageName = null;
	this.modifiers = 0;
	this.signature = null;
	this.parameterTypes = null;
	this.parameterNames = null;
	
	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.declaringPackageName = CharOperation.subarray(key, start, slash);
	
	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	if (this.parameterCount == 0) {
		start = slash + 1;
		slash = CharOperation.indexOf(SEPARATOR, key, start); // skip parameter type/signature

		start = slash + 1;
		slash = CharOperation.indexOf(SEPARATOR, key, start); //skip parameter names

		this.modifiers = key[last-1] + (key[last]<<16);
	} else if (this.parameterCount > 0){

		boolean hasParameterStoredAsSignature = (this.extraFlags & ExtraFlags.ParameterTypesStoredAsSignature) != 0;
		if (hasParameterStoredAsSignature) {
			this.signature  = CharOperation.subarray(key, start, slash);
			CharOperation.replace(this.signature , '\\', SEPARATOR);
		} else {
			this.parameterTypes = CharOperation.splitOn(PARAMETER_SEPARATOR, key, start, slash);
		}
		start = slash + 1;
		slash = CharOperation.indexOf(SEPARATOR, key, start);
		
		if (slash != start) {
			this.parameterNames = CharOperation.splitOn(PARAMETER_SEPARATOR, key, start, slash);
		}
		
		start = slash + 1;
		slash = CharOperation.indexOf(SEPARATOR, key, start);
		last = slash - 1;
		
		this.modifiers = key[last-1] + (key[last]<<16);
	} else {
		this.modifiers = ClassFileConstants.AccPublic;
	}

	start = slash + 1;
	slash = CharOperation.indexOf(SEPARATOR, key, start);
	this.returnSimpleName = CharOperation.subarray(key, start, slash); //TODO : separate return qualified and simple names - currently stored together in simple name.

	removeInternalFlags(); // remove internal flags
}

	public SearchPattern getBlankPattern() {
		return new MethodDeclarationPattern(R_EXACT_MATCH | R_CASE_SENSITIVE);
	}

	public char[][] getIndexCategories() {
		return new char[][] { METHOD_DECL_PLUS };
	}

	private void removeInternalFlags() {
		this.extraFlags = this.extraFlags & ~ExtraFlags.ParameterTypesStoredAsSignature; // ParameterTypesStoredAsSignature is an internal flags only used to decode key
	}

}

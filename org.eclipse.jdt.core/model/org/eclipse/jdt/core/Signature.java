/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added J2SE 1.5 support
 *******************************************************************************/
package org.eclipse.jdt.core;

import org.eclipse.jdt.core.compiler.*;
/**
 * Provides methods for encoding and decoding type and method signature strings.
 * <p>
 * The syntax for a type signature is:
 * <pre>
 * typeSignature ::=
 *     "B"  // byte
 *   | "C"  // char
 *   | "D"  // double
 *   | "F"  // float
 *   | "I"  // int
 *   | "J"  // long
 *   | "S"  // short
 *   | "V"  // void
 *   | "Z"  // boolean
 *   | "T" + typeVariableName + ";" // type variable
 *   | "L" + binaryTypeName + optionalTypeArguments + ";"  // resolved named type (in compiled code)
 *   | "Q" + sourceTypeName + optionalTypeArguments + ";"  // unresolved named type (in source code)
 *   | "[" + typeSignature  // array of type denoted by typeSignature
 * optionalTypeArguments ::=
 *     "&lt;" + typeArgument+ + "&gt;" 
 *   |
 * typeArgument ::=
 *   | typeSignature
 *   | "*" 
 *   | "+" typeSignature
 *   | "-" typeSignature
 * </pre>
 * </p>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"[[I"</code> denotes <code>int[][]</code></li>
 *   <li><code>"Ljava.lang.String;"</code> denotes <code>java.lang.String</code> in compiled code</li>
 *   <li><code>"QString;"</code> denotes <code>String</code> in source code</li>
 *   <li><code>"Qjava.lang.String;"</code> denotes <code>java.lang.String</code> in source code</li>
 *   <li><code>"[QString;"</code> denotes <code>String[]</code> in source code</li>
 *   <li><code>"QMap&lt;QString;&ast;&gt;;"</code> denotes <code>Map&lt;String,?&gt;</code> in source code</li>
 *   <li><code>"Ljava.util.List&ltTV;&gt;;"</code> denotes <code>java.util.List&lt;V&gt;</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a method signature is: 
 * <pre>
 * methodSignature ::= 
 *    optionalFormalTypeParameters + "(" + paramTypeSignature* + ")" + returnTypeSignature + throwsSignature*
 * paramTypeSignature ::= typeSignature
 * returnTypeSignature ::= typeSignature
 * throwsSignature ::= "^" + typeSignature
 * optionalFormalTypeParameters ::=
 *     "&lt;" + formalTypeParameter+ + "&gt;" 
 *   |
 * </pre>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"()I"</code> denotes <code>int foo()</code></li>
 *   <li><code>"([Ljava.lang.String;)V"</code> denotes <code>void foo(java.lang.String[])</code> in compiled code</li>
 *   <li><code>"(QString;)QObject;"</code> denotes <code>Object foo(String)</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a format type parameter signature is:
 * <pre>
 * formalTypeParameter ::=
 *     typeVariableName + optionalClassBound + interfaceBound*
 * optionalClassBound ::=
 *     ":"
 *   | ":" + typeSignature
 * interfaceBound ::=
 *     ":" + typeSignature
 * </pre>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"X:"</code> denotes <code>X</code></li>
 *   <li><code>"X:QReader;"</code> denotes <code>X extends Reader</code> in compiled code</li>
 *   <li><code>"X:QReader;QSerializable;"</code> denotes <code>X extends Reader & Serializable</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * This class provides static methods and constants only; it is not intended to be
 * instantiated or subclassed by clients.
 * </p>
 */
public final class Signature {

	/**
	 * Character constant indicating the primitive type boolean in a signature.
	 * Value is <code>'Z'</code>.
	 */
	public static final char C_BOOLEAN 		= 'Z';

	/**
	 * Character constant indicating the primitive type byte in a signature.
	 * Value is <code>'B'</code>.
	 */
	public static final char C_BYTE 		= 'B';

	/**
	 * Character constant indicating the primitive type char in a signature.
	 * Value is <code>'C'</code>.
	 */
	public static final char C_CHAR 		= 'C';

	/**
	 * Character constant indicating the primitive type double in a signature.
	 * Value is <code>'D'</code>.
	 */
	public static final char C_DOUBLE 		= 'D';

	/**
	 * Character constant indicating the primitive type float in a signature.
	 * Value is <code>'F'</code>.
	 */
	public static final char C_FLOAT 		= 'F';

	/**
	 * Character constant indicating the primitive type int in a signature.
	 * Value is <code>'I'</code>.
	 */
	public static final char C_INT 			= 'I';
	
	/**
	 * Character constant indicating the semicolon in a signature.
	 * Value is <code>';'</code>.
	 */
	public static final char C_SEMICOLON 			= ';';

	/**
	 * Character constant indicating the colon in a signature.
	 * Value is <code>':'</code>.
	 * @since 3.0
	 */
	public static final char C_COLON 			= ':';

	/**
	 * Character constant indicating the primitive type long in a signature.
	 * Value is <code>'J'</code>.
	 */
	public static final char C_LONG			= 'J';
	
	/**
	 * Character constant indicating the primitive type short in a signature.
	 * Value is <code>'S'</code>.
	 */
	public static final char C_SHORT		= 'S';
	
	/**
	 * Character constant indicating result type void in a signature.
	 * Value is <code>'V'</code>.
	 */
	public static final char C_VOID			= 'V';
	
	/**
	 * Character constant indicating the start of a resolved type variable in a 
	 * signature. Value is <code>'T'</code>.
	 * @since 3.0
	 */
	public static final char C_TYPE_VARIABLE	= 'T';
	
	/**
	 * Character constant indicating a wildcard type argument 
	 * in a signature.
	 * Value is <code>'&ast;'</code>.
	 * @since 3.0
	 */
	public static final char C_STAR	= '*';
	
	/**
	 * Character constant indicating the start of a bounded wildcard type argument
	 * in a signature.
	 * Value is <code>'+'</code>.
	 * @since 3.0
	 * TODO (jeem) - is this 'super' or 'extends' ?
	 */
	public static final char C_PLUS	= '+';
	
	/**
	 * Character constant indicating the start of a bounded wildcard type argument
	 * in a signature. Value is <code>'-'</code>.
	 * @since 3.0
	 * TODO (jeem) - is this 'super' or 'extends' ?
	 */
	public static final char C_MINUS	= '-';
	
	/**
	 * Character constant indicating the start of a thrown exception in a
	 * method signature. Value is <code>'^'</code>.
	 * @since 3.0
	 */
	public static final char C_THROWS	= '^';
	
	/** 
	 * Character constant indicating the dot in a signature. 
	 * Value is <code>'.'</code>.
	 */
	public static final char C_DOT			= '.';
	
	/** 
	 * Character constant indicating the dollar in a signature.
	 * Value is <code>'$'</code>.
	 */
	public static final char C_DOLLAR			= '$';

	/** 
	 * Character constant indicating an array type in a signature.
	 * Value is <code>'['</code>.
	 */
	public static final char C_ARRAY		= '[';

	/** 
	 * Character constant indicating the start of a resolved, named type in a 
	 * signature. Value is <code>'L'</code>.
	 */
	public static final char C_RESOLVED		= 'L';

	/** 
	 * Character constant indicating the start of an unresolved, named type in a
	 * signature. Value is <code>'Q'</code>.
	 */
	public static final char C_UNRESOLVED	= 'Q';

	/**
	 * Character constant indicating the end of a named type in a signature. 
	 * Value is <code>';'</code>.
	 */
	public static final char C_NAME_END		= ';';

	/**
	 * Character constant indicating the start of a parameter type list in a
	 * signature. Value is <code>'('</code>.
	 */
	public static final char C_PARAM_START	= '(';

	/**
	 * Character constant indicating the end of a parameter type list in a 
	 * signature. Value is <code>')'</code>.
	 */
	public static final char C_PARAM_END	= ')';

	/**
	 * Character constant indicating the start of a formal type parameter
	 * (or type argument) list in a signature. Value is <code>'&lt;'</code>.
	 * @since 3.0
	 */
	public static final char C_GENERIC_START	= '<';

	/**
	 * Character constant indicating the end of a generic type list in a 
	 * signature. Value is <code>'%gt;'</code>.
	 * @since 3.0
	 */
	public static final char C_GENERIC_END	= '>';

	/**
	 * String constant for the signature of the primitive type boolean.
	 * Value is <code>"Z"</code>.
	 */
	public static final String SIG_BOOLEAN 		= "Z"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type byte. 
	 * Value is <code>"B"</code>.
	 */
	public static final String SIG_BYTE 		= "B"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type char.
	 * Value is <code>"C"</code>.
	 */
	public static final String SIG_CHAR 		= "C"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type double.
	 * Value is <code>"D"</code>.
	 */
	public static final String SIG_DOUBLE 		= "D"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type float.
	 * Value is <code>"F"</code>.
	 */
	public static final String SIG_FLOAT 		= "F"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type int.
	 * Value is <code>"I"</code>.
	 */
	public static final String SIG_INT 			= "I"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type long.
	 * Value is <code>"J"</code>.
	 */
	public static final String SIG_LONG			= "J"; //$NON-NLS-1$

	/**
	 * String constant for the signature of the primitive type short.
	 * Value is <code>"S"</code>.
	 */
	public static final String SIG_SHORT		= "S"; //$NON-NLS-1$

	/** String constant for the signature of result type void.
	 * Value is <code>"V"</code>.
	 */
	public static final String SIG_VOID			= "V"; //$NON-NLS-1$
	
	private static final char[] BOOLEAN = {'b', 'o', 'o', 'l', 'e', 'a', 'n'};
	private static final char[] BYTE = {'b', 'y', 't', 'e'};
	private static final char[] CHAR = {'c', 'h', 'a', 'r'};
	private static final char[] DOUBLE = {'d', 'o', 'u', 'b', 'l', 'e'};
	private static final char[] FLOAT = {'f', 'l', 'o', 'a', 't'};
	private static final char[] INT = {'i', 'n', 't'};
	private static final char[] LONG = {'l', 'o', 'n', 'g'};
	private static final char[] SHORT = {'s', 'h', 'o', 'r', 't'};
	private static final char[] VOID = {'v', 'o', 'i', 'd'};
	
	private static final String EMPTY = new String(CharOperation.NO_CHAR);
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
		
private Signature() {
	// Not instantiable
}

private static boolean checkPrimitiveType(char[] primitiveTypeName, char[] typeName) {
	return CharOperation.fragmentEquals(primitiveTypeName, typeName, 0, true) &&
		(typeName.length == primitiveTypeName.length
		 || Character.isWhitespace(typeName[primitiveTypeName.length])
		 || typeName[primitiveTypeName.length] == C_ARRAY
		 || typeName[primitiveTypeName.length] == C_DOT);
}

private static long copyType(char[] signature, int sigPos, char[] dest, int index, boolean fullyQualifyTypeNames) {
	int arrayCount = 0;
	loop: while (true) {
		switch (signature[sigPos++]) {
			case C_ARRAY :
				arrayCount++;
				break;
			case C_BOOLEAN :
				int length = BOOLEAN.length;
				System.arraycopy(BOOLEAN, 0, dest, index, length);
				index += length;
				break loop;
			case C_BYTE :
				length = BYTE.length;
				System.arraycopy(BYTE, 0, dest, index, length);
				index += length;
				break loop;
			case C_CHAR :
				length = CHAR.length;
				System.arraycopy(CHAR, 0, dest, index, length);
				index += length;
				break loop;
			case C_DOUBLE :
				length = DOUBLE.length;
				System.arraycopy(DOUBLE, 0, dest, index, length);
				index += length;
				break loop;
			case C_FLOAT :
				length = FLOAT.length;
				System.arraycopy(FLOAT, 0, dest, index, length);
				index += length;
				break loop;
			case C_INT :
				length = INT.length;
				System.arraycopy(INT, 0, dest, index, length);
				index += length;
				break loop;
			case C_LONG :
				length = LONG.length;
				System.arraycopy(LONG, 0, dest, index, length);
				index += length;
				break loop;
			case C_SHORT :
				length = SHORT.length;
				System.arraycopy(SHORT, 0, dest, index, length);
				index += length;
				break loop;
			case C_VOID :
				length = VOID.length;
				System.arraycopy(VOID, 0, dest, index, length);
				index += length;
				break loop;
			case C_RESOLVED :
			case C_UNRESOLVED :
				int end = CharOperation.indexOf(C_SEMICOLON, signature, sigPos);
				if (end == -1) throw new IllegalArgumentException();
				int start;
				if (fullyQualifyTypeNames) {
					start = sigPos;
				} else {
					start = CharOperation.lastIndexOf(C_DOT, signature, sigPos, end)+1;
					if (start == 0) start = sigPos;
				} 
				length = end-start;
				System.arraycopy(signature, start, dest, index, length);
				sigPos = end+1;
				index += length;
				break loop;
		}
	}
	while (arrayCount-- > 0) {
		dest[index++] = '[';
		dest[index++] = ']';
	}
	return (((long) index) << 32) + sigPos;
}
/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 * 
 * @since 2.0
 */
public static char[] createArraySignature(char[] typeSignature, int arrayCount) {
	if (arrayCount == 0) return typeSignature;
	int sigLength = typeSignature.length;
	char[] result = new char[arrayCount + sigLength];
	for (int i = 0; i < arrayCount; i++) {
		result[i] = C_ARRAY;
	}
	System.arraycopy(typeSignature, 0, result, arrayCount, sigLength);
	return result;
}
/**
 * Creates a new type signature with the given amount of array nesting added 
 * to the given type signature.
 *
 * @param typeSignature the type signature
 * @param arrayCount the desired number of levels of array nesting
 * @return the encoded array type signature
 */
public static String createArraySignature(String typeSignature, int arrayCount) {
	return new String(createArraySignature(typeSignature.toCharArray(), arrayCount));
}
/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based. This method
 * is equivalent to
 * <code>createMethodSignature(new char[0][], parameterTypes, returnType, new char[0][])</code>.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 * @see #createMethodSignature(char[][], char[][], char[], char[][])
 * @since 2.0
 */
public static char[] createMethodSignature(char[][] parameterTypes, char[] returnType) {
	return createMethodSignature(parameterTypes, returnType, CharOperation.NO_CHAR_CHAR, CharOperation.NO_CHAR_CHAR);
}
/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @param formalTypeParameters the list of formal type parameter signatures
 * @param thrownExceptions the list of type signatures for thrown exceptions
 * @since 3.0
 */
public static char[] createMethodSignature(
		char[][] parameterTypes,
		char[] returnType,
		char[][] formalTypeParameters,
		char[][] thrownExceptions) {
	int formalTypeParameterCount = formalTypeParameters.length;
	int formalTypeLength = 0;
	for (int i = 0; i < formalTypeParameterCount; i++) {
		formalTypeLength += formalTypeParameters[i].length;
	}
	int parameterTypesCount = parameterTypes.length;
	int parameterLength = 0;
	for (int i = 0; i < parameterTypesCount; i++) {
		parameterLength += parameterTypes[i].length;
	}
	int returnTypeLength = returnType.length;
	int thrownExceptionsCount = thrownExceptions.length;
	int exceptionsLength = 0;
	for (int i = 0; i < thrownExceptionsCount; i++) {
		exceptionsLength += thrownExceptions[i].length;
	}
	int extras = 2; // "(" and ")"
	if (formalTypeParameterCount > 0) {
		extras += 2;   // "<" and ">"
	}
	if (thrownExceptionsCount > 0) {
		extras += thrownExceptionsCount;   // one "^" per
	}
	char[] result = new char[extras+ formalTypeLength + parameterLength + returnTypeLength + exceptionsLength];
	int index = 0;
	if (formalTypeParameterCount > 0) {
		result[index++] = C_GENERIC_START;
		for (int i = 0; i < formalTypeParameterCount; i++) {
			char[] formalTypeParameter = formalTypeParameters[i];
			int length = formalTypeParameter.length;
			System.arraycopy(formalTypeParameter, 0, result, index, length);
			index += length;
		}
		result[index++] = C_GENERIC_END;
	}
	result[index++] = C_PARAM_START;
	for (int i = 0; i < parameterTypesCount; i++) {
		char[] parameterType = parameterTypes[i];
		int length = parameterType.length;
		System.arraycopy(parameterType, 0, result, index, length);
		index += length;
	}
	result[index++] = C_PARAM_END;
	System.arraycopy(returnType, 0, result, index, returnTypeLength);
	index += returnTypeLength;
	if (thrownExceptionsCount > 0) {
		for (int i = 0; i < thrownExceptionsCount; i++) {
			result[index++] = C_THROWS;
			char[] thrownException = thrownExceptions[i];
			int length = thrownException.length;
			System.arraycopy(thrownException, 0, result, index, length);
			index += length;
		}
	}
	return result;
}

/**
 * Creates a method signature from the given parameter and return type 
 * signatures. The encoded method signature is dot-based. This method
 * is equivalent to
 * <code>createMethodSignature(new String[0], parameterTypes, returnType, new String[0])</code>.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 * @see Signature#createMethodSignature(String[], String[], String, String[])
 */
public static String createMethodSignature(String[] parameterTypes, String returnType) {
	return createMethodSignature(parameterTypes, returnType, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
}

/**
 * Creates a method signature. The encoded method signature is dot-based.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @param formalTypeParameters the list of formal type parameter signatures
 * @param thrownExceptions the list of type signatures for thrown exceptions
 * @return the encoded method signature
 * @since 3.0
 */
public static String createMethodSignature(
		String[] parameterTypes,
		String returnType,
		String[] formalTypeParameters,
		String[] thrownExceptions) {
	char[][] formalTypes = new char[formalTypeParameters.length][];
	for (int i = 0; i < formalTypeParameters.length; i++) {
		formalTypes[i] = formalTypeParameters[i].toCharArray();
	}
	char[][] parameters = new char[parameterTypes.length][];
	for (int i = 0; i < parameterTypes.length; i++) {
		parameters[i] = parameterTypes[i].toCharArray();
	}
	char[][] exceptionTypes = new char[thrownExceptions.length][];
	for (int i = 0; i < thrownExceptions.length; i++) {
		exceptionTypes[i] = thrownExceptions[i].toCharArray();
	}
	return new String(createMethodSignature(parameters, returnType.toCharArray(), formalTypes, exceptionTypes));
}


/**
 * Creates a new type signature from the given type name encoded as a character
 * array. This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved)</code>, although
 * more efficient for callers with character arrays rather than strings. If the 
 * type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 */
public static String createTypeSignature(char[] typeName, boolean isResolved) {
	return new String(createCharArrayTypeSignature(typeName, isResolved));
}
/**
 * Creates a new type signature from the given type name encoded as a character
 * array. This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved).toCharArray()</code>, although
 * more efficient for callers with character arrays rather than strings. If the 
 * type name is qualified, then it is expected to be dot-based.
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * @see #createTypeSignature(java.lang.String,boolean)
 * 
 * @since 2.0
 */
public static char[] createCharArrayTypeSignature(char[] typeName, boolean isResolved) {
	// TODO (jerome) - needs to be reworked for parameterized types like List<String[]>[]
	
	if (typeName == null) throw new IllegalArgumentException("null"); //$NON-NLS-1$
	int length = typeName.length;
	if (length == 0) throw new IllegalArgumentException(new String(typeName));

	int arrayCount = CharOperation.occurencesOf('[', typeName);
	char[] sig;
	
	switch (typeName[0]) {
		// primitive type?
		case 'b' :
			if (checkPrimitiveType(BOOLEAN, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_BOOLEAN;
				break;
			} else if (checkPrimitiveType(BYTE, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_BYTE;
				break;
			}
		case 'c':
			if (checkPrimitiveType(CHAR, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_CHAR;
				break;
			}
		case 'd':
			if (checkPrimitiveType(DOUBLE, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_DOUBLE;
				break;
			}
		case 'f':
			if (checkPrimitiveType(FLOAT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_FLOAT;
				break;
			}
		case 'i':
			if (checkPrimitiveType(INT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_INT;
				break;
			}
		case 'l':
			if (checkPrimitiveType(LONG, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_LONG;
				break;
			}
		case 's':
			if (checkPrimitiveType(SHORT, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_SHORT;
				break;
			}
		case 'v':
			if (checkPrimitiveType(VOID, typeName)) {
				sig = new char[arrayCount+1];
				sig[arrayCount] = C_VOID;
				break;
			}
		default:
			// non primitive type
			int sigLength = arrayCount + 1 + length + 1; // for example '[[[Ljava.lang.String;'
			sig = new char[sigLength];
			int sigIndex = arrayCount+1; // index in sig
			int startID = 0; // start of current ID in typeName
			int index = 0; // index in typeName
			while (index < length) {
				char currentChar = typeName[index];
				switch (currentChar) {
					case '.':
						if (startID == -1) throw new IllegalArgumentException(new String(typeName));
						if (startID < index) {
							sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
							sigIndex += index-startID;
						}
						sig[sigIndex++] = C_DOT;
						index++;
						startID = index;
						break;
					case '[':
						if (startID != -1) {
							if (startID < index) {
								sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
								sigIndex += index-startID;
							}
							startID = -1; // no more id after []
						}
						index++;
						break;
					default :
						if (startID != -1 && CharOperation.isWhitespace(currentChar)) {
							if (startID < index) {
								sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
								sigIndex += index-startID;
							}
							startID = index+1;
						}
						index++;
						break;
				}
			}
			// last id
			if (startID != -1 && startID < index) {
				sig = CharOperation.append(sig, sigIndex, typeName, startID, index);
				sigIndex += index-startID;
			}
			
			// add L (or Q) at the beigininig and ; at the end
			sig[arrayCount] = isResolved ? C_RESOLVED : C_UNRESOLVED;
			sig[sigIndex++] = C_NAME_END;
			
			// resize if needed
			if (sigLength > sigIndex) {
				System.arraycopy(sig, 0, sig = new char[sigIndex], 0, sigIndex);
			}
	}

	// add array info
	for (int i = 0; i < arrayCount; i++) {
		sig[i] = C_ARRAY;
	}
	
	return sig;
}
/**
 * Creates a new type signature from the given type name. If the type name is qualified,
 * then it is expected to be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * createTypeSignature("int", hucairz) -> "I"
 * createTypeSignature("java.lang.String", true) -> "Ljava.lang.String;"
 * createTypeSignature("String", false) -> "QString;"
 * createTypeSignature("java.lang.String", false) -> "Qjava.lang.String;"
 * createTypeSignature("int []", false) -> "[I"
 * createTypeSignature("List&lt;String&gt;", false) -> "QList&lt;QString;&gt;;"
 * createTypeSignature("List&lt;?&gt;", false) -> "QList&lt;&ast;&gt;;"
 * createTypeSignature("List&lt;? extends EventListener&gt;", false) -> "QList&lt;-QEventListener;&gt;;"
 * createTypeSignature("List&lt;? super Reader&gt;", false) -> "QList&lt;+QReader;&gt;;"
 * </code>
 * </pre>
 * </p>
 *
 * @param typeName the possibly qualified type name
 * @param isResolved <code>true</code> if the type name is to be considered
 *   resolved (for example, a type name from a binary class file), and 
 *   <code>false</code> if the type name is to be considered unresolved
 *   (for example, a type name found in source code)
 * @return the encoded type signature
 * TODO (jeem) - fundamental problem with resolve types involving type variables which are syntactically indistinguishable for type in default package
 */
public static String createTypeSignature(String typeName, boolean isResolved) {
	return createTypeSignature(typeName == null ? null : typeName.toCharArray(), isResolved);
}

/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static int getArrayCount(char[] typeSignature) throws IllegalArgumentException {	
	try {
		int count = 0;
		while (typeSignature[count] == C_ARRAY) {
			++count;
		}
		return count;
	} catch (ArrayIndexOutOfBoundsException e) { // signature is syntactically incorrect if last character is C_ARRAY
		throw new IllegalArgumentException();
	}
}
/**
 * Returns the array count (array nesting depth) of the given type signature.
 *
 * @param typeSignature the type signature
 * @return the array nesting depth, or 0 if not an array
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getArrayCount(String typeSignature) throws IllegalArgumentException {
	return getArrayCount(typeSignature.toCharArray());
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType({'[', '[', 'I'}) --> {'I'}.
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] getElementType(char[] typeSignature) throws IllegalArgumentException {
	int count = getArrayCount(typeSignature);
	if (count == 0) return typeSignature;
	int length = typeSignature.length;
	char[] result = new char[length-count];
	System.arraycopy(typeSignature, count, result, 0, length-count);
	return result;
}
/**
 * Returns the type signature without any array nesting.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getElementType("[[I") --> "I".
 * </code>
 * </pre>
 * </p>
 * 
 * @param typeSignature the type signature
 * @return the type signature without arrays
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String getElementType(String typeSignature) throws IllegalArgumentException {
	return new String(getElementType(typeSignature.toCharArray()));
}
/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * @since 2.0
 */
public static int getParameterCount(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = 0;
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature) + 1;
		if (i == 0)
			throw new IllegalArgumentException();
		for (;;) {
			char c = methodSignature[i++];
			switch (c) {
				case C_ARRAY :
					break;
				case C_BOOLEAN :
				case C_BYTE :
				case C_CHAR :
				case C_DOUBLE :
				case C_FLOAT :
				case C_INT :
				case C_LONG :
				case C_SHORT :
				case C_VOID :
					++count;
					break;
				case C_TYPE_VARIABLE :
				case C_RESOLVED :
				case C_UNRESOLVED :
					// TODO (jeem) - rework to handle type arguments like QList<QString;>;
					i = CharOperation.indexOf(C_SEMICOLON, methodSignature, i) + 1;
					if (i == 0)
						throw new IllegalArgumentException();
					++count;
					break;
				case C_PARAM_END :
					return count;
				default :
					throw new IllegalArgumentException();
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Returns the number of parameter types in the given method signature.
 *
 * @param methodSignature the method signature
 * @return the number of parameters
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static int getParameterCount(String methodSignature) throws IllegalArgumentException {
	return getParameterCount(methodSignature.toCharArray());
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[][] getParameterTypes(char[] methodSignature) throws IllegalArgumentException {
	try {
		int count = getParameterCount(methodSignature);
		char[][] result = new char[count][];
		if (count == 0)
			return result;
		int i = CharOperation.indexOf(C_PARAM_START, methodSignature) + 1;
		count = 0;
		int start = i;
		for (;;) {
			char c = methodSignature[i++];
			switch (c) {
				case C_ARRAY :
					// array depth is i - start;
					break;
				case C_BOOLEAN :
				case C_BYTE :
				case C_CHAR :
				case C_DOUBLE :
				case C_FLOAT :
				case C_INT :
				case C_LONG :
				case C_SHORT :
				case C_VOID :
					// common case of base types
					if (i - start == 1) {
						switch (c) {
							case C_BOOLEAN :
								result[count++] = new char[] {C_BOOLEAN};
								break;
							case C_BYTE :
								result[count++] = new char[] {C_BYTE};
								break;
							case C_CHAR :
								result[count++] = new char[] {C_CHAR};
								break;
							case C_DOUBLE :
								result[count++] = new char[] {C_DOUBLE};
								break;
							case C_FLOAT :
								result[count++] = new char[] {C_FLOAT};
								break;
							case C_INT :
								result[count++] = new char[] {C_INT};
								break;
							case C_LONG :
								result[count++] = new char[] {C_LONG};
								break;
							case C_SHORT :
								result[count++] = new char[] {C_SHORT};
								break;
							case C_VOID :
								result[count++] = new char[] {C_VOID};
								break;
						}
					} else {
						result[count++] = CharOperation.subarray(methodSignature, start, i);
					}
					start = i;
					break;
				case C_TYPE_VARIABLE :
				case C_RESOLVED :
				case C_UNRESOLVED :
					// TODO (jeem) - rework to handle type arguments like QList<QString;>;
					i = CharOperation.indexOf(C_SEMICOLON, methodSignature, i) + 1;
					if (i == 0)
						throw new IllegalArgumentException();
					result[count++] = CharOperation.subarray(methodSignature, start, i);
					start = i;
					break;
				case C_PARAM_END:
					return result;
				default :
					throw new IllegalArgumentException();
			}
		}
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Extracts the parameter type signatures from the given method signature. 
 * The method signature is expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String[] getParameterTypes(String methodSignature) throws IllegalArgumentException {
	char[][] parameterTypes = getParameterTypes(methodSignature.toCharArray());
	int length = parameterTypes.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(parameterTypes[i]);
	}
	return result;
}

/**
 * Extracts the type variable name from the given formal type parameter
 * signature. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the name of the type variable
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static String getTypeVariable(String formalTypeParameterSignature) throws IllegalArgumentException {
	return new String(getTypeVariable(formalTypeParameterSignature.toCharArray()));
}

/**
 * Extracts the type variable name from the given formal type parameter
 * signature. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the name of the type variable
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static char[] getTypeVariable(char[] formalTypeParameterSignature) throws IllegalArgumentException {
	int p = CharOperation.indexOf(C_COLON, formalTypeParameterSignature);
	if (p < 0) {
		// no ":" means can't be a formal type parameter signature
		throw new IllegalArgumentException();
	}
	return CharOperation.subarray(formalTypeParameterSignature, 0, p);
}

/**
 * Extracts the class and interface bounds from the given formal type
 * parameter signature. The class bound, if present, is listed before
 * the interface bounds. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the (possibly empty) list of type signatures for the bounds
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static char[][] getTypeParameterBounds(char[] formalTypeParameterSignature) throws IllegalArgumentException {
	int p1 = CharOperation.indexOf(C_COLON, formalTypeParameterSignature);
	if (p1 < 0) {
		// no ":" means can't be a formal type parameter signature
		throw new IllegalArgumentException();
	}
	if (p1 == formalTypeParameterSignature.length - 1) {
		// no class or interface bounds
		return CharOperation.NO_CHAR_CHAR;
	}
	int p2 = CharOperation.indexOf(C_COLON, formalTypeParameterSignature, p1 + 1);
	char[] classBound;
	if (p2 < 0) {
		// no interface bounds
		classBound = CharOperation.subarray(formalTypeParameterSignature, p1 + 1, formalTypeParameterSignature.length);
		return new char[][] {classBound};
	}
	if (p2 == p1 + 1) {
		// no class bound, but 1 or more interface bounds
		classBound = null;
	} else {
		classBound = CharOperation.subarray(formalTypeParameterSignature, p1 + 1, p2);
	}
	char[][] interfaceBounds = CharOperation.splitOn(C_COLON, formalTypeParameterSignature, p2 + 1, formalTypeParameterSignature.length);
	if (classBound == null) {
		return interfaceBounds;
	}
	int resultLength = interfaceBounds.length + 1;
	char[][] result = new char[resultLength][];
	result[0] = classBound;
	System.arraycopy(interfaceBounds, 0, result, 1, interfaceBounds.length);
	return result;
}

/**
 * Extracts the class and interface bounds from the given formal type
 * parameter signature. The class bound, if present, is listed before
 * the interface bounds. The signature is expected to be dot-based.
 *
 * @param formalTypeParameterSignature the formal type parameter signature
 * @return the (possibly empty) list of type signatures for the bounds
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * @since 3.0
 */
public static String[] getTypeParameterBounds(String formalTypeParameterSignature) throws IllegalArgumentException {
	char[][] bounds = getTypeParameterBounds(formalTypeParameterSignature.toCharArray());
	int length = bounds.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(bounds[i]);
	}
	return result;
}

/**
 * Returns a char array containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty char array if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g'}
 * getQualifier({'O', 'u', 't', 'e', 'r', '.', 'I', 'n', 'n', 'e', 'r'}) -> {'O', 'u', 't', 'e', 'r'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty char array if the name contains no
 *   dots
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[] getQualifier(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return CharOperation.NO_CHAR;
	}
	return CharOperation.subarray(name, 0, lastDot);
}
/**
 * Returns a string containing all but the last segment of the given 
 * dot-separated qualified name. Returns the empty string if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getQualifier("java.lang.Object") -> "java.lang"
 * getQualifier("Outer.Inner") -> "Outer"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the qualifier prefix, or the empty string if the name contains no
 *   dots
 * @exception NullPointerException if name is null
 */
public static String getQualifier(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return EMPTY;
	}
	return name.substring(0, lastDot);
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 * 
 * @since 2.0
 */
public static char[] getReturnType(char[] methodSignature) throws IllegalArgumentException {
	// skip type parameters
	int i = CharOperation.lastIndexOf(C_PARAM_END, methodSignature);
	if (i == -1) {
		throw new IllegalArgumentException();
	}
	// ignore any thrown exceptions
	int j = CharOperation.indexOf(C_THROWS, methodSignature);
	int last = (j == -1 ? methodSignature.length : j);
	return CharOperation.subarray(methodSignature, i + 1, last);
}
/**
 * Extracts the return type from the given method signature. The method signature is 
 * expected to be dot-based.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String getReturnType(String methodSignature) throws IllegalArgumentException {
	return new String(getReturnType(methodSignature.toCharArray()));
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[] getSimpleName(char[] name) {
	int lastDot = CharOperation.lastIndexOf(C_DOT, name);
	if (lastDot == -1) {
		return name;
	}
	return CharOperation.subarray(name, lastDot + 1, name.length);
}
/**
 * Returns the last segment of the given dot-separated qualified name.
 * Returns the given name if it is not qualified.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleName("java.lang.Object") -> "Object"
 * </code>
 * </pre>
 * </p>
 *
 * @param name the name
 * @return the last segment of the qualified name
 * @exception NullPointerException if name is null
 */
public static String getSimpleName(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return name;
	}
	return name.substring(lastDot + 1, name.length());
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames({'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}) -> {{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames({'O', 'b', 'j', 'e', 'c', 't'}) -> {{'O', 'b', 'j', 'e', 'c', 't'}}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 * @exception NullPointerException if name is null
 * @since 2.0
 */
public static char[][] getSimpleNames(char[] name) {
	if (name.length == 0) {
		return CharOperation.NO_CHAR_CHAR;
	}
	int dot = CharOperation.indexOf(C_DOT, name);
	if (dot == -1) {
		return new char[][] {name};
	}
	int n = 1;
	while ((dot = CharOperation.indexOf(C_DOT, name, dot + 1)) != -1) {
		++n;
	}
	char[][] result = new char[n + 1][];
	int segStart = 0;
	for (int i = 0; i < n; ++i) {
		dot = CharOperation.indexOf(C_DOT, name, segStart);
		result[i] = CharOperation.subarray(name, segStart, dot);
		segStart = dot + 1;
	}
	result[n] = CharOperation.subarray(name, segStart, name.length);
	return result;
}
/**
 * Returns all segments of the given dot-separated qualified name.
 * Returns an array with only the given name if it is not qualified.
 * Returns an empty array if the name is empty.
 * <p>
 * For example:
 * <pre>
 * <code>
 * getSimpleNames("java.lang.Object") -> {"java", "lang", "Object"}
 * getSimpleNames("Object") -> {"Object"}
 * getSimpleNames("") -> {}
 * </code>
 * </pre>
 *
 * @param name the name
 * @return the list of simple names, possibly empty
 * @exception NullPointerException if name is null
 */
public static String[] getSimpleNames(String name) {
	char[][] simpleNames = getSimpleNames(name.toCharArray());
	int length = simpleNames.length;
	String[] result = new String[length];
	for (int i = 0; i < length; i++) {
		result[i] = new String(simpleNames[i]);
	}
	return result;
}
/**
 * Converts the given method signature to a readable form. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @return the char array representation of the method signature
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] methodSignature, char[] methodName, char[][] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	return toCharArray(methodSignature, methodName, parameterNames, fullyQualifyTypeNames, includeReturnType, false, false);
}

/**
 * Converts the given method signature to a readable form. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @param includeFormalTypeParameters <code>true</code> if any formal type
 * parameters are to be included
 * @param includeThrownExceptions <code>true</code> if any thrown exceptions
 * are to be included
 * @return the char array representation of the method signature
 * @since 3.0
 */
public static char[] toCharArray(
		char[] methodSignature,
		char[] methodName,
		char[][] parameterNames,
		boolean fullyQualifyTypeNames,
		boolean includeReturnType,
		boolean includeFormalTypeParameters,
		boolean includeThrownExceptions) {
	// TODO (jeem) - needs to handle includeFormalTypeParameters and includeThrownExceptions
	try {
		int firstParen = CharOperation.indexOf(C_PARAM_START, methodSignature);
		if (firstParen == -1) throw new IllegalArgumentException();
		
		int sigLength = methodSignature.length;
		
		// compute result length
		
		// method signature
		int paramCount = 0;
		int lastParen = -1;
		int resultLength = 0;
		signature: for (int i = firstParen; i < sigLength; i++) {
			switch (methodSignature[i]) {
				case C_ARRAY :
					resultLength += 2; // []
					continue signature;
				case C_BOOLEAN :
					resultLength += BOOLEAN.length;
					break;
				case C_BYTE :
					resultLength += BYTE.length;
					break;
				case C_CHAR :
					resultLength += CHAR.length;
					break;
				case C_DOUBLE :
					resultLength += DOUBLE.length;
					break;
				case C_FLOAT :
					resultLength += FLOAT.length;
					break;
				case C_INT :
					resultLength += INT.length;
					break;
				case C_LONG :
					resultLength += LONG.length;
					break;
				case C_SHORT :
					resultLength += SHORT.length;
					break;
				case C_VOID :
					resultLength += VOID.length;
					break;
				case C_RESOLVED :
				case C_UNRESOLVED :
					int end = CharOperation.indexOf(C_SEMICOLON, methodSignature, i);
					if (end == -1) throw new IllegalArgumentException();
					int start;
					if (fullyQualifyTypeNames) {
						start = i+1;
					} else {
						start = CharOperation.lastIndexOf(C_DOT, methodSignature, i, end) + 1;
						if (start == 0) start = i+1;
					} 
					resultLength += end-start;
					i = end;
					break;
				case C_PARAM_START :
					// add space for "("
					resultLength++;
					continue signature;
				case C_PARAM_END :
					lastParen = i;
					if (includeReturnType) {
						if (paramCount > 0) {
							// remove space for ", " that was added with last parameter and remove space that is going to be added for ", " after return type 
							// and add space for ") "
							resultLength -= 2;
						} //else
							// remove space that is going to be added for ", " after return type 
							// and add space for ") "
							// -> noop
						
						// decrement param count because it is going to be added for return type
						paramCount--;
						continue signature;
					} else {
						if (paramCount > 0) {
							// remove space for ", " that was added with last parameter and add space for ")"
							resultLength--;
						} else {
							// add space for ")"
							resultLength++;
						}
						break signature;
					}
				default :
					throw new IllegalArgumentException();
			}
			resultLength += 2; // add space for ", "
			paramCount++;
		}
		
		// parameter names
		int parameterNamesLength = parameterNames == null ? 0 : parameterNames.length;
		for (int i = 0; i <parameterNamesLength; i++) {
			resultLength += parameterNames[i].length + 1; // parameter name + space
		}
		
		// selector
		int selectorLength = methodName == null ? 0 : methodName.length;
		resultLength += selectorLength;
		
		// create resulting char array
		char[] result = new char[resultLength];
		
		// returned type
		int index = 0;
		if (includeReturnType) {
			long pos = copyType(methodSignature, lastParen+1, result, index, fullyQualifyTypeNames);
			index = (int) (pos >>> 32);
			result[index++] = ' ';
		}
		
		// selector
		if (methodName != null) {
			System.arraycopy(methodName, 0, result, index, selectorLength);
			index += selectorLength;
		}
		
		// parameters
		result[index++] = C_PARAM_START;
		int sigPos = firstParen+1;
		for (int i = 0; i < paramCount; i++) {
			long pos = copyType(methodSignature, sigPos, result, index, fullyQualifyTypeNames);
			index = (int) (pos >>> 32);
			sigPos = (int)pos;
			if (parameterNames != null) {
				result[index++] = ' ';
				char[] parameterName = parameterNames[i];
				int paramLength = parameterName.length;
				System.arraycopy(parameterName, 0, result, index, paramLength);
				index += paramLength;
			}
			if (i != paramCount-1) {
				result[index++] = ',';
				result[index++] = ' ';
			}
		}
		if (sigPos >= sigLength) {
			throw new IllegalArgumentException(); // should be on last paren
		}
		result[index++] = C_PARAM_END;
		
		return result;
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}		
}


/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString({'[', 'L', 'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', ';'}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'S', 't', 'r', 'i', 'n', 'g', '[', ']'}
 * toString({'I'}) -> {'i', 'n', 't'}
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 * 
 * @since 2.0
 */
public static char[] toCharArray(char[] signature) throws IllegalArgumentException {
	try {
		int sigLength = signature.length;

		if (sigLength == 0 || signature[0] == C_PARAM_START || signature[0] == C_GENERIC_START) {
			return toCharArray(signature, CharOperation.NO_CHAR, null, true, true);
		}
		
		// compute result length
		int resultLength = 0;
		int index = -1;
		while (signature[++index] == C_ARRAY) {
			resultLength += 2; // []
		}
		switch (signature[index]) {
			case C_BOOLEAN :
				resultLength += BOOLEAN.length;
				break;
			case C_BYTE :
				resultLength += BYTE.length;
				break;
			case C_CHAR :
				resultLength += CHAR.length;
				break;
			case C_DOUBLE :
				resultLength += DOUBLE.length;
				break;
			case C_FLOAT :
				resultLength += FLOAT.length;
				break;
			case C_INT :
				resultLength += INT.length;
				break;
			case C_LONG :
				resultLength += LONG.length;
				break;
			case C_SHORT :
				resultLength += SHORT.length;
				break;
			case C_VOID :
				resultLength += VOID.length;
				break;
			case C_TYPE_VARIABLE :
			case C_RESOLVED :
			case C_UNRESOLVED :
				// TODO (jeem) - needs to handle type arguments
				int end = CharOperation.indexOf(C_SEMICOLON, signature, index);
				if (end == -1) throw new IllegalArgumentException();
				int start = index + 1;
				resultLength += end-start;
				break;
			default :
				throw new IllegalArgumentException();
		}
		
		char[] result = new char[resultLength];
		copyType(signature, 0, result, 0, true);

		/**
		 * Converts '$' separated type signatures into '.' separated type signature.
		 * NOTE: This assumes that the type signature is an inner type signature.
		 *       This is true in most cases, but someone can define a non-inner type 
		 *       name containing a '$'. However to tell the difference, we would have
		 *       to resolve the signature, which cannot be done at this point.
		 */
		CharOperation.replace(result, C_DOLLAR, C_DOT);

		return result;
	} catch (ArrayIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}	
}
/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName({{'j', 'a', 'v', 'a'}, {'l', 'a', 'n', 'g'}, {'O', 'b', 'j', 'e', 'c', 't'}}) -> {'j', 'a', 'v', 'a', '.', 'l', 'a', 'n', 'g', '.', 'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{'O', 'b', 'j', 'e', 'c', 't'}}) -> {'O', 'b', 'j', 'e', 'c', 't'}
 * toQualifiedName({{}}) -> {}
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 * 
 * @since 2.0
 */
public static char[] toQualifiedName(char[][] segments) {
	int length = segments.length;
	if (length == 0) return CharOperation.NO_CHAR;
	if (length == 1) return segments[0];
	
	int resultLength = 0;
	for (int i = 0; i < length; i++) {
		resultLength += segments[i].length+1;
	}
	resultLength--;
	char[] result = new char[resultLength];
	int index = 0;
	for (int i = 0; i < length; i++) {
		char[] segment = segments[i];
		int segmentLength = segment.length;
		System.arraycopy(segment, 0, result, index, segmentLength);
		index += segmentLength;
		if (i != length-1) {
			result[index++] = C_DOT;
		}
	}
	return result;
}
/**
 * Converts the given array of qualified name segments to a qualified name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toQualifiedName(new String[] {"java", "lang", "Object"}) -> "java.lang.Object"
 * toQualifiedName(new String[] {"Object"}) -> "Object"
 * toQualifiedName(new String[0]) -> ""
 * </code>
 * </pre>
 * </p>
 *
 * @param segments the list of name segments, possibly empty
 * @return the dot-separated qualified name, or the empty string
 */
public static String toQualifiedName(String[] segments) {
	int length = segments.length;
	char[][] charArrays = new char[length][];
	for (int i = 0; i < length; i++) {
		charArrays[i] = segments[i].toCharArray();
	}
	return new String(toQualifiedName(charArrays));
}
/**
 * Converts the given type signature to a readable string. The signature is expected to
 * be dot-based.
 * 
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("[Ljava.lang.String;") -> "java.lang.String[]"
 * toString("I") -> "int"
 * </code>
 * </pre>
 * </p>
 * <p>
 * Note: This method assumes that a type signature containing a <code>'$'</code>
 * is an inner type signature. While this is correct in most cases, someone could 
 * define a non-inner type name containing a <code>'$'</code>. Handling this 
 * correctly in all cases would have required resolving the signature, which 
 * generally not feasible.
 * </p>
 *
 * @param signature the type signature
 * @return the string representation of the type
 * @exception IllegalArgumentException if the signature is not syntactically
 *   correct
 */
public static String toString(String signature) throws IllegalArgumentException {
	return new String(toCharArray(signature.toCharArray()));
}
/**
 * Converts the given method signature to a readable string. The method signature is expected to
 * be dot-based. This method is equivalent to
 * <code>toString(methodSignature, methodName, parameterNames, fullyQualifyTypeNames, includeReturnType, false, false)</code>.
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @see #toString(String, String, String[], boolean, boolean, boolean, boolean)
 * @return the string representation of the method signature
 */
public static String toString(String methodSignature, String methodName, String[] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	return new String(toString(
			methodSignature,
			methodName,
			parameterNames,
			fullyQualifyTypeNames,
			includeReturnType,
			false,
			false));
}

/**
 * Converts the given method signature to a readable string. The method signature is expected to
 * be dot-based.
 * <p>
 * For example:
 * <pre>
 * <code>
 * toString("([Ljava.lang.String;)V", "main", new String[] {"args"}, false, true, false, false) -> "void main(String[] args)"
 * </code>
 * </pre>
 * </p>
 * 
 * @param methodSignature the method signature to convert
 * @param methodName the name of the method to insert in the result, or 
 *   <code>null</code> if no method name is to be included
 * @param parameterNames the parameter names to insert in the result, or 
 *   <code>null</code> if no parameter names are to be included; if supplied,
 *   the number of parameter names must match that of the method signature
 * @param fullyQualifyTypeNames <code>true</code> if type names should be fully
 *   qualified, and <code>false</code> to use only simple names
 * @param includeReturnType <code>true</code> if the return type is to be
 *   included
 * @param includeFormalTypeParameters <code>true</code> if any formal type
 * parameters are to be included
 * @param includeThrownExceptions <code>true</code> if any thrown exceptions
 * are to be included
 * @return the string representation of the method signature
 * @since 3.0
 */
public static String toString(
		String methodSignature,
		String methodName,
		String[] parameterNames,
		boolean fullyQualifyTypeNames,
		boolean includeReturnType,
		boolean includeFormalTypeParameters,
		boolean includeThrownExceptions) {
	char[][] params;
	if (parameterNames == null) {
		params = null;
	} else {
		int paramLength = parameterNames.length;
		params = new char[paramLength][];
		for (int i = 0; i < paramLength; i++) {
			params[i] = parameterNames[i].toCharArray();
		}
	}
	return new String(toCharArray(
			methodSignature.toCharArray(),
			methodName == null ? null : methodName.toCharArray(),
			params,
			fullyQualifyTypeNames,
			includeReturnType,
			includeFormalTypeParameters,
			includeThrownExceptions));
}

}

package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalSymbols;

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
 *   | "L" + binaryTypeName + ";"  // resolved named type (i.e., in compiled code)
 *   | "Q" + sourceTypeName + ";"  // unresolved named type (i.e., in source code)
 *   | "[" + typeSignature  // array of type denoted by typeSignature
 * </pre>
 * </p>
 * <p>
 * Examples:
 * <ul>
 *   <li><code>"[[I"</code> denotes <code>int[][]</code></li>
 *   <li><code>"Ljava.lang.String;"</code> denotes <code>java.lang.String</code> in compiled code</li>
 *   <li><code>"QString"</code> denotes <code>String</code> in source code</li>
 *   <li><code>"Qjava.lang.String"</code> denotes <code>java.lang.String</code> in source code</li>
 *   <li><code>"[QString"</code> denotes <code>String[]</code> in source code</li>
 * </ul>
 * </p>
 * <p>
 * The syntax for a method signature is:
 * <pre>
 * methodSignature ::= "(" + paramTypeSignature* + ")" + returnTypeSignature
 * paramTypeSignature ::= typeSignature
 * returnTypeSignature ::= typeSignature
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
	
/**
 * Not instantiable.
 */
private Signature() {}
/**
 * Internal - Adds array brackets to a readable type name.
 */
private static String arrayIfy(String typeName, int arrayCount) {
	if (arrayCount == 0) {
		return typeName;
	}
	StringBuffer sb = new StringBuffer(typeName.length() + arrayCount * 2);
	sb.append(typeName);
	for (int i = 0; i < arrayCount; ++i) {
		sb.append("[]"); //$NON-NLS-1$
	}
	return sb.toString();
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
	if (arrayCount == 0) return typeSignature;
	StringBuffer sb = new StringBuffer(typeSignature.length() + arrayCount);
	for (int i = 0; i < arrayCount; ++i) {
		sb.append(C_ARRAY);
	}
	sb.append(typeSignature);
	return sb.toString();
}
/**
 * Creates a method signature from the given parameter and return type 
 * signatures.
 *
 * @param parameterTypes the list of parameter type signatures
 * @param returnType the return type signature
 * @return the encoded method signature
 */
public static String createMethodSignature(String[] parameterTypes, String returnType) {
	StringBuffer sb = new StringBuffer();
	sb.append(C_PARAM_START);
	for (int i = 0; i < parameterTypes.length; ++i) {
		sb.append(parameterTypes[i]);
	}
	sb.append(C_PARAM_END);
	sb.append(returnType);
	return sb.toString();
}
/**
 * Creates a new type signature from the given type name encoded as a character
 * array. This method is equivalent to
 * <code>createTypeSignature(new String(typeName),isResolved)</code>, although
 * more efficient for callers with character arrays rather than strings.
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
	int len = typeName.length;
	if (typeName[len - 1] != ']') {
		switch (len) {
			case 3 :
				if (typeName[0] == 'i' && typeName[1] == 'n' && typeName[2] == 't')
					return SIG_INT;
				break;
			case 4 :
				if (typeName[0] == 'v' && typeName[1] == 'o' && typeName[2] == 'i' && typeName[3] == 'd')
					return SIG_VOID;
			case 6 :
				if (typeName[0] == 'S' && typeName[1] == 't' && typeName[2] == 'r' && typeName[3] == 'i' && typeName[4] == 'n' && typeName[5] == 'g')
					if (!isResolved) return "QString;"; //$NON-NLS-1$
					break;
			case 7 :
				if (typeName[0] == 'b' && typeName[1] == 'o' && typeName[2] == 'o' && typeName[3] == 'l' && typeName[4] == 'e' && typeName[5] == 'a' && typeName[6] == 'n')
					return SIG_BOOLEAN;
		}
	}
	return createTypeSignature(new String(typeName), isResolved);
}
/**
 * Creates a new type signature from the given type name.
 * <p>
 * For example:
 * <pre>
 * <code>
 * createTypeSignature("int", hucairz) -> "I"
 * createTypeSignature("java.lang.String", true) -> "Ljava.lang.String;"
 * createTypeSignature("String", false) -> "QString;"
 * createTypeSignature("java.lang.String", false) -> "Qjava.lang.String;"
 * createTypeSignature("int []", false) -> "[I"
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
 */
public static String createTypeSignature(String typeName, boolean isResolved) {
	try {
		Scanner scanner = new Scanner();
		scanner.setSourceBuffer(typeName.toCharArray());
		int token = scanner.getNextToken();
		StringBuffer sig = new StringBuffer();
		int arrayCount = 0;
		boolean primitive = true;
		switch (token) {
			case TerminalSymbols.TokenNameIdentifier :
				sig.append(scanner.getCurrentIdentifierSource());
				primitive = false;
				break;
			case TerminalSymbols.TokenNameboolean :
				sig.append(Signature.SIG_BOOLEAN);
				break;
			case TerminalSymbols.TokenNamebyte :
				sig.append(Signature.SIG_BYTE);
				break;
			case TerminalSymbols.TokenNamechar :
				sig.append(Signature.SIG_CHAR);
				break;
			case TerminalSymbols.TokenNamedouble :
				sig.append(Signature.SIG_DOUBLE);
				break;
			case TerminalSymbols.TokenNamefloat :
				sig.append(Signature.SIG_FLOAT);
				break;
			case TerminalSymbols.TokenNameint :
				sig.append(Signature.SIG_INT);
				break;
			case TerminalSymbols.TokenNamelong :
				sig.append(Signature.SIG_LONG);
				break;
			case TerminalSymbols.TokenNameshort :
				sig.append(Signature.SIG_SHORT);
				break;
			case TerminalSymbols.TokenNamevoid :
				sig.append(Signature.SIG_VOID);
				break;
			default :
				throw new IllegalArgumentException();
		}
		token = scanner.getNextToken();
		while (!primitive && token == TerminalSymbols.TokenNameDOT) {
			sig.append(scanner.getCurrentIdentifierSource());
			token = scanner.getNextToken();
			if (token == TerminalSymbols.TokenNameIdentifier) {
				sig.append(scanner.getCurrentIdentifierSource());
				token = scanner.getNextToken();
			} else {
				throw new IllegalArgumentException();
			}
		}
		while (token == TerminalSymbols.TokenNameLBRACKET) {
			token = scanner.getNextToken();
			if (token != TerminalSymbols.TokenNameRBRACKET)
				throw new IllegalArgumentException();
			arrayCount++;
			token = scanner.getNextToken();
		}
		if (token != TerminalSymbols.TokenNameEOF)
			throw new IllegalArgumentException();
		if (!primitive) {
			sig.insert(0, isResolved ? C_RESOLVED : C_UNRESOLVED);
		}
		if (arrayCount == 0) {
			if (primitive)
				return sig.toString();
		} else {
			char[] brackets = new char[arrayCount];
			while (arrayCount-- != 0) {
				brackets[arrayCount] = C_ARRAY;
			}
			sig.insert(0, brackets);
		}
		if (!primitive) {
			sig.append(C_NAME_END);
		}
		return sig.toString();
	} catch (InvalidInputException e) {
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
	try {
		int count = 0;
		while (typeSignature.charAt(count) == C_ARRAY) {
			++count;
		}
		return count;
	} catch (StringIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
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
	try {
		int count = 0;
		while (typeSignature.charAt(count) == C_ARRAY) {
			++count;
		}
		return typeSignature.substring(count);
	} catch (StringIndexOutOfBoundsException e) {
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
	try {
		int count = 0;
		int i = methodSignature.indexOf(C_PARAM_START) + 1;
		if (i == 0)
			throw new IllegalArgumentException();
		int start = i;
		for (;;) {
			char c = methodSignature.charAt(i++);
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
				case C_RESOLVED :
				case C_UNRESOLVED :
					i = methodSignature.indexOf(C_SEMICOLON, i) + 1;
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
	} catch (StringIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Extracts the parameter type signatures from the given method signature.
 *
 * @param methodSignature the method signature
 * @return the list of parameter type signatures
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String[] getParameterTypes(String methodSignature) throws IllegalArgumentException {
	try {
		int count = getParameterCount(methodSignature);
		String[] result = new String[count];
		if (count == 0)
			return result;
		int i = methodSignature.indexOf(C_PARAM_START) + 1;
		count = 0;
		int start = i;
		for (;;) {
			char c = methodSignature.charAt(i++);
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
								result[count++] = SIG_BOOLEAN;
								break;
							case C_BYTE :
								result[count++] = SIG_BYTE;
								break;
							case C_CHAR :
								result[count++] = SIG_CHAR;
								break;
							case C_DOUBLE :
								result[count++] = SIG_DOUBLE;
								break;
							case C_FLOAT :
								result[count++] = SIG_FLOAT;
								break;
							case C_INT :
								result[count++] = SIG_INT;
								break;
							case C_LONG :
								result[count++] = SIG_LONG;
								break;
							case C_SHORT :
								result[count++] = SIG_SHORT;
								break;
							case C_VOID :
								result[count++] = SIG_VOID;
								break;
						}
					} else {
						result[count++] = methodSignature.substring(start, i);
					}
					start = i;
					break;
				case C_RESOLVED :
				case C_UNRESOLVED :
					i = methodSignature.indexOf(C_SEMICOLON, i) + 1;
					if (i == 0)
						throw new IllegalArgumentException();
					result[count++] = methodSignature.substring(start, i);
					start = i;
					break;
				case C_PARAM_END:
					return result;
				default :
					throw new IllegalArgumentException();
			}
		}
	} catch (StringIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
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
 */
public static String getQualifier(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return ""; //$NON-NLS-1$
	}
	return name.substring(0, lastDot);
}
/**
 * Extracts the return type from the given method signature.
 *
 * @param methodSignature the method signature
 * @return the type signature of the return type
 * @exception IllegalArgumentException if the signature is syntactically
 *   incorrect
 */
public static String getReturnType(String methodSignature) throws IllegalArgumentException {
	int i = methodSignature.lastIndexOf(C_PARAM_END);
	if (i == -1) {
		throw new IllegalArgumentException();
	}
	return methodSignature.substring(i + 1);
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
 */
public static String getSimpleName(String name) {
	int lastDot = name.lastIndexOf(C_DOT);
	if (lastDot == -1) {
		return name;
	}
	return name.substring(lastDot + 1);
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
 */
public static String[] getSimpleNames(String name) {
	if (name.length() == 0) {
		return new String[0];
	}
	int dot = name.indexOf(C_DOT);
	if (dot == -1) {
		return new String[] {name};
	}
	int n = 1;
	while ((dot = name.indexOf(C_DOT, dot + 1)) != -1) {
		++n;
	}
	String[] result = new String[n + 1];
	int segStart = 0;
	for (int i = 0; i < n; ++i) {
		dot = name.indexOf(C_DOT, segStart);
		result[i] = name.substring(segStart, dot);
		segStart = dot + 1;
	}
	result[n] = name.substring(segStart);
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
	if (segments.length == 0) {
		return ""; //$NON-NLS-1$
	}
	if (segments.length == 1) {
		return segments[0];
	}
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < segments.length; ++i) {
		if (i != 0)
			sb.append(C_DOT);
		sb.append(segments[i]);
	}
	return sb.toString();
}
/**
 * Converts the given type signature to a readable string.
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
	try {
		if (signature.charAt(0) == C_PARAM_START) {
			return toString(signature, "", null, true, true); //$NON-NLS-1$
		}
		int arrayCount = getArrayCount(signature);
		switch (signature.charAt(arrayCount)) {
			case C_BOOLEAN :
				return arrayIfy("boolean", arrayCount); //$NON-NLS-1$
			case C_BYTE :
				return arrayIfy("byte", arrayCount); //$NON-NLS-1$
			case C_CHAR :
				return arrayIfy("char", arrayCount); //$NON-NLS-1$
			case C_DOUBLE :
				return arrayIfy("double", arrayCount); //$NON-NLS-1$
			case C_FLOAT :
				return arrayIfy("float", arrayCount); //$NON-NLS-1$
			case C_INT :
				return arrayIfy("int", arrayCount); //$NON-NLS-1$
			case C_LONG :
				return arrayIfy("long", arrayCount); //$NON-NLS-1$
			case C_SHORT :
				return arrayIfy("short", arrayCount); //$NON-NLS-1$
			case C_VOID :
				return arrayIfy("void", arrayCount); //$NON-NLS-1$
			case C_RESOLVED :
			case C_UNRESOLVED :
				int semi = signature.indexOf(C_SEMICOLON, arrayCount + 1);
				if (semi == -1)
					throw new IllegalArgumentException();
					
				/**
				 * Converts '$' separated type signatures into '.' separated type signature.
				 * NOTE: This assumes that the type signature is an inner type signature.
				 *       This is true in most cases, but someone can define a non-inner type 
				 *       name containing a '$'. However to tell the difference, we would have
				 *       to resolve the signature, which cannot be done at this point.
				 */
				String qualifiedTypeName = signature.substring(arrayCount+1, semi).replace(C_DOLLAR, C_DOT);
				
				return arrayIfy(qualifiedTypeName, arrayCount);
			default :
				throw new IllegalArgumentException();
		}
	} catch (StringIndexOutOfBoundsException e) {
		throw new IllegalArgumentException();
	}
}
/**
 * Converts the given method signature to a readable string.
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
 * @return the string representation of the method signature
 */
public static String toString(String methodSignature, String methodName, String[] parameterNames, boolean fullyQualifyTypeNames, boolean includeReturnType) {
	StringBuffer sb = new StringBuffer();
	String[] paramTypes = getParameterTypes(methodSignature);
	if (includeReturnType) {
		String returnType = getReturnType(methodSignature);
		if (returnType.length() != 0) {
			sb.append(toString(returnType));
			sb.append(' ');
		}
	}
	if (methodName != null)
		sb.append(methodName);
	sb.append(C_PARAM_START);
	for (int i = 0; i < paramTypes.length; ++i) {
		if (i != 0)
			sb.append(", "); //$NON-NLS-1$
		String readableParamType = toString(paramTypes[i]);
		if (!fullyQualifyTypeNames) {
			int lastDot = readableParamType.lastIndexOf(C_DOT);
			if (lastDot != -1) {
				readableParamType = readableParamType.substring(lastDot + 1);
			}
		}
		sb.append(readableParamType);
		if (parameterNames != null) {
			sb.append(' ');
			sb.append(parameterNames[i]);
		}
	}
	sb.append(C_PARAM_END);
	return sb.toString();
}
}

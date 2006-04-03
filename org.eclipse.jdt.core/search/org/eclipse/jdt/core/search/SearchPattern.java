/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.*;


/**
 * A search pattern defines how search results are found. Use <code>SearchPattern.createPattern</code>
 * to create a search pattern.
 * <p>
 * Search patterns are used during the search phase to decode index entries that were added during the indexing phase
 * (see {@link SearchDocument#addIndexEntry(char[], char[])}). When an index is queried, the 
 * index categories and keys to consider are retrieved from the search pattern using {@link #getIndexCategories()} and
 * {@link #getIndexKey()}, as well as the match rule (see {@link #getMatchRule()}). A blank pattern is
 * then created (see {@link #getBlankPattern()}). This blank pattern is used as a record as follows.
 * For each index entry in the given index categories and that starts with the given key, the blank pattern is fed using 
 * {@link #decodeIndexKey(char[])}. The original pattern is then asked if it matches the decoded key using
 * {@link #matchesDecodedKey(SearchPattern)}. If it matches, a search doument is created for this index entry
 * using {@link SearchParticipant#getDocument(String)}.
 * 
 * </p><p>
 * This class is intended to be subclassed by clients. A default behavior is provided for each of the methods above, that
 * clients can ovveride if they wish.
 * </p>
 * @see #createPattern(org.eclipse.jdt.core.IJavaElement, int)
 * @see #createPattern(String, int, int, int)
 * @since 3.0
 */
public abstract class SearchPattern extends InternalSearchPattern {

	// Rules for pattern matching: (exact, prefix, pattern) [ | case sensitive]
	/**
	 * Match rule: The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 */
	public static final int R_EXACT_MATCH = 0;

	/**
	 * Match rule: The search pattern is a prefix of the search result.
	 */
	public static final int R_PREFIX_MATCH = 0x0001;

	/**
	 * Match rule: The search pattern contains one or more wild cards ('*' or '?'). 
	 * A '*' wild-card can replace 0 or more characters in the search result.
	 * A '?' wild-card replaces exactly 1 character in the search result.
	 */
	public static final int R_PATTERN_MATCH = 0x0002;

	/**
	 * Match rule: The search pattern contains a regular expression.
	 */
	public static final int R_REGEXP_MATCH = 0x0004;

	/**
	 * Match rule: The search pattern matches the search result only if cases are the same.
	 * Can be combined to previous rules, e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE}
	 */
	public static final int R_CASE_SENSITIVE = 0x0008;

	/**
	 * Match rule: The search pattern matches search results as raw/parameterized types/methods with same erasure.
	 * This mode has no effect on other java elements search.<br>
	 * Type search example:
	 * 	<ul>
	 * 	<li>pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match: <code>List&lt;Object&gt;</code></li>
	 * 	</ul>
	 * Method search example:
	 * 	<ul>
	 * 	<li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
	 * 	<li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
	 * 	<li>match: <code>&lt;Object&gt;foo(new Object())</code></li>
	 * 	</ul>
	 * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_ERASURE_MATCH}
	 * This rule is not activated by default, so raw types or parameterized types with same erasure will not be found
	 * for pattern List&lt;String&gt;,
	 * Note that with this pattern, the match selection will be only on the erasure even for parameterized types.
	 * @since 3.1
	 */
	public static final int R_ERASURE_MATCH = 0x0010;

	/**
	 * Match rule: The search pattern matches search results as raw/parameterized types/methods with equivalent type parameters.
	 * This mode has no effect on other java elements search.<br>
	 * Type search example:
	 * <ul>
	 * 	<li>pattern: <code>List&lt;Exception&gt;</code></li>
	 * 	<li>match:
	 * 		<ul>
	 * 		<li><code>List&lt;? extends Throwable&gt;</code></li>
	 * 		<li><code>List&lt;? super RuntimeException&gt;</code></li>
	 * 		<li><code>List&lt;?&gt;</code></li>
	 *			</ul>
	 * 	</li>
	 * 	</ul>
	 * Method search example:
	 * 	<ul>
	 * 	<li>declaration: <code>&lt;T&gt;foo(T t)</code></li>
	 * 	<li>pattern: <code>&lt;Exception&gt;foo(new Exception())</code></li>
	 * 	<li>match:
	 * 		<ul>
	 * 		<li><code>&lt;? extends Throwable&gt;foo(new Exception())</code></li>
	 * 		<li><code>&lt;? super RuntimeException&gt;foo(new Exception())</code></li>
	 * 		<li><code>foo(new Exception())</code></li>
	 *			</ul>
	 * 	</ul>
	 * Can be combined to all other match rules, e.g. {@link #R_CASE_SENSITIVE} | {@link #R_EQUIVALENT_MATCH}
	 * This rule is not activated by default, so raw types or equivalent parameterized types will not be found
	 * for pattern List&lt;String&gt;,
	 * This mode is overridden by {@link  #R_ERASURE_MATCH} as erasure matches obviously include equivalent ones.
	 * That means that pattern with rule set to {@link #R_EQUIVALENT_MATCH} | {@link  #R_ERASURE_MATCH}
	 * will return same results than rule only set with {@link  #R_ERASURE_MATCH}.
	 * @since 3.1
	 */
	public static final int R_EQUIVALENT_MATCH = 0x0020;

	/**
	 * Match rule: The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 * @since 3.1
	 */
	public static final int R_FULL_MATCH = 0x0040;

	/**
	 * Match rule: The search pattern contains a Camel Case expression.
	 * For example, <code>NPE</code> type string pattern will match
	 * <code>NullPointerException</code> type.
	 * @see CharOperation#camelCaseMatch(char[], char[]) for a detailed explanation
	 * of Camel Case matching.
	 *<br>
	 * Can be combined to {@link #R_PREFIX_MATCH} match rule. For example,
	 * when prefix match rule is combined with Camel Case match rule,
	 * <code>"nPE"</code> pattern will match <code>nPException</code>.
	 *<br>
	 * Match rule {@link #R_PATTERN_MATCH} may also be combined but both rules
	 * will not be used simultaneously as they are mutually exclusive.
	 * Used match rule depends on whether string pattern contains specific pattern 
	 * characters (e.g. '*' or '?') or not. If it does, then only Pattern match rule
	 * will be used, otherwise only Camel Case match will be used.
	 * For example, with <code>"NPE"</code> string pattern, search will only use
	 * Camel Case match rule, but with <code>N*P*E*</code> string pattern, it will 
	 * use only Pattern match rule.
	 * 
	 * @since 3.2
	 */
	public static final int R_CAMELCASE_MATCH = 0x0080;

	private static final int MODE_MASK = R_EXACT_MATCH | R_PREFIX_MATCH | R_PATTERN_MATCH | R_REGEXP_MATCH;

	private int matchRule;

/**
 * Creates a search pattern with the rule to apply for matching index keys. 
 * It can be exact match, prefix match, pattern match or regexp match.
 * Rule can also be combined with a case sensitivity flag.
 * 
 * @param matchRule one of {@link #R_EXACT_MATCH}, {@link #R_PREFIX_MATCH}, {@link #R_PATTERN_MATCH},
 * 	{@link #R_REGEXP_MATCH}, {@link #R_CAMELCASE_MATCH} combined with one of following values:
 * 	{@link #R_CASE_SENSITIVE}, {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH}.
 *		e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 *		{@link #R_PREFIX_MATCH} if a prefix non case sensitive match is requested or {@link #R_EXACT_MATCH} | {@link #R_ERASURE_MATCH}
 *		if a non case sensitive and erasure match is requested.<br>
 * 	Note that {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH} have no effect
 * 	on non-generic types/methods search.<br>
 * 	Note also that default behavior for generic types/methods search is to find exact matches.
 */
public SearchPattern(int matchRule) {
	this.matchRule = matchRule;
	// Set full match implicit mode
	if ((matchRule & (R_EQUIVALENT_MATCH | R_ERASURE_MATCH )) == 0) {
		this.matchRule |= R_FULL_MATCH;
	}
}

/**
 * Answers true if the pattern matches the given name using CamelCase rules, or false otherwise. 
 * CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive.
 * <br>
 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
 * names follow the lower CamelCase convention.
 * <br>
 * The pattern may contain trailing lowercase characters, which will be match in a case sensitive way. These characters must
 * appear in sequence in the name, after the last matching capital of the pattern. For instance, 'NPExcep' will match
 * 'NullPointerException', but not 'NullPointerExCEPTION'.
 * 
 * For example:
 * <ol>
 * <li><pre>
 *    pattern = "NPE"
 *    name = NullPointerException
 *    result => true
 * </pre>
 * </li>
 * <li><pre>
 *    pattern = "npe"
 *    name = NullPointerException
 *    result => false
 * </pre>
 * </li>
 * </ol>
 * @see CharOperation#camelCaseMatch(char[], char[])
 * 	Implementation has been entirely copied from this method except for array lengthes
 * 	which were obviously replaced with calls to {@link String#length()}.
 * 
 * @param pattern the given pattern
 * @param name the given name
 * @return true if the pattern matches the given name, false otherwise
 * @since 3.2
 */
public static final boolean camelCaseMatch(String pattern, String name) {
	if (pattern == null)
		return true; // null pattern is equivalent to '*'
	if (name == null)
		return false; // null name cannot match

	return camelCaseMatch(pattern, 0, pattern.length(), name, 0, name.length());
}

/**
 * Answers true if a sub-pattern matches the subpart of the given name using CamelCase rules, or false otherwise.  
 * CamelCase matching does NOT accept explicit wild-cards '*' and '?' and is inherently case sensitive. 
 * Can match only subset of name/pattern, considering end positions as non-inclusive.
 * The subpattern is defined by the patternStart and patternEnd positions.
 * <br>
 * CamelCase denotes the convention of writing compound names without spaces, and capitalizing every term.
 * This function recognizes both upper and lower CamelCase, depending whether the leading character is capitalized
 * or not. The leading part of an upper CamelCase pattern is assumed to contain a sequence of capitals which are appearing
 * in the matching name; e.g. 'NPE' will match 'NullPointerException', but not 'NewPerfData'. A lower CamelCase pattern
 * uses a lowercase first character. In Java, type names follow the upper CamelCase convention, whereas method or field
 * names follow the lower CamelCase convention.
 * <br>
 * The pattern may contain trailing lowercase characters, which will be match in a case sensitive way. These characters must
 * appear in sequence in the name, after the last matching capital of the pattern. For instance, 'NPExcep' will match
 * 'NullPointerException', but not 'NullPointerExCEPTION'.
 * 
 * For example:
 * <ol>
 * <li><pre>
 *    pattern = "NPE"
 *    patternStart = 1
 *    patternEnd = 3
 *    name = NullPointerException
 *    nameStart = 0
 *    nameEnd = 20
 *    result => true
 * </pre>
 * </li>
 * <li><pre>
 *    pattern = "npe"
 *    patternStart = 1
 *    patternEnd = 3
 *    name = NullPointerException
 *    nameStart = 0
 *    nameEnd = 20
 *    result => false
 * </pre>
 * </li>
 * </ol>
 * @see CharOperation#camelCaseMatch(char[], int, int, char[], int, int)
 * 	Implementation has been entirely copied from this method except for array lengthes
 * 	which were obviously replaced with calls to {@link String#length()} and
 * 	for array direct access which were replaced with calls to {@link String#charAt(int)}.
 * 
 * @param pattern the given pattern
 * @param patternStart the given pattern start
 * @param patternEnd the given pattern end
 * @param name the given name
 * @param nameStart the given name start
 * @param nameEnd the given name end
 * @return true if a sub-pattern matches the subpart of the given name, false otherwise
 * @since 3.2
 */
public static final boolean camelCaseMatch(String pattern, int patternStart, int patternEnd, String name, int nameStart, int nameEnd) {
	if (name == null)
		return false; // null name cannot match
	if (pattern == null)
		return true; // null pattern is equivalent to '*'
	if (patternEnd < 0) 	patternEnd = pattern.length();
	if (nameEnd < 0) nameEnd = name.length();

	if (patternEnd <= patternStart) return nameEnd <= nameStart;
	if (nameEnd <= nameStart) return false;
	// check first pattern char
	if (name.charAt(nameStart) != pattern.charAt(patternStart)) {
		// first char must strictly match (upper/lower)
		return false;
	}
	char patternChar, nameChar;
	int iPattern = patternStart+1;
	int iName = nameStart+1;
	nextPatternChar: while (iPattern < patternEnd) {
		// check patternChar, keep camelCasing only if uppercase
		if ((patternChar = pattern.charAt(iPattern)) < ScannerHelper.MAX_OBVIOUS) {
			if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[patternChar] & ScannerHelper.C_UPPER_LETTER) == 0) {
				// end of camelCase part of pattern
				break nextPatternChar;
			}
			// still uppercase
		} else if (ScannerHelper.isJavaIdentifierPart(patternChar) 
						&& !ScannerHelper.isUpperCase(patternChar)) {
			// end of camelCase part of pattern
			break nextPatternChar;
		}
		nextNameChar: while (iName < nameEnd) {
			if ((nameChar = name.charAt(iName)) != patternChar) {
				if (nameChar < ScannerHelper.MAX_OBVIOUS) {
					if ((ScannerHelper.OBVIOUS_IDENT_CHAR_NATURES[nameChar] & (ScannerHelper.C_LOWER_LETTER|ScannerHelper.C_SPECIAL |ScannerHelper.C_DIGIT)) != 0) {
						// lowercase/digit char is ignored
						iName++;
						continue nextNameChar;
					}
				} else if (ScannerHelper.isJavaIdentifierPart(nameChar) 
								&& !ScannerHelper.isUpperCase(nameChar)) {
					// lowercase name char is ignored
					iName++;
					continue nextNameChar;
				}
				// mismatch, either uppercase in name or non case char ('/' etc)--> reject
				return false;
			} else {
				// pattern char == name char (uppercase)
				iName++;
				iPattern++;
				continue nextPatternChar;
			}	
		}
		if (iPattern == patternEnd) return true;
		if (iName == nameEnd) return false;
		continue nextPatternChar;
	}
		
	// check trailing part in case sensitive way
	while (iPattern < patternEnd && iName < nameEnd) {
		if (pattern.charAt(iPattern) != name.charAt(iName)) {
			return false;
		}
		iPattern++;
		iName++;
	}
	return iPattern == patternEnd;
}	

/**
 * Returns a search pattern that combines the given two patterns into an
 * "and" pattern. The search result will match both the left pattern and
 * the right pattern.
 *
 * @param leftPattern the left pattern
 * @param rightPattern the right pattern
 * @return an "and" pattern
 */
public static SearchPattern createAndPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	return MatchLocator.createAndPattern(leftPattern, rightPattern);
}

/**
 * Field pattern are formed by [declaringType.]name[ type]
 * e.g. java.lang.String.serialVersionUID long
 *		field*
 */
private static SearchPattern createFieldPattern(String patternString, int limitTo, int matchRule) {
	
	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/); 
	scanner.setSource(patternString.toCharArray());
	final int InsideDeclaringPart = 1;
	final int InsideType = 2;
	int lastToken = -1;
	
	String declaringType = null, fieldName = null;
	String type = null;
	int mode = InsideDeclaringPart;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != TerminalTokens.TokenNameEOF) {
		switch(mode) {
			// read declaring type and fieldName
			case InsideDeclaringPart :
				switch (token) {
					case TerminalTokens.TokenNameDOT:
						if (declaringType == null) {
							if (fieldName == null) return null;
							declaringType = fieldName;
						} else {
							String tokenSource = scanner.getCurrentTokenString();
							declaringType += tokenSource + fieldName;
						}
						fieldName = null;
						break;
					case TerminalTokens.TokenNameWHITESPACE:
						if (!(TerminalTokens.TokenNameWHITESPACE == lastToken || TerminalTokens.TokenNameDOT == lastToken))
							mode = InsideType;
						break;
					default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
						if (fieldName == null)
							fieldName = scanner.getCurrentTokenString();
						else
							fieldName += scanner.getCurrentTokenString();
				}
				break;
			// read type 
			case InsideType:
				switch (token) {
					case TerminalTokens.TokenNameWHITESPACE:
						break;
					default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
						if (type == null)
							type = scanner.getCurrentTokenString();
						else
							type += scanner.getCurrentTokenString();
				}
				break;
		}
		lastToken = token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	if (fieldName == null) return null;

	char[] fieldNameChars = fieldName.toCharArray();
	if (fieldNameChars.length == 1 && fieldNameChars[0] == '*') fieldNameChars = null;
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] typeQualification = null, typeSimpleName = null;

	// extract declaring type infos
	if (declaringType != null) {
		char[] declaringTypePart = declaringType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0) {
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*')
				declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*')
			declaringTypeSimpleName = null;
	}
	// extract type infos
	if (type != null) {
		char[] typePart = type.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
		if (lastDotPosition >= 0) {
			typeQualification = CharOperation.subarray(typePart, 0, lastDotPosition);
			if (typeQualification.length == 1 && typeQualification[0] == '*') {
				typeQualification = null;
			} else {
				// prefix with a '*' as the full qualification could be bigger (because of an import)
				typeQualification = CharOperation.concat(IIndexConstants.ONE_STAR, typeQualification);
			}
			typeSimpleName = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
		} else {
			typeSimpleName = typePart;
		}
		if (typeSimpleName.length == 1 && typeSimpleName[0] == '*')
			typeSimpleName = null;
	}
	// Create field pattern
	boolean findDeclarations = false;
	boolean readAccess = false;
	boolean writeAccess = false;
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			findDeclarations = true;
			break;
		case IJavaSearchConstants.REFERENCES :
			readAccess = true;
			writeAccess = true;
			break;
		case IJavaSearchConstants.READ_ACCESSES :
			readAccess = true;
			break;
		case IJavaSearchConstants.WRITE_ACCESSES :
			writeAccess = true;
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			findDeclarations = true;
			readAccess = true;
			writeAccess = true;
			break;
	}
	return new FieldPattern(
			findDeclarations,
			readAccess,
			writeAccess,
			fieldNameChars,
			declaringTypeQualification,
			declaringTypeSimpleName,
			typeQualification,
			typeSimpleName,
			matchRule);
}

/**
 * Method pattern are formed by:<br>
 * 	[declaringType '.'] ['&lt;' typeArguments '&gt;'] selector ['(' parameterTypes ')'] [returnType]
 *		<br>e.g.<ul>
 *			<li>java.lang.Runnable.run() void</li>
 *			<li>main(*)</li>
 *			<li>&lt;String&gt;toArray(String[])</li>
 *		</ul>
 * Constructor pattern are formed by:<br>
 *		[declaringQualification '.'] ['&lt;' typeArguments '&gt;'] type ['(' parameterTypes ')']
 *		<br>e.g.<ul>
 *			<li>java.lang.Object()</li>
 *			<li>Main(*)</li>
 *			<li>&lt;Exception&gt;Sample(Exception)</li>
 *		</ul>
 * Type arguments have the same pattern that for type patterns
 * @see #createTypePattern(String,int,int,char)
 */
private static SearchPattern createMethodOrConstructorPattern(String patternString, int limitTo, int matchRule, boolean isConstructor) {
	
	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/); 
	scanner.setSource(patternString.toCharArray());
	final int InsideSelector = 1;
	final int InsideTypeArguments = 2;
	final int InsideParameter = 3;
	final int InsideReturnType = 4;
	int lastToken = -1;
	
	String declaringType = null, selector = null, parameterType = null;
	String[] parameterTypes = null;
	char[][] typeArguments = null;
	String typeArgumentsString = null;
	int parameterCount = -1;
	String returnType = null;
	boolean foundClosingParenthesis = false;
	int mode = InsideSelector;
	int token, argCount = 0;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	while (token != TerminalTokens.TokenNameEOF) {
		switch(mode) {
			// read declaring type and selector
			case InsideSelector :
				if (argCount == 0) {
					switch (token) {
						case TerminalTokens.TokenNameLESS:
							argCount++;
							if (selector == null || lastToken == TerminalTokens.TokenNameDOT) {
								if (typeArgumentsString != null) return null; // invalid syntax
								typeArgumentsString = scanner.getCurrentTokenString();
								mode = InsideTypeArguments;
								break;
							}
							if (declaringType == null) {
								declaringType = selector;
							} else {
								declaringType += '.' + selector;
							}
							declaringType += scanner.getCurrentTokenString();
							selector = null;
							break;
						case TerminalTokens.TokenNameDOT:
							if (typeArgumentsString != null) return null; // invalid syntax
							if (declaringType == null) {
								if (selector == null) return null; // invalid syntax
								declaringType = selector;
							} else if (selector != null) {
								declaringType += scanner.getCurrentTokenString() + selector;
							}
							selector = null;
							break;
						case TerminalTokens.TokenNameLPAREN:
							parameterTypes = new String[5];
							parameterCount = 0;
							mode = InsideParameter;
							break;
						case TerminalTokens.TokenNameWHITESPACE:
							switch (lastToken) {
								case TerminalTokens.TokenNameWHITESPACE:
								case TerminalTokens.TokenNameDOT:
								case TerminalTokens.TokenNameGREATER:
								case TerminalTokens.TokenNameRIGHT_SHIFT:
								case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
									break;
								default:
									mode = InsideReturnType;
									break;
							}
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (selector == null)
								selector = scanner.getCurrentTokenString();
							else
								selector += scanner.getCurrentTokenString();
							break;
					}
				} else {
					if (declaringType == null) return null; // invalid syntax
					switch (token) {
						case TerminalTokens.TokenNameGREATER:
						case TerminalTokens.TokenNameRIGHT_SHIFT:
						case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
							argCount--;
							break;
						case TerminalTokens.TokenNameLESS:
							argCount++;
							break;
					}
					declaringType += scanner.getCurrentTokenString();
				}
				break;
			// read type arguments
			case InsideTypeArguments:
				if (typeArgumentsString == null) return null; // invalid syntax
				typeArgumentsString += scanner.getCurrentTokenString();
				switch (token) {
					case TerminalTokens.TokenNameGREATER:
					case TerminalTokens.TokenNameRIGHT_SHIFT:
					case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
						argCount--;
						if (argCount == 0) {
							String pseudoType = "Type"+typeArgumentsString; //$NON-NLS-1$
							typeArguments = Signature.getTypeArguments(Signature.createTypeSignature(pseudoType, false).toCharArray());
							mode = InsideSelector;
						}
						break;
					case TerminalTokens.TokenNameLESS:
						argCount++;
						break;
				}
				break;
			// read parameter types
			case InsideParameter :
				if (argCount == 0) {
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						case TerminalTokens.TokenNameCOMMA:
							if (parameterType == null) return null;
							if (parameterTypes != null) {
								if (parameterTypes.length == parameterCount)
									System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
								parameterTypes[parameterCount++] = parameterType;
							}
							parameterType = null;
							break;
						case TerminalTokens.TokenNameRPAREN:
							foundClosingParenthesis = true;
							if (parameterType != null && parameterTypes != null) {
								if (parameterTypes.length == parameterCount)
									System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
								parameterTypes[parameterCount++] = parameterType;
							}
							mode = isConstructor ? InsideTypeArguments : InsideReturnType;
							break;
						case TerminalTokens.TokenNameLESS:
							argCount++;
							if (parameterType == null) return null; // invalid syntax
							// fall through next case to add token
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (parameterType == null)
								parameterType = scanner.getCurrentTokenString();
							else
								parameterType += scanner.getCurrentTokenString();
					}
				} else {
					if (parameterType == null) return null; // invalid syntax
					switch (token) {
						case TerminalTokens.TokenNameGREATER:
						case TerminalTokens.TokenNameRIGHT_SHIFT:
						case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
							argCount--;
							break;
						case TerminalTokens.TokenNameLESS:
							argCount++;
							break;
					}
					parameterType += scanner.getCurrentTokenString();
				}
				break;
			// read return type
			case InsideReturnType:
				if (argCount == 0) {
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						case TerminalTokens.TokenNameLPAREN:
							parameterTypes = new String[5];
							parameterCount = 0;
							mode = InsideParameter;
							break;
						case TerminalTokens.TokenNameLESS:
							argCount++;
							if (returnType == null) return null; // invalid syntax
							// fall through next case to add token
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (returnType == null)
								returnType = scanner.getCurrentTokenString();
							else
								returnType += scanner.getCurrentTokenString();
					}
				} else {
					if (returnType == null) return null; // invalid syntax
					switch (token) {
						case TerminalTokens.TokenNameGREATER:
						case TerminalTokens.TokenNameRIGHT_SHIFT:
						case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
							argCount--;
							break;
						case TerminalTokens.TokenNameLESS:
							argCount++;
							break;
					}
					returnType += scanner.getCurrentTokenString();
				}
				break;
		}
		lastToken = token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	// parenthesis mismatch
	if (parameterCount>0 && !foundClosingParenthesis) return null;
	// type arguments mismatch
	if (argCount > 0) return null;

	char[] selectorChars = null;
	if (isConstructor) {
		// retrieve type for constructor patterns
		if (declaringType == null)
			declaringType = selector;
		else if (selector != null)
			declaringType += '.' + selector;
	} else {
		// get selector chars
		if (selector == null) return null;
		selectorChars = selector.toCharArray();
		if (selectorChars.length == 1 && selectorChars[0] == '*')
			selectorChars = null;
	}
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] returnTypeQualification = null, returnTypeSimpleName = null;
	char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;
	// Signatures
	String declaringTypeSignature = null;
	String returnTypeSignature = null;
	String[] parameterTypeSignatures = null;

	// extract declaring type infos
	if (declaringType != null) {
		// get declaring type part and signature
		char[] declaringTypePart = null;
		try {
			declaringTypeSignature = Signature.createTypeSignature(declaringType, false);
			if (declaringTypeSignature.indexOf(Signature.C_GENERIC_START) < 0) {
				declaringTypePart = declaringType.toCharArray();
			} else {
				declaringTypePart = Signature.toCharArray(Signature.getTypeErasure(declaringTypeSignature.toCharArray()));
			}
		}
		catch (IllegalArgumentException iae) {
			// declaring type is invalid
			return null;
		}
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0) {
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*')
				declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*')
			declaringTypeSimpleName = null;
	}
	// extract parameter types infos
	if (parameterCount >= 0) {
		parameterTypeQualifications = new char[parameterCount][];
		parameterTypeSimpleNames = new char[parameterCount][];
		parameterTypeSignatures = new String[parameterCount];
		for (int i = 0; i < parameterCount; i++) {
			// get parameter type part and signature
			char[] parameterTypePart = null;
			try {
				if (parameterTypes != null) {
					parameterTypeSignatures[i] = Signature.createTypeSignature(parameterTypes[i], false);
					if (parameterTypeSignatures[i].indexOf(Signature.C_GENERIC_START) < 0) {
						parameterTypePart = parameterTypes[i].toCharArray();
					} else {
						parameterTypePart = Signature.toCharArray(Signature.getTypeErasure(parameterTypeSignatures[i].toCharArray()));
					}
				}
			}
			catch (IllegalArgumentException iae) {
				// string is not a valid type syntax
				return null;
			}
			int lastDotPosition = parameterTypePart==null ? -1 : CharOperation.lastIndexOf('.', parameterTypePart);
			if (parameterTypePart != null && lastDotPosition >= 0) {
				parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
				if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') {
					parameterTypeQualifications[i] = null;
				} else {
					// prefix with a '*' as the full qualification could be bigger (because of an import)
					parameterTypeQualifications[i] = CharOperation.concat(IIndexConstants.ONE_STAR, parameterTypeQualifications[i]);
				}
				parameterTypeSimpleNames[i] = CharOperation.subarray(parameterTypePart, lastDotPosition+1, parameterTypePart.length);
			} else {
				parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = parameterTypePart;
			}
			if (parameterTypeSimpleNames[i].length == 1 && parameterTypeSimpleNames[i][0] == '*')
				parameterTypeSimpleNames[i] = null;
		}
	}	
	// extract return type infos
	if (returnType != null) {
		// get return type part and signature
		char[] returnTypePart = null;
		try {
			returnTypeSignature = Signature.createTypeSignature(returnType, false);
			if (returnTypeSignature.indexOf(Signature.C_GENERIC_START) < 0) {
				returnTypePart = returnType.toCharArray();
			} else {
				returnTypePart = Signature.toCharArray(Signature.getTypeErasure(returnTypeSignature.toCharArray()));
			}
		}
		catch (IllegalArgumentException iae) {
			// declaring type is invalid
			return null;
		}
		int lastDotPosition = CharOperation.lastIndexOf('.', returnTypePart);
		if (lastDotPosition >= 0) {
			returnTypeQualification = CharOperation.subarray(returnTypePart, 0, lastDotPosition);
			if (returnTypeQualification.length == 1 && returnTypeQualification[0] == '*') {
				returnTypeQualification = null;
			} else {
				// because of an import
				returnTypeQualification = CharOperation.concat(IIndexConstants.ONE_STAR, returnTypeQualification);
			}			
			returnTypeSimpleName = CharOperation.subarray(returnTypePart, lastDotPosition+1, returnTypePart.length);
		} else {
			returnTypeSimpleName = returnTypePart;
		}
		if (returnTypeSimpleName.length == 1 && returnTypeSimpleName[0] == '*')
			returnTypeSimpleName = null;
	}
	// Create method/constructor pattern
	boolean findDeclarations = true;
	boolean findReferences = true;
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			findReferences = false;
			break;
		case IJavaSearchConstants.REFERENCES :
			findDeclarations = false;
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			break;
	}
	if (isConstructor) {
		return new ConstructorPattern(
				findDeclarations,
				findReferences,
				declaringTypeSimpleName, 
				declaringTypeQualification,
				declaringTypeSignature,
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				parameterTypeSignatures,
				typeArguments,
				matchRule);
	} else {
		return new MethodPattern(
				findDeclarations,
				findReferences,
				selectorChars,
				declaringTypeQualification,
				declaringTypeSimpleName,
				declaringTypeSignature,
				returnTypeQualification,
				returnTypeSimpleName,
				returnTypeSignature,
				parameterTypeQualifications,
				parameterTypeSimpleNames,
				parameterTypeSignatures,
				typeArguments,
				matchRule);
	}
}

/**
 * Returns a search pattern that combines the given two patterns into an
 * "or" pattern. The search result will match either the left pattern or the
 * right pattern.
 *
 * @param leftPattern the left pattern
 * @param rightPattern the right pattern
 * @return an "or" pattern
 */
public static SearchPattern createOrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
	return new OrPattern(leftPattern, rightPattern);
}

private static SearchPattern createPackagePattern(String patternString, int limitTo, int matchRule) {
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			return new PackageDeclarationPattern(patternString.toCharArray(), matchRule);
		case IJavaSearchConstants.REFERENCES :
			return new PackageReferencePattern(patternString.toCharArray(), matchRule);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new PackageDeclarationPattern(patternString.toCharArray(), matchRule),
				new PackageReferencePattern(patternString.toCharArray(), matchRule)
			);
	}
	return null;
}

/**
 * Returns a search pattern based on a given string pattern. The string patterns support '*' wild-cards.
 * The remaining parameters are used to narrow down the type of expected results.
 *
 * <br>
 *	Examples:
 *	<ul>
 * 		<li>search for case insensitive references to <code>Object</code>:
 *			<code>createSearchPattern("Object", TYPE, REFERENCES, false);</code></li>
 *  	<li>search for case sensitive references to exact <code>Object()</code> constructor:
 *			<code>createSearchPattern("java.lang.Object()", CONSTRUCTOR, REFERENCES, true);</code></li>
 *  	<li>search for implementers of <code>java.lang.Runnable</code>:
 *			<code>createSearchPattern("java.lang.Runnable", TYPE, IMPLEMENTORS, true);</code></li>
 *  </ul>
 * @param stringPattern the given pattern
 * @param searchFor determines the nature of the searched elements
 *	<ul>
 * 	<li>{@link IJavaSearchConstants#CLASS}: only look for classes</li>
 *		<li>{@link IJavaSearchConstants#INTERFACE}: only look for interfaces</li>
 * 	<li>{@link IJavaSearchConstants#ENUM}: only look for enumeration</li>
 *		<li>{@link IJavaSearchConstants#ANNOTATION_TYPE}: only look for annotation type</li>
 * 	<li>{@link IJavaSearchConstants#CLASS_AND_ENUM}: only look for classes and enumerations</li>
 *		<li>{@link IJavaSearchConstants#CLASS_AND_INTERFACE}: only look for classes and interfaces</li>
 * 	<li>{@link IJavaSearchConstants#TYPE}: look for all types (ie. classes, interfaces, enum and annotation types)</li>
 *		<li>{@link IJavaSearchConstants#FIELD}: look for fields</li>
 *		<li>{@link IJavaSearchConstants#METHOD}: look for methods</li>
 *		<li>{@link IJavaSearchConstants#CONSTRUCTOR}: look for constructors</li>
 *		<li>{@link IJavaSearchConstants#PACKAGE}: look for packages</li>
 *	</ul>
 * @param limitTo determines the nature of the expected matches
 *	<ul>
 * 	<li>{@link IJavaSearchConstants#DECLARATIONS}: will search declarations matching
 * 			with the corresponding element. In case the element is a method, declarations of matching
 * 			methods in subtypes will also be found, allowing to find declarations of abstract methods, etc.<br>
 * 			Note that additional flags {@link IJavaSearchConstants#IGNORE_DECLARING_TYPE} and
 * 			{@link IJavaSearchConstants#IGNORE_RETURN_TYPE} are ignored for string patterns.
 * 			This is due to the fact that client may omit to define them in string pattern to have same behavior.
 * 	</li>
 *		 <li>{@link IJavaSearchConstants#REFERENCES}: will search references to the given element.</li>
 *		 <li>{@link IJavaSearchConstants#ALL_OCCURRENCES}: will search for either declarations or
 *				references as specified above.
 *		</li>
 *		 <li>{@link IJavaSearchConstants#IMPLEMENTORS}: for types, will find all types
 *				which directly implement/extend a given interface.
 *				Note that types may be only classes or only interfaces if {@link IJavaSearchConstants#CLASS } or
 *				{@link IJavaSearchConstants#INTERFACE} is respectively used instead of {@link IJavaSearchConstants#TYPE}.
 *		</li>
 *	</ul>
 * @param matchRule one of {@link #R_EXACT_MATCH}, {@link #R_PREFIX_MATCH}, {@link #R_PATTERN_MATCH},
 * 	{@link #R_REGEXP_MATCH}, {@link #R_CAMELCASE_MATCH} combined with one of following values:
 * 	{@link #R_CASE_SENSITIVE}, {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH}.
 *		e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 *		{@link #R_PREFIX_MATCH} if a prefix non case sensitive match is requested or {@link #R_EXACT_MATCH} | {@link #R_ERASURE_MATCH}
 *		if a non case sensitive and erasure match is requested.<br>
 * 	Note that {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH} have no effect
 * 	on non-generic types/methods search.<br>
 * 	Note also that default behavior for generic types/methods search is to find exact matches.
 * @return a search pattern on the given string pattern, or <code>null</code> if the string pattern is ill-formed
 */
public static SearchPattern createPattern(String stringPattern, int searchFor, int limitTo, int matchRule) {
	if (stringPattern == null || stringPattern.length() == 0) return null;

	if ((matchRule = validateMatchRule(stringPattern, matchRule)) == -1) {
		return null;
	}

	// Ignore additional nature flags
	limitTo &= ~(IJavaSearchConstants.IGNORE_DECLARING_TYPE+IJavaSearchConstants.IGNORE_RETURN_TYPE);

	switch (searchFor) {
		case IJavaSearchConstants.CLASS:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.CLASS_SUFFIX);
		case IJavaSearchConstants.CLASS_AND_INTERFACE:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.CLASS_AND_INTERFACE_SUFFIX);
		case IJavaSearchConstants.CLASS_AND_ENUM:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.CLASS_AND_ENUM_SUFFIX);
		case IJavaSearchConstants.INTERFACE:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.INTERFACE_SUFFIX);
		case IJavaSearchConstants.ENUM:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.ENUM_SUFFIX);
		case IJavaSearchConstants.ANNOTATION_TYPE:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.ANNOTATION_TYPE_SUFFIX);
		case IJavaSearchConstants.TYPE:
			return createTypePattern(stringPattern, limitTo, matchRule, IIndexConstants.TYPE_SUFFIX);
		case IJavaSearchConstants.METHOD:
			return createMethodOrConstructorPattern(stringPattern, limitTo, matchRule, false/*not a constructor*/);
		case IJavaSearchConstants.CONSTRUCTOR:
			return createMethodOrConstructorPattern(stringPattern, limitTo, matchRule, true/*constructor*/);
		case IJavaSearchConstants.FIELD:
			return createFieldPattern(stringPattern, limitTo, matchRule);
		case IJavaSearchConstants.PACKAGE:
			return createPackagePattern(stringPattern, limitTo, matchRule);
	}
	return null;
}

/**
 * Returns a search pattern based on a given Java element. 
 * The pattern is used to trigger the appropriate search, and can be parameterized as follows:
 *
 * @param element the Java element the search pattern is based on
 * @param limitTo determines the nature of the expected matches
 *	<ul>
 * 	<li>{@link IJavaSearchConstants#DECLARATIONS}: will search declarations matching
 * 			with the corresponding element. In case the element is a method, declarations of matching
 * 			methods in subtypes will also be found, allowing to find declarations of abstract methods, etc.
 *				Some additional flags may be specified while searching declaration:
 *				<ul>
 *					<li>{@link IJavaSearchConstants#IGNORE_DECLARING_TYPE}: declaring type will be ignored
 *							during the search.<br>
 *							For example using following test case:
 *					<pre>
 *                  class A { A method() { return null; } }
 *                  class B extends A { B method() { return null; } }
 *                  class C { A method() { return null; } }
 *					</pre>
 *							search for <code>method</code> declaration with this flag
 *							will return 2 matches: in A and in C
 *					</li>
 *					<li>{@link IJavaSearchConstants#IGNORE_RETURN_TYPE}: return type will be ignored
 *							during the search.<br>
 *							Using same example, search for <code>method</code> declaration with this flag
 *							will return 2 matches: in A and in B.
 *					</li>
 *				</ul>
 *				Note that these two flags may be combined and both declaring and return types can be ignored
 *				during the search. Then, using same example, search for <code>method</code> declaration
 *				with these 2 flags will return 3 matches: in A, in B  and in C
 * 	</li>
 *		 <li>{@link IJavaSearchConstants#REFERENCES}: will search references to the given element.</li>
 *		 <li>{@link IJavaSearchConstants#ALL_OCCURRENCES}: will search for either declarations or
 *				references as specified above.
 *		</li>
 *		 <li>{@link IJavaSearchConstants#IMPLEMENTORS}: for types, will find all types
 *				which directly implement/extend a given interface.
 *		</li>
 *	</ul>
 * @return a search pattern for a Java element or <code>null</code> if the given element is ill-formed
 */
public static SearchPattern createPattern(IJavaElement element, int limitTo) {
	return createPattern(element, limitTo, R_EXACT_MATCH | R_CASE_SENSITIVE);
}

/**
 * Returns a search pattern based on a given Java element. 
 * The pattern is used to trigger the appropriate search, and can be parameterized as follows:
 *
 * @param element the Java element the search pattern is based on
 * @param limitTo determines the nature of the expected matches
 *	<ul>
 * 	<li>{@link IJavaSearchConstants#DECLARATIONS}: will search declarations matching
 * 			with the corresponding element. In case the element is a method, declarations of matching
 * 			methods in subtypes will also be found, allowing to find declarations of abstract methods, etc.
 *				Some additional flags may be specified while searching declaration:
 *				<ul>
 *					<li>{@link IJavaSearchConstants#IGNORE_DECLARING_TYPE}: declaring type will be ignored
 *							during the search.<br>
 *							For example using following test case:
 *					<pre>
 *                  class A { A method() { return null; } }
 *                  class B extends A { B method() { return null; } }
 *                  class C { A method() { return null; } }
 *					</pre>
 *							search for <code>method</code> declaration with this flag
 *							will return 2 matches: in A and in C
 *					</li>
 *					<li>{@link IJavaSearchConstants#IGNORE_RETURN_TYPE}: return type will be ignored
 *							during the search.<br>
 *							Using same example, search for <code>method</code> declaration with this flag
 *							will return 2 matches: in A and in B.
 *					</li>
 *				</ul>
 *				Note that these two flags may be combined and both declaring and return types can be ignored
 *				during the search. Then, using same example, search for <code>method</code> declaration
 *				with these 2 flags will return 3 matches: in A, in B  and in C
 * 	</li>
 *		 <li>{@link IJavaSearchConstants#REFERENCES}: will search references to the given element.</li>
 *		 <li>{@link IJavaSearchConstants#ALL_OCCURRENCES}: will search for either declarations or
 *				references as specified above.
 *		</li>
 *		 <li>{@link IJavaSearchConstants#IMPLEMENTORS}: for types, will find all types
 *				which directly implement/extend a given interface.
 *		</li>
 *	</ul>
 * @param matchRule one of {@link #R_EXACT_MATCH}, {@link #R_PREFIX_MATCH}, {@link #R_PATTERN_MATCH},
 * 	{@link #R_REGEXP_MATCH}, {@link #R_CAMELCASE_MATCH} combined with one of following values:
 * 	{@link #R_CASE_SENSITIVE}, {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH}.
 *		e.g. {@link #R_EXACT_MATCH} | {@link #R_CASE_SENSITIVE} if an exact and case sensitive match is requested, 
 *		{@link #R_PREFIX_MATCH} if a prefix non case sensitive match is requested or {@link #R_EXACT_MATCH} |{@link #R_ERASURE_MATCH}
 *		if a non case sensitive and erasure match is requested.<br>
 * 	Note that {@link #R_ERASURE_MATCH} or {@link #R_EQUIVALENT_MATCH} have no effect on non-generic types
 * 	or methods search.<br>
 * 	Note also that default behavior for generic types or methods is to find exact matches.
 * @return a search pattern for a Java element or <code>null</code> if the given element is ill-formed
 * @since 3.1
 */
public static SearchPattern createPattern(IJavaElement element, int limitTo, int matchRule) {
	SearchPattern searchPattern = null;
	int lastDot;
	boolean ignoreDeclaringType = false;
	boolean ignoreReturnType = false;
	int maskedLimitTo = limitTo & ~(IJavaSearchConstants.IGNORE_DECLARING_TYPE+IJavaSearchConstants.IGNORE_RETURN_TYPE);
	if (maskedLimitTo == IJavaSearchConstants.DECLARATIONS || maskedLimitTo == IJavaSearchConstants.ALL_OCCURRENCES) {
		ignoreDeclaringType = (limitTo & IJavaSearchConstants.IGNORE_DECLARING_TYPE) != 0;
		ignoreReturnType = (limitTo & IJavaSearchConstants.IGNORE_RETURN_TYPE) != 0;
	}
	char[] declaringSimpleName = null;
	char[] declaringQualification = null;
	switch (element.getElementType()) {
		case IJavaElement.FIELD :
			IField field = (IField) element; 
			if (!ignoreDeclaringType) {
				IType declaringClass = field.getDeclaringType();
				declaringSimpleName = declaringClass.getElementName().toCharArray();
				declaringQualification = declaringClass.getPackageFragment().getElementName().toCharArray();
				char[][] enclosingNames = enclosingTypeNames(declaringClass);
				if (enclosingNames.length > 0) {
					declaringQualification = CharOperation.concat(declaringQualification, CharOperation.concatWith(enclosingNames, '.'), '.');
				}
			}
			char[] name = field.getElementName().toCharArray();
			char[] typeSimpleName = null;
			char[] typeQualification = null;
			String typeSignature = null;
			if (!ignoreReturnType) {
				try {
					typeSignature = field.getTypeSignature();
					char[] signature = typeSignature.toCharArray();
					char[] typeErasure = Signature.toCharArray(Signature.getTypeErasure(signature));
					CharOperation.replace(typeErasure, '$', '.');
					if ((lastDot = CharOperation.lastIndexOf('.', typeErasure)) == -1) {
						typeSimpleName = typeErasure;
					} else {
						typeSimpleName = CharOperation.subarray(typeErasure, lastDot + 1, typeErasure.length);
						typeQualification = CharOperation.subarray(typeErasure, 0, lastDot);
						if (!field.isBinary()) {
							// prefix with a '*' as the full qualification could be bigger (because of an import)
							CharOperation.concat(IIndexConstants.ONE_STAR, typeQualification);
						}
					}
				} catch (JavaModelException e) {
					return null;
				}
			}
			// Create field pattern
			boolean findDeclarations = false;
			boolean readAccess = false;
			boolean writeAccess = false;
			switch (maskedLimitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					findDeclarations = true;
					break;
				case IJavaSearchConstants.REFERENCES :
					readAccess = true;
					writeAccess = true;
					break;
				case IJavaSearchConstants.READ_ACCESSES :
					readAccess = true;
					break;
				case IJavaSearchConstants.WRITE_ACCESSES :
					writeAccess = true;
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					findDeclarations = true;
					readAccess = true;
					writeAccess = true;
					break;
			}
			searchPattern = 
				new FieldPattern(
					findDeclarations,
					readAccess,
					writeAccess,
					name, 
					declaringQualification, 
					declaringSimpleName, 
					typeQualification, 
					typeSimpleName,
					typeSignature,
					matchRule);
			break;
		case IJavaElement.IMPORT_DECLARATION :
			String elementName = element.getElementName();
			lastDot = elementName.lastIndexOf('.');
			if (lastDot == -1) return null; // invalid import declaration
			IImportDeclaration importDecl = (IImportDeclaration)element;
			if (importDecl.isOnDemand()) {
				searchPattern = createPackagePattern(elementName.substring(0, lastDot), maskedLimitTo, matchRule);
			} else {
				searchPattern = 
					createTypePattern(
						elementName.substring(lastDot+1).toCharArray(),
						elementName.substring(0, lastDot).toCharArray(),
						null,
						null,
						null,
						maskedLimitTo,
						matchRule);
			}
			break;
		case IJavaElement.LOCAL_VARIABLE :
			LocalVariable localVar = (LocalVariable) element;
			boolean findVarDeclarations = false;
			boolean findVarReadAccess = false;
			boolean findVarWriteAccess = false;
			switch (maskedLimitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					findVarDeclarations = true;
					break;
				case IJavaSearchConstants.REFERENCES :
					findVarReadAccess = true;
					findVarWriteAccess = true;
					break;
				case IJavaSearchConstants.READ_ACCESSES :
					findVarReadAccess = true;
					break;
				case IJavaSearchConstants.WRITE_ACCESSES :
					findVarWriteAccess = true;
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					findVarDeclarations = true;
					findVarReadAccess = true;
					findVarWriteAccess = true;
					break;
			}
			searchPattern = 
				new LocalVariablePattern(
					findVarDeclarations,
					findVarReadAccess,
					findVarWriteAccess,
					localVar,
					matchRule);
			break;
		case IJavaElement.TYPE_PARAMETER:
			ITypeParameter typeParam = (ITypeParameter) element;
			boolean findParamDeclarations = true;
			boolean findParamReferences = true;
			switch (maskedLimitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					findParamReferences = false;
					break;
				case IJavaSearchConstants.REFERENCES :
					findParamDeclarations = false;
					break;
			}
			searchPattern = 
				new TypeParameterPattern(
					findParamDeclarations,
					findParamReferences,
					typeParam,
					matchRule);
			break;
		case IJavaElement.METHOD :
			IMethod method = (IMethod) element;
			boolean isConstructor;
			try {
				isConstructor = method.isConstructor();
			} catch (JavaModelException e) {
				return null;
			}
			IType declaringClass = method.getDeclaringType();
			if (ignoreDeclaringType) {
				if (isConstructor) declaringSimpleName = declaringClass.getElementName().toCharArray();
			} else {
				declaringSimpleName = declaringClass.getElementName().toCharArray();
				declaringQualification = declaringClass.getPackageFragment().getElementName().toCharArray();
				char[][] enclosingNames = enclosingTypeNames(declaringClass);
				if (enclosingNames.length > 0) {
					declaringQualification = CharOperation.concat(declaringQualification, CharOperation.concatWith(enclosingNames, '.'), '.');
				}
			}
			char[] selector = method.getElementName().toCharArray();
			char[] returnSimpleName = null;
			char[] returnQualification = null;
			String returnSignature = null;
			if (!ignoreReturnType) {
				try {
					returnSignature = method.getReturnType();
					char[] signature = returnSignature.toCharArray();
					char[] returnErasure = Signature.toCharArray(Signature.getTypeErasure(signature));
					CharOperation.replace(returnErasure, '$', '.');
					if ((lastDot = CharOperation.lastIndexOf('.', returnErasure)) == -1) {
						returnSimpleName = returnErasure;
					} else {
						returnSimpleName = CharOperation.subarray(returnErasure, lastDot + 1, returnErasure.length);
						returnQualification = CharOperation.subarray(returnErasure, 0, lastDot);
						if (!method.isBinary()) {
							// prefix with a '*' as the full qualification could be bigger (because of an import)
							CharOperation.concat(IIndexConstants.ONE_STAR, returnQualification);
						}
					}
				} catch (JavaModelException e) {
					return null;
				}
			}
			String[] parameterTypes = method.getParameterTypes();
			int paramCount = parameterTypes.length;
			char[][] parameterSimpleNames = new char[paramCount][];
			char[][] parameterQualifications = new char[paramCount][];
			String[] parameterSignatures = new String[paramCount];
			for (int i = 0; i < paramCount; i++) {
				parameterSignatures[i] = parameterTypes[i];
				char[] signature = parameterSignatures[i].toCharArray();
				char[] paramErasure = Signature.toCharArray(Signature.getTypeErasure(signature));
				CharOperation.replace(paramErasure, '$', '.');
				if ((lastDot = CharOperation.lastIndexOf('.', paramErasure)) == -1) {
					parameterSimpleNames[i] = paramErasure;
					parameterQualifications[i] = null;
				} else {
					parameterSimpleNames[i] = CharOperation.subarray(paramErasure, lastDot + 1, paramErasure.length);
					parameterQualifications[i] = CharOperation.subarray(paramErasure, 0, lastDot);
					if (!method.isBinary()) {
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						CharOperation.concat(IIndexConstants.ONE_STAR, parameterQualifications[i]);
					}
				}
			}

			// Create method/constructor pattern
			boolean findMethodDeclarations = true;
			boolean findMethodReferences = true;
			switch (maskedLimitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					findMethodReferences = false;
					break;
				case IJavaSearchConstants.REFERENCES :
					findMethodDeclarations = false;
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					break;
			}
			if (isConstructor) {
				searchPattern =
					new ConstructorPattern(
						findMethodDeclarations,
						findMethodReferences,
						declaringSimpleName, 
						declaringQualification, 
						parameterQualifications, 
						parameterSimpleNames,
						parameterSignatures,
						method,
						matchRule);
			} else {
				searchPattern =
					new MethodPattern(
						findMethodDeclarations,
						findMethodReferences,
						selector, 
						declaringQualification, 
						declaringSimpleName, 
						returnQualification, 
						returnSimpleName, 
						returnSignature,
						parameterQualifications, 
						parameterSimpleNames,
						parameterSignatures,
						method,
						matchRule);
			}
			break;
		case IJavaElement.TYPE :
			IType type = (IType)element;
			searchPattern = 	createTypePattern(
						type.getElementName().toCharArray(), 
						type.getPackageFragment().getElementName().toCharArray(),
						ignoreDeclaringType ? null : enclosingTypeNames(type),
						null,
						type,
						maskedLimitTo,
						matchRule);
			break;
		case IJavaElement.PACKAGE_DECLARATION :
		case IJavaElement.PACKAGE_FRAGMENT :
			searchPattern = createPackagePattern(element.getElementName(), maskedLimitTo, matchRule);
			break;
	}
	if (searchPattern != null)
		MatchLocator.setFocus(searchPattern, element);
	return searchPattern;
}

private static SearchPattern createTypePattern(char[] simpleName, char[] packageName, char[][] enclosingTypeNames, String typeSignature, IType type, int limitTo, int matchRule) {
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			return new TypeDeclarationPattern(
				packageName, 
				enclosingTypeNames, 
				simpleName, 
				IIndexConstants.TYPE_SUFFIX,
				matchRule);
		case IJavaSearchConstants.REFERENCES :
			if (type != null) {
				return new TypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName,
					type,
					matchRule);
			}
			return new TypeReferencePattern(
				CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
				simpleName,
				typeSignature,
				matchRule);
		case IJavaSearchConstants.IMPLEMENTORS : 
			return new SuperTypeReferencePattern(
				CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
				simpleName,
				SuperTypeReferencePattern.ONLY_SUPER_INTERFACES,
				matchRule);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new TypeDeclarationPattern(
					packageName, 
					enclosingTypeNames, 
					simpleName, 
					IIndexConstants.TYPE_SUFFIX,
					matchRule), 
				(type != null)
					? new TypeReferencePattern(
						CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
						simpleName,
						type,
						matchRule)
					: new TypeReferencePattern(
						CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
						simpleName,
						typeSignature,
						matchRule)
			);
	}
	return null;
}
/**
 * Type pattern are formed by [qualification '.']type [typeArguments].
 * e.g. java.lang.Object
 *		Runnable
 *		List&lt;String&gt;
 *
 * @since 3.1
 *		Type arguments can be specified to search references to parameterized types.
 * 	and look as follow: '&lt;' { [ '?' {'extends'|'super'} ] type ( ',' [ '?' {'extends'|'super'} ] type )* | '?' } '&gt;'
 * 	Please note that:
 * 		- '*' is not valid inside type arguments definition &lt;&gt;
 * 		- '?' is treated as a wildcard when it is inside &lt;&gt; (ie. it must be put on first position of the type argument)
 */
private static SearchPattern createTypePattern(String patternString, int limitTo, int matchRule, char indexSuffix) {
	
	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/); 
	scanner.setSource(patternString.toCharArray());
	String type = null;
	int token;
	try {
		token = scanner.getNextToken();
	} catch (InvalidInputException e) {
		return null;
	}
	int argCount = 0;
	while (token != TerminalTokens.TokenNameEOF) {
		if (argCount == 0) {
			switch (token) {
				case TerminalTokens.TokenNameWHITESPACE:
					break;
				case TerminalTokens.TokenNameLESS:
					argCount++;
					// fall through default case to add token to type
				default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
					if (type == null)
						type = scanner.getCurrentTokenString();
					else
						type += scanner.getCurrentTokenString();
			}
		} else {
			switch (token) {
				case TerminalTokens.TokenNameGREATER:
				case TerminalTokens.TokenNameRIGHT_SHIFT:
				case TerminalTokens.TokenNameUNSIGNED_RIGHT_SHIFT:
					argCount--;
					break;
				case TerminalTokens.TokenNameLESS:
					argCount++;
					break;
			}
			if (type == null) return null; // invalid syntax
			type += scanner.getCurrentTokenString();
		}
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
	}
	if (type == null) return null;
	String typeSignature = null;
	char[] qualificationChars = null, typeChars = null;

	// get type part and signature
	char[] typePart = null;
	try {
		typeSignature = Signature.createTypeSignature(type, false);
		if (typeSignature.indexOf(Signature.C_GENERIC_START) < 0) {
			typePart = type.toCharArray();
		} else {
			typePart = Signature.toCharArray(Signature.getTypeErasure(typeSignature.toCharArray()));
		}
	}
	catch (IllegalArgumentException iae) {
		// string is not a valid type syntax
		return null;
	}

	// get qualification name
	int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
	if (lastDotPosition >= 0) {
		qualificationChars = CharOperation.subarray(typePart, 0, lastDotPosition);
		if (qualificationChars.length == 1 && qualificationChars[0] == '*')
			qualificationChars = null;
		typeChars = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
	} else {
		typeChars = typePart;
	}
	if (typeChars.length == 1 && typeChars[0] == '*') {
		typeChars = null;
	}
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS : // cannot search for explicit member types
			return new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, indexSuffix, matchRule);
		case IJavaSearchConstants.REFERENCES :
			return new TypeReferencePattern(qualificationChars, typeChars, typeSignature, matchRule);
		case IJavaSearchConstants.IMPLEMENTORS : 
			return new SuperTypeReferencePattern(qualificationChars, typeChars, SuperTypeReferencePattern.ONLY_SUPER_INTERFACES, indexSuffix, matchRule);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, indexSuffix, matchRule),// cannot search for explicit member types
				new TypeReferencePattern(qualificationChars, typeChars, matchRule));
	}
	return null;
}
/**
 * Returns the enclosing type names of the given type.
 */
private static char[][] enclosingTypeNames(IType type) {
	IJavaElement parent = type.getParent();
	switch (parent.getElementType()) {
		case IJavaElement.CLASS_FILE:
			// For a binary type, the parent is not the enclosing type, but the declaring type is.
			// (see bug 20532  Declaration of member binary type not found)
			IType declaringType = type.getDeclaringType();
			if (declaringType == null) return CharOperation.NO_CHAR_CHAR;
			return CharOperation.arrayConcat(
				enclosingTypeNames(declaringType), 
				declaringType.getElementName().toCharArray());
		case IJavaElement.COMPILATION_UNIT:
			return CharOperation.NO_CHAR_CHAR;
		case IJavaElement.FIELD:
		case IJavaElement.INITIALIZER:
		case IJavaElement.METHOD:
			IType declaringClass = ((IMember) parent).getDeclaringType();
			return CharOperation.arrayConcat(
				enclosingTypeNames(declaringClass),
				new char[][] {declaringClass.getElementName().toCharArray(), IIndexConstants.ONE_STAR});
		case IJavaElement.TYPE:
			return CharOperation.arrayConcat(
				enclosingTypeNames((IType)parent), 
				parent.getElementName().toCharArray());
		default:
			return null;
	}
}

/**
 * Decode the given index key in this pattern. The decoded index key is used by 
 * {@link #matchesDecodedKey(SearchPattern)} to find out if the corresponding index entry 
 * should be considered.
 * <p>
 * This method should be re-implemented in subclasses that need to decode an index key.
 * </p>
 * 
 * @param key the given index key
 */
public void decodeIndexKey(char[] key) {
	// called from findIndexMatches(), override as necessary
}
/**
 * Returns a blank pattern that can be used as a record to decode an index key.
 * <p>
 * Implementors of this method should return a new search pattern that is going to be used
 * to decode index keys.
 * </p>
 * 
 * @return a new blank pattern
 * @see #decodeIndexKey(char[])
 */
public abstract SearchPattern getBlankPattern();
/**
 * Returns a key to find in relevant index categories, if null then all index entries are matched.
 * The key will be matched according to some match rule. These potential matches
 * will be further narrowed by the match locator, but precise match locating can be expensive,
 * and index query should be as accurate as possible so as to eliminate obvious false hits.
 * <p>
 * This method should be re-implemented in subclasses that need to narrow down the
 * index query.
 * </p>
 * 
 * @return an index key from this pattern, or <code>null</code> if all index entries are matched.
 */
public char[] getIndexKey() {
	return null; // called from queryIn(), override as necessary
}
/**
 * Returns an array of index categories to consider for this index query.
 * These potential matches will be further narrowed by the match locator, but precise
 * match locating can be expensive, and index query should be as accurate as possible
 * so as to eliminate obvious false hits.
 * <p>
 * This method should be re-implemented in subclasses that need to narrow down the
 * index query.
 * </p>
 * 
 * @return an array of index categories
 */
public char[][] getIndexCategories() {
	return CharOperation.NO_CHAR_CHAR; // called from queryIn(), override as necessary
}
/**
 * Returns the rule to apply for matching index keys. Can be exact match, prefix match, pattern match or regexp match.
 * Rule can also be combined with a case sensitivity flag.
 * 
 * @return one of R_EXACT_MATCH, R_PREFIX_MATCH, R_PATTERN_MATCH, R_REGEXP_MATCH combined with R_CASE_SENSITIVE,
 *   e.g. R_EXACT_MATCH | R_CASE_SENSITIVE if an exact and case sensitive match is requested, 
 *   or R_PREFIX_MATCH if a prefix non case sensitive match is requested.
 * [TODO (frederic) I hope R_ERASURE_MATCH doesn't need to be on this list. Because it would be a breaking API change.]
 */	
public final int getMatchRule() {
	return this.matchRule;
}
/**
 * Returns whether this pattern matches the given pattern (representing a decoded index key).
 * <p>
 * This method should be re-implemented in subclasses that need to narrow down the
 * index query.
 * </p>
 * 
 * @param decodedPattern a pattern representing a decoded index key
 * @return whether this pattern matches the given pattern
 */
public boolean matchesDecodedKey(SearchPattern decodedPattern) {
	return true; // called from findIndexMatches(), override as necessary if index key is encoded
}

/**
 * Returns whether the given name matches the given pattern.
 * <p>
 * This method should be re-implemented in subclasses that need to define how
 * a name matches a pattern.
 * </p>
 * 
 * @param pattern the given pattern, or <code>null</code> to represent "*"
 * @param name the given name
 * @return whether the given name matches the given pattern
 */
public boolean matchesName(char[] pattern, char[] name) {
	if (pattern == null) return true; // null is as if it was "*"
	if (name != null) {
		boolean isCaseSensitive = (this.matchRule & R_CASE_SENSITIVE) != 0;
		boolean isCamelCase = (this.matchRule & R_CAMELCASE_MATCH) != 0;
		int matchMode = this.matchRule & MODE_MASK;
		boolean matchFirstChar = !isCaseSensitive || pattern.length == 0 || (name.length > 0 &&  pattern[0] == name[0]);
		if (isCamelCase && matchFirstChar && CharOperation.camelCaseMatch(pattern, name)) {
			return true;
		}
		switch (matchMode) {
			case R_EXACT_MATCH :
			case R_FULL_MATCH :
				if (isCamelCase) return false;
				return matchFirstChar && CharOperation.equals(pattern, name, isCaseSensitive);

			case R_PREFIX_MATCH :
				return matchFirstChar && CharOperation.prefixEquals(pattern, name, isCaseSensitive);

			case R_PATTERN_MATCH :
				if (!isCaseSensitive)
					pattern = CharOperation.toLowerCase(pattern);
				return CharOperation.match(pattern, name, isCaseSensitive);

			case R_REGEXP_MATCH :
				// TODO (frederic) implement regular expression match
				return true;
		}
	}
	return false;
}

/**
 * Validate compatibility between given string pattern and match rule.
 *<br>
 * Optimized (ie. returned match rule is modified) combinations are:
 * <ul>
 * 	<li>{@link #R_PATTERN_MATCH} without any '*' or '?' in string pattern:
 * 		pattern match bit is unset,
 * 	</li>
 * 	<li>{@link #R_PATTERN_MATCH} and {@link #R_PREFIX_MATCH}  bits simultaneously set:
 * 		prefix match bit is unset,
 * 	</li>
 * 	<li>{@link #R_PATTERN_MATCH} and {@link #R_CAMELCASE_MATCH}  bits simultaneously set:
 * 		camel case match bit is unset,
 * 	</li>
 * 	<li>{@link #R_CAMELCASE_MATCH} with invalid combination of uppercase and lowercase characters:
 * 		camel case match bit is unset and replaced with prefix match pattern,
 * 	</li>
 * 	<li>{@link #R_CAMELCASE_MATCH} combined with {@link #R_PREFIX_MATCH} and {@link #R_CASE_SENSITIVE}
 * 		bits is reduced to only {@link #R_CAMELCASE_MATCH} as Camel Case search is already prefix and case sensitive,
 * 	</li>
 * </ul>
 *<br>
 * Rejected (ie. returned match rule -1) combinations are:
 * <ul>
 * 	<li>{@link #R_REGEXP_MATCH} with any other match mode bit set,
 * 	</li>
 * </ul>
 *
 * @param stringPattern The string pattern
 * @param matchRule The match rule
 * @return Optimized valid match rule or -1 if an incompatibility was detected.
 * @since 3.2
 */
public static int validateMatchRule(String stringPattern, int matchRule) {

	// Verify Regexp match rule
	if ((matchRule & R_REGEXP_MATCH) != 0) {
		if ((matchRule & R_PATTERN_MATCH) != 0 || (matchRule & R_PREFIX_MATCH) != 0 || (matchRule & R_CAMELCASE_MATCH) != 0) {
			return -1;
		}
	}

	// Verify Pattern match rule
	if ((matchRule & R_PATTERN_MATCH) != 0) {
		if ((matchRule & R_PREFIX_MATCH) != 0) {
			matchRule &= ~R_PREFIX_MATCH;
		}
		int starIndex = stringPattern.indexOf('*');
		int questionIndex = stringPattern.indexOf('?');
		if (starIndex < 0 && questionIndex < 0) {
			// No need to have pattern match
			matchRule &= ~R_PATTERN_MATCH;
		} else {
			// Remove Camel Case match when there's '*' or '?' characters
			if ((matchRule & R_CAMELCASE_MATCH) != 0) {
				matchRule &= ~R_CAMELCASE_MATCH;
			}
		}
	}

	// Verify Camel Case match rule
	if ((matchRule & R_CAMELCASE_MATCH) != 0) {
		// Verify sting pattern validity
		int length = stringPattern.length();
		boolean validCamelCase = false;
		if (length > 1) {
			int idx = 0;
			char ch = stringPattern.charAt(idx++);
			if (ScannerHelper.isJavaIdentifierStart(ch)) {
				ch = stringPattern.charAt(idx++);
				if (ScannerHelper.isUpperCase(ch)) {
					while (idx<length && ScannerHelper.isUpperCase(stringPattern.charAt(idx))) {
						idx++;
					}
					while (idx<length && (!ScannerHelper.isUpperCase(ch=stringPattern.charAt(idx)) && ScannerHelper.isJavaIdentifierPart(ch))) {
						idx++;
					}
					validCamelCase = idx == length;
				}
			}
		}
		// Verify bits compatibility
		if (validCamelCase) {
			if ((matchRule & R_PREFIX_MATCH) != 0) {
				if ((matchRule & R_CASE_SENSITIVE) != 0) {
					// This is equivalent to Camel Case match rule
					matchRule &= ~R_PREFIX_MATCH;
					matchRule &= ~R_CASE_SENSITIVE;
				}
			}
		} else {
			matchRule &= ~R_CAMELCASE_MATCH;
			if ((matchRule & R_PREFIX_MATCH) == 0) {
				matchRule |= R_PREFIX_MATCH;
				matchRule |= R_CASE_SENSITIVE;
			}
		}
	}
	return matchRule;
}

/**
 * @see java.lang.Object#toString()
 */
public String toString() {
	return "SearchPattern"; //$NON-NLS-1$
}
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.matching.*;
import org.eclipse.jdt.internal.core.search.pattern.InternalSearchPattern;

/**
 * TODO (jerome) spec
 * @since 3.0
 */
public abstract class SearchPattern extends InternalSearchPattern implements IIndexConstants, IJavaSearchConstants {

	/**
	 * Rules for pattern matching: (exact, prefix, pattern) [ | case sensitive]
	 */
	/**
	 * Match rule: The search pattern matches exactly the search result,
	 * that is, the source of the search result equals the search pattern.
	 */
	public static final int R_EXACT_MATCH = EXACT_MATCH;
	/**
	 * Match rule: The search pattern is a prefix of the search result.
	 */
	public static final int R_PREFIX_MATCH = PREFIX_MATCH;
	/**
	 * Match rule: The search pattern contains one or more wild cards ('*') where a 
	 * wild-card can replace 0 or more characters in the search result.
	 */
	public static final int R_PATTERN_MATCH = PATTERN_MATCH;
	/**
	 * Match rule: The search pattern contains a regular expression.
	 */
	public static final int R_REGEXP_MATCH = 3;
	/**
	 * Match rule: The search pattern matches the search result only if cases are the same.
	 * Can be combined to previous rules, e.g. R_EXACT_MATCH | R_CASE_SENSITIVE
	 */
	public static final int R_CASE_SENSITIVE = 4;


	public final int kind;
	public final boolean isCaseSensitive;
	public final int matchMode;

	/* focus element (used for reference patterns*/
	public IJavaElement focus;

	public SearchPattern(int patternKind, int matchRule) {
		this.kind = patternKind;
		this.isCaseSensitive = (matchRule & R_CASE_SENSITIVE) != 0;
		this.matchMode = matchRule - (this.isCaseSensitive ? R_CASE_SENSITIVE : 0);
	}

	/**
	 * Returns a search pattern that combines the given two patterns into a "and" pattern.
	 * The search result will match both the left pattern and the right pattern.
	 *
	 * @param leftPattern the left pattern
	 * @param rightPattern the right pattern
	 * @return a "and" pattern
	 */
	public static SearchPattern createAndPattern(final SearchPattern leftPattern, final SearchPattern rightPattern) {
		return new AndPattern(0/*no kind*/, 0/*no rule*/) {
			SearchPattern current = leftPattern;
			public SearchPattern currentPattern() {
				return current;
			}
			protected boolean hasNextQuery() {
				if (current == leftPattern) {
					current = rightPattern;
					return true;
				}
				return false; 
			}
			protected void resetQuery() {
				current = leftPattern;
			}
		};
	}
	
	/**
	 * Constructor pattern are formed by [declaringQualification.]type[(parameterTypes)]
	 * e.g. java.lang.Object()
	 *		Main(*)
	 */
	private static SearchPattern createConstructorPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
	
		int matchRule = isCaseSensitive ? matchMode | R_CASE_SENSITIVE : matchMode;
		
		Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		scanner.setSource(patternString.toCharArray());
		final int InsideName = 1;
		final int InsideParameter = 2;
		
		String declaringQualification = null, typeName = null, parameterType = null;
		String[] parameterTypes = null;
		int parameterCount = -1;
		boolean foundClosingParenthesis = false;
		int mode = InsideName;
		int token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
		while (token != TerminalTokens.TokenNameEOF) {
			switch(mode) {
				// read declaring type and selector
				case InsideName :
					switch (token) {
						case TerminalTokens.TokenNameDOT:
							if (declaringQualification == null) {
								if (typeName == null) return null;
								declaringQualification = typeName;
							} else {
								String tokenSource = new String(scanner.getCurrentTokenSource());
								declaringQualification += tokenSource + typeName;
							}
							typeName = null;
							break;
						case TerminalTokens.TokenNameLPAREN:
							parameterTypes = new String[5];
							parameterCount = 0;
							mode = InsideParameter;
							break;
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (typeName == null)
								typeName = new String(scanner.getCurrentTokenSource());
							else
								typeName += new String(scanner.getCurrentTokenSource());
					}
					break;
				// read parameter types
				case InsideParameter :
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						case TerminalTokens.TokenNameCOMMA:
							if (parameterType == null) return null;
							if (parameterTypes.length == parameterCount)
								System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
							parameterTypes[parameterCount++] = parameterType;
							parameterType = null;
							break;
						case TerminalTokens.TokenNameRPAREN:
							foundClosingParenthesis = true;
							if (parameterType != null) {
								if (parameterTypes.length == parameterCount)
									System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
								parameterTypes[parameterCount++] = parameterType;
							}
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (parameterType == null)
								parameterType = new String(scanner.getCurrentTokenSource());
							else
								parameterType += new String(scanner.getCurrentTokenSource());
					}
					break;
			}
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
				return null;
			}
		}
		// parenthesis mismatch
		if (parameterCount>0 && !foundClosingParenthesis) return null;
		if (typeName == null) return null;
	
		char[] typeNameChars = typeName.toCharArray();
		if (typeNameChars.length == 1 && typeNameChars[0] == '*') typeNameChars = null;
			
		char[] declaringQualificationChars = null;
		if (declaringQualification != null) declaringQualificationChars = declaringQualification.toCharArray();
		char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;
	
		// extract parameter types infos
		if (parameterCount >= 0) {
			parameterTypeQualifications = new char[parameterCount][];
			parameterTypeSimpleNames = new char[parameterCount][];
			for (int i = 0; i < parameterCount; i++) {
				char[] parameterTypePart = parameterTypes[i].toCharArray();
				int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
				if (lastDotPosition >= 0) {
					parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
					if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') {
						parameterTypeQualifications[i] = null;
					} else {
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						parameterTypeQualifications[i] = CharOperation.concat(ONE_STAR, parameterTypeQualifications[i]);
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
		switch (limitTo) {
			case IJavaSearchConstants.DECLARATIONS :
				return new ConstructorPattern(
					true,
					false,
					typeNameChars, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					matchRule);
			case IJavaSearchConstants.REFERENCES :
				return new ConstructorPattern(
					false,
					true,
					typeNameChars, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					matchRule);
			case IJavaSearchConstants.ALL_OCCURRENCES :
				return new ConstructorPattern(
					true,
					true,
					typeNameChars, 
					declaringQualificationChars, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					matchRule);
		}
		return null;
	}
	/**
	 * Field pattern are formed by [declaringType.]name[type]
	 * e.g. java.lang.String.serialVersionUID long
	 *		field*
	 */
	private static SearchPattern createFieldPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
		
		int matchRule = isCaseSensitive ? matchMode | R_CASE_SENSITIVE : matchMode;
	
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
								String tokenSource = new String(scanner.getCurrentTokenSource());
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
								fieldName = new String(scanner.getCurrentTokenSource());
							else
								fieldName += new String(scanner.getCurrentTokenSource());
					}
					break;
				// read type 
				case InsideType:
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (type == null)
								type = new String(scanner.getCurrentTokenSource());
							else
								type += new String(scanner.getCurrentTokenSource());
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
				declaringTypeQualification = null;
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
					typeQualification = CharOperation.concat(ONE_STAR, typeQualification);
				}
				typeSimpleName = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
			} else {
				typeQualification = null;
				typeSimpleName = typePart;
			}
			if (typeSimpleName.length == 1 && typeSimpleName[0] == '*')
				typeSimpleName = null;
		}
		switch (limitTo) {
			case IJavaSearchConstants.DECLARATIONS :
				return new FieldPattern(
					true,
					false,
					false,
					fieldNameChars,
					declaringTypeQualification,
					declaringTypeSimpleName,
					typeQualification,
					typeSimpleName,
					matchRule);
			case IJavaSearchConstants.REFERENCES :
				return new FieldPattern(
					false,
					true, // read access
					true, // write access
					fieldNameChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					matchRule);
			case IJavaSearchConstants.READ_ACCESSES :
				return new FieldPattern(
					false,
					true, // read access only
					false,
					fieldNameChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					matchRule);
			case IJavaSearchConstants.WRITE_ACCESSES :
				return new FieldPattern(
					false,
					false,
					true, // write access only
					fieldNameChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					matchRule);
			case IJavaSearchConstants.ALL_OCCURRENCES :
				return new FieldPattern(
					true,
					true, // read access
					true, // write access
					fieldNameChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					typeQualification, 
					typeSimpleName,
					matchRule);
		}
		return null;
	}
	/**
	 * Method pattern are formed by [declaringType.]selector[(parameterTypes)][returnType]
	 * e.g. java.lang.Runnable.run() void
	 *		main(*)
	 */
	private static SearchPattern createMethodPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
		
		int matchRule = isCaseSensitive ? matchMode | R_CASE_SENSITIVE : matchMode;
	
		Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/); 
		scanner.setSource(patternString.toCharArray());
		final int InsideSelector = 1;
		final int InsideParameter = 2;
		final int InsideReturnType = 3;
		int lastToken = -1;
		
		String declaringType = null, selector = null, parameterType = null;
		String[] parameterTypes = null;
		int parameterCount = -1;
		String returnType = null;
		boolean foundClosingParenthesis = false;
		int mode = InsideSelector;
		int token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
		while (token != TerminalTokens.TokenNameEOF) {
			switch(mode) {
				// read declaring type and selector
				case InsideSelector :
					switch (token) {
						case TerminalTokens.TokenNameDOT:
							if (declaringType == null) {
								if (selector == null) return null;
								declaringType = selector;
							} else {
								String tokenSource = new String(scanner.getCurrentTokenSource());
								declaringType += tokenSource + selector;
							}
							selector = null;
							break;
						case TerminalTokens.TokenNameLPAREN:
							parameterTypes = new String[5];
							parameterCount = 0;
							mode = InsideParameter;
							break;
						case TerminalTokens.TokenNameWHITESPACE:
							if (!(TerminalTokens.TokenNameWHITESPACE == lastToken || TerminalTokens.TokenNameDOT == lastToken))
								mode = InsideReturnType;
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (selector == null)
								selector = new String(scanner.getCurrentTokenSource());
							else
								selector += new String(scanner.getCurrentTokenSource());
							break;
					}
					break;
				// read parameter types
				case InsideParameter :
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						case TerminalTokens.TokenNameCOMMA:
							if (parameterType == null) return null;
							if (parameterTypes.length == parameterCount)
								System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
							parameterTypes[parameterCount++] = parameterType;
							parameterType = null;
							break;
						case TerminalTokens.TokenNameRPAREN:
							foundClosingParenthesis = true;
							if (parameterType != null){
								if (parameterTypes.length == parameterCount)
									System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
								parameterTypes[parameterCount++] = parameterType;
							}
							mode = InsideReturnType;
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (parameterType == null)
								parameterType = new String(scanner.getCurrentTokenSource());
							else
								parameterType += new String(scanner.getCurrentTokenSource());
					}
					break;
				// read return type
				case InsideReturnType:
					switch (token) {
						case TerminalTokens.TokenNameWHITESPACE:
							break;
						default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
							if (returnType == null)
								returnType = new String(scanner.getCurrentTokenSource());
							else
								returnType += new String(scanner.getCurrentTokenSource());
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
		if (selector == null) return null;
	
		char[] selectorChars = selector.toCharArray();
		if (selectorChars.length == 1 && selectorChars[0] == '*')
			selectorChars = null;
			
		char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
		char[] returnTypeQualification = null, returnTypeSimpleName = null;
		char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;
	
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
				declaringTypeQualification = null;
				declaringTypeSimpleName = declaringTypePart;
			}
			if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*')
				declaringTypeSimpleName = null;
		}
		// extract parameter types infos
		if (parameterCount >= 0) {
			parameterTypeQualifications = new char[parameterCount][];
			parameterTypeSimpleNames = new char[parameterCount][];
			for (int i = 0; i < parameterCount; i++) {
				char[] parameterTypePart = parameterTypes[i].toCharArray();
				int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
				if (lastDotPosition >= 0) {
					parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
					if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') {
						parameterTypeQualifications[i] = null;
					} else {
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						parameterTypeQualifications[i] = CharOperation.concat(ONE_STAR, parameterTypeQualifications[i]);
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
			char[] returnTypePart = returnType.toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', returnTypePart);
			if (lastDotPosition >= 0) {
				returnTypeQualification = CharOperation.subarray(returnTypePart, 0, lastDotPosition);
				if (returnTypeQualification.length == 1 && returnTypeQualification[0] == '*') {
					returnTypeQualification = null;
				} else {
					// because of an import
					returnTypeQualification = CharOperation.concat(ONE_STAR, returnTypeQualification);
				}			
				returnTypeSimpleName = CharOperation.subarray(returnTypePart, lastDotPosition+1, returnTypePart.length);
			} else {
				returnTypeQualification = null;
				returnTypeSimpleName = returnTypePart;
			}
			if (returnTypeSimpleName.length == 1 && returnTypeSimpleName[0] == '*')
				returnTypeSimpleName = null;
		}
		switch (limitTo) {
			case IJavaSearchConstants.DECLARATIONS :
				return new MethodPattern(
					true,
					false,
					selectorChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null,
					matchRule);
			case IJavaSearchConstants.REFERENCES :
				return new MethodPattern(
					false,
					true,
					selectorChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null,
					matchRule);
			case IJavaSearchConstants.ALL_OCCURRENCES :
				return new MethodPattern(
					true,
					true,
					selectorChars, 
					declaringTypeQualification, 
					declaringTypeSimpleName, 
					returnTypeQualification, 
					returnTypeSimpleName, 
					parameterTypeQualifications, 
					parameterTypeSimpleNames,
					null,
					matchRule);
		}
		return null;
	}
	/**
	 * Returns a search pattern that combines the given two patterns into a "or" pattern.
	 * The search result will match either the left pattern or the right pattern.
	 *
	 * @param leftPattern the left pattern
	 * @param rightPattern the right pattern
	 * @return a "or" pattern
	 */
	public static SearchPattern createOrPattern(SearchPattern leftPattern, SearchPattern rightPattern) {
		return new OrPattern(leftPattern, rightPattern);
	}
	private static SearchPattern createPackagePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
		int matchRule = isCaseSensitive ? matchMode | R_CASE_SENSITIVE : matchMode;
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
	public static SearchPattern createPattern(String patternString, int searchFor, int limitTo, int matchMode, boolean isCaseSensitive) {
		if (patternString == null || patternString.length() == 0) return null;
	
		switch (searchFor) {
			case IJavaSearchConstants.TYPE:
				return createTypePattern(patternString, limitTo, matchMode, isCaseSensitive);
			case IJavaSearchConstants.METHOD:
				return createMethodPattern(patternString, limitTo, matchMode, isCaseSensitive);
			case IJavaSearchConstants.CONSTRUCTOR:
				return createConstructorPattern(patternString, limitTo, matchMode, isCaseSensitive);
			case IJavaSearchConstants.FIELD:
				return createFieldPattern(patternString, limitTo, matchMode, isCaseSensitive);
			case IJavaSearchConstants.PACKAGE:
				return createPackagePattern(patternString, limitTo, matchMode, isCaseSensitive);
		}
		return null;
	}
	public static SearchPattern createPattern(IJavaElement element, int limitTo) {
		SearchPattern searchPattern = null;
		int lastDot;
		switch (element.getElementType()) {
			case IJavaElement.FIELD :
				IField field = (IField) element; 
				IType declaringClass = field.getDeclaringType();
				char[] declaringSimpleName = declaringClass.getElementName().toCharArray();
				char[] declaringQualification = declaringClass.getPackageFragment().getElementName().toCharArray();
				char[][] enclosingNames = enclosingTypeNames(declaringClass);
				if (enclosingNames.length > 0)
					declaringQualification = CharOperation.concat(declaringQualification, CharOperation.concatWith(enclosingNames, '.'), '.');
				char[] name = field.getElementName().toCharArray();
				char[] typeSimpleName;
				char[] typeQualification;
				try {
					String typeSignature = Signature.toString(field.getTypeSignature()).replace('$', '.');
					if ((lastDot = typeSignature.lastIndexOf('.')) == -1) {
						typeSimpleName = typeSignature.toCharArray();
						typeQualification = null;
					} else {
						typeSimpleName = typeSignature.substring(lastDot + 1).toCharArray();
						typeQualification = field.isBinary()
							? typeSignature.substring(0, lastDot).toCharArray()
							// prefix with a '*' as the full qualification could be bigger (because of an import)
							: CharOperation.concat(ONE_STAR, typeSignature.substring(0, lastDot).toCharArray());
					}
				} catch (JavaModelException e) {
					return null;
				}
				switch (limitTo) {
					case IJavaSearchConstants.DECLARATIONS :
						searchPattern = 
							new FieldPattern(
								true,
								false,
								false,
								name, 
								declaringQualification, 
								declaringSimpleName, 
								typeQualification, 
								typeSimpleName,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.REFERENCES :
						searchPattern = 
							new FieldPattern(
								false,
								true, // read access
								true, // write access
								name, 
								declaringQualification, 
								declaringSimpleName, 
								typeQualification, 
								typeSimpleName,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.READ_ACCESSES :
						searchPattern = 
							new FieldPattern(
								false,
								true, // read access only
								false,
								name, 
								declaringQualification, 
								declaringSimpleName, 
								typeQualification, 
								typeSimpleName,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.WRITE_ACCESSES :
						searchPattern = 
							new FieldPattern(
								false,
								false,
								true, // write access only
								name, 
								declaringQualification, 
								declaringSimpleName, 
								typeQualification, 
								typeSimpleName,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.ALL_OCCURRENCES :
						searchPattern =
							new FieldPattern(
								true,
								true, // read access
								true, // write access
								name, 
								declaringQualification, 
								declaringSimpleName, 
								typeQualification, 
								typeSimpleName,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
				}
				break;
			case IJavaElement.IMPORT_DECLARATION :
				String elementName = element.getElementName();
				lastDot = elementName.lastIndexOf('.');
				if (lastDot == -1) return null; // invalid import declaration
				IImportDeclaration importDecl = (IImportDeclaration)element;
				if (importDecl.isOnDemand()) {
					searchPattern = createPackagePattern(elementName.substring(0, lastDot), limitTo, R_EXACT_MATCH, true);
				} else {
					searchPattern = 
						createTypePattern(
							elementName.substring(lastDot+1).toCharArray(),
							elementName.substring(0, lastDot).toCharArray(),
							null,
							limitTo);
				}
				break;
			case IJavaElement.LOCAL_VARIABLE :
				LocalVariable localVar = (LocalVariable) element;
				switch (limitTo) {
					case IJavaSearchConstants.DECLARATIONS :
						searchPattern = 
							new LocalVariablePattern(
								true, // declarations
								false, // no read access
								false, // no write access
								localVar,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.REFERENCES :
						searchPattern = 
							new LocalVariablePattern(
								false,
								true, // read access
								true, // write access
								localVar,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.READ_ACCESSES :
						searchPattern = 
							new LocalVariablePattern(
								false,
								true, // read access only
								false,
								localVar,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.WRITE_ACCESSES :
						searchPattern = 
							new LocalVariablePattern(
								false,
								false,
								true, // write access only
								localVar,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
					case IJavaSearchConstants.ALL_OCCURRENCES :
						searchPattern =
							new LocalVariablePattern(
								true,
								true, // read access
								true, // write access
								localVar,
								R_EXACT_MATCH | R_CASE_SENSITIVE);
						break;
				}
				break;
			case IJavaElement.METHOD :
				IMethod method = (IMethod) element;
				boolean isConstructor;
				try {
					isConstructor = method.isConstructor();
				} catch (JavaModelException e) {
					return null;
				}
				declaringClass = method.getDeclaringType();
				declaringSimpleName = declaringClass.getElementName().toCharArray();
				declaringQualification = declaringClass.getPackageFragment().getElementName().toCharArray();
				enclosingNames = enclosingTypeNames(declaringClass);
				if (enclosingNames.length > 0)
					declaringQualification = CharOperation.concat(declaringQualification, CharOperation.concatWith(enclosingNames, '.'), '.');
				char[] selector = method.getElementName().toCharArray();
				char[] returnSimpleName;
				char[] returnQualification;
				try {
					String returnType = Signature.toString(method.getReturnType()).replace('$', '.');
					if ((lastDot = returnType.lastIndexOf('.')) == -1) {
						returnSimpleName = returnType.toCharArray();
						returnQualification = null;
					} else {
						returnSimpleName = returnType.substring(lastDot + 1).toCharArray();
						returnQualification = method.isBinary()
							? returnType.substring(0, lastDot).toCharArray()
							// prefix with a '*' as the full qualification could be bigger (because of an import)
							: CharOperation.concat(ONE_STAR, returnType.substring(0, lastDot).toCharArray());
					}
				} catch (JavaModelException e) {
					return null;
				}
				String[] parameterTypes = method.getParameterTypes();
				int paramCount = parameterTypes.length;
				char[][] parameterSimpleNames = new char[paramCount][];
				char[][] parameterQualifications = new char[paramCount][];
				for (int i = 0; i < paramCount; i++) {
					String signature = Signature.toString(parameterTypes[i]).replace('$', '.');
					if ((lastDot = signature.lastIndexOf('.')) == -1) {
						parameterSimpleNames[i] = signature.toCharArray();
						parameterQualifications[i] = null;
					} else {
						parameterSimpleNames[i] = signature.substring(lastDot + 1).toCharArray();
						parameterQualifications[i] = method.isBinary()
							? signature.substring(0, lastDot).toCharArray()
							// prefix with a '*' as the full qualification could be bigger (because of an import)
							: CharOperation.concat(ONE_STAR, signature.substring(0, lastDot).toCharArray());
				}
				}
				switch (limitTo) {
					case IJavaSearchConstants.DECLARATIONS :
						if (isConstructor) {
							searchPattern = 
								new ConstructorPattern(
									true,
									false,
									declaringSimpleName, 
									declaringQualification, 
									parameterQualifications, 
									parameterSimpleNames,
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						} else {
							searchPattern = 
								new MethodPattern(
									true,
									false,
									selector, 
									declaringQualification, 
									declaringSimpleName, 
									returnQualification, 
									returnSimpleName, 
									parameterQualifications, 
									parameterSimpleNames,
									null,
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						}
						break;
					case IJavaSearchConstants.REFERENCES :
						if (isConstructor) {
							searchPattern = 
								new ConstructorPattern(
									false,
									true,
									declaringSimpleName, 
									declaringQualification, 
									parameterQualifications, 
									parameterSimpleNames,
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						} else {
							searchPattern = 
								new MethodPattern(
									false,
									true,
									selector, 
									declaringQualification, 
									declaringSimpleName, 
									returnQualification, 
									returnSimpleName, 
									parameterQualifications, 
									parameterSimpleNames,
									method.getDeclaringType(),
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						}
						break;
					case IJavaSearchConstants.ALL_OCCURRENCES :
						if (isConstructor) {
							searchPattern =
								new ConstructorPattern(
									true,
									true,
									declaringSimpleName, 
									declaringQualification, 
									parameterQualifications, 
									parameterSimpleNames,
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						} else {
							searchPattern =
								new MethodPattern(
									true,
									true,
									selector, 
									declaringQualification, 
									declaringSimpleName, 
									returnQualification, 
									returnSimpleName, 
									parameterQualifications, 
									parameterSimpleNames,
									method.getDeclaringType(),
									R_EXACT_MATCH | R_CASE_SENSITIVE);
						}
						break;
				}
				break;
			case IJavaElement.TYPE :
				IType type = (IType)element;
				searchPattern = 
					createTypePattern(
						type.getElementName().toCharArray(), 
						type.getPackageFragment().getElementName().toCharArray(),
						enclosingTypeNames(type),
						limitTo);
				break;
			case IJavaElement.PACKAGE_DECLARATION :
			case IJavaElement.PACKAGE_FRAGMENT :
				searchPattern = createPackagePattern(element.getElementName(), limitTo, R_EXACT_MATCH, true);
				break;
		}
		if (searchPattern != null)
			searchPattern.focus = element;
		return searchPattern;
	}
	private static SearchPattern createTypePattern(char[] simpleName, char[] packageName, char[][] enclosingTypeNames, int limitTo) {
		switch (limitTo) {
			case IJavaSearchConstants.DECLARATIONS :
				return new TypeDeclarationPattern(
					packageName, 
					enclosingTypeNames, 
					simpleName, 
					TYPE_SUFFIX,
					R_EXACT_MATCH | R_CASE_SENSITIVE);
			case IJavaSearchConstants.REFERENCES :
				return new TypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName,
					R_EXACT_MATCH | R_CASE_SENSITIVE);
			case IJavaSearchConstants.IMPLEMENTORS : 
				return new SuperTypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName,
					true,
					R_EXACT_MATCH | R_CASE_SENSITIVE);
			case IJavaSearchConstants.ALL_OCCURRENCES :
				return new OrPattern(
					new TypeDeclarationPattern(
						packageName, 
						enclosingTypeNames, 
						simpleName, 
						TYPE_SUFFIX,
						R_EXACT_MATCH | R_CASE_SENSITIVE), 
					new TypeReferencePattern(
						CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
						simpleName,
						R_EXACT_MATCH | R_CASE_SENSITIVE));
		}
		return null;
	}
	/**
	 * Type pattern are formed by [qualification.]type
	 * e.g. java.lang.Object
	 *		Runnable
	 *
	 */
	private static SearchPattern createTypePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
		
		int matchRule = isCaseSensitive ? matchMode | R_CASE_SENSITIVE : matchMode;
	
		Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/); 
		scanner.setSource(patternString.toCharArray());
		String type = null;
		int token;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
			return null;
		}
		while (token != TerminalTokens.TokenNameEOF) {
			switch (token) {
				case TerminalTokens.TokenNameWHITESPACE:
					break;
				default: // all other tokens are considered identifiers (see bug 21763 Problem in Java search [search])
					if (type == null)
						type = new String(scanner.getCurrentTokenSource());
					else
						type += new String(scanner.getCurrentTokenSource());
			}
			try {
				token = scanner.getNextToken();
			} catch (InvalidInputException e) {
				return null;
			}
		}
		if (type == null) return null;
	
		char[] qualificationChars = null, typeChars = null;
	
		// extract declaring type infos
		if (type != null) {
			char[] typePart = type.toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
			if (lastDotPosition >= 0) {
				qualificationChars = CharOperation.subarray(typePart, 0, lastDotPosition);
				if (qualificationChars.length == 1 && qualificationChars[0] == '*')
					qualificationChars = null;
				typeChars = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
			} else {
				qualificationChars = null;
				typeChars = typePart;
			}
			if (typeChars.length == 1 && typeChars[0] == '*')
				typeChars = null;
		}
		switch (limitTo) {
			case IJavaSearchConstants.DECLARATIONS : // cannot search for explicit member types
				return new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchRule);
			case IJavaSearchConstants.REFERENCES :
				return new TypeReferencePattern(qualificationChars, typeChars, matchRule);
			case IJavaSearchConstants.IMPLEMENTORS : 
				return new SuperTypeReferencePattern(qualificationChars, typeChars, true, matchRule);
			case IJavaSearchConstants.ALL_OCCURRENCES :
				return new OrPattern(
					new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchRule),// cannot search for explicit member types
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
					new char[][] {declaringClass.getElementName().toCharArray(), ONE_STAR});
			case IJavaElement.TYPE:
				return CharOperation.arrayConcat(
					enclosingTypeNames((IType)parent), 
					parent.getElementName().toCharArray());
			default:
				return null;
		}
	}
	public static char[] encodeIndexKey(char[] key, int matchMode) {
		return key; // null means match all words

//		switch(matchMode) {
//			case SearchPattern.R_EXACT_MATCH :
//			case  SearchPattern.R_PREFIX_MATCH :
//			case  SearchPattern.R_PATTERN_MATCH :
//				return key;
//			case  SearchPattern.R_REGEXP_MATCH:
//				// TODO (jerome) implement
//				return key;
//		}
	}
	/**
	 * Decode the given index key.
	 */
	public void decodeIndexKey(char[] key) {
		// called from findIndexMatches(), override as necessary
	}
	/**
	 * TODO (jerome) spec
	 */
	public SearchPattern getBlankPattern() {
		return null; // called from findIndexMatches(), override as necessary
	}
	/**
	 * Returns a key to find in relevant index categories, if null then all words are matched.
	 * The key will be matched according to some match rule. These potential matches
	 * will be further narrowed by the match locator, but precise match locating can be expensive,
	 * and index query should be as accurate as possible so as to eliminate obvious false hits.
	 */
	public char[] getIndexKey() {
		return null; // called from queryIn(), override as necessary
	}
	/**
	 * Returns an array of index categories to consider for this index query.
	 * These potential matches will be further narrowed by the match locator, but precise
	 * match locating can be expensive, and index query should be as accurate as possible
	 * so as to eliminate obvious false hits.
	 */
	public char[][] getMatchCategories() {
		return CharOperation.NO_CHAR_CHAR; // called from queryIn(), override as necessary
	}
	/**
	 * Returns the rule to apply for matching index keys. Can be exact match, prefix match, pattern match or regexp match.
	 * Rule can also be combined with a case sensitivity flag.
	 */	
	public int getMatchRule() {
		return this.matchMode + (this.isCaseSensitive ? SearchPattern.R_CASE_SENSITIVE : 0);
	}
	/**
	 * TODO (jerome) spec
	 */
	public boolean matchesDecodedKey(SearchPattern decodedPattern) {
		return true; // called from findIndexMatches(), override as necessary if index key is encoded
	}
	/**
	 * Returns whether the given name matches the given pattern.
	 */
	public boolean matchesName(char[] pattern, char[] name) {
		if (pattern == null) return true; // null is as if it was "*"
		if (name != null) {
			switch (this.matchMode) {
				case R_EXACT_MATCH :
					return CharOperation.equals(pattern, name, this.isCaseSensitive);
				case R_PREFIX_MATCH :
					return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
				case R_PATTERN_MATCH :
					if (!this.isCaseSensitive)
						pattern = CharOperation.toLowerCase(pattern);
					return CharOperation.match(pattern, name, this.isCaseSensitive);
				case R_REGEXP_MATCH :
					// TODO (jerome) implement regular expression match
					return true;
			}
		}
		return false;
	}
	public String toString() {
		return "SearchPattern"; //$NON-NLS-1$
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import java.io.IOException;

import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.core.index.IEntryResult;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.index.impl.*;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public abstract class SearchPattern implements ISearchPattern, IIndexConstants, IJavaSearchConstants {

protected int kind;
protected int matchMode;
protected boolean isCaseSensitive;
public boolean mustResolve = true;

/* focus element (used for reference patterns*/
public IJavaElement focus;

/**
 * Constructor pattern are formed by [declaringQualification.]type[(parameterTypes)]
 * e.g. java.lang.Object()
 *		Main(*)
 */
private static SearchPattern createConstructorPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/);
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
				matchMode, 
				isCaseSensitive, 
				declaringQualificationChars, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
		case IJavaSearchConstants.REFERENCES :
			return new ConstructorPattern(
				false,
				true,
				typeNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringQualificationChars, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new ConstructorPattern(
				true,
				true,
				typeNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringQualificationChars, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
	}
	return null;
}
/**
 * Field pattern are formed by [declaringType.]name[type]
 * e.g. java.lang.String.serialVersionUID long
 *		field*
 */
private static SearchPattern createFieldPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/); 
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
				matchMode,
				isCaseSensitive,
				declaringTypeQualification,
				declaringTypeSimpleName,
				typeQualification,
				typeSimpleName);
		case IJavaSearchConstants.REFERENCES :
			return new FieldPattern(
				false,
				true, // read access
				true, // write access
				fieldNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				typeQualification, 
				typeSimpleName);
		case IJavaSearchConstants.READ_ACCESSES :
			return new FieldPattern(
				false,
				true, // read access only
				false,
				fieldNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				typeQualification, 
				typeSimpleName);
		case IJavaSearchConstants.WRITE_ACCESSES :
			return new FieldPattern(
				false,
				false,
				true, // write access only
				fieldNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				typeQualification, 
				typeSimpleName);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new FieldPattern(
				true,
				true, // read access
				true, // write access
				fieldNameChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				typeQualification, 
				typeSimpleName);
	}
	return null;
}
/**
 * Method pattern are formed by [declaringType.]selector[(parameterTypes)][returnType]
 * e.g. java.lang.Runnable.run() void
 *		main(*)
 */
private static SearchPattern createMethodPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/); 
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
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				returnTypeQualification, 
				returnTypeSimpleName, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
		case IJavaSearchConstants.REFERENCES :
			return new MethodPattern(
				false,
				true,
				selectorChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				returnTypeQualification, 
				returnTypeSimpleName, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new MethodPattern(
				true,
				true,
				selectorChars, 
				matchMode, 
				isCaseSensitive, 
				declaringTypeQualification, 
				declaringTypeSimpleName, 
				returnTypeQualification, 
				returnTypeSimpleName, 
				parameterTypeQualifications, 
				parameterTypeSimpleNames,
				null);
	}
	return null;
}
private static SearchPattern createPackagePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			return new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive);
		case IJavaSearchConstants.REFERENCES :
			return new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive),
				new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive)
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
			String fullDeclaringName = field.getDeclaringType().getFullyQualifiedName().replace('$', '.');
			lastDot = fullDeclaringName.lastIndexOf('.');
			char[] declaringSimpleName = (lastDot != -1 ? fullDeclaringName.substring(lastDot + 1) : fullDeclaringName).toCharArray();
			char[] declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : CharOperation.NO_CHAR;
			char[] name = field.getElementName().toCharArray();
			char[] typeSimpleName;
			char[] typeQualification;
			try {
				String typeSignature = Signature.toString(field.getTypeSignature()).replace('$', '.');
				lastDot = typeSignature.lastIndexOf('.');
				typeSimpleName = (lastDot != -1 ? typeSignature.substring(lastDot + 1) : typeSignature).toCharArray();
				typeQualification = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						CharOperation.concat(ONE_STAR, typeSignature.substring(0, lastDot).toCharArray()) : 
						null;
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
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
				case IJavaSearchConstants.REFERENCES :
					searchPattern = 
						new FieldPattern(
							false,
							true, // read access
							true, // write access
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
				case IJavaSearchConstants.READ_ACCESSES :
					searchPattern = 
						new FieldPattern(
							false,
							true, // read access only
							false,
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
				case IJavaSearchConstants.WRITE_ACCESSES :
					searchPattern = 
						new FieldPattern(
							false,
							false,
							true, // write access only
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					searchPattern =
						new FieldPattern(
							true,
							true, // read access
							true, // write access
							name, 
							EXACT_MATCH, 
							CASE_SENSITIVE, 
							declaringQualification, 
							declaringSimpleName, 
							typeQualification, 
							typeSimpleName);
					break;
			}
			break;
		case IJavaElement.IMPORT_DECLARATION :
			String elementName = element.getElementName();
			lastDot = elementName.lastIndexOf('.');
			if (lastDot == -1) return null; // invalid import declaration
			IImportDeclaration importDecl = (IImportDeclaration)element;
			if (importDecl.isOnDemand()) {
				searchPattern = createPackagePattern(elementName.substring(0, lastDot), limitTo, EXACT_MATCH, CASE_SENSITIVE);
			} else {
				searchPattern = 
					createTypePattern(
						elementName.substring(lastDot+1).toCharArray(),
						elementName.substring(0, lastDot).toCharArray(),
						null,
						limitTo);
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
			fullDeclaringName = method.getDeclaringType().getFullyQualifiedName().replace('$', '.');
			lastDot = fullDeclaringName.lastIndexOf('.');
			declaringSimpleName = (lastDot != -1 ? fullDeclaringName.substring(lastDot + 1) : fullDeclaringName).toCharArray();
			declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : CharOperation.NO_CHAR;
			char[] selector = method.getElementName().toCharArray();
			char[] returnSimpleName;
			char[] returnQualification;
			try {
				String returnType = Signature.toString(method.getReturnType()).replace('$', '.');
				lastDot = returnType.lastIndexOf('.');
				returnSimpleName = (lastDot != -1 ? returnType.substring(lastDot + 1) : returnType).toCharArray();
				returnQualification = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						CharOperation.concat(ONE_STAR, returnType.substring(0, lastDot).toCharArray()) : 
						null;
			} catch (JavaModelException e) {
				return null;
			}
			String[] parameterTypes = method.getParameterTypes();
			int paramCount = parameterTypes.length;
			char[][] parameterSimpleNames = new char[paramCount][];
			char[][] parameterQualifications = new char[paramCount][];
			for (int i = 0; i < paramCount; i++) {
				String signature = Signature.toString(parameterTypes[i]).replace('$', '.');
				lastDot = signature.lastIndexOf('.');
				parameterSimpleNames[i] = (lastDot != -1 ? signature.substring(lastDot + 1) : signature).toCharArray();
				parameterQualifications[i] = 
					lastDot != -1 ? 
						// prefix with a '*' as the full qualification could be bigger (because of an import)
						CharOperation.concat(ONE_STAR, signature.substring(0, lastDot).toCharArray()) : 
						null;
			}
			switch (limitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					if (isConstructor) {
						searchPattern = 
							new ConstructorPattern(
								true,
								false,
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames,
								null);
					} else {
						searchPattern = 
							new MethodPattern(
								true,
								false,
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames,
								null);
					}
					break;
				case IJavaSearchConstants.REFERENCES :
					if (isConstructor) {
						searchPattern = 
							new ConstructorPattern(
								false,
								true,
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
					} else {
						searchPattern = 
							new MethodPattern(
								false,
								true,
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
					}
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					if (isConstructor) {
						searchPattern =
							new ConstructorPattern(
								true,
								true,
								declaringSimpleName, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
					} else {
						searchPattern =
							new MethodPattern(
								true,
								true,
								selector, 
								EXACT_MATCH, 
								CASE_SENSITIVE, 
								declaringQualification, 
								declaringSimpleName, 
								returnQualification, 
								returnSimpleName, 
								parameterQualifications, 
								parameterSimpleNames,
								method.getDeclaringType());
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
			searchPattern = createPackagePattern(element.getElementName(), limitTo, EXACT_MATCH, CASE_SENSITIVE);
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
				EXACT_MATCH, 
				CASE_SENSITIVE);
		case IJavaSearchConstants.REFERENCES :
			return new TypeReferencePattern(
				CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
				simpleName, 
				EXACT_MATCH, 
				CASE_SENSITIVE);
		case IJavaSearchConstants.IMPLEMENTORS : 
			return new SuperTypeReferencePattern(
				CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
				simpleName, 
				EXACT_MATCH, 
				CASE_SENSITIVE,
				true);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new TypeDeclarationPattern(
					packageName, 
					enclosingTypeNames, 
					simpleName, 
					TYPE_SUFFIX, 
					EXACT_MATCH, 
					CASE_SENSITIVE), 
				new TypeReferencePattern(
					CharOperation.concatWith(packageName, enclosingTypeNames, '.'), 
					simpleName, 
					EXACT_MATCH, 
					CASE_SENSITIVE));
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

	Scanner scanner = new Scanner(false /*comment*/, true /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3/*sourceLevel*/, null /*taskTags*/, null/*taskPriorities*/); 
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
			return new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive);
		case IJavaSearchConstants.REFERENCES :
			return new TypeReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive);
		case IJavaSearchConstants.IMPLEMENTORS : 
			return new SuperTypeReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive, true);
		case IJavaSearchConstants.ALL_OCCURRENCES :
			return new OrPattern(
				new QualifiedTypeDeclarationPattern(qualificationChars, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive),// cannot search for explicit member types
				new TypeReferencePattern(qualificationChars, typeChars, matchMode, isCaseSensitive));
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
		case IJavaElement.TYPE:
			return CharOperation.arrayConcat(
				enclosingTypeNames((IType)parent), 
				parent.getElementName().toCharArray());
		default:
			return null;
	}
}

public SearchPattern(int kind, int matchMode, boolean isCaseSensitive) {
	this.kind = kind;
	this.matchMode = matchMode;
	this.isCaseSensitive = isCaseSensitive;
}
protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	// default is to do nothing
}
protected void decodeIndexEntry(IEntryResult entryResult) {
	// default is to do nothing
}
/**
 * Feed the requestor according to the current search pattern
 */
public void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope) throws IOException {
	for (int i = 0, max = references.length; i < max; i++) {
		IndexedFile file = input.getIndexedFile(references[i]);
		if (file != null) {
			String path = IndexedFile.convertPath(file.getPath());
			if (scope.encloses(path))
				acceptPath(requestor, path);
		}
	}
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IIndex index, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

	IndexInput input = new BlocksIndexInput(index.getIndexFile());
	try {
		input.open();
		findIndexMatches(input, requestor, detailLevel, progressMonitor,scope);
	} finally {
		input.close();
	}
}
/**
 * Query a given index for matching entries. 
 */
public void findIndexMatches(IndexInput input, IIndexSearchRequestor requestor, int detailLevel, IProgressMonitor progressMonitor, IJavaSearchScope scope) throws IOException {

	if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();
	
	/* narrow down a set of entries using prefix criteria */
	IEntryResult[] entries = input.queryEntriesPrefixedBy(indexEntryPrefix());
	if (entries == null) return;
	
	/* only select entries which actually match the entire search pattern */
	for (int i = 0, max = entries.length; i < max; i++) {
		if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		/* retrieve and decode entry */	
		IEntryResult entry = entries[i];
		decodeIndexEntry(entry);
		if (matchIndexEntry())
			feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
	}
}
/**
 * Answers the suitable prefix that should be used in order
 * to query indexes for the corresponding item.
 * The more accurate the prefix and the less false hits will have
 * to be eliminated later on.
 */
protected char[] indexEntryPrefix() {
	// override with the best prefix possible for the pattern
	return null;
}
protected char[] indexEntryPrefix(char[] tag, char[] name) {
	if (this.isCaseSensitive && name != null) {
		switch(this.matchMode) {
			case EXACT_MATCH :
			case PREFIX_MATCH :
				return CharOperation.concat(tag, name);
			case PATTERN_MATCH :
				int starPos = CharOperation.indexOf('*', name);
				switch(starPos) {
					case -1 :
						return CharOperation.concat(tag, name);
					default : 
						int length = tag.length;
						char[] result = new char[length + starPos];
						System.arraycopy(tag, 0, result, 0, length);
						System.arraycopy(name, 0, result, length, starPos);
						return result;
					case 0 : // fall through
				}
		}
	}
	return tag; // find them all
}
/**
 * Initializes this search pattern so that polymorphic search can be performed.
 */ 
public void initializePolymorphicSearch(MatchLocator locator, IProgressMonitor progressMonitor) {
	// default is to do nothing
}
/*
 * Returns whether this pattern is a polymorphic search pattern.
 */
public boolean isPolymorphicSearch() {
	return false;
}
/**
 * Returns whether the given name matches the given pattern.
 */
protected boolean matchesName(char[] pattern, char[] name) {
	if (pattern == null) return true; // null is as if it was "*"
	if (name != null) {
		switch (this.matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(pattern, name, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
			case PATTERN_MATCH :
				if (!this.isCaseSensitive)
					pattern = CharOperation.toLowerCase(pattern);
				return CharOperation.match(pattern, name, this.isCaseSensitive);
		}
	}
	return false;
}
/**
 * Checks whether an entry matches the current search pattern
 */
protected boolean matchIndexEntry() {
	// override if the pattern can match the index entry
	return false;
}
public String toString(){
	return "SearchPattern"; //$NON-NLS-1$
}
}

package org.eclipse.jdt.internal.core.search.matching;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.core.search.*;

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.util.*;

import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.index.impl.IndexedFile;
import org.eclipse.jdt.internal.core.index.impl.BlocksIndexInput;
import org.eclipse.jdt.internal.core.search.*;
import org.eclipse.jdt.internal.core.search.indexing.*;

import java.io.*;
import java.util.*;

public abstract class SearchPattern implements ISearchPattern, IIndexConstants, IJavaSearchConstants {

	protected int matchMode;
	protected boolean isCaseSensitive;
	protected boolean needsResolve;

	/* match level */
	public static final int IMPOSSIBLE_MATCH = 0;
	public static final int POSSIBLE_MATCH = 1;
	public static final int TRUSTED_MATCH = 2;

	/* match container */
	public static final int COMPILATION_UNIT = 1;
	public static final int CLASS = 2;
	public static final int FIELD = 4;
	public static final int METHOD = 8;
	
	public static final char[][][] NOT_FOUND_DECLARING_TYPE = new char[0][][];

public SearchPattern(int matchMode, boolean isCaseSensitive) {
	this.matchMode = matchMode;
	this.isCaseSensitive = isCaseSensitive;
}
/**
 * Constructor pattern are formed by [declaringQualification.]type[(parameterTypes)]
 * e.g. java.lang.Runnable.run() void
 *		main(*)
 */
private static SearchPattern createConstructorPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	StringTokenizer tokenizer = new StringTokenizer(patternString, " .(,)", true);
	final int InsideName = 1;
	final int InsideParameter = 2;
	String lastToken = null;
	
	String declaringQualification = null, typeName = null, parameterType = null;
	String[] parameterTypes = null;
	int parameterCount = -1;
	String returnType = null;
	boolean foundClosingParenthesis = false;
	int mode = InsideName;
	while (tokenizer.hasMoreTokens()){
		String token = tokenizer.nextToken();
		switch(mode){

			// read declaring type and selector
			case InsideName :
				if (token.equals(".")){
					if (declaringQualification == null){
						if (typeName == null) return null;
						declaringQualification = typeName;
					} else {
						declaringQualification += token + typeName;
					}
					typeName = null;
				} else if (token.equals("(")){
					parameterTypes = new String[5];
					parameterCount = 0;
					mode = InsideParameter;
				} else if (token.equals(" ")){
					if (!(" ".equals(lastToken) || ".".equals(lastToken))){
						break;
					}
				} else { // name
					if (typeName != null) return null;
					typeName = token;
				}
				break;
			// read parameter types
			case InsideParameter :
				if (token.equals(" ")){
				} else if (token.equals(",")){
					if (parameterType == null) return null;
					if (parameterTypes.length == parameterCount){
						System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
					}
					parameterTypes[parameterCount++] = parameterType;
					parameterType = null;
				} else if (token.equals (")")){
					foundClosingParenthesis = true;
					if (parameterType != null){
						if (parameterTypes.length == parameterCount){
							System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
						}
						parameterTypes[parameterCount++] = parameterType;
					}
					break;
				} else {
					if (parameterType == null){
						parameterType = token;
					} else {
						if (!(".".equals(lastToken) || ".".equals(token) || "[]".equals(token))) return null;
						parameterType += token;
					}
				}
				break;
		}
		lastToken = token;
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
	if (parameterCount >= 0){
		parameterTypeQualifications = new char[parameterCount][];
		parameterTypeSimpleNames = new char[parameterCount][];
		for (int i = 0; i < parameterCount; i++){
			char[] parameterTypePart = parameterTypes[i].toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
			if (lastDotPosition >= 0){
				parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
				if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = CharOperation.subarray(parameterTypePart, lastDotPosition+1, parameterTypePart.length);
			} else {
				parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = parameterTypePart;
			}
			if (parameterTypeSimpleNames[i].length == 1 && parameterTypeSimpleNames[i][0] == '*') parameterTypeSimpleNames[i] = null;
		}
	}	
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = new ConstructorDeclarationPattern(typeNameChars, matchMode, isCaseSensitive, declaringQualificationChars, parameterTypeQualifications, parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new ConstructorReferencePattern(typeNameChars, matchMode, isCaseSensitive, declaringQualificationChars, parameterTypeQualifications, parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new ConstructorDeclarationPattern(typeNameChars, matchMode, isCaseSensitive, declaringQualificationChars, parameterTypeQualifications, parameterTypeSimpleNames),
				new ConstructorReferencePattern(typeNameChars, matchMode, isCaseSensitive, declaringQualificationChars, parameterTypeQualifications, parameterTypeSimpleNames));
			break;
	}
	return searchPattern;

}
/**
 * Field pattern are formed by [declaringType.]name[type]
 * e.g. java.lang.Runnable.run() void
 *		main(*)
 */
private static SearchPattern createFieldPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	StringTokenizer tokenizer = new StringTokenizer(patternString, " .(,)", true);
	final int InsideDeclaringPart = 1;
	final int InsideType = 2;
	String lastToken = null;
	
	String declaringType = null, fieldName = null, parameterType = null;
	String type = null;
	boolean foundClosingParenthesis = false;
	int mode = InsideDeclaringPart;
	while (tokenizer.hasMoreTokens()){
		String token = tokenizer.nextToken();
		switch(mode){

			// read declaring type and fieldName
			case InsideDeclaringPart :
				if (token.equals(".")){
					if (declaringType == null){
						if (fieldName == null) return null;
						declaringType = fieldName;
					} else {
						declaringType += token + fieldName;
					}
					fieldName = null;
				} else if (token.equals(" ")){
					if (!(" ".equals(lastToken) || ".".equals(lastToken))){
						mode = InsideType;
					}
				} else { // name
					if (fieldName != null) return null;
					fieldName = token;
				}
				break;
			// read type 
			case InsideType:
				if (!token.equals(" ")){
					if (type == null){
						type = token;
					} else {
						if (!(!(".".equals(lastToken) || ".".equals(token) || "[]".equals(token)))) return null;
						type += token;
					}
				}
		}
		lastToken = token;
	}
	if (fieldName == null) return null;

	char[] fieldNameChars = fieldName.toCharArray();
	if (fieldNameChars.length == 1 && fieldNameChars[0] == '*') fieldNameChars = null;
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] typeQualification = null, typeSimpleName = null;

	// extract declaring type infos
	if (declaringType != null){
		char[] declaringTypePart = declaringType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0){
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*') declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeQualification = null;
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*') declaringTypeSimpleName = null;
	}
	// extract type infos
	if (type != null){
		char[] typePart = type.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
		if (lastDotPosition >= 0){
			typeQualification = CharOperation.subarray(typePart, 0, lastDotPosition);
			if (typeQualification.length == 1 && typeQualification[0] == '*') typeQualification = null;
			typeSimpleName = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
		} else {
			typeQualification = null;
			typeSimpleName = typePart;
		}
		if (typeSimpleName.length == 1 && typeSimpleName[0] == '*') typeSimpleName = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = new FieldDeclarationPattern(fieldNameChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, typeQualification, typeSimpleName);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new FieldReferencePattern(fieldNameChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, typeQualification, typeSimpleName);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new FieldDeclarationPattern(fieldNameChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, typeQualification, typeSimpleName),
				new FieldReferencePattern(fieldNameChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, typeQualification, typeSimpleName));
			break;
	}
	return searchPattern;

}
/**
 * Method pattern are formed by [declaringType.]selector[(parameterTypes)][returnType]
 * e.g. java.lang.Runnable.run() void
 *		main(*)
 */
private static SearchPattern createMethodPattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	StringTokenizer tokenizer = new StringTokenizer(patternString, " .(,)", true);
	final int InsideSelector = 1;
	final int InsideParameter = 2;
	final int InsideReturnType = 3;
	String lastToken = null;
	
	String declaringType = null, selector = null, parameterType = null;
	String[] parameterTypes = null;
	int parameterCount = -1;
	String returnType = null;
	boolean foundClosingParenthesis = false;
	int mode = InsideSelector;
	while (tokenizer.hasMoreTokens()){
		String token = tokenizer.nextToken();
		switch(mode){

			// read declaring type and selector
			case InsideSelector :
				if (token.equals(".")){
					if (declaringType == null){
						if (selector == null) return null;
						declaringType = selector;
					} else {
						declaringType += token + selector;
					}
					selector = null;
				} else if (token.equals("(")){
					parameterTypes = new String[5];
					parameterCount = 0;
					mode = InsideParameter;
				} else if (token.equals(" ")){
					if (!(" ".equals(lastToken) || ".".equals(lastToken))){
						mode = InsideReturnType;
					}
				} else { // name
					if (selector != null) return null;
					selector = token;
				}
				break;
			// read parameter types
			case InsideParameter :
				if (token.equals(" ")){
				} else if (token.equals(",")){
					if (parameterType == null) return null;
					if (parameterTypes.length == parameterCount){
						System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
					}
					parameterTypes[parameterCount++] = parameterType;
					parameterType = null;
				} else if (token.equals (")")){
					foundClosingParenthesis = true;
					if (parameterType != null){
						if (parameterTypes.length == parameterCount){
							System.arraycopy(parameterTypes, 0, parameterTypes = new String[parameterCount*2], 0, parameterCount);
						}
						parameterTypes[parameterCount++] = parameterType;
					}
					mode = InsideReturnType;
				} else {
					if (parameterType == null){
						parameterType = token;
					} else {
						if (!(".".equals(lastToken) || ".".equals(token) || "[]".equals(token))) return null;
						parameterType += token;
					}
				}
				break;
			// read return type
			case InsideReturnType:
				if (!token.equals(" ")){
					if (returnType == null){
						returnType = token;
					} else {
						if (!(!(".".equals(lastToken) || ".".equals(token) || "[]".equals(token)))) return null;
						returnType += token;
					}
				}
		}
		lastToken = token;
	}
	// parenthesis mismatch
	if (parameterCount>0 && !foundClosingParenthesis) return null;
	if (selector == null) return null;

	char[] selectorChars = selector.toCharArray();
	if (selectorChars.length == 1 && selectorChars[0] == '*') selectorChars = null;
		
	char[] declaringTypeQualification = null, declaringTypeSimpleName = null;
	char[] returnTypeQualification = null, returnTypeSimpleName = null;
	char[][] parameterTypeQualifications = null, parameterTypeSimpleNames = null;

	// extract declaring type infos
	if (declaringType != null){
		char[] declaringTypePart = declaringType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', declaringTypePart);
		if (lastDotPosition >= 0){
			declaringTypeQualification = CharOperation.subarray(declaringTypePart, 0, lastDotPosition);
			if (declaringTypeQualification.length == 1 && declaringTypeQualification[0] == '*') declaringTypeQualification = null;
			declaringTypeSimpleName = CharOperation.subarray(declaringTypePart, lastDotPosition+1, declaringTypePart.length);
		} else {
			declaringTypeQualification = null;
			declaringTypeSimpleName = declaringTypePart;
		}
		if (declaringTypeSimpleName.length == 1 && declaringTypeSimpleName[0] == '*') declaringTypeSimpleName = null;
	}
	// extract parameter types infos
	if (parameterCount >= 0){
		parameterTypeQualifications = new char[parameterCount][];
		parameterTypeSimpleNames = new char[parameterCount][];
		for (int i = 0; i < parameterCount; i++){
			char[] parameterTypePart = parameterTypes[i].toCharArray();
			int lastDotPosition = CharOperation.lastIndexOf('.', parameterTypePart);
			if (lastDotPosition >= 0){
				parameterTypeQualifications[i] = CharOperation.subarray(parameterTypePart, 0, lastDotPosition);
				if (parameterTypeQualifications[i].length == 1 && parameterTypeQualifications[i][0] == '*') parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = CharOperation.subarray(parameterTypePart, lastDotPosition+1, parameterTypePart.length);
			} else {
				parameterTypeQualifications[i] = null;
				parameterTypeSimpleNames[i] = parameterTypePart;
			}
			if (parameterTypeSimpleNames[i].length == 1 && parameterTypeSimpleNames[i][0] == '*') parameterTypeSimpleNames[i] = null;
		}
	}	
	// extract return type infos
	if (returnType != null){
		char[] returnTypePart = returnType.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', returnTypePart);
		if (lastDotPosition >= 0){
			returnTypeQualification = CharOperation.subarray(returnTypePart, 0, lastDotPosition);
			if (returnTypeQualification.length == 1 && returnTypeQualification[0] == '*') returnTypeQualification = null;
			returnTypeSimpleName = CharOperation.subarray(returnTypePart, lastDotPosition+1, returnTypePart.length);
		} else {
			returnTypeQualification = null;
			returnTypeSimpleName = returnTypePart;
		}
		if (returnTypeSimpleName.length == 1 && returnTypeSimpleName[0] == '*') returnTypeSimpleName = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = new MethodDeclarationPattern(selectorChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, returnTypeQualification, returnTypeSimpleName, parameterTypeQualifications, parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new MethodReferencePattern(selectorChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, returnTypeQualification, returnTypeSimpleName, parameterTypeQualifications, parameterTypeSimpleNames);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new MethodDeclarationPattern(selectorChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, returnTypeQualification, returnTypeSimpleName, parameterTypeQualifications, parameterTypeSimpleNames),
				new MethodReferencePattern(selectorChars, matchMode, isCaseSensitive, declaringTypeQualification, declaringTypeSimpleName, returnTypeQualification, returnTypeSimpleName, parameterTypeQualifications, parameterTypeSimpleNames));
			break;
	}
	return searchPattern;

}
private static SearchPattern createPackagePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS :
			searchPattern = new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new PackageDeclarationPattern(patternString.toCharArray(), matchMode, isCaseSensitive),
				new PackageReferencePattern(patternString.toCharArray(), matchMode, isCaseSensitive)
			);
			break;
	}
	return searchPattern;

}
public static SearchPattern createPattern(String patternString, int searchFor, int limitTo, int matchMode, boolean isCaseSensitive) {

	if (patternString == null || patternString.length() == 0)
		return null;

	SearchPattern searchPattern = null;
	switch (searchFor) {

		case IJavaSearchConstants.TYPE:
			searchPattern = createTypePattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.METHOD:
			searchPattern = createMethodPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;			
		case IJavaSearchConstants.CONSTRUCTOR:
			searchPattern = createConstructorPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;			
		case IJavaSearchConstants.FIELD:
			searchPattern = createFieldPattern(patternString, limitTo, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.PACKAGE:
			searchPattern = createPackagePattern(patternString, limitTo, matchMode, isCaseSensitive);
	}
	return searchPattern;
}
public static SearchPattern createPattern(IJavaElement element, int limitTo) {
	SearchPattern searchPattern = null;
	int lastDot;
	switch (element.getElementType()) {
		case IJavaElement.FIELD :
			IField field = (IField) element; 
			String fullDeclaringName = field.getDeclaringType().getFullyQualifiedName().replace('$', '.');;
			lastDot = fullDeclaringName.lastIndexOf('.');
			char[] declaringSimpleName = (lastDot != -1 ? fullDeclaringName.substring(lastDot + 1) : fullDeclaringName).toCharArray();
			char[] declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : NO_CHAR;
			char[] name = field.getElementName().toCharArray();
			char[] typeSimpleName;
			char[] typeQualification;
			try {
				String typeSignature = Signature.toString(field.getTypeSignature()).replace('$', '.');
				lastDot = typeSignature.lastIndexOf('.');
				typeSimpleName = (lastDot != -1 ? typeSignature.substring(lastDot + 1) : typeSignature).toCharArray();
				typeQualification = lastDot != -1 ? typeSignature.substring(0, lastDot).toCharArray() : null;
			} catch (JavaModelException e) {
				return null;
			}
			switch (limitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					searchPattern = new FieldDeclarationPattern(name, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName);
					break;
				case IJavaSearchConstants.REFERENCES :
					searchPattern = new FieldReferencePattern(name, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName);
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					searchPattern = new OrPattern(
						new FieldDeclarationPattern(name, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName), 
						new FieldReferencePattern(name, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, typeQualification, typeSimpleName));
					break;
			}
			break;
		case IJavaElement.IMPORT_DECLARATION :
			String elementName = element.getElementName();
			IImportDeclaration importDecl = (IImportDeclaration)element;
			if (importDecl.isOnDemand()) {
				lastDot = elementName.lastIndexOf('.');
				if (lastDot == -1) return null; // invalid import declaration
				searchPattern = createPackagePattern(elementName.substring(0, lastDot), limitTo, EXACT_MATCH, CASE_SENSITIVE);
			} else {
				searchPattern = createTypePattern(elementName, limitTo);
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
			declaringQualification = lastDot != -1 ? fullDeclaringName.substring(0, lastDot).toCharArray() : NO_CHAR;
			char[] selector = method.getElementName().toCharArray();
			char[] returnSimpleName;
			char[] returnQualification;
			try {
				String returnType = Signature.toString(method.getReturnType()).replace('$', '.');
				lastDot = returnType.lastIndexOf('.');
				returnSimpleName = (lastDot != -1 ? returnType.substring(lastDot + 1) : returnType).toCharArray();
				returnQualification = lastDot != -1 ? returnType.substring(0, lastDot).toCharArray() : null;
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
				parameterQualifications[i] = lastDot != -1 ? signature.substring(0, lastDot).toCharArray() : null;
			}
			switch (limitTo) {
				case IJavaSearchConstants.DECLARATIONS :
					if (isConstructor) {
						searchPattern = new ConstructorDeclarationPattern(declaringSimpleName, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, parameterQualifications, parameterSimpleNames);
					} else {
						searchPattern = new MethodDeclarationPattern(selector, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, returnQualification, returnSimpleName, parameterQualifications, parameterSimpleNames);
					}
					break;
				case IJavaSearchConstants.REFERENCES :
					if (isConstructor) {
						searchPattern = new ConstructorReferencePattern(declaringSimpleName, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, parameterQualifications, parameterSimpleNames);
					} else {
						searchPattern = new MethodReferencePattern(selector, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, returnQualification, returnSimpleName, parameterQualifications, parameterSimpleNames);
					}
					break;
				case IJavaSearchConstants.ALL_OCCURRENCES :
					if (isConstructor) {
						searchPattern = new OrPattern(
							new ConstructorDeclarationPattern(declaringSimpleName, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, parameterQualifications, parameterSimpleNames), 
							new ConstructorReferencePattern(declaringSimpleName, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, parameterQualifications, parameterSimpleNames));
					} else {
						searchPattern = new OrPattern(
							new MethodDeclarationPattern(selector, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, returnQualification, returnSimpleName, parameterQualifications, parameterSimpleNames), 
							new MethodReferencePattern(selector, EXACT_MATCH, CASE_SENSITIVE, declaringQualification, declaringSimpleName, returnQualification, returnSimpleName, parameterQualifications, parameterSimpleNames));
					}
					break;
			}
			break;
		case IJavaElement.TYPE :
			IType type = (IType) element;
			searchPattern = createTypePattern(type.getFullyQualifiedName(), limitTo);
			break;
		case IJavaElement.PACKAGE_DECLARATION :
		case IJavaElement.PACKAGE_FRAGMENT :
			searchPattern = createPackagePattern(element.getElementName(), limitTo, EXACT_MATCH, CASE_SENSITIVE);
			break;
	}
	return searchPattern;
}
private static SearchPattern createTypePattern(String fullyQualifiedName, int limitTo) {
	SearchPattern searchPattern = null;
	int lastDot = fullyQualifiedName.lastIndexOf('.');
	int lastDollar = fullyQualifiedName.lastIndexOf('$');
	if (lastDollar < lastDot) lastDollar = -1; // must be in last segment
	char[] enclosingTypeName, simpleName;
	if (lastDollar >= 0){
		enclosingTypeName = fullyQualifiedName.substring(lastDot+1, lastDollar).toCharArray();
		simpleName = fullyQualifiedName.substring(lastDollar+1, fullyQualifiedName.length()).toCharArray();
	} else {
		enclosingTypeName = new char[0];
		simpleName = (lastDot != -1 ? fullyQualifiedName.substring(lastDot + 1) : fullyQualifiedName).toCharArray();
	}
	char[] qualification = lastDot != -1 ? fullyQualifiedName.substring(0, lastDot).toCharArray() : null;
	
	switch (limitTo) {
		case IJavaSearchConstants.DECLARATIONS :
			char[][] enclosingTypeNames = CharOperation.splitOn('$', enclosingTypeName);
			searchPattern = new TypeDeclarationPattern(qualification, enclosingTypeNames, simpleName, TYPE_SUFFIX, EXACT_MATCH, CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.REFERENCES :
			if (enclosingTypeName.length > 0) {
				qualification = CharOperation.concat(qualification, enclosingTypeName, '.');
			}
			searchPattern = new TypeReferencePattern(qualification, simpleName, EXACT_MATCH, CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.IMPLEMENTORS : 
			searchPattern = new SuperInterfaceReferencePattern(qualification, simpleName, EXACT_MATCH, CASE_SENSITIVE);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			enclosingTypeNames = CharOperation.splitOn('$', enclosingTypeName);
			searchPattern = new OrPattern(
				new TypeDeclarationPattern(qualification, enclosingTypeNames, simpleName, TYPE_SUFFIX, EXACT_MATCH, CASE_SENSITIVE), 
				new TypeReferencePattern(qualification, simpleName, EXACT_MATCH, CASE_SENSITIVE));
			break;
	}
	return searchPattern;
}
/**
 * Type pattern are formed by [package.]type
 * e.g. java.lang.Object
 *		Runnable
 *
 */
private static SearchPattern createTypePattern(String patternString, int limitTo, int matchMode, boolean isCaseSensitive) {

	StringTokenizer tokenizer = new StringTokenizer(patternString, " .", true);
	String type = null;
	String lastToken = null;
	while (tokenizer.hasMoreTokens()){
		String token = tokenizer.nextToken();
		if (!token.equals(" ")){
			if (type == null){
				type = token;
			} else {
				if (!(".".equals(lastToken) || ".".equals(token) || "[]".equals(token))) return null;
				type += token;
			}
		}
		lastToken = token;
	}
	if (type == null) return null;

	char[] packageChars = null, typeChars = null;

	// extract declaring type infos
	if (type != null){
		char[] typePart = type.toCharArray();
		int lastDotPosition = CharOperation.lastIndexOf('.', typePart);
		if (lastDotPosition >= 0){
			packageChars = CharOperation.subarray(typePart, 0, lastDotPosition);
			if (packageChars.length == 1 && packageChars[0] == '*') packageChars = null;
			typeChars = CharOperation.subarray(typePart, lastDotPosition+1, typePart.length);
		} else {
			packageChars = null;
			typeChars = typePart;
		}
		if (typeChars.length == 1 && typeChars[0] == '*') typeChars = null;
	}
	SearchPattern searchPattern = null;
	switch (limitTo){
		case IJavaSearchConstants.DECLARATIONS : // cannot search for explicit member types
			searchPattern = new TypeDeclarationPattern(packageChars, null, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.REFERENCES :
			searchPattern = new TypeReferencePattern(packageChars, typeChars, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.IMPLEMENTORS : 
			searchPattern = new SuperInterfaceReferencePattern(packageChars, typeChars, matchMode, isCaseSensitive);
			break;
		case IJavaSearchConstants.ALL_OCCURRENCES :
			searchPattern = new OrPattern(
				new TypeDeclarationPattern(packageChars, null, typeChars, TYPE_SUFFIX, matchMode, isCaseSensitive),// cannot search for explicit member types
				new TypeReferencePattern(packageChars, typeChars, matchMode, isCaseSensitive));
			break;
	}
	return searchPattern;

}
protected abstract void decodeIndexEntry(IEntryResult entryResult);
/**
 * Feed the requestor according to the current search pattern
 */
public abstract void feedIndexRequestor(IIndexSearchRequestor requestor, int detailLevel, int[] references, IndexInput input, IJavaSearchScope scope)  throws IOException ;
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
	for (int i = 0, max = entries.length; i < max; i++){

		if (progressMonitor != null && progressMonitor.isCanceled()) throw new OperationCanceledException();

		/* retrieve and decode entry */	
		IEntryResult entry = entries[i];
		decodeIndexEntry(entry);
		if (matchIndexEntry()){
			feedIndexRequestor(requestor, detailLevel, entry.getFileReferences(), input, scope);
		}
	}
}
/**
 * Answers the suitable prefix that should be used in order
 * to query indexes for the corresponding item.
 * The more accurate the prefix and the less false hits will have
 * to be eliminated later on.
 */
public abstract char[] indexEntryPrefix();
/**
 * Returns the type of container of this pattern, i.e. is it in compilation unit,
 * in class declarations, field declarations, or in method declarations.
 */
protected abstract int matchContainer();
/**
 * Finds out whether the given resolved ast node matches this search pattern.
 */
public boolean matches(AstNode node) {
	return this.matches(node, true);
}
/**
 * Returns whether this pattern matches the given node.
 * Look at resolved information only if specified.
 */
protected abstract boolean matches(AstNode node, boolean resolve);
/**
 * Finds out whether the given binding matches this search pattern.
 * Default is to return false.
 */
public boolean matches(Binding binding) {
	return false;
}
/**
 * Finds out whether the given binary info matches this search pattern.
 * Default is to return false.
 */
public boolean matchesBinary(Object binaryInfo, Object enclosingBinaryInfo) {
	return false;
}
/**
 * Returns whether the given name matches the given pattern.
 */
protected boolean matchesName(char[] pattern, char[] name) {
	if (name != null){
		switch (this.matchMode) {
			case EXACT_MATCH :
				return CharOperation.equals(pattern, name, this.isCaseSensitive);
			case PREFIX_MATCH :
				return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
			case PATTERN_MATCH :
				return CharOperation.match(pattern, name, this.isCaseSensitive);
		}
	}
	return false;
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * and qualification pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] qualificationPattern, char[] fullyQualifiedTypeName) {
	char[] pattern;
	if (simpleNamePattern == null) {
		if (qualificationPattern == null) {
			pattern = ONE_STAR;
		} else {
			pattern = CharOperation.concat(qualificationPattern, ONE_STAR, '.');
		}
	} else {
		if (qualificationPattern == null) {
			pattern = CharOperation.concat(ONE_STAR, simpleNamePattern);
		} else {
			pattern = CharOperation.concat(qualificationPattern, simpleNamePattern, '.');
		}
	}
	return 
		CharOperation.match(
			pattern,
			fullyQualifiedTypeName,
			this.isCaseSensitive
		);
}
/**
 * Returns whether the given type binding matches the given simple name pattern 
 * and qualification pattern.
 */
protected boolean matchesType(char[] simpleNamePattern, char[] qualificationPattern, TypeBinding type) {
	if (type == null) return false; 
	return 
		this.matchesType(
			simpleNamePattern, 
			qualificationPattern, 
			type.qualifiedPackageName().length == 0 ? 
				type.qualifiedSourceName() : 
				CharOperation.concat(type.qualifiedPackageName(), type.qualifiedSourceName(), '.')
		);
}
/**
 * Checks whether an entry matches the current search pattern
 */
protected abstract boolean matchIndexEntry();
/**
 * Finds out whether the given ast node matches this search pattern.
 * Returns IMPOSSIBLE_MATCH if it doesn't.
 * Returns TRUSTED_MATCH if it matches exactly this search pattern (ie. 
 * it doesn't need to be resolved or it has already been resolved.)
 * Returns POSSIBLE_MATCH if it potentially matches 
 * this search pattern and it needs to be resolved to get more information.
 */
public int matchLevel(AstNode node) {
	if (this.matches(node, false)) {
		if (this.needsResolve) {
			return POSSIBLE_MATCH;
		} else {
			return TRUSTED_MATCH;
		}
	}
	return IMPOSSIBLE_MATCH;
}
/**
 * Reports the match of the given reference.
 */
protected void matchReportReference(AstNode reference, IJavaElement element, int accuracy, MatchLocator locator) throws CoreException {
	// default is to report a match on the whole node.
	locator.report(reference.sourceStart, reference.sourceEnd, element, accuracy);
}
/**
 * Add square brackets to the given simple name
 */
protected char[] toArrayName(char[] simpleName, int dimensions) {
	if (dimensions == 0) return simpleName;
	char[] result = new char[simpleName.length + dimensions * 2];
	for (int i = 0; i < dimensions; i++) {
		result[simpleName.length + i*2] = '[';
		result[simpleName.length + i*2 + 1] = ']';
	}
	return result;
}
public String toString(){
	return "SearchPattern";
}

/**
 * Collects the super type names of the given declaring type.
 * Returns NOT_FOUND_DECLARING_TYPE if the declaring type was not found.
 * Returns null if the declaring type pattern doesn't require an exact match.
 */
protected char[][][] collectSuperTypeNames(char[] declaringQualification, char[] declaringSimpleName, int matchMode, LookupEnvironment env) {

	char[][] declaringTypeName = null;
	if (declaringQualification != null 
			&& declaringSimpleName != null
			&& matchMode == EXACT_MATCH) {
		char[][] qualification = CharOperation.splitOn('.', declaringQualification);
		declaringTypeName = CharOperation.arrayConcat(qualification, declaringSimpleName);
	}
	if (declaringTypeName != null) {
		for (int i = 0, max = declaringTypeName.length; i < max; i++) {
			ReferenceBinding matchingDeclaringType = env.askForType(declaringTypeName);
			if (matchingDeclaringType != null 
				&& (matchingDeclaringType.isValidBinding()
					|| matchingDeclaringType.problemId() != ProblemReasons.NotFound)) {
				return this.collectSuperTypeNames(matchingDeclaringType);
			}
			// if nothing is in the cache, it could have been a member type (A.B.C.D --> A.B.C$D)
			int last = declaringTypeName.length - 1;
			if (last == 0) break; 
			declaringTypeName[last-1] = CharOperation.concat(declaringTypeName[last-1], declaringTypeName[last], '$'); // try nested type
			declaringTypeName = CharOperation.subarray(declaringTypeName, 0, last);
		}
		return NOT_FOUND_DECLARING_TYPE; // the declaring type was not found 
	} else {
		// non exact match: use the null value so that matches is more tolerant
		return null;
	}
}

/**
 * Collects the names of all the supertypes of the given type.
 */
private char[][][] collectSuperTypeNames(ReferenceBinding type) {

	// superclass
	char[][][] superClassNames = null;
	ReferenceBinding superclass = type.superclass();
	if (superclass != null) {
		superClassNames = this.collectSuperTypeNames(superclass);
	}

	// interfaces
	char[][][][] superInterfaceNames = null;
	int superInterfaceNamesLength = 0;
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces != null) {
		superInterfaceNames = new char[interfaces.length][][][];
		for (int i = 0; i < interfaces.length; i++) {
			superInterfaceNames[i] = this.collectSuperTypeNames(interfaces[i]);
			superInterfaceNamesLength += superInterfaceNames[i].length;
		}
	}

	int length = 
		(superclass == null ? 0 : 1)
		+ (superClassNames == null ? 0 : superClassNames.length)
		+ (interfaces == null ? 0 : interfaces.length)
		+ superInterfaceNamesLength;
	char[][][] result = new char[length][][];
	int index = 0;
	if (superclass != null) {
		result[index++] = superclass.compoundName;
		if (superClassNames != null) {
			System.arraycopy(superClassNames, 0, result, index, superClassNames.length);
			index += superClassNames.length;
		}
	}
	if (interfaces != null) {
		for (int i = 0, max = interfaces.length; i < max; i++) {
			result[index++] = interfaces[i].compoundName;
			if (superInterfaceNames != null) {
				System.arraycopy(superInterfaceNames[i], 0, result, index, superInterfaceNames[i].length);
				index += superInterfaceNames[i].length;
			}
		}
	}
	
	return result;
}

/**
 * Initializes this search pattern from the given lookup environment.
 * Returns whether it could be initialized.
 */ 
public boolean initializeFromLookupEnvironment(LookupEnvironment env) {
	return true;
}

/**
 * Returns whether the given reference type binding matches or is a subtype of a type
 * that matches the given simple name pattern and qualification pattern.
 */
protected boolean matchesAsSubtype(ReferenceBinding type, char[] simpleNamePattern, char[] qualificationPattern) {
	// matches type
	if (this.matchesType(simpleNamePattern, qualificationPattern, type))
		return true;
	
	// matches superclass
	ReferenceBinding superclass = type.superclass();
	if (superclass != null) {
		if (this.matchesAsSubtype(superclass, simpleNamePattern, qualificationPattern))
			return true;
	}

	// matches interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	for (int i = 0; i < interfaces.length; i++) {
		if (this.matchesAsSubtype(interfaces[i], simpleNamePattern, qualificationPattern))
			return true;
	}

	return false;
}

/**
 * Returns whether one of the given declaring types is the given receiver type.
 */
protected boolean matchesType(char[][][] declaringTypes, ReferenceBinding receiverType) {
	if (declaringTypes == null) {
		return true; // we were not able to compute the declaring types, default to true
	} else {
		for (int i = 0, max = declaringTypes.length; i < max; i++) {
			if (CharOperation.equals(declaringTypes[i], receiverType.compoundName)) {
				return true;
			}
		}
		return false;
	}
}
}

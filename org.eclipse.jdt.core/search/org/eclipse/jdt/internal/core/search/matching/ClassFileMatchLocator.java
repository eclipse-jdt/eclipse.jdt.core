/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class ClassFileMatchLocator implements IIndexConstants {

public static char[] convertClassFileFormat(char[] name) {
	return CharOperation.replaceOnCopy(name, '/', '.');
}

private boolean checkDeclaringType(IBinaryType enclosingBinaryType, char[] simpleName, char[] qualification, boolean isCaseSensitive, boolean isCamelCase) {
	if (simpleName == null && qualification == null) return true;
	if (enclosingBinaryType == null) return true;

	char[] declaringTypeName = convertClassFileFormat(enclosingBinaryType.getName());
	return checkTypeName(simpleName, qualification, declaringTypeName, isCaseSensitive, isCamelCase);
}
private boolean checkParameters(char[] methodDescriptor, char[][] parameterSimpleNames, char[][] parameterQualifications, boolean isCaseSensitive, boolean isCamelCase) {
	char[][] arguments = Signature.getParameterTypes(methodDescriptor);
	int parameterCount = parameterSimpleNames.length;
	if (parameterCount != arguments.length) return false;
	for (int i = 0; i < parameterCount; i++)
		if (!checkTypeName(parameterSimpleNames[i], parameterQualifications[i], Signature.toCharArray(arguments[i]), isCaseSensitive, isCamelCase))
			return false;
	return true;
}
private boolean checkTypeName(char[] simpleName, char[] qualification, char[] fullyQualifiedTypeName, boolean isCaseSensitive, boolean isCamelCase) {
	// NOTE: if case insensitive then simpleName & qualification are assumed to be lowercase
	char[] wildcardPattern = PatternLocator.qualifiedPattern(simpleName, qualification);
	if (wildcardPattern == null) return true;
	return CharOperation.match(wildcardPattern, fullyQualifiedTypeName, isCaseSensitive);
}
/**
 * Locate declaration in the current class file. This class file is always in a jar.
 */
public void locateMatches(MatchLocator locator, ClassFile classFile, IBinaryType info) throws CoreException {
	// check class definition
	SearchPattern pattern = locator.pattern;
	BinaryType binaryType = (BinaryType) classFile.getType();
	if (matchBinary(pattern, info, null)) {
		binaryType = new ResolvedBinaryType((JavaElement) binaryType.getParent(), binaryType.getElementName(), binaryType.getKey());
		locator.reportBinaryMemberDeclaration(null, binaryType, null, info, SearchMatch.A_ACCURATE);
	}

	// Get methods from binary type info
	IBinaryMethod[] binaryMethods = info.getMethods();
	int bMethodsLength = binaryMethods == null ? 0 : binaryMethods.length;
	IBinaryMethod[] inaccurateMethods = new IBinaryMethod[bMethodsLength];
	char[][] binaryMethodSignatures = null;
	if (bMethodsLength > 0) {
		System.arraycopy(binaryMethods, 0, inaccurateMethods, 0, bMethodsLength);
	}

	// Get fields from binary type info
	IBinaryField[] binaryFields = info.getFields();
	int bFieldsLength = binaryFields == null ? 0 : binaryFields.length;
	IBinaryField[] inaccurateFields = new IBinaryField[bFieldsLength];
	if (bFieldsLength > 0) {
		System.arraycopy(binaryFields, 0, inaccurateFields, 0, bFieldsLength);
	}
	
	// Report as many accurate matches as possible
	if (((InternalSearchPattern)pattern).mustResolve) {
		BinaryTypeBinding binding = locator.cacheBinaryType(binaryType, info);
		if (binding != null) {
			// filter out element not in hierarchy scope
			if (!locator.typeInHierarchy(binding)) return;

			// Report accurate methods
			MethodBinding[] availableMethods = binding.availableMethods();
			int aMethodsLength = availableMethods == null ? 0 : availableMethods.length;
			for (int i = 0; i < aMethodsLength; i++) {
				MethodBinding method = availableMethods[i];
				int level = locator.patternLocator.resolveLevel(method);
				char[] methodSignature = method.genericSignature();
				if (methodSignature == null) methodSignature = method.signature();
				switch (level) {
					case PatternLocator.ACCURATE_MATCH:
						IMethod methodHandle = binaryType.getMethod(
							new String(method.isConstructor() ? binding.compoundName[binding.compoundName.length-1] : method.selector),
							CharOperation.toStrings(Signature.getParameterTypes(convertClassFileFormat(methodSignature))));
						locator.reportBinaryMemberDeclaration(null, methodHandle, method, info, SearchMatch.A_ACCURATE);
						// fall through impossible match case to remove the reported method
					case PatternLocator.IMPOSSIBLE_MATCH:
						// Store binary method signatures to avoid multiple computation
						if (binaryMethodSignatures == null) {
							binaryMethodSignatures = new char[bMethodsLength][];
							for (int j=0; j<bMethodsLength; j++) {
								IBinaryMethod binaryMethod = binaryMethods[j];
								char[] signature = binaryMethod.getGenericSignature();
								if (signature == null) signature = binaryMethod.getMethodDescriptor();
								binaryMethodSignatures[j] = signature;
							}
						}
						// The method is either accurate or impossible so remove from inaccurate methods list
						for (int j=0; j<bMethodsLength; j++) {
							if (CharOperation.equals(binaryMethods[j].getSelector(), method.selector) && CharOperation.equals(binaryMethodSignatures[j], methodSignature)) {
								inaccurateMethods[j] = null;
								break;
							}
						}
						break;
				}
			}

			// Report accurate fields
			FieldBinding[] availableFields = binding.availableFields();
			int aFieldsLength = availableFields == null ? 0 : availableFields.length;
			for (int i = 0; i < aFieldsLength; i++) {
				FieldBinding field = availableFields[i];
				int level = locator.patternLocator.resolveLevel(field);
				switch (level) {
					case PatternLocator.ACCURATE_MATCH:
						IField fieldHandle = binaryType.getField(new String(field.name));
						locator.reportBinaryMemberDeclaration(null, fieldHandle, field, info, SearchMatch.A_ACCURATE);
						// fall through impossible match case to remove reported field
					case PatternLocator.IMPOSSIBLE_MATCH:
						// The field is either an accurate or impossible match, so remove it from inaccurate fields list
						for (int j=0; j<bFieldsLength; j++) {
							if ( CharOperation.equals(binaryFields[j].getName(), field.name)) {
								inaccurateFields[j] = null;
								break;
							}
						}
						break;
				}
			}

			// If all methods/fields were accurate then returns now
			if (bMethodsLength == aMethodsLength && bFieldsLength == aFieldsLength) {
				return;
			}
		}
	}

	// Report inaccurate methods
	for (int i=0; i < bMethodsLength; i++) {
		IBinaryMethod method = inaccurateMethods[i];
		if (method == null) continue; // impossible match or already reported as accurate
		if (matchBinary(pattern, method, info)) {
			char[] name;
			if (method.isConstructor()) {
				name = info.getName();
				int lastSlash = CharOperation.lastIndexOf('/', name);
				if (lastSlash != -1) {
					name = CharOperation.subarray(name, lastSlash+1, name.length);
				}
			} else {
				name = method.getSelector();
			}
			String selector = new String(name);
			char[] methodSignature = binaryMethodSignatures == null ? null : binaryMethodSignatures[i];
			if (methodSignature == null) {
				methodSignature = method.getGenericSignature();
				if (methodSignature == null) methodSignature = method.getMethodDescriptor();
			}
			String[] parameterTypes = CharOperation.toStrings(Signature.getParameterTypes(convertClassFileFormat(methodSignature)));
			IMethod methodHandle = binaryType.getMethod(selector, parameterTypes);
			methodHandle = new ResolvedBinaryMethod(binaryType, selector, parameterTypes, methodHandle.getKey());
			locator.reportBinaryMemberDeclaration(null, methodHandle, null, info, SearchMatch.A_INACCURATE);
		}
	}

	// Report inaccurate fields
	for (int i=0; i<bFieldsLength; i++) {
		IBinaryField field = inaccurateFields[i];
		if (field == null) continue; // impossible match or already reported as accurate
		if (matchBinary(pattern, field, info)) {
			String fieldName = new String(field.getName());
			IField fieldHandle = binaryType.getField(fieldName);
			fieldHandle = new ResolvedBinaryField(binaryType, fieldName, fieldHandle.getKey());
			locator.reportBinaryMemberDeclaration(null, fieldHandle, null, info, SearchMatch.A_INACCURATE);
		}
	}
}
/**
 * Finds out whether the given binary info matches the search pattern.
 * Default is to return false.
 */
boolean matchBinary(SearchPattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	switch (((InternalSearchPattern)pattern).kind) {
		case CONSTRUCTOR_PATTERN :
			return matchConstructor((ConstructorPattern) pattern, binaryInfo, enclosingBinaryType);
		case FIELD_PATTERN :
			return matchField((FieldPattern) pattern, binaryInfo, enclosingBinaryType);
		case METHOD_PATTERN :
			return matchMethod((MethodPattern) pattern, binaryInfo, enclosingBinaryType);
		case SUPER_REF_PATTERN :
			return matchSuperTypeReference((SuperTypeReferencePattern) pattern, binaryInfo, enclosingBinaryType);
		case TYPE_DECL_PATTERN :
			return matchTypeDeclaration((TypeDeclarationPattern) pattern, binaryInfo, enclosingBinaryType);
		case OR_PATTERN :
			SearchPattern[] patterns = ((OrPattern) pattern).patterns;
			for (int i = 0, length = patterns.length; i < length; i++)
				if (matchBinary(patterns[i], binaryInfo, enclosingBinaryType)) return true;
	}
	return false;
}
boolean matchConstructor(ConstructorPattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	if (!pattern.findDeclarations) return false; // only relevant when finding declarations
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod) binaryInfo;
	if (!method.isConstructor()) return false;
	if (!checkDeclaringType(enclosingBinaryType, pattern.declaringSimpleName, pattern.declaringQualification, pattern.isCaseSensitive(), pattern.isCamelCase()))
		return false;
	if (pattern.parameterSimpleNames != null) {
		char[] methodDescriptor = convertClassFileFormat(method.getMethodDescriptor());
		if (!checkParameters(methodDescriptor, pattern.parameterSimpleNames, pattern.parameterQualifications, pattern.isCaseSensitive(), pattern.isCamelCase()))
			return false;
	}
	return true;
}
boolean matchField(FieldPattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	if (!pattern.findDeclarations) return false; // only relevant when finding declarations
	if (!(binaryInfo instanceof IBinaryField)) return false;

	IBinaryField field = (IBinaryField) binaryInfo;
	if (!pattern.matchesName(pattern.name, field.getName())) return false;
	if (!checkDeclaringType(enclosingBinaryType, pattern.declaringSimpleName, pattern.declaringQualification, pattern.isCaseSensitive(), pattern.isCamelCase()))
		return false;

	char[] fieldTypeSignature = Signature.toCharArray(convertClassFileFormat(field.getTypeName()));
	return checkTypeName(pattern.typeSimpleName, pattern.typeQualification, fieldTypeSignature, pattern.isCaseSensitive(), pattern.isCamelCase());
}
boolean matchMethod(MethodPattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	if (!pattern.findDeclarations) return false; // only relevant when finding declarations
	if (!(binaryInfo instanceof IBinaryMethod)) return false;

	IBinaryMethod method = (IBinaryMethod) binaryInfo;
	if (!pattern.matchesName(pattern.selector, method.getSelector())) return false;
	if (!checkDeclaringType(enclosingBinaryType, pattern.declaringSimpleName, pattern.declaringQualification, pattern.isCaseSensitive(), pattern.isCamelCase()))
		return false;

	// look at return type only if declaring type is not specified
	boolean checkReturnType = pattern.declaringSimpleName == null && (pattern.returnSimpleName != null || pattern.returnQualification != null);
	boolean checkParameters = pattern.parameterSimpleNames != null;
	if (checkReturnType || checkParameters) {
		char[] methodDescriptor = convertClassFileFormat(method.getMethodDescriptor());
		if (checkReturnType) {
			char[] returnTypeSignature = Signature.toCharArray(Signature.getReturnType(methodDescriptor));
			if (!checkTypeName(pattern.returnSimpleName, pattern.returnQualification, returnTypeSignature, pattern.isCaseSensitive(), pattern.isCamelCase()))
				return false;
		}
		if (checkParameters &&  !checkParameters(methodDescriptor, pattern.parameterSimpleNames, pattern.parameterQualifications, pattern.isCaseSensitive(), pattern.isCamelCase()))
			return false;
	}
	return true;
}
boolean matchSuperTypeReference(SuperTypeReferencePattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	if (!(binaryInfo instanceof IBinaryType)) return false;

	IBinaryType type = (IBinaryType) binaryInfo;
	if (pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_INTERFACES) {
		char[] vmName = type.getSuperclassName();
		if (vmName != null) {
			char[] superclassName = convertClassFileFormat(vmName);
			if (checkTypeName(pattern.superSimpleName, pattern.superQualification, superclassName, pattern.isCaseSensitive(), pattern.isCamelCase()))
				return true;
		}
	}

	if (pattern.superRefKind != SuperTypeReferencePattern.ONLY_SUPER_CLASSES) {
		char[][] superInterfaces = type.getInterfaceNames();
		if (superInterfaces != null) {
			for (int i = 0, max = superInterfaces.length; i < max; i++) {
				char[] superInterfaceName = convertClassFileFormat(superInterfaces[i]);
				if (checkTypeName(pattern.superSimpleName, pattern.superQualification, superInterfaceName, pattern.isCaseSensitive(), pattern.isCamelCase()))
					return true;
			}
		}
	}
	return false;
}
boolean matchTypeDeclaration(TypeDeclarationPattern pattern, Object binaryInfo, IBinaryType enclosingBinaryType) {
	if (!(binaryInfo instanceof IBinaryType)) return false;

	IBinaryType type = (IBinaryType) binaryInfo;
	char[] fullyQualifiedTypeName = convertClassFileFormat(type.getName());
	boolean qualifiedPattern = pattern instanceof QualifiedTypeDeclarationPattern;
	if (pattern.enclosingTypeNames == null || qualifiedPattern) {
		char[] simpleName = (pattern.getMatchMode() == SearchPattern.R_PREFIX_MATCH)
			? CharOperation.concat(pattern.simpleName, IIndexConstants.ONE_STAR)
			: pattern.simpleName;
		char[] pkg = qualifiedPattern ? ((QualifiedTypeDeclarationPattern)pattern).qualification : pattern.pkg;
		if (!checkTypeName(simpleName, pkg, fullyQualifiedTypeName, pattern.isCaseSensitive(), pattern.isCamelCase())) return false;
	} else {
		char[] enclosingTypeName = CharOperation.concatWith(pattern.enclosingTypeNames, '.');
		char[] patternString = pattern.pkg == null
			? enclosingTypeName
			: CharOperation.concat(pattern.pkg, enclosingTypeName, '.');
		if (!checkTypeName(pattern.simpleName, patternString, fullyQualifiedTypeName, pattern.isCaseSensitive(), pattern.isCamelCase())) return false;
	}

	int kind  = TypeDeclaration.kind(type.getModifiers());
	switch (pattern.typeSuffix) {
		case CLASS_SUFFIX:
			return kind == TypeDeclaration.CLASS_DECL;
		case INTERFACE_SUFFIX:
			return kind == TypeDeclaration.INTERFACE_DECL;
		case ENUM_SUFFIX:
			return kind == TypeDeclaration.ENUM_DECL;
		case ANNOTATION_TYPE_SUFFIX:
			return kind == TypeDeclaration.ANNOTATION_TYPE_DECL;
		case CLASS_AND_INTERFACE_SUFFIX:
			return kind == TypeDeclaration.CLASS_DECL || kind == TypeDeclaration.INTERFACE_DECL;
		case CLASS_AND_ENUM_SUFFIX:
			return kind == TypeDeclaration.CLASS_DECL || kind == TypeDeclaration.ENUM_DECL;
		case INTERFACE_AND_ANNOTATION_SUFFIX:
			return kind == TypeDeclaration.INTERFACE_DECL || kind == TypeDeclaration.ANNOTATION_TYPE_DECL;
		case TYPE_SUFFIX: // nothing
	}
	return true;
}
}

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
package org.eclipse.jdt.internal.core.util;

/**
 * Converter from element info to parsed compilation unit.
 *
 */

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;
import org.eclipse.jdt.internal.core.SourceMethodElementInfo;

public class ElementInfoConverter implements CompilerModifiers {

	/*
	 * Convert a set of source type infos into a parsed compilation unit declaration
	 * The argument types are then all grouped in the same unit. The argument types must 
	 * at least contain one type.
	 * Can optionally add local and anonymous types
	 */
	public static CompilationUnitDeclaration buildCompilationUnit(
		SourceTypeElementInfo[] sourceTypes,
		boolean needLocalTypes,
		ProblemReporter problemReporter,
		CompilationResult compilationResult) {

		return 
			new ElementInfoConverter(needLocalTypes, problemReporter).convert(
				sourceTypes, 
				compilationResult);
	}
	
	private boolean needLocalTypes; // local and anoymous types
	private ProblemReporter problemReporter;
	private CompilationUnitDeclaration unit;
	
	private ElementInfoConverter(boolean needLocalTypes, ProblemReporter problemReporter) {
		this.needLocalTypes = needLocalTypes;
		this.problemReporter = problemReporter;
	}
	
	/*
	 * Convert an initializerinfo into a parsed initializer declaration
	 */
	private Initializer convert(InitializerElementInfo initializerInfo, CompilationResult compilationResult) {

		Block block = new Block(0);
		Initializer initializer = new Initializer(block, IConstants.AccDefault);

		int start = initializerInfo.getDeclarationSourceStart();
		int end = initializerInfo.getDeclarationSourceEnd();

		initializer.name = initializerInfo.getName();
		initializer.sourceStart = initializer.declarationSourceStart = start;
		initializer.sourceEnd = initializer.declarationSourceEnd = end;
		initializer.modifiers = initializerInfo.getModifiers();

		/* convert local and anonymous types */
		IJavaElement[] children = initializerInfo.getChildren();
		int typesLength = children.length;
		if (typesLength > 0) {
			Statement[] statements = new Statement[typesLength];
			for (int i = 0; i < typesLength; i++) {
				JavaElement type = (JavaElement)children[i];
				try {
					TypeDeclaration localType = convert((SourceTypeElementInfo)type.getElementInfo(), compilationResult);
					if ((localType.bits & ASTNode.IsAnonymousTypeMASK) != 0) {
						QualifiedAllocationExpression expression = new QualifiedAllocationExpression(localType);
						expression.type = localType.superclass;
						localType.superclass = null;
						localType.superInterfaces = null;
						localType.allocation = expression;
						statements[i] = expression;
					} else {
						statements[i] = localType;
					}
				} catch (JavaModelException e) {
					// ignore
				}
			}
			block.statements = statements;
		}
		
		return initializer;
	}

	/*
	 * Convert a source field info into a parsed field declaration
	 */
	private FieldDeclaration convert(SourceFieldElementInfo sourceField, CompilationResult compilationResult) {

		FieldDeclaration field = new FieldDeclaration();

		int start = sourceField.getNameSourceStart();
		int end = sourceField.getNameSourceEnd();

		field.name = sourceField.getName();
		field.sourceStart = start;
		field.sourceEnd = end;
		field.type = createTypeReference(sourceField.getTypeName(), start, end);
		field.declarationSourceStart = sourceField.getDeclarationSourceStart();
		field.declarationSourceEnd = sourceField.getDeclarationSourceEnd();
		field.modifiers = sourceField.getModifiers();

		/* convert local and anonymous types */
		if (this.needLocalTypes) {
			IJavaElement[] children = sourceField.getChildren();
			int typesLength = children.length;
			if (typesLength > 0) {
				ArrayInitializer initializer = new ArrayInitializer();
				field.initialization = initializer;
				Expression[] expressions = new Expression[typesLength];
				initializer.expressions = expressions;
				for (int i = 0; i < typesLength; i++) {
					IJavaElement localType = children[i];
					try {
						TypeDeclaration anonymousLocalTypeDeclaration = convert((SourceTypeElementInfo)((JavaElement)localType).getElementInfo(),compilationResult);
						QualifiedAllocationExpression expression = new QualifiedAllocationExpression(anonymousLocalTypeDeclaration);
						expression.type = anonymousLocalTypeDeclaration.superclass;
						anonymousLocalTypeDeclaration.superclass = null;
						anonymousLocalTypeDeclaration.superInterfaces = null;
						anonymousLocalTypeDeclaration.allocation = expression;
						expressions[i] = expression;
					} catch (JavaModelException e) {
						// ignore
					}
				}
			}
		}
		
		return field;
	}

	/*
	 * Convert a source method info into a parsed method/constructor declaration 
	 */
	private AbstractMethodDeclaration convert(SourceMethodElementInfo sourceMethod, CompilationResult compilationResult) {

		AbstractMethodDeclaration method;

		/* only source positions available */
		int start = sourceMethod.getNameSourceStart();
		int end = sourceMethod.getNameSourceEnd();

		if (sourceMethod.isConstructor()) {
			ConstructorDeclaration decl = new ConstructorDeclaration(compilationResult);
			decl.isDefaultConstructor = false;
			method = decl;
		} else {
			MethodDeclaration decl = new MethodDeclaration(compilationResult);
			/* convert return type */
			decl.returnType =
				createTypeReference(sourceMethod.getReturnTypeName(), start, end);
			method = decl;
		}
		method.selector = sourceMethod.getSelector();
		method.modifiers = sourceMethod.getModifiers();
		method.sourceStart = start;
		method.sourceEnd = end;
		method.declarationSourceStart = sourceMethod.getDeclarationSourceStart();
		method.declarationSourceEnd = sourceMethod.getDeclarationSourceEnd();

		/* convert arguments */
		char[][] argumentTypeNames = sourceMethod.getArgumentTypeNames();
		char[][] argumentNames = sourceMethod.getArgumentNames();
		int argumentCount = argumentTypeNames == null ? 0 : argumentTypeNames.length;
		long position = (long) start << 32 + end;
		method.arguments = new Argument[argumentCount];
		for (int i = 0; i < argumentCount; i++) {
			method.arguments[i] =
				new Argument(
					argumentNames[i],
					position,
					createTypeReference(argumentTypeNames[i], start, end),
					AccDefault,
					false);
			// do not care whether was final or not
		}

		/* convert thrown exceptions */
		char[][] exceptionTypeNames = sourceMethod.getExceptionTypeNames();
		int exceptionCount = exceptionTypeNames == null ? 0 : exceptionTypeNames.length;
		method.thrownExceptions = new TypeReference[exceptionCount];
		for (int i = 0; i < exceptionCount; i++) {
			method.thrownExceptions[i] =
				createTypeReference(exceptionTypeNames[i], start, end);
		}
		
		/* convert local and anonymous types */
		if (this.needLocalTypes) {
			IJavaElement[] children = sourceMethod.getChildren();
			int typesLength = children.length;
			if (typesLength != 0) {
				Statement[] statements = new Statement[typesLength];
				for (int i = 0; i < typesLength; i++) {
					JavaElement type = (JavaElement)children[i];
					try {
						TypeDeclaration localType = convert((SourceTypeElementInfo)type.getElementInfo(), compilationResult);
						if ((localType.bits & ASTNode.IsAnonymousTypeMASK) != 0) {
							QualifiedAllocationExpression expression = new QualifiedAllocationExpression(localType);
							expression.type = localType.superclass;
							localType.superclass = null;
							localType.superInterfaces = null;
							localType.allocation = expression;
							statements[i] = expression;
						} else {
							statements[i] = localType;
						}
					} catch (JavaModelException e) {
						// ignore
					}
				}
				method.statements = statements;
			}
		}
		
		return method;
	}

	/*
	 * Convert a source type info into a parsed type declaration
	 */
	private TypeDeclaration convert(SourceTypeElementInfo sourceType, CompilationResult compilationResult) {
		
		/* create type declaration - can be member type, local type or anonymous type */
		TypeDeclaration type = new TypeDeclaration(compilationResult);
		if (sourceType.getEnclosingType() == null) {
			IType typeHandle = sourceType.getHandle();
			try {
				if (typeHandle.isAnonymous()) {
					type.name = TypeDeclaration.ANONYMOUS_EMPTY_NAME;
					type.bits |= ASTNode.AnonymousAndLocalMask;
				} else {
					if (typeHandle.isLocal()) {
						type.bits |= ASTNode.IsLocalTypeMASK;
					}
				}
			} catch (JavaModelException e) {
				// could not figure, assume toplevel
			}
		}  else {
			type.bits |= ASTNode.IsMemberTypeMASK;
		}
		if ((type.bits & ASTNode.IsAnonymousTypeMASK) == 0) {
			type.name = sourceType.getName();
		}
		int start, end; // only positions available
		type.sourceStart = start = sourceType.getNameSourceStart();
		type.sourceEnd = end = sourceType.getNameSourceEnd();
		type.modifiers = sourceType.getModifiers();
		type.declarationSourceStart = sourceType.getDeclarationSourceStart();
		type.declarationSourceEnd = sourceType.getDeclarationSourceEnd();
		type.bodyEnd = type.declarationSourceEnd;

		/* set superclass and superinterfaces */
		if (sourceType.getSuperclassName() != null) {
			type.superclass = createTypeReference(sourceType.getSuperclassName(), start, end);
			type.superclass.bits |= ASTNode.IsSuperType;
		}
		char[][] interfaceNames = sourceType.getInterfaceNames();
		int interfaceCount = interfaceNames == null ? 0 : interfaceNames.length;
		type.superInterfaces = new TypeReference[interfaceCount];
		for (int i = 0; i < interfaceCount; i++) {
			type.superInterfaces[i] = createTypeReference(interfaceNames[i], start, end);
			type.superInterfaces[i].bits |= ASTNode.IsSuperType;
		}
		
		/* convert member types */
		ISourceType[] sourceMemberTypes = sourceType.getMemberTypes();
		int sourceMemberTypeCount =
			sourceMemberTypes == null ? 0 : sourceMemberTypes.length;
		type.memberTypes = new TypeDeclaration[sourceMemberTypeCount];
		for (int i = 0; i < sourceMemberTypeCount; i++) {
			type.memberTypes[i] = convert((SourceTypeElementInfo)sourceMemberTypes[i], compilationResult);
		}
		
		/* convert fields and initializers */
		ISourceField[] sourceFields = sourceType.getFields();
		int sourceFieldCount = sourceFields == null ? 0 : sourceFields.length;
		InitializerElementInfo[] initializers = null;
		int initializerCount = 0;
		if (this.needLocalTypes) {
			initializers = sourceType.getInitializers();
			initializerCount = initializers.length;
			type.fields = new FieldDeclaration[initializerCount + sourceFieldCount];
			for (int i = 0; i < initializerCount; i++) {
				type.fields[i] = convert(initializers[i], compilationResult);
			}
		} else {
			type.fields = new FieldDeclaration[sourceFieldCount];
		}
		int length = initializerCount + sourceFieldCount;
		int index = 0;
		for (int i = initializerCount; i < length; i++) {
			type.fields[i] = convert((SourceFieldElementInfo)sourceFields[index++], compilationResult);
		}

		/* convert methods - need to add default constructor if necessary */
		ISourceMethod[] sourceMethods = sourceType.getMethods();
		int sourceMethodCount = sourceMethods == null ? 0 : sourceMethods.length;

		/* source type has a constructor ?           */
		/* by default, we assume that one is needed. */
		int neededCount = 0;
		if (!type.isInterface()) {
			neededCount = 1;
			for (int i = 0; i < sourceMethodCount; i++) {
				if (sourceMethods[i].isConstructor()) {
					neededCount = 0;
					// Does not need the extra constructor since one constructor already exists.
					break;
				}
			}
		}
		type.methods = new AbstractMethodDeclaration[sourceMethodCount + neededCount];
		if (neededCount != 0) { // add default constructor in first position
			type.methods[0] = type.createsInternalConstructor(false, false);
		}
		boolean isInterface = type.isInterface();
		for (int i = 0; i < sourceMethodCount; i++) {
			AbstractMethodDeclaration method =convert((SourceMethodElementInfo)sourceMethods[i], compilationResult);
			if (isInterface || method.isAbstract()) { // fix-up flag 
				method.modifiers |= AccSemicolonBody;
			}
			type.methods[neededCount + i] = method;
		}

		return type;
	}

	/*
	 * Convert a set of source element types into a parsed compilation unit declaration
	 * The argument types are then all grouped in the same unit. The argument types must 
	 * at least contain one type.
	 */
	private CompilationUnitDeclaration convert(SourceTypeElementInfo[] sourceTypes, CompilationResult compilationResult) {
		
		SourceTypeElementInfo sourceType = sourceTypes[0];
		if (sourceType.getName() == null)
			return null; // do a basic test that the sourceType is valid

		this.unit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);
		// not filled at this point

		/* only positions available */
		int start = sourceType.getNameSourceStart();
		int end = sourceType.getNameSourceEnd();

		/* convert package and imports */
		if (sourceType.getPackageName() != null
			&& sourceType.getPackageName().length > 0)
			// if its null then it is defined in the default package
			this.unit.currentPackage =
				createImportReference(sourceType.getPackageName(), start, end, false, AccDefault);
		ISourceImport[]  sourceImports = sourceType.getImports();
		int importCount = sourceImports == null ? 0 : sourceImports.length;
		this.unit.imports = new ImportReference[importCount];
		for (int i = 0; i < importCount; i++) {
			ISourceImport sourceImport = sourceImports[i];
			this.unit.imports[i] = createImportReference(
				sourceImport.getName(), 
				sourceImport.getDeclarationSourceStart(),
				sourceImport.getDeclarationSourceEnd(),
				sourceImport.onDemand(),
				sourceImport.getModifiers());
		}
		/* convert type(s) */
		int typeCount = sourceTypes.length;
		this.unit.types = new TypeDeclaration[typeCount];
		for (int i = 0; i < typeCount; i++) {
			this.unit.types[i] = convert(sourceTypes[i], compilationResult);
		}
		return this.unit;
	}

	/*
	 * Build an import reference from an import name, e.g. java.lang.*
	 */
	private ImportReference createImportReference(
		char[] importName,
		int start,
		int end, 
		boolean onDemand,
		int modifiers) {
	
		char[][] qImportName = CharOperation.splitOn('.', importName);
		long[] positions = new long[qImportName.length];
		long position = (long) start << 32 + end;
		for (int i = 0; i < qImportName.length; i++) {
			positions[i] = position; // dummy positions
		}
		return new ImportReference(
			qImportName,
			positions,
			onDemand,
			modifiers);
	}

	/*
	 * Build a type reference from a readable name, e.g. java.lang.Object[][]
	 */
	private TypeReference createTypeReference(
		char[] typeSignature,
		int start,
		int end) {

		/* count identifiers and dimensions */
		int max = typeSignature.length;
		int dimStart = max;
		int dim = 0;
		int identCount = 1;
		for (int i = 0; i < max; i++) {
			switch (typeSignature[i]) {
				case '[' :
					if (dim == 0)
						dimStart = i;
					dim++;
					break;
				case '.' :
					identCount++;
					break;
			}
		}
		/* rebuild identifiers and dimensions */
		if (identCount == 1) { // simple type reference
			if (dim == 0) {
				return new SingleTypeReference(typeSignature, (((long) start )<< 32) + end);
			} else {
				char[] identifier = new char[dimStart];
				System.arraycopy(typeSignature, 0, identifier, 0, dimStart);
				return new ArrayTypeReference(identifier, dim, (((long) start) << 32) + end);
			}
		} else { // qualified type reference
			long[] positions = new long[identCount];
			long pos = (((long) start) << 32) + end;
			for (int i = 0; i < identCount; i++) {
				positions[i] = pos;
			}
			char[][] identifiers =
				CharOperation.splitOn('.', typeSignature, 0, dimStart);
			if (dim == 0) {
				return new QualifiedTypeReference(identifiers, positions);
			} else {
				return new ArrayQualifiedTypeReference(identifiers, dim, positions);
			}
		}
	}
}

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
package org.eclipse.jdt.internal.compiler.parser;

/**
 * Converter from source element type to parsed compilation unit.
 *
 * Limitation:
 * | The source element field does not carry any information for its constant part, thus
 * | the converted parse tree will not include any field initializations.
 * | Therefore, any binary produced by compiling against converted source elements will
 * | not take advantage of remote field constant inlining.
 * | Given the intended purpose of the conversion is to resolve references, this is not
 * | a problem.
 *
 */

import java.util.ArrayList;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ArrayQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.SingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeParameter;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.SourceFieldElementInfo;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;

public class SourceTypeConverter implements CompilerModifiers {
	
	public static final int FIELD = 0x01;
	public static final int CONSTRUCTOR = 0x02;
	public static final int METHOD = 0x04;
	public static final int MEMBER_TYPE = 0x08;
	public static final int FIELD_INITIALIZATION = 0x10;
	public static final int FIELD_AND_METHOD = FIELD | CONSTRUCTOR | METHOD;
	public static final int LOCAL_TYPE = 0x20;
	public static final int NONE = 0;
	
	private int flags;
	private CompilationUnitDeclaration unit;
	private Parser parser;
	private ProblemReporter problemReporter;
	
	int namePos;
	
	private SourceTypeConverter(int flags, ProblemReporter problemReporter) {
		this.flags = flags;
		this.problemReporter = problemReporter;
	}

	/*
	 * Convert a set of source element types into a parsed compilation unit declaration
	 * The argument types are then all grouped in the same unit. The argument types must 
	 * at least contain one type.
	 * Can optionally ignore fields & methods or member types or field initialization
	 */
	public static CompilationUnitDeclaration buildCompilationUnit(
		ISourceType[] sourceTypes,
		int flags,
		ProblemReporter problemReporter,
		CompilationResult compilationResult) {
			
		return 
			new SourceTypeConverter(flags, problemReporter).convert(sourceTypes, compilationResult);
	}

	/*
	 * Convert a set of source element types into a parsed compilation unit declaration
	 * The argument types are then all grouped in the same unit. The argument types must 
	 * at least contain one type.
	 */
	private CompilationUnitDeclaration convert(ISourceType[] sourceTypes, CompilationResult compilationResult) {
		this.unit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);
		// not filled at this point

		if (sourceTypes.length == 0) return this.unit;
		ISourceType sourceType = sourceTypes[0];

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
			this.unit.types[i] =
				convert(sourceTypes[i], compilationResult);
		}
		return this.unit;
	}
	
	/*
	 * Convert an initializerinfo into a parsed initializer declaration
	 */
	private Initializer convert(InitializerElementInfo initializerInfo, CompilationResult compilationResult) {

		Block block = new Block(0);
		Initializer initializer = new Initializer(block, IConstants.AccDefault);

		int start = initializerInfo.getDeclarationSourceStart();
		int end = initializerInfo.getDeclarationSourceEnd();

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
					TypeDeclaration localType = convert((ISourceType)type.getElementInfo(), compilationResult);
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
	 * Convert a field source element into a parsed field declaration
	 */
	private FieldDeclaration convert(ISourceField sourceField, TypeDeclaration type, CompilationResult compilationResult) {

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

		/* conversion of field constant */
		if ((this.flags & FIELD_INITIALIZATION) != 0) {
			char[] initializationSource = sourceField.getInitializationSource();
			if (initializationSource != null) {
				if (this.parser == null) {
					this.parser = new Parser(this.problemReporter, true);
				}
				this.parser.parse(field, type, this.unit, initializationSource);
			}
		}
		
		/* conversion of local and anonymous types */
		if ((this.flags & LOCAL_TYPE) != 0 && sourceField instanceof SourceFieldElementInfo) {
			IJavaElement[] children = ((SourceFieldElementInfo)sourceField).getChildren();
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
	 * Convert a method source element into a parsed method/constructor declaration 
	 */
	private AbstractMethodDeclaration convert(ISourceMethod sourceMethod, CompilationResult compilationResult) {

		AbstractMethodDeclaration method;

		/* only source positions available */
		int start = sourceMethod.getNameSourceStart();
		int end = sourceMethod.getNameSourceEnd();

		/* convert type parameters */
		char[][] typeParameterNames = sourceMethod.getTypeParameterNames();
		TypeParameter[] typeParams = null;
		if (typeParameterNames != null) {
			int parameterCount = typeParameterNames.length;
			char[][][] typeParameterBounds = sourceMethod.getTypeParameterBounds();
			typeParams = new TypeParameter[parameterCount];
			for (int i = 0; i < parameterCount; i++) {
				typeParams[i] = createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
			}
		}
		
		if (sourceMethod.isConstructor()) {
			ConstructorDeclaration decl = new ConstructorDeclaration(compilationResult);
			decl.isDefaultConstructor = false;
			method = decl;
			decl.typeParameters = typeParams;
		} else {
			MethodDeclaration decl = new MethodDeclaration(compilationResult);
			/* convert return type */
			decl.returnType =
				createTypeReference(sourceMethod.getReturnTypeName(), start, end);
			method = decl;
			decl.typeParameters = typeParams;
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
		if ((this.flags & LOCAL_TYPE) != 0 && sourceMethod instanceof SourceMethodElementInfo) {
			IJavaElement[] children = ((SourceMethodElementInfo)sourceMethod).getChildren();
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
	 * Convert a source element type into a parsed type declaration
	 */
	private TypeDeclaration convert(ISourceType sourceType, CompilationResult compilationResult) {
		/* create type declaration - can be member type */
		TypeDeclaration type = new TypeDeclaration(compilationResult);
		if (sourceType.getEnclosingType() == null && sourceType instanceof SourceTypeElementInfo) {
			IType typeHandle = ((SourceTypeElementInfo)sourceType).getHandle();
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
		type.name = sourceType.getName();
		int start, end; // only positions available
		type.sourceStart = start = sourceType.getNameSourceStart();
		type.sourceEnd = end = sourceType.getNameSourceEnd();
		type.modifiers = sourceType.getModifiers();
		type.declarationSourceStart = sourceType.getDeclarationSourceStart();
		type.declarationSourceEnd = sourceType.getDeclarationSourceEnd();
		type.bodyEnd = type.declarationSourceEnd;

		/* convert type parameters */
		char[][] typeParameterNames = sourceType.getTypeParameterNames();
		if (typeParameterNames != null && typeParameterNames.length > 0) { // TODO (jerome) fix once ISourceType's spec & SourceTypeElementInfo match
			int parameterCount = typeParameterNames.length;
			char[][][] typeParameterBounds = sourceType.getTypeParameterBounds();
			type.typeParameters = new TypeParameter[parameterCount];
			for (int i = 0; i < parameterCount; i++) {
				type.typeParameters[i] = createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
			}
		}
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
		if ((this.flags & MEMBER_TYPE) != 0) {
			ISourceType[] sourceMemberTypes = sourceType.getMemberTypes();
			int sourceMemberTypeCount =
				sourceMemberTypes == null ? 0 : sourceMemberTypes.length;
			type.memberTypes = new TypeDeclaration[sourceMemberTypeCount];
			for (int i = 0; i < sourceMemberTypeCount; i++) {
				type.memberTypes[i] = convert(sourceMemberTypes[i], compilationResult);
			}
		}

		/* convert intializers and fields*/
		InitializerElementInfo[] initializers = null;
		int initializerCount = 0;
		if ((this.flags & LOCAL_TYPE) != 0 && sourceType instanceof SourceTypeElementInfo) {
			initializers = ((SourceTypeElementInfo)sourceType).getInitializers();
			initializerCount = initializers.length;
		}
		ISourceField[] sourceFields = null;
		int sourceFieldCount = 0;
		if ((this.flags & FIELD) != 0) {
			sourceFields = sourceType.getFields();
			sourceFieldCount = sourceFields == null ? 0 : sourceFields.length;
		}
		int length = initializerCount + sourceFieldCount;
		if (length > 0) {
			type.fields = new FieldDeclaration[length];
			for (int i = 0; i < initializerCount; i++) {
				type.fields[i] = convert(initializers[i], compilationResult);
			}
			int index = 0;
			for (int i = initializerCount; i < length; i++) {
				type.fields[i] = convert(sourceFields[index++], type, compilationResult);
			}
		}

		/* convert methods - need to add default constructor if necessary */
		boolean needConstructor = (this.flags & CONSTRUCTOR) != 0;
		boolean needMethod = (this.flags & METHOD) != 0;
		if (needConstructor || needMethod) {
			
			ISourceMethod[] sourceMethods = sourceType.getMethods();
			int sourceMethodCount = sourceMethods == null ? 0 : sourceMethods.length;
	
			/* source type has a constructor ?           */
			/* by default, we assume that one is needed. */
			int extraConstructor = 0;
			int methodCount = 0;
			boolean isInterface = type.isInterface();
			if (!isInterface) {
				extraConstructor = needConstructor ? 1 : 0;
				for (int i = 0; i < sourceMethodCount; i++) {
					if (sourceMethods[i].isConstructor()) {
						if (needConstructor) {
							extraConstructor = 0; // Does not need the extra constructor since one constructor already exists.
							methodCount++;
						}
					} else if (needMethod) {
						methodCount++;
					}
				}
			} else {
				methodCount = needMethod ? sourceMethodCount : 0;
			}
			type.methods = new AbstractMethodDeclaration[methodCount + extraConstructor];
			if (extraConstructor != 0) { // add default constructor in first position
				type.methods[0] = type.createsInternalConstructor(false, false);
			}
			int index = 0;
			for (int i = 0; i < sourceMethodCount; i++) {
				ISourceMethod sourceMethod = sourceMethods[i];
				boolean isConstructor = sourceMethod.isConstructor();
				if ((isConstructor && needConstructor) || (!isConstructor && needMethod)) {
					AbstractMethodDeclaration method =convert(sourceMethod, compilationResult);
					if (isInterface || method.isAbstract()) { // fix-up flag 
						method.modifiers |= AccSemicolonBody;
					}
					type.methods[extraConstructor + index++] = method;
				}
			}
		}
		
		return type;
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

	private TypeParameter createTypeParameter(char[] typeParameterName, char[][] typeParameterBounds, int start, int end) {

		TypeParameter parameter = new TypeParameter();
		parameter.name = typeParameterName;
		parameter.sourceStart = start;
		parameter.sourceEnd = end;
		if (typeParameterBounds != null) {
			int length = typeParameterBounds.length;
			parameter.bounds = new TypeReference[length];
			for (int i = 0; i < length; i++) {
				TypeReference bound = createTypeReference(typeParameterBounds[i], start, end);
				bound.bits |= ASTNode.IsSuperType;
				parameter.bounds[i] = bound;
			}
		}
		return parameter;
	}
	
	/*
	 * Build a type reference from a readable name, e.g. java.lang.Object[][]
	 */
	private TypeReference createTypeReference(
		char[] typeName,
		int start,
		int end) {

		int length = typeName.length;
		this.namePos = 0;
		TypeReference type = decodeType(typeName, length, start, end);
		return type;
	}
	private TypeReference decodeType(char[] typeName, int length, int start, int end) {
		int identCount = 1;
		int dim = 0;
		int nameFragmentStart = this.namePos, nameFragmentEnd = -1;
		ArrayList fragments = null;
		typeLoop: while (this.namePos < length) {
			char currentChar = typeName[this.namePos];
			switch (currentChar) {
				case '?' :
					this.namePos++; // skip '?'
					while (typeName[this.namePos] == ' ') this.namePos++;
					switch(typeName[this.namePos]) {
						case 's' :
							checkSuper: {
								int max = TypeConstants.WILDCARD_SUPER.length-1;
								for (int ahead = 1; ahead < max; ahead++) {
									if (typeName[this.namePos+ahead] != TypeConstants.WILDCARD_SUPER[ahead+1]) {
										break checkSuper;
									}
								}
								this.namePos += max;
								Wildcard result = new Wildcard(Wildcard.SUPER);
								result.bound = decodeType(typeName, length, start, end);
								result.sourceStart = start;
								result.sourceEnd = end;
								return result;
							}
							break;
						case 'e' :
							checkExtends: {
								int max = TypeConstants.WILDCARD_EXTENDS.length-1;
								for (int ahead = 1; ahead < max; ahead++) {
									if (typeName[this.namePos+ahead] != TypeConstants.WILDCARD_EXTENDS[ahead+1]) {
										break checkExtends;
									}
								}
								this.namePos += max;
								Wildcard result = new Wildcard(Wildcard.EXTENDS);
								result.bound = decodeType(typeName, length, start, end);
								result.sourceStart = start;
								result.sourceEnd = end;
								return result;
							}
							break;
					}
					Wildcard result = new Wildcard(Wildcard.UNBOUND);
					result.sourceStart = start;
					result.sourceEnd = end;
					return result;
				case '[' :
					if (dim == 0) nameFragmentEnd = this.namePos-1;
					dim++;
					break;
				case ']' :
					break;
				case '>' :
				case ',' :
					break typeLoop;
				case '.' :
					if (nameFragmentStart < 0) nameFragmentStart = this.namePos+1; // member type name
					identCount ++;
					break;
				case '<' :
					if (fragments == null) fragments = new ArrayList(2);
					nameFragmentEnd = this.namePos-1;
					char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, this.namePos);
					fragments.add(identifiers);
					this.namePos++; // skip '<'
					TypeReference[] arguments = decodeTypeArguments(typeName, length, start, end); // positionned on '>' at end
					fragments.add(arguments);
					identCount = 0;
					nameFragmentStart = -1;
					nameFragmentEnd = -1;
					// next increment will skip '>'
			}
			this.namePos++;
		}
		if (nameFragmentEnd < 0) nameFragmentEnd = this.namePos-1;
		if (fragments == null) { // non parameterized 
			/* rebuild identifiers and dimensions */
			if (identCount == 1) { // simple type reference
				if (dim == 0) {
					char[] nameFragment;
					if (nameFragmentStart != 0 || nameFragmentEnd >= 0) {
						int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
						System.arraycopy(typeName, nameFragmentStart, nameFragment = new char[nameFragmentLength], 0, nameFragmentLength);						
					} else {
						nameFragment = typeName;
					}
					return new SingleTypeReference(nameFragment, (((long) start )<< 32) + end);
				} else {
					int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
					char[] nameFragment = new char[nameFragmentLength];
					System.arraycopy(typeName, nameFragmentStart, nameFragment, 0, nameFragmentLength);
					return new ArrayTypeReference(nameFragment, dim, (((long) start) << 32) + end);
				}
			} else { // qualified type reference
				long[] positions = new long[identCount];
				long pos = (((long) start) << 32) + end;
				for (int i = 0; i < identCount; i++) {
					positions[i] = pos;
				}
				char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd+1);
				if (dim == 0) {
					return new QualifiedTypeReference(identifiers, positions);
				} else {
					return new ArrayQualifiedTypeReference(identifiers, dim, positions);
				}
			}
		} else { // parameterized
			// rebuild type reference from available fragments: char[][], arguments, char[][], arguments...
			// check trailing qualified name
			if (nameFragmentStart > 0 && nameFragmentStart < length) {
				char[][] identifiers = CharOperation.splitOn('.', typeName, nameFragmentStart, nameFragmentEnd+1);
				fragments.add(identifiers);
			}
			int fragmentLength = fragments.size();
			if (fragmentLength == 2) {
				char[][] firstFragment = (char[][]) fragments.get(0);
				if (firstFragment.length == 1) {
					// parameterized single type
					return new ParameterizedSingleTypeReference(firstFragment[0], (TypeReference[]) fragments.get(1), dim, (((long) start) << 32) + end);
				}
			}
			// parameterized qualified type
			identCount = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					identCount += ((char[][])element).length;
				}
			}
			char[][] tokens = new char[identCount][];
			TypeReference[][] arguments = new TypeReference[identCount][];
			int index = 0;
			for (int i = 0; i < fragmentLength; i ++) {
				Object element = fragments.get(i);
				if (element instanceof char[][]) {
					char[][] fragmentTokens = (char[][]) element;
					int fragmentTokenLength = fragmentTokens.length;
					System.arraycopy(fragmentTokens, 0, tokens, index, fragmentTokenLength);
					index += fragmentTokenLength;
				} else {
					arguments[index-1] = (TypeReference[]) element;
				}
			}
			long[] positions = new long[identCount];
			long pos = (((long) start) << 32) + end;
			for (int i = 0; i < identCount; i++) {
				positions[i] = pos;
			}
			return new ParameterizedQualifiedTypeReference(tokens, arguments, dim, positions);
		}
	}
	
	private TypeReference[] decodeTypeArguments(char[] typeName, int length, int start, int end) {
		ArrayList argumentList = new ArrayList(1);
		int count = 0;
		argumentsLoop: while (this.namePos < length) {
			TypeReference argument = decodeType(typeName, length, start, end);
			count++;
			argumentList.add(argument);
			if (this.namePos >= length) break argumentsLoop;
			if (typeName[this.namePos] == '>') {
				break argumentsLoop;
			}
			this.namePos++; // skip ','
		}
		TypeReference[] typeArguments = new TypeReference[count];
		argumentList.toArray(typeArguments);
		return typeArguments;
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import java.util.HashMap;

import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
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
import org.eclipse.jdt.internal.compiler.env.ISourceImport;
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
	private ICompilationUnit cu;
	private char[] source;
	private HashMap annotationPositions;
	
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
			
//		long start = System.currentTimeMillis();
		SourceTypeConverter converter = new SourceTypeConverter(flags, problemReporter);
		try {
			return converter.convert(sourceTypes, compilationResult);
		} catch (JavaModelException e) {
			return null;
/*		} finally {
			System.out.println("Spent " + (System.currentTimeMillis() - start) + "ms to convert " + ((JavaElement) converter.cu).toStringWithAncestors());
*/		}
	}

	/*
	 * Convert a set of source element types into a parsed compilation unit declaration
	 * The argument types are then all grouped in the same unit. The argument types must 
	 * at least contain one type.
	 */
	private CompilationUnitDeclaration convert(ISourceType[] sourceTypes, CompilationResult compilationResult) throws JavaModelException {
		this.unit = new CompilationUnitDeclaration(this.problemReporter, compilationResult, 0);
		// not filled at this point

		if (sourceTypes.length == 0) return this.unit;
		SourceTypeElementInfo topLevelTypeInfo = (SourceTypeElementInfo) sourceTypes[0];
		this.cu = (ICompilationUnit) topLevelTypeInfo.getHandle().getCompilationUnit();
		this.annotationPositions = ((CompilationUnitElementInfo) ((JavaElement) this.cu).getElementInfo()).annotationPositions;

		if (this.annotationPositions != null && this.annotationPositions.size() > 10) { // experimental value
			// if more than 10 annotations, diet parse as this is faster
			return new Parser(this.problemReporter, true).dietParse(this.cu, compilationResult);
		}

		/* only positions available */
		int start = topLevelTypeInfo.getNameSourceStart();
		int end = topLevelTypeInfo.getNameSourceEnd();

		/* convert package and imports */
		if (topLevelTypeInfo.getPackageName() != null
			&& topLevelTypeInfo.getPackageName().length > 0)
			// if its null then it is defined in the default package
			this.unit.currentPackage =
				createImportReference(topLevelTypeInfo.getPackageName(), start, end, false, AccDefault);
		ISourceImport[]  sourceImports = topLevelTypeInfo.getImports();
		int importCount = sourceImports.length;
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
			SourceTypeElementInfo typeInfo = (SourceTypeElementInfo) sourceTypes[i];
			this.unit.types[i] =
				convert((SourceType) typeInfo.getHandle(), compilationResult);
		}
		return this.unit;
	}
	
	/*
	 * Convert an initializerinfo into a parsed initializer declaration
	 */
	private Initializer convert(InitializerElementInfo initializerInfo, CompilationResult compilationResult) throws JavaModelException {

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
				SourceType type = (SourceType) children[i];
				TypeDeclaration localType = convert(type, compilationResult);
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
			}
			block.statements = statements;
		}
		
		return initializer;
	}

	/*
	 * Convert a field source element into a parsed field declaration
	 */
	private FieldDeclaration convert(SourceField fieldHandle, TypeDeclaration type, CompilationResult compilationResult) throws JavaModelException {

		SourceFieldElementInfo fieldInfo = (SourceFieldElementInfo) fieldHandle.getElementInfo();
		FieldDeclaration field = new FieldDeclaration();

		int start = fieldInfo.getNameSourceStart();
		int end = fieldInfo.getNameSourceEnd();

		field.name = fieldHandle.getElementName().toCharArray();
		field.sourceStart = start;
		field.sourceEnd = end;
		field.declarationSourceStart = fieldInfo.getDeclarationSourceStart();
		field.declarationSourceEnd = fieldInfo.getDeclarationSourceEnd();
		int modifiers = fieldInfo.getModifiers();
		boolean isEnumConstant = (modifiers & AccEnum) != 0;
		if (isEnumConstant) {
			field.modifiers = modifiers & ~Flags.AccEnum; // clear AccEnum bit onto AST (binding will add it)
		} else {
			field.modifiers = modifiers;
			field.type = createTypeReference(fieldInfo.getTypeName(), start, end);
		}

		/* convert annotations */
		field.annotations = convertAnnotations(fieldHandle);

		/* conversion of field constant */
		if ((this.flags & FIELD_INITIALIZATION) != 0) {
			char[] initializationSource = fieldInfo.getInitializationSource();
			if (initializationSource != null) {
				if (this.parser == null) {
					this.parser = new Parser(this.problemReporter, true);
				}
				this.parser.parse(field, type, this.unit, initializationSource);
			}
		}
		
		/* conversion of local and anonymous types */
		if ((this.flags & LOCAL_TYPE) != 0) {
			IJavaElement[] children = fieldInfo.getChildren();
			int childrenLength = children.length;
			if (childrenLength > 0) {
				ArrayInitializer initializer = new ArrayInitializer();
				field.initialization = initializer;
				Expression[] expressions = new Expression[childrenLength];
				initializer.expressions = expressions;
				for (int i = 0; i < childrenLength; i++) {
					IJavaElement localType = children[i];
					TypeDeclaration anonymousLocalTypeDeclaration = convert((SourceType) localType, compilationResult);
					QualifiedAllocationExpression expression = new QualifiedAllocationExpression(anonymousLocalTypeDeclaration);
					expression.type = anonymousLocalTypeDeclaration.superclass;
					anonymousLocalTypeDeclaration.superclass = null;
					anonymousLocalTypeDeclaration.superInterfaces = null;
					anonymousLocalTypeDeclaration.allocation = expression;
					anonymousLocalTypeDeclaration.modifiers &= ~AccEnum; // remove tag in case this is the init of an enum constant
					expressions[i] = expression;
				}
			}
		}
		return field;
	}

	/*
	 * Convert a method source element into a parsed method/constructor declaration 
	 */
	private AbstractMethodDeclaration convert(SourceMethod methodHandle, CompilationResult compilationResult) throws JavaModelException {

		SourceMethodElementInfo methodInfo = (SourceMethodElementInfo) methodHandle.getElementInfo();
		AbstractMethodDeclaration method;

		/* only source positions available */
		int start = methodInfo.getNameSourceStart();
		int end = methodInfo.getNameSourceEnd();

		/* convert type parameters */
		char[][] typeParameterNames = methodInfo.getTypeParameterNames();
		TypeParameter[] typeParams = null;
		if (typeParameterNames != null) {
			int parameterCount = typeParameterNames.length;
			if (parameterCount > 0) { // method's type parameters must be null if no type parameter
				char[][][] typeParameterBounds = methodInfo.getTypeParameterBounds();
				typeParams = new TypeParameter[parameterCount];
				for (int i = 0; i < parameterCount; i++) {
					typeParams[i] = createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
				}
			}
		}
		
		int modifiers = methodInfo.getModifiers();
		if (methodInfo.isConstructor()) {
			ConstructorDeclaration decl = new ConstructorDeclaration(compilationResult);
			decl.isDefaultConstructor = false;
			method = decl;
			decl.typeParameters = typeParams;
		} else {
			MethodDeclaration decl;
			if (methodInfo.isAnnotationMethod()) {
				AnnotationMethodDeclaration annotationMethodDeclaration = new AnnotationMethodDeclaration(compilationResult);

				/* conversion of default value */
				if ((this.flags & FIELD_INITIALIZATION) != 0) {
					char[] defaultValueSource = ((SourceAnnotationMethodInfo) methodInfo).getDefaultValueSource(getSource());
					if (defaultValueSource != null) {
						Expression expression =  parseMemberValue(defaultValueSource);
						if (expression != null) {
							annotationMethodDeclaration.defaultValue = expression;
							modifiers |= AccAnnotationDefault;
						}
					}
				}
				decl = annotationMethodDeclaration;
			} else {
				decl = new MethodDeclaration(compilationResult);
			}
			
			// convert return type
			decl.returnType = createTypeReference(methodInfo.getReturnTypeName(), start, end);
			
			// type parameters
			decl.typeParameters = typeParams;
			
			method = decl;
		}
		method.selector = methodHandle.getElementName().toCharArray();
		boolean isVarargs = (modifiers & AccVarargs) != 0;
		method.modifiers = modifiers & ~AccVarargs;
		method.sourceStart = start;
		method.sourceEnd = end;
		method.declarationSourceStart = methodInfo.getDeclarationSourceStart();
		method.declarationSourceEnd = methodInfo.getDeclarationSourceEnd();

		/* convert annotations */
		method.annotations = convertAnnotations(methodHandle);

		/* convert arguments */
		String[] argumentTypeSignatures = methodHandle.getParameterTypes();
		char[][] argumentNames = methodInfo.getArgumentNames();
		int argumentCount = argumentTypeSignatures == null ? 0 : argumentTypeSignatures.length;
		long position = ((long) start << 32) + end;
		method.arguments = new Argument[argumentCount];
		for (int i = 0; i < argumentCount; i++) {
			char[] typeName = Signature.toCharArray(argumentTypeSignatures[i].toCharArray());
			TypeReference typeReference = createTypeReference(typeName, start, end);
			if (isVarargs && i == argumentCount-1) {
				typeReference.bits |= ASTNode.IsVarArgs;
			}
			method.arguments[i] =
				new Argument(
					argumentNames[i],
					position,
					typeReference,
					AccDefault);
			// do not care whether was final or not
		}

		/* convert thrown exceptions */
		char[][] exceptionTypeNames = methodInfo.getExceptionTypeNames();
		int exceptionCount = exceptionTypeNames == null ? 0 : exceptionTypeNames.length;
		method.thrownExceptions = new TypeReference[exceptionCount];
		for (int i = 0; i < exceptionCount; i++) {
			method.thrownExceptions[i] =
				createTypeReference(exceptionTypeNames[i], start, end);
		}
		
		/* convert local and anonymous types */
		if ((this.flags & LOCAL_TYPE) != 0) {
			IJavaElement[] children = methodInfo.getChildren();
			int typesLength = children.length;
			if (typesLength != 0) {
				Statement[] statements = new Statement[typesLength];
				for (int i = 0; i < typesLength; i++) {
					SourceType type = (SourceType) children[i];
					TypeDeclaration localType = convert(type, compilationResult);
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
				}
				method.statements = statements;
			}
		}
		
		return method;
	}

	/*
	 * Convert a source element type into a parsed type declaration
	 */
	private TypeDeclaration convert(SourceType typeHandle, CompilationResult compilationResult) throws JavaModelException {
		SourceTypeElementInfo typeInfo = (SourceTypeElementInfo) typeHandle.getElementInfo();
		/* create type declaration - can be member type */
		TypeDeclaration type = new TypeDeclaration(compilationResult);
		if (typeInfo.getEnclosingType() == null) {
			if (typeHandle.isAnonymous()) {
				type.name = TypeDeclaration.ANONYMOUS_EMPTY_NAME;
				type.bits |= ASTNode.AnonymousAndLocalMask;
			} else {
				if (typeHandle.isLocal()) {
					type.bits |= ASTNode.IsLocalTypeMASK;
				}
			}
		}  else {
			type.bits |= ASTNode.IsMemberTypeMASK;
		}
		if ((type.bits & ASTNode.IsAnonymousTypeMASK) == 0) {
			type.name = typeInfo.getName();
		}
		type.name = typeInfo.getName();
		int start, end; // only positions available
		type.sourceStart = start = typeInfo.getNameSourceStart();
		type.sourceEnd = end = typeInfo.getNameSourceEnd();
		type.modifiers = typeInfo.getModifiers();
		type.declarationSourceStart = typeInfo.getDeclarationSourceStart();
		type.declarationSourceEnd = typeInfo.getDeclarationSourceEnd();
		type.bodyEnd = type.declarationSourceEnd;
		
		/* convert annotations */
		type.annotations = convertAnnotations(typeHandle);

		/* convert type parameters */
		char[][] typeParameterNames = typeInfo.getTypeParameterNames();
		if (typeParameterNames.length > 0) {
			int parameterCount = typeParameterNames.length;
			char[][][] typeParameterBounds = typeInfo.getTypeParameterBounds();
			type.typeParameters = new TypeParameter[parameterCount];
			for (int i = 0; i < parameterCount; i++) {
				type.typeParameters[i] = createTypeParameter(typeParameterNames[i], typeParameterBounds[i], start, end);
			}
		}
		/* set superclass and superinterfaces */
		if (typeInfo.getSuperclassName() != null) {
			type.superclass = createTypeReference(typeInfo.getSuperclassName(), start, end);
			type.superclass.bits |= ASTNode.IsSuperType;
		}
		char[][] interfaceNames = typeInfo.getInterfaceNames();
		int interfaceCount = interfaceNames == null ? 0 : interfaceNames.length;
		if (interfaceCount > 0) {
			type.superInterfaces = new TypeReference[interfaceCount];
			for (int i = 0; i < interfaceCount; i++) {
				type.superInterfaces[i] = createTypeReference(interfaceNames[i], start, end);
				type.superInterfaces[i].bits |= ASTNode.IsSuperType;
			}
		}
		/* convert member types */
		if ((this.flags & MEMBER_TYPE) != 0) {
			SourceType[] sourceMemberTypes = typeInfo.getMemberTypeHandles();
			int sourceMemberTypeCount = sourceMemberTypes.length;
			type.memberTypes = new TypeDeclaration[sourceMemberTypeCount];
			for (int i = 0; i < sourceMemberTypeCount; i++) {
				type.memberTypes[i] = convert(sourceMemberTypes[i], compilationResult);
			}
		}

		/* convert intializers and fields*/
		InitializerElementInfo[] initializers = null;
		int initializerCount = 0;
		if ((this.flags & LOCAL_TYPE) != 0) {
			initializers = typeInfo.getInitializers();
			initializerCount = initializers.length;
		}
		SourceField[] sourceFields = null;
		int sourceFieldCount = 0;
		if ((this.flags & FIELD) != 0) {
			sourceFields = typeInfo.getFieldHandles();
			sourceFieldCount = sourceFields.length;
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
			
			SourceMethod[] sourceMethods = typeInfo.getMethodHandles();
			int sourceMethodCount = sourceMethods.length;
	
			/* source type has a constructor ?           */
			/* by default, we assume that one is needed. */
			int extraConstructor = 0;
			int methodCount = 0;
			int kind = type.kind();
			boolean isAbstract = kind == IGenericType.INTERFACE_DECL || kind == IGenericType.ANNOTATION_TYPE_DECL;
			if (!isAbstract) {
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
				type.methods[0] = type.createDefaultConstructor(false, false);
			}
			int index = 0;
			boolean hasAbstractMethods = false;
			for (int i = 0; i < sourceMethodCount; i++) {
				SourceMethod sourceMethod = sourceMethods[i];
				boolean isConstructor = sourceMethod.isConstructor();
				if ((sourceMethod.getFlags() & Flags.AccAbstract) != 0) {
					hasAbstractMethods = true;
				}
				if ((isConstructor && needConstructor) || (!isConstructor && needMethod)) {
					AbstractMethodDeclaration method = convert(sourceMethod, compilationResult);
					if (isAbstract || method.isAbstract()) { // fix-up flag 
						method.modifiers |= AccSemicolonBody;
					}
					type.methods[extraConstructor + index++] = method;
				}
			}
			if (hasAbstractMethods) type.bits |= ASTNode.HasAbstractMethods;
		}
		
		return type;
	}
	
	private Annotation[] convertAnnotations(JavaElement element) {
		if (this.annotationPositions == null) return null;
		char[] cuSource = getSource();
		long[] positions = (long[]) this.annotationPositions.get(element);
		if (positions == null) return null;
		int length = positions.length;
		Annotation[] annotations = new Annotation[length];
		for (int i = 0; i < length; i++) {
			long position = positions[i];
			int start = (int) (position >>> 32);
			int end = (int) position;
			char[] annotationSource = CharOperation.subarray(cuSource, start, end+1);
			Expression expression = parseMemberValue(annotationSource);
			annotations[i] = (Annotation) expression;
		}
		return annotations;
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
		long position = ((long) start << 32) + end;
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
			if (length > 0) {
				parameter.type = createTypeReference(typeParameterBounds[0], start, end);
				if (length > 1) {
					parameter.bounds = new TypeReference[length-1];
					for (int i = 1; i < length; i++) {
						TypeReference bound = createTypeReference(typeParameterBounds[i], start, end);
						bound.bits |= ASTNode.IsSuperType;
						parameter.bounds[i-1] = bound;
					}
				}
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
					return new SingleTypeReference(nameFragment, ((long) start << 32) + end);
				} else {
					int nameFragmentLength = nameFragmentEnd - nameFragmentStart + 1;
					char[] nameFragment = new char[nameFragmentLength];
					System.arraycopy(typeName, nameFragmentStart, nameFragment, 0, nameFragmentLength);
					return new ArrayTypeReference(nameFragment, dim, ((long) start << 32) + end);
				}
			} else { // qualified type reference
				long[] positions = new long[identCount];
				long pos = ((long) start << 32) + end;
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
					return new ParameterizedSingleTypeReference(firstFragment[0], (TypeReference[]) fragments.get(1), dim, ((long) start << 32) + end);
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
			long pos = ((long) start << 32) + end;
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
	
	private char[] getSource() {
		if (this.source == null)
			this.source = this.cu.getContents();
		return this.source;
	}
	
	private Expression parseMemberValue(char[] memberValue) {
		if (this.parser == null) {
			this.parser = new Parser(this.problemReporter, true);
		}
		return this.parser.parseMemberValue(memberValue, 0, memberValue.length, this.unit);
	}
}

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
package org.eclipse.jdt.core.dom;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.Modifier.ModifierKeyword;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.EnumConstant;
import org.eclipse.jdt.internal.compiler.ast.ForeachStatement;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedQualifiedTypeReference;
import org.eclipse.jdt.internal.compiler.ast.ParameterizedSingleTypeReference;
import org.eclipse.jdt.internal.compiler.ast.TypeReference;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Internal class for converting internal compiler ASTs into public ASTs.
 */
class ASTConverter2 extends ASTConverter {

	public ASTConverter2(Map options, boolean resolveBindings, IProgressMonitor monitor) {
		super(options, resolveBindings, monitor);
	}

	protected void buildBodyDeclarations(org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration typeDeclaration, AnnotationTypeDeclaration typeDecl) {
		// add body declaration in the lexical order
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] members = typeDeclaration.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = typeDeclaration.fields;
		org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		
		int fieldsLength = fields == null? 0 : fields.length;
		int methodsLength = methods == null? 0 : methods.length;
		int membersLength = members == null ? 0 : members.length;
		int fieldsIndex = 0;
		int methodsIndex = 0;
		int membersIndex = 0;
		
		while ((fieldsIndex < fieldsLength)
			|| (membersIndex < membersLength)
			|| (methodsIndex < methodsLength)) {
			org.eclipse.jdt.internal.compiler.ast.FieldDeclaration nextFieldDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration nextMethodDeclaration = null;
			org.eclipse.jdt.internal.compiler.ast.TypeDeclaration nextMemberDeclaration = null;
		
			int position = Integer.MAX_VALUE;
			int nextDeclarationType = -1;
			if (fieldsIndex < fieldsLength) {
				nextFieldDeclaration = fields[fieldsIndex];
				if (nextFieldDeclaration.declarationSourceStart < position) {
					position = nextFieldDeclaration.declarationSourceStart;
					nextDeclarationType = 0; // FIELD
				}
			}
			if (methodsIndex < methodsLength) {
				nextMethodDeclaration = methods[methodsIndex];
				if (nextMethodDeclaration.declarationSourceStart < position) {
					position = nextMethodDeclaration.declarationSourceStart;
					nextDeclarationType = 1; // METHOD
				}
			}
			if (membersIndex < membersLength) {
				nextMemberDeclaration = members[membersIndex];
				if (nextMemberDeclaration.declarationSourceStart < position) {
					position = nextMemberDeclaration.declarationSourceStart;
					nextDeclarationType = 2; // MEMBER
				}
			}
			switch (nextDeclarationType) {
				case 0 :
					checkAndAddMultipleFieldDeclaration(fields, fieldsIndex, typeDecl.bodyDeclarations());
					fieldsIndex++;
					break;
				case 1 :
					methodsIndex++;
					if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
						typeDecl.bodyDeclarations().add(convert(nextMethodDeclaration));
					}
					break;
				case 2 :
					membersIndex++;
					typeDecl.bodyDeclarations().add(convert(nextMemberDeclaration));
			}
		}
		// Convert javadoc
		convert(typeDeclaration.javadoc, typeDecl);
	}

	public EnumConstantDeclaration convert(EnumConstant constant) {
		return null;
	}

	public EnhancedForStatement convert(ForeachStatement statement) {
		EnhancedForStatement enhancedForStatement = this.ast.newEnhancedForStatement();
		enhancedForStatement.setParameter(convertToSingleVariableDeclaration(statement.elementVariable));
		enhancedForStatement.setExpression(convert(statement.collection));
		enhancedForStatement.setBody(convert(statement.action));
		int start = statement.sourceStart;
		int end = statement.sourceEnd;
		enhancedForStatement.setSourceRange(start, end - start + 1);
		return enhancedForStatement;
	}
	
	public BodyDeclaration convert(org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration methodDeclaration) {
		if (methodDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration) methodDeclaration);
		}
		return super.convert(methodDeclaration);	
	}
	
	public Annotation convert(org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) annotation);
		} else if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) annotation);
		} else {
			return convert((org.eclipse.jdt.internal.compiler.ast.NormalAnnotation) annotation);
		}
	}

	public AnnotationTypeDeclaration convert(org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration typeDeclaration) {
		checkCanceled();
		AnnotationTypeDeclaration typeDecl = this.ast.newAnnotationTypeDeclaration();
		int modifiers = typeDeclaration.modifiers;
		modifiers &= ~IConstants.AccInterface; // remove AccInterface flags
		modifiers &= CompilerModifiers.AccJustFlag;
		if (modifiers != 0) {
			setModifiers(typeDecl, typeDeclaration);
		}
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);
		
		buildBodyDeclarations(typeDeclaration, typeDecl);
		// The javadoc comment is now got from list store in compilation unit declaration
//		setJavaDocComment(typeDecl);
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	public AnnotationTypeMemberDeclaration convert(org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
		checkCanceled();
		AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration2 = this.ast.newAnnotationTypeMemberDeclaration();
		if ((annotationTypeMemberDeclaration.modifiers & CompilerModifiers.AccJustFlag) != 0) {
			setModifiers(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
		}
		SimpleName methodName = this.ast.newSimpleName(new String(annotationTypeMemberDeclaration.selector));
		int start = annotationTypeMemberDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, annotationTypeMemberDeclaration.sourceEnd);
		methodName.setSourceRange(start, end - start + 1);
		annotationTypeMemberDeclaration2.setName(methodName);
		org.eclipse.jdt.internal.compiler.ast.TypeReference typeReference = annotationTypeMemberDeclaration.returnType;
		if (typeReference != null) {
			Type returnType = convertType(typeReference);
			// get the positions of the right parenthesis
			// TODO need to be updated if extra dimensions are legal
/*			int rightParenthesisPosition = retrieveEndOfRightParenthesisPosition(end, annotationTypeMemberDeclaration.bodyEnd);
			int extraDimensions = retrieveExtraDimension(rightParenthesisPosition, annotationTypeMemberDeclaration.bodyEnd);
			annotationTypeMemberDeclaration2.setExtraDimensions(extraDimensions);
			setTypeForMethodDeclaration(annotationTypeMemberDeclaration2, returnType, extraDimensions);*/
			setTypeForMethodDeclaration(annotationTypeMemberDeclaration2, returnType, 0);
		}
		int declarationSourceStart = annotationTypeMemberDeclaration.declarationSourceStart;
		int declarationSourceEnd = annotationTypeMemberDeclaration.bodyEnd;
		annotationTypeMemberDeclaration2.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
/*		int closingPosition = retrieveRightBraceOrSemiColonPosition(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
		if (closingPosition != -1) {
			int startPosition = annotationTypeMemberDeclaration2.getStartPosition();
			annotationTypeMemberDeclaration2.setSourceRange(startPosition, closingPosition - startPosition);
		}*/
		// The javadoc comment is now got from list store in compilation unit declaration
		convert(annotationTypeMemberDeclaration.javadoc, annotationTypeMemberDeclaration2);
		org.eclipse.jdt.internal.compiler.ast.Expression memberValue = annotationTypeMemberDeclaration.memberValue;
		if (memberValue != null) {
			annotationTypeMemberDeclaration2.setDefault(convert(memberValue));
		}
		if (this.resolveBindings) {
			recordNodes(annotationTypeMemberDeclaration2, annotationTypeMemberDeclaration);
			recordNodes(methodName, annotationTypeMemberDeclaration);
			annotationTypeMemberDeclaration2.resolveBinding();
		}
		return annotationTypeMemberDeclaration2;
	}

	public EnumDeclaration convert(org.eclipse.jdt.internal.compiler.ast.EnumDeclaration enumDeclaration) {
		return null;
	}

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.Annotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Annotation) expression);
		}
		return super.convert(expression);
	}

	public MarkerAnnotation convert(org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation annotation) {
		checkCanceled();
		MarkerAnnotation markerAnnotation = this.ast.newMarkerAnnotation();
		setTypeNameForAnnotation(annotation, markerAnnotation);
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		markerAnnotation.setSourceRange(start, end - start + 1);
		return markerAnnotation;
	}
	
	public MemberValuePair convert(org.eclipse.jdt.internal.compiler.ast.MemberValuePair memberValuePair) {
		checkCanceled();
		MemberValuePair pair = this.ast.newMemberValuePair();
		SimpleName simpleName = this.ast.newSimpleName(new String(memberValuePair.token));
		int start = memberValuePair.sourceStart;
		int end = memberValuePair.sourceEnd;
		simpleName.setSourceRange(start, end - start + 1);
		pair.setName(simpleName);
		pair.setValue(convert(memberValuePair.value));
		start = memberValuePair.sourceStart;
		end = memberValuePair.value.sourceEnd;
		pair.setSourceRange(start, end - start + 1);
		return pair;
	}
	
	public NormalAnnotation convert(org.eclipse.jdt.internal.compiler.ast.NormalAnnotation annotation) {
		checkCanceled();
		NormalAnnotation normalAnnotation = this.ast.newNormalAnnotation();
		setTypeNameForAnnotation(annotation, normalAnnotation);
		org.eclipse.jdt.internal.compiler.ast.MemberValuePair[] memberValuePairs = annotation.memberValuePairs;
		if (memberValuePairs != null) {
			for (int i = 0, max = memberValuePairs.length; i < max; i++) {
				normalAnnotation.values().add(convert(memberValuePairs[i]));
			}
		}
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		normalAnnotation.setSourceRange(start, end - start + 1);
		return normalAnnotation;
	}

	public SingleMemberAnnotation convert(org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation annotation) {
		checkCanceled();
		SingleMemberAnnotation singleMemberAnnotation = this.ast.newSingleMemberAnnotation();
		setTypeNameForAnnotation(annotation, singleMemberAnnotation);
		singleMemberAnnotation.setValue(convert(annotation.memberValue));
		int start = annotation.sourceStart;
		int end = annotation.declarationSourceEnd;
		singleMemberAnnotation.setSourceRange(start, end - start + 1);
		return singleMemberAnnotation;
	}
	
	public Statement convert(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof ForeachStatement) {
			return convert((ForeachStatement) statement);
		}
		return super.convert(statement);
	}

	public ASTNode convert(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		if (typeDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration) typeDeclaration);
		}
		if (typeDeclaration instanceof EnumConstant) {
			return convert((EnumConstant) typeDeclaration);
		}
		if (typeDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.EnumDeclaration) {
			return convert((org.eclipse.jdt.internal.compiler.ast.EnumDeclaration) typeDeclaration);
		}
		checkCanceled();
		TypeDeclaration typeDecl = this.ast.newTypeDeclaration();
		int modifiers = typeDeclaration.modifiers;
		modifiers &= ~IConstants.AccInterface; // remove AccInterface flags
		modifiers &= CompilerModifiers.AccJustFlag;
		if (modifiers != 0) {
			setModifiers(typeDecl, typeDeclaration);
		}
		typeDecl.setInterface(typeDeclaration.isInterface());
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);
		
		// need to set the superclass and super interfaces here since we cannot distinguish them at
		// the type references level.
		if (typeDeclaration.superclass != null) {
			switch(this.ast.apiLevel) {
				case AST.LEVEL_2_0 :
					typeDecl.setSuperclass(convert(typeDeclaration.superclass));
					break;
				case AST.LEVEL_3_0 :
					typeDecl.setSuperclassType(convertType(typeDeclaration.superclass));
					break;
			}
		}
		
		org.eclipse.jdt.internal.compiler.ast.TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			switch(this.ast.apiLevel) {
				case AST.LEVEL_2_0 :
					for (int index = 0, length = superInterfaces.length; index < length; index++) {
						typeDecl.superInterfaces().add(convert(superInterfaces[index]));
					}
					break;
				case AST.LEVEL_3_0 :
					for (int index = 0, length = superInterfaces.length; index < length; index++) {
						typeDecl.superInterfaces().add(convertType(superInterfaces[index]));
					}
			}					
		}
		org.eclipse.jdt.internal.compiler.ast.TypeParameter[] typeParameters = typeDeclaration.typeParameters;
		if (typeParameters != null) {
			switch(this.ast.apiLevel) {
				case AST.LEVEL_2_0 :
					typeDecl.setFlags(ASTNode.MALFORMED);
					break;
				case AST.LEVEL_3_0 :
					for (int index = 0, length = typeParameters.length; index < length; index++) {
						typeDecl.typeParameters().add(convert(typeParameters[index]));
					}
			}
		}
		buildBodyDeclarations(typeDeclaration, typeDecl);
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}
	
	public TypeParameter convert(org.eclipse.jdt.internal.compiler.ast.TypeParameter typeParameter) {
		TypeParameter typeParameter2 = this.ast.newTypeParameter();
		SimpleName simpleName = this.ast.newSimpleName(new String(typeParameter.name));
		int start = typeParameter.sourceStart;
		int end = typeParameter.sourceEnd;
		simpleName.setSourceRange(start, end - start + 1);
		typeParameter2.setName(simpleName);
		final TypeReference superType = typeParameter.type;
		end = typeParameter.declarationSourceEnd;
		if (superType != null) {
			Type type = convertType(superType);
			typeParameter2.typeBounds().add(type);
			end = type.getStartPosition() + type.getLength() - 1;
		}
		TypeReference[] bounds = typeParameter.bounds;
		if (bounds != null) {
			Type type = null;
			for (int index = 0, length = bounds.length; index < length; index++) {
				type = convertType(bounds[index]);
				typeParameter2.typeBounds().add(type);
				end = type.getStartPosition() + type.getLength() - 1;
			}
		}
		start = typeParameter.declarationSourceStart;
		end = retrieveClosingAngleBracketPosition(end);
		typeParameter2.setSourceRange(start, end - start + 1);
		return typeParameter2;
	}
	
	public Name convert(TypeReference typeReference) {
		char[][] typeName = typeReference.getTypeName();
		int length = typeName.length;
		Name name = null;
		if (length > 1) {
			// QualifiedName
			org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference qualifiedTypeReference = (org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference;
			long[] positions = qualifiedTypeReference.sourcePositions;			
			name = setQualifiedNameNameAndSourceRanges(typeName, positions, typeReference);
		} else {
			name = this.ast.newSimpleName(new String(typeName[0]));
			name.setSourceRange(typeReference.sourceStart, typeReference.sourceEnd - typeReference.sourceStart + 1);
		}
		if (this.resolveBindings) {
			recordNodes(name, typeReference);
		}
		return name;
	}

	public PackageDeclaration convertPackage(org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration compilationUnitDeclaration) {
		org.eclipse.jdt.internal.compiler.ast.ImportReference importReference = compilationUnitDeclaration.currentPackage;
		PackageDeclaration packageDeclaration = this.ast.newPackageDeclaration();
		char[][] tokens = importReference.tokens;
		int length = importReference.tokens.length;
		long[] positions = importReference.sourcePositions;
		int start = (int)(positions[0]>>>32);
		int end = (int)(positions[length - 1] & 0xFFFFFFFF);
		Name name = null;
		if (length > 1) {
			name = setQualifiedNameNameAndSourceRanges(tokens, positions, importReference);
		} else {
			name = this.ast.newSimpleName(new String(tokens[0]));
			name.setSourceRange(start, end - start + 1);
		}
		packageDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationEnd - importReference.declarationSourceStart + 1);
		packageDeclaration.setName(name);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = importReference.annotations;
		if (annotations != null && this.ast.apiLevel == AST.LEVEL_3_0) {
			for (int i = 0, max = annotations.length; i < max; i++) {
				packageDeclaration.annotations().add(convert(annotations[i]));
			}
		}
		if (this.resolveBindings) {
			recordNodes(packageDeclaration, importReference);
			recordNodes(name, compilationUnitDeclaration);
		}
		return packageDeclaration;
	}
	
	protected SingleVariableDeclaration convertToSingleVariableDeclaration(LocalDeclaration localDeclaration) {
		SingleVariableDeclaration variableDecl = this.ast.newSingleVariableDeclaration();
		if ((localDeclaration.modifiers & CompilerModifiers.AccJustFlag) != 0) {
			setModifiers(variableDecl, localDeclaration);
		}
		SimpleName name = this.ast.newSimpleName(new String(localDeclaration.name));
		int start = localDeclaration.sourceStart;
		int nameEnd = localDeclaration.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, localDeclaration.type.sourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		Type type = convertType(localDeclaration.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		int rightEnd = Math.max(typeEnd, localDeclaration.declarationSourceEnd);
		/*
		 * There is extra work to do to set the proper type positions
		 * See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		variableDecl.setSourceRange(localDeclaration.declarationSourceStart, rightEnd - localDeclaration.declarationSourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(name, localDeclaration);
			recordNodes(variableDecl, localDeclaration);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}

	public Type convertType(TypeReference typeReference) {
		Type type = null;				
		int sourceStart = -1;
		int length = 0;
		int dimensions = typeReference.dimensions();
		if (typeReference instanceof org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) {
			// this is either an ArrayTypeReference or a SingleTypeReference
			char[] name = ((org.eclipse.jdt.internal.compiler.ast.SingleTypeReference) typeReference).getTypeName()[0];
			sourceStart = typeReference.sourceStart;
			length = typeReference.sourceEnd - typeReference.sourceStart + 1;
			// need to find out if this is an array type of primitive types or not
			if (isPrimitiveType(name)) {
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}					
				type = this.ast.newPrimitiveType(getPrimitiveTypeCode(name));
				type.setSourceRange(sourceStart, end - sourceStart + 1);
			} else if (typeReference instanceof ParameterizedSingleTypeReference) {
				ParameterizedSingleTypeReference parameterizedSingleTypeReference = (ParameterizedSingleTypeReference) typeReference;
				SimpleName simpleName = this.ast.newSimpleName(new String(name));
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				type = this.ast.newParameterizedType(simpleName);
				TypeReference[] typeArguments = parameterizedSingleTypeReference.typeArguments;
				if (typeArguments != null) {
					Type type2 = null;
					for (int i = 0, max = typeArguments.length; i < max; i++) {
						type2 = convertType(typeArguments[i]);
						((ParameterizedType) type).typeArguments().add(type2);
						end = type2.getStartPosition() + type2.getLength() - 1;
					}
					end = retrieveClosingAngleBracketPosition(end + 1);
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				} else {
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				}
				if (this.resolveBindings) {
					this.recordNodes(simpleName, typeReference);
				}
			} else {
				SimpleName simpleName = this.ast.newSimpleName(new String(name));
				// we need to search for the starting position of the first brace in order to set the proper length
				// PR http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
				int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
				if (end == -1) {
					end = sourceStart + length - 1;
				}
				simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
				type = this.ast.newSimpleType(simpleName);
				type.setSourceRange(sourceStart, end - sourceStart + 1);
				if (this.resolveBindings) {
					this.recordNodes(simpleName, typeReference);
				}
			}
			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				type.setSourceRange(sourceStart, length);
				if (this.resolveBindings) {
					// store keys for inner types
					completeRecord((ArrayType) type, typeReference);
				}
			}
		} else {
			char[][] name = ((org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).getTypeName();
			int nameLength = name.length;
			long[] positions = ((org.eclipse.jdt.internal.compiler.ast.QualifiedTypeReference) typeReference).sourcePositions;
			sourceStart = (int)(positions[0]>>>32);
			length = (int)(positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
			Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
			if (typeReference instanceof ParameterizedQualifiedTypeReference) {
//				ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = (ParameterizedQualifiedTypeReference) typeReference;
				type = this.ast.newParameterizedType(qualifiedName);
/*				TypeReference[][] typeArguments = parameterizedQualifiedTypeReference.typeArguments;
				if (typeArguments != null) {
					for (int i = 0, max = typeArguments.length; i < max; i++) {
						((ParameterizedType) type).typeArguments().add(convertType(typeArguments[i]));
					}
				}*/
			} else {
				type = this.ast.newSimpleType(qualifiedName);
				type.setSourceRange(sourceStart, length);
			}

			if (dimensions != 0) {
				type = this.ast.newArrayType(type, dimensions);
				if (this.resolveBindings) {
					completeRecord((ArrayType) type, typeReference);
				}
				int end = retrieveEndOfDimensionsPosition(sourceStart+length, this.compilationUnitSource.length);
				if (end != -1) {
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				} else {
					type.setSourceRange(sourceStart, length);
				}
			}
		}
		if (this.resolveBindings) {
			this.recordNodes(type, typeReference);
		}
		return type;
	}

	/**
	 * @return
	 */
	private Modifier createModifier(ModifierKeyword keyword) {
		Modifier modifier;
		modifier = this.ast.newModifier(keyword);
		int start = this.scanner.getCurrentTokenStartPosition();
		int end = this.scanner.getCurrentTokenEndPosition();
		modifier.setSourceRange(start, end - start + 1);
		return modifier;
	}

	/**
	 * This method is used to retrieve the end position of the block.
	 * @return int the dimension found, -1 if none
	 */
	protected int retrieveClosingAngleBracketPosition(int start) {
		this.scanner.resetTo(start, this.scanner.eofPosition);
		this.scanner.returnOnlyGreater = true;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameGREATER://110
						return this.scanner.currentPosition - 1;
					default:
						return start;
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
		this.scanner.returnOnlyGreater = false;
		return start;
	}
	
	protected void setModifiers(AnnotationTypeDeclaration typeDecl, org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration typeDeclaration) {
		this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
		this.setModifiers(typeDecl, typeDeclaration.annotations);
	}

	protected void setModifiers(AnnotationTypeMemberDeclaration annotationTypeMemberDecl, org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
		this.scanner.resetTo(annotationTypeMemberDeclaration.declarationSourceStart, annotationTypeMemberDeclaration.sourceStart);
		this.setModifiers(annotationTypeMemberDecl, annotationTypeMemberDeclaration.annotations);
	}
	
	/**
	 * @param bodyDeclaration
	 */
	private void setModifiers(BodyDeclaration bodyDeclaration, org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations) {
		try {
			int token;
			int indexInAnnotations = 0;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.scanner.eofPosition);
						}
				}
				if (modifier != null) {
					bodyDeclaration.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}
	
	protected void setModifiers(FieldDeclaration fieldDecl, org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		this.scanner.resetTo(fieldDeclaration.declarationSourceStart, fieldDeclaration.sourceStart);
		this.setModifiers(fieldDecl, fieldDeclaration.annotations);
	}

	/**
	 * @param initializer
	 * @param oldInitializer
	 */
	protected void setModifiers(Initializer initializer, org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer) {
		this.scanner.resetTo(oldInitializer.declarationSourceStart, oldInitializer.bodyStart);
		this.setModifiers(initializer, oldInitializer.annotations);
	}

	protected void setModifiers(MethodDeclaration methodDecl, AbstractMethodDeclaration methodDeclaration) {
		this.scanner.resetTo(methodDeclaration.declarationSourceStart, methodDeclaration.sourceStart);
		this.setModifiers(methodDecl, methodDeclaration.annotations);
	}

	/**
	 * @param variableDecl
	 * @param argument
	 */
	protected void setModifiers(SingleVariableDeclaration variableDecl, Argument argument) {
		this.scanner.resetTo(argument.declarationSourceStart, argument.sourceStart);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = argument.annotations;
		int indexInAnnotations = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.scanner.eofPosition);
						}
				}
				if (modifier != null) {
					variableDecl.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}
	
	/**
	 * @param variableDecl
	 * @param argument
	 */
	protected void setModifiers(SingleVariableDeclaration variableDecl, LocalDeclaration localDeclaration) {
		this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
		int indexInAnnotations = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.scanner.eofPosition);
						}
				}
				if (modifier != null) {
					variableDecl.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}	
	
	protected void setModifiers(TypeDeclaration typeDecl, org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
		this.setModifiers(typeDecl, typeDeclaration.annotations);
	}
	
	/**
	 * @param variableDeclarationExpression
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationExpression variableDeclarationExpression, LocalDeclaration localDeclaration) {
		this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
		int indexInAnnotations = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.scanner.eofPosition);
						}
				}
				if (modifier != null) {
					variableDeclarationExpression.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}

	/**
	 * @param variableDeclarationStatement
	 * @param localDeclaration
	 */
	protected void setModifiers(VariableDeclarationStatement variableDeclarationStatement, LocalDeclaration localDeclaration) {
		this.scanner.resetTo(localDeclaration.declarationSourceStart, localDeclaration.sourceStart);
		org.eclipse.jdt.internal.compiler.ast.Annotation[] annotations = localDeclaration.annotations;
		int indexInAnnotations = 0;
		try {
			int token;
			while ((token = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				IExtendedModifier modifier = null;
				switch(token) {
					case TerminalTokens.TokenNameabstract:
						modifier = createModifier(Modifier.ModifierKeyword.ABSTRACT_KEYWORD);
						break;
					case TerminalTokens.TokenNamepublic:
						modifier = createModifier(Modifier.ModifierKeyword.PUBLIC_KEYWORD);
						break;
					case TerminalTokens.TokenNamestatic:
						modifier = createModifier(Modifier.ModifierKeyword.STATIC_KEYWORD);
						break;
					case TerminalTokens.TokenNameprotected:
						modifier = createModifier(Modifier.ModifierKeyword.PROTECTED_KEYWORD);
						break;
					case TerminalTokens.TokenNameprivate:
						modifier = createModifier(Modifier.ModifierKeyword.PRIVATE_KEYWORD);
						break;
					case TerminalTokens.TokenNamefinal:
						modifier = createModifier(Modifier.ModifierKeyword.FINAL_KEYWORD);
						break;
					case TerminalTokens.TokenNamenative:
						modifier = createModifier(Modifier.ModifierKeyword.NATIVE_KEYWORD);
						break;
					case TerminalTokens.TokenNamesynchronized:
						modifier = createModifier(Modifier.ModifierKeyword.SYNCHRONIZED_KEYWORD);
						break;
					case TerminalTokens.TokenNametransient:
						modifier = createModifier(Modifier.ModifierKeyword.TRANSIENT_KEYWORD);
						break;
					case TerminalTokens.TokenNamevolatile:
						modifier = createModifier(Modifier.ModifierKeyword.VOLATILE_KEYWORD);
						break;
					case TerminalTokens.TokenNamestrictfp:
						modifier = createModifier(Modifier.ModifierKeyword.STRICTFP_KEYWORD);
						break;
					case TerminalTokens.TokenNameAT :
						// we have an annotation
						if (annotations != null && indexInAnnotations < annotations.length) {
							org.eclipse.jdt.internal.compiler.ast.Annotation annotation = annotations[indexInAnnotations++];
							modifier = convert(annotation);
							this.scanner.resetTo(annotation.declarationSourceEnd + 1, this.scanner.eofPosition);
						}
				}
				if (modifier != null) {
					variableDeclarationStatement.modifiers().add(modifier);
				}
			}
		} catch(InvalidInputException e) {
			// ignore
		}
	}
	
	protected void setTypeForMethodDeclaration(AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration, Type type, int extraDimension) {
		annotationTypeMemberDeclaration.setType(type);
	}
	/**
	 * @param annotation
	 * @param singleMemberAnnotation
	 */
	protected void setTypeNameForAnnotation(org.eclipse.jdt.internal.compiler.ast.Annotation compilerAnnotation, Annotation annotation) {
		checkCanceled();
		char[][] typeName = compilerAnnotation.tokens;
		int length = typeName.length;
		Name name = null;
		if (length > 1) {
			// QualifiedName
			name = setQualifiedNameNameAndSourceRanges(typeName, compilerAnnotation.sourcePositions, compilerAnnotation);
		} else {
			name = this.ast.newSimpleName(new String(typeName[0]));
			long position = compilerAnnotation.sourcePositions[0];
			int start = (int) (position >>> 32);
			int end = (int) position;
			name.setSourceRange(start, end - start + 1);
		}
		if (this.resolveBindings) {
			recordNodes(name, compilerAnnotation);
		}
		annotation.setTypeName(name);
	}
}


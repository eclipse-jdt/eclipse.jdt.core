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
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
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
	
	public Annotation convert(org.eclipse.jdt.internal.compiler.ast.Annotation annotation) {
		if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SingleMemberAnnotation) annotation);
		} else if (annotation instanceof org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) {
			return convert((org.eclipse.jdt.internal.compiler.ast.MarkerAnnotation) annotation);
		} else {
			return convert((org.eclipse.jdt.internal.compiler.ast.NormalAnnotation) annotation);
		}
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

	public EnumConstantDeclaration convert(EnumConstant constant) {
		return null;
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

	public BodyDeclaration convert(org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration methodDeclaration) {
		if (methodDeclaration instanceof org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration) methodDeclaration);
		}
		return super.convert(methodDeclaration);	
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
		return super.convert(typeDeclaration);
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

	protected void setModifiers(AnnotationTypeDeclaration typeDecl, org.eclipse.jdt.internal.compiler.ast.AnnotationTypeDeclaration typeDeclaration) {
		this.scanner.resetTo(typeDeclaration.declarationSourceStart, typeDeclaration.sourceStart);
		this.setModifiers(typeDecl, typeDeclaration.annotations);
	}

	protected void setModifiers(AnnotationTypeMemberDeclaration annotationTypeMemberDecl, org.eclipse.jdt.internal.compiler.ast.AnnotationTypeMemberDeclaration annotationTypeMemberDeclaration) {
		this.scanner.resetTo(annotationTypeMemberDeclaration.declarationSourceStart, annotationTypeMemberDeclaration.sourceStart);
		this.setModifiers(annotationTypeMemberDecl, annotationTypeMemberDeclaration.annotations);
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
}


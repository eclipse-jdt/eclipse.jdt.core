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

package org.eclipse.jdt.core.dom;

import java.util.*;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;

/**
 * Internal class for converting internal compiler ASTs into public ASTs.
 */
class ASTConverter {

	private AST ast;
	private char[] compilationUnitSource;
	private Scanner scanner;
	private boolean resolveBindings;
	private Set pendingThisExpressionScopeResolution;
	private Set pendingNameScopeResolution;	
	
	public ASTConverter(Map options, boolean resolveBindings) {
		this.resolveBindings = resolveBindings;
		scanner = new Scanner(
					true /*comment*/,
					false /*whitespace*/,
					false /*nls*/,
					JavaCore.VERSION_1_4.equals(options.get(JavaCore.COMPILER_SOURCE)) ? ClassFileConstants.JDK1_4 : ClassFileConstants.JDK1_3 /*sourceLevel*/, 
					null /*taskTags*/,
					null/*taskPriorities*/);
	}
	
	public void setAST(AST ast) {
		this.ast = ast;
	}
	
	public CompilationUnit convert(CompilationUnitDeclaration unit, char[] source) {
		this.compilationUnitSource = source;
		scanner.setSource(source);
		CompilationUnit compilationUnit = this.ast.newCompilationUnit();
		// handle the package declaration immediately
		// There is no node corresponding to the package declaration
		if (resolveBindings) {
			recordNodes(compilationUnit, unit);
		}
		if (unit.currentPackage != null) {
			PackageDeclaration packageDeclaration = convertPackage(unit);
			compilationUnit.setPackage(packageDeclaration);
		}
		ImportReference[] imports = unit.imports;
		if (imports != null) {
			int importLength = imports.length;
			for (int i = 0; i < importLength; i++) {
				compilationUnit.imports().add(convertImport(imports[i]));
			}
		}
		org.eclipse.jdt.internal.compiler.ast.TypeDeclaration[] types = unit.types;
		if (types != null) {
			int typesLength = types.length;
			for (int i = 0; i < typesLength; i++) {
				compilationUnit.types().add(convert(types[i]));
			}
		}
		compilationUnit.setSourceRange(unit.sourceStart, unit.sourceEnd - unit.sourceStart  + 1);
		
		if (unit.compilationResult.problemCount != 0) {
			propagateErrors(compilationUnit, unit.compilationResult.problems, unit.compilationResult.problemCount);
		}
		if (resolveBindings) {
			lookupForScopes();
		}
		return compilationUnit;
	}
	
	public PackageDeclaration convertPackage(CompilationUnitDeclaration compilationUnitDeclaration) {
		ImportReference importReference = compilationUnitDeclaration.currentPackage;
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
		if (resolveBindings) {
			recordNodes(packageDeclaration, importReference);
			recordNodes(name, compilationUnitDeclaration);
		}
		return packageDeclaration;
	}
	
	public ImportDeclaration convertImport(ImportReference importReference) {
		ImportDeclaration importDeclaration = this.ast.newImportDeclaration();
		boolean onDemand = importReference.onDemand;
		char[][] tokens = importReference.tokens;
		int length = importReference.tokens.length;
		long[] positions = importReference.sourcePositions;
		Name name = null;
		if (length > 1) {
			name = setQualifiedNameNameAndSourceRanges(tokens, positions, importReference);
		} else {
			name = this.ast.newSimpleName(new String(tokens[0]));
			int start = (int)(positions[0]>>>32);
			int end = (int)(positions[0] & 0xFFFFFFFF);
			name.setSourceRange(start, end - start + 1);
		}
		importDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationEnd - importReference.declarationSourceStart + 1);
		importDeclaration.setName(name);
		importDeclaration.setOnDemand(onDemand);
		if (resolveBindings) {
			recordNodes(importDeclaration, importReference);
		}
		return importDeclaration;
	}

	public TypeDeclaration convert(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		TypeDeclaration typeDecl = this.ast.newTypeDeclaration();
		int modifiers = typeDeclaration.modifiers;
		modifiers &= ~IConstants.AccInterface; // remove AccInterface flags
		modifiers &= CompilerModifiers.AccJustFlag;
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built nodes with invalid modifiers.
		 */
		try {
			// AccJustFlag doesn't flush Modifier.TRANSIENT or Modifier.VOLATILE.
			// Therefore we need to handle these cases
			typeDecl.setModifiers(modifiers);
		} catch(IllegalArgumentException e) {
			int legalModifiers =
				Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
				| Modifier.STATIC | Modifier.FINAL | Modifier.ABSTRACT
				| Modifier.STRICTFP;
			typeDecl.setModifiers(modifiers & legalModifiers);
			typeDecl.setFlags(ASTNode.MALFORMED);
		}
		typeDecl.setInterface(typeDeclaration.isInterface());
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);
		
		// need to set the superclass and super interfaces here since we cannot distinguish them at
		// the type references level.
		if (typeDeclaration.superclass != null) {
			typeDecl.setSuperclass(convert(typeDeclaration.superclass));
		}
		
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			for (int index = 0, length = superInterfaces.length; index < length; index++) {
				typeDecl.superInterfaces().add(convert(superInterfaces[index]));
			}
		}
		
		buildBodyDeclarations(typeDeclaration, typeDecl);
		setJavaDocComment(typeDecl);
		if (resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	private void buildBodyDeclarations(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration, TypeDeclaration typeDecl) {
		// add body declaration in the lexical order
		MemberTypeDeclaration[] members = typeDeclaration.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = typeDeclaration.fields;
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		
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
			AbstractMethodDeclaration nextMethodDeclaration = null;
			MemberTypeDeclaration nextMemberDeclaration = null;
		
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
	}
	
	private void checkAndAddMultipleFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields, int index, List bodyDeclarations) {
		if (fields[index] instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
			org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer = (org.eclipse.jdt.internal.compiler.ast.Initializer) fields[index];
			Initializer initializer = this.ast.newInitializer();
			initializer.setBody(convert(oldInitializer.block));
			initializer.setModifiers(oldInitializer.modifiers);
			initializer.setSourceRange(oldInitializer.declarationSourceStart, oldInitializer.sourceEnd - oldInitializer.declarationSourceStart + 1);
			setJavaDocComment(initializer);
			bodyDeclarations.add(initializer);
			return;
		}
		if (index > 0 && fields[index - 1].declarationSourceStart == fields[index].declarationSourceStart) {
			// we have a multiple field declaration
			// We retrieve the existing fieldDeclaration to add the new VariableDeclarationFragment
			FieldDeclaration fieldDeclaration = (FieldDeclaration) bodyDeclarations.get(bodyDeclarations.size() - 1);
			fieldDeclaration.fragments().add(convertToVariableDeclarationFragment(fields[index]));
		} else {
			// we can create a new FieldDeclaration
			bodyDeclarations.add(convertToFieldDeclaration(fields[index]));
		}
	}
	
	private void checkAndAddMultipleLocalDeclaration(org.eclipse.jdt.internal.compiler.ast.Statement[] stmts, int index, List blockStatements) {
		if (index > 0
		    && stmts[index - 1] instanceof LocalDeclaration) {
		    	LocalDeclaration local1 = (LocalDeclaration) stmts[index - 1];
		    	LocalDeclaration local2 = (LocalDeclaration) stmts[index];
			   if (local1.declarationSourceStart == local2.declarationSourceStart) {
					// we have a multiple local declarations
					// We retrieve the existing VariableDeclarationStatement to add the new VariableDeclarationFragment
					VariableDeclarationStatement variableDeclarationStatement = (VariableDeclarationStatement) blockStatements.get(blockStatements.size() - 1);
					variableDeclarationStatement.fragments().add(convertToVariableDeclarationFragment((LocalDeclaration)stmts[index]));
			   } else {
					// we can create a new FieldDeclaration
					blockStatements.add(convertToVariableDeclarationStatement((LocalDeclaration)stmts[index]));
			   }
		} else {
			// we can create a new FieldDeclaration
			blockStatements.add(convertToVariableDeclarationStatement((LocalDeclaration)stmts[index]));
		}
	}

	public Name convert(TypeReference typeReference) {
		char[][] typeName = typeReference.getTypeName();
		int length = typeName.length;
		Name name = null;
		if (length > 1) {
			// QualifiedName
			QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) typeReference;
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
	
	public SimpleName convert(SingleNameReference nameReference) {
		SimpleName name = this.ast.newSimpleName(new String(nameReference.token));		
		if (this.resolveBindings) {
			recordNodes(name, nameReference);
		}
		name.setSourceRange(nameReference.sourceStart, nameReference.sourceEnd - nameReference.sourceStart + 1);
		return name;
	}

	public Name convert(QualifiedNameReference nameReference) {
		char[][] typeName = nameReference.tokens;
		long[] positions = scanAllIdentifiersPositions(nameReference.sourceStart, nameReference.sourceEnd, typeName.length);
		return setQualifiedNameNameAndSourceRanges(typeName, positions, nameReference);
	}

	private QualifiedName setQualifiedNameNameAndSourceRanges(char[][] typeName, long[] positions, AstNode node) {
		int length = typeName.length;
		SimpleName firstToken = this.ast.newSimpleName(new String(typeName[0]));
		firstToken.index = length - 1;
		int start0 = (int)(positions[0]>>>32);
		int start = start0;
		int end = (int)(positions[0] & 0xFFFFFFFF);
		firstToken.setSourceRange(start, end - start + 1);
		SimpleName secondToken = this.ast.newSimpleName(new String(typeName[1]));
		secondToken.index = length - 2;
		start = (int)(positions[1]>>>32);
		end = (int)(positions[1] & 0xFFFFFFFF);
		secondToken.setSourceRange(start, end - start + 1);
		QualifiedName qualifiedName = this.ast.newQualifiedName(firstToken, secondToken);
		if (this.resolveBindings) {
			recordNodes(qualifiedName, node);
			recordPendingNameScopeResolution(qualifiedName);
			recordNodes(firstToken, node);
			recordNodes(secondToken, node);
			recordPendingNameScopeResolution(firstToken);
			recordPendingNameScopeResolution(secondToken);
		}
		qualifiedName.index = length - 2;
		qualifiedName.setSourceRange(start0, end - start0 + 1);
		SimpleName newPart = null;
		for (int i = 2; i < length; i++) {
			newPart = this.ast.newSimpleName(new String(typeName[i]));
			newPart.index = length - i - 1;
			start = (int)(positions[i]>>>32);
			end = (int)(positions[i] & 0xFFFFFFFF);
			newPart.setSourceRange(start,  end - start + 1);
			qualifiedName = this.ast.newQualifiedName(qualifiedName, newPart);
			qualifiedName.index = newPart.index;
			qualifiedName.setSourceRange(start0, end - start0 + 1);
			if (this.resolveBindings) {
				recordNodes(qualifiedName, node);
				recordNodes(newPart, node);				
				recordPendingNameScopeResolution(qualifiedName);
				recordPendingNameScopeResolution(newPart);
			}
		}
		QualifiedName name = qualifiedName;
		if (this.resolveBindings) {
			recordNodes(name, node);
			recordPendingNameScopeResolution(name);
		}
		return name;
	}
	
	public Expression convert(ThisReference reference) {
		if (reference.isImplicitThis()) {
			// There is no source associated with an implicit this
			return null;
		} else if (reference instanceof QualifiedSuperReference) {
			return convert((QualifiedSuperReference) reference);
		} else if (reference instanceof QualifiedThisReference) {
			return convert((QualifiedThisReference) reference);
		}  else {
			ThisExpression thisExpression = this.ast.newThisExpression();
			thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
			if (this.resolveBindings) {
				recordNodes(thisExpression, reference);
				recordPendingThisExpressionScopeResolution(thisExpression);
			}
			return thisExpression;
		}
	}

	public ThisExpression convert(QualifiedThisReference reference) {
		ThisExpression thisExpression = this.ast.newThisExpression();
		thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		thisExpression.setQualifier(convert(reference.qualification));
		if (this.resolveBindings) {
			recordNodes(thisExpression, reference);
			recordPendingThisExpressionScopeResolution(thisExpression);
		}
		return thisExpression;
	}

	public Name convert(QualifiedSuperReference reference) {
		return convert(reference.qualification);
	}

	public ArrayAccess convert(ArrayReference reference) {
		ArrayAccess arrayAccess = this.ast.newArrayAccess();
		if (this.resolveBindings) {
			recordNodes(arrayAccess, reference);
		}
		arrayAccess.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		arrayAccess.setArray(convert(reference.receiver));
		arrayAccess.setIndex(convert(reference.position));
		return arrayAccess;
	}
	
	public Expression convert(FieldReference reference) {
		if (reference.receiver.isSuper()) {
			SuperFieldAccess superFieldAccess = this.ast.newSuperFieldAccess();
			if (this.resolveBindings) {
				recordNodes(superFieldAccess, reference);
			}
			if (reference.receiver instanceof QualifiedSuperReference) {
				Name qualifier = convert((QualifiedSuperReference) reference.receiver);
				superFieldAccess.setQualifier(qualifier);
				if (this.resolveBindings) {
					recordNodes(qualifier, reference.receiver);
				}
			}
			SimpleName simpleName = this.ast.newSimpleName(new String(reference.token)); 
			int sourceStart = (int)(reference.nameSourcePosition>>>32);
			int length = (int)(reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			superFieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			superFieldAccess.setSourceRange(reference.receiver.sourceStart, reference.sourceEnd - reference.receiver.sourceStart + 1);
			return superFieldAccess;
		} else {
			FieldAccess fieldAccess = this.ast.newFieldAccess();
			if (this.resolveBindings) {
				recordNodes(fieldAccess, reference);
			}
			Expression receiver = convert(reference.receiver);
			fieldAccess.setExpression(receiver);
			SimpleName simpleName = this.ast.newSimpleName(new String(reference.token)); 
			int sourceStart = (int)(reference.nameSourcePosition>>>32);
			int length = (int)(reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			fieldAccess.setName(simpleName);
			if (this.resolveBindings) {
				recordNodes(simpleName, reference);
			}
			fieldAccess.setSourceRange(receiver.getStartPosition(), reference.sourceEnd - receiver.getStartPosition() + 1);
			return fieldAccess;
		}
	}
	
	public Expression convert(Reference reference) {
		if (reference instanceof NameReference) {
			return convert((NameReference) reference);
		}
		if (reference instanceof ThisReference) {
			return convert((ThisReference) reference);
		}
		if (reference instanceof ArrayReference) {
			return convert((ArrayReference) reference);
		}
		if (reference instanceof FieldReference) {
			return convert((FieldReference) reference);
		}
		throw new IllegalArgumentException("Not yet implemented: convert(" + reference.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}
						
	public Name convert(NameReference reference) {
		if (reference instanceof QualifiedNameReference) {
			return convert((QualifiedNameReference) reference);
		}
		if (reference instanceof SingleNameReference) {
			return convert((SingleNameReference) reference);
		}
		throw new IllegalArgumentException("Not yet implemented: convert(" + reference.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}	

	public TypeDeclaration convert(MemberTypeDeclaration typeDeclaration) {
		TypeDeclaration typeDecl = this.ast.newTypeDeclaration();
		int modifiers = typeDeclaration.modifiers;
		modifiers &= ~IConstants.AccInterface; // remove AccInterface flags
		modifiers &= CompilerModifiers.AccJustFlag;
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built nodes with invalid modifiers.
		 */
		try {
			// AccJustFlag doesn't flush Modifier.TRANSIENT or Modifier.VOLATILE.
			// Therefore we need to handle these cases
			typeDecl.setModifiers(modifiers);
		} catch(IllegalArgumentException e) {
			int legalModifiers =
				Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
				| Modifier.STATIC | Modifier.FINAL | Modifier.ABSTRACT
				| Modifier.STRICTFP;
			typeDecl.setModifiers(modifiers & legalModifiers);
			typeDecl.setFlags(ASTNode.MALFORMED);
		}
		typeDecl.setInterface(typeDeclaration.isInterface());
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(typeName);
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.bodyEnd - typeDeclaration.declarationSourceStart + 1);
		
		// need to set the superclass and super interfaces here since we cannot distinguish them at
		// the type references level.
		if (typeDeclaration.superclass != null) {
			typeDecl.setSuperclass(convert(typeDeclaration.superclass));
		}
		
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			for (int index = 0, length = superInterfaces.length; index < length; index++) {
				typeDecl.superInterfaces().add(convert(superInterfaces[index]));
			}
		}
		buildBodyDeclarations(typeDeclaration, typeDecl);
		setJavaDocComment(typeDecl);
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
			recordNodes(typeName, typeDeclaration);
			typeDecl.resolveBinding();
		}
		return typeDecl;
	}

	private void completeRecord(ArrayType arrayType, AstNode astNode) {
		ArrayType array = arrayType;
		int dimensions = array.getDimensions();
		for (int i = 0; i < dimensions; i++) {
			Type componentType = array.getComponentType();
			this.recordNodes(componentType, astNode);
			if (componentType.isArrayType()) {
				array = (ArrayType) componentType;
			}
		}
	}
	
	public Type convertType(TypeReference typeReference) {
		Type type = null;				
		int sourceStart = -1;
		int length = 0;
		int dimensions = typeReference.dimensions();
		if (typeReference instanceof SingleTypeReference) {
			// this is either an ArrayTypeReference or a SingleTypeReference
			char[] name = ((SingleTypeReference) typeReference).getTypeName()[0];
			sourceStart = typeReference.sourceStart;
			length = typeReference.sourceEnd - typeReference.sourceStart + 1;
			if (dimensions != 0) {
				// need to find out if this is an array type of primitive types or not
				if (isPrimitiveType(name)) {
					int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
					if (end == -1) {
						end = sourceStart + length - 1;
					}					
					PrimitiveType primitiveType = this.ast.newPrimitiveType(getPrimitiveTypeCode(name));
					primitiveType.setSourceRange(sourceStart, end - sourceStart + 1);
					type = this.ast.newArrayType(primitiveType, dimensions);
					if (resolveBindings) {
						// store keys for inner types
						completeRecord((ArrayType) type, typeReference);
					}
					type.setSourceRange(sourceStart, length);
				} else {
					SimpleName simpleName = this.ast.newSimpleName(new String(name));
					// we need to search for the starting position of the first brace in order to set the proper length
					// PR http://dev.eclipse.org/bugs/show_bug.cgi?id=10759
					int end = retrieveEndOfElementTypeNamePosition(sourceStart, sourceStart + length);
					if (end == -1) {
						end = sourceStart + length - 1;
					}
					simpleName.setSourceRange(sourceStart, end - sourceStart + 1);
					SimpleType simpleType = this.ast.newSimpleType(simpleName);
					simpleType.setSourceRange(sourceStart, end - sourceStart + 1);
					type = this.ast.newArrayType(simpleType, dimensions);
					type.setSourceRange(sourceStart, length);
					if (this.resolveBindings) {
						completeRecord((ArrayType) type, typeReference);
						this.recordNodes(simpleName, typeReference);
					}
				}
			} else {
				if (isPrimitiveType(name)) {
					type = this.ast.newPrimitiveType(getPrimitiveTypeCode(name));
					type.setSourceRange(sourceStart, length);
				} else {
					SimpleName simpleName = this.ast.newSimpleName(new String(name));
					simpleName.setSourceRange(sourceStart, length);
					type = this.ast.newSimpleType(simpleName);
					type.setSourceRange(sourceStart, length);
					if (this.resolveBindings) {
						this.recordNodes(simpleName, typeReference);
					}
				}
			}
		} else {
			char[][] name = ((QualifiedTypeReference) typeReference).getTypeName();
			int nameLength = name.length;
			long[] positions = ((QualifiedTypeReference) typeReference).sourcePositions;
			sourceStart = (int)(positions[0]>>>32);
			length = (int)(positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
			Name qualifiedName = this.setQualifiedNameNameAndSourceRanges(name, positions, typeReference);
			if (dimensions != 0) {
				// need to find out if this is an array type of primitive types or not
				SimpleType simpleType = this.ast.newSimpleType(qualifiedName);
				simpleType.setSourceRange(sourceStart, length);
				type = this.ast.newArrayType(simpleType, dimensions);
				if (this.resolveBindings) {
					completeRecord((ArrayType) type, typeReference);
				}				
				int end = retrieveEndOfDimensionsPosition(sourceStart+length, this.compilationUnitSource.length);
				if (end != -1) {
					type.setSourceRange(sourceStart, end - sourceStart + 1);
				} else {
					type.setSourceRange(sourceStart, length);
				}
			} else {
				type = this.ast.newSimpleType(qualifiedName);
				type.setSourceRange(sourceStart, length);
			}
		}
		if (this.resolveBindings) {
			this.recordNodes(type, typeReference);
		}
		return type;
	}
		
	public MethodDeclaration convert(AbstractMethodDeclaration methodDeclaration) {
		MethodDeclaration methodDecl = this.ast.newMethodDeclaration();
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built nodes with invalid modifiers.
		 */
		try {
			// if Modifier.VOLATILE is set, setModifiers is not allowed, but the code has no syntax error.
			methodDecl.setModifiers(methodDeclaration.modifiers & org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers.AccJustFlag);
		} catch(IllegalArgumentException e) {
			int legalModifiers =
				Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
				| Modifier.STATIC | Modifier.FINAL | Modifier.SYNCHRONIZED
				| Modifier.NATIVE | Modifier.ABSTRACT | Modifier.STRICTFP;
			methodDecl.setModifiers(methodDeclaration.modifiers & legalModifiers);
			methodDecl.setFlags(ASTNode.MALFORMED);
		}
		boolean isConstructor = methodDeclaration.isConstructor();
		methodDecl.setConstructor(isConstructor);
		SimpleName methodName = this.ast.newSimpleName(new String(methodDeclaration.selector));
		int start = methodDeclaration.sourceStart;
		int end = retrieveIdentifierEndPosition(start, methodDeclaration.sourceEnd);
		methodName.setSourceRange(start, end - start + 1);
		methodDecl.setName(methodName);
		TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
		if (thrownExceptions != null) {
			int thrownExceptionsLength = thrownExceptions.length;
			for (int i = 0; i < thrownExceptionsLength; i++) {
				methodDecl.thrownExceptions().add(convert(thrownExceptions[i]));
			}
		}
		Argument[] parameters = methodDeclaration.arguments;
		if (parameters != null) {
			int parametersLength = parameters.length;
			for (int i = 0; i < parametersLength; i++) {
				methodDecl.parameters().add(convert(parameters[i]));
			}
		}
		ExplicitConstructorCall explicitConstructorCall = null;
		if (isConstructor) {
			// set the return type to VOID
			PrimitiveType returnType = this.ast.newPrimitiveType(PrimitiveType.VOID);
			returnType.setSourceRange(methodDeclaration.sourceStart, 0);
			methodDecl.setReturnType(returnType);
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			explicitConstructorCall = constructorDeclaration.constructorCall;
		} else {
			org.eclipse.jdt.internal.compiler.ast.MethodDeclaration method = (org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) methodDeclaration;
			TypeReference typeReference = method.returnType;
			if (typeReference != null) {
				Type returnType = convertType(typeReference);
				// get the positions of the right parenthesis
				int rightParenthesisPosition = retrieveEndOfRightParenthesisPosition(end, method.bodyEnd);
				int extraDimensions = retrieveExtraDimension(rightParenthesisPosition, method.bodyEnd);
				methodDecl.setExtraDimensions(extraDimensions);
				setTypeForMethodDeclaration(methodDecl, returnType, extraDimensions);
			}
		}
		int declarationSourceStart = methodDeclaration.declarationSourceStart;
		int declarationSourceEnd = methodDeclaration.bodyEnd;
		methodDecl.setSourceRange(declarationSourceStart, declarationSourceEnd - declarationSourceStart + 1);
		int closingPosition = retrieveRightBraceOrSemiColonPosition(methodDecl, methodDeclaration);
		if (closingPosition != -1) {
			int startPosition = methodDecl.getStartPosition();
			methodDecl.setSourceRange(startPosition, closingPosition - startPosition);

			org.eclipse.jdt.internal.compiler.ast.Statement[] statements = methodDeclaration.statements;
			
			if (statements != null || explicitConstructorCall != null) {
				start = retrieveStartBlockPosition(methodDeclaration.sourceStart, declarationSourceEnd);
				end = retrieveEndBlockPosition(methodDeclaration.sourceStart, methodDeclaration.declarationSourceEnd);
				if (start != -1 && end != -1) {
					Block block = this.ast.newBlock();
					block.setSourceRange(start, end - start + 1);
					if (explicitConstructorCall != null && explicitConstructorCall.accessMode != ExplicitConstructorCall.ImplicitSuper) {
						block.statements().add(convert(explicitConstructorCall));
					}
					int statementsLength = statements == null ? 0 : statements.length;
					for (int i = 0; i < statementsLength; i++) {
						if (statements[i] instanceof LocalDeclaration) {
							checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
						} else {
							block.statements().add(convert(statements[i]));
						}
					}
					methodDecl.setBody(block);
				}
			} else if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
				start = retrieveStartBlockPosition(methodDeclaration.sourceStart, declarationSourceEnd);
				end = retrieveEndBlockPosition(methodDeclaration.sourceStart, methodDeclaration.declarationSourceEnd);
				if (start != -1 && end != -1) {
					/*
					 * start or end can be equal to -1 if we have an interface's method.
					 */
					Block block = this.ast.newBlock();
					block.setSourceRange(start, end - start + 1);
					methodDecl.setBody(block);
				}
			}
		} else {
			// syntax error in this method declaration
			if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
				start = retrieveStartBlockPosition(methodDeclaration.sourceStart, declarationSourceEnd);
				end = methodDeclaration.bodyEnd;
				// try to get the best end position
				IProblem[] problems = methodDeclaration.compilationResult().problems;
				if (problems != null) {
					for (int i = 0, max = problems.length; i < max; i++) {
						IProblem currentProblem = problems[i];
						if (currentProblem.getSourceStart() == start && currentProblem.getID() == IProblem.ParsingErrorInsertToComplete) {
							end = currentProblem.getSourceEnd();
							break;
						}
					}
				}
				int startPosition = methodDecl.getStartPosition();
				methodDecl.setSourceRange(startPosition, end - startPosition + 1);
				if (start != -1 && end != -1) {
					/*
					 * start or end can be equal to -1 if we have an interface's method.
					 */
					Block block = this.ast.newBlock();
					block.setSourceRange(start, end - start + 1);
					methodDecl.setBody(block);
				}
			}			
		}
		
		setJavaDocComment(methodDecl);
		if (this.resolveBindings) {
			recordNodes(methodDecl, methodDeclaration);
			recordNodes(methodName, methodDeclaration);
			methodDecl.resolveBinding();
		}
		return methodDecl;
	}	

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		if ((expression.bits & AstNode.ParenthesizedMASK) != 0) {
			return convertToParenthesizedExpression(expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.CastExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CastExpression) expression);
		}
		// switch between all types of expression
		if (expression instanceof ArrayAllocationExpression) {
			return convert((ArrayAllocationExpression) expression);
		}
		if (expression instanceof QualifiedAllocationExpression) {
			return convert((QualifiedAllocationExpression) expression);
		}
		if (expression instanceof AllocationExpression) {
			return convert((AllocationExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ArrayInitializer) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.PrefixExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.PrefixExpression) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.PostfixExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.PostfixExpression) expression);
		}
		if (expression instanceof CompoundAssignment) {
			return convert((CompoundAssignment) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.Assignment) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Assignment) expression);
		}
		if (expression instanceof ClassLiteralAccess) {
			return convert((ClassLiteralAccess) expression);
		}
		if (expression instanceof FalseLiteral) {
			return convert((FalseLiteral) expression);
		}
		if (expression instanceof TrueLiteral) {
			return convert((TrueLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.NullLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.NullLiteral) expression);
		}
		if (expression instanceof CharLiteral) {
			return convert((CharLiteral) expression);
		}
		if (expression instanceof DoubleLiteral) {
			return convert((DoubleLiteral) expression);
		}
		if (expression instanceof FloatLiteral) {
			return convert((FloatLiteral) expression);
		}
		if (expression instanceof IntLiteralMinValue) {
			return convert((IntLiteralMinValue) expression);
		}
		if (expression instanceof IntLiteral) {
			return convert((IntLiteral) expression);
		}
		if (expression instanceof LongLiteralMinValue) {
			return convert((LongLiteralMinValue) expression);
		}				
		if (expression instanceof LongLiteral) {
			return convert((LongLiteral) expression);
		}
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ExtendedStringLiteral) {
			return convert((ExtendedStringLiteral) expression);
		}	
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.StringLiteral) {
			return convert((org.eclipse.jdt.internal.compiler.ast.StringLiteral) expression);
		}				
		if (expression instanceof AND_AND_Expression) {
			return convert((AND_AND_Expression) expression);
		}				
		if (expression instanceof OR_OR_Expression) {
			return convert((OR_OR_Expression) expression);
		}				
		if (expression instanceof EqualExpression) {
			return convert((EqualExpression) expression);
		}				
		if (expression instanceof BinaryExpression) {
			return convert((BinaryExpression) expression);
		}				
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression) expression);
		}				
		if (expression instanceof UnaryExpression) {
			return convert((UnaryExpression) expression);
		}				
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ConditionalExpression) expression);
		}				
		if (expression instanceof MessageSend) {
			return convert((MessageSend) expression);
		}				
		if (expression instanceof Reference) {
			return convert((Reference) expression);
		}
		if (expression instanceof TypeReference) {
			return convert((TypeReference) expression);
		}				
		throw new IllegalArgumentException("Not yet implemented: convert(" + expression.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}

	public ParenthesizedExpression convertToParenthesizedExpression(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		ParenthesizedExpression parenthesizedExpression = this.ast.newParenthesizedExpression();
		if (this.resolveBindings) {
			recordNodes(parenthesizedExpression, expression);
		}
		parenthesizedExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		adjustSourcePositionsForParent(expression);
		removeExtraBlanks(expression);
		// decrement the number of parenthesis
		int numberOfParenthesis = (expression.bits & AstNode.ParenthesizedMASK) >> AstNode.ParenthesizedSHIFT;
		expression.bits &= ~AstNode.ParenthesizedMASK;
		expression.bits |= (numberOfParenthesis - 1) << AstNode.ParenthesizedSHIFT;
		parenthesizedExpression.setExpression(convert(expression));
		return parenthesizedExpression;
	}
	
	public ClassInstanceCreation convert(AllocationExpression expression) {
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		if (this.resolveBindings) {
			recordNodes(classInstanceCreation, expression);
		}
		classInstanceCreation.setName(convert(expression.type));
		classInstanceCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				classInstanceCreation.arguments().add(convert(arguments[i]));
			}
		}
		removeTrailingCommentFromExpressionEndingWithAParen(classInstanceCreation);
		return classInstanceCreation;
	}
	
	public ClassInstanceCreation convert(AnonymousLocalTypeDeclaration expression) {
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		classInstanceCreation.setName(convert(expression.allocation.type));
		if (expression.allocation.enclosingInstance != null) {
			classInstanceCreation.setExpression(convert(expression.allocation.enclosingInstance));
		}
		int declarationSourceStart = expression.allocation.sourceStart;
		classInstanceCreation.setSourceRange(declarationSourceStart, expression.bodyEnd - declarationSourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.allocation.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				classInstanceCreation.arguments().add(convert(arguments[i]));
			}
		}
		AnonymousClassDeclaration anonymousClassDeclaration = this.ast.newAnonymousClassDeclaration();
		int start = retrieveStartBlockPosition(declarationSourceStart, expression.bodyEnd);
		anonymousClassDeclaration.setSourceRange(start, expression.bodyEnd - start + 1);
		classInstanceCreation.setAnonymousClassDeclaration(anonymousClassDeclaration);
		buildBodyDeclarations(expression, anonymousClassDeclaration);
		if (this.resolveBindings) {
			recordNodes(classInstanceCreation, expression);
			recordNodes(anonymousClassDeclaration, expression);
			anonymousClassDeclaration.resolveBinding();
		}
		return classInstanceCreation;
	}

	private void buildBodyDeclarations(AnonymousLocalTypeDeclaration expression, AnonymousClassDeclaration anonymousClassDeclaration) {
		// add body declaration in the lexical order
		MemberTypeDeclaration[] members = expression.memberTypes;
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = expression.fields;
		AbstractMethodDeclaration[] methods = expression.methods;
		
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
			AbstractMethodDeclaration nextMethodDeclaration = null;
			MemberTypeDeclaration nextMemberDeclaration = null;
		
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
					checkAndAddMultipleFieldDeclaration(fields, fieldsIndex, anonymousClassDeclaration.bodyDeclarations());
					fieldsIndex++;
					break;
				case 1 :
					methodsIndex++;
					if (!nextMethodDeclaration.isDefaultConstructor() && !nextMethodDeclaration.isClinit()) {
						anonymousClassDeclaration.bodyDeclarations().add(convert(nextMethodDeclaration));
					}
					break;
				case 2 :
					membersIndex++;
					anonymousClassDeclaration.bodyDeclarations().add(convert(nextMemberDeclaration));
			}
		}
	}

	public ArrayCreation convert(ArrayAllocationExpression expression) {
		ArrayCreation arrayCreation = this.ast.newArrayCreation();
		if (this.resolveBindings) {
			recordNodes(arrayCreation, expression);
		}
		arrayCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] dimensions = expression.dimensions;
		
		int dimensionsLength = dimensions.length;
		for (int i = 0; i < dimensionsLength; i++) {
			if (dimensions[i] != null) {
				Expression dimension = convert(dimensions[i]);
				if (this.resolveBindings) {
					recordNodes(dimension, dimensions[i]);
				}
				arrayCreation.dimensions().add(dimension);
			}
		}
		Type type = convertType(expression.type);
		if (this.resolveBindings) {
			recordNodes(type, expression.type);
		}		
		ArrayType arrayType = null;
		if (type.isArrayType()) {
			arrayType = (ArrayType) type;
		} else {
			arrayType = this.ast.newArrayType(type, dimensionsLength);
			if (this.resolveBindings) {
				completeRecord(arrayType, expression);
			}			
			int start = type.getStartPosition();
			int end = type.getStartPosition() + type.getLength();
			int previousSearchStart = end;
			ArrayType componentType = (ArrayType) type.getParent();
			for (int i = 0; i < dimensionsLength; i++) {
				previousSearchStart = retrieveRightBracketPosition(previousSearchStart + 1, this.compilationUnitSource.length);
				componentType.setSourceRange(start, previousSearchStart - start + 1);
				componentType = (ArrayType) componentType.getParent();
			}
		}
		arrayCreation.setType(arrayType);
		if (this.resolveBindings) {
			recordNodes(arrayType, expression);
		}	
		if (expression.initializer != null) {
			arrayCreation.setInitializer(convert(expression.initializer));
		}
		return arrayCreation;
	}

	public SingleVariableDeclaration convert(Argument argument) {
		SingleVariableDeclaration variableDecl = this.ast.newSingleVariableDeclaration();
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built nodes with invalid modifiers.
		 */
		try {
			variableDecl.setModifiers(argument.modifiers);
		} catch(IllegalArgumentException e) {
			int legalModifiers =
				Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
				| Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE
				| Modifier.TRANSIENT;
			variableDecl.setModifiers(argument.modifiers & legalModifiers);
			variableDecl.setFlags(ASTNode.MALFORMED);
		}
		
		variableDecl.setModifiers(argument.modifiers);
		SimpleName name = this.ast.newSimpleName(new String(argument.name));
		int start = argument.sourceStart;
		int nameEnd = argument.sourceEnd;
		name.setSourceRange(start, nameEnd - start + 1);
		variableDecl.setName(name);
		final int extraDimensions = retrieveExtraDimension(nameEnd + 1, argument.type.sourceEnd);
		variableDecl.setExtraDimensions(extraDimensions);
		Type type = convertType(argument.type);
		int typeEnd = type.getStartPosition() + type.getLength() - 1;
		int rightEnd = Math.max(typeEnd, argument.declarationSourceEnd);
		/*
		 * There is extra work to do to set the proper type positions
		 * See PR http://bugs.eclipse.org/bugs/show_bug.cgi?id=23284
		 */
		setTypeForSingleVariableDeclaration(variableDecl, type, extraDimensions);
		variableDecl.setSourceRange(argument.declarationSourceStart, rightEnd - argument.declarationSourceStart + 1);
		if (this.resolveBindings) {
			recordNodes(name, argument);
			recordNodes(variableDecl, argument);
			variableDecl.resolveBinding();
		}
		return variableDecl;
	}

	public ArrayInitializer convert(org.eclipse.jdt.internal.compiler.ast.ArrayInitializer expression) {
		ArrayInitializer arrayInitializer = this.ast.newArrayInitializer();
		if (this.resolveBindings) {
			recordNodes(arrayInitializer, expression);
		}
		arrayInitializer.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] expressions = expression.expressions;
		if (expressions != null) {
			int length = expressions.length;
			for (int i = 0; i < length; i++) {
				Expression expr = convert(expressions[i]);
				if (this.resolveBindings) {
					recordNodes(expr, expressions[i]);
				}
				arrayInitializer.expressions().add(expr);
			}
		}
		return arrayInitializer;
	}

	public Expression convert(QualifiedAllocationExpression expression) {
		if (expression.anonymousType != null) {
			return convert((AnonymousLocalTypeDeclaration) expression.anonymousType);
		} else {
			ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
			classInstanceCreation.setExpression(convert(expression.enclosingInstance));
			classInstanceCreation.setName(convert(expression.type));
			classInstanceCreation.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					Expression argument = convert(arguments[i]);
					if (this.resolveBindings) {
						recordNodes(argument, arguments[i]);
					}
					classInstanceCreation.arguments().add(argument);
				}
			}
			if (this.resolveBindings) {
				recordNodes(classInstanceCreation, expression);
			}
			removeTrailingCommentFromExpressionEndingWithAParen(classInstanceCreation);
			return classInstanceCreation;
		}
	}
	
	public Assignment convert(org.eclipse.jdt.internal.compiler.ast.Assignment expression) {
		Assignment assignment = this.ast.newAssignment();
		if (this.resolveBindings) {
			recordNodes(assignment, expression);
		}
		Expression lhs = convert(expression.lhs);
		assignment.setLeftHandSide(lhs);
		assignment.setOperator(Assignment.Operator.ASSIGN);
		assignment.setRightHandSide(convert(expression.expression));
		int start = lhs.getStartPosition();
		assignment.setSourceRange(start, expression.sourceEnd - start + 1);
		return assignment;
	}

	public Assignment convert(CompoundAssignment expression) {
		Assignment assignment = this.ast.newAssignment();
		Expression lhs = convert(expression.lhs);
		assignment.setLeftHandSide(lhs);
		int start = lhs.getStartPosition();
		assignment.setSourceRange(start, expression.sourceEnd - start + 1);
		switch (expression.operator) {
			case OperatorIds.PLUS :
				assignment.setOperator(Assignment.Operator.PLUS_ASSIGN);
				break;
			case OperatorIds.MINUS :
				assignment.setOperator(Assignment.Operator.MINUS_ASSIGN);
				break;
			case OperatorIds.MULTIPLY :
				assignment.setOperator(Assignment.Operator.TIMES_ASSIGN);
				break;
			case OperatorIds.DIVIDE :
				assignment.setOperator(Assignment.Operator.DIVIDE_ASSIGN);
				break;
			case OperatorIds.AND :
				assignment.setOperator(Assignment.Operator.BIT_AND_ASSIGN);
				break;
			case OperatorIds.OR :
				assignment.setOperator(Assignment.Operator.BIT_OR_ASSIGN);
				break;
			case OperatorIds.XOR :
				assignment.setOperator(Assignment.Operator.BIT_XOR_ASSIGN);
				break;
			case OperatorIds.REMAINDER :
				assignment.setOperator(Assignment.Operator.REMAINDER_ASSIGN);
				break;
			case OperatorIds.LEFT_SHIFT :
				assignment.setOperator(Assignment.Operator.LEFT_SHIFT_ASSIGN);
				break;
			case OperatorIds.RIGHT_SHIFT :
				assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_SIGNED_ASSIGN);
				break;
			case OperatorIds.UNSIGNED_RIGHT_SHIFT :
				assignment.setOperator(Assignment.Operator.RIGHT_SHIFT_UNSIGNED_ASSIGN);
				break;
		}
		assignment.setRightHandSide(convert(expression.expression));
		return assignment;
	}

	public PrefixExpression convert(org.eclipse.jdt.internal.compiler.ast.PrefixExpression expression) {
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		if (this.resolveBindings) {
			recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(convert(expression.lhs));
		switch (expression.operator) {
			case OperatorIds.PLUS :
				prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
				break;
			case OperatorIds.MINUS :
				prefixExpression.setOperator(PrefixExpression.Operator.DECREMENT);
				break;
		}
		return prefixExpression;
	}

	public PostfixExpression convert(org.eclipse.jdt.internal.compiler.ast.PostfixExpression expression) {
		PostfixExpression postfixExpression = this.ast.newPostfixExpression();
		if (this.resolveBindings) {
			recordNodes(postfixExpression, expression);
		}
		postfixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		postfixExpression.setOperand(convert(expression.lhs));
		switch (expression.operator) {
			case OperatorIds.PLUS :
				postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
				break;
			case OperatorIds.MINUS :
				postfixExpression.setOperator(PostfixExpression.Operator.DECREMENT);
				break;
		}
		return postfixExpression;
	}

	public CastExpression convert(org.eclipse.jdt.internal.compiler.ast.CastExpression expression) {
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression type = expression.type;
		removeExtraBlanks(type);
		if (type instanceof TypeReference ) {
			castExpression.setType(convertType((TypeReference)type));
		} else if (type instanceof NameReference) {
			castExpression.setType(convertToType((NameReference)type));
		}
		castExpression.setExpression(convert(expression.expression));
		if (this.resolveBindings) {
			recordNodes(castExpression, expression);
		}
		return castExpression;
	}
		
	public Type convertToType(NameReference reference) {
		Name name = convert(reference);
		SimpleType type = this.ast.newSimpleType(name);
		type.setSourceRange(name.getStartPosition(), name.getLength());
		if (this.resolveBindings) {
			this.recordNodes(type, reference);
		}
		return type;
	}
	public Expression convert(ClassLiteralAccess expression) {
		TypeLiteral typeLiteral = this.ast.newTypeLiteral();
		if (this.resolveBindings) {
			this.recordNodes(typeLiteral, expression);
		}
		typeLiteral.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		typeLiteral.setType(convertType(expression.type));
		return typeLiteral;
	}

	public BooleanLiteral convert(FalseLiteral expression) {
		BooleanLiteral literal = this.ast.newBooleanLiteral(false);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;	
	}
		
	public BooleanLiteral convert(TrueLiteral expression) {
		BooleanLiteral literal = this.ast.newBooleanLiteral(true);
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;		
	}
	
	public org.eclipse.jdt.core.dom.NullLiteral convert(org.eclipse.jdt.internal.compiler.ast.NullLiteral expression) {
		org.eclipse.jdt.core.dom.NullLiteral literal = this.ast.newNullLiteral();
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;		
	}

	public CharacterLiteral convert(CharLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		CharacterLiteral literal = this.ast.newCharacterLiteral();
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setEscapedValue(new String(tokens));
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(DoubleLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(FloatLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(IntLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(IntLiteralMinValue expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(LongLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public NumberLiteral convert(LongLiteralMinValue expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		NumberLiteral literal = this.ast.newNumberLiteral(new String(tokens));
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setSourceRange(sourceStart, length);
		removeLeadingAndTrailingCommentsFromLiteral(literal);
		return literal;
	}

	public StringLiteral convert(org.eclipse.jdt.internal.compiler.ast.StringLiteral expression) {
		int length = expression.sourceEnd - expression.sourceStart + 1;	
		int sourceStart = expression.sourceStart;
		char[] tokens = new char[length];
		System.arraycopy(this.compilationUnitSource, sourceStart, tokens, 0, length);
		StringLiteral literal = this.ast.newStringLiteral();
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setEscapedValue(new String(tokens));
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}

	public StringLiteral convert(ExtendedStringLiteral expression) {
		expression.computeConstant();
		StringLiteral literal = this.ast.newStringLiteral();
		if (this.resolveBindings) {
			this.recordNodes(literal, expression);
		}
		literal.setLiteralValue(expression.constant.stringValue());
		literal.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		return literal;
	}
	
	public Expression convert(BinaryExpression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}

		int expressionOperatorID = (expression.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT;
		switch (expressionOperatorID) {
			case OperatorIds.EQUAL_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.EQUALS);
				break;
			case OperatorIds.LESS_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.LESS_EQUALS);
				break;
			case OperatorIds.GREATER_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.GREATER_EQUALS);
				break;
			case OperatorIds.NOT_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
				break;
			case OperatorIds.LEFT_SHIFT :
				infixExpression.setOperator(InfixExpression.Operator.LEFT_SHIFT);
				break;
			case OperatorIds.RIGHT_SHIFT :
				infixExpression.setOperator(InfixExpression.Operator.RIGHT_SHIFT_SIGNED);
				break;
			case OperatorIds.UNSIGNED_RIGHT_SHIFT :
				infixExpression.setOperator(InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED);
				break;
			case OperatorIds.OR_OR :
				infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
				break;
			case OperatorIds.AND_AND :
				infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
				break;
			case OperatorIds.PLUS :
				infixExpression.setOperator(InfixExpression.Operator.PLUS);
				break;
			case OperatorIds.MINUS :
				infixExpression.setOperator(InfixExpression.Operator.MINUS);
				break;
			case OperatorIds.REMAINDER :
				infixExpression.setOperator(InfixExpression.Operator.REMAINDER);
				break;
			case OperatorIds.XOR :
				infixExpression.setOperator(InfixExpression.Operator.XOR);
				break;
			case OperatorIds.AND :
				infixExpression.setOperator(InfixExpression.Operator.AND);
				break;
			case OperatorIds.MULTIPLY :
				infixExpression.setOperator(InfixExpression.Operator.TIMES);
				break;
			case OperatorIds.OR :
				infixExpression.setOperator(InfixExpression.Operator.OR);
				break;
			case OperatorIds.DIVIDE :
				infixExpression.setOperator(InfixExpression.Operator.DIVIDE);
				break;
			case OperatorIds.GREATER :
				infixExpression.setOperator(InfixExpression.Operator.GREATER);
				break;
			case OperatorIds.LESS :
				infixExpression.setOperator(InfixExpression.Operator.LESS);
		}
		
		if (expression.left instanceof BinaryExpression && ((expression.left.bits & AstNode.ParenthesizedMASK) == 0)) {
			// create an extended string literal equivalent => use the extended operands list
			infixExpression.extendedOperands().add(convert(expression.right));
			org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
			org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
			do {
				rightOperand = ((BinaryExpression) leftOperand).right;
				if ((((leftOperand.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT) != expressionOperatorID && ((leftOperand.bits & AstNode.ParenthesizedMASK) == 0))
				 || ((rightOperand instanceof BinaryExpression && ((rightOperand.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT) != expressionOperatorID) && ((rightOperand.bits & AstNode.ParenthesizedMASK) == 0))) {
				 	List extendedOperands = infixExpression.extendedOperands();
				 	InfixExpression temp = this.ast.newInfixExpression();
					if (this.resolveBindings) {
						this.recordNodes(temp, expression);
					}
				 	temp.setOperator(getOperatorFor(expressionOperatorID));
				 	Expression leftSide = convert(leftOperand);
					temp.setLeftOperand(leftSide);
					temp.setSourceRange(leftSide.getStartPosition(), leftSide.getLength());
					int size = extendedOperands.size();
				 	for (int i = 0; i < size - 1; i++) {
				 		Expression expr = temp;
				 		temp = this.ast.newInfixExpression();
				 		
						if (this.resolveBindings) {
							this.recordNodes(temp, expression);
						}				 	
				 		temp.setLeftOperand(expr);
					 	temp.setOperator(getOperatorFor(expressionOperatorID));
						temp.setSourceRange(expr.getStartPosition(), expr.getLength());
				 	}
				 	infixExpression = temp;
				 	for (int i = 0; i < size; i++) {
				 		Expression extendedOperand = (Expression) extendedOperands.remove(size - 1 - i);
				 		temp.setRightOperand(extendedOperand);
				 		int startPosition = temp.getLeftOperand().getStartPosition();
				 		temp.setSourceRange(startPosition, extendedOperand.getStartPosition() + extendedOperand.getLength() - startPosition);
				 		if (temp.getLeftOperand().getNodeType() == ASTNode.INFIX_EXPRESSION) {
				 			temp = (InfixExpression) temp.getLeftOperand();
				 		}
				 	}
					int startPosition = infixExpression.getLeftOperand().getStartPosition();
					infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
					if (this.resolveBindings) {
						this.recordNodes(infixExpression, expression);
					}
					return infixExpression;
				}
				infixExpression.extendedOperands().add(0, convert(rightOperand));
				leftOperand = ((BinaryExpression) leftOperand).left;
			} while (leftOperand instanceof BinaryExpression && ((leftOperand.bits & AstNode.ParenthesizedMASK) == 0));
			Expression leftExpression = convert(leftOperand);
			infixExpression.setLeftOperand(leftExpression);
			infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
			int startPosition = leftExpression.getStartPosition();
			infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
			return infixExpression;
		}		
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return infixExpression;
	}
			
	public PrefixExpression convert(UnaryExpression expression) {
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		if (this.resolveBindings) {
			this.recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(convert(expression.expression));
		switch ((expression.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT) {
			case OperatorIds.PLUS :
				prefixExpression.setOperator(PrefixExpression.Operator.PLUS);
				break;
			case OperatorIds.MINUS :
				prefixExpression.setOperator(PrefixExpression.Operator.MINUS);
				break;
			case OperatorIds.NOT :
				prefixExpression.setOperator(PrefixExpression.Operator.NOT);
				break;
			case OperatorIds.TWIDDLE :
				prefixExpression.setOperator(PrefixExpression.Operator.COMPLEMENT);
		}
		return prefixExpression;
	}
	
	public InstanceofExpression convert(org.eclipse.jdt.internal.compiler.ast.InstanceOfExpression expression) {
		InstanceofExpression instanceOfExpression = this.ast.newInstanceofExpression();
		if (this.resolveBindings) {
			recordNodes(instanceOfExpression, expression);
		}
		Expression leftExpression = convert(expression.expression);
		instanceOfExpression.setLeftOperand(leftExpression);
		instanceOfExpression.setRightOperand(convertType(expression.type));
		int startPosition = leftExpression.getStartPosition();
		instanceOfExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return instanceOfExpression;
	}

	public ConditionalExpression convert(org.eclipse.jdt.internal.compiler.ast.ConditionalExpression expression) {
		ConditionalExpression conditionalExpression = this.ast.newConditionalExpression();
		if (this.resolveBindings) {
			recordNodes(conditionalExpression, expression);
		}
		conditionalExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		conditionalExpression.setExpression(convert(expression.condition));
		conditionalExpression.setThenExpression(convert(expression.valueIfTrue));
		conditionalExpression.setElseExpression(convert(expression.valueIfFalse));
		return conditionalExpression;
	}

	public Expression convert(MessageSend expression) {
		// will return a MethodInvocation or a SuperMethodInvocation or
		Expression expr;
		int sourceStart = expression.sourceStart;
		if (expression.isSuperAccess()) {
			// returns a SuperMethodInvocation
			SuperMethodInvocation superMethodInvocation = this.ast.newSuperMethodInvocation();
			if (this.resolveBindings) {
				recordNodes(superMethodInvocation, expression);
			}
			SimpleName name = this.ast.newSimpleName(new String(expression.selector));
			int nameSourceStart =  (int) (expression.nameSourcePosition >>> 32);
			int nameSourceLength = (int)(expression.nameSourcePosition & 0xFFFFFFFF) - nameSourceStart + 1;
			name.setSourceRange(nameSourceStart, nameSourceLength);
			if (this.resolveBindings) {
				recordNodes(name, expression);
			}
			superMethodInvocation.setName(name);
			// expression.receiver is either a QualifiedSuperReference or a SuperReference
			// so the casting cannot fail
			if (expression.receiver instanceof QualifiedSuperReference) {
				Name qualifier = convert((QualifiedSuperReference) expression.receiver);
				superMethodInvocation.setQualifier(qualifier);
				if (this.resolveBindings) {
					recordNodes(qualifier, expression.receiver);
				}
				if (qualifier != null) {
					sourceStart = qualifier.getStartPosition();
				}			
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++) {
					Expression expri = convert(arguments[i]);
					if (this.resolveBindings) {
						recordNodes(expri, arguments[i]);
					}
					superMethodInvocation.arguments().add(expri);
				}
			}
			expr = superMethodInvocation;
		} else {
			// returns a MethodInvocation
			MethodInvocation methodInvocation = this.ast.newMethodInvocation();
			if (this.resolveBindings) {
				recordNodes(methodInvocation, expression);
			}
			SimpleName name = this.ast.newSimpleName(new String(expression.selector));
			int nameSourceStart =  (int) (expression.nameSourcePosition >>> 32);
			int nameSourceLength = (int)(expression.nameSourcePosition & 0xFFFFFFFF) - nameSourceStart + 1;
			name.setSourceRange(nameSourceStart, nameSourceLength);
			methodInvocation.setName(name);
			if (this.resolveBindings) {
				recordNodes(name, expression);
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.arguments;
			if (arguments != null) {
				int argumentsLength = arguments.length;
				for (int i = 0; i < argumentsLength; i++) {
					Expression expri = convert(arguments[i]);
					if (this.resolveBindings) {
						recordNodes(expri, arguments[i]);
					}
					methodInvocation.arguments().add(expri);
				}
			}
			Expression qualifier = convert(expression.receiver);
			if (qualifier instanceof Name && this.resolveBindings) {
				recordNodes(qualifier, expression.receiver);
			}
			methodInvocation.setExpression(qualifier);
			if (qualifier != null) {
				sourceStart = qualifier.getStartPosition();
			}
			expr = methodInvocation;
		}
		expr.setSourceRange(sourceStart, expression.sourceEnd - sourceStart + 1);	
		removeTrailingCommentFromExpressionEndingWithAParen(expr);
		return expr;
	}

	public Expression convert(AND_AND_Expression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		return infixExpression;
	
	}
	
	public Expression convert(EqualExpression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		int startPosition = leftExpression.getStartPosition();
		infixExpression.setSourceRange(startPosition, expression.sourceEnd - startPosition + 1);
		switch ((expression.bits & AstNode.OperatorMASK) >> AstNode.OperatorSHIFT) {
			case OperatorIds.EQUAL_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.EQUALS);
				break;
			case OperatorIds.NOT_EQUAL :
				infixExpression.setOperator(InfixExpression.Operator.NOT_EQUALS);
		}
		return infixExpression;
	
	}

	public Expression convert(OR_OR_Expression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		Expression leftExpression = convert(expression.left);
		infixExpression.setLeftOperand(leftExpression);
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		int sourceStart = leftExpression.getStartPosition();
		infixExpression.setSourceRange(sourceStart, expression.sourceEnd - sourceStart + 1);
		return infixExpression;
	}

	public Statement convert(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof LocalDeclaration) {
			return convertToVariableDeclarationStatement((LocalDeclaration)statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.AssertStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AssertStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Block) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Block) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.BreakStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.BreakStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ContinueStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ContinueStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.CaseStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CaseStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.DoStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.DoStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.EmptyStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.EmptyStatement) statement);
		}
		if (statement instanceof ExplicitConstructorCall) {
			return convert((ExplicitConstructorCall) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ForStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ForStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.IfStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.IfStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.LabeledStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.LabeledStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ReturnStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ReturnStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.SwitchStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SwitchStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.ThrowStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.ThrowStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.TryStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.TryStatement) statement);
		}
		if (statement instanceof LocalTypeDeclaration) {
			TypeDeclarationStatement typeDeclarationStatement = this.ast.newTypeDeclarationStatement(convert((LocalTypeDeclaration) statement));
			TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
			typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
			return typeDeclarationStatement;
					
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) {
			TypeDeclarationStatement typeDeclarationStatement = this.ast.newTypeDeclarationStatement(convert((org.eclipse.jdt.internal.compiler.ast.TypeDeclaration) statement));
			TypeDeclaration typeDecl = typeDeclarationStatement.getTypeDeclaration();
			typeDeclarationStatement.setSourceRange(typeDecl.getStartPosition(), typeDecl.getLength());
			return typeDeclarationStatement;
		}
		if (statement instanceof AnonymousLocalTypeDeclaration) {
			Expression expr = convert((AnonymousLocalTypeDeclaration) statement);
			Statement stmt = this.ast.newExpressionStatement(expr);
			stmt.setSourceRange(expr.getStartPosition(), expr.getLength());
			return stmt;
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.WhileStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.WhileStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Expression) {
			Expression expr = convert((org.eclipse.jdt.internal.compiler.ast.Expression) statement);
			Statement stmt = this.ast.newExpressionStatement(expr);
			stmt.setSourceRange(expr.getStartPosition(), expr.getLength());
			retrieveSemiColonPosition(stmt);
			return stmt;
		}
		throw new IllegalArgumentException("Not yet implemented: convert(" + statement.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}

	public AssertStatement convert(org.eclipse.jdt.internal.compiler.ast.AssertStatement statement) {
		AssertStatement assertStatement = this.ast.newAssertStatement();
		int end = statement.assertExpression.sourceEnd + 1;
		assertStatement.setExpression(convert(statement.assertExpression));
		org.eclipse.jdt.internal.compiler.ast.Expression exceptionArgument = statement.exceptionArgument;
		if (exceptionArgument != null) {
			assertStatement.setMessage(convert(exceptionArgument));
			end = exceptionArgument.sourceEnd + 1;
		}
		int start = statement.sourceStart;
		int sourceEnd = retrieveEndingSemiColonPosition(end, compilationUnitSource.length);
		assertStatement.setSourceRange(start, sourceEnd - start + 1);
		return assertStatement;
	}

	public Block convert(org.eclipse.jdt.internal.compiler.ast.Block statement) {
		Block block = this.ast.newBlock();
		if (statement.sourceEnd > 0) {
			block.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		}
		org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				if (statements[i] instanceof LocalDeclaration) {
					checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
				} else {
					block.statements().add(convert(statements[i]));
				}				
			}
		}
		return block;
	}
	
	public BreakStatement convert(org.eclipse.jdt.internal.compiler.ast.BreakStatement statement)  {
		BreakStatement breakStatement = this.ast.newBreakStatement();
		breakStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			SimpleName name = this.ast.newSimpleName(new String(statement.label));
			retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
			breakStatement.setLabel(name);
		}
		retrieveSemiColonPosition(breakStatement);
		return breakStatement;
	}

	public ContinueStatement convert(org.eclipse.jdt.internal.compiler.ast.ContinueStatement statement)  {
		ContinueStatement continueStatement = this.ast.newContinueStatement();
		continueStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			SimpleName name = this.ast.newSimpleName(new String(statement.label));
			retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
			continueStatement.setLabel(name);
		}
		retrieveSemiColonPosition(continueStatement);
		return continueStatement;
	}
		
		
	public SwitchCase convert(org.eclipse.jdt.internal.compiler.ast.CaseStatement statement) {
		SwitchCase switchCase = this.ast.newSwitchCase();
		org.eclipse.jdt.internal.compiler.ast.Expression constantExpression = statement.constantExpression;
		if (constantExpression == null) {
			switchCase.setExpression(null);
		} else {
			switchCase.setExpression(convert(constantExpression));
		}
		switchCase.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		retrieveColonPosition(switchCase);
		return switchCase;
	}
	
	public DoStatement convert(org.eclipse.jdt.internal.compiler.ast.DoStatement statement) {
		DoStatement doStatement = this.ast.newDoStatement();
		doStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		doStatement.setExpression(convert(statement.condition));
		doStatement.setBody(convert(statement.action));
		retrieveSemiColonPosition(doStatement);
		return doStatement;
	}
	
	public EmptyStatement convert(org.eclipse.jdt.internal.compiler.ast.EmptyStatement statement) {
		EmptyStatement emptyStatement = this.ast.newEmptyStatement();
		emptyStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		return emptyStatement;
	}
	
	public Statement convert(ExplicitConstructorCall statement) {
		Statement newStatement;
		if (statement.isSuperAccess() || statement.isSuper()) {
			SuperConstructorInvocation superConstructorInvocation = this.ast.newSuperConstructorInvocation();
			if (statement.qualification != null) {
				superConstructorInvocation.setExpression(convert(statement.qualification));
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					superConstructorInvocation.arguments().add(convert(arguments[i]));
				}
			}
			newStatement = superConstructorInvocation;
		} else {
			ConstructorInvocation constructorInvocation = this.ast.newConstructorInvocation();
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					constructorInvocation.arguments().add(convert(arguments[i]));
				}
			}
			newStatement = constructorInvocation;
		}
		newStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		retrieveSemiColonPosition(newStatement);
		if (this.resolveBindings) {
			recordNodes(newStatement, statement);
		}
		return newStatement;
	}
	
	public ForStatement convert(org.eclipse.jdt.internal.compiler.ast.ForStatement statement) {
		ForStatement forStatement = this.ast.newForStatement();
		forStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Statement[] initializations = statement.initializations;
		if (initializations != null) {
			// we know that we have at least one initialization
			if (initializations[0] instanceof LocalDeclaration) {
				VariableDeclarationExpression variableDeclarationExpression = convertToVariableDeclarationExpression((LocalDeclaration) initializations[0]);
				int initializationsLength = initializations.length;
				for (int i = 1; i < initializationsLength; i++) {
					variableDeclarationExpression.fragments().add(convertToVariableDeclarationFragment((LocalDeclaration)initializations[i]));
				}
				if (initializationsLength != 1) {
					int start = variableDeclarationExpression.getStartPosition();
					int end = ((LocalDeclaration) initializations[initializationsLength - 1]).declarationSourceEnd;
					variableDeclarationExpression.setSourceRange(start, end - start + 1);
				}
				forStatement.initializers().add(variableDeclarationExpression);
			} else {
				int initializationsLength = initializations.length;
				for (int i = 0; i < initializationsLength; i++) {
					forStatement.initializers().add(convertToExpression(initializations[i]));
				}
			}
		}
		if (statement.condition != null) {
			forStatement.setExpression(convert(statement.condition));
		}
		org.eclipse.jdt.internal.compiler.ast.Statement[] increments = statement.increments;
		if (increments != null) {
			int incrementsLength = increments.length;
			for (int i = 0; i < incrementsLength; i++) {
				forStatement.updaters().add(convertToExpression(increments[i]));				
			}
		}
		forStatement.setBody(convert(statement.action));
		return forStatement;
	}
	
	public Expression convertToExpression(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Expression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Expression) statement);
		}
		// unsupported
		throw new IllegalArgumentException("Not yet implemented: convert(" + statement.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
	}
	
	public IfStatement convert(org.eclipse.jdt.internal.compiler.ast.IfStatement statement) {
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		ifStatement.setExpression(convert(statement.condition));
		ifStatement.setThenStatement(convert(statement.thenStatement));
		if (statement.elseStatement != null) {
			ifStatement.setElseStatement(convert(statement.elseStatement));
		}
		return ifStatement;
	}
	
	public LabeledStatement convert(org.eclipse.jdt.internal.compiler.ast.LabeledStatement statement) {
		LabeledStatement labeledStatement = this.ast.newLabeledStatement();
		labeledStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		org.eclipse.jdt.internal.compiler.ast.Statement body = statement.statement;
		labeledStatement.setBody(convert(body));
		SimpleName name = this.ast.newSimpleName(new String(statement.label));
		retrieveIdentifierAndSetPositions(statement.sourceStart, statement.sourceEnd, name);
		labeledStatement.setLabel(name);
		return labeledStatement;
	}
	
	public ReturnStatement convert(org.eclipse.jdt.internal.compiler.ast.ReturnStatement statement) {
		ReturnStatement returnStatement = this.ast.newReturnStatement();
		returnStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		if (statement.expression != null) {
			returnStatement.setExpression(convert(statement.expression));
		}
		retrieveSemiColonPosition(returnStatement);
		return returnStatement;
	}
	
	public SwitchStatement convert(org.eclipse.jdt.internal.compiler.ast.SwitchStatement statement) {
		SwitchStatement switchStatement = this.ast.newSwitchStatement();
		switchStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		switchStatement.setExpression(convert(statement.testExpression));
		org.eclipse.jdt.internal.compiler.ast.Statement[] statements = statement.statements;
		if (statements != null) {
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				switchStatement.statements().add(convert(statements[i]));
			}
		}
		return switchStatement;
	}
	
	public SynchronizedStatement convert(org.eclipse.jdt.internal.compiler.ast.SynchronizedStatement statement) {
		SynchronizedStatement synchronizedStatement = this.ast.newSynchronizedStatement();
		synchronizedStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		synchronizedStatement.setBody(convert(statement.block));
		synchronizedStatement.setExpression(convert(statement.expression));
		return synchronizedStatement;
	}
	
	public ThrowStatement convert(org.eclipse.jdt.internal.compiler.ast.ThrowStatement statement) {
		ThrowStatement throwStatement = this.ast.newThrowStatement();
		throwStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		throwStatement.setExpression(convert(statement.exception));
		retrieveSemiColonPosition(throwStatement);
		return throwStatement;
	}
	
	public TryStatement convert(org.eclipse.jdt.internal.compiler.ast.TryStatement statement) {
		TryStatement tryStatement = this.ast.newTryStatement();
		tryStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	

		tryStatement.setBody(convert(statement.tryBlock));
		Argument[] catchArguments = statement.catchArguments;
		if (catchArguments != null) {
			int catchArgumentsLength = catchArguments.length;
			org.eclipse.jdt.internal.compiler.ast.Block[] catchBlocks = statement.catchBlocks;
			int start = statement.tryBlock.sourceEnd;
			for (int i = 0; i < catchArgumentsLength; i++) {
				CatchClause catchClause = this.ast.newCatchClause();
				int catchClauseSourceStart = retrieveStartingCatchPosition(start, catchArguments[i].sourceStart);
				catchClause.setSourceRange(catchClauseSourceStart, catchBlocks[i].sourceEnd - catchClauseSourceStart + 1);	
				catchClause.setBody(convert(catchBlocks[i]));
				catchClause.setException(convert(catchArguments[i]));
				tryStatement.catchClauses().add(catchClause);
				start = catchBlocks[i].sourceEnd;
			}
		}
		if (statement.finallyBlock != null) {
			tryStatement.setFinally(convert(statement.finallyBlock));
		}
		return tryStatement;
	}
	
	public WhileStatement convert(org.eclipse.jdt.internal.compiler.ast.WhileStatement statement) {
		WhileStatement whileStatement = this.ast.newWhileStatement();
		whileStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		whileStatement.setExpression(convert(statement.condition));
		org.eclipse.jdt.internal.compiler.ast.Statement action = statement.action;
		whileStatement.setBody(convert(action));
		return whileStatement;
	}
	
	private boolean isPrimitiveType(char[] name) {
		switch(name[0]) {
			case 'i' :
				if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
					return true;
				}
				return false;
			case 'l' :
				if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
					return true;
				}
				return false;
			case 'd' :
				if (name.length == 6
					 && name[1] == 'o'
					 && name[2] == 'u'
					 && name[3] == 'b'
					 && name[4] == 'l'
					 && name[5] == 'e') {
					return true;
				}
				return false;
			case 'f' :
				if (name.length == 5
					 && name[1] == 'l'
					 && name[2] == 'o'
					 && name[3] == 'a'
					 && name[4] == 't') {
					return true;
				}
				return false;
			case 'b' :
				if (name.length == 4
					 && name[1] == 'y'
					 && name[2] == 't'
					 && name[3] == 'e') {
					return true;
				} else
					if (name.length == 7
						 && name[1] == 'o'
						 && name[2] == 'o'
						 && name[3] == 'l'
						 && name[4] == 'e'
						 && name[5] == 'a'
						 && name[6] == 'n') {
					return true;
				}
				return false;
			case 'c' :
				if (name.length == 4
					 && name[1] == 'h'
					 && name[2] == 'a'
					 && name[3] == 'r') {
					return true;
				}
				return false;
			case 's' :
				if (name.length == 5
					 && name[1] == 'h'
					 && name[2] == 'o'
					 && name[3] == 'r'
					 && name[4] == 't') {
					return true;
				}
				return false;
			case 'v' :
				if (name.length == 4
					 && name[1] == 'o'
					 && name[2] == 'i'
					 && name[3] == 'd') {
					return true;
				}
				return false;
		}
		return false;
	}
	
	private PrimitiveType.Code getPrimitiveTypeCode(char[] name) {
		switch(name[0]) {
			case 'i' :
				if (name.length == 3 && name[1] == 'n' && name[2] == 't') {
					return PrimitiveType.INT;
				}
				break;
			case 'l' :
				if (name.length == 4 && name[1] == 'o' && name[2] == 'n' && name[3] == 'g') {
					return PrimitiveType.LONG;
				}
				break;
			case 'd' :
				if (name.length == 6
					 && name[1] == 'o'
					 && name[2] == 'u'
					 && name[3] == 'b'
					 && name[4] == 'l'
					 && name[5] == 'e') {
					return PrimitiveType.DOUBLE;
				}
				break;
			case 'f' :
				if (name.length == 5
					 && name[1] == 'l'
					 && name[2] == 'o'
					 && name[3] == 'a'
					 && name[4] == 't') {
					return PrimitiveType.FLOAT;
				}
				break;
			case 'b' :
				if (name.length == 4
					 && name[1] == 'y'
					 && name[2] == 't'
					 && name[3] == 'e') {
					return PrimitiveType.BYTE;
				} else
					if (name.length == 7
						 && name[1] == 'o'
						 && name[2] == 'o'
						 && name[3] == 'l'
						 && name[4] == 'e'
						 && name[5] == 'a'
						 && name[6] == 'n') {
					return PrimitiveType.BOOLEAN;
				}
				break;
			case 'c' :
				if (name.length == 4
					 && name[1] == 'h'
					 && name[2] == 'a'
					 && name[3] == 'r') {
					return PrimitiveType.CHAR;
				}
				break;
			case 's' :
				if (name.length == 5
					 && name[1] == 'h'
					 && name[2] == 'o'
					 && name[3] == 'r'
					 && name[4] == 't') {
					return PrimitiveType.SHORT;
				}
				break;
			case 'v' :
				if (name.length == 4
					 && name[1] == 'o'
					 && name[2] == 'i'
					 && name[3] == 'd') {
					return PrimitiveType.VOID;
				}
		}
		throw new IllegalArgumentException("Not a primitive type");//$NON-NLS-1$
	}
	
	/**
	 * This method is used to set the right end position for expression
	 * statement. The actual AST nodes don't include the trailing semicolon.
	 * This method fixes the length of the corresponding node.
	 */
	private void retrieveSemiColonPosition(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		int count = 0;
		scanner.resetTo(end, this.compilationUnitSource.length);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameSEMICOLON:
						if (count == 0) {
							node.setSourceRange(start, scanner.currentPosition - start);
							return;
						}
						break;
					case TerminalTokens.TokenNameLBRACE :
						count++;
						break;
					case TerminalTokens.TokenNameRBRACE :
						count--;
						break;
					case TerminalTokens.TokenNameLPAREN :
						count++;
						break;
					case TerminalTokens.TokenNameRPAREN :
						count--;
						break;
					case TerminalTokens.TokenNameLBRACKET :
						count++;
						break;
					case TerminalTokens.TokenNameRBRACKET :
						count--;
				}
			}
		} catch(InvalidInputException e) {
		}
	}

	/**
	 * This method is used to set the right end position for expression
	 * statement. The actual AST nodes don't include the trailing semicolon.
	 * This method fixes the length of the corresponding node.
	 */
	private void retrieveColonPosition(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		scanner.resetTo(end, this.compilationUnitSource.length);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameCOLON:
						node.setSourceRange(start, scanner.currentPosition - start);
						return;
				}
			}
		} catch(InvalidInputException e) {
		}
	}

	private int retrieveEndingSemiColonPosition(int start, int end) {
		int count = 0;
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameSEMICOLON:
						if (count == 0) {
							return scanner.currentPosition - 1;
						}
						break;
					case TerminalTokens.TokenNameLBRACE :
						count++;
						break;
					case TerminalTokens.TokenNameRBRACE :
						count--;
						break;
					case TerminalTokens.TokenNameLPAREN :
						count++;
						break;
					case TerminalTokens.TokenNameRPAREN :
						count--;
						break;
					case TerminalTokens.TokenNameLBRACKET :
						count++;
						break;
					case TerminalTokens.TokenNameRBRACKET :
						count--;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the array dimension declared after the
	 * name of a local or a field declaration.
	 * For example:
	 *    int i, j[] = null, k[][] = {{}};
	 *    It should return 0 for i, 1 for j and 2 for k.
	 * @return int the dimension found
	 */
	private int retrieveExtraDimension(int start, int end) {
		scanner.resetTo(start, end);
		int dimensions = 0;
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACKET://166 
						dimensions++;
						break;
					case TerminalTokens.TokenNameLBRACE ://90						
					case TerminalTokens.TokenNameCOMMA ://90
					case TerminalTokens.TokenNameEQUAL ://167
					case TerminalTokens.TokenNameSEMICOLON ://64
					case TerminalTokens.TokenNameRPAREN : //86
						return dimensions;
				}
			}
		} catch(InvalidInputException e) {
		}
		return dimensions;
	}

	/**
	 * This method is used to retrieve the ending position for a type declaration when the dimension is right after the type
	 * name.
	 * For example:
	 *    int[] i; => return 5, but int i[] => return -1;
	 * @return int the dimension found
	 */
	private int retrieveEndOfDimensionsPosition(int start, int end) {
		scanner.resetTo(start, end);
		int foundPosition = -1;
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACKET:
					case TerminalTokens.TokenNameCOMMENT_BLOCK:
					case TerminalTokens.TokenNameCOMMENT_JAVADOC:
					case TerminalTokens.TokenNameCOMMENT_LINE:
						break;
					case TerminalTokens.TokenNameRBRACKET://166
						foundPosition = scanner.currentPosition - 1;
						break;
					default:
						return foundPosition;
				}
			}
		} catch(InvalidInputException e) {
		}
		return foundPosition;
	}

	/**
	 * This method is used to retrieve the starting position of the catch keyword.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveStartingCatchPosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNamecatch://225
						return scanner.startPosition;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position just before the left bracket.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveEndOfElementTypeNamePosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameIdentifier:
					case TerminalTokens.TokenNamebyte:
					case TerminalTokens.TokenNamechar:
					case TerminalTokens.TokenNamedouble:
					case TerminalTokens.TokenNamefloat:
					case TerminalTokens.TokenNameint:
					case TerminalTokens.TokenNamelong:
					case TerminalTokens.TokenNameshort:
						return scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position of the right bracket.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveRightBracketPosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACKET:
						return scanner.currentPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve the position after the right parenthesis.
	 * @return int the position found
	 */
	private int retrieveEndOfRightParenthesisPosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRPAREN:
						return scanner.currentPosition;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	private int retrieveProperRightBracketPosition(int bracketNumber, int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token, count = 0;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameRBRACKET:
						count++;
						if (count == bracketNumber) {
							return scanner.currentPosition - 1;
						}
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}
	
	/**
	 * This method is used to retrieve the start position of the block.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveStartBlockPosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE://110
						return scanner.startPosition;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}
	
	/**
	 * This method is used to retrieve the start position of the block.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveIdentifierEndPosition(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameIdentifier://110
						return scanner.getCurrentTokenEndPosition();
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}	
	
	/**
	 * This method is used to retrieve the end position of the block.
	 * @return int the dimension found, -1 if none
	 */
	private int retrieveEndBlockPosition(int start, int end) {
		scanner.resetTo(start, end);
		int count = 0;
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE://110
						count++;
						break;
					case TerminalTokens.TokenNameRBRACE://95
						count--;
						if (count == 0) {
							return scanner.currentPosition - 1;
						}
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next right brace or semi-colon.
	 * @return int the position found.
	 */
	private int retrieveRightBraceOrSemiColonPosition(MethodDeclaration node, AbstractMethodDeclaration methodDeclaration) {
		int start = node.getStartPosition();
		scanner.resetTo(start, methodDeclaration.declarationSourceEnd);
		try {
			int token;
			int braceCounter = 0;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE :
						braceCounter++;
						break;
					case TerminalTokens.TokenNameRBRACE :
						braceCounter--;
						if (braceCounter == 0) {
							return scanner.currentPosition;
						}
						break;
					case TerminalTokens.TokenNameSEMICOLON :
						if (braceCounter == 0) {
							return scanner.currentPosition;
						}
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	/**
	 * This method is used to retrieve position before the next comma or semi-colon.
	 * @return int the position found.
	 */
	private int retrievePositionBeforeNextCommaOrSemiColon(int start, int end) {
		scanner.resetTo(start, end);
		int braceCounter = 0;
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameLBRACE :
						braceCounter++;
						break;
					case TerminalTokens.TokenNameRBRACE :
						braceCounter--;
						break;
					case TerminalTokens.TokenNameLPAREN :
						braceCounter++;
						break;
					case TerminalTokens.TokenNameRPAREN :
						braceCounter--;
						break;
					case TerminalTokens.TokenNameLBRACKET :
						braceCounter++;
						break;
					case TerminalTokens.TokenNameRBRACKET :
						braceCounter--;
						break;
					case TerminalTokens.TokenNameCOMMA :
					case TerminalTokens.TokenNameSEMICOLON :
						if (braceCounter == 0) {
							return scanner.startPosition - 1;
						}
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}
	
	private VariableDeclarationFragment convertToVariableDeclarationFragment(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		SimpleName name = this.ast.newSimpleName(new String(fieldDeclaration.name));
		name.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.sourceEnd - fieldDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int end = retrievePositionBeforeNextCommaOrSemiColon(fieldDeclaration.sourceEnd, fieldDeclaration.declarationSourceEnd);
		if (end == -1) {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, fieldDeclaration.declarationSourceEnd - fieldDeclaration.sourceStart + 1);
			variableDeclarationFragment.setFlags(ASTNode.MALFORMED);
		} else {
			variableDeclarationFragment.setSourceRange(fieldDeclaration.sourceStart, end - fieldDeclaration.sourceStart + 1);
		}
		if (fieldDeclaration.initialization != null) {
			variableDeclarationFragment.setInitializer(convert(fieldDeclaration.initialization));
		}
		variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(fieldDeclaration.sourceEnd + 1, fieldDeclaration.declarationSourceEnd ));
		if (this.resolveBindings) {
			recordNodes(name, fieldDeclaration);
			recordNodes(variableDeclarationFragment, fieldDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	private VariableDeclarationFragment convertToVariableDeclarationFragment(LocalDeclaration localDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		SimpleName name = this.ast.newSimpleName(new String(localDeclaration.name));
		name.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd - localDeclaration.sourceStart + 1);
		variableDeclarationFragment.setName(name);
		int end = retrievePositionBeforeNextCommaOrSemiColon(localDeclaration.sourceEnd, this.compilationUnitSource.length);
		if (end == -1) {
			if (localDeclaration.initialization != null) {
				variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, localDeclaration.initialization.sourceEnd - localDeclaration.sourceStart + 1);
			} else {
				variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd - localDeclaration.sourceStart + 1);
			}
		} else {
			variableDeclarationFragment.setSourceRange(localDeclaration.sourceStart, end - localDeclaration.sourceStart + 1);
		}
		if (localDeclaration.initialization != null) {
			variableDeclarationFragment.setInitializer(convert(localDeclaration.initialization));
		}
		variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(localDeclaration.sourceEnd + 1, this.compilationUnitSource.length));
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
			recordNodes(name, localDeclaration);
			variableDeclarationFragment.resolveBinding();
		}
		return variableDeclarationFragment;
	}

	private FieldDeclaration convertToFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(fieldDecl);
		FieldDeclaration fieldDeclaration = this.ast.newFieldDeclaration(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDecl);
			variableDeclarationFragment.resolveBinding();
		}
		fieldDeclaration.setSourceRange(fieldDecl.declarationSourceStart, fieldDecl.declarationEnd - fieldDecl.declarationSourceStart + 1);
		Type type = convertType(fieldDecl.type);
		setTypeForField(fieldDeclaration, type, variableDeclarationFragment.getExtraDimensions());
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built nodes with invalid modifiers.
		 */
		try {
			fieldDeclaration.setModifiers(fieldDecl.modifiers & org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers.AccJustFlag);
		} catch(IllegalArgumentException e) {
			int legalModifiers = 
				Modifier.PUBLIC | Modifier.PRIVATE | Modifier.PROTECTED
				| Modifier.STATIC | Modifier.FINAL | Modifier.VOLATILE
				| Modifier.TRANSIENT;
			fieldDeclaration.setModifiers(fieldDecl.modifiers & legalModifiers);
			fieldDeclaration.setFlags(ASTNode.MALFORMED);
		}
		setJavaDocComment(fieldDeclaration);
		return fieldDeclaration;
	}

	private VariableDeclarationStatement convertToVariableDeclarationStatement(LocalDeclaration localDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		VariableDeclarationStatement variableDeclarationStatement = this.ast.newVariableDeclarationStatement(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationStatement.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationStatement(variableDeclarationStatement, type, variableDeclarationFragment.getExtraDimensions());
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built variables with invalid modifiers.
		 * The compilation unit is tagged as having wrong modifiers for the local.
		 * Only final is allowed in this case.
		 */
		try {
			variableDeclarationStatement.setModifiers(localDeclaration.modifiers & ~CompilerModifiers.AccBlankFinal);
		} catch(IllegalArgumentException e) {
			variableDeclarationStatement.setModifiers(localDeclaration.modifiers & Modifier.FINAL);
			variableDeclarationStatement.setFlags(ASTNode.MALFORMED);
		}
		retrieveSemiColonPosition(variableDeclarationStatement);
		return variableDeclarationStatement;
	}
	
	private VariableDeclarationExpression convertToVariableDeclarationExpression(LocalDeclaration localDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(localDeclaration);
		VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		variableDeclarationExpression.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
		Type type = convertType(localDeclaration.type);
		setTypeForVariableDeclarationExpression(variableDeclarationExpression, type, variableDeclarationFragment.getExtraDimensions());
		/**
		 * http://dev.eclipse.org/bugs/show_bug.cgi?id=13233
		 * This handles cases where the parser built variables with invalid modifiers.
		 * The compilation unit is tagged as having wrong modifiers for the local.
		 * Only final is allowed in this case.
		 */
		try {
			variableDeclarationExpression.setModifiers(localDeclaration.modifiers & ~CompilerModifiers.AccBlankFinal);
		} catch(IllegalArgumentException e) {
			variableDeclarationExpression.setModifiers(localDeclaration.modifiers & Modifier.FINAL);
			variableDeclarationExpression.setFlags(ASTNode.MALFORMED);
		}
		return variableDeclarationExpression;
	}
	
	private void setTypeForField(FieldDeclaration fieldDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					elementType.setParent(null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					fieldDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					int length = type.getLength();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start, start + length);
					subarrayType.setSourceRange(start, end - start + 1);
					subarrayType.setParent(null);
					fieldDeclaration.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				fieldDeclaration.setType(type);
			}
		} else {
			fieldDeclaration.setType(type);
		}
	}

	private void setTypeForSingleVariableDeclaration(SingleVariableDeclaration singleVariableDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					elementType.setParent(null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					singleVariableDeclaration.setType(elementType);
				} else {
					int start = type.getStartPosition();
					int length = type.getLength();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start, start + length);
					subarrayType.setSourceRange(start, end - start + 1);
					subarrayType.setParent(null);
					singleVariableDeclaration.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				singleVariableDeclaration.setType(type);
			}
		} else {
			singleVariableDeclaration.setType(type);
		}
	}

	private void setTypeForMethodDeclaration(MethodDeclaration methodDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					elementType.setParent(null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					methodDeclaration.setReturnType(elementType);
				} else {
					int start = type.getStartPosition();
					int length = type.getLength();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start, start + length);
					subarrayType.setSourceRange(start, end - start + 1);
					subarrayType.setParent(null);
					methodDeclaration.setReturnType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				methodDeclaration.setReturnType(type);
			}
		} else {
			methodDeclaration.setReturnType(type);
		}
	}

	private void setTypeForVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					elementType.setParent(null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationStatement.setType(elementType);
				} else {
					int start = type.getStartPosition();
					int length = type.getLength();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start, start + length);
					subarrayType.setSourceRange(start, end - start + 1);
					subarrayType.setParent(null);
					variableDeclarationStatement.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationStatement.setType(type);
			}
		} else {
			variableDeclarationStatement.setType(type);
		}
	}

	private void setTypeForVariableDeclarationExpression(VariableDeclarationExpression variableDeclarationExpression, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					Type elementType = arrayType.getElementType();
					elementType.setParent(null);
					this.ast.getBindingResolver().updateKey(type, elementType);
					variableDeclarationExpression.setType(elementType);
				} else {
					int start = type.getStartPosition();
					int length = type.getLength();
					ArrayType subarrayType = arrayType;
					int index = extraDimension;
					while (index > 0) {
						subarrayType = (ArrayType) subarrayType.getComponentType();
						index--;
					}
					int end = retrieveProperRightBracketPosition(remainingDimensions, start, start + length);
					subarrayType.setSourceRange(start, end - start + 1);
					subarrayType.setParent(null);
					variableDeclarationExpression.setType(subarrayType);
					this.ast.getBindingResolver().updateKey(type, subarrayType);
				}
			} else {
				variableDeclarationExpression.setType(type);
			}
		} else {
			variableDeclarationExpression.setType(type);
		}
	}
	
	private void setJavaDocComment(BodyDeclaration bodyDeclaration) {
		scanner.resetTo(bodyDeclaration.getStartPosition(), bodyDeclaration.getStartPosition() + bodyDeclaration.getLength());
		try {
			int token;
			while ((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(token) {
					case TerminalTokens.TokenNameCOMMENT_JAVADOC: //1003
						Javadoc javadocComment = this.ast.newJavadoc();
						int start = scanner.startPosition;
						int length = scanner.currentPosition - start;
						char[] contents = new char[length];
						System.arraycopy(this.compilationUnitSource, start, contents, 0, length);
						javadocComment.setComment(new String(contents));
						javadocComment.setSourceRange(start, length);
						bodyDeclaration.setJavadoc(javadocComment);
						return;
					default :
						return;
				}
			}
		} catch(InvalidInputException e) {
		}
	}
	
	private void propagateErrors(CompilationUnit unit, IProblem[] problems, int problemLength) {
		// resize the problem array to the proper size
		IProblem[] resizeProblems = null;
		if (problems.length == problemLength) {
			resizeProblems = problems;
		} else {
			System.arraycopy(problems, 0, (resizeProblems = new IProblem[problemLength]), 0, problemLength);
		}
		ASTSyntaxErrorPropagator syntaxErrorPropagator = new ASTSyntaxErrorPropagator(resizeProblems);
		unit.accept(syntaxErrorPropagator);
		// store the messages error on the compulation unit
		unit.setProblems(resizeProblems);
	}
	
	private void recordNodes(ASTNode node, org.eclipse.jdt.internal.compiler.ast.AstNode oldASTNode) {
		this.ast.getBindingResolver().store(node, oldASTNode);
	}
	
	/**
	 * Remove whitespaces before and after the expression.
	 */	
	private void removeExtraBlanks(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		int start = expression.sourceStart;
		int end = expression.sourceEnd;
		int token;
		int trimLeftPosition = expression.sourceStart;
		int trimRigthPosition = expression.sourceEnd;
		boolean first = true;
		Scanner removeBlankScanner = this.ast.scanner;
		try {
			removeBlankScanner.setSource(this.compilationUnitSource);
			removeBlankScanner.resetTo(start, end);
			while (true) {
				token = removeBlankScanner.getNextToken();
				switch (token) {
					case TerminalTokens.TokenNameWHITESPACE :
						if (first) {
							trimLeftPosition = removeBlankScanner.currentPosition;
						}
						trimRigthPosition = removeBlankScanner.startPosition - 1;
						break;
					case TerminalTokens.TokenNameEOF :
						expression.sourceStart = trimLeftPosition;
						expression.sourceEnd = trimRigthPosition;
						return;
					default :
						/*
						 * if we find something else than a whitespace, then we reset the trimRigthPosition
						 * to the expression source end.
						 */
						trimRigthPosition = expression.sourceEnd;
				}
				first = false;
			}
		} catch (InvalidInputException e){
		}
	}
	
	private void adjustSourcePositionsForParent(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
		int start = expression.sourceStart;
		int end = expression.sourceEnd;
		int leftParentCount = 1;
		int rightParentCount = 0;
		scanner.resetTo(start, end);
		try {
			int token = scanner.getNextToken();
			expression.sourceStart = scanner.currentPosition;
			boolean stop = false;
			while (!stop && ((token  = scanner.getNextToken()) != TerminalTokens.TokenNameEOF)) {
				switch(token) {
					case TerminalTokens.TokenNameLPAREN:
						leftParentCount++;
						break;
					case TerminalTokens.TokenNameRPAREN:
						rightParentCount++;
						if (rightParentCount == leftParentCount) {
							// we found the matching parenthesis
							stop = true;
						}
				}
			}
			expression.sourceEnd = scanner.startPosition - 1;
		} catch(InvalidInputException e) {
		}
	}

	private long[] scanAllIdentifiersPositions(int start, int end, int length) {
		scanner.resetTo(start, end);
		long[] positions = new long[length];
		int token;
		int index = 0;
		try {
			while((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				if (token == TerminalTokens.TokenNameIdentifier) {
					positions[index] = (((long) scanner.startPosition) << 32) + (scanner.currentPosition - 1);
					index++;
				}
			}
		} catch(InvalidInputException e) {
		}
		return positions;
	}
	
	private void retrieveIdentifierAndSetPositions(int start, int end, Name name) {
		scanner.resetTo(start, end);
		int token;
		try {
			while((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				if (token == TerminalTokens.TokenNameIdentifier) {
					int startName = scanner.startPosition;
					int endName = scanner.currentPosition - 1;
					name.setSourceRange(startName, endName - startName + 1);
					return;
				}
			}
		} catch(InvalidInputException e) {
		}
	}
	
	/**
	 * Remove potential trailing comment by settings the source end on the closing parenthesis
	 */
	private void removeTrailingCommentFromExpressionEndingWithAParen(ASTNode node) {
		int start = node.getStartPosition();
		scanner.resetTo(start, start + node.getLength());
		int token;
		int parenCounter = 0;
		try {
			while((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				switch(token) {
					case TerminalTokens.TokenNameLPAREN :
						parenCounter++;
						break;
					case TerminalTokens.TokenNameRPAREN :
						parenCounter--;
						if (parenCounter == 0) {
							int end = scanner.currentPosition - 1;
							node.setSourceRange(start, end - start + 1);
						}
				}
			}
		} catch(InvalidInputException e) {
		}
	}

	/**
	 * Remove potential trailing comment by settings the source end on the closing parenthesis
	 */
	private void removeLeadingAndTrailingCommentsFromLiteral(ASTNode node) {
		int start = node.getStartPosition();
		scanner.resetTo(start, start + node.getLength());
		int token;
		int startPosition = -1;
		try {
			while((token = scanner.getNextToken()) != TerminalTokens.TokenNameEOF)  {
				switch(token) {
					case TerminalTokens.TokenNameIntegerLiteral :
					case TerminalTokens.TokenNameFloatingPointLiteral :
					case TerminalTokens.TokenNameLongLiteral :
					case TerminalTokens.TokenNameDoubleLiteral :
					case TerminalTokens.TokenNameCharacterLiteral :
						if (startPosition == -1) {
							startPosition = scanner.startPosition;
						}
						int end = scanner.currentPosition;
						node.setSourceRange(startPosition, end - startPosition);
						return;
					case TerminalTokens.TokenNameMINUS :
						startPosition = scanner.startPosition;
						break;
				}
			}
		} catch(InvalidInputException e) {
		}
	}
	
	private void recordPendingThisExpressionScopeResolution(ThisExpression thisExpression) {
		if (this.pendingThisExpressionScopeResolution == null) {
			this.pendingThisExpressionScopeResolution = new HashSet();
		}
		this.pendingThisExpressionScopeResolution.add(thisExpression);
	}
	
	private void recordPendingNameScopeResolution(Name name) {
		if (this.pendingNameScopeResolution == null) {
			this.pendingNameScopeResolution = new HashSet();
		}
		this.pendingNameScopeResolution.add(name);
	}
	
	private void lookupForScopes() {
		if (this.pendingNameScopeResolution != null) {
			for (Iterator iterator = this.pendingNameScopeResolution.iterator(); iterator.hasNext(); ) {
				Name name = (Name) iterator.next();
				this.ast.getBindingResolver().recordScope(name, name.lookupScope());
			}
		}
		if (this.pendingThisExpressionScopeResolution != null) {
			for (Iterator iterator = this.pendingThisExpressionScopeResolution.iterator(); iterator.hasNext(); ) {
				ThisExpression thisExpression = (ThisExpression) iterator.next();
				this.ast.getBindingResolver().recordScope(thisExpression, thisExpression.lookupScope());
			}
		}
		
	}
	
	private InfixExpression.Operator getOperatorFor(int operatorID) {
		switch (operatorID) {
			case OperatorIds.EQUAL_EQUAL :
				return InfixExpression.Operator.EQUALS;
			case OperatorIds.LESS_EQUAL :
				return InfixExpression.Operator.LESS_EQUALS;
			case OperatorIds.GREATER_EQUAL :
				return InfixExpression.Operator.GREATER_EQUALS;
			case OperatorIds.NOT_EQUAL :
				return InfixExpression.Operator.NOT_EQUALS;
			case OperatorIds.LEFT_SHIFT :
				return InfixExpression.Operator.LEFT_SHIFT;
			case OperatorIds.RIGHT_SHIFT :
				return InfixExpression.Operator.RIGHT_SHIFT_SIGNED;
			case OperatorIds.UNSIGNED_RIGHT_SHIFT :
				return InfixExpression.Operator.RIGHT_SHIFT_UNSIGNED;
			case OperatorIds.OR_OR :
				return InfixExpression.Operator.CONDITIONAL_OR;
			case OperatorIds.AND_AND :
				return InfixExpression.Operator.CONDITIONAL_AND;
			case OperatorIds.PLUS :
				return InfixExpression.Operator.PLUS;
			case OperatorIds.MINUS :
				return InfixExpression.Operator.MINUS;
			case OperatorIds.REMAINDER :
				return InfixExpression.Operator.REMAINDER;
			case OperatorIds.XOR :
				return InfixExpression.Operator.XOR;
			case OperatorIds.AND :
				return InfixExpression.Operator.AND;
			case OperatorIds.MULTIPLY :
				return InfixExpression.Operator.TIMES;
			case OperatorIds.OR :
				return InfixExpression.Operator.OR;
			case OperatorIds.DIVIDE :
				return InfixExpression.Operator.DIVIDE;
			case OperatorIds.GREATER :
				return InfixExpression.Operator.GREATER;
			case OperatorIds.LESS :
				return InfixExpression.Operator.LESS;
		}
		return null;
	}
}


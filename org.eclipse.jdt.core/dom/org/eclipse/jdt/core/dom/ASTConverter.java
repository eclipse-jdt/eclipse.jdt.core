/*******************************************************************************
 * Copyright (c) 2001 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.jdt.core.dom;

import java.util.List;
import java.util.Locale;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

class ASTConverter {

	private AST ast;
	private char[] compilationUnitSource;
	private Scanner scanner;
	private boolean resolveBindings;
	
	public ASTConverter(boolean resolveBindings) {
		ast = new AST();
		this.resolveBindings = resolveBindings;
		if (resolveBindings) {
			ast.setBindingResolver(new DefaultBindingResolver());
		} else {
			ast.setBindingResolver(new BindingResolver());
		}
		scanner = new Scanner(true, false);
	}
	
	public CompilationUnit convert(CompilationUnitDeclaration unit, char[] source) {
		this.compilationUnitSource = source;
		scanner.setSourceBuffer(source);
		CompilationUnit compilationUnit = this.ast.newCompilationUnit();
		// handle the package declaration immediately
		// There is no node corresponding to the package declaration
		if (unit.currentPackage != null) {
			compilationUnit.setPackage(convertPackage(unit.currentPackage));
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
		return compilationUnit;
	}
	
	public PackageDeclaration convertPackage(ImportReference importReference) {
		PackageDeclaration packageDeclaration = this.ast.newPackageDeclaration();
		this.ast.getBindingResolver().store(packageDeclaration, importReference);
		// set the name (qualified or simple name is handled by newName(String[]))
		char[][] tokens = importReference.tokens;
		int length = importReference.tokens.length;
		String[] identifiers = new String[length];
		for (int index = 0; index < length; index++) {
			identifiers[index] = new String(tokens[index]);
		} 
		Name packageName = this.ast.newName(identifiers);
		
		// set the positions of each name subpart
		long[] positions = importReference.sourcePositions;
		packageName.setSourceRange((int)(positions[0]>>>32), (int)(positions[length - 1] & 0xFFFFFFFF) - (int)(positions[0]>>>32) + 1);
			// create the actual package declaration
		packageDeclaration.setName(packageName);
		packageDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationSourceEnd - importReference.declarationSourceStart + 1);
		return packageDeclaration;
	}
	
	public ImportDeclaration convertImport(ImportReference importReference) {
		ImportDeclaration importDeclaration = this.ast.newImportDeclaration();
		// set the name (qualified or simple name is handled by newName(String[]))
		char[][] tokens = importReference.tokens;
		int length = importReference.tokens.length;
		String[] identifiers = new String[length];
		for (int index = 0; index < length; index++) {
			identifiers[index] = new String(tokens[index]);
		} 
		Name packageName = this.ast.newName(identifiers);
		long[] positions = importReference.sourcePositions;
		packageName.setSourceRange((int)(positions[0]>>>32), (int)(positions[length - 1] & 0xFFFFFFFF) - (int)(positions[0]>>>32) + 1);
		// create the actual package declaration
		importDeclaration.setName(packageName);
		importDeclaration.setSourceRange(importReference.declarationSourceStart, importReference.declarationSourceEnd - importReference.declarationSourceStart + 1);
		importDeclaration.setOnDemand(importReference.onDemand);
		return importDeclaration;
	}

	public TypeDeclaration convert(org.eclipse.jdt.internal.compiler.ast.TypeDeclaration typeDeclaration) {
		TypeDeclaration typeDecl = this.ast.newTypeDeclaration();
		if (resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
		}
		typeDecl.setModifiers(typeDeclaration.modifiers & ~0x200); // remove AccInterface: 0x200
		typeDecl.setInterface(typeDeclaration.isInterface());
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(this.ast.newSimpleName(new String(typeDeclaration.name)));
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.declarationSourceEnd - typeDeclaration.declarationSourceStart + 1);
		
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
		MemberTypeDeclaration[] members = typeDeclaration.memberTypes;
		if (members != null) {
			for (int i = 0, length = members.length; i < length; i++) {
				typeDecl.bodyDeclarations().add(convert(members[i]));
			}
		}
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = typeDeclaration.fields;
		if (fields != null) {
			int fieldsLength = fields.length;
			for (int i = 0; i < fieldsLength; i++) {
				checkAndAddMultipleFieldDeclaration(fields, i, typeDecl.bodyDeclarations());
			}
		}
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		if (methods != null) {
			int methodsLength = methods.length;
			for (int i = 0; i < methodsLength; i++) {
				if (!methods[i].isDefaultConstructor() && !methods[i].isClinit()) {
					typeDecl.bodyDeclarations().add(convert(methods[i]));
				}
			}
		}
		setJavaDocComment(typeDecl);
		return typeDecl;
	}
	
	private void checkAndAddMultipleFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields, int index, List bodyDeclarations) {
		if (fields[index] instanceof org.eclipse.jdt.internal.compiler.ast.Initializer) {
			org.eclipse.jdt.internal.compiler.ast.Initializer oldInitializer = (org.eclipse.jdt.internal.compiler.ast.Initializer) fields[index];
			Initializer initializer = this.ast.newInitializer();
			initializer.setBody(convert(oldInitializer.block));
			initializer.setModifiers(oldInitializer.modifiers);
			initializer.setSourceRange(oldInitializer.declarationSourceStart, oldInitializer.declarationSourceEnd - oldInitializer.declarationSourceStart + 1);
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
			String[] identifiers = new String[length];
			for (int index = 0; index < length; index++) {
				identifiers[index] = new String(typeName[index]);
			}
			Name name = this.ast.newName(identifiers);		
			if (typeReference instanceof QualifiedTypeReference) {
				long[] positions = ((QualifiedTypeReference) typeReference).sourcePositions;
				name.setSourceRange((int)(positions[0]>>>32), (int)(positions[length - 1] & 0xFFFFFFFF) - (int)(positions[0]>>>32) + 1);
			} else {
				name.setSourceRange(typeReference.sourceStart, typeReference.sourceEnd - typeReference.sourceStart + 1);
			}
			return name;
	}

	public Name convert(SingleNameReference nameReference) {
			Name name = this.ast.newName(new String[] {new String(nameReference.token)});		
			if (this.resolveBindings) {
				recordNodes(name, nameReference);
			}
			name.setSourceRange(nameReference.sourceStart, nameReference.sourceEnd - nameReference.sourceStart + 1);
			return name;
	}

	public Name convert(QualifiedNameReference nameReference) {
			char[][] typeName = nameReference.tokens;
			int length = typeName.length;
			String[] identifiers = new String[length];
			for (int index = 0; index < length; index++) {
				identifiers[index] = new String(typeName[index]);
			}
			Name name = this.ast.newName(identifiers);
			if (this.resolveBindings) {
				recordNodes(name, nameReference);
			}
			name.setSourceRange(nameReference.sourceStart, nameReference.sourceEnd - nameReference.sourceStart + 1);
			return name;
	}

	public Expression convert(ThisReference reference) {
		if (reference instanceof QualifiedThisReference) {
			return convert((QualifiedThisReference) reference);
		}
		if (reference instanceof QualifiedSuperReference) {
			return convert((QualifiedSuperReference) reference);
		}
		if (reference == ThisReference.ThisImplicit) {
			// There is no source associated with an implicit this
			return null;
		} else {
			ThisExpression thisExpression = this.ast.newThisExpression();
			if (this.resolveBindings) {
				recordNodes(thisExpression, reference);
			}
			thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
			return thisExpression;
		}
	}

	public ThisExpression convert(QualifiedThisReference reference) {
		ThisExpression thisExpression = this.ast.newThisExpression();
			if (this.resolveBindings) {
				recordNodes(thisExpression, reference);
			}
		thisExpression.setSourceRange(reference.sourceStart, reference.sourceEnd - reference.sourceStart + 1);
		thisExpression.setQualifier(convert(reference.qualification));
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
				superFieldAccess.setQualifier(convert((QualifiedSuperReference) reference.receiver));
			}
			SimpleName simpleName = this.ast.newSimpleName(new String(reference.token)); 
			int sourceStart = (int)(reference.nameSourcePosition>>>32);
			int length = (int)(reference.nameSourcePosition & 0xFFFFFFFF) - sourceStart + 1;
			simpleName.setSourceRange(sourceStart, length);
			superFieldAccess.setName(simpleName);
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
		if (this.resolveBindings) {
			recordNodes(typeDecl, typeDeclaration);
		}
		typeDecl.setModifiers(typeDeclaration.modifiers);
		typeDecl.setInterface(typeDeclaration.isInterface());
		SimpleName typeName = this.ast.newSimpleName(new String(typeDeclaration.name));
		typeName.setSourceRange(typeDeclaration.sourceStart, typeDeclaration.sourceEnd - typeDeclaration.sourceStart + 1);
		typeDecl.setName(this.ast.newSimpleName(new String(typeDeclaration.name)));
		typeDecl.setSourceRange(typeDeclaration.declarationSourceStart, typeDeclaration.declarationSourceEnd - typeDeclaration.declarationSourceStart + 1);
		
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
		MemberTypeDeclaration[] members = typeDeclaration.memberTypes;
		if (members != null) {
			for (int i = 0, length = members.length; i < length; i++) {
				typeDecl.bodyDeclarations().add(convert(members[i]));
			}
		}
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fields[] = typeDeclaration.fields;
		if (fields != null) {
			int fieldsLength = fields.length;
			for (int i = 0; i < fieldsLength; i++) {
				checkAndAddMultipleFieldDeclaration(fields, i, typeDecl.bodyDeclarations());
			}
		}
		AbstractMethodDeclaration[] methods = typeDeclaration.methods;
		if (methods != null) {
			int methodsLength = methods.length;
			for (int i = 0; i < methodsLength; i++) {
				if (!methods[i].isDefaultConstructor() && !methods[i].isClinit()) {
					typeDecl.bodyDeclarations().add(convert(methods[i]));
				}
			}
		}
		setJavaDocComment(typeDecl);
		return typeDecl;
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
					PrimitiveType primitiveType = this.ast.newPrimitiveType(getPrimitiveTypeCode(name));
					primitiveType.setSourceRange(sourceStart, length);
					type = this.ast.newArrayType(primitiveType, dimensions);
				} else {
					SimpleName simpleName = this.ast.newSimpleName(new String(name));
					simpleName.setSourceRange(sourceStart, length);
					SimpleType simpleType = this.ast.newSimpleType(simpleName);
					simpleType.setSourceRange(sourceStart, length);
					type = this.ast.newArrayType(simpleType, dimensions);
				}
			} else {
				if (isPrimitiveType(name)) {
					type = this.ast.newPrimitiveType(getPrimitiveTypeCode(name));
				} else {
					SimpleName simpleName = this.ast.newSimpleName(new String(name));
					simpleName.setSourceRange(sourceStart, length);
					type = this.ast.newSimpleType(simpleName);
				}
			}
		} else {
			char[][] name = ((QualifiedTypeReference) typeReference).getTypeName();
			int nameLength = name.length;
			String[] identifiers = new String[nameLength];
			for (int index = 0; index < nameLength; index++) {
				identifiers[index] = new String(name[index]);
			}
			long[] positions = ((QualifiedTypeReference) typeReference).sourcePositions;
			sourceStart = (int)(positions[0]>>>32);
			length = (int)(positions[nameLength - 1] & 0xFFFFFFFF) - sourceStart + 1;
			if (dimensions != 0) {
				// need to find out if this is an array type of primitive types or not
				Name qualifiedName = this.ast.newName(identifiers);
				qualifiedName.setSourceRange(sourceStart, length);
				SimpleType simpleType = this.ast.newSimpleType(qualifiedName);
				simpleType.setSourceRange(sourceStart, length);
				type = this.ast.newArrayType(simpleType, dimensions);
			} else {
				Name qualifiedName = this.ast.newName(identifiers);
				qualifiedName.setSourceRange(sourceStart, length);
				type = this.ast.newSimpleType(this.ast.newName(identifiers));
			}
		}
		if (this.resolveBindings) {
			this.recordNodes(type, typeReference);
		}
		type.setSourceRange(sourceStart, length);
		return type;
	}
		
	public MethodDeclaration convert(AbstractMethodDeclaration methodDeclaration) {
		MethodDeclaration methodDecl = this.ast.newMethodDeclaration();
		if (this.resolveBindings) {
			recordNodes(methodDecl, methodDeclaration);
		}
		methodDecl.setModifiers(methodDeclaration.modifiers);
		boolean isConstructor = methodDeclaration.isConstructor();
		methodDecl.setConstructor(isConstructor);
		SimpleName methodName = this.ast.newSimpleName(new String(methodDeclaration.selector));
		methodName.setSourceRange(methodDeclaration.sourceStart, methodDeclaration.sourceEnd - methodDeclaration.sourceStart + 1);
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
		if (isConstructor) {
			// set the return type to VOID
			methodDecl.setReturnType(this.ast.newPrimitiveType(PrimitiveType.VOID));
		} else {
			org.eclipse.jdt.internal.compiler.ast.MethodDeclaration method = (org.eclipse.jdt.internal.compiler.ast.MethodDeclaration) methodDeclaration;
			methodDecl.setReturnType(convertType(method.returnType));
		}
		methodDecl.setSourceRange(methodDeclaration.declarationSourceStart, methodDeclaration.declarationSourceEnd - methodDeclaration.declarationSourceStart + 1);
		
		org.eclipse.jdt.internal.compiler.ast.Statement[] statements = methodDeclaration.statements;
		if (statements != null) {
			Block block = this.ast.newBlock();
			block.setSourceRange(methodDeclaration.bodyStart, methodDeclaration.bodyEnd - methodDeclaration.bodyStart + 1);
			int statementsLength = statements.length;
			for (int i = 0; i < statementsLength; i++) {
				if (statements[i] instanceof LocalDeclaration) {
					checkAndAddMultipleLocalDeclaration(statements, i, block.statements());
				} else {
					block.statements().add(convert(statements[i]));
				}
			}
			methodDecl.setBody(block);
		} else if (!methodDeclaration.isNative() && !methodDeclaration.isAbstract()) {
			methodDecl.setBody(this.ast.newBlock());
		}
		setJavaDocComment(methodDecl);
		return methodDecl;
	}	

	public Expression convert(org.eclipse.jdt.internal.compiler.ast.Expression expression) {
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
		if (expression instanceof org.eclipse.jdt.internal.compiler.ast.CastExpression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.CastExpression) expression);
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
		if (expression instanceof InstanceOfExpression) {
			return convert((InstanceOfExpression) expression);
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
		return classInstanceCreation;
	}
	
	public ClassInstanceCreation convert(AnonymousLocalTypeDeclaration expression) {
		ClassInstanceCreation classInstanceCreation = this.ast.newClassInstanceCreation();
		if (this.resolveBindings) {
			recordNodes(classInstanceCreation, expression);
		}
		classInstanceCreation.setName(convert(expression.allocation.type));
		if (expression.allocation.enclosingInstance != null) {
			classInstanceCreation.setExpression(convert(expression.allocation.enclosingInstance));
		}
		int declarationSourceStart = expression.allocation.sourceStart;
		classInstanceCreation.setSourceRange(declarationSourceStart, expression.declarationSourceEnd - declarationSourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = expression.allocation.arguments;
		if (arguments != null) {
			int length = arguments.length;
			for (int i = 0; i < length; i++) {
				classInstanceCreation.arguments().add(convert(arguments[i]));
			}
		}
		int fieldsLength;
		int methodsLength;
		int memberTypesLength;

		// <superclass> is bound to the actual type from the allocation expression
		// therefore it has already been iterated at this point.
		MemberTypeDeclaration[] memberTypes = expression.memberTypes;
		if (memberTypes != null) {
			memberTypesLength = memberTypes.length;
			for (int i = 0; i < memberTypesLength; i++) {
				classInstanceCreation.bodyDeclarations().add(convert(memberTypes[i]));
			}
		}
		org.eclipse.jdt.internal.compiler.ast.FieldDeclaration[] fields = expression.fields;
		if (fields != null) {
			fieldsLength = fields.length;
			for (int i = 0; i < fieldsLength; i++) {
				checkAndAddMultipleFieldDeclaration(fields, i, classInstanceCreation.bodyDeclarations());
			}
		}
		org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration[] methods = expression.methods;
		if (methods != null) {
			methodsLength = methods.length;
			for (int i = 0; i < methodsLength; i++) {
				if (!methods[i].isDefaultConstructor() && !methods[i].isClinit()) {
					classInstanceCreation.bodyDeclarations().add(convert(methods[i]));
				}
			}
		}
		classInstanceCreation.setAnonymousClassDeclaration(true);
		return classInstanceCreation;
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
		ArrayType arrayType = null;
		if (type.isArrayType()) {
			arrayType = (ArrayType) type;
		} else {
			arrayType = this.ast.newArrayType(type, dimensionsLength);
		}
		arrayCreation.setType(arrayType);
		if (expression.initializer != null) {
			arrayCreation.setInitializer(convert(expression.initializer));
		}
		if (expression.initializer != null) {
			arrayCreation.setInitializer(convert(expression.initializer));
		}
		return arrayCreation;
	}

	public SingleVariableDeclaration convert(Argument argument) {
		SingleVariableDeclaration variableDecl = this.ast.newSingleVariableDeclaration();
		variableDecl.setModifiers(argument.modifiers);
		SimpleName name = this.ast.newSimpleName(argument.name());
		name.setSourceRange(argument.sourceStart, argument.sourceEnd - argument.sourceStart + 1);
		variableDecl.setName(name);
		variableDecl.setType(convertType(argument.type));
		variableDecl.setSourceRange(argument.declarationSourceStart, argument.declarationSourceEnd - argument.declarationSourceStart + 1);
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
			return classInstanceCreation;
		}
	}
	
	public Assignment convert(org.eclipse.jdt.internal.compiler.ast.Assignment expression) {
		Assignment assignment = this.ast.newAssignment();
		if (this.resolveBindings) {
			recordNodes(assignment, expression);
		}
		assignment.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		assignment.setLeftHandSide(convert(expression.lhs));
		assignment.setOperator(Assignment.Operator.ASSIGN);
		assignment.setRightHandSide(convert(expression.expression));
		return assignment;
	}

	public Assignment convert(CompoundAssignment expression) {
		Assignment assignment = this.ast.newAssignment();
		assignment.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		assignment.setLeftHandSide(convert(expression.lhs));
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
		};
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
			case org.eclipse.jdt.internal.compiler.ast.PrefixExpression.PLUS :
				prefixExpression.setOperator(PrefixExpression.Operator.INCREMENT);
				break;
			case org.eclipse.jdt.internal.compiler.ast.PrefixExpression.MINUS :
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
			case org.eclipse.jdt.internal.compiler.ast.PostfixExpression.PLUS :
				postfixExpression.setOperator(PostfixExpression.Operator.INCREMENT);
				break;
			case org.eclipse.jdt.internal.compiler.ast.PostfixExpression.MINUS :
				postfixExpression.setOperator(PostfixExpression.Operator.DECREMENT);
				break;
		}
		return postfixExpression;
	}

	public CastExpression convert(org.eclipse.jdt.internal.compiler.ast.CastExpression expression) {
		CastExpression castExpression = this.ast.newCastExpression();
		castExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Expression type = expression.type;
		if (type instanceof TypeReference ) {
			castExpression.setType(convertType((TypeReference)expression.type));
		} else if (type instanceof NameReference) {
			castExpression.setType(convertToType((NameReference)expression.type));
		}
		castExpression.setExpression(convert(expression.expression));
		return castExpression;
	}
		
	public Type convertToType(NameReference reference) {
		SimpleType type = this.ast.newSimpleType(convert(reference));
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
	
	public InfixExpression convert(BinaryExpression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			this.recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);

		int expressionOperatorID = (expression.bits & OperatorExpression.OperatorMASK) >> OperatorExpression.OperatorSHIFT;
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
		};
		
		if (expression.left instanceof BinaryExpression) {
			// create an extended string literal equivalent => use the extended operands list
			infixExpression.extendedOperands().add(convert(expression.right));
			org.eclipse.jdt.internal.compiler.ast.Expression leftOperand = expression.left;
			org.eclipse.jdt.internal.compiler.ast.Expression rightOperand = null;
			do {
				if (((leftOperand.bits & OperatorExpression.OperatorMASK) >> OperatorExpression.OperatorSHIFT) != expressionOperatorID) {
					infixExpression.extendedOperands().clear();
					infixExpression.setLeftOperand(convert(expression.left));
					infixExpression.setRightOperand(convert(expression.right));
					return infixExpression;
				}
				rightOperand = ((BinaryExpression) leftOperand).right;
				infixExpression.extendedOperands().add(0, convert(rightOperand));
				leftOperand = ((BinaryExpression) leftOperand).left;
			} while (leftOperand instanceof BinaryExpression);
			// check that the right operand wasn't a BinaryExpression
			if (rightOperand instanceof BinaryExpression) {
				infixExpression.extendedOperands().clear();
				infixExpression.setLeftOperand(convert(expression.left));
				infixExpression.setRightOperand(convert(expression.right));
				return infixExpression;
			} else {
				infixExpression.setLeftOperand(convert(leftOperand));
				infixExpression.setRightOperand((Expression)infixExpression.extendedOperands().remove(0));
				return infixExpression;
			}
		}		
		infixExpression.setLeftOperand(convert(expression.left));
		infixExpression.setRightOperand(convert(expression.right));
		return infixExpression;
	}
			
	public PrefixExpression convert(UnaryExpression expression) {
		PrefixExpression prefixExpression = this.ast.newPrefixExpression();
		if (this.resolveBindings) {
			this.recordNodes(prefixExpression, expression);
		}
		prefixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		prefixExpression.setOperand(convert(expression.expression));
		switch ((expression.bits & UnaryExpression.OperatorMASK) >> UnaryExpression.OperatorSHIFT) {
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
	
	public InfixExpression convert(InstanceOfExpression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		infixExpression.setLeftOperand(convert(expression.expression));
		infixExpression.setRightOperand(convert(expression.type));
		infixExpression.setOperator(InfixExpression.Operator.INSTANCEOF);
		return infixExpression;
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
			superMethodInvocation.setName(name);
			// expression.receiver is either a QualifiedSuperReference or a SuperReference
			// so the casting cannot fail
			if (expression.receiver instanceof QualifiedSuperReference) {
				superMethodInvocation.setQualifier(convert((QualifiedSuperReference) expression.receiver));
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
			methodInvocation.setExpression(convert(expression.receiver));
			expr = methodInvocation;
		}
		expr.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);	
		return expr;
	}

	public Expression convert(AND_AND_Expression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		infixExpression.setLeftOperand(convert(expression.left));
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_AND);
		return infixExpression;
	
	}
	
	public Expression convert(EqualExpression expression) {
		InfixExpression infixExpression = this.ast.newInfixExpression();
		if (this.resolveBindings) {
			recordNodes(infixExpression, expression);
		}
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		infixExpression.setLeftOperand(convert(expression.left));
		infixExpression.setRightOperand(convert(expression.right));
		switch ((expression.bits & OperatorExpression.OperatorMASK) >> OperatorExpression.OperatorSHIFT) {
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
		infixExpression.setSourceRange(expression.sourceStart, expression.sourceEnd - expression.sourceStart + 1);
		infixExpression.setLeftOperand(convert(expression.left));
		infixExpression.setRightOperand(convert(expression.right));
		infixExpression.setOperator(InfixExpression.Operator.CONDITIONAL_OR);
		return infixExpression;
	
	}

	public Statement convert(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof LocalDeclaration) {
			throw new IllegalArgumentException("Should not be reached: convert(" + statement.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.AssertStatement) {
			return convert((org.eclipse.jdt.internal.compiler.ast.AssertStatement) statement);
		}
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Block) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Block) statement);
		}
		if (statement instanceof Break) {
			return convert((Break) statement);
		}
		if (statement instanceof Continue) {
			return convert((Continue) statement);
		}
		if (statement instanceof Case) {
			return convert((Case) statement);
		}
		if (statement instanceof DefaultCase) {
			return convert((DefaultCase) statement);
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
		assertStatement.setExpression(convert(statement.assertExpression));
		assertStatement.setMessage(convert(statement.exceptionArgument));
		assertStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
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
	
	public BreakStatement convert(Break statement)  {
		BreakStatement breakStatement = this.ast.newBreakStatement();
		breakStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			breakStatement.setLabel(this.ast.newSimpleName(new String(statement.label)));
		}
		return breakStatement;
	}

	public ContinueStatement convert(Continue statement)  {
		ContinueStatement continueStatement = this.ast.newContinueStatement();
		continueStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		if (statement.label != null) {
			continueStatement.setLabel(this.ast.newSimpleName(new String(statement.label)));
		}
		return continueStatement;
	}
		
		
	public SwitchCase convert(Case statement) {
		SwitchCase switchCase = this.ast.newSwitchCase();
		switchCase.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		switchCase.setExpression(convert(statement.constantExpression));
		return switchCase;
	}
	
	public SwitchCase convert(DefaultCase statement) {
		SwitchCase switchCase = this.ast.newSwitchCase();
		switchCase.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		return switchCase;
	}
	
	public DoStatement convert(org.eclipse.jdt.internal.compiler.ast.DoStatement statement) {
		DoStatement doStatement = this.ast.newDoStatement();
		doStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		doStatement.setExpression(convert(statement.condition));
		if (statement.action != null) {
			doStatement.setBody(convert(statement.action));
		}
		return doStatement;
	}
	
	public EmptyStatement convert(org.eclipse.jdt.internal.compiler.ast.EmptyStatement statement) {
		EmptyStatement emptyStatement = this.ast.newEmptyStatement();
		emptyStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		return emptyStatement;
	}
	
	public Statement convert(ExplicitConstructorCall statement) {
		Statement newStatement;
		if (statement.isSuperAccess() || statement.isSuper() || statement.isImplicitSuper()) {
			SuperConstructorInvocation superConstructorInvocaTion = this.ast.newSuperConstructorInvocation();
			if (statement.qualification != null) {
				superConstructorInvocaTion.setExpression(convert(statement.qualification));
			}
			org.eclipse.jdt.internal.compiler.ast.Expression[] arguments = statement.arguments;
			if (arguments != null) {
				int length = arguments.length;
				for (int i = 0; i < length; i++) {
					superConstructorInvocaTion.arguments().add(convert(arguments[i]));
				}
			}
			newStatement = superConstructorInvocaTion;
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
		return newStatement;
	}
	
	public ForStatement convert(org.eclipse.jdt.internal.compiler.ast.ForStatement statement) {
		ForStatement forStatement = this.ast.newForStatement();
		forStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		org.eclipse.jdt.internal.compiler.ast.Statement[] initializations = statement.initializations;
		if (initializations != null) {
			int initializationsLength = initializations.length;
			for (int i = 0; i < initializationsLength; i++) {
				forStatement.initializers().add(convertToExpression(initializations[i]));
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

		if (statement.action != null) {
			forStatement.setBody(convert(statement.action));
		} else {
			retrieveSemiColonPosition(forStatement);
		}
		return forStatement;
	}
	
	public Expression convertToExpression(org.eclipse.jdt.internal.compiler.ast.Statement statement) {
		if (statement instanceof org.eclipse.jdt.internal.compiler.ast.Expression) {
			return convert((org.eclipse.jdt.internal.compiler.ast.Expression) statement);
		} else if (statement instanceof LocalDeclaration) {
			LocalDeclaration localDeclaration = (LocalDeclaration) statement;
			VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
			SimpleName name = this.ast.newSimpleName(localDeclaration.name());
			name.setSourceRange(localDeclaration.sourceStart, localDeclaration.sourceEnd - localDeclaration.sourceStart + 1);
			variableDeclarationFragment.setName(name);
			variableDeclarationFragment.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
			if (localDeclaration.initialization != null) {
				variableDeclarationFragment.setInitializer(convert(localDeclaration.initialization));
			}
			VariableDeclarationExpression variableDeclarationExpression = this.ast.newVariableDeclarationExpression(variableDeclarationFragment);
			if (this.resolveBindings) {
				recordNodes(variableDeclarationFragment, localDeclaration);
			}
			variableDeclarationExpression.setSourceRange(localDeclaration.declarationSourceStart, localDeclaration.declarationSourceEnd - localDeclaration.declarationSourceStart + 1);
			variableDeclarationExpression.setModifiers(localDeclaration.modifiers);
			variableDeclarationFragment.setExtraDimensions(retrieveExtraDimension(localDeclaration.sourceEnd + 1, this.compilationUnitSource.length));
			Type type = convertType(localDeclaration.type);
			setTypeForVariableDeclarationExpression(variableDeclarationExpression, type, variableDeclarationFragment.getExtraDimensions());
			return variableDeclarationExpression;
		} else {
			// unsupported
			throw new IllegalArgumentException("Not yet implemented: convert(" + statement.getClass() + ")");//$NON-NLS-1$//$NON-NLS-2$
		}
	}
	
	public IfStatement convert(org.eclipse.jdt.internal.compiler.ast.IfStatement statement) {
		IfStatement ifStatement = this.ast.newIfStatement();
		ifStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);
		ifStatement.setExpression(convert(statement.condition));
		org.eclipse.jdt.internal.compiler.ast.Statement thenStatement = statement.thenStatement;
		org.eclipse.jdt.internal.compiler.ast.Statement elseStatement = statement.elseStatement;
		if (thenStatement != null) {
			if (thenStatement == org.eclipse.jdt.internal.compiler.ast.Block.None) {
				ifStatement.setThenStatement(this.ast.newEmptyStatement());
			} else {
				ifStatement.setThenStatement(convert(statement.thenStatement));
			}
			if (!(ifStatement.getThenStatement() instanceof Block) && elseStatement == null) {
				retrieveSemiColonPosition(ifStatement);
			}				
		}
		if (elseStatement != null) {
			if (elseStatement == org.eclipse.jdt.internal.compiler.ast.Block.None) {
				ifStatement.setElseStatement(this.ast.newEmptyStatement());
			} else {
				ifStatement.setElseStatement(convert(elseStatement));
				if (!(ifStatement.getElseStatement() instanceof Block)) {
					retrieveSemiColonPosition(ifStatement);
				}
			}
		}
		return ifStatement;
	}
	
	public LabeledStatement convert(org.eclipse.jdt.internal.compiler.ast.LabeledStatement statement) {
		LabeledStatement labeledStatement = this.ast.newLabeledStatement();
		labeledStatement.setSourceRange(statement.sourceStart, statement.sourceEnd - statement.sourceStart + 1);	
		labeledStatement.setBody(convert(statement.statement));
		labeledStatement.setLabel(this.ast.newSimpleName(new String(statement.label)));
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
			for (int i = 0; i < catchArgumentsLength; i++) {
				CatchClause catchClause = this.ast.newCatchClause();
				catchClause.setSourceRange(catchArguments[i].sourceStart, catchBlocks[i].sourceEnd - catchArguments[i].sourceStart + 1);	
				catchClause.setBody(convert(catchBlocks[i]));
				catchClause.setException(convert(catchArguments[i]));
				tryStatement.catchClauses().add(catchClause);
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
		if (statement.action != null) {
			whileStatement.setBody(convert(statement.action));
		}
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
		scanner.resetTo(end, this.compilationUnitSource.length);
		try {
			int token;
			while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
				switch(token) {
					case Scanner.TokenNameSEMICOLON:
						node.setSourceRange(start, scanner.currentPosition - start);
						return;
				}
			}
			node.setSourceRange(start, scanner.currentPosition - start);
		} catch(InvalidInputException e) {
		}
	}

	/**
	 * This method is used to set the right end position for switch and 
	 * try statements. They don't include the close }. 
	 */
	private void retrieveRightBracePosition(ASTNode node) {
		int start = node.getStartPosition();
		int length = node.getLength();
		int end = start + length;
		scanner.resetTo(end, this.compilationUnitSource.length);
		try {
			int token;
			while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
				switch(token) {
					case Scanner.TokenNameRBRACE:
						node.setSourceRange(start, scanner.currentPosition - start);
						return;
				}
			}
		} catch(InvalidInputException e) {
		}
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
			while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
				switch(token) {
					case Scanner.TokenNameRBRACKET://166 
						dimensions++;
						break;
					case Scanner.TokenNameCOMMA ://90
					case Scanner.TokenNameEQUAL ://167
					case Scanner.TokenNameSEMICOLON ://64
					case Scanner.TokenNameRPAREN : //86
						return dimensions;
				}
			}
		} catch(InvalidInputException e) {
		}
		return dimensions;
	}

	/**
	 * This method is used to retrieve position before the next comma or semi-colon.
	 * @return int the position found.
	 */
	private int retrievePositionBeforeNextCommaOrSemiColon(int start, int end) {
		scanner.resetTo(start, end);
		try {
			int token;
			while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
				switch(token) {
					case Scanner.TokenNameCOMMA :
					case Scanner.TokenNameSEMICOLON :
						return scanner.startPosition - 1;
				}
			}
		} catch(InvalidInputException e) {
		}
		return -1;
	}

	private VariableDeclarationFragment convertToVariableDeclarationFragment(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDeclaration);
		}
		SimpleName name = this.ast.newSimpleName(fieldDeclaration.name());
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
		return variableDeclarationFragment;
	}

	private VariableDeclarationFragment convertToVariableDeclarationFragment(LocalDeclaration localDeclaration) {
		VariableDeclarationFragment variableDeclarationFragment = this.ast.newVariableDeclarationFragment();
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, localDeclaration);
		}
		SimpleName name = this.ast.newSimpleName(localDeclaration.name());
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
		return variableDeclarationFragment;
	}

	private FieldDeclaration convertToFieldDeclaration(org.eclipse.jdt.internal.compiler.ast.FieldDeclaration fieldDecl) {
		VariableDeclarationFragment variableDeclarationFragment = convertToVariableDeclarationFragment(fieldDecl);
		FieldDeclaration fieldDeclaration = this.ast.newFieldDeclaration(variableDeclarationFragment);
		if (this.resolveBindings) {
			recordNodes(variableDeclarationFragment, fieldDecl);
		}
		fieldDeclaration.setSourceRange(fieldDecl.declarationSourceStart, fieldDecl.declarationSourceEnd - fieldDecl.declarationSourceStart + 1);
		Type type = convertType(fieldDecl.type);
		setTypeForField(fieldDeclaration, type, variableDeclarationFragment.getExtraDimensions());
		fieldDeclaration.setModifiers(fieldDecl.modifiers);
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
		variableDeclarationStatement.setModifiers(localDeclaration.modifiers);
		retrieveSemiColonPosition(variableDeclarationStatement);
		return variableDeclarationStatement;
	}
	
	private void setTypeForField(FieldDeclaration fieldDeclaration, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				Type elementType = (Type) ASTNode.copySubtree(this.ast, arrayType.getElementType());
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					fieldDeclaration.setType(elementType);
				} else {
					fieldDeclaration.setType(this.ast.newArrayType(elementType, remainingDimensions));
				}
			} else {
				fieldDeclaration.setType(type);
			}
		} else {
			fieldDeclaration.setType(type);
		}
	}

	private void setTypeForVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement, Type type, int extraDimension) {
		if (extraDimension != 0) {
			if (type.isArrayType()) {
				ArrayType arrayType = (ArrayType) type;
				int remainingDimensions = arrayType.getDimensions() - extraDimension;
				Type elementType = (Type) ASTNode.copySubtree(this.ast, arrayType.getElementType());
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					variableDeclarationStatement.setType(elementType);
				} else {
					variableDeclarationStatement.setType(this.ast.newArrayType(elementType, remainingDimensions));
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
				Type elementType = (Type) ASTNode.copySubtree(this.ast, arrayType.getElementType());				
				if (remainingDimensions == 0)  {
					// the dimensions are after the name so the type of the fieldDeclaration is a simpleType
					variableDeclarationExpression.setType(elementType);
				} else {
					variableDeclarationExpression.setType(this.ast.newArrayType(elementType, remainingDimensions));
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
			while ((token = scanner.getNextToken()) != Scanner.TokenNameEOF) {
				switch(token) {
					case Scanner.TokenNameCOMMENT_JAVADOC: //1003
						Javadoc javadocComment = this.ast.newJavadoc();
						int start = scanner.startPosition;
						int length = scanner.currentPosition - start;
						char[] contents = new char[length];
						System.arraycopy(this.compilationUnitSource, start, contents, 0, length);
						javadocComment.setComment(new String(contents));
						javadocComment.setSourceRange(start, length);
						bodyDeclaration.setJavadoc(javadocComment);
						return;
				}
			}
		} catch(InvalidInputException e) {
		}
	}
	
	private void propagateErrors(CompilationUnit unit, IProblem[] problems, int problemLength) {
		for (int n = 0; n < problemLength; n++) {
			int position = problems[n].getSourceStart();
				//check the package declaration
			PackageDeclaration packageDeclaration = unit.getPackage();
			if (packageDeclaration != null && checkAndTagAsMalformed(packageDeclaration, position)) {
				return;
			}
			List imports = unit.imports();
			for (int i = 0, length = imports.size(); i < length; i++) {
				if (checkAndTagAsMalformed((ASTNode) imports.get(i), position)) {
					return;
				}
			}
			for (int i = 0, max = unit.types().size(); i < max; i++) {
				TypeDeclaration typeDeclaration = (TypeDeclaration) unit.types().get(0);
				for (int j = 0, max2 = typeDeclaration.bodyDeclarations().size();
					j < max2;
					j++) {
					if (checkAndTagAsMalformed((ASTNode) typeDeclaration.bodyDeclarations().get(j), position)) {
						return;
					}
				}
				if (checkAndTagAsMalformed(typeDeclaration, position)) {
					return;
				}
			}
		}
		// if we get there, then we cannot do better than tag the compilation unit as being malformed
		unit.setFlags(ASTNode.MALFORMED);
	}
	
	private boolean checkAndTagAsMalformed(ASTNode node, int position) {
		int start = node.getStartPosition();
		int end = start + node.getLength();
		if ((start <= position) && (position <= end)) {
			node.setFlags(ASTNode.MALFORMED);
			return true;
		}
		return false;
	}
	
	private void recordNodes(ASTNode node, org.eclipse.jdt.internal.compiler.ast.AstNode oldASTNode) {
		this.ast.getBindingResolver().store(node, oldASTNode);
	}
}


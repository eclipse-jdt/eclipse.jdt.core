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
package org.eclipse.jdt.internal.compiler;

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.util.CommentRecorderParser;

/**
 * A source element parser extracts structural and reference information
 * from a piece of source.
 *
 * also see @ISourceElementRequestor
 *
 * The structural investigation includes:
 * - the package statement
 * - import statements
 * - top-level types: package member, member types (member types of member types...)
 * - fields
 * - methods
 *
 * If reference information is requested, then all source constructs are
 * investigated and type, field & method references are provided as well.
 *
 * Any (parsing) problem encountered is also provided.
 */
public class SourceElementParser extends CommentRecorderParser {
	
	ISourceElementRequestor requestor;
	int fieldCount;
	ISourceType sourceType;
	boolean reportReferenceInfo;
	char[][] typeNames;
	char[][] superTypeNames;
	int nestedTypeIndex;
	static final char[] JAVA_LANG_OBJECT = "java.lang.Object".toCharArray(); //$NON-NLS-1$
	NameReference[] unknownRefs;
	int unknownRefsCounter;
	LocalDeclarationVisitor localDeclarationVisitor = null;
	CompilerOptions options;
	
/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends ASTVisitor {
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null);
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null);
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	
}

public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory,
	CompilerOptions options) {
	// we want to notify all syntax error with the acceptProblem API
	// To do so, we define the record method of the ProblemReporter
	super(new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		options, 
		problemFactory) {
		public void record(IProblem problem, CompilationResult unitResult, ReferenceContext context) {
			unitResult.record(problem, context); // TODO (jerome) clients are trapping problems either through factory or requestor... is result storing needed?
			requestor.acceptProblem(problem);
		}
	},
	true);
	this.requestor = requestor;
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
	this.options = options;
}

public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory,
	CompilerOptions options,
	boolean reportLocalDeclarations) {
		this(requestor, problemFactory, options);
		if (reportLocalDeclarations) {
			this.localDeclarationVisitor = new LocalDeclarationVisitor();
		}
}

public void checkComment() {
	if (this.currentElement != null && this.scanner.commentPtr >= 0) {
		flushCommentsDefinedPriorTo(this.endStatementPosition); // discard obsolete comments during recovery
	}
	
	int lastComment = this.scanner.commentPtr;
	
	if (this.modifiersSourceStart >= 0) {
		// eliminate comments located after modifierSourceStart if positionned
		while (lastComment >= 0 && Math.abs(this.scanner.commentStarts[lastComment]) > this.modifiersSourceStart) lastComment--;
	}
	if (lastComment >= 0) {
		// consider all remaining leading comments to be part of current declaration
		this.modifiersSourceStart = Math.abs(this.scanner.commentStarts[0]); 
	
		// check deprecation in last comment if javadoc (can be followed by non-javadoc comments which are simply ignored)	
		while (lastComment >= 0 && this.scanner.commentStops[lastComment] < 0) lastComment--; // non javadoc comment have negative end positions
		if (lastComment >= 0 && this.javadocParser != null) {
			if (this.javadocParser.checkDeprecation(
					this.scanner.commentStarts[lastComment],
					this.scanner.commentStops[lastComment] - 1)) { //stop is one over,
				checkAndSetModifiers(AccDeprecated);
			}
			this.javadoc = this.javadocParser.docComment;	// null if check javadoc is not activated 
		}
	}

	if (this.reportReferenceInfo && this.javadocParser.checkDocComment && this.javadoc != null) {
		// Report reference info in javadoc comment @throws/@exception tags
		TypeReference[] thrownExceptions = this.javadoc.thrownExceptions;
		int throwsTagsNbre = thrownExceptions == null ? 0 : thrownExceptions.length;
		for (int i = 0; i < throwsTagsNbre; i++) {
			TypeReference typeRef = thrownExceptions[i];
			if (typeRef instanceof JavadocSingleTypeReference) {
				JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
				this.requestor.acceptTypeReference(singleRef.token, singleRef.sourceStart);
			} else if (typeRef instanceof JavadocQualifiedTypeReference) {
				JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) typeRef;
				this.requestor.acceptTypeReference(qualifiedRef.tokens, qualifiedRef.sourceStart, qualifiedRef.sourceEnd);
			}
		}

		// Report reference info in javadoc comment @see tags
		Expression[] references = this.javadoc.references;
		int seeTagsNbre = references == null ? 0 : references.length;
		for (int i = 0; i < seeTagsNbre; i++) {
			Expression reference = references[i];
			acceptJavadocTypeReference(reference);
			if (reference instanceof JavadocFieldReference) {
				JavadocFieldReference fieldRef = (JavadocFieldReference) reference;
				this.requestor.acceptFieldReference(fieldRef.token, fieldRef.sourceStart);
				if (fieldRef.receiver != null && !fieldRef.receiver.isThis()) {
					acceptJavadocTypeReference(fieldRef.receiver);
				}
			} else if (reference instanceof JavadocMessageSend) {
				JavadocMessageSend messageSend = (JavadocMessageSend) reference;
				int argCount = messageSend.arguments == null ? 0 : messageSend.arguments.length;
				this.requestor.acceptMethodReference(messageSend.selector, argCount, messageSend.sourceStart);
				if (messageSend.receiver != null && !messageSend.receiver.isThis()) {
					acceptJavadocTypeReference(messageSend.receiver);
				}
			} else if (reference instanceof JavadocAllocationExpression) {
				JavadocAllocationExpression constructor = (JavadocAllocationExpression) reference;
				int argCount = constructor.arguments == null ? 0 : constructor.arguments.length;
				if (constructor.type != null) {
					char[][] compoundName = constructor.type.getParameterizedTypeName();
					this.requestor.acceptConstructorReference(compoundName[compoundName.length-1], argCount, constructor.sourceStart);
					if (!constructor.type.isThis()) {
						acceptJavadocTypeReference(constructor.type);
					}
				}
			}
		}
	}
}
private void acceptJavadocTypeReference(Expression expression) {
	if (expression instanceof JavadocSingleTypeReference) {
		JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) expression;
		this.requestor.acceptTypeReference(singleRef.token, singleRef.sourceStart);
	} else if (expression instanceof JavadocQualifiedTypeReference) {
		JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) expression;
		this.requestor.acceptTypeReference(qualifiedRef.tokens, qualifiedRef.sourceStart, qualifiedRef.sourceEnd);
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {

	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.classInstanceCreation(alwaysQualified);
	reportReferenceInfo = previousFlag;
	if (reportReferenceInfo){
		AllocationExpression alloc = (AllocationExpression)expressionStack[expressionPtr];
		TypeReference typeRef = alloc.type;
		requestor.acceptConstructorReference(
			typeRef instanceof SingleTypeReference 
				? ((SingleTypeReference) typeRef).token
				: CharOperation.concatWith(alloc.type.getParameterizedTypeName(), '.'),
			alloc.arguments == null ? 0 : alloc.arguments.length, 
			alloc.sourceStart);
	}
}
protected void consumeConstructorHeaderName() {
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('

	/* recovering - might be an empty message send */
	if (currentElement != null){
		if (lastIgnoredToken == TokenNamenew){ // was an allocation expression
			lastCheckPoint = scanner.startPosition; // force to restart at this exact position				
			restartRecovery = true;
			return;
		}
	}
	SourceConstructorDeclaration cd = new SourceConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack, 
			(this.expressionPtr -= length) + 1, 
			cd.annotations = new Annotation[length], 
			0, 
			length); 
	}
	// javadoc
	cd.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSourcePositions >>> 32);
	cd.selectorSourceEnd = (int) selectorSourcePositions;
	pushOnAstStack(cd);

	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (currentElement != null){
		lastCheckPoint = cd.bodyStart;
		if ((currentElement instanceof RecoveredType && lastIgnoredToken != TokenNameDOT)
			|| cd.modifiers != 0){
			currentElement = currentElement.add(cd, 0);
			lastIgnoredToken = -1;
		}
	}	
}
protected void consumeConstructorHeaderNameWithTypeParameters() {

	/* recovering - might be an empty message send */
	if (this.currentElement != null){
		if (this.lastIgnoredToken == TokenNamenew){ // was an allocation expression
			this.lastCheckPoint = this.scanner.startPosition; // force to restart at this exact position				
			this.restartRecovery = true;
			return;
		}
	}
	
	// ConstructorHeaderName ::=  Modifiersopt TypeParameters 'Identifier' '('
	SourceConstructorDeclaration cd = new SourceConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = this.identifierStack[this.identifierPtr];
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;

	// consume type parameters
	int length = this.genericsLengthStack[this.genericsLengthPtr--];
	this.genericsPtr -= length;
	System.arraycopy(this.genericsStack, this.genericsPtr + 1, cd.typeParameters = new TypeParameter[length], 0, length);
	
	//modifiers
	cd.declarationSourceStart = this.intStack[this.intPtr--];
	cd.modifiers = this.intStack[this.intPtr--];
	// consume annotations
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack, 
			(this.expressionPtr -= length) + 1, 
			cd.annotations = new Annotation[length], 
			0, 
			length); 
	}
	// javadoc
	cd.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSourcePositions >>> 32);
	cd.selectorSourceEnd = (int) selectorSourcePositions;
	pushOnAstStack(cd);
	cd.sourceEnd = this.lParenPos;
	cd.bodyStart = this.lParenPos+1;
	this.listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = cd.bodyStart;
		if ((this.currentElement instanceof RecoveredType && this.lastIgnoredToken != TokenNameDOT)
			|| cd.modifiers != 0){
			this.currentElement = this.currentElement.add(cd, 0);
			this.lastIgnoredToken = -1;
		}
	}	
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof SourceFieldDeclaration) {
		((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
	}
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof SourceFieldDeclaration) {
		((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'
	super.consumeFieldAccess(isSuperAccess);
	FieldReference fr = (FieldReference) expressionStack[expressionPtr];
	if (reportReferenceInfo) {
		requestor.acceptFieldReference(fr.token, fr.sourceStart);
	}
}
protected void consumeMethodHeaderName() {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	SourceMethodDeclaration md = new SourceMethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = this.identifierStack[identifierPtr];
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(this.intStack[this.intPtr--]);
	//modifiers
	md.declarationSourceStart = this.intStack[this.intPtr--];
	md.modifiers = this.intStack[this.intPtr--];
	// consume annotations
	int length;
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack, 
			(this.expressionPtr -= length) + 1, 
			md.annotations = new Annotation[length], 
			0, 
			length); 
	}
	// javadoc
	md.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	md.selectorSourceEnd = (int) selectorSourcePositions;
	pushOnAstStack(md);
	md.sourceEnd = this.lParenPos;
	md.bodyStart = this.lParenPos+1;
	this.listLength = 0; // initialize listLength before reading parameters/throws
	
	// recovery
	if (this.currentElement != null){
		if (this.currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (this.scanner.getLineNumber(md.returnType.sourceStart)
					== this.scanner.getLineNumber(md.sourceStart))){
			this.lastCheckPoint = md.bodyStart;
			this.currentElement = currentElement.add(md, 0);
			this.lastIgnoredToken = -1;			
		} else {
			this.lastCheckPoint = md.sourceStart;
			this.restartRecovery = true;
		}
	}		
}
protected void consumeMethodHeaderNameWithTypeParameters() {
	// MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	SourceMethodDeclaration md = new SourceMethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = this.identifierStack[this.identifierPtr];
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr--];
	this.identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(this.intStack[this.intPtr--]);
	
	// consume type parameters
	int length = this.genericsLengthStack[this.genericsLengthPtr--];
	this.genericsPtr -= length;
	System.arraycopy(this.genericsStack, this.genericsPtr + 1, md.typeParameters = new TypeParameter[length], 0, length);
	
	//modifiers
	md.declarationSourceStart = this.intStack[this.intPtr--];
	md.modifiers = this.intStack[this.intPtr--];
	// consume annotations
	if ((length = this.expressionLengthStack[this.expressionLengthPtr--]) != 0) {
		System.arraycopy(
			this.expressionStack, 
			(this.expressionPtr -= length) + 1, 
			md.annotations = new Annotation[length], 
			0, 
			length); 
	}	
	// javadoc
	md.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	md.selectorSourceEnd = (int) selectorSourcePositions;
	pushOnAstStack(md);
	md.sourceEnd = this.lParenPos;
	md.bodyStart = this.lParenPos+1;
	this.listLength = 0; // initialize this.listLength before reading parameters/throws
	
	// recovery
	if (this.currentElement != null){
		if (this.currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (this.scanner.getLineNumber(md.returnType.sourceStart)
					== this.scanner.getLineNumber(md.sourceStart))){
			this.lastCheckPoint = md.bodyStart;
			this.currentElement = this.currentElement.add(md, 0);
			this.lastIgnoredToken = -1;
		} else {
			this.lastCheckPoint = md.sourceStart;
			this.restartRecovery = true;
		}
	}		
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'
	super.consumeMethodInvocationName();

	// when the name is only an identifier...we have a message send to "this" (implicit)
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeMethodInvocationNameWithTypeArguments() {
	// MethodInvocation ::= Name '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationNameWithTypeArguments();

	// when the name is only an identifier...we have a message send to "this" (implicit)
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationPrimary() {
	super.consumeMethodInvocationPrimary();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationPrimaryWithTypeArguments() {
	super.consumeMethodInvocationPrimaryWithTypeArguments();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/*
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationSuper();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeMethodInvocationSuperWithTypeArguments() {
	// MethodInvocation ::= 'super' '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'
	super.consumeMethodInvocationSuperWithTypeArguments();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name
	super.consumeSingleStaticImportDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeSingleTypeImportDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
public MethodDeclaration convertToMethodDeclaration(ConstructorDeclaration c, CompilationResult compilationResult) {
	SourceMethodDeclaration m = new SourceMethodDeclaration(compilationResult);
	m.sourceStart = c.sourceStart;
	m.sourceEnd = c.sourceEnd;
	m.bodyStart = c.bodyStart;
	m.bodyEnd = c.bodyEnd;
	m.declarationSourceEnd = c.declarationSourceEnd;
	m.declarationSourceStart = c.declarationSourceStart;
	m.selector = c.selector;
	m.statements = c.statements;
	m.modifiers = c.modifiers;
	m.annotations = c.annotations;
	m.arguments = c.arguments;
	m.thrownExceptions = c.thrownExceptions;
	m.explicitDeclarations = c.explicitDeclarations;
	m.returnType = null;
	if (c instanceof SourceConstructorDeclaration) {
		m.selectorSourceEnd = ((SourceConstructorDeclaration)c).selectorSourceEnd;
	}
	return m;
}
protected FieldDeclaration createFieldDeclaration(char[] fieldName, int sourceStart, int sourceEnd) {
	return new SourceFieldDeclaration(fieldName, sourceStart, sourceEnd);
}
protected CompilationUnitDeclaration endParse(int act) {
	if (sourceType != null) {
		if (sourceType.isInterface()) {
			consumeInterfaceDeclaration();
		} else {
			consumeClassDeclaration();
		}
	}
	if (compilationUnit != null) {
		CompilationUnitDeclaration result = super.endParse(act);
		return result;
	} else {
		return null;
	}		
}
public TypeReference getTypeReference(int dim) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions
	 */
	int length = identifierLengthStack[identifierLengthPtr--];
	if (length < 0) { //flag for precompiled type reference on base types
		TypeReference ref = TypeReference.baseTypeReference(-length, dim);
		ref.sourceStart = intStack[intPtr--];
		if (dim == 0) {
			ref.sourceEnd = intStack[intPtr--];
		} else {
			intPtr--; // no need to use this position as it is an array
			ref.sourceEnd = endPosition;
		}
		if (reportReferenceInfo){
				requestor.acceptTypeReference(ref.getParameterizedTypeName(), ref.sourceStart, ref.sourceEnd);
		}
		return ref;
	} else {
		int numberOfIdentifiers = this.genericsIdentifiersLengthStack[this.genericsIdentifiersLengthPtr--];
		if (length != numberOfIdentifiers || this.genericsLengthStack[this.genericsLengthPtr] != 0) {
			// generic type
			TypeReference ref = getTypeReferenceForGenericType(dim, length, numberOfIdentifiers);
			if (reportReferenceInfo) {
				if (length == 1 && numberOfIdentifiers == 1) {
					ParameterizedSingleTypeReference parameterizedSingleTypeReference = (ParameterizedSingleTypeReference) ref;
					requestor.acceptTypeReference(parameterizedSingleTypeReference.token, parameterizedSingleTypeReference.sourceStart);
				} else {
					ParameterizedQualifiedTypeReference parameterizedQualifiedTypeReference = (ParameterizedQualifiedTypeReference) ref;
					requestor.acceptTypeReference(parameterizedQualifiedTypeReference.tokens, parameterizedQualifiedTypeReference.sourceStart, parameterizedQualifiedTypeReference.sourceEnd);
				}
			}
			return ref;
		} else if (length == 1) {
			// single variable reference
			this.genericsLengthPtr--; // pop the 0
			if (dim == 0) {
				SingleTypeReference ref = 
					new SingleTypeReference(
						identifierStack[identifierPtr], 
						identifierPositionStack[identifierPtr--]);
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.token, ref.sourceStart);
				}
				return ref;
			} else {
				ArrayTypeReference ref = 
					new ArrayTypeReference(
						identifierStack[identifierPtr], 
						dim, 
						identifierPositionStack[identifierPtr--]); 
				ref.sourceEnd = endPosition;
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.token, ref.sourceStart);
				}
				return ref;
			}
		} else {//Qualified variable reference
			this.genericsLengthPtr--;
			char[][] tokens = new char[length][];
			identifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
				identifierPtr + 1, 
				positions, 
				0, 
				length); 
			if (dim == 0) {
				QualifiedTypeReference ref = new QualifiedTypeReference(tokens, positions);
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			} else {
				ArrayQualifiedTypeReference ref = 
					new ArrayQualifiedTypeReference(tokens, dim, positions); 
				ref.sourceEnd = endPosition;					
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart, ref.sourceEnd);
				}
				return ref;
			}
		}
	}
}
public NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	} else {
		//Qualified variable reference
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		long[] positions = new long[length];
		System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
		QualifiedNameReference ref = 
			new QualifiedNameReference(
				tokens, 
				positions,
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	}
}
public NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		SingleNameReference ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		ref.bits &= ~ASTNode.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		if (reportReferenceInfo) {
			this.addUnknownRef(ref);
		}
		return ref;
	}

	//Qualified-variable-reference
	//In fact it is variable-reference DOT field-ref , but it would result in a type
	//conflict tha can be only reduce by making a superclass (or inetrface ) between
	//nameReference and FiledReference or putting FieldReference under NameReference
	//or else..........This optimisation is not really relevant so just leave as it is

	char[][] tokens = new char[length][];
	identifierPtr -= length;
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	long[] positions = new long[length];
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	QualifiedNameReference ref = 
		new QualifiedNameReference(
			tokens, 
			positions,
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), 
	// sourceStart
	 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~ASTNode.RestrictiveFlagMASK;
	ref.bits |= LOCAL | FIELD;
	if (reportReferenceInfo) {
		this.addUnknownRef(ref);
	}
	return ref;
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(CompilationUnitDeclaration parsedUnit) {
	if (parsedUnit == null) {
		// when we parse a single type member declaration the compilation unit is null, but we still
		// want to be able to notify the requestor on the created ast node
		if (astStack[0] instanceof AbstractMethodDeclaration) {
			notifySourceElementRequestor((AbstractMethodDeclaration) astStack[0]);
			return;
		}
		return;
	}
	// range check
	boolean isInRange = 
				scanner.initialPosition <= parsedUnit.sourceStart
				&& scanner.eofPosition >= parsedUnit.sourceEnd;
	
	if (reportReferenceInfo) {
		notifyAllUnknownReferences();
	}
	// collect the top level ast nodes
	int length = 0;
	ASTNode[] nodes = null;
	if (sourceType == null){
		if (isInRange) {
			requestor.enterCompilationUnit();
		}
		ImportReference currentPackage = parsedUnit.currentPackage;
		ImportReference[] imports = parsedUnit.imports;
		TypeDeclaration[] types = parsedUnit.types;
		length = 
			(currentPackage == null ? 0 : 1) 
			+ (imports == null ? 0 : imports.length)
			+ (types == null ? 0 : types.length);
		nodes = new ASTNode[length];
		int index = 0;
		if (currentPackage != null) {
			nodes[index++] = currentPackage;
		}
		if (imports != null) {
			for (int i = 0, max = imports.length; i < max; i++) {
				nodes[index++] = imports[i];
			}
		}
		if (types != null) {
			for (int i = 0, max = types.length; i < max; i++) {
				nodes[index++] = types[i];
			}
		}
	} else {
		TypeDeclaration[] types = parsedUnit.types;
		if (types != null) {
			length = types.length;
			nodes = new ASTNode[length];
			for (int i = 0, max = types.length; i < max; i++) {
				nodes[i] = types[i];
			}
		}
	}
	
	// notify the nodes in the syntactical order
	if (nodes != null && length > 0) {
		quickSort(nodes, 0, length-1);
		for (int i=0;i<length;i++) {
			ASTNode node = nodes[i];
			if (node instanceof ImportReference) {
				ImportReference importRef = (ImportReference)node;
				if (node == parsedUnit.currentPackage) {
					notifySourceElementRequestor(importRef, true);
				} else {
					notifySourceElementRequestor(importRef, false);
				}
			} else { // instanceof TypeDeclaration
				notifySourceElementRequestor((TypeDeclaration)node, sourceType == null);
			}
		}
	}
	
	if (sourceType == null){
		if (isInRange) {
			requestor.exitCompilationUnit(parsedUnit.sourceEnd);
		}
	}
}

private void notifyAllUnknownReferences() {
	for (int i = 0, max = this.unknownRefsCounter; i < max; i++) {
		NameReference nameRef = this.unknownRefs[i];
		if ((nameRef.bits & BindingIds.VARIABLE) != 0) {
			if ((nameRef.bits & BindingIds.TYPE) == 0) { 
				// variable but not type
				if (nameRef instanceof SingleNameReference) { 
					// local var or field
					requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
				} else {
					// QualifiedNameReference
					// The last token is a field reference and the previous tokens are a type/variable references
					char[][] tokens = ((QualifiedNameReference) nameRef).tokens;
					int tokensLength = tokens.length;
					requestor.acceptFieldReference(tokens[tokensLength - 1], nameRef.sourceEnd - tokens[tokensLength - 1].length + 1);
					char[][] typeRef = new char[tokensLength - 1][];
					System.arraycopy(tokens, 0, typeRef, 0, tokensLength - 1);
					requestor.acceptUnknownReference(typeRef, nameRef.sourceStart, nameRef.sourceEnd - tokens[tokensLength - 1].length);
				}
			} else {
				// variable or type
				if (nameRef instanceof SingleNameReference) {
					requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
				} else {
					//QualifiedNameReference
					requestor.acceptUnknownReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
				}
			}
		} else if ((nameRef.bits & BindingIds.TYPE) != 0) {
			if (nameRef instanceof SingleNameReference) {
				requestor.acceptTypeReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
			} else {
				// it is a QualifiedNameReference
				requestor.acceptTypeReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
			}
		}
	}
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(AbstractMethodDeclaration methodDeclaration) {

	// range check
	boolean isInRange = 
				scanner.initialPosition <= methodDeclaration.declarationSourceStart
				&& scanner.eofPosition >= methodDeclaration.declarationSourceEnd;

	if (methodDeclaration.isClinit()) {
		this.visitIfNeeded(methodDeclaration);
		return;
	}

	if (methodDeclaration.isDefaultConstructor()) {
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :					
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
				}
			}
		}	
		return;	
	}	
	char[][] argumentTypes = null;
	char[][] argumentNames = null;
	Argument[] arguments = methodDeclaration.arguments;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		for (int i = 0; i < argumentLength; i++) {
			argumentTypes[i] = CharOperation.concatWith(arguments[i].type.getParameterizedTypeName(), '.');
			argumentNames[i] = arguments[i].name;
		}
	}
	char[][] thrownExceptionTypes = null;
	TypeReference[] thrownExceptions = methodDeclaration.thrownExceptions;
	if (thrownExceptions != null) {
		int thrownExceptionLength = thrownExceptions.length;
		thrownExceptionTypes = new char[thrownExceptionLength][];
		for (int i = 0; i < thrownExceptionLength; i++) {
			thrownExceptionTypes[i] = 
				CharOperation.concatWith(thrownExceptions[i].getParameterizedTypeName(), '.'); 
		}
	}
	// by default no selector end position
	int selectorSourceEnd = -1;
	if (methodDeclaration.isConstructor()) {
		if (methodDeclaration instanceof SourceConstructorDeclaration) {
			selectorSourceEnd = 
				((SourceConstructorDeclaration) methodDeclaration).selectorSourceEnd; 
		}
		if (isInRange){
			TypeParameter[] typeParameters = methodDeclaration.typeParameters();
			char[][] typeParameterNames = null;
			char[][][] typeParameterBounds = null;
			if (typeParameters != null) {
				int typeParametersLength = typeParameters.length;
				typeParameterNames = new char[typeParametersLength][];
				typeParameterBounds = new char[typeParametersLength][][];
				for (int i = 0; i < typeParametersLength; i++) {
					typeParameterNames[i] = typeParameters[i].name;
					TypeReference[] bounds = typeParameters[i].bounds;
					if (bounds != null) {
						int boundLength = bounds.length;
						char[][] boundNames = new char[boundLength][];
						for (int j = 0; j < boundLength; j++) {
							boundNames[j] = 
								CharOperation.concatWith(bounds[j].getParameterizedTypeName(), '.'); 
						}
						typeParameterBounds[i] = boundNames;
					}
				}
			}			
			requestor.enterConstructor(
				methodDeclaration.declarationSourceStart, 
				methodDeclaration.modifiers, 
				methodDeclaration.selector, 
				methodDeclaration.sourceStart, 
				selectorSourceEnd, 
				argumentTypes, 
				argumentNames, 
				thrownExceptionTypes,
				typeParameterNames,
				typeParameterBounds);
		}
		if (reportReferenceInfo) {
			ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) methodDeclaration;
			ExplicitConstructorCall constructorCall = constructorDeclaration.constructorCall;
			if (constructorCall != null) {
				switch(constructorCall.accessMode) {
					case ExplicitConstructorCall.This :
						requestor.acceptConstructorReference(
							typeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
					case ExplicitConstructorCall.Super :
					case ExplicitConstructorCall.ImplicitSuper :
						requestor.acceptConstructorReference(
							superTypeNames[nestedTypeIndex-1],
							constructorCall.arguments == null ? 0 : constructorCall.arguments.length, 
							constructorCall.sourceStart);
						break;
				}
			}
		}
		this.visitIfNeeded(methodDeclaration);
		if (isInRange){
			requestor.exitConstructor(methodDeclaration.declarationSourceEnd);
		}
		return;
	}
	if (methodDeclaration instanceof SourceMethodDeclaration) {
		selectorSourceEnd = 
			((SourceMethodDeclaration) methodDeclaration).selectorSourceEnd; 
	}
	if (isInRange) {
		TypeParameter[] typeParameters = methodDeclaration.typeParameters();
		char[][] typeParameterNames = null;
		char[][][] typeParameterBounds = null;
		if (typeParameters != null) {
			int typeParametersLength = typeParameters.length;
			typeParameterNames = new char[typeParametersLength][];
			typeParameterBounds = new char[typeParametersLength][][];
			for (int i = 0; i < typeParametersLength; i++) {
				typeParameterNames[i] = typeParameters[i].name;
				TypeReference[] bounds = typeParameters[i].bounds;
				if (bounds != null) {
					int boundLength = bounds.length;
					char[][] boundNames = new char[boundLength][];
					for (int j = 0; j < boundLength; j++) {
						boundNames[j] = 
							CharOperation.concatWith(bounds[j].getParameterizedTypeName(), '.'); 
					}
					typeParameterBounds[i] = boundNames;
				}
			}
		}
		int currentModifiers = methodDeclaration.modifiers;
		boolean deprecated = (currentModifiers & AccDeprecated) != 0; // remember deprecation so as to not lose it below
		if (methodDeclaration instanceof MethodDeclaration) {
			TypeReference returnType = ((MethodDeclaration) methodDeclaration).returnType;
			requestor.enterMethod(
				methodDeclaration.declarationSourceStart, 
				deprecated ? (currentModifiers & AccJustFlag) | AccDeprecated : currentModifiers & AccJustFlag, 
				returnType == null ? null : CharOperation.concatWith(returnType.getParameterizedTypeName(), '.'),
				methodDeclaration.selector, 
				methodDeclaration.sourceStart, 
				selectorSourceEnd, 
				argumentTypes, 
				argumentNames, 
				thrownExceptionTypes,
				typeParameterNames,
				typeParameterBounds);
		} else {
			TypeReference returnType = ((AnnotationTypeMemberDeclaration) methodDeclaration).returnType;
			requestor.enterMethod(
				methodDeclaration.declarationSourceStart, 
				deprecated ? (currentModifiers & AccJustFlag) | AccDeprecated : currentModifiers & AccJustFlag, 
				returnType == null ? null : CharOperation.concatWith(returnType.getParameterizedTypeName(), '.'),
				methodDeclaration.selector, 
				methodDeclaration.sourceStart, 
				selectorSourceEnd, 
				argumentTypes, 
				argumentNames, 
				thrownExceptionTypes,
				typeParameterNames,
				typeParameterBounds);
		}
	}		
		
	this.visitIfNeeded(methodDeclaration);

	if (isInRange){	
		requestor.exitMethod(methodDeclaration.declarationSourceEnd);
	}
}
/*
* Update the bodyStart of the corresponding parse node
*/
public void notifySourceElementRequestor(FieldDeclaration fieldDeclaration) {
	
	// range check
	boolean isInRange = 
				scanner.initialPosition <= fieldDeclaration.declarationSourceStart
				&& scanner.eofPosition >= fieldDeclaration.declarationSourceEnd;

	if (fieldDeclaration.isField()) {
		int fieldEndPosition = fieldDeclaration.declarationSourceEnd;
		if (fieldDeclaration instanceof SourceFieldDeclaration) {
			fieldEndPosition = ((SourceFieldDeclaration) fieldDeclaration).fieldEndPosition;
			if (fieldEndPosition == 0) {
				// use the declaration source end by default
				fieldEndPosition = fieldDeclaration.declarationSourceEnd;
			}
		}
		if (isInRange) {
			int currentModifiers = fieldDeclaration.modifiers;
			boolean deprecated = (currentModifiers & AccDeprecated) != 0; // remember deprecation so as to not lose it below
			requestor.enterField(
				fieldDeclaration.declarationSourceStart, 
				deprecated ? (currentModifiers & AccJustFlag) | AccDeprecated : currentModifiers & AccJustFlag, 
				CharOperation.concatWith(fieldDeclaration.type.getParameterizedTypeName(), '.'),
				fieldDeclaration.name, 
				fieldDeclaration.sourceStart, 
				fieldDeclaration.sourceEnd); 
		}
		this.visitIfNeeded(fieldDeclaration);
		if (isInRange){
			requestor.exitField(
				// filter out initializations that are not a constant (simple check)
				(fieldDeclaration.initialization == null 
						|| fieldDeclaration.initialization instanceof ArrayInitializer
						|| fieldDeclaration.initialization instanceof AllocationExpression
						|| fieldDeclaration.initialization instanceof ArrayAllocationExpression
						|| fieldDeclaration.initialization instanceof Assignment
						|| fieldDeclaration.initialization instanceof ClassLiteralAccess
						|| fieldDeclaration.initialization instanceof MessageSend
						|| fieldDeclaration.initialization instanceof ArrayReference
						|| fieldDeclaration.initialization instanceof ThisReference) ? 
					-1 :  
					fieldDeclaration.initialization.sourceStart, 
				fieldEndPosition,
				fieldDeclaration.declarationSourceEnd);
		}

	} else {
		if (isInRange){
			requestor.enterInitializer(
				fieldDeclaration.declarationSourceStart,
				fieldDeclaration.modifiers); 
		}
		this.visitIfNeeded((Initializer)fieldDeclaration);
		if (isInRange){
			requestor.exitInitializer(fieldDeclaration.declarationSourceEnd);
		}
	}
}
public void notifySourceElementRequestor(
	ImportReference importReference, 
	boolean isPackage) {
	if (isPackage) {
		requestor.acceptPackage(
			importReference.declarationSourceStart, 
			importReference.declarationSourceEnd, 
			CharOperation.concatWith(importReference.getImportName(), '.')); 
	} else {
		requestor.acceptImport(
			importReference.declarationSourceStart, 
			importReference.declarationSourceEnd, 
			CharOperation.concatWith(importReference.getImportName(), '.'), 
			importReference.onDemand,
			importReference.modifiers); 
	}
}
public void notifySourceElementRequestor(TypeDeclaration typeDeclaration, boolean notifyTypePresence) {
	
	// range check
	boolean isInRange = 
				scanner.initialPosition <= typeDeclaration.declarationSourceStart
				&& scanner.eofPosition >= typeDeclaration.declarationSourceEnd;
	
	FieldDeclaration[] fields = typeDeclaration.fields;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	TypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
	int fieldCounter = fields == null ? 0 : fields.length;
	int methodCounter = methods == null ? 0 : methods.length;
	int memberTypeCounter = memberTypes == null ? 0 : memberTypes.length;
	int fieldIndex = 0;
	int methodIndex = 0;
	int memberTypeIndex = 0;
	boolean isInterface = typeDeclaration.isInterface();

	if (notifyTypePresence){
		char[][] interfaceNames = null;
		int superInterfacesLength = 0;
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			superInterfacesLength = superInterfaces.length;
			interfaceNames = new char[superInterfacesLength][];
		} else {
			if ((typeDeclaration.bits & ASTNode.IsAnonymousTypeMASK) != 0) {
				// see PR 3442
				QualifiedAllocationExpression alloc = typeDeclaration.allocation;
				if (alloc != null && alloc.type != null) {
					superInterfaces = new TypeReference[] { typeDeclaration.allocation.type};
					superInterfacesLength = 1;
					interfaceNames = new char[1][];
				}
			}
		}
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfacesLength; i++) {
				interfaceNames[i] = 
					CharOperation.concatWith(superInterfaces[i].getParameterizedTypeName(), '.'); 
			}
		}
		TypeParameter[] typeParameters = typeDeclaration.typeParameters;
		char[][] typeParameterNames = null;
		char[][][] typeParameterBounds = null;
		if (typeParameters != null) {
			int typeParametersLength = typeParameters.length;
			typeParameterNames = new char[typeParametersLength][];
			typeParameterBounds = new char[typeParametersLength][][];
			for (int i = 0; i < typeParametersLength; i++) {
				typeParameterNames[i] = typeParameters[i].name;
				TypeReference[] bounds = typeParameters[i].bounds;
				if (bounds != null) {
					int boundLength = bounds.length;
					char[][] boundNames = new char[boundLength][];
					for (int j = 0; j < boundLength; j++) {
						boundNames[j] = 
							CharOperation.concatWith(bounds[j].getParameterizedTypeName(), '.'); 
					}
					typeParameterBounds[i] = boundNames;
				}
			}
		}
		if (isInterface) {
			if (isInRange){
				int currentModifiers = typeDeclaration.modifiers;
				boolean deprecated = (currentModifiers & AccDeprecated) != 0; // remember deprecation so as to not lose it below
				requestor.enterInterface(
					typeDeclaration.declarationSourceStart, 
					deprecated ? (currentModifiers & AccJustFlag) | AccDeprecated : currentModifiers & AccJustFlag, 
					typeDeclaration.name, 
					typeDeclaration.sourceStart, 
					sourceEnd(typeDeclaration), 
					interfaceNames,
					typeParameterNames,
					typeParameterBounds);
			}
			if (nestedTypeIndex == typeNames.length) {
				// need a resize
				System.arraycopy(typeNames, 0, (typeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
				System.arraycopy(superTypeNames, 0, (superTypeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
			}
			typeNames[nestedTypeIndex] = typeDeclaration.name;
			superTypeNames[nestedTypeIndex++] = JAVA_LANG_OBJECT;
		} else {
			TypeReference superclass = typeDeclaration.superclass;
			if (superclass == null) {
				if (isInRange){
					requestor.enterClass(
						typeDeclaration.declarationSourceStart, 
						typeDeclaration.modifiers, 
						typeDeclaration.name, 
						typeDeclaration.sourceStart, 
						sourceEnd(typeDeclaration), 
						null, 
						interfaceNames,
						typeParameterNames,
						typeParameterBounds);
				}
			} else {
				if (isInRange){
					requestor.enterClass(
						typeDeclaration.declarationSourceStart, 
						typeDeclaration.modifiers, 
						typeDeclaration.name, 
						typeDeclaration.sourceStart, 
						sourceEnd(typeDeclaration), 
						CharOperation.concatWith(superclass.getParameterizedTypeName(), '.'), 
						interfaceNames,
						typeParameterNames,
						typeParameterBounds);
				}
			}
			if (nestedTypeIndex == typeNames.length) {
				// need a resize
				System.arraycopy(typeNames, 0, (typeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
				System.arraycopy(superTypeNames, 0, (superTypeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
			}
			typeNames[nestedTypeIndex] = typeDeclaration.name;
			superTypeNames[nestedTypeIndex++] = superclass == null ? JAVA_LANG_OBJECT : CharOperation.concatWith(superclass.getParameterizedTypeName(), '.');
		}
	}
	while ((fieldIndex < fieldCounter)
		|| (memberTypeIndex < memberTypeCounter)
		|| (methodIndex < methodCounter)) {
		FieldDeclaration nextFieldDeclaration = null;
		AbstractMethodDeclaration nextMethodDeclaration = null;
		TypeDeclaration nextMemberDeclaration = null;

		int position = Integer.MAX_VALUE;
		int nextDeclarationType = -1;
		if (fieldIndex < fieldCounter) {
			nextFieldDeclaration = fields[fieldIndex];
			if (nextFieldDeclaration.declarationSourceStart < position) {
				position = nextFieldDeclaration.declarationSourceStart;
				nextDeclarationType = 0; // FIELD
			}
		}
		if (methodIndex < methodCounter) {
			nextMethodDeclaration = methods[methodIndex];
			if (nextMethodDeclaration.declarationSourceStart < position) {
				position = nextMethodDeclaration.declarationSourceStart;
				nextDeclarationType = 1; // METHOD
			}
		}
		if (memberTypeIndex < memberTypeCounter) {
			nextMemberDeclaration = memberTypes[memberTypeIndex];
			if (nextMemberDeclaration.declarationSourceStart < position) {
				position = nextMemberDeclaration.declarationSourceStart;
				nextDeclarationType = 2; // MEMBER
			}
		}
		switch (nextDeclarationType) {
			case 0 :
				fieldIndex++;
				notifySourceElementRequestor(nextFieldDeclaration);
				break;
			case 1 :
				methodIndex++;
				notifySourceElementRequestor(nextMethodDeclaration);
				break;
			case 2 :
				memberTypeIndex++;
				notifySourceElementRequestor(nextMemberDeclaration, true);
		}
	}
	if (notifyTypePresence){
		if (isInRange){
			if (isInterface) {
				requestor.exitInterface(typeDeclaration.declarationSourceEnd);
			} else {
				requestor.exitClass(typeDeclaration.declarationSourceEnd);
			}
		}
		nestedTypeIndex--;
	}
}
private int sourceEnd(TypeDeclaration typeDeclaration) {
	if ((typeDeclaration.bits & ASTNode.IsAnonymousTypeMASK) != 0) {
		return typeDeclaration.allocation.type.sourceEnd;
	} else {
		return typeDeclaration.sourceEnd;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	int start, 
	int end, 
	boolean fullParse) {

	this.reportReferenceInfo = fullParse;
	boolean old = diet;
	if (fullParse) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}
	
	try {
		diet = true;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult, start, end);
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.lineSeparatorPositions);
		}
		if (this.localDeclarationVisitor != null || fullParse){
			diet = false;
			this.getMethodBodies(parsedUnit);
		}		
		this.scanner.resetTo(start, end);
		notifySourceElementRequestor(parsedUnit);
	} catch (AbortCompilation e) {
		// ignore this exception
	} finally {
		diet = old;
	}
}
public CompilationUnitDeclaration parseCompilationUnit(
	ICompilationUnit unit, 
	boolean fullParse) {
		
	boolean old = diet;
	if (fullParse) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}

	try {
		diet = true;
		this.reportReferenceInfo = fullParse;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult);
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.lineSeparatorPositions);
		}
		int initialStart = this.scanner.initialPosition;
		int initialEnd = this.scanner.eofPosition;
		if (this.localDeclarationVisitor != null || fullParse){
			diet = false;
			this.getMethodBodies(parsedUnit);
		}
		this.scanner.resetTo(initialStart, initialEnd);
		notifySourceElementRequestor(parsedUnit);
		return parsedUnit;
	} catch (AbortCompilation e) {
		// ignore this exception
	} finally {
		diet = old;
	}
	return null;
}
public void parseTypeMemberDeclarations(
	ISourceType type, 
	ICompilationUnit sourceUnit, 
	int start, 
	int end, 
	boolean needReferenceInfo) {
	boolean old = diet;
	if (needReferenceInfo) {
		unknownRefs = new NameReference[10];
		unknownRefsCounter = 0;
	}
	
	CompilationResult compilationUnitResult = 
		new CompilationResult(sourceUnit, 0, 0, this.options.maxProblemsPerUnit); 
	try {
		diet = !needReferenceInfo;
		reportReferenceInfo = needReferenceInfo;
		CompilationUnitDeclaration unit = 
			SourceTypeConverter.buildCompilationUnit(
				new ISourceType[]{type}, 
				// no need for field and methods
				// no need for member types
				// no need for field initialization
				SourceTypeConverter.NONE,
				problemReporter(), 
				compilationUnitResult); 
		if ((unit == null) || (unit.types == null) || (unit.types.length != 1))
			return;
		this.sourceType = type;
		try {
			/* automaton initialization */
			initialize();
			goForClassBodyDeclarations();
			/* scanner initialization */
			scanner.setSource(sourceUnit.getContents());
			scanner.resetTo(start, end);
			/* unit creation */
			referenceContext = compilationUnit = unit;
			/* initialize the astStacl */
			// the compilationUnitDeclaration should contain exactly one type
			pushOnAstStack(unit.types[0]);
			/* run automaton */
			parse();
			notifySourceElementRequestor(unit);
		} finally {
			unit = compilationUnit;
			compilationUnit = null; // reset parser
		}
	} catch (AbortCompilation e) {
		// ignore this exception
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.lineSeparatorPositions);
		}
		diet = old;
	}
}

public void parseTypeMemberDeclarations(
	char[] contents, 
	int start, 
	int end) {

	boolean old = diet;
	
	try {
		diet = true;

		/* automaton initialization */
		initialize();
		goForClassBodyDeclarations();
		/* scanner initialization */
		scanner.setSource(contents);
		scanner.recordLineSeparator = false;
		scanner.taskTags = null;
		scanner.taskPriorities = null;
		scanner.resetTo(start, end);

		/* unit creation */
		referenceContext = null;

		/* initialize the astStacl */
		// the compilationUnitDeclaration should contain exactly one type
		/* run automaton */
		parse();
		notifySourceElementRequestor((CompilationUnitDeclaration)null);
	} catch (AbortCompilation e) {
		// ignore this exception
	} finally {
		diet = old;
	}
}
/*
 * Sort the given ast nodes by their positions.
 */
private static void quickSort(ASTNode[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	ASTNode mid = sortedCollection[ (left + right) / 2];
	do {
		while (sortedCollection[left].sourceStart < mid.sourceStart) {
			left++;
		}
		while (mid.sourceStart < sortedCollection[right].sourceStart) {
			right--;
		}
		if (left <= right) {
			ASTNode tmp = sortedCollection[left];
			sortedCollection[left] = sortedCollection[right];
			sortedCollection[right] = tmp;
			left++;
			right--;
		}
	} while (left <= right);
	if (original_left < right) {
		quickSort(sortedCollection, original_left, right);
	}
	if (left < original_right) {
		quickSort(sortedCollection, left, original_right);
	}
}
public void addUnknownRef(NameReference nameRef) {
	if (this.unknownRefs.length == this.unknownRefsCounter) {
		// resize
		System.arraycopy(
			this.unknownRefs,
			0,
			(this.unknownRefs = new NameReference[this.unknownRefsCounter * 2]),
			0,
			this.unknownRefsCounter);
	}
	this.unknownRefs[this.unknownRefsCounter++] = nameRef;
}

private void visitIfNeeded(AbstractMethodDeclaration method) {
	if (this.localDeclarationVisitor != null 
		&& (method.bits & ASTNode.HasLocalTypeMASK) != 0) {
			if (method instanceof ConstructorDeclaration) {
				ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) method;
				if (constructorDeclaration.constructorCall != null) {
					constructorDeclaration.constructorCall.traverse(this.localDeclarationVisitor, method.scope);
				}
			}
			if (method.statements != null) {
				int statementsLength = method.statements.length;
				for (int i = 0; i < statementsLength; i++)
					method.statements[i].traverse(this.localDeclarationVisitor, method.scope);
			}
	}
}

private void visitIfNeeded(FieldDeclaration field) {
	if (this.localDeclarationVisitor != null 
		&& (field.bits & ASTNode.HasLocalTypeMASK) != 0) {
			if (field.initialization != null) {
				field.initialization.traverse(this.localDeclarationVisitor, (MethodScope) null);
			}
	}
}

private void visitIfNeeded(Initializer initializer) {
	if (this.localDeclarationVisitor != null 
		&& (initializer.bits & ASTNode.HasLocalTypeMASK) != 0) {
			if (initializer.block != null) {
				initializer.block.traverse(this.localDeclarationVisitor, null);
			}
	}
}
}

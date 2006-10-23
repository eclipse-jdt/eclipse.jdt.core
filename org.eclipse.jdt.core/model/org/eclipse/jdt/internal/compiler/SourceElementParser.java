/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObjectToInt;
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
	
	public ISourceElementRequestor requestor;
	ISourceType sourceType;
	boolean reportReferenceInfo;
	char[][] typeNames;
	char[][] superTypeNames;
	int nestedTypeIndex;
	LocalDeclarationVisitor localDeclarationVisitor = null;
	CompilerOptions options;
	HashtableOfObjectToInt sourceEnds = new HashtableOfObjectToInt();
	HashMap nodesToCategories = new HashMap(); // a map from ASTNode to char[][]
	boolean useSourceJavadocParser = true;
	
/**
 * An ast visitor that visits local type declarations.
 */
public class LocalDeclarationVisitor extends ASTVisitor {
	ArrayList declaringTypes;
	public void pushDeclaringType(TypeDeclaration declaringType) {
		if (this.declaringTypes == null) {
			this.declaringTypes = new ArrayList();
		}
		this.declaringTypes.add(declaringType);
	}
	public void popDeclaringType() {
		this.declaringTypes.remove(this.declaringTypes.size()-1);
	}
	public TypeDeclaration peekDeclaringType() {
		if (this.declaringTypes == null) return null;
		int size = this.declaringTypes.size();
		if (size == 0) return null;
		return (TypeDeclaration) this.declaringTypes.get(size-1);
	}
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null, peekDeclaringType());
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}
	public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
		notifySourceElementRequestor(typeDeclaration, sourceType == null, peekDeclaringType());
		return false; // don't visit members as this was done during notifySourceElementRequestor(...)
	}	
}

public SourceElementParser(
		final ISourceElementRequestor requestor, 
		IProblemFactory problemFactory,
		CompilerOptions options,
		boolean reportLocalDeclarations,
		boolean optimizeStringLiterals) {
	this(requestor, problemFactory, options, reportLocalDeclarations, optimizeStringLiterals, true/* use SourceJavadocParser */);
}

public SourceElementParser(
		ISourceElementRequestor requestor, 
		IProblemFactory problemFactory,
		CompilerOptions options,
		boolean reportLocalDeclarations,
		boolean optimizeStringLiterals,
		boolean useSourceJavadocParser) {
	
	super(
		new ProblemReporter(
			DefaultErrorHandlingPolicies.exitAfterAllProblems(),
			options, 
			problemFactory),
		optimizeStringLiterals);
	
	// we want to notify all syntax error with the acceptProblem API
	// To do so, we define the record method of the ProblemReporter
	this.problemReporter = new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		options, 
		problemFactory) {
		public void record(CategorizedProblem problem, CompilationResult unitResult, ReferenceContext context) {
			unitResult.record(problem, context); // TODO (jerome) clients are trapping problems either through factory or requestor... is result storing needed?
			SourceElementParser.this.requestor.acceptProblem(problem);
		}
	};
	this.requestor = requestor;
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
	this.options = options;
	if (reportLocalDeclarations) {
		this.localDeclarationVisitor = new LocalDeclarationVisitor();
	}
	// set specific javadoc parser
	this.useSourceJavadocParser = useSourceJavadocParser;
	if (useSourceJavadocParser) {
		this.javadocParser = new SourceJavadocParser(this);
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
public void addUnknownRef(NameReference nameRef) {
	// Note that:
	// - the only requestor interested in references is the SourceIndexerRequestor
	// - a name reference can become a type reference only during the cast case, it is then tagged later with the Binding.TYPE bit
	// However since the indexer doesn't make the distinction between name reference and type reference, there is no need
	// to report a type reference in the SourceElementParser.
	// This gained 3.7% in the indexing performance test.
	if (nameRef instanceof SingleNameReference) {
		requestor.acceptUnknownReference(((SingleNameReference) nameRef).token, nameRef.sourceStart);
	} else {
		//QualifiedNameReference
		requestor.acceptUnknownReference(((QualifiedNameReference) nameRef).tokens, nameRef.sourceStart, nameRef.sourceEnd);
	}
}
public void checkComment() {
	// discard obsolete comments while inside methods or fields initializer (see bug 74369)
	if (!(this.diet && this.dietInt==0) && this.scanner.commentPtr >= 0) {
		flushCommentsDefinedPriorTo(this.endStatementPosition);
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
			int commentEnd = this.scanner.commentStops[lastComment] - 1; //stop is one over,
			// do not report problem before last parsed comment while recovering code...
			this.javadocParser.reportProblems = this.currentElement == null || commentEnd > this.lastJavadocEnd;
			if (this.javadocParser.checkDeprecation(lastComment)) {
				checkAndSetModifiers(ClassFileConstants.AccDeprecated);
			}
			this.javadoc = this.javadocParser.docComment;	// null if check javadoc is not activated
			if (currentElement == null) this.lastJavadocEnd = commentEnd;
		}
	}

	if (this.reportReferenceInfo && this.javadocParser.checkDocComment && this.javadoc != null) {
		// Report reference info in javadoc comment @throws/@exception tags
		TypeReference[] thrownExceptions = this.javadoc.exceptionReferences;
		if (thrownExceptions != null) {
			for (int i = 0, max=thrownExceptions.length; i < max; i++) {
				TypeReference typeRef = thrownExceptions[i];
				if (typeRef instanceof JavadocSingleTypeReference) {
					JavadocSingleTypeReference singleRef = (JavadocSingleTypeReference) typeRef;
					this.requestor.acceptTypeReference(singleRef.token, singleRef.sourceStart);
				} else if (typeRef instanceof JavadocQualifiedTypeReference) {
					JavadocQualifiedTypeReference qualifiedRef = (JavadocQualifiedTypeReference) typeRef;
					this.requestor.acceptTypeReference(qualifiedRef.tokens, qualifiedRef.sourceStart, qualifiedRef.sourceEnd);
				}
			}
		}

		// Report reference info in javadoc comment @see tags
		Expression[] references = this.javadoc.seeReferences;
		if (references != null) {
			for (int i = 0, max=references.length; i < max; i++) {
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
					this.requestor.acceptConstructorReference(messageSend.selector, argCount, messageSend.sourceStart);
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
private long[] collectAnnotationPositions(Annotation[] annotations) {
	if (annotations == null) return null;
	int length = annotations.length;
	long[] result = new long[length];
	for (int i = 0; i < length; i++) {
		Annotation annotation = annotations[i];
		result[i] = (((long) annotation.sourceStart) << 32) + annotation.declarationSourceEnd;
	}
	return result;
}
protected void consumeAnnotationAsModifier() {
	super.consumeAnnotationAsModifier();
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}
protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.consumeClassInstanceCreationExpressionQualifiedWithTypeArguments();
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
protected void consumeAnnotationTypeDeclarationHeaderName() {
	int currentAstPtr = this.astPtr;
	super.consumeAnnotationTypeDeclarationHeaderName();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeClassHeaderName1() {
	int currentAstPtr = this.astPtr;
	super.consumeClassHeaderName1();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
	boolean previousFlag = reportReferenceInfo;
	reportReferenceInfo = false; // not to see the type reference reported in super call to getTypeReference(...)
	super.consumeClassInstanceCreationExpressionWithTypeArguments();
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
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeConstructorHeaderName();
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}
protected void consumeConstructorHeaderNameWithTypeParameters() {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeConstructorHeaderNameWithTypeParameters();
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}
protected void consumeEnumConstantWithClassBody() {
	super.consumeEnumConstantWithClassBody();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeEnumConstantNoClassBody() {
	super.consumeEnumConstantNoClassBody();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& this.astStack[this.astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeEnumHeaderName() {
	int currentAstPtr = this.astPtr;
	super.consumeEnumHeaderName();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& this.astStack[this.astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
	}
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	super.consumeExitVariableWithoutInitialization();
	if ((currentToken == TokenNameCOMMA || currentToken == TokenNameSEMICOLON)
			&& astStack[astPtr] instanceof FieldDeclaration) {
		this.sourceEnds.put(this.astStack[this.astPtr], this.scanner.currentPosition - 1);
		rememberCategories();
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
protected void consumeFormalParameter(boolean isVarArgs) {
	super.consumeFormalParameter(isVarArgs);
	
	// Flush comments prior to this formal parameter so the declarationSourceStart of the following parameter
	// is correctly set (see bug 80904)
	// Note that this could be done in the Parser itself, but this would slow down all parsers, when they don't need 
	// the declarationSourceStart to be set
	flushCommentsDefinedPriorTo(this.scanner.currentPosition);
}
protected void consumeInterfaceHeaderName1() {
	int currentAstPtr = this.astPtr;
	super.consumeInterfaceHeaderName1();
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		rememberCategories();
}
protected void consumeMemberValuePair() {
	super.consumeMemberValuePair();
	MemberValuePair memberValuepair = (MemberValuePair) this.astStack[this.astPtr];
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(memberValuepair.name, 0, memberValuepair.sourceStart);
	}
}
protected void consumeMarkerAnnotation() {
	super.consumeMarkerAnnotation();
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}
protected void consumeMethodHeaderName(boolean isAnnotationMethod) {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeMethodHeaderName(isAnnotationMethod);
	if (this.astPtr > currentAstPtr) { // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
	}
}

protected void consumeMethodHeaderNameWithTypeParameters(boolean isAnnotationMethod) {
	long selectorSourcePositions = this.identifierPositionStack[this.identifierPtr];
	int selectorSourceEnd = (int) selectorSourcePositions;
	int currentAstPtr = this.astPtr;
	super.consumeMethodHeaderNameWithTypeParameters(isAnnotationMethod);
	if (this.astPtr > currentAstPtr) // if ast node was pushed on the ast stack
		this.sourceEnds.put(this.astStack[this.astPtr], selectorSourceEnd);
		rememberCategories();
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
protected void consumeNormalAnnotation() {
	super.consumeNormalAnnotation();
	Annotation annotation = (Annotation)expressionStack[expressionPtr];
	if (reportReferenceInfo) { // accept annotation type reference
		this.requestor.acceptTypeReference(annotation.type.getTypeName(), annotation.sourceStart, annotation.sourceEnd);
	}
}
protected void consumeSingleMemberAnnotation() {
	super.consumeSingleMemberAnnotation();
	SingleMemberAnnotation member = (SingleMemberAnnotation) expressionStack[expressionPtr];
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(TypeConstants.VALUE, 0, member.sourceStart);
	}
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name
	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = newImportReference(tokens, positions, false, ClassFileConstants.AccStatic));
	
	this.modifiers = ClassFileConstants.AccDefault;
	this.modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	
	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];
	
	if(!this.statementRecoveryActivated &&
			this.options.sourceLevel < ClassFileConstants.JDK1_5 &&
			this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
		impt.modifiers = ClassFileConstants.AccDefault; // convert the static import reference to a non-static importe reference
		this.problemReporter().invalidUsageOfStaticImports(impt);
	}
	
	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	if (reportReferenceInfo) {
		// Name for static import is TypeName '.' Identifier
		// => accept unknown ref on identifier
		int tokensLength = impt.tokens.length-1;
		int start = (int) (impt.sourcePositions[tokensLength] >>> 32);
		char[] last = impt.tokens[tokensLength];
		// accept all possible kind for last name, index users will have to select the right one...
		// see bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=86901
		requestor.acceptFieldReference(last, start);
		requestor.acceptMethodReference(last, 0,start);
		requestor.acceptTypeReference(last, start);
		// accept type name
		if (tokensLength > 0) {
			char[][] compoundName = new char[tokensLength][];
			System.arraycopy(impt.tokens, 0, compoundName, 0, tokensLength);
			int end = (int) impt.sourcePositions[tokensLength-1];
			requestor.acceptTypeReference(compoundName, impt.sourceStart, end);
		}
	}
}

protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = newImportReference(tokens, positions, false, ClassFileConstants.AccDefault));
	
	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];
	
	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' 'static' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, ClassFileConstants.AccStatic));
	
	this.modifiers = ClassFileConstants.AccDefault;
	this.modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	
	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];
	
	if(!this.statementRecoveryActivated &&
			options.sourceLevel < ClassFileConstants.JDK1_5 &&
			this.lastErrorEndPositionBeforeRecovery < this.scanner.currentPosition) {
		impt.modifiers = ClassFileConstants.AccDefault; // convert the static import reference to a non-static importe reference
		this.problemReporter().invalidUsageOfStaticImports(impt);
	}
	
	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = this.identifierLengthStack[this.identifierLengthPtr--]][];
	this.identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(this.identifierStack, this.identifierPtr + 1, tokens, 0, length);
	System.arraycopy(this.identifierPositionStack, this.identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, ClassFileConstants.AccDefault));
	
	if (this.currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = this.scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//this.endPosition is just before the ;
	impt.declarationSourceStart = this.intStack[this.intPtr--];
	
	// recovery
	if (this.currentElement != null){
		this.lastCheckPoint = impt.declarationSourceEnd+1;
		this.currentElement = this.currentElement.add(impt, 0);
		this.lastIgnoredToken = -1;
		this.restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(impt.tokens, impt.sourceStart, impt.sourceEnd);
	}
}
public MethodDeclaration convertToMethodDeclaration(ConstructorDeclaration c, CompilationResult compilationResult) {
	MethodDeclaration methodDeclaration = super.convertToMethodDeclaration(c, compilationResult);
	int selectorSourceEnd = this.sourceEnds.removeKey(c);
	if (selectorSourceEnd != -1)
		this.sourceEnds.put(methodDeclaration, selectorSourceEnd);
	char[][] categories =  (char[][]) this.nodesToCategories.remove(c);
	if (categories != null)
		this.nodesToCategories.put(methodDeclaration, categories);
	
	return methodDeclaration;
}
protected CompilationUnitDeclaration endParse(int act) {
	if (sourceType != null) {
		switch (TypeDeclaration.kind(sourceType.getModifiers())) {
			case TypeDeclaration.CLASS_DECL :
				consumeClassDeclaration();
				break;
			case TypeDeclaration.INTERFACE_DECL :
				consumeInterfaceDeclaration();
				break;
			case TypeDeclaration.ENUM_DECL :
				consumeEnumDeclaration();
				break;
			case TypeDeclaration.ANNOTATION_TYPE_DECL :
				consumeAnnotationTypeDeclaration();
				break;
		}
	}
	if (compilationUnit != null) {
		CompilationUnitDeclaration result = super.endParse(act);
		return result;
	} else {
		return null;
	}		
}
private ISourceElementRequestor.TypeParameterInfo[] getTypeParameterInfos(TypeParameter[] typeParameters) {
	if (typeParameters == null) return null;
	int typeParametersLength = typeParameters.length;
	ISourceElementRequestor.TypeParameterInfo[] result = new ISourceElementRequestor.TypeParameterInfo[typeParametersLength];
	for (int i = 0; i < typeParametersLength; i++) {
		TypeParameter typeParameter = typeParameters[i];
		TypeReference firstBound = typeParameter.type;
		TypeReference[] otherBounds = typeParameter.bounds;
		char[][] typeParameterBounds = null;
		if (firstBound != null) {
			if (otherBounds != null) {
				int otherBoundsLength = otherBounds.length;
				char[][] boundNames = new char[otherBoundsLength+1][];
				boundNames[0] = CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.');
				for (int j = 0; j < otherBoundsLength; j++) {
					boundNames[j+1] = 
						CharOperation.concatWith(otherBounds[j].getParameterizedTypeName(), '.'); 
				}
				typeParameterBounds = boundNames;
			} else {
				typeParameterBounds = new char[][] { CharOperation.concatWith(firstBound.getParameterizedTypeName(), '.')};
			}
		} else {
			typeParameterBounds = CharOperation.NO_CHAR_CHAR;
		}
		ISourceElementRequestor.TypeParameterInfo typeParameterInfo = new ISourceElementRequestor.TypeParameterInfo();
		typeParameterInfo.declarationStart = typeParameter.declarationSourceStart;
		typeParameterInfo.declarationEnd = typeParameter.declarationSourceEnd;
		typeParameterInfo.name = typeParameter.name;
		typeParameterInfo.nameSourceStart = typeParameter.sourceStart;
		typeParameterInfo.nameSourceEnd = typeParameter.sourceEnd;
		typeParameterInfo.bounds = typeParameterBounds;
		result[i] = typeParameterInfo;
	}
	return result;
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
			newSingleNameReference(
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
			newQualifiedNameReference(
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
			newSingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		ref.bits &= ~ASTNode.RestrictiveFlagMASK;
		ref.bits |= Binding.LOCAL | Binding.FIELD;
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
		newQualifiedNameReference(
			tokens, 
			positions,
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), 
	// sourceStart
	 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~ASTNode.RestrictiveFlagMASK;
	ref.bits |= Binding.LOCAL | Binding.FIELD;
	if (reportReferenceInfo) {
		this.addUnknownRef(ref);
	}
	return ref;
}

/*
 * Checks whether one of the annotations is the @Deprecated annotation
 * (see https://bugs.eclipse.org/bugs/show_bug.cgi?id=89807)
 */
private boolean hasDeprecatedAnnotation(Annotation[] annotations) {
	if (annotations != null) {
		for (int i = 0, length = annotations.length; i < length; i++) {
			Annotation annotation = annotations[i];
			if (CharOperation.equals(annotation.type.getLastToken(), TypeConstants.JAVA_LANG_DEPRECATED[2])) {
				return true;
			}
		}
	}
	return false;
}

protected ImportReference newImportReference(char[][] tokens, long[] positions, boolean onDemand, int mod) {
	return new ImportReference(tokens, positions, onDemand, mod);
}
protected QualifiedNameReference newQualifiedNameReference(char[][] tokens, long[] positions, int sourceStart, int sourceEnd) {
	return new QualifiedNameReference(tokens, positions, sourceStart, sourceEnd);
}
protected SingleNameReference newSingleNameReference(char[] source, long positions) {
	return new SingleNameReference(source, positions);
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
				notifySourceElementRequestor((TypeDeclaration)node, sourceType == null, null);
			}
		}
	}
	
	if (sourceType == null){
		if (isInRange) {
			requestor.exitCompilationUnit(parsedUnit.sourceEnd);
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
	boolean isVarArgs = false;
	Argument[] arguments = methodDeclaration.arguments;
	if (arguments != null) {
		int argumentLength = arguments.length;
		argumentTypes = new char[argumentLength][];
		argumentNames = new char[argumentLength][];
		for (int i = 0; i < argumentLength; i++) {
			argumentTypes[i] = CharOperation.concatWith(arguments[i].type.getParameterizedTypeName(), '.');
			argumentNames[i] = arguments[i].name;
		}
		isVarArgs = arguments[argumentLength-1].isVarArgs();
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
		selectorSourceEnd = this.sourceEnds.get(methodDeclaration);
		if (isInRange){
			int currentModifiers = methodDeclaration.modifiers;
			if (isVarArgs)
				currentModifiers |= ClassFileConstants.AccVarargs;
			
			// remember deprecation so as to not lose it below
			boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(methodDeclaration.annotations);
			
			ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
			methodInfo.isConstructor = true;
			methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
			methodInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
			methodInfo.name = methodDeclaration.selector;
			methodInfo.nameSourceStart = methodDeclaration.sourceStart;
			methodInfo.nameSourceEnd = selectorSourceEnd;
			methodInfo.parameterTypes = argumentTypes;
			methodInfo.parameterNames = argumentNames;
			methodInfo.exceptionTypes = thrownExceptionTypes;
			methodInfo.typeParameters = getTypeParameterInfos(methodDeclaration.typeParameters());
			methodInfo.annotationPositions = collectAnnotationPositions(methodDeclaration.annotations);
			methodInfo.categories = (char[][]) this.nodesToCategories.get(methodDeclaration);
			requestor.enterConstructor(methodInfo);
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
	selectorSourceEnd = this.sourceEnds.get(methodDeclaration);
	if (isInRange) {
		int currentModifiers = methodDeclaration.modifiers;
		if (isVarArgs)
			currentModifiers |= ClassFileConstants.AccVarargs;
		
		// remember deprecation so as to not lose it below
		boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(methodDeclaration.annotations);	
			
		TypeReference returnType = methodDeclaration instanceof MethodDeclaration
			? ((MethodDeclaration) methodDeclaration).returnType
			: null;
		ISourceElementRequestor.MethodInfo methodInfo = new ISourceElementRequestor.MethodInfo();
		methodInfo.isAnnotation = methodDeclaration instanceof AnnotationMethodDeclaration;
		methodInfo.declarationStart = methodDeclaration.declarationSourceStart;
		methodInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
		methodInfo.returnType = returnType == null ? null : CharOperation.concatWith(returnType.getParameterizedTypeName(), '.');
		methodInfo.name = methodDeclaration.selector;
		methodInfo.nameSourceStart = methodDeclaration.sourceStart;
		methodInfo.nameSourceEnd = selectorSourceEnd;
		methodInfo.parameterTypes = argumentTypes;
		methodInfo.parameterNames = argumentNames;
		methodInfo.exceptionTypes = thrownExceptionTypes;
		methodInfo.typeParameters = getTypeParameterInfos(methodDeclaration.typeParameters());
		methodInfo.annotationPositions = collectAnnotationPositions(methodDeclaration.annotations);
		methodInfo.categories = (char[][]) this.nodesToCategories.get(methodDeclaration);
		requestor.enterMethod(methodInfo);
	}		
		
	this.visitIfNeeded(methodDeclaration);

	if (isInRange) {
		if (methodDeclaration instanceof AnnotationMethodDeclaration) {
			AnnotationMethodDeclaration annotationMethodDeclaration = (AnnotationMethodDeclaration) methodDeclaration;
			Expression expression = annotationMethodDeclaration.defaultValue;
			if (expression != null) {
				requestor.exitMethod(methodDeclaration.declarationSourceEnd, expression.sourceStart, expression.sourceEnd);
				return;
			}
		} 
		requestor.exitMethod(methodDeclaration.declarationSourceEnd, -1, -1);
	}
}

/*
* Update the bodyStart of the corresponding parse node
*/
public void notifySourceElementRequestor(FieldDeclaration fieldDeclaration, TypeDeclaration declaringType) {
	
	// range check
	boolean isInRange = 
				scanner.initialPosition <= fieldDeclaration.declarationSourceStart
				&& scanner.eofPosition >= fieldDeclaration.declarationSourceEnd;

	switch(fieldDeclaration.getKind()) {
		case AbstractVariableDeclaration.ENUM_CONSTANT:
			// accept constructor reference for enum constant
			if (fieldDeclaration.initialization instanceof AllocationExpression) {
				AllocationExpression alloc = (AllocationExpression) fieldDeclaration.initialization;
				requestor.acceptConstructorReference(
					declaringType.name,
					alloc.arguments == null ? 0 : alloc.arguments.length, 
					alloc.sourceStart);
			}
			// fall through next case
		case AbstractVariableDeclaration.FIELD:
			int fieldEndPosition = this.sourceEnds.get(fieldDeclaration);
			if (fieldEndPosition == -1) {
				// use the declaration source end by default
				fieldEndPosition = fieldDeclaration.declarationSourceEnd;
			}
			if (isInRange) {
				int currentModifiers = fieldDeclaration.modifiers;
				
				// remember deprecation so as to not lose it below
				boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(fieldDeclaration.annotations);	
			
				char[] typeName = null;
				if (fieldDeclaration.type == null) {
					// enum constant
					typeName = declaringType.name;
					currentModifiers |= ClassFileConstants.AccEnum;
				} else {
					// regular field
					typeName = CharOperation.concatWith(fieldDeclaration.type.getParameterizedTypeName(), '.');
				}
				ISourceElementRequestor.FieldInfo fieldInfo = new ISourceElementRequestor.FieldInfo();
				fieldInfo.declarationStart = fieldDeclaration.declarationSourceStart;
				fieldInfo.name = fieldDeclaration.name;
				fieldInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
				fieldInfo.type = typeName;
				fieldInfo.nameSourceStart = fieldDeclaration.sourceStart;
				fieldInfo.nameSourceEnd = fieldDeclaration.sourceEnd;
				fieldInfo.annotationPositions = collectAnnotationPositions(fieldDeclaration.annotations);
				fieldInfo.categories = (char[][]) this.nodesToCategories.get(fieldDeclaration);
				requestor.enterField(fieldInfo);
			}
			this.visitIfNeeded(fieldDeclaration, declaringType);
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
			break;
		case AbstractVariableDeclaration.INITIALIZER:
			if (isInRange){
				requestor.enterInitializer(
					fieldDeclaration.declarationSourceStart,
					fieldDeclaration.modifiers); 
			}
			this.visitIfNeeded((Initializer)fieldDeclaration);
			if (isInRange){
				requestor.exitInitializer(fieldDeclaration.declarationSourceEnd);
			}
			break;
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
			importReference.tokens, 
			importReference.onDemand,
			importReference.modifiers); 
	}
}
public void notifySourceElementRequestor(TypeDeclaration typeDeclaration, boolean notifyTypePresence, TypeDeclaration declaringType) {
	
	if (CharOperation.equals(TypeConstants.PACKAGE_INFO_NAME, typeDeclaration.name)) return;

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
	
	if (notifyTypePresence){
		char[][] interfaceNames = null;
		int superInterfacesLength = 0;
		TypeReference[] superInterfaces = typeDeclaration.superInterfaces;
		if (superInterfaces != null) {
			superInterfacesLength = superInterfaces.length;
			interfaceNames = new char[superInterfacesLength][];
		} else {
			if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
				// see PR 3442
				QualifiedAllocationExpression alloc = typeDeclaration.allocation;
				if (alloc != null && alloc.type != null) {
					superInterfaces = new TypeReference[] { alloc.type};
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
		int kind = TypeDeclaration.kind(typeDeclaration.modifiers);
		char[] implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
		if (isInRange) {
			int currentModifiers = typeDeclaration.modifiers;
			
			// remember deprecation so as to not lose it below
			boolean deprecated = (currentModifiers & ClassFileConstants.AccDeprecated) != 0 || hasDeprecatedAnnotation(typeDeclaration.annotations);	
			
			boolean isEnumInit = typeDeclaration.allocation != null && typeDeclaration.allocation.enumConstant != null;
			char[] superclassName;
			if (isEnumInit) {
				currentModifiers |= ClassFileConstants.AccEnum;
				superclassName = declaringType.name;
			} else {
				TypeReference superclass = typeDeclaration.superclass;
				superclassName = superclass != null ? CharOperation.concatWith(superclass.getParameterizedTypeName(), '.') : null;
			}
			ISourceElementRequestor.TypeInfo typeInfo = new ISourceElementRequestor.TypeInfo();
			typeInfo.declarationStart = typeDeclaration.declarationSourceStart;
			typeInfo.modifiers = deprecated ? (currentModifiers & ExtraCompilerModifiers.AccJustFlag) | ClassFileConstants.AccDeprecated : currentModifiers & ExtraCompilerModifiers.AccJustFlag;
			typeInfo.name = typeDeclaration.name;
			typeInfo.nameSourceStart = typeDeclaration.sourceStart;
			typeInfo.nameSourceEnd = sourceEnd(typeDeclaration);
			typeInfo.superclass = superclassName;
			typeInfo.superinterfaces = interfaceNames;
			typeInfo.typeParameters = getTypeParameterInfos(typeDeclaration.typeParameters);
			typeInfo.annotationPositions = collectAnnotationPositions(typeDeclaration.annotations);
			typeInfo.categories = (char[][]) this.nodesToCategories.get(typeDeclaration);
			typeInfo.secondary = typeDeclaration.isSecondary();
			typeInfo.anonymousMember = typeDeclaration.allocation != null && typeDeclaration.allocation.enclosingInstance != null;
			requestor.enterType(typeInfo);
			switch (kind) {
				case TypeDeclaration.CLASS_DECL :
					if (superclassName != null)
						implicitSuperclassName = superclassName;
					break;
				case TypeDeclaration.INTERFACE_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_OBJECT;
					break;
				case TypeDeclaration.ENUM_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ENUM;
					break;
				case TypeDeclaration.ANNOTATION_TYPE_DECL :
					implicitSuperclassName = TypeConstants.CharArray_JAVA_LANG_ANNOTATION_ANNOTATION;
					break;
			}
		}
		if (this.nestedTypeIndex == this.typeNames.length) {
			// need a resize
			System.arraycopy(this.typeNames, 0, (this.typeNames = new char[this.nestedTypeIndex * 2][]), 0, this.nestedTypeIndex);
			System.arraycopy(this.superTypeNames, 0, (this.superTypeNames = new char[this.nestedTypeIndex * 2][]), 0, this.nestedTypeIndex);
		}
		this.typeNames[this.nestedTypeIndex] = typeDeclaration.name;
		this.superTypeNames[this.nestedTypeIndex++] = implicitSuperclassName;
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
				notifySourceElementRequestor(nextFieldDeclaration, typeDeclaration);
				break;
			case 1 :
				methodIndex++;
				notifySourceElementRequestor(nextMethodDeclaration);
				break;
			case 2 :
				memberTypeIndex++;
				notifySourceElementRequestor(nextMemberDeclaration, true, null);
		}
	}
	if (notifyTypePresence){
		if (isInRange){
			requestor.exitType(typeDeclaration.declarationSourceEnd);
		}
		nestedTypeIndex--;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	int start, 
	int end, 
	boolean fullParse) {

	this.reportReferenceInfo = fullParse;
	boolean old = diet;
	
	try {
		diet = true;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult, start, end);
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.getLineSeparatorPositions());
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
		reset();
	}
}
public CompilationUnitDeclaration parseCompilationUnit(
	ICompilationUnit unit, 
	boolean fullParse) {
		
	boolean old = diet;

	try {
		diet = true;
		this.reportReferenceInfo = fullParse;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = parse(unit, compilationUnitResult);
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(compilationUnitResult.getLineSeparatorPositions());
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
		reset();
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
			requestor.acceptLineSeparatorPositions(compilationUnitResult.getLineSeparatorPositions());
		}
		diet = old;
		reset();
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
		reset();
	}
}
/*
 * Sort the given ast nodes by their positions.
 */
private static void quickSort(ASTNode[] sortedCollection, int left, int right) {
	int original_left = left;
	int original_right = right;
	ASTNode mid = sortedCollection[left +  (right - left) / 2];
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
private void rememberCategories() {
	if (this.useSourceJavadocParser) {
		SourceJavadocParser sourceJavadocParser = (SourceJavadocParser) this.javadocParser;
		char[][] categories =  sourceJavadocParser.categories;
		if (categories.length > 0) {
			this.nodesToCategories.put(this.astStack[this.astPtr], categories);
			sourceJavadocParser.categories = CharOperation.NO_CHAR_CHAR;
		}
	}
}
private void reset() {
	this.sourceEnds = new HashtableOfObjectToInt();
	this.nodesToCategories = new HashMap();
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
}
private int sourceEnd(TypeDeclaration typeDeclaration) {
	if ((typeDeclaration.bits & ASTNode.IsAnonymousType) != 0) {
		QualifiedAllocationExpression allocation = typeDeclaration.allocation;
		if (allocation.type == null) // case of enum constant body
			return typeDeclaration.sourceEnd;
		return allocation.type.sourceEnd;
	} else {
		return typeDeclaration.sourceEnd;
	}
}
private void visitIfNeeded(AbstractMethodDeclaration method) {
	if (this.localDeclarationVisitor != null 
		&& (method.bits & ASTNode.HasLocalType) != 0) {
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

private void visitIfNeeded(FieldDeclaration field, TypeDeclaration declaringType) {
	if (this.localDeclarationVisitor != null 
		&& (field.bits & ASTNode.HasLocalType) != 0) {
			if (field.initialization != null) {
				try {
					this.localDeclarationVisitor.pushDeclaringType(declaringType);
					field.initialization.traverse(this.localDeclarationVisitor, (MethodScope) null);
				} finally {
					this.localDeclarationVisitor.popDeclaringType();
				}
			}
	}
}

private void visitIfNeeded(Initializer initializer) {
	if (this.localDeclarationVisitor != null 
		&& (initializer.bits & ASTNode.HasLocalType) != 0) {
			if (initializer.block != null) {
				initializer.block.traverse(this.localDeclarationVisitor, null);
			}
	}
}
}

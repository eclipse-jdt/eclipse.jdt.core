package org.eclipse.jdt.internal.codeassist.select;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/*
 * Parser able to build specific completion parse nodes, given a cursorLocation.
 *
 * Cursor location denotes the position of the last character behind which completion
 * got requested:
 *  -1 means completion at the very beginning of the source
 *	0  means completion behind the first character
 *  n  means completion behind the n-th character
 */
 
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.env.*;

import org.eclipse.jdt.internal.codeassist.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.CharOperation;

public class SelectionParser extends AssistParser {

	/* public fields */

	public int selectionStart, selectionEnd;
	public AstNode selectionNode;

	public static final char[] SUPER = "super"/*nonNLS*/.toCharArray();
public SelectionParser(ProblemReporter problemReporter) {
	super(problemReporter);
}
public char[] assistIdentifier(){
	return ((SelectionScanner)scanner).selectionIdentifier;
}
protected void attachOrphanCompletionNode(){
	if (isOrphanCompletionNode){
		isOrphanCompletionNode = false;
		Statement statement = (Statement)wrapWithExplicitConstructorCallIfNeeded(this.assistNode);
		currentElement = currentElement.add(statement, 0);
		currentToken = 0; // given we are not on an eof, we do not want side effects caused by looked-ahead token
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	if (this.indexOfAssistIdentifier() < 0) {
		super.classInstanceCreation(alwaysQualified);
		return;
	} 
	QualifiedAllocationExpression alloc;
	int length;
	if (((length = astLengthStack[astLengthPtr--]) == 1)
		&& (astStack[astPtr] == null)) {
		//NO ClassBody
		astPtr--;
		alloc = new SelectionOnQualifiedAllocationExpression();
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		// trick to avoid creating a selection on type reference
		char [] oldIdent = this.assistIdentifier();
		this.setAssistIdentifier(null);			
		alloc.type = getTypeReference(0);
		this.setAssistIdentifier(oldIdent);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);

		this.assistNode = alloc;
		this.lastCheckPoint = alloc.sourceEnd + 1;
		restartRecovery = true; // force to restart into recovery mode
		isOrphanCompletionNode = true;				
	}
}
protected void consumeArrayCreationExpression() {
	// ArrayCreationExpression ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializeropt
	// ArrayCreationExpression ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializeropt

	super.consumeArrayCreationExpression();

	ArrayAllocationExpression alloc = (ArrayAllocationExpression)expressionStack[expressionPtr];
	if (alloc.type == assistNode){
		restartRecovery = true;
		isOrphanCompletionNode = true; 
	}
}
protected void consumeEnterAnonymousClassBody() {
	// EnterAnonymousClassBody ::= $empty

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeEnterAnonymousClassBody();
		return;
	}
	QualifiedAllocationExpression alloc;
	AnonymousLocalTypeDeclaration anonymousType = 
		new AnonymousLocalTypeDeclaration(); 
	alloc = 
		anonymousType.allocation = new SelectionOnQualifiedAllocationExpression(anonymousType); 
	pushOnAstStack(anonymousType);

	alloc.sourceEnd = rParenPos; //the position has been stored explicitly
	int argumentLength;
	if ((argumentLength = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= argumentLength;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			alloc.arguments = new Expression[argumentLength], 
			0, 
			argumentLength); 
	}
	// trick to avoid creating a selection on type reference
	char [] oldIdent = this.assistIdentifier();
	this.setAssistIdentifier(null);			
	alloc.type = getTypeReference(0);
	this.setAssistIdentifier(oldIdent);		

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	assistNode = alloc;
	this.lastCheckPoint = alloc.sourceEnd + 1;
	restartRecovery = true; // force to restart into recovery mode
	isOrphanCompletionNode = true;	
		
	anonymousType.bodyStart = scanner.currentPosition;	
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = anonymousType.bodyStart;
		currentElement = currentElement.add(anonymousType, 0); // the recoveryTokenCheck will deal with the open brace
		lastIgnoredToken = -1;		
	}
}
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	// do nothing by default

	super.consumeEnterVariable();

	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	if (variable.type == assistNode){
		restartRecovery = true;
		isOrphanCompletionNode = false; // already attached inside variable decl
	}
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFieldAccess(isSuperAccess);
		return;
	} 
	FieldReference fieldReference = 
		new SelectionOnFieldReference(
			identifierStack[identifierPtr], 
			identifierPositionStack[identifierPtr--]);
	identifierLengthPtr--;
	if (isSuperAccess) { //considerates the fieldReferenceerence beginning at the 'super' ....	
		fieldReference.sourceStart = intStack[intPtr--];
		fieldReference.receiver = new SuperReference(fieldReference.sourceStart, endPosition);
		pushOnExpressionStack(fieldReference);
	} else { //optimize push/pop
		if ((fieldReference.receiver = expressionStack[expressionPtr]).isThis()) { //fieldReferenceerence begins at the this
			fieldReference.sourceStart = fieldReference.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fieldReference;
	}
	assistNode = fieldReference;
	this.lastCheckPoint = fieldReference.sourceEnd + 1;
	restartRecovery	= true;	// force to restart in recovery mode
	isOrphanCompletionNode = true;		
}
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	char[] selector = identifierStack[identifierPtr];
	if (!(selector == this.assistIdentifier() && CharOperation.equals(selector, SUPER))){
		super.consumeMethodInvocationName();
		return;
	}	
	ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(ExplicitConstructorCall.Super);
	constructorCall.sourceEnd = rParenPos;
	constructorCall.sourceStart = (int) (identifierPositionStack[identifierPtr] >>> 32);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}

	pushOnAstStack(constructorCall);
	this.assistNode = constructorCall;	
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	restartRecovery	= true;	// force to restart in recovery mode
	isOrphanCompletionNode = true;	
}
protected void consumeMethodInvocationPrimary() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'

	char[] selector = identifierStack[identifierPtr];
	if (!(selector == this.assistIdentifier() && CharOperation.equals(selector, SUPER))){
		super.consumeMethodInvocationPrimary();
		return;
	}
	ExplicitConstructorCall constructorCall = new SelectionOnExplicitConstructorCall(ExplicitConstructorCall.Super);
	constructorCall.sourceEnd = rParenPos;
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, constructorCall.arguments = new Expression[length], 0, length);
	}
	constructorCall.qualification = expressionStack[expressionPtr--];
	constructorCall.sourceStart = constructorCall.qualification.sourceStart;
	
	pushOnAstStack(constructorCall);
	this.assistNode = constructorCall;
	this.lastCheckPoint = constructorCall.sourceEnd + 1;
	restartRecovery	= true;	// force to restart in recovery mode
	isOrphanCompletionNode = true;	
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	int index;

	/* no need to take action if not inside assist identifiers */
	if ((index = indexOfAssistIdentifier()) < 0) {
		super.consumeTypeImportOnDemandDeclarationName();
		return;
	}
	/* retrieve identifiers subset and whole positions, the assist node positions
		should include the entire replaced source. */
	int length = identifierLengthStack[identifierLengthPtr];
	char[][] subset = identifierSubSet(index+1); // include the assistIdentifier
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length); 

	/* build specific assist node on import statement */
	ImportReference reference = this.createAssistImportReference(subset, positions);
	reference.onDemand = true;
	assistNode = reference;
	this.lastCheckPoint = reference.sourceEnd + 1;
	
	pushOnAstStack(reference);

	if (currentToken == TokenNameSEMICOLON){
		reference.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		reference.declarationSourceEnd = (int) positions[length-1];
	}
	//endPosition is just before the ;
	reference.declarationSourceStart = intStack[intPtr--];
	// flush annotations defined prior to import statements
	reference.declarationSourceEnd = this.flushAnnotationsDefinedPriorTo(reference.declarationSourceEnd);

	// recovery
	if (currentElement != null){
		lastCheckPoint = reference.declarationSourceEnd+1;
		currentElement = currentElement.add(reference, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
public ImportReference createAssistImportReference(char[][] tokens, long[] positions){
	return new SelectionOnImportReference(tokens, positions);
}
public ImportReference createAssistPackageReference(char[][] tokens, long[] positions){
	return new SelectionOnPackageReference(tokens, positions);
}
public NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new SelectionOnQualifiedNameReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new SelectionOnQualifiedTypeReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public NameReference createSingleAssistNameReference(char[] name, long position) {
	return new SelectionOnSingleNameReference(name, position);
}
public TypeReference createSingleAssistTypeReference(char[] name, long position) {
	return new SelectionOnSingleTypeReference(name, position);
}
public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int selectionStart, int selectionEnd) {

	this.selectionStart = selectionStart;
	this.selectionEnd = selectionEnd;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = selectionStart;
	selectionScanner.selectionEnd = selectionEnd;	
	return this.dietParse(sourceUnit, compilationResult);
}
/*
 * Flush parser/scanner state regarding to code assist
 */
public void flushAssistState() {

	super.flushAssistState();
	this.selectionNode = null;
	this.setAssistIdentifier(null);
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int completionIndex;

	/* no need to take action if not inside completed identifiers */
	if ((completionIndex = indexOfAssistIdentifier()) < 0) {
		return super.getUnspecifiedReference();
	}

	int length = identifierLengthStack[identifierLengthPtr];
	if (CharOperation.equals(assistIdentifier(), SUPER)){
		Reference reference;
		if (completionIndex > 0){ // qualified super
			// discard 'super' from identifier stacks
			identifierLengthStack[identifierLengthPtr] = completionIndex;
			int ptr = identifierPtr -= (length - completionIndex);
			reference = 
				new SelectionOnQualifiedSuperReference(
					getTypeReference(0), 
					(int)(identifierPositionStack[ptr+1] >>> 32),
					(int) identifierPositionStack[ptr+1]);
		} else { // standard super
			identifierPtr -= length;
			identifierLengthPtr--;
			reference = new SelectionOnSuperReference((int)(identifierPositionStack[identifierPtr+1] >>> 32), (int) identifierPositionStack[identifierPtr+1]);
		}
		pushOnAstStack(reference);
		this.assistNode = reference;	
		this.lastCheckPoint = reference.sourceEnd + 1;
		restartRecovery	= true;	// force to restart in recovery mode
		isOrphanCompletionNode = true;	
		return new SingleNameReference(new char[0], 0); // dummy reference
	}
	NameReference nameReference;
	/* retrieve identifiers subset and whole positions, the completion node positions
		should include the entire replaced source. */
	char[][] subset = identifierSubSet(completionIndex);
	identifierLengthPtr--;
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr + 1, 
		positions, 
		0, 
		length);
	/* build specific completion on name reference */
	if (completionIndex == 0) {
		/* completion inside first identifier */
		nameReference = this.createSingleAssistNameReference(assistIdentifier(), positions[0]);
	} else {
		/* completion inside subsequent identifier */
		nameReference = this.createQualifiedAssistNameReference(subset, assistIdentifier(), positions);
	}
	assistNode = nameReference;
	this.lastCheckPoint = nameReference.sourceEnd + 1;
	isOrphanCompletionNode = true;
	restartRecovery = true; // force to restart into recovery mode
	return nameReference;
}
/*
 * Copy of code from superclass with the following change:
 * In the case of qualified name reference if the cursor location is on the 
 * qualified name reference, then create a CompletionOnQualifiedNameReference 
 * instead.
 */
protected NameReference getUnspecifiedReferenceOptimized() {

	int index = indexOfAssistIdentifier();
	NameReference reference = super.getUnspecifiedReferenceOptimized();

	if (index >= 0){
		restartRecovery = true; // force to stop and restart in recovery mode
		isOrphanCompletionNode = true;		
	}
	return reference;
}
public void initializeScanner(){
	this.scanner = new SelectionScanner();
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	char[] selector = identifierStack[identifierPtr];
	if (selector != this.assistIdentifier()){
		return super.newMessageSend();
	}	
	MessageSend messageSend = new SelectionOnMessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			messageSend.arguments = new Expression[length], 
			0, 
			length); 
	};
	assistNode = messageSend;
	restartRecovery	= true;	// force to restart in recovery mode
	isOrphanCompletionNode = true;	
	return messageSend;
}
public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int selectionStart, int selectionEnd) {

	this.selectionStart = selectionStart;
	this.selectionEnd = selectionEnd;	
	SelectionScanner selectionScanner = (SelectionScanner)this.scanner;
	selectionScanner.selectionIdentifier = null;
	selectionScanner.selectionStart = selectionStart;
	selectionScanner.selectionEnd = selectionEnd;	
	return this.parse(sourceUnit, compilationResult);
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	/* if reached assist node inside method body, but still inside nested type,
		should continue in diet mode until the end of the method body */
	if (this.assistNode != null
		&& !(referenceContext instanceof CompilationUnitDeclaration)){
		currentElement.preserveEnclosingBlocks();
		if (currentElement.enclosingType() == null){
			this.resetStacks();
			return false;
		}
	}
	return super.resumeAfterRecovery();			
}
public void setAssistIdentifier(char[] assistIdent){
	((SelectionScanner)scanner).selectionIdentifier = assistIdent;
}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* may be able to retrieve completionNode as an orphan, and then attach it */
	this.attachOrphanCompletionNode();
	
	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}
}

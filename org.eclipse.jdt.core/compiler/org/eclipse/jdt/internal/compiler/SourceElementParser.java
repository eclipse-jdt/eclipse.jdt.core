package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
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
import java.lang.reflect.Constructor;

import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

public class SourceElementParser extends Parser {
	ISourceElementRequestor requestor;
	private int fieldCount;
	private int localIntPtr;
	private int lastFieldEndPosition;
	private ISourceType sourceType;
	private boolean reportReferenceInfo;
	private char[][] typeNames;
	private char[][] superTypeNames;
	private int nestedTypeIndex;
	private static final char[] JAVA_LANG_OBJECT = "java.lang.Object"/*nonNLS*/.toCharArray();
public SourceElementParser(
	final ISourceElementRequestor requestor, 
	IProblemFactory problemFactory) {
	// we want to notify all syntax error with the acceptProblem API
	// To do so, we define the record method of the ProblemReporter
	super(new ProblemReporter(
		DefaultErrorHandlingPolicies.exitAfterAllProblems(), 
		new CompilerOptions(), 
		problemFactory) {
		public void record(IProblem problem, CompilationResult unitResult) {
			unitResult.record(problem);
			requestor.acceptProblem(problem);
		}
	}, false);
	this.requestor = requestor;
	typeNames = new char[4][];
	superTypeNames = new char[4][];
	nestedTypeIndex = 0;
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
				: CharOperation.concatWith(alloc.type.getTypeName(), '.'),
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
	SourceConstructorDeclaration cd = new SourceConstructorDeclaration();

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];

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
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// the scanner is located after the comma or the semi-colon.
	// we want to include the comma or the semi-colon
	super.consumeExitVariableWithInitialization();
	if (isLocalDeclaration() || ((currentToken != TokenNameCOMMA) && (currentToken != TokenNameSEMICOLON)))
		return;
	((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	if (isLocalDeclaration() || ((currentToken != TokenNameCOMMA) && (currentToken != TokenNameSEMICOLON)))
		return;
	((SourceFieldDeclaration) astStack[astPtr]).fieldEndPosition = scanner.currentPosition - 1;
}
/**
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
	int length;
	SourceMethodDeclaration md = new SourceMethodDeclaration();

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSourcePositions = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSourcePositions >>> 32);
	md.selectorSourceEnd = (int) selectorSourcePositions;
	pushOnAstStack(md);
	md.sourceEnd = lParenPos;
	md.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws
	
	// recovery
	if (currentElement != null){
		if (currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (scanner.searchLineNumber(md.returnType.sourceStart)
					== scanner.searchLineNumber(md.sourceStart))){
			lastCheckPoint = md.bodyStart;
			currentElement = currentElement.add(md, 0);
			lastIgnoredToken = -1;			
		} else {
			lastCheckPoint = md.sourceStart;
			restartRecovery = true;
		}
	}		
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)
	super.consumeMethodInvocationName();
	MessageSend messageSend = (MessageSend) expressionStack[expressionPtr];
	Expression[] args = messageSend.arguments;
	if (reportReferenceInfo) {
		requestor.acceptMethodReference(
			messageSend.selector, 
			args == null ? 0 : args.length, 
			(int)(messageSend.nameSourcePosition >>> 32));
	}
}
/**
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
/**
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
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeSingleTypeImportDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptTypeReference(impt.tokens, impt.sourceStart(), impt.sourceEnd());
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	super.consumeTypeImportOnDemandDeclarationName();
	ImportReference impt = (ImportReference)astStack[astPtr];
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(impt.tokens, impt.sourceStart(), impt.sourceEnd());
	}
}
protected FieldDeclaration createFieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	return new SourceFieldDeclaration(null, name, sourceStart, sourceEnd);
}
protected CompilationUnitDeclaration endParse(int act) {

	if (sourceType != null) {
		if (sourceType.isInterface()) {
			consumeInterfaceDeclaration();
		} else {
			consumeClassDeclaration();
		}
	}
	CompilationUnitDeclaration result = super.endParse(act);
	notifySourceElementRequestor();
	return result;
}
/*
 * Flush annotations defined prior to a given positions.
 *
 * Note: annotations are stacked in syntactical order
 *
 * Either answer given <position>, or the end position of a comment line 
 * immediately following the <position> (same line)
 *
 * e.g.
 * void foo(){
 * } // end of method foo
 */
 
public int flushAnnotationsDefinedPriorTo(int position) {

	return lastFieldEndPosition = super.flushAnnotationsDefinedPriorTo(position);
}
public TypeReference getTypeReference(int dim) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions
	 */
	int length;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
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
			if (reportReferenceInfo) {
				requestor.acceptTypeReference(ref.token, ref.sourceStart);
			}
			return ref;
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			TypeReference ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[intPtr--];
			return ref;
		} else { //Qualified variable reference
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
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart(), ref.sourceEnd());
				}
				return ref;
			} else {
				ArrayQualifiedTypeReference ref = 
					new ArrayQualifiedTypeReference(tokens, dim, positions); 
				if (reportReferenceInfo) {
					requestor.acceptTypeReference(ref.tokens, ref.sourceStart(), ref.sourceEnd());
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
			requestor.acceptUnknownReference(ref.token, ref.sourceStart());
		}
		return ref;
	} else {
		//Qualified variable reference
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		QualifiedNameReference ref = 
			new QualifiedNameReference(
				tokens, 
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), 
		// sourceStart
		 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
		if (reportReferenceInfo) {
			requestor.acceptUnknownReference(
				ref.tokens, 
				ref.sourceStart(), 
				ref.sourceEnd()); 
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
		ref.bits &= ~NameReference.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		if (reportReferenceInfo) {
			requestor.acceptUnknownReference(ref.token, ref.sourceStart());
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
	QualifiedNameReference ref = 
		new QualifiedNameReference(
			tokens, 
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), 
	// sourceStart
	 (int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~NameReference.RestrictiveFlagMASK;
	ref.bits |= LOCAL | FIELD;
	if (reportReferenceInfo) {
		requestor.acceptUnknownReference(
			ref.tokens, 
			ref.sourceStart(), 
			ref.sourceEnd());
	}
	return ref;
}
/**
 *
 * INTERNAL USE-ONLY
 */
private boolean isLocalDeclaration() {
	int nestedDepth = nestedType;
	while (nestedDepth >= 0) {
		if (nestedMethod[nestedDepth] != 0) {
			return true;
		}
		nestedDepth--;
	}
	return false;
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor() {

	if (sourceType == null){
		if (scanner.initialPosition == 0) {
			requestor.enterCompilationUnit();
		}
		// first we notify the package declaration
		ImportReference currentPackage = compilationUnit.currentPackage;
		if (currentPackage != null) {
			notifySourceElementRequestor(currentPackage, true);
		}
		// then the imports
		ImportReference[] imports = compilationUnit.imports;
		if (imports != null) {
			for (int i = 0, max = imports.length; i < max; i++) {
				notifySourceElementRequestor(imports[i], false);
			}
		}
	}
	// then the types contained by this compilation unit
	TypeDeclaration[] types = compilationUnit.types;
	if (types != null) {
		for (int i = 0, max = types.length; i < max; i++) {
			notifySourceElementRequestor(types[i], sourceType == null);
		}
	}
	if (sourceType == null){
		if (scanner.eofPosition >= compilationUnit.sourceEnd) {
			requestor.exitCompilationUnit(compilationUnit.sourceEnd);
		}
	}
}
/*
 * Update the bodyStart of the corresponding parse node
 */
public void notifySourceElementRequestor(AbstractMethodDeclaration methodDeclaration) {
	if (methodDeclaration.isClinit())
		return;

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
			argumentTypes[i] = returnTypeName(arguments[i].type);
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
				CharOperation.concatWith(thrownExceptions[i].getTypeName(), '.'); 
		}
	}
	// by default no selector end position
	int selectorSourceEnd = -1;
	if (methodDeclaration.isConstructor()) {
		if (methodDeclaration instanceof SourceConstructorDeclaration) {
			selectorSourceEnd = 
				((SourceConstructorDeclaration) methodDeclaration).selectorSourceEnd; 
		}
		if (scanner.initialPosition <= methodDeclaration.declarationSourceStart) {
			requestor.enterConstructor(
				methodDeclaration.declarationSourceStart, 
				methodDeclaration.modifiers, 
				methodDeclaration.selector, 
				methodDeclaration.sourceStart, 
				selectorSourceEnd, 
				argumentTypes, 
				argumentNames, 
				thrownExceptionTypes);
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
		}
		if (scanner.eofPosition >= methodDeclaration.declarationSourceEnd) {
			requestor.exitConstructor(methodDeclaration.declarationSourceEnd);
		}
		return;
	}
	if (methodDeclaration instanceof SourceMethodDeclaration) {
		selectorSourceEnd = 
			((SourceMethodDeclaration) methodDeclaration).selectorSourceEnd; 
	}
	requestor.enterMethod(
		methodDeclaration.declarationSourceStart, 
		methodDeclaration.modifiers, 
		returnTypeName(((MethodDeclaration) methodDeclaration).returnType), 
		methodDeclaration.selector, 
		methodDeclaration.sourceStart, 
		selectorSourceEnd, 
		argumentTypes, 
		argumentNames, 
		thrownExceptionTypes); 
	requestor.exitMethod(methodDeclaration.declarationSourceEnd);
}
/*
* Update the bodyStart of the corresponding parse node
*/
public void notifySourceElementRequestor(FieldDeclaration fieldDeclaration) {
	if (fieldDeclaration.isField()) {
		int fieldEndPosition = fieldDeclaration.declarationSourceEnd;
		if (fieldDeclaration instanceof SourceFieldDeclaration) {
			fieldEndPosition = ((SourceFieldDeclaration) fieldDeclaration).fieldEndPosition;
			if (fieldEndPosition == 0) {
				// use the declaration source end by default
				fieldEndPosition = fieldDeclaration.declarationSourceEnd;
			}
		}
		requestor.enterField(
			fieldDeclaration.declarationSourceStart, 
			fieldDeclaration.modifiers, 
			returnTypeName(fieldDeclaration.type), 
			fieldDeclaration.name, 
			fieldDeclaration.sourceStart, 
			fieldDeclaration.sourceEnd); 
		requestor.exitField(fieldEndPosition);

	} else {
		requestor.acceptInitializer(
			fieldDeclaration.modifiers, 
			fieldDeclaration.declarationSourceStart, 
			fieldDeclaration.declarationSourceEnd); 
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
			importReference.onDemand); 
	}
}
public void notifySourceElementRequestor(TypeDeclaration typeDeclaration, boolean notifyTypePresence) {
	FieldDeclaration[] fields = typeDeclaration.fields;
	AbstractMethodDeclaration[] methods = typeDeclaration.methods;
	MemberTypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
	int fieldCount = fields == null ? 0 : fields.length;
	int methodCount = methods == null ? 0 : methods.length;
	int memberTypeCount = memberTypes == null ? 0 : memberTypes.length;
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
		}
		if (superInterfaces != null) {
			for (int i = 0; i < superInterfacesLength; i++) {
				interfaceNames[i] = 
					CharOperation.concatWith(superInterfaces[i].getTypeName(), '.'); 
			}
		}
		if (isInterface) {
			requestor.enterInterface(
				typeDeclaration.declarationSourceStart, 
				typeDeclaration.modifiers, 
				typeDeclaration.name, 
				typeDeclaration.sourceStart, 
				typeDeclaration.sourceEnd, 
				interfaceNames);
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
				requestor.enterClass(
					typeDeclaration.declarationSourceStart, 
					typeDeclaration.modifiers, 
					typeDeclaration.name, 
					typeDeclaration.sourceStart, 
					typeDeclaration.sourceEnd, 
					null, 
					interfaceNames); 
			} else {
				requestor.enterClass(
					typeDeclaration.declarationSourceStart, 
					typeDeclaration.modifiers, 
					typeDeclaration.name, 
					typeDeclaration.sourceStart, 
					typeDeclaration.sourceEnd, 
					CharOperation.concatWith(superclass.getTypeName(), '.'), 
					interfaceNames); 
			}
			if (nestedTypeIndex == typeNames.length) {
				// need a resize
				System.arraycopy(typeNames, 0, (typeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
				System.arraycopy(superTypeNames, 0, (superTypeNames = new char[nestedTypeIndex * 2][]), 0, nestedTypeIndex);
			}
			typeNames[nestedTypeIndex] = typeDeclaration.name;
			superTypeNames[nestedTypeIndex++] = superclass == null ? JAVA_LANG_OBJECT : CharOperation.concatWith(superclass.getTypeName(), '.');
		}
	}
	while ((fieldIndex < fieldCount)
		|| (memberTypeIndex < memberTypeCount)
		|| (methodIndex < methodCount)) {
		FieldDeclaration nextFieldDeclaration = null;
		AbstractMethodDeclaration nextMethodDeclaration = null;
		TypeDeclaration nextMemberDeclaration = null;

		int position = Integer.MAX_VALUE;
		int nextDeclarationType = -1;
		if (fieldIndex < fieldCount) {
			nextFieldDeclaration = fields[fieldIndex];
			if (nextFieldDeclaration.declarationSourceStart < position) {
				position = nextFieldDeclaration.declarationSourceStart;
				nextDeclarationType = 0; // FIELD
			}
		}
		if (methodIndex < methodCount) {
			nextMethodDeclaration = methods[methodIndex];
			if (nextMethodDeclaration.declarationSourceStart < position) {
				position = nextMethodDeclaration.declarationSourceStart;
				nextDeclarationType = 1; // METHOD
			}
		}
		if (memberTypeIndex < memberTypeCount) {
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
		if (isInterface) {
			requestor.exitInterface(typeDeclaration.declarationSourceEnd);
		} else {
			requestor.exitClass(typeDeclaration.declarationSourceEnd);
		}
		nestedTypeIndex--;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	int start, 
	int end, 
	boolean needReferenceInfo) {

	CompilationUnitDeclaration result;
	reportReferenceInfo = needReferenceInfo;
	boolean old = diet;
	try {
		diet = !needReferenceInfo;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0);
		result = parse(unit, compilationUnitResult, start, end);
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.lineEnds());
		}
		diet = old;
	}
}
public void parseCompilationUnit(
	ICompilationUnit unit, 
	boolean needReferenceInfo) {

	CompilationUnitDeclaration result;
	boolean old = diet;
	try {
		diet = !needReferenceInfo;
		reportReferenceInfo = needReferenceInfo;
		CompilationResult compilationUnitResult = new CompilationResult(unit, 0, 0);
		result = parse(unit, compilationUnitResult);
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.lineEnds());
		}
		diet = old;
	}
}
public void parseTypeMemberDeclarations(
	ISourceType sourceType, 
	ICompilationUnit sourceUnit, 
	int start, 
	int end, 
	boolean needReferenceInfo) {

	boolean old = diet;
	try {
		diet = !needReferenceInfo;
		reportReferenceInfo = needReferenceInfo;
		CompilationResult compilationUnitResult = 
			new CompilationResult(sourceUnit, 0, 0); 

		CompilationUnitDeclaration unit = 
			SourceTypeConverter.buildCompilationUnit(
				new ISourceType[]{sourceType}, 
				false,
				false, 
				problemReporter(), 
				compilationUnitResult); 
		if ((unit == null) || (unit.types == null) || (unit.types.length != 1))
			return;

		this.sourceType = sourceType;

		try {
			/* automaton initialization */
			initialize();
			goForClassBodyDeclarations();
			/* scanner initialization */
			scanner.setSourceBuffer(sourceUnit.getContents());
			int sourceLength = scanner.source.length;
			scanner.resetTo(start, end);
			/* unit creation */
			referenceContext = compilationUnit = unit;

			/* initialize the astStacl */
			// the compilationUnitDeclaration should contain exactly one type
			pushOnAstStack(unit.types[0]);
			/* run automaton */
			parse();
		} finally {
			unit = compilationUnit;
			compilationUnit = null; // reset parser
		}
	} catch (AbortCompilation e) {
	} finally {
		if (scanner.recordLineSeparator) {
			requestor.acceptLineSeparatorPositions(scanner.lineEnds());
		}
		diet = old;
	}
}
/*
 * Answer a char array representation of the type name formatted like:
 * - type name + dimensions
 * Example:
 * "A[][]".toCharArray()
 * "java.lang.String".toCharArray()
 */
private char[] returnTypeName(TypeReference type) {
	if (type == null)
		return null;
	int dimension = type.dimensions();
	if (dimension != 0) {
		char[] dimensionsArray = new char[dimension * 2];
		for (int i = 0; i < dimension; i++) {
			dimensionsArray[i * 2] = '[';
			dimensionsArray[(i * 2) + 1] = ']';
		}
		return CharOperation.concat(
			CharOperation.concatWith(type.getTypeName(), '.'), 
			dimensionsArray); 
	}
	return CharOperation.concatWith(type.getTypeName(), '.');
}
private TypeReference typeReference(
	int dim, 
	int localIdentifierPtr, 
	int localIdentifierLengthPtr) {
	/* build a Reference on a variable that may be qualified or not
	 * This variable is a type reference and dim will be its dimensions.
	 * We don't have any side effect on the stacks' pointers.
	 */

	int length;
	TypeReference ref;
	if ((length = identifierLengthStack[localIdentifierLengthPtr]) == 1) {
		// single variable reference
		if (dim == 0) {
			ref = 
				new SingleTypeReference(
					identifierStack[localIdentifierPtr], 
					identifierPositionStack[localIdentifierPtr--]); 
		} else {
			ref = 
				new ArrayTypeReference(
					identifierStack[localIdentifierPtr], 
					dim, 
					identifierPositionStack[localIdentifierPtr--]); 
		}
	} else {
		if (length < 0) { //flag for precompiled type reference on base types
			ref = TypeReference.baseTypeReference(-length, dim);
			ref.sourceStart = intStack[localIntPtr--];
		} else { //Qualified variable reference
			char[][] tokens = new char[length][];
			localIdentifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, localIdentifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
				localIdentifierPtr + 1, 
				positions, 
				0, 
				length); 
			if (dim == 0)
				ref = new QualifiedTypeReference(tokens, positions);
			else
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
		}
	};
	return ref;
}
}

/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist.complete;

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

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.codeassist.impl.*;

public class CompletionParser extends AssistParser {
	// OWNER
	protected static final int COMPLETION_PARSER = 1024;
	protected static final int COMPLETION_OR_ASSIST_PARSER = ASSIST_PARSER + COMPLETION_PARSER;
	
	// KIND : all values known by CompletionParser are between 1025 and 1549
	protected static final int K_BLOCK_DELIMITER = COMPLETION_PARSER + 1; // whether we are inside a block
	protected static final int K_SELECTOR_INVOCATION_TYPE = COMPLETION_PARSER + 2; // whether we are inside a message send
	protected static final int K_SELECTOR_QUALIFIER = COMPLETION_PARSER + 3; // whether we are inside a message send
	protected static final int K_BETWEEN_CATCH_AND_RIGHT_PAREN = COMPLETION_PARSER + 4; // whether we are between the keyword 'catch' and the following ')'
	protected static final int K_NEXT_TYPEREF_IS_CLASS = COMPLETION_PARSER + 5; // whether the next type reference is a class
	protected static final int K_NEXT_TYPEREF_IS_INTERFACE = COMPLETION_PARSER + 6; // whether the next type reference is an interface
	protected static final int K_NEXT_TYPEREF_IS_EXCEPTION = COMPLETION_PARSER + 7; // whether the next type reference is an exception
	protected static final int K_BETWEEN_NEW_AND_LEFT_BRACKET = COMPLETION_PARSER + 8; // whether we are between the keyword 'new' and the following left braket, ie. '[', '(' or '{'
	protected static final int K_INSIDE_THROW_STATEMENT = COMPLETION_PARSER + 9; // whether we are between the keyword 'throw' and the end of a throw statement
	protected static final int K_INSIDE_RETURN_STATEMENT = COMPLETION_PARSER + 10; // whether we are between the keyword 'return' and the end of a return statement
	protected static final int K_CAST_STATEMENT = COMPLETION_PARSER + 11; // whether we are between ')' and the end of a cast statement
	protected static final int K_LOCAL_INITIALIZER_DELIMITER = COMPLETION_PARSER + 12;
	protected static final int K_ARRAY_INITIALIZER = COMPLETION_PARSER + 13;
	protected static final int K_ARRAY_CREATION = COMPLETION_PARSER + 14;
	protected static final int K_UNARY_OPERATOR = COMPLETION_PARSER + 15;
	protected static final int K_BINARY_OPERATOR = COMPLETION_PARSER + 16;
	protected static final int K_ASSISGNMENT_OPERATOR = COMPLETION_PARSER + 17;
	protected static final int K_CONDITIONAL_OPERATOR = COMPLETION_PARSER + 18;
	protected static final int K_BETWEEN_IF_AND_RIGHT_PAREN = COMPLETION_PARSER + 19;
	protected static final int K_BETWEEN_WHILE_AND_RIGHT_PAREN = COMPLETION_PARSER + 20;
	protected static final int K_BETWEEN_FOR_AND_RIGHT_PAREN = COMPLETION_PARSER + 21;
	protected static final int K_BETWEEN_SWITCH_AND_RIGHT_PAREN = COMPLETION_PARSER + 22;
	protected static final int K_BETWEEN_SYNCHRONIZED_AND_RIGHT_PAREN = COMPLETION_PARSER + 23;
	protected static final int K_INSIDE_ASSERT_STATEMENT = COMPLETION_PARSER + 24;
	protected static final int K_SWITCH_LABEL= COMPLETION_PARSER + 25;
	protected static final int K_BETWEEN_CASE_AND_COLON = COMPLETION_PARSER + 26;
	protected static final int K_BETWEEN_DEFAULT_AND_COLON = COMPLETION_PARSER + 27;
	protected static final int K_BETWEEN_LEFT_AND_RIGHT_BRACKET = COMPLETION_PARSER + 28;
	

	/* public fields */

	public int cursorLocation;
	public AstNode assistNodeParent; // the parent node of assist node
	/* the following fields are internal flags */
	
	// block kind
	static final int IF = 1;
	static final int TRY = 2;
	static final int CATCH = 3;
	static final int WHILE = 4;
	static final int SWITCH = 5;
	static final int FOR = 6;
	static final int DO = 7;
	static final int SYNCHRONIZED = 8;
	static final int METHOD = 9;
	
	// label kind
	static final int DEFAULT = 1;
	
	// invocation type constants
	static final int EXPLICIT_RECEIVER = 0;
	static final int NO_RECEIVER = -1;
	static final int SUPER_RECEIVER = -2;
	static final int NAME_RECEIVER = -3;
	static final int ALLOCATION = -4;
	static final int QUALIFIED_ALLOCATION = -5;
	
	static final int QUESTION = 1;
	static final int COLON = 2;

	// the type of the current invocation (one of the invocation type constants)
	int invocationType;

	// a pointer in the expression stack to the qualifier of a invocation
	int qualifier;

	// last modifiers info
	int lastModifiers = AccDefault;
	int lastModifiersStart = -1;
	
	// depth of '(', '{' and '[]'
	int bracketDepth;
	
	// show if the current token can be an explicit constructor
	int canBeExplicitConstructor = NO;
	static final int NO = 0;
	static final int NEXTTOKEN = 1;
	static final int YES = 2;
	
	
public CompletionParser(ProblemReporter problemReporter, boolean assertMode) {
	super(problemReporter, assertMode);
}
public char[] assistIdentifier(){
	return ((CompletionScanner)scanner).completionIdentifier;
}
protected void attachOrphanCompletionNode(){
	if(assistNode == null) return;
	
	if (this.isOrphanCompletionNode) {
		AstNode orphan = this.assistNode;
		this.isOrphanCompletionNode = false;
		
		if (currentElement instanceof RecoveredUnit){
			if (orphan instanceof ImportReference){
				currentElement.add((ImportReference)orphan, 0);
			}
		}
		
		/* if in context of a type, then persists the identifier into a fake field return type */
		if (currentElement instanceof RecoveredType){
			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (recoveredType.foundOpeningBrace) {
				/* generate a pseudo field with a completion on type reference */	
				if (orphan instanceof TypeReference){
					CompletionOnFieldType fieldDeclaration = new CompletionOnFieldType((TypeReference)orphan, false);

					// retrieve available modifiers if any
					if (intPtr >= 2 && intStack[intPtr-1] == this.lastModifiersStart && intStack[intPtr-2] == this.lastModifiers){
						fieldDeclaration.modifiersSourceStart = intStack[intPtr-1];
						fieldDeclaration.modifiers = intStack[intPtr-2];
					}

					currentElement = currentElement.add(fieldDeclaration, 0);
					return;
				}
			}
		}
		/* if in context of a method, persists if inside arguments as a type */
		if (currentElement instanceof RecoveredMethod){
			RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
			/* only consider if inside method header */
			if (!recoveredMethod.foundOpeningBrace) {
				//if (rParenPos < lParenPos){ // inside arguments
				if (orphan instanceof TypeReference){
					currentElement = currentElement.parent.add(
						new CompletionOnFieldType((TypeReference)orphan, true), 0);
					return;
				}
			}
		}

		// add the completion node to the method declaration or constructor declaration
		if (orphan instanceof Statement) {
			/* check for completion at the beginning of method body
				behind an invalid signature
			 */
			RecoveredMethod method = currentElement.enclosingMethod();
			if (method != null){
				AbstractMethodDeclaration methodDecl = method.methodDeclaration;
				if ((methodDecl.bodyStart == methodDecl.sourceEnd+1) // was missing opening brace
					&& (scanner.getLineNumber(orphan.sourceStart) == scanner.getLineNumber(methodDecl.sourceEnd))){
					return;
				}
			}
			// add the completion node as a statement to the list of block statements
			currentElement = currentElement.add((Statement)orphan, 0);
			return;
		} 
	}
	
	// the following code applies only in methods, constructors or initializers
	if ((!isInsideMethod() && !isInsideFieldInitialization())) { 
		return;
	}
	
	// push top expression on ast stack if it contains the completion node
	Expression expression;
	if (this.expressionPtr > -1) {
		expression = this.expressionStack[this.expressionPtr];
		CompletionNodeDetector detector = new CompletionNodeDetector(assistNode, expression);
		if(detector.containsCompletionNode()) {
			/* check for completion at the beginning of method body
				behind an invalid signature
			 */
			RecoveredMethod method = currentElement.enclosingMethod();
			if (method != null){
				AbstractMethodDeclaration methodDecl = method.methodDeclaration;
				if ((methodDecl.bodyStart == methodDecl.sourceEnd+1) // was missing opening brace
					&& (scanner.getLineNumber(expression.sourceStart) == scanner.getLineNumber(methodDecl.sourceEnd))){
					return;
				}
			}
			if(expression == assistNode
				|| (expression instanceof AllocationExpression
					&& ((AllocationExpression)expression).type == assistNode)){
				buildMoreCompletionContext(expression);
			} else {
				assistNodeParent = detector.getCompletionNodeParent();
				if(assistNodeParent != null) {
					currentElement = currentElement.add((Statement)assistNodeParent, 0);
				} else {
					currentElement = currentElement.add(expression, 0);
				}
			}
		}
	}
}
private void buildMoreCompletionContext(Expression expression) {
	Statement statement = expression;
	int kind = topKnownElementKind(COMPLETION_OR_ASSIST_PARSER);
	if(kind != 0) {
		int info = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER);
		nextElement : switch (kind) {
			case K_SELECTOR_QUALIFIER :
				int selector = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER, 2);
				if(selector == THIS_CONSTRUCTOR || selector == SUPER_CONSTRUCTOR) {
					ExplicitConstructorCall call = new ExplicitConstructorCall(
						(selector == THIS_CONSTRUCTOR) ? 
							ExplicitConstructorCall.This : 
							ExplicitConstructorCall.Super
					);
					call.arguments = new Expression[] {expression};
					call.sourceStart = expression.sourceStart;
					call.sourceEnd = expression.sourceEnd;
					assistNodeParent = call;
				} else {
					int invocationType = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER,1);
					int qualifierExprPtr = info;
					
					// find arguments
					int length = expressionLengthStack[expressionLengthPtr];
					
					// search previous arguments if missing
					if(expressionLengthPtr > 0 && length == 1) {
						int start = (int) (identifierPositionStack[selector] >>> 32);
						if(this.expressionStack[expressionPtr-1] != null && this.expressionStack[expressionPtr-1].sourceStart > start) {
							length += expressionLengthStack[expressionLengthPtr-1];;	
						}

					}
					
					Expression[] arguments = null;
					if (length != 0) {
						arguments = new Expression[length];
						expressionPtr -= length;
						System.arraycopy(expressionStack, expressionPtr + 1, arguments, 0, length-1);
						arguments[length-1] = expression;
					};
					
					if(invocationType != ALLOCATION && invocationType != QUALIFIED_ALLOCATION) {
						MessageSend messageSend = new MessageSend();
						messageSend.selector = identifierStack[selector];
						messageSend.arguments = arguments;
	
						// find receiver
						switch (invocationType) {
							case NO_RECEIVER:
								messageSend.receiver = ThisReference.implicitThis();
								break;
							case NAME_RECEIVER:
								// remove special flags for primitive types
								while (this.identifierLengthPtr >= 0 && this.identifierLengthStack[this.identifierLengthPtr] < 0) {
									this.identifierLengthPtr--;
								}
								
								// remove selector 
								this.identifierPtr--; 
								this.identifierLengthStack[this.identifierLengthPtr]--;
								// consume the receiver
								messageSend.receiver = this.getUnspecifiedReference();
								break;
							case SUPER_RECEIVER:
								messageSend.receiver = new SuperReference(0, 0);
								break;
							case EXPLICIT_RECEIVER:
								messageSend.receiver = this.expressionStack[qualifierExprPtr];
								break;
							default :
								messageSend.receiver = ThisReference.implicitThis();
								break;
						}
						assistNodeParent = messageSend;
					} else {
						if(invocationType == ALLOCATION) {
							AllocationExpression allocationExpr = new AllocationExpression();
							allocationExpr.arguments = arguments;
							allocationExpr.type = getTypeReference(0);
							assistNodeParent = allocationExpr;
						} else {
							QualifiedAllocationExpression allocationExpr = new QualifiedAllocationExpression();
							allocationExpr.enclosingInstance = this.expressionStack[qualifierExprPtr];
							allocationExpr.arguments = arguments;
							allocationExpr.type = getTypeReference(0);
							assistNodeParent = allocationExpr;
						}
					}
				}
				break nextElement;
			case K_INSIDE_RETURN_STATEMENT :
				if(info == bracketDepth) {
					ReturnStatement returnStatement = new ReturnStatement(expression, expression.sourceStart, expression.sourceEnd);
					assistNodeParent = returnStatement;
				}
				break nextElement;
			case K_CAST_STATEMENT :
				Expression castType;
				if(this.expressionPtr > 0
					&& ((castType = this.expressionStack[this.expressionPtr-1]) instanceof TypeReference
						|| castType instanceof NameReference)) {
					CastExpression cast = new CastExpression(expression, getTypeReference(castType));
					cast.sourceStart = castType.sourceStart;
					cast.sourceEnd= expression.sourceEnd;
					assistNodeParent = cast;
				}
				break nextElement;
			case K_UNARY_OPERATOR :
				if(expressionPtr > -1) {
					Expression operatorExpression = null;
					switch (info) {
						case PLUS_PLUS :
							operatorExpression = new PrefixExpression(expression,IntLiteral.One, PLUS, expression.sourceStart);
							break;
						case MINUS_MINUS :
							operatorExpression = new PrefixExpression(expression,IntLiteral.One, MINUS, expression.sourceStart);
							break;
						default :
							operatorExpression = new UnaryExpression(expression, info);
							break;
					}
					if(operatorExpression != null) {
						assistNodeParent = operatorExpression;
					}
				}
				break nextElement;
			case K_BINARY_OPERATOR :
				if(expressionPtr > 0) {
					Expression operatorExpression = null;
					switch (info) {
						case AND_AND :
							operatorExpression = new AND_AND_Expression(this.expressionStack[expressionPtr-1], expression, info);
							break;
						case OR_OR :
							operatorExpression = new OR_OR_Expression(this.expressionStack[expressionPtr-1], expression, info);
							break;
						case EQUAL_EQUAL :
						case NOT_EQUAL :
							operatorExpression = new EqualExpression(this.expressionStack[expressionPtr-1], expression, info);
							break;
						case INSTANCEOF :
							// should never occur
							break;
						default :
							operatorExpression = new BinaryExpression(this.expressionStack[expressionPtr-1], expression, info);
							break;
					}
					if(operatorExpression != null) {
						assistNodeParent = operatorExpression;
					}
				}
				break nextElement;
			case K_ARRAY_INITIALIZER :
				ArrayInitializer arrayInitializer = new ArrayInitializer();
				arrayInitializer.expressions = new Expression[]{expression};
				expressionPtr -= expressionLengthStack[expressionLengthPtr--];
				
				if(expressionLengthPtr > -1
					&& expressionPtr > -1
					&& this.expressionStack[expressionPtr] != null
					&& this.expressionStack[expressionPtr].sourceStart > info) {
					expressionLengthPtr--;	
				}
					
				lastCheckPoint = scanner.currentPosition;
				
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER, 1) == K_ARRAY_CREATION) {
					ArrayAllocationExpression allocationExpression = new ArrayAllocationExpression();
					allocationExpression.type = getTypeReference(0);
					int length = expressionLengthStack[expressionLengthPtr];
					allocationExpression.dimensions = new Expression[length];

					allocationExpression.initializer = arrayInitializer;
					assistNodeParent = allocationExpression;
				} else if(currentElement instanceof RecoveredField) {
					RecoveredField recoveredField = (RecoveredField) currentElement;
					if(recoveredField.fieldDeclaration.type.dimensions() == 0) {
						Block block = new Block(0);
						block.sourceStart = info;
						currentElement = currentElement.add(block, 1);
					} else {
						statement = arrayInitializer;
					}
				} else if(currentElement instanceof RecoveredLocalVariable) {
					RecoveredLocalVariable recoveredLocalVariable = (RecoveredLocalVariable) currentElement;
					if(recoveredLocalVariable.localDeclaration.type.dimensions() == 0) {
						Block block = new Block(0);
						block.sourceStart = info;
						currentElement = currentElement.add(block, 1);
					} else {
						statement = arrayInitializer;
					}
				} else {
					statement = arrayInitializer;
				}
				break nextElement;
			case K_ARRAY_CREATION :
				ArrayAllocationExpression allocationExpression = new ArrayAllocationExpression();
				allocationExpression.type = getTypeReference(0);
				allocationExpression.dimensions = new Expression[]{expression};
				
				assistNodeParent = allocationExpression;
				break nextElement;
			case K_ASSISGNMENT_OPERATOR :
				if(expressionPtr > 0 && expressionStack[expressionPtr - 1] != null) {
					Assignment assignment;
					if(info == EQUAL) {
						assignment = new Assignment(
							expressionStack[expressionPtr - 1],
							expression,
							expression.sourceEnd
						);
					} else {
						assignment = new CompoundAssignment(
							expressionStack[expressionPtr - 1],
							expression,
							info,
							expression.sourceEnd
						);
					}
					assistNodeParent = assignment;
				}
				break nextElement;
			case K_CONDITIONAL_OPERATOR :
				if(info == QUESTION) {
					if(expressionPtr > 0) {
						expressionPtr--;
						expressionLengthPtr--;
						expressionStack[expressionPtr] = expressionStack[expressionPtr+1];
						popElement(K_CONDITIONAL_OPERATOR);
						buildMoreCompletionContext(expression);
						return;
					}
				} else {
					if(expressionPtr > 1) {
						expressionPtr = expressionPtr - 2;
						expressionLengthPtr = expressionLengthPtr - 2;
						expressionStack[expressionPtr] = expressionStack[expressionPtr+2];
						popElement(K_CONDITIONAL_OPERATOR);
						buildMoreCompletionContext(expression);
						return;
					}
				}
				break nextElement;
			case K_BETWEEN_LEFT_AND_RIGHT_BRACKET :
				ArrayReference arrayReference;
				if(expressionPtr > 0 && expressionStack[expressionPtr] == expression) {
					arrayReference =
						new ArrayReference(
							expressionStack[expressionPtr-1],
							expression);
				} else {
					arrayReference =
						new ArrayReference(
							getUnspecifiedReferenceOptimized(),
							expression);
				}
				assistNodeParent = arrayReference;
				break;
				
		}
	}
	if(assistNodeParent != null) {
		currentElement = currentElement.add((Statement)assistNodeParent, 0);
	} else {
		if(currentElement instanceof RecoveredField
			&& ((RecoveredField) currentElement).fieldDeclaration.initialization == null) {
			
			assistNodeParent = ((RecoveredField) currentElement).fieldDeclaration;
			currentElement = currentElement.add(statement, 0);
		} else if(currentElement instanceof RecoveredLocalVariable
			&& ((RecoveredLocalVariable) currentElement).localDeclaration.initialization == null) {
			
			assistNodeParent = ((RecoveredLocalVariable) currentElement).localDeclaration;
			currentElement = currentElement.add(statement, 0);
		} else {
			currentElement = currentElement.add(expression, 0);
		}
	}
}

public int bodyEnd(AbstractMethodDeclaration method){
	return cursorLocation;
}
public int bodyEnd(Initializer initializer){
	return cursorLocation;
}
/**
 * Checks if the completion is on the exception type of a catch clause.
 * Returns whether we found a completion node.
 */
private boolean checkCatchClause() {
	if ((topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_CATCH_AND_RIGHT_PAREN) && this.identifierPtr > -1) { 
		// NB: if the cursor is on the variable, then it has been reduced (so identifierPtr is -1), 
		//     thus this can only be a completion on the type of the catch clause
		pushOnElementStack(K_NEXT_TYPEREF_IS_EXCEPTION);
		this.assistNode = getTypeReference(0);
		popElement(K_NEXT_TYPEREF_IS_EXCEPTION);
		this.lastCheckPoint = this.assistNode.sourceEnd + 1;
		this.isOrphanCompletionNode = true;
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on the type following a 'new'.
 * Returns whether we found a completion node.
 */
private boolean checkClassInstanceCreation() {
	if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_NEW_AND_LEFT_BRACKET) {
		// completion on type inside an allocation expression
		
		TypeReference type;
		if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER, 1) == K_INSIDE_THROW_STATEMENT
			&& topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER, 1) == this.bracketDepth) {
			pushOnElementStack(K_NEXT_TYPEREF_IS_EXCEPTION);
			type = getTypeReference(0);
			popElement(K_NEXT_TYPEREF_IS_EXCEPTION);
		} else {
			type = getTypeReference(0);
		}
		this.assistNode = type;
		this.lastCheckPoint = type.sourceEnd + 1;
		if (this.invocationType == ALLOCATION) {
			// non qualified allocation expression
			AllocationExpression allocExpr = new AllocationExpression();
			allocExpr.type = type;
			allocExpr.sourceStart = type.sourceStart;
			allocExpr.sourceEnd = type.sourceEnd;
			pushOnExpressionStack(allocExpr);
			this.isOrphanCompletionNode = false;
		} else {
			// qualified allocation expression
			QualifiedAllocationExpression allocExpr = new QualifiedAllocationExpression();
			allocExpr.type = type;
			allocExpr.enclosingInstance = this.expressionStack[this.qualifier];
			allocExpr.sourceStart = this.intStack[this.intPtr--];
			allocExpr.sourceEnd = type.sourceEnd;
			this.expressionStack[this.qualifier] = allocExpr; // attach it now (it replaces the qualifier expression)
			this.isOrphanCompletionNode = false;
		}
		popElement(K_BETWEEN_NEW_AND_LEFT_BRACKET);
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on the dot following an array type,
 * a primitive type or an primitive array type.
 * Returns whether we found a completion node.
 */
private boolean checkClassLiteralAccess() {
	if (this.identifierLengthPtr >= 1 && this.previousToken == TokenNameDOT) { // (NB: the top id length is 1 and it is for the completion identifier)
		int length;
		// if the penultimate id length is negative, 
		// the completion is after a primitive type or a primitive array type
		if ((length = this.identifierLengthStack[this.identifierLengthPtr-1]) < 0) {
			// build the primitive type node
			int dim = this.isAfterArrayType() ? this.intStack[this.intPtr--] : 0;
			SingleTypeReference typeRef = (SingleTypeReference)TypeReference.baseTypeReference(-length, dim);
			typeRef.sourceStart = this.intStack[this.intPtr--];
			if (dim == 0) {
				typeRef.sourceEnd = this.intStack[this.intPtr--];
			} else {
				this.intPtr--;
				typeRef.sourceEnd = this.endPosition;
			}
			//typeRef.sourceEnd = typeRef.sourceStart + typeRef.token.length; // NB: It's ok to use the length of the token since it doesn't contain any unicode

			// find the completion identifier and its source positions
			char[] source = identifierStack[identifierPtr];
			long pos = this.identifierPositionStack[this.identifierPtr--];
			this.identifierLengthPtr--; // it can only be a simple identifier (so its length is one)

			// build the completion on class literal access node
			CompletionOnClassLiteralAccess access = new CompletionOnClassLiteralAccess(pos, typeRef);
			access.completionIdentifier = source;
			this.identifierLengthPtr--; // pop the length that was used to say it is a primitive type
			this.assistNode = access;
			this.isOrphanCompletionNode = true;
			return true;
		}

		// if the completion is after a regular array type
		if (isAfterArrayType()) {
			// find the completion identifier and its source positions
			char[] source = identifierStack[identifierPtr];
			long pos = this.identifierPositionStack[this.identifierPtr--];
			this.identifierLengthPtr--; // it can only be a simple identifier (so its length is one)
			
			// get the type reference
			TypeReference typeRef = getTypeReference(this.intPtr--);
			
			// build the completion on class literal access node
			CompletionOnClassLiteralAccess access = new CompletionOnClassLiteralAccess(pos, typeRef);
			access.completionIdentifier = source;
			this.assistNode = access;
			this.isOrphanCompletionNode = true;
			return true;
		}

	}
	return false;
}
private boolean checkKeyword() {
	if (currentElement instanceof RecoveredUnit) {
		RecoveredUnit unit = (RecoveredUnit) currentElement;
		int index = -1;
		if ((index = this.indexOfAssistIdentifier()) > -1) {
			char[] ident = identifierStack[index];
			long pos = identifierPositionStack[index];
			
			char[][] keywords = new char[Keywords.COUNT][];
			int count = 0;
			if(unit.typeCount == 0
				&& lastModifiers == AccDefault
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.IMPORT)) {
				keywords[count++] = Keywords.IMPORT;
			}
			if(unit.typeCount == 0
				&& unit.importCount == 0
				&& lastModifiers == AccDefault
				&& compilationUnit.currentPackage == null
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.PACKAGE)) {
				keywords[count++] = Keywords.PACKAGE;
			}
			if((lastModifiers & AccPublic) == 0
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.PUBLIC)) {
				boolean hasNoPublicType = true;
				for (int i = 0; i < unit.typeCount; i++) {
					if((unit.types[i].typeDeclaration.modifiers & AccPublic) != 0) {
						hasNoPublicType = false;
					}
				}
				if(hasNoPublicType) {
					keywords[count++] = Keywords.PUBLIC;
				}
			}
			if((lastModifiers & AccAbstract) == 0
				&& (lastModifiers & AccFinal) == 0
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.ABSTARCT)) {
				keywords[count++] = Keywords.ABSTARCT;
			}
			if((lastModifiers & AccAbstract) == 0
				&& (lastModifiers & AccFinal) == 0
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.FINAL)) {
				keywords[count++] = Keywords.FINAL;
			}
			if(CharOperation.prefixEquals(identifierStack[index], Keywords.CLASS)) {
				keywords[count++] = Keywords.CLASS;
			}
			if((lastModifiers & AccFinal) == 0
				&& CharOperation.prefixEquals(identifierStack[index], Keywords.INTERFACE)) {
				keywords[count++] = Keywords.INTERFACE;
			}
			if(count != 0) {
				System.arraycopy(keywords, 0, keywords = new char[count][], 0, count);
				
				this.assistNode = new CompletionOnKeyword2(ident, pos, keywords);
				this.lastCheckPoint = assistNode.sourceEnd + 1;
				this.isOrphanCompletionNode = true;
				return true;
			}
		}
	}
	return false;
}
private boolean checkInstanceofKeyword() {
	if(isInsideMethod()) {
		int kind = topKnownElementKind(COMPLETION_OR_ASSIST_PARSER);
		int index;
		if(kind != K_BLOCK_DELIMITER
			&& (index = indexOfAssistIdentifier()) > -1
			&& expressionPtr > -1
			&& expressionLengthStack[expressionPtr] == 1
			&& CharOperation.prefixEquals(identifierStack[index], Keywords.INSTANCEOF)) {
			this.assistNode = new CompletionOnKeyword3(
					identifierStack[index],
					identifierPositionStack[index],
					Keywords.INSTANCEOF);
			this.lastCheckPoint = assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
/**
 * Checks if the completion is inside a method invocation or a constructor invocation.
 * Returns whether we found a completion node.
 */
private boolean checkInvocation() {
	Expression topExpression = this.expressionPtr >= 0 ? 
		this.expressionStack[this.expressionPtr] :
		null;
	boolean isEmptyNameCompletion = false;
	boolean isEmptyAssistIdentifier = false;
	if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SELECTOR_QUALIFIER
		&& ((isEmptyNameCompletion = topExpression == this.assistNode && this.isEmptyNameCompletion()) // eg. it is something like "this.fred([cursor]" but it is not something like "this.fred(1 + [cursor]"
			|| (isEmptyAssistIdentifier = this.indexOfAssistIdentifier() >= 0 && this.identifierStack[this.identifierPtr].length == 0))) { // eg. it is something like "this.fred(1 [cursor]"
				
		// pop empty name completion
		if (isEmptyNameCompletion) {
			this.expressionPtr--;
			this.expressionLengthStack[this.expressionLengthPtr]--;
		} else if (isEmptyAssistIdentifier) {
			this.identifierPtr--;
			this.identifierLengthPtr--;
		}

		// find receiver and qualifier
		int invocationType = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER, 1);
		int qualifierExprPtr = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER);

		// find arguments
		int numArgs = this.expressionPtr - qualifierExprPtr;
		int argStart = qualifierExprPtr + 1;
		Expression[] arguments = null;
		if (numArgs > 0) {
			// remember the arguments
			arguments = new Expression[numArgs];
			System.arraycopy(this.expressionStack, argStart, arguments, 0, numArgs);

			// consume the expression arguments
			this.expressionPtr -= numArgs;
			int count = numArgs;
			while (count > 0) {
				count -= this.expressionLengthStack[this.expressionLengthPtr--];
			} 
		}

		// build ast node
		if (invocationType != ALLOCATION && invocationType != QUALIFIED_ALLOCATION) {
			// creates completion on message send	
			CompletionOnMessageSend messageSend = new CompletionOnMessageSend();
			messageSend.arguments = arguments;
			switch (invocationType) {
				case NO_RECEIVER:
					// implicit this
					messageSend.receiver = ThisReference.implicitThis();
					break;
				case NAME_RECEIVER:
					// remove special flags for primitive types
					while (this.identifierLengthPtr >= 0 && this.identifierLengthStack[this.identifierLengthPtr] < 0) {
						this.identifierLengthPtr--;
					}
				
					// remove selector 
					this.identifierPtr--; 
					this.identifierLengthStack[this.identifierLengthPtr]--;
					// consume the receiver
					messageSend.receiver = this.getUnspecifiedReference();
					break;
				case SUPER_RECEIVER:
					messageSend.receiver = new SuperReference(0, 0);
					break;
				case EXPLICIT_RECEIVER:
					messageSend.receiver = this.expressionStack[qualifierExprPtr];
			}

			// set selector
			int selectorPtr = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER, 2);
			messageSend.selector = this.identifierStack[selectorPtr];
			// remove selector
			if (this.identifierLengthPtr >=0 && this.identifierLengthStack[this.identifierLengthPtr] == 1) {
				this.identifierPtr--; 
				this.identifierLengthPtr--;
			}
		
			// the entire message may be replaced in case qualification is needed
			messageSend.sourceStart = (int)(this.identifierPositionStack[selectorPtr] >> 32); //this.cursorLocation + 1;
			messageSend.sourceEnd = this.cursorLocation;

			// remember the message send as an orphan completion node
			this.assistNode = messageSend;
			this.lastCheckPoint = messageSend.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		} else {
			int selectorPtr = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER, 2);
			if (selectorPtr == THIS_CONSTRUCTOR || selectorPtr == SUPER_CONSTRUCTOR) {
				// creates an explicit constructor call
				CompletionOnExplicitConstructorCall call = new CompletionOnExplicitConstructorCall(
					(selectorPtr == THIS_CONSTRUCTOR) ? ExplicitConstructorCall.This : ExplicitConstructorCall.Super);
				call.arguments = arguments;
				if (invocationType == QUALIFIED_ALLOCATION) {
					call.qualification = this.expressionStack[qualifierExprPtr];
				}
		
				// no source is going to be replaced
				call.sourceStart = this.cursorLocation + 1;
				call.sourceEnd = this.cursorLocation;

				// remember the explicit constructor call as an orphan completion node
				this.assistNode = call;
				this.lastCheckPoint = call.sourceEnd + 1;
				this.isOrphanCompletionNode = true;
				return true;
			} else {
				// creates an allocation expression 
				CompletionOnQualifiedAllocationExpression allocExpr = new CompletionOnQualifiedAllocationExpression();
				allocExpr.arguments = arguments;
				allocExpr.type = super.getTypeReference(0); // we don't want a completion node here, so call super
				if (invocationType == QUALIFIED_ALLOCATION) {
					allocExpr.enclosingInstance = this.expressionStack[qualifierExprPtr];
				}
				// no source is going to be replaced
				allocExpr.sourceStart = this.cursorLocation + 1;
				allocExpr.sourceEnd = this.cursorLocation;
				
				// remember the allocation expression as an orphan completion node
				this.assistNode = allocExpr;
				this.lastCheckPoint = allocExpr.sourceEnd + 1;
				this.isOrphanCompletionNode = true;
				return true;
			}
		}
	}
	return false;
}
/**
 * Checks if the completion is on a member access (ie. in an identifier following a dot).
 * Returns whether we found a completion node.
 */
private boolean checkMemberAccess() {
	if (this.previousToken == TokenNameDOT && this.qualifier > -1 && this.expressionPtr == this.qualifier) {
		// the receiver is an expression
		pushCompletionOnMemberAccessOnExpressionStack(false);
		return true;
	}
	return false;
}
/**
 * Checks if the completion is on a name reference.
 * Returns whether we found a completion node.
 */
private boolean checkNameCompletion() {
	/* 
		We didn't find any other completion, but the completion identifier is on the identifier stack,
		so it can only be a completion on name.
		Note that we allow the completion on a name even if nothing is expected (eg. foo() b[cursor] would
		be a completion on 'b'). This policy gives more to the user than he/she would expect, but this 
		simplifies the problem. To fix this, the recovery must be changed to work at a 'statement' granularity
		instead of at the 'expression' granularity as it does right now.
	*/ 
	
	// NB: at this point the completion identifier is on the identifier stack
	this.assistNode = getUnspecifiedReferenceOptimized();
	this.lastCheckPoint = this.assistNode.sourceEnd + 1;
	this.isOrphanCompletionNode = true;
	return true;
}
/**
 * Checks if the completion is in the context of a method and on the type of one of its arguments
 * Returns whether we found a completion node.
 */
private boolean checkRecoveredMethod() {
	if (currentElement instanceof RecoveredMethod){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		/* check if on line with an error already - to avoid completing inside 
			illegal type names e.g.  int[<cursor> */
		if (lastErrorEndPosition <= cursorLocation+1
			&& scanner.getLineNumber(lastErrorEndPosition) 
				== scanner.getLineNumber(((CompletionScanner)scanner).completedIdentifierStart)){
			return false;
		}		
 		RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
		/* only consider if inside method header */
		if (!recoveredMethod.foundOpeningBrace
			&& lastIgnoredToken == -1) {
			//if (rParenPos < lParenPos){ // inside arguments
			this.assistNode = this.getTypeReference(0);
			this.lastCheckPoint = this.assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
/**
 * Checks if the completion is in the context of a type and on a type reference in this type.
 * Persists the identifier into a fake field return type
 * Returns whether we found a completion node.
 */
private boolean checkRecoveredType() {
	if (currentElement instanceof RecoveredType){
		/* check if current awaiting identifier is the completion identifier */
		if (this.indexOfAssistIdentifier() < 0) return false;

		/* check if on line with an error already - to avoid completing inside 
			illegal type names e.g.  int[<cursor> */
		if ((lastErrorEndPosition <= cursorLocation+1)
			&& scanner.getLineNumber(lastErrorEndPosition) 
				== scanner.getLineNumber(((CompletionScanner)scanner).completedIdentifierStart)){
			return false;
		}
		RecoveredType recoveredType = (RecoveredType)currentElement;
		/* filter out cases where scanner is still inside type header */
		if (recoveredType.foundOpeningBrace) {
			this.assistNode = this.getTypeReference(0);
			this.lastCheckPoint = this.assistNode.sourceEnd + 1;
			this.isOrphanCompletionNode = true;
			return true;
		}
	}
	return false;
}
/* 
 * Check whether about to shift beyond the completion token.
 * If so, depending on the context, a special node might need to be created
 * and attached to the existing recovered structure so as to be remember in the
 * resulting parsed structure.
 */
public void completionIdentifierCheck(){
	//if (assistNode != null) return; 

	if (checkKeyword()) return;
	if (checkRecoveredType()) return;
	if (checkRecoveredMethod()) return;

	// if not in a method in non diet mode and if not inside a field initializer, only record references attached to types
	if (!(isInsideMethod() && !this.diet)
		&& !isIndirectlyInsideFieldInitialization()) return; 

	/*
	 	In some cases, the completion identifier may not have yet been consumed,
	 	e.g.  int.[cursor]
	 	This is because the grammar does not allow any (empty) identifier to follow
	 	a base type. We thus have to manually force the identifier to be consumed
	 	(i.e. pushed).
	 */
	if (assistIdentifier() == null && this.currentToken == TokenNameIdentifier) { // Test below copied from CompletionScanner.getCurrentIdentifierSource()
		if (cursorLocation < this.scanner.startPosition && this.scanner.currentPosition == this.scanner.startPosition){ // fake empty identifier got issued
			this.pushIdentifier();					
		} else if (cursorLocation+1 >= this.scanner.startPosition && cursorLocation < this.scanner.currentPosition){
			this.pushIdentifier();
		}
	}

	// check for different scenarii
	try {
		// no need to go further if we found a non empty completion node
		// (we still need to store labels though)
		if (this.assistNode != null) {
			// however inside an invocation, the completion identifier may already have been consumed into an empty name 
			// completion, so this check should be before we check that we are at the cursor location
			if (!isEmptyNameCompletion() || checkInvocation()) return;
		}

		// no need to check further if we are not at the cursor location
		if (this.indexOfAssistIdentifier() < 0) return;

		if (checkClassInstanceCreation()) return;
		if (checkCatchClause()) return;
		if (checkMemberAccess()) return;
		if (checkClassLiteralAccess()) return;
		if (checkInstanceofKeyword()) return;
		
		// if the completion was not on an empty name, it can still be inside an invocation (eg. this.fred("abc"[cursor])
		// (NB: Put this check before checkNameCompletion() because the selector of the invocation can be on the identifier stack)
		if (checkInvocation()) return;

		if (checkNameCompletion()) return;
	} finally {
	}
}
protected void consumeArrayCreationExpressionWithInitializer() {
	super.consumeArrayCreationExpressionWithInitializer();
	popElement(K_ARRAY_CREATION);
}
protected void consumeArrayCreationExpressionWithoutInitializer() {
	super.consumeArrayCreationExpressionWithoutInitializer();
	popElement(K_ARRAY_CREATION);
}
protected void consumeAssignment() {
	popElement(K_ASSISGNMENT_OPERATOR);
	super.consumeAssignment();
}
protected void consumeAssignmentOperator(int pos) {
	super.consumeAssignmentOperator(pos);
	pushOnElementStack(K_ASSISGNMENT_OPERATOR, pos);
}
protected void consumeBinaryExpression(int op) {
	super.consumeBinaryExpression(op);
	popElement(K_BINARY_OPERATOR);
	
	if(expressionStack[expressionPtr] instanceof BinaryExpression) {
		BinaryExpression exp = (BinaryExpression) expressionStack[expressionPtr];
		if(assistNode != null && exp.right == assistNode) {
			assistNodeParent = exp;
		}
	}
}
protected void consumeCaseLabel() {
	super.consumeCaseLabel();
	if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) != K_SWITCH_LABEL) {
		pushOnElementStack(K_SWITCH_LABEL);
	}
}
protected void consumeCastExpression() {
	popElement(K_CAST_STATEMENT);
	
	Expression exp, cast, castType;
	expressionPtr--;
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr+1], castType = expressionStack[expressionPtr]);
	cast.sourceStart = castType.sourceStart - 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionLL1() {
	popElement(K_CAST_STATEMENT);
	super.consumeCastExpressionLL1();
}
protected void consumeClassBodyDeclaration() {
	popElement(K_BLOCK_DELIMITER);
	super.consumeClassBodyDeclaration();
}
protected void consumeClassBodyopt() {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeClassBodyopt();
}
protected void consumeClassHeaderName() {
	super.consumeClassHeaderName();

	if (currentElement != null
		&& currentToken == TokenNameIdentifier
		&& this.cursorLocation+1 >= scanner.startPosition
		&& this.cursorLocation < scanner.currentPosition){
		this.pushIdentifier();

		int index = -1;
		/* check if current awaiting identifier is the completion identifier */
		if ((index = this.indexOfAssistIdentifier()) > -1) {

			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (!recoveredType.foundOpeningBrace) {
				char[][] keywords = new char[Keywords.COUNT][];
				int count = 0;
				
				TypeDeclaration type = recoveredType.typeDeclaration;
				if(type.superInterfaces == null) {
					if(type.superclass == null) {;
						keywords[count++] = Keywords.EXTENDS;
					}
					keywords[count++] = Keywords.IMPLEMENTS;
				}
				
				System.arraycopy(keywords, 0, keywords = new char[count][], 0, count);
				
				if(count > 0) {
					type.superclass = new CompletionOnKeyword1(
						identifierStack[index],
						identifierPositionStack[index],
						keywords);
					this.assistNode = type.superclass;
					this.lastCheckPoint = type.superclass.sourceEnd + 1;
				}
			}
		}
	}
}
protected void consumeClassHeaderExtends() {
	pushOnElementStack(K_NEXT_TYPEREF_IS_CLASS);
	super.consumeClassHeaderExtends();
	popElement(K_NEXT_TYPEREF_IS_CLASS);
	
	if (currentElement != null
		&& currentToken == TokenNameIdentifier
		&& this.cursorLocation+1 >= scanner.startPosition
		&& this.cursorLocation < scanner.currentPosition){
		this.pushIdentifier();
		
		int index = -1;
		/* check if current awaiting identifier is the completion identifier */
		if ((index = this.indexOfAssistIdentifier()) > -1) {

			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (!recoveredType.foundOpeningBrace) {
				TypeDeclaration type = recoveredType.typeDeclaration;
				if(type.superInterfaces == null) {
					type.superclass = new CompletionOnKeyword1(
						identifierStack[index],
						identifierPositionStack[index],
						Keywords.IMPLEMENTS);
					this.assistNode = type.superclass;
					this.lastCheckPoint = type.superclass.sourceEnd + 1;
				}
			}
		}
	}
}
protected void consumeClassTypeElt() {
	pushOnElementStack(K_NEXT_TYPEREF_IS_EXCEPTION);
	super.consumeClassTypeElt();
	popElement(K_NEXT_TYPEREF_IS_EXCEPTION);
}
protected void consumeConditionalExpression(int op) {
	popElement(K_CONDITIONAL_OPERATOR);
	super.consumeConditionalExpression(op);
}
protected void consumeConstructorBody() {
	popElement(K_BLOCK_DELIMITER);
	super.consumeConstructorBody();
}
protected void consumeConstructorHeader() {
	super.consumeConstructorHeader();
	pushOnElementStack(K_BLOCK_DELIMITER);
}
protected void consumeConstructorHeaderName() {

	/* no need to take action if not inside assist identifiers */
	if (indexOfAssistIdentifier() < 0) {
		super.consumeConstructorHeaderName();
		return;
	}
		
	/* force to start recovering in order to get fake field behavior */
	if (currentElement == null){
		this.hasReportedError = true; // do not report any error
	}
	this.restartRecovery = true;
}
protected void consumeDefaultLabel() {
	super.consumeDefaultLabel();
	if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SWITCH_LABEL) {
		popElement(K_SWITCH_LABEL);
	}
	pushOnElementStack(K_SWITCH_LABEL, DEFAULT);
}
protected void consumeEnterAnonymousClassBody() {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeEnterAnonymousClassBody();
}
protected void consumeEnterVariable() {
	identifierPtr--;
	identifierLengthPtr--;

	boolean isLocalDeclaration = nestedMethod[nestedType] != 0;
	int variableIndex = variablesCounter[nestedType];
	int extendedDimension = intStack[intPtr + 1];
	
	if(isLocalDeclaration || indexOfAssistIdentifier() < 0 || variableIndex != 0 || extendedDimension != 0) {
		identifierPtr++;
		identifierLengthPtr++;
		super.consumeEnterVariable();
	} else {
		restartRecovery = true;
		
//		private boolean checkKeyword() {
//			if (currentElement instanceof RecoveredUnit) {
//				RecoveredUnit unit = (RecoveredUnit) currentElement;
//				int index = -1;
//				if ((index = this.indexOfAssistIdentifier()) > -1) {
//					if(unit.typeCount == 0
//						&& CharOperation.prefixEquals(identifierStack[index], Keywords.IMPORT)) {
//						CompletionOnKeyword2 completionOnImportKeyword = new CompletionOnKeyword2(Keywords.IMPORT, identifierPositionStack[index]);
//						this.assistNode = completionOnImportKeyword;
//						this.lastCheckPoint = completionOnImportKeyword.sourceEnd + 1;
//						this.isOrphanCompletionNode = true;
//						return true;
//					} else if(unit.typeCount == 0
//						&& unit.importCount == 0
//						&& CharOperation.prefixEquals(identifierStack[index], Keywords.PACKAGE)) {
//						CompletionOnKeyword2 completionOnImportKeyword = new CompletionOnKeyword2(Keywords.PACKAGE, identifierPositionStack[index]);
//						this.assistNode = completionOnImportKeyword;
//						this.lastCheckPoint = completionOnImportKeyword.sourceEnd + 1;
//						this.isOrphanCompletionNode = true;
//						return true;
//					}
//				}
//			}
//			return false;
//		}
		
		// recovery
		if (currentElement != null) {
			if(!checkKeyword() && !(currentElement instanceof RecoveredUnit && ((RecoveredUnit)currentElement).typeCount == 0)) {
				int nameSourceStart = (int)(identifierPositionStack[identifierPtr] >>> 32);
				intPtr--;
				
				TypeReference type = getTypeReference(intStack[intPtr--]);
				intPtr--;
				
				if (!(currentElement instanceof RecoveredType)
					&& (currentToken == TokenNameDOT
						|| (scanner.getLineNumber(type.sourceStart)
								!= scanner.getLineNumber(nameSourceStart)))){
					lastCheckPoint = nameSourceStart;
					restartRecovery = true;
					return;
				}
				
				FieldDeclaration completionFieldDecl = new CompletionOnFieldType(type, false);
				completionFieldDecl.modifiers = intStack[intPtr--];
				assistNode = completionFieldDecl;
				lastCheckPoint = type.sourceEnd + 1;
				currentElement = currentElement.add(completionFieldDecl, 0);
				lastIgnoredToken = -1;
			}
		}
	}
}
protected void consumeEqualityExpression(int op) {
	super.consumeEqualityExpression(op);
	popElement(K_BINARY_OPERATOR);
	
	BinaryExpression exp = (BinaryExpression) expressionStack[expressionPtr];
	if(assistNode != null && exp.right == assistNode) {
		assistNodeParent = exp;
	}
}
protected void consumeExitVariableWithInitialization() {
	super.consumeExitVariableWithInitialization();
	
	// does not keep the initialization if completion is not inside
	AbstractVariableDeclaration variable = (AbstractVariableDeclaration) astStack[astPtr];
	if (cursorLocation + 1 < variable.initialization.sourceStart ||
		cursorLocation > variable.initialization.sourceEnd) {
		variable.initialization = null;
	} else if (assistNode != null && assistNode == variable.initialization) {
		assistNodeParent = variable;
	}
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeExplicitConstructorInvocation(flag, recFlag);	
}
/*
 * Copy of code from superclass with the following change:
 * If the cursor location is on the field access, then create a 
 * CompletionOnMemberAccess instead.
 */
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	// potential receiver is being poped, so reset potential receiver
	this.invocationType = NO_RECEIVER;

	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFieldAccess(isSuperAccess);
	} else {
		this.pushCompletionOnMemberAccessOnExpressionStack(isSuperAccess);
	}
}
protected void consumeForceNoDiet() {
	super.consumeForceNoDiet();
	if (isInsideMethod()) {
		pushOnElementStack(K_LOCAL_INITIALIZER_DELIMITER);
	}
}
protected void consumeFormalParameter() {
	if (this.indexOfAssistIdentifier() < 0) {
		super.consumeFormalParameter();
	} else {

		identifierLengthPtr--;
		char[] name = identifierStack[identifierPtr];
		long namePositions = identifierPositionStack[identifierPtr--];
		TypeReference type = getTypeReference(intStack[intPtr--] + intStack[intPtr--]);
		intPtr -= 2;
		CompletionOnArgumentName arg = 
			new CompletionOnArgumentName(
				name, 
				namePositions, 
				type, 
				intStack[intPtr + 1] & ~AccDeprecated); // modifiers
				
		arg.isCatchArgument = topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_CATCH_AND_RIGHT_PAREN;
		pushOnAstStack(arg);
		
		assistNode = arg;
		this.lastCheckPoint = (int) namePositions;
		isOrphanCompletionNode = true;

		/* if incomplete method header, listLength counter will not have been reset,
			indicating that some arguments are available on the stack */
		listLength++;
	} 	
}
protected void consumeInsideCastExpression() {
	int end = intStack[intPtr--];
	Expression castType = getTypeReference(intStack[intPtr--]);
	castType.sourceEnd = end - 1;
	castType.sourceStart = intStack[intPtr--] + 1;
	pushOnExpressionStack(castType);
	
	pushOnElementStack(K_CAST_STATEMENT);
}
protected void consumeInsideCastExpressionLL1() {
	super.consumeInsideCastExpressionLL1();
	pushOnElementStack(K_CAST_STATEMENT);
}
protected void consumeInstanceOfExpression(int op) {
	super.consumeInstanceOfExpression(op);
	popElement(K_BINARY_OPERATOR);
	
	InstanceOfExpression exp = (InstanceOfExpression) expressionStack[expressionPtr];
	if(assistNode != null && exp.type == assistNode) {
		assistNodeParent = exp;
	}
}
protected void consumeInterfaceHeaderName() {
	super.consumeInterfaceHeaderName();
	
	if (currentElement != null
		&& currentToken == TokenNameIdentifier
		&& this.cursorLocation+1 >= scanner.startPosition
		&& this.cursorLocation < scanner.currentPosition){
		this.pushIdentifier();
		
		int index = -1;
		/* check if current awaiting identifier is the completion identifier */
		if ((index = this.indexOfAssistIdentifier()) > -1) {

			RecoveredType recoveredType = (RecoveredType)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (!recoveredType.foundOpeningBrace) {
				TypeDeclaration type = recoveredType.typeDeclaration;
				if(type.superInterfaces == null) {
					CompletionOnKeyword1 completionOnKeyword = new CompletionOnKeyword1(
						identifierStack[index],
						identifierPositionStack[index],
						Keywords.EXTENDS);
					type.superInterfaces = new TypeReference[]{completionOnKeyword};
					this.assistNode = completionOnKeyword;
					this.lastCheckPoint = completionOnKeyword.sourceEnd + 1;
				}
			}
		}
	}
}
protected void consumeInterfaceType() {
	pushOnElementStack(K_NEXT_TYPEREF_IS_INTERFACE);
	super.consumeInterfaceType();
	popElement(K_NEXT_TYPEREF_IS_INTERFACE);
}
protected void consumeMethodInvocationName() {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeMethodInvocationName();
}
protected void consumeMethodInvocationPrimary() {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeMethodInvocationPrimary();
}
protected void consumeMethodInvocationSuper() {
	popElement(K_SELECTOR_QUALIFIER);
	popElement(K_SELECTOR_INVOCATION_TYPE);
	super.consumeMethodInvocationSuper();
}
protected void consumeMethodHeaderName() {
	if(this.indexOfAssistIdentifier() < 0) {
		identifierPtr--;
		identifierLengthPtr--;
		if(this.indexOfAssistIdentifier() != 0) {
			identifierPtr++;
			identifierLengthPtr++;
			super.consumeMethodHeaderName();
		} else {
			restartRecovery = true;
			
			// recovery
			if (currentElement != null) {
				//name
				char[] selector = identifierStack[identifierPtr + 1];
				long selectorSource = identifierPositionStack[identifierPtr + 1];
				
				//type
				TypeReference type = getTypeReference(intStack[intPtr--]);
				((CompletionOnSingleTypeReference)type).isCompletionNode = false;
				//modifiers
				int declarationSourceStart = intStack[intPtr--];
				int modifiers = intStack[intPtr--];
				
				if(scanner.getLineNumber(type.sourceStart) != scanner.getLineNumber((int) (selectorSource >>> 32))) {
					FieldDeclaration completionFieldDecl = new CompletionOnFieldType(type, false);
					completionFieldDecl.modifiers = modifiers;
					assistNode = completionFieldDecl;
					lastCheckPoint = type.sourceEnd + 1;
					currentElement = currentElement.add(completionFieldDecl, 0);
					lastIgnoredToken = -1;
				} else {
					CompletionOnMethodReturnType md = new CompletionOnMethodReturnType(type, this.compilationUnit.compilationResult);
					md.selector = selector;
					md.declarationSourceStart = declarationSourceStart;
					md.modifiers = modifiers;
					md.bodyStart = lParenPos+1;
					listLength = 0; // initialize listLength before reading parameters/throws
					assistNode = md;
					this.lastCheckPoint = md.bodyStart;
					currentElement = currentElement.add(md, 0);
					lastIgnoredToken = -1;
				}
			}
		}
	} else {
		// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
		CompletionOnMethodName md = new CompletionOnMethodName(this.compilationUnit.compilationResult);
	
		//name
		md.selector = identifierStack[identifierPtr];
		long selectorSource = identifierPositionStack[identifierPtr--];
		//type
		md.returnType = getTypeReference(intStack[intPtr--]);
		//modifiers
		md.declarationSourceStart = intStack[intPtr--];
		md.modifiers = intStack[intPtr--];
	
		//highlight starts at selector start
		md.sourceStart = (int) (selectorSource >>> 32);
		md.selectorEnd = (int) selectorSource;
		pushOnAstStack(md);
		md.sourceEnd = lParenPos;
		md.bodyStart = lParenPos+1;
		listLength = 0; // initialize listLength before reading parameters/throws
		
		this.assistNode = md;	
		this.lastCheckPoint = md.sourceEnd;
		// recovery
		if (currentElement != null){
			if (currentElement instanceof RecoveredType 
				//|| md.modifiers != 0
				|| (scanner.getLineNumber(md.returnType.sourceStart)
						== scanner.getLineNumber(md.sourceStart))){
				lastCheckPoint = md.bodyStart;
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			} else {
				lastCheckPoint = md.sourceStart;
				restartRecovery = true;
			}
		}
	}
}
protected void consumeMethodHeaderParameters() {
	super.consumeMethodHeaderParameters();
	
	if (currentElement != null
		&& currentToken == TokenNameIdentifier
		&& this.cursorLocation+1 >= scanner.startPosition
		&& this.cursorLocation < scanner.currentPosition){
		this.pushIdentifier();
		
		int index = -1;
		/* check if current awaiting identifier is the completion identifier */
		if ((index = this.indexOfAssistIdentifier()) > -1) {

			RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (!recoveredMethod.foundOpeningBrace) {
				AbstractMethodDeclaration method = recoveredMethod.methodDeclaration;
				if(method.thrownExceptions == null
					&& CharOperation.prefixEquals(identifierStack[index], Keywords.THROWS)) {
					CompletionOnKeyword1 completionOnKeyword = new CompletionOnKeyword1(
						identifierStack[index],
						identifierPositionStack[index],
						Keywords.THROWS);
					method.thrownExceptions = new TypeReference[]{completionOnKeyword};
					recoveredMethod.foundOpeningBrace = true;
					this.assistNode = completionOnKeyword;
					this.lastCheckPoint = completionOnKeyword.sourceEnd + 1;
				}
			}
		}
	}
}
protected void consumeMethodHeaderExtendedDims() {
	super.consumeMethodHeaderExtendedDims();
	
	if (currentElement != null
		&& currentToken == TokenNameIdentifier
		&& this.cursorLocation+1 >= scanner.startPosition
		&& this.cursorLocation < scanner.currentPosition){
		this.pushIdentifier();
		
		int index = -1;
		/* check if current awaiting identifier is the completion identifier */
		if ((index = this.indexOfAssistIdentifier()) > -1) {

			RecoveredMethod recoveredMethod = (RecoveredMethod)currentElement;
			/* filter out cases where scanner is still inside type header */
			if (!recoveredMethod.foundOpeningBrace) {
				AbstractMethodDeclaration method = recoveredMethod.methodDeclaration;
				if(method.thrownExceptions == null) {
					CompletionOnKeyword1 completionOnKeyword = new CompletionOnKeyword1(
						identifierStack[index],
						identifierPositionStack[index],
						Keywords.THROWS);
					method.thrownExceptions = new TypeReference[]{completionOnKeyword};
					recoveredMethod.foundOpeningBrace = true;
					this.assistNode = completionOnKeyword;
					this.lastCheckPoint = completionOnKeyword.sourceEnd + 1;
				}
			}
		}
	}
}

protected void consumeMethodBody() {
	popElement(K_BLOCK_DELIMITER);
	super.consumeMethodBody();
}
protected void consumeMethodHeader() {
	super.consumeMethodHeader();
	pushOnElementStack(K_BLOCK_DELIMITER);
}
protected void consumeModifiers() {
	super.consumeModifiers();
	// save from stack values
	this.lastModifiersStart = intStack[intPtr];
	this.lastModifiers = 	intStack[intPtr-1];
}
protected void consumeRestoreDiet() {
	super.consumeRestoreDiet();
	if (isInsideMethod()) {
		popElement(K_LOCAL_INITIALIZER_DELIMITER);
	}
}
protected void consumeStatementSwitch() {
	super.consumeStatementSwitch();
	if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SWITCH_LABEL) {
		popElement(K_SWITCH_LABEL);
	}
}
protected void consumeNestedMethod() {
	super.consumeNestedMethod();
	pushOnElementStack(K_BLOCK_DELIMITER);
}
protected void consumePushPosition() {
	super.consumePushPosition();
	if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BINARY_OPERATOR) {
		int info = topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER);
		popElement(K_BINARY_OPERATOR);
		pushOnElementStack(K_UNARY_OPERATOR, info);
	}
}
protected void consumeToken(int token) {
	if(isFirst) {
		super.consumeToken(token);
		return;
	}
	if(canBeExplicitConstructor == NEXTTOKEN) {
		canBeExplicitConstructor = YES;
	} else {
		canBeExplicitConstructor = NO;
	}
	
	int previous = this.previousToken;
	int previousIdentifierPtr = this.previousIdentifierPtr;
	
	if (isInsideMethod() || isInsideFieldInitialization()) {
		switch(token) {
			case TokenNameLPAREN:
				popElement(K_BETWEEN_NEW_AND_LEFT_BRACKET);
				break;
			case TokenNameLBRACE:
				popElement(K_BETWEEN_NEW_AND_LEFT_BRACKET);
				break;
			case TokenNameLBRACKET:
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_NEW_AND_LEFT_BRACKET) {
					popElement(K_BETWEEN_NEW_AND_LEFT_BRACKET);
					pushOnElementStack(K_ARRAY_CREATION);
				}
				break;
			case TokenNameRBRACE:
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BLOCK_DELIMITER) {
					popElement(K_BLOCK_DELIMITER);
				} else {
					popElement(K_ARRAY_INITIALIZER);	
				}
				break;
			case TokenNameRBRACKET:
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_LEFT_AND_RIGHT_BRACKET) {
					popElement(K_BETWEEN_LEFT_AND_RIGHT_BRACKET);
				}
				break;
			
		}
	}
	super.consumeToken(token);

	// if in field initializer (directly or not), on the completion identifier and not in recovery mode yet
	// then position end of file at cursor location (so that we have the same behavior as
	// in method bodies)
	if (token == TokenNameIdentifier
			&& this.identifierStack[this.identifierPtr] == assistIdentifier()
			&& this.currentElement == null
			&& this.isIndirectlyInsideFieldInitialization()) {
		this.scanner.eofPosition = cursorLocation < Integer.MAX_VALUE ? cursorLocation+1 : cursorLocation;
	}
	
	// if in a method or if in a field initializer 
	if (isInsideMethod() || isInsideFieldInitialization()) {
		switch (token) {
			case TokenNameDOT:
				switch (previous) {
					case TokenNamethis: // eg. this[.]fred()
						this.invocationType = EXPLICIT_RECEIVER;
						break;
					case TokenNamesuper: // eg. super[.]fred()
						this.invocationType = SUPER_RECEIVER;
						break;
					case TokenNameIdentifier: // eg. bar[.]fred()
						if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) != K_BETWEEN_NEW_AND_LEFT_BRACKET) {
							if (this.identifierPtr != previousIdentifierPtr) { // if identifier has been consumed, eg. this.x[.]fred()
								this.invocationType = EXPLICIT_RECEIVER;
							} else {
								this.invocationType = NAME_RECEIVER;
							}
						}
						break;
				}
				break;
			case TokenNameIdentifier:
				if (previous == TokenNameDOT) { // eg. foo().[fred]()
					if (this.invocationType != SUPER_RECEIVER // eg. not super.[fred]()
						&& this.invocationType != NAME_RECEIVER // eg. not bar.[fred]()
						&& this.invocationType != ALLOCATION // eg. not new foo.[Bar]()
						&& this.invocationType != QUALIFIED_ALLOCATION) { // eg. not fred().new foo.[Bar]()

						this.invocationType = EXPLICIT_RECEIVER;
						this.qualifier = this.expressionPtr;
					}
				}
				break;	
			case TokenNamenew:
				pushOnElementStack(K_BETWEEN_NEW_AND_LEFT_BRACKET);
				this.qualifier = this.expressionPtr; // NB: even if there is no qualification, set it to the expression ptr so that the number of arguments are correctly computed
				if (previous == TokenNameDOT) { // eg. fred().[new] X()
					this.invocationType = QUALIFIED_ALLOCATION;
				} else { // eg. [new] X()
					this.invocationType = ALLOCATION;
				}
				break;
			case TokenNamethis:
				if (previous == TokenNameDOT) { // eg. fred().[this]()
					this.invocationType = QUALIFIED_ALLOCATION;
					this.qualifier = this.expressionPtr;
				}
				break;
			case TokenNamesuper:
				if (previous == TokenNameDOT) { // eg. fred().[super]()
					this.invocationType = QUALIFIED_ALLOCATION;
					this.qualifier = this.expressionPtr;
				}
				break;
			case TokenNamecatch:
				pushOnElementStack(K_BETWEEN_CATCH_AND_RIGHT_PAREN);
				break;
			case TokenNameLPAREN:
				if (this.invocationType == NO_RECEIVER || this.invocationType == NAME_RECEIVER) {
					this.qualifier = this.expressionPtr; // remenber the last expression so that arguments are correctly computed
				}
				switch (previous) {
					case TokenNameIdentifier: // eg. fred[(]) or foo.fred[(])
						if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SELECTOR) {
							this.pushOnElementStack(K_SELECTOR_INVOCATION_TYPE, this.invocationType);
							this.pushOnElementStack(K_SELECTOR_QUALIFIER, this.qualifier);
						}
						this.invocationType = NO_RECEIVER;
						break;
					case TokenNamethis: // explicit constructor invocation, eg. this[(]1, 2)
						if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SELECTOR) {
							this.pushOnElementStack(K_SELECTOR_INVOCATION_TYPE, (this.invocationType == QUALIFIED_ALLOCATION) ? QUALIFIED_ALLOCATION : ALLOCATION);
							this.pushOnElementStack(K_SELECTOR_QUALIFIER, this.qualifier);
						}
						this.invocationType = NO_RECEIVER;
						break;
					case TokenNamesuper: // explicit constructor invocation, eg. super[(]1, 2)
						if (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SELECTOR) {
							this.pushOnElementStack(K_SELECTOR_INVOCATION_TYPE, (this.invocationType == QUALIFIED_ALLOCATION) ? QUALIFIED_ALLOCATION : ALLOCATION);
							this.pushOnElementStack(K_SELECTOR_QUALIFIER, this.qualifier);
						}
						this.invocationType = NO_RECEIVER;
						break;
				}
				break;
			case TokenNameLBRACE:
				this.bracketDepth++;
				int kind;
				if((kind = topKnownElementKind(COMPLETION_OR_ASSIST_PARSER)) == K_FIELD_INITIALIZER_DELIMITER
					|| kind == K_LOCAL_INITIALIZER_DELIMITER
					|| kind == K_ARRAY_CREATION) {
					pushOnElementStack(K_ARRAY_INITIALIZER, endPosition);
				} else {
					switch(previous) {
						case TokenNameRPAREN :
							switch(previousKind) {
								case K_BETWEEN_IF_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, IF);
									break;
								case K_BETWEEN_CATCH_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, CATCH);
									break;
								case K_BETWEEN_WHILE_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, WHILE);
									break;
								case K_BETWEEN_SWITCH_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, SWITCH);
									break;
								case K_BETWEEN_FOR_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, FOR);
									break;
								case K_BETWEEN_SYNCHRONIZED_AND_RIGHT_PAREN :
									pushOnElementStack(K_BLOCK_DELIMITER, SYNCHRONIZED);
									break;
								default :
									pushOnElementStack(K_BLOCK_DELIMITER);
									break;
							}
							break;
						case TokenNametry :
							pushOnElementStack(K_BLOCK_DELIMITER, TRY);
							break;
						case TokenNamedo:
							pushOnElementStack(K_BLOCK_DELIMITER, DO);
							break;
						default :
							pushOnElementStack(K_BLOCK_DELIMITER);
							break;
					}
				}
				break;
			case TokenNameLBRACKET:
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) != K_ARRAY_CREATION) {
					pushOnElementStack(K_BETWEEN_LEFT_AND_RIGHT_BRACKET);
				}
				this.bracketDepth++;
				break; 
			case TokenNameRBRACE:
				this.bracketDepth--;
				break;
			case TokenNameRBRACKET:
				this.bracketDepth--;
				break; 
			case TokenNameRPAREN:
				switch(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER)) {
					case K_BETWEEN_CATCH_AND_RIGHT_PAREN :
						popElement(K_BETWEEN_CATCH_AND_RIGHT_PAREN);
						break;
					case K_BETWEEN_IF_AND_RIGHT_PAREN :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == bracketDepth) {
							popElement(K_BETWEEN_IF_AND_RIGHT_PAREN);
						}
						break;
					case K_BETWEEN_WHILE_AND_RIGHT_PAREN :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == bracketDepth) {
							popElement(K_BETWEEN_WHILE_AND_RIGHT_PAREN);
						}
						break;
					case K_BETWEEN_FOR_AND_RIGHT_PAREN :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == bracketDepth) {
							popElement(K_BETWEEN_FOR_AND_RIGHT_PAREN);
						}
						break;
					case K_BETWEEN_SWITCH_AND_RIGHT_PAREN :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == bracketDepth) {
							popElement(K_BETWEEN_SWITCH_AND_RIGHT_PAREN);
						}
						break;
					case K_BETWEEN_SYNCHRONIZED_AND_RIGHT_PAREN :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == bracketDepth) {
							popElement(K_BETWEEN_SYNCHRONIZED_AND_RIGHT_PAREN);
						}
						break;
				}
				break;
			case TokenNamethrow:
				pushOnElementStack(K_INSIDE_THROW_STATEMENT, bracketDepth);
				break;
			case TokenNameSEMICOLON:
				switch(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER)) {
					case K_INSIDE_THROW_STATEMENT :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == this.bracketDepth) {
							popElement(K_INSIDE_THROW_STATEMENT);
						}
						break;
					case K_INSIDE_RETURN_STATEMENT :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == this.bracketDepth) {
							popElement(K_INSIDE_RETURN_STATEMENT);
						}
						break;
					case K_INSIDE_ASSERT_STATEMENT :
						if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == this.bracketDepth) {
							popElement(K_INSIDE_ASSERT_STATEMENT);
						}
						break;
				}
				break;
			case TokenNamereturn:
				pushOnElementStack(K_INSIDE_RETURN_STATEMENT, this.bracketDepth);
				break;
			case TokenNameMULTIPLY:
				pushOnElementStack(K_BINARY_OPERATOR, MULTIPLY);
				break;
			case TokenNameDIVIDE:
				pushOnElementStack(K_BINARY_OPERATOR, DIVIDE);
				break;
			case TokenNameREMAINDER:
				pushOnElementStack(K_BINARY_OPERATOR, REMAINDER);
				break;
			case TokenNamePLUS:
				pushOnElementStack(K_BINARY_OPERATOR, PLUS);
				break;
			case TokenNameMINUS:
				pushOnElementStack(K_BINARY_OPERATOR, MINUS);
				break;
			case TokenNameLEFT_SHIFT:
				pushOnElementStack(K_BINARY_OPERATOR, LEFT_SHIFT);
				break;
			case TokenNameRIGHT_SHIFT:
				pushOnElementStack(K_BINARY_OPERATOR, RIGHT_SHIFT);
				break;
			case TokenNameUNSIGNED_RIGHT_SHIFT:
				pushOnElementStack(K_BINARY_OPERATOR, UNSIGNED_RIGHT_SHIFT);
				break;
			case TokenNameLESS:
				pushOnElementStack(K_BINARY_OPERATOR, LESS);
				break;
			case TokenNameGREATER:
				pushOnElementStack(K_BINARY_OPERATOR, GREATER);
				break;
			case TokenNameLESS_EQUAL:
				pushOnElementStack(K_BINARY_OPERATOR, LESS_EQUAL);
				break;
			case TokenNameGREATER_EQUAL:
				pushOnElementStack(K_BINARY_OPERATOR, GREATER_EQUAL);
				break;
			case TokenNameAND:
				pushOnElementStack(K_BINARY_OPERATOR, AND);
				break;
			case TokenNameXOR:
				pushOnElementStack(K_BINARY_OPERATOR, XOR);
				break;
			case TokenNameOR:
				pushOnElementStack(K_BINARY_OPERATOR, OR);
				break;
			case TokenNameAND_AND:
				pushOnElementStack(K_BINARY_OPERATOR, AND_AND);
				break;
			case TokenNameOR_OR:
				pushOnElementStack(K_BINARY_OPERATOR, OR_OR);
				break;
			case TokenNamePLUS_PLUS:
				pushOnElementStack(K_UNARY_OPERATOR, PLUS_PLUS);
				break;
			case TokenNameMINUS_MINUS:
				pushOnElementStack(K_UNARY_OPERATOR, MINUS_MINUS);
				break;
			case TokenNameTWIDDLE:
				pushOnElementStack(K_UNARY_OPERATOR, TWIDDLE);
				break;
			case TokenNameNOT:
				pushOnElementStack(K_UNARY_OPERATOR, NOT);
				break;
			case TokenNameEQUAL_EQUAL:
				pushOnElementStack(K_BINARY_OPERATOR, EQUAL_EQUAL);
				break;
			case TokenNameNOT_EQUAL:
				pushOnElementStack(K_BINARY_OPERATOR, NOT_EQUAL);
				break;
			case TokenNameinstanceof:
				pushOnElementStack(K_BINARY_OPERATOR, INSTANCEOF);
				break;
			case TokenNameQUESTION:
				pushOnElementStack(K_CONDITIONAL_OPERATOR, QUESTION);
				break;
			case TokenNameCOLON:
				if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_CONDITIONAL_OPERATOR
					&& topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == QUESTION) {
					popElement(K_CONDITIONAL_OPERATOR);
					pushOnElementStack(K_CONDITIONAL_OPERATOR, COLON);
				} else {
					if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BETWEEN_CASE_AND_COLON) {
						popElement(K_BETWEEN_CASE_AND_COLON);
					} else {
						popElement(K_BETWEEN_DEFAULT_AND_COLON);
					}
				}
				break;
			case TokenNameif:
				pushOnElementStack(K_BETWEEN_IF_AND_RIGHT_PAREN, bracketDepth);
				break;
			case TokenNamewhile:
				pushOnElementStack(K_BETWEEN_WHILE_AND_RIGHT_PAREN, bracketDepth);
				break;
			case TokenNamefor:
				pushOnElementStack(K_BETWEEN_FOR_AND_RIGHT_PAREN, bracketDepth);
				break;
			case TokenNameswitch:
				pushOnElementStack(K_BETWEEN_SWITCH_AND_RIGHT_PAREN, bracketDepth);
				break;
			case TokenNamesynchronized:
				pushOnElementStack(K_BETWEEN_SYNCHRONIZED_AND_RIGHT_PAREN, bracketDepth);
				break;
			case TokenNameassert:
				pushOnElementStack(K_INSIDE_ASSERT_STATEMENT, this.bracketDepth);
				break;
			case TokenNamecase :
				pushOnElementStack(K_BETWEEN_CASE_AND_COLON);
				break;
			case TokenNamedefault :
				pushOnElementStack(K_BETWEEN_DEFAULT_AND_COLON);
				break;
		}
	}
}
protected void consumeUnaryExpression(int op) {
	super.consumeUnaryExpression(op);
	popElement(K_UNARY_OPERATOR);
	
	if(expressionStack[expressionPtr] instanceof UnaryExpression) {
		UnaryExpression exp = (UnaryExpression) expressionStack[expressionPtr];
		if(assistNode != null && exp.expression == assistNode) {
			assistNodeParent = exp;
		}
	}
}
protected void consumeUnaryExpression(int op, boolean post) {
	super.consumeUnaryExpression(op, post);
	popElement(K_UNARY_OPERATOR);
	
	if(expressionStack[expressionPtr] instanceof UnaryExpression) {
		UnaryExpression exp = (UnaryExpression) expressionStack[expressionPtr];
		if(assistNode != null && exp.expression == assistNode) {
			assistNodeParent = exp;
		}
	}
}

public ImportReference createAssistImportReference(char[][] tokens, long[] positions){
	return new CompletionOnImportReference(tokens, positions);
}
public ImportReference createAssistPackageReference(char[][] tokens, long[] positions){
	return new CompletionOnPackageReference(tokens, positions);
}
public NameReference createQualifiedAssistNameReference(char[][] previousIdentifiers, char[] name, long[] positions){
	return new CompletionOnQualifiedNameReference(
					previousIdentifiers, 
					name, 
					positions); 	
}
public TypeReference createQualifiedAssistTypeReference(char[][] previousIdentifiers, char[] name, long[] positions){
	switch (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER)) {
		case K_NEXT_TYPEREF_IS_EXCEPTION :
			return new CompletionOnQualifiedExceptionReference(previousIdentifiers, name, positions);
		case K_NEXT_TYPEREF_IS_CLASS :
			return new CompletionOnQualifiedClassReference(previousIdentifiers, name, positions);
		case K_NEXT_TYPEREF_IS_INTERFACE :
			return new CompletionOnQualifiedInterfaceReference(previousIdentifiers, name, positions);
		default :
			return new CompletionOnQualifiedTypeReference(previousIdentifiers, name, positions); 
	}
}
public NameReference createSingleAssistNameReference(char[] name, long position) {
	int kind = topKnownElementKind(COMPLETION_OR_ASSIST_PARSER);
	if(!isInsideMethod()) {
		return new CompletionOnSingleNameReference(name, position);
	} else {
		boolean canBeExplicitConstructorCall = false;
		if(kind == K_BLOCK_DELIMITER
			&& previousKind == K_BLOCK_DELIMITER
			&& previousInfo == DO) {
			return new CompletionOnKeyword3(name, position, Keywords.WHILE);
		} else if(kind == K_BLOCK_DELIMITER
			&& previousKind == K_BLOCK_DELIMITER
			&& previousInfo == TRY) {
			return new CompletionOnKeyword3(name, position, new char[][]{Keywords.CATCH, Keywords.FINALLY});
		} else if(kind == K_BLOCK_DELIMITER
			&& topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == SWITCH) {
			return new CompletionOnKeyword3(name, position, new char[][]{Keywords.CASE, Keywords.DEFAULT});
		} else {
			char[][] keywords = new char[Keywords.COUNT][];
			int count = 0;
			
			if((lastModifiers & AccStatic) == 0) {
				keywords[count++]= Keywords.SUPER;
				keywords[count++]= Keywords.THIS;
			}
			keywords[count++]= Keywords.NEW;
			
			if(kind == K_BLOCK_DELIMITER) {
				if(canBeExplicitConstructor == YES) {
					canBeExplicitConstructorCall = true;
				}
				
				keywords[count++]= Keywords.ASSERT;
				keywords[count++]= Keywords.DO;
				keywords[count++]= Keywords.FOR;
				keywords[count++]= Keywords.IF;
				keywords[count++]= Keywords.RETURN;
				keywords[count++]= Keywords.SWITCH;
				keywords[count++]= Keywords.SYNCHRONIZED;
				keywords[count++]= Keywords.THROW;
				keywords[count++]= Keywords.TRY;
				keywords[count++]= Keywords.WHILE;
				
				keywords[count++]= Keywords.FINAL;
				keywords[count++]= Keywords.CLASS;
				
				if(previousKind == K_BLOCK_DELIMITER) {
					switch (previousInfo) {
						case IF :
							keywords[count++]= Keywords.ELSE;
							break;
						case CATCH :
							keywords[count++]= Keywords.CATCH;
							keywords[count++]= Keywords.FINALLY;
							break;
					}
				}
				if(isInsideLoop()) {
					keywords[count++]= Keywords.CONTINUE;
				}
				if(isInsideBreakable()) {
					keywords[count++]= Keywords.BREAK;
				}
			} else if(kind != K_BETWEEN_CASE_AND_COLON && kind != K_BETWEEN_DEFAULT_AND_COLON) {
				keywords[count++]= Keywords.TRUE;
				keywords[count++]= Keywords.FALSE;
				keywords[count++]= Keywords.NULL;
			
				if(kind == K_SWITCH_LABEL) {
					if(topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) != DEFAULT) {
						keywords[count++]= Keywords.DEFAULT;
					}
					keywords[count++]= Keywords.BREAK;
					keywords[count++]= Keywords.CASE;
				}
			}
			System.arraycopy(keywords, 0 , keywords = new char[count][], 0, count);
			
			return new CompletionOnSingleNameReference(name, position, keywords, canBeExplicitConstructorCall);
		}
	}
}
public TypeReference createSingleAssistTypeReference(char[] name, long position) {
	switch (topKnownElementKind(COMPLETION_OR_ASSIST_PARSER)) {
		case K_NEXT_TYPEREF_IS_EXCEPTION :
			return new CompletionOnExceptionReference(name, position) ;
		case K_NEXT_TYPEREF_IS_CLASS :
			return new CompletionOnClassReference(name, position);
		case K_NEXT_TYPEREF_IS_INTERFACE :
			return new CompletionOnInterfaceReference(name, position);
		default :
			return new CompletionOnSingleTypeReference(name, position); 
	}
}
public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int cursorLocation) {

	this.cursorLocation = cursorLocation;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completionIdentifier = null;
	completionScanner.cursorLocation = cursorLocation;
	return this.dietParse(sourceUnit, compilationResult);
}
/*
 * Flush parser/scanner state regarding to code assist
 */
public void flushAssistState() {

	super.flushAssistState();
	this.isOrphanCompletionNode = false;
	assistNodeParent = null;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completedIdentifierStart = 0;
	completionScanner.completedIdentifierEnd = -1;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	if (this.identifierLengthStack[this.identifierLengthPtr] > 1) { // reducing a qualified name
		// potential receiver is being poped, so reset potential receiver
		this.invocationType = NO_RECEIVER;
	}
	return super.getUnspecifiedReferenceOptimized();
}
public void initialize() {
	super.initialize();
	this.initializeForBlockStatements();
}
/*
 * Initializes the state of the parser that is about to go for BlockStatements.
 */
private void initializeForBlockStatements() {
	this.previousToken = -1;
	this.previousIdentifierPtr = -1;
	this.bracketDepth = 0;
	this.invocationType = NO_RECEIVER;
	this.qualifier = -1;
	popUntilElement(K_SWITCH_LABEL);
	if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) != K_SWITCH_LABEL) {
		this.popUntilElement(K_BLOCK_DELIMITER);
	}
}
public void initializeScanner(){
	this.scanner = new CompletionScanner(this.assertMode);
}
/**
 * Returns whether the completion is just after an array type
 * eg. String[].[cursor]
 */
private boolean isAfterArrayType() {
	// TBD: The following relies on the fact that array dimensions are small: it says that if the
	//      top of the intStack is less than 11, then it must be a dimension 
	//      (smallest position of array type in a compilation unit is 11 as in "class X{Y[]")
	if ((this.intPtr > -1) && (this.intStack[this.intPtr] < 11)) {
		return true;
	}
	return false;
}
private boolean isEmptyNameCompletion() {
	return
		this.assistNode != null && 
		this.assistNode instanceof CompletionOnSingleNameReference &&
		(((CompletionOnSingleNameReference)this.assistNode).token.length == 0);
}
protected boolean isIndirectlyInsideBlock(){
	int i = elementPtr;
	while(i > -1) {
		if(elementKindStack[i] == K_BLOCK_DELIMITER)
			return true;
		i--;
	}
	return false;
}

protected boolean isInsideBlock(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
			case K_BLOCK_DELIMITER : return true;
		}
		i--;
	}
	return false;
}
protected boolean isInsideBreakable(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
			case K_SWITCH_LABEL : return true;
			case K_BLOCK_DELIMITER :
				switch(elementInfoStack[i]) {
					case FOR :
					case DO :
					case WHILE :
						return true;
				}
		}
		i--;
	}
	return false;
}
protected boolean isInsideLoop(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
			case K_BLOCK_DELIMITER :
				switch(elementInfoStack[i]) {
					case FOR :
					case DO :
					case WHILE :
						return true;
				}
		}
		i--;
	}
	return false;
}
protected boolean isInsideReturn(){
	int i = elementPtr;
	while(i > -1) {
		switch (elementKindStack[i]) {
			case K_TYPE_DELIMITER : return false;
			case K_METHOD_DELIMITER : return false;
			case K_FIELD_INITIALIZER_DELIMITER : return false;
			case K_BLOCK_DELIMITER : return false;
			case K_INSIDE_RETURN_STATEMENT : return true;
		}
		i--;
	}
	return false;
}
public CompilationUnitDeclaration parse(ICompilationUnit sourceUnit, CompilationResult compilationResult, int cursorLocation) {

	this.cursorLocation = cursorLocation;
	CompletionScanner completionScanner = (CompletionScanner)this.scanner;
	completionScanner.completionIdentifier = null;
	completionScanner.cursorLocation = cursorLocation;
	return this.parse(sourceUnit, compilationResult);
}
public void parseBlockStatements(
	ConstructorDeclaration cd,
	CompilationUnitDeclaration unit) {
	canBeExplicitConstructor = 1;
	super.parseBlockStatements(cd, unit);
}
/*
 * Prepares the state of the parser to go for BlockStatements.
 */
protected void prepareForBlockStatements() {
	this.nestedMethod[this.nestedType = 0] = 1;
	this.variablesCounter[this.nestedType] = 0;
	this.realBlockStack[this.realBlockPtr = 1] = 0;

	this.initializeForBlockStatements();
}
/**
 * Creates a completion on member access node and push it
 * on the expression stack.
 */
private void pushCompletionOnMemberAccessOnExpressionStack(boolean isSuperAccess) {
	char[] source = identifierStack[identifierPtr];
	long pos = identifierPositionStack[identifierPtr--];
	CompletionOnMemberAccess fr = new CompletionOnMemberAccess(source, pos);
	this.assistNode = fr;
	this.lastCheckPoint = fr.sourceEnd + 1;
	identifierLengthPtr--;
	if (isSuperAccess) { //considerates the fieldReference beginning at the 'super' ....	
		fr.sourceStart = intStack[intPtr--];
		fr.receiver = new SuperReference(fr.sourceStart, endPosition);
		pushOnExpressionStack(fr);
	} else { //optimize push/pop
		if ((fr.receiver = expressionStack[expressionPtr]).isThis()) { //fieldreference begins at the this
			fr.sourceStart = fr.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fr;
	}
}
public void recordCompletionOnReference(){

	if (currentElement instanceof RecoveredType){
		RecoveredType recoveredType = (RecoveredType)currentElement;

		/* filter out cases where scanner is still inside type header */
		if (!recoveredType.foundOpeningBrace) return;
		
		/* generate a pseudo field with a completion on type reference */	
		currentElement.add(
			new CompletionOnFieldType(this.getTypeReference(0), false), 0);
		return;
	}
	if (!diet) return; // only record references attached to types

}
public void recoveryExitFromVariable() {
	if(currentElement != null && currentElement instanceof RecoveredLocalVariable) {
		RecoveredElement oldElement = currentElement;
		super.recoveryExitFromVariable();
		if(oldElement != currentElement) {
			popElement(K_LOCAL_INITIALIZER_DELIMITER);
		}
	} else {
		super.recoveryExitFromVariable();
	}
}
public void recoveryTokenCheck() {
	RecoveredElement oldElement = currentElement;
	switch (currentToken) {
		case TokenNameRBRACE :
			super.recoveryTokenCheck();
			if(currentElement != oldElement && oldElement instanceof RecoveredBlock) {
				popElement(K_BLOCK_DELIMITER);
			}
			break;
		case TokenNamecase :
			super.recoveryTokenCheck();
			if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BLOCK_DELIMITER
				&& topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == SWITCH) {
				pushOnElementStack(K_SWITCH_LABEL);
			}
			break;
		case TokenNamedefault :
			super.recoveryTokenCheck();
			if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_BLOCK_DELIMITER
				&& topKnownElementInfo(COMPLETION_OR_ASSIST_PARSER) == SWITCH) {
				pushOnElementStack(K_SWITCH_LABEL, DEFAULT);
			} else if(topKnownElementKind(COMPLETION_OR_ASSIST_PARSER) == K_SWITCH_LABEL) {
				popElement(K_SWITCH_LABEL);
				pushOnElementStack(K_SWITCH_LABEL, DEFAULT);
			}
			break;
		default :
			super.recoveryTokenCheck();
			break;
	}
}
protected void reportSyntaxError(int act, int currentKind, int stateStackTop) {

	/* Intercept error state on EOF inside method bodies, due to 
	   cursor location being used as an EOF position.
	*/
	if (!diet && currentToken == TokenNameEOF) return;
	super.reportSyntaxError(act, currentKind, stateStackTop);
}
/*
 * Reset internal state after completion is over
 */
 
public void reset() {
	super.reset();
	this.cursorLocation = 0;
}
/*
 * Reset internal state after completion is over
 */
 
public void resetAfterCompletion() {
	this.cursorLocation = 0;
	this.flushAssistState();
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {
	if (this.assistNode != null) {
		/* if reached [eof] inside method body, but still inside nested type,
			or inside a field initializer, should continue in diet mode until 
			the end of the method body or compilation unit */
		if ((scanner.eofPosition == cursorLocation+1)
			&& (!(referenceContext instanceof CompilationUnitDeclaration) 
				|| isIndirectlyInsideFieldInitialization())) {

			/*	disabled since does not handle possible field/message refs, i.e. Obj[ASSIST HERE]ect.registerNatives()		    
			// consume extra tokens which were part of the qualified reference
			//   so that the replaced source comprises them as well 
			if (this.assistNode instanceof NameReference){
				int oldEof = scanner.eofPosition;
				scanner.eofPosition = currentElement.topElement().sourceEnd()+1;
				scanner.currentPosition = this.cursorLocation+1;
				int token = -1;
				try {
					do {
						// first token might not have to be a dot
						if (token >= 0 || !this.completionBehindDot){
							if ((token = scanner.getNextToken()) != TokenNameDOT) break;
						}
						if ((token = scanner.getNextToken()) != TokenNameIdentifier) break;
						this.assistNode.sourceEnd = scanner.currentPosition - 1;
					} while (token != TokenNameEOF);
				} catch (InvalidInputException e){
				} finally {
					scanner.eofPosition = oldEof;
				}
			}
			*/			
			/* restart in diet mode for finding sibling constructs */
			if (currentElement instanceof RecoveredType
				|| currentElement.enclosingType() != null){
					
				if(lastCheckPoint <= this.assistNode.sourceEnd) {
					lastCheckPoint = this.assistNode.sourceEnd+1;
				}
				int end = currentElement.topElement().sourceEnd();
				scanner.eofPosition = end < Integer.MAX_VALUE ? end + 1 : end;
			} else {
				this.resetStacks();
				return false;	
			}
		}
	}
	return super.resumeAfterRecovery();
}
public void setAssistIdentifier(char[] assistIdent){
	((CompletionScanner)scanner).completionIdentifier = assistIdent;
}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* may be able to retrieve completionNode as an orphan, and then attach it */
	this.completionIdentifierCheck();
	this.attachOrphanCompletionNode();
	
	// if an assist node has been found and a recovered element exists,
	// mark enclosing blocks as to be preserved
	if (this.assistNode != null && this.currentElement != null) {
		currentElement.preserveEnclosingBlocks();
	}
	
	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
	
	this.recoveryExitFromVariable();
}

protected LocalDeclaration createLocalDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0) {
		return super.createLocalDeclaration(initialization, name, sourceStart, sourceEnd);
	} else {
		CompletionOnLocalName local = new CompletionOnLocalName(initialization, name, sourceStart, sourceEnd);
		this.assistNode = local;
		this.lastCheckPoint = sourceEnd + 1;
		return local;
	}
}

protected FieldDeclaration createFieldDeclaration(Expression initialization, char[] name, int sourceStart, int sourceEnd) {
	if (this.indexOfAssistIdentifier() < 0 || (currentElement instanceof RecoveredUnit && ((RecoveredUnit)currentElement).typeCount == 0)) {
		return super.createFieldDeclaration(initialization, name, sourceStart, sourceEnd);
	} else {
		CompletionOnFieldName field = new CompletionOnFieldName(initialization, name, sourceStart, sourceEnd);
		this.assistNode = field;
		this.lastCheckPoint = sourceEnd + 1;
		return field;
	}
}
}

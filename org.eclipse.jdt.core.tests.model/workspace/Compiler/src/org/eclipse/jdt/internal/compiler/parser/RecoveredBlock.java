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
package org.eclipse.jdt.internal.compiler.parser;

/**
 * Internal block structure for parsing recovery 
 */
import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.internal.compiler.ast.Argument;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.Block;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.BaseTypes;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;

public class RecoveredBlock extends RecoveredStatement implements CompilerModifiers, TerminalTokens, BaseTypes {

	public Block blockDeclaration;
	public RecoveredStatement[] statements;
	public int statementCount;
	public boolean preserveContent = false;
	public RecoveredLocalVariable pendingArgument;
	
public RecoveredBlock(Block block, RecoveredElement parent, int bracketBalance){
	super(block, parent, bracketBalance);
	this.blockDeclaration = block;
	this.foundOpeningBrace = true;
}
/*
 * Record a nested block declaration 
 */
public RecoveredElement add(Block nestedBlockDeclaration, int bracketBalanceValue) {

	/* do not consider a nested block starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (this.blockDeclaration.sourceEnd != 0 
		&& nestedBlockDeclaration.sourceStart > this.blockDeclaration.sourceEnd){
		return this.parent.add(nestedBlockDeclaration, bracketBalanceValue);
	}
			
	RecoveredBlock element = new RecoveredBlock(nestedBlockDeclaration, this, bracketBalanceValue);

	// if we have a pending Argument, promote it into the new block
	if (this.pendingArgument != null){
		element.attach(this.pendingArgument);
		this.pendingArgument = null;
	}
	this.attach(element);
	if (nestedBlockDeclaration.sourceEnd == 0) return element;
	return this;	
}
/*
 * Record a local declaration 
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalanceValue) {
	return this.add(localDeclaration, bracketBalanceValue, false);
}
/*
 * Record a local declaration 
 */
public RecoveredElement add(LocalDeclaration localDeclaration, int bracketBalanceValue, boolean delegatedByParent) {

	/* local variables inside method can only be final and non void */
/*	
	char[][] localTypeName; 
	if ((localDeclaration.modifiers & ~AccFinal) != 0 // local var can only be final 
		|| (localDeclaration.type == null) // initializer
		|| ((localTypeName = localDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(localTypeName[0], VoidBinding.sourceName()))){ 

		if (delegatedByParent){
			return this; //ignore
		} else {
			this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(localDeclaration.declarationSourceStart - 1));
			return this.parent.add(localDeclaration, bracketBalance);
		}
	}
*/	
		/* do not consider a local variable starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (this.blockDeclaration.sourceEnd != 0 
			&& localDeclaration.declarationSourceStart > this.blockDeclaration.sourceEnd){
		if (delegatedByParent) return this; //ignore
		return this.parent.add(localDeclaration, bracketBalanceValue);
	}

	RecoveredLocalVariable element = new RecoveredLocalVariable(localDeclaration, this, bracketBalanceValue);

	if (localDeclaration instanceof Argument){
		this.pendingArgument = element;
		return this;
	}
	
	this.attach(element);
	if (localDeclaration.declarationSourceEnd == 0) return element;
	return this;	
}
/*
 * Record a statement declaration 
 */
public RecoveredElement add(Statement stmt, int bracketBalanceValue) {
	return this.add(stmt, bracketBalanceValue, false);
}

/*
 * Record a statement declaration 
 */
public RecoveredElement add(Statement stmt, int bracketBalanceValue, boolean delegatedByParent) {

	/* do not consider a nested block starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (this.blockDeclaration.sourceEnd != 0 
			&& stmt.sourceStart > this.blockDeclaration.sourceEnd){
		if (delegatedByParent) return this; //ignore
		return this.parent.add(stmt, bracketBalanceValue);
	}
			
	RecoveredStatement element = new RecoveredStatement(stmt, this, bracketBalanceValue);
	this.attach(element);
	if (stmt.sourceEnd == 0) return element;
	return this;	
}
/*
 * Addition of a type to an initializer (act like inside method body)
 */
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue) {
	return this.add(typeDeclaration, bracketBalanceValue, false);
}
/*
 * Addition of a type to an initializer (act like inside method body)
 */
public RecoveredElement add(TypeDeclaration typeDeclaration, int bracketBalanceValue, boolean delegatedByParent) {

	/* do not consider a type starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (this.blockDeclaration.sourceEnd != 0 
			&& typeDeclaration.declarationSourceStart > this.blockDeclaration.sourceEnd){
		if (delegatedByParent) return this; //ignore
		return this.parent.add(typeDeclaration, bracketBalanceValue);
	}
			
	RecoveredStatement element = new RecoveredType(typeDeclaration, this, bracketBalanceValue);
	this.attach(element);
	if (typeDeclaration.declarationSourceEnd == 0) return element;
	return this;
}
/*
 * Attach a recovered statement
 */
void attach(RecoveredStatement recoveredStatement) {

	if (this.statements == null) {
		this.statements = new RecoveredStatement[5];
		this.statementCount = 0;
	} else {
		if (this.statementCount == this.statements.length) {
			System.arraycopy(
				this.statements, 
				0, 
				(this.statements = new RecoveredStatement[2 * this.statementCount]), 
				0, 
				this.statementCount); 
		}
	}
	this.statements[this.statementCount++] = recoveredStatement;
}
/* 
 * Answer the associated parsed structure
 */
public ASTNode parseTree(){
	return this.blockDeclaration;
}
public String toString(int tab) {
	StringBuffer result = new StringBuffer(tabString(tab));
	result.append("Recovered block:\n"); //$NON-NLS-1$
	this.blockDeclaration.print(tab + 1, result);
	if (this.statements != null) {
		for (int i = 0; i < this.statementCount; i++) {
			result.append("\n"); //$NON-NLS-1$
			result.append(this.statements[i].toString(tab + 1));
		}
	}
	return result.toString();
}
/*
 * Rebuild a block from the nested structure which is in scope
 */
public Block updatedBlock(){

	// if block was not marked to be preserved or empty, then ignore it
	if (!this.preserveContent || this.statementCount == 0) return null;

	Statement[] updatedStatements = new Statement[this.statementCount];
	int updatedCount = 0;
	
	// only collect the non-null updated statements
	for (int i = 0; i < this.statementCount; i++){
		Statement updatedStatement = this.statements[i].updatedStatement();
		if (updatedStatement != null){
			updatedStatements[updatedCount++] = updatedStatement;
		}
	}
	if (updatedCount == 0) return null; // not interesting block

	// resize statement collection if necessary
	if (updatedCount != this.statementCount){
		this.blockDeclaration.statements = new Statement[updatedCount];
		System.arraycopy(updatedStatements, 0, this.blockDeclaration.statements, 0, updatedCount);
	} else {
		this.blockDeclaration.statements = updatedStatements;
	}

	return this.blockDeclaration;
}
/*
 * Rebuild a statement from the nested structure which is in scope
 */
public Statement updatedStatement(){

	return this.updatedBlock();
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if ((--this.bracketBalance <= 0) && (this.parent != null)){
		this.updateSourceEndIfNecessary(braceStart, braceEnd);

		/* if the block is the method body, then it closes the method too */
		RecoveredMethod method = enclosingMethod();
		if (method != null && method.methodBody == this){
			return this.parent.updateOnClosingBrace(braceStart, braceEnd);
		}
		RecoveredInitializer initializer = enclosingInitializer();
		if (initializer != null && initializer.initializerBody == this){
			return this.parent.updateOnClosingBrace(braceStart, braceEnd);
		}
		return this.parent;
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int braceStart, int braceEnd){

	// create a nested block
	Block block = new Block(0);
	block.sourceStart = parser().scanner.startPosition;
	return this.add(block, 1);
}
/*
 * Final update the corresponding parse node
 */
public void updateParseTree(){

	this.updatedBlock();
}
/*
 * Rebuild a flattened block from the nested structure which is in scope
 */
public Statement updateStatement(){

	// if block was closed or empty, then ignore it
	if (this.blockDeclaration.sourceEnd != 0 || this.statementCount == 0) return null;

	Statement[] updatedStatements = new Statement[this.statementCount];
	int updatedCount = 0;
	
	// only collect the non-null updated statements
	for (int i = 0; i < this.statementCount; i++){
		Statement updatedStatement = this.statements[i].updatedStatement();
		if (updatedStatement != null){
			updatedStatements[updatedCount++] = updatedStatement;
		}
	}
	if (updatedCount == 0) return null; // not interesting block

	// resize statement collection if necessary
	if (updatedCount != this.statementCount){
		this.blockDeclaration.statements = new Statement[updatedCount];
		System.arraycopy(updatedStatements, 0, this.blockDeclaration.statements, 0, updatedCount);
	} else {
		this.blockDeclaration.statements = updatedStatements;
	}

	return this.blockDeclaration;
}

/*
 * Record a field declaration 
 */
public RecoveredElement add(FieldDeclaration fieldDeclaration, int bracketBalanceValue) {

	/* local variables inside method can only be final and non void */
	char[][] fieldTypeName; 
	if ((fieldDeclaration.modifiers & ~AccFinal) != 0 // local var can only be final 
		|| (fieldDeclaration.type == null) // initializer
		|| ((fieldTypeName = fieldDeclaration.type.getTypeName()).length == 1 // non void
			&& CharOperation.equals(fieldTypeName[0], VoidBinding.sourceName()))){ 
		this.updateSourceEndIfNecessary(this.previousAvailableLineEnd(fieldDeclaration.declarationSourceStart - 1));
		return this.parent.add(fieldDeclaration, bracketBalanceValue);
	}
	
	/* do not consider a local variable starting passed the block end (if set)
		it must be belonging to an enclosing block */
	if (this.blockDeclaration.sourceEnd != 0 
		&& fieldDeclaration.declarationSourceStart > this.blockDeclaration.sourceEnd){
		return this.parent.add(fieldDeclaration, bracketBalanceValue);
	}

	// ignore the added field, since indicates a local variable behind recovery point
	// which thus got parsed as a field reference. This can happen if restarting after
	// having reduced an assistNode to get the following context (see 1GEK7SG)
	return this;	
}
}

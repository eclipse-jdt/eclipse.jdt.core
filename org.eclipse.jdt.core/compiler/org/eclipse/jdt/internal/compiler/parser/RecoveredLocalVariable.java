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
package org.eclipse.jdt.internal.compiler.parser;

/**
 * Internal local variable structure for parsing recovery 
 */
import org.eclipse.jdt.internal.compiler.ast.ArrayTypeReference;
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.LocalDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Statement;

public class RecoveredLocalVariable extends RecoveredStatement {

	public LocalDeclaration localDeclaration;
	boolean alreadyCompletedLocalInitialization;
public RecoveredLocalVariable(LocalDeclaration localDeclaration, RecoveredElement parent, int bracketBalance){
	super(localDeclaration, parent, bracketBalance);
	this.localDeclaration = localDeclaration;
	this.alreadyCompletedLocalInitialization = localDeclaration.initialization != null;
}
/*
 * Record an expression statement if local variable is expecting an initialization expression. 
 */
public RecoveredElement add(Statement statement, int bracketBalance) {

	if (this.alreadyCompletedLocalInitialization || !(statement instanceof Expression)) {
		return super.add(statement, bracketBalance);
	} else {
		this.alreadyCompletedLocalInitialization = true;
		this.localDeclaration.initialization = (Expression)statement;
		this.localDeclaration.declarationSourceEnd = statement.sourceEnd;
		this.localDeclaration.declarationEnd = statement.sourceEnd;
		return this;
	}
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return localDeclaration;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.localDeclaration.declarationSourceEnd;
}
public String toString(int tab) {
	return tabString(tab) + "Recovered local variable:\n" + localDeclaration.toString(tab + 1); //$NON-NLS-1$
}
public Statement updatedStatement(){
	return localDeclaration;
}
/*
 * A closing brace got consumed, might have closed the current element,
 * in which case both the currentElement is exited.
 *
 * Fields have no associated braces, thus if matches, then update parent.
 */
public RecoveredElement updateOnClosingBrace(int braceStart, int braceEnd){
	if (bracketBalance > 0){ // was an array initializer
		bracketBalance--;
		if (bracketBalance == 0) alreadyCompletedLocalInitialization = true;
		return this;
	}
	if (parent != null){
		return parent.updateOnClosingBrace(braceStart, braceEnd);
	}
	return this;
}
/*
 * An opening brace got consumed, might be the expected opening one of the current element,
 * in which case the bodyStart is updated.
 */
public RecoveredElement updateOnOpeningBrace(int currentPosition){
	if (localDeclaration.declarationSourceEnd == 0 
		&& localDeclaration.type instanceof ArrayTypeReference
		&& !alreadyCompletedLocalInitialization){
		bracketBalance++;
		return null; // no update is necessary	(array initializer)
	}
	// might be an array initializer
	this.updateSourceEndIfNecessary(currentPosition - 1);	
	return this.parent.updateOnOpeningBrace(currentPosition);	
}
public void updateParseTree(){
	this.updatedStatement();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.localDeclaration.declarationSourceEnd == 0) {
		this.localDeclaration.declarationSourceEnd = sourceEnd;
		this.localDeclaration.declarationEnd = sourceEnd;	
	}
}
}

package org.eclipse.jdt.internal.compiler.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Internal statement structure for parsing recovery 
 */
import org.eclipse.jdt.internal.compiler.ast.*;

public class RecoveredStatement extends RecoveredElement {

	public Statement statement;
	boolean alreadyCompletedLocalInitialization;
public RecoveredStatement(Statement statement, RecoveredElement parent, int bracketBalance){
	super(parent, bracketBalance);
	this.statement = statement;
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return statement;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.statement.sourceEnd;
}
public String toString(int tab){
	return tabString(tab) + "Recovered statement:\n" + statement.toString(tab + 1); //$NON-NLS-1$
}
public Statement updatedStatement(){
	return statement;
}
public void updateParseTree(){
	this.updatedStatement();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.statement.sourceEnd == 0)	
		this.statement.sourceEnd = sourceEnd;
}
}

package org.eclipse.jdt.internal.compiler.parser;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Internal import structure for parsing recovery 
 */
 import org.eclipse.jdt.internal.compiler.ast.*;

public class RecoveredImport extends RecoveredElement {

	public ImportReference importReference;
public RecoveredImport(ImportReference importReference, RecoveredElement parent, int bracketBalance){
	super(parent, bracketBalance);
	this.importReference = importReference;
}
/* 
 * Answer the associated parsed structure
 */
public AstNode parseTree(){
	return importReference;
}
/*
 * Answer the very source end of the corresponding parse node
 */
public int sourceEnd(){
	return this.importReference.declarationSourceEnd;
}
public String toString(int tab) {
	return tabString(tab) + "Recovered import: " + importReference.toString(); //$NON-NLS-1$
}
public ImportReference updatedImportReference(){

	return importReference;
}
public void updateParseTree(){
	this.updatedImportReference();
}
/*
 * Update the declarationSourceEnd of the corresponding parse node
 */
public void updateSourceEndIfNecessary(int sourceEnd){
	if (this.importReference.declarationSourceEnd == 0)	
		this.importReference.declarationSourceEnd = sourceEnd;
}
}

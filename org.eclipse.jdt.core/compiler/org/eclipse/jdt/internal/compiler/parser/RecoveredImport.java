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
 * Internal import structure for parsing recovery 
 */
import org.eclipse.jdt.internal.compiler.ast.AstNode;
import org.eclipse.jdt.internal.compiler.ast.ImportReference;

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
	if (this.importReference.declarationSourceEnd == 0) {
		this.importReference.declarationSourceEnd = sourceEnd;
		this.importReference.declarationEnd = sourceEnd;
	}
}
}

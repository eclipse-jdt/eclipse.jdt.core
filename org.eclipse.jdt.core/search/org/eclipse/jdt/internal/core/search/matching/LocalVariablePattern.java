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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.index.IIndex;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;

public class LocalVariablePattern extends VariablePattern {
	
LocalVariable localVariable;

public LocalVariablePattern(
	boolean findDeclarations,
	boolean readAccess,
	boolean writeAccess,
	LocalVariable localVariable,
	int matchMode, 
	boolean isCaseSensitive) {

	super(LOCAL_VAR_PATTERN, findDeclarations, readAccess, writeAccess, localVariable.getElementName().toCharArray(), matchMode, isCaseSensitive);
	this.localVariable = localVariable;
}

protected void acceptPath(IIndexSearchRequestor requestor, String path) {
	requestor.acceptFieldDeclaration(path, null); // just remember the path
}
/*
 * @see SearchPattern#findIndexMatches
 */
public void findIndexMatches(IIndex index, IIndexSearchRequestor requestor, IProgressMonitor progressMonitor, IJavaSearchScope scope) {
	String path = this.localVariable.getPath().toString();
	if (scope.encloses(path))
		acceptPath(requestor, path);
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	if (this.findDeclarations) {
		buffer.append(this.findReferences
			? "LocalVarCombinedPattern: " //$NON-NLS-1$
			: "LocalVarDeclarationPattern: "); //$NON-NLS-1$
	} else {
		buffer.append("LocalVarReferencePattern: "); //$NON-NLS-1$
	}
	buffer.append(this.localVariable.toStringWithAncestors());
	buffer.append(", "); //$NON-NLS-1$
	switch(this.matchMode){
		case EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(this.isCaseSensitive ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}

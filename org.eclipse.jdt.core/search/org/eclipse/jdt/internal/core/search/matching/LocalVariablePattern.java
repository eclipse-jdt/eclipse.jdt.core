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
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.LocalVariable;
import org.eclipse.jdt.internal.core.index.Index;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;

public class LocalVariablePattern extends VariablePattern implements IIndexConstants {
	
LocalVariable localVariable;

public LocalVariablePattern(boolean findDeclarations, boolean readAccess, boolean writeAccess, LocalVariable localVariable, int matchRule) {
	super(LOCAL_VAR_PATTERN, findDeclarations, readAccess, writeAccess, localVariable.getElementName().toCharArray(), matchRule);
	this.localVariable = localVariable;
}
public void findIndexMatches(Index index, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) {
    IPackageFragmentRoot root = (IPackageFragmentRoot)this.localVariable.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
	String path;
    if (root.isArchive()) {
        IType type = (IType)this.localVariable.getAncestor(IJavaElement.TYPE);
        String filePath = (type.getFullyQualifiedName('/')).replace('.', '/') + SuffixConstants.SUFFIX_STRING_class;
        path = root.getPath() + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + filePath;
    } else {
        path = this.localVariable.getPath().toString();
    }
	if (scope.encloses(path))
		if (!requestor.acceptIndexMatch(path, this, participant)) 
			throw new OperationCanceledException();
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
	switch(getMatchMode()) {
		case R_EXACT_MATCH : 
			buffer.append("exact match, "); //$NON-NLS-1$
			break;
		case R_PREFIX_MATCH :
			buffer.append("prefix match, "); //$NON-NLS-1$
			break;
		case R_PATTERN_MATCH :
			buffer.append("pattern match, "); //$NON-NLS-1$
			break;
	}
	buffer.append(isCaseSensitive() ? "case sensitive" : "case insensitive"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
}

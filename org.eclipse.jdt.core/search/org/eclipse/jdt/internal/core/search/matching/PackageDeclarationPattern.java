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
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchParticipant;
import org.eclipse.jdt.internal.core.index.impl.IndexInput;
import org.eclipse.jdt.internal.core.search.IndexQueryRequestor;

public class PackageDeclarationPattern extends SearchPattern {

protected char[] pkgName;

public PackageDeclarationPattern(char[] pkgName, int matchRule) {
	super(PKG_DECL_PATTERN, matchRule);
	this.pkgName = pkgName;
}
public void decodeIndexKey(char[] key) {
	// package declarations are not indexed
}
public char[] encodeIndexKey() {
	// package declarations are not indexed
	return null;
}
public void findIndexMatches(IndexInput input, IndexQueryRequestor requestor, SearchParticipant participant, IJavaSearchScope scope, IProgressMonitor progressMonitor) /* throws IOException */ {
	// package declarations are not indexed
}
public SearchPattern getIndexRecord() {
	// package declarations are not indexed
	return null;
}
public char[][] getMatchCategories() {
	// package declarations are not indexed
	return CharOperation.NO_CHAR_CHAR;
}
public boolean isMatchingIndexRecord() {
	// package declarations are not indexed
	return false;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("PackageDeclarationPattern: <"); //$NON-NLS-1$
	if (this.pkgName != null) 
		buffer.append(this.pkgName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
	switch(matchMode()){
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
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}

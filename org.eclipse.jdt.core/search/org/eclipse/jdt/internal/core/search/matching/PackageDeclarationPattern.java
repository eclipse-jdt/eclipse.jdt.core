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

import org.eclipse.jdt.core.search.*;
import org.eclipse.jdt.internal.core.index.*;

public class PackageDeclarationPattern extends SearchPattern {

protected char[] pkgName;

public PackageDeclarationPattern(char[] pkgName, int matchRule) {
	super(PKG_DECL_PATTERN, matchRule);
	this.pkgName = pkgName;
}
EntryResult[] queryIn(Index index) {
	// package declarations are not indexed
	return null;
}
public String toString() {
	StringBuffer buffer = new StringBuffer(20);
	buffer.append("PackageDeclarationPattern: <"); //$NON-NLS-1$
	if (this.pkgName != null) 
		buffer.append(this.pkgName);
	else
		buffer.append("*"); //$NON-NLS-1$
	buffer.append(">, "); //$NON-NLS-1$
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
	if (isCaseSensitive())
		buffer.append("case sensitive"); //$NON-NLS-1$
	else
		buffer.append("case insensitive"); //$NON-NLS-1$
	return buffer.toString();
}
}

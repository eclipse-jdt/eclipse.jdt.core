/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.search.TypeNameMatch;

/**
 * Specific match collected while searching for all type names
 * when type belongs to a working copy.
 * 
 * @since 3.3
 */
public class WorkingCopiesTypeNameMatch extends TypeNameMatch {
	private ICompilationUnit[] workingCopies;

public WorkingCopiesTypeNameMatch(int modifiers, char[] packageName, char[] typeName, char[][] enclosingTypeNames, String path, ICompilationUnit[] workingCopies) {
	super(modifiers, packageName, typeName, enclosingTypeNames, path);
	this.workingCopies = workingCopies;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.search.TypeNameMatch#getWorkingCopies()
 */
protected ICompilationUnit[] getWorkingCopies() {
	return this.workingCopies;
}
}

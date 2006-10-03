/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.TypeNameMatch;
import org.eclipse.jdt.core.search.TypeNameMatchRequestor;
import org.eclipse.jdt.core.search.TypeNameRequestor;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;

/**
 * Wrapper used to link {@link IRestrictedAccessTypeRequestor} with {@link TypeNameRequestor}.
 * This wrapper specifically allows usage of internal method {@link BasicSearchEngine#searchAllTypeNames(
 * 	char[] packageName, 
 * 	int packageMatchRule, 
 * 	char[] typeName,
 * 	int typeMatchRule, 
 * 	int searchFor, 
 * 	org.eclipse.jdt.core.search.IJavaSearchScope scope, 
 * 	IRestrictedAccessTypeRequestor nameRequestor,
 * 	int waitingPolicy,
 * 	org.eclipse.core.runtime.IProgressMonitor monitor) }.
 * from  API method {@link org.eclipse.jdt.core.search.SearchEngine#searchAllTypeNames(
 * 	char[] packageName, 
 * 	int packageMatchRule,
 * 	char[] typeName,
 * 	int matchRule, 
 * 	int searchFor, 
 * 	org.eclipse.jdt.core.search.IJavaSearchScope scope, 
 * 	TypeNameRequestor nameRequestor,
 * 	int waitingPolicy,
 * 	org.eclipse.core.runtime.IProgressMonitor monitor) }.
 */
public class TypeNameMatchRequestorWrapper implements IRestrictedAccessTypeRequestor {
	private TypeNameMatchRequestor requestor;
	private IJavaSearchScope scope; // scope is needed to retrieve project path for external resource
	private ICompilationUnit[] workingCopies; // working copies in which types may be found

public TypeNameMatchRequestorWrapper(TypeNameMatchRequestor requestor, IJavaSearchScope scope, ICompilationUnit[] workingCopies) {
	this.requestor = requestor;
	this.scope = scope;
	this.workingCopies = workingCopies;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.internal.core.search.IRestrictedAccessTypeRequestor#acceptType(int, char[], char[], char[][], java.lang.String, org.eclipse.jdt.internal.compiler.env.AccessRestriction)
 */
public void acceptType(int modifiers, char[] packageName, char[] simpleTypeName, char[][] enclosingTypeNames, String path, AccessRestriction access) {
	if (access == null) { // accept only if there's no access violation
		if (this.scope instanceof JavaSearchScope) {
			String projectPath = ((JavaSearchScope)this.scope).projectPathFor(path);
			if (projectPath == null) {
				if (this.workingCopies == null) {
					// Internal resource, project path won't be store as it can be computed from path
					TypeNameMatch match = new TypeNameMatch(modifiers, packageName, simpleTypeName, enclosingTypeNames, path);
					this.requestor.acceptTypeNameMatch(match);
				} else {
					// Internal working copy, project path won't be store as it can be computed from path
					WorkingCopiesTypeNameMatch match = new WorkingCopiesTypeNameMatch(modifiers, packageName, simpleTypeName, enclosingTypeNames, path, this.workingCopies);
					this.requestor.acceptTypeNameMatch(match);
				}
			} else {
				// External resource, store specific project path
				ExternalTypeNameMatch match = new ExternalTypeNameMatch(modifiers, packageName, simpleTypeName, enclosingTypeNames, path, projectPath);
				this.requestor.acceptTypeNameMatch(match);
			}
		}
	}
}
}

/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

import org.eclipse.jdt.core.ICompletionRequestor;

//TODO remove this class once new CompletionRequestor is working.
public interface IExtendedCompletionRequestor extends ICompletionRequestor {
	void acceptPotentialMethodDeclaration(
			char[] declaringTypePackageName,
			char[] declaringTypeName,
			char[] selector,
			int completionStart,
			int completionEnd,
			int relevance);
}

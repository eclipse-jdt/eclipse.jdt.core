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
package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnQualifiedClassReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedClassReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public String toStringExpression(int tab) {

	StringBuffer buffer = new StringBuffer();
	buffer. append("<CompleteOnClass:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		buffer.append(tokens[i]);
		buffer.append("."); //$NON-NLS-1$
	}
	buffer.append(completionIdentifier).append(">"); //$NON-NLS-1$
	return buffer.toString();
}
}

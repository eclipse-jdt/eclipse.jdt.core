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
package org.eclipse.jdt.internal.codeassist.complete;

public class CompletionOnQualifiedClassReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedClassReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public StringBuffer printExpression(int indent, StringBuffer output) {

	output.append("<CompleteOnClass:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		output.append(tokens[i]);
		output.append('.');
	}
	output.append(completionIdentifier).append('>');
	return output;
}
}

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

public class CompletionOnClassReference extends CompletionOnSingleTypeReference {

	public CompletionOnClassReference(char[] source, long pos) {

		super(source, pos);
	}

	public StringBuffer printExpression(int indent, StringBuffer output) {
		
		return output.append("<CompleteOnClass:").append(this.token).append('>'); //$NON-NLS-1$
	}
}

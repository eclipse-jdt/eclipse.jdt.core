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

/*
 * Completion node build by the parser in any case it was intending to
 * reduce an exception type reference containing the completion identifier 
 * as part of a qualified name.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      try {
 *        bar();
 *      } catch (java.io.IOExc[cursor] e) {
 *      }
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           try {
 *             bar();
 *           } catch (<CompleteOnException:java.io.IOExc> e) {
 *           }
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */
public class CompletionOnQualifiedExceptionReference extends CompletionOnQualifiedTypeReference {
public CompletionOnQualifiedExceptionReference(char[][] previousIdentifiers, char[] completionIdentifier, long[] positions) {
	super(previousIdentifiers, completionIdentifier, positions);
}
public StringBuffer printExpression(int indent, StringBuffer output) {

	output.append("<CompleteOnException:"); //$NON-NLS-1$
	for (int i = 0; i < tokens.length; i++) {
		output.append(tokens[i]);
		output.append('.'); 
	}
	output.append(completionIdentifier).append('>');
	return output;
}
}

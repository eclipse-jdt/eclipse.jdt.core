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

/*
 * Completion node build by the parser in any case it was intending to
 * reduce a single name reference containing the completion identifier.
 * e.g.
 *
 *	class X {
 *    void foo() {
 *      ba[cursor]
 *    }
 *  }
 *
 *	---> class X {
 *         void foo() {
 *           <CompleteOnName:ba>
 *         }
 *       }
 *
 * The source range of the completion node denotes the source range
 * which should be replaced by the completion.
 */

import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
 
public class CompletionOnSingleNameReference extends SingleNameReference {
public char[][] possibleKeywords;
public boolean canBeExplicitConstructor;
public CompletionOnSingleNameReference(char[] source, long pos) {
	this(source, pos, null, false);
}
public CompletionOnSingleNameReference(char[] source, long pos, char[][] possibleKeywords, boolean canBeExplicitConstructor) {
	super(source, pos);
	this.possibleKeywords = possibleKeywords;
	this.canBeExplicitConstructor = canBeExplicitConstructor;
}
public TypeBinding resolveType(BlockScope scope) {
	throw new CompletionNodeFound(this, scope);
}
public String toStringExpression() {
	return "<CompleteOnName:" + super.toStringExpression() + ">"; //$NON-NLS-2$ //$NON-NLS-1$
}
}

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
package org.eclipse.jdt.internal.compiler.lookup;

public class ProblemReferenceBinding extends ReferenceBinding {
	public ReferenceBinding original;
	private int problemReason;
	public ReferenceBinding alternateMatch;
	
// NOTE: must only answer the subset of the name related to the problem

public ProblemReferenceBinding(char[][] compoundName, int problemReason) {
	this(compoundName, null, problemReason);
}
public ProblemReferenceBinding(char[] name, int problemReason) {
	this(new char[][] {name}, null, problemReason);
}

public ProblemReferenceBinding(char[][] compoundName, ReferenceBinding original, int problemReason) {
	this.compoundName = compoundName;
	this.original = original;
	this.problemReason = problemReason;
}
public ProblemReferenceBinding(char[] name, ReferenceBinding original, int problemReason) {
	this(new char[][] {name}, original, problemReason);
}
/* API
* Answer the problem id associated with the receiver.
* NoError if the receiver is a valid binding.
*/
public int problemId() {
	return this.problemReason;
}

/**
 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#shortReadableName()
 */
public char[] shortReadableName() {
	return readableName();
}

}

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
package org.eclipse.jdt.internal.compiler.ast;

/**
 */
public class JavadocImportReference extends ImportReference {

	public int tagSourceStart, tagSourceEnd;

	/**
	 * @param tokens
	 * @param sourcePositions
	 * @param tagStart
	 * @param tagEnd
	 */
	public JavadocImportReference(char[][] tokens, long[] sourcePositions, int tagStart, int tagEnd) {
		super(tokens, sourcePositions, false, AccDefault);
		this.tagSourceStart = tagStart;
		this.tagSourceEnd = tagEnd;
		this.bits |= InsideJavadoc;
	}

}

/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import org.eclipse.jdt.internal.compiler.parser.JavadocTagConstants;

/**
 * Abstract class for all {@link FormatJavadoc} nodes.
 * <p>
 * The basic information for these nodes are the start and end positions in the
 * source.
 * </p>
 */
public abstract class FormatJavadocNode implements JavadocTagConstants {

	// default size used for array
	final static int DEFAULT_ARRAY_SIZE = 10;
	final static int INCREMENT_ARRAY_SIZE = 10;
	protected int sourceStart, sourceEnd;

public FormatJavadocNode(int start, int end) {
	this.sourceStart = start;
	this.sourceEnd = end;
}

abstract void clean();

/**
 * Returns whether the node is a text (see {@link FormatJavadocText} or not.
 * In case not, that means that the node is an block (see
 * {@link FormatJavadocBlock}).
 * 
 * @return <code>true</code> if the node is a text <code>false</code>
 * 	otherwise.
 */
public boolean isText() {
	return false;
}
}

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

import org.eclipse.jdt.core.compiler.CharOperation;

/**
 * Representation of an annotation
 */
public class AnnotationBinding extends Binding {
	
	private static char[] ANNOTATION_PREFIX = "@".toCharArray(); //$NON-NLS-1$
	private static char[] ANNOTATION_SUFFIX = "()".toCharArray(); //$NON-NLS-1$
	
	public ReferenceBinding type;
	public char[][] attributeKeys;
	public Object[] attributeValues;
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#kind()
	 */
	public int kind() {
		return ANNOTATION_BINDING;
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() {
		return CharOperation.concat(ANNOTATION_PREFIX, this.type.readableName(), ANNOTATION_SUFFIX);
	}
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() {
		return CharOperation.concat(ANNOTATION_PREFIX, this.type.shortReadableName(), ANNOTATION_SUFFIX);
	}
	
	
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

public class NLSTag {

	public int start;
	public int end;
	public int bits;
	public static final int USED = 1;
	public static final int UNUSED = 2;
	
	public NLSTag(int start, int end) {
		this(start, end, UNUSED);
	}

	public NLSTag(int start, int end, int bits) {
		this.start = start;
		this.end = end;
		this.bits = bits;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj instanceof NLSTag) {
			NLSTag tag = (NLSTag) obj;
			return (tag.start == this.start) && (tag.end == this.end);
		}
		return false;
	}
	
	public int hashCode() {
		return this.start;
	}
	
	public String toString() {
		return "NLSTag(" + this.start + "," + this.end + "," + this.bits + ")"; //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}
}

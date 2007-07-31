/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Matt McCutchen - fix for bug 197169
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter.comment;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.internal.compiler.parser.*;

/**
 * <code>SubstitutionTextReader</code> that will substitute html entities for
 * html symbols encountered in the original text. Line breaks and whitespaces
 * are preserved.
 * 
 * @since 3.0
 */
public class Java2HTMLEntityReader extends SubstitutionTextReader {

	private static final int BEGIN_LINE = 0x01;

	/** The hardcoded entity map. */
	private static final Map fgEntityLookup;

	/**
	 * True if we have not yet seen a non-whitespace character on the current
	 * line.
	 */
	private int bits = BEGIN_LINE;

	static {
		fgEntityLookup= new HashMap(7);
		fgEntityLookup.put("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("^", "&circ;"); //$NON-NLS-1$ //$NON-NLS-2$
		fgEntityLookup.put("~", "&tilde;"); //$NON-NLS-2$ //$NON-NLS-1$
		fgEntityLookup.put("\"", "&quot;"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Creates a new instance that will read from <code>reader</code>
	 * 
	 * @param reader the source reader
	 */
	public Java2HTMLEntityReader(Reader reader) {
		super(reader);
		setSkipWhitespace(false);
	}

	/*
	 * @see org.eclipse.jdt.internal.ui.text.SubstitutionTextReader#computeSubstitution(int)
	 */
	protected String computeSubstitution(int c) throws IOException {
		/*
		 * When @ is first on a line, translate it to &#064; so it isn't
		 * misinterpreted as a Javadoc tag.
		 * See https://bugs.eclipse.org/bugs/show_bug.cgi?id=197169
		 */
		if (c == '@') {
			return (this.bits & BEGIN_LINE) != 0 ? "&#064;" : null; //$NON-NLS-1$
		}
		if (c == '*') {
			this.bits &= ~BEGIN_LINE;
			int next = nextChar();
			if (next == '/') {
				return "&#42;/"; //$NON-NLS-1$
			}
			if (next == -1) {
				return "*"; //$NON-NLS-1$
			}
			return "*" + (char) next; //$NON-NLS-1$
		}
		if (c == '\n' || c == '\r') {
			this.bits |= BEGIN_LINE;
		} else if (!ScannerHelper.isWhitespace((char) c)) {
			this.bits &= ~BEGIN_LINE;
		}
		return (String) fgEntityLookup.get(String.valueOf((char) c));
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.util;

import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.IN_COMMENT;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.IN_COMMENT_SEEN_STAR;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.IN_DOUBLE_QUOTE;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.IN_SINGLE_LINE_COMMENT;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.IN_SINGLE_QUOTE;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.NORMAL;
import static org.eclipse.jdt.apt.core.internal.util.AnnotationScanner.State.SEEN_SLASH;

import java.io.IOException;

/**
 * Utility scanner for quickly determining if a file contains annotations
 */
public abstract class AnnotationScanner {

	enum State {
		NORMAL,
		SEEN_SLASH,
		IN_COMMENT,
		IN_COMMENT_SEEN_STAR,
		IN_SINGLE_LINE_COMMENT,
		IN_SINGLE_QUOTE,
		IN_DOUBLE_QUOTE
	}

	public AnnotationScanner() {}

	public boolean containsAnnotations() throws IOException {
		State state = NORMAL;

		// for escaping quotes -- need to ignore the next single character
		// Since this applies to all states it's handled separately
		boolean seenBackslash = false;

		int c = getNext();
		while (c != -1) {

			if (seenBackslash) {
				// Skip one character
				seenBackslash = false;
			}
			else if (c == '\\') {
				// Skip the next character
				seenBackslash = true;
			}
			else {
				// Handle the character based on state
				switch (state) {

				case NORMAL :
					if (c == '@')
						return true;
					if (c == '/') {
						state = SEEN_SLASH;
					}
					else if (c == '\'') {
						state = IN_SINGLE_QUOTE;
					}
					else if (c == '\"') {
						state = IN_DOUBLE_QUOTE;
					}
					break;

				case SEEN_SLASH :
					if (c == '*') {
						state = IN_COMMENT;
					}
					else if (c == '/') {
						state = IN_SINGLE_LINE_COMMENT;
					}
					else {
						state = NORMAL;
					}
					break;

				case IN_COMMENT :
					if (c == '*') {
						state = IN_COMMENT_SEEN_STAR;
					}
					break;

				case IN_COMMENT_SEEN_STAR :
					if (c == '/') {
						state = NORMAL;
					}
					else {
						state = IN_COMMENT;
					}
					break;

				case IN_SINGLE_LINE_COMMENT :
					if (c == '\n' || c == '\r') {
						state = NORMAL;
					}
					break;

				case IN_SINGLE_QUOTE :
					if (c == '\'') {
						state = NORMAL;
					}
					break;

				case IN_DOUBLE_QUOTE :
					if (c == '\"') {
						state = NORMAL;
					}
					break;

				default :
					throw new IllegalStateException("Unhandled state: " + state);  //$NON-NLS-1$
				}
			}
			c = getNext();
		}
		return false;
	}

	/**
	 * Returns -1 at the end of the input
	 */
	protected abstract int getNext() throws IOException;
}

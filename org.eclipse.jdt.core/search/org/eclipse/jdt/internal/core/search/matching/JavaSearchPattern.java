/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.search.SearchPattern;


public class JavaSearchPattern extends SearchPattern {
	
	/*
	 * Whether this pattern is case sensitive.
	 */
	boolean isCaseSensitive;
	
	/*
	 * One of R_EXACT_MATCH, R_PREFIX_MATCH, R_PATTERN_MATCH, R_REGEXP_MATCH.
	 */
	int matchMode;
	
	protected JavaSearchPattern(int patternKind, int matchRule) {
		super(matchRule);
		((InternalSearchPattern)this).kind = patternKind;
		this.isCaseSensitive = (matchRule & R_CASE_SENSITIVE) != 0;
		this.matchMode = matchRule - (this.isCaseSensitive ? R_CASE_SENSITIVE : 0);
	}
	
	public SearchPattern getBlankPattern() {
		return null;
	}

	int getMatchMode() {
		return this.matchMode;
	}

	boolean isCaseSensitive () {
		return this.isCaseSensitive;
	}

	/*
	 * Optimization of implementation above (uses cached matchMode and isCaseSenistive)
	 */
	public boolean matchesName(char[] pattern, char[] name) {
		if (pattern == null) return true; // null is as if it was "*"
		if (name != null) {
			switch (this.matchMode) {
				case R_EXACT_MATCH :
					return CharOperation.equals(pattern, name, this.isCaseSensitive);
				case R_PREFIX_MATCH :
					return CharOperation.prefixEquals(pattern, name, this.isCaseSensitive);
				case R_PATTERN_MATCH :
					if (!this.isCaseSensitive)
						pattern = CharOperation.toLowerCase(pattern);
					return CharOperation.match(pattern, name, this.isCaseSensitive);
				case R_REGEXP_MATCH :
					// TODO (jerome) implement regular expression match
					return true;
			}
		}
		return false;
	}
}

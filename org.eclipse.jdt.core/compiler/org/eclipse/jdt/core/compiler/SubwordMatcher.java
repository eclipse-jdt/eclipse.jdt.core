/*******************************************************************************
 * Copyright (c) 2020 Julian Honnen.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Julian Honnen - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.compiler;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;

class SubwordMatcher {

	private static final int[] EMPTY_REGIONS = new int[0];

	private final char[] name;

	public SubwordMatcher(String name) {
		this.name = name.toCharArray();
	}

	public int[] getMatchingRegions(String pattern) {
		int segmentStart = 0;
		int[] segments = EMPTY_REGIONS;

		// Main loop is on pattern characters
		int iName = -1;
		int iPatternWordStart = 0;
		for (int iPattern = 0; iPattern < pattern.length(); iPattern++) {
			iName++;
			if (iName == this.name.length) {
				// We have exhausted the name (and not the pattern), so it's not a match
				return null;
			}

			char patternChar = pattern.charAt(iPattern);
			char nameChar = this.name[iName];

			// For as long as we're exactly matching, bring it on
			if (patternChar == nameChar) {
				continue;
			}

			// not matching, record previous segment and find next word match in name
			if (iName > segmentStart) {
				segments = Arrays.copyOf(segments, segments.length + 2);
				segments[segments.length - 2] = segmentStart;
				segments[segments.length - 1] = iName - segmentStart;
			}

			int wordStart = indexOfWordStart(iName, patternChar);
			if (wordStart < 0) {
				// no matching word found, backtrack and try to find next occurrence of current word
				int next = indexOfWordStart(iName, pattern.charAt(iPatternWordStart));
				if (next > 0) {
					wordStart = next;
					iPattern = iPatternWordStart;
					// last recorded segment was invalid -> drop it
					segments = Arrays.copyOfRange(segments, 0, segments.length - 2);
				}
			}

			if (wordStart < 0) {
				// We have exhausted name (and not pattern), so it's not a match
				return null;
			}

			segmentStart = wordStart;
			iName = wordStart;
			iPatternWordStart = iPattern;
		}

		// we have exhausted pattern, record final segment
		segments = Arrays.copyOf(segments, segments.length + 2);
		segments[segments.length - 2] = segmentStart;
		segments[segments.length - 1] = iName - segmentStart + 1;

		return segments;
	}

	/**
	 * Returns the index of the first word after nameStart, beginning with patternChar. Returns -1 if no matching word
	 * is found.
	 */
	private int indexOfWordStart(int nameStart, char patternChar) {

		char target = ScannerHelper.toUpperCase(patternChar);
		boolean lastWasSeparator = false;

		for (int iName = nameStart; iName < this.name.length; iName++) {
			char nameChar = this.name[iName];
			if (nameChar == target || (lastWasSeparator && nameChar == patternChar)) {
				return iName;
			}

			// don't match across identifiers (e.g. "index" should not match "substring(int beginIndex)")
			if (!ScannerHelper.isJavaIdentifierPart(nameChar)) {
				return -1;
			}

			lastWasSeparator = nameChar == '_';
		}

		// We have exhausted name
		return -1;
	}
}
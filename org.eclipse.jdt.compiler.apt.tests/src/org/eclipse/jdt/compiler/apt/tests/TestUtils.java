/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests;

import java.io.File;

/**
 * @since 3.3
 */
public class TestUtils {

	/**
	 * Convert an array of strings into a path.
	 * E.g., turn { "a", "b", "c.d" } into a File representing "a/b/c.d".
	 */
	public static File concatPath(String... names) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < names.length; ++i) {
			if (i > 0) {
				sb.append(File.separator);
			}
			sb.append(names[i]);
		}
		return new File(sb.toString());
	}

	public static String convertToIndependantLineDelimiter(String source) {
		if (source.indexOf('\n') == -1 && source.indexOf('\r') == -1) return source;
		StringBuffer buffer = new StringBuffer();
		for (int i = 0, length = source.length(); i < length; i++) {
			char car = source.charAt(i);
			if (car == '\r') {
				buffer.append('\n');
				if (i < length-1 && source.charAt(i+1) == '\n') {
					i++; // skip \n after \r
				}
			} else {
				buffer.append(car);
			}
		}
		return buffer.toString();
	}
}

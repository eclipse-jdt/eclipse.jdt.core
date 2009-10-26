/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;

public class ModelTestsUtil {

static private void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
	File[] files = root.listFiles(fileFilter);
	for (int i = 0; i < files.length; i++) {
		final File currentFile = files[i];
		if (currentFile.isDirectory()) {
			collectAllFiles(currentFile, collector, fileFilter);
		} else {
			collector.add(currentFile);
		}
	}
}

public static File[] getAllFiles(File root, FileFilter fileFilter) {
	ArrayList files = new ArrayList();
	if (root.isDirectory()) {
		collectAllFiles(root, files, fileFilter);
		File[] result = new File[files.size()];
		files.toArray(result);
		return result;
	} else {
		return null;
	}
}

/**
 * Remove all white spaces from a string.
 *
 * @param input The input string
 * @return A new string without any whitespaces
 */
public static String removeWhiteSpace(String input) {
	StringTokenizer tokenizer = new StringTokenizer(input);
	StringBuffer buffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {
		buffer.append(tokenizer.nextToken());
	}
    return buffer.toString();
}

/**
 * Remove all white spaces at the deginning of each lines from a string.
 *
 * @param input The input string
 * @return A new string without any whitespaces
 */
public static String trimLinesLeadingWhitespaces(String input) {
	StringTokenizer tokenizer = new StringTokenizer(input, "\r\n\f");
	StringBuffer buffer = new StringBuffer();
	while (tokenizer.hasMoreTokens()) {
		String line = tokenizer.nextToken();
		int length = line.length();
		int size = 0;
		int idx = -1;
		if (length > 0) {
			loop: while ((idx+1) < length) {
				char ch = line.charAt(++idx);
				switch (ch) {
					case '\t':
						size += 4;
						break;
					case '*':
					case ' ':
						break;
					default:
						break loop;
				}
			}
		}
		if (length > 0 && idx > 0 && idx < length) {
			int splitLineIndex = line.indexOf("||", idx);
			if (splitLineIndex > 0) {
				int commentStart = line.indexOf("/*", splitLineIndex);
				if (commentStart >= 80-((size*3)/4)) {
					StringBuffer newLine = new StringBuffer(line.substring(idx-1, splitLineIndex).trim());
					newLine.append('\n');
					newLine.append(line.substring(splitLineIndex).trim());
					newLine.append('\n');
					buffer.append(newLine);
					continue;
				}
			}
			buffer.append(line.substring(idx).trim());
		} else {
			buffer.append(line);
		}
		buffer.append('\n');
	}
    return buffer.toString();
}

}

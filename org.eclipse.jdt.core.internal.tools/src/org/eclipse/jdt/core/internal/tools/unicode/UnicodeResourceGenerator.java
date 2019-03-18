/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.tools.unicode;

import java.io.IOException;

public class UnicodeResourceGenerator {

	double unicodeValue = -1.0;
	String[] args = null;
	boolean generateParts;

	UnicodeResourceGenerator(String[] args, boolean doPart) {
		if (args.length != 3) {
			System.err.println("Usage: " + GenerateIdentifierStartResources.class + " <unicode version> <path to ucd.all.flat.xml> <export directory>"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}

		this.unicodeValue = 0.0;
		try {
			this.unicodeValue = Double.parseDouble(args[0]);
		} catch (NumberFormatException e) {
			System.err.println("<unicode version> has the wrong format. Expecting a double value. e.g. 8.0"); //$NON-NLS-1$
			return;
		}
		this.args = args;
		this.generateParts = doPart;
	}

	public void generate() throws IOException {
		Environment environment = null;
		if (this.generateParts) {
			environment = new PartEnvironment();
		} else {
			environment = new StartEnvironment();
		}
		if (this.args == null) {
			// wrong settings
			return;
		}
		String[] codePointTable = TableBuilder.buildTables(this.unicodeValue, this.generateParts, environment, this.args[1]);
		if (codePointTable == null) {
			System.err.println("Generation failed"); //$NON-NLS-1$
			return;
		}
		Integer[] codePoints = CodePointsBuilder.build(codePointTable, environment);
		if (codePoints == null) {
			System.err.println("Generation failed"); //$NON-NLS-1$
			return;
		}
		FileEncoder.encodeResourceFiles(codePoints, environment, this.args[2]);
	}
}

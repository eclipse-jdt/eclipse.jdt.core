/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;

public class ScannerTest extends AbstractRegressionTest {

	public ScannerTest(String name) {
		super(name);
	}
	public static Test suite() {
		return setupSuite(testClass());
	}

	public static Class testClass() {
		return ScannerTest.class;
	}

	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test001() {
		String sourceA001 =	"\\u003b";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA001.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
		} catch (InvalidInputException e) {
		}
		assertEquals("Wrong token type", ITerminalSymbols.TokenNameSEMICOLON, token);
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test002() {
		String sourceA002 =	"// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(false, true, false, false);
		scanner.setSource(sourceA002.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
		}
	}
	/**
	 * http://bugs.eclipse.org/bugs/show_bug.cgi?id=23181
	 */
	public void test003() {
		String sourceA003 =	"// tests\n  ";
		IScanner scanner = ToolFactory.createScanner(true, true, false, false);
		scanner.setSource(sourceA003.toCharArray());
		int token = 0;
		try {
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameCOMMENT_LINE, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameWHITESPACE, token);
			token = scanner.getNextToken();
			assertEquals("Wrong token type", ITerminalSymbols.TokenNameEOF, token);
		} catch (InvalidInputException e) {
		}
	}						
}
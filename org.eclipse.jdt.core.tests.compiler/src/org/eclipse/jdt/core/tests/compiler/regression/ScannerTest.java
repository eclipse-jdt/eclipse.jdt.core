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
import java.io.*;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFormatException;
import org.eclipse.jdt.internal.compiler.classfmt.MethodInfo;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;

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
}
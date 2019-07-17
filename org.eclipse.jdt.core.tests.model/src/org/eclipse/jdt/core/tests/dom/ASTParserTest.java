/*******************************************************************************
 * Copyright (c) 2004, 2019 IBM Corporation and others.
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

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

@SuppressWarnings("rawtypes")
public class ASTParserTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase {

	/** @deprecated using deprecated code */
	public static Test suite() {
		// TODO (frederic) use buildList + setAstLevel(init) instead...
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTParserTest.class.getName());

		Class c = ASTParserTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTParserTest(methods[i].getName(), AST.JLS2));
				suite.addTest(new ASTParserTest(methods[i].getName(), AST.JLS3));
			}
		}
		return suite;
	}

	AST ast;
	ASTParser parser;
	int API_LEVEL;

	public ASTParserTest(String name, int apiLevel) {
		super(name);
		this.API_LEVEL = apiLevel;
	}

	protected void setUp() throws Exception {
		super.setUp();
		this.ast = AST.newAST(this.API_LEVEL, true);
		this.parser = ASTParser.newParser(this.API_LEVEL);
	}

	protected void tearDown() throws Exception {
		this.ast = null;
		super.tearDown();
	}

	/** @deprecated using deprecated code */
	public String getName() {
		String name = super.getName();
		switch (this.API_LEVEL) {
			case AST.JLS2:
				name = "JLS2 - " + name;
				break;
			case AST.JLS3:
				name = "JLS3 - " + name;
				break;
		}
		return name;
	}

	public void testKConstants() {
		assertSame(ASTParser.K_EXPRESSION, 1);
		assertSame(ASTParser.K_STATEMENTS, 2);
		assertSame(ASTParser.K_CLASS_BODY_DECLARATIONS, 4);
		assertSame(ASTParser.K_COMPILATION_UNIT, 8);
	}

	public void testSetting() {
		// for now, just slam some values in
	    this.parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    this.parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
	    this.parser.setKind(ASTParser.K_EXPRESSION);
	    this.parser.setKind(ASTParser.K_STATEMENTS);

	    this.parser.setSource(new char[0]);
	    this.parser.setSource((char[]) null);
	    this.parser.setSource((ICompilationUnit) null);
	    this.parser.setSource((IClassFile) null);

	    this.parser.setResolveBindings(false);
	    this.parser.setResolveBindings(true);

	    this.parser.setSourceRange(0, -1);
	    this.parser.setSourceRange(0, 1);
	    this.parser.setSourceRange(1, 0);
	    this.parser.setSourceRange(1, -1);

	    this.parser.setWorkingCopyOwner(null);

	    this.parser.setUnitName(null);
	    this.parser.setUnitName("Foo.java"); //$NON-NLS-1$

	    this.parser.setProject(null);

	    this.parser.setFocalPosition(-1);
	    this.parser.setFocalPosition(0);

	    this.parser.setCompilerOptions(null);
	    this.parser.setCompilerOptions(new HashMap<String, String>());
	}
}

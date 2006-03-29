/*******************************************************************************
 * Copyright (c) 2004, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		ast = AST.newAST(this.API_LEVEL);
		parser = ASTParser.newParser(this.API_LEVEL);
	}
	
	protected void tearDown() throws Exception {
		ast = null;
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
		assertTrue(ASTParser.K_EXPRESSION == 1);
		assertTrue(ASTParser.K_STATEMENTS == 2);
		assertTrue(ASTParser.K_CLASS_BODY_DECLARATIONS == 4);
		assertTrue(ASTParser.K_COMPILATION_UNIT == 8);
	}

	public void testSetting() {
		// for now, just slam some values in
	    parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);
	    parser.setKind(ASTParser.K_EXPRESSION);
	    parser.setKind(ASTParser.K_STATEMENTS);
	    
	    parser.setSource(new char[0]);
	    parser.setSource((char[]) null);
	    parser.setSource((ICompilationUnit) null);
	    parser.setSource((IClassFile) null);
	    
	    parser.setResolveBindings(false);
	    parser.setResolveBindings(true);
	    
	    parser.setSourceRange(0, -1);
	    parser.setSourceRange(0, 1);
	    parser.setSourceRange(1, 0);
	    parser.setSourceRange(1, -1);
	    
	    parser.setWorkingCopyOwner(null);

	    parser.setUnitName(null);
	    parser.setUnitName("Foo.java"); //$NON-NLS-1$

	    parser.setProject(null);

	    parser.setFocalPosition(-1);
	    parser.setFocalPosition(0);

	    parser.setCompilerOptions(null);
	    parser.setCompilerOptions(new HashMap());
	}
}

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

package org.eclipse.jdt.core.tests.dom;

import java.lang.reflect.Method;
import java.util.HashMap;

import junit.framework.Test;

import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;

public class ASTParserTest extends org.eclipse.jdt.core.tests.junit.extension.TestCase { 

	public static Test suite() {
		junit.framework.TestSuite suite = new junit.framework.TestSuite(ASTParserTest.class.getName());
		
		Class c = ASTParserTest.class;
		Method[] methods = c.getMethods();
		for (int i = 0, max = methods.length; i < max; i++) {
			if (methods[i].getName().startsWith("test")) { //$NON-NLS-1$
				suite.addTest(new ASTParserTest(methods[i].getName(), AST.LEVEL_2_0));
				suite.addTest(new ASTParserTest(methods[i].getName(), AST.LEVEL_3_0));
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
	
	protected void setUp() {
		if (this.API_LEVEL == AST.LEVEL_2_0) {
			ast = AST.newAST2();
			parser = ASTParser.newParser2();
		}
		if (this.API_LEVEL == AST.LEVEL_3_0) {
			ast = AST.newAST3();
			parser = ASTParser.newParser3();
		}
	}
	
	protected void tearDown() {
		ast = null;
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

	    parser.setPartial(-1);
	    parser.setPartial(0);

	    parser.setCompilerOptions(null);
	    parser.setCompilerOptions(new HashMap());
	}
}

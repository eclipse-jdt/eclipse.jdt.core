/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import junit.framework.Test;

import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.util.ClassFileBytesDisassembler;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class GenericTypeTest extends AbstractRegressionTest {
public GenericTypeTest(String name) {
	super(name);
}

/*
 * Toggle compiler in mode -1.5
 */
protected Map getCompilerOptions() {
	Map options = super.getCompilerOptions();
	options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
	options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);	
	options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);	
	return options;
}
public static Test suite() {
	return setupSuite(testClass());
}
public void _test001() { // TODO reenable once generics are supported
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<Tx1 extends String, Tx2 extends Comparable>  extends XS<Tx2> {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
			"        System.out.println(\"SUCCESS\");\n" + 
			"    }\n" + 
			"}\n" + 
			"\n" + 
			"class XS <Txs> {\n" + 
			"    Txs get(Txs t) {\n" + 
			"        return t;\n" + 
			"    }\n" + 
			"}\n"
		},
		"SUCCESS");
}

public void _test002() { // TODO reenable once generics are supported
	this.runConformTest(
		new String[] {
			"X.java",
			"public class X<Xp1 extends String, Xp2 extends Comparable>  extends XS<Xp2> {\n" + 
			"\n" + 
			"    public static void main(String[] args) {\n" + 
			"        Integer w = new X<String,Integer>().get(new Integer(12));\n" + 
			"        System.out.println(\"SUCCESS\");\n" + 
			"    }\n" + 
			"    Xp2 get(Xp2 t){\n" + 
			"        System.out.print(\"{X::get}\");\n" + 
			"        return super.get(t);\n" + 
			"    }\n" + 
			"}\n" + 
			"\n" + 
			"class XS <XSp1> {\n" + 
			"    XSp1 get(XSp1 t) {\n" + 
			"        System.out.print(\"{XS::get}\");\n" + 
			"        return t;\n" + 
			"    }\n" + 
			"}\n"
		},
		"{XS::get}{X::get}SUCCESS");
}

// check cannot bind superclass to type variable
public void test003() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X <X> extends X {\n" + 
			"}\n", 
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <X> extends X {\n" + 
		"	                           ^\n" + 
		"Superclass X cannot refer to a type variable\n" + 
		"----------\n");
}
		
// check cannot bind superinterface to type variable
public void test004() {
	this.runNegativeTest(
		new String[] {
			"X.java",
			"public class X <X> implements X {\n" + 
			"}\n", 
		},
		"----------\n" + 
		"1. ERROR in X.java (at line 1)\n" + 
		"	public class X <X> implements X {\n" + 
		"	                              ^\n" + 
		"Superinterface X cannot refer to a type variable\n" + 
		"----------\n");
}


public static Class testClass() {
	return GenericTypeTest.class;
}
}

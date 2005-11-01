/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    sbandow@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.mirrortest;

/**
 * Holds information for the mirror tests.
 */
@SuppressWarnings("nls")
public class MirrorUtilTestCodeExample {
	
	public static final String CODE_PACKAGE = "testpackage";
	
	public static final String CODE_CLASS_NAME = "DeclarationsTestClass";

	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;
	
	public static final String CODE = 
		"package testpackage;" + "\n" +
	    "import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorUtilTestAnnotation;" + "\n" +
	    "@MirrorUtilTestAnnotation" + "\n" +
	    "public class DeclarationsTestClass" + "\n" +
	    "{" + "\n" +
		"    public class A" + "\n" +
		"    {" + "\n" +
		"        public int field; " + "\n" +
		"        public void method(){}" + "\n" +
		"    }" + "\n" +
		"    public class B extends A" + "\n" +
		"    {" + "\n" +
		"        public int field; " + "\n" +
		"        public void method(){}" + "\n" +
		"    }" + "\n" +
		"    public class C" + "\n" +
		"    {" + "\n" +
		"        public int field; " + "\n" +
		"        public void method(){}" + "\n" +
		"    }" + "\n" +
		"    public class D extends A" + "\n" +
		"    {" + "\n" +
		"    	public Object field;" + "\n" +
		"    	public void method(String s){}" + "\n" +
		"    }" + "\n" +
		"    public interface I" + "\n" +
		"    {" + "\n" +
		"    	public int field = 1;" + "\n" +
		"       public void method();" + "\n" +
		"    }" + "\n" +
		"    public interface J" + "\n" +
		"    {" + "\n" +
		"    	public int field = 2;" + "\n" +
		"    }" + "\n" +
		"    public class K implements I, J" + "\n" +
		"    {" + "\n" +
		"    	public int field;" + "\n" +
		"       public void method(){}" + "\n" +
		"    }" + "\n" +
	    "}";
	
}

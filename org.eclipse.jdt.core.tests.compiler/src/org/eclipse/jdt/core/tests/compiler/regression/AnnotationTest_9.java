/*******************************************************************************
 * Copyright (c) 2000, 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

@SuppressWarnings({ "rawtypes" })
public class AnnotationTest_9 extends AbstractComparableTest {

    public AnnotationTest_9(String name) {
        super(name);
    }

    public static Test suite() {
        return buildMinimalComplianceTestSuite(testClass(), F_9);
    }

    public static Class testClass() {
        return AnnotationTest_9.class;
    }

    public void testBug532913() throws Exception {
	    this.complianceLevel = ClassFileConstants.JDK9;
	    if (this.complianceLevel < ClassFileConstants.JDK9) {
	        return;
	    }
	
	    runConformTest(
	        new String[] {
	                "p/A.java",
	                "package p;\n" +
	                "@java.lang.annotation.Target({\n" + 
	                "    java.lang.annotation.ElementType.TYPE, java.lang.annotation.ElementType.METHOD})\n" + 
	                "@Deprecated\n" + 
	                "public @interface A {}\n",
	        },"");
	    runConformTest(
            new String[] {
                    "X.java",
                    "import p.A;\n" +
                    "class X {\n" + 
                    "  @A void foo() {}\n" + 
                    "}\n",
            },"", null, false, null);
	}
}

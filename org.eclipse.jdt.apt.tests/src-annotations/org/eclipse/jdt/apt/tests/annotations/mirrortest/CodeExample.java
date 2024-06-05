/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/


package org.eclipse.jdt.apt.tests.annotations.mirrortest;

/**
 * Holds information for the mirror tests.
 */
public class CodeExample {

	public static final String CODE_PACKAGE = "mirrortestpackage";

	public static final String CODE_CLASS_NAME = "MirrorTestClass";

	public static final String CODE_FULL_NAME = CODE_PACKAGE + "." + CODE_CLASS_NAME;

	public static final String CODE =
		"""
		package mirrortestpackage;
		
		import java.io.Serializable;
		import org.eclipse.jdt.apt.tests.annotations.mirrortest.MirrorTestAnnotation;
		
		public class MirrorTestClass implements Serializable {
		
		    public static final String STATIC_FIELD = "Static Field";
		
		    private static final long serialVersionUID = 42L;
		
		    public String field;
		
		    public MirrorTestClass() {
		        field = "Field";
		    }
		
		    @MirrorTestAnnotation
		    public static Object staticMethod() {
		        return null;
		    }
		
		    public String stringMethod() {
		        return null;
		    }
		
		
		    public String toString() {
		        return null;
		    }
		
		    public static class InnerClass extends MirrorTestClass {
		
		        private static final long serialVersionUID = 148L;
		
		        public static Object staticMethod() {
		            return null;
		        }
		
		    }
		}""";
}

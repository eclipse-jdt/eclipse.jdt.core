/*******************************************************************************
 * Copyright (c) 2020, 2021 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import junit.framework.Test;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.lookup.ExtraCompilerModifiers;

@SuppressWarnings({ "rawtypes" })
public class ClassFileReaderTest_17 extends AbstractRegressionTest {
	static {
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_17);
	}
	public static Class testClass() {
		return ClassFileReaderTest_17.class;
	}

	public ClassFileReaderTest_17(String name) {
		super(name);
	}

	// Needed to run tests individually from JUnit
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.complianceLevel = ClassFileConstants.JDK17;
	}

	public void testBug564227_001() throws Exception {
		String source =
				"""
			sealed class X permits Y, Z{
			  public static void main(String[] args){
			     System.out.println(0);
			  }
			}
			final class Y extends X{}
			final class Z extends X{}
			""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(2, permittedSubtypesNames.length);

		char [][] expected = {"Y".toCharArray(), "Z".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

	}
	public void testBug565782_001() throws Exception {
		String source =
				"""
			sealed interface I {}
			enum X implements I {
			    ONE {};
			    public static void main(String[] args) {
			        System.out.println(0);
			   }
			}""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
	public void testBug565782_002() throws Exception {
		String source =
				"""
			sealed interface I {}
			class X {
				enum E implements I {
			   	ONE {};
				}
			   public static void main(String[] args) {
			      	System.out.println(0);
			   }
			}""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X.E", "X$E", source);
		char[][] permittedSubtypesNames = classFileReader.getPermittedSubtypeNames();

		assertEquals(1, permittedSubtypesNames.length);

		char [][] expected = {"X$E$1".toCharArray()};
		assertTrue(CharOperation.equals(permittedSubtypesNames, expected));

		int modifiers = classFileReader.getModifiers();
		assertTrue("sealed modifier expected", (modifiers & ExtraCompilerModifiers.AccSealed) != 0);
	}
	public void testBug545510_1() throws Exception {
		String source =
				"strictfp class X {\n"+
				"}";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);

		int modifiers = classFileReader.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testBug545510_2() throws Exception {
		String source =
				"""
			class X {
			  strictfp void foo() {}
			}""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[1];
		int modifiers = method.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testBug545510_3() throws Exception {
		String source =
				"""
			strictfp class X {
			  void foo() {}
			}""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[1];
		int modifiers = method.getModifiers();
		assertTrue("strictfp modifier not expected", (modifiers & ClassFileConstants.AccStrictfp) == 0);
	}
	public void testWildcardBinding() throws Exception {
		String source =
				"""
			public class X {   \s
			    public static void main(String[] args) {
					getHasValue().addValueChangeListener(evt -> {System.out.println("hello");});	\t
			    }
			    public static HasValue<?, ?> getHasValue() {\s
			        return new HasValue<HasValue.ValueChangeEvent<String>, String>() {\s
						@Override
						public void addValueChangeListener(
								HasValue.ValueChangeListener<? super HasValue.ValueChangeEvent<String>> listener) {
							listener.valueChanged(null);
						}
					};
			    }   \s
			}
			
			interface HasValue<E extends HasValue.ValueChangeEvent<V>,V> {   \s
			    public static interface ValueChangeEvent<V> {}   \s
			    public static interface ValueChangeListener<E extends HasValue.ValueChangeEvent<?>> {
			        void valueChanged(E event);
			    }   \s
			    void addValueChangeListener(HasValue.ValueChangeListener<? super E> listener);
			}
			""";

		org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader classFileReader = getInternalClassFile("", "X", "X", source);
		IBinaryMethod[] methods = classFileReader.getMethods();
		IBinaryMethod method = methods[3];
		String name = new String(method.getSelector());
		assertTrue("invalid name", "lambda$0".equals(name));
		String descriptor = new String(method.getMethodDescriptor());
		assertTrue("invalid descriptor", "(LHasValue$ValueChangeEvent;)V".equals(descriptor));
	}
}

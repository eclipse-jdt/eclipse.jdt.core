/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8) // Not at compliance 9 yet
public class Java9ElementProcessor extends Java8ElementProcessor {

	int roundNo = 0;
	boolean reportSuccessAlready = true;

	public void testBug521723() {
		//		private int foo1(int i) { return i; }
		//		default int foo2(int i) {return foo(i); }
		//		public default void foo3() {}
		//		static void foo4() {}
		//		private static void foo5() {}
		//		public static void foo6() {}
		Modifier[] f1 = new Modifier[] {Modifier.PRIVATE};
		Modifier[] f2 = new Modifier[] {Modifier.PUBLIC, Modifier.DEFAULT};
		Modifier[] f3 = f2;
		Modifier[] f4 = new Modifier[] {Modifier.STATIC, Modifier.PUBLIC};
		Modifier[] f5 = new Modifier[] {Modifier.PRIVATE, Modifier.STATIC};
		Modifier[] f6 = f4;
		Set<? extends Element> rootElements = roundEnv.getRootElements();
		TypeElement t = null;
		for (Element element : rootElements) {
			if (element instanceof TypeElement) {
				if (((TypeElement) element).getQualifiedName().toString().equals("targets.bug521723.I")) {
					t = (TypeElement) element;
				}
			}
		}
		assertNotNull("type should not be null", t);
		List<? extends Element> enclosedElements = t.getEnclosedElements();
		for (Element element : enclosedElements) {
			if (element instanceof ExecutableElement) {
				String string = element.getSimpleName().toString();
				if (string.equals("foo1")) {
					validateModifiers((ExecutableElement) element, f1);
				} else if (string.equals("foo2")) {
					validateModifiers((ExecutableElement) element, f2);
				} else if (string.equals("foo3")) {
					validateModifiers((ExecutableElement) element, f3);
				} else if (string.equals("foo4")) {
					validateModifiers((ExecutableElement) element, f4);
				} else if (string.equals("foo5")) {
					validateModifiers((ExecutableElement) element, f5);
				} else if (string.equals("foo6")) {
					validateModifiers((ExecutableElement) element, f6);
				}
			}
		}
		
	}
	private void validateModifiers(ExecutableElement method, Modifier[] expected) {
		Set<Modifier> modifiers = method.getModifiers();
		List<Modifier> list = new ArrayList<>(modifiers);
		for (Modifier modifier : expected) {
			list.remove(modifier);
		}
		assertTrue("modifiers still present: " + list.toString(), list.isEmpty());
	}
}

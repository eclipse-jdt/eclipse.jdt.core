/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes("*")
public class SealedTypeElementProcessor extends BaseElementProcessor {
	TypeElement nonSealed = null;
	TypeElement sealed1 = null;
	Modifier sealed = null;
	Modifier non_Sealed = null;
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}
	public void testAll() throws AssertionFailedError, IOException {
		test001();
		test002();
	}
	private void fetchElements() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		for (Element element : elements) {
			if ("SealedExample".equals(element.getSimpleName().toString())) {
				nonSealed = (TypeElement) element;
			} else if ("SealedI1".equals(element.getSimpleName().toString())) {
				sealed1 = (TypeElement) element;
			}
		}
		try {
			sealed = Modifier.valueOf("SEALED");
			non_Sealed = Modifier.valueOf("NON_SEALED");
		} catch(IllegalArgumentException iae) {
		}
	}
	// Test simple sealed and non-sealed type modifier
	public void test001() {
		fetchElements();
		if (sealed == null || non_Sealed == null) {
			// this will never be the case if the test is run with JRE 15
			return;
		}
		assertNotNull("TypeElement for non sealed type should not be null", nonSealed);
		assertNotNull("TypeElement for sealed type should not be null", sealed1);
		Set<Modifier> modifiers = nonSealed.getModifiers();
		assertTrue("should contain modifier \'non-sealed\'", modifiers.contains(non_Sealed));
		assertFalse("should not contain modifier \'sealed\'", modifiers.contains(sealed));
		modifiers = sealed1.getModifiers();
		assertTrue("should contain modifier \'sealed\'", modifiers.contains(sealed));
		assertFalse("should not contain modifier \'non-sealed\'", modifiers.contains(non_Sealed));
	}
	// Test getPermittedSubclasses()
	public void test002() {
		fetchElements();
		// The collection returned by asList() doesn't support remove(), hence wrap it in another collection.
		List<String> list = new ArrayList<String>(Arrays.asList("sealed.SealedExample", "sealed.NonSealed1"));
		assertNotNull("TypeElement for non sealed type should not be null", nonSealed);
		assertNotNull("TypeElement for sealed type should not be null", sealed1);
		List<? extends TypeMirror> permittedSubclasses = sealed1.getPermittedSubclasses();
		assertEquals("incorrect no of permitted classes", 2, permittedSubclasses.size());
		for (TypeMirror typeMirror : permittedSubclasses) {
			list.remove(typeMirror.toString());
		}
		assertEquals("missing permitted classes", 0, list.size());
	}
}

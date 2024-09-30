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
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;

@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SealedTypeElementProcessor extends BaseElementProcessor {
	TypeElement nonSealed = null;
	TypeElement sealed1 = null;
	Modifier sealed = null;
	Modifier non_Sealed = null;
	Modifier modifierStatic = null;
	Modifier modifierFinal = null;
	PackageElement topPkg = null;
	PackageElement topPkg2 = null;
	PackageElement topPkg3 = null;
	PackageElement topPkg4 = null;
	@Override
	public synchronized void init(ProcessingEnvironment processingEnv) {
		super.init(processingEnv);
	}
	public void testAll() throws AssertionFailedError, IOException {
		test001();
		test002();
		test003();
		test004();
		test005Src();
		test005Binary();
		test006();
	}
	private void fetchElements() {
		Set<? extends Element> elements = roundEnv.getRootElements();
		for (Element element : elements) {
			if ("SealedExample".equals(element.getSimpleName().toString())) {
				nonSealed = (TypeElement) element;
			} else if ("SealedI1".equals(element.getSimpleName().toString())) {
				sealed1 = (TypeElement) element;
			} else if ("TopMain".equals(element.getSimpleName().toString())) {
				topPkg = (PackageElement) (element.getEnclosingElement());
			} else if ("TopMain2".equals(element.getSimpleName().toString())) {
				topPkg2 = (PackageElement) (element.getEnclosingElement());
			} else if ("TopMain3".equals(element.getSimpleName().toString())) {
				topPkg3 = (PackageElement) (element.getEnclosingElement());
			} else if ("TopMain4".equals(element.getSimpleName().toString())) {
				topPkg4 = (PackageElement) (element.getEnclosingElement());
			}
		}
		try {
			sealed = Modifier.valueOf("SEALED");
			non_Sealed = Modifier.valueOf("NON_SEALED");
			modifierStatic = Modifier.valueOf("STATIC");
			modifierFinal = Modifier.valueOf("FINAL");
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
		if (!isBinaryMode)
			assertTrue("should contain modifier \'non-sealed\'", modifiers.contains(non_Sealed));
		assertFalse("should not contain modifier \'sealed\'", modifiers.contains(sealed));
		modifiers = sealed1.getModifiers();
		assertTrue("should contain modifier \'sealed\'", modifiers.contains(sealed));
		if (!isBinaryMode)
			assertFalse("should not contain modifier \'non-sealed\'", modifiers.contains(non_Sealed));
	}
	// Test getPermittedSubclasses()
	public void test002() {
		fetchElements();
		// The collection returned by asList() doesn't support remove(), hence wrap it in another collection.
		List<String> list = new ArrayList<>(Arrays.asList("sealed.SealedExample", "sealed.NonSealed1"));
		assertNotNull("TypeElement for non sealed type should not be null", nonSealed);
		assertNotNull("TypeElement for sealed type should not be null", sealed1);
		List<? extends TypeMirror> permittedSubclasses = sealed1.getPermittedSubclasses();
		assertEquals("incorrect no of permitted classes", 2, permittedSubclasses.size());
		for (TypeMirror typeMirror : permittedSubclasses) {
			list.remove(typeMirror.toString());
		}
		assertEquals("missing permitted classes", 0, list.size());
	}
	public void test003() {
		fetchElements();
		assertNotNull("package null", topPkg);
		List<? extends Element> enclosedElements = topPkg.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 3, enclosedElements.size());
		TypeElement sealedType = null;
		for (Element element : enclosedElements) {
			if (element instanceof TypeElement) {
				TypeElement temp = (TypeElement) element;
				if (temp.getQualifiedName().toString().equals("sealed.sub.TopSecond")) {
					sealedType = (TypeElement) element;
					break;
				}
			}
		}
		assertNotNull("type should not be null", sealedType);
		List<? extends TypeMirror> permittedSubclasses = sealedType.getPermittedSubclasses();
		assertEquals("incorrect no of permitted types", 2, permittedSubclasses.size());
		for (TypeMirror typeMirror : permittedSubclasses) {
			TypeElement typeEl = (TypeElement) _typeUtils.asElement(typeMirror);
			if (typeEl.getQualifiedName().toString().equals("sealed.sub.TopThird")) {
				Set<Modifier> modifiers = typeEl.getModifiers();
				assertTrue("should contain modifier final", modifiers.contains(modifierFinal));
				assertFalse("should not contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
				TypeMirror superclass = typeEl.getSuperclass();
				assertFalse("should be a valid type", (superclass instanceof NoType));
				TypeElement temp = (TypeElement) _typeUtils.asElement(superclass);
				assertNotNull("type element should not be null", temp);
				assertEquals("incorrect super class", "sealed.sub.TopSecond", temp.getQualifiedName().toString());
				modifiers = temp.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
				enclosedElements = temp.getEnclosedElements();
				assertEquals("incorrect no of enclosed elements", 1, enclosedElements.size());
				Element element = enclosedElements.get(0);
				assertEquals("should be a constructor", ElementKind.CONSTRUCTOR, element.getKind());
				ExecutableElement method = (ExecutableElement) element;
				assertEquals("incorrect constructor name", "<init>", method.getSimpleName().toString());
				continue;
			}
			if (typeEl.getQualifiedName().toString().equals("sealed.sub.TopThird.NonSealedStaticNested")) {
				Set<Modifier> modifiers = typeEl.getModifiers();
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				if (isBinaryMode)
					assertFalse("should not contain modifier non-sealed in binary", modifiers.contains(non_Sealed));
				else
					assertTrue("should contain modifier non-sealed in source mode", modifiers.contains(non_Sealed));
				assertFalse("should not contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				TypeMirror superclass = typeEl.getSuperclass();
				assertFalse("should be a valid type", (superclass instanceof NoType));
				TypeElement temp = (TypeElement) _typeUtils.asElement(superclass);
				assertNotNull("type element should not be null", temp);
				assertEquals("incorrect super class", "sealed.sub.TopSecond", temp.getQualifiedName().toString());
				modifiers = temp.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
				enclosedElements = temp.getEnclosedElements();
				assertEquals("incorrect no of enclosed elements", 1, enclosedElements.size());
				Element element = enclosedElements.get(0);
				assertEquals("should be a constructor", ElementKind.CONSTRUCTOR, element.getKind());
				ExecutableElement method = (ExecutableElement) element;
				assertEquals("incorrect constructor name", "<init>", method.getSimpleName().toString());
				continue;
			}
		}
	}
	public void test004() {
		fetchElements();
		assertNotNull("package null", topPkg2);
		List<? extends Element> enclosedElements = topPkg2.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 1, enclosedElements.size());
		TypeElement topType = null;
		for (Element element : enclosedElements) {
			if (element instanceof TypeElement) {
				TypeElement temp = (TypeElement) element;
				if (temp.getQualifiedName().toString().equals("sealed.sub2.TopMain2")) {
					topType = (TypeElement) element;
					break;
				}
			}
		}
		assertNotNull("type should not be null", topType);
		enclosedElements = topType.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 3, enclosedElements.size());
		
		for (Element element : enclosedElements) {
			if (!(element instanceof TypeElement))
				continue;
			TypeElement typeEl = (TypeElement) element;
			if (typeEl.getQualifiedName().toString().equals("sealed.sub2.TopMain2.SealedIntf")) {
				Set<Modifier> modifiers = typeEl.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
				TypeMirror superclass = typeEl.getSuperclass();
				assertTrue("should be a NoType", (superclass instanceof NoType));
				List<? extends TypeMirror> permittedSubclasses = typeEl.getPermittedSubclasses();
				assertEquals("incorrect no of permitted types", 1, permittedSubclasses.size());
				for (TypeMirror typeMirror : permittedSubclasses) {
					TypeElement permittedTypeEl = (TypeElement) _typeUtils.asElement(typeMirror);
					if (permittedTypeEl.getQualifiedName().toString().equals("sealed.sub2.TopMain2.MyRecord")) {
						superclass = permittedTypeEl.getSuperclass();
						modifiers = permittedTypeEl.getModifiers();
						assertTrue("record should contain modifier final", modifiers.contains(modifierFinal));
						assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
						assertFalse("should not contain modifier sealed", modifiers.contains(sealed));
						assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
						assertFalse("should not be a NoType", (superclass instanceof NoType));
						TypeElement superClassElement = (TypeElement) _typeUtils.asElement(superclass);
						assertNotNull("type element should not be null", superClassElement);
						assertEquals("incorrect super class", "java.lang.Record", superClassElement.getQualifiedName().toString());
						List<? extends TypeMirror> interfaces = permittedTypeEl.getInterfaces();
						assertEquals("incorrect no of super interfaces", 1, interfaces.size());
						TypeElement superInterfaceElement = (TypeElement) _typeUtils.asElement(interfaces.get(0));
						assertEquals("incorrect super interface", "sealed.sub2.TopMain2.SealedIntf", superInterfaceElement.getQualifiedName().toString());
						modifiers = superInterfaceElement.getModifiers();
						assertTrue("should contain modifier sealed", modifiers.contains(sealed));
						assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
						assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
						assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
					}
				}
				break;
			}
		}
	}
	public void _test005() {
		fetchElements();
		assertNotNull("package null", topPkg3);
		List<? extends Element> enclosedElements = topPkg3.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 1, enclosedElements.size());
		TypeElement topType = null;
		for (Element element : enclosedElements) {
			if (element instanceof TypeElement) {
				TypeElement temp = (TypeElement) element;
				if (temp.getQualifiedName().toString().equals("sealed.sub3.TopMain3")) {
					topType = (TypeElement) element;
					break;
				}
			}
		}
		assertNotNull("type should not be null", topType);
		enclosedElements = topType.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 4, enclosedElements.size());
		TypeElement sealedIntf = null;
		TypeElement enumElement = null;
		for (Element element : enclosedElements) {
			if (!(element instanceof TypeElement))
				continue;
			TypeElement typeEl = (TypeElement) element;
			Set<Modifier> modifiers = null;
			if (typeEl.getQualifiedName().toString().equals("sealed.sub3.TopMain3.SealedIntf")) {
				sealedIntf = typeEl;
				modifiers = typeEl.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
			} else if (typeEl.getQualifiedName().toString().equals("sealed.sub3.TopMain3.MyEnum")) {
				enumElement = typeEl;
				modifiers = typeEl.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertFalse("enum should not contain modifier final", modifiers.contains(modifierFinal));
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
			}
		}
		assertNotNull("type element should not null", sealedIntf);
		assertNotNull("type element should not null", enumElement);
	}
	public void test005Src() {
		if (this.isBinaryMode) return;
		_test005();
	}	
	public void test005Binary() {
		if (!this.isBinaryMode) return;
		_test005();
	}
	public void test006() {
		fetchElements();
		assertNotNull("package null", topPkg4);
		List<? extends Element> enclosedElements = topPkg4.getEnclosedElements();
		assertEquals("incorrect no of enclosed elements", 2, enclosedElements.size());
		TypeElement topInterface = null;
		for (Element element : enclosedElements) {
			if (element instanceof TypeElement) {
				TypeElement temp = (TypeElement) element;
				if (temp.getQualifiedName().toString().equals("sealed.sub4.TopMain4Test")) {
					topInterface = (TypeElement) element;
				}
			}
		}
		Set<Modifier> modifiers = topInterface.getModifiers();
		assertTrue("should contain modifier sealed", modifiers.contains(sealed));
		assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
		assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
		TypeMirror superclass = topInterface.getSuperclass();
		assertTrue("should be a NoType", (superclass instanceof NoType));
		List<? extends TypeMirror> permittedSubclasses = topInterface.getPermittedSubclasses();
		assertEquals("incorrect no of permitted types", 1, permittedSubclasses.size());
		for (TypeMirror typeMirror : permittedSubclasses) {
			TypeElement permittedTypeEl = (TypeElement) _typeUtils.asElement(typeMirror);
			if (permittedTypeEl.getQualifiedName().toString().equals("sealed.sub2.TopMain2.MyRecord")) {
				superclass = permittedTypeEl.getSuperclass();
				modifiers = permittedTypeEl.getModifiers();
				assertTrue("record should contain modifier final", modifiers.contains(modifierFinal));
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier sealed", modifiers.contains(sealed));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
				assertFalse("should not be a NoType", (superclass instanceof NoType));
				TypeElement superClassElement = (TypeElement) _typeUtils.asElement(superclass);
				assertNotNull("type element should not be null", superClassElement);
				assertEquals("incorrect super class", "java.lang.Record", superClassElement.getQualifiedName().toString());
				List<? extends TypeMirror> interfaces = permittedTypeEl.getInterfaces();
				assertEquals("incorrect no of super interfaces", 1, interfaces.size());
				TypeElement superInterfaceElement = (TypeElement) _typeUtils.asElement(interfaces.get(0));
				assertEquals("incorrect super interface", "sealed.sub2.TopMain2.SealedIntf", superInterfaceElement.getQualifiedName().toString());
				modifiers = superInterfaceElement.getModifiers();
				assertTrue("should contain modifier sealed", modifiers.contains(sealed));
				assertTrue("should contain modifier static", modifiers.contains(modifierStatic));
				assertFalse("should not contain modifier final", modifiers.contains(modifierFinal));
				assertFalse("should not contain modifier non-sealed", modifiers.contains(non_Sealed));
			}
		}
	}
}

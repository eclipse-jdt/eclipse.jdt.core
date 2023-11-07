/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.inherited;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that tests the dispatch functionality in the presence of annotations that are
 * @Inherited.  To enable this processor, add
 * -Aorg.eclipse.jdt.compiler.apt.tests.processors.inherited.InheritedAnnoProc to the command line.
 *
 * @since 3.3
 */
@SupportedAnnotationTypes("*")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
@SupportedOptions("org.eclipse.jdt.compiler.apt.tests.processors.inherited.InheritedAnnoProc")
public class InheritedAnnoProc extends BaseProcessor
{
	// Initialized in collectElements:
	private TypeElement _inheritedAnno;
	private TypeElement _notInheritedAnno;
	private TypeElement _elementA;
	private TypeElement _elementAChild;
	private TypeElement _elementANotAnnotated;
	private TypeElement _elementAIntf;
	private TypeElement _elementAEnum;
	private Element _elementAi;
	private Element _elementAfoo;
	private Element _elementAinit; // c'tor with no param
	private Element _elementAinitI; // c'tor with int param
	private Element _elementAa; // method with c'tor-like name

	private TypeElement _elementB;
	private TypeElement _elementBChild;
	private TypeElement _elementBNotAnnotated;
	private Element _elementBfoo;
	private Element _elementBi;

	// Always return false from this processor, because it supports "*".
	// The return value does not signify success or failure!
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
	{
		if (roundEnv.processingOver()) {
			return false;
		}
		Map<String, String> options = processingEnv.getOptions();
		if (!options.containsKey(this.getClass().getName())) {
			// Disable this processor unless we are intentionally performing the test.
			return false;
		}

		if (!collectElements()) {
			return false;
		}

		if (!examineGetRootElements(roundEnv)) {
			return false;
		}

		if (!examineGetElementsAnnotatedWith(roundEnv)) {
			return false;
		}

		reportSuccess();
		return false;
	}

	private boolean collectElements() {
		_inheritedAnno = _elementUtils.getTypeElement("org.eclipse.jdt.compiler.apt.tests.annotations.InheritedAnno");
		_notInheritedAnno = _elementUtils.getTypeElement("NotInheritedAnno");
		if (null == _inheritedAnno || null == _notInheritedAnno) {
			reportError("collectElements: Couldn't load annotation type");
			return false;
		}

		_elementA = _elementUtils.getTypeElement("InheritanceA");
		for (Element e : _elementA.getEnclosedElements()) {
			String name = e.getSimpleName().toString();
			if ("AChild".equals(name)) {
				_elementAChild = (TypeElement)e;
			}
			else if ("ANotAnnotated".equals(name)) {
				_elementANotAnnotated = (TypeElement)e;
			}
			else if ("AIntf".equals(name)) {
				_elementAIntf = (TypeElement)e;
			}
			else if ("AEnum".equals(name)) {
				_elementAEnum = (TypeElement)e;
			}
			else if ("i".equals(name)) {
				_elementAi = e;
			}
			else if ("foo".equals(name)) {
				_elementAfoo = e;
			}
			else if ("InheritanceA".equals(name)) {
				_elementAa = e;
			}
			else if ("<init>".equals(name)) {
				if (((ExecutableElement)e).getParameters().isEmpty()) {
					_elementAinit = e;
				}
				else {
					_elementAinitI = e;
				}
			}
		}
		if (null == _elementA || null == _elementAChild || null == _elementANotAnnotated ||
				null == _elementAi || null == _elementAfoo || null == _elementAinitI || null == _elementAa ||
				null == _elementAIntf || null == _elementAEnum || null == _elementAinit) {
			reportError("collectElements: couldn't load elements from InheritanceA");
			return false;
		}

		_elementB = _elementUtils.getTypeElement("InheritanceB");
		for (Element e : _elementB.getEnclosedElements()) {
			String name = e.getSimpleName().toString();
			if ("BChild".equals(name)) {
				_elementBChild = (TypeElement)e;
			}
			else if ("BNotAnnotated".equals(name)) {
				_elementBNotAnnotated = (TypeElement)e;
			}
			else if ("i".equals(name)) {
				_elementBi = e;
			}
			else if ("foo".equals(name)) {
				_elementBfoo = e;
			}
		}
		if (null == _elementB || null == _elementBChild || null == _elementBNotAnnotated ||
				null == _elementBi || null == _elementBfoo) {
			reportError("collectElements: couldn't load elements from InheritanceB");
			return false;
		}

		return true;
	}

	/**
	 * Test the getRootElements implementation.
	 * @return true if tests passed
	 */
	private boolean examineGetRootElements(RoundEnvironment roundEnv)
	{
		// Expect to see all elements (unaffected by presence of @Inherited)
		final Element[] expected = {_notInheritedAnno, _elementA, _elementB};

		Set<? extends Element> elements = new HashSet<Element>(roundEnv.getRootElements());
		for (Element element : expected) {
			if (!elements.remove(element)) {
				reportError("examineRootElements: root elements did not contain expected element " + element.getSimpleName());
				return false;
			}
		}
		if (!elements.isEmpty()) {
			reportError("examineRootElements: root elements contained unexpected elements " + elements);
			return false;
		}
		return true;
	}

	private boolean examineGetElementsAnnotatedWith(RoundEnvironment roundEnv)
	{
		// Elements we expect to get from getElementsAnnotatedWith(@InheritedAnno)
		final Element[] expectedInherited =
				{ _elementA, _elementAChild, _elementAIntf, _elementAEnum,
				_elementAi, _elementAfoo, _elementAinit, _elementAinitI, _elementAa,
				_elementB, _elementBChild };

		Set<? extends Element> actualInherited = new HashSet<Element>(roundEnv.getElementsAnnotatedWith(_inheritedAnno));
		for (Element element : expectedInherited) {
			if (!actualInherited.remove(element)) {
				reportError("examineGetElementsAnnotatedWith(@InheritedAnno): did not contain expected element " + element.getSimpleName());
				return false;
			}
		}
		if (!actualInherited.isEmpty()) {
			reportError("examineGetElementsAnnotatedWith(@InheritedAnno): contained unexpected elements " + actualInherited);
			return false;
		}

		// Elements we expect to get from getElementsAnnotatedWith(@NotInheritedAnno)
		final Element[] expectedNotInherited =
				{ _elementA, _elementAChild, _elementAIntf, _elementAEnum,
				_elementAi, _elementAfoo, _elementAinit, _elementAinitI, _elementAa };

		Set<? extends Element> actualNotInherited = new HashSet<Element>(roundEnv.getElementsAnnotatedWith(_notInheritedAnno));
		for (Element element : expectedNotInherited) {
			if (!actualNotInherited.remove(element)) {
				reportError("examineGetElementsAnnotatedWith(@NotInheritedAnno): did not contain expected element " + element.getSimpleName());
				return false;
			}
		}
		if (!actualNotInherited.isEmpty()) {
			reportError("examineGetElementsAnnotatedWith(@NotInheritedAnno): contained unexpected elements " + actualNotInherited);
			return false;
		}

		return true;
	}

}

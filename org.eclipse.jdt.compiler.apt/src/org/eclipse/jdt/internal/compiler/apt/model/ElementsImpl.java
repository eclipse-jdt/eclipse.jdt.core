/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;

/**
 * Utilities for working with language elements.
 * There is one of these for every ProcessingEnvironment.
 */
public class ElementsImpl implements Elements {
	
	private final BaseProcessingEnvImpl _env;
	
	/*
	 * The processing env creates and caches an ElementsImpl.  Other clients should
	 * not create their own; they should ask the env for it.
	 */
	public ElementsImpl(BaseProcessingEnvImpl env) {
		_env = env;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getAllAnnotationMirrors(javax.lang.model.element.Element)
	 */
	@Override
	public List<? extends AnnotationMirror> getAllAnnotationMirrors(Element e) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getAllMembers(javax.lang.model.element.TypeElement)
	 */
	@Override
	public List<? extends Element> getAllMembers(TypeElement type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getBinaryName(javax.lang.model.element.TypeElement)
	 */
	@Override
	public Name getBinaryName(TypeElement type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getConstantExpression(java.lang.Object)
	 */
	@Override
	public String getConstantExpression(Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getDocComment(javax.lang.model.element.Element)
	 */
	@Override
	public String getDocComment(Element e) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getElementValuesWithDefaults(javax.lang.model.element.AnnotationMirror)
	 */
	@Override
	public Map<? extends ExecutableElement, ? extends AnnotationValue> getElementValuesWithDefaults(
			AnnotationMirror a) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getName(java.lang.CharSequence)
	 */
	@Override
	public Name getName(CharSequence cs) {
		return new NameImpl(cs);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getPackageElement(java.lang.CharSequence)
	 */
	@Override
	public PackageElement getPackageElement(CharSequence name) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getPackageOf(javax.lang.model.element.Element)
	 */
	@Override
	public PackageElement getPackageOf(Element type) {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#getTypeElement(java.lang.CharSequence)
	 */
	@Override
	public TypeElement getTypeElement(CharSequence name) {
		LookupEnvironment le = _env.getLookupEnvironment();
		//TODO: do this the right way - this is a hack to test if it works
		String qname = name.toString();
		String parts[] = qname.split("\\."); //$NON-NLS-1$
		int length = parts.length;
		char[][] compoundName = new char[length][];
		for (int i = 0; i < length; i++) {
			compoundName[i] = parts[i].toCharArray();
		}
		ReferenceBinding binding = le.getType(compoundName);
		if (binding == null) {
			return null;
		}
		return new TypeElementImpl(binding);
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#hides(javax.lang.model.element.Element, javax.lang.model.element.Element)
	 */
	@Override
	public boolean hides(Element hider, Element hidden) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#isDeprecated(javax.lang.model.element.Element)
	 */
	@Override
	public boolean isDeprecated(Element e) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#overrides(javax.lang.model.element.ExecutableElement, javax.lang.model.element.ExecutableElement, javax.lang.model.element.TypeElement)
	 */
	@Override
	public boolean overrides(ExecutableElement overrider, ExecutableElement overridden,
			TypeElement type) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.lang.model.util.Elements#printElements(java.io.Writer, javax.lang.model.element.Element[])
	 */
	@Override
	public void printElements(Writer w, Element... elements) {
		// TODO Auto-generated method stub

	}

}

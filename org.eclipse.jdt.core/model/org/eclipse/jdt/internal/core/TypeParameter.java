/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ITypeParameter;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;

public class TypeParameter extends SourceRefElement implements ITypeParameter {

	public static boolean ENABLED = false;
	
	protected String name;
	
	public TypeParameter(JavaElement parent, String name) {
		super(parent);
		this.name = name;
	}
	
	/*
	 * Returns the elements that are not type parameters in the given list.
	 * TODO (jerome) remove once type parameters are enabled
	 */
	static IJavaElement[] nonTypeParameters(IJavaElement[] elements) {
		int length = elements.length;
		int count = 0;
		for (int i = 0; i < length; i++)
			if (elements[i].getElementType() != IJavaElement.TYPE_PARAMETER)
				count++;
		if (count == length) return elements;
		IJavaElement[] filteredElements = new IJavaElement[count];
		int index = 0;
		for (int i = 0; i < length; i++) 
			if (elements[i].getElementType() != IJavaElement.TYPE_PARAMETER)
				filteredElements[index++] = elements[i];
		return filteredElements;
	}

	public String[] getBounds() throws JavaModelException {
		TypeParameterElementInfo info = (TypeParameterElementInfo) getElementInfo();
		return CharOperation.toStrings(info.bounds);
	}

	public IMember getDeclaringMember() {
			return (IMember) getParent();
	}

	public String getElementName() {
		return this.name;
	}

	public int getElementType() {
		return TYPE_PARAMETER;
	}

	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_TYPE_PARAMETER;
	}
	
	public ISourceRange getNameRange() throws JavaModelException {
		TypeParameterElementInfo info = (TypeParameterElementInfo) getElementInfo();
		return new SourceRange(info.nameStart, info.nameEnd - info.nameStart + 1);
	}

	protected void toStringName(StringBuffer buffer) {
		buffer.append('<');
		buffer.append(getElementName());
		buffer.append('>');
	}
}

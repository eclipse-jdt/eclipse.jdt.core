/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
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
package org.eclipse.jdt.internal.core;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.internal.compiler.env.ISourceMethod;
import org.eclipse.jdt.internal.compiler.env.ISourceType;

/**
 * Element info for an LambdaExpression element that originated from source.
 */
public class LambdaTypeElementInfo extends SourceTypeElementInfo {

	protected LambdaExpression lambdaHandle = null;
	
	public LambdaTypeElementInfo(LambdaExpression handle) {
		this.lambdaHandle = handle;
		setSourceRangeStart(handle.lambdaExpression.sourceStart);
		setSourceRangeEnd(handle.lambdaExpression.sourceEnd);
	}

	public IJavaElement[] getChildren() {
		return new IJavaElement [] { this.lambdaHandle.getMethod() };
	}

	public ISourceType getEnclosingType() { // FIXME
		IJavaElement parent= this.lambdaHandle.getParent();
		if (parent != null && parent.getElementType() == IJavaElement.TYPE) {
			try {
				return (ISourceType)((JavaElement)parent).getElementInfo();
			} catch (JavaModelException e) {
				return null;
			}
		} else {
			return null;
		}
	}

	public ISourceField[] getFields() {
		return new ISourceField[0];
	}

	public char[] getFileName() {
		return this.lambdaHandle.getPath().toString().toCharArray();
	}

	public char[][] getInterfaceNames() {
		return new char[][] { this.lambdaHandle.lambdaExpression.descriptor.declaringClass.sourceName() };
	}

	public ISourceType[] getMemberTypes() {
		return new ISourceType[0];
	}

	public ISourceMethod[] getMethods() {
		ISourceMethod [] methods = new ISourceMethod[1];
		SourceMethod sourceMethod = this.lambdaHandle.getMethod();
		try {
			methods[0] = (ISourceMethod) sourceMethod.getElementInfo();
		} catch (JavaModelException e) {
			// ignore
		}
		return methods;
	}
	
	public char[] getName() {
		return this.lambdaHandle.getElementName().toCharArray();
	}

	public char[] getSuperclassName() {
		return "Object".toCharArray(); //$NON-NLS-1$
	}

	public char[][][] getTypeParameterBounds() {
		return new char[0][][];
	}

	public char[][] getTypeParameterNames() {
		return CharOperation.NO_CHAR_CHAR;
	}

	public boolean isBinaryType() {
		return false;
	}

	public String toString() {
		return "Info for " + this.lambdaHandle.toString(); //$NON-NLS-1$
	}

	public IType getHandle() {
		return this.lambdaHandle;
	}
}
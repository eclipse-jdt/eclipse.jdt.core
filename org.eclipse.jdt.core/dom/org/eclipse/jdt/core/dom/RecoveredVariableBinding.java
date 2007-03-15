/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.IJavaElement;

public class RecoveredVariableBinding implements IVariableBinding {

	private VariableDeclaration variableDeclaration;
	private BindingResolver resolver;

	public RecoveredVariableBinding(BindingResolver resolver, VariableDeclaration variableDeclaration) {
		this.resolver = resolver;
		this.variableDeclaration = variableDeclaration;
	}
	public Object getConstantValue() {
		return null;
	}

	public ITypeBinding getDeclaringClass() {
		return null;
	}

	public IMethodBinding getDeclaringMethod() {
		return null;
	}

	public String getName() {
		return this.variableDeclaration.getName().getIdentifier();
	}

	public ITypeBinding getType() {
		return this.resolver.getTypeBinding(this.variableDeclaration);
	}

	public IVariableBinding getVariableDeclaration() {
		return this;
	}

	public int getVariableId() {
		return 0;
	}

	public boolean isEnumConstant() {
		return false;
	}

	public boolean isField() {
		return this.variableDeclaration.getParent() instanceof FieldDeclaration;
	}

	public boolean isParameter() {
		return this.variableDeclaration instanceof SingleVariableDeclaration;
	}

	public IAnnotationBinding[] getAnnotations() {
		return AnnotationBinding.NoAnnotations;
	}

	public IJavaElement getJavaElement() {
		return null;
	}

	public String getKey() {
		return null;
	}

	public int getKind() {
		return IBinding.VARIABLE;
	}

	public int getModifiers() {
		return 0;
	}

	public boolean isDeprecated() {
		return false;
	}

	public boolean isEqualTo(IBinding binding) {
		return false;
	}

	public boolean isRecovered() {
		return true;
	}

	public boolean isSynthetic() {
		return false;
	}
}

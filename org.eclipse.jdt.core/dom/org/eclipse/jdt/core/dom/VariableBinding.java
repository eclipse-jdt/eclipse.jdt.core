/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.core.dom;

import org.eclipse.jdt.core.util.IModifierConstants;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.LocalVariableBinding;

/**
 * Internal implementation of variable bindings.
 */
class VariableBinding implements IVariableBinding {

	private org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding;
	private BindingResolver resolver;
	private String name;
	private ITypeBinding declaringClass;
	private ITypeBinding type;
	private String key;

	VariableBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding) {
		this.resolver = resolver;
		this.binding = binding;
	}

	/*
	 * @see IVariableBinding#isField()
	 */
	public boolean isField() {
		return this.binding instanceof FieldBinding;
	}

	/*
	 * @see IBinding#getName()
	 */
	public String getName() {
		if (this.name == null) {
			this.name = new String(this.binding.name);
		}
		return this.name;
	}

	/*
	 * @see IVariableBinding#getDeclaringClass()
	 */
	public ITypeBinding getDeclaringClass() {
		if (isField()) {
			if (this.declaringClass == null) {
				FieldBinding fieldBinding = (FieldBinding) this.binding;
				this.declaringClass = this.resolver.getTypeBinding(fieldBinding.declaringClass);
			}
			return this.declaringClass;
		} else {
			return null;
		}
	}
	
	/*
	 * @see IVariableBinding#getType()
	 */
	public ITypeBinding getType() {
		if (type == null) {
			type = this.resolver.getTypeBinding(this.binding.type);
		}
		return type;
	}

	/*
	 * @see IBinding#getKind()
	 */
	public int getKind() {
		return IBinding.VARIABLE;
	}

	/*
	 * @see IBinding#getModifiers()
	 */
	public int getModifiers() {
		if (isField()) {
			return ((FieldBinding) this.binding).getAccessFlags();
		}
		if (binding.isFinal()) {
			return IModifierConstants.ACC_FINAL;
		}
		return 0;
	}

	/*
	 * @see IBinding#isDeprecated()
	 */
	public boolean isDeprecated() {
		if (isField()) {
			return ((FieldBinding) this.binding).isDeprecated();
		}
		return false;
	}

	/**
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		if (isField()) {
			return ((FieldBinding) this.binding).isSynthetic();
		}
		return false;
	}

	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			if (isField()) {
				StringBuffer buffer = new StringBuffer();
				if (this.getDeclaringClass() != null) {
					buffer.append(this.getDeclaringClass().getKey());
					buffer.append('/');
				}
				buffer.append(this.getName());
				this.key = buffer.toString();
			} else {
				StringBuffer buffer = new StringBuffer();
				
				// declaring method or type
				LocalVariableBinding localVarBinding = (LocalVariableBinding) this.binding;
				BlockScope scope = localVarBinding.declaringScope;
				MethodScope methodScope = scope instanceof MethodScope ? (MethodScope) scope : scope.enclosingMethodScope();
				ReferenceContext referenceContext = methodScope.referenceContext;
				if (referenceContext instanceof AbstractMethodDeclaration) {
					org.eclipse.jdt.internal.compiler.lookup.MethodBinding internalBinding = ((AbstractMethodDeclaration) referenceContext).binding;
					IMethodBinding methodBinding = this.resolver.getMethodBinding(internalBinding);
					if (methodBinding != null) {
						buffer.append(methodBinding.getKey());
					}
				} else if (referenceContext instanceof TypeDeclaration) {
					org.eclipse.jdt.internal.compiler.lookup.TypeBinding internalBinding = ((TypeDeclaration) referenceContext).binding;
					ITypeBinding typeBinding = this.resolver.getTypeBinding(internalBinding);
					if (typeBinding != null) {
						buffer.append(typeBinding.getKey());
					}
				}
	
				// scope index
				getKey(scope, buffer);
	
				// variable name
				buffer.append('/');
				buffer.append(getName());
				
				this.key = buffer.toString();
			}
		}
		return this.key;
	}
	
	private void getKey(BlockScope scope, StringBuffer buffer) {
		int scopeIndex = scope.scopeIndex();
		if (scopeIndex != -1) {
			getKey((BlockScope)scope.parent, buffer);
			buffer.append('/');
			buffer.append(scopeIndex);
		}
	}
	
	/*
	 * @see IVariableBinding#getVariableId()
	 */
	public int getVariableId() {
		return this.binding.id;
	}

	/* (non-Javadoc)
	 * @see IVariableBinding#getConstantValue()
	 * @since 3.0
	 */
	public Object getConstantValue() {
		// TODO (olivier) - review implementation and add tests
		if (!this.binding.isConstantValue()) {
			return null;
		}
		Constant c = this.binding.constant;
		if (c == Constant.NotAConstant) return null;
		switch (c.typeID()) {
			case TypeIds.T_boolean:
				return Boolean.valueOf(c.booleanValue());
			case TypeIds.T_byte:
				return new Byte(c.byteValue());
			case TypeIds.T_char:
				return new Character(c.charValue());
			case TypeIds.T_double:
				return new Double(c.doubleValue());
			case TypeIds.T_float:
				return new Float(c.floatValue());
			case TypeIds.T_int:
				return new Integer(c.intValue());
			case TypeIds.T_long:
				return new Long(c.longValue());
			case TypeIds.T_short:
				return new Short(c.shortValue());
			case TypeIds.T_String:
				return c.stringValue();
		}
	}

	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}

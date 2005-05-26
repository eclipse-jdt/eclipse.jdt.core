/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
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
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.util.IModifierConstants;
import org.eclipse.jdt.internal.compiler.env.IConstants;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.FieldBinding;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.core.JavaElement;
import org.eclipse.jdt.internal.core.LocalVariable;

/**
 * Internal implementation of variable bindings.
 */
class VariableBinding implements IVariableBinding {

	private static final int VALID_MODIFIERS = Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE |
		Modifier.STATIC | Modifier.FINAL | Modifier.TRANSIENT | Modifier.VOLATILE;
	
	private org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding;
	private ITypeBinding declaringClass;
	private String key;
	private String name;
	private BindingResolver resolver;
	private ITypeBinding type;

	VariableBinding(BindingResolver resolver, org.eclipse.jdt.internal.compiler.lookup.VariableBinding binding) {
		this.resolver = resolver;
		this.binding = binding;
	}

	/* (non-Javadoc)
	 * @see IVariableBinding#getConstantValue()
	 * @since 3.0
	 */
	public Object getConstantValue() {
		if (!this.binding.isConstantValue()) return null;
		Constant c = this.binding.constant();
		if (c == null) return null;
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
			case TypeIds.T_JavaLangString:
				return c.stringValue();
		}
		return null;
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
	 * @see IVariableBinding#getDeclaringMethod()
	 */
	public IMethodBinding getDeclaringMethod() {
		if (!isField()) {
			ASTNode node = this.resolver.findDeclaringNode(this);
			while (true) {
				if (node == null) break;
				switch(node.getNodeType()) {
					case ASTNode.INITIALIZER :
						return null;
					case ASTNode.METHOD_DECLARATION :
						MethodDeclaration methodDeclaration = (MethodDeclaration) node;
						return methodDeclaration.resolveBinding();
					default:
						node = node.getParent();
				}
			}
		}
		return null;
	}

	/*
	 * @see IVariableBinding#getVariableDeclaration()
	 * @since 3.1
	 */
	public IVariableBinding getVariableDeclaration() {
		if (this.isField()) {
			FieldBinding fieldBinding = (FieldBinding) this.binding;
			return this.resolver.getVariableBinding(fieldBinding.original());
		}
		return this;
	}
	
	/*
	 * @see IBinding#getJavaElement()
	 */
	public IJavaElement getJavaElement() {
		JavaElement element = getUnresolvedJavaElement();
		if (element == null)
			return null;
		return element.resolved(this.binding);
	}
	
	private JavaElement getUnresolvedJavaElement() {
		if (isField()) {
			// field
			FieldBinding fieldBinding = (FieldBinding) this.binding;
			if (fieldBinding.declaringClass == null) return null; // arraylength
			IType declaringType = (IType) getDeclaringClass().getJavaElement();
			if (declaringType == null) return null;
			return (JavaElement) declaringType.getField(getName());
		}
		// local variable
		IMethodBinding declaringMethod = getDeclaringMethod();
		if (declaringMethod == null) return null;
		JavaElement method = (JavaElement) declaringMethod.getJavaElement();
		if (!(this.resolver instanceof DefaultBindingResolver)) return null;
		VariableDeclaration localVar = (VariableDeclaration) ((DefaultBindingResolver) this.resolver).bindingsToAstNodes.get(this);
		if (localVar == null) return null;
		int nameStart;
		int nameLength;
		int sourceStart;
		int sourceLength;
		if (localVar instanceof SingleVariableDeclaration) {
			sourceStart = localVar.getStartPosition();
			sourceLength = localVar.getLength();
			SimpleName simpleName = ((SingleVariableDeclaration) localVar).getName();
			nameStart = simpleName.getStartPosition();
			nameLength = simpleName.getLength();
		} else {
			nameStart =  localVar.getStartPosition();
			nameLength = localVar.getLength();
			ASTNode node = localVar.getParent();
			sourceStart = node.getStartPosition();
			sourceLength = node.getLength();
		}
		char[] typeSig = this.binding.type.genericTypeSignature();
		return new LocalVariable(method, localVar.getName().getIdentifier(), sourceStart, sourceStart+sourceLength-1, nameStart, nameStart+nameLength-1, new String(typeSig));
	}
	
	/*
	 * @see IBinding#getKey()
	 */
	public String getKey() {
		if (this.key == null) {
			this.key = new String(this.binding.computeUniqueKey());
		}
		return this.key;
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
			return ((FieldBinding) this.binding).getAccessFlags() & VALID_MODIFIERS;
		}
		if (binding.isFinal()) {
			return IModifierConstants.ACC_FINAL;
		}
		return 0;
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
	 * @see IVariableBinding#getType()
	 */
	public ITypeBinding getType() {
		if (type == null) {
			type = this.resolver.getTypeBinding(this.binding.type);
		}
		return type;
	}
	
	/*
	 * @see IVariableBinding#getVariableId()
	 */
	public int getVariableId() {
		return this.binding.id;
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
	
	/*
	 * @see IBinding#isEqualTo(Binding)
	 * @since 3.1
	 */
	public boolean isEqualTo(IBinding other) {
		if (other == this) {
			// identical binding - equal (key or no key)
			return true;
		}
		if (other == null) {
			// other binding missing
			return false;
		}
		if (!(other instanceof VariableBinding)) {
			return false;
		}
		org.eclipse.jdt.internal.compiler.lookup.VariableBinding otherBinding = ((VariableBinding) other).binding;
		if (this.binding instanceof FieldBinding) {
			if (otherBinding instanceof FieldBinding) {
				return BindingComparator.isEqual((FieldBinding) this.binding, (FieldBinding) otherBinding);
			} else {
				return false;
			}
		} else {
			if (BindingComparator.isEqual(this.binding, otherBinding)) {
				IMethodBinding declaringMethod = this.getDeclaringMethod();
				IMethodBinding otherDeclaringMethod = ((VariableBinding) other).getDeclaringMethod();
				if (declaringMethod == null) {
					if (otherDeclaringMethod != null) {
						return false;
					}
					return true;
				}
				return declaringMethod.isEqualTo(otherDeclaringMethod);
			}
			return false;
		}
	}

	/*
	 * @see IVariableBinding#isField()
	 */
	public boolean isField() {
		return this.binding instanceof FieldBinding;
	}

	/*
	 * @see IVariableBinding#isEnumConstant()
	 * @since 3.1
	 */
	public boolean isEnumConstant() {
		return (this.binding.modifiers & IConstants.AccEnum) != 0;
	}

	/*
	 * @see IBinding#isSynthetic()
	 */
	public boolean isSynthetic() {
		if (isField()) {
			return ((FieldBinding) this.binding).isSynthetic();
		}
		return false;
	}

	/* 
	 * For debugging purpose only.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return this.binding.toString();
	}
}

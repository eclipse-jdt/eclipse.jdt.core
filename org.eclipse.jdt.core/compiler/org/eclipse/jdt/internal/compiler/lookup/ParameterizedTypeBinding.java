/*******************************************************************************
 * Copyright (c) 2000-2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

/**
 * A parameterized type encapsulates a type with type arguments,
 */
public class ParameterizedTypeBinding extends ReferenceBinding {

	public ReferenceBinding type; 
	public TypeBinding[] typeArguments;
	
	public ParameterizedTypeBinding(ReferenceBinding type, TypeBinding[] typeArguments){
		
		this.type = type;
		this.fPackage = type.fPackage;
		this.fileName = type.fileName;
		this.typeArguments = typeArguments;
		this.tagBits = type.tagBits | IsParameterizedType;
		// TODO determine if need to copy other tagBits from type so as to provide right behavior to all predicates
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#availableFields()
	 */
	public FieldBinding[] availableFields() {

		return this.type.availableFields();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#availableMethods()
	 */
	public MethodBinding[] availableMethods() {

		return this.type.availableMethods();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#canBeInstantiated()
	 */
	public boolean canBeInstantiated() {

		if (this.type == null) return false;		
		return this.type.canBeInstantiated();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#computeId()
	 */
	public void computeId() {

		this.id = NoId;		
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#constantPoolName()
	 */
	public char[] constantPoolName() {
		return this.type.constantPoolName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#debugName()
	 */
	String debugName() {
		return this.type.debugName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#enclosingType()
	 */
	public ReferenceBinding enclosingType() {
		return this.type.enclosingType();
	}

	/**
	 * The erasure of a parameterized type is the erasure of its type (in case itself a parameterized type)
	 */
	public TypeBinding erasure(){
		return this.type.erasure();
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fieldCount()
	 */
	public int fieldCount() {
		return this.type.fieldCount();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#fields()
	 */
	public FieldBinding[] fields() {
		return this.type.fields();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactConstructor(TypeBinding[])
	 */
	public MethodBinding getExactConstructor(TypeBinding[] argumentTypes) {
		return this.type.getExactConstructor(argumentTypes);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getExactMethod(char[], TypeBinding[])
	 */
	public MethodBinding getExactMethod(char[] selector, TypeBinding[] argumentTypes) {
		return this.type.getExactMethod(selector, argumentTypes);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getField(char[], boolean)
	 */
	public FieldBinding getField(char[] fieldName, boolean needResolve) {
		return this.type.getField(fieldName, needResolve);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMemberType(char[])
	 */
	public ReferenceBinding getMemberType(char[] typeName) {
		return this.type.getMemberType(typeName);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#getMethods(char[])
	 */
	public MethodBinding[] getMethods(char[] selector) {
		return this.type.getMethods(selector);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#implementsInterface(ReferenceBinding, boolean)
	 */
	public boolean implementsInterface(ReferenceBinding anInterface, boolean searchHierarchy) {
		return this.type.implementsInterface(anInterface, searchHierarchy);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#implementsMethod(MethodBinding)
	 */
	public boolean implementsMethod(MethodBinding method) {
		return this.type.implementsMethod(method);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isCompatibleWith(TypeBinding)
	 */
	public boolean isCompatibleWith(TypeBinding right) {
		return this.type.isCompatibleWith(right);
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#isSuperclassOf(ReferenceBinding)
	 */
	public boolean isSuperclassOf(ReferenceBinding type) {
		return this.type.isSuperclassOf(type);
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#memberTypes()
	 */
	public ReferenceBinding[] memberTypes() {
		return this.type.memberTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#methods()
	 */
	public MethodBinding[] methods() {
		return this.type.methods();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedSourceName()
	 */
	public char[] qualifiedSourceName() {
		return this.type.qualifiedSourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#readableName()
	 */
	public char[] readableName() {
		return this.type.readableName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#shortReadableName()
	 */
	public char[] shortReadableName() {
		return this.type.shortReadableName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#signature()
	 */
	public char[] signature() {
		return this.type.signature();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#sourceName()
	 */
	public char[] sourceName() {
		return this.type.sourceName();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superclass()
	 */
	public ReferenceBinding superclass() {
		return this.type.superclass();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#superInterfaces()
	 */
	public ReferenceBinding[] superInterfaces() {
		return this.type.superInterfaces();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticEnclosingInstanceTypes()
	 */
	public ReferenceBinding[] syntheticEnclosingInstanceTypes() {
		return this.type.syntheticEnclosingInstanceTypes();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding#syntheticOuterLocalVariables()
	 */
	public SyntheticArgumentBinding[] syntheticOuterLocalVariables() {
		return this.type.syntheticOuterLocalVariables();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#leafComponentType()
	 */
	public TypeBinding leafComponentType() {
		return this.type.leafComponentType();
	}

	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#qualifiedPackageName()
	 */
	public char[] qualifiedPackageName() {
		return this.type.qualifiedPackageName();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer(10);
		buffer.append(this.type);
		buffer.append('<');
		for (int i = 0; i < this.typeArguments.length; i++){
			if (i > 0) buffer.append(',');
			buffer.append(this.typeArguments[i]);
		}
		buffer.append('>');
		return buffer.toString();
	}
}

package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.env.ISourceField;
import org.eclipse.jdt.core.Signature;

/**
 * Element info for IField elements.
 */

/* package */
class SourceFieldElementInfo
	extends MemberElementInfo
	implements ISourceField {

	/**
	 * The type name of this field.
	 */
	protected char[] fTypeName;

	/**
	 * The field's constant value
	 */
	protected Constant fConstant;
	/**
	 * Constructs an info object for the given field.
	 */
	protected SourceFieldElementInfo() {
		fConstant = Constant.NotAConstant;
	}

	/**
	 * Returns the constant associated with this field or
	 * Constant.NotAConstant if the field is not constant.
	 */
	public Constant getConstant() {
		return fConstant;
	}

	/**
	 * Returns the type name of the field.
	 */
	public char[] getTypeName() {
		return fTypeName;
	}

	/**
	 * Returns the type signature of the field.
	 *
	 * @see Signature
	 */
	protected String getTypeSignature() {
		return Signature.createTypeSignature(fTypeName, false);
	}

	/**
	 * Returns the constant associated with this field or
	 * Constant.NotAConstant if the field is not constant.
	 */
	public void setConstant(Constant constant) {
		fConstant = constant;
	}

	/**
	 * Sets the type name of the field.
	 */
	protected void setTypeName(char[] typeName) {
		fTypeName = typeName;
	}

}

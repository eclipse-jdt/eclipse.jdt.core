package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.IDOMNode;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;

/**
 * @see IMember
 */

/* package */
abstract class Member extends SourceRefElement implements IMember {
	protected Member(int type, IJavaElement parent, String name) {
		super(type, parent, name);
	}

	/**
	 * Converts a field constant from the compiler's representation
	 * to the Java Model constant representation (Number or String).
	 */
	protected static Object convertConstant(Constant constant) {
		if (constant == null)
			return null;
		if (constant == Constant.NotAConstant) {
			return null;
		}
		switch (constant.typeID()) {
			case TypeIds.T_boolean :
				return constant.booleanValue() ? Boolean.TRUE : Boolean.FALSE;
			case TypeIds.T_byte :
				return new Byte(constant.byteValue());
			case TypeIds.T_char :
				return new Character(constant.charValue());
			case TypeIds.T_double :
				return new Double(constant.doubleValue());
			case TypeIds.T_float :
				return new Float(constant.floatValue());
			case TypeIds.T_int :
				return new Integer(constant.intValue());
			case TypeIds.T_long :
				return new Long(constant.longValue());
			case TypeIds.T_null :
				return null;
			case TypeIds.T_short :
				return new Short(constant.shortValue());
			case TypeIds.T_String :
				return constant.stringValue();
			default :
				return null;
		}
	}

	/**
	 * @see JavaElement#equalsDOMNode
	 */
	protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
		return getElementName().equals(node.getName());
	}

	/**
	 * @see IMember
	 */
	public IClassFile getClassFile() {
		return ((JavaElement) getParent()).getClassFile();
	}

	/**
	 * @see IMember
	 */
	public IType getDeclaringType() {
		JavaElement parent = (JavaElement) getParent();
		if (parent.fLEType == TYPE) {
			return (IType) parent;
		}
		return null;
	}

	/**
	 * @see IMember
	 */
	public int getFlags() throws JavaModelException {
		MemberElementInfo info = (MemberElementInfo) getElementInfo();
		return info.getModifiers();
	}

	/**
	 * @see JavaElement#getHandleMemento()
	 */
	protected char getHandleMementoDelimiter() {
		return JavaElement.JEM_TYPE;
	}

	/**
	 * @see IMember
	 */
	public ISourceRange getNameRange() throws JavaModelException {
		MemberElementInfo info = (MemberElementInfo) getRawInfo();
		return new SourceRange(
			info.getNameSourceStart(),
			info.getNameSourceEnd() - info.getNameSourceStart() + 1);
	}

	/**
	 * @see IMember
	 */
	public boolean isBinary() {
		return false;
	}

	/**
	 * @see IJavaElement
	 */
	public boolean isReadOnly() {
		return getClassFile() != null;
	}

	/**
	 * Changes the source indexes of this element.  Updates the name range as well.
	 */
	public void offsetSourceRange(int amount) {
		super.offsetSourceRange(amount);
		try {
			MemberElementInfo info = (MemberElementInfo) getRawInfo();
			info.setNameSourceStart(info.getNameSourceStart() + amount);
			info.setNameSourceEnd(info.getNameSourceEnd() + amount);
		} catch (JavaModelException npe) {
			return;
		}
	}

	/**
	 */
	public String readableName() {

		IJavaElement declaringType = getDeclaringType();
		if (declaringType != null) {
			String declaringName = ((JavaElement) getDeclaringType()).readableName();
			StringBuffer buffer = new StringBuffer(declaringName);
			buffer.append('.');
			buffer.append(this.getElementName());
			return buffer.toString();
		} else {
			return super.readableName();
		}
	}

	/**
	 * Updates the source positions for this element.
	 */
	public void triggerSourceEndOffset(int amount, int nameStart, int nameEnd) {
		super.triggerSourceEndOffset(amount, nameStart, nameEnd);
		updateNameRange(nameStart, nameEnd);
	}

	/**
	 * Updates the source positions for this element.
	 */
	public void triggerSourceRangeOffset(int amount, int nameStart, int nameEnd) {
		super.triggerSourceRangeOffset(amount, nameStart, nameEnd);
		updateNameRange(nameStart, nameEnd);
	}

	/**
	 * Updates the name range for this element.
	 */
	protected void updateNameRange(int nameStart, int nameEnd) {
		try {
			MemberElementInfo info = (MemberElementInfo) getRawInfo();
			info.setNameSourceStart(nameStart);
			info.setNameSourceEnd(nameEnd);
		} catch (JavaModelException npe) {
			return;
		}
	}

}

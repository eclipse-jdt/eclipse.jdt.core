package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public abstract class AbstractMemberHandle
	extends NonStateSpecificHandleImpl
	implements IMember {
	/**
	 * The owner of the member
	 */
	ClassOrInterfaceHandleImpl fOwner;

	/**
	 * Member signature
	 */
	String fSignature;
	/**
	 * Synopsis: Answer a method or constructor signature given
	 * the name (which may be empty) and the parameter types.
	 */
	String computeSignature(String name, IType[] parameterTypes) {

		if (parameterTypes.length == 0) {
			return name + "()";
		}

		StringBuffer sb = new StringBuffer(name);
		sb.append('(');
		for (int i = 0; i < parameterTypes.length; i++) {
			try {
				((TypeImpl) parameterTypes[i]).appendSignature(sb, true);
			} catch (ClassCastException e) {
				throw new StateSpecificException("incompatible parameter types");
			}
		}
		sb.append(')');
		return sb.toString();
	}

	/**
	 * Compares two members
	 */
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof AbstractMemberHandle))
			return false;

		AbstractMemberHandle member = (AbstractMemberHandle) o;
		return member.fSignature.equals(this.fSignature)
			&& member.fOwner.equals(this.fOwner);
	}

	/**
	 * Returns the owning class of this member.
	 */
	public IType getDeclaringClass() {
		return fOwner;
	}

	/**
	 * getInternalDC method comment.
	 */
	JavaDevelopmentContextImpl getInternalDC() {
		return fOwner.getInternalDC();
	}

	/**
	 * Returns the Java language modifiers for the member 
	 *	represented by this object, as an integer.  
	 */
	public int getModifiers() {
		return ((AbstractMemberHandleSWH) inCurrentState()).getModifiers();
	}

	/**
	 *	Returns the simple name of the member represented by this object.
	 *	If this Member represents a constructor, this returns 
	 *	the simple name of its declaring class.
	 *	This is a handle-only method. 
	 */
	public abstract String getName();
	/**
	 * Returns the signature of the member.  For fields, this is the field name.
	 * For methods, this is the method name, followed by $(, followed by the 
	 * source signatures of the parameter types, followed by $).
	 * For constructors, this is $(, followed by the source signatures of the 
	 * parameter types, followed by $).
	 */
	String getSignature() {
		return fSignature;
	}

	/**
	 * Returns a consistent hash code for this object
	 */
	public int hashCode() {
		return fSignature.hashCode();
	}

	/**
	 * Returns true if this represents a binary member, false otherwise.
	 * A binary member is one for which the declaring class is in .class 
	 * file format in the source tree.
	 */
	public boolean isBinary() {
		return ((IMember) inCurrentState()).isBinary();
	}

	/**
	 * @see IMember
	 */
	public boolean isDeprecated() {
		return ((AbstractMemberHandleSWH) inCurrentState()).isDeprecated();
	}

	/**
	 * Returns true if the member represented by this object is
	 *	synthetic, false otherwise.  A synthetic object is one that
	 *	was invented by the compiler, but was not declared in the source.
	 *	See <em>The Inner Classes Specification</em>.
	 *	A synthetic object is not the same as a fictitious object.
	 */
	public boolean isSynthetic() {
		return ((AbstractMemberHandleSWH) inCurrentState()).isSynthetic();
	}

	/**
	 * kind method comment.
	 */
	public int kind() {
		return 0;
	}

}

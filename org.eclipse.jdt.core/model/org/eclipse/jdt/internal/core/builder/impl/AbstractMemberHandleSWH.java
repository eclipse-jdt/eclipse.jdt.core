package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.core.builder.*;

public abstract class AbstractMemberHandleSWH
	extends StateSpecificHandleImpl
	implements IMember {
	/**
	 *	Returns the Type object representing the class or interface
	 *	that declares the member represented by this object.
	 *	Derived from java.lang.reflect.Member.getDeclaringClass();
	 *	This is a handle-only method.
	 */
	public IType getDeclaringClass() {
		return (IType) getHandle().getDeclaringClass().inState(fState);
	}

	/**
	 * Returns the handle for this member
	 */
	abstract IMember getHandle();
	/**
	 * Returns the Java language modifiers for the member 
	 *	represented by this object, as an integer.  
	 */
	public abstract int getModifiers();
	/**
	 *	Returns the simple name of the member represented by this object.
	 *	If this Member represents a constructor, this returns 
	 *	the simple name of its declaring class.
	 *	This is a handle-only method. 
	 */
	public String getName() {
		return ((IMember) nonStateSpecific()).getName();
	}

	/**
	 * Returns the type structure entry for the class or interface.
	 */
	TypeStructureEntry getTypeStructureEntry() throws NotPresentException {
		TypeStructureEntry tsEntry =
			fState.getTypeStructureEntry(getHandle().getDeclaringClass(), true);
		if (tsEntry == null) {
			throw new NotPresentException();
		}
		return tsEntry;
	}

	/**
	 * Returns true if this represents a binary member, false otherwise.
	 * A binary member is one for which the declaring class is in .class 
	 * file format in the source tree.
	 */
	public boolean isBinary() {
		if (!isPresent())
			throw new NotPresentException();
		return getDeclaringClass().isBinary();
	}

	/**
	 * @see IMember
	 */
	public abstract boolean isDeprecated();
	/**
	 * Returns true if the member represented by this object is
	 *	synthetic, false otherwise.  A synthetic object is one that
	 *	was invented by the compiler, but was not declared in the source.
	 *	See <em>The Inner Classes Specification</em>.
	 *	A synthetic object is not the same as a fictitious object.
	 */
	public boolean isSynthetic() {
		//Do we have synthetic fields right now?
		return false;
	}

}

package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;

public class FieldImplSWH extends AbstractMemberHandleSWH implements IField {
	FieldImpl fHandle;
	/**
	 * Internal - Creates a new field handle in the given state
	 */
	FieldImplSWH(StateImpl state, FieldImpl handle) {
		fState = state;
		fHandle = handle;
	}

	/**
	 * Gets the IBinaryField for this field.
	 */
	protected IBinaryField getBinaryField() throws NotPresentException {
		return getBinaryField(getTypeStructureEntry());
	}

	/**
	 * Returns the IBinaryField for this field
	 */
	protected IBinaryField getBinaryField(TypeStructureEntry tsEntry)
		throws NotPresentException {
		IBinaryType t = fState.getBinaryType(tsEntry);
		IBinaryField f = BinaryStructure.getField(t, fHandle.getName());
		if (f == null) {
			throw new NotPresentException();
		} else {
			return f;
		}
	}

	/**
	  * Returns the non state specific handle
	  */
	IMember getHandle() {
		return fHandle;
	}

	/**
	 * Returns the Java language modifiers for the member 
	 * represented by this object, as an integer.  
	 */
	public int getModifiers() {
		return getBinaryField().getModifiers() & 0xFFFF;
	}

	/**
	 * Returns the name of the field.
	 */
	public String getName() {
		return fHandle.getName();
	}

	/**
	 * Returns a Type object that identifies the declared type for
	 *	the field represented by this Field object.
	 */
	public IType getType() {
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		IBinaryField f = getBinaryField(tsEntry);
		char[] sig = f.getTypeName();
		return (IType) fState
			.typeSignatureToHandle(tsEntry, Util.convertTypeSignature(sig))
			.inState(fState);
	}

	/**
	 * Returns true if the member represented by this object is
	 *	deprecated, false otherwise.  A deprecated object is one that
	 *	has a @ deprecated tag in its doc comment.
	 */
	public boolean isDeprecated() {
		return (getBinaryField().getModifiers() & IConstants.AccDeprecated) != 0;
	}

	/**
	 * Returns true if the object represented by the receiver is present 
	 *	in the development context, false otherwise.  If the receiver is 
	 *	state-specific, checks whether it is present in this object's state, 
	 *	otherwise checks whether it is present in the current state of the 
	 *	development context.
	 */
	public boolean isPresent() {
		TypeStructureEntry entry =
			fState.getTypeStructureEntry(fHandle.getDeclaringClass(), true);
		if (entry == null) {
			return false;
		}
		IBinaryType t = fState.getBinaryType(entry);
		IBinaryField f = BinaryStructure.getField(t, fHandle.fSignature);
		return f != null;
	}

	/**
	 * Returns true if the member represented by this object is
	 *	synthetic, false otherwise.  A synthetic object is one that
	 *	was invented by the compiler, but was not declared in the source.
	 *	See <em>The Inner Classes Specification</em>.
	 *	A synthetic object is not the same as a fictitious object.
	 */
	public boolean isSynthetic() throws NotPresentException {
		return (getBinaryField().getModifiers() & IConstants.AccSynthetic) != 0;
	}

	/**
	  * Returns the non state specific handle
	  */
	public IHandle nonStateSpecific() {
		return fHandle;
	}

}

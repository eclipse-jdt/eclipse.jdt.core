package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.core.builder.*;
import org.eclipse.jdt.internal.core.Util;

public class ConstructorImplSWH
	extends AbstractMemberHandleSWH
	implements IConstructor {
	ConstructorImpl fHandle;
	/**
	 * Internal - Creates a new constructor handle in the given state
	 */
	ConstructorImplSWH(StateImpl state, ConstructorImpl handle) {
		fState = state;
		fHandle = handle;
	}

	/**
	 * Internal - Returns the IBinaryMethod for this method.
	 */
	protected IBinaryMethod getBinaryMethod() throws NotPresentException {
		return getBinaryMethod(getTypeStructureEntry());
	}

	/**
	 * Internal - Returns the IBinaryMethod for this method.
	 */
	protected IBinaryMethod getBinaryMethod(TypeStructureEntry tsEntry)
		throws NotPresentException {
		IBinaryType t = fState.getBinaryType(getTypeStructureEntry());
		IBinaryMethod m = BinaryStructure.getConstructor(t, fHandle.fSignature);
		if (m == null) {
			throw new NotPresentException();
		} else {
			return m;
		}
	}

	/**
	 * Returns an array of Type objects that represent the types of
	 *	the checked exceptions thrown by the underlying constructor
	 *	represented by this Constructor object.	
	 *	Unchecked exceptions are not included in the result, even if
	 *	they are declared in the source.
	 *	Returns an array of length 0 if the constructor throws no checked 
	 *	exceptions.
	 *	The resulting Types are in no particular order.
	 */
	public IType[] getExceptionTypes() {
		TypeStructureEntry tsEntry = getTypeStructureEntry();
		char[][] exceptions = getBinaryMethod(tsEntry).getExceptionTypeNames();
		PackageImpl pkg = fHandle.fOwner.fOwner;
		int len = exceptions.length;
		IType[] results = new IType[len];
		for (int i = 0; i < len; i++) {
			results[i] =
				(IType) fState
					.typeNameToHandle(tsEntry, Util.convertTypeSignature(exceptions[i]))
					.inState(fState);
		}
		return results;
	}

	/**
	 * Internal - Returns the non state specific handle
	 */
	IMember getHandle() {
		return fHandle;
	}

	/**
	 * Returns the Java language modifiers for the member 
	 *	represented by this object, as an integer.  
	 */
	public int getModifiers() {
		return getBinaryMethod().getModifiers() & 0xFFFF;
	}

	/**
	 * Returns an array of Type objects that represent the formal
	 *	parameter types, in declaration order, of the constructor
	 *	represented by this Constructor object.	
	 *	Returns an array of length 0 if the underlying constructor 
	 *	takes no parameters.  This is a handle-only method.
	*/
	public IType[] getParameterTypes() {
		IType[] unwrapped = (IType[]) fHandle.getParameterTypes();
		IType[] results = new IType[unwrapped.length];
		for (int i = 0; i < results.length; i++) {
			results[i] = (IType) unwrapped[i].inState(fState);
		}
		return results;
	}

	/**
	 * Returns true if the member represented by this object is
	 *	deprecated, false otherwise.  A deprecated object is one that
	 *	has a @ deprecated tag in its doc comment.
	 */
	public boolean isDeprecated() {
		return (getBinaryMethod().getModifiers() & IConstants.AccDeprecated) != 0;
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
		IBinaryMethod m = BinaryStructure.getConstructor(t, fHandle.fSignature);
		return m != null;
	}

	/**
	 * Returns true if the member represented by this object is
	 *	synthetic, false otherwise.  A synthetic object is one that
	 *	was invented by the compiler, but was not declared in the source.
	 *	See <em>The Inner Classes Specification</em>.
	 *	A synthetic object is not the same as a fictitious object.
	 */
	public boolean isSynthetic() throws NotPresentException {
		return (getBinaryMethod().getModifiers() & IConstants.AccSynthetic) != 0;
	}

	/**
	  * Returns the non state specific handle
	  */
	public IHandle nonStateSpecific() {
		return fHandle;
	}

}

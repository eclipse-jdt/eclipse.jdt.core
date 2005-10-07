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
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;

public class CaptureBinding extends TypeVariableBinding {
	
	public TypeBinding lowerBound;
	public WildcardBinding wildcard;
	
	/* information to compute unique binding key */
	public ReferenceBinding sourceType;
	public int position;
	
	public CaptureBinding(WildcardBinding wildcard, ReferenceBinding sourceType, int position) {
		super(WILDCARD_CAPTURE_NAME, null, 0);
		this.wildcard = wildcard;
		this.modifiers = AccPublic | AccGenericSignature; // treat capture as public
		this.fPackage = wildcard.fPackage;
		this.sourceType = sourceType;
		this.position = position;
	}

	/*
	 * sourceTypeKey ! wildcardKey position semi-colon
	 * p.X { capture of ? } --> !*123; (Lp/X; in declaring type except if leaf)
	 * p.X { capture of ? extends p.Y } --> !+Lp/Y;123; (Lp/X; in declaring type except if leaf)
	 */
	public char[] computeUniqueKey(boolean isLeaf) {
		StringBuffer buffer = new StringBuffer();
		if (isLeaf) {
			buffer.append(this.sourceType.computeUniqueKey(false/*not a leaf*/));
			buffer.append('&');
		}
		buffer.append(WILDCARD_CAPTURE);
		buffer.append(this.wildcard.computeUniqueKey(false/*not a leaf*/));
		buffer.append(this.position);
		buffer.append(';');
		int length = buffer.length();
		char[] uniqueKey = new char[length];
		buffer.getChars(0, length, uniqueKey, 0);
		return uniqueKey;
	}	

	public String debugName() {
		if (this.wildcard != null) {
			return String.valueOf(TypeConstants.WILDCARD_CAPTURE_NAME) + this.wildcard.debugName();
		}
		return super.debugName();
	}
	
	public char[] genericTypeSignature() {
		if (this.genericTypeSignature == null) {
			this.genericTypeSignature = CharOperation.concat(WILDCARD_CAPTURE, this.wildcard.genericTypeSignature());
		}
		return this.genericTypeSignature;
	}

	/**
	 * Initialize capture bounds using substituted supertypes
	 * e.g. given X<U, V extends X<U, V>>,     capture(X<E,?>) = X<E,capture>, where capture extends X<E,capture>
	 */
	public void initializeBounds(Scope scope, ParameterizedTypeBinding capturedParameterizedType) {
		TypeVariableBinding wildcardVariable = wildcard.typeVariable();
		ReferenceBinding originalVariableSuperclass = wildcardVariable.superclass;
		ReferenceBinding substitutedVariableSuperclass = (ReferenceBinding) Scope.substitute(capturedParameterizedType, originalVariableSuperclass);
		// prevent cyclic capture: given X<T>, capture(X<? extends T> could yield a circular type
		if (substitutedVariableSuperclass == this) substitutedVariableSuperclass = originalVariableSuperclass;
		
		ReferenceBinding[] originalVariableInterfaces = wildcardVariable.superInterfaces();		
		ReferenceBinding[] substitutedVariableInterfaces = Scope.substitute(capturedParameterizedType, originalVariableInterfaces);
		if (substitutedVariableInterfaces != originalVariableInterfaces) {
			// prevent cyclic capture: given X<T>, capture(X<? extends T> could yield a circular type
			for (int i = 0, length = substitutedVariableInterfaces.length; i < length; i++) {
				if (substitutedVariableInterfaces[i] == this) substitutedVariableInterfaces[i] = originalVariableInterfaces[i];
			}
		}
		// no substitution for wildcard bound (only formal bounds from type variables are to be substituted: 104082)
		// still need to capture bound supertype as well so as not to expose wildcards to the outside (111208)
		TypeBinding originalWildcardBound = wildcard.bound;
		TypeBinding substitutedWildcardBound = originalWildcardBound == null ? null : originalWildcardBound.capture(scope, this.position);
		if (substitutedWildcardBound == this) substitutedWildcardBound = originalWildcardBound;
		
		switch (wildcard.boundKind) {
			case Wildcard.EXTENDS :
				if (wildcard.bound.isInterface()) {
					this.superclass = substitutedVariableSuperclass;
					// merge wildcard bound into variable superinterfaces using glb
					if (substitutedVariableInterfaces == NoSuperInterfaces) {
						this.superInterfaces = new ReferenceBinding[] { (ReferenceBinding) substitutedWildcardBound };
					} else {
						int length = substitutedVariableInterfaces.length;
						System.arraycopy(substitutedVariableInterfaces, 0, substitutedVariableInterfaces = new ReferenceBinding[length+1], 1, length);
						substitutedVariableInterfaces[0] =  (ReferenceBinding) substitutedWildcardBound;
						this.superInterfaces = Scope.greaterLowerBound(substitutedVariableInterfaces);
					}
				} else {
					// per construction the wildcard bound is a subtype of variable superclass
					this.superclass = wildcard.bound.isArrayType() ? substitutedVariableSuperclass : (ReferenceBinding)substitutedWildcardBound;
					this.superInterfaces = substitutedVariableInterfaces;
				}
				this.firstBound =  substitutedWildcardBound;
				if ((originalWildcardBound.tagBits & HasTypeVariable) == 0)
					this.tagBits &= ~HasTypeVariable;
				break;
			case Wildcard.UNBOUND :
				this.superclass = substitutedVariableSuperclass;
				this.superInterfaces = substitutedVariableInterfaces;
				this.tagBits &= ~HasTypeVariable;
				break;
			case Wildcard.SUPER :
				this.superclass = substitutedVariableSuperclass;
				if (wildcardVariable.firstBound == substitutedVariableSuperclass || substitutedWildcardBound == substitutedVariableSuperclass) {
					this.firstBound = substitutedVariableSuperclass;
				}
				this.superInterfaces = substitutedVariableInterfaces;
				this.lowerBound = substitutedWildcardBound;
				if ((substitutedWildcardBound.tagBits & HasTypeVariable) == 0)
					this.tagBits &= ~HasTypeVariable;
				break;
		}		
	}
	
	/**
	 * @see org.eclipse.jdt.internal.compiler.lookup.TypeBinding#isCapture()
	 */
	public boolean isCapture() {
		return true;
	}
	
	/**
	 * @see TypeBinding#isEquivalentTo(TypeBinding)
	 */
	public boolean isEquivalentTo(TypeBinding otherType) {
	    if (this == otherType) return true;
	    if (otherType == null) return false;
		// capture of ? extends X[]
		if (this.firstBound != null && this.firstBound.isArrayType()) {
			if (this.firstBound.isCompatibleWith(otherType))
				return true;
		}
	    if (otherType.isWildcard()) // wildcard
			return ((WildcardBinding) otherType).boundCheck(this);
		return false;
	}

	public char[] readableName() {
		if (this.wildcard != null) {
			return CharOperation.concat(TypeConstants.WILDCARD_CAPTURE_NAME, this.wildcard.readableName());
		}
		return super.readableName();
	}
	
	public char[] shortReadableName() {
		if (this.wildcard != null) {
			return CharOperation.concat(TypeConstants.WILDCARD_CAPTURE_NAME, this.wildcard.shortReadableName());
		}
		return super.shortReadableName();
	}
	
	public String toString() {
		if (this.wildcard != null) {
			return String.valueOf(TypeConstants.WILDCARD_CAPTURE_NAME) + this.wildcard.toString();
		}
		return super.toString();
	}		
}

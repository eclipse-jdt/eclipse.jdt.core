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
		TypeVariableBinding wildcardVariable = wildcard.typeVariable();
		switch (wildcard.boundKind) {
			case Wildcard.EXTENDS :
				this.superclass = wildcard.superclass();
				this.firstBound = wildcard.bound;
				ReferenceBinding[] wildcardInterfaces = wildcard.superInterfaces();
				if (wildcardInterfaces == NoSuperInterfaces) {
					this.superInterfaces = NoSuperInterfaces;
				} else {
					this.superInterfaces = Scope.greaterLowerBound(wildcardInterfaces);
				}
				if ((wildcard.bound.tagBits & HasTypeVariable) == 0)
					this.tagBits &= ~HasTypeVariable;
				break;
			case Wildcard.UNBOUND :
				this.superclass = wildcardVariable.superclass();
				this.superInterfaces = wildcardVariable.superInterfaces();
				this.tagBits &= ~HasTypeVariable;
				break;
			case Wildcard.SUPER :
				this.superclass = wildcardVariable.superclass();
				if (wildcardVariable.firstBound == this.superclass || wildcard.bound == this.superclass) {
					this.firstBound = this.superclass;
				}
				this.superInterfaces = wildcardVariable.superInterfaces();
				this.lowerBound = wildcard.bound;
				if ((wildcard.bound.tagBits & HasTypeVariable) == 0)
					this.tagBits &= ~HasTypeVariable;
				break;
		}
	}

	/*
	 * sourceTypeKey ! wildcardKey position semi-colon
	 * p.X { capture of ? } --> Lp/X;!*123;
	 * p.X { capture of ? extends p.Y } --> Lp/X;!+Lp/Y;123;
	 */
	public char[] computeUniqueKey(boolean withAccessFlags) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.sourceType.computeUniqueKey(false/*without access flags*/));
		buffer.append(WILDCARD_CAPTURE);
		buffer.append(this.wildcard.computeUniqueKey(false/*without access flags*/));
		buffer.append(this.position);
		buffer.append(';');
		int length = buffer.length();
		char[] uniqueKey = new char[length];
		buffer.getChars(0, length, uniqueKey, 0);
		return uniqueKey;
	}	

	public String debugName() {
		if (this.wildcard != null) {
			return String.valueOf(TypeConstants.WILDCARD_CAPTURE_NAME) + this.wildcard.debugName(); //$NON-NLS-1$
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

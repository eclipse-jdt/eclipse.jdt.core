/*******************************************************************************
 * Copyright (c) 2000, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class IntersectionCastTypeBinding extends ReferenceBinding {

	public ReferenceBinding [] intersectingTypes;
	private ReferenceBinding javaLangObject;
	int length;
	
	public IntersectionCastTypeBinding(ReferenceBinding[] intersectingTypes, LookupEnvironment environment) {
		this.intersectingTypes = intersectingTypes;
		this.length = intersectingTypes.length;
		if (!intersectingTypes[0].isClass()) {
			this.javaLangObject = environment.getResolvedType(TypeConstants.JAVA_LANG_OBJECT, null);
			this.modifiers |= ClassFileConstants.AccInterface;
		}
	}
	
	public MethodBinding getSingleAbstractMethod(Scope scope) {
		if (this.singleAbstractMethod != null)
			return this.singleAbstractMethod;
		MethodBinding sam = samProblemBinding;  // guilty unless proven innocent !
		for (int i = 0; i < this.length; i++) {
			MethodBinding method = this.intersectingTypes[i].getSingleAbstractMethod(scope);
			if (method != null) {
				if (method.isValidBinding()) {
					if (sam.isValidBinding())
						return this.singleAbstractMethod = new ProblemMethodBinding(TypeConstants.ANONYMOUS_METHOD, null, ProblemReasons.IntersectionHasMultipleFunctionalInterfaces);
					else
						sam = method;
				}
			}
		}
		return this.singleAbstractMethod = sam; // I don't see a value in building the notional interface described in 9.8 - it appears just pedantic/normative - perhaps it plays a role in wildcard parameterized types ?
	}

	public boolean hasTypeBit(int bit) { // Stephan ??
		for (int i = 0; i < this.length; i++) {		
			if (this.intersectingTypes[i].hasTypeBit(bit))
				return true;
		}
		return false;
	}

	public char[] constantPoolName() {
		return " notional ".toCharArray(); //$NON-NLS-1$
	}

	public PackageBinding getPackage() {
		throw new UnsupportedOperationException(); // cannot be referred to
	}
	
	public ReferenceBinding[] getIntersectingTypes() {
		return this.intersectingTypes;
	}

	public ReferenceBinding superclass() {
		return this.intersectingTypes[0].isClass() ? this.intersectingTypes[0] : this.javaLangObject; 
	}
	
	public ReferenceBinding [] superInterfaces() {
		if (this.intersectingTypes[0].isClass()) {
			ReferenceBinding [] superInterfaces = new ReferenceBinding[this.length - 1];
			System.arraycopy(this.intersectingTypes, 1, superInterfaces, 0, this.length - 1);
			return superInterfaces;
		}
		return this.intersectingTypes;
	}
	/* Answer true if the receiver type can be assigned to the argument type (right)
	 */
	public boolean isCompatibleWith(TypeBinding right, Scope scope) {
		for (int i = 0; i < this.length; i++) {		
			if (this.intersectingTypes[i].isCompatibleWith(right, scope))
				return true;
		}
		return false;
	}

	public char[] qualifiedSourceName() {
		StringBuffer qualifiedSourceName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				qualifiedSourceName.append(this.intersectingTypes[i].qualifiedSourceName());
				if (i != this.length - 1)
					qualifiedSourceName.append(" & "); //$NON-NLS-1$
		}
		return qualifiedSourceName.toString().toCharArray();
	}

	public char[] sourceName() {
		StringBuffer srcName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				srcName.append(this.intersectingTypes[i].sourceName());
				if (i != this.length - 1)
					srcName.append(" & "); //$NON-NLS-1$
		}
		return srcName.toString().toCharArray();
	}

	public char[] readableName() {
		StringBuffer readableName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				readableName.append(this.intersectingTypes[i].readableName());
				if (i != this.length - 1)
					readableName.append(" & "); //$NON-NLS-1$
		}
		return readableName.toString().toCharArray();
	}
	public char[] shortReadableName() {
		StringBuffer shortReadableName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				shortReadableName.append(this.intersectingTypes[i].shortReadableName());
				if (i != this.length - 1)
					shortReadableName.append(" & "); //$NON-NLS-1$
		}
		return shortReadableName.toString().toCharArray();
	}
	public boolean isIntersectionCastType() {
		return true;
	}
	public int kind() {
		return Binding.INTERSECTION_CAST_TYPE;
	}
	public String debugName() {
		StringBuffer debugName = new StringBuffer(16);
		for (int i = 0; i < this.length; i++) {		
				debugName.append(this.intersectingTypes[i].debugName());
				if (i != this.length - 1)
					debugName.append(" & "); //$NON-NLS-1$
		}
		return debugName.toString();
	}
	public String toString() {
	    return debugName();
	}
}

/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.lookup;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.ast.CaseStatement;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;

public final class LocalTypeBinding extends NestedTypeBinding {
	final static char[] LocalTypePrefix = { '$', 'L', 'o', 'c', 'a', 'l', '$' };

	private InnerEmulationDependency[] dependents;
	public ArrayBinding[] localArrayBindings; // used to cache array bindings of various dimensions for this local type
	public CaseStatement switchCase; // from 1.4 on, local types should not be accessed across switch case blocks (52221)
	
public LocalTypeBinding(ClassScope scope, SourceTypeBinding enclosingType, CaseStatement switchCase) {
	super(
		new char[][] {CharOperation.concat(LocalTypePrefix, scope.referenceContext.name)},
		scope,
		enclosingType);
	
	if (this.sourceName == TypeDeclaration.ANONYMOUS_EMPTY_NAME)
		this.tagBits |= AnonymousTypeMask;
	else
		this.tagBits |= LocalTypeMask;
	this.switchCase = switchCase;
}
/* Record a dependency onto a source target type which may be altered
* by the end of the innerclass emulation. Later on, we will revisit
* all its dependents so as to update them (see updateInnerEmulationDependents()).
*/

public void addInnerEmulationDependent(BlockScope dependentScope, boolean wasEnclosingInstanceSupplied) {
	int index;
	if (dependents == null) {
		index = 0;
		dependents = new InnerEmulationDependency[1];
	} else {
		index = dependents.length;
		for (int i = 0; i < index; i++)
			if (dependents[i].scope == dependentScope)
				return; // already stored
		System.arraycopy(dependents, 0, (dependents = new InnerEmulationDependency[index + 1]), 0, index);
	}
	dependents[index] = new InnerEmulationDependency(dependentScope, wasEnclosingInstanceSupplied);
	//  System.out.println("Adding dependency: "+ new String(scope.enclosingType().readableName()) + " --> " + new String(this.readableName()));
}
/* Answer the receiver's constant pool name.
*
* NOTE: This method should only be used during/after code gen.
*/

public char[] constantPoolName() /* java/lang/Object */ {
	return constantPoolName;
}

ArrayBinding createArrayType(int dimensionCount) {
	if (localArrayBindings == null) {
		localArrayBindings = new ArrayBinding[] {new ArrayBinding(this, dimensionCount, scope.environment())};
		return localArrayBindings[0];
	}

	// find the cached array binding for this dimensionCount (if any)
	int length = localArrayBindings.length;
	for (int i = 0; i < length; i++)
		if (localArrayBindings[i].dimensions == dimensionCount)
			return localArrayBindings[i];

	// no matching array
	System.arraycopy(localArrayBindings, 0, localArrayBindings = new ArrayBinding[length + 1], 0, length); 
	return localArrayBindings[length] = new ArrayBinding(this, dimensionCount, scope.environment());
}

public char[] readableName() /*java.lang.Object,  p.X<T> */ {
    char[] readableName;
	if (isAnonymousType()) {
		if (superInterfaces == NoSuperInterfaces)
			readableName = CharOperation.concat(TypeConstants.ANONYM_PREFIX, superclass.readableName(), TypeConstants.ANONYM_SUFFIX);
		else
			readableName = CharOperation.concat(TypeConstants.ANONYM_PREFIX, superInterfaces[0].readableName(), TypeConstants.ANONYM_SUFFIX);
	} else if (isMemberType()) {
		readableName = CharOperation.concat(enclosingType().readableName(), this.sourceName, '.');
	} else {
		readableName = this.sourceName;
	}    
	TypeVariableBinding[] typeVars;
	if ((typeVars = this.typeVariables()) != NoTypeVariables) {
	    StringBuffer nameBuffer = new StringBuffer(10);
	    nameBuffer.append(readableName).append('<');
	    for (int i = 0, length = typeVars.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(typeVars[i].readableName());
	    }
	    nameBuffer.append('>');
	    int nameLength = nameBuffer.length();
		readableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, readableName, 0);
	}
	return readableName;
}

public char[] shortReadableName() /*Object*/ {
    char[] shortReadableName;
	if (isAnonymousType()) {
		if (superInterfaces == NoSuperInterfaces)
			shortReadableName = CharOperation.concat(TypeConstants.ANONYM_PREFIX, superclass.shortReadableName(), TypeConstants.ANONYM_SUFFIX);
		else
			shortReadableName = CharOperation.concat(TypeConstants.ANONYM_PREFIX, superInterfaces[0].shortReadableName(), TypeConstants.ANONYM_SUFFIX);
	} else if (isMemberType()) {
		shortReadableName = CharOperation.concat(enclosingType().shortReadableName(), sourceName, '.');
	} else {
		shortReadableName = sourceName;
	}
	TypeVariableBinding[] typeVars;
	if ((typeVars = this.typeVariables()) != NoTypeVariables) {
	    StringBuffer nameBuffer = new StringBuffer(10);
	    nameBuffer.append(shortReadableName).append('<');
	    for (int i = 0, length = typeVars.length; i < length; i++) {
	        if (i > 0) nameBuffer.append(',');
	        nameBuffer.append(typeVars[i].shortReadableName());
	    }
	    nameBuffer.append('>');
		int nameLength = nameBuffer.length();
		shortReadableName = new char[nameLength];
		nameBuffer.getChars(0, nameLength, shortReadableName, 0);	    
	}
	return shortReadableName;
}

// Record that the type is a local member type
public void setAsMemberType() {
	tagBits |= MemberTypeMask;
}

public void setConstantPoolName(char[] computedConstantPoolName) /* java/lang/Object */ {
	this.constantPoolName = computedConstantPoolName;
}

public char[] sourceName() {
	if (isAnonymousType()) {
		if (superInterfaces == NoSuperInterfaces)
			return CharOperation.concat(TypeConstants.ANONYM_PREFIX, superclass.sourceName(), TypeConstants.ANONYM_SUFFIX);
		else
			return CharOperation.concat(TypeConstants.ANONYM_PREFIX, superInterfaces[0].sourceName(), TypeConstants.ANONYM_SUFFIX);
			
	} else
		return sourceName;
}
public String toString() {
	if (isAnonymousType())
		return "Anonymous type : " + super.toString(); //$NON-NLS-1$
	if (isMemberType())
		return "Local member type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
	return "Local type : " + new String(sourceName()) + " " + super.toString(); //$NON-NLS-2$ //$NON-NLS-1$
}
/* Trigger the dependency mechanism forcing the innerclass emulation
* to be propagated to all dependent source types.
*/

public void updateInnerEmulationDependents() {
	if (dependents != null) {
		for (int i = 0; i < dependents.length; i++) {
			InnerEmulationDependency dependency = dependents[i];
			// System.out.println("Updating " + new String(this.readableName()) + " --> " + new String(dependency.scope.enclosingType().readableName()));
			dependency.scope.propagateInnerEmulation(this, dependency.wasEnclosingInstanceSupplied);
		}
	}
}
}

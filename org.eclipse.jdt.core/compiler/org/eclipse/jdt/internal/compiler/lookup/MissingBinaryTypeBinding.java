/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
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
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

public class MissingBinaryTypeBinding extends BinaryTypeBinding {

/**
 * Special constructor for constructing proxies of missing binary types (114349)
 * @param packageBinding
 * @param compoundName
 * @param environment
 */
public MissingBinaryTypeBinding(PackageBinding packageBinding, char[][] compoundName, LookupEnvironment environment) {
	this.compoundName = compoundName;
	computeId();
	this.tagBits |= TagBits.IsBinaryBinding | TagBits.HierarchyHasProblems;
	this.environment = environment;
	this.fPackage = packageBinding;
	this.fileName = CharOperation.concatWith(compoundName, '/');
	this.sourceName = compoundName[compoundName.length - 1]; // [java][util][Map$Entry]
	this.modifiers = ClassFileConstants.AccPublic;
	this.superclass = null; // will be fixed up using #setMissingSuperclass(...)
	this.superInterfaces = Binding.NO_SUPERINTERFACES;
	this.typeVariables = Binding.NO_TYPE_VARIABLES;
	this.memberTypes = Binding.NO_MEMBER_TYPES;
	this.fields = Binding.NO_FIELDS;
	this.methods = Binding.NO_METHODS;
}	
	
/**
 * Missing binary type will answer <code>false</code> to #isValidBinding()
 * @see org.eclipse.jdt.internal.compiler.lookup.Binding#problemId()
 */
public int problemId() {
	return ProblemReasons.NotFound;
}

/**
 * Only used to fixup the superclass hierarchy of proxy binary types
 * @param missingSuperclass
 * @see LookupEnvironment#cacheMissingBinaryType(char[][], CompilationUnitDeclaration)
 */
void setMissingSuperclass(ReferenceBinding missingSuperclass) {
	this.superclass = missingSuperclass;
}	
}

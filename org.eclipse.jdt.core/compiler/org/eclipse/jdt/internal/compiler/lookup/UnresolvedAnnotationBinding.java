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

public class UnresolvedAnnotationBinding extends AnnotationBinding {
	private LookupEnvironment env;

UnresolvedAnnotationBinding(ReferenceBinding type, ElementValuePair[] pairs, LookupEnvironment env) {
	super(type, pairs);
	this.env = env;
}

public ReferenceBinding getAnnotationType() {
	// the type is resolved when requested
	if (this.env != null) {
		// annotation type are never parameterized
		this.type = BinaryTypeBinding.resolveType(this.type, this.env, false);
		this.env = null;
		setMethodBindings();
	}
	return this.type;
}

public ElementValuePair[] getElementValuePairs() {
	if (this.env != null)
		getAnnotationType(); // resolve the annotation type & method bindings of each pair

	return this.pairs;
}
}
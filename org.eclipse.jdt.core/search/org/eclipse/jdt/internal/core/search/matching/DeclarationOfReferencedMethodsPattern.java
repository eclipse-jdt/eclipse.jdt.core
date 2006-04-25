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
package org.eclipse.jdt.internal.core.search.matching;

//import java.util.HashSet;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.compiler.util.SimpleSet;

public class DeclarationOfReferencedMethodsPattern extends MethodPattern {

protected IJavaElement enclosingElement;
protected SimpleSet knownMethods;

public DeclarationOfReferencedMethodsPattern(IJavaElement enclosingElement) {
	super(false, true, null, null, null, null, null, null, null, null, R_PATTERN_MATCH);

	this.enclosingElement = enclosingElement;
	this.knownMethods = new SimpleSet();
	((InternalSearchPattern)this).mustResolve = true;
}
}

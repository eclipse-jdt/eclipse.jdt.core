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

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.internal.core.util.SimpleSet;

public class DeclarationOfAccessedFieldsPattern extends FieldPattern {

protected IJavaElement enclosingElement;
protected SimpleSet knownFields;

public DeclarationOfAccessedFieldsPattern(IJavaElement enclosingElement) {
	super(false, true, true, null, null, null, null, null, R_PATTERN_MATCH);

	this.enclosingElement = enclosingElement;
	this.knownFields = new SimpleSet();
	((InternalSearchPattern)this).mustResolve = true;
}
}

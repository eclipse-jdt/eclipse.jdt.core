/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.codeassist.select;

import org.eclipse.jdt.internal.compiler.lookup.*;

public class SelectionNodeFound extends RuntimeException {
	public Binding binding;
public SelectionNodeFound() {
	this(null); // we found a problem in the selection node
}
public SelectionNodeFound(Binding binding) {
	this.binding = binding;
}
}

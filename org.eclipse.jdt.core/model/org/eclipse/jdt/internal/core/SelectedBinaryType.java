/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

/**
 * Handle returned by the selection engine representing a binary type.
 * The uniqueKey contains the genericTypeSignature of the selected type.
 * The boolean partialSection represents whether the type was selected without its type arguments.
 */
public class SelectedBinaryType extends BinaryType {
	
	String uniqueKey;
	boolean partialSelection;
	
	/*
	 * See class comments.
	 */
	public SelectedBinaryType(JavaElement parent, String name, String uniqueKey, boolean partialSelection) {
		super(parent, name);
		this.uniqueKey = uniqueKey;
		this.partialSelection = partialSelection;
	
	}

}

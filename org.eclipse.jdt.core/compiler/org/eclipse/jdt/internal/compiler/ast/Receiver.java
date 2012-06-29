/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
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

package org.eclipse.jdt.internal.compiler.ast;

import org.eclipse.jdt.core.compiler.CharOperation;

public class Receiver extends Argument {
	TypeReference qualifyingTypeReference;
	public Receiver(char[] name, long posNom, TypeReference typeReference, TypeReference qualifyingTypeReference, int modifiers) {
		super(qualifyingTypeReference == null ? name : CharOperation.concatWith(qualifyingTypeReference.getTypeName(), name, '.'), posNom, typeReference, modifiers);
		this.qualifyingTypeReference = qualifyingTypeReference;
	}
	public boolean isReceiver() {
		return true;
	}
}

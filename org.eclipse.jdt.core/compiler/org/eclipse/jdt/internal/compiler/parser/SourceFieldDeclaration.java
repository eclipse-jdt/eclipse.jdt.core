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
package org.eclipse.jdt.internal.compiler.parser;

import org.eclipse.jdt.internal.compiler.ast.Expression;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;

public class SourceFieldDeclaration extends FieldDeclaration {
	public int fieldEndPosition;
public SourceFieldDeclaration(
	Expression initialization, 
	char[] name, 
	int sourceStart, 
	int sourceEnd) {
	super(initialization, name, sourceStart, sourceEnd);
}
}

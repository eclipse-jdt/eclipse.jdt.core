/*******************************************************************************
 * Copyright (c) 2002 IBM Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.parser;

public class SourceInitializer extends SourceField {
public SourceInitializer(
	int declarationStart, 
	int modifiers) {
	super(declarationStart, modifiers, null, null, -1, -1, null);
}

public void setDeclarationSourceEnd(int declarationSourceEnd) {
	this.declarationEnd = declarationSourceEnd;
}

public String toString(int tab) {
	if (modifiers == AccStatic) {
		return tabString(tab) + "static {}";
	}
	return tabString(tab) + "{}";
}
}

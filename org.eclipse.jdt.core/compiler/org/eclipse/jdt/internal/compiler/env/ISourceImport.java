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
package org.eclipse.jdt.internal.compiler.env;

public interface ISourceImport {

/**
 * Answer the source end position of the import declaration.
 */

int getDeclarationSourceEnd();
/**
 * Answer the source start position of the import declaration.
 */

int getDeclarationSourceStart();

/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 * Since Java 1.5, static imports can be defined.
 */ 
int getModifiers();

/**
 * Answer the name of the import.
 * A name is a simple name or a qualified, dot separated name.
 * For example, Hashtable or java.util.Hashtable.
 */
char[] getName();	

/**
 * Answer whether the import is on demand or not
 * On demand import names have no trailing star
 */
boolean onDemand();
}

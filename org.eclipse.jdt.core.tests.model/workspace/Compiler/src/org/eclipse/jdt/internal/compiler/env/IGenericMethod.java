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

public interface IGenericMethod {
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
// We have added AccDeprecated
int getModifiers();

/**
 * Answer the name of the method.
 *
 * For a constructor, answer <init> & <clinit> for a clinit method.
 */
char[] getSelector();

boolean isConstructor();

/**
 * Answer the names of the argument
 * or null if the argument names are not available.
 */

char[][] getArgumentNames();
}

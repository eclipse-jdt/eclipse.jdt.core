/*******************************************************************************
 * Copyright (c) 2000, 2001, 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.codeassist;

public interface RelevanceConstants {
	
	int R_DEFAULT = 0;
	int R_INTERESTING = 5;
	int R_CASE = 10;
	int R_EXACT_NAME = 4;
	int R_EXPECTED_TYPE = 20;
	int R_EXACT_EXPECTED_TYPE = 30;
	int R_INTERFACE = 20;
	int R_CLASS = 20;
	int R_EXCEPTION = 20;
	int R_ABSTRACT_METHOD = 20;
	int R_NON_STATIC = 10;
	int R_UNQUALIFIED = 3;
	int R_NAME_FIRST_PREFIX = 6;
	int R_NAME_PREFIX = 5;
	int R_NAME_FIRST_SUFFIX = 4;
	int R_NAME_SUFFIX = 3;
	
	
}

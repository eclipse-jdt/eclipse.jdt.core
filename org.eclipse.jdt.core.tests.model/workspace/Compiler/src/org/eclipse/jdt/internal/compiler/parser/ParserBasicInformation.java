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
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int ERROR_SYMBOL = 108,
		MAX_NAME_LENGTH = 41,
		NUM_STATES = 932,

		NT_OFFSET = 108,
		SCOPE_UBOUND = 125,
		SCOPE_SIZE = 126,
		LA_STATE_OFFSET = 11429,
		MAX_LA = 1,
		NUM_RULES = 670,
		NUM_TERMINALS = 108,
		NUM_NON_TERMINALS = 298,
		NUM_SYMBOLS = 406,
		START_STATE = 1999,
		EOFT_SYMBOL = 68,
		EOLT_SYMBOL = 68,
		ACCEPT_ACTION = 11428,
		ERROR_ACTION = 11429;
}
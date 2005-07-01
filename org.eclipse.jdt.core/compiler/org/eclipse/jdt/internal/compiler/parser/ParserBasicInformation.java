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
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int ERROR_SYMBOL = 110,
		MAX_NAME_LENGTH = 41,
		NUM_STATES = 955,

		NT_OFFSET = 110,
		SCOPE_UBOUND = 131,
		SCOPE_SIZE = 132,
		LA_STATE_OFFSET = 12619,
		MAX_LA = 1,
		NUM_RULES = 691,
		NUM_TERMINALS = 110,
		NUM_NON_TERMINALS = 306,
		NUM_SYMBOLS = 416,
		START_STATE = 1466,
		EOFT_SYMBOL = 66,
		EOLT_SYMBOL = 66,
		ACCEPT_ACTION = 12618,
		ERROR_ACTION = 12619;
}

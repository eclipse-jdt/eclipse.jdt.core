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
		NUM_STATES = 939,

		NT_OFFSET = 108,
		SCOPE_UBOUND = 130,
		SCOPE_SIZE = 131,
		LA_STATE_OFFSET = 11754,
		MAX_LA = 1,
		NUM_RULES = 680,
		NUM_TERMINALS = 108,
		NUM_NON_TERMINALS = 303,
		NUM_SYMBOLS = 411,
		START_STATE = 1776,
		EOFT_SYMBOL = 69,
		EOLT_SYMBOL = 69,
		ACCEPT_ACTION = 11753,
		ERROR_ACTION = 11754;
}
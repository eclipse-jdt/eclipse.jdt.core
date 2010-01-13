/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
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
		MAX_NAME_LENGTH = 50,
		NUM_STATES = 1052,

		NT_OFFSET = 110,
		SCOPE_UBOUND = 222,
		SCOPE_SIZE = 223,
		LA_STATE_OFFSET = 15177,
		MAX_LA = 1,
		NUM_RULES = 755,
		NUM_TERMINALS = 110,
		NUM_NON_TERMINALS = 334,
		NUM_SYMBOLS = 444,
		START_STATE = 1162,
		EOFT_SYMBOL = 66,
		EOLT_SYMBOL = 66,
		ACCEPT_ACTION = 15176,
		ERROR_ACTION = 15177;
}

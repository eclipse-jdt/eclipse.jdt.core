/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
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

	public final static int

	ERROR_SYMBOL = 125,
					MAX_NAME_LENGTH = 41,
					NUM_STATES = 1129,

					NT_OFFSET = 125,
					SCOPE_UBOUND = 290,
					SCOPE_SIZE = 291,
					LA_STATE_OFFSET = 16374,
					MAX_LA = 1,
					NUM_RULES = 824,
					NUM_TERMINALS = 125,
					NUM_NON_TERMINALS = 376,
					NUM_SYMBOLS = 501,
					START_STATE = 871,
					EOFT_SYMBOL = 60,
					EOLT_SYMBOL = 60,
					ACCEPT_ACTION = 16373,
					ERROR_ACTION = 16374;
}

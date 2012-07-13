/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/
public interface ParserBasicInformation {

	int ERROR_SYMBOL = 115,
		MAX_NAME_LENGTH = 50,
		NUM_STATES = 1102,

		NT_OFFSET = 115,
		SCOPE_UBOUND = 240,
		SCOPE_SIZE = 241,
		LA_STATE_OFFSET = 16678,
		MAX_LA = 1,
		NUM_RULES = 802,
		NUM_TERMINALS = 115,
		NUM_NON_TERMINALS = 358,
		NUM_SYMBOLS = 473,
		START_STATE = 868,
		EOFT_SYMBOL = 67,
		EOLT_SYMBOL = 67,
		ACCEPT_ACTION = 16677,
		ERROR_ACTION = 16678;
}

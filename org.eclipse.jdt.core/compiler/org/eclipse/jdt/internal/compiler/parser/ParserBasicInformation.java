/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
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

	public final static int
      ERROR_SYMBOL      = 105,
      MAX_NAME_LENGTH   = 36,
      NUM_STATES        = 596,
      NT_OFFSET         = 105,
      SCOPE_UBOUND      = 63,
      SCOPE_SIZE        = 64,
      LA_STATE_OFFSET   = 5979,
      MAX_LA            = 1,
      NUM_RULES         = 431,
      NUM_TERMINALS     = 105,
      NUM_NON_TERMINALS = 201,
      NUM_SYMBOLS       = 306,
      START_STATE       = 527,
      EOFT_SYMBOL       = 54,
      EOLT_SYMBOL       = 54,
      ACCEPT_ACTION     = 5978,
      ERROR_ACTION      = 5979;
}

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
package org.eclipse.jdt.internal.compiler.parser;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/

public interface ParserBasicInformation {

    public final static int
      ERROR_SYMBOL      = 309,
      MAX_NAME_LENGTH   = 36,
      NUM_STATES        = 594,
      NT_OFFSET         = 310,
      SCOPE_UBOUND      = -1,
      SCOPE_SIZE        = 0,
      LA_STATE_OFFSET   = 16753,
      MAX_LA            = 1,
      NUM_RULES         = 438,
      NUM_TERMINALS     = 105,
      NUM_NON_TERMINALS = 205,
      NUM_SYMBOLS       = 310,
      START_STATE       = 16710,
      EOFT_SYMBOL       = 158,
      EOLT_SYMBOL       = 158,
      ACCEPT_ACTION     = 16752,
      ERROR_ACTION      = 16753;
}

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
      ERROR_SYMBOL      = 307,
      MAX_NAME_LENGTH   = 36,
      NUM_STATES        = 591,
      NT_OFFSET         = 308,
      SCOPE_UBOUND      = -1,
      SCOPE_SIZE        = 0,
      LA_STATE_OFFSET   = 16966,
      MAX_LA            = 1,
      NUM_RULES         = 436,
      NUM_TERMINALS     = 105,
      NUM_NON_TERMINALS = 203,
      NUM_SYMBOLS       = 308,
      START_STATE       = 12260,
      EOFT_SYMBOL       = 158,
      EOLT_SYMBOL       = 158,
      ACCEPT_ACTION     = 16965,
      ERROR_ACTION      = 16966;
}

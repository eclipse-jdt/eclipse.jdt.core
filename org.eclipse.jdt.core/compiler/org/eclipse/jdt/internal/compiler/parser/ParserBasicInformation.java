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
      NT_OFFSET         = 308,
      SCOPE_UBOUND      = -1,
      SCOPE_SIZE        = 0,
      LA_STATE_OFFSET   = 17759,
      MAX_LA            = 1,
      NUM_RULES         = 437,
      NUM_TERMINALS     = 104,
      NUM_NON_TERMINALS = 204,
      NUM_SYMBOLS       = 308,
      START_STATE       = 16034,
      EOFT_SYMBOL       = 114,
      EOLT_SYMBOL       = 105,
      ACCEPT_ACTION     = 17758,
      ERROR_ACTION      = 17759;
}

package org.eclipse.jdt.internal.compiler.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

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
      LA_STATE_OFFSET   = 16965,
      MAX_LA            = 1,
      NUM_RULES         = 435,
      NUM_TERMINALS     = 105,
      NUM_NON_TERMINALS = 203,
      NUM_SYMBOLS       = 308,
      START_STATE       = 12259,
      EOFT_SYMBOL       = 158,
      EOLT_SYMBOL       = 158,
      ACCEPT_ACTION     = 16964,
      ERROR_ACTION      = 16965;
}

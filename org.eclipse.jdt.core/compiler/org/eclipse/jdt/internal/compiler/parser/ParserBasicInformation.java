package org.eclipse.jdt.internal.compiler.parser;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/*An interface that contains static declarations for some basic information
 about the parser such as the number of rules in the grammar, the starting state, etc...*/

public interface ParserBasicInformation {

	public final static int ERROR_SYMBOL = 304,
	  MAX_NAME_LENGTH   = 36,
	  NUM_STATES        = 586,
	  NT_OFFSET         = 305,
	  SCOPE_UBOUND      = -1,
	  SCOPE_SIZE        = 0,
	  LA_STATE_OFFSET   = 17754,
	  MAX_LA            = 1,
	  NUM_RULES         = 431,
	  NUM_TERMINALS     = 104,
	  NUM_NON_TERMINALS = 201,
	  NUM_SYMBOLS       = 305,
	  START_STATE       = 12614,
	  EOFT_SYMBOL       = 156,
	  EOLT_SYMBOL       = 156,
	  ACCEPT_ACTION     = 17753,
	  ERROR_ACTION      = 17754;
}

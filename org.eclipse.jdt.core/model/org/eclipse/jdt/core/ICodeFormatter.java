package org.eclipse.jdt.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

/**
 * Specification for a generic source code formatter. Client plug-ins can contribute
 * an implementation for an ICodeFormatter, through the extension point "org.eclipse.jdt.core.codeFormatter".
 * In case none is found, a default formatter can be provided through the ToolFactory.
 * 
 * @see ToolFactory#createCodeFormatter()
 * @see ToolFactory#createDefaultCodeFormatter()
 * @since 2.0
 */
public interface ICodeFormatter {

	/** 
	 * Formats the String <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * 
	 * Formatting can be passed along an initial indentation level
	 * to shift left/right the entire source fragment. An initial indentation
	 * level of zero has no effect.
	 * Furthermore, the formatting can also map character-based source
	 * positions, so as to relocate elements associated with the original
	 * source. It updates the positions array with updated positions.
	 * 
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level
	 * @param positions the array of positions to map
	 * @return the formatted ouput string.
	 */
	String format(String string, int indentationLevel, int[] positions);
}

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
	 * Formats the char array <code>sourceString</code>,
	 * and returns a string containing the formatted version.
	 * The positions array is modified to contain the mapped positions.
	 * 
	 * @param string the string to format
	 * @param indentationLevel the initial indentation level
	 * @param positions the array of positions to map
	 * @return the formatted ouput string.
	 */
	String format(String string, int indentationLevel, int[] positions);
}

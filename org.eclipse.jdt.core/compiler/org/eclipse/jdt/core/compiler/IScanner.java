package org.eclipse.jdt.core.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
 /**
  * Definition of a Java scanner, as returned by the <code>ToolFactory</code>.
  * The scanner is responsible for tokenizing a given source, providing information about
  * the nature of the token read, its positions and source equivalent.
  * 
  * When the scanner has finished tokenizing, it answers an EOF token (<code>
  * ITerminalSymbols#TokenNameEOF</code>.
  * 
  * When encountering lexical errors, an <code>InvalidInputException</code> is thrown.
  * 
  * @see ToolFactory
  * @see ITerminalSymbols
  * @since 2.0
  */
public interface IScanner {

	/**
	 * Answers the current identifier source, after unicode escape sequences have
	 * been translated into unicode characters.
	 * e.g. if original source was <code>\\u0061bc</code> then it will answer <code>abc</code>.
	 */
	char[] getCurrentTokenSource();

	/**
	 * Answers the starting position of the current token inside the original source.
	 * This position is zero-based and inclusive. It corresponds to the position of the first character 
	 * which is part of this token. If this character was a unicode escape sequence, it points at the first 
	 * character of this sequence.
	 */
	int getCurrentTokenStartPosition();

	/**
	 * Answers the ending position of the current token inside the original source.
	 * This position is zero-based and inclusive. It corresponds to the position of the last character
	 * which is part of this token. If this character was a unicode escape sequence, it points at the last 
	 * character of this sequence.
	 */
	int getCurrentTokenEndPosition();

	/**
	 * Answers the starting position of a given line number. This line has to have been encountered
	 * already in the tokenization process (i.e. it cannot be used to compute positions of lines beyond
	 * current token). Once the entire source has been processed, it can be used without any limit.
	 * Line starting positions are zero-based, and start immediately after the previous line separator (if any).
	 */
	int getLineStart(int lineNumber);

	/**
	 * Answers the ending position of a given line number. This line has to have been encountered
	 * already in the tokenization process (i.e. it cannot be used to compute positions of lines beyond
	 * current token). Once the entire source has been processed, it can be used without any limit.
	* Line ending positions are zero-based, and correspond to the last character of the line separator 
	* (in case multi-character line separators).	 
	**/
	int getLineEnd(int lineNumber);

	/**
	 * Answers an array of the ending positions of the lines encountered so far. Line ending positions
	 * are zero-based, and correspond to the last character of the line separator (in case multi-character
	 * line separators).
	 */
	int[] getLineEnds();

	/**
	 * Read the next token in the source, and answers its ID as specified by <code>ITerminalSymbols</code>.
	 * Note that the actual token ID values are subject to change if new keywords were added to the language
	 * (i.e. 'assert' keyword in 1.4).
	 * 
	 * @throws InvalidInputException - in case a lexical error was detected while reading the current token
	 */
	int getNextToken() throws InvalidInputException;

	/**
	 * Reposition the scanner on some portion of the original source. Once reaching the given <code>endPosition</code>
	 * it will anser EOF tokens (<code>ITerminalSymbols.TokenNameEOF</code>).
	 */
	void resetTo(int startPosition, int endPosition);

	/**
	 * Set the scanner source to process. By default, the scanner will consider starting at the beginning of the
	 * source until it reaches its end.
	 */
	void setSourceBuffer(char[] source);
}

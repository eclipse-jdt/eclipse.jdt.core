/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.util.Iterator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.StringLiteral;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * IMPORTANT NOTE: Internal Scanner implementation. It is mirrored in 
 * org.eclipse.jdt.core.compiler public package where it is API. 
 * The mirror implementation is using the backward compatible ITerminalSymbols constant 
 * definitions (stable with 2.0), whereas the internal implementation uses TerminalTokens 
 * which constant values reflect the latest parser generation state.
 */
public class Scanner implements TerminalTokens {

	/* APIs ares
	 - getNextToken() which return the current type of the token
	   (this value is not memorized by the scanner)
	 - getCurrentTokenSource() which provides with the token "REAL" source
	   (aka all unicode have been transformed into a correct char)
	 - sourceStart gives the position into the stream
	 - currentPosition-1 gives the sourceEnd position into the stream 
	*/
	private long sourceLevel;
	// 1.4 feature 
	public boolean useAssertAsAnIndentifier = false;
	//flag indicating if processed source contains occurrences of keyword assert 
	public boolean containsAssertKeyword = false; 
	
	// 1.5 feature
	public boolean useEnumAsAnIndentifier = false;
	
	public boolean recordLineSeparator = false;
	public char currentCharacter;
	public int startPosition;
	public int currentPosition;
	public int initialPosition, eofPosition;
	// after this position eof are generated instead of real token from the source

	public boolean tokenizeComments = false;
	public boolean tokenizeWhiteSpace = false;

	//source should be viewed as a window (aka a part)
	//of a entire very large stream
	public char source[];

	//unicode support
	public char[] withoutUnicodeBuffer;
	public int withoutUnicodePtr; //when == 0 ==> no unicode in the current token
	public boolean unicodeAsBackSlash = false;

	public boolean scanningFloatLiteral = false;

	//support for /** comments
	public int[] commentStops = new int[10];
	public int[] commentStarts = new int[10];
	public int commentPtr = -1; // no comment test with commentPtr value -1
	protected int lastCommentLinePosition = -1;
	
	// task tag support
	public char[][] foundTaskTags = null;
	public char[][] foundTaskMessages;
	public char[][] foundTaskPriorities = null;
	public int[][] foundTaskPositions;
	public int foundTaskCount = 0;
	public char[][] taskTags = null;
	public char[][] taskPriorities = null;
	public boolean isTaskCaseSensitive = true;
	
	//diet parsing support - jump over some method body when requested
	public boolean diet = false;

	//support for the  poor-line-debuggers ....
	//remember the position of the cr/lf
	public int[] lineEnds = new int[250];
	public int linePtr = -1;
	public boolean wasAcr = false;

	public static final String END_OF_SOURCE = "End_Of_Source"; //$NON-NLS-1$

	public static final String INVALID_HEXA = "Invalid_Hexa_Literal"; //$NON-NLS-1$
	public static final String INVALID_OCTAL = "Invalid_Octal_Literal"; //$NON-NLS-1$
	public static final String INVALID_CHARACTER_CONSTANT = "Invalid_Character_Constant";  //$NON-NLS-1$
	public static final String INVALID_ESCAPE = "Invalid_Escape"; //$NON-NLS-1$
	public static final String INVALID_INPUT = "Invalid_Input"; //$NON-NLS-1$
	public static final String INVALID_UNICODE_ESCAPE = "Invalid_Unicode_Escape"; //$NON-NLS-1$
	public static final String INVALID_FLOAT = "Invalid_Float_Literal"; //$NON-NLS-1$

	public static final String NULL_SOURCE_STRING = "Null_Source_String"; //$NON-NLS-1$
	public static final String UNTERMINATED_STRING = "Unterminated_String"; //$NON-NLS-1$
	public static final String UNTERMINATED_COMMENT = "Unterminated_Comment"; //$NON-NLS-1$
	public static final String INVALID_CHAR_IN_STRING = "Invalid_Char_In_String"; //$NON-NLS-1$
	public static final String INVALID_DIGIT = "Invalid_Digit"; //$NON-NLS-1$	

	//----------------optimized identifier managment------------------
	static final char[] charArray_a = new char[] {'a'}, 
		charArray_b = new char[] {'b'}, 
		charArray_c = new char[] {'c'}, 
		charArray_d = new char[] {'d'}, 
		charArray_e = new char[] {'e'}, 
		charArray_f = new char[] {'f'}, 
		charArray_g = new char[] {'g'}, 
		charArray_h = new char[] {'h'}, 
		charArray_i = new char[] {'i'}, 
		charArray_j = new char[] {'j'}, 
		charArray_k = new char[] {'k'}, 
		charArray_l = new char[] {'l'}, 
		charArray_m = new char[] {'m'}, 
		charArray_n = new char[] {'n'}, 
		charArray_o = new char[] {'o'}, 
		charArray_p = new char[] {'p'}, 
		charArray_q = new char[] {'q'}, 
		charArray_r = new char[] {'r'}, 
		charArray_s = new char[] {'s'}, 
		charArray_t = new char[] {'t'}, 
		charArray_u = new char[] {'u'}, 
		charArray_v = new char[] {'v'}, 
		charArray_w = new char[] {'w'}, 
		charArray_x = new char[] {'x'}, 
		charArray_y = new char[] {'y'}, 
		charArray_z = new char[] {'z'}; 

	static final char[] initCharArray = 
		new char[] {'\u0000', '\u0000', '\u0000', '\u0000', '\u0000', '\u0000'}; 
	static final int TableSize = 30, InternalTableSize = 6; //30*6 = 180 entries
	public static final int OptimizedLength = 6;
	public /*static*/ final char[][][][] charArray_length = 
		new char[OptimizedLength][TableSize][InternalTableSize][]; 
	// support for detecting non-externalized string literals
	public NLSLine currentLine= null;
	public static final String TAG_PREFIX= "//$NON-NLS-"; //$NON-NLS-1$
	public static final int TAG_PREFIX_LENGTH= TAG_PREFIX.length();
	public static final String TAG_POSTFIX= "$"; //$NON-NLS-1$
	public static final int TAG_POSTFIX_LENGTH= TAG_POSTFIX.length();
	public StringLiteral[] nonNLSStrings = null;
	public boolean checkNonExternalizedStringLiterals = false;
	public boolean wasNonExternalizedStringLiteral = false;
	
	// generic support
	public boolean returnOnlyGreater = false;
	
	/*static*/ {
		for (int i = 0; i < 6; i++) {
			for (int j = 0; j < TableSize; j++) {
				for (int k = 0; k < InternalTableSize; k++) {
					this.charArray_length[i][j][k] = initCharArray;
				}
			}
		}
	}
	static int newEntry2 = 0, 
		newEntry3 = 0, 
		newEntry4 = 0, 
		newEntry5 = 0, 
		newEntry6 = 0;

	public static final int RoundBracket = 0;
	public static final int SquareBracket = 1;
	public static final int CurlyBracket = 2;	
	public static final int BracketKinds = 3;

public Scanner() {
	this(false /*comment*/, false /*whitespace*/, false /*nls*/, ClassFileConstants.JDK1_3 /*sourceLevel*/, null/*taskTag*/, null/*taskPriorities*/, true /*taskCaseSensitive*/);
}

public Scanner(
	boolean tokenizeComments, 
	boolean tokenizeWhiteSpace, 
	boolean checkNonExternalizedStringLiterals, 
	long sourceLevel,
	char[][] taskTags,
	char[][] taskPriorities,
	boolean isTaskCaseSensitive) {

	this.eofPosition = Integer.MAX_VALUE;
	this.tokenizeComments = tokenizeComments;
	this.tokenizeWhiteSpace = tokenizeWhiteSpace;
	this.checkNonExternalizedStringLiterals = checkNonExternalizedStringLiterals;
	this.sourceLevel = sourceLevel;
	this.taskTags = taskTags;
	this.taskPriorities = taskPriorities;
	this.isTaskCaseSensitive = isTaskCaseSensitive;
}

public  final boolean atEnd() {
	// This code is not relevant if source is 
	// Only a part of the real stream input

	return this.source.length == this.currentPosition;
}

private void checkNonExternalizedString() {
	if (this.currentLine == null) 
		return;
	parseTags(this.currentLine);
}

// chech presence of task: tags
// TODO (frederic) see if we need to take unicode characters into account...
public void checkTaskTag(int commentStart, int commentEnd) {
	char[] src = this.source;
	
	// only look for newer task: tags
	if (this.foundTaskCount > 0
		&& this.foundTaskPositions[this.foundTaskCount - 1][0] >= commentStart) {
		return;
	}
	int foundTaskIndex = this.foundTaskCount;
	char previous = src[commentStart+1]; // should be '*' or '/'
	nextChar : for (
		int i = commentStart + 2; i < commentEnd && i < this.eofPosition; i++) {
		char[] tag = null;
		char[] priority = null;
		// check for tag occurrence only if not ambiguous with javadoc tag
		if (previous != '@') {
			nextTag : for (int itag = 0; itag < this.taskTags.length; itag++) {
				tag = this.taskTags[itag];
				int tagLength = tag.length;
				if (tagLength == 0) continue nextTag;
	
				// ensure tag is not leaded with letter if tag starts with a letter
				if (Character.isJavaIdentifierStart(tag[0])) {
					if (Character.isJavaIdentifierPart(previous)) {
						continue nextTag;
					}
				}
	
				for (int t = 0; t < tagLength; t++) {
					char sc, tc;
					int x = i+t;
					if (x >= this.eofPosition || x >= commentEnd) continue nextTag;
					if ((sc = src[i + t]) != (tc = tag[t])) { 																					// case sensitive check
						if (this.isTaskCaseSensitive || (Character.toLowerCase(sc) != Character.toLowerCase(tc))) { 	// case insensitive check
							continue nextTag;
						}
					}
				}
				// ensure tag is not followed with letter if tag finishes with a letter
				if (i+tagLength < commentEnd && Character.isJavaIdentifierPart(src[i+tagLength-1])) {
					if (Character.isJavaIdentifierPart(src[i + tagLength]))
						continue nextTag;
				}
				if (this.foundTaskTags == null) {
					this.foundTaskTags = new char[5][];
					this.foundTaskMessages = new char[5][];
					this.foundTaskPriorities = new char[5][];
					this.foundTaskPositions = new int[5][];
				} else if (this.foundTaskCount == this.foundTaskTags.length) {
					System.arraycopy(this.foundTaskTags, 0, this.foundTaskTags = new char[this.foundTaskCount * 2][], 0, this.foundTaskCount);
					System.arraycopy(this.foundTaskMessages, 0, this.foundTaskMessages = new char[this.foundTaskCount * 2][], 0, this.foundTaskCount);
					System.arraycopy(this.foundTaskPriorities, 0, this.foundTaskPriorities = new char[this.foundTaskCount * 2][], 0, this.foundTaskCount);
					System.arraycopy(this.foundTaskPositions, 0, this.foundTaskPositions = new int[this.foundTaskCount * 2][], 0, this.foundTaskCount);
				}
				
				priority = this.taskPriorities != null && itag < this.taskPriorities.length
							? this.taskPriorities[itag]
							: null;
				
				this.foundTaskTags[this.foundTaskCount] = tag;
				this.foundTaskPriorities[this.foundTaskCount] = priority;
				this.foundTaskPositions[this.foundTaskCount] = new int[] { i, i + tagLength - 1 };
				this.foundTaskMessages[this.foundTaskCount] = CharOperation.NO_CHAR;
				this.foundTaskCount++;
				i += tagLength - 1; // will be incremented when looping
				break nextTag;
			}
		}
		previous = src[i];
	}
	for (int i = foundTaskIndex; i < this.foundTaskCount; i++) {
		// retrieve message start and end positions
		int msgStart = this.foundTaskPositions[i][0] + this.foundTaskTags[i].length;
		int max_value = i + 1 < this.foundTaskCount
				? this.foundTaskPositions[i + 1][0] - 1
				: commentEnd - 1;
		// at most beginning of next task
		if (max_value < msgStart) {
			max_value = msgStart; // would only occur if tag is before EOF.
		}
		int end = -1;
		char c;
		for (int j = msgStart; j < max_value; j++) {
			if ((c = src[j]) == '\n' || c == '\r') {
				end = j - 1;
				break;
			}
		}
		if (end == -1) {
			for (int j = max_value; j > msgStart; j--) {
				if ((c = src[j]) == '*') {
					end = j - 1;
					break;
				}
			}
			if (end == -1)
				end = max_value;
		}
		if (msgStart == end)
			continue; // empty
		// trim the message
		while (CharOperation.isWhitespace(src[end]) && msgStart <= end)
			end--;
		while (CharOperation.isWhitespace(src[msgStart]) && msgStart <= end)
			msgStart++;
		// update the end position of the task
		this.foundTaskPositions[i][1] = end;
		// get the message source
		final int messageLength = end - msgStart + 1;
		char[] message = new char[messageLength];
		System.arraycopy(src, msgStart, message, 0, messageLength);
		this.foundTaskMessages[i] = message;
	}
}
public char[] getCurrentIdentifierSource() {
	//return the token REAL source (aka unicodes are precomputed)

	char[] result;
	if (this.withoutUnicodePtr != 0) {
		//0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(
			this.withoutUnicodeBuffer, 
			1, 
			result = new char[this.withoutUnicodePtr], 
			0, 
			this.withoutUnicodePtr); 
	} else {
		int length = this.currentPosition - this.startPosition;
		if (length == this.source.length) return this.source;
		switch (length) { // see OptimizedLength
			case 1 :
				return optimizedCurrentTokenSource1();
			case 2 :
				return optimizedCurrentTokenSource2();
			case 3 :
				return optimizedCurrentTokenSource3();
			case 4 :
				return optimizedCurrentTokenSource4();
			case 5 :
				return optimizedCurrentTokenSource5();
			case 6 :
				return optimizedCurrentTokenSource6();
		}
		//no optimization
		System.arraycopy(this.source, this.startPosition, result = new char[length], 0, length);
	}
	return result;
}
public int getCurrentTokenEndPosition(){
	return this.currentPosition - 1;
}
public final char[] getCurrentTokenSource() {
	// Return the token REAL source (aka unicodes are precomputed)

	char[] result;
	if (this.withoutUnicodePtr != 0)
		// 0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(
			this.withoutUnicodeBuffer, 
			1, 
			result = new char[this.withoutUnicodePtr], 
			0, 
			this.withoutUnicodePtr); 
	else {
		int length;
		System.arraycopy(
			this.source, 
			this.startPosition, 
			result = new char[length = this.currentPosition - this.startPosition], 
			0, 
			length); 
	}
	return result;
}
public final char[] getCurrentTokenSourceString() {
	//return the token REAL source (aka unicodes are precomputed).
	//REMOVE the two " that are at the beginning and the end.

	char[] result;
	if (this.withoutUnicodePtr != 0)
		//0 is used as a fast test flag so the real first char is in position 1
		System.arraycopy(this.withoutUnicodeBuffer, 2,
		//2 is 1 (real start) + 1 (to jump over the ")
		result = new char[this.withoutUnicodePtr - 2], 0, this.withoutUnicodePtr - 2);
	else {
		int length;
		System.arraycopy(
			this.source, 
			this.startPosition + 1, 
			result = new char[length = this.currentPosition - this.startPosition - 2], 
			0, 
			length); 
	}
	return result;
}

public final char[] getRawTokenSource() {
	int length = this.currentPosition - this.startPosition;
	char[] tokenSource = new char[length];
	System.arraycopy(this.source, this.startPosition, tokenSource, 0, length);
	return tokenSource;	
}
	
public final char[] getRawTokenSourceEnd() {
	int length = this.eofPosition - this.currentPosition - 1;
	char[] sourceEnd = new char[length];
	System.arraycopy(this.source, this.currentPosition, sourceEnd, 0, length);
	return sourceEnd;	
}
	
public int getCurrentTokenStartPosition(){
	return this.startPosition;
}
/*
 * Search the source position corresponding to the end of a given line number
 *
 * Line numbers are 1-based, and relative to the scanner initialPosition. 
 * Character positions are 0-based.
 *
 * In case the given line number is inconsistent, answers -1.
 */
public final int getLineEnd(int lineNumber) {

	if (this.lineEnds == null) 
		return -1;
	if (lineNumber > this.lineEnds.length+1) 
		return -1;
	if (lineNumber <= 0) 
		return -1;
	if (lineNumber == this.lineEnds.length + 1) 
		return this.eofPosition;
	return this.lineEnds[lineNumber-1]; // next line start one character behind the lineEnd of the previous line
}

public final int[] getLineEnds() {
	//return a bounded copy of this.lineEnds 

	int[] copy;
	System.arraycopy(this.lineEnds, 0, copy = new int[this.linePtr + 1], 0, this.linePtr + 1);
	return copy;
}

/**
 * Search the source position corresponding to the beginning of a given line number
 *
 * Line numbers are 1-based, and relative to the scanner initialPosition. 
 * Character positions are 0-based.
 *
 * e.g.	getLineStart(1) --> 0	indicates that the first line starts at character 0.
 *
 * In case the given line number is inconsistent, answers -1.
 * 
 * @param lineNumber int
 * @return int
 */
public final int getLineStart(int lineNumber) {

	if (this.lineEnds == null) 
		return -1;
	if (lineNumber > this.lineEnds.length + 1) 
		return -1;
	if (lineNumber <= 0) 
		return -1;
	
	if (lineNumber == 1) 
		return this.initialPosition;
	return this.lineEnds[lineNumber-2]+1; // next line start one character behind the lineEnd of the previous line
}
public final int getNextChar() {
	try {
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				return -1;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);

			this.unicodeAsBackSlash = this.currentCharacter == '\\';

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
			    unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
			return this.currentCharacter;

		} //-------------end unicode traitement--------------
		else {
			this.unicodeAsBackSlash = false;
			if (this.withoutUnicodePtr != 0) {
			    unicodeStoreAt(++this.withoutUnicodePtr);
			}
			return this.currentCharacter;
		}
	} catch (IndexOutOfBoundsException e) {
		return -1;
	}
}
public final boolean getNextChar(char testedChar) {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is == to the testedChar
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	if (this.currentPosition >= this.source.length) { // handle the obvious case upfront
		this.unicodeAsBackSlash = false;
		return false;
	}

	int temp = this.currentPosition;
	try {
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				this.currentPosition = temp;
				return false;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (this.currentCharacter != testedChar) {
				this.currentPosition = temp;
				return false;
			}
			this.unicodeAsBackSlash = this.currentCharacter == '\\';

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
			    unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
			return true;

		} //-------------end unicode traitement--------------
		else {
			if (this.currentCharacter != testedChar) {
				this.currentPosition = temp;
				return false;
			}
			this.unicodeAsBackSlash = false;
			if (this.withoutUnicodePtr != 0)
				unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		this.unicodeAsBackSlash = false;
		this.currentPosition = temp;
		return false;
	}
}
public final int getNextChar(char testedChar1, char testedChar2) {
	//INT 0 : testChar1 \\\\///\\\\ 1 : testedChar2 \\\\///\\\\ -1 : others
	//test can be done with (x==0) for the first and (x>0) for the second
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is == to the testedChar1/2
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 
	if (this.currentPosition >= this.source.length) // handle the obvious case upfront
		return -1;

	int temp = this.currentPosition;
	try {
		int result;
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				this.currentPosition = temp;
				return 2;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (this.currentCharacter == testedChar1)
				result = 0;
			else
				if (this.currentCharacter == testedChar2)
					result = 1;
				else {
					this.currentPosition = temp;
					return -1;
				}

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
			return result;
		} //-------------end unicode traitement--------------
		else {
			if (this.currentCharacter == testedChar1)
				result = 0;
			else
				if (this.currentCharacter == testedChar2)
					result = 1;
				else {
					this.currentPosition = temp;
					return -1;
				}

			if (this.withoutUnicodePtr != 0)
				unicodeStoreAt(++this.withoutUnicodePtr);
			return result;
		}
	} catch (IndexOutOfBoundsException e) {
		this.currentPosition = temp;
		return -1;
	}
}
public final boolean getNextCharAsDigit() throws InvalidInputException {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a digit
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 
	if (this.currentPosition >= this.source.length) // handle the obvious case upfront
		return false;

	int temp = this.currentPosition;
	try {
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				this.currentPosition = temp;
				return false;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (!isDigit(this.currentCharacter)) {
				this.currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (!isDigit(this.currentCharacter)) {
				this.currentPosition = temp;
				return false;
			}
			if (this.withoutUnicodePtr != 0)
				unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		this.currentPosition = temp;
		return false;
	}
}
public final boolean getNextCharAsDigit(int radix) {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a digit base on radix
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 
	if (this.currentPosition >= this.source.length) // handle the obvious case upfront
		return false;

	int temp = this.currentPosition;
	try {
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				this.currentPosition = temp;
				return false;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (Character.digit(this.currentCharacter, radix) == -1) {
				this.currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (Character.digit(this.currentCharacter, radix) == -1) {
				this.currentPosition = temp;
				return false;
			}
			if (this.withoutUnicodePtr != 0)
				unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		this.currentPosition = temp;
		return false;
	}
}
public boolean getNextCharAsJavaIdentifierPart() {
	//BOOLEAN
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it
	//Both previous lines are true if the currentCharacter is a JavaIdentifierPart
	//On false, no side effect has occured.

	//ALL getNextChar.... ARE OPTIMIZED COPIES 
	if (this.currentPosition >= this.source.length) // handle the obvious case upfront
		return false;

	int temp = this.currentPosition;
	try {
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			//-------------unicode traitement ------------
			int c1, c2, c3, c4;
			int unicodeSize = 6;
			this.currentPosition++;
			while (this.source[this.currentPosition] == 'u') {
				this.currentPosition++;
				unicodeSize++;
			}

			if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
				this.currentPosition = temp;
				return false;
			}

			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			if (!Character.isJavaIdentifierPart(this.currentCharacter)) {
				this.currentPosition = temp;
				return false;
			}

			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
		    unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		} //-------------end unicode traitement--------------
		else {
			if (!Character.isJavaIdentifierPart(this.currentCharacter)) {
				this.currentPosition = temp;
				return false;
			}

			if (this.withoutUnicodePtr != 0)
			    unicodeStoreAt(++this.withoutUnicodePtr);
			return true;
		}
	} catch (IndexOutOfBoundsException e) {
		this.currentPosition = temp;
		return false;
	}
}
public int getNextToken() throws InvalidInputException {
	this.wasAcr = false;
	if (this.diet) {
		jumpOverMethodBody();
		this.diet = false;
		return this.currentPosition > this.source.length ? TokenNameEOF : TokenNameRBRACE;
	}
	int whiteStart = 0;
	try {
		while (true) { //loop for jumping over comments
			this.withoutUnicodePtr = 0;
			//start with a new token (even comment written with unicode )

			// ---------Consume white space and handles startPosition---------
			whiteStart = this.currentPosition;
			boolean isWhiteSpace, hasWhiteSpaces = false;
			int offset = 0;
			do {
				this.startPosition = this.currentPosition;
				boolean checkIfUnicode = false;
				try {
					checkIfUnicode = ((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u');
				} catch(IndexOutOfBoundsException e) {
					if (this.tokenizeWhiteSpace && (whiteStart != this.currentPosition - 1)) {
						// reposition scanner in case we are interested by spaces as tokens
						this.currentPosition--;
						this.startPosition = whiteStart;
						return TokenNameWHITESPACE;
					}
					if (this.currentPosition > this.eofPosition)
						return TokenNameEOF;
				}
				if (checkIfUnicode) {
					isWhiteSpace = jumpOverUnicodeWhiteSpace();
					offset = 6;
				} else {
					offset = 1;
					if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
						checkNonExternalizedString();
						if (this.recordLineSeparator) {
							pushLineSeparator();
						} else {
							this.currentLine = null;
						}
					}
					isWhiteSpace = 
						(this.currentCharacter == ' ') || CharOperation.isWhitespace(this.currentCharacter); 
				}
				if (isWhiteSpace) {
					hasWhiteSpaces = true;
				}
			} while (isWhiteSpace);
			if (this.tokenizeWhiteSpace && hasWhiteSpaces) {
				// reposition scanner in case we are interested by spaces as tokens
				this.currentPosition-=offset;
				this.startPosition = whiteStart;
				return TokenNameWHITESPACE;
			}
			//little trick to get out in the middle of a source compuation
			if (this.currentPosition > this.eofPosition)
				return TokenNameEOF;

			// ---------Identify the next token-------------

			switch (this.currentCharacter) {
				case '@' :
/*					if (this.sourceLevel >= ClassFileConstants.JDK1_5) {
						return TokenNameAT;
					} else {
						return TokenNameERROR;
					}*/
					return TokenNameAT;
				case '(' :
					return TokenNameLPAREN;
				case ')' :
					return TokenNameRPAREN;
				case '{' :
					return TokenNameLBRACE;
				case '}' :
					return TokenNameRBRACE;
				case '[' :
					return TokenNameLBRACKET;
				case ']' :
					return TokenNameRBRACKET;
				case ';' :
					return TokenNameSEMICOLON;
				case ',' :
					return TokenNameCOMMA;
				case '.' :
					if (getNextCharAsDigit()) {
						return scanNumber(true);
					}
/*					if (this.sourceLevel >= ClassFileConstants.JDK1_5) {
						int temp = this.currentPosition;
						if (getNextChar('.')) {
							if (getNextChar('.')) {
								return TokenNameELLIPSIS;
							} else {
								this.currentPosition = temp;
								return TokenNameDOT;
							}
						} else {
							this.currentPosition = temp;
							return TokenNameDOT;
						}
					} else {
						return TokenNameDOT;
					}*/
					int temp = this.currentPosition;
					if (getNextChar('.')) {
						if (getNextChar('.')) {
							return TokenNameELLIPSIS;
						} else {
							this.currentPosition = temp;
							return TokenNameDOT;
						}
					} else {
						this.currentPosition = temp;
						return TokenNameDOT;
					}
				case '+' :
					{
						int test;
						if ((test = getNextChar('+', '=')) == 0)
							return TokenNamePLUS_PLUS;
						if (test > 0)
							return TokenNamePLUS_EQUAL;
						return TokenNamePLUS;
					}
				case '-' :
					{
						int test;
						if ((test = getNextChar('-', '=')) == 0)
							return TokenNameMINUS_MINUS;
						if (test > 0)
							return TokenNameMINUS_EQUAL;
						return TokenNameMINUS;
					}
				case '~' :
					return TokenNameTWIDDLE;
				case '!' :
					if (getNextChar('='))
						return TokenNameNOT_EQUAL;
					return TokenNameNOT;
				case '*' :
					if (getNextChar('='))
						return TokenNameMULTIPLY_EQUAL;
					return TokenNameMULTIPLY;
				case '%' :
					if (getNextChar('='))
						return TokenNameREMAINDER_EQUAL;
					return TokenNameREMAINDER;
				case '<' :
					{
						int test;
						if ((test = getNextChar('=', '<')) == 0)
							return TokenNameLESS_EQUAL;
						if (test > 0) {
							if (getNextChar('='))
								return TokenNameLEFT_SHIFT_EQUAL;
							return TokenNameLEFT_SHIFT;
						}
						return TokenNameLESS;
					}
				case '>' :
					{
						int test;
						if (this.returnOnlyGreater) {
							return TokenNameGREATER;
						}
						if ((test = getNextChar('=', '>')) == 0)
							return TokenNameGREATER_EQUAL;
						if (test > 0) {
							if ((test = getNextChar('=', '>')) == 0)
								return TokenNameRIGHT_SHIFT_EQUAL;
							if (test > 0) {
								if (getNextChar('='))
									return TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL;
								return TokenNameUNSIGNED_RIGHT_SHIFT;
							}
							return TokenNameRIGHT_SHIFT;
						}
						return TokenNameGREATER;
					}
				case '=' :
					if (getNextChar('='))
						return TokenNameEQUAL_EQUAL;
					return TokenNameEQUAL;
				case '&' :
					{
						int test;
						if ((test = getNextChar('&', '=')) == 0)
							return TokenNameAND_AND;
						if (test > 0)
							return TokenNameAND_EQUAL;
						return TokenNameAND;
					}
				case '|' :
					{
						int test;
						if ((test = getNextChar('|', '=')) == 0)
							return TokenNameOR_OR;
						if (test > 0)
							return TokenNameOR_EQUAL;
						return TokenNameOR;
					}
				case '^' :
					if (getNextChar('='))
						return TokenNameXOR_EQUAL;
					return TokenNameXOR;
				case '?' :
					return TokenNameQUESTION;
				case ':' :
					return TokenNameCOLON;
				case '\'' :
					{
						int test;
						if ((test = getNextChar('\n', '\r')) == 0) {
							throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
						}
						if (test > 0) {
							// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
							for (int lookAhead = 0; lookAhead < 3; lookAhead++) {
								if (this.currentPosition + lookAhead == this.source.length)
									break;
								if (this.source[this.currentPosition + lookAhead] == '\n')
									break;
								if (this.source[this.currentPosition + lookAhead] == '\'') {
									this.currentPosition += lookAhead + 1;
									break;
								}
							}
							throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
						}
					}
					if (getNextChar('\'')) {
						// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
						for (int lookAhead = 0; lookAhead < 3; lookAhead++) {
							if (this.currentPosition + lookAhead == this.source.length)
								break;
							if (this.source[this.currentPosition + lookAhead] == '\n')
								break;
							if (this.source[this.currentPosition + lookAhead] == '\'') {
								this.currentPosition += lookAhead + 1;
								break;
							}
						}
						throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
					}
					if (getNextChar('\\'))
						scanEscapeCharacter();
					else { // consume next character
						this.unicodeAsBackSlash = false;
						boolean checkIfUnicode = false;
						try {
							checkIfUnicode = ((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
							&& (this.source[this.currentPosition] == 'u');
						} catch(IndexOutOfBoundsException e) {
							this.currentPosition--;
							throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
						}
						if (checkIfUnicode) {
							getNextUnicodeChar();
						} else {
							if (this.withoutUnicodePtr != 0) {
								unicodeStoreAt(++this.withoutUnicodePtr);
							}
						}
					}
					if (getNextChar('\''))
						return TokenNameCharacterLiteral;
					// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
					for (int lookAhead = 0; lookAhead < 20; lookAhead++) {
						if (this.currentPosition + lookAhead == this.source.length)
							break;
						if (this.source[this.currentPosition + lookAhead] == '\n')
							break;
						if (this.source[this.currentPosition + lookAhead] == '\'') {
							this.currentPosition += lookAhead + 1;
							break;
						}
					}
					throw new InvalidInputException(INVALID_CHARACTER_CONSTANT);
				case '"' :
					try {
						// consume next character
						this.unicodeAsBackSlash = false;
						boolean isUnicode = false;
						if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
							&& (this.source[this.currentPosition] == 'u')) {
							getNextUnicodeChar();
							isUnicode = true;
						} else {
							if (this.withoutUnicodePtr != 0) {
								unicodeStoreAt(++this.withoutUnicodePtr);
							}
						}

						while (this.currentCharacter != '"') {
							/**** \r and \n are not valid in string literals ****/
							if ((this.currentCharacter == '\n') || (this.currentCharacter == '\r')) {
								// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
								if (isUnicode) {
									int start = this.currentPosition;
									for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
										if (this.currentPosition >= this.eofPosition) {
											this.currentPosition = start;
											break;
										}
										if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
											isUnicode = true;
											getNextUnicodeChar();
										} else {
											isUnicode = false;
										}
										if (!isUnicode && this.currentCharacter == '\n') {
											this.currentPosition--; // set current position on new line character
											break;
										}
										if (this.currentCharacter == '\"') {
											throw new InvalidInputException(INVALID_CHAR_IN_STRING);
										}
									}
								} else {
									this.currentPosition--; // set current position on new line character
								}
								throw new InvalidInputException(INVALID_CHAR_IN_STRING);
							}
							if (this.currentCharacter == '\\') {
								int escapeSize = this.currentPosition;
								boolean backSlashAsUnicodeInString = this.unicodeAsBackSlash;
								//scanEscapeCharacter make a side effect on this value and we need the previous value few lines down this one
								scanEscapeCharacter();
								escapeSize = this.currentPosition - escapeSize;
								if (this.withoutUnicodePtr == 0) {
									//buffer all the entries that have been left aside....
								    unicodeInitializeBuffer(this.currentPosition - escapeSize - 1 - this.startPosition);
								    unicodeStoreAt(++this.withoutUnicodePtr);
								} else { //overwrite the / in the buffer
								    unicodeStoreAt(this.withoutUnicodePtr);
									if (backSlashAsUnicodeInString) { //there are TWO \ in the stream where only one is correct
										this.withoutUnicodePtr--;
									}
								}
							}
							// consume next character
							this.unicodeAsBackSlash = false;
							if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
								&& (this.source[this.currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (this.withoutUnicodePtr != 0) {
									unicodeStoreAt(++this.withoutUnicodePtr);
								}
							}

						}
					} catch (IndexOutOfBoundsException e) {
						this.currentPosition--;
						throw new InvalidInputException(UNTERMINATED_STRING);
					} catch (InvalidInputException e) {
						if (e.getMessage().equals(INVALID_ESCAPE)) {
							// relocate if finding another quote fairly close: thus unicode '/u000D' will be fully consumed
							for (int lookAhead = 0; lookAhead < 50; lookAhead++) {
								if (this.currentPosition + lookAhead == this.source.length)
									break;
								if (this.source[this.currentPosition + lookAhead] == '\n')
									break;
								if (this.source[this.currentPosition + lookAhead] == '\"') {
									this.currentPosition += lookAhead + 1;
									break;
								}
							}

						}
						throw e; // rethrow
					}
					if (this.checkNonExternalizedStringLiterals){ // check for presence of	NLS tags //$NON-NLS-?$ where ? is an int.
						if (this.currentLine == null) {
							this.currentLine = new NLSLine();
						}
						this.currentLine.add(
							new StringLiteral(
								getCurrentTokenSourceString(), 
								this.startPosition, 
								this.currentPosition - 1));
					}
					return TokenNameStringLiteral;
				case '/' :
					{
						int test;
						if ((test = getNextChar('/', '*')) == 0) { //line comment 
							this.lastCommentLinePosition = this.currentPosition;
							try { //get the next char 
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									this.currentPosition++;
									while (this.source[this.currentPosition] == 'u') {
										this.currentPosition++;
									}
									if ((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c1 < 0
										|| (c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c2 < 0
										|| (c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c3 < 0
										|| (c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c4 < 0) {
										throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
									} else {
										this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}

								//handle the \\u case manually into comment
								if (this.currentCharacter == '\\') {
									if (this.source[this.currentPosition] == '\\')
										this.currentPosition++;
								} //jump over the \\
								boolean isUnicode = false;
								while (this.currentCharacter != '\r' && this.currentCharacter != '\n') {
									this.lastCommentLinePosition = this.currentPosition;
									//get the next char
									isUnicode = false;									
									if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
										&& (this.source[this.currentPosition] == 'u')) {
										isUnicode = true;											
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										this.currentPosition++;
										while (this.source[this.currentPosition] == 'u') {
											this.currentPosition++;
										}
										if ((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c4 < 0) {
											throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
										} else {
											this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
									//handle the \\u case manually into comment
									if (this.currentCharacter == '\\') {
										if (this.source[this.currentPosition] == '\\')
											this.currentPosition++;
									} //jump over the \\
								}
								/*
								 * We need to completely consume the line break
								 */
								if (this.currentCharacter == '\r'
								   && this.source.length > this.currentPosition) {
								   	if (this.source[this.currentPosition] == '\n') {
										this.currentPosition++;
										this.currentCharacter = '\n';
								   	} else if ((this.source[this.currentPosition] == '\\')
										&& (this.source[this.currentPosition + 1] == 'u')) {
										isUnicode = true;
										char unicodeChar;
										int index = this.currentPosition + 1;
										index++;
										while (this.source[index] == 'u') {
											index++;
										}
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										if ((c1 = Character.getNumericValue(this.source[index++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(this.source[index++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(this.source[index++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(this.source[index++])) > 15
											|| c4 < 0) {
											this.currentPosition = index;
											throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
										} else {
											unicodeChar = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
										if (unicodeChar == '\n') {
											this.currentPosition = index;
											this.currentCharacter = '\n';
										}
									}
							   	}
								recordComment(TokenNameCOMMENT_LINE);
								if (this.taskTags != null) checkTaskTag(this.startPosition, this.currentPosition);
								if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
									checkNonExternalizedString();
									if (this.recordLineSeparator) {
										if (isUnicode) {
											pushUnicodeLineSeparator();
										} else {
											pushLineSeparator();
										}
									} else {
										this.currentLine = null;
									}
								}
								if (this.tokenizeComments) {
									return TokenNameCOMMENT_LINE;
								}
							} catch (IndexOutOfBoundsException e) {
								this.currentPosition--;
								recordComment(TokenNameCOMMENT_LINE);
								if (this.taskTags != null) checkTaskTag(this.startPosition, this.currentPosition);
								if (this.tokenizeComments) {
									return TokenNameCOMMENT_LINE;
								} else {
									this.currentPosition++; 
								}
							}
							break;
						}
						if (test > 0) { //traditional and javadoc comment
							try { //get the next char
								boolean isJavadoc = false, star = false;
								boolean isUnicode = false;
								// consume next character
								this.unicodeAsBackSlash = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									getNextUnicodeChar();
									isUnicode = true;
								} else {
									isUnicode = false;
									if (this.withoutUnicodePtr != 0) {
										unicodeStoreAt(++this.withoutUnicodePtr);
									}
								}
	
								if (this.currentCharacter == '*') {
									isJavadoc = true;
									star = true;
								}
								if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
									checkNonExternalizedString();
									if (this.recordLineSeparator) {
										if (!isUnicode) {
											pushLineSeparator();
										}
									} else {
										this.currentLine = null;
									}
								}
								isUnicode = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									getNextUnicodeChar();
									isUnicode = true;
								} else {
									isUnicode = false;
								}
								//handle the \\u case manually into comment
								if (this.currentCharacter == '\\') {
									if (this.source[this.currentPosition] == '\\')
										this.currentPosition++; //jump over the \\
								}
								// empty comment is not a javadoc /**/
								if (this.currentCharacter == '/') { 
									isJavadoc = false;
								}
								//loop until end of comment */
								while ((this.currentCharacter != '/') || (!star)) {
									if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
										checkNonExternalizedString();
										if (this.recordLineSeparator) {
											if (!isUnicode) {
												pushLineSeparator();
											}
										} else {
											this.currentLine = null;
										}
									}
									star = this.currentCharacter == '*';
									//get next char
									if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
										&& (this.source[this.currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										getNextUnicodeChar();
										isUnicode = true;
									} else {
										isUnicode = false;
									}
									//handle the \\u case manually into comment
									if (this.currentCharacter == '\\') {
										if (this.source[this.currentPosition] == '\\')
											this.currentPosition++;
									} //jump over the \\
								}
								int token = isJavadoc ? TokenNameCOMMENT_JAVADOC : TokenNameCOMMENT_BLOCK;
								recordComment(token);
								if (this.taskTags != null) checkTaskTag(this.startPosition, this.currentPosition);
								if (this.tokenizeComments) {
									/*
									if (isJavadoc)
										return TokenNameCOMMENT_JAVADOC;
									return TokenNameCOMMENT_BLOCK;
									*/
									return token;
								}
							} catch (IndexOutOfBoundsException e) {
								this.currentPosition--;
								throw new InvalidInputException(UNTERMINATED_COMMENT);
							}
							break;
						}
						if (getNextChar('='))
							return TokenNameDIVIDE_EQUAL;
						return TokenNameDIVIDE;
					}
				case '\u001a' :
					if (atEnd())
						return TokenNameEOF;
					//the atEnd may not be <currentPosition == source.length> if source is only some part of a real (external) stream
					throw new InvalidInputException("Ctrl-Z"); //$NON-NLS-1$

				default :
					if (Character.isJavaIdentifierStart(this.currentCharacter))
						return scanIdentifierOrKeyword();
					if (isDigit(this.currentCharacter)) {
						return scanNumber(false);
					}						
					return TokenNameERROR;
			}
		}
	} //-----------------end switch while try--------------------
	catch (IndexOutOfBoundsException e) {
		if (this.tokenizeWhiteSpace && (whiteStart != this.currentPosition - 1)) {
			// reposition scanner in case we are interested by spaces as tokens
			this.currentPosition--;
			this.startPosition = whiteStart;
			return TokenNameWHITESPACE;
		}
	}
	return TokenNameEOF;
}
public final void getNextUnicodeChar()
	throws InvalidInputException {
	//VOID
	//handle the case of unicode.
	//when a unicode appears then we must use a buffer that holds char internal values
	//At the end of this method currentCharacter holds the new visited char
	//and currentPosition points right next after it

	//ALL getNextChar.... ARE OPTIMIZED COPIES 

	try {
		int c1 = 0, c2 = 0, c3 = 0, c4 = 0, unicodeSize = 6;
		this.currentPosition++;
		while (this.source[this.currentPosition] == 'u') {
			this.currentPosition++;
			unicodeSize++;
		}

		if ((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
			|| c1 < 0
			|| (c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
			|| c2 < 0
			|| (c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
			|| c3 < 0
			|| (c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
			|| c4 < 0){
			throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
		} else {
			this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			//need the unicode buffer
			if (this.withoutUnicodePtr == 0) {
				//buffer all the entries that have been left aside....
				unicodeInitializeBuffer(this.currentPosition - unicodeSize - this.startPosition);
			}
			//fill the buffer with the char
			unicodeStoreAt(++this.withoutUnicodePtr);
		}
		this.unicodeAsBackSlash = this.currentCharacter == '\\';
	} catch (ArrayIndexOutOfBoundsException e) {
		this.currentPosition--;
		throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
	}
}

public char[] getSource(){
	return this.source;
}
private boolean isDigit(char c) throws InvalidInputException {
	if (Character.isDigit(c)) {
		switch(c) {
			case '0' :
			case '1' :
			case '2' :
			case '3' :
			case '4' :
			case '5' :
			case '6' :
			case '7' :
			case '8' :
			case '9' :
				return true;
		}
		throw new InvalidInputException(Scanner.INVALID_DIGIT);
	} else {
		return false;
	}
}
/* Tokenize a method body, assuming that curly brackets are properly balanced.
 */
public final void jumpOverMethodBody() {

	this.wasAcr = false;
	int found = 1;
	try {
		while (true) { //loop for jumping over comments
			// ---------Consume white space and handles startPosition---------
			boolean isWhiteSpace;
			do {
				this.startPosition = this.currentPosition;
				if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
					&& (this.source[this.currentPosition] == 'u')) {
					isWhiteSpace = jumpOverUnicodeWhiteSpace();
				} else {
					if (this.recordLineSeparator
						&& ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')))
						pushLineSeparator();
					isWhiteSpace = CharOperation.isWhitespace(this.currentCharacter);
				}
			} while (isWhiteSpace);

			// -------consume token until } is found---------
			switch (this.currentCharacter) {
				case '{' :
					found++;
					break;
				case '}' :
					found--;
					if (found == 0)
						return;
					break;
				case '\'' :
					{
						boolean test;
						test = getNextChar('\\');
						if (test) {
							try {
								scanEscapeCharacter();
							} catch (InvalidInputException ex) {
								// ignore
							}
						} else {
							try { // consume next character
								this.unicodeAsBackSlash = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									getNextUnicodeChar();
								} else {
									if (this.withoutUnicodePtr != 0) {
										unicodeStoreAt(++this.withoutUnicodePtr);
									}
								}
							} catch (InvalidInputException ex) {
								// ignore
							}
						}
						getNextChar('\'');
						break;
					}
				case '"' :
					try {
						try { // consume next character
							this.unicodeAsBackSlash = false;
							if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
								&& (this.source[this.currentPosition] == 'u')) {
								getNextUnicodeChar();
							} else {
								if (this.withoutUnicodePtr != 0) {
								    unicodeStoreAt(++this.withoutUnicodePtr);
								}
							}
						} catch (InvalidInputException ex) {
								// ignore
						}
						while (this.currentCharacter != '"') {
							if (this.currentCharacter == '\r'){
								if (this.source[this.currentPosition] == '\n') this.currentPosition++;
								break; // the string cannot go further that the line
							}
							if (this.currentCharacter == '\n'){
								break; // the string cannot go further that the line
							}
							if (this.currentCharacter == '\\') {
								try {
									scanEscapeCharacter();
								} catch (InvalidInputException ex) {
									// ignore
								}
							}
							try { // consume next character
								this.unicodeAsBackSlash = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									getNextUnicodeChar();
								} else {
									if (this.withoutUnicodePtr != 0) {
										unicodeStoreAt(++this.withoutUnicodePtr);
									}
								}
							} catch (InvalidInputException ex) {
								// ignore
							}
						}
					} catch (IndexOutOfBoundsException e) {
						return;
					}
					break;
				case '/' :
					{
						int test;
						if ((test = getNextChar('/', '*')) == 0) { //line comment 
							try {
								this.lastCommentLinePosition = this.currentPosition;
								//get the next char 
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
									this.currentPosition++;
									while (this.source[this.currentPosition] == 'u') {
										this.currentPosition++;
									}
									if ((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c1 < 0
										|| (c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c2 < 0
										|| (c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c3 < 0
										|| (c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
										|| c4 < 0) { //error don't care of the value
										this.currentCharacter = 'A';
									} //something different from \n and \r
									else {
										this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
									}
								}
								//handle the \\u case manually into comment
								if (this.currentCharacter == '\\') {
									if (this.source[this.currentPosition] == '\\')
										this.currentPosition++;
								} //jump over the \\
								boolean isUnicode = false;
								while (this.currentCharacter != '\r' && this.currentCharacter != '\n') {
									this.lastCommentLinePosition = this.currentPosition;
									//get the next char 
									isUnicode = false;
									if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
										&& (this.source[this.currentPosition] == 'u')) {
										isUnicode = true;
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										this.currentPosition++;
										while (this.source[this.currentPosition] == 'u') {
											this.currentPosition++;
										}
										if ((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
											|| c4 < 0) { //error don't care of the value
											this.currentCharacter = 'A';
										} //something different from \n and \r
										else {
											this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
									}
									//handle the \\u case manually into comment
									if (this.currentCharacter == '\\') {
										if (this.source[this.currentPosition] == '\\')
											this.currentPosition++;
									} //jump over the \\
								}
								/*
								 * We need to completely consume the line break
								 */
								if (this.currentCharacter == '\r'
								   && this.source.length > this.currentPosition) {
								   	if (this.source[this.currentPosition] == '\n') {
										this.currentPosition++;
										this.currentCharacter = '\n';
								   	} else if ((this.source[this.currentPosition] == '\\')
										&& (this.source[this.currentPosition + 1] == 'u')) {
										isUnicode = true;
										char unicodeChar;
										int index = this.currentPosition + 1;
										index++;
										while (this.source[index] == 'u') {
											index++;
										}
										//-------------unicode traitement ------------
										int c1 = 0, c2 = 0, c3 = 0, c4 = 0;
										if ((c1 = Character.getNumericValue(this.source[index++])) > 15
											|| c1 < 0
											|| (c2 = Character.getNumericValue(this.source[index++])) > 15
											|| c2 < 0
											|| (c3 = Character.getNumericValue(this.source[index++])) > 15
											|| c3 < 0
											|| (c4 = Character.getNumericValue(this.source[index++])) > 15
											|| c4 < 0) { //error don't care of the value
											unicodeChar = 'A';
										} else {
											unicodeChar = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
										}
										if (unicodeChar == '\n') {
											this.currentPosition = index;
											this.currentCharacter = '\n';
										}
									}
							   	}
								recordComment(TokenNameCOMMENT_LINE);
								if (this.recordLineSeparator
									&& ((this.currentCharacter == '\r') || (this.currentCharacter == '\n'))) {
										if (isUnicode) {
											pushUnicodeLineSeparator();
										} else {
											pushLineSeparator();
										}
									}
							} catch (IndexOutOfBoundsException e) {
								 //an eof will then be generated
								this.currentPosition--;
								recordComment(TokenNameCOMMENT_LINE);
								if (!this.tokenizeComments) {
									this.currentPosition++; 
								}
							}
							break;
						}
						if (test > 0) { //traditional and javadoc comment
							boolean isJavadoc = false;
							try { //get the next char
								boolean star = false;
								boolean isUnicode = false;
								// consume next character
								this.unicodeAsBackSlash = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									getNextUnicodeChar();
									isUnicode = true;
								} else {
									isUnicode = false;
									if (this.withoutUnicodePtr != 0) {
    								    unicodeStoreAt(++this.withoutUnicodePtr);
									}
								}
	
								if (this.currentCharacter == '*') {
									isJavadoc = true;
									star = true;
								}
								if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
									if (this.recordLineSeparator) {
										if (isUnicode) {
											pushUnicodeLineSeparator();
										} else {
											pushLineSeparator();
										}
									} else {
										this.currentLine = null;
									}
								}
								isUnicode = false;
								if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
									&& (this.source[this.currentPosition] == 'u')) {
									//-------------unicode traitement ------------
									getNextUnicodeChar();
									isUnicode = true;
								} else {
									isUnicode = false;
								}
								//handle the \\u case manually into comment
								if (this.currentCharacter == '\\') {
									if (this.source[this.currentPosition] == '\\')
										this.currentPosition++; //jump over the \\
								}
								// empty comment is not a javadoc /**/
								if (this.currentCharacter == '/') { 
									isJavadoc = false;
								}
								//loop until end of comment */
								while ((this.currentCharacter != '/') || (!star)) {
									if ((this.currentCharacter == '\r') || (this.currentCharacter == '\n')) {
										if (this.recordLineSeparator) {
											if (isUnicode) {
												pushUnicodeLineSeparator();
											} else {
												pushLineSeparator();
											}
										} else {
											this.currentLine = null;
										}
									}
									star = this.currentCharacter == '*';
									//get next char
									if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
										&& (this.source[this.currentPosition] == 'u')) {
										//-------------unicode traitement ------------
										getNextUnicodeChar();
										isUnicode = true;
									} else {
										isUnicode = false;
									}
									//handle the \\u case manually into comment
									if (this.currentCharacter == '\\') {
										if (this.source[this.currentPosition] == '\\')
											this.currentPosition++;
									} //jump over the \\
								}
								recordComment(isJavadoc ? TokenNameCOMMENT_JAVADOC : TokenNameCOMMENT_BLOCK);
							} catch (IndexOutOfBoundsException e) {
								return;
							}
							break;
						}
						break;
					}

				default :
					if (Character.isJavaIdentifierStart(this.currentCharacter)) {
						scanIdentifierOrKeyword();
						break;
					}
					if (isDigit(this.currentCharacter)) {
						try {
							scanNumber(false);
						} catch (InvalidInputException ex) {
 							// ignore
 						}
						break;
					}
			}
		}
		//-----------------end switch while try--------------------
	} catch (IndexOutOfBoundsException e) {
		// ignore
	} catch (InvalidInputException e) {
		// ignore
	}
	return;
}
public final boolean jumpOverUnicodeWhiteSpace() throws InvalidInputException {
	//BOOLEAN
	//handle the case of unicode. Jump over the next whiteSpace
	//making startPosition pointing on the next available char
	//On false, the currentCharacter is filled up with a potential
	//correct char

	try {
		this.wasAcr = false;
		int c1, c2, c3, c4;
		int unicodeSize = 6;
		this.currentPosition++;
		while (this.source[this.currentPosition] == 'u') {
			this.currentPosition++;
			unicodeSize++;
		}

		if (((c1 = Character.getNumericValue(this.source[this.currentPosition++])) > 15
			|| c1 < 0)
			|| ((c2 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c2 < 0)
			|| ((c3 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c3 < 0)
			|| ((c4 = Character.getNumericValue(this.source[this.currentPosition++])) > 15 || c4 < 0)) {
			throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
		}

		this.currentCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
		if (CharOperation.isWhitespace(this.currentCharacter))
			return true;

		//buffer the new char which is not a white space
		unicodeStoreAt(++this.withoutUnicodePtr);
		//this.withoutUnicodePtr == 1 is true here
		return false;
	} catch (IndexOutOfBoundsException e){
		this.currentPosition--;
		throw new InvalidInputException(INVALID_UNICODE_ESCAPE);
	}
}

final char[] optimizedCurrentTokenSource1() {
	//return always the same char[] build only once

	//optimization at no speed cost of 99.5 % of the singleCharIdentifier
	char charOne = this.source[this.startPosition];
	switch (charOne) {
		case 'a' :
			return charArray_a;
		case 'b' :
			return charArray_b;
		case 'c' :
			return charArray_c;
		case 'd' :
			return charArray_d;
		case 'e' :
			return charArray_e;
		case 'f' :
			return charArray_f;
		case 'g' :
			return charArray_g;
		case 'h' :
			return charArray_h;
		case 'i' :
			return charArray_i;
		case 'j' :
			return charArray_j;
		case 'k' :
			return charArray_k;
		case 'l' :
			return charArray_l;
		case 'm' :
			return charArray_m;
		case 'n' :
			return charArray_n;
		case 'o' :
			return charArray_o;
		case 'p' :
			return charArray_p;
		case 'q' :
			return charArray_q;
		case 'r' :
			return charArray_r;
		case 's' :
			return charArray_s;
		case 't' :
			return charArray_t;
		case 'u' :
			return charArray_u;
		case 'v' :
			return charArray_v;
		case 'w' :
			return charArray_w;
		case 'x' :
			return charArray_x;
		case 'y' :
			return charArray_y;
		case 'z' :
			return charArray_z;
		default :
			return new char[] {charOne};
	}
}
final char[] optimizedCurrentTokenSource2() {
	//try to return the same char[] build only once

	char c0, c1;
	int hash = 
		(((c0 = this.source[this.startPosition]) << 6) + (c1 = this.source[this.startPosition + 1]))
			% TableSize; 
	char[][] table = this.charArray_length[0][hash];
	int i = newEntry2;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry2;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1});
	newEntry2 = max;
	return r;
}
final char[] optimizedCurrentTokenSource3() {
	//try to return the same char[] build only once

	char c0, c1, c2;
	int hash = 
		(((c0 = this.source[this.startPosition]) << 12)
			+ ((c1 = this.source[this.startPosition + 1]) << 6)
			+ (c2 = this.source[this.startPosition + 2]))
			% TableSize; 
	char[][] table = this.charArray_length[1][hash];
	int i = newEntry3;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]) && (c2 == charArray[2]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry3;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0]) && (c1 == charArray[1]) && (c2 == charArray[2]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2});
	newEntry3 = max;
	return r;
}
final char[] optimizedCurrentTokenSource4() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3;
	long hash = 
		((((long) (c0 = this.source[this.startPosition])) << 18)
			+ ((c1 = this.source[this.startPosition + 1]) << 12)
			+ ((c2 = this.source[this.startPosition + 2]) << 6)
			+ (c3 = this.source[this.startPosition + 3]))
			% TableSize; 
	char[][] table = this.charArray_length[2][(int) hash];
	int i = newEntry4;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry4;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3});
	newEntry4 = max;
	return r;
	
}
final char[] optimizedCurrentTokenSource5() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3, c4;
	long hash = 
		((((long) (c0 = this.source[this.startPosition])) << 24)
			+ (((long) (c1 = this.source[this.startPosition + 1])) << 18)
			+ ((c2 = this.source[this.startPosition + 2]) << 12)
			+ ((c3 = this.source[this.startPosition + 3]) << 6)
			+ (c4 = this.source[this.startPosition + 4]))
			% TableSize; 
	char[][] table = this.charArray_length[3][(int) hash];
	int i = newEntry5;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry5;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3, c4});
	newEntry5 = max;
	return r;
		
}
final char[] optimizedCurrentTokenSource6() {
	//try to return the same char[] build only once

	char c0, c1, c2, c3, c4, c5;
	long hash = 
		((((long) (c0 = this.source[this.startPosition])) << 32)
			+ (((long) (c1 = this.source[this.startPosition + 1])) << 24)
			+ (((long) (c2 = this.source[this.startPosition + 2])) << 18)
			+ ((c3 = this.source[this.startPosition + 3]) << 12)
			+ ((c4 = this.source[this.startPosition + 4]) << 6)
			+ (c5 = this.source[this.startPosition + 5]))
			% TableSize; 
	char[][] table = this.charArray_length[4][(int) hash];
	int i = newEntry6;
	while (++i < InternalTableSize) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4])
			&& (c5 == charArray[5]))
			return charArray;
	}
	//---------other side---------
	i = -1;
	int max = newEntry6;
	while (++i <= max) {
		char[] charArray = table[i];
		if ((c0 == charArray[0])
			&& (c1 == charArray[1])
			&& (c2 == charArray[2])
			&& (c3 == charArray[3])
			&& (c4 == charArray[4])
			&& (c5 == charArray[5]))
			return charArray;
	}
	//--------add the entry-------
	if (++max >= InternalTableSize) max = 0;
	char[] r;
	table[max] = (r = new char[] {c0, c1, c2, c3, c4, c5});
	newEntry6 = max;
	return r;	
}
private void parseTags(NLSLine line) {
	String s = new String(getCurrentTokenSource());
	int pos = s.indexOf(TAG_PREFIX);
	int lineLength = line.size();
	while (pos != -1) {
		int start = pos + TAG_PREFIX_LENGTH;
		int end = s.indexOf(TAG_POSTFIX, start);
		if (end != -1) {
			String index = s.substring(start, end);
			int i = 0;
			try {
				i = Integer.parseInt(index) - 1; // Tags are one based not zero based.
			} catch (NumberFormatException e) {
				i = -1; // we don't want to consider this as a valid NLS tag
			}
			if (line.exists(i)) {
				line.set(i, null);
			}
		}
		pos = s.indexOf(TAG_PREFIX, start);
	}

	this.nonNLSStrings = new StringLiteral[lineLength];
	int nonNLSCounter = 0;
	for (Iterator iterator = line.iterator(); iterator.hasNext(); ) {
		StringLiteral literal = (StringLiteral) iterator.next();
		if (literal != null) {
			this.nonNLSStrings[nonNLSCounter++] = literal;
		}
	}
	if (nonNLSCounter == 0) {
		this.nonNLSStrings = null;
		this.currentLine = null;
		return;
	} 
	this.wasNonExternalizedStringLiteral = true;
	if (nonNLSCounter != lineLength) {
		System.arraycopy(this.nonNLSStrings, 0, (this.nonNLSStrings = new StringLiteral[nonNLSCounter]), 0, nonNLSCounter);
	}
	this.currentLine = null;
}

public final void pushLineSeparator() {
	//see comment on isLineDelimiter(char) for the use of '\n' and '\r'
	final int INCREMENT = 250;
	
	if (this.checkNonExternalizedStringLiterals) {
	// reinitialize the current line for non externalize strings purpose
		this.currentLine = null;
	}
	//currentCharacter is at position currentPosition-1

	// cr 000D
	if (this.currentCharacter == '\r') {
		int separatorPos = this.currentPosition - 1;
		//TODO (olivier) david - why the following line was "if ((this.linePtr > 0) && (this.lineEnds[this.linePtr] >= separatorPos)) return;" ?
		if ((this.linePtr >= 0) && (this.lineEnds[this.linePtr] >= separatorPos)) return;
		//System.out.println("CR-" + separatorPos);
		int length = this.lineEnds.length;
		if (++this.linePtr >=  length)
			System.arraycopy(this.lineEnds, 0, this.lineEnds = new int[length + INCREMENT], 0, length);
		this.lineEnds[this.linePtr] = separatorPos;
		// look-ahead for merged cr+lf
		try {
			if (this.source[this.currentPosition] == '\n') {
				//System.out.println("look-ahead LF-" + this.currentPosition);			
				this.lineEnds[this.linePtr] = this.currentPosition;
				this.currentPosition++;
				this.wasAcr = false;
			} else {
				this.wasAcr = true;
			}
		} catch(IndexOutOfBoundsException e) {
			this.wasAcr = true;
		}
	} else {
		// lf 000A
		if (this.currentCharacter == '\n') { //must merge eventual cr followed by lf
			if (this.wasAcr && (this.lineEnds[this.linePtr] == (this.currentPosition - 2))) {
				//System.out.println("merge LF-" + (this.currentPosition - 1));							
				this.lineEnds[this.linePtr] = this.currentPosition - 1;
			} else {
				int separatorPos = this.currentPosition - 1;
				//TODO (olivier) david - why the following line was "if ((this.linePtr > 0) && (this.lineEnds[this.linePtr] >= separatorPos)) return;" ?
				if ((this.linePtr >= 0) && (this.lineEnds[this.linePtr] >= separatorPos)) return;
				// System.out.println("LF-" + separatorPos);
				int length = this.lineEnds.length;
				if (++this.linePtr >=  length)
					System.arraycopy(this.lineEnds, 0, this.lineEnds = new int[length + INCREMENT], 0, length);
				this.lineEnds[this.linePtr] = separatorPos;
			}
			this.wasAcr = false;
		}
	}
}
public final void pushUnicodeLineSeparator() {
	if (this.checkNonExternalizedStringLiterals) {
	// reinitialize the current line for non externalize strings purpose
		this.currentLine = null;
	}
	
	// cr 000D
	if (this.currentCharacter == '\r') {
		if (this.source[this.currentPosition] == '\n') {
			this.wasAcr = false;
		} else {
			this.wasAcr = true;
		}
	} else {
		// lf 000A
		if (this.currentCharacter == '\n') { //must merge eventual cr followed by lf
			this.wasAcr = false;
		}
	}
}
public void recordComment(int token) {
	// compute position
	int stopPosition = this.currentPosition;
	switch (token) {
		case TokenNameCOMMENT_LINE:
			stopPosition = -this.lastCommentLinePosition;
			break;
		case TokenNameCOMMENT_BLOCK:
			stopPosition = -this.currentPosition;
			break;
	}

	// a new comment is recorded
	int length = this.commentStops.length;
	if (++this.commentPtr >=  length) {
		System.arraycopy(this.commentStops, 0, this.commentStops = new int[length + 30], 0, length);
		//grows the positions buffers too
		System.arraycopy(this.commentStarts, 0, this.commentStarts = new int[length + 30], 0, length);
	}
	this.commentStops[this.commentPtr] = stopPosition;
	this.commentStarts[this.commentPtr] = this.startPosition;
}

/**
 * Reposition the scanner on some portion of the original source. The given endPosition is the last valid position.
 * Beyond this position, the scanner will answer EOF tokens (<code>ITerminalSymbols.TokenNameEOF</code>).
 * 
 * @param begin the given start position
 * @param end the given end position
 */
public void resetTo(int begin, int end) {
	//reset the scanner to a given position where it may rescan again

	this.diet = false;
	this.initialPosition = this.startPosition = this.currentPosition = begin;
	this.eofPosition = end < Integer.MAX_VALUE ? end + 1 : end;
	this.commentPtr = -1; // reset comment stack
	this.foundTaskCount = 0;
	
//	// if resetTo is used with being > than end.
//	if (begin > this.eofPosition) {
//		begin = this.eofPosition;
//	}
}

public final void scanEscapeCharacter() throws InvalidInputException {
	// the string with "\\u" is a legal string of two chars \ and u
	//thus we use a direct access to the source (for regular cases).

	if (this.unicodeAsBackSlash) {
		// consume next character
		this.unicodeAsBackSlash = false;
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\') && (this.source[this.currentPosition] == 'u')) {
			getNextUnicodeChar();
		} else {
			if (this.withoutUnicodePtr != 0) {
				unicodeStoreAt(++this.withoutUnicodePtr);
			}
		}
	} else
		this.currentCharacter = this.source[this.currentPosition++];
	switch (this.currentCharacter) {
		case 'b' :
			this.currentCharacter = '\b';
			break;
		case 't' :
			this.currentCharacter = '\t';
			break;
		case 'n' :
			this.currentCharacter = '\n';
			break;
		case 'f' :
			this.currentCharacter = '\f';
			break;
		case 'r' :
			this.currentCharacter = '\r';
			break;
		case '\"' :
			this.currentCharacter = '\"';
			break;
		case '\'' :
			this.currentCharacter = '\'';
			break;
		case '\\' :
			this.currentCharacter = '\\';
			break;
		default :
			// -----------octal escape--------------
			// OctalDigit
			// OctalDigit OctalDigit
			// ZeroToThree OctalDigit OctalDigit

			int number = Character.getNumericValue(this.currentCharacter);
			if (number >= 0 && number <= 7) {
				boolean zeroToThreeNot = number > 3;
				if (isDigit(this.currentCharacter = this.source[this.currentPosition++])) {
					int digit = Character.getNumericValue(this.currentCharacter);
					if (digit >= 0 && digit <= 7) {
						number = (number * 8) + digit;
						if (isDigit(this.currentCharacter = this.source[this.currentPosition++])) {
							if (zeroToThreeNot) {// has read \NotZeroToThree OctalDigit Digit --> ignore last character
								this.currentPosition--;
							} else {
								digit = Character.getNumericValue(this.currentCharacter);
								if (digit >= 0 && digit <= 7){ // has read \ZeroToThree OctalDigit OctalDigit
									number = (number * 8) + digit;
								} else {// has read \ZeroToThree OctalDigit NonOctalDigit --> ignore last character
									this.currentPosition--;
								}
							}
						} else { // has read \OctalDigit NonDigit--> ignore last character
							this.currentPosition--;
						}
					} else { // has read \OctalDigit NonOctalDigit--> ignore last character						
						this.currentPosition--;
					}
				} else { // has read \OctalDigit --> ignore last character
					this.currentPosition--;
				}
				if (number > 255)
					throw new InvalidInputException(INVALID_ESCAPE);
				this.currentCharacter = (char) number;
			} else
				throw new InvalidInputException(INVALID_ESCAPE);
	}
}
public int scanIdentifierOrKeyword() {
	//test keywords

	//first dispatch on the first char.
	//then the length. If there are several
	//keywors with the same length AND the same first char, then do another
	//dispatch on the second char 
	this.useAssertAsAnIndentifier = false;
	this.useEnumAsAnIndentifier = false;
	while (getNextCharAsJavaIdentifierPart()){/*empty*/}

	int index, length;
	char[] data;
	char firstLetter;
	if (this.withoutUnicodePtr == 0)

		//quick test on length == 1 but not on length > 12 while most identifier
		//have a length which is <= 12...but there are lots of identifier with
		//only one char....

		{
		if ((length = this.currentPosition - this.startPosition) == 1)
			return TokenNameIdentifier;
		data = this.source;
		index = this.startPosition;
	} else {
		if ((length = this.withoutUnicodePtr) == 1)
			return TokenNameIdentifier;
		data = this.withoutUnicodeBuffer;
		index = 1;
	}

	firstLetter = data[index];
	switch (firstLetter) {

		case 'a' : 
			switch(length) {
				case 8: //abstract
					if ((data[++index] == 'b')
						&& (data[++index] == 's')
						&& (data[++index] == 't')
						&& (data[++index] == 'r')
						&& (data[++index] == 'a')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')) {
							return TokenNameabstract;
						} else {
							return TokenNameIdentifier;
						}
				case 6: // assert
					if ((data[++index] == 's')
						&& (data[++index] == 's')
						&& (data[++index] == 'e')
						&& (data[++index] == 'r')
						&& (data[++index] == 't')) {
							if (this.sourceLevel >= ClassFileConstants.JDK1_4) {
								this.containsAssertKeyword = true;
								return TokenNameassert;
							} else {
								this.useAssertAsAnIndentifier = true;
								return TokenNameIdentifier;								
							}
						} else {
							return TokenNameIdentifier;
						}
				default: 
					return TokenNameIdentifier;
			}
		case 'b' : //boolean break byte
			switch (length) {
				case 4 :
					if ((data[++index] == 'y') && (data[++index] == 't') && (data[++index] == 'e'))
						return TokenNamebyte;
					else
						return TokenNameIdentifier;
				case 5 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'e')
						&& (data[++index] == 'a')
						&& (data[++index] == 'k'))
						return TokenNamebreak;
					else
						return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'o')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e')
						&& (data[++index] == 'a')
						&& (data[++index] == 'n'))
						return TokenNameboolean;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'c' : //case char catch const class continue
			switch (length) {
				case 4 :
					if (data[++index] == 'a')
						if ((data[++index] == 's') && (data[++index] == 'e'))
							return TokenNamecase;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'h') && (data[++index] == 'a') && (data[++index] == 'r'))
							return TokenNamechar;
						else
							return TokenNameIdentifier;
				case 5 :
					if (data[++index] == 'a')
						if ((data[++index] == 't') && (data[++index] == 'c') && (data[++index] == 'h'))
							return TokenNamecatch;
						else
							return TokenNameIdentifier;
					else
						if (data[index] == 'l')
							if ((data[++index] == 'a')
								&& (data[++index] == 's')
								&& (data[++index] == 's'))
								return TokenNameclass;
							else
								return TokenNameIdentifier;
						else if ((data[index] == 'o')
							&& (data[++index] == 'n')
							&& (data[++index] == 's')
							&& (data[++index] == 't'))
							return TokenNameERROR; //const is not used in java ???????
						else
							return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'n')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'n')
						&& (data[++index] == 'u')
						&& (data[++index] == 'e'))
						return TokenNamecontinue;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'd' : //default do double
			switch (length) {
				case 2 :
					if ((data[++index] == 'o'))
						return TokenNamedo;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'u')
						&& (data[++index] == 'b')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e'))
						return TokenNamedouble;
					else
						return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'e')
						&& (data[++index] == 'f')
						&& (data[++index] == 'a')
						&& (data[++index] == 'u')
						&& (data[++index] == 'l')
						&& (data[++index] == 't'))
						return TokenNamedefault;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}
		case 'e' : //else extends
			switch (length) {
				case 4 :
					if ((data[++index] == 'l') && (data[++index] == 's') && (data[++index] == 'e'))
						return TokenNameelse;
					else if ((data[index] == 'n')
						&& (data[++index] == 'u')
						&& (data[++index] == 'm')) {
							if (this.sourceLevel >= ClassFileConstants.JDK1_5) {
								return TokenNameenum;
							} else {
								this.useEnumAsAnIndentifier = true;
								return TokenNameIdentifier;								
							}
						} else {
							return TokenNameIdentifier;
						}
				case 7 :
					if ((data[++index] == 'x')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'n')
						&& (data[++index] == 'd')
						&& (data[++index] == 's'))
						return TokenNameextends;
					else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'f' : //final finally float for false
			switch (length) {
				case 3 :
					if ((data[++index] == 'o') && (data[++index] == 'r'))
						return TokenNamefor;
					else
						return TokenNameIdentifier;
				case 5 :
					if (data[++index] == 'i')
						if ((data[++index] == 'n')
							&& (data[++index] == 'a')
							&& (data[++index] == 'l')) {
							return TokenNamefinal;
						} else
							return TokenNameIdentifier;
					else
						if (data[index] == 'l')
							if ((data[++index] == 'o')
								&& (data[++index] == 'a')
								&& (data[++index] == 't'))
								return TokenNamefloat;
							else
								return TokenNameIdentifier;
						else
							if ((data[index] == 'a')
								&& (data[++index] == 'l')
								&& (data[++index] == 's')
								&& (data[++index] == 'e'))
								return TokenNamefalse;
							else
								return TokenNameIdentifier;
				case 7 :
					if ((data[++index] == 'i')
						&& (data[++index] == 'n')
						&& (data[++index] == 'a')
						&& (data[++index] == 'l')
						&& (data[++index] == 'l')
						&& (data[++index] == 'y'))
						return TokenNamefinally;
					else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}
		case 'g' : //goto
			if (length == 4) {
				if ((data[++index] == 'o')
					&& (data[++index] == 't')
					&& (data[++index] == 'o')) {
					return TokenNameERROR;
				}
			} //no goto in java are allowed, so why java removes this keyword ???
			return TokenNameIdentifier;

		case 'i' : //if implements import instanceof int interface
			switch (length) {
				case 2 :
					if (data[++index] == 'f')
						return TokenNameif;
					else
						return TokenNameIdentifier;
				case 3 :
					if ((data[++index] == 'n') && (data[++index] == 't'))
						return TokenNameint;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'm')
						&& (data[++index] == 'p')
						&& (data[++index] == 'o')
						&& (data[++index] == 'r')
						&& (data[++index] == 't'))
						return TokenNameimport;
					else
						return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'n')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'r')
						&& (data[++index] == 'f')
						&& (data[++index] == 'a')
						&& (data[++index] == 'c')
						&& (data[++index] == 'e'))
						return TokenNameinterface;
					else
						return TokenNameIdentifier;
				case 10 :
					if (data[++index] == 'm')
						if ((data[++index] == 'p')
							&& (data[++index] == 'l')
							&& (data[++index] == 'e')
							&& (data[++index] == 'm')
							&& (data[++index] == 'e')
							&& (data[++index] == 'n')
							&& (data[++index] == 't')
							&& (data[++index] == 's'))
							return TokenNameimplements;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'n')
							&& (data[++index] == 's')
							&& (data[++index] == 't')
							&& (data[++index] == 'a')
							&& (data[++index] == 'n')
							&& (data[++index] == 'c')
							&& (data[++index] == 'e')
							&& (data[++index] == 'o')
							&& (data[++index] == 'f'))
							return TokenNameinstanceof;
						else
							return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'l' : //long
			if (length == 4) {
				if ((data[++index] == 'o')
					&& (data[++index] == 'n')
					&& (data[++index] == 'g')) {
					return TokenNamelong;
				}
			}
			return TokenNameIdentifier;

		case 'n' : //native new null
			switch (length) {
				case 3 :
					if ((data[++index] == 'e') && (data[++index] == 'w'))
						return TokenNamenew;
					else
						return TokenNameIdentifier;
				case 4 :
					if ((data[++index] == 'u') && (data[++index] == 'l') && (data[++index] == 'l'))
						return TokenNamenull;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'a')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'v')
						&& (data[++index] == 'e')) {
						return TokenNamenative;
					} else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 'p' : //package private protected public
			switch (length) {
				case 6 :
					if ((data[++index] == 'u')
						&& (data[++index] == 'b')
						&& (data[++index] == 'l')
						&& (data[++index] == 'i')
						&& (data[++index] == 'c')) {
						return TokenNamepublic;
					} else
						return TokenNameIdentifier;
				case 7 :
					if (data[++index] == 'a')
						if ((data[++index] == 'c')
							&& (data[++index] == 'k')
							&& (data[++index] == 'a')
							&& (data[++index] == 'g')
							&& (data[++index] == 'e'))
							return TokenNamepackage;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'r')
							&& (data[++index] == 'i')
							&& (data[++index] == 'v')
							&& (data[++index] == 'a')
							&& (data[++index] == 't')
							&& (data[++index] == 'e')) {
							return TokenNameprivate;
						} else
							return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')
						&& (data[++index] == 'e')
						&& (data[++index] == 'd')) {
						return TokenNameprotected;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'r' : //return
			if (length == 6) {
				if ((data[++index] == 'e')
					&& (data[++index] == 't')
					&& (data[++index] == 'u')
					&& (data[++index] == 'r')
					&& (data[++index] == 'n')) {
					return TokenNamereturn;
				}
			}
			return TokenNameIdentifier;

		case 's' : //short static super switch synchronized strictfp
			switch (length) {
				case 5 :
					if (data[++index] == 'h')
						if ((data[++index] == 'o') && (data[++index] == 'r') && (data[++index] == 't'))
							return TokenNameshort;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'u')
							&& (data[++index] == 'p')
							&& (data[++index] == 'e')
							&& (data[++index] == 'r'))
							return TokenNamesuper;
						else
							return TokenNameIdentifier;

				case 6 :
					if (data[++index] == 't')
						if ((data[++index] == 'a')
							&& (data[++index] == 't')
							&& (data[++index] == 'i')
							&& (data[++index] == 'c')) {
							return TokenNamestatic;
						} else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'w')
							&& (data[++index] == 'i')
							&& (data[++index] == 't')
							&& (data[++index] == 'c')
							&& (data[++index] == 'h'))
							return TokenNameswitch;
						else
							return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 't')
						&& (data[++index] == 'r')
						&& (data[++index] == 'i')
						&& (data[++index] == 'c')
						&& (data[++index] == 't')
						&& (data[++index] == 'f')
						&& (data[++index] == 'p'))
						return TokenNamestrictfp;
					else
						return TokenNameIdentifier;
				case 12 :
					if ((data[++index] == 'y')
						&& (data[++index] == 'n')
						&& (data[++index] == 'c')
						&& (data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'n')
						&& (data[++index] == 'i')
						&& (data[++index] == 'z')
						&& (data[++index] == 'e')
						&& (data[++index] == 'd')) {
						return TokenNamesynchronized;
					} else
						return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		case 't' : //try throw throws transient this true
			switch (length) {
				case 3 :
					if ((data[++index] == 'r') && (data[++index] == 'y'))
						return TokenNametry;
					else
						return TokenNameIdentifier;
				case 4 :
					if (data[++index] == 'h') 
						if ((data[++index] == 'i') && (data[++index] == 's'))
							return TokenNamethis;
						else
							return TokenNameIdentifier;
					else
						if ((data[index] == 'r') && (data[++index] == 'u') && (data[++index] == 'e'))
							return TokenNametrue;
						else
							return TokenNameIdentifier;
				case 5 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'w'))
						return TokenNamethrow;
					else
						return TokenNameIdentifier;
				case 6 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'r')
						&& (data[++index] == 'o')
						&& (data[++index] == 'w')
						&& (data[++index] == 's'))
						return TokenNamethrows;
					else
						return TokenNameIdentifier;
				case 9 :
					if ((data[++index] == 'r')
						&& (data[++index] == 'a')
						&& (data[++index] == 'n')
						&& (data[++index] == 's')
						&& (data[++index] == 'i')
						&& (data[++index] == 'e')
						&& (data[++index] == 'n')
						&& (data[++index] == 't')) {
						return TokenNametransient;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'v' : //void volatile
			switch (length) {
				case 4 :
					if ((data[++index] == 'o') && (data[++index] == 'i') && (data[++index] == 'd'))
						return TokenNamevoid;
					else
						return TokenNameIdentifier;
				case 8 :
					if ((data[++index] == 'o')
						&& (data[++index] == 'l')
						&& (data[++index] == 'a')
						&& (data[++index] == 't')
						&& (data[++index] == 'i')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e')) {
						return TokenNamevolatile;
					} else
						return TokenNameIdentifier;

				default :
					return TokenNameIdentifier;
			}

		case 'w' : //while widefp
			switch (length) {
				case 5 :
					if ((data[++index] == 'h')
						&& (data[++index] == 'i')
						&& (data[++index] == 'l')
						&& (data[++index] == 'e'))
						return TokenNamewhile;
					else
						return TokenNameIdentifier;
					//case 6:if ( (data[++index] =='i') && (data[++index]=='d') && (data[++index]=='e') && (data[++index]=='f')&& (data[++index]=='p'))
					//return TokenNamewidefp ;
					//else
					//return TokenNameIdentifier;
				default :
					return TokenNameIdentifier;
			}

		default :
			return TokenNameIdentifier;
	}
}
public int scanNumber(boolean dotPrefix) throws InvalidInputException {

	//when entering this method the currentCharacter is the first
	//digit of the number. It may be preceeded by a '.' when
	//dotPrefix is true

	boolean floating = dotPrefix;
	if ((!dotPrefix) && (this.currentCharacter == '0')) {
		if (getNextChar('x', 'X') >= 0) { //----------hexa-----------------
			int start = this.currentPosition;
			while (getNextCharAsDigit(16)){/*empty*/}
			int end = this.currentPosition;
			if (getNextChar('l', 'L') >= 0) {
				if (end == start) {
					throw new InvalidInputException(INVALID_HEXA);
				}
				return TokenNameLongLiteral;
			} else if (getNextChar('.')) {
				if (this.sourceLevel < ClassFileConstants.JDK1_5) {
					// if we are in source level < 1.5, we report an integer literal
					this.currentPosition = end;
					return TokenNameIntegerLiteral;
				}
				// hexadeciman floating point literal
				// read decimal part
				while (getNextCharAsDigit(16)){/*empty*/}
				
				if (getNextChar('p', 'P') >= 0) { // consume next character
					this.unicodeAsBackSlash = false;
					if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u')) {
						getNextUnicodeChar();
					} else {
						if (this.withoutUnicodePtr != 0) {
							unicodeStoreAt(++this.withoutUnicodePtr);
						}
					}

					if ((this.currentCharacter == '-')
						|| (this.currentCharacter == '+')) { // consume next character
						this.unicodeAsBackSlash = false;
						if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
							&& (this.source[this.currentPosition] == 'u')) {
							getNextUnicodeChar();
						} else {
							if (this.withoutUnicodePtr != 0) {
								unicodeStoreAt(++this.withoutUnicodePtr);
							}
						}
					}
					if (!isDigit(this.currentCharacter)) {
						throw new InvalidInputException(INVALID_HEXA);
					}
					while (getNextCharAsDigit()){/*empty*/}
					if (getNextChar('f', 'F') >= 0) {
						return TokenNameFloatingPointLiteral;
					}
					if (getNextChar('d', 'D') >= 0) {
						return TokenNameDoubleLiteral;
					}
					if (getNextChar('l', 'L') >= 0) {
						throw new InvalidInputException(INVALID_HEXA);
					}					
					return TokenNameDoubleLiteral;
				} else {
					throw new InvalidInputException(INVALID_HEXA);
				}
			} else if (getNextChar('p', 'P') >= 0) { // consume next character
				if (this.sourceLevel < ClassFileConstants.JDK1_5) {
					// if we are in source level < 1.5 we report an integer literal
					this.currentPosition = end;
					return TokenNameIntegerLiteral;
				}
				this.unicodeAsBackSlash = false;
				if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
					&& (this.source[this.currentPosition] == 'u')) {
					getNextUnicodeChar();
				} else {
					if (this.withoutUnicodePtr != 0) {
						unicodeStoreAt(++this.withoutUnicodePtr);
					}
				}

				if ((this.currentCharacter == '-')
					|| (this.currentCharacter == '+')) { // consume next character
					this.unicodeAsBackSlash = false;
					if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u')) {
						getNextUnicodeChar();
					} else {
						if (this.withoutUnicodePtr != 0) {
							unicodeStoreAt(++this.withoutUnicodePtr);
						}
					}
				}
				if (!isDigit(this.currentCharacter))
					throw new InvalidInputException(INVALID_FLOAT);
				while (getNextCharAsDigit()){/*empty*/}
				if (getNextChar('f', 'F') >= 0)
					return TokenNameFloatingPointLiteral;
				if (getNextChar('d', 'D') >= 0)
					return TokenNameDoubleLiteral;
				if (getNextChar('l', 'L') >= 0) {
					throw new InvalidInputException(INVALID_HEXA);
				}
				return TokenNameDoubleLiteral;
			} else {
				if (end == start)
					throw new InvalidInputException(INVALID_HEXA);
				return TokenNameIntegerLiteral;
			}
		}

		//there is x or X in the number
		//potential octal ! ... some one may write 000099.0 ! thus 00100 < 00078.0 is true !!!!! crazy language
		if (getNextCharAsDigit()) { //-------------potential octal-----------------
			while (getNextCharAsDigit()){/*empty*/}

			if (getNextChar('l', 'L') >= 0) {
				return TokenNameLongLiteral;
			}

			if (getNextChar('f', 'F') >= 0) {
				return TokenNameFloatingPointLiteral;
			}

			if (getNextChar('d', 'D') >= 0) {
				return TokenNameDoubleLiteral;
			} else { //make the distinction between octal and float ....
				boolean isInteger = true;
				if (getNextChar('.')) { 
					isInteger = false;
					while (getNextCharAsDigit()){/*empty*/}
				}
				if (getNextChar('e', 'E') >= 0) { // consume next character
					isInteger = false;
					this.unicodeAsBackSlash = false;
					if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
						&& (this.source[this.currentPosition] == 'u')) {
						getNextUnicodeChar();
					} else {
						if (this.withoutUnicodePtr != 0) {
							unicodeStoreAt(++this.withoutUnicodePtr);
						}
					}

					if ((this.currentCharacter == '-')
						|| (this.currentCharacter == '+')) { // consume next character
						this.unicodeAsBackSlash = false;
						if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
							&& (this.source[this.currentPosition] == 'u')) {
							getNextUnicodeChar();
						} else {
							if (this.withoutUnicodePtr != 0) {
								unicodeStoreAt(++this.withoutUnicodePtr);
							}
						}
					}
					if (!isDigit(this.currentCharacter))
						throw new InvalidInputException(INVALID_FLOAT);
					while (getNextCharAsDigit()){/*empty*/}
				}
				if (getNextChar('f', 'F') >= 0)
					return TokenNameFloatingPointLiteral;
				if (getNextChar('d', 'D') >= 0 || !isInteger)
					return TokenNameDoubleLiteral;
				return TokenNameIntegerLiteral;
			}
		} else {
			/* carry on */
		}
	}

	while (getNextCharAsDigit()){/*empty*/}

	if ((!dotPrefix) && (getNextChar('l', 'L') >= 0))
		return TokenNameLongLiteral;

	if ((!dotPrefix) && (getNextChar('.'))) { //decimal part that can be empty
		while (getNextCharAsDigit()){/*empty*/}
		floating = true;
	}

	//if floating is true both exponant and suffix may be optional

	if (getNextChar('e', 'E') >= 0) {
		floating = true;
		// consume next character
		this.unicodeAsBackSlash = false;
		if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
			&& (this.source[this.currentPosition] == 'u')) {
			getNextUnicodeChar();
		} else {
			if (this.withoutUnicodePtr != 0) {
				unicodeStoreAt(++this.withoutUnicodePtr);
			}
		}

		if ((this.currentCharacter == '-')
			|| (this.currentCharacter == '+')) { // consume next character
			this.unicodeAsBackSlash = false;
			if (((this.currentCharacter = this.source[this.currentPosition++]) == '\\')
				&& (this.source[this.currentPosition] == 'u')) {
				getNextUnicodeChar();
			} else {
				if (this.withoutUnicodePtr != 0) {
					unicodeStoreAt(++this.withoutUnicodePtr);
				}
			}
		}
		if (!isDigit(this.currentCharacter))
			throw new InvalidInputException(INVALID_FLOAT);
		while (getNextCharAsDigit()){/*empty*/}
	}

	if (getNextChar('d', 'D') >= 0)
		return TokenNameDoubleLiteral;
	if (getNextChar('f', 'F') >= 0)
		return TokenNameFloatingPointLiteral;

	//the long flag has been tested before

	return floating ? TokenNameDoubleLiteral : TokenNameIntegerLiteral;
}
/**
 * Search the line number corresponding to a specific position
 * @param position int
 * @return int
 */
public final int getLineNumber(int position) {

	if (this.lineEnds == null)
		return 1;
	int length = this.linePtr+1;
	if (length == 0)
		return 1;
	int g = 0, d = length - 1;
	int m = 0;
	while (g <= d) {
		m = (g + d) /2;
		if (position < this.lineEnds[m]) {
			d = m-1;
		} else if (position > this.lineEnds[m]) {
			g = m+1;
		} else {
			return m + 1;
		}
	}
	if (position < this.lineEnds[m]) {
		return m+1;
	}
	return m+2;
}
public final void setSource(char[] sourceString){
	//the source-buffer is set to sourceString

	int sourceLength;
	if (sourceString == null) {
		this.source = CharOperation.NO_CHAR;
		sourceLength = 0;
	} else {
		this.source = sourceString;
		sourceLength = sourceString.length;
	}
	this.startPosition = -1;
	this.eofPosition = sourceLength;
	this.initialPosition = this.currentPosition = 0;
	this.containsAssertKeyword = false;
	this.linePtr = -1;	
}

/*
 * Should be used if a parse (usually a diet parse) has already been performed on the unit, 
 * so as to get the already computed line end positions.
 */
public final void setSource(CompilationResult compilationResult) {
	char[] contents = compilationResult.compilationUnit.getContents();
	setSource(contents);
	int[] lineSeparatorPositions = compilationResult.lineSeparatorPositions;
	if (lineSeparatorPositions != null) {
		this.lineEnds = lineSeparatorPositions;
		this.linePtr = lineSeparatorPositions.length - 1;
	}
}

public String toString() {
	if (this.startPosition == this.source.length)
		return "EOF\n\n" + new String(this.source); //$NON-NLS-1$
	if (this.currentPosition > this.source.length)
		return "behind the EOF\n\n" + new String(this.source); //$NON-NLS-1$

	char front[] = new char[this.startPosition];
	System.arraycopy(this.source, 0, front, 0, this.startPosition);

	int middleLength = (this.currentPosition - 1) - this.startPosition + 1;
	char middle[];
	if (middleLength > -1) {
		middle = new char[middleLength];
		System.arraycopy(
			this.source, 
			this.startPosition, 
			middle, 
			0, 
			middleLength);
	} else {
		middle = CharOperation.NO_CHAR;
	}
	
	char end[] = new char[this.source.length - (this.currentPosition - 1)];
	System.arraycopy(
		this.source, 
		(this.currentPosition - 1) + 1, 
		end, 
		0, 
		this.source.length - (this.currentPosition - 1) - 1);
	
	return new String(front)
		+ "\n===============================\nStarts here -->" //$NON-NLS-1$
		+ new String(middle)
		+ "<-- Ends here\n===============================\n" //$NON-NLS-1$
		+ new String(end); 
}
public final String toStringAction(int act) {
	switch (act) {
		case TokenNameIdentifier :
			return "Identifier(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameabstract :
			return "abstract"; //$NON-NLS-1$
		case TokenNameboolean :
			return "boolean"; //$NON-NLS-1$
		case TokenNamebreak :
			return "break"; //$NON-NLS-1$
		case TokenNamebyte :
			return "byte"; //$NON-NLS-1$
		case TokenNamecase :
			return "case"; //$NON-NLS-1$
		case TokenNamecatch :
			return "catch"; //$NON-NLS-1$
		case TokenNamechar :
			return "char"; //$NON-NLS-1$
		case TokenNameclass :
			return "class"; //$NON-NLS-1$
		case TokenNamecontinue :
			return "continue"; //$NON-NLS-1$
		case TokenNamedefault :
			return "default"; //$NON-NLS-1$
		case TokenNamedo :
			return "do"; //$NON-NLS-1$
		case TokenNamedouble :
			return "double"; //$NON-NLS-1$
		case TokenNameelse :
			return "else"; //$NON-NLS-1$
		case TokenNameextends :
			return "extends"; //$NON-NLS-1$
		case TokenNamefalse :
			return "false"; //$NON-NLS-1$
		case TokenNamefinal :
			return "final"; //$NON-NLS-1$
		case TokenNamefinally :
			return "finally"; //$NON-NLS-1$
		case TokenNamefloat :
			return "float"; //$NON-NLS-1$
		case TokenNamefor :
			return "for"; //$NON-NLS-1$
		case TokenNameif :
			return "if"; //$NON-NLS-1$
		case TokenNameimplements :
			return "implements"; //$NON-NLS-1$
		case TokenNameimport :
			return "import"; //$NON-NLS-1$
		case TokenNameinstanceof :
			return "instanceof"; //$NON-NLS-1$
		case TokenNameint :
			return "int"; //$NON-NLS-1$
		case TokenNameinterface :
			return "interface"; //$NON-NLS-1$
		case TokenNamelong :
			return "long"; //$NON-NLS-1$
		case TokenNamenative :
			return "native"; //$NON-NLS-1$
		case TokenNamenew :
			return "new"; //$NON-NLS-1$
		case TokenNamenull :
			return "null"; //$NON-NLS-1$
		case TokenNamepackage :
			return "package"; //$NON-NLS-1$
		case TokenNameprivate :
			return "private"; //$NON-NLS-1$
		case TokenNameprotected :
			return "protected"; //$NON-NLS-1$
		case TokenNamepublic :
			return "public"; //$NON-NLS-1$
		case TokenNamereturn :
			return "return"; //$NON-NLS-1$
		case TokenNameshort :
			return "short"; //$NON-NLS-1$
		case TokenNamestatic :
			return "static"; //$NON-NLS-1$
		case TokenNamesuper :
			return "super"; //$NON-NLS-1$
		case TokenNameswitch :
			return "switch"; //$NON-NLS-1$
		case TokenNamesynchronized :
			return "synchronized"; //$NON-NLS-1$
		case TokenNamethis :
			return "this"; //$NON-NLS-1$
		case TokenNamethrow :
			return "throw"; //$NON-NLS-1$
		case TokenNamethrows :
			return "throws"; //$NON-NLS-1$
		case TokenNametransient :
			return "transient"; //$NON-NLS-1$
		case TokenNametrue :
			return "true"; //$NON-NLS-1$
		case TokenNametry :
			return "try"; //$NON-NLS-1$
		case TokenNamevoid :
			return "void"; //$NON-NLS-1$
		case TokenNamevolatile :
			return "volatile"; //$NON-NLS-1$
		case TokenNamewhile :
			return "while"; //$NON-NLS-1$

		case TokenNameIntegerLiteral :
			return "Integer(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameLongLiteral :
			return "Long(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameFloatingPointLiteral :
			return "Float(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameDoubleLiteral :
			return "Double(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameCharacterLiteral :
			return "Char(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		case TokenNameStringLiteral :
			return "String(" + new String(getCurrentTokenSource()) + ")"; //$NON-NLS-1$ //$NON-NLS-2$

		case TokenNamePLUS_PLUS :
			return "++"; //$NON-NLS-1$
		case TokenNameMINUS_MINUS :
			return "--"; //$NON-NLS-1$
		case TokenNameEQUAL_EQUAL :
			return "=="; //$NON-NLS-1$
		case TokenNameLESS_EQUAL :
			return "<="; //$NON-NLS-1$
		case TokenNameGREATER_EQUAL :
			return ">="; //$NON-NLS-1$
		case TokenNameNOT_EQUAL :
			return "!="; //$NON-NLS-1$
		case TokenNameLEFT_SHIFT :
			return "<<"; //$NON-NLS-1$
		case TokenNameRIGHT_SHIFT :
			return ">>"; //$NON-NLS-1$
		case TokenNameUNSIGNED_RIGHT_SHIFT :
			return ">>>"; //$NON-NLS-1$
		case TokenNamePLUS_EQUAL :
			return "+="; //$NON-NLS-1$
		case TokenNameMINUS_EQUAL :
			return "-="; //$NON-NLS-1$
		case TokenNameMULTIPLY_EQUAL :
			return "*="; //$NON-NLS-1$
		case TokenNameDIVIDE_EQUAL :
			return "/="; //$NON-NLS-1$
		case TokenNameAND_EQUAL :
			return "&="; //$NON-NLS-1$
		case TokenNameOR_EQUAL :
			return "|="; //$NON-NLS-1$
		case TokenNameXOR_EQUAL :
			return "^="; //$NON-NLS-1$
		case TokenNameREMAINDER_EQUAL :
			return "%="; //$NON-NLS-1$
		case TokenNameLEFT_SHIFT_EQUAL :
			return "<<="; //$NON-NLS-1$
		case TokenNameRIGHT_SHIFT_EQUAL :
			return ">>="; //$NON-NLS-1$
		case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL :
			return ">>>="; //$NON-NLS-1$
		case TokenNameOR_OR :
			return "||"; //$NON-NLS-1$
		case TokenNameAND_AND :
			return "&&"; //$NON-NLS-1$
		case TokenNamePLUS :
			return "+"; //$NON-NLS-1$
		case TokenNameMINUS :
			return "-"; //$NON-NLS-1$
		case TokenNameNOT :
			return "!"; //$NON-NLS-1$
		case TokenNameREMAINDER :
			return "%"; //$NON-NLS-1$
		case TokenNameXOR :
			return "^"; //$NON-NLS-1$
		case TokenNameAND :
			return "&"; //$NON-NLS-1$
		case TokenNameMULTIPLY :
			return "*"; //$NON-NLS-1$
		case TokenNameOR :
			return "|"; //$NON-NLS-1$
		case TokenNameTWIDDLE :
			return "~"; //$NON-NLS-1$
		case TokenNameDIVIDE :
			return "/"; //$NON-NLS-1$
		case TokenNameGREATER :
			return ">"; //$NON-NLS-1$
		case TokenNameLESS :
			return "<"; //$NON-NLS-1$
		case TokenNameLPAREN :
			return "("; //$NON-NLS-1$
		case TokenNameRPAREN :
			return ")"; //$NON-NLS-1$
		case TokenNameLBRACE :
			return "{"; //$NON-NLS-1$
		case TokenNameRBRACE :
			return "}"; //$NON-NLS-1$
		case TokenNameLBRACKET :
			return "["; //$NON-NLS-1$
		case TokenNameRBRACKET :
			return "]"; //$NON-NLS-1$
		case TokenNameSEMICOLON :
			return ";"; //$NON-NLS-1$
		case TokenNameQUESTION :
			return "?"; //$NON-NLS-1$
		case TokenNameCOLON :
			return ":"; //$NON-NLS-1$
		case TokenNameCOMMA :
			return ","; //$NON-NLS-1$
		case TokenNameDOT :
			return "."; //$NON-NLS-1$
		case TokenNameEQUAL :
			return "="; //$NON-NLS-1$
		case TokenNameEOF :
			return "EOF"; //$NON-NLS-1$
		default :
			return "not-a-token"; //$NON-NLS-1$
	}
}
public void unicodeInitializeBuffer(int length) {
	this.withoutUnicodePtr = length;	
    if (this.withoutUnicodeBuffer == null) this.withoutUnicodeBuffer = new char[length+(1+10)];
    int bLength = this.withoutUnicodeBuffer.length;
    if (1+length >= bLength) {
        System.arraycopy(this.withoutUnicodeBuffer, 0, this.withoutUnicodeBuffer = new char[length + (1+10)], 0, bLength);
    }
	System.arraycopy(this.source, this.startPosition, this.withoutUnicodeBuffer, 1, length);    
}
public void unicodeStoreAt(int pos) {
    if (this.withoutUnicodeBuffer == null) this.withoutUnicodeBuffer = new char[10];
    int length = this.withoutUnicodeBuffer.length;
    if (pos == length) {
        System.arraycopy(this.withoutUnicodeBuffer, 0, this.withoutUnicodeBuffer = new char[length * 2], 0, length);
    }
	this.withoutUnicodeBuffer[pos] = this.currentCharacter;
}
}

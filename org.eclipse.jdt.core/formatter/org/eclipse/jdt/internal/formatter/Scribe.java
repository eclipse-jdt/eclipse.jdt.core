/*******************************************************************************
 * Copyright (c) 2002 International Business Machines Corp. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.util.Arrays;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.core.util.PublicScanner;
import org.eclipse.jdt.internal.formatter.align.*;

/**
 * This class is responsible for dumping formatted source
 * @since 2.1
 */
public class Scribe {

	private StringBuffer buffer;
	private boolean checkLineWrapping;
	public int column;
		
	// Most specific alignment. 
	public Alignment currentAlignment;
	public int currentToken;
	
	// TODO: to remove when the testing is done
	private char fillingSpace;
	public CodeFormatterVisitor formatter;
	public int indentationLevel;	
	public int lastNumberOfNewLines;
	public int line;
	private String lineSeparator;
	public boolean needSpace = false;
	public int pageWidth;

	public PublicScanner scanner;
	public int scannerEndPosition;
	public int tabSize;	
	public boolean useTab;
	
	// position mapping
	int[] positionsToMap;
	int[] mappedPositions;
	int positionsIndex;
	
	Scribe(CodeFormatterVisitor formatter, Map settings) {
		if (settings != null) {
			Object assertModeSetting = settings.get(JavaCore.COMPILER_SOURCE);
			this.scanner = (PublicScanner) ToolFactory.createScanner(true, true, JavaCore.VERSION_1_4.equals(assertModeSetting), true);
		} else {
			this.scanner = (PublicScanner) ToolFactory.createScanner(true, true, false, true);
		}
		this.formatter = formatter;
		this.pageWidth = formatter.preferences.page_width;
		this.tabSize = formatter.preferences.tab_size;
		this.useTab = formatter.preferences.use_tab;
		this.fillingSpace = formatter.preferences.filling_space;
		setLineSeparatorAndIdentationLevel(formatter.preferences);

		reset(positionsToMap);
	}

	public void alignFragment(Alignment alignment, int fragmentIndex){
		alignment.fragmentIndex = fragmentIndex;
		alignment.checkColumn();
		alignment.performFragmentEffect();
	}
	
	public Alignment createAlignment(String name, int mode, int count, int sourceRestart){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
	}
	
	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart){
		Alignment alignment = new Alignment(name, mode, tieBreakRule, this, count, sourceRestart);
		// adjust break indentation
/*		if (this.currentAlignment != null) {
			Alignment current = this.currentAlignment;
			while (current.enclosing != null) {
				current = current.enclosing;
			}
			switch(current.chunkKind) {
				case Alignment.CHUNK_METHOD :
				case Alignment.CHUNK_TYPE :
					alignment.breakIndentationLevel = this.indentationLevel + (this.useTab ? 1 : this.tabSize);
					break;
				case Alignment.CHUNK_FIELD :
					alignment.breakIndentationLevel = current.originalIndentationLevel + (this.useTab ? 1 : this.tabSize);
					break;
			}
		}*/
		return alignment; 
	}
	
	public void enterAlignment(Alignment alignment){
		alignment.enclosing = this.currentAlignment;
		this.currentAlignment = alignment;
	}

	public void exitAlignment(Alignment alignment, boolean discardAlignment){
		Alignment current = this.currentAlignment;
		while (current != null){
			if (current == alignment) break;
			current = current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
		}
		this.indentationLevel = alignment.location.outputIndentationLevel;
			
		if (alignment.wasSplit) {
			this.column = alignment.location.outputColumn;
		}
		if (discardAlignment){ 
			this.currentAlignment = alignment.enclosing;
		}
	}
	
	public String formattedSource() {
		return this.buffer.toString();
	}
	
	public Alignment getAlignment(String name){
		if (this.currentAlignment != null) {
			return this.currentAlignment.getAlignment(name);
		}
		return null;
	}
	
	/** 
	 * Answer actual indentation level based on true column position
	 * @return int
	 */
	public int getColumnIndentationLevel() {
		if (this.useTab) {
			return (this.column - 1)/ this.tabSize; 
		} else {
			return this.column - 1;
		}
	}	

	/** 
	 * Answer indentation level based on column estimated position
	 * (if column is not indented, then use indentationLevel)
	 */
	public int getIndentationLevel(int someColumn) {
		if (someColumn == 1) return this.indentationLevel;
		if (this.useTab) {
			return (someColumn - 1) / this.tabSize;
		} else {
			return someColumn - 1;
		}
	}	

	/** 
	 * Answer next indentation level based on column estimated position
	 * (if column is not indented, then use indentationLevel)
	 */
	public int getNextIndentationLevel(int someColumn) {
		if (someColumn == 1) return this.indentationLevel;
		if (this.useTab) {
			int rem = (someColumn - 1)% this.tabSize; // round to superior
			return rem == 0 ? (someColumn - 1)/ this.tabSize : ((someColumn - 1)/ this.tabSize)+1;
		} else {
			return someColumn - 1;
		}
	}	

	public void handleLineTooLong() {
		
		// search for closest breakable alignment, using tiebreak rules
		
		// look for outermost breakable one
		int relativeDepth = 0, outerMostDepth = -1;
		Alignment targetAlignment = this.currentAlignment;
		while (targetAlignment != null){
			if (targetAlignment.tieBreakRule == Alignment.R_OUTERMOST && targetAlignment.couldBreak()){
				outerMostDepth = relativeDepth;
			}
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		if (outerMostDepth >= 0) {
			throw new AlignmentException(AlignmentException.LINE_TOO_LONG, outerMostDepth);
		}
		// look for innermost breakable one
		relativeDepth = 0;
		targetAlignment = this.currentAlignment;
		while (targetAlignment != null){
			if (targetAlignment.couldBreak()){
				throw new AlignmentException(AlignmentException.LINE_TOO_LONG, relativeDepth);
			}
			targetAlignment = targetAlignment.enclosing;
			relativeDepth++;
		}
		// did not find any breakable location - proceed
	}
	
	public void indent() {
		if (this.useTab) {
			this.indentationLevel++; 
		} else {
			this.indentationLevel += tabSize; 
		}
	}	
	
	private void mapPositions(int startOrigin, int startFormatted, int length) {
		// positionsToMap is not null
		for (; this.positionsIndex < this.positionsToMap.length; this.positionsIndex++) {
			if (this.positionsToMap[this.positionsIndex] < startOrigin) {
				this.mappedPositions[this.positionsIndex] = startFormatted;
			} else if (this.positionsToMap[this.positionsIndex] < startOrigin + length) {
				this.mappedPositions[this.positionsIndex] = this.positionsToMap[this.positionsIndex] - startOrigin + startFormatted;
			} else {
				return;
			}
		}
	}
	
	private void mapRemainingPositions() {
		final int bufferLength = this.buffer.length() - 1;
		for (; this.positionsIndex < this.positionsToMap.length; this.positionsIndex++) {
			this.mappedPositions[this.positionsIndex] = bufferLength;
		}
	}
	
	private void preserveEmptyLines(int count) {
		if (count > 0) {
			if (this.formatter.preferences.preserve_user_linebreaks) {
				this.printNewLines(count);
			} else if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve + 1);
				this.printNewLines(linesToPreserve);
			}
		}
	}
		
	private void print(char[] s, int start, boolean considerSpaceIfAny) {
		if (checkLineWrapping && s.length + column > this.pageWidth) {
			handleLineTooLong();
		}
		this.lastNumberOfNewLines = 0;
		printIndentationIfNecessary();
		if (considerSpaceIfAny) {
			this.space();
		}
		if (this.positionsToMap != null) {
			mapPositions(start, this.buffer.length(), s.length);
		}
		this.buffer.append(s);
		column += s.length;
		needSpace = true;
	}
	public void printBlockComment(char[] s, int commentStart, boolean isJavadoc) {
		String commentSource = new String(s);
		StringTokenizer tokenizer = new StringTokenizer(commentSource, "\r\n");	//$NON-NLS-1$
		printIndentationIfNecessary();
		Location location = null;
		int lineCounter = 0;
		int startSearchIndex = 0;
		int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition = this.scanner.getCurrentTokenEndPosition() + 1;
		
		while(tokenizer.hasMoreElements()) {
			String lineContents = tokenizer.nextToken();
			if (lineContents.length() != 0) {
				printIndentationIfNecessary();
				if (lineCounter >= 1) {
					buffer.append(" ");//$NON-NLS-1$
				}
				// remove leading whitespaces
				int lineStartPosition = currentTokenStartPosition + commentSource.indexOf(lineContents, startSearchIndex);
				this.scanner.resetTo(lineStartPosition, this.scannerEndPosition - 1);
				String reduceLine = null;
				try {
					// TODO need investigation in term of edits
					if (this.scanner.getNextToken() == ITerminalSymbols.TokenNameWHITESPACE) {
						int begin = this.scanner.getCurrentTokenEndPosition() + 1 - lineStartPosition;
						if (begin < lineContents.length()) {
							reduceLine = lineContents.substring(begin);
						} else {
							reduceLine = ""; //$NON-NLS-1$
						}
					} else {
						reduceLine = lineContents;
					}
				} catch (InvalidInputException e) {
					// should not happen
				}
				final int reduceLineLength = reduceLine.length();
				// add position mapping
				int start = commentSource.indexOf(reduceLine, startSearchIndex);
				if (this.positionsToMap != null) {
					mapPositions(start + commentStart, this.buffer.length(), reduceLineLength);
				}
				startSearchIndex = start + reduceLineLength + 1;
				buffer.append(reduceLine);
				column += reduceLineLength;
				location = new Location(this, 0);
				buffer.append(this.lineSeparator);
				this.line++;
				lineCounter++;
				column = 1;
			}
		}
		if (location != null) {
			resetAt(location);
		}
		this.lastNumberOfNewLines = 0;
		needSpace = false;
		if (isJavadoc) {
			printNewLine();
		}
		this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
	}

	public void printComment() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespace = true;
			while ((this.currentToken = this.scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				switch(this.currentToken) {
					case ITerminalSymbols.TokenNameWHITESPACE :
						// TODO add a replace edit
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
											count++;
										}
									} else {
										count++; 
									}
									break;
								case '\n' :
									count++;
							}
						}
						if (count > 1) {
							if (hasLineComment) {
								// the line comment consumed the line break
								preserveEmptyLines(count - 1);
							} else {
								preserveEmptyLines(count);
							}
						} else if (count == 1) {
							if (hasLineComment) {
								preserveEmptyLines(1);
							} else if (hasComment || this.formatter.preferences.preserve_user_linebreaks) {
								printNewLine();
							}
						} else {
							hasWhitespace = true;
						}
						currentTokenStartPosition = this.scanner.currentPosition;						
						break;
					case ITerminalSymbols.TokenNameCOMMENT_LINE :
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printCommentLine(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;					
						break;
					case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						break;
					case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), true);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						break;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	
	public void printCommentLine(char[] s, int start) {
		int length = s.length;
		int index;
		loop: for (index = length - 1; index >= 0; index--) {
			switch(s[index]) {
				case '\r' :
				case '\n' :
					break;
				default:
					break loop;
			}
		}
		printIndentationIfNecessary();
		if (this.positionsToMap != null) {
			mapPositions(start, this.buffer.length(), s.length);
		}
		this.buffer
			.append(s, 0, index + 1)
			.append(lineSeparator);
		line++; 
		column = 1;
		needSpace = false;
		lastNumberOfNewLines = 1;
	}

	public void printIndentationIfNecessary() {
		int indentationColumn = (this.useTab ? this.indentationLevel * this.tabSize : this.indentationLevel)+1;
		if (this.column < indentationColumn) {
			for (int i = getColumnIndentationLevel(), max = this.indentationLevel; i < max; i++) { 
				if (this.useTab) {
					this.tab();
				} else {
					this.column++;
					this.buffer.append(this.fillingSpace); // indentation in term of space
					this.needSpace = false;
				}
			}
		}
	}

	public void printLastComment() {
		this.printComment();
		if (positionsToMap != null) {
			this.mapRemainingPositions();
		}
	}

	public void printModifiers() {
		
		try {
			boolean firstComment = true;
			boolean isFirstModifier = true;
			int currentTokenStartPosition = this.scanner.currentPosition;
			while ((this.currentToken = this.scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				switch(this.currentToken) {
					case ITerminalSymbols.TokenNamepublic :
					case ITerminalSymbols.TokenNameprotected :
					case ITerminalSymbols.TokenNameprivate :
					case ITerminalSymbols.TokenNamestatic :
					case ITerminalSymbols.TokenNameabstract :
					case ITerminalSymbols.TokenNamefinal :
					case ITerminalSymbols.TokenNamenative :
					case ITerminalSymbols.TokenNamesynchronized :
					case ITerminalSymbols.TokenNametransient :
					case ITerminalSymbols.TokenNamevolatile :
						firstComment = false;
						this.print(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), !isFirstModifier);
						isFirstModifier = false;
						currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
						break;
					case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), false);
						currentTokenStartPosition = this.scanner.currentPosition;
						if (firstComment) {
							this.printNewLine();
						}
						firstComment = false;
						break;
					case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), true);
						currentTokenStartPosition = this.scanner.currentPosition;
						if (firstComment) {
							this.printNewLine();
						}
						firstComment = false;
						break;
					case ITerminalSymbols.TokenNameCOMMENT_LINE :
						this.printCommentLine(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case ITerminalSymbols.TokenNameWHITESPACE :
						// TODO add a remove edit
					    currentTokenStartPosition = this.scanner.currentPosition;
						break;
					default:
						// step back one token
						firstComment = false;
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;					
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	
	public void printNewLine() {
		if (lastNumberOfNewLines >= 1) {
			column = 1; // ensure that the scribe is at the beginning of a new line
			return;
		}
		this.buffer.append(this.lineSeparator);
		line++;
		lastNumberOfNewLines = 1;
		column = 1;
		needSpace = false;
	}
	public void printNewLines(int linesNumber) {
		if ((lastNumberOfNewLines - 1) >= linesNumber) {
			// there is no need to add new lines
			return;
		}
		final int realNewLineNumber = lastNumberOfNewLines != 0 ? linesNumber - lastNumberOfNewLines + 1 : linesNumber;
		for (int i = 0; i < realNewLineNumber; i++) {
			this.buffer.append(this.lineSeparator);
		}
		lastNumberOfNewLines += realNewLineNumber;
		line += realNewLineNumber;
		column = 1;
		needSpace = false;
	}
	
	public void printNextToken(int expectedTokenType){
		printNextToken(expectedTokenType, false);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
			char[] currentTokenSource = this.scanner.getRawTokenSource();
			if (expectedTokenType != this.currentToken) {
				throw new AbortFormatting("unexpected token type, expecting:"+expectedTokenType+", actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			this.print(currentTokenSource, this.scanner.getCurrentTokenStartPosition(), considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
		
	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny, boolean considerNewLineAfterComment){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
			char[] currentTokenSource = this.scanner.getRawTokenSource();
			if (expectedTokenType != this.currentToken) {
				throw new AbortFormatting("unexpected token type, expecting:"+expectedTokenType+", actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			this.print(currentTokenSource, this.scanner.getCurrentTokenStartPosition(), considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printNextToken(int[] expectedTokenTypes){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
			char[] currentTokenSource = this.scanner.getRawTokenSource();
			if (Arrays.binarySearch(expectedTokenTypes, this.currentToken) < 0) {
				StringBuffer expectations = new StringBuffer(5);
				for (int i = 0; i < expectedTokenTypes.length; i++){
					if (i > 0) {
						expectations.append(',');
					}
					expectations.append(expectedTokenTypes[i]);
				}				
				throw new AbortFormatting("unexpected token type, expecting:["+expectations.toString()+"], actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			this.print(currentTokenSource, this.scanner.getCurrentTokenStartPosition(), false);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	
	public void printNextToken(int[] expectedTokenTypes, boolean considerSpaceIfAny, boolean considerNewLineAfterComment){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
			char[] currentTokenSource = this.scanner.getRawTokenSource();
			if (Arrays.binarySearch(expectedTokenTypes, this.currentToken) < 0) {
				StringBuffer expectations = new StringBuffer(5);
				for (int i = 0; i < expectedTokenTypes.length; i++){
					if (i > 0) {
						expectations.append(',');
					}
					expectations.append(expectedTokenTypes[i]);
				}				
				throw new AbortFormatting("unexpected token type, expecting:["+expectations.toString()+"], actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			this.print(currentTokenSource, this.scanner.getCurrentTokenStartPosition(), considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printQualifiedReference(int sourceEnd) {
		try {
			do {
				this.printComment();
				switch(this.currentToken = this.scanner.getNextToken()) {
					case ITerminalSymbols.TokenNameEOF :
						return;
					case ITerminalSymbols.TokenNameWHITESPACE :
						// TODO add a remove edit
						break;
					default: 
						this.print(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), false);
						break;
				}
			} while (this.scanner.currentPosition < sourceEnd);
		} catch(InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	private void printRule(StringBuffer stringBuffer) {
		for (int i = 0; i < this.pageWidth; i++){
			if ((i % this.tabSize) == 0) { 
				stringBuffer.append('+');
			} else {
				stringBuffer.append('-');
			}
		}
		stringBuffer.append(this.lineSeparator);
		
		for (int i = 0; i < (pageWidth / tabSize); i++) {
			stringBuffer.append(i);
			stringBuffer.append('\t');
		}			
	}

	public void printTrailingComment() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasLineComment = false;
			boolean hasWhitespace = false;
			while ((this.currentToken = this.scanner.getNextToken()) != ITerminalSymbols.TokenNameEOF) {
				switch(this.currentToken) {
					case ITerminalSymbols.TokenNameWHITESPACE :
						// add a replace edit
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
											count++;
										}
									} else {
										count++; 
									}
									break;
								case '\n' :
									count++;
							}
						}
						currentTokenStartPosition = this.scanner.currentPosition;						
						if (count > 1) {
							if (hasLineComment) {
								preserveEmptyLines(count - 1);
							} else {
								preserveEmptyLines(count);
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (count == 1) {
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (hasLineComment) {
							printNewLine();
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else {
							hasWhitespace = true;
						}
						break;
					case ITerminalSymbols.TokenNameCOMMENT_LINE :
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printCommentLine(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;					
						break;
					case ITerminalSymbols.TokenNameCOMMENT_BLOCK :
						if (hasLineComment) {
							printNewLine();
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						}
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						break;
					case ITerminalSymbols.TokenNameCOMMENT_JAVADOC :
						if (hasLineComment) {
							printNewLine();
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						}
						if (hasWhitespace) {
							space();
						}
						hasWhitespace = false;
						this.printBlockComment(this.scanner.getRawTokenSource(), this.scanner.getCurrentTokenStartPosition(), true);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						break;
					default :
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	void redoAlignment(AlignmentException e){
		if (e.relativeDepth > 0) { // if exception targets a distinct context
			e.relativeDepth--; // record fact that current context got traversed
			this.currentAlignment = this.currentAlignment.enclosing; // pop currentLocation
			throw e; // rethrow
		} 
		// reset scribe/scanner to restart at this given location
		this.resetAt(this.currentAlignment.location);
		this.scanner.resetTo(this.currentAlignment.location.inputOffset, this.scanner.eofPosition);
		// clean alignment chunkKind so it will think it is a new chunk again
		this.currentAlignment.chunkKind = 0;
		this.formatter.lastLocalDeclarationSourceStart = -1;
	}

	public void reset(int[] positionsToMapValue) {
		this.buffer = new StringBuffer();
		this.checkLineWrapping = true;
		this.line = 0;
		this.column = 1;
		if (positionsToMapValue != null) {
			this.positionsToMap = positionsToMapValue;
			this.positionsIndex = 0;
			this.mappedPositions = new int[positionsToMapValue.length];
		} else {
			this.positionsToMap = null;
			this.positionsIndex = 0;
			this.mappedPositions = null;
		}
	}
		
	public void resetAt(Location location) {
		this.line = location.outputLine;
		this.column = location.outputColumn;
		this.buffer.replace(location.outputOffset, this.buffer.length(), "");	//$NON-NLS-1$
		this.indentationLevel = location.outputIndentationLevel;
		this.lastNumberOfNewLines = location.lastNumberOfNewLines;
		this.needSpace = location.needSpace;
		this.positionsIndex = location.positionsIndex;
	}

	public void setLineSeparatorAndIdentationLevel(FormattingPreferences preferences) {
		this.lineSeparator = preferences.line_delimiter;
		if (this.useTab) {
			this.indentationLevel = preferences.initial_indentation_level;
		} else {
			this.indentationLevel = preferences.initial_indentation_level * this.tabSize;
		}
	}

	public int size() {
		return this.buffer.length();
	}

	public void space() {
		
		if (!this.needSpace) return;
		this.lastNumberOfNewLines = 0;
		if (CodeFormatterVisitor.DEBUG) {
			this.buffer.append(this.fillingSpace);
		} else {
			this.buffer.append(' ');
		}
		this.column++;
		this.needSpace = false;
	}

	public void tab() {
		this.lastNumberOfNewLines = 0;
		int complement = this.tabSize - ((this.column - 1)% this.tabSize); // amount of space
		if (this.useTab) {
			this.buffer.append('\t');
		} else {
			for (int i = 0; i < complement; i++) {
				this.buffer.append(this.fillingSpace);
			}
		}
		this.column += complement;
		this.needSpace = false;
	}
	
	public String toString() {
		if (CodeFormatterVisitor.DEBUG) {
			StringBuffer stringBuffer = new StringBuffer();
			stringBuffer
				.append("(page witdh = " + this.pageWidth + ") - (useTab = " + this.useTab + ") - (tabSize = " + this.tabSize + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
				.append(this.lineSeparator)
				.append("(line = " + this.line + ") - (column = " + this.column + ") - (identationLevel = " + this.indentationLevel + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
				.append(this.lineSeparator)
				.append("(needSpace = " + this.needSpace + ") - (lastNumberOfNewLines = " + this.lastNumberOfNewLines + ") - (checkLineWrapping = " + this.checkLineWrapping + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
				.append(this.lineSeparator)
				.append("==================================================================================")	//$NON-NLS-1$
				.append(this.lineSeparator);
			printRule(stringBuffer);
			stringBuffer
				.append(this.lineSeparator)
				.append(this.buffer.toString())
				.append(this.lineSeparator);
			printRule(stringBuffer);
			return stringBuffer.toString();
		} else {
			return this.buffer.toString();
		}			
	}
	
	public void unIndent() {
		if (this.useTab) {
			this.indentationLevel--;
		} else {
			this.indentationLevel -= tabSize;
		}
	}	
}

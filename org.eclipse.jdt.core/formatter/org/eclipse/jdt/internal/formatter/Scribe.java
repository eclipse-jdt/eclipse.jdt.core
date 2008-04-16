/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.formatter;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.compiler.ASTVisitor;
import org.eclipse.jdt.internal.compiler.ast.Annotation;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.ScannerHelper;
import org.eclipse.jdt.internal.compiler.parser.TerminalTokens;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.util.CodeSnippetParsingUtil;
import org.eclipse.jdt.internal.core.util.RecordedParsingInformation;
import org.eclipse.jdt.internal.formatter.align.Alignment;
import org.eclipse.jdt.internal.formatter.align.AlignmentException;
import org.eclipse.jdt.internal.formatter.comment.CommentFormatterUtil;
import org.eclipse.jdt.internal.formatter.comment.HTMLEntity2JavaReader;
import org.eclipse.jdt.internal.formatter.comment.IJavaDocTagConstants;
import org.eclipse.jdt.internal.formatter.comment.Java2HTMLEntityReader;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultLineTracker;
import org.eclipse.jface.text.ILineTracker;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

/**
 * This class is responsible for dumping formatted source
 * @since 2.1
 */
public class Scribe implements IJavaDocTagConstants {
	private static final int INITIAL_SIZE = 100;
	
	private boolean checkLineWrapping;
	/** one-based column */
	public int column;
	private int[][] commentPositions;
		
	// Most specific alignment. 
	public Alignment currentAlignment;
	public int currentToken;
	
	// edits management
	private OptimizedReplaceEdit[] edits;
	public int editsIndex;
	
	public CodeFormatterVisitor formatter;
	public int indentationLevel;	
	public int lastNumberOfNewLines;
	public int line;
	
	private int[] lineEnds;
	private int maxLines;
	private String lineSeparator;
	public Alignment memberAlignment;
	public boolean needSpace = false;
	
	public int nlsTagCounter;
	public int pageWidth;
	public boolean pendingSpace = false;

	public Scanner scanner;
	public int scannerEndPosition;
	public int tabLength;	
	public int indentationSize;
	private IRegion[] regions;
	private IRegion[] adaptedRegions;
	public int tabChar;
	public int numberOfIndentations;
	private boolean useTabsOnlyForLeadingIndents;

	/** indent empty lines*/
	private final boolean indentEmptyLines;
	
	boolean formatJavadocComment;
	private final boolean formatBlockComment;
	
	// New way to format javadoc
	private FormatterCommentParser formatterCommentParser; // specialized parser to format comments

	Scribe(CodeFormatterVisitor formatter, long sourceLevel, IRegion[] regions, CodeSnippetParsingUtil codeSnippetParsingUtil, boolean formatJavadoc) {
		this.scanner = new Scanner(true, true, false/*nls*/, sourceLevel/*sourceLevel*/, null/*taskTags*/, null/*taskPriorities*/, true/*taskCaseSensitive*/);
		this.formatter = formatter;
		this.pageWidth = formatter.preferences.page_width;
		this.tabLength = formatter.preferences.tab_size;
		this.indentationLevel= 0; // initialize properly
		this.numberOfIndentations = 0;
		this.useTabsOnlyForLeadingIndents = formatter.preferences.use_tabs_only_for_leading_indentations;
		this.indentEmptyLines = formatter.preferences.indent_empty_lines;
		this.tabChar = formatter.preferences.tab_char;
		if (this.tabChar == DefaultCodeFormatterOptions.MIXED) {
			this.indentationSize = formatter.preferences.indentation_size;
		} else {
			this.indentationSize = this.tabLength;
		}
		this.lineSeparator = formatter.preferences.line_separator;
		this.indentationLevel = formatter.preferences.initial_indentation_level * this.indentationSize;
		this.regions= regions;
		if (codeSnippetParsingUtil != null) {
			final RecordedParsingInformation information = codeSnippetParsingUtil.recordedParsingInformation;
			if (information != null) {
				this.lineEnds = information.lineEnds;
				this.commentPositions = information.commentPositions;
			}
		}
		this.formatBlockComment = formatter.preferences.comment_format_block_comment;
		this.formatJavadocComment = formatJavadoc;
		reset();
	}
	
	/**
	 * This method will adapt the selected regions if needed.
	 * If a region should be adapted (see isAdaptableRegion(IRegion))
	 * retrieve correct upper and lower bounds and replace the region.
	 */
	private void adaptRegions() {
		this.adaptedRegions = new IRegion[this.regions.length];
		for (int i = 0, max = this.regions.length; i < max; i++) {
			IRegion aRegion = this.regions[i];
			int offset = aRegion.getOffset();
			if (offset > 0) {
				int length = aRegion.getLength();
				if (isAdaptableRegion(offset, length)) {
					// if we have a selection, search for overlapping edits
					int upperBound = offset;
					int lowerBound = 0;
					boolean upperFound = false;
					int regionEnd = offset + length;
					for (int j = 0, max2 = this.editsIndex - 1; j <= max2; j++) {
						// search for lower bound
						int editOffset = this.edits[j].offset;
						if (upperFound && lowerBound == 0) {
							int editLength = this.edits[j].length;
							if (editOffset == regionEnd) { // matching edit found
								lowerBound = regionEnd;
								break;
							} else if (editOffset + editLength < regionEnd) {
								continue;
							} else {
								lowerBound = editOffset + editLength; // upper and lower bounds found
								break;
							}
							// search for upper bound
						} else {
							int next = j+1;
							if (next == max2) {
								// https://bugs.eclipse.org/bugs/show_bug.cgi?id=213284
								// checked all edits, no upper bound found: leave the loop
								break;
							}
							if (this.edits[next].offset < offset) {
								continue;
							} else {
								upperBound = editOffset;
								upperFound = true;
								// verify if region end is at EOF
								if (this.scannerEndPosition == regionEnd) {
									lowerBound = this.scannerEndPosition - 1;
									break;
								}
							}
						}
					}
					if (lowerBound != 0) {
						if (offset != upperBound || regionEnd != lowerBound) { // ensure we found a different region
							this.adaptedRegions[i] = new Region(upperBound,
									lowerBound - upperBound);
						}
						// keep other unadaptable region
					} else {
						this.adaptedRegions[i] = this.regions[i];
					}
				} else {
					this.adaptedRegions[i] = this.regions[i];
				}
			} else {
				this.adaptedRegions[i] = this.regions[i];
			}
		}
	}

	private final void addDeleteEdit(int start, int end) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start, end - start + 1, Util.EMPTY_STRING);
	}

	public final void addInsertEdit(int insertPosition, String insertedString) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(insertPosition, 0, insertedString);
	}

	private final void addOptimizedReplaceEdit(int offset, int length, String replacement) {
		if (this.editsIndex > 0) {
			// try to merge last two edits
			final OptimizedReplaceEdit previous = this.edits[this.editsIndex-1];
			final int previousOffset = previous.offset;
			final int previousLength = previous.length;
			final int endOffsetOfPreviousEdit = previousOffset + previousLength;
			final int replacementLength = replacement.length();
			final String previousReplacement = previous.replacement;
			final int previousReplacementLength = previousReplacement.length();
			if (previousOffset == offset && previousLength == length && (replacementLength == 0 || previousReplacementLength == 0)) {
				if (this.currentAlignment != null) {
					final Location location = this.currentAlignment.location;
					if (location.editsIndex == this.editsIndex) {
						location.editsIndex--;
						location.textEdit = previous;
					}
				}
				this.editsIndex--;
				return;
			}
			if (endOffsetOfPreviousEdit == offset) {
				if (length != 0) {
					if (replacementLength != 0) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement + replacement);
					} else if (previousLength + length == previousReplacementLength) {
						// check the characters. If they are identical, we can get rid of the previous edit
						boolean canBeRemoved = true;
						loop: for (int i = previousOffset; i < previousOffset + previousReplacementLength; i++) {
							if (scanner.source[i] != previousReplacement.charAt(i - previousOffset)) {
								this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousReplacementLength, previousReplacement);
								canBeRemoved = false;
								break loop;
							}
						}
						if (canBeRemoved) {
							if (this.currentAlignment != null) {
								final Location location = this.currentAlignment.location;
								if (location.editsIndex == this.editsIndex) {
									location.editsIndex--;
									location.textEdit = previous;
								}
							}
							this.editsIndex--;
						}
					} else {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength + length, previousReplacement);
					}
				} else {
					if (replacementLength != 0) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(previousOffset, previousLength, previousReplacement + replacement);
					}
				}
			} else if ((offset + length == previousOffset) && (previousLength + length == replacementLength + previousReplacementLength)) {
				// check if both edits corresponds to the orignal source code
				boolean canBeRemoved = true;
				String totalReplacement = replacement + previousReplacement;
				loop: for (int i = 0; i < previousLength + length; i++) {
					if (scanner.source[i + offset] != totalReplacement.charAt(i)) {
						this.edits[this.editsIndex - 1] = new OptimizedReplaceEdit(offset, previousLength + length, totalReplacement);
						canBeRemoved = false;
						break loop;
					}
				}
				if (canBeRemoved) {
					if (this.currentAlignment != null) {
						final Location location = this.currentAlignment.location;
						if (location.editsIndex == this.editsIndex) {
							location.editsIndex--;
							location.textEdit = previous;
						}
					}
					this.editsIndex--;
				}
			} else {
				this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
			}
		} else {
			this.edits[this.editsIndex++] = new OptimizedReplaceEdit(offset, length, replacement);
		}
	}
	
	public final void addReplaceEdit(int start, int end, String replacement) {
		if (this.edits.length == this.editsIndex) {
			// resize
			resize();
		}
		addOptimizedReplaceEdit(start,  end - start + 1, replacement);
	}

	public void alignFragment(Alignment alignment, int fragmentIndex){
		alignment.fragmentIndex = fragmentIndex;
		alignment.checkColumn();
		alignment.performFragmentEffect();
	}
	
	public void checkNLSTag(int sourceStart) {
		if (hasNLSTag(sourceStart)) {
			this.nlsTagCounter++;
		}
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, boolean adjust){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, adjust);
	}
	
	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart){
		return createAlignment(name, mode, tieBreakRule, count, sourceRestart, this.formatter.preferences.continuation_indentation, false);
	}

	public Alignment createAlignment(String name, int mode, int count, int sourceRestart, int continuationIndent, boolean adjust){
		return createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart, continuationIndent, adjust);
	}

	public Alignment createAlignment(String name, int mode, int tieBreakRule, int count, int sourceRestart, int continuationIndent, boolean adjust){
		Alignment alignment = new Alignment(name, mode, tieBreakRule, this, count, sourceRestart, continuationIndent);
		// adjust break indentation
		if (adjust && this.memberAlignment != null) {
			Alignment current = this.memberAlignment;
			while (current.enclosing != null) {
				current = current.enclosing;
			}
			if ((current.mode & Alignment.M_MULTICOLUMN) != 0) {
				final int indentSize = this.indentationSize;
				switch(current.chunkKind) {
					case Alignment.CHUNK_METHOD :
					case Alignment.CHUNK_TYPE :
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel = this.indentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
						}
						alignment.update();
						break;
					case Alignment.CHUNK_FIELD :
						if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
							alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
						} else {
							alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
						}
						alignment.update();
						break;
				}
			} else {
				switch(current.mode & Alignment.SPLIT_MASK) {
					case Alignment.M_COMPACT_SPLIT :
					case Alignment.M_COMPACT_FIRST_BREAK_SPLIT :
					case Alignment.M_NEXT_PER_LINE_SPLIT :
					case Alignment.M_NEXT_SHIFTED_SPLIT :
					case Alignment.M_ONE_PER_LINE_SPLIT :
						final int indentSize = this.indentationSize;
						switch(current.chunkKind) {
							case Alignment.CHUNK_METHOD :
							case Alignment.CHUNK_TYPE :
								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
									alignment.breakIndentationLevel = this.indentationLevel + indentSize;
								} else {
									alignment.breakIndentationLevel = this.indentationLevel + continuationIndent * indentSize;
								}
								alignment.update();
								break;
							case Alignment.CHUNK_FIELD :
								if ((mode & Alignment.M_INDENT_BY_ONE) != 0) {
									alignment.breakIndentationLevel = current.originalIndentationLevel + indentSize;
								} else {
									alignment.breakIndentationLevel = current.originalIndentationLevel + continuationIndent * indentSize;
								}
								alignment.update();
								break;
						}
						break;
				}
			}
		}
		return alignment; 
	}

	public Alignment createMemberAlignment(String name, int mode, int count, int sourceRestart) {
		Alignment mAlignment = createAlignment(name, mode, Alignment.R_INNERMOST, count, sourceRestart);
		mAlignment.breakIndentationLevel = this.indentationLevel;
		return mAlignment;
	}
	
	public void enterAlignment(Alignment alignment){
		alignment.enclosing = this.currentAlignment;
		alignment.location.lastLocalDeclarationSourceStart = this.formatter.lastLocalDeclarationSourceStart;
		this.currentAlignment = alignment;
	}

	public void enterMemberAlignment(Alignment alignment) {
		alignment.enclosing = this.memberAlignment;
		alignment.location.lastLocalDeclarationSourceStart = this.formatter.lastLocalDeclarationSourceStart;
		this.memberAlignment = alignment;
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
		this.numberOfIndentations = alignment.location.numberOfIndentations;
		this.formatter.lastLocalDeclarationSourceStart = alignment.location.lastLocalDeclarationSourceStart;	
		if (discardAlignment){ 
			this.currentAlignment = alignment.enclosing;
		}
	}
	
	public void exitMemberAlignment(Alignment alignment){
		Alignment current = this.memberAlignment;
		while (current != null){
			if (current == alignment) break;
			current = current.enclosing;
		}
		if (current == null) {
			throw new AbortFormatting("could not find matching alignment: "+alignment); //$NON-NLS-1$
		}
		this.indentationLevel = current.location.outputIndentationLevel;
		this.numberOfIndentations = current.location.numberOfIndentations;
		this.formatter.lastLocalDeclarationSourceStart = alignment.location.lastLocalDeclarationSourceStart;	
		this.memberAlignment = current.enclosing;
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
		return this.column - 1;
	}	
	
	public final int getCommentIndex(int position) {
		if (this.commentPositions == null)
			return -1;
		int length = this.commentPositions.length;
		if (length == 0) {
			return -1;
		}
		int g = 0, d = length - 1;
		int m = 0;
		while (g <= d) {
			m = g + (d - g) / 2;
			int bound = this.commentPositions[m][1];
			if (bound < 0) {
				bound = -bound;
			}
			if (bound < position) {
				g = m + 1;
			} else if (bound > position) {
				d = m - 1;
			} else {
				return m;
			}
		}
		return -(g + 1);
	}

	private IRegion getCoveringRegion(int offset, int end) {
		int index = getIndexOfRegionAt(offset);

		if (index < 0) {
			index = -(index + 1);
			index--;
			if (index < 0) {
				return null;
			}
		}

		IRegion region = this.adaptedRegions[index];
		if ((region.getOffset() <= offset) && (end <= region.getOffset() + region.getLength() - 1)) {
			return region;
		}
		return null;
	}
	
	private int getCurrentCommentOffset(int start) {
		int linePtr = -Arrays.binarySearch(this.lineEnds, start);
		int offset = 0;
		int beginningOfLine = this.getLineEnd(linePtr - 1);
		if (beginningOfLine == -1) {
			beginningOfLine = 0;
		}
		int currentStartPosition = start;
		char[] source = scanner.source;

		// find the position of the beginning of the line containing the comment
		while (beginningOfLine > currentStartPosition) {
			if (linePtr > 0) {
				beginningOfLine = this.getLineEnd(--linePtr);
			} else {
				beginningOfLine = 0;
				break;
			}
		}
		for (int i = currentStartPosition - 1; i >= beginningOfLine ; i--) {
			char currentCharacter = source[i];
			switch (currentCharacter) {
				case '\t' :
					offset += this.tabLength;
					break;
				case ' ' :
					offset++;
					break;
				case '\r' :
				case '\n' :
					break;
				default:
					return offset;
			}
		}
		return offset;
	}

	public String getEmptyLines(int linesNumber) {
		if (this.nlsTagCounter > 0) {
			return Util.EMPTY_STRING;
		}
		StringBuffer buffer = new StringBuffer();
		if (lastNumberOfNewLines == 0) {
			linesNumber++; // add an extra line breaks
			for (int i = 0; i < linesNumber; i++) {
				if (indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			lastNumberOfNewLines += linesNumber;
			line += linesNumber;
			column = 1;
			needSpace = false;
			this.pendingSpace = false;
		} else if (lastNumberOfNewLines == 1) {
			for (int i = 0; i < linesNumber; i++) {
				if (indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			lastNumberOfNewLines += linesNumber;
			line += linesNumber;
			column = 1;
			needSpace = false;
			this.pendingSpace = false;
		} else {
			if ((lastNumberOfNewLines - 1) >= linesNumber) {
				// there is no need to add new lines
				return Util.EMPTY_STRING;
			}
			final int realNewLineNumber = linesNumber - lastNumberOfNewLines + 1;
			for (int i = 0; i < realNewLineNumber; i++) {
				if (indentEmptyLines) printIndentationIfNecessary(buffer);
				buffer.append(this.lineSeparator);
			}
			lastNumberOfNewLines += realNewLineNumber;
			line += realNewLineNumber;
			column = 1;
			needSpace = false;
			this.pendingSpace = false;
		}
		return String.valueOf(buffer);
	}
	
	private int getIndexOfRegionAt(int offset) {
		if (this.adaptedRegions.length == 1) {
			int offset2 = this.adaptedRegions[0].getOffset();
			if (offset2 == offset) {
				return 0;
			}
			return offset2 < offset ? -2 : -1; 
		}
		return Arrays.binarySearch(this.adaptedRegions, new Region(offset, 0), new Comparator() {
			public int compare(Object o1, Object o2) {
				int r1Offset = ((IRegion)o1).getOffset();
				int r2Offset = ((IRegion)o2).getOffset();
				
				return r1Offset - r2Offset;
			}
		});
	}

	public OptimizedReplaceEdit getLastEdit() {
		if (this.editsIndex > 0) {
			return this.edits[this.editsIndex - 1];
		}
		return null;
	}
	
	public final int getLineEnd(int lineNumber) {
		if (this.lineEnds == null) 
			return -1;
		if (lineNumber >= this.lineEnds.length + 1) 
			return this.scannerEndPosition;
		if (lineNumber <= 0) 
			return -1;
		return this.lineEnds[lineNumber-1]; // next line start one character behind the lineEnd of the previous line	
	}
	
	Alignment getMemberAlignment() {
		return this.memberAlignment;
	}
	
	public String getNewLine() {
		if (this.nlsTagCounter > 0) {
			return Util.EMPTY_STRING;
		}
		if (lastNumberOfNewLines >= 1) {
			column = 1; // ensure that the scribe is at the beginning of a new line
			return Util.EMPTY_STRING;
		}
		line++;
		lastNumberOfNewLines = 1;
		column = 1;
		needSpace = false;
		this.pendingSpace = false;
		return this.lineSeparator;
	}

	/** 
	 * Answer next indentation level based on column estimated position
	 * (if column is not indented, then use indentationLevel)
	 */
	public int getNextIndentationLevel(int someColumn) {
		int indent = someColumn - 1;
		if (indent == 0)
			return this.indentationLevel;
		if (this.tabChar == DefaultCodeFormatterOptions.TAB) {
			if (this.useTabsOnlyForLeadingIndents) {
				return indent;
			}
			int rem = indent % this.indentationSize;
			int addition = rem == 0 ? 0 : this.indentationSize - rem; // round to superior
			return indent + addition;
		}
		return indent;
	}

	private String getPreserveEmptyLines(int count) {
		if (count > 0) {
			if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
				return this.getEmptyLines(linesToPreserve);
			}
			return getNewLine();
		}
		return Util.EMPTY_STRING;
	}

	private IRegion getRegionAt(int offset) {
		int index = getIndexOfRegionAt(offset);
		if (index < 0) {
			return null;
		}
		
		return this.regions[index];
	}
	
	public TextEdit getRootEdit() {
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=208541
		adaptRegions();
		
		MultiTextEdit edit = null;
		int regionsLength = this.adaptedRegions.length;
		int textRegionStart;
		int textRegionEnd;
		if (regionsLength == 1) {
			IRegion lastRegion = this.adaptedRegions[0];
			textRegionStart = lastRegion.getOffset();
			textRegionEnd = textRegionStart + lastRegion.getLength();
		} else {
			textRegionStart = this.adaptedRegions[0].getOffset();
			IRegion lastRegion = this.adaptedRegions[regionsLength - 1];
			textRegionEnd = lastRegion.getOffset() + lastRegion.getLength();
		}
		
		int length = textRegionEnd - textRegionStart + 1;
		if (textRegionStart <= 0) {
			if (length <= 0) {
				edit = new MultiTextEdit(0, 0);
			} else {
				edit = new MultiTextEdit(0, textRegionEnd);
			}
		} else {
			edit = new MultiTextEdit(textRegionStart, length - 1);
		}
		for (int i= 0, max = this.editsIndex; i < max; i++) {
			OptimizedReplaceEdit currentEdit = edits[i];
			if (isValidEdit(currentEdit)) {
				try {
					edit.addChild(new ReplaceEdit(currentEdit.offset, currentEdit.length, currentEdit.replacement));
				}
				catch (MalformedTreeException ex) {
					// log exception in case of error
					CommentFormatterUtil.log(ex);
 					throw ex;
				}
			}
		}
		this.edits = null;
		return edit;
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

	/*
	 * Check if there is a NLS tag on this line. If yes, return true, returns false otherwise.
	 */
	private boolean hasNLSTag(int sourceStart) {
		// search the last comment where commentEnd < current lineEnd
		if (this.lineEnds == null) return false;
		int index = Arrays.binarySearch(this.lineEnds, sourceStart);
		int currentLineEnd = this.getLineEnd(-index);
		if (currentLineEnd != -1) {
			int commentIndex = getCommentIndex(currentLineEnd);
			if (commentIndex < 0) {
				commentIndex = -commentIndex - 2;
			}
			if (commentIndex >= 0 && commentIndex < this.commentPositions.length) {
				int start = this.commentPositions[commentIndex][0];
				if (start < 0) {
					start = -start;
					// check that we are on the same line
					int lineIndexForComment = Arrays.binarySearch(this.lineEnds, start);
					if (lineIndexForComment == index) {
						return CharOperation.indexOf(Scanner.TAG_PREFIX, this.scanner.source, true, start, currentLineEnd) != -1;
					}
				}
			}
		}
		return false;
	}

	public void indent() {
		this.indentationLevel += this.indentationSize;
		this.numberOfIndentations++;
	}	

	/**
	 * @param compilationUnitSource
	 */
	public void initializeScanner(char[] compilationUnitSource) {
		this.scanner.setSource(compilationUnitSource);
		this.scannerEndPosition = compilationUnitSource.length;
		this.scanner.resetTo(0, this.scannerEndPosition - 1);
		this.edits = new OptimizedReplaceEdit[INITIAL_SIZE];
		this.maxLines = this.lineEnds == null ? -1 : this.lineEnds.length - 1;
		this.scanner.lineEnds = this.lineEnds;
		this.scanner.linePtr = this.maxLines;
		// Comment following line to de-activate new Javadoc formatter
		initFormatterCommentParser();
	}
	
	private void initFormatterCommentParser() {
		if (this.formatterCommentParser == null) {
			this.formatterCommentParser = new FormatterCommentParser(null);
		}
		this.formatterCommentParser.scanner.setSource(this.scanner.source);
		this.formatterCommentParser.source = this.scanner.source;
		this.formatterCommentParser.scanner.lineEnds = this.lineEnds;
		this.formatterCommentParser.scanner.linePtr = this.maxLines;
	}
	
	/**
	 * Returns whether the given region should be adpated of not.
	 * A region should be adapted only if:
	 * - region does not exceed the page width
	 * - on a single line when more than one line in CU
	 * @param offset the offset of the region to consider
	 * @param length the length of the region to consider
	 * @return boolean true if line should be adapted, false otherwhise
	 */
	private boolean isAdaptableRegion(int offset, int length) {
		int regionEnd = offset + length;
		
		// first check region width
		if (regionEnd > this.pageWidth) {
			return false;
		}
		
		int numberOfLineEnds = this.lineEnds != null && this.lineEnds.length > 0 ? this.lineEnds.length : 0;
		if (this.line == numberOfLineEnds + 1) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=213283
			return true; // last line of the CU
		}
		
		if (this.line > 1 && numberOfLineEnds > 0) { // CU has more than one line
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=222182
			// take the max number of line ends as right bound when searching for line number
			int lineNumber = Util.getLineNumber(offset, this.lineEnds, 0, numberOfLineEnds);
			int lineEnd = this.getLineEnd(lineNumber);
			if (regionEnd > lineEnd) {
				// if more than one line selected, check whether selection is at line end
				for (int i = lineNumber + 1 ; i <=  numberOfLineEnds ; i++) {
					int nextLineEnd = this.getLineEnd(i);
					// accept both line ends and line starts
					if (regionEnd == nextLineEnd) {
						return length > 1; // except when formatting a single character
					} else if (regionEnd == lineEnd + 1 || regionEnd == nextLineEnd + 1) {
						return true;
					}
				}
				return false; // more than one line selected, no need to adapt region
			} else {
				if (this.scannerEndPosition - 1 == lineEnd) { // EOF reached?
					return false;
				}
				return true; // a single line was selected
			}
		}
		return false;
	}
	
	private boolean isOnFirstColumn(int start) {
		if (this.lineEnds == null) return start == 0;
		int index = Arrays.binarySearch(this.lineEnds, start);
		// we want the line end of the previous line
		int previousLineEnd = this.getLineEnd(-index - 1);
		return previousLineEnd != -1 && previousLineEnd == start - 1;
	}

	private boolean isValidEdit(OptimizedReplaceEdit edit) {
		final int editLength= edit.length;
		final int editReplacementLength= edit.replacement.length();
		final int editOffset= edit.offset;
		if (editLength != 0) {
			
			IRegion covering = getCoveringRegion(editOffset, (editOffset + editLength - 1));
			if (covering != null) {
				if (editReplacementLength != 0 && editLength == editReplacementLength) {
					for (int i = editOffset, max = editOffset + editLength; i < max; i++) {
						if (scanner.source[i] != edit.replacement.charAt(i - editOffset)) {
							return true;
						}
					}
					return false;
				}
				return true;
			}

			IRegion starting = getRegionAt(editOffset + editLength);
			if (starting != null) {
				int i = editOffset;
				for (int max = editOffset + editLength; i < max; i++) {
					int replacementStringIndex = i - editOffset;
					if (replacementStringIndex >= editReplacementLength || scanner.source[i] != edit.replacement.charAt(replacementStringIndex)) {
						break;
					}
				}
				if (i - editOffset != editReplacementLength && i != editOffset + editLength - 1) {
					edit.offset = starting.getOffset();
					edit.length = 0;
					edit.replacement = edit.replacement.substring(i - editOffset);
					return true;
				}
			}
			
			return false;
		}
		
		IRegion covering = getCoveringRegion(editOffset, editOffset);
		if (covering != null) {
			return true;
		}

		if (editOffset == this.scannerEndPosition) {
			int index = Arrays.binarySearch(
				this.adaptedRegions,
				new Region(editOffset, 0),
				new Comparator() {
					public int compare(Object o1, Object o2) {
						IRegion r1 = (IRegion)o1;
						IRegion r2 = (IRegion)o2;
						
						int r1End = r1.getOffset() + r1.getLength();
						int r2End = r2.getOffset() + r2.getLength();
						
						return r1End - r2End;
					}
				});
			if (index < 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	private void preserveEmptyLines(int count, int insertPosition) {
		if (count > 0) {
			if (this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
				int linesToPreserve = Math.min(count, this.formatter.preferences.number_of_empty_lines_to_preserve);
				this.printEmptyLines(linesToPreserve, insertPosition);
			} else {
				printNewLine(insertPosition);
			}
		}
	}

	private void print(int length, boolean considerSpaceIfAny) {
		if (checkLineWrapping && length + column > this.pageWidth) {
			handleLineTooLong();
		}
		this.lastNumberOfNewLines = 0;
		if (this.indentationLevel != 0) {
			printIndentationIfNecessary();
		}
		if (considerSpaceIfAny) {
			this.space();
		}
		if (this.pendingSpace) {
			this.addInsertEdit(this.scanner.getCurrentTokenStartPosition(), " "); //$NON-NLS-1$
		}
		this.pendingSpace = false;	
		this.needSpace = false;		
		column += length;
		needSpace = true;
	}

	private void printBlockComment(boolean isJavadoc) {
		int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition = this.scanner.getCurrentTokenEndPosition() + 1;
		
		this.scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		boolean isNewLine = false;
		int start = currentTokenStartPosition;
		int nextCharacterStart = currentTokenStartPosition;
		int previousStart = currentTokenStartPosition;
		boolean onFirstColumn = isOnFirstColumn(start);

		boolean indentComment = false;
		if (this.indentationLevel != 0) {
			if (isJavadoc
					|| !this.formatter.preferences.never_indent_block_comments_on_first_column
					|| !onFirstColumn) {
				indentComment = true;
				printIndentationIfNecessary();
			}
		}
		if (this.pendingSpace) {
			this.addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
		}
		this.needSpace = false;
		this.pendingSpace = false;

		int currentCommentOffset = onFirstColumn ? 0 : getCurrentCommentOffset(start);
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=102780
		// Previous behavior was to do nothing when dealing with javadoc kind and not formatting javadocs.
		// Now, by default, all javadoc comments will be indented once processed in this method.
		// This modification does not apply to block or single line comments
		boolean formatComment = isJavadoc || formatBlockComment;

		while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = this.scanner.getNextChar()) != -1) {
			nextCharacterStart = this.scanner.currentPosition;

			switch(currentCharacter) {
				case '\r' :
					start = previousStart;
					isNewLine = true;
					if (this.scanner.getNextChar('\n')) {
						currentCharacter = '\n';
						nextCharacterStart = this.scanner.currentPosition;
					}
					break;
				case '\n' :
					start = previousStart;
					isNewLine = true;
					nextCharacterStart = this.scanner.currentPosition;
					break;
				default:
					if (isNewLine) {
						this.column = 1;
						this.line++;
						isNewLine = false;
						
						StringBuffer buffer = new StringBuffer();
						if (onFirstColumn) {
							// simply insert indentation if necessary
							buffer.append(this.lineSeparator);
							if (indentComment) {
								printIndentationIfNecessary(buffer);
							}
							if (formatComment) {
								if (ScannerHelper.isWhitespace((char) currentCharacter)) {
									int previousStartPosition = this.scanner.currentPosition;
									while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
										previousStart = nextCharacterStart;
										previousStartPosition = this.scanner.currentPosition;
										currentCharacter = this.scanner.getNextChar();
										nextCharacterStart = this.scanner.currentPosition;
									}
									if (currentCharacter == '\r' || currentCharacter == '\n') {
										nextCharacterStart = previousStartPosition;
									}
								}
								if (currentCharacter != '\r' && currentCharacter != '\n') {
									buffer.append(' ');
								}
							}
						} else {
							if (ScannerHelper.isWhitespace((char) currentCharacter)) {
								int previousStartPosition = this.scanner.currentPosition;
								int count = 0;
								loop: while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
									if (count >= currentCommentOffset) {
										break loop;
									}
									previousStart = nextCharacterStart;
									previousStartPosition = this.scanner.currentPosition;
									switch(currentCharacter) {
										case '\t' :
											count += this.tabLength;
											break;
										default :
											count ++;
									}
									currentCharacter = this.scanner.getNextChar();
									nextCharacterStart = this.scanner.currentPosition;
								}
								if (currentCharacter == '\r' || currentCharacter == '\n') {
									nextCharacterStart = previousStartPosition;
								}
							}
							buffer.append(this.lineSeparator);
							if (indentComment) {
								printIndentationIfNecessary(buffer);
							}
							if (formatComment) {
								int previousStartTemp = previousStart;
								int nextCharacterStartTemp = nextCharacterStart;
								while(currentCharacter != -1 && currentCharacter != '\r' && currentCharacter != '\n' && ScannerHelper.isWhitespace((char) currentCharacter)) {
									previousStart = nextCharacterStart;
									currentCharacter = this.scanner.getNextChar();
									nextCharacterStart = this.scanner.currentPosition;
								}
								if (currentCharacter == '*') {
									buffer.append(' ');
								} else {
									previousStart = previousStartTemp;
									nextCharacterStart = nextCharacterStartTemp;
								}
								this.scanner.currentPosition = nextCharacterStart;
							}
						}
						addReplaceEdit(start, previousStart - 1, String.valueOf(buffer));
					} else {
						this.column += (nextCharacterStart - previousStart);
					}
			}
			previousStart = nextCharacterStart;
			this.scanner.currentPosition = nextCharacterStart;
		}
		this.lastNumberOfNewLines = 0;
		needSpace = false;
		this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
	}
	
	public void printEndOfCompilationUnit() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespace = false;
			int count = 0;
			while (true) {
				this.currentToken = this.scanner.getNextToken();
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						count = 0;
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count == 0) {
							hasWhitespace = true;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasComment) {
							if (count == 1) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							} else {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							}
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasLineComment) {
							this.preserveEmptyLines(count, this.scanner.getCurrentTokenStartPosition());
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else {
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						currentTokenStartPosition = this.scanner.currentPosition;						
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;		
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.startPosition);
							} else if (count == 1) {
								printNewLine(this.scanner.startPosition);
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						if (this.formatJavadocComment) {
							printJavadocComment(this.scanner.startPosition, this.scanner.currentPosition, -1);
						} else {
							printBlockComment(true);
						}
						printNewLine();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameSEMICOLON :
						print(this.scanner.currentPosition - this.scanner.startPosition, this.formatter.preferences.insert_space_before_semicolon);
						break;
					case TerminalTokens.TokenNameEOF :
						if (count >= 1 || this.formatter.preferences.insert_new_line_at_end_of_file_if_missing) {
							this.printNewLine(this.scannerEndPosition);
						}
						return;
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
	/*
	 * prints a code snippet
	 */
	private void printCodeSnippet(int startPosition, int endPosition) {
		String snippet = new String(this.scanner.source, startPosition, endPosition - startPosition + 1);
		
		// 1 - strip content prefix (@see JavaDocRegion#preprocessCodeSnippet)
		ILineTracker tracker= new DefaultLineTracker();
		String contentPrefix= IJavaDocTagConstants.JAVADOC_STAR;

		StringBuffer inputBuffer= new StringBuffer();
		inputBuffer.setLength(0);
		inputBuffer.append(snippet);
		tracker.set(snippet);
		for (int lines= tracker.getNumberOfLines() - 1; lines > 0; lines--) {
			int lineOffset;
			try {
				lineOffset= tracker.getLineOffset(lines);
			} catch (BadLocationException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
			int prefixOffset= inputBuffer.indexOf(contentPrefix, lineOffset);
			if (prefixOffset >= 0 && inputBuffer.substring(lineOffset, prefixOffset).trim().length() == 0)
				inputBuffer.delete(lineOffset, prefixOffset + 1 + 1);
		}
		
		// 2 - convert HTML to Java (@see JavaDocRegion#convertHtml2Java)
		HTMLEntity2JavaReader reader= new HTMLEntity2JavaReader(new StringReader(inputBuffer.toString()));
		char[] buf= new char[snippet.length()]; // html2text never gets longer, only shorter!
		String convertedSnippet;
		try {
			int read= reader.read(buf);
			convertedSnippet = new String(buf, 0, read);
		} catch (IOException e) {
			// should not happen
			CommentFormatterUtil.log(e);
			return;
		}
		
		// 3 - process snippet (@see JavaDocRegion#formatCodeSnippet)
		String formattedSnippet = convertedSnippet;
		TextEdit edit= CommentFormatterUtil.format2(CodeFormatter.K_UNKNOWN, convertedSnippet, 0, this.lineSeparator, this.formatter.preferences.getMap());
		if (edit != null) {
			formattedSnippet= CommentFormatterUtil.evaluateFormatterEdit(convertedSnippet, edit, null);
		}

		// remove trailing spaces
		formattedSnippet= formattedSnippet.trim();

		// 4 - add the content prefix (@see JavaDocRegion#postprocessCodeSnippet)
		StringBuffer outputBuffer= new StringBuffer();
		tracker= new DefaultLineTracker();
		this.column = 1;
		printIndentationIfNecessary(outputBuffer); // append indentation
		outputBuffer.append(JAVADOC_LINE_PREFIX);
		String linePrefix = outputBuffer.toString();		
		outputBuffer.setLength(0);
		
		// 5 - convert back to HTML (@see JavaDocRegion#convertJava2Html)
		Java2HTMLEntityReader javaReader= new Java2HTMLEntityReader(new StringReader(formattedSnippet));
		buf= new char[256];
		StringBuffer conversionBuffer= new StringBuffer();
		int l;
		try {
			do {
				l= javaReader.read(buf);
				if (l != -1)
					conversionBuffer.append(buf, 0, l);
			} while (l > 0);
			formattedSnippet = conversionBuffer.toString();
		} catch (IOException e) {
			// should not happen
			CommentFormatterUtil.log(e);
			return;
		}
		
		outputBuffer.append(formattedSnippet);
		
		tracker.set(outputBuffer.toString());
		
		for (int lines= tracker.getNumberOfLines() - 1; lines > 0; lines--) {
			try {
				outputBuffer.insert(tracker.getLineOffset(lines), linePrefix);
			} catch (BadLocationException e) {
				// should not happen
				CommentFormatterUtil.log(e);
				return;
			}
		}
		// replace old text with the formatted snippet
		addReplaceEdit(startPosition, endPosition, outputBuffer.toString());		
	}
	
	/*
	 * Main method to print and format comments (javadoc, block and single line comments)
	 */
	public void printComment() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasLineComment = false;
			boolean hasWhitespace = false;
			int count = 0;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						count = 0;
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count == 0) {
							hasWhitespace = true;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasComment) {
							if (count == 1) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							} else {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							}
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (hasLineComment) {
							this.preserveEmptyLines(count, this.scanner.getCurrentTokenStartPosition());
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						} else if (count != 0 && this.formatter.preferences.number_of_empty_lines_to_preserve != 0) {
							addReplaceEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition(), this.getPreserveEmptyLines(count - 1));
						} else {
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						currentTokenStartPosition = this.scanner.currentPosition;						
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;		
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						if (count >= 1) {
							if (count > 1) {
								preserveEmptyLines(count - 1, this.scanner.getCurrentTokenStartPosition());
							} else if (count == 1) {
								printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
						} else if (hasWhitespace) {
							space();
						} 
						hasWhitespace = false;
						if (this.formatJavadocComment) {
							printJavadocComment(this.scanner.startPosition, this.scanner.currentPosition, -1);
						} else {
							printBlockComment(true);
						}
						printNewLine();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = false;
						hasComment = true;
						count = 0;
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
	
	private void printLineComment() {
		int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
		int currentTokenEndPosition = this.scanner.getCurrentTokenEndPosition() + 1;
		if (CharOperation.indexOf(Scanner.TAG_PREFIX, this.scanner.source, true, currentTokenStartPosition, currentTokenEndPosition) != -1) {
			this.nlsTagCounter = 0;
		}
		this.scanner.resetTo(currentTokenStartPosition, currentTokenEndPosition - 1);
		int currentCharacter;
		int start = currentTokenStartPosition;
		int nextCharacterStart = currentTokenStartPosition;
		
		if (this.indentationLevel != 0) {
			if (!this.formatter.preferences.never_indent_line_comments_on_first_column
					|| !isOnFirstColumn(start)) {
				printIndentationIfNecessary();
			}
		}
		if (this.pendingSpace) {
			this.addInsertEdit(currentTokenStartPosition, " "); //$NON-NLS-1$
		}
		this.needSpace = false;
		this.pendingSpace = false;
		int previousStart = currentTokenStartPosition;

		loop: while (nextCharacterStart <= currentTokenEndPosition && (currentCharacter = this.scanner.getNextChar()) != -1) {
			nextCharacterStart = this.scanner.currentPosition;

			switch(currentCharacter) {
				case '\r' :
					start = previousStart;
					break loop;
				case '\n' :
					start = previousStart;
					break loop;
			}
			previousStart = nextCharacterStart;
		}
		if (start != currentTokenStartPosition) {
			// this means that the line comment doesn't end the file
			addReplaceEdit(start, currentTokenEndPosition - 1, lineSeparator);
			this.line++; 
			this.column = 1;
			this.lastNumberOfNewLines = 1;
		}
		this.needSpace = false;
		this.pendingSpace = false;
		// realign to the proper value
		if (this.currentAlignment != null) {
			if (this.memberAlignment != null) {
				// select the last alignment
				if (this.currentAlignment.location.inputOffset > this.memberAlignment.location.inputOffset) {
					if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
						this.currentAlignment.performFragmentEffect();
					}
				} else {
					this.indentationLevel = Math.max(this.indentationLevel, this.memberAlignment.breakIndentationLevel);
				}
			} else if (this.currentAlignment.couldBreak() && this.currentAlignment.wasSplit) {
				this.currentAlignment.performFragmentEffect();
			}
		}
		this.scanner.resetTo(currentTokenEndPosition, this.scannerEndPosition - 1);
	}

	public void printEmptyLines(int linesNumber) {
		this.printEmptyLines(linesNumber, this.scanner.getCurrentTokenEndPosition() + 1);
	}

	private void printEmptyLines(int linesNumber, int insertPosition) {
		final String buffer = getEmptyLines(linesNumber);
		if (Util.EMPTY_STRING == buffer) return;
		addInsertEdit(insertPosition, buffer);
	}

	void printIndentationIfNecessary() {
		StringBuffer buffer = new StringBuffer();
		printIndentationIfNecessary(buffer);
		if (buffer.length() > 0) {
			addInsertEdit(this.scanner.getCurrentTokenStartPosition(), buffer.toString());
			this.pendingSpace = false;
		}
	}

	private void printIndentationIfNecessary(StringBuffer buffer) {
		switch(this.tabChar) {
			case DefaultCodeFormatterOptions.TAB :
				boolean useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
				int numberOfLeadingIndents = this.numberOfIndentations;
				int indentationsAsTab = 0;
				if (useTabsForLeadingIndents) {
					while (this.column <= this.indentationLevel) {
						if (indentationsAsTab < numberOfLeadingIndents) {
							if (buffer != null) buffer.append('\t');
							indentationsAsTab++;
							int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
							this.column += complement;
							this.needSpace = false;
						} else {
							if (buffer != null) buffer.append(' ');
							this.column++;
							this.needSpace = false;
						}
					}
				} else {
					while (this.column <= this.indentationLevel) {
						if (buffer != null) buffer.append('\t');
						int complement = this.tabLength - ((this.column - 1) % this.tabLength); // amount of space
						this.column += complement;
						this.needSpace = false;
					}
				}
				break;
			case DefaultCodeFormatterOptions.SPACE :
				while (this.column <= this.indentationLevel) {
					if (buffer != null) buffer.append(' ');
					this.column++;
					this.needSpace = false;
				}
				break;
			case DefaultCodeFormatterOptions.MIXED :
				useTabsForLeadingIndents = this.useTabsOnlyForLeadingIndents;
				numberOfLeadingIndents = this.numberOfIndentations;
				indentationsAsTab = 0;
				if (useTabsForLeadingIndents) {
					final int columnForLeadingIndents = numberOfLeadingIndents * this.indentationSize;
					while (this.column <= this.indentationLevel) {
						if (this.column <= columnForLeadingIndents) {
							if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
								if (buffer != null) buffer.append('\t');
								this.column += this.tabLength;
							} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
								// print one indentation
								for (int i = 0, max = this.indentationSize; i < max; i++) {
									if (buffer != null) buffer.append(' ');
									this.column++;
								}
							} else {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						} else {
							for (int i = this.column, max = this.indentationLevel; i <= max; i++) {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						}
						this.needSpace = false;
					}
				} else {
					while (this.column <= this.indentationLevel) {
						if ((this.column - 1 + this.tabLength) <= this.indentationLevel) {
							if (buffer != null) buffer.append('\t');
							this.column += this.tabLength;
						} else if ((this.column - 1 + this.indentationSize) <= this.indentationLevel) {
							// print one indentation
							for (int i = 0, max = this.indentationSize; i < max; i++) {
								if (buffer != null) buffer.append(' ');
								this.column++;
							}
						} else {
							if (buffer != null) buffer.append(' ');
							this.column++;
						}
						this.needSpace = false;
					}
				}
				break;
		}
	}

	private void printJavadocBlock(FormatJavadocBlock block) {
		if( block == null) return;

		// Init positions
		int previousEnd = block.tagEnd;
		int maxNodes = block.nodesPtr;

		// format tag section if necessary
		if (!block.isDescription()) {
			this.column += previousEnd - block.sourceStart + 1;
			if (block.isInlined()) 	{
				this.column++; // Add extra character for inline tag
			}
			FormatJavadocNode reference= block.reference;
			if (reference != null) {
				// format between tag name and reference
				addReplaceEdit(previousEnd+1, reference.sourceStart - 1, " "); //$NON-NLS-1$
				this.column++;
				previousEnd = reference.sourceEnd;
				this.column += previousEnd - reference.sourceStart + 1;
			}

			// Nothing else to do if the tag has no node
			if (maxNodes < 0)  return;
		}
		
		// tag section: iterate through the blocks composing this tag but the last one
		FormatJavadocNode previousNode = null;
		for (int i=0; i<=maxNodes; i++) {
			FormatJavadocNode node = block.nodes[i];
			int nodeStart = node.sourceStart;
			
			// Print empty lines before the node
			int textLength = -1;
			if (i == 0) {
				int newLines = this.formatter.preferences.comment_insert_new_line_for_parameter && block.isParamTag() ? 1 : 0;
				if (nodeStart > (previousEnd+1)) {
			   		printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
				} else {
					StringBuffer buffer = new StringBuffer();
					if (newLines > 0) {
						for (int j=0; j<newLines; j++) {
							printJavadocNewLine(buffer);
						}
						addInsertEdit(nodeStart, buffer.toString());
					}
				}
			} else {
				int newLines = this.column > this.formatter.preferences.comment_line_length ? 1 : 0;
				if (node.isText()) {
					// Need to verify if new line is necessary for immutable tag
					FormatJavadocText text = (FormatJavadocText) node;
					if (newLines < text.linesBefore) newLines = text.linesBefore;
					if (newLines == 0 && text.isImmutableHtmlTag()) {
						textLength = getTextLength(block, text);
						if ((this.column + textLength) > this.formatter.preferences.comment_line_length) {
							newLines = 1;
						}
					}
				}
				if (!node.isText() || newLines > 0 || nodeStart > (previousEnd+1)) {
					if (newLines == 0 && previousNode.isText() && node.isText()) {
						// Special case when two texts are consecutive (html tag->text or text->html tag)
						// If no line is to be inserted, then no space should not be inserted either
						// and column reset if the next token is over the max line length
						try {
							this.scanner.resetTo(nodeStart, block.sourceEnd);
							this.scanner.getNextToken();
				    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
				    		if (((FormatJavadocText)node).isHtmlTag()) {
								// read the html tag
								if (this.scanner.getNextToken() == TerminalTokens.TokenNameDIVIDE) {
									tokenLength++;
									this.scanner.getNextToken();
								}
								tokenLength += (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - this.scanner.startPosition;
								this.scanner.getNextToken(); // '>'
								tokenLength++;
							}
							if ((this.column + tokenLength + 1) > this.formatter.preferences.comment_line_length) {
								// finally a new line will be inserted while printing next text, just reset the column
								this.column = 1;
							} else {
								// print one
					   			printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
							}
						} catch (InvalidInputException iie) {
							// skip
						}
					} else {
			   			printJavadocGapLines(previousEnd+1, nodeStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					}
				}
			}

			// Print node
			if (node.isText()) {
				FormatJavadocText text = (FormatJavadocText) node;
				if (text.isHtmlTag()) {
					if (text.isImmutableHtmlTag()) {
						if (textLength == -1) {
							textLength = getTextLength(block, text);
						}
						this.column += textLength;
					} else {
						// Immutable tags are not formatted
//						StringBuffer buffer = new StringBuffer();
						printJavadocHtmlTag(text, block);
//						if ((text.htmlTagIndex & JAVADOC_TAGS_ID_MASK) != JAVADOC_CODE_TAGS_ID) {
//							addReplaceEdit(text.sourceStart, text.sourceEnd, buffer.toString());
//						}
					}
				} else {
					printJavadocText(text, block);
				}
			} else {
				printJavadocBlock((FormatJavadocBlock)node);
			}

			// Print empty lines before the node
			previousEnd = node.sourceEnd;
			previousNode = node;
		}
	}

	private int getTextLength(FormatJavadocBlock block, FormatJavadocText text) {

		// Simple for one line tags
	    if (block.isOneLineTag()) {
	    	return text.sourceEnd - text.sourceStart + 1;
	    }
	    
	    // Find last line
    	int startLine = Util.getLineNumber(text.sourceStart, this.lineEnds, 0, this.maxLines);
    	int endLine = startLine;
    	int previousEnd = -1;
    	for (int i=0; i<=text.separatorsPtr; i++) {
    		int end = (int) (text.separators[i] >>> 32);
    		endLine = Util.getLineNumber(end, this.lineEnds, endLine-1, this.maxLines);
    		if (endLine > startLine) {
    			return previousEnd - text.sourceStart + 1;
    		}
    		previousEnd = end;
    	}

    	// This was a one line text
		return text.sourceEnd - text.sourceStart + 1;
    }
	
	/*
	 * Print and formats a javadoc comments
	 */
	void printJavadocComment(int start, int end, int indentLevel) {
		if (indentLevel > 0) {
			this.numberOfIndentations = indentLevel;
			this.indentationLevel = indentLevel * this.indentationSize;
			this.column = this.indentationLevel + 1;
		}
		int lastIndentationLevel = this.indentationLevel;
		try {
			// parse the comment on the fly
			this.scanner.resetTo(start, end-1);
			if (! this.formatterCommentParser.parse(start, end-1)) {
				// problem occurred while parsing the javadoc, early abort formatting
				return;
			}
			
			FormatJavadoc javadoc = (FormatJavadoc) this.formatterCommentParser.docComment;
			
			// handle indentation
			if (this.indentationLevel != 0) {
				printIndentationIfNecessary();
			}
			
			// handle pending space if any
			if (this.pendingSpace) {
				addInsertEdit(start, " "); //$NON-NLS-1$
			}
			
			if (javadoc.blocks == null) {
				// no FormatJavadocTags in this this javadoc
				return;
			}
			
			// init properly
			this.needSpace = false;
			this.pendingSpace = false;
			int length = javadoc.blocks.length;
			
			// format empty lines between before the first block
			FormatJavadocBlock previousBlock = javadoc.blocks[0];
			this.lastNumberOfNewLines = 0;
			int firstBlockStart = previousBlock.sourceStart;
			printIndentationIfNecessary(null);
			this.column += JAVADOC_HEADER_LENGTH; // consider that the header is already scanned
	
			// If there are several blocks in the javadoc
			int index = 1;
			if (length > 1) {
				// format the description if any
				if (previousBlock.isDescription()) {
					printJavadocBlock(previousBlock);
					FormatJavadocBlock block = javadoc.blocks[index++];
					int newLines = this.formatter.preferences.comment_insert_empty_line_before_root_tags ? 2 : 1;
					printJavadocGapLines(previousBlock.sourceEnd+1, block.sourceStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					previousBlock = block;
				}
	
				// format all tags but the last one composing this comment
				while (index < length) {
					printJavadocBlock(previousBlock);
					FormatJavadocBlock block = javadoc.blocks[index++];
					printJavadocGapLines(previousBlock.sourceEnd+1, block.sourceStart-1, 1, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					previousBlock = block;
				}
			}
	
			// format the last block
			printJavadocBlock(previousBlock);
	
			// format the header and footer empty spaces
			int newLines = this.lastNumberOfNewLines > 0 || javadoc.isMultiLine() ? 1 : 0;
			printJavadocGapLines(javadoc.textStart, firstBlockStart-1, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
			printJavadocGapLines(previousBlock.sourceEnd+1, javadoc.textEnd, newLines, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, true, null);
		}
		finally {
			// reset the scanner
			this.scanner.resetTo(end, this.scannerEndPosition - 1);
			needSpace = false;
			this.indentationLevel = lastIndentationLevel;
			this.lastNumberOfNewLines = 0;
		}
	}
	
	/*
	 * prints the empty javadoc line between the 2 given positions.
	 * May insert new '*' before each new line
	 */
	private void printJavadocGapLines(int textStartPosition, int textEndPosition, int newLines, boolean clearBlankLines, boolean footer, StringBuffer output) {
		try {
			// If no lines to set in the gap then just insert a space if there's enough room to
			if (newLines == 0) {
				if (output == null) {
					addReplaceEdit(textStartPosition, textEndPosition,  " "); //$NON-NLS-1$
				} else {
					output.append(' ');
				}
				this.column++;
				return;
			}

			// if there's no enough room to replace text, then insert the gap
			if (textStartPosition > textEndPosition) {
				if (newLines > 0) {
					StringBuffer buffer = new StringBuffer();
					for (int i=0; i<newLines; i++) {
						buffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(buffer);
						if (footer) {
							buffer.append(' ');
							this.column++;
						} else {
							buffer.append(JAVADOC_LINE_PREFIX);
							this.column += JAVADOC_LINE_PREFIX_LENGTH;
						}
					}
					if (output == null) {
						addInsertEdit(textStartPosition, buffer.toString());
					} else {
						output.append(buffer);
					}
				}
				return;
			}

			// There's enough room and some lines to set...
			// Skip the text token per token to keep existing stars when possible
			this.scanner.resetTo(textStartPosition, textEndPosition);
			this.scanner.recordLineSeparator = true;
			this.scanner.linePtr = Util.getLineNumber(textStartPosition, this.lineEnds, 0, this.maxLines) - 2;
			int linePtr = this.scanner.linePtr;
			int lineCount = 0;
			int start = textStartPosition;
			while (!this.scanner.atEnd()) {
				switch (this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameMULTIPLY:
						// we just need to replace each lines between '*' with the javadoc formatted ones
						int linesGap = this.scanner.linePtr - linePtr;
						if (linesGap > 0) {
							StringBuffer buffer = new StringBuffer();
							if (lineCount > 0) {
								// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
								buffer.append( ' ');
							}
							for (int i = 0; i < linesGap ; i++) {
								if (clearBlankLines && lineCount >= newLines) {
									// leave as the required new lines have been inserted
									// so remove any remaining blanks and leave
									if (textEndPosition >= start) {
										if (output == null) {
											addReplaceEdit(start, textEndPosition, buffer.toString());
										} else {
											output.append(buffer);
										}
									}
									return;
								}
								buffer.append(this.lineSeparator);
								this.column = 1;
								printIndentationIfNecessary(buffer);
								if (i == (linesGap-1)) {
									buffer.append(' ');
									this.column++;
								} else {
									buffer.append(JAVADOC_LINE_PREFIX);
									this.column += JAVADOC_LINE_PREFIX_LENGTH;
								}
								lineCount++;
							}
							int currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
							int tokenLength = this.scanner.currentPosition - currentTokenStartPosition;
							if (output == null) {
								addReplaceEdit(start, currentTokenStartPosition-1, buffer.toString());
							} else {
								output.append(buffer);
								output.append(this.scanner.source, currentTokenStartPosition, tokenLength);
							}
							this.column += tokenLength;
							if (footer && clearBlankLines && lineCount == newLines) {
								if (textEndPosition >= currentTokenStartPosition) {
									if (output == null) {
										addDeleteEdit(currentTokenStartPosition, textEndPosition);
									}
								}
								return;
							}
						}
						// next start is just after the current token
						start = this.scanner.currentPosition;
						linePtr = this.scanner.linePtr;
						break;
				}
			}

			// Format the last whitespaces
			if (lineCount < newLines) {
				// Insert new lines as not enough was encountered while scanning the whitespaces
				StringBuffer buffer = new StringBuffer();
				if (lineCount > 0) {
					// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
					buffer.append( ' ');
				}
				for (int i = lineCount; i < newLines-1; i++) {
					printJavadocNewLine(buffer);
				}
				buffer.append(this.lineSeparator);
				this.column = 1;
				printIndentationIfNecessary(buffer);
				if (footer) {
					buffer.append(' ');
					this.column++;
				} else {
					buffer.append(JAVADOC_LINE_PREFIX);
					this.column += JAVADOC_LINE_PREFIX_LENGTH;
				}
				if (output == null) {
					if (textEndPosition >= start) {
						addReplaceEdit(start, textEndPosition, buffer.toString());
					} else {
						addInsertEdit(textEndPosition+1, buffer.toString());
					}
				} else {
					output.append(buffer);
				}
			} else {
				// Replace all remaining whitespaces by a single space
				if (textEndPosition >= start) {
					StringBuffer buffer = new StringBuffer();
					if (this.scanner.linePtr > linePtr) {
						if (lineCount > 0) {
							// TODO (eric) https://bugs.eclipse.org/bugs/show_bug.cgi?id=49619
							buffer.append( ' ');
						}
						buffer.append(this.lineSeparator);
						this.column = 1;
						printIndentationIfNecessary(buffer);
					}
					buffer.append(' ');
					if (output == null) {
						addReplaceEdit(start, textEndPosition, buffer.toString());
					} else {
						output.append(buffer);
					}
				}
				this.column++;
			}
		}		
		catch (InvalidInputException iie) {
			// there's nothing to do if this exception happens
		}
		finally {
			this.scanner.recordLineSeparator = false;
			needSpace = false;
			this.scanner.resetTo(textEndPosition+1, this.scannerEndPosition - 1);
			this.lastNumberOfNewLines += newLines;
		}
	}

	private int printJavadocHtmlTag(FormatJavadocText text, FormatJavadocBlock block) {
		
		// Local variables init
		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;
		int textStart = text.sourceStart;
		int nextStart = textStart;
		int startLine = Util.getLineNumber(textStart, this.lineEnds, 0, this.maxLines);
		boolean textOnNewLine = (block.isParamTag() && this.formatter.preferences.comment_insert_new_line_for_parameter) || !block.hasTextOnTagLine();
	    int tagCategory =  text.getHtmlTagID();
	    StringBuffer buffer = new StringBuffer();

	    // New line will be added before next node
	    int max = text.separatorsPtr;
		int linesAfter = 0;
		int previousEnd = -1;
	    if (tagCategory != JAVADOC_SINGLE_BREAK_TAG_ID) {

			// Iterate on text line separators
			boolean isCode = tagCategory == JAVADOC_CODE_TAGS_ID;
			for (int i=0, ptr=0; i<=max ; i++) {
	
				// append text to buffer realigning with the line length
				int end = (int) (text.separators[i] >>> 32);
				boolean wasHtmlTag = false;
				if (text.htmlNodesPtr >= 0 && ptr <= text.htmlNodesPtr && end > text.htmlNodes[ptr].sourceStart) {
					FormatJavadocNode node = text.htmlNodes[ptr];
					FormatJavadocText htmlTag = node.isText() ? (FormatJavadocText) node : null;
					int newLines = htmlTag == null ? 0 : htmlTag.linesBefore;
					if (linesAfter > newLines) {
						newLines = linesAfter;
						if (newLines > 1 && clearBlankLines) newLines = 1;
					}
					if (textStart < previousEnd) addReplaceEdit(textStart, previousEnd, buffer.toString());
					printJavadocGapLines(previousEnd+1, node.sourceStart-1, newLines, clearBlankLines, false, null);
					if (newLines > 0) textOnNewLine = true;
					if (node.isText()) {
						linesAfter = printJavadocHtmlTag(htmlTag, block);
					} else {
						printJavadocBlock((FormatJavadocBlock)node);
						linesAfter = 0;
					}
					buffer = new StringBuffer();
					textStart = node.sourceEnd + 1;
					ptr++;
					wasHtmlTag = true;
				} else {
					if (i > 0 && linesAfter > 0) {
						printJavadocGapLines(previousEnd+1, nextStart, linesAfter, clearBlankLines, false, buffer);
						textOnNewLine = true;
					}
					boolean needIndentation = buffer.length() == 0 && textOnNewLine;
					printJavadocTextLine(buffer, nextStart, end, block, i==1/*first text?*/, needIndentation, i==0/* opening html tag?*/);
				    if (i==0 && tagCategory == JAVADOC_SEPARATOR_TAGS_ID) {
				    	linesAfter = 1;
					} else {
						linesAfter = 0;
					}
				}

				// Replace with current buffer if there are several empty lines between text lines
				nextStart = (int) text.separators[i];
				if (!clearBlankLines && (wasHtmlTag || i==0 || i==max)) {
					int endLine = Util.getLineNumber(end, this.lineEnds, startLine-1, this.maxLines);
					startLine = Util.getLineNumber(nextStart, this.lineEnds, endLine-1, this.maxLines);
					if (linesAfter < (startLine - endLine)) {
						linesAfter = startLine - endLine;
					}
				}

				// print <pre> tag
				if (isCode) {
	    			int codeEnd = (int) (text.separators[max] >>> 32);
	    			if (codeEnd > end) {
	    				if (this.formatter.preferences.comment_format_source) {
							if (textStart < end) addReplaceEdit(textStart, end, buffer.toString());
			    			printJavadocGapLines(end+1, nextStart-1, 1, false/* never clear blank lines inside <pre> tag*/, false, null);
							printCodeSnippet(nextStart, codeEnd);
							nextStart = (int) text.separators[max];
		    				printJavadocGapLines(codeEnd+1, nextStart-1, 1, false/* never clear blank lines inside <pre> tag*/, false, null);
		    				return 2;
	    				}
	    			} else {
						nextStart = (int) text.separators[max];
						if ((nextStart-1) > (end+1)) {
							int line1 = Util.getLineNumber(end+1, this.lineEnds, startLine-1, this.maxLines);
							int line2 = Util.getLineNumber(nextStart-1, this.lineEnds, line1-1, this.maxLines);
		    				int gapLines = line2-line1-1;
							printJavadocGapLines(end+1, nextStart-1, gapLines, false/* never clear blank lines inside <pre> tag*/, false, null);
							if (gapLines > 0) textOnNewLine = true;
						}
	    			}
    				return 1;
				}
				
				// store previous end
				previousEnd = end;
			}
	    }
		
		// Insert last gap
		if (previousEnd != -1) {
		    if (max > 0 && tagCategory == JAVADOC_SEPARATOR_TAGS_ID) {
				if (linesAfter == 0) linesAfter = 1;
			}
			if (linesAfter > 0) {
				int newLines = linesAfter;
//				if (newLines > 1 && clearBlankLines) newLines = 1;
				printJavadocGapLines(previousEnd+1, nextStart-1, newLines, clearBlankLines, false, buffer);
			}
		}
	    
	    // Print closing tag
		boolean needIndentation = buffer.length() == 0 && textOnNewLine;
		printJavadocTextLine(buffer, nextStart, text.sourceEnd, block, false /*not the first text*/, needIndentation, true/* closing html tag*/);
		if (textStart < text.sourceEnd) {
			addReplaceEdit(textStart, text.sourceEnd, buffer.toString());
		}

		// Reset
		needSpace = false;
		this.scanner.resetTo(text.sourceEnd+1, this.scannerEndPosition - 1);
		
		// Return the new lines to insert after
	    if (max > 0 && tagCategory == JAVADOC_SEPARATOR_TAGS_ID) {
			return 1;
		}
	    return 0;
	}

	private void printJavadocNewLine(StringBuffer buffer) {
	    buffer.append(this.lineSeparator);
	    this.column = 1;
	    printIndentationIfNecessary(buffer);
	    buffer.append(JAVADOC_LINE_PREFIX);
	    this.column += JAVADOC_LINE_PREFIX_LENGTH;
    }

	private void printJavadocText(FormatJavadocText text, FormatJavadocBlock block) {

		boolean clearBlankLines = this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment;
		StringBuffer buffer = new StringBuffer();
		int textStart = text.sourceStart;
		int nextStart = textStart;
		int startLine = Util.getLineNumber(textStart, this.lineEnds, 0, this.maxLines);
		boolean textOnNewLine = text == block.nodes[0] && block.isParamTag() && (this.formatter.preferences.comment_insert_new_line_for_parameter || !block.hasTextOnTagLine());

		// Iterate on text line separators
		for (int i=0, max=text.separatorsPtr; i<=max ; i++) {

			// append text to buffer realigning with the line length
			int end = (int) (text.separators[i] >>> 32);
			boolean needIndentation = buffer.length() == 0 && textOnNewLine;
			printJavadocTextLine(buffer, nextStart, end, block, i==0 /*first text?*/, needIndentation, false /*not an html tag*/);

			// Replace with current buffer if there are several empty lines between text lines
			nextStart = (int) text.separators[i];
			if (!clearBlankLines) {
				int endLine = Util.getLineNumber(end, this.lineEnds, startLine-1, this.maxLines);
				startLine = Util.getLineNumber(nextStart, this.lineEnds, endLine-1, this.maxLines);
				if (startLine > (endLine+1)) {
					addReplaceEdit(textStart, end, buffer.toString());
					textStart = nextStart;
					buffer = new StringBuffer();
					printJavadocGapLines(end+1, nextStart-1, startLine - endLine, this.formatter.preferences.comment_clear_blank_lines_in_javadoc_comment, false, null);
					textOnNewLine = true;
				}
			}
		}
		
		// Replace remaining line
		boolean needIndentation = buffer.length() == 0 && textOnNewLine;
		printJavadocTextLine(buffer, nextStart, text.sourceEnd, block, text.separatorsPtr==-1 /* first text?*/, needIndentation, false /*not an html tag*/);
		// TODO (eric) Bring back following optimization
		// if (lastNewLines != this.lastNumberOfNewLines || (this.column - currentColumn) != (text.sourceEnd - text.sourceStart + 1)) {
			addReplaceEdit(textStart, text.sourceEnd, buffer.toString());
		// }

		// Reset
		needSpace = false;
		this.scanner.resetTo(text.sourceEnd+1, this.scannerEndPosition - 1);
	}

	/*
	 * Returns whether the text has been modified or not.
	 */
	private void printJavadocTextLine(StringBuffer buffer, int textStart, int textEnd, FormatJavadocBlock block, boolean firstText, boolean needIndentation, boolean isHtmlTag) {

		boolean indentRootTags = this.formatter.preferences.comment_indent_root_tags && !block.isDescription();
		boolean indentParamTag = this.formatter.preferences.comment_indent_parameter_description && block.isParamTag();
		boolean headerLine = block.isHeaderLine() && this.lastNumberOfNewLines == 0;
		StringBuffer textBuffer = isHtmlTag ? new StringBuffer() : buffer;
		
		// First we need to know what is the indentation
		int firstColumn = 1 + this.indentationLevel + JAVADOC_LINE_PREFIX_LENGTH;
		if (headerLine) firstColumn++;
		StringBuffer indentationBuffer = null;
		if (indentRootTags) {
			int indentLevel = this.indentationLevel;
			int indentations = this.numberOfIndentations;
			this.numberOfIndentations += (JAVADOC_LINE_PREFIX_LENGTH / this.indentationSize) + 1;
			this.indentationLevel = this.numberOfIndentations * this.indentationSize;
			int currentColumn = this.column;
			this.column = firstColumn;
			if (indentParamTag) {
				this.indentationLevel += this.indentationSize;
				this.numberOfIndentations++;
			}
			printIndentationIfNecessary(indentationBuffer = new StringBuffer());
			if (needIndentation) {
				this.column = firstColumn;
				printIndentationIfNecessary(textBuffer);
			}
			firstColumn = this.indentationLevel + 1;
			this.column = currentColumn < firstColumn ? firstColumn : currentColumn;
			this.indentationLevel = indentLevel;
			this.numberOfIndentations = indentations;
		} else if (this.column < firstColumn) {
			this.column = firstColumn;
		}

		// Scan the text token per token to compact it and size it the max line length
		int maxColumn = this.formatter.preferences.comment_line_length + 1;
		try {
			this.scanner.resetTo(textStart, textEnd);
			this.scanner.skipComments = true;
			int previousToken = -1;

			// Consume text token per token
    		while (!this.scanner.atEnd()) {
				int token;
				try {
					token = this.scanner.getNextToken();
				} catch (InvalidInputException iie) {
					String msg = iie.getMessage();
					if (msg == Scanner.INVALID_CHARACTER_CONSTANT) {
						buffer.append('\'');
					} else if (msg == Scanner.INVALID_CHAR_IN_STRING) {
						buffer.append('"');
					} else {
						throw iie;
					}
					this.column++;
					// Need to retrieve correct position
					this.scanner.resetTo(this.scanner.startPosition, textEnd);
					this.scanner.getNextChar();
					continue;
				}
				if (token == TerminalTokens.TokenNameWHITESPACE) {
					previousToken = token;
					continue;
				}
    			int tokenStart = this.scanner.getCurrentTokenStartPosition();
	    		int tokenLength = (this.scanner.atEnd() ? this.scanner.eofPosition : this.scanner.currentPosition) - tokenStart;
	    		boolean insertSpace = previousToken == TerminalTokens.TokenNameWHITESPACE || (tokenStart == textStart && this.column > firstColumn && !(firstText || isHtmlTag));
				this.column += tokenLength;
	    		if (insertSpace) this.column++;
	    		int col = column; 
	    		if (headerLine) {
	    			// special case when text is on the same line of the javadoc's header
	    			if (col > maxColumn)  {
	    				col--; // new line gives an extra character
	    				this.lastNumberOfNewLines++; // in case we leave just after
	    			}
	    		}
				if (col > maxColumn) {
					// not enough space on the line
					if (col == this.column) this.lastNumberOfNewLines++;
			    	buffer.append(this.lineSeparator);
			    	this.column = 1;
			    	printIndentationIfNecessary(buffer);
		    		buffer.append(JAVADOC_LINE_PREFIX);
			    	this.column = firstColumn;
			    	if (indentationBuffer != null) {
			    		buffer.append(indentationBuffer);
			    	}
					if (isHtmlTag && buffer != textBuffer) {
						buffer.append(textBuffer);
						textBuffer = buffer;
					}
					textBuffer.append(this.scanner.source, tokenStart, tokenLength);
					this.column += tokenLength;
					if (headerLine) {
						firstColumn--;
						headerLine = false;
					}
    			} else {
					// append token to the line
					if (insertSpace)  {
		    			textBuffer.append(' ');
					}
					textBuffer.append(this.scanner.source, tokenStart, tokenLength);
    			}
				previousToken = token;
    		}
		} catch (InvalidInputException iie) {
			// leave now
		}
		finally {
			this.scanner.skipComments = false;
			if (isHtmlTag && buffer != textBuffer) {
				buffer.append(textBuffer);
			}
		}
    }

	public void printModifiers(Annotation[] annotations, ASTVisitor visitor) {
		printModifiers(annotations, visitor, ICodeFormatterConstants.ANNOTATION_UNSPECIFIED);
	}
	
	public void printModifiers(Annotation[] annotations, ASTVisitor visitor, int annotationSourceKind) {
		try {
			int annotationsLength = annotations != null ? annotations.length : 0;
			int annotationsIndex = 0;
			boolean isFirstModifier = true;
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasComment = false;
			boolean hasModifiers = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNamepublic :
					case TerminalTokens.TokenNameprotected :
					case TerminalTokens.TokenNameprivate :
					case TerminalTokens.TokenNamestatic :
					case TerminalTokens.TokenNameabstract :
					case TerminalTokens.TokenNamefinal :
					case TerminalTokens.TokenNamenative :
					case TerminalTokens.TokenNamesynchronized :
					case TerminalTokens.TokenNametransient :
					case TerminalTokens.TokenNamevolatile :
					case TerminalTokens.TokenNamestrictfp :
						hasModifiers = true;
						print(this.scanner.currentPosition - this.scanner.startPosition, !isFirstModifier);
						isFirstModifier = false;
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameAT :
						hasModifiers = true;
						if (!isFirstModifier) {
							this.space();
						}
						this.scanner.resetTo(this.scanner.getCurrentTokenStartPosition(), this.scannerEndPosition - 1);
						if (annotationsIndex < annotationsLength) {
							annotations[annotationsIndex++].traverse(visitor, (BlockScope) null);
							// https://bugs.eclipse.org/bugs/show_bug.cgi?id=122247
							boolean shouldAddNewLine = false;
							switch (annotationSourceKind) {
								case ICodeFormatterConstants.ANNOTATION_ON_MEMBER :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_member) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_PARAMETER :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_parameter) {
										shouldAddNewLine = true;
									}
									break;
								case ICodeFormatterConstants.ANNOTATION_ON_LOCAL_VARIABLE :
									if (this.formatter.preferences.insert_new_line_after_annotation_on_local_variable) {
										shouldAddNewLine = true;
									}
									break;
								default:
									// do nothing when no annotation formatting option specified
							}
							if (shouldAddNewLine) {
								this.printNewLine();
							}
						} else {
							return;
						}
						isFirstModifier = false;
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(true);
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (count >= 1 && hasComment) {
							printNewLine();
						}
						currentTokenStartPosition = this.scanner.currentPosition;
						hasComment = false;
						break;
					default:
						if (hasModifiers) {
							this.space();
						}
						// step back one token
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;					
				}
			}
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}
	
	public void printNewLine() {
		this.printNewLine(this.scanner.getCurrentTokenEndPosition() + 1);
	}

	public void printNewLine(int insertPosition) {
		if (this.nlsTagCounter > 0) {
			return;
		}
		if (lastNumberOfNewLines >= 1) {
			column = 1; // ensure that the scribe is at the beginning of a new line
			return;
		}
		addInsertEdit(insertPosition, this.lineSeparator);
		line++;
		lastNumberOfNewLines = 1;
		column = 1;
		needSpace = false;
		this.pendingSpace = false;
	}

	public void printNextToken(int expectedTokenType){
		printNextToken(expectedTokenType, false);
	}

	public void printNextToken(int expectedTokenType, boolean considerSpaceIfAny){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
			if (expectedTokenType != this.currentToken) {
				throw new AbortFormatting("unexpected token type, expecting:"+expectedTokenType+", actual:"+this.currentToken);//$NON-NLS-1$//$NON-NLS-2$
			}
			print(this.scanner.currentPosition - this.scanner.startPosition, considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printNextToken(int[] expectedTokenTypes) {
		printNextToken(expectedTokenTypes, false);
	}

	public void printNextToken(int[] expectedTokenTypes, boolean considerSpaceIfAny){
		printComment();
		try {
			this.currentToken = this.scanner.getNextToken();
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
			print(this.scanner.currentPosition - this.scanner.startPosition, considerSpaceIfAny);
		} catch (InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printArrayQualifiedReference(int numberOfTokens, int sourceEnd) {
		int currentTokenStartPosition = this.scanner.currentPosition;
		int numberOfIdentifiers = 0;
		try {
			do {
				this.printComment();
				switch(this.currentToken = this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameEOF :
						return;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameIdentifier :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						if (++ numberOfIdentifiers == numberOfTokens) {
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						}
						break;						
					case TerminalTokens.TokenNameDOT :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					default:
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			} while (this.scanner.currentPosition <= sourceEnd);
		} catch(InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	public void printQualifiedReference(int sourceEnd) {
		int currentTokenStartPosition = this.scanner.currentPosition;
		try {
			do {
				this.printComment();
				switch(this.currentToken = this.scanner.getNextToken()) {
					case TerminalTokens.TokenNameEOF :
						return;
					case TerminalTokens.TokenNameWHITESPACE :
						addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
					case TerminalTokens.TokenNameCOMMENT_JAVADOC :
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					case TerminalTokens.TokenNameIdentifier :
					case TerminalTokens.TokenNameDOT :
						print(this.scanner.currentPosition - this.scanner.startPosition, false);
						currentTokenStartPosition = this.scanner.currentPosition;
						break;
					default:
						this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
						return;
				}
			} while (this.scanner.currentPosition <= sourceEnd);
		} catch(InvalidInputException e) {
			throw new AbortFormatting(e);
		}
	}

	private void printRule(StringBuffer stringBuffer) {
		for (int i = 0; i < this.pageWidth; i++){
			if ((i % this.tabLength) == 0) { 
				stringBuffer.append('+');
			} else {
				stringBuffer.append('-');
			}
		}
		stringBuffer.append(this.lineSeparator);
		
		for (int i = 0; i < (pageWidth / tabLength); i++) {
			stringBuffer.append(i);
			stringBuffer.append('\t');
		}			
	}

	public void printTrailingComment(int numberOfNewLinesToInsert) {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasWhitespaces = false;
			boolean hasLineComment = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (hasLineComment) {
							if (count >= 1) {
								currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
								this.preserveEmptyLines(numberOfNewLinesToInsert, currentTokenStartPosition);
								addDeleteEdit(currentTokenStartPosition, this.scanner.getCurrentTokenEndPosition());
								this.scanner.resetTo(this.scanner.currentPosition, this.scannerEndPosition - 1);
								return;
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (count > 1) {
							this.printEmptyLines(numberOfNewLinesToInsert, this.scanner.getCurrentTokenStartPosition());
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else {
							hasWhitespaces = true;
							currentTokenStartPosition = this.scanner.currentPosition;						
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (hasWhitespaces) {
							space();
						}
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (hasWhitespaces) {
							space();
						}
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
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
	public void printTrailingComment() {
		try {
			// if we have a space between two tokens we ensure it will be dumped in the formatted string
			int currentTokenStartPosition = this.scanner.currentPosition;
			boolean hasWhitespaces = false;
			boolean hasComment = false;
			boolean hasLineComment = false;
			while ((this.currentToken = this.scanner.getNextToken()) != TerminalTokens.TokenNameEOF) {
				switch(this.currentToken) {
					case TerminalTokens.TokenNameWHITESPACE :
						int count = 0;
						char[] whiteSpaces = this.scanner.getCurrentTokenSource();
						for (int i = 0, max = whiteSpaces.length; i < max; i++) {
							switch(whiteSpaces[i]) {
								case '\r' :
									if ((i + 1) < max) {
										if (whiteSpaces[i + 1] == '\n') {
											i++;
										}
									}
									count++;
									break;
								case '\n' :
									count++;
							}
						}
						if (hasLineComment) {
							if (count >= 1) {
								currentTokenStartPosition = this.scanner.getCurrentTokenStartPosition();
								this.preserveEmptyLines(count, currentTokenStartPosition);
								addDeleteEdit(currentTokenStartPosition, this.scanner.getCurrentTokenEndPosition());
								this.scanner.resetTo(this.scanner.currentPosition, this.scannerEndPosition - 1);
								return;
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else if (count >= 1) {
							if (hasComment) {
								this.printNewLine(this.scanner.getCurrentTokenStartPosition());
							}
							this.scanner.resetTo(currentTokenStartPosition, this.scannerEndPosition - 1);
							return;
						} else {
							hasWhitespaces = true;
							currentTokenStartPosition = this.scanner.currentPosition;
							addDeleteEdit(this.scanner.getCurrentTokenStartPosition(), this.scanner.getCurrentTokenEndPosition());
						}
						break;
					case TerminalTokens.TokenNameCOMMENT_LINE :
						if (hasWhitespaces) {
							space();
						}
						printLineComment();
						currentTokenStartPosition = this.scanner.currentPosition;
						hasLineComment = true;
						break;
					case TerminalTokens.TokenNameCOMMENT_BLOCK :
						if (hasWhitespaces) {
							space();
						}
						printBlockComment(false);
						currentTokenStartPosition = this.scanner.currentPosition;
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
	}

	void redoMemberAlignment(AlignmentException e){
		// reset scribe/scanner to restart at this given location
		this.resetAt(this.memberAlignment.location);
		this.scanner.resetTo(this.memberAlignment.location.inputOffset, this.scanner.eofPosition);
		// clean alignment chunkKind so it will think it is a new chunk again
		this.memberAlignment.chunkKind = 0;
	}

	public void reset() {
		this.checkLineWrapping = true;
		this.line = 0;
		this.column = 1;
		this.editsIndex = 0;
		this.nlsTagCounter = 0;
	}
		
	private void resetAt(Location location) {
		this.line = location.outputLine;
		this.column = location.outputColumn;
		this.indentationLevel = location.outputIndentationLevel;
		this.numberOfIndentations = location.numberOfIndentations;
		this.lastNumberOfNewLines = location.lastNumberOfNewLines;
		this.needSpace = location.needSpace;
		this.pendingSpace = location.pendingSpace;
		this.editsIndex = location.editsIndex;
		this.nlsTagCounter = location.nlsTagCounter;
		if (this.editsIndex > 0) {
			this.edits[this.editsIndex - 1] = location.textEdit;
		}
		this.formatter.lastLocalDeclarationSourceStart = location.lastLocalDeclarationSourceStart;
	}

	private void resize() {
		System.arraycopy(this.edits, 0, (this.edits = new OptimizedReplaceEdit[this.editsIndex * 2]), 0, this.editsIndex);
	}

	public void space() {
		if (!this.needSpace) return;
		this.lastNumberOfNewLines = 0;
		this.pendingSpace = true;
		this.column++;
		this.needSpace = false;		
	}

	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer
			.append("(page width = " + this.pageWidth + ") - (tabChar = ");//$NON-NLS-1$//$NON-NLS-2$
		switch(this.tabChar) {
			case DefaultCodeFormatterOptions.TAB :
				 stringBuffer.append("TAB");//$NON-NLS-1$
				 break;
			case DefaultCodeFormatterOptions.SPACE :
				 stringBuffer.append("SPACE");//$NON-NLS-1$
				 break;
			default :
				 stringBuffer.append("MIXED");//$NON-NLS-1$
		}
		stringBuffer
			.append(") - (tabSize = " + this.tabLength + ")")//$NON-NLS-1$//$NON-NLS-2$
			.append(this.lineSeparator)
			.append("(line = " + this.line + ") - (column = " + this.column + ") - (identationLevel = " + this.indentationLevel + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(this.lineSeparator)
			.append("(needSpace = " + this.needSpace + ") - (lastNumberOfNewLines = " + this.lastNumberOfNewLines + ") - (checkLineWrapping = " + this.checkLineWrapping + ")")	//$NON-NLS-1$	//$NON-NLS-2$	//$NON-NLS-3$	//$NON-NLS-4$
			.append(this.lineSeparator)
			.append("==================================================================================")	//$NON-NLS-1$
			.append(this.lineSeparator);
		printRule(stringBuffer);
		return stringBuffer.toString();
	}
	
	public void unIndent() {
		this.indentationLevel -= this.indentationSize;
		this.numberOfIndentations--;
	}
}

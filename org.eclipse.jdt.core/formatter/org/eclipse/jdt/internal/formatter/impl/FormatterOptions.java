package org.eclipse.jdt.internal.formatter.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.formatter.*;
import java.util.*;

public class FormatterOptions {	

	/**
	 * Option IDs
	 */
	public static final String OPTION_InsertNewlineBeforeOpeningBrace = CodeFormatter.class.getName() + ".newlineOpeningBrace"/*nonNLS*/;
	public static final String OPTION_InsertNewlineInControlStatement = CodeFormatter.class.getName() + ".newlineControlStatement"/*nonNLS*/;
	public static final String OPTION_InsertNewLineBetweenElseAndIf = CodeFormatter.class.getName() + ".newlineElseIf"/*nonNLS*/;
	public static final String OPTION_InsertNewLineInEmptyBlock = CodeFormatter.class.getName() + ".newlineEmptyBlock"/*nonNLS*/;
	public static final String OPTION_ClearAllBlankLines = CodeFormatter.class.getName() + ".newlineClearAll"/*nonNLS*/;
	public static final String OPTION_SplitLineExceedingLength = CodeFormatter.class.getName() + ".lineSplit"/*nonNLS*/;
	public static final String OPTION_CompactAssignment = CodeFormatter.class.getName() + ".compactAssignment"/*nonNLS*/;
	public static final String OPTION_TabulationChar = CodeFormatter.class.getName() + ".tabulationChar"/*nonNLS*/;
	public static final String OPTION_TabulationSize = CodeFormatter.class.getName() + ".tabulationSize"/*nonNLS*/;
	
	// by default, do not insert blank line before opening brace
	public boolean newLineBeforeOpeningBraceMode = false;

	// by default, do not insert blank line behind keywords (ELSE, CATCH, FINALLY,...) in control statements
	public boolean newlineInControlStatementMode = false;

	// by default, preserve one blank line per sequence of blank lines
	public boolean clearAllBlankLinesMode = false;
	
	// line splitting will occur when line exceeds this length
	public int maxLineLength = 80;

	public boolean compactAssignmentMode = false; // if isTrue, assignments look like x= 12 (not like x = 12);

	//number of consecutive spaces used to replace the tab char
	public int tabSize = 4; // n spaces for one tab
	public boolean indentWithTab = true;

	public boolean compactElseIfMode = false; // if true, else and if are kept on the same line.
	public boolean newLineInEmptyBlockMode = true; // if false, no new line in {} if it's empty.
	
	public char[] lineSeparatorSequence = System.getProperty("line.separator"/*nonNLS*/).toCharArray();
/** 
 * Initializing the formatter options with default settings
 */
public FormatterOptions(){
}
/** 
 * Initializing the formatter options with external settings
 */
public FormatterOptions(ConfigurableOption[] settings){
	if (settings == null) return;

	// filter options which are related to the formatter component
	String componentName = CodeFormatter.class.getName();
	for (int i = 0, max = settings.length; i < max; i++){
		if (settings[i].getComponentName().equals(componentName)){
			this.setOption(settings[i]);
		}
	}
}

/**
 * 
 * @return int
 */
public int getMaxLineLength() {
	return maxLineLength;
}
public int getTabSize() {
	return tabSize;
}
public boolean isAddingNewLineBeforeOpeningBrace() {
	return newLineBeforeOpeningBraceMode;
}
public boolean isAddingNewLineInControlStatement() {
	return newlineInControlStatementMode;
}
public boolean isAddingNewLineInEmptyBlock() {
	return newLineInEmptyBlockMode;
}
public boolean isClearingAllBlankLines() {
	return clearAllBlankLinesMode;
}
public boolean isCompactingAssignment() {
	return compactAssignmentMode;
}
public boolean isCompactingElseIf() {
	return compactElseIfMode;
}
public boolean isUsingTabForIndenting() {
	return indentWithTab;
}
public void setClearAllBlankLinesMode(boolean flag) {
	clearAllBlankLinesMode = flag;
}
/** Set the behaviour of the formatter about the braces.<br>
 * @param boolean newBraceIndentationLevel<ul>
 * <li>if true, the formatter add new line & indent before the opening brace.
 * <li>if false, the formatter leaves the brace on the same line.</ul> 
 */
public void setCompactAssignmentMode(boolean flag) {
	compactAssignmentMode = flag;
}
/** Set the behaviour of the formatter about else if.<br>
 * @param boolean flag<ul>
 * <li>if true, a <code>else if</code> sequence is kept on the same line.
 * <li>if false, <code>else if</code> is formatted like:
 <pre>
 else
 	if
 </pre>
 </ul> 
 */
public void setCompactElseIfMode(boolean flag) {
	compactElseIfMode = flag;
}
/** Defines whether to use tab characters or sequence of spaces when indenting
 * @param boolean useTab <ul>
 * <li>if true, the formatter add new line & indent before the opening brace.
 * <li>if false, the formatter leaves the brace on the same line.</ul> 
 */
public void setIndentationUsesTab(boolean flag) {
	indentWithTab = flag;
}
public void setLineSeparator(String lineSeparator) {
	lineSeparatorSequence = lineSeparator.toCharArray();
}
public void setMaxLineLength(int maxLineLength) {
	this.maxLineLength = maxLineLength;
}
/** Set the behaviour of the formatter about the braces.<br>
 * @param boolean newBraceIndentationLevel<ul>
 * <li>if true, the formatter add new line & indent before the opening brace.
 * <li>if false, the formatter leaves the brace on the same line.</ul> 
 */
public void setNewLineBeforeOpeningBraceMode(boolean flag) {
	newLineBeforeOpeningBraceMode = flag;
}
public void setNewlineInControlStatementMode(boolean flag) {
	newlineInControlStatementMode = flag;
}
public void setNewLineInEmptyBlockMode(boolean flag) {
	newLineInEmptyBlockMode = flag;
}
/**
 * Change the value of the option corresponding to the option number
 *
 * @param optionNumber <CODE>int</CODE>
 * @param newValue <CODE>int</CODE>
 */
public void setOption(ConfigurableOption setting) {
	
	String optionID = setting.getID();
	
	if(optionID.equals(OPTION_InsertNewlineBeforeOpeningBrace)){
		setNewLineBeforeOpeningBraceMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_InsertNewlineInControlStatement)){
		setNewlineInControlStatementMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_ClearAllBlankLines)){
		setClearAllBlankLinesMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_InsertNewLineBetweenElseAndIf)){
		setCompactElseIfMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_InsertNewLineInEmptyBlock)){
		setNewLineInEmptyBlockMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_SplitLineExceedingLength)){
		try {
			setMaxLineLength(Integer.parseInt(setting.getValue()));
		} catch(NumberFormatException e){
		}
	}else if(optionID.equals(OPTION_CompactAssignment)){
		setCompactAssignmentMode(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_TabulationChar)){
		setIndentationUsesTab(setting.getValueIndex() == 0);
	}else if(optionID.equals(OPTION_TabulationSize)){
		try {
			setTabSize(Integer.parseInt(setting.getValue()));
		} catch(NumberFormatException e){
		}
	}
}

public void setReuseExistingLayoutMode(boolean flag) {
}
public void setTabSize(int size) {
	this.tabSize = size;
}
}

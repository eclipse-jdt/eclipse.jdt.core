package org.eclipse.jdt.internal.formatter.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.formatter.*;
import java.util.*;

public class FormatterOptions {

	// by default, do not insert blank line before opening brace
	public boolean newLineBeforeOpeningBraceMode = false;

	// by default, do not insert blank line behind keywords (ELSE, CATCH, FINALLY,...) in control statements
	public boolean newlineInControlStatementMode = false;

	// by default, preserve one blank line per sequence of blank lines
	public boolean clearAllBlankLinesMode = false;

	// line splitting will occur when line exceeds this length
	public int maxLineLength = 80;

	public boolean compactAssignmentMode = false;
	// if isTrue, assignments look like x= 12 (not like x = 12);

	//number of consecutive spaces used to replace the tab char
	public int tabSize = 4; // n spaces for one tab
	public boolean indentWithTab = true;

	public boolean compactElseIfMode = false;
	// if true, else and if are kept on the same line.
	public boolean newLineInEmptyBlockMode = true;
	// if false, no new line in {} if it's empty.

	public char[] lineSeparatorSequence =
		System.getProperty("line.separator").toCharArray();
	/** 
	 * Initializing the formatter options with default settings
	 */
	public FormatterOptions() {
	}

	/** 
	 * Initializing the formatter options with external settings
	 */
	public FormatterOptions(ConfigurableOption[] settings) {
		if (settings == null)
			return;
		// filter options which are related to the compiler component
		String componentName = CodeFormatter.class.getName();
		for (int i = 0, max = settings.length; i < max; i++) {
			if (settings[i].getComponentName().equals(componentName)) {
				this.setOption(settings[i]);
			}
		}
	}

	/**
	 * Returns all the options of the Code Formatter to be shown by the UI
	 *
	 * @param locale java.util.Locale
	 * @return com.ibm.compiler.java.ConfigurableOption[]
	 */
	public ConfigurableOption[] getConfigurableOptions(Locale locale) {
		String componentName = CodeFormatter.class.getName();
		return new ConfigurableOption[] {
			new ConfigurableOption(
				componentName,
				"newline.openingBrace",
				locale,
				newLineBeforeOpeningBraceMode ? 0 : 1),
			new ConfigurableOption(
				componentName,
				"newline.controlStatement",
				locale,
				newlineInControlStatementMode ? 0 : 1),
			new ConfigurableOption(
				componentName,
				"newline.clearAll",
				locale,
				clearAllBlankLinesMode ? 0 : 1),
			new ConfigurableOption(
				componentName,
				"newline.elseIf",
				locale,
				compactElseIfMode ? 0 : 1),
			new ConfigurableOption(
				componentName,
				"newline.emptyBlock",
				locale,
				newLineInEmptyBlockMode ? 0 : 1),
			new ConfigurableOption(componentName, "line.split", locale, maxLineLength),
			new ConfigurableOption(
				componentName,
				"style.compactAssignment",
				locale,
				compactAssignmentMode ? 0 : 1),
			new ConfigurableOption(
				componentName,
				"tabulation.char",
				locale,
				indentWithTab ? 0 : 1),
			new ConfigurableOption(componentName, "tabulation.size", locale, tabSize)};
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

		switch (setting.getID()) {
			case 1 : // insert blank line before opening brace
				setNewLineBeforeOpeningBraceMode(setting.getCurrentValueIndex() == 0);
				break;
			case 2 : // insert blank line behind keywords (ELSE, CATCH, FINALLY,...) in control statements
				setNewlineInControlStatementMode(setting.getCurrentValueIndex() == 0);
				break;
			case 3 : // flush all blank lines
				setClearAllBlankLinesMode(setting.getCurrentValueIndex() == 0);
				break;
			case 4 : // puts else if on the same line
				setCompactElseIfMode(setting.getCurrentValueIndex() == 0);
				break;
			case 5 : // add a new line inside an empty block.
				setNewLineInEmptyBlockMode(setting.getCurrentValueIndex() == 0);
				break;
			case 6 : // line splitting will occur when line exceeds this length (0 -> no splitting)
				setMaxLineLength(setting.getCurrentValueIndex());
				break;
			case 7 : // if isTrue, assignments look like x= 12 (not like x = 12);
				setCompactAssignmentMode(setting.getCurrentValueIndex() == 0);
				break;
			case 9 : // should use tab or spaces to indent
				setIndentationUsesTab(setting.getCurrentValueIndex() == 0);
				break;
			case 10 : // amount of spaces for a tabulation
				setTabSize(setting.getCurrentValueIndex());
				break;
		}
	}

	public void setReuseExistingLayoutMode(boolean flag) {
	}

	public void setTabSize(int size) {
		this.tabSize = size;
	}

}

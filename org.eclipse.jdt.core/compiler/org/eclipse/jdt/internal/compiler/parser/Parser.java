/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Tom Tromey - patch for readTable(String) as described in http://bugs.eclipse.org/bugs/show_bug.cgi?id=32196
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ReferenceContext;
import org.eclipse.jdt.internal.compiler.lookup.BindingIds;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;
import org.eclipse.jdt.internal.compiler.lookup.TypeIds;
import org.eclipse.jdt.internal.compiler.parser.diagnose.DiagnoseParser;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Parser implements BindingIds, ParserBasicInformation, TerminalTokens, CompilerModifiers, OperatorIds, TypeIds {
	public static char asb[] = null;
	public static char asr[] = null;
	//ast stack
	final static int AstStackIncrement = 100;
	public static char base_action[] = null;
	public static final int BracketKinds = 3;
    
	public static short check_table[] = null;
	public static final int CurlyBracket = 2;
	// TODO remove once testing is done
	private static final boolean DEBUG = false;
	private static final String EOF_TOKEN = "$eof" ; //$NON-NLS-1$
	private static final String ERROR_TOKEN = "$error" ; //$NON-NLS-1$
	//expression stack
	final static int ExpressionStackIncrement = 100;

	final static int GenericsStackIncrement = 10;
    
	private final static String FILEPREFIX = "parser"; //$NON-NLS-1$
    public static char in_symb[] = null;
	private static final String INVALID_CHARACTER = "Invalid Character" ; //$NON-NLS-1$
	public static char lhs[] =  null;

	public static String name[] = null;
	public static char nasb[] = null;
	public static char nasr[] = null;
	public static char non_terminal_index[] = null;
	private final static String READABLE_NAMES_FILE = "readableNames"; //$NON-NLS-1$
	private final static String READABLE_NAMES_FILE_NAME =
		"org.eclipse.jdt.internal.compiler.parser." + READABLE_NAMES_FILE; //$NON-NLS-1$
	public static String readableName[] = null;
	
	public static byte rhs[] = null;

	public static final int RoundBracket = 0;
    
    public static byte scope_la[] = null;
    public static char scope_lhs[] = null;
	
	public static char scope_prefix[] = null;
    public static char scope_rhs[] = null;
    public static char scope_state[] = null;

    public static char scope_state_set[] = null;
    public static char scope_suffix[] = null;
	public static final int SquareBracket = 1;
		
	//internal data for the automat 
	protected final static int StackIncrement = 255;
	
	public static char term_action[] = null;
	public static byte term_check[] = null;

	public static char terminal_index[] = null;

	private static final String UNEXPECTED_EOF = "Unexpected End Of File" ; //$NON-NLS-1$
	public static boolean VERBOSE_RECOVERY = false;



	protected int astLengthPtr;
	protected int[] astLengthStack;
	protected int astPtr;
	protected ASTNode[] astStack = new ASTNode[AstStackIncrement];
	public CompilationUnitDeclaration compilationUnit; /*the result from parse()*/
	protected RecoveredElement currentElement;
	public int currentToken;
	protected boolean diet = false; //tells the scanner to jump over some parts of the code/expressions like method bodies
	protected int dietInt = 0; // if > 0 force the none-diet-parsing mode (even if diet if requested) [field parsing with anonymous inner classes...]
	protected int endPosition; //accurate only when used ! (the start position is pushed into intStack while the end the current one)
	protected int endStatementPosition;
	protected int expressionLengthPtr;
	protected int[] expressionLengthStack;
	protected int expressionPtr;
	protected Expression[] expressionStack = new Expression[ExpressionStackIncrement];
	public int firstToken ; // handle for multiple parsing goals

	// generics management
	protected int genericsIdentifiersLengthPtr;
	protected int[] genericsIdentifiersLengthStack = new int[GenericsStackIncrement];
	protected int genericsLengthPtr;
	protected int[] genericsLengthStack = new int[GenericsStackIncrement];
	protected int genericsPtr;
	protected ASTNode[] genericsStack = new ASTNode[GenericsStackIncrement];
	
	protected boolean hasError;
	protected boolean hasReportedError;

	//identifiers stacks 
	protected int identifierLengthPtr;
	protected int[] identifierLengthStack;
	protected long[] identifierPositionStack;
	protected int identifierPtr;
	protected char[][] identifierStack;
	
	protected boolean ignoreNextOpeningBrace;
	//positions , dimensions , .... (int stacks)
	protected int intPtr;
	protected int[] intStack;
	public int lastAct ; //handle for multiple parsing goals

	//error recovery management
	protected int lastCheckPoint;
	protected int lastErrorEndPosition;
	protected int lastIgnoredToken, nextIgnoredToken;
	protected int listLength; // for recovering some incomplete list (interfaces, throws or parameters)
	protected int lParenPos,rParenPos; //accurate only when used !
	protected int modifiers;
	protected int modifiersSourceStart;
	protected int[] nestedMethod; //the ptr is nestedType
	protected int nestedType, dimensions;
	ASTNode [] noAstNodes = new ASTNode[AstStackIncrement];
	Expression [] noExpressions = new Expression[ExpressionStackIncrement];
	//modifiers dimensions nestedType etc.......
	protected boolean optimizeStringLiterals =true;
	protected CompilerOptions options;
	protected ProblemReporter problemReporter;
	protected int rBraceStart, rBraceEnd, rBraceSuccessorStart; //accurate only when used !
	protected int realBlockPtr;
	protected int[] realBlockStack;
	protected int recoveredStaticInitializerStart;
	public ReferenceContext referenceContext;
	public boolean reportOnlyOneSyntaxError = false;
	public boolean reportSyntaxErrorIsRequired = true;
	protected boolean restartRecovery;
	//scanner token 
	public Scanner scanner;
	protected int[] stack = new int[StackIncrement];
	protected int stateStackTop;
	private int synchronizedBlockSourceStart;
	protected int[] variablesCounter;

	public Javadoc javadoc;	// javadoc
	public JavadocParser javadocParser;	static {
		try{
			initTables();
		} catch(java.io.IOException ex){
			throw new ExceptionInInitializerError(ex.getMessage());
		}
	}
public static int asi(int state) {

	return asb[original_state(state)]; 
}
public final static short base_check(int i) {
	return check_table[i - (NUM_RULES + 1)];
}
private final static void buildFile(String filename, List listToDump) throws java.io.IOException {
	BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
	for (Iterator iterator = listToDump.iterator(); iterator.hasNext(); ) {
		writer.write(String.valueOf(iterator.next()));
	}
	writer.flush();
	writer.close();
	System.out.println(filename + " creation complete"); //$NON-NLS-1$
}
private final static String[] buildFileForName(String filename, String contents) throws java.io.IOException {
	String[] result = new String[contents.length()];
	result[0] = null;
	int resultCount = 1;
	
	StringBuffer buffer = new StringBuffer();
	
	int start = contents.indexOf("name[]"); //$NON-NLS-1$
	start = contents.indexOf('\"', start); 
	int end = contents.indexOf("};", start); //$NON-NLS-1$
	
	contents = contents.substring(start, end);
	
	boolean addLineSeparator = false;
	int tokenStart = -1;
	StringBuffer currentToken = new StringBuffer();
	for (int i = 0; i < contents.length(); i++) {
		char c = contents.charAt(i);
		if(c == '\"') {
			if(tokenStart == -1) {
				tokenStart = i + 1;	
			} else {
				if(addLineSeparator) {
					buffer.append('\n');
					result[resultCount++] = currentToken.toString();
					currentToken = new StringBuffer();
				}
				String token = contents.substring(tokenStart, i);
				if(token.equals(ERROR_TOKEN)){
					token = INVALID_CHARACTER;
				} else if(token.equals(EOF_TOKEN)) {
					token = UNEXPECTED_EOF;
				}
				buffer.append(token);
				currentToken.append(token);
				addLineSeparator = true;
				tokenStart = -1;
			}
		}
		if(tokenStart == -1 && c == '+'){
			addLineSeparator = false;
		}
	}
	if(currentToken.length() > 0) {
		result[resultCount++] = currentToken.toString();
	}
	
	buildFileForTable(filename, buffer.toString().toCharArray());
	
	System.arraycopy(result, 0, result = new String[resultCount], 0, resultCount);
	return result;
}
private static void buildFileForReadableName(
	String file,
	char[] newLhs,
	char[] newNonTerminalIndex,
	String[] newName,
	String[] tokens) throws java.io.IOException {

	ArrayList entries = new ArrayList();
	
	boolean[] alreadyAdded = new boolean[newName.length];
	
	for (int i = 0; i < tokens.length; i = i + 2) {
		int index = newNonTerminalIndex[newLhs[Integer.parseInt(tokens[i])]];
		StringBuffer buffer = new StringBuffer();
		if(!alreadyAdded[index]) {
			alreadyAdded[index] = true;
			buffer.append(newName[index]);
			buffer.append('=');
			buffer.append(tokens[i+1].trim());
			buffer.append('\n');
			entries.add(String.valueOf(buffer));
		}
	}
	int i = 1;
	while(!INVALID_CHARACTER.equals(newName[i])) i++;
	i++;
	for (; i < alreadyAdded.length; i++) {
		if(!alreadyAdded[i]) {
			System.out.println(newName[i] + " has no readable name"); //$NON-NLS-1$
		}
	}
	Collections.sort(entries);
	buildFile(file, entries);
}
private final static void buildFileForTable(String filename, byte[] bytes) throws java.io.IOException {
	java.io.FileOutputStream stream = new java.io.FileOutputStream(filename);
	stream.write(bytes);
	stream.close();
	System.out.println(filename + " creation complete"); //$NON-NLS-1$
}
private final static void buildFileForTable(String filename, char[] chars) throws java.io.IOException {

	byte[] bytes = new byte[chars.length * 2];
	for (int i = 0; i < chars.length; i++) {
		bytes[2 * i] = (byte) (chars[i] >>> 8);
		bytes[2 * i + 1] = (byte) (chars[i] & 0xFF);
	}

	java.io.FileOutputStream stream = new java.io.FileOutputStream(filename);
	stream.write(bytes);
	stream.close();
	System.out.println(filename + " creation complete"); //$NON-NLS-1$
}
private final static void buildFileOfByteFor(String filename, String tag, String[] tokens) throws java.io.IOException {

	//transform the String tokens into chars before dumping then into file

	int i = 0;
	//read upto the tag
	while (!tokens[i++].equals(tag));
	//read upto the }
	
	byte[] bytes = new byte[tokens.length]; //can't be bigger
	int ic = 0;
	String token;
	while (!(token = tokens[i++]).equals("}")) { //$NON-NLS-1$
		int c = Integer.parseInt(token);
		bytes[ic++] = (byte) c;
	}

	//resize
	System.arraycopy(bytes, 0, bytes = new byte[ic], 0, ic);

	buildFileForTable(filename, bytes);
}
private final static char[] buildFileOfIntFor(String filename, String tag, String[] tokens) throws java.io.IOException {

	//transform the String tokens into chars before dumping then into file

	int i = 0;
	//read upto the tag
	while (!tokens[i++].equals(tag));
	//read upto the }
	
	char[] chars = new char[tokens.length]; //can't be bigger
	int ic = 0;
	String token;
	while (!(token = tokens[i++]).equals("}")) { //$NON-NLS-1$
		int c = Integer.parseInt(token);
		chars[ic++] = (char) c;
	}

	//resize
	System.arraycopy(chars, 0, chars = new char[ic], 0, ic);

	buildFileForTable(filename, chars);
	return chars;
}
private final static void buildFileOfShortFor(String filename, String tag, String[] tokens) throws java.io.IOException {

	//transform the String tokens into chars before dumping then into file

	int i = 0;
	//read upto the tag
	while (!tokens[i++].equals(tag));
	//read upto the }
	
	char[] chars = new char[tokens.length]; //can't be bigger
	int ic = 0;
	String token;
	while (!(token = tokens[i++]).equals("}")) { //$NON-NLS-1$
		int c = Integer.parseInt(token);
		chars[ic++] = (char) (c + 32768);
	}

	//resize
	System.arraycopy(chars, 0, chars = new char[ic], 0, ic);

	buildFileForTable(filename, chars);
}
public final static void buildFilesFromLPG(String dataFilename, String dataFilename2)	throws java.io.IOException {

	//RUN THIS METHOD TO GENERATE PARSER*.RSC FILES

	//build from the lpg javadcl.java files that represents the parser tables
	//lhs check_table asb asr symbol_index

	//[org.eclipse.jdt.internal.compiler.parser.Parser.buildFilesFromLPG("d:/leapfrog/grammar/javadcl.java")]

	char[] contents = new char[] {};
	try {
		contents = Util.getFileCharContent(new File(dataFilename), null);
	} catch (IOException ex) {
		System.out.println(Util.bind("parser.incorrectPath")); //$NON-NLS-1$
		return;
	}
	java.util.StringTokenizer st = 
		new java.util.StringTokenizer(new String(contents), " \t\n\r[]={,;");  //$NON-NLS-1$
	String[] tokens = new String[st.countTokens()];
	int i = 0;
	while (st.hasMoreTokens()) {
		tokens[i++] = st.nextToken();
	}
	final String prefix = FILEPREFIX;
	i = 0;
	
	char[] newLhs = buildFileOfIntFor(prefix + (++i) + ".rsc", "lhs", tokens); //$NON-NLS-1$ //$NON-NLS-2$
	buildFileOfShortFor(prefix + (++i) + ".rsc", "check_table", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "asb", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "asr", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "nasb", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "nasr", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "terminal_index", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	char[] newNonTerminalIndex = buildFileOfIntFor(prefix + (++i) + ".rsc", "non_terminal_index", tokens); //$NON-NLS-1$ //$NON-NLS-2$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "term_action", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_prefix", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_suffix", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_lhs", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_state_set", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_rhs", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "scope_state", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfIntFor(prefix + (++i) + ".rsc", "in_symb", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	
	buildFileOfByteFor(prefix + (++i) + ".rsc", "rhs", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfByteFor(prefix + (++i) + ".rsc", "term_check", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	buildFileOfByteFor(prefix + (++i) + ".rsc", "scope_la", tokens); //$NON-NLS-2$ //$NON-NLS-1$
	
	String[] newName = buildFileForName(prefix + (++i) + ".rsc", new String(contents)); //$NON-NLS-1$
	
	contents = new char[] {};
	try {
		contents = Util.getFileCharContent(new File(dataFilename2), null);
	} catch (IOException ex) {
		System.out.println(Util.bind("parser.incorrectPath")); //$NON-NLS-1$
		return;
	}
	st = new java.util.StringTokenizer(new String(contents), "\t\n\r=");  //$NON-NLS-1$
	tokens = new String[st.countTokens()];
	i = 0;
	while (st.hasMoreTokens()) {
		tokens[i++] = st.nextToken();
	}
	buildFileForReadableName(READABLE_NAMES_FILE+".properties", newLhs, newNonTerminalIndex, newName, tokens);//$NON-NLS-1$
	
	System.out.println(Util.bind("parser.moveFiles")); //$NON-NLS-1$
}
public static int in_symbol(int state) {
	return in_symb[original_state(state)];
}
public final static void initTables() throws java.io.IOException {

	final String prefix = FILEPREFIX;
	int i = 0;
	lhs = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	char[] chars = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	check_table = new short[chars.length];
	for (int c = chars.length; c-- > 0;) {
		check_table[c] = (short) (chars[c] - 32768);
	}
	asb = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	asr = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	nasb = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	nasr = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	terminal_index = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	non_terminal_index = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	term_action = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	
	scope_prefix = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_suffix = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_lhs = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_state_set = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_rhs = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_state = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	in_symb = readTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	
	rhs = readByteTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	term_check = readByteTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	scope_la = readByteTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	
	name = readNameTable(prefix + (++i) + ".rsc"); //$NON-NLS-1$
	readableName = readReadableNameTable(READABLE_NAMES_FILE_NAME);
	
	base_action = lhs;
}
public static int nasi(int state) {
	return nasb[original_state(state)];
}
public static int ntAction(int state, int sym) {
	return base_action[state + sym];
}
protected static int original_state(int state) {
	return -base_check(state);
}
protected static byte[] readByteTable(String filename) throws java.io.IOException {

	//files are located at Parser.class directory

	InputStream stream = Parser.class.getResourceAsStream(filename);
	if (stream == null) {
		throw new java.io.IOException(Util.bind("parser.missingFile",filename)); //$NON-NLS-1$
	}
	byte[] bytes = null;
	try {
		stream = new BufferedInputStream(stream);
		bytes = Util.getInputStreamAsByteArray(stream, -1);
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
			// ignore
		}
	}
	return bytes;
}
	
protected static String[] readNameTable(String filename) throws java.io.IOException {
	char[] contents = readTable(filename);
	char[][] nameAsChar = CharOperation.splitOn('\n', contents);

	String[] result = new String[nameAsChar.length + 1];
	result[0] = null;
	for (int i = 0; i < nameAsChar.length; i++) {
		result[i + 1] = new String(nameAsChar[i]);
	}
	
	return result;
}
protected static String[] readReadableNameTable(String filename) {
	String[] result = new String[name.length];

	ResourceBundle bundle;
	try {
		bundle = ResourceBundle.getBundle(filename, Locale.getDefault());
	} catch(MissingResourceException e) {
		System.out.println("Missing resource : " + filename.replace('.', '/') + ".properties for locale " + Locale.getDefault()); //$NON-NLS-1$//$NON-NLS-2$
		throw e;
	}
	for (int i = 0; i < NT_OFFSET + 1; i++) {
		result[i] = name[i];
	}
	for (int i = NT_OFFSET; i < name.length; i++) {
		try {
			String n = bundle.getString(name[i]);
			if(n != null && n.length() > 0) {
				result[i] = n;
			} else {
				result[i] = name[i];
			}
		} catch(MissingResourceException e) {
			result[i] = name[i];
		}
	}
	return result;
}
protected static char[] readTable(String filename) throws java.io.IOException {

	//files are located at Parser.class directory

	InputStream stream = Parser.class.getResourceAsStream(filename);
	if (stream == null) {
		throw new java.io.IOException(Util.bind("parser.missingFile",filename)); //$NON-NLS-1$
	}
	byte[] bytes = null;
	try {
		stream = new BufferedInputStream(stream);
		bytes = Util.getInputStreamAsByteArray(stream, -1);
	} finally {
		try {
			stream.close();
		} catch (IOException e) {
			// ignore
		}
	}

	//minimal integrity check (even size expected)
	int length = bytes.length;
	if (length % 2 != 0)
		throw new java.io.IOException(Util.bind("parser.corruptedFile",filename)); //$NON-NLS-1$

	// convert bytes into chars
	char[] chars = new char[length / 2];
	int i = 0;
	int charIndex = 0;

	while (true) {
		chars[charIndex++] = (char) (((bytes[i++] & 0xFF) << 8) + (bytes[i++] & 0xFF));
		if (i == length)
			break;
	}
	return chars;
}
public static int tAction(int state, int sym) {
	return term_action[term_check[base_action[state]+sym] == sym ? base_action[state] + sym : base_action[state]];
}

public Parser(ProblemReporter problemReporter, boolean optimizeStringLiterals) {
		
	this.problemReporter = problemReporter;
	this.options = problemReporter.options;
	this.optimizeStringLiterals = optimizeStringLiterals;
	this.initializeScanner();
	astLengthStack = new int[50];
	expressionLengthStack = new int[30];
	intStack = new int[50];
	identifierStack = new char[30][];
	identifierLengthStack = new int[30];
	nestedMethod = new int[30];
	realBlockStack = new int[30];
	identifierPositionStack = new long[30];
	variablesCounter = new int[30];
	
	// javadoc support
	this.javadocParser = new JavadocParser(this);	
}
/**
 *
 * INTERNAL USE-ONLY
 */
protected void adjustInterfaceModifiers() {
	intStack[intPtr - 1] |= AccInterface;
}
public final void arrayInitializer(int length) {
	//length is the size of the array Initializer
	//expressionPtr points on the last elt of the arrayInitializer, 
	// in other words, it has not been decremented yet.

	ArrayInitializer ai = new ArrayInitializer();
	if (length != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, ai.expressions = new Expression[length], 0, length);
	}
	pushOnExpressionStack(ai);
	//positionning
	ai.sourceEnd = endStatementPosition;
	int searchPosition = length == 0 ? endPosition + 1 : ai.expressions[0].sourceStart;
	try {
		//does not work with comments(that contain '{') nor '{' describes as a unicode....		
		while (scanner.source[--searchPosition] != '{');
	} catch (IndexOutOfBoundsException ex) {
		//should never occur (except for strange cases like whose describe above)
		searchPosition = (length == 0 ? endPosition : ai.expressions[0].sourceStart) - 1;
	}
	ai.sourceStart = searchPosition;
}
protected void blockReal() {
	// See consumeLocalVariableDeclarationStatement in case of change: duplicated code
	// increment the amount of declared variables for this block
	realBlockStack[realBlockPtr]++;
}
/*
 * Build initial recovery state.
 * Recovery state is inferred from the current state of the parser (reduced node stack).
 */
public RecoveredElement buildInitialRecoveryState(){

	/* initialize recovery by retrieving available reduced nodes 
	 * also rebuild bracket balance 
	 */
	lastCheckPoint = 0;

	RecoveredElement element = null;
	if (referenceContext instanceof CompilationUnitDeclaration){
		element = new RecoveredUnit(compilationUnit, 0, this);
		
		/* ignore current stack state, since restarting from the beginnning 
		   since could not trust simple brace count */
		if (true){ // experimenting restart recovery from scratch
			compilationUnit.currentPackage = null;
			compilationUnit.imports = null;
			compilationUnit.types = null;
			currentToken = 0;
			listLength = 0;
			endPosition = 0;
			endStatementPosition = 0;
			return element;
		}
		if (compilationUnit.currentPackage != null){
			lastCheckPoint = compilationUnit.currentPackage.declarationSourceEnd+1;
		}
		if (compilationUnit.imports != null){
			lastCheckPoint = compilationUnit.imports[compilationUnit.imports.length -1].declarationSourceEnd+1;		
		}
	} else {
		if (referenceContext instanceof AbstractMethodDeclaration){
			element = new RecoveredMethod((AbstractMethodDeclaration) referenceContext, null, 0, this);
			lastCheckPoint = ((AbstractMethodDeclaration) referenceContext).bodyStart;
		} else {
			/* Initializer bodies are parsed in the context of the type declaration, we must thus search it inside */
			if (referenceContext instanceof TypeDeclaration){
				TypeDeclaration type = (TypeDeclaration) referenceContext;
				for (int i = 0; i < type.fields.length; i++){
					FieldDeclaration field = type.fields[i];					
					if (field != null
						&& !field.isField()
						&& field.declarationSourceStart <= scanner.initialPosition
						&& scanner.initialPosition <= field.declarationSourceEnd
						&& scanner.eofPosition <= field.declarationSourceEnd+1){
						element = new RecoveredInitializer(field, null, 1, this);
						lastCheckPoint = field.declarationSourceStart;					
						break;
					}
				}
			} 
		}
	}

	if (element == null) return element;
	
	for(int i = 0; i <= astPtr; i++){
		ASTNode node = astStack[i];
		if (node instanceof AbstractMethodDeclaration){
			AbstractMethodDeclaration method = (AbstractMethodDeclaration) node;
			if (method.declarationSourceEnd == 0){
				element = element.add(method, 0);
				lastCheckPoint = method.bodyStart;
			} else {
				element = element.add(method, 0);
				lastCheckPoint = method.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof Initializer){
			Initializer initializer = (Initializer) node;
			if (initializer.declarationSourceEnd == 0){
				element = element.add(initializer, 1);
				lastCheckPoint = initializer.sourceStart;				
			} else {
				element = element.add(initializer, 0);
				lastCheckPoint = initializer.declarationSourceEnd + 1;
			}
			continue;
		}		
		if (node instanceof FieldDeclaration){
			FieldDeclaration field = (FieldDeclaration) node;
			if (field.declarationSourceEnd == 0){
				element = element.add(field, 0);
				if (field.initialization == null){
					lastCheckPoint = field.sourceEnd + 1;
				} else {
					lastCheckPoint = field.initialization.sourceEnd + 1;
				}
			} else {
				element = element.add(field, 0);
				lastCheckPoint = field.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof TypeDeclaration){
			TypeDeclaration type = (TypeDeclaration) node;
			if (type.declarationSourceEnd == 0){
				element = element.add(type, 0);	
				lastCheckPoint = type.bodyStart;
			} else {
				element = element.add(type, 0);				
				lastCheckPoint = type.declarationSourceEnd + 1;
			}
			continue;
		}
		if (node instanceof ImportReference){
			ImportReference importRef = (ImportReference) node;
			element = element.add(importRef, 0);
			lastCheckPoint = importRef.declarationSourceEnd + 1;
		}
	}
	return element;
}

public final void checkAndSetModifiers(int flag){
	/*modify the current modifiers buffer.
	When the startPosition of the modifiers is 0
	it means that the modifier being parsed is the first
	of a list of several modifiers. The startPosition
	is zeroed when a copy of modifiers-buffer is push
	onto the astStack. */

	if ((modifiers & flag) != 0){ // duplicate modifier
		modifiers |= AccAlternateModifierProblem;
	}
	modifiers |= flag;
			
	if (modifiersSourceStart < 0) modifiersSourceStart = scanner.startPosition;
}
public void checkComment() {

	if (this.currentElement != null && this.scanner.commentPtr >= 0) {
		flushCommentsDefinedPriorTo(endStatementPosition); // discard obsolete comments during recovery
	}
	
	int lastComment = this.scanner.commentPtr;
	
	if (this.modifiersSourceStart >= 0) {
		// eliminate comments located after modifierSourceStart if positionned
		while (lastComment >= 0 && this.scanner.commentStarts[lastComment] > modifiersSourceStart) lastComment--;
	}
	if (lastComment >= 0) {
		// consider all remaining leading comments to be part of current declaration
		this.modifiersSourceStart = scanner.commentStarts[0]; 
	
		// check deprecation in last comment if javadoc (can be followed by non-javadoc comments which are simply ignored)	
		while (lastComment >= 0 && this.scanner.commentStops[lastComment] < 0) lastComment--; // non javadoc comment have negative end positions
		if (lastComment >= 0) {
			if (this.javadocParser.checkDeprecation(
					this.scanner.commentStarts[lastComment],
					this.scanner.commentStops[lastComment] - 1)) { //stop is one over,
				checkAndSetModifiers(AccDeprecated);
			}
			this.javadoc = this.javadocParser.javadoc;	// null if check javadoc is not activated 
		}
	}
}
protected void checkNonExternalizedStringLiteral() {
	if (scanner.wasNonExternalizedStringLiteral) {
		StringLiteral[] literals = this.scanner.nonNLSStrings;
		// could not reproduce, but this is the only NPE
		// added preventive null check see PR 9035
		if (literals != null) {
			for (int i = 0, max = literals.length; i < max; i++) {
				problemReporter().nonExternalizedStringLiteral(literals[i]);
			}
		}
		scanner.wasNonExternalizedStringLiteral = false;
	}
}
protected void checkNonNLSAfterBodyEnd(int declarationEnd){
	if(scanner.currentPosition - 1 <= declarationEnd) {
		scanner.eofPosition = declarationEnd < Integer.MAX_VALUE ? declarationEnd + 1 : declarationEnd;
		try {
			while(scanner.getNextToken() != TokenNameEOF);
			checkNonExternalizedStringLiteral();
		} catch (InvalidInputException e) {
			// Nothing to do
		}
	}
}
protected void classInstanceCreation(boolean alwaysQualified) {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	AllocationExpression alloc;
	int length;
	if (((length = astLengthStack[astLengthPtr--]) == 1)
		&& (astStack[astPtr] == null)) {
		//NO ClassBody
		astPtr--;
		if (alwaysQualified) {
			alloc = new QualifiedAllocationExpression();
		} else {
			alloc = new AllocationExpression();
		}
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		if (alwaysQualified) {
			pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
			pushOnGenericsLengthStack(0);
		}
		alloc.type = getTypeReference(0);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);
	} else {
		dispatchDeclarationInto(length);
		TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)astStack[astPtr];
		anonymousTypeDeclaration.declarationSourceEnd = endStatementPosition;
		anonymousTypeDeclaration.bodyEnd = endStatementPosition;
		if (anonymousTypeDeclaration.allocation != null) {
			anonymousTypeDeclaration.allocation.sourceEnd = endStatementPosition;
		}
		if (length == 0 && !containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
			anonymousTypeDeclaration.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
		astPtr--;
		astLengthPtr--;

		// mark initializers with local type mark if needed
		markInitializersWithLocalType(anonymousTypeDeclaration);
	}
}
protected final void concatExpressionLists() {
	expressionLengthStack[--expressionLengthPtr]++;
}
private final void concatGenericsLists() {
	genericsLengthStack[genericsLengthPtr - 1] += genericsLengthStack[genericsLengthPtr--];
}
private final void concatNodeLists() {
	/*
	 * This is a case where you have two sublists into the astStack that you want
	 * to merge in one list. There is no action required on the astStack. The only
	 * thing you need to do is merge the two lengths specified on the astStackLength.
	 * The top two length are for example:
	 * ... p   n
	 * and you want to result in a list like:
	 * ... n+p 
	 * This means that the p could be equals to 0 in case there is no astNode pushed
	 * on the astStack.
	 * Look at the InterfaceMemberDeclarations for an example.
	 */

	astLengthStack[astLengthPtr - 1] += astLengthStack[astLengthPtr--];
}
protected void consumeAdditionalBound() {
	pushOnAstStack(getTypeReference(intStack[intPtr--]));
}
protected void consumeAdditionalBound1() {
}
protected void consumeAdditionalBoundList() {
	concatGenericsLists();
}
protected void consumeAdditionalBoundList1() {
	concatGenericsLists();
}
protected void consumeAllocationHeader() {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt

	// ClassBodyopt produces a null item on the astStak if it produces NO class body
	// An empty class body produces a 0 on the length stack.....

	if (currentElement == null){
		return; // should never occur, this consumeRule is only used in recovery mode
	}
	if (currentToken == TokenNameLBRACE){
		// beginning of an anonymous type
		TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult);
		anonymousType.name = TypeDeclaration.ANONYMOUS_EMPTY_NAME;
		anonymousType.bits |= ASTNode.AnonymousAndLocalMask;
		anonymousType.sourceStart = intStack[intPtr--];
		anonymousType.sourceEnd = rParenPos; // closing parenthesis
		lastCheckPoint = anonymousType.bodyStart = scanner.currentPosition;
		currentElement = currentElement.add(anonymousType, 0);
		lastIgnoredToken = -1;
		currentToken = 0; // opening brace already taken into account
		return;
	}
	lastCheckPoint = scanner.startPosition; // force to restart at this exact position
	restartRecovery = true; // request to restart from here on
}
protected void consumeAnnotationTypeBody() {
	// TODO Auto-generated method stub
}
protected void consumeAnnotationTypeDeclaration() {
	// TODO Auto-generated method stub
}
protected void consumeAnnotationTypeMemberDeclaration() {
	// TODO Auto-generated method stub
}
protected void consumeAnnotationTypeMemberDeclarations() {
	// TODO Auto-generated method stub
}
protected void consumeArgumentList() {
	// ArgumentList ::= ArgumentList ',' Expression
	concatExpressionLists();
}
protected void consumeArguments() {
	// Arguments ::= '(' ArgumentListopt ')' 
	// nothing to do, the expression stack is already updated
}
protected void consumeArrayAccess(boolean unspecifiedReference) {
	// ArrayAccess ::= Name '[' Expression ']' ==> true
	// ArrayAccess ::= PrimaryNoNewArray '[' Expression ']' ==> false


	//optimize push/pop
	Expression exp;
	if (unspecifiedReference) {
		exp = 
			expressionStack[expressionPtr] = 
				new ArrayReference(
					getUnspecifiedReferenceOptimized(),
					expressionStack[expressionPtr]);
	} else {
		expressionPtr--;
		expressionLengthPtr--;
		exp = 
			expressionStack[expressionPtr] = 
				new ArrayReference(
					expressionStack[expressionPtr],
					expressionStack[expressionPtr + 1]);
	}
	exp.sourceEnd = endPosition;
}
protected void consumeArrayCreationExpressionWithInitializer() {
	// ArrayCreationWithArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs ArrayInitializer
	// ArrayCreationWithArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs ArrayInitializer

	int length;
	ArrayAllocationExpression aae = new ArrayAllocationExpression();
	expressionLengthPtr -- ;
	aae.initializer = (ArrayInitializer) expressionStack[expressionPtr--];
		
	aae.type = getTypeReference(0);
	length = (expressionLengthStack[expressionLengthPtr--]);
	expressionPtr -= length ;
	System.arraycopy(
		expressionStack,
		expressionPtr+1,
		aae.dimensions = new Expression[length],
		0,
		length);
	aae.sourceStart = intStack[intPtr--];
	if (aae.initializer == null) {
		aae.sourceEnd = endPosition;
	} else {
		aae.sourceEnd = aae.initializer.sourceEnd ;
	}
	pushOnExpressionStack(aae);
}
protected void consumeArrayCreationExpressionWithoutInitializer() {
	// ArrayCreationWithoutArrayInitializer ::= 'new' ClassOrInterfaceType DimWithOrWithOutExprs
	// ArrayCreationWithoutArrayInitializer ::= 'new' PrimitiveType DimWithOrWithOutExprs

	int length;
	ArrayAllocationExpression aae = new ArrayAllocationExpression();
	aae.type = getTypeReference(0);
	length = (expressionLengthStack[expressionLengthPtr--]);
	expressionPtr -= length ;
	System.arraycopy(
		expressionStack,
		expressionPtr+1,
		aae.dimensions = new Expression[length],
		0,
		length);
	aae.sourceStart = intStack[intPtr--];
	if (aae.initializer == null) {
		aae.sourceEnd = endPosition;
	} else {
		aae.sourceEnd = aae.initializer.sourceEnd ;
	}
	pushOnExpressionStack(aae);
}
protected void consumeArrayCreationHeader() {
	// nothing to do
}
protected void consumeArrayInitializer() {
	// ArrayInitializer ::= '{' VariableInitializers '}'
	// ArrayInitializer ::= '{' VariableInitializers , '}'

	arrayInitializer(expressionLengthStack[expressionLengthPtr--]);
}
protected void consumeArrayTypeWithTypeArgumentsName() {
	intStack[intPtr] += identifierLengthStack[identifierLengthPtr];
}
protected void consumeAssertStatement() {
	// AssertStatement ::= 'assert' Expression ':' Expression ';'
	expressionLengthPtr-=2;
	pushOnAstStack(new AssertStatement(expressionStack[expressionPtr--], expressionStack[expressionPtr--], intStack[intPtr--]));
}
protected void consumeAssignment() {
	// Assignment ::= LeftHandSide AssignmentOperator AssignmentExpression
	//optimize the push/pop

	int op = intStack[intPtr--] ; //<--the encoded operator
	
	expressionPtr -- ; expressionLengthPtr -- ;
	expressionStack[expressionPtr] =
		(op != EQUAL ) ?
			new CompoundAssignment(
				expressionStack[expressionPtr] ,
				expressionStack[expressionPtr+1], 
				op,
				scanner.startPosition - 1)	:
			new Assignment(
				expressionStack[expressionPtr] ,
				expressionStack[expressionPtr+1],
				scanner.startPosition - 1);
}
protected void consumeAssignmentOperator(int pos) {
	// AssignmentOperator ::= '='
	// AssignmentOperator ::= '*='
	// AssignmentOperator ::= '/='
	// AssignmentOperator ::= '%='
	// AssignmentOperator ::= '+='
	// AssignmentOperator ::= '-='
	// AssignmentOperator ::= '<<='
	// AssignmentOperator ::= '>>='
	// AssignmentOperator ::= '>>>='
	// AssignmentOperator ::= '&='
	// AssignmentOperator ::= '^='
	// AssignmentOperator ::= '|='

	try {
		intStack[++intPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		//intPtr is correct 
		int oldStackLength = intStack.length;
		int oldStack[] = intStack;
		intStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, intStack, 0, oldStackLength);
		intStack[intPtr] = pos;
	}
}
protected void consumeBinaryExpression(int op) {
	// MultiplicativeExpression ::= MultiplicativeExpression '*' UnaryExpression
	// MultiplicativeExpression ::= MultiplicativeExpression '/' UnaryExpression
	// MultiplicativeExpression ::= MultiplicativeExpression '%' UnaryExpression
	// AdditiveExpression ::= AdditiveExpression '+' MultiplicativeExpression
	// AdditiveExpression ::= AdditiveExpression '-' MultiplicativeExpression
	// ShiftExpression ::= ShiftExpression '<<'  AdditiveExpression
	// ShiftExpression ::= ShiftExpression '>>'  AdditiveExpression
	// ShiftExpression ::= ShiftExpression '>>>' AdditiveExpression
	// RelationalExpression ::= RelationalExpression '<'  ShiftExpression
	// RelationalExpression ::= RelationalExpression '>'  ShiftExpression
	// RelationalExpression ::= RelationalExpression '<=' ShiftExpression
	// RelationalExpression ::= RelationalExpression '>=' ShiftExpression
	// AndExpression ::= AndExpression '&' EqualityExpression
	// ExclusiveOrExpression ::= ExclusiveOrExpression '^' AndExpression
	// InclusiveOrExpression ::= InclusiveOrExpression '|' ExclusiveOrExpression
	// ConditionalAndExpression ::= ConditionalAndExpression '&&' InclusiveOrExpression
	// ConditionalOrExpression ::= ConditionalOrExpression '||' ConditionalAndExpression

	//optimize the push/pop

	expressionPtr--;
	expressionLengthPtr--;
	if (op == OR_OR) {
		expressionStack[expressionPtr] = 
			new OR_OR_Expression(
				expressionStack[expressionPtr], 
				expressionStack[expressionPtr + 1], 
				op); 
	} else {
		if (op == AND_AND) {
			expressionStack[expressionPtr] = 
				new AND_AND_Expression(
					expressionStack[expressionPtr], 
					expressionStack[expressionPtr + 1], 
					op);
		} else {
			// look for "string1" + "string2"
			if ((op == PLUS) && optimizeStringLiterals) {
				Expression expr1, expr2;
				expr1 = expressionStack[expressionPtr];
				expr2 = expressionStack[expressionPtr + 1];
				if (expr1 instanceof StringLiteral) {
					if (expr2 instanceof CharLiteral) { // string+char
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((CharLiteral) expr2); 
					} else if (expr2 instanceof StringLiteral) { //string+string
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((StringLiteral) expr2); 
					} else {
						expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
					}
				} else {
					expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
				}
			} else {
				expressionStack[expressionPtr] = 
					new BinaryExpression(
						expressionStack[expressionPtr], 
						expressionStack[expressionPtr + 1], 
						op);
			}
		}
	}
}
/**
 * @param op binary operator
 */
protected void consumeBinaryExpressionWithName(int op) {
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	expressionPtr--;
	expressionLengthPtr--;
	if (op == OR_OR) {
		expressionStack[expressionPtr] = 
			new OR_OR_Expression(
				expressionStack[expressionPtr + 1], 
				expressionStack[expressionPtr], 
				op); 
	} else {
		if (op == AND_AND) {
			expressionStack[expressionPtr] = 
				new AND_AND_Expression(
					expressionStack[expressionPtr + 1], 
					expressionStack[expressionPtr], 
					op);
		} else {
			// look for "string1" + "string2"
			if ((op == PLUS) && optimizeStringLiterals) {
				Expression expr1, expr2;
				expr1 = expressionStack[expressionPtr + 1];
				expr2 = expressionStack[expressionPtr];
				if (expr1 instanceof StringLiteral) {
					if (expr2 instanceof CharLiteral) { // string+char
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((CharLiteral) expr2); 
					} else if (expr2 instanceof StringLiteral) { //string+string
						expressionStack[expressionPtr] = 
							((StringLiteral) expr1).extendWith((StringLiteral) expr2); 
					} else {
						expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
					}
				} else {
					expressionStack[expressionPtr] = new BinaryExpression(expr1, expr2, PLUS);
				}
			} else {
				expressionStack[expressionPtr] = 
					new BinaryExpression(
						expressionStack[expressionPtr + 1], 
						expressionStack[expressionPtr], 
						op);
			}
		}
	}
}
protected void consumeBlock() {
	// Block ::= OpenBlock '{' BlockStatementsopt '}'
	// simpler action for empty blocks

	int statementsLength = astLengthStack[astLengthPtr--];
	Block block;
	if (statementsLength == 0) { // empty block 
		block = new Block(0);
		block.sourceStart = intStack[intPtr--];
		block.sourceEnd = endStatementPosition;
		// check whether this block at least contains some comment in it
		if (!containsComment(block.sourceStart, block.sourceEnd)) {
			block.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
		realBlockPtr--; // still need to pop the block variable counter
	} else {
		block = new Block(realBlockStack[realBlockPtr--]);
		astPtr -= statementsLength;
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			block.statements = new Statement[statementsLength], 
			0, 
			statementsLength); 
		block.sourceStart = intStack[intPtr--];
		block.sourceEnd = endStatementPosition;
	}
	pushOnAstStack(block);
}
protected void consumeBlockStatements() {
	// BlockStatements ::= BlockStatements BlockStatement
	concatNodeLists();
}
protected void consumeCaseLabel() {
	// SwitchLabel ::= 'case' ConstantExpression ':'
	expressionLengthPtr--;
	Expression expression = expressionStack[expressionPtr--];
	pushOnAstStack(new CaseStatement(expression, expression.sourceEnd, intStack[intPtr--]));
}
protected void consumeCastExpressionLL1() {
	//CastExpression ::= '(' Expression ')' InsideCastExpressionLL1 UnaryExpressionNotPlusMinus
	// Expression is used in order to make the grammar LL1

	//optimize push/pop

	Expression cast,exp;
	expressionPtr--;
	expressionStack[expressionPtr] = 
		cast = new CastExpression(
			exp=expressionStack[expressionPtr+1] ,
			getTypeReference(expressionStack[expressionPtr]));
	expressionLengthPtr -- ;
	updateSourcePosition(cast);
	cast.sourceEnd=exp.sourceEnd;
}
protected void consumeCastExpressionWithGenerics() {
	// CastExpression ::= PushLPAREN Name TypeArguments PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus

	Expression exp, cast, castType;
	int end = intStack[intPtr--];
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = getTypeReference(0));
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionWithGenericsArray() {
	// CastExpression ::= PushLPAREN Name TypeArguments Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus

	Expression exp, cast, castType;
	int end = intStack[intPtr--];

	int dim = intStack[intPtr--];
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = getTypeReference(dim));
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionWithNameArray() {
	// CastExpression ::= PushLPAREN Name Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus

	Expression exp, cast, castType;
	int end = intStack[intPtr--];
	
	// handle type arguments
	pushOnGenericsLengthStack(0);
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = getTypeReference(intStack[intPtr--]));
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionWithPrimitiveType() {
	// CastExpression ::= PushLPAREN PrimitiveType Dimsopt PushRPAREN InsideCastExpression UnaryExpression

	//intStack : posOfLeftParen dim posOfRightParen

	//optimize the push/pop

	Expression exp, cast, castType;
	int end = intStack[intPtr--];
	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = getTypeReference(intStack[intPtr--]));
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionWithQualifiedGenerics() {
	// CastExpression ::= PushLPAREN Name OnlyTypeArguments '.' ClassOrInterfaceType PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus

	Expression exp, cast, castType;
	int end = intStack[intPtr--];

	TypeReference rightSide = getTypeReference(0);
	int nameSize = identifierLengthStack[identifierLengthPtr];
	int tokensSize = nameSize;
	if (rightSide instanceof SingleParameterizedTypeReference) {
		tokensSize ++;
	} else if (rightSide instanceof SingleTypeReference) {
		tokensSize ++;
	} else if (rightSide instanceof QualifiedParameterizedTypeReference) {
		tokensSize += ((QualifiedTypeReference) rightSide).tokens.length;
	} else if (rightSide instanceof QualifiedTypeReference) {
		tokensSize += ((QualifiedTypeReference) rightSide).tokens.length;
	}
	TypeReference[][] typeArguments = new TypeReference[tokensSize][];
	char[][] tokens = new char[tokensSize][];
	long[] positions = new long[tokensSize];
	if (rightSide instanceof SingleParameterizedTypeReference) {
		SingleParameterizedTypeReference singleParameterizedTypeReference = (SingleParameterizedTypeReference) rightSide;
		tokens[nameSize] = singleParameterizedTypeReference.token;
		positions[nameSize] = (((long) singleParameterizedTypeReference.sourceStart) << 32) + singleParameterizedTypeReference.sourceEnd;
		typeArguments[nameSize] = singleParameterizedTypeReference.typeArguments;
	} else if (rightSide instanceof SingleTypeReference) {
		SingleTypeReference singleTypeReference = (SingleTypeReference) rightSide;
		tokens[nameSize] = singleTypeReference.token;
		positions[nameSize] = (((long) singleTypeReference.sourceStart) << 32) + singleTypeReference.sourceEnd;
	} else if (rightSide instanceof QualifiedParameterizedTypeReference) {
		QualifiedParameterizedTypeReference parameterizedTypeReference = (QualifiedParameterizedTypeReference) rightSide;
		TypeReference[][] rightSideTypeArguments = parameterizedTypeReference.typeArguments;
		System.arraycopy(rightSideTypeArguments, 0, typeArguments, nameSize, rightSideTypeArguments.length);
		char[][] rightSideTokens = parameterizedTypeReference.tokens;
		System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
		long[] rightSidePositions = parameterizedTypeReference.sourcePositions;
		System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
	} else if (rightSide instanceof QualifiedTypeReference) {
		QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) rightSide;
		char[][] rightSideTokens = qualifiedTypeReference.tokens;
		System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
		long[] rightSidePositions = qualifiedTypeReference.sourcePositions;
		System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
	}

	int currentTypeArgumentsLength = genericsLengthStack[genericsLengthPtr--];
	TypeReference[] currentTypeArguments = new TypeReference[currentTypeArgumentsLength];
	genericsPtr -= currentTypeArgumentsLength;
	System.arraycopy(genericsStack, genericsPtr + 1, currentTypeArguments, 0, currentTypeArgumentsLength);
	
	if (nameSize == 1) {
		tokens[0] = identifierStack[identifierPtr];
		positions[0] = identifierPositionStack[identifierPtr--];
		typeArguments[0] = currentTypeArguments;
	} else {
		identifierPtr -= nameSize;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, nameSize);
		System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, nameSize);
		typeArguments[nameSize - 1] = currentTypeArguments;
	}
	
	QualifiedParameterizedTypeReference qualifiedParameterizedTypeReference = new QualifiedParameterizedTypeReference(tokens, typeArguments, 0, positions);

	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = qualifiedParameterizedTypeReference);
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCastExpressionWithQualifiedGenericsArray() {
	// CastExpression ::= PushLPAREN Name OnlyTypeArguments '.' ClassOrInterfaceType Dims PushRPAREN InsideCastExpression UnaryExpressionNotPlusMinus
	Expression exp, cast, castType;
	int end = intStack[intPtr--];

	int dim = intStack[intPtr--];
	TypeReference rightSide = getTypeReference(0);
	int nameSize = identifierLengthStack[identifierLengthPtr];
	int tokensSize = nameSize;
	if (rightSide instanceof SingleParameterizedTypeReference) {
		tokensSize ++;
	} else if (rightSide instanceof SingleTypeReference) {
		tokensSize ++;
	} else if (rightSide instanceof QualifiedParameterizedTypeReference) {
		tokensSize += ((QualifiedTypeReference) rightSide).tokens.length;
	} else if (rightSide instanceof QualifiedTypeReference) {
		tokensSize += ((QualifiedTypeReference) rightSide).tokens.length;
	}
	TypeReference[][] typeArguments = new TypeReference[tokensSize][];
	char[][] tokens = new char[tokensSize][];
	long[] positions = new long[tokensSize];
	if (rightSide instanceof SingleParameterizedTypeReference) {
		SingleParameterizedTypeReference singleParameterizedTypeReference = (SingleParameterizedTypeReference) rightSide;
		tokens[nameSize] = singleParameterizedTypeReference.token;
		positions[nameSize] = (((long) singleParameterizedTypeReference.sourceStart) << 32) + singleParameterizedTypeReference.sourceEnd;
		typeArguments[nameSize] = singleParameterizedTypeReference.typeArguments;
	} else if (rightSide instanceof SingleTypeReference) {
		SingleTypeReference singleTypeReference = (SingleTypeReference) rightSide;
		tokens[nameSize] = singleTypeReference.token;
		positions[nameSize] = (((long) singleTypeReference.sourceStart) << 32) + singleTypeReference.sourceEnd;
	} else if (rightSide instanceof QualifiedParameterizedTypeReference) {
		QualifiedParameterizedTypeReference parameterizedTypeReference = (QualifiedParameterizedTypeReference) rightSide;
		TypeReference[][] rightSideTypeArguments = parameterizedTypeReference.typeArguments;
		System.arraycopy(rightSideTypeArguments, 0, typeArguments, nameSize, rightSideTypeArguments.length);
		char[][] rightSideTokens = parameterizedTypeReference.tokens;
		System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
		long[] rightSidePositions = parameterizedTypeReference.sourcePositions;
		System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
	} else if (rightSide instanceof QualifiedTypeReference) {
		QualifiedTypeReference qualifiedTypeReference = (QualifiedTypeReference) rightSide;
		char[][] rightSideTokens = qualifiedTypeReference.tokens;
		System.arraycopy(rightSideTokens, 0, tokens, nameSize, rightSideTokens.length);
		long[] rightSidePositions = qualifiedTypeReference.sourcePositions;
		System.arraycopy(rightSidePositions, 0, positions, nameSize, rightSidePositions.length);
	}

	int currentTypeArgumentsLength = genericsLengthStack[genericsLengthPtr--];
	TypeReference[] currentTypeArguments = new TypeReference[currentTypeArgumentsLength];
	genericsPtr -= currentTypeArgumentsLength;
	System.arraycopy(genericsStack, genericsPtr + 1, currentTypeArguments, 0, currentTypeArgumentsLength);
	
	identifierPtr -= nameSize;
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, nameSize);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, nameSize);
	typeArguments[nameSize - 1] = currentTypeArguments;
	
	QualifiedParameterizedTypeReference qualifiedParameterizedTypeReference = new QualifiedParameterizedTypeReference(tokens, typeArguments, dim, positions);

	expressionStack[expressionPtr] = cast = new CastExpression(exp = expressionStack[expressionPtr], castType = qualifiedParameterizedTypeReference);
	castType.sourceEnd = end - 1;
	castType.sourceStart = (cast.sourceStart = intStack[intPtr--]) + 1;
	cast.sourceEnd = exp.sourceEnd;
}
protected void consumeCatches() {
	// Catches ::= Catches CatchClause
	optimizedConcatNodeLists();
}
protected void consumeCatchHeader() {
	// CatchDeclaration ::= 'catch' '(' FormalParameter ')' '{'

	if (currentElement == null){
		return; // should never occur, this consumeRule is only used in recovery mode
	}
	// current element should be a block due to the presence of the opening brace
	if (!(currentElement instanceof RecoveredBlock)){
		return;
	}
	// exception argument is already on astStack
	((RecoveredBlock)currentElement).attach(
		new RecoveredLocalVariable((Argument)astStack[astPtr--], currentElement, 0)); // insert catch variable in catch block
	lastCheckPoint = scanner.startPosition; // force to restart at this exact position
	restartRecovery = true; // request to restart from here on
	lastIgnoredToken = -1;
}
protected void consumeClassBodyDeclaration() {
	// ClassBodyDeclaration ::= Diet Block
	//push an Initializer
	//optimize the push/pop
	nestedMethod[nestedType]--;
	Block block = (Block) astStack[astPtr];
	if (diet) block.bits &= ~ASTNode.UndocumentedEmptyBlockMASK; // clear bit since was diet
	Initializer initializer = new Initializer(block, 0);
	intPtr--; // pop sourcestart left on the stack by consumeNestedMethod.
	initializer.bodyStart = intStack[intPtr--];
	realBlockPtr--; // pop the block variable counter left on the stack by consumeNestedMethod
	int javadocCommentStart = intStack[intPtr--];
	if (javadocCommentStart != -1) {
		initializer.declarationSourceStart = javadocCommentStart;
	}
	astStack[astPtr] = initializer;
	initializer.bodyEnd = endPosition;
	initializer.sourceEnd = endStatementPosition;
	initializer.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
}
protected void consumeClassBodyDeclarations() {
	// ClassBodyDeclarations ::= ClassBodyDeclarations ClassBodyDeclaration
	concatNodeLists();
}
protected void consumeClassBodyDeclarationsopt() {
	// ClassBodyDeclarationsopt ::= NestedType ClassBodyDeclarations
	nestedType-- ;
}
protected void consumeClassBodyopt() {
	// ClassBodyopt ::= $empty
	pushOnAstStack(null);
	endPosition = scanner.startPosition - 1;
}
protected void consumeClassDeclaration() {
	// ClassDeclaration ::= ClassHeader ClassBody

	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch according to the type of the declarations
		dispatchDeclarationInto(length);
	}

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];

	// mark initializers with local type mark if needed
	markInitializersWithLocalType(typeDecl);

	//convert constructor that do not have the type's name into methods
	boolean hasConstructor = typeDecl.checkConstructors(this);
	
	//add the default constructor when needed (interface don't have it)
	if (!hasConstructor && !typeDecl.isInterface()) {
		boolean insideFieldInitializer = false;
		if (diet) {
			for (int i = nestedType; i > 0; i--){
				if (variablesCounter[i] > 0) {
					insideFieldInitializer = true;
					break;
				}
			}
		}
		typeDecl.createsInternalConstructor(!diet || insideFieldInitializer, true);
	}

	//always add <clinit> (will be remove at code gen time if empty)
	if (this.scanner.containsAssertKeyword) {
		typeDecl.bits |= ASTNode.AddAssertionMASK;
	}
	typeDecl.addClinit();
	typeDecl.bodyEnd = endStatementPosition;
	if (length == 0 && !containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
		typeDecl.bits |= ASTNode.UndocumentedEmptyBlockMASK;
	}

	typeDecl.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition); 
}
protected void consumeClassHeader() {
	// ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt ClassHeaderImplementsopt

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];	
	if (currentToken == TokenNameLBRACE) { 
		typeDecl.bodyStart = scanner.currentPosition;
	}
	if (currentElement != null) {
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	// flush the comments related to the class header
	scanner.commentPtr = -1;
}
protected void consumeClassHeaderExtends() {
	// ClassHeaderExtends ::= 'extends' ClassType
	//superclass
	TypeReference superClass = getTypeReference(0);
	// There is a class declaration on the top of stack
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	typeDecl.superclass = superClass;
	typeDecl.bodyStart = typeDecl.superclass.sourceEnd + 1;
	// recovery
	if (currentElement != null){
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeClassHeaderImplements() {
	// ClassHeaderImplements ::= 'implements' InterfaceTypeList
	int length = astLengthStack[astLengthPtr--];
	//super interfaces
	astPtr -= length;
	// There is a class declaration on the top of stack
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		typeDecl.superInterfaces = new TypeReference[length], 
		0, 
		length); 
	typeDecl.bodyStart = typeDecl.superInterfaces[length-1].sourceEnd + 1;
	listLength = 0; // reset after having read super-interfaces
	// recovery
	if (currentElement != null) { // is recovering
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeClassHeaderName() {
	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberTypeMASK;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalTypeMASK;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--]; 
	intPtr--; // remove the end position of the class token

	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);

	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;
	}
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeClassHeaderNameWithTypeParameters() {

	// ClassHeaderName ::= Modifiersopt 'class' 'Identifier' TypeParameters
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberTypeMASK;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalTypeMASK;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	// consume type parameters
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);
	
	
	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--]; 
	intPtr--; // remove the end position of the class token

	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);

	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;
	}
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeClassInstanceCreationExpression() {
	// ClassInstanceCreationExpression ::= 'new' ClassType '(' ArgumentListopt ')' ClassBodyopt
	classInstanceCreation(false);
}
protected void consumeClassInstanceCreationExpressionName() {
	// ClassInstanceCreationExpressionName ::= Name '.'
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumeClassInstanceCreationExpressionQualified() {
	// ClassInstanceCreationExpression ::= Primary '.' 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' SimpleName '(' ArgumentListopt ')' ClassBodyopt
	classInstanceCreation(true);

	expressionLengthPtr--;
	QualifiedAllocationExpression qae = 
		(QualifiedAllocationExpression) expressionStack[expressionPtr--]; 
	qae.enclosingInstance = expressionStack[expressionPtr];
	expressionStack[expressionPtr] = qae;
	qae.sourceStart = qae.enclosingInstance.sourceStart;
}
protected void consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() {
	// ClassInstanceCreationExpression ::= Primary '.' 'new' TypeArguments SimpleName '(' ArgumentListopt ')' ClassBodyopt
	// ClassInstanceCreationExpression ::= ClassInstanceCreationExpressionName 'new' TypeArguments SimpleName '(' ArgumentListopt ')' ClassBodyopt

	ParameterizedQualifiedAllocationExpression alloc;
	int length;
	if (((length = astLengthStack[astLengthPtr--]) == 1) && (astStack[astPtr] == null)) {
		//NO ClassBody
		astPtr--;
		alloc = new ParameterizedQualifiedAllocationExpression();
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
		pushOnGenericsLengthStack(0);
		alloc.type = getTypeReference(0);

		length = genericsLengthStack[genericsLengthPtr--];
		genericsPtr -= length;
		System.arraycopy(genericsStack, genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);
	} else {
		dispatchDeclarationInto(length);
		TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)astStack[astPtr];
		anonymousTypeDeclaration.declarationSourceEnd = endStatementPosition;
		anonymousTypeDeclaration.bodyEnd = endStatementPosition;
		if (length == 0 && !containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
			anonymousTypeDeclaration.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
		astPtr--;
		astLengthPtr--;

		QualifiedAllocationExpression anonymousTypeDeclarationAllocationExpression = anonymousTypeDeclaration.allocation;
		if (anonymousTypeDeclarationAllocationExpression != null) {
			anonymousTypeDeclarationAllocationExpression.sourceEnd = endStatementPosition;
			// handle type arguments
			anonymousTypeDeclarationAllocationExpression = ParameterizedQualifiedAllocationExpression.copyInto(anonymousTypeDeclarationAllocationExpression);
			length = genericsLengthStack[genericsLengthPtr--];
			genericsPtr -= length;
			System.arraycopy(genericsStack, genericsPtr + 1, ((ParameterizedQualifiedAllocationExpression) anonymousTypeDeclarationAllocationExpression).typeArguments = new TypeReference[length], 0, length);
		}
		
		// mark initializers with local type mark if needed
		markInitializersWithLocalType(anonymousTypeDeclaration);
	}

	expressionLengthPtr--;
	QualifiedAllocationExpression qae = 
		(QualifiedAllocationExpression) expressionStack[expressionPtr--]; 
	qae.enclosingInstance = expressionStack[expressionPtr];
	expressionStack[expressionPtr] = qae;
	qae.sourceStart = qae.enclosingInstance.sourceStart;
}
protected void consumeClassInstanceCreationExpressionWithTypeArguments() {
	// ClassInstanceCreationExpression ::= 'new' TypeArguments ClassType '(' ArgumentListopt ')' ClassBodyopt
	ParameterizedAllocationExpression alloc;
	int length;
	if (((length = astLengthStack[astLengthPtr--]) == 1)
		&& (astStack[astPtr] == null)) {
		//NO ClassBody
		astPtr--;
		alloc = new ParameterizedAllocationExpression();
		alloc.sourceEnd = endPosition; //the position has been stored explicitly

		if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				alloc.arguments = new Expression[length], 
				0, 
				length); 
		}
		alloc.type = getTypeReference(0);

		length = genericsLengthStack[genericsLengthPtr--];
		genericsPtr -= length;
		System.arraycopy(genericsStack, genericsPtr + 1, alloc.typeArguments = new TypeReference[length], 0, length);
		
		//the default constructor with the correct number of argument
		//will be created and added by the TC (see createsInternalConstructorWithBinding)
		alloc.sourceStart = intStack[intPtr--];
		pushOnExpressionStack(alloc);
	} else {
		dispatchDeclarationInto(length);
		TypeDeclaration anonymousTypeDeclaration = (TypeDeclaration)astStack[astPtr];
		anonymousTypeDeclaration.declarationSourceEnd = endStatementPosition;
		anonymousTypeDeclaration.bodyEnd = endStatementPosition;
		if (length == 0 && !containsComment(anonymousTypeDeclaration.bodyStart, anonymousTypeDeclaration.bodyEnd)) {
			anonymousTypeDeclaration.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
		astPtr--;
		astLengthPtr--;

		QualifiedAllocationExpression anonymousTypeDeclarationAllocationExpression = anonymousTypeDeclaration.allocation;
		if (anonymousTypeDeclarationAllocationExpression != null) {
			anonymousTypeDeclarationAllocationExpression.sourceEnd = endStatementPosition;
			// handle type arguments
			anonymousTypeDeclarationAllocationExpression = ParameterizedQualifiedAllocationExpression.copyInto(anonymousTypeDeclarationAllocationExpression);
			length = genericsLengthStack[genericsLengthPtr--];
			genericsPtr -= length;
			System.arraycopy(genericsStack, genericsPtr + 1, ((ParameterizedQualifiedAllocationExpression) anonymousTypeDeclarationAllocationExpression).typeArguments = new TypeReference[length], 0, length);
		}
		
		// mark initializers with local type mark if needed
		markInitializersWithLocalType(anonymousTypeDeclaration);
	}
}
protected void consumeClassOrInterface() {
	genericsIdentifiersLengthStack[genericsIdentifiersLengthPtr] += identifierLengthStack[identifierLengthPtr];
	pushOnGenericsLengthStack(0); // handle type arguments
}
protected void consumeClassOrInterfaceName() {
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	pushOnGenericsLengthStack(0); // handle type arguments
}
protected void consumeClassTypeElt() {
	// ClassTypeElt ::= ClassType
	pushOnAstStack(getTypeReference(0));
	/* if incomplete thrown exception list, listLength counter will not have been reset,
		indicating that some items are available on the stack */
	listLength++; 	
}
protected void consumeClassTypeList() {
	// ClassTypeList ::= ClassTypeList ',' ClassTypeElt
	optimizedConcatNodeLists();
}
protected void consumeCompilationUnit() {
	// CompilationUnit ::= EnterCompilationUnit PackageDeclarationopt ImportDeclarationsopt
	// do nothing by default
}
protected void consumeConditionalExpression(int op) {
	// ConditionalExpression ::= ConditionalOrExpression '?' Expression ':' ConditionalExpression
	//optimize the push/pop

	expressionPtr -= 2;
	expressionLengthPtr -= 2;
	expressionStack[expressionPtr] =
		new ConditionalExpression(
			expressionStack[expressionPtr],
			expressionStack[expressionPtr + 1],
			expressionStack[expressionPtr + 2]);
}
/**
 * @param op
 */
protected void consumeConditionalExpressionWithName(int op) {
	// ConditionalExpression ::= Name '?' Expression ':' ConditionalExpression
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	expressionPtr -= 2;
	expressionLengthPtr -= 2;
	expressionStack[expressionPtr] =
		new ConditionalExpression(
			expressionStack[expressionPtr + 2],
			expressionStack[expressionPtr],
			expressionStack[expressionPtr + 1]);
}
protected void consumeConstructorBlockStatements() {
	// ConstructorBody ::= NestedMethod '{' ExplicitConstructorInvocation BlockStatements '}'
	concatNodeLists(); // explictly add the first statement into the list of statements 
}
protected void consumeConstructorBody() {
	// ConstructorBody ::= NestedMethod  '{' BlockStatementsopt '}'
	// ConstructorBody ::= NestedMethod  '{' ExplicitConstructorInvocation '}'
	nestedMethod[nestedType] --;
}
protected void consumeConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ConstructorBody

	/*
	astStack : MethodDeclaration statements
	identifierStack : name
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	*/

	//must provide a default constructor call when needed

	int length;

	// pop the position of the {  (body of the method) pushed in block decl
	intPtr--;
	intPtr--;

	//statements
	realBlockPtr--;
	ExplicitConstructorCall constructorCall = null;
	Statement[] statements = null;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		if (astStack[astPtr + 1] instanceof ExplicitConstructorCall) {
			//avoid a isSomeThing that would only be used here BUT what is faster between two alternatives ?
			System.arraycopy(
				astStack, 
				astPtr + 2, 
				statements = new Statement[length - 1], 
				0, 
				length - 1); 
			constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		} else { //need to add explicitly the super();
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				statements = new Statement[length], 
				0, 
				length); 
			constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	} else {
		boolean insideFieldInitializer = false;
		if (diet) {
			for (int i = nestedType; i > 0; i--){
				if (variablesCounter[i] > 0) {
					insideFieldInitializer = true;
					break;
				}
			}
		}
		
		if (!diet || insideFieldInitializer){
			// add it only in non-diet mode, if diet_bodies, then constructor call will be added elsewhere.
			constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	}

	// now we know that the top of stack is a constructorDeclaration
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];
	cd.constructorCall = constructorCall;
	cd.statements = statements;

	//highlight of the implicit call on the method name
	if (constructorCall != null && cd.constructorCall.sourceEnd == 0) {
		cd.constructorCall.sourceEnd = cd.sourceEnd;
		cd.constructorCall.sourceStart = cd.sourceStart;
	}

	if (!diet && (statements == null && constructorCall.isImplicitSuper())) {
		if (!containsComment(cd.bodyStart, endPosition)) {
			cd.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
	}

	//watch for } that could be given as a unicode ! ( u007D is '}' )
	// store the endPosition (position just before the '}') in case there is
	// a trailing comment behind the end of the method
	cd.bodyEnd = endPosition;
	cd.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition); 
}
protected void consumeConstructorHeader() {
	// ConstructorHeader ::= ConstructorHeaderName MethodHeaderParameters MethodHeaderThrowsClauseopt

	AbstractMethodDeclaration method = (AbstractMethodDeclaration)astStack[astPtr];

	if (currentToken == TokenNameLBRACE){ 
		method.bodyStart = scanner.currentPosition;
	}
	// recovery
	if (currentElement != null){
		if (currentToken == TokenNameSEMICOLON){ // for invalid constructors
			method.modifiers |= AccSemicolonBody;			
			method.declarationSourceEnd = scanner.currentPosition-1;
			method.bodyEnd = scanner.currentPosition-1;
			if (currentElement.parseTree() == method && currentElement.parent != null) {
				currentElement = currentElement.parent;
			}
		}		
		restartRecovery = true; // used to avoid branching back into the regular automaton
	}		
}
protected void consumeConstructorHeaderName() {

	/* recovering - might be an empty message send */
	if (currentElement != null){
		if (lastIgnoredToken == TokenNamenew){ // was an allocation expression
			lastCheckPoint = scanner.startPosition; // force to restart at this exact position				
			restartRecovery = true;
			return;
		}
	}
	
	// ConstructorHeaderName ::=  Modifiersopt 'Identifier' '('
	ConstructorDeclaration cd = new ConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];
	// javadoc
	cd.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(cd);
	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (currentElement != null){
		lastCheckPoint = cd.bodyStart;
		if ((currentElement instanceof RecoveredType && lastIgnoredToken != TokenNameDOT)
			|| cd.modifiers != 0){
			currentElement = currentElement.add(cd, 0);
			lastIgnoredToken = -1;
		}
	}	
}
protected void consumeConstructorHeaderNameWithTypeParameters() {

	/* recovering - might be an empty message send */
	if (currentElement != null){
		if (lastIgnoredToken == TokenNamenew){ // was an allocation expression
			lastCheckPoint = scanner.startPosition; // force to restart at this exact position				
			restartRecovery = true;
			return;
		}
	}
	
	// ConstructorHeaderName ::=  Modifiersopt TypeParameters 'Identifier' '('
	ParameterizedConstructorDeclaration cd = new ParameterizedConstructorDeclaration(this.compilationUnit.compilationResult);

	//name -- this is not really revelant but we do .....
	cd.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;

	// consume type parameters
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, cd.typeParameters = new TypeParameter[length], 0, length);
	
	//modifiers
	cd.declarationSourceStart = intStack[intPtr--];
	cd.modifiers = intStack[intPtr--];
	// javadoc
	cd.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at the selector starts
	cd.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(cd);
	cd.sourceEnd = lParenPos;
	cd.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws

	// recovery
	if (currentElement != null){
		lastCheckPoint = cd.bodyStart;
		if ((currentElement instanceof RecoveredType && lastIgnoredToken != TokenNameDOT)
			|| cd.modifiers != 0){
			currentElement = currentElement.add(cd, 0);
			lastIgnoredToken = -1;
		}
	}	
}
protected void consumeDefaultLabel() {
	// SwitchLabel ::= 'default' ':'
	pushOnAstStack(new CaseStatement(null, intStack[intPtr--], intStack[intPtr--]));
}
protected void consumeDefaultModifiers() {
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(
		modifiersSourceStart >= 0 ? modifiersSourceStart : scanner.startPosition); 
	resetModifiers();
}
protected void consumeDefaultValue() {
	// TODO Auto-generated method stub	
}
protected void consumeDiet() {
	// Diet ::= $empty
	checkComment();
	pushOnIntStack(modifiersSourceStart); // push the start position of a javadoc comment if there is one
	resetModifiers();
	jumpOverMethodBody();
}
protected void consumeDims() {
	// Dims ::= DimsLoop
	pushOnIntStack(dimensions);
	dimensions = 0;
}
protected void consumeDimWithOrWithOutExpr() {
	// DimWithOrWithOutExpr ::= '[' ']'
	pushOnExpressionStack(null);
	
	if(currentElement != null && currentToken == TokenNameLBRACE) {
		ignoreNextOpeningBrace = true;
		currentElement.bracketBalance++; 
	}
}
protected void consumeDimWithOrWithOutExprs() {
	// DimWithOrWithOutExprs ::= DimWithOrWithOutExprs DimWithOrWithOutExpr
	concatExpressionLists();
}
protected void consumeEmptyAnnotationTypeMemberDeclaration() {
	// TODO Auto-generated method stub	
}
protected void consumeEmptyAnnotationTypeMemberDeclarations() {
	// TODO Auto-generated method stub	
}
protected void consumeEmptyArgumentListopt() {
	// ArgumentListopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyArguments() {
	// Argumentsopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyArrayInitializer() {
	// ArrayInitializer ::= '{' ,opt '}'
	arrayInitializer(0);
}
protected void consumeEmptyArrayInitializeropt() {
	// ArrayInitializeropt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyBlockStatementsopt() {
	// BlockStatementsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyCatchesopt() {
	// Catchesopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyClassBodyDeclarationsopt() {
	// ClassBodyDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyClassMemberDeclaration() {
	// ClassMemberDeclaration ::= ';'
	pushOnAstLengthStack(0);
	problemReporter().superfluousSemicolon(endPosition+1, endStatementPosition);
	this.scanner.commentPtr = -1;
}
protected void consumeEmptyDefaultValue() {
	// TODO Auto-generated method stub	
}
protected void consumeEmptyDimsopt() {
	// Dimsopt ::= $empty
	pushOnIntStack(0);
}
protected void consumeEmptyEnumDeclarations() {
	// EnumBodyDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyExpression() {
	// Expressionopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyForInitopt() {
	// ForInitopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyForUpdateopt() {
	// ForUpdateopt ::= $empty
	pushOnExpressionStackLengthStack(0);
}
protected void consumeEmptyImportDeclarationsopt() {
	// ImportDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyInterfaceMemberDeclaration() {
	// InterfaceMemberDeclaration ::= ';'
	pushOnAstLengthStack(0);
}
protected void consumeEmptyInterfaceMemberDeclarationsopt() {
	// InterfaceMemberDeclarationsopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeEmptyMemberValuePairs() {
	// TODO Auto-generated method stub	
}
protected void consumeEmptyStatement() {
	// EmptyStatement ::= ';'
	if (this.scanner.source[endStatementPosition] == ';') {
		pushOnAstStack(new EmptyStatement(endStatementPosition, endStatementPosition));
	} else {
		// we have a Unicode for the ';' (/u003B)
		pushOnAstStack(new EmptyStatement(endStatementPosition - 5, endStatementPosition));
	}
}
protected void consumeEmptySwitchBlock() {
	// SwitchBlock ::= '{' '}'
	pushOnAstLengthStack(0);
}
protected void consumeEmptyTypeDeclaration() {
	// TypeDeclaration ::= ';' 
	pushOnAstLengthStack(0);
	problemReporter().superfluousSemicolon(endPosition+1, endStatementPosition);
	this.scanner.commentPtr = -1;	
}
protected void consumeEmptyTypeDeclarationsopt() {
	// TypeDeclarationsopt ::= $empty
	pushOnAstLengthStack(0); 
}
protected void consumeEmptyWildcardBounds() {
	// TODO Auto-generated method stub	
}
protected void consumeEnhancedForStatement() {
	// EnhancedForStatement ::= 'for' '(' Type PushModifiers Identifier ':' Expression ')' Statement
	// EnhancedForStatementNoShortIf ::= 'for' '(' Type PushModifiers Identifier ':' Expression ')' StatementNoShortIf

	Expression collection = null;
	TypeReference type;
	//statements
	astLengthPtr--;
	Statement statement = (Statement) astStack[astPtr--];

	//updates are on the expresion stack
	if (expressionLengthStack[expressionLengthPtr--] == 1) {
		expressionPtr--;
		collection = expressionStack[expressionPtr + 1];
	}

	char[] identifierName = identifierStack[identifierPtr];
	long namePosition = identifierPositionStack[identifierPtr];
	
	identifierPtr--;
	identifierLengthPtr--;
	// remove fake modifiers/modifiers start
	intPtr-=2;
	
	type = getTypeReference(intStack[intPtr--]); // type dimension

	LocalDeclaration localDeclaration = createLocalDeclaration(identifierName, type.sourceStart, (int) namePosition);
	localDeclaration.type = type;
	pushOnAstStack(
		new IteratorForStatement(
			localDeclaration,
			collection,
			statement, 
			intStack[intPtr--], 
			endStatementPosition)); 
}
protected void consumeEnterAnonymousClassBody() {
	// EnterAnonymousClassBody ::= $empty
	TypeReference typeReference = getTypeReference(0);

	QualifiedAllocationExpression alloc;
	TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult); 
	anonymousType.name = TypeDeclaration.ANONYMOUS_EMPTY_NAME;
	anonymousType.bits |= ASTNode.AnonymousAndLocalMask;
	alloc = anonymousType.allocation = new QualifiedAllocationExpression(anonymousType); 
	markEnclosingMemberWithLocalType();
	pushOnAstStack(anonymousType);

	alloc.sourceEnd = rParenPos; //the position has been stored explicitly
	int argumentLength;
	if ((argumentLength = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= argumentLength;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			alloc.arguments = new Expression[argumentLength], 
			0, 
			argumentLength); 
	}
	alloc.type = typeReference;

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	anonymousType.bodyStart = scanner.currentPosition;	
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = anonymousType.bodyStart;		
		currentElement = currentElement.add(anonymousType, 0);
		currentToken = 0; // opening brace already taken into account
		lastIgnoredToken = -1;
	}	
}
protected void consumeEnterAnonymousClassBodySimpleName() {
	// EnterAnonymousClassBody ::= $empty
	pushOnGenericsLengthStack(0);
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	TypeReference typeReference = getTypeReference(0);

	QualifiedAllocationExpression alloc;
	TypeDeclaration anonymousType = new TypeDeclaration(this.compilationUnit.compilationResult); 
	anonymousType.name = TypeDeclaration.ANONYMOUS_EMPTY_NAME;
	anonymousType.bits |= ASTNode.AnonymousAndLocalMask;
	alloc = anonymousType.allocation = new QualifiedAllocationExpression(anonymousType); 
	markEnclosingMemberWithLocalType();
	pushOnAstStack(anonymousType);

	alloc.sourceEnd = rParenPos; //the position has been stored explicitly
	int argumentLength;
	if ((argumentLength = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= argumentLength;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			alloc.arguments = new Expression[argumentLength], 
			0, 
			argumentLength); 
	}
	alloc.type = typeReference;

	anonymousType.sourceEnd = alloc.sourceEnd;
	//position at the type while it impacts the anonymous declaration
	anonymousType.sourceStart = anonymousType.declarationSourceStart = alloc.type.sourceStart;
	alloc.sourceStart = intStack[intPtr--];
	pushOnExpressionStack(alloc);

	anonymousType.bodyStart = scanner.currentPosition;	
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = anonymousType.bodyStart;		
		currentElement = currentElement.add(anonymousType, 0);
		currentToken = 0; // opening brace already taken into account
		lastIgnoredToken = -1;
	}	
}
protected void consumeEnterCompilationUnit() {
	// EnterCompilationUnit ::= $empty
	// do nothing by default
}
protected void consumeEnterVariable() {
	// EnterVariable ::= $empty
	// do nothing by default

	char[] identifierName = identifierStack[identifierPtr];
	long namePosition = identifierPositionStack[identifierPtr];
	int extendedDimension = intStack[intPtr--];
	AbstractVariableDeclaration declaration;
	// create the ast node
	boolean isLocalDeclaration = nestedMethod[nestedType] != 0; 
	if (isLocalDeclaration) {
		// create the local variable declarations
		declaration = 
			this.createLocalDeclaration(identifierName, (int) (namePosition >>> 32), (int) namePosition);
	} else {
		// create the field declaration
		declaration = 
			this.createFieldDeclaration(identifierName, (int) (namePosition >>> 32), (int) namePosition); 
	}
	
	identifierPtr--;
	identifierLengthPtr--;
	TypeReference type;
	int variableIndex = variablesCounter[nestedType];
	int typeDim = 0;
	if (variableIndex == 0) {
		// first variable of the declaration (FieldDeclaration or LocalDeclaration)
		if (isLocalDeclaration) {
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			if (declaration.declarationSourceStart == -1) {
				// this is true if there is no modifiers for the local variable declaration
				declaration.declarationSourceStart = type.sourceStart;
			}
			pushOnAstStack(type);
		} else {
			type = getTypeReference(typeDim = intStack[intPtr--]); // type dimension
			pushOnAstStack(type);
			declaration.declarationSourceStart = intStack[intPtr--];
			declaration.modifiers = intStack[intPtr--];
			
			// Store javadoc only on first declaration as it is the same for all ones
			FieldDeclaration fieldDeclaration = (FieldDeclaration) declaration;
			fieldDeclaration.javadoc = this.javadoc;
			this.javadoc = null;
		}
	} else {
		type = (TypeReference) astStack[astPtr - variableIndex];
		typeDim = type.dimensions();
		AbstractVariableDeclaration previousVariable = 
			(AbstractVariableDeclaration) astStack[astPtr]; 
		declaration.declarationSourceStart = previousVariable.declarationSourceStart;
		declaration.modifiers = previousVariable.modifiers;
	}

	if (extendedDimension == 0) {
		declaration.type = type;
	} else {
		int dimension = typeDim + extendedDimension;
		//on the identifierLengthStack there is the information about the type....
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			int typeSourceStart = type.sourceStart;
			int typeSourceEnd = type.sourceEnd;
			type = TypeReference.baseTypeReference(-baseType, dimension);
			type.sourceStart = typeSourceStart;
			type.sourceEnd = typeSourceEnd;
			declaration.type = type;
		} else {
			declaration.type = this.copyDims(type, dimension);
		}
	}
	variablesCounter[nestedType]++;
	pushOnAstStack(declaration);
	// recovery
	if (currentElement != null) {
		if (!(currentElement instanceof RecoveredType)
			&& (currentToken == TokenNameDOT
				//|| declaration.modifiers != 0
				|| (scanner.getLineNumber(declaration.type.sourceStart)
						!= scanner.getLineNumber((int) (namePosition >>> 32))))){
			lastCheckPoint = (int) (namePosition >>> 32);
			restartRecovery = true;
			return;
		}
		if (isLocalDeclaration){
			LocalDeclaration localDecl = (LocalDeclaration) astStack[astPtr];
			lastCheckPoint = localDecl.sourceEnd + 1;
			currentElement = currentElement.add(localDecl, 0);
		} else {
			FieldDeclaration fieldDecl = (FieldDeclaration) astStack[astPtr];
			lastCheckPoint = fieldDecl.sourceEnd + 1;
			currentElement = currentElement.add(fieldDecl, 0);
		}
		lastIgnoredToken = -1;
	}
}
protected void consumeEnumBodyNoConstants() {
}
protected void consumeEnumBodyWithConstants() {
	// merge the constants values with the class body
	concatNodeLists();
}
protected void consumeEnumConstantNoClassBody() {
	EnumConstant enumConstant = new EnumConstant(this.compilationUnit.compilationResult);
	long pos = identifierPositionStack[identifierPtr];
	enumConstant.sourceEnd = (int) pos;
	enumConstant.sourceStart = (int) (pos >>> 32);
	enumConstant.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	// fill arguments if needed
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			enumConstant.arguments = new Expression[length], 
			0, 
			length); 
	}

	pushOnAstStack(enumConstant);
}
protected void consumeEnumConstants() {
	concatNodeLists();
}
protected void consumeEnumConstantWithClassBody() {
	EnumConstant enumConstant = new EnumConstant(this.compilationUnit.compilationResult);
	long pos = identifierPositionStack[identifierPtr];
	enumConstant.sourceEnd = (int) pos;
	enumConstant.sourceStart = (int) (pos >>> 32);
	enumConstant.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	// fill arguments if needed
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			enumConstant.arguments = new Expression[length], 
			0, 
			length); 
	}

	dispatchDeclarationInto(enumConstant, astLengthStack[astLengthPtr--]);
	pushOnAstStack(enumConstant);
}
protected void consumeEnumDeclaration() {
	// EnumDeclaration ::= EnumHeader ClassHeaderImplementsopt EnumBody
	// TODO needs to be reviewed

	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch according to the type of the declarations
		dispatchDeclarationIntoEnumDeclaration(length);
	}

	EnumDeclaration enumDeclaration = (EnumDeclaration) astStack[astPtr];

	// mark initializers with local type mark if needed
	markInitializersWithLocalType(enumDeclaration);

	//convert constructor that do not have the type's name into methods
	boolean hasConstructor = enumDeclaration.checkConstructors(this);
	
	//add the default constructor when needed (interface don't have it)
	if (!hasConstructor && !enumDeclaration.isInterface()) {
		boolean insideFieldInitializer = false;
		if (diet) {
			for (int i = nestedType; i > 0; i--){
				if (variablesCounter[i] > 0) {
					insideFieldInitializer = true;
					break;
				}
			}
		}
		enumDeclaration.createsInternalConstructor(!diet || insideFieldInitializer, true);
	}

	//always add <clinit> (will be remove at code gen time if empty)
	if (this.scanner.containsAssertKeyword) {
		enumDeclaration.bits |= ASTNode.AddAssertionMASK;
	}
	enumDeclaration.addClinit();
	enumDeclaration.bodyEnd = endStatementPosition;
	if (length == 0 && !containsComment(enumDeclaration.bodyStart, enumDeclaration.bodyEnd)) {
		enumDeclaration.bits |= ASTNode.UndocumentedEmptyBlockMASK;
	}

	enumDeclaration.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition); 
}
protected void consumeEnumHeader() {
	// EnumHeader ::= Modifiersopt 'enum' Identifier
	EnumDeclaration enumDeclaration;
	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			enumDeclaration = new EnumDeclaration(this.compilationUnit.compilationResult);
		} else {
			enumDeclaration = new EnumDeclaration(this.compilationUnit.compilationResult);
		}
	} else {
		// Record that the block has a declaration for local types
		enumDeclaration = new EnumDeclaration(this.compilationUnit.compilationResult);
//		markEnclosingMemberWithLocalType();
		blockReal();
	}
	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	enumDeclaration.sourceEnd = (int) pos;
	enumDeclaration.sourceStart = (int) (pos >>> 32);
	enumDeclaration.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	enumDeclaration.declarationSourceStart = intStack[intPtr--]; 
	intPtr--; // remove the end position of the class token

	enumDeclaration.modifiersSourceStart = intStack[intPtr--];
	enumDeclaration.modifiers = intStack[intPtr--];
	if (enumDeclaration.modifiersSourceStart >= 0) {
		enumDeclaration.declarationSourceStart = enumDeclaration.modifiersSourceStart;
	}
	enumDeclaration.bodyStart = enumDeclaration.sourceEnd + 1;
	pushOnAstStack(enumDeclaration);

	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ 
		lastCheckPoint = enumDeclaration.bodyStart;
		currentElement = currentElement.add(enumDeclaration, 0);
		lastIgnoredToken = -1;
	}
	// javadoc
	enumDeclaration.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeEqualityExpression(int op) {
	// EqualityExpression ::= EqualityExpression '==' RelationalExpression
	// EqualityExpression ::= EqualityExpression '!=' RelationalExpression

	//optimize the push/pop

	expressionPtr--;
	expressionLengthPtr--;
	expressionStack[expressionPtr] =
		new EqualExpression(
			expressionStack[expressionPtr],
			expressionStack[expressionPtr + 1],
			op);
}
/*
 * @param op
 */
protected void consumeEqualityExpressionWithName(int op) {
	// EqualityExpression ::= Name '==' RelationalExpression
	// EqualityExpression ::= Name '!=' RelationalExpression
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	expressionPtr--;
	expressionLengthPtr--;
	expressionStack[expressionPtr] =
		new EqualExpression(
			expressionStack[expressionPtr + 1],
			expressionStack[expressionPtr],
			op);
}
protected void consumeExitVariableWithInitialization() {
	// ExitVariableWithInitialization ::= $empty
	// do nothing by default
	expressionLengthPtr--;
	AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
	variableDecl.initialization = expressionStack[expressionPtr--];
	// we need to update the declarationSourceEnd of the local variable declaration to the
	// source end position of the initialization expression
	variableDecl.declarationSourceEnd = variableDecl.initialization.sourceEnd;
	variableDecl.declarationEnd = variableDecl.initialization.sourceEnd;
	
	this.recoveryExitFromVariable();
}
protected void consumeExitVariableWithoutInitialization() {
	// ExitVariableWithoutInitialization ::= $empty
	// do nothing by default
	
	AbstractVariableDeclaration variableDecl = (AbstractVariableDeclaration) astStack[astPtr];
	variableDecl.declarationSourceEnd = variableDecl.declarationEnd;
	
	this.recoveryExitFromVariable();
}
protected void consumeExplicitConstructorInvocation(int flag, int recFlag) {

	/* flag allows to distinguish 3 cases :
	(0) :   
	ExplicitConstructorInvocation ::= 'this' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= 'super' '(' ArgumentListopt ')' ';'
	(1) :
	ExplicitConstructorInvocation ::= Primary '.' 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Primary '.' 'this' '(' ArgumentListopt ')' ';'
	(2) :
	ExplicitConstructorInvocation ::= Name '.' 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Name '.' 'this' '(' ArgumentListopt ')' ';'
	*/
	int startPosition = intStack[intPtr--];
	ExplicitConstructorCall ecc = new ExplicitConstructorCall(recFlag);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, ecc.arguments = new Expression[length], 0, length);
	}
	switch (flag) {
		case 0 :
			ecc.sourceStart = startPosition;
			break;
		case 1 :
			expressionLengthPtr--;
			ecc.sourceStart = (ecc.qualification = expressionStack[expressionPtr--]).sourceStart;
			break;
		case 2 :
			ecc.sourceStart = (ecc.qualification = getUnspecifiedReferenceOptimized()).sourceStart;
			break;
	}
	pushOnAstStack(ecc);
	ecc.sourceEnd = endPosition;
}
protected void consumeExplicitConstructorInvocationWithTypeArguments(int flag, int recFlag) {

	/* flag allows to distinguish 3 cases :
	(0) :   
	ExplicitConstructorInvocation ::= TypeArguments 'this' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= TypeArguments 'super' '(' ArgumentListopt ')' ';'
	(1) :
	ExplicitConstructorInvocation ::= Primary '.' TypeArguments 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Primary '.' TypeArguments 'this' '(' ArgumentListopt ')' ';'
	(2) :
	ExplicitConstructorInvocation ::= Name '.' TypeArguments 'super' '(' ArgumentListopt ')' ';'
	ExplicitConstructorInvocation ::= Name '.' TypeArguments 'this' '(' ArgumentListopt ')' ';'
	*/
	int startPosition = intStack[intPtr--];
	ParameterizedExplicitConstructorCall ecc = new ParameterizedExplicitConstructorCall(recFlag);
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(expressionStack, expressionPtr + 1, ecc.arguments = new Expression[length], 0, length);
	}
	length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, ecc.typeArguments = new TypeReference[length], 0, length);

	switch (flag) {
		case 0 :
			ecc.sourceStart = startPosition;
			break;
		case 1 :
			expressionLengthPtr--;
			ecc.sourceStart = (ecc.qualification = expressionStack[expressionPtr--]).sourceStart;
			break;
		case 2 :
			ecc.sourceStart = (ecc.qualification = getUnspecifiedReferenceOptimized()).sourceStart;
			break;
	}
	
	pushOnAstStack(ecc);
	ecc.sourceEnd = endPosition;
}
protected void consumeExpressionStatement() {
	// ExpressionStatement ::= StatementExpression ';'
	expressionLengthPtr--;
	pushOnAstStack(expressionStack[expressionPtr--]);
}
protected void consumeFieldAccess(boolean isSuperAccess) {
	// FieldAccess ::= Primary '.' 'Identifier'
	// FieldAccess ::= 'super' '.' 'Identifier'

	FieldReference fr =
		new FieldReference(
			identifierStack[identifierPtr],
			identifierPositionStack[identifierPtr--]);
	identifierLengthPtr--;
	if (isSuperAccess) {
		//considerates the fieldReference beginning at the 'super' ....	
		fr.sourceStart = intStack[intPtr--];
		fr.receiver = new SuperReference(fr.sourceStart, endPosition);
		pushOnExpressionStack(fr);
	} else {
		//optimize push/pop
		if ((fr.receiver = expressionStack[expressionPtr]).isThis()) {
			//fieldreference begins at the this
			fr.sourceStart = fr.receiver.sourceStart;
		}
		expressionStack[expressionPtr] = fr;
	}
}
protected void consumeFieldDeclaration() {
	// See consumeLocalVariableDeclarationDefaultModifier() in case of change: duplicated code
	// FieldDeclaration ::= Modifiersopt Type VariableDeclarators ';'

	/*
	astStack : 
	expressionStack: Expression Expression ...... Expression
	identifierStack : type  identifier identifier ...... identifier
	intStack : typeDim      dim        dim               dim
	 ==>
	astStack : FieldDeclaration FieldDeclaration ...... FieldDeclaration
	expressionStack :
	identifierStack : 
	intStack : 
	  
	*/
	int variableDeclaratorsCounter = astLengthStack[astLengthPtr];

	for (int i = variableDeclaratorsCounter - 1; i >= 0; i--) {
		FieldDeclaration fieldDeclaration = (FieldDeclaration) astStack[astPtr - i];
		fieldDeclaration.declarationSourceEnd = endStatementPosition; 
		fieldDeclaration.declarationEnd = endStatementPosition;	// semi-colon included
	}
	
	updateSourceDeclarationParts(variableDeclaratorsCounter);
	int endPos = flushCommentsDefinedPriorTo(endStatementPosition);
	if (endPos != endStatementPosition) {
		for (int i = 0; i < variableDeclaratorsCounter; i++) {
			FieldDeclaration fieldDeclaration = (FieldDeclaration) astStack[astPtr - i];
			fieldDeclaration.declarationSourceEnd = endPos;
		}
	}
	// update the astStack, astPtr and astLengthStack
	int startIndex = astPtr - variablesCounter[nestedType] + 1;
	System.arraycopy(
		astStack, 
		startIndex, 
		astStack, 
		startIndex - 1, 
		variableDeclaratorsCounter); 
	astPtr--; // remove the type reference
	astLengthStack[--astLengthPtr] = variableDeclaratorsCounter;

	// recovery
	if (currentElement != null) {
		lastCheckPoint = endPos + 1;
		if (currentElement.parent != null && currentElement instanceof RecoveredField){
			if (!(currentElement instanceof RecoveredInitializer)) {
				currentElement = currentElement.parent;
			}
		}
		restartRecovery = true;
	}
	variablesCounter[nestedType] = 0;
}
protected void consumeForceNoDiet() {
	// ForceNoDiet ::= $empty
	dietInt++;
}
protected void consumeForInit() {
	// ForInit ::= StatementExpressionList
	pushOnAstLengthStack(-1);
}
protected void consumeFormalParameter(boolean isVarArgs) {
	// FormalParameter ::= Type VariableDeclaratorId ==> false
	// FormalParameter ::= Modifiers Type VariableDeclaratorId ==> true
	/*
	astStack : 
	identifierStack : type identifier
	intStack : dim dim
	 ==>
	astStack : Argument
	identifierStack :  
	intStack :  
	*/

	identifierLengthPtr--;
	char[] identifierName = identifierStack[identifierPtr];
	long namePositions = identifierPositionStack[identifierPtr--];
	TypeReference type = getTypeReference(intStack[intPtr--] + intStack[intPtr--]);
	int modifierPositions = intStack[intPtr--];
	intPtr--;
	Argument arg = 
		new Argument(
			identifierName, 
			namePositions, 
			type, 
			intStack[intPtr + 1] & ~AccDeprecated,
			isVarArgs); // modifiers
	arg.declarationSourceStart = modifierPositions;
	pushOnAstStack(arg);

	/* if incomplete method header, listLength counter will not have been reset,
		indicating that some arguments are available on the stack */
	listLength++; 	
}
protected void consumeFormalParameterList() {
	// FormalParameterList ::= FormalParameterList ',' FormalParameter
	optimizedConcatNodeLists();
}
protected void consumeFormalParameterListopt() {
	// FormalParameterListopt ::= $empty
	pushOnAstLengthStack(0);
}
protected void consumeGenericTypeArrayType() {
}
protected void consumeGenericTypeNameArrayType() {
	pushOnGenericsLengthStack(0); // handle type arguments
}
protected void consumeImportDeclaration() {
	// SingleTypeImportDeclaration ::= SingleTypeImportDeclarationName ';'
	ImportReference impt = (ImportReference) astStack[astPtr];
	// flush annotations defined prior to import statements
	impt.declarationEnd = endStatementPosition;
	impt.declarationSourceEnd = 
		this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd); 

	// recovery
	if (currentElement != null) {
		lastCheckPoint = impt.declarationSourceEnd + 1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; 
		// used to avoid branching back into the regular automaton
	}
}
protected void consumeImportDeclarations() {
	// ImportDeclarations ::= ImportDeclarations ImportDeclaration 
	optimizedConcatNodeLists();
}
protected void consumeImportDeclarationsopt() {
	// ImportDeclarationsopt ::= ImportDeclarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(
			astStack,
			astPtr + 1,
			compilationUnit.imports = new ImportReference[length],
			0,
			length);
	}
}
protected void consumeInsideCastExpression() {
	// InsideCastExpression ::= $empty
}
protected void consumeInsideCastExpressionLL1() {
	// InsideCastExpressionLL1 ::= $empty
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumeInstanceOfExpression(int op) {
	// RelationalExpression ::= RelationalExpression 'instanceof' ReferenceType
	//optimize the push/pop

	//by construction, no base type may be used in getTypeReference
	Expression exp;
	expressionStack[expressionPtr] = exp =
		new InstanceOfExpression(
			expressionStack[expressionPtr],
			getTypeReference(intStack[intPtr--]),
			op);
	if (exp.sourceEnd == 0) {
		//array on base type....
		exp.sourceEnd = scanner.startPosition - 1;
	}
	//the scanner is on the next token already....
}
/**
 * @param op
 */
protected void consumeInstanceOfExpressionWithName(int op) {
	// RelationalExpression_NotName ::= Name instanceof ReferenceType
	//optimize the push/pop

	//by construction, no base type may be used in getTypeReference
	TypeReference reference = getTypeReference(intStack[intPtr--]);
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	Expression exp;
	expressionStack[expressionPtr] = exp =
		new InstanceOfExpression(
			expressionStack[expressionPtr],
			reference,
			op);
	if (exp.sourceEnd == 0) {
		//array on base type....
		exp.sourceEnd = scanner.startPosition - 1;
	}
	//the scanner is on the next token already....
}
protected void consumeInterfaceDeclaration() {
	// see consumeClassDeclaration in case of changes: duplicated code
	// InterfaceDeclaration ::= InterfaceHeader InterfaceBody
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch.....according to the type of the declarations
		dispatchDeclarationInto(length);
	}

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	
	// mark initializers with local type mark if needed
	markInitializersWithLocalType(typeDecl);

	//convert constructor that do not have the type's name into methods
	typeDecl.checkConstructors(this);
	
	//always add <clinit> (will be remove at code gen time if empty)
	if (this.scanner.containsAssertKeyword) {
		typeDecl.bits |= ASTNode.AddAssertionMASK;
	}
	typeDecl.addClinit();
	typeDecl.bodyEnd = endStatementPosition;
	if (length == 0 && !containsComment(typeDecl.bodyStart, typeDecl.bodyEnd)) {
		typeDecl.bits |= ASTNode.UndocumentedEmptyBlockMASK;
	}
	typeDecl.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition); 
}
protected void consumeInterfaceHeader() {
	// InterfaceHeader ::= InterfaceHeaderName InterfaceHeaderExtendsopt

	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];	
	if (currentToken == TokenNameLBRACE){ 
		typeDecl.bodyStart = scanner.currentPosition;
	}
	if (currentElement != null){
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
	// flush the comments related to the interface header
	scanner.commentPtr = -1;	
}
protected void consumeInterfaceHeaderExtends() {
	// InterfaceHeaderExtends ::= 'extends' InterfaceTypeList
	int length = astLengthStack[astLengthPtr--];
	//super interfaces
	astPtr -= length;
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		typeDecl.superInterfaces = new TypeReference[length], 
		0, 
		length); 
	typeDecl.bodyStart = typeDecl.superInterfaces[length-1].sourceEnd + 1;		
	listLength = 0; // reset after having read super-interfaces		
	// recovery
	if (currentElement != null) { 
		lastCheckPoint = typeDecl.bodyStart;
	}
}
protected void consumeInterfaceHeaderName() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier'
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);

	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberTypeMASK;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalTypeMASK;
		markEnclosingMemberWithLocalType();
		blockReal();
	}


	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--; // remove the end position of the class token
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ // is recovering
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;		
	}
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeInterfaceHeaderNameWithTypeParameters() {
	// InterfaceHeaderName ::= Modifiersopt 'interface' 'Identifier' TypeParameters
	TypeDeclaration typeDecl = new TypeDeclaration(this.compilationUnit.compilationResult);

	if (nestedMethod[nestedType] == 0) {
		if (nestedType != 0) {
			typeDecl.bits |= ASTNode.IsMemberTypeMASK;
		}
	} else {
		// Record that the block has a declaration for local types
		typeDecl.bits |= ASTNode.IsLocalTypeMASK;
		markEnclosingMemberWithLocalType();
		blockReal();
	}

	// consume type parameters
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, typeDecl.typeParameters = new TypeParameter[length], 0, length);
	
	//highlight the name of the type
	long pos = identifierPositionStack[identifierPtr];
	typeDecl.sourceEnd = (int) pos;
	typeDecl.sourceStart = (int) (pos >>> 32);
	typeDecl.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;

	//compute the declaration source too
	// 'class' and 'interface' push two int positions: the beginning of the class token and its end.
	// we want to keep the beginning position but get rid of the end position
	// it is only used for the ClassLiteralAccess positions.
	typeDecl.declarationSourceStart = intStack[intPtr--];
	intPtr--; // remove the end position of the class token
	typeDecl.modifiersSourceStart = intStack[intPtr--];
	typeDecl.modifiers = intStack[intPtr--];
	if (typeDecl.modifiersSourceStart >= 0) {
		typeDecl.declarationSourceStart = typeDecl.modifiersSourceStart;
	}
	typeDecl.bodyStart = typeDecl.sourceEnd + 1;
	pushOnAstStack(typeDecl);
	listLength = 0; // will be updated when reading super-interfaces
	// recovery
	if (currentElement != null){ // is recovering
		lastCheckPoint = typeDecl.bodyStart;
		currentElement = currentElement.add(typeDecl, 0);
		lastIgnoredToken = -1;		
	}
	// javadoc
	typeDecl.javadoc = this.javadoc;
	this.javadoc = null;
}
protected void consumeInterfaceMemberDeclarations() {
	// InterfaceMemberDeclarations ::= InterfaceMemberDeclarations InterfaceMemberDeclaration
	concatNodeLists();
}
protected void consumeInterfaceMemberDeclarationsopt() {
	// InterfaceMemberDeclarationsopt ::= NestedType InterfaceMemberDeclarations
	nestedType--;
}
protected void consumeInterfaceType() {
	// InterfaceType ::= ClassOrInterfaceType
	pushOnAstStack(getTypeReference(0));
	/* if incomplete type header, listLength counter will not have been reset,
		indicating that some interfaces are available on the stack */
	listLength++; 	
}
protected void consumeInterfaceTypeList() {
	// InterfaceTypeList ::= InterfaceTypeList ',' InterfaceType
	optimizedConcatNodeLists();
}

protected void consumeInvalidConstructorDeclaration() {
	// ConstructorDeclaration ::= ConstructorHeader ';'
	// now we know that the top of stack is a constructorDeclaration
	ConstructorDeclaration cd = (ConstructorDeclaration) astStack[astPtr];

	cd.bodyEnd = endPosition; // position just before the trailing semi-colon
	cd.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition); 
	// report the problem and continue the parsing - narrowing the problem onto the method
	
	cd.modifiers |= AccSemicolonBody; // remember semi-colon body
}
protected void consumeLeftParen() {
	// PushLPAREN ::= '('
	pushOnIntStack(lParenPos);
}
protected void consumeLocalVariableDeclaration() {
	// LocalVariableDeclaration ::= Modifiers Type VariableDeclarators ';'

	/*
	astStack : 
	expressionStack: Expression Expression ...... Expression
	identifierStack : type  identifier identifier ...... identifier
	intStack : typeDim      dim        dim               dim
	 ==>
	astStack : FieldDeclaration FieldDeclaration ...... FieldDeclaration
	expressionStack :
	identifierStack : 
	intStack : 
	  
	*/
	int variableDeclaratorsCounter = astLengthStack[astLengthPtr];

	// update the astStack, astPtr and astLengthStack
	int startIndex = astPtr - variablesCounter[nestedType] + 1;
	System.arraycopy(
		astStack, 
		startIndex, 
		astStack, 
		startIndex - 1, 
		variableDeclaratorsCounter); 
	astPtr--; // remove the type reference
	astLengthStack[--astLengthPtr] = variableDeclaratorsCounter;
	variablesCounter[nestedType] = 0;
}
protected void consumeLocalVariableDeclarationStatement() {
	// LocalVariableDeclarationStatement ::= LocalVariableDeclaration ';'
	// see blockReal in case of change: duplicated code
	// increment the amount of declared variables for this block
	realBlockStack[realBlockPtr]++;
	
	// update source end to include the semi-colon
	int variableDeclaratorsCounter = astLengthStack[astLengthPtr];
	for (int i = variableDeclaratorsCounter - 1; i >= 0; i--) {
		LocalDeclaration localDeclaration = (LocalDeclaration) astStack[astPtr - i];
		localDeclaration.declarationSourceEnd = endStatementPosition; 
		localDeclaration.declarationEnd = endStatementPosition;	// semi-colon included
	}

}
protected void consumeMarkerAnnotation() {
	// TODO Auto-generated method stub
}
protected void consumeMemberValueArrayInitializer() {
	// TODO Auto-generated method stub	
}
protected void consumeMemberValuePair() {
	// TODO Auto-generated method stub
}
protected void consumeMemberValuePairs() {
	// TODO Auto-generated method stub	
}
protected void consumeMemberValues() {
	// TODO Auto-generated method stub	
}
protected void consumeMethodBody() {
	// MethodBody ::= NestedMethod '{' BlockStatementsopt '}' 
	nestedMethod[nestedType] --;
}
protected void consumeMethodDeclaration(boolean isNotAbstract) {
	// MethodDeclaration ::= MethodHeader MethodBody
	// AbstractMethodDeclaration ::= MethodHeader ';'

	/*
	astStack : modifiers arguments throws statements
	identifierStack : type name
	intStack : dim dim dim
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	intStack : 
	*/

	int length;
	if (isNotAbstract) {
		// pop the position of the {  (body of the method) pushed in block decl
		intPtr--;
		intPtr--;
	}

	int explicitDeclarations = 0;
	Statement[] statements = null;
	if (isNotAbstract) {
		//statements
		explicitDeclarations = realBlockStack[realBlockPtr--];
		if ((length = astLengthStack[astLengthPtr--]) != 0) {
			System.arraycopy(
				astStack, 
				(astPtr -= length) + 1, 
				statements = new Statement[length], 
				0, 
				length); 
		}
	}

	// now we know that we have a method declaration at the top of the ast stack
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	md.statements = statements;
	md.explicitDeclarations = explicitDeclarations;

	// cannot be done in consumeMethodHeader because we have no idea whether or not there
	// is a body when we reduce the method header
	if (!isNotAbstract) { //remember the fact that the method has a semicolon body
		md.modifiers |= AccSemicolonBody;
	} else {
		if (!diet && statements == null) {
			if (!containsComment(md.bodyStart, endPosition)) {
				md.bits |= ASTNode.UndocumentedEmptyBlockMASK;
			}
		}
	}
	// store the endPosition (position just before the '}') in case there is
	// a trailing comment behind the end of the method
	md.bodyEnd = endPosition;
	md.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
}
protected void consumeMethodHeader() {
	// MethodHeader ::= MethodHeaderName MethodHeaderParameters MethodHeaderExtendedDims ThrowsClauseopt
	// retrieve end position of method declarator
	AbstractMethodDeclaration method = (AbstractMethodDeclaration)astStack[astPtr];

	if (currentToken == TokenNameLBRACE){ 
		method.bodyStart = scanner.currentPosition;
	}
	// recovery
	if (currentElement != null){
		if (currentToken == TokenNameSEMICOLON){
			method.modifiers |= AccSemicolonBody;			
			method.declarationSourceEnd = scanner.currentPosition-1;
			method.bodyEnd = scanner.currentPosition-1;
			if (currentElement.parseTree() == method && currentElement.parent != null) {
				currentElement = currentElement.parent;
			}
		}		
		restartRecovery = true; // used to avoid branching back into the regular automaton
	}		
}
protected void consumeMethodHeaderExtendedDims() {
	// MethodHeaderExtendedDims ::= Dimsopt
	// now we update the returnType of the method
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	int extendedDims = intStack[intPtr--];
	if (extendedDims != 0) {
		TypeReference returnType = md.returnType;
		md.sourceEnd = endPosition;
		int dims = returnType.dimensions() + extendedDims;
		int baseType;
		if ((baseType = identifierLengthStack[identifierLengthPtr + 1]) < 0) {
			//it was a baseType
			int sourceStart = returnType.sourceStart;
			int sourceEnd =  returnType.sourceEnd;
			returnType = TypeReference.baseTypeReference(-baseType, dims);
			returnType.sourceStart = sourceStart;
			returnType.sourceEnd = sourceEnd;
			md.returnType = returnType;
		} else {
			md.returnType = this.copyDims(md.returnType, dims);
		}
		if (currentToken == TokenNameLBRACE){ 
			md.bodyStart = endPosition + 1;
		}
		// recovery
		if (currentElement != null){
			lastCheckPoint = md.bodyStart;
		}		
	}
}
protected void consumeMethodHeaderName() {
	// MethodHeaderName ::= Modifiersopt Type 'Identifier' '('
	MethodDeclaration md = new MethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(intStack[intPtr--]);
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];
	// javadoc
	md.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(md);
	md.sourceEnd = lParenPos;
	md.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws
	
	// recovery
	if (currentElement != null){
		if (currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (scanner.getLineNumber(md.returnType.sourceStart)
					== scanner.getLineNumber(md.sourceStart))){
			lastCheckPoint = md.bodyStart;
			currentElement = currentElement.add(md, 0);
			lastIgnoredToken = -1;
		} else {
			lastCheckPoint = md.sourceStart;
			restartRecovery = true;
		}
	}		
}
protected void consumeMethodHeaderNameWithTypeParameters() {
	// MethodHeaderName ::= Modifiersopt TypeParameters Type 'Identifier' '('
	ParameterizedMethodDeclaration md = new ParameterizedMethodDeclaration(this.compilationUnit.compilationResult);

	//name
	md.selector = identifierStack[identifierPtr];
	long selectorSource = identifierPositionStack[identifierPtr--];
	identifierLengthPtr--;
	//type
	md.returnType = getTypeReference(intStack[intPtr--]);
	
	// consume type parameters
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, md.typeParameters = new TypeParameter[length], 0, length);
	
	//modifiers
	md.declarationSourceStart = intStack[intPtr--];
	md.modifiers = intStack[intPtr--];
	// javadoc
	md.javadoc = this.javadoc;
	this.javadoc = null;

	//highlight starts at selector start
	md.sourceStart = (int) (selectorSource >>> 32);
	pushOnAstStack(md);
	md.sourceEnd = lParenPos;
	md.bodyStart = lParenPos+1;
	listLength = 0; // initialize listLength before reading parameters/throws
	
	// recovery
	if (currentElement != null){
		if (currentElement instanceof RecoveredType 
			//|| md.modifiers != 0
			|| (scanner.getLineNumber(md.returnType.sourceStart)
					== scanner.getLineNumber(md.sourceStart))){
			lastCheckPoint = md.bodyStart;
			currentElement = currentElement.add(md, 0);
			lastIgnoredToken = -1;
		} else {
			lastCheckPoint = md.sourceStart;
			restartRecovery = true;
		}
	}		
}
protected void consumeMethodHeaderParameters() {
	// MethodHeaderParameters ::= FormalParameterListopt ')'
	int length = astLengthStack[astLengthPtr--];
	astPtr -= length;
	AbstractMethodDeclaration md = (AbstractMethodDeclaration) astStack[astPtr];
	md.sourceEnd = 	rParenPos;
	//arguments
	if (length != 0) {
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			md.arguments = new Argument[length], 
			0, 
			length); 
	}
	md.bodyStart = rParenPos+1;
	listLength = 0; // reset listLength after having read all parameters
	// recovery
	if (currentElement != null){
		lastCheckPoint = md.bodyStart;
		if (currentElement.parseTree() == md) return;

		// might not have been attached yet - in some constructor scenarii
		if (md.isConstructor()){
			if ((length != 0)
				|| (currentToken == TokenNameLBRACE) 
				|| (currentToken == TokenNamethrows)){
				currentElement = currentElement.add(md, 0);
				lastIgnoredToken = -1;
			}	
		}	
	}	
}
protected void consumeMethodHeaderThrowsClause() {
	// MethodHeaderThrowsClause ::= 'throws' ClassTypeList
	int length = astLengthStack[astLengthPtr--];
	astPtr -= length;
	AbstractMethodDeclaration md = (AbstractMethodDeclaration) astStack[astPtr];
	System.arraycopy(
		astStack, 
		astPtr + 1, 
		md.thrownExceptions = new TypeReference[length], 
		0, 
		length);
	md.sourceEnd = md.thrownExceptions[length-1].sourceEnd;
	md.bodyStart = md.thrownExceptions[length-1].sourceEnd + 1;
	listLength = 0; // reset listLength after having read all thrown exceptions	
	// recovery
	if (currentElement != null){
		lastCheckPoint = md.bodyStart;
	}		
}
protected void consumeMethodInvocationName() {
	// MethodInvocation ::= Name '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	MessageSend m = newMessageSend();
	m.sourceEnd = rParenPos;
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];
	if (identifierLengthStack[identifierLengthPtr] == 1) {
		m.receiver = ThisReference.implicitThis();
		identifierLengthPtr--;
	} else {
		identifierLengthStack[identifierLengthPtr]--;
		m.receiver = getUnspecifiedReference();
		m.sourceStart = m.receiver.sourceStart;		
	}
	pushOnExpressionStack(m);
}
protected void consumeMethodInvocationNameWithTypeArguments() {
	// MethodInvocation ::= Name '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'

	// when the name is only an identifier...we have a message send to "this" (implicit)

	ParameterizedMessageSend m = newMessageSendWithTypeArguments();
	m.sourceEnd = rParenPos;
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];

	// handle type arguments
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);
	
	if (identifierLengthStack[identifierLengthPtr] == 1) {
		m.receiver = ThisReference.implicitThis();
		identifierLengthPtr--;
	} else {
		identifierLengthStack[identifierLengthPtr]--;
		m.receiver = getUnspecifiedReference();
		m.sourceStart = m.receiver.sourceStart;		
	}
	pushOnExpressionStack(m);
}
protected void consumeMethodInvocationPrimary() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' 'Identifier' '(' ArgumentListopt ')'

	MessageSend m = newMessageSend();
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	m.receiver = expressionStack[expressionPtr];
	m.sourceStart = m.receiver.sourceStart;
	m.sourceEnd = rParenPos;
	expressionStack[expressionPtr] = m;
}
protected void consumeMethodInvocationPrimaryWithTypeArguments() {
	//optimize the push/pop
	//MethodInvocation ::= Primary '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'

	ParameterizedMessageSend m = newMessageSendWithTypeArguments();
	m.sourceStart = 
		(int) ((m.nameSourcePosition = identifierPositionStack[identifierPtr]) >>> 32); 
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	// handle type arguments
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);

	m.receiver = expressionStack[expressionPtr];
	m.sourceStart = m.receiver.sourceStart;
	m.sourceEnd = rParenPos;
	expressionStack[expressionPtr] = m;
}
protected void consumeMethodInvocationSuper() {
	// MethodInvocation ::= 'super' '.' 'Identifier' '(' ArgumentListopt ')'

	MessageSend m = newMessageSend();
	m.sourceStart = intStack[intPtr--];
	m.sourceEnd = rParenPos;
	m.nameSourcePosition = identifierPositionStack[identifierPtr];
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	m.receiver = new SuperReference(m.sourceStart, endPosition);
	pushOnExpressionStack(m);
}
protected void consumeMethodInvocationSuperWithTypeArguments() {
	// MethodInvocation ::= 'super' '.' TypeArguments 'Identifier' '(' ArgumentListopt ')'

	ParameterizedMessageSend m = newMessageSendWithTypeArguments();
	m.sourceStart = intStack[intPtr--];
	m.sourceEnd = rParenPos;
	m.nameSourcePosition = identifierPositionStack[identifierPtr];
	m.selector = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	// handle type arguments
	int length = genericsLengthStack[genericsLengthPtr--];
	genericsPtr -= length;
	System.arraycopy(genericsStack, genericsPtr + 1, m.typeArguments = new TypeReference[length], 0, length);

	m.receiver = new SuperReference(m.sourceStart, endPosition);
	pushOnExpressionStack(m);
}
protected void consumeModifiers() {
	int savedModifiersSourceStart = modifiersSourceStart;	
	checkComment(); // might update modifiers with AccDeprecated
	pushOnIntStack(modifiers); // modifiers
	if (modifiersSourceStart >= savedModifiersSourceStart) {
		modifiersSourceStart = savedModifiersSourceStart;
	}
	pushOnIntStack(modifiersSourceStart);
	resetModifiers();
}
protected void consumeNameArrayType() {
	pushOnGenericsLengthStack(0); // handle type arguments
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
}
protected void consumeNestedMethod() {
	// NestedMethod ::= $empty
	jumpOverMethodBody();
	nestedMethod[nestedType] ++;
	pushOnIntStack(scanner.currentPosition);
	consumeOpenBlock();
}
protected void consumeNestedType() {
	// NestedType ::= $empty
	nestedType++;
	try {
		nestedMethod[nestedType] = 0;
	} catch (IndexOutOfBoundsException e) {
		//except in test's cases, it should never raise
		int oldL = nestedMethod.length;
		System.arraycopy(nestedMethod , 0, (nestedMethod = new int[oldL + 30]), 0, oldL);
		nestedMethod[nestedType] = 0;
		// increase the size of the fieldsCounter as well. It has to be consistent with the size of the nestedMethod collection
		System.arraycopy(variablesCounter, 0, (variablesCounter = new int[oldL + 30]), 0, oldL);
	}
	variablesCounter[nestedType] = 0;
}
protected void consumeNormalAnnotation() {
	// TODO Auto-generated method stub	
}
protected void consumeOneDimLoop() {
	// OneDimLoop ::= '[' ']'
	dimensions++;
}
protected void consumeOnlySynchronized() {
	// OnlySynchronized ::= 'synchronized'
	pushOnIntStack(this.synchronizedBlockSourceStart);
	resetModifiers();
}
protected void consumeOnlyTypeArguments() {
}
protected void consumeOpenBlock() {
	// OpenBlock ::= $empty

	pushOnIntStack(scanner.startPosition);
	try {
		realBlockStack[++realBlockPtr] = 0;
	} catch (IndexOutOfBoundsException e) {
		//realBlockPtr is correct 
		int oldStackLength = realBlockStack.length;
		int oldStack[] = realBlockStack;
		realBlockStack = new int[oldStackLength + StackIncrement];
		System.arraycopy(oldStack, 0, realBlockStack, 0, oldStackLength);
		realBlockStack[realBlockPtr] = 0;
	}
}
protected void consumePackageDeclaration() {
	// PackageDeclaration ::= 'package' Name ';'
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt = compilationUnit.currentPackage;
	// flush comments defined prior to import statements
	impt.declarationEnd = endStatementPosition;
	impt.declarationSourceEnd = this.flushCommentsDefinedPriorTo(impt.declarationSourceEnd);
}
protected void consumePackageDeclarationName() {
	// PackageDeclarationName ::= 'package' Name
	/* build an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = 
		new char[length = identifierLengthStack[identifierLengthPtr--]][]; 
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, ++identifierPtr, tokens, 0, length);
	System.arraycopy(
		identifierPositionStack, 
		identifierPtr--, 
		positions, 
		0, 
		length); 
	compilationUnit.currentPackage = 
		impt = new ImportReference(tokens, positions, true, AccDefault); 

	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumePostfixExpression() {
	// PostfixExpression ::= Name
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
}
protected void consumePrimaryNoNewArray() {
	// PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
	final Expression parenthesizedExpression = expressionStack[expressionPtr];
	updateSourcePosition(parenthesizedExpression);
	int numberOfParenthesis = (parenthesizedExpression.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT;
	parenthesizedExpression.bits &= ~ASTNode.ParenthesizedMASK;
	parenthesizedExpression.bits |= (numberOfParenthesis + 1) << ASTNode.ParenthesizedSHIFT;
}
protected void consumePrimaryNoNewArrayArrayType() {
	// PrimaryNoNewArray ::= Name Dims '.' 'class'
	intPtr--; // remove the class start position

	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	pushOnGenericsLengthStack(0);

	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--], getTypeReference(intStack[intPtr--])));
}
protected void consumePrimaryNoNewArrayName() {
	// PrimaryNoNewArray ::= Name '.' 'class'
	intPtr--; // remove the class start position

	// handle type arguments
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	pushOnGenericsLengthStack(0);
	TypeReference typeReference = getTypeReference(0);
	
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--], typeReference));
}
protected void consumePrimaryNoNewArrayNameSuper() {
	// PrimaryNoNewArray ::= Name '.' 'super'
	// handle type arguments
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	pushOnGenericsLengthStack(0);
	TypeReference typeReference = getTypeReference(0);

	pushOnExpressionStack(
		new QualifiedSuperReference(
			typeReference,
			intStack[intPtr--],
			endPosition));
}
protected void consumePrimaryNoNewArrayNameThis() {
	// PrimaryNoNewArray ::= Name '.' 'this'
	// handle type arguments
	pushOnGenericsIdentifiersLengthStack(identifierLengthStack[identifierLengthPtr]);
	pushOnGenericsLengthStack(0); // handle type arguments

	TypeReference typeReference = getTypeReference(0);
	
	pushOnExpressionStack(
		new QualifiedThisReference(
			typeReference,
			intStack[intPtr--],
			endPosition));
}
protected void consumePrimaryNoNewArrayPrimitiveArrayType() {
	// PrimaryNoNewArray ::= PrimitiveType Dims '.' 'class'
	intPtr--; // remove the class start position
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--], getTypeReference(intStack[intPtr--])));
}
protected void consumePrimaryNoNewArrayPrimitiveType() {
	// PrimaryNoNewArray ::= PrimitiveType '.' 'class'
	intPtr--; // remove the class start position
	pushOnExpressionStack(
		new ClassLiteralAccess(intStack[intPtr--], getTypeReference(0)));
}
protected void consumePrimaryNoNewArrayThis() {
	// PrimaryNoNewArray ::= 'this'
	pushOnExpressionStack(new ThisReference(intStack[intPtr--], endPosition));
}
protected void consumePrimaryNoNewArrayWithName() {
	// PrimaryNoNewArray ::=  PushLPAREN Expression PushRPAREN 
	pushOnExpressionStack(getUnspecifiedReferenceOptimized());
	final Expression parenthesizedExpression = expressionStack[expressionPtr];
	updateSourcePosition(parenthesizedExpression);
	int numberOfParenthesis = (parenthesizedExpression.bits & ASTNode.ParenthesizedMASK) >> ASTNode.ParenthesizedSHIFT;
	parenthesizedExpression.bits &= ~ASTNode.ParenthesizedMASK;
	parenthesizedExpression.bits |= (numberOfParenthesis + 1) << ASTNode.ParenthesizedSHIFT;
}
protected void consumePrimitiveArrayType() {
}
protected void consumePrimitiveType() {
	// Type ::= PrimitiveType
	pushOnIntStack(0);
}
protected void consumePushModifiers() {
	pushOnIntStack(modifiers); // modifiers
	pushOnIntStack(modifiersSourceStart);
	resetModifiers();
}
protected void consumePushPosition() {
	// for source managment purpose
	// PushPosition ::= $empty
	pushOnIntStack(endPosition);
}
protected void consumeQualifiedName() {
	// QualifiedName ::= Name '.' SimpleName 
	/*back from the recursive loop of QualifiedName.
	Updates identifier length into the length stack*/

	identifierLengthStack[--identifierLengthPtr]++;
}
protected void consumeReferenceType() {
	pushOnIntStack(0); // handle array type
}
protected void consumeReferenceType1() {
	pushOnGenericsStack(getTypeReference(intStack[intPtr--]));	
}
protected void consumeReferenceType2() {
	pushOnGenericsStack(getTypeReference(intStack[intPtr--]));	
}
protected void consumeReferenceType3() {
	pushOnGenericsStack(getTypeReference(intStack[intPtr--]));	
}
protected void consumeRestoreDiet() {
	// RestoreDiet ::= $empty
	dietInt--;
}
protected void consumeRightParen() {
	// PushRPAREN ::= ')'
	pushOnIntStack(rParenPos);
}
// This method is part of an automatic generation : do NOT edit-modify  
protected void consumeRule(int act) {
  switch ( act ) {
    case 26 : if (DEBUG) { System.out.println("Type ::= PrimitiveType"); }  //$NON-NLS-1$
		    consumePrimitiveType();  
			break;
 
    case 40 : if (DEBUG) { System.out.println("ReferenceType ::= ClassOrInterfaceType"); }  //$NON-NLS-1$
		    consumeReferenceType();   
			break;
 
    case 44 : if (DEBUG) { System.out.println("ClassOrInterface ::= Name"); }  //$NON-NLS-1$
		    consumeClassOrInterfaceName();   
			break;
 
    case 45 : if (DEBUG) { System.out.println("ClassOrInterface ::= ClassOrInterface TypeArguments DOT"); }  //$NON-NLS-1$
		    consumeClassOrInterface();   
			break;
 
    case 46 : if (DEBUG) { System.out.println("ArrayTypeWithTypeArgumentsName ::= ClassOrInterface..."); }  //$NON-NLS-1$
		    consumeArrayTypeWithTypeArgumentsName();   
			break;
 
    case 47 : if (DEBUG) { System.out.println("ArrayType ::= PrimitiveType Dims"); }  //$NON-NLS-1$
		    consumePrimitiveArrayType();   
			break;
 
    case 48 : if (DEBUG) { System.out.println("ArrayType ::= Name Dims"); }  //$NON-NLS-1$
		    consumeNameArrayType();   
			break;
 
    case 49 : if (DEBUG) { System.out.println("ArrayType ::= ArrayTypeWithTypeArgumentsName Dims"); }  //$NON-NLS-1$
		    consumeGenericTypeNameArrayType();   
			break;
 
    case 50 : if (DEBUG) { System.out.println("ArrayType ::= ClassOrInterface TypeArguments Dims"); }  //$NON-NLS-1$
		    consumeGenericTypeArrayType();   
			break;
 
    case 55 : if (DEBUG) { System.out.println("QualifiedName ::= Name DOT SimpleName"); }  //$NON-NLS-1$
		    consumeQualifiedName();  
			break;
 
    case 56 : if (DEBUG) { System.out.println("CompilationUnit ::= EnterCompilationUnit..."); }  //$NON-NLS-1$
		    consumeCompilationUnit();  
			break;
 
    case 57 : if (DEBUG) { System.out.println("EnterCompilationUnit ::="); }  //$NON-NLS-1$
		    consumeEnterCompilationUnit();  
			break;
 
    case 68 : if (DEBUG) { System.out.println("CatchHeader ::= catch LPAREN FormalParameter RPAREN..."); }  //$NON-NLS-1$
		    consumeCatchHeader();  
			break;
 
    case 70 : if (DEBUG) { System.out.println("ImportDeclarations ::= ImportDeclarations..."); }  //$NON-NLS-1$
		    consumeImportDeclarations();  
			break;
 
    case 72 : if (DEBUG) { System.out.println("TypeDeclarations ::= TypeDeclarations TypeDeclaration"); }  //$NON-NLS-1$
		    consumeTypeDeclarations();  
			break;
 
    case 73 : if (DEBUG) { System.out.println("PackageDeclaration ::= PackageDeclarationName SEMICOLON"); }  //$NON-NLS-1$
		     consumePackageDeclaration();  
			break;
 
    case 74 : if (DEBUG) { System.out.println("PackageDeclarationName ::= package Name"); }  //$NON-NLS-1$
		     consumePackageDeclarationName();  
			break;
 
    case 79 : if (DEBUG) { System.out.println("SingleTypeImportDeclaration ::=..."); }  //$NON-NLS-1$
		    consumeImportDeclaration();  
			break;
 
    case 80 : if (DEBUG) { System.out.println("SingleTypeImportDeclarationName ::= import Name"); }  //$NON-NLS-1$
		    consumeSingleTypeImportDeclarationName();  
			break;
 
    case 81 : if (DEBUG) { System.out.println("TypeImportOnDemandDeclaration ::=..."); }  //$NON-NLS-1$
		    consumeImportDeclaration();  
			break;
 
    case 82 : if (DEBUG) { System.out.println("TypeImportOnDemandDeclarationName ::= import Name DOT..."); }  //$NON-NLS-1$
		    consumeTypeImportOnDemandDeclarationName();  
			break;
 
     case 85 : if (DEBUG) { System.out.println("TypeDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
		    consumeEmptyTypeDeclaration();  
			break;
 
    case 101 : if (DEBUG) { System.out.println("ClassDeclaration ::= ClassHeader ClassBody"); }  //$NON-NLS-1$
		    consumeClassDeclaration();  
			break;
 
    case 102 : if (DEBUG) { System.out.println("ClassHeader ::= ClassHeaderName ClassHeaderExtendsopt..."); }  //$NON-NLS-1$
		    consumeClassHeader();  
			break;
 
    case 103 : if (DEBUG) { System.out.println("ClassHeaderName ::= Modifiersopt class Identifier..."); }  //$NON-NLS-1$
		    consumeClassHeaderNameWithTypeParameters();  
			break;
 
    case 104 : if (DEBUG) { System.out.println("ClassHeaderName ::= Modifiersopt class Identifier"); }  //$NON-NLS-1$
		    consumeClassHeaderName();  
			break;
 
    case 105 : if (DEBUG) { System.out.println("ClassHeaderExtends ::= extends ClassType"); }  //$NON-NLS-1$
		    consumeClassHeaderExtends();  
			break;
 
    case 106 : if (DEBUG) { System.out.println("ClassHeaderImplements ::= implements InterfaceTypeList"); }  //$NON-NLS-1$
		    consumeClassHeaderImplements();  
			break;
 
    case 108 : if (DEBUG) { System.out.println("InterfaceTypeList ::= InterfaceTypeList COMMA..."); }  //$NON-NLS-1$
		    consumeInterfaceTypeList();  
			break;
 
    case 109 : if (DEBUG) { System.out.println("InterfaceType ::= ClassOrInterfaceType"); }  //$NON-NLS-1$
		    consumeInterfaceType();  
			break;
 
    case 112 : if (DEBUG) { System.out.println("ClassBodyDeclarations ::= ClassBodyDeclarations..."); }  //$NON-NLS-1$
		    consumeClassBodyDeclarations();  
			break;
 
    case 116 : if (DEBUG) { System.out.println("ClassBodyDeclaration ::= Diet NestedMethod Block"); }  //$NON-NLS-1$
		    consumeClassBodyDeclaration();  
			break;
 
    case 117 : if (DEBUG) { System.out.println("Diet ::="); }  //$NON-NLS-1$
		    consumeDiet();  
			break;

    case 118 : if (DEBUG) { System.out.println("Initializer ::= Diet NestedMethod Block"); }  //$NON-NLS-1$
		    consumeClassBodyDeclaration();  
			break;
 
    case 126 : if (DEBUG) { System.out.println("ClassMemberDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
		    consumeEmptyClassMemberDeclaration();  
			break;

    case 127 : if (DEBUG) { System.out.println("FieldDeclaration ::= Modifiersopt Type..."); }  //$NON-NLS-1$
		    consumeFieldDeclaration();  
			break;
 
    case 129 : if (DEBUG) { System.out.println("VariableDeclarators ::= VariableDeclarators COMMA..."); }  //$NON-NLS-1$
		    consumeVariableDeclarators();  
			break;
 
    case 132 : if (DEBUG) { System.out.println("EnterVariable ::="); }  //$NON-NLS-1$
		    consumeEnterVariable();  
			break;
 
    case 133 : if (DEBUG) { System.out.println("ExitVariableWithInitialization ::="); }  //$NON-NLS-1$
		    consumeExitVariableWithInitialization();  
			break;
 
    case 134 : if (DEBUG) { System.out.println("ExitVariableWithoutInitialization ::="); }  //$NON-NLS-1$
		    consumeExitVariableWithoutInitialization();  
			break;
 
    case 135 : if (DEBUG) { System.out.println("ForceNoDiet ::="); }  //$NON-NLS-1$
		    consumeForceNoDiet();  
			break;
 
    case 136 : if (DEBUG) { System.out.println("RestoreDiet ::="); }  //$NON-NLS-1$
		    consumeRestoreDiet();  
			break;
 
    case 141 : if (DEBUG) { System.out.println("MethodDeclaration ::= MethodHeader MethodBody"); }  //$NON-NLS-1$
		    // set to true to consume a method with a body
  consumeMethodDeclaration(true);   
			break;
 
    case 142 : if (DEBUG) { System.out.println("AbstractMethodDeclaration ::= MethodHeader SEMICOLON"); }  //$NON-NLS-1$
		    // set to false to consume a method without body
  consumeMethodDeclaration(false);  
			break;
 
    case 143 : if (DEBUG) { System.out.println("MethodHeader ::= MethodHeaderName MethodHeaderParameters"); }  //$NON-NLS-1$
		    consumeMethodHeader();  
			break;
 
    case 144 : if (DEBUG) { System.out.println("MethodHeaderName ::= Modifiersopt TypeParameters Type..."); }  //$NON-NLS-1$
		    consumeMethodHeaderNameWithTypeParameters();  
			break;
 
    case 145 : if (DEBUG) { System.out.println("MethodHeaderName ::= Modifiersopt Type Identifier LPAREN"); }  //$NON-NLS-1$
		    consumeMethodHeaderName();  
			break;
 
    case 146 : if (DEBUG) { System.out.println("MethodHeaderParameters ::= FormalParameterListopt RPAREN"); }  //$NON-NLS-1$
		    consumeMethodHeaderParameters();  
			break;
 
    case 147 : if (DEBUG) { System.out.println("MethodHeaderExtendedDims ::= Dimsopt"); }  //$NON-NLS-1$
		    consumeMethodHeaderExtendedDims();  
			break;
 
    case 148 : if (DEBUG) { System.out.println("MethodHeaderThrowsClause ::= throws ClassTypeList"); }  //$NON-NLS-1$
		    consumeMethodHeaderThrowsClause();  
			break;
 
    case 149 : if (DEBUG) { System.out.println("ConstructorHeader ::= ConstructorHeaderName..."); }  //$NON-NLS-1$
		    consumeConstructorHeader();  
			break;
 
    case 150 : if (DEBUG) { System.out.println("ConstructorHeaderName ::= Modifiersopt TypeParameters..."); }  //$NON-NLS-1$
		    consumeConstructorHeaderNameWithTypeParameters();  
			break;
 
    case 151 : if (DEBUG) { System.out.println("ConstructorHeaderName ::= Modifiersopt Identifier LPAREN"); }  //$NON-NLS-1$
		    consumeConstructorHeaderName();  
			break;
 
    case 153 : if (DEBUG) { System.out.println("FormalParameterList ::= FormalParameterList COMMA..."); }  //$NON-NLS-1$
		    consumeFormalParameterList();  
			break;
 
    case 154 : if (DEBUG) { System.out.println("FormalParameter ::= Modifiersopt Type..."); }  //$NON-NLS-1$
		    consumeFormalParameter(false);  
			break;
 
    case 155 : if (DEBUG) { System.out.println("FormalParameter ::= Modifiersopt Type ELLIPSIS..."); }  //$NON-NLS-1$
		    consumeFormalParameter(true);  
			break;
 
    case 157 : if (DEBUG) { System.out.println("ClassTypeList ::= ClassTypeList COMMA ClassTypeElt"); }  //$NON-NLS-1$
		    consumeClassTypeList();  
			break;
 
    case 158 : if (DEBUG) { System.out.println("ClassTypeElt ::= ClassType"); }  //$NON-NLS-1$
		    consumeClassTypeElt();  
			break;
 
    case 159 : if (DEBUG) { System.out.println("MethodBody ::= NestedMethod LBRACE BlockStatementsopt..."); }  //$NON-NLS-1$
		    consumeMethodBody();  
			break;
 
    case 160 : if (DEBUG) { System.out.println("NestedMethod ::="); }  //$NON-NLS-1$
		    consumeNestedMethod();  
			break;
 
    case 161 : if (DEBUG) { System.out.println("StaticInitializer ::= StaticOnly Block"); }  //$NON-NLS-1$
		    consumeStaticInitializer();  
			break;

    case 162 : if (DEBUG) { System.out.println("StaticOnly ::= static"); }  //$NON-NLS-1$
		    consumeStaticOnly();  
			break;
 
    case 163 : if (DEBUG) { System.out.println("ConstructorDeclaration ::= ConstructorHeader MethodBody"); }  //$NON-NLS-1$
		    consumeConstructorDeclaration() ;  
			break;
 
    case 164 : if (DEBUG) { System.out.println("ConstructorDeclaration ::= ConstructorHeader SEMICOLON"); }  //$NON-NLS-1$
		    consumeInvalidConstructorDeclaration() ;  
			break;
 
    case 165 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= this LPAREN..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.This);  
			break;
 
    case 166 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= OnlyTypeArguments this"); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(0,ExplicitConstructorCall.This);  
			break;
 
    case 167 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= super LPAREN..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(0,ExplicitConstructorCall.Super);  
			break;
 
    case 168 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(0,ExplicitConstructorCall.Super);  
			break;
 
    case 169 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT super..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.Super);  
			break;
 
    case 170 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(1, ExplicitConstructorCall.Super);  
			break;
 
    case 171 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT super LPAREN"); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.Super);  
			break;
 
    case 172 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(2, ExplicitConstructorCall.Super);  
			break;
 
    case 173 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT this..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(1, ExplicitConstructorCall.This);  
			break;
 
    case 174 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Primary DOT..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(1, ExplicitConstructorCall.This);  
			break;
 
    case 175 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT this LPAREN"); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocation(2, ExplicitConstructorCall.This);  
			break;
 
    case 176 : if (DEBUG) { System.out.println("ExplicitConstructorInvocation ::= Name DOT..."); }  //$NON-NLS-1$
		    consumeExplicitConstructorInvocationWithTypeArguments(2, ExplicitConstructorCall.This);  
			break;
 
    case 177 : if (DEBUG) { System.out.println("InterfaceDeclaration ::= InterfaceHeader InterfaceBody"); }  //$NON-NLS-1$
		    consumeInterfaceDeclaration();  
			break;
 
    case 178 : if (DEBUG) { System.out.println("InterfaceHeader ::= InterfaceHeaderName..."); }  //$NON-NLS-1$
		    consumeInterfaceHeader();  
			break;
 
    case 179 : if (DEBUG) { System.out.println("InterfaceHeaderName ::= Modifiersopt interface..."); }  //$NON-NLS-1$
		    consumeInterfaceHeaderNameWithTypeParameters();  
			break;
 
    case 180 : if (DEBUG) { System.out.println("InterfaceHeaderName ::= Modifiersopt interface..."); }  //$NON-NLS-1$
		    consumeInterfaceHeaderName();  
			break;
 
    case 182 : if (DEBUG) { System.out.println("InterfaceHeaderExtends ::= extends InterfaceTypeList"); }  //$NON-NLS-1$
		    consumeInterfaceHeaderExtends();  
			break;
 
    case 185 : if (DEBUG) { System.out.println("InterfaceMemberDeclarations ::=..."); }  //$NON-NLS-1$
		    consumeInterfaceMemberDeclarations();  
			break;
 
    case 186 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
		    consumeEmptyInterfaceMemberDeclaration();  
			break;
 
    case 189 : if (DEBUG) { System.out.println("InterfaceMemberDeclaration ::= InvalidMethodDeclaration"); }  //$NON-NLS-1$
		    ignoreMethodBody();  
			break;
 
    case 190 : if (DEBUG) { System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader..."); }  //$NON-NLS-1$
		    ignoreInvalidConstructorDeclaration(true);   
			break;
 
    case 191 : if (DEBUG) { System.out.println("InvalidConstructorDeclaration ::= ConstructorHeader..."); }  //$NON-NLS-1$
		    ignoreInvalidConstructorDeclaration(false);   
			break;
 
    case 197 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE ,opt RBRACE"); }  //$NON-NLS-1$
		    consumeEmptyArrayInitializer();  
			break;
 
    case 198 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE VariableInitializers RBRACE"); }  //$NON-NLS-1$
		    consumeArrayInitializer();  
			break;
 
    case 199 : if (DEBUG) { System.out.println("ArrayInitializer ::= LBRACE VariableInitializers COMMA"); }  //$NON-NLS-1$
		    consumeArrayInitializer();  
			break;
 
    case 201 : if (DEBUG) { System.out.println("VariableInitializers ::= VariableInitializers COMMA..."); }  //$NON-NLS-1$
		    consumeVariableInitializers();  
			break;
 
    case 202 : if (DEBUG) { System.out.println("Block ::= OpenBlock LBRACE BlockStatementsopt RBRACE"); }  //$NON-NLS-1$
		    consumeBlock();  
			break;
 
    case 203 : if (DEBUG) { System.out.println("OpenBlock ::="); }  //$NON-NLS-1$
		    consumeOpenBlock() ;  
			break;
 
    case 205 : if (DEBUG) { System.out.println("BlockStatements ::= BlockStatements BlockStatement"); }  //$NON-NLS-1$
		    consumeBlockStatements() ;  
			break;
 
    case 209 : if (DEBUG) { System.out.println("BlockStatement ::= InvalidInterfaceDeclaration"); }  //$NON-NLS-1$
		    ignoreInterfaceDeclaration();  
			break;
 
    case 210 : if (DEBUG) { System.out.println("LocalVariableDeclarationStatement ::=..."); }  //$NON-NLS-1$
		    consumeLocalVariableDeclarationStatement();  
			break;
 
    case 211 : if (DEBUG) { System.out.println("LocalVariableDeclaration ::= Type PushModifiers..."); }  //$NON-NLS-1$
		    consumeLocalVariableDeclaration();  
			break;
 
    case 212 : if (DEBUG) { System.out.println("LocalVariableDeclaration ::= Modifiers Type..."); }  //$NON-NLS-1$
		    consumeLocalVariableDeclaration();  
			break;
 
    case 213 : if (DEBUG) { System.out.println("PushModifiers ::="); }  //$NON-NLS-1$
		    consumePushModifiers();  
			break;
 
    case 239 : if (DEBUG) { System.out.println("EmptyStatement ::= SEMICOLON"); }  //$NON-NLS-1$
		    consumeEmptyStatement();  
			break;
 
    case 240 : if (DEBUG) { System.out.println("LabeledStatement ::= Identifier COLON Statement"); }  //$NON-NLS-1$
		    consumeStatementLabel() ;  
			break;
 
    case 241 : if (DEBUG) { System.out.println("LabeledStatementNoShortIf ::= Identifier COLON..."); }  //$NON-NLS-1$
		    consumeStatementLabel() ;  
			break;
 
     case 242 : if (DEBUG) { System.out.println("ExpressionStatement ::= StatementExpression SEMICOLON"); }  //$NON-NLS-1$
		    consumeExpressionStatement();  
			break;
 
    case 251 : if (DEBUG) { System.out.println("IfThenStatement ::= if LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
		    consumeStatementIfNoElse();  
			break;
 
    case 252 : if (DEBUG) { System.out.println("IfThenElseStatement ::= if LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
		    consumeStatementIfWithElse();  
			break;
 
    case 253 : if (DEBUG) { System.out.println("IfThenElseStatementNoShortIf ::= if LPAREN Expression..."); }  //$NON-NLS-1$
		    consumeStatementIfWithElse();  
			break;
 
    case 254 : if (DEBUG) { System.out.println("SwitchStatement ::= switch LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
		    consumeStatementSwitch() ;  
			break;
 
    case 255 : if (DEBUG) { System.out.println("SwitchBlock ::= LBRACE RBRACE"); }  //$NON-NLS-1$
		    consumeEmptySwitchBlock() ;  
			break;
 
    case 258 : if (DEBUG) { System.out.println("SwitchBlock ::= LBRACE SwitchBlockStatements..."); }  //$NON-NLS-1$
		    consumeSwitchBlock() ;  
			break;
 
    case 260 : if (DEBUG) { System.out.println("SwitchBlockStatements ::= SwitchBlockStatements..."); }  //$NON-NLS-1$
		    consumeSwitchBlockStatements() ;  
			break;
 
    case 261 : if (DEBUG) { System.out.println("SwitchBlockStatement ::= SwitchLabels BlockStatements"); }  //$NON-NLS-1$
		    consumeSwitchBlockStatement() ;  
			break;
 
    case 263 : if (DEBUG) { System.out.println("SwitchLabels ::= SwitchLabels SwitchLabel"); }  //$NON-NLS-1$
		    consumeSwitchLabels() ;  
			break;
 
     case 264 : if (DEBUG) { System.out.println("SwitchLabel ::= case ConstantExpression COLON"); }  //$NON-NLS-1$
		    consumeCaseLabel();  
			break;
 
     case 265 : if (DEBUG) { System.out.println("SwitchLabel ::= default COLON"); }  //$NON-NLS-1$
		    consumeDefaultLabel();  
			break;
 
    case 266 : if (DEBUG) { System.out.println("WhileStatement ::= while LPAREN Expression RPAREN..."); }  //$NON-NLS-1$
		    consumeStatementWhile() ;  
			break;
 
    case 267 : if (DEBUG) { System.out.println("WhileStatementNoShortIf ::= while LPAREN Expression..."); }  //$NON-NLS-1$
		    consumeStatementWhile() ;  
			break;
 
    case 268 : if (DEBUG) { System.out.println("DoStatement ::= do Statement while LPAREN Expression..."); }  //$NON-NLS-1$
		    consumeStatementDo() ;  
			break;
 
    case 269 : if (DEBUG) { System.out.println("ForStatement ::= for LPAREN ForInitopt SEMICOLON..."); }  //$NON-NLS-1$
		    consumeStatementFor() ;  
			break;
 
    case 270 : if (DEBUG) { System.out.println("ForStatementNoShortIf ::= for LPAREN ForInitopt..."); }  //$NON-NLS-1$
		    consumeStatementFor() ;  
			break;
 
    case 271 : if (DEBUG) { System.out.println("ForInit ::= StatementExpressionList"); }  //$NON-NLS-1$
		    consumeForInit() ;  
			break;
 
    case 275 : if (DEBUG) { System.out.println("StatementExpressionList ::= StatementExpressionList..."); }  //$NON-NLS-1$
		    consumeStatementExpressionList() ;  
			break;
 
    case 276 : if (DEBUG) { System.out.println("AssertStatement ::= assert Expression SEMICOLON"); }  //$NON-NLS-1$
		    consumeSimpleAssertStatement() ;  
			break;
 
    case 277 : if (DEBUG) { System.out.println("AssertStatement ::= assert Expression COLON Expression"); }  //$NON-NLS-1$
		    consumeAssertStatement() ;  
			break;
 
    case 278 : if (DEBUG) { System.out.println("BreakStatement ::= break SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementBreak() ;  
			break;
 
    case 279 : if (DEBUG) { System.out.println("BreakStatement ::= break Identifier SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementBreakWithLabel() ;  
			break;
 
    case 280 : if (DEBUG) { System.out.println("ContinueStatement ::= continue SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementContinue() ;  
			break;
 
    case 281 : if (DEBUG) { System.out.println("ContinueStatement ::= continue Identifier SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementContinueWithLabel() ;  
			break;
 
    case 282 : if (DEBUG) { System.out.println("ReturnStatement ::= return Expressionopt SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementReturn() ;  
			break;
 
    case 283 : if (DEBUG) { System.out.println("ThrowStatement ::= throw Expression SEMICOLON"); }  //$NON-NLS-1$
		    consumeStatementThrow();  
			break;
 
    case 284 : if (DEBUG) { System.out.println("SynchronizedStatement ::= OnlySynchronized LPAREN..."); }  //$NON-NLS-1$
		    consumeStatementSynchronized();  
			break;
 
    case 285 : if (DEBUG) { System.out.println("OnlySynchronized ::= synchronized"); }  //$NON-NLS-1$
		    consumeOnlySynchronized();  
			break;
 
    case 286 : if (DEBUG) { System.out.println("TryStatement ::= try Block Catches"); }  //$NON-NLS-1$
		    consumeStatementTry(false);  
			break;
 
    case 287 : if (DEBUG) { System.out.println("TryStatement ::= try Block Catchesopt Finally"); }  //$NON-NLS-1$
		    consumeStatementTry(true);  
			break;
 
    case 289 : if (DEBUG) { System.out.println("Catches ::= Catches CatchClause"); }  //$NON-NLS-1$
		    consumeCatches();  
			break;
 
    case 290 : if (DEBUG) { System.out.println("CatchClause ::= catch LPAREN FormalParameter RPAREN..."); }  //$NON-NLS-1$
		    consumeStatementCatch() ;  
			break;
 
    case 292 : if (DEBUG) { System.out.println("PushLPAREN ::= LPAREN"); }  //$NON-NLS-1$
		    consumeLeftParen();  
			break;
 
    case 293 : if (DEBUG) { System.out.println("PushRPAREN ::= RPAREN"); }  //$NON-NLS-1$
		    consumeRightParen();  
			break;
 
    case 298 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= this"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayThis();  
			break;
 
    case 299 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PushLPAREN Expression_NotName..."); }  //$NON-NLS-1$
		    consumePrimaryNoNewArray();  
			break;
 
    case 300 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PushLPAREN Name PushRPAREN"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayWithName();  
			break;
 
    case 303 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT this"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayNameThis();  
			break;
 
    case 304 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT super"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayNameSuper();  
			break;
 
    case 305 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name DOT class"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayName();  
			break;
 
    case 306 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= Name Dims DOT class"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayArrayType();  
			break;
 
    case 307 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PrimitiveType Dims DOT class"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayPrimitiveArrayType();  
			break;
 
    case 308 : if (DEBUG) { System.out.println("PrimaryNoNewArray ::= PrimitiveType DOT class"); }  //$NON-NLS-1$
		    consumePrimaryNoNewArrayPrimitiveType();  
			break;
 
    case 311 : if (DEBUG) { System.out.println("AllocationHeader ::= new ClassType LPAREN..."); }  //$NON-NLS-1$
		    consumeAllocationHeader();  
			break;
 
    case 312 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= new..."); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionWithTypeArguments();  
			break;
 
    case 313 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= new ClassType LPAREN"); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpression();  
			break;
 
    case 314 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= Primary DOT new..."); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() ;  
			break;
 
    case 315 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::= Primary DOT new..."); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionQualified() ;  
			break;
 
    case 316 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::=..."); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionQualified() ;  
			break;
 
    case 317 : if (DEBUG) { System.out.println("ClassInstanceCreationExpression ::=..."); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionQualifiedWithTypeArguments() ;  
			break;
 
    case 318 : if (DEBUG) { System.out.println("ClassInstanceCreationExpressionName ::= Name DOT"); }  //$NON-NLS-1$
		    consumeClassInstanceCreationExpressionName() ;  
			break;
 
    case 319 : if (DEBUG) { System.out.println("ClassBodyopt ::="); }  //$NON-NLS-1$
		    consumeClassBodyopt();  
			break;
 
    case 321 : if (DEBUG) { System.out.println("ClassBodySimpleNameopt ::="); }  //$NON-NLS-1$
		    consumeClassBodyopt();  
			break;
 
    case 323 : if (DEBUG) { System.out.println("EnterAnonymousClassBodySimpleName ::="); }  //$NON-NLS-1$
		    consumeEnterAnonymousClassBodySimpleName();  
			break;
 
    case 324 : if (DEBUG) { System.out.println("EnterAnonymousClassBody ::="); }  //$NON-NLS-1$
		    consumeEnterAnonymousClassBody();  
			break;
 
    case 326 : if (DEBUG) { System.out.println("ArgumentList ::= ArgumentList COMMA Expression"); }  //$NON-NLS-1$
		    consumeArgumentList();  
			break;
 
    case 327 : if (DEBUG) { System.out.println("ArrayCreationHeader ::= new PrimitiveType..."); }  //$NON-NLS-1$
		    consumeArrayCreationHeader();  
			break;
 
    case 328 : if (DEBUG) { System.out.println("ArrayCreationHeader ::= new ClassOrInterfaceType..."); }  //$NON-NLS-1$
		    consumeArrayCreationHeader();  
			break;
 
    case 329 : if (DEBUG) { System.out.println("ArrayCreationWithoutArrayInitializer ::= new..."); }  //$NON-NLS-1$
		    consumeArrayCreationExpressionWithoutInitializer();  
			break;
 
    case 330 : if (DEBUG) { System.out.println("ArrayCreationWithArrayInitializer ::= new PrimitiveType"); }  //$NON-NLS-1$
		    consumeArrayCreationExpressionWithInitializer();  
			break;
 
    case 331 : if (DEBUG) { System.out.println("ArrayCreationWithoutArrayInitializer ::= new..."); }  //$NON-NLS-1$
		    consumeArrayCreationExpressionWithoutInitializer();  
			break;
 
    case 332 : if (DEBUG) { System.out.println("ArrayCreationWithArrayInitializer ::= new..."); }  //$NON-NLS-1$
		    consumeArrayCreationExpressionWithInitializer();  
			break;
 
    case 334 : if (DEBUG) { System.out.println("DimWithOrWithOutExprs ::= DimWithOrWithOutExprs..."); }  //$NON-NLS-1$
		    consumeDimWithOrWithOutExprs();  
			break;
 
     case 336 : if (DEBUG) { System.out.println("DimWithOrWithOutExpr ::= LBRACKET RBRACKET"); }  //$NON-NLS-1$
		    consumeDimWithOrWithOutExpr();  
			break;
 
     case 337 : if (DEBUG) { System.out.println("Dims ::= DimsLoop"); }  //$NON-NLS-1$
		    consumeDims();  
			break;
 
     case 340 : if (DEBUG) { System.out.println("OneDimLoop ::= LBRACKET RBRACKET"); }  //$NON-NLS-1$
		    consumeOneDimLoop();  
			break;
 
    case 341 : if (DEBUG) { System.out.println("FieldAccess ::= Primary DOT Identifier"); }  //$NON-NLS-1$
		    consumeFieldAccess(false);  
			break;
 
    case 342 : if (DEBUG) { System.out.println("FieldAccess ::= super DOT Identifier"); }  //$NON-NLS-1$
		    consumeFieldAccess(true);  
			break;
 
    case 343 : if (DEBUG) { System.out.println("MethodInvocation ::= Name LPAREN ArgumentListopt RPAREN"); }  //$NON-NLS-1$
		    consumeMethodInvocationName();  
			break;
 
    case 344 : if (DEBUG) { System.out.println("MethodInvocation ::= Name DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeMethodInvocationNameWithTypeArguments();  
			break;
 
    case 345 : if (DEBUG) { System.out.println("MethodInvocation ::= Primary DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeMethodInvocationPrimaryWithTypeArguments();  
			break;
 
    case 346 : if (DEBUG) { System.out.println("MethodInvocation ::= Primary DOT Identifier LPAREN..."); }  //$NON-NLS-1$
		    consumeMethodInvocationPrimary();  
			break;
 
    case 347 : if (DEBUG) { System.out.println("MethodInvocation ::= super DOT OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeMethodInvocationSuperWithTypeArguments();  
			break;
 
    case 348 : if (DEBUG) { System.out.println("MethodInvocation ::= super DOT Identifier LPAREN..."); }  //$NON-NLS-1$
		    consumeMethodInvocationSuper();  
			break;
 
    case 349 : if (DEBUG) { System.out.println("ArrayAccess ::= Name LBRACKET Expression RBRACKET"); }  //$NON-NLS-1$
		    consumeArrayAccess(true);  
			break;
 
    case 350 : if (DEBUG) { System.out.println("ArrayAccess ::= PrimaryNoNewArray LBRACKET Expression..."); }  //$NON-NLS-1$
		    consumeArrayAccess(false);  
			break;
 
    case 351 : if (DEBUG) { System.out.println("ArrayAccess ::= ArrayCreationWithArrayInitializer..."); }  //$NON-NLS-1$
		    consumeArrayAccess(false);  
			break;
 
    case 353 : if (DEBUG) { System.out.println("PostfixExpression ::= Name"); }  //$NON-NLS-1$
		    consumePostfixExpression();  
			break;
 
    case 356 : if (DEBUG) { System.out.println("PostIncrementExpression ::= PostfixExpression PLUS_PLUS"); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.PLUS,true);  
			break;
 
    case 357 : if (DEBUG) { System.out.println("PostDecrementExpression ::= PostfixExpression..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.MINUS,true);  
			break;
 
    case 358 : if (DEBUG) { System.out.println("PushPosition ::="); }  //$NON-NLS-1$
		    consumePushPosition();  
			break;
 
    case 361 : if (DEBUG) { System.out.println("UnaryExpression ::= PLUS PushPosition UnaryExpression"); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.PLUS);  
			break;
 
    case 362 : if (DEBUG) { System.out.println("UnaryExpression ::= MINUS PushPosition UnaryExpression"); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.MINUS);  
			break;
 
    case 364 : if (DEBUG) { System.out.println("PreIncrementExpression ::= PLUS_PLUS PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.PLUS,false);  
			break;
 
    case 365 : if (DEBUG) { System.out.println("PreDecrementExpression ::= MINUS_MINUS PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.MINUS,false);  
			break;
 
    case 367 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus ::= TWIDDLE PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.TWIDDLE);  
			break;
 
    case 368 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus ::= NOT PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.NOT);  
			break;
 
    case 370 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN PrimitiveType Dimsopt..."); }  //$NON-NLS-1$
		    consumeCastExpressionWithPrimitiveType();  
			break;
 
    case 371 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeCastExpressionWithGenericsArray();  
			break;
 
    case 372 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name OnlyTypeArguments..."); }  //$NON-NLS-1$
		    consumeCastExpressionWithGenerics();  
			break;
 
    case 373 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name OnlyTypeArguments DOT"); }  //$NON-NLS-1$
		    consumeCastExpressionWithQualifiedGenerics();  
			break;
 
    case 374 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name OnlyTypeArguments DOT"); }  //$NON-NLS-1$
		    consumeCastExpressionWithQualifiedGenericsArray();  
			break;
 
    case 375 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name PushRPAREN..."); }  //$NON-NLS-1$
		    consumeCastExpressionLL1();  
			break;
 
    case 376 : if (DEBUG) { System.out.println("CastExpression ::= PushLPAREN Name Dims PushRPAREN..."); }  //$NON-NLS-1$
		    consumeCastExpressionWithNameArray();  
			break;
 
    case 377 : if (DEBUG) { System.out.println("InsideCastExpression ::="); }  //$NON-NLS-1$
		    consumeInsideCastExpression();  
			break;
 
    case 378 : if (DEBUG) { System.out.println("InsideCastExpressionLL1 ::="); }  //$NON-NLS-1$
		    consumeInsideCastExpressionLL1();  
			break;
 
    case 380 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.MULTIPLY);  
			break;
 
    case 381 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.DIVIDE);  
			break;
 
    case 382 : if (DEBUG) { System.out.println("MultiplicativeExpression ::= MultiplicativeExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.REMAINDER);  
			break;
 
    case 384 : if (DEBUG) { System.out.println("AdditiveExpression ::= AdditiveExpression PLUS..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.PLUS);  
			break;
 
    case 385 : if (DEBUG) { System.out.println("AdditiveExpression ::= AdditiveExpression MINUS..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.MINUS);  
			break;
 
    case 387 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression LEFT_SHIFT..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LEFT_SHIFT);  
			break;
 
    case 388 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression RIGHT_SHIFT..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.RIGHT_SHIFT);  
			break;
 
    case 389 : if (DEBUG) { System.out.println("ShiftExpression ::= ShiftExpression UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
			break;
 
    case 391 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression LESS..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LESS);  
			break;
 
    case 392 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression GREATER..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.GREATER);  
			break;
 
    case 393 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression LESS_EQUAL"); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LESS_EQUAL);  
			break;
 
    case 394 : if (DEBUG) { System.out.println("RelationalExpression ::= RelationalExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.GREATER_EQUAL);  
			break;
 
    case 396 : if (DEBUG) { System.out.println("InstanceofExpression ::= InstanceofExpression instanceof"); }  //$NON-NLS-1$
		    consumeInstanceOfExpression(OperatorIds.INSTANCEOF);  
			break;
 
    case 398 : if (DEBUG) { System.out.println("EqualityExpression ::= EqualityExpression EQUAL_EQUAL..."); }  //$NON-NLS-1$
		    consumeEqualityExpression(OperatorIds.EQUAL_EQUAL);  
			break;
 
    case 399 : if (DEBUG) { System.out.println("EqualityExpression ::= EqualityExpression NOT_EQUAL..."); }  //$NON-NLS-1$
		    consumeEqualityExpression(OperatorIds.NOT_EQUAL);  
			break;
 
    case 401 : if (DEBUG) { System.out.println("AndExpression ::= AndExpression AND EqualityExpression"); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.AND);  
			break;
 
    case 403 : if (DEBUG) { System.out.println("ExclusiveOrExpression ::= ExclusiveOrExpression XOR..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.XOR);  
			break;
 
    case 405 : if (DEBUG) { System.out.println("InclusiveOrExpression ::= InclusiveOrExpression OR..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.OR);  
			break;
 
    case 407 : if (DEBUG) { System.out.println("ConditionalAndExpression ::= ConditionalAndExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.AND_AND);  
			break;
 
    case 409 : if (DEBUG) { System.out.println("ConditionalOrExpression ::= ConditionalOrExpression..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.OR_OR);  
			break;
 
    case 411 : if (DEBUG) { System.out.println("ConditionalExpression ::= ConditionalOrExpression..."); }  //$NON-NLS-1$
		    consumeConditionalExpression(OperatorIds.QUESTIONCOLON) ;  
			break;
 
    case 414 : if (DEBUG) { System.out.println("Assignment ::= PostfixExpression AssignmentOperator..."); }  //$NON-NLS-1$
		    consumeAssignment();  
			break;
 
    case 416 : if (DEBUG) { System.out.println("Assignment ::= InvalidArrayInitializerAssignement"); }  //$NON-NLS-1$
		    ignoreExpressionAssignment(); 
			break;
 
    case 417 : if (DEBUG) { System.out.println("AssignmentOperator ::= EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(EQUAL);  
			break;
 
    case 418 : if (DEBUG) { System.out.println("AssignmentOperator ::= MULTIPLY_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(MULTIPLY);  
			break;
 
    case 419 : if (DEBUG) { System.out.println("AssignmentOperator ::= DIVIDE_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(DIVIDE);  
			break;
 
    case 420 : if (DEBUG) { System.out.println("AssignmentOperator ::= REMAINDER_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(REMAINDER);  
			break;
 
    case 421 : if (DEBUG) { System.out.println("AssignmentOperator ::= PLUS_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(PLUS);  
			break;
 
    case 422 : if (DEBUG) { System.out.println("AssignmentOperator ::= MINUS_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(MINUS);  
			break;
 
    case 423 : if (DEBUG) { System.out.println("AssignmentOperator ::= LEFT_SHIFT_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(LEFT_SHIFT);  
			break;
 
    case 424 : if (DEBUG) { System.out.println("AssignmentOperator ::= RIGHT_SHIFT_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(RIGHT_SHIFT);  
			break;
 
    case 425 : if (DEBUG) { System.out.println("AssignmentOperator ::= UNSIGNED_RIGHT_SHIFT_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(UNSIGNED_RIGHT_SHIFT);  
			break;
 
    case 426 : if (DEBUG) { System.out.println("AssignmentOperator ::= AND_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(AND);  
			break;
 
    case 427 : if (DEBUG) { System.out.println("AssignmentOperator ::= XOR_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(XOR);  
			break;
 
    case 428 : if (DEBUG) { System.out.println("AssignmentOperator ::= OR_EQUAL"); }  //$NON-NLS-1$
		    consumeAssignmentOperator(OR);  
			break;
 
    case 434 : if (DEBUG) { System.out.println("Expressionopt ::="); }  //$NON-NLS-1$
		    consumeEmptyExpression();  
			break;
 
    case 439 : if (DEBUG) { System.out.println("ImportDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyImportDeclarationsopt();  
			break;
 
    case 440 : if (DEBUG) { System.out.println("ImportDeclarationsopt ::= ImportDeclarations"); }  //$NON-NLS-1$
		    consumeImportDeclarationsopt();  
			break;
 
    case 441 : if (DEBUG) { System.out.println("TypeDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyTypeDeclarationsopt();  
			break;
 
    case 442 : if (DEBUG) { System.out.println("TypeDeclarationsopt ::= TypeDeclarations"); }  //$NON-NLS-1$
		    consumeTypeDeclarationsopt();  
			break;
 
    case 443 : if (DEBUG) { System.out.println("ClassBodyDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyClassBodyDeclarationsopt();  
			break;
 
    case 444 : if (DEBUG) { System.out.println("ClassBodyDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
		    consumeClassBodyDeclarationsopt();  
			break;
 
     case 445 : if (DEBUG) { System.out.println("Modifiersopt ::="); }  //$NON-NLS-1$
		    consumeDefaultModifiers();  
			break;
 
    case 446 : if (DEBUG) { System.out.println("Modifiersopt ::= Modifiers"); }  //$NON-NLS-1$
		    consumeModifiers();  
			break;
 
    case 447 : if (DEBUG) { System.out.println("BlockStatementsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyBlockStatementsopt();  
			break;
 
     case 449 : if (DEBUG) { System.out.println("Dimsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyDimsopt();  
			break;
 
     case 451 : if (DEBUG) { System.out.println("ArgumentListopt ::="); }  //$NON-NLS-1$
		    consumeEmptyArgumentListopt();  
			break;
 
    case 455 : if (DEBUG) { System.out.println("FormalParameterListopt ::="); }  //$NON-NLS-1$
		    consumeFormalParameterListopt();  
			break;
 
     case 459 : if (DEBUG) { System.out.println("InterfaceMemberDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyInterfaceMemberDeclarationsopt();  
			break;
 
     case 460 : if (DEBUG) { System.out.println("InterfaceMemberDeclarationsopt ::= NestedType..."); }  //$NON-NLS-1$
		    consumeInterfaceMemberDeclarationsopt();  
			break;
 
    case 461 : if (DEBUG) { System.out.println("NestedType ::="); }  //$NON-NLS-1$
		    consumeNestedType();  
			break;

     case 462 : if (DEBUG) { System.out.println("ForInitopt ::="); }  //$NON-NLS-1$
		    consumeEmptyForInitopt();  
			break;
 
     case 464 : if (DEBUG) { System.out.println("ForUpdateopt ::="); }  //$NON-NLS-1$
		    consumeEmptyForUpdateopt();  
			break;
 
     case 468 : if (DEBUG) { System.out.println("Catchesopt ::="); }  //$NON-NLS-1$
		    consumeEmptyCatchesopt();  
			break;
 
     case 470 : if (DEBUG) { System.out.println("EnumDeclaration ::= EnumHeader ClassHeaderImplementsopt"); }  //$NON-NLS-1$
		    consumeEnumDeclaration();  
			break;
 
     case 471 : if (DEBUG) { System.out.println("EnumHeader ::= Modifiersopt enum Identifier"); }  //$NON-NLS-1$
		    consumeEnumHeader();  
			break;
 
     case 472 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumBodyDeclarationsopt RBRACE"); }  //$NON-NLS-1$
		    consumeEnumBodyNoConstants();  
			break;
 
     case 473 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE COMMA EnumBodyDeclarationsopt..."); }  //$NON-NLS-1$
		    consumeEnumBodyNoConstants();  
			break;
 
     case 474 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumConstants COMMA..."); }  //$NON-NLS-1$
		    consumeEnumBodyWithConstants();  
			break;
 
     case 475 : if (DEBUG) { System.out.println("EnumBody ::= LBRACE EnumConstants..."); }  //$NON-NLS-1$
		    consumeEnumBodyWithConstants();  
			break;
 
    case 477 : if (DEBUG) { System.out.println("EnumConstants ::= EnumConstants COMMA EnumConstant"); }  //$NON-NLS-1$
		    consumeEnumConstants();  
			break;
 
    case 478 : if (DEBUG) { System.out.println("EnumConstant ::= Identifier Argumentsopt ClassBody"); }  //$NON-NLS-1$
		    consumeEnumConstantWithClassBody();  
			break;
 
    case 479 : if (DEBUG) { System.out.println("EnumConstant ::= Identifier Argumentsopt"); }  //$NON-NLS-1$
		    consumeEnumConstantNoClassBody();  
			break;
 
    case 480 : if (DEBUG) { System.out.println("Arguments ::= LPAREN ArgumentListopt RPAREN"); }  //$NON-NLS-1$
		    consumeArguments();  
			break;
 
    case 481 : if (DEBUG) { System.out.println("Argumentsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyArguments();  
			break;
 
    case 484 : if (DEBUG) { System.out.println("EnumBodyDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyEnumDeclarations();  
			break;
 
    case 486 : if (DEBUG) { System.out.println("EnhancedForStatement ::= for LPAREN Type PushModifiers"); }  //$NON-NLS-1$
		    consumeEnhancedForStatement();  
			break;
 
    case 487 : if (DEBUG) { System.out.println("EnhancedForStatementNoShortIf ::= for LPAREN Type..."); }  //$NON-NLS-1$
		    consumeEnhancedForStatement();  
			break;
 
    case 488 : if (DEBUG) { System.out.println("SingleStaticImportDeclaration ::=..."); }  //$NON-NLS-1$
		    consumeImportDeclaration();  
			break;
 
    case 489 : if (DEBUG) { System.out.println("SingleStaticImportDeclarationName ::= import static Name"); }  //$NON-NLS-1$
		    consumeSingleStaticImportDeclarationName();  
			break;
 
    case 490 : if (DEBUG) { System.out.println("StaticImportOnDemandDeclaration ::=..."); }  //$NON-NLS-1$
		    consumeImportDeclaration();  
			break;
 
    case 491 : if (DEBUG) { System.out.println("StaticImportOnDemandDeclarationName ::= import static..."); }  //$NON-NLS-1$
		    consumeStaticImportOnDemandDeclarationName();  
			break;
 
    case 492 : if (DEBUG) { System.out.println("TypeArguments ::= LESS TypeArgumentList1"); }  //$NON-NLS-1$
		    consumeTypeArguments();  
			break;
 
    case 493 : if (DEBUG) { System.out.println("OnlyTypeArguments ::= LESS TypeArgumentList1"); }  //$NON-NLS-1$
		    consumeOnlyTypeArguments();  
			break;
 
    case 495 : if (DEBUG) { System.out.println("TypeArgumentList1 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
		    consumeTypeArgumentList1();  
			break;
 
    case 497 : if (DEBUG) { System.out.println("TypeArgumentList ::= TypeArgumentList COMMA TypeArgument"); }  //$NON-NLS-1$
		    consumeTypeArgumentList();  
			break;
 
    case 498 : if (DEBUG) { System.out.println("TypeArgument ::= ReferenceType"); }  //$NON-NLS-1$
		    consumeTypeArgument();  
			break;
 
    case 502 : if (DEBUG) { System.out.println("ReferenceType1 ::= ReferenceType GREATER"); }  //$NON-NLS-1$
		    consumeReferenceType1();  
			break;
 
    case 503 : if (DEBUG) { System.out.println("ReferenceType1 ::= ClassOrInterface LESS..."); }  //$NON-NLS-1$
		    consumeTypeArgumentReferenceType1();  
			break;
 
    case 505 : if (DEBUG) { System.out.println("TypeArgumentList2 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
		    consumeTypeArgumentList2();  
			break;
 
    case 508 : if (DEBUG) { System.out.println("ReferenceType2 ::= ReferenceType RIGHT_SHIFT"); }  //$NON-NLS-1$
		    consumeReferenceType2();  
			break;
 
    case 509 : if (DEBUG) { System.out.println("ReferenceType2 ::= ClassOrInterface LESS..."); }  //$NON-NLS-1$
		    consumeTypeArgumentReferenceType2();  
			break;
 
    case 511 : if (DEBUG) { System.out.println("TypeArgumentList3 ::= TypeArgumentList COMMA..."); }  //$NON-NLS-1$
		    consumeTypeArgumentList3();  
			break;
 
    case 514 : if (DEBUG) { System.out.println("ReferenceType3 ::= ReferenceType UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
		    consumeReferenceType3();  
			break;
 
    case 515 : if (DEBUG) { System.out.println("Wildcard ::= QUESTION"); }  //$NON-NLS-1$
		    consumeWildcard();  
			break;
 
    case 516 : if (DEBUG) { System.out.println("Wildcard ::= QUESTION WildcardBounds"); }  //$NON-NLS-1$
		    consumeWildcardWithBounds();  
			break;
 
    case 517 : if (DEBUG) { System.out.println("WildcardBounds ::= extends ReferenceType"); }  //$NON-NLS-1$
		    consumeWildcardBoundsExtends();  
			break;
 
    case 518 : if (DEBUG) { System.out.println("WildcardBounds ::= super ReferenceType"); }  //$NON-NLS-1$
		    consumeWildcardBoundsSuper();  
			break;
 
    case 519 : if (DEBUG) { System.out.println("Wildcard1 ::= QUESTION GREATER"); }  //$NON-NLS-1$
		    consumeWildcard1();  
			break;
 
    case 520 : if (DEBUG) { System.out.println("Wildcard1 ::= QUESTION WildcardBounds1"); }  //$NON-NLS-1$
		    consumeWildcard1WithBounds();  
			break;
 
    case 521 : if (DEBUG) { System.out.println("WildcardBounds1 ::= extends ReferenceType1"); }  //$NON-NLS-1$
		    consumeWildcardBounds1Extends();  
			break;
 
    case 522 : if (DEBUG) { System.out.println("WildcardBounds1 ::= super ReferenceType1"); }  //$NON-NLS-1$
		    consumeWildcardBounds1Super();  
			break;
 
    case 523 : if (DEBUG) { System.out.println("Wildcard2 ::= QUESTION RIGHT_SHIFT"); }  //$NON-NLS-1$
		    consumeWildcard2();  
			break;
 
    case 524 : if (DEBUG) { System.out.println("Wildcard2 ::= QUESTION WildcardBounds2"); }  //$NON-NLS-1$
		    consumeWildcard2WithBounds();  
			break;
 
    case 525 : if (DEBUG) { System.out.println("WildcardBounds2 ::= extends ReferenceType2"); }  //$NON-NLS-1$
		    consumeWildcardBounds2Extends();  
			break;
 
    case 526 : if (DEBUG) { System.out.println("WildcardBounds2 ::= super ReferenceType2"); }  //$NON-NLS-1$
		    consumeWildcardBounds2Super();  
			break;
 
    case 527 : if (DEBUG) { System.out.println("Wildcard3 ::= QUESTION UNSIGNED_RIGHT_SHIFT"); }  //$NON-NLS-1$
		    consumeWildcard3();  
			break;
 
    case 528 : if (DEBUG) { System.out.println("Wildcard3 ::= QUESTION WildcardBounds3"); }  //$NON-NLS-1$
		    consumeWildcard3WithBounds();  
			break;
 
    case 529 : if (DEBUG) { System.out.println("WildcardBounds3 ::= extends ReferenceType3"); }  //$NON-NLS-1$
		    consumeWildcardBounds3Extends();  
			break;
 
    case 530 : if (DEBUG) { System.out.println("WildcardBounds3 ::= super ReferenceType3"); }  //$NON-NLS-1$
		    consumeWildcardBounds3Super();  
			break;
 
    case 531 : if (DEBUG) { System.out.println("TypeParameters ::= LESS TypeParameterList1"); }  //$NON-NLS-1$
		    consumeTypeParameters();  
			break;
 
    case 533 : if (DEBUG) { System.out.println("TypeParameterList ::= TypeParameterList COMMA..."); }  //$NON-NLS-1$
		    consumeTypeParameterList();  
			break;
 
    case 534 : if (DEBUG) { System.out.println("TypeParameter ::= Identifier"); }  //$NON-NLS-1$
		    consumeTypeParameter();  
			break;
 
    case 535 : if (DEBUG) { System.out.println("TypeParameter ::= Identifier extends ReferenceType"); }  //$NON-NLS-1$
		    consumeTypeParameterWithExtends();  
			break;
 
    case 536 : if (DEBUG) { System.out.println("TypeParameter ::= Identifier extends ReferenceType..."); }  //$NON-NLS-1$
		    consumeTypeParameterWithExtendsAndBounds();  
			break;
 
    case 538 : if (DEBUG) { System.out.println("AdditionalBoundList ::= AdditionalBoundList..."); }  //$NON-NLS-1$
		    consumeAdditionalBoundList();  
			break;
 
    case 539 : if (DEBUG) { System.out.println("AdditionalBound ::= AND ReferenceType"); }  //$NON-NLS-1$
		    consumeAdditionalBound();  
			break;
 
    case 541 : if (DEBUG) { System.out.println("TypeParameterList1 ::= TypeParameterList COMMA..."); }  //$NON-NLS-1$
		    consumeTypeParameterList1();  
			break;
 
    case 542 : if (DEBUG) { System.out.println("TypeParameter1 ::= Identifier GREATER"); }  //$NON-NLS-1$
		    consumeTypeParameter1();  
			break;
 
    case 543 : if (DEBUG) { System.out.println("TypeParameter1 ::= Identifier extends ReferenceType1"); }  //$NON-NLS-1$
		    consumeTypeParameter1WithExtends();  
			break;
 
    case 544 : if (DEBUG) { System.out.println("TypeParameter1 ::= Identifier extends ReferenceType..."); }  //$NON-NLS-1$
		    consumeTypeParameter1WithExtendsAndBounds();  
			break;
 
    case 546 : if (DEBUG) { System.out.println("AdditionalBoundList1 ::= AdditionalBoundList1..."); }  //$NON-NLS-1$
		    consumeAdditionalBoundList1();  
			break;
 
    case 547 : if (DEBUG) { System.out.println("AdditionalBound1 ::= AND ReferenceType1"); }  //$NON-NLS-1$
		    consumeAdditionalBound1();  
			break;
 
    case 553 : if (DEBUG) { System.out.println("UnaryExpression_NotName ::= PLUS PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.PLUS);  
			break;
 
    case 554 : if (DEBUG) { System.out.println("UnaryExpression_NotName ::= MINUS PushPosition..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.MINUS);  
			break;
 
    case 557 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus_NotName ::= TWIDDLE..."); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.TWIDDLE);  
			break;
 
    case 558 : if (DEBUG) { System.out.println("UnaryExpressionNotPlusMinus_NotName ::= NOT PushPosition"); }  //$NON-NLS-1$
		    consumeUnaryExpression(OperatorIds.NOT);  
			break;
 
    case 561 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.MULTIPLY);  
			break;
 
    case 562 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= Name MULTIPLY..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.MULTIPLY);  
			break;
 
    case 563 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.DIVIDE);  
			break;
 
    case 564 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= Name DIVIDE..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.DIVIDE);  
			break;
 
    case 565 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.REMAINDER);  
			break;
 
    case 566 : if (DEBUG) { System.out.println("MultiplicativeExpression_NotName ::= Name REMAINDER..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.REMAINDER);  
			break;
 
    case 568 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.PLUS);  
			break;
 
    case 569 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::= Name PLUS..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.PLUS);  
			break;
 
    case 570 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.MINUS);  
			break;
 
    case 571 : if (DEBUG) { System.out.println("AdditiveExpression_NotName ::= Name MINUS..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.MINUS);  
			break;
 
    case 573 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LEFT_SHIFT);  
			break;
 
    case 574 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= Name LEFT_SHIFT..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.LEFT_SHIFT);  
			break;
 
    case 575 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.RIGHT_SHIFT);  
			break;
 
    case 576 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= Name RIGHT_SHIFT..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.RIGHT_SHIFT);  
			break;
 
    case 577 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= ShiftExpression_NotName..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
			break;
 
    case 578 : if (DEBUG) { System.out.println("ShiftExpression_NotName ::= Name UNSIGNED_RIGHT_SHIFT..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.UNSIGNED_RIGHT_SHIFT);  
			break;
 
    case 580 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= ShiftExpression_NotName"); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LESS);  
			break;
 
    case 581 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= Name LESS..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.LESS);  
			break;
 
    case 582 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= ShiftExpression_NotName"); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.GREATER);  
			break;
 
    case 583 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= Name GREATER..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.GREATER);  
			break;
 
    case 584 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.LESS_EQUAL);  
			break;
 
    case 585 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= Name LESS_EQUAL..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.LESS_EQUAL);  
			break;
 
    case 586 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.GREATER_EQUAL);  
			break;
 
    case 587 : if (DEBUG) { System.out.println("RelationalExpression_NotName ::= Name GREATER_EQUAL..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.GREATER_EQUAL);  
			break;
 
    case 589 : if (DEBUG) { System.out.println("InstanceofExpression_NotName ::= Name instanceof..."); }  //$NON-NLS-1$
		    consumeInstanceOfExpressionWithName(OperatorIds.INSTANCEOF);  
			break;
 
    case 590 : if (DEBUG) { System.out.println("InstanceofExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeInstanceOfExpression(OperatorIds.INSTANCEOF);  
			break;
 
    case 592 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeEqualityExpression(OperatorIds.EQUAL_EQUAL);  
			break;
 
    case 593 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::= Name EQUAL_EQUAL..."); }  //$NON-NLS-1$
		    consumeEqualityExpressionWithName(OperatorIds.EQUAL_EQUAL);  
			break;
 
    case 594 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeEqualityExpression(OperatorIds.NOT_EQUAL);  
			break;
 
    case 595 : if (DEBUG) { System.out.println("EqualityExpression_NotName ::= Name NOT_EQUAL..."); }  //$NON-NLS-1$
		    consumeEqualityExpressionWithName(OperatorIds.NOT_EQUAL);  
			break;
 
    case 597 : if (DEBUG) { System.out.println("AndExpression_NotName ::= AndExpression_NotName AND..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.AND);  
			break;
 
    case 598 : if (DEBUG) { System.out.println("AndExpression_NotName ::= Name AND EqualityExpression"); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.AND);  
			break;
 
    case 600 : if (DEBUG) { System.out.println("ExclusiveOrExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.XOR);  
			break;
 
    case 601 : if (DEBUG) { System.out.println("ExclusiveOrExpression_NotName ::= Name XOR AndExpression"); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.XOR);  
			break;
 
    case 603 : if (DEBUG) { System.out.println("InclusiveOrExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.OR);  
			break;
 
    case 604 : if (DEBUG) { System.out.println("InclusiveOrExpression_NotName ::= Name OR..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.OR);  
			break;
 
    case 606 : if (DEBUG) { System.out.println("ConditionalAndExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.AND_AND);  
			break;
 
    case 607 : if (DEBUG) { System.out.println("ConditionalAndExpression_NotName ::= Name AND_AND..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.AND_AND);  
			break;
 
    case 609 : if (DEBUG) { System.out.println("ConditionalOrExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeBinaryExpression(OperatorIds.OR_OR);  
			break;
 
    case 610 : if (DEBUG) { System.out.println("ConditionalOrExpression_NotName ::= Name OR_OR..."); }  //$NON-NLS-1$
		    consumeBinaryExpressionWithName(OperatorIds.OR_OR);  
			break;
 
    case 612 : if (DEBUG) { System.out.println("ConditionalExpression_NotName ::=..."); }  //$NON-NLS-1$
		    consumeConditionalExpression(OperatorIds.QUESTIONCOLON) ;  
			break;
 
    case 613 : if (DEBUG) { System.out.println("ConditionalExpression_NotName ::= Name QUESTION..."); }  //$NON-NLS-1$
		    consumeConditionalExpressionWithName(OperatorIds.QUESTIONCOLON) ;  
			break;
 
    case 617 : if (DEBUG) { System.out.println("AnnotationTypeDeclaration ::= Modifiersopt AT interface"); }  //$NON-NLS-1$
		    consumeAnnotationTypeDeclaration() ;  
			break;
 
    case 618 : if (DEBUG) { System.out.println("AnnotationTypeBody ::= LBRACE..."); }  //$NON-NLS-1$
		    consumeAnnotationTypeBody() ;  
			break;
 
    case 619 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclarationsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyAnnotationTypeMemberDeclarations() ;  
			break;
 
    case 622 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclarations ::=..."); }  //$NON-NLS-1$
		    consumeAnnotationTypeMemberDeclarations() ;  
			break;
 
    case 623 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclaration ::= Modifiersopt Type..."); }  //$NON-NLS-1$
		    consumeAnnotationTypeMemberDeclaration() ;  
			break;
 
    case 624 : if (DEBUG) { System.out.println("AnnotationTypeMemberDeclaration ::= SEMICOLON"); }  //$NON-NLS-1$
		    consumeEmptyAnnotationTypeMemberDeclaration() ;  
			break;
 
    case 625 : if (DEBUG) { System.out.println("DefaultValueopt ::="); }  //$NON-NLS-1$
		    consumeEmptyDefaultValue() ;  
			break;
 
    case 627 : if (DEBUG) { System.out.println("DefaultValue ::= default MemberValue"); }  //$NON-NLS-1$
		    consumeDefaultValue() ;  
			break;
 
    case 631 : if (DEBUG) { System.out.println("NormalAnnotation ::= AT Name LPAREN MemberValuePairsopt"); }  //$NON-NLS-1$
		    consumeNormalAnnotation() ;  
			break;
 
    case 632 : if (DEBUG) { System.out.println("MemberValuePairsopt ::="); }  //$NON-NLS-1$
		    consumeEmptyMemberValuePairs() ;  
			break;
 
    case 635 : if (DEBUG) { System.out.println("MemberValuePairs ::= MemberValuePairs COMMA..."); }  //$NON-NLS-1$
		    consumeMemberValuePairs() ;  
			break;
 
    case 636 : if (DEBUG) { System.out.println("MemberValuePair ::= SimpleName EQUAL MemberValue"); }  //$NON-NLS-1$
		    consumeMemberValuePair() ;  
			break;
 
    case 640 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::= LBRACE MemberValues..."); }  //$NON-NLS-1$
		    consumeMemberValueArrayInitializer() ;  
			break;
 
    case 641 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::= LBRACE MemberValues..."); }  //$NON-NLS-1$
		    consumeMemberValueArrayInitializer() ;  
			break;
 
    case 642 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::= LBRACE COMMA RBRACE"); }  //$NON-NLS-1$
		    consumeMemberValueArrayInitializer() ;  
			break;
 
    case 643 : if (DEBUG) { System.out.println("MemberValueArrayInitializer ::= LBRACE RBRACE"); }  //$NON-NLS-1$
		    consumeMemberValueArrayInitializer() ;  
			break;
 
    case 645 : if (DEBUG) { System.out.println("MemberValues ::= MemberValues COMMA MemberValue"); }  //$NON-NLS-1$
		    consumeMemberValues() ;  
			break;
 
    case 646 : if (DEBUG) { System.out.println("MarkerAnnotation ::= AT Name"); }  //$NON-NLS-1$
		    consumeMarkerAnnotation() ;  
			break;
 
    case 647 : if (DEBUG) { System.out.println("SingleMemberAnnotation ::= AT Name LPAREN MemberValue..."); }  //$NON-NLS-1$
		    consumeSingleMemberAnnotation() ;  
			break;
 
	}
} 
protected void consumeSimpleAssertStatement() {
	// AssertStatement ::= 'assert' Expression ';'
	expressionLengthPtr--;
	pushOnAstStack(new AssertStatement(expressionStack[expressionPtr--], intStack[intPtr--]));	
}
protected void consumeSingleMemberAnnotation() {
	// TODO Auto-generated method stub	
}
protected void consumeSingleStaticImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' 'static' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, false, AccStatic));

	modifiers = AccDefault;
	modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	
	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeSingleTypeImportDeclarationName() {
	// SingleTypeImportDeclarationName ::= 'import' Name
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, false, AccDefault));
	
	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}
}
protected void consumeStatementBreak() {
	// BreakStatement ::= 'break' ';'
	// break pushs a position on intStack in case there is no label

	pushOnAstStack(new BreakStatement(null, intStack[intPtr--], endPosition));
}
protected void consumeStatementBreakWithLabel() {
	// BreakStatement ::= 'break' Identifier ';'
	// break pushs a position on intStack in case there is no label

	pushOnAstStack(
		new BreakStatement(
			identifierStack[identifierPtr--],
			intStack[intPtr--],
			endPosition)); 
	identifierLengthPtr--;
}
protected void consumeStatementCatch() {
	// CatchClause ::= 'catch' '(' FormalParameter ')'    Block

	//catch are stored directly into the Try
	//has they always comes two by two....
	//we remove one entry from the astlengthPtr.
	//The construction of the try statement must
	//then fetch the catches using  2*i and 2*i + 1

	astLengthPtr--;
	listLength = 0; // reset formalParameter counter (incremented for catch variable)
}
protected void consumeStatementContinue() {
	// ContinueStatement ::= 'continue' ';'
	// continue pushs a position on intStack in case there is no label

	pushOnAstStack(
		new ContinueStatement(
			null,
			intStack[intPtr--],
			endPosition));
}
protected void consumeStatementContinueWithLabel() {
	// ContinueStatement ::= 'continue' Identifier ';'
	// continue pushs a position on intStack in case there is no label

	pushOnAstStack(
		new ContinueStatement(
			identifierStack[identifierPtr--], 
			intStack[intPtr--], 
			endPosition)); 
	identifierLengthPtr--;
}
protected void consumeStatementDo() {
	// DoStatement ::= 'do' Statement 'while' '(' Expression ')' ';'

	//the 'while' pushes a value on intStack that we need to remove
	intPtr--;

	Statement statement = (Statement) astStack[astPtr];
	expressionLengthPtr--;
	astStack[astPtr] = 
		new DoStatement(
			expressionStack[expressionPtr--], 
			statement, 
			intStack[intPtr--], 
			endPosition); 
}
protected void consumeStatementExpressionList() {
	// StatementExpressionList ::= StatementExpressionList ',' StatementExpression
	concatExpressionLists();
}
protected void consumeStatementFor() {
	// ForStatement ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' Statement
	// ForStatementNoShortIf ::= 'for' '(' ForInitopt ';' Expressionopt ';' ForUpdateopt ')' StatementNoShortIf

	int length;
	Expression cond = null;
	Statement[] inits, updates;
	boolean scope = true;

	//statements
	astLengthPtr--;
	Statement statement = (Statement) astStack[astPtr--];

	//updates are on the expresion stack
	if ((length = expressionLengthStack[expressionLengthPtr--]) == 0) {
		updates = null;
	} else {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			updates = new Statement[length], 
			0, 
			length); 
	}

	if (expressionLengthStack[expressionLengthPtr--] != 0)
		cond = expressionStack[expressionPtr--];

	//inits may be on two different stacks
	if ((length = astLengthStack[astLengthPtr--]) == 0) {
		inits = null;
		scope = false;
	} else {
		if (length == -1) { //on expressionStack
			scope = false;
			length = expressionLengthStack[expressionLengthPtr--];
			expressionPtr -= length;
			System.arraycopy(
				expressionStack, 
				expressionPtr + 1, 
				inits = new Statement[length], 
				0, 
				length); 
		} else { //on astStack
			astPtr -= length;
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				inits = new Statement[length], 
				0, 
				length); 
		}
	}
	pushOnAstStack(
		new ForStatement(
			inits, 
			cond, 
			updates, 
			statement, 
			scope, 
			intStack[intPtr--], 
			endStatementPosition)); 
}
protected void consumeStatementIfNoElse() {
	// IfThenStatement ::=  'if' '(' Expression ')' Statement

	//optimize the push/pop
	expressionLengthPtr--;
	Statement thenStatement = (Statement) astStack[astPtr];
	astStack[astPtr] = 
		new IfStatement(
			expressionStack[expressionPtr--], 
			thenStatement, 
			intStack[intPtr--], 
			endStatementPosition); 
}
protected void consumeStatementIfWithElse() {
	// IfThenElseStatement ::=  'if' '(' Expression ')' StatementNoShortIf 'else' Statement
	// IfThenElseStatementNoShortIf ::=  'if' '(' Expression ')' StatementNoShortIf 'else' StatementNoShortIf

	expressionLengthPtr--;

	// optimized {..., Then, Else } ==> {..., If }
	astLengthPtr--;

	//optimize the push/pop
	astStack[--astPtr] = 
		new IfStatement(
			expressionStack[expressionPtr--], 
			(Statement) astStack[astPtr], 
			(Statement) astStack[astPtr + 1], 
			intStack[intPtr--], 
			endStatementPosition); 
}
protected void consumeStatementLabel() {
	// LabeledStatement ::= 'Identifier' ':' Statement
	// LabeledStatementNoShortIf ::= 'Identifier' ':' StatementNoShortIf

	//optimize push/pop
	Statement stmt = (Statement) astStack[astPtr];
	astStack[astPtr] = 
		new LabeledStatement(
			identifierStack[identifierPtr], 
			stmt, 
			(int) (identifierPositionStack[identifierPtr--] >>> 32), 
			endStatementPosition); 
	identifierLengthPtr--;
}
protected void consumeStatementReturn() {
	// ReturnStatement ::= 'return' Expressionopt ';'
	// return pushs a position on intStack in case there is no expression

	if (expressionLengthStack[expressionLengthPtr--] != 0) {
		pushOnAstStack(
			new ReturnStatement(
				expressionStack[expressionPtr--], 
				intStack[intPtr--], 
				endPosition)
		);
	} else {
		pushOnAstStack(new ReturnStatement(null, intStack[intPtr--], endPosition));
	}
}
protected void consumeStatementSwitch() {
	// SwitchStatement ::= 'switch' OpenBlock '(' Expression ')' SwitchBlock

	//OpenBlock just makes the semantic action blockStart()
	//the block is inlined but a scope need to be created
	//if some declaration occurs.

	int length;
	SwitchStatement switchStatement = new SwitchStatement();
	expressionLengthPtr--;
	switchStatement.expression = expressionStack[expressionPtr--];
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(
			astStack, 
			astPtr + 1, 
			switchStatement.statements = new Statement[length], 
			0, 
			length); 
	}
	switchStatement.explicitDeclarations = realBlockStack[realBlockPtr--];
	pushOnAstStack(switchStatement);
	switchStatement.blockStart = intStack[intPtr--];
	switchStatement.sourceStart = intStack[intPtr--];
	switchStatement.sourceEnd = endStatementPosition;
	if (length == 0 && !containsComment(switchStatement.blockStart, switchStatement.sourceEnd)) {
		switchStatement.bits |= ASTNode.UndocumentedEmptyBlockMASK;
	}
}
protected void consumeStatementSynchronized() {
	// SynchronizedStatement ::= OnlySynchronized '(' Expression ')' Block
	//optimize the push/pop

	if (astLengthStack[astLengthPtr] == 0) {
		astLengthStack[astLengthPtr] = 1;
		expressionLengthPtr--;
		astStack[++astPtr] = 
			new SynchronizedStatement(
				expressionStack[expressionPtr--], 
				null, 
				intStack[intPtr--], 
				endStatementPosition); 
	} else {
		expressionLengthPtr--;
		astStack[astPtr] = 
			new SynchronizedStatement(
				expressionStack[expressionPtr--], 
				(Block) astStack[astPtr], 
				intStack[intPtr--], 
				endStatementPosition); 
	}
	resetModifiers();
}
protected void consumeStatementThrow() {
	// ThrowStatement ::= 'throw' Expression ';'
	expressionLengthPtr--;
	pushOnAstStack(new ThrowStatement(expressionStack[expressionPtr--], intStack[intPtr--]));
}
protected void consumeStatementTry(boolean withFinally) {
	//TryStatement ::= 'try'  Block Catches
	//TryStatement ::= 'try'  Block Catchesopt Finally

	int length;
	TryStatement tryStmt = new TryStatement();
	//finally
	if (withFinally) {
		astLengthPtr--;
		tryStmt.finallyBlock = (Block) astStack[astPtr--];
	}
	//catches are handle by two <argument-block> [see statementCatch]
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		if (length == 1) {
			tryStmt.catchBlocks = new Block[] {(Block) astStack[astPtr--]};
			tryStmt.catchArguments = new Argument[] {(Argument) astStack[astPtr--]};
		} else {
			Block[] bks = (tryStmt.catchBlocks = new Block[length]);
			Argument[] args = (tryStmt.catchArguments = new Argument[length]);
			while (length-- > 0) {
				bks[length] = (Block) astStack[astPtr--];
				args[length] = (Argument) astStack[astPtr--];
			}
		}
	}
	//try
	astLengthPtr--;
	tryStmt.tryBlock = (Block) astStack[astPtr--];

	//positions
	tryStmt.sourceEnd = endStatementPosition;
	tryStmt.sourceStart = intStack[intPtr--];
	pushOnAstStack(tryStmt);
}
protected void consumeStatementWhile() {
	// WhileStatement ::= 'while' '(' Expression ')' Statement
	// WhileStatementNoShortIf ::= 'while' '(' Expression ')' StatementNoShortIf

	expressionLengthPtr--;
	Statement statement = (Statement) astStack[astPtr];
	astStack[astPtr] = 
		new WhileStatement(
			expressionStack[expressionPtr--], 
			statement, 
			intStack[intPtr--], 
			endStatementPosition); 
}
protected void consumeStaticImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' 'static' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, AccStatic));

	modifiers = AccDefault;
	modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	
	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumeStaticInitializer() {
	// StaticInitializer ::=  StaticOnly Block
	//push an Initializer
	//optimize the push/pop
	Block block = (Block) astStack[astPtr];
	if (diet) block.bits &= ~ASTNode.UndocumentedEmptyBlockMASK; // clear bit set since was diet
	Initializer initializer = new Initializer(block, AccStatic);
	astStack[astPtr] = initializer;
	initializer.sourceEnd = endStatementPosition;	
	initializer.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
	nestedMethod[nestedType] --;
	initializer.declarationSourceStart = intStack[intPtr--];
	initializer.bodyStart = intStack[intPtr--];
	initializer.bodyEnd = endPosition;
	
	// recovery
	if (currentElement != null){
		lastCheckPoint = initializer.declarationSourceEnd;
		currentElement = currentElement.add(initializer, 0);
		lastIgnoredToken = -1;
	}
}
protected void consumeStaticOnly() {
	// StaticOnly ::= 'static'
	int savedModifiersSourceStart = modifiersSourceStart;
	checkComment(); // might update declaration source start
	if (modifiersSourceStart >= savedModifiersSourceStart) {
		modifiersSourceStart = savedModifiersSourceStart;
	}
	pushOnIntStack(scanner.currentPosition);
	pushOnIntStack(
		modifiersSourceStart >= 0 ? modifiersSourceStart : scanner.startPosition);
	jumpOverMethodBody();
	nestedMethod[nestedType]++;
	resetModifiers();

	// recovery
	if (currentElement != null){
		recoveredStaticInitializerStart = intStack[intPtr]; // remember start position only for static initializers
	}
}
protected void consumeSwitchBlock() {
	// SwitchBlock ::= '{' SwitchBlockStatements SwitchLabels '}'
	concatNodeLists();
}
protected void consumeSwitchBlockStatement() {
	// SwitchBlockStatement ::= SwitchLabels BlockStatements
	concatNodeLists();
}
protected void consumeSwitchBlockStatements() {
	// SwitchBlockStatements ::= SwitchBlockStatements SwitchBlockStatement
	concatNodeLists();
}
protected void consumeSwitchLabels() {
	// SwitchLabels ::= SwitchLabels SwitchLabel
	optimizedConcatNodeLists();
}
protected void consumeToken(int type) {
	/* remember the last consumed value */
	/* try to minimize the number of build values */
	checkNonExternalizedStringLiteral();
//	// clear the commentPtr of the scanner in case we read something different from a modifier
//	switch(type) {
//		case TokenNameabstract :
//		case TokenNamestrictfp :
//		case TokenNamefinal :
//		case TokenNamenative :
//		case TokenNameprivate :
//		case TokenNameprotected :
//		case TokenNamepublic :
//		case TokenNametransient :
//		case TokenNamevolatile :
//		case TokenNamestatic :
//		case TokenNamesynchronized :
//			break;
//		default:
//			scanner.commentPtr = -1;
//	}
	//System.out.println(scanner.toStringAction(type));
	switch (type) {
		case TokenNameIdentifier :
			pushIdentifier();
			if (scanner.useAssertAsAnIndentifier) {
				long positions = identifierPositionStack[identifierPtr];
				problemReporter().useAssertAsAnIdentifier((int) (positions >>> 32), (int) positions);
			}
			break;
		case TokenNameinterface :
			adjustInterfaceModifiers();
			//'class' is pushing two int (positions) on the stack ==> 'interface' needs to do it too....
			pushOnIntStack(scanner.currentPosition - 1);			
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameabstract :
			checkAndSetModifiers(AccAbstract);
			break;
		case TokenNamestrictfp :
			checkAndSetModifiers(AccStrictfp);
			break;
		case TokenNamefinal :
			checkAndSetModifiers(AccFinal);
			break;
		case TokenNamenative :
			checkAndSetModifiers(AccNative);
			break;
		case TokenNameprivate :
			checkAndSetModifiers(AccPrivate);
			break;
		case TokenNameprotected :
			checkAndSetModifiers(AccProtected);
			break;
		case TokenNamepublic :
			checkAndSetModifiers(AccPublic);
			break;
		case TokenNametransient :
			checkAndSetModifiers(AccTransient);
			break;
		case TokenNamevolatile :
			checkAndSetModifiers(AccVolatile);
			break;
		case TokenNamestatic :
			checkAndSetModifiers(AccStatic);
			break;
		case TokenNamesynchronized :
			this.synchronizedBlockSourceStart = scanner.startPosition;	
			checkAndSetModifiers(AccSynchronized);
			break;
			//==============================
		case TokenNamevoid :
			pushIdentifier(-T_void);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);
			break;
			//push a default dimension while void is not part of the primitive
			//declaration baseType and so takes the place of a type without getting into
			//regular type parsing that generates a dimension on intStack
		case TokenNameboolean :
			pushIdentifier(-T_boolean);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);		
			break;
		case TokenNamebyte :
			pushIdentifier(-T_byte);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNamechar :
			pushIdentifier(-T_char);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNamedouble :
			pushIdentifier(-T_double);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNamefloat :
			pushIdentifier(-T_float);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNameint :
			pushIdentifier(-T_int);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNamelong :
			pushIdentifier(-T_long);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
		case TokenNameshort :
			pushIdentifier(-T_short);
			pushOnIntStack(scanner.currentPosition - 1);				
			pushOnIntStack(scanner.startPosition);					
			break;
			//==============================
		case TokenNameIntegerLiteral :
			pushOnExpressionStack(
				new IntLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			break;
		case TokenNameLongLiteral :
			pushOnExpressionStack(
				new LongLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			break;
		case TokenNameFloatingPointLiteral :
			pushOnExpressionStack(
				new FloatLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			break;
		case TokenNameDoubleLiteral :
			pushOnExpressionStack(
				new DoubleLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			break;
		case TokenNameCharacterLiteral :
			pushOnExpressionStack(
				new CharLiteral(
					scanner.getCurrentTokenSource(), 
					scanner.startPosition, 
					scanner.currentPosition - 1)); 
			break;
		case TokenNameStringLiteral :
			StringLiteral stringLiteral = new StringLiteral(
					scanner.getCurrentTokenSourceString(), 
					scanner.startPosition, 
					scanner.currentPosition - 1); 
			pushOnExpressionStack(stringLiteral); 
			break;
		case TokenNamefalse :
			pushOnExpressionStack(
				new FalseLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			break;
		case TokenNametrue :
			pushOnExpressionStack(
				new TrueLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			break;
		case TokenNamenull :
			pushOnExpressionStack(
				new NullLiteral(scanner.startPosition, scanner.currentPosition - 1)); 
			break;
			//============================
		case TokenNamesuper :
		case TokenNamethis :
			endPosition = scanner.currentPosition - 1;
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameassert :
		case TokenNameimport :
		case TokenNamepackage :
		case TokenNamethrow :
		case TokenNamedo :
		case TokenNameif :
		case TokenNamefor :
		case TokenNameswitch :
		case TokenNametry :
		case TokenNamewhile :
		case TokenNamebreak :
		case TokenNamecontinue :
		case TokenNamereturn :
		case TokenNamecase :
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNamenew :
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=40954
			resetModifiers();
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameclass :
			pushOnIntStack(scanner.currentPosition - 1);
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNameenum :
			pushOnIntStack(scanner.currentPosition - 1);
			pushOnIntStack(scanner.startPosition);
			break;
		case TokenNamedefault :
			pushOnIntStack(scanner.startPosition);
			pushOnIntStack(scanner.currentPosition - 1);
			break;
			//let extra semantic action decide when to push
		case TokenNameRBRACKET :
		case TokenNamePLUS :
		case TokenNameMINUS :
		case TokenNameNOT :
		case TokenNameTWIDDLE :
			endPosition = scanner.startPosition;
			break;
		case TokenNamePLUS_PLUS :
		case TokenNameMINUS_MINUS :
			endPosition = scanner.startPosition;
			endStatementPosition = scanner.currentPosition - 1;
			break;
		case TokenNameRBRACE:
		case TokenNameSEMICOLON :
			endStatementPosition = scanner.currentPosition - 1;
			endPosition = scanner.startPosition - 1; 
			//the item is not part of the potential futur expression/statement
			break;
		case TokenNameRPAREN :
			// in order to handle ( expression) ////// (cast)expression///// foo(x)
			rParenPos = scanner.currentPosition - 1; // position of the end of right parenthesis (in case of unicode \u0029) lex00101
			break;
		case TokenNameLPAREN :
			lParenPos = scanner.startPosition;
			break;
			//  case TokenNameQUESTION  :
			//  case TokenNameCOMMA :
			//  case TokenNameCOLON  :
			//  case TokenNameEQUAL  :
			//  case TokenNameLBRACKET  :
			//  case TokenNameDOT :
			//  case TokenNameERROR :
			//  case TokenNameEOF  :
			//  case TokenNamecase  :
			//  case TokenNamecatch  :
			//  case TokenNameelse  :
			//  case TokenNameextends  :
			//  case TokenNamefinally  :
			//  case TokenNameimplements  :
			//  case TokenNamethrows  :
			//  case TokenNameinstanceof  :
			//  case TokenNameEQUAL_EQUAL  :
			//  case TokenNameLESS_EQUAL  :
			//  case TokenNameGREATER_EQUAL  :
			//  case TokenNameNOT_EQUAL  :
			//  case TokenNameLEFT_SHIFT  :
			//  case TokenNameRIGHT_SHIFT  :
			//  case TokenNameUNSIGNED_RIGHT_SHIFT :
			//  case TokenNamePLUS_EQUAL  :
			//  case TokenNameMINUS_EQUAL  :
			//  case TokenNameMULTIPLY_EQUAL  :
			//  case TokenNameDIVIDE_EQUAL  :
			//  case TokenNameAND_EQUAL  :
			//  case TokenNameOR_EQUAL  :
			//  case TokenNameXOR_EQUAL  :
			//  case TokenNameREMAINDER_EQUAL  :
			//  case TokenNameLEFT_SHIFT_EQUAL  :
			//  case TokenNameRIGHT_SHIFT_EQUAL  :
			//  case TokenNameUNSIGNED_RIGHT_SHIFT_EQUAL  :
			//  case TokenNameOR_OR  :
			//  case TokenNameAND_AND  :
			//  case TokenNameREMAINDER :
			//  case TokenNameXOR  :
			//  case TokenNameAND  :
			//  case TokenNameMULTIPLY :
			//  case TokenNameOR  :
			//  case TokenNameDIVIDE :
			//  case TokenNameGREATER  :
			//  case TokenNameLESS  :
	}
}
protected void consumeTypeArgument() {
	pushOnGenericsStack(getTypeReference(intStack[intPtr--]));	
}
protected void consumeTypeArgumentList() {
	concatGenericsLists();
}
protected void consumeTypeArgumentList1() {
	concatGenericsLists();
}
protected void consumeTypeArgumentList2() {
	concatGenericsLists();
}
protected void consumeTypeArgumentList3() {
	concatGenericsLists();
}
protected void consumeTypeArgumentReferenceType1() {
	concatGenericsLists();
	pushOnGenericsStack(getTypeReference(0));	
}
protected void consumeTypeArgumentReferenceType2() {
	concatGenericsLists();
	pushOnGenericsStack(getTypeReference(0));	
}
protected void consumeTypeArguments() {
	concatGenericsLists();
}
protected void consumeTypeDeclarations() {
	// TypeDeclarations ::= TypeDeclarations TypeDeclaration
	concatNodeLists();
}
protected void consumeTypeDeclarationsopt() {
	// TypeDeclarationsopt ::= TypeDeclarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		System.arraycopy(astStack, astPtr + 1, compilationUnit.types = new TypeDeclaration[length], 0, length);
	}
}
protected void consumeTypeImportOnDemandDeclarationName() {
	// TypeImportOnDemandDeclarationName ::= 'import' Name '.' '*'
	/* push an ImportRef build from the last name 
	stored in the identifier stack. */

	ImportReference impt;
	int length;
	char[][] tokens = new char[length = identifierLengthStack[identifierLengthPtr--]][];
	identifierPtr -= length;
	long[] positions = new long[length];
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	pushOnAstStack(impt = new ImportReference(tokens, positions, true, AccDefault));

	if (currentToken == TokenNameSEMICOLON){
		impt.declarationSourceEnd = scanner.currentPosition - 1;
	} else {
		impt.declarationSourceEnd = impt.sourceEnd;
	}
	impt.declarationEnd = impt.declarationSourceEnd;
	//endPosition is just before the ;
	impt.declarationSourceStart = intStack[intPtr--];

	// recovery
	if (currentElement != null){
		lastCheckPoint = impt.declarationSourceEnd+1;
		currentElement = currentElement.add(impt, 0);
		lastIgnoredToken = -1;
		restartRecovery = true; // used to avoid branching back into the regular automaton		
	}	
}
protected void consumeTypeParameter() {
	TypeParameter typeParameter = new TypeParameter();
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	pushOnGenericsStack(typeParameter);
}
protected void consumeTypeParameter1() {
	TypeParameter typeParameter = new TypeParameter();
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	pushOnGenericsStack(typeParameter);
}
protected void consumeTypeParameter1WithExtends() {
	TypeParameter typeParameter = new TypeParameter();
	TypeReference superType = (TypeReference) genericsStack[genericsPtr];
	
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	typeParameter.type = superType;
	genericsStack[genericsPtr] = typeParameter;
}
protected void consumeTypeParameter1WithExtendsAndBounds() {
	TypeParameter typeParameter = new TypeParameter();
	int additionalBoundsLength = genericsLengthStack[genericsLengthPtr--];
	TypeReference[] bounds = new TypeReference[additionalBoundsLength];
	genericsPtr -= additionalBoundsLength;
	System.arraycopy(genericsStack, genericsPtr + 1, bounds, 0, additionalBoundsLength);

	TypeReference superType = getTypeReference(intStack[intPtr--]);
	
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	typeParameter.type = superType;
	typeParameter.bounds = bounds;
	pushOnGenericsStack(typeParameter);
}
protected void consumeTypeParameterList() {
	concatGenericsLists();
}
protected void consumeTypeParameterList1() {
	concatGenericsLists();
}
protected void consumeTypeParameters() {
}
protected void consumeTypeParameterWithExtends() {
	TypeParameter typeParameter = new TypeParameter();
	TypeReference superType = (TypeReference) genericsStack[genericsPtr];
	
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	typeParameter.type = superType;
	genericsStack[genericsPtr] = typeParameter;
}
protected void consumeTypeParameterWithExtendsAndBounds() {
	TypeParameter typeParameter = new TypeParameter();
	int additionalBoundsLength = genericsLengthStack[genericsLengthPtr--];
	TypeReference[] bounds = new TypeReference[additionalBoundsLength];
	genericsPtr -= additionalBoundsLength;
	System.arraycopy(genericsStack, genericsPtr + 1, bounds, 0, additionalBoundsLength);

	TypeReference superType = getTypeReference(intStack[intPtr--]);
	
	long pos = identifierPositionStack[identifierPtr];
	typeParameter.declarationSourceEnd = (int) pos;
	typeParameter.declarationSourceStart = (int) (pos >>> 32);
	typeParameter.name = identifierStack[identifierPtr--];
	identifierLengthPtr--;
	
	typeParameter.type = superType;
	typeParameter.bounds = bounds;
	pushOnGenericsStack(typeParameter);
}
protected void consumeUnaryExpression(int op) {
	// UnaryExpression ::= '+' PushPosition UnaryExpression
	// UnaryExpression ::= '-' PushPosition UnaryExpression
	// UnaryExpressionNotPlusMinus ::= '~' PushPosition UnaryExpression
	// UnaryExpressionNotPlusMinus ::= '!' PushPosition UnaryExpression

	//optimize the push/pop

	//handle manually the -2147483648 while it is not a real
	//computation of an - and 2147483648 (notice that 2147483648
	//is Integer.MAX_VALUE+1.....)
	//Same for -9223372036854775808L ............

	//intStack have the position of the operator

	Expression r, exp = expressionStack[expressionPtr];
	if (op == MINUS) {
		if ((exp instanceof IntLiteral) && (((IntLiteral) exp).mayRepresentMIN_VALUE())) {
			r = expressionStack[expressionPtr] = new IntLiteralMinValue();
		} else {
			if ((exp instanceof LongLiteral) && (((LongLiteral) exp).mayRepresentMIN_VALUE())) {
				r = expressionStack[expressionPtr] = new LongLiteralMinValue();
			} else {
				r = expressionStack[expressionPtr] = new UnaryExpression(exp, op);
			}
		}
	} else {
		r = expressionStack[expressionPtr] = new UnaryExpression(exp, op);
	}
	r.sourceStart = intStack[intPtr--];
	r.sourceEnd = exp.sourceEnd;
}
protected void consumeUnaryExpression(int op, boolean post) {
	// PreIncrementExpression ::= '++' PushPosition UnaryExpression
	// PreDecrementExpression ::= '--' PushPosition UnaryExpression

	// ++ and -- operators
	//optimize the push/pop

	//intStack has the position of the operator when prefix

	Expression leftHandSide = expressionStack[expressionPtr];
	if (leftHandSide instanceof Reference) {
		// ++foo()++ is unvalid 
		if (post) {
			expressionStack[expressionPtr] = 
				new PostfixExpression(
					leftHandSide,
					IntLiteral.One,
					op,
					endStatementPosition); 
		} else {
			expressionStack[expressionPtr] = 
				new PrefixExpression(
					leftHandSide,
					IntLiteral.One,
					op,
					intStack[intPtr--]); 
		}
	} else {
		//the ++ or the -- is NOT taken into account if code gen proceeds
		if (!post) {
			intPtr--;
		}
		problemReporter().invalidUnaryExpression(leftHandSide);
	}
}
protected void consumeVariableDeclarators() {
	// VariableDeclarators ::= VariableDeclarators ',' VariableDeclarator
	optimizedConcatNodeLists();
}
protected void consumeVariableInitializers() {
	// VariableInitializers ::= VariableInitializers ',' VariableInitializer
	concatExpressionLists();
}
protected void consumeWildcard() {
	pushOnGenericsStack(new Wildcard(false));
}
protected void consumeWildcard1() {
	pushOnGenericsStack(new Wildcard(false));
}
protected void consumeWildcard1WithBounds() {
}
protected void consumeWildcard2() {
	pushOnGenericsStack(new Wildcard(false));
}
protected void consumeWildcard2WithBounds() {
}
protected void consumeWildcard3() {
	pushOnGenericsStack(new Wildcard(false));
}
protected void consumeWildcard3WithBounds() {
}
protected void consumeWildcardBounds1Extends() {
	Wildcard wildcard = new Wildcard(false);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBounds1Super() {
	Wildcard wildcard = new Wildcard(true);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	intPtr--; // remove the starting position of the super keyword
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBounds2Extends() {
	Wildcard wildcard = new Wildcard(false);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBounds2Super() {
	Wildcard wildcard = new Wildcard(true);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	intPtr--; // remove the starting position of the super keyword
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBounds3Extends() {
	Wildcard wildcard = new Wildcard(false);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBounds3Super() {
	Wildcard wildcard = new Wildcard(true);
	wildcard.type = (TypeReference) genericsStack[genericsPtr];
	intPtr--; // remove the starting position of the super keyword
	genericsStack[genericsPtr] = wildcard;
}
protected void consumeWildcardBoundsExtends() {
	Wildcard wildcard = new Wildcard(false);
	wildcard.type = getTypeReference(intStack[intPtr--]);
	pushOnGenericsStack(wildcard);
}
protected void consumeWildcardBoundsSuper() {
	Wildcard wildcard = new Wildcard(true);
	wildcard.type = getTypeReference(intStack[intPtr--]);
	pushOnGenericsStack(wildcard);
}
protected void consumeWildcardWithBounds() {
}
/**
 * Given the current comment stack, answer whether some comment is available in a certain exclusive range
 * 
 * @param sourceStart int
 * @param sourceEnd int
 * @return boolean
 */
public boolean containsComment(int sourceStart, int sourceEnd) {
	int iComment = this.scanner.commentPtr;
	for (; iComment >= 0; iComment--) {
		int commentStart = this.scanner.commentStarts[iComment];
		// ignore comments before start
		if (commentStart < sourceStart) continue;
		// ignore comments after end
		if (commentStart > sourceEnd) continue;
		return true;
	}
	return false;
}
public MethodDeclaration convertToMethodDeclaration(ConstructorDeclaration c, CompilationResult compilationResult) {
	MethodDeclaration m = new MethodDeclaration(compilationResult);
	m.sourceStart = c.sourceStart;
	m.sourceEnd = c.sourceEnd;
	m.bodyStart = c.bodyStart;
	m.bodyEnd = c.bodyEnd;
	m.declarationSourceEnd = c.declarationSourceEnd;
	m.declarationSourceStart = c.declarationSourceStart;
	m.selector = c.selector;
	m.statements = c.statements;
	m.modifiers = c.modifiers;
	m.arguments = c.arguments;
	m.thrownExceptions = c.thrownExceptions;
	m.explicitDeclarations = c.explicitDeclarations;
	m.returnType = null;
	return m;
}
protected TypeReference copyDims(TypeReference typeRef, int dim) {
	return typeRef.copyDims(dim);
}
protected FieldDeclaration createFieldDeclaration(char[] fieldDeclarationName, int sourceStart, int sourceEnd) {
	return new FieldDeclaration(fieldDeclarationName, sourceStart, sourceEnd);
}

protected LocalDeclaration createLocalDeclaration(char[] localDeclarationName, int sourceStart, int sourceEnd) {
	return new LocalDeclaration(localDeclarationName, sourceStart, sourceEnd);
}

public CompilationUnitDeclaration dietParse(ICompilationUnit sourceUnit, CompilationResult compilationResult) {

	CompilationUnitDeclaration parsedUnit;
	boolean old = diet;
	try {
		diet = true;
		parsedUnit = parse(sourceUnit, compilationResult);
	}
	finally {
		diet = old;
	}
	return parsedUnit;
}
protected void dispatchDeclarationInto(int length) {
	/* they are length on astStack that should go into
	   methods fields constructors lists of the typeDecl

	   Return if there is a constructor declaration in the methods declaration */
	   
	
	// Looks for the size of each array . 

	if (length == 0)
		return;
	int[] flag = new int[length + 1]; //plus one -- see <HERE>
	int size1 = 0, size2 = 0, size3 = 0, size4 = 0;
	for (int i = length - 1; i >= 0; i--) {
		ASTNode astNode = astStack[astPtr--];
		if (astNode instanceof AbstractMethodDeclaration) {
			//methods and constructors have been regrouped into one single list
			flag[i] = 3;
			size2++;
		} else if (astNode instanceof EnumDeclaration) {
			flag[i] = 2;
			size4++;
		} else if (astNode instanceof TypeDeclaration) {
			flag[i] = 4;
			size3++;
		} else {
			//field
			flag[i] = 1;
			size1++;
		}
	}

	//arrays creation
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	if (size1 != 0) {
		typeDecl.fields = new FieldDeclaration[size1];
	}
	if (size2 != 0) {
		typeDecl.methods = new AbstractMethodDeclaration[size2];
	}
	if (size3 != 0) {
		typeDecl.memberTypes = new TypeDeclaration[size3];
	}
	if (size4 != 0) {
		typeDecl.enumDeclarations = new EnumDeclaration[size4];
	}

	//arrays fill up
	size1 = size2 = size3 = size4 = 0;
	int flagI = flag[0], start = 0;
	int length2;
	for (int end = 0; end <= length; end++) //<HERE> the plus one allows to 
		{
		if (flagI != flag[end]) //treat the last element as a ended flag.....
			{ //array copy
			switch (flagI) {
				case 1 :
					size1 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.fields, 
						size1 - length2, 
						length2); 
					break;
				case 2 :
					size4 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.enumDeclarations, 
						size4 - length2, 
						length2); 
					break;
				case 3 :
					size2 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.methods, 
						size2 - length2, 
						length2); 
					break;
				case 4 :
					size3 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						typeDecl.memberTypes, 
						size3 - length2, 
						length2); 
					break;
			}
			flagI = flag[start = end];
		}
	}

	if (typeDecl.memberTypes != null) {
		for (int i = typeDecl.memberTypes.length - 1; i >= 0; i--) {
			typeDecl.memberTypes[i].enclosingType = typeDecl;
		}
	}
}
protected void dispatchDeclarationInto(EnumConstant enumConstant, int length) {
	/* they are length on astStack that should go into
	   methods fields constructors lists of the typeDecl

	   Return if there is a constructor declaration in the methods declaration */
	   
	
	// Looks for the size of each array . 

	if (length == 0)
		return;
	int[] flag = new int[length + 1]; //plus one -- see <HERE>
	int size1 = 0, size2 = 0, size3 = 0, size4 = 0;
	for (int i = length - 1; i >= 0; i--) {
		ASTNode astNode = astStack[astPtr--];
		if (astNode instanceof AbstractMethodDeclaration) {
			//methods and constructors have been regrouped into one single list
			flag[i] = 3;
			size2++;
		} else if (astNode instanceof EnumDeclaration) {
			flag[i] = 2;
			size4++;
		} else if (astNode instanceof TypeDeclaration) {
			flag[i] = 4;
			size3++;
		} else {
			//field
			flag[i] = 1;
			size1++;
		}
	}

	//arrays creation
	if (size1 != 0) {
		enumConstant.fields = new FieldDeclaration[size1];
	}
	if (size2 != 0) {
		enumConstant.methods = new AbstractMethodDeclaration[size2];
	}
	if (size3 != 0) {
		enumConstant.memberTypes = new TypeDeclaration[size3];
	}
	if (size4 != 0) {
		enumConstant.enumDeclarations = new EnumDeclaration[size4];
	}

	//arrays fill up
	size1 = size2 = size3 = size4 = 0;
	int flagI = flag[0], start = 0;
	int length2;
	for (int end = 0; end <= length; end++) //<HERE> the plus one allows to 
		{
		if (flagI != flag[end]) //treat the last element as a ended flag.....
			{ //array copy
			switch (flagI) {
				case 1 :
					size1 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumConstant.fields, 
						size1 - length2, 
						length2); 
					break;
				case 2 :
					size4 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumConstant.enumDeclarations, 
						size4 - length2, 
						length2); 
					break;
				case 3 :
					size2 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumConstant.methods, 
						size2 - length2, 
						length2); 
					break;
				case 4 :
					size3 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumConstant.memberTypes, 
						size3 - length2, 
						length2); 
					break;
			}
			flagI = flag[start = end];
		}
	}

	if (enumConstant.memberTypes != null) {
		for (int i = enumConstant.memberTypes.length - 1; i >= 0; i--) {
			enumConstant.memberTypes[i].enclosingType = enumConstant;
		}
	}
}
protected void dispatchDeclarationIntoEnumDeclaration(int length) {
	if (length == 0)
		return;
	int[] flag = new int[length + 1]; //plus one -- see <HERE>
	int size1 = 0, size2 = 0, size3 = 0, size4 = 0;
	for (int i = length - 1; i >= 0; i--) {
		ASTNode astNode = astStack[astPtr--];
		if (astNode instanceof AbstractMethodDeclaration) {
			//methods and constructors have been regrouped into one single list
			flag[i] = 3;
			size2++;
		} else if (astNode instanceof EnumConstant) {
			// enum constants
			flag[i] = 2;
			size4++;
		} else if (astNode instanceof TypeDeclaration) {
			flag[i] = 4;
			size3++;
		} else if (astNode instanceof FieldDeclaration) {
			flag[i] = 1;
			size1++;
		}
	}

	//arrays creation
	EnumDeclaration enumDeclaration = (EnumDeclaration) astStack[astPtr];
	if (size1 != 0) {
		enumDeclaration.fields = new FieldDeclaration[size1];
	}
	if (size2 != 0) {
		enumDeclaration.methods = new AbstractMethodDeclaration[size2];
	}
	if (size3 != 0) {
		enumDeclaration.memberTypes = new TypeDeclaration[size3];
	}
	if (size4 != 0) {
		enumDeclaration.enumConstants = new EnumConstant[size4];
	}

	//arrays fill up
	size1 = size2 = size3 = size4 = 0;
	int flagI = flag[0], start = 0;
	int length2;
	for (int end = 0; end <= length; end++) //<HERE> the plus one allows to 
		{
		if (flagI != flag[end]) //treat the last element as a ended flag.....
			{ //array copy
			switch (flagI) {
				case 1 :
					size1 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumDeclaration.fields, 
						size1 - length2, 
						length2); 
					break;
				case 2 :
					size4 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumDeclaration.enumConstants, 
						size4 - length2, 
						length2); 
					break;
				case 3 :
					size2 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumDeclaration.methods, 
						size2 - length2, 
						length2); 
					break;
				case 4 :
					size3 += (length2 = end - start);
					System.arraycopy(
						astStack, 
						astPtr + start + 1, 
						enumDeclaration.memberTypes, 
						size3 - length2, 
						length2); 
					break;
			}
			flagI = flag[start = end];
		}
	}

	if (enumDeclaration.memberTypes != null) {
		for (int i = enumDeclaration.memberTypes.length - 1; i >= 0; i--) {
			enumDeclaration.memberTypes[i].enclosingType = enumDeclaration;
		}
	}
}
protected CompilationUnitDeclaration endParse(int act) {

	this.lastAct = act;

	if (currentElement != null){
		currentElement.topElement().updateParseTree();
		if (VERBOSE_RECOVERY){
			System.out.print(Util.bind("parser.syntaxRecovery")); //$NON-NLS-1$
			System.out.println("--------------------------");		 //$NON-NLS-1$
			System.out.println(compilationUnit);		
			System.out.println("----------------------------------"); //$NON-NLS-1$
		}		
	} else {
		if (diet & VERBOSE_RECOVERY){
			System.out.print(Util.bind("parser.regularParse"));	 //$NON-NLS-1$
			System.out.println("--------------------------");	 //$NON-NLS-1$
			System.out.println(compilationUnit);		
			System.out.println("----------------------------------"); //$NON-NLS-1$
		}
	}
	if (scanner.recordLineSeparator) {
		compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
	}
	for (int i = 0; i < scanner.foundTaskCount; i++){
		problemReporter().task(
			new String(scanner.foundTaskTags[i]), 
			new String(scanner.foundTaskMessages[i]),
			scanner.foundTaskPriorities[i] == null ? null : new String(scanner.foundTaskPriorities[i]), 
			scanner.foundTaskPositions[i][0], 
			scanner.foundTaskPositions[i][1]);
	}
	return compilationUnit;
}
/*
 * Flush comments defined prior to a given positions.
 *
 * Note: comments are stacked in syntactical order
 *
 * Either answer given <position>, or the end position of a comment line 
 * immediately following the <position> (same line)
 *
 * e.g.
 * void foo(){
 * } // end of method foo
 */
 
public int flushCommentsDefinedPriorTo(int position) {

	int lastCommentIndex = scanner.commentPtr;
	if (lastCommentIndex < 0) return position; // no comment

	// compute the index of the first obsolete comment
	int index = lastCommentIndex;
	int validCount = 0;
	while (index >= 0){
		int commentEnd = scanner.commentStops[index];
		if (commentEnd < 0) commentEnd = -commentEnd; // negative end position for non-javadoc comments
		if (commentEnd <= position){
			break;
		}
		index--;
		validCount++;
	}
	// if the source at <position> is immediately followed by a line comment, then
	// flush this comment and shift <position> to the comment end.
	if (validCount > 0){
		int immediateCommentEnd = -scanner.commentStops[index+1]; //non-javadoc comment end positions are negative
		if (immediateCommentEnd > 0){ // only tolerating non-javadoc comments
			// is there any line break until the end of the immediate comment ? (thus only tolerating line comment)
			immediateCommentEnd--; // comment end in one char too far
			if (scanner.getLineNumber(position) == scanner.getLineNumber(immediateCommentEnd)){
				position = immediateCommentEnd;
				validCount--; // flush this comment
				index++;
			}
		}
	}

	if (index < 0) return position; // no obsolete comment

	if (validCount > 0){ // move valid comment infos, overriding obsolete comment infos
		System.arraycopy(scanner.commentStarts, index + 1, scanner.commentStarts, 0, validCount);
		System.arraycopy(scanner.commentStops, index + 1, scanner.commentStops, 0, validCount);		
	}
	scanner.commentPtr = validCount - 1;
	return position;
}
public final int getFirstToken() {
	// the first token is a virtual token that
	// allows the parser to parse several goals
	// even if they aren't LALR(1)....
	// Goal ::= '++' CompilationUnit
	// Goal ::= '--' MethodBody
	// Goal ::= '==' ConstructorBody
	// -- Initializer
	// Goal ::= '>>' StaticInitializer
	// Goal ::= '>>' Block
	// -- error recovery
	// Goal ::= '>>>' Headers
	// Goal ::= '*' BlockStatements
	// Goal ::= '*' MethodPushModifiersHeader
	// -- JDOM
	// Goal ::= '&&' FieldDeclaration
	// Goal ::= '||' ImportDeclaration
	// Goal ::= '?' PackageDeclaration
	// Goal ::= '+' TypeDeclaration
	// Goal ::= '/' GenericMethodDeclaration
	// Goal ::= '&' ClassBodyDeclaration
	// -- code snippet
	// Goal ::= '%' Expression
	// -- completion parser
	// Goal ::= '!' ConstructorBlockStatementsopt
	// Goal ::= '~' BlockStatementsopt
	
	return firstToken;
}
/*
 * Answer back an array of sourceStart/sourceEnd positions of the available JavaDoc comments.
 * The array is a flattened structure: 2*n entries with consecutives start and end positions.
 *
 * If no JavaDoc is available, then null is answered instead of an empty array.
 *
 * e.g. { 10, 20, 25, 45 }  --> javadoc1 from 10 to 20, javadoc2 from 25 to 45
 */
public int[] getJavaDocPositions() {

	int javadocCount = 0;
	for (int i = 0, max = scanner.commentPtr; i <= max; i++){
		// javadoc only (non javadoc comment have negative end positions.)
		if (scanner.commentStops[i] > 0){
			javadocCount++;
		}
	}
	if (javadocCount == 0) return null;

	int[] positions = new int[2*javadocCount];
	int index = 0;
	for (int i = 0, max = scanner.commentPtr; i <= max; i++){
		// javadoc only (non javadoc comment have negative end positions.)
		if (scanner.commentStops[i] > 0){
			positions[index++] = scanner.commentStarts[i];
			positions[index++] = scanner.commentStops[i]-1; //stop is one over			
		}
	}
	return positions;
}
	public void getMethodBodies(CompilationUnitDeclaration unit) {
		//fill the methods bodies in order for the code to be generated

		if (unit == null) return;
		
		if (unit.ignoreMethodBodies) {
			unit.ignoreFurtherInvestigation = true;
			return;
			// if initial diet parse did not work, no need to dig into method bodies.
		}

		if ((unit.bits & ASTNode.HasAllMethodBodies) != 0)
			return; //work already done ...

		//real parse of the method....
		char[] contents = unit.compilationResult.compilationUnit.getContents();
		this.scanner.setSource(contents);
		
		// save existing values to restore them at the end of the parsing process
		// see bug 47079 for more details
		int[] oldLineEnds = this.scanner.lineEnds;
		int oldLinePtr = this.scanner.linePtr;

		final int[] lineSeparatorPositions = unit.compilationResult.lineSeparatorPositions;
		this.scanner.lineEnds = lineSeparatorPositions;
		this.scanner.linePtr = lineSeparatorPositions.length - 1;

		if (this.javadocParser.checkJavadoc) {
			this.javadocParser.scanner.setSource(contents);
		}
		if (unit.types != null) {
			for (int i = unit.types.length; --i >= 0;)
				unit.types[i].parseMethod(this, unit);
		}
		
		// tag unit has having read bodies
		unit.bits |= ASTNode.HasAllMethodBodies;

		// this is done to prevent any side effects on the compilation unit result
		// line separator positions array.
		this.scanner.lineEnds = oldLineEnds;
		this.scanner.linePtr = oldLinePtr;
	}
protected char getNextCharacter(char[] comment, int[] index) {
	char nextCharacter = comment[index[0]++];
	switch(nextCharacter) {
		case '\\' :
			int c1, c2, c3, c4;
			index[0]++;
			while (comment[index[0]] == 'u') index[0]++;
			if (!(((c1 = Character.getNumericValue(comment[index[0]++])) > 15
				|| c1 < 0)
				|| ((c2 = Character.getNumericValue(comment[index[0]++])) > 15 || c2 < 0)
				|| ((c3 = Character.getNumericValue(comment[index[0]++])) > 15 || c3 < 0)
				|| ((c4 = Character.getNumericValue(comment[index[0]++])) > 15 || c4 < 0))) {
					nextCharacter = (char) (((c1 * 16 + c2) * 16 + c3) * 16 + c4);
			}
			break;
	}
	return nextCharacter;
}
protected Expression getTypeReference(Expression exp) {
	
	exp.bits &= ~ASTNode.RestrictiveFlagMASK;
	exp.bits |= TYPE;
	return exp;
}
protected TypeReference getTypeReference(int dim) {
	/* build a Reference on a variable that may be qualified or not
	 This variable is a type reference and dim will be its dimensions*/

	TypeReference ref;
	int length = identifierLengthStack[identifierLengthPtr--];
	if (length < 0) { //flag for precompiled type reference on base types
		ref = TypeReference.baseTypeReference(-length, dim);
		ref.sourceStart = intStack[intPtr--];
		if (dim == 0) {
			ref.sourceEnd = intStack[intPtr--];
		} else {
			intPtr--;
			ref.sourceEnd = endPosition;
		}
	} else {
		int numberOfIdentifiers = genericsIdentifiersLengthStack[genericsIdentifiersLengthPtr--];
		if (length != numberOfIdentifiers || genericsLengthStack[genericsLengthPtr] != 0) {
			// generic type
			ref = getTypeReferenceForGenericType(dim, length, numberOfIdentifiers);
		} else if (length == 1) {
			// single variable reference
			genericsLengthPtr--; // pop the 0
			if (dim == 0) {
				ref = 
					new SingleTypeReference(
						identifierStack[identifierPtr], 
						identifierPositionStack[identifierPtr--]); 
			} else {
				ref = 
					new ArrayTypeReference(
						identifierStack[identifierPtr], 
						dim, 
						identifierPositionStack[identifierPtr--]); 
				ref.sourceEnd = endPosition;			
			}
		} else {
			genericsLengthPtr--;
			//Qualified variable reference
			char[][] tokens = new char[length][];
			identifierPtr -= length;
			long[] positions = new long[length];
			System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
			System.arraycopy(
				identifierPositionStack, 
				identifierPtr + 1, 
				positions, 
				0, 
				length); 
			if (dim == 0) {
				ref = new QualifiedTypeReference(tokens, positions);
			} else {
				ref = new ArrayQualifiedTypeReference(tokens, dim, positions);
				ref.sourceEnd = endPosition;
			}
		}
	}
	return ref;
}
protected TypeReference getTypeReferenceForGenericType(int dim, int identifierLength, int numberOfIdentifiers) {
	if (identifierLength == 1 && numberOfIdentifiers == 1) {
		int currentTypeArgumentsLength = genericsLengthStack[genericsLengthPtr--];
		TypeReference[] typeArguments = new TypeReference[currentTypeArgumentsLength];
		genericsPtr -= currentTypeArgumentsLength;
		System.arraycopy(genericsStack, genericsPtr + 1, typeArguments, 0, currentTypeArgumentsLength);
		return new SingleParameterizedTypeReference(identifierStack[identifierPtr], typeArguments, dim, identifierPositionStack[identifierPtr--]);
	} else {
		TypeReference[][] typeArguments = new TypeReference[numberOfIdentifiers][];
		char[][] tokens = new char[numberOfIdentifiers][];
		long[] positions = new long[numberOfIdentifiers];
		int index = numberOfIdentifiers;
		int currentIdentifiersLength = identifierLength;
		while (index > 0) {
			int currentTypeArgumentsLength = genericsLengthStack[genericsLengthPtr--];
			if (currentTypeArgumentsLength != 0) {
				genericsPtr -= currentTypeArgumentsLength;
				System.arraycopy(genericsStack, genericsPtr + 1, typeArguments[index - 1] = new TypeReference[currentTypeArgumentsLength], 0, currentTypeArgumentsLength);
			}
			switch(currentIdentifiersLength) {
				case 1 :
					// we are in a case A<B>.C<D> or A<B>.C<D>
					tokens[index - 1] = identifierStack[identifierPtr];
					positions[index - 1] = identifierPositionStack[identifierPtr--];
					break;
				default:
					// we are in a case A.B.C<B>.C<D> or A.B.C<B>...
					identifierPtr -= currentIdentifiersLength;
					System.arraycopy(identifierStack, identifierPtr + 1, tokens, index - currentIdentifiersLength, currentIdentifiersLength);
					System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, index - currentIdentifiersLength, currentIdentifiersLength);
			}
			index -= currentIdentifiersLength;
			if (index > 0) {
				currentIdentifiersLength = identifierLengthStack[identifierLengthPtr--];
			}
		}
		return new QualifiedParameterizedTypeReference(tokens, typeArguments, dim, positions);
	}
}
protected NameReference getUnspecifiedReference() {
	/* build a (unspecified) NameReference which may be qualified*/

	int length;
	NameReference ref;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1)
		// single variable reference
		ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
	else
		//Qualified variable reference
		{
		char[][] tokens = new char[length][];
		identifierPtr -= length;
		System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
		long[] positions = new long[length];
		System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
		ref = 
			new QualifiedNameReference(tokens, 
				positions,
				(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
				(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	}
	return ref;
}
protected NameReference getUnspecifiedReferenceOptimized() {
	/* build a (unspecified) NameReference which may be qualified
	The optimization occurs for qualified reference while we are
	certain in this case the last item of the qualified name is
	a field access. This optimization is IMPORTANT while it results
	that when a NameReference is build, the type checker should always
	look for that it is not a type reference */

	int length;
	NameReference ref;
	if ((length = identifierLengthStack[identifierLengthPtr--]) == 1) {
		// single variable reference
		ref = 
			new SingleNameReference(
				identifierStack[identifierPtr], 
				identifierPositionStack[identifierPtr--]); 
		ref.bits &= ~ASTNode.RestrictiveFlagMASK;
		ref.bits |= LOCAL | FIELD;
		return ref;
	}

	//Qualified-variable-reference
	//In fact it is variable-reference DOT field-ref , but it would result in a type
	//conflict tha can be only reduce by making a superclass (or inetrface ) between
	//nameReference and FiledReference or putting FieldReference under NameReference
	//or else..........This optimisation is not really relevant so just leave as it is

	char[][] tokens = new char[length][];
	identifierPtr -= length;
	System.arraycopy(identifierStack, identifierPtr + 1, tokens, 0, length);
	long[] positions = new long[length];
	System.arraycopy(identifierPositionStack, identifierPtr + 1, positions, 0, length);
	ref = new QualifiedNameReference(
			tokens,
			positions, 
			(int) (identifierPositionStack[identifierPtr + 1] >> 32), // sourceStart
			(int) identifierPositionStack[identifierPtr + length]); // sourceEnd
	ref.bits &= ~ASTNode.RestrictiveFlagMASK;
	ref.bits |= LOCAL | FIELD;
	return ref;
}
public void goForBlockStatementsopt() {
	//tells the scanner to go for block statements opt parsing

	firstToken = TokenNameTWIDDLE;
	scanner.recordLineSeparator = false;
}
public void goForBlockStatementsOrCatchHeader() {
	//tells the scanner to go for block statements or method headers parsing 

	firstToken = TokenNameMULTIPLY;
	scanner.recordLineSeparator = false;
}
public void goForClassBodyDeclarations() {
	//tells the scanner to go for any body declarations parsing

	firstToken = TokenNameAND;
	scanner.recordLineSeparator = true;
}
public void goForCompilationUnit(){
	//tells the scanner to go for compilation unit parsing

	firstToken = TokenNamePLUS_PLUS ;
	scanner.linePtr = -1;	
	scanner.foundTaskCount = 0;
	scanner.recordLineSeparator = true;
	scanner.currentLine= null;
}
public void goForExpression() {
	//tells the scanner to go for an expression parsing

	firstToken = TokenNameREMAINDER;
	scanner.recordLineSeparator = true; // recovery goals must record line separators
}
public void goForFieldDeclaration(){
	//tells the scanner to go for field declaration parsing

	firstToken = TokenNameAND_AND ;
	scanner.recordLineSeparator = true;
}
public void goForGenericMethodDeclaration(){
	//tells the scanner to go for generic method declarations parsing

	firstToken = TokenNameDIVIDE;
	scanner.recordLineSeparator = true;
}
public void goForHeaders(){
	//tells the scanner to go for headers only parsing

	firstToken = TokenNameUNSIGNED_RIGHT_SHIFT;
	scanner.recordLineSeparator = true; // recovery goals must record line separators
}
public void goForImportDeclaration(){
	//tells the scanner to go for import declaration parsing

	firstToken = TokenNameOR_OR ;
	scanner.recordLineSeparator = true;
}
public void goForInitializer(){
	//tells the scanner to go for initializer parsing

	firstToken = TokenNameRIGHT_SHIFT ;
	scanner.recordLineSeparator = false;
}
public void goForMethodBody(){
	//tells the scanner to go for method body parsing

	firstToken = TokenNameMINUS_MINUS ;
	scanner.recordLineSeparator = false;
}
public void goForPackageDeclaration() {
	//tells the scanner to go for package declaration parsing

	firstToken = TokenNameQUESTION;
	scanner.recordLineSeparator = true;
}
public void goForTypeDeclaration() {
	//tells the scanner to go for type (interface or class) declaration parsing

	firstToken = TokenNamePLUS;
	scanner.recordLineSeparator = true;
}
protected void ignoreExpressionAssignment() {
	// Assignment ::= InvalidArrayInitializerAssignement
	// encoded operator would be: intStack[intPtr]
	intPtr--;
	ArrayInitializer arrayInitializer = (ArrayInitializer) expressionStack[expressionPtr--];
	expressionLengthPtr -- ;
	// report a syntax error and abort parsing
	problemReporter().arrayConstantsOnlyInArrayInitializers(arrayInitializer.sourceStart, arrayInitializer.sourceEnd); 	
}
protected void ignoreInterfaceDeclaration() {
	// BlockStatement ::= InvalidInterfaceDeclaration
	//InterfaceDeclaration ::= Modifiersopt 'interface' 'Identifier' ExtendsInterfacesopt InterfaceHeader InterfaceBody

	// length declarations
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		//there are length declarations
		//dispatch according to the type of the declarations
		dispatchDeclarationInto(length);
	}
	
	flushCommentsDefinedPriorTo(endStatementPosition);

	// report the problem and continue parsing
	TypeDeclaration typeDecl = (TypeDeclaration) astStack[astPtr];
	typeDecl.bodyEnd = endStatementPosition;
	problemReporter().cannotDeclareLocalInterface(typeDecl.name, typeDecl.sourceStart, typeDecl.sourceEnd);

	// mark initializers with local type mark if needed
	markInitializersWithLocalType(typeDecl);

	// remove the ast node created in interface header
	astPtr--;	
	// Don't create an astnode for this inner interface, but have to push
	// a 0 on the astLengthStack to be consistent with the reduction made
	// at the end of the method:
	// public void parse(MethodDeclaration md, CompilationUnitDeclaration unit)
	pushOnAstLengthStack(0);
}
protected void ignoreInvalidConstructorDeclaration(boolean hasBody) {
	// InvalidConstructorDeclaration ::= ConstructorHeader ConstructorBody ==> true
	// InvalidConstructorDeclaration ::= ConstructorHeader ';' ==> false

	/*
	astStack : modifiers arguments throws statements
	identifierStack : name
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	*/
	if (hasBody) {
		// pop the position of the {  (body of the method) pushed in block decl
		intPtr--;
	}

	//statements
	if (hasBody) {
		realBlockPtr--;
	}

	int length;
	if (hasBody && ((length = astLengthStack[astLengthPtr--]) != 0)) {
		astPtr -= length;
	}
	ConstructorDeclaration constructorDeclaration = (ConstructorDeclaration) astStack[astPtr];
	constructorDeclaration.bodyEnd = endStatementPosition;
	constructorDeclaration.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);
	if (!hasBody) {
		constructorDeclaration.modifiers |= AccSemicolonBody;
	}
}
protected void ignoreMethodBody() {
	// InterfaceMemberDeclaration ::= InvalidMethodDeclaration

	/*
	astStack : modifiers arguments throws statements
	identifierStack : type name
	intStack : dim dim dim
	 ==>
	astStack : MethodDeclaration
	identifierStack :
	intStack : 
	*/

	// pop the position of the {  (body of the method) pushed in block decl
	intPtr--;
	// retrieve end position of method declarator

	//statements
	realBlockPtr--;
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
	}

	//watch for } that could be given as a unicode ! ( u007D is '}' )
	MethodDeclaration md = (MethodDeclaration) astStack[astPtr];
	md.bodyEnd = endPosition;
	md.declarationSourceEnd = flushCommentsDefinedPriorTo(endStatementPosition);

	// report the problem and continue the parsing - narrowing the problem onto the method
	problemReporter().abstractMethodNeedingNoBody(md);
}
public void initialize() {
	//positionning the parser for a new compilation unit
	//avoiding stack reallocation and all that....
	astPtr = -1;
	astLengthPtr = -1;
	expressionPtr = -1;
	expressionLengthPtr = -1;
	identifierPtr = -1;	
	identifierLengthPtr	= -1;
	intPtr = -1;
	nestedMethod[nestedType = 0] = 0; // need to reset for further reuse
	variablesCounter[nestedType] = 0;
	dimensions = 0 ;
	realBlockPtr = -1;
	compilationUnit = null;
	referenceContext = null;
	endStatementPosition = 0;

	//remove objects from stack too, while the same parser/compiler couple is
	//re-used between two compilations ....
	
	int astLength = astStack.length;
	if (noAstNodes.length < astLength){
		noAstNodes = new ASTNode[astLength];
		//System.out.println("Resized AST stacks : "+ astLength);
		
	}
	System.arraycopy(noAstNodes, 0, astStack, 0, astLength);

	int expressionLength = expressionStack.length;
	if (noExpressions.length < expressionLength){
		noExpressions = new Expression[expressionLength];
		//System.out.println("Resized EXPR stacks : "+ expressionLength);
	}
	System.arraycopy(noExpressions, 0, expressionStack, 0, expressionLength);

	// reset scanner state
	scanner.commentPtr = -1;
	scanner.foundTaskCount = 0;
	scanner.eofPosition = Integer.MAX_VALUE;
	scanner.wasNonExternalizedStringLiteral = false;
	scanner.nonNLSStrings = null;
	scanner.currentLine = null;	

	resetModifiers();

	// recovery
	lastCheckPoint = -1;
	currentElement = null;
	restartRecovery = false;
	hasReportedError = false;
	recoveredStaticInitializerStart = 0;
	lastIgnoredToken = -1;
	lastErrorEndPosition = -1;
	listLength = 0;
}
public void initializeScanner(){
	this.scanner = new Scanner(
		false /*comment*/, 
		false /*whitespace*/, 
		this.options.getSeverity(CompilerOptions.NonExternalizedString) != ProblemSeverities.Ignore /*nls*/, 
		this.options.sourceLevel /*sourceLevel*/, 
		this.options.taskTags/*taskTags*/,
		this.options.taskPriorites/*taskPriorities*/);
}
public final void jumpOverMethodBody() {
	//on diet parsing.....do not buffer method statements

	//the scanner.diet is reinitialized to false
	//automatically by the scanner once it has jumped over
	//the statements

	if (diet && (dietInt == 0))
		scanner.diet = true;
}
protected void markEnclosingMemberWithLocalType() {
	if (this.currentElement != null) return; // this is already done in the recovery code
	for (int i = this.astPtr; i >= 0; i--) {
		ASTNode node = this.astStack[i];
		if (node instanceof AbstractMethodDeclaration 
				|| node instanceof FieldDeclaration
				|| node instanceof TypeDeclaration) { // mark type for now: all initializers will be marked when added to this type
			node.bits |= ASTNode.HasLocalTypeMASK;
			return;
		}
	}
	// default to reference context (case of parse method body)
	if (this.referenceContext instanceof AbstractMethodDeclaration
			|| this.referenceContext instanceof TypeDeclaration) {
		((ASTNode)this.referenceContext).bits |= ASTNode.HasLocalTypeMASK;
	}
}
protected void markInitializersWithLocalType(TypeDeclaration type) {
	if (type.fields == null || (type.bits & ASTNode.HasLocalTypeMASK) == 0) return;
	for (int i = 0, length = type.fields.length; i < length; i++) {
		FieldDeclaration field = type.fields[i];
		if (field instanceof Initializer) {
			field.bits |= ASTNode.HasLocalTypeMASK;
		}
	}
}
/*
 * Move checkpoint location (current implementation is moving it by one token)
 *
 * Answers true if successfully moved checkpoint (in other words, it did not attempt to move it
 * beyond end of file).
 */
protected boolean moveRecoveryCheckpoint() {

	int pos = lastCheckPoint;
	/* reset scanner, and move checkpoint by one token */
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.diet = false; // quit jumping over method bodies
	
	/* if about to restart, then no need to shift token */
	if (restartRecovery){
		lastIgnoredToken = -1;
		scanner.currentLine = null;
		return true;
	}
	
	/* protect against shifting on an invalid token */
	lastIgnoredToken = nextIgnoredToken;
	nextIgnoredToken = -1;
	do {
		try {
			nextIgnoredToken = scanner.getNextToken();
			if(scanner.currentPosition == scanner.startPosition){
				scanner.currentPosition++; // on fake completion identifier
				nextIgnoredToken = -1;
			}
			
		} catch(InvalidInputException e){
			pos = scanner.currentPosition;
		}
	} while (nextIgnoredToken < 0);
	
	if (nextIgnoredToken == TokenNameEOF) { // no more recovery after this point
		if (currentToken == TokenNameEOF) { // already tried one iteration on EOF
			scanner.currentLine = null;
			return false;
		}
	}
	lastCheckPoint = scanner.currentPosition;
	
	/* reset scanner again to previous checkpoint location*/
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.commentPtr = -1;
	scanner.foundTaskCount = 0;
	scanner.currentLine = null;

	return true;

/*
 	The following implementation moves the checkpoint location by one line:
	 
	int pos = lastCheckPoint;
	// reset scanner, and move checkpoint by one token
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.diet = false; // quit jumping over method bodies
	
	// if about to restart, then no need to shift token
	if (restartRecovery){
		lastIgnoredToken = -1;
		return true;
	}
	
	// protect against shifting on an invalid token
	lastIgnoredToken = nextIgnoredToken;
	nextIgnoredToken = -1;
	
	boolean wasTokenizingWhiteSpace = scanner.tokenizeWhiteSpace;
	scanner.tokenizeWhiteSpace = true;
	checkpointMove: 
		do {
			try {
				nextIgnoredToken = scanner.getNextToken();
				switch(nextIgnoredToken){
					case Scanner.TokenNameWHITESPACE :
						if(scanner.getLineNumber(scanner.startPosition)
							== scanner.getLineNumber(scanner.currentPosition)){
							nextIgnoredToken = -1;
							}
						break;
					case TokenNameSEMICOLON :
					case TokenNameLBRACE :
					case TokenNameRBRACE :
						break;
					case TokenNameIdentifier :
						if(scanner.currentPosition == scanner.startPosition){
							scanner.currentPosition++; // on fake completion identifier
						}
					default:						
						nextIgnoredToken = -1;
						break;
					case TokenNameEOF :
						break checkpointMove;
				}
			} catch(InvalidInputException e){
				pos = scanner.currentPosition;
			}
		} while (nextIgnoredToken < 0);
	scanner.tokenizeWhiteSpace = wasTokenizingWhiteSpace;
	
	if (nextIgnoredToken == TokenNameEOF) { // no more recovery after this point
		if (currentToken == TokenNameEOF) { // already tried one iteration on EOF
			return false;
		}
	}
	lastCheckPoint = scanner.currentPosition;
	
	// reset scanner again to previous checkpoint location
	scanner.startPosition = pos;
	scanner.currentPosition = pos;
	scanner.commentPtr = -1;

	return true;
*/
}
protected MessageSend newMessageSend() {
	// '(' ArgumentListopt ')'
	// the arguments are on the expression stack

	MessageSend m = new MessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			m.arguments = new Expression[length], 
			0, 
			length); 
	}
	return m;
}
protected ParameterizedMessageSend newMessageSendWithTypeArguments() {
	ParameterizedMessageSend m = new ParameterizedMessageSend();
	int length;
	if ((length = expressionLengthStack[expressionLengthPtr--]) != 0) {
		expressionPtr -= length;
		System.arraycopy(
			expressionStack, 
			expressionPtr + 1, 
			m.arguments = new Expression[length], 
			0, 
			length); 
	}
	return m;
}
private final void optimizedConcatNodeLists() {
	/*back from a recursive loop. Virtualy group the
	astNode into an array using astLengthStack*/

	/*
	 * This is a case where you have two sublists into the astStack that you want
	 * to merge in one list. There is no action required on the astStack. The only
	 * thing you need to do is merge the two lengths specified on the astStackLength.
	 * The top two length are for example:
	 * ... p   n
	 * and you want to result in a list like:
	 * ... n+p 
	 * This means that the p could be equals to 0 in case there is no astNode pushed
	 * on the astStack.
	 * Look at the InterfaceMemberDeclarations for an example.
	 * This case optimizes the fact that p == 1.
	 */

	astLengthStack[--astLengthPtr]++;
}
/*main loop of the automat
When a rule is reduced, the method consumeRule(int) is called with the number
of the consumed rule. When a terminal is consumed, the method consumeToken(int) is 
called in order to remember (when needed) the consumed token */
// (int)asr[asi(act)]
// name[symbol_index[currentKind]]
protected void parse() {
	boolean isDietParse = diet;
	int oldFirstToken = getFirstToken();
	hasError = false;
	
	hasReportedError = false;
	int act = START_STATE;
	stateStackTop = -1;
	currentToken = getFirstToken();
	ProcessTerminals : for (;;) {
		try {
			stack[++stateStackTop] = act;
		} catch (IndexOutOfBoundsException e) {
			int oldStackLength = stack.length;
			int oldStack[] = stack;
			stack = new int[oldStackLength + StackIncrement];
			System.arraycopy(oldStack, 0, stack, 0, oldStackLength);
			stack[stateStackTop] = act;
		}

		act = tAction(act, currentToken);
		if (act == ERROR_ACTION || restartRecovery) {
			int errorPos = scanner.currentPosition;
			if (!hasReportedError) {
				hasError = true;
			}
			if (resumeOnSyntaxError()) {
				if (act == ERROR_ACTION) {
					lastErrorEndPosition = errorPos;
				}
				act = START_STATE;
				stateStackTop = -1;
				currentToken = getFirstToken();
				continue ProcessTerminals;
			} else {
				act = ERROR_ACTION;
			}	break ProcessTerminals;
		}
		if (act <= NUM_RULES) {
			stateStackTop--;
		} else {
			if (act > ERROR_ACTION) { /* shift-reduce */
				consumeToken(currentToken);
				if (currentElement != null) {
					this.recoveryTokenCheck();
				}
				try {
					currentToken = scanner.getNextToken();
				} catch(InvalidInputException e) {
					if (!hasReportedError) {
						this.problemReporter().scannerError(this, e.getMessage());
						hasReportedError = true;
					}
					lastCheckPoint = scanner.currentPosition;
					restartRecovery = true;
				}					
				act -= ERROR_ACTION;
			} else {
				if (act < ACCEPT_ACTION) { /* shift */
					consumeToken(currentToken);
					if (currentElement != null) {
						this.recoveryTokenCheck();
					}
					try {
						currentToken = scanner.getNextToken();
					} catch(InvalidInputException e) {
						if (!hasReportedError) {
							this.problemReporter().scannerError(this, e.getMessage());
							hasReportedError = true;
						}
						lastCheckPoint = scanner.currentPosition;
						restartRecovery = true;
					}					
					continue ProcessTerminals;
				} else {
					break ProcessTerminals;
				}
			}
		}
			
		ProcessNonTerminals : do { /* reduce */
			consumeRule(act);
			stateStackTop -= (rhs[act] - 1);
			act = ntAction(stack[stateStackTop], lhs[act]);
		} while (act <= NUM_RULES);
	}
	endParse(act);
	
	if(reportSyntaxErrorIsRequired && hasError) {
		reportSyntaxErrors(isDietParse, oldFirstToken);
	}
}
public void parse(ConstructorDeclaration cd, CompilationUnitDeclaration unit) {
	//only parse the method body of cd
	//fill out its statements

	//convert bugs into parse error

	initialize();
	goForBlockStatementsopt();
	nestedMethod[nestedType]++;
	pushOnRealBlockStack(0);
	
	referenceContext = cd;
	compilationUnit = unit;

	scanner.resetTo(cd.bodyStart, cd.bodyEnd);
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}

	checkNonNLSAfterBodyEnd(cd.declarationSourceEnd);
	
	if (lastAct == ERROR_ACTION) {
		initialize();
		return;
	}

	//statements
	cd.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		astPtr -= length;
		if (astStack[astPtr + 1] instanceof ExplicitConstructorCall)
			//avoid a isSomeThing that would only be used here BUT what is faster between two alternatives ?
			{
			System.arraycopy(
				astStack, 
				astPtr + 2, 
				cd.statements = new Statement[length - 1], 
				0, 
				length - 1); 
			cd.constructorCall = (ExplicitConstructorCall) astStack[astPtr + 1];
		} else { //need to add explicitly the super();
			System.arraycopy(
				astStack, 
				astPtr + 1, 
				cd.statements = new Statement[length], 
				0, 
				length); 
			cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		}
	} else {
		cd.constructorCall = SuperReference.implicitSuperConstructorCall();
		if (!containsComment(cd.bodyStart, cd.bodyEnd)) {
			cd.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}		
	}

	if (cd.constructorCall.sourceEnd == 0) {
		cd.constructorCall.sourceEnd = cd.sourceEnd;
		cd.constructorCall.sourceStart = cd.sourceStart;
	}
}
// A P I

public void parse(
	FieldDeclaration field, 
	TypeDeclaration type, 
	CompilationUnitDeclaration unit,
	char[] initializationSource) {
	//only parse the initializationSource of the given field

	//convert bugs into parse error

	initialize();
	goForExpression();
	nestedMethod[nestedType]++;

	referenceContext = type;
	compilationUnit = unit;

	scanner.setSource(initializationSource);
	scanner.resetTo(0, initializationSource.length-1);
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}

	if (lastAct == ERROR_ACTION) {
		return;
	}

	field.initialization = expressionStack[expressionPtr];
	
	// mark field with local type if one was found during parsing
	if ((type.bits & ASTNode.HasLocalTypeMASK) != 0) {
		field.bits |= ASTNode.HasLocalTypeMASK;
	}	
}
// A P I

public CompilationUnitDeclaration parse(
	ICompilationUnit sourceUnit, 
	CompilationResult compilationResult) {
	// parses a compilation unit and manages error handling (even bugs....)

	return parse(sourceUnit, compilationResult, -1, -1/*parse without reseting the scanner*/);
}
// A P I

public CompilationUnitDeclaration parse(
	ICompilationUnit sourceUnit, 
	CompilationResult compilationResult,
	int start,
	int end) {
	// parses a compilation unit and manages error handling (even bugs....)

	CompilationUnitDeclaration unit;
	try {
		/* automaton initialization */
		initialize();
		goForCompilationUnit();

		/* scanners initialization */
		char[] contents = sourceUnit.getContents();
		scanner.setSource(contents);
		if (end != -1) scanner.resetTo(start, end);
		if (this.javadocParser.checkJavadoc) {
			this.javadocParser.scanner.setSource(contents);
			if (end != -1) {
				this.javadocParser.scanner.resetTo(start, end);
			}
		}
		/* unit creation */
		referenceContext = 
			compilationUnit = 
				new CompilationUnitDeclaration(
					problemReporter, 
					compilationResult, 
					scanner.source.length);
		/* run automaton */
		parse();
	} finally {
		unit = compilationUnit;
		compilationUnit = null; // reset parser
		// tag unit has having read bodies
		if (!this.diet) unit.bits |= ASTNode.HasAllMethodBodies;		
	}
	return unit;
}
// A P I

public void parse(
	Initializer initializer, 
	TypeDeclaration type, 
	CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	initialize();
	goForBlockStatementsopt();
	nestedMethod[nestedType]++;
	pushOnRealBlockStack(0);
	
	referenceContext = type;
	compilationUnit = unit;

	scanner.resetTo(initializer.bodyStart, initializer.bodyEnd); // just on the beginning {
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;
	}

	checkNonNLSAfterBodyEnd(initializer.declarationSourceEnd);
	
	if (lastAct == ERROR_ACTION) {
		return;
	}
	
	//refill statements
	initializer.block.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) > 0) {
		System.arraycopy(astStack, (astPtr -= length) + 1, initializer.block.statements = new Statement[length], 0, length); 
	} else {
		// check whether this block at least contains some comment in it
		if (!containsComment(initializer.block.sourceStart, initializer.block.sourceEnd)) {
			initializer.block.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
	}
	
	// mark initializer with local type if one was found during parsing
	if ((type.bits & ASTNode.HasLocalTypeMASK) != 0) {
		initializer.bits |= ASTNode.HasLocalTypeMASK;
	}	
}
// A P I

public void parse(MethodDeclaration md, CompilationUnitDeclaration unit) {
	//only parse the method body of md
	//fill out method statements

	//convert bugs into parse error

	if (md.isAbstract())
		return;
	if (md.isNative())
		return;
	if ((md.modifiers & AccSemicolonBody) != 0)
		return;

	initialize();
	goForBlockStatementsopt();
	nestedMethod[nestedType]++;
	pushOnRealBlockStack(0);

	referenceContext = md;
	compilationUnit = unit;

	scanner.resetTo(md.bodyStart, md.bodyEnd);
	// reset the scanner to parser from { down to }
	try {
		parse();
	} catch (AbortCompilation ex) {
		lastAct = ERROR_ACTION;
	} finally {
		nestedMethod[nestedType]--;		
	}

	checkNonNLSAfterBodyEnd(md.declarationSourceEnd);
	
	if (lastAct == ERROR_ACTION) {
		return;
	}

	//refill statements
	md.explicitDeclarations = realBlockStack[realBlockPtr--];
	int length;
	if ((length = astLengthStack[astLengthPtr--]) != 0) {
		System.arraycopy(
			astStack, 
			(astPtr -= length) + 1, 
			md.statements = new Statement[length], 
			0, 
			length); 
	} else {
		if (!containsComment(md.bodyStart, md.bodyEnd)) {
			md.bits |= ASTNode.UndocumentedEmptyBlockMASK;
		}
	}
}
/**
 * Returns this parser's problem reporter initialized with its reference context.
 * Also it is assumed that a problem is going to be reported, so initializes
 * the compilation result's line positions.
 * 
 * @return ProblemReporter
 */
public ProblemReporter problemReporter(){
	if (scanner.recordLineSeparator) {
		compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
	}
	problemReporter.referenceContext = referenceContext;
	return problemReporter;
}
protected void pushIdentifier() {
	/*push the consumeToken on the identifier stack.
	Increase the total number of identifier in the stack.
	identifierPtr points on the next top */

	try {
		identifierStack[++identifierPtr] = scanner.getCurrentIdentifierSource();
		identifierPositionStack[identifierPtr] = 
			(((long) scanner.startPosition) << 32) + (scanner.currentPosition - 1); 
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallaocation (identifierPtr is correct)---*/
		int oldStackLength = identifierStack.length;
		System.arraycopy(identifierStack, 0, identifierStack = new char[oldStackLength + 20][], 0, oldStackLength);
		identifierStack[identifierPtr] = scanner.getCurrentTokenSource();
		/*identifier position stack*/
		System.arraycopy(identifierPositionStack, 0, identifierPositionStack = new long[oldStackLength + 20], 0, oldStackLength);
		identifierPositionStack[identifierPtr] = 
			(((long) scanner.startPosition) << 32) + (scanner.currentPosition - 1); 
	}

	try {
		identifierLengthStack[++identifierLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallocation (identifierLengthPtr is correct)---*/
		int oldStackLength = identifierLengthStack.length;
		System.arraycopy(identifierLengthStack, 0, identifierLengthStack = new int[oldStackLength + 10], 0, oldStackLength);
		identifierLengthStack[identifierLengthPtr] = 1;
	}

}
protected void pushIdentifier(int flag) {
	/*push a special flag on the stack :
	-zero stands for optional Name
	-negative number for direct ref to base types.
	identifierLengthPtr points on the top */

	try {
		identifierLengthStack[++identifierLengthPtr] = flag;
	} catch (IndexOutOfBoundsException e) {
		/*---stack reallaocation (identifierLengthPtr is correct)---*/
		int oldStackLength = identifierLengthStack.length;
		System.arraycopy(identifierLengthStack, 0, identifierLengthStack = new int[oldStackLength + 10], 0, oldStackLength);
		identifierLengthStack[identifierLengthPtr] = flag;
	}

}
protected void pushOnAstLengthStack(int pos) {
	try {
		astLengthStack[++astLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astLengthStack.length;
		System.arraycopy(astLengthStack, 0, astLengthStack = new int[oldStackLength + StackIncrement], 0, oldStackLength);
		astLengthStack[astLengthPtr] = pos;
	}
}
protected void pushOnAstStack(ASTNode node) {
	/*add a new obj on top of the ast stack
	astPtr points on the top*/

	try {
		astStack[++astPtr] = node;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astStack.length;
		System.arraycopy(astStack, 0, astStack = new ASTNode[oldStackLength + AstStackIncrement], 0, oldStackLength);
		astPtr = oldStackLength;
		astStack[astPtr] = node;
	}

	try {
		astLengthStack[++astLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = astLengthStack.length;
		System.arraycopy(astLengthStack, 0, astLengthStack = new int[oldStackLength + AstStackIncrement], 0, oldStackLength);
		astLengthStack[astLengthPtr] = 1;
	}
}
protected void pushOnExpressionStack(Expression expr) {

	try {
		expressionStack[++expressionPtr] = expr;
	} catch (IndexOutOfBoundsException e) {
		//expressionPtr is correct 
		int oldStackLength = expressionStack.length;
		System.arraycopy(expressionStack, 0, expressionStack  = new Expression[oldStackLength + ExpressionStackIncrement], 0, oldStackLength);
		expressionStack[expressionPtr] = expr;
	}

	try {
		expressionLengthStack[++expressionLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = expressionLengthStack.length;
		System.arraycopy(expressionLengthStack, 0, expressionLengthStack = new int[oldStackLength + ExpressionStackIncrement], 0, oldStackLength);
		expressionLengthStack[expressionLengthPtr] = 1;
	}
}
protected void pushOnExpressionStackLengthStack(int pos) {
	try {
		expressionLengthStack[++expressionLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = expressionLengthStack.length;
		System.arraycopy(expressionLengthStack, 0, expressionLengthStack = new int[oldStackLength + StackIncrement], 0, oldStackLength);
		expressionLengthStack[expressionLengthPtr] = pos;
	}
}
protected void pushOnGenericsStack(ASTNode node) {
	/*add a new obj on top of the generics stack
	genericsPtr points on the top*/

	try {
		genericsStack[++genericsPtr] = node;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = genericsStack.length;
		System.arraycopy(genericsStack, 0, genericsStack = new ASTNode[oldStackLength + GenericsStackIncrement], 0, oldStackLength);
		genericsPtr = oldStackLength;
		genericsStack[genericsPtr] = node;
	}

	try {
		genericsLengthStack[++genericsLengthPtr] = 1;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = genericsLengthStack.length;
		System.arraycopy(genericsLengthStack, 0, genericsLengthStack = new int[oldStackLength + GenericsStackIncrement], 0, oldStackLength);
		genericsLengthStack[genericsLengthPtr] = 1;
	}
}
protected void pushOnGenericsIdentifiersLengthStack(int pos) {
	try {
		genericsIdentifiersLengthStack[++genericsIdentifiersLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = genericsIdentifiersLengthStack.length;
		System.arraycopy(genericsIdentifiersLengthStack, 0, (genericsIdentifiersLengthStack = new int[oldStackLength + GenericsStackIncrement]), 0, oldStackLength);
		genericsIdentifiersLengthStack[genericsIdentifiersLengthPtr] = pos;
	}
}
protected void pushOnGenericsLengthStack(int pos) {
	try {
		genericsLengthStack[++genericsLengthPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		int oldStackLength = genericsLengthStack.length;
		int[] oldPos = genericsLengthStack;
		genericsLengthStack = new int[oldStackLength + GenericsStackIncrement];
		System.arraycopy(oldPos, 0, genericsLengthStack, 0, oldStackLength);
		genericsLengthStack[genericsLengthPtr] = pos;
	}
}
protected void pushOnIntStack(int pos) {
	try {
		intStack[++intPtr] = pos;
	} catch (IndexOutOfBoundsException e) {
		//intPtr is correct 
		int oldStackLength = intStack.length;
		System.arraycopy(intStack, 0, intStack = new int[oldStackLength + StackIncrement], 0, oldStackLength);
		intStack[intPtr] = pos;
	}
}
protected void pushOnRealBlockStack(int i){
	try {
		realBlockStack[++realBlockPtr] = i;
	} catch (IndexOutOfBoundsException e) {
		//realBlockPtr is correct 
		int oldStackLength = realBlockStack.length;
		System.arraycopy(realBlockStack, 0, realBlockStack = new int[oldStackLength + StackIncrement], 0, oldStackLength);
		realBlockStack[realBlockPtr] = i;
	}
}
public void recoveryExitFromVariable() {
	if(currentElement != null && currentElement.parent != null) {
		if(currentElement instanceof RecoveredLocalVariable) {
			
			int end = ((RecoveredLocalVariable)currentElement).localDeclaration.sourceEnd;
			currentElement.updateSourceEndIfNecessary(end);
			currentElement = currentElement.parent;
		} else if(currentElement instanceof RecoveredField
			&& !(currentElement instanceof RecoveredInitializer)) {
				
			int end = ((RecoveredField)currentElement).fieldDeclaration.sourceEnd;
			currentElement.updateSourceEndIfNecessary(end);
			currentElement = currentElement.parent;
		}
	}
}
/* Token check performed on every token shift once having entered
 * recovery mode.
 */
public void recoveryTokenCheck() {
	switch (currentToken) {
		case TokenNameLBRACE : 
			RecoveredElement newElement = null;
			if(!ignoreNextOpeningBrace) {
				newElement = currentElement.updateOnOpeningBrace(scanner.startPosition - 1, scanner.currentPosition - 1);
			}
			lastCheckPoint = scanner.currentPosition;				
			if (newElement != null){ // null means nothing happened
				restartRecovery = true; // opening brace detected
				currentElement = newElement;
			}
			break;
		
		case TokenNameRBRACE : 
			this.rBraceStart = scanner.startPosition - 1;
			this.rBraceEnd = scanner.currentPosition - 1;
			endPosition = this.flushCommentsDefinedPriorTo(this.rBraceEnd);
			newElement =
				currentElement.updateOnClosingBrace(scanner.startPosition, this.rBraceEnd);
				lastCheckPoint = scanner.currentPosition;
			if (newElement != currentElement){
				currentElement = newElement;
			}
			break;
		case TokenNameSEMICOLON :
			endStatementPosition = scanner.currentPosition - 1;
			endPosition = scanner.startPosition - 1; 
			// fall through
		default : {
			if (this.rBraceEnd > this.rBraceSuccessorStart && scanner.currentPosition != scanner.startPosition){
				this.rBraceSuccessorStart = scanner.startPosition;
			}
			break;
		}
	}
	ignoreNextOpeningBrace = false;
}

// A P I
protected void reportSyntaxErrors(boolean isDietParse, int oldFirstToken) {
	if(referenceContext instanceof MethodDeclaration) {
		MethodDeclaration methodDeclaration = (MethodDeclaration) referenceContext;
		if(methodDeclaration.errorInSignature){
			return;
		}
	}
	compilationUnit.compilationResult.lineSeparatorPositions = scanner.getLineEnds();
	scanner.recordLineSeparator = false;
	
	int start = scanner.initialPosition;
	int end = scanner.eofPosition <= Integer.MAX_VALUE ? scanner.eofPosition - 1 : scanner.eofPosition;
	if(isDietParse) {
		TypeDeclaration[] types = this.compilationUnit.types;
		
		int[][] intervalToSkip = org.eclipse.jdt.internal.compiler.parser.diagnose.Util.computeDietRange(types);
		DiagnoseParser diagnoseParser = new DiagnoseParser(this, oldFirstToken, start, end, intervalToSkip[0], intervalToSkip[1], intervalToSkip[2]);
		diagnoseParser.diagnoseParse();
		
		reportSyntaxErrorsForSkippedMethod(types);
		scanner.resetTo(start, end);
	} else {
		DiagnoseParser diagnoseParser = new DiagnoseParser(this, oldFirstToken, start, end);
		diagnoseParser.diagnoseParse();
	}
}
private void reportSyntaxErrorsForSkippedMethod(TypeDeclaration[] types){
	if(types != null) {
		for (int i = 0; i < types.length; i++) {
			TypeDeclaration[] memberTypes = types[i].memberTypes;
			if(memberTypes != null) {
				reportSyntaxErrorsForSkippedMethod(memberTypes);
			}
			
			AbstractMethodDeclaration[] methods = types[i].methods;
			if(methods != null) {
				for (int j = 0; j < methods.length; j++) {
					AbstractMethodDeclaration method = methods[j];
					if(methods[j].errorInSignature) {
						DiagnoseParser diagnoseParser = new DiagnoseParser(this, TokenNameDIVIDE, method.declarationSourceStart, method.declarationSourceEnd);
						diagnoseParser.diagnoseParse();
					}
				}
			}
			
			FieldDeclaration[] fields = types[i].fields;
			if (fields != null) {
				int length = fields.length;
				for (int j = 0; j < length; j++) {
					if (fields[j] instanceof Initializer) {
						Initializer initializer = (Initializer)fields[j];
						if(initializer.errorInSignature){
							DiagnoseParser diagnoseParser = new DiagnoseParser(this, TokenNameRIGHT_SHIFT, initializer.declarationSourceStart, initializer.declarationSourceEnd);
							diagnoseParser.diagnoseParse();
						}
					}
				}
			}
		}
	}
}
protected void resetModifiers() {
	modifiers = AccDefault;
	modifiersSourceStart = -1; // <-- see comment into modifiersFlag(int)
	scanner.commentPtr = -1;
}
/*
 * Reset context so as to resume to regular parse loop
 */
protected void resetStacks() {

	astPtr = -1;
	astLengthPtr = -1;
	expressionPtr = -1;
	expressionLengthPtr = -1;
	identifierPtr = -1;	
	identifierLengthPtr	= -1;
	intPtr = -1;
	nestedMethod[nestedType = 0] = 0; // need to reset for further reuse
	variablesCounter[nestedType] = 0;
	dimensions = 0 ;
	realBlockStack[realBlockPtr = 0] = 0;
	recoveredStaticInitializerStart = 0;
	listLength = 0;
	// Fix for http://dev.eclipse.org/bugs/show_bug.cgi?id=29365
	if (this.scanner != null) this.scanner.currentLine = null;
	
	genericsIdentifiersLengthPtr = -1;
	genericsLengthPtr = -1;
	genericsPtr = -1;
}
/*
 * Reset context so as to resume to regular parse loop
 * If unable to reset for resuming, answers false.
 *
 * Move checkpoint location, reset internal stacks and
 * decide which grammar goal is activated.
 */
protected boolean resumeAfterRecovery() {

	// reset internal stacks 
	this.resetStacks();
	
	/* attempt to move checkpoint location */
	if (!this.moveRecoveryCheckpoint()) {
		return false;
	}

	// only look for headers
	if (referenceContext instanceof CompilationUnitDeclaration){
		goForHeaders();
		diet = true; // passed this point, will not consider method bodies
		return true;
	}
	// does not know how to restart
	return false;
}
/*
 * Syntax error was detected. Will attempt to perform some recovery action in order
 * to resume to the regular parse loop.
 */
protected boolean resumeOnSyntaxError() {

	/* request recovery initialization */
	if (currentElement == null){
		currentElement = 
			this.buildInitialRecoveryState(); // build some recovered elements
	}
	/* do not investigate deeper in recovery when no recovered element */
	if (currentElement == null) return false;
	
	/* manual forced recovery restart - after headers */
	if (restartRecovery){
		restartRecovery = false;
	}
	/* update recovery state with current error state of the parser */
	this.updateRecoveryState();
	
	/* attempt to reset state in order to resume to parse loop */
	return this.resumeAfterRecovery();
}
public String toString() {

	String s = "identifierStack : char[][] = {"; //$NON-NLS-1$
	for (int i = 0; i <= identifierPtr; i++) {
		s = s + "\"" + String.valueOf(identifierStack[i]) + "\","; //$NON-NLS-1$ //$NON-NLS-2$
	}
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "identierLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= identifierLengthPtr; i++) {
		s = s + identifierLengthStack[i] + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "astLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= astLengthPtr; i++) {
		s = s + astLengthStack[i] + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$
	s = s + "astPtr : int = " + String.valueOf(astPtr) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

	s = s + "intStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= intPtr; i++) {
		s = s + intStack[i] + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "expressionLengthStack : int[] = {"; //$NON-NLS-1$
	for (int i = 0; i <= expressionLengthPtr; i++) {
		s = s + expressionLengthStack[i] + ","; //$NON-NLS-1$
	}
	s = s + "}\n"; //$NON-NLS-1$

	s = s + "expressionPtr : int = " + String.valueOf(expressionPtr) + "\n"; //$NON-NLS-1$ //$NON-NLS-2$

	s = s + "\n\n\n----------------Scanner--------------\n" + scanner.toString(); //$NON-NLS-1$
	return s;

}
/*
 * Update recovery state based on current parser/scanner state
 */
protected void updateRecoveryState() {

	/* expose parser state to recovery state */
	currentElement.updateFromParserState();

	/* check and update recovered state based on current token,
		this action is also performed when shifting token after recovery
		got activated once. 
	*/
	this.recoveryTokenCheck();
}
protected void updateSourceDeclarationParts(int variableDeclaratorsCounter) {
	//fields is a definition of fields that are grouped together like in
	//public int[] a, b[], c
	//which results into 3 fields.

	FieldDeclaration field;
	int endTypeDeclarationPosition = 
		-1 + astStack[astPtr - variableDeclaratorsCounter + 1].sourceStart; 
	for (int i = 0; i < variableDeclaratorsCounter - 1; i++) {
		//last one is special(see below)
		field = (FieldDeclaration) astStack[astPtr - i - 1];
		field.endPart1Position = endTypeDeclarationPosition;
		field.endPart2Position = -1 + astStack[astPtr - i].sourceStart;
	}
	//last one
	(field = (FieldDeclaration) astStack[astPtr]).endPart1Position = 
		endTypeDeclarationPosition; 
	field.endPart2Position = field.declarationSourceEnd;

}
protected void updateSourcePosition(Expression exp) {
	//update the source Position of the expression

	//intStack : int int
	//-->
	//intStack : 

	exp.sourceEnd = intStack[intPtr--];
	exp.sourceStart = intStack[intPtr--];
}
}

package org.eclipse.jdt.internal.core.search.indexing;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.core.index.*;
import org.eclipse.jdt.internal.compiler.parser.InvalidInputException;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.compiler.parser.TerminalSymbols;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;

import java.io.*;
import java.util.*;

/**
 * A SourceIndexer indexes java files using a java parser. The following items are indexed:
 * Declarations of:
 * - Classes<br>
 * - Interfaces; <br>
 * - Methods;<br>
 * - Fields;<br>
 * References to:
 * - Methods (with number of arguments); <br>
 * - Fields;<br>
 * - Types;<br>
 * - Constructors.
 */
public class SourceIndexer extends AbstractIndexer {
	
	public static final String[] FILE_TYPES= new String[] {"java"}; //$NON-NLS-1$
	protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	
/**
 * Returns the file types the <code>IIndexer</code> handles.
 */

public String[] getFileTypes(){
	return FILE_TYPES;
}
protected void indexFile(IDocument document) throws IOException {

	// Add the name of the file to the index
	output.addDocument(document);

	// Create a new Parser
	SourceIndexerRequestor requestor = new SourceIndexerRequestor(this, document);
	SourceElementParser parser = new SourceElementParser(requestor, problemFactory);

	// Launch the parser
	char[] source = null;
	char[] name = null;
	try {
		source = document.getCharContent();
		name = document.getName().toCharArray();
	} catch(Exception e){
	}
	if (source == null || name == null) return; // could not retrieve document info (e.g. resource was discarded)
	CompilationUnit compilationUnit = new CompilationUnit(source, name);
	try {
		parser.parseCompilationUnit(compilationUnit, true);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
/**
 * Sets the document types the <code>IIndexer</code> handles.
 */

public void setFileTypes(String[] fileTypes){}
}

/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.io.IOException;
import java.util.Locale;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.index.IDocument;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;

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
public class SourceIndexer extends AbstractIndexer implements SuffixConstants {
	
	public static final String[] FILE_TYPES= new String[] {EXTENSION_java};
	protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	IFile resourceFile;
	
SourceIndexer(IFile resourceFile)	{
	this.resourceFile = resourceFile;
}

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
	SourceElementParser parser = new SourceElementParser(
		requestor, 
		problemFactory, 
		new CompilerOptions(JavaCore.create(this.resourceFile.getProject()).getOptions(true)), 
		true); // index local declarations

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
		parser.parseCompilationUnit(compilationUnit, true/*full parse*/);
	} catch (Exception e) {
		e.printStackTrace();
	}
}
/**
 * Sets the document types the <code>IIndexer</code> handles.
 */

public void setFileTypes(String[] fileTypes){}
}

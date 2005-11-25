/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import java.util.Locale;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.internal.compiler.SourceElementParser;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;
import org.eclipse.jdt.internal.core.search.processing.JobManager;

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
	
	protected DefaultProblemFactory problemFactory= new DefaultProblemFactory(Locale.getDefault());
	
	public SourceIndexer(SearchDocument document) {
		super(document);
	}
	public void indexDocument() {
		// Create a new Parser
		SourceIndexerRequestor requestor = new SourceIndexerRequestor(this);
		String documentPath = this.document.getPath();
		IPath path = new Path(documentPath);
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(path.segment(0));
		Map options = JavaCore.create(project).getOptions(true);
		// disable task tags to speed up parsing
		options.put(JavaCore.COMPILER_TASK_TAGS, ""); //$NON-NLS-1$
		SourceElementParser parser = new SourceElementParser(
			requestor, 
			this.problemFactory, 
			new CompilerOptions(options), 
			true, // index local declarations
			true, // optimize string literals
			false); // do not use source javadoc parser to speed up parsing
		parser.reportOnlyOneSyntaxError = true;
	
		// Always check javadoc while indexing
		parser.javadocParser.checkDocComment = true;
		parser.javadocParser.reportProblems = false;
		
		// Launch the parser
		char[] source = null;
		char[] name = null;
		try {
			source = document.getCharContents();
			name = documentPath.toCharArray();
		} catch(Exception e){
			// ignore
		}
		if (source == null || name == null) return; // could not retrieve document info (e.g. resource was discarded)
		CompilationUnit compilationUnit = new CompilationUnit(source, name);
		try {
			parser.parseCompilationUnit(compilationUnit, true/*full parse*/);
		} catch (Exception e) {
			if (JobManager.VERBOSE) {
				e.printStackTrace();
			}
		}
	}
}

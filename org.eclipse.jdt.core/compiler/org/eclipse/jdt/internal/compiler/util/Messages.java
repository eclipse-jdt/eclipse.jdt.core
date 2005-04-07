/**********************************************************************
 * Copyright (c) 2005 IBM Corporation and others. All rights reserved.   This
 * program and the accompanying materials are made available under the terms of
 * the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.jdt.internal.compiler.util;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.internal.compiler.util.messages";//$NON-NLS-1$
	//
	// Copyright (c) 2000, 2004 IBM Corporation and others.
	// All rights reserved. This program and the accompanying materials
	// are made available under the terms of the Eclipse Public License v1.0
	// which accompanies this distribution, and is available at
	// http://www.eclipse.org/legal/epl-v10.html
	//
	// Contributors:
	//     IBM Corporation - initial API and implementation
	//
	// Eclipse Java Core Compiler messages.

	// compilation
	public static String compilation_unresolvedProblem;
	public static String compilation_unresolvedProblems;
	public static String compilation_request;
	public static String compilation_loadBinary;
	public static String compilation_process;
	public static String compilation_write;
	public static String compilation_done;
	public static String compilation_units;
	public static String compilation_unit;
	public static String compilation_internalError;

	// output
	public static String output_isFile;
	public static String output_isFileNotDirectory;
	public static String output_dirName;
	public static String output_notValidAll;
	public static String output_fileName;
	public static String output_notValid;

	// problem
	public static String problem_noSourceInformation;
	public static String problem_atLine;

	// abort
	public static String abort_invalidAttribute;
	public static String abort_missingCode;
	public static String abort_againstSourceModel;

	// accept
	public static String accept_cannot;

	// parser
	public static String parser_incorrectPath;
	public static String parser_moveFiles;
	public static String parser_syntaxRecovery;
	public static String parser_regularParse;
	public static String parser_missingFile;
	public static String parser_corruptedFile;
	public static String parser_endOfFile;
	public static String parser_endOfConstructor;
	public static String parser_endOfMethod;
	public static String parser_endOfInitializer;

	// ast
	public static String ast_missingCode;

	// constant
	public static String constant_cannotCastedInto;
	public static String constant_cannotConvertedTo;

	// miscellaneous
	public static String error_undefinedTypeVariable;
	public static String error_missingBound;

	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
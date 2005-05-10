/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.apt.core.util;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.internal.env.ProcessorEnvImpl;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

public class AptUtil {

	/**
	 * scan the source code to see if there are any annotation tokens
	 */
	public static boolean hasAnnotationInstance( IFile f )
	{
		try
		{
			char[] source = ProcessorEnvImpl.getFileContents( f );
			return hasAnnotationInstance( source );
		}
		catch( Exception ioe )
		{
			return false;
		}
	}
	
	public static boolean hasAnnotationInstance( ICompilationUnit cu )
	{
		try
		{
			IBuffer b = cu.getBuffer();
			if ( b == null )
				return false;
			char[] source = b.getCharacters();
			return hasAnnotationInstance( source );
		}
		catch( JavaModelException jme )
		{
			return false;
		}
	}
	
	public static boolean hasAnnotationInstance( char[] source )
	{
		try
		{		
			if ( source == null )
				return false;
			IScanner scanner = ToolFactory.createScanner( 
				false, false, false, CompilerOptions.VERSION_1_5 );
			scanner.setSource( source );
			int token = scanner.getNextToken();
			while ( token != ITerminalSymbols.TokenNameEOF )
			{
				token = scanner.getNextToken();
				if ( token == ITerminalSymbols.TokenNameAT )
				{
					//
					// found an @ sign, see if next token is "interface"
					// @interface is an annotation decl and not an annotation
					// instance.  
					//
					token = scanner.getNextToken();
					if ( token != ITerminalSymbols.TokenNameinterface )
						return true;
				}
			}
			return false;
		}
		catch( InvalidInputException iie )
		{
			// lex error, so report false
			return false;
		}
		catch( Exception e )
		{
			// TODO:  deal with this exception
			e.printStackTrace();
			return false;
		}
	}
	
}

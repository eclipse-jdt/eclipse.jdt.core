/**********************************************************************
Copyright (c)2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.util.IClassFileDisassembler;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.core.util.Disassembler;
import org.eclipse.jdt.internal.formatter.CodeFormatter;

/**
 * Factory for creating various compiler tools, such as scanners, parsers and compilers.
 * 
 * @since 2.0
 */
public class ToolFactory {

	/**
	 * Create an instance of a code formatter. A code formatter implementation can be contributed via the 
	 * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered extension, the factory 
	 * will default to using the default code formatter.
	 * 
	 * @see ICodeFormatter
	 * @see ToolFactory#createDefaultCodeFormatter()
	 */
	public static ICodeFormatter createCodeFormatter(){
		
			Plugin jdtCorePlugin = JavaCore.getPlugin();
			if (jdtCorePlugin == null) return null;
		
			IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaCore.FORMATTER_EXTPOINT_ID);
			if (extension != null) {
				IExtension[] extensions =  extension.getExtensions();
				for(int i = 0; i < extensions.length; i++){
					IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
						IPluginDescriptor plugin = extension.getDeclaringPluginDescriptor();
						if (plugin.isPluginActivated()) {
							for(int j = 0; j < configElements.length; j++){
								try {
									Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
									if (execExt instanceof ICodeFormatter){
										// use first contribution found
										return (ICodeFormatter)execExt;
									}
								} catch(CoreException e){
								}
							}
						}
				}	
			}
		// no proper contribution found, use default formatter			
		return createDefaultCodeFormatter(null);
	}

	/**
	 * Create an instance of the buit-in code formatter. A code formatter implementation can be contributed via the 
	 * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered extension, the factory will 
	 * default to using the default code formatter.
	 * 
	 * @param options - the options map to use for formatting with the default code formatter. Recognized options
	 * 	are documented on <code>JavaCore#getDefaultOptions()</code>. If set to <code>null</code>, then use 
	 * 	the current settings from <code>JavaCore#getOptions</code>.
	 * 
	 * @see ICodeFormatter
	 * @see ToolFactory#createCodeFormatter()
	 * @see JavaCore#getOptions()
	 */
	public static ICodeFormatter createDefaultCodeFormatter(Map options){

		if (options == null) options = JavaCore.getOptions();
		return new CodeFormatter(options);
	}
	
	/**
	 * Create a classfile bytecode disassembler, able to produce a String representation of a given classfile.
	 * 
	 * @see IClassFileDisassembler
	 */
	public static IClassFileDisassembler createDefaultClassFileDisassembler(){
		return new Disassembler();
	}
	
	/**
	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can then be
	 * used to tokenize some source in a Java aware way.
	 * Here is a typical scanning loop:
	 * 
	 * <code>
	 *   IScanner scanner = ToolFactory.createScanner(false, false, false);
	 *   scanner.setSource("int i = 0;".toCharArray());
	 *   while (true) {
	 *     int token = scanner.getNextToken();
	 *     if (token == ITerminalSymbols.TokenNameEOF) break;
	 *     System.out.println(token + " : " + new String(scanner.getCurrentTokenSource()));
	 *   }
	 * </code>
	 * 
  	 * @return IScanner
	 * 
	 * @param tokenizeComments -  if set to <code>false</code>, comments will be silently consumed
	 * @param tokenizeWhiteSpace -  if set to <code>false</code>, white spaces will be silently consumed,
		@param assertKeyword - if set to <code>false</code>, occurrences of 'assert' will be reported as identifiers
	 * (<code>ITerminalSymbols#TokenNameIdentifier</code>), whereas if set to <code>true</code>, it
	 * would report assert keywords (<code>ITerminalSymbols#TokenNameassert</code>). Java 1.4 has introduced
	 * a new 'assert' keyword.
	 * @param recordLineSeparator - if set to <code>true</code>, the scanner will record positions of encountered line 
	 * separator ends. In case of multi-character line separators, the last character position is considered. These positions
	 * can then be extracted using <code>IScanner#getLineEnds</code>
	 * 
	 * @see IScanner
	 */
	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean assertMode, boolean recordLineSeparator){

		Scanner scanner = new Scanner(tokenizeComments, tokenizeWhiteSpace, false, assertMode);
		scanner.recordLineSeparator = recordLineSeparator;
		return scanner;
	}
}

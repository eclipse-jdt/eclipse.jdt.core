package org.eclipse.jdt.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.internal.compiler.parser.Scanner;
import org.eclipse.jdt.internal.formatter.CodeFormatter;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

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
		return createDefaultCodeFormatter();
	}

	/**
	 * Create an instance of the buit-in code formatter. A code formatter implementation can be contributed via the 
	 * extension point "org.eclipse.jdt.core.codeFormatter". If unable to find a registered extension, the factory will 
	 * default to using the default code formatter.
	 * 
	 * @see ICodeFormatter
	 * @see ToolFactory#createCodeFormatter()
	 */
	public static ICodeFormatter createDefaultCodeFormatter(){

		return new CodeFormatter(JavaCore.getOptions());
	}
	
	/**
	 * Create a scanner, indicating the level of detail requested for tokenizing. The scanner can then be
	 * used to tokenize some source in a Java aware way.
	 * Here is a typical scanning loop:
	 * 
	 * <code>
	 *   IScanner scanner = ToolFactory.createScanner(false, false, false);
	 *   scanner.setSourceBuffer("int i = 0;".toCharArray());
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
	 */
	public static IScanner createScanner(boolean tokenizeComments, boolean tokenizeWhiteSpace, boolean assertMode){
		return new Scanner(tokenizeComments, tokenizeWhiteSpace, false, assertMode);
	}
}

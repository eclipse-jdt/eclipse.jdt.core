/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.problem;

import java.util.Enumeration;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.jdt.core.compiler.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.util.HashtableOfInt;
import org.eclipse.jdt.internal.compiler.util.Util;

public class DefaultProblemFactory implements IProblemFactory {

	public HashtableOfInt messageTemplates;
	private Locale locale;
	private static HashtableOfInt DEFAULT_LOCALE_TEMPLATES;
	private final static char[] DOUBLE_QUOTES = "''".toCharArray(); //$NON-NLS-1$
	private final static char[] SINGLE_QUOTE = "'".toCharArray(); //$NON-NLS-1$

public DefaultProblemFactory() {
	this(Locale.getDefault());
}
/**
 * @param loc the locale used to get the right message
 */
public DefaultProblemFactory(Locale loc) {
	this.locale = loc;
	if (Locale.getDefault().equals(loc)){
		if (DEFAULT_LOCALE_TEMPLATES == null){
			DEFAULT_LOCALE_TEMPLATES = loadMessageTemplates(loc);
		}
		this.messageTemplates = DEFAULT_LOCALE_TEMPLATES;
	} else {
		this.messageTemplates = loadMessageTemplates(loc);
	}
}
/**
 * Answer a new IProblem created according to the parameters value
 * <ul>
 * <li>originatingFileName the name of the file name from which the problem is originated
 * <li>problemId the problem id
 * <li>problemArguments the fully qualified arguments recorded inside the problem
 * <li>messageArguments the arguments needed to set the error message (shorter names than problemArguments ones)
 * <li>severity the severity of the problem
 * <li>startPosition the starting position of the problem
 * <li>endPosition the end position of the problem
 * <li>lineNumber the line on which the problem occured
 * </ul>
 * @param originatingFileName char[]
 * @param problemId int
 * @param arguments String[]
 * @param severity int
 * @param startPosition int
 * @param endPosition int
 * @param lineNumber int
 * @return org.eclipse.jdt.internal.compiler.IProblem
 */
public IProblem createProblem(
	char[] originatingFileName, 
	int problemId, 
	String[] problemArguments, 
	String[] messageArguments, 
	int severity, 
	int startPosition, 
	int endPosition, 
	int lineNumber) {

	return new DefaultProblem(
		originatingFileName, 
		this.getLocalizedMessage(problemId, messageArguments),
		problemId, 
		problemArguments, 
		severity, 
		startPosition, 
		endPosition, 
		lineNumber); 
}
private final static int keyFromID(int id) {
    return id + 1; // keys are offsetted by one in table, since it cannot handle 0 key
}
private final static int keyToID(int key) {
    return key - 1; // keys are offsetted by one in table, since it cannot handle 0 key
}

/**
 * Answer the locale used to retrieve the error messages
 * @return java.util.Locale
 */
public Locale getLocale() {
	return locale;
}
public final String getLocalizedMessage(int id, String[] problemArguments) {
	StringBuffer output = new StringBuffer(80);
	if ((id & IProblem.Javadoc) != 0) {
		output.append((String)messageTemplates.get(keyFromID(IProblem.JavadocMessagePrefix & IProblem.IgnoreCategoriesMask)));
	}
	String message = (String)messageTemplates.get(keyFromID(id & IProblem.IgnoreCategoriesMask)); 
	if (message == null) {
		return "Unable to retrieve the error message for problem id: " //$NON-NLS-1$
			+ (id & IProblem.IgnoreCategoriesMask)
			+ ". Check compiler resources.";  //$NON-NLS-1$
	}

	// for compatibility with MessageFormat which eliminates double quotes in original message
	char[] messageWithNoDoubleQuotes =
		CharOperation.replace(message.toCharArray(), DOUBLE_QUOTES, SINGLE_QUOTE);
	message = new String(messageWithNoDoubleQuotes);

	int length = message.length();
	int start = -1, end = length;
	while (true) {
		if ((end = message.indexOf('{', start)) > -1) {
			output.append(message.substring(start + 1, end));
			if ((start = message.indexOf('}', end)) > -1) {
				try {
					output.append(
						problemArguments[Integer.parseInt(message.substring(end + 1, start))]); 
				} catch (NumberFormatException nfe) {
					output.append(message.substring(end + 1, start + 1));
				} catch (ArrayIndexOutOfBoundsException e) {
					return "Cannot bind message for problem (id: " //$NON-NLS-1$
						+ (id & IProblem.IgnoreCategoriesMask)
						+ ") \""  //$NON-NLS-1$
						+ message
						+ "\" with arguments: {" //$NON-NLS-1$
						+ Util.toString(problemArguments)
						+"}"; //$NON-NLS-1$
				}
			} else {
				output.append(message.substring(end, length));
				break;
			}
		} else {
			output.append(message.substring(start + 1, length));
			break;
		}
	}
	return output.toString();
}
/**
 * @param problem org.eclipse.jdt.internal.compiler.IProblem
 * @return String
 */
public final String localizedMessage(IProblem problem) {
	return getLocalizedMessage(problem.getID(), problem.getArguments());
}

/**
 * This method initializes the MessageTemplates class variable according
 * to the current Locale.
 */
public static HashtableOfInt loadMessageTemplates(Locale loc) {
	ResourceBundle bundle = null;
	String bundleName = "org.eclipse.jdt.internal.compiler.problem.messages"; //$NON-NLS-1$
	try {
		bundle = ResourceBundle.getBundle(bundleName, loc); 
	} catch(MissingResourceException e) {
		System.out.println("Missing resource : " + bundleName.replace('.', '/') + ".properties for locale " + loc); //$NON-NLS-1$//$NON-NLS-2$
		throw e;
	}
	HashtableOfInt templates = new HashtableOfInt(700);
	Enumeration keys = bundle.getKeys();
	while (keys.hasMoreElements()) {
	    String key = (String)keys.nextElement();
	    try {
	        int messageID = Integer.parseInt(key);
			templates.put(keyFromID(messageID), bundle.getString(key));
	    } catch(NumberFormatException e) {
	        // key ill-formed
		} catch (MissingResourceException e) {
			// available ID
	    }
	}
	return templates;
}

}

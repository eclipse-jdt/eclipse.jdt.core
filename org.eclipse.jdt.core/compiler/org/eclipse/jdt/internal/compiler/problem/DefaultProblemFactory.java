package org.eclipse.jdt.internal.compiler.problem;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.text.*;
import java.util.*;
import org.eclipse.jdt.internal.compiler.*;

public class DefaultProblemFactory implements IProblemFactory {


	public String[] messageTemplates;
	private Locale locale;
	private static String[] DEFAULT_LOCALE_TEMPLATES;

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
 * @param originatingFileName the name of the file name from which the problem is originated
 * @param problemId the problem id
 * @param arguments the arguments needed to set the error message
 * @param severity the severity of the problem
 * @param startPosition the starting position of the problem
 * @param endPosition the end position of the problem
 * @param lineNumber the line on which the problem occured
 * @return com.ibm.compiler.java.api.IProblem
 */
public IProblem createProblem(
	char[] originatingFileName, 
	int problemId, 
	String[] arguments, 
	int severity, 
	int startPosition, 
	int endPosition, 
	int lineNumber) {

	return new DefaultProblem(
		originatingFileName, 
		this.getLocalizedMessage(problemId, arguments),
		problemId, 
		arguments, 
		severity, 
		startPosition, 
		endPosition, 
		lineNumber); 
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
	String message = 
		messageTemplates[(id & ProblemIrritants.IgnoreCategoriesMask)]; 
	if (message == null) {
		return "Unable to retrieve the error message for problem id: "/*nonNLS*/
			+ id
			+ ". Check compiler resources."/*nonNLS*/; 
	}

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
					return "Corrupted compiler resources for problem id: "/*nonNLS*/
						+ (id & ProblemIrritants.IgnoreCategoriesMask)
						+ ". Check compiler resources."/*nonNLS*/; 
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
 * @return com.ibm.compiler.java.problem.LocalizedProblem
 * @param problem com.ibm.compiler.java.problem.Problem
 */
public final String localizedMessage(IProblem problem) {
	return getLocalizedMessage(problem.getID(), problem.getArguments());
}

/**
 * This method initializes the MessageTemplates class variable according
 * to the current Locale.
 */
public static String[] loadMessageTemplates(Locale loc) {
	ResourceBundle bundle = ResourceBundle.getBundle("org.eclipse.jdt.internal.compiler.problem.Messages"/*nonNLS*/, loc);
	String[] templates = new String[500];
	for (int i = 0, max = templates.length; i < max; i++) {
		try {
			templates[i] = bundle.getString(String.valueOf(i));
		} catch (MissingResourceException e) {
		}
	}
	return templates;
}

public DefaultProblemFactory() {
	this(Locale.getDefault());
}
}

package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;
import org.eclipse.jdt.internal.compiler.problem.*;

import java.text.*;
import java.util.*;

/**
 * @see IProblemFactory
 */
public class ProblemFactory implements IProblemFactory {
	private ResourceBundle compilerResources;
	public String[] messageTemplates;
	private Locale locale;
	public ProblemFactory() {
		this(Locale.getDefault());
	}

	/**
	 * @param loc the locale used to get the right message
	 */
	public ProblemFactory(Locale loc) {
		locale = loc;
		compilerResources =
			ResourceBundle.getBundle(
				"org.eclipse.jdt.internal.compiler.problem.Messages",
				loc);
		//$NON-NLS-1$
		initializeMessageTemplates();
	}

	/**
	 * Returns a new IProblem created according to the parameters value
	 * @param originatingFileName the name of the file name from which the problem is originated
	 * @param problemId the problem id
	 * @param arguments the arguments needed to set the error message
	 * @param severity the severity of the problem
	 * @param startPosition the starting position of the problem
	 * @param endPosition the end position of the problem
	 * @param lineNumber the line on which the problem occured
	 * @return com.ibm.compiler.java.api.IProblem
	 * @see IProblemFactory
	 */
	public IProblem createProblem(
		char[] originatingFileName,
		int problemId,
		String[] arguments,
		int severity,
		int startPosition,
		int endPosition,
		int lineNumber) {

		return new Problem(
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
	 * Returns the locale used to retrieve the error messages.
	 */
	public Locale getLocale() {
		return locale;
	}

	public final String getLocalizedMessage(int id, String[] problemArguments) {
		StringBuffer output = new StringBuffer(80);
		String message = messageTemplates[(id & ProblemIrritants.IgnoreCategoriesMask)];
		if (message == null) {
			return "Unable to retrieve the error message for problem id: "
				+ id
				+ ". Check compiler resources.";
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
						return "Corrupted compiler resources for problem id: "
							+ (id & ProblemIrritants.IgnoreCategoriesMask)
							+ ". Check compiler resources.";
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
	 * This method initializes the MessageTemplates class variable according
	 * to the current Locale.
	 */
	public final void initializeMessageTemplates() {
		messageTemplates = new String[500];
		for (int i = 0; i < messageTemplates.length; i++) {
			try {
				messageTemplates[i] = compilerResources.getString(String.valueOf(i));
				//$NON-NLS-1$
			} catch (MissingResourceException e) {
			}
		}
	}

	/**
	 * @see IProblemFactory
	 */
	public final String localizedMessage(Problem problem) {
		return getLocalizedMessage(problem.getID(), problem.getArguments());
	}

}

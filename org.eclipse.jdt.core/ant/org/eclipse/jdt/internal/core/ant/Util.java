package org.eclipse.jdt.internal.core.ant;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;

public class Util {


	/* Bundle containing messages */
	protected static ResourceBundle bundle;
	private final static String bundleName = "org.eclipse.jdt.internal.core.ant.Messages"; //$NON-NLS-1$


	static {
		relocalize();
	}	

/**
 * Lookup the message with the given ID in this catalog 
 */
public static String bind(String id) {
	return bind(id, (String[])null);
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string values.
 */
public static String bind(String id, String[] bindings) {
	if (id == null)
		return "No message available"; //$NON-NLS-1$
	String message = null;
	try {
		message = bundle.getString(id);
	} catch (MissingResourceException e) {
		// If we got an exception looking for the message, fail gracefully by just returning
		// the id we were looking for.  In most cases this is semi-informative so is not too bad.
		return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
	}
	if (bindings == null)
		return message;
	int length = message.length();
	int start = -1;
	int end = length;
	StringBuffer output = new StringBuffer(80);
	while (true) {
		if ((end = message.indexOf('{', start)) > -1) {
			output.append(message.substring(start + 1, end));
			if ((start = message.indexOf('}', end)) > -1) {
				int index = -1;
				try {
					index = Integer.parseInt(message.substring(end + 1, start));
					output.append(bindings[index]);
				} catch (NumberFormatException nfe) {
					output.append(message.substring(end + 1, start + 1));
				} catch (ArrayIndexOutOfBoundsException e) {
					output.append("{missing " + Integer.toString(index) + "}"); //$NON-NLS-2$ //$NON-NLS-1$
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
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given string.
 */
public static String bind(String id, String binding) {
	return bind(id, new String[] {binding});
}

/**
 * Lookup the message with the given ID in this catalog and bind its
 * substitution locations with the given strings.
 */
public static String bind(String id, String binding1, String binding2) {
	return bind(id, new String[] {binding1, binding2});
}

/**
 * Creates a NLS catalog for the given locale.
 */
public static void relocalize() {
	bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
}
}

/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

/**
 * Generic option description, which can be modified independently from the
 * component it belongs to.
 *
 * @deprecated backport 1.0 internal functionality
 */

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.NoSuchElementException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.CharOperation;

public class ConfigurableOption {
	private String componentName;
	private String optionName;
	private int id;

	private String category;
	private String name;
	private String description;
	private int currentValueIndex;
	private String[] possibleValues;

	// special value for <possibleValues> indicating that
	// the <currentValueIndex> is the actual value
	public final static String[] NoDiscreteValue = {};
/**
 * INTERNAL USE ONLY
 *
 * Initialize an instance of this class according to a specific locale
 *
 * @param loc java.util.Locale
 */
public ConfigurableOption(
	String componentName,
	String optionName,
	Locale loc,
	int currentValueIndex) {

	this.componentName = componentName;
	this.optionName = optionName;
	this.currentValueIndex = currentValueIndex;

	ResourceBundle resource = null;
	try {
		String location = componentName.substring(0, componentName.lastIndexOf('.'));
		resource = ResourceBundle.getBundle(location + ".options", loc); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		this.category = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		this.name = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		this.description = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
		this.possibleValues = CharOperation.NO_STRINGS;
		this.id = -1;
	}
	if (resource == null) return;
	try {
		this.id = Integer.parseInt(resource.getString(optionName + ".number")); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		this.id = -1;
	} catch (NumberFormatException e) {
		this.id = -1;
	}
	try {
		this.category = resource.getString(optionName + ".category"); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		this.category = "Missing ressources entries for" + componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	try {
		this.name = resource.getString(optionName + ".name"); //$NON-NLS-1$
	} catch (MissingResourceException e) {
		this.name = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
	try {
		StringTokenizer tokenizer = new StringTokenizer(resource.getString(optionName + ".possibleValues"), "|"); //$NON-NLS-1$ //$NON-NLS-2$
		int numberOfValues = Integer.parseInt(tokenizer.nextToken());
		if(numberOfValues == -1){
			this.possibleValues = NoDiscreteValue;
		} else {
			this.possibleValues = new String[numberOfValues];
			int index = 0;
			while (tokenizer.hasMoreTokens()) {
				this.possibleValues[index] = tokenizer.nextToken();
				index++;
			}
		}
	} catch (MissingResourceException e) {
		this.possibleValues = CharOperation.NO_STRINGS;
	} catch (NoSuchElementException e) {
		this.possibleValues = CharOperation.NO_STRINGS;
	} catch (NumberFormatException e) {
		this.possibleValues = CharOperation.NO_STRINGS;
	}
	try {
		this.description = resource.getString(optionName + ".description");  //$NON-NLS-1$
	} catch (MissingResourceException e) {
		this.description = "Missing ressources entries for"+ componentName + " options"; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
/**
 * Return a String that represents the localized category of the receiver.
 * @return java.lang.String
 */
public String getCategory() {
	return this.category;
}
/**
 * Return a String that identifies the component owner (typically the qualified
 *	type name of the class which it corresponds to).
 *
 * e.g. "org.eclipse.jdt.internal.compiler.api.Compiler"
 *
 * @return java.lang.String
 */
public String getComponentName() {
	return this.componentName;
}
/**
 * Answer the index (in possibleValues array) of the current setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 *
 * @return int
 */
public int getCurrentValueIndex() {
	return this.currentValueIndex;
}
/**
 * Return an String that represents the localized description of the receiver.
 *
 * @return java.lang.String
 */
public String getDescription() {
	return this.description;
}
/**
 * Internal ID which allows the configurable component to identify this particular option.
 *
 * @return int
 */
public int getID() {
	return this.id;
}
/**
 * Return a String that represents the localized name of the receiver.
 * @return java.lang.String
 */
public String getName() {
	return this.name;
}
/**
 * Return an array of String that represents the localized possible values of the receiver.
 * @return java.lang.String[]
 */
public String[] getPossibleValues() {
	return this.possibleValues;
}
/**
 * Change the index (in possibleValues array) of the current setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 */
public void setValueIndex(int newIndex) {
	this.currentValueIndex = newIndex;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Configurable option for "); //$NON-NLS-1$
	buffer.append(this.componentName).append("\n"); //$NON-NLS-1$
	buffer.append("- category:			").append(this.category).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	buffer.append("- name:				").append(this.name).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	/* display current value */
	buffer.append("- current value:	"); //$NON-NLS-1$
	if (this.possibleValues == NoDiscreteValue){
		buffer.append(this.currentValueIndex);
	} else {
		buffer.append(this.possibleValues[this.currentValueIndex]);
	}
	buffer.append("\n"); //$NON-NLS-1$

	/* display possible values */
	if (this.possibleValues != NoDiscreteValue){
		buffer.append("- possible values:	["); //$NON-NLS-1$
		for (int i = 0, max = this.possibleValues.length; i < max; i++) {
			if (i != 0)
				buffer.append(", "); //$NON-NLS-1$
			buffer.append(this.possibleValues[i]);
		}
		buffer.append("]\n"); //$NON-NLS-1$
		buffer.append("- curr. val. index:	").append(this.currentValueIndex).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	}
	buffer.append("- description:		").append(this.description).append("\n"); //$NON-NLS-1$ //$NON-NLS-2$
	return buffer.toString();
}
	/**
	 * Gets the optionName.
	 * @return Returns a String
	 */
	public String getOptionName() {
		return this.optionName;
	}
}

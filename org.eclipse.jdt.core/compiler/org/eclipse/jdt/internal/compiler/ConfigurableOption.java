package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic option description, which can be modified independently from the
 * component it belongs to.
 */

import java.util.*;

public class ConfigurableOption {
	private String componentName;
	private int id;

	private String category;
	private String name;
	private String description;
	private int currentValueIndex;
	private int defaultValueIndex;
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
	this.currentValueIndex = currentValueIndex;
		
	ResourceBundle resource = null;
	try {
		String location = componentName.substring(0, componentName.lastIndexOf('.'));
		resource = ResourceBundle.getBundle(location + ".options"/*nonNLS*/, loc); 
	} catch (MissingResourceException e) {
		category = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
		name = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
		description = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
		possibleValues = new String[0];
		id = -1;
	}
	if (resource == null) return;
	try {
		id = Integer.parseInt(resource.getString(optionName + ".number"/*nonNLS*/)); 
	} catch (MissingResourceException e) {
		id = -1;
	} catch (NumberFormatException e) {
		id = -1;
	}
	try {
		category = resource.getString(optionName + ".category"/*nonNLS*/); 
	} catch (MissingResourceException e) {
		category = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
	}
	try {
		name = resource.getString(optionName + ".name"/*nonNLS*/); 
	} catch (MissingResourceException e) {
		name = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
	}
	try {
		StringTokenizer tokenizer = new StringTokenizer(resource.getString(optionName + ".possibleValues"/*nonNLS*/), "|"/*nonNLS*/);
		int numberOfValues = Integer.parseInt(tokenizer.nextToken());
		if(numberOfValues == -1){
			possibleValues = NoDiscreteValue;
		} else {
			possibleValues = new String[numberOfValues];
			int index = 0;
			while (tokenizer.hasMoreTokens()) {
				possibleValues[index] = tokenizer.nextToken();
				index++;
			}
		}
	} catch (MissingResourceException e) {
		possibleValues = new String[0];
	} catch (NoSuchElementException e) {
		possibleValues = new String[0];
	} catch (NumberFormatException e) {
		possibleValues = new String[0];
	}
	try {
		description = resource.getString(optionName + ".description"/*nonNLS*/); 
	} catch (MissingResourceException e) {
		description = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
	}
}
/**
 * Return a String that represents the localized category of the receiver.
 * @return java.lang.String
 */
public String getCategory() {
	return category;
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
	return componentName;
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
	return currentValueIndex;
}
/**
 * Answer the index (in possibleValues array) of the default setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 *
 * @return int
 */
public int getDefaultValueIndex() {
	return defaultValueIndex;
}
/**
 * Return an String that represents the localized description of the receiver.
 *
 * @return java.lang.String
 */
public String getDescription() {
	return description;
}
/**
 * Internal ID which allows the configurable component to identify this particular option.
 *
 * @return int
 */
public int getID() {
	return id;
}
/**
 * Return a String that represents the localized name of the receiver.
 * @return java.lang.String
 */
public String getName() {
	return name;
}
/**
 * Return an array of String that represents the localized possible values of the receiver.
 * @return java.lang.String[]
 */
public String[] getPossibleValues() {
	return possibleValues;
}
/**
 * Change the index (in possibleValues array) of the current setting for this
 * particular option.
 *
 * In case the set of possibleValues is NoDiscreteValue, then this index is the
 * actual value (e.g. max line lenght set to 80).
 *
 * @return int
 */
public void setValueIndex(int newIndex) {
	currentValueIndex = newIndex;
}
public String toString() {
	StringBuffer buffer = new StringBuffer();
	buffer.append("Configurable option for "/*nonNLS*/);
	buffer.append(this.componentName).append("\n"/*nonNLS*/);
	buffer.append("- category:			"/*nonNLS*/).append(this.category).append("\n"/*nonNLS*/);
	buffer.append("- name:				"/*nonNLS*/).append(this.name).append("\n"/*nonNLS*/);
	/* display current value */
	buffer.append("- current value:	"/*nonNLS*/);
	if (possibleValues == NoDiscreteValue){
		buffer.append(this.currentValueIndex);
	} else {
		buffer.append(this.possibleValues[this.currentValueIndex]);
	}
	buffer.append("\n"/*nonNLS*/);
	
	/* display possible values */
	if (possibleValues != NoDiscreteValue){
		buffer.append("- possible values:	["/*nonNLS*/);
		for (int i = 0, max = possibleValues.length; i < max; i++) {
			if (i != 0)
				buffer.append(", "/*nonNLS*/);
			buffer.append(possibleValues[i]);
		}
		buffer.append("]\n"/*nonNLS*/);
		buffer.append("- curr. val. index:	"/*nonNLS*/).append(currentValueIndex).append("\n"/*nonNLS*/);
	}
	buffer.append("- description:		"/*nonNLS*/).append(description).append("\n"/*nonNLS*/);
	return buffer.toString();
}
}

package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * Generic option description, which can be modified independently from the
 * component it belongs to.
 * 
 * File to modify when adding an option :
 * Add option in option file of component
 * Modify setOption method of component option class
 * 
 * Option file syntax
 * <code>
 * com.foo.Bar.optionOne.category=Category One
 * com.foo.Bar.optionOne.name=Option One
 * com.foo.Bar.optionOne.possibleValues=2|Value 1|Value 2
 * com.foo.Bar.optionOne.description=Description of Option One
 * com.foo.Bar.optionOne.default=0
 * 
 * com.foo.Bar.optionTwo.category=Category One
 * com.foo.Bar.optionTwo.name=Option Two
 * com.foo.Bar.optionTwo.possibleValues=-1|string
 * com.foo.Bar.optionTwo.description=Description of Option Two
 * com.foo.Bar.optionTwo.default=Value
 *  
 * com.foo.Bar.optionThree.category=Category Two
 * com.foo.Bar.optionThree.name=Option Three
 * com.foo.Bar.optionThree.possibleValues=-1|int|0|no
 * com.foo.Bar.optionThree.description=Description of Option Three
 * com.foo.Bar.optionThree.default=4
 * </code>
 * 
 * possibleValues values :
 * - A positive number and the list of the value
 * - Value -1 following by type. If type is a number
 * there is min and max value (with value no if there is no max or min
 * 
 * e.g.
 * 2|Value 1|Value 2
 * 3|Value 1|Value 2|Value 3
 * -1|string
 * -1|int|0|20
 * -1|int|no|20
 * -1|int|-10|no
 * -1|int|no|no
 * -1|int
 * -1|float|0.0|20.0
 * -1|float|no|20.0
 * -1|float|-10.0|no
 * -1|float|no|no
 * -1|float
 * 
 */
import java.util.*;

public class ConfigurableOption {
	public final static String STRING = "string"/*nonNLS*/;
	public final static String INT = "int"/*nonNLS*/;
	public final static String FLOAT = "float"/*nonNLS*/;
	public final static String DISCRETE = "discrete"/*nonNLS*/;
	
	// special value for <possibleValues> indicating that 
	// the <currentValueIndex> is the actual value
	public final static String[] NoDiscreteValue = {};

	private String id;
	private String value;
	private int valueIndex = -1;
	private String defaultValue;
	private int defaultValueIndex = -1;
	private String category;
	private String name;
	private String description;
	private String[] possibleValues;
	private int order;
	
	private String type;
	private boolean maxExisting = false;
	private boolean minExisting = false;
	private int maxInt;
	private int minInt;
	private float maxFloat;
	private float minFloat;
	
	
	private Locale loc;

	private String componentName;
	private String missing;

	public ConfigurableOption(String id, Locale loc) {
		this.id = id;
		this.value = value;
		this.loc = loc;

		this.componentName = id.substring(0,id.lastIndexOf('.'));

		ResourceBundle bundle = null;
		missing = "Missing ressources entries for"/*nonNLS*/ + componentName + " options"/*nonNLS*/;
		try {
			bundle = ResourceBundle.getBundle(componentName,loc); 
		} catch (MissingResourceException e) {
			id = missing;
			defaultValue = missing;
			category = missing;
			name = missing;
			description = missing;
			possibleValues = NoDiscreteValue;
		}
		if (bundle == null) return;
		try{
			StringTokenizer tokenizer =
				new StringTokenizer(
					bundle.getString(id + ".possibleValues"/*nonNLS*/),
					"|"/*nonNLS*/);
			int numberOfValues = Integer.parseInt(tokenizer.nextToken());
			if (numberOfValues == -1) {
				// the possible value are not discrete
				possibleValues = NoDiscreteValue;
				
				String token = tokenizer.nextToken();
				type = token;
				if(token.equals(STRING)){

				}
				else if(token.equals(INT) && tokenizer.hasMoreTokens()){
					token = tokenizer.nextToken();
					if(!token.equals("no"/*nonNLS*/)){
						minExisting = true;
						minInt = Integer.parseInt(token);
					}
					token = tokenizer.nextToken();
					if(!token.equals("no"/*nonNLS*/)){
						maxExisting = true;
						maxInt = Integer.parseInt(token);
					}
				}
				else if(token.equals(FLOAT) && tokenizer.hasMoreTokens()){
					token = tokenizer.nextToken();
					if(!token.equals("no"/*nonNLS*/)){
						minExisting = true;
						minFloat = Float.parseFloat(token);
					}
					token = tokenizer.nextToken();
					if(!token.equals("no"/*nonNLS*/)){
						maxExisting = true;
						maxFloat = Float.parseFloat(token);
					}
				}			
			} else {
				// possible value are discrete
				type = DISCRETE;
				possibleValues = new String[numberOfValues];
				int index = 0;
				while (tokenizer.hasMoreTokens()) {
					possibleValues[index] = tokenizer.nextToken();
					index++;
				}
			}
			
		} catch (MissingResourceException e) {
			possibleValues = NoDiscreteValue;
			type = missing;
		} catch (NoSuchElementException e) {
			possibleValues = NoDiscreteValue;
			type = missing;
		} catch (NumberFormatException e) {
			possibleValues = NoDiscreteValue;
			type = missing;
		}
		try{
			if(possibleValues == NoDiscreteValue){
				defaultValue = bundle.getString(id + ".default"/*nonNLS*/);
			}
			else{
				defaultValueIndex = Integer.parseInt(bundle.getString(id + ".default"/*nonNLS*/));
			}
		} catch (MissingResourceException e) {
			defaultValue = missing;
		} catch (NumberFormatException e) {
			defaultValueIndex = -1;
		}
		try{
			order = Integer.parseInt(bundle.getString(id + ".order"/*nonNLS*/));
		} catch (NumberFormatException e) {
			order = -1;
		} catch (MissingResourceException e) {
			order = -1;
		}
		try{
			category = bundle.getString(id + ".category"/*nonNLS*/);
		} catch (MissingResourceException e) {
			category = missing;
		}
		try{
			name = bundle.getString(id + ".name"/*nonNLS*/);
		} catch (MissingResourceException e) {
			name = missing;
		}
		try{
			description = bundle.getString(id + ".description"/*nonNLS*/);
		} catch (MissingResourceException e) {
			description = missing;
		}
	}
	
	/**
	* Internal ID which allows the configurable component to identify this particular option.
	*
	* @return String
	*/
	public String getID() {
		return id;
	}
	
	/**
	* Answer the value of the current setting for this particular option.
	*
	* @return String
	*/
	public String getValue() {
		if(possibleValues == NoDiscreteValue){
			if (value == null)
				return getDefaultValue();
			return value;
		}
		else {
			if (valueIndex == -1)
				return getDefaultValue();
			return possibleValues[valueIndex];
		}
	}
	
	/**
	* Change the value of the current setting for this particular option.
	*
	* @param value String
	*/
	public void setValue(String value) {
		if(possibleValues == NoDiscreteValue){
			this.value = value;
		}
		else{
			for(int i = 0 ; i < possibleValues.length ; i++){
				if(possibleValues[i].equals(value)){
					this.valueIndex = i;
					break;
				}
			}
		}
	}
	
	/**
	 * Gets the valueIndex
	 * @return Returns a int
	 */
	public int getValueIndex() {
		if(possibleValues == NoDiscreteValue)
			return -1;
		if (valueIndex == -1)
			return getDefaultValueIndex();
		return valueIndex;
	}
	/**
	 * Sets the valueIndex
	 * @param valueIndex The valueIndex to set
	 */
	public void setValueIndex(int valueIndex) {
		if(valueIndex < 0 || valueIndex >= possibleValues.length){
			this.valueIndex = -1;
		}
		else {
			this.valueIndex = valueIndex;
		}
	}

	/**
	* Answer the value of the default setting for this particular option.
	*
	* @return String
	*/
	public String getDefaultValue() {
		if(possibleValues != NoDiscreteValue){
			if(defaultValueIndex == -1)
				return missing;
			return possibleValues[defaultValueIndex];
		}
		return defaultValue;
	}
	
	/**
	* Change the value of the default setting for this particular option.
	*
	* @param value String
	*/
	public void setDefaultValue(String defaultValue) {		
		if(possibleValues == NoDiscreteValue){
			this.defaultValue = defaultValue;
		}
		else{
			for(int i = 0 ; i < possibleValues.length ; i++){
				if(possibleValues[i].equals(defaultValue)){
					this.defaultValueIndex = i;
					break;
				}
			}
		}
	}
	
	public void setToDefault(){
		value = null;
		valueIndex = -1;
	}
	
	/**
	 * Gets the defaultValueIndex
	 * @return Returns a int
	 */
	public int getDefaultValueIndex() {
		if(possibleValues == NoDiscreteValue)
			return -1;
		
		return defaultValueIndex;
	}
	/**
	 * Sets the defaultValueIndex
	 * @param defaultValueIndex The defaultValueIndex to set
	 */
	public void setDefaultValueIndex(int defaultValueIndex) {
		if(defaultValueIndex < 0 || defaultValueIndex >= possibleValues.length){
			this.defaultValueIndex = -1;
		}
		else {
			this.defaultValueIndex = defaultValueIndex;
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
	* Return a String that represents the localized name of the receiver.
	* @return java.lang.String
	*/
	public String getName() {
		return name;
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
	* Return an array of String that represents the localized possible values of the receiver.
	*
	* @return java.lang.String[]
	*/
	public String[] getPossibleValues() {
		return possibleValues;
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
	 * Gets the type
	 * @return Returns a int
	 */
	public String getType() {
		return type;
	}

	/**
	 * Gets the max
	 * @return Returns a Number
	 */
	public Number getMax() {
		if(possibleValues == NoDiscreteValue){
			if(type.equals(INT)){
				if(maxExisting){
					return new Integer(maxInt);
				}
				else {
					return new Integer(Integer.MAX_VALUE);
				}
			}
			else if(type.equals(FLOAT)){
				if(maxExisting){
					return new Float(maxFloat);
				}
				else {
					return new Float(Float.MAX_VALUE);
				}
			}
		}
		return null;
	}

	/**
	 * Gets the min
	 * @return Returns a Number
	 */
	public Number getMin() {
		if(possibleValues == NoDiscreteValue){
			if(type.equals(INT)){
				if(minExisting){
					return new Integer(minInt);
				}
				else {
					return new Integer(Integer.MIN_VALUE);
				}
			}
			else if(type.equals(FLOAT)){
				if(minExisting){
					return new Float(minFloat);
				}
				else {
					return new Float(Float.MIN_VALUE);
				}
			}
		}
		return null;
	}
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Configurable option for "/*nonNLS*/);
		buffer.append(this.componentName).append("\n"/*nonNLS*/);
		buffer.append("- category:			"/*nonNLS*/).append(this.category).append("\n"/*nonNLS*/);
		buffer.append("- name:				"/*nonNLS*/).append(this.name).append("\n"/*nonNLS*/);
		/* display current value */
		buffer.append("- current value:	    "/*nonNLS*/).append(this.value).append("\n"/*nonNLS*/);
		/* display possible values */
		if (possibleValues != NoDiscreteValue){
			buffer.append("- possible values:	["/*nonNLS*/);
			for (int i = 0, max = possibleValues.length; i < max; i++) {
				if (i != 0)
					buffer.append(", "/*nonNLS*/);
				buffer.append(possibleValues[i]);
			}
			buffer.append("]\n"/*nonNLS*/);
		}
		buffer.append("- description:		"/*nonNLS*/).append(description).append("\n"/*nonNLS*/);
		return buffer.toString();
	}
	
	public static String[] getIDs(String componentName,Locale locale){
		try {
			ResourceBundle bundle = ResourceBundle.getBundle(componentName,locale);
			Enumeration bundleKeys = bundle.getKeys();
			
			String partialResult[] = new String[100];
			int resultCount = 0;

			while(bundleKeys.hasMoreElements()){
				String bundleKey = (String)bundleKeys.nextElement();
				if(bundleKey.endsWith("order"/*nonNLS*/)){
					int order;
					try{
						order = Integer.parseInt(bundle.getString(bundleKey));
					
						String id = bundleKey.substring(0,bundleKey.lastIndexOf('.'));
						if(partialResult.length <= order)
							System.arraycopy(partialResult,0,partialResult = new String[order+1],0,partialResult.length);
						partialResult[order]= id;
						resultCount++;
					} catch (NumberFormatException e) {
						//if order can not be read, option is not add
					}
				}
			}
			String[] result = new String[resultCount];
			resultCount = 0;
			for(int i = 0; i < partialResult.length;i++){
				if(partialResult[i]!= null){
					result[resultCount++]=partialResult[i];
					
				}
			}
			return result;
			
		} catch (MissingResourceException e) {
			return new String[0];
		}
	}
}
package org.eclipse.jdt.internal.codeassist.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.codeassist.*;
import org.eclipse.jdt.internal.compiler.ConfigurableOption;

public class CompletionOptions {
	/**
	 * Option IDs
	 */
	public static final String OPTION_PerformVisibilityCheck = CompletionEngine.class.getName() + ".performVisibilityCheck"; //$NON-NLS-1$
	public static final String OPTION_EntireWordReplacement = CompletionEngine.class.getName() + ".entireWordReplacement"; //$NON-NLS-1$
	
	private boolean visibilitySensitive = true;
	private boolean entireWordReplacement = true;

	/** 
	 * Initializing the completion engine options with default settings
	 */
	public CompletionOptions() {
	}
	/** 
	 * Initializing the completion engine options with external settings
	 */
	public CompletionOptions(ConfigurableOption[] settings) {
		if (settings == null)
			return;

		// filter options which are related to the formatter component
		String componentName = CompletionEngine.class.getName();
		for (int i = 0, max = settings.length; i < max; i++) {
			if (settings[i].getComponentName().equals(componentName)) {
				this.setOption(settings[i]);
			}
		}
	}

	public void setVisibilitySensitive(boolean visibilitySensitive){
		this.visibilitySensitive = visibilitySensitive;
	}
	
	public boolean checkVisibilitySensitive(){
		return visibilitySensitive;
	}

	public void setEntireWordReplacement(boolean entireWordReplacement){
		this.entireWordReplacement = entireWordReplacement;
	}
	
	public boolean checkEntireWordReplacement(){
		return entireWordReplacement;
	}
	
	/**
	 * Change the value of the option corresponding to the option number
	 *
	 * @param optionNumber <CODE>int</CODE>
	 * @param newValue <CODE>int</CODE>
	 */
	public void setOption(ConfigurableOption setting) {
		String componentName = CompletionEngine.class.getName();

		String optionID = setting.getID();

		if (optionID.equals(OPTION_PerformVisibilityCheck)) {
			setVisibilitySensitive(setting.getValueIndex() == 0);
		} else if (optionID.equals(OPTION_EntireWordReplacement)) {
			setEntireWordReplacement(setting.getValueIndex() == 0);
		}
	}
}
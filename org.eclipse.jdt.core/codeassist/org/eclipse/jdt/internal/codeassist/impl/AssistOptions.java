package org.eclipse.jdt.internal.codeassist.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

import org.eclipse.jdt.internal.codeassist.*;

public class AssistOptions {
	/**
	 * Option IDs
	 */
	public static final String OPTION_PerformVisibilityCheck = "org.eclipse.jdt.core.codeComplete.visibilityCheck"; //$NON-NLS-1$
	public static final String OPTION_EntireWordReplacement = "org.eclipse.jdt.core.codeComplete.entireWordReplacement"; //$NON-NLS-1$
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$
	public static final String REPLACE = "replace"; //$NON-NLS-1$
	public static final String DO_NOT_REPLACE = "do not replace"; //$NON-NLS-1$
	
	private boolean checkVisibility = false;
	private boolean replaceEntireWord = true;

	/** 
	 * Initializing the assist options with default settings
	 */
	public AssistOptions() {
	}

	/** 
	 * Initializing the assist options with external settings
	 */
	public AssistOptions(Map settings) {
	if (settings == null) return;
	
	// filter options which are related to the assist component
	Object[] entries = settings.entrySet().toArray();
	for (int i = 0, max = entries.length; i < max; i++){
		Map.Entry entry = (Map.Entry)entries[i];
		if (!(entry.getKey() instanceof String)) continue;
		if (!(entry.getValue() instanceof String)) continue;
		String optionID = (String) entry.getKey();
		String optionValue = (String) entry.getValue();
		
		if (optionID.equals(OPTION_PerformVisibilityCheck)) {
			if (optionValue.equals(ENABLED)){
				this.checkVisibility = true;
			} else if (optionValue.equals(DISABLED)){
				this.checkVisibility = false;
			}
			continue;
		} 
		if (optionID.equals(OPTION_EntireWordReplacement)) {
			if (optionValue.equals(REPLACE)){
				this.replaceEntireWord = true;
			} else if (optionValue.equals(DO_NOT_REPLACE)){
				this.replaceEntireWord = false;
			}
			continue;
		}
	}
}


	public boolean checkVisibility(){
		return this.checkVisibility;
	}

	public boolean replaceEntireWord(){
		return this.replaceEntireWord;
	}
}
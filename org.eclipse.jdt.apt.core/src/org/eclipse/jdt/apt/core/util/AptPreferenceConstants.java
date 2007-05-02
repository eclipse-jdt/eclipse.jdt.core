/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * String constants used to access APT preference store
 */
public class AptPreferenceConstants {
	public static final String APT_STRING_BASE = "org.eclipse.jdt.apt"; //$NON-NLS-1$
	public static final String APT_GENSRCDIR = APT_STRING_BASE + ".genSrcDir"; //$NON-NLS-1$
	public static final String APT_PROCESSOROPTIONS = APT_STRING_BASE + ".processorOptions"; //$NON-NLS-1$
	public static final String APT_RECONCILEENABLED = APT_STRING_BASE + ".reconcileEnabled"; //$NON-NLS-1$
	public static final String APT_PROCESSANNOTATIONS = "org.eclipse.jdt.core.compiler.processAnnotations"; //$NON-NLS-1$
	// backward compatibility prior to Eclipse 3.3:
	public static final String APT_ENABLED = APT_STRING_BASE + ".aptEnabled"; //$NON-NLS-1$

	// used for APT_PROCESSANNOTATIONS setting:
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	public static final String DISABLED = "disabled"; //$NON-NLS-1$
	
	// Used in preference to indicate null value for processor option
	public static final String APT_NULLVALUE = APT_STRING_BASE + ".NULLVALUE"; //$NON-NLS-1$

	public static final String DEFAULT_GENERATED_SOURCE_FOLDER_NAME = ".apt_generated"; //$NON-NLS-1$
	
	/**
	 * Processors should report this option in {@link com.sun.mirror.apt.AnnotationProcessorFactory#supportedOptions()}
	 * in order to enable type generation while editing, that is, during reconcile.
	 */
	public static final String RTTG_ENABLED_OPTION = "enableTypeGenerationInEditor"; //$NON-NLS-1$

	public static Map<String,String> DEFAULT_OPTIONS_MAP;
	
	static {
		Map<String,String> options = new HashMap<String,String>();
		options.put(AptPreferenceConstants.APT_ENABLED, "false"); //$NON-NLS-1$
		options.put(AptPreferenceConstants.APT_GENSRCDIR, DEFAULT_GENERATED_SOURCE_FOLDER_NAME);
		options.put(AptPreferenceConstants.APT_PROCESSOROPTIONS, ""); //$NON-NLS-1$
		options.put(AptPreferenceConstants.APT_RECONCILEENABLED, "true"); //$NON-NLS-1$
		DEFAULT_OPTIONS_MAP = Collections.unmodifiableMap(options);
	}
	
	/**
	 * Names of all apt settings that can be read from APT preference store.
	 * Order is unimportant.  Note that not all "apt settings" may be in the
	 * APT preference store - for instance, the factory path is kept in a
	 * separate file.  This list only applies to the information available
	 * from IPreferencesService.  See AptConfig for usage.
	 */
	public static final String[] OPTION_NAMES = {
		APT_ENABLED,
		APT_GENSRCDIR,
		APT_PROCESSOROPTIONS,
		APT_RECONCILEENABLED,
	};
	
	/**
	 * Number of apt settings in the APT preference store.
	 */
	public static final int NSETTINGS = OPTION_NAMES.length;
}



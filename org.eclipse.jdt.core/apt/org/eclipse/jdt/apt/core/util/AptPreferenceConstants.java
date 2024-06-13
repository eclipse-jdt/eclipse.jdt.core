/*******************************************************************************
 * Copyright (c) 2005, 2019 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
 *
 * This class is not intended to be subclassed or instantiated.
 */
public class AptPreferenceConstants {
	public static final String APT_STRING_BASE = "org.eclipse.jdt.apt"; //$NON-NLS-1$
	public static final String APT_GENSRCDIR = APT_STRING_BASE + ".genSrcDir"; //$NON-NLS-1$
	/**
	 * @since 3.6
	 */
	public static final String APT_GENTESTSRCDIR = APT_STRING_BASE + ".genTestSrcDir"; //$NON-NLS-1$
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
	 * @since 3.6
	 */
	public static final String DEFAULT_GENERATED_TEST_SOURCE_FOLDER_NAME = ".apt_generated_tests"; //$NON-NLS-1$

	/**
	 * Processors should report this option in {@link com.sun.mirror.apt.AnnotationProcessorFactory#supportedOptions()}
	 * in order to enable type generation while editing, that is, during reconcile.  This setting will be ignored if
	 * the processor also reports {@link #PROCESSING_IN_EDITOR_DISABLED_OPTION}.
	 */
	public static final String RTTG_ENABLED_OPTION = "enableTypeGenerationInEditor"; //$NON-NLS-1$

	/**
	 * Processors should report this option in {@link com.sun.mirror.apt.AnnotationProcessorFactory#supportedOptions()}
	 * in order to disable processing while editing, that is, during reconcile.  If this option is set, ie if processing
	 * is disabled, then the value of {@link #RTTG_ENABLED_OPTION} will be ignored.  Whether a processor is called
	 * during reconcile is also influenced by the project settings and the project factory path.
	 *
	 * @see AptConfig#setProcessDuringReconcile(org.eclipse.jdt.core.IJavaProject, boolean)
	 */
	public static final String PROCESSING_IN_EDITOR_DISABLED_OPTION = "disableProcessingInEditor"; //$NON-NLS-1$

	public static Map<String,String> DEFAULT_OPTIONS_MAP;

	static {
		Map<String,String> options = new HashMap<>();
		options.put(AptPreferenceConstants.APT_ENABLED, "false"); //$NON-NLS-1$
		options.put(AptPreferenceConstants.APT_GENSRCDIR, DEFAULT_GENERATED_SOURCE_FOLDER_NAME);
		options.put(AptPreferenceConstants.APT_GENTESTSRCDIR, DEFAULT_GENERATED_TEST_SOURCE_FOLDER_NAME);
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
		APT_GENTESTSRCDIR,
		APT_PROCESSOROPTIONS,
		APT_RECONCILEENABLED,
	};

	/**
	 * Number of apt settings in the APT preference store.
	 */
	public static final int NSETTINGS = OPTION_NAMES.length;
}



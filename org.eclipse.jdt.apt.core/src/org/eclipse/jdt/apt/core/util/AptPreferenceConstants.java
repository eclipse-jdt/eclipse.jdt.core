/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   {INITIAL_AUTHOR} - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.util;

/**
 * String constants used to access APT preference store
 */
public class AptPreferenceConstants {
	public static final String APT_ENABLED = "org.eclipse.jdt.apt.aptEnabled";
	public static final String APT_GENBINDIR = "org.eclipse.jdt.apt.genBinDir";
	public static final String APT_GENSRCDIR = "org.eclipse.jdt.apt.genSrcDir";

	/**
	 * Names of all apt settings that can be read from APT preference store.
	 * Order is unimportant.  Note that not all "apt settings" may be in the
	 * APT preference store - for instance, the factory path is kept in a
	 * separate file.  This list only applies to the information available
	 * from IPreferencesService.  See AptConfig for usage.
	 */
	public static final String[] OPTION_NAMES = {
		APT_ENABLED,
		APT_GENBINDIR,
		APT_GENSRCDIR
	};
	
	/**
	 * Number of apt settings in the APT preference store.
	 */
	public static final int NSETTINGS = OPTION_NAMES.length;
}



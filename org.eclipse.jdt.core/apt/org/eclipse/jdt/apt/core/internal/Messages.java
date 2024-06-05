/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.jdt.apt.core.internal.messages"; //$NON-NLS-1$

	private Messages() {
	}

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String AnnotationProcessorFactoryLoader_jarNotFound;

	public static String AnnotationProcessorFactoryLoader_ioError;

	public static String AnnotationProcessorFactoryLoader_factorypath_missingLibrary;

	public static String AnnotationProcessorFactoryLoader_factorypath;

	public static String AnnotationProcessorFactoryLoader_unableToLoadFactoryClass;

	public static String GeneratedFileManager_missing_classpath_entry;
}

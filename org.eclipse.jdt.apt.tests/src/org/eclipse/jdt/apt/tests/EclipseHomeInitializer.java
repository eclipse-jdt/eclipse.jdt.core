/*******************************************************************************
 * Copyright (c) Red Hat Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.apt.tests;

import java.net.URL;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.ClasspathVariableInitializer;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.osgi.service.datalocation.Location;

public class EclipseHomeInitializer extends ClasspathVariableInitializer {

	public static final String ECLIPSE_HOME_VARIABLE = "TEST_ECLIPSE_HOME"; //$NON-NLS-1$

	@Override
	public void initialize(String variable) {
		resetEclipseHomeVariable();
	}

	private static void resetEclipseHomeVariable() {
		try {
			JavaCore.setClasspathVariable(ECLIPSE_HOME_VARIABLE, IPath.fromOSString(getDefaultLocation()), null);
		} catch (CoreException e) {
		}
	}
	
	private static String getDefaultLocation() {
		Location location = Platform.getInstallLocation();
		if (location != null) {
			URL url = Platform.getInstallLocation().getURL();
			IPath path = IPath.fromOSString(url.getFile()).removeTrailingSeparator();
			return path.toOSString();
		}
		return ""; //$NON-NLS-1$
	}
}
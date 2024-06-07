/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.JavaCore;

class CompilationUnitResolverDiscovery {
	private static final String COMPILATION_UNIT_RESOLVER_EXTPOINT_ID = "compilationUnitResolver" ; //$NON-NLS-1$
	private static boolean ERROR_LOGGED = false;


	static ICompilationUnitResolver getInstance() {
		String compilationUnitResolverId = System.getProperty(ICompilationUnitResolver.class.getSimpleName());
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(JavaCore.PLUGIN_ID, COMPILATION_UNIT_RESOLVER_EXTPOINT_ID);
		if (extension != null && compilationUnitResolverId != null && !compilationUnitResolverId.isEmpty()) {
			IExtension[] extensions = extension.getExtensions();
			for (IExtension ext : extensions) {
				IConfigurationElement[] configElements = ext.getConfigurationElements();
				for (final IConfigurationElement configElement : configElements) {
					String elementId = configElement.getAttribute("id"); //$NON-NLS-1$
					if( compilationUnitResolverId.equals(elementId)) {
						String elementName =configElement.getName();
						if (!("resolver".equals(elementName))) { //$NON-NLS-1$
							continue;
						}
						try {
							Object executableExtension = configElement.createExecutableExtension("class"); //$NON-NLS-1$
							if( executableExtension instanceof ICompilationUnitResolver icur) {
								return icur;
							}
						} catch (CoreException e) {
							if( !ERROR_LOGGED) {
								ILog.get().error("Could not instantiate ICompilationUnitResolver: '" + elementId + "' with class: " + configElement.getAttribute("class"), e); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
								ERROR_LOGGED = true;
							}
						}
					}
				}
			}
		}
		return CompilationUnitResolver.FACADE;
	}

}

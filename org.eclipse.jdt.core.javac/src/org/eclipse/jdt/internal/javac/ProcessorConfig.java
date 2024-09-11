/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Copied from eclipse.jdt.core/org.eclipse.jdt.apt.core/src/org/eclipse/jdt/apt/core/util/AptConfig.java
 *
 * Contributors:
 *    jgarms@bea.com, wharley@bea.com - initial API and implementation
 *    het@google.com - Bug 423254 - There is no way to tell if a project's factory path is different from the workspace default
 *******************************************************************************/

package org.eclipse.jdt.internal.javac;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.osgi.service.prefs.BackingStoreException;

public class ProcessorConfig {
	private static final String APT_PLUGIN_ID = "org.eclipse.jdt.apt.core"; //$NON-NLS-1$
	private static final String APT_STRING_BASE = "org.eclipse.jdt.apt"; //$NON-NLS-1$ 1$
	private static final String APT_PROCESSOROPTIONS = APT_STRING_BASE + ".processorOptions"; //$NON-NLS-1$
	private static final String APT_NULLVALUE = APT_STRING_BASE + ".NULLVALUE"; //$NON-NLS-1$

	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
	/** path variable meaning "project root" */
	private static final String PATHVAR_PROJECTROOT = "%PROJECT.DIR%"; //$NON-NLS-1$

	public static Map<String, String> getProcessorOptions(IJavaProject jproj) {
		Map<String, String> rawOptions = getRawProcessorOptions(jproj);
		// map is large enough to also include the programmatically generated options
		Map<String, String> options = new HashMap<>(rawOptions.size());

		// Resolve path metavariables like %ROOT%
		for (Map.Entry<String, String> entry : rawOptions.entrySet()) {
			String resolvedValue = resolveVarPath(jproj, entry.getValue());
			String value = (resolvedValue == null) ? entry.getValue() : resolvedValue;
			options.put(entry.getKey(), value);
		}

		return options;
	}

	private static Map<String, String> getRawProcessorOptions(IJavaProject jproj) {
		Map<String, String> options = new HashMap<>();

		// Fall back from project to workspace scope on an all-or-nothing basis,
		// not value by value. (Never fall back to default scope; there are no
		// default processor options.) We can't use IPreferencesService for this
		// as we would normally do, because we don't know the names of the keys.
		IScopeContext[] contexts;
		if (jproj != null && jproj.getProject() != null) {
			contexts = new IScopeContext[] { new ProjectScope(jproj.getProject()), InstanceScope.INSTANCE };
		} else {
			contexts = new IScopeContext[] { InstanceScope.INSTANCE };
		}
		for (IScopeContext context : contexts) {
			IEclipsePreferences prefs = context.getNode(APT_PLUGIN_ID);
			try {
				if (prefs.childrenNames().length > 0) {
					IEclipsePreferences procOptionsNode = context.getNode(APT_PLUGIN_ID + "/" + APT_PROCESSOROPTIONS); //$NON-NLS-1$
					if (procOptionsNode != null) {
						for (String key : procOptionsNode.keys()) {
							String nonNullVal = procOptionsNode.get(key, null);
							String val = APT_NULLVALUE.equals(nonNullVal) ? null : nonNullVal;
							options.put(key, val);
						}
						break;
					}
				}
			} catch (BackingStoreException e) {
				ILog.get().error("Unable to load annotation processor options", e); //$NON-NLS-1$
			}
		}
		return options;
	}

	/**
	 * If the value starts with a path variable such as %ROOT%, replace it with the
	 * absolute path.
	 * 
	 * @param value the value of a -Akey=value command option
	 */
	private static String resolveVarPath(IJavaProject jproj, String value) {
		if (value == null) {
			return null;
		}
		// is there a token to substitute?
		if (!Pattern.matches(PATHVAR_TOKEN, value)) {
			return value;
		}
		IPath path = new Path(value);
		String firstToken = path.segment(0);
		// If it matches %ROOT%/project, it is a project-relative path.
		if (PATHVAR_ROOT.equals(firstToken)) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource proj = root.findMember(path.segment(1));
			if (proj == null) {
				return value;
			}
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(2);
			IPath absoluteProjPath = proj.getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}

		// If it matches %PROJECT.DIR%/project, the path is relative to the current
		// project.
		if (jproj != null && PATHVAR_PROJECTROOT.equals(firstToken)) {
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(1);
			IPath absoluteProjPath = jproj.getProject().getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}

		// otherwise it's a classpath-var-based path.
		String cpvName = firstToken.substring(1, firstToken.length() - 1);
		IPath cpvPath = JavaCore.getClasspathVariable(cpvName);
		if (cpvPath != null) {
			IPath resolved = cpvPath.append(path.removeFirstSegments(1));
			return resolved.toOSString();
		} else {
			return value;
		}
	}
}

/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
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
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Stores factories or libraries that were inaccessible when
 * attempting to load annotation processors, and then handles
 * reporting those errors to the user as markers in the problems pane.
 * <p>
 * This class is necessary due to deadlock possibilities in
 * {@link AnnotationProcessorFactoryLoader}. We need to gather up
 * the errors while holding a lock in that class,
 * and then later report them outside the lock, via the
 * reportFailureMarkers() method.
 */
public class LoadFailureHandler {

	private final IProject _project;
	private final List<String> _missingLibraries = new ArrayList<>();
	private final List<String> _failedFactories = new ArrayList<>();

	public LoadFailureHandler(IJavaProject proj) {
		_project = proj.getProject();
	}

	public void addMissingLibrary(String lib) {
		_missingLibraries.add(lib);
	}

	public void addFailedFactory(String factory) {
		_failedFactories.add(factory);
	}

	public void reportFailureMarkers() {
		reportFailureToLoadFactories();
		reportMissingLibraries();
	}

	/**
	 * Enter problem markers for factory containers that could not be found on
	 * disk.  This routine does not check whether markers already exist.
	 * See {@link AnnotationProcessorFactoryLoader} for information about
	 * the lifecycle of these markers.
	 */
	private void reportMissingLibraries() {
		for (String fc : _missingLibraries) {
			try {
				String message = Messages.bind(
						Messages.AnnotationProcessorFactoryLoader_factorypath_missingLibrary,
						new String[] {fc, _project.getName()});
				IMarker marker = _project.createMarker(AptPlugin.APT_LOADER_PROBLEM_MARKER);
				marker.setAttributes(
						new String[] {
							IMarker.MESSAGE,
							IMarker.SEVERITY,
							IMarker.LOCATION,
							IMarker.SOURCE_ID
						},
						new Object[] {
							message,
							IMarker.SEVERITY_ERROR,
							Messages.AnnotationProcessorFactoryLoader_factorypath,
							AptPlugin.APT_MARKER_SOURCE_ID
						}
					);
			} catch (CoreException e) {
				AptPlugin.log(e, "Unable to create APT build problem marker on project " + _project.getName()); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Enter a marker for a factory class that could not be loaded.
	 * Note that if a jar is missing, we won't be able to load its factory
	 * names, and thus we won't even try loading its factory classes; but
	 * we can still fail to load a factory class if, for instance, the
	 * jar is corrupted or the factory constructor throws an exception.
	 * See {@link AnnotationProcessorFactoryLoader} for information about
	 * the lifecycle of these markers.
	 */
	private void reportFailureToLoadFactories() {
		for (String factoryName : _failedFactories) {
			try {
				String message = Messages.bind(
						Messages.AnnotationProcessorFactoryLoader_unableToLoadFactoryClass,
						new String[] {factoryName, _project.getName()});
				IMarker marker = _project.createMarker(AptPlugin.APT_LOADER_PROBLEM_MARKER);
				marker.setAttributes(
						new String[] {
							IMarker.MESSAGE,
							IMarker.SEVERITY,
							IMarker.LOCATION,
							IMarker.SOURCE_ID
						},
						new Object[] {
							message,
							IMarker.SEVERITY_ERROR,
							Messages.AnnotationProcessorFactoryLoader_factorypath,
							AptPlugin.APT_MARKER_SOURCE_ID
						}
					);
			} catch (CoreException e) {
				AptPlugin.log(e, "Unable to create build problem marker"); //$NON-NLS-1$
			}
		}
	}

	@Override
	public int hashCode() {
		return _project.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof LoadFailureHandler)) return false;
		LoadFailureHandler otherHandler = (LoadFailureHandler)o;
		return _project.equals(otherHandler._project);
	}

}

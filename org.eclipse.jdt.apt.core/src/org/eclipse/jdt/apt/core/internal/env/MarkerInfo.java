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
package org.eclipse.jdt.apt.core.internal.env;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;

/**
 * Simple container class for attributes of IMarker
 */
public class MarkerInfo {

	private final int start;
	private final int end;
	private final Severity severity;
	private final String msg;
	private final int line;


	public MarkerInfo(final int start,
							final int end,
							final Severity severity,
							final String msg,
							final int line)
	{
		this.start = start;
		this.end = end;
		this.severity = severity;
		this.msg = msg;
		this.line = line;
	}

	private int getSeverity() {
		switch (severity) {
		case ERROR :
			return IMarker.SEVERITY_ERROR;
		case WARNING :
			return IMarker.SEVERITY_WARNING;
		case INFO :
			return IMarker.SEVERITY_INFO;
		}
		throw new IllegalStateException("Unhandled severity level: " + severity); //$NON-NLS-1$
	}

	public boolean isError(){
		return severity == Severity.ERROR;
	}

	/**
	 * Copy this info into the provided marker
	 */
	public void copyIntoMarker(IMarker marker) throws CoreException {
		marker.setAttribute(IMarker.CHAR_START, start);
		marker.setAttribute(IMarker.CHAR_END, end);
		marker.setAttribute(IMarker.SEVERITY, getSeverity());
		marker.setAttribute(IMarker.MESSAGE, msg);
		marker.setAttribute(IMarker.LINE_NUMBER, line);
	}
}

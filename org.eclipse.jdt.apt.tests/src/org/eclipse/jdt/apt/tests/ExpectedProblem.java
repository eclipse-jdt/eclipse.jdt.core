/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
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
package org.eclipse.jdt.apt.tests;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.tests.builder.Problem;

/**
 * Represents a problem expected to be found by the test.
 * Similar to an IProblem, but allows skipping of offset and category
 *
 */
public class ExpectedProblem {

	private final String _location;
	private final String _message;
	private final IPath _resourcePath;
	private final int _start;
	private final int _end;

	public ExpectedProblem(String location, String message, IPath resourcePath) {
		this (location, message, resourcePath, -1, -1);
	}

	public ExpectedProblem(String location,
			String message,
			IPath resourcePath,
			int start,
			int end)
	{
		_location = location;
		_message = message;
		_resourcePath = resourcePath;
		_start = start;
		_end = end;
	}

	public String getLocation() {
		return _location;
	}

	public boolean equalsProblem(final Problem problem) {
		if (problem == null)
			return false;

		// Ignore the location, as this is what Problem.equals does as well
		//if (!_location.equals(problem.getLocation())) return false;
		if (!_message.equals(problem.getMessage())) return false;
		if (!_resourcePath.equals(problem.getResourcePath())) return false;
		if (_start != -1 && _start != problem.getStart()) return false;
		if (_end != -1 && _end != problem.getEnd()) return false;

		return true;
	}

	@Override
	public String toString(){
  		return
			"Problem : "
			+ _message
			+ " [ resource : <"
			+ _resourcePath
			+ ">"
			+ (" range : <" + _start + "," + _end + ">")
			+ "]";
	}
}

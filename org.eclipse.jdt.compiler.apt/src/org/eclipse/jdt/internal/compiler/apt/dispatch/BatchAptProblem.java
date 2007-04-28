/*******************************************************************************
 * Copyright (c) 2001, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.apt.dispatch;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class BatchAptProblem extends DefaultProblem {
	private static final String MARKER_ID = "org.eclipse.jdt.compiler.apt.compiler.problem"; //$NON-NLS-1$
	public BatchAptProblem(
			char[] originatingFileName,
			String message,
			int id,
			String[] stringArguments,
			int severity,
			int startPosition,
			int endPosition,
			int line,
			int column) {
		super(originatingFileName,
			message,
			id,
			stringArguments,
			severity,
			startPosition,
			endPosition,
			line,
			column);
	}
	@Override
	public int getCategoryID() {
		return CAT_UNSPECIFIED;
	}

	@Override
	public String getMarkerType() {
		return MARKER_ID;
	}
}

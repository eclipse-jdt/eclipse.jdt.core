/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.CompilationParticipant;
import org.eclipse.jdt.core.compiler.ReconcileContext;

public class TestCompilationParticipant extends CompilationParticipant {

	public static CompilationParticipant PARTICIPANT;
	public static boolean failToInstantiate = false;

	public TestCompilationParticipant() {
		if (failToInstantiate)
			throw new RuntimeException();
	}

	public boolean isActive(IJavaProject project) {
		return PARTICIPANT != null && PARTICIPANT.isActive(project);
	}

	public void reconcile(ReconcileContext context) {
		PARTICIPANT.reconcile(context);
	}
}

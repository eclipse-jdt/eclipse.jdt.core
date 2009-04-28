/*******************************************************************************
 * Copyright (c) 2000, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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

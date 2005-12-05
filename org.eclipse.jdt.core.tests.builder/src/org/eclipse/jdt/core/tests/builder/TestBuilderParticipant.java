/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.*;

public class TestBuilderParticipant extends CompilationParticipant {

	public static CompilationParticipant PARTICIPANT;

public int buildStarting(IJavaProject project) {
	return PARTICIPANT.buildStarting(project);
}

public void cleanStarting(IJavaProject project) {
	PARTICIPANT.cleanStarting(project);
}

public void compileStarting(ICompilationParticipantResult[] files) {
	PARTICIPANT.compileStarting(files);
}

public boolean isActive(IJavaProject project) {
	return PARTICIPANT != null;
}

public boolean isAnnotationProcessor() {
	return PARTICIPANT != null && PARTICIPANT.isAnnotationProcessor();
}

public void processAnnotations(ICompilationParticipantResult[] filesWithAnnotations, boolean isBatchBuild) {
	PARTICIPANT.processAnnotations(filesWithAnnotations, isBatchBuild);
}
}

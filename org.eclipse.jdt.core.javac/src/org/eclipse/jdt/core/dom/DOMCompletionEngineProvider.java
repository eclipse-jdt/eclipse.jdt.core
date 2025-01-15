/*******************************************************************************
 * Copyright (c) 2025 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.DOMCompletionEngine;
import org.eclipse.jdt.internal.codeassist.ICompletionEngine;
import org.eclipse.jdt.internal.codeassist.ICompletionEngineProvider;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

public class DOMCompletionEngineProvider implements ICompletionEngineProvider {

	@Override
	public ICompletionEngine newCompletionEngine(SearchableEnvironment nameEnvironment, CompletionRequestor requestor,
			Map<String, String> settings, IJavaProject javaProject, WorkingCopyOwner owner, IProgressMonitor monitor) {
		return new DOMCompletionEngine(nameEnvironment, requestor, settings, javaProject, owner, monitor);
	}

}

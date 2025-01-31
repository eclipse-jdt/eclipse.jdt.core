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
package org.eclipse.jdt.core.tests.model;

import java.util.Map;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.internal.codeassist.ICompletionEngine;
import org.eclipse.jdt.internal.codeassist.ICompletionEngineProvider;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.core.SearchableEnvironment;

/**
 * Test implementation of ICompletionEngineProvider for {@link CompletionEngineProviderTests}.
 */
public class TestCompletionEngineProvider implements ICompletionEngineProvider {

	private static final char[] COMPLETION_TEXT = "test".toCharArray();

	@Override
	public ICompletionEngine newCompletionEngine(SearchableEnvironment nameEnvironment, CompletionRequestor requestor,
			Map<String, String> settings, IJavaProject javaProject, WorkingCopyOwner owner, IProgressMonitor monitor) {
		return new TestCompletionEngine(requestor);
	}

	private static class TestCompletionEngine implements ICompletionEngine {

		private CompletionRequestor requestor;

		public TestCompletionEngine(CompletionRequestor requestor) {
			this.requestor = requestor;
		}

		@Override
		public void complete(ICompilationUnit sourceUnit, int completionPosition, int unused, ITypeRoot root) {
			this.requestor.accept(createProposal(completionPosition));
		}

		private final CompletionProposal createProposal(int pos) {
			CompletionProposal res = CompletionProposal.create(CompletionProposal.FIELD_REF, pos);
			res.setCompletion(COMPLETION_TEXT);
			res.setName(COMPLETION_TEXT);
			res.setRelevance(100);
			return res;
		}

	}

}

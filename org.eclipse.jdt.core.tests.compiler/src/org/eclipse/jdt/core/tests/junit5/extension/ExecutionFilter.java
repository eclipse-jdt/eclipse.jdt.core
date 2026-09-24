/*******************************************************************************
 * Copyright (c) 2026 GK Software and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.junit5.extension;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class ExecutionFilter implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		if (TestCase.TESTS_NAMES != null && !TestCase.DISABLE_FILTERS && context.getTestMethod().isPresent()) {
			for (String name : TestCase.TESTS_NAMES) {
				if (context.getDisplayName().startsWith(name))
					return ConditionEvaluationResult.enabled(null);
			}
			return ConditionEvaluationResult.disabled("Filtered");
		}
		return ConditionEvaluationResult.enabled(null);
	}
}

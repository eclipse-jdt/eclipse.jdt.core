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

import java.util.Optional;
import org.eclipse.jdt.core.tests.compiler.regression.RunJavac;
import org.eclipse.jdt.core.tests.util.AbstractCompilerTest;
import org.eclipse.jdt.core.tests.util.MinimalCompliance;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class TestClassFilter implements ExecutionCondition {

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		if (context.getTestMethod().isEmpty()) { // only per-class filtering
			Optional<Class<?>> testClassOpt = context.getTestClass();
			if (testClassOpt.isPresent()) {
				Class<?> testClass = testClassOpt.get();
				// if -Drun.javac=optInOnly run only tests with @RunJava annotation:
				if (AbstractCompilerTest.ONLY_RUN_JAVA_OPT_IN && testClass.getAnnotation(RunJavac.class) == null) {
					return ConditionEvaluationResult.disabled("Not opting in for run.javac mode");
				}
				if (AbstractCompilerTest.class.isAssignableFrom(testClass)) {
					// run only tests with compatible compliances
					MinimalCompliance minimalCompliance = testClass.getAnnotation(MinimalCompliance.class);
					if (minimalCompliance != null) {
						int possibleComplianceLevels = AbstractCompilerTest.getPossibleComplianceLevels();
						if (possibleComplianceLevels < minimalCompliance.value())
							return ConditionEvaluationResult.disabled("No applicable compliance level enabled");
					}
				}
			}
		}
		return ConditionEvaluationResult.enabled(null);
	}
}

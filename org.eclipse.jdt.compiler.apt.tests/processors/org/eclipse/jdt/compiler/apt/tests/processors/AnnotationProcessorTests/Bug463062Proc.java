/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.jdt.compiler.apt.tests.processors.AnnotationProcessorTests;

import java.util.Set;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import org.eclipse.jdt.compiler.apt.tests.processors.base.BaseProcessor;

/**
 * A processor that should not be invoked. Reports an error if invoked.
 */
@SupportedAnnotationTypes({"targets.AnnotationProcessorTests.bug463062.EBean"})
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class Bug463062Proc extends BaseProcessor {
	@Override
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		reportFailure(); // The annotation processor should never be invoked (https://bugs.eclipse.org/bugs/show_bug.cgi?id=463062)
		return false;
	}

}

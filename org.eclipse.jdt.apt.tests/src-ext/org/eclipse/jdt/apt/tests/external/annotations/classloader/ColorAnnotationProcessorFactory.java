/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
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
 *
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.external.annotations.classloader;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class ColorAnnotationProcessorFactory implements AnnotationProcessorFactory {

	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

	public Collection<String> supportedAnnotationTypes() {
		return Collections.singleton("org.eclipse.jdt.apt.tests.external.annotations.classloader.ColorAnnotation");
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
		return new ColorAnnotationProcessor(env);
	}


}

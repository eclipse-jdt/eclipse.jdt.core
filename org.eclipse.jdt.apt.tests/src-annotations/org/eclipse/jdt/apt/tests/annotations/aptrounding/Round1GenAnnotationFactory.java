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
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.annotations.aptrounding;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class Round1GenAnnotationFactory implements AnnotationProcessorFactory{

	public static final List<String> SUPPORTED_TYPES;

	static{
		SUPPORTED_TYPES = new ArrayList<>();
		SUPPORTED_TYPES.add(GenBean.class.getName());
	}

	public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> decls, AnnotationProcessorEnvironment env) {
		return new Round1GenAnnotationProcessor(env);
	}

	public Collection<String> supportedAnnotationTypes() {
		return SUPPORTED_TYPES;
	}

	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}
}

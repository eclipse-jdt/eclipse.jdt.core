/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.tests.external.annotations.batch;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class BatchAnnotationFactory implements AnnotationProcessorFactory{
	
	private static final List<String> SUPPORTED_TYPES = 
		Collections.singletonList(Batch.class.getName());
	
	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> decls, 
			AnnotationProcessorEnvironment env) {
		return new BatchProcessor(env);
	}
	
	public Collection<String> supportedAnnotationTypes() {
		return SUPPORTED_TYPES;
	}
	public Collection<String> supportedOptions() {
		return Collections.emptyList();
	}

}
